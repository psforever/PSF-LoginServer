// Copyright (c) 2026
package game.objectcreate

import net.psforever.packet.PacketCoding
import net.psforever.packet.game.ObjectCreateMessage
import net.psforever.packet.game.objectcreate._
import org.specs2.mutable._
import scodec.bits._

class RemoteProjectileRetailDecodeTest extends Specification {
  val meteor  = hex"17 ef000000 912d33d83caad690b89cc0000009696010600190000000008108"
  val wasp    = hex"17 c5000000 f4b39a76f479eab5a5d2b0006294400000000d0400"
  val striker = hex"17 c5000000 a4bc0a8e7089e98ab561300fee3040000666686400"

  "RemoteProjectileData retail decode" should {
    "decode meteor retail payload" in {
      PacketCoding.decodePacket(meteor).require match {
        case ObjectCreateMessage(_, cls, _, _, data) =>
          cls mustEqual ObjectClass.meteor_projectile_b_small
          data match {
            case RemoteProjectileData(_, u1, u2, unk3, unk4, unk5) =>
              (u1, u2, unk3.id, unk4, unk5) mustEqual ((0, 32, 2, 1, 0))
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode wasp retail payload" in {
      PacketCoding.decodePacket(wasp).require match {
        case ObjectCreateMessage(_, cls, _, _, data) =>
          cls mustEqual ObjectClass.wasp_rocket_projectile
          data match {
            case RemoteProjectileData(_, u1, u2, unk3, unk4, unk5) =>
              (u1, u2, unk3.id, unk4, unk5) mustEqual ((0, 208, 0, 0, 0))
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }

    "decode striker retail payload" in {
      PacketCoding.decodePacket(striker).require match {
        case ObjectCreateMessage(_, cls, _, _, data) =>
          cls mustEqual ObjectClass.striker_missile_targeting_projectile
          data match {
            case RemoteProjectileData(_, u1, u2, unk3, unk4, unk5) =>
              (u1, u2, unk3.id, unk4, unk5) mustEqual ((26214, 134, 6, 0, 0))
            case _ =>
              ko
          }
        case _ =>
          ko
      }
    }
  }
}
