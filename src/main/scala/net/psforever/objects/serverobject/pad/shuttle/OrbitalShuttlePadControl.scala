// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.pad.shuttle

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.guid.{GUIDTask, Task, TaskResolver}
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.vehicles.AccessPermissionGroup
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.ChatMsg
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
import net.psforever.services.local.{LocalAction, LocalServiceMessage}
import net.psforever.services.hart.HartTimer
import net.psforever.services.{Service, ServiceManager}
import net.psforever.types.{ChatMessageType, Vector3}

import scala.util.Success

/**
  * An `Actor` that handles messages being dispatched to a specific `OrbitalShuttlePad`.<br>
  * <br>
  * For the purposes of maintaining a close relationship
  * with the rest of the high altitude rapid transport (HART) system's components,
  * this control agency also locally creates the vehicle that will the shuttle when ti starts up.
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
    case HartTimer.LockDoors =>
      managedDoors.foreach { door =>
        door.Actor ! Door.UpdateMechanism(OrbitalShuttlePadControl.lockedWaitingForShuttle)
        val zone = pad.Zone
        if(door.isOpen) {
          zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.DoorSlamsShut(door))
        }
      }

    case HartTimer.UnlockDoors =>
      managedDoors.foreach { _.Actor ! Door.ResetMechanism }

    case HartTimer.ShuttleDocked =>
      val zone = pad.Zone
      shuttle.MountedIn = pad.GUID
      zone.LocalEvents ! LocalServiceMessage(zone.id, LocalAction.ShuttleDock(pad.GUID, shuttle.GUID, 3))

    case HartTimer.ShuttleFreeFromDock =>
      val zone = pad.Zone
      shuttle.MountedIn = None
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.ShuttleUndock(pad.GUID, shuttle.GUID, shuttle.Position, shuttle.Orientation)
      )

    case HartTimer.ShuttleStateUpdate(state) =>
      val zone = pad.Zone
      zone.LocalEvents ! LocalServiceMessage(
        zone.id,
        LocalAction.ShuttleState(shuttle.GUID, shuttle.Position, shuttle.Orientation, state)
      )

    case _ => ;
  }

  /** wire the pad and shuttle into a zone-scoped service handler */
  val shuttleTime: Receive = {
    case Zone.Vehicle.HasSpawned(_, newShuttle) =>
      shuttle = newShuttle
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
      val position = pad.Position
      val zone = pad.Zone
      //collect managed doors
      managedDoors = pad.Owner.Amenities
        .collect { case d: Door if d.Definition == GlobalDefinitions.gr_door_mb_orb => d }
        .sortBy { o => Vector3.DistanceSquared(position, o.Position) }
        .take(8)
      //create shuttle
      val newShuttle = new Vehicle(GlobalDefinitions.orbital_shuttle) {
        Position = position + Vector3(0, 8.25f, 0) //magic offset
        Orientation = pad.Orientation
        Faction = pad.Faction
        override def SeatPermissionGroup(seatNumber : Int) : Option[AccessPermissionGroup.Value] = {
          Seat(seatNumber) match {
            case Some(_) => Some(AccessPermissionGroup.Passenger)
            case _       => None
          }
        }
      }
      //register and add shuttle
      zone.tasks ! OrbitalShuttlePadControl.registerShuttle(zone, newShuttle, self)
      //progress ...
      context.become(shuttleTime)

    case _ => ;
  }
}

object OrbitalShuttlePadControl {
  /**
    * Register the shuttle as a common vehicle in a zone.
    * @param zone the zone the shuttle and the pad will occupy
    * @param shuttle the vehicle that will be the shuttle
    * @param ref a reference to the control agency for the orbital shuttle pad
    * @return a `TaskResolver.GiveTask` object
    */
  def registerShuttle(zone: Zone, shuttle: Vehicle, ref: ActorRef): TaskResolver.GiveTask = {
    TaskResolver.GiveTask(
      new Task() {
        private val localZone = zone
        private val localShuttle = shuttle
        private val localSelf = ref

        override def Description: String = s"register an orbital shuttle"

        override def isComplete : Task.Resolution.Value = if (localShuttle.HasGUID) {
          Task.Resolution.Success
        } else {
          Task.Resolution.Incomplete
        }

        def Execute(resolver : ActorRef) : Unit = {
          localZone.Transport.tell(Zone.Vehicle.Spawn(localShuttle), localSelf)
          resolver ! Success(true)
        }

        override def onFailure(ex : Throwable) : Unit = {
          super.onFailure(ex)
          localSelf ! Zone.Vehicle.CanNotSpawn(localZone, localShuttle, ex.getMessage)
        }
      }, List(GUIDTask.RegisterVehicle(shuttle)(zone.GUID))
    )
  }

  /**
    * Logic for door mechanism that keeps select doors shut when the shuttle is not ready for boarding.
    * A message flashes onscreen to explain this reason.
    * @see `AvatarAction>SendResponse`
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
    val channel = obj match {
      case p: Player => p.Name
      case _ => ""
    }
    zone.AvatarEvents ! AvatarServiceMessage(
      channel,
      AvatarAction.SendResponse(
        Service.defaultPlayerGUID,
        ChatMsg(ChatMessageType.UNK_225, false, "", "@DoorWillOpenWhenShuttleReturns", None)
      )
    )
    false
  }
}
