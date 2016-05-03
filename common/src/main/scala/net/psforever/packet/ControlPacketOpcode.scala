// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet

import scodec.bits.BitVector
import scodec.{Err, DecodeResult, Attempt, Codec}
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
      case HandleGamePacket => psforever.packet.HandleGamePacket.decode
      case ServerStart => psforever.packet.ServerStart.decode
      case ClientStart => psforever.packet.ClientStart.decode
      case MultiPacket => psforever.packet.MultiPacket.decode
      case SlottedMetaPacket0 => psforever.packet.SlottedMetaPacket.decode
      case ConnectionClose => psforever.packet.ConnectionClose.decode
      case default => (a : BitVector) => Attempt.failure(Err(s"Could not find a marshaller for control packet ${opcode}"))
    }
  }

  implicit val codec: Codec[this.Value] = PacketHelpers.createEnumerationCodec(this, uint8L)
}
