// Copyright (c) 2023 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.OutfitMembershipResponseAction._
import net.psforever.packet.game.OutfitMembershipResponse.ResponseType
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable._
import scodec.bits._

class OutfitMembershipResponseTest extends Specification {

  /*
  Outfit Create request that results in "someResponse" packet below
  C >> S
  OutfitMembershipRequest(Type = Create (0), AvatarID = ValidPlanetSideGUID(43541), 634, CreateOutfit(, 0, false, PlanetSide_Forever_Vanu))
  0x8c 0 2b54 f405 000 97 50006c0061006e006500740053006900640065005f0046006f00720065007600650072005f00560061006e007500))
  */
  val createResponse = hex"8d 0 00 2b54     f404        0000              0001     00 0 80 80" // response to create
  // validity unknown
  //val unk0_0       = hex"8d 0 11 2600     0000        c2b8              1a02     28c0 0000 a037 2340 4598 0000 0010 1284 dd0d 4060 0000 280d 2080 1176 0000 0004 04a1 3021 9018 0000 0"

  //                               ?        ?           ?                               xNick                                       PlanetSide_Forever_TR
  val new2           = hex"8d 2 01 bb39     9e03        ddb4              f405     0a 0 78004e00690063006b00                     95 50006c0061006e006500740053006900640065005f0046006f00720065007600650072005f0054005200 00"

  //                               AvatarID OutfitID-1  TargetID/LeaderID OutfitID      Zergling92)                                 PlanetSide_Forever_Vanu
  val someOther      = hex"8d 4 00 49b0     f404        2b54              f405     14 0 5a006500720067006c0069006e00670039003200 97 50006c0061006e006500740053006900640065005f0046006f00720065007600650072005f00560061006e007500 00"
  val someOther2     = hex"8d 4 01 ddb4     f405        bb39              9e03     14 0 48006100480061004100540052004d0061007800 95 50006c0061006e006500740053006900640065005f0046006f00720065007600650072005f0054005200 80"
  //                                                                                    HaHaATRMax                                  PlanetSide_Forever_TR

  // unk validity
  val muh6           = hex"8d 6 64 b351     2cf3        f2ef              8040     80 0 201bb4088d1abe638d8b6b62133d81ffad501e3e1f0000083014d5948e886b3517d6404b0004028020059408681a38a68db8cb7c133f807fba501bff110aec70450569c2a000314e569f1187e9f9c00380083c30aa83879c3b6213ebbeecf8040d5fe0076408d40ccc948b488b35170381001590"
  val muh61          = hex"8d 6 00 e8c2     f405        10d3              b603     00 0 80 80"

  //
  val blubOther      = hex"8d 8 00 2b54     f404        0000              0001     0   00                                                          80 80"

  //                                                                                      PlayerName (PSFoutfittest1)
  val yetAnOther     = hex"8d a 02 2b54     f405        1fb0              f405     1   c0 5000530046006f007500740066006900740074006500730074003100 80 80"
  //                                                                                      VSsulferix
  val blubBlah       = hex"8d a 03 8afa     f404        2b54              f405     1   40 56005300730075006c0066006500720069007800                 80 00"

  // validity unknown
  val blah           = hex"8d e 37 0660     3000        0002              6b38     4   a050 0000 0020 2429 6010 80c0 0000 2c91 a0f8 2400 0000 0808 383cc7a0300000082a68420d00000002023785e2280c000006a115138440000000808388588203000001ca6520661b0000000202439351580c00000299331287c00000008090e5b84e03000000ac650662300000002020a88c2280c000002a1bb1789c00000008082a35c3e03000000a86484e2b00000002020a8f81280c000000"


  "decode CreateResponse" in {
    PacketCoding.decodePacket(createResponse).require match {
      case OutfitMembershipResponse(request_type, u0, avatar_id, outfit_guid_1, target_guid, u3, action) =>
        request_type mustEqual ResponseType.CreateResponse
        u0 mustEqual 0
        avatar_id mustEqual PlanetSideGUID(43541)
        outfit_guid_1 mustEqual PlanetSideGUID(634)
        target_guid mustEqual PlanetSideGUID(0)
        u3 mustEqual 0
        action mustEqual CreateOutfitResponse("", "", "")
      case _ =>
        ko
    }
  }

  "encode CreateResponse" in {
    val msg = OutfitMembershipResponse(ResponseType.CreateResponse, 0, PlanetSideGUID(43541), PlanetSideGUID(634), PlanetSideGUID(0), 0, CreateOutfitResponse("", "", ""))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual createResponse
  }

  "decode Unk1" in {
    PacketCoding.decodePacket(new2).require match {
      case OutfitMembershipResponse(request_type, u0, avatar_id, outfit_guid_1, target_guid, u3, action) =>
        request_type mustEqual ResponseType.Unk1
        u0 mustEqual 0
        avatar_id mustEqual PlanetSideGUID(40157)
        outfit_guid_1 mustEqual PlanetSideGUID(463)
        target_guid mustEqual PlanetSideGUID(56046)
        u3 mustEqual 634
        action mustEqual Unk1OutfitResponse("xNick", "PlanetSide_Forever_TR", 0)
      case _ =>
        ko
    }
  }

  "encode Unk1" in {
    val msg = OutfitMembershipResponse(ResponseType.Unk1, 0, PlanetSideGUID(40157), PlanetSideGUID(463), PlanetSideGUID(56046), 634, Unk1OutfitResponse("xNick", "PlanetSide_Forever_TR", 0))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual new2
  }

  "decode Unk2" in {
    PacketCoding.decodePacket(someOther).require match {
      case OutfitMembershipResponse(request_type, u0, avatar_id, outfit_guid_1, target_guid, u3, action) =>
        request_type mustEqual ResponseType.Unk2
        u0 mustEqual 0
        avatar_id mustEqual PlanetSideGUID(55332)
        outfit_guid_1 mustEqual PlanetSideGUID(634)
        target_guid mustEqual PlanetSideGUID(43541)
        u3 mustEqual 634
        action mustEqual Unk2OutfitResponse("Zergling92", "PlanetSide_Forever_Vanu", 0)
      case _ =>
        ko
    }
  }

  "encode Unk2" in {
    val msg = OutfitMembershipResponse(ResponseType.Unk2, 0, PlanetSideGUID(55332), PlanetSideGUID(634), PlanetSideGUID(43541), 634, Unk2OutfitResponse("Zergling92", "PlanetSide_Forever_Vanu", 0))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual someOther
  }
}
