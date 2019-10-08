// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.CertificationType
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
  object SearchMode extends Enumeration {
    type Type = Value

    val
    AnyPositions,
    AvailablePositions,
    SomeCertifications,
    AllCertifications
    = Value

    implicit val codec : Codec[SearchMode.Value] = PacketHelpers.createEnumerationCodec(enum = this, uint(bits = 3))
  }

  final case class DisplaySquad() extends SquadAction(0)

  /**
    * Dispatched from client to server to indicate a squad detail update that has no foundation entry to update?
    * Not dissimilar from `DisplaySquad`.
    */
  final case class SquadMemberInitializationIssue() extends SquadAction(1)

  final case class SaveSquadFavorite() extends SquadAction(3)

  final case class LoadSquadFavorite() extends SquadAction(4)

  final case class DeleteSquadFavorite() extends SquadAction(5)

  final case class ListSquadFavorite(name : String) extends SquadAction(7)

  final case class RequestListSquad() extends SquadAction(8)

  final case class StopListSquad() extends SquadAction(9)

  final case class SelectRoleForYourself(state : Int) extends SquadAction(10)

  final case class CancelSelectRoleForYourself(value: Long = 0) extends SquadAction(15)

  final case class AssociateWithSquad() extends SquadAction(16)

  final case class SetListSquad() extends SquadAction(17)

  final case class ChangeSquadPurpose(purpose : String) extends SquadAction(19)

  final case class ChangeSquadZone(zone : PlanetSideZoneID) extends SquadAction(20)

  final case class CloseSquadMemberPosition(position : Int) extends SquadAction(21)

  final case class AddSquadMemberPosition(position : Int) extends SquadAction(22)

  final case class ChangeSquadMemberRequirementsRole(u1 : Int, role : String) extends SquadAction(23)

  final case class ChangeSquadMemberRequirementsDetailedOrders(u1 : Int, orders : String) extends SquadAction(24)

  final case class ChangeSquadMemberRequirementsCertifications(u1 : Int, certs : Set[CertificationType.Value]) extends SquadAction(25)

  final case class ResetAll() extends SquadAction(26)

  final case class AutoApproveInvitationRequests(state : Boolean) extends SquadAction(28)

  final case class LocationFollowsSquadLead(state : Boolean) extends SquadAction(31)

  final case class SearchForSquadsWithParticularRole(role: String, requirements : Set[CertificationType.Value], zone_id: Int, mode : SearchMode.Value) extends SquadAction(34)

  final case class CancelSquadSearch() extends SquadAction(35)

  final case class AssignSquadMemberToRole(position : Int, char_id : Long) extends SquadAction(38)

  final case class NoSquadSearchResults() extends SquadAction(39)

  final case class FindLfsSoldiersForRole(state : Int) extends SquadAction(40)

  final case class CancelFind() extends SquadAction(41)

  final case class Unknown(badCode : Int, data : BitVector) extends SquadAction(badCode)

  object Unknown {
    import scodec.bits._
    val StandardBits : BitVector = hex"00".toBitVector.take(6)

    def apply(badCode : Int) : Unknown = Unknown(badCode, StandardBits)
  }

  /**
    * The `Codec`s used to transform the input stream into the context of a specific action
    * and extract the field data from that stream.
    */
  object Codecs {
    private val everFailCondition = conditional(included = false, bool)

    val displaySquadCodec = everFailCondition.xmap[DisplaySquad] (
      _ => DisplaySquad(),
      {
        case DisplaySquad() => None
      }
    )

    val squadMemberInitializationIssueCodec = everFailCondition.xmap[SquadMemberInitializationIssue] (
      _ => SquadMemberInitializationIssue(),
      {
        case SquadMemberInitializationIssue() => None
      }
    )

    val saveSquadFavoriteCodec = everFailCondition.xmap[SaveSquadFavorite] (
      _ => SaveSquadFavorite(),
      {
        case SaveSquadFavorite() => None
      }
    )

    val loadSquadFavoriteCodec = everFailCondition.xmap[LoadSquadFavorite] (
      _ => LoadSquadFavorite(),
      {
        case LoadSquadFavorite() => None
      }
    )

    val deleteSquadFavoriteCodec = everFailCondition.xmap[DeleteSquadFavorite] (
      _ => DeleteSquadFavorite(),
      {
        case DeleteSquadFavorite() => None
      }
    )

    val listSquadFavoriteCodec = PacketHelpers.encodedWideStringAligned(6).xmap[ListSquadFavorite] (
      text => ListSquadFavorite(text),
      {
        case ListSquadFavorite(text) => text
      }
    )

    val requestListSquadCodec = everFailCondition.xmap[RequestListSquad] (
      _ => RequestListSquad(),
      {
        case RequestListSquad() => None
      }
    )

    val stopListSquadCodec = everFailCondition.xmap[StopListSquad] (
      _ => StopListSquad(),
      {
        case StopListSquad() => None
      }
    )

    val selectRoleForYourselfCodec = uint4.xmap[SelectRoleForYourself] (
      value => SelectRoleForYourself(value),
      {
        case SelectRoleForYourself(value) => value
      }
    )

    val cancelSelectRoleForYourselfCodec = uint32.xmap[CancelSelectRoleForYourself] (
      value => CancelSelectRoleForYourself(value),
      {
        case CancelSelectRoleForYourself(value) => value
      }
    )

    val associateWithSquadCodec = everFailCondition.xmap[AssociateWithSquad] (
      _ => AssociateWithSquad(),
      {
        case AssociateWithSquad() => None
      }
    )

    val setListSquadCodec = everFailCondition.xmap[SetListSquad] (
      _ => SetListSquad(),
      {
        case SetListSquad() => None
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

    val changeSquadMemberRequirementsCertificationsCodec = (uint4 :: ulongL(46)).xmap[ChangeSquadMemberRequirementsCertifications] (
      {
        case u1 :: u2 :: HNil =>
          ChangeSquadMemberRequirementsCertifications(u1, CertificationType.fromEncodedLong(u2))
      },
      {
        case ChangeSquadMemberRequirementsCertifications(u1, u2) =>
          u1 :: CertificationType.toEncodedLong(u2) :: HNil
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
        SearchMode.codec).xmap[SearchForSquadsWithParticularRole] (
      {
        case u1 :: u2 :: u3 :: u4 :: HNil => SearchForSquadsWithParticularRole(u1, CertificationType.fromEncodedLong(u2), u3, u4)
      },
      {
        case SearchForSquadsWithParticularRole(u1, u2, u3, u4) => u1 :: CertificationType.toEncodedLong(u2) :: u3 :: u4 :: HNil
      }
    )

    val cancelSquadSearchCodec = everFailCondition.xmap[CancelSquadSearch] (
      _ => CancelSquadSearch(),
      {
        case CancelSquadSearch() => None
      }
    )

    val assignSquadMemberToRoleCodec = (uint4 :: uint32L).xmap[AssignSquadMemberToRole] (
      {
        case u1 :: u2 :: HNil => AssignSquadMemberToRole(u1, u2)
      },
      {
        case AssignSquadMemberToRole(u1, u2) => u1 :: u2 :: HNil
      }
    )

    val noSquadSearchResultsCodec = everFailCondition.xmap[NoSquadSearchResults] (
      _ => NoSquadSearchResults(),
      {
        case NoSquadSearchResults() => None
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
  * &nbsp;&nbsp;&nbsp;&nbsp;`0 ` - Display Squad<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`1 ` - Answer Squad Join Request<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`2 ` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`3 ` - Save Squad Favorite<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`4 ` - Load Squad Favorite<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`5 ` - Delete Squad Favorite<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`6 ` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`8 ` - Request List Squad<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`9 ` - Stop List Squad<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`16` - Associate with Squad<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`17` - Set List Squad (ui)<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`18` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`26` - Reset All<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`32` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`35` - Cancel Squad Search<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`39` - No Squad Search Results<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`41` - Cancel Find<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`42` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`43` - UNKNOWN<br>
  * &nbsp;&nbsp;`Boolean`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`28` - Auto-approve Requests for Invitation<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`29` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`30` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`31` - Location Follows Squad Leader<br>
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
  * &nbsp;&nbsp;&nbsp;&nbsp;`15` - Select this Role for Yourself<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`37` - UNKNOWN<br>
  * &nbsp;&nbsp;`String`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`7 ` - List Squad Favorite<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`19` - (Squad leader) Change Squad Purpose<br>
  * &nbsp;&nbsp;`Int :: Long`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`12` - UNKNOWN<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`25` - (Squad leader) Change Squad Member Requirements - Weapons<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`38` - Assign Squad Member To Role<br>
  * &nbsp;&nbsp;`Int :: String`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`23` - (Squad leader) Change Squad Member Requirements - Role<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`24` - (Squad leader) Change Squad Member Requirements - Detailed Orders<br>
  * &nbsp;&nbsp;`Long :: Long`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`36` - UNKNOWN<br>
  * &nbsp;&nbsp;`String :: Long :: Int :: Int`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`34` - Search for Squads with a Particular Role
  * @param squad_guid the unique identifier of the squad, if non-zero
  * @param line the original listing line number, if applicable
  * @param action the purpose of this packet;
  *               also decides the content of the parameter fields
  */
final case class SquadDefinitionActionMessage(squad_guid : PlanetSideGUID,
                                              line : Int,
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
      case 0 => displaySquadCodec
      case 1 => squadMemberInitializationIssueCodec
      case 3 => saveSquadFavoriteCodec
      case 4 => loadSquadFavoriteCodec
      case 5 => deleteSquadFavoriteCodec
      case 7 => listSquadFavoriteCodec
      case 8 => requestListSquadCodec
      case 9 => stopListSquadCodec
      case 10 => selectRoleForYourselfCodec
      case 15 => cancelSelectRoleForYourselfCodec
      case 16 => associateWithSquadCodec
      case 17 => setListSquadCodec
      case 19 => changeSquadPurposeCodec
      case 20 => changeSquadZoneCodec
      case 21 => closeSquadMemberPositionCodec
      case 22 => addSquadMemberPositionCodec
      case 23 => changeSquadMemberRequirementsRoleCodec
      case 24 => changeSquadMemberRequirementsDetailedOrdersCodec
      case 25 => changeSquadMemberRequirementsCertificationsCodec
      case 26 => resetAllCodec
      case 28 => autoApproveInvitationRequestsCodec
      case 31 => locationFollowsSquadLeadCodec
      case 34 => searchForSquadsWithParticularRoleCodec
      case 35 => cancelSquadSearchCodec
      case 38 => assignSquadMemberToRoleCodec
      case 39 => noSquadSearchResultsCodec
      case 40 => findLfsSoldiersForRoleCodec
      case 41 => cancelFindCodec
      case  2 |  6 | 11 | 12 | 13 |
           14 | 18 | 29 | 30 | 32 |
           33 | 36 | 37 | 42 | 43 => unknownCodec(code)
      case _ => failureCodec(code)
    }).asInstanceOf[Codec[SquadAction]]
  }

  implicit val codec : Codec[SquadDefinitionActionMessage] = (
    uintL(6) >>:~ { code =>
      ("squad_guid" | PlanetSideGUID.codec) ::
        ("line" | uint4L) ::
        ("action" | selectFromActionCode(code))
    }
    ).xmap[SquadDefinitionActionMessage] (
    {
      case _ :: guid :: line :: action :: HNil =>
        SquadDefinitionActionMessage(guid, line, action)
    },
    {
      case SquadDefinitionActionMessage(guid, line, action) =>
        action.code :: guid :: line :: action :: HNil
    }
  )
}
