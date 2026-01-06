// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.{GlobalDefinitions, SpawnPoint}
import net.psforever.objects.avatar.Certification
import net.psforever.objects.equipment.EffectTarget
import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.serverobject.deploy.InterferenceRange
import net.psforever.objects.serverobject.doors.InteriorDoorField
import net.psforever.objects.serverobject.mount.{MountInfo, SeatDefinition}
import net.psforever.objects.serverobject.pad.VehicleSpawnPadDefinition
import net.psforever.objects.serverobject.structures.AutoRepairStats
import net.psforever.objects.serverobject.terminals.implant.ImplantTerminalDefinition
import net.psforever.objects.serverobject.terminals.{EquipmentTerminalDefinition, VehicleTerminalDefinition}
import net.psforever.objects.serverobject.terminals.tabs.{ArmorWithAmmoPage, BattleframeSpawnLoadoutPage, CavernEquipmentQuestion, CavernVehicleQuestion, CertificationPage, EquipmentPage, ImplantPage, InfantryLoadoutPage, NoCavernEquipmentRule, NoExoSuitRule, VehicleLoadoutPage, VehiclePage}
import net.psforever.objects.serverobject.turret.{AutoChecks, AutoRanges, Automation, TurretUpgrade}
import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.etc.ExplodingRadialDegrade
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.types.{ExoSuitType, Vector3}
import net.psforever.util.Config

import scala.collection.mutable
import scala.concurrent.duration._

object GlobalDefinitionsMiscellaneous {
  import GlobalDefinitions._

