// Copyright (c) 2026 PSForever
package net.psforever.packet.game.packets

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.types.Vector3
import scodec.{Attempt, Codec}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

sealed abstract class ModuleIcon(val value: Int) extends IntEnumEntry

object ModuleIcon extends IntEnum[ModuleIcon] {
  val values: IndexedSeq[ModuleIcon] = findValues

  case object NonPowered extends ModuleIcon(value = 1)
  case object Speed extends ModuleIcon(value = 2)
  case object Defender extends ModuleIcon(value = 3)
  case object Vehicle extends ModuleIcon(value = 4)
  case object Weapon extends ModuleIcon(value = 5)
  case object Healing extends ModuleIcon(value = 6)
  case object Pain extends ModuleIcon(value = 7)

  case object TR extends ModuleIcon(value = 10)
  case object NC extends ModuleIcon(value = 11)
  case object VS extends ModuleIcon(value = 12)

  implicit val codec: Codec[ModuleIcon] = PacketHelpers.createIntEnumCodec(ModuleIcon, uint4)
}

/**
 * Whether the vanu module icon on the continental map alternates red or green in between displays of it's distinguishing color.
 * There are four values - 0 to 3 - but 3 is the only green color and all of the other values result in red.
 * @param value charge state as an integer value
 */
sealed abstract class ModuleIconPulseColor(val value: Int) extends IntEnumEntry

object ModuleIconPulseColor extends IntEnum[ModuleIconPulseColor] {
  val values: IndexedSeq[ModuleIconPulseColor] = findValues

  /** Red module */
  case object Red extends ModuleIconPulseColor(value = 0)
  /** Green module */
  case object Green extends ModuleIconPulseColor(value = 3)

  implicit val codec: Codec[ModuleIconPulseColor] = uint2.xmap(
    {
      case 3 => Green
      case _ => Red
    },
    state => state.value
  )
}

/**
 * na
 * When `u4 = true` and `u5 = false`, the icons do not display on the continental map.
 * All other value combinations display correctly, though there's no apparent distinction between those combinations.
 * @param module_type na
 * @param pulse na
 * @param u3 na
 * @param u4 na
 * @param u5 na
 * @param pos_xy na
 */
final case class ModuleInfo(module_type: ModuleIcon, pulse: ModuleIconPulseColor, u3: Long, u4: Boolean, u5: Boolean, pos_xy: Vector3)

object ModuleInfo {
  def apply(module_type: ModuleIcon, pulseGreen: Boolean, u3: Long, pos_xy: Vector3): ModuleInfo = {
    val intCharge: ModuleIconPulseColor = if (pulseGreen) ModuleIconPulseColor.Green else ModuleIconPulseColor.Red
    new ModuleInfo(module_type, intCharge, u3, u4 = false, u5 = false, pos_xy)
  }
}

/**
 * na
 * @param zone_number zone's number
 * @param info list of module information for this zone
 */
case class VanuModuleUpdateMessage(
                                    zone_number: Int,
                                    info: List[ModuleInfo]
                                  ) extends PlanetSideGamePacket {
  type Packet = VanuModuleUpdateMessage
  def opcode: Type = GamePacketOpcode.VanuModuleUpdateMessage
  def encode: Attempt[BitVector] = VanuModuleUpdateMessage.encode(this)
}

object VanuModuleUpdateMessage extends Marshallable[VanuModuleUpdateMessage] {
  private val moduleInfoCodec: Codec[ModuleInfo] = (
    ("module_type" | ModuleIcon.codec) ::
      ("pulse" | ModuleIconPulseColor.codec) ::
      ("u3" | uint32L) ::
      ("u4" | bool) ::
      ("u5" | bool) ::
      newcodecs.q_float(min = 0.0, max = 8192.0, bits = 20) ::
      newcodecs.q_float(min = 0.0, max = 8192.0, bits = 20)
  ).xmap[ModuleInfo](
    {
      case module_type :: pulse :: u3 :: u4 :: u5 :: x :: y :: HNil =>
        ModuleInfo(module_type, pulse, u3, u4, u5, Vector3(x, y, 0f))
    },
    {
      case ModuleInfo(module_type, pulse, u3, u4, u5, Vector3(x, y, _)) =>
        module_type :: pulse :: u3 :: u4 :: u5 :: x :: y :: HNil
    }
  )

  implicit val codec: Codec[VanuModuleUpdateMessage] = (
    ("zone_number" | uint16L) ::
      ("info" | listOfN(uint(bits = 6), moduleInfoCodec))
    ).as[VanuModuleUpdateMessage]
}
