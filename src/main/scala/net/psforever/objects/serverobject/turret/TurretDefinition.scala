// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.{ObjectDefinition, ToolDefinition, WithShields}
import net.psforever.objects.vehicles.{MountableWeaponsDefinition, Turrets}
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.objects.vital.resolution.DamageResistanceModel

import scala.collection.mutable
import scala.concurrent.duration._

final case class Automation(
                             ranges: AutoRanges,
                             checks: AutoChecks,
                             /** the boundary for target searching is typically a sphere of `ranges.detection` radius;
                              * instead, takes the shape of a cylinder of `ranges.detection` radius and height */
                             cylindrical: Boolean = false,
                             /** if target searching is performed in the shape of a cylinder,
                              * add height on top of the cylinder's normal height */
                             cylindricalExtraHeight: Float = 0, //m
                             /** how long after the last target engagement
                              * or how long into the current target engagement
                              * before the turret may counterattack damage;
                              * set to `0L` to never retaliate */
                             retaliatoryDelay: Long = 0, //ms
                             /** if the turret has a current target,
                              * allow for retaliation against a different target */
                             retaliationOverridesTarget: Boolean = true,
                             /** frequency at which the turret will test target for reachability */
                             detectionSweepTime: FiniteDuration = 1.seconds,
                             cooldowns: AutoCooldowns = AutoCooldowns(),
                             /** if the turret weapon has multiple fire modes,
                              * revert to the base fire mode before engaging in target testing or other automatic operations */
                             revertToDefaultFireMode: Boolean = true,
                             /** the simulated weapon fire rate for self-reporting (internal damage loop) */
                             refireTime: FiniteDuration = 1.seconds //60rpm
                           )

final case class AutoRanges(
                             /** distance at which a target is first noticed */
                             detection: Float, //m
                             /** distance at which the target is tested */
                             trigger: Float, //m
                             /** distance away from the source of damage before the turret stops engaging */
                             escape: Float //m
                           ) {
  assert(detection >= trigger, "detection range must be greater than or equal to trigger range")
  assert(escape >= trigger, "escape range must be greater than or equal to trigger range")
}

final case class AutoChecks(
                             /** reasons why this target should be engaged */
                             validation: List[PlanetSideGameObject => Boolean],
                             /** reasons why an ongoing target engagement should be stopped */
                             blanking: List[PlanetSideGameObject => Boolean] = Nil
                           )

final case class AutoCooldowns(
                                /** when the target gets switched (generic) */
                                targetSelect: Long = 1500L, //ms
                                /** when the target escapes being damaged */
                                missedShot: Long = 3000L, //ms
                                /** when the target gets destroyed during an ongoing engagement */
                                targetElimination: Long = 0L //ms
                              )

/**
  * The definition for any `WeaponTurret`.
  */
trait TurretDefinition
  extends MountableWeaponsDefinition
  with ResistanceProfileMutators
  with DamageResistanceModel
  with WithShields {
  odef: ObjectDefinition =>
  Turrets(odef.ObjectId) //let throw NoSuchElementException
  /* key - upgrade, value - weapon definition */
  private val weaponPaths: mutable.HashMap[Int, mutable.HashMap[TurretUpgrade.Value, ToolDefinition]] =
    mutable.HashMap[Int, mutable.HashMap[TurretUpgrade.Value, ToolDefinition]]()

  /** can only be mounted by owning faction when `true` */
  private var factionLocked: Boolean = true

  /** creates internal ammunition reserves that can not become depleted */
  private var hasReserveAmmunition: Boolean = false

  /** */
  private var turretAutomation: Option[Automation] = None

  def WeaponPaths: mutable.HashMap[Int, mutable.HashMap[TurretUpgrade.Value, ToolDefinition]] = weaponPaths

  def FactionLocked: Boolean = factionLocked

  def FactionLocked_=(ownable: Boolean): Boolean = {
    factionLocked = ownable
    FactionLocked
  }

  def ReserveAmmunition: Boolean = hasReserveAmmunition

  def ReserveAmmunition_=(reserved: Boolean): Boolean = {
    hasReserveAmmunition = reserved
    ReserveAmmunition
  }

  def AutoFire: Option[Automation] = turretAutomation

  def AutoFire_=(auto: Automation): Option[Automation] = {
    AutoFire_=(Some(auto))
  }

  def AutoFire_=(auto: Option[Automation]): Option[Automation] = {
    turretAutomation = auto
    turretAutomation
  }
}
