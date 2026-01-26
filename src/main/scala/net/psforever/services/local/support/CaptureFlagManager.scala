// Copyright (c) 2021 PSForever
package net.psforever.services.local.support

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.login.WorldSession
import net.psforever.objects.{Default, PlanetSideGameObject, Player}
import net.psforever.objects.guid.{GUIDTask, TaskWorkflow}
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, EnvironmentTrait}
import net.psforever.objects.serverobject.environment.interaction.InteractWithEnvironment
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.structures.{Building, WarpGate}
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.zones.Zone
import net.psforever.objects.zones.interaction.InteractsWithZone
import net.psforever.packet.game._
import net.psforever.services.ServiceManager
import net.psforever.services.ServiceManager.{Lookup, LookupResult}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.{CaptureMessage, LocalAction, LocalServiceMessage}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire, PlanetSideGUID, Vector3}

import scala.concurrent.duration.DurationInt

/**
  * Responsible for handling capture flag related lifecycles
  */
class CaptureFlagManager(zone: Zone) extends Actor {
  import CaptureFlagManager.CaptureFlagEntry
  private[this] val log = org.log4s.getLogger(self.path.name)

  private var galaxyService: ActorRef = ActorRef.noSender
  private var mapUpdateTick: Cancellable = Default.Cancellable
  /** An internally tracked list of cached flags, to avoid querying the amenity owner each second for flag lookups. */
  private var flags: List[CaptureFlagEntry] = Nil

  ServiceManager.serviceManager ! Lookup("galaxy")

  def receive: Receive = {
    case LookupResult("galaxy", endpoint) =>
      galaxyService = endpoint

    case CaptureFlagManager.MapUpdate() =>
      DoMapUpdate()

    case CaptureFlagManager.SpawnCaptureFlag(capture_terminal, target, hackingFaction) =>
      val socket = capture_terminal.Owner.asInstanceOf[Building].GetFlagSocket.get
      // Override CC message when looked at
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        PlanetSideGUID(-1),
        LocalAction.GenericObjectAction(
          capture_terminal.GUID,
          GenericObjectActionEnum.FlagSpawned
        )
      )
      // Register LLU object create task and callback to create on clients
      val flag: CaptureFlag = CaptureFlag.Constructor(
        socket.Position - Vector3.z(value = 1),
        socket.Orientation,
        target,
        socket.Owner,
        hackingFaction
      )
      // Add the flag as an amenity and track it internally
      socket.captureFlag = flag
      TrackFlag(flag)
      TaskWorkflow.execute(WorldSession.CallBackForTask(
        GUIDTask.registerObject(zone.GUID, flag),
        zone.LocalEvents,
        LocalServiceMessage(
          zone.id,
          LocalAction.LluSpawned(flag)
        )
      ))
      // Broadcast chat message for LLU spawn
      val owner = flag.Owner.asInstanceOf[Building]
      CaptureFlagManager.ChatBroadcast(zone, CaptureFlagChatMessageStrings.CTF_FlagSpawned(owner, flag.Target))

