// Copyright (c) 2017 PSForever
import org.specs2.mutable._
import net.psforever.newcodecs._
import net.psforever.types._
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

  "Vector3" should {
    "position" should {
      val string_pos = hex"6E2D762222B616"

      "decode" in {
        Vector3.codec_pos.decode(string_pos.bits).require.value mustEqual Vector3(3674.859375f, 1092.7656f, 90.84375f)
      }

      "encode" in {
        Vector3.codec_pos.encode(Vector3(3674.859375f, 1092.7656f, 90.84375f)).require.bytes mustEqual string_pos
      }
    }

    "velocity" should {
      val string_vel = hex"857D4E0FFFC0"

      "decode" in {
        Vector3.codec_vel.decode(string_vel.bits).require.value mustEqual Vector3(-3.84375f, 2.59375f, 255.96875f)
      }

      "encode" in {
        Vector3.codec_vel.encode(Vector3(-3.84375f, 2.59375f, 255.96875f)).require.bytes mustEqual string_vel
      }
    }
  }

  "Angular" should {
    "roll" should {
      val string_roll_0 = hex"00"
      val string_roll_90 = hex"20"
      val string_roll_180 = hex"40"
      val string_roll_270 = hex"60"

      "decode (0)" in {
        Angular.codec_roll.decode(string_roll_0.bits).require.value mustEqual 0f
      }

      "decode (90)" in {
        Angular.codec_roll.decode(string_roll_90.bits).require.value mustEqual 90f
      }

      "decode (180)" in {
        Angular.codec_roll.decode(string_roll_180.bits).require.value mustEqual 180f
      }

      "decode (270)" in {
        Angular.codec_roll.decode(string_roll_270.bits).require.value mustEqual 270f
      }

      "encode (0)" in {
        Angular.codec_roll.encode(0f).require.bytes mustEqual string_roll_0
      }

      "encode (90)" in {
        Angular.codec_roll.encode(90f).require.bytes mustEqual string_roll_90
      }

      "encode (180)" in {
        Angular.codec_roll.encode(180f).require.bytes mustEqual string_roll_180
      }

      "encode (270)" in {
        Angular.codec_roll.encode(270f).require.bytes mustEqual string_roll_270
      }
    }

    "pitch" should {
      val string_pitch_0 = hex"00"
      val string_pitch_90 = hex"60"
      val string_pitch_180 = hex"40"
      val string_pitch_270 = hex"20"

      "decode (0)" in {
        Angular.codec_pitch.decode(string_pitch_0.bits).require.value mustEqual 0f
      }

      "decode (90)" in {
        Angular.codec_pitch.decode(string_pitch_90.bits).require.value mustEqual 90f
      }

      "decode (180)" in {
        Angular.codec_pitch.decode(string_pitch_180.bits).require.value mustEqual 180f
      }

      "decode (270)" in {
        Angular.codec_pitch.decode(string_pitch_270.bits).require.value mustEqual 270f
      }

      "encode (0)" in {
        Angular.codec_pitch.encode(0f).require.bytes mustEqual string_pitch_0
      }

      "encode (90)" in {
        Angular.codec_pitch.encode(90f).require.bytes mustEqual string_pitch_90
      }

      "encode (180)" in {
        Angular.codec_pitch.encode(180f).require.bytes mustEqual string_pitch_180
      }

      "encode (270)" in {
        Angular.codec_pitch.encode(270f).require.bytes mustEqual string_pitch_270
      }
    }

    "yaw, normal" should {
      val string_pitch_0 = hex"00"
      val string_pitch_90 = hex"60"
      val string_pitch_180 = hex"40"
      val string_pitch_270 = hex"20"
      val string_yaw_0 = hex"20"
      val string_yaw_90 = hex"00"
      val string_yaw_180 = hex"60"
      val string_yaw_270 = hex"40"

      "decode (0)" in {
        Angular.codec_yaw(0f).decode(string_yaw_0.bits).require.value mustEqual 270f
      }

      "decode (90)" in {
        Angular.codec_yaw(0f).decode(string_yaw_90.bits).require.value mustEqual 0f
      }

      "decode (180)" in {
        Angular.codec_yaw(0f).decode(string_yaw_180.bits).require.value mustEqual 90f
      }

      "decode (270)" in {
        Angular.codec_yaw(0f).decode(string_yaw_270.bits).require.value mustEqual 180f
      }

      "encode (0)" in {
        Angular.codec_yaw(0f).encode(0f).require.bytes mustEqual string_pitch_0
      }

      "encode (90)" in {
        Angular.codec_yaw(0f).encode(90f).require.bytes mustEqual string_pitch_90
      }

      "encode (180)" in {
        Angular.codec_yaw(0f).encode(180f).require.bytes mustEqual string_pitch_180
      }

      "encode (270)" in {
        Angular.codec_yaw(0f).encode(270f).require.bytes mustEqual string_pitch_270
      }
    }

    "yaw, North-corrected" should {
      val string_yaw_0 = hex"20"
      val string_yaw_90 = hex"00"
      val string_yaw_180 = hex"60"
      val string_yaw_270 = hex"40"

      "decode (0)" in {
        Angular.codec_yaw().decode(string_yaw_0.bits).require.value mustEqual 0f
      }

      "decode (90)" in {
        Angular.codec_yaw().decode(string_yaw_90.bits).require.value mustEqual 90f
      }

      "decode (180)" in {
        Angular.codec_yaw().decode(string_yaw_180.bits).require.value mustEqual 180f
      }

      "decode (270)" in {
        Angular.codec_yaw().decode(string_yaw_270.bits).require.value mustEqual 270f
      }

      "encode (0)" in {
        Angular.codec_yaw().encode(0f).require.bytes mustEqual string_yaw_0
      }

      "encode (90)" in {
        Angular.codec_yaw().encode(90f).require.bytes mustEqual string_yaw_90
      }

      "encode (180)" in {
        Angular.codec_yaw().encode(180f).require.bytes mustEqual string_yaw_180
      }

      "encode (270)" in {
        Angular.codec_yaw().encode(270f).require.bytes mustEqual string_yaw_270
      }
    }
  }
}
