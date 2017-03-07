// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Maintains squad information changes performed by this listing.
  * Only certain information will be transmitted depending on the purpose of the packet.
  * @param leader the name of the squad leader as a wide character string, or `None` if not applicable
  * @param task the task the squad is trying to perform as a wide character string, or `None` if not applicable
  * @param zone_id the continent on which the squad is acting, or `None` if not applicable
  * @param size the current size of the squad, or `None` if not applicable;
  *             "can" be greater than `capacity`, though with issues
  * @param capacity the maximum number of members that the squad can tolerate, or `None` if not applicable;
  *                 normal count is 10;
  *                 maximum is 15 but naturally can not be assigned that many
  * @param squad_guid a GUID associated with the squad, used to recover the squad definition, or `None` if not applicable;
  *                   sometimes it is defined but is still not applicable
  */
final case class SquadInfo(leader : Option[String],
                           task : Option[String],
                           zone_id : Option[PlanetSideZoneID],
                           size : Option[Int],
                           capacity : Option[Int],
                           squad_guid : Option[PlanetSideGUID] = None)

/**
  * Define three fields determining the purpose of data in this listing.<br>
  * <br>
  * The third field `unk3` is not always be defined and will be supplanted by the squad (definition) GUID during initialization and a full update.<br>
  * <br>
  * Actions:<br>
  * `unk1&nbsp;unk2&nbsp;&nbsp;unk3`<br>
  * `0&nbsp;&nbsp;&nbsp;&nbsp;true&nbsp;&nbsp;4 -- `Remove a squad from listing<br>
  * `128&nbsp;&nbsp;true&nbsp;&nbsp;0 -- `Update a squad's leader<br>
  * `128&nbsp;&nbsp;true&nbsp;&nbsp;1 -- `Update a squad's task or continent<br>
  * `128&nbsp;&nbsp;true&nbsp;&nbsp;2 -- `Update a squad's size<br>
  * `129&nbsp;&nbsp;false&nbsp;0 -- `Update a squad's leader or size<br>
  * `129&nbsp;&nbsp;false&nbsp;1 -- `Update a squad's task and continent<br>
  * `131&nbsp;&nbsp;false&nbsp;X -- `Add all squads during initialization / update all information pertaining to this squad
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na;
  *             not always defined
  * @param info information pertaining to this squad listing
  */
//TODO when these unk# values are better understood, transform SquadHeader to streamline the actions to be performed
final case class SquadHeader(unk1 : Int,
                             unk2 : Boolean,
                             unk3 : Option[Int],
                             info : Option[SquadInfo] = None)

/**
  * An indexed entry in the listing of squads.<br>
  * <br>
  * Squad listing indices are not an arbitrary order.
  * The server communicates changes to the client by referencing a squad's listing index, defined at the time of list initialization.
  * Once initialized, each client may organize their squads however they wish, e.g., by leader, by task, etc., without compromising this index.
  * During the list initialization process, the entries must always follow numerical order, increasing from `0`.
  * During any other operation, the entries may be prefixed with whichever index is necessary to indicate the squad listing in question.
  * @param index the index of this listing;
  *              first entry should be 0, and subsequent valid entries are sequentially incremental;
  *              last entry is always a placeholder with index 255
  * @param listing the data for this entry, defining both the actions and the pertinent squad information
  */
final case class SquadListing(index : Int = 255,
                              listing : Option[SquadHeader] = None)

