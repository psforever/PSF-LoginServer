// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideEmpire
import scodec.Codec
import scodec.codecs._

/**
  * Create a dispatched game packet that instructs the client to update the user about continents that are conquered.<br>
  * <br>
  * This generates the event message "The [empire] have captured [continent]."
  * If the continent_guid is not a valid zone, no message is displayed.
  * If empire is not a valid empire, or refers to the neutral or Black Ops forces, no message is displayed.
  * @param continent_id identifies the zone (continent)
  * @param empire identifies the empire
  */
final case class ContinentalLockUpdateMessage(continent_id: Int, empire: PlanetSideEmpire.Value)
    extends PlanetSideGamePacket {
  type Packet = ContinentalLockUpdateMessage
  def opcode = GamePacketOpcode.ContinentalLockUpdateMessage
  def encode = ContinentalLockUpdateMessage.encode(this)
}

object ContinentalLockUpdateMessage extends Marshallable[ContinentalLockUpdateMessage] {
  implicit val codec: Codec[ContinentalLockUpdateMessage] = (
    ("continent_id" | uint16L) ::
      ("empire" | PlanetSideEmpire.codec)
  ).as[ContinentalLockUpdateMessage]
}
