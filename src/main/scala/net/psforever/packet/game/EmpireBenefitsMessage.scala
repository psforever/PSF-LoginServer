// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.game.EmpireBenefitsMessage.{ZoneBenefit, ZoneLock}
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideEmpire
import scodec.Codec
import scodec.codecs._

import scala.language.implicitConversions

/**
  * EmpireBenefitsMessage
  *
  * zoneLocks gives the client information about which empire locks what continent.
  * This produces a chat message.
  * zoneBenefits tells the client what empire has which benefits enabled.
  * This has to match zoneLocks to work properly.
  */
final case class EmpireBenefitsMessage(
    zoneLocks: Vector[ZoneLock],
    zoneBenefits: Vector[ZoneBenefit]
  ) extends PlanetSideGamePacket {
  type Packet = EmpireBenefitsMessage
  def opcode = GamePacketOpcode.EmpireBenefitsMessage
  def encode = EmpireBenefitsMessage.encode(this)
}

object EmpireBenefitsMessage extends Marshallable[EmpireBenefitsMessage] {

  /**
    * ZoneLockZone
    *
    * Available Types of Zones
    *
    * These zones can be used to notify the client of a lock.
    */
  object ZoneLockZone extends Enumeration {
    type Type = String

    val i1: ZoneLockZone.Value =          Value("lock-i1")          // Extinction Continental Lock
    val i2: ZoneLockZone.Value =          Value("lock-i2")          // Ascension Continental Lock
    val i3: ZoneLockZone.Value =          Value("lock-i3")          // Desolation Continental Lock
    val i4: ZoneLockZone.Value =          Value("lock-i4")          // Nexus Continental Lock
    val i1_i2_i3_i4: ZoneLockZone.Value = Value("lock-i1-i2-i3-i4") // Oshur Cluster Lock
    val z3: ZoneLockZone.Value =          Value("lock-z3")          // Cyssor Continental Lock
    val z4: ZoneLockZone.Value =          Value("lock-z4")          // Ishundar Continental Lock
    val z9: ZoneLockZone.Value =          Value("lock-z9")          // Searhus Continental Lock
    val tr_homes: ZoneLockZone.Value =    Value("lock-tr-homes")    // TR Home Continent Lock
    val nc_homes: ZoneLockZone.Value =    Value("lock-nc-homes")    // NC Home Continent Lock
    val vs_homes: ZoneLockZone.Value =    Value("lock-vs-homes")    // VS Home Continent Lock

    implicit def valueToType(v: ZoneLockZone.Value): Type = v.toString
    implicit val codec: Codec[Type] = PacketHelpers.encodedStringAligned(6)
  }

  /**
    * ZoneLockBenefit
    *
    * Available Types of Benefits
    *
    * Benefits 0, 2 and 5 are unknown. Benefits for i1 to i4 are unknown and mapped incorrectly here.
    */
  object ZoneLockBenefit extends Enumeration {
    type Type = Value

    val i1: ZoneLockBenefit.Value =          Value(-1)          // Extinction Continental Lock
    val i2: ZoneLockBenefit.Value =          Value(-2)          // Ascension Continental Lock
    val i3: ZoneLockBenefit.Value =          Value(-3)          // Desolation Continental Lock
    val i4: ZoneLockBenefit.Value =          Value(-4)          // Nexus Continental Lock

    // val unk0: ZoneLockBenefit.Value =     Value(0)
    val z4: ZoneLockBenefit.Value =          Value(1) // Ishundar Continental Lock
    // val unk2: ZoneLockBenefit.Value =     Value(2)
    val z9: ZoneLockBenefit.Value =          Value(3) // Searhus Continental Lock
    val i1_i2_i3_i4: ZoneLockBenefit.Value = Value(4) // Oshur Cluster Lock
    // val unk5: ZoneLockBenefit.Value =     Value(5)
    val z3: ZoneLockBenefit.Value =          Value(6) // Cyssor Continental Lock
    val tr_homes: ZoneLockBenefit.Value =    Value(7) // TR Home Continent Lock
    val nc_homes: ZoneLockBenefit.Value =    Value(8) // NC Home Continent Lock
    val vs_homes: ZoneLockBenefit.Value =    Value(9) // VS Home Continent Lock

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uint16L)
  }

  final case class ZoneLock(
    empire: PlanetSideEmpire.Type,
    zone: ZoneLockZone.Type,
  )

  final case class ZoneBenefit(
    empire: PlanetSideEmpire.Type,
    value: ZoneLockBenefit.Type
  )

  private implicit val zoneLockCodec: Codec[ZoneLock] = (
    ("empire" | PlanetSideEmpire.codec) ::
      ("zone" | ZoneLockZone.codec)
    ).as[ZoneLock]

  private implicit val zoneBenefitCodec: Codec[ZoneBenefit] = (
    ("empire" | PlanetSideEmpire.codec) ::
      ("benefit" | ZoneLockBenefit.codec)
    ).as[ZoneBenefit]

  implicit val codec: Codec[EmpireBenefitsMessage] = (
    ("zoneLocks" | vectorOfN(uint32L.xmap(_.toInt, _.toLong), zoneLockCodec)) ::
      ("zoneBenefits" | vectorOfN(uint32L.xmap(_.toInt, _.toLong), zoneBenefitCodec))
    ).as[EmpireBenefitsMessage]
}
