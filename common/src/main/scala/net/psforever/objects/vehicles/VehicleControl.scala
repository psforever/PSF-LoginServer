// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import akka.actor.Actor
import net.psforever.objects.Vehicle
import net.psforever.objects.serverobject.mount.MountableBehavior
<<<<<<< 27d86af015d5a835f7d594aed9ccdd1de4048c53
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}
=======
import net.psforever.objects.serverobject.affinity.FactionAffinityBehavior
import net.psforever.objects.serverobject.deploy.DeploymentBehavior
>>>>>>> Deployment:

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
<<<<<<< 27d86af015d5a835f7d594aed9ccdd1de4048c53
  //make control actors belonging to utilities when making control actor belonging to vehicle
  vehicle.Utilities.foreach({case (_, util) => util.Setup })

  def MountableObject = vehicle //do not add type!
=======
  def MountableObject = vehicle
>>>>>>> Deployment:

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
