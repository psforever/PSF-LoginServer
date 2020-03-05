//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.Actor.Receive
import net.psforever.objects.Tool
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.ZoneAware

trait Repairable {
  def RepairableObject : Repairable.Target

  def canBeRepairedByNanoDispenser : Receive

  def RepairValue(item : Tool) : Int = 0

  def Restoration(obj : Repairable.Target) : Unit = {
    obj.Destroyed = false
  }
}

object Repairable {
  type Target = PlanetSideServerObject with Vitality with FactionAffinity with ZoneAware
}
