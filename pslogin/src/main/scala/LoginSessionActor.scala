// Copyright (c) 2017 PSForever
import java.net.InetSocketAddress
import java.net.InetAddress

import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet.{PlanetSideGamePacket, _}
import net.psforever.packet.control._
import net.psforever.packet.game._
import org.log4s.MDC
import scodec.bits._
import MDCContextAware.Implicits._
import com.github.mauricio.async.db.general.ArrayRowData
import com.github.mauricio.async.db.{Connection, QueryResult}
import net.psforever.objects.Account
import net.psforever.objects.DefaultCancellable
import net.psforever.types.PlanetSideEmpire
import services.ServiceManager
import services.ServiceManager.Lookup
import services.account.{ReceiveIPAddress, RetrieveIPAddress, StoreAccountData}
import com.github.t3hnar.bcrypt._
import net.psforever.packet.game.LoginRespMessage.{LoginError, StationError, StationSubscriptionStatus}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

case class StartAccountAuthentication(connection: Option[Connection], username: String, password: String, newToken: String, queryResult: Any)
case class FinishAccountLogin(connection: Option[Connection], username: String, newToken: String, isSuccessfulLogin: Boolean, isInactive:Boolean = false)
case class CreateNewAccount(connection: Option[Connection], username: String, password: String, newToken: String)
case class LogTheLoginOccurrence(connection: Option[Connection], username: String, newToken: String, isSuccessfulLogin: Boolean, accountId: Int)

class LoginSessionActor extends Actor with MDCContextAware {
  private[this] val log = org.log4s.getLogger

  import scala.concurrent.ExecutionContext.Implicits.global
  private case class UpdateServerList()

  var sessionId : Long = 0
  var leftRef : ActorRef = ActorRef.noSender
  var rightRef : ActorRef = ActorRef.noSender
  var accountIntermediary : ActorRef = Actor.noSender

  var updateServerListTask : Cancellable = DefaultCancellable.obj

  var ipAddress : String = ""
  var hostName : String = ""
  var canonicalHostName : String = ""
  var port : Int = 0

  val serverName = WorldConfig.Get[String]("worldserver.ServerName")

  // This MUST be an IP address. The client DOES NOT do name resolution 
  var serverHost : String = if (WorldConfig.Get[String]("worldserver.Hostname") != "")
    InetAddress.getByName(WorldConfig.Get[String]("worldserver.Hostname")).getHostAddress
  else
    LoginConfig.serverIpAddress.getHostAddress

  val serverAddress = new InetSocketAddress(serverHost, WorldConfig.Get[Int]("worldserver.ListeningPort"))

  // Reference: https://stackoverflow.com/a/50470009
  private val numBcryptPasses = 10

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

