// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.game.EmpireBenefitsMessage.{ZoneLocks, ZoneBenefits}
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class EmpireBenefitsMessage(
                                        entriesA: Vector[ZoneLocks],
                                        entriesB: Vector[ZoneBenefits]
  ) extends PlanetSideGamePacket {
  type Packet = EmpireBenefitsMessage
  def opcode = GamePacketOpcode.EmpireBenefitsMessage
  def encode = EmpireBenefitsMessage.encode(this)
}

object EmpireBenefitsMessage extends Marshallable[EmpireBenefitsMessage] {

  final case class ZoneLocks(
    empire: Int,
    zone: String
  )

  final case class ZoneBenefits(
    empire: Int,
    value: Int
  )

  private implicit val entryACodec: Codec[ZoneLocks] = (
    ("empire" | uint(2)) ::
      ("zone" | PacketHelpers.encodedStringAligned(6))
    ).as[ZoneLocks]

  private implicit val entryBCodec: Codec[ZoneBenefits] = (
    ("empire" | uint(2)) ::
      ("benefit" | uint16L)
    ).as[ZoneBenefits]

  implicit val codec: Codec[EmpireBenefitsMessage] = (
    ("entriesA" | vectorOfN(uint32L.xmap(_.toInt, _.toLong), entryACodec)) ::
      ("entriesB" | vectorOfN(uint32L.xmap(_.toInt, _.toLong), entryBCodec))
    ).as[EmpireBenefitsMessage]
}
