package net.psforever.objects.zones;
import akka.actor.{Actor, Cancellable}
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.structures.{Building, SphereOfInfluence}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SphereOfInfluenceActor(zone: Zone) extends Actor {
  def receive : Receive = Established

  private var populateTick: Cancellable = context.system.scheduler.scheduleOnce(5 seconds, self, SOI.Populate())
  private[this] val log = org.log4s.getLogger(s"${zone.Id.capitalize}-SphereOfInfluenceActor")

  def Established : Receive = {
    case SOI.Populate() =>
      UpdateSOI()
  }

  def UpdateSOI(): Unit = {
    val players = zone.LivePlayers

    zone.Buildings.foreach({
      case (_, building : Building) =>
        building.Definition match {
        case _ : ObjectDefinition with SphereOfInfluence =>
          // todo: overlapping soi (e.g. tower soi in base soi) order by smallest soi first?
          val playersInSoi = players.filter(p => Math.pow(p.Position.x - building.Position.x, 2) + Math.pow(p.Position.y - building.Position.y, 2) < Math.pow(300, 2) )
          if(playersInSoi.length > 0) {
//            log.info(s"Building ${building.GUID} players in soi: ${playersInSoi.toString()}" )
          }
          building.PlayersInSOI = playersInSoi
        case _ => ;
      }

    })

    populateTick = context.system.scheduler.scheduleOnce(5 seconds, self, SOI.Populate())
  }
}

object SOI {
  /** Populate the list of players within a SOI **/
  final case class Populate()
}