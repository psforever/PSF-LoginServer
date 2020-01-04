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
      .collect { case (facility, soi : ObjectDefinition with SphereOfInfluence) if soi.SOIRadius > 0 =>
        (facility, soi.SOIRadius * soi.SOIRadius)
      }
  }

  def UpdateSOI(): Unit = {
    SOI.Populate(sois.iterator, zone.LivePlayers)
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

  /**
    * Recursively populate each facility's sphere of influence with players.
    * @param buildings an iterator of buildings and the radius of its sphere of influence
    * @param players a list of players to allocate;
    *                the list gets shorter as each building is allocated
    */
  @tailrec
  def Populate(buildings : Iterator[(Building, Int)], players : List[Player]) : Unit = {
    if(players.nonEmpty && buildings.hasNext) {
      val (facility, radius) = buildings.next
      val (tenants, remainder) = players.partition(p => Vector3.DistanceSquared(facility.Position.xy, p.Position.xy) < radius)
      facility.PlayersInSOI = tenants
      Populate(buildings, remainder)
    }
  }
}
