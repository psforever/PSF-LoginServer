// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.CertificationType
import scodec.bits.BitVector
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.HNil

import scala.annotation.tailrec

final case class SquadPositionDetail(is_closed : Boolean,
                                     role : String,
                                     detailed_orders : String,
                                     requirements : Set[CertificationType.Value],
                                     char_id : Long,
                                     name : String)

final case class SquadDetailDefinitionUpdateMessage(guid : PlanetSideGUID,
                                                    unk : BitVector,
                                                    leader_name : String,
                                                    task : String,
                                                    zone_id : PlanetSideZoneID,
                                                    member_info : List[SquadPositionDetail])
  extends PlanetSideGamePacket {
  type Packet = SquadDetailDefinitionUpdateMessage
  def opcode = GamePacketOpcode.SquadDetailDefinitionUpdateMessage
  def encode = SquadDetailDefinitionUpdateMessage.encode(this)
}

object SquadPositionDetail {
  final val Closed : SquadPositionDetail = SquadPositionDetail(is_closed = true, "", "", Set.empty, 0L, "")

  private def reliableNameHash(name : String) : Long = {
    val hash = name.hashCode.toLong
    if(hash < 0) {
      -1L * hash
    }
    else {
      hash
    }
  }

  def apply() : SquadPositionDetail = SquadPositionDetail(is_closed = false, "", "", Set.empty, 0L, "")

  def apply(name : String) : SquadPositionDetail = SquadPositionDetail(is_closed = false, "", "", Set.empty, reliableNameHash(name), name)

  def apply(role : String, detailed_orders : String) : SquadPositionDetail = SquadPositionDetail(is_closed = false, role, detailed_orders, Set.empty, 0L, "")

  def apply(role : String, detailed_orders : String, requirements : Set[CertificationType.Value]) : SquadPositionDetail = SquadPositionDetail(is_closed = false, role, detailed_orders, requirements, 0L, "")

  def apply(role : String, detailed_orders : String, name : String) : SquadPositionDetail = SquadPositionDetail(is_closed = false, role, detailed_orders, Set.empty, reliableNameHash(name), name)

  def apply(role : String, detailed_orders : String, requirements : Set[CertificationType.Value], name : String) : SquadPositionDetail = SquadPositionDetail(is_closed = false, role, detailed_orders, requirements, reliableNameHash(name), name)
}

object SquadDetailDefinitionUpdateMessage extends Marshallable[SquadDetailDefinitionUpdateMessage] {
  final val defaultRequirements : Set[CertificationType.Value] = Set(
    CertificationType.StandardAssault,
    CertificationType.StandardExoSuit,
    CertificationType.AgileExoSuit
  )

  final val Init = SquadDetailDefinitionUpdateMessage(
    PlanetSideGUID(0),
    "",
    "",
    PlanetSideZoneID(0),
    List(
      SquadPositionDetail(),
      SquadPositionDetail(),
      SquadPositionDetail(),
      SquadPositionDetail(),
      SquadPositionDetail(),
      SquadPositionDetail(),
      SquadPositionDetail(),
      SquadPositionDetail(),
      SquadPositionDetail(),
      SquadPositionDetail()
    )
  )

  def apply(guid : PlanetSideGUID, leader_name : String, task : String, zone_id : PlanetSideZoneID, member_info : List[SquadPositionDetail]) : SquadDetailDefinitionUpdateMessage = {
    import scodec.bits._
    SquadDetailDefinitionUpdateMessage(guid, hex"080000000000000000000".toBitVector, leader_name, task, zone_id, member_info)
  }

  private def memberCodec(pad : Int) : Codec[SquadPositionDetail] = {
    import shapeless.::
    (
        uint8 :: //required value = 6
        ("is_closed" | bool) :: //if all positions are closed, the squad detail menu display no positions at all
        ("role" | PacketHelpers.encodedWideStringAligned(pad)) ::
        ("detailed_orders" | PacketHelpers.encodedWideString) ::
        ("char_id" | uint32L) ::
        ("name" | PacketHelpers.encodedWideString) ::
        ("requirements" | ulongL(46))
      ).exmap[SquadPositionDetail] (
      {
        case 6 :: closed :: role :: orders :: char_id :: name :: requirements :: HNil =>
          Attempt.Successful(
            SquadPositionDetail(closed, role, orders, defaultRequirements ++ CertificationType.fromEncodedLong(requirements), char_id, name)
          )
        case data =>
          Attempt.Failure(Err(s"can not decode a SquadDetailDefinitionUpdate member's data - $data"))
      },
      {
        case SquadPositionDetail(closed, role, orders, requirements, char_id, name) =>
          Attempt.Successful(6 :: closed :: role :: orders :: char_id :: name :: CertificationType.toEncodedLong(defaultRequirements ++ requirements) :: HNil)
      }
    )
  }

