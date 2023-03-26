// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{StatisticalCategory, StatisticalElement}
import scodec.bits._

class AvatarStatisticsMessageTest extends Specification {
  val string_long = hex"7F 4 00000000 0"
  val string_complex =
    hex"7F 01 3C 40 20 00 00 00  C0 00 00 00 00 00 00 00  20 00 00 00 20 00 00 00  40 00 00 00 00 00 00 00  00 00 00 00"

  "decode (long)" in {
    PacketCoding.decodePacket(string_long).require match {
      case AvatarStatisticsMessage(stat) =>
        stat match {
          case DeathStatistic(value) =>
            value mustEqual 0L
          case _ =>
            ko
        }
      case _ =>
        ko
    }
  }

  "decode (complex)" in {
    PacketCoding.decodePacket(string_complex).require match {
      case AvatarStatisticsMessage(stat) =>
        stat match {
          case CampaignStatistic(a, b, c) =>
            a mustEqual StatisticalCategory.Destroyed
            b mustEqual StatisticalElement.Mosquito
            c mustEqual List(1, 6, 0, 1, 1, 2, 0, 0)
          case _ =>
            ko
        }
      case _ =>
        ko
    }
  }

  "encode (long)" in {
    val msg = AvatarStatisticsMessage(DeathStatistic(0L))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_long
  }

  "encode (complex)" in {
    val msg = AvatarStatisticsMessage(
      CampaignStatistic(StatisticalCategory.Destroyed, StatisticalElement.Mosquito, List[Long](1, 6, 0, 1, 1, 2, 0, 0))
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_complex
  }

  "encode (failure; complex; wrong number of list entries)" in {
    val msg = AvatarStatisticsMessage(
      CampaignStatistic(StatisticalCategory.Destroyed, StatisticalElement.Mosquito, List[Long](1, 6, 0, 1))
    )
    PacketCoding.encodePacket(msg).isFailure mustEqual true
  }
}
