// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of a number of simplified objects that the user can hold in their hands, including:
  * the advanced construction engine (`ace`),
  * the field deployable unit (`advanced_ace`),
  * the boomer trigger apparatus,
  * the remote telepad (not deployed),
  * the flail laser pointer (`flail_targeting_laser`),
  * and the command uplink device (`command_detonater`).
  * @param data fields that are common to this game object
  *             - v4 - not used, i.e., the simple format `CommonFieldData` object is employed
  *             - v5 - for the telepad, this field is expected to be the GUID of the associated Router
  */
final case class HandheldData(data: CommonFieldData, mode: Int, unk: Int) extends ConstructorData {
  override def bitsize: Long = {
    11L + data.bitsize
  }
}

object HandheldData extends Marshallable[HandheldData] {
  def apply(data: CommonFieldData): HandheldData = HandheldData(data, 0, 0)

  def apply(data: CommonFieldData, mode: Int): HandheldData = HandheldData(data, mode, 0)

  implicit val codec: Codec[HandheldData] = (
    ("data" | CommonFieldData.codec) ::
      ("mode" | uint8) ::
      ("unk" | uint(3))
  ).exmap[HandheldData](
    {
      case data :: mode :: unk :: HNil =>
        Attempt.successful(HandheldData(data, mode, unk))

      case data =>
        Attempt.failure(Err(s"invalid handheld tool data format - $data"))
    },
    {
      case HandheldData(data, mode, unk) =>
        Attempt.successful(data :: mode :: unk :: HNil)
    }
  )
}
