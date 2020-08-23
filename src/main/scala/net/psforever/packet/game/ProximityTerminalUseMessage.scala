// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * The player's avatar has moved in relation to a set piece that reacts with the player due to his proximity.<br>
  * <br>
  * Elements that exhibit this behavior include Repair/Rearm Silos in facility courtyards and various cavern crystals.
  * The packets are only dispatched when it is appropriate for the player to be affected.<br>
  * <br>
  * Exploration:<br>
  * Packets where the bytes for the player's GUID are blank exist.
  * @param player_guid the player
  * @param object_guid the object whose functionality is triggered
  * @param unk na
  */
final case class ProximityTerminalUseMessage(player_guid: PlanetSideGUID, object_guid: PlanetSideGUID, unk: Boolean)
    extends PlanetSideGamePacket {
  type Packet = ProximityTerminalUseMessage
  def opcode = GamePacketOpcode.ProximityTerminalUseMessage
  def encode = ProximityTerminalUseMessage.encode(this)
}

object ProximityTerminalUseMessage extends Marshallable[ProximityTerminalUseMessage] {
  implicit val codec: Codec[ProximityTerminalUseMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("object_guid" | PlanetSideGUID.codec) ::
      ("unk" | bool)
  ).as[ProximityTerminalUseMessage]
}
