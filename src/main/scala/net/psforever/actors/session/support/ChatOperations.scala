// Copyright (c) 2024 PSForever
package net.psforever.actors.session.support

import akka.actor.Cancellable
import akka.actor.{ActorRef => ClassicActorRef}
import akka.actor.typed.ActorRef
import akka.actor.{ActorContext, typed}
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.actors.session.spectator.SpectatorMode
import net.psforever.actors.session.{AvatarActor, SessionActor}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.LivePlayerList
import net.psforever.objects.sourcing.PlayerSource
import net.psforever.objects.zones.{Zone, ZoneInfo}
import net.psforever.packet.game.SetChatFilterMessage
import net.psforever.services.chat.{DefaultChannel, OutfitChannel, SquadChannel}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.teamwork.{SquadResponse, SquadService, SquadServiceResponse}
import net.psforever.types.ChatMessageType.CMT_QUIT
import org.log4s.Logger

import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import scala.annotation.unused
import scala.collection.{Seq, mutable}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Success
//
import net.psforever.actors.zone.BuildingActor
import net.psforever.login.WorldSession
import net.psforever.objects.{Default, Player, Session}
import net.psforever.objects.avatar.{BattleRank, Certification, CommandRank, Shortcut => AvatarShortcut}
import net.psforever.objects.definition.ImplantDefinition
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Amenity, Building}
import net.psforever.objects.serverobject.turret.{FacilityTurret, TurretUpgrade, WeaponTurrets}
import net.psforever.objects.zones.Zoning
import net.psforever.packet.game.objectcreate.DrawnSlot
import net.psforever.packet.game.{ChatMsg, CreateShortcutMessage, DeadState, RequestDestroyMessage, Shortcut}
import net.psforever.services.{CavernRotationService, InterstellarClusterService}
import net.psforever.services.chat.ChatService
import net.psforever.services.chat.ChatChannel
import net.psforever.types.ChatMessageType.{CMT_GMOPEN, UNK_227, UNK_229}
import net.psforever.types.{ChatMessageType, Cosmetic, ExperienceType, ImplantType, PlanetSideEmpire, PlanetSideGUID, Vector3}
import net.psforever.util.{Config, PointOfInterest}
import net.psforever.zones.Zones

trait ChatFunctions extends CommonSessionInterfacingFunctionality {
  def ops: ChatOperations

  def handleChatMsg(message: ChatMsg): Unit

  def handleChatFilter(pkt: SetChatFilterMessage): Unit

  def handleIncomingMessage(message: ChatMsg, fromSession: Session): Unit
}

