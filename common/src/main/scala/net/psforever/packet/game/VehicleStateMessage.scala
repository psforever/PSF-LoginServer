// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, Vector3}
import scodec.Codec
import scodec.codecs._

//TODO write more thorough comments later.
/**
  * Dispatched to report and update the operational condition of a given vehicle.
  * @param vehicle_guid the vehicle
  * @param unk1 na
  * @param pos the xyz-coordinate location in the world
  * @param ang the orientation of the vehicle
  * @param vel optional movement data
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param wheel_direction for ground vehicles, whether the wheels are being turned;
  *                        15 for straight;
  *                        0 for hard left;
  *                        30 for hard right
  * @param unk5 na
  * @param unk6 na
  * @see `PlacementData`
  */
final case class VehicleStateMessage(vehicle_guid : PlanetSideGUID,
                                     unk1 : Int,
                                     pos : Vector3,
                                     ang : Vector3,
                                     vel : Option[Vector3],
                                     unk2 : Option[Int],
                                     unk3 : Int,
                                     unk4 : Int,
                                     wheel_direction : Int,
                                     unk5 : Boolean,
                                     unk6 : Boolean
                                    ) extends PlanetSideGamePacket {
  type Packet = VehicleStateMessage
  def opcode = GamePacketOpcode.VehicleStateMessage
  def encode = VehicleStateMessage.encode(this)
}

object VehicleStateMessage extends Marshallable[VehicleStateMessage] {
  /**
    * Calculate common orientation from little-endian bit data.
    * @see `Angular.codec_roll`
    * @see `Angular.codec_pitch`
    * @see `Angular.codec_yaw`
    */
  private val codec_orient : Codec[Vector3] = (
    ("roll"    | Angular.codec_roll(10)) ::
      ("pitch" | Angular.codec_pitch(10)) ::
      ("yaw"   | Angular.codec_yaw(10, 90f))
    ).as[Vector3]

  implicit val codec : Codec[VehicleStateMessage] = (
    ("vehicle_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uintL(3)) ::
      ("pos" | Vector3.codec_pos) ::
      ("ang" | codec_orient) ::
      optional(bool, "vel" | Vector3.codec_vel) ::
      optional(bool, "unk2" | uintL(5)) ::
      ("unk3" | uintL(7)) ::
      ("unk4" | uint4L) ::
      ("wheel_direction" | uintL(5)) ::
      ("int5" | bool) ::
      ("int6" | bool)
    ).as[VehicleStateMessage]
}
