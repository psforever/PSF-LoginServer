// Copyright (c) 2016 PSForever.net to present
package net.psforever.crypto

import net.psforever.IFinalizable
import sna.Library
import com.sun.jna.Pointer
import scodec.bits.ByteVector

object CryptoInterface {
  final val libName = "pscrypto"
  final val fullLibName = libName
  final val psLib = new Library(libName)

  final val RC5_BLOCK_SIZE = 8
  final val MD5_MAC_SIZE = 16

  val functionsList = List(
    "RC5_Init",
    "RC5_Encrypt",
    "RC5_Decrypt",
    "DH_Start",
    "DH_Start_Generate",
    "DH_Agree",
    "MD5_MAC",
    "Free_DH",
    "Free_RC5"
  )

  /**
    * Used to initialize the crypto library at runtime. This allows
    */
  def initialize() : Unit = {
    functionsList foreach psLib.prefetch
  }

  /**
   * Used for debugging object loading
   */
  def printEnvironment() : Unit = {
    import java.io.File

    val classpath = System.getProperty("java.class.path")
    val classpathEntries = classpath.split(File.pathSeparator)

    val myLibraryPath = System.getProperty("user.dir")
    println("User dir: " + myLibraryPath)
    classpathEntries.foreach(println)

    println("Required data model: " + System.getProperty("sun.arch.data.model"))
  }

  def MD5MAC(key : ByteVector, message : ByteVector, bytesWanted : Int) : ByteVector = {
    val out = Array.ofDim[Byte](bytesWanted)

    // WARNING BUG: the function must be cast to something (even if void) otherwise it doesnt work
    val ret = psLib.MD5_MAC(key.toArray, key.length, message.toArray, message.length, out, out.length)[Boolean]

    if(!ret)
      throw new Exception("MD5MAC failed to process")

    ByteVector(out)
  }

  /**
    * Checks if two MAC values are the same in constant time, preventing a timing attack for MAC forgery
 *
    * @param mac1
    * @param mac2
    */
  def verifyMAC(mac1 : ByteVector, mac2 : ByteVector) : Boolean = {
    var okay = true

    if(mac1.length != mac2.length)
      return false

    for(i <- 0 until mac1.length.toInt) {
      okay = okay && mac1{i} == mac2{i}
    }

    okay
  }

  class CryptoDHState extends IFinalizable {
    var started = false
    // these types MUST be Arrays of bytes for JNA to work
    val privateKey = Array.ofDim[Byte](16)
    val publicKey = Array.ofDim[Byte](16)
    val p = Array.ofDim[Byte](16)
    val g = Array.ofDim[Byte](16)
    var dhHandle = Pointer.NULL

    def start(modulus : ByteVector, generator : ByteVector) : Unit = {
      assertNotClosed

      if(started)
        throw new IllegalStateException("DH state has already been started")

      dhHandle = psLib.DH_Start(modulus.toArray, generator.toArray, privateKey, publicKey)[Pointer]

      if(dhHandle == Pointer.NULL)
        throw new Exception("DH initialization failed!")

      modulus.copyToArray(p, 0)
      generator.copyToArray(g, 0)

      started = true
    }

    def start() : Unit = {
      assertNotClosed

      if(started)
        throw new IllegalStateException("DH state has already been started")

      dhHandle = psLib.DH_Start_Generate(privateKey, publicKey, p, g)[Pointer]

      if(dhHandle == Pointer.NULL)
        throw new Exception("DH initialization failed!")

      started = true
    }

    def agree(otherPublicKey : ByteVector) = {
      if(!started)
        throw new IllegalStateException("DH state has not been started")

      val agreedValue = Array.ofDim[Byte](16)
      val agreed = psLib.DH_Agree(dhHandle, agreedValue, privateKey, otherPublicKey.toArray)[Boolean]

      if(!agreed)
        throw new Exception("Failed to DH agree")

      ByteVector.view(agreedValue)
    }

