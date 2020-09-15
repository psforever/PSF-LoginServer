// Copyright (c) 2020 PSForever
package net.psforever.objects.inventory

import net.psforever.objects.Tool
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.SpecificNumberSource
import net.psforever.types.PlanetSideGUID

import scala.util.{Failure, Success}

/**
  * An inventory that contains its own internal unique number system bound by a domain of numbers.
  * When equipment is inserted into this inventory,
  * the equipment is registered to it, assigned one of its internal unique numbers.
  * The equipment must not already be registered to another unique number system for that reason.
  * Upon being removed, the removed equipment is unregistered.
  * The registration system adds another unspoken layer to `Capacity`
  * as it imposes a total object count to the inventory.
  * @see `NumberSourceHub`
  * @see `RandomSelector`
  * @see `SpecificNumberSource`
  * @param numbers the numbers used as unique identifiers
  */
class LocallyRegisteredInventory(numbers: Iterable[Int])
  extends GridInventory {
  private val hub: NumberPoolHub = {
    val numHub = new NumberPoolHub(SpecificNumberSource(numbers))
    //only one pool composed of all of the numbers; randomized selection
    numHub.AddPool("internal", numbers.toList).Selector = new RandomSelector
    numHub
  }

  override def Insert(start : Int, obj : Equipment) : Boolean = {
    if(!obj.HasGUID) {
      registerEquipment(obj) match {
        case true if super.Insert(start, obj) =>
          true
        case true =>
          unregisterEquipment(obj) //the item failed to be inserted; undo the previous registration
          false
        case _ =>
          false
      }
    }
    else {
      false
    }
  }

  override def InsertQuickly(start : Int, obj : Equipment) : Boolean = {
    if(!obj.HasGUID) {
      registerEquipment(obj) match {
        case true if super.InsertQuickly(start, obj) =>
          true
        case true =>
          unregisterEquipment(obj) //the item failed to be inserted; undo the previous registration
          false
        case _ =>
          false
      }
    }
    else {
      false
    }
  }

  override def Remove(guid : PlanetSideGUID) : Boolean = {
    hub(guid) match {
      case Some(obj: Equipment) if super.Remove(guid) =>
        unregisterEquipment(obj)
      case _ =>
        false
    }
  }

  override def Remove(index : Int) : Boolean = {
    Slot(index).Equipment match {
      case Some(obj: Equipment) if super.Remove(obj.GUID) =>
        unregisterEquipment(obj)
      case _ =>
        false
    }
  }

  override def Clear() : List[InventoryItem] = {
    val items = super.Clear()
    items.foreach { item => unregisterEquipment(item.obj) }
    items
  }

  private def registerEquipment(obj: Equipment): Boolean = {
    obj match {
      case tool: Tool => registerTool(tool)
      case _ => registerObject(obj)
    }
  }

  private def registerTool(obj: Tool): Boolean = {
    val parts = obj +: obj.AmmoSlots.map { _.Box }
    val tasks = parts.map { part => hub.register(part, "internal") }
    if(tasks.exists(o => o.isInstanceOf[Failure[Int]])) {
      tasks.zipWithIndex.collect { case (Success(_), index) =>
        unregisterEquipment(parts(index))
      }
      false
    } else {
      true
    }
  }

  private def registerObject(obj: Equipment): Boolean = {
    hub.register(obj, "internal").isSuccess
  }

  private def unregisterEquipment(obj: Equipment): Boolean = {
    obj match {
      case tool: Tool => unregisterTool(tool)
      case _ => unregisterObject(obj)
    }
  }

  private def unregisterTool(obj: Tool): Boolean = {
    val parts = obj +: obj.AmmoSlots.map { _.Box }
    parts.map { part => hub.unregister(part) }.forall(o => o.isInstanceOf[Success[Int]])
  }

  private def unregisterObject(obj: Equipment): Boolean = {
    hub.unregister(obj).isSuccess
  }
}
