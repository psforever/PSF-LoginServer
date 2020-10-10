// Copyright (c) 2017 PSForever
package net.psforever.services.avatar.support

import akka.actor.ActorRef
import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import net.psforever.objects.Player
import net.psforever.types.ExoSuitType
import net.psforever.services.{RemoverActor, Service}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._

class CorpseRemovalActor(taskResolver: ActorRef) extends RemoverActor(taskResolver) {
  final val FirstStandardDuration: FiniteDuration = 1 minute

  final val SecondStandardDuration: FiniteDuration = 500 milliseconds

  def InclusionTest(entry: RemoverActor.Entry): Boolean = {
    entry.obj.isInstanceOf[Player] && entry.obj.asInstanceOf[Player].isBackpack
  }

  def InitialJob(entry: RemoverActor.Entry): Unit = {}

  def FirstJob(entry: RemoverActor.Entry): Unit = {
    import net.psforever.objects.zones.Zone
    entry.zone.Population ! Zone.Corpse.Remove(entry.obj.asInstanceOf[Player])
    context.parent ! AvatarServiceMessage(
      entry.zone.id,
      AvatarAction.ObjectDelete(Service.defaultPlayerGUID, entry.obj.GUID)
    )
  }

  def ClearanceTest(entry: RemoverActor.Entry): Boolean = !entry.zone.Corpses.contains(entry.obj)

  def DeletionTask(entry: RemoverActor.Entry): TaskResolver.GiveTask = {
    val player = entry.obj.asInstanceOf[Player]
    val task   = GUIDTask.UnregisterPlayer(player)(entry.zone.GUID)
    player.ExoSuit = ExoSuitType.Standard
    task
  }
}
