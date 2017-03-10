// Copyright (c) 2017 PSForever
package net.psforever.crypto

import com.sun.jna.ptr.IntByReference
import net.psforever.IFinalizable
import sna.Library
import com.sun.jna.{NativeLibrary, Pointer}
import scodec.bits.ByteVector

object CryptoInterface {
  final val libName = "pscrypto"
  final val fullLibName = libName
  final val PSCRYPTO_VERSION_MAJOR = 1
  final val PSCRYPTO_VERSION_MINOR = 1

  /**
     NOTE: this is a single, global shared library for the entire server's crypto needs

     Unfortunately, access to this object didn't used to be synchronized. I noticed that
     tests for this module were hanging ("arrive at a shared secret" & "must fail to agree on
     a secret..."). This heisenbug was responsible for failed Travis test runs and developer
     issues as well. Using Windows minidumps, I tracked the issue to a single thread deep in
     pscrypto.dll. It appeared to be executing an EB FE instruction (on Intel x86 this is
     `jmp $-2` or jump to self), which is an infinite loop. The stack trace made little to no
     sense and after banging my head on the wall for many hours, I assumed that something deep
     in CryptoPP, the libgcc libraries, or MSVC++ was the cause (or myself). Now all access to
     pscrypto functions that allocate and deallocate memory (DH_Start, RC5_Init) are synchronized.
     This *appears* to have fixed the problem.
  */
  final val psLib = new Library(libName)

  final val RC5_BLOCK_SIZE = 8
  final val MD5_MAC_SIZE = 16

  val functionsList = List(
    "PSCrypto_Init",
    "PSCrypto_Get_Version",
    "PSCrypto_Version_String",
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
    * Used to initialize the crypto library at runtime. The version is checked and
    * all functions are mapped.
    */
  def initialize() : Unit = {
    // preload all library functions for speed
    functionsList foreach psLib.prefetch

    val libraryMajor = new IntByReference
    val libraryMinor = new IntByReference

    psLib.PSCrypto_Get_Version(libraryMajor, libraryMinor)[Unit]

    if(!psLib.PSCrypto_Init(PSCRYPTO_VERSION_MAJOR, PSCRYPTO_VERSION_MINOR)[Boolean]) {
      throw new IllegalArgumentException(s"Invalid PSCrypto library version ${libraryMajor.getValue}.${libraryMinor.getValue}. Expected " +
        s"$PSCRYPTO_VERSION_MAJOR.$PSCRYPTO_VERSION_MINOR")
    }
  }

  /**
   * Used for debugging object loading
   */
  def printEnvironment() : Unit = {
    import java.io.File

    val classpath = System.getProperty("java.class.path")
    val classpathEntries = classpath.split(File.pathSeparator)

    val myLibraryPath = System.getProperty("user.dir")
    val jnaLibrary = System.getProperty("jna.library.path")
    val javaLibrary = System.getProperty("java.library.path")
    println("User dir: " + myLibraryPath)
    println("JNA Lib: " + jnaLibrary)
    println("Java Lib: " + javaLibrary)
    print("Classpath: ")
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
    * Checks if two Message Authentication Codes are the same in constant time,
    * preventing a timing attack for MAC forgery
    *
    * @param mac1 A MAC value
    * @param mac2 Another MAC value
    */
  def verifyMAC(mac1 : ByteVector, mac2 : ByteVector) : Boolean = {
    var okay = true

    // prevent byte by byte guessing
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

      psLib.synchronized {
        dhHandle = psLib.DH_Start(modulus.toArray, generator.toArray, privateKey, publicKey)[Pointer]
      }

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

      psLib.synchronized {
        dhHandle = psLib.DH_Start_Generate(privateKey, publicKey, p, g)[Pointer]
      }

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
        // TODO: zero private key material
        psLib.synchronized {
          psLib.Free_DH(dhHandle)[Unit]
        }
        started = false
      }

      super.close
    }
  }

  class CryptoState(val decryptionKey : ByteVector,
                    val encryptionKey : ByteVector) extends IFinalizable {
    // Note that the keys must be returned as primitive Arrays for JNA to work
    var encCryptoHandle : Pointer = Pointer.NULL
    var decCryptoHandle : Pointer = Pointer.NULL

    psLib.synchronized {
      encCryptoHandle = psLib.RC5_Init(encryptionKey.toArray, encryptionKey.length, true)[Pointer]
      decCryptoHandle = psLib.RC5_Init(decryptionKey.toArray, decryptionKey.length, false)[Pointer]
    }

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
      psLib.synchronized {
        psLib.Free_RC5(encCryptoHandle)[Unit]
        psLib.Free_RC5(decCryptoHandle)[Unit]
      }
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
      * @param message the input message
      * @return ByteVector
      */
    def macForEncrypt(message : ByteVector) : ByteVector = {
      MD5MAC(encryptionMACKey, message, MD5_MAC_SIZE)
    }

    /**
      * Performs a MAC operation over the message. Used when verifying decrypted packets
      *
      * @param message the input message
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
