// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of the tactical resonance area protection unit deployed using an advanced adaptive construction engine.
  * Three metal beams, erect and tangled, block passage to enemies and their vehicles.
  * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
  * @param health the amount of health the object has, as a percentage of a filled bar
  */
final case class TRAPData(deploy: CommonFieldDataWithPlacement, health: Int) extends ConstructorData {
  override def bitsize: Long = {
    23L + deploy.bitsize //1u + 8u + 7u + 4u + 3u
  }
}

object TRAPData extends Marshallable[TRAPData] {
  implicit val codec: Codec[TRAPData] = (
    ("deploy" | CommonFieldDataWithPlacement.codec) ::
      ignore(size = 1) ::
      ("health" | uint8L) ::
      uint(bits = 7) ::
      uint4L ::
      uint(bits = 3)
  ).exmap[TRAPData](
    {
      case deploy :: _:: health :: 0 :: 15 :: 0 :: HNil =>
        Attempt.successful(TRAPData(deploy, health))

      case data =>
        Attempt.failure(Err(s"invalid trap data format - $data"))
    },
    {
      case TRAPData(deploy, health) =>
        Attempt.successful(deploy :: () :: health :: 0 :: 15 :: 0 :: HNil)
    }
  )
}
