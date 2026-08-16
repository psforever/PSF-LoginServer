// Copyright (c) 2017 PSForever
package net.psforever.packet.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class CommonFieldDataExtra(unk1: Option[Int], unk2: Boolean) extends StreamBitSize {
  override def bitsize: Long = {
    unk1 match {
      case Some(_) => 17L
      case None => 1L
    }
  }
}

object CommonFieldDataExtra {
  final val Default: CommonFieldDataExtra = CommonFieldDataExtra(unk1 = None, unk2 = false)

  def codec(unk1: Boolean): Codec[CommonFieldDataExtra] =
    (
      ("unk1" | conditional(unk1, uint16L)) :: //not sure what flags this field
        ("unk2" | bool)
    ).as[CommonFieldDataExtra]
}

/**
  * Data that is common to a number of game object serializations.
  * @param faction   faction affinity
  *                  `NEUTRAL` when not required to be any specific value
  * @param bops      usually indicates black ops affiliation
  * @param alternate usually indicates variance in model from default (e.g., vehicle is destroyed, player has released, etc.);
  *                  when set on a tool, that tool will be rendered nonfunctional instead (though it can still be equipped)
  * @param v1        na
  * @param v2        na;
  *                  optional data whose reading is triggered in unknown conditions
  * @param jammered  flag as "jammered;"
  * @param v5        na;
  *                  previously considered to flag as "jammered"
  * @param guid      usually indicates another active game object that placed/leverages/[action]s this object
  */
final case class CommonFieldData(faction: PlanetSideEmpire.Value, bops: Boolean, alternate: Boolean, v1: Boolean, v2: Option[CommonFieldDataExtra], jammered: Boolean, v5: Option[Int], guid: PlanetSideGUID) extends ConstructorData {
  override def bitsize: Long = {
    val extraSize: Long = v2 match {
      case Some(v) => v.bitsize
      case None    => 0L
    }
    val v5Size = v5 match {
      case Some(_) => 16L
      case None    => 0L
    }
    23L + extraSize + v5Size
  }
}

object CommonFieldData extends Marshallable[CommonFieldData] {
  /**
    * Overloaded constructors.
    * @return a `CommonFieldData` object
    */
  def apply(): CommonFieldData = {
    CommonFieldData(PlanetSideEmpire.NEUTRAL, bops = false, alternate = false, v1 = false, None, jammered = false, None, PlanetSideGUID(0))
  }

  def apply(faction: PlanetSideEmpire.Value): CommonFieldData = {
    CommonFieldData(faction, bops = false, alternate = false, v1 = false, None, jammered = false, None, PlanetSideGUID(0))
  }

  def extra(): CommonFieldData = {
    CommonFieldData(PlanetSideEmpire.NEUTRAL, bops = false, alternate = false, v1 = false, Some(CommonFieldDataExtra.Default), jammered = false, None, PlanetSideGUID(0))
  }

  def extra(faction: PlanetSideEmpire.Value): CommonFieldData = {
    CommonFieldData(faction, bops = false, alternate = false, v1 = false, Some(CommonFieldDataExtra.Default), jammered = false, None, PlanetSideGUID(0))
  }

  def codec(extra: Boolean, extra16bit: Boolean = false): Codec[CommonFieldData] =
    (
      ("faction" | PlanetSideEmpire.codec) ::
        ("bops" | bool) ::
        ("alternate" | bool) ::
        ("v1" | bool) :: //the purpose of this bit changes depending on the previous bit
        ("v2" | conditional(extra, CommonFieldDataExtra.codec(unk1 = extra16bit))) ::
        ("jammered" | bool) ::
        ("v5" | optional(bool, uint16L)) ::
        ("guid" | PlanetSideGUID.codec)
    ).xmap[CommonFieldData](
      {
        case faction :: bops :: alternate :: v1 :: v2 :: v3 :: v5 :: player_guid :: HNil =>
          CommonFieldData(faction, bops, alternate, v1, v2, v3, v5, player_guid)
      },
      {
        case CommonFieldData(faction, bops, alternate, v1, v2, v3, v5, guid) =>
          faction :: bops :: alternate :: v1 :: v2 :: v3 :: v5 :: guid :: HNil
      }
    )

  implicit val codec: Codec[CommonFieldData] = codec(extra = false)

  val codec_extra: Codec[CommonFieldData] = codec(extra = true)
}
