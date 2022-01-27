// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  */
final case class GenericObjectActionAtPositionMessage(
                                                       object_guid: PlanetSideGUID,
                                                       code: Int,
                                                       pos: Vector3
                                                     ) extends PlanetSideGamePacket {
  type Packet = GenericObjectActionAtPositionMessage
  def opcode = GamePacketOpcode.GenericObjectActionAtPositionMessage
  def encode = GenericObjectActionAtPositionMessage.encode(this)
}

object GenericObjectActionAtPositionMessage extends Marshallable[GenericObjectActionAtPositionMessage] {
  implicit val codec: Codec[GenericObjectActionAtPositionMessage] = (
    ("object_guid" | PlanetSideGUID.codec) ::
    ("code" | uint(bits = 8)) ::
    ("pos" | Vector3.codec_pos)
    ).exmap[GenericObjectActionAtPositionMessage](
    {
      case guid :: code :: pos :: HNil =>
        Attempt.Successful(GenericObjectActionAtPositionMessage(guid, code, pos))
    },
    {
      case GenericObjectActionAtPositionMessage(guid, code, pos) =>
        Attempt.Successful(guid :: code :: pos :: HNil)
    }
  )
}
