// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of a projectile that the server must intentionally convey to players other than the shooter.
  * @param pos where and how the projectile is oriented
  * @param unk1 na
  * @param unk2 na;
  *             data specific to the type of projectile(?)
  */
final case class TrackedProjectileData(pos : PlacementData,
                                       unk1 : Int,
                                       unk2 : Int
                                      ) extends ConstructorData {
  override def bitsize : Long = 56L + pos.bitsize
}

object TrackedProjectileData extends Marshallable[TrackedProjectileData] {
  final val oicw_projectile_data = 3355587
  final val striker_missile_targetting_projectile_data = 6710918
  final val hunter_seeker_missile_projectile_data = 10131913
  final val starfire_projectile_data = 10131961

  /**
    * Overloaded constructor specifically for OICW projectiles.
    * @param pos where and how the projectile is oriented
    * @param unk na
    * @return a `TrackedProjectileData` object
    */
  def oicw(pos : PlacementData, unk : Int) : TrackedProjectileData =
    new TrackedProjectileData(pos, unk, oicw_projectile_data)

  /**
    * Overloaded constructor specifically for Striker projectiles.
    * @param pos where and how the projectile is oriented
    * @param unk na
    * @return a `TrackedProjectileData` object
    */
  def striker(pos : PlacementData, unk : Int) : TrackedProjectileData =
    new TrackedProjectileData(pos, unk, striker_missile_targetting_projectile_data)

  /**
    * Overloaded constructor specifically for Hunter Seeker (Phoenix) projectiles.
    * @param pos where and how the projectile is oriented
    * @param unk na
    * @return a `TrackedProjectileData` object
    */
  def hunter_seeker(pos : PlacementData, unk : Int) : TrackedProjectileData =
    new TrackedProjectileData(pos, unk, hunter_seeker_missile_projectile_data)

  /**
    * Overloaded constructor specifically for Starfire projectiles.
    * @param pos where and how the projectile is oriented
    * @param unk na
    * @return a `TrackedProjectileData` object
    */
  def starfire(pos : PlacementData, unk : Int) : TrackedProjectileData =
    new TrackedProjectileData(pos, unk, starfire_projectile_data)

  implicit val codec : Codec[TrackedProjectileData] = (
    ("pos" | PlacementData.codec) ::
      ("unk1" | uint(3)) ::
      uint4L ::
      uint16L ::
      ("unk2" | uint24) ::
      uint4L ::
      uint(5)
    ).exmap[TrackedProjectileData] (
    {
      case pos :: unk1 :: 4 :: 0 :: unk2 :: 4 :: 0 :: HNil =>
        Attempt.successful(TrackedProjectileData(pos, unk1, unk2))

      case _ =>
        Attempt.failure(Err("invalid projectile data format"))
    },
    {
      case TrackedProjectileData(pos, unk1, unk2) =>
        Attempt.successful(pos :: unk1 :: 4 :: 0 :: unk2 :: 4 :: 0 :: HNil)
    }
  )
}
