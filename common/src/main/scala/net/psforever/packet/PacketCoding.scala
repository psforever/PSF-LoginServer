// Copyright (c) 2017 PSForever
package net.psforever.packet

import net.psforever.crypto.CryptoInterface
import scodec.Attempt.{Failure, Successful}
import scodec.bits._
import scodec.{Attempt, Codec, Err}
import scodec.codecs.{bytes, uint16L, uint8L}

/**
  * Base trait of the packet container `case class`.
  */
sealed trait PlanetSidePacketContainer

/**
  * An encrypted packet contains the following:
  * a sequence;
  * an encrypted opcode;
  * an encrypted payload;
  * and an implicit MD5MAC plus padding.
  * @param sequenceNumber na
  * @param payload the packet data
  */
final case class EncryptedPacket(sequenceNumber : Int,
                                 payload : ByteVector) extends PlanetSidePacketContainer

/**
  * A crypto packet contains the following:
  * a sequence;
  * and, a payload.
  * These packets have no opcodes and they rely on implicit state to decode properly.
  * @param sequenceNumber na
  * @param packet the packet data
  */
final case class CryptoPacket(sequenceNumber : Int,
                              packet : PlanetSideCryptoPacket) extends PlanetSidePacketContainer

/**
  * A game packet is prefaced by a byte that determines the type of packet and how to interpret the data.
  * This is important for decoding and encoding.
  * @param opcode a byte that identifies the packet
  * @param sequenceNumber na
  * @param packet the packet data
  */
final case class GamePacket(opcode : GamePacketOpcode.Value,
                            sequenceNumber : Int,
                            packet : PlanetSideGamePacket) extends PlanetSidePacketContainer

/**
  * A control packet is prefaced with a zero'd byte (`00`) followed by a special byte opcode for the type of control packet.
  * @param opcode a byte that identifies the packet
  * @param packet the packet data
  */
final case class ControlPacket(opcode : ControlPacketOpcode.Value,
                               packet : PlanetSideControlPacket) extends PlanetSidePacketContainer

object PacketCoding {
  /**
    * Access to the `ControlPacket` constructor.
    * @param packet a `PlanetSideControlPacket`
    * @return a `ControlPacket`
    */
  def CreateControlPacket(packet : PlanetSideControlPacket) = ControlPacket(packet.opcode, packet)

  /**
    * Access to the `CryptoPacket` constructor.
    * @param sequence na
    * @param packet a `PlanetSideCryptoPacket`
    * @return a `CryptoPacket`
    */
  def CreateCryptoPacket(sequence : Int, packet : PlanetSideCryptoPacket) = CryptoPacket(sequence, packet)

  /**
    * Access to the `GamePacket` constructor.
    * @param sequence na
    * @param packet a `PlanetSideGamePacket`
    * @return a `GamePacket`
    */
  def CreateGamePacket(sequence : Int, packet : PlanetSideGamePacket) = GamePacket(packet.opcode, sequence, packet)

/* Marshalling and Encoding. */