    case CaptureFlagManager.Captured(flag: CaptureFlag) =>
      val name = flag.Carrier match {
        case Some(carrier) => carrier.Name
        case None => "A soldier"
      }
      // Trigger Install sound
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        PlanetSideGUID(-1),
        LocalAction.TriggerSound(TriggeredSound.LLUInstall, flag.Target.CaptureTerminal.get.Position, 20, 0.8000001f)
      )
      // Broadcast capture chat message
      CaptureFlagManager.ChatBroadcast(zone, CaptureFlagChatMessageStrings.CTF_Success(name, flag.Faction, flag.Owner.asInstanceOf[Building].Name))
      // Despawn flag
      HandleFlagDespawn(flag)

    case CaptureFlagManager.Lost(flag: CaptureFlag, reason: CaptureFlagLostReasonEnum) =>
      reason match {
        case CaptureFlagLostReasonEnum.Resecured =>
          CaptureFlagManager.ChatBroadcast(
            zone,
            CaptureFlagChatMessageStrings.CTF_Failed_SourceResecured(flag.Owner.asInstanceOf[Building].Name, flag.Owner.asInstanceOf[Building].Faction)
          )
        case CaptureFlagLostReasonEnum.TimedOut  =>
          val building = flag.Owner.asInstanceOf[Building]
          CaptureFlagManager.ChatBroadcast(
            zone,
            CaptureFlagChatMessageStrings.CTF_Failed_TimedOut(building.Name, flag.Target.Name, flag.Faction)
          )
        case CaptureFlagLostReasonEnum.FlagLost  =>
          val building = flag.Owner.asInstanceOf[Building]
          CaptureFlagManager.ChatBroadcast(
            zone,
            CaptureFlagChatMessageStrings.CTF_Failed_FlagLost(building.Name, flag.Faction),
            fanfare = false
          )
        case CaptureFlagLostReasonEnum.Ended     =>
          ()
      }
      HandleFlagDespawn(flag)

    case CaptureFlagManager.PickupFlag(flag: CaptureFlag, player: Player) =>
      flag.Carrier = Some(player)
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        PlanetSideGUID(-1),
        LocalAction.SendResponse(ObjectAttachMessage(player.GUID, flag.GUID, 252))
      )
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        PlanetSideGUID(-1),
        LocalAction.TriggerSound(TriggeredSound.LLUPickup, player.Position, 15, volume = 0.8f)
      )
      CaptureFlagManager.ChatBroadcast(
        zone,
        CaptureFlagChatMessageStrings.CTF_FlagPickedUp(player.Name, player.Faction, flag.Owner.asInstanceOf[Building].Name),
        fanfare = false
      )

    case CaptureFlagManager.DropFlag(flag: CaptureFlag) =>
      flag.Carrier match {
        case Some(player: Player) =>
          val newFlag = flag
          // Set the flag position to where the player is that dropped it
          flag.Position = player.Position
          // Remove attached player from flag
          flag.Carrier = None
          // Send drop packet
          zone.LocalEvents ! LocalServiceMessage(
            zone.id,
            PlanetSideGUID(-1),
            LocalAction.SendResponse(ObjectDetachMessage(player.GUID, flag.GUID, player.Position, 0, 0, 0))
          )
          // Send dropped chat message
          CaptureFlagManager.ChatBroadcast(
            zone,
            CaptureFlagChatMessageStrings.CTF_FlagDropped(player.Name, player.Faction, flag.Owner.asInstanceOf[Building].Name),
            fanfare = false
          )
          HandleFlagDespawn(flag)
          // Register LLU object create task and callback to create on clients
          val replacementLlu = CaptureFlag.Constructor(
            newFlag.Position,
            newFlag.Orientation,
            newFlag.Target,
            newFlag.Owner,
            player.Faction
          )
          // Add the flag as an amenity and track it internally
          val socket = newFlag.Owner.asInstanceOf[Building].GetFlagSocket.get
          socket.captureFlag = replacementLlu
          TrackFlag(replacementLlu)
          TaskWorkflow.execute(WorldSession.CallBackForTask(
            GUIDTask.registerObject(zone.GUID, replacementLlu),
            zone.LocalEvents,
            LocalServiceMessage(
              zone.id,
              LocalAction.LluSpawned(replacementLlu)
            )
          ))
        case _ =>
          log.warn("Tried to drop flag but flag has no carrier")
      }

    case _ =>
      log.warn("Received unhandled message")
  }

  private def DoMapUpdate(): Unit = {
    val flagInfo = flags.map { case entry @ CaptureFlagManager.CaptureFlagEntry(flag) =>
      val owner = flag.Owner.asInstanceOf[Building]
      val pos = flag.Position
      val hackTimeRemaining = owner.infoUpdateMessage().hack_time_remaining
      val nextMessageAfterMinutes = CaptureFlagManager.CaptureFlagCountdownMessages(entry.currentMessageIndex)
      if (hackTimeRemaining < nextMessageAfterMinutes.minutes.toMillis) {
        entry.currentMessageIndex += 1
        val msg = CaptureFlagManager.ComposeWarningMessage(flag, owner.Name, nextMessageAfterMinutes)
        CaptureFlagManager.ChatBroadcast(zone, msg, fanfare = false)
      }
      FlagInfo(
        u1 = 0,
        owner_map_id = owner.MapId,
        target_map_id = flag.Target.MapId,
        x = pos.x,
        y = pos.y,
        hack_time_remaining = hackTimeRemaining,
        is_monolith_unit = false
      )
    }
    galaxyService ! GalaxyServiceMessage(GalaxyAction.FlagMapUpdate(CaptureFlagUpdateMessage(zone.Number, flagInfo)))
  }

  private def TrackFlag(flag: CaptureFlag): Unit = {
    flag.Owner.Amenities = flag
    flags = flags :+ CaptureFlagEntry(flag)
    if (mapUpdateTick.isCancelled) {
      // Start sending map updates periodically
      import scala.concurrent.ExecutionContext.Implicits.global
      mapUpdateTick = context.system.scheduler.scheduleAtFixedRate(initialDelay = 0 seconds, interval = 1 second, self, CaptureFlagManager.MapUpdate())
    }
  }

  private def UntrackFlag(flag: CaptureFlag): Unit = {
    flag.Owner.RemoveAmenity(flag)
    flags = flags.filterNot(x => x.flag eq flag)
    if (flags.isEmpty) {
      mapUpdateTick.cancel()
      DoMapUpdate()
    }
  }

  private def HandleFlagDespawn(flag: CaptureFlag): Unit = {
    // Remove the flag as an amenity
    flag.Owner.asInstanceOf[Building].GetFlagSocket.get.captureFlag = None
    UntrackFlag(flag)
    // Unregister LLU from clients,
    zone.LocalEvents ! LocalServiceMessage(zone.id, PlanetSideGUID(-1), LocalAction.LluDespawned(flag.GUID, flag.Position))
    // Then unregister it from the GUID pool
    TaskWorkflow.execute(GUIDTask.unregisterObject(zone.GUID, flag))
  }
}

