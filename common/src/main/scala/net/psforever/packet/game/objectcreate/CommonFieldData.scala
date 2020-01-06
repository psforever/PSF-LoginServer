// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class CommonFieldDataExtra(unk1 : Option[Int], unk2 : Boolean) extends StreamBitSize {
  override def bitsize : Long = 17L
}

object CommonFieldDataExtra {
  def codec(unk1 : Boolean) : Codec[CommonFieldDataExtra] = (
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
  *                  set on most game objects, that object will produce the characteristic jammered buzz
  * @param v4        na;
  *                  a field used by a second encoding format for this data
  * @param v5        na;
  *                  previously considered to flag as "jammered"
  * @param guid      usually indicates another active game object that placed/leverages/[action]s this object
  */
final case class CommonFieldData(faction : PlanetSideEmpire.Value,
                                 bops : Boolean,
                                 alternate : Boolean,
                                 v1 : Boolean,
                                 v2 : Option[CommonFieldDataExtra],
                                 jammered : Boolean,
                                 v4 : Option[Boolean],
                                 v5 : Option[Int],
                                 guid : PlanetSideGUID
                                ) extends ConstructorData {
  override def bitsize : Long = {
    val extraSize : Long = v2 match {
      case Some(v) => v.bitsize
      case None => 0L
    }
    val v4Size = v4 match {
      case Some(_) => 1L
      case None => 0L
    }
    val v5Size = v5 match {
      case Some(_) => 16L
      case None => 0L
    }
    23L + extraSize + v4Size + v5Size
  }

  def apply(flag : Boolean) : CommonFieldData = CommonFieldData(faction, bops, alternate, v1, v2, jammered, Some(flag), v5, guid)
}

object CommonFieldData extends Marshallable[CommonFieldData] {
  /**
    * Overloaded constructors.
    * @return a `CommonFieldData` object
    */
  def apply() : CommonFieldData =
    CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, false, None, false, None, None, PlanetSideGUID(0))

  def apply(faction : PlanetSideEmpire.Value) : CommonFieldData =
    CommonFieldData(faction, false, false, false, None, false, None, None, PlanetSideGUID(0))

  def apply(faction : PlanetSideEmpire.Value, unk : Int) : CommonFieldData =
    CommonFieldData(faction, false, false, unk>1, None, unk%1==1, None, None, PlanetSideGUID(0))

  def apply(faction : PlanetSideEmpire.Value, unk : Int, player_guid : PlanetSideGUID) : CommonFieldData =
    CommonFieldData(faction, false, false, unk>1, None, unk%1==1, None, None, player_guid)

  def apply(faction : PlanetSideEmpire.Value, destroyed : Boolean, unk : Int) : CommonFieldData =
    CommonFieldData(faction, false, destroyed, unk>1, None, unk%1==1, None, None, PlanetSideGUID(0))

  def apply(faction : PlanetSideEmpire.Value, destroyed : Boolean, unk : Int, player_guid : PlanetSideGUID) : CommonFieldData =
    CommonFieldData(faction, false, destroyed, unk>1, None, unk%1==1, None, None, player_guid)

  def apply(faction : PlanetSideEmpire.Value, bops : Boolean, destroyed : Boolean, unk : Int, jammered : Boolean, player_guid : PlanetSideGUID) : CommonFieldData = {
    val jammeredField = if(jammered) { Some(0) } else { None }
    CommonFieldData(faction, bops, destroyed, unk>1, None, unk%1==1, None, jammeredField, player_guid)
  }

  def codec(extra : Boolean) : Codec[CommonFieldData] = (
    ("faction" | PlanetSideEmpire.codec) ::
      ("bops" | bool) ::
      ("alternate" | bool) ::
      ("v1" | bool) :: //the purpose of this bit changes depending on the previous bit
      conditional(extra, "v2" | CommonFieldDataExtra.codec(unk1 = false)) ::
      ("jammered" | bool) ::
      optional(bool, "v5" | uint16L) ::
      ("guid" | PlanetSideGUID.codec)
    ).xmap[CommonFieldData] (
    {
      case faction :: bops :: alternate :: v1 :: v2 :: v3 :: v5 :: player_guid :: HNil =>
        CommonFieldData(faction, bops, alternate, v1, v2, v3, None, v5, player_guid)
    },
    {
      case CommonFieldData(faction, bops, alternate, v1, v2, v3, _, v5, guid) =>
        faction :: bops :: alternate :: v1 :: v2 :: v3 :: v5 :: guid :: HNil
    }
  )

  implicit val codec : Codec[CommonFieldData] = codec(false)

  def codec2(extra : Boolean) : Codec[CommonFieldData] = (
    ("faction" | PlanetSideEmpire.codec) ::
      ("bops" | bool) ::
      ("alternate" | bool) ::
      ("v1" | bool) :: //though the code path differs depending on the previous bit, this one gets read one way or another
      conditional(extra, "v2" | CommonFieldDataExtra.codec(unk1 = false)) ::
      ("jammered" | bool) ::
      optional(bool, "v5" | uint16L) ::
      ("v4" | bool) ::
      ("guid" | PlanetSideGUID.codec)
    ).exmap[CommonFieldData] (
    {
      case faction :: bops :: alternate :: v1 :: v2 :: v3 :: v5 :: v4 :: guid :: HNil =>
        Attempt.successful(CommonFieldData(faction, bops, alternate, v1, v2, v3, Some(v4), v5, guid))
    },
    {
      case CommonFieldData(_, _, _, _, _, _, None, _, _) =>
        Attempt.Failure(Err("invalid CommonFieldData - expected a field to be defined, but it was 'None'"))

      case CommonFieldData(faction, bops, alternate, v1, v2, v3, Some(v4), v5, player_guid) =>
        Attempt.successful(faction :: bops :: alternate :: v1 :: v2 :: v3 :: v5 :: v4 :: player_guid :: HNil)
    }
  )

  val codec2 : Codec[CommonFieldData] = codec2(false)
}
