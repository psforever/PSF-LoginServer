// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.{Actor, ActorRef}
import net.psforever.objects.{GlobalDefinitions, SimpleItem, Vehicle, Vehicles}
import net.psforever.objects.ballistics.{ResolvedProjectile, VehicleSource}
import net.psforever.objects.equipment.JammableMountedWeapons
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.damage.DamageableVehicle
import net.psforever.objects.serverobject.deploy.DeploymentBehavior
import net.psforever.objects.serverobject.hackable.GenericHackables
import net.psforever.objects.serverobject.repair.RepairableVehicle
import net.psforever.objects.vital.VehicleShieldCharge
import net.psforever.types.{ExoSuitType, PlanetSideGUID}
import services.vehicle.{VehicleAction, VehicleServiceMessage}

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
  with JammableMountedWeapons {

  //make control actors belonging to utilities when making control actor belonging to vehicle
  vehicle.Utilities.foreach({case (_, util) => util.Setup })

  def MountableObject = vehicle
  def CargoObject = vehicle
  def JammableObject = vehicle
  def FactionObject = vehicle
  def DeploymentObject = vehicle
  def DamageableObject = vehicle
  def RepairableObject = vehicle

  def receive : Receive = Enabled

  override def postStop() : Unit = {
    super.postStop()
    vehicle.Utilities.values.foreach { util =>
      context.stop(util().Actor)
      util().Actor = ActorRef.noSender
    }
  }

  def Enabled : Receive = checkBehavior
    .orElse(deployBehavior)
    .orElse(cargoBehavior)
    .orElse(jammableBehavior)
    .orElse(takesDamage)
    .orElse(canBeRepairedByNanoDispenser)
    .orElse {
      case msg : Mountable.TryMount =>
        tryMountBehavior.apply(msg)

      case msg : Mountable.TryDismount =>
        dismountBehavior.apply(msg)

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

      case Vehicle.PrepareForDeletion() =>
        CancelJammeredSound(vehicle)
        CancelJammeredStatus(vehicle)
        context.become(Disabled)

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

  def Disabled : Receive = checkBehavior
    .orElse {
      case msg : Mountable.TryDismount =>
        dismountBehavior.apply(msg)

      case Vehicle.Reactivate() =>
        context.become(Enabled)

      case _ =>
    }

  override def TryJammerEffectActivate(target : Any, cause : ResolvedProjectile) : Unit = {
    if(vehicle.MountedIn.isEmpty) {
      super.TryJammerEffectActivate(target, cause)
    }
  }
}

object VehicleControl {
  import net.psforever.objects.vital.{DamageFromProjectile, VehicleShieldCharge, VitalsActivity}
  import scala.concurrent.duration._

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
