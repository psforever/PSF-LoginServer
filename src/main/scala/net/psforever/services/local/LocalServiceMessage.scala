// Copyright (c) 2017 PSForever
package net.psforever.services.local

import net.psforever.services.Service
import net.psforever.services.base.envelope.MessageEnvelope
import net.psforever.services.base.message.EventMessage
import net.psforever.services.base.{GenericSupportEnvelope, GenericSupportEnvelopeOnly}
import net.psforever.services.local.LocalAction.{IsADoorMessage, IsAHackMessage}
import net.psforever.services.local.support.CaptureFlagManager
import net.psforever.types.PlanetSideGUID

object LocalServiceMessage {
  def apply(channel: String, localMessage: EventMessage): MessageEnvelope =
    MessageEnvelope(channel, Service.defaultPlayerGUID, localMessage)

  def apply(channel: String, filter: PlanetSideGUID, msg: EventMessage): MessageEnvelope =
    MessageEnvelope(channel, filter, msg)
}

final case class DoorMessage(
                              originalChannel: String,
                              msg: IsADoorMessage,
                              supportMessage: Any
                            ) extends GenericSupportEnvelope {
  def filter: PlanetSideGUID = Service.defaultPlayerGUID
  def supportLabel: String = "doorCloser"
}

final case class HackEntityMessage(
                                    originalChannel: String,
                                    filter: PlanetSideGUID,
                                    msg: IsAHackMessage,
                                    supportMessage: Any
                                  ) extends GenericSupportEnvelope {
  def supportLabel: String = "hackClearer"
}

final case class HackClearMessage(supportMessage: Any)
  extends GenericSupportEnvelopeOnly {
  def supportLabel: String = "hackClearer"
}

final case class CaptureMessage(supportMessage: Any)
  extends GenericSupportEnvelopeOnly {
  def supportLabel: String = "hackCapturer"
}

final case class FlagMessage(supportMessage: CaptureFlagManager.Command)
  extends GenericSupportEnvelopeOnly {
  def supportLabel: String = "captureFlagManager"
}
