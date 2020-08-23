// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param player_guid na
  * @param object_guid na
  * @param unk na
  */
final case class ActionCancelMessage(player_guid: PlanetSideGUID, object_guid: PlanetSideGUID, unk: Int)
    extends PlanetSideGamePacket {
  type Packet = ActionCancelMessage
  def opcode = GamePacketOpcode.ActionCancelMessage
  def encode = ActionCancelMessage.encode(this)
}

object ActionCancelMessage extends Marshallable[ActionCancelMessage] {
  implicit val codec: Codec[ActionCancelMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("object_guid" | PlanetSideGUID.codec) ::
      ("unk" | uint4L)
  ).as[ActionCancelMessage]
}
