// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.HNil

import scala.annotation.tailrec

/**
  * Maintain squad information for a given squad's listing.
  * Only certain information will be transmitted depending on the purpose of the packet.
  * @param leader the name of the squad leader, usually the first person in the squad member list;
  *               `None` if not applicable
  * @param task the task the squad is trying to perform as a wide character string;
  *             `None` if not applicable
  * @param zone_id the continent on which the squad is acting;
  *                `None` if not applicable
  * @param size the current size of the squad;
  *             "can" be greater than `capacity`, though with issues;
  *             `None` if not applicable
  * @param capacity the maximum number of members that the squad can tolerate;
  *                 normal count is 10;
  *                 maximum is 15 but naturally can not be assigned that many;
  *                 `None` if not applicable
  * @param squad_guid a GUID associated with the squad, used to recover the squad definition;
  *                   sometimes it is defined but is still not applicable;
  *                   `None` if not applicable (rarely applicable)
  */
final case class SquadInfo(
    leader: Option[String],
    task: Option[String],
    zone_id: Option[PlanetSideZoneID],
    size: Option[Int],
    capacity: Option[Int],
    squad_guid: Option[PlanetSideGUID] = None
) {

  /**
    * Populate the undefined fields of this object with the populated fields of a second object.
    * If the field is already defined in this object, the provided object does not contribute new data.
    * @param info the `SquadInfo` data to be incorporated into this object's data
    * @return a new `SquadInfo` object, combining with two objects' field data
    */
  def And(info: SquadInfo): SquadInfo = {
    SquadInfo(
      leader.orElse(info.leader),
      task.orElse(info.task),
      zone_id.orElse(info.zone_id),
      size.orElse(info.size),
      capacity.orElse(info.capacity),
      squad_guid.orElse(info.squad_guid)
    )
  }

  //methods intended to combine the fields of itself and another object
  def Leader(leader: String): SquadInfo =
    this And SquadInfo(Some(leader), None, None, None, None, None)
  def Task(task: String): SquadInfo =
    this And SquadInfo(None, Some(task), None, None, None, None)
  def ZoneId(zone: PlanetSideZoneID): SquadInfo =
    this And SquadInfo(None, None, Some(zone), None, None, None)
  def ZoneId(zone: Option[PlanetSideZoneID]): SquadInfo =
    zone match {
      case Some(zoneId) => this And SquadInfo(None, None, zone, None, None, None)
      case None         => SquadInfo(leader, task, zone, size, capacity, squad_guid)
    }
  def Size(sz: Int): SquadInfo =
    this And SquadInfo(None, None, None, Some(sz), None, None)
  def Capacity(cap: Int): SquadInfo =
    this And SquadInfo(None, None, None, None, Some(cap), None)
}

/**
  * An indexed entry in the listing of squads.
  * @param index the listing entry index for this squad;
  *              zero-based;
  *              255 is the maximum index and is reserved to indicate the end of the listings for the packet
  * @param listing the squad data;
  *                `None` when the index is 255, or when invoking a "remove" action on any squad at a known index
  */
final case class SquadListing(index: Int = 255, listing: Option[SquadInfo] = None)