/**
  * Modify the list of squads available to a given player.
  * The squad list updates in real time rather than just whenever a player opens the squad information window.<br>
  * <br>
  * The four main operations are: initializing the list, updating entries in the list, removing entries from the list, and clearing the list.
  * The process of initializing the list and clearing the list actually are performed by similar behavior.
  * Squads would just not be added after the list clears.
  * Moreover, removing entries from the list overrides the behavior to update entries in the list.
  * The two-three codes per entry (see `SquadHeader`) are important for determining the effect of a specific entry.
  * As of the moment, the important details of the behaviors is that they modify how the packet is encoded and padded.<br>
  * <br>
  * Referring to information in `SquadListing`, entries are identified by their index in the list.
  * This is followed by a coded section that indicates what action the entry will execute on that squad listing.
  * After the "coded action" section is the "general information" section where the data for the change is specified.
  * In this manner, all the entries will have a knowable length.<br>
  * <br>
  * The total number of entries in a packet is not known until they have all been parsed.
  * During the list initialization process, the entries must be in ascending order of index.
  * Otherwise, the specific index of the squad listing is referenced.
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
  * `5&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;6&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; `Clear squad list (ransitions directly into 255-entry)<br>
  * `6&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;X&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; `Update a squad in the list
  * @param behavior a code that suggests the primary purpose of the data in this packet
  * @param behavior2 during initialization, this code is read;
  *                  it supplements the normal `behavior` and is typically is an "update" code
  * @param entries a `Vector` of the squad listings
  */
final case class ReplicationStreamMessage(behavior : Int,
                                          behavior2 : Option[Int],
                                          entries : Vector[SquadListing])
  extends PlanetSideGamePacket {
  type Packet = ReplicationStreamMessage
  def opcode = GamePacketOpcode.ReplicationStreamMessage
  def encode = ReplicationStreamMessage.encode(this)
}

