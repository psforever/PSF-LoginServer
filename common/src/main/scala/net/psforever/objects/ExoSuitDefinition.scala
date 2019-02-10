// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.equipment.EquipmentSize
import net.psforever.objects.inventory.InventoryTile
import net.psforever.objects.vital._
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.types.ExoSuitType

/**
  * A definition for producing the personal armor the player wears.
  * Players are influenced by the exo-suit they wear in a variety of ways, with speed and available equipment slots being major differences.
  * @param suitType the `Enumeration` corresponding to this exo-suit
  */
class ExoSuitDefinition(private val suitType : ExoSuitType.Value) extends ResistanceProfileMutators
  with DamageResistanceModel {
  protected var permission : Int = 0 //TODO certification type?
  protected var maxArmor : Int = 0
  protected val holsters : Array[EquipmentSize.Value] = Array.fill[EquipmentSize.Value](5)(EquipmentSize.Blocked)
  protected var inventoryScale : InventoryTile = InventoryTile.Tile11 //override with custom InventoryTile
  protected var inventoryOffset : Int = 0
  Damage = StandardInfantryDamage
  Resistance = StandardInfantryResistance
  Model = StandardResolutions.Infantry

  def SuitType : ExoSuitType.Value = suitType

  def MaxArmor : Int = maxArmor

  def MaxArmor_=(armor : Int) : Int = {
    maxArmor = math.min(math.max(0, armor), 65535)
    MaxArmor
  }

  def InventoryScale : InventoryTile = inventoryScale

  def InventoryScale_=(scale : InventoryTile) : InventoryTile = {
    inventoryScale = scale
    InventoryScale
  }

  def InventoryOffset : Int = inventoryOffset

  def InventoryOffset_=(offset : Int) : Int = {
    inventoryOffset = math.min(math.max(0, offset), 65535)
    InventoryOffset
  }

  def Holsters : Array[EquipmentSize.Value] = holsters

  def Holster(slot : Int) : EquipmentSize.Value = {
    if(slot >= 0 && slot < 5) {
      holsters(slot)
    }
    else {
      EquipmentSize.Blocked
    }
  }

  def Holster(slot : Int, value : EquipmentSize.Value) : EquipmentSize.Value = {
    if(slot >= 0 && slot < 5) {
      holsters(slot) = value
      holsters(slot)
    }
    else {
      EquipmentSize.Blocked
    }
  }

  def Use : ExoSuitDefinition = this
}

class SpecialExoSuitDefinition(private val suitType : ExoSuitType.Value) extends ExoSuitDefinition(suitType) {
  private var activatedSpecial : SpecialExoSuitDefinition.Mode.Value = SpecialExoSuitDefinition.Mode.Normal

  def UsingSpecial : SpecialExoSuitDefinition.Mode.Value = activatedSpecial

  def UsingSpecial_=(state : SpecialExoSuitDefinition.Mode.Value) : SpecialExoSuitDefinition.Mode.Value = {
    activatedSpecial = state
    UsingSpecial
  }

  override def Use : ExoSuitDefinition = {
    val obj = new SpecialExoSuitDefinition(SuitType)
    obj.MaxArmor = MaxArmor
    obj.InventoryScale = InventoryScale
    obj.InventoryOffset = InventoryOffset
    obj.Subtract.Damage0 = Subtract.Damage0
    obj.Subtract.Damage1 = Subtract.Damage1
    obj.Subtract.Damage2 = Subtract.Damage2
    obj.Subtract.Damage3 = Subtract.Damage3
    obj.ResistanceDirectHit = ResistanceDirectHit
    obj.ResistanceSplash = ResistanceSplash
    obj.ResistanceAggravated = ResistanceAggravated
    obj.Damage = Damage
    obj.Resistance = Resistance
    obj.Model = Model
    (0 until 5).foreach(index => { obj.Holster(index, Holster(index)) })
    obj
  }
}

object SpecialExoSuitDefinition {
  def apply(suitType : ExoSuitType.Value) : SpecialExoSuitDefinition = {
    new SpecialExoSuitDefinition(suitType)
  }

