// Copyright (c) 2017 PSForever
import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Cancellable, MDCContextAware}
import net.psforever.packet.{PlanetSideGamePacket, _}
import net.psforever.packet.control._
import net.psforever.packet.game._
import org.log4s.MDC
import scodec.bits._
import MDCContextAware.Implicits._
import com.github.mauricio.async.db.{Connection, QueryResult, RowData}
import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import net.psforever.objects.Account
import net.psforever.objects.DefaultCancellable
import net.psforever.types.PlanetSideEmpire
import services.ServiceManager
import services.ServiceManager.Lookup
import services.account.StoreAccountData

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

  // TODO: Comment this properly
  // A successful account lookup results in a new token to be sent to client
  // If the lookup fails, a token is not returned
  // Account is stored in ServiceManager held actor.....add description
  def accountLookup(username : String, password : String) : Option[String] = {
    val connection: Connection = DatabaseConnector.getAccountsConnection
    Await.result(connection.connect, 5 seconds) // TODO remove awaits

    val future: Future[QueryResult] = connection.sendPreparedStatement(
      "SELECT id, pass FROM accounts where username=?", Array(username))

    val mapResult: Future[Any] = future.map(queryResult => queryResult.rows match {
      case Some(resultSet) =>
        if(resultSet.nonEmpty) {
          val row : RowData = resultSet.head
          row
        } else {
          -2 // Account does not exist
        }
      case None =>
        -1
    })

    // TODO https://github.com/t3hnar/scala-bcrypt
    val newToken = Some("HHHGGGGFFFFEEEEDDDDCCCCBBBBAAAA")

    try {
      import com.github.mauricio.async.db.general.ArrayRowData
      val userData = Await.result(mapResult, 5 seconds) // TODO remove awaits

      userData match {
        case row : ArrayRowData =>
          val accountId : Int = row(0).asInstanceOf[Int]
          val dbPass : String = row(1).asInstanceOf[String] // TODO https://github.com/t3hnar/scala-bcrypt
          if (dbPass == password) {
            log.info(s"Account password correct for $username!")
            accountIntermediary ! StoreAccountData(newToken.get, new Account(accountId, username))
            return newToken
          } else {
            log.info(s"Account password incorrect for $username")
          }
        case errorCode : Int => errorCode match {
          case -2 =>
            log.info(s"Account $username does not exist, creating new account...")

            // TODO https://github.com/t3hnar/scala-bcrypt
            val createNewAccountTransaction : Future[QueryResult] = connection.inTransaction {
              c => c.sendPreparedStatement("INSERT INTO accounts (username, pass) VALUES(?,?)", Array(username, password))
            }
            val insertResult = Await.result(createNewAccountTransaction, 5 seconds) // TODO remove awaits

            insertResult match {
              case result : QueryResult =>
                if(result.rowsAffected == 1 && result.rows.nonEmpty) {
                  val accountId = result.rows.head(0).asInstanceOf[Int]
                  accountIntermediary ! StoreAccountData(newToken.get, new Account(accountId, username))
                  log.info(s"Successfully created new account for $username")
                  return newToken
                } else {
                  log.error(s"No result from account create insert for $username")
                }

              case _ =>
                log.error(s"Error creating new account for $username")
            }
          case _ =>
            log.error(s"Issue retrieving result set from database for account login")
        }
      }
    } catch {
      case e : MySQLException =>
        log.error(s"SQL exception $e")
      case e: Exception =>
        log.error(s"Unknown exception when executing SQL statement: $e")
    } finally {
      connection.disconnect
    }
    None
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
        val newToken = accountLookup(username, password.get)

        if(newToken.nonEmpty) {
          val response = LoginRespMessage(newToken.get, LoginError.Success, StationError.AccountActive,
            StationSubscriptionStatus.Active, 0, username, 10001)

          sendResponse(PacketCoding.CreateGamePacket(0, response))
          updateServerListTask = context.system.scheduler.schedule(0 seconds, 2 seconds, self, UpdateServerList())
        } else {
          val newToken = token.getOrElse("AAAABBBBCCCCDDDDEEEEFFFFGGGGHHH")
          val response = LoginRespMessage(newToken, LoginError.BadUsernameOrPassword, StationError.AccountActive,
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
