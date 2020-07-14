// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param player_guid na
  * @param vehicle_guid vehicle ?, turret ? Found a HART GUID for now. Need more search.
  * @param u1 na - maybe a delay ?
  * @param u2 na
  */
final case class DelayedPathMountMsg(player_guid: PlanetSideGUID, vehicle_guid: PlanetSideGUID, u1: Int, u2: Boolean)
    extends PlanetSideGamePacket {
  type Packet = DelayedPathMountMsg
  def opcode = GamePacketOpcode.DelayedPathMountMsg
  def encode = DelayedPathMountMsg.encode(this)
}

object DelayedPathMountMsg extends Marshallable[DelayedPathMountMsg] {
  implicit val codec: Codec[DelayedPathMountMsg] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("vehicle_guid" | PlanetSideGUID.codec) ::
      ("u1" | uint8L) ::
      ("u2" | bool)
  ).as[DelayedPathMountMsg]
}