object SquadInfo {
  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the fields.<br>
    * <br>
    * This constructor is not actually used at the moment.
    * @param leader the name of the squad leader
    * @param task the task the squad is trying to perform
    * @param continent_guid the continent on which the squad is acting
    * @param size the current size of the squad
    * @param capacity the maximum number of members that the squad can tolerate
    * @return a `SquadInfo` object
    */
  def apply(leader : String, task : String, continent_guid : PlanetSideZoneID, size : Int, capacity : Int) : SquadInfo = {
    SquadInfo(Some(leader), Some(task), Some(continent_guid), Some(size), Some(capacity))
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the fields.<br>
    * <br>
    * This constructor is used by the `initCodec`, `alt_initCodec`, and `allCodec`.
    * @param leader the name of the squad leader
    * @param task the task the squad is trying to perform
    * @param continent_guid the continent on which the squad is acting
    * @param size the current size of the squad
    * @param capacity the maximum number of members that the squad can tolerate
    * @param squad_guid a GUID associated with the squad, used to recover the squad definition
    * @return a `SquadInfo` object
    */
  def apply(leader : String, task : String, continent_guid : PlanetSideZoneID, size : Int, capacity : Int, squad_guid : PlanetSideGUID) : SquadInfo = {
    SquadInfo(Some(leader), Some(task), Some(continent_guid), Some(size), Some(capacity), Some(squad_guid))
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the important field.<br>
    * <br>
    * This constructor is used by `leaderCodec`.
    * Two of the fields normally are `Option[String]`s.
    * Only the `leader` field in this packet is a `String`, giving the method a distinct signature.
    * The other field - an `Option[String]` for `task` - can still be set if passed.<br>
    * <br>
    * Recommended use: `SquadInfo(leader, None)`
    * @param leader the name of the squad leader
    * @param task the task the squad is trying to perform, if not `None`
    * @return a `SquadInfo` object
    */
  def apply(leader : String, task : Option[String]) : SquadInfo = {
    SquadInfo(Some(leader), task, None, None, None)
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the important field.<br>
    * <br>
    * This constructor is used by `taskOrContinentCodec`.
    * Two of the fields normally are `Option[String]`s.
    * Only the `task` field in this packet is a `String`, giving the method a distinct signature.
    * The other field - an `Option[String]` for `leader` - can still be set if passed.<br>
    * <br>
    * Recommended use: `SquadInfo(None, task)`
    * @param leader the name of the squad leader, if not `None`
    * @param task the task the squad is trying to perform
    * @return a `SquadInfo` object
    */
  def apply(leader : Option[String], task : String) : SquadInfo = {
    SquadInfo(leader, Some(task), None, None, None)
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the field.<br>
    * <br>
    * This constructor is used by `taskOrContinentCodec`.
    * @param continent_guid the continent on which the squad is acting
    * @return a `SquadInfo` object
    */
  def apply(continent_guid : PlanetSideZoneID) : SquadInfo = {
    SquadInfo(None, None, Some(continent_guid), None, None)
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the important field.<br>
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
    * @return a `SquadInfo` object
    */
  def apply(size : Int, capacity : Option[Int]) : SquadInfo = {
    SquadInfo(None, None, None, Some(size), None)
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the important field.<br>
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
    * @return a `SquadInfo` object
    */
  def apply(size : Option[Int], capacity : Int) : SquadInfo = {
    SquadInfo(None, None, None, None, Some(capacity))
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the `Option` requirement for the fields.<br>
    * <br>
    * This constructor is used by `leaderSizeCodec`.
    * @param leader the name of the squad leader
    * @param size the current size of the squad
    * @return a `SquadInfo` object
    */
  def apply(leader : String, size : Int) : SquadInfo = {
    SquadInfo(Some(leader), None, None, Some(size), None)
  }

  /**
    * Alternate constructor for `SquadInfo` that ignores the Option requirement for the fields.<br>
    * <br>
    * This constructor is used by `taskAndContinentCodec`.
    * @param task the task the squad is trying to perform
    * @param continent_guid the continent on which the squad is acting
    * @return a `SquadInfo` object
    */
  def apply(task : String, continent_guid : PlanetSideZoneID) : SquadInfo = {
    SquadInfo(None, Some(task), Some(continent_guid), None, None, None)
  }
}

object SquadHeader {
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

  /**
    * `squadPattern` completes the fields for the `SquadHeader` class.
    * It translates an indeterminate number of bit regions into something that can be processed as an `Option[SquadInfo]`.
    */
  private type squadPattern = Option[SquadInfo] :: HNil

  /**
    * `Codec` for reading `SquadInfo` data from the first entry from a packet with squad list initialization entries.
    */
  private val initCodec : Codec[squadPattern] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideString) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideZoneID.codec) ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).exmap[squadPattern] (
    {
      case sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil =>
        Attempt.successful(Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for adding [A] a squad entry"))
    },
    {
      case Some(SquadInfo(Some(lead), Some(tsk), Some(cguid), Some(sz), Some(cap), Some(sguid))) :: HNil =>
        Attempt.successful(sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for adding [A] a squad entry"))
    }
  )

  /**
    * `Codec` for reading `SquadInfo` data from all entries other than the first from a packet with squad list initialization entries.
    */
  private val alt_initCodec : Codec[squadPattern] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideZoneID.codec) ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).exmap[squadPattern] (
    {
      case sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil =>
        Attempt.successful(Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for adding [B] a squad entry"))
    },
    {
      case Some(SquadInfo(Some(lead), Some(tsk), Some(cguid), Some(sz), Some(cap), Some(sguid))) :: HNil =>
        Attempt.successful(sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for adding [B] a squad entry"))
    }
  )

  /**
    * `Codec` for reading the `SquadInfo` data in an "update all squad data" entry.
    */
  private val allCodec : Codec[squadPattern] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideStringAligned(3)) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideZoneID.codec) ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).exmap[squadPattern] (
    {
      case sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil =>
        Attempt.successful(Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for updating a squad entry"))
    },
    {
      case Some(SquadInfo(Some(lead), Some(tsk), Some(cguid), Some(sz), Some(cap), Some(sguid))) :: HNil =>
        Attempt.successful(sguid :: lead :: tsk :: cguid :: sz :: cap :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for updating a squad entry"))
    }
  )

  /**
    * `Codec` for reading the `SquadInfo` data in an "update squad leader" entry.
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
    * `Codec` for reading the `SquadInfo` data in an "update squad task or continent" entry.
    */
  private val taskOrContinentCodec : Codec[squadPattern] = (
    bool >>:~ { path =>
      conditional(path, "continent_guid" | PlanetSideZoneID.codec) ::
        conditional(!path, "task" | PacketHelpers.encodedWideStringAligned(7))
    }
    ).exmap[squadPattern] (
    {
      case true :: Some(cguid) :: _ :: HNil =>
        Attempt.successful(Some(SquadInfo(cguid)) :: HNil)
      case true :: None :: _ :: HNil =>
        Attempt.failure(Err("failed to decode squad data for a task - no continent"))
      case false :: _ :: Some(tsk) :: HNil =>
        Attempt.successful(Some(SquadInfo(None, tsk)) :: HNil)
      case false :: _ :: None :: HNil =>
        Attempt.failure(Err("failed to decode squad data for a task - no task"))
    },
    {
      case Some(SquadInfo(_, None, Some(cguid), _, _, _)) :: HNil =>
        Attempt.successful(true :: Some(cguid) :: None :: HNil)
      case Some(SquadInfo(_, Some(tsk), None, _, _, _)) :: HNil =>
        Attempt.successful(false :: None :: Some(tsk) :: HNil)
      case Some(SquadInfo(_, Some(_), Some(_), _, _, _)) :: HNil =>
        Attempt.failure(Err("failed to encode squad data for either a task or a continent - multiple encodings reachable"))
      case _ =>
        Attempt.failure(Err("failed to encode squad data for either a task or a continent"))
    }
  )

  /**
    * `Codec` for reading the `SquadInfo` data in an "update squad size" entry.
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
    * `Codec` for reading the `SquadInfo` data in an "update squad leader and size" entry.
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
    * `Codec` for reading the `SquadInfo` data in an "update squad task and continent" entry.
    */
  private val taskAndContinentCodec : Codec[squadPattern] = (
    bool ::
      ("task" | PacketHelpers.encodedWideStringAligned(7)) ::
      uintL(3) ::
      bool ::
      ("continent_guid" | PlanetSideZoneID.codec)
    ).exmap[squadPattern] (
    {
      case false :: tsk :: 1 :: true :: cguid :: HNil =>
        Attempt.successful(Some(SquadInfo(tsk, cguid)) :: HNil)
      case _ =>
        Attempt.failure(Err("failed to decode squad data for a task and a continent"))
    },
    {
      case Some(SquadInfo(_, Some(tsk), Some(cguid), _, _, _)) :: HNil =>
        Attempt.successful(false :: tsk :: 1 :: true :: cguid :: HNil)
      case _ =>
        Attempt.failure(Err("failed to encode squad data for a task and a continent"))
    }
  )

  /**
    * Codec for reading the `SquadInfo` data in a "remove squad from list" entry.
    * This `Codec` is unique because it is considered a valid `Codec` that does not read any bit data.
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
    * `Codec` for failing to determine a valid `Codec` based on the entry data.
    * This `Codec` is an invalid codec that does not read any bit data.
    * The `conditional` will always return `None` because its determining conditional statement is explicitly `false`.
    */
  private val failureCodec : Codec[squadPattern] = conditional(false, bool).exmap[squadPattern] (
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
    * Select the `Codec` to translate bit data in this packet into an `Option[SquadInfo]` using other fields of the same packet.
    * Refer to comments for the primary `case class` constructor for `SquadHeader` to explain how the conditions in this function path.
    * @param a na
    * @param b na
    * @param c na; may be `None`
    * @param optionalCodec a to-be-defined `Codec` that is determined by the suggested mood of the packet and listing of the squad;
    *                      despite the name, actually a required parameter
    * @return a `Codec` that corresponds to a `squadPattern` translation
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
    //we've not encountered a valid Codec
    failureCodec
  }

  /**
    * `Codec` for standard `SquadHeader` entries.
    */
  val codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) >>:~ { unk1 =>
      ("unk2" | bool) >>:~ { unk2 =>
        conditional(unk1 != 131, "unk3" | uintL(3)) >>:~ { unk3 =>
          selectCodec(unk1, unk2, unk3, allCodec)
        }
      }
    }).as[SquadHeader]

  /**
    * `Codec` for types of `SquadHeader` initializations.
    */
  val init_codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) >>:~ { unk1 =>
      ("unk2" | bool) >>:~ { unk2 =>
        conditional(unk1 != 131, "unk3" | uintL(3)) >>:~ { unk3 =>
          selectCodec(unk1, unk2, unk3, initCodec)
        }
      }
    }).as[SquadHeader]

  /**
    * Alternate `Codec` for types of `SquadHeader` initializations.
    */
  val alt_init_codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) >>:~ { unk1 =>
      ("unk2" | bool) >>:~ { unk2 =>
        conditional(unk1 != 131, "unk3" | uintL(3)) >>:~ { unk3 =>
          selectCodec(unk1, unk2, unk3, alt_initCodec)
        }
      }
    }).as[SquadHeader]
}

