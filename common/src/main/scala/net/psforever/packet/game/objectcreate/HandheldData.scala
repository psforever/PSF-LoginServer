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
final case class HandheldData(data : CommonFieldData) extends ConstructorData {
  override def bitsize : Long = {
    11L + data.bitsize
  }
}

object HandheldData extends Marshallable[HandheldData] {
  implicit val codec : Codec[HandheldData] = (
    ("data" | CommonFieldData.codec) ::
      uint4 ::
      uint4 ::
      uint(3)
    ).exmap[HandheldData] (
    {
      case data :: 0 :: 0 :: 0 :: HNil =>
        Attempt.successful(HandheldData(data))

      case data =>
        Attempt.failure(Err(s"invalid handheld tool data format - $data"))
    },
    {
      case HandheldData(data) =>
        Attempt.successful(data :: 0 :: 0 :: 0 :: HNil)
    }
  )
}
