// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless._

//this packet is limited mainly by byte-size

//continents are stored in this packet as 32-bit numbers instead of 16-bit; after the normal 16 bits, two bytes can be ignored

final case class SquadInfo(leader : Option[String],
                           task : Option[String],
                           continent_guid : Option[PlanetSideGUID],
                           size : Option[Int],
                           capacity : Option[Int],
                           squad_guid : Option[PlanetSideGUID] = None)

final case class SquadHeader(action : Int,
                             unk : Boolean,
                             action2 : Option[Int],
                             info : Option[SquadInfo] = None)

final case class SquadListing(index : Int = 255,
                              listing : Option[SquadHeader] = None,
                              na : Option[BitVector] = None)

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
  //use: SquadInfo(leader, task, continent_guid, size, capacity)
  def apply(leader : String, task : String, continent_guid : PlanetSideGUID, size : Int, capacity : Int) : SquadInfo = {
    SquadInfo(Some(leader), Some(task), Some(continent_guid), Some(size), Some(capacity))
  }

  //use: SquadInfo(leader, task, continent_guid, size, capacity, sguid)
  def apply(leader : String, task : String, continent_guid : PlanetSideGUID, size : Int, capacity : Int, sguid : PlanetSideGUID) : SquadInfo = {
    SquadInfo(Some(leader), Some(task), Some(continent_guid), Some(size), Some(capacity), Some(sguid))
  }

  //use: SquadInfo(leader, None)
  def apply(leader : Option[String], task : String) : SquadInfo = {
    SquadInfo(leader, Some(task), None, None, None)
  }

  //use: SquadInfo(None, task)
  def apply(leader : String, task : Option[String]) : SquadInfo = {
    SquadInfo(Some(leader), task, None, None, None)
  }

  //use: SquadInfo(continent_guid)
  def apply(continent_guid : PlanetSideGUID) : SquadInfo = {
    SquadInfo(None, None, Some(continent_guid), None, None)
  }

  //use: SquadInfo(size, None)
  //we currently do not know the action codes that adjust squad capacity
  def apply(size : Int, capacity : Option[Int]) : SquadInfo = {
    SquadInfo(None, None, None, Some(size), None)
  }

  //use: SquadInfo(None, capacity)
  //we currently do not know the action codes that adjust squad capacity
  def apply(size : Option[Int], capacity : Int) : SquadInfo = {
    SquadInfo(None, None, None, size, Some(capacity))
  }

  //use: SquadInfo(leader, size)
  def apply(leader : String, size : Int) : SquadInfo = {
    SquadInfo(Some(leader), None, None, Some(size), None)
  }

  //use: SquadInfo(task, continent_guid)
  def apply(task : String, continent_guid : PlanetSideGUID) : SquadInfo = {
    SquadInfo(None, Some(task), Some(continent_guid), None, None, None)
  }
}

