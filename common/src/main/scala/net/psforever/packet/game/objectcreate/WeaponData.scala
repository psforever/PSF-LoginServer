// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

case class WeaponData(unk : Int,
                      ammo : InternalSlot) extends ConstructorData {
  override def bsize : Long = 59L + ammo.bsize
}

object WeaponData extends Marshallable[WeaponData] {
  def apply(unk : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : AmmoBoxData) : WeaponData =
    new WeaponData(unk, InternalSlot(cls, guid, parentSlot, Some(ammo)))

  implicit val codec : Codec[WeaponData] = (
    ("unk" | uint4L) ::
      uint4L ::
      ignore(20) ::
      uint4L ::
      ignore(16) ::
      uintL(11) ::
      ("ammo" | InternalSlot.codec)
    ).exmap[WeaponData] (
    {
      case code :: 8 :: _ :: 2 :: _ :: 0x2C0 :: ammo :: HNil =>
        Attempt.successful(WeaponData(code, ammo))
      case _ :: x :: _ ::  y :: _ :: z :: _ :: HNil =>
        Attempt.failure(Err("looking for 8-2-704 pattern, found %d-%d-%d".format(x,y,z))) //TODO I actually don't know what of this is actually important
    },
    {
      case WeaponData(code, ammo) =>
        Attempt.successful(code :: 8 :: () :: 2 :: () :: 0x2C0 :: ammo :: HNil)
    }
  ).as[WeaponData]



  val genericCodec : Codec[ConstructorData.genericPattern] = codec.exmap[ConstructorData.genericPattern] (
    {
      case x =>
        Attempt.successful(Some(x.asInstanceOf[ConstructorData]))
    },
    {
      case Some(x) =>
        Attempt.successful(x.asInstanceOf[WeaponData])
      case _ =>
        Attempt.failure(Err(""))
    }
  )
}
