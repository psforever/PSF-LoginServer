// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.ce.DeployableCategory
import net.psforever.objects.definition.DeployAnimation
import net.psforever.objects.definition.converter.{FieldTurretConverter, InternalTelepadDeployableConverter, TelepadDeployableConverter}
import net.psforever.objects.equipment.{EffectTarget, TargetValidation}
import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.geometry.d3.VolumetricGeometry
import net.psforever.objects.serverobject.deploy.InterferenceRange
import net.psforever.objects.serverobject.mount.{MountInfo, SeatDefinition}
import net.psforever.objects.serverobject.turret.{AutoChecks, AutoCooldowns, AutoRanges, Automation, TurretUpgrade}
import net.psforever.objects.vital.{CollisionXYData, CollisionZData, ComplexDeployableResolutions, SimpleResolutions}
import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.collision.TrapCollisionDamageMultiplier
import net.psforever.objects.vital.etc.ExplodingRadialDegrade
import net.psforever.objects.vital.prop.DamageWithPosition

import scala.collection.mutable
import scala.concurrent.duration._

object GlobalDefinitionsDeployable {
  import GlobalDefinitions._

  /**
   * Initialize `Deployable` globals.
   */
  def init(): Unit = {
    val mine: Any => VolumetricGeometry = GeometryForm.representByCylinder(radius = 0.1914f, height = 0.0957f)
    val smallTurret: Any => VolumetricGeometry = GeometryForm.representByCylinder(radius = 0.48435f, height = 1.23438f)
    val sensor: Any => VolumetricGeometry = GeometryForm.representByCylinder(radius = 0.1914f, height = 1.21875f)
    val largeTurret: Any => VolumetricGeometry = GeometryForm.representByCylinder(radius = 0.8437f, height = 2.29687f)

    boomer.Name = "boomer"
    boomer.Descriptor = "Boomers"
    boomer.MaxHealth = 50
    boomer.Damageable = true
    boomer.DamageableByFriendlyFire = false
    boomer.Repairable = false
    boomer.DeployCategory = DeployableCategory.Boomers
    boomer.DeployTime = Duration.create(1000, "ms")
    boomer.deployAnimation = DeployAnimation.Standard
    boomer.interference = InterferenceRange(main = 0.2f)
    boomer.Stable = true
    boomer.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.Splash
      SympatheticExplosion = true
      Damage0 = 250
      Damage1 = 750
      Damage2 = 400
      Damage3 = 400
      Damage4 = 1850
      DamageRadius = 5.1f
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    boomer.Geometry = mine

    he_mine.Name = "he_mine"
    he_mine.Descriptor = "Mines"
    he_mine.MaxHealth = 25
    he_mine.Damageable = true
    he_mine.DamageableByFriendlyFire = false
    he_mine.Repairable = false
    he_mine.DeployTime = Duration.create(1000, "ms")
    he_mine.deployAnimation = DeployAnimation.Standard
    he_mine.interference = InterferenceRange(main = 7f, sharedGroupId = 1, shared = 7f, deployables = 0.1f)
    he_mine.triggerRadius = 3f
    he_mine.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.Splash
      SympatheticExplosion = true
      Damage0 = 100
      Damage1 = 750
      Damage2 = 400
      Damage3 = 565
      Damage4 = 1600
      DamageRadius = 6.6f
      DamageAtEdge = 0.25f
      Modifiers = ExplodingRadialDegrade
    }
    he_mine.Geometry = mine

