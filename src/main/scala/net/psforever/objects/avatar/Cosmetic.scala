package net.psforever.objects.avatar

import enumeratum.values.{IntEnum, IntEnumEntry}
import scodec.{Attempt, Codec}
import scodec.codecs.uint

/** Avatar cosmetic options */
sealed abstract class Cosmetic(val value: Int) extends IntEnumEntry

case object Cosmetic extends IntEnum[Cosmetic] {

  case object BrimmedCap extends Cosmetic(value = 1)

  case object Earpiece extends Cosmetic(value = 2)

  case object Sunglasses extends Cosmetic(value = 4)

  case object Beret extends Cosmetic(value = 8)

  case object NoHelmet extends Cosmetic(value = 16)

  val values: IndexedSeq[Cosmetic] = findValues

  /** Get enum values from ObjectCreateMessage value */
  def valuesFromObjectCreateValue(value: Int): Set[Cosmetic] = {
    values.filter(c => (value & c.value) == c.value).toSet
  }

  /** Serialize enum values to ObjectCreateMessage value */
  def valuesToObjectCreateValue(values: Set[Cosmetic]): Int = {
    values.foldLeft(0)(_ + _.value)
  }

  /** Get enum values from AttributeMessage value
    * Attribute and object create messages use different indexes and the NoHelmet value becomes a YesHelmet value
    */
  def valuesFromAttributeValue(value: Long): Set[Cosmetic] = {
    var values = Set[Cosmetic]()
    if (((value >> 4L) & 1L) == 1L) values += Cosmetic.Beret
    if (((value >> 3L) & 1L) == 1L) values += Cosmetic.Earpiece
    if (((value >> 2L) & 1L) == 1L) values += Cosmetic.Sunglasses
    if (((value >> 1L) & 1L) == 1L) values += Cosmetic.BrimmedCap
    if (((value >> 0L) & 1L) == 0L) values += Cosmetic.NoHelmet
    values
  }

  /** Serialize enum values to AttributeMessage value
    * Attribute and object create messages use different indexes and the NoHelmet value becomes a YesHelmet value
    */
  def valuesToAttributeValue(values: Set[Cosmetic]): Long = {
    values.foldLeft(1) {
      case (sum, NoHelmet)   => sum - 1
      case (sum, BrimmedCap) => sum + 2
      case (sum, Sunglasses) => sum + 4
      case (sum, Earpiece)   => sum + 8
      case (sum, Beret)      => sum + 16
    }
  }

  /** Codec for object create messages */
  implicit val codec: Codec[Set[Cosmetic]] = uint(5).exmap(
    value => Attempt.Successful(Cosmetic.valuesFromObjectCreateValue(value)),
    cosmetics => Attempt.Successful(Cosmetic.valuesToObjectCreateValue(cosmetics))
  )

}