  object Mode extends Enumeration {
    type Type = Value

    val
    Normal,
    Anchored,
    Overdrive,
    Shielded
    = Value
  }
}

object ExoSuitDefinition {
  final val Standard = ExoSuitDefinition(ExoSuitType.Standard)
  Standard.MaxArmor = 50
  Standard.InventoryScale = InventoryTile.Tile96
  Standard.InventoryOffset = 6
  Standard.Holster(0, EquipmentSize.Pistol)
  Standard.Holster(2, EquipmentSize.Rifle)
  Standard.Holster(4, EquipmentSize.Melee)
  Standard.ResistanceDirectHit = 4
  Standard.ResistanceSplash = 15
  Standard.ResistanceAggravated = 8

  final val Agile = ExoSuitDefinition(ExoSuitType.Agile)
  Agile.MaxArmor = 100
  Agile.InventoryScale = InventoryTile.Tile99
  Agile.InventoryOffset = 6
  Agile.Holster(0, EquipmentSize.Pistol)
  Agile.Holster(1, EquipmentSize.Pistol)
  Agile.Holster(2, EquipmentSize.Rifle)
  Agile.Holster(4, EquipmentSize.Melee)
  Agile.ResistanceDirectHit = 6
  Agile.ResistanceSplash = 25
  Agile.ResistanceAggravated = 10

  final val Reinforced = ExoSuitDefinition(ExoSuitType.Reinforced)
  Reinforced.permission = 1
  Reinforced.MaxArmor = 200
  Reinforced.InventoryScale = InventoryTile.Tile1209
  Reinforced.InventoryOffset = 6
  Reinforced.Holster(0, EquipmentSize.Pistol)
  Reinforced.Holster(1, EquipmentSize.Pistol)
  Reinforced.Holster(2, EquipmentSize.Rifle)
  Reinforced.Holster(3, EquipmentSize.Rifle)
  Reinforced.Holster(4, EquipmentSize.Melee)
  Reinforced.ResistanceDirectHit = 10
  Reinforced.ResistanceSplash = 35
  Reinforced.ResistanceAggravated = 12

  final val Infiltration = ExoSuitDefinition(ExoSuitType.Infiltration)
  Infiltration.permission = 1
  Infiltration.MaxArmor = 0
  Infiltration.InventoryScale = InventoryTile.Tile66
  Infiltration.InventoryOffset = 6
  Infiltration.Holster(0, EquipmentSize.Pistol)
  Infiltration.Holster(4, EquipmentSize.Melee)

  final val MAX = SpecialExoSuitDefinition(ExoSuitType.MAX)
  MAX.permission = 1
  MAX.MaxArmor = 650
  MAX.InventoryScale = InventoryTile.Tile1612
  MAX.InventoryOffset = 6
  MAX.Holster(0, EquipmentSize.Max)
  MAX.Holster(4, EquipmentSize.Melee)
  MAX.Subtract.Damage1 = -2
  MAX.ResistanceDirectHit = 6
  MAX.ResistanceSplash = 35
  MAX.ResistanceAggravated = 10
  MAX.Damage = StandardMaxDamage
  MAX.Model = StandardResolutions.Max

  def apply(suitType : ExoSuitType.Value) : ExoSuitDefinition = {
    new ExoSuitDefinition(suitType)
  }

  /**
    * A function to retrieve the correct defintion of an exo-suit from the type of exo-suit.
    * @param suit the `Enumeration` corresponding to this exo-suit
    * @return the exo-suit definition
    */
  def Select(suit : ExoSuitType.Value) : ExoSuitDefinition = {
    suit match {
      case ExoSuitType.Agile => ExoSuitDefinition.Agile.Use
      case ExoSuitType.Infiltration => ExoSuitDefinition.Infiltration.Use
      case ExoSuitType.MAX => ExoSuitDefinition.MAX.Use
      case ExoSuitType.Reinforced => ExoSuitDefinition.Reinforced.Use
      case _ => ExoSuitDefinition.Standard.Use
    }
  }
}
