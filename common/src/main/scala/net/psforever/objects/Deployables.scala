// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.{Deployable, DeployedItem}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.packet.game.{DeployableInfo, DeploymentAction, PlanetSideGUID}
import services.RemoverActor
import services.local.{LocalAction, LocalServiceMessage}

import scala.concurrent.duration.FiniteDuration

object Deployables {
  object Make {
    def apply(item : DeployedItem.Value) : ()=>PlanetSideGameObject with Deployable = cemap(item)

    private val cemap : Map[DeployedItem.Value, ()=>PlanetSideGameObject with Deployable] = Map(
      DeployedItem.boomer -> { ()=> new BoomerDeployable(GlobalDefinitions.boomer) },
      DeployedItem.he_mine -> { ()=> new ExplosiveDeployable(GlobalDefinitions.he_mine) },
      DeployedItem.jammer_mine -> { ()=> new ExplosiveDeployable(GlobalDefinitions.jammer_mine) },
      DeployedItem.spitfire_turret -> { ()=> new TurretDeployable(GlobalDefinitions.spitfire_turret) },
      DeployedItem.spitfire_cloaked -> { ()=> new TurretDeployable(GlobalDefinitions.spitfire_cloaked) },
      DeployedItem.spitfire_aa -> { ()=> new TurretDeployable(GlobalDefinitions.spitfire_aa) },
      DeployedItem.motionalarmsensor -> { ()=> new SensorDeployable(GlobalDefinitions.motionalarmsensor) },
      DeployedItem.sensor_shield -> { ()=> new SensorDeployable(GlobalDefinitions.sensor_shield) },
      DeployedItem.tank_traps -> { ()=> new TrapDeployable(GlobalDefinitions.tank_traps) },
      DeployedItem.portable_manned_turret -> { ()=> new TurretDeployable(GlobalDefinitions.portable_manned_turret) },
      DeployedItem.portable_manned_turret -> { ()=> new TurretDeployable(GlobalDefinitions.portable_manned_turret) },
      DeployedItem.portable_manned_turret_nc -> { ()=> new TurretDeployable(GlobalDefinitions.portable_manned_turret_nc) },
      DeployedItem.portable_manned_turret_tr -> { ()=> new TurretDeployable(GlobalDefinitions.portable_manned_turret_tr) },
      DeployedItem.portable_manned_turret_vs -> { ()=> new TurretDeployable(GlobalDefinitions.portable_manned_turret_vs) },
      DeployedItem.deployable_shield_generator -> { ()=> new ShieldGeneratorDeployable(GlobalDefinitions.deployable_shield_generator) },
      DeployedItem.router_telepad_deployable -> { () => new TelepadDeployable(GlobalDefinitions.router_telepad_deployable) }
    ).withDefaultValue( { ()=> new ExplosiveDeployable(GlobalDefinitions.boomer) } )
  }

  /**
    * Distribute information that a deployable has been destroyed.
    * The deployable may not have yet been eliminated from the game world (client or server),
    * but its health is zero and it has entered the conditions where it is nearly irrelevant.<br>
    * <br>
    * The typical use case of this function involves destruction via weapon fire, attributed to a particular player.
    * Contrast this to simply destroying a deployable by being the deployable's owner and using the map icon controls.
    * This function eventually invokes the same routine
    * but mainly goes into effect when the deployable has been destroyed
    * and may still leave a physical component in the game world to be cleaned up later.
    * That is the task `EliminateDeployable` performs.
    * Additionally, since the player who destroyed the deployable isn't necessarily the owner,
    * and the real owner will still be aware of the existence of the deployable,
    * that player must be informed of the loss of the deployable directly.
    * @see `DeployableRemover`
    * @see `Vitality.DamageResolution`
    * @see `LocalResponse.EliminateDeployable`
    * @see `DeconstructDeployable`
    * @param target the deployable that is destroyed
    * @param time length of time that the deployable is allowed to exist in the game world;
    *             `None` indicates the normal un-owned existence time (180 seconds)
    */
  def AnnounceDestroyDeployable(target : PlanetSideServerObject with Deployable, time : Option[FiniteDuration]) : Unit = {
    val zone = target.Zone
    target.OwnerName match {
      case Some(owner) =>
        target.OwnerName = None
        zone.LocalEvents ! LocalServiceMessage(owner, LocalAction.AlertDestroyDeployable(PlanetSideGUID(0), target))
      case None => ;
    }
    zone.LocalEvents ! LocalServiceMessage(s"${target.Faction}", LocalAction.DeployableMapIcon(
      PlanetSideGUID(0),
      DeploymentAction.Dismiss,
      DeployableInfo(target.GUID, Deployable.Icon(target.Definition.Item), target.Position, PlanetSideGUID(0)))
    )
    zone.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.ClearSpecific(List(target), zone))
    zone.LocalEvents ! LocalServiceMessage.Deployables(RemoverActor.AddTask(target, zone, time))
  }
}
