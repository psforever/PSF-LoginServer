package psforever.net

import psforever.crypto.CryptoInterface
import psforever.crypto.CryptoInterface._
import scodec.Attempt.{Successful, Failure}
import scodec.bits._
import scodec.{DecodeResult, Err, Attempt, Codec}
import scodec.codecs.{uint16L, uint8L, uint4L, bytes}

// Packet containers
sealed trait PlanetSidePacketContainer

// A sequence, encrypted opcode, encrypted payload, and MD5MAC plus padding
final case class EncryptedPacket(sequenceNumber : Int,
                                 payload : ByteVector) extends PlanetSidePacketContainer

// A sequence, and payload. Crypto packets have no discernible opcodes
final case class CryptoPacket(sequenceNumber : Int,
                              packet : PlanetSideCryptoPacket) extends PlanetSidePacketContainer

final case class GamePacket(opcode : GamePacketOpcode.Value,
                            sequenceNumber : Int,
                            packet : PlanetSideGamePacket) extends PlanetSidePacketContainer

// Just an opcode + payload (does not expect a response)
final case class ControlPacket(opcode : ControlPacketOpcode.Value,
                               packet : PlanetSideControlPacket) extends PlanetSidePacketContainer

object PacketCoding {
  final val PLANETSIDE_MIN_PACKET_SIZE = 2

  def UnmarshalPacket(msg : ByteVector) : Attempt[PlanetSidePacketContainer] = {
    UnmarshalPacket(msg, CryptoPacketOpcode.Ignore)
  }

  def UnmarshalPacket(msg : ByteVector, cryptoState : CryptoPacketOpcode.Type) : Attempt[PlanetSidePacketContainer] = {
    // check for a minimum length
    if(msg.length < PLANETSIDE_MIN_PACKET_SIZE)
      return Attempt.failure(Err(s"Packet does not meet the minimum length of $PLANETSIDE_MIN_PACKET_SIZE bytes"))

    val firstByte = msg{0}

    firstByte match {
      // drop the first byte as control packets dont need it
      case 0x00 => unmarshalControlPacket(msg.drop(1))
      case _ => unmarshalFlaggedPacket(msg, cryptoState) // returns either EncryptedPacket or CryptoPacket
    }
  }

  def DecodePacket(msg : ByteVector) : Attempt[PlanetSidePacket] = {
    // check for a minimum length
    if(msg.length < PLANETSIDE_MIN_PACKET_SIZE)
      return Attempt.failure(Err(s"Packet does not meet the minimum length of $PLANETSIDE_MIN_PACKET_SIZE bytes"))

    val firstByte = msg{0}

    firstByte match {
      // drop the first byte as control packets dont need it
      case 0x00 => DecodeControlPacket(msg.drop(1))
      case _ => DecodeGamePacket(msg)
    }
  }

  def MarshalPacket(packet : PlanetSidePacketContainer) : Attempt[BitVector] = {
    var flagsEncoded : BitVector = BitVector.empty
    var seqEncoded : BitVector = BitVector.empty
    var paddingEncoded : BitVector = BitVector.empty
    var opcodeEncoded : BitVector = BitVector.empty
    var payloadEncoded : BitVector = BitVector.empty

    packet match {
      case GamePacket(opcode, seq, payload) =>
        val flags = PlanetSidePacketFlags(PacketType.Normal, secured = false)
        flagsEncoded = PlanetSidePacketFlags.codec.encode(flags).require

        GamePacketOpcode.codec.encode(opcode) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal opcode in packet $opcode: " + e.messageWithContext))
          case Successful(p) => opcodeEncoded = p
        }

        uint16L.encode(seq) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal sequence in packet $opcode: " + e.messageWithContext))
          case Successful(p) => seqEncoded = p
        }

