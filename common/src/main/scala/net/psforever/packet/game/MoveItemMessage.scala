// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

final case class MoveItemMessage(item_guid : PlanetSideGUID,
                                 avatar_guid_1 : PlanetSideGUID,
                                 avatar_guid_2 : PlanetSideGUID,
                                 dest : Int,
                                 unk1 : Int)  // TODO ammo quantity ?
  extends PlanetSideGamePacket {
  type Packet = MoveItemMessage
  def opcode = GamePacketOpcode.MoveItemMessage
  def encode = MoveItemMessage.encode(this)
}

object MoveItemMessage extends Marshallable[MoveItemMessage] {
  implicit val codec : Codec[MoveItemMessage] = (
      ("item_guid" | PlanetSideGUID.codec) ::
        ("avatar_guid_1" | PlanetSideGUID.codec) ::
        ("avatar_guid_2" | PlanetSideGUID.codec) ::
        ("dest" | uint16L) ::
        ("unk1" | uint16L)
    ).as[MoveItemMessage]
}
