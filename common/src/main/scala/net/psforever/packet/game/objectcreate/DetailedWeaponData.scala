// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of a class of weapons that can be created using `ObjectCreateDetailedMessage` packet data.
  * This data will help construct a "loaded weapon" such as a Suppressor or a Gauss.<br>
  * <br>
  * The data for the weapons nests information for the default (current) type and number of ammunition in its magazine.
  * This ammunition data essentially is the weapon's magazines as numbered slots.
  * This format only handles one type of ammunition at a time.
  * Any weapon that has two types of ammunition simultaneously loaded must be handled with another `Codec`.
  * This functionality is unrelated to a weapon that switches ammunition type;
  * a weapon with that behavior is handled perfectly fine using this `case class`.
  * @param unk na
  * @param ammo data regarding the currently loaded ammunition type and quantity
  * @see DetailedAmmoBoxData
  */
final case class DetailedWeaponData(unk : Int,
                                    ammo : InternalSlot) extends ConstructorData {
  override def bitsize : Long = 61L + ammo.bitsize
}

object DetailedWeaponData extends Marshallable[DetailedWeaponData] {
  /**
    * An abbreviated constructor for creating `DetailedWeaponData` while masking use of `InternalSlot` for its `DetailedAmmoBoxData`.
    * @param unk na
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the constructor data for the ammunition
    * @return a DetailedWeaponData object
    */
  def apply(unk : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : DetailedAmmoBoxData) : DetailedWeaponData =
    new DetailedWeaponData(unk, InternalSlot(cls, guid, parentSlot, ammo))

  implicit val codec : Codec[DetailedWeaponData] = (
    ("unk" | uint4L) ::
      uint4L ::
      uint24 ::
      uint16L ::
      uint2 ::
      uint8 :: //size = 1 type of ammunition loaded
      uint2 ::
      ("ammo" | InternalSlot.codec_detailed) ::
      bool
    ).exmap[DetailedWeaponData] (
    {
      case code :: 8 :: 2 :: 0 :: 3 :: 1 :: 0 :: ammo :: false :: HNil =>
        Attempt.successful(DetailedWeaponData(code, ammo))
      case _ :: _ :: _ ::  _ :: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid weapon data format"))
    },
    {
      case DetailedWeaponData(code, ammo) =>
        Attempt.successful(code :: 8 :: 2 :: 0 :: 3 :: 1 :: 0 :: ammo :: false :: HNil)
    }
  )
}
