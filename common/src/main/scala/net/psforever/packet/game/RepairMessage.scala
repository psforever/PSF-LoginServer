// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched to the client to report the amount of repair that is performed upon a target item.
  * On the client, a progress bar window is displayed with the appropriate repair type and amount.
  * The item could be a player or a vehicle or a stationary game object, e.g., a terminal.
  * @param item_guid a game object;
  *                  the kind of object influences the kind of repair
  * @param repair_value the percentage of maximum health that the object possesses after repairs;
  *                     as the value is a percentage, it should be from 0 to 100;
  *                     at 100, the progress window does not display anymore;
  *                     above 100, the progress window stays displayed unless the underlying process is interrupted
  */
final case class RepairMessage(item_guid: PlanetSideGUID, repair_value: Long) extends PlanetSideGamePacket {
  type Packet = RepairMessage
  def opcode = GamePacketOpcode.RepairMessage
  def encode = RepairMessage.encode(this)
}

object RepairMessage extends Marshallable[RepairMessage] {
  implicit val codec: Codec[RepairMessage] = (
    ("item_guid" | PlanetSideGUID.codec) ::
      ("repair_value" | uint32L)
  ).as[RepairMessage]
}
