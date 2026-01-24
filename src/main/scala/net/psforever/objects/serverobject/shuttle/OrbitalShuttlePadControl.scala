// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.shuttle

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.guid._
import net.psforever.objects.{Player, Vehicle}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.hart.{HartTimer, HartTimerActions}
import net.psforever.services.{Service, ServiceManager}
import net.psforever.types.ChatMessageType

import scala.concurrent.Future

/**
  * An `Actor` that handles messages being dispatched to a specific `OrbitalShuttlePad`.<br>
  * <br>
  * For the purposes of maintaining a close relationship
  * with the rest of the high altitude rapid transport (HART) system's components,
  * this control agency also locally creates the vehicle that will the shuttle when it starts up.
  * The shuttle should be treated like a supporting object to the zone
  * that exists within the normal vehicle pipeline.
  * @see `ShuttleState`
  * @see `ShuttleTimer`
  * @see `HartService`
  * @param pad the `OrbitalShuttlePad` object being governed
  */
class OrbitalShuttlePadControl(pad: OrbitalShuttlePad) extends Actor {
  /** the doors that allow would be passengers to access the shuttle boarding gantries
    * (actually, a hallway with a teleport);
    * the target doors are of a specific type that flag their purpose - "gr_door_mb_orb"
    */
  var managedDoors: List[Door] = Nil
  var shuttle: Vehicle = _

  def receive: Receive = startUp

  /** the HART system is active and ready to handle state changes */
  val taxiing: Receive = {
    case OrbitalShuttlePad.GetShuttle(to) =>
      to ! OrbitalShuttlePad.GiveShuttle(shuttle)

    case HartTimer.LockDoors =>
      managedDoors.foreach { door =>
        door.Actor ! Door.UpdateMechanism(OrbitalShuttlePadControl.lockedWaitingForShuttle)
        val zone = pad.Zone
        if(door.isOpen) {
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.DoorSlamsShut(door))
        }
      }

    case HartTimer.UnlockDoors =>
      managedDoors.foreach { _.Actor ! Door.UpdateMechanism(OrbitalShuttlePadControl.shuttleIsBoarding) }

    case HartTimer.ShuttleDocked(forChannel) =>
      HartTimerActions.ShuttleDocked(pad, shuttle, forChannel)

    case HartTimer.ShuttleFreeFromDock(forChannel) =>
      HartTimerActions.ShuttleFreeFromDock(pad, shuttle, forChannel)

    case HartTimer.ShuttleStateUpdate(forChannel, state) =>
      HartTimerActions.ShuttleStateUpdate(pad, shuttle, forChannel, state)

