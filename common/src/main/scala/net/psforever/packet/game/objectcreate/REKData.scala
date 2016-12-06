// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

case class REKData(unk : Int
                  ) extends ConstructorData {
  override def bitsize : Long = 72L
}

object REKData extends Marshallable[REKData] {
  implicit val codec : Codec[REKData] = (
    ("unk" | uint4L) ::
      uint4L ::
      ignore(20) ::
      uint4L ::
      ignore(16) ::
      uint4L ::
      ignore(20)
    ).exmap[REKData] (
    {
      case code :: 8 :: _ :: 2 :: _ :: 8 :: _ :: HNil =>
        Attempt.successful(REKData(code))
      case _ :: x :: _ :: y :: _ :: z :: _ :: HNil =>
        Attempt.failure(Err("looking for 8-2-8 pattern, found %d-%d-%d".format(x,y,z))) //TODO I actually don't know what of this is actually important
    },
    {
      case REKData(code) =>
        Attempt.successful(code :: 8 :: () :: 2 :: () :: 8 :: () :: HNil)
    }
  ).as[REKData]



  val genericCodec : Codec[ConstructorData.genericPattern] = codec.exmap[ConstructorData.genericPattern] (
    {
      case x =>
        Attempt.successful(Some(x.asInstanceOf[ConstructorData]))
    },
    {
      case Some(x) =>
        Attempt.successful(x.asInstanceOf[REKData])
      case _ =>
        Attempt.failure(Err(""))
    }
  )
}
