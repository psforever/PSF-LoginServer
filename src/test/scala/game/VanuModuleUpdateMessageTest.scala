// Copyright (c) 2026 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.packets.{PulseState, ModuleInfo, VanuModuleUpdateMessage}
import net.psforever.types.Vector3
import scodec.bits._
import org.specs2.mutable.Specification

class VanuModuleUpdateMessageTest  extends Specification {
  val string = hex"C1 020000"
  val string_info = hex"c1 0200087000000000000100001140000000000006000008" //manufactured, not from packet captures

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case VanuModuleUpdateMessage(zone_number, info) =>
        zone_number mustEqual 2
        info.isEmpty mustEqual true
      case _ =>
        ko
    }
  }

  "decode (info)" in {
    PacketCoding.decodePacket(string_info).require match {
      case VanuModuleUpdateMessage(zone_number, info) =>
        zone_number mustEqual 2
        info.size mustEqual 2
        info.head match {
          case ModuleInfo(1, PulseState.Green, 0L, false, false, Vector3(2048, 2048, 0)) => ok
          case _ => ko
        }
        info(1) match {
          case ModuleInfo(5, PulseState.Red, 0L, false, false, Vector3(4098, 1024, 0)) => ok
          case _ => ko
        }
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = VanuModuleUpdateMessage(2, Nil)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  "encode (info)" in {
    val msg = VanuModuleUpdateMessage(2, List(
      ModuleInfo(module_type = 1, pulseGreen = true, u3 = 0L, Vector3(2048, 2048, 0)),
      ModuleInfo(module_type = 5, pulseGreen = false, u3 = 0L, Vector3(4098, 1024, 0))
    ))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_info
  }
}
