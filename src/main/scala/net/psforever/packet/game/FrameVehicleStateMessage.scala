// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

//TODO write more thorough comments later.
/**
  * Dispatched to report and update the operational condition of a given battle frame robotics vehicle.
  * @param vehicle_guid the battleframe robotic unit
  * @param unk1 na
  * @param pos the xyz-coordinate location in the world
  * @param orient the orientation of the vehicle
  * @param vel optional movement data
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param is_crouched the battleframe unit is crouched
  * @param is_airborne the battleframe unit is either flying or falling (after flying)
  * @param ascending_flight is the battleframe unit ascending;
  *                         normally reports `ascending_flight` before properly reporting as `is_airborne`;
  *                         continues to report `ascending_flight` until begins falling
  * @param flight_time_remaining a measure of how much longer the battleframe unit, if it can fly, can fly;
  *                              reported as a 0-10 value, counting down from 10 when airborne and provided vertical thrust
  * @param unk9 na
  * @param unkA na
  * @see `PlacementData`
  */
final case class FrameVehicleStateMessage(
                                           vehicle_guid: PlanetSideGUID,
                                           unk1: Int,
                                           pos: Vector3,
                                           orient: Vector3,
                                           vel: Option[Vector3],
                                           unk2: Boolean,
                                           unk3: Int,
                                           unk4: Int,
                                           is_crouched: Boolean,
                                           is_airborne: Boolean,
                                           ascending_flight: Boolean,
                                           flight_time_remaining: Int,
                                           unk9: Long,
                                           unkA: Long
                                         ) extends PlanetSideGamePacket {
  type Packet = FrameVehicleStateMessage
  def opcode = GamePacketOpcode.FrameVehicleStateMessage
  def encode = FrameVehicleStateMessage.encode(this)
}

object FrameVehicleStateMessage extends Marshallable[FrameVehicleStateMessage] {
  /**
    * Calculate common orientation from little-endian bit data.
    * @see `Angular.codec_roll`
    * @see `Angular.codec_pitch`
    * @see `Angular.codec_yaw`
    */
  val codec_orient : Codec[Vector3] = (
    ("roll" | Angular.codec_roll(bits = 10)) ::
    ("pitch" | Angular.codec_pitch(bits = 10)) ::
    ("yaw" | Angular.codec_yaw(bits = 10, North = 90f))
  ).as[Vector3]

  implicit val codec : Codec[FrameVehicleStateMessage] = (
    ("vehicle_guid" | PlanetSideGUID.codec) ::
    ("unk1" | uint(bits = 3)) ::
    ("pos" | Vector3.codec_pos) ::
    ("orient" | codec_orient) ::
    optional(bool, target = "vel" | Vector3.codec_vel) ::
    ("unk2" | bool) ::
    ("unk3" | uint2) ::
    ("unk4" | uint2) ::
    ("is_crouched" | bool) ::
    ("is_airborne" | bool) ::
    ("ascending_flight" | bool) ::
    ("flight_time_remaining" | uint4) ::
    ("unk9" | uint32) ::
    ("unkA" | uint32)
  ).as[FrameVehicleStateMessage]
}
