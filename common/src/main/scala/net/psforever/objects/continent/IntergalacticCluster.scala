// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import akka.actor.{Actor, Props}
import net.psforever.objects.Player

import scala.annotation.tailrec

class IntergalacticCluster(zones : List[Zone]) extends Actor {
  private[this] val log = org.log4s.getLogger
  log.info("Starting interplanetary cluster ...")

  override def preStart() : Unit = {
    super.preStart()
    for(zone <- zones) {
      log.info(s"Built continent ${zone.ZoneId}")
      zone.Actor = context.actorOf(Props(classOf[ZoneActor], zone), s"${zone.ZoneId}-actor")
    }
  }

  def receive : Receive = {
    case IntergalacticCluster.GetWorld(zoneId) =>
      log.info(s"Asked to find $zoneId")
      findWorldInCluster(zones.iterator, zoneId) match {
        case Some(continent) =>
          sender ! IntergalacticCluster.GiveWorld(zoneId, continent)
        case None =>
          log.error(s"Requested zone with id $zoneId could not be found")
      }

    case IntergalacticCluster.RequestZoneInitialization(tplayer) =>
      zones.foreach(zone => {
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
