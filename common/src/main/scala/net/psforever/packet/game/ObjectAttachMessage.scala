// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Change the location of an object - the child - within the inventory system for another object - the parent.
  * (Where the child object was before it was moved is not specified or important.)<br>
  * <br>
  * The data portion of this packet defines a parent object, a child object to-be, and a destination.
  * After the packet is delivered, the child object will be expected to be a possession of the parent object in the codified inventory location.
  * The "inventory" of the parent object is a generalization of that object's containment or installation positions.
  * The inventory is has different referral words for these positions depending on the target parent;
  * but, it is generally "seats" or "mounting points" for vehicles;
  * and, it is generally "holsters" or "grid inventory positions" for players.
  * For players, "holsters" and "grid inventory positions" have 1:1 numerical mapping.
  * For vehicles, however, "seats" and "mounting points" are not consistently mapped and are much more context sensitive.
  * For that reason, this installation position will hitherto be referred to as a generic "slot."<br>
  * <br>
  * Both the client and the server can send and receive this packet.
  * Its interplay with other packets simulate a lazy TCP-like approach to object manipulation.
  * If the client sends this packet, it will generally have already done what it was going to do.
  * If the server sends this packet, the client will have been waiting on confirmation of an action it previously requested.<br>
  * <br>
  * Player inventory slots:<br>
  * `0x80` - 0 - pistol holster 1<br>
  * `0x81` - 1 - pistol holster 2<br>
  * `0x82` - 2 - rifle holster 1<br>
  * `0x83` - 3 - rifle holster 2<br>
  * `0x84` - 4 - knife holster<br>
  * `0x86` - 6 - grid (1,1)<br>
  * `0x00FA` - 250 - is a special dest/extra code that "attaches the item to the player's cursor"
  * @param parent_guid the parent object
  * @param child_guid the child object
  * @param slot a codified location within the parent object's inventory;
  *             8u (0 - 127 or `0x80 - 0xFF`) or 16u (128 - 32767 or `0x0080 - 0x7FFF`)
  */
final case class ObjectAttachMessage(parent_guid : PlanetSideGUID,
                                     child_guid : PlanetSideGUID,
                                     slot : Int)
  extends PlanetSideGamePacket {
  type Packet = ObjectAttachMessage
  def opcode = GamePacketOpcode.ObjectAttachMessage
  def encode = ObjectAttachMessage.encode(this)
}

object ObjectAttachMessage extends Marshallable[ObjectAttachMessage] {
  implicit val codec : Codec[ObjectAttachMessage] = (
    ("parent_guid" | PlanetSideGUID.codec) ::
      ("child_guid" | PlanetSideGUID.codec) ::
      ("slot" | PacketHelpers.encodedStringSize)
    ).as[ObjectAttachMessage]
}
