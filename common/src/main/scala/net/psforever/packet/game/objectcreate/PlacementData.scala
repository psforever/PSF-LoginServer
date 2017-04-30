// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.Vector3
import scodec.codecs._
import scodec.Codec

/**
  * A specific location and heading in game world coordinates and game world measurements.
  * @param coord the xyz-coordinate location in the world
  * @param roll the amount of roll that affects orientation
  * @param pitch the amount of pitch that affects orientation
  * @param yaw the amount of yaw that affects orientation
  * @param init_move optional movement data that occurs upon placement
  */
final case class PlacementData(coord : Vector3,
                               roll : Int,
                               pitch : Int,
                               yaw : Int,
                               init_move : Option[Vector3] = None
                              ) extends StreamBitSize {
  override def bitsize : Long = {
    val moveLength = if(init_move.isDefined) { 42 } else { 0 }
    81L + moveLength
  }
}

object PlacementData extends Marshallable[PlacementData] {
  /**
    * An abbreviated constructor for creating `PlacementData`, ignoring the `Vector3` for position data.
    * @param x the x-coordinate location in the world
    * @param y the y-coordinate location in the world
    * @param z the z-coordinate location in the world
    * @return a `PlacementData` object
    */
  def apply(x : Float, y : Float, z : Float) : PlacementData =
    new PlacementData(Vector3(x, y, z), 0, 0, 0)

  /**
    * An abbreviated constructor for creating `PlacementData`, ignoring the `Vector3` for position data, supplying other important fields.
    * @param x the x-coordinate location in the world
    * @param y the y-coordinate location in the world
    * @param z the z-coordinate location in the world
    * @param roll the amount of roll that affects orientation
    * @param pitch the amount of pitch that affects orientation
    * @param yaw the amount of yaw that affects orientation
    * @return a `PlacementData` object
    */
  def apply(x : Float, y : Float, z : Float, roll : Int, pitch : Int, yaw : Int) : PlacementData =
    new PlacementData(Vector3(x, y, z), roll, pitch, yaw)

  /**
    * An abbreviated constructor for creating `PlacementData`, ignoring the `Vector3` for position data, supplying all other fields.
    * @param x the x-coordinate location in the world
    * @param y the y-coordinate location in the world
    * @param z the z-coordinate location in the world
    * @param roll the amount of roll that affects orientation
    * @param pitch the amount of pitch that affects orientation
    * @param yaw the amount of yaw that affects orientation
    * @param init_move optional movement data that occurs upon placement
    * @return a `PlacementData` object
    */
  def apply(x : Float, y : Float, z : Float, roll : Int, pitch : Int, yaw : Int, init_move : Vector3) : PlacementData =
    new PlacementData(Vector3(x, y, z), roll, pitch, yaw, Some(init_move))

  implicit val codec : Codec[PlacementData] = (
    ("coord" | Vector3.codec_pos) ::
      ("roll" | uint8L) ::
      ("pitch" | uint8L) ::
      ("yaw" | uint8L) ::
      optional(bool, "init_move" | Vector3.codec_vel)
    ).as[PlacementData]
}
