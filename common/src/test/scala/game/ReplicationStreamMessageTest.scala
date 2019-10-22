// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import scodec.bits._

class ReplicationStreamMessageTest extends Specification {
  val stringListClear = hex"E6 B9 FE"
  val stringListOne = hex"E6 B8 01 06 01 00 8B 46007200610067004C0041004E00640049004E004300 84 4600720061006700 0A00 00 00 0A FF"
  val stringListTwo = hex"E6 B8 01 06 06 00 8E 470065006E006500720061006C0047006F0072006700750074007A00 A1 46004C0059002C0041006C006C002000770065006C0063006F006D0065002C0063006E0020006C0061007300740020006E0069006700680074002100210021002100 0400 00 00 7A 01 83 02 00 45 80 4B004F004B006B006900610073004D00460043004E00 87 5300710075006100640020003200 0400 00 00 6A FF"
  val stringListThree = hex"E6 B8 01 06 06 00 8E 470065006E006500720061006C0047006F0072006700750074007A00 A1 46004C0059002C0041006C006C002000770065006C0063006F006D0065002C0063006E0020006C0061007300740020006E0069006700680074002100210021002100 0400 00 00 7A 01 83 01 80 4600 4E0049004700480054003800380052004100560045004E00 8B 41006C006C002000570065006C0063006F006D006500 0A 00 00 00 4A 02 83 02 00 45 80 4B004F004B006B006900610073004D00460043004E00 87 5300710075006100640020003200 0400 00 00 6A FF"
  val stringListRemove = hex"E6 20 A0 19 FE"
  val stringUpdateLeader = hex"E6 C0 28 08 C4 00 46006100740065004A0048004E004300 FF"
  val stringUpdateTask = hex"E6 C0 58 094E00 52004900500020005000530031002C0020007600690073006900740020005000530046006F00720065007600650072002E006E0065007400 FF"
  val stringUpdateContinent = hex"E6 C0 38 09 85000000 7F80"
  val stringUpdateSize = hex"E6 C0 18 0A 37 F8"
  val stringUpdateLeaderSize = hex"E6 C0 58 10 C3 00 4A0069006D006D0079006E00 43 FF"
  val stringUpdateTaskContinent = hex"E6 C0 58 11 40 80 3200 3 04000000 FF0"
  val stringUpdateAll = hex"E6 C0 78 30 58 0430 6D00610064006D0075006A00 80 040000000A FF"
  val stringRemoveUpdate = hex"e6 20201801014aff"

  "SquadInfo (w/out squad_guid)" in {
    val o = SquadInfo("FragLANdINC", "Frag", PlanetSideZoneID(10), 0, 10)
    o.leader.isDefined mustEqual true
    o.leader.get mustEqual "FragLANdINC"
    o.task.isDefined mustEqual true
    o.task.get mustEqual "Frag"
    o.zone_id.isDefined mustEqual true
    o.zone_id.get mustEqual PlanetSideZoneID(10)
    o.size.isDefined mustEqual true
    o.size.get mustEqual 0
    o.capacity.isDefined mustEqual true
    o.capacity.get mustEqual 10
    o.squad_guid.isDefined mustEqual false
  }

  "SquadInfo (capacity)" in {
    val o = SquadInfo(None, 7)
    o.leader.isDefined mustEqual false
    o.task.isDefined mustEqual false
    o.zone_id.isDefined mustEqual false
    o.size.isDefined mustEqual false
    o.capacity.isDefined mustEqual true
    o.capacity.get mustEqual 7
  }

  "SquadInfo (Add)" in {
    val o1 = SquadInfo(Some("FragLANdINC"), Some("Frag"), Some(PlanetSideZoneID(10)), None, None)
    val o2 = SquadInfo(Some(7), 10)
    val o3 = SquadInfo("FragLANdINC", "Frag", PlanetSideZoneID(10), 7, 10)
    o1.And(o2) mustEqual o3
  }

