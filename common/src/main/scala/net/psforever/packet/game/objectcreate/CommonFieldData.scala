// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Data that is common to a number of game object serializations.
  * @param pos where and how the object is oriented
  * @param faction association of the object with
  * @param unk na
  * @param player_guid the player who placed/leverages/[action]s this object
  */
final case class CommonFieldData(pos : PlacementData,
                                 faction : PlanetSideEmpire.Value,
                                 unk : Int,
                                 player_guid : PlanetSideGUID
                                  ) extends StreamBitSize {
  override def bitsize : Long = 23L + pos.bitsize
}

object CommonFieldData extends Marshallable[CommonFieldData] {
  final val internalWeapon_bitsize : Long = 10

  /**
    * Overloaded constructor that eliminates the need to list the fourth, optional, GUID field.
    * @param pos where and how the object is oriented
    * @param faction association of the object with
    * @param unk na
    * @return a `CommonFieldData` object
    */
  def apply(pos : PlacementData, faction : PlanetSideEmpire.Value, unk : Int) : CommonFieldData =
    CommonFieldData(pos, faction, unk, PlanetSideGUID(0))

  /**
    * `Codec` for transforming reliable `WeaponData` from the internal structure of the turret when it is defined.
    * Works for both `SmallTurretData` and `OneMannedFieldTurretData`.
    */
  val internalWeaponCodec : Codec[InternalSlot] = (
    uint8L :: //number of internal weapons (should be 1)?
      uint2L ::
      InternalSlot.codec
    ).exmap[InternalSlot] (
    {
      case 1 :: 0 :: InternalSlot(a1, b1, c1, WeaponData(a2, b2, c2, d)) :: HNil =>
        Attempt.successful(InternalSlot(a1, b1, c1, WeaponData(a2, b2, c2, d)))

      case 1 :: 0 :: InternalSlot(_, _, _, _) :: HNil =>
        Attempt.failure(Err(s"turret internals must contain weapon data"))

      case n :: 0 :: _ :: HNil =>
        Attempt.failure(Err(s"turret internals can not have $n weapons"))

      case _ =>
        Attempt.failure(Err("invalid turret internals data format"))
    },
    {
      case InternalSlot(a1, b1, c1, WeaponData(a2, b2, c2, d)) =>
        Attempt.successful(1 :: 0 :: InternalSlot(a1, b1, c1, WeaponData(a2, b2, c2, d)) :: HNil)

      case InternalSlot(_, _, _, _) =>
        Attempt.failure(Err(s"turret internals must contain weapon data"))

      case _ =>
        Attempt.failure(Err("invalid turret internals data format"))
    }
  )

  implicit val codec : Codec[CommonFieldData] = (
    ("pos" | PlacementData.codec) ::
      ("faction" | PlanetSideEmpire.codec) ::
      ("unk" | uint(5)) ::
      ("player_guid" | PlanetSideGUID.codec)
    ).exmap[CommonFieldData] (
    {
      case pos :: fac :: unk :: player :: HNil =>
        Attempt.successful(CommonFieldData(pos, fac, unk, player))

      case _ =>
        Attempt.failure(Err("invalid deployable data format"))
    },
    {
      case CommonFieldData(pos, fac, unk, player) =>
        Attempt.successful(pos :: fac :: unk :: player :: HNil)
    }
  )
}
