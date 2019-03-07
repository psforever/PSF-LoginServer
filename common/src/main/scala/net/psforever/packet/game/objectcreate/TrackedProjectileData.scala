// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

object TrackedProjectile extends Enumeration {
  type Type = Value

  val Meteor =       Value(32)
  val WaspRocket =   Value(208)
  val Sparrow =      Value(3355579)
  val OICW =         Value(3355587)
  val Striker =      Value(6710918)
  val HunterSeeker = Value(10131913)
  val Starfire =     Value(10131961)

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint24)
}

/**
  * A representation of a projectile that the server must intentionally convey to players other than the shooter.
  * @param data na
  * @param unk2 na;
  *             data specific to the type of projectile(?)
  * @param unk3 na
  */
final case class TrackedProjectileData(data : CommonFieldDataWithPlacement,
                                       unk2 : TrackedProjectile.Value,
                                       unk3 : Int = 0
                                      ) extends ConstructorData {
  override def bitsize : Long = 33L + data.bitsize
}

object TrackedProjectileData extends Marshallable[TrackedProjectileData] {
  implicit val codec : Codec[TrackedProjectileData] = (
    ("data" | CommonFieldDataWithPlacement.codec) ::
      ("unk2" | TrackedProjectile.codec) ::
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
