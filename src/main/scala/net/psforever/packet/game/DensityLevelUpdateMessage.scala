// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param zone_id the continent
  * @param building_id the building
  * @param density na
  */
final case class DensityLevelUpdateMessage(zone_id: Int, building_id: Int, density: List[Int])
    extends PlanetSideGamePacket {
  type Packet = DensityLevelUpdateMessage
  def opcode = GamePacketOpcode.DensityLevelUpdateMessage
  def encode = DensityLevelUpdateMessage.encode(this)
}

object DensityLevelUpdateMessage extends Marshallable[DensityLevelUpdateMessage] {
  implicit val codec: Codec[DensityLevelUpdateMessage] = (
    ("zone_id" | uint16L) ::
      ("building_id" | uint16L) ::
      ("density" | PacketHelpers.listOfNSized(8, uint(3)))
  ).exmap[DensityLevelUpdateMessage](
    {
      case a :: b :: c :: HNil =>
        Attempt.Successful(DensityLevelUpdateMessage(a, b, c))
    },
    {
      case DensityLevelUpdateMessage(a, b, c) =>
        if (c.length != 8) {
          Attempt.Failure(Err("list must have 8 entries"))
        } else if (c.count(i => { i < 0 || i > 7 }) > 0) {
          Attempt.Failure(Err("list entries must be 0-7 inclusive"))
        } else {
          Attempt.Successful(a :: b :: c :: HNil)
        }
    }
  )
}
