// Copyright (c) 2017-2026 PSForever
package net.psforever.services.avatar

import net.psforever.objects.zones.Zone
import net.psforever.services.{RemoverActor, Service}
import net.psforever.services.avatar.AvatarAction.{DropItem, PickupItem, Release}
import net.psforever.services.base.{EventMessage, GenericMessageEnvelope, GenericMessageToSupportEnvelope, GenericMessageToSupportEnvelopeOnly}
import net.psforever.types.PlanetSideGUID

final case class AvatarServiceMessage(channel: String, filter: PlanetSideGUID, msg: EventMessage)
  extends GenericMessageEnvelope

object AvatarServiceMessage {
  def apply(channel: String, actionMessage: EventMessage): AvatarServiceMessage =
    AvatarServiceMessage(channel, Service.defaultPlayerGUID, actionMessage)
}

final case class ReleaseMessage(
                                 channel: String,
                                 filter: PlanetSideGUID,
                                 msg: Release
                               )
  extends GenericMessageToSupportEnvelope {
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
                                    channel: String,
                                    filter: PlanetSideGUID,
                                    msg: PickupItem,
                                    zone: Zone
                                  )
  extends GenericMessageToSupportEnvelope {
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
                                  channel: String,
                                  filter: PlanetSideGUID,
                                  msg: DropItem,
                                  zone: Zone
                                )
  extends GenericMessageToSupportEnvelope {
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
  extends GenericMessageToSupportEnvelopeOnly {
  def supportLabel: String = "undertaker"
}

final case class GroundEnvelope(supportMessage: Any)
  extends GenericMessageToSupportEnvelopeOnly {
  def supportLabel: String = "janitor"
}
