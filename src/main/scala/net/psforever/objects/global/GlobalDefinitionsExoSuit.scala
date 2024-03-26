// Copyright (c) 2024 PSForever
package net.psforever.objects.global

import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.avatar.Certification
import net.psforever.objects.definition.SpecialExoSuitDefinition
import net.psforever.objects.equipment.EquipmentSize
import net.psforever.objects.inventory.InventoryTile
import net.psforever.objects.vital.MaxResolutions
import net.psforever.objects.vital.damage.DamageCalculations

object GlobalDefinitionsExoSuit {
  import GlobalDefinitions._

  /**
   * Initialize `ExoSuitType` globals.
   */
  def init(): Unit = {
    Standard.Name = "standard"
    Standard.MaxArmor = 50
    Standard.InventoryScale = InventoryTile.Tile96
    Standard.InventoryOffset = 6
    Standard.Holster(0, EquipmentSize.Pistol)
    Standard.Holster(2, EquipmentSize.Rifle)
    Standard.Holster(4, EquipmentSize.Melee)
    Standard.ResistanceDirectHit = 4
    Standard.ResistanceSplash = 15
    Standard.ResistanceAggravated = 8
    Standard.collision.forceFactor = 1.5f
    Standard.collision.massFactor = 2f

    Agile.Name = "lite_armor"
    Agile.Descriptor = "agile"
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
    Agile.collision.forceFactor = 1.5f
    Agile.collision.massFactor = 2f

    Reinforced.Name = "med_armor"
    Reinforced.Descriptor = "reinforced"
    Reinforced.Permissions = List(Certification.ReinforcedExoSuit)
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
    Reinforced.collision.forceFactor = 2f
    Reinforced.collision.massFactor = 3f

    Infiltration.Name = "infiltration_suit"
    Infiltration.Permissions = List(Certification.InfiltrationSuit)
    Infiltration.MaxArmor = 0
    Infiltration.InventoryScale = InventoryTile.Tile66
    Infiltration.InventoryOffset = 6
    Infiltration.Holster(0, EquipmentSize.Pistol)
    Infiltration.Holster(4, EquipmentSize.Melee)

    def CommonMaxConfig(max: SpecialExoSuitDefinition): Unit = {
      max.Permissions = List(Certification.AIMAX, Certification.AVMAX, Certification.AAMAX, Certification.UniMAX)
      max.MaxArmor = 650
      max.InventoryScale = InventoryTile.Tile1612
      max.InventoryOffset = 6
      max.Holster(0, EquipmentSize.Max)
      max.Holster(4, EquipmentSize.Melee)
      max.Subtract.Damage1 = 2
      max.ResistanceDirectHit = 6
      max.ResistanceSplash = 35
      max.ResistanceAggravated = 10
      max.RadiationShielding = 0.5f
      max.collision.forceFactor = 4f
      max.collision.massFactor = 10f
      max.DamageUsing = DamageCalculations.AgainstMaxSuit
      max.Model = MaxResolutions.calculate
    }

    CommonMaxConfig(VSMAX)
    VSMAX.Name = "vshev"
    VSMAX.MaxCapacitor = 50
    VSMAX.CapacitorRechargeDelayMillis = 5000
    VSMAX.CapacitorRechargePerSecond = 3
    VSMAX.CapacitorDrainPerSecond = 20

    CommonMaxConfig(TRMAX)
    TRMAX.Name = "trhev"
    TRMAX.MaxCapacitor = 300
    TRMAX.CapacitorRechargeDelayMillis = 10000
    TRMAX.CapacitorRechargePerSecond = 10
    TRMAX.CapacitorDrainPerSecond = 30

    CommonMaxConfig(NCMAX)
    NCMAX.Name = "nchev"
    NCMAX.MaxCapacitor = 400
    NCMAX.CapacitorRechargeDelayMillis = 10000
    NCMAX.CapacitorRechargePerSecond = 4
    NCMAX.CapacitorDrainPerSecond = 4
  }
}
