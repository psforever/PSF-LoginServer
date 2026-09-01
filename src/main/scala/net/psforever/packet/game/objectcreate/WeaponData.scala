// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
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
final case class WeaponData(data: CommonFieldData, fire_mode: Int, ammo: List[InternalSlot])
    extends ConstructorData {
  override def bitsize: Long = {
    val dataSize       = data.bitsize
    val ammoSize: Long = ammo.foldLeft(0L)(_ + _.bitsize)
    21L + dataSize + ammoSize //11 + 10 (from InventoryData) + ammo
  }
}

object WeaponData extends Marshallable[WeaponData] {
  private def baseCodec(commonFieldCodec: Codec[CommonFieldData]): Codec[WeaponData] = (
    ("data" | commonFieldCodec) ::
    ("fire_mode" | int8) ::
    bool ::
    optional(bool, "ammo" | InventoryData.codec) ::
    ignore(size = 1)
    ).exmap[WeaponData](
    {
      case data :: fmode :: false :: Some(InventoryData(ammo)) :: _ :: HNil =>
        val magSize = ammo.size
        if (magSize == 0) {
          Attempt.failure(Err("weapon must decode some ammunition"))
        } else {
          Attempt.successful(WeaponData(data, fmode, ammo))
        }
      case data :: fmode :: false :: None :: _ :: HNil =>
        //rare pass condition, usually found in LockerContainer objects or temporarily existing as a dropped item
        Attempt.successful(WeaponData(data, fmode, Nil))
      case data =>
        Attempt.failure(Err(s"invalid weapon data format - $data"))
    },
    {
      case WeaponData(data, fmode, ammo) =>
        val magSize = ammo.size
        if (magSize == 0) {
          Attempt.failure(Err("weapon must encode some ammunition"))
        } else if (magSize >= 255) {
          Attempt.failure(Err("weapon encodes too much ammunition (255+ types!)"))
        } else {
          Attempt.successful(data :: fmode :: false :: Some(InventoryData(ammo)) :: () :: HNil)
        }
      case _ =>
        Attempt.failure(Err("invalid weapon data format"))
    }
  )

  implicit val codec: Codec[WeaponData] = baseCodec(CommonFieldData.codec)

  val codec_bfr_arm: Codec[WeaponData] = baseCodec(CommonFieldData.codec_extra)
}
