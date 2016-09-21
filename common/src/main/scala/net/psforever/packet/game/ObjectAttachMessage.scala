// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
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
  * The client will "attach to the player's cursor" without waiting for the `ObjectAttachMessage` from the server which echoes the destination.
  * The change is observable.
  * Inversely, the client is blocked from detaching the item from "the player's cursor" and putting it back into the inventory on its own.
  * It waits until it receives an `ObjectAttachMessage` in confirmation.<br>
  * <br>
  *  Destination codes:<br>
  * `80` is the first pistol slot<br>
  * `81` is the second pistol slot<br>
  * `82` is the first rifle slot<br>
  * `83` is the second rifle slot<br>
  * `86` is the first entry in the player's inventory<br>
  * `00 FA` is a special dest/extra code that "attaches the object to the player's cursor"
  * @param player_guid the player GUID
  * @param item_guid the item GUID
  * @param dest a codified location within the player's inventory see above
  * @param extra optional; a special kind of item manipulation; the common one is `FA`
  * @see MoveItemMessage
  * @see ObjectAttachMessage
  */
final case class ObjectAttachMessage(player_guid : PlanetSideGUID,
                                     item_guid : PlanetSideGUID,
                                     dest : Int,
                                     extra : Option[Int])
  extends PlanetSideGamePacket {
  type Packet = ObjectAttachMessage
  def opcode = GamePacketOpcode.ObjectAttachMessage
  def encode = ObjectAttachMessage.encode(this)
}

object ObjectAttachMessage extends Marshallable[ObjectAttachMessage] {
  implicit val codec : Codec[ObjectAttachMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("item_guid" | PlanetSideGUID.codec) ::
      (("dest" | uint8L) >>:~ { loc =>
        conditional(loc == 0, "extra" | uint8L).hlist
      })
    ).as[ObjectAttachMessage]
}
