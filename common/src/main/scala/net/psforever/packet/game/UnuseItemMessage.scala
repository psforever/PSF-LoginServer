// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when its player is done using something.<br>
  * <br>
  * The common example is sifting through backpacks, an activity that only one player is allowed to do at a time.
  * When a backpack is accessed by one player, other players are blocked.
  * When the first player is done accessing the backpack, this packet informs the server so other players may be allowed access.
  * @param player_guid the player
  * @param item_guid the item
  */
final case class UnuseItemMessage(player_guid : PlanetSideGUID,
                                  item_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = UnuseItemMessage
  def opcode = GamePacketOpcode.UnuseItemMessage
  def encode = UnuseItemMessage.encode(this)
}

object UnuseItemMessage extends Marshallable[UnuseItemMessage] {
  implicit val codec : Codec[UnuseItemMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("item_guid" | PlanetSideGUID.codec)
    ).as[UnuseItemMessage]
}
