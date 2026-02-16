// Copyright (c) 2017-2026 PSForever
package net.psforever.services.avatar

import net.psforever.objects.zones.Zone
import net.psforever.services.{RemoverActor, Service}
import net.psforever.services.avatar.AvatarAction.{DropItem, PickupItem, Release}
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.base.message.EventMessage
import net.psforever.services.base.{GenericSupportEnvelope, GenericSupportEnvelopeOnly}
import net.psforever.types.PlanetSideGUID

object AvatarServiceMessage {
  def apply(channel: String, msg: EventMessage): MessageEnvelope =
    MessageEnvelope(channel, Service.defaultPlayerGUID, msg)

  def apply(channel: String, filter: PlanetSideGUID, msg: EventMessage): MessageEnvelope =
    MessageEnvelope(channel, filter, msg)
}

final case class ReleaseMessage(
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

object ReleaseMessage {
  def apply(channel: String, actionMessage: Release): ReleaseMessage =
    ReleaseMessage(channel, Service.defaultPlayerGUID, actionMessage)
}

final case class PickupItemMessage(
                                    originalChannel: String,
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

object PickupItemMessage {
  def apply(channel: String, actionMessage: PickupItem, zone: Zone): PickupItemMessage =
    PickupItemMessage(channel, Service.defaultPlayerGUID, actionMessage, zone)
}

final case class DropItemMessage(
                                  originalChannel: String,
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

object DropItemMessage {
  def apply(channel: String, actionMessage: DropItem, zone: Zone): DropItemMessage =
    DropItemMessage(channel, Service.defaultPlayerGUID, actionMessage, zone)
}

final case class CorpseEnvelope(supportMessage: Any)
  extends GenericSupportEnvelopeOnly {
  def supportLabel: String = "undertaker"
}

final case class GroundEnvelope(supportMessage: Any)
  extends GenericSupportEnvelopeOnly {
  def supportLabel: String = "janitor"
}
