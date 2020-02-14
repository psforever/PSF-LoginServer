// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.{AmmoBox, SimpleItem, Tool}
import net.psforever.objects.definition.SimpleItemDefinition
import net.psforever.objects.inventory.{GridInventory, InventoryDisarrayException, InventoryItem, InventoryTile}
import net.psforever.objects.GlobalDefinitions.{bullet_9mm, suppressor}
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable._

import scala.collection.mutable.ListBuffer
import scala.util.Success

class InventoryTest extends Specification {
  val
  bullet9mmBox1 = AmmoBox(bullet_9mm)
  bullet9mmBox1.GUID = PlanetSideGUID(1)
  val
  bullet9mmBox2 = AmmoBox(bullet_9mm)
  bullet9mmBox2.GUID = PlanetSideGUID(2)

  "InventoryDisarrayException" should {
    "construct" in {
      InventoryDisarrayException("slot out of bounds")
      ok
    }

    "construct (with Throwable)" in {
      InventoryDisarrayException("slot out of bounds", new Throwable())
      ok
    }
  }

  "GridInventory" should {
    "construct" in {
      val obj : GridInventory = GridInventory()
      obj.TotalCapacity mustEqual 1
      obj.Capacity mustEqual 1
    }

    "resize" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj.TotalCapacity mustEqual 54
      obj.Capacity mustEqual 54
      obj.Size mustEqual 0
    }

    "check for collision with inventory border" in {
      val obj : GridInventory = GridInventory(3, 3)
      //safe
      obj.CheckCollisionsAsList(0, 3, 3) mustEqual Success(Nil)
      //right
      obj.CheckCollisionsAsList(-1, 3, 3) match {
        case scala.util.Failure(fail) =>
          fail.isInstanceOf[IndexOutOfBoundsException] mustEqual true
        case _ => ko
      }
      //left
      obj.CheckCollisionsAsList(1, 3, 3) match {
        case scala.util.Failure(fail) =>
          fail.isInstanceOf[IndexOutOfBoundsException] mustEqual true
        case _ => ko
      }
      //bottom
      obj.CheckCollisionsAsList(3, 3, 3) match {
        case scala.util.Failure(fail) =>
          fail.isInstanceOf[IndexOutOfBoundsException] mustEqual true
        case _ => ko
      }
    }

