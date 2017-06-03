// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of simple objects that are spawned by the adaptive construction engine.
  * @param deploy data common to objects spawned by the (advanced) adaptive construction engine
  */
final case class SmallDeployableData(deploy : CommonFieldData) extends ConstructorData {
  override def bitsize : Long = deploy.bitsize + 1L
}

object SmallDeployableData extends Marshallable[SmallDeployableData] {
  implicit val codec : Codec[SmallDeployableData] = (
    ("deploy" | CommonFieldData.codec) ::
      bool
    ).exmap[SmallDeployableData] (
    {
      case deploy :: false :: HNil =>
        Attempt.successful(SmallDeployableData(deploy))

      case _ =>
        Attempt.failure(Err("invalid small deployable data format"))
    },
    {
      case SmallDeployableData(deploy) =>
        Attempt.successful(deploy :: false :: HNil)
    }
  )
}
