// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

import net.psforever.objects._
import net.psforever.objects.ce.{DeployableCategory, DeployedItem}
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.vital.DamagingActivity
import net.psforever.objects.zones.blockmap.SectorPopulation
import net.psforever.types.{DriveState, ExoSuitType, ImplantType, LatticeBenefit, PlanetSideEmpire, Vector3}

final case class TargetValidation(category: EffectTarget.Category.Value, test: EffectTarget.Validation.Value)

object EffectTarget {

  /**
    * A classification of the target of interactions.
    * Arbitrary, but useful.
    */
  object Category extends Enumeration {
    val Aircraft, Deployable, Equipment, Player, Turret, Vehicle = Value
  }

  object Validation {
    type Value = PlanetSideGameObject => Boolean

    //noinspection ScalaUnusedSymbol
    def Invalid(target: PlanetSideGameObject): Boolean = false

    def Medical(target: PlanetSideGameObject): Boolean =
      target match {
        case p: Player =>
          p.Health > 0 && (p.Health < p.MaxHealth || p.Armor < p.MaxArmor)
        case _ =>
          false
      }

    def HealthCrystal(target: PlanetSideGameObject): Boolean =
      target match {
        case p: Player =>
          p.Health > 0 && p.Health < p.MaxHealth
        case _ =>
          false
      }

    def RepairCrystal(target: PlanetSideGameObject): Boolean =
      target match {
        case p: Player =>
          p.Health > 0 && p.Armor < p.MaxArmor
        case _ =>
          false
      }

    def VehicleCrystal(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle => RepairSilo(v) || PadLanding(v) || AncientVehicleWeaponRecharge(v)
        case _ => false
      }

    def LodestarRepair(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle => RepairSilo(v) || PadLanding(v)
        case _ => false
      }

    /**
     * To repair at this silo, the vehicle:
     * can not be a flight vehicle,
     * must have some health already, but does not have all its health,
     * and can not have taken damage in the last five seconds.
    */
    def RepairSilo(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle => !GlobalDefinitions.isFlightVehicle(v.Definition) && CommonRepairConditions(v)
        case _ => false
      }

    /**
     * To repair at this landing pad, the vehicle:
     * be a flight vehicle,
     * must have some health already, but does not have all its health,
     * and can not have taken damage in the last five seconds.
     */
    def PadLanding(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle => GlobalDefinitions.isFlightVehicle(v.Definition) && CommonRepairConditions(v)
        case _ => false
      }

    private def CommonRepairConditions(v: Vehicle): Boolean = {
      v.Health > 0 && v.Health < v.MaxHealth &&
        (v.History.findLast { entry => entry.isInstanceOf[DamagingActivity] } match {
          case Some(entry) if System.currentTimeMillis() - entry.time < 5000L => false
          case _ => true
        })
    }

    def Player(target: PlanetSideGameObject): Boolean =
      target match {
        case p: Player =>
          p.isAlive
        case _ =>
          false
      }

    def MotionSensor(target: PlanetSideGameObject): Boolean =
      target match {
        case s: SensorDeployable =>
          s.Health > 0
        case _ =>
          false
      }

    def Spitfire(target: PlanetSideGameObject): Boolean =
      target match {
        case t: TurretDeployable =>
          t.Definition.DeployCategory == DeployableCategory.SmallTurrets && t.Health > 0
        case _ =>
          false
      }

    def Turret(target: PlanetSideGameObject): Boolean =
      target match {
        case t: TurretDeployable =>
          t.Definition.DeployCategory == DeployableCategory.FieldTurrets && t.Health > 0
        case t: FacilityTurret =>
          t.Health > 0
        case _ =>
          false
      }

    def Vehicle(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle =>
          v.Health > 0
        case _ =>
          false
      }

    def VehicleNotAMS(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle =>
          v.Health > 0 && v.Definition != GlobalDefinitions.ams
        case _ =>
          false
      }

    def AMS(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle =>
          v.Health > 0 && v.Definition == GlobalDefinitions.ams
        case _ =>
          false
      }

    def Aircraft(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle =>
          GlobalDefinitions.isFlightVehicle(v.Definition) && v.Health > 0
        case _ =>
          false
      }

    def AncientWeaponRecharge(target: PlanetSideGameObject): Boolean = {
      target match {
        case p: Player =>
          (p.Holsters().flatMap { _.Equipment }.toSeq ++ p.Inventory.Items.map { _.obj })
            .flatMap {
              case weapon: Tool => weapon.AmmoSlots
              case _            => Nil
            }
            .exists { slot => slot.Box.Capacity < slot.Definition.Magazine }
        case _ =>
          false
      }
    }

