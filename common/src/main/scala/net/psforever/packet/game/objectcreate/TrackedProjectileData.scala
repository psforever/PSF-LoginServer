// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of a projectile that the server must intentionally convey to players other than the shooter.
  * @param data na
  * @param unk2 na
  * @param unk3 na
  */
final case class TrackedProjectileData(data : CommonFieldDataWithPlacement,
                                       unk2 : Int,
                                       unk3 : Int = 0
                                      ) extends ConstructorData {
  override def bitsize : Long = 33L + data.bitsize
}

object TrackedProjectileData extends Marshallable[TrackedProjectileData] {
  implicit val codec : Codec[TrackedProjectileData] = (
    ("data" | CommonFieldDataWithPlacement.codec) ::
      ("unk2" | uint24) ::
      uint4 ::
      uint(3) ::
      uint2
    ).exmap[TrackedProjectileData] (
    {
      case data :: unk2 :: 4 :: unk3 :: 0 :: HNil =>
        Attempt.successful(TrackedProjectileData(data, unk2, unk3))

      case data =>
        Attempt.failure(Err(s"invalid projectile data format - $data"))
    },
    {
      case TrackedProjectileData(data, unk2, unk3) =>
        Attempt.successful(data :: unk2 :: 4 :: unk3 :: 0 :: HNil)
    }
  )
}
