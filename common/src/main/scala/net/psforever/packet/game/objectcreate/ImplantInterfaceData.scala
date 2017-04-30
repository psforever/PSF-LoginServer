// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of an object that can be interacted with when using an implant terminal.
  * This object is generally invisible.
  */
final case class ImplantInterfaceData() extends ConstructorData {
  override def bitsize : Long = 24L
}

object ImplantInterfaceData extends Marshallable[ImplantInterfaceData] {
  implicit val codec : Codec[ImplantInterfaceData] = (
    bool ::
      uint(23)
    ).exmap[ImplantInterfaceData] (
    {
      case true :: 0 :: HNil =>
        Attempt.successful(ImplantInterfaceData())
      case _ :: _ :: HNil =>
        Attempt.failure(Err("invalid interface data format"))
    },
    {
      case ImplantInterfaceData() =>
        Attempt.successful(true :: 0 :: HNil)
    }
  )
}
