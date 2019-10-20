// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.CertificationType
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.HNil

import scala.annotation.tailrec

/**
  * A container that should be used to keep track of the current length of a stream of bits.
  * @param init the starting pad value;
  *             defaults to 0
  */
class StreamLengthToken(init : Int = 0) {
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

/**
  * Information regarding a squad's position as a series of common fields.
  * When parsed in an itemized way, only the important fields are represented.
  * When parsed in a continuous manner, all of the fields are populated.
  * All fields are optional for that reason.
  * @param is_closed availability, whether the position can be occupied by a player;
  *                  an unavailable position is referenced as "Closed" and no other position detail is displayed;
  *                  an available unoccupied position is "Available"
  * @param role the title of the position
  * @param detailed_orders the suggested responsibilities of the position
  * @param requirements the actual responsibilities of the position
  * @param char_id the unique character identification number for the player that is occupying this position
  * @param name the name of the player who is occupying this position
  */
final case class SquadPositionDetail(is_closed : Option[Boolean],
                                     role : Option[String],
                                     detailed_orders : Option[String],
                                     requirements : Option[Set[CertificationType.Value]],
                                     char_id : Option[Long],
                                     name : Option[String]) {
  /**
    * Combine two `SquadPositionDetail` objects, with priority given to `this` one.
    * Most fields that are not empty are assigned.
    * Even if the current object reports the squad position being open - `is_closed = Some(false)` -
    * just one instance of the squad position being closed overwrites all future updates.
    * @param info the object being combined
    * @return the combined `SquadDetail` object
    */
  def And(info : SquadPositionDetail) : SquadPositionDetail = {
    SquadPositionDetail(
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

  //methods intended to combine the fields of itself and another object
  def Open : SquadPositionDetail =
    this And SquadPositionDetail(Some(false), None, None, None, None, None)
  def Close : SquadPositionDetail =
    this And SquadPositionDetail(Some(true), None, None, None, None, None)
  def Role(role : String) : SquadPositionDetail =
    this And SquadPositionDetail(None, Some(role), None, None, None, None)
  def DetailedOrders(orders : String) : SquadPositionDetail =
    this And SquadPositionDetail(None, None, Some(orders), None, None, None)
  def Requirements(req : Set[CertificationType.Value]) : SquadPositionDetail =
    this And SquadPositionDetail(None, None, None, Some(req), None, None)
  def CharId(char_id : Long) : SquadPositionDetail =
    this And SquadPositionDetail(None, None, None, None, Some(char_id), None)
  def Name(name : String) : SquadPositionDetail =
    this And SquadPositionDetail(None, None, None, None, None, Some(name))
  def Player(char_id : Long, name : String) : SquadPositionDetail =
    this And SquadPositionDetail(None, None, None, None, Some(char_id), Some(name))

  /**
    * Complete the object by providing placeholder values for all fields.
    * @return a `SquadPositionDetail` object with all of its field populated
    */
  def Complete : SquadPositionDetail = SquadPositionDetail(
    is_closed.orElse(Some(false)),
    role.orElse(Some("")),
    detailed_orders.orElse(Some("")),
    requirements.orElse(Some(Set.empty)),
    char_id.orElse(Some(0L)),
    name.orElse(Some(""))
  )
}

/**
  * A container for squad position field data
  * associating what would be the ordinal position of that field data in full squad data.
  * @param index the index for this squad position;
  *              expected to be a number 0-9 or 255;
  *              when 255, this indicated the end of enumerated squad position data and the data for that position is absent
  * @param info the squad position field data
  */
final case class SquadPositionEntry(index : Int, info : Option[SquadPositionDetail]) {
  assert((index > -1 && index < 10) || index == 255, "index value is out of range 0=>n<=9 or n=255")
  assert(if(index == 255) { info.isEmpty } else { true }, "index=255 indicates end of stream exclusively and field data should be blank")
}

/**
  * Information regarding a squad's position as a series of common fields.
  * When parsed in an itemized way, only the important fields are represented.
  * When parsed in a continuous manner, all of the fields are populated.
  * All fields are optional for that reason.<br>
  * <br>
  * The squad leader does not necessarily have to be a person from the `member_info` list.
  * @param unk1 na;
  *             must be non-zero when parsed in a FullSquad pattern
  * @param unk2 na;
  *             not associated with any fields during itemized parsing
  * @param leader_char_id he unique character identification number for the squad leader
  * @param unk3 na
  * @param leader_name the name of the player who is the squad leader
  * @param task the suggested responsibilities or mission statement of the squad
  * @param zone_id the suggested area of engagement for this squad's activities;
  *                can also indicate the zone of the squad leader
  * @param unk7 na
  * @param member_info a list of squad position data
  */
final case class SquadDetail(unk1 : Option[Int],
                             unk2 : Option[Int],
                             leader_char_id : Option[Long],
                             unk3 : Option[Long],
                             leader_name : Option[String],
                             task : Option[String],
                             zone_id : Option[PlanetSideZoneID],
                             unk7 : Option[Int],
                             member_info : Option[List[SquadPositionEntry]]) {
  /**
    * Combine two `SquadDetail` objects, with priority given to `this` one.
    * Most fields that are not empty are assigned.
    * @param info the object being combined
    * @return the combined `SquadDetail` object
    */
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
      {
        (member_info, info.member_info) match {
          case (Some(info1), Some(info2)) =>
            //combine the first list with the elements of the second list whose indices not found in the first list
            val indices = info1.map { _.index }
            Some(info1 ++ (for {
              position <- info2
              if !indices.contains(position.index)
            } yield position).sortBy(_.index))
          case (Some(info1), None) =>
            Some(info1)
          case (None, _) =>
            info.member_info
        }
      }
    )
  }

  //methods intended to combine the fields of itself and another object
  def Field1(value : Int) : SquadDetail =
    this And SquadDetail(Some(value), None, None, None, None, None, None, None, None)
  def LeaderCharId(char_id : Long) : SquadDetail =
    this And SquadDetail(None, None, Some(char_id), None, None, None, None, None, None)
  def Field3(value : Long) : SquadDetail =
    this And SquadDetail(None, None, None, Some(value), None, None, None, None, None)
  def LeaderName(name : String) : SquadDetail =
    this And SquadDetail(None, None, None, None, Some(name), None, None, None, None)
  def Leader(char_id : Long, name : String) : SquadDetail =
    this And SquadDetail(None, None, Some(char_id), None, Some(name), None, None, None, None)
  def Task(task : String) : SquadDetail =
    this And SquadDetail(None, None, None, None, None, Some(task), None, None, None)
  def ZoneId(zone : PlanetSideZoneID) : SquadDetail =
    this And SquadDetail(None, None, None, None, None, None, Some(zone), None, None)
  def Field7(value : Int) : SquadDetail =
    this And SquadDetail(None, None, None, None, None, None, None, Some(value), None)
  def Members(list : List[SquadPositionEntry]) : SquadDetail =
    this And SquadDetail(None, None, None, None, None, None, None, None, Some(list))

  /**
    * Complete the object by providing placeholder values for all fields.
    * The `member_info` field requires additional allocation.
    * @return a `SquadDetail` object with all of its field populated
    */
  def Complete : SquadDetail = SquadDetail(
    unk1.orElse(Some(1)),
    unk2.orElse(Some(0)),
    leader_char_id.orElse(Some(0L)),
    unk3.orElse(Some(0L)),
    leader_name.orElse(Some("")),
    task.orElse(Some("")),
    zone_id.orElse(Some(PlanetSideZoneID(0))),
    unk7.orElse(Some(4983296)), //FullSquad value
    {
      val complete = SquadPositionDetail().Complete
      Some(member_info match {
        case Some(info) =>
          //create one list that ensures all existing positions are "complete" then add a list of the missing indices
          val (indices, fields) = info.collect {
            case SquadPositionEntry(a, Some(b)) => (a, SquadPositionEntry(a, b.Complete))
            case out @ SquadPositionEntry(a, None) => (a, out)
          }.unzip
          ((0 to 9).toSet.diff(indices.toSet).map { SquadPositionEntry(_, complete) } ++ fields).toList.sortBy(_.index)
        case None =>
          //original list
          (0 to 9).map { i => SquadPositionEntry(i, complete) }.toList
      })
    }
  )
}

/**
  * A compilation of the fields that communicate detailed information about squad structure and composition
  * as a complement to the packet `ReplicationStreamMessage` and the packet `SquadDefinitionActionMessage`.
  * The information communicated by the `SquadDefinitionActionMessage` packets allocates individual fields of the squad's structure
  * and the `ReplicationStreamMessage` packet reports very surface-level information about the squad to other players.
  * The `SquadDetailDefinitionUpdateMessage` packet serves as a realization of the field information reported by the former
  * and a fully fleshed-out explanation of the information presented by the latter.<br>
  * <br>
  * Squads are generally referenced by their own non-zero globally unique identifier that is valid server-wide.
  * A zero GUID squad is also accessible for information related to the local unpublished squad that exists on a specific client.
  * Only one published squad can have its information displayed at a time.
  * While imperfect squad information can be shown, two major formats for the data in this packet are common.
  * The first format lists all of the squad's fields and data and is used as an initialization of the squad locally.
  * This format is always used the first time information about the squad is communicated to the client.
  * The second format lists specific portions of the squad's fields and data and is used primarily for simple updating purposes.
  * @param guid the globally unique identifier of the squad
  * @param detail information regarding the squad
  */
final case class SquadDetailDefinitionUpdateMessage(guid : PlanetSideGUID,
                                                    detail : SquadDetail)
  extends PlanetSideGamePacket {
  type Packet = SquadDetailDefinitionUpdateMessage
  def opcode = GamePacketOpcode.SquadDetailDefinitionUpdateMessage
  def encode = SquadDetailDefinitionUpdateMessage.encode(this)
}

object SquadPositionDetail {
  /**
    * A featureless squad position.
    * References the default overloaded constructor.
    */
  final val Blank : SquadPositionDetail = SquadPositionDetail()
  /**
    * An unavailable squad position.
    */
  final val Closed : SquadPositionDetail = SquadPositionDetail(is_closed = Some(true), None, None, None, None, None)
  /**
    * An available squad position.
    */
  final val Open : SquadPositionDetail = SquadPositionDetail(is_closed = Some(false), None, None, None, None, None)