  /**
    * Transform a kind of packet into the sequence of data that represents it.
    * Wraps around the encoding process for all valid packet container types.
    * @param packet the packet to encode
    * @return a `BitVector` translated from the packet's data
    */
  def MarshalPacket(packet : PlanetSidePacketContainer) : Attempt[BitVector] = {
    var flagsEncoded : BitVector = BitVector.empty //flags before everything in packet
    var seqEncoded : BitVector = BitVector.empty //control packets have a sequence number
    var paddingEncoded : BitVector = BitVector.empty //encrypted packets need to be aligned in a certain way
    var payloadEncoded : BitVector = BitVector.empty //the packet itself as bits and bytes
    var controlPacket = false
    var sequenceNum = 0
    //packet flags
    var secured = false
    var packetType = PacketType.Crypto

    packet match {
      case GamePacket(_, seq, payload) =>
        packetType = PacketType.Normal
        sequenceNum = seq
        EncodePacket(payload) match {
          case f @ Failure(_) => return f
          case Successful(p) => payloadEncoded = p
        }

      case ControlPacket(_, payload) =>
        controlPacket = true
        EncodePacket(payload) match {
          case f @ Failure(_) => return f
          case Successful(p) => payloadEncoded = p
        }

      case CryptoPacket(seq, payload) =>
        packetType = PacketType.Crypto
        sequenceNum = seq
        EncodePacket(payload) match {
          case f @ Failure(_) => return f
          case Successful(p) => payloadEncoded = p
        }

      case EncryptedPacket(seq, payload) =>
        secured = true
        packetType = PacketType.Normal
        sequenceNum = seq
        //encrypted packets need to be aligned to 4 bytes before encryption/decryption
        //first byte are flags, second is the sequence, and third is the pad
        paddingEncoded = hex"00".bits
        payloadEncoded = payload.bits
    }

    //crypto packets DON'T have flags
    if(!controlPacket) {
      val flags = PlanetSidePacketFlags(packetType, secured = secured)
      flagsEncoded = PlanetSidePacketFlags.codec.encode(flags).require
      uint16L.encode(sequenceNum) match {
        case Failure(e) => return Attempt.failure(Err(s"Failed to marshal sequence in packet $packet: " + e.messageWithContext))
        case Successful(p) => seqEncoded = p
      }
    }
    Attempt.successful(flagsEncoded ++ seqEncoded ++ paddingEncoded ++ payloadEncoded)
  }

  /**
    * Overloaded method for transforming a `ControlPacket` into its `BitVector` representation.
    * @param packet the control packet to encode
    * @return a `BitVector` translated from the packet's data
    */
  def EncodePacket(packet : PlanetSideControlPacket) : Attempt[BitVector] = {
    val opcode = packet.opcode
    var opcodeEncoded = BitVector.empty
    ControlPacketOpcode.codec.encode(opcode) match {
      case Failure(e) => return Attempt.failure(Err(s"Failed to marshal opcode in control packet $opcode: " + e.messageWithContext))
      case Successful(p) => opcodeEncoded = p
    }
    var payloadEncoded = BitVector.empty
    encodePacket(packet) match {
      case Failure(e) => return Attempt.failure(Err(s"Failed to marshal control packet $packet: " + e.messageWithContext))
      case Successful(p) => payloadEncoded = p
    }
    Attempt.Successful(hex"00".bits ++ opcodeEncoded ++ payloadEncoded)
  }

  /**
    * Overloaded method for transforming a `CryptoPacket` into its `BitVector` representation.
    * @param packet the crypto packet to encode
    * @return a `BitVector` translated from the packet's data
    */
  def EncodePacket(packet : PlanetSideCryptoPacket) : Attempt[BitVector] = {
    encodePacket(packet) match {
      case Failure(e) => Attempt.failure(Err(s"Failed to marshal crypto packet $packet: " + e.messageWithContext))
      case s @ Successful(_) => s
    }
  }

  /**
    * Overloaded method for transforming a `GamePacket` into its `BitVector` representation.
    * @param packet the game packet to encode
    * @return a `BitVector` translated from the packet's data
    */
  def EncodePacket(packet : PlanetSideGamePacket) : Attempt[BitVector] = {
    val opcode = packet.opcode
    var opcodeEncoded = BitVector.empty
    GamePacketOpcode.codec.encode(opcode) match {
      case Failure(e) => return Attempt.failure(Err(s"Failed to marshal opcode in game packet $opcode: " + e.messageWithContext))
      case Successful(p) => opcodeEncoded = p
    }
    var payloadEncoded = BitVector.empty
    encodePacket(packet) match {
      case Failure(e) => return Attempt.failure(Err(s"Failed to marshal game packet $packet: " + e.messageWithContext))
      case Successful(p) => payloadEncoded = p
    }
    Attempt.Successful(opcodeEncoded ++ payloadEncoded)
  }

