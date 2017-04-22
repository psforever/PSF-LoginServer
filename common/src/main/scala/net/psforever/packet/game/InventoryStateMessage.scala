// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class InventoryStateMessage(player_guid : PlanetSideGUID,
                                       unk1 : Int,
                                       unk2 : Int,
                                       unk3 : Long)
  extends PlanetSideGamePacket {
  type Packet = InventoryStateMessage
  def opcode = GamePacketOpcode.InventoryStateMessage
  def encode = InventoryStateMessage.encode(this)
}

object InventoryStateMessage extends Marshallable[InventoryStateMessage] {
  implicit val codec : Codec[InventoryStateMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uintL(10)) ::
      ("unk2" | uint16L) ::
      ("unk3" | uint32L)
    ).as[InventoryStateMessage]
}
