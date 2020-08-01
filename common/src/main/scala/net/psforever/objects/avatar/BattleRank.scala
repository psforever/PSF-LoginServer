package net.psforever.objects.avatar

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.game.objectcreate.UniformStyle

/** Battle ranks and their starting experience values
  * Source: http://wiki.psforever.net/wiki/Battle_Rank
  */
sealed abstract class BattleRank(val value: Int, val experience: Long) extends IntEnumEntry {
  def implantSlots: Int = {
    if (this.value >= BattleRank.BR18.value) {
      3
    } else if (this.value >= BattleRank.BR12.value) {
      2
    } else if (this.value >= BattleRank.BR6.value) {
      1
    } else {
      0
    }
  }

  def uniformStyle: UniformStyle.Value = {
    if (this.value >= BattleRank.BR25.value) {
      UniformStyle.ThirdUpgrade
    } else if (this.value >= BattleRank.BR14.value) {
      UniformStyle.SecondUpgrade
    } else if (this.value >= BattleRank.BR7.value) {
      UniformStyle.FirstUpgrade
    } else {
      UniformStyle.Normal
    }
  }

}

case object BattleRank extends IntEnum[BattleRank] {

  case object BR1 extends BattleRank(value = 1, experience = 0L)

  case object BR2 extends BattleRank(value = 2, experience = 1000L)

  case object BR3 extends BattleRank(value = 3, experience = 3000L)

  case object BR4 extends BattleRank(value = 4, experience = 7500L)

  case object BR5 extends BattleRank(value = 5, experience = 15000L)

  case object BR6 extends BattleRank(value = 6, experience = 30000L)

  case object BR7 extends BattleRank(value = 7, experience = 45000L)

  case object BR8 extends BattleRank(value = 8, experience = 67500L)

  case object BR9 extends BattleRank(value = 9, experience = 101250L)

  case object BR10 extends BattleRank(value = 10, experience = 126563L)

  case object BR11 extends BattleRank(value = 11, experience = 158203L)

  case object BR12 extends BattleRank(value = 12, experience = 197754L)

  case object BR13 extends BattleRank(value = 13, experience = 247192L)

  case object BR14 extends BattleRank(value = 14, experience = 308990L)

  case object BR15 extends BattleRank(value = 15, experience = 386239L)

  case object BR16 extends BattleRank(value = 16, experience = 482798L)

  case object BR17 extends BattleRank(value = 17, experience = 603497L)

  case object BR18 extends BattleRank(value = 18, experience = 754371L)

  case object BR19 extends BattleRank(value = 19, experience = 942964L)

  case object BR20 extends BattleRank(value = 20, experience = 1178705L)

  case object BR21 extends BattleRank(value = 21, experience = 1438020L)

  case object BR22 extends BattleRank(value = 22, experience = 1710301L)

  case object BR23 extends BattleRank(value = 23, experience = 1988027L)

  case object BR24 extends BattleRank(value = 24, experience = 2286231L)

  case object BR25 extends BattleRank(value = 25, experience = 2583441L)

  val values: IndexedSeq[BattleRank] = findValues

  /** Find BattleRank variant for given experience value */
  def withExperience(experience: Long): BattleRank = {
    withExperienceOpt(experience).get
  }

  /** Find BattleRank variant for given experience value */
  def withExperienceOpt(experience: Long): Option[BattleRank] = {
    values.find(br =>
      this.withValueOpt(br.value + 1) match {
        case Some(nextBr) =>
          experience >= br.experience && experience < nextBr.experience
        case None =>
          experience >= br.experience
      }
    )
  }
}
