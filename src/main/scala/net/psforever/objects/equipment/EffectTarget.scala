// Copyright (c) 2017 PSForever
package net.psforever.objects.equipment

import net.psforever.objects._
import net.psforever.objects.ce.DeployableCategory
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.vital.DamagingActivity

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

    def RepairSilo(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle =>
          !GlobalDefinitions.isFlightVehicle(v.Definition) && v.Health > 0 && v.Health < v.MaxHealth && v.History.find(x => x.isInstanceOf[DamagingActivity] && x.t >= (System.nanoTime - 5000000000L)).isEmpty
        case _ =>
          false
      }

    def PadLanding(target: PlanetSideGameObject): Boolean =
      target match {
        case v: Vehicle =>
          GlobalDefinitions.isFlightVehicle(v.Definition) && v.Health > 0 && v.Health < v.MaxHealth && v.History.find(x => x.isInstanceOf[DamagingActivity] && x.t >= (System.nanoTime - 5000000000L)).isEmpty
        case _ =>
          false
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
  }
}
