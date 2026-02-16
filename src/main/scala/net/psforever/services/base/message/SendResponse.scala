// Copyright (c) 2026 PSForever
package net.psforever.services.base.message

import net.psforever.packet.PlanetSideGamePacket

final case class SendResponse(pkts: Seq[PlanetSideGamePacket]) extends SelfRespondingEvent

object SendResponse {
  def apply(pkt: PlanetSideGamePacket): SendResponse = SendResponse(Seq(pkt))
}
