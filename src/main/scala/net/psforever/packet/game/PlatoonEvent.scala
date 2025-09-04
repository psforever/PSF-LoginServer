// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec}
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HNil}

final case class PlatoonEvent(
    packet_type: PlatoonEvent.PacketType.Type,  // only seen 0 and 1
    unk0: Int,                                  // squad size?
    squad_supplement_id: Int,
    squad_ui_index: Int
  ) extends PlanetSideGamePacket {
  type Packet = PlatoonEvent

  def opcode: Type = GamePacketOpcode.PlatoonEvent
  def encode: Attempt[BitVector] = PlatoonEvent.encode(this)
}

object PlatoonEvent extends Marshallable[PlatoonEvent] {

  object PacketType extends Enumeration {
    type Type = Value

    val AddSquad: PacketType.Value = Value(0) // Add / Update?
    val RemoveSquad: PacketType.Value = Value(1)
    val Unk2: PacketType.Value = Value(2)
    val Unk3: PacketType.Value = Value(3) // seen as decode error, Squad ID too high

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(2))
  }

  implicit val codec: Codec[PlatoonEvent] = (
    ("packet_type" | PacketType.codec) ::
      ("unk0" | uint16L) ::
      ("squad_supplement_id" | uint16L) ::
      ("squad_ui_index" | uintL(2))
    ).xmap[PlatoonEvent](
    {
      case packet_type :: u0 :: squad_supplement_id :: squad_ui_index :: HNil =>
        PlatoonEvent(packet_type, u0, squad_supplement_id, squad_ui_index)
    },
    {
      case PlatoonEvent(packet_type, u0, squad_supplement_id, squad_ui_index) =>
        packet_type :: u0 :: squad_supplement_id :: squad_ui_index :: HNil
    }
  )
}
