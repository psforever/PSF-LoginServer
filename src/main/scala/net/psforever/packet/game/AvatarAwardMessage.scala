// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.MeritCommendation
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

trait AwardOption {
  def value: Long
  def completion: Long
}
/**
  * Display this award's progress.
  * @param value the current count towards this award
  * @param completion the target (maximum) count
  */
final case class AwardProgress(value: Long, completion: Long) extends AwardOption
/**
  * Display this award's qualification progress.
  * The process is the penultimate conditions necessary for award completion,
  * separate from the aforementioned progress.
  * This is almost always a kill streak,
  * where the user must terminate a certain numbers of enemies without dying.
  * @param value the current count towards this award
  */
final case class AwardQualificationProgress(value: Long) extends AwardOption {
  /** zero'd as the value is not reported here */
  def completion: Long = 0L
}
/**
  * Display this award as completed.
  * @param value the date (mm/dd/yyyy) that the award was achieved in POSIX time
  */
final case class AwardCompletion(value: Long) extends AwardOption {
  /** same as the parameter value */
  def completion: Long = value
}

/**
  * na
  * @param merit_commendation na
  * @param state na
  * @param unk na;
  *            0 and 1 are the possible values;
  *            0 is the common value
  */
final case class AvatarAwardMessage(
                                     merit_commendation: MeritCommendation.Value,
                                     state: AwardOption,
                                     unk: Int
                                   )
  extends PlanetSideGamePacket {
  type Packet = AvatarAwardMessage
  def opcode = GamePacketOpcode.AvatarAwardMessage
  def encode = AvatarAwardMessage.encode(this)
}

object AvatarAwardMessage extends Marshallable[AvatarAwardMessage] {
  def apply(meritCommendation: MeritCommendation.Value, state: AwardOption):AvatarAwardMessage =
    AvatarAwardMessage(meritCommendation, state, unk = 0)

  private val codec_one: Codec[AwardOption] = {
    uint32L.hlist
  }.xmap[AwardOption](
    {
      case a :: HNil => AwardQualificationProgress(a)
    },
    {
      case AwardQualificationProgress(a) => a :: HNil
    }
  )

  private val codec_two: Codec[AwardOption] = {
    uint32L.hlist
  }.xmap[AwardOption](
    {
      case a :: HNil => AwardCompletion(a)
    },
    {
      case AwardCompletion(a) => a :: HNil
    }
  )

  private val codec_zero: Codec[AwardOption] = {
    uint32L :: uint32L
  }.xmap[AwardOption](
    {
      case a :: b :: HNil => AwardProgress(a, b)
    },
    {
      case AwardProgress(a, b) => a :: b :: HNil
    }
  )

  implicit val codec: Codec[AvatarAwardMessage] = (
    ("merit_commendation" | MeritCommendation.codec) ::
    ("state" | either(bool,
      either(bool, codec_zero, codec_one).xmap[AwardOption](
        {
          case Left(d)  => d
          case Right(d) => d
        },
        {
          case d: AwardProgress               => Left(d)
          case d: AwardQualificationProgress  => Right(d)
        }
      ),
      codec_two
    ).xmap[AwardOption](
      {
        case Left(d)  => d
        case Right(d) => d
      },
      {
        case d: AwardProgress               => Left(d)
        case d: AwardQualificationProgress  => Left(d)
        case d: AwardCompletion             => Right(d)
      }
    )) ::
    ("unk" | uint8L)
  ).as[AvatarAwardMessage]
}
