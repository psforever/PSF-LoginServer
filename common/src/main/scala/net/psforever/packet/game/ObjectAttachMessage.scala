// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Change the location of an item within the game's inventory system.<br>
  * <br>
  * The data portion of this packet defines a player, an item, and a destination.
  * After the packet is received by the client, the item will be guaranteed to be within the player's inventory in the codified location.
  * The "inventory" in this case includes both the player's literal grid inventory and their available equipment slots.
  * Where the item was before it was moved is not specified.<br>
  * <br>
  * This packet is a complementary packet that simulates a lazy TCP-like approach to coordinating item manipulation.
  * In some cases, the client will (appear to) proceed with what it intended to do without waiting for the server to confirm.
  * For example, grabbing an item from an inventory position will generate a `MoveItemMessage` that defines "the player's cursor" as a destination.
  * The client will "attach to the player's cursor" without waiting for an `ObjectAttachMessage` from the server which echoes the destination.
  * The change is observable.
  * Inversely, the client is blocked from detaching the item from "the player's cursor" and putting it back into the inventory on its own.
  * It waits until it receives an `ObjectAttachMessage` in confirmation.<br>
  * <br>
  *  Destination codes:<br>
  * `0x80` - pistol slot 1<br>
  * `0x81` - pistol slot 2<br>
  * `0x82` - rifle slot 1<br>
  * `0x83` - rifle slot 2<br>
  * `0x84` - melee/knife slot<br>
  * `0x85` - mystery slot<br>
  * `0x86` - grid inventory (1,1)<br>
  * `0x00FA` is a special dest/extra code that "attaches the item to the player's cursor"
  * @param player_guid the player
  * @param item_guid the item
  * @param slot a codified location within an inventory, and overlapping the player's holsters if need be;
  *             8u (0 - 127 or `0x80 - 0xFF`) or
  *             16u (128 - 32767 or `0x0080 - 0x7FFF`)
  * @see `MoveItemMessage`, `objectcreate\ObjectClass.SLOT_BLOCKER`
  */
final case class ObjectAttachMessage(player_guid : PlanetSideGUID,
                                     item_guid : PlanetSideGUID,
                                     slot : Int)
  extends PlanetSideGamePacket {
  type Packet = ObjectAttachMessage
  def opcode = GamePacketOpcode.ObjectAttachMessage
  def encode = ObjectAttachMessage.encode(this)
}

object ObjectAttachMessage extends Marshallable[ObjectAttachMessage] {
  implicit val codec : Codec[ObjectAttachMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("item_guid" | PlanetSideGUID.codec) ::
      ("slot" | PacketHelpers.encodedStringSize)
    ).as[ObjectAttachMessage]
}