    "check for item collision (right insert)" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj += 0 -> bullet9mmBox1
      obj.Capacity mustEqual 45
      val w = bullet9mmBox2.Tile.Width
      val h = bullet9mmBox2.Tile.Height
      val list0 = obj.CheckCollisionsAsList(0, w, h)
      obj.CheckCollisionsAsList(0, w, h) match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list1 = obj.CheckCollisionsAsList(1, w, h)
      list1 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list2 = obj.CheckCollisionsAsList(2, w, h)
      list2 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list3 = obj.CheckCollisionsAsList(3, w, h)
      list3 match {
        case scala.util.Success(list) => list.isEmpty mustEqual true
        case scala.util.Failure(_) => ko
      }
      obj.CheckCollisionsAsGrid(0, w, h) mustEqual list0
      obj.CheckCollisionsAsGrid(1, w, h) mustEqual list1
      obj.CheckCollisionsAsGrid(2, w, h) mustEqual list2
      obj.CheckCollisionsAsGrid(3, w, h) mustEqual list3
      obj.Clear()
      ok
    }

    "check for item collision (left insert)" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj += 3 -> bullet9mmBox1
      obj.Capacity mustEqual 45
      val w = bullet9mmBox2.Tile.Width
      val h = bullet9mmBox2.Tile.Height
      val list0 = obj.CheckCollisionsAsList(3, w, h)
      list0 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list1 = obj.CheckCollisionsAsList(2, w, h)
      list1 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list2 = obj.CheckCollisionsAsList(1, w, h)
      list2 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list3 = obj.CheckCollisionsAsList(0, w, h)
      list3 match {
        case scala.util.Success(list) => list.isEmpty mustEqual true
        case scala.util.Failure(_) => ko
      }
      obj.CheckCollisionsAsGrid(3, w, h) mustEqual list0
      obj.CheckCollisionsAsGrid(2, w, h) mustEqual list1
      obj.CheckCollisionsAsGrid(1, w, h) mustEqual list2
      obj.CheckCollisionsAsGrid(0, w, h) mustEqual list3
      obj.Clear()
      ok
    }

    "check for item collision (below insert)" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj += 0 -> bullet9mmBox1
      obj.Capacity mustEqual 45
      val w = bullet9mmBox2.Tile.Width
      val h = bullet9mmBox2.Tile.Height
      val list0 = obj.CheckCollisionsAsList(0, w, h)
      list0 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list1 = obj.CheckCollisionsAsList(9, w, h)
      list1 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list2 = obj.CheckCollisionsAsList(18, w, h)
      list2 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list3 = obj.CheckCollisionsAsList(27, w, h)
      list3 match {
        case scala.util.Success(list) => list.isEmpty mustEqual true
        case scala.util.Failure(_) => ko
      }
      obj.CheckCollisionsAsGrid(0, w, h) mustEqual list0
      obj.CheckCollisionsAsGrid(9, w, h) mustEqual list1
      obj.CheckCollisionsAsGrid(18, w, h) mustEqual list2
      obj.CheckCollisionsAsGrid(27, w, h) mustEqual list3
      obj.Clear()
      ok
    }

    "check for item collision (above insert)" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj += 27 -> bullet9mmBox1
      obj.Capacity mustEqual 45
      val w = bullet9mmBox2.Tile.Width
      val h = bullet9mmBox2.Tile.Height
      val list0 = obj.CheckCollisionsAsList(27, w, h)
      list0 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list1 = obj.CheckCollisionsAsList(18, w, h)
      list1 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list2 = obj.CheckCollisionsAsList(9, w, h)
      list2 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list3 = obj.CheckCollisionsAsList(0, w, h)
      list3 match {
        case scala.util.Success(list) => list.isEmpty mustEqual true
        case scala.util.Failure(_) => ko
      }
      obj.CheckCollisionsAsGrid(27, w, h) mustEqual list0
      obj.CheckCollisionsAsGrid(18, w, h) mustEqual list1
      obj.CheckCollisionsAsGrid(9, w, h) mustEqual list2
      obj.CheckCollisionsAsGrid(0, w, h) mustEqual list3
      obj.Clear()
      ok
    }

    "check for item collision (diagonal insert)" in {
      /*
      Number indicates upper-left corner of attempted 3x3 insertion by list#
      0 - - - - - 2 - - - - -
      - 1 - - - 3 - - - - - -
      - - - - - - - - - - - -
      - - - X X X - - - - - -
      - - - X X X - - - - - -
      - 5 - X X 7 - - - - - -
      4 - - - - - 6 - - - - -
      - - - - - - - - - - - -
      - - - - - - - - - - - -
       */
      val obj : GridInventory = GridInventory(12, 9)
      obj += 39 -> bullet9mmBox1
      obj.Capacity mustEqual 99 //108 - 9
      val w = bullet9mmBox2.Tile.Width
      val h = bullet9mmBox2.Tile.Height
      val list0 = obj.CheckCollisionsAsList(0, w, h)
      list0 match {
        case scala.util.Success(list) => list.isEmpty mustEqual true
        case scala.util.Failure(_) => ko
      }
      val list1 = obj.CheckCollisionsAsList(13, w, h)
      list1 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list2 = obj.CheckCollisionsAsList(6, w, h)
      list2 match {
        case scala.util.Success(list) =>list.isEmpty mustEqual true
        case scala.util.Failure(_) => ko
      }
      val list3 = obj.CheckCollisionsAsList(17, w, h)
      list3 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list4 = obj.CheckCollisionsAsList(72, w, h)
      list4 match {
        case scala.util.Success(list) => list.isEmpty mustEqual true
        case scala.util.Failure(_) => ko
      }
      val list5 = obj.CheckCollisionsAsList(61, w, h)
      list5 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      val list6 = obj.CheckCollisionsAsList(78, w, h)
      list6 match {
        case scala.util.Success(list) => list.isEmpty mustEqual true
        case scala.util.Failure(_) => ko
      }
      val list7 = obj.CheckCollisionsAsList(65, w, h)
      list7 match {
        case scala.util.Success(list) => list.length mustEqual 1
        case scala.util.Failure(_) => ko
      }
      obj.CheckCollisionsAsGrid(0, w, h) mustEqual list0
      obj.CheckCollisionsAsGrid(13, w, h) mustEqual list1
      obj.CheckCollisionsAsGrid(6, w, h) mustEqual list2
      obj.CheckCollisionsAsGrid(17, w, h) mustEqual list3
      obj.CheckCollisionsAsGrid(72, w, h) mustEqual list4
      obj.CheckCollisionsAsGrid(61, w, h) mustEqual list5
      obj.CheckCollisionsAsGrid(78, w, h) mustEqual list6
      obj.CheckCollisionsAsGrid(65, w, h) mustEqual list7
      obj.Clear()
      ok
    }

    "insert item" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj.CheckCollisions(23, bullet9mmBox1) mustEqual Success(Nil)
      obj += 2 -> bullet9mmBox1
      obj.TotalCapacity mustEqual 54
      obj.Capacity mustEqual 45
      obj.Size mustEqual 1
      obj.hasItem(PlanetSideGUID(1)).contains(bullet9mmBox1) mustEqual true
      obj.Clear()
      obj.Size mustEqual 0
    }

    "not insert into an invalid slot (n < 0)" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj.Capacity mustEqual 54
      obj.Size mustEqual 0
      obj.Insert(-1, bullet9mmBox1) must throwA[IndexOutOfBoundsException]
      obj.Capacity mustEqual 54
      obj.Size mustEqual 0
    }

    "not insert into an invalid slot (n > capacity)" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj.Capacity mustEqual 54
      obj.Size mustEqual 0
      obj.Insert(55, bullet9mmBox1) must throwA[IndexOutOfBoundsException]
      obj.Capacity mustEqual 54
      obj.Size mustEqual 0
    }

    "block insertion if item collision" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj += 0 -> bullet9mmBox1
      obj.Capacity mustEqual 45
      obj.hasItem(PlanetSideGUID(1)).contains(bullet9mmBox1) mustEqual true
      obj += 2 -> bullet9mmBox2
      obj.hasItem(PlanetSideGUID(2)).isEmpty mustEqual true
    }

    "insert items quickly (risk overwriting entries)" in {
      val obj : GridInventory = GridInventory(6, 6)
      (obj += 0 -> bullet9mmBox1) mustEqual true
      val collision1 = obj.CheckCollisions(0,1,1)
      obj.CheckCollisions(1,1,1) mustEqual collision1
      obj.CheckCollisions(2,1,1) mustEqual collision1
      obj.CheckCollisions(6,1,1) mustEqual collision1
      obj.CheckCollisions(7,1,1) mustEqual collision1
      obj.CheckCollisions(8,1,1) mustEqual collision1
      obj.CheckCollisions(12,1,1) mustEqual collision1
      obj.CheckCollisions(13,1,1) mustEqual collision1
      obj.CheckCollisions(14,1,1) mustEqual collision1

      (obj += 7 -> bullet9mmBox2) mustEqual false //can not insert overlapping object
      obj.CheckCollisions(0,1,1) mustEqual collision1
      obj.CheckCollisions(1,1,1) mustEqual collision1
      obj.CheckCollisions(2,1,1) mustEqual collision1
      obj.CheckCollisions(6,1,1) mustEqual collision1
      obj.CheckCollisions(7,1,1) mustEqual collision1
      obj.CheckCollisions(8,1,1) mustEqual collision1
      obj.CheckCollisions(12,1,1) mustEqual collision1
      obj.CheckCollisions(13,1,1) mustEqual collision1
      obj.CheckCollisions(14,1,1) mustEqual collision1

      obj.InsertQuickly(7, bullet9mmBox2) mustEqual true //overwrite
      val collision2 = obj.CheckCollisions(7,1,1)
      obj.CheckCollisions(0,1,1) mustEqual collision1
      obj.CheckCollisions(1,1,1) mustEqual collision1
      obj.CheckCollisions(2,1,1) mustEqual collision1
      obj.CheckCollisions(6,1,1) mustEqual collision1
      obj.CheckCollisions(7,1,1) mustEqual collision2
      obj.CheckCollisions(8,1,1) mustEqual collision2
      obj.CheckCollisions(12,1,1) mustEqual collision1
      obj.CheckCollisions(13,1,1) mustEqual collision2
      obj.CheckCollisions(14,1,1) mustEqual collision2
    }

    "clear all items" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj += 2 -> bullet9mmBox1
      obj.Size mustEqual 1
      obj.hasItem(PlanetSideGUID(1)).contains(bullet9mmBox1) mustEqual true
      obj.Clear()
      obj.Size mustEqual 0
      obj.hasItem(PlanetSideGUID(1)).isEmpty mustEqual true
    }

    "remove item" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj += 0 -> bullet9mmBox1
      obj.hasItem(PlanetSideGUID(1)).contains(bullet9mmBox1) mustEqual true
      obj -= PlanetSideGUID(1)
      obj.hasItem(PlanetSideGUID(1)).isEmpty mustEqual true
      obj.Clear()
      ok
    }

    "fail to remove from an invalid slot (n < 0)" in {
      val obj : GridInventory = GridInventory(9, 6)
      (obj -= -1) mustEqual false
    }

    "fail to remove from an invalid slot (n > capacity)" in {
      val obj : GridInventory = GridInventory(9, 6)
      (obj -= 55) mustEqual false
    }

    "unblock insertion on item removal" in {
      val obj : GridInventory = GridInventory(9, 6)
      obj.CheckCollisions(23, bullet9mmBox1) mustEqual Success(Nil)
      obj += 23 -> bullet9mmBox1
      obj.hasItem(PlanetSideGUID(1)).contains(bullet9mmBox1) mustEqual true
      obj.CheckCollisions(23, bullet9mmBox1) mustEqual Success(1 :: Nil)
      obj -= PlanetSideGUID(1)
      obj.hasItem(PlanetSideGUID(1)).isEmpty mustEqual true
      obj.CheckCollisions(23, bullet9mmBox1) mustEqual Success(Nil)
      obj.Clear()
      ok
    }

    "attempt to fit an item" in {
      val sampleDef22 = new SimpleItemDefinition(149)
      sampleDef22.Tile = InventoryTile.Tile22
      val sampleDef33 = new SimpleItemDefinition(149)
      sampleDef33.Tile = InventoryTile.Tile33
      val sampleDef63 = new SimpleItemDefinition(149)
      sampleDef63.Tile = InventoryTile.Tile63

      val obj : GridInventory = GridInventory(9, 9)
      obj += 0 -> SimpleItem(sampleDef22)
      obj += 20 -> SimpleItem(sampleDef63)
      obj += 56 -> SimpleItem(sampleDef33)
      obj.Fit(InventoryTile.Tile33) match {
        case Some(x) =>
          x mustEqual 50
        case None =>
          ko
      }
      ok
    }

    "attempt to fit all the items" in {
      val sampleDef1 = new SimpleItemDefinition(149)
      sampleDef1.Tile = InventoryTile.Tile22
      val sampleDef2 = new SimpleItemDefinition(149)
      sampleDef2.Tile = InventoryTile.Tile33
      val sampleDef3 = new SimpleItemDefinition(149)
      sampleDef3.Tile = InventoryTile.Tile42
      val sampleDef4 = new SimpleItemDefinition(149)
      sampleDef4.Tile = InventoryTile.Tile63

      val list : ListBuffer[InventoryItem] = ListBuffer()
      list += new InventoryItem(SimpleItem(sampleDef2), -1)
      list += new InventoryItem(SimpleItem(sampleDef3), -1)
      list += new InventoryItem(SimpleItem(sampleDef1), -1)
      list += new InventoryItem(SimpleItem(sampleDef4), -1)
      list += new InventoryItem(SimpleItem(sampleDef1), -1)
      list += new InventoryItem(SimpleItem(sampleDef4), -1)
      list += new InventoryItem(SimpleItem(sampleDef2), -1)
      list += new InventoryItem(SimpleItem(sampleDef3), -1)
      val obj : GridInventory = GridInventory(9, 9)

      val (elements, out) = GridInventory.recoverInventory(list.toList, obj)
      elements.length mustEqual 6
      out.length mustEqual 2
      elements.foreach(item => {
        obj.Insert(item.start, item.obj) mustEqual true
      })
      out.head.Definition.Tile mustEqual InventoryTile.Tile22 //did not fit
      out(1).Definition.Tile mustEqual InventoryTile.Tile22 //did not fit
      ok
    }

    "confirm integrity of inventory as a grid" in {
      val obj : GridInventory = GridInventory(6, 6)
      (obj += 0 -> bullet9mmBox1) mustEqual true
      (obj += 21 -> bullet9mmBox2) mustEqual true
      //artificially pollute the inventory grid-space
      obj.SetCells(10, 1, 1, 3)
      obj.SetCells(19, 2, 2, 4)
      obj.ElementsOnGridMatchList() mustEqual 5 //number of misses repaired
    }

    "confirm integrity of inventory as a list (no overlap)" in {
      val obj : GridInventory = GridInventory(9, 9)
      val gun = Tool(suppressor)
      obj.InsertQuickly(0, gun)
      obj.InsertQuickly(33, bullet9mmBox1)
      //nothing should overlap
      val lists = obj.ElementsInListCollideInGrid()
      lists.size mustEqual 0
    }

    "confirm integrity of inventory as a list (normal overlap)" in {
      val obj : GridInventory = GridInventory(9, 9)
      val gun = Tool(suppressor)
      val bullet9mmBox3 = AmmoBox(bullet_9mm)
      obj.InsertQuickly(0, gun)
      obj.InsertQuickly(18, bullet9mmBox1)
      obj.InsertQuickly(38, bullet9mmBox2)
      obj.InsertQuickly(33, bullet9mmBox3)
      //gun and box1 should overlap
      //box1 and box2 should overlap
      //box3 should not overlap with anything
      val lists = obj.ElementsInListCollideInGrid()
      lists.size mustEqual 2
      lists.foreach { list =>
        val out = list.map { _.obj }
        if(out.size == 2 && out.contains(gun) && out.contains(bullet9mmBox1)) {
          ok
        }
        else if(out.size == 2 && out.contains(bullet9mmBox1) && out.contains(bullet9mmBox2)) {
          ok
        }
        else {
          ko
        }
      }
      ok
    }

    "confirm integrity of inventory as a list (triple overlap)" in {
      val obj : GridInventory = GridInventory(9, 9)
      val gun = Tool(suppressor)
      val bullet9mmBox3 = AmmoBox(bullet_9mm)
      val bullet9mmBox4 = AmmoBox(bullet_9mm)
      obj.InsertQuickly(0, gun)
      obj.InsertQuickly(18, bullet9mmBox1)
      obj.InsertQuickly(36, bullet9mmBox2)
      obj.InsertQuickly(38, bullet9mmBox3)
      obj.InsertQuickly(33, bullet9mmBox4)
      //gun and box1 should overlap
      //box1, box2, and box3 should overlap
      //box4 should not overlap with anything
      val lists = obj.ElementsInListCollideInGrid()
      lists.size mustEqual 2
      lists.foreach { list =>
        val out = list.map { _.obj }
        if(out.size == 2 && out.contains(gun) && out.contains(bullet9mmBox1)) {
          ok
        }
        else if(out.size == 3 && out.contains(bullet9mmBox1) && out.contains(bullet9mmBox2) && out.contains(bullet9mmBox3)) {
          ok
        }
        else {
          ko
        }
      }
      ok
    }
  }
}