  /**
   * Initialize `Miscellaneous` globals.
   */
  def init(): Unit = {
    val vterm: Any => VolumetricGeometry = GeometryForm.representByCylinder(radius = 1.03515f, height = 1.09374f)
    val certs: Seq[Certification] = Certification.values.filter(_.cost != 0)
    val page: CertificationPage = CertificationPage(certs)

    ams_respawn_tube.Name = "ams_respawn_tube"
    ams_respawn_tube.Delay = 10 // Temporary -- Default value is 15
    ams_respawn_tube.SpecificPointFunc = SpawnPoint.AMS
    ams_respawn_tube.Damageable = false
    ams_respawn_tube.Repairable = false

    matrix_terminala.Name = "matrix_terminala"
    matrix_terminala.Damageable = false
    matrix_terminala.Repairable = false

    matrix_terminalb.Name = "matrix_terminalb"
    matrix_terminalb.Damageable = false
    matrix_terminalb.Repairable = false

    matrix_terminalc.Name = "matrix_terminalc"
    matrix_terminalc.Damageable = false
    matrix_terminalc.Repairable = false

    spawn_terminal.Name = "spawn_terminal"
    spawn_terminal.Damageable = false
    spawn_terminal.Repairable = false
    spawn_terminal.autoRepair = AutoRepairStats(1, 5000, 200, 1)

    order_terminal.Name = "order_terminal"
    order_terminal.Tab += 0 -> {
      val tab = EquipmentPage(
        EquipmentTerminalDefinition.infantryAmmunition ++ EquipmentTerminalDefinition.infantryWeapons
      )
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    order_terminal.Tab += 1 -> ArmorWithAmmoPage(
      EquipmentTerminalDefinition.suits ++ EquipmentTerminalDefinition.maxSuits,
      EquipmentTerminalDefinition.maxAmmo
    )
    order_terminal.Tab += 2 -> EquipmentPage(
      EquipmentTerminalDefinition.supportAmmunition ++ EquipmentTerminalDefinition.supportWeapons
    )
    order_terminal.Tab += 3 -> EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    order_terminal.Tab += 4 -> {
      val tab = InfantryLoadoutPage()
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    order_terminal.SellEquipmentByDefault = true
    order_terminal.MaxHealth = 500
    order_terminal.Damageable = true
    order_terminal.Repairable = true
    order_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    order_terminal.RepairIfDestroyed = true
    order_terminal.Subtract.Damage1 = 8
    order_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.8438f, height = 1.3f)

    order_terminala.Name = "order_terminala"
    order_terminala.Tab += 0 -> {
      val tab = EquipmentPage(
        EquipmentTerminalDefinition.infantryAmmunition ++ EquipmentTerminalDefinition.infantryWeapons
      )
      tab.Exclude = List(NoCavernEquipmentRule)
      tab
    }
    order_terminala.Tab += 1 -> ArmorWithAmmoPage(
      EquipmentTerminalDefinition.suits,
      EquipmentTerminalDefinition.maxAmmo
    )
    order_terminala.Tab += 2 -> EquipmentPage(
      EquipmentTerminalDefinition.supportAmmunition ++ EquipmentTerminalDefinition.supportWeapons
    )
    order_terminala.Tab += 3 -> EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    order_terminala.Tab += 4 -> {
      val tab = InfantryLoadoutPage()
      tab.Exclude = List(NoExoSuitRule(ExoSuitType.MAX), NoCavernEquipmentRule)
      tab
    }
    order_terminala.SellEquipmentByDefault = true
    order_terminala.Damageable = false
    order_terminala.Repairable = false

    order_terminalb.Name = "order_terminalb"
    order_terminalb.Tab += 0 -> {
      val tab = EquipmentPage(
        EquipmentTerminalDefinition.infantryAmmunition ++ EquipmentTerminalDefinition.infantryWeapons
      )
      tab.Exclude = List(NoCavernEquipmentRule)
      tab
    }
    order_terminalb.Tab += 1 -> ArmorWithAmmoPage(
      EquipmentTerminalDefinition.suits,
      EquipmentTerminalDefinition.maxAmmo
    )
    order_terminalb.Tab += 2 -> EquipmentPage(
      EquipmentTerminalDefinition.supportAmmunition ++ EquipmentTerminalDefinition.supportWeapons
    )
    order_terminalb.Tab += 3 -> EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    order_terminalb.Tab += 4 -> {
      val tab = InfantryLoadoutPage()
      tab.Exclude = List(NoExoSuitRule(ExoSuitType.MAX), NoCavernEquipmentRule)
      tab
    }
    order_terminalb.SellEquipmentByDefault = true
    order_terminalb.Damageable = false
    order_terminalb.Repairable = false

    vanu_equipment_term.Name = "vanu_equipment_term"
    vanu_equipment_term.Tab += 0 -> EquipmentPage(
      EquipmentTerminalDefinition.infantryAmmunition ++ EquipmentTerminalDefinition.infantryWeapons
    )
    vanu_equipment_term.Tab += 1 -> ArmorWithAmmoPage(
      EquipmentTerminalDefinition.suits ++ EquipmentTerminalDefinition.maxSuits,
      EquipmentTerminalDefinition.maxAmmo
    )
    vanu_equipment_term.Tab += 2 -> EquipmentPage(
      EquipmentTerminalDefinition.supportAmmunition ++ EquipmentTerminalDefinition.supportWeapons
    )
    vanu_equipment_term.Tab += 3 -> EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    vanu_equipment_term.Tab += 4 -> InfantryLoadoutPage()
    vanu_equipment_term.SellEquipmentByDefault = true
    vanu_equipment_term.Damageable = false
    vanu_equipment_term.Repairable = false

    cert_terminal.Name = "cert_terminal"
    cert_terminal.Tab += 0 -> page
    cert_terminal.MaxHealth = 500
    cert_terminal.Damageable = true
    cert_terminal.Repairable = true
    cert_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    cert_terminal.RepairIfDestroyed = true
    cert_terminal.Subtract.Damage1 = 8
    cert_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.66405f, height = 1.09374f)

    implant_terminal_mech.Name = "implant_terminal_mech"
    implant_terminal_mech.MaxHealth = 1000
    implant_terminal_mech.Damageable = true
    implant_terminal_mech.Repairable = true
    implant_terminal_mech.autoRepair = AutoRepairStats(1.6f, 5000, 2400, 0.05f)
    implant_terminal_mech.RepairIfDestroyed = true
    implant_terminal_mech.RadiationShielding = 0.5f
    implant_terminal_mech.Geometry = GeometryForm.representByCylinder(radius = 2.7813f, height = 6.4375f)

    implant_terminal_interface.Name = "implant_terminal_interface"
    implant_terminal_interface.Tab += 0 -> ImplantPage(ImplantTerminalDefinition.implants)
    implant_terminal_interface.MaxHealth = 500
    implant_terminal_interface.Damageable = false //true
    implant_terminal_interface.Repairable = true
    implant_terminal_interface.autoRepair = AutoRepairStats(1, 5000, 200, 1)
    implant_terminal_interface.RepairIfDestroyed = true
    //TODO will need geometry when Damageable = true

    ground_vehicle_terminal.Name = "ground_vehicle_terminal"
    ground_vehicle_terminal.Tab += 46769 -> {
      val tab = VehiclePage(
        VehicleTerminalDefinition.groundVehicles,
        VehicleTerminalDefinition.trunk
      )
      tab.Exclude = List(CavernVehicleQuestion)
      tab
    }
    ground_vehicle_terminal.Tab += 4 -> {
      val tab = VehicleLoadoutPage(10)
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    ground_vehicle_terminal.MaxHealth = 500
    ground_vehicle_terminal.Damageable = true
    ground_vehicle_terminal.Repairable = true
    ground_vehicle_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    ground_vehicle_terminal.RepairIfDestroyed = true
    ground_vehicle_terminal.Subtract.Damage1 = 8
    ground_vehicle_terminal.Geometry = vterm

    air_vehicle_terminal.Name = "air_vehicle_terminal"
    air_vehicle_terminal.Tab += 46769 -> VehiclePage(
      VehicleTerminalDefinition.flight1Vehicles,
      VehicleTerminalDefinition.trunk
    )
    air_vehicle_terminal.Tab += 4 -> {
      val tab = VehicleLoadoutPage(10)
      tab.Exclude = List(CavernVehicleQuestion, CavernEquipmentQuestion)
      tab
    }
    air_vehicle_terminal.MaxHealth = 500
    air_vehicle_terminal.Damageable = true
    air_vehicle_terminal.Repairable = true
    air_vehicle_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    air_vehicle_terminal.RepairIfDestroyed = true
    air_vehicle_terminal.Subtract.Damage1 = 8
    air_vehicle_terminal.Geometry = vterm

    dropship_vehicle_terminal.Name = "dropship_vehicle_terminal"
    dropship_vehicle_terminal.Tab += 46769 -> VehiclePage(
      VehicleTerminalDefinition.flight1Vehicles ++ VehicleTerminalDefinition.flight2Vehicles,
      VehicleTerminalDefinition.trunk
    )
    dropship_vehicle_terminal.Tab += 4 -> {
      val tab = VehicleLoadoutPage(10)
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    dropship_vehicle_terminal.MaxHealth = 500
    dropship_vehicle_terminal.Damageable = true
    dropship_vehicle_terminal.Repairable = true
    dropship_vehicle_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    dropship_vehicle_terminal.RepairIfDestroyed = true
    dropship_vehicle_terminal.Subtract.Damage1 = 8
    dropship_vehicle_terminal.Geometry = vterm

    vehicle_terminal_combined.Name = "vehicle_terminal_combined"
    vehicle_terminal_combined.Tab += 46769 -> {
      val tab = VehiclePage(
        VehicleTerminalDefinition.flight1Vehicles ++ VehicleTerminalDefinition.groundVehicles,
        VehicleTerminalDefinition.trunk
      )
      tab.Exclude = List(CavernVehicleQuestion)
      tab
    }
    vehicle_terminal_combined.Tab += 4 -> {
      val tab = VehicleLoadoutPage(10)
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    vehicle_terminal_combined.MaxHealth = 500
    vehicle_terminal_combined.Damageable = true
    vehicle_terminal_combined.Repairable = true
    vehicle_terminal_combined.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    vehicle_terminal_combined.RepairIfDestroyed = true
    vehicle_terminal_combined.Subtract.Damage1 = 8
    vehicle_terminal_combined.Geometry = vterm

    vanu_air_vehicle_term.Name = "vanu_air_vehicle_term"
    vanu_air_vehicle_term.Tab += 46769 -> VehiclePage(
      VehicleTerminalDefinition.flight1Vehicles,
      VehicleTerminalDefinition.trunk
    )
    vanu_air_vehicle_term.Tab += 4 -> VehicleLoadoutPage(10)
    vanu_air_vehicle_term.MaxHealth = 500
    vanu_air_vehicle_term.Damageable = true
    vanu_air_vehicle_term.Repairable = true
    vanu_air_vehicle_term.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    vanu_air_vehicle_term.RepairIfDestroyed = true
    vanu_air_vehicle_term.Subtract.Damage1 = 8

    vanu_vehicle_term.Name = "vanu_vehicle_term"
    vanu_vehicle_term.Tab += 46769 -> VehiclePage(
      VehicleTerminalDefinition.groundVehicles,
      VehicleTerminalDefinition.trunk
    )
    vanu_vehicle_term.Tab += 4 -> VehicleLoadoutPage(10)
    vanu_vehicle_term.MaxHealth = 500
    vanu_vehicle_term.Damageable = true
    vanu_vehicle_term.Repairable = true
    vanu_vehicle_term.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    vanu_vehicle_term.RepairIfDestroyed = true
    vanu_vehicle_term.Subtract.Damage1 = 8

    bfr_terminal.Name = "bfr_terminal"
    bfr_terminal.Tab += 0 -> VehiclePage(
      VehicleTerminalDefinition.bfrVehicles,
      VehicleTerminalDefinition.trunk
    )
    bfr_terminal.Tab += 1 -> EquipmentPage(
      EquipmentTerminalDefinition.bfrAmmunition ++ EquipmentTerminalDefinition.bfrArmWeapons
    ) //inaccessible?
    bfr_terminal.Tab += 2 -> EquipmentPage(
      EquipmentTerminalDefinition.bfrAmmunition ++ EquipmentTerminalDefinition.bfrGunnerWeapons
    ) //inaccessible?
    bfr_terminal.Tab += 3 -> {
      val tab = BattleframeSpawnLoadoutPage(VehicleTerminalDefinition.bfrVehicles)
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    bfr_terminal.MaxHealth = 500
    bfr_terminal.Damageable = true
    bfr_terminal.Repairable = true
    bfr_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    bfr_terminal.RepairIfDestroyed = true
    bfr_terminal.Subtract.Damage1 = 8
    bfr_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.92185f, height = 2.64693f)

    respawn_tube.Name = "respawn_tube"
    respawn_tube.Delay = 10
    respawn_tube.SpecificPointFunc = SpawnPoint.Tube
    respawn_tube.MaxHealth = 1000
    respawn_tube.Damageable = true
    respawn_tube.DamageableByFriendlyFire = false
    respawn_tube.Repairable = true
    respawn_tube.autoRepair = AutoRepairStats(1.6f, 10000, 2400, 1)
    respawn_tube.RepairIfDestroyed = true
    respawn_tube.Subtract.Damage1 = 8
    respawn_tube.Geometry = GeometryForm.representByCylinder(radius = 0.9336f, height = 2.84375f)

    respawn_tube_sanctuary.Name = "respawn_tube"
    respawn_tube_sanctuary.Delay = 10
    respawn_tube_sanctuary.SpecificPointFunc = SpawnPoint.Default
    respawn_tube_sanctuary.MaxHealth = 1000
    respawn_tube_sanctuary.Damageable = false //true?
    respawn_tube_sanctuary.DamageableByFriendlyFire = false
    respawn_tube_sanctuary.Repairable = true
    respawn_tube_sanctuary.autoRepair = AutoRepairStats(1.6f, 10000, 2400, 1)
    //TODO will need geometry when Damageable = true

    respawn_tube_tower.Name = "respawn_tube_tower"
    respawn_tube_tower.Delay = 10 // Temporary -- Default value is 20
    respawn_tube_tower.SpecificPointFunc = SpawnPoint.Tube
    respawn_tube_tower.MaxHealth = 1000
    respawn_tube_tower.Damageable = true
    respawn_tube_tower.DamageableByFriendlyFire = false
    respawn_tube_tower.Repairable = true
    respawn_tube_tower.autoRepair = AutoRepairStats(1.6f, 10000, 2400, 1)
    respawn_tube_tower.RepairIfDestroyed = true
    respawn_tube_tower.Subtract.Damage1 = 8
    respawn_tube_tower.Geometry = GeometryForm.representByCylinder(radius = 0.9336f, height = 2.84375f)

    teleportpad_terminal.Name = "teleportpad_terminal"
    teleportpad_terminal.Tab += 0 -> EquipmentPage(EquipmentTerminalDefinition.routerTerminal)
    teleportpad_terminal.Damageable = false
    teleportpad_terminal.Repairable = false

    targeting_laser_dispenser.Name = "targeting_laser_dispenser"
    targeting_laser_dispenser.Tab += 0 -> EquipmentPage(EquipmentTerminalDefinition.flailTerminal)
    targeting_laser_dispenser.Damageable = false
    targeting_laser_dispenser.Repairable = false

    medical_terminal.Name = "medical_terminal"
    medical_terminal.Interval = 500
    medical_terminal.HealAmount = 5
    medical_terminal.ArmorAmount = 10
    medical_terminal.UseRadius = 0.75f
    medical_terminal.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.Medical
    medical_terminal.MaxHealth = 500
    medical_terminal.Damageable = true
    medical_terminal.Repairable = true
    medical_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)
    medical_terminal.RepairIfDestroyed = true
    medical_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.711f, height = 1.75f)

    medical_terminal_healing_module.Name = "medical_terminal_healing_module"
    medical_terminal_healing_module.Interval = 2000
    medical_terminal_healing_module.HealAmount = 1
    medical_terminal_healing_module.ArmorAmount = 0
    medical_terminal_healing_module.UseRadius = 300
    medical_terminal_healing_module.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.HealthModule
    medical_terminal_healing_module.Damageable = false
    medical_terminal_healing_module.Repairable = false

    adv_med_terminal.Name = "adv_med_terminal"
    adv_med_terminal.Interval = 500
    adv_med_terminal.HealAmount = 8
    adv_med_terminal.ArmorAmount = 15
    adv_med_terminal.UseRadius = 0.75f
    adv_med_terminal.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.Medical
    adv_med_terminal.MaxHealth = 750
    adv_med_terminal.Damageable = true
    adv_med_terminal.Repairable = true
    adv_med_terminal.autoRepair = AutoRepairStats(1.57894f, 5000, 2400, 0.05f)
    adv_med_terminal.RepairIfDestroyed = true
    adv_med_terminal.Geometry = GeometryForm.representByCylinder(radius = 0.8662125f, height = 3.47f)

    crystals_health_a.Name = "crystals_health_a"
    crystals_health_a.Interval = 500
    crystals_health_a.HealAmount = 4
    crystals_health_a.UseRadius = 5
    crystals_health_a.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.HealthCrystal
    crystals_health_a.Damageable = false
    crystals_health_a.Repairable = false

    crystals_health_b.Name = "crystals_health_b"
    crystals_health_b.Interval = 500
    crystals_health_b.HealAmount = 4
    crystals_health_b.UseRadius = 5
    crystals_health_b.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.HealthCrystal
    crystals_health_b.Damageable = false
    crystals_health_b.Repairable = false

    crystals_repair_a.Name = "crystals_repair_a"
    crystals_repair_a.Interval = 500
    crystals_repair_a.ArmorAmount = 4
    crystals_repair_a.UseRadius = 5
    crystals_repair_a.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.RepairCrystal
    crystals_repair_a.Damageable = false
    crystals_repair_a.Repairable = false

    crystals_repair_b.Name = "crystals_repair_b"
    crystals_repair_b.Interval = 500
    crystals_repair_b.ArmorAmount = 4
    crystals_repair_b.UseRadius = 5
    crystals_repair_b.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.RepairCrystal
    crystals_repair_b.Damageable = false
    crystals_repair_b.Repairable = false

    crystals_vehicle_a.Name = "crystals_vehicle_a"
    crystals_vehicle_a.Interval = 1000
    crystals_vehicle_a.HealAmount = 60
    crystals_vehicle_a.UseRadius = 15
    crystals_vehicle_a.TargetValidation += EffectTarget.Category.Vehicle -> EffectTarget.Validation.VehicleCrystal
    crystals_vehicle_a.Damageable = false
    crystals_vehicle_a.Repairable = false

    crystals_vehicle_b.Name = "crystals_vehicle_b"
    crystals_vehicle_b.Interval = 1000
    crystals_vehicle_b.HealAmount = 60
    crystals_vehicle_b.UseRadius = 15
    crystals_vehicle_b.TargetValidation += EffectTarget.Category.Vehicle -> EffectTarget.Validation.VehicleCrystal
    crystals_vehicle_b.Damageable = false
    crystals_vehicle_b.Repairable = false

    crystals_energy_a.Name = "crystals_energy_a"
    crystals_energy_a.Interval = 1000
    crystals_energy_a.UseRadius = 5
    crystals_energy_a.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.AncientWeaponRecharge
    crystals_energy_a.Damageable = false
    crystals_energy_a.Repairable = false

    crystals_energy_b.Name = "crystals_energy_b"
    crystals_energy_b.Interval = 1000
    crystals_energy_b.UseRadius = 5
    crystals_energy_b.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.AncientWeaponRecharge
    crystals_energy_b.Damageable = false
    crystals_energy_b.Repairable = false

    crystals_energy.Name = "crystals_energy"
    crystals_energy.Interval = 1000
    crystals_energy.UseRadius = 5
    crystals_energy.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.AncientWeaponRecharge
    crystals_energy.Damageable = false
    crystals_energy.Repairable = false

    portable_med_terminal.Name = "portable_med_terminal"
    portable_med_terminal.Interval = 500
    portable_med_terminal.HealAmount = 5
    portable_med_terminal.ArmorAmount = 10
    portable_med_terminal.UseRadius = 3
    portable_med_terminal.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.Medical
    portable_med_terminal.MaxHealth = 500
    portable_med_terminal.Damageable = false //TODO actually true
    portable_med_terminal.Repairable = false
    portable_med_terminal.autoRepair = AutoRepairStats(2.24215f, 5000, 3500, 0.05f)

    pad_landing_frame.Name = "pad_landing_frame"
    pad_landing_frame.Interval = 1000
    pad_landing_frame.HealAmount = 60
    pad_landing_frame.UseRadius = 20
    pad_landing_frame.TargetValidation += EffectTarget.Category.Aircraft -> EffectTarget.Validation.PadLanding
    pad_landing_frame.Damageable = false
    pad_landing_frame.Repairable = false

    pad_landing_tower_frame.Name = "pad_landing_tower_frame"
    pad_landing_tower_frame.Interval = 1000
    pad_landing_tower_frame.HealAmount = 60
    pad_landing_tower_frame.UseRadius = 20
    pad_landing_tower_frame.TargetValidation += EffectTarget.Category.Aircraft -> EffectTarget.Validation.PadLanding
    pad_landing_tower_frame.Damageable = false
    pad_landing_tower_frame.Repairable = false

    repair_silo.Name = "repair_silo"
    repair_silo.Interval = 1000
    repair_silo.HealAmount = 60
    repair_silo.UseRadius = 20
    repair_silo.TargetValidation += EffectTarget.Category.Vehicle -> EffectTarget.Validation.RepairSilo
    repair_silo.Damageable = false
    repair_silo.Repairable = false

    recharge_terminal.Name = "recharge_terminal"
    recharge_terminal.Interval = 1000
    recharge_terminal.UseRadius = 20
    recharge_terminal.TargetValidation += EffectTarget.Category.Vehicle -> EffectTarget.Validation.AncientVehicleWeaponRecharge
    recharge_terminal.Damageable = false
    recharge_terminal.Repairable = false

    recharge_terminal_weapon_module.Name = "recharge_terminal_weapon_module"
    recharge_terminal_weapon_module.Interval = 1000
    recharge_terminal_weapon_module.UseRadius = 300
    recharge_terminal_weapon_module.TargetValidation += EffectTarget.Category.Player -> EffectTarget.Validation.AncientWeaponRecharge
    recharge_terminal_weapon_module.Damageable = false
    recharge_terminal_weapon_module.Repairable = false

    mb_pad_creation.Name = "mb_pad_creation"
    mb_pad_creation.Damageable = false
    mb_pad_creation.Repairable = false
    mb_pad_creation.VehicleCreationZOffset = 2.52604f
    mb_pad_creation.killBox = VehicleSpawnPadDefinition.prepareKillBox(
      forwardLimit = 14,
      backLimit = 10,
      sideLimit = 7.5f,
      aboveLimit = 5 //double to 10 when spawning a flying vehicle
    )
    mb_pad_creation.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 14
      DamageRadius = 14.5f
      DamageAtEdge = 0.00002f
      //damage is 99999 at 14m, dropping rapidly to ~1 at 14.5m
    }

    dropship_pad_doors.Name = "dropship_pad_doors"
    dropship_pad_doors.Damageable = false
    dropship_pad_doors.Repairable = false
    dropship_pad_doors.VehicleCreationZOffset = 4.89507f
    dropship_pad_doors.VehicleCreationZOrientOffset = -90f
    dropship_pad_doors.killBox = VehicleSpawnPadDefinition.prepareKillBox(
      forwardLimit = 14,
      backLimit = 14,
      sideLimit = 13.5f,
      aboveLimit = 5 //doubles to 10
    )
    dropship_pad_doors.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 14
      DamageRadius = 14.5f
      DamageAtEdge = 0.00002f
      //damage is 99999 at 14m, dropping rapidly to ~1 at 14.5m
    }

