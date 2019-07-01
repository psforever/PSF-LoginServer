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

final case class OldSquadDetailDefinitionUpdateMessage(guid : PlanetSideGUID,
                                                    unk1 : Int,
                                                    leader_char_id : Long,
                                                    unk2 : BitVector,
                                                    leader_name : String,
                                                    task : String,
                                                    zone_id : PlanetSideZoneID,
                                                    member_info : List[SquadPositionDetail])

object SquadPositionDetail {
  final val Closed : SquadPositionDetail = SquadPositionDetail(is_closed = true, "", "", Set.empty, 0L, "")

  def apply() : SquadPositionDetail = SquadPositionDetail(is_closed = false, "", "", Set.empty, 0L, "")

  def apply(char_id : Long, name : String) : SquadPositionDetail = SquadPositionDetail(is_closed = false, "", "", Set.empty, char_id, name)

  def apply(role : String, detailed_orders : String) : SquadPositionDetail = SquadPositionDetail(is_closed = false, role, detailed_orders, Set.empty, 0L, "")

  def apply(role : String, detailed_orders : String, requirements : Set[CertificationType.Value]) : SquadPositionDetail = SquadPositionDetail(is_closed = false, role, detailed_orders, requirements, 0L, "")

  def apply(role : String, detailed_orders : String, char_id : Long, name : String) : SquadPositionDetail = SquadPositionDetail(is_closed = false, role, detailed_orders, Set.empty, char_id, name)

  def apply(role : String, detailed_orders : String, requirements : Set[CertificationType.Value], char_id : Long, name : String) : SquadPositionDetail = SquadPositionDetail(is_closed = false, role, detailed_orders, requirements, char_id, name)
}

object OldSquadDetailDefinitionUpdateMessage {
  def apply(guid : PlanetSideGUID, char_id : Long, leader_name : String, task : String, zone_id : PlanetSideZoneID, member_info : List[SquadPositionDetail]) : OldSquadDetailDefinitionUpdateMessage = {
    import scodec.bits._
    OldSquadDetailDefinitionUpdateMessage(guid, 1, char_id, hex"000000".toBitVector, leader_name, task, zone_id, member_info)
  }
}

//NEW FORM SquadDetailDefinitionUpdateMessage

private class StreamLengthToken(init : Int = 0) {
  private var bitLength : Int = init

  def Length : Int = bitLength

  def Length_=(toLength : Int) : StreamLengthToken = {
    bitLength = toLength
    this
  }

  def Add(more : Int) : StreamLengthToken = {
    bitLength += more
    this
  }
}

final case class SquadPositionDetail2(is_closed : Option[Boolean],
                                      role : Option[String],
                                      detailed_orders : Option[String],
                                      requirements : Option[Set[CertificationType.Value]],
                                      char_id : Option[Long],
                                      name : Option[String]) {
  def And(info : SquadPositionDetail2) : SquadPositionDetail2 = {
    SquadPositionDetail2(
      is_closed match {
        case Some(false) | None =>
          info.is_closed.orElse(is_closed)
        case _ =>
          Some(true)
      },
      role.orElse(info.role),
      detailed_orders.orElse(info.detailed_orders),
      requirements.orElse(info.requirements),
      char_id.orElse(info.char_id),
      name.orElse(info.name)
    )
  }
}

final case class SquadPositionEntry(index : Int, info : Option[SquadPositionDetail2])

final case class SquadDetail(unk1 : Option[Int],
                             unk2 : Option[Int],
                             leader_char_id : Option[Long],
                             unk3 : Option[Long],
                             leader_name : Option[String],
                             task : Option[String],
                             zone_id : Option[PlanetSideZoneID],
                             unk7 : Option[Int],
                             member_info : Option[List[SquadPositionEntry]]) {
  def And(info : SquadDetail) : SquadDetail = {
    SquadDetail(
      unk1.orElse(info.unk1),
      unk2.orElse(info.unk2),
      leader_char_id.orElse(info.leader_char_id),
      unk3.orElse(info.unk3),
      leader_name.orElse(info.leader_name),
      task.orElse(info.task),
      zone_id.orElse(info.zone_id),
      unk7.orElse(info.unk7),
      (member_info, info.member_info) match {
        case (Some(info1), Some(info2)) => Some(info1 ++ info2)
        case (Some(info1), _) => Some(info1)
        case (None, Some(info2)) => Some(info2)
        case _ => None
      }
    )
  }
}

