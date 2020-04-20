// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched to the client in regards to cavern connections via geowarp gates.
  * @param zone the zone
  * @param unk determines the number and composition of cavern links
  *            assuming two geowarps per continent the logic seems to be roughly:
  *            0 - gate A disabled, B active
  *            1 - gate A active, B disabled
  *            2 - gate A and B active
  *            3 - same as 2 (no change in destination)
  *            Destinations also change (north/south/east/west), but seemingly only to two of the currently active caverns can be linked to?
  */
final case class ZoneForcedCavernConnectionsMessage(zone : Int,
                                                    unk : Int)
  extends PlanetSideGamePacket {
  type Packet = ZoneForcedCavernConnectionsMessage
  def opcode = GamePacketOpcode.ZoneForcedCavernConnectionsMessage
  def encode = ZoneForcedCavernConnectionsMessage.encode(this)
}

object ZoneForcedCavernConnectionsMessage extends Marshallable[ZoneForcedCavernConnectionsMessage] {
  implicit val codec : Codec[ZoneForcedCavernConnectionsMessage] = (
    ("zone" | uint16L) ::
      ("unk" | uint2L)
    ).as[ZoneForcedCavernConnectionsMessage]
}
