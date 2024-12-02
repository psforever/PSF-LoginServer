// Copyright (c) 2024 PSForever
package net.psforever.actors.session.spectator

import akka.actor.ActorContext
import net.psforever.actors.session.SessionActor
import net.psforever.actors.session.normal.NormalMode
import net.psforever.actors.session.support.{ChatFunctions, ChatOperations, SessionData}
import net.psforever.objects.Session
import net.psforever.packet.game.{ChatMsg, SetChatFilterMessage}
import net.psforever.services.chat.SpectatorChannel
import net.psforever.types.ChatMessageType
import net.psforever.types.ChatMessageType.{CMT_TOGGLESPECTATORMODE, CMT_TOGGLE_GM}
import net.psforever.zones.Zones

import scala.collection.Seq

object ChatLogic {
  def apply(ops: ChatOperations): ChatLogic = {
    new ChatLogic(ops, ops.context)
  }
}

class ChatLogic(val ops: ChatOperations, implicit val context: ActorContext) extends ChatFunctions {
  ops.transitoryCommandEntered = None

  def sessionLogic: SessionData = ops.sessionLogic

  def handleChatMsg(message: ChatMsg): Unit = {
    import ChatMessageType._
    (message.messageType, message.recipient.trim, message.contents.trim) match {
      /** Messages starting with ! are custom chat commands */
      case (_, _, contents) if contents.startsWith("!") &&
        customCommandMessages(message, session) => ()

      case (CMT_FLY, recipient, contents) =>
        ops.commandFly(contents, recipient)

      case (CMT_ANONYMOUS, _, _) => ()

      case (CMT_TOGGLE_GM, _, _) => ()
      sessionLogic.zoning.maintainInitialGmState = false

      case (CMT_CULLWATERMARK, _, contents) =>
        ops.commandWatermark(contents)

      case (CMT_SPEED, _, contents) =>
        ops.commandSpeed(message, contents)

      case (CMT_TOGGLESPECTATORMODE, _, contents) =>
        commandToggleSpectatorMode(contents)

      case (CMT_RECALL, _, _) =>
        commandToggleSpectatorMode(contents = "off")

      case (CMT_QUIT, _, _) =>
        ops.commandQuit(session)

      case (CMT_SUICIDE, _, _) =>
        commandToggleSpectatorMode(contents = "off")

      case (CMT_OPEN, _, _) =>
        ops.commandSendToRecipient(session, spectatorColoredMessage(message), SpectatorChannel)

      case (CMT_VOICE, _, contents) =>
        ops.commandVoice(session, message, contents, SpectatorChannel)

      case (CMT_TELL, _, _) =>
        ops.commandTellOrIgnore(session, spectatorColoredMessage(message), SpectatorChannel)

      case (CMT_BROADCAST, _, _) =>
        ops.commandSendToRecipient(session, spectatorColoredMessage(message), SpectatorChannel)

      case (CMT_PLATOON, _, _) =>
        ops.commandSendToRecipient(session, spectatorColoredMessage(message), SpectatorChannel)

      case (CMT_GMTELL, _, _) =>
        ops.commandSend(session, message, SpectatorChannel)

      case (CMT_NOTE, _, _) =>
        ops.commandSend(session, message, SpectatorChannel)

      case (CMT_WHO | CMT_WHO_CSR | CMT_WHO_CR | CMT_WHO_PLATOONLEADERS | CMT_WHO_SQUADLEADERS | CMT_WHO_TEAMS, _, _) =>
        ops.commandWho(session)

      case (CMT_ZONE, _, _) =>
        commandToggleSpectatorMode(contents = "off")
        ops.commandZone(message, Zones.sanctuaryZoneId(player.Faction))

      case (CMT_WARP, _, contents) =>
        ops.commandWarp(session, message, contents)

      case (CMT_REPORTUSER, _, contents) =>
        ops.commandReportUser(session, message, contents)

      case _ =>
        sendResponse(ChatMsg(ChatMessageType.UNK_227, "@no_permission"))
    }
  }

  def handleChatFilter(pkt: SetChatFilterMessage): Unit = {
    val SetChatFilterMessage(_, _, _) = pkt
  }

  def handleIncomingMessage(message: ChatMsg, fromSession: Session): Unit = {
    import ChatMessageType._
    message.messageType match {
      case CMT_BROADCAST | CMT_SQUAD | CMT_PLATOON | CMT_COMMAND | CMT_NOTE =>
        ops.commandIncomingSendAllIfOnline(session, message)

      case CMT_OPEN =>
        ops.commandIncomingSendToLocalIfOnline(session, fromSession, message)

      case CMT_TELL | U_CMT_TELLFROM |
           CMT_GMOPEN | CMT_GMBROADCAST | CMT_GMBROADCAST_NC | CMT_GMBROADCAST_TR | CMT_GMBROADCAST_VS |
           CMT_GMBROADCASTPOPUP | CMT_GMTELL | U_CMT_GMTELLFROM | UNK_45 | UNK_71 | UNK_227 | UNK_229 =>
        ops.commandIncomingSend(message)

      case _ => ()
    }
  }

  private def spectatorColoredMessage(message: ChatMsg): ChatMsg = {
    if (message.contents.nonEmpty) {
      val colorlessText = message.contents.replaceAll("//#\\d", "").trim
      val colorCodedText = s"/#5$colorlessText/#0"
      message.copy(recipient = s"<spectator:${message.recipient}>", contents = colorCodedText)
    } else {
      message
    }
  }

  private def customCommandMessages(
                                     message: ChatMsg,
                                     session: Session
                                   ): Boolean = {
    val contents = message.contents
    if (contents.startsWith("!")) {
      val (command, params) = ops.cliTokenization(contents.drop(1)) match {
        case a :: b => (a, b)
        case _ => ("", Seq(""))
      }
      command match {
        case "list" => ops.customCommandList(session, params, message)
        case "nearby" => ops.customCommandNearby(session)
        case "loc" => ops.customCommandLoc(session, message)
        case _ => false
      }
    } else {
      false
    }
  }

  private def commandToggleSpectatorMode(contents: String): Boolean = {
    ops.transitoryCommandEntered
      .collect {
        case CMT_TOGGLESPECTATORMODE => true
        case CMT_TOGGLE_GM => false
      }
      .getOrElse {
        contents.toLowerCase() match {
          case "off" | "of" =>
            ops.transitoryCommandEntered = Some(CMT_TOGGLESPECTATORMODE)
            context.self ! SessionActor.SetMode(NormalMode)
            true
          case _ =>
            false
        }
      }
  }
}
