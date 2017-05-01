// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * For completion's sake.
  * We've never actually sent or received this packet during session captures on Gemini Live.
  * @param guid na
  * @param unk1 na
  * @param pos na
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param unk5 na
  */
final case class DeployObjectMessage(guid : PlanetSideGUID,
                                     unk1 : Long,
                                     pos : Vector3,
                                     unk2 : Int,
                                     unk3 : Int,
                                     unk4 : Int,
                                     unk5 : Long)
  extends PlanetSideGamePacket {
  type Packet = DeployObjectMessage
  def opcode = GamePacketOpcode.DeployObjectMessage
  def encode = DeployObjectMessage.encode(this)
}

object DeployObjectMessage extends Marshallable[DeployObjectMessage] {
  implicit val codec : Codec[DeployObjectMessage] = (
    ("guid1" | PlanetSideGUID.codec) ::
      ("unk1" | uint32L) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint8L) ::
      ("unk4" | uint8L) ::
      ("unk5" | uint32L)
    ).as[DeployObjectMessage]
}
