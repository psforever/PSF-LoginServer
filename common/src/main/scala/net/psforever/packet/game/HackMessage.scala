// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
 *
 * @param unk1 na
 * @param unk2 na
 * @param unk3 na
 * @param unk4 na
 * @param unk5 na
 * @param unk6 na
 * @param unk7 na
 */
final case class HackMessage(unk1 : Int,
                             unk2 : Int,
                             unk3 : Int,
                             unk4 : Int,
                             unk5 : Long,
                             unk6 : Int,
                             unk7 : Long)
  extends PlanetSideGamePacket {
  type Packet = HackMessage
  def opcode = GamePacketOpcode.HackMessage
  def encode = HackMessage.encode(this)
}

object HackMessage extends Marshallable[HackMessage] {
  implicit val codec : Codec[HackMessage] = (
      ("unk1" | uint2L) ::
        ("unk2" | uint16L) ::
        ("unk3" | uint16L) ::
        ("unk4" | uint8L) ::
        ("unk5" | uint32L) ::
        ("unk6" | uint8L) ::
        ("unk7" | uint32L)
    ).as[HackMessage]
}
