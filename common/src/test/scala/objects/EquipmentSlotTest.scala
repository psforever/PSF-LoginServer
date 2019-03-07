// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.{OffhandEquipmentSlot, Tool}
import net.psforever.objects.equipment.{EquipmentSize, EquipmentSlot}
import net.psforever.objects.GlobalDefinitions.{beamer, repeater, suppressor}
import org.specs2.mutable._

class EquipmentSlotTest extends Specification {
  "EquipmentSlot" should {
    "construct" in {
      val obj = new EquipmentSlot()
      obj.Size mustEqual EquipmentSize.Blocked
      obj.Equipment mustEqual None
    }

    "construct with a default size" in {
      val obj = EquipmentSlot(EquipmentSize.Pistol)
      obj.Size mustEqual EquipmentSize.Pistol
    }

    "change size" in {
      val obj = new EquipmentSlot()
      obj.Size mustEqual EquipmentSize.Blocked
      obj.Size = EquipmentSize.Pistol
      obj.Size mustEqual EquipmentSize.Pistol
    }

    "hold equipment" in {
      val obj = new EquipmentSlot()
      val equipment = Tool(beamer)
      obj.Equipment = None

      beamer.Size mustEqual EquipmentSize.Pistol
      obj.Size = EquipmentSize.Pistol
      obj.Equipment = equipment
      obj.Equipment match {
        case Some(item) =>
          item.Definition mustEqual beamer
        case None =>
          ko
      }
    }

    "put down previously held equipment" in {
      val obj = EquipmentSlot(EquipmentSize.Pistol)
      obj.Equipment = Tool(beamer)

      obj.Equipment match {
        case Some(item) =>
          item.Definition mustEqual beamer
        case None =>
          ko
      }
      obj.Equipment = None
      obj.Equipment match {
        case Some(_) =>
          ko
        case None =>
          ok
      }
    }

    "not change size when holding equipment" in {
      val obj = new EquipmentSlot()
      obj.Size mustEqual EquipmentSize.Blocked
      obj.Size = EquipmentSize.Pistol
      obj.Equipment = Tool(beamer)
      obj.Equipment match {
        case Some(_) => ;
        case None => ko
      }

      obj.Size mustEqual EquipmentSize.Pistol
      obj.Size = EquipmentSize.Rifle
      obj.Size mustEqual EquipmentSize.Pistol
    }

    "not hold wrong-sized equipment" in {
      val obj = new EquipmentSlot()
      val equipment = Tool(suppressor)
      obj.Equipment = None

      beamer.Size mustEqual EquipmentSize.Pistol
      obj.Size = EquipmentSize.Pistol
      obj.Equipment = equipment
      obj.Equipment mustEqual None
    }

    "not switch to holding a second item in place of a first one" in {
      val obj = EquipmentSlot(EquipmentSize.Pistol)
      obj.Equipment = Tool(beamer)

      obj.Equipment match {
        case Some(item) =>
          item.Definition mustEqual beamer
        case None =>
          ko
      }
      repeater.Size mustEqual EquipmentSize.Pistol
      obj.Equipment = Tool(repeater) //also a pistol
      obj.Equipment match {
        case Some(item) =>
          item.Definition mustEqual beamer
        case None =>
          ko
      }
    }
  }

  "OffhandEquipmentSLot" should {
    "construct" in {
      val obj = new OffhandEquipmentSlot(EquipmentSize.Pistol)
      obj.Size mustEqual EquipmentSize.Pistol
      obj.Equipment mustEqual None
    }

    "hold equipment" in {
      val obj = new OffhandEquipmentSlot(EquipmentSize.Pistol)
      val equipment = Tool(beamer)
      obj.Equipment = None

      beamer.Size mustEqual EquipmentSize.Pistol
      obj.Equipment = equipment
      obj.Equipment match {
        case Some(item) =>
          item.Definition mustEqual beamer
        case None =>
          ko
      }
    }

    "not change size after being constructed" in {
      //see above test "EquipmentSlot should/not change size when holding equipment"
      val obj = new OffhandEquipmentSlot(EquipmentSize.Pistol)
      obj.Equipment mustEqual None

      obj.Size mustEqual EquipmentSize.Pistol
      obj.Size = EquipmentSize.Rifle
      obj.Size mustEqual EquipmentSize.Pistol
    }

    "special Blocked size default" in {
      OffhandEquipmentSlot.BlockedSlot.Size mustEqual EquipmentSize.Blocked
      OffhandEquipmentSlot.BlockedSlot.Equipment mustEqual None
    }
  }
}
