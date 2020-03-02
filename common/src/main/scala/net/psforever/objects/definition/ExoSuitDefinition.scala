// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.equipment.EquipmentSize
import net.psforever.objects.inventory.InventoryTile
import net.psforever.objects.vital._
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.types.{CertificationType, ExoSuitType, PlanetSideEmpire}

/**
  * A definition for producing the personal armor the player wears.
  * Players are influenced by the exo-suit they wear in a variety of ways, with speed and available equipment slots being major differences.
  * @param suitType the `Enumeration` corresponding to this exo-suit
  */
class ExoSuitDefinition(private val suitType : ExoSuitType.Value) extends BasicDefinition
  with ResistanceProfileMutators
  with DamageResistanceModel {
  protected var permissions : List[CertificationType.Value] = List.empty
  protected var maxArmor : Int = 0
  protected val holsters : Array[EquipmentSize.Value] = Array.fill[EquipmentSize.Value](5)(EquipmentSize.Blocked)
  protected var inventoryScale : InventoryTile = InventoryTile.Tile11 //override with custom InventoryTile
  protected var inventoryOffset : Int = 0
  protected var maxCapacitor : Int = 0
  protected var capacitorRechargeDelayMillis : Int = 0
  protected var capacitorRechargePerSecond : Int = 0
  protected var capacitorDrainPerSecond : Int = 0
  Name = "exo-suit"
  DamageUsing = StandardInfantryDamage
  ResistUsing = StandardInfantryResistance
  Model = StandardResolutions.Infantry

  def SuitType : ExoSuitType.Value = suitType

  def MaxArmor : Int = maxArmor

  def MaxArmor_=(armor : Int) : Int = {
    maxArmor = math.min(math.max(0, armor), 65535)
    MaxArmor
  }

  def MaxCapacitor : Int = maxCapacitor

  def MaxCapacitor_=(value : Int) : Int = {
    maxCapacitor = value
    maxCapacitor
  }

  def CapacitorRechargeDelayMillis : Int = capacitorRechargeDelayMillis

  def CapacitorRechargeDelayMillis_=(value : Int) : Int = {
    capacitorRechargeDelayMillis = value
    capacitorRechargeDelayMillis
  }

  def CapacitorRechargePerSecond : Int = capacitorRechargePerSecond

  def CapacitorRechargePerSecond_=(value : Int) : Int = {
    capacitorRechargePerSecond = value
    capacitorRechargePerSecond
  }

  def CapacitorDrainPerSecond : Int = capacitorDrainPerSecond

  def CapacitorDrainPerSecond_=(value : Int) : Int = {
    capacitorDrainPerSecond = value
    capacitorDrainPerSecond
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

  def Permissions : List[CertificationType.Value] = permissions

  def Permissions_=(certs : List[CertificationType.Value]) : List[CertificationType.Value] = {
    permissions = certs
    Permissions
  }

  def Use : ExoSuitDefinition = this
}

class SpecialExoSuitDefinition(private val suitType : ExoSuitType.Value) extends ExoSuitDefinition(suitType) {
  Name = "heavy_armor"
  Descriptor = "heavy_armor"

  private var activatedSpecial : SpecialExoSuitDefinition.Mode.Value = SpecialExoSuitDefinition.Mode.Normal

  def UsingSpecial : SpecialExoSuitDefinition.Mode.Value = activatedSpecial

  def UsingSpecial_=(state : SpecialExoSuitDefinition.Mode.Value) : SpecialExoSuitDefinition.Mode.Value = {
    activatedSpecial = state
    UsingSpecial
  }

  override def Use : ExoSuitDefinition = {
    val obj = new SpecialExoSuitDefinition(SuitType)
    obj.Permissions = Permissions
    obj.MaxArmor = MaxArmor
    obj.MaxCapacitor = MaxCapacitor
    obj.CapacitorRechargePerSecond = CapacitorRechargePerSecond
    obj.CapacitorDrainPerSecond = CapacitorDrainPerSecond
    obj.CapacitorRechargeDelayMillis = CapacitorRechargeDelayMillis
    obj.InventoryScale = InventoryScale
    obj.InventoryOffset = InventoryOffset
    obj.Subtract.Damage0 = Subtract.Damage0
    obj.Subtract.Damage1 = Subtract.Damage1
    obj.Subtract.Damage2 = Subtract.Damage2
    obj.Subtract.Damage3 = Subtract.Damage3
    obj.ResistanceDirectHit = ResistanceDirectHit
    obj.ResistanceSplash = ResistanceSplash
    obj.ResistanceAggravated = ResistanceAggravated
    obj.DamageUsing = DamageUsing
    obj.ResistUsing = ResistUsing
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
  def apply(suitType : ExoSuitType.Value) : ExoSuitDefinition = {
    new ExoSuitDefinition(suitType)
  }

  /**
    * A function to retrieve the correct defintion of an exo-suit from the type of exo-suit.
    * @param suit the `Enumeration` corresponding to this exo-suit
    * @param faction the faction the player belongs to for this exosuit
    * @return the exo-suit definition
    */
  def Select(suit : ExoSuitType.Value, faction : PlanetSideEmpire.Value) : ExoSuitDefinition = {
    suit match {
      case ExoSuitType.Infiltration => GlobalDefinitions.Infiltration.Use
      case ExoSuitType.Agile => GlobalDefinitions.Agile.Use
      case ExoSuitType.Reinforced => GlobalDefinitions.Reinforced.Use
      case ExoSuitType.MAX => faction match {
        case PlanetSideEmpire.TR => GlobalDefinitions.TRMAX.Use
        case PlanetSideEmpire.NC => GlobalDefinitions.NCMAX.Use
        case PlanetSideEmpire.VS => GlobalDefinitions.VSMAX.Use
      }
      case _ => GlobalDefinitions.Standard.Use
    }
  }
}