    def AncientVehicleWeaponRecharge(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle =>
          GlobalDefinitions.isCavernVehicle(v.Definition) && v.Health > 0 &&
          v.Weapons.values
            .map { _.Equipment }
            .flatMap {
              case Some(weapon: Tool) => weapon.AmmoSlots
              case _                  => Nil
            }
            .exists { slot => slot.Box.Capacity < slot.Definition.Magazine }
        case _ =>
          false
      }

    def SmallRoboticsTurretValidatePlayerTarget(target: PlanetSideGameObject): Boolean =
      target match {
        case p: Player
          if p.ExoSuit != ExoSuitType.MAX && p.VehicleSeated.isEmpty =>
          val now = System.currentTimeMillis()
          val pos = p.Position
          val faction = p.Faction
          val sector = p.Zone.blockMap.sector(p.Position, range = 51f)
          lazy val tookDamage = p.LastDamage.exists(dam => dam.adversarial.nonEmpty && now - dam.interaction.hitTime < 2000L)
          //todo equipment-use usually a violation for any equipment type
          lazy val usedEquipment = (p.Holsters().flatMap(_.Equipment) ++ p.Inventory.Items.map(_.obj))
            .collect {
              case t: Tool
                if !(t.Projectile == GlobalDefinitions.no_projectile || t.Projectile.GrenadeProjectile || t.Size == EquipmentSize.Melee) =>
                now - t.LastDischarge
            }
            .exists(_ < 2000L)
          lazy val silentRunActive = p.avatar.implants.flatten.find(a => a.definition.implantType == ImplantType.SilentRun).exists(_.active)
          lazy val cloakedByInfiltrationSuit = p.ExoSuit == ExoSuitType.Infiltration && p.Cloaked
          lazy val movingFast = p.isMoving(test = 17d)
          if (radarCloakedAms(sector, pos) || radarCloakedAegis(sector, pos)) false
          else if (radarCloakedSensor(sector, pos, faction)) tookDamage || usedEquipment
          else if (radarEnhancedInterlink(sector, pos, faction) || radarEnhancedSensor(sector, pos, faction)) true
          else tookDamage || usedEquipment || p.Jumping || (cloakedByInfiltrationSuit || !silentRunActive) && movingFast
        case _ =>
          false
      }

    def SmallRoboticsTurretValidateMaxTarget(target: PlanetSideGameObject): Boolean =
      target match {
        case p: Player
          if p.ExoSuit == ExoSuitType.MAX && p.VehicleSeated.isEmpty =>
          val now = System.currentTimeMillis()
          val pos = p.Position
          val faction = p.Faction
          val sector = p.Zone.blockMap.sector(p.Position, range = 51f)
          lazy val tookDamage = p.LastDamage.exists(dam => dam.adversarial.nonEmpty && now - dam.interaction.hitTime < 2000L)
          lazy val usedEquipment = p.Holsters().flatMap(_.Equipment)
            .collect { case t: Tool => now - t.LastDischarge }
            .exists(_ < 2000L)
          if (radarCloakedAms(sector, pos) || radarCloakedAegis(sector, pos)) false
          else if (radarCloakedSensor(sector, pos, faction)) tookDamage || usedEquipment
          else true
        case _ =>
          false
      }

    def FacilityTurretValidateMaxTarget(target: PlanetSideGameObject): Boolean =
      target match {
        case p: Player
          if p.ExoSuit == ExoSuitType.MAX && p.VehicleSeated.isEmpty =>
          val pos = p.Position
          val faction = p.Faction
          val sector = p.Zone.blockMap.sector(p.Position, range = 51f)
          !(radarCloakedAms(sector, pos) ||
            radarCloakedAegis(sector, pos) ||
            radarCloakedSensor(sector, pos, faction)) &&
            p.isMoving(test = 17d)
        case _ =>
          false
      }

    def AutoTurretBlankPlayerTarget(target: PlanetSideGameObject): Boolean =
      target match {
        case p: Player =>
          val pos = p.Position
          val sector = p.Zone.blockMap.sector(p.Position, range = 51f)
          p.VehicleSeated.nonEmpty || radarCloakedAms(sector, pos) || radarCloakedAegis(sector, pos)
        case _ =>
          false
      }

