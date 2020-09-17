// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.Vector3
import scodec.bits._

class TriggerSoundMessageTest extends Specification {
  val string = hex"6B 1FD5E1B466DB3858F1FC"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case TriggerSoundMessage(sound, pos, unk2, volume) =>
        sound mustEqual TriggeredSound.HackDoor
        pos.x mustEqual 1913.9531f
        pos.y mustEqual 6042.8125f
        pos.z mustEqual 45.609375f
        unk2 mustEqual 30
        volume mustEqual 0.49803925f
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = TriggerSoundMessage(TriggeredSound.HackDoor, Vector3(1913.9531f, 6042.8125f, 45.609375f), 30, 0.49803925f)
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual string
  }
}