/**
  * Display the list of squads available to a given player.<br>
  * <br>
  * The four main operations are:
  * initializing the list,
  * updating entries in the list,
  * removing entries from the list,
  * and clearing the list.
  * The process of initializing the list and clearing the list actually are performed by similar behavior.
  * Squads would just not be added after the list clears.
  * Moreover, removing entries from the list overrides the behavior to update entries in the list.
  * Squad list entries are typically referenced by their line index.<br>
  * <br>
  * Though often specified with a global identifier, squads are rarely accessed using that identifier.
  * Outside of initialization activities, the specific index of the squad listing is referenced.
  * During the list initialization process, the entries must be in ascending order of index.
  * The total number of entries in a packet is not known until they have all been parsed.
  * The minimum number of entries is "no entries."
  * The maximum number of entries is supposedly 254.
  * The last item is always the index 255 and this is interpreted as the end of the stream.<br>
  * <br>
  * When no updates are provided, the client loads a default (but invalid) selection of data comprising four squads:<br>
  * `0&nbsp;&nbsp;Holeesh&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;another purpose&nbsp;&nbsp;Desolation&nbsp;&nbsp;6/7`<br>
  * `1&nbsp;&nbsp;Korealis&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;another purpose&nbsp;&nbsp;Drugaskan&nbsp;&nbsp;&nbsp;10/10`<br>
  * `2&nbsp;&nbsp;PsychoSanta&nbsp;&nbsp;blah blah blah&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;10/10`<br>
  * `3&nbsp;&nbsp;Squishling&nbsp;&nbsp;&nbsp;another purpose&nbsp;&nbsp;Cyssor&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;8/10`<br>
  * The last entry is entirely in green text.<br>
  * <br>
  * Behaviors:<br>
  * `behavior behavior2`<br>
  * `1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;X&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; `Update where initial entry removes a squad from the list<br>
  * `5&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; `Clear squad list and initialize new squad list<br>
  * `5&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; `Clear squad list (transitions directly into 255-entry)<br>
  * `6&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;X&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; `Update a squad in the list
  * @param behavior a required code that suggests the operations of the data in this packet
  * @param behavior2 an optional code that suggests the operations of the data in this packet;
  *                  during initialization, this code is read;
  *                  it typically flags an "update" action
  * @param entries a `Vector` of the squad listings
  */
final case class ReplicationStreamMessage(behavior: Int, behavior2: Option[Int], entries: Vector[SquadListing])
    extends PlanetSideGamePacket {
  type Packet = ReplicationStreamMessage
  def opcode = GamePacketOpcode.ReplicationStreamMessage
  def encode = ReplicationStreamMessage.encode(this)
}

object SquadInfo {

  /**
    * An entry where no fields are defined.
    */
  final val Blank = SquadInfo()