        encodePacket(payload) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal packet $opcode: " + e.messageWithContext))
          case Successful(p) => payloadEncoded = p
        }
      case ControlPacket(opcode, payload) =>
        flagsEncoded = hex"00".bits

        ControlPacketOpcode.codec.encode(opcode) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal opcode in packet $opcode: " + e.messageWithContext))
          case Successful(p) => opcodeEncoded = p
        }

        encodePacket(payload) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal packet $opcode: " + e.messageWithContext))
          case Successful(p) => payloadEncoded = p
        }
      case CryptoPacket(seq, payload) =>
        val flags = PlanetSidePacketFlags(PacketType.Crypto, secured = false)
        flagsEncoded = PlanetSidePacketFlags.codec.encode(flags).require

        uint16L.encode(seq) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal sequence in packet $payload: " + e.messageWithContext))
          case Successful(p) => seqEncoded = p
        }

        encodePacket(payload) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal packet $payload: " + e.messageWithContext))
          case Successful(p) => payloadEncoded = p
        }
      case EncryptedPacket(seq, payload) =>
        val flags = PlanetSidePacketFlags(PacketType.Normal, secured = true)
        flagsEncoded = PlanetSidePacketFlags.codec.encode(flags).require

        // encrypted packets need to be aligned to 4 bytes before encryption/decryption
        // first byte are flags, second and third the sequence, and fourth is the pad
        paddingEncoded = hex"00".bits

        uint16L.encode(seq) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal sequence in packet $payload: " + e.messageWithContext))
          case Successful(p) => seqEncoded = p
        }

        payloadEncoded = payload.bits
    }

    val finalPacket = flagsEncoded ++ seqEncoded ++ paddingEncoded ++ opcodeEncoded ++ payloadEncoded
    Attempt.successful(finalPacket)
  }

  def CreateControlPacket(packet : PlanetSideControlPacket) = ControlPacket(packet.opcode, packet)
  def CreateCryptoPacket(sequence : Int, packet : PlanetSideCryptoPacket) = CryptoPacket(sequence, packet)
  def CreateGamePacket(sequence : Int, packet : PlanetSideGamePacket) = GamePacket(packet.opcode, sequence, packet)

  //////////////////////////////////////////////////////////////////////////////

  private def encodePacket(packet : PlanetSidePacket) : Attempt[BitVector] = packet.encode

  private def unmarshalFlaggedPacket(msg : ByteVector, cryptoState : CryptoPacketOpcode.Type) : Attempt[PlanetSidePacketContainer] = {
    val decodedFlags = Codec.decode[PlanetSidePacketFlags](BitVector(msg))

    decodedFlags match {
      case Failure(e) =>
        return Attempt.failure(Err("Failed to parse packet flags: " + e.message))
      case _ =>
    }

    val flags = decodedFlags.require.value
    val rest = decodedFlags.require.remainder
    val packetType = flags.packetType

    // perform a quick reject of weird packet types
    packetType match {
      case PacketType.Crypto => ;
      case PacketType.Normal => ;
      case default =>
        return Attempt.failure(Err("Unsupported packet type: " + flags.packetType.toString))
    }

    // we only support normal packets if they are encrypted
    if(packetType == PacketType.Normal && !flags.secured)
      return Attempt.failure(Err("Unsupported packet type: normal packets must be encryped"))

    // we only support crypto packets if they are not encrypted
    if(packetType == PacketType.Crypto && flags.secured)
      return Attempt.failure(Err("Unsupported packet type: crypto packets must be unencrypted"))

    // all packets have a two byte sequence ID
    // TODO: make this a codec for reuse
    val decodedSeq = uint16L.decode(rest)

    decodedSeq match {
      case Failure(e) =>
        return Attempt.failure(Err("Failed to parse packet sequence number: " + e.message))
      case _ =>
    }

    val sequence = decodedSeq.require.value
    var payload = decodedSeq.require.remainder.toByteVector

    // encrypted packets must be 4-byte aligned
    if(flags.secured) {
      payload = payload.drop(1)
    }

    packetType match {
      case PacketType.Crypto =>
        unmarshalCryptoPacket(cryptoState, sequence, payload)
      case PacketType.Normal =>
        unmarshalEncryptedPacket(sequence, payload)
    }
  }

  private def unmarshalControlPacket(msg : ByteVector) : Attempt[ControlPacket] = {
    val packet = DecodeControlPacket(msg)

    packet match {
      case f @ Failure(e) => f
      case Successful(p) =>
        Attempt.successful(CreateControlPacket(p))
    }
  }

  def DecodeControlPacket(msg : ByteVector) : Attempt[PlanetSideControlPacket] = {
    val opcode = ControlPacketOpcode.codec.decode(msg.bits)

    opcode match {
      case Failure(e) =>
        return Attempt.failure(Err("Failed to decode control packet's opcode: " + e.message))
      case _ =>
    }

    val packet = ControlPacketOpcode.getPacketDecoder(opcode.require.value)(opcode.require.remainder)

    packet match {
      case Failure(e) =>
        Attempt.failure(Err(f"Failed to parse control packet 0x${opcode.require.value.id}%02x: " + e.messageWithContext))
      case Successful(p) => Attempt.successful(p.value)
    }
  }

  private def unmarshalGamePacket(sequence : Int, msg : ByteVector) : Attempt[GamePacket] = {
    val packet = DecodeGamePacket(msg)

    packet match {
      case f @ Failure(e) => f
      case Successful(p) =>
        Attempt.successful(CreateGamePacket(sequence, p))
    }
  }

  def DecodeGamePacket(msg : ByteVector) : Attempt[PlanetSideGamePacket] = {
    val opcode = GamePacketOpcode.codec.decode(msg.bits)

    opcode match {
      case Failure(e) =>
        return Attempt.failure(Err("Failed to decode game packet's opcode: " + e.message))
      case _ =>
    }

    val packet = GamePacketOpcode.getPacketDecoder(opcode.require.value)(opcode.require.remainder)

    packet match {
      case Failure(e) =>
        Attempt.failure(Err(f"Failed to parse game packet 0x${opcode.require.value.id}%02x: " + e.messageWithContext))
      case Successful(p) => Attempt.successful(p.value)
    }
  }

  private def unmarshalCryptoPacket(state : CryptoPacketOpcode.Type, sequence : Int, payload : ByteVector) : Attempt[CryptoPacket] = {
    val packet = CryptoPacketOpcode.getPacketDecoder(state)(payload.bits)

    packet match {
      case Successful(a) =>
        Attempt.successful(CryptoPacket(sequence, a.value))
      case Failure(e) =>
        Attempt.failure(e.pushContext("unmarshal_crypto_packet"))
    }
  }

  private def unmarshalEncryptedPacket(sequence : Int, payload : ByteVector) : Attempt[EncryptedPacket] = {
    Attempt.successful(EncryptedPacket(sequence, payload))
  }

  ///////////////////////////////////////////////////////////
  // Packet Crypto
  ///////////////////////////////////////////////////////////

  def encryptPacket(crypto : CryptoInterface.CryptoStateWithMAC, packet : PlanetSidePacketContainer) : Attempt[EncryptedPacket] = {
    // TODO: this is bad. rework
    var sequenceNumber = 0

    val rawPacket : BitVector = packet match {
      case GamePacket(opcode, seq, payload) =>
        val opcodeEncoded = GamePacketOpcode.codec.encode(opcode)
        sequenceNumber = seq

        opcodeEncoded match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal opcode in packet $opcode: " + e.message))
          case _ =>
        }

        encodePacket(payload) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal packet $opcode: " + e.messageWithContext))
          case Successful(p) => opcodeEncoded.require ++ p
        }
      case ControlPacket(opcode, payload) =>
        val opcodeEncoded = ControlPacketOpcode.codec.encode(opcode)

        opcodeEncoded match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal opcode in packet $opcode: " + e.messageWithContext))
          case _ =>
        }

        encodePacket(payload) match {
          case Failure(e) => return Attempt.failure(Err(s"Failed to marshal packet $opcode: " + e.messageWithContext))
          case Successful(p) => hex"00".bits ++ opcodeEncoded.require ++ p
        }
      case default => throw new IllegalArgumentException("Unsupported packet container type")
    }

    val packetMac = crypto.macForEncrypt(rawPacket.toByteVector)

    // opcode, payload, and MAC
    val packetNoPadding = rawPacket.toByteVector ++ packetMac

    val remainder = packetNoPadding.length % CryptoInterface.RC5_BLOCK_SIZE

    // minus 1 because of the actual byte telling of the padding, which always has to be there
    val paddingNeeded = CryptoInterface.RC5_BLOCK_SIZE - remainder - 1
    val paddingEncoded = uint8L.encode(paddingNeeded).require

    val packetWithPadding = packetNoPadding ++ ByteVector.fill(paddingNeeded)(0x00) ++ paddingEncoded.toByteVector

    // raw packets plus MAC must be padded to the nearest 16 byte boundary
    val encryptedPayload = crypto.encrypt(packetWithPadding)

    Attempt.successful(EncryptedPacket(sequenceNumber, encryptedPayload))
  }

  def decryptPacket(crypto : CryptoInterface.CryptoStateWithMAC, packet : EncryptedPacket) : Attempt[PlanetSidePacketContainer] = {
    val payloadDecrypted = crypto.decrypt(packet.payload)

    // get the last byte which is the padding length
    val payloadJustLen = payloadDecrypted.takeRight(1)
    val padding = uint8L.decode(payloadJustLen.bits)

    padding match {
      case Failure(e) => return Attempt.failure(Err("Failed to decode the encrypted padding length: " + e.message))
      case _ =>
    }

    val macSize = CryptoInterface.MD5_MAC_SIZE
    val macDecoder = bytes(macSize)
    val payloadNoPadding = payloadDecrypted.dropRight(1 + padding.require.value)
    val payloadMac = payloadNoPadding.takeRight(macSize)
    val payloadNoMac = payloadNoPadding.dropRight(macSize)

    /*
    println("Payload: " + packet.payload)
    println("DecPayload: " + payloadDecrypted)
    println("DecPayloadNoLen: " + payloadJustLen)
    println("Padding: " + padding.require.value)
    println("NoPadding: " + payloadNoPadding)
    println("Mac: " + payloadMac)
    println("NoMac: " + payloadNoMac)*/


    val mac = macDecoder.decode(payloadMac.bits)

    mac match {
      case Failure(e) => return Attempt.failure(Err("Failed to extract the encrypted MAC: " + e.message))
      case _ =>
    }

    val computedMac = crypto.macForDecrypt(payloadNoMac)

    // verify that the MAC matches
    if(!CryptoInterface.verifyMAC(computedMac, mac.require.value))
      throw new SecurityException("Invalid packet MAC")

    if(payloadNoMac.length < PLANETSIDE_MIN_PACKET_SIZE) {
      return Attempt.failure(Err(s"Decrypted packet does not meet the minimum length of $PLANETSIDE_MIN_PACKET_SIZE bytes"))
    }

    val firstByte = payloadNoMac{0}

    firstByte match {
      case 0x00 => unmarshalControlPacket(payloadNoMac.drop(1))
      case _ => unmarshalGamePacket(packet.sequenceNumber, payloadNoMac)
    }
  }
}
