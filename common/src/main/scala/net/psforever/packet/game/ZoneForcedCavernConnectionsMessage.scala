// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched to the client.
  * @param zone the zone
  * @param unk na;
  *            usually 0
  */
final case class ZoneForcedCavernConnectionsMessage(zone : PlanetSideGUID,
                                                    unk : Int)
  extends PlanetSideGamePacket {
  type Packet = ZoneForcedCavernConnectionsMessage
  def opcode = GamePacketOpcode.ZoneForcedCavernConnectionsMessage
  def encode = ZoneForcedCavernConnectionsMessage.encode(this)
}

object ZoneForcedCavernConnectionsMessage extends Marshallable[ZoneForcedCavernConnectionsMessage] {
  implicit val codec : Codec[ZoneForcedCavernConnectionsMessage] = (
    ("zone_guid" | PlanetSideGUID.codec) ::
      ("unk" | uint2L)
    ).as[ZoneForcedCavernConnectionsMessage]
}
