// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
 * @param player_guid player guid
 * @param text internal name of the item or vehicle name, e.g., medkit, fury, trhev_antipersonnel
 * @param time cooldown/delay in seconds
 * @param unk unk;
 *            most likely has to do with the visibility of the timer in equipment purchasing;
 *            `false` for kit items;
 *            `true` for almost everything else
  */
final case class AvatarVehicleTimerMessage(player_guid: PlanetSideGUID, text: String, time: Long, unk: Boolean)
    extends PlanetSideGamePacket {
  type Packet = AvatarVehicleTimerMessage
  def opcode = GamePacketOpcode.AvatarVehicleTimerMessage
  def encode = AvatarVehicleTimerMessage.encode(this)
}

object AvatarVehicleTimerMessage extends Marshallable[AvatarVehicleTimerMessage] {
  implicit val codec: Codec[AvatarVehicleTimerMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("text" | PacketHelpers.encodedString) ::
      ("time" | uint32L) ::
      ("unk" | bool)
  ).as[AvatarVehicleTimerMessage]
}
