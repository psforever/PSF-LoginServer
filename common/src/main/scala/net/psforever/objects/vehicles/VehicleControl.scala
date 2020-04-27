// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.{Actor, ActorRef, Cancellable}
import net.psforever.objects._
import net.psforever.objects.ballistics.{ResolvedProjectile, VehicleSource}
import net.psforever.objects.equipment.{Equipment, JammableMountedWeapons}
import net.psforever.objects.serverobject.{CommonMessages, Containable}
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.damage.DamageableVehicle
import net.psforever.objects.serverobject.deploy.DeploymentBehavior
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.repair.RepairableVehicle
import net.psforever.objects.vital.VehicleShieldCharge
import net.psforever.objects.zones.Zone
import net.psforever.types.{DriveState, ExoSuitType, PlanetSideGUID, Vector3}
import services.{RemoverActor, Service}
import net.psforever.packet.game.{ObjectAttachMessage, ObjectCreateDetailedMessage, ObjectDetachMessage}
import net.psforever.packet.game.objectcreate.ObjectCreateMessageParent
import net.psforever.types.{ExoSuitType, PlanetSideEmpire, PlanetSideGUID, Vector3}
import services.Service
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * An `Actor` that handles messages being dispatched to a specific `Vehicle`.<br>
  * <br>
  * Vehicle-controlling actors have two behavioral states - responsive and "`Disabled`."
  * The latter is applicable only when the specific vehicle is being deconstructed.
  * @param vehicle the `Vehicle` object being governed
  */