  def handleControlPkt(pkt : PlanetSideControlPacket) = {
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

  def handleGamePkt(pkt : PlanetSideGamePacket) = pkt match {
    case LoginMessage(majorVersion, minorVersion, buildDate, username, password, token, revision) =>
      // TODO: prevent multiple LoginMessages from being processed in a row!! We need a state machine

      val clientVersion = s"Client Version: $majorVersion.$minorVersion.$revision, $buildDate"

      accountIntermediary ! RetrieveIPAddress(sessionId)

      if(token.isDefined)
        log.info(s"New login UN:$username Token:${token.get}. $clientVersion")
      else {
//        log.info(s"New login UN:$username PW:$password. $clientVersion")
        log.info(s"New login UN:$username. $clientVersion")
      }

      startAccountLogin(username, password.get)

    case ConnectToWorldRequestMessage(name, _, _, _, _, _, _) =>
      log.info(s"Connect to world request for '$name'")
      val response = ConnectToWorldMessage(serverName, serverAddress.getHostString, serverAddress.getPort)
      sendResponse(PacketCoding.CreateGamePacket(0, response))
      sendResponse(DropSession(sessionId, "user transferring to world"))

    case _ =>
      log.debug(s"Unhandled GamePacket $pkt")
  }

  def startAccountLogin(username: String, password: String) : Unit = {
    val newToken = this.generateToken()
    Database.getConnection.connect.onComplete {
      case Success(connection) =>
        Database.query(connection.sendPreparedStatement(
          "SELECT id, passhash, inactive, gm FROM accounts where username=?", Array(username)
        )).onComplete {
          case Success(queryResult) =>
            context.become(startAccountAuthentication)
            self ! StartAccountAuthentication(Some(connection), username, password, newToken, queryResult)
          case Failure(e) =>
            log.error("Failed account lookup query " + e.getMessage)
            connection.disconnect
            context.become(finishAccountLogin)
            self ! FinishAccountLogin(Some(connection), username, newToken, false)
        }
      case Failure(e) =>
        log.error("Failed connecting to database for account lookup " + e.getMessage)
        context.become(finishAccountLogin)
        self ! FinishAccountLogin(None, username, newToken, false)
    }
  }

  def startAccountAuthentication : Receive = {
    case StartAccountAuthentication(connection, username, password, newToken, queryResult) =>
      queryResult match {

        // If we got a row from the database
        case row: ArrayRowData =>
          val (isSuccessfulLogin, accountId) = authenticateExistingAccount(connection.get, username, password, newToken, row)
          if(isSuccessfulLogin) { // login OK
            context.become(logTheLoginOccurrence)
            self ! LogTheLoginOccurrence(connection, username, newToken, isSuccessfulLogin, accountId)
          } else {
            if (accountId == 0) { // Bad password
              context.become(finishAccountLogin)
              self ! FinishAccountLogin(connection, username, newToken, isSuccessfulLogin)
            } else { // Account inactive
              context.become(finishAccountLogin)
              self ! FinishAccountLogin(connection, username, newToken, isSuccessfulLogin, true)
            }
          }

        // If the account didn't exist in the database
        case errorCode: Int => errorCode match {
          case Database.EMPTY_RESULT =>
            if (WorldConfig.Get[Boolean]("loginserver.CreateMissingAccounts")) {
              self ! CreateNewAccount(connection, username, password, newToken)
              context.become(createNewAccount)
            } else {
              context.become(finishAccountLogin)
              self ! FinishAccountLogin(connection, username, newToken, false)
            }

          case _ =>
            log.error(s"Issue retrieving result set from database for account $username")
            context.become(finishAccountLogin)
            self ! FinishAccountLogin(connection, username, newToken, false)
        }
      }
    case default =>
      failWithError(s"Invalid message '$default' received in startAccountAuthentication")
  }

  def createNewAccount : Receive = {
    case CreateNewAccount(connection, username, password, newToken) =>
      log.info(s"Account $username does not exist, creating new account...")
      val bcryptPassword : String = password.bcrypt(numBcryptPasses)

      connection.get.inTransaction {
        c => c.sendPreparedStatement(
          "INSERT INTO accounts (username, passhash) VALUES(?,?) RETURNING id",
          Array(username, bcryptPassword)
        )
      }.onComplete {
        case Success(insertResult) =>
          insertResult match {
            case result: QueryResult =>
              if (result.rows.nonEmpty) {
                val accountId = result.rows.get.head(0).asInstanceOf[Int]
                accountIntermediary ! StoreAccountData(newToken, new Account(accountId, username))
                log.info(s"Successfully created new account for $username")
                context.become(logTheLoginOccurrence)
                self ! LogTheLoginOccurrence(connection, username, newToken, true, accountId)
              } else {
                log.error(s"No result from account create insert for $username")
                context.become(finishAccountLogin)
                self ! FinishAccountLogin(connection, username, newToken, false)
              }
            case default =>
              log.error(s"Error creating new account for $username - $default")
              context.become(finishAccountLogin)
              self ! FinishAccountLogin(connection, username, newToken, false)
          }
        case Failure(e : com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException) =>
          log.error(s"Error creating new account - ${e.errorMessage.message}")
          context.become(finishAccountLogin)
          self ! FinishAccountLogin(connection, username, newToken, false)

        case Failure(e : java.sql.SQLException) =>
          log.error(s"Error creating new account - ${e.getMessage}")
          context.become(finishAccountLogin)
          self ! FinishAccountLogin(connection, username, newToken, false)

        case _ =>
          failWithError(s"Something to do?")
      }
    case default =>
      failWithError(s"Invalid message '$default' received in createNewAccount")
  }

  // Essentially keeps a record of this individual login occurrence
  def logTheLoginOccurrence : Receive = {
    case LogTheLoginOccurrence(connection, username, newToken, isSuccessfulLogin, accountId) =>
      connection.get.inTransaction {
        c => c.sendPreparedStatement(
          "INSERT INTO logins (account_id, login_time, ip_address, canonical_hostName, hostname, port) VALUES(?,?,?,?,?,?)",
          Array(accountId, new java.sql.Timestamp(System.currentTimeMillis), ipAddress, canonicalHostName, hostName, port)
        )
      }.onComplete {
        _ =>
          context.become(finishAccountLogin)
          self ! FinishAccountLogin(connection, username, newToken, isSuccessfulLogin)
      }
    case default => failWithError(s"Invalid message '$default' received in logTheLoginOccurrence")
  }

  def finishAccountLogin : Receive = {
    case FinishAccountLogin(connection, username, newToken, isSuccessfulLogin, isInactive) =>
      if(isSuccessfulLogin) { // Login OK
        loginSuccessfulResponse(username, newToken)
        updateServerListTask = context.system.scheduler.schedule(0 seconds, 2 seconds, self, UpdateServerList())
      } else {
        if (!isInactive && connection.nonEmpty) { // Bad Password
          loginPwdFailureResponse(username, newToken)
        } else if (connection.nonEmpty) { // Account inactive
          loginAccountFailureResponse(username, newToken)
        } else {
          loginFailureResponse(username, newToken)
        }
      }
      if(connection.nonEmpty) {
        connection.get.disconnect
      }
      context.become(Started)
    case default =>
      failWithError(s"Invalid message '$default' received in finishAccountLogin")
  }

  def authenticateExistingAccount(
    connection: Connection, username: String, password: String, newToken: String, row: ArrayRowData
  ) : (Boolean, Int) = {
    val accountId : Int = row(0).asInstanceOf[Int]
    val dbPassHash : String = row(1).asInstanceOf[String]
    val inactive : Boolean = row(2).asInstanceOf[Boolean]
    val gm : Boolean = row(3).asInstanceOf[Boolean]

    if (password.isBcrypted(dbPassHash)) {
      if (!inactive) {
        log.info(s"Account password correct for $username!")
        accountIntermediary ! StoreAccountData(newToken, new Account(accountId, username, gm))
        return (true, accountId)
      } else {
        log.info(s"Account password correct for $username but account inactive !")
        return (false, accountId)
      }
    } else {
      log.info(s"Account password incorrect for $username")
    }

    (false, 0)
  }

  def loginSuccessfulResponse(username: String, newToken: String) = {
    sendResponse(PacketCoding.CreateGamePacket(0, LoginRespMessage(
      newToken, LoginError.Success, StationError.AccountActive,
      StationSubscriptionStatus.Active, 0, username, 10001
    )))
  }

  def loginPwdFailureResponse(username: String, newToken: String) = {
    log.info(s"Failed login to account $username")
    sendResponse(PacketCoding.CreateGamePacket(0, LoginRespMessage(
      newToken, LoginError.BadUsernameOrPassword, StationError.AccountActive,
      StationSubscriptionStatus.Active, 685276011, username, 10001
    )))
  }

  def loginFailureResponse(username: String, newToken: String) = {
    log.info("DB problem")
    sendResponse(PacketCoding.CreateGamePacket(0, LoginRespMessage(
      newToken, LoginError.unk1, StationError.AccountActive,
      StationSubscriptionStatus.Active, 685276011, username, 10001
    )))
  }

  def loginAccountFailureResponse(username: String, newToken: String) = {
    log.info(s"Account $username inactive")
    sendResponse(PacketCoding.CreateGamePacket(0, LoginRespMessage(
      newToken, LoginError.BadUsernameOrPassword, StationError.AccountClosed,
      StationSubscriptionStatus.Active, 685276011, username, 10001
    )))
  }

  def generateToken() = {
    val r = new scala.util.Random
    val sb = new StringBuilder
    for (_ <- 1 to 31) {
      sb.append(r.nextPrintableChar)
    }
    sb.toString
  }

  def updateServerList() = {
    val msg = VNLWorldStatusMessage("Welcome to PlanetSide! ",
      Vector(
        WorldInformation(
          serverName, WorldStatus.Up,
          WorldConfig.Get[ServerType.Value]("worldserver.ServerType"), Vector(WorldConnectionInfo(serverAddress)), PlanetSideEmpire.VS
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
