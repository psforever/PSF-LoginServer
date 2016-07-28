// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Create a dispatched game packet that instructs the client to update the user about continents being conquered.
  *
  * This generates the event message "The [empire] have captured [continent_guid]."
  * If the continent_guid is not a valid zone, no message is displayed.
  * If empire is not a valid empire, no message is displayed.
  *
  * @param continent_guid identifies the zone (continent)
  * @param empire identifies the empire;
  *               this value is matchable against PlanetSideEmpire, except for hex `C0` (open)
  */
final case class ContinentalLockUpdateMessage(continent_guid : PlanetSideGUID,
                                              empire : Int) // 00 for TR, 40 for NC, 80 for VS; C0 is used, but it generates no message
  extends PlanetSideGamePacket {
  type Packet = ContinentalLockUpdateMessage
  def opcode = GamePacketOpcode.ContinentalLockUpdateMessage
  def encode = ContinentalLockUpdateMessage.encode(this)
}

object ContinentalLockUpdateMessage extends Marshallable[ContinentalLockUpdateMessage] {
  implicit val codec : Codec[ContinentalLockUpdateMessage] = (
    ("continent_guid" | PlanetSideGUID.codec) ::
      ("empire" | uint8L)
    ).as[ContinentalLockUpdateMessage]
}
