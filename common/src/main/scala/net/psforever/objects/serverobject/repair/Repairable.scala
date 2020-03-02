//Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.repair

import akka.actor.Actor.Receive
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.ZoneAware

trait Repairable {
  def RepairableObject : PlanetSideGameObject with Vitality with FactionAffinity with ZoneAware

  def canBeRepairedByNanoDispenser : Receive
}
