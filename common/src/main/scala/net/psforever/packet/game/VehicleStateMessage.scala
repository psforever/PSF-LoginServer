// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

//TODO write more thorough comments later.
/**
  * Dispatched to report and update the operational condition of a given vehicle.
  * @param vehicle_guid the vehicle
  * @param unk1 na
  * @param pos the xyz-coordinate location in the world
  * @param roll the amount of roll that affects orientation;
  *            0.0f is flat to the ground;
  *            roll-right rotation increases angle
  * @param pitch the amount of pitch that affects orientation;
  *            0.0f is flat to the ground;
  *            front-up rotation increases angle
  * @param yaw the amount of yaw that affects orientation;
  *            0.0f is North (before the correction, 0.0f is East);
  *            clockwise rotation increases angle
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
                                     roll : Float,
                                     pitch : Float,
                                     yaw : Float,
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
  implicit val codec : Codec[VehicleStateMessage] = (
    ("vehicle_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uintL(3)) ::
      ("pos" | Vector3.codec_pos) ::
      ("roll" | newcodecs.q_float(0.0f, 360.0f, 10)) ::
      ("pitch" | newcodecs.q_float(360.0f, 0.0f, 10)) ::
      ("yaw" | newcodecs.q_float(360.0f, 0.0f, 10)) ::
      optional(bool, "vel" | Vector3.codec_vel) ::
      optional(bool, "unk2" | uintL(5)) ::
      ("unk3" | uintL(7)) ::
      ("unk4" | uint4L) ::
      ("wheel_direction" | uintL(5)) ::
      ("int5" | bool) ::
      ("int6" | bool)
    ).xmap[VehicleStateMessage] (
    {
      case guid :: u1 :: pos :: roll :: pitch :: yaw :: vel :: u2 :: u3 :: u4 :: wheel :: u5 :: u6 :: HNil =>
        var northCorrectedYaw : Float = yaw + 90f
        if(northCorrectedYaw > 360f) {
          northCorrectedYaw = northCorrectedYaw - 360f
        }
        VehicleStateMessage(guid, u1, pos, roll, pitch, northCorrectedYaw, vel, u2, u3, u4, wheel, u5, u6)
    },

    {
      case VehicleStateMessage(guid, u1, pos, roll, pitch, yaw, vel, u2, u3, u4, wheel, u5, u6) =>
        var northCorrectedYaw : Float = yaw - 90f
        //TODO this invites imprecision
        while(northCorrectedYaw < 0f) {
          northCorrectedYaw = 360f + northCorrectedYaw
        }
        if(northCorrectedYaw > 360f) {
          northCorrectedYaw = northCorrectedYaw % 360f
        }
        guid :: u1 :: pos :: roll :: pitch :: northCorrectedYaw :: vel :: u2 :: u3 :: u4 :: wheel :: u5 :: u6 :: HNil
    }
  )
}
