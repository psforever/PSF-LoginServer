// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
import net.psforever.objects.avatar.DeployableToolbox
import net.psforever.objects.ce.{DeployableCategory, DeployedItem}
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable.Specification
import net.psforever.objects.avatar.Certification._

class DeployableToolboxTest extends Specification {
  "DeployableToolbbox" should {
    "construct" in {
      new DeployableToolbox //should just construct without issue
      ok
    }

    "initialization (default)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set())
      val list = obj.UpdateUI()
      list.size mustEqual DeployedItem.values.size - 3 //extra field turrets
      val (routers, allOthers) = list.partition({ case ((_, _, _, max)) => max == 1024 })
      allOthers.foreach({
        case (_, curr, _, max) =>
          curr mustEqual 0
          max mustEqual 0
      })
      routers.length mustEqual 1
      ok
    }

    "initialization (CombatEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 10
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "initialization (AssaultEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AssaultEngineering))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 10
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "initialization (FortificationEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, FortificationEngineering))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "initialization (AdvancedEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(AdvancedEngineering))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "initialization (AdvancedHacking)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 10
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "initialization (without CombatEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(AssaultEngineering, FortificationEngineering, AdvancedHacking))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "initialization (GroundSupport)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(GroundSupport))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "can not initialize twice" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set()) mustEqual true
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.Initialize(Set(AdvancedEngineering)) mustEqual false
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "uninitialized fields can not accept deployables" in {
      val obj = new DeployableToolbox
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Accept(boomer) mustEqual false
      obj.Add(boomer) mustEqual false
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
    }

    "only initialized fields can accept deployables" in {
      val obj = new DeployableToolbox
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      obj.Initialize(Set(CombatEngineering))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Accept(boomer) mustEqual true
      obj.Add(boomer) mustEqual true
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(1, 20)
    }

    "change accessible fields by adding by certification type (CombatEngineering ...)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set())
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 10
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering, FortificationEngineering))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering, FortificationEngineering, AssaultEngineering))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering, FortificationEngineering, AssaultEngineering))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering, FortificationEngineering, AssaultEngineering, AdvancedHacking))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "change accessible fields by adding by certification type (GroundSupport)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set())
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(GroundSupport))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "change accessible fields by adding by certification type (AdvancedEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 10
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering, AdvancedEngineering))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "change accessible fields by removing by certification types (all)" in {
      val obj = new DeployableToolbox
      obj.Initialize(
        Set(CombatEngineering, AssaultEngineering, FortificationEngineering, AdvancedHacking, GroundSupport)
      )
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering, AssaultEngineering, FortificationEngineering, AdvancedHacking))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering, AssaultEngineering, FortificationEngineering))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering, AssaultEngineering))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 10
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 10
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set())

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "change accessible fields by removing by certification type (AdvancedEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedEngineering))
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 15
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 25
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 5
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 1
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024

      obj.UpdateMaxCounts(Set(CombatEngineering))

      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.spitfire_turret)._2 mustEqual 10
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.jammer_mine)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_cloaked)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.spitfire_aa)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.tank_traps)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_nc)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_tr)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.portable_manned_turret_vs)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.deployable_shield_generator)._2 mustEqual 0
      obj.CountDeployable(DeployedItem.router_telepad_deployable)._2 mustEqual 1024
    }

    "can not remove deployables from an unpopulated field" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.Remove(boomer) mustEqual false
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
    }

    "can remove a deployable from a field it populates" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.Add(boomer) mustEqual true
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(1, 20)
      obj.Remove(boomer) mustEqual true
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
    }

    "can not remove a deployable from a field it does not populate" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      val boomer1 = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.CountDeployable(DeployedItem.boomer)._2 mustEqual 20
      obj.Add(boomer1) mustEqual true
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(1, 20)
      val boomer2 = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Remove(boomer2) mustEqual false
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(1, 20)
    }

    "changing accessible fields by removing by certification type does not invalidate existing deployables" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      obj.Add(new BoomerDeployable(GlobalDefinitions.boomer))
      obj.Add(new BoomerDeployable(GlobalDefinitions.boomer))
      obj.Add(new BoomerDeployable(GlobalDefinitions.boomer))
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(3, 20)

      obj.UpdateMaxCounts(Set())

      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(3, 0)
    }

    "can not add the same deployable multiple times" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Add(boomer) mustEqual true
      obj.Add(boomer) mustEqual false
    }

    "can not add more deployables to a field than is allowed by its current type maximum" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      obj.CountDeployable(DeployedItem.he_mine)._2 mustEqual 20
      (1 to 19).foreach(_ => {
        val o1 = new ExplosiveDeployable(GlobalDefinitions.he_mine)
        obj.Accept(o1) mustEqual true
        obj.Add(o1) mustEqual true
      })
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(19, 20)
      val o2 = new BoomerDeployable(GlobalDefinitions.he_mine)
      obj.Accept(o2) mustEqual true
      obj.Add(o2) mustEqual true
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(20, 20)
      //fail extra
      val o3 = new BoomerDeployable(GlobalDefinitions.he_mine)
      obj.Accept(o3) mustEqual false
      obj.Add(o3) mustEqual false
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(20, 20)
      //remove old
      obj.Remove(o2) mustEqual true
      obj.Accept(o2) mustEqual true
      obj.Accept(o3) mustEqual true
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(19, 20)
      //add extra
      obj.Add(o3) mustEqual true
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(20, 20)
    }

    "some deployables can share a category and a category maximum" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 20
      (1 to 10).foreach(_ => {
        val o = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
        obj.Accept(o) mustEqual true
        obj.Add(o) mustEqual true
      })
      (1 to 10).foreach(_ => {
        val o = new SensorDeployable(GlobalDefinitions.sensor_shield)
        obj.Accept(o) mustEqual true
        obj.Add(o) mustEqual true
      })
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(10, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(10, 20)

      val o1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      obj.Accept(o1) mustEqual false
      obj.Add(o1) mustEqual false
      val o2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      obj.Accept(o2) mustEqual false
      obj.Add(o2) mustEqual false
    }

    "remove the first deployable type from a category" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 20
      val o1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      obj.Accept(o1) mustEqual true
      obj.Add(o1) mustEqual true
      val o2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      obj.Accept(o2) mustEqual true
      obj.Add(o2) mustEqual true
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(1, 20)

      val o3          = new SensorDeployable(GlobalDefinitions.sensor_shield)
      val displaced1a = obj.DisplaceFirst(o3) //remove the first sensor_shield's deployable
      displaced1a.nonEmpty mustEqual true
      displaced1a.get.Definition == o3.Definition mustEqual true
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(0, 20)
      //test: add o2 again and try to remove the motionalarmsensor
      obj.Add(o2)
      val displaced1b =
        obj.DisplaceFirst(
          o3,
          { (d) => d.Definition.Item != DeployedItem.sensor_shield }
        ) //remove the first sensor_shield's deployable
      displaced1b.nonEmpty mustEqual true
      displaced1b.get.Definition == o3.Definition mustEqual false
      displaced1b.get.Definition mustEqual GlobalDefinitions.motionalarmsensor
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(0, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(1, 20)

      val o4         = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val displaced2 = obj.DisplaceFirst(o3) //remove the first deployable of motionalarmsensor's category
      displaced2.nonEmpty mustEqual true
      displaced2.get.Definition == o4.Definition mustEqual false
      displaced2.get.Definition mustEqual GlobalDefinitions.sensor_shield //removed deployable is a sensor_shield type
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 20

      obj.DisplaceFirst(o3) mustEqual None //we can not remove anymore deployables because the category is empty
      obj.DisplaceFirst(o4) mustEqual None //likewise
    }

    "remove the first deployable type from a category" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      val o1 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      val o2 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val o3 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      obj.Add(o3) mustEqual true //0
      obj.Add(o1) mustEqual true //1
      obj.Add(o2) mustEqual true //2

      val displaced1 = obj.DisplaceFirst(DeployableCategory.Sensors)
      displaced1.nonEmpty mustEqual true
      displaced1.get.Definition == GlobalDefinitions.sensor_shield mustEqual true
      displaced1.get == o3 mustEqual true

      val displaced2 = obj.DisplaceFirst(DeployableCategory.Sensors)
      displaced2.nonEmpty mustEqual true
      displaced2.get.Definition == GlobalDefinitions.sensor_shield mustEqual true
      displaced2.get == o1 mustEqual true
    }

    "counting deployables will give the type number; counting categories gives all deployables in that category" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.Add(new SensorDeployable(GlobalDefinitions.sensor_shield))
      obj.Add(new SensorDeployable(GlobalDefinitions.motionalarmsensor))
      obj.Add(new SensorDeployable(GlobalDefinitions.sensor_shield))
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(2, 20)
      obj.CountCategory(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(3, 20)
      obj.CountCategory(DeployedItem.sensor_shield).productIterator.toList mustEqual List(3, 20)
    }

    "get a list of GUIDs when the category contents are tested" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 20
      val o1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      o1.GUID = PlanetSideGUID(1)
      obj.Add(o1) mustEqual true
      val o3 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      o3.GUID = PlanetSideGUID(3)
      obj.Add(o3) mustEqual true
      val o4 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      o4.GUID = PlanetSideGUID(4)
      obj.Add(o4) mustEqual true
      val o2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      o2.GUID = PlanetSideGUID(2)
      obj.Add(o2) mustEqual true
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(2, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(2, 20)

      val test1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val test2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      obj.Category(test1) mustEqual List(PlanetSideGUID(1), PlanetSideGUID(3), PlanetSideGUID(4), PlanetSideGUID(2))
      obj.Category(test1) mustEqual obj.Category(test2)
    }

    "get a list of GUIDs when the types are tested" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.CountDeployable(DeployedItem.motionalarmsensor)._2 mustEqual 20
      obj.CountDeployable(DeployedItem.sensor_shield)._2 mustEqual 20
      val o1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      o1.GUID = PlanetSideGUID(1)
      obj.Add(o1) mustEqual true
      val o3 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      o3.GUID = PlanetSideGUID(3)
      obj.Add(o3) mustEqual true
      val o4 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      o4.GUID = PlanetSideGUID(4)
      obj.Add(o4) mustEqual true
      val o2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      o2.GUID = PlanetSideGUID(2)
      obj.Add(o2) mustEqual true
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(2, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(2, 20)

      val test1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val test2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      obj.Deployables(test1) mustEqual List(PlanetSideGUID(1), PlanetSideGUID(4))
      obj.Deployables(test2) mustEqual List(PlanetSideGUID(3), PlanetSideGUID(2))
    }

    "three tests: 'contains' detects same deployable" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))

      val boomer1 = new BoomerDeployable(GlobalDefinitions.boomer)
      val boomer2 = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Contains(boomer1) mustEqual false
      obj.Contains(boomer2) mustEqual false
      obj.Add(boomer1)
      obj.Contains(boomer1) mustEqual true
      obj.Contains(boomer2) mustEqual false
    }

    "three tests: 'valid' tests whether deployable type can be accepted" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))

      val cerebus = new TurretDeployable(GlobalDefinitions.spitfire_aa) //cerebus turret
      obj.Valid(cerebus) mustEqual false
      obj.CountDeployable(DeployedItem.spitfire_aa).productIterator.toList mustEqual List(0, 0)

      obj.UpdateMaxCounts(Set(CombatEngineering, AdvancedEngineering))

      obj.Valid(cerebus) mustEqual true
      obj.CountDeployable(DeployedItem.spitfire_aa).productIterator.toList mustEqual List(0, 5)
    }

    "three tests: 'available' tests whether there is enough space to add more deployables of a type" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(0, 20)

      (1 to 20).foreach(_ => {
        val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
        obj.Available(boomer) mustEqual true
        obj.Add(boomer) mustEqual true
      })
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(20, 20)
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Available(boomer) mustEqual false
      obj.Add(boomer) mustEqual false
    }

    "three tests: 'accept' ensures that all three of the previous tests are passable" in {
      val obj    = new DeployableToolbox
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)

      obj.Initialize(Set())
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(0, 0)
      obj.Accept(boomer) mustEqual false
      obj.Available(boomer) mustEqual false
      obj.Contains(boomer) mustEqual false //false is being passable
      obj.Valid(boomer) mustEqual false

      obj.UpdateMaxCounts(Set(CombatEngineering))

      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(0, 20)
      obj.Accept(boomer) mustEqual true
      obj.Available(boomer) mustEqual true //true is being passable
      obj.Contains(boomer) mustEqual false //false is being passable
      obj.Valid(boomer) mustEqual true     //true is being passable

      obj.Add(boomer)
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(1, 20)
      obj.Accept(boomer) mustEqual false
      obj.Available(boomer) mustEqual true //true is being passable
      obj.Contains(boomer) mustEqual true
      obj.Valid(boomer) mustEqual true //true is being passable

      (1 to 20).foreach(_ => { obj.Add(new BoomerDeployable(GlobalDefinitions.boomer)) })
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(20, 20)
      obj.Accept(boomer) mustEqual false
      obj.Available(boomer) mustEqual false
      obj.Contains(boomer) mustEqual true
      obj.Valid(boomer) mustEqual true //true is being passable

      obj.UpdateMaxCounts(Set())

      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(20, 0)
      obj.Accept(boomer) mustEqual false
      obj.Available(boomer) mustEqual false
      obj.Contains(boomer) mustEqual true
      obj.Valid(boomer) mustEqual false
    }

    "UI elements report individual deployable types for a certification type" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedEngineering, AdvancedHacking))

      val list1 = obj.UpdateUI(CombatEngineering)
      list1.head.productIterator.toList mustEqual List(94, 0, 83, 25)
      list1(1).productIterator.toList mustEqual List(95, 0, 84, 25)
      list1(2).productIterator.toList mustEqual List(97, 0, 86, 15)
      list1(3).productIterator.toList mustEqual List(98, 0, 87, 25)

      val list2 = obj.UpdateUI(AdvancedHacking)
      list2.head.productIterator.toList mustEqual List(104, 0, 93, 25)

      val list3 = obj.UpdateUI(AssaultEngineering)
      list3.head.productIterator.toList mustEqual List(96, 0, 85, 20)
      list3(1).productIterator.toList mustEqual List(103, 0, 92, 1)
      list3(2).productIterator.toList mustEqual List(101, 0, 90, 1)

      val list4 = obj.UpdateUI(FortificationEngineering)
      list4.head.productIterator.toList mustEqual List(94, 0, 83, 25)
      list4(1).productIterator.toList mustEqual List(95, 0, 84, 25)
      list4(2).productIterator.toList mustEqual List(97, 0, 86, 15)
      list4(3).productIterator.toList mustEqual List(99, 0, 88, 5)
      list4(4).productIterator.toList mustEqual List(100, 0, 89, 5)
      list4(5).productIterator.toList mustEqual List(98, 0, 87, 25)
      list4(6).productIterator.toList mustEqual List(102, 0, 91, 5)

      obj.UpdateUI(AdvancedEngineering).flatMap(tuple => tuple.productIterator.toList) mustEqual
        obj.UpdateUI(AssaultEngineering).flatMap(tuple => tuple.productIterator.toList) ++
          obj.UpdateUI(FortificationEngineering).flatMap(tuple => tuple.productIterator.toList)

      obj.UpdateUI(MediumAssault) mustEqual Nil
    }

    "UI elements report individual deployable types for a combination of certification types" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedEngineering, AdvancedHacking))

      obj
        .UpdateUI(
          List(AssaultEngineering, FortificationEngineering)
        )
        .flatMap(tuple => tuple.productIterator.toList) mustEqual
        obj.UpdateUI(AdvancedEngineering).flatMap(tuple => tuple.productIterator.toList)
    }

    "all of the one manned field turrets use the same category and the same UI elements" in {
      val obj = new DeployableToolbox
      //initial state
      (obj.UpdateUIElement(DeployedItem.portable_manned_turret) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs)).toSet mustEqual Set(
        (103, 0, 92, 0)
      ) //note: four elements become one common element

      //initialized state
      obj.Initialize(Set(CombatEngineering, AdvancedEngineering))
      (obj.UpdateUIElement(DeployedItem.portable_manned_turret) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs)).toSet mustEqual Set((103, 0, 92, 1))

      //portable_manned_turret_vs added
      val obj1 = new TurretDeployable(GlobalDefinitions.portable_manned_turret_vs)
      obj1.Definition.Item mustEqual DeployedItem.portable_manned_turret_vs
      obj1.Definition.DeployCategory mustEqual DeployableCategory.FieldTurrets
      obj1.GUID = PlanetSideGUID(1)
      obj.Add(obj1) mustEqual true
      (obj.UpdateUIElement(DeployedItem.portable_manned_turret) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs)).toSet mustEqual Set((103, 1, 92, 1))

      //portable_manned_turret_nc fails to add
      val obj2 = new TurretDeployable(GlobalDefinitions.portable_manned_turret_nc)
      obj2.Definition.Item mustEqual DeployedItem.portable_manned_turret_nc
      obj2.Definition.DeployCategory mustEqual DeployableCategory.FieldTurrets
      obj2.GUID = PlanetSideGUID(2)
      obj.Add(obj2) mustEqual false
      obj.Category(DeployableCategory.FieldTurrets).contains(PlanetSideGUID(1)) mustEqual true //included
      obj.Category(DeployableCategory.FieldTurrets).contains(PlanetSideGUID(2)) mustEqual false

      //swap turrets
      obj.Remove(obj1) mustEqual true
      obj.Add(obj2) mustEqual true
      (obj.UpdateUIElement(DeployedItem.portable_manned_turret) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs)).toSet mustEqual Set((103, 1, 92, 1))
      obj.Category(DeployableCategory.FieldTurrets).contains(PlanetSideGUID(1)) mustEqual false
      obj.Category(DeployableCategory.FieldTurrets).contains(PlanetSideGUID(2)) mustEqual true //included
    }

    "clear all deployables, return their GUIDs" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))

      obj.Add(new SensorDeployable(GlobalDefinitions.motionalarmsensor) { GUID = PlanetSideGUID(1) })
      obj.Add(new BoomerDeployable(GlobalDefinitions.boomer) { GUID = PlanetSideGUID(2) })
      obj.Add(new BoomerDeployable(GlobalDefinitions.he_mine) { GUID = PlanetSideGUID(3) })
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(1, 20)

      obj.Clear().toSet mustEqual Set(PlanetSideGUID(1), PlanetSideGUID(2), PlanetSideGUID(3))
      obj.CountDeployable(DeployedItem.boomer).productIterator.toList mustEqual List(0, 20)
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(0, 20)
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(0, 20)
    }

    "clear all deployables of a certain type" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))

      obj.Add(new ExplosiveDeployable(GlobalDefinitions.he_mine) { GUID = PlanetSideGUID(1) })
      obj.Add(new SensorDeployable(GlobalDefinitions.motionalarmsensor) { GUID = PlanetSideGUID(3) })
      obj.Add(new SensorDeployable(GlobalDefinitions.sensor_shield) { GUID = PlanetSideGUID(4) })
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(1, 20)
      obj.CountCategory(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(2, 20)

      obj.ClearDeployable(DeployedItem.motionalarmsensor).toSet mustEqual Set(PlanetSideGUID(3))
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(0, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(1, 20)
      obj.CountCategory(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(1, 20)
    }

    "clear all deployables of a certain category" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))

      obj.Add(new ExplosiveDeployable(GlobalDefinitions.he_mine) { GUID = PlanetSideGUID(1) })
      obj.Add(new SensorDeployable(GlobalDefinitions.motionalarmsensor) { GUID = PlanetSideGUID(3) })
      obj.Add(new SensorDeployable(GlobalDefinitions.sensor_shield) { GUID = PlanetSideGUID(4) })
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(1, 20)
      obj.CountCategory(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(2, 20)

      obj.ClearCategory(DeployedItem.motionalarmsensor).toSet mustEqual Set(PlanetSideGUID(3), PlanetSideGUID(4))
      obj.CountDeployable(DeployedItem.he_mine).productIterator.toList mustEqual List(1, 20)
      obj.CountDeployable(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(0, 20)
      obj.CountDeployable(DeployedItem.sensor_shield).productIterator.toList mustEqual List(0, 20)
      obj.CountCategory(DeployedItem.motionalarmsensor).productIterator.toList mustEqual List(0, 20)
    }
  }
}