final case class SquadDetailDefinitionUpdateMessage(guid : PlanetSideGUID,
                                                    detail : SquadDetail)
  extends PlanetSideGamePacket {
  type Packet = SquadDetailDefinitionUpdateMessage
  def opcode = GamePacketOpcode.SquadDetailDefinitionUpdateMessage
  def encode = SquadDetailDefinitionUpdateMessage.encode(this)
}

object SquadPositionDetail2 {
  final val Blank : SquadPositionDetail2 = SquadPositionDetail2()
  final val Closed : SquadPositionDetail2 = SquadPositionDetail2(is_closed = Some(true), None, None, None, None, None)
  final val Open : SquadPositionDetail2 = SquadPositionDetail2(is_closed = Some(false), None, None, None, None, None)

  def apply() : SquadPositionDetail2 = SquadPositionDetail2(None, None, None, None, None, None)

  def apply(role : String, detailed_orders : Option[String]) : SquadPositionDetail2 = SquadPositionDetail2(None, Some(role), detailed_orders, None, None, None)

  def apply(role : Option[String], detailed_orders : String) : SquadPositionDetail2 = SquadPositionDetail2(None, role, Some(detailed_orders), None, None, None)

  def apply(role : String, detailed_orders : String) : SquadPositionDetail2 = SquadPositionDetail2(None, Some(role), Some(detailed_orders), None, None, None)

  def apply(requirements : Set[CertificationType.Value]) : SquadPositionDetail2 = SquadPositionDetail2(None, None, None, Some(requirements), None, None)

  def apply(role : String, detailed_orders : String, requirements : Set[CertificationType.Value]) : SquadPositionDetail2 = SquadPositionDetail2(None, Some(role), Some(detailed_orders), Some(requirements), None, None)

  def apply(char_id : Long) : SquadPositionDetail2 = SquadPositionDetail2(None, None, None, None, Some(char_id), None)

  def apply(name : String) : SquadPositionDetail2 = SquadPositionDetail2(None, None, None, None, None, Some(name))

  def apply(char_id : Long, name : String) : SquadPositionDetail2 = SquadPositionDetail2(None, None, None, None, Some(char_id), Some(name))

  def apply(role : String, detailed_orders : String, requirements : Set[CertificationType.Value], char_id : Long, name : String) : SquadPositionDetail2 = SquadPositionDetail2(is_closed = Some(false), Some(role), Some(detailed_orders), Some(requirements), Some(char_id), Some(name))
}

object SquadPositionEntry {
  import SquadDetailDefinitionUpdateMessage.paddedStringMetaCodec

  def apply(index : Int, detail : SquadPositionDetail2) : SquadPositionEntry = SquadPositionEntry(index, Some(detail))

  private val isClosedCodec : Codec[SquadPositionDetail2] = bool.exmap[SquadPositionDetail2] (
    state => Attempt.successful(SquadPositionDetail2(Some(state), None, None, None, None, None)),
    {
      case SquadPositionDetail2(Some(state), _, _, _, _, _) =>
        Attempt.successful(state)
      case _ =>
        Attempt.failure(Err("failed to encode squad position data for availability"))
    }
  )

