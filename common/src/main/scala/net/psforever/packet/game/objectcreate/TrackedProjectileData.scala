// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

object TrackedProjectile extends Enumeration {
  type Type = Value

  val OICWLittleBuddy = Value(-1) //?, ?
  val Meteor =          Value(32) //0, 32
  val Wasp =            Value(208) //0, 208
  val Sparrow =         Value(3355579) //13107, 187
  val OICW =            Value(3355587) //13107, 195
  val Striker =         Value(6710918) //26214, 134
  val HunterSeeker =    Value(10131913) //39577, 201
  val Starfire =        Value(10131961) //39577, 249

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint24)
}

object TrackedProjectiles {
  abstract class Data(val is : TrackedProjectile.Value, val a : Int, val b : Int)

  final case object Meteor extends Data(TrackedProjectile.Meteor, 0, 32)
  final case object Wasp extends Data(TrackedProjectile.Wasp, 0, 208)
  final case object Sparrow extends Data(TrackedProjectile.Sparrow, 13107, 187)
  final case object OICW extends Data(TrackedProjectile.OICW, 13107, 195)
  final case object Striker extends Data(TrackedProjectile.Striker, 26214, 134)
  final case object HunterSeeker extends Data(TrackedProjectile.HunterSeeker, 39577, 201)
  final case object Starfire extends Data(TrackedProjectile.Starfire, 39577, 249)
  class OICWLittleBuddy(x : Int, y : Int) extends Data(TrackedProjectile.OICWLittleBuddy, x, y)

  val values: Seq[TrackedProjectiles.Data] = Seq(Meteor, Wasp, Sparrow, OICW, Striker, HunterSeeker, Starfire)

  def apply(x : Int, y : Int) : TrackedProjectiles.Data = {
    values.find(p => p.a == x && p.b == y) match {
      case Some(projectileData) => projectileData
      case None =>
        throw new IllegalArgumentException("no combination of projectile data equates to a defined projectile type")
    }
  }

  def apply(is : TrackedProjectile.Value) : TrackedProjectiles.Data = {
    values.find(p => p.is == is) match {
      case Some(projectileData) => projectileData
      case None =>
        throw new IllegalArgumentException("unknown projectile type")
    }
  }
}

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
  * @param common_data common game object information
  * @param unk3 na
  */
final case class TrackedProjectileData(common_data : CommonFieldDataWithPlacement,
                                       u1 : Int,
                                       u2 : Int,
                                       unk3 : FlightPhysics.Value,
                                       unk4 : Int,
                                       unk5 : Int
                                      ) extends ConstructorData {
  override def bitsize : Long = 33L + common_data.bitsize
}

object TrackedProjectileData extends Marshallable[TrackedProjectileData] {
  implicit val codec : Codec[TrackedProjectileData] = (
    ("data" | CommonFieldDataWithPlacement.codec) ::
      ("u1" | uint16) ::
      ("u2" | uint8) ::
      ("unk3" | FlightPhysics.codec) ::
      ("unk4" | uint(3)) ::
      ("unk5" | uint2)
    ).exmap[TrackedProjectileData] (
    {
      case data :: u1 :: u2 :: unk3 :: unk4 :: unk5 :: HNil =>
        Attempt.successful(TrackedProjectileData(data, u1, u2, unk3, unk4, unk5))

//      case data =>
//        Attempt.failure(Err(s"invalid projectile data format - $data"))
    },
    {
      case TrackedProjectileData(data, u1, u2, unk3, unk4, unk5) =>
        Attempt.successful(data :: u1 :: u2 :: unk3 :: unk4 :: unk5 :: HNil)
    }
  )
}
