// Copyright (c) 2017 PSForever
package net.psforever.services.avatar.support

import net.psforever.objects.guid.{GUIDTask, TaskBundle}
import net.psforever.objects.Player
import net.psforever.types.ExoSuitType
import net.psforever.services.RemoverActor
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._

class CorpseRemovalActor extends RemoverActor() {
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
      AvatarAction.ObjectDelete(entry.obj.GUID, unk=1)
    )
  }

  def ClearanceTest(entry: RemoverActor.Entry): Boolean = !entry.zone.Corpses.contains(entry.obj)

  def DeletionTask(entry: RemoverActor.Entry): TaskBundle = {
    val player = entry.obj.asInstanceOf[Player]
    val task   = GUIDTask.unregisterPlayer(entry.zone.GUID, player)
    player.ExoSuit = ExoSuitType.Standard
    task
  }
}
