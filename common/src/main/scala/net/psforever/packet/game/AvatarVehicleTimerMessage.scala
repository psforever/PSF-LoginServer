// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * @param player_guid player guid !
  * @param text name of the item or vehicle name (ex : medkit, fury ...)
  * @param time in seconds
  * @param unk1 NA - Seems to be false when it's for medkit, true for vehicles
  */
final case class AvatarVehicleTimerMessage(player_guid : PlanetSideGUID,
                                           text : String,
                                           time : Long,
                                           unk1 : Boolean
                                          ) extends PlanetSideGamePacket {
  type Packet = AvatarVehicleTimerMessage
  def opcode = GamePacketOpcode.AvatarVehicleTimerMessage
  def encode = AvatarVehicleTimerMessage.encode(this)
}

object AvatarVehicleTimerMessage extends Marshallable[AvatarVehicleTimerMessage] {
  implicit val codec : Codec[AvatarVehicleTimerMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("text" | PacketHelpers.encodedString) ::
      ("time" | uint32L) ::
      ("unk1" | bool)
    ).as[AvatarVehicleTimerMessage]
}
