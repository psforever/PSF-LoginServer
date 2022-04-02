// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.MeritCommendation
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Base class for all merit commendation advancement stages.
  */
sealed trait AwardOption {
  def value: Long
  def completion: Long
}
/**
  * Display this award's development progress.
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
  * @param value the date (mm/dd/yyyy) that the award was achieved in POSIX seconds;
  *              that's `System.currentTimeMillis() / 1000`
  */
final case class AwardCompletion(value: Long) extends AwardOption {
  /** same as the parameter value */
  def completion: Long = value
}

/**
  * Dispatched from the server to load information about a character's merit commendation awards progress.<br>
  * <br>
  * The three stages of a merit commendation award are: progress, qualification, and completion.
  * The progress stage and the qualification stage have their own development conditions.
  * Ocassionally, the development is nonexistent and the award is merely an on/off switch.
  * Occasionally, there is no qualification requirement and the award merely advances in the progress stage
  * then transitions directly from progress to completion.
  * Completion information is available from the character info / achievements tab
  * and takes the form of ribbons associated with the merit commendation at a given rank
  * and the date that rank was attained.
  * Progress and qualification information are visible from the character info / achievements / award progress window
  * and take the form of the name and rank of the merit commendation
  * and two numbers that indicate the current and the goal towards the next stage.
  * The completion stage is also visible from this window
  * and will take the form of the same name and rank of the merit commendation indicated as "Completed" as of a date.
  * @see `MeritCommendation.Value`
  * @param merit_commendation the award and rank
  * @param state the current state of the award advancement
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

  private val qualification_codec: Codec[AwardOption] = {
    uint32L.hlist
  }.xmap[AwardOption](
    {
      case a :: HNil => AwardQualificationProgress(a)
    },
    {
      case AwardQualificationProgress(a) => a :: HNil
    }
  )

  private val completion_codec: Codec[AwardOption] = {
    uint32L.hlist
  }.xmap[AwardOption](
    {
      case a :: HNil => AwardCompletion(a)
    },
    {
      case AwardCompletion(a) => a :: HNil
    }
  )

  private val progress_codec: Codec[AwardOption] = {
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
      either(bool, progress_codec, qualification_codec).xmap[AwardOption](
        {
          case Left(d)  => d
          case Right(d) => d
        },
        {
          case d: AwardProgress               => Left(d)
          case d: AwardQualificationProgress  => Right(d)
        }
      ),
      completion_codec
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
