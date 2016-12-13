// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the REK portion of `ObjectCreateMessage` packet data.
  * This data will help construct the "tool" called a Remote Electronics Kit.<br>
  * <br>
  * Of note is the first portion of the data which resembles the `WeaponData` format.
  * @param unk na
  */
final case class REKData(unk : Int) extends ConstructorData {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = 67L
}

object REKData extends Marshallable[REKData] {
  implicit val codec : Codec[REKData] = (
    ("unk" | uint4L) ::
      uint4L ::
      uintL(20) ::
      uint4L ::
      uint16L ::
      uint4L ::
      uintL(15)
    ).exmap[REKData] (
    {
      case code :: 8 :: 0 :: 2 :: 0 :: 8 :: 0 :: HNil =>
        Attempt.successful(REKData(code))
      case code :: _ :: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid rek data format"))
    },
    {
      case REKData(code) =>
        Attempt.successful(code :: 8 :: 0 :: 2 :: 0 :: 8 :: 0 :: HNil)
    }
  ).as[REKData]

  /**
    * Transform between REKData and ConstructorData.
    */
  val genericCodec : Codec[ConstructorData.genericPattern] = codec.exmap[ConstructorData.genericPattern] (
    {
      case x =>
        Attempt.successful(Some(x.asInstanceOf[ConstructorData]))
    },
    {
      case Some(x) =>
        Attempt.successful(x.asInstanceOf[REKData])
      case _ =>
        Attempt.failure(Err("can not encode rek data"))
    }
  )
}
