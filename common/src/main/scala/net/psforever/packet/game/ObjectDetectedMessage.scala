// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * na
  * @param guid1 na
  * @param guid2 na;
  *              often matches with `guid1`
  * @param unk1 na
  * @param unk2 na;
  *             normally contains at least one element
  */
final case class ObjectDetectedMessage(guid1 : PlanetSideGUID,
                                       guid2 : PlanetSideGUID,
                                       unk1 : Int,
                                       unk2 : List[Int])
  extends PlanetSideGamePacket {
  type Packet = ObjectDetectedMessage
  def opcode = GamePacketOpcode.ObjectDetectedMessage
  def encode = ObjectDetectedMessage.encode(this)
}

object ObjectDetectedMessage extends Marshallable[ObjectDetectedMessage] {
  implicit val codec : Codec[ObjectDetectedMessage] = (
    ("guid1" | PlanetSideGUID.codec) ::
      ("guid2" | PlanetSideGUID.codec) ::
      ("unk1" | uint8L) ::
      ("unk2" | listOfN(uintL(6), uint16L)) //TODO are these uids?
    ).exmap[ObjectDetectedMessage] (
    {
      case g1 :: g2 :: u1 :: u2 :: HNil =>
        Attempt.successful(ObjectDetectedMessage(g1, g2, u1, u2))
    },
    {
      case ObjectDetectedMessage(g1, g2, u1, u2) =>
        if(u2.size > 63) {
          Attempt.failure(Err(s"too many list elements (max: 63, actual: ${u2.size})"))
        }
        else {
          Attempt.successful(g1 :: g2 :: u1 :: u2 :: HNil)
        }
    }
  )
}
