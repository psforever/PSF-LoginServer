// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.{Angular, Vector3}
import scodec.codecs._
import scodec.Codec

/**
  * A specific location and heading in game world coordinates and game world measurements.
  * @param coord the xyz-coordinate location in the world
  * @param roll the amount of roll that affects orientation
  * @param pitch the amount of pitch that affects orientation
  * @param yaw the amount of yaw that affects orientation
  * @param vel optional movement data (that occurs upon placement)
  */
final case class PlacementData(coord : Vector3,
                               roll : Float,
                               pitch : Float,
                               yaw : Float,
                               vel : Option[Vector3] = None
                              ) extends StreamBitSize {
  override def bitsize : Long = {
    val moveLength = if(vel.isDefined) { 42 } else { 0 }
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
    new PlacementData(Vector3(x, y, z), 0f, 0f, 0f)

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
  def apply(x : Float, y : Float, z : Float, roll : Float, pitch : Float, yaw : Float) : PlacementData =
    new PlacementData(Vector3(x, y, z), roll, pitch, yaw)

  /**
    * An abbreviated constructor for creating `PlacementData`, ignoring the `Vector3` for position data, supplying all other fields.
    * @param x the x-coordinate location in the world
    * @param y the y-coordinate location in the world
    * @param z the z-coordinate location in the world
    * @param roll the amount of roll that affects orientation
    * @param pitch the amount of pitch that affects orientation
    * @param yaw the amount of yaw that affects orientation
    * @param vel optional movement data that occurs upon placement
    * @return a `PlacementData` object
    */
  def apply(x : Float, y : Float, z : Float, roll : Float, pitch : Float, yaw : Float, vel : Vector3) : PlacementData =
    new PlacementData(Vector3(x, y, z), roll, pitch, yaw, Some(vel))

  implicit val codec : Codec[PlacementData] = (
    ("coord" | Vector3.codec_pos) ::
      ("roll" | Angular.codec_roll) ::
      ("pitch" | Angular.codec_pitch) ::
      ("yaw" | Angular.codec_yaw()) ::
      optional(bool, "vel" | Vector3.codec_vel)
    ).as[PlacementData]
}
