// Copyright (c) 2017 PSForever
package net.psforever.services.local

import net.psforever.services.Service
import net.psforever.services.base.{EventMessage, GenericMessageToSupportEnvelope, GenericMessageToSupportEnvelopeOnly, MessageEnvelope}
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
                              channel: String,
                              msg: IsADoorMessage,
                              supportMessage: Any
                            ) extends GenericMessageToSupportEnvelope {
  def filter: PlanetSideGUID = Service.defaultPlayerGUID
  def supportLabel: String = "doorCloser"
}

final case class HackEntityMessage(
                                    channel: String,
                                    filter: PlanetSideGUID,
                                    msg: IsAHackMessage,
                                    supportMessage: Any
                                  ) extends GenericMessageToSupportEnvelope {
  def supportLabel: String = "hackClearer"
}

final case class HackClearMessage(supportMessage: Any)
  extends GenericMessageToSupportEnvelopeOnly {
  def supportLabel: String = "hackClearer"
}

final case class CaptureMessage(supportMessage: Any)
  extends GenericMessageToSupportEnvelopeOnly {
  def supportLabel: String = "hackCapturer"
}

final case class FlagMessage(supportMessage: CaptureFlagManager.Command)
  extends GenericMessageToSupportEnvelopeOnly {
  def supportLabel: String = "captureFlagManager"
}
