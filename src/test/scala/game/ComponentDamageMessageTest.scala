// Copyright (c) 2021 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, SubsystemComponent}
import scodec.bits._

class ComponentDamageMessageTest extends Specification {
  val string_on = hex"d3 8f01 1a000000820000000000202040"
  val string_off = hex"d3 8f01 1a00000000"

  "decode (on)" in {
    PacketCoding.decodePacket(string_on).require match {
      case ComponentDamageMessage(guid, code, data) =>
        guid mustEqual PlanetSideGUID(399)
        code mustEqual SubsystemComponent.WeaponSystemsCOFRecovery
        data match {
          case Some(ComponentDamageField(u2, u3, u4)) =>
            u2 mustEqual 4
            u3 mustEqual 1077936128
            u4 mustEqual true
          case _ =>
            ko
        }
      case _ =>
        ko
    }
  }

  "decode (off)" in {
    PacketCoding.decodePacket(string_off).require match {
      case ComponentDamageMessage(guid, code, data) =>
        guid mustEqual PlanetSideGUID(399)
        code mustEqual SubsystemComponent.WeaponSystemsCOFRecovery
        data.isEmpty mustEqual true
      case _ =>
        ko
    }
  }

  "encode (on)" in {
    val msg = ComponentDamageMessage(
      PlanetSideGUID(399),
      SubsystemComponent.WeaponSystemsCOFRecovery,
      Some(ComponentDamageField(4, 1077936128, unk = true))
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_on
  }

  "encode (off; 1)" in {
    val msg = ComponentDamageMessage(PlanetSideGUID(399), SubsystemComponent.WeaponSystemsCOFRecovery, None)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_off
  }

  "encode (off; 2)" in {
    val msg = ComponentDamageMessage(PlanetSideGUID(399), SubsystemComponent.WeaponSystemsCOFRecovery)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string_off
  }
}

