// Copyright (c) 2017 PSForever
import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet.{PlanetSideGamePacket, _}
import net.psforever.packet.control._
import net.psforever.packet.game._
import org.log4s.MDC
import scodec.bits._
import MDCContextAware.Implicits._
import com.github.mauricio.async.db.general.ArrayRowData
import com.github.mauricio.async.db.{Connection, QueryResult, RowData}
import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException
import net.psforever.objects.Account
import net.psforever.objects.DefaultCancellable
import net.psforever.types.PlanetSideEmpire
import services.ServiceManager
import services.ServiceManager.Lookup
import services.account.StoreAccountData
import com.github.t3hnar.bcrypt._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class LoginSessionActor extends Actor with MDCContextAware {
  private[this] val log = org.log4s.getLogger

  import scala.concurrent.ExecutionContext.Implicits.global
  private case class UpdateServerList()

  var sessionId : Long = 0
  var leftRef : ActorRef = ActorRef.noSender
  var rightRef : ActorRef = ActorRef.noSender
  var accountIntermediary : ActorRef = Actor.noSender

  var updateServerListTask : Cancellable = DefaultCancellable.obj

  private val numBcryptPasses = 4

  override def postStop() = {
    if(updateServerListTask != null)
      updateServerListTask.cancel()
  }

  def receive = Initializing

  def Initializing : Receive = {
    case HelloFriend(aSessionId, pipe) =>
      this.sessionId = aSessionId
      leftRef = sender()
      if(pipe.hasNext) {
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

  def Started : Receive = {
    case ServiceManager.LookupResult("accountIntermediary", endpoint) =>
      accountIntermediary = endpoint
    case UpdateServerList() =>
      updateServerList()
    case ControlPacket(_, ctrl) =>
      handleControlPkt(ctrl)
    case GamePacket(_, _, game) =>
      handleGamePkt(game)
    case default => failWithError(s"Invalid packet class received: $default")
  }

  def handleControlPkt(pkt : PlanetSideControlPacket) = {
    pkt match {
      /// TODO: figure out what this is what what it does for the PS client
      /// I believe it has something to do with reliable packet transmission and resending
      case sync @ ControlSync(diff, _, _, _, _, _, fa, fb) =>
        log.trace(s"SYNC: $sync")
        val serverTick = Math.abs(System.nanoTime().toInt) // limit the size to prevent encoding error
        sendResponse(PacketCoding.CreateControlPacket(ControlSyncResp(diff, serverTick, fa, fb, fb, fa)))

      case default =>
        log.error(s"Unhandled ControlPacket $default")
    }
  }

  // TODO: move to global configuration or database lookup
  val serverName = "PSForever"
  val serverAddress = new InetSocketAddress(LoginConfig.serverIpAddress.getHostAddress, 51001)

  /**
    * Queries the database for the given account username and authenticates with the
    * given password. If the account does not exist, a new account is automatically
    * created.
    *
    * @param username The typed username sent by the player login request
    * @param password The typed password sent by the player login request
    * @return (
    *         successful True if the account login/creation was successful
    *         newToken The token key to send back to the client
    * )
    */
  def accountLogin(username : String, password : String) : (Boolean, Option[String]) = {
    val connection: Connection = Database.getConnection
    Await.result(connection.connect, 5 seconds) // TODO remove awaits

    val accountLookupQuery: Future[QueryResult] = connection.sendPreparedStatement(
      "SELECT id, passhash FROM accounts where username=?", Array(username))

    val queryResult: Future[Any] = Database.query(accountLookupQuery)
    val newToken = Some(this.generateToken())

    try {
      val userData = Await.result(queryResult, 5 seconds) // TODO remove awaits

      userData match {
        case row : ArrayRowData => return handleExistingAccount(connection, username, password, newToken, row)
        case errorCode : Int => errorCode match {
          case Database.EMPTY_RESULT => return handleNewAccount(connection, username, password, newToken)
          case _ => log.error(s"Issue retrieving result set from database for account login")
        }
      }
    } catch {
      case e : GenericDatabaseException =>
        log.error(s"SQL exception $e")
      case e: Exception =>
        log.error(s"Unknown exception when executing SQL statement: $e")
    } finally {
      connection.disconnect
    }
    (false, newToken)
  }

  def handleExistingAccount(
      connection: Connection, username: String, password: String, newToken: Option[String], row: ArrayRowData
  ) : (Boolean, Option[String]) = {
    val accountId : Int = row(0).asInstanceOf[Int]
    val dbPassHash : String = row(1).asInstanceOf[String]

    if (password.isBcrypted(dbPassHash)) {
      log.info(s"Account password correct for $username!")
      accountIntermediary ! StoreAccountData(newToken.get, new Account(accountId, username))
      return (true, newToken)
    } else {
      log.info(s"Account password incorrect for $username")
    }

    (false, newToken)
  }

  def handleNewAccount(
      connection: Connection, username: String, password: String, newToken: Option[String]
  ) : (Boolean, Option[String]) = {
    log.info(s"Account $username does not exist, creating new account...")

    val bcryptPassword : String = password.bcrypt(numBcryptPasses)
    val createNewAccountTransaction : Future[QueryResult] = connection.inTransaction {
      c => c.sendPreparedStatement(
        "INSERT INTO accounts (username, passhash) VALUES(?,?) RETURNING id",
        Array(username, bcryptPassword))
    }
    val insertResult = Await.result(createNewAccountTransaction, 5 seconds) // TODO remove awaits

    insertResult match {
      case result : QueryResult =>
        if(result.rows.nonEmpty) {
          val accountId = result.rows.get.head(0).asInstanceOf[Int]
          accountIntermediary ! StoreAccountData(newToken.get, new Account(accountId, username))
          log.info(s"Successfully created new account for $username")
          return (true, newToken)
        } else {
          log.error(s"No result from account create insert for $username")
        }

      case _ =>
        log.error(s"Error creating new account for $username")
    }
    (false, newToken)
  }

  def handleGamePkt(pkt : PlanetSideGamePacket) = pkt match {
      case LoginMessage(majorVersion, minorVersion, buildDate, username, password, token, revision) =>
        // TODO: prevent multiple LoginMessages from being processed in a row!! We need a state machine
        import game.LoginRespMessage._

        val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"

        if(token.isDefined)
          log.info(s"New login UN:$username Token:${token.get}. $clientVersion")
        else
          log.info(s"New login UN:$username PW:$password. $clientVersion")

        // TODO: Make this non-blocking
        val (successfulLogin, newToken) = accountLogin(username, password.get)

        if(successfulLogin) {
          val response = LoginRespMessage(newToken.get, LoginError.Success, StationError.AccountActive,
            StationSubscriptionStatus.Active, 0, username, 10001)

          sendResponse(PacketCoding.CreateGamePacket(0, response))
          updateServerListTask = context.system.scheduler.schedule(0 seconds, 2 seconds, self, UpdateServerList())
        } else {
          val response = LoginRespMessage(newToken.get, LoginError.BadUsernameOrPassword, StationError.AccountActive,
            StationSubscriptionStatus.Active, 685276011, username, 10001)

          log.info(s"Failed login to account $username")
          sendResponse(PacketCoding.CreateGamePacket(0, response))
        }

      case ConnectToWorldRequestMessage(name, _, _, _, _, _, _) =>
        log.info(s"Connect to world request for '$name'")
        val response = ConnectToWorldMessage(serverName, serverAddress.getHostString, serverAddress.getPort)
        sendResponse(PacketCoding.CreateGamePacket(0, response))
        sendResponse(DropSession(sessionId, "user transferring to world"))

      case _ =>
        log.debug(s"Unhandled GamePacket $pkt")
  }

  def generateToken() = {
    val r = new scala.util.Random
    val sb = new StringBuilder
    for (i <- 1 to 31) {
      sb.append(r.nextPrintableChar)
    }
    sb.toString
  }

  def updateServerList() = {
    val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
      Vector(
        WorldInformation(
          serverName, WorldStatus.Up, ServerType.Beta, Vector(WorldConnectionInfo(serverAddress)), PlanetSideEmpire.VS
        )
      )
    )
    sendResponse(PacketCoding.CreateGamePacket(0, msg))
  }

  def failWithError(error : String) = {
    log.error(error)
    //sendResponse(PacketCoding.CreateControlPacket(ConnectionClose()))
  }

  def sendResponse(cont : Any) = {
    log.trace("LOGIN SEND: " + cont)
    MDC("sessionId") = sessionId.toString
    rightRef !> cont
  }

  def sendRawResponse(pkt : ByteVector) = {
    log.trace("LOGIN SEND RAW: " + pkt)
    MDC("sessionId") = sessionId.toString
    rightRef !> RawPacket(pkt)
  }
}
