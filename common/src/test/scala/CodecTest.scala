// Copyright (c) 2016 PSForever.net to present
import org.specs2.mutable._
import net.psforever.newcodecs._
import scodec.bits._

class CodecTest extends Specification {

  "QuantizedDoubleCodec" should {
    val string_1 = hex"6E2D70"
    val string_2 = hex"000000"
    val string_3 = hex"B616"
    val string_4 = hex"857C"
    val string_5 = hex"5380"
    val string_6 = hex"FFFC"

    "decode" in {
      newcodecs.q_double(0.0, 8192.0, 20).decode(string_1.bits).require.value mustEqual 3674.859375
      newcodecs.q_double(0.0, 8192.0, 20).decode(string_2.bits).require.value mustEqual 0.0
      newcodecs.q_double(0.0, 1024.0, 16).decode(string_3.bits).require.value mustEqual 90.84375
      newcodecs.q_double(-256.0, 256.0, 14).decode(string_4.bits).require.value mustEqual -3.84375
      newcodecs.q_double(-256.0, 256.0, 14).decode(string_5.bits).require.value mustEqual 2.59375
      newcodecs.q_double(-256.0, 256.0, 14).decode(string_6.bits).require.value mustEqual 255.96875
    }

    "encode" in {
      newcodecs.q_double(0.0, 8192.0, 20).encode(3674.859375).require.bytes mustEqual string_1
      newcodecs.q_double(0.0, 8192.0, 20).encode(-1.23).require.bytes mustEqual string_2
      newcodecs.q_double(0.0, 1024.0, 16).encode(90.84375).require.bytes mustEqual string_3
      newcodecs.q_double(-256.0, 256.0, 14).encode(-3.84375).require.bytes mustEqual string_4
      newcodecs.q_double(-256.0, 256.0, 14).encode(2.59375).require.bytes mustEqual string_5
      newcodecs.q_double(-256.0, 256.0, 14).encode(257.0).require.bytes mustEqual string_6
    }
  }
}