    def SmallRoboticsTurretValidateGroundVehicleTarget(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle
          if v.MountedIn.isEmpty =>
          val now = System.currentTimeMillis()
          val vdef = v.Definition
          lazy val tookDamage = v.LastDamage.exists(dam => dam.adversarial.nonEmpty && now - dam.interaction.hitTime< 2000L)
          lazy val usedEquipment = v.Weapons.values.flatMap(_.Equipment)
            .collect { case t: Tool => now - t.LastDischarge }
            .exists(_ < 2000L)
          !GlobalDefinitions.isFlightVehicle(vdef) && (
            if (vdef == GlobalDefinitions.ams && v.DeploymentState == DriveState.Deployed) false
            else !v.Cloaked && ( tookDamage || usedEquipment))
        case _ =>
          false
      }

    def SmallRoboticsTurretValidateAircraftTarget(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle if GlobalDefinitions.isFlightVehicle(v.Definition) =>
          !v.Cloaked
        case _ =>
          false
      }

    def FacilityTurretValidateGroundVehicleTarget(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle
          if v.MountedIn.isEmpty =>
          val now = System.currentTimeMillis()
          val vdef = v.Definition
          lazy val tookDamage = v.LastDamage.exists(dam => dam.adversarial.nonEmpty && now - dam.interaction.hitTime< 2000L)
          lazy val usedEquipment = v.Weapons.values.flatMap(_.Equipment)
            .collect { case t: Tool => now - t.LastDischarge }
            .exists(_ < 2000L)
          !GlobalDefinitions.isFlightVehicle(vdef) && (
            if (vdef == GlobalDefinitions.ams && v.DeploymentState == DriveState.Deployed) false
            else if (
              v.Cloaked ||
                GlobalDefinitions.isAtvVehicle(vdef) ||
                vdef == GlobalDefinitions.two_man_assault_buggy ||
                vdef == GlobalDefinitions.skyguard
            ) tookDamage || usedEquipment
            else true)
        case _ =>
          false
      }

    def FacilityTurretValidateAircraftTarget(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle if GlobalDefinitions.isFlightVehicle(v.Definition) =>
          v.Definition != GlobalDefinitions.mosquito || !v.Cloaked
        case _ =>
          false
      }

    def AutoTurretBlankVehicleTarget(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle =>
          (v.Definition == GlobalDefinitions.ams && v.DeploymentState == DriveState.Deployed) || v.MountedIn.nonEmpty || v.Cloaked
        case _ =>
          false
      }
  }

  private def radarEnhancedInterlink(
                                      sector: SectorPopulation,
                                      position: Vector3,
                                      faction: PlanetSideEmpire.Value
                                    ): Boolean = {
    sector.buildingList.collect {
      case b =>
        b.Faction != faction &&
          b.hasLatticeBenefit(LatticeBenefit.InterlinkFacility) &&
          Vector3.DistanceSquared(b.Position, position).toDouble < math.pow(b.Definition.SOIRadius.toDouble, 2d)
    }.contains(true)
  }

  private def radarEnhancedSensor(
                                   sector: SectorPopulation,
                                   position: Vector3,
                                   faction: PlanetSideEmpire.Value
                                 ): Boolean = {
    sector.deployableList.collect {
      case d: SensorDeployable =>
        !d.Destroyed &&
          d.Definition.Item == DeployedItem.motionalarmsensor &&
          d.Faction != faction &&
          !d.Jammed && Vector3.DistanceSquared(d.Position, position) < 2500f
    }.contains(true)
  }

  private def radarCloakedAms(
                               sector: SectorPopulation,
                               position: Vector3
                             ): Boolean = {
    sector.vehicleList.collect {
      case v =>
        !v.Destroyed &&
          v.Definition == GlobalDefinitions.ams &&
          v.DeploymentState == DriveState.Deployed &&
          !v.Jammed &&
          Vector3.DistanceSquared(v.Position, position) < 144f
    }.contains(true)
  }

  private def radarCloakedAegis(
                                 sector: SectorPopulation,
                                 position: Vector3
                               ): Boolean = {
    sector.deployableList.collect {
      case d: ShieldGeneratorDeployable =>
        !d.Destroyed &&
          !d.Jammed &&
          Vector3.DistanceSquared(d.Position, position) < 100f
    }.contains(true)
  }

  private def radarCloakedSensor(
                                  sector: SectorPopulation,
                                  position: Vector3,
                                  faction: PlanetSideEmpire.Value
                                ): Boolean = {
    sector.deployableList.collect {
      case d: SensorDeployable =>
        !d.Destroyed &&
          d.Definition.Item == DeployedItem.sensor_shield &&
          d.Faction == faction &&
          !d.Jammed &&
          Vector3.DistanceSquared(d.Position, position) < 900f
    }.contains(true)
  }
}
