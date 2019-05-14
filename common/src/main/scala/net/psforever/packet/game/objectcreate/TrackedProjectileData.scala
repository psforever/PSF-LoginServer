// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

object FlightPhysics extends Enumeration {
  type Type = Value

  //valid (extremely small distance) (requires non-zero unk4, unk5)
  val State3 = Value(3)
  //valid (infinite) (if unk4 == 0 unk5 == 0, minimum distance + time)
  val State4 = Value(4)
  //valid(infinite)
  val State5 = Value(5)
  //valid (uses velocity) (infinite)
  val State6 = Value(6)
  //valid (uses velocity) (infinite)
  val State7 = Value(7)
  //valid (uses velocity) (time > 0 is infinite) (unk5 == 2)
  val State15 = Value(15)

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

/**
  * A representation of a projectile that the server must intentionally convey to players other than the shooter.
  * @param data common game object information
  * @param unk2 na
  * @param unit_distance_limit how quickly the projectile travels before naturally being destroyed
  *                            `FlightPhysics` needs to be
  * @param unk3 na
  */
final case class TrackedProjectileData(data : CommonFieldDataWithPlacement,
                                       unk2 : Int,
                                       unit_distance_limit : Int,
                                       unk3 : FlightPhysics.Value,
                                       unk4 : Int,
                                       unk5 : Int
                                      ) extends ConstructorData {
  override def bitsize : Long = 33L + data.bitsize
}

object TrackedProjectileData extends Marshallable[TrackedProjectileData] {
  implicit val codec : Codec[TrackedProjectileData] = (
    ("data" | CommonFieldDataWithPlacement.codec) ::
      ("unk2" | uint16) ::
      ("unit_distance_limit" | uint8) ::
      ("unk3" | FlightPhysics.codec) ::
      ("unk4" | uint(3)) ::
      ("unk5" | uint2)
    ).exmap[TrackedProjectileData] (
    {
      case data :: unk2 :: lim :: unk3 :: unk4 :: unk5 :: HNil =>
        Attempt.successful(TrackedProjectileData(data, unk2, lim, unk3, unk4, unk5))

      case data =>
        Attempt.failure(Err(s"invalid projectile data format - $data"))
    },
    {
      case TrackedProjectileData(data, unk2, lim, unk3, unk4, unk5) =>
        Attempt.successful(data :: unk2 :: lim :: unk3 :: unk4 :: unk5 :: HNil)
    }
  )
}
