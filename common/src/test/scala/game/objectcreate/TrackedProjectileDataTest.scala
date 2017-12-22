// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate.{ObjectClass, PlacementData, TrackedProjectileData}
import org.specs2.mutable._
import scodec.bits._

class TrackedProjectileDataTest extends Specification {
  val string_striker_projectile = hex"17 C5000000 A4B 009D 4C129 0CB0A 9814 00 F5 E3 040000666686400"

  "TrackedProjectileData" should {
    "decode (striker projectile)" in {
      PacketCoding.DecodePacket(string_striker_projectile).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 197
          cls mustEqual ObjectClass.striker_missile_targeting_projectile
          guid mustEqual PlanetSideGUID(40192)
          parent.isDefined mustEqual false
          data.isDefined mustEqual true
          data.get.isInstanceOf[TrackedProjectileData] mustEqual true
          val projectile = data.get.asInstanceOf[TrackedProjectileData]
          projectile.pos.coord.x mustEqual 4644.5938f
          projectile.pos.coord.y mustEqual 5472.0938f
          projectile.pos.coord.z mustEqual 82.375f
          projectile.pos.orient.x mustEqual 0f
          projectile.pos.orient.y mustEqual 30.9375f
          projectile.pos.orient.z mustEqual 171.5625f
          projectile.unk1 mustEqual 0
          projectile.unk2 mustEqual TrackedProjectileData.striker_missile_targetting_projectile_data
        case _ =>
          ko
      }
    }

    "encode (striker projectile)" in {
      val obj = TrackedProjectileData.striker(
        PlacementData(4644.5938f, 5472.0938f, 82.375f, 0f, 30.9375f, 171.5625f),
        0
      )
      val msg = ObjectCreateMessage(ObjectClass.striker_missile_targeting_projectile, PlanetSideGUID(40192), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt.toBitVector.take(132) mustEqual string_striker_projectile.toBitVector.take(132)
      pkt.toBitVector.drop(133).take(7) mustEqual string_striker_projectile.toBitVector.drop(133).take(7)
      pkt.toBitVector.drop(141) mustEqual string_striker_projectile.toBitVector.drop(141)
    }

    "hunter_seeker" in {
      TrackedProjectileData.hunter_seeker(PlacementData(0f, 0f, 0f), 0) mustEqual
        TrackedProjectileData(PlacementData(0f, 0f, 0f), 0, TrackedProjectileData.hunter_seeker_missile_projectile_data)
    }

    "oicw" in {
      TrackedProjectileData.oicw(PlacementData(0f, 0f, 0f), 0) mustEqual
        TrackedProjectileData(PlacementData(0f, 0f, 0f), 0, TrackedProjectileData.oicw_projectile_data)
    }

    "starfire" in {
      TrackedProjectileData.starfire(PlacementData(0f, 0f, 0f), 0) mustEqual
        TrackedProjectileData(PlacementData(0f, 0f, 0f), 0, TrackedProjectileData.starfire_projectile_data)
    }

    "striker" in {
      TrackedProjectileData.striker(PlacementData(0f, 0f, 0f), 0) mustEqual
        TrackedProjectileData(PlacementData(0f, 0f, 0f), 0, TrackedProjectileData.striker_missile_targetting_projectile_data)
    }
  }
}
