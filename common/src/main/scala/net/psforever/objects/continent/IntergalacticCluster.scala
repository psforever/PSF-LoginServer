// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import akka.actor.Actor
import net.psforever.objects.Player

import scala.annotation.tailrec

class IntergalacticCluster(continents : List[Zone]) extends Actor {
  private[this] val log = org.log4s.getLogger
  for(continent <- continents) {
    log.info(s"Built continent ${continent.ZoneId}")
    continent.Actor //seed context
  }

  def receive : Receive = {
    case IntergalacticCluster.GetWorld(zoneId) =>
      log.info(s"Asked to find $zoneId")
      findWorldInCluster(continents.iterator, zoneId) match {
        case Some(continent) =>
          sender ! IntergalacticCluster.GiveWorld(zoneId, continent)
        case None =>
          sender ! IntergalacticCluster.GiveWorld(zoneId, Zone.Nowhere)
      }

    case IntergalacticCluster.RequestZoneInitialization(tplayer) =>
      continents.foreach(zone => {
        sender ! Zone.ClientInitialization(zone.ClientInitialization())
      })
      sender ! IntergalacticCluster.ClientInitializationComplete(tplayer)

    case _ => ;
  }

  @tailrec private def findWorldInCluster(iter : Iterator[Zone], zoneId : String) : Option[Zone] = {
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

  final case class GiveWorld(zoneId : String, zone : Zone)

  final case class RequestZoneInitialization(tplayer : Player)

  final case class ClientInitializationComplete(tplayer : Player)
}
