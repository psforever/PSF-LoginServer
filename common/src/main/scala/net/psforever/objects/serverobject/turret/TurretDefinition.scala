// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.definition.{ObjectDefinition, ToolDefinition}
import net.psforever.objects.vehicles.Turrets
import net.psforever.objects.vital.{DamageResistanceModel, StandardResolutions, StandardVehicleDamage, StandardVehicleResistance}
import net.psforever.objects.vital.resistance.ResistanceProfileMutators

import scala.collection.mutable

/**
  * The definition for any `MannedTurret`.
  * @param objectId the object's identifier number
  */
class TurretDefinition(private val objectId : Int) extends ObjectDefinition(objectId)
  with ResistanceProfileMutators
  with DamageResistanceModel {
  Turrets(objectId) //let throw NoSuchElementException

  private var maxHealth : Int = 100
  /* key - entry point index, value - seat index */
  private val mountPoints : mutable.HashMap[Int, Int] = mutable.HashMap()
  /* key - seat number, value - hash map (below) */
  /* key - upgrade, value - weapon definition */
  private val weapons : mutable.HashMap[Int, mutable.HashMap[TurretUpgrade.Value, ToolDefinition]] =
    mutable.HashMap[Int, mutable.HashMap[TurretUpgrade.Value, ToolDefinition]]()
  /** can only be mounted by owning faction when `true` */
  private var factionLocked : Boolean = true
  /** creates internal ammunition reserves that can not become depleted
    * see `MannedTurret.TurretAmmoBox` for details */
  private var hasReserveAmmunition : Boolean = false
  Damage = StandardVehicleDamage
  Resistance = StandardVehicleResistance
  Model = StandardResolutions.FacilityTurrets

  def MaxHealth : Int = maxHealth

  def MaxHealth_=(health : Int) : Int = {
    maxHealth = health
    MaxHealth
  }

  def MountPoints : mutable.HashMap[Int, Int] = mountPoints

  def Weapons : mutable.HashMap[Int, mutable.HashMap[TurretUpgrade.Value, ToolDefinition]] = weapons

  def FactionLocked : Boolean = factionLocked

  def FactionLocked_=(ownable : Boolean) : Boolean =  {
    factionLocked = ownable
    FactionLocked
  }

  def ReserveAmmunition : Boolean = hasReserveAmmunition

  def ReserveAmmunition_=(reserved : Boolean) : Boolean = {
    hasReserveAmmunition = reserved
    ReserveAmmunition
  }
}
