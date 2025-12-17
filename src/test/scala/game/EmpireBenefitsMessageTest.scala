// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.EmpireBenefitsMessage
import net.psforever.packet.game.EmpireBenefitsMessage.{ZoneBenefit, ZoneLock, ZoneLockBenefit, ZoneLockZone}
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable._
import scodec.bits._

class EmpireBenefitsMessageTest extends Specification {

  val sample1: ByteVector = ByteVector.fromValidHex(
    "d7" + // header
    "04000000" + // count uint32L
    "21c06c6f636b2d7a3321c06c6f636b2d7a3464006c6f636b2d69312d69322d69332d6934a1c06c6f636b2d7a39" +
    "04000000" + // count uint32L
    "004000600410020300"
  )

  val sample2: ByteVector = ByteVector.fromValidHex(
    "d7" +
    "05000000 23406c6f636b2d76732d686f6d657321c06c6f636b2d7a3321c06c6f636b2d7a3461c06c6f636b2d7a3964006c6f636b2d69312d69322d69332d6934" +
    "05000000 004000600024010300410000"
  )

  val sample3: ByteVector = ByteVector.fromValidHex(
    "d7" +
    "05000000 21c06c6f636b2d7a3321c06c6f636b2d7a3423406c6f636b2d6e632d686f6d657361c06c6f636b2d7a39a4006c6f636b2d69312d69322d69332d6934" +
    "05000000 004000600020010300810000"
  )

  val sample4: ByteVector = ByteVector.fromValidHex(
    "d7" +
    "06000000 a3406c6f636b2d6e632d686f6d6573a3406c6f636b2d74722d686f6d6573a1c06c6f636b2d7a33a1c06c6f636b2d7a34a1c06c6f636b2d7a39a4006c6f636b2d69312d69322d69332d6934" +
    "06000000 80402030081002060081c0208000"
  )

  private val sample1_expectedLocks = Vector(
    ZoneLock(PlanetSideEmpire.TR, ZoneLockZone.z3),
    ZoneLock(PlanetSideEmpire.TR, ZoneLockZone.z4),
    ZoneLock(PlanetSideEmpire.NC, ZoneLockZone.i1_i2_i3_i4),
    ZoneLock(PlanetSideEmpire.VS, ZoneLockZone.z9)
  )

  private val sample1_expectedBenefits = Vector(
    ZoneBenefit(PlanetSideEmpire.TR, ZoneLockBenefit.z4),
    ZoneBenefit(PlanetSideEmpire.TR, ZoneLockBenefit.z3),
    ZoneBenefit(PlanetSideEmpire.NC, ZoneLockBenefit.i1_i2_i3_i4),
    ZoneBenefit(PlanetSideEmpire.VS, ZoneLockBenefit.z9)
  )

  private val sample2_expectedLocks = Vector(
    ZoneLock(PlanetSideEmpire.TR, ZoneLockZone.vs_homes),
    ZoneLock(PlanetSideEmpire.TR, ZoneLockZone.z3),
    ZoneLock(PlanetSideEmpire.TR, ZoneLockZone.z4),
    ZoneLock(PlanetSideEmpire.NC, ZoneLockZone.z9),
    ZoneLock(PlanetSideEmpire.NC, ZoneLockZone.i1_i2_i3_i4)
  )

  private val sample2_expectedBenefits = Vector(
    ZoneBenefit(PlanetSideEmpire.TR, ZoneLockBenefit.z4),
    ZoneBenefit(PlanetSideEmpire.TR, ZoneLockBenefit.z3),
    ZoneBenefit(PlanetSideEmpire.TR, ZoneLockBenefit.vs_homes),
    ZoneBenefit(PlanetSideEmpire.NC, ZoneLockBenefit.z9),
    ZoneBenefit(PlanetSideEmpire.NC, ZoneLockBenefit.i1_i2_i3_i4)
  )

