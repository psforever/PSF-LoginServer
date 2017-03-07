// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ObjectAttachMessageTest extends Specification {
  val stringToInventory = hex"2A 9F05 D405 86"
  val stringToCursor = hex"2A 9F05 D405 00FA"

  "decode (inventory 1,1)" in {
    PacketCoding.DecodePacket(stringToInventory).require match {
      case ObjectAttachMessage(player_guid, item_guid, index) =>
        player_guid mustEqual PlanetSideGUID(1439)
        item_guid mustEqual PlanetSideGUID(1492)
        index mustEqual 6
      case _ =>
        ko
    }
  }

  "decode (cursor)" in {
    PacketCoding.DecodePacket(stringToCursor).require match {
      case ObjectAttachMessage(player_guid, item_guid, index) =>
        player_guid mustEqual PlanetSideGUID(1439)
        item_guid mustEqual PlanetSideGUID(1492)
        index mustEqual 250
      case _ =>
        ko
    }
  }

  "encode (inventory 1,1)" in {
    val msg = ObjectAttachMessage(PlanetSideGUID(1439), PlanetSideGUID(1492), 6)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringToInventory
  }

  "encode (cursor)" in {
    val msg = ObjectAttachMessage(PlanetSideGUID(1439), PlanetSideGUID(1492), 250)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringToCursor
  }
}
