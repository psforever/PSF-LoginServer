// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import net.psforever.packet.control.SlottedMetaPacket
import scodec.bits.BitVector
import scodec.{Attempt, Codec, DecodeResult, Err}
import scodec.codecs._

object ControlPacketOpcode extends Enumeration {
  type Type = Value
  val

  // Opcodes should have a marker every 10
  // OPCODE 0
  HandleGamePacket, // a whoopsi case: not actually a control packet, but a game packet
  ClientStart, // first packet ever sent during client connection
  ServerStart, // second packet sent in response to ClientStart
  MultiPacket, // used to send multiple packets with one UDP message (subpackets limited to <= 255)
  Unknown4,
  Unknown5,
  Unknown6,
  Unknown7,
  Unknown8,
  SlottedMetaPacket0,

  // OPCODE 10
  SlottedMetaPacket1,
  SlottedMetaPacket2,
  SlottedMetaPacket3,
  SlottedMetaPacket4,
  SlottedMetaPacket5,
  SlottedMetaPacket6,
  SlottedMetaPacket7,
  RelatedA0,
  RelatedA1,
  RelatedA2,

  // OPCODE 20
  RelatedA3,
  RelatedB0,
  RelatedB1,
  RelatedB2,
  RelatedB3,
  AggregatePacket, // same as MultiPacket, but with the ability to send extended length packets
  Unknown26,
  Unknown27,
  Unknown28,
  ConnectionClose,

  // OPCODE 30
  Unknown30
  = Value

  def getPacketDecoder(opcode : ControlPacketOpcode.Type) : (BitVector) => Attempt[DecodeResult[PlanetSideControlPacket]] = {
    import net.psforever

    opcode match {
      case HandleGamePacket => control.HandleGamePacket.decode
      case ServerStart => control.ServerStart.decode
      case ClientStart => control.ClientStart.decode
      case MultiPacket => control.MultiPacket.decode
      case SlottedMetaPacket0 => SlottedMetaPacket.decode
      case ConnectionClose => control.ConnectionClose.decode
      case default => (a : BitVector) => Attempt.failure(Err(s"Could not find a marshaller for control packet ${opcode}"))
    }
  }

  implicit val codec: Codec[this.Value] = PacketHelpers.createEnumerationCodec(this, uint8L)
}
