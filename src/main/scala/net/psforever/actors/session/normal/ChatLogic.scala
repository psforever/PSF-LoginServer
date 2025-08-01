// Copyright (c) 2024 PSForever
package net.psforever.actors.session.normal

import akka.actor.ActorContext
import net.psforever.actors.session.SessionActor
import net.psforever.actors.session.spectator.SpectatorMode
import net.psforever.actors.session.support.{ChatFunctions, ChatOperations, SessionData}
import net.psforever.objects.Session
import net.psforever.packet.game.{ChatMsg, ServerType, SetChatFilterMessage}
import net.psforever.services.chat.DefaultChannel
import net.psforever.types.ChatMessageType
import net.psforever.types.ChatMessageType.{CMT_TOGGLESPECTATORMODE, CMT_TOGGLE_GM}
import net.psforever.util.Config

object ChatLogic {
  def apply(ops: ChatOperations): ChatLogic = {
    new ChatLogic(ops, ops.context)
  }
}

class ChatLogic(val ops: ChatOperations, implicit val context: ActorContext) extends ChatFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  ops.CurrentSpectatorMode = SpectatorMode
  ops.transitoryCommandEntered = None

  def handleChatMsg(message: ChatMsg): Unit = {
    import net.psforever.types.ChatMessageType._
    lazy val isAlive = avatar != null && player != null && player.isAlive
    (message.messageType, message.recipient.trim, message.contents.trim) match {
      /** Messages starting with ! are custom chat commands */
      case (_, _, contents) if contents.startsWith("!") &&
        customCommandMessages(message, session) => ()

      case (CMT_ANONYMOUS, _, _) => ()

      case (CMT_TOGGLE_GM, _, contents) if isAlive =>
        customCommandModerator(contents)

      case (CMT_CULLWATERMARK, _, contents) =>
        ops.commandWatermark(contents)

      case (CMT_TOGGLESPECTATORMODE, _, contents) if isAlive =>
        commandToggleSpectatorMode(contents)

      case (CMT_RECALL, _, _) =>
        ops.commandRecall(session)

      case (CMT_INSTANTACTION, _, _) =>
        ops.commandInstantAction(session)

      case (CMT_QUIT, _, _) =>
        ops.commandQuit(session)

      case (CMT_SUICIDE, _, _) =>
        ops.commandSuicide(session)

      case (CMT_DESTROY, _, contents) if contents.matches("\\d+") =>
        ops.commandDestroy(session, message, contents)

      case (CMT_OPEN, _, _) if !player.silenced =>
        ops.commandSendToRecipient(session, message, DefaultChannel)

      case (CMT_VOICE, _, contents) =>
        ops.commandVoice(session, message, contents, DefaultChannel)

      case (CMT_TELL, _, _) if !player.silenced =>
        ops.commandTellOrIgnore(session, message, DefaultChannel)

      case (CMT_BROADCAST, _, _) if !player.silenced =>
        ops.commandSendToRecipient(session, message, DefaultChannel)

      case (CMT_PLATOON, _, _) if !player.silenced =>
        ops.commandSendToRecipient(session, message, DefaultChannel)

      case (CMT_NOTE, _, _) =>
        ops.commandSend(session, message, DefaultChannel)

      case (CMT_SQUAD, _, _) =>
        ops.commandSquad(session, message, DefaultChannel) //todo SquadChannel, but what is the guid

      case (CMT_WHO | CMT_WHO_CSR | CMT_WHO_CR | CMT_WHO_PLATOONLEADERS | CMT_WHO_SQUADLEADERS | CMT_WHO_TEAMS, _, _) =>
        ops.commandWho(session)

      case (CMT_TOGGLE_HAT, _, contents) =>
        ops.commandToggleHat(session, message, contents)

      case (CMT_HIDE_HELMET | CMT_TOGGLE_SHADES | CMT_TOGGLE_EARPIECE, _, contents) =>
        ops.commandToggleCosmetics(session, message, contents)

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

      case CMT_VOICE =>
        ops.commandIncomingVoice(session, fromSession, message)

      case CMT_SILENCE =>
        ops.commandIncomingSilence(session, message)

      case _ =>
        log.warn(s"Unexpected messageType $message")
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
        case "loc" => ops.customCommandLoc(session, message)
        case "suicide" => ops.customCommandSuicide(session)
        case "grenade" => ops.customCommandGrenade(session, log)
        case "macro" => ops.customCommandMacro(session, params)
        case "progress" => ops.customCommandProgress(session, params)
        case "squad" => ops.customCommandSquad(params)
        case _ =>
          // command was not handled
          sendResponse(
            ChatMsg(
              ChatMessageType.CMT_GMOPEN, // CMT_GMTELL
              message.wideContents,
              "Server",
              s"Unknown command !$command",
              message.note
            )
          )
          true
      }
    } else {
      false
    }
  }

  def commandToggleSpectatorMode(contents: String): Boolean = {
    ops.transitoryCommandEntered
      .collect {
        case CMT_TOGGLESPECTATORMODE => true
        case CMT_TOGGLE_GM => false
      }
      .getOrElse {
        val currentSpectatorActivation =
          avatar.permissions.canSpectate ||
            avatar.permissions.canGM ||
            Config.app.world.serverType == ServerType.Development
        contents.toLowerCase() match {
          case "on" | "o" | "" if currentSpectatorActivation && !player.spectator =>
            ops.transitoryCommandEntered = Some(CMT_TOGGLESPECTATORMODE)
            context.self ! SessionActor.SetMode(ops.CurrentSpectatorMode)
            true
          case _ =>
            false
        }
      }
  }

  def customCommandModerator(contents: String): Boolean = {
    if (sessionLogic.zoning.maintainInitialGmState) {
      sessionLogic.zoning.maintainInitialGmState = false
      true
    } else {
      ops.transitoryCommandEntered
        .collect {
          case CMT_TOGGLE_GM => true
          case CMT_TOGGLESPECTATORMODE => false
        }
        .getOrElse {
          val currentCsrActivation =
            avatar.permissions.canGM ||
              Config.app.world.serverType == ServerType.Development
          contents.toLowerCase() match {
            case "on" | "o" | "" if currentCsrActivation =>
              import net.psforever.actors.session.csr.CustomerServiceRepresentativeMode
              ops.transitoryCommandEntered = Some(CMT_TOGGLE_GM)
              context.self ! SessionActor.SetMode(CustomerServiceRepresentativeMode)
              true
            case _ =>
              false
          }
        }
    }
  }
}
