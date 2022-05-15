// Copyright (c) 2022 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class LinkOverride(
                               unk1: Int,
                               unk2: Int,
                               unk3: Int,
                               unk4: Int
                             )

final case class WarpgateLinkOverrideMessage(links: List[LinkOverride])
  extends PlanetSideGamePacket {
  type Packet = WarpgateLinkOverrideMessage
  def opcode = GamePacketOpcode.WarpgateLinkOverrideMessage
  def encode = WarpgateLinkOverrideMessage.encode(this)
}

object WarpgateLinkOverrideMessage extends Marshallable[WarpgateLinkOverrideMessage] {
  private val linkOverrideCodec: Codec[LinkOverride] = (
    ("unk1" | uint16L) ::
    ("unk2" | uint16L) ::
    ("unk3" | uint16L) ::
    ("unk4" | uint16L)
  ).as[LinkOverride]

  implicit val codec: Codec[WarpgateLinkOverrideMessage] =
    ("links" | listOfN(uint16L, linkOverrideCodec)).as[WarpgateLinkOverrideMessage]
}
