// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
import net.psforever.objects.avatar.DeployableToolbox
import net.psforever.objects.equipment.CItem.DeployedItem
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.CertificationType._
import org.specs2.mutable.Specification

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
      list.foreach({case(_,curr,_,max) =>
        curr mustEqual 0
        max mustEqual 0
      })
      ok
    }

    "initialization (CombatEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,10)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "initialization (AssaultEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AssaultEngineering))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,10)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "initialization (FortificationEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, FortificationEngineering))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "initialization (AdvancedEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(AdvancedEngineering))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "initialization (AdvancedHacking)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,10)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "initialization (without CombatEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(AssaultEngineering, FortificationEngineering, AdvancedHacking))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "initialization (GroundSupport)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(GroundSupport))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
    }

    "can not initialize twice" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set()) mustEqual true
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.Initialize(Set(AdvancedEngineering)) mustEqual false
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "uninitialized fields can not accept deployables" in {
      val obj = new DeployableToolbox
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Accept(boomer) mustEqual false
      obj.Add(boomer) mustEqual false
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "only initialized fields can accept deployables" in {
      val obj = new DeployableToolbox
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.Initialize(Set(CombatEngineering))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Accept(boomer) mustEqual true
      obj.Add(boomer) mustEqual true
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)
    }

    "change accessible fields by adding by certification type (CombatEngineering ...)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set())
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.AddToDeployableQuantities(
        CombatEngineering,
        Set(CombatEngineering)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,10)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.AddToDeployableQuantities(
        FortificationEngineering,
        Set(CombatEngineering, FortificationEngineering)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.AddToDeployableQuantities(
        AssaultEngineering,
        Set(CombatEngineering, FortificationEngineering, AssaultEngineering)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.AddToDeployableQuantities(
        AssaultEngineering,
        Set(CombatEngineering, FortificationEngineering, AssaultEngineering)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.AddToDeployableQuantities(
        AdvancedHacking,
        Set(CombatEngineering, FortificationEngineering, AssaultEngineering, AdvancedHacking)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "change accessible fields by adding by certification type (GroundSupport)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set())
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.AddToDeployableQuantities(
        GroundSupport,
        Set(GroundSupport)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
    }

    "change accessible fields by adding by certification type (AdvancedEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,10)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.AddToDeployableQuantities(
        AdvancedEngineering,
        Set(CombatEngineering, AdvancedEngineering)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "change accessible fields by removing by certification types (all)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AssaultEngineering, FortificationEngineering, AdvancedHacking, GroundSupport))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)

      obj.RemoveFromDeployablesQuantities(
        GroundSupport,
        Set(CombatEngineering, AssaultEngineering, FortificationEngineering, AdvancedHacking)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.RemoveFromDeployablesQuantities(
        AdvancedHacking,
        Set(CombatEngineering, AssaultEngineering, FortificationEngineering)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.RemoveFromDeployablesQuantities(
        FortificationEngineering,
        Set(CombatEngineering, AssaultEngineering)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,10)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.RemoveFromDeployablesQuantities(
        AssaultEngineering,
        Set(CombatEngineering)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,10)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.RemoveFromDeployablesQuantities(
        CombatEngineering,
        Set()
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "change accessible fields by removing by certification type (AdvancedEngineering)" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedEngineering))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,15)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,25)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,5)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,1)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)

      obj.RemoveFromDeployablesQuantities(
        AdvancedEngineering,
        Set(CombatEngineering)
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.spitfire_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,10)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.jammer_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_cloaked).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.spitfire_aa).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.tank_traps).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.deployable_shield_generator).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
      obj.UpdateUIElement(DeployedItem.router_telepad_deployable).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,0)
    }

    "can not remove deployables from an unpopulated field" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.Remove(boomer) mustEqual false
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
    }

    "can remove a deployable from a field it populates" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      val boomer = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.Add(boomer) mustEqual true
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)
      obj.Remove(boomer) mustEqual true
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
    }

    "can not remove a deployable from a field it does not populate" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      val boomer1 = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.Add(boomer1) mustEqual true
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)
      val boomer2 = new BoomerDeployable(GlobalDefinitions.boomer)
      obj.Remove(boomer2) mustEqual false
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)
    }

    "changing accessible fields by removing by certification type does not invalidate existing deployables" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      obj.Add(new BoomerDeployable(GlobalDefinitions.boomer))
      obj.Add(new BoomerDeployable(GlobalDefinitions.boomer))
      obj.Add(new BoomerDeployable(GlobalDefinitions.boomer))
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(3,20)

      obj.RemoveFromDeployablesQuantities(
        CombatEngineering,
        Set()
      )
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(3,0)
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
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      (1 to 19).foreach(_ => {
        val o1 = new ExplosiveDeployable(GlobalDefinitions.he_mine)
        obj.Accept(o1)mustEqual true
        obj.Add(o1) mustEqual true
      })
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(19,20)
      val o2 = new BoomerDeployable(GlobalDefinitions.he_mine)
      obj.Accept(o2) mustEqual true
      obj.Add(o2) mustEqual true
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(20,20)
      //fail extra
      val o3 = new BoomerDeployable(GlobalDefinitions.he_mine)
      obj.Accept(o3) mustEqual false
      obj.Add(o3) mustEqual false
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(20,20)
      //remove old
      obj.Remove(o2) mustEqual true
      obj.Accept(o2) mustEqual true
      obj.Accept(o3) mustEqual true
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(19,20)
      //add extra
      obj.Add(o3) mustEqual true
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(20,20)
    }

    "some deployables can share a category and a category maximum" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      (1 to 10).foreach(_ => {
        val o = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
        obj.Accept(o)mustEqual true
        obj.Add(o) mustEqual true
      })
      (1 to 10).foreach(_ => {
        val o = new SensorDeployable(GlobalDefinitions.sensor_shield)
        obj.Accept(o)mustEqual true
        obj.Add(o) mustEqual true
      })
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(10,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(10,20)

      val o1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      obj.Accept(o1)mustEqual false
      obj.Add(o1) mustEqual false
      val o2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      obj.Accept(o2)mustEqual false
      obj.Add(o2) mustEqual false
    }

    "remove the first element from a category, regardless of deployable type it is" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      val o1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      obj.Accept(o1)mustEqual true
      obj.Add(o1) mustEqual true
      val o2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      obj.Accept(o2)mustEqual true
      obj.Add(o2) mustEqual true
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)

      val o3 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      val displaced1 = obj.DisplaceFirst(o3) //remove the first deployable of sensor_shield's category
      displaced1.nonEmpty mustEqual true
      displaced1.get.Definition == o3.Definition mustEqual false
      displaced1.get.Definition mustEqual GlobalDefinitions.motionalarmsensor //removed deployable is a motionalarmsensor type
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)

      val o4 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val displaced2 = obj.DisplaceFirst(o3) //remove the first deployable of motionalarmsensor's category
      displaced2.nonEmpty mustEqual true
      displaced2.get.Definition == o4.Definition mustEqual false
      displaced2.get.Definition mustEqual GlobalDefinitions.sensor_shield //removed deployable is a sensor_shield type
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)

      obj.DisplaceFirst(o3) mustEqual None //we can not remove anymore deployables because the category is empty
      obj.DisplaceFirst(o4) mustEqual None //likewise
    }

    "get a list of GUIDs when the category contents are tested" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
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
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(2,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(2,20)

      val test1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val test2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      obj.Category(test1) mustEqual List(PlanetSideGUID(1),PlanetSideGUID(3),PlanetSideGUID(4),PlanetSideGUID(2))
      obj.Category(test1) mustEqual obj.Category(test2)
    }

    "get a list of GUIDs when the types are tested" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering, AdvancedHacking))
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
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
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(2,20)
      obj.UpdateUIElement(DeployedItem.sensor_shield).flatMap({ x => List(x._2, x._4) }) mustEqual List(2,20)

      val test1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      val test2 = new SensorDeployable(GlobalDefinitions.sensor_shield)
      obj.Deployables(test1) mustEqual List(PlanetSideGUID(1),PlanetSideGUID(4))
      obj.Deployables(test2) mustEqual List(PlanetSideGUID(3),PlanetSideGUID(2))
    }

    "all of the one manned field turrets use the same category and the same UI elements" in {
      val obj = new DeployableToolbox
      //initial state
      (obj.UpdateUIElement(DeployedItem.portable_manned_turret) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs)
        ).toSet mustEqual Set((103, 0, 92, 0)) //note: four elements become one common element

      //initialized state
      obj.Initialize(Set(CombatEngineering, AdvancedEngineering))
      (obj.UpdateUIElement(DeployedItem.portable_manned_turret) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs)
        ).toSet mustEqual Set((103, 0, 92, 1))

      //portable_manned_turret_vs added
      val obj1 = new TurretDeployable(GlobalDefinitions.portable_manned_turret_vs)
      obj1.Definition.Item mustEqual DeployedItem.portable_manned_turret_vs
      obj1.Definition.DeployCategory mustEqual DeployableCategory.FieldTurrets
      obj1.GUID = PlanetSideGUID(1)
      obj.Add(obj1) mustEqual true
      (obj.UpdateUIElement(DeployedItem.portable_manned_turret) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_nc) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_tr) ++
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs)
        ).toSet mustEqual Set((103, 1, 92, 1))

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
        obj.UpdateUIElement(DeployedItem.portable_manned_turret_vs)
        ).toSet mustEqual Set((103, 1, 92, 1))
      obj.Category(DeployableCategory.FieldTurrets).contains(PlanetSideGUID(1)) mustEqual false
      obj.Category(DeployableCategory.FieldTurrets).contains(PlanetSideGUID(2)) mustEqual true //included
    }

    "clear all deployables, return their GUIDs" in {
      val obj = new DeployableToolbox
      obj.Initialize(Set(CombatEngineering))
      val obj1 = new SensorDeployable(GlobalDefinitions.motionalarmsensor)
      obj1.GUID = PlanetSideGUID(1)
      obj.Add(obj1)
      val obj2 = new BoomerDeployable(GlobalDefinitions.boomer)
      obj2.GUID = PlanetSideGUID(2)
      obj.Add(obj2)
      val obj3 = new BoomerDeployable(GlobalDefinitions.he_mine)
      obj3.GUID = PlanetSideGUID(3)
      obj.Add(obj3)
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(1,20)

      val list = obj.Clear()
      list.contains(PlanetSideGUID(1)) mustEqual true
      list.contains(PlanetSideGUID(2)) mustEqual true
      list.contains(PlanetSideGUID(3)) mustEqual true
      obj.UpdateUIElement(DeployedItem.boomer).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.he_mine).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
      obj.UpdateUIElement(DeployedItem.motionalarmsensor).flatMap({ x => List(x._2, x._4) }) mustEqual List(0,20)
    }
  }
}
