package net.psforever.login.psadmin

import java.net.InetSocketAddress

import akka.actor.typed.receptionist.Receptionist
import akka.actor.{Actor, ActorRef, Props, Stash, typed}
import akka.io.Tcp
import akka.util.ByteString
import org.json4s._
import org.json4s.native.Serialization.write
import scodec.bits._
import scodec.interop.akka._
import net.psforever.services._
import scala.collection.mutable.Map
import akka.actor.typed.scaladsl.adapter._

object PsAdminActor {
  val whiteSpaceRegex = """\s+""".r
}

class PsAdminActor(peerAddress: InetSocketAddress, connection: ActorRef) extends Actor with Stash {
  private[this] val log = org.log4s.getLogger(self.path.name)

  var cluster: typed.ActorRef[InterstellarClusterService.Command] = null

  // val services          = Map[String, ActorRef]()
  // val servicesToResolve = Array("cluster")
  var buffer = ByteString()

  implicit val formats = DefaultFormats // for JSON serialization

  case class CommandCall(operation: String, args: Array[String])

  ServiceManager.receptionist ! Receptionist.Find(
    InterstellarClusterService.InterstellarClusterServiceKey,
    context.self
  )

  override def preStart() = {
    log.trace(s"PsAdmin connection started $peerAddress")
  }

  override def receive = ServiceLookup

  def ServiceLookup: Receive = {
    case InterstellarClusterService.InterstellarClusterServiceKey.Listing(listings) =>
      cluster = listings.head
      unstashAll()
      context.become(ReceiveCommand)

    /*
    case ServiceManager.LookupResult(service, endpoint) =>
      services { service } = endpoint

      if (services.size == servicesToResolve.size) {
        unstashAll()
        context.become(ReceiveCommand)
      }
     */

    case default => stash()
  }

  def ReceiveCommand: Receive = {
    case Tcp.Received(data) =>
      buffer ++= data

      var pos    = -1;
      var amount = 0
      do {
        pos = buffer.indexOf('\n')
        if (pos != -1) {
          val (cmd, rest) = buffer.splitAt(pos)
          buffer = rest.drop(1); // drop the newline

          // make sure the CN cant crash us
          val line = cmd.decodeString("utf-8").trim

          if (line != "") {
            val tokens = PsAdminActor.whiteSpaceRegex.split(line)
            val cmd    = tokens.head
            val args   = tokens.tail

            amount += 1
            self ! CommandCall(cmd, args)
          }
        }
      } while (pos != -1)

      if (amount > 0)
        context.become(ProcessCommands)

    case Tcp.PeerClosed =>
      context.stop(self)

    case Tcp.ErrorClosed(_) =>
      context.stop(self)

    case default =>
      log.error(s"Unexpected message $default")
  }

  /// Process all buffered commands and stash other ones
  def ProcessCommands: Receive = {
    case c: CommandCall =>
      stash()
      unstashAll()
      context.become(ProcessCommand)

    case default =>
      stash()
      unstashAll()
      context.become(ReceiveCommand)
  }

  /// Process a single command
  def ProcessCommand: Receive = {
    case CommandCall(cmd, args) =>
      val data = Map[String, Any]()

      if (cmd == "help" || cmd == "?") {
        if (args.size == 0) {
          var resp = "PsAdmin command usage\n"

          for ((command, info) <- PsAdminCommands.commands) {
            resp += s"${command} - ${info.usage}\n"
          }

          data { "message" } = resp
        } else {
          if (PsAdminCommands.commands.contains(args(0))) {
            val info = PsAdminCommands.commands { args(0) }

            data { "message" } = s"${args(0)} - ${info.usage}"
          } else {
            data { "message" } = s"Unknown command ${args(0)}"
            data { "error" } = true
          }
        }

        sendLine(write(data.toMap))
      } else if (PsAdminCommands.commands.contains(cmd)) {
        val cmd_template = PsAdminCommands.commands { cmd }

        cmd_template match {
          case PsAdminCommands.Command(usage, handler) =>
            context.actorOf(Props(handler, args, Map[String, ActorRef]()))

          case PsAdminCommands.CommandInternal(usage, handler) =>
            val resp = handler(args)

            resp match {
              case CommandGoodResponse(msg, data) =>
                data { "message" } = msg
                sendLine(write(data.toMap))

              case CommandErrorResponse(msg, data) =>
                data { "message" } = msg
                data { "error" } = true
                sendLine(write(data.toMap))
            }

            context.become(ProcessCommands)
        }
      } else {
        data { "message" } = "Unknown command"
        data { "error" } = true
        sendLine(write(data.toMap))
        context.become(ProcessCommands)
      }

    case resp: CommandResponse =>
      resp match {
        case CommandGoodResponse(msg, data) =>
          data { "message" } = msg
          sendLine(write(data.toMap))

        case CommandErrorResponse(msg, data) =>
          data { "message" } = msg
          data { "error" } = true
          sendLine(write(data.toMap))
      }

      context.become(ProcessCommands)
      context.stop(sender())
    case default =>
      stash()
      unstashAll()
      context.become(ProcessCommands)
  }

  def sendLine(line: String) = {
    ByteVector.encodeUtf8(line + "\n") match {
      case Left(e) =>
        log.error(s"Message encoding failure: $e")
      case Right(bv) =>
        connection ! Tcp.Write(bv.toByteString)
    }
  }
}
