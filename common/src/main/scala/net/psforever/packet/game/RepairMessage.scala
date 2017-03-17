// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched to the client to report the amount of repair that is performed upon a target item.
  * The item could be a player or a vehicle or a stationary game object, e.g., a terminal.
  * @param item_guid an item
  * @param repair_value how much the item has been repaired for
  */
final case class RepairMessage(item_guid : PlanetSideGUID,
                               repair_value : Long)
  extends PlanetSideGamePacket {
  type Packet = RepairMessage
  def opcode = GamePacketOpcode.RepairMessage
  def encode = RepairMessage.encode(this)
}

object RepairMessage extends Marshallable[RepairMessage] {
  implicit val codec : Codec[RepairMessage] = (
    ("item_guid" | PlanetSideGUID.codec) ::
      ("repair_value" | uint32L)
    ).as[RepairMessage]
}
