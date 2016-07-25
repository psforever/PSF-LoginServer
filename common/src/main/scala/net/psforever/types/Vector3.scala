// Copyright (c) 2016 PSForever.net to present
package net.psforever.types

import net.psforever.newcodecs._
import scodec.Codec
import scodec.codecs._

final case class Vector3(x : Float,
                         y : Float,
                         z : Float)

object Vector3 {
  implicit val codec_pos : Codec[Vector3] = (
      ("x" | newcodecs.q_float(0.0, 8192.0, 20)) ::
      ("y" | newcodecs.q_float(0.0, 8192.0, 20)) ::
      ("z" | newcodecs.q_float(0.0, 1024.0, 16))
    ).as[Vector3]

  implicit val codec_vel : Codec[Vector3] = (
      ("x" | newcodecs.q_float(-256.0, 256.0, 14)) ::
      ("y" | newcodecs.q_float(-256.0, 256.0, 14)) ::
      ("z" | newcodecs.q_float(-256.0, 256.0, 14))
    ).as[Vector3]
}
