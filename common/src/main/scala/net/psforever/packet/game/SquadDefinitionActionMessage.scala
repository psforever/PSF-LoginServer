// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class SquadDefinitionActionMessage(action : Int,
                                              unk2 : Int,
                                              unk3 : Int,
                                              unk4 : Option[String],
                                              unk5 : Option[Int],
                                              unk6 : Option[Int],
                                              unk7 : Option[Long],
                                              unk8 : Option[Long],
                                              unk9 : Option[Boolean])
  extends PlanetSideGamePacket {
  type Packet = SquadDefinitionActionMessage
  def opcode = GamePacketOpcode.SquadDefinitionActionMessage
  def encode = SquadDefinitionActionMessage.encode(this)
}

object SquadDefinitionActionMessage extends Marshallable[SquadDefinitionActionMessage] {
  private type allPattern = Option[String] :: Option[Int] :: Option[Int] :: Option[Long] :: Option[Long] :: Option[Boolean] :: HNil

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
  def select_codec(action : Int) : Codec[allPattern] = (action : @switch) match {
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
      //TODO for debugging purposes only
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
//            Attempt.failure(Err(s"can not match a codec pattern for decoding $test"))
//        },
//        {
//          case _ =>
//            Attempt.failure(Err(s"can not match a codec pattern for encoding $test"))
//        }
//      )
  }

  implicit val codec : Codec[SquadDefinitionActionMessage] = (
    ("action" | uintL(6)) >>:~ { action =>
      ("unk2" | uint16L) ::
        ("unk3" | uint4L) ::
        select_codec(action)
//        conditional(unk1 == 7, "unk4" | PacketHelpers.encodedWideStringAligned(6)) ::
//        conditional((unk1 > 9 && unk1 < 13) || (unk1 > 20 && unk1 < 26) || unk1 == 38 || unk1 == 40, "unk5" | uint4L) ::
//        conditional(unk1 == 23 || unk1 == 34, "unk6" | PacketHelpers.encodedWideStringAligned(4)) ::
//        conditional(unk1 == 25 || unk1 == 34 || unk1 == 36, "unk7" | ulongL(46)) ::
//        conditional(unk1 == 19, "unk8" | PacketHelpers.encodedWideStringAligned(2)) :: //goto LABEL_49
//        conditional(unk1 == 20 || unk1 == 34, "unk9" | uint16L) :: //goto LABEL_48
//        conditional(unk1 == 24, "unkA" | PacketHelpers.encodedWideString) :: //goto LABEL_49
//        conditional((unk1 > 11 && unk1 < 16) || (unk1 > 35 && unk1 < 39), "unkB" | uint32L) :: //goto LABEL_49
//      //LABEL_48
//        conditional(unk1 > 27 && unk1 < 32, "unkC" | bool) :: //goto LABEL_49
//        conditional(unk1 == 33, "unkD" | uintL(3)) ::
//      //LABEL_49
//        conditional(unk1 == 34, "unkE" | uintL(3))
    }
    ).as[SquadDefinitionActionMessage]
}

/*
("change" specifically indicates the perspective is from the SL; "update for squad member" actions may be different)
("[#]" indicates the mode is detected but not properly parsed; the length of the combined fields may follow

[0] - clicking on a squad listed in the "Find Squad" tab / cancel squad search
[3] - save sqad favorite (6 bits)
[4] - load a squad definition favorite (6 bits)
7  -
[8] - list squad (6 bits)
10 - select this role for yourself
11 -
12 -
13 -
14 -
15 -
19 - change purpose
20 - change zone
21 - change/close squad member position
22 - change/add squad member position
23 - change squad member req role
24 - change squad member req detailed orders
25 - change squad member req weapons
[26] - reset all (6 bits)
28 - auto-approve requests for invitation
29 -
30 -
31 - location follows squad lead
33 -
34 - search for squads with a particular role
36 -
37 -
38 -
40 - find LFS soldiers that meet the requirements for this role
[41] - cancel search for LFS soldiers (6 bits)

28 - 1u (Boolean)
29 - iu (Boolean)
30 - 1u (Boolean)
31 - 1u (Boolean)
33 - 3u (Int)
10 - 4u (Int)
11 - 4u (Int)
21 - 4u (Int)
22 - 4u (Int)
40 - 4u (Int)
20 - 16u (Int)
13 - 32u (Long)
14 - 32u (Long)
15 - 32u (Long)
37 - 32u (Long)
7  - String(6) (String)
19 - String(6) (String)
12 - 4u + 32u (Int :: Long)
38 - 4u + 32u (Int :: Long)
25 - 4u + 46u (Int :: Long)
23 - 4u + String(2) (Int :: String)
24 - 4u + String(2) (Int :: String)
36 - 46u + 32u (Long :: Long)
34 - String(6) + 46u + 16u + 3u (String :: Long :: Int :: Int)
  */