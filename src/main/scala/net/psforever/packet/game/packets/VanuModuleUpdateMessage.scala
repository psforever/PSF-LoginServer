// Copyright (c) 2026 PSForever
package net.psforever.packet.game.packets

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.types.Vector3
import scodec.{Attempt, Codec}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

/**
 * Whether the vanu module icon on the continental map alternates red or green in between displays of it's distinguishing color.
 * There are four values - 0 to 3 - but 3 is the only green color and all of the other values result in red.
 * @param value charge state as an integer value
 */
sealed abstract class PulseState(val value: Int) extends IntEnumEntry

object PulseState extends IntEnum[PulseState] {
  val values: IndexedSeq[PulseState] = findValues

  /** Red module */
  case object Red extends PulseState(value = 0)
  /** Green module */
  case object Green extends PulseState(value = 3)

  implicit val codec: Codec[PulseState] = uint(bits = 2).xmap(
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
final case class ModuleInfo(module_type: Int, pulse: PulseState, u3: Long, u4: Boolean, u5: Boolean, pos_xy: Vector3)

object ModuleInfo {
  def apply(module_type: Int, pulseGreen: Boolean, u3: Long, pos_xy: Vector3): ModuleInfo = {
    val intCharge: PulseState = if (pulseGreen) PulseState.Green else PulseState.Red
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
    ("module_type" | uint4) ::
      ("pulse" | PulseState.codec) ::
      ("u3" | uint32L) ::
      ("u4" | bool) ::
      ("u5" | bool) ::
      newcodecs.q_float(min = 0.0, max = 8192.0, bits = 20) ::
      newcodecs.q_float(min = 0.0, max = 8192.0, bits = 20)
  ).xmap[ModuleInfo](
    {
      case module_type :: charged :: u3 :: u4 :: u5 :: x :: y :: HNil =>
        ModuleInfo(module_type, charged, u3, u4, u5, Vector3(x, y, 0f))
    },
    {
      case ModuleInfo(module_type, charged, u3, u4, u5, Vector3(x, y, _)) =>
        module_type :: charged :: u3 :: u4 :: u5 :: x :: y :: HNil
    }
  )

  implicit val codec: Codec[VanuModuleUpdateMessage] = (
    ("zone_number" | uint16L) ::
      ("info" | listOfN(uint(bits = 6), moduleInfoCodec))
    ).as[VanuModuleUpdateMessage]
}
