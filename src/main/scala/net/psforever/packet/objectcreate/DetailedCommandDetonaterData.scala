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
    DetailedCommandDetonaterData.baseSize + dataSize
  }
}

object DetailedCommandDetonaterData extends Marshallable[DetailedCommandDetonaterData] {
  private val base: ToolPatternData = ToolPatternData(u1 = 1, u2 = 0, u3 = false, u4 = true, u5 = None, u6 = false)
  private val baseSize: Long = base.bitsize

  implicit val codec: Codec[DetailedCommandDetonaterData] = (
    ("data" | CommonFieldData.codec) ::
      ToolPatternData.codec
  ).exmap[DetailedCommandDetonaterData](
    {
      case data :: _ :: HNil =>
        Attempt.successful(DetailedCommandDetonaterData(data))

      case data =>
        Attempt.failure(Err(s"invalid detailed command detonater data format - $data"))
    },
    {
      case DetailedCommandDetonaterData(data) =>
        Attempt.successful(data :: base :: HNil)
    }
  )
}