  private def roleCodec(bitsOverByte : StreamLengthToken) : Codec[SquadPositionDetail2] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadPositionDetail2] (
    role => Attempt.successful(SquadPositionDetail2(role, None)),
    {
      case SquadPositionDetail2(_, Some(role), _, _, _, _) =>
        Attempt.successful(role)
      case _ =>
        Attempt.failure(Err("failed to encode squad position data for role"))
    }
  )

  private def ordersCodec(bitsOverByte : StreamLengthToken) : Codec[SquadPositionDetail2] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadPositionDetail2] (
    orders => Attempt.successful(SquadPositionDetail2(None, orders)),
    {
      case SquadPositionDetail2(_, _, Some(orders), _, _, _) =>
        Attempt.successful(orders)
      case _ =>
        Attempt.failure(Err("failed to encode squad position data for detailed orders"))
    }
  )

  private val requirementsCodec : Codec[SquadPositionDetail2] = ulongL(46).exmap[SquadPositionDetail2] (
    requirements => Attempt.successful(SquadPositionDetail2(CertificationType.fromEncodedLong(requirements))),
    {
      case SquadPositionDetail2(_, _, _, Some(requirements), _, _) =>
        Attempt.successful(CertificationType.toEncodedLong(requirements))
      case _ =>
        Attempt.failure(Err("failed to encode squad position data for certification requirements"))
    }
  )

  private val charIdCodec : Codec[SquadPositionDetail2] = uint32L.exmap[SquadPositionDetail2] (
    char_id => Attempt.successful(SquadPositionDetail2(char_id)),
    {
      case SquadPositionDetail2(_, _, _, _, Some(char_id), _) =>
        Attempt.successful(char_id)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for member id"))
    }
  )

  private def nameCodec(bitsOverByte : StreamLengthToken) : Codec[SquadPositionDetail2] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadPositionDetail2] (
    name => Attempt.successful(SquadPositionDetail2(name)),
    {
      case SquadPositionDetail2(_, _, _, _, _, Some(orders)) =>
        Attempt.successful(orders)
      case _ =>
        Attempt.failure(Err("failed to encode squad position data for member name"))
    }
  )

  /**
    * `Codec` for failing to determine a valid `Codec` based on the entry data.
    * This `Codec` is an invalid codec that does not read any bit data.
    * The `conditional` will always return `None` because
    * its determining conditional statement is explicitly `false`
    * and all cases involving explicit failure.
    */
  private def failureCodec(code : Int) : Codec[SquadPositionDetail2] = conditional(included = false, bool).exmap[SquadPositionDetail2] (
    _ => Attempt.failure(Err(s"decoding with unhandled codec - $code")),
    _ => Attempt.failure(Err(s"encoding with unhandled codec - $code"))
  )

  private final case class LinkedSquadPositionInfo(code : Int, info : SquadPositionDetail2, next : Option[LinkedSquadPositionInfo])

  /**
    * Concatenate a `SquadInfo` object chain into a single `SquadInfo` object.
    * Recursively visits every link in a `SquadInfo` object chain.
    * @param info the current link in the chain
    * @param squadInfo the persistent `SquadInfo` concatenation object;
    *                  defaults to `SquadInfo.Blank`
    * @return the concatenated `SquadInfo` object
    */
  @tailrec
  private def unlinkSquadPositionInfo(info : Option[LinkedSquadPositionInfo], squadInfo : SquadPositionDetail2 = SquadPositionDetail2.Blank) : SquadPositionDetail2 = {
    info match {
      case None =>
        squadInfo
      case Some(sqInfo) =>
        unlinkSquadPositionInfo(sqInfo.next, squadInfo And sqInfo.info)
    }
  }

  /**
    * Decompose a single `SquadInfo` object into a `SquadInfo` object chain of the original's fields.
    * The fields as a linked list are explicitly organized "leader", "task", "zone_id", "size", "capacity,"
    * or as "(leader, (task, (zone_id, (size, (capacity, None)))))" when fully populated and composed.
    * @param info a `SquadInfo` object that has all relevant fields populated
    * @return a linked list of `SquadInfo` objects, each with a single field from the input `SquadInfo` object
    */
  private def linkSquadPositionInfo(info : SquadPositionDetail2) : LinkedSquadPositionInfo = {
    //import scala.collection.immutable.::
    Seq(
      (5, SquadPositionDetail2(None, None, None, info.requirements, None, None)),
      (4, SquadPositionDetail2(None, None, None, None, None, info.name)),
      (3, SquadPositionDetail2(None, None, None, None, info.char_id, None)),
      (2, SquadPositionDetail2(None, None, info.detailed_orders, None, None, None)),
      (1, SquadPositionDetail2(None, info.role, None, None, None, None)),
      (0, SquadPositionDetail2(info.is_closed, None, None, None, None, None))
    ) //in reverse order so that the linked list is in the correct order
      .filterNot { case (_, sqInfo) => sqInfo == SquadPositionDetail2.Blank}
    match {
      case Nil =>
        throw new Exception("no linked list squad position fields encountered where at least one was expected") //bad end
      case x :: Nil =>
        val (code, squadInfo) = x
        LinkedSquadPositionInfo(code, squadInfo, None)
      case x :: xs =>
        val (code, squadInfo) = x
        linkSquadPositionInfo(xs, LinkedSquadPositionInfo(code, squadInfo, None))
    }
  }

  /**
    * Decompose a single `SquadInfo` object into a `SquadInfo` object chain of the original's fields.
    * The fields as a linked list are explicitly organized "leader", "task", "zone_id", "size", "capacity,"
    * or as "(leader, (task, (zone_id, (size, (capacity, None)))))" when fully populated and composed.
    * @param infoList a series of paired field codes and `SquadInfo` objects with data in the indicated fields
    * @return a linked list of `SquadInfo` objects, each with a single field from the input `SquadInfo` object
    */
  @tailrec
  private def linkSquadPositionInfo(infoList : Seq[(Int, SquadPositionDetail2)], linkedInfo : LinkedSquadPositionInfo) : LinkedSquadPositionInfo = {
    if(infoList.isEmpty) {
      linkedInfo
    }
    else {
      val (code, data) = infoList.head
      linkSquadPositionInfo(infoList.tail, LinkedSquadPositionInfo(code, data, Some(linkedInfo)))
    }
  }

  private def listing_codec(size : Int, bitsOverByte : StreamLengthToken) : Codec[LinkedSquadPositionInfo] = {
    import shapeless.::
    (
      uint4 >>:~ { code =>
        selectCodecAction(code,  bitsOverByte.Add(4)) >>:~ { _ =>
          modifyCodecPadValue(code, bitsOverByte)
          conditional(size - 1 > 0, listing_codec(size - 1, bitsOverByte)).hlist
        }
      }
      ).xmap[LinkedSquadPositionInfo] (
      {
        case code :: entry :: next :: HNil =>
          LinkedSquadPositionInfo(code, entry, next)
      },
      {
        case LinkedSquadPositionInfo(code, entry, next) =>
          code :: entry :: next :: HNil
      }
    )
  }

  private def selectCodecAction(code : Int, bitsOverByte : StreamLengthToken) : Codec[SquadPositionDetail2] = {
    code match {
      case 0 => isClosedCodec
      case 1 => roleCodec(bitsOverByte)
      case 2 => ordersCodec(bitsOverByte)
      case 3 => charIdCodec
      case 4 => nameCodec(bitsOverByte)
      case 5 => requirementsCodec
      case _ => failureCodec(code)
    }
  }

  private def modifyCodecPadValue(code : Int, bitsOverByte : StreamLengthToken) : StreamLengthToken = {
    code match {
      case 0 => bitsOverByte.Add(1) //additional 1u
      case 1 => bitsOverByte.Length = 0 //byte-aligned string; padding zero'd
      case 2 => bitsOverByte.Length = 0 //byte-aligned string; padding zero'd
      case 3 => bitsOverByte //32u = no added padding
      case 4 => bitsOverByte.Length = 0 //byte-aligned string; padding zero'd
      case 5 => bitsOverByte.Add(6) //46u = 5*8u + 6u = additional 6u
      case _ => bitsOverByte.Length = Int.MinValue //wildly incorrect
    }
  }

  private def squad_member_details_codec(bitsOverByte : StreamLengthToken) : Codec[LinkedSquadPositionInfo] = {
    import shapeless.::
    (
      uint8 >>:~ { size =>
        listing_codec(size, bitsOverByte).hlist
      }
      ).xmap[LinkedSquadPositionInfo] (
      {
        case _ :: info :: HNil =>
          info
      },
      info => {
        var i = 1
        var dinfo = info
        while(dinfo.next.nonEmpty) {
          i += 1
          dinfo = dinfo.next.get
        }
        i :: info :: HNil
      }
    )
  }

  def codec(bitsOverByte : StreamLengthToken) : Codec[SquadPositionEntry] = {
    import shapeless.::
    /*
     import net.psforever.newcodecs.newcodecs
//        newcodecs.binary_choice[Option[LinkedSquadPositionInfo] :: HNil](
//          index < 255,
//          (bool :: squad_member_details_codec(bitsOverByte.Add(1))).exmap[Option[LinkedSquadPositionInfo] :: HNil] (
//            {
//              case _ :: info :: HNil => Attempt.Successful(Some(info) :: HNil)
//            },
//            {
//              case Some(info) :: HNil => Attempt.Successful(true :: info :: HNil)
//              case None :: HNil => Attempt.Failure(Err(s"this data for this position should not be None - $index < 255"))
//            }
//          ),
//          bits.xmap[Option[LinkedSquadPositionInfo] :: HNil] (
//            _ => None :: HNil,
//            {
//              case _ :: HNil => BitVector.empty
//            }
//          )
//        )
    */
    (
      ("index" | uint8) >>:~ { index =>
        conditional(index < 255, bool :: squad_member_details_codec(bitsOverByte.Add(1))) ::
        conditional(index == 255, bits)
      }
      ).xmap[SquadPositionEntry] (
      {
        case 255 :: _ :: _ :: HNil =>
          SquadPositionEntry(255, None)
        case ndx :: Some(_ :: info :: HNil) :: _ :: HNil =>
          SquadPositionEntry(ndx, Some(unlinkSquadPositionInfo(Some(info))))
      },
      {
        case SquadPositionEntry(255, _) =>
          255 :: None :: None :: HNil
        case SquadPositionEntry(ndx, Some(info)) =>
          ndx :: Some(true :: linkSquadPositionInfo(info) :: HNil) :: None :: HNil
      }
    )
  }
}

