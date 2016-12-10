// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the ammunition portion of `ObjectCreateMessage` packet data.
  * This data will help construct a "box" of that type of ammunition when standalone.
  * It can also be constructed directly inside a weapon as its magazine.<br>
  * <br>
  * The maximum amount of ammunition that can be stored in a single box is 65535 units.
  * Regardless of the interface, however, the number will never be fully visible.
  * Only the first three digits or first four digits may be represented.
  * @param magazine the number of rounds available
  * @see WeaponData
  */
case class AmmoBoxData(magazine : Int
                      ) extends ConstructorData {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = 40L
}

object AmmoBoxData extends Marshallable[AmmoBoxData] {
  implicit val codec : Codec[AmmoBoxData] = (
    uintL(8) ::
      uintL(15) ::
      ("magazine" | uint16L) ::
      bool
    ).exmap[AmmoBoxData] (
    {
      case 0xC8 :: 0 :: mag :: false :: HNil =>
        Attempt.successful(AmmoBoxData(mag))
      case a :: b :: _ :: d :: HNil =>
        Attempt.failure(Err("illegal ammunition data format"))
    },
    {
      case AmmoBoxData(mag) =>
        Attempt.successful(0xC8 :: 0 :: mag :: false:: HNil)
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
        Attempt.failure(Err("can not encode ammo box data"))
    }
  )
}
