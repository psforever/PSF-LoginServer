// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

object RemoteProjectiles {
  abstract class Data(val a : Int, val b : Int)

  final case object Meteor extends Data(0, 32)
  final case object Wasp extends Data(0, 208)
  final case object Sparrow extends Data(13107, 187)
  final case object OICW extends Data(13107, 195)
  final case object Striker extends Data(26214, 134)
  final case object HunterSeeker extends Data(39577, 201)
  final case object Starfire extends Data(39577, 249)
  class OICWLittleBuddy(x : Int, y : Int) extends Data(x, y)
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
  * @param u1 na;
  *           first part of the canned remote projectile data
  * @param u2 na;
  *           second part of the canned remote projectile data
  * @param unk3 na;
  *             does something to how the projectile flies
  * @param unk4 na
  * @param unk5 na
  */
final case class RemoteProjectileData(common_data : CommonFieldDataWithPlacement,
                                      u1 : Int,
                                      u2 : Int,
                                      unk3 : FlightPhysics.Value,
                                      unk4 : Int,
                                      unk5 : Int
                                      ) extends ConstructorData {
  override def bitsize : Long = 33L + common_data.bitsize
}

object RemoteProjectileData extends Marshallable[RemoteProjectileData] {
  implicit val codec : Codec[RemoteProjectileData] = (
    ("data" | CommonFieldDataWithPlacement.codec) ::
      ("u1" | uint16) ::
      ("u2" | uint8) ::
      ("unk3" | FlightPhysics.codec) ::
      ("unk4" | uint(3)) ::
      ("unk5" | uint2)
    ).exmap[RemoteProjectileData] (
    {
      case data :: u1 :: u2 :: unk3 :: unk4 :: unk5 :: HNil =>
        Attempt.successful(RemoteProjectileData(data, u1, u2, unk3, unk4, unk5))

//      case data =>
//        Attempt.failure(Err(s"invalid projectile data format - $data"))
    },
    {
      case RemoteProjectileData(data, u1, u2, unk3, unk4, unk5) =>
        Attempt.successful(data :: u1 :: u2 :: unk3 :: unk4 :: unk5 :: HNil)
    }
  )
}
