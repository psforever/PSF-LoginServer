// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless._

/**
  * Maintains squad information changes performed by this listing.
  * Only certain information will be transmitted depending on the purpose of the packet.
  * @param leader the name of the squad leader as a wide character string, or `None` if not applicable
  * @param task the task the squad is trying to perform as a wide character string, or `None` if not applicable
  * @param continent_guid the continent on which the squad is acting, or `None` if not applicable;
  *                       this GUID is transmitted as a 32-bit number;
  *                       the internal GUID is 16-bit, so the last 16 bits of this field can be ignored
  * @param size the current size of the squad, or `None` if not applicable;
  *             "can" be greater than `capacity`, though with issues
  * @param capacity the maximum number of members that the squad can tolerate, or `None` if not applicable;
  *                 normally is 10;
  *                 maximum is 15
  * @param squad_guid a GUID associated with the squad, used to recover the squad definition, or `None` if not applicable;
  *                   sometimes it is defined but is still not applicable
  */
final case class SquadInfo(leader : Option[String],
                           task : Option[String],
                           continent_guid : Option[PlanetSideGUID],
                           size : Option[Int],
                           capacity : Option[Int],
                           squad_guid : Option[PlanetSideGUID] = None)

/**
  * Define three fields determining the purpose of data in this listing.<br>
  * <br>
  * The third field `unk3` is not always be defined and will be supplanted by the squad (definition) GUID during initialization and a full update.<br>
  * <br>
  * Actions:<br>
  * (`unk1`, `unk2`, `unk3`)<br>
  * - `0, true, 4 -- 0x00C -- `Remove a squad from listing<br>
  * - `128, true, 0 -- 0x808 -- `Update a squad's leader<br>
  * - `128, true, 1 -- 0x809 -- `Update a squad's task or continent<br>
  * - `128, true, 2 -- 0x80A -- `Update a squad's size<br>
  * - `129, false, 0 -- 0x810 -- `Update a squad's leader or size<br>
  * - `129, false, 1 -- 0x811 -- `Update a squad's task and continent<br>
  * - `131, false, X -- 0x830 -- `Add all squads during initialization / update all information pertaining to this squad
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na; not always defined
  * @param info information pertaining to this squad listing
  */
//TODO when these unk# values are better understood, transform SquadHeader to streamline the actions to be performed
final case class SquadHeader(unk1 : Int,
                             unk2 : Boolean,
                             unk3 : Option[Int],
                             info : Option[SquadInfo] = None)

/**
  * An entry in the listing of squads.
  * The entries are loaded into a `Vector` externally.<br>
  * <br>
  * Squad listing indicies are not an arbitrary order.
  * The server communicates changes to the client by referencing a squad's listing index, defined at the time of list initialization.
  * Once initialized, each client may organize their squads however they wish, e.g., by leader, by task, etc., without compromising this index.
  * During the list initialization process, the entries must always follow numerical order, increasing from `0`.
  * During any other operation, the entries may be prefixed with whichever index is necessary to indicate the squad listing in question.<br>
  * <br>
  * Index 255 (`0xFF` or `0x11111111`) is a special entry that signifies the end of the stream.
  * There are no more listings after this entry and nothing can happen in that entry.
  * @param index the index of this listing
  * @param listing the data for this entry, defining both the actions and the pertinent squad information
  * @param na collects runoff bits that pad the end of the stream that may disrupt the decoding process;
  *           can (and should) be ignored by the user
  */
final case class SquadListing(index : Int = 255,
                              listing : Option[SquadHeader] = None,
                              na : Option[BitVector] = None)

