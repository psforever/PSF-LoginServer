package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  *
  * @param zone_number The zone number this packet applies to
  * @param flagInfoList The list of LLUs/Monolith units for this zone
  */
final case class CaptureFlagUpdateMessage(zone_number: Int, flagInfoList: List[FlagInfo]) extends PlanetSideGamePacket {
  type Packet = CaptureFlagUpdateMessage
  def opcode = GamePacketOpcode.CaptureFlagUpdateMessage
  def encode = CaptureFlagUpdateMessage.encode(this)
}

/**
  *
  * @param u1 No effect. Faction ID perhaps?
  * @param owner_map_id The mapID of the base the LLU belongs to
  * @param target_map_id The mapID of the base the LLU must be delivered to
  * @param x X map position
  * @param y Y map position
  * @param u6 Time remaining for hack? No effect when modified
  * @param isMonolithUnit Changes the icon on the map to the monolith unit icon
  */
final case class FlagInfo(u1: Int, owner_map_id: Int, target_map_id: Int, x: Float, y: Float, u6: Long, isMonolithUnit: Boolean)
object FlagInfo extends Marshallable[FlagInfo] {
  implicit val codec: Codec[FlagInfo] = {
    (("u1" | uint2L)
    :: ("owner_map_id" | uint16L)
    :: ("target_map_id" | uint16L)
    :: ("u4" | newcodecs.q_float(0.0, 8192.0, 20))
    :: ("u5" | newcodecs.q_float(0.0, 8192.0, 20))
    :: ("u6" | uint32L)
    :: ("isMonolithUnit" | bool))
  }.as[FlagInfo]
}

object CaptureFlagUpdateMessage extends Marshallable[CaptureFlagUpdateMessage] {
  implicit val codec: Codec[CaptureFlagUpdateMessage] = (
    ("zone_number" | uint16L)
    :: ("flagInfoList" | PacketHelpers.listOfNAligned(longL(4), alignment = 0, FlagInfo.codec)) // Maximum of 7 on any map
    ).xmap[CaptureFlagUpdateMessage] (
      {
        case zone_number :: flagInfoList :: HNil =>
          CaptureFlagUpdateMessage(zone_number, flagInfoList)
      },
      {
        case CaptureFlagUpdateMessage(zone_number, flagInfoList) =>
          zone_number :: flagInfoList :: HNil
      }
    )
}
