// Copyright (c) 2017 PSForever
package net.psforever.packet

import java.security.{Key, SecureRandom, Security}

import javax.crypto.Cipher
import javax.crypto.spec.RC5ParameterSpec
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import scodec.{Attempt, Codec, DecodeResult, Err}
import scodec.codecs.{bytes, uint16L, uint8L}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scodec.bits.ByteVector
import net.psforever.util.Md5Mac

object PacketCoding {
  Security.addProvider(new BouncyCastleProvider)

  private val random = new SecureRandom()

  val RC5_BLOCK_SIZE = 8

  /** A lower bound on the packet size */
  final val PLANETSIDE_MIN_PACKET_SIZE = 1

  /**
    * Transform a kind of packet into the sequence of data that represents it.
    * Wraps around the encoding process for all valid packet container types.
    * @param packet the packet to encode
    * @param sequence the packet's sequence number. Must be set for all non ControlPacket packets (but always for encrypted packets).
    * @param crypto if set, encrypt final payload
    * @return a `BitVector` translated from the packet's data
    */
  def marshalPacket(
      packet: PlanetSidePacket,
      sequence: Option[Int] = None,
      crypto: Option[CryptoCoding] = None
  ): Attempt[BitVector] = {
    val seq = packet match {
      case _: PlanetSideControlPacket if crypto.isEmpty => BitVector.empty
      case _ =>
        sequence match {
          case Some(sequence) =>
            uint16L.encode(sequence) match {
              case Successful(seq) => seq
              case f @ Failure(_)  => return f
            }
          case None =>
            return Failure(Err(s"Missing sequence"))
        }
    }

    val (flags, payload) = packet match {
      case _: PlanetSideGamePacket | _: PlanetSideControlPacket if crypto.isDefined =>
        encodePacket(packet) match {
          case Successful(payload) =>
            val encryptedPayload = crypto.get.encrypt(payload.bytes) match {
              case Successful(p) => p
              case f: Failure    => return f
            }
            (
              PlanetSidePacketFlags.codec.encode(PlanetSidePacketFlags(PacketType.Normal, secured = true)).require,
              // encrypted packets need to be aligned to 4 bytes before encryption/decryption
              // first byte are flags, second is the sequence, and third is the pad
              hex"00".bits ++ encryptedPayload.bits
            )
          case f @ Failure(_) => return f
        }
      case packet: PlanetSideGamePacket =>
        encodePacket(packet) match {
          case Successful(payload) =>
            (
              PlanetSidePacketFlags.codec.encode(PlanetSidePacketFlags(PacketType.Normal, secured = false)).require,
              payload
            )
          case f @ Failure(_) => return f
        }
      case packet: PlanetSideControlPacket =>
        encodePacket(packet) match {
          case Successful(payload) =>
            (
              // control packets don't have flags
              BitVector.empty,
              payload
            )
          case f @ Failure(_) => return f
        }
      case packet: PlanetSideCryptoPacket =>
        encodePacket(packet) match {
          case Successful(payload) =>
            (
              PlanetSidePacketFlags.codec.encode(PlanetSidePacketFlags(PacketType.Crypto, secured = false)).require,
              payload
            )
          case f @ Failure(_) => return f
        }
    }

    Successful(flags ++ seq ++ payload)
  }

  /**
    * Transform a `PlanetSidePacket` into its `BitVector` representation.
    * @param packet the packet to encode
    * @return a `BitVector` translated from the packet's data
    */
  def encodePacket(packet: PlanetSidePacket): Attempt[BitVector] = {
    packet.encode match {
      case Successful(payload) =>
        packet match {
          case _: PlanetSideCryptoPacket => Successful(payload)
          case packet: PlanetSideControlPacket =>
            ControlPacketOpcode.codec.encode(packet.opcode) match {
              case Successful(opcode) => Successful(hex"00".bits ++ opcode ++ payload)
              case f @ Failure(_)     => f
            }
          case packet: PlanetSideGamePacket =>
            GamePacketOpcode.codec.encode(packet.opcode) match {
              case Successful(opcode) => Successful(opcode ++ payload)
              case f @ Failure(_)     => f
            }
        }
      case f @ Failure(_) => f
    }
  }

