// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * The generic superclass of a specific behavior for this type of squad definition action.
  * All behaviors have a "code" that indicates how the rest of the data is parsed.
  * @param code the action behavior code
  */
abstract class SquadAction(val code : Int)

object SquadAction{
  final case class SaveSquadDefinition() extends SquadAction(3)

  final case class ListSquad() extends SquadAction(8)

  final case class SelectRoleForYourself(state : Int) extends SquadAction(10)

  final case class ChangeSquadPurpose(purpose : String) extends SquadAction(19)

  final case class ChangeSquadZone(zone : PlanetSideZoneID) extends SquadAction(20)

  final case class CloseSquadMemberPosition(position : Int) extends SquadAction(21)

  final case class AddSquadMemberPosition(position : Int) extends SquadAction(22)

  final case class ChangeSquadMemberRequirementsRole(u1 : Int, role : String) extends SquadAction(23)

  final case class ChangeSquadMemberRequirementsDetailedOrders(u1 : Int, orders : String) extends SquadAction(24)

  final case class ChangeSquadMemberRequirementsWeapons(u1 : Int, u2 : Long) extends SquadAction(25)

  final case class ResetAll() extends SquadAction(26)

  final case class AutoApproveInvitationRequests(state : Boolean) extends SquadAction(28)

  final case class LocationFollowsSquadLead(state : Boolean) extends SquadAction(31)

  final case class SearchForSquadsWithParticularRole(u1: String, u2 : Long, u3: Int, u4 : Int) extends SquadAction(34)

  final case class CancelSquadSearch() extends SquadAction(35)

  final case class FindLfsSoldiersForRole(state : Int) extends SquadAction(40)

  final case class CancelFind() extends SquadAction(41)

  final case class Unknown(badCode : Int, data : BitVector) extends SquadAction(badCode)

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val saveSquadDefinitionCodec = everFailCondition.xmap[SaveSquadDefinition] (
      _ => SaveSquadDefinition(),
      {
        case SaveSquadDefinition() => None
      }
    )

    val listSquadCodec = everFailCondition.xmap[ListSquad] (
      _ => ListSquad(),
      {
        case ListSquad() => None
      }
    )

    val selectRoleForYourselfCodec = uint4.xmap[SelectRoleForYourself] (
      value => SelectRoleForYourself(value),
      {
        case SelectRoleForYourself(value) => value
      }
    )

    val changeSquadPurposeCodec = PacketHelpers.encodedWideStringAligned(6).xmap[ChangeSquadPurpose] (
      purpose => ChangeSquadPurpose(purpose),
      {
        case ChangeSquadPurpose(purpose) => purpose
      }
    )

    val changeSquadZoneCodec = uint16L.xmap[ChangeSquadZone] (
      value => ChangeSquadZone(PlanetSideZoneID(value)),
      {
        case ChangeSquadZone(value) => value.zoneId.toInt
      }
    )

    val closeSquadMemberPositionCodec = uint4.xmap[CloseSquadMemberPosition] (
      position => CloseSquadMemberPosition(position),
      {
        case CloseSquadMemberPosition(position) => position
      }
    )

    val addSquadMemberPositionCodec = uint4.xmap[AddSquadMemberPosition] (
      position => AddSquadMemberPosition(position),
      {
        case AddSquadMemberPosition(position) => position
      }
    )

    val changeSquadMemberRequirementsRoleCodec = (uint4L :: PacketHelpers.encodedWideStringAligned(2)).xmap[ChangeSquadMemberRequirementsRole] (
      {
        case u1 :: role :: HNil => ChangeSquadMemberRequirementsRole(u1, role)
      },
      {
        case ChangeSquadMemberRequirementsRole(u1, role) => u1 :: role :: HNil
      }
    )

    val changeSquadMemberRequirementsDetailedOrdersCodec = (uint4L :: PacketHelpers.encodedWideStringAligned(2)).xmap[ChangeSquadMemberRequirementsDetailedOrders] (
      {
        case u1 :: role :: HNil => ChangeSquadMemberRequirementsDetailedOrders(u1, role)
      },
      {
        case ChangeSquadMemberRequirementsDetailedOrders(u1, role) => u1 :: role :: HNil
      }
    )

    val changeSquadMemberRequirementsWeaponsCodec = (uint4 :: ulongL(46)).xmap[ChangeSquadMemberRequirementsWeapons] (
      {
        case u1 :: u2 :: HNil => ChangeSquadMemberRequirementsWeapons(u1, u2)
      },
      {
        case ChangeSquadMemberRequirementsWeapons(u1, u2) => u1 :: u2 :: HNil
      }
    )

