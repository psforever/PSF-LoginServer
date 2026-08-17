// Copyright (c) 2024 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class SmallDeployableData(deploy: CommonFieldDataWithPlacement) extends ConstructorData {
  override def bitsize: Long = {
    deploy.bitsize + 1
  }
}

object SmallDeployableData extends Marshallable[SmallDeployableData] {
  implicit val codec: Codec[SmallDeployableData] = (
    ("deploy" | CommonFieldDataWithPlacement.codec) ::
      ignore(size = 1)
    ).exmap[SmallDeployableData](
    {
      case deploy :: _ :: HNil =>
        Attempt.successful(SmallDeployableData(deploy))

      case data =>
        Attempt.failure(Err(s"invalid small deployable data format - $data"))
    },
    {
      case SmallDeployableData(deploy) =>
        Attempt.successful(deploy :: () :: HNil)
    }
  )
}