  /**
    * An overloaded constructor that produces a featureless squad position.
    * @return a `SquadPositionDetail` object
    */
  def apply() : SquadPositionDetail = SquadPositionDetail(None, None, None, None, None, None)
  /**
    * An overloaded constructor that produces a full squad position with a role, detailed orders, and certification requirements.
    * This basically defines an available squad position that is unoccupied.
    * @return a `SquadPositionDetail` object
    */
  def apply(role : String, detailed_orders : String, requirements : Set[CertificationType.Value], char_id : Long, name : String) : SquadPositionDetail = SquadPositionDetail(Some(false), Some(role), Some(detailed_orders), Some(requirements), Some(char_id), Some(name))

  object Fields {
    final val Closed = 0
    final val Role = 1
    final val Orders = 2
    final val CharId = 3
    final val Name = 4
    final val Requirements = 5
  }
}

object SquadPositionEntry {
  /**
    * An overloaded constructor.
    * @return a `SquadPositionEntry` object
    */
  def apply(index : Int, detail : SquadPositionDetail) : SquadPositionEntry = SquadPositionEntry(index, Some(detail))
}

object SquadDetail {
  /**
    * A featureless squad.
    * References the default overloaded constructor.
    */
  final val Blank = SquadDetail()

  /**
    * An overloaded constructor that produces a featureless squad.
    * @return a `SquadDetail` object
    */
  def apply() : SquadDetail = SquadDetail(None, None, None, None, None, None, None, None, None)
  /**
    * An overloaded constructor that produces a complete squad with all fields populated.
    * @return a `SquadDetail` object
    */
  def apply(unk1 : Int, unk2 : Int, leader_char_id : Long, unk3 : Long, leader_name : String, task : String, zone_id : PlanetSideZoneID, unk7 : Int, member_info : List[SquadPositionEntry]) : SquadDetail = {
    SquadDetail(Some(unk1), Some(unk2), Some(leader_char_id), Some(unk3), Some(leader_name), Some(task), Some(zone_id), Some(unk7), Some(member_info))
  }

