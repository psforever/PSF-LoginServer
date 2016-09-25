// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Alert that the player is "dismounting" a building.<br>
  * <br>
  * Paragraph in which "'dismounting' a building" is explained.
  * @param player_guid the player
  * @param building_guid the building being "dismounted"
  */
final case class DismountBuildingMsg(player_guid : PlanetSideGUID,
                                     building_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = DismountBuildingMsg
  def opcode = GamePacketOpcode.DismountBuildingMsg
  def encode = DismountBuildingMsg.encode(this)
}

object DismountBuildingMsg extends Marshallable[DismountBuildingMsg] {
  implicit val codec : Codec[DismountBuildingMsg] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("building_guid" | PlanetSideGUID.codec)
    ).as[DismountBuildingMsg]
}