  def apply(): SquadInfo = SquadInfo(None, None, None, None, None, None)

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the fields.<br>
    * <br>
    * This constructor is not actually used at the moment.
    * @param leader         the name of the squad leader
    * @param task           the task the squad is trying to perform
    * @param continent_guid the continent on which the squad is acting
    * @param size           the current size of the squad
    * @param capacity       the maximum number of members that the squad can tolerate
    * @return a `SquadInfo` object
    */
  def apply(leader: String, task: String, continent_guid: PlanetSideZoneID, size: Int, capacity: Int): SquadInfo = {
    SquadInfo(Some(leader), Some(task), Some(continent_guid), Some(size), Some(capacity))
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the fields.<br>
    * <br>
    * This constructor is used by the `infoCodec`, `alt_infoCodec`, and `allCodec`.
    * @param leader         the name of the squad leader
    * @param task           the task the squad is trying to perform
    * @param continent_guid the continent on which the squad is acting
    * @param size           the current size of the squad
    * @param capacity       the maximum number of members that the squad can tolerate
    * @param squad_guid     a GUID associated with the squad, used to recover the squad definition
    * @return a `SquadInfo` object
    */
  def apply(
      leader: String,
      task: String,
      continent_guid: PlanetSideZoneID,
      size: Int,
      capacity: Int,
      squad_guid: PlanetSideGUID
  ): SquadInfo = {
    SquadInfo(Some(leader), Some(task), Some(continent_guid), Some(size), Some(capacity), Some(squad_guid))
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the important field.
    * @param leader the name of the squad leader
    * @return a `SquadInfo` object
    */
  def apply(leader: String): SquadInfo = {
    SquadInfo(Some(leader), None, None, None, None)
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the important field.<br>
    * <br>
    * Only the `task` field in this packet is a `String`, giving the method a distinct signature.
    * The other field - an `Option[String]` for `leader` - can still be set if passed.<br>
    * <br>
    * Recommended use: `SquadInfo(None, task)`
    * @param leader the name of the squad leader, if not `None`
    * @param task   the task the squad is trying to perform
    * @return a `SquadInfo` object
    */
  def apply(leader: Option[String], task: String): SquadInfo = {
    SquadInfo(leader, Some(task), None, None, None)
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the field.
    * @param continent_guid the continent on which the squad is acting
    * @return a `SquadInfo` object
    */
  def apply(continent_guid: PlanetSideZoneID): SquadInfo = {
    SquadInfo(None, None, Some(continent_guid), None, None)
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the important field.
    * @param size     the current size of the squad
    * @return a `SquadInfo` object
    */
  def apply(size: Int): SquadInfo = {
    SquadInfo(None, None, None, Some(size), None)
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the important field.<br>
    * <br>
    * Two of the fields normally are `Option[Int]`s.
    * Only the `capacity` field in this packet is an `Int`, giving the method a distinct signature.
    * The other field - an `Option[Int]` for `size` - can still be set if passed.<br>
    * <br>
    * Recommended use: `SquadInfo(None, capacity)`
    * @param size     the current size of the squad
    * @param capacity the maximum number of members that the squad can tolerate, if not `None`
    * @return a `SquadInfo` object
    */
  def apply(size: Option[Int], capacity: Int): SquadInfo = {
    SquadInfo(None, None, None, size, Some(capacity))
  }

  /**
    * The codes related to the specific application of different `Codec`s when parsing squad information as different fields.
    * Hence, "field codes."
    * These fields are identified when using `SquadInfo` data in `ReplicationStreamMessage`'s "update" format
    * but are considered absent when performing squad list initialization.
    */
  object Field {
    final val Leader   = 1
    final val Task     = 2
    final val ZoneId   = 3
    final val Size     = 4
    final val Capacity = 5
  }
}

/**
  * An object that contains all of the logic necessary to transform between
  * the various forms of squad information found in formulaic packet data structures
  * and a singular `SquadInfo` object with only the important data fields that were defined.
  */
object SquadHeader {
  //DO NOT IMPORT shapeless.:: TOP LEVEL TO THIS OBJECT - it interferes with required scala.collection.immutable.::

  /**
    * `Codec` for reading `SquadInfo` data from the first entry from a packet with squad list initialization entries.
    */
  private val infoCodec: Codec[SquadInfo] = {
    import shapeless.::
    (("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideString) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideZoneID.codec) ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)).exmap[SquadInfo](
      {
        case sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil =>
          Attempt.successful(SquadInfo(lead, tsk, cguid, sz, cap, sguid))
        case _ =>
          Attempt.failure(Err("failed to decode squad data for adding the initial squad entry"))
      },
      {
        case SquadInfo(Some(lead), Some(tsk), Some(cguid), Some(sz), Some(cap), Some(sguid)) =>
          Attempt.successful(sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for adding the initial squad entry"))
      }
    )
  }

  /**
    * `Codec` for reading `SquadInfo` data from all entries other than the first from a packet with squad list initialization entries.
    */
  private val alt_infoCodec: Codec[SquadInfo] = {
    import shapeless.::
    (
      ("squad_guid" | PlanetSideGUID.codec) ::
        ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
        ("task" | PacketHelpers.encodedWideString) ::
        ("continent_guid" | PlanetSideZoneID.codec) ::
        ("size" | uint4L) ::
        ("capacity" | uint4L)
    ).exmap[SquadInfo](
      {
        case sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil =>
          Attempt.successful(SquadInfo(lead, tsk, cguid, sz, cap, sguid))
        case _ =>
          Attempt.failure(Err("failed to decode squad data for adding an additional squad entry"))
      },
      {
        case SquadInfo(Some(lead), Some(tsk), Some(cguid), Some(sz), Some(cap), Some(sguid)) =>
          Attempt.successful(sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for adding an additional squad entry"))
      }
    )
  }

  /**
    * `Codec` for reading the `SquadInfo` data in an "update all squad data" entry.
    */
  private val allCodec: Codec[SquadInfo] = {
    import shapeless.::
    (
      ("squad_guid" | PlanetSideGUID.codec) ::
        ("leader" | PacketHelpers.encodedWideStringAligned(3)) ::
        ("task" | PacketHelpers.encodedWideString) ::
        ("continent_guid" | PlanetSideZoneID.codec) ::
        ("size" | uint4L) ::
        ("capacity" | uint4L)
    ).exmap[SquadInfo](
      {
        case sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil =>
          Attempt.successful(SquadInfo(lead, tsk, cguid, sz, cap, sguid))
        case _ =>
          Attempt.failure(Err("failed to decode squad data for updating a squad entry"))
      },
      {
        case SquadInfo(Some(lead), Some(tsk), Some(cguid), Some(sz), Some(cap), Some(sguid)) =>
          Attempt.successful(sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for updating a squad entry"))
      }
    )
  }

  /**
    * Produces a `Codec` function for byte-aligned, padded Pascal strings encoded through common manipulations.
    * @see `PacketHelpers.encodedWideStringAligned`
    * @param over the number of bits past the previous byte-aligned index;
    *             should be a 0-7 number that gets converted to a 1-7 string padding number
    * @return the encoded string `Codec`
    */
  private def paddedStringMetaCodec(over: Int): Codec[String] =
    PacketHelpers.encodedWideStringAligned({
      val mod8 = over % 8
      if (mod8 == 0) {
        0
      } else {
        8 - mod8
      }
    })

  /**
    * `Codec` for reading the `SquadInfo` data in an "update squad leader" entry.
    */
  private def leaderCodec(over: Int): Codec[SquadInfo] =
    paddedStringMetaCodec(over).exmap[SquadInfo](
      lead => Attempt.successful(SquadInfo(lead)),
      {
        case SquadInfo(Some(lead), _, _, _, _, _) =>
          Attempt.successful(lead)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for a leader name"))
      }
    )

  /**
    * `Codec` for reading the `SquadInfo` data in an "update task text" entry.
    */
  private def taskCodec(over: Int): Codec[SquadInfo] =
    paddedStringMetaCodec(over).exmap[SquadInfo](
      task => Attempt.successful(SquadInfo(None, task)),
      {
        case SquadInfo(_, Some(task), _, _, _, _) =>
          Attempt.successful(task)
        case _ =>
          Attempt.failure(Err("failed to encode squad data for a task string"))
      }
    )

  /**
    * `Codec` for reading the `SquadInfo` data in an "update squad zone id" entry.
    * In reality, the "zone's id"  is the zone's server ordinal index.
    */
  private val zoneIdCodec: Codec[SquadInfo] = PlanetSideZoneID.codec.exmap[SquadInfo](
    cguid => Attempt.successful(SquadInfo(cguid)),
    {
      case SquadInfo(_, _, Some(cguid), _, _, _) =>
        Attempt.successful(cguid)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for a continent number"))
    }
  )

  /**
    * `Codec` for reading the `SquadInfo` data in an "update squad size" entry.
    */
  private val sizeCodec: Codec[SquadInfo] = uint4L.exmap[SquadInfo](
    sz => Attempt.successful(SquadInfo(sz)),
    {
      case SquadInfo(_, _, _, Some(sz), _, _) =>
        Attempt.successful(sz)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for squad size"))
    }
  )

  /**
    * `Codec` for reading the `SquadInfo` data in an "update squad capacity" entry.
    */
  private val capacityCodec: Codec[SquadInfo] = uint4L.exmap[SquadInfo](
    cap => Attempt.successful(SquadInfo(None, cap)),
    {
      case SquadInfo(_, _, _, _, Some(cap), _) =>
        Attempt.successful(cap)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for squad capacity"))
    }
  )

  /**
    * `Codec` for reading the `SquadInfo` data in a "remove squad from list" entry.
    * While the input has no impact, it always writes the number four to a `3u` field - or `0x100`.
    */
  private val removeCodec: Codec[SquadInfo] = uint(3).exmap[SquadInfo](
    _ => Attempt.successful(SquadInfo.Blank),
    _ => Attempt.successful(4)
  )

  /**
    * `Codec` for failing to determine a valid `Codec` based on the entry data.
    * This `Codec` is an invalid codec that does not read any bit data.
    * The `conditional` will always return `None` because
    * its determining conditional statement is explicitly `false`
    * and all cases involving explicit failure.
    */
  private val failureCodec: Codec[SquadInfo] = conditional(included = false, bool).exmap[SquadInfo](
    _ => Attempt.failure(Err("decoding with unhandled codec")),
    _ => Attempt.failure(Err("encoding with unhandled codec"))
  )

  /**
    * An internal class that assists in the process of transforming squad data
    * encoded in the "update" format of a `ReplicationStreamMessage` packet as index-coded fields
    * into a singular decoded `SquadInfo` object that is populated with all of the previously-discovered fields.
    * @param code the field code for the current data
    * @param info the current squad data
    * @param next a potential next encoded squad field
    */
  private final case class LinkedSquadInfo(code: Int, info: SquadInfo, next: Option[LinkedSquadInfo])

  /**
    * Concatenate a `SquadInfo` object chain into a single `SquadInfo` object.
    * @param info the chain
    * @return the concatenated `SquadInfo` object
    */
  private def unlinkSquadInfo(info: LinkedSquadInfo): SquadInfo = unlinkSquadInfo(Some(info))

  /**
    * Concatenate a `SquadInfo` object chain into a single `SquadInfo` object.
    * Recursively visits every link in a `SquadInfo` object chain.
    * @param info the current link in the chain
    * @param squadInfo the persistent `SquadInfo` concatenation object;
    *                  defaults to `SquadInfo.Blank`
    * @return the concatenated `SquadInfo` object
    */
  @tailrec
  private def unlinkSquadInfo(info: Option[LinkedSquadInfo], squadInfo: SquadInfo = SquadInfo.Blank): SquadInfo = {
    info match {
      case None =>
        squadInfo
      case Some(sqInfo) =>
        unlinkSquadInfo(sqInfo.next, squadInfo And sqInfo.info)
    }
  }

  /**
    * Decompose a single `SquadInfo` object into a `SquadInfo` object chain of the original's fields.
    * The fields as a linked list are explicitly organized "leader", "task", "zone_id", "size", "capacity,"
    * or as "(leader, (task, (zone_id, (size, (capacity, None)))))" when fully populated and composed.
    * @param info a `SquadInfo` object that has all relevant fields populated
    * @return a linked list of `SquadInfo` objects, each with a single field from the input `SquadInfo` object
    */
  private def linkSquadInfo(info: SquadInfo): LinkedSquadInfo = {
    //import scala.collection.immutable.::
    Seq(
      (SquadInfo.Field.Capacity, SquadInfo(None, None, None, None, info.capacity)),
      (SquadInfo.Field.Size, SquadInfo(None, None, None, info.size, None)),
      (SquadInfo.Field.ZoneId, SquadInfo(None, None, info.zone_id, None, None)),
      (SquadInfo.Field.Task, SquadInfo(None, info.task, None, None, None)),
      (SquadInfo.Field.Leader, SquadInfo(info.leader, None, None, None, None))
    ) //in reverse order so that the linked list is in the correct order
      .filterNot { case (_, sqInfo) => sqInfo == SquadInfo.Blank } match {
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

  /**
    * Decompose a single `SquadInfo` object into a `SquadInfo` object chain of the original's fields.
    * The fields as a linked list are explicitly organized "leader", "task", "zone_id", "size", "capacity,"
    * or as "(leader, (task, (zone_id, (size, (capacity, None)))))" when fully populated and composed.
    * @param infoList a series of paired field codes and `SquadInfo` objects with data in the indicated fields
    * @return a linked list of `SquadInfo` objects, each with a single field from the input `SquadInfo` object
    */
  @tailrec
  private def linkSquadInfo(infoList: Seq[(Int, SquadInfo)], linkedInfo: LinkedSquadInfo): LinkedSquadInfo = {
    if (infoList.isEmpty) {
      linkedInfo
    } else {
      val (code, data) = infoList.head
      linkSquadInfo(infoList.tail, LinkedSquadInfo(code, data, Some(linkedInfo)))
    }
  }

  /**
    * Parse a known number of knowable format data fields for squad info
    * until no more fields are left for parsing.
    * @see `selectCodecAction`
    * @see `modifyPadValue`
    * @param size the total number of data fields to parse
    * @param pad the current overflow/padding value
    * @return a `LinkedSquadInfo` `Codec` object (linked list)
    */
  private def listing_codec(size: Int, pad: Int = 1): Codec[LinkedSquadInfo] = {
    import shapeless.::
    (
      uint4 >>:~ { code =>
        selectCodecAction(code, pad) ::
          conditional(size - 1 > 0, listing_codec(size - 1, (pad + modifyPadValue(code, pad)) % 8))
      }
    ).xmap[LinkedSquadInfo](
      {
        case code :: entry :: next :: HNil =>
          LinkedSquadInfo(code, entry, next)
      },
      {
        case LinkedSquadInfo(code, entry, next) =>
          code :: entry :: next :: HNil
      }
    )
  }

  /**
    * Given the field code value of the current `SquadInfo` object's data,
    * select the `Codec` object that is most suitable to parse that data.
    * @param code the field code number
    * @param pad  the current overflow/padding value;
    *             the number of bits past the previous byte-aligned index;
    *             should be a 0-7 number that gets converted to a 1-7 string padding number
    * @return a `Codec` object for the specific field's data
    */
  private def selectCodecAction(code: Int, pad: Int): Codec[SquadInfo] = {
    code match {
      case SquadInfo.Field.Leader   => leaderCodec(pad)
      case SquadInfo.Field.Task     => taskCodec(pad)
      case SquadInfo.Field.ZoneId   => zoneIdCodec
      case SquadInfo.Field.Size     => sizeCodec
      case SquadInfo.Field.Capacity => capacityCodec
      case _                        => failureCodec
    }
  }

  /**
    * Given the field code value of the current `SquadInfo` object's data,
    * determine how the inherited overflow/padding value for string data should be adjusted for future entries.
    * There are three paths: becomes zero, doesn't change, or increases by four units.
    * The invalid condition leads to an extremely negative number, but this condition should also never be encountered.
    * @param code the field code number
    * @param pad  the current overflow/padding value;
    *             the number of bits past the previous byte-aligned index;
    *             should be a 0-7 number that gets converted to a 1-7 string padding number
    * @return the number of units that the current overflow/padding value should be modified, in terms of addition
    */
  private def modifyPadValue(code: Int, pad: Int): Int = {
    code match {
      case SquadInfo.Field.Leader   => -pad         //byte-aligned string; padding zero'd
      case SquadInfo.Field.Task     => -pad         //byte-aligned string; padding zero'd
      case SquadInfo.Field.ZoneId   => 4            //4u + 32u = 4u + 8*4u = additional 4u
      case SquadInfo.Field.Size     => 0            //4u + 4u = no change to padding
      case SquadInfo.Field.Capacity => 0            //4u + 4u = no change to padding
      case _                        => Int.MinValue //wildly incorrect
    }
  }

  /**
    * The framework for transforming data from squad listing entries.
    * Three paths lead from this position:
    * processing the data in the course of an entry removal action,
    * processing the data in the course of a total squad listing initialization action, and
    * processing the data of a single entry in a piecemeal fashion that parses a coded field-by-field format.
    * For the second - initialization - another `Codec` object is utilized to determine how the data should be interpreted.
    * @param providedCodec the `Codec` for processing a `SquadInfo` object during the squad list initialization process
    * @return a `SquadListing` `Codec` object
    */
  private def meta_codec(providedCodec: Codec[SquadInfo]): Codec[Option[SquadInfo]] = {
    import shapeless.::
    (bool >>:~ { unk1 =>
      uint8 >>:~ { unk2 =>
        conditional(!unk1 && unk2 == 1, removeCodec) ::
          conditional(unk1 && unk2 == 6, providedCodec) ::
          conditional(unk1 && unk2 != 6, listing_codec(unk2))
      }
    }).exmap[Option[SquadInfo]](
      {
        case false :: 1 :: Some(SquadInfo.Blank) :: None :: None :: HNil => //'remove' case
          Attempt.Successful(None)

        case true :: 6 :: None :: Some(info) :: None :: HNil => //handle complete squad info; no field codes
          Attempt.Successful(Some(info))

        case true :: _ :: None :: None :: Some(result) :: HNil => //iterable field codes
          Attempt.Successful(Some(unlinkSquadInfo(result)))

        case data => //error
          Attempt.failure(Err(s"$data can not be encoded as a squad header"))
      },
      {
        case None => //'remove' case
          Attempt.Successful(false :: 1 :: Some(SquadInfo.Blank) :: None :: None :: HNil)

        case info @ Some(
              SquadInfo(Some(_), Some(_), Some(_), Some(_), Some(_), Some(_))
            ) => //handle complete squad info; no field codes
          Attempt.Successful(true :: 6 :: None :: info :: None :: HNil)

        case Some(info) => //iterable field codes
          val linkedInfo = linkSquadInfo(info)
          var count      = 1
          var linkNext   = linkedInfo.next
          while (linkNext.nonEmpty) {
            count += 1
            linkNext = linkNext.get.next
          }
          Attempt.Successful(true :: count :: None :: None :: Some(linkSquadInfo(info)) :: HNil)

        case data => //error
          Attempt.failure(Err(s"$data can not be decoded into a squad header"))
      }
    )
  }

  /**
    * `Codec` for standard `SquadHeader` entries.
    */
  val codec: Codec[Option[SquadInfo]] = meta_codec(allCodec)

  /**
    * `Codec` for types of `SquadHeader` initializations.
    */
  val info_codec: Codec[Option[SquadInfo]] = meta_codec(infoCodec)

  /**
    * Alternate `Codec` for types of `SquadHeader` initializations.
    */
  val alt_info_codec: Codec[Option[SquadInfo]] = meta_codec(alt_infoCodec)
}

object SquadListing {
  import shapeless.::

  /**
    * Overloaded constructor for guaranteed squad information.
    * @param index the listing entry index for this squad
    * @param info the squad data
    * @return a `SquadListing` object
    */
  def apply(index: Int, info: SquadInfo): SquadListing = {
    SquadListing(index, Some(info))
  }

  /**
    * The framework for transforming data from squad listing entries.
    * @param entryFunc the `Codec` for processing a given `SquadListing` object
    * @return a `SquadListing` `Codec` object
    */
  private def meta_codec(entryFunc: Int => Codec[Option[SquadInfo]]): Codec[SquadListing] =
    (("index" | uint8L) >>:~ { index =>
      conditional(index < 255, "listing" | entryFunc(index)) ::
        conditional(
          index == 255,
          bits
        ) //consume n < 8 bits after the tail entry, else vector will try to operate on invalid data
    }).xmap[SquadListing](
      {
        case ndx :: Some(lstng) :: _ :: HNil =>
          SquadListing(ndx, lstng)
        case ndx :: None :: _ :: HNil =>
          SquadListing(ndx, None)
      },
      {
        case SquadListing(ndx, lstng) =>
          ndx :: Some(lstng) :: None :: HNil
      }
    )

  /**
    * `Codec` for standard `SquadListing` entries.
    */
  val codec: Codec[SquadListing] = meta_codec({ _ => SquadHeader.codec })

  /**
    * `Codec` for branching types of `SquadListing` initializations.
    */
  val info_codec: Codec[SquadListing] = meta_codec({ index: Int =>
    newcodecs.binary_choice(index == 0, "listing" | SquadHeader.info_codec, "listing" | SquadHeader.alt_info_codec)
  })
}

object ReplicationStreamMessage extends Marshallable[ReplicationStreamMessage] {
  import shapeless.::

  /**
    * A shortcut for the squad list initialization format of the packet.
    * Supplied squad data is given a zero-indexed counter and transformed into formal "`Listing`s" for processing.
    * @param infos the squad data to be composed into formal list entries
    * @return a `ReplicationStreamMessage` packet object
    */
  def apply(infos: Iterable[SquadInfo]): ReplicationStreamMessage = {
    ReplicationStreamMessage(
      5,
      Some(6),
      infos.zipWithIndex.map { case (info, index) => SquadListing(index, Some(info)) }.toVector
    )
  }

  implicit val codec: Codec[ReplicationStreamMessage] = (("behavior" | uintL(3)) >>:~ { behavior =>
    conditional(behavior == 5, "behavior2" | uintL(3)) ::
      conditional(behavior != 1, bool) ::
      newcodecs.binary_choice(
        behavior != 5,
        "entries" | vector(SquadListing.codec),
        "entries" | vector(SquadListing.info_codec)
      )
  }).xmap[ReplicationStreamMessage](
    {
      case bhvr :: bhvr2 :: _ :: lst :: HNil =>
        ReplicationStreamMessage(bhvr, bhvr2, ignoreTerminatingEntry(lst))
    },
    {
      case ReplicationStreamMessage(1, bhvr2, lst) =>
        1 :: bhvr2 :: None :: ensureTerminatingEntry(lst) :: HNil
      case ReplicationStreamMessage(bhvr, bhvr2, lst) =>
        bhvr :: bhvr2 :: Some(false) :: ensureTerminatingEntry(lst) :: HNil
    }
  )

  /**
    * The last entry in the sequence of squad information listings should be a dummied listing with an index of 255.
    * Ensure that this terminal entry is located at the end.
    * @param list the listing of squad information
    * @return the listing of squad information, with a specific final entry
    */
  private def ensureTerminatingEntry(list: Vector[SquadListing]): Vector[SquadListing] = {
    list.lastOption match {
      case Some(SquadListing(255, _)) => list
      case Some(_) | None             => list :+ SquadListing()
    }
  }

  /**
    * The last entry in the sequence of squad information listings should be a dummied listing with an index of 255.
    * Remove this terminal entry from the end of the list so as not to hassle with it.
    * @param list the listing of squad information
    * @return the listing of squad information, with a specific final entry truncated
    */
  private def ignoreTerminatingEntry(list: Vector[SquadListing]): Vector[SquadListing] = {
    list.lastOption match {
      case Some(SquadListing(255, _)) => list.init
      case Some(_) | None             => list
    }
  }
}

/*
                         +-> SquadListing.codec --------> SquadHeader.codec ----------+
                         |                                                            |
                         |                                                            |
ReplicationStream.codec -+                                                            |
                         |                                                            |
                         |                            +-> SquadHeader.info_codec -----+-> SquadInfo
                         |                            |                               |
                         +-> SquadListing.info_codec -+                               |
                                                      |                               |
                                                      +-> SquadHeader.alt_info_codec -+
 */
