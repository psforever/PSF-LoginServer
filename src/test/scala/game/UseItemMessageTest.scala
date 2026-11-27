// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.packets.UseItemMessage
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class UseItemMessageTest extends Specification {
  val string = hex"10 eb06 0000 7b01 FFFFFFFF 4001000000000000000000000000058c803600800000" // LongGameplay.gcap #46046

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case UseItemMessage(avatarGUID, itemUsedGUID, objectGUID, actionContext, weaponFireUse, rayStartPos, rayHitPos, pitch, yaw, roll, objectID) =>
        avatarGUID mustEqual PlanetSideGUID(1771)
        itemUsedGUID mustEqual PlanetSideGUID(0)
        objectGUID mustEqual PlanetSideGUID(379)
        actionContext mustEqual 0xFFFFFFFFL
        weaponFireUse mustEqual false
        rayStartPos mustEqual Vector3(5.0f, 0.0f, 0.0f)
        rayHitPos mustEqual Vector3.Zero
        pitch mustEqual 11
        yaw mustEqual 25
        roll mustEqual 0
        objectID mustEqual 364
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = UseItemMessage(
      avatarGUID = PlanetSideGUID(1771),
      itemUsedGUID = PlanetSideGUID(0),
      objectGUID = PlanetSideGUID(379),
      actionContext = 0xFFFFFFFFL,
      weaponFireUse = false,
      rayStartPos = Vector3(5.0f, 0.0f, 0.0f),
      rayHitPos = Vector3.Zero,
      pitch = 11,
      yaw = 25,
      roll = 0,
      objectID = 364
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
