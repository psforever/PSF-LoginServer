// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.EmpireBenefitsMessage.{ZoneLocks, ZoneBenefits}
import net.psforever.packet.game.{EmpireBenefitsMessage, OutfitEvent}
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

  val sample1_expectedLocks = Vector(
    ZoneLocks(0, "lock-z3"),
    ZoneLocks(0, "lock-z4"),
    ZoneLocks(1, "lock-i1-i2-i3-i4"),
    ZoneLocks(2, "lock-z9")
  )

  val sample1_expectedBenefits = Vector(
    ZoneBenefits(0, 1),
    ZoneBenefits(0, 6),
    ZoneBenefits(1, 4),
    ZoneBenefits(2, 3)
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
      entriesA = sample1_expectedLocks,
      entriesB = sample1_expectedBenefits
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual sample1
  }
}
