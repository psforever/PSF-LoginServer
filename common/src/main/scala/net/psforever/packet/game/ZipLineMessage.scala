// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.bits.ByteVector
import scodec.codecs._
//(ZipLineMessage; remainder: ByteVector(19 bytes, 0x4b0013e0000013c05ce867bcafc88172548840)

final case class ZipLineMessage(player_guid : PlanetSideGUID,
                                unk1 : Boolean,
                                unk2 : Int,
                                unk3 : Long,
                                unk4 : Long,
                                unk5 : Long,
                                unk6 : Long)
  extends PlanetSideGamePacket {
  type Packet = ZipLineMessage
  def opcode = GamePacketOpcode.ZipLineMessage
  def encode = ZipLineMessage.encode(this)
}

object ZipLineMessage extends Marshallable[ZipLineMessage] {
  implicit val codec : Codec[ZipLineMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("time" | uintL(24)) ::
      ("unk1" | bool) ::
      ("unk2" | uintL(2)) ::
      ("unk3" | uint32L) ::
      ("unk4" | uint32L) ::
      ("unk5" | uint32L) ::
      ("unk6" | uint32L)
    ).as[ZipLineMessage]
}
