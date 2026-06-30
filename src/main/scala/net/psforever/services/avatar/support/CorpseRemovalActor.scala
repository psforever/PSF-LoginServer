// Copyright (c) 2017 PSForever
package net.psforever.services.avatar.support

import akka.actor.{ActorContext, ActorRef, Props}
import net.psforever.objects.guid.{GUIDTask, TaskBundle}
import net.psforever.objects.Player
import net.psforever.types.{ExoSuitType, PlanetSideGUID}
import net.psforever.services.avatar.AvatarAction.Release
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.base.{EventServiceSupport, GenericSupportEnvelope, GenericSupportEnvelopeOnly}
import net.psforever.services.base.message.ObjectDelete
import net.psforever.services.base.support.RemoverActor

import scala.concurrent.duration._

case object CorpseRemovalSupport
  extends EventServiceSupport {
  def label: String = "undertaker"
  def constructor(context: ActorContext): ActorRef = {
    context.actorOf(Props[CorpseRemovalActor](), name = "CorpseRemoval")
  }
}

final case class ReleaseEnvelope(
                                  channel: String,
                                  filter: PlanetSideGUID,
                                  msg: Release
                                )
  extends GenericSupportEnvelope {
  def supportLabel: String = "undertaker"
  def supportMessage: Any = {
    val Release(player, zone, time) = msg
    RemoverActor.AddTask(player, zone, time)
  }
}

object ReleaseEnvelope {
  def apply(channel: String, actionMessage: Release): ReleaseEnvelope =
    ReleaseEnvelope(channel, actionMessage.player.GUID, actionMessage)
}

final case class CorpseEnvelope(supportMessage: Any)
  extends GenericSupportEnvelopeOnly {
  def supportLabel: String = "undertaker"
}

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
    context.parent ! MessageEnvelope(
      entry.zone.id,
      ObjectDelete(entry.obj.GUID, unk=1)
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
