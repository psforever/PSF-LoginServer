// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Alert that the player wishes to board a vehicle into a specific seat.<br>
  * <br>
  * The client will only dispatch this packet when it feels confident that the player can get into the specific seat on a vehicle.
  * It makes its own check whether or not to display that "enter vehicle here" icon on the ground.
  * Even without that condition, the player is not allowed to do anything until the server responds in affirmation.<br>
  * <br>
  * Base turrets and implant terminals count as "vehicles" for the purpose of mounting.
  * @param player_guid the player
  * @param vehicle_guid the vehicle
  * @param seat the vehicle-specific seat index
  */
final case class MountVehicleMsg(player_guid : PlanetSideGUID,
                                 vehicle_guid : PlanetSideGUID,
                                 seat : Int)
  extends PlanetSideGamePacket {
  type Packet = MountVehicleMsg
  def opcode = GamePacketOpcode.MountVehicleMsg
  def encode = MountVehicleMsg.encode(this)
}

object MountVehicleMsg extends Marshallable[MountVehicleMsg] {
  implicit val codec : Codec[MountVehicleMsg] = (
    ("player_guid" | PlanetSideGUID.codec) ::
      ("vehicle_guid" | PlanetSideGUID.codec) ::
      ("seat" | uint8L)
    ).as[MountVehicleMsg]
}
