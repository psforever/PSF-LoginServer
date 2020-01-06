// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * Update a list of (new) objects that have been detected by this client in one way or another.
  * @param player_guid1 the player
  * @param player_guid2 the player(?);
  *                     often matches with `player_guid1`
  * @param unk na;
  *            commonly, zero
  * @param list list of detected objects;
  *             normally contains at least one element
  */

  /*
    BETA CLIENT DEBUG INFO:
      Detector
      Sender
      Object Count (not really)
      Detected Object[]
   */
final case class ObjectDetectedMessage(player_guid1 : PlanetSideGUID,
                                       player_guid2 : PlanetSideGUID,
                                       unk : Int,
                                       list : List[PlanetSideGUID])
  extends PlanetSideGamePacket {
  type Packet = ObjectDetectedMessage
  def opcode = GamePacketOpcode.ObjectDetectedMessage
  def encode = ObjectDetectedMessage.encode(this)
}

object ObjectDetectedMessage extends Marshallable[ObjectDetectedMessage] {
  implicit val codec : Codec[ObjectDetectedMessage] = (
    ("player_guid1" | PlanetSideGUID.codec) ::
      ("player_guid2" | PlanetSideGUID.codec) ::
      ("unk" | uint8L) ::
      ("list" | listOfN(uintL(6), PlanetSideGUID.codec))
    ).exmap[ObjectDetectedMessage] (
    {
      case g1 :: g2 :: u :: lst :: HNil =>
        Attempt.successful(ObjectDetectedMessage(g1, g2, u, lst))
    },
    {
      case ObjectDetectedMessage(g1, g2, u, lst) =>
        if(lst.size > 63) {
          Attempt.failure(Err(s"too many list elements (max: 63, actual: ${lst.size})"))
        }
        else {
          Attempt.successful(g1 :: g2 :: u :: lst :: HNil)
        }
    }
  )
}
