// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of a class of weapons that can be created using `ObjectCreateMessage` packet data.
  * Common uses include items deposited on the ground and items in another player's visible inventory (holsters).
  * @param unk1 na;
  *             commonly 8
  * @param unk2 na;
  *             commonly 12
  * @param fire_mode the current mode of weapon's fire;
  *                  zero-indexed
  * @param ammo data regarding the currently loaded ammunition type
  * @see `WeaponData`
  * @see `AmmoBoxData`
  */
final case class WeaponData(unk1 : Int,
                            unk2 : Int,
                            fire_mode : Int,
                            ammo : InternalSlot
                           ) extends ConstructorData {
  override def bitsize : Long = 44L + ammo.bitsize
}

object WeaponData extends Marshallable[WeaponData] {
  /**
    * An abbreviated constructor for creating `WeaponData` while masking use of `InternalSlot` for its `AmmoBoxData`.
    * @param unk1 na
    * @param unk2 na
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the ammunition object
    * @return a `WeaponData` object
    */
  def apply(unk1 : Int, unk2 : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : AmmoBoxData) : WeaponData =
    new WeaponData(unk1, unk2, 0, InternalSlot(cls, guid, parentSlot, ammo))

  /**
    * An abbreviated constructor for creating `WeaponData` while masking use of `InternalSlot` for its `AmmoBoxData`.
    * @param unk1 na
    * @param unk2 na
    * @param fire_mode data regarding the currently loaded ammunition type
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the ammunition object
    * @return a `WeaponData` object
    */
  def apply(unk1 : Int, unk2 : Int, fire_mode : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : AmmoBoxData) : WeaponData =
    new WeaponData(unk1, unk2, fire_mode, InternalSlot(cls, guid, parentSlot, ammo))

  implicit val codec : Codec[WeaponData] = (
    ("unk1" | uint4L) ::
      ("unk2" | uint4L) ::
      uint(20) ::
      ("fire_mode" | int(3)) ::
      bool ::
      bool ::
      uint8L :: //size = 1 type of ammunition loaded
      uint2 ::
      ("ammo" | InternalSlot.codec) ::
      bool
    ).exmap[WeaponData] (
    {
      case unk1 :: unk2 :: 0 :: fmode :: false :: true :: 1 :: 0 :: ammo :: false :: HNil =>
        Attempt.successful(WeaponData(unk1, unk2, fmode, ammo))
      case _ :: _ :: _ :: _ :: _ :: _ ::  _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid weapon data format"))
    },
    {
      case WeaponData(unk1, unk2, fmode, ammo) =>
        Attempt.successful(unk1 :: unk2 :: 0 :: fmode :: false :: true :: 1 :: 0 :: ammo :: false :: HNil)
    }
  )
}
