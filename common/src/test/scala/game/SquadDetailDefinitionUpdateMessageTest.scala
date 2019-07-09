// Copyright (c) 2019 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.CertificationType
import org.specs2.mutable._
import scodec.bits._

class SquadDetailDefinitionUpdateMessageTest extends Specification {
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
  val string_mixed = hex"e80300812cd85000530046006f007200650076006500720020005000610063006b0065007400200043006f006c006c0065006300740069006f006e00841400000181306400800000000080000000000000220c808000000000800000000000001e0c808000000000800000000000001a0c80800000000080000000000000160c80800000000080000000000000120c808000000000800000000000000e0c808000000000800000000000000a0c80800000000080000000000000060c80800000000080000000000000020c80800000000080000000000003fc"

  "SquadDetailDefinitionUpdateMessage" should {
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

    "decode (char id)" in {
      PacketCoding.DecodePacket(string_leader_char_id).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(5)
          detail match {
            case SquadDetail(None, None, Some(char_id), None, None, None, None, None, None) =>
              char_id mustEqual 30910985
            case _ =>
              ko
          }
          ok
        case _ =>
          ko
      }
    }

    "decode (unk3 + leader name)" in {
      PacketCoding.DecodePacket(string_unk3LeaderName).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(3)
          detail match {
            case SquadDetail(None, None, Some(char_id), Some(unk3), Some(leader), None, None, None, Some(_)) =>
              char_id mustEqual 42631712L
              unk3 mustEqual 556403L
              leader mustEqual "Jaako"
              //members tests follow ...
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (task)" in {
      PacketCoding.DecodePacket(string_task).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(5)
          detail match {
            case SquadDetail(None, None, None, None, None, Some(task), None, None, None) =>
              task mustEqual "All Welcome "
            case _ =>
              ko
          }
          ok
        case _ =>
          ko
      }
    }

