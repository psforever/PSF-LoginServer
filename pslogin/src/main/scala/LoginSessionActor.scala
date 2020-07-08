package net.psforever.pslogin

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet.{PlanetSideGamePacket, _}
import net.psforever.packet.control._
import net.psforever.packet.game._
import org.log4s.MDC
import scodec.bits._
import MDCContextAware.Implicits._
import net.psforever.objects.Account
import net.psforever.objects.Default
import net.psforever.types.PlanetSideEmpire
import services.ServiceManager
import services.ServiceManager.Lookup
import services.account.{ReceiveIPAddress, RetrieveIPAddress, StoreAccountData}
import com.github.t3hnar.bcrypt._
import net.psforever.packet.game.LoginRespMessage.{LoginError, StationError, StationSubscriptionStatus}
import net.psforever.persistence
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.Future
import Database._
import java.net.InetAddress

class LoginSessionActor extends Actor with MDCContextAware {
  private[this] val log = org.log4s.getLogger

  import scala.concurrent.ExecutionContext.Implicits.global
  private case class UpdateServerList()

  val usernameRegex = """[A-Za-z0-9]{3,}""".r

  var sessionId: Long               = 0
  var leftRef: ActorRef             = ActorRef.noSender
  var rightRef: ActorRef            = ActorRef.noSender
  var accountIntermediary: ActorRef = ActorRef.noSender

  var updateServerListTask: Cancellable = Default.Cancellable

  var ipAddress: String         = ""
  var hostName: String          = ""
  var canonicalHostName: String = ""
  var port: Int                 = 0

  val serverName    = Config.app.world.serverName
  val publicAddress = new InetSocketAddress(InetAddress.getByName(Config.app.public), Config.app.world.port)

  // Reference: https://stackoverflow.com/a/50470009
  private val numBcryptPasses = 10

  override def postStop() = {
    if (updateServerListTask != null)
      updateServerListTask.cancel()
  }

  def receive = Initializing

  def Initializing: Receive = {
    case HelloFriend(aSessionId, pipe) =>
      this.sessionId = aSessionId
      leftRef = sender()
      if (pipe.hasNext) {
        rightRef = pipe.next
        rightRef !> HelloFriend(aSessionId, pipe)
      } else {
        rightRef = sender()
      }
      context.become(Started)
      ServiceManager.serviceManager ! Lookup("accountIntermediary")

    case _ =>
      log.error("Unknown message")
      context.stop(self)
  }

  def Started: Receive = {
    case ServiceManager.LookupResult("accountIntermediary", endpoint) =>
      accountIntermediary = endpoint
    case ReceiveIPAddress(address) =>
      ipAddress = address.Address
      hostName = address.HostName
      canonicalHostName = address.CanonicalHostName
      port = address.Port
    case UpdateServerList() =>
      updateServerList()
    case ControlPacket(_, ctrl) =>
      handleControlPkt(ctrl)
    case GamePacket(_, _, game) =>
      handleGamePkt(game)
    case default => failWithError(s"Invalid packet class received: $default")
  }

  def handleControlPkt(pkt: PlanetSideControlPacket) = {
    pkt match {
      /// TODO: figure out what this is what what it does for the PS client
      /// I believe it has something to do with reliable packet transmission and resending
      case sync @ ControlSync(diff, _, _, _, _, _, fa, fb) =>
        log.trace(s"SYNC: $sync")
        val serverTick = Math.abs(System.nanoTime().toInt) // limit the size to prevent encoding error
        sendResponse(PacketCoding.CreateControlPacket(ControlSyncResp(diff, serverTick, fa, fb, fb, fa)))

      case TeardownConnection(_) =>
        sendResponse(DropSession(sessionId, "client requested session termination"))

      case default =>
        log.error(s"Unhandled ControlPacket $default")
    }
  }

  def handleGamePkt(pkt: PlanetSideGamePacket) =
    pkt match {
      case LoginMessage(majorVersion, minorVersion, buildDate, username, password, token, revision) =>
        // TODO: prevent multiple LoginMessages from being processed in a row!! We need a state machine

        val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"

        accountIntermediary ! RetrieveIPAddress(sessionId)

        if (token.isDefined)
          log.info(s"New login UN:$username Token:${token.get}. $clientVersion")
        else {
//        log.info(s"New login UN:$username PW:$password. $clientVersion")
          log.info(s"New login UN:$username. $clientVersion")
        }

        accountLogin(username, password.get)

      case ConnectToWorldRequestMessage(name, _, _, _, _, _, _) =>
        log.info(s"Connect to world request for '$name'")
        val response = ConnectToWorldMessage(serverName, publicAddress.getHostString, publicAddress.getPort)
        sendResponse(PacketCoding.CreateGamePacket(0, response))
        sendResponse(DropSession(sessionId, "user transferring to world"))

      case _ =>
        log.debug(s"Unhandled GamePacket $pkt")
    }

