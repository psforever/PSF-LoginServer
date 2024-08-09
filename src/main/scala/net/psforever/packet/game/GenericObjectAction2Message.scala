// Copyright (c) 2024 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec}

/**
 * na
 * @param unk na
 * @param guid1 na
 * @param guid2 na
 */
final case class GenericObjectAction2Message(
                                              unk: Int,
                                              guid1: PlanetSideGUID,
                                              guid2: PlanetSideGUID
                                            ) extends PlanetSideGamePacket {
  type Packet = GenericObjectActionMessage
  def opcode: Type = GamePacketOpcode.GenericObjectAction2Message
  def encode: Attempt[BitVector] = GenericObjectAction2Message.encode(this)
}

object GenericObjectAction2Message extends Marshallable[GenericObjectAction2Message] {
  implicit val codec: Codec[GenericObjectAction2Message] = (
    ("unk" | uint(bits = 3)) :: //dword_D32FC0
      ("guid1" | PlanetSideGUID.codec) ::
      ("guid2" | PlanetSideGUID.codec)
    ).as[GenericObjectAction2Message]
}