  /**
    * Transforms `ByteVector` data into a PlanetSide packet.
    * Attempt to decode with an optional header and required payload.
    * Does not decode into a `GamePacket`.
    * @param msg the raw packet
    * @param crypto CryptoCoding instance for packet decryption, if this is a encrypted packet
    * @param cryptoState the current state of the connection's crypto. This is only used when decoding
    *                    crypto packets as they do not have opcodes
    * @return `PlanetSidePacketContainer`
    */
  def unmarshalPacket(
      msg: ByteVector,
      crypto: Option[CryptoCoding] = None,
      cryptoState: CryptoPacketOpcode.Type = CryptoPacketOpcode.Ignore
  ): Attempt[(PlanetSidePacket, Option[Int])] = {
    if (msg.length < PLANETSIDE_MIN_PACKET_SIZE) {
      Failure(Err(s"Packet does not meet the minimum length of $PLANETSIDE_MIN_PACKET_SIZE bytes"))
    } else {
      msg(0) match {
        // ControlPacket
        case 0x00 => decodePacket(msg).map(p => (p, None))
        // either encrypted payload or CryptoPacket
        case _ => unmarshalFlaggedPacket(msg, cryptoState, crypto).map { case (p, s) => (p, Some(s)) }
      }
    }
  }

  /**
    * Handle decoding for a packet that has been identified as not a `ControlPacket`.
    * It may just be encrypted (`EncryptedPacket`) or it may be involved in the encryption process itself (`CryptoPacket`).
    * @param msg the packet
    * @param cryptoState the current cryptographic state
    * @return a `PlanetSidePacketContainer`
    */
  private def unmarshalFlaggedPacket(
      msg: ByteVector,
      cryptoState: CryptoPacketOpcode.Type,
      crypto: Option[CryptoCoding] = None
  ): Attempt[(PlanetSidePacket, Int)] = {
    val (flags, remainder) = Codec.decode[PlanetSidePacketFlags](BitVector(msg)) match {
      case Successful(DecodeResult(value, remainder)) => (value, remainder)
      case Failure(e)                                 => return Failure(Err(s"Failed to parse packet flags: ${e.message}"))
    }

    flags.packetType match {
      case PacketType.Normal =>
        // support normal packets only if they are encrypted
        if (!flags.secured) {
          return Failure(Err("Unsupported packet type: normal packets must be encryped"))
        }
      case PacketType.Crypto =>
        if (flags.secured && crypto.isEmpty) {
          return Failure(Err("Unsupported packet type: crypto packets must be unencrypted"))
        }
      case _ =>
        return Failure(Err(s"Unsupported packet type: ${flags.packetType.toString}"))
    }

    // all packets have a two byte sequence ID
    val (sequence, payload) = uint16L.decode(remainder) match {
      case Successful(DecodeResult(value, remainder)) =>
        (value, remainder.toByteVector)
      case Failure(e) =>
        return Failure(Err(s"Failed to parse packet sequence number: ${e.message}"))
    }

    (flags.packetType, crypto) match {
      case (PacketType.Crypto, _) =>
        CryptoPacketOpcode
          .getPacketDecoder(cryptoState)(payload.bits)
          .map(p => (p.value.asInstanceOf[PlanetSidePacket], sequence))
      case (PacketType.Normal, Some(crypto)) if flags.secured =>
        // encrypted payload is 4-byte aligned: 1b flags, 2b sequence, 1b padding
        crypto.decrypt(payload.drop(1)).map(p => decodePacket(p)).flatten.map(p => (p, sequence))
      case (PacketType.Normal, None) if !flags.secured =>
        decodePacket(payload).map(p => (p, sequence))
      case (PacketType.Normal, None) =>
        Failure(Err(s"Cannot unmarshal encrypted packet without CryptoCoding"))
    }

  }

  /**
    * Transforms `ByteVector` data into a PlanetSide packet.
    * Similar to the `UnmarshalPacket` but it does not process packet headers.
    * It supports `GamePacket` in exchange for not supporting `CryptoPacket` (like `UnMarshalPacket`).
    * Mostly used in tests.
    * @param msg raw, unencrypted packet
    * @return `PlanetSidePacket`
    * @see `UnMarshalPacket`
    */
  def decodePacket(msg: ByteVector): Attempt[PlanetSidePacket] = {
    if (msg.length < PLANETSIDE_MIN_PACKET_SIZE)
      return Failure(Err(s"Packet does not meet the minimum length of $PLANETSIDE_MIN_PACKET_SIZE bytes"))
    val firstByte = msg { 0 }
    firstByte match {
      case 0x00 =>
        // control packets don't need the first byte
        ControlPacketOpcode.codec.decode(msg.drop(1).bits) match {
          case Successful(op) =>
            ControlPacketOpcode.getPacketDecoder(op.value)(op.remainder) match {
              case Successful(p) => Successful(p.value)
              case Failure(e)    => Failure(Err(e.messageWithContext))
            }
          case Failure(e) => Failure(Err(e.message))
        }
      case _ =>
        GamePacketOpcode.codec.decode(msg.bits) match {
          case Successful(opcode) =>
            GamePacketOpcode.getPacketDecoder(opcode.value)(opcode.remainder) match {
              case Failure(e) =>
                Failure(Err(f"Failed to parse game packet 0x${opcode.value.id}%02x: " + e.messageWithContext))
              case Successful(p) => Successful(p.value)
            }
          case Failure(e) => Failure(Err("Failed to decode game packet's opcode: " + e.message))
        }
    }
  }

