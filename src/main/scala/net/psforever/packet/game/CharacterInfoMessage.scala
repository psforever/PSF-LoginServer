// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

case class PlanetSideZoneID(zoneId: Long)

object PlanetSideZoneID {
  implicit val codec = uint32L.as[PlanetSideZoneID]
}

/**
  * Is sent by the PlanetSide world server when sending character selection screen state. Provides metadata
  * about a certain character for rendering purposes (zone background, etc). Acts as an array insert for the
  * client character list. A blank displayed character is most likely caused by either:
  * - a mismatch between an ObjectCreateMessage GUID and the GUID from this message.
  * - bundling CharacterInfoMessage with OCDM, they should appear one after another
  *
  * @param finished True when there are no more characters to give info on
  */
final case class CharacterInfoMessage(
    unk: Long,
    zoneId: PlanetSideZoneID,
    charId: Long,
    guid: PlanetSideGUID,
    finished: Boolean,
    secondsSinceLastLogin: Long
) extends PlanetSideGamePacket {
  type Packet = CharacterInfoMessage
  def opcode = GamePacketOpcode.CharacterInfoMessage
  def encode = CharacterInfoMessage.encode(this)
}

object CharacterInfoMessage extends Marshallable[CharacterInfoMessage] {
  implicit val codec: Codec[CharacterInfoMessage] = (
    ("unk" | uint32L) ::
      ("zoneId" | PlanetSideZoneID.codec) ::
      ("charId" | uint32L) ::
      ("charGUID" | PlanetSideGUID.codec) ::
      ("finished" | bool) ::
      ("seconds_since_last_login" | uint32L)
  ).as[CharacterInfoMessage]
}
