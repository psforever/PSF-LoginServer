package net.psforever.services.local.support

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.login.WorldSession
import net.psforever.objects.guid.actor.TaskWorkflow
import net.psforever.objects.{Default, Player}
import net.psforever.objects.guid.GUIDTask
import net.psforever.objects.serverobject.llu.CaptureFlag
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.serverobject.terminals.capture.CaptureTerminal
import net.psforever.objects.zones.Zone
import net.psforever.packet.game._
import net.psforever.services.ServiceManager
import net.psforever.services.ServiceManager.{Lookup, LookupResult}
import net.psforever.services.galaxy.{GalaxyAction, GalaxyServiceMessage}
import net.psforever.services.local.support.CaptureFlagLostReasonEnum.CaptureFlagLostReasonEnum
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.types.{ChatMessageType, PlanetSideEmpire, PlanetSideGUID, Vector3}

import scala.concurrent.duration.DurationInt

/**
  * Responsible for handling capture flag related lifecycles
  */
class CaptureFlagManager(zone: Zone) extends Actor{
  private[this] val log = org.log4s.getLogger(self.path.name)

  var galaxyService: ActorRef = ActorRef.noSender

  private var mapUpdateTick: Cancellable = Default.Cancellable

  /** An internally tracked list of current flags, to avoid querying AmenityOwners each second for flag lookups */
  private var flags: List[CaptureFlag] = Nil

  private def TrackFlag(flag: CaptureFlag): Unit = {
    flag.Owner.Amenities = flag
    flags = flags :+ flag

    if (mapUpdateTick.isCancelled) {
      // Start sending map updates periodically
      import scala.concurrent.ExecutionContext.Implicits.global
      mapUpdateTick = context.system.scheduler.scheduleAtFixedRate(0 seconds, 1 second, self, CaptureFlagManager.MapUpdate())
    }
  }

  private def UntrackFlag(flag: CaptureFlag): Unit = {
    flag.Owner.RemoveAmenity(flag)
    flags = flags.filterNot(x => x == flag)

    if (flags.isEmpty) {
      mapUpdateTick.cancel()

      // Send one final map update to clear the last flag from the map
      self ! CaptureFlagManager.MapUpdate()
    }
  }

  val serviceManager = ServiceManager.serviceManager
  serviceManager ! Lookup("galaxy")

  def receive: Receive = {
    case LookupResult("galaxy", endpoint) =>
      galaxyService = endpoint

    case CaptureFlagManager.MapUpdate() =>
      val flagInfo = flags.map(flag =>
        FlagInfo(
          u1 = 0,
          owner_map_id = flag.Owner.asInstanceOf[Building].MapId,
          target_map_id = flag.Target.MapId,
          x = flag.Position.x,
          y = flag.Position.y,
          hack_time_remaining = flag.Owner.asInstanceOf[Building].infoUpdateMessage().hack_time_remaining,
          is_monolith_unit = false
        )
      )

      galaxyService ! GalaxyServiceMessage(GalaxyAction.FlagMapUpdate(CaptureFlagUpdateMessage(zone.Number, flagInfo)))

    case CaptureFlagManager.SpawnCaptureFlag(capture_terminal, target, hackingFaction) =>
      val zone = capture_terminal.Zone
      val socket = capture_terminal.Owner.asInstanceOf[Building].GetFlagSocket.get

      // Override CC message when looked at
        zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.SendGenericObjectActionMessage(
          PlanetSideGUID(-1),
          capture_terminal.GUID,
          GenericObjectActionEnum.FlagSpawned
        )
      )

      // Register LLU object create task and callback to create on clients
      val flag: CaptureFlag = CaptureFlag.Constructor(
        Vector3(socket.Position.x, socket.Position.y, socket.Position.z - 1),
        socket.Orientation,
        target,
        socket.Owner,
        hackingFaction
      )

      // Add the flag as an amenity and track it internally
      socket.captureFlag = flag
      TrackFlag(flag)

      TaskWorkflow.execute(WorldSession.CallBackForTask(
        GUIDTask.registerObject(socket.Zone.GUID, flag),
        socket.Zone.LocalEvents,
        LocalServiceMessage(
          socket.Zone.id,
          LocalAction.LluSpawned(PlanetSideGUID(-1), flag)
        )
      ))

