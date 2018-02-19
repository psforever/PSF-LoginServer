// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.Actor
import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.MountableBehavior
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
import net.psforever.objects.serverobject.deploy.DeploymentBehavior

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
    .orElse(mountBehavior)
    .orElse(dismountBehavior)
    .orElse {
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
