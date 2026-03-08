// Copyright (c) 2017 PSForever
package net.psforever.services.avatar.support

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.equipment.Equipment
import net.psforever.objects.guid.{GUIDTask, TaskBundle}
import net.psforever.objects.zones.Zone
import net.psforever.services.avatar.AvatarAction.{DropItem, PickupItem}
import net.psforever.services.Service
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.base.{EventServiceSupport, GenericSupportEnvelope, GenericSupportEnvelopeOnly}
import net.psforever.services.base.message.ObjectDelete
import net.psforever.services.base.support.RemoverActor
import net.psforever.types.PlanetSideGUID

import scala.concurrent.duration._

case object LitterRemovalSupport
  extends EventServiceSupport {
  def label: String = "janitor"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[DroppedItemRemover](), name = "DroppedItemRemover")
  }
}

final case class PickupItemEnvelope(
                                     channel: String,
                                     filter: PlanetSideGUID,
                                     msg: PickupItem,
                                     zone: Zone
                                   )
  extends GenericSupportEnvelope {
  def supportLabel: String = "janitor"
  def supportMessage: Any = {
    val PickupItem(item, _) = msg
    RemoverActor.ClearSpecific(List(item), zone)
  }
}

object PickupItemEnvelope {
  def apply(channel: String, actionMessage: PickupItem, zone: Zone): PickupItemEnvelope =
    PickupItemEnvelope(channel, Service.defaultPlayerGUID, actionMessage, zone)
}

final case class DropItemEnvelope(
                                   channel: String,
                                   filter: PlanetSideGUID,
                                   msg: DropItem,
                                   zone: Zone
                                 )
  extends GenericSupportEnvelope {
  def supportLabel: String = "janitor"
  def supportMessage: Any = {
    val DropItem(item) = msg
    RemoverActor.AddTask(item, zone)
  }
}

object DropItemEnvelope {
  def apply(channel: String, actionMessage: DropItem, zone: Zone): DropItemEnvelope =
    DropItemEnvelope(channel, Service.defaultPlayerGUID, actionMessage, zone)
}

final case class GroundEnvelope(supportMessage: Any)
  extends GenericSupportEnvelopeOnly {
  def supportLabel: String = "janitor"
}

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
    context.parent ! MessageEnvelope(
      entry.zone.id,
      ObjectDelete(entry.obj.GUID)
    )
  }

  def ClearanceTest(entry: RemoverActor.Entry): Boolean = !entry.zone.EquipmentOnGround.contains(entry.obj)

  def DeletionTask(entry: RemoverActor.Entry): TaskBundle = {
    GUIDTask.unregisterEquipment(entry.zone.GUID, entry.obj.asInstanceOf[Equipment])
  }
}
