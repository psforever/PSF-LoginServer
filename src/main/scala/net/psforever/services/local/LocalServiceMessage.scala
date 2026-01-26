// Copyright (c) 2017 PSForever
package net.psforever.services.local

import net.psforever.services.Service
import net.psforever.services.base.{EventMessage, GenericMessageEnvelope, GenericMessageToSupportEnvelope, GenericMessageToSupportEnvelopeOnly}
import net.psforever.services.local.LocalAction.DoorSlamsShut
import net.psforever.types.PlanetSideGUID


final case class LocalServiceMessage(channel: String, filter: PlanetSideGUID, msg: EventMessage)
  extends GenericMessageEnvelope

object LocalServiceMessage {
  def apply(channel: String, localMessage: EventMessage): LocalServiceMessage =
    LocalServiceMessage(channel, Service.defaultPlayerGUID, localMessage)

  final case class Deployables(msg: Any)
}

final case class DoorMessage(
                                 channel: String,
                                 filter: PlanetSideGUID,
                                 msg: DoorSlamsShut,
                                 supportMessage: Any
                               )
  extends GenericMessageToSupportEnvelope {
  def supportLabel: String = "doorCloser"
}

final case class HackEntityMessage(
                                    channel: String,
                                    filter: PlanetSideGUID,
                                    msg: EventMessage,
                                    supportMessage: Any
                                  )
  extends GenericMessageToSupportEnvelope {
  def supportLabel: String = "hackClearer"
}

final case class ClearMessage(supportMessage: Any)
  extends GenericMessageToSupportEnvelopeOnly {
  def supportLabel: String = "hackClearer"
}

final case class CaptureMessage(supportMessage: Any)
  extends GenericMessageToSupportEnvelopeOnly {
  def supportLabel: String = "hackCapturer"
}

final case class FlagMessage(supportMessage: Any)
  extends GenericMessageToSupportEnvelopeOnly {
  def supportLabel: String = "captureFlagManager"
}