object CaptureFlagManager {
  sealed trait Command

  final case class SpawnCaptureFlag(capture_terminal: CaptureTerminal, target: Building, hackingFaction: PlanetSideEmpire.Value) extends Command
  final case class PickupFlag(flag: CaptureFlag, player: Player) extends Command
  final case class DropFlag(flag: CaptureFlag) extends Command
  final case class Captured(flag: CaptureFlag) extends Command
  final case class Lost(flag: CaptureFlag, reason: CaptureFlagLostReasonEnum) extends Command
  final case class MapUpdate()

  private case class CaptureFlagEntry(flag: CaptureFlag) {
    var currentMessageIndex: Int = 0
  }

  val CaptureFlagCountdownMessages: Seq[Int] = Seq(10, 5, 2, 1, 0)

  private def ChatBroadcast(zone: Zone, message: String, fanfare: Boolean = true): Unit = {
    //todo use UNK_222 sometimes
    //todo I think the fanfare was relate to whether the message was celebratory is tone, based on the faction
    val messageType: ChatMessageType = if (fanfare) {
      ChatMessageType.UNK_223
    } else {
      ChatMessageType.UNK_229
    }
    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      PlanetSideGUID(-1),
      LocalAction.ChatMessage(ChatMsg(messageType, wideContents = true, "", message, None))
    )
  }

  private def ComposeWarningMessage(flag: CaptureFlag, buildingName: String, minutesLeft: Int): String = {
    import CaptureFlagChatMessageStrings._
    val carrier = flag.Carrier
    val hasCarrier = carrier.nonEmpty
    minutesLeft match {
      case 1 if hasCarrier =>
        CTF_Warning_Carrier1Min(carrier.get.Name, flag.Faction, buildingName, flag.Target.Name)
      case 1 =>
        CTF_Warning_NoCarrier1Min(buildingName, flag.Faction, flag.Target.Name)
      case time if hasCarrier =>
        CTF_Warning_Carrier(carrier.get.Name, flag.Faction, buildingName, flag.Target.Name, time)
      case time =>
        CTF_Warning_NoCarrier(buildingName, flag.Faction, flag.Target.Name, time)
    }
  }

  /**
   * na
   * @param flagGuid flag that may exist
   * @param target evaluate this to determine if to continue with this loss
   */
  def ReasonToLoseFlagViolently(
                                 zone: Zone,
                                 flagGuid: Option[PlanetSideGUID],
                                 target: PlanetSideGameObject with InteractsWithZone
                               ): Boolean = {
    zone
      .GUID(flagGuid)
      .collect {
        case flag: CaptureFlag
          if LoseFlagViolentlyToEnvironment(target, Set(EnvironmentAttribute.Water, EnvironmentAttribute.Lava, EnvironmentAttribute.Death)) /*||
            LoseFlagViolentlyToWarpGateEnvelope(zone, target)*/ =>
          flag.Destroyed = true
          zone.LocalEvents ! CaptureMessage(HackCaptureActor.FlagLost(flag))
          true
      }
      .getOrElse(false)
  }

  def LoseFlagViolentlyToEnvironment(
                                      target: PlanetSideGameObject with InteractsWithZone,
                                      deniedEnvironments: Set[EnvironmentTrait]
                                    ): Boolean = {
      target
        .interaction()
        .collectFirst { case env: InteractWithEnvironment => env.OngoingInteractions }
        .map(_.intersect(deniedEnvironments))
        .getOrElse(Set())
        .nonEmpty
  }

  def LoseFlagViolentlyToWarpGateEnvelope(
                                           zone: Zone,
                                           target: PlanetSideGameObject with InteractsWithZone
                                         ): Boolean = {
    val position = target.Position
    zone
      .blockMap
      .sector(position, range = 10f)
      .buildingList
      .collectFirst {
        case gate: WarpGate if Vector3.DistanceSquared(position, gate.Position) < math.pow(gate.Definition.SOIRadius, 2f) => gate
      }
      .nonEmpty
  }
}

