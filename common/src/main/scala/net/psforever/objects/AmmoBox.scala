// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.AmmoBoxDefinition
import net.psforever.objects.equipment.{Ammo, Equipment}

class AmmoBox(private val ammoDef : AmmoBoxDefinition,
              cap : Option[Int] = None
             ) extends Equipment {
  private var capacity = if(cap.isDefined) { AmmoBox.limitCapacity(cap.get, 1) } else { FullCapacity }

  def AmmoType : Ammo.Value = ammoDef.AmmoType

  def Capacity : Int = capacity

  def Capacity_=(toCapacity : Int) : Int = {
    capacity = AmmoBox.limitCapacity(toCapacity)
    Capacity
  }

  def FullCapacity : Int = ammoDef.Capacity

  def Definition : AmmoBoxDefinition = ammoDef

  override def toString : String = {
    AmmoBox.toString(this)
  }
}

object AmmoBox {
  def apply(ammoDef : AmmoBoxDefinition) : AmmoBox = {
    new AmmoBox(ammoDef)
  }

  def apply(ammoDef : AmmoBoxDefinition, capacity : Int) : AmmoBox = {
    new AmmoBox(ammoDef, Some(capacity))
  }

  import net.psforever.packet.game.PlanetSideGUID
  def apply(guid : PlanetSideGUID, ammoDef : AmmoBoxDefinition) : AmmoBox = {
    val obj = new AmmoBox(ammoDef)
    obj.GUID = guid
    obj
  }

  def apply(guid : PlanetSideGUID, ammoDef : AmmoBoxDefinition, capacity : Int) : AmmoBox = {
    val obj = new AmmoBox(ammoDef, Some(capacity))
    obj.GUID = guid
    obj
  }

  def limitCapacity(count : Int, min : Int = 0) : Int = math.min(math.max(min, count), 65535)

  def toString(obj : AmmoBox) : String = {
    s"box of ${obj.AmmoType} ammo (${obj.Capacity})"
  }
}
