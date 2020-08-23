// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.newcodecs.newcodecs
import scodec.Codec
import scodec.codecs.ignore
import shapeless.{::, HNil}

/**
  * A series of `Codec`s designed to work with convert between 8-bit angle values in the packets and `Float` numbers.
  * As far as the data is concerned, the first bit appears to be ignored when it comes to the actual angle measurement.
  * The latter seven bits map between 0 to 360 perfectly (according to the game).
  */
object Angular {
  //roll
  val codec_roll: Codec[Float] = (
    ignore(1) ::
      codec_roll(7)
  ).xmap[Float](
    {
      case _ :: roll :: HNil =>
        roll
    },
    roll => () :: roll :: HNil
  )

  def codec_roll(bits: Int): Codec[Float] = newcodecs.q_float(0.0f, 360.0f, bits)

  //pitch
  val codec_pitch: Codec[Float] = (
    ignore(1) ::
      codec_pitch(7)
  ).xmap[Float](
    {
      case _ :: pitch :: HNil =>
        pitch
    },
    pitch => () :: pitch :: HNil
  )

  def codec_pitch(bits: Int): Codec[Float] =
    newcodecs
      .q_float(360.0f, 0.0f, bits)
      .xmap[Float](
        pitch => decodeCorrectedAngle(pitch),
        pitch => encodeCorrectedAngle(pitch)
      )

  //yaw
  def codec_yaw(North: Float = 90.0f): Codec[Float] =
    (
      ignore(1) ::
        codec_yaw(7, North)
    ).xmap[Float](
      {
        case _ :: yaw :: HNil =>
          yaw
      },
      yaw => () :: yaw :: HNil
    )

  def codec_yaw(bits: Int, North: Float): Codec[Float] =
    newcodecs
      .q_float(360.0f, 0.0f, bits)
      .xmap[Float](
        yaw => decodeCorrectedAngle(yaw, North),
        yaw => encodeCorrectedAngle(yaw, North)
      )

  val codec_zero_centered: Codec[Float] = codec_yaw(North = 0).xmap[Float](
    out => if (out > 180) out - 360 else out,
    in => {
      val adjustedIn = in % 360
      if (adjustedIn < 0) 360 + adjustedIn
      else if (adjustedIn > 180) 360 - adjustedIn
      else adjustedIn
    }
  )

  //support
  def decodeCorrectedAngle(angle: Float, correction: Float = 0f): Float = {
    var correctedAng: Float = angle + correction
    if (correctedAng >= 360f) {
      correctedAng = correctedAng - 360f
    }
    correctedAng
  }

  def encodeCorrectedAngle(angle: Float, correction: Float = 0f): Float = {
    var correctedAng: Float = angle - correction
    if (correctedAng <= 0f) {
      correctedAng = 360f + correctedAng % 360f
    } else if (correctedAng > 360f) {
      correctedAng = correctedAng % 360f
    }
    correctedAng
  }
}
