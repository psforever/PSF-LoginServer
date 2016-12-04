// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

case class AmmoBoxData(magazine : Int) extends ConstructorData

object AmmoBoxData extends Marshallable[AmmoBoxData] {
  implicit val codec : Codec[AmmoBoxData] = (
    uintL(8) ::
      ignore(15) ::
      ("magazine" | uint16L)
    ).exmap[AmmoBoxData] (
    {
      case 0xC8 :: _ :: mag :: HNil =>
        Attempt.successful(AmmoBoxData(mag))
      case x :: _ :: _ :: HNil =>
        Attempt.failure(Err("looking for 200, found "+x))
    },
    {
      case AmmoBoxData(mag) =>
        Attempt.successful(0xC8 :: () :: mag :: HNil)
    }
  ).as[AmmoBoxData]
}
