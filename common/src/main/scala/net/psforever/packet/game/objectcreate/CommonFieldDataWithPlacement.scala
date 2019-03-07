// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire
import scodec.Codec
import scodec.codecs._

/**
  * Data that is common to a number of game object serializations, plus position information
  * @see `DroppedItemData`
  * @param pos the location, orientation, and potential velocity of the object
  * @param data the common fields
  */
final case class CommonFieldDataWithPlacement(pos : PlacementData,
                                              data : CommonFieldData
                                             ) extends ConstructorData {
  override def bitsize : Long = pos.bitsize + data.bitsize
}

object CommonFieldDataWithPlacement extends Marshallable[CommonFieldDataWithPlacement] {
  /**
    * Overloaded constructors.
    * @return a `CommonFieldDataWithPlacement` object
    */
  def apply(pos : PlacementData, faction : PlanetSideEmpire.Value) : CommonFieldDataWithPlacement =
    CommonFieldDataWithPlacement(pos, CommonFieldData(faction))

  def apply(pos : PlacementData, faction : PlanetSideEmpire.Value, unk : Int) : CommonFieldDataWithPlacement =
    CommonFieldDataWithPlacement(pos, CommonFieldData(faction, unk))

  def apply(pos : PlacementData, faction : PlanetSideEmpire.Value, unk : Int, player_guid : PlanetSideGUID) : CommonFieldDataWithPlacement =
    CommonFieldDataWithPlacement(pos, CommonFieldData(faction, unk, player_guid))

  def apply(pos : PlacementData, faction : PlanetSideEmpire.Value, destroyed : Boolean, unk : Int) : CommonFieldDataWithPlacement =
    CommonFieldDataWithPlacement(pos, CommonFieldData(faction, destroyed, unk))

  def apply(pos : PlacementData, faction : PlanetSideEmpire.Value, destroyed : Boolean, unk : Int, player_guid : PlanetSideGUID) : CommonFieldDataWithPlacement =
    CommonFieldDataWithPlacement(pos, CommonFieldData(faction, destroyed, unk, player_guid))

  def codec(extra : Boolean) : Codec[CommonFieldDataWithPlacement] = (
    ("pos" | PlacementData.codec) ::
      CommonFieldData.codec(extra)
    ).as[CommonFieldDataWithPlacement]

  implicit val codec : Codec[CommonFieldDataWithPlacement] = codec(false)

  def codec2(extra : Boolean) : Codec[CommonFieldDataWithPlacement] = (
    ("pos" | PlacementData.codec) ::
      CommonFieldData.codec2(extra)
    ).as[CommonFieldDataWithPlacement]

  implicit val codec2 : Codec[CommonFieldDataWithPlacement] = codec2(false)
}