object SquadDetail {
  final val Blank = SquadDetail(None, None, None, None, None, None, None, None, None)

  def apply(leader_char_id : Long) : SquadDetail = SquadDetail(None, None, Some(leader_char_id), None, None, None, None, None, None)

  def apply(leader_char_id : Long, leader_name : String) : SquadDetail = SquadDetail(None, None, Some(leader_char_id), None, Some(leader_name), None, None, None, None)

  def apply(leader_name : String, task : Option[String]) : SquadDetail = SquadDetail(None, None, None, None, Some(leader_name), task, None, None, None)

  def apply(leader_name : Option[String], task : String) : SquadDetail = SquadDetail(None, None, None, None, leader_name, Some(task), None, None, None)

  def apply(zone_id : PlanetSideZoneID) : SquadDetail = SquadDetail(None, None, None, None, None, None, Some(zone_id), None, None)

  def apply(member_list : List[SquadPositionEntry]) : SquadDetail = SquadDetail(None, None, None, None, None, None, None, None, Some(member_list))

  def apply(unk1 : Int, unk2 : Int, leader_char_id : Long, unk3 : Long, leader_name : String, task : String, zone_id : PlanetSideZoneID, unk7 : Int, member_info : List[SquadPositionEntry]) : SquadDetail = {
    SquadDetail(Some(unk1), Some(unk2), Some(leader_char_id), Some(unk3), Some(leader_name), Some(task), Some(zone_id), Some(unk7), Some(member_info))
  }
}