/**
  * Modify the list of squads visible to a given player.
  * The squad list updates in real time rather than just whenever a player opens the squad information window.<br>
  * <br>
  * The four main operations are: initializing the list, clearing the list, updating entries in the list, and removing entries from the list.
  * The process of initializing the list and clearing the list actually are performed by the same behavior.
  * Squads would just not added after the behavior clears the list.
  * Moreover, removing entries from the list overrides the behavior to update entries in the list.
  * The two-three per-entry codes (see SquadHeader) are more important for determining the effect of a specific entry than these behaviors.
  * As of the moment, the important details of the behaviors is that they modify how the packet is encoded and padded.<br>
  * <br>
  * Entries are identified by their index in the squad list.
  * This is followed by a coded section that indicates what action the entry will execute on that squad listing.
  * After the "coded acton" section is the "codec information" section where the data for the change for the given squad listing is specified.
  * In this manner, all the entries will have a knowable length.<br>
  * <br>
  * The total number of entries in a packet is not known until they have all been parsed.
  * During the list initialization process, the entries must be in ascending order of index.
  * Otherwise, the specific index of the squad listing is referenced.
  * The minimum number of entries is "no entries."
  * The maximum number of entries is supposedly 254, but is at least a number below 32.
  * The last item in the stream is the number 255 and this is interpretted as a concluding 255th entry that does nothing.<br>
  * <br>
  * Packet length may/will limit the number of squads that can be loaded in a single session.
  * If the packet is too long in the sense of byte-length, no updates will be made to an existing squad list nor will a new squad list be initialized.
  * That packet will be discarded by the client.
  * The presence of and length of string data is the primary variability when it comes to packet length.<br>
  * <br>
  * Behaviors:<br>
  * (`behavior`, `behavior2`, `unk`)<br>
  * - `1, X, X     -- 0x20 -- `Update where initial entry removes a squad from the list<br>
  * - `5, 6, false -- 0xB8 -- `Clear squad list and initialize new squad list<br>
  * - `5, 6, false -- 0xB9 -- `Clear squad list (actually is a `0xB8` that transitions directly into 255-entry)<br>
  * - `6, X, false -- 0xC0 -- `Update a squad in the list<br>
  * <br>
  * Exploration:<br>
  * This logic contains a lot of holes.
  * It is consistent for known packets, however.
  * @param behavior a code that suggests the primary purpose of the data in this packet
  * @param behavior2 during initialization, this code is read;
  *                  it supplements the normal `behavior` and is typically is an "update" code
  * @param unk a spacer byte between the `behavior` code(s) and the start of the first entry;
  *            should be set to `false` when it exists
  * @param entries a vector of the squad listings
  */
final case class ReplicationStreamMessage(behavior : Int,
                                          behavior2 : Option[Int],
                                          unk : Option[Boolean],
                                          entries : Vector[SquadListing])
  extends PlanetSideGamePacket {
  type Packet = ReplicationStreamMessage
  def opcode = GamePacketOpcode.ReplicationStreamMessage
  def encode = ReplicationStreamMessage.encode(this)
}