  case class CryptoCoding(
      rc5EncryptionKey: Key,
      rc5DecryptionKey: Key,
      macEncryptionKey: ByteVector,
      macDecryptionKey: ByteVector
  ) {
    private val iv         = BigInt(64, random)
    private val rc5Spec    = new RC5ParameterSpec(0, 16, 32)
    private val rc5Encrypt = Cipher.getInstance("RC5/ECB/NoPadding")
    private val rc5Decrypt = Cipher.getInstance("RC5/ECB/NoPadding")
    rc5Encrypt.init(Cipher.ENCRYPT_MODE, rc5EncryptionKey, rc5Spec)
    rc5Decrypt.init(Cipher.DECRYPT_MODE, rc5DecryptionKey, rc5Spec)

    def encrypt(packet: PlanetSidePacket): Attempt[ByteVector] = {
      encodePacket(packet) match {
        case Successful(data) =>
          encrypt(data.toByteVector)
        case f @ Failure(_) =>
          f
      }
    }

    def encrypt(
        data: ByteVector
    ): Attempt[ByteVector] = {
      // This is basically X9.23 padding, except that the length byte is -1 because it doesn't count itself
      val packetNoPadding   = data ++ new Md5Mac(macEncryptionKey).updateFinal(data) // opcode, payload, and MAC
      val remainder         = packetNoPadding.length % RC5_BLOCK_SIZE
      val paddingNeeded     = RC5_BLOCK_SIZE - remainder - 1 // minus 1 because of a mandatory padding byte
      val paddingEncoded    = uint8L.encode(paddingNeeded.toInt).require
      val packetWithPadding = packetNoPadding ++ ByteVector.fill(paddingNeeded)(0x00) ++ paddingEncoded.toByteVector
      // raw packets plus MAC, padded to the nearest 8 byte boundary
      try {
        Successful(ByteVector.view(rc5Encrypt.doFinal(packetWithPadding.toArray)))
      } catch {
        case e: Throwable => Failure(Err(s"encrypt error: '${e.getMessage}' data: ${packetWithPadding.toHex}"))
      }
    }

    def decrypt(data: ByteVector): Attempt[ByteVector] = {
      val payloadDecrypted =
        try {
          ByteVector.view(rc5Decrypt.doFinal(data.toArray))
        } catch {
          case e: Throwable => return Failure(Err(e.getMessage))
        }

      // last byte is the padding length
      val padding = uint8L.decode(payloadDecrypted.takeRight(1).bits) match {
        case Successful(padding) => padding.value
        case Failure(e)          => return Failure(Err(s"Failed to decode the encrypted padding length: ${e.message}"))
      }

      val payloadNoPadding = payloadDecrypted.dropRight(1 + padding)
      val payloadMac       = payloadNoPadding.takeRight(Md5Mac.MACLENGTH)

      val mac = bytes(Md5Mac.MACLENGTH).decode(payloadMac.bits) match {
        case Failure(e)      => return Failure(Err("Failed to extract the encrypted MAC: " + e.message))
        case Successful(mac) => mac.value
      }

      val payloadNoMac = payloadNoPadding.dropRight(Md5Mac.MACLENGTH)
      val computedMac  = new Md5Mac(macDecryptionKey).updateFinal(payloadNoMac)

      if (!Md5Mac.verifyMac(computedMac, mac)) {
        return Failure(Err("Invalid packet MAC"))
      }

      if (payloadNoMac.length < PLANETSIDE_MIN_PACKET_SIZE) {
        return Failure(
          Err(s"Decrypted packet does not meet the minimum length of $PLANETSIDE_MIN_PACKET_SIZE bytes")
        )
      }

      Successful(payloadNoMac)
    }

  }
}
