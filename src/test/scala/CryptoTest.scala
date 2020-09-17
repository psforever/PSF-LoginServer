// Copyright (c) 2017 PSForever

import java.security.{SecureRandom, Security}

import javax.crypto.spec.SecretKeySpec
import org.specs2.mutable._
import net.psforever.packet.PacketCoding
import net.psforever.packet.PacketCoding.CryptoCoding
import net.psforever.packet.control.{HandleGamePacket, SlottedMetaPacket}
import net.psforever.packet.game.PlanetsideAttributeMessage
import net.psforever.types.PlanetSideGUID
import net.psforever.util.{DiffieHellman, Md5Mac}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scodec.Attempt.Failure
import scodec.Err
import scodec.bits._

class CryptoTest extends Specification {
  Security.addProvider(new BouncyCastleProvider)

  args(stopOnFail = true)
  "Crypto" should {
    "encrypt and decrypt" in {
      val key       = hex"41414141414141414141414141414141"
      val keySpec   = new SecretKeySpec(key.take(20).toArray, "RC5")
      val plaintext = ByteVector.fill(32)(0x42)

      val crypto = CryptoCoding(keySpec, keySpec, key, key)

      val ciphertext = crypto.encrypt(plaintext).require
      val decrypted  = crypto.decrypt(ciphertext).require

      decrypted mustEqual plaintext
      ciphertext mustNotEqual plaintext

    }

    "encrypt and decrypt must only accept block aligned inputs" in {
      val key     = hex"41414141414141414141414141414141"
      val keySpec = new SecretKeySpec(key.take(20).toArray, "RC5")
      val badPad  = ByteVector.fill(PacketCoding.RC5_BLOCK_SIZE - 1)('a')

      val crypto = CryptoCoding(keySpec, keySpec, key, key)

      //crypto.encrypt(badPad) must throwA[javax.crypto.IllegalBlockSizeException]
      crypto.decrypt(badPad) mustEqual Failure(Err("data not block size aligned"))
    }

    "encrypt and decrypt packet" in {
      val key     = hex"41414141414141414141414141414141"
      val keySpec = new SecretKeySpec(key.take(20).toArray, "RC5")
      val crypto  = CryptoCoding(keySpec, keySpec, key, key)

      val packet =
        SlottedMetaPacket(
          0,
          5,
          PacketCoding
            .encodePacket(
              HandleGamePacket(
                PacketCoding.encodePacket(PlanetsideAttributeMessage(PlanetSideGUID(0), 0, 0L)).require.toByteVector
              )
            )
            .require
            .bytes
        )

      val encrypted = PacketCoding.marshalPacket(packet, Some(10), Some(crypto)).require
      println(s"encrypted ${encrypted}")
      val (decryptedPacket, sequence) = PacketCoding.unmarshalPacket(encrypted.bytes, Some(crypto)).require

      decryptedPacket mustEqual packet
      sequence must beSome(10)
    }

    "MD5MAC" in {
      val key = hex"377b60f8790f91b35a9da82945743da9"
      val message = ByteVector(Array[Byte]('m', 'a', 's', 't', 'e', 'r', ' ', 's', 'e', 'c', 'r', 'e', 't')) ++
        hex"b4aea1559444a20b6112a2892de40eac00000000c8aea155b53d187076b79abab59001b600000000"
      val message2 = ByteVector.view((0 until 64).map(_.toByte).toArray)
      val expected = hex"5aa15de41f5220cf5cca489155e1438c5aa15de4"

      val mac = new Md5Mac(key)
      mac.update(message)
      mac.doFinal(20) mustEqual expected

      val mac2 = new Md5Mac(key)
      mac2.update(message2)
      mac.update(message2)
      mac.doFinal() mustEqual mac2.doFinal()

      (0 to 20).map(_ => mac.updateFinal(message, 20) mustEqual expected)
    }

    "DH" in {
      val p = BigInt(128, new SecureRandom()).toByteArray
      val g = Array(1.toByte)

      val bob   = new DiffieHellman(p, g)
      val alice = new DiffieHellman(p, g)

      bob.agree(alice.publicKey) mustEqual alice.agree(bob.publicKey)
    }

  }
}