class ChatOperations(
                      val sessionLogic: SessionData,
                      val avatarActor: typed.ActorRef[AvatarActor.Command],
                      val chatService: typed.ActorRef[ChatService.Command],
                      val squadService: ClassicActorRef,
                      val cluster: typed.ActorRef[InterstellarClusterService.Command],
                      implicit val context: ActorContext
                    ) extends CommonSessionInterfacingFunctionality {
  private var channels: List[ChatChannel] = List()
  private var silenceTimer: Cancellable = Default.Cancellable
  private[session] var transitoryCommandEntered: Option[ChatMessageType] = None
  private val scheduler = Executors.newScheduledThreadPool(2)
  /**
   * when another player is listed as one of our ignored players,
   * and that other player sends an emote,
   * that player is assigned a cooldown and only one emote per period will be seen<br>
   * key - character unique avatar identifier, value - when the current cooldown period will end
   */
  private val ignoredEmoteCooldown: mutable.LongMap[Long] = mutable.LongMap[Long]()

  private[session] var CurrentSpectatorMode: PlayerMode = SpectatorMode

  import akka.actor.typed.scaladsl.adapter._
  private val chatServiceAdapter: ActorRef[ChatService.MessageResponse] = context.self.toTyped[ChatService.MessageResponse]

  private implicit lazy val timeout: Timeout = Timeout(2.seconds)

  private var invitationList: Array[String] = Array()

  def JoinChannel(channel: ChatChannel): Unit = {
    chatService ! ChatService.JoinChannel(chatServiceAdapter, sessionLogic, channel)
    channels ++= List(channel)
  }

  def LeaveChannel(channel: ChatChannel): Unit = {
    chatService ! ChatService.LeaveChannel(chatServiceAdapter, channel)
    channels = channels.filterNot(_ == channel)
  }

  def commandFly(contents: String, recipient: String): Unit = {
    val (token, flying) = contents match {
      case "on"  => (contents, true)
      case "off" => (contents, false)
      case _     => ("off", false)
    }
    context.self ! SessionActor.SetFlying(flying)
    sendResponse(ChatMsg(ChatMessageType.CMT_FLY, wideContents=false, recipient, token, None))
  }

  def commandWatermark(contents: String): Unit = {
    val connectionState = {
      if (contents.contains("40 80")) 100
      else if (contents.contains("120 200")) 25
      else 50
    }
    context.self ! SessionActor.SetConnectionState(connectionState)
    context.self ! SessionActor.SendResponse(ChatMsg(ChatMessageType.UNK_227, "@CMT_CULLWATERMARK_success"))
  }

  def commandSpeed(message: ChatMsg, contents: String): Unit = {
    val speed =
      try {
        contents.toFloat
      } catch {
        case _: Throwable =>
          1f
      }
    context.self ! SessionActor.SetSpeed(speed)
    sendResponse(message.copy(contents = f"$speed%.3f"))
  }

  def commandRecall(session: Session): Unit = {
    val player = session.player
    val errorMessage = session.zoningType match {
      case Zoning.Method.Quit =>
        Some("You can't recall to your sanctuary continent while quitting")
      case Zoning.Method.InstantAction =>
        Some("You can't recall to your sanctuary continent while instant actioning")
      case Zoning.Method.Recall =>
        Some("You already requested to recall to your sanctuary continent")
      case _ if session.zone.id == Zones.sanctuaryZoneId(player.Faction) =>
        Some("You can't recall to your sanctuary when you are already in your sanctuary")
      case _ if !player.isAlive || session.deadState != DeadState.Alive =>
        Some(if (player.isAlive) "@norecall_deconstructing" else "@norecall_dead")
      case _ if player.VehicleSeated.nonEmpty =>
        Some("@norecall_invehicle")
      case _ =>
        None
    }
    errorMessage match {
      case Some(errorMessage) =>
        sendResponse(ChatMsg(CMT_QUIT, errorMessage))
      case None =>
        context.self ! SessionActor.Recall()
    }
  }

  def commandInstantAction(session: Session): Unit = {
    val player = session.player
    if (session.zoningType == Zoning.Method.Quit) {
      sendResponse(ChatMsg(CMT_QUIT, "You can't instant action while quitting."))
    } else if (session.zoningType == Zoning.Method.InstantAction) {
      sendResponse(ChatMsg(CMT_QUIT, "@noinstantaction_instantactionting"))
    } else if (session.zoningType == Zoning.Method.Recall) {
      sendResponse(
        ChatMsg(CMT_QUIT, "You won't instant action. You already requested to recall to your sanctuary continent")
      )
    } else if (!player.isAlive || session.deadState != DeadState.Alive) {
      if (player.isAlive) {
        sendResponse(ChatMsg(CMT_QUIT, "@noinstantaction_deconstructing"))
      } else {
        sendResponse(ChatMsg(CMT_QUIT, "@noinstantaction_dead"))
      }
    } else if (player.VehicleSeated.nonEmpty) {
      sendResponse(ChatMsg(CMT_QUIT, "@noinstantaction_invehicle"))
    } else {
      context.self ! SessionActor.InstantAction()
    }
  }

  def commandQuit(session: Session): Unit = {
    val player = session.player
    if (session.zoningType == Zoning.Method.Quit) {
      sendResponse(ChatMsg(CMT_QUIT, "@noquit_quitting"))
    } else if (!player.isAlive || session.deadState != DeadState.Alive) {
      if (player.isAlive) {
        sendResponse(ChatMsg(CMT_QUIT, "@noquit_deconstructing"))
      } else {
        sendResponse(ChatMsg(CMT_QUIT, "@noquit_dead"))
      }
    } else if (player.VehicleSeated.nonEmpty) {
      sendResponse(ChatMsg(CMT_QUIT, "@noquit_invehicle"))
    } else {
      context.self ! SessionActor.Quit()
    }
  }

  def commandSuicide(session: Session): Unit = {
    if (session.player.isAlive && session.deadState != DeadState.Release) {
      context.self ! SessionActor.Suicide()
    }
  }

  def commandDestroy(session: Session, message: ChatMsg, contents: String): Unit = {
    val guid = contents.toInt
    session.zone.GUID(session.zone.map.terminalToSpawnPad.getOrElse(guid, guid)) match {
      case Some(pad: VehicleSpawnPad) =>
        pad.Actor ! VehicleSpawnControl.ProcessControl.Flush
      case Some(turret: FacilityTurret) if turret.isUpgrading =>
        WeaponTurrets.FinishUpgradingMannedTurret(turret, TurretUpgrade.None)
      case _ =>
        // FIXME we shouldn't do it like that
        context.self ! RequestDestroyMessage(PlanetSideGUID(guid))
    }
    sendResponse(message)
  }

  def commandSetBaseResources(session: Session, contents: String): Unit = {
    val buffer = cliTokenization(contents)
    val customNtuValue = buffer.lift(1) match {
      case Some(x) if x.toIntOption.nonEmpty => Some(x.toInt)
      case _                                 => None
    }
    val silos = {
      val position = session.player.Position
      session.zone.Buildings.values
        .filter { building =>
          val soi2 = building.Definition.SOIRadius * building.Definition.SOIRadius
          Vector3.DistanceSquared(building.Position, position) < soi2
        }
    }
      .flatMap { building => building.Amenities.filter { _.isInstanceOf[ResourceSilo] } }
    setBaseResources(customNtuValue, silos, debugContent="")
  }

  def commandZoneLock(contents: String): Unit = {
    val buffer = cliTokenization(contents)
    val (zoneOpt, lockVal) = (buffer.lift(1), buffer.lift(2)) match {
      case (Some(x), Some(y)) =>
        val zone = if (x.toIntOption.nonEmpty) {
          val xInt = x.toInt
          Zones.zones.find(_.Number == xInt)
        } else {
          Zones.zones.find(z => z.id.equals(x))
        }
        val value = if (y.toIntOption.nonEmpty && y.toInt == 0) {
          0
        } else {
          1
        }
        (zone, Some(value))
      case _ =>
        (None, None)
    }
    (zoneOpt, lockVal) match {
      case (Some(zone), Some(lock)) if zone.map.cavern =>
        //caverns must be rotated in an order
        if (lock == 0) {
          cluster ! InterstellarClusterService.CavernRotation(CavernRotationService.HurryRotationToZoneUnlock(zone.id))
        } else {
          cluster ! InterstellarClusterService.CavernRotation(CavernRotationService.HurryRotationToZoneLock(zone.id))
        }
      case (Some(_), Some(_)) =>
      //normal zones can lock when all facilities and towers on it belong to the same faction
      //normal zones can lock when ???
      case _ => ()
    }
  }

  def commandZoneRotate(): Unit = {
    cluster ! InterstellarClusterService.CavernRotation(CavernRotationService.HurryNextRotation)
  }

  def commandCaptureBase(session: Session, message: ChatMsg, contents: String): Unit = {
    val buffer = cliTokenization(contents).take(3)
    //walk through the param buffer
    val (foundFacilities, foundFacilitiesTag, factionBuffer) = firstParam(session, buffer, captureBaseParamFacilities)
    val (foundFaction, foundFactionTag, timerBuffer) = firstParam(session, factionBuffer, captureBaseParamFaction)
    val (foundTimer, foundTimerTag, _) = firstParam(session, timerBuffer, captureBaseParamTimer)
    //resolve issues with the initial params
    var facilityError: Int = 0
    var factionError: Boolean = false
    var timerError: Boolean = false
    var usageMessage: Boolean = false
    val resolvedFacilities = foundFacilities
      .orElse {
        if (foundFacilitiesTag.nonEmpty) {
          if (foundFaction.isEmpty) {
            /* /capturebase <bad_facility> OR /capturebase <bad_facility> <no_faction> */
            //malformed facility tag error
            facilityError = 2
            None
          } else if (!foundFacilitiesTag.contains("curr")) { //did we do this next check already
            /* /capturebase <faction>, potentially */
            val buildings = captureBaseCurrSoi(session)
            if (buildings.nonEmpty) {
              //convert facilities to faction
              Some(buildings.toSeq)
            } else {
              //no facilities error
              facilityError = 1
              None
            }
          } else {
            //no facilities error
            facilityError = 1
            None
          }
        } else {
          //no params; post command usage reminder
          usageMessage = true
          None
        }
      }
    val resolvedFaction = foundFaction
      .orElse {
        if (resolvedFacilities.nonEmpty) {
          /* /capturebase <facility> OR /capturebase <facility> <timer> */
          if (foundFactionTag.isEmpty || foundTimer.nonEmpty) {
            //convert facilities to OUR PLAYER'S faction
            Some(session.player.Faction)
          } else {
            //malformed faction tag error
            factionError = true
            None
          }
        } else {
          //incorrect params; already posted an error message
          None
        }
      }
    val resolvedTimer = foundTimer
      .orElse {
        //todo stop command execution? post command usage reminder?
        if (resolvedFaction.nonEmpty && foundTimerTag.nonEmpty) {
          /* /capturebase <?> <?> <bad_timer> */
          //malformed timer tag error
          timerError = true
          None
        } else {
          //eh
          Some(1)
        }
      }
    //evaluate results
    if (!commandCaptureBaseProcessResults(resolvedFacilities, resolvedFaction, resolvedTimer)) {
      if (usageMessage) {
        sendResponse(
          message.copy(messageType = UNK_229, contents = "@CMT_CAPTUREBASE_usage")
        )
      } else {
        val msg = if (facilityError == 1) { "can not contextually determine building target" }
        else if (facilityError == 2) { s"\'${foundFacilitiesTag.get}\' is not a valid building name" }
        else if (factionError) { s"\'${foundFactionTag.get}\' is not a valid faction designation" }
        else if (timerError) { s"\'${foundTimerTag.get}\' is not a valid timer value" }
        else { "malformed params; check usage" }
        sendResponse(ChatMsg(UNK_229, wideContents=true, "", s"\\#FF4040ERROR - $msg", None))
      }
    }
  }

  def commandCaptureBaseProcessResults(
                                        resolvedFacilities: Option[Seq[Building]],
                                        resolvedFaction: Option[PlanetSideEmpire.Value],
                                        resolvedTimer: Option[Int]
                                      ): Boolean = {
    //evaluate results
    (resolvedFacilities, resolvedFaction, resolvedTimer) match {
      case (Some(buildings), Some(faction), Some(_)) =>
          //TODO implement timer
        //schedule processing of buildings with a delay
        processBuildingsWithDelay(buildings, faction, 1000) { zone =>
          zone.actor ! ZoneActor.AssignLockedBy(zone, notifyPlayers=true)
        }
        true
      case _ =>
        false
    }
  }

  def processBuildingsWithDelay(
                                 buildings: Seq[Building],
                                 faction: PlanetSideEmpire.Value,
                                 delayMillis: Long
                               )(onComplete: Zone => Unit): Unit = {
    val buildingsToProcess = buildings.filter(b => b.CaptureTerminal.isDefined && b.Faction != faction)
    val iterator = buildingsToProcess.iterator
    val zone = buildings.head.Zone
    var handle: ScheduledFuture[_] = null
    handle = scheduler.scheduleAtFixedRate(
      () => {
        if (iterator.hasNext) {
          val building = iterator.next()
          val terminal = building.CaptureTerminal.get
          val zoneActor = zone.actor
          if (building.CaptureTerminalIsHacked) {
            zone.LocalEvents ! LocalServiceMessage(
              zone.id,
              LocalAction.ResecureCaptureTerminal(terminal, PlayerSource.Nobody)
            )
          }
          zoneActor ! ZoneActor.ZoneMapUpdate()
          building.Actor ! BuildingActor.SetFaction(faction)
          building.Actor ! BuildingActor.AmenityStateChange(terminal, Some(false))
          zoneActor ! ZoneActor.ZoneMapUpdate()
        } else {
          handle.cancel(false)
          onComplete(zone)
        }
      },
      0,
      delayMillis,
      TimeUnit.MILLISECONDS
    )
  }

  def commandVoice(session: Session, message: ChatMsg, contents: String, toChannel: ChatChannel): Unit = {
    // SH prefix are tactical voice macros only sent to squad
    if (contents.startsWith("SH")) {
      channels.foreach {
        case _/*channel*/: SquadChannel =>
          commandSendToRecipient(session, message, SquadChannel(sessionLogic.squad.squad_guid))
        case _ => ()
      }
    } else {
      commandSendToRecipient(session, message, toChannel)
    }
  }

  def commandTellOrIgnore(session: Session, message: ChatMsg, toChannel: ChatChannel): Unit = {
    if (AvatarActor.onlineIfNotIgnored(message.recipient, session.avatar.name)) {
      commandSend(session, message, toChannel)
    } else if (AvatarActor.getLiveAvatarForFunc(message.recipient, (_,_,_)=>{}).isEmpty) {
      sendResponse(
        ChatMsg(ChatMessageType.UNK_45, wideContents=false, "none", "@notell_target", None)
      )
    } else {
      sendResponse(
        ChatMsg(ChatMessageType.UNK_45, wideContents=false, "none", "@notell_ignore", None)
      )
    }
  }

  def commandSquad(session: Session, message: ChatMsg, toChannel: ChatChannel): Unit = {
    channels.foreach {
      case _/*channel*/: SquadChannel =>
        commandSendToRecipient(session, message, toChannel)
      case _ => ()
    }
  }

  def commandOutfit(session: Session, message: ChatMsg, toChannel: ChatChannel): Unit = {
    channels.foreach {
      case _/*channel*/: OutfitChannel =>
        commandSendToRecipient(session, message, toChannel)
      case _ => ()
    }
  }

  def commandWho(session: Session): Unit = {
    val players  = session.zone.Players
    val popTR    = players.count(_.faction == PlanetSideEmpire.TR)
    val popNC    = players.count(_.faction == PlanetSideEmpire.NC)
    val popVS    = players.count(_.faction == PlanetSideEmpire.VS)
    if (popNC + popTR + popVS == 0) {
      sendResponse(ChatMsg(ChatMessageType.CMT_WHO, "@Nomatches"))
    } else {
      val contName = session.zone.map.name
      sendResponse(
        ChatMsg(ChatMessageType.CMT_WHO, wideContents=true, "", "That command doesn't work for now, but : ", None)
      )
      sendResponse(
        ChatMsg(ChatMessageType.CMT_WHO, wideContents=true, "", "NC online : " + popNC + " on " + contName, None)
      )
      sendResponse(
        ChatMsg(ChatMessageType.CMT_WHO, wideContents=true, "", "TR online : " + popTR + " on " + contName, None)
      )
      sendResponse(
        ChatMsg(ChatMessageType.CMT_WHO, wideContents=true, "", "VS online : " + popVS + " on " + contName, None)
      )
    }
  }

  def commandZone(message: ChatMsg, contents: String): Unit = {
    val buffer = cliTokenization(contents)
    val (zone, gate, list) = (buffer.headOption, buffer.lift(1)) match {
      case (Some("-list"), None) =>
        (None, None, true)
      case (Some(zoneId), Some("-list")) =>
        (PointOfInterest.get(zoneId), None, true)
      case (Some(zoneId), gateId) =>
        val zone = PointOfInterest.get(zoneId)
        val gate = (zone, gateId) match {
          case (Some(zone), Some(gateId)) => PointOfInterest.getWarpgate(zone, gateId)
          case (Some(zone), None)         => Some(PointOfInterest.selectRandom(zone))
          case _                          => None
        }
        (zone, gate, false)
      case _ =>
        (None, None, false)
    }
    (zone, gate, list) match {
      case (None, None, true) =>
        sendResponse(ChatMsg(UNK_229, wideContents=true, "", PointOfInterest.list, None))
      case (Some(zone), None, true) =>
        sendResponse(
          ChatMsg(UNK_229, wideContents=true, "", PointOfInterest.listWarpgates(zone), None)
        )
      case (Some(zone), Some(gate), false) =>
        context.self ! SessionActor.SetZone(zone.zonename, gate)
      case (_, None, false) =>
        sendResponse(
          ChatMsg(UNK_229, wideContents=true, "", "Gate id not defined (use '/zone <zone> -list')", None)
        )
      case (_, _, _) if buffer.isEmpty || buffer.headOption.contains("-help") =>
        sendResponse(
          message.copy(messageType = UNK_229, contents = "@CMT_ZONE_usage")
        )
      case _ => ()
    }
  }

  def commandWarp(session: Session, message: ChatMsg, contents: String): Unit = {
    val buffer = cliTokenization(contents)
    val (coordinates, waypoint) = (buffer.headOption, buffer.lift(1), buffer.lift(2)) match {
      case (Some(x), Some(y), Some(z))                       => (Some(x, y, z), None)
      case (Some("to"), Some(_/*character*/), None)          => (None, None) // TODO not implemented
      case (Some("near"), Some(_/*objectName*/), None)       => (None, None) // TODO not implemented
      case (Some(waypoint), None, None) if waypoint.nonEmpty => (None, Some(waypoint))
      case _                                                 => (None, None)
    }
    (coordinates, waypoint) match {
      case (Some((x, y, z)), None) if List(x, y, z).forall { str =>
        val coordinate = str.toFloatOption
        coordinate.isDefined && coordinate.get >= 0 && coordinate.get <= 8191
      } =>
        context.self ! SessionActor.SetPosition(Vector3(x.toFloat, y.toFloat, z.toFloat))
      case (None, Some(waypoint)) if waypoint == "-list" =>
        val zone = PointOfInterest.get(session.player.Zone.id)
        zone match {
          case Some(zone: PointOfInterest) =>
            sendResponse(
              ChatMsg(UNK_229, wideContents=true, "", PointOfInterest.listAll(zone), None)
            )
          case _ =>
            sendResponse(
              ChatMsg(UNK_229, wideContents=true, "", s"unknown player zone '${session.player.Zone.id}'", None)
            )
        }
      case (None, Some(waypoint)) if waypoint != "-help" =>
        PointOfInterest.getWarpLocation(session.zone.id, waypoint) match {
          case Some(location) =>
            context.self ! SessionActor.SetPosition(location)
          case None =>
            sendResponse(
              ChatMsg(UNK_229, wideContents=true, "", s"unknown location '$waypoint'", None)
            )
        }
      case _ =>
        sendResponse(
          message.copy(messageType = UNK_229, contents = "@CMT_WARP_usage")
        )
    }
  }

  def commandSetBattleRank(session: Session, message: ChatMsg, contents: String): Unit = {
    if (!setBattleRank(session, cliTokenization(contents), AvatarActor.SetBep)) {
      sendResponse(
        message.copy(messageType = UNK_229, contents = "@CMT_SETBATTLERANK_usage")
      )
    }
  }

  def commandSetCommandRank(session: Session, message: ChatMsg, contents: String): Unit = {
    if (!setCommandRank(contents, session)) {
      sendResponse(
        message.copy(messageType = UNK_229, contents = "@CMT_SETCOMMANDRANK_usage")
      )
    }
  }

  def commandAddBattleExperience(message: ChatMsg, contents: String): Unit = {
    contents.toIntOption match {
      case Some(bep) => avatarActor ! AvatarActor.AwardBep(bep, ExperienceType.Normal)
      case None =>
        sendResponse(
          message.copy(messageType = UNK_229, contents = "@CMT_ADDBATTLEEXPERIENCE_usage")
        )
    }
  }

  def commandAddCommandExperience(message: ChatMsg, contents: String): Unit = {
    contents.toIntOption match {
      case Some(cep) => avatarActor ! AvatarActor.AwardCep(cep)
      case None =>
        sendResponse(
          message.copy(messageType = UNK_229, contents = "@CMT_ADDCOMMANDEXPERIENCE_usage")
        )
    }
  }

  def commandToggleHat(session: Session, message: ChatMsg, contents: String): Unit = {
    val cosmetics = session.avatar.decoration.cosmetics.getOrElse(Set())
    val nextCosmetics = contents match {
      case "off" =>
        cosmetics.diff(Set(Cosmetic.BrimmedCap, Cosmetic.Beret))
      case _ =>
        if (cosmetics.contains(Cosmetic.BrimmedCap)) {
          cosmetics.diff(Set(Cosmetic.BrimmedCap)) + Cosmetic.Beret
        } else if (cosmetics.contains(Cosmetic.Beret)) {
          cosmetics.diff(Set(Cosmetic.BrimmedCap, Cosmetic.Beret))
        } else {
          cosmetics + Cosmetic.BrimmedCap
        }
    }
    val on = nextCosmetics.contains(Cosmetic.BrimmedCap) || nextCosmetics.contains(Cosmetic.Beret)
    avatarActor ! AvatarActor.SetCosmetics(nextCosmetics)
    sendResponse(
      message.copy(
        messageType = UNK_229,
        contents = s"@CMT_TOGGLE_HAT_${if (on) "on" else "off"}"
      )
    )
  }

  def commandToggleCosmetics(session: Session, message: ChatMsg, contents: String): Unit = {
    val cosmetics = session.avatar.decoration.cosmetics.getOrElse(Set())
    val cosmetic = message.messageType match {
      case ChatMessageType.CMT_HIDE_HELMET     => Cosmetic.NoHelmet
      case ChatMessageType.CMT_TOGGLE_SHADES   => Cosmetic.Sunglasses
      case ChatMessageType.CMT_TOGGLE_EARPIECE => Cosmetic.Earpiece
      case _                                   => null
    }
    val on = contents match {
      case "on"  => true
      case "off" => false
      case _     => !cosmetics.contains(cosmetic)
    }
    avatarActor ! AvatarActor.SetCosmetics(
      if (on) cosmetics + cosmetic
      else cosmetics.diff(Set(cosmetic))
    )
    sendResponse(
      message.copy(
        messageType = UNK_229,
        contents = s"@${message.messageType.toString}_${if (on) "on" else "off"}"
      )
    )
  }

  def commandAddCertification(session: Session, message: ChatMsg, contents: String): Unit = {
    val tokens = cliTokenization(contents)
    val certs = tokens.map(name => Certification.values.find(_.name == name))
    val result = if (tokens.nonEmpty) {
      if (certs.contains(None)) {
        s"@AckErrorCertifications"
      } else {
        avatarActor ! AvatarActor.SetCertifications(session.avatar.certifications ++ certs.flatten)
        s"@AckSuccessCertifications"
      }
    } else {
      if (session.avatar.certifications.size < Certification.values.size) {
        avatarActor ! AvatarActor.SetCertifications(Certification.values.toSet)
      } else {
        avatarActor ! AvatarActor.SetCertifications(Certification.values.filter(_.cost == 0).toSet)
      }
      s"@AckSuccessCertifications"
    }
    sendResponse(message.copy(messageType = UNK_229, contents = result))
  }

  def commandKick(session: Session, message: ChatMsg, contents: String): Unit = {
    val inputs = cliTokenizationCaseSensitive(contents)
    inputs.headOption match {
      case Some(input) =>
        val determination: Player => Boolean = input.toLongOption match {
          case Some(id) => _.CharId == id
          case _ => _.Name.equals(input)
        }
        session.zone.LivePlayers
          .find(determination)
          .orElse(session.zone.Corpses.find(determination)) match {
          case Some(player) =>
            inputs.lift(1).map(_.toLongOption) match {
              case Some(Some(time)) =>
                context.self ! SessionActor.Kick(player, Some(time))
              case _ =>
                context.self ! SessionActor.Kick(player)
            }
            sendResponse(message.copy(messageType = UNK_229, recipient = "Server", contents = "@kick_i"))
          case None =>
            sendResponse(message.copy(messageType = UNK_229, recipient = "Server", contents = "@kick_o"))
        }
      case None =>
        sendResponse(message.copy(messageType = UNK_229, recipient = "Server", contents = "@kick_o"))
    }
  }

  def commandReportUser(@unused session: Session, @unused message: ChatMsg, @unused contents: String): Unit = {
    //todo get user from contents
    sendResponse(ChatMsg(ChatMessageType.UNK_227, "@rpt_i"))
  }

  def commandIncomingSendAllIfOnline(session: Session, message: ChatMsg): Unit = {
    if (AvatarActor.onlineIfNotIgnored(session.avatar, message.recipient)) {
      sendResponse(message)
    }
  }

  def commandIncomingSendToLocalIfOnline(session: Session, fromSession: Session, message: ChatMsg): Unit = {
    if (
      session.zone == fromSession.zone &&
        Vector3.DistanceSquared(session.player.Position, fromSession.player.Position) < 625 &&
        session.player.Faction == fromSession.player.Faction &&
        AvatarActor.onlineIfNotIgnored(session.avatar, message.recipient)
    ) {
      sendResponse(message)
    }
  }

  def commandIncomingVoice(session: Session, fromSession: Session, message: ChatMsg): Unit = {
    if (
      (session.zone == fromSession.zone || message.contents.startsWith("SH")) && /*tactical squad voice macro*/
        Vector3.DistanceSquared(session.player.Position, fromSession.player.Position) < 1600
    ) {
      val name = fromSession.avatar.name
      if (!session.avatar.people.ignored.exists { f => f.name.equals(name) } ||
        {
          val id = fromSession.avatar.id.toLong
          val curr = System.currentTimeMillis()
          ignoredEmoteCooldown.get(id) match {
            case None =>
              ignoredEmoteCooldown.put(id, curr + 15000L)
              true
            case Some(time) if time < curr =>
              ignoredEmoteCooldown.put(id, curr + 15000L)
              true
            case _ =>
              false
          }}
      ) {
        sendResponse(message)
      }
    }
  }

  def commandIncomingSilence(session: Session, message: ChatMsg): Unit = {
    val args = cliTokenization(message.contents)
    val (name, time) = (args.headOption, args.lift(1)) match {
      case (Some(name), _) if name != session.player.Name =>
        log.error("Received silence message for other player")
        (None, None)
      case (Some(name), None)                                     => (Some(name), Some(5))
      case (Some(name), Some(time)) if time.toIntOption.isDefined => (Some(name), Some(time.toInt))
      case _                                                      => (None, None)
    }
    (name, time) match {
      case (Some(_), Some(time)) =>
        if (session.player.silenced) {
          context.self ! SessionActor.SetSilenced(false)
          sendResponse(
            ChatMsg(ChatMessageType.UNK_229, wideContents=true, "", "@silence_off", None)
          )
          if (!silenceTimer.isCancelled) silenceTimer.cancel()
        } else {
          context.self ! SessionActor.SetSilenced(true)
          sendResponse(
            ChatMsg(ChatMessageType.UNK_229, wideContents=true, "", "@silence_on", None)
          )
          import scala.concurrent.ExecutionContext.Implicits.global
          silenceTimer = context.system.scheduler.scheduleOnce(
            time minutes,
            new Runnable {
              def run(): Unit = {
                context.self ! SessionActor.SetSilenced(false)
                sendResponse(
                  ChatMsg(ChatMessageType.UNK_229, wideContents=true, "", "@silence_timeout", None)
                )
              }
            }
          )
        }
      case (name, time) =>
        log.warn(s"Bad silence args $name $time")
    }
  }



  /**
   * For a provided number of facility nanite transfer unit resource silos,
   * charge the facility's silo with an expected amount of nanite transfer units.
   * @see `Amenity`
   * @see `ChatMsg`
   * @see `ResourceSilo`
   * @see `ResourceSilo.UpdateChargeLevel`
   * @see `SessionActor.Command`
   * @see `SessionActor.SendResponse`
   * @param resources the optional number of resources to set to each silo;
   *                  different values provide different resources as indicated below;
   *                  an undefined value also has a condition
   * @param silos where to deposit the resources
   * @param debugContent something for log output context
   */
  private def setBaseResources(resources: Option[Int], silos: Iterable[Amenity], debugContent: String): Unit = {
    if (silos.isEmpty) {
      context.self ! SessionActor.SendResponse(
        ChatMsg(UNK_229, wideContents=true, "Server", s"no targets for ntu found with parameters $debugContent", None)
      )
    }
    resources match {
      // x = n0% of maximum capacitance
      case Some(value) if value > -1 && value < 11 =>
        silos.collect {
          case silo: ResourceSilo =>
            silo.Actor ! ResourceSilo.UpdateChargeLevel(
              value * silo.MaxNtuCapacitor * 0.1f - silo.NtuCapacitor
            )
        }
      // capacitance set to x (where x > 10) exactly, within limits
      case Some(value) =>
        silos.collect {
          case silo: ResourceSilo =>
            silo.Actor ! ResourceSilo.UpdateChargeLevel(value - silo.NtuCapacitor)
        }
      case None =>
        // x >= n0% of maximum capacitance and x <= maximum capacitance
        val rand = new scala.util.Random
        silos.collect {
          case silo: ResourceSilo =>
            val a     = 7
            val b     = 10 - a
            val tenth = silo.MaxNtuCapacitor * 0.1f
            silo.Actor ! ResourceSilo.UpdateChargeLevel(
              a * tenth + rand.nextFloat() * b * tenth - silo.NtuCapacitor
            )
        }
    }
  }

  /**
   * Create a medkit shortcut if there is no medkit shortcut on the hotbar.
   * Bounce the packet to the client and the client will bounce it back to the server to continue the setup,
   * or cancel / invalidate the shortcut creation.
   * @see `Array::indexWhere`
   * @see `CreateShortcutMessage`
   * @see `net.psforever.objects.avatar.Shortcut`
   * @see `net.psforever.packet.game.Shortcut.Medkit`
   * @see `SessionActor.SendResponse`
   * @param guid current player unique identifier for the target client
   * @param shortcuts list of all existing shortcuts, used for early validation
   */
  private def medkitSanityTest(
                                guid: PlanetSideGUID,
                                shortcuts: Array[Option[AvatarShortcut]]
                              ): Unit = {
    if (!shortcuts.exists {
      case Some(a) => a.purpose == 0
      case None    => false
    }) {
      shortcuts.indexWhere(_.isEmpty) match {
        case -1 => ()
        case index =>
          //new shortcut
          sendResponse(CreateShortcutMessage(
            guid,
            index + 1,
            Some(Shortcut.Medkit)
          ))
      }
    }
  }

  /**
   * Create all implant macro shortcuts for all implants whose shortcuts have been removed from the hotbar.
   * Bounce the packet to the client and the client will bounce it back to the server to continue the setup,
   * or cancel / invalidate the shortcut creation.
   * @see `CreateShortcutMessage`
   * @see `ImplantDefinition`
   * @see `net.psforever.objects.avatar.Shortcut`
   * @see `SessionActor.SendResponse`
   * @param guid current player unique identifier for the target client
   * @param haveImplants list of implants the player possesses
   * @param shortcuts list of all existing shortcuts, used for early validation
   */
  private def implantSanityTest(
                                 guid: PlanetSideGUID,
                                 haveImplants: Iterable[ImplantDefinition],
                                 shortcuts: Array[Option[AvatarShortcut]]
                               ): Unit = {
    val haveImplantShortcuts = shortcuts.collect {
      case Some(shortcut) if shortcut.purpose == 2 => shortcut.tile
    }
    var start: Int = 0
    haveImplants.filterNot { imp => haveImplantShortcuts.contains(imp.Name) }
      .foreach { implant =>
        shortcuts.indexWhere(_.isEmpty, start) match {
          case -1 => ()
          case index =>
            //new shortcut
            start = index + 1
            sendResponse(CreateShortcutMessage(
              guid,
              start,
              Some(implant.implantType.shortcut)
            ))
        }
      }
  }

  /**
   * Create a text chat macro shortcut if it doesn't already exist.
   * Bounce the packet to the client and the client will bounce it back to the server to continue the setup,
   * or cancel / invalidate the shortcut creation.
   * @see `Array::indexWhere`
   * @see `CreateShortcutMessage`
   * @see `net.psforever.objects.avatar.Shortcut`
   * @see `net.psforever.packet.game.Shortcut.Macro`
   * @see `SessionActor.SendResponse`
   * @param guid current player unique identifier for the target client
   * @param acronym three letters emblazoned on the shortcut icon
   * @param msg the message published to text chat
   * @param shortcuts a list of all existing shortcuts, used for early validation
   */
  private def macroSanityTest(
                               guid: PlanetSideGUID,
                               acronym: String,
                               msg: String,
                               shortcuts: Array[Option[AvatarShortcut]]
                             ): Unit = {
    shortcuts.indexWhere(_.isEmpty) match {
      case -1 => ()
      case index =>
        //new shortcut
        sendResponse(CreateShortcutMessage(
          guid,
          index + 1,
          Some(Shortcut.Macro(acronym, msg))
        ))
    }
  }

  private def setBattleRank(
                             session: Session,
                             params: Seq[String],
                             msgFunc: Long => AvatarActor.Command
                           ): Boolean = {
    val (target, rank) = (params.headOption, params.lift(1)) match {
      case (Some(target), Some(rank)) if target == session.avatar.name =>
        rank.toIntOption match {
          case Some(rank) => (None, BattleRank.withValueOpt(rank))
          case None       => (None, None)
        }
      case (Some("-h"), _) | (Some("-help"), _) =>
        (None, Some(BattleRank.BR1))
      case (Some(_), Some(_)) =>
        // picking other targets is not supported for now
        (None, None)
      case (Some(rank), None) =>
        rank.toIntOption match {
          case Some(rank) => (None, BattleRank.withValueOpt(rank))
          case None       => (None, None)
        }
      case _ => (None, None)
    }
    (target, rank) match {
      case (_, Some(rank)) if rank.value <= Config.app.game.maxBattleRank =>
        avatarActor ! msgFunc(rank.experience)
        true
      case _ =>
        false
    }
  }

  private def setCommandRank(
                              contents: String,
                              session: Session
                            ): Boolean = {
    val buffer = cliTokenization(contents)
    val (target, rank) = (buffer.headOption, buffer.lift(1)) match {
      case (Some(target), Some(rank)) if target == session.avatar.name =>
        rank.toIntOption match {
          case Some(rank) => (None, CommandRank.withValueOpt(rank))
          case None       => (None, None)
        }
      case (Some(_), Some(_)) =>
        // picking other targets is not supported for now
        (None, None)
      case (Some(rank), None) =>
        rank.toIntOption match {
          case Some(rank) => (None, CommandRank.withValueOpt(rank))
          case None       => (None, None)
        }
      case _ => (None, None)
    }
    (target, rank) match {
      case (_, Some(rank)) =>
        avatarActor ! AvatarActor.SetCep(rank.experience)
        true
      case _ =>
        false
    }
  }

  private def captureBaseParamFacilities(
                                          session: Session,
                                          token: Option[String]
                                        ): Option[Seq[Building]] = {
    token.collect {
      case "curr" =>
        val list = captureBaseCurrSoi(session)
        if (list.nonEmpty) {
          Some(list.toSeq)
        } else {
          None
        }
      case "all" =>
        val list = session.zone.Buildings.values.filter(_.CaptureTerminal.isDefined)
        if (list.nonEmpty) {
          Some(list.toSeq)
        } else {
          None
        }
      case name =>
        val trueName = ZoneInfo
          .values
          .find(_.id.equals(session.zone.id))
          .flatMap { info =>
            info.aliases
              .facilities
              .collectFirst { case (key, internalName) if key.equalsIgnoreCase(name) => internalName }
          }
          .getOrElse(name)
        session.zone.Buildings
          .values
          .find {
            building => trueName.equalsIgnoreCase(building.Name) && building.CaptureTerminal.isDefined
          }
          .map(b => Seq(b))
    }
      .flatten
  }

  private def captureBaseCurrSoi(
                                  session: Session
                                ): Iterable[Building] = {
    val player = session.player
    val positionxy = player.Position.xy
    session
      .zone
      .blockMap
      .sector(player)
      .buildingList
      .filter { building =>
        val radius = building.Definition.SOIRadius
        Vector3.DistanceSquared(building.Position.xy, positionxy) < radius * radius
      }
  }

  def captureBaseParamFaction(
                                       @unused session: Session,
                                       token: Option[String]
                                     ): Option[PlanetSideEmpire.Value] = {
    token.collect {
      case faction =>
        faction.toLowerCase() match {
          case "tr"      => Some(PlanetSideEmpire.TR)
          case "nc"      => Some(PlanetSideEmpire.NC)
          case "vs"      => Some(PlanetSideEmpire.VS)
          case "none"    => Some(PlanetSideEmpire.NEUTRAL)
          case "bo"      => Some(PlanetSideEmpire.NEUTRAL)
          case "neutral" => Some(PlanetSideEmpire.NEUTRAL)
          case _         => None
        }
    }.flatten
  }

  private def captureBaseParamTimer(
                                     @unused session: Session,
                                     token: Option[String]
                                   ): Option[Int] = {
    token.flatMap(_.toIntOption)
  }



  def customCommandWhitetext(
                                      session: Session,
                                      content: Seq[String]
                                    ): Boolean = {
    chatService ! ChatService.Message(
      session,
      ChatMsg(UNK_227, wideContents=true, "", content.mkString(" "), None),
      DefaultChannel
    )
    true
  }

  def customCommandLoc(
                                session: Session,
                                message: ChatMsg
                              ): Boolean = {
    val continent = session.zone
    val player = session.player
    val loc =
      s"zone=${continent.id} pos=${player.Position.x},${player.Position.y},${player.Position.z}; ori=${player.Orientation.x},${player.Orientation.y},${player.Orientation.z}"
    sendResponse(message.copy(contents = loc))
    true
  }

  def customCommandList(
                                 session: Session,
                                 params: Seq[String],
                                 message: ChatMsg
                               ): Boolean = {
    val zone = params.headOption match {
      case Some("") | None =>
        Some(session.zone)
      case Some(id) =>
        Zones.zones.find(_.id == id)
    }
    zone match {
      case Some(inZone) =>
        sendResponse(
          ChatMsg(
            CMT_GMOPEN,
            message.wideContents,
            "Server",
            "\\#8Name (Faction) [ID] at PosX PosY PosZ",
            message.note
          )
        )
        (inZone.LivePlayers ++ inZone.Corpses)
          .filter(_.CharId != session.player.CharId)
          .sortBy(p => (p.Name, !p.isAlive))
          .foreach(player => {
            val color = if (!player.isAlive) "\\#7" else ""
            sendResponse(
              ChatMsg(
                CMT_GMOPEN,
                message.wideContents,
                "Server",
                s"$color${player.Name} (${player.Faction}) [${player.CharId}] at ${player.Position.x.toInt} ${player.Position.y.toInt} ${player.Position.z.toInt}",
                message.note
              )
            )
          })
      case None =>
        sendResponse(
          ChatMsg(
            CMT_GMOPEN,
            message.wideContents,
            "Server",
            "Invalid zone ID",
            message.note
          )
        )
    }
    true
  }

  def customCommandNtu(
                                session: Session,
                                params: Seq[String]
                              ): Boolean = {
    val (facility, customNtuValue) = (params.headOption, params.lift(1)) match {
      case (Some(x), Some(y)) if y.toIntOption.nonEmpty => (Some(x), Some(y.toInt))
      case (Some(x), None) if x.toIntOption.nonEmpty => (None, Some(x.toInt))
      case _ => (None, None)
    }
    val silos = (facility match {
      case Some(cur) if cur.toLowerCase().startsWith("curr") =>
        val position = session.player.Position
        session.zone.Buildings.values
          .filter { building =>
            val soi2 = building.Definition.SOIRadius * building.Definition.SOIRadius
            Vector3.DistanceSquared(building.Position, position) < soi2
          }
      case Some(all) if all.toLowerCase.startsWith("all") =>
        session.zone.Buildings.values
      case Some(x) =>
        session.zone.Buildings.values.find(_.Name.equalsIgnoreCase(x)).toList
      case _ =>
        session.zone.Buildings.values
    })
      .flatMap { building =>
        building.Amenities.filter(_.isInstanceOf[ResourceSilo])
      }
    setBaseResources(customNtuValue, silos, debugContent = s"$facility")
    true
  }

  def customCommandZonerotate(
                                       params: Seq[String]
                                     ): Boolean = {
    cluster ! InterstellarClusterService.CavernRotation(params.headOption match {
      case Some("-list") | Some("-l") =>
        CavernRotationService.ReportRotationOrder(context.self)
      case _ =>
        CavernRotationService.HurryNextRotation
    })
    true
  }

  def customCommandSuicide(
                                    session: Session
                                  ): Boolean = {
    //this is like CMT_SUICIDE but it ignores checks and forces a suicide state
    val tplayer = session.player
    tplayer.Revive
    tplayer.Actor ! Player.Die()
    true
  }

  def customCommandGrenade(
                                    session: Session,
                                    log: Logger
                                  ): Boolean = {
    WorldSession.QuickSwapToAGrenade(session.player, DrawnSlot.Pistol1.id, log)
    true
  }

  def customCommandMacro(
                                  session: Session,
                                  params: Seq[String]
                                ): Boolean = {
    val avatar = session.avatar
    (params.headOption, params.lift(1)) match {
      case (Some(cmd), other) =>
        cmd.toLowerCase() match {
          case "medkit" =>
            medkitSanityTest(session.player.GUID, avatar.shortcuts)
            true

          case "implants" =>
            //implant shortcut sanity test
            implantSanityTest(
              session.player.GUID,
              avatar.implants.collect {
                case Some(implant) if implant.definition.implantType != ImplantType.None => implant.definition
              },
              avatar.shortcuts
            )
            true

          case name
            if ImplantType.values.exists { a => a.shortcut.tile.equals(name) } =>
            avatar.implants.find {
              case Some(implant) => implant.definition.Name.equalsIgnoreCase(name)
              case None => false
            } match {
              case Some(Some(implant)) =>
                //specific implant shortcut sanity test
                implantSanityTest(session.player.GUID, Seq(implant.definition), avatar.shortcuts)
                true
              case _ if other.nonEmpty =>
                //add macro?
                macroSanityTest(session.player.GUID, name, params.drop(2).mkString(" "), avatar.shortcuts)
                true
              case _ =>
                false
            }

          case name
            if name.nonEmpty && other.nonEmpty =>
            //add macro
            macroSanityTest(session.player.GUID, name, params.drop(2).mkString(" "), avatar.shortcuts)
            true

          case _ =>
            false
        }
      case _ =>
        false
    }
  }

  def customCommandProgress(
                                     session: Session,
                                     params: Seq[String]
                                   ): Boolean = {
    val ourRank = BattleRank.withExperience(session.avatar.bep).value
    if (!avatar.permissions.canGM &&
      (ourRank <= Config.app.game.promotion.broadcastBattleRank ||
        ourRank > Config.app.game.promotion.resetBattleRank && ourRank < Config.app.game.promotion.maxBattleRank + 1)) {
      setBattleRank(session, params, AvatarActor.Progress)
      true
    } else {
      setBattleRank(session, Seq("1"), AvatarActor.Progress)
      false
    }
  }

  def customCommandNearby(
                                   session: Session
                                 ): Boolean = {
    val playerPos = session.player.Position.xy
    val closest = session.zone
      .Buildings
      .values
      .toSeq
      .minByOption(base => Vector3.DistanceSquared(playerPos, base.Position.xy))
      .map(base => s"${base.Name} - ${base.Definition.Name}")
    sendResponse(
      ChatMsg(CMT_GMOPEN, wideContents = false, "Server", s"closest facility: $closest", None)
    )
    true
  }

  def customCommandSquad(params: Seq[String]): Boolean = {
    params match {
      case "invites" :: _ =>
        invitationList = Array()
        ask(squadService, SquadService.ListAllCurrentInvites)
          .onComplete {
            case Success(msg @ SquadServiceResponse(_, _, SquadResponse.WantsSquadPosition(_, str))) =>
              invitationList = str.replaceAll("/s","").split(",")
              context.self.forward(msg)
            case _ => ()
          }

      case "accept" :: names if names.contains("all") =>
        squadService ! SquadService.ChainAcceptance(player, player.CharId, Nil)
      case "accept" :: names if names.nonEmpty =>
        //when passing indices to existing invite list, the indices are 1-based
        val results = (names.flatMap(_.toIntOption.flatMap(i => invitationList.lift(i-1))) ++ names)
          .distinct
          .flatMap { name =>
            LivePlayerList.WorldPopulation { case (_, p) => p.name.equalsIgnoreCase(name) }
              .map(_.id.toLong)
        }
        squadService ! SquadService.ChainAcceptance(player, player.CharId, results)

      case "reject" :: names if names.contains("all") =>
        squadService ! SquadService.ChainRejection(player, player.CharId, Nil)
      case "reject" :: names if names.nonEmpty =>
        //when passing indices to existing invite list, the indices are 1-based
        val results = (names.flatMap(_.toIntOption.flatMap(i => invitationList.lift(i-1))) ++ names)
          .distinct
          .flatMap { name =>
            LivePlayerList.WorldPopulation { case (_, p) => p.name.equalsIgnoreCase(name) }
              .map(_.id.toLong)
          }
        squadService ! SquadService.ChainRejection(player, player.CharId, results)

      case _ => ()
    }
    true
  }

  def firstParam[T](
                             session: Session,
                             buffer: Iterable[String],
                             func: (Session, Option[String])=>Option[T]
                           ): (Option[T], Option[String], Iterable[String]) = {
    val tokenOpt = buffer.headOption
    val valueOpt = func(session, tokenOpt)
    val outBuffer = if (valueOpt.nonEmpty) {
      buffer.drop(1)
    } else {
      buffer
    }
    (valueOpt, tokenOpt, outBuffer)
  }

  def cliTokenization(str: String): List[String] = {
    str.replaceAll("\\s+", " ").toLowerCase.trim.split("\\s").toList.filter(!_.equals(""))
  }

  def cliTokenizationCaseSensitive(str: String): List[String] = {
    str.replaceAll("\\s+", " ").trim.split("\\s").toList.filter(!_.equals(""))
  }

  def cliCommaSeparatedParams(params: Seq[String]): Seq[String] = {
    var len = 0
    var appendNext = false
    var formattedParams: Seq[String] = Seq()
    params.foreach {
      case "," =>
        appendNext = true
      case param if appendNext || param.startsWith(",") =>
        formattedParams = formattedParams.slice(0, len - 1) :+ formattedParams(len - 1) + "," + param.replaceAll(",", "")
        appendNext = param.endsWith(",")
      case param if param.endsWith(",") =>
        formattedParams = formattedParams :+ param.take(param.length-1)
        len += 1
        appendNext = true
      case param =>
        formattedParams = formattedParams :+ param
        len += 1
        appendNext = false
    }
    formattedParams
  }

  def commandIncomingSend(message: ChatMsg): Unit = {
    sendResponse(message)
  }

  def commandSend(session: Session, message: ChatMsg, toChannel: ChatChannel): Unit = {
    chatService ! ChatService.Message(
      session,
      message,
      toChannel
    )
  }

  def commandSendToRecipient(session: Session, message: ChatMsg, toChannel: ChatChannel): Unit = {
    chatService ! ChatService.Message(
      session,
      message.copy(recipient = session.player.Name),
      toChannel
    )
  }

  override protected[session] def stop(): Unit = {
    silenceTimer.cancel()
    chatService ! ChatService.LeaveAllChannels(chatServiceAdapter)
  }
}