object SquadDetailDefinitionUpdateMessage extends Marshallable[SquadDetailDefinitionUpdateMessage] {
  final val DefaultRequirements : Set[CertificationType.Value] = Set(
    CertificationType.StandardAssault,
    CertificationType.StandardExoSuit,
    CertificationType.AgileExoSuit
  )

  final val Init = SquadDetailDefinitionUpdateMessage(
    PlanetSideGUID(0),
    SquadDetail(
      0,
      0,
      0L,
      0L,
      "",
      "",
      PlanetSideZoneID(0),
      0,
      (0 to 9).map { i => SquadPositionEntry(i, SquadPositionDetail2.Blank)} toList
    )
  )


  /*
  DECOY CONSTRUCTORS
   */
  def apply(guid : PlanetSideGUID,
             unk1 : Int,
             leader_char_id : Long,
             unk2 : BitVector,
             leader_name : String,
             task : String,
             zone_id : PlanetSideZoneID,
             member_info : List[SquadPositionDetail]) : SquadDetailDefinitionUpdateMessage = {
    SquadDetailDefinitionUpdateMessage(PlanetSideGUID(0), SquadDetail.Blank)
  }
  def apply(guid : PlanetSideGUID, char_id : Long, leader_name : String, task : String, zone_id : PlanetSideZoneID, member_info : List[SquadPositionDetail]) : SquadDetailDefinitionUpdateMessage = {
    SquadDetailDefinitionUpdateMessage(PlanetSideGUID(0), SquadDetail.Blank)
  }

  /**
    * Produces a `Codec` function for byte-aligned, padded Pascal strings encoded through common manipulations.
    * @see `PacketHelpers.encodedWideStringAligned`
    * @param bitsOverByte the number of bits past the previous byte-aligned index;
    *             should be a 0-7 number that gets converted to a 1-7 string padding number
    * @return the encoded string `Codec`
    */
  def paddedStringMetaCodec(bitsOverByte : Int) : Codec[String] = PacketHelpers.encodedWideStringAligned({
    val mod8 = bitsOverByte % 8
    if(mod8 == 0) {
      0
    }
    else {
      8 - mod8
    }
  })

  private def memberCodec(pad : Int) : Codec[SquadPositionDetail2] = {
    import shapeless.::
    (
      uint8 :: //required value = 6
        ("is_closed" | bool) :: //if all positions are closed, the squad detail menu display no positions at all
        ("role" | PacketHelpers.encodedWideStringAligned(pad)) ::
        ("detailed_orders" | PacketHelpers.encodedWideString) ::
        ("char_id" | uint32L) ::
        ("name" | PacketHelpers.encodedWideString) ::
        ("requirements" | ulongL(46))
      ).exmap[SquadPositionDetail2] (
      {
        case 6 :: closed :: role :: orders :: char_id :: name :: requirements :: HNil =>
          Attempt.Successful(
            SquadPositionDetail2(Some(closed), Some(role), Some(orders), Some(DefaultRequirements ++ CertificationType.fromEncodedLong(requirements)), Some(char_id), Some(name))
          )
        case data =>
          Attempt.Failure(Err(s"can not decode a SquadDetailDefinitionUpdate member's data - $data"))
      },
      {
        case SquadPositionDetail2(Some(closed), Some(role), Some(orders), Some(requirements), Some(char_id), Some(name)) =>
          Attempt.Successful(6 :: closed :: role :: orders :: char_id :: name :: CertificationType.toEncodedLong(DefaultRequirements ++ requirements) :: HNil)
      }
    )
  }

