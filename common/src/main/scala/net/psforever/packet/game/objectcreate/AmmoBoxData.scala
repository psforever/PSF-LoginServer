// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the ammunition portion of `ObjectCreateMessage` packet data.
  * When alone, this data will help construct a "box" of that type of ammunition, hence the name.<br>
  * <br>
  * Exploration:<br>
  * This class may need to be rewritten later to support objects spawned in the world environment.
  * @param magazine the number of rounds available
  */
case class AmmoBoxData(magazine : Int
                      ) extends ConstructorData {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = 39L
}

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
  )

  /**
    * Transform between AmmoBoxData and ConstructorData.
    */
  val genericCodec : Codec[ConstructorData.genericPattern] = codec.exmap[ConstructorData.genericPattern] (
    {
      case x =>
        Attempt.successful(Some(x.asInstanceOf[ConstructorData]))
    },
    {
      case Some(x) =>
        Attempt.successful(x.asInstanceOf[AmmoBoxData])
      case _ =>
        Attempt.failure(Err(""))
    }
  )
}
