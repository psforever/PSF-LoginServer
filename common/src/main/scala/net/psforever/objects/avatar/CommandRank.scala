package net.psforever.objects.avatar

import enumeratum.values.{IntEnum, IntEnumEntry}

/** Command ranks and their starting experience values */
sealed abstract class CommandRank(val value: Int, val experience: Long) extends IntEnumEntry

case object CommandRank extends IntEnum[CommandRank] {

  case object CR0 extends CommandRank(value = 0, experience = 0L)

  case object CR1 extends CommandRank(value = 1, experience = 10000L)

  case object CR2 extends CommandRank(value = 2, experience = 50000L)

  case object CR3 extends CommandRank(value = 3, experience = 150000L)

  case object CR4 extends CommandRank(value = 4, experience = 300000L)

  case object CR5 extends CommandRank(value = 5, experience = 600000L)

  val values: IndexedSeq[CommandRank] = findValues

  /** Find CommandRank variant for given experience value */
  def withExperience(experience: Long): CommandRank = {
    withExperienceOpt(experience).get
  }

  /** Find CommandRank variant for given experience value */
  def withExperienceOpt(experience: Long): Option[CommandRank] = {
    values.find(cr =>
      this.withValueOpt(cr.value + 1) match {
        case Some(nextCr) =>
          experience >= cr.experience && experience < nextCr.experience
        case None =>
          experience >= cr.experience
      }
    )
  }
}