object SquadInfo {
  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the fields.<br>
    * <br>
    * This constructor is not actually used at the moment.
    * @param leader the name of the squad leader
    * @param task the task the squad is trying to perform
    * @param continent_guid the continent on which the squad is acting
    * @param size the current size of the squad
    * @param capacity the maximum number of members that the squad can tolerate
    * @return a SquadInfo object
    */
  def apply(leader : String, task : String, continent_guid : PlanetSideGUID, size : Int, capacity : Int) : SquadInfo = {
    SquadInfo(Some(leader), Some(task), Some(continent_guid), Some(size), Some(capacity))
  }

  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the fields.<br>
    * <br>
    * This constructor is used by the `initCodec`, `alt_initCodec`, and `allCodec`.
    * @param leader the name of the squad leader
    * @param task the task the squad is trying to perform
    * @param continent_guid the continent on which the squad is acting
    * @param size the current size of the squad
    * @param capacity the maximum number of members that the squad can tolerate
    * @param squad_guid a GUID associated with the squad, used to recover the squad definition
    * @return a SquadInfo object
    */
  def apply(leader : String, task : String, continent_guid : PlanetSideGUID, size : Int, capacity : Int, squad_guid : PlanetSideGUID) : SquadInfo = {
    SquadInfo(Some(leader), Some(task), Some(continent_guid), Some(size), Some(capacity), Some(squad_guid))
  }

  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the important field.<br>
    * <br>
    * This constructor is used by `leaderCodec`.
    * Two of the fields normally are `Option[String]`s.
    * Only the `leader` field in this packet is a `String`, giving the method a distinct signature.
    * The other field - an `Option[String]` for `task` - can still be set if passed.<br>
    * <br>
    * Recommended use: `SquadInfo(leader, None)`
    * @param leader the name of the squad leader
    * @param task the task the squad is trying to perform, if not `None`
    * @return a SquadInfo object
    */
  def apply(leader : String, task : Option[String]) : SquadInfo = {
    SquadInfo(Some(leader), task, None, None, None)
  }

  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the important field.<br>
    * <br>
    * This constructor is used by `taskOrContinentCodec`.
    * Two of the fields normally are `Option[String]`s.
    * Only the `task` field in this packet is a `String`, giving the method a distinct signature.
    * The other field - an `Option[String]` for `leader` - can still be set if passed.<br>
    * <br>
    * Recommended use: `SquadInfo(None, task)`
    * @param leader the name of the squad leader, if not `None`
    * @param task the task the squad is trying to perform
    * @return a SquadInfo object
    */
  def apply(leader : Option[String], task : String) : SquadInfo = {
    SquadInfo(leader, Some(task), None, None, None)
  }

  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the field.<br>
    * <br>
    * This constructor is used by `taskOrContinentCodec`.
    * @param continent_guid the continent on which the squad is acting
    * @return a SquadInfo object
    */
  def apply(continent_guid : PlanetSideGUID) : SquadInfo = {
    SquadInfo(None, None, Some(continent_guid), None, None)
  }

  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the important field.<br>
    * <br>
    * This constructor is used by `sizeCodec`.
    * Two of the fields normally are `Option[Int]`s.
    * Only the `size` field in this packet is an `Int`, giving the method a distinct signature.<br>
    * <br>
    * Recommended use: `SquadInfo(size, None)`<br>
    * <br>
    * Exploration:<br>
    * We do not currently know any `SquadHeader` action codes for adjusting `capacity`.
    * @param size the current size of the squad
    * @param capacity the maximum number of members that the squad can tolerate, if not `None`
    * @return a SquadInfo object
    */
  def apply(size : Int, capacity : Option[Int]) : SquadInfo = {
    SquadInfo(None, None, None, Some(size), None)
  }

  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the important field.<br>
    * <br>
    * This constructor is not actually used at the moment.
    * Two of the fields normally are `Option[Int]`s.
    * Only the `capacity` field in this packet is an `Int`, giving the method a distinct signature.<br>
    * <br>
    * Recommended use: `SquadInfo(None, capacity)`<br>
    * <br>
    * Exploration:<br>
    * We do not currently know any `SquadHeader` action codes for adjusting `capacity`.
    * @param size the current size of the squad
    * @param capacity the maximum number of members that the squad can tolerate, if not `None`
    * @return a SquadInfo object
    */
  def apply(size : Option[Int], capacity : Int) : SquadInfo = {
    SquadInfo(None, None, None, None, Some(capacity))
  }

  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the fields.<br>
    * <br>
    * This constructor is used by `leaderSizeCodec`.
    * @param leader the name of the squad leader
    * @param size the current size of the squad
    * @return a SquadInfo object
    */
  def apply(leader : String, size : Int) : SquadInfo = {
    SquadInfo(Some(leader), None, None, Some(size), None)
  }

  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the fields.<br>
    * <br>
    * This constructor is used by `taskAndContinentCodec`.
    * @param task the task the squad is trying to perform
    * @param continent_guid the continent on which the squad is acting
    * @return a SquadInfo object
    */
  def apply(task : String, continent_guid : PlanetSideGUID) : SquadInfo = {
    SquadInfo(None, Some(task), Some(continent_guid), None, None, None)
  }
}

