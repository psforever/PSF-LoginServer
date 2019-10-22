// Copyright (c) 2019 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.SquadRequestType
import org.specs2.mutable._
import scodec.bits._

class SquadMembershipRequestTest extends Specification {
  //481c897e-b47b-41cc-b7ad-c604606d985e / PSCap-2016-03-18_12-48-12-PM.gcap / Game record 5521 at 662.786844s
  val string1 = hex"6e015aa7a0224d87a0280000"
  //... / PSCap-2016-06-29_07-49-26-PM (last).gcap / Game record 77 at 9.732430 (found in MultiPacket)
  val string2 = hex"6E265DD7A02800"
  //TODO find example where player_name field is defined
  //TODO find example where unk field is defined and is a string

  "decode (1)" in {
    PacketCoding.DecodePacket(string1).require match {
      case SquadMembershipRequest(req_type, unk2, unk3, p_name, unk5) =>
        req_type mustEqual SquadRequestType.Invite
        unk2 mustEqual 41593365L
        unk3.contains(41605156L) mustEqual true
        p_name mustEqual ""
        unk5.contains(None) mustEqual true
      case _ =>
        ko
    }
  }

  "decode (2)" in {
    PacketCoding.DecodePacket(string2).require match {
      case SquadMembershipRequest(req_type, unk2, unk3, p_name, unk5) =>
        req_type mustEqual SquadRequestType.Accept
        unk2 mustEqual 41606501L
        unk3.isEmpty mustEqual true
        p_name mustEqual ""
        unk5.isEmpty mustEqual true
      case _ =>
        ko
    }
  }

  "encode (1)" in {
    val msg = SquadMembershipRequest(SquadRequestType.Invite, 41593365, Some(41605156L), "", Some(None))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string1
  }

  "encode (1; failure 1)" in {
    SquadMembershipRequest(SquadRequestType.Invite, 41593365, None, "", Some(None)) must throwA[AssertionError]
  }

  "encode (1; failure 2)" in {
    SquadMembershipRequest(SquadRequestType.Invite, 41593365, Some(41605156L), "", None) must throwA[AssertionError]
  }

  "encode (2)" in {
    val msg = SquadMembershipRequest(SquadRequestType.Accept, 41606501, None, "", None)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector
    pkt mustEqual string2
  }

  "encode (2; failure)" in {
    SquadMembershipRequest(SquadRequestType.Accept, 41606501, Some(41606501), "", None) must throwA[AssertionError]
  }
}
