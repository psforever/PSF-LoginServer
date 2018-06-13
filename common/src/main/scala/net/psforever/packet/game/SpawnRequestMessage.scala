// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param unk1 when defined, na;
  *             non-zero when selecting the sanctuary option from a non-sanctuary continent deployment map
  * @param unk2 when defined, indicates type of spawn point by destination;
  *             0 is nothing;
  *             2 is ams;
  *             6 is towers;
  *             7 is facilities
  * @param unk3 na
  * @param unk4 na
  * @param unk5 when defined, the continent number
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
