// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * This class currently is unused but is based on the `SmallTurretData` `Codec` class.
  */
final case class LargeDeployableData(deploy : CommonFieldDataWithPlacement,
                                     health : Int,
                                     internals : Option[InventoryData] = None
                                ) extends ConstructorData {
  override def bitsize : Long = {
    val deploySize = deploy.bitsize
    val internalSize = internals match {
      case Some(inv) =>
        inv.bitsize
      case None =>
        0
    }
    22L + deploySize + internalSize //8u + 7u + 4u + 2u + 1u
  }
}

object LargeDeployableData extends Marshallable[LargeDeployableData] {
  implicit val codec : Codec[LargeDeployableData] = (
    ("deploy" | CommonFieldDataWithPlacement.codec2) ::
      ("health" | uint8L) ::
      uintL(7) ::
      uint4L ::
      uint2L ::
      optional(bool, "internals" | InventoryData.codec)
    ).exmap[LargeDeployableData] (
    {
      case deploy :: health :: 0 :: 0xF :: 0 :: internals :: HNil =>
        val (newHealth, newInternals) = if(health == 0 || internals.isEmpty || internals.get.contents.isEmpty) {
          (0, None)
        }
        else {
          (health, internals)
        }
        Attempt.successful(LargeDeployableData(deploy, newHealth, newInternals))


      case data =>
        Attempt.failure(Err(s"invalid large deployable data format - $data"))
    },
    {
      case LargeDeployableData(deploy, health, internals) =>
        val (newHealth, newInternals) = if(health == 0 || internals.isEmpty || internals.get.contents.isEmpty) {
          (0, None)
        }
        else {
          (health, internals)
        }
        Attempt.successful(deploy :: newHealth :: 0 :: 0xF :: 0 :: newInternals :: HNil)
    }
  )
}
