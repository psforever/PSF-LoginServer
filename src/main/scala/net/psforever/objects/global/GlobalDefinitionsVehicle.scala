// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.definition._
import net.psforever.objects.definition.converter._
import net.psforever.objects.geometry.GeometryForm
import net.psforever.objects.inventory.InventoryTile
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.deploy.InterferenceRange
import net.psforever.objects.serverobject.mount._
import net.psforever.objects.vehicles.{DestroyedVehicle, UtilityType, VehicleSubsystemEntry}
import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.damage._
import net.psforever.objects.vital.etc.{ShieldAgainstRadiation => _, _}
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.vital._
import net.psforever.types.Vector3

import scala.concurrent.duration._

object GlobalDefinitionsVehicle {
  import GlobalDefinitions._

  def init(): Unit = {
    init_ground_vehicles()
    init_flight_vehicles()
    init_bfr_vehicles()
  }

  /**
   * Initialize land-based `VehicleDefinition` globals.
   */
  private def init_ground_vehicles(): Unit = {
    val atvForm       = GeometryForm.representByCylinder(radius = 1.1797f, height = 1.1875f) _
    val delivererForm = GeometryForm.representByCylinder(radius = 2.46095f, height = 2.40626f) _ //TODO hexahedron
    val apcForm       = GeometryForm.representByCylinder(radius = 4.6211f, height = 3.90626f) _  //TODO hexahedron

    val driverSeat = new SeatDefinition() {
      restriction = NoReinforcedOrMax
    }
    val normalSeat = new SeatDefinition()
    val bailableSeat = new SeatDefinition() {
      bailable = true
    }
    val maxOnlySeat = new SeatDefinition() {
      restriction = MaxOnly
    }

    val controlSubsystem = List(VehicleSubsystemEntry.Controls)

    fury.Name = "fury"
    fury.MaxHealth = 650
    fury.Damageable = true
    fury.Repairable = true
    fury.RepairIfDestroyed = false
    fury.MaxShields = 130
    fury.Seats += 0             -> bailableSeat
    fury.controlledWeapons(seat = 0, weapon = 1)
    fury.Weapons += 1           -> fury_weapon_systema
    fury.MountPoints += 1       -> MountInfo(0)
    fury.MountPoints += 2       -> MountInfo(0)
    fury.subsystems = controlSubsystem
    fury.TrunkSize = InventoryTile.Tile1111
    fury.TrunkOffset = 30
    fury.TrunkLocation = Vector3(-1.71f, 0f, 0f)
    fury.AutoPilotSpeeds = (24, 10)
    fury.DestroyedModel = Some(DestroyedVehicle.QuadAssault)
    fury.JackingDuration = Array(0, 10, 3, 2)
    fury.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 225
      DamageRadius = 5
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    fury.DrownAtMaxDepth = true
    fury.MaxDepth = 1.3f
    fury.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    fury.Geometry = atvForm
    fury.collision.avatarCollisionDamageMax = 35
    fury.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    fury.collision.z = CollisionZData(Array((8f, 1), (24f, 35), (40f, 100), (48f, 175), (52f, 350)))
    fury.maxForwardSpeed = 90f
    fury.mass = 32.1f

    quadassault.Name = "quadassault" // Basilisk
    quadassault.MaxHealth = 650
    quadassault.Damageable = true
    quadassault.Repairable = true
    quadassault.RepairIfDestroyed = false
    quadassault.MaxShields = 130
    quadassault.Seats += 0             -> bailableSeat
    quadassault.controlledWeapons(seat = 0, weapon = 1)
    quadassault.Weapons += 1           -> quadassault_weapon_system
    quadassault.MountPoints += 1       -> MountInfo(0)
    quadassault.MountPoints += 2       -> MountInfo(0)
    quadassault.subsystems = controlSubsystem
    quadassault.TrunkSize = InventoryTile.Tile1111
    quadassault.TrunkOffset = 30
    quadassault.TrunkLocation = Vector3(-1.71f, 0f, 0f)
    quadassault.AutoPilotSpeeds = (24, 10)
    quadassault.DestroyedModel = Some(DestroyedVehicle.QuadAssault)
    quadassault.JackingDuration = Array(0, 10, 3, 2)
    quadassault.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 225
      DamageRadius = 5
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    quadassault.DrownAtMaxDepth = true
    quadassault.MaxDepth = 1.3f
    quadassault.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    quadassault.Geometry = atvForm
    quadassault.collision.avatarCollisionDamageMax = 35
    quadassault.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    quadassault.collision.z = CollisionZData(Array((8f, 1), (24f, 35), (40f, 100), (48f, 175), (52f, 350)))
    quadassault.maxForwardSpeed = 90f
    quadassault.mass = 32.1f

    quadstealth.Name = "quadstealth" // Wraith
    quadstealth.MaxHealth = 650
    quadstealth.Damageable = true
    quadstealth.Repairable = true
    quadstealth.RepairIfDestroyed = false
    quadstealth.MaxShields = 130
    quadstealth.CanCloak = true
    quadstealth.Seats += 0 -> bailableSeat
    quadstealth.CanCloak = true
    quadstealth.MountPoints += 1 -> MountInfo(0)
    quadstealth.MountPoints += 2 -> MountInfo(0)
    quadstealth.subsystems = controlSubsystem
    quadstealth.TrunkSize = InventoryTile.Tile1111
    quadstealth.TrunkOffset = 30
    quadstealth.TrunkLocation = Vector3(-1.71f, 0f, 0f)
    quadstealth.AutoPilotSpeeds = (24, 10)
    quadstealth.DestroyedModel = Some(DestroyedVehicle.QuadStealth)
    quadstealth.JackingDuration = Array(0, 10, 3, 2)
    quadstealth.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 150
      Damage1 = 225
      DamageRadius = 5
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    quadstealth.DrownAtMaxDepth = true
    quadstealth.MaxDepth = 1.25f
    quadstealth.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    quadstealth.Geometry = atvForm
    quadstealth.collision.avatarCollisionDamageMax = 35
    quadstealth.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    quadstealth.collision.z = CollisionZData(Array((8f, 1), (24f, 35), (40f, 100), (48f, 175), (52f, 350)))
    quadstealth.maxForwardSpeed = 90f
    quadstealth.mass = 32.1f

    two_man_assault_buggy.Name = "two_man_assault_buggy" // Harasser
    two_man_assault_buggy.MaxHealth = 1250
    two_man_assault_buggy.Damageable = true
    two_man_assault_buggy.Repairable = true
    two_man_assault_buggy.RepairIfDestroyed = false
    two_man_assault_buggy.MaxShields = 250
    two_man_assault_buggy.Seats += 0             -> bailableSeat
    two_man_assault_buggy.Seats += 1             -> bailableSeat
    two_man_assault_buggy.controlledWeapons(seat = 1, weapon = 2)
    two_man_assault_buggy.Weapons += 2           -> chaingun_p
    two_man_assault_buggy.MountPoints += 1       -> MountInfo(0)
    two_man_assault_buggy.MountPoints += 2       -> MountInfo(1)
    two_man_assault_buggy.subsystems = controlSubsystem
    two_man_assault_buggy.TrunkSize = InventoryTile.Tile1511
    two_man_assault_buggy.TrunkOffset = 30
    two_man_assault_buggy.TrunkLocation = Vector3(-2.5f, 0f, 0f)
    two_man_assault_buggy.AutoPilotSpeeds = (22, 8)
    two_man_assault_buggy.DestroyedModel = Some(DestroyedVehicle.TwoManAssaultBuggy)
    two_man_assault_buggy.RadiationShielding = 0.5f
    two_man_assault_buggy.JackingDuration = Array(0, 15, 5, 3)
    two_man_assault_buggy.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    two_man_assault_buggy.DrownAtMaxDepth = true
    two_man_assault_buggy.MaxDepth = 1.5f
    two_man_assault_buggy.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    two_man_assault_buggy.Geometry = GeometryForm.representByCylinder(radius = 2.10545f, height = 1.59376f)
    two_man_assault_buggy.collision.avatarCollisionDamageMax = 75
    two_man_assault_buggy.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    two_man_assault_buggy.collision.z = CollisionZData(Array((7f, 1), (21f, 50), (35f, 150), (42f, 300), (45.5f, 600)))
    two_man_assault_buggy.maxForwardSpeed = 85f
    two_man_assault_buggy.mass = 52.4f

    skyguard.Name = "skyguard"
    skyguard.MaxHealth = 1000
    skyguard.Damageable = true
    skyguard.Repairable = true
    skyguard.RepairIfDestroyed = false
    skyguard.MaxShields = 200
    skyguard.Seats += 0             -> bailableSeat
    skyguard.Seats += 1             -> bailableSeat
    skyguard.controlledWeapons(seat = 1, weapon = 2)
    skyguard.Weapons += 2           -> skyguard_weapon_system
    skyguard.MountPoints += 1       -> MountInfo(0)
    skyguard.MountPoints += 2       -> MountInfo(0)
    skyguard.MountPoints += 3       -> MountInfo(1)
    skyguard.subsystems = controlSubsystem
    skyguard.TrunkSize = InventoryTile.Tile1511
    skyguard.TrunkOffset = 30
    skyguard.TrunkLocation = Vector3(2.5f, 0f, 0f)
    skyguard.AutoPilotSpeeds = (22, 8)
    skyguard.DestroyedModel = Some(DestroyedVehicle.Skyguard)
    skyguard.JackingDuration = Array(0, 15, 5, 3)
    skyguard.RadiationShielding = 0.5f
    skyguard.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    skyguard.DrownAtMaxDepth = true
    skyguard.MaxDepth = 1.5f
    skyguard.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    skyguard.Geometry = GeometryForm.representByCylinder(radius = 1.8867f, height = 1.4375f)
    skyguard.collision.avatarCollisionDamageMax = 100
    skyguard.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    skyguard.collision.z = CollisionZData(Array((7f, 1), (21f, 50), (35f, 150), (42f, 300), (45.4f, 600)))
    skyguard.maxForwardSpeed = 90f
    skyguard.mass = 78.9f

    threemanheavybuggy.Name = "threemanheavybuggy" // Marauder
    threemanheavybuggy.MaxHealth = 1700
    threemanheavybuggy.Damageable = true
    threemanheavybuggy.Repairable = true
    threemanheavybuggy.RepairIfDestroyed = false
    threemanheavybuggy.MaxShields = 340
    threemanheavybuggy.Seats += 0             -> bailableSeat
    threemanheavybuggy.Seats += 1             -> bailableSeat
    threemanheavybuggy.Seats += 2             -> bailableSeat
    threemanheavybuggy.controlledWeapons(seat = 1, weapon = 3)
    threemanheavybuggy.controlledWeapons(seat = 2, weapon = 4)
    threemanheavybuggy.Weapons += 3           -> chaingun_p
    threemanheavybuggy.Weapons += 4           -> grenade_launcher_marauder
    threemanheavybuggy.MountPoints += 1       -> MountInfo(0)
    threemanheavybuggy.MountPoints += 2       -> MountInfo(1)
    threemanheavybuggy.MountPoints += 3       -> MountInfo(2)
    threemanheavybuggy.subsystems = controlSubsystem
    threemanheavybuggy.TrunkSize = InventoryTile.Tile1511
    threemanheavybuggy.TrunkOffset = 30
    threemanheavybuggy.TrunkLocation = Vector3(3.01f, 0f, 0f)
    threemanheavybuggy.AutoPilotSpeeds = (22, 8)
    threemanheavybuggy.DestroyedModel = Some(DestroyedVehicle.ThreeManHeavyBuggy)
    threemanheavybuggy.Subtract.Damage1 = 5
    threemanheavybuggy.JackingDuration = Array(0, 20, 7, 5)
    threemanheavybuggy.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    threemanheavybuggy.DrownAtMaxDepth = true
    threemanheavybuggy.MaxDepth = 1.83f
    threemanheavybuggy.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    threemanheavybuggy.Geometry = GeometryForm.representByCylinder(radius = 2.1953f, height = 2.03125f)
    threemanheavybuggy.collision.avatarCollisionDamageMax = 100
    threemanheavybuggy.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 15), (0.5f, 30), (0.75f, 60), (1f, 80)))
    threemanheavybuggy.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 300), (39f, 900)))
    threemanheavybuggy.maxForwardSpeed = 80f
    threemanheavybuggy.mass = 96.3f

    twomanheavybuggy.Name = "twomanheavybuggy" // Enforcer
    twomanheavybuggy.MaxHealth = 1800
    twomanheavybuggy.Damageable = true
    twomanheavybuggy.Repairable = true
    twomanheavybuggy.RepairIfDestroyed = false
    twomanheavybuggy.MaxShields = 360
    twomanheavybuggy.Seats += 0             -> bailableSeat
    twomanheavybuggy.Seats += 1             -> bailableSeat
    twomanheavybuggy.controlledWeapons(seat = 1, weapon = 2)
    twomanheavybuggy.Weapons += 2           -> advanced_missile_launcher_t
    twomanheavybuggy.MountPoints += 1       -> MountInfo(0)
    twomanheavybuggy.MountPoints += 2       -> MountInfo(1)
    twomanheavybuggy.subsystems = controlSubsystem
    twomanheavybuggy.TrunkSize = InventoryTile.Tile1511
    twomanheavybuggy.TrunkOffset = 30
    twomanheavybuggy.TrunkLocation = Vector3(-0.23f, -2.05f, 0f)
    twomanheavybuggy.AutoPilotSpeeds = (22, 8)
    twomanheavybuggy.DestroyedModel = Some(DestroyedVehicle.TwoManHeavyBuggy)
    twomanheavybuggy.RadiationShielding = 0.5f
    twomanheavybuggy.Subtract.Damage1 = 5
    twomanheavybuggy.JackingDuration = Array(0, 20, 7, 5)
    twomanheavybuggy.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 8
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    twomanheavybuggy.DrownAtMaxDepth = true
    twomanheavybuggy.MaxDepth = 1.95f
    twomanheavybuggy.UnderwaterLifespan(suffocation = 5000L, recovery = 2500L)
    twomanheavybuggy.Geometry = GeometryForm.representByCylinder(radius = 2.60935f, height = 1.79688f)
    twomanheavybuggy.collision.avatarCollisionDamageMax = 100
    twomanheavybuggy.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 12), (0.5f, 30), (0.75f, 55), (1f, 80)))
    twomanheavybuggy.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 300), (39f, 900)))
    twomanheavybuggy.maxForwardSpeed = 80f
    twomanheavybuggy.mass = 83.2f

    twomanhoverbuggy.Name = "twomanhoverbuggy" // Thresher
    twomanhoverbuggy.MaxHealth = 1600
    twomanhoverbuggy.Damageable = true
    twomanhoverbuggy.Repairable = true
    twomanhoverbuggy.RepairIfDestroyed = false
    twomanhoverbuggy.MaxShields = 320
    twomanhoverbuggy.Seats += 0             -> bailableSeat
    twomanhoverbuggy.Seats += 1             -> bailableSeat
    twomanhoverbuggy.controlledWeapons(seat = 1, weapon = 2)
    twomanhoverbuggy.Weapons += 2           -> flux_cannon_thresher
    twomanhoverbuggy.MountPoints += 1       -> MountInfo(0)
    twomanhoverbuggy.MountPoints += 2       -> MountInfo(1)
    twomanhoverbuggy.subsystems = controlSubsystem
    twomanhoverbuggy.TrunkSize = InventoryTile.Tile1511
    twomanhoverbuggy.TrunkOffset = 30
    twomanhoverbuggy.TrunkLocation = Vector3(-3.39f, 0f, 0f)
    twomanhoverbuggy.AutoPilotSpeeds = (22, 10)
    twomanhoverbuggy.DestroyedModel = Some(DestroyedVehicle.TwoManHoverBuggy)
    twomanhoverbuggy.RadiationShielding = 0.5f
    twomanhoverbuggy.Subtract.Damage1 = 5
    twomanhoverbuggy.JackingDuration = Array(0, 20, 7, 5)
    twomanhoverbuggy.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    twomanhoverbuggy.DrownAtMaxDepth = false //true?
    twomanhoverbuggy.MaxDepth = 2f
    twomanhoverbuggy.UnderwaterLifespan(
      suffocation = 45000L,
      recovery = 5000L
    ) //but the thresher hovers over water, so ...?
    twomanhoverbuggy.Geometry = GeometryForm.representByCylinder(radius = 2.1875f, height = 2.01563f)
    twomanhoverbuggy.collision.avatarCollisionDamageMax = 125
    twomanhoverbuggy.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 13), (0.5f, 35), (0.75f, 65), (1f, 90)))
    twomanhoverbuggy.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 300), (39f, 900)))
    twomanhoverbuggy.maxForwardSpeed = 85f
    twomanhoverbuggy.mass = 55.5f

    mediumtransport.Name = "mediumtransport" // Deliverer
    mediumtransport.MaxHealth = 2500
    mediumtransport.Damageable = true
    mediumtransport.Repairable = true
    mediumtransport.RepairIfDestroyed = false
    mediumtransport.MaxShields = 500
    mediumtransport.Seats += 0             -> driverSeat
    mediumtransport.Seats += 1             -> normalSeat
    mediumtransport.Seats += 2             -> normalSeat
    mediumtransport.Seats += 3             -> normalSeat
    mediumtransport.Seats += 4             -> normalSeat
    mediumtransport.controlledWeapons(seat = 1, weapon = 5)
    mediumtransport.controlledWeapons(seat = 2, weapon = 6)
    mediumtransport.Weapons += 5           -> mediumtransport_weapon_systemA
    mediumtransport.Weapons += 6           -> mediumtransport_weapon_systemB
    mediumtransport.MountPoints += 1       -> MountInfo(0)
    mediumtransport.MountPoints += 2       -> MountInfo(1)
    mediumtransport.MountPoints += 3       -> MountInfo(2)
    mediumtransport.MountPoints += 4       -> MountInfo(3)
    mediumtransport.MountPoints += 5       -> MountInfo(4)
    mediumtransport.subsystems = controlSubsystem
    mediumtransport.TrunkSize = InventoryTile.Tile1515
    mediumtransport.TrunkOffset = 30
    mediumtransport.TrunkLocation = Vector3(-3.46f, 0f, 0f)
    mediumtransport.AutoPilotSpeeds = (18, 6)
    mediumtransport.DestroyedModel = Some(DestroyedVehicle.MediumTransport)
    mediumtransport.Subtract.Damage1 = 7
    mediumtransport.JackingDuration = Array(0, 25, 8, 5)
    mediumtransport.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    mediumtransport.DrownAtMaxDepth = false
    mediumtransport.MaxDepth = 2f
    mediumtransport.UnderwaterLifespan(suffocation = -1, recovery = -1)
    mediumtransport.Geometry = delivererForm
    mediumtransport.collision.avatarCollisionDamageMax = 120
    mediumtransport.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 60), (0.75f, 110), (1f, 175)))
    mediumtransport.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 200), (30f, 750), (32.5f, 2000)))
    mediumtransport.maxForwardSpeed = 70f
    mediumtransport.mass = 108.5f

    battlewagon.Name = "battlewagon" // Raider
    battlewagon.MaxHealth = 2500
    battlewagon.Damageable = true
    battlewagon.Repairable = true
    battlewagon.RepairIfDestroyed = false
    battlewagon.MaxShields = 500
    battlewagon.Seats += 0             -> driverSeat
    battlewagon.Seats += 1             -> normalSeat
    battlewagon.Seats += 2             -> normalSeat
    battlewagon.Seats += 3             -> normalSeat
    battlewagon.Seats += 4             -> normalSeat
    battlewagon.controlledWeapons(seat = 1, weapon = 5)
    battlewagon.controlledWeapons(seat = 2, weapon = 6)
    battlewagon.controlledWeapons(seat = 3, weapon = 7)
    battlewagon.controlledWeapons(seat = 4, weapon = 8)
    battlewagon.Weapons += 5           -> battlewagon_weapon_systema
    battlewagon.Weapons += 6           -> battlewagon_weapon_systemb
    battlewagon.Weapons += 7           -> battlewagon_weapon_systemc
    battlewagon.Weapons += 8           -> battlewagon_weapon_systemd
    battlewagon.MountPoints += 1       -> MountInfo(0)
    battlewagon.MountPoints += 2       -> MountInfo(1)
    battlewagon.MountPoints += 3       -> MountInfo(2)
    battlewagon.MountPoints += 4       -> MountInfo(3)
    battlewagon.MountPoints += 5       -> MountInfo(4)
    battlewagon.subsystems = controlSubsystem
    battlewagon.TrunkSize = InventoryTile.Tile1515
    battlewagon.TrunkOffset = 30
    battlewagon.TrunkLocation = Vector3(-3.46f, 0f, 0f)
    battlewagon.AutoPilotSpeeds = (18, 6)
    battlewagon.DestroyedModel = Some(DestroyedVehicle.MediumTransport)
    battlewagon.JackingDuration = Array(0, 25, 8, 5)
    battlewagon.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    battlewagon.DrownAtMaxDepth = false
    battlewagon.MaxDepth = 2f
    battlewagon.UnderwaterLifespan(suffocation = -1, recovery = -1)
    battlewagon.Geometry = delivererForm
    battlewagon.collision.avatarCollisionDamageMax = 120
    battlewagon.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 60), (0.75f, 110), (1f, 175))) //inherited from mediumtransport
    battlewagon.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 200), (30f, 750), (32.5f, 2000))) //inherited from mediumtransport
    battlewagon.maxForwardSpeed = 65f
    battlewagon.mass = 108.5f

    thunderer.Name = "thunderer"
    thunderer.MaxHealth = 2500
    thunderer.Damageable = true
    thunderer.Repairable = true
    thunderer.RepairIfDestroyed = false
    thunderer.MaxShields = 500
    thunderer.Seats += 0             -> driverSeat
    thunderer.Seats += 1             -> normalSeat
    thunderer.Seats += 2             -> normalSeat
    thunderer.Seats += 3             -> normalSeat
    thunderer.Seats += 4             -> normalSeat
    thunderer.Weapons += 5           -> thunderer_weapon_systema
    thunderer.Weapons += 6           -> thunderer_weapon_systemb
    thunderer.controlledWeapons(seat = 1, weapon = 5)
    thunderer.controlledWeapons(seat = 2, weapon = 6)
    thunderer.MountPoints += 1       -> MountInfo(0)
    thunderer.MountPoints += 2       -> MountInfo(1)
    thunderer.MountPoints += 3       -> MountInfo(2)
    thunderer.MountPoints += 4       -> MountInfo(3)
    thunderer.MountPoints += 5       -> MountInfo(4)
    thunderer.subsystems = controlSubsystem
    thunderer.TrunkSize = InventoryTile.Tile1515
    thunderer.TrunkOffset = 30
    thunderer.TrunkLocation = Vector3(-3.46f, 0f, 0f)
    thunderer.AutoPilotSpeeds = (18, 6)
    thunderer.DestroyedModel = Some(DestroyedVehicle.MediumTransport)
    thunderer.Subtract.Damage1 = 7
    thunderer.JackingDuration = Array(0, 25, 8, 5)
    thunderer.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    thunderer.DrownAtMaxDepth = false
    thunderer.MaxDepth = 2f
    thunderer.UnderwaterLifespan(suffocation = -1, recovery = -1)
    thunderer.Geometry = delivererForm
    thunderer.collision.avatarCollisionDamageMax = 120
    thunderer.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 60), (0.75f, 110), (1f, 175)))
    thunderer.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 200), (30f, 750), (32.5f, 2000)))
    thunderer.maxForwardSpeed = 65f
    thunderer.mass = 108.5f

    aurora.Name = "aurora"
    aurora.MaxHealth = 2500
    aurora.Damageable = true
    aurora.Repairable = true
    aurora.RepairIfDestroyed = false
    aurora.MaxShields = 500
    aurora.Seats += 0             -> driverSeat
    aurora.Seats += 1             -> normalSeat
    aurora.Seats += 2             -> normalSeat
    aurora.Seats += 3             -> normalSeat
    aurora.Seats += 4             -> normalSeat
    aurora.controlledWeapons(seat = 1, weapon = 5)
    aurora.controlledWeapons(seat = 2, weapon = 6)
    aurora.Weapons += 5           -> aurora_weapon_systema
    aurora.Weapons += 6           -> aurora_weapon_systemb
    aurora.MountPoints += 1       -> MountInfo(0)
    aurora.MountPoints += 2       -> MountInfo(1)
    aurora.MountPoints += 3       -> MountInfo(2)
    aurora.MountPoints += 4       -> MountInfo(3)
    aurora.MountPoints += 5       -> MountInfo(4)
    aurora.subsystems = controlSubsystem
    aurora.TrunkSize = InventoryTile.Tile1515
    aurora.TrunkOffset = 30
    aurora.TrunkLocation = Vector3(-3.46f, 0f, 0f)
    aurora.AutoPilotSpeeds = (18, 6)
    aurora.DestroyedModel = Some(DestroyedVehicle.MediumTransport)
    aurora.Subtract.Damage1 = 7
    aurora.JackingDuration = Array(0, 25, 8, 5)
    aurora.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    aurora.DrownAtMaxDepth = false
    aurora.MaxDepth = 2f
    aurora.UnderwaterLifespan(suffocation = -1, recovery = -1)
    aurora.Geometry = delivererForm
    aurora.collision.avatarCollisionDamageMax = 120
    aurora.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 60), (0.75f, 110), (1f, 175)))
    aurora.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 200), (30f, 750), (32.5f, 2000)))
    aurora.maxForwardSpeed = 65f
    aurora.mass = 108.5f

    apc_tr.Name = "apc_tr" // Juggernaut
    apc_tr.MaxHealth = 6000
    apc_tr.Damageable = true
    apc_tr.Repairable = true
    apc_tr.RepairIfDestroyed = false
    apc_tr.MaxShields = 1200
    apc_tr.Seats += 0             -> normalSeat
    apc_tr.Seats += 1             -> normalSeat
    apc_tr.Seats += 2             -> normalSeat
    apc_tr.Seats += 3             -> normalSeat
    apc_tr.Seats += 4             -> normalSeat
    apc_tr.Seats += 5             -> normalSeat
    apc_tr.Seats += 6             -> normalSeat
    apc_tr.Seats += 7             -> normalSeat
    apc_tr.Seats += 8             -> normalSeat
    apc_tr.Seats += 9             -> maxOnlySeat
    apc_tr.Seats += 10            -> maxOnlySeat
    apc_tr.controlledWeapons(seat = 1, weapon = 11)
    apc_tr.controlledWeapons(seat = 2, weapon = 12)
    apc_tr.controlledWeapons(seat = 5, weapon = 15)
    apc_tr.controlledWeapons(seat = 6, weapon = 16)
    apc_tr.controlledWeapons(seat = 7, weapon = 13)
    apc_tr.controlledWeapons(seat = 8, weapon = 14)
    apc_tr.Weapons += 11          -> apc_weapon_systemc_tr
    apc_tr.Weapons += 12          -> apc_weapon_systemb
    apc_tr.Weapons += 13          -> apc_weapon_systema
    apc_tr.Weapons += 14          -> apc_weapon_systemd_tr
    apc_tr.Weapons += 15          -> apc_ballgun_r
    apc_tr.Weapons += 16          -> apc_ballgun_l
    apc_tr.MountPoints += 1       -> MountInfo(0)
    apc_tr.MountPoints += 2       -> MountInfo(0)
    apc_tr.MountPoints += 3       -> MountInfo(1)
    apc_tr.MountPoints += 4       -> MountInfo(2)
    apc_tr.MountPoints += 5       -> MountInfo(3)
    apc_tr.MountPoints += 6       -> MountInfo(4)
    apc_tr.MountPoints += 7       -> MountInfo(5)
    apc_tr.MountPoints += 8       -> MountInfo(6)
    apc_tr.MountPoints += 9       -> MountInfo(7)
    apc_tr.MountPoints += 10      -> MountInfo(8)
    apc_tr.MountPoints += 11      -> MountInfo(9)
    apc_tr.MountPoints += 12      -> MountInfo(10)
    apc_tr.subsystems = controlSubsystem
    apc_tr.TrunkSize = InventoryTile.Tile2016
    apc_tr.TrunkOffset = 30
    apc_tr.TrunkLocation = Vector3(-5.82f, 0f, 0f)
    apc_tr.AutoPilotSpeeds = (16, 6)
    apc_tr.DestroyedModel = Some(DestroyedVehicle.Apc)
    apc_tr.JackingDuration = Array(0, 45, 15, 10)
    apc_tr.RadiationShielding = 0.5f
    apc_tr.Subtract.Damage1 = 10
    apc_tr.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 15
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    apc_tr.DrownAtMaxDepth = true
    apc_tr.MaxDepth = 3
    apc_tr.UnderwaterLifespan(suffocation = 15000L, recovery = 7500L)
    apc_tr.Geometry = apcForm
    apc_tr.MaxCapacitor = 300
    apc_tr.CapacitorRecharge = 1
    apc_tr.collision.avatarCollisionDamageMax = 300
    apc_tr.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 40), (0.75f, 70), (1f, 110)))
    apc_tr.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 300), (12f, 1000), (13f, 3000)))
    apc_tr.maxForwardSpeed = 60f
    apc_tr.mass = 128.4f

    apc_nc.Name = "apc_nc" // Vindicator
    apc_nc.MaxHealth = 6000
    apc_nc.Damageable = true
    apc_nc.Repairable = true
    apc_nc.RepairIfDestroyed = false
    apc_nc.MaxShields = 1200
    apc_nc.Seats += 0             -> normalSeat
    apc_nc.Seats += 1             -> normalSeat
    apc_nc.Seats += 2             -> normalSeat
    apc_nc.Seats += 3             -> normalSeat
    apc_nc.Seats += 4             -> normalSeat
    apc_nc.Seats += 5             -> normalSeat
    apc_nc.Seats += 6             -> normalSeat
    apc_nc.Seats += 7             -> normalSeat
    apc_nc.Seats += 8             -> normalSeat
    apc_nc.Seats += 9             -> maxOnlySeat
    apc_nc.Seats += 10            -> maxOnlySeat
    apc_nc.controlledWeapons(seat = 1, weapon = 11)
    apc_nc.controlledWeapons(seat = 2, weapon = 12)
    apc_nc.controlledWeapons(seat = 5, weapon = 15)
    apc_nc.controlledWeapons(seat = 6, weapon = 16)
    apc_nc.controlledWeapons(seat = 7, weapon = 13)
    apc_nc.controlledWeapons(seat = 8, weapon = 14)
    apc_nc.Weapons += 11          -> apc_weapon_systemc_nc
    apc_nc.Weapons += 12          -> apc_weapon_systemb
    apc_nc.Weapons += 13          -> apc_weapon_systema
    apc_nc.Weapons += 14          -> apc_weapon_systemd_nc
    apc_nc.Weapons += 15          -> apc_ballgun_r
    apc_nc.Weapons += 16          -> apc_ballgun_l
    apc_nc.MountPoints += 1       -> MountInfo(0)
    apc_nc.MountPoints += 2       -> MountInfo(0)
    apc_nc.MountPoints += 3       -> MountInfo(1)
    apc_nc.MountPoints += 4       -> MountInfo(2)
    apc_nc.MountPoints += 5       -> MountInfo(3)
    apc_nc.MountPoints += 6       -> MountInfo(4)
    apc_nc.MountPoints += 7       -> MountInfo(5)
    apc_nc.MountPoints += 8       -> MountInfo(6)
    apc_nc.MountPoints += 9       -> MountInfo(7)
    apc_nc.MountPoints += 10      -> MountInfo(8)
    apc_nc.MountPoints += 11      -> MountInfo(9)
    apc_nc.MountPoints += 12      -> MountInfo(10)
    apc_nc.subsystems = controlSubsystem
    apc_nc.TrunkSize = InventoryTile.Tile2016
    apc_nc.TrunkOffset = 30
    apc_nc.TrunkLocation = Vector3(-5.82f, 0f, 0f)
    apc_nc.AutoPilotSpeeds = (16, 6)
    apc_nc.DestroyedModel = Some(DestroyedVehicle.Apc)
    apc_nc.JackingDuration = Array(0, 45, 15, 10)
    apc_nc.RadiationShielding = 0.5f
    apc_nc.Subtract.Damage1 = 10
    apc_nc.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 15
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    apc_nc.DrownAtMaxDepth = true
    apc_nc.MaxDepth = 3
    apc_nc.UnderwaterLifespan(suffocation = 15000L, recovery = 7500L)
    apc_nc.Geometry = apcForm
    apc_nc.MaxCapacitor = 300
    apc_nc.CapacitorRecharge = 1
    apc_nc.collision.avatarCollisionDamageMax = 300
    apc_nc.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 40), (0.75f, 70), (1f, 110)))
    apc_nc.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 300), (12f, 1000), (13f, 3000)))
    apc_nc.maxForwardSpeed = 60f
    apc_nc.mass = 128.4f

    apc_vs.Name = "apc_vs" // Leviathan
    apc_vs.MaxHealth = 6000
    apc_vs.Damageable = true
    apc_vs.Repairable = true
    apc_vs.RepairIfDestroyed = false
    apc_vs.MaxShields = 1200
    apc_vs.Seats += 0             -> normalSeat
    apc_vs.Seats += 1             -> normalSeat
    apc_vs.Seats += 2             -> normalSeat
    apc_vs.Seats += 3             -> normalSeat
    apc_vs.Seats += 4             -> normalSeat
    apc_vs.Seats += 5             -> normalSeat
    apc_vs.Seats += 6             -> normalSeat
    apc_vs.Seats += 7             -> normalSeat
    apc_vs.Seats += 8             -> normalSeat
    apc_vs.Seats += 9             -> maxOnlySeat
    apc_vs.Seats += 10            -> maxOnlySeat
    apc_vs.controlledWeapons(seat = 1, weapon = 11)
    apc_vs.controlledWeapons(seat = 2, weapon = 12)
    apc_vs.controlledWeapons(seat = 5, weapon = 15)
    apc_vs.controlledWeapons(seat = 6, weapon = 16)
    apc_vs.controlledWeapons(seat = 7, weapon = 13)
    apc_vs.controlledWeapons(seat = 8, weapon = 14)
    apc_vs.Weapons += 11          -> apc_weapon_systemc_vs
    apc_vs.Weapons += 12          -> apc_weapon_systemb
    apc_vs.Weapons += 13          -> apc_weapon_systema
    apc_vs.Weapons += 14          -> apc_weapon_systemd_vs
    apc_vs.Weapons += 15          -> apc_ballgun_r
    apc_vs.Weapons += 16          -> apc_ballgun_l
    apc_vs.MountPoints += 1       -> MountInfo(0)
    apc_vs.MountPoints += 2       -> MountInfo(0)
    apc_vs.MountPoints += 3       -> MountInfo(1)
    apc_vs.MountPoints += 4       -> MountInfo(2)
    apc_vs.MountPoints += 5       -> MountInfo(3)
    apc_vs.MountPoints += 6       -> MountInfo(4)
    apc_vs.MountPoints += 7       -> MountInfo(5)
    apc_vs.MountPoints += 8       -> MountInfo(6)
    apc_vs.MountPoints += 9       -> MountInfo(7)
    apc_vs.MountPoints += 10      -> MountInfo(8)
    apc_vs.MountPoints += 11      -> MountInfo(9)
    apc_vs.MountPoints += 12      -> MountInfo(10)
    apc_vs.subsystems = controlSubsystem
    apc_vs.TrunkSize = InventoryTile.Tile2016
    apc_vs.TrunkOffset = 30
    apc_vs.TrunkLocation = Vector3(-5.82f, 0f, 0f)
    apc_vs.AutoPilotSpeeds = (16, 6)
    apc_vs.DestroyedModel = Some(DestroyedVehicle.Apc)
    apc_vs.JackingDuration = Array(0, 45, 15, 10)
    apc_vs.RadiationShielding = 0.5f
    apc_vs.Subtract.Damage1 = 10
    apc_vs.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 15
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    apc_vs.DrownAtMaxDepth = true
    apc_vs.MaxDepth = 3
    apc_vs.UnderwaterLifespan(suffocation = 15000L, recovery = 7500L)
    apc_vs.Geometry = apcForm
    apc_vs.MaxCapacitor = 300
    apc_vs.CapacitorRecharge = 1
    apc_vs.collision.avatarCollisionDamageMax = 300
    apc_vs.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 40), (0.75f, 70), (1f, 110)))
    apc_vs.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 300), (12f, 1000), (13f, 3000)))
    apc_vs.maxForwardSpeed = 60f
    apc_vs.mass = 128.4f

    lightning.Name = "lightning"
    lightning.MaxHealth = 2000
    lightning.Damageable = true
    lightning.Repairable = true
    lightning.RepairIfDestroyed = false
    lightning.MaxShields = 400
    lightning.Seats += 0             -> driverSeat
    lightning.controlledWeapons(seat = 0, weapon = 1)
    lightning.Weapons += 1           -> lightning_weapon_system
    lightning.MountPoints += 1       -> MountInfo(0)
    lightning.MountPoints += 2       -> MountInfo(0)
    lightning.subsystems = controlSubsystem
    lightning.TrunkSize = InventoryTile.Tile1511
    lightning.TrunkOffset = 30
    lightning.TrunkLocation = Vector3(-3f, 0f, 0f)
    lightning.AutoPilotSpeeds = (20, 8)
    lightning.DestroyedModel = Some(DestroyedVehicle.Lightning)
    lightning.RadiationShielding = 0.5f
    lightning.Subtract.Damage1 = 7
    lightning.JackingDuration = Array(0, 20, 7, 5)
    lightning.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    lightning.DrownAtMaxDepth = true
    lightning.MaxDepth = 1.38f
    lightning.UnderwaterLifespan(suffocation = 12000L, recovery = 6000L)
    lightning.Geometry = GeometryForm.representByCylinder(radius = 2.5078f, height = 1.79688f)
    lightning.collision.avatarCollisionDamageMax = 150
    lightning.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 25), (0.75f, 50), (1f, 80)))
    lightning.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 300), (39f, 750)))
    lightning.maxForwardSpeed = 74f
    lightning.mass = 100.2f

    prowler.Name = "prowler"
    prowler.MaxHealth = 4800
    prowler.Damageable = true
    prowler.Repairable = true
    prowler.RepairIfDestroyed = false
    prowler.MaxShields = 960
    prowler.Seats += 0             -> driverSeat
    prowler.Seats += 1             -> normalSeat
    prowler.Seats += 2             -> normalSeat
    prowler.controlledWeapons(seat = 1, weapon = 3)
    prowler.controlledWeapons(seat = 2, weapon = 4)
    prowler.Weapons += 3           -> prowler_weapon_systemA
    prowler.Weapons += 4           -> prowler_weapon_systemB
    prowler.MountPoints += 1       -> MountInfo(0)
    prowler.MountPoints += 2       -> MountInfo(1)
    prowler.MountPoints += 3       -> MountInfo(2)
    prowler.subsystems = controlSubsystem
    prowler.TrunkSize = InventoryTile.Tile1511
    prowler.TrunkOffset = 30
    prowler.TrunkLocation = Vector3(-4.71f, 0f, 0f)
    prowler.AutoPilotSpeeds = (14, 6)
    prowler.DestroyedModel = Some(DestroyedVehicle.Prowler)
    prowler.RadiationShielding = 0.5f
    prowler.Subtract.Damage1 = 9
    prowler.JackingDuration = Array(0, 30, 10, 5)
    prowler.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    prowler.DrownAtMaxDepth = true
    prowler.MaxDepth = 3
    prowler.UnderwaterLifespan(suffocation = 12000L, recovery = 6000L)
    prowler.Geometry = GeometryForm.representByCylinder(radius = 3.461f, height = 3.48438f)
    prowler.collision.avatarCollisionDamageMax = 300
    prowler.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 15), (0.5f, 40), (0.75f, 75), (1f, 100)))
    prowler.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 250), (30f, 600), (32.5f, 1500)))
    prowler.maxForwardSpeed = 57f
    prowler.mass = 510.5f

    vanguard.Name = "vanguard"
    vanguard.MaxHealth = 5400
    vanguard.Damageable = true
    vanguard.Repairable = true
    vanguard.RepairIfDestroyed = false
    vanguard.MaxShields = 1080
    vanguard.Seats += 0             -> driverSeat
    vanguard.Seats += 1             -> normalSeat
    vanguard.controlledWeapons(seat = 1, weapon = 2)
    vanguard.Weapons += 2           -> vanguard_weapon_system
    vanguard.MountPoints += 1       -> MountInfo(0)
    vanguard.MountPoints += 2       -> MountInfo(1)
    vanguard.subsystems = controlSubsystem
    vanguard.TrunkSize = InventoryTile.Tile1511
    vanguard.TrunkOffset = 30
    vanguard.TrunkLocation = Vector3(-4.84f, 0f, 0f)
    vanguard.AutoPilotSpeeds = (16, 6)
    vanguard.DestroyedModel = Some(DestroyedVehicle.Vanguard)
    vanguard.RadiationShielding = 0.5f
    vanguard.Subtract.Damage1 = 9
    vanguard.JackingDuration = Array(0, 30, 10, 5)
    vanguard.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    vanguard.DrownAtMaxDepth = true
    vanguard.MaxDepth = 2.7f
    vanguard.UnderwaterLifespan(suffocation = 12000L, recovery = 6000L)
    vanguard.Geometry = GeometryForm.representByCylinder(radius = 3.8554f, height = 2.60938f)
    vanguard.collision.avatarCollisionDamageMax = 300
    vanguard.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 5), (0.5f, 20), (0.75f, 40), (1f, 60)))
    vanguard.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 100), (30f, 250), (32.5f, 600)))
    vanguard.maxForwardSpeed = 60f
    vanguard.mass = 460.4f

    magrider.Name = "magrider"
    magrider.MaxHealth = 4200
    magrider.Damageable = true
    magrider.Repairable = true
    magrider.RepairIfDestroyed = false
    magrider.MaxShields = 840
    magrider.Seats += 0             -> driverSeat
    magrider.Seats += 1             -> normalSeat
    magrider.controlledWeapons(seat = 0, weapon = 2)
    magrider.controlledWeapons(seat = 1, weapon = 3)
    magrider.Weapons += 2           -> particle_beam_magrider
    magrider.Weapons += 3           -> heavy_rail_beam_magrider
    magrider.MountPoints += 1       -> MountInfo(0)
    magrider.MountPoints += 2       -> MountInfo(1)
    magrider.subsystems = controlSubsystem
    magrider.TrunkSize = InventoryTile.Tile1511
    magrider.TrunkOffset = 30
    magrider.TrunkLocation = Vector3(5.06f, 0f, 0f)
    magrider.AutoPilotSpeeds = (18, 6)
    magrider.DestroyedModel = Some(DestroyedVehicle.Magrider)
    magrider.RadiationShielding = 0.5f
    magrider.Subtract.Damage1 = 9
    magrider.JackingDuration = Array(0, 30, 10, 5)
    magrider.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    magrider.DrownAtMaxDepth = true
    magrider.MaxDepth = 2
    magrider.UnderwaterLifespan(suffocation = 45000L, recovery = 5000L) //but the magrider hovers over water, so ...?
    magrider.Geometry = GeometryForm.representByCylinder(radius = 3.3008f, height = 3.26562f)
    magrider.collision.avatarCollisionDamageMax = 225
    magrider.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 35), (0.5f, 70), (0.75f, 90), (1f, 120)))
    magrider.collision.z = CollisionZData(Array((5f, 1), (15f, 50), (25f, 250), (30f, 600), (32.5f, 1500)))
    magrider.maxForwardSpeed = 65f
    magrider.mass = 75.3f

    val utilityConverter = new UtilityVehicleConverter
    ant.Name = "ant"
    ant.MaxHealth = 2000
    ant.Damageable = true
    ant.Repairable = true
    ant.RepairIfDestroyed = false
    ant.MaxShields = 400
    ant.Seats += 0       -> driverSeat
    ant.MountPoints += 1 -> MountInfo(0)
    ant.MountPoints += 2 -> MountInfo(0)
    ant.subsystems = controlSubsystem
    ant.Deployment = true
    ant.DeployTime = 1500
    ant.UndeployTime = 1500
    ant.AutoPilotSpeeds = (18, 6)
    ant.MaxNtuCapacitor = 1500
    ant.Packet = utilityConverter
    ant.DestroyedModel = Some(DestroyedVehicle.Ant)
    ant.RadiationShielding = 0.5f
    ant.Subtract.Damage1 = 5
    ant.JackingDuration = Array(0, 60, 20, 15)
    ant.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    ant.DrownAtMaxDepth = true
    ant.MaxDepth = 2
    ant.UnderwaterLifespan(suffocation = 12000L, recovery = 6000L)
    ant.Geometry = GeometryForm.representByCylinder(radius = 2.16795f, height = 2.09376f) //TODO hexahedron
    ant.collision.avatarCollisionDamageMax = 50
    ant.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 30), (0.75f, 50), (1f, 70)))
    ant.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 250), (12f, 500), (13f, 750)))
    ant.maxForwardSpeed = 65f
    ant.mass = 80.5f

    ams.Name = "ams"
    ams.MaxHealth = 3000
    ams.Damageable = true
    ams.Repairable = true
    ams.RepairIfDestroyed = false
    ams.MaxShields = 600 + 1
    ams.Seats += 0       -> driverSeat
    ams.MountPoints += 1 -> MountInfo(0)
    ams.MountPoints += 2 -> MountInfo(0)
    ams.Utilities += 1   -> UtilityType.matrix_terminalc
    ams.Utilities += 2   -> UtilityType.ams_respawn_tube
    ams.Utilities += 3   -> UtilityType.order_terminala
    ams.Utilities += 4   -> UtilityType.order_terminalb
    ams.subsystems = controlSubsystem
    ams.Deployment = true
    ams.DeployTime = 2000
    ams.UndeployTime = 2000
    ams.interference = InterferenceRange(main = 125f, sharedGroupId = 3, shared = 30f)
    ams.DeconstructionTime = Some(15 minutes)
    ams.AutoPilotSpeeds = (18, 6)
    ams.Packet = utilityConverter
    ams.DestroyedModel = Some(DestroyedVehicle.Ams)
    ams.RadiationShielding = 0.5f
    ams.Subtract.Damage1 = 10
    ams.JackingDuration = Array(0, 60, 20, 15)
    ams.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.Splash
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 15
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    ams.DrownAtMaxDepth = true
    ams.MaxDepth = 3
    ams.UnderwaterLifespan(suffocation = 5000L, recovery = 5000L)
    ams.Geometry = GeometryForm.representByCylinder(radius = 3.0117f, height = 3.39062f) //TODO hexahedron
    ams.collision.avatarCollisionDamageMax = 250
    ams.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 10), (0.5f, 40), (0.75f, 60), (1f, 100)))
    ams.collision.z = CollisionZData(Array((2f, 1), (6f, 50), (10f, 250), (12f, 805), (13f, 3000)))
    ams.maxForwardSpeed = 70f
    ams.mass = 136.8f

    val variantConverter = new VariantVehicleConverter
    router.Name = "router"
    router.MaxHealth = 4000
    router.Damageable = true
    router.Repairable = true
    router.RepairIfDestroyed = false
    router.MaxShields = 800
    router.Seats += 0       -> normalSeat
    router.MountPoints += 1 -> MountInfo(0)
    router.Utilities += 1   -> UtilityType.teleportpad_terminal
    router.Utilities += 2   -> UtilityType.internal_router_telepad_deployable
    router.subsystems = controlSubsystem
    router.TrunkSize = InventoryTile.Tile1511
    router.TrunkOffset = 30
    router.TrunkLocation = Vector3(0f, 3.4f, 0f)
    router.Deployment = true
    router.DeployTime = 2000
    router.UndeployTime = 2000
    router.interference = InterferenceRange(main = 20f)
    router.DeconstructionTime = Duration(20, "minutes")
    router.AutoPilotSpeeds = (16, 6)
    router.Packet = variantConverter
    router.DestroyedModel = Some(DestroyedVehicle.Router)
    router.RadiationShielding = 0.5f
    router.Subtract.Damage1 = 5
    router.JackingDuration = Array(0, 20, 7, 5)
    router.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    router.DrownAtMaxDepth = true
    router.MaxDepth = 2
    router.UnderwaterLifespan(suffocation = 45000L, recovery = 5000L) //but the router hovers over water, so ...?
    router.Geometry = GeometryForm.representByCylinder(radius = 3.64845f, height = 3.51563f) //TODO hexahedron
    router.collision.avatarCollisionDamageMax = 150 //it has to bonk you on the head when it falls?
    router.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 13), (0.5f, 35), (0.75f, 65), (1f, 90)))
    router.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 350), (39f, 900)))
    router.maxForwardSpeed = 60f
    router.mass = 60f

    switchblade.Name = "switchblade"
    switchblade.MaxHealth = 1750
    switchblade.Damageable = true
    switchblade.Repairable = true
    switchblade.RepairIfDestroyed = false
    switchblade.MaxShields = 350
    switchblade.Seats += 0             -> normalSeat
    switchblade.controlledWeapons(seat = 0, weapon = 1)
    switchblade.Weapons += 1           -> scythe
    switchblade.MountPoints += 1       -> MountInfo(0)
    switchblade.MountPoints += 2       -> MountInfo(0)
    switchblade.subsystems = controlSubsystem
    switchblade.TrunkSize = InventoryTile.Tile1511
    switchblade.TrunkOffset = 30
    switchblade.TrunkLocation = Vector3(-2.5f, 0f, 0f)
    switchblade.Deployment = true
    switchblade.DeployTime = 2000
    switchblade.UndeployTime = 2000
    switchblade.AutoPilotSpeeds = (22, 8)
    switchblade.Packet = variantConverter
    switchblade.DestroyedModel = Some(DestroyedVehicle.Switchblade)
    switchblade.RadiationShielding = 0.5f
    switchblade.Subtract.Damage0 = 5
    switchblade.Subtract.Damage1 = 5
    switchblade.JackingDuration = Array(0, 20, 7, 5)
    switchblade.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    switchblade.DrownAtMaxDepth = true
    switchblade.MaxDepth = 2
    switchblade.UnderwaterLifespan(
      suffocation = 45000L,
      recovery = 5000L
    ) //but the switchblade hovers over water, so ...?
    switchblade.Geometry = GeometryForm.representByCylinder(radius = 2.4335f, height = 2.73438f)
    switchblade.collision.avatarCollisionDamageMax = 35
    switchblade.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 13), (0.5f, 35), (0.75f, 65), (1f, 90)))
    switchblade.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 350), (39f, 800)))
    switchblade.maxForwardSpeed = 80f
    switchblade.mass = 63.9f

    flail.Name = "flail"
    flail.MaxHealth = 2400
    flail.Damageable = true
    flail.Repairable = true
    flail.RepairIfDestroyed = false
    flail.MaxShields = 480
    flail.Seats += 0             -> normalSeat
    flail.controlledWeapons(seat = 0, weapon = 1)
    flail.Weapons += 1           -> flail_weapon
    flail.Utilities += 2         -> UtilityType.targeting_laser_dispenser
    flail.MountPoints += 1       -> MountInfo(0)
    flail.subsystems = controlSubsystem
    flail.TrunkSize = InventoryTile.Tile1511
    flail.TrunkOffset = 30
    flail.TrunkLocation = Vector3(-3.75f, 0f, 0f)
    flail.Deployment = true
    flail.DeployTime = 5500
    flail.UndeployTime = 5500
    flail.AutoPilotSpeeds = (14, 6)
    flail.Packet = variantConverter
    flail.DestroyedModel = Some(DestroyedVehicle.Flail)
    flail.RadiationShielding = 0.5f
    flail.Subtract.Damage1 = 7
    flail.JackingDuration = Array(0, 20, 7, 5)
    flail.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    flail.DrownAtMaxDepth = true
    flail.MaxDepth = 2
    flail.UnderwaterLifespan(suffocation = 45000L, recovery = 5000L) //but the flail hovers over water, so ...?
    flail.Geometry = GeometryForm.representByCylinder(radius = 2.1875f, height = 2.21875f)
    flail.collision.avatarCollisionDamageMax = 175
    flail.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 12), (0.5f, 35), (0.75f, 65), (1f, 90)))
    flail.collision.z = CollisionZData(Array((6f, 1), (18f, 50), (30f, 150), (36f, 350), (39f, 900)))
    flail.maxForwardSpeed = 55f
    flail.mass = 73.5f
  }

  /**
   * Initialize flight `VehicleDefinition` globals.
   */
  private def init_flight_vehicles(): Unit = {
    val liberatorForm = GeometryForm.representByCylinder(radius = 3.74615f, height = 2.51563f) _
    val bailableSeat = new SeatDefinition() {
      bailable = true
    }

    val flightSubsystems = List(VehicleSubsystemEntry.Controls, VehicleSubsystemEntry.Ejection)

    val variantConverter = new VariantVehicleConverter
    mosquito.Name = "mosquito"
    mosquito.MaxHealth = 665
    mosquito.Damageable = true
    mosquito.Repairable = true
    mosquito.RepairIfDestroyed = false
    mosquito.MaxShields = 133
    mosquito.CanFly = true
    mosquito.Seats += 0             -> bailableSeat
    mosquito.controlledWeapons(seat = 0, weapon = 1)
    mosquito.Weapons += 1           -> rotarychaingun_mosquito
    mosquito.MountPoints += 1       -> MountInfo(0)
    mosquito.MountPoints += 2       -> MountInfo(0)
    mosquito.subsystems = flightSubsystems :+ VehicleSubsystemEntry.MosquitoRadar
    mosquito.TrunkSize = InventoryTile.Tile1111
    mosquito.TrunkOffset = 30
    mosquito.TrunkLocation = Vector3(-4.6f, 0f, 0f)
    mosquito.AutoPilotSpeeds = (0, 6)
    mosquito.Packet = variantConverter
    mosquito.DestroyedModel = Some(DestroyedVehicle.Mosquito)
    mosquito.JackingDuration = Array(0, 20, 7, 5)
    mosquito.RadiationShielding = 0.5f
    mosquito.DamageUsing = DamageCalculations.AgainstAircraft
    mosquito.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    mosquito.DrownAtMaxDepth = true
    mosquito.MaxDepth = 2 //flying vehicles will automatically disable
    mosquito.Geometry = GeometryForm.representByCylinder(radius = 2.72108f, height = 2.5f)
    mosquito.collision.avatarCollisionDamageMax = 50
    mosquito.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 50), (0.5f, 100), (0.75f, 150), (1f, 200)))
    mosquito.collision.z = CollisionZData(Array((3f, 1), (9f, 25), (15f, 50), (18f, 75), (19.5f, 100)))
    mosquito.maxForwardSpeed = 120f
    mosquito.mass = 53.6f

    lightgunship.Name = "lightgunship" // Reaver
    lightgunship.MaxHealth = 855 // Temporary - Correct Reaver Health from pre-"Coder Madness 2" Event
    lightgunship.Damageable = true
    lightgunship.Repairable = true
    lightgunship.RepairIfDestroyed = false
    lightgunship.MaxShields = 171 // Temporary - Correct Reaver Shields from pre-"Coder Madness 2" Event
    lightgunship.CanFly = true
    lightgunship.Seats += 0             -> bailableSeat
    lightgunship.controlledWeapons(seat = 0, weapon = 1)
    lightgunship.Weapons += 1           -> lightgunship_weapon_system
    lightgunship.MountPoints += 1       -> MountInfo(0)
    lightgunship.MountPoints += 2       -> MountInfo(0)
    lightgunship.subsystems = flightSubsystems
    lightgunship.TrunkSize = InventoryTile.Tile1511
    lightgunship.TrunkOffset = 30
    lightgunship.TrunkLocation = Vector3(-5.61f, 0f, 0f)
    lightgunship.AutoPilotSpeeds = (0, 4)
    lightgunship.Packet = variantConverter
    lightgunship.DestroyedModel = Some(DestroyedVehicle.LightGunship)
    lightgunship.RadiationShielding = 0.5f
    lightgunship.Subtract.Damage1 = 3
    lightgunship.JackingDuration = Array(0, 30, 10, 5)
    lightgunship.DamageUsing = DamageCalculations.AgainstAircraft
    lightgunship.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    lightgunship.DrownAtMaxDepth = true
    lightgunship.MaxDepth = 2 //flying vehicles will automatically disable
    lightgunship.Geometry = GeometryForm.representByCylinder(radius = 2.375f, height = 1.98438f)
    lightgunship.collision.avatarCollisionDamageMax = 75
    lightgunship.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 60), (0.5f, 120), (0.75f, 180), (1f, 250)))
    lightgunship.collision.z = CollisionZData(Array((3f, 1), (9f, 30), (15f, 60), (18f, 90), (19.5f, 125)))
    lightgunship.maxForwardSpeed = 104f
    lightgunship.mass = 51.1f

    wasp.Name = "wasp"
    wasp.MaxHealth = 515
    wasp.Damageable = true
    wasp.Repairable = true
    wasp.RepairIfDestroyed = false
    wasp.MaxShields = 103
    wasp.CanFly = true
    wasp.Seats += 0             -> bailableSeat
    wasp.controlledWeapons(seat = 0, weapon = 1)
    wasp.Weapons += 1           -> wasp_weapon_system
    wasp.MountPoints += 1       -> MountInfo(0)
    wasp.MountPoints += 2       -> MountInfo(0)
    wasp.subsystems = flightSubsystems
    wasp.TrunkSize = InventoryTile.Tile1111
    wasp.TrunkOffset = 30
    wasp.TrunkLocation = Vector3(-4.6f, 0f, 0f)
    wasp.AutoPilotSpeeds = (0, 6)
    wasp.Packet = variantConverter
    wasp.DestroyedModel = Some(DestroyedVehicle.Mosquito) //set_resource_parent wasp game_objects mosquito
    wasp.JackingDuration = Array(0, 20, 7, 5)
    wasp.DamageUsing = DamageCalculations.AgainstAircraft
    wasp.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 200
      Damage1 = 300
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    wasp.DrownAtMaxDepth = true
    wasp.MaxDepth = 2 //flying vehicles will automatically disable
    wasp.Geometry = GeometryForm.representByCylinder(radius = 2.88675f, height = 2.5f)
    wasp.collision.avatarCollisionDamageMax = 50 //mosquito numbers
    wasp.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 50), (0.5f, 100), (0.75f, 150), (1f, 200))) //mosquito numbers
    wasp.collision.z = CollisionZData(Array((3f, 1), (9f, 25), (15f, 50), (18f, 75), (19.5f, 100))) //mosquito numbers
    wasp.maxForwardSpeed = 120f
    wasp.mass = 53.6f

    liberator.Name = "liberator"
    liberator.MaxHealth = 2500
    liberator.Damageable = true
    liberator.Repairable = true
    liberator.RepairIfDestroyed = false
    liberator.MaxShields = 500
    liberator.CanFly = true
    liberator.Seats += 0             -> bailableSeat //new SeatDefinition()
    liberator.Seats += 1             -> bailableSeat
    liberator.Seats += 2             -> bailableSeat
    liberator.controlledWeapons(seat = 0, weapon = 3)
    liberator.controlledWeapons(seat = 1, weapon = 4)
    liberator.controlledWeapons(seat = 2, weapon = 5)
    liberator.Weapons += 3           -> liberator_weapon_system
    liberator.Weapons += 4           -> liberator_bomb_bay
    liberator.Weapons += 5           -> liberator_25mm_cannon
    liberator.MountPoints += 1       -> MountInfo(0)
    liberator.MountPoints += 2       -> MountInfo(1)
    liberator.MountPoints += 3       -> MountInfo(1)
    liberator.MountPoints += 4       -> MountInfo(2)
    liberator.subsystems = flightSubsystems
    liberator.TrunkSize = InventoryTile.Tile1515
    liberator.TrunkOffset = 30
    liberator.TrunkLocation = Vector3(-0.76f, -1.88f, 0f)
    liberator.AutoPilotSpeeds = (0, 4)
    liberator.Packet = variantConverter
    liberator.DestroyedModel = Some(DestroyedVehicle.Liberator)
    liberator.RadiationShielding = 0.5f
    liberator.Subtract.Damage1 = 5
    liberator.JackingDuration = Array(0, 30, 10, 5)
    liberator.DamageUsing = DamageCalculations.AgainstAircraft
    liberator.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    liberator.DrownAtMaxDepth = true
    liberator.MaxDepth = 2 //flying vehicles will automatically disable
    liberator.Geometry = liberatorForm
    liberator.collision.avatarCollisionDamageMax = 100
    liberator.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 60), (0.5f, 120), (0.75f, 180), (1f, 250)))
    liberator.collision.z = CollisionZData(Array((3f, 1), (9f, 30), (15f, 60), (18f, 90), (19.5f, 125)))
    liberator.maxForwardSpeed = 90f
    liberator.mass = 82f

    vulture.Name = "vulture"
    vulture.MaxHealth = 2500
    vulture.Damageable = true
    vulture.Repairable = true
    vulture.RepairIfDestroyed = false
    vulture.MaxShields = 500
    vulture.CanFly = true
    vulture.Seats += 0             -> bailableSeat //new SeatDefinition()
    vulture.Seats += 1             -> bailableSeat
    vulture.Seats += 2             -> bailableSeat
    vulture.controlledWeapons(seat = 0, weapon = 3)
    vulture.controlledWeapons(seat = 1, weapon = 4)
    vulture.controlledWeapons(seat = 2, weapon = 5)
    vulture.Weapons += 3           -> vulture_nose_weapon_system
    vulture.Weapons += 4           -> vulture_bomb_bay
    vulture.Weapons += 5           -> vulture_tail_cannon
    vulture.MountPoints += 1       -> MountInfo(0)
    vulture.MountPoints += 2       -> MountInfo(1)
    vulture.MountPoints += 3       -> MountInfo(1)
    vulture.MountPoints += 4       -> MountInfo(2)
    vulture.subsystems = flightSubsystems
    vulture.TrunkSize = InventoryTile.Tile1611
    vulture.TrunkOffset = 30
    vulture.TrunkLocation = Vector3(-0.76f, -1.88f, 0f)
    vulture.AutoPilotSpeeds = (0, 4)
    vulture.Packet = variantConverter
    vulture.DestroyedModel =
      Some(DestroyedVehicle.Liberator) //add_property vulture destroyedphysics liberator_destroyed
    vulture.RadiationShielding = 0.5f
    vulture.Subtract.Damage1 = 5
    vulture.JackingDuration = Array(0, 30, 10, 5)
    vulture.DamageUsing = DamageCalculations.AgainstAircraft
    vulture.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 250
      Damage1 = 375
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    vulture.DrownAtMaxDepth = true
    vulture.MaxDepth = 2 //flying vehicles will automatically disable
    vulture.Geometry = liberatorForm
    vulture.collision.avatarCollisionDamageMax = 100
    vulture.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 60), (0.5f, 120), (0.75f, 180), (1f, 250)))
    vulture.collision.z = CollisionZData(Array((3f, 1), (9f, 30), (15f, 60), (18f, 90), (19.5f, 125)))
    vulture.maxForwardSpeed = 97f
    vulture.mass = 82f

    dropship.Name = "dropship" // Galaxy
    dropship.MaxHealth = 5000
    dropship.Damageable = true
    dropship.Repairable = true
    dropship.RepairDistance = 20
    dropship.RepairIfDestroyed = false
    dropship.MaxShields = 1000
    dropship.CanFly = true
    dropship.Seats += 0 -> bailableSeat //new SeatDefinition()
    dropship.Seats += 1 -> bailableSeat
    dropship.Seats += 2 -> bailableSeat
    dropship.Seats += 3 -> bailableSeat
    dropship.Seats += 4 -> bailableSeat
    dropship.Seats += 5 -> bailableSeat
    dropship.Seats += 6 -> bailableSeat
    dropship.Seats += 7 -> bailableSeat
    dropship.Seats += 8 -> bailableSeat
    dropship.Seats += 9 -> new SeatDefinition() {
      bailable = true
      restriction = MaxOnly
    }
    dropship.Seats += 10 -> new SeatDefinition() {
      bailable = true
      restriction = MaxOnly
    }
    dropship.Seats += 11             -> bailableSeat
    dropship.controlledWeapons(seat = 1, weapon = 12)
    dropship.controlledWeapons(seat = 2, weapon = 13)
    dropship.controlledWeapons(seat = 11, weapon = 14)
    dropship.Weapons += 12           -> cannon_dropship_20mm
    dropship.Weapons += 13           -> cannon_dropship_20mm
    dropship.Weapons += 14           -> dropship_rear_turret
    dropship.Cargo += 15 -> new CargoDefinition() {
      restriction = SmallCargo
    }
    dropship.MountPoints += 1  -> MountInfo(0)
    dropship.MountPoints += 2  -> MountInfo(11)
    dropship.MountPoints += 3  -> MountInfo(1)
    dropship.MountPoints += 4  -> MountInfo(2)
    dropship.MountPoints += 5  -> MountInfo(3)
    dropship.MountPoints += 6  -> MountInfo(4)
    dropship.MountPoints += 7  -> MountInfo(5)
    dropship.MountPoints += 8  -> MountInfo(6)
    dropship.MountPoints += 9  -> MountInfo(7)
    dropship.MountPoints += 10 -> MountInfo(8)
    dropship.MountPoints += 11 -> MountInfo(9)
    dropship.MountPoints += 12 -> MountInfo(10)
    dropship.MountPoints += 13 -> MountInfo(15)
    dropship.subsystems = flightSubsystems
    dropship.TrunkSize = InventoryTile.Tile1612
    dropship.TrunkOffset = 30
    dropship.TrunkLocation = Vector3(-7.39f, -4.96f, 0f)
    dropship.AutoPilotSpeeds = (0, 4)
    dropship.Packet = variantConverter
    dropship.DestroyedModel = Some(DestroyedVehicle.Dropship)
    dropship.RadiationShielding = 0.5f
    dropship.Subtract.Damage1 = 7
    dropship.JackingDuration = Array(0, 60, 20, 10)
    dropship.DamageUsing = DamageCalculations.AgainstAircraft
    dropship.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 30
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    dropship.DrownAtMaxDepth = true
    dropship.MaxDepth = 2
    dropship.Geometry = GeometryForm.representByCylinder(radius = 10.52202f, height = 6.23438f)
    dropship.collision.avatarCollisionDamageMax = 300
    dropship.collision.xy = CollisionXYData(Array((0.1f, 5), (0.25f, 125), (0.5f, 250), (0.75f, 500), (1f, 1000)))
    dropship.collision.z = CollisionZData(Array((3f, 5), (9f, 125), (15f, 250), (18f, 500), (19.5f, 1000)))
    dropship.maxForwardSpeed = 80f
    dropship.mass = 133f

    galaxy_gunship.Name = "galaxy_gunship"
    galaxy_gunship.MaxHealth = 6000
    galaxy_gunship.Damageable = true
    galaxy_gunship.Repairable = true
    galaxy_gunship.RepairDistance = 20
    galaxy_gunship.RepairIfDestroyed = false
    galaxy_gunship.MaxShields = 1200
    galaxy_gunship.CanFly = true
    galaxy_gunship.Seats += 0             -> bailableSeat //new SeatDefinition()
    galaxy_gunship.Seats += 1             -> bailableSeat
    galaxy_gunship.Seats += 2             -> bailableSeat
    galaxy_gunship.Seats += 3             -> bailableSeat
    galaxy_gunship.Seats += 4             -> bailableSeat
    galaxy_gunship.Seats += 5             -> bailableSeat
    galaxy_gunship.controlledWeapons(seat = 1, weapon = 6)
    galaxy_gunship.controlledWeapons(seat = 2, weapon = 7)
    galaxy_gunship.controlledWeapons(seat = 3, weapon = 8)
    galaxy_gunship.controlledWeapons(seat = 4, weapon = 9)
    galaxy_gunship.controlledWeapons(seat = 5, weapon = 10)
    galaxy_gunship.Weapons += 6           -> galaxy_gunship_cannon
    galaxy_gunship.Weapons += 7           -> galaxy_gunship_cannon
    galaxy_gunship.Weapons += 8           -> galaxy_gunship_tailgun
    galaxy_gunship.Weapons += 9           -> galaxy_gunship_gun
    galaxy_gunship.Weapons += 10          -> galaxy_gunship_gun
    galaxy_gunship.MountPoints += 1       -> MountInfo(0)
    galaxy_gunship.MountPoints += 2       -> MountInfo(3)
    galaxy_gunship.MountPoints += 3       -> MountInfo(1)
    galaxy_gunship.MountPoints += 4       -> MountInfo(2)
    galaxy_gunship.MountPoints += 5       -> MountInfo(4)
    galaxy_gunship.MountPoints += 6       -> MountInfo(5)
    galaxy_gunship.subsystems = flightSubsystems
    galaxy_gunship.TrunkSize = InventoryTile.Tile1816
    galaxy_gunship.TrunkOffset = 30
    galaxy_gunship.TrunkLocation = Vector3(-9.85f, 0f, 0f)
    galaxy_gunship.AutoPilotSpeeds = (0, 4)
    galaxy_gunship.Packet = variantConverter
    galaxy_gunship.DestroyedModel =
      Some(DestroyedVehicle.Dropship) //the adb calls out a galaxy_gunship_destroyed but no such asset exists
    galaxy_gunship.RadiationShielding = 0.5f
    galaxy_gunship.Subtract.Damage1 = 7
    galaxy_gunship.JackingDuration = Array(0, 60, 20, 10)
    galaxy_gunship.DamageUsing = DamageCalculations.AgainstAircraft
    galaxy_gunship.Modifiers = GalaxyGunshipReduction(0.63f)
    galaxy_gunship.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 30
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    galaxy_gunship.DrownAtMaxDepth = true
    galaxy_gunship.MaxDepth = 2
    galaxy_gunship.Geometry = GeometryForm.representByCylinder(radius = 9.2382f, height = 5.01562f)
    galaxy_gunship.collision.avatarCollisionDamageMax = 300
    galaxy_gunship.collision.xy = CollisionXYData(Array((0.1f, 5), (0.25f, 125), (0.5f, 250), (0.75f, 500), (1f, 1000)))
    galaxy_gunship.collision.z = CollisionZData(Array((3f, 5), (9f, 125), (15f, 250), (18f, 500), (19.5f, 1000)))
    galaxy_gunship.maxForwardSpeed = 85f
    galaxy_gunship.mass = 133f

    lodestar.Name = "lodestar"
    lodestar.MaxHealth = 5000
    lodestar.Damageable = true
    lodestar.Repairable = true
    lodestar.RepairDistance = 20
    lodestar.RepairIfDestroyed = false
    lodestar.MaxShields = 1000
    lodestar.CanFly = true
    lodestar.Seats += 0         -> bailableSeat
    lodestar.MountPoints += 1   -> MountInfo(0)
    lodestar.MountPoints += 2   -> MountInfo(1)
    lodestar.Cargo += 1         -> new CargoDefinition()
    lodestar.Utilities += 2     -> UtilityType.lodestar_repair_terminal
    lodestar.UtilityOffset += 2 -> Vector3(0, 20, 0)
    lodestar.Utilities += 3     -> UtilityType.lodestar_repair_terminal
    lodestar.UtilityOffset += 3 -> Vector3(0, -20, 0)
    lodestar.Utilities += 4     -> UtilityType.multivehicle_rearm_terminal
    lodestar.UtilityOffset += 4 -> Vector3(0, 20, 0)
    lodestar.Utilities += 5     -> UtilityType.multivehicle_rearm_terminal
    lodestar.UtilityOffset += 5 -> Vector3(0, -20, 0)
    lodestar.Utilities += 6     -> UtilityType.bfr_rearm_terminal
    lodestar.UtilityOffset += 6 -> Vector3(0, 20, 0)
    lodestar.Utilities += 7     -> UtilityType.bfr_rearm_terminal
    lodestar.UtilityOffset += 7 -> Vector3(0, -20, 0)
    lodestar.subsystems = flightSubsystems
    lodestar.TrunkSize = InventoryTile.Tile1612
    lodestar.TrunkOffset = 30
    lodestar.TrunkLocation = Vector3(6.85f, -6.8f, 0f)
    lodestar.AutoPilotSpeeds = (0, 4)
    lodestar.Packet = variantConverter
    lodestar.DestroyedModel = Some(DestroyedVehicle.Lodestar)
    lodestar.RadiationShielding = 0.5f
    lodestar.Subtract.Damage1 = 7
    lodestar.JackingDuration = Array(0, 60, 20, 10)
    lodestar.DamageUsing = DamageCalculations.AgainstAircraft
    lodestar.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 300
      Damage1 = 450
      DamageRadius = 30
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    lodestar.DrownAtMaxDepth = true
    lodestar.MaxDepth = 2
    lodestar.Geometry = GeometryForm.representByCylinder(radius = 7.8671f, height = 6.79688f) //TODO hexahedron
    lodestar.collision.avatarCollisionDamageMax = 300
    lodestar.collision.xy = CollisionXYData(Array((0.1f, 5), (0.25f, 125), (0.5f, 250), (0.75f, 500), (1f, 1000)))
    lodestar.collision.z = CollisionZData(Array((3f, 5), (9f, 125), (15f, 250), (18f, 500), (19.5f, 1000)))
    lodestar.maxForwardSpeed = 80f
    lodestar.mass = 128.2f

    phantasm.Name = "phantasm"
    phantasm.MaxHealth = 2500
    phantasm.Damageable = true
    phantasm.Repairable = true
    phantasm.RepairIfDestroyed = false
    phantasm.MaxShields = 500
    phantasm.CanCloak = true
    phantasm.CanFly = true
    phantasm.Seats += 0       -> bailableSeat
    phantasm.Seats += 1       -> bailableSeat
    phantasm.Seats += 2       -> bailableSeat
    phantasm.Seats += 3       -> bailableSeat
    phantasm.Seats += 4       -> bailableSeat
    phantasm.MountPoints += 1 -> MountInfo(0)
    phantasm.MountPoints += 2 -> MountInfo(1)
    phantasm.MountPoints += 3 -> MountInfo(2)
    phantasm.MountPoints += 4 -> MountInfo(3)
    phantasm.MountPoints += 5 -> MountInfo(4)
    phantasm.subsystems = flightSubsystems
    phantasm.TrunkSize = InventoryTile.Tile1107
    phantasm.TrunkOffset = 30
    phantasm.TrunkLocation = Vector3(-6.16f, 0f, 0f)
    phantasm.AutoPilotSpeeds = (0, 6)
    phantasm.Packet = variantConverter
    phantasm.DestroyedModel = None //the adb calls out a phantasm_destroyed but no such asset exists
    phantasm.JackingDuration = Array(0, 60, 20, 10)
    phantasm.RadiationShielding = 0.5f
    phantasm.DamageUsing = DamageCalculations.AgainstAircraft
    phantasm.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 100
      Damage1 = 150
      DamageRadius = 12
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    phantasm.DrownAtMaxDepth = true
    phantasm.MaxDepth = 2
    phantasm.Geometry = GeometryForm.representByCylinder(radius = 5.2618f, height = 3f)
    phantasm.collision.avatarCollisionDamageMax = 100
    phantasm.collision.xy = CollisionXYData(Array((0.1f, 1), (0.25f, 60), (0.5f, 120), (0.75f, 180), (1f, 250)))
    phantasm.collision.z = CollisionZData(Array((3f, 1), (9f, 30), (15f, 60), (18f, 90), (19.5f, 125)))
    phantasm.maxForwardSpeed = 140f
    phantasm.mass = 100f

    droppod.Name = "droppod"
    droppod.MaxHealth = 20000
    droppod.Damageable = false
    droppod.Repairable = false
    droppod.CanFly = true
    droppod.Seats += 0 -> new SeatDefinition {
      restriction = Unrestricted
    }
    droppod.MountPoints += 1 -> MountInfo(0)
    droppod.TrunkSize = InventoryTile.None
    droppod.Packet = new DroppodConverter()
    droppod.DeconstructionTime = Some(5 seconds)
    droppod.DestroyedModel = None //the adb calls out a droppod; the cyclic nature of this confounds me
    droppod.DamageUsing = DamageCalculations.AgainstAircraft
    droppod.DrownAtMaxDepth = false
    droppod.mass = 2500f

    orbital_shuttle.Name = "orbital_shuttle"
    orbital_shuttle.MaxHealth = 20000
    orbital_shuttle.Damageable = false
    orbital_shuttle.Repairable = false
    orbital_shuttle.CanFly = true
    orbital_shuttle.CanBeOwned = None
    orbital_shuttle.undergoesDecay = false
    orbital_shuttle.Seats += 0 -> new SeatDefinition {
      occupancy = 300
      restriction = Unrestricted
    }
    /*
    these are close to the mount point offsets in the ADB;
    physically, they correlate to positions in the HART building rather than with the shuttle model by itself;
    set the shuttle pad based on the zonemap extraction values then position the shuttle relative to that pad;
    rotation based on the shuttle should place these offsets in the HART lobby whose gantry hall corresponds to that mount index
     */
    orbital_shuttle.MountPoints += 1 -> MountInfo(0, Vector3(-62, 4, -28.2f))
    orbital_shuttle.MountPoints += 2 -> MountInfo(0, Vector3(-62, 28, -28.2f))
    orbital_shuttle.MountPoints += 3 -> MountInfo(0, Vector3(-62, 4, -18.2f))
    orbital_shuttle.MountPoints += 4 -> MountInfo(0, Vector3(-62, 28, -18.2f))
    orbital_shuttle.MountPoints += 5 -> MountInfo(0, Vector3(62, 4, -28.2f))
    orbital_shuttle.MountPoints += 6 -> MountInfo(0, Vector3(62, 28, -28.2f))
    orbital_shuttle.MountPoints += 7 -> MountInfo(0, Vector3(62, 4, -18.2f))
    orbital_shuttle.MountPoints += 8 -> MountInfo(0, Vector3(62, 28, -18.2f))
    orbital_shuttle.TrunkSize = InventoryTile.None
    orbital_shuttle.Packet = new OrbitalShuttleConverter
    orbital_shuttle.DeconstructionTime = None
    orbital_shuttle.DestroyedModel = None
    orbital_shuttle.DamageUsing = DamageCalculations.AgainstNothing
    orbital_shuttle.DrownAtMaxDepth = false
    orbital_shuttle.mass = 25000f
  }

  /**
   * Initialize land-based giant mecha `VehicleDefinition` globals.
   */
  private def init_bfr_vehicles(): Unit = {
    val driverSeat = new SeatDefinition() {
      restriction = NoReinforcedOrMax
    }
    val bailableSeat = new SeatDefinition() {
      restriction = NoReinforcedOrMax
      bailable = true
    }
    val normalSeat = new SeatDefinition()
    val bfrSubsystems = List(
      VehicleSubsystemEntry.BattleframeMovementServos,
      VehicleSubsystemEntry.BattleframeSensorArray,
      VehicleSubsystemEntry.BattleframeShieldGenerator,
      VehicleSubsystemEntry.BattleframeTrunk
    )
    val bfrGunnerSubsystems = List(
      VehicleSubsystemEntry.BattleframeLeftArm,
      VehicleSubsystemEntry.BattleframeRightArm,
      VehicleSubsystemEntry.BattleframeLeftWeapon,
      VehicleSubsystemEntry.BattleframeRightWeapon,
      VehicleSubsystemEntry.BattleframeGunnerWeapon
    ) ++ bfrSubsystems
    val bfrFlightSubsystems = List(
      VehicleSubsystemEntry.BattleframeFlightLeftArm,
      VehicleSubsystemEntry.BattleframeFlightRightArm,
      VehicleSubsystemEntry.BattleframeFlightLeftWeapon,
      VehicleSubsystemEntry.BattleframeFlightRightWeapon
    ) ++ bfrSubsystems ++ List(
      VehicleSubsystemEntry.BattleframeFlightPod
    )

    val battleFrameConverter = new BattleFrameRoboticsConverter
    aphelion_gunner.Name = "aphelion_gunner"
    aphelion_gunner.MaxHealth = 4500
    aphelion_gunner.Damageable = true
    aphelion_gunner.Repairable = true
    aphelion_gunner.RepairIfDestroyed = false
    aphelion_gunner.shieldUiAttribute = 79
    aphelion_gunner.MaxShields = 3000
    aphelion_gunner.ShieldPeriodicDelay = 500
    aphelion_gunner.ShieldDamageDelay = 3500
    aphelion_gunner.ShieldAutoRecharge = 45
    aphelion_gunner.ShieldAutoRechargeSpecial = 85
    aphelion_gunner.DefaultShields = aphelion_gunner.MaxShields
    aphelion_gunner.Seats += 0       -> driverSeat
    aphelion_gunner.Seats += 1       -> normalSeat
    aphelion_gunner.controlledWeapons(seat = 0, weapons = Set(2, 3))
    aphelion_gunner.controlledWeapons(seat = 1, weapon = 4)
    aphelion_gunner.Weapons += 2     -> aphelion_ppa_left
    aphelion_gunner.Weapons += 3     -> aphelion_ppa_right
    aphelion_gunner.Weapons += 4     -> aphelion_plasma_rocket_pod
    aphelion_gunner.MountPoints += 1 -> MountInfo(0)
    aphelion_gunner.MountPoints += 2 -> MountInfo(1)
    aphelion_gunner.subsystems = bfrGunnerSubsystems
    aphelion_gunner.TrunkSize = InventoryTile.Tile1518
    aphelion_gunner.TrunkOffset = 30
    aphelion_gunner.TrunkLocation = Vector3(0f, -2f, 0f)
    aphelion_gunner.AutoPilotSpeeds = (5, 1)
    aphelion_gunner.Packet = battleFrameConverter
    aphelion_gunner.DestroyedModel = None
    aphelion_gunner.destructionDelay = Some(4000L)
    aphelion_gunner.JackingDuration = Array(0, 62, 60, 30)
    aphelion_gunner.RadiationShielding = 0.5f
    aphelion_gunner.DamageUsing = DamageCalculations.AgainstBfr
    aphelion_gunner.Model = BfrResolutions.calculate
    aphelion_gunner.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    aphelion_gunner.DrownAtMaxDepth = true
    aphelion_gunner.MaxDepth = 5.09375f
    aphelion_gunner.UnderwaterLifespan(suffocation = 60000L, recovery = 30000L)
    aphelion_gunner.Geometry = GeometryForm.representByCylinder(radius = 1.2618f, height = 6.01562f)
    aphelion_gunner.collision.avatarCollisionDamageMax = 300
    aphelion_gunner.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    aphelion_gunner.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    aphelion_gunner.maxForwardSpeed = 17
    aphelion_gunner.mass = 615.1f

    colossus_gunner.Name = "colossus_gunner"
    colossus_gunner.MaxHealth = 4500
    colossus_gunner.Damageable = true
    colossus_gunner.Repairable = true
    colossus_gunner.RepairIfDestroyed = false
    colossus_gunner.shieldUiAttribute = 79
    colossus_gunner.MaxShields = 3000
    colossus_gunner.ShieldPeriodicDelay = 500
    colossus_gunner.ShieldDamageDelay = 3500
    colossus_gunner.ShieldAutoRecharge = 45
    colossus_gunner.ShieldAutoRechargeSpecial = 85
    colossus_gunner.DefaultShields = colossus_gunner.MaxShields
    colossus_gunner.Seats += 0       -> driverSeat
    colossus_gunner.Seats += 1       -> normalSeat
    colossus_gunner.controlledWeapons(seat = 0, weapons = Set(2, 3))
    colossus_gunner.controlledWeapons(seat = 1, weapon = 4)
    colossus_gunner.Weapons += 2     -> colossus_tank_cannon_left
    colossus_gunner.Weapons += 3     -> colossus_tank_cannon_right
    colossus_gunner.Weapons += 4     -> colossus_dual_100mm_cannons
    colossus_gunner.MountPoints += 1 -> MountInfo(0)
    colossus_gunner.MountPoints += 2 -> MountInfo(1)
    colossus_gunner.subsystems = bfrGunnerSubsystems
    colossus_gunner.TrunkSize = InventoryTile.Tile1518
    colossus_gunner.TrunkOffset = 30
    colossus_gunner.TrunkLocation = Vector3(0f, -5f, 0f)
    colossus_gunner.AutoPilotSpeeds = (5, 1)
    colossus_gunner.Packet = battleFrameConverter
    colossus_gunner.DestroyedModel = None
    colossus_gunner.destructionDelay = Some(4000L)
    colossus_gunner.JackingDuration = Array(0, 62, 60, 30)
    colossus_gunner.RadiationShielding = 0.5f
    colossus_gunner.DamageUsing = DamageCalculations.AgainstBfr
    colossus_gunner.Model = BfrResolutions.calculate
    colossus_gunner.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    colossus_gunner.DrownAtMaxDepth = true
    colossus_gunner.MaxDepth = 5.515625f
    colossus_gunner.UnderwaterLifespan(suffocation = 60000L, recovery = 30000L)
    colossus_gunner.Geometry = GeometryForm.representByCylinder(radius = 3.60935f, height = 5.984375f)
    colossus_gunner.collision.avatarCollisionDamageMax = 300
    colossus_gunner.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    colossus_gunner.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    colossus_gunner.maxForwardSpeed = 17
    colossus_gunner.mass = 709.7f

    peregrine_gunner.Name = "peregrine_gunner"
    peregrine_gunner.MaxHealth = 4500
    peregrine_gunner.Damageable = true
    peregrine_gunner.Repairable = true
    peregrine_gunner.RepairIfDestroyed = false
    peregrine_gunner.shieldUiAttribute = 79
    peregrine_gunner.MaxShields = 3000
    peregrine_gunner.ShieldPeriodicDelay = 500
    peregrine_gunner.ShieldDamageDelay = 3500
    peregrine_gunner.ShieldAutoRecharge = 45
    peregrine_gunner.ShieldAutoRechargeSpecial = 85
    peregrine_gunner.DefaultShields = peregrine_gunner.MaxShields
    peregrine_gunner.Seats += 0       -> driverSeat
    peregrine_gunner.Seats += 1       -> normalSeat
    peregrine_gunner.controlledWeapons(seat = 0, weapons = Set(2, 3))
    peregrine_gunner.controlledWeapons(seat = 1, weapon = 4)
    peregrine_gunner.Weapons += 2     -> peregrine_dual_machine_gun_left
    peregrine_gunner.Weapons += 3     -> peregrine_dual_machine_gun_right
    peregrine_gunner.Weapons += 4     -> peregrine_particle_cannon
    peregrine_gunner.MountPoints += 1 -> MountInfo(0)
    peregrine_gunner.MountPoints += 2 -> MountInfo(1)
    peregrine_gunner.subsystems = bfrGunnerSubsystems
    peregrine_gunner.TrunkSize = InventoryTile.Tile1518
    peregrine_gunner.TrunkOffset = 30
    peregrine_gunner.TrunkLocation = Vector3(0f, -5f, 0f)
    peregrine_gunner.AutoPilotSpeeds = (5, 1)
    peregrine_gunner.Packet = battleFrameConverter
    peregrine_gunner.DestroyedModel = None
    peregrine_gunner.destructionDelay = Some(4000L)
    peregrine_gunner.JackingDuration = Array(0, 62, 60, 30)
    peregrine_gunner.RadiationShielding = 0.5f
    peregrine_gunner.DamageUsing = DamageCalculations.AgainstBfr
    peregrine_gunner.Model = BfrResolutions.calculate
    peregrine_gunner.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    peregrine_gunner.DrownAtMaxDepth = true
    peregrine_gunner.MaxDepth = 6.03125f
    peregrine_gunner.UnderwaterLifespan(suffocation = 60000L, recovery = 30000L)
    peregrine_gunner.Geometry = GeometryForm.representByCylinder(radius = 3.60935f, height = 6.421875f)
    peregrine_gunner.collision.avatarCollisionDamageMax = 300
    peregrine_gunner.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    peregrine_gunner.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    peregrine_gunner.maxForwardSpeed = 17
    peregrine_gunner.mass = 713f

    val battleFrameFlightConverter = new BattleFrameFlightConverter
    aphelion_flight.Name = "aphelion_flight"
    aphelion_flight.MaxHealth = 3500
    aphelion_flight.Damageable = true
    aphelion_flight.Repairable = true
    aphelion_flight.RepairIfDestroyed = false
    aphelion_flight.CanFly = true
    aphelion_flight.shieldUiAttribute = 79
    aphelion_flight.MaxShields = 2500
    aphelion_flight.ShieldPeriodicDelay = 500
    aphelion_flight.ShieldDamageDelay = 3500
    aphelion_flight.ShieldAutoRecharge = 12 //12.5
    aphelion_flight.ShieldAutoRechargeSpecial = 25
    aphelion_flight.ShieldDrain = 30
    aphelion_flight.DefaultShields = aphelion_flight.MaxShields
    aphelion_flight.Seats += 0       -> bailableSeat
    aphelion_flight.controlledWeapons(seat = 0, weapons = Set(1, 2))
    aphelion_flight.Weapons += 1     -> aphelion_ppa_left
    aphelion_flight.Weapons += 2     -> aphelion_ppa_right
    aphelion_flight.MountPoints += 1 -> MountInfo(0)
    aphelion_flight.subsystems = bfrFlightSubsystems
    aphelion_flight.TrunkSize = InventoryTile.Tile1511
    aphelion_flight.TrunkOffset = 30
    aphelion_flight.TrunkLocation = Vector3(0f, -2f, 0f)
    aphelion_flight.AutoPilotSpeeds = (5, 1)
    aphelion_flight.Packet = battleFrameFlightConverter
    aphelion_flight.DestroyedModel = None
    aphelion_flight.destructionDelay = Some(4000L)
    aphelion_flight.JackingDuration = Array(0, 62, 60, 30)
    aphelion_flight.RadiationShielding = 0.5f
    aphelion_flight.DamageUsing = DamageCalculations.AgainstBfr
    aphelion_flight.Model = BfrResolutions.calculate
    aphelion_flight.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    aphelion_flight.DrownAtMaxDepth = true
    aphelion_flight.MaxDepth = 5.09375f
    aphelion_flight.UnderwaterLifespan(suffocation = 60000L, recovery = 30000L)
    aphelion_flight.Geometry = GeometryForm.representByCylinder(radius = 1.98045f, height = 6.03125f)
    aphelion_flight.MaxCapacitor = 156
    aphelion_flight.DefaultCapacitor = aphelion_flight.MaxCapacitor
    aphelion_flight.CapacitorDrain = 16
    aphelion_flight.CapacitorDrainSpecial = 3
    aphelion_flight.CapacitorRecharge = 42
    aphelion_flight.collision.avatarCollisionDamageMax = 300
    aphelion_flight.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    aphelion_flight.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    aphelion_flight.maxForwardSpeed = 35
    aphelion_flight.mass = 615.1f

    colossus_flight.Name = "colossus_flight"
    colossus_flight.MaxHealth = 3500
    colossus_flight.Damageable = true
    colossus_flight.Repairable = true
    colossus_flight.RepairIfDestroyed = false
    colossus_flight.CanFly = true
    colossus_flight.shieldUiAttribute = 79
    colossus_flight.MaxShields = 2500
    colossus_flight.ShieldPeriodicDelay = 500
    colossus_flight.ShieldDamageDelay = 3500
    colossus_flight.ShieldAutoRecharge = 12 //12.5
    colossus_flight.ShieldAutoRechargeSpecial = 25
    colossus_flight.ShieldDrain = 30
    colossus_flight.DefaultShields = colossus_flight.MaxShields
    colossus_flight.Seats += 0       -> bailableSeat
    colossus_flight.controlledWeapons(seat = 0, weapons = Set(1, 2))
    colossus_flight.Weapons += 1     -> colossus_tank_cannon_left
    colossus_flight.Weapons += 2     -> colossus_tank_cannon_right
    colossus_flight.MountPoints += 1 -> MountInfo(0)
    colossus_flight.subsystems = bfrFlightSubsystems
    colossus_flight.TrunkSize = InventoryTile.Tile1511
    colossus_flight.TrunkOffset = 30
    colossus_flight.TrunkLocation = Vector3(0f, -5f, 0f)
    colossus_flight.AutoPilotSpeeds = (5, 1)
    colossus_flight.Packet = battleFrameFlightConverter
    colossus_flight.DestroyedModel = None
    colossus_flight.destructionDelay = Some(4000L)
    colossus_flight.JackingDuration = Array(0, 62, 60, 30)
    colossus_flight.RadiationShielding = 0.5f
    colossus_flight.DamageUsing = DamageCalculations.AgainstBfr
    colossus_flight.Model = BfrResolutions.calculate
    colossus_flight.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    colossus_flight.DrownAtMaxDepth = true
    colossus_flight.MaxDepth = 5.515625f
    colossus_flight.UnderwaterLifespan(suffocation = 60000L, recovery = 30000L)
    colossus_flight.Geometry = GeometryForm.representByCylinder(radius = 3.60935f, height = 5.984375f)
    colossus_flight.MaxCapacitor = 156
    colossus_flight.DefaultCapacitor = colossus_flight.MaxCapacitor
    colossus_flight.CapacitorDrain = 16
    colossus_flight.CapacitorDrainSpecial = 3
    colossus_flight.CapacitorRecharge = 42
    colossus_flight.collision.avatarCollisionDamageMax = 300
    colossus_flight.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    colossus_flight.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    colossus_flight.maxForwardSpeed = 34
    colossus_flight.mass = 709.7f

    peregrine_flight.Name = "peregrine_flight"
    peregrine_flight.MaxHealth = 3500
    peregrine_flight.Damageable = true
    peregrine_flight.Repairable = true
    peregrine_flight.RepairIfDestroyed = false
    peregrine_flight.CanFly = true
    peregrine_flight.shieldUiAttribute = 79
    peregrine_flight.MaxShields = 2500
    peregrine_flight.ShieldPeriodicDelay = 500
    peregrine_flight.ShieldDamageDelay = 3500
    peregrine_flight.ShieldAutoRecharge = 12 //12.5
    peregrine_flight.ShieldAutoRechargeSpecial = 25
    peregrine_flight.ShieldDrain = 30
    peregrine_flight.DefaultShields = peregrine_flight.MaxShields
    peregrine_flight.Seats += 0       -> bailableSeat
    peregrine_flight.controlledWeapons(seat = 0, weapons = Set(1, 2))
    peregrine_flight.Weapons += 1     -> peregrine_dual_machine_gun_left
    peregrine_flight.Weapons += 2     -> peregrine_dual_machine_gun_right
    peregrine_flight.MountPoints += 1 -> MountInfo(0)
    peregrine_flight.subsystems = bfrFlightSubsystems
    peregrine_flight.TrunkSize = InventoryTile.Tile1511
    peregrine_flight.TrunkOffset = 30
    peregrine_flight.TrunkLocation = Vector3(0f, -5f, 0f)
    peregrine_flight.AutoPilotSpeeds = (5, 1)
    peregrine_flight.Packet = battleFrameFlightConverter
    peregrine_flight.DestroyedModel = None
    peregrine_flight.destructionDelay = Some(4000L)
    peregrine_flight.JackingDuration = Array(0, 62, 60, 30)
    peregrine_flight.RadiationShielding = 0.5f
    peregrine_flight.DamageUsing = DamageCalculations.AgainstBfr
    peregrine_flight.Model = BfrResolutions.calculate
    peregrine_flight.innateDamage = new DamageWithPosition {
      CausesDamageType = DamageType.One
      Damage0 = 400
      Damage1 = 500
      DamageRadius = 10
      DamageAtEdge = 0.2f
      Modifiers = ExplodingRadialDegrade
    }
    peregrine_flight.DrownAtMaxDepth = true
    peregrine_flight.MaxDepth = 6.03125f
    peregrine_flight.UnderwaterLifespan(suffocation = 60000L, recovery = 30000L)
    peregrine_flight.Geometry = GeometryForm.representByCylinder(radius = 3.60935f, height = 6.421875f)
    peregrine_flight.MaxCapacitor = 156
    peregrine_flight.DefaultCapacitor = peregrine_flight.MaxCapacitor
    peregrine_flight.CapacitorDrain = 16
    peregrine_flight.CapacitorDrainSpecial = 3
    peregrine_flight.CapacitorRecharge = 42
    peregrine_flight.collision.avatarCollisionDamageMax = 300
    peregrine_flight.collision.xy = CollisionXYData(Array((0.2f, 1), (0.35f, 5), (0.55f, 20), (0.75f, 40), (1f, 60)))
    peregrine_flight.collision.z = CollisionZData(Array((25f, 2), (40f, 4), (60f, 8), (85f, 16), (115f, 32)))
    peregrine_flight.maxForwardSpeed = 35
    peregrine_flight.mass = 713f
  }
}
