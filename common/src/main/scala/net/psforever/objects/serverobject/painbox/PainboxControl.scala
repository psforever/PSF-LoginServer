package net.psforever.objects.serverobject.painbox

import akka.actor.{Actor, Cancellable}
import net.psforever.objects.DefaultCancellable
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.structures.Building
import net.psforever.types.{PlanetSideEmpire, Vector3}
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class PainboxControl(painbox: Painbox) extends Actor {
  //private[this] val log = org.log4s.getLogger(s"Painbox")
  private var painboxTick: Cancellable = DefaultCancellable.obj
  private var nearestDoor : Door = null

  def receive : Receive = {
    case "startup" =>
      painbox.Owner match {
        case obj : Building =>
          nearestDoor = obj.Amenities
            .collect { case door : Door => door }
            .minBy(door => Vector3.DistanceSquared(painbox.Position, door.Position))
          painboxTick = context.system.scheduler.schedule(0 seconds,1 second, self, Painbox.Tick())
          context.become(Processing)
        case _ => ;
      }

    case _ => ;
  }

  def Processing : Receive = {
    case Painbox.Tick() =>
      //todo: Account for overlapping pain fields
      //todo: Pain module
      //todo: REK boosting
      val owner = painbox.Owner.asInstanceOf[Building]
      val faction = owner.Faction
      if(faction != PlanetSideEmpire.NEUTRAL && nearestDoor.Open.nonEmpty) {
        val events = owner.Zone.AvatarEvents
        val damage = painbox.Definition.Damage
        val radius = painbox.Definition.Radius * painbox.Definition.Radius
        val position = painbox.Position
        owner.PlayersInSOI
          .collect { case p if p.Faction != faction
            && p.Health > 0
            && Vector3.DistanceSquared(p.Position, position) < radius =>
            events ! AvatarServiceMessage(p.Name, AvatarAction.EnvironmentalDamage(p.GUID, damage))
          }
      }
  }
}
