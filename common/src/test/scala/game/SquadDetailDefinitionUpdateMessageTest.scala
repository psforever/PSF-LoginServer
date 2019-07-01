// Copyright (c) 2019 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.CertificationType
import org.specs2.mutable._
import scodec.bits._

class SquadDetailDefinitionUpdateMessageTest extends Specification {
  val string_unk = hex"e80300821104145011b9be840024284a00610061006b006f008c008118000000024000ff"
  val string_unk1 = hex"e80300818800015c5189004603408c000000012000ff"
  val string_leader_char_id = hex"e8050080904d56b808"
  val string_unk3LeaderName = hex"e80300821104145011b9be840024284a00610061006b006f008c008118000000024000ff"
  val string_task = hex"e8050080ac6041006c006c002000570065006c0063006f006d0065002000"
  val string_zone = hex"e8030080b0a8000000"
  val string_taskZone = hex"e80200812ce05c002300460046003000300030003000200054006800650020005c002300660066006600660066006600200042006c0061006400650073006040000000"
  val string_unk7 = hex"e8030081ac8054006800650020004b0069006e00670027007300200053007100750061006400788c09808c4854006800650020004700750061007200640008808c5054006800650020004b006e00690067006800740007808c4054006800650020004500610072006c0006808c4054006800650020004c006f007200640005808c405400680065002000440075006b00650004808c4854006800650020004200610072006f006e0003808c6054006800650020005000720069006e00630065007300730002808c5054006800650020005000720069006e006300650001808c48540068006500200051007500650065006e0000808c4054006800650020004b0069006e006700ff"
  val string_member_closed = hex"e8030080c602c043fe"
  val string_member_role = hex"e8070080c60040462443006f006d006d0061006e00640065007200ff"
  val string_member_roleRequirements = hex"e8010080c60340862841004400560020004800610063006b00650072005000000002003fc0"
  val string_member_charIdName = hex"e8030080c602c08f2658480123004400750063006b006d006100730074006500720034003300ff"
  val string_task_memberEtc = hex"e80100812ce05c002300460046003000300030003000200054006800650020005c002300660066006600660066006600200042006c0061006400650073008c09810c005000000000000220230007808c0006808c0005808c0004808c0003808c0002808c0001808c0000808c00ff"
  val string_full = hex"e80300848180038021514601288a8400420048006f0066004400bf5c0023006600660064006300300030002a002a002a005c0023003900360034003000660066003d004b004f004b002b005300500043002b0046004c0059003d005c0023006600660064006300300030002a002a002a005c002300460046003400300034003000200041006c006c002000570065006c0063006f006d006500070000009814010650005c00230066006600300030003000300020007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c008000000000800100000c00020c8c5c00230066006600640063003000300020002000200043008000000000800100000c00020c8c5c002300660066006400630030003000200020002000480080eab58a02854f0070006f006c0045000100000c00020c8d5c002300660066006400630030003000200020002000200049008072d47a028b42006f006200610046003300740074003900300037000100000c00020c8c5c0023006600660064006300300030002000200020004e008000000000800100000c00020c8c5c00230066006600640063003000300020002000200041008000000000800100000c00020ca05c00230066006600300030003000300020007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c007c008000000000800100000c00020c8c5c0023003900360034003000660066002000200020004b008000000000800100000c00020c8c5c0023003900360034003000660066002000200020004f008042a28c028448006f00660044000100000c00020c8c5c0023003900360034003000660066002000200020004b008000000000800100000c0000"

  "SquadDetailDefinitionUpdateMessage" should {
    "decode (test)" in {
      PacketCoding.DecodePacket(string_unk).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          detail
          ok
        case _ =>
          ko
      }
      ok
    }

