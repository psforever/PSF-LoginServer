// Copyright (c) 2024 PSForever
package net.psforever.packet.game

import enumeratum.values.{IntEnum, IntEnumEntry}
import shapeless.{::, HNil}
import net.psforever.newcodecs.newcodecs
import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._

sealed abstract class UplinkRequestType(val value: Int) extends IntEnumEntry

object UplinkRequestType extends IntEnum[UplinkRequestType] {
  val values: IndexedSeq[UplinkRequestType] = findValues

  case object RevealFriendlies extends UplinkRequestType(value = 0)

  case object RevealEnemies extends UplinkRequestType(value = 1)

  case object Unknown2 extends UplinkRequestType(value = 2)

  case object ElectroMagneticPulse extends UplinkRequestType(value = 3)

  case object OrbitalStrike extends UplinkRequestType(value = 4)

  implicit val codec: Codec[UplinkRequestType] = PacketHelpers.createIntEnumCodec(this, uint4)
}

final case class UplinkRequest(
                                uplinkType: UplinkRequestType,
                                pos: Option[Vector3],
                                unk: Boolean
                              ) extends PlanetSideGamePacket {
  type Packet = UplinkRequest
  def opcode: Type = GamePacketOpcode.UplinkRequest
  def encode: Attempt[BitVector] = UplinkRequest.encode(this)
}

object UplinkRequest extends Marshallable[UplinkRequest] {
  private val xyToVector3: Codec[Vector3] =
    (newcodecs.q_float(0.0, 8192.0, 20) ::
      newcodecs.q_float(0.0, 8192.0, 20)).xmap[Vector3](
      {
        case x :: y :: HNil => Vector3(x, y, 0f)
      },
      {
        case Vector3(x, y, _) => x :: y :: HNil
      }
    )

  implicit val codec: Codec[UplinkRequest] = (
    ("uplinkType" | UplinkRequestType.codec) >>:~ { uplinkType =>
      conditional(uplinkType == UplinkRequestType.OrbitalStrike, xyToVector3) ::
        ("unk" | bool)
    }).as[UplinkRequest]
}