  /**
    * Calls the packet's own `encode` function.
    * Lowest encode call before the packet-specific implementations.
    * @param packet the packet to encode
    * @return a `BitVector` translated from the packet's data
    */
  private def encodePacket(packet : PlanetSidePacket) : Attempt[BitVector] = packet.encode

/* Unmarshalling and Decoding. */

  /**
    * A lower bound on the packet size
    */
  final val PLANETSIDE_MIN_PACKET_SIZE = 1

  /**
    * Transforms `ByteVector` data into a PlanetSide packet.
    * Attempt to decode with an optional header and required payload.
    * Does not decode into a `GamePacket`.
    * @param msg the raw packet
    * @param cryptoState the current state of the connection's crypto. This is only used when decoding
    *                    crypto packets as they do not have opcodes
    * @return `PlanetSidePacketContainer`
    */
  def UnmarshalPacket(msg : ByteVector, cryptoState : CryptoPacketOpcode.Type) : Attempt[PlanetSidePacketContainer] = {
    if(msg.length < PLANETSIDE_MIN_PACKET_SIZE)
      return Attempt.failure(Err(s"Packet does not meet the minimum length of $PLANETSIDE_MIN_PACKET_SIZE bytes"))

    val firstByte = msg{0}
    firstByte match {
      case 0x00 => unmarshalControlPacket(msg.drop(1)) //control packets dont need the first byte
      case _ => unmarshalFlaggedPacket(msg, cryptoState) //either EncryptedPacket or CryptoPacket
    }
  }

  /**
    * Helper function to decode a packet without specifying a crypto state.
    * Used when there is no crypto state available such as in tests.
    * @param msg packet data bytes
    * @return `PlanetSidePacketContainer`
    */
  def UnmarshalPacket(msg : ByteVector) : Attempt[PlanetSidePacketContainer] = UnmarshalPacket(msg, CryptoPacketOpcode.Ignore)

  /**
    * Handle decoding for a packet that has been identified as not a `ControlPacket`.
    * It may just be encrypted (`EncryptedPacket`) or it may be involved in the encryption process itself (`CryptoPacket`).
    * @param msg the packet
    * @param cryptoState the current cryptographic state
    * @return a `PlanetSidePacketContainer`
    */
  private def unmarshalFlaggedPacket(msg : ByteVector, cryptoState : CryptoPacketOpcode.Type) : Attempt[PlanetSidePacketContainer] = {
    val decodedFlags = Codec.decode[PlanetSidePacketFlags](BitVector(msg)) //get the flags
    decodedFlags match {
      case Failure(e) =>
        return Attempt.failure(Err("Failed to parse packet flags: " + e.message))
      case _ =>
    }
    val flags = decodedFlags.require.value
    val packetType = flags.packetType
    packetType match {
      case PacketType.Normal =>
        if(!flags.secured) { //support normal packets only if they are encrypted
          return Attempt.failure(Err("Unsupported packet type: normal packets must be encryped"))
        }

      case PacketType.Crypto =>
        if(flags.secured) { //support crypto packets only if they are not encrypted
          return Attempt.failure(Err("Unsupported packet type: crypto packets must be unencrypted"))
        }

      case _ =>
        return Attempt.failure(Err("Unsupported packet type: " + flags.packetType.toString))
    }
    //all packets have a two byte sequence ID
    val decodedSeq = uint16L.decode(decodedFlags.require.remainder) //TODO: make this a codec for reuse
    decodedSeq match {
      case Failure(e) =>
        return Attempt.failure(Err("Failed to parse packet sequence number: " + e.message))
      case _ =>
    }
    val sequence = decodedSeq.require.value
    val payload = decodedSeq.require.remainder.toByteVector
    packetType match {
      case PacketType.Crypto =>
        unmarshalCryptoPacket(cryptoState, sequence, payload)
      case PacketType.Normal =>
        unmarshalEncryptedPacket(sequence, payload.drop(1)) //payload is 4-byte aligned
    }
  }

