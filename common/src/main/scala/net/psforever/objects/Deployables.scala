// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.{Deployable, DeployedItem}

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
}
