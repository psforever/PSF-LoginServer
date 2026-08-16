// Copyright (c) 2017 PSForever
package net.psforever.packet.objectcreate

import net.psforever.packet.Marshallable
import scodec.Codec
import scodec.codecs._

/**
  * Data that is common to a number of game object serializations, plus position information
  * @see `DroppedItemData`
  * @param pos the location, orientation, and potential velocity of the object
  * @param data the common fields
  */
final case class CommonFieldDataWithPlacement(pos: PlacementData, data: CommonFieldData) extends ConstructorData {
  override def bitsize: Long = pos.bitsize + data.bitsize
}

object CommonFieldDataWithPlacement extends Marshallable[CommonFieldDataWithPlacement] {
  implicit val codec: Codec[CommonFieldDataWithPlacement] =
    (
      ("pos" | PlacementData.codec) ::
        ("data" | CommonFieldData.codec)
      ).as[CommonFieldDataWithPlacement]

  implicit val codec_extra: Codec[CommonFieldDataWithPlacement] =
    (
      ("pos" | PlacementData.codec) ::
        ("data" | CommonFieldData.codec_extra)
      ).as[CommonFieldDataWithPlacement]
}