  /**
    * Handle decoding for a `ControlPacket`.
    * @param msg the packet
    * @return a `ControlPacket`
    */
  private def unmarshalControlPacket(msg : ByteVector) : Attempt[ControlPacket] = {
    DecodeControlPacket(msg) match {
      case f @ Failure(_) =>
        f
      case Successful(p) =>
        Attempt.successful(CreateControlPacket(p))
    }
  }

  /**
    * Handle decoding for a `GamePacket`.
    * @param sequence na
    * @param msg the packet data
    * @return a `GamePacket`
    */
  private def unmarshalGamePacket(sequence : Int, msg : ByteVector) : Attempt[GamePacket] = {
   DecodeGamePacket(msg) match {
      case f @ Failure(_) =>
        f
      case Successful(p) =>
        Attempt.successful(CreateGamePacket(sequence, p))
    }
  }

  /**
    * Handle decoding for a `CryptoPacket`.
    * @param state the current cryptographic state
    * @param sequence na
    * @param payload the packet data
    * @return a `CryptoPacket`
    */
  private def unmarshalCryptoPacket(state : CryptoPacketOpcode.Type, sequence : Int, payload : ByteVector) : Attempt[CryptoPacket] = {
    CryptoPacketOpcode.getPacketDecoder(state)(payload.bits) match {
      case Successful(a) =>
        Attempt.successful(CryptoPacket(sequence, a.value))
      case Failure(e) =>
        Attempt.failure(e.pushContext("unmarshal_crypto_packet"))
    }
  }