    case _ => ;
  }

  /** wire the pad and shuttle into a zone-scoped service handler */
  val shuttleTime: Receive = {
    case Zone.Vehicle.HasSpawned(_, newShuttle: OrbitalShuttle) =>
      shuttle = newShuttle
      pad.shuttle = newShuttle
      pad.Owner.Amenities = new ShuttleAmenity(newShuttle)
      ServiceManager.serviceManager ! ServiceManager.Lookup("hart")

    case ServiceManager.LookupResult(_, timer) =>
      timer ! HartTimer.PairWith(pad.Zone, pad.GUID, shuttle.GUID, self)
      context.become(taxiing)

    case Zone.Vehicle.CanNotSpawn(zone, _, reason) =>
      org.log4s
        .getLogger("OrbitalShuttle")
        .error(s"shuttle for pad#${pad.Owner.GUID.guid} in zone ${zone.id} did not spawn - $reason")
      //seal doors
      managedDoors.foreach { _.Actor ! Door.UpdateMechanism(OrbitalShuttlePadControl.lockedWaitingForShuttle) }

    case msg: HartTimer.Command =>
      self.forward(msg) //delay?

    case _ => ;
  }

  /** collect all of the doors that will be controlled by the HART system;
    * set up the shuttle information based on the pad to which it belongs;
    * register and add the shuttle as a common vehicle of the said zone
    */
  val startUp: Receive = {
    case Service.Startup() =>
      import net.psforever.types.Vector3
      import net.psforever.types.Vector3.DistanceSquared
      import net.psforever.objects.GlobalDefinitions._
      val position = pad.Position
      val zone = pad.Zone
      //collect managed doors
      managedDoors = pad.Owner.Amenities
        .collect { case d: Door if d.Definition == gr_door_mb_orb => d }
        .sortBy { o => DistanceSquared(position, o.Position) }
        .take(8)
      //create shuttle
      val newShuttle = new OrbitalShuttle(orbital_shuttle)
      newShuttle.Position = position + Vector3(0, -8.25f, 0).Rz(pad.Orientation.z) //magic offset number
      newShuttle.Orientation = pad.Orientation
      newShuttle.Faction = pad.Faction
      shuttleRegistration(zone, newShuttle, self)
      context.become(shuttleTime)

    case _ => ;
  }

  /**
   * If the new shuttle fails to register the nth time, try again.
   * Don't take "no" for an answer.
   */
  def shuttleRegistration(zone: Zone, newShuttle: OrbitalShuttle, to: ActorRef): Future[Any] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    TaskWorkflow.execute(OrbitalShuttlePadControl.registerShuttle(zone, newShuttle, to)).recover({
      case _: Exception =>
        if (!newShuttle.HasGUID) shuttleRegistration(zone, newShuttle, to)
    })
  }
}

object OrbitalShuttlePadControl {
  /**
    * Register the shuttle as a common vehicle in a zone.
    * @param zone the zone the shuttle and the pad will occupy
    * @param shuttle the vehicle that will be the shuttle
    * @param ref a reference to the control agency for the orbital shuttle pad
    * @return a `TaskBundle` object
    */
  def registerShuttle(zone: Zone, shuttle: Vehicle, ref: ActorRef): TaskBundle = {
    import scala.concurrent.ExecutionContext.Implicits.global
    TaskBundle(
      new StraightforwardTask() {
        private val localZone = zone
        private val localShuttle = shuttle
        private val localSelf = ref

        override def description(): String = s"register an orbital shuttle"

        def action() : Future[Any] = {
          localZone.Transport.tell(Zone.Vehicle.Spawn(localShuttle), localSelf)
          Future(this)
        }
      }, GUIDTask.registerVehicle(zone.GUID, shuttle)
    )
  }

  /**
    * Logic for door mechanism that allows the shuttle entryway to be opened.
    * Only opens for users with proper faction affinity.
    * @param obj what attempted to open the door
    * @param door the door
    * @return `true`, if the user is the accepted by the door;
    *        `false`, otherwise
    */
  def shuttleIsBoarding(obj: PlanetSideServerObject, door: Door): Boolean = {
    obj.Faction == door.Faction
  }

  /**
    * Logic for door mechanism that keeps select doors shut when the shuttle is not ready for boarding.
    * A message flashes onscreen to explain this reason.
    * The message will not flash if the door has no expectation of ever opening for a user.
    * @see `AvatarAction.SendResponse`
    * @see `AvatarServiceMessage`
    * @see `ChatMessageType`
    * @see `ChatMsg`
    * @see `Player`
    * @see `Service`
    * @see `Zone.AvatarEvents`
    * @param obj what attempted to open the door
    * @param door the door
    * @return `false`, as the door can not be opened in this state
    */
  def lockedWaitingForShuttle(obj: PlanetSideServerObject, door: Door): Boolean = {
    val zone = door.Zone
    obj match {
      case p: Player if p.Faction == door.Faction =>
        zone.AvatarEvents ! AvatarServiceMessage(
          p.Name,
          AvatarAction.SendResponse(
            ChatMsg(ChatMessageType.UNK_225, wideContents=false, "", "@DoorWillOpenWhenShuttleReturns", None)
          )
        )
        p.Name
      case _ => ;
    }
    false
  }
}