    "decode (zone)" in {
      PacketCoding.DecodePacket(string_zone).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(3)
          detail match {
            case SquadDetail(None, None, None, None, None, None, Some(zone), None, None) =>
              zone mustEqual PlanetSideZoneID(21)
            case _ =>
              ko
          }
          ok
        case _ =>
          ko
      }
    }

    "decode (task + zone)" in {
      PacketCoding.DecodePacket(string_taskZone).require match {
        case SquadDetailDefinitionUpdateMessage(_, detail) =>
          detail match {
            case SquadDetail(None, None, None, None, None, Some(task), Some(zone), None, None) =>
              task mustEqual "\\#FF0000 The \\#ffffff Blades"
              zone mustEqual PlanetSideZoneID(4)
            case _ =>
              ko
          }
        case _ =>
          ko
      }
      ok
    }

      "decode (unk7 + members)" in {
        PacketCoding.DecodePacket(string_unk7).require match {
          case SquadDetailDefinitionUpdateMessage(guid, detail) =>
            guid mustEqual PlanetSideGUID(3)
            detail match {
              case SquadDetail(None, None, None, None, None, Some(task), None, Some(unk7), Some(_)) =>
                task mustEqual "The King's Squad"
                unk7 mustEqual 8
                //members tests follow ...
              case _ =>
                ko
            }
          case _ =>
            ko
        }
      }

    "decode (member closed)" in {
      PacketCoding.DecodePacket(string_member_closed).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(3)
          detail match {
            case SquadDetail(None, None, None, None, None, None, None, None, Some(members)) =>
              members.size mustEqual 1
              members.head.index mustEqual 5
              members.head.info match {
                case Some(SquadPositionDetail(Some(is_closed), None, None, None, None, None)) =>
                  is_closed mustEqual true
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
          ok
        case _ =>
          ko
      }
    }

    "decode (member role)" in {
      PacketCoding.DecodePacket(string_member_role).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(7)
          detail match {
            case SquadDetail(None, None, None, None, None, None, None, None, Some(members)) =>
              members.size mustEqual 1
              members.head.index mustEqual 0
              members.head.info match {
                case Some(SquadPositionDetail(None, Some(role), None, None, None, None)) =>
                  role mustEqual "Commander"
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
          ok
        case _ =>
          ko
      }
    }

    "decode (member role + requirements)" in {
      PacketCoding.DecodePacket(string_member_roleRequirements).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(1)
          detail match {
            case SquadDetail(None, None, None, None, None, None, None, None, Some(members)) =>
              members.size mustEqual 1
              members.head.index mustEqual 6
              members.head.info match {
                case Some(SquadPositionDetail(None, Some(role), None, Some(req), None, None)) =>
                  role mustEqual "ADV Hacker"
                  req.size mustEqual 1
                  req.contains(CertificationType.AdvancedHacking) mustEqual true
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
          ok
        case _ =>
          ko
      }
    }

    "decode (member char id + name)" in {
      PacketCoding.DecodePacket(string_member_charIdName).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(3)
          detail match {
            case SquadDetail(None, None, None, None, None, None, None, None, Some(members)) =>
              members.size mustEqual 1
              members.head.index mustEqual 5
              members.head.info match {
                case Some(SquadPositionDetail(None, None, None, None, Some(char_id), Some(name))) =>
                  char_id mustEqual 1218249L
                  name mustEqual "Duckmaster43"
                case _ =>
                  ko
              }
            case _ =>
              ko
          }
          ok
        case _ =>
          ko
      }
    }

    "decode (task + member etc)" in {
      PacketCoding.DecodePacket(string_task_memberEtc).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(1)
          detail match {
            case SquadDetail(None, None, None, None, None, Some(task), None, None, Some(members)) =>
              task mustEqual "\\#FF0000 The \\#ffffff Blades"
              members.size mustEqual 10
              //
              members.head.index mustEqual 9
              members.head.info match {
                case Some(SquadPositionDetail(None, Some(role), None, Some(req), None, None)) =>
                  role mustEqual ""
                  req mustEqual Set.empty
                case _ =>
                  ko
              }
              //
              (1 to 9).foreach { index =>
                members(index).index mustEqual 9 - index
                members(index).info match {
                  case Some(SquadPositionDetail(None, Some(role), None, None, None, None)) =>
                    role mustEqual ""
                  case _ =>
                    ko
                }
              }
            case _ =>
              ko
          }
        case _ =>
          ko
      }
      ok
    }

    "decode (full squad)" in {
      PacketCoding.DecodePacket(string_full).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(3)
          detail match {
            case SquadDetail(Some(u1), Some(u2), Some(char_id), Some(u3), Some(leader), Some(task), Some(zone), Some(unk7), Some(member_list)) =>
              u1 mustEqual 3
              u2 mustEqual 1792
              char_id mustEqual 42771010L
              u3 mustEqual 529745L
              leader mustEqual "HofD"
              task mustEqual "\\#ffdc00***\\#9640ff=KOK+SPC+FLY=\\#ffdc00***\\#FF4040 All Welcome"
              zone mustEqual PlanetSideZoneID(7)
              unk7 mustEqual 4983296
              member_list.size mustEqual 10
              member_list.head mustEqual SquadPositionEntry(0,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#ff0000 |||||||||||||||||||||||"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(0),
                  Some("")))
              )
              member_list(1) mustEqual SquadPositionEntry(1,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#ffdc00   C"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(0),
                  Some("")))
              )
              member_list(2) mustEqual SquadPositionEntry(2,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#ffdc00   H"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(42644970L),
                  Some("OpolE")
                )
              ))
              member_list(3) mustEqual SquadPositionEntry(3,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#ffdc00    I"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(41604210L),
                  Some("BobaF3tt907")
                )
              ))
              member_list(4) mustEqual SquadPositionEntry(4,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#ffdc00   N"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(0),
                  Some("")
                )
              ))
              member_list(5) mustEqual SquadPositionEntry(5,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#ffdc00   A"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(0),
                  Some("")
                )
              ))
              member_list(6) mustEqual SquadPositionEntry(6,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#ff0000 |||||||||||||||||||||||"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(0),
                  Some("")
                )
              ))
              member_list(7) mustEqual SquadPositionEntry(7,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#9640ff   K"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(0),
                  Some("")
                )
              ))
              member_list(8) mustEqual SquadPositionEntry(8,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#9640ff   O"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(42771010L),
                  Some("HofD")
                )
              ))
              member_list(9) mustEqual SquadPositionEntry(9,Some(
                SquadPositionDetail(
                  Some(false),
                  Some("\\#9640ff   K"),
                  Some(""),
                  Some(Set(CertificationType.StandardAssault, CertificationType.StandardExoSuit, CertificationType.AgileExoSuit)),
                  Some(0),
                  Some("")
                )
              ))
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode (mixed)" in {
      PacketCoding.DecodePacket(string_mixed).require match {
        case SquadDetailDefinitionUpdateMessage(guid, detail) =>
          guid mustEqual PlanetSideGUID(3)
          detail match {
            case SquadDetail(None, None, None, None, None, Some(task), None, None, Some(member_list)) =>
              task mustEqual "PSForever Packet Collection"
              member_list.size mustEqual 10
              member_list.head mustEqual SquadPositionEntry(9,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                ))
              )
              member_list(1) mustEqual SquadPositionEntry(8,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                ))
              )
              member_list(2) mustEqual SquadPositionEntry(7,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                )
              ))
              member_list(3) mustEqual SquadPositionEntry(6,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                )
              ))
              member_list(4) mustEqual SquadPositionEntry(5,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                )
              ))
              member_list(5) mustEqual SquadPositionEntry(4,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                )
              ))
              member_list(6) mustEqual SquadPositionEntry(3,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                )
              ))
              member_list(7) mustEqual SquadPositionEntry(2,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                )
              ))
              member_list(8) mustEqual SquadPositionEntry(1,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                )
              ))
              member_list(9) mustEqual SquadPositionEntry(0,Some(
                SquadPositionDetail(
                  Some(false),
                  Some(""),
                  Some(""),
                  Some(Set.empty),
                  Some(0),
                  Some("")
                )
              ))
            case _ =>
              ko
          }
        case _ =>
          ko
      }
      ok
    }

    "encode (unk1 + members)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(3),
        SquadDetail()
          .Field1(0)
          .LeaderCharId(1221560L)
          .Members(List(
            SquadPositionEntry(6, SquadPositionDetail().Player(0L, ""))
          ))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_unk1
    }

    "encode (char id)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(5),
        SquadDetail().LeaderCharId(30910985L)
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_leader_char_id
    }

    "encode (unk3 + leader name)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(3),
        SquadDetail()
          .Leader(42631712L, "Jaako")
          .Field3(556403L)
          .Members(List(
            SquadPositionEntry(0, SquadPositionDetail().Player(0L, ""))
          ))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_unk3LeaderName
    }

    "encode (task)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(5),
        SquadDetail().Task("All Welcome ")
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_task
    }

    "encode (zone)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(3),
        SquadDetail().ZoneId(PlanetSideZoneID(21))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_zone
    }

    "encode (task + zone)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(2),
        SquadDetail()
          .Task("\\#FF0000 The \\#ffffff Blades")
          .ZoneId(PlanetSideZoneID(4))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_taskZone
    }

    "encode (unk7 + members)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(3),
        SquadDetail()
          .Task("The King's Squad")
          .Field7(8)
          .Members(List(
            SquadPositionEntry(9, SquadPositionDetail().Role("The Guard")),
            SquadPositionEntry(8, SquadPositionDetail().Role("The Knight")),
            SquadPositionEntry(7, SquadPositionDetail().Role("The Earl")),
            SquadPositionEntry(6, SquadPositionDetail().Role("The Lord")),
            SquadPositionEntry(5, SquadPositionDetail().Role("The Duke")),
            SquadPositionEntry(4, SquadPositionDetail().Role("The Baron")),
            SquadPositionEntry(3, SquadPositionDetail().Role("The Princess")),
            SquadPositionEntry(2, SquadPositionDetail().Role("The Prince")),
            SquadPositionEntry(1, SquadPositionDetail().Role("The Queen")),
            SquadPositionEntry(0, SquadPositionDetail().Role("The King"))
          ))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_unk7
    }

    "encode (member closed)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(3),
        SquadDetail()
          .Members(List(
            SquadPositionEntry(5, SquadPositionDetail.Closed)
          ))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_member_closed
    }


    "encode (member role)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(7),
        SquadDetail()
          .Members(List(
            SquadPositionEntry(0, SquadPositionDetail().Role("Commander"))
          ))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_member_role
    }

    "encode (member role + requirements)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(1),
        SquadDetail()
          .Members(List(
            SquadPositionEntry(6, SquadPositionDetail()
              .Role("ADV Hacker")
              .Requirements(Set(CertificationType.AdvancedHacking)))
          ))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_member_roleRequirements
    }

    "encode (member char id + name)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(3),
        SquadDetail()
          .Members(List(
          SquadPositionEntry(5, SquadPositionDetail().Player(1218249L, "Duckmaster43"))
        ))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_member_charIdName
    }

    "encode (task + member etc)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(1),
        SquadDetail()
          .Task("\\#FF0000 The \\#ffffff Blades")
          .Members(List(
            SquadPositionEntry(9, SquadPositionDetail().Role("").Requirements(Set())),
            SquadPositionEntry(8, SquadPositionDetail().Role("")),
            SquadPositionEntry(7, SquadPositionDetail().Role("")),
            SquadPositionEntry(6, SquadPositionDetail().Role("")),
            SquadPositionEntry(5, SquadPositionDetail().Role("")),
            SquadPositionEntry(4, SquadPositionDetail().Role("")),
            SquadPositionEntry(3, SquadPositionDetail().Role("")),
            SquadPositionEntry(2, SquadPositionDetail().Role("")),
            SquadPositionEntry(1, SquadPositionDetail().Role("")),
            SquadPositionEntry(0, SquadPositionDetail().Role(""))
          ))
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_task_memberEtc
    }

    "encode (full squad)" in {
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(3),
        SquadDetail(
          3,
          1792,
          42771010L,
          529745L,
          "HofD",
          "\\#ffdc00***\\#9640ff=KOK+SPC+FLY=\\#ffdc00***\\#FF4040 All Welcome",
          PlanetSideZoneID(7),
          4983296,
          List(
            SquadPositionEntry(0, SquadPositionDetail("\\#ff0000 |||||||||||||||||||||||", "", Set(), 0, "")),
            SquadPositionEntry(1, SquadPositionDetail("\\#ffdc00   C", "", Set(), 0, "")),
            SquadPositionEntry(2, SquadPositionDetail("\\#ffdc00   H", "", Set(), 42644970L, "OpolE")),
            SquadPositionEntry(3, SquadPositionDetail("\\#ffdc00    I", "", Set(), 41604210L, "BobaF3tt907")),
            SquadPositionEntry(4, SquadPositionDetail("\\#ffdc00   N", "", Set(), 0, "")),
            SquadPositionEntry(5, SquadPositionDetail("\\#ffdc00   A", "", Set(), 0, "")),
            SquadPositionEntry(6, SquadPositionDetail("\\#ff0000 |||||||||||||||||||||||", "", Set(), 0, "")),
            SquadPositionEntry(7, SquadPositionDetail("\\#9640ff   K", "", Set(), 0, "")),
            SquadPositionEntry(8, SquadPositionDetail("\\#9640ff   O", "", Set(), 42771010L ,"HofD")),
            SquadPositionEntry(9, SquadPositionDetail("\\#9640ff   K", "", Set(), 0, ""))
          )
        )
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_full
    }

    "encode (mixed)" in {
      val position = Some(SquadPositionDetail("", "", Set(), 0, ""))
      val msg = SquadDetailDefinitionUpdateMessage(
        PlanetSideGUID(3),
        SquadDetail
          .Task("PSForever Packet Collection")
          .Members((0 to 9).map { index => SquadPositionEntry(index, position) }.reverse.toList)
      )
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
      pkt mustEqual string_mixed
    }
  }
}