  /**
    * Handle decoding for an `EncryptedPacket`.
    * The payload is already encrypted.
    * Just repackage the data.
    * @param sequence na
    * @param payload the packet data
    * @return an `EncryptedPacket`
    */
  private def unmarshalEncryptedPacket(sequence : Int, payload : ByteVector) : Attempt[EncryptedPacket] = {
    Attempt.successful(EncryptedPacket(sequence, payload))
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
  def DecodePacket(msg : ByteVector) : Attempt[PlanetSidePacket] = {
    if(msg.length < PLANETSIDE_MIN_PACKET_SIZE)
      return Attempt.failure(Err(s"Packet does not meet the minimum length of $PLANETSIDE_MIN_PACKET_SIZE bytes"))
    val firstByte = msg{0}
    firstByte match {
      case 0x00 => DecodeControlPacket(msg.drop(1)) //control packets dont need the first byte
      case _ => DecodeGamePacket(msg)
    }
  }

  /**
    * Transform a `ByteVector` into a `ControlPacket`.
    * @param msg the the raw data to decode
    * @return a `PlanetSideControlPacket`
    */
  def DecodeControlPacket(msg : ByteVector) : Attempt[PlanetSideControlPacket] = {
    ControlPacketOpcode.codec.decode(msg.bits) match {
      case Failure(e) =>
        Attempt.failure(Err("Failed to decode control packet's opcode: " + e.message))

      case Successful(op) =>
        ControlPacketOpcode.getPacketDecoder(op.value)(op.remainder) match {
          case Failure(e) =>
            Attempt.failure(Err(f"Failed to parse control packet ${op.value}: " + e.messageWithContext))

          case Successful(p) =>
            Attempt.successful(p.value)
        }
    }
  }

  /**
    * Transform a `ByteVector` into a `GamePacket`.
    * @param msg the the raw data to decode
    * @return a `PlanetSideGamePacket`
    */
  def DecodeGamePacket(msg : ByteVector) : Attempt[PlanetSideGamePacket] = {
    GamePacketOpcode.codec.decode(msg.bits) match {
      case Failure(e) =>
        Attempt.failure(Err("Failed to decode game packet's opcode: " + e.message))

      case Successful(opcode) =>
        GamePacketOpcode.getPacketDecoder(opcode.value)(opcode.remainder) match {
          case Failure(e) =>
            Attempt.failure(Err(f"Failed to parse game packet 0x${opcode.value.id}%02x: " + e.messageWithContext))

          case Successful(p) =>
            Attempt.successful(p.value)
        }
    }
  }

/* Encrypting and Decrypting. */

  /**
    * Transform the privileged `packet` into a `RawPacket` representation to get:
    * the sequence number,
    * and the raw `ByteVector` data.
    * @param packet the unencrypted packet
    * @return paired data based on the packet
    */
  def getPacketDataForEncryption(packet : PlanetSidePacketContainer) : Attempt[(Int, ByteVector)] = {
    makeRawPacket(packet) match {
      case Successful(rawPacket) =>
        val sequenceNumber = packet match { //the sequence is variable if this is a GamePacket
          case GamePacket(_, seq, _) => seq
          case _ => 0
        }
        Successful((sequenceNumber, rawPacket.toByteVector))

      case f @ Failure(_) =>
        f
    }
  }

  /**
    * Encrypt the provided packet using the provided cryptographic state.
    * Translate the packet into data to send on to the actual encryption process.
    * @param crypto the current cryptographic state
    * @param packet the unencrypted packet
    * @return an `EncryptedPacket`
    */
  def encryptPacket(crypto : CryptoInterface.CryptoStateWithMAC, packet : PlanetSidePacketContainer) : Attempt[EncryptedPacket] = {
    getPacketDataForEncryption(packet) match {
      case Successful((seq, data)) =>
        encryptPacket(crypto, seq, data)
      case f @ Failure(_) =>
        f
    }
  }

  /**
    * Transform either a `GamePacket` or a `ControlPacket` into a `BitVector`.
    * This is not as thorough as the process of unmarshalling though the results are very similar.
    * @param packet a packet
    * @return a `BitVector` that represents the packet
    */
  def makeRawPacket(packet : PlanetSidePacketContainer) : Attempt[BitVector] = packet match {
    case GamePacket(opcode, _, payload) =>
      val opcodeEncoded = GamePacketOpcode.codec.encode(opcode)
      opcodeEncoded match {
        case Failure(e) =>
          Attempt.failure(Err(s"Failed to marshal opcode in packet $opcode: " + e.message))
        case _ =>
          encodePacket(payload) match {
            case Failure(e) => Attempt.failure(Err(s"Failed to marshal packet $opcode: " + e.messageWithContext))
            case Successful(p) => Attempt.successful(opcodeEncoded.require ++ p)
          }
      }

    case ControlPacket(opcode, payload) =>
      val opcodeEncoded = ControlPacketOpcode.codec.encode(opcode)
      opcodeEncoded match {
        case Failure(e) =>
          Attempt.failure(Err(s"Failed to marshal opcode in packet $opcode: " + e.messageWithContext))
        case _ =>
          encodePacket(payload) match {
            case Failure(e) => Attempt.failure(Err(s"Failed to marshal packet $opcode: " + e.messageWithContext))
            case Successful(p) => Attempt.successful(hex"00".bits ++ opcodeEncoded.require ++ p)
          }
      }

    case _ =>
      throw new IllegalArgumentException("Unsupported packet container type")
  }

  /**
    * Perform encryption on the packet's raw data.
    * @param crypto the current cryptographic state
    * @param sequenceNumber na
    * @param rawPacket a `ByteVector` that represents the packet data
    * @return an `EncryptedPacket`
    */
  def encryptPacket(crypto : CryptoInterface.CryptoStateWithMAC, sequenceNumber : Int, rawPacket : ByteVector) : Attempt[EncryptedPacket] = {
    val packetMac = crypto.macForEncrypt(rawPacket)
    val packetNoPadding = rawPacket ++ packetMac //opcode, payload, and MAC
    val remainder = packetNoPadding.length % CryptoInterface.RC5_BLOCK_SIZE
    val paddingNeeded = CryptoInterface.RC5_BLOCK_SIZE - remainder - 1 //minus 1 because of a mandatory padding bit
    val paddingEncoded = uint8L.encode(paddingNeeded.toInt).require
    val packetWithPadding = packetNoPadding ++ ByteVector.fill(paddingNeeded)(0x00) ++ paddingEncoded.toByteVector
    val encryptedPayload = crypto.encrypt(packetWithPadding) //raw packets plus MAC, padded to the nearest 16 byte boundary
    Attempt.successful(EncryptedPacket(sequenceNumber, encryptedPayload))
  }

  /**
    * Perform decryption on an `EncryptedPacket`.
    * @param crypto the current state of the connection's crypto
    * @param packet an encrypted packet
    * @return a general packet container type
    */
  def decryptPacket(crypto : CryptoInterface.CryptoStateWithMAC, packet : EncryptedPacket) : Attempt[PlanetSidePacketContainer] = {
    decryptPacketData(crypto, packet) match {
      case Successful(payload) => unmarshalPayload(packet.sequenceNumber, payload)
      case f @ Failure(_) => f
    }
  }

  /**
    * Transform decrypted packet data into the type of packet that it represents.
    * Will not compose data into an `EncryptedPacket` or into a `CryptoPacket`.
    * @param sequenceNumber na
    * @param payload the decrypted packet data
    * @return a general packet container type
    */
  def unmarshalPayload(sequenceNumber : Int, payload : ByteVector) : Attempt[PlanetSidePacketContainer] = {
    payload{0} match {
      case 0x00 => unmarshalControlPacket(payload.drop(1))
      case _ => unmarshalGamePacket(sequenceNumber, payload)
    }
  }

  /**
    * Compose the decrypted payload data from a formerly encrypted packet.
    * @param crypto the current state of the connection's crypto
    * @param packet an encrypted packet
    * @return a sequence of decrypted data
    */
  def decryptPacketData(crypto : CryptoInterface.CryptoStateWithMAC, packet : EncryptedPacket) : Attempt[ByteVector] = {
    val payloadDecrypted = crypto.decrypt(packet.payload)
    val payloadJustLen = payloadDecrypted.takeRight(1) //get the last byte which is the padding length
    val padding = uint8L.decode(payloadJustLen.bits)
    padding match {
      case Failure(e) => return Attempt.failure(Err("Failed to decode the encrypted padding length: " + e.message))
      case _ =>
    }

    val macSize = CryptoInterface.MD5_MAC_SIZE
    val macDecoder = bytes(macSize)
    val payloadNoPadding = payloadDecrypted.dropRight(1 + padding.require.value)
    val payloadMac = payloadNoPadding.takeRight(macSize)
    /*
    println("Payload: " + packet.payload)
    println("DecPayload: " + payloadDecrypted)
    println("DecPayloadNoLen: " + payloadJustLen)
    println("Padding: " + padding.require.value)
    println("NoPadding: " + payloadNoPadding)
    println("Mac: " + payloadMac)
    println("NoMac: " + payloadNoMac)
    */
    val mac = macDecoder.decode(payloadMac.bits)
    mac match {
      case Failure(e) => return Attempt.failure(Err("Failed to extract the encrypted MAC: " + e.message))
      case _ =>
    }

    val payloadNoMac = payloadNoPadding.dropRight(macSize)
    val computedMac = crypto.macForDecrypt(payloadNoMac)
    if(!CryptoInterface.verifyMAC(computedMac, mac.require.value)) { //verify that the MAC matches
      throw new SecurityException("Invalid packet MAC")
    }
    if(payloadNoMac.length < PLANETSIDE_MIN_PACKET_SIZE) {
      return Attempt.failure(Err(s"Decrypted packet does not meet the minimum length of $PLANETSIDE_MIN_PACKET_SIZE bytes"))
    }
    Successful(payloadNoMac)
  }
}
