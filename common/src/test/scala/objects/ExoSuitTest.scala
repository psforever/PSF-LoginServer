// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.definition.{ExoSuitDefinition, SpecialExoSuitDefinition}
import net.psforever.objects.equipment._
import net.psforever.objects.inventory.InventoryTile
import net.psforever.types.ExoSuitType
import org.specs2.mutable._

class ExoSuitTest extends Specification {
  "ExoSuitDefinition" should {
    "construct" in {
      val obj = ExoSuitDefinition(ExoSuitType.Standard)
      obj.MaxArmor mustEqual 0
      obj.InventoryScale mustEqual InventoryTile.Tile11
      obj.InventoryOffset mustEqual 0
      obj.SuitType mustEqual ExoSuitType.Standard
      obj.Holsters.length mustEqual 5
      obj.Holsters.foreach(slot => { if(slot != EquipmentSize.Blocked) { ko } })
      ok
    }

    "produce the type of exo-suit that was provided as a clarified type" in {
      ExoSuitDefinition(ExoSuitType.Standard).SuitType mustEqual ExoSuitType.Standard
      ExoSuitDefinition(ExoSuitType.Agile).SuitType mustEqual ExoSuitType.Agile
    }

    "change the maximum armor value" in {
      val obj = ExoSuitDefinition(ExoSuitType.Standard)
      obj.MaxArmor mustEqual 0
      obj.MaxArmor = 1
      obj.MaxArmor mustEqual 1
    }

    "not change the maximum armor to an invalid value" in {
      val obj = ExoSuitDefinition(ExoSuitType.Standard)
      obj.MaxArmor mustEqual 0
      obj.MaxArmor = -1
      obj.MaxArmor mustEqual 0
      obj.MaxArmor = 65536
      obj.MaxArmor mustEqual 65535
    }

    "change the size of the inventory" in {
      val obj = ExoSuitDefinition(ExoSuitType.Standard)
      obj.InventoryScale mustEqual InventoryTile.Tile11
      obj.InventoryScale = InventoryTile.Tile42
      obj.InventoryScale mustEqual InventoryTile.Tile42
    }

    "change the start index of the inventory" in {
      val obj = ExoSuitDefinition(ExoSuitType.Standard)
      obj.InventoryOffset mustEqual 0
      obj.InventoryOffset = 1
      obj.InventoryOffset mustEqual 1
    }

    "not change the start index of the inventory to an invalid value" in {
      val obj = ExoSuitDefinition(ExoSuitType.Standard)
      obj.InventoryOffset mustEqual 0
      obj.InventoryOffset = -1
      obj.InventoryOffset mustEqual 0
      obj.InventoryOffset = 65536
      obj.InventoryOffset mustEqual 65535
    }

    "change specific holsters to specific values" in {
      val obj = ExoSuitDefinition(ExoSuitType.Standard)
      obj.Holster(0) mustEqual EquipmentSize.Blocked
      obj.Holster(0, EquipmentSize.Pistol)
      obj.Holster(0) mustEqual EquipmentSize.Pistol
      obj.Holster(4) mustEqual EquipmentSize.Blocked
      obj.Holster(4, EquipmentSize.Rifle)
      obj.Holster(4) mustEqual EquipmentSize.Rifle
      (0 to 4).foreach {
        case 0 => obj.Holsters(0) mustEqual EquipmentSize.Pistol
        case 4 => obj.Holsters(4) mustEqual EquipmentSize.Rifle
        case x => obj.Holsters(x) mustEqual EquipmentSize.Blocked
      }
      ok
    }

    "can not change any slot that does not exist" in {
      val obj = ExoSuitDefinition(ExoSuitType.Standard)
      obj.Holster(9) mustEqual EquipmentSize.Blocked
      obj.Holster(9, EquipmentSize.Pistol)
      obj.Holster(9) mustEqual EquipmentSize.Blocked
    }

    "produce a copy of the definition" in {
      val obj = ExoSuitDefinition(ExoSuitType.Standard)
      val obj2 = obj.Use
      obj eq obj2
    }
  }

  "SpecialExoSuitDefinition" should {
    "construct" in {
      val obj = SpecialExoSuitDefinition(ExoSuitType.Standard)
      obj.MaxArmor mustEqual 0
      obj.InventoryScale mustEqual InventoryTile.Tile11
      obj.InventoryOffset mustEqual 0
      obj.SuitType mustEqual ExoSuitType.Standard
      obj.Holsters.length mustEqual 5
      obj.Holsters.foreach(slot => { if(slot != EquipmentSize.Blocked) { ko } })
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
    }

    "configure UsingSpecial to various values" in {
      val obj = SpecialExoSuitDefinition(ExoSuitType.Standard)
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Anchored
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Anchored
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Overdrive
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Overdrive
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Shielded
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Shielded
      obj.UsingSpecial = SpecialExoSuitDefinition.Mode.Normal
      obj.UsingSpecial mustEqual SpecialExoSuitDefinition.Mode.Normal
    }

    "produce a separate copy of the definition" in {
      val obj = SpecialExoSuitDefinition(ExoSuitType.Standard)
      val obj2 = obj.Use
      obj ne obj2
    }
  }

  "ExoSuitDefinition.Select" should {
    "produce common, shared instances of exo suits" in {
      ExoSuitDefinition.Select(ExoSuitType.Standard) eq ExoSuitDefinition.Select(ExoSuitType.Standard)
      ExoSuitDefinition.Select(ExoSuitType.Agile) eq ExoSuitDefinition.Select(ExoSuitType.Agile)
      ExoSuitDefinition.Select(ExoSuitType.Reinforced) eq ExoSuitDefinition.Select(ExoSuitType.Reinforced)
      ExoSuitDefinition.Select(ExoSuitType.Infiltration) eq ExoSuitDefinition.Select(ExoSuitType.Infiltration)
    }

    "produces unique instances of the mechanized assault exo suit" in {
      val obj = ExoSuitDefinition.Select(ExoSuitType.MAX)
      obj ne ExoSuitDefinition.Select(ExoSuitType.MAX)
      obj.isInstanceOf[SpecialExoSuitDefinition] mustEqual true
    }
  }
}
