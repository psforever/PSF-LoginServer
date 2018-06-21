// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.turret

import net.psforever.objects.definition.{ObjectDefinition, ToolDefinition}

import scala.collection.mutable

class MannedTurretDefinition(private val objectId : Int) extends ObjectDefinition(objectId) {
  private var maxHealth : Int = 100
  /* key - entry point index, value - seat index */
  private val mountPoints : mutable.HashMap[Int, Int] = mutable.HashMap()
  private val weapons : mutable.HashMap[Int, mutable.HashMap[TurretUpgrade.Value, ToolDefinition]] =
    mutable.HashMap[Int, mutable.HashMap[TurretUpgrade.Value, ToolDefinition]]()
  private var factionLocked : Boolean = true
  private var hasReserveAmmunition : Boolean = true
  Name = "manned_turret"

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
