package net.psforever.objects.zones

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.{DefaultCancellable, Player}
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.structures.{Building, SphereOfInfluence}
import net.psforever.types.Vector3

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SphereOfInfluenceActor(zone: Zone) extends Actor {
  var sois : Iterable[(Building, Int)] = Nil
  var populateTick : Cancellable = DefaultCancellable.obj
  //private[this] val log = org.log4s.getLogger(s"${zone.Id.capitalize}-SphereOfInfluenceActor")

  def receive : Receive = Stopped

  def Build : Receive = {
    case SOI.Build() =>
      BuildSOI()
  }

  def Running : Receive = Build.orElse {
    case SOI.Populate() =>
      UpdateSOI()

    case SOI.Stop() =>
      context.become(Stopped)
      populateTick.cancel
      sois.foreach { case (facility, _) => facility.PlayersInSOI = Nil }

    case _ => ;
  }

  def Stopped : Receive = Build.orElse {
    case SOI.Start() if sois.nonEmpty =>
      context.become(Running)
      UpdateSOI()

    case _ => ;
  }

  def BuildSOI() : Unit = {
    sois = zone.Buildings
      .values
      .map { facility => (facility, facility.Definition) }
      .collect { case (facility, soi) if soi.SOIRadius > 0 =>
        (facility, soi.SOIRadius * soi.SOIRadius)
      }
  }

  def UpdateSOI(): Unit = {
    sois.foreach { case (facility, radius) =>
      facility.PlayersInSOI = zone.LivePlayers.filter(p => Vector3.DistanceSquared(facility.Position.xy, p.Position.xy) < radius)
    }
    populateTick.cancel
    populateTick = context.system.scheduler.scheduleOnce(5 seconds, self, SOI.Populate())
  }
}

object SOI {
  /** Rebuild the list of facility SOI data **/
  final case class Build()
  /** Populate the list of players within a SOI **/
  final case class Populate()
  /** Stop sorting players into sois */
  final case class Start()
  /** Stop sorting players into sois */
  final case class Stop()
}
