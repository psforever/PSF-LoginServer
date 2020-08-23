package net.psforever.objects.avatar

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.PacketHelpers
import scodec.Codec
import scodec.codecs._

import scala.annotation.tailrec

sealed abstract class Certification(
    val value: Int,
    /** Name used in packets */
    val name: String,
    /** Certification point cost */
    val cost: Int,
    val requires: Set[Certification] = Set(),
    val replaces: Set[Certification] = Set()
) extends IntEnumEntry

case object Certification extends IntEnum[Certification] {

  case object StandardAssault extends Certification(value = 0, name = "standard_assault", cost = 0)

  case object MediumAssault extends Certification(value = 1, name = "medium_assault", cost = 2)

  case object HeavyAssault
      extends Certification(value = 2, name = "heavy_assault", cost = 4, requires = Set(MediumAssault))

  case object SpecialAssault
      extends Certification(value = 3, name = "special_assault", cost = 3, requires = Set(MediumAssault))

  case object AntiVehicular
      extends Certification(value = 4, name = "anti_vehicular", cost = 3, requires = Set(MediumAssault))

  case object Sniping extends Certification(value = 5, name = "sniper", cost = 3, requires = Set(MediumAssault))

  case object EliteAssault
      extends Certification(value = 6, name = "special_assault_2", cost = 1, requires = Set(SpecialAssault))

  case object AirCavalryScout extends Certification(value = 7, name = "air_cavalry_scout", cost = 3)

  case object AirCavalryInterceptor
      extends Certification(value = 8, name = "air_cavalry_interceptor", cost = 2, requires = Set(AirCavalryScout))

  case object AirCavalryAssault
      extends Certification(
        value = 9,
        name = "air_cavalry_assault",
        cost = 2,
        requires = Set(AirCavalryScout)
      )

  case object AirSupport extends Certification(value = 10, name = "air_support", cost = 3)

  case object ATV extends Certification(value = 11, name = "quad_all", cost = 1)

  case object LightScout
      extends Certification(
        value = 12,
        name = "light_scout",
        cost = 5,
        replaces = Set(AirCavalryScout, AssaultBuggy, Harasser)
      )

  case object AssaultBuggy extends Certification(value = 13, name = "assault_buggy", cost = 3, replaces = Set(Harasser))

  case object ArmoredAssault1 extends Certification(value = 14, name = "armored_assault1", cost = 2)

  case object ArmoredAssault2
      extends Certification(value = 15, name = "armored_assault2", cost = 3, requires = Set(ArmoredAssault1))

  case object GroundTransport extends Certification(value = 16, name = "ground_transport", cost = 2)

  case object GroundSupport extends Certification(value = 17, name = "ground_support", cost = 2)

  case object BattleFrameRobotics
      extends Certification(value = 18, name = "TODO2", cost = 4, requires = Set(ArmoredAssault2)) // TODO name

  case object Flail extends Certification(value = 19, name = "flail", cost = 1, requires = Set(ArmoredAssault2))

  case object Switchblade extends Certification(value = 20, name = "switchblade", cost = 1, requires = Set(ATV))

  case object Harasser extends Certification(value = 21, name = "harasser", cost = 1)

  case object Phantasm extends Certification(value = 22, name = "phantasm", cost = 3, requires = Set(InfiltrationSuit))

  case object GalaxyGunship extends Certification(value = 23, name = "gunship", cost = 2, requires = Set(AirSupport))

  case object BFRAntiAircraft
      extends Certification(value = 24, name = "TODO3", cost = 1, requires = Set(BattleFrameRobotics))

  case object BFRAntiInfantry
      extends Certification(value = 25, name = "TODO4", cost = 1, requires = Set(BattleFrameRobotics)) // TODO name

  case object StandardExoSuit extends Certification(value = 26, name = "TODO5", cost = 0)

  case object AgileExoSuit extends Certification(value = 27, name = "agile_armor", cost = 0)

  case object ReinforcedExoSuit extends Certification(value = 28, name = "reinforced_armor", cost = 3)

  case object InfiltrationSuit extends Certification(value = 29, name = "infiltration_suit", cost = 2)

  case object AAMAX extends Certification(value = 30, name = "max_anti_aircraft", cost = 2)

  case object AIMAX extends Certification(value = 31, name = "max_anti_personnel", cost = 3)

  case object AVMAX extends Certification(value = 32, name = "max_anti_vehicular", cost = 3)

  case object UniMAX extends Certification(value = 33, name = "max_all", cost = 6, replaces = Set(AAMAX, AIMAX, AVMAX))

  case object Medical extends Certification(value = 34, name = "Medical", cost = 3)

  case object AdvancedMedical
      extends Certification(value = 35, name = "advanced_medical", cost = 2, requires = Set(Medical))

  case object Hacking extends Certification(value = 36, name = "Hacking", cost = 3)

  case object AdvancedHacking
      extends Certification(value = 37, name = "advanced_hacking", cost = 2, requires = Set(Hacking))

  case object ExpertHacking
      extends Certification(value = 38, name = "expert_hacking", cost = 2, requires = Set(AdvancedHacking))

  case object DataCorruption
      extends Certification(value = 39, name = "virus_hacking", cost = 3, requires = Set(AdvancedHacking))

  case object ElectronicsExpert
      extends Certification(
        value = 40,
        name = "electronics_expert",
        cost = 4,
        requires = Set(AdvancedHacking),
        replaces = Set(DataCorruption, ExpertHacking)
      )

  case object Engineering extends Certification(value = 41, name = "Repair", cost = 3)

  case object CombatEngineering
      extends Certification(value = 42, name = "combat_engineering", cost = 2, requires = Set(Engineering))

  case object FortificationEngineering
      extends Certification(value = 43, name = "ce_defense", cost = 3, requires = Set(CombatEngineering))

  case object AssaultEngineering
      extends Certification(value = 44, name = "ce_offense", cost = 3, requires = Set(CombatEngineering))

  case object AdvancedEngineering
      extends Certification(
        value = 45,
        name = "ce_advanced",
        cost = 5,
        requires = Set(CombatEngineering),
        replaces = Set(AssaultEngineering, FortificationEngineering)
      )

  // https://github.com/lloydmeta/enumeratum/issues/86
  lazy val values: IndexedSeq[Certification] = findValues

  implicit val codec: Codec[Certification] = PacketHelpers.createIntEnumCodec(this, uint8L)

  /**
    * Certifications are often stored, in object form, as a 46-member collection.
    * Encode a subset of certification values for packet form.
    *
    * @return the certifications, as a single value
    */
  def toEncodedLong(certs: Set[Certification]): Long = {
    certs
      .map { cert => math.pow(2, cert.value).toLong }
      .foldLeft(0L)(_ + _)
  }

  /**
    * Certifications are often stored, in packet form, as an encoded little-endian `46u` value.
    * Decode a representative value into a subset of certification values.
    *
    * @see `ChangeSquadMemberRequirementsCertifications`
    * @see `changeSquadMemberRequirementsCertificationsCodec`
    * @see `fromEncodedLong(Long, Iterable[Long], Set[CertificationType.Value])`
    * @param certs the certifications, as a single value
    * @return the certifications, as a sequence of values
    */
  def fromEncodedLong(certs: Long): Set[Certification] = {
    recursiveFromEncodedLong(
      certs,
      Certification.values.map { cert => math.pow(2, cert.value).toLong }.sorted
    )
  }

  /**
    * Certifications are often stored, in packet form, as an encoded little-endian `46u` value.
    * Decode a representative value into a subset of certification values
    * by repeatedly finding the partition point of values less than a specific one,
    * providing for both the next lowest value (to subtract) and an index (of a certification).
    *
    * @see `ChangeSquadMemberRequirementsCertifications`
    * @see `changeSquadMemberRequirementsCertificationsCodec`
    * @see `fromEncodedLong(Long)`
    * @param certs     the certifications, as a single value
    * @param splitList the available values to partition
    * @param out       the accumulating certification values;
    *                  defaults to an empty set
    * @return the certifications, as a sequence of values
    */
  @tailrec
  private def recursiveFromEncodedLong(
      certs: Long,
      splitList: Iterable[Long],
      out: Set[Certification] = Set.empty
  ): Set[Certification] = {
    if (certs == 0 || splitList.isEmpty) {
      out
    } else {
      val (less, _) = splitList.partition(_ <= certs)
      recursiveFromEncodedLong(certs - less.last, less, out ++ Set(Certification.withValue(less.size - 1)))
    }
  }

}
