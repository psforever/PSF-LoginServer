// Copyright (c) 2026 PSForever
package net.psforever.services.base.messages

import net.psforever.packet.PlanetSideGamePacket
import net.psforever.services.base.SelfRespondingEvent

final case class SendResponse(pkts: Seq[PlanetSideGamePacket]) extends SelfRespondingEvent

object SendResponse {
  def apply(pkt: PlanetSideGamePacket): SendResponse = SendResponse(Seq(pkt))
}
