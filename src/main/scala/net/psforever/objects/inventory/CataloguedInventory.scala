// Copyright (c) 2020 PSForever
package net.psforever.objects.inventory

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.SpecificNumberSource
import net.psforever.types.PlanetSideGUID

import scala.util.Success

class CataloguedInventory(numbers: Iterable[Int]) extends GridInventory {
  private val hub: NumberPoolHub = {
    val numHub = new NumberPoolHub(SpecificNumberSource(numbers))
    numHub.AddPool("internal", numbers.toList).Selector = new RandomSelector
    numHub
  }

  override def Insert(start : Int, obj : Equipment) : Boolean = {
    if(!obj.HasGUID) {
      hub.register(obj, name = "internal") match {
        case Success(_) if super.Insert(start, obj) =>
          true
        case Success(_) =>
          hub.unregister(obj)
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
      hub.register(obj, name = "internal") match {
        case Success(_) if super.InsertQuickly(start, obj) =>
          true
        case Success(_) =>
          hub.unregister(obj)
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
      case Some(obj) if super.Remove(guid) =>
        hub.unregister(obj).isSuccess
      case _ =>
        false
    }
  }

  override def Remove(index : Int) : Boolean = {
    Slot(index).Equipment match {
      case Some(obj) if super.Remove(obj.GUID) =>
        hub.unregister(obj).isSuccess
      case _ =>
        false
    }
  }
}
