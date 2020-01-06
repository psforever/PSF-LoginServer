// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when the player's intent is to collect an item from the ground.<br>
  * <br>
  * When a player faces a freed item on the ground in the game world, a prompt appears that invites him to pick it up.
  * Doing so generates this packet.
  * The server determines the exact inventory position where the item will get placed.
  * If the inventory has insufficient space to accommodate the item, it gets put into the player's hand (on the cursor).<br>
  * <br>
  * This packet is complemented by an `ObjectAttachMessage` packet from the server that performs the actual "picking up."
  * @param item_guid na
  * @param player_guid na
  * @param unk1 na
  * @param unk2 na
  */
final case class PickupItemMessage(item_guid : PlanetSideGUID,
                                   player_guid : PlanetSideGUID,
                                   unk1 : Int,
                                   unk2 : Int)
  extends PlanetSideGamePacket {
  type Packet = PickupItemMessage
  def opcode = GamePacketOpcode.PickupItemMessage
  def encode = PickupItemMessage.encode(this)
}

object PickupItemMessage extends Marshallable[PickupItemMessage] {
  implicit val codec : Codec[PickupItemMessage] = (
    ("item_guid" | PlanetSideGUID.codec) ::
      ("player_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint16L)
    ).as[PickupItemMessage]
}