  //individual field overloaded constructors
  def Field1(unk1 : Int) : SquadDetail =
    SquadDetail(Some(unk1), None, None, None, None, None, None, None, None)
  def LeaderCharId(char_id : Long) : SquadDetail =
    SquadDetail(None, None, Some(char_id), None, None, None, None, None, None)
  def Field3(char_id : Option[Long], unk3 : Long) : SquadDetail =
    SquadDetail(None, None, None, Some(unk3), None, None, None, None, None)
  def LeaderName(name : String) : SquadDetail =
    SquadDetail(None, None, None, None, Some(name), None, None, None, None)
  def Leader(char_id : Long, name : String) : SquadDetail =
    SquadDetail(None, None, Some(char_id), None, Some(name), None, None, None, None)
  def Task(task : String) : SquadDetail =
    SquadDetail(None, None, None, None, None, Some(task), None, None, None)
  def ZoneId(zone : PlanetSideZoneID) : SquadDetail =
    SquadDetail(None, None, None, None, None, None, Some(zone), None, None)
  def Field7(unk7 : Int) : SquadDetail =
    SquadDetail(None, None, None, None, None, None, None, Some(unk7), None)
  def Members(list : List[SquadPositionEntry]) : SquadDetail =
    SquadDetail(None, None, None, None, None, None, None, None, Some(list))

  object Fields {
    final val Field1 = 1
    final val CharId = 2
    final val Field3 = 3
    final val Leader = 4
    final val Task = 5
    final val ZoneId = 6
    final val Field7 = 7
    final val Members = 8
  }
}

object SquadDetailDefinitionUpdateMessage extends Marshallable[SquadDetailDefinitionUpdateMessage] {
  /**
    * The patterns necessary to read uncoded squad data.
    * All squad fields and all squad position fields are parsed.
    */
  object FullSquad {
    /**
      * The first squad position entry has its first string (`role`) field padded by a constant amount.
      */
    private val first_position_codec : Codec[SquadPositionDetail] = basePositionCodec(bitsOverByteLength = 1, DefaultRequirements)
    /**
      * All squad position entries asides from the first have unpadded strings.
      * The very first entry aligns the remainder of the string fields along byte boundaries.
      */
    private val position_codec : Codec[SquadPositionDetail] = basePositionCodec(bitsOverByteLength = 0, DefaultRequirements)

    /**
      * Internal class for linked list operations.
      * @param info details regarding the squad position
      * @param next if there is a "next" squad position
      */
    private final case class LinkedFields(info: SquadPositionDetail, next: Option[LinkedFields])

    /**
      * Parse each squad position field in the bitstream after the first one.
      * @return a pattern outlining sequential squad positions
      */
    private def subsequent_member_codec : Codec[LinkedFields] = {
      import shapeless.::
      (
        //disruptive coupling action (e.g., flatPrepend) is necessary for recursive Codec
        ("member" | position_codec) >>:~ { _ =>
          optional(bool, "next" | subsequent_member_codec).hlist
        }
        ).xmap[LinkedFields] (
        {
          case a :: b :: HNil =>
            LinkedFields(a, b)
        },
        {
          case LinkedFields(a, b) =>
            a :: b :: HNil
        }
      )
    }

    /**
      * Parse the first squad position field in the bitstream.
      * @return a pattern outlining sequential squad positions
      */
    private def initial_member_codec : Codec[LinkedFields] = {
      import shapeless.::
      (
        ("member" | first_position_codec) ::
          optional(bool, "next" | subsequent_member_codec)
        ).xmap[LinkedFields] (
        {
          case a :: b :: HNil =>
            LinkedFields(a, b)
        },
        {
          case LinkedFields(a, b) =>
            a :: b :: HNil
        }
      )
    }

    /**
      * Transform a linked list of squad position data into a normal `List`.
      * @param list the current section of the original linked list
      * @param out the accumulative traditional `List`
      * @return the final `List` output
      */
    @tailrec
    private def unlinkFields(list : LinkedFields, out : List[SquadPositionDetail] = Nil) : List[SquadPositionDetail] = {
      list.next match {
        case None =>
          out :+ list.info
        case Some(next) =>
          unlinkFields(next, out :+ list.info)
      }
    }

    /**
      * Transform a normal `List` of squad position data into a linked list.
      * The original list becomes reversed in the process.
      * @param list the original traditional `List`
      * @return the final linked list output
      */
    private def linkFields(list : List[SquadPositionDetail]) : LinkedFields = {
      list match {
        case Nil =>
          throw new Exception("")
        case x :: Nil =>
          LinkedFields(x, None)
        case x :: xs =>
          linkFields(xs, LinkedFields(x, None))
      }
    }

    /**
      * Transform a normal `List` of squad position data into a linked list.
      * The original list becomes reversed in the process.
      * @param list the current subsection of the original traditional `List`
      * @param out the accumulative linked list
      * @return the final linked list output
      */
    @tailrec
    private def linkFields(list : List[SquadPositionDetail], out : LinkedFields) : LinkedFields = {
      list match {
        case Nil =>
          out
        case x :: Nil =>
          LinkedFields(x, Some(out))
        case x :: xs =>
          linkFields(xs, LinkedFields(x, Some(out)))
      }
    }

