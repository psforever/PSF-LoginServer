// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
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
final case class DetailedWeaponData(
    data: CommonFieldData,
    fire_mode: Int,
    ammo: List[InternalSlot],
    unk: Boolean = false
) extends ConstructorData {
  override def bitsize: Long = {
    val dataSize       = data.bitsize
    val ammoSize: Long = ammo.foldLeft(0L)(_ + _.bitsize)
    38L + dataSize + ammoSize //28 + 10 (from InventoryData) + ammo
  }
}

object DetailedWeaponData extends Marshallable[DetailedWeaponData] {
  implicit val codec: Codec[DetailedWeaponData] = (
    ("data" | CommonFieldData.codec) ::
      ToolPatternData.codec
  ).exmap[DetailedWeaponData](
    {
      case data :: ToolPatternData(_, fmode, _, _, Some(InventoryData(ammoList)), unk) :: HNil =>
        val magSize = ammoList.size
        if (magSize == 0) {
          Attempt.failure(Err("weapon must decode some ammunition"))
        } else {
          Attempt.successful(DetailedWeaponData(data, fmode, ammoList, unk))
        }

      case data =>
        Attempt.failure(Err(s"invalid weapon data format - $data"))
    },
    {
      case DetailedWeaponData(data, fmode, ammo, unk) =>
        val magSize = ammo.size
        if (magSize == 0) {
          Attempt.failure(Err("weapon must encode some ammunition"))
        } else if (magSize >= 255) {
          Attempt.failure(Err("weapon encodes too much ammunition (255+ types!)"))
        } else {
          Attempt.successful(data :: ToolPatternData(u1 = 1, fmode, u3 = false, u4 = true, Some(InventoryData(ammo)), u6 = unk) :: HNil)
        }
    }
  )
}