    jammer_mine.Name = "jammer_mine"
    jammer_mine.Descriptor = "JammerMines"
    jammer_mine.MaxHealth = 50
    jammer_mine.Damageable = true
    jammer_mine.DamageableByFriendlyFire = false
    jammer_mine.Repairable = false
    jammer_mine.DeployTime = Duration.create(1000, "ms")
    jammer_mine.deployAnimation = DeployAnimation.Standard
    jammer_mine.interference = InterferenceRange(main = 7f, sharedGroupId = 1, shared = 7f, deployables = 0.1f)
    jammer_mine.DetonateOnJamming = false
    jammer_mine.triggerRadius = 3f
    jammer_mine.Stable = true
    jammer_mine.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.Splash
      Damage0 = 0
      DamageRadius = 10f
      DamageAtEdge = 1.0f
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Player,
        EffectTarget.Validation.Player
      ) -> 1000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Vehicle,
        EffectTarget.Validation.AMS
      ) -> 5000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Deployable,
        EffectTarget.Validation.MotionSensor
      ) -> 30000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Deployable,
        EffectTarget.Validation.Spitfire
      ) -> 30000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Turret,
        EffectTarget.Validation.Turret
      ) -> 30000
      JammedEffectDuration += TargetValidation(
        EffectTarget.Category.Vehicle,
        EffectTarget.Validation.VehicleNotAMS
      ) -> 10000
    }
    jammer_mine.Geometry = mine

    spitfire_turret.Name = "spitfire_turret"
    spitfire_turret.Descriptor = "Spitfires"
    spitfire_turret.MaxHealth = 100
    spitfire_turret.Damageable = true
    spitfire_turret.Repairable = true
    spitfire_turret.RepairIfDestroyed = false
    spitfire_turret.WeaponPaths += 1 -> new mutable.HashMap()
    spitfire_turret.WeaponPaths(1) += TurretUpgrade.None -> spitfire_weapon
    spitfire_turret.ReserveAmmunition = false
    spitfire_turret.DeployCategory = DeployableCategory.SmallTurrets
    spitfire_turret.DeployTime = Duration.create(5000, "ms")
    spitfire_turret.Model = ComplexDeployableResolutions.calculate
    spitfire_turret.deployAnimation = DeployAnimation.Standard
    spitfire_turret.interference = InterferenceRange(main = 25f, sharedGroupId = 2, shared = 25f, deployables = 0.1f)
    spitfire_turret.AutoFire = Automation(
      AutoRanges(
        detection = 75f,
        trigger = 50f,
        escape = 50f
      ),
      AutoChecks(
        validation = List(
          EffectTarget.Validation.SmallRoboticsTurretValidatePlayerTarget,
          EffectTarget.Validation.SmallRoboticsTurretValidateMaxTarget,
          EffectTarget.Validation.SmallRoboticsTurretValidateGroundVehicleTarget,
          EffectTarget.Validation.SmallRoboticsTurretValidateAircraftTarget,
          EffectTarget.Validation.AutoTurretValidateMountableEntityTarget
        )
      ),
      retaliatoryDelay = 2000L, //8000L
      refireTime = 200.milliseconds //150.milliseconds
    )
    spitfire_turret.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    spitfire_turret.Geometry = smallTurret
    spitfire_turret.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    spitfire_turret.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    spitfire_turret.mass = 5f

    spitfire_cloaked.Name = "spitfire_cloaked"
    spitfire_cloaked.Descriptor = "CloakingSpitfires"
    spitfire_cloaked.MaxHealth = 100
    spitfire_cloaked.Damageable = true
    spitfire_cloaked.Repairable = true
    spitfire_cloaked.RepairIfDestroyed = false
    spitfire_cloaked.WeaponPaths += 1 -> new mutable.HashMap()
    spitfire_cloaked.WeaponPaths(1) += TurretUpgrade.None -> spitfire_weapon
    spitfire_cloaked.ReserveAmmunition = false
    spitfire_cloaked.DeployCategory = DeployableCategory.SmallTurrets
    spitfire_cloaked.DeployTime = Duration.create(5000, "ms")
    spitfire_cloaked.deployAnimation = DeployAnimation.Standard
    spitfire_cloaked.interference = InterferenceRange(main = 25f, sharedGroupId = 2, shared = 25f, deployables = 0.1f)
    spitfire_cloaked.Model = ComplexDeployableResolutions.calculate
    spitfire_cloaked.AutoFire = Automation(
      AutoRanges(
        detection = 75f,
        trigger = 50f,
        escape = 75f
      ),
      AutoChecks(
        validation = List(
          EffectTarget.Validation.SmallRoboticsTurretValidatePlayerTarget,
          EffectTarget.Validation.SmallRoboticsTurretValidateMaxTarget,
          EffectTarget.Validation.SmallRoboticsTurretValidateGroundVehicleTarget,
          EffectTarget.Validation.SmallRoboticsTurretValidateAircraftTarget,
          EffectTarget.Validation.AutoTurretValidateMountableEntityTarget
        )
      ),
      cooldowns = AutoCooldowns(
        targetSelect = 0L,
        missedShot = 0L
      ),
      detectionSweepTime = 500.milliseconds,
      retaliatoryDelay = 1L, //8000L
      retaliationOverridesTarget = false,
      refireTime = 200.milliseconds //150.milliseconds
    )
    spitfire_cloaked.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 50
      Damage1 = 75
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    spitfire_cloaked.Geometry = smallTurret
    spitfire_cloaked.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    spitfire_cloaked.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    spitfire_cloaked.mass = 5f

    spitfire_aa.Name = "spitfire_aa"
    spitfire_aa.Descriptor = "FlakSpitfires"
    spitfire_aa.MaxHealth = 100
    spitfire_aa.Damageable = true
    spitfire_aa.Repairable = true
    spitfire_aa.RepairIfDestroyed = false
    spitfire_aa.WeaponPaths += 1 -> new mutable.HashMap()
    spitfire_aa.WeaponPaths(1) += TurretUpgrade.None -> spitfire_aa_weapon
    spitfire_aa.ReserveAmmunition = false
    spitfire_aa.DeployCategory = DeployableCategory.SmallTurrets
    spitfire_aa.DeployTime = Duration.create(5000, "ms")
    spitfire_aa.deployAnimation = DeployAnimation.Standard
    spitfire_aa.interference = InterferenceRange(main = 25f, sharedGroupId = 2, shared = 25f, deployables = 0.1f)
    spitfire_aa.Model = ComplexDeployableResolutions.calculate
    spitfire_aa.AutoFire = Automation(
      AutoRanges(
        detection = 125f,
        trigger = 100f,
        escape = 200f
      ),
      AutoChecks(
        validation = List(EffectTarget.Validation.SmallRoboticsTurretValidateAircraftTarget)
      ),
      retaliatoryDelay = 2000L, //8000L
      retaliationOverridesTarget = false,
      refireTime = 0.seconds, //300.milliseconds
      cylindrical = true,
      cylindricalExtraHeight = 50f
    )
    spitfire_aa.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    spitfire_aa.Geometry = smallTurret
    spitfire_aa.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    spitfire_aa.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    spitfire_aa.mass = 5f

    motionalarmsensor.Name = "motionalarmsensor"
    motionalarmsensor.Descriptor = "MotionSensors"
    motionalarmsensor.MaxHealth = 100
    motionalarmsensor.Damageable = true
    motionalarmsensor.Repairable = true
    motionalarmsensor.RepairIfDestroyed = false
    motionalarmsensor.DeployTime = Duration.create(1000, "ms")
    motionalarmsensor.deployAnimation = DeployAnimation.Standard
    motionalarmsensor.interference = InterferenceRange(main = 25f, deployables = 0.1f)
    motionalarmsensor.Geometry = sensor

    sensor_shield.Name = "sensor_shield"
    sensor_shield.Descriptor = "SensorShields"
    sensor_shield.MaxHealth = 100
    sensor_shield.Damageable = true
    sensor_shield.Repairable = true
    sensor_shield.RepairIfDestroyed = false
    sensor_shield.DeployTime = Duration.create(5000, "ms")
    sensor_shield.deployAnimation = DeployAnimation.Standard
    sensor_shield.interference = InterferenceRange(main = 20f, deployables = 0.1f)
    sensor_shield.Geometry = sensor

    tank_traps.Name = "tank_traps"
    tank_traps.Descriptor = "TankTraps"
    tank_traps.MaxHealth = 5000
    tank_traps.Damageable = true
    tank_traps.Repairable = true
    tank_traps.RepairIfDestroyed = false
    tank_traps.DeployCategory = DeployableCategory.TankTraps
    tank_traps.DeployTime = Duration.create(6000, "ms")
    tank_traps.deployAnimation = DeployAnimation.Fdu
    tank_traps.interference = InterferenceRange(main = 3.5f, sharedGroupId = 3, shared = 60f, deployables = 3f)
    //todo what is tank_traps interference2 60
    //tank_traps do not explode
    tank_traps.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 10
      Damage1 = 10
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    tank_traps.Geometry = GeometryForm.representByCylinder(radius = 2.89680997f, height = 3.57812f)
    tank_traps.collision.xy = CollisionXYData(Array((0.01f, 5), (0.02f, 10), (0.03f, 15), (0.04f, 20), (0.05f, 25)))
    tank_traps.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    tank_traps.Modifiers = TrapCollisionDamageMultiplier(5f) //10f
    tank_traps.mass = 600f

    val fieldTurretConverter = new FieldTurretConverter
    portable_manned_turret.Name = "portable_manned_turret"
    portable_manned_turret.Descriptor = "FieldTurrets"
    portable_manned_turret.MaxHealth = 1000
    portable_manned_turret.Damageable = true
    portable_manned_turret.Repairable = true
    portable_manned_turret.RepairIfDestroyed = false
    portable_manned_turret.MaxShields = 0//200
    portable_manned_turret.WeaponPaths += 1 -> new mutable.HashMap()
    portable_manned_turret.WeaponPaths(1) += TurretUpgrade.None -> energy_gun
    portable_manned_turret.Seats += 0 -> new SeatDefinition()
    portable_manned_turret.controlledWeapons(seat = 0, weapon = 1)
    portable_manned_turret.MountPoints += 1 -> MountInfo(0)
    portable_manned_turret.MountPoints += 2 -> MountInfo(0)
    portable_manned_turret.ReserveAmmunition = true
    portable_manned_turret.FactionLocked = true
    portable_manned_turret.Packet = fieldTurretConverter
    portable_manned_turret.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret.deployAnimation = DeployAnimation.Fdu
    portable_manned_turret.interference = InterferenceRange(main = 60f, sharedGroupId = 3, shared = 40f, deployables = 2.5f)
    portable_manned_turret.Model = ComplexDeployableResolutions.calculate
    portable_manned_turret.RadiationShielding = 0.5f
    portable_manned_turret.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    portable_manned_turret.Geometry = largeTurret
    portable_manned_turret.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    portable_manned_turret.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    portable_manned_turret.mass = 100f

    portable_manned_turret_nc.Name = "portable_manned_turret_nc"
    portable_manned_turret_nc.Descriptor = "FieldTurrets"
    portable_manned_turret_nc.MaxHealth = 1000
    portable_manned_turret_nc.Damageable = true
    portable_manned_turret_nc.Repairable = true
    portable_manned_turret_nc.RepairIfDestroyed = false
    portable_manned_turret_nc.MaxShields = 0//200
    portable_manned_turret_nc.WeaponPaths += 1 -> new mutable.HashMap()
    portable_manned_turret_nc.WeaponPaths(1) += TurretUpgrade.None -> energy_gun_nc
    portable_manned_turret_nc.Seats += 0 -> new SeatDefinition()
    portable_manned_turret_nc.controlledWeapons(seat = 0, weapon = 1)
    portable_manned_turret_nc.MountPoints += 1 -> MountInfo(0)
    portable_manned_turret_nc.MountPoints += 2 -> MountInfo(0)
    portable_manned_turret_nc.ReserveAmmunition = true
    portable_manned_turret_nc.FactionLocked = true
    portable_manned_turret_nc.Packet = fieldTurretConverter
    portable_manned_turret_nc.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret_nc.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret_nc.deployAnimation = DeployAnimation.Fdu
    portable_manned_turret_nc.interference = InterferenceRange(main = 60f, sharedGroupId = 3, shared = 40f, deployables = 2.5f)
    portable_manned_turret_nc.Model = ComplexDeployableResolutions.calculate
    portable_manned_turret_nc.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    portable_manned_turret_nc.Geometry = largeTurret
    portable_manned_turret_nc.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    portable_manned_turret_nc.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    portable_manned_turret_nc.mass = 100f

    portable_manned_turret_tr.Name = "portable_manned_turret_tr"
    portable_manned_turret_tr.Descriptor = "FieldTurrets"
    portable_manned_turret_tr.MaxHealth = 1000
    portable_manned_turret_tr.Damageable = true
    portable_manned_turret_tr.Repairable = true
    portable_manned_turret_tr.RepairIfDestroyed = false
    portable_manned_turret_tr.MaxShields = 0//200
    portable_manned_turret_tr.WeaponPaths += 1 -> new mutable.HashMap()
    portable_manned_turret_tr.WeaponPaths(1) += TurretUpgrade.None -> energy_gun_tr
    portable_manned_turret_tr.Seats += 0 -> new SeatDefinition()
    portable_manned_turret_tr.controlledWeapons(seat = 0, weapon = 1)
    portable_manned_turret_tr.MountPoints += 1 -> MountInfo(0)
    portable_manned_turret_tr.MountPoints += 2 -> MountInfo(0)
    portable_manned_turret_tr.ReserveAmmunition = true
    portable_manned_turret_tr.FactionLocked = true
    portable_manned_turret_tr.Packet = fieldTurretConverter
    portable_manned_turret_tr.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret_tr.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret_tr.deployAnimation = DeployAnimation.Fdu
    portable_manned_turret_tr.interference = InterferenceRange(main = 60f, sharedGroupId = 3, shared = 40f, deployables = 2.5f)
    portable_manned_turret_tr.Model = ComplexDeployableResolutions.calculate
    portable_manned_turret_tr.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    portable_manned_turret_tr.Geometry = largeTurret
    portable_manned_turret_tr.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    portable_manned_turret_tr.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    portable_manned_turret_tr.mass = 100f

    portable_manned_turret_vs.Name = "portable_manned_turret_vs"
    portable_manned_turret_vs.Descriptor = "FieldTurrets"
    portable_manned_turret_vs.MaxHealth = 1000
    portable_manned_turret_vs.Damageable = true
    portable_manned_turret_vs.Repairable = true
    portable_manned_turret_vs.RepairIfDestroyed = false
    portable_manned_turret_vs.MaxShields = 0//200
    portable_manned_turret_vs.WeaponPaths += 1 -> new mutable.HashMap()
    portable_manned_turret_vs.WeaponPaths(1) += TurretUpgrade.None -> energy_gun_vs
    portable_manned_turret_vs.Seats += 0 -> new SeatDefinition()
    portable_manned_turret_vs.controlledWeapons(seat = 0, weapon = 1)
    portable_manned_turret_vs.MountPoints += 1 -> MountInfo(0)
    portable_manned_turret_vs.MountPoints += 2 -> MountInfo(0)
    portable_manned_turret_vs.ReserveAmmunition = true
    portable_manned_turret_vs.FactionLocked = true
    portable_manned_turret_vs.Packet = fieldTurretConverter
    portable_manned_turret_vs.DeployCategory = DeployableCategory.FieldTurrets
    portable_manned_turret_vs.DeployTime = Duration.create(6000, "ms")
    portable_manned_turret_vs.deployAnimation = DeployAnimation.Fdu
    portable_manned_turret_vs.interference = InterferenceRange(main = 60f, sharedGroupId = 3, shared = 40f, deployables = 2.5f)
    portable_manned_turret_vs.Model = ComplexDeployableResolutions.calculate
    portable_manned_turret_vs.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.1f
      Modifiers = ExplodingRadialDegrade
    }
    portable_manned_turret_vs.Geometry = largeTurret
    portable_manned_turret_vs.collision.xy = CollisionXYData(Array((0.01f, 10), (0.02f, 40), (0.03f, 60), (0.04f, 80), (0.05f, 100)))
    portable_manned_turret_vs.collision.z = CollisionZData(Array((4f, 10), (4.25f, 40), (4.5f, 60), (4.75f, 80), (5f, 100)))
    portable_manned_turret_vs.mass = 100f

    deployable_shield_generator.Name = "deployable_shield_generator"
    deployable_shield_generator.Descriptor = "ShieldGenerators"
    deployable_shield_generator.MaxHealth = 1700
    deployable_shield_generator.Damageable = true
    deployable_shield_generator.Repairable = true
    deployable_shield_generator.RepairIfDestroyed = false
    deployable_shield_generator.DeployTime = Duration.create(6000, "ms")
    deployable_shield_generator.deployAnimation = DeployAnimation.Fdu
    deployable_shield_generator.interference = InterferenceRange(main = 125f, sharedGroupId = 3, shared = 60f, deployables = 2f)
    deployable_shield_generator.Model = ComplexDeployableResolutions.calculate
    deployable_shield_generator.Geometry = GeometryForm.representByCylinder(radius = 0.6562f, height = 2.17188f)

    router_telepad_deployable.Name = "router_telepad_deployable"
    router_telepad_deployable.MaxHealth = 100
    router_telepad_deployable.Damageable = true
    router_telepad_deployable.Repairable = false
    router_telepad_deployable.DeployTime = Duration.create(1, "ms")
    router_telepad_deployable.DeployCategory = DeployableCategory.Telepads
    router_telepad_deployable.Packet = new TelepadDeployableConverter
    router_telepad_deployable.Model = SimpleResolutions.calculate
    router_telepad_deployable.Geometry = GeometryForm.representByRaisedSphere(radius = 1.2344f)

    internal_router_telepad_deployable.Name = "router_telepad_deployable"
    internal_router_telepad_deployable.MaxHealth = 1
    internal_router_telepad_deployable.Damageable = false
    internal_router_telepad_deployable.Repairable = false
    internal_router_telepad_deployable.DeployTime = Duration.create(1, "ms")
    internal_router_telepad_deployable.DeployCategory = DeployableCategory.Telepads
    internal_router_telepad_deployable.Packet = new InternalTelepadDeployableConverter
  }
}
