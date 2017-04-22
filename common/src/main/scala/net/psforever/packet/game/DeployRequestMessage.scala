// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

final case class DeployRequestMessage(guid1 : PlanetSideGUID,
                                      guid2 : PlanetSideGUID,
                                      unk1 : Int,
                                      unk2 : Int,
                                      unk3 : Boolean,
                                      pos : Vector3)
  extends PlanetSideGamePacket {
  type Packet = DeployRequestMessage
  def opcode = GamePacketOpcode.DeployRequestMessage
  def encode = DeployRequestMessage.encode(this)
}

object DeployRequestMessage extends Marshallable[DeployRequestMessage] {
  implicit val codec : Codec[DeployRequestMessage] = (
    ("guid1" | PlanetSideGUID.codec) ::
      ("guid2" | PlanetSideGUID.codec) ::
      ("unk1" | uint(3)) ::
      ("unk2" | uint(5)) ::
      ("unk3" | bool) ::
      ("pos" | Vector3.codec_pos)
    ).as[DeployRequestMessage]
}
