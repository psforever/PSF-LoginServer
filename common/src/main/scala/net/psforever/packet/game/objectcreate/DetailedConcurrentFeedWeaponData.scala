// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of a class of weapons that can be created using `ObjectCreateDetailedMessage` packet data.
  * A "concurrent feed weapon" refers to a weapon system that can chamber multiple types of ammunition simultaneously.
  * This data will help construct a "weapon" such as a Punisher.<br>
  * <br>
  * The data for the weapons nests information for the default (current) type of ammunition in its magazine.
  * This ammunition data essentially is the weapon's magazines as numbered slots.
  * @param unk1 na
  * @param unk2 na
  * @param ammo `List` data regarding the currently loaded ammunition types and quantities
  * @see DetailedWeaponData
  * @see DetailedAmmoBoxData
  */
final case class DetailedConcurrentFeedWeaponData(unk1 : Int,
                                                  unk2 : Int,
                                                  ammo : List[InternalSlot]) extends ConstructorData {
  override def bitsize : Long = {
    var bitsize : Long = 0L
    for(o <- ammo) {
      bitsize += o.bitsize
    }
    61L + bitsize
  }
}

object DetailedConcurrentFeedWeaponData extends Marshallable[DetailedConcurrentFeedWeaponData] {
  /**
    * An abbreviated constructor for creating `DetailedConcurrentFeedWeaponData` while masking use of `InternalSlot` for its `DetailedAmmoBoxData`.<br>
    * <br>
    * Exploration:<br>
    * This class may need to be rewritten later to support objects spawned in the world environment.
    * @param unk1 na
    * @param unk2 na
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the constructor data for the ammunition
    * @return a DetailedWeaponData object
    */
  def apply(unk1 : Int, unk2 : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : DetailedAmmoBoxData) : DetailedConcurrentFeedWeaponData =
    new DetailedConcurrentFeedWeaponData(unk1, unk2, InternalSlot(cls, guid, parentSlot, ammo) :: Nil)

  implicit val codec : Codec[DetailedConcurrentFeedWeaponData] = (
    ("unk" | uint4L) ::
      uint4L ::
      uint24 ::
      uint16 ::
      uint2L ::
      (uint8L >>:~ { size =>
        uint2L ::
          ("ammo" | PacketHelpers.listOfNSized(size, InternalSlot.codec_detailed)) ::
          bool
      })
    ).exmap[DetailedConcurrentFeedWeaponData] (
    {
      case unk1 :: unk2 :: 2 :: 0 :: 3 :: size :: 0 :: ammo :: false :: HNil =>
        if(size != ammo.size)
          Attempt.failure(Err("weapon encodes wrong number of ammunition"))
        else if(size == 0)
          Attempt.failure(Err("weapon needs to encode at least one type of ammunition"))
        else
          Attempt.successful(DetailedConcurrentFeedWeaponData(unk1, unk2, ammo))
      case _ =>
        Attempt.failure(Err("invalid weapon data format"))
    },
    {
      case DetailedConcurrentFeedWeaponData(unk1, unk2, ammo) =>
        val size = ammo.size
        if(size == 0)
          Attempt.failure(Err("weapon needs to encode at least one type of ammunition"))
        else if(size >= 255)
          Attempt.failure(Err("weapon has too much ammunition (255+ types!)"))
        else
          Attempt.successful(unk1 :: unk2 :: 2 :: 0 :: 3 :: size :: 0 :: ammo :: false :: HNil)
    }
  )
}
