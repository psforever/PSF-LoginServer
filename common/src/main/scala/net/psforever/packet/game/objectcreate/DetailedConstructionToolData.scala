// Copyright (c) 2018 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * `DetailedACEData` - `data.faction` is faction affinity, `data.unk1` is `true`
  * `DetailedBoomerTriggerData` - `data.faction` can be `NEUTRAL`, `data.unk1` is `true`
  * `DetailedTelepadData` - `data.faction` can be `NEUTRAL`, `data.jammered` is the router's GUID
  */
final case class DetailedConstructionToolData(data: CommonFieldData, mode: Int) extends ConstructorData {
  override def bitsize: Long = 28L + data.bitsize
}

object DetailedConstructionToolData extends Marshallable[DetailedConstructionToolData] {
  def apply(data: CommonFieldData): DetailedConstructionToolData = DetailedConstructionToolData(data, 0)

  implicit val codec: Codec[DetailedConstructionToolData] = (
    ("data" | CommonFieldData.codec(false)) ::
      uint8 ::
      ("mode" | uint16) ::
      uint2 ::
      uint2
  ).exmap[DetailedConstructionToolData](
    {
      case data :: 1 :: mode :: 1 :: _ :: HNil =>
        Attempt.successful(DetailedConstructionToolData(data, mode))
      case data =>
        Attempt.failure(Err(s"invalid detailed construction tool data format - $data"))
    },
    {
      case DetailedConstructionToolData(data, mode) =>
        Attempt.successful(data :: 1 :: mode :: 1 :: 0 :: HNil)
    }
  )
}
