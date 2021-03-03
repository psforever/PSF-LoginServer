// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Attempt.Successful
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class DroppodLaunchRequestMessage(
                                              guid: PlanetSideGUID,
                                              zone_number: Int,
                                              xypos: Vector3,
                                              unk: Int
                                            ) extends PlanetSideGamePacket {
  type Packet = DroppodLaunchRequestMessage
  def opcode = GamePacketOpcode.DroppodLaunchRequestMessage
  def encode = DroppodLaunchRequestMessage.encode(this)
}

object DroppodLaunchRequestMessage extends Marshallable[DroppodLaunchRequestMessage] {
  def apply(guid: PlanetSideGUID, zoneNumber: Int, pos: Vector3): DroppodLaunchRequestMessage =
    DroppodLaunchRequestMessage(guid, zoneNumber, pos, 3)

  implicit val codec: Codec[DroppodLaunchRequestMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
    ("zone_number" | uint16L) ::
    (floatL :: floatL).narrow[Vector3](
      {
        case x :: y :: HNil => Successful(Vector3(x, y, 0))
      },
      {
        case Vector3(x, y, _) => x :: y :: HNil
      }
    ) ::
    ("unk" | uint2)
    ).as[DroppodLaunchRequestMessage]
}
