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
  private var painboxTick: Cancellable = DefaultCancellable.obj
  private var nearestDoor : Door = null

  def receive : Receive = {
    case "startup" =>
      nearestDoor = painbox.Owner.asInstanceOf[Building].Amenities
        .collect { case door : Door => door }
        .minBy(door => Vector3.DistanceSquared(painbox.Position, door.Position))
      context.become(Processing)
      painboxTick = context.system.scheduler.schedule(0 seconds,1 second, self, Painbox.Tick())

    case _ => ;
  }

  def Processing : Receive = {
    case Painbox.Tick() =>
      //todo: Account for overlapping pain fields
      if(painbox.Owner.Faction == PlanetSideEmpire.NEUTRAL) return null
      if(painbox.Definition.HasNearestDoorDependency && nearestDoor.Open.isEmpty) return null

      val playersToCheck = painbox.Owner.asInstanceOf[Building].PlayersInSOI
      if(playersToCheck.isEmpty) return null
      // todo: Disable if no base power

      val playersInRange = playersToCheck.filter(p =>
        p.Faction != painbox.Owner.Faction
          && p.Health > 0
          && Math.pow(p.Position.x - painbox.Position.x, 2) + Math.pow(p.Position.y - painbox.Position.y, 2) + Math.pow(p.Position.z - painbox.Position.z, 2) < Math.pow(painbox.Definition.Radius, 2)
      )

      // Make 'em hurt.
      playersInRange.foreach({ p =>
        painbox.Owner.asInstanceOf[Building].Zone.AvatarEvents ! AvatarServiceMessage(
          p.Name,
          AvatarAction.EnvironmentalDamage(p.GUID, painbox.Definition.Damage)
        )
        // todo: Pain module
        // todo: REK boosting
      })
  }
}