    val resetAllCodec = everFailCondition.xmap[ResetAll] (
      _ => ResetAll(),
      {
        case ResetAll() => None
      }
    )

    val autoApproveInvitationRequestsCodec = bool.xmap[AutoApproveInvitationRequests] (
      state => AutoApproveInvitationRequests(state),
      {
        case AutoApproveInvitationRequests(state) => state
      }
    )

    val locationFollowsSquadLeadCodec = bool.xmap[LocationFollowsSquadLead] (
      state => LocationFollowsSquadLead(state),
      {
        case LocationFollowsSquadLead(state) => state
      }
    )

    val searchForSquadsWithParticularRoleCodec = (
      PacketHelpers.encodedWideStringAligned(6) ::
        ulongL(46) ::
        uint16L ::
        uintL(3)).xmap[SearchForSquadsWithParticularRole] (
      {
        case u1 :: u2 :: u3 :: u4 :: HNil => SearchForSquadsWithParticularRole(u1, u2, u3, u4)
      },
      {
        case SearchForSquadsWithParticularRole(u1, u2, u3, u4) => u1 :: u2 :: u3 :: u4 :: HNil
      }
    )

    val cancelSquadSearchCodec = everFailCondition.xmap[CancelSquadSearch] (
      _ => CancelSquadSearch(),
      {
        case CancelSquadSearch() => None
      }
    )

    val findLfsSoldiersForRoleCodec = uint4.xmap[FindLfsSoldiersForRole] (
      state => FindLfsSoldiersForRole(state),
      {
        case FindLfsSoldiersForRole(state) => state
      }
    )

    val cancelFindCodec = everFailCondition.xmap[CancelFind] (
      _ => CancelFind(),
      {
        case CancelFind() => None
      }
    )

    /**
      * A common form for known action code indexes with an unknown purpose and transformation is an "Unknown" object.
      * @param action the action behavior code
      * @return a transformation between the action code and the unknown bit data
      */
    def unknownCodec(action : Int) = bits.xmap[Unknown] (
     data => Unknown(action, data),
      {
        case Unknown(_, data) => data
      }
    )

    /**
      * The action code was completely unanticipated!
      * @param action the action behavior code
      * @return nothing; always fail
      */
    def failureCodec(action : Int)= everFailCondition.exmap[SquadAction] (
      _ => Attempt.failure(Err(s"can not match a codec pattern for decoding $action")),
      _ => Attempt.failure(Err(s"can not match a codec pattern for encoding $action"))
    )
  }
}

/**
  * Manage composition and details of a player's current squad, or the currently-viewed squad.<br>
  * <br>
  * The `action` code indicates the format of the remainder data in the packet.
  * The following formats are translated; their purposes are listed:<br>
  * &nbsp;&nbsp;`(None)`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`0 ` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`1 ` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`2 ` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`3 ` - Save Squad Definition<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`4 ` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`6 ` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`8 ` - List Squad<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`9 ` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`16` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`17` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`18` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`26` - Reset All<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`35` - Cancel Squad Search<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`41` - Cancel Find<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`42` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`43` - UNKNOWN<br>
  * &nbsp;&nbsp;`Boolean`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`28` - Auto-approve Requests for Invitation<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`29` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`30` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`31` - Location Follows Squad Lead<br>
  * &nbsp;&nbsp;`Int`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`10` - Select this Role for Yourself<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`11` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`20` - (Squad leader) Change Squad Zone<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`21` - (Squad leader) Close Squad Member Position<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`22` - (Squad leader) Add Squad Member Position<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`33` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`40` - Find LFS Soldiers that Meet the Requirements for this Role<br>
  * &nbsp;&nbsp;`Long`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`13` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`14` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`15` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`37` - UNKNOWN<br>
  * &nbsp;&nbsp;`String`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`7 ` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`19` - (Squad leader) Change Squad Purpose<br>
  * &nbsp;&nbsp;`Int :: Long`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`12` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`25` - (Squad leader) Change Squad Member Requirements - Weapons<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`38` - UNKNOWN<br>
  * &nbsp;&nbsp;`Int :: String`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`23` - (Squad leader) Change Squad Member Requirements - Role<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`24` - (Squad leader) Change Squad Member Requirements - Detailed Orders<br>
  * &nbsp;&nbsp;`Long :: Long`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`36` - UNKNOWN<br>
  * &nbsp;&nbsp;`String :: Long :: Int :: Int`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`34` - Search for Squads with a Particular Role
  * @param unk1 na
  * @param unk2 na
  * @param action the purpose of this packet;
  *               also decides the content of the parameter fields
  */
