package net.psforever.actors.net

import akka.actor.typed.receptionist.Receptionist

import java.net.{InetAddress, InetSocketAddress}
import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware, typed}
import akka.actor.typed.scaladsl.adapter._
import com.github.t3hnar.bcrypt._
import net.psforever.objects.{Account, Default}
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.packet.game.LoginRespMessage.{LoginError, StationError, StationSubscriptionStatus}
import net.psforever.packet.game._
import net.psforever.persistence
import net.psforever.services.ServiceManager
import net.psforever.services.ServiceManager.Lookup
import net.psforever.services.account.{ReceiveIPAddress, StoreAccountData}
import net.psforever.types.PlanetSideEmpire
import net.psforever.util.Config
import net.psforever.util.Database._

import java.security.MessageDigest
import org.joda.time.LocalDateTime

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.matching.Regex
import scala.util.{Failure, Success}



object LoginActor {
  sealed trait Command

  final case class ReceptionistListing(listing: Receptionist.Listing) extends Command
}

class LoginActor(middlewareActor: typed.ActorRef[MiddlewareActor.Command], connectionId: String, sessionId: Long)
    extends Actor
    with MDCContextAware {

  import scala.concurrent.ExecutionContext.Implicits.global

  private case class UpdateServerList()

  val usernameRegex: Regex = """[A-Za-z0-9]{3,}""".r

  var leftRef: ActorRef             = Default.Actor
  var rightRef: ActorRef            = Default.Actor
  var accountIntermediary: ActorRef = Default.Actor
  var sockets: typed.ActorRef[SocketPane.Command] = Default.typed.Actor

  var updateServerListTask: Cancellable = Default.Cancellable

  var ipAddress: String         = ""
  var hostName: String          = ""
  var canonicalHostName: String = ""
  var port: Int                 = 0

  val serverName: String = Config.app.world.serverName
  val gameTestServerAddress = new InetSocketAddress(InetAddress.getByName(Config.app.public), Config.app.world.port)

  private val bcryptRounds = 12

  ServiceManager.serviceManager ! Lookup("accountIntermediary")
  ServiceManager.receptionist ! Receptionist.Find(SocketPane.SocketPaneKey, context.self)

  override def postStop(): Unit = {
    if (updateServerListTask != null)
      updateServerListTask.cancel()
  }

  def receive: Receive = {
    case ServiceManager.LookupResult("accountIntermediary", endpoint) =>
      accountIntermediary = endpoint

    case SocketPane.SocketPaneKey.Listing(listings) =>
      sockets = listings.head

    case ReceiveIPAddress(address) =>
      ipAddress = address.Address
      hostName = address.HostName
      canonicalHostName = address.CanonicalHostName
      port = address.Port

    case UpdateServerList() =>
      updateServerList()

    case packet: PlanetSideGamePacket =>
      handleGamePkt(packet)

    case SocketPane.NextPort(_, _, portNum) =>
      val address = gameTestServerAddress.getAddress
      log.info(s"Connecting to ${address.getHostAddress.toLowerCase}: $portNum ...")
      val response = ConnectToWorldMessage(serverName, address.getHostAddress, portNum)
      middlewareActor ! MiddlewareActor.Send(response)
      middlewareActor ! MiddlewareActor.Close()

    case default =>
      failWithError(s"Invalid packet class received: $default")
  }

  def handleGamePkt(pkt: PlanetSideGamePacket): Unit =
    pkt match {
      case LoginMessage(majorVersion, minorVersion, buildDate, username, password, token, revision) =>
        // TODO: prevent multiple LoginMessages from being processed in a row!! We need a state machine
        val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"
        if (token.isDefined)
          log.debug(s"New login UN:$username Token:${token.get}. $clientVersion")
        else {
          log.debug(s"New login UN:$username. $clientVersion")
        }
        requestAccountLogin(username, password, token)

      case ConnectToWorldRequestMessage(name, _, _, _, _, _, _, _) =>
        log.info(s"Request to connect to world  '$name' ...")
        sockets ! SocketPane.GetNextPort("world", context.self)

      case _ =>
        log.warning(s"Unhandled GamePacket $pkt")
    }

  // generates a password from username and password combination
  // mimics the process the launcher follows and hashes the password salted by the username
  def generateNewPassword(username: String, password: String): String = {
    // salt password hash with username (like the launcher does) (username + password)
    val saltedPassword = username.concat(password)
    // https://stackoverflow.com/a/46332228
    // hash password (like the launcher sends)
    val hashedPassword = MessageDigest.getInstance("SHA-256")
      .digest(saltedPassword.getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
    // bcrypt hash for DB storage
    val bcryptedPassword = hashedPassword.bcryptBounded(bcryptRounds)
    bcryptedPassword
  }

  def requestAccountLogin(username: String, passwordOpt: Option[String], tokenOpt: Option[String]): Unit = {
    tokenOpt match {
      case Some(token) => accountLoginWithToken(token)
      case None        => accountLogin(username, passwordOpt.getOrElse(""))
    }
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
        // account found
        case Some(account) =>
          Future.successful(Some(account))
        // create new account
        case None =>
          if (Config.app.login.createMissingAccounts) {
            // generate bcrypted passwords
            val bcryptedPassword = generateNewPassword(username, password)
            val passhash = password.bcryptBounded(bcryptRounds)
            // save bcrypted password hash to DB
            ctx.run(
              query[persistence.Account]
                .insert(
                  _.password -> lift(bcryptedPassword),
                  _.passhash -> lift(passhash),
                  _.username -> lift(username)
                )
                .returningGenerated(_.id)
            ) flatMap { id => ctx.run(query[persistence.Account].filter(_.id == lift(id))) } map { accounts =>
              Some(accounts.head)
            }
          } else {
            loginFailureResponse(username, newToken)
            Future.successful(None)
          }
      }

      login <- accountOption match {
        case Some(account) =>
          // remember: this is the in client "StagingTest" login handling
          // the password is send in clear and needs to be checked against the "old" (only bcrypted) passhash
          // if there ever is a way to update the password in the future passhash and password need be updated
          (account.inactive, password.isBcryptedBounded(account.passhash)) match {
            case (false, true) =>
              accountIntermediary ! StoreAccountData(newToken, Account(account.id, account.username, account.gm))
              val future = ctx.run(
                query[persistence.Login].insert(
                  _.accountId         -> lift(account.id),
                  _.ipAddress         -> lift(ipAddress),
                  _.canonicalHostname -> lift(canonicalHostName),
                  _.hostname          -> lift(hostName),
                  _.port              -> lift(port)
                )
              )
              // handle new password
              if (account.password == "") {
                // generate bcrypted password
                // use username as provided by the user (db entry could be wrong), that is the way the launcher does it
                val bcryptedPassword = generateNewPassword(username, password)
                // update account, set password
                ctx.run(
                  query[persistence.Account]
                    .filter(_.username == lift(account.username))
                    .update(_.password -> lift(bcryptedPassword))
                )
              }
              loginSuccessfulResponse(username, newToken)
              updateServerListTask =
                context.system.scheduler.scheduleWithFixedDelay(0 seconds, 5 seconds, self, UpdateServerList())
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
      case Failure(e) => log.error(e.getMessage)
    }
  }

  def accountLoginWithToken(token: String): Unit = {
    import ctx._
    val newToken = this.generateToken()
    val result = for {
      accountsExact <- ctx.run(query[persistence.Account].filter(_.token.getOrNull == lift(token)))
      accountOption <- accountsExact.headOption match {
        case Some(account) =>
          // token expires after 2 hours
          // new connections and players leaving a world server will return to desktop
          if (LocalDateTime.now().isAfter(account.tokenCreated.get.plusHours(2))) {
            loginFailureResponseTokenExpired(token, newToken)
            Future.successful(None)
          } else {
            Future.successful(Some(account))
          }

        case None =>
          loginFailureResponseToken(token, newToken)
          Future.successful(None)
      }

      login <- accountOption match {
        case Some(account) =>
          (account.inactive, account.token.getOrElse("") == token) match {
            case (false, true) =>
              accountIntermediary ! StoreAccountData(newToken, Account(account.id, account.username, account.gm))
              val future = ctx.run(
                query[persistence.Login].insert(
                  _.accountId -> lift(account.id),
                  _.ipAddress -> lift(ipAddress),
                  _.canonicalHostname -> lift(canonicalHostName),
                  _.hostname -> lift(hostName),
                  _.port -> lift(port)
                )
              )
              loginSuccessfulResponseToken(account.username, token, newToken)
              updateServerListTask =
                context.system.scheduler.scheduleWithFixedDelay(0 seconds, 5 seconds, self, UpdateServerList())
              future

            case (_, false) =>
              loginFailureResponseToken(account.username, token, newToken)
              Future.successful(None)

            case (true, _) =>
              loginAccountFailureResponseToken(account.username, token, newToken)
              Future.successful(None)
          }
        case None => Future.successful(None)
      }
    } yield login

    result.onComplete {
      case Success(_) =>
      case Failure(e) => log.error(e.getMessage)
    }
  }

  def loginSuccessfulResponse(username: String, newToken: String): Unit = {
    middlewareActor ! MiddlewareActor.Send(
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
  }

  def loginSuccessfulResponseToken(username: String, token: String, newToken: String): Unit = {
    log.info(s"User $username logged in unsing token $token")
    middlewareActor ! MiddlewareActor.Send(
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
  }

  def loginPwdFailureResponse(username: String, newToken: String): Unit = {
    log.warning(s"Failed login to account $username")
    middlewareActor ! MiddlewareActor.Send(
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
  }

  def loginFailureResponseToken(token: String, newToken: String): Unit = {
    log.warning(s"Failed login using unknown token $token")
    middlewareActor ! MiddlewareActor.Send(
      LoginRespMessage(
        newToken,
        LoginError.BadUsernameOrPassword,
        StationError.AccountActive,
        StationSubscriptionStatus.Active,
        685276011,
        "",
        10001
      )
    )
  }

  def loginFailureResponseTokenExpired(token: String, newToken: String): Unit = {
    log.warning(s"Failed login using expired token $token")
    middlewareActor ! MiddlewareActor.Send(
      LoginRespMessage(
        newToken,
        LoginError.BadUsernameOrPassword,
        StationError.AccountActive,
        StationSubscriptionStatus.Active,
        685276011,
        "",
        10001
      )
    )
  }

  def loginFailureResponse(username: String, newToken: String): Unit = {
    log.warning(s"DB problem username: $username")
    middlewareActor ! MiddlewareActor.Send(
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
  }

  def loginFailureResponseToken(username: String, token: String, newToken: String): Unit = {
    log.warning(s"DB problem username $username token: $token")
    middlewareActor ! MiddlewareActor.Send(
      LoginRespMessage(
        newToken,
        LoginError.unk1,
        StationError.AccountActive,
        StationSubscriptionStatus.Active,
        685276011,
        "",
        10001
      )
    )
  }

  def loginAccountFailureResponse(username: String, newToken: String): Unit = {
    log.warning(s"Account $username inactive")
    middlewareActor ! MiddlewareActor.Send(
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
  }

  def loginAccountFailureResponseToken(username: String, token: String, newToken: String): Unit = {
    log.warning(s"Account $username inactive token: $token ")
    middlewareActor ! MiddlewareActor.Send(
      LoginRespMessage(
        newToken,
        LoginError.BadUsernameOrPassword,
        StationError.AccountClosed,
        StationSubscriptionStatus.Active,
        685276011,
        "",
        10001
      )
    )
  }

  def generateToken(): String = {
    val r = new scala.util.Random
    val sb = new mutable.StringBuilder
    for (_ <- 1 to 31) {
      sb.append(r.nextPrintableChar())
    }
    sb.toString
  }

  def updateServerList(): Unit = {
    middlewareActor ! MiddlewareActor.Send(
      VNLWorldStatusMessage(
        "Welcome to PlanetSide! ",
        Vector(
          WorldInformation(
            serverName,
            WorldStatus.Up,
            Config.app.world.serverType,
            Vector(WorldConnectionInfo(gameTestServerAddress)), //todo ideally, ask for info from SocketPane
            PlanetSideEmpire.VS
          )
        )
      )
    )
  }

  def failWithError(error: String): Unit = {
    log.error(error)
    middlewareActor ! MiddlewareActor.Close()
  }
}