object CaptureFlagChatMessageStrings {
  // @CTF_Success=%1 captured %2's LLU for the %3!
  /** {player.Name} captured {ownerName}'s LLU for the {player.Faction}! */
  private[support] def CTF_Success(playerName:String, playerFaction: PlanetSideEmpire.Value, ownerName: String): String =
    s"@CTF_Success^$playerName~^@$ownerName~^@${GetFactionString(playerFaction)}~"

  // @CTF_Failed_TimedOut=The %1 failed to deliver %2's LLU to %3 in time!\nHack canceled!
  /** The {faction} failed to deliver {ownerName}'s LLU to {name} in time!\nHack canceled! */
  private[support] def CTF_Failed_TimedOut(ownerName: String, name: String, faction: PlanetSideEmpire.Value): String =
    s"@CTF_Failed_TimedOut^@${GetFactionString(faction)}~^@$ownerName~^@$name~"

  // @CTF_Failed_Lost=The %1 lost %2's LLU!\nHack canceled!
  /** The {faction} lost {ownerName}'s LLU!\nHack canceled! */
  private[support] def CTF_Failed_FlagLost(ownerName: String, faction: PlanetSideEmpire.Value): String =
    s"@CTF_Failed_FlagLost^@${GetFactionString(faction)}~^@$ownerName~"

  // @CTF_Failed_TargetLost=%1's LLU target facility %2 was lost!\nHack canceled!
  /** {hackFacility}'s LLU target facility {targetFacility} was lost!\nHack canceled! */
  private[support] def CTF_Failed_TargetLost(hackFacility: String, targetFacility: String): String =
    s"@CTF_Failed_TargetLost^@$hackFacility~^@$targetFacility~"

  // @CTF_Failed_SourceResecured=The %1 resecured %2!\nThe LLU was lost!
  /** The {faction} resecured {name}!\nThe LLU was lost! */
  private[support] def CTF_Failed_SourceResecured(name: String, faction: PlanetSideEmpire.Value): String =
    s"@CTF_Failed_SourceResecured^@${GetFactionString(faction)}~^@$name~"

  // @CTF_FlagSpawned=%1 %2 has spawned a LLU.\nIt must be taken to %3 %4's Control Console within %5 minutes or the hack will fail!
  /** {facilityType} {facilityName} has spawned a LLU.\nIt must be taken to {targetFacilityType} {targetFacilityName}'s Control Console within 15 minutes or the hack will fail! */
  private[support] def CTF_FlagSpawned(owner: Building, target: Building): String =
    s"@CTF_FlagSpawned^@${owner.Definition.Name}~^@${owner.Name}~^@${target.Definition.Name}~^@${target.Name}~^15~"