      // Broadcast chat message for LLU spawn
      val owner = flag.Owner.asInstanceOf[Building]
      ChatBroadcast(flag.Zone, CaptureFlagChatMessageStrings.CTF_FlagSpawned(owner, flag.Target))

    case CaptureFlagManager.Captured(flag: CaptureFlag) =>
      // Trigger Install sound
      flag.Zone.LocalEvents ! LocalServiceMessage(flag.Zone.id, LocalAction.TriggerSound(PlanetSideGUID(-1), TriggeredSound.LLUInstall, flag.Target.CaptureTerminal.get.Position, 20, 0.8000001f))

      // Broadcast capture chat message
      ChatBroadcast(flag.Zone, CaptureFlagChatMessageStrings.CTF_Success(flag.Carrier.get, flag.Owner.asInstanceOf[Building].Name))

      // Despawn flag
      HandleFlagDespawn(flag)

    case CaptureFlagManager.Lost(flag: CaptureFlag, reason: CaptureFlagLostReasonEnum) =>
      reason match {
        case CaptureFlagLostReasonEnum.Resecured =>
          ChatBroadcast(
            flag.Zone,
            CaptureFlagChatMessageStrings.CTF_Failed_SourceResecured(flag.Owner.asInstanceOf[Building])
          )
        case CaptureFlagLostReasonEnum.TimedOut  =>
          ChatBroadcast(
            flag.Zone,
            CaptureFlagChatMessageStrings.CTF_Failed_TimedOut(flag.Owner.asInstanceOf[Building].Name, flag.Target)
          )
        case CaptureFlagLostReasonEnum.Ended     => ; /* no message */
      }
      HandleFlagDespawn(flag)

    case CaptureFlagManager.PickupFlag(flag: CaptureFlag, player: Player) =>
      val continent = flag.Zone

      flag.Carrier = Some(player)

      continent.LocalEvents ! LocalServiceMessage(continent.id, LocalAction.SendPacket(ObjectAttachMessage(player.GUID, flag.GUID, 252)))
      continent.LocalEvents ! LocalServiceMessage(continent.id, LocalAction.TriggerSound(PlanetSideGUID(-1), TriggeredSound.LLUPickup, player.Position, 15, volume = 0.8f))

      ChatBroadcast(flag.Zone, CaptureFlagChatMessageStrings.CTF_FlagPickedUp(player, flag.Owner.asInstanceOf[Building].Name), fanfare = false)

    case CaptureFlagManager.DropFlag(flag: CaptureFlag) =>
      flag.Carrier match {
        case Some(player: Player) =>
          // Set the flag position to where the player is that dropped it
          flag.Position = player.Position

          // Remove attached player from flag
          flag.Carrier = None

          // Send drop packet
          flag.Zone.LocalEvents ! LocalServiceMessage(flag.Zone.id, LocalAction.SendPacket(ObjectDetachMessage(player.GUID, flag.GUID, player.Position, 0, 0, 0)))

          // Send dropped chat message
          ChatBroadcast(flag.Zone, CaptureFlagChatMessageStrings.CTF_FlagDropped(player, flag.Owner.asInstanceOf[Building].Name), fanfare = false)

        case None =>
          log.warn("Tried to drop flag but flag has no carrier")
      }

    case _ =>
      log.warn("Received unhandled message");
  }

  private def HandleFlagDespawn(flag: CaptureFlag): Unit = {
    // Remove the flag as an amenity
    flag.Owner.asInstanceOf[Building].GetFlagSocket.get.captureFlag = None
    UntrackFlag(flag)
    // Unregister LLU from clients,
    flag.Zone.LocalEvents ! LocalServiceMessage(flag.Zone.id, LocalAction.LluDespawned(PlanetSideGUID(-1), flag))
    // Then unregister it from the GUID pool
    TaskWorkflow.execute(GUIDTask.unregisterObject(flag.Zone.GUID,flag))
  }

  private def ChatBroadcast(zone: Zone, message: String, fanfare: Boolean = true): Unit = {
    val messageType: ChatMessageType = if (fanfare) {
      ChatMessageType.UNK_223
    } else {
      ChatMessageType.UNK_229
    }

    zone.LocalEvents ! LocalServiceMessage(
      zone.id,
      LocalAction.SendChatMsg(
        PlanetSideGUID(-1),
        ChatMsg(messageType, wideContents = false, "", message, None)
      )
    )
  }
}

