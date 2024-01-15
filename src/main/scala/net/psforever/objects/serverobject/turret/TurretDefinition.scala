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
                             targetDetectionRange: Float, //m
                             targetTriggerRange: Float, //m
                             targetEscapeRange: Float, //m
                             targetValidation: List[PlanetSideGameObject => Boolean],
                             cylindricalCheck: Boolean = false,
                             cylindricalHeight: Float = 0, //m
                             retaliatoryDuration: Long = 0, //ms
                             retaliationOverridesTarget: Boolean = true,
                             initialDetectionSpeed: FiniteDuration = Duration.Zero,
                             detectionSpeed: FiniteDuration = 1.seconds,
                             targetSelectCooldown: Long = 1500L, //ms
                             missedShotCooldown: Long = 3000L, //ms
                             targetEliminationCooldown: Long = 0L, //ms
                             revertToDefaultFireMode: Boolean = true,
                             refireTime: FiniteDuration = 1.seconds //60rpm
                           ) {
  assert(targetDetectionRange > targetTriggerRange, "trigger range must be less than detection range")
}

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