class VehicleControl(vehicle : Vehicle) extends Actor
  with FactionAffinityBehavior.Check
  with DeploymentBehavior
  with MountableBehavior.Mount
  with MountableBehavior.Dismount
  with CargoBehavior
  with DamageableVehicle
  with RepairableVehicle
  with JammableMountedWeapons
  with Containable {

  //make control actors belonging to utilities when making control actor belonging to vehicle
  vehicle.Utilities.foreach({case (_, util) => util.Setup })

  def MountableObject = vehicle
  def CargoObject = vehicle
  def JammableObject = vehicle
  def FactionObject = vehicle
  def DeploymentObject = vehicle
  def DamageableObject = vehicle
  def RepairableObject = vehicle
  def ContainerObject = vehicle

  /** cheap flag for whether the vehicle is decaying */
  var decaying : Boolean = false
  /** primary vehicle decay timer */
  var decayTimer : Cancellable = Default.Cancellable

  def receive : Receive = Enabled

  override def postStop() : Unit = {
    super.postStop()
    decaying = false
    decayTimer.cancel
    vehicle.Utilities.values.foreach { util =>
      context.stop(util().Actor)
      util().Actor = Default.Actor
    }
  }

  def Enabled : Receive = checkBehavior
    .orElse(deployBehavior)
    .orElse(cargoBehavior)
    .orElse(jammableBehavior)
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse(containerBehavior)
    .orElse {
      case Vehicle.Ownership(None) =>
        LoseOwnership()

      case Vehicle.Ownership(Some(player)) =>
        GainOwnership(player)

      case msg @ Mountable.TryMount(player, seat_num) =>
        tryMountBehavior.apply(msg)
        val obj = MountableObject
        //check that the player has actually been sat in the expected seat
        if(obj.PassengerInSeat(player).contains(seat_num)) {
          //if the driver seat, change ownership
          if(seat_num == 0 && !obj.OwnerName.contains(player.Name)) {
            //whatever vehicle was previously owned
            vehicle.Zone.GUID(player.VehicleOwned) match {
              case Some(v : Vehicle) =>
                v.Actor ! Vehicle.Ownership(None)
              case _ =>
                player.VehicleOwned = None
            }
            LoseOwnership() //lose our current ownership
            GainOwnership(player) //gain new ownership
          }
          else {
            decaying = false
            decayTimer.cancel
          }
        }

      case msg : Mountable.TryDismount =>
        dismountBehavior.apply(msg)
        val obj = MountableObject

        // Reset velocity to zero when driver dismounts, to allow jacking/repair if vehicle was moving slightly before dismount
        if(!obj.Seats(0).isOccupied) {
          obj.Velocity = Some(Vector3.Zero)
        }
        //are we already decaying? are we unowned? is no one seated anywhere?
        if(!decaying && obj.Owner.isEmpty && obj.Seats.values.forall(!_.isOccupied)) {
          decaying = true
          decayTimer = context.system.scheduler.scheduleOnce(MountableObject.Definition.DeconstructionTime.getOrElse(5 minutes), self, VehicleControl.PrepareForDeletion())
        }

      case Vehicle.ChargeShields(amount) =>
        val now : Long = System.nanoTime
        //make certain vehicle doesn't charge shields too quickly
        if(vehicle.Health > 0 && vehicle.Shields < vehicle.MaxShields &&
          !vehicle.History.exists(VehicleControl.LastShieldChargeOrDamage(now))) {
          vehicle.History(VehicleShieldCharge(VehicleSource(vehicle), amount))
          vehicle.Shields = vehicle.Shields + amount
          vehicle.Zone.VehicleEvents ! VehicleServiceMessage(s"${vehicle.Actor}", VehicleAction.PlanetsideAttribute(PlanetSideGUID(0), vehicle.GUID, 68, vehicle.Shields))
        }

      case FactionAffinity.ConvertFactionAffinity(faction) =>
        val originalAffinity = vehicle.Faction
        if(originalAffinity != (vehicle.Faction = faction)) {
          vehicle.Utilities.foreach({ case(_ : Int, util : Utility) => util().Actor forward FactionAffinity.ConfirmFactionAffinity() })
        }
        sender ! FactionAffinity.AssertFactionAffinity(vehicle, faction)

      case CommonMessages.Use(player, Some(item : SimpleItem)) if item.Definition == GlobalDefinitions.remote_electronics_kit =>
        //TODO setup certifications check
        if(vehicle.Faction != player.Faction) {
          sender ! CommonMessages.Progress(
            GenericHackables.GetHackSpeed(player, vehicle),
            Vehicles.FinishHackingVehicle(vehicle, player,3212836864L),
            GenericHackables.HackingTickAction(progressType = 1, player, vehicle, item.GUID)
          )
        }

      case Vehicle.Deconstruct(time) =>
        time match {
          case Some(delay) =>
            decaying = true
            decayTimer.cancel
            decayTimer = context.system.scheduler.scheduleOnce(delay, self, VehicleControl.PrepareForDeletion())
          case _ =>
            PrepareForDeletion()
        }

      case VehicleControl.PrepareForDeletion() =>
        PrepareForDeletion()

      case _ => ;
    }

  val tryMountBehavior : Receive = {
    case msg @ Mountable.TryMount(user, seat_num) =>
      val exosuit = user.ExoSuit
      val restriction = vehicle.Seats(seat_num).ArmorRestriction
      val seatGroup = vehicle.SeatPermissionGroup(seat_num).getOrElse(AccessPermissionGroup.Passenger)
      val permission = vehicle.PermissionGroup(seatGroup.id).getOrElse(VehicleLockState.Empire)
      if(
        (if(seatGroup == AccessPermissionGroup.Driver) {
          vehicle.Owner.contains(user.GUID) || vehicle.Owner.isEmpty || permission != VehicleLockState.Locked
        }
        else {
          permission != VehicleLockState.Locked
        }) &&
          (exosuit match {
            case ExoSuitType.MAX => restriction == SeatArmorRestriction.MaxOnly
            case ExoSuitType.Reinforced => restriction == SeatArmorRestriction.NoMax
            case _ => restriction != SeatArmorRestriction.MaxOnly
          })
      ) {
        mountBehavior.apply(msg)
      }
      else {
        sender ! Mountable.MountMessages(user, Mountable.CanNotMount(vehicle, seat_num))
      }
  }

  def PrepareForDeletion() : Unit = {
    decaying = false
    val guid = vehicle.GUID
    val zone = vehicle.Zone
    val zoneId = zone.Id
    val events = zone.VehicleEvents
    //become disabled
    context.become(Disabled)
    //cancel jammed behavior
    CancelJammeredSound(vehicle)
    CancelJammeredStatus(vehicle)
    //escape being someone else's cargo
    vehicle.MountedIn match {
      case Some(_) =>
        CargoBehavior.HandleVehicleCargoDismount(zone, guid, bailed = false, requestedByPassenger = false, kicked = false)
      case _ => ;
    }
    //kick all passengers
    vehicle.Seats.values.foreach(seat => {
      seat.Occupant match {
        case Some(player) =>
          seat.Occupant = None
          player.VehicleSeated = None
          if(player.HasGUID) {
            events ! VehicleServiceMessage(zoneId, VehicleAction.KickPassenger(player.GUID, 4, false, guid))
          }
        case None => ;
      }
      //abandon all cargo
      vehicle.CargoHolds.values
        .collect { case hold if hold.isOccupied =>
          val cargo = hold.Occupant.get
          CargoBehavior.HandleVehicleCargoDismount(cargo.GUID, cargo, guid, vehicle, bailed = false, requestedByPassenger = false, kicked = false)
        }
    })
    //unregister
    events ! VehicleServiceMessage.Decon(RemoverActor.AddTask(vehicle, zone, Some(0 seconds)))
    //banished to the shadow realm
    vehicle.Position = Vector3.Zero
    vehicle.DeploymentState = DriveState.Mobile
    //queue final deletion
    decayTimer = context.system.scheduler.scheduleOnce(5 seconds, self, VehicleControl.Deletion())
  }

  def Disabled : Receive = checkBehavior
    .orElse {
      case VehicleControl.Deletion() =>
        val zone = vehicle.Zone
        zone.VehicleEvents ! VehicleServiceMessage(zone.Id, VehicleAction.UnloadVehicle(Service.defaultPlayerGUID, zone, vehicle, vehicle.GUID))
        zone.Transport ! Zone.Vehicle.Despawn(vehicle)
      case _ =>
    }

  override def TryJammerEffectActivate(target : Any, cause : ResolvedProjectile) : Unit = {
    if(vehicle.MountedIn.isEmpty) {
      super.TryJammerEffectActivate(target, cause)
    }
  }

  def LoseOwnership() : Unit = {
    val obj = MountableObject
    Vehicles.Disown(obj.GUID, obj)
    if(!decaying && obj.Seats.values.forall(!_.isOccupied)) {
      decaying = true
      decayTimer = context.system.scheduler.scheduleOnce(obj.Definition.DeconstructionTime.getOrElse(5 minutes), self, VehicleControl.PrepareForDeletion())
    }
  }

  def GainOwnership(player : Player) : Unit = {
    Vehicles.Own(MountableObject, player) match {
      case Some(_) =>
        decaying = false
        decayTimer.cancel
      case None => ;
    }

  def MessageDeferredCallback(msg : Any) : Unit = {
    msg match {
      case Containable.MoveItem(_, item, _) =>
        //momentarily put item back where it was originally
        val obj = ContainerObject
        obj.Find(item) match {
          case Some(slot) =>
            obj.Zone.AvatarEvents ! AvatarServiceMessage(
              self.toString,
              AvatarAction.SendResponse(Service.defaultPlayerGUID, ObjectAttachMessage(obj.GUID, item.GUID, slot))
            )
          case None => ;
        }
      case _ => ;
    }
  }

  def RemoveItemFromSlotCallback(item : Equipment, slot : Int) : Unit = {
    val zone = ContainerObject.Zone
    zone.VehicleEvents ! VehicleServiceMessage(self.toString, VehicleAction.UnstowEquipment(Service.defaultPlayerGUID, item.GUID))
  }

  def PutItemInSlotCallback(item : Equipment, slot : Int) : Unit = {
    val obj = ContainerObject
    val guid = obj.GUID
    val zone = obj.Zone
    val events = zone.VehicleEvents
    val definition = item.Definition
    item.Faction = PlanetSideEmpire.NEUTRAL
    if(obj.VisibleSlots.contains(slot)) {
      events ! VehicleServiceMessage(zone.Id, VehicleAction.StowEquipment(guid, guid, slot, item))
    }
    events ! VehicleServiceMessage(
      self.toString,
      VehicleAction.SendResponse(
        Service.defaultPlayerGUID,
        ObjectCreateDetailedMessage(
          definition.ObjectId,
          item.GUID,
          ObjectCreateMessageParent(obj.GUID, slot),
          definition.Packet.DetailedConstructorData(item).get
        )
      )
    )
  }

  def SwapItemCallback(item : Equipment) : Unit = {
    val obj = ContainerObject
    val zone = obj.Zone
    zone.VehicleEvents ! VehicleServiceMessage(self.toString, VehicleAction.SendResponse(Service.defaultPlayerGUID, ObjectDetachMessage(obj.GUID, item.GUID, Vector3.Zero, 0f)))
  }
}

object VehicleControl {
  import net.psforever.objects.vital.{DamageFromProjectile, VehicleShieldCharge, VitalsActivity}
  import scala.concurrent.duration._

  private case class PrepareForDeletion()

  private case class Deletion()

  /**
    * Determine if a given activity entry would invalidate the act of charging vehicle shields this tick.
    * @param now the current time (in nanoseconds)
    * @param act a `VitalsActivity` entry to test
    * @return `true`, if the vehicle took damage in the last five seconds or
    *        charged shields in the last second;
    *        `false`, otherwise
    */
  def LastShieldChargeOrDamage(now : Long)(act : VitalsActivity) : Boolean = {
    act match {
      case DamageFromProjectile(data) => now - data.hit_time < (5 seconds).toNanos //damage delays next charge by 5s
      case vsc : VehicleShieldCharge => now - vsc.time < (1 seconds).toNanos //previous charge delays next by 1s
      case _ => false
    }
  }
}
