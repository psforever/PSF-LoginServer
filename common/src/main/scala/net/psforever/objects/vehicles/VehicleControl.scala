// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.Actor
import net.psforever.objects.Vehicle
import net.psforever.objects.ballistics.VehicleSource
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.deploy.DeploymentBehavior
import net.psforever.objects.vital.{VehicleShieldCharge, Vitality}
import net.psforever.types.ExoSuitType
import org.slf4j.Logger

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
  with MountableBehavior.Dismount {
  //make control actors belonging to utilities when making control actor belonging to vehicle
  vehicle.Utilities.foreach({case (_, util) => util.Setup })

  def MountableObject = vehicle

  def FactionObject = vehicle

  def DeploymentObject = vehicle

  def receive : Receive = Enabled

  def Enabled : Receive = checkBehavior
    .orElse(deployBehavior)
    .orElse(dismountBehavior)
    .orElse {
      case Mountable.TryMount(user, seat_num) =>
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
          mountBehavior.apply(Mountable.TryMount(user, seat_num))
        }
        else {
          sender ! Mountable.MountMessages(user, Mountable.CanNotMount(vehicle, seat_num))
        }

      case Vitality.Damage(damage_func) =>
        if(vehicle.Health > 0) {
          val originalHealth = vehicle.Health
          val originalShields = vehicle.Shields
          damage_func(vehicle)
          val health = vehicle.Health
          val shields = vehicle.Shields
          val damageToHealth = originalHealth - health
          val damageToShields = originalShields - shields
          org.log4s.getLogger("DAMAGE_RESOLUTION").info(s"BEFORE: $originalHealth/$originalShields, AFTER: $health/$shields, CHANGE: $damageToHealth/$damageToShields")
          sender ! Vitality.DamageResolution(vehicle)
        }

      case Vehicle.ChargeShields(amount) =>
        val now : Long = System.nanoTime
        //make certain vehicle doesn't charge shields too quickly
        if(vehicle.Health > 0 && vehicle.Shields < vehicle.MaxShields &&
          !vehicle.History.exists(VehicleControl.LastShieldChargeOrDamage(now))) {
          vehicle.History(VehicleShieldCharge(VehicleSource(vehicle), amount))
          vehicle.Shields = vehicle.Shields + amount
          sender ! Vehicle.UpdateShieldsCharge(vehicle)
        }

      case FactionAffinity.ConvertFactionAffinity(faction) =>
        val originalAffinity = vehicle.Faction
        if(originalAffinity != (vehicle.Faction = faction)) {
          vehicle.Utilities.foreach({ case(_ : Int, util : Utility) => util().Actor forward FactionAffinity.ConfirmFactionAffinity() })
        }
        sender ! FactionAffinity.AssertFactionAffinity(vehicle, faction)

      case Vehicle.PrepareForDeletion =>
        context.become(Disabled)

      case _ => ;
    }

  def Disabled : Receive = checkBehavior
    .orElse(dismountBehavior)
    .orElse {
      case Vehicle.Reactivate =>
        context.become(Enabled)

      case _ => ;
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
