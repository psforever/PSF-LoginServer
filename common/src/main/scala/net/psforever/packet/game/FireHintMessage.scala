// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * not sure for u1 / u2 / u3, maybe need a real brain ...
  * @param weapon_guid na
  * @param pos na; pos of what ?!
  * @param u1 na
  * @param u2 na
  * @param u3 na
  * @param u4 na
  * @param u5 na; vel of what ?!
  */

final case class FireHintMessage(weapon_guid : PlanetSideGUID,
                                 pos : Vector3,
                                 u1 : Int,
                                 u2 : Int,
                                 u3 : Int,
                                 u4 : Int,
                                 u5 : Option[Vector3] = None)
  extends PlanetSideGamePacket {
  type Packet = FireHintMessage
  def opcode = GamePacketOpcode.FireHintMessage
  def encode = FireHintMessage.encode(this)
}

object FireHintMessage extends Marshallable[FireHintMessage] {

  implicit val codec : Codec[FireHintMessage] = (
    ("weapon_guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("u1" | uint16L) ::
      ("u2" | uint16L) ::
      ("u3" | uint16L) ::
      ("u4" | uintL(3)) ::
      optional(bool, "u5" | Vector3.codec_vel)
    ).as[FireHintMessage]
}
