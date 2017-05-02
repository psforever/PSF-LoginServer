// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.codecs.{uint, _}
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of a class of weapons that can be created using `ObjectCreateDetailedMessage` packet data.
  * A "concurrent feed weapon" refers to a weapon system that can chamber multiple types of ammunition simultaneously.
  * This data will help construct a "weapon" such as a Punisher.<br>
  * <br>
  * The data for the weapons nests information for the default (current) type and number of ammunition in its magazine.
  * This ammunition data essentially is the weapon's magazines as numbered slots.
  * @param unk1 na
  * @param unk2 na
  * @param fire_mode the current mode of weapon's fire;
  *                  zero-indexed
  * @param ammo `List` data regarding the currently loaded ammunition types and quantities
  * @see `WeaponData`
  * @see `AmmoBoxData`
  */
final case class ConcurrentFeedWeaponData(unk1 : Int,
                                          unk2 : Int,
                                          fire_mode : Int,
                                          ammo : List[InternalSlot]) extends ConstructorData {
  override def bitsize : Long = {
    var bitsize : Long = 0L
    for(o <- ammo) {
      bitsize += o.bitsize
    }
    44L + bitsize
  }
}

object ConcurrentFeedWeaponData extends Marshallable[ConcurrentFeedWeaponData] {
  /**
    * An abbreviated constructor for creating `ConcurrentFeedWeaponData` while masking use of `InternalSlot` for its `DetailedAmmoBoxData`.<br>
    * <br>
    * Exploration:<br>
    * This class may need to be rewritten later to support objects spawned in the world environment.
    * @param unk1 na
    * @param unk2 na
    * @param fire_mode data regarding the currently loaded ammunition type
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the constructor data for the ammunition
    * @return a DetailedWeaponData object
    */
  def apply(unk1 : Int, unk2 : Int, fire_mode : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : DetailedAmmoBoxData) : ConcurrentFeedWeaponData =
    new ConcurrentFeedWeaponData(unk1, unk2, fire_mode, InternalSlot(cls, guid, parentSlot, ammo) :: Nil)

  implicit val codec : Codec[ConcurrentFeedWeaponData] = (
    ("unk1" | uint4L) ::
      ("unk2" | uint4L) ::
      uint(20) ::
      ("fire_mode" | int(3)) ::
      bool ::
      bool ::
      (uint8L >>:~ { size =>
        uint2L ::
          ("ammo" | PacketHelpers.listOfNSized(size, InternalSlot.codec)) ::
          bool
      })
    ).exmap[ConcurrentFeedWeaponData] (
    {
      case unk1 :: unk2 :: 0 :: fmode :: false :: true :: size :: 0 :: ammo :: false :: HNil =>
        if(size != ammo.size)
          Attempt.failure(Err("weapon encodes wrong number of ammunition"))
        else if(size == 0)
          Attempt.failure(Err("weapon needs to encode at least one type of ammunition"))
        else
          Attempt.successful(ConcurrentFeedWeaponData(unk1, unk2, fmode, ammo))
      case _ :: _ ::  _ :: _ :: _ :: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("invalid weapon data format"))
    },
    {
      case ConcurrentFeedWeaponData(unk1, unk2, fmode, ammo) =>
        val size = ammo.size
        if(size == 0)
          Attempt.failure(Err("weapon needs to encode at least one type of ammunition"))
        else if(size >= 255)
          Attempt.failure(Err("weapon has too much ammunition (255+ types!)"))
        else
          Attempt.successful(unk1 :: unk2 :: 0 :: fmode :: false :: true :: size :: 0 :: ammo :: false :: HNil)
    }
  )
}
