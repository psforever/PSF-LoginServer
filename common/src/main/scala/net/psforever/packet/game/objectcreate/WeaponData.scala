// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the weapon portion of `ObjectCreateMessage` packet data.
  * When alone, this data will help construct a "weapon" such as Suppressor.<br>
  * <br>
  * The data for the weapon also nests required default ammunition data.
  * Where the ammunition is loaded is considered the "first slot."
  * @param unk na
  * @param ammo data regarding the currently loaded ammunition type and quantity
  * @see AmmoBoxData
  */
case class WeaponData(unk : Int,
                      ammo : InternalSlot) extends ConstructorData {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @see AmmoBoxData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = 59L + ammo.bitsize
}

object WeaponData extends Marshallable[WeaponData] {
  /**
    * An abbreviated constructor for creating `WeaponData` while masking use of `InternalSlot` for its `AmmoBoxData`.<br>
    * <br>
    * Exploration:<br>
    * This class may need to be rewritten later to support objects spawned in the world environment.
    * @param unk na
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the constructor data for the ammunition
    * @return a WeaponData object
    */
  def apply(unk : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : AmmoBoxData) : WeaponData =
    new WeaponData(unk, InternalSlot(cls, guid, parentSlot, ammo))

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

  /**
    * Transform between WeaponData and ConstructorData.
    */
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
