// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.structures

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

class BuildingControl(building : Building) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = building

  def receive : Receive = checkBehavior.orElse {
    case FactionAffinity.ConvertFactionAffinity(faction) =>
      val originalAffinity = building.Faction
      if(originalAffinity != (building.Faction = faction)) {
        building.Amenities.foreach(_.Actor forward FactionAffinity.ConfirmFactionAffinity())
      }
      sender ! FactionAffinity.AssertFactionAffinity(building, faction)
    case _ => ;
  }
}
