// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.PlatoonEvent
import net.psforever.packet.game.PlatoonEvent.PacketType._
import org.specs2.mutable._
import scodec.bits.ByteVector

class PlatoonEventTest extends Specification {

  val add_squad: ByteVector = ByteVector.fromValidHex("71 0200 0040 00")
  val remove_squad: ByteVector = ByteVector.fromValidHex("71 4280 0300 20")

  "decode addSquad" in {
    PacketCoding.decodePacket(add_squad).require match {
      case PlatoonEvent(action, unk0, squad_supplement_id, squad_ui_index) =>
        action mustEqual AddSquad
        unk0 mustEqual 8
        squad_supplement_id mustEqual 1
        squad_ui_index mustEqual 0
      case _ =>
        ko
    }
  }

  "encode addSquad" in {
    val msg = PlatoonEvent(
      AddSquad,
      unk0 = 8,
      squad_supplement_id = 1,
      squad_ui_index = 0
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual add_squad
  }

  "decode removeSquad" in {
    PacketCoding.decodePacket(remove_squad).require match {
      case PlatoonEvent(action, unk0, squad_supplement_id, squad_ui_index) =>
        action mustEqual RemoveSquad
        unk0 mustEqual 10
        squad_supplement_id mustEqual 12
        squad_ui_index mustEqual 2
      case _ =>
        ko
    }
  }

  "encode removeSquad" in {
    val msg = PlatoonEvent(
      RemoveSquad,
      unk0 = 10,
      squad_supplement_id = 12,
      squad_ui_index = 2
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual remove_squad
  }

}
