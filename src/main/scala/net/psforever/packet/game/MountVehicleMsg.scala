// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Alert that the player wishes to board a vehicle at a specific entry point.<br>
  * <br>
  * The client will only dispatch this packet when it feels confident that the player can get into a vehicle.
  * It makes its own check whether or not to display that "enter vehicle here" icon on the ground.
  * This is called an "entry point."
  * Entry points and seat numbers are not required as one-to-one;
  * multiple entry points can lead to the same seat, such as the driver seat of an ANT.<br>
  * <br>
  * The player is not allowed to board anything until the server responds in affirmation.
  * @param player_guid the player
  * @param vehicle_guid the vehicle
  * @param entry_point the entry index that maps to a seat index, specific to the selected vehicle
  */
final case class MountVehicleMsg(player_guid: PlanetSideGUID, vehicle_guid: PlanetSideGUID, entry_point: Int)
    extends PlanetSideGamePacket {
  type Packet = MountVehicleMsg
  def opcode = GamePacketOpcode.MountVehicleMsg
  def encode = MountVehicleMsg.encode(this)
}

object MountVehicleMsg extends Marshallable[MountVehicleMsg] {
  implicit val codec: Codec[MountVehicleMsg] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("vehicle_guid" | PlanetSideGUID.codec) ::
      ("entry_point" | uint8L)
  ).as[MountVehicleMsg]
}
