// Copyright (c) 2024 PSForever
package net.psforever.actors.session.csr

import akka.actor.ActorContext
import net.psforever.actors.session.SessionActor
import net.psforever.actors.session.normal.NormalMode
import net.psforever.actors.session.support.{ChatFunctions, ChatOperations, SessionData}
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Session, TurretDeployable}
import net.psforever.objects.ce.{Deployable, DeployableCategory}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.{CommonMessages, PlanetSideServerObject}
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.{ChatMsg, SetChatFilterMessage}
import net.psforever.services.Service
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.chat.{ChatChannel, DefaultChannel, SpectatorChannel}
import net.psforever.types.ChatMessageType.{CMT_TOGGLESPECTATORMODE, CMT_TOGGLE_GM}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire}

import scala.util.Success

object ChatLogic {
  def apply(ops: ChatOperations): ChatLogic = {
    new ChatLogic(ops, ops.context)
  }
}

class ChatLogic(val ops: ChatOperations, implicit val context: ActorContext) extends ChatFunctions {
  ops.transitoryCommandEntered match {
    case Some(CMT_TOGGLESPECTATORMODE) =>
      //we are transitioning down from csr spectator mode to normal mode, continue to block transitory messages
      ()
    case _ =>
      //correct player mode
      ops.transitoryCommandEntered = None
  }

  def sessionLogic: SessionData = ops.sessionLogic

  ops.CurrentSpectatorMode = SpectateAsCustomerServiceRepresentativeMode

  private var comms: ChatChannel = DefaultChannel
  private var seeSpectatorsIn: Option[Zone] = None

  def handleChatMsg(message: ChatMsg): Unit = {
    import net.psforever.types.ChatMessageType._
    val isAlive = if (player != null) player.isAlive else false
    (message.messageType, message.recipient.trim, message.contents.trim) match {
      /** Messages starting with ! are custom chat commands */
      case (_, _, contents) if contents.startsWith("!") &&
        customCommandMessages(message, session) => ()

      case (CMT_FLY, recipient, contents) =>
        ops.commandFly(contents, recipient)

      case (CMT_ANONYMOUS, _, _) => ()

      case (CMT_TOGGLE_GM, _, contents) =>
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
        ops.commandSendToRecipient(session, message, comms)

      case (CMT_GMTELL, _, _) =>
        ops.commandSend(session, message, comms)

      case (CMT_GMBROADCASTPOPUP, _, _) =>
        ops.commandSendToRecipient(session, message, comms)

      case (CMT_OPEN, _, _) if !player.silenced =>
        ops.commandSendToRecipient(session, message, comms)

      case (CMT_VOICE, _, contents) =>
        ops.commandVoice(session, message, contents, comms)

      case (CMT_TELL, _, _) if !player.silenced =>
        ops.commandTellOrIgnore(session, message, comms)

      case (CMT_BROADCAST, _, _) if !player.silenced =>
        ops.commandSendToRecipient(session, message, comms)

      case (CMT_PLATOON, _, _) if !player.silenced =>
        ops.commandSendToRecipient(session, message, comms)

      case (CMT_COMMAND, _, _) =>
        ops.commandSendToRecipient(session, message, comms)

      case (CMT_NOTE, _, _) =>
        ops.commandSend(session, message, comms)

      case (CMT_SILENCE, _, _) =>
        ops.commandSend(session, message, comms)

      case (CMT_SQUAD, _, _) =>
        ops.commandSquad(session, message, comms) //todo SquadChannel, but what is the guid

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
        case "togglespectators" => customCommandToggleSpectators(params)
        case "showspectators" => customCommandShowSpectators()
        case "hidespectators" => customCommandHideSpectators()
        case "sayspectator" => customCommandSpeakAsSpectator(params, message)
        case "setempire" => customCommandSetEmpire(params)
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

  private def commandToggleSpectatorMode(contents: String): Unit = {
    contents.toLowerCase() match {
      case "on" | "o" | "" if !player.spectator =>
        context.self ! SessionActor.SetMode(SpectateAsCustomerServiceRepresentativeMode)
      case "off" | "of" if player.spectator =>
        context.self ! SessionActor.SetMode(CustomerServiceRepresentativeMode)
      case _ => ()
    }
  }

  private def customCommandModerator(contents: String): Boolean = {
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
          contents.toLowerCase() match {
            case "off" | "of" if player.spectator =>
              ops.transitoryCommandEntered = Some(CMT_TOGGLESPECTATORMODE)
              context.self ! SessionActor.SetMode(CustomerServiceRepresentativeMode)
              context.self ! SessionActor.SetMode(NormalMode)
              true
            case "off" | "of" =>
              ops.transitoryCommandEntered = Some(CMT_TOGGLE_GM)
              context.self ! SessionActor.SetMode(NormalMode)
              true
            case _ =>
              false
          }
        }
    }
  }

