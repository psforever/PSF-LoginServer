// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class ReloadMessage(item_guid : PlanetSideGUID,
                               ammo_clip : Long,
                               unk1 : Int)
  extends PlanetSideGamePacket {
  type Packet = ReloadMessage
  def opcode = GamePacketOpcode.ReloadMessage
  def encode = ReloadMessage.encode(this)
}

object ReloadMessage extends Marshallable[ReloadMessage] {
  implicit val codec : Codec[ReloadMessage] = (
      ("item_guid" | PlanetSideGUID.codec) ::
        ("ammo_clip" | uint32L) ::
        ("unk1" | int32L)
    ).as[ReloadMessage]
}