object SquadListing {
  /**
    * `Codec` for standard `SquadListing` entries.
    */
  val codec : Codec[SquadListing] = (
    ("index" | uint8L) >>:~ { index =>
      conditional(index < 255, "listing" | SquadHeader.codec) ::
        conditional(index == 255, bits) //consume n < 8 bits padding the tail entry, else vector will try to operate on invalid data
    }).xmap[SquadListing] (
    {
      case ndx :: lstng :: _ :: HNil =>
        SquadListing(ndx, lstng)
    },
    {
      case SquadListing(ndx, lstng) =>
        ndx :: lstng :: None :: HNil
    }
  )

  /**
    * `Codec` for branching types of `SquadListing` initializations.
    */
  val init_codec : Codec[SquadListing] = (
    ("index" | uint8L) >>:~ { index =>
      conditional(index < 255,
        newcodecs.binary_choice(index == 0,
          "listing" | SquadHeader.init_codec,
          "listing" | SquadHeader.alt_init_codec)
      ) ::
        conditional(index == 255, bits) //consume n < 8 bits padding the tail entry, else vector will try to operate on invalid data
    }).xmap[SquadListing] (
    {
      case ndx :: lstng :: _ :: HNil =>
        SquadListing(ndx, lstng)
    },
    {
      case SquadListing(ndx, lstng) =>
        ndx :: lstng :: None :: HNil
    }
  )
}

