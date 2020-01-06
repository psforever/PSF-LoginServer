// Copyright (c) 2017 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{LoadoutType, PlanetSideGUID}
import scodec.bits._

class FavoritesRequestTest extends Specification {
  val stringInfantry = hex"5E 4B00 1187 4500 7800 6100 6D00 7000 6C00 6500"

  "decode (for infantry)" in {
    PacketCoding.DecodePacket(stringInfantry).require match {
      case FavoritesRequest(player_guid, list, action, line, label) =>
        player_guid mustEqual PlanetSideGUID(75)
        list mustEqual LoadoutType.Infantry
        action mustEqual FavoritesAction.Save
        line mustEqual 1
        label.isDefined mustEqual true
        label.get mustEqual "Example"
      case _ =>
        ko
    }
  }

  "encode (for infantry)" in {
    val msg = FavoritesRequest(PlanetSideGUID(75), LoadoutType.Infantry, FavoritesAction.Save, 1, Some("Example"))
    val pkt = PacketCoding.EncodePacket(msg).require.toByteVector

    pkt mustEqual stringInfantry
  }
}