  private def customCommandToggleSpectators(contents: Seq[String]): Boolean = {
    contents
      .headOption
      .map(_.toLowerCase())
      .collect {
        case "on" | "o" | "" if !seeSpectatorsIn.contains(continent) =>
          customCommandShowSpectators()
        case "off" | "of" if seeSpectatorsIn.contains(continent) =>
          customCommandHideSpectators()
        case _ => ()
      }
    true
  }

  private def customCommandShowSpectators(): Boolean = {
    val channel = player.Name
    val events = continent.AvatarEvents
    seeSpectatorsIn = Some(continent)
    events ! Service.Join(s"spectator")
    continent
      .AllPlayers
      .filter(_.spectator)
      .foreach { spectator =>
        val guid = spectator.GUID
        val definition = spectator.Definition
        events ! AvatarServiceMessage(
          channel,
          AvatarAction.LoadPlayer(guid, definition.ObjectId, guid, definition.Packet.ConstructorData(spectator).get, None)
        )
      }
    true
  }

  private def customCommandHideSpectators(): Boolean = {
    val channel = player.Name
    val events = continent.AvatarEvents
    seeSpectatorsIn = None
    events ! Service.Leave(Some("spectator"))
    continent
      .AllPlayers
      .filter(_.spectator)
      .foreach { spectator =>
        val guid = spectator.GUID
        events ! AvatarServiceMessage(
          channel,
          AvatarAction.ObjectDelete(guid, guid)
        )
      }
    true
  }

  private def customCommandSpeakAsSpectator(params: Seq[String], message: ChatMsg): Boolean = {
    comms = SpectatorChannel
    handleChatMsg(message.copy(contents = params.mkString(" ")))
    comms = DefaultChannel
    true
  }

  private def customCommandSetEmpire(params: Seq[String]): Boolean = {
    var postUsage: Boolean = false
    val (entityOpt, foundFaction) = (params.headOption, params.lift(1)) match {
      case (Some(guid), Some(faction)) if guid.toIntOption.nonEmpty =>
        try {
          (continent.GUID(guid.toInt), PlanetSideEmpire.apply(faction))
        } catch {
          case _: Exception =>
            (None, PlanetSideEmpire.NEUTRAL)
        }
      case (Some(guid), None) if guid.toIntOption.nonEmpty =>
        (continent.GUID(guid.toInt), player.Faction)
      case _ =>
        postUsage = true
        (None, PlanetSideEmpire.NEUTRAL)
    }
    entityOpt
      .collect {
        case f: FactionAffinity if f.Faction != foundFaction && foundFaction != PlanetSideEmpire.NEUTRAL => f
      }
      .collect {
        case o: TurretDeployable
          if o.Definition.DeployCategory == DeployableCategory.FieldTurrets =>
          //remove prior turret and construct new one
          import scala.concurrent.ExecutionContext.Implicits.global
          import scala.concurrent.duration._
          o.Actor ! Deployable.Deconstruct(Some(2.seconds))
          sessionLogic.general.handleDeployObject(
            continent,
            GlobalDefinitions.PortableMannedTurret(foundFaction).Item,
            o.Position,
            o.Orientation,
            o.WhichSide,
            foundFaction
          ).onComplete {
            case Success(obj2) => sendResponse(ChatMsg(ChatMessageType.UNK_227, s"${obj2.GUID.guid}"))
            case _ => ()
          }
          true
        case o: Deployable =>
          o.Faction = foundFaction
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.SetEmpire(Service.defaultPlayerGUID, o.GUID, foundFaction)
          )
          true
        case o: Building =>
          ops.commandCaptureBaseProcessResults(Some(Seq(o)), Some(foundFaction), Some(1))
          true
        case o: PlanetSideServerObject with Hackable =>
          o.Actor ! CommonMessages.Hack(player, o)
          true
        case o: PlanetSideGameObject with FactionAffinity =>
          o.Faction = foundFaction
          continent.AvatarEvents ! AvatarServiceMessage(
            continent.id,
            AvatarAction.SetEmpire(Service.defaultPlayerGUID, o.GUID, foundFaction)
          )
          true
      }
      .getOrElse {
        if (postUsage) {
          sendResponse(ChatMsg(ChatMessageType.UNK_227, "!setempire guid [faction]"))
        } else if (entityOpt.nonEmpty) {
          sendResponse(ChatMsg(ChatMessageType.UNK_227, "set empire entity not supported"))
        } else {
          sendResponse(ChatMsg(ChatMessageType.UNK_227, "set empire entity not found"))
        }
        true
      }
  }

  override def stop(): Unit = {
    super.stop()
    seeSpectatorsIn.foreach(_ => customCommandHideSpectators())
    seeSpectatorsIn = None
  }
}
