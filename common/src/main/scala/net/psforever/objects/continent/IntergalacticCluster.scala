// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import akka.actor.Actor

import scala.annotation.tailrec

class IntergalacticCluster(continents : List[Continent]) extends Actor {
  //private[this] val log = org.log4s.getLogger
  for(continent <- continents) {
    continent.Actor //seed context
  }

  def receive : Receive = {
    case IntergalacticCluster.GetWorld(zoneId) =>
      findWorldInCluster(continents.iterator, zoneId) match {
        case Some(continent) =>
          sender ! IntergalacticCluster.GiveWorld(zoneId, continent)
        case None =>
          sender ! IntergalacticCluster.GiveWorld(zoneId, Continent.Nowhere)
      }

    case _ => ;
  }

  @tailrec private def findWorldInCluster(iter : Iterator[Continent], zoneId : String) : Option[Continent] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val cont = iter.next
      if(cont.ZoneId == zoneId) {
        Some(cont)
      }
      else {
        findWorldInCluster(iter, zoneId)
      }
    }
  }
}

object IntergalacticCluster {
  final case class GetWorld(zoneId : String)

  final case class GiveWorld(zoneId : String, zone : Continent)
}