    "decode (unk1 + members)" in {
      PacketCoding.DecodePacket(string_unk1).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(3)
          detail match {
            case SquadDetail(Some(unk1), None, Some(char_id), None, None, None, None, None, Some(_)) =>
              unk1 mustEqual 0
              char_id mustEqual 1221560L
              //members tests follow ...
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

//    "decode (char id)" in {
//      PacketCoding.DecodePacket(string_leader_char_id).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(5)
//          detail match {
//            case SquadDetail(None, None, Some(char_id), None, None, None, None, None, None) =>
//              char_id mustEqual 30910985
//            case _ =>
//              ko
//          }
//          ok
//        case _ =>
//          ko
//      }
//    }
//
//    "decode (unk3 + leader name)" in {
//      PacketCoding.DecodePacket(string_unk3LeaderName).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(3)
//          detail match {
//            case SquadDetail(None, None, Some(char_id), Some(unk3), Some(leader), None, None, None, Some(_)) =>
//              char_id mustEqual 42631712L
//              unk3 mustEqual 556403L
//              leader mustEqual "Jaako"
//              //members tests follow ...
//            case _ =>
//              ko
//          }
//        case _ =>
//          ko
//      }
//    }
//
//    "decode (task)" in {
//      PacketCoding.DecodePacket(string_task).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(5)
//          detail match {
//            case SquadDetail(None, None, None, None, None, Some(task), None, None, None) =>
//              task mustEqual "All Welcome "
//            case _ =>
//              ko
//          }
//          ok
//        case _ =>
//          ko
//      }
//    }
//
//    "decode (zone)" in {
//      PacketCoding.DecodePacket(string_zone).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(3)
//          detail match {
//            case SquadDetail(None, None, None, None, None, None, Some(zone), None, None) =>
//              zone mustEqual PlanetSideZoneID(21)
//            case _ =>
//              ko
//          }
//          ok
//        case _ =>
//          ko
//      }
//    }
//
//    "decode (task + zone)" in {
//      PacketCoding.DecodePacket(string_taskZone).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          detail match {
//            case SquadDetail(None, None, None, None, None, Some(task), Some(zone), None, None) =>
//              task mustEqual "\\#FF0000 The \\#ffffff Blades"
//              zone mustEqual PlanetSideZoneID(4)
//            case _ =>
//              ko
//          }
//        case _ =>
//          ko
//      }
//      ok
//    }
//
//      "decode (unk7 + members)" in {
//        PacketCoding.DecodePacket(string_unk7).require match {
//          case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//            guid mustEqual PlanetSideGUID(3)
//            detail match {
//              case SquadDetail(None, None, None, None, None, Some(task), None, Some(unk7), Some(_)) =>
//                task mustEqual "The King's Squad"
//                unk7 mustEqual 8
//                //members tests follow ...
//              case _ =>
//                ko
//            }
//          case _ =>
//            ko
//        }
//      }
//
//    "decode (member closed)" in {
//      PacketCoding.DecodePacket(string_member_closed).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(3)
//          detail match {
//            case SquadDetail(None, None, None, None, None, None, None, None, Some(members)) =>
//              members.size mustEqual 2
//              members.head.index mustEqual 5
//              members.head.info match {
//                case Some(SquadPositionDetail2(Some(is_closed), None, None, None, None, None)) =>
//                  is_closed mustEqual true
//                case _ =>
//                  ko
//              }
//            case _ =>
//              ko
//          }
//          ok
//        case _ =>
//          ko
//      }
//    }
//
//    "decode (member role)" in {
//      PacketCoding.DecodePacket(string_member_role).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(7)
//          detail match {
//            case SquadDetail(None, None, None, None, None, None, None, None, Some(members)) =>
//              members.size mustEqual 2
//              members.head.index mustEqual 0
//              members.head.info match {
//                case Some(SquadPositionDetail2(None, Some(role), None, None, None, None)) =>
//                  role mustEqual "Commander"
//                case _ =>
//                  ko
//              }
//            case _ =>
//              ko
//          }
//          ok
//        case _ =>
//          ko
//      }
//    }
//
//    "decode (member role + requirements)" in {
//      PacketCoding.DecodePacket(string_member_roleRequirements).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(1)
//          detail match {
//            case SquadDetail(None, None, None, None, None, None, None, None, Some(members)) =>
//              members.size mustEqual 2
//              members.head.index mustEqual 6
//              members.head.info match {
//                case Some(SquadPositionDetail2(None, Some(role), None, Some(req), None, None)) =>
//                  role mustEqual "ADV Hacker"
//                  req.size mustEqual 1
//                  req.contains(CertificationType.AdvancedHacking) mustEqual true
//                case _ =>
//                  ko
//              }
//            case _ =>
//              ko
//          }
//          ok
//        case _ =>
//          ko
//      }
//    }
//
//    "decode (member char id + name)" in {
//      PacketCoding.DecodePacket(string_member_charIdName).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(3)
//          detail match {
//            case SquadDetail(None, None, None, None, None, None, None, None, Some(members)) =>
//              members.size mustEqual 2
//              members.head.index mustEqual 5
//              members.head.info match {
//                case Some(SquadPositionDetail2(None, None, None, None, Some(char_id), Some(name))) =>
//                  char_id mustEqual 1218249L
//                  name mustEqual "Duckmaster43"
//                case _ =>
//                  ko
//              }
//            case _ =>
//              ko
//          }
//          ok
//        case _ =>
//          ko
//      }
//    }
//
//    "decode (task + member etc)" in {
//      PacketCoding.DecodePacket(string_task_memberEtc).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(1)
//          detail match {
//            case SquadDetail(None, None, None, None, None, Some(task), None, None, Some(members)) =>
//              task mustEqual "\\#FF0000 The \\#ffffff Blades"
//              members.size mustEqual 11
//              //
//              members.head.index mustEqual 9
//              members.head.info match {
//                case Some(SquadPositionDetail2(None, Some(role), None, Some(req), None, None)) =>
//                  role mustEqual ""
//                  req mustEqual Set.empty
//                case _ =>
//                  ko
//              }
//              //
//              (1 to 9).foreach { index =>
//                members(index).index mustEqual 9 - index
//                members(index).info match {
//                  case Some(SquadPositionDetail2(None, Some(role), None, None, None, None)) =>
//                    role mustEqual ""
//                  case _ =>
//                    ko
//                }
//              }
//            case _ =>
//              ko
//          }
//        case _ =>
//          ko
//      }
//      ok
//    }
//
//    "decode (full squad)" in {
//      PacketCoding.DecodePacket(string_full).require match {
//        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
//          guid mustEqual PlanetSideGUID(3)
//          detail match {
//            case SquadDetail(Some(u1), Some(u2), Some(char_id), Some(u3), Some(leader), Some(task), Some(zone), Some(unk7), Some(member_list)) =>
//              u1 mustEqual 3
//              u2 mustEqual 1792
//              char_id mustEqual 42771010L
//              u3 mustEqual 529745L
//              leader mustEqual "HofD"
//              task mustEqual "\\#ffdc00***\\#9640ff=KOK+SPC+FLY=\\#ffdc00***\\#FF4040 All Welcome"
//              zone mustEqual PlanetSideZoneID(7)
//              unk7 mustEqual 4983296
//              member_list.size mustEqual 10
//              member_list.head mustEqual SquadPositionEntry(0,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#ff0000 |||||||||||||||||||||||"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(0),
//                  Some("")))
//              )
//              member_list(1) mustEqual SquadPositionEntry(1,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#ffdc00   C"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(0),
//                  Some("")))
//              )
//              member_list(2) mustEqual SquadPositionEntry(2,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#ffdc00   H"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(42644970L),
//                  Some("OpolE")
//                )
//              ))
//              member_list(3) mustEqual SquadPositionEntry(3,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#ffdc00    I"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(41604210L),
//                  Some("BobaF3tt907")
//                )
//              ))
//              member_list(4) mustEqual SquadPositionEntry(4,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#ffdc00   N"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(0),
//                  Some("")
//                )
//              ))
//              member_list(5) mustEqual SquadPositionEntry(5,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#ffdc00   A"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(0),
//                  Some("")
//                )
//              ))
//              member_list(6) mustEqual SquadPositionEntry(6,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#ff0000 |||||||||||||||||||||||"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(0),
//                  Some("")
//                )
//              ))
//              member_list(7) mustEqual SquadPositionEntry(7,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#9640ff   K"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(0),
//                  Some("")
//                )
//              ))
//              member_list(8) mustEqual SquadPositionEntry(8,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#9640ff   O"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(42771010L),
//                  Some("HofD")
//                )
//              ))
//              member_list(9) mustEqual SquadPositionEntry(9,Some(
//                SquadPositionDetail2(
//                  Some(false),
//                  Some("\\#9640ff   K"),
//                  Some(""),
//                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
//                  Some(0),
//                  Some("")
//                )
//              ))
//            case _ =>
//              ko
//          }
//        case _ =>
//          ko
//      }
//    }
//
//    "encode (char id)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(5),
//        SquadDetail(30910985L)
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_leader_char_id
//    }
//
//    "encode (unk3 + leader name)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(3),
//        SquadDetail(None, None, Some(42631712L), Some(556403L), Some("Jaako"), None, None, None, Some(List(
//          SquadPositionEntry(0, SquadPositionDetail2(0L, "")),
//          SquadPositionEntry(255, None)
//        )))
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_unk3LeaderName
//    }
//
//    "encode (task)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(5),
//        SquadDetail(None, "All Welcome ")
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_task
//    }
//
//    "encode (zone)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(3),
//        SquadDetail(PlanetSideZoneID(21))
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_zone
//    }
//
//    "encode (task + zone)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(2),
//        SquadDetail(None, None, None, None, None, Some("\\#FF0000 The \\#ffffff Blades"), Some(PlanetSideZoneID(4)), None)
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_taskZone
//    }
//
//    "encode (unk7 + members)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(3),
//        SquadDetail(
//          None, None, None, None, None,
//          Some("The King's Squad"),
//          None, Some(8),
//          Some(List(
//            SquadPositionEntry(9, SquadPositionDetail2("The Guard", None)),
//            SquadPositionEntry(8, SquadPositionDetail2("The Knight", None)),
//            SquadPositionEntry(7, SquadPositionDetail2("The Earl", None)),
//            SquadPositionEntry(6, SquadPositionDetail2("The Lord", None)),
//            SquadPositionEntry(5, SquadPositionDetail2("The Duke", None)),
//            SquadPositionEntry(4, SquadPositionDetail2("The Baron", None)),
//            SquadPositionEntry(3, SquadPositionDetail2("The Princess", None)),
//            SquadPositionEntry(2, SquadPositionDetail2("The Prince", None)),
//            SquadPositionEntry(1, SquadPositionDetail2("The Queen", None)),
//            SquadPositionEntry(0, SquadPositionDetail2("The King", None)),
//            SquadPositionEntry(255, None)
//          ))
//        )
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_unk7
//    }
//
//    "encode (member closed)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(3),
//        SquadDetail(List(
//          SquadPositionEntry(5, SquadPositionDetail2.Closed),
//          SquadPositionEntry(255, None)
//        ))
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_member_closed
//    }
//
//
//    "encode (member role)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(7),
//        SquadDetail(List(
//          SquadPositionEntry(0, SquadPositionDetail2("Commander", None)),
//          SquadPositionEntry(255, None)
//        ))
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_member_role
//    }
//
//    "encode (member role + requirements)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(1),
//        SquadDetail(List(
//          SquadPositionEntry(6, SquadPositionDetail2(None, Some("ADV Hacker"), None, Some(Set(CertificationType.AdvancedHacking)), None, None)),
//          SquadPositionEntry(255, None)
//        ))
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_member_roleRequirements
//    }
//
//    "encode (member char id + name)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(3),
//        SquadDetail(List(
//          SquadPositionEntry(5, SquadPositionDetail2(1218249L, "Duckmaster43")),
//          SquadPositionEntry(255, None)
//        ))
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_member_charIdName
//    }
//
//    "encode (task + member etc)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(1),
//        SquadDetail(
//          None, None, None, None, None,
//          Some("\\#FF0000 The \\#ffffff Blades"), None, None,
//          Some(List(
//            SquadPositionEntry(9, Some(SquadPositionDetail2(None, Some(""), None, Some(Set()), None, None))),
//            SquadPositionEntry(8, Some(SquadPositionDetail2(None, Some(""), None, None, None, None))),
//            SquadPositionEntry(7, Some(SquadPositionDetail2(None, Some(""), None, None, None, None))),
//            SquadPositionEntry(6, Some(SquadPositionDetail2(None, Some(""), None, None, None, None))),
//            SquadPositionEntry(5, Some(SquadPositionDetail2(None, Some(""), None, None, None, None))),
//            SquadPositionEntry(4, Some(SquadPositionDetail2(None, Some(""), None, None, None, None))),
//            SquadPositionEntry(3, Some(SquadPositionDetail2(None, Some(""), None, None, None, None))),
//            SquadPositionEntry(2, Some(SquadPositionDetail2(None, Some(""), None, None, None, None))),
//            SquadPositionEntry(1, Some(SquadPositionDetail2(None, Some(""), None, None, None, None))),
//            SquadPositionEntry(0, Some(SquadPositionDetail2(None, Some(""), None, None, None, None))),
//            SquadPositionEntry(255, None)
//          ))
//        )
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      pkt mustEqual string_task_memberEtc
//    }
//
//    "encode (full squad)" in {
//      val msg = SquadDetailDefinitionUpdateMessage(
//        PlanetSideGUID(3),
//        SquadDetail(
//          Some(3),
//          Some(1792),
//          Some(42771010L),
//          Some(529745L),
//          Some("HofD"),
//          Some("\\#ffdc00***\\#9640ff=KOK+SPC+FLY=\\#ffdc00***\\#FF4040 All Welcome"),
//          Some(PlanetSideZoneID(7)),
//          Some(4983296),
//          Some(List(
//            SquadPositionEntry(0, SquadPositionDetail2("\\#ff0000 |||||||||||||||||||||||", "", Set(), 0, "")),
//            SquadPositionEntry(1, SquadPositionDetail2("\\#ffdc00   C", "", Set(), 0, "")),
//            SquadPositionEntry(2, SquadPositionDetail2("\\#ffdc00   H", "", Set(), 42644970L, "OpolE")),
//            SquadPositionEntry(3, SquadPositionDetail2("\\#ffdc00    I", "", Set(), 41604210L, "BobaF3tt907")),
//            SquadPositionEntry(4, SquadPositionDetail2("\\#ffdc00   N", "", Set(), 0, "")),
//            SquadPositionEntry(5, SquadPositionDetail2("\\#ffdc00   A", "", Set(), 0, "")),
//            SquadPositionEntry(6, SquadPositionDetail2("\\#ff0000 |||||||||||||||||||||||", "", Set(), 0, "")),
//            SquadPositionEntry(7, SquadPositionDetail2("\\#9640ff   K", "", Set(), 0, "")),
//            SquadPositionEntry(8, SquadPositionDetail2("\\#9640ff   O", "", Set(), 42771010L ,"HofD")),
//            SquadPositionEntry(9, SquadPositionDetail2("\\#9640ff   K", "", Set(), 0, ""))
//          )
//        ))
//      )
//      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
//      val pktBits = pkt.toBitVector
//      val strBits = string_full.toBitVector
//      pktBits.grouped(100).zip(strBits.grouped(100)).foreach({ case (a, b) =>
//        a mustEqual b
//      })
//      pkt mustEqual string_full
//    }
  }
}