object SquadHeader extends Marshallable[SquadHeader] {
  type squadPattern = Option[SquadInfo] :: HNil
  val initCodec : Codec[squadPattern] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideString) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      uint16L ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).xmap[squadPattern] (
    {
      case sguid :: lead :: tsk :: cguid :: x :: sz :: cap :: HNil =>
        Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil
      case _ =>
        null
    },
    {
      case Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil =>
        sguid.get :: lead.get :: tsk.get :: cguid.get :: 0 :: sz.get :: cap.get :: HNil
      case _ =>
        PlanetSideGUID(0) :: "" :: "" :: PlanetSideGUID(0) :: 0 :: 0 :: 0 :: HNil
    }
  )
  val alt_initCodec : Codec[squadPattern] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      uint16L ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).xmap[squadPattern] (
    {
      case sguid :: lead :: tsk :: cguid :: x :: sz :: cap :: HNil =>
        Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil
      case _ =>
        null
    },
    {
      case Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil =>
        sguid.get :: lead.get :: tsk.get :: cguid.get :: 0 :: sz.get :: cap.get :: HNil
      case _ =>
        PlanetSideGUID(0) :: "" :: "" :: PlanetSideGUID(0) :: 0 :: 0 :: 0 :: HNil
    }
  )

  val allCodec : Codec[squadPattern] = (
    ("squad_guid" | PlanetSideGUID.codec) ::
      ("leader" | PacketHelpers.encodedWideStringAligned(3)) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      uint16L ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).xmap[squadPattern] (
    {
      case sguid :: lead :: tsk :: cguid :: x :: sz :: cap :: HNil =>
        Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil
      case _ =>
        null
    },
    {
      case Some(SquadInfo(lead, tsk, cguid, sz, cap, sguid)) :: HNil =>
        sguid.get :: lead.get :: tsk.get :: cguid.get :: 0 :: sz.get :: cap.get :: HNil
      case _ =>
        PlanetSideGUID(0) :: "" :: "" :: PlanetSideGUID(0) :: 0 :: 0 :: 0 :: HNil
    }
  )

  val leaderCodec : Codec[squadPattern] = (
    bool ::
      ("leader" | PacketHelpers.encodedWideStringAligned(7))
    ).xmap[squadPattern] (
    {
      case x :: lead :: HNil =>
        Some(SquadInfo(lead, None)) :: HNil
      case _ =>
        null :: HNil
    },
    {
      case Some(SquadInfo(lead, _, _, _, _, _)) :: HNil =>
        true :: lead.get :: HNil
      case _ =>
        true :: "" :: HNil
    }
  )

  val taskOrContinentCodec : Codec[squadPattern] = (
    bool >>:~ { path =>
      conditional(path, "continent_guid" | PlanetSideGUID.codec) ::
        conditional(path, uint16L) ::
        conditional(!path, "task" | PacketHelpers.encodedWideStringAligned(7))
    }
    ).xmap[squadPattern] (
    {
      case true :: cguid :: Some(0) :: None :: HNil =>
        Some(SquadInfo(cguid.get)) :: HNil
      case false :: None :: None :: tsk :: HNil =>
        Some(SquadInfo(None, tsk.get)) :: HNil
      case _ =>
        null :: HNil
    },
    {
      case Some(SquadInfo(_, None, cguid, _, _, _)) :: HNil =>
        true :: cguid :: Some(0) :: None :: HNil
      case Some(SquadInfo(_, tsk, None, _, _, _)) :: HNil =>
        false :: None :: None :: tsk :: HNil
      case _ =>
        false :: None :: None :: None :: HNil
    }
  )

  val sizeCodec : Codec[squadPattern] = (
    bool ::
      ("size" | uint4L)
    ).xmap[squadPattern] (
    {
      case false :: sz :: HNil =>
        Some(SquadInfo(sz, None)) :: HNil
      case _ =>
        null :: HNil
    },
    {
      case Some(SquadInfo(_, _, _, sz, _, _)) :: HNil =>
        false :: sz.get :: HNil
      case _ =>
        false :: 0 :: HNil
    }
  )

  val leaderSizeCodec : Codec[squadPattern] = (
    bool ::
      ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
      uint4L ::
      ("size" | uint4L)
    ).xmap[squadPattern] (
    {
      case true :: lead :: 4 :: sz :: HNil =>
        Some(SquadInfo(lead, sz)) :: HNil
      case _ =>
        null :: HNil
    },
    {
      case Some(SquadInfo(lead, _, _, sz, _, _)) ::HNil =>
        true :: lead.get :: 4 :: sz.get :: HNil
      case _ =>
        true :: "" :: 4 :: 0 :: HNil
    }
  )

  val taskAndContinentCodec : Codec[squadPattern] = (
    bool ::
      ("task" | PacketHelpers.encodedWideStringAligned(7)) ::
      uintL(3) ::
      bool ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      uint16L
    ).xmap[squadPattern] (
    {
      case false :: tsk :: 1 :: true :: cguid :: 0 :: HNil =>
        Some(SquadInfo(tsk, cguid)) :: HNil
      case _ =>
        null :: HNil
    },
    {
      case Some(SquadInfo(_, tsk, cguid, _, _, _)) :: HNil =>
        false :: tsk.get :: 1 :: true :: cguid.get :: 0 :: HNil
      case _ =>
        false :: "" :: 1 :: true :: PlanetSideGUID(0) :: 0 :: HNil
    }
  )

  val removeCodec : Codec[squadPattern] = conditional(false, bool).xmap[squadPattern] (
    {
      case None =>
        None :: HNil
      case _ =>
        None :: HNil
    },
    {
      case None :: HNil =>
        None
      case _ =>
        None
    }
  )

  val failureCodec : Codec[squadPattern] = conditional(false, bool).exmap[squadPattern] (
    {
      case Some(x) =>
        Attempt.failure(Err("path that should be unreachable, while decoding with unhandled codec"))
      case None =>
        Attempt.failure(Err("decoding with unhandled codec"))
    },
    {
      case null =>
        Attempt.failure(Err("path that should be unreachable, while encoding with unhandled codec"))
      case _ =>
        Attempt.failure(Err("encoding with unhandled codec"))
    }
  )

  def apply(action : Int, unk : Boolean, action2 : Option[Int], info : SquadInfo) : SquadHeader = {
    SquadHeader(action, unk, action2, Some(info))
  }

  implicit val codec : Codec[SquadHeader] = (
    ("action" | uint8L) >>:~ { action =>
      ("unk" | bool) >>:~ { unk =>
        conditional(action != 131, "action2" | uintL(3)) >>:~ { action2 =>
          selectCodec(action, unk, action2, allCodec)
        }
      }
    }
    ).as[SquadHeader]

  implicit val init_codec : Codec[SquadHeader] = (
    ("action" | uint8L) >>:~ { action =>
      ("unk" | bool) >>:~ { unk =>
        conditional(action != 131, "action2" | uintL(3)) >>:~ { action2 =>
          selectCodec(action, unk, action2, initCodec)
        }
      }
    }
    ).as[SquadHeader]

  implicit val alt_init_codec : Codec[SquadHeader] = (
    ("action" | uint8L) >>:~ { action =>
      ("unk" | bool) >>:~ { unk =>
        conditional(action != 131, "action2" | uintL(3)) >>:~ { action2 =>
          selectCodec(action, unk, action2, alt_initCodec)
        }
      }
    }
    ).as[SquadHeader]

  def selectCodec(action : Int, unk : Boolean, action2 : Option[Int], extra : Codec[squadPattern]) : Codec[squadPattern] = {
    if(action2.isDefined) {
      val action2Val = action2.get
      if(action == 0 && unk)
        if(action2Val == 4)
          return removeCodec
      if(action == 128 && unk) {
        if(action2Val == 0)
          return leaderCodec
        else if(action2Val == 1)
          return taskOrContinentCodec
        else if(action2Val == 2)
          return sizeCodec
      }
      else if(action == 129 && !unk) {
        if(action2Val == 0)
          return leaderSizeCodec
        else if(action2Val == 1)
          return taskAndContinentCodec
      }
    }
    else {
      if(action == 131 && !unk)
        return extra
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
  implicit val codec : Codec[ReplicationStreamMessage] = (
    ("behavior" | uintL(3)) >>:~ { behavior =>
      conditional(behavior == 5, "behavior2" | uintL(3)) :: //note: uses self
        conditional(behavior != 1, "unk" | bool) ::
        newcodecs.binary_choice(behavior != 5,
          "entries" | vector(SquadListing.codec),
          "entries" | vector(SquadListing.init_codec)
        )
    }
    ).as[ReplicationStreamMessage]
}
