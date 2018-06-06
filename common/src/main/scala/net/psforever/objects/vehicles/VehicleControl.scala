// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.Actor
import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.{Mountable, MountableBehavior}
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.deploy.DeploymentBehavior
import net.psforever.types.ExoSuitType

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
              case ExoSuitType.Reinforced => restriction != SeatArmorRestriction.NoReinforcedOrMax
              case _ => true
            })
        ) {
          mountBehavior.apply(Mountable.TryMount(user, seat_num))
        }
        else {
          sender ! Mountable.MountMessages(user, Mountable.CanNotMount(vehicle, seat_num))
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
