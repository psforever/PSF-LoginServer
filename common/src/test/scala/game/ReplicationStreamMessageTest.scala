// Copyright (c) 2016 PSForever.net to present
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
  //failing conditions
  val stringCodecFail = hex"E6 20 A1 19 FE"
  val stringListOneFail = hex"E6 B8 01 06 01 00 8B 46007200610067004C0041004E00640049004E004300 84 4600720061006700 0A00 00 01 0A FF"
  val stringListTwoFail = hex"E6 B8 01 06 06 00 8E 470065006E006500720061006C0047006F0072006700750074007A00 A1 46004C0059002C0041006C006C002000770065006C0063006F006D0065002C0063006E0020006C0061007300740020006E0069006700680074002100210021002100 0400 00 00 7A 01 83 02 00 45 80 4B004F004B006B006900610073004D00460043004E00 87 5300710075006100640020003200 0400 00 01 6A FF"
  val stringUpdateLeaderFail = hex"E6 C0 28 08 44 00 46006100740065004A0048004E004300 FF"
  val stringUpdateTaskFail = hex"E6 C0 58 09CE00 52004900500020005000530031002C0020007600690073006900740020005000530046006F00720065007600650072002E006E0065007400 FF"
  val stringUpdateContinentFail = hex"E6 C0 38 09 85000001 7F80"
  val stringUpdateSizeFail = hex"E6 C0 18 0A B7 F8"
  val stringUpdateLeaderSizeFail = hex"E6 C0 58 10 43 00 4A0069006D006D0079006E00 43 FF"
  val stringUpdateTaskContinentFail = hex"E6 C0 58 11 C0 80 3200 3 04000000 FF0"
  val stringUpdateAllFail = hex"E6 C0 78 30 58 0430 6D00610064006D0075006A00 80 04000001 0A FF"

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

  "decode (clear)" in {
    PacketCoding.DecodePacket(stringListClear).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 5
        behavior2.isDefined mustEqual true
        behavior2.get mustEqual 6
        entries.length mustEqual 1
        entries.head.index mustEqual 255
        entries.head.listing.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (one)" in {
    PacketCoding.DecodePacket(stringListOne).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 5
        behavior2.get mustEqual 6
        entries.length mustEqual 2
        entries.head.index mustEqual 0
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.unk1 mustEqual 131
        entries.head.listing.get.unk2 mustEqual false
        entries.head.listing.get.unk3.isDefined mustEqual false
        entries.head.listing.get.info.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.get mustEqual "FragLANdINC"
        entries.head.listing.get.info.get.task.isDefined mustEqual true
        entries.head.listing.get.info.get.task.get mustEqual "Frag"
        entries.head.listing.get.info.get.zone_id.isDefined mustEqual true
        entries.head.listing.get.info.get.zone_id.get mustEqual PlanetSideZoneID(10)
        entries.head.listing.get.info.get.size.isDefined mustEqual true
        entries.head.listing.get.info.get.size.get mustEqual 0
        entries.head.listing.get.info.get.capacity.isDefined mustEqual true
        entries.head.listing.get.info.get.capacity.get mustEqual 10
        entries.head.listing.get.info.get.squad_guid.isDefined mustEqual true
        entries.head.listing.get.info.get.squad_guid.get mustEqual PlanetSideGUID(1)
        entries(1).index mustEqual 255
        entries(1).listing.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "decode (two)" in {
    PacketCoding.DecodePacket(stringListTwo).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 5
        behavior2.get mustEqual 6
        entries.length mustEqual 3
        entries.head.index mustEqual 0
        entries.head.listing.get.unk1 mustEqual 131
        entries.head.listing.get.unk2 mustEqual false
        entries.head.listing.get.unk3.isDefined mustEqual false
        entries.head.listing.get.info.get.leader.get mustEqual "GeneralGorgutz"
        entries.head.listing.get.info.get.task.get mustEqual "FLY,All welcome,cn last night!!!!"
        entries.head.listing.get.info.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries.head.listing.get.info.get.size.get mustEqual 7
        entries.head.listing.get.info.get.capacity.get mustEqual 10
        entries.head.listing.get.info.get.squad_guid.get mustEqual PlanetSideGUID(6)
        entries(1).index mustEqual 1
        entries(1).listing.get.unk1 mustEqual 131
        entries(1).listing.get.unk2 mustEqual false
        entries(1).listing.get.unk3.isDefined mustEqual false
        entries(1).listing.get.info.get.leader.get mustEqual "KOKkiasMFCN"
        entries(1).listing.get.info.get.task.get mustEqual "Squad 2"
        entries(1).listing.get.info.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries(1).listing.get.info.get.size.get mustEqual 6
        entries(1).listing.get.info.get.capacity.get mustEqual 10
        entries(1).listing.get.info.get.squad_guid.get mustEqual PlanetSideGUID(4)
        entries(2).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (three)" in {
    PacketCoding.DecodePacket(stringListThree).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 5
        behavior2.get mustEqual 6
        entries.length mustEqual 4
        entries.head.index mustEqual 0
        entries.head.listing.get.unk1 mustEqual 131
        entries.head.listing.get.unk2 mustEqual false
        entries.head.listing.get.unk3.isDefined mustEqual false
        entries.head.listing.get.info.get.leader.get mustEqual "GeneralGorgutz"
        entries.head.listing.get.info.get.task.get mustEqual "FLY,All welcome,cn last night!!!!"
        entries.head.listing.get.info.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries.head.listing.get.info.get.size.get mustEqual 7
        entries.head.listing.get.info.get.capacity.get mustEqual 10
        entries.head.listing.get.info.get.squad_guid.get mustEqual PlanetSideGUID(6)
        entries(1).index mustEqual 1
        entries(1).listing.get.unk1 mustEqual 131
        entries(1).listing.get.unk2 mustEqual false
        entries(1).listing.get.unk3.isDefined mustEqual false
        entries(1).listing.get.info.get.leader.get mustEqual "NIGHT88RAVEN"
        entries(1).listing.get.info.get.task.get mustEqual "All Welcome"
        entries(1).listing.get.info.get.zone_id.get mustEqual PlanetSideZoneID(10)
        entries(1).listing.get.info.get.size.get mustEqual 4
        entries(1).listing.get.info.get.capacity.get mustEqual 10
        entries(1).listing.get.info.get.squad_guid.get mustEqual PlanetSideGUID(3)
        entries(2).index mustEqual 2
        entries(2).listing.get.unk1 mustEqual 131
        entries(2).listing.get.unk2 mustEqual false
        entries(2).listing.get.unk3.isDefined mustEqual false
        entries(2).listing.get.info.get.leader.get mustEqual "KOKkiasMFCN"
        entries(2).listing.get.info.get.task.get mustEqual "Squad 2"
        entries(2).listing.get.info.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries(2).listing.get.info.get.size.get mustEqual 6
        entries(2).listing.get.info.get.capacity.get mustEqual 10
        entries(2).listing.get.info.get.squad_guid.get mustEqual PlanetSideGUID(4)
        entries(3).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (remove)" in {
    PacketCoding.DecodePacket(stringListRemove).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 1
        behavior2.isDefined mustEqual false
        entries.length mustEqual 2
        entries.head.index mustEqual 5
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.unk1 mustEqual 0
        entries.head.listing.get.unk2 mustEqual true
        entries.head.listing.get.unk3.isDefined mustEqual true
        entries.head.listing.get.unk3.get mustEqual 4
        entries.head.listing.get.info.isDefined mustEqual false
        entries(1).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (update leader)" in {
    PacketCoding.DecodePacket(stringUpdateLeader).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 2
        entries.head.index mustEqual 2
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.unk1 mustEqual 128
        entries.head.listing.get.unk2 mustEqual true
        entries.head.listing.get.unk3.isDefined mustEqual true
        entries.head.listing.get.unk3.get mustEqual 0
        entries.head.listing.get.info.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.get mustEqual "FateJHNC"
        entries.head.listing.get.info.get.task.isDefined mustEqual false
        entries.head.listing.get.info.get.zone_id.isDefined mustEqual false
        entries.head.listing.get.info.get.size.isDefined mustEqual false
        entries.head.listing.get.info.get.capacity.isDefined mustEqual false
        entries.head.listing.get.info.get.squad_guid.isDefined mustEqual false
        entries(1).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (update task)" in {
    PacketCoding.DecodePacket(stringUpdateTask).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 2
        entries.head.index mustEqual 5
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.unk1 mustEqual 128
        entries.head.listing.get.unk2 mustEqual true
        entries.head.listing.get.unk3.isDefined mustEqual true
        entries.head.listing.get.unk3.get mustEqual 1
        entries.head.listing.get.info.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.isDefined mustEqual false
        entries.head.listing.get.info.get.task.isDefined mustEqual true
        entries.head.listing.get.info.get.task.get mustEqual "RIP PS1, visit PSForever.net"
        entries.head.listing.get.info.get.zone_id.isDefined mustEqual false
        entries.head.listing.get.info.get.size.isDefined mustEqual false
        entries.head.listing.get.info.get.capacity.isDefined mustEqual false
        entries.head.listing.get.info.get.squad_guid.isDefined mustEqual false
        entries(1).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (update continent)" in {
    PacketCoding.DecodePacket(stringUpdateContinent).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 2
        entries.head.index mustEqual 3
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.unk1 mustEqual 128
        entries.head.listing.get.unk2 mustEqual true
        entries.head.listing.get.unk3.isDefined mustEqual true
        entries.head.listing.get.unk3.get mustEqual 1
        entries.head.listing.get.info.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.isDefined mustEqual false
        entries.head.listing.get.info.get.task.isDefined mustEqual false
        entries.head.listing.get.info.get.zone_id.isDefined mustEqual true
        entries.head.listing.get.info.get.zone_id.get mustEqual PlanetSideZoneID(10)
        entries.head.listing.get.info.get.size.isDefined mustEqual false
        entries.head.listing.get.info.get.capacity.isDefined mustEqual false
        entries.head.listing.get.info.get.squad_guid.isDefined mustEqual false
        entries(1).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (update size)" in {
    PacketCoding.DecodePacket(stringUpdateSize).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 2
        entries.head.index mustEqual 1
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.unk1 mustEqual 128
        entries.head.listing.get.unk2 mustEqual true
        entries.head.listing.get.unk3.isDefined mustEqual true
        entries.head.listing.get.unk3.get mustEqual 2
        entries.head.listing.get.info.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.isDefined mustEqual false
        entries.head.listing.get.info.get.task.isDefined mustEqual false
        entries.head.listing.get.info.get.zone_id.isDefined mustEqual false
        entries.head.listing.get.info.get.size.isDefined mustEqual true
        entries.head.listing.get.info.get.size.get mustEqual 6
        entries.head.listing.get.info.get.capacity.isDefined mustEqual false
        entries.head.listing.get.info.get.squad_guid.isDefined mustEqual false
        entries(1).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (update leader and size)" in {
    PacketCoding.DecodePacket(stringUpdateLeaderSize).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 2
        entries.head.index mustEqual 5
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.unk1 mustEqual 129
        entries.head.listing.get.unk2 mustEqual false
        entries.head.listing.get.unk3.isDefined mustEqual true
        entries.head.listing.get.unk3.get mustEqual 0
        entries.head.listing.get.info.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.get mustEqual "Jimmyn"
        entries.head.listing.get.info.get.task.isDefined mustEqual false
        entries.head.listing.get.info.get.zone_id.isDefined mustEqual false
        entries.head.listing.get.info.get.size.isDefined mustEqual true
        entries.head.listing.get.info.get.size.get mustEqual 3
        entries.head.listing.get.info.get.capacity.isDefined mustEqual false
        entries.head.listing.get.info.get.squad_guid.isDefined mustEqual false
        entries(1).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (update task and continent)" in {
    PacketCoding.DecodePacket(stringUpdateTaskContinent).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 2
        entries.head.index mustEqual 5
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.unk1 mustEqual 129
        entries.head.listing.get.unk2 mustEqual false
        entries.head.listing.get.unk3.isDefined mustEqual true
        entries.head.listing.get.unk3.get mustEqual 1
        entries.head.listing.get.info.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.isDefined mustEqual false
        entries.head.listing.get.info.get.task.isDefined mustEqual true
        entries.head.listing.get.info.get.task.get mustEqual "2"
        entries.head.listing.get.info.get.zone_id.isDefined mustEqual true
        entries.head.listing.get.info.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries.head.listing.get.info.get.size.isDefined mustEqual false
        entries.head.listing.get.info.get.capacity.isDefined mustEqual false
        entries.head.listing.get.info.get.squad_guid.isDefined mustEqual false
        entries(1).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (update all)" in {
    PacketCoding.DecodePacket(stringUpdateAll).require match {
      case ReplicationStreamMessage(behavior, behavior2, entries) =>
        behavior mustEqual 6
        behavior2.isDefined mustEqual false
        entries.length mustEqual 2
        entries.head.index mustEqual 7
        entries.head.listing.isDefined mustEqual true
        entries.head.listing.get.unk1 mustEqual 131
        entries.head.listing.get.unk2 mustEqual false
        entries.head.listing.get.unk3.isDefined mustEqual false
        entries.head.listing.get.info.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.isDefined mustEqual true
        entries.head.listing.get.info.get.leader.get mustEqual "madmuj"
        entries.head.listing.get.info.get.task.isDefined mustEqual true
        entries.head.listing.get.info.get.task.get mustEqual ""
        entries.head.listing.get.info.get.zone_id.isDefined mustEqual true
        entries.head.listing.get.info.get.zone_id.get mustEqual PlanetSideZoneID(4)
        entries.head.listing.get.info.get.size.isDefined mustEqual true
        entries.head.listing.get.info.get.size.get mustEqual 0
        entries.head.listing.get.info.get.capacity.isDefined mustEqual true
        entries.head.listing.get.info.get.capacity.get mustEqual 10
        entries.head.listing.get.info.get.squad_guid.isDefined mustEqual true
        entries.head.listing.get.info.get.squad_guid.get mustEqual PlanetSideGUID(11)
        entries(1).index mustEqual 255
      case _ =>
        ko
    }
  }

  "decode (fails)" in {
    PacketCoding.DecodePacket(stringCodecFail).isFailure mustEqual true
    //PacketCoding.DecodePacket(stringListOneFail).isFailure mustEqual true -> used to fail
    //PacketCoding.DecodePacket(stringListTwoFail).isFailure mustEqual true -> used to fail
    PacketCoding.DecodePacket(stringUpdateLeaderFail).isFailure mustEqual true
    PacketCoding.DecodePacket(stringUpdateTaskFail).isFailure mustEqual true
    //PacketCoding.DecodePacket(stringUpdateContinentFail).isFailure mustEqual true -> used to fail
    PacketCoding.DecodePacket(stringUpdateSizeFail).isFailure mustEqual true
    PacketCoding.DecodePacket(stringUpdateLeaderSizeFail).isFailure mustEqual true
    PacketCoding.DecodePacket(stringUpdateTaskContinentFail).isFailure mustEqual true
    //PacketCoding.DecodePacket(stringUpdateAllFail).isFailure mustEqual true -> used to fail
  }

  "encode (clear)" in {
    val msg = ReplicationStreamMessage(5, Some(6),
      Vector(
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListClear
  }

  "encode (one)" in {
    val msg = ReplicationStreamMessage(5, Some(6),
      Vector(
        SquadListing(0, Some(SquadHeader(131, false, None, SquadInfo("FragLANdINC", "Frag", PlanetSideZoneID(10), 0, 10, PlanetSideGUID(1))))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListOne
  }

  "encode (two)" in {
    val msg = ReplicationStreamMessage(5, Some(6),
      Vector(
        SquadListing(0, Some(SquadHeader(131, false, None, SquadInfo("GeneralGorgutz", "FLY,All welcome,cn last night!!!!", PlanetSideZoneID(4), 7, 10, PlanetSideGUID(6))))),
        SquadListing(1, Some(SquadHeader(131, false, None, SquadInfo("KOKkiasMFCN", "Squad 2", PlanetSideZoneID(4), 6, 10, PlanetSideGUID(4))))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListTwo
  }

  "encode (three)" in {
    val msg = ReplicationStreamMessage(5, Some(6),
      Vector(
        SquadListing(0, Some(SquadHeader(131, false, None, SquadInfo("GeneralGorgutz", "FLY,All welcome,cn last night!!!!", PlanetSideZoneID(4), 7, 10, PlanetSideGUID(6))))),
        SquadListing(1, Some(SquadHeader(131, false, None, SquadInfo("NIGHT88RAVEN", "All Welcome", PlanetSideZoneID(10), 4, 10, PlanetSideGUID(3))))),
        SquadListing(2, Some(SquadHeader(131, false, None, SquadInfo("KOKkiasMFCN", "Squad 2", PlanetSideZoneID(4), 6, 10, PlanetSideGUID(4))))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListThree
  }

  "encode (remove)" in {
    val msg = ReplicationStreamMessage(1, None,
      Vector(
        SquadListing(5, Some(SquadHeader(0, true, Some(4)))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringListRemove
  }

  "encode (update leader)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(2, Some(SquadHeader(128, true, Some(0), SquadInfo("FateJHNC", None)))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateLeader
  }

  "encode (update task)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(5, Some(SquadHeader(128, true, Some(1), SquadInfo(None, "RIP PS1, visit PSForever.net")))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateTask
  }

  "encode (update continent)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(3, Some(SquadHeader(128, true, Some(1), SquadInfo(PlanetSideZoneID(10))))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateContinent
  }

  "encode (update size)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(1, Some(SquadHeader(128, true, Some(2), SquadInfo(6, None)))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateSize
  }

  "encode (update leader and size)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(5, Some(SquadHeader(129, false, Some(0), SquadInfo("Jimmyn", 3)))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateLeaderSize
  }

  "encode (update task and continent)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(5, Some(SquadHeader(129, false, Some(1), SquadInfo("2", PlanetSideZoneID(4))))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateTaskContinent
  }

  "encode (update all)" in {
    val msg = ReplicationStreamMessage(6, None,
      Vector(
        SquadListing(7, Some(SquadHeader(131, false, None, SquadInfo("madmuj", "", PlanetSideZoneID(4), 0, 10, PlanetSideGUID(11))))),
        SquadListing(255)
      )
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringUpdateAll
  }

  "encode (fails)" in {
    //encode codec fail
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(1, None,
        Vector(
          SquadListing(5, Some(SquadHeader(0, false, Some(4)))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode one
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(5, Some(6),
        Vector(
          SquadListing(0, Some(SquadHeader(131, false, None, Some(SquadInfo(Some("FragLANdINC"), Some("Frag"), None, Some(0),Some(10), Some(PlanetSideGUID(1))))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode two
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(5, Some(6),
        Vector(
          SquadListing(0, Some(SquadHeader(131, false, None, SquadInfo("GeneralGorgutz", "FLY,All welcome,cn last night!!!!", PlanetSideZoneID(4), 7, 10, PlanetSideGUID(6))))),
          SquadListing(1, Some(SquadHeader(131, false, None, Some(SquadInfo(Some("KOKkiasMFCN"), Some("Squad 2"), None, Some(6), Some(10), Some(PlanetSideGUID(4))))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode leader
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(6, None,
        Vector(
          SquadListing(2, Some(SquadHeader(128, true, Some(0), Some(SquadInfo(None, None, None, None, None, None))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode task
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(6, None,
        Vector(
          SquadListing(5, Some(SquadHeader(128, true, Some(1), Some(SquadInfo(None, None, None, None, None, None))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode continent
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(6, None,
        Vector(
          SquadListing(3, Some(SquadHeader(128, true, Some(1), Some(SquadInfo(None, None, None, None, None, None))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode task or continent
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(6, None,
        Vector(
          SquadListing(3, Some(SquadHeader(128, true, Some(1), Some(SquadInfo(None, Some(""), Some(PlanetSideZoneID(10)), None, None, None))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode size
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(6, None,
        Vector(
          SquadListing(1, Some(SquadHeader(128, true, Some(2), Some(SquadInfo(None, None, None, None, None, None))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode leader and size
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(6, None,
        Vector(
          SquadListing(5, Some(SquadHeader(129, false, Some(0), Some(SquadInfo(None, None, None, None, None, None))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode task and continent
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(6, None,
        Vector(
          SquadListing(5, Some(SquadHeader(129, false, Some(1), Some(SquadInfo(None, None, None, None, None, None))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true

    //encode all
    PacketCoding.EncodePacket(
      ReplicationStreamMessage(6, None,
        Vector(
          SquadListing(7, Some(SquadHeader(131, false, None, Some(SquadInfo(None, None, None, None, None, None))))),
          SquadListing(255)
        )
      )
    ).isFailure mustEqual true
  }
}
