// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param unk1 na
  * @param unk2 na
  */
final case class AvatarSearchCriteriaMessage(unk1: PlanetSideGUID, unk2: List[Int]) extends PlanetSideGamePacket {
  type Packet = AvatarSearchCriteriaMessage
  def opcode = GamePacketOpcode.AvatarSearchCriteriaMessage
  def encode = AvatarSearchCriteriaMessage.encode(this)
}

object AvatarSearchCriteriaMessage extends Marshallable[AvatarSearchCriteriaMessage] {
  implicit val codec: Codec[AvatarSearchCriteriaMessage] = (
    ("unk1" | PlanetSideGUID.codec) ::
      ("unk2" | PacketHelpers.listOfNSized(6, uint8L))
  ).exmap[AvatarSearchCriteriaMessage](
    {
      case a :: b :: HNil =>
        Attempt.Successful(AvatarSearchCriteriaMessage(a, b))
    },
    {
      case AvatarSearchCriteriaMessage(a, b) =>
        if (b.length != 6) {
          Attempt.Failure(Err("list must have 6 entries"))
        } else if (b.count(i => { i < 0 || i > 255 }) > 0) {
          Attempt.Failure(Err("list entries must be 0-255 inclusive"))
        } else {
          Attempt.Successful(a :: b :: HNil)
        }
    }
  )
}
