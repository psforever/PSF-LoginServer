// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of the aegis shield generator deployed using an advanced adaptive construction engine.
  * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
  * @param health the amount of health the object has, as a percentage of a filled bar
  */
final case class AegisShieldGeneratorData(deploy : ACEDeployableData,
                                          health : Int
                                         ) extends ConstructorData {
  override def bitsize : Long = {
    108 + deploy.bitsize //8u + 100u
  }
}

object AegisShieldGeneratorData extends Marshallable[AegisShieldGeneratorData] {
  implicit val codec : Codec[AegisShieldGeneratorData] = (
    ("deploy" | ACEDeployableData.codec) ::
      ("health" | uint8L) ::
      uint32 :: uint32 :: uint32 :: uint4L //100 bits
  ).exmap[AegisShieldGeneratorData] (
    {
      case deploy :: health :: 0 :: 0 :: 0 :: 0 :: HNil =>
        Attempt.successful(AegisShieldGeneratorData(deploy, health))
      case _ =>
        Attempt.failure(Err("invalid aegis data format"))
    },
    {
      case AegisShieldGeneratorData(deploy, health) =>
        Attempt.successful(deploy :: health :: 0L :: 0L :: 0L :: 0 :: HNil)
    }
  )
}