  "SquadInfo (Add, with blocked fields)" in {
    val o1 = SquadInfo(Some("FragLANdINC"), None, Some(PlanetSideZoneID(10)), None, Some(10))
    val o2 = SquadInfo(Some("Frag"), Some("Frag"), Some(PlanetSideZoneID(15)), Some(7), Some(7))
    val o3 = SquadInfo("FragLANdINC", "Frag", PlanetSideZoneID(10), 7, 10)
    o1.And(o2) mustEqual o3
  }

  "decode (clear)" in {
    PacketCoding.DecodePacket(stringListClear).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 5
        behavior2.isDefined mustEqual true
        behavior2.get mustEqual 6
        entries.length mustEqual 0
      case _ =>
        ko
    }
  }

  "decode (one)" in {
    PacketCoding.DecodePacket(stringListOne).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 5
        behavior2.get mustEqual 6
        entries.length mustEqual 1
        entries.head.index mustEqual 0
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.leader.isDefined mustEqual true
        entries.head.listing.get.leader.get mustEqual "FragLANdINC"
        entries.head.listing.get.task.isDefined mustEqual true
        entries.head.listing.get.task.get mustEqual "Frag"
        entries.head.listing.get.zone_id.isDefined mustEqual true
        entries.head.listing.get.zone_id.get mustEqual PlanetSideZoneID(10)
        entries.head.listing.get.size.isDefined mustEqual true
        entries.head.listing.get.size.get mustEqual 0
        entries.head.listing.get.capacity.isDefined mustEqual true
        entries.head.listing.get.capacity.get mustEqual 10
        entries.head.listing.get.squad_guid.isDefined mustEqual true
        entries.head.listing.get.squad_guid.get mustEqual PlanetSideGUID(1)
      case _ =>
        ko
    }
  }

  "decode (two)" in {
    PacketCoding.DecodePacket(stringListTwo).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 5
        behavior2.get mustEqual 6
        entries.length mustEqual 2
        entries.head.index mustEqual 0
        entries.head.listing.get.leader.get mustEqual "GeneralGorgutz"
        entries.head.listing.get.task.get mustEqual "FLY,All welcome,cn last night!!!!"
        entries.head.listing.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries.head.listing.get.size.get mustEqual 7
        entries.head.listing.get.capacity.get mustEqual 10
        entries.head.listing.get.squad_guid.get mustEqual PlanetSideGUID(6)
        entries(1).index mustEqual 1
        entries(1).listing.get.leader.get mustEqual "KOKkiasMFCN"
        entries(1).listing.get.task.get mustEqual "Squad 2"
        entries(1).listing.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries(1).listing.get.size.get mustEqual 6
        entries(1).listing.get.capacity.get mustEqual 10
        entries(1).listing.get.squad_guid.get mustEqual PlanetSideGUID(4)
      case _ =>
        ko
    }
  }

  "decode (three)" in {
    PacketCoding.DecodePacket(stringListThree).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 5
        behavior2.get mustEqual 6
        entries.length mustEqual 3
        entries.head.index mustEqual 0
        entries.head.listing.get.leader.get mustEqual "GeneralGorgutz"
        entries.head.listing.get.task.get mustEqual "FLY,All welcome,cn last night!!!!"
        entries.head.listing.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries.head.listing.get.size.get mustEqual 7
        entries.head.listing.get.capacity.get mustEqual 10
        entries.head.listing.get.squad_guid.get mustEqual PlanetSideGUID(6)
        entries(1).index mustEqual 1
        entries(1).listing.get.leader.get mustEqual "NIGHT88RAVEN"
        entries(1).listing.get.task.get mustEqual "All Welcome"
        entries(1).listing.get.zone_id.get mustEqual PlanetSideZoneID(10)
        entries(1).listing.get.size.get mustEqual 4
        entries(1).listing.get.capacity.get mustEqual 10
        entries(1).listing.get.squad_guid.get mustEqual PlanetSideGUID(3)
        entries(2).index mustEqual 2
        entries(2).listing.get.leader.get mustEqual "KOKkiasMFCN"
        entries(2).listing.get.task.get mustEqual "Squad 2"
        entries(2).listing.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries(2).listing.get.size.get mustEqual 6
        entries(2).listing.get.capacity.get mustEqual 10
        entries(2).listing.get.squad_guid.get mustEqual PlanetSideGUID(4)
      case _ =>
        ko
    }
  }

  "decode (remove)" in {
    PacketCoding.DecodePacket(stringListRemove).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 1
        behavior2.isDefined mustEqual false
        entries.length mustEqual 1
        entries.head.index mustEqual 5
        entries.head.listing.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (update leader)" in {
    PacketCoding.DecodePacket(stringUpdateLeader).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 1
        entries.head.index mustEqual 2
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.leader.isDefined mustEqual true
        entries.head.listing.get.leader.get mustEqual "FateJHNC"
        entries.head.listing.get.task.isDefined mustEqual false
        entries.head.listing.get.zone_id.isDefined mustEqual false
        entries.head.listing.get.size.isDefined mustEqual false
        entries.head.listing.get.capacity.isDefined mustEqual false
        entries.head.listing.get.squad_guid.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (update task)" in {
    PacketCoding.DecodePacket(stringUpdateTask).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 1
        entries.head.index mustEqual 5
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.leader.isDefined mustEqual false
        entries.head.listing.get.task.isDefined mustEqual true
        entries.head.listing.get.task.get mustEqual "RIP PS1, visit PSForever.net"
        entries.head.listing.get.zone_id.isDefined mustEqual false
        entries.head.listing.get.size.isDefined mustEqual false
        entries.head.listing.get.capacity.isDefined mustEqual false
        entries.head.listing.get.squad_guid.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (update continent)" in {
    PacketCoding.DecodePacket(stringUpdateContinent).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 1
        entries.head.index mustEqual 3
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.leader.isDefined mustEqual false
        entries.head.listing.get.task.isDefined mustEqual false
        entries.head.listing.get.zone_id.isDefined mustEqual true
        entries.head.listing.get.zone_id.get mustEqual PlanetSideZoneID(10)
        entries.head.listing.get.size.isDefined mustEqual false
        entries.head.listing.get.capacity.isDefined mustEqual false
        entries.head.listing.get.squad_guid.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (update size)" in {
    PacketCoding.DecodePacket(stringUpdateSize).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 1
        entries.head.index mustEqual 1
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.leader.isDefined mustEqual false
        entries.head.listing.get.task.isDefined mustEqual false
        entries.head.listing.get.zone_id.isDefined mustEqual false
        entries.head.listing.get.size.isDefined mustEqual true
        entries.head.listing.get.size.get mustEqual 6
        entries.head.listing.get.capacity.isDefined mustEqual false
        entries.head.listing.get.squad_guid.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (update leader and size)" in {
    PacketCoding.DecodePacket(stringUpdateLeaderSize).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 1
        entries.head.index mustEqual 5
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.leader.isDefined mustEqual true
        entries.head.listing.get.leader.get mustEqual "Jimmyn"
        entries.head.listing.get.task.isDefined mustEqual false
        entries.head.listing.get.zone_id.isDefined mustEqual false
        entries.head.listing.get.size.isDefined mustEqual true
        entries.head.listing.get.size.get mustEqual 3
        entries.head.listing.get.capacity.isDefined mustEqual false
        entries.head.listing.get.squad_guid.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (update task and continent)" in {
    PacketCoding.DecodePacket(stringUpdateTaskContinent).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 1
        entries.head.index mustEqual 5
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.leader.isDefined mustEqual false
        entries.head.listing.get.task.isDefined mustEqual true
        entries.head.listing.get.task.get mustEqual "2"
        entries.head.listing.get.zone_id.isDefined mustEqual true
        entries.head.listing.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries.head.listing.get.size.isDefined mustEqual false
        entries.head.listing.get.capacity.isDefined mustEqual false
        entries.head.listing.get.squad_guid.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (update all)" in {
    PacketCoding.DecodePacket(stringUpdateAll).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 1
        entries.head.index mustEqual 7
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.leader.isDefined mustEqual true
        entries.head.listing.get.leader.get mustEqual "madmuj"
        entries.head.listing.get.task.isDefined mustEqual true
        entries.head.listing.get.task.get mustEqual ""
        entries.head.listing.get.zone_id.isDefined mustEqual true
        entries.head.listing.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries.head.listing.get.size.isDefined mustEqual true
        entries.head.listing.get.size.get mustEqual 0
        entries.head.listing.get.capacity.isDefined mustEqual true
        entries.head.listing.get.capacity.get mustEqual 10
        entries.head.listing.get.squad_guid.isDefined mustEqual true
        entries.head.listing.get.squad_guid.get mustEqual PlanetSideGUID(11)
      case _ =>
        ko
    }
  }

  "decode (remove 1 and update 0)" in {
    PacketCoding.DecodePacket(stringRemoveUpdate).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 1
        behavior2.isDefined mustEqual false
        entries.length mustEqual 2
        entries.head.index mustEqual 1
        entries.head.listing.isDefined mustEqual false
        entries(1).listing.get.leader.isDefined mustEqual false
        entries(1).listing.get.task.isDefined mustEqual false
        entries(1).listing.get.zone_id.isDefined mustEqual false
        entries(1).listing.get.size.isDefined mustEqual true
        entries(1).listing.get.size.get mustEqual 10
        entries(1).listing.get.capacity.isDefined mustEqual false
        entries(1).listing.get.squad_guid.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode (clear)" in {
    val msg = ReplicationStreamMessage(5, Some(6), Vector.empty)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListClear
  }

  "encode (one)" in {
    val msg = ReplicationStreamMessage(
      Seq(
        SquadInfo("FragLANdINC", "Frag", PlanetSideZoneID(10), 0, 10, PlanetSideGUID(1))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListOne
  }

  "encode (two)" in {
    val msg = ReplicationStreamMessage(
      Seq(
        SquadInfo("GeneralGorgutz", "FLY,All welcome,cn last night!!!!", PlanetSideZoneID(4), 7, 10, PlanetSideGUID(6)),
        SquadInfo("KOKkiasMFCN", "Squad 2", PlanetSideZoneID(4), 6, 10, PlanetSideGUID(4))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListTwo
  }

  "encode (three)" in {
    val msg = ReplicationStreamMessage(
      Seq(
        SquadInfo("GeneralGorgutz", "FLY,All welcome,cn last night!!!!", PlanetSideZoneID(4), 7, 10, PlanetSideGUID(6)),
        SquadInfo("NIGHT88RAVEN", "All Welcome", PlanetSideZoneID(10), 4, 10, PlanetSideGUID(3)),
        SquadInfo("KOKkiasMFCN", "Squad 2", PlanetSideZoneID(4), 6, 10, PlanetSideGUID(4))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListThree
  }

  "encode (remove)" in {
    val msg = ReplicationStreamMessage(1, None,
      Vector(
        SquadListing(5, None)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListRemove
  }

  "encode (update leader)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(2, SquadInfo("FateJHNC"))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateLeader
  }

  "encode (update task)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(5, SquadInfo(None, "RIP PS1, visit PSForever.net"))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateTask
  }

  "encode (update continent)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(3, SquadInfo(PlanetSideZoneID(10)))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateContinent
  }

  "encode (update size)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(1, SquadInfo(6))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateSize
  }

  "encode (update leader and size)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(5, SquadInfo("Jimmyn").And(SquadInfo(3)))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateLeaderSize
  }

  "encode (update task and continent)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(5, SquadInfo(None, "2").And(SquadInfo(PlanetSideZoneID(4))))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateTaskContinent
  }

  "encode (update all)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(7, SquadInfo("madmuj", "", PlanetSideZoneID(4), 0, 10, PlanetSideGUID(11)))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateAll
  }

  "encode (remove 1 and update 0)" in {
    val msg = ReplicationStreamMessage(1, None,
      Vector(
        SquadListing(1, None),
        SquadListing(0, SquadInfo(10))
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringRemoveUpdate
  }
}
