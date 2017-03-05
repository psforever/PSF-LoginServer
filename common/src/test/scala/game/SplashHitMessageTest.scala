// Copyright (c) 2016 PSForever.net to present
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class SplashHitMessageTest extends Specification {
  val string = hex"62 7129e72b0c1dd1516ec58000051e01d8371f0100000025803616bb2a9ae50b000008889d00644bdd35454c45c000000400"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case SplashHitMessage(unk1, projectile_uid, projectile_pos, unk2, unk3, projectile_vel, unk4, targets) =>
        unk1 mustEqual 113
        projectile_uid mustEqual PlanetSideGUID(40103)
        projectile_pos.x mustEqual 3681.3438f
        projectile_pos.y mustEqual 2728.9062f
        projectile_pos.z mustEqual 90.921875f
        unk2 mustEqual 0
        unk3 mustEqual 0
        projectile_vel.isDefined mustEqual true
        projectile_vel.get.x mustEqual 2.21875f
        projectile_vel.get.y mustEqual 0.90625f
        projectile_vel.get.z mustEqual -1.125f
        unk4.isDefined mustEqual false
        targets.size mustEqual 2
        //0
        targets.head.uid mustEqual PlanetSideGUID(75)
        targets.head.pos.x mustEqual 3674.8438f
        targets.head.pos.y mustEqual 2726.789f
        targets.head.pos.z mustEqual 91.15625f
        targets.head.unk1 mustEqual 286326784L
        targets.head.unk2.isDefined mustEqual false
        //1
        targets(1).uid mustEqual PlanetSideGUID(372)
        targets(1).pos.x mustEqual 3679.1328f
        targets(1).pos.y mustEqual 2722.6016f
        targets(1).pos.z mustEqual 92.765625f
        targets(1).unk1 mustEqual 268435456L
        targets(1).unk2.isDefined mustEqual false
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = SplashHitMessage(113, PlanetSideGUID(40103),
      Vector3(3681.3438f, 2728.9062f, 90.921875f), 0, 0,
      Some(Vector3(2.21875f, 0.90625f, -1.125f)), None,
        SplashedTarget(PlanetSideGUID(75), Vector3(3674.8438f, 2726.789f, 91.15625f), 286326784L, None) ::
        SplashedTarget(PlanetSideGUID(372), Vector3(3679.1328f, 2722.6016f, 92.765625f), 268435456L, None) ::
        Nil
    )
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
