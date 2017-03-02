// Copyright (c) 2016 PSForever.net to present

import org.specs2.mutable._
import net.psforever.crypto.CryptoInterface
import net.psforever.crypto.CryptoInterface.CryptoDHState
import scodec.bits._

class CryptoInterfaceTest extends Specification { args(stopOnFail = true)
  "Crypto interface" should {
    "correctly initialize" in {
      CryptoInterface.initialize()
      ok
    }

    "encrypt and decrypt" in {
      val key = hex"41414141"
      val plaintext = ByteVector.fill(16)(0x42)

      val crypto = new CryptoInterface.CryptoState(key, key)

      val ciphertext = crypto.encrypt(plaintext)
      val decrypted = crypto.decrypt(ciphertext)

      crypto.close
      decrypted mustEqual plaintext
      ciphertext mustNotEqual plaintext
    }

    "encrypt and decrypt must handle no bytes" in {
      val key = hex"41414141"
      val empty = ByteVector.empty

      val crypto = new CryptoInterface.CryptoState(key, key)

      val ciphertext = crypto.encrypt(empty)
      val decrypted = crypto.decrypt(ciphertext)

      crypto.close

      ciphertext mustEqual empty
      decrypted mustEqual empty
    }

    "encrypt and decrypt must only accept block aligned inputs" in {
      val key = hex"41414141"
      val badPad = ByteVector.fill(CryptoInterface.RC5_BLOCK_SIZE-1)('a')

      val crypto = new CryptoInterface.CryptoState(key, key)

      crypto.encrypt(badPad) must throwA[IllegalArgumentException]
      crypto.decrypt(badPad) must throwA[IllegalArgumentException]

      crypto.close
      ok
    }

    "arrive at a shared secret" in {
      val server = new CryptoInterface.CryptoDHState()
      val client = new CryptoInterface.CryptoDHState()

      // 1. Client generates p, g, and its key pair
      client.start()

      // 2. Client sends p and g to server who then generates a key pair as well
      server.start(client.getModulus, client.getGenerator)

      // 3. Both parties come to a shared secret
      val clientAgreed = client.agree(server.getPublicKey)
      val serverAgreed = server.agree(client.getPublicKey)

      // Free resources
      server.close
      client.close

      clientAgreed mustEqual serverAgreed
    }

    "must fail to agree on a secret with a bad public key" in {
      val server = new CryptoInterface.CryptoDHState()
      val client = new CryptoInterface.CryptoDHState()

      // 1. Client generates p, g, and its key pair
      client.start()

      // 2. Client sends p and g to server who then generates a key pair as well
      server.start(client.getModulus, client.getGenerator)

      // 3. Client agrees with a bad public key, so it must fail
      val clientAgreed = client.agree(client.getPublicKey)
      val serverAgreed = server.agree(client.getPublicKey)

      // Free resources
      server.close
      client.close

      clientAgreed mustNotEqual serverAgreed
    }

    "MD5MAC correctly" in {
      val key = hex"377b60f8790f91b35a9da82945743da9"
      val message = ByteVector(Array[Byte]('m', 'a', 's', 't', 'e', 'r', ' ', 's', 'e', 'c', 'r', 'e', 't')) ++
        hex"b4aea1559444a20b6112a2892de40eac00000000c8aea155b53d187076b79abab59001b600000000"
      val expected = hex"5aa15de41f5220cf5cca489155e1438c5aa15de4"

      val output = CryptoInterface.MD5MAC(key, message, expected.length.toInt)

      output mustEqual expected
    }

    "safely handle multiple starts" in {
      val dontCare = ByteVector.fill(16)(0x42)
      val dh = new CryptoDHState()

      dh.start()
      dh.start() must throwA[IllegalStateException]
      dh.close

      ok
    }

    "prevent function calls before initialization" in {
      val dontCare = ByteVector.fill(16)(0x42)
      val dh = new CryptoDHState()

      dh.getGenerator must throwA[IllegalStateException]
      dh.getModulus must throwA[IllegalStateException]
      dh.getPrivateKey must throwA[IllegalStateException]
      dh.getPublicKey must throwA[IllegalStateException]
      dh.agree(dontCare) must throwA[IllegalStateException]
      dh.close

      ok
    }
  }
}
