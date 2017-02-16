// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Manage composition and details of a player's current squad, or the currently-viewed squad.<br>
  * <br>
  * The `action` code indicates the format of the remainder data in the packet.
  * The following formats are translated; their purposes are listed:<br>
  * &nbsp;&nbsp;`(None)`<br>
  * &nbsp;&nbsp;&nbsp;&nbsp;`3 ` - Save Squad Definition
  * &nbsp;&nbsp;&nbsp;&nbsp;`8 ` - List Squad
  * &nbsp;&nbsp;&nbsp;&nbsp;`26` - Reset All
  * &nbsp;&nbsp;&nbsp;&nbsp;`35` - Cancel Squad Search
  * &nbsp;&nbsp;&nbsp;&nbsp;`41` - Cancel Find
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
  * &nbsp;&nbsp;&nbsp;&nbsp;`34` - Search for Squads with a Particular Role<br>
  * <br>
  * Exploration:<br>
  * Some notes regarding the full list of action codes follows after this packet.
  * Asides from codes whose behaviors are unknown, some codes also have unknown data format.
  * No information for codes 1, 5, 9, 27, or 35 has been found yet.
  * @param action the purpose of this packet;
  *               also decides the content of the parameter fields
  * @param unk1 na
  * @param unk2 na
  * @param string_opt the optional `String` parameter
  * @param int1_opt the first optional `Int` parameter;
  *                 will not necessarily conform to a single bit length
  * @param int2_opt the second optional `Int` parameter
  * @param long1_opt the first optional `Long` parameter;
  *                 will not necessarily conform to a single bit length
  * @param long2_opt the second optional `Long` parameter
  * @param bool_opt the optional `Boolean` parameter
  */
final case class SquadDefinitionActionMessage(action : Int,
                                              unk1 : Int,
                                              unk2 : Int,
                                              string_opt : Option[String],
                                              int1_opt : Option[Int],
                                              int2_opt : Option[Int],
                                              long1_opt : Option[Long],
                                              long2_opt : Option[Long],
                                              bool_opt : Option[Boolean])
  extends PlanetSideGamePacket {
  type Packet = SquadDefinitionActionMessage
  def opcode = GamePacketOpcode.SquadDefinitionActionMessage
  def encode = SquadDefinitionActionMessage.encode(this)
}

object SquadDefinitionActionMessage extends Marshallable[SquadDefinitionActionMessage] {
  /**
    * Common pattern for the parameters, with enough fields to support all possible outputs.
    * All fields are `Option`al purposefully.
    */
  private type allPattern = Option[String] :: Option[Int] :: Option[Int] :: Option[Long] :: Option[Long] :: Option[Boolean] :: HNil

  /**
    * `Codec` for reading nothing from the remainder of the stream data.
    * @return a filled-out `allPattern` if successful
    */
  def noneCodec : Codec[allPattern] = ignore(0).xmap[allPattern] (
    {
      case () =>
        None :: None :: None :: None :: None :: None :: HNil
    },
    {
      case _ :: _ :: _ :: _ :: _ :: _ :: HNil =>
        ()
    }
  )

  /**
    * `Codec` for reading a single `Boolean` from remaining stream data.
    * @return a filled-out `allPattern` if successful
    */
  def boolCodec : Codec[allPattern] = bool.hlist.exmap[allPattern] (
    {
      case n :: HNil =>
        Attempt.successful(None :: None :: None :: None :: None :: Some(n) :: HNil)
    },
    {
      case _ :: _ :: _ :: _ :: _ :: None :: HNil =>
        Attempt.failure(Err("expected a boolean value but found nothing"))
      case _ :: _ :: _ :: _ :: _ :: Some(n) :: HNil =>
        Attempt.successful(n :: HNil)
    }
  )

