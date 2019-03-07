// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of a class of weapons that can be created using `ObjectCreateMessage` packet data.
  * This data will help construct a "loaded weapon" such as a Suppressor or a Gauss.
  * Common uses include items deposited on the ground and items in another player's visible inventory (holsters).<br>
  * <br>
  * The data for the weapons nests information for the default (current) type of ammunition and number of ammunitions in its magazine(s).
  * This ammunition data essentially is the weapon's magazines as numbered slots.
  * An "expected" number of ammunition slot data can be passed into the class for the purposes of validating input.
  * @param data na;
  *             commonly 8
  * @param fire_mode the current mode of weapon's fire;
  *                  zero-indexed
  * @param ammo data regarding the currently loaded ammunition type(s)
  * @see `AmmoBoxData`
  */
final case class WeaponData(data : CommonFieldData,
                            fire_mode : Int,
                            ammo : List[InternalSlot],
                            unk : Boolean = false
                           ) extends ConstructorData {
  override def bitsize : Long = {
    val dataSize = data.bitsize
    val ammoSize : Long = ammo.foldLeft(0L)(_ + _.bitsize)
    21L + dataSize + ammoSize //11 + 10 (from InventoryData) + ammo
  }
}

object WeaponData extends Marshallable[WeaponData] {
  /**
    * Overloaded constructor for creating `WeaponData` that mandates information about a single type of ammunition.
    * @param unk1 na
    * @param unk2 na
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the ammunition object
    * @return a `WeaponData` object
    */
  def apply(unk1 : Int, unk2 : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : CommonFieldData) : WeaponData = {
    WeaponData(
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
    * Overloaded constructor for creating `WeaponData` that mandates information about the firemode and a single type of ammunition.
    * @param unk1 na
    * @param unk2 na
    * @param fire_mode data regarding the currently loaded ammunition type
    * @param cls the code for the type of object (ammunition) being constructed
    * @param guid the globally unique id assigned to the ammunition
    * @param parentSlot the slot where the ammunition is to be installed in the weapon
    * @param ammo the ammunition object
    * @return a `WeaponData` object
    */
  def apply(unk1 : Int, unk2 : Int, fire_mode : Int, cls : Int, guid : PlanetSideGUID, parentSlot : Int, ammo : CommonFieldData) : WeaponData = {
    WeaponData(
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

  /**
    * Overloaded constructor for creating `WeaponData` with two types of ammunition concurrently loaded.
    * This is a common weapon configuration, especially for vehicle-mounted weaponry.
    * @param unk1 na
    * @param unk2 na
    * @param fire_mode data regarding the currently loaded ammunition type
    * @param cls1 the code for the first type of object (ammunition) being constructed
    * @param guid1 the globally unique id assigned to the first type of ammunition
    * @param slot1 the slot where the first type of ammunition is to be installed in the weapon
    * @param ammo1 the first ammunition object
    * @param cls2 the code for the second type of object (ammunition) being constructed
    * @param guid2 the globally unique id assigned to the second type of ammunition
    * @param slot2 the slot where the second type of ammunition is to be installed in the weapon
    * @param ammo2 the second ammunition object
    * @return a `WeaponData` object
    */
  def apply(unk1 : Int, unk2 : Int, fire_mode : Int, cls1 : Int, guid1 : PlanetSideGUID, slot1 : Int, ammo1 : CommonFieldData, cls2 : Int, guid2 : PlanetSideGUID, slot2 : Int, ammo2 : CommonFieldData) : WeaponData ={
    WeaponData(
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
      List(InternalSlot(cls1, guid1, slot1, ammo1), InternalSlot(cls2, guid2, slot2, ammo2))
    )
  }

  implicit val codec : Codec[WeaponData] = (
    ("data" | CommonFieldData.codec) ::
      ("fire_mode" | int8) ::
      bool ::
      optional(bool, "ammo" | InventoryData.codec) ::
      ("unk" | bool)
    ).exmap[WeaponData] (
    {
      case data :: fmode :: false :: Some(InventoryData(ammo)) :: unk :: HNil =>
        val magSize = ammo.size
        if(magSize == 0) {
          Attempt.failure(Err("weapon must decode some ammunition"))
        }
        else {
          Attempt.successful(WeaponData(data, fmode, ammo, unk))
        }

      case data =>
        Attempt.failure(Err(s"invalid weapon data format - $data"))
    },
    {
      case WeaponData(data, fmode, ammo, unk) =>
        val magSize = ammo.size
        if(magSize == 0) {
          Attempt.failure(Err("weapon must encode some ammunition"))
        }
        else if(magSize >= 255) {
          Attempt.failure(Err("weapon encodes too much ammunition (255+ types!)"))
        }
        else {
          Attempt.successful(data :: fmode :: false :: Some(InventoryData(ammo)) :: unk :: HNil)
        }

      case _ =>
        Attempt.failure(Err("invalid weapon data format"))
    }
  )
}
