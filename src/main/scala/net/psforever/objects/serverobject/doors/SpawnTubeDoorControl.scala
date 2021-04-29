// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.doors

import akka.actor.ActorRef
import net.psforever.objects.vital.Vitality

class SpawnTubeDoorControl(door: Door)
  extends DoorControl(door) {
  lazy val spawnTube: ActorRef = {
    import net.psforever.objects.serverobject.tube.SpawnTube
    import net.psforever.types.Vector3

    val dpos = door.Position
    door.Owner.Amenities
      .filter { _.isInstanceOf[SpawnTube] }
      .minBy { tube => Vector3.DistanceSquared(dpos, tube.Position) }
      .Actor
  }

  //doors do not implement the Damageable mxin, so it should be fine just catching the message
  override def commonBehavior: Receive = super.commonBehavior
    .orElse {
      case msg: Vitality.Damage =>
        spawnTube.forward(msg)
    }
}
