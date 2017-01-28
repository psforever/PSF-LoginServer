// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param zone the zone id
  * @param empire_status change the "Empire Status" text in the Interstellar Map zone description;
  *                      `true` displays the queue availability for the avatar's empire;
  *                      `false` display "Inactive"
  * @param unk na;
  *            always 0?
  * @see `ZonePopulationUpdateMessage`
  */
final case class ZoneInfoMessage(zone : Int,
                                 empire_status : Boolean,
                                 unk : Long)
  extends PlanetSideGamePacket {
  type Packet = ZoneInfoMessage
  def opcode = GamePacketOpcode.ZoneInfoMessage
  def encode = ZoneInfoMessage.encode(this)
}

object ZoneInfoMessage extends Marshallable[ZoneInfoMessage] {
  implicit val codec : Codec[ZoneInfoMessage] = (
    ("zone" | uint16L) ::
      ("empire_status" | bool) ::
      ("unk" | uint32L)
    ).as[ZoneInfoMessage]
}
