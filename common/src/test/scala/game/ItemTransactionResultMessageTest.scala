// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.TransactionType
import scodec.bits._

class ItemTransactionResultMessageTest extends Specification {
  //these are paried packets come from the same capture
  val string_request = hex"44 DD 03 40 00 11 40 73 75 70 70 72 65 73 73 6F 72 00 00 00"
  val string_result = hex"45 DD 03 50 00"
  "decode" in {
    PacketCoding.DecodePacket(string_result).require match {
      case ItemTransactionResultMessage(terminal_guid, transaction_type, is_success, error_code) =>
        terminal_guid mustEqual PlanetSideGUID(989)
        transaction_type mustEqual TransactionType.Buy
        is_success mustEqual true
        error_code mustEqual 0
      case default =>
        ko
    }
  }

  "encode" in {
    val msg = ItemTransactionResultMessage(PlanetSideGUID(989), TransactionType.Buy, true, 0)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string_result
  }

  "proper reply" in {
    try {
      val request = PacketCoding.DecodePacket(string_request).require.asInstanceOf[ItemTransactionMessage]
      val result = PacketCoding.DecodePacket(string_result).require.asInstanceOf[ItemTransactionResultMessage]
      request.terminal_guid mustEqual result.terminal_guid
      request.transaction_type mustEqual result.transaction_type
    }
    catch {
      case e : Exception =>
        ko
    }
  }
}