object SquadHeader extends Marshallable[SquadHeader] {
  /**
    * `squadPattern` completes the fields for the `SquadHeader` class.
    * It translates an indeterminate number of bit regions into something that can be processed as an `Option[SquadInfo]`.
    */
  private type squadPattern = Option[SquadInfo] :: HNil
  /**
    * Codec for reading `SquadInfo` data from the first entry from a packet with squad list initialization entries.
    */
  private val initCodec : Codec[squadPattern] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideString) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      uint16L ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).exmap[squadPattern] (
    {
      case sguid :: lead :: tsk :: cguid :: 0 :: sz :: cap :: HNil =>
        Attempt.successful(Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for adding [A] a squad entry"))
    },
    {
      case Some(SquadInfo(Some(lead), Some(tsk), Some(cguid), Some(sz), Some(cap), Some(sguid))) :: HNil =>
        Attempt.successful(sguid :: lead :: tsk :: cguid :: 0 :: sz :: cap :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for adding [A] a squad entry"))
    }
  )

  /**
    * Codec for reading `SquadInfo` data from all entries other than the first from a packet with squad list initialization entries.
    */
  private val alt_initCodec : Codec[squadPattern] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      uint16L ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).exmap[squadPattern] (
    {
      case sguid :: lead :: tsk :: cguid :: 0 :: sz :: cap :: HNil =>
        Attempt.successful(Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for adding [B] a squad entry"))
    },
    {
      case Some(SquadInfo(Some(lead), Some(tsk), Some(cguid), Some(sz), Some(cap), Some(sguid))) :: HNil =>
        Attempt.successful(sguid :: lead :: tsk :: cguid :: 0 :: sz :: cap :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for adding [B] a squad entry"))
    }
  )

  /**
    * Codec for reading the `SquadInfo` data in an "update all squad data" entry.
    */
  private val allCodec : Codec[squadPattern] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideStringAligned(3)) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      uint16L ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).exmap[squadPattern] (
    {
      case sguid :: lead :: tsk :: cguid :: 0 :: sz :: cap :: HNil =>
        Attempt.successful(Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for updating a squad entry"))
    },
    {
      case Some(SquadInfo(Some(lead), Some(tsk), Some(cguid), Some(sz), Some(cap), Some(sguid))) :: HNil =>
        Attempt.successful(sguid :: lead :: tsk :: cguid :: 0 :: sz :: cap :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for updating a squad entry"))
    }
  )

  /**
    * Codec for reading the `SquadInfo` data in an "update squad leader" entry.
    */
  private val leaderCodec : Codec[squadPattern] = (
    bool ::
      ("leader" | PacketHelpers.encodedWideStringAligned(7))
    ).exmap[squadPattern] (
    {
      case true :: lead :: HNil =>
        Attempt.successful(Some(SquadInfo(lead, None)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for a leader name"))
    },
    {
      case Some(SquadInfo(Some(lead), _, _, _, _, _)) :: HNil =>
        Attempt.successful(true :: lead :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for a leader name"))
    }
  )

  /**
    * Codec for reading the `SquadInfo` data in an "update squad task or continent" entry.
    */
  private val taskOrContinentCodec : Codec[squadPattern] = (
    bool >>:~ { path =>
      conditional(path, "continent_guid" | PlanetSideGUID.codec) ::
        conditional(path, uint16L) ::
        conditional(!path, "task" | PacketHelpers.encodedWideStringAligned(7))
    }
    ).exmap[squadPattern] (
    {
      case true :: Some(cguid) :: Some(0) :: _ :: HNil =>
        Attempt.successful(Some(SquadInfo(cguid)) :: HNil)
      case true :: Some(cguid) :: Some(_) :: _ :: HNil =>
        Attempt.failure(Err("failed to decode squad data for a continent - malformed GUID"))
      case false :: _ :: _ :: Some(tsk) :: HNil =>
        Attempt.successful(Some(SquadInfo(None, tsk)) :: HNil)
      case false :: _ :: _ :: None :: HNil =>
        Attempt.failure(Err("failed to decode squad data for a task - no task"))
    },
    {
      case Some(SquadInfo(_, None, Some(cguid), _, _, _)) :: HNil =>
        Attempt.successful(true :: Some(cguid) :: Some(0) :: None :: HNil)
      case Some(SquadInfo(_, Some(tsk), None, _, _, _)) :: HNil =>
        Attempt.successful(false :: None :: None :: Some(tsk) :: HNil)
      case Some(SquadInfo(_, Some(tsk), Some(cguid), _, _, _)) :: HNil =>
        Attempt.failure(Err("failed to encode squad data for either a task or a continent - multiple encodings reachable"))
      case _ =>
        Attempt.failure(Err("failed to encode squad data for either a task or a continent"))
    }
  )

  /**
    * Codec for reading the `SquadInfo` data in an "update squad size" entry.
    */
  private val sizeCodec : Codec[squadPattern] = (
    bool ::
      ("size" | uint4L)
    ).exmap[squadPattern] (
    {
      case false :: sz :: HNil =>
        Attempt.successful(Some(SquadInfo(sz, None)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for a size"))
    },
    {
      case Some(SquadInfo(_, _, _, Some(sz), _, _)) :: HNil =>
        Attempt.successful(false :: sz :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for a size"))
    }
  )

  /**
    * Codec for reading the `SquadInfo` data in an "update squad leader and size" entry.
    */
  private val leaderSizeCodec : Codec[squadPattern] = (
    bool ::
      ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
      uint4L ::
      ("size" | uint4L)
    ).exmap[squadPattern] (
    {
      case true :: lead :: 4 :: sz :: HNil =>
        Attempt.successful(Some(SquadInfo(lead, sz)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for a leader and a size"))
    },
    {
      case Some(SquadInfo(Some(lead), _, _, Some(sz), _, _)) :: HNil =>
        Attempt.successful(true :: lead :: 4 :: sz :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for a leader and a size"))
    }
  )

  /**
    * Codec for reading the `SquadInfo` data in an "update squad task and continent" entry.
    */
  private val taskAndContinentCodec : Codec[squadPattern] = (
    bool ::
      ("task" | PacketHelpers.encodedWideStringAligned(7)) ::
      uintL(3) ::
      bool ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      uint16L
    ).exmap[squadPattern] (
    {
      case false :: tsk :: 1 :: true :: cguid :: 0 :: HNil =>
        Attempt.successful(Some(SquadInfo(tsk, cguid)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for a task and a continent"))
    },
    {
      case Some(SquadInfo(_, Some(tsk), Some(cguid), _, _, _)) :: HNil =>
        Attempt.successful(false :: tsk :: 1 :: true :: cguid :: 0 :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for a task and a continent"))
    }
  )

  /**
    * Codec for reading the `SquadInfo` data in a "remove squad from list" entry.
    * This codec is unique because it is considered a valid codec that does not read any bit data.
    * The `conditional` will always return `None` because its determining conditional statement is explicitly `false`.
    */
  private val removeCodec : Codec[squadPattern] = conditional(false, bool).exmap[squadPattern] (
    {
      case None | _ =>
        Attempt.successful(None :: HNil)
    },
    {
      case None :: HNil | _ =>
        Attempt.successful(None)
    }
  )

  /**
    * Codec for failing to determine a valid codec based on the entry data.
    * This codec is an invalid codec that does not read any bit data.
    * The `conditional` will always return `None` because its determining conditional statement is explicitly `false`.
    */
  val failureCodec : Codec[squadPattern] = conditional(false, bool).exmap[squadPattern] (
    {
      case None | _ =>
        Attempt.failure(Err("decoding with unhandled codec"))
    },
    {
      case None :: HNil | _ =>
        Attempt.failure(Err("encoding with unhandled codec"))
    }
  )

  /**
    * Alternate constructor for SquadInfo that ignores the Option requirement for the `info` field.
    * @param unk1 na
    * @param unk2 na
    * @param unk3 na; not always defined
    * @param info information pertaining to this squad listing
    */
  def apply(unk1 : Int, unk2 : Boolean, unk3 : Option[Int], info : SquadInfo) : SquadHeader = {
    SquadHeader(unk1, unk2, unk3, Some(info))
  }

  implicit val codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) >>:~ { unk1 =>
      ("unk2" | bool) >>:~ { unk2 =>
        conditional(unk1 != 131, "unk3" | uintL(3)) >>:~ { unk3 =>
          selectCodec(unk1, unk2, unk3, allCodec)
        }
      }
    }
    ).as[SquadHeader]

  implicit val init_codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) >>:~ { unk1 =>
      ("unk2" | bool) >>:~ { unk2 =>
        conditional(unk1 != 131, "unk3" | uintL(3)) >>:~ { unk3 =>
          selectCodec(unk1, unk2, unk3, initCodec)
        }
      }
    }
    ).as[SquadHeader]

  implicit val alt_init_codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) >>:~ { unk1 =>
      ("unk2" | bool) >>:~ { unk2 =>
        conditional(unk1 != 131, "unk3" | uintL(3)) >>:~ { unk3 =>
          selectCodec(unk1, unk2, unk3, alt_initCodec)
        }
      }
    }
    ).as[SquadHeader]

  /**
    * Select the codec to translate bit data in this packet into an `Option[SquadInfo]` using other fields of the same packet.
    * Refer to comments for the primary `case class` constructor for `SquadHeader` to explain how the conditions in this function path.
    * @param a na
    * @param b na
    * @param c na; may be `None`
    * @param optionalCodec a to-be-defined codec that is determined by the suggested mood of the packet and listing of the squad;
    *                      despite the name, actually a required parameter
    * @return a codec that corresponds to a `squadPattern` translation
    */
  private def selectCodec(a : Int, b : Boolean, c : Option[Int], optionalCodec : Codec[squadPattern]) : Codec[squadPattern] = {
    if(c.isDefined) {
      val cVal = c.get
      if(a == 0 && b)
        if(cVal == 4)
          return removeCodec
      if(a == 128 && b) {
        if(cVal == 0)
          return leaderCodec
        else if(cVal == 1)
          return taskOrContinentCodec
        else if(cVal == 2)
          return sizeCodec
      }
      else if(a == 129 && !b) {
        if(cVal == 0)
          return leaderSizeCodec
        else if(cVal == 1)
          return taskAndContinentCodec
      }
    }
    else {
      if(a == 131 && !b)
        return optionalCodec
    }
    //we've not encountered a valid codec
    failureCodec
  }
}

object SquadListing extends Marshallable[SquadListing] {
  implicit val codec : Codec[SquadListing] = (
    ("index" | uint8L) >>:~ { index =>
      conditional(index < 255, "listing" | SquadHeader.codec) ::
        conditional(index == 255, bits) //consume n < 8 bits padding the tail entry, else vector will try to operate on invalid data
    }).as[SquadListing]

  implicit val init_codec : Codec[SquadListing] = (
    ("index" | uint8L) >>:~ { index =>
      conditional(index < 255,
        newcodecs.binary_choice(index == 0,
          "listing" | SquadHeader.init_codec,
          "listing" | SquadHeader.alt_init_codec)
      ) ::
        conditional(index == 255, bits) //consume n < 8 bits padding the tail entry, else vector will try to operate on invalid data
    }).as[SquadListing]
}

object ReplicationStreamMessage extends Marshallable[ReplicationStreamMessage] {
  //vector is a greedy unsized codec that will consume data until there is no data left in the stream
  //vector will attempt to parse the amount of data necessary to translate sequential elements using the supplied codec
  //an unexpected end of stream while parsing for an element will cause the packet to fail to decode
  implicit val codec : Codec[ReplicationStreamMessage] = (
    ("behavior" | uintL(3)) >>:~ { behavior =>
      conditional(behavior == 5, "behavior2" | uintL(3)) ::
        conditional(behavior != 1, "unk" | bool) ::
        newcodecs.binary_choice(behavior != 5,
          "entries" | vector(SquadListing.codec),
          "entries" | vector(SquadListing.init_codec)
        )
    }
    ).as[ReplicationStreamMessage]
}