object ReplicationStreamMessage extends Marshallable[ReplicationStreamMessage] {
  implicit val codec : Codec[ReplicationStreamMessage] = (
    ("behavior" | uintL(3)) >>:~ { behavior =>
      conditional(behavior == 5, "behavior2" | uintL(3)) ::
        conditional(behavior != 1, bool) ::
        newcodecs.binary_choice(behavior != 5,
          "entries" | vector(SquadListing.codec),
          "entries" | vector(SquadListing.init_codec)
        )
    }).xmap[ReplicationStreamMessage] (
    {
      case bhvr :: bhvr2 :: _ :: lst :: HNil =>
        ReplicationStreamMessage(bhvr, bhvr2, lst)
    },
    {
      case ReplicationStreamMessage(1, bhvr2, lst) =>
        1 :: bhvr2 :: None :: lst :: HNil
      case ReplicationStreamMessage(bhvr, bhvr2, lst) =>
        bhvr :: bhvr2 :: Some(false) :: lst :: HNil
    }
  )
}

/*
                         +-> SquadListing.codec -------> SquadHeader.codec ----------+
                         |                                                           |
                         |                                                           |
ReplicationStream.codec -+                                                           |
                         |                                                           |
                         |                           +-> SquadHeader.init_codec -----+-> SquadInfo
                         |                           |                               |
                         +-> SquadListing.initCodec -+                               |
                                                     |                               |
                                                     +-> SquadHeader.alt_init_codec -+
*/
