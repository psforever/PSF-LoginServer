// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

//TODO write more thorough comments later.
/**
  * Dispatched to report and update the operational condition of a given battle frame robotics vehicle.
  * @param vehicle_guid the battle frame robotics
  * @param unk1 na
  * @param pos the xyz-coordinate location in the world
  * @param orient the orientation of the vehicle
  * @param vel optional movement data
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param is_crouched the battleframe unit is crouched
  * @param unk6 na
  * @param unk7 na
  * @param unk8 na
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
                                           unk6: Boolean,
                                           unk7: Boolean,
                                           unk8: Int,
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
    ("unk6" | bool) ::
    ("unk7" | bool) ::
    ("unk8" | uint4) ::
    ("unk9" | uint32) ::
    ("unkA" | uint32)
  ).as[FrameVehicleStateMessage]
}
