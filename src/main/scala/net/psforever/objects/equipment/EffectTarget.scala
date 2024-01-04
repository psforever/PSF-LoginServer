// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

import net.psforever.objects._
import net.psforever.objects.ce.DeployableCategory
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.vital.DamagingActivity
import net.psforever.types.{ExoSuitType, ImplantType}

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

    def PlayerOnRadar(target: PlanetSideGameObject): Boolean =
      !target.Destroyed && (target match {
        case p: Player =>
          //TODO attacking breaks stealth
          p.LastDamage.map(_.interaction.hitTime).exists(System.currentTimeMillis() - _ < 3000L) ||
            p.avatar.implants.flatten.find(a => a.definition.implantType == ImplantType.SilentRun).exists(_.active) ||
            (p.isMoving(test = 17d) && !(p.Crouching || p.Cloaked)) ||
            p.Jumping
        case _ =>
          false
      })

    def MaxOnRadar(target: PlanetSideGameObject): Boolean =
      !target.Destroyed && (target match {
        case p: Player =>
          p.ExoSuit == ExoSuitType.MAX && p.isMoving(test = 17d)
        case _ =>
          false
      })

    def VehiclesOnRadar(target: PlanetSideGameObject): Boolean =
      !target.Destroyed && (target match {
        case v: Vehicle =>
          val vdef = v.Definition
          !(v.Cloaked ||
            GlobalDefinitions.isAtvVehicle(vdef) ||
            vdef == GlobalDefinitions.two_man_assault_buggy ||
            vdef == GlobalDefinitions.skyguard)
        case _ =>
          false
      })



    def AircraftOnRadar(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle =>
          GlobalDefinitions.isFlightVehicle(v.Definition) && v.Health > 0 && !v.Cloaked
        case _ =>
          false
      }
  }
}
