// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, TransactionType}
import scodec.bits._

class ItemTransactionMessageTest extends Specification {
  val string_buy = hex"44 4C03 4000110070756E6973686572000000"
  val string_sell = hex"44 5303 60001000004E00"
  val string_forget = hex"44 BA00 600011006861726173736572000000"

  "decode (buy)" in {
    PacketCoding.DecodePacket(string_buy).require match {
      case ItemTransactionMessage(terminal_guid, transaction_type, item_page, item_name, unk1, item_guid) =>
        terminal_guid mustEqual PlanetSideGUID(844)
        transaction_type mustEqual TransactionType.Buy
        item_page mustEqual 0
        item_name mustEqual "punisher"
        unk1 mustEqual 0
        item_guid mustEqual PlanetSideGUID(0)
      case _ =>
        ko
    }
  }

  "decode (sell)" in {
    PacketCoding.DecodePacket(string_sell).require match {
      case ItemTransactionMessage(terminal_guid, transaction_type, item_page, item_name, unk1, item_guid) =>
        terminal_guid mustEqual PlanetSideGUID(851)
        transaction_type mustEqual TransactionType.Sell
        item_page mustEqual 0
        item_name mustEqual ""
        unk1 mustEqual 0
        item_guid mustEqual PlanetSideGUID(78)
      case _ =>
        ko
    }
  }

  "decode (forget)" in {
    PacketCoding.DecodePacket(string_forget).require match {
      case ItemTransactionMessage(terminal_guid, transaction_type, item_page, item_name, unk1, item_guid) =>
        terminal_guid mustEqual PlanetSideGUID(186)
        transaction_type mustEqual TransactionType.Sell
        item_page mustEqual 0
        item_name mustEqual "harasser"
        unk1 mustEqual 0
        item_guid mustEqual PlanetSideGUID(0)
      case _ =>
        ko
    }
  }

  "encode (buy)" in {
    val msg_buy = ItemTransactionMessage(PlanetSideGUID(844), TransactionType.Buy, 0, "punisher", 0, PlanetSideGUID(0))
    val pkt_buy = PacketCoding.EncodePacket(msg_buy).require.toByteVector
    pkt_buy mustEqual string_buy
  }

  "encode (sell)" in {
    val msg_sell = ItemTransactionMessage(PlanetSideGUID(851), TransactionType.Sell, 0, "", 0, PlanetSideGUID(78))
    val pkt_sell = PacketCoding.EncodePacket(msg_sell).require.toByteVector

    pkt_sell mustEqual string_sell
  }

  "encode (forget)" in {
    val msg_forget = ItemTransactionMessage(PlanetSideGUID(186), TransactionType.Sell, 0, "harasser", 0, PlanetSideGUID(0))
    val pkt_forget = PacketCoding.EncodePacket(msg_forget).require.toByteVector

    pkt_forget mustEqual string_forget
  }
}
