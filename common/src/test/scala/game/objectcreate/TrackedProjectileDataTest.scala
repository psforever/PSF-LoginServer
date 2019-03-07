// Copyright (c) 2017 PSForever
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.{ObjectCreateMessage, PlanetSideGUID}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideEmpire, Vector3}
import org.specs2.mutable._
import scodec.bits._

class TrackedProjectileDataTest extends Specification {
  val string_striker_projectile = hex"17 C5000000 A4B 009D 4C129 0CB0A 9814 00 F5 E3 040000666686400"

  "TrackedProjectileData" should {
    "decode" in {
      PacketCoding.DecodePacket(string_striker_projectile).require match {
        case ObjectCreateMessage(len, cls, guid, parent, data) =>
          len mustEqual 197
          cls mustEqual ObjectClass.striker_missile_targeting_projectile
          guid mustEqual PlanetSideGUID(40192)
          parent.isDefined mustEqual false
          data match {
            case TrackedProjectileData(CommonFieldDataWithPlacement(pos, deploy), unk2, unk3) =>
              pos.coord mustEqual Vector3(4644.5938f, 5472.0938f, 82.375f)
              pos.orient mustEqual Vector3(0, 30.9375f, 171.5625f)
              deploy.faction mustEqual PlanetSideEmpire.TR
              deploy.bops mustEqual false
              deploy.alternate mustEqual false
              deploy.v1 mustEqual true
              deploy.v2.isEmpty mustEqual true
              deploy.v3 mustEqual false
              deploy.v4.isEmpty mustEqual true
              deploy.v5.isEmpty mustEqual true
              deploy.guid mustEqual PlanetSideGUID(0)

              unk2 mustEqual TrackedProjectile.Striker

              unk3 mustEqual 0
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "encode" in {
      val obj = TrackedProjectileData(
        CommonFieldDataWithPlacement(
          PlacementData(4644.5938f, 5472.0938f, 82.375f, 0f, 30.9375f, 171.5625f),
          CommonFieldData(PlanetSideEmpire.TR, false, false, true, None, false, None, None, PlanetSideGUID(0))
        ),
        TrackedProjectile.Striker,
        0
      )
      val msg = ObjectCreateMessage(ObjectClass.striker_missile_targeting_projectile, PlanetSideGUID(40192), obj)
      val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

      pkt.toBitVector.take(132) mustEqual string_striker_projectile.toBitVector.take(132)
      pkt.toBitVector.drop(133).take(7) mustEqual string_striker_projectile.toBitVector.drop(133).take(7)
      pkt.toBitVector.drop(141) mustEqual string_striker_projectile.toBitVector.drop(141)
    }
  }
}
