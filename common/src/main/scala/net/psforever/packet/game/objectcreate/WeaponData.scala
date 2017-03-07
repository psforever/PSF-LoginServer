// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of a class of weapons that can be created using `ObjectCreateMessage` packet data.
  * This data will help construct a "loaded weapon" such as a Suppressor or a Gauss.<br>
  * <br>
  * The data for the weapons nests information for the default (current) type and number of ammunition in its magazine.
  * This ammunition data essentially is the weapon's magazines as numbered slots.
  * Having said that, this format only handles one type of ammunition at a time.
  * Any weapon that has two types of ammunition simultaneously loaded, e.g., a Punisher, must be handled with another `Codec`.
  * This functionality is unrelated to a weapon that switches ammunition type;
  * a weapon with that behavior is handled perfectly fine using this `case class`.
  * @param unk na
  * @param ammo data regarding the currently loaded ammunition type and quantity
  * @see AmmoBoxData
  */
final case class WeaponData(unk : Int,
                            ammo : InternalSlot) extends ConstructorData {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @see AmmoBoxData.bitsize
    * @return the number of bits necessary to represent this object
    */
  override def bitsize : Long = 61L + ammo.bitsize
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
      uint24 ::
      uint16L ::
      uint2 ::
      uint8 :: //size = 1 type of ammunition loaded
      uint2 ::
      ("ammo" | InternalSlot.codec) ::
      bool
    ).exmap[WeaponData] (
    {
      case code :: 8 :: 2 :: 0 :: 3 :: 1 :: 0 :: ammo :: false :: HNil =>
        Attempt.successful(WeaponData(code, ammo))
      case code :: _ :: _ ::  _ :: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid weapon data format"))
    },
    {
      case WeaponData(code, ammo) =>
        Attempt.successful(code :: 8 :: 2 :: 0 :: 3 :: 1 :: 0 :: ammo :: false :: HNil)
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
        Attempt.failure(Err("can not encode weapon data"))
    }
  )
}
