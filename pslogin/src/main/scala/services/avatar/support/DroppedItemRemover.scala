// Copyright (c) 2017 PSForever
package services.avatar.support

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.{GUIDTask, TaskResolver}
import services.avatar.{AvatarAction, AvatarServiceMessage}
import services.{RemoverActor, Service}

import scala.concurrent.duration._

class DroppedItemRemover extends RemoverActor {
  final val FirstStandardDuration : FiniteDuration = 3 minutes

  final val SecondStandardDuration : FiniteDuration = 500 milliseconds

  def InclusionTest(entry : RemoverActor.Entry) : Boolean = {
    entry.obj.isInstanceOf[Equipment]
  }

  def InitialJob(entry : RemoverActor.Entry) : Unit = { }

  def FirstJob(entry : RemoverActor.Entry) : Unit = {
    import net.psforever.objects.zones.Zone
    entry.zone.Ground ! Zone.Ground.PickupItem(entry.obj.GUID)
    context.parent ! AvatarServiceMessage(entry.zone.Id, AvatarAction.ObjectDelete(Service.defaultPlayerGUID, entry.obj.GUID))
  }

  def ClearanceTest(entry : RemoverActor.Entry) : Boolean = true

  def DeletionTask(entry : RemoverActor.Entry) : TaskResolver.GiveTask = {
    GUIDTask.UnregisterEquipment(entry.obj.asInstanceOf[Equipment])(entry.zone.GUID)
  }
}
