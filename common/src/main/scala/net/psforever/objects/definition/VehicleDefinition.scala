// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.definition.converter.VehicleConverter
import net.psforever.objects.inventory.InventoryTile

import scala.collection.mutable

/**
  * An object definition system used to construct and retain the parameters of various vehicles.
  * @param objectId the object id the is associated with this sort of `Vehicle`
  */
class VehicleDefinition(objectId : Int) extends ObjectDefinition(objectId) {
  private var maxHealth : Int = 100
  private var maxShields : Int = 0
  /* key - seat index, value - seat object */
  private val seats : mutable.HashMap[Int, SeatDefinition] = mutable.HashMap[Int, SeatDefinition]()
  /* key - entry point index, value - seat index */
  private val mountPoints : mutable.HashMap[Int, Int] = mutable.HashMap()
  /* key - seat index (where this weapon attaches during object construction), value - the weapon on an EquipmentSlot */
  private val weapons : mutable.HashMap[Int, ToolDefinition] = mutable.HashMap[Int, ToolDefinition]()
  private var deployment : Boolean = false
  private val utilities : mutable.ArrayBuffer[Int] = mutable.ArrayBuffer[Int]()
  private var trunkSize : InventoryTile = InventoryTile.None
  private var trunkOffset : Int = 0
  private var canCloak : Boolean = false
  private var canBeOwned : Boolean = true
  Name = "vehicle"
  Packet = new VehicleConverter

  def MaxHealth : Int = maxHealth

  def MaxHealth_=(health : Int) : Int = {
    maxHealth = health
    MaxHealth
  }

  def MaxShields : Int = maxShields

  def MaxShields_=(shields : Int) : Int = {
    maxShields = shields
    MaxShields
  }

  def Seats : mutable.HashMap[Int, SeatDefinition] = seats

  def MountPoints : mutable.HashMap[Int, Int] = mountPoints

  def CanBeOwned : Boolean = canBeOwned

  def CanBeOwned_=(ownable : Boolean) : Boolean =  {
    canBeOwned = ownable
    CanBeOwned
  }

  def CanCloak : Boolean = canCloak

  def CanCloak_=(cloakable : Boolean) : Boolean =  {
    canCloak = cloakable
    CanCloak
  }

  def Weapons : mutable.HashMap[Int, ToolDefinition] = weapons

  def Deployment : Boolean = deployment

  def Deployment_=(deployable : Boolean) : Boolean = {
    deployment = deployable
    Deployment
  }

  def Utilities : mutable.ArrayBuffer[Int] = utilities

  def TrunkSize : InventoryTile = trunkSize

  def TrunkSize_=(tile : InventoryTile) : InventoryTile = {
    trunkSize = tile
    TrunkSize
  }

  def TrunkOffset : Int = trunkOffset

  def TrunkOffset_=(offset : Int) : Int = {
    trunkOffset = offset
    TrunkOffset
  }
}

object VehicleDefinition {
  def apply(objectId: Int) : VehicleDefinition = {
    new VehicleDefinition(objectId)
  }
}