    /**
      * Entry point.
      */
    val codec : Codec[SquadDetail] = {
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
                Some(unlinkFields(member_list).zipWithIndex.map { case (entry, index) => SquadPositionEntry(index, Some(entry)) })
              )
            )
          case data =>
            Attempt.failure(Err(s"can not get squad detail definition from data $data"))
        },
        {
          case SquadDetail(Some(u1), Some(u2), Some(char_id), Some(u3), Some(leader), Some(task), Some(zone), Some(unk7), Some(member_list)) =>
            Attempt.Successful(
              math.max(u1, 1) :: u2 :: char_id :: u3 :: leader :: task :: zone :: unk7 ::
                Some(linkFields(member_list.collect { case SquadPositionEntry(_, Some(entry)) => entry }.reverse)) ::
                HNil
            )
        }
      )
    }
  }

  /**
    * The patterns necessary to read coded squad data fields.
    * Any number of squad fields can be parsed,
    * but the number is always counted and the fields are always preceded by a unique action code.
    * Only important fields are listed as if to update them;
    * unlisted fields indicate fields that do not get updated from their current values.
    */
  object ItemizedSquad {
    /**
      * A pattern for data related to "field1."
      */
    private val field1Codec : Codec[SquadDetail] = uint16L.exmap[SquadDetail] (
      unk1 => Attempt.successful(SquadDetail(Some(unk1), None, None, None, None, None, None, None, None)),
      {
        case SquadDetail(Some(unk1), _, _, _,  _, _, _, _, _) =>
          Attempt.successful(unk1)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for unknown field #1"))
      }
    )
    /**
      * A pattern for data related to the squad leader's `char_id` field.
      */
    private val leaderCharIdCodec : Codec[SquadDetail] = uint32L.exmap[SquadDetail] (
      char_id => Attempt.successful(SquadDetail(None, None, Some(char_id), None, None, None, None, None, None)),
      {
        case SquadDetail(_, _, Some(char_id), _,  _, _, _, _, _) =>
          Attempt.successful(char_id)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for leader id"))
      }
    )
    /**
      * A pattern for data related to "field3."
      */
    private val field3Codec : Codec[SquadDetail] = uint32L.exmap[SquadDetail] (
      unk3 => Attempt.successful(SquadDetail(None, None, None, Some(unk3), None, None, None, None, None)),
      {
        case SquadDetail(_, _, _, Some(unk3),  _, _, _, _, _) =>
          Attempt.successful(unk3)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for unknown field #3"))
      }
    )
    /**
      * A pattern for data related to the squad leader's `name` field.
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    private def leaderNameCodec(bitsOverByte : StreamLengthToken) : Codec[SquadDetail] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadDetail] (
      name => Attempt.successful(SquadDetail(None, None, None, None, Some(name), None, None, None, None)),
      {
        case SquadDetail(_, _, _, _, Some(name), _, _, _, _) =>
          Attempt.successful(name)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for leader name"))
      }
    )
    /**
      * A pattern for data related to the squad's `task` field, also often described as the squad description.
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    private def taskCodec(bitsOverByte : StreamLengthToken) : Codec[SquadDetail] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadDetail] (
      task => Attempt.successful(SquadDetail(None, None, None, None, None, Some(task), None, None, None)),
      {
        case SquadDetail(_, _, _, _, _, Some(task), _, _, _) =>
          Attempt.successful(task)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for task"))
      }
    )
    /**
      * A pattern for data related to the squad leader's `zone_id` field.
      * @see `PlanetSideZoneID.codec`
      */
    private val zoneCodec : Codec[SquadDetail] = PlanetSideZoneID.codec.exmap[SquadDetail] (
      zone_id => Attempt.successful(SquadDetail(None, None, None, None, None, None, Some(zone_id), None, None)),
      {
        case SquadDetail(_, _, _, _, _, _, Some(zone_id), _, _) =>
          Attempt.successful(zone_id)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for zone id"))
      }
    )
    /**
      * A pattern for data related to "field7."
      */
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
    /**
      * A pattern for data related to the squad's position entry fields.
      * The actual parsing of the data for the positions diverges
      * into either an itemized parsing pattern
      * or a fully populated parsing pattern.
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    private def membersCodec(bitsOverByte : StreamLengthToken) : Codec[SquadDetail] = {
      import shapeless.::
      (
        bool >>:~ { flag =>
          conditional(flag, {
            bitsOverByte.Add(4)
            uint(3) :: vector(ItemizedPositions.codec(bitsOverByte))
          }) ::
            conditional(!flag, {
              bitsOverByte.Add(3)
              uint2 :: FullyPopulatedPositions.codec(bitsOverByte)
            })
        }
        ).exmap[SquadDetail] (
        {
          case true :: Some(_ :: member_list :: HNil) :: _ :: HNil =>
            Attempt.successful(SquadDetail(None, None, None, None, None, None, None, None, Some(ignoreTerminatingEntry(member_list.toList))))
          case false :: None :: Some(_ :: member_list :: HNil) :: HNil =>
            Attempt.successful(SquadDetail(None, None, None, None, None, None, None, None, Some(ignoreTerminatingEntry(member_list.toList))))
        },
        {
          case SquadDetail(_, _, _, _, _, _, _, _, Some(member_list)) =>
            if(member_list
              .collect { case position if position.info.nonEmpty =>
                val info = position.info.get
                List(info.is_closed, info.role, info.detailed_orders, info.requirements, info.char_id, info.name)
              }
              .flatten
              .count(_.isEmpty) == 0) {
              Attempt.successful(false :: None :: Some(2 :: ensureTerminatingEntry(member_list).toVector :: HNil) :: HNil)
            }
            else {
              Attempt.successful(true :: Some(4 :: ensureTerminatingEntry(member_list).toVector :: HNil) :: None :: HNil)
            }
          case _ =>
            Attempt.failure(Err("failed to encode squad data for members"))
        }
      )
    }
    //TODO while this pattern looks elegant, bitsOverByte does not accumulate properly with the either(bool, L, R); why?
//    private def membersCodec(bitsOverByte : StreamLengthToken) : Codec[SquadDetail] = {
//      import shapeless.::
//      either(bool,
//        { //false
//          bitsOverByte.Add(3)
//          uint2 :: FullyPopulatedPositions.codec(bitsOverByte)
//        },
//        { //true
//          bitsOverByte.Add(4)
//          uint(3) :: vector(ItemizedPositions.codec(bitsOverByte))
//        }
//      ).exmap[SquadDetail] (
//        {
//          case Left(_ :: member_list :: HNil) =>
//            Attempt.successful(SquadDetail(None, None, None, None, None, None, None, None, Some(ignoreTerminatingEntry(member_list.toList))))
//          case Right(_ :: member_list :: HNil) =>
//            Attempt.successful(SquadDetail(None, None, None, None, None, None, None, None, Some(ignoreTerminatingEntry(member_list.toList))))
//        },
//        {
//          case SquadDetail(_, _, _, _, _, _, _, _, Some(member_list)) =>
//            if(member_list
//              .collect { case position if position.info.nonEmpty =>
//                val info = position.info.get
//                List(info.is_closed, info.role, info.detailed_orders, info.requirements, info.char_id, info.name)
//              }
//              .flatten
//              .count(_.isEmpty) == 0) {
//              Attempt.successful(Left(2 :: ensureTerminatingEntry(member_list).toVector :: HNil))
//            }
//            else {
//              Attempt.successful(Right(4 :: ensureTerminatingEntry(member_list).toVector :: HNil))
//            }
//          case _ =>
//            Attempt.failure(Err("failed to encode squad data for members"))
//        }
//      )
//    }
    /**
      * A failing pattern for when the coded value is not tied to a known field pattern.
      * This pattern does not read or write any bit data.
      * The `conditional` will always return `None` because
      * its determining conditional statement is explicitly `false`
      * and all cases involving explicit failure.
      * @param code the unknown action code
      */
    private def failureCodec(code : Int) : Codec[SquadDetail] = conditional(included = false, bool).exmap[SquadDetail] (
      _ => Attempt.failure(Err(s"decoding squad data with unhandled codec - $code")),
      _ => Attempt.failure(Err(s"encoding squad data with unhandled codec - $code"))
    )

    /**
      * Retrieve the field pattern by its associated action code.
      * @param code the action code
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      * @return the field pattern
      */
    private def selectCodedAction(code : Int, bitsOverByte : StreamLengthToken) : Codec[SquadDetail] = {
      code match {
        case 1 => field1Codec
        case 2 => leaderCharIdCodec
        case 3 => field3Codec
        case 4 => leaderNameCodec(bitsOverByte)
        case 5 => taskCodec(bitsOverByte)
        case 6 => zoneCodec
        case 7 => field7Codec
        case 8 => membersCodec(bitsOverByte)
        case _ => failureCodec(code)
      }
    }

    /**
      * Advance information about the current stream length because on which pattern was previously utilized.
      * @see `selectCodedAction(Int, StreamLengthToken)`
      * @param code the action code, connecting to a field pattern
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      * @return a modified token maintaining stream misalignment
      */
    private def modifyCodedPadValue(code : Int, bitsOverByte : StreamLengthToken) : StreamLengthToken = {
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

    /**
      * Internal class for linked list operations.
      * @param code action code indicating the squad field
      * @param info data for the squad field
      * @param next if there is a "next" squad field
      */
    private final case class LinkedFields(code : Int, info : SquadDetail, next : Option[LinkedFields])

    /**
      * Transform a linked list of individual squad field data into a combined squad data object.
      * @param list the current section of the original linked list
      * @return the final squad object output
      */
    private def unlinkFields(list : LinkedFields) : SquadDetail = unlinkFields(Some(list))

    /**
      * Transform a linked list of individual squad field data into a combined squad data object.
      * @param info the current section of the original linked list
      * @param out the accumulative squad data object
      * @return the final squad object output
      */
    @tailrec
    private def unlinkFields(info : Option[LinkedFields], out : SquadDetail = SquadDetail.Blank) : SquadDetail = {
      info match {
        case None =>
          out
        case Some(sqInfo) =>
          unlinkFields(sqInfo.next, out And sqInfo.info)
      }
    }

    /**
      * Transform a squad detail object whose field population may be sparse into a linked list of individual fields.
      * Fields of the combined object are separated into a list of pairs
      * of each of those fields's action codes and a squad detail object with only that given field populated.
      * After the blank entries are eliminated, the remaining fields are transformed into a linked list.
      * @param info the combined squad detail object
      * @return the final linked list output
      */
    private def linkFields(info : SquadDetail) : LinkedFields = {
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
          LinkedFields(code, squadInfo, None)
        case x :: xs =>
          val (code, squadInfo) = x
          linkFields(xs, LinkedFields(code, squadInfo, None))
      }
    }

    /**
      * Transform a `List` of squad field data paired with its field action code into a linked list.
      * @param list the current subsection of the original list of fields
      * @param out the accumulative linked list
      * @return the final linked list output
      */
    @tailrec
    private def linkFields(list : Seq[(Int, SquadDetail)], out : LinkedFields) : LinkedFields = {
      if(list.isEmpty) {
        out
      }
      else {
        val (code, data) = list.head
        linkFields(list.tail, LinkedFields(code, data, Some(out)))
      }
    }

    /**
      * Parse each action code to determine the format of the following squad field.
      * Keep parsing until all reported squad fields have been encountered.
      * @param size the number of fields to be parsed
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      * @return a linked list composed of the squad fields
      */
    private def chain_linked_fields(size : Int, bitsOverByte : StreamLengthToken) : Codec[LinkedFields] = {
      import shapeless.::
      (
        //disruptive coupling action (e.g., flatPrepend) is necessary for recursive Codec
        uint4 >>:~ { code =>
          selectCodedAction(code, bitsOverByte.Add(4)) ::
            conditional(size - 1 > 0, chain_linked_fields(size - 1, modifyCodedPadValue(code, bitsOverByte)))
        }
        ).exmap[LinkedFields] (
        {
          case action :: detail :: next :: HNil =>
            Attempt.Successful(LinkedFields(action, detail, next))
        },
        {
          case LinkedFields(action, detail, next) =>
            Attempt.Successful(action :: detail :: next :: HNil)
        }
      )
    }

    /**
      * Entry point.
      * The stream misalignment will always be by 1 bit over the previous boundary when this is invoked.
      * @param size the number of squad fields to be parsed
      * @return a pattern for parsing the coded squad field data between a coded linked list and a combined squad object
      */
    def codec(size : Int) : Codec[SquadDetail] = chain_linked_fields(size, new StreamLengthToken(1)).xmap[SquadDetail] (
      linkedDetail => unlinkFields(linkedDetail),
      unlinkedDetail => linkFields(unlinkedDetail)
    )
  }

  /**
    * The patterns necessary to read coded itemized squad position data fields.
    * The main squad position data has been completed and now the squad's open positions are being parsed.
    * Any number of squad position fields can be parsed,
    * but the number is always counted and the fields are always preceded by a unique action code.
    * Only important fields are listed as if to update them;
    * unlisted fields indicate fields that do not get updated from their current values.
    */
  object ItemizedPositions {
    /**
      * A pattern for data related to the squad position's `is_closed` field.
      */
    private val isClosedCodec : Codec[SquadPositionDetail] = bool.exmap[SquadPositionDetail] (
      state => Attempt.successful(SquadPositionDetail(Some(state), None, None, None, None, None)),
      {
        case SquadPositionDetail(Some(state), _, _, _, _, _) =>
          Attempt.successful(state)
        case _ =>
          Attempt.failure(Err("failed to encode squad position data for availability"))
      }
    )
    /**
      * A pattern for data related to the squad position's `role` field.
      * @see `SquadDetailDefinitionUpdateMessage.paddedStringMetaCodec`
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    private def roleCodec(bitsOverByte : StreamLengthToken) : Codec[SquadPositionDetail] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadPositionDetail] (
      role => Attempt.successful(SquadPositionDetail(None, Some(role), None, None, None, None)),
      {
        case SquadPositionDetail(_, Some(role), _, _, _, _) =>
          Attempt.successful(role)
        case _ =>
          Attempt.failure(Err("failed to encode squad position data for role"))
      }
    )
    /**
      * A pattern for data related to the squad position's `detailed_orders` field.
      * @see `SquadDetailDefinitionUpdateMessage.paddedStringMetaCodec`
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    private def ordersCodec(bitsOverByte : StreamLengthToken) : Codec[SquadPositionDetail] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadPositionDetail] (
      orders => Attempt.successful(SquadPositionDetail(None, None, Some(orders), None, None, None)),
      {
        case SquadPositionDetail(_, _, Some(orders), _, _, _) =>
          Attempt.successful(orders)
        case _ =>
          Attempt.failure(Err("failed to encode squad position data for detailed orders"))
      }
    )
    /**
      * A pattern for data related to the squad position's `requirements` field.
      * @see `CertificationType.fromEncodedLong`
      * @see `CertificationType.toEncodedLong`
      * @see `SquadDefinitionActionMessage.ChangeSquadMemberRequirementsCertifications`
      */
    private val requirementsCodec : Codec[SquadPositionDetail] = ulongL(46).exmap[SquadPositionDetail] (
      requirements => Attempt.successful(SquadPositionDetail(None, None, None, Some(CertificationType.fromEncodedLong(requirements)), None, None)),
      {
        case SquadPositionDetail(_, _, _, Some(requirements), _, _) =>
          Attempt.successful(CertificationType.toEncodedLong(requirements))
        case _ =>
          Attempt.failure(Err("failed to encode squad position data for certification requirements"))
      }
    )
    /**
      * A pattern for data related to the squad position's `char_id` field, when occupied.
      */
    private val charIdCodec : Codec[SquadPositionDetail] = uint32L.exmap[SquadPositionDetail] (
      char_id => Attempt.successful(SquadPositionDetail(None, None, None, None, Some(char_id), None)),
      {
        case SquadPositionDetail(_, _, _, _, Some(char_id), _) =>
          Attempt.successful(char_id)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for member id"))
      }
    )
    /**
      * A pattern for data related to the squad position's `name` field, when occupied.
      * @see `SquadDetailDefinitionUpdateMessage.paddedStringMetaCodec`
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    private def nameCodec(bitsOverByte : StreamLengthToken) : Codec[SquadPositionDetail] = paddedStringMetaCodec(bitsOverByte.Length).exmap[SquadPositionDetail] (
      name => Attempt.successful(SquadPositionDetail(None, None, None, None, None, Some(name))),
      {
        case SquadPositionDetail(_, _, _, _, _, Some(orders)) =>
          Attempt.successful(orders)
        case _ =>
          Attempt.failure(Err("failed to encode squad position data for member name"))
      }
    )
    /**
      * A failing pattern for when the coded value is not tied to a known field pattern.
      * This pattern does not read or write any bit data.
      * The `conditional` will always return `None` because
      * its determining conditional statement is explicitly `false`
      * and all cases involving explicit failure.
      * @param code the unknown action code
      */
    private def failureCodec(code : Int) : Codec[SquadPositionDetail] = conditional(included = false, bool).exmap[SquadPositionDetail] (
      _ => Attempt.failure(Err(s"decoding squad position data with unhandled codec - $code")),
      _ => Attempt.failure(Err(s"encoding squad position data with unhandled codec - $code"))
    )

    /**
      * Retrieve the field pattern by its associated action code.
      * @param code the action code
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      * @return the field pattern
      */
    private def selectCodedAction(code : Int, bitsOverByte : StreamLengthToken) : Codec[SquadPositionDetail] = {
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

    /**
      * Advance information about the current stream length because on which pattern was previously utilized.
      * @see `selectCodedAction(Int, StreamLengthToken)`
      * @param code the action code, connecting to a field pattern
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      * @return a modified token maintaining stream misalignment
      */
    private def modifyCodedPadValue(code : Int, bitsOverByte : StreamLengthToken) : StreamLengthToken = {
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

    /**
      * Internal class for linked list operations.
      * @param code action code indicating the squad position field
      * @param info data for the squad position field
      * @param next if there is a "next" squad position field
      */
    private final case class LinkedFields(code : Int, info : SquadPositionDetail, next : Option[LinkedFields])

    /**
      * Transform a linked list of individual squad position field data into a combined squad position object.
      * @param info the current section of the original linked list
      * @param out the accumulative squad position data object
      * @return the final squad position object output
      */
    @tailrec
    private def unlinkFields(info : Option[LinkedFields], out : SquadPositionDetail = SquadPositionDetail.Blank) : SquadPositionDetail = {
      info match {
        case None =>
          out
        case Some(sqInfo) =>
          unlinkFields(sqInfo.next, out And sqInfo.info)
      }
    }

    /**
      * Transform a squad position object whose field population may be sparse into a linked list of individual fields.
      * Fields of the combined object are separated into a list of pairs
      * of each of those fields's action codes and a squad position object with only that given field populated.
      * After the blank entries are eliminated, the remaining fields are transformed into a linked list.
      * @param info the combined squad position object
      * @return the final linked list output
      */
    private def linkFields(info : SquadPositionDetail) : LinkedFields = {
      Seq(
        (5, SquadPositionDetail(None, None, None, info.requirements, None, None)),
        (4, SquadPositionDetail(None, None, None, None, None, info.name)),
        (3, SquadPositionDetail(None, None, None, None, info.char_id, None)),
        (2, SquadPositionDetail(None, None, info.detailed_orders, None, None, None)),
        (1, SquadPositionDetail(None, info.role, None, None, None, None)),
        (0, SquadPositionDetail(info.is_closed, None, None, None, None, None))
      ) //in reverse order so that the linked list is in the correct order
        .filterNot { case (_, sqInfo) => sqInfo == SquadPositionDetail.Blank}
      match {
        case Nil =>
          throw new Exception("no linked list squad position fields encountered where at least one was expected") //bad end
        case x :: Nil =>
          val (code, squadInfo) = x
          LinkedFields(code, squadInfo, None)
        case x :: xs =>
          val (code, squadInfo) = x
          linkFields(xs, LinkedFields(code, squadInfo, None))
      }
    }

    /**
      * Transform a `List` of squad position field data paired with its field action code into a linked list.
      * @param list the current subsection of the original list of fields
      * @param out the accumulative linked list
      * @return the final linked list output
      */
    @tailrec
    private def linkFields(list : Seq[(Int, SquadPositionDetail)], out : LinkedFields) : LinkedFields = {
      if(list.isEmpty) {
        out
      }
      else {
        val (code, data) = list.head
        linkFields(list.tail, LinkedFields(code, data, Some(out)))
      }
    }

    /**
      * Parse each action code to determine the format of the following squad position field.
      * Keep parsing until all reported squad position fields have been encountered.
      * @param size the number of fields to be parsed
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      * @return a linked list composed of the squad position fields
      */
    private def chain_linked_fields(size : Int, bitsOverByte : StreamLengthToken) : Codec[LinkedFields] = {
      import shapeless.::
      (
        uint4 >>:~ { code =>
          selectCodedAction(code,  bitsOverByte.Add(4)) >>:~ { _ =>
            modifyCodedPadValue(code, bitsOverByte)
            conditional(size - 1 > 0, chain_linked_fields(size - 1, bitsOverByte)).hlist
          }
        }
        ).xmap[LinkedFields] (
        {
          case code :: entry :: next :: HNil =>
            LinkedFields(code, entry, next)
        },
        {
          case LinkedFields(code, entry, next) =>
            code :: entry :: next :: HNil
        }
      )
    }

    /**
      * Parse the number of squad position fields anticipated and then start parsing those position fields.
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    private def squad_member_details_codec(bitsOverByte : StreamLengthToken) : Codec[LinkedFields] = {
      import shapeless.::
      (
        uint8 >>:~ { size =>
          chain_linked_fields(size, bitsOverByte).hlist
        }
        ).xmap[LinkedFields] (
        {
          case _ :: info :: HNil =>
            info
        },
        info => {
          //count the linked position fields by tracing the "next" field in the linked list
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

    /**
      * Entry point.
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    def codec(bitsOverByte : StreamLengthToken) : Codec[SquadPositionEntry] = {
      import shapeless.::
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
            SquadPositionEntry(ndx, Some(unlinkFields(Some(info))))
        },
        {
          case SquadPositionEntry(255, _) =>
            255 :: None :: None :: HNil
          case SquadPositionEntry(ndx, Some(info)) =>
            ndx :: Some(true :: linkFields(info) :: HNil) :: None :: HNil
        }
      )
    }
  }

  /**
    * The patterns necessary to read enumerated squad position data.
    * The main squad position data has been completed and now the squad's open positions are being parsed.
    * These patterns split the difference between `FullSquad` operations and `ItemizedSquad` operations.
    * Normally the whole of the squad position data is parsed in a single pass in `FullSquad`
    * and, during `ItemizedSquad`, only piecemeal squad position fields are parsed.
    * Furthermore, `FullSquad` position data is un-indexed because it is always presented in correct order,
    * and `ItemizedSquad` positional data is indexed because it can skip entries and may be encountered in any order.
    * These patterns parse full squad position data that is also indexed.
    */
  object FullyPopulatedPositions {
    /**
      * The primary difference between the cores of `FullSquad` position data and `FullyPopulatedPositions` data,
      * besides variable padding,
      * involves the `requirements` field not having a basic set of values that are always masked.
      * @param bitsOverByteLength a token maintaining stream misalignment for purposes of calculating string padding
      */
    private def position_codec(bitsOverByteLength : Int) : Codec[SquadPositionDetail] = basePositionCodec(bitsOverByteLength, Set.empty)

    /**
      * Internal class for linked list operations.
      * @param index the current position's ordinal number
      * @param info data for the squad position field
      * @param next if there is a "next" squad position field
      */
    private final case class LinkedFields(index: Int, info: SquadPositionDetail, next: Option[LinkedFields])

    /**
      * Transform a linked list of squad position data into a normal `List`.
      * @param list the current section of the original linked list
      * @param out the accumulative traditional `List`
      * @return the final `List` output
      */
    @tailrec
    private def unlinkFields(list : LinkedFields, out : List[SquadPositionEntry] = Nil) : List[SquadPositionEntry] = {
      list.next match {
        case None =>
          out :+ SquadPositionEntry(list.index, list.info)
        case Some(next) =>
          unlinkFields(next, out :+ SquadPositionEntry(list.index, list.info))
      }
    }

    /**
      * Transform a normal `List` of squad position data into a linked list.
      * The original list becomes reversed in the process.
      * @param list the original traditional `List`
      * @return the final linked list output
      */
    private def linkFields(list : List[SquadPositionEntry]) : LinkedFields = {
      list match {
        case Nil =>
          throw new Exception("")
        case x :: xs if x.info.isEmpty =>
          linkFields(xs, LinkedFields(x.index, SquadPositionDetail.Blank, None))
        case x :: xs =>
          linkFields(xs, LinkedFields(x.index, x.info.get, None))
      }
    }

    /**
      * Transform a normal `List` of squad position data into a linked list.
      * The original list becomes reversed in the process.
      * @param list the current subsection of the original traditional `List`
      * @param out the accumulative linked list
      * @return the final linked list output
      */
    @tailrec
    private def linkFields(list : List[SquadPositionEntry], out : LinkedFields) : LinkedFields = {
      list match {
        case Nil =>
          out
        case x :: _ if x.info.isEmpty =>
          LinkedFields(x.index, SquadPositionDetail.Blank, Some(out))
        case x :: Nil =>
          LinkedFields(x.index, x.info.get, Some(out))
        case x :: xs =>
          linkFields(xs, LinkedFields(x.index, x.info.get, Some(out)))
      }
    }

    /**
      * All squad position entries asides from the first have unpadded strings.
      * The very first entry aligns the remainder of the string fields along byte boundaries.
      */
    private def subsequent_position_codec(size : Int) : Codec[LinkedFields] = {
      import shapeless.::
      (
        uint8 >>:~ { index =>
          conditional(index < 255, bool :: position_codec(bitsOverByteLength = 0)) ::
            conditional(size - 1 > 0, subsequent_position_codec(size - 1)) ::
            conditional(index == 255, bits)
        }
        ).xmap[LinkedFields](
        {
          case 255 :: _ :: _ :: _ :: HNil =>
            LinkedFields(255, SquadPositionDetail.Blank, None)
          case index :: Some(_ :: entry :: HNil) :: next :: _ :: HNil =>
            LinkedFields(index, entry, next)
        },
        {
          case LinkedFields(255, _, _) =>
            255 :: None :: None :: None :: HNil
          case LinkedFields(index, entry, next) =>
            index :: Some(true :: entry :: HNil) :: next :: None :: HNil
        }
      )
    }

    /**
      * The first squad position entry has its first string (`role`) field padded by an amount that can be determined.
      * @param size the number of position entries to be parsed
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    private def initial_position_codec(size : Int, bitsOverByte : StreamLengthToken) : Codec[LinkedFields] = {
      import shapeless.::
      (
        uint8 >>:~ { index =>
          conditional(index < 255, {
            bitsOverByte.Add(2) //1 (below) + 1 (position_codec)
            bool :: position_codec(bitsOverByte.Length)
          }) ::
            conditional(index < 255 && size - 1 > 0, subsequent_position_codec(size - 1)) ::
            conditional(index == 255, bits)
        }
        ).xmap[LinkedFields](
        {
          case 255 :: _ :: _ :: _ :: HNil =>
            LinkedFields(255, SquadPositionDetail.Blank, None)
          case index :: Some(_ :: entry :: HNil) :: next :: _ :: HNil =>
            LinkedFields(index, entry, next)
        },
        {
          case LinkedFields(255, _, _) =>
            255 :: None :: None :: None :: HNil
          case LinkedFields(index, entry, next) =>
            index :: Some(true :: entry :: HNil) :: next :: None :: HNil
        }
      )
    }

    /**
      * Entry point.
      * @param bitsOverByte a token maintaining stream misalignment for purposes of calculating string padding
      */
    def codec(bitsOverByte : StreamLengthToken) : Codec[Vector[SquadPositionEntry]] = {
      import shapeless.::
      (
        uint32L >>:~ { size =>
          bitsOverByte.Add(4)
          uint4 ::
            initial_position_codec(size.toInt + 1, bitsOverByte)
        }).xmap[Vector[SquadPositionEntry]] (
        {
          case _ :: _ :: linkedMembers :: HNil =>
            ignoreTerminatingEntry(unlinkFields(linkedMembers)).toVector
        },
        memberList => 10 :: 12 :: linkFields(ensureTerminatingEntry(memberList.toList).reverse) :: HNil
      )
    }
  }

  /**
    * Certification values that are front-loaded into the `FullSquad` operations for finding squad position requirements.
    * In the game proper, these are three certification values that the user can not give up or interact with.
    */
  final val DefaultRequirements : Set[CertificationType.Value] = Set(
    CertificationType.StandardAssault,
    CertificationType.StandardExoSuit,
    CertificationType.AgileExoSuit
  )

  /**
    * Blank squad data set up for `FullSquad` parsing.
    * The `guid` value is significant - it represents the client-local squad data.
    */
  final val Init = SquadDetailDefinitionUpdateMessage(PlanetSideGUID(0), SquadDetail().Complete)

  /**
    * Produces a byte-aligned Pascal strings encoded through common manipulations.
    * Rather than pass in the amount of the padding directly, however,
    * the stream length or the misalignment to the stream's previous byte boundary is passed into the function
    * and is converted into the proper padding value.
    * @see `PacketHelpers.encodedWideStringAligned`
    * @param bitsOverByte the number of bits past the previous byte-aligned index;
    *             gets converted to a 0-7 string padding number based on how many bits remain befoire the next byte
    * @return the padded string `Codec`
    */
  private def paddedStringMetaCodec(bitsOverByte : Int) : Codec[String] = PacketHelpers.encodedWideStringAligned({
    val mod8 = bitsOverByte % 8
    if(mod8 == 0) {
      0
    }
    else {
      8 - mod8
    }
  })

  /**
    * Pattern for reading all of the fields for squad position data.
    * @param bitsOverByteLength the number of bits past the previous byte-aligned index
    * @param defaultRequirements `CertificationType` values that are automatically masked in the `requirements` field
    */
  private def basePositionCodec(bitsOverByteLength : Int, defaultRequirements : Set[CertificationType.Value]) : Codec[SquadPositionDetail] = {
    import shapeless.::
    (
      uint8 :: //required value = 6
        ("is_closed" | bool) :: //if all positions are closed, the squad detail menu display no positions at all
        ("role" | paddedStringMetaCodec(bitsOverByteLength)) ::
        ("detailed_orders" | PacketHelpers.encodedWideString) ::
        ("char_id" | uint32L) ::
        ("name" | PacketHelpers.encodedWideString) ::
        ("requirements" | ulongL(46))
      ).exmap[SquadPositionDetail] (
      {
        case 6 :: closed :: role :: orders :: char_id :: name :: requirements :: HNil =>
          Attempt.Successful(
            SquadPositionDetail(Some(closed), Some(role), Some(orders), Some(defaultRequirements ++ CertificationType.fromEncodedLong(requirements)), Some(char_id), Some(name))
          )
        case data =>
          Attempt.Failure(Err(s"can not decode a SquadDetailDefinitionUpdate member's data - $data"))
      },
      {
        case SquadPositionDetail(Some(closed), Some(role), Some(orders), Some(requirements), Some(char_id), Some(name)) =>
          Attempt.Successful(6 :: closed :: role :: orders :: char_id :: name :: CertificationType.toEncodedLong(defaultRequirements ++ requirements) :: HNil)
      }
    )
  }

  /**
    * The last entry in the sequence of squad information listings should be a dummied listing with an index of 255.
    * Ensure that this terminal entry is located at the end.
    * @param list the listing of squad information
    * @return the listing of squad information, with a specific final entry
    */
  private def ensureTerminatingEntry(list : List[SquadPositionEntry]) : List[SquadPositionEntry] = {
    list.lastOption match {
      case Some(SquadPositionEntry(255, _)) => list
      case Some(_) | None => list :+ SquadPositionEntry(255, None)
    }
  }

  /**
    * The last entry in the sequence of squad information listings should be a dummied listing with an index of 255.
    * Remove this terminal entry from the end of the list so as not to hassle with it.
    * @param list the listing of squad information
    * @return the listing of squad information, with a specific final entry truncated
    */
  private def ignoreTerminatingEntry(list : List[SquadPositionEntry]) : List[SquadPositionEntry] = {
    list.lastOption match {
      case Some(SquadPositionEntry(255, _)) => list.init
      case Some(_) | None => list
    }
  }

  implicit val codec : Codec[SquadDetailDefinitionUpdateMessage] = {
    import shapeless.::
    import net.psforever.newcodecs.newcodecs
    (
      ("guid" | PlanetSideGUID.codec) ::
        bool ::
        (uint8 >>:~ { size =>
          newcodecs.binary_choice(
            size == 9,
            FullSquad.codec,
            ItemizedSquad.codec(size)
          ).hlist
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
            //full squad detail definition protocol
            Attempt.Successful(guid :: true :: 9 :: info :: HNil)
          }
      }
    )
  }
}
