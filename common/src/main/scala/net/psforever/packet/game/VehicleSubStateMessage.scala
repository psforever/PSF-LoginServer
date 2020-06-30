// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client of a driver who is involved in a collision with another vehicle that has no driver.
  * May be supplemented with `GenericCollisionMsg` packets that call out both vehicles.
  * Multiple packets may be generated for a single event representing either
  * entanglement, escalation, or a lack of resolution.
  * @param vehicle_guid the vehicle that was collided with
  * @param player_guid the player who drove into the vehicle
  * @param vehicle_pos the xyz-coordinates of the displaced collision vehicle in reference to the player's client
  * @param vehicle_ang the orientation of the displaced collision vehicle in reference to the player's client
  * @param vel optional velocity (unknown)
  * @param unk1 na
  * @param unk2 na
  */
final case class VehicleSubStateMessage(
    vehicle_guid: PlanetSideGUID,
    player_guid: PlanetSideGUID,
    vehicle_pos: Vector3,
    vehicle_ang: Vector3,
    vel: Option[Vector3],
    unk1: Boolean,
    unk2: Option[List[Boolean]]
) extends PlanetSideGamePacket {
  type Packet = VehicleSubStateMessage
  def opcode = GamePacketOpcode.VehicleSubStateMessage
  def encode = VehicleSubStateMessage.encode(this)
}

object VehicleSubStateMessage extends Marshallable[VehicleSubStateMessage] {
  implicit val codec: Codec[VehicleSubStateMessage] = (
    ("vehicle_guid" | PlanetSideGUID.codec) ::
      ("player_guid" | PlanetSideGUID.codec) ::
      ("vehicle_pos" | Vector3.codec_float) ::
      ("vehicle_ang" | VehicleStateMessage.codec_orient) ::
      optional(bool, "vel" | Vector3.codec_vel) ::
      ("unk1" | bool) ::
      optional(bool, "unk2" | PacketHelpers.listOfNSized(4, bool))
  ).as[VehicleSubStateMessage]
}
