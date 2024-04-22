// Copyright (c) 2023 PSForever
package game

import net.psforever.packet._
import net.psforever.packet.game.OutfitEvent.RequestType
import net.psforever.packet.game.OutfitEventAction._
import net.psforever.packet.game._
import org.specs2.mutable._
import scodec.bits._

class OutfitEventTest extends Specification {
  val created_ABC   = hex"8f 4 0201 feff 2e 0 50006c0061006e006500740053006900640065005f0046006f00720065007600650072005f00560061006e007500 00000000 00000000 0100 0000 80 80 80 80 80 80 80 80 80 0070 4982 00000000 00000000 00000000 00000000 0000"


  "decode CreatedOutfit ABC" in {
    PacketCoding.decodePacket(created_ABC).require match {
      case OutfitEvent(request_type, unk1, action) =>
        request_type mustEqual RequestType.CreatedOutfit
        unk1 mustEqual 258
        action mustEqual CreatedOutfit(unk2 = 254, unk3 = 15, unk4 = 7, outfit_name = "PlanetSide_Forever_Vanu", unk6 = 0, unk7 = 0, members = 1, unk9 = 0, "", "", "", "", "", "", "", "", "", 28672, 33353, 0, 0, 0, 0, 0)
      case _ =>
        ko
    }
  }

  "encode CreatedOutfit ABC" in {
    val msg = OutfitEvent(RequestType.CreatedOutfit, 258, CreatedOutfit(unk2 = 254, unk3 = 15, unk4 = 7, outfit_name = "PlanetSide_Forever_Vanu", unk6 = 0, unk7 = 0, members = 1, unk9 = 0, "", "", "", "", "", "", "", "", "", 28672, 33353, 0, 0, 0, 0, 0))
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual created_ABC
  }
}
