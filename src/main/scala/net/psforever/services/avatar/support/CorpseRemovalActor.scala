// Copyright (c) 2017 PSForever
package net.psforever.services.avatar.support

import net.psforever.objects.guid.{GUIDTask, TaskBundle}
import net.psforever.objects.Player
import net.psforever.types.{ExoSuitType, PlanetSideGUID}
import net.psforever.services.{RemoverActor, Service}
import net.psforever.services.avatar.AvatarAction.Release
import net.psforever.services.avatar.AvatarServiceMessage
import net.psforever.services.base.{GenericSupportEnvelope, GenericSupportEnvelopeOnly}
import net.psforever.services.base.message.ObjectDelete

import scala.concurrent.duration._

final case class ReleaseEnvelope(
                                  originalChannel: String,
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
    ReleaseEnvelope(channel, Service.defaultPlayerGUID, actionMessage)
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
    context.parent ! AvatarServiceMessage(
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