  private val first_member_codec : Codec[SquadPositionDetail2] = memberCodec(pad = 7)

  private val member_codec : Codec[SquadPositionDetail2] = memberCodec(pad = 0)

  private case class LinkedMemberList(member : SquadPositionDetail2, next : Option[LinkedMemberList])

  private def subsequent_member_codec : Codec[LinkedMemberList] = {
    import shapeless.::
    (
      //disruptive coupling action (e.g., flatPrepend) is necessary to allow for recursive Codec
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
  private def unlinkMemberList(list : LinkedMemberList, out : List[SquadPositionDetail2] = Nil) : List[SquadPositionDetail2] = {
    list.next match {
      case None =>
        out :+ list.member
      case Some(next) =>
        unlinkMemberList(next, out :+ list.member)
    }
  }

  private def linkMemberList(list : List[SquadPositionDetail2]) : LinkedMemberList = {
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
  private def linkMemberList(list : List[SquadPositionDetail2], out : LinkedMemberList) : LinkedMemberList = {
    list match {
      case Nil =>
        out
      case x :: Nil =>
        LinkedMemberList(x, Some(out))
      case x :: xs =>
        linkMemberList(xs, LinkedMemberList(x, Some(out)))
    }
  }

  val full_squad_detail_codec : Codec[SquadDetail] = {
    import shapeless.::
    (
      ("unk1" | uint8) ::
        ("unk2" | uint24) :: //unknown, but can be 0'd
        ("leader_char_id" | uint32L) ::
        ("unk3" | uint32L) :: //variable fields, but can be 0'd
        ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
        ("task" | PacketHelpers.encodedWideString) ::
        ("zone_id" | PlanetSideZoneID.codec) ::
        ("unk7" | uint(23)) :: //during full squad mode, constant = 4983296
        optional(bool, "member_info" | initial_member_codec)
      ).exmap[SquadDetail] (
      {
        case u1 :: u2 :: char_id :: u3 :: leader :: task :: zone :: unk7 :: Some(member_list) :: HNil =>
          Attempt.Successful(
            SquadDetail(Some(u1), Some(u2), Some(char_id), Some(u3), Some(leader), Some(task), Some(zone), Some(unk7),
              Some(unlinkMemberList(member_list).zipWithIndex.map { case (entry, index) => SquadPositionEntry(index, Some(entry)) })
            )
          )
        case data =>
          Attempt.failure(Err(s"can not get squad detail definition from data $data"))
      },
      {
        case SquadDetail(Some(u1), Some(u2), Some(char_id), Some(u3), Some(leader), Some(task), Some(zone), Some(unk7), Some(member_list)) =>
          Attempt.Successful(
            math.max(u1, 1) :: u2 :: char_id :: u3 :: leader :: task :: zone :: unk7 ::
              Some(linkMemberList(member_list.collect { case SquadPositionEntry(_, Some(entry)) => entry }.reverse)) ::
              HNil
          )
      }
    )
  }

  private val field1Codec : Codec[SquadDetail] = uint16L.exmap[SquadDetail] (
    unk1 => Attempt.successful(SquadDetail(Some(unk1), None, None, None, None, None, None, None, None)),
    {
      case SquadDetail(Some(unk1), _, _, _,  _, _, _, _, _) =>
        Attempt.successful(unk1)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for unknown field #1"))
    }
  )

  private val leaderCharIdCodec : Codec[SquadDetail] = uint32L.exmap[SquadDetail] (
    char_id => Attempt.successful(SquadDetail(char_id)),
    {
      case SquadDetail(_, _, Some(char_id), _,  _, _, _, _, _) =>
        Attempt.successful(char_id)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for leader id"))
    }
  )

  private val field3Codec : Codec[SquadDetail] = uint32L.exmap[SquadDetail] (
    unk3 => Attempt.successful(SquadDetail(None, None, None, Some(unk3), None, None, None, None, None)),
    {
      case SquadDetail(_, _, _, Some(unk3),  _, _, _, _, _) =>
        Attempt.successful(unk3)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for unknown field #3"))
    }
  )

  private def leaderNameCodec(bitsOverByte : StreamLengthToken) : Codec[SquadDetail] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadDetail] (
    name => Attempt.successful(SquadDetail(name, None)),
    {
      case SquadDetail(_, _, _, _, Some(name), _, _, _, _) =>
        Attempt.successful(name)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for leader name"))
    }
  )

  private def taskCodec(bitsOverByte : StreamLengthToken) : Codec[SquadDetail] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadDetail] (
    task => Attempt.successful(SquadDetail(None, task)),
    {
      case SquadDetail(_, _, _, _, _, Some(task), _, _, _) =>
        Attempt.successful(task)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for task"))
    }
  )

  private val zoneCodec : Codec[SquadDetail] = PlanetSideZoneID.codec.exmap[SquadDetail] (
    zone_id => Attempt.successful(SquadDetail(zone_id)),
    {
      case SquadDetail(_, _, _, _, _, _, Some(zone_id), _, _) =>
        Attempt.successful(zone_id)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for zone id"))
    }
  )

  private val field7Codec : Codec[SquadDetail] = {
    uint4.exmap[SquadDetail] (
      unk7 => Attempt.successful(SquadDetail(None, None, None, None, None, None, None, Some(unk7), None)),
      {
        case SquadDetail(_, _, _,  _, _, _, _, Some(unk7), _) =>
          Attempt.successful(unk7)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for unknown field #7"))
      }
    )
  }

  private def membersCodec(bitsOverByte : StreamLengthToken) : Codec[SquadDetail] = {
    import shapeless.::
    bitsOverByte.Add(4)
    (
      uint4 :: //constant = 12
        vector(SquadPositionEntry.codec(bitsOverByte))
      ).exmap[SquadDetail] (
      {
        case 12 :: member_list :: HNil =>
          Attempt.successful(SquadDetail(member_list.toList))
      },
      {
        case SquadDetail(_, _, _, _, _, _, _, _, Some(member_list)) =>
          Attempt.successful(12 :: member_list.toVector :: HNil)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for members"))
      }
    )
  }

  private val failureCodec : Codec[SquadDetail] = conditional(included = false, bool).exmap[SquadDetail] (
    _ => Attempt.failure(Err("decoding with unhandled codec")),
    _ => Attempt.failure(Err("encoding with unhandled codec"))
  )

  private def selectCodecAction(code : Int, bitsOverByte : StreamLengthToken) : Codec[SquadDetail] = {
    code match {
      case 1 => field1Codec
      case 2 => leaderCharIdCodec
      case 3 => field3Codec
      case 4 => leaderNameCodec(bitsOverByte)
      case 5 => taskCodec(bitsOverByte)
      case 6 => zoneCodec
      case 7 => field7Codec
      case 8 => membersCodec(bitsOverByte)
      case _ => failureCodec
    }
  }

  private def modifyCodecPadValue(code : Int, bitsOverByte : StreamLengthToken) : StreamLengthToken = {
    code match {
      case 1 => bitsOverByte //16u = no added padding
      case 2 => bitsOverByte //32u = no added padding
      case 3 => bitsOverByte //32u = no added padding
      case 4 => bitsOverByte.Length = 0 //byte-aligned string; padding zero'd
      case 5 => bitsOverByte.Length = 0 //byte-aligned string; padding zero'd
      case 6 => bitsOverByte //32u = no added padding
      case 7 => bitsOverByte.Add(4) //additional 4u
      case 8 => bitsOverByte.Length = 0 //end of stream
      case _ => bitsOverByte.Length = Int.MinValue //wildly incorrect
    }
  }

  private final case class LinkedSquadInfo(code : Int, info : SquadDetail, next : Option[LinkedSquadInfo])

  private def unlinkSquadInfo(info : LinkedSquadInfo) : SquadDetail = unlinkSquadInfo(Some(info))

  @tailrec
  private def unlinkSquadInfo(info : Option[LinkedSquadInfo], squadInfo : SquadDetail = SquadDetail.Blank) : SquadDetail = {
    info match {
      case None =>
        squadInfo
      case Some(sqInfo) =>
        unlinkSquadInfo(sqInfo.next, squadInfo And sqInfo.info)
    }
  }

  private def linkSquadInfo(info : SquadDetail) : LinkedSquadInfo = {
    //import scala.collection.immutable.::
    Seq(
      (8, SquadDetail(None, None, None, None, None, None, None, None, info.member_info)),
      (7, SquadDetail(None, None, None, None, None, None, None, info.unk7, None)),
      (6, SquadDetail(None, None, None, None, None, None, info.zone_id, None, None)),
      (5, SquadDetail(None, None, None, None, None, info.task, None, None, None)),
      (4, SquadDetail(None, None, None, None, info.leader_name, None, None, None, None)),
      (3, SquadDetail(None, None, None, info.unk3, None, None, None, None, None)),
      (2, SquadDetail(None, None, info.leader_char_id, None, None, None, None, None, None)),
      (1, SquadDetail(info.unk1, None, None, None, None, None, None, None, None))
    ) //in reverse order so that the linked list is in the correct order
      .filterNot { case (_, sqInfo) => sqInfo == SquadDetail.Blank}
    match {
      case Nil =>
        throw new Exception("no linked list squad fields encountered where at least one was expected") //bad end
      case x :: Nil =>
        val (code, squadInfo) = x
        LinkedSquadInfo(code, squadInfo, None)
      case x :: xs =>
        val (code, squadInfo) = x
        linkSquadInfo(xs, LinkedSquadInfo(code, squadInfo, None))
    }
  }

  @tailrec
  private def linkSquadInfo(infoList : Seq[(Int, SquadDetail)], linkedInfo : LinkedSquadInfo) : LinkedSquadInfo = {
    if(infoList.isEmpty) {
      linkedInfo
    }
    else {
      val (code, data) = infoList.head
      linkSquadInfo(infoList.tail, LinkedSquadInfo(code, data, Some(linkedInfo)))
    }
  }

  private def linked_squad_detail_codec(size : Int, bitsOverByte : StreamLengthToken) : Codec[LinkedSquadInfo] = {
    import shapeless.::
    (
      uint4 >>:~ { code =>
        selectCodecAction(code, bitsOverByte.Add(4)) ::
          conditional(size - 1 > 0, linked_squad_detail_codec(size - 1, modifyCodecPadValue(code, bitsOverByte)))
      }
      ).exmap[LinkedSquadInfo] (
      {
        case action :: detail :: next :: HNil =>
          Attempt.Successful(LinkedSquadInfo(action, detail, next))
      },
      {
        case LinkedSquadInfo(action, detail, next) =>
          Attempt.Successful(action :: detail :: next :: HNil)
      }
    )
  }

  def squadDetailSelectCodec(size : Int) : Codec[SquadDetail] = {
    if(size == 9) {
      full_squad_detail_codec
    }
    else {
      linked_squad_detail_codec(size, new StreamLengthToken(1)).xmap[SquadDetail] (
        linkedDetail => unlinkSquadInfo(linkedDetail),
        unlinkedDetail => linkSquadInfo(unlinkedDetail)
      )
    }
  }

  implicit val codec : Codec[SquadDetailDefinitionUpdateMessage] = {
    import shapeless.::
    (
      ("guid" | PlanetSideGUID.codec) ::
        bool ::
        (uint8 >>:~ { size =>
          squadDetailSelectCodec(size).hlist
        })
      ).exmap[SquadDetailDefinitionUpdateMessage] (
      {
        case guid :: _ :: _ :: info :: HNil =>
          Attempt.Successful(SquadDetailDefinitionUpdateMessage(guid, info))
      },
      {
        case SquadDetailDefinitionUpdateMessage(guid, info) =>
          val occupiedSquadFieldCount = List(info.unk1, info.unk2, info.leader_char_id, info.unk3, info.leader_name, info.task, info.zone_id, info.unk7, info.member_info)
            .count(_.nonEmpty)
          if(occupiedSquadFieldCount < 9) {
            //itemized detail definition protocol
            Attempt.Successful(guid :: true :: occupiedSquadFieldCount :: info :: HNil)
          }
          else {
            info.member_info match {
              case Some(list) =>
                if(list.size == 10 &&
                list
                  .collect { case position if position.info.nonEmpty =>
                    val info = position.info.get
                    List(info.is_closed, info.role, info.detailed_orders, info.requirements, info.char_id, info.name)
                  }
                  .flatten
                  .count(_.isEmpty) == 0) {
                  //full squad detail definition protocol
                  Attempt.Successful(guid :: true :: 9 :: info :: HNil)
                }
                else {
                  //unhandled state
                  Attempt.Failure(Err("can not split encoding patterns - all squad fields are defined but not all squad member fields are defined"))
                }
              case None =>
                //impossible?
                Attempt.Failure(Err("the members field can not be empty; the existence of this field was already proven"))
            }
          }
      }
    )
  }
}