object CaptureFlagManager {
  final case class SpawnCaptureFlag(capture_terminal: CaptureTerminal, target: Building, hackingFaction: PlanetSideEmpire.Value)
  final case class PickupFlag(flag: CaptureFlag, player: Player)
  final case class DropFlag(flag: CaptureFlag)
  final case class Captured(flag: CaptureFlag)
  final case class Lost(flag: CaptureFlag, reason: CaptureFlagLostReasonEnum)
  final case class MapUpdate()
}

object CaptureFlagChatMessageStrings {
  /*
      @CTF_Failed_TargetLost=%1's LLU target facility %2 was lost!\nHack canceled!
      @CTF_Failed_FlagLost=The %1 lost %2's LLU!\nHack canceled!
      @CTF_Warning_Carrier=%1 of the %2 has %3's LLU.\nIt must be taken to %4 within %5 minutes!
      @CTF_Warning_NoCarrier=%1's LLU is in the field.\nThe %2 must take it to %3 within %4 minutes!
      @CTF_Warning_Carrier1Min=%1 of the %2 has %3's LLU.\nIt must be taken to %4 within the next minute!
      @CTF_Warning_NoCarrier1Min=%1's LLU is in the field.\nThe %2 must take it to %3 within the next minute!
   */

  // @CTF_Success=%1 captured %2's LLU for the %3!
  /** {player.Name} captured {owner_name}'s LLU for the {player.Faction}! */
  def CTF_Success(player: Player, owner_name: String): String = s"@CTF_Success^${player.Name}~^@$owner_name~^@${GetFactionString(player.Faction)}~"

  // @CTF_Failed_TimedOut=The %1 failed to deliver %2's LLU to %3 in time!\nHack canceled!
  /** The {target.Faction} failed to deliver {owner_name}'s LLU to {target.Name} in time!\nHack canceled! */
  def CTF_Failed_TimedOut(owner_name: String, target: Building): String = s"@CTF_Failed_TimedOut^@${GetFactionString(target.Faction)}~^@$owner_name~^@${target.Name}~"

  // @CTF_Failed_SourceResecured=The %1 resecured %2!\nThe LLU was lost!
  /** The {owner.Faction} resecured {owner.Name}!\nThe LLU was lost! */
  def CTF_Failed_SourceResecured(owner: Building): String = s"@CTF_Failed_SourceResecured^@${CaptureFlagChatMessageStrings.GetFactionString(owner.Faction)}~^@${owner.Name}~"



  // @CTF_FlagSpawned=%1 %2 has spawned a LLU.\nIt must be taken to %3 %4's Control Console within %5 minutes or the hack will fail!
  /** {facilityType} {facilityName} has spawned a LLU.\nIt must be taken to {targetFacilityType} {targetFacilityName}'s Control Console within 15 minutes or the hack will fail! */
  def CTF_FlagSpawned(owner: Building, target: Building): String = s"@CTF_FlagSpawned^@${owner.Definition.Name}~^@${owner.Name}~^@${target.Definition.Name}~^@${target.Name}~^15~"


  // @CTF_FlagPickedUp=%1 of the %2 picked up %3's LLU
  /** {playerName} of the {faction} picked up {facilityName}'s LLU */
  def CTF_FlagPickedUp(player: Player, owner_name: String): String = s"@CTF_FlagPickedUp^${player.Name}~^@${CaptureFlagChatMessageStrings.GetFactionString(player.Faction)}~^@$owner_name~"

  // @CTF_FlagDropped=%1 of the %2 dropped %3's LLU
  /** {playerName} of the {faction} dropped {facilityName}'s LLU */
  def CTF_FlagDropped(player: Player, owner_name: String): String = s"@CTF_FlagDropped^${player.Name}~^@${CaptureFlagChatMessageStrings.GetFactionString(player.Faction)}~^@$owner_name~"

  // todo: make private
  private def GetFactionString(faction: PlanetSideEmpire.Value): String = {
    faction match {
      case PlanetSideEmpire.TR => "TerranRepublic"
      case PlanetSideEmpire.NC => "NewConglomerate"
      case PlanetSideEmpire.VS => "VanuSovereigncy" // Yes, this is wrong. It is like that in packet captures.
      case _                   => "TerranRepublic" //todo: BO message?
    }
  }
}

object CaptureFlagLostReasonEnum extends Enumeration {
  type CaptureFlagLostReasonEnum = Value

  val Resecured, TimedOut, Ended = Value
}
