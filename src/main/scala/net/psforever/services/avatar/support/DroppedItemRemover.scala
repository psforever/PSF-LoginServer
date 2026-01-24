// Copyright (c) 2017 PSForever
package net.psforever.services.avatar.support

import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.{GUIDTask, TaskBundle}
import net.psforever.services.{RemoverActor, Service}
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}

import scala.concurrent.duration._

class DroppedItemRemover extends RemoverActor() {
  final val FirstStandardDuration: FiniteDuration = 3 minutes

  final val SecondStandardDuration: FiniteDuration = 500 milliseconds

  def InclusionTest(entry: RemoverActor.Entry): Boolean = {
    entry.obj.isInstanceOf[Equipment]
  }

  def InitialJob(entry: RemoverActor.Entry): Unit = {}

  def FirstJob(entry: RemoverActor.Entry): Unit = {
    import net.psforever.objects.zones.Zone
    entry.zone.Ground ! Zone.Ground.RemoveItem(entry.obj.GUID)
    context.parent ! AvatarServiceMessage(
      entry.zone.id,
      AvatarAction.ObjectDelete(entry.obj.GUID)
    )
  }

  def ClearanceTest(entry: RemoverActor.Entry): Boolean = !entry.zone.EquipmentOnGround.contains(entry.obj)

  def DeletionTask(entry: RemoverActor.Entry): TaskBundle = {
    GUIDTask.unregisterEquipment(entry.zone.GUID, entry.obj.asInstanceOf[Equipment])
  }
}
