// Copyright (c) 2017 PSForever
package services.avatar.support

import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import net.psforever.objects.Player
import services.{RemoverActor, Service}
import services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._

class CorpseRemovalActor extends RemoverActor {
  final val FirstStandardDuration : FiniteDuration = 1 minute

  final val SecondStandardDuration : FiniteDuration = 500 milliseconds

  def InclusionTest(entry : RemoverActor.Entry) : Boolean = {
    entry.obj.isInstanceOf[Player] && entry.obj.asInstanceOf[Player].isBackpack
  }

  def InitialJob(entry : RemoverActor.Entry) : Unit = { }

  def FirstJob(entry : RemoverActor.Entry) : Unit = {
    import net.psforever.objects.zones.Zone
    entry.zone.Population ! Zone.Corpse.Remove(entry.obj.asInstanceOf[Player])
    context.parent ! AvatarServiceMessage(entry.zone.Id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, entry.obj.GUID))
  }

  def ClearanceTest(entry : RemoverActor.Entry) : Boolean = !entry.zone.Corpses.contains(entry.obj)

  def DeletionTask(entry : RemoverActor.Entry) : TaskResolver.GiveTask = {
    GUIDTask.UnregisterPlayer(entry.obj.asInstanceOf[Player])(entry.zone.GUID)
  }
}
