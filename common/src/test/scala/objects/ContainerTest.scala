// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem}
import net.psforever.objects.{GlobalDefinitions, OffhandEquipmentSlot, Tool}
import net.psforever.packet.game.PlanetSideGUID
import org.specs2.mutable._

import scala.util.Success

class ContainerTest extends Specification {
  "Container" should {
    "construct" in {
      val obj = new ContainerTest.CObject
      obj.VisibleSlots mustEqual (0 until 9).toSet
      obj.Inventory.Size mustEqual 0
      obj.Inventory.Capacity mustEqual 9
      obj.Find(PlanetSideGUID(0)) mustEqual None
      obj.Slot(0) mustEqual OffhandEquipmentSlot.BlockedSlot
      obj.Collisions(0, 2, 2) mustEqual Success(List())
    }

    "Collisions can Find items in Inventory (default behavior)" in {
      val obj = new ContainerTest.CObject
      val weapon = Tool(GlobalDefinitions.beamer)
      weapon.GUID = PlanetSideGUID(1)

      obj.Inventory += 0 -> weapon
      obj.Find(PlanetSideGUID(1)) match {
        case Some(index) =>
          obj.Inventory.Items(index).obj mustEqual weapon
        case None =>
          ko
      }
      obj.Collisions(1,1,1) match {
        case Success(items) =>
          items.length mustEqual 1
          items.head.obj mustEqual weapon
        case _ =>;
          ko
      }
    }
  }
}

object ContainerTest {
  class CObject extends Container {
    private val inv = GridInventory(3, 3)

    def Inventory : GridInventory = inv

    def Find(guid : PlanetSideGUID) : Option[Int] =  {
      Inventory.Items.find({
        case((_, item)) =>
          if(item.obj.HasGUID) {
            item.obj.GUID == guid
          }
          else {
            false
          }
      }) match {
        case Some((index, _)) =>
          Some(index)
        case None =>
          None
      }
    }

    def VisibleSlots :Set[Int] = Set[Int](0,1,2, 3,4,5, 6,7,8)
  }
}
