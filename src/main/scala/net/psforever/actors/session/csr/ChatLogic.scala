// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import akka.actor.ActorContext
import net.psforever.actors.session.support.{ChatFunctions, ChatOperations, SessionData}
import net.psforever.objects.Session
import net.psforever.packet.game.{ChatMsg, SetChatFilterMessage}
import net.psforever.services.chat.DefaultChannel
import net.psforever.types.ChatMessageType

object ChatLogic {
  def apply(ops: ChatOperations): ChatLogic = {
    new ChatLogic(ops, ops.context)
  }
}

class ChatLogic(val ops: ChatOperations, implicit val context: ActorContext) extends ChatFunctions {
  def sessionLogic: SessionData = ops.sessionLogic

  ops.SpectatorMode = SpectateAsCustomerServiceRepresentativeMode

  def handleChatMsg(message: ChatMsg): Unit = {
    import net.psforever.types.ChatMessageType._
    val isAlive = if (player != null) player.isAlive else false
    (message.messageType, message.recipient.trim, message.contents.trim) match {
      /** Messages starting with ! are custom chat commands */
      case (_, _, contents) if contents.startsWith("!") &&
        customCommandMessages(message, session) => ()

      case (CMT_FLY, recipient, contents) =>
        ops.commandFly(contents, recipient)

      case (CMT_ANONYMOUS, _, _) =>
      // ?

      case (CMT_TOGGLE_GM, _, contents)=>
        customCommandModerator(contents)

      case (CMT_CULLWATERMARK, _, contents) =>
        ops.commandWatermark(contents)

      case (CMT_SPEED, _, contents) =>
        ops.commandSpeed(message, contents)

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

      case (CMT_SETBASERESOURCES, _, contents) =>
        ops.commandSetBaseResources(session, contents)

      case (CMT_ZONELOCK, _, contents) =>
        ops.commandZoneLock(contents)

      case (U_CMT_ZONEROTATE, _, _) =>
        ops.commandZoneRotate()

      case (CMT_CAPTUREBASE, _, contents) =>
        ops.commandCaptureBase(session, message, contents)

      case (CMT_GMBROADCAST | CMT_GMBROADCAST_NC | CMT_GMBROADCAST_VS | CMT_GMBROADCAST_TR, _, _) =>
        ops.commandSendToRecipient(session, message, DefaultChannel)

      case (CMT_GMTELL, _, _) =>
        ops.commandSend(session, message, DefaultChannel)

      case (CMT_GMBROADCASTPOPUP, _, _) =>
        ops.commandSendToRecipient(session, message, DefaultChannel)

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

      case (CMT_COMMAND, _, _) =>
        ops.commandSendToRecipient(session, message, DefaultChannel)

      case (CMT_NOTE, _, _) =>
        ops.commandSend(session, message, DefaultChannel)

      case (CMT_SILENCE, _, _) =>
        ops.commandSend(session, message, DefaultChannel)

      case (CMT_SQUAD, _, _) =>
        ops.commandSquad(session, message, DefaultChannel) //todo SquadChannel, but what is the guid

      case (CMT_WHO | CMT_WHO_CSR | CMT_WHO_CR | CMT_WHO_PLATOONLEADERS | CMT_WHO_SQUADLEADERS | CMT_WHO_TEAMS, _, _) =>
        ops.commandWho(session)

      case (CMT_ZONE, _, contents) =>
        ops.commandZone(message, contents)

      case (CMT_WARP, _, contents) =>
        ops.commandWarp(session, message, contents)

      case (CMT_SETBATTLERANK, _, contents) =>
        ops.commandSetBattleRank(session, message, contents)

      case (CMT_SETCOMMANDRANK, _, contents) =>
        ops.commandSetCommandRank(session, message, contents)

      case (CMT_ADDBATTLEEXPERIENCE, _, contents) =>
        ops.commandAddBattleExperience(message, contents)

      case (CMT_ADDCOMMANDEXPERIENCE, _, contents) =>
        ops.commandAddCommandExperience(message, contents)

      case (CMT_TOGGLE_HAT, _, contents) =>
        ops.commandToggleHat(session, message, contents)

      case (CMT_HIDE_HELMET | CMT_TOGGLE_SHADES | CMT_TOGGLE_EARPIECE, _, contents) =>
        ops.commandToggleCosmetics(session, message, contents)

      case (CMT_ADDCERTIFICATION, _, contents) =>
        ops.commandAddCertification(session, message, contents)

      case (CMT_KICK, _, contents) =>
        ops.commandKick(session, message, contents)

      case _ =>
        log.warn(s"Unhandled chat message $message")
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
        case "whitetext" => ops.customCommandWhitetext(session, params)
        case "list" => ops.customCommandList(session, params, message)
        case "ntu" => ops.customCommandNtu(session, params)
        case "zonerotate" => ops.customCommandZonerotate(params)
        case "nearby" => ops.customCommandNearby(session)
        case "csr" | "gm" | "op" => customCommandModerator(params.headOption.getOrElse(""))
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
          false
      }
    } else {
      false
    }
  }

  def commandToggleSpectatorMode(contents: String): Unit = {
//    val currentSpectatorActivation = (if (avatar != null) avatar.permissions else ModePermissions()).canSpectate
//    contents.toLowerCase() match {
//      case "on" | "o" | "" if !currentSpectatorActivation =>
//        context.self ! SessionActor.SetMode(SessionSpectatorMode)
//      case "off" | "of" if currentSpectatorActivation =>
//        context.self ! SessionActor.SetMode(SessionCustomerServiceRepresentativeMode)
//      case _ => ()
//    }
  }

  def customCommandModerator(contents : String): Boolean = {
    if (player.spectator) {
      sendResponse(ChatMsg(ChatMessageType.UNK_225, "CSR SPECTATOR MODE"))
      sendResponse(ChatMsg(ChatMessageType.UNK_227, "Disable spectator mode first."))
    } else {
      ops.customCommandModerator(contents)
    }
    true
  }
}