  private val first_member_codec : Codec[SquadPositionDetail] = memberCodec(pad = 7)

  private val member_codec : Codec[SquadPositionDetail] = memberCodec(pad = 0)

  private case class LinkedMemberList(member : SquadPositionDetail, next : Option[LinkedMemberList])

  private def subsequent_member_codec : Codec[LinkedMemberList] = {
    import shapeless.::
    (
      //disruptive coupling action (e.g., flatPrepend) necessary to allow for recursive Codec
      ("member" | member_codec) >>:~ { _ =>
        optional(bool, "next" | subsequent_member_codec).hlist
      }
      ).xmap[LinkedMemberList] (
      {
        case a :: b :: HNil =>
          LinkedMemberList(a, b)
      },
      {
        case LinkedMemberList(a, b) =>
          a :: b :: HNil
      }
    )
  }

  private def initial_member_codec : Codec[LinkedMemberList] = {
    import shapeless.::
    (
      ("member" | first_member_codec) ::
        optional(bool, "next" | subsequent_member_codec)
      ).xmap[LinkedMemberList] (
      {
        case a :: b :: HNil =>
          LinkedMemberList(a, b)
      },
      {
        case LinkedMemberList(a, b) =>
          a :: b :: HNil
      }
    )
  }

  @tailrec
  private def unlinkMemberList(list : LinkedMemberList, out : List[SquadPositionDetail] = Nil) : List[SquadPositionDetail] = {
    list.next match {
      case None =>
        out :+ list.member
      case Some(next) =>
        unlinkMemberList(next, out :+ list.member)
    }
  }

  private def linkMemberList(list : List[SquadPositionDetail]) : LinkedMemberList = {
    list match {
      case Nil =>
        throw new Exception("")
      case x :: Nil =>
        LinkedMemberList(x, None)
      case x :: xs =>
        linkMemberList(xs, LinkedMemberList(x, None))
    }
  }

  @tailrec
  private def linkMemberList(list : List[SquadPositionDetail], out : LinkedMemberList) : LinkedMemberList = {
    list match {
      case Nil =>
        out
      case x :: Nil =>
        LinkedMemberList(x, Some(out))
      case x :: xs =>
        linkMemberList(xs, LinkedMemberList(x, Some(out)))
    }
  }

  implicit val codec : Codec[SquadDetailDefinitionUpdateMessage] = {
    import shapeless.::
    (
      ("guid" | PlanetSideGUID.codec) ::
        uint8 ::
        uint4 ::
        bits(83) :: //variable fields, but can be 0'd
        uint(10) :: //constant = 0
        ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
        ("task" | PacketHelpers.encodedWideString) ::
        ("zone_id" | PlanetSideZoneID.codec) ::
        uint(23) :: //constant = 4983296
        optional(bool, "member_info" | initial_member_codec)
      ).exmap[SquadDetailDefinitionUpdateMessage] (
      {
        case guid :: _ :: _ :: _ :: _ :: leader :: task :: zone :: _ :: Some(member_list) :: HNil =>
          Attempt.Successful(SquadDetailDefinitionUpdateMessage(guid, leader, task, zone, unlinkMemberList(member_list)))
        case data =>
          Attempt.failure(Err(s"can not get squad detail definition from data $data"))
      },
      {
        case SquadDetailDefinitionUpdateMessage(guid, unk, leader, task, zone, member_list) =>
          Attempt.Successful(guid :: 132 :: 8 :: unk.take(83) :: 0 :: leader :: task :: zone :: 4983296 :: Some(linkMemberList(member_list.reverse)) :: HNil)
      }
    )
  }
}
