// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game.{DeployableIcon, DeployableInfo, DeployableObjectsInfoMessage, DeploymentAction}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class DeployableObjectsInfoMessageTest extends Specification {
  val string = hex"76 00 80 00 00 31 85 41 CF D3 7E B3 34 00 E6 30 48" //this was a TRAP @ Ogma, Forseral

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case DeployableObjectsInfoMessage(action, list) =>
        action mustEqual DeploymentAction.Dismiss
        list.size mustEqual 1
        //0
        list.head.object_guid mustEqual PlanetSideGUID(2659)
        list.head.icon mustEqual DeployableIcon.TRAP
        list.head.pos.x mustEqual 3572.4453f
        list.head.pos.y mustEqual 3277.9766f
        list.head.pos.z mustEqual 114.0f
        list.head.player_guid mustEqual PlanetSideGUID(2502)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DeployableObjectsInfoMessage(
      DeploymentAction.Dismiss,
      DeployableInfo(
        PlanetSideGUID(2659),
        DeployableIcon.TRAP,
        Vector3(3572.4453f, 3277.9766f, 114.0f),
        PlanetSideGUID(2502)
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