  /**
    * `Codec` for reading a single `Int` from remaining stream data.
    * Multiple bit lengths can be processed from this reading.
    * @param icodec the `Codec[Int]` read by this method
    * @return a filled-out `allPattern` if successful
    */
  def intCodec(icodec : Codec[Int]) : Codec[allPattern] = icodec.hlist.exmap[allPattern] (
    {
      case n :: HNil =>
        Attempt.successful(None :: Some(n) :: None :: None :: None :: None :: HNil)
    },
    {
      case _ :: None :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("expected an integer value but found nothing"))
      case _ :: Some(n) :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.successful(n :: HNil)
    }
  )

  /**
    * `Codec` for reading a single `Long` from remaining stream data.
    * @return a filled-out `allPattern` if successful
    */
  def longCodec : Codec[allPattern] = uint32L.hlist.exmap[allPattern] (
    {
      case n :: HNil =>
        Attempt.successful(None :: None :: None :: Some(n) :: None :: None :: HNil)
    },
    {
      case _ :: _ :: _ :: None :: _ :: _ :: HNil =>
        Attempt.failure(Err("expected a long value but found nothing"))
      case _ :: _ :: _ :: Some(n) :: _ :: _ :: HNil =>
        Attempt.successful(n :: HNil)
    }
  )

  /**
    * `Codec` for reading a `String` from remaining stream data.
    * All `String`s processed by this reading are wide character and are padded by six.
    * @return a filled-out `allPattern` if successful
    */
  def stringCodec : Codec[allPattern] = PacketHelpers.encodedWideStringAligned(6).hlist.exmap[allPattern] (
    {
      case a :: HNil =>
        Attempt.successful(Some(a) :: None :: None :: None :: None :: None :: HNil)
    },
    {
      case None:: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("expected a string value but found nothing"))
      case Some(a) :: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.successful(a :: HNil)
    }
  )

  /**
    * `Codec` for reading an `Int` followed by a `Long` from remaining stream data.
    * Multiple bit lengths can be processed for the `Long1` value from this reading.
    * @param lcodec the `Codec[Long]` read by this method
    * @return a filled-out `allPattern` if successful
    */
  def intLongCodec(lcodec : Codec[Long]) : Codec[allPattern] = (
    uint4L ::
      lcodec
    ).exmap[allPattern] (
    {
      case a :: b :: HNil =>
        Attempt.successful(None :: Some(a) :: None :: Some(b) :: None :: None :: HNil)
    },
    {
      case _ :: None :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("expected a integer value but found nothing"))
      case _ :: _ :: _ :: None :: _ :: _ :: HNil =>
        Attempt.failure(Err("expected a long value but found nothing"))
      case _ :: Some(a) :: _ :: Some(b) :: _ :: _ :: HNil =>
        Attempt.successful(a :: b :: HNil)
    }
  )

  /**
    * `Codec` for reading an `Int` followed by a `String` from remaining stream data.
    * All `String`s processed by this reading are wide character and are padded by two.
    * @return a filled-out `allPattern` if successful
    */
  def intStringCodec : Codec[allPattern] = (
    uint4L ::
      PacketHelpers.encodedWideStringAligned(2)
    ).exmap[allPattern] (
    {
      case a :: b :: HNil =>
        Attempt.successful(Some(b) :: Some(a) :: None :: None :: None :: None :: HNil)
    },
    {
      case None:: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("expected a string value but found nothing"))
      case _ :: None :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("expected an integer value but found nothing"))
      case Some(b) :: Some(a) :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.successful(a :: b :: HNil)
    }
  )

  /**
    * `Codec` for reading two `Long`s from remaining stream data.
    * @return a filled-out `allPattern` if successful
    */
  def longLongCodec : Codec[allPattern] = (
    ulongL(46) ::
      uint32L
    ).exmap[allPattern] (
    {
      case a :: b :: HNil =>
        Attempt.successful(None :: None :: None :: Some(a) :: Some(b) :: None :: HNil)
    },
    {
      case (_ :: _ :: _ :: None :: _ :: _ :: HNil) | (_ :: _ :: _ :: _ :: None :: _ :: HNil) =>
        Attempt.failure(Err("expected two long values but found one"))
      case _ :: _ :: _ :: Some(a) :: Some(b) :: _ :: HNil =>
        Attempt.successful(a :: b :: HNil)
    }
  )

  /**
    * `Codec` for reading a `String`, a `Long`, and two `Int`s from remaining stream data.
    * All `String`s processed by this reading are wide character and are padded by six.
    * @return a filled-out `allPattern` if successful
    */
  def complexCodec : Codec[allPattern] = (
    PacketHelpers.encodedWideStringAligned(6) ::
      ulongL(46) ::
      uint16L ::
      uintL(3)
    ).exmap[allPattern] (
    {
      case a :: b :: c :: d :: HNil =>
        Attempt.successful(Some(a) :: Some(c) :: Some(d) :: Some(b) :: None :: None :: HNil)
    },
    {
      case None:: _ :: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err("expected a string value but found nothing"))
      case _ :: _ :: _ :: None :: _ :: _ :: HNil =>
        Attempt.failure(Err("expected a long value but found nothing"))
      case (_ :: None :: _ :: _ :: _ :: _ :: HNil) | (_ :: _ :: None :: _ :: _ :: _ :: HNil) =>
        Attempt.failure(Err("expected two integer values but found one"))
      case Some(a) :: Some(c) :: Some(d) :: Some(b) :: _ :: _ :: HNil =>
        Attempt.successful(a :: b :: c :: d :: HNil)
    }
  )

  import scala.annotation.switch

  /**
    * Select the `Codec` to translate bit data in this packet with an `allPattern` format.
    * @param action the purpose of this packet;
    *               also decides the content of the parameter fields
    * @return an `allPattern` `Codec` that parses the appropriate data
    */
  def selectCodec(action : Int) : Codec[allPattern] = (action : @switch) match {
    case 3 | 8 | 26 | 35 | 41 => //TODO double check these
      noneCodec

    case 28 | 29 | 30 | 31  =>
      boolCodec

    case 33 =>
      intCodec(uintL(3))
    case 10 | 11 | 21 | 22 | 40 =>
      intCodec(uint4L)
    case 20 =>
      intCodec(uint16L)

    case 13 | 14 | 15 | 37 =>
      longCodec

    case 7 | 19 =>
      stringCodec

    case 12 | 38 =>
      intLongCodec(uint32L)
    case 25 =>
      intLongCodec(ulongL(46))

    case 23 | 24 =>
      intStringCodec

    case 36 =>
      longLongCodec

    case 34 =>
      complexCodec

    case _ =>
      //TODO for debugging purposes only; normal failure condition below
      bits.hlist.exmap[allPattern] (
        {
          case x :: HNil =>
            org.log4s.getLogger.warn(s"can not match a codec pattern for decoding $action")
            Attempt.successful(Some(x.toString) :: None :: None :: None :: None :: None :: HNil)
        },
        {
          case Some(x) :: None :: None :: None :: None :: None :: HNil =>
            org.log4s.getLogger.warn(s"can not match a codec pattern for encoding $action")
            Attempt.successful(scodec.bits.BitVector.fromValidBin(x) :: HNil)
        }
      )
//      ignore(0).exmap[allPattern] (
//        {
//          case () =>
//            Attempt.failure(Err(s"can not match a codec pattern for decoding $action"))
//        },
//        {
//          case _ :: _ :: _ :: _ :: _ :: _ :: HNil =>
//            Attempt.failure(Err(s"can not match a codec pattern for encoding $action"))
//        }
//      )
  }

  implicit val codec : Codec[SquadDefinitionActionMessage] = (
    ("action" | uintL(6)) >>:~ { action =>
      ("unk1" | uint16L) ::
        ("unk2" | uint4L) ::
        selectCodec(action)
    }
    ).as[SquadDefinitionActionMessage]
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