    private def checkAndReturnView(array : Array[Byte]) = {
      if(!started)
        throw new IllegalStateException("DH state has not been started")

      ByteVector.view(array)
    }

    def getPrivateKey = {
      checkAndReturnView(privateKey)
    }

    def getPublicKey = {
      checkAndReturnView(publicKey)
    }

    def getModulus = {
      checkAndReturnView(p)
    }

    def getGenerator = {
      checkAndReturnView(g)
    }

    override def close = {
      if(started) {
        psLib.Free_DH(dhHandle)[Unit]
        started = false
      }

      super.close
    }
  }

  class CryptoState(val decryptionKey : ByteVector,
                    val encryptionKey : ByteVector) extends IFinalizable {
    // Note that the keys must be returned as primitive Arrays for JNA to work
    val encCryptoHandle = psLib.RC5_Init(encryptionKey.toArray, encryptionKey.length, true)[Pointer]
    val decCryptoHandle = psLib.RC5_Init(decryptionKey.toArray, decryptionKey.length, false)[Pointer]

    if(encCryptoHandle == Pointer.NULL)
      throw new Exception("Encryption initialization failed!")

    if(decCryptoHandle == Pointer.NULL)
      throw new Exception("Decryption initialization failed!")

    def encrypt(plaintext : ByteVector) : ByteVector = {
      if(plaintext.length % RC5_BLOCK_SIZE != 0)
        throw new IllegalArgumentException(s"input must be padded to the nearest $RC5_BLOCK_SIZE byte boundary")

      val ciphertext = Array.ofDim[Byte](plaintext.length.toInt)

      val ret = psLib.RC5_Encrypt(encCryptoHandle, plaintext.toArray, plaintext.length, ciphertext)[Boolean]

      if(!ret)
        throw new Exception("Failed to encrypt plaintext")

      ByteVector.view(ciphertext)
    }

    def decrypt(ciphertext : ByteVector) : ByteVector = {
      if(ciphertext.length % RC5_BLOCK_SIZE != 0)
        throw new IllegalArgumentException(s"input must be padded to the nearest $RC5_BLOCK_SIZE byte boundary")

      val plaintext = Array.ofDim[Byte](ciphertext.length.toInt)

      val ret = psLib.RC5_Decrypt(decCryptoHandle, ciphertext.toArray, ciphertext.length, plaintext)[Boolean]

      if(!ret)
        throw new Exception("Failed to decrypt ciphertext")

      ByteVector.view(plaintext)
    }

    override def close = {
      psLib.Free_RC5(encCryptoHandle)[Unit]
      psLib.Free_RC5(decCryptoHandle)[Unit]
      super.close
    }
  }

  class CryptoStateWithMAC(decryptionKey : ByteVector,
                           encryptionKey : ByteVector,
                           val decryptionMACKey : ByteVector,
                           val encryptionMACKey : ByteVector) extends CryptoState(decryptionKey, encryptionKey) {
    /**
      * Performs a MAC operation over the message. Used when encrypting packets
 *
      * @param message
      * @return ByteVector
      */
    def macForEncrypt(message : ByteVector) : ByteVector = {
      MD5MAC(encryptionMACKey, message, MD5_MAC_SIZE)
    }

    /**
      * Performs a MAC operation over the message. Used when verifying decrypted packets
 *
      * @param message
      * @return ByteVector
      */
    def macForDecrypt(message : ByteVector) : ByteVector = {
      MD5MAC(decryptionMACKey, message, MD5_MAC_SIZE)
    }

    /**
      * MACs the plaintext message, encrypts it, and then returns the encrypted message with the
      * MAC appended to the end.
 *
      * @param message Arbitrary set of bytes
      * @return ByteVector
      */
    def macAndEncrypt(message : ByteVector) : ByteVector = {
      encrypt(message) ++ MD5MAC(encryptionMACKey, message, MD5_MAC_SIZE)
    }
  }

}
