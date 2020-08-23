// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the command uplink device.
  */
final case class DetailedCommandDetonaterData(data: CommonFieldData) extends ConstructorData {
  override def bitsize: Long = {
    val dataSize = data.bitsize
    28L + dataSize
  }
}

object DetailedCommandDetonaterData extends Marshallable[DetailedCommandDetonaterData] {
  implicit val codec: Codec[DetailedCommandDetonaterData] = (
    ("data" | CommonFieldData.codec) ::
      uint8 ::
      uint16 ::
      uint4
  ).exmap[DetailedCommandDetonaterData](
    {
      case data :: 1 :: 0 :: 4 :: HNil =>
        Attempt.successful(DetailedCommandDetonaterData(data))

      case data =>
        Attempt.failure(Err(s"invalid detailed command detonater data format - $data"))
    },
    {
      case DetailedCommandDetonaterData(data) =>
        Attempt.successful(data :: 1 :: 0 :: 4 :: HNil)
    }
  )
}
