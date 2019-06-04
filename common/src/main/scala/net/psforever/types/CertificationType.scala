// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

import scala.annotation.tailrec
/**
  * An `Enumeration` of the available certifications.<br>
  * <br>
  * As indicated, the following certifications are always enqueued on an avatar's permissions:
  * `StandardAssault`, `StandardExoSuit`, `AgileExoSuit`.
  * They must still be included in any formal lists of permitted equipment for a user.
  * The other noted certifications require all prerequisite certifications listed or they themselves will not be listed:
  * `ElectronicsExpert` and `AdvancedEngineering`.
  * No other certification requires its prerequisites explicitly listed to be listed itself.
  * Any certification that contains multiple other certifications overrides those individual certifications in the list.
  * There is no certification for the Advanced Nanite Transport.<br>
  * <br>
  * In terms of pricing, `StandardAssault`, `StandardExoSuit`, and `AgileExoSuit` are costless.
  * A certification that contains multiple other certifications acts as the overriding cost.
  * (Taking `UniMAX` while owning `AAMAX` will refund the `AAMAX` cost and replace it with the `UniMAX` cost.)
  */
object CertificationType extends Enumeration {
  type Type = Value
  val
  //0
  StandardAssault, //always listed
  MediumAssault,
  HeavyAssault,
  SpecialAssault,
  AntiVehicular,
  Sniping,
  EliteAssault,
  AirCavalryScout,
  AirCavalryInterceptor,
  AirCavalryAssault,
  //10
  AirSupport,
  ATV,
  LightScout,
  AssaultBuggy,
  ArmoredAssault1,
  ArmoredAssault2,
  GroundTransport,
  GroundSupport,
  BattleFrameRobotics,
  Flail,
  //20
  Switchblade,
  Harasser,
  Phantasm,
  GalaxyGunship,
  BFRAntiAircraft,
  BFRAntiInfantry,
  StandardExoSuit, //always listed
  AgileExoSuit, //always listed
  ReinforcedExoSuit,
  InfiltrationSuit,
  //30
  AAMAX,
  AIMAX,
  AVMAX,
  UniMAX,
  Medical,
  AdvancedMedical,
  Hacking,
  AdvancedHacking,
  ExpertHacking,
  DataCorruption,
  //40
  ElectronicsExpert, //requires Hacking and AdvancedHacking
  Engineering,
  CombatEngineering,
  FortificationEngineering,
  AssaultEngineering,
  AdvancedEngineering //requires Engineering and CombatEngineering
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)

  /**
    * Certifications are often stored, in object form, as a 46-member collection.
    * Encode a subset of certification values for packet form.
    * @see `ChangeSquadMemberRequirementsCertifications`
    * @see `changeSquadMemberRequirementsCertificationsCodec`
    * @param certs the certifications, as a sequence of values
    * @return the certifications, as a single value
    */
  def toEncodedLong(certs : Set[CertificationType.Value]) : Long = {
    certs
      .map{ cert => math.pow(2, cert.id).toLong }
      .foldLeft(0L)(_ + _)
  }

  /**
    * Certifications are often stored, in packet form, as an encoded little-endian `46u` value.
    * Decode a representative value into a subset of certification values.
    * @see `ChangeSquadMemberRequirementsCertifications`
    * @see `changeSquadMemberRequirementsCertificationsCodec`
    * @see `fromEncodedLong(Long, Iterable[Long], Set[CertificationType.Value])`
    * @param certs the certifications, as a single value
    * @return the certifications, as a sequence of values
    */
  def fromEncodedLong(certs : Long) : Set[CertificationType.Value] = {
    recursiveFromEncodedLong(
      certs,
      CertificationType.values.map{ cert => math.pow(2, cert.id).toLong }.toSeq.sorted
    )
  }

  /**
    * Certifications are often stored, in packet form, as an encoded little-endian `46u` value.
    * Decode a representative value into a subset of certification values
    * by repeatedly finding the partition point of values less than a specific one,
    * providing for both the next lowest value (to subtract) and an index (of a certification).
    * @see `ChangeSquadMemberRequirementsCertifications`
    * @see `changeSquadMemberRequirementsCertificationsCodec`
    * @see `fromEncodedLong(Long)`
    * @param certs the certifications, as a single value
    * @param splitList the available values to partition
    * @param out the accumulating certification values;
    *            defaults to an empty set
    * @return the certifications, as a sequence of values
    */
  @tailrec
  private def recursiveFromEncodedLong(certs : Long, splitList : Iterable[Long], out : Set[CertificationType.Value] = Set.empty) : Set[CertificationType.Value] = {
    if(certs == 0 || splitList.isEmpty) {
      out
    }
    else {
      val (less, _) = splitList.partition(_ <= certs)
      recursiveFromEncodedLong(certs - less.last, less, out ++ Set(CertificationType(less.size - 1)))
    }
  }
}
