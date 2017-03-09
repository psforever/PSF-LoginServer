// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when the player wishes to get out of a vehicle.
  * @param player_guid the player
  * @param unk1 na
  * @param unk2 na
  */
final case class DismountVehicleMsg(player_guid : PlanetSideGUID,
                                    unk1 : Int, //maybe, seat number?
                                    unk2 : Boolean) //maybe, bailing?
  extends PlanetSideGamePacket {
  type Packet = DismountVehicleMsg
  def opcode = GamePacketOpcode.DismountVehicleMsg
  def encode = DismountVehicleMsg.encode(this)
}

object DismountVehicleMsg extends Marshallable[DismountVehicleMsg] {
  implicit val codec : Codec[DismountVehicleMsg] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint4L) ::
      ("unk2" | bool)
    ).as[DismountVehicleMsg]
}