  private val sample3_expectedLocks = Vector(
    ZoneLock(PlanetSideEmpire.TR, ZoneLockZone.z3),
    ZoneLock(PlanetSideEmpire.TR, ZoneLockZone.z4),
    ZoneLock(PlanetSideEmpire.TR, ZoneLockZone.nc_homes),
    ZoneLock(PlanetSideEmpire.NC, ZoneLockZone.z9),
    ZoneLock(PlanetSideEmpire.VS, ZoneLockZone.i1_i2_i3_i4)
  )

  private val sample3_expectedBenefits = Vector(
    ZoneBenefit(PlanetSideEmpire.TR, ZoneLockBenefit.z4),
    ZoneBenefit(PlanetSideEmpire.TR, ZoneLockBenefit.z3),
    ZoneBenefit(PlanetSideEmpire.TR, ZoneLockBenefit.nc_homes),
    ZoneBenefit(PlanetSideEmpire.NC, ZoneLockBenefit.z9),
    ZoneBenefit(PlanetSideEmpire.VS, ZoneLockBenefit.i1_i2_i3_i4)
  )

  private val sample4_expectedLocks = Vector(
    ZoneLock(PlanetSideEmpire.VS, ZoneLockZone.nc_homes),
    ZoneLock(PlanetSideEmpire.VS, ZoneLockZone.tr_homes),
    ZoneLock(PlanetSideEmpire.VS, ZoneLockZone.z3),
    ZoneLock(PlanetSideEmpire.VS, ZoneLockZone.z4),
    ZoneLock(PlanetSideEmpire.VS, ZoneLockZone.z9),
    ZoneLock(PlanetSideEmpire.VS, ZoneLockZone.i1_i2_i3_i4)
  )

  private val sample4_expectedBenefits = Vector(
    ZoneBenefit(PlanetSideEmpire.VS, ZoneLockBenefit.z4),
    ZoneBenefit(PlanetSideEmpire.VS, ZoneLockBenefit.z9),
    ZoneBenefit(PlanetSideEmpire.VS, ZoneLockBenefit.i1_i2_i3_i4),
    ZoneBenefit(PlanetSideEmpire.VS, ZoneLockBenefit.z3),
    ZoneBenefit(PlanetSideEmpire.VS, ZoneLockBenefit.tr_homes),
    ZoneBenefit(PlanetSideEmpire.VS, ZoneLockBenefit.nc_homes)
  )

  "decode sample1" in {
    PacketCoding.decodePacket(sample1).require match {
      case EmpireBenefitsMessage(a, b) =>
        a mustEqual sample1_expectedLocks
        b mustEqual sample1_expectedBenefits
      case _ =>
        ko
    }
  }

  "encode sample1" in {
    val msg = EmpireBenefitsMessage(
      zoneLocks = sample1_expectedLocks,
      zoneBenefits = sample1_expectedBenefits
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample1
  }

  "decode sample2" in {
    PacketCoding.decodePacket(sample2).require match {
      case EmpireBenefitsMessage(a, b) =>
        a mustEqual sample2_expectedLocks
        b mustEqual sample2_expectedBenefits
      case _ =>
        ko
    }
  }

  "encode sample2" in {
    val msg = EmpireBenefitsMessage(
      zoneLocks = sample2_expectedLocks,
      zoneBenefits = sample2_expectedBenefits
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample2
  }

  "decode sample3" in {
    PacketCoding.decodePacket(sample3).require match {
      case EmpireBenefitsMessage(a, b) =>
        a mustEqual sample3_expectedLocks
        b mustEqual sample3_expectedBenefits
      case _ =>
        ko
    }
  }

  "encode sample3" in {
    val msg = EmpireBenefitsMessage(
      zoneLocks = sample3_expectedLocks,
      zoneBenefits = sample3_expectedBenefits
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample3
  }

  "decode sample4" in {
    PacketCoding.decodePacket(sample4).require match {
      case EmpireBenefitsMessage(a, b) =>
        a mustEqual sample4_expectedLocks
        b mustEqual sample4_expectedBenefits
      case _ =>
        ko
    }
  }

  "encode sample4" in {
    val msg = EmpireBenefitsMessage(
      zoneLocks = sample4_expectedLocks,
      zoneBenefits = sample4_expectedBenefits
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample4
  }
}
