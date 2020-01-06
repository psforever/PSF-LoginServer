// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of a class of weapons that can be created using `ObjectCreateDetailedMessage` packet data.
  * This data will help construct a "loaded weapon" such as a Suppressor or a Gauss.<br>
  * <br>
  * The data for the weapons nests information for the default (current) type and number of ammunition in its magazine.
  * This ammunition data essentially is the weapon's magazines as numbered slots.
  * An "expected" number of ammunition slot data can be passed into the function.
  * @param data field common to multiple game objects
  * @param fire_mode the current fire mode
  * @param ammo data regarding the currently loaded ammunition type(s) and quantity(ies)
  * @see `DetailedAmmoBoxData`
  * @see `WeaponData`
  */
final case class DetailedWeaponData(data : CommonFieldData,
                                    fire_mode : Int,
                                    ammo : List[InternalSlot],
                                    unk : Boolean = false
                                   ) extends ConstructorData {
  override def bitsize : Long = {
    val dataSize = data.bitsize
    val ammoSize : Long = ammo.foldLeft(0L)(_ + _.bitsize)
    38L + dataSize + ammoSize //28 + 10 (from InventoryData) + ammo
  }
}

object DetailedWeaponData extends Marshallable[DetailedWeaponData] {
  /**
    * Overloaded constructor for creating `DetailedWeaponData` while masking use of `InternalSlot` for its `DetailedAmmoBoxData`.
    * @param unk1 na
    * @param unk2 na
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the constructor data for the ammunition
    * @return a `DetailedWeaponData` object
    */
  def apply(unk1 : Int, unk2 : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : DetailedAmmoBoxData) : DetailedWeaponData = {
    DetailedWeaponData(
      CommonFieldData(
        PlanetSideEmpire(unk1 & 3),
        false,
        false,
        (unk2 & 8) == 8,
        None,
        (unk2 & 4) == 4,
        None,
        None,
        PlanetSideGUID(0)
      ),
      0,
      List(InternalSlot(cls, guid, parentSlot, ammo))
    )
  }


  /**
    * Overloaded constructor for creating `DetailedWeaponData` while masking use of `InternalSlot` for its `DetailedAmmoBoxData`.
    * @param unk1 na
    * @param unk2 na
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the constructor data for the ammunition
    * @return a `DetailedWeaponData` object
    */
  def apply(unk1 : Int, unk2 : Int, fire_mode : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : DetailedAmmoBoxData) : DetailedWeaponData = {
    DetailedWeaponData(
      CommonFieldData(
        PlanetSideEmpire(unk1 & 3),
        false,
        false,
        (unk2 & 8) == 8,
        None,
        (unk2 & 4) == 4,
        None,
        None,
        PlanetSideGUID(0)
      ),
      fire_mode,
      List(InternalSlot(cls, guid, parentSlot, ammo))
    )
  }

  implicit val codec : Codec[DetailedWeaponData] = (
    ("data" | CommonFieldData.codec) ::
      uint8 ::
      uint8 ::
      ("fire_mode" | uint8) ::
      uint2 ::
      optional(bool, "ammo" | InventoryData.codec_detailed) ::
      ("unk" | bool)
    ).exmap[DetailedWeaponData] (
    {
      case data :: 1 :: 0 :: fmode :: 1 :: Some(InventoryData(ammo)) :: unk :: HNil =>
        val magSize = ammo.size
        if(magSize == 0) {
          Attempt.failure(Err("weapon must decode some ammunition"))
        }
        else {
          Attempt.successful(DetailedWeaponData(data, fmode, ammo, unk))
        }

      case data =>
        Attempt.failure(Err(s"invalid weapon data format - $data"))
    },
    {
      case DetailedWeaponData(data, fmode, ammo, unk) =>
        val magSize = ammo.size
        if(magSize == 0) {
          Attempt.failure(Err("weapon must encode some ammunition"))
        }
        else if(magSize >= 255) {
          Attempt.failure(Err("weapon encodes too much ammunition (255+ types!)"))
        }
        else {
          Attempt.successful(data :: 1 :: 0 :: fmode :: 1 :: Some(InventoryData(ammo)) :: unk :: HNil)
        }
    }
  )
}
