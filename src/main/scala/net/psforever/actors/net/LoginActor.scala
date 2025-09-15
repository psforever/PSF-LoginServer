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
import scala.util.{Failure, Success}

object LoginActor {
  sealed trait Command

  private case object UpdateServerList extends Command

  final case class ReceptionistListing(listing: Receptionist.Listing) extends Command

  /**
   * What does a token do?
   * No one knows.
   * @return a 32-bit ascii string
   */
  private def generateToken(): String = {
    val r = new scala.util.Random
    val sb = new mutable.StringBuilder
    for (_ <- 1 to 31) {
      sb.append(r.nextPrintableChar())
    }
    sb.toString
  }

  /**
   * Generates a new password from username and password combination,
   * hashing the initial password when salted by the username,
   * mimicking the process the launcher follows.
   * @param username part of the original details
   * @param password part of the original details
   * @param rounds number of times cryptographic mutation occurs
   * @return new password
   */
  private def generateNewPassword(username: String, password: String, rounds: Int): String = {
    // salt password hash with username (like the launcher does) (username + password)
    val saltedPassword = username.concat(password)
    // https://stackoverflow.com/a/46332228
    // hash password (like the launcher sends)
    val hashedPassword = MessageDigest.getInstance("SHA-256")
      .digest(saltedPassword.getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
    // bcrypt hash for DB storage
    val bcryptedPassword = hashedPassword.bcryptBounded(rounds)
    bcryptedPassword
  }

  /**
   * Remove flavor from the server name that should not show up in the log.
   * @param name original name
   * @return sanitized name
   */
  private def sanitizeServerName(name: String): String = {
    //remove color codes from the server name - look for '\\#' followed by six characters or numbers
    name.replaceAll("\\\\#[\\da-fA-F]{6}","")
  }
}

class LoginActor(middlewareActor: typed.ActorRef[MiddlewareActor.Command], connectionId: String, sessionId: Long)
    extends Actor
    with MDCContextAware {
  import scala.concurrent.ExecutionContext.Implicits.global

  //private val usernameRegex: Regex = """[A-Za-z\d]{3,}""".r might be useful one day
  private var accountIntermediary: ActorRef = Default.Actor
  private var sockets: typed.ActorRef[SocketPane.Command] = Default.typed.Actor

  private var updateServerListTask: Cancellable = Default.Cancellable

  private var ipAddress: String         = ""
  private var hostName: String          = ""
  private var canonicalHostName: String = ""
  private var port: Int                 = 0

  private val serverName: String = Config.app.world.serverName
  private val gameTestServerAddress = new InetSocketAddress(InetAddress.getByName(Config.app.public), Config.app.world.port)

  private val bcryptRounds = 12

  private var buffer: Seq[Any] = Seq() //for typed actors, this becomes an akka.actor.typed.scaladsl.StashBuffer (size 10?)

  log.info("LOGIN_TEST::actor start: port activity starts the actor")

  override def preStart(): Unit = {
    super.preStart()
    log.info("LOGIN_TEST::actor start: formal actor prestart")
    ServiceManager.serviceManager ! Lookup("accountIntermediary")
    ServiceManager.receptionist ! Receptionist.Find(SocketPane.SocketPaneKey, context.self)
  }

  override def postStop(): Unit = {
    if (updateServerListTask != null)
      updateServerListTask.cancel()
  }

  def receive: Receive = beforeLoginBehavior

  private def persistentSetupMixinBehavior: Receive = {
    case ServiceManager.LookupResult("accountIntermediary", endpoint) =>
      log.info("LOGIN_TEST::account intermediary received (must be completed before actual login attempt)")
      accountIntermediary = endpoint

    case SocketPane.SocketPaneKey.Listing(listings) =>
      log.info("LOGIN_TEST::socket pane received (must be completed before actual login attempt)")
      sockets = listings.head
  }

  private def idlingBufferBehavior: Receive = persistentSetupMixinBehavior.orElse {
    case packet =>
      buffer = buffer :+ packet
  }

  private def idlingIgnoreBehavior: Receive = persistentSetupMixinBehavior.orElse {
    case _ => ()
  }

 private def beforeLoginBehavior: Receive = persistentSetupMixinBehavior.orElse {
   case ReceiveIPAddress(address) =>
     ipAddress = address.Address
     hostName = address.HostName
     canonicalHostName = address.CanonicalHostName
     port = address.Port
     context.become(idlingBufferBehavior)
     runLoginTest()
     log.info("LOGIN_TEST::ip address received")

    case _ => ()
      log.info("LOGIN_TEST::weird message when waiting for ip address")
  }

  private def accountLoginBehavior: Receive = persistentSetupMixinBehavior.orElse {
    case packet: PlanetSideGamePacket =>
      handleGamePktDuringLogin(packet)

    case default =>
      failWithError(s"Invalid packet class received: $default")
  }

  private def nextPortTransferBehavior: Receive = {
    case SocketPane.NextPort(_, _, portNum) =>
      val address = gameTestServerAddress.getAddress.getHostAddress
      log.info(s"Connecting to ${address.toLowerCase}: $portNum ...")
      val response = ConnectToWorldMessage(serverName, address, portNum)
      context.become(idlingIgnoreBehavior)
      middlewareActor ! MiddlewareActor.Send(response)
      middlewareActor ! MiddlewareActor.Close()
  }

  private def displayingServerListBehavior: Receive = persistentSetupMixinBehavior
    .orElse(nextPortTransferBehavior)
    .orElse {
      case packet: PlanetSideGamePacket =>
        handleGamePktDuringWorldSelect(packet)

      case LoginActor.UpdateServerList =>
        updateServerList()

      case default =>
        failWithError(s"Invalid packet class received: $default")
  }

  private def waitingForServerTransferBehavior: Receive = persistentSetupMixinBehavior
    .orElse(nextPortTransferBehavior)
    .orElse {
      case _ => ()
  }

  private def handleGamePktDuringLogin(pkt: PlanetSideGamePacket): Unit = {
    pkt match {
      case LoginMessage(majorVersion, minorVersion, buildDate, username, _, Some(token), revision) =>
        val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"
        log.debug(s"New login UN:$username Token:$token. $clientVersion")
        context.become(idlingIgnoreBehavior)
        accountLoginWithToken(token)

      case LoginMessage(majorVersion, minorVersion, buildDate, username, password, None, revision) =>
        val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"
        log.debug(s"New login UN:$username. $clientVersion")
        context.become(idlingIgnoreBehavior)
        accountLogin(username, password.getOrElse(""))

      case _ =>
        log.warning(s"Unhandled GamePacket during login $pkt")
    }
  }

  private def handleGamePktDuringWorldSelect(pkt: PlanetSideGamePacket): Unit = {
    pkt match {
      case ConnectToWorldRequestMessage(name, _, _, _, _, _, _, _) =>
        val sanitizedName = LoginActor.sanitizeServerName(name)
        log.info(s"Request to connect to world '$sanitizedName' ...")
        context.become(waitingForServerTransferBehavior)
        sockets ! SocketPane.GetNextPort("world", context.self)

      case _ =>
        log.warning(s"Unhandled GamePacket during world select $pkt")
    }
  }

  private def runLoginTest(): Unit = {
    import ctx._
    val result = for {
      accountsExact <- ctx.run(query[persistence.Account].filter(_.username == lift("PSForever")))
      accountOption <- accountsExact.headOption match {
        case Some(account) =>
          Future.successful(Some(account))
        case None =>
          Future.successful(None)
      }
    } yield accountOption

    result.onComplete {
      case Success(Some(_)) =>
        log.info("LOGIN_TEST::login test passed")
        context.become(accountLoginBehavior) // account found
        buffer.foreach { self ! _ }
        buffer = Seq()
      case Success(None) =>
        log.error("account database not found")
        log.info("LOGIN_TEST::login test failed, not found")
        middlewareActor ! MiddlewareActor.Send(DisconnectMessage("Account database not found; stopping ..."))
        middlewareActor ! MiddlewareActor.Close()
      case Failure(e) =>
        log.error(e.getMessage)
        middlewareActor ! MiddlewareActor.Send(DisconnectMessage("Encountered login error; stopping ..."))
        middlewareActor ! MiddlewareActor.Close()
    }
  }

  private def accountLogin(username: String, password: String): Unit = {
    import ctx._
    val newToken = LoginActor.generateToken()
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
            val bcryptedPassword = LoginActor.generateNewPassword(username, password, bcryptRounds)
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
                val bcryptedPassword = LoginActor.generateNewPassword(username, password, bcryptRounds)
                // update account, set password
                ctx.run(
                  query[persistence.Account]
                    .filter(_.username == lift(account.username))
                    .update(_.password -> lift(bcryptedPassword))
                )
              }
              log.info("LOGIN_TEST::login successful")
              loginSuccessfulResponse(username, newToken)
              context.become(displayingServerListBehavior)
              updateServerListTask =
                context.system.scheduler.scheduleWithFixedDelay(0 seconds, 5 seconds, self, LoginActor.UpdateServerList)
              future

            case (_, false) =>
              log.info("LOGIN_TEST::login failure, password")
              loginFailurePasswordResponse(username, newToken)
              loginFailureAction()

            case (true, _) =>
              log.info("LOGIN_TEST::login failure, general but with token")
              loginAccountFailureResponse(username, newToken)
              loginFailureAction()
          }
        case None =>
          log.info("LOGIN_TEST::login failure, general")
          loginFailureAction()
      }
    } yield login

    result.onComplete {
      case Success(_) => ()
      case Failure(e) => log.error(e.getMessage)
    }
  }

  private def accountLoginWithToken(token: String): Unit = {
    import ctx._
    val newToken = LoginActor.generateToken()
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
              loginSuccessfulResponseWithToken(account.username, token, newToken)
              context.become(displayingServerListBehavior)
              updateServerListTask =
                context.system.scheduler.scheduleWithFixedDelay(0 seconds, 5 seconds, self, LoginActor.UpdateServerList)
              future

            case (_, false) =>
              loginFailureResponseToken(account.username, token, newToken)
              loginFailureAction()

            case (true, _) =>
              loginAccountFailureResponseToken(account.username, token, newToken)
              loginFailureAction()
          }
        case None =>
          loginFailureAction()
      }
    } yield login

    result.onComplete {
      case Success(_) => ()
        log.info("LOGIN_TEST::login successful")
      case Failure(e) => log.error(e.getMessage)
    }
  }

  private def loginSuccessfulResponse(username: String, token: String): Unit = {
    middlewareActor ! MiddlewareActor.Send(
      LoginRespMessage(
        token,
        LoginError.Success,
        StationError.AccountActive,
        StationSubscriptionStatus.Active,
        0,
        username,
        10001
      )
    )
  }

  private def loginSuccessfulResponseWithToken(username: String, token: String, newToken: String): Unit = {
    log.info(s"User $username logged in using token $token")
    loginSuccessfulResponse(username, newToken)
  }

  private def loginFailureAction(): Future[Any] = {
    context.become(accountLoginBehavior)
    Future.successful(None)
  }

  private def loginFailurePasswordResponse(username: String, token: String): Unit = {
    log.warning(s"Failed login to account $username")
    middlewareActor ! MiddlewareActor.Send(
      LoginRespMessage(
        token,
        LoginError.BadUsernameOrPassword,
        StationError.AccountActive,
        StationSubscriptionStatus.Active,
        685276011,
        username,
        10001
      )
    )
  }

  private def loginFailureResponseToken(token: String, newToken: String): Unit = {
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

  private def loginFailureResponseTokenExpired(token: String, newToken: String): Unit = {
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

  private def loginFailureResponse(username: String, token: String): Unit = {
    log.warning(s"DB problem username: $username")
    middlewareActor ! MiddlewareActor.Send(
      LoginRespMessage(
        token,
        LoginError.unk1,
        StationError.AccountActive,
        StationSubscriptionStatus.Active,
        685276011,
        username,
        10001
      )
    )
  }

  private def loginFailureResponseToken(username: String, token: String, newToken: String): Unit = {
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

  private def loginAccountFailureResponse(username: String, token: String): Unit = {
    log.warning(s"Account $username inactive")
    middlewareActor ! MiddlewareActor.Send(
      LoginRespMessage(
        token,
        LoginError.BadUsernameOrPassword,
        StationError.AccountClosed,
        StationSubscriptionStatus.Active,
        685276011,
        username,
        10001
      )
    )
  }

  private def loginAccountFailureResponseToken(username: String, token: String, newToken: String): Unit = {
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

  private def updateServerList(): Unit = {
    //todo list of game servers from database, eventually, which is a separation of game server from login server
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

  private def failWithError(error: String): Unit = {
    log.error(error)
    middlewareActor ! MiddlewareActor.Close()
  }
}