  def accountLogin(username: String, password: String): Unit = {
    import ctx._
    val newToken = this.generateToken()

    val result = for {
      // backwards compatibility: prefer exact match first, then try lowercase
      accountsExact <- ctx.run(query[persistence.Account].filter(_.username == lift(username)))
      accountsLower <- accountsExact.headOption match {
        case None =>
          ctx.run(query[persistence.Account].filter(_.username.toLowerCase == lift(username).toLowerCase))
        case Some(_) =>
          Future.successful(Seq())
      }
      accountOption <- accountsExact.headOption orElse accountsLower.headOption match {
        case Some(account) => Future.successful(Some(account))
        case None => {
          Config.app.login.createMissingAccounts match {
            case true =>
              val passhash: String = password.bcrypt(numBcryptPasses)
              ctx.run(
                query[persistence.Account]
                  .insert(_.passhash -> lift(passhash), _.username -> lift(username))
                  .returningGenerated(_.id)
              ) flatMap { id => ctx.run(query[persistence.Account].filter(_.id == lift(id))) } map { accounts =>
                Some(accounts.head)
              }
            case false =>
              loginFailureResponse(username, newToken)
              Future.successful(None)
          }
        }
      }
      login <- accountOption match {
        case Some(account) =>
          (account.inactive, password.isBcrypted(account.passhash)) match {
            case (false, true) =>
              accountIntermediary ! StoreAccountData(newToken, new Account(account.id, account.username, account.gm))
              val future = ctx.run(
                query[persistence.Login].insert(
                  _.accountId         -> lift(account.id),
                  _.ipAddress         -> lift(ipAddress),
                  _.canonicalHostname -> lift(canonicalHostName),
                  _.hostname          -> lift(hostName),
                  _.port              -> lift(port)
                )
              )
              loginSuccessfulResponse(username, newToken)
              updateServerListTask =
                context.system.scheduler.scheduleWithFixedDelay(0 seconds, 2 seconds, self, UpdateServerList())
              future
            case (_, false) =>
              loginPwdFailureResponse(username, newToken)
              Future.successful(None)
            case (true, _) =>
              loginAccountFailureResponse(username, newToken)
              Future.successful(None)
          }
        case None => Future.successful(None)
      }
    } yield login

    result.onComplete {
      case Success(_) =>
      case Failure(e) => log.error(e.getMessage())
    }
  }

  def loginSuccessfulResponse(username: String, newToken: String) = {
    sendResponse(
      PacketCoding.CreateGamePacket(
        0,
        LoginRespMessage(
          newToken,
          LoginError.Success,
          StationError.AccountActive,
          StationSubscriptionStatus.Active,
          0,
          username,
          10001
        )
      )
    )
  }

  def loginPwdFailureResponse(username: String, newToken: String) = {
    log.info(s"Failed login to account $username")
    sendResponse(
      PacketCoding.CreateGamePacket(
        0,
        LoginRespMessage(
          newToken,
          LoginError.BadUsernameOrPassword,
          StationError.AccountActive,
          StationSubscriptionStatus.Active,
          685276011,
          username,
          10001
        )
      )
    )
  }

  def loginFailureResponse(username: String, newToken: String) = {
    log.info("DB problem")
    sendResponse(
      PacketCoding.CreateGamePacket(
        0,
        LoginRespMessage(
          newToken,
          LoginError.unk1,
          StationError.AccountActive,
          StationSubscriptionStatus.Active,
          685276011,
          username,
          10001
        )
      )
    )
  }

  def loginAccountFailureResponse(username: String, newToken: String) = {
    log.info(s"Account $username inactive")
    sendResponse(
      PacketCoding.CreateGamePacket(
        0,
        LoginRespMessage(
          newToken,
          LoginError.BadUsernameOrPassword,
          StationError.AccountClosed,
          StationSubscriptionStatus.Active,
          685276011,
          username,
          10001
        )
      )
    )
  }

  def generateToken() = {
    val r  = new scala.util.Random
    val sb = new StringBuilder
    for (_ <- 1 to 31) {
      sb.append(r.nextPrintableChar)
    }
    sb.toString
  }

  def updateServerList() = {
    val msg = VNLWorldStatusMessage(
      "Welcome to PlanetSide! ",
      Vector(
        WorldInformation(
          serverName,
          WorldStatus.Up,
          Config.app.world.serverType,
          Vector(WorldConnectionInfo(publicAddress)),
          PlanetSideEmpire.VS
        )
      )
    )
    sendResponse(PacketCoding.CreateGamePacket(0, msg))
  }

  def failWithError(error: String) = {
    log.error(error)
    //sendResponse(PacketCoding.CreateControlPacket(ConnectionClose()))
  }

  def sendResponse(cont: Any) = {
    log.trace("LOGIN SEND: " + cont)
    MDC("sessionId") = sessionId.toString
    rightRef !> cont
  }

  def sendRawResponse(pkt: ByteVector) = {
    log.trace("LOGIN SEND RAW: " + pkt)
    MDC("sessionId") = sessionId.toString
    rightRef !> RawPacket(pkt)
  }
}