  // @CTF_FlagPickedUp=%1 of the %2 picked up %3's LLU
  /** {player.Name} of the {player.Faction} picked up {ownerName}'s LLU */
  private[support] def CTF_FlagPickedUp(playerName: String, playerFaction: PlanetSideEmpire.Value, ownerName: String): String =
    s"@CTF_FlagPickedUp^$playerName~^@${GetFactionString(playerFaction)}~^@$ownerName~"

  // @CTF_FlagDropped=%1 of the %2 dropped %3's LLU
  /** {playerName} of the {faction} dropped {facilityName}'s LLU */
  private[support] def CTF_FlagDropped(playerName: String, playerFaction: PlanetSideEmpire.Value, ownerName: String): String =
    s"@CTF_FlagDropped^$playerName~^@${GetFactionString(playerFaction)}~^@$ownerName~"

  // @CTF_Warning_Carrier=%1's LLU is in the field.\nThe %2 must take it to %3 within %4 minutes!
  /** {facilityName}'s LLU is in the field.\nThe {faction} must take it to {targetFacilityName} within {time} minutes! */
  private[support] def CTF_Warning_Carrier(
                                            playerName:String,
                                            playerFaction: PlanetSideEmpire.Value,
                                            facilityName: String,
                                            targetFacilityName: String,
                                            time: Int
                                          ): String = {
    s"@CTF_Warning_Carrier^$playerName~^@${GetFactionString(playerFaction)}~^@$facilityName~^@$targetFacilityName~^@$time~"
  }

  // @CTF_Warning_Carrier1Min=%1 of the %2 has %3's LLU.\nIt must be taken to %4 within the next minute!
  /** {playerName} of the {faction} has {facilityName}'s LLU.\nIt must be taken to {targetFacilityName} within the next minute! */
  private[support] def CTF_Warning_Carrier1Min(
                                                playerName:String,
                                                playerFaction: PlanetSideEmpire.Value,
                                                facilityName: String,
                                                targetFacilityName: String
                                              ): String = {
    s"@CTF_Warning_Carrier1Min^$playerName~^@${GetFactionString(playerFaction)}~^@$facilityName~^@$targetFacilityName~"
  }

  // @CTF_Warning_NoCarrier=%1's LLU is in the field.\nThe %2 must take it to %3 within %4 minutes!
  /** {facilityName}'s LLU is in the field.\nThe {faction} must take it to {targetFacilityName} within {time} minute! */
  private[support] def CTF_Warning_NoCarrier(
                                              facilityName: String,
                                              playerFaction: PlanetSideEmpire.Value,
                                              targetFacilityName: String,
                                              time: Int
                                            ): String = {
    s"@CTF_Warning_NoCarrier^@$facilityName~^@${GetFactionString(playerFaction)}~^@$targetFacilityName~^$time~"
  }

  // @CTF_Warning_NoCarrier1Min=%1's LLU is in the field.\nThe %2 must take it to %3 within the next minute!
  /** {facilityName}'s LLU is in the field.\nThe {faction} must take it to {targetFacilityName} within the next minute! */
  private[support] def CTF_Warning_NoCarrier1Min(
                                                  facilityName: String,
                                                  playerFaction: PlanetSideEmpire.Value,
                                                  targetFacilityName: String
                                                ): String = {
    s"@CTF_Warning_NoCarrier1Min^@$facilityName~^@${GetFactionString(playerFaction)}~^@$targetFacilityName~"
  }

  private def GetFactionString: PlanetSideEmpire.Value=>String = {
    case PlanetSideEmpire.TR => "TerranRepublic"
    case PlanetSideEmpire.NC => "NewConglomerate"
    case PlanetSideEmpire.VS => "VanuSovereigncy" //intentional typo; it's like this in packet captures
    case _                   => "TerranRepublic" //todo: BO message?
  }
}

sealed trait CaptureFlagLostReasonEnum

object CaptureFlagLostReasonEnum {
  final case object Resecured extends CaptureFlagLostReasonEnum
  final case object TimedOut extends CaptureFlagLostReasonEnum
  final case object Ended extends CaptureFlagLostReasonEnum
  final case object FlagLost extends CaptureFlagLostReasonEnum
}
