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
final case class TRAPData(deploy : CommonFieldData,
                          health : Int
                         ) extends ConstructorData {
  override def bitsize : Long = {
    23L + deploy.bitsize //8u + 7u + 4u + 3u + 1u
  }
}

object TRAPData extends Marshallable[TRAPData] {
  implicit val codec : Codec[TRAPData] = (
    ("deploy" | CommonFieldData.codec) ::
      bool ::
      ("health" | uint8L) ::
      uint(7) ::
      uint4L ::
      uint(3)
    ).exmap[TRAPData] (
    {
      case deploy :: false :: health :: 0 :: 15 :: 0 :: HNil =>
        Attempt.successful(TRAPData(deploy, health))

      case _ =>
        Attempt.failure(Err("invalid trap data format"))
    },
    {
      case TRAPData(deploy, health) =>
        Attempt.successful(deploy :: false :: health :: 0 :: 15 :: 0 :: HNil)
    }
  )
}
