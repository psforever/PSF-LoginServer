// Copyright (c) 2016 PSForever.net to present
package net.psforever.newcodecs

import scodec.Codec

package object newcodecs {

  def q_double(min: Double, max: Double, bits: Int): Codec[Double] = new QuantizedDoubleCodec(min, max, bits)

}
