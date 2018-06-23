// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * Not working
  */

final case class FrameVehicleStateMessage(item_guid : PlanetSideGUID,
                                   unk1 : Vector3,
                                          unk2 : Boolean,
                                          unk3 : Int,
                                          unk4 : Int,
                                          unk5 : Boolean,
                                          unk6 : Boolean,
                                          unk7 : Boolean,
                                          unk8 : Int,
                                          unk9 : Long,
                                          unk10 : Long)
  extends PlanetSideGamePacket {
  type Packet = FrameVehicleStateMessage
  def opcode = GamePacketOpcode.FrameVehicleStateMessage
  def encode = FrameVehicleStateMessage.encode(this)
}

object FrameVehicleStateMessage extends Marshallable[FrameVehicleStateMessage] {
  implicit val codec : Codec[FrameVehicleStateMessage] = (
    ("item_guid" | PlanetSideGUID.codec) ::
      ("unk1" | Vector3.codec_pos) ::
      ("unk2" | bool) ::
      ("unk3" | uint2L) ::
      ("unk4" | uint2L) ::
      ("unk5" | bool) ::
      ("unk6" | bool) ::
      ("unk7" | bool) ::
      ("unk8" | uint4L) ::
      ("unk9" | uint32L) ::
      ("unk10" | uint32L)
    ).as[FrameVehicleStateMessage]
}
