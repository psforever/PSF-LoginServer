// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  *
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param unk5 continent?
  */
final case class SpawnRequestMessage(unk1 : Int,
                                     unk2 : Long,
                                     unk3 : Int,
                                     unk4 : Int,
                                     unk5 : Int)
  extends PlanetSideGamePacket {
  type Packet = SpawnRequestMessage
  def opcode = GamePacketOpcode.SpawnRequestMessage
  def encode = SpawnRequestMessage.encode(this)
}

object SpawnRequestMessage extends Marshallable[SpawnRequestMessage] {
  implicit val codec : Codec[SpawnRequestMessage] = (
    ("unk1" | uint16L) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint16L) ::
      ("unk4" | uint16L) ::
      ("unk5" | uintL(10))
    ).as[SpawnRequestMessage]
}
