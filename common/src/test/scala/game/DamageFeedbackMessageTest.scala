// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.PlanetSideGUID
import scodec.bits._

class DamageFeedbackMessageTest extends Specification {
  val string = hex"7b 3d842f610b2040000000"

  "decode" in {
    PacketCoding.DecodePacket(string).require match {
      case DamageFeedbackMessage(unk1, unk2, unk2a, unk2b, unk2c, unk3, unk3a, unk3b, unk3c, unk3d, unk4, unk5, unk6) =>
        unk1 mustEqual 3
        unk2 mustEqual true
        unk2a.contains(PlanetSideGUID(2913)) mustEqual true
        unk2b.isEmpty mustEqual true
        unk2c.isEmpty mustEqual true
        unk3 mustEqual true
        unk3a.contains(PlanetSideGUID(2913)) mustEqual true
        unk3b.isEmpty mustEqual true
        unk3c.isEmpty mustEqual true
        unk3d.isEmpty mustEqual true
        unk4 mustEqual 1
        unk5 mustEqual 2
        unk6 mustEqual 0
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, true, Some(PlanetSideGUID(2913)), None, None, None, 1, 2, 0)
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual string
  }

  "assert catches" in {
    //unk2: no parameters
    DamageFeedbackMessage(3, true, None, None, None, true, Some(PlanetSideGUID(2913)), None, None, None, 1, 2, 0) must throwA[AssertionError]
    //unk2: two exclusive parameters
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), Some("error"), None, true, Some(PlanetSideGUID(2913)), None, None, None, 1, 2, 0) must throwA[AssertionError]
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, Some(5), true, Some(PlanetSideGUID(2913)), None, None, None, 1, 2, 0) must throwA[AssertionError]
    DamageFeedbackMessage(3, true, None, Some("error"), Some(5), true, Some(PlanetSideGUID(2913)), None, None, None, 1, 2, 0) must throwA[AssertionError]
    //unk2: all parameters
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), Some("error"), Some(5), true, Some(PlanetSideGUID(2913)), None, None, None, 1, 2, 0) must throwA[AssertionError]
    //unk2: mismatched flag for strings
    DamageFeedbackMessage(3, true, None, None, Some(5), true, Some(PlanetSideGUID(2913)), None, None, None, 1, 2, 0) must throwA[AssertionError]
    DamageFeedbackMessage(3, false, None, Some("error"), None, true, Some(PlanetSideGUID(2913)), None, None, None, 1, 2, 0) must throwA[AssertionError]

    //unk3: no parameters
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, true, None, None, None, None, 1, 2, 0) must throwA[AssertionError]
    //unk3: two exclusive parameters
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, true, Some(PlanetSideGUID(2913)), Some("error"), None, None, 1, 2, 0) must throwA[AssertionError]
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, true, Some(PlanetSideGUID(2913)), None, Some(5), None, 1, 2, 0) must throwA[AssertionError]
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, true, None, Some("error"), Some(5), Some(1), 1, 2, 0) must throwA[AssertionError]
    //unk3: all parameters
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, true, Some(PlanetSideGUID(2913)), Some("error"), Some(5), None, 1, 2, 0) must throwA[AssertionError]
    //unk3: mismatched fields
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, true, Some(PlanetSideGUID(2913)), None, None, Some(5), 1, 2, 0) must throwA[AssertionError]
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, true, None, Some("Error"), None, None, 1, 2, 0) must throwA[AssertionError]
    //unk3: mismatched flag for strings
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, true, None, None, Some(5), None, 1, 2, 0) must throwA[AssertionError]
    DamageFeedbackMessage(3, true, Some(PlanetSideGUID(2913)), None, None, false, None, Some("error"), None, None, 1, 2, 0) must throwA[AssertionError]
  }
}
