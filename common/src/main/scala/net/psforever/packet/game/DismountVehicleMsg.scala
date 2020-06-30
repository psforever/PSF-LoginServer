// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import net.psforever.types.{BailType, PlanetSideGUID}

/**
  * Dispatched by the client when the player wishes to get out of a vehicle.
  * @param player_guid the player
  * @param bailType The dismount action e.g. normal dismount, kicked by owner, bailed
  * @param wasKickedByDriver Seems to be true if a passenger was manually kicked by the vehicle owner
  */
final case class DismountVehicleMsg(player_guid: PlanetSideGUID, bailType: BailType.Value, wasKickedByDriver: Boolean)
    extends PlanetSideGamePacket {
  type Packet = DismountVehicleMsg
  def opcode = GamePacketOpcode.DismountVehicleMsg
  def encode = DismountVehicleMsg.encode(this)
}

object DismountVehicleMsg extends Marshallable[DismountVehicleMsg] {
  implicit val codec: Codec[DismountVehicleMsg] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("bailType" | BailType.codec) ::
      ("wasKickedByDriver" | bool)
  ).as[DismountVehicleMsg]
}
