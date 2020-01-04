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
  private[this] val log = org.log4s.getLogger(s"Painbox")
  var painboxTick: Cancellable = DefaultCancellable.obj
  var nearestDoor : Option[Door] = None

  def receive : Receive = {
    case "startup" =>
      if(painbox.Definition.HasNearestDoorDependency) {
        (painbox.Owner match {
          case obj : Building =>
            obj.Amenities
              .collect { case door : Door => door }
              .sortBy(door => Vector3.DistanceSquared(painbox.Position, door.Position))
              .headOption
          case _ =>
            None
        }) match {
          case door @ Some(_) =>
            nearestDoor = door
            context.become(Stopped)
          case _ =>
            log.error(s"object #${painbox.GUID.guid} can not find a door that it needed")
        }
      }
      else {
        context.become(Stopped)
      }

    case _ => ;
  }

  def Running : Receive = {
    case Painbox.Stop() =>
      context.become(Stopped)
      painboxTick.cancel
      painboxTick = DefaultCancellable.obj

    case Painbox.Tick() =>
      //todo: Account for overlapping pain fields
      //todo: Pain module
      //todo: REK boosting
      val guid = painbox.GUID
      val owner = painbox.Owner.asInstanceOf[Building]
      val faction = owner.Faction
      if(faction != PlanetSideEmpire.NEUTRAL && (nearestDoor match { case Some(door) => door.Open.nonEmpty; case _ => true })) {
        val events = owner.Zone.AvatarEvents
        val damage = painbox.Definition.Damage
        val radius = painbox.Definition.Radius * painbox.Definition.Radius
        val position = painbox.Position
        owner.PlayersInSOI
          .collect { case p if p.Faction != faction
            && p.Health > 0
            && Vector3.DistanceSquared(p.Position, position) < radius =>
            events ! AvatarServiceMessage(p.Name, AvatarAction.EnvironmentalDamage(p.GUID, guid, damage))
          }
      }

    case _ => ;
  }

  def Stopped : Receive = {
    case Painbox.Start() =>
      context.become(Running)
      painboxTick.cancel
      painboxTick = context.system.scheduler.schedule(0 seconds, 1 second, self, Painbox.Tick())

    case _ => ;
  }
}