    vanu_vehicle_creation_pad.Name = "vanu_vehicle_creation_pad"
    vanu_vehicle_creation_pad.Damageable = false
    vanu_vehicle_creation_pad.Repairable = false
    vanu_vehicle_creation_pad.killBox = VehicleSpawnPadDefinition.prepareVanuKillBox(
      radius = 8.5f,
      aboveLimit = 5
    )
    vanu_vehicle_creation_pad.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 14
      DamageRadius = 14.5f
      DamageAtEdge = 0.00002f
      //damage is 99999 at 14m, dropping rapidly to ~1 at 14.5m
    }

    bfr_door.Name = "bfr_door"
    bfr_door.Damageable = false
    bfr_door.Repairable = false
    //bfr_door.VehicleCreationZOffset = -4.5f
    bfr_door.VehicleCreationZOrientOffset = 0f //90f
    bfr_door.killBox = VehicleSpawnPadDefinition.prepareBfrShedKillBox(
      radius = 10f,
      aboveLimit = 10f
    )
    bfr_door.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 14 //TODO fix this
      DamageRadius = 14.5f //TODO fix this
      DamageAtEdge = 0.00002f
      //damage is 99999 at 14m, dropping rapidly to ~1 at 14.5m
    }

    pad_create.Name = "pad_create"
    pad_create.Damageable = false
    pad_create.Repairable = false
    //pad_create.killBox = ...
    //pad_create.innateDamage = ...

    pad_creation.Name = "pad_creation"
    pad_creation.Damageable = false
    pad_creation.Repairable = false
    pad_creation.VehicleCreationZOffset = 1.70982f
    //pad_creation.killBox = ...
    //pad_creation.innateDamage = ...

    spawnpoint_vehicle.Name = "spawnpoint_vehicle"
    spawnpoint_vehicle.Damageable = false
    spawnpoint_vehicle.Repairable = false
    //spawnpoint_vehicle.killBox = ...
    //spawnpoint_vehicle.innateDamage = ...

    mb_locker.Name = "mb_locker"
    mb_locker.Damageable = false
    mb_locker.Repairable = false

    lock_external.Name = "lock_external"
    lock_external.Damageable = false
    lock_external.Repairable = false

    amp_cap_door.Name = "amp_cap_door"

    ancient_door.Name = "ancient_door"
    ancient_door.UseRadius = 1f
    //ancient_door.environmentField = InteriorDoorField()

    ancient_garage_door.Name = "ancient_garage_door"
    ancient_garage_door.UseRadius = 1f
    //ancient_garage_door.environmentField = InteriorDoorField()

    cryo_med_door.Name = "cryo_med_door"

    cryo_room_door.Name = "cryo_room_door"

    door.Name = "door"

    door_airlock.Name = "door_airlock"

    door_airlock_orb.Name = "door_airlock_orb"

    door_dsp.Name = "door_dsp"

    door_garage.Name = "door_garage"

    door_interior.Name = "door_interior"

    door_mb.Name = "door_mb"

    door_mb_garage.Name = "door_mb_garage"

    door_mb_main.Name = "door_mb_main"

    door_mb_orb.Name = "door_mb_orb"

    door_mb_side.Name = "door_mb_side"

    door_nc_garage.Name = "door_nc_garage"

    door_nc_rotating.Name = "door_nc_rotating"

    door_ncside.Name = "door_ncside"

    door_orbspawn.Name = "door_orbspawn"

    door_spawn_mb.Name = "door_spawn_mb"
    door_spawn_mb.Damageable = true

    garage_door.Name = "garage_door"

    gr_door_airlock.Name = "gr_door_airlock"

    gr_door_ext.Name = "gr_door_ext"
    gr_door_ext.UseRadius = 1.9f
    gr_door_ext.environmentField = InteriorDoorField()

    gr_door_garage_ext.Name = "gr_door_garage_ext"
    gr_door_garage_ext.UseRadius = 11f
    gr_door_garage_ext.initialOpeningDistance = 8f
    gr_door_garage_ext.continuousOpenDistance = 9f
    gr_door_garage_ext.environmentField = InteriorDoorField(Some(-11), centerOn = true)

    gr_door_garage_int.Name = "gr_door_garage_int"
    gr_door_garage_int.initialOpeningDistance = 8f
    gr_door_garage_int.continuousOpenDistance = 9f

    gr_door_int.Name = "gr_door_int"

    gr_door_main.Name = "gr_door_main"
    gr_door_main.UseRadius = 2.75f
    gr_door_main.environmentField = InteriorDoorField()

    gr_door_mb_ext.Name = "gr_door_mb_ext"
    gr_door_mb_ext.UseRadius = 2f
    gr_door_mb_ext.environmentField = InteriorDoorField()

    gr_door_mb_int.Name = "gr_door_mb_int"

    gr_door_mb_lrg.Name = "gr_door_mb_lrg"
    gr_door_mb_lrg.UseRadius = 2.5f
    gr_door_mb_lrg.environmentField = InteriorDoorField()

    gr_door_mb_obsd.Name = "gr_door_mb_obsd"

    gr_door_mb_orb.Name = "gr_door_mb_orb"

    gr_door_med.Name = "gr_door_med"

    main_door.Name = "main_door"

    shield_door.Name = "shield_door"

    spawn_tube_door.Name = "spawn_tube_door"
    spawn_tube_door.Damageable = true

    spawn_tube_door_coffin.Name = "spawn_tube_door_coffin"
    spawn_tube_door_coffin.Damageable = true

    resource_silo.Name = "resource_silo"
    resource_silo.UseRadius = 22 //20
    resource_silo.Damageable = false
    resource_silo.Repairable = false
    resource_silo.MaxNtuCapacitor = 1000
    resource_silo.ChargeTime = 105.seconds //from 0-100% in roughly 105s on live (~20%-100% https://youtu.be/veOWToR2nSk?t=1402)

    capture_terminal.Name = "capture_terminal"
    capture_terminal.Damageable = false
    capture_terminal.Repairable = false
    capture_terminal.FacilityHackTime = Config.app.game.facilityHackTime

    secondary_capture.Name = "secondary_capture"
    secondary_capture.Damageable = false
    secondary_capture.Repairable = false
    secondary_capture.FacilityHackTime = 1.millisecond

    vanu_control_console.Name = "vanu_control_console"
    vanu_control_console.Damageable = false
    vanu_control_console.Repairable = false
    vanu_control_console.FacilityHackTime = 10.minutes

    main_terminal.Name = "main_terminal"
    main_terminal.Damageable = false
    main_terminal.Repairable = false

    lodestar_repair_terminal.Name = "lodestar_repair_terminal"
    lodestar_repair_terminal.Interval = 1000
    lodestar_repair_terminal.HealAmount = 60
    lodestar_repair_terminal.UseRadius = 40
    lodestar_repair_terminal.TargetValidation += EffectTarget.Category.Vehicle -> EffectTarget.Validation.LodestarRepair
    lodestar_repair_terminal.Damageable = false
    lodestar_repair_terminal.Repairable = false

    multivehicle_rearm_terminal.Name = "multivehicle_rearm_terminal"
    multivehicle_rearm_terminal.Tab += 3 -> EquipmentPage(
      EquipmentTerminalDefinition.vehicleAmmunition
    )
    multivehicle_rearm_terminal.Tab += 4 -> {
      val tab = VehicleLoadoutPage(10)
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    multivehicle_rearm_terminal.SellEquipmentByDefault = true //TODO ?
    multivehicle_rearm_terminal.Damageable = false
    multivehicle_rearm_terminal.Repairable = false

    bfr_rearm_terminal.Name = "bfr_rearm_terminal"
    bfr_rearm_terminal.Tab += 1 -> EquipmentPage(
      EquipmentTerminalDefinition.bfrAmmunition ++ EquipmentTerminalDefinition.bfrArmWeapons
    )
    bfr_rearm_terminal.Tab += 2 -> EquipmentPage(
      EquipmentTerminalDefinition.bfrAmmunition ++ EquipmentTerminalDefinition.bfrGunnerWeapons
    )
    bfr_rearm_terminal.Tab += 3 -> {
      val tab = VehicleLoadoutPage(15)
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    bfr_rearm_terminal.SellEquipmentByDefault = true //TODO ?
    bfr_rearm_terminal.Damageable = false
    bfr_rearm_terminal.Repairable = false

    air_rearm_terminal.Name = "air_rearm_terminal"
    air_rearm_terminal.Tab += 3 -> EquipmentPage(EquipmentTerminalDefinition.vehicleAmmunition)
    air_rearm_terminal.Tab += 4 -> {
      val tab = VehicleLoadoutPage(10)
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    air_rearm_terminal.SellEquipmentByDefault = true //TODO ?
    air_rearm_terminal.Damageable = false
    air_rearm_terminal.Repairable = false

    ground_rearm_terminal.Name = "ground_rearm_terminal"
    ground_rearm_terminal.Tab += 3 -> EquipmentPage(
      EquipmentTerminalDefinition.vehicleAmmunition
    )
    ground_rearm_terminal.Tab += 4 -> {
      val tab = VehicleLoadoutPage(10)
      tab.Exclude = List(CavernEquipmentQuestion)
      tab
    }
    ground_rearm_terminal.SellEquipmentByDefault = true //TODO ?
    ground_rearm_terminal.Damageable = false
    ground_rearm_terminal.Repairable = false

    manned_turret.Name = "manned_turret"
    manned_turret.MaxHealth = 3600
    manned_turret.Damageable = true
    manned_turret.DamageDisablesAt = 1800
    manned_turret.Repairable = true
    manned_turret.autoRepair = AutoRepairStats(1.0909f, 10000, 1600, 0.05f)
    manned_turret.RepairIfDestroyed = true
    manned_turret.WeaponPaths += 1                          -> new mutable.HashMap()
    manned_turret.WeaponPaths(1) += TurretUpgrade.None      -> phalanx_sgl_hevgatcan
    manned_turret.WeaponPaths(1) += TurretUpgrade.AVCombo   -> phalanx_avcombo
    manned_turret.WeaponPaths(1) += TurretUpgrade.FlakCombo -> phalanx_flakcombo
    manned_turret.Seats += 0 -> new SeatDefinition()
    manned_turret.controlledWeapons(seat = 0, weapon = 1)
    manned_turret.MountPoints += 1                          -> MountInfo(0)
    manned_turret.FactionLocked = true
    manned_turret.ReserveAmmunition = false
    manned_turret.RadiationShielding = 0.5f
    manned_turret.AutoFire = Automation(
      AutoRanges(
        detection = 125f,
        trigger = 100f,
        escape = 150f
      ),
      AutoChecks(
        validation = List(
          EffectTarget.Validation.FacilityTurretValidateMaxTarget,
          EffectTarget.Validation.FacilityTurretValidateGroundVehicleTarget,
          EffectTarget.Validation.FacilityTurretValidateAircraftTarget,
          EffectTarget.Validation.AutoTurretValidateMountableEntityTarget
        )
      ),
      retaliatoryDelay = 4000L, //8000L
      cylindrical = true,
      cylindricalExtraHeight = 25f,
      detectionSweepTime = 2.seconds,
      refireTime = 362.milliseconds //312.milliseconds
    )
    manned_turret.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 5
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    manned_turret.Geometry = GeometryForm.representByCylinder(radius = 1.2695f, height = 4.042f)

    vanu_sentry_turret.Name = "vanu_sentry_turret"
    vanu_sentry_turret.MaxHealth = 1500
    vanu_sentry_turret.Damageable = true
    vanu_sentry_turret.DamageDisablesAt = 0
    vanu_sentry_turret.Repairable = true
    vanu_sentry_turret.autoRepair = AutoRepairStats(3.27272f, 10000, 1000, 0.05f)
    vanu_sentry_turret.RepairIfDestroyed = true
    vanu_sentry_turret.WeaponPaths += 1                     -> new mutable.HashMap()
    vanu_sentry_turret.WeaponPaths(1) += TurretUpgrade.None -> vanu_sentry_turret_weapon
    vanu_sentry_turret.Seats += 0 -> new SeatDefinition()
    vanu_sentry_turret.controlledWeapons(seat = 0, weapon = 1)
    vanu_sentry_turret.MountPoints += 1                     -> MountInfo(0)
    vanu_sentry_turret.MountPoints += 2                     -> MountInfo(0)
    vanu_sentry_turret.FactionLocked = false
    vanu_sentry_turret.ReserveAmmunition = false
    vanu_sentry_turret.Geometry = GeometryForm.representByCylinder(radius = 1.76311f, height = 3.984375f)

    painbox.Name = "painbox"
    painbox.alwaysOn = false
    painbox.sphereOffset = Vector3(0, 0, -0.4f)
    painbox.Damageable = false
    painbox.Repairable = false
    painbox.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 0
      DamageToHealthOnly = true
    }

    painbox_continuous.Name = "painbox_continuous"
    painbox_continuous.sphereOffset = Vector3(0, 0, -0.4f)
    painbox_continuous.Damageable = false
    painbox_continuous.Repairable = false
    painbox_continuous.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 0
      DamageToHealthOnly = true
    }

    painbox_door_radius.Name = "painbox_door_radius"
    painbox_door_radius.alwaysOn = false
    painbox_door_radius.sphereOffset = Vector3(0, 0, -0.4f)
    painbox_door_radius.hasNearestDoorDependency = true
    painbox_door_radius.Damageable = false
    painbox_door_radius.Repairable = false
    painbox_door_radius.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 10f * 0.6928f
      DamageToHealthOnly = true
    }

    painbox_door_radius_continuous.Name = "painbox_door_radius_continuous"
    painbox_door_radius_continuous.sphereOffset = Vector3(0, 0, -0.4f)
    painbox_door_radius_continuous.hasNearestDoorDependency = true
    painbox_door_radius_continuous.Damageable = false
    painbox_door_radius_continuous.Repairable = false
    painbox_door_radius_continuous.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 10f * 0.6928f
      DamageToHealthOnly = true
    }

    painbox_radius.Name = "painbox_radius"
    painbox_radius.alwaysOn = false
    painbox_radius.sphereOffset = Vector3(0, 0, -0.4f)
    painbox_radius.Damageable = false
    painbox_radius.Repairable = false
    painbox_radius.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 10f * 0.6928f
      DamageToHealthOnly = true
    }

    painbox_radius_continuous.Name = "painbox_radius_continuous"
    painbox_radius_continuous.Damageable = false
    painbox_radius_continuous.Repairable = false
    painbox_radius_continuous.innateDamage = new DamageWithPosition {
      Damage0 = 2
      DamageRadius = 8.55f
      DamageToHealthOnly = true
    }

    gen_control.Name = "gen_control"
    gen_control.Damageable = false
    gen_control.Repairable = false

    generator.Name = "generator"
    generator.MaxHealth = 4000
    generator.Damageable = true
    generator.DamageableByFriendlyFire = false
    generator.Repairable = true
    generator.autoRepair = AutoRepairStats(0.77775f, 5000, 875, 1)
    generator.RepairDistance = 13.5f
    generator.RepairIfDestroyed = true
    generator.Subtract.Damage1 = 9
    generator.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 99999
      DamageRadiusMin = 15
      DamageRadius = 15.1f
      DamageAtEdge = 0.000011f
      Modifiers = ExplodingRadialDegrade
      //damage is 99999 at 15m, dropping rapidly to ~1 at 15.1m
    }
    generator.Geometry = GeometryForm.representByCylinder(radius = 1.2617f, height = 9.14063f)

    obbasemesh.Name = "obbasemesh"
    obbasemesh.Descriptor = "orbital_shuttle_pad"
    obbasemesh.Damageable = false
    obbasemesh.Repairable = false

    stationaryteleportpad.Name = "stationaryteleportpad"
    stationaryteleportpad.interference = InterferenceRange(deployables = 5.5f)

    zipline.Name = "zipline"
    zipline.interference = InterferenceRange(deployables = 5.5f)
  }
}
