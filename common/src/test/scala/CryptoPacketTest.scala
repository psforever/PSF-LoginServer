// Copyright (c) 2017 PSForever
import org.specs2.mutable._
import net.psforever.packet.control.{ClientStart, ServerStart}
import net.psforever.packet.crypto.{ClientChallengeXchg, ClientFinished, ServerChallengeXchg, ServerFinished}
import scodec.Codec
import scodec.bits._

class CryptoPacketTest extends Specification {

  "PlanetSide crypto packet" in {
    val cNonce = 656287232

    "ClientStart" should {
      val string = hex"0000000200261e27000001f0".bits

      "decode" in {
        val res = Codec.decode[ClientStart](string)
        res.isSuccessful mustEqual true
        res.require.value.clientNonce mustEqual cNonce
      }

      "encode" in {
        val res = Codec.encode(ClientStart(cNonce))

        res.require mustEqual string
      }
    }

    "ServerStart" should {
      val sNonce = 3468803409L
      val string = hex"00261e2751bdc1ce000000000001d300000002".bits

      "decode" in {
        val res   = Codec.decode[ServerStart](string)
        val value = res.require.value

        value.clientNonce mustEqual cNonce
        value.serverNonce mustEqual sNonce
      }

      "encode" in {
        val res = Codec.encode(ServerStart(cNonce, sNonce))

        res.require mustEqual string
      }
    }

    "ClientChallengeXchg" should {
      val time        = hex"962d8453"
      val timeDecoded = scodec.codecs.uint32L.decode(time.bits).require.value
      val challenge   = hex"24f5997c c7d16031 d1f567e9"
      val p           = hex"f57511eb 8e5d1efb 8b7f3287 d5a18b17"
      val g           = hex"00000000 00000000 00000000 00000002"
      val string = (hex"0101" ++ time ++ challenge ++ hex"00 01 0002  ff 2400 00 1000" ++
        p ++ hex"1000" ++ g ++ hex"0000010307000000").bits

      "decode" in {
        val res   = Codec.decode[ClientChallengeXchg](string)
        val value = res.require.value

        value.time mustEqual timeDecoded
        value.challenge mustEqual challenge
        value.p mustEqual p
        value.g mustEqual g
      }

      "encode" in {
        val res = Codec.encode(ClientChallengeXchg(timeDecoded, challenge, p, g))

        res.require mustEqual string
      }
    }

    "ServerChallengeXchg" should {
      val time        = hex"962d8453"
      val timeDecoded = scodec.codecs.uint32L.decode(time.bits).require.value
      val challenge   = hex"1b0e6408 cd935ec2 429aeb58"
      val pubKey      = hex"51f83ce6 45e86c3e 79c8fc70 f6ddf14b"
      val string      = (hex"0201" ++ time ++ challenge ++ hex"00 01 03070000000c00   1000 " ++ pubKey ++ hex"0e").bits

      "decode" in {
        val res   = Codec.decode[ServerChallengeXchg](string)
        val value = res.require.value

        value.time mustEqual timeDecoded
        value.challenge mustEqual challenge
        value.pubKey mustEqual pubKey
      }

      "encode" in {
        val res = Codec.encode(ServerChallengeXchg(timeDecoded, challenge, pubKey))

        res.require mustEqual string
      }
    }

    "ClientFinished" should {
      val challengeResult = hex"ea3cf05d a5cb4256 8bb91aa7"
      val pubKey          = hex"eddc35f2 52b02d0e 496ba273 54578e73"
      val string          = (hex"10 1000" ++ pubKey ++ hex"0114 " ++ challengeResult).bits

      "decode" in {
        val res   = Codec.decode[ClientFinished](string)
        val value = res.require.value

        value.challengeResult mustEqual challengeResult
        value.pubKey mustEqual pubKey
      }

      "encode" in {
        val res = Codec.encode(ClientFinished(pubKey, challengeResult))

        res.require mustEqual string
      }
    }

    "ServerFinished" should {
      val challengeResult = hex"d64ffb8e 526311b4 af46bece"
      val string          = (hex"0114" ++ challengeResult).bits

      "decode" in {
        val res   = Codec.decode[ServerFinished](string)
        val value = res.require.value

        value.challengeResult mustEqual challengeResult
      }

      "encode" in {
        val res = Codec.encode(ServerFinished(challengeResult))

        res.require mustEqual string
      }
    }
  }
}
