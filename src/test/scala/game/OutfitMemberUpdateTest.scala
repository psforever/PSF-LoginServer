// Copyright (c) 2023-2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.OutfitMemberUpdate
import org.specs2.mutable._
import scodec.bits._

class OutfitMemberUpdateTest extends Specification {

  val updateRankToOwnerOfOutfitInFormation = hex"91 0100ff7f15aa7a02f0"
  val normalRankChange                     = hex"91 1219000086d9130090"

  "decode updateOwnerOfOutfitInFormation" in {
    PacketCoding.decodePacket(updateRankToOwnerOfOutfitInFormation).require match {
      case OutfitMemberUpdate(outfit_id, char_id, rank, flag) =>
        outfit_id mustEqual 2147418113
        char_id mustEqual 41593365
        rank mustEqual 7
        flag mustEqual true
      case _ =>
        ko
    }
  }

  "encode updateOwnerOfOutfitInFormation" in {
    val msg = OutfitMemberUpdate(outfit_id = 2147418113, char_id = 41593365, rank = 7, flag = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual updateRankToOwnerOfOutfitInFormation
  }

  "decode normalRankChange" in {
    PacketCoding.decodePacket(normalRankChange).require match {
      case OutfitMemberUpdate(outfit_id, char_id, rank, flag) =>
        outfit_id mustEqual 6418
        char_id mustEqual 1300870
        rank mustEqual 4
        flag mustEqual true
      case _ =>
        ko
    }
  }

  "encode normalRankChange" in {
    val msg = OutfitMemberUpdate(outfit_id = 6418, char_id = 1300870, rank = 4, flag = true)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual normalRankChange
  }
}