final case class SquadDefinitionActionMessage(unk1 : Int,
                                              unk2 : Int,
                                              action : SquadAction)
  extends PlanetSideGamePacket {
  type Packet = SquadDefinitionActionMessage
  def opcode = GamePacketOpcode.SquadDefinitionActionMessage
  def encode = SquadDefinitionActionMessage.encode(this)
}

object SquadDefinitionActionMessage extends Marshallable[SquadDefinitionActionMessage] {
  /**
    * Use the action code to transform between
    * the specific action object and its field data
    * and the stream of bits of the original packet.
    * @param code the action behavior code
    * @return the `SquadAction` `Codec` to use for the given `code`
    */
  def selectFromActionCode(code : Int) : Codec[SquadAction] = {
    import SquadAction.Codecs._
    import scala.annotation.switch
    ((code : @switch) match {
      case 3 => saveSquadDefinitionCodec
      case 8 => listSquadCodec
      case 10 => selectRoleForYourselfCodec
      case 19 => changeSquadPurposeCodec
      case 20 => changeSquadZoneCodec
      case 21 => closeSquadMemberPositionCodec
      case 22 => addSquadMemberPositionCodec
      case 23 => changeSquadMemberRequirementsRoleCodec
      case 24 => changeSquadMemberRequirementsDetailedOrdersCodec
      case 25 => changeSquadMemberRequirementsWeaponsCodec
      case 26 => resetAllCodec
      case 28 => autoApproveInvitationRequestsCodec
      case 31 => locationFollowsSquadLeadCodec
      case 34 => searchForSquadsWithParticularRoleCodec
      case 35 => cancelSquadSearchCodec
      case 40 => findLfsSoldiersForRoleCodec
      case 41 => cancelFindCodec
      case 0 | 1 | 2 | 4 | 6 | 7 | 9 |
           11 | 12 | 13 | 14 | 15 | 16 |
           17 | 18 | 29 | 30 | 33 | 36 |
           37 | 38 | 42 | 43 => unknownCodec(code)
      case _ => failureCodec(code)
    }).asInstanceOf[Codec[SquadAction]]
  }

  implicit val codec : Codec[SquadDefinitionActionMessage] = (
    uintL(6) >>:~ { code =>
      ("unk1" | uint16L) ::
        ("unk2" | uint4L) ::
        ("action" | selectFromActionCode(code))
    }
    ).xmap[SquadDefinitionActionMessage] (
    {
      case _ :: u1 :: u2 :: action :: HNil =>
        SquadDefinitionActionMessage(u1, u2, action)
    },
    {
      case SquadDefinitionActionMessage(u1, u2, action) =>
        action.code :: u1 :: u2 :: action :: HNil
    }
  )
}

/*
("change" specifically indicates the perspective is from the SL; "update" indicates squad members other than the oen who made the change
("[#]" indicates the mode is detected but not properly parsed; the length of the combined fields may follow

[0] - clicking on a squad listed in the "Find Squad" tab / cancel squad search (6 bits/pad?)
[2] - ? (6 bits/pad?)
[3] - save squad favorite (6 bits/pad?)
[4] - load a squad definition favorite (6 bits/pad?)
[6] - ? (6 bits/pad?)
7 - ?
[8] - list squad (6 bits/pad?)
10 - select this role for yourself
11 - ?
12 - ?
13 - ?
14 - ?
15 - ?
[16] - ? (6 bits/pad?)
[17] - ? (6 bits/pad?)
[18] - ? (6 bits/pad?)
19 - change purpose
20 - change zone
21 - change/close squad member position
22 - change/add squad member position
23 - change squad member req role
24 - change squad member req detailed orders
25 - change squad member req weapons
[26] - reset all (6 bits/pad?)
28 - auto-approve requests for invitation
29 -
30 -
31 - location follows squad lead
[32] - ? (6 bits/pad?)
33 -
34 - search for squads with a particular role
36 -
37 -
38 -
[39] - ? (?)
40 - find LFS soldiers that meet the requirements for this role
[41] - cancel search for LFS soldiers (6 bits)
[42] - ? (6 bits/pad?)
[43] - ? (6 bits/pad?)
*/
