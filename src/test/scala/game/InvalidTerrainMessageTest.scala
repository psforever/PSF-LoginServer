// Copyright (c) 2021 PSForever
package game

import org.specs2.mutable._
import net.psforever.packet._
import net.psforever.packet.game._
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits._

class InvalidTerrainMessageTest extends Specification {
  val string = hex"9a 6c26ce218ddcc922cf8c3c22f0eeaca180"

  "decode" in {
    PacketCoding.decodePacket(string).require match {
      case InvalidTerrainMessage(player, vehicle, alert, pos) =>
        player mustEqual PlanetSideGUID(9836)
        vehicle mustEqual PlanetSideGUID(8654)
        alert mustEqual TerrainCondition.Unsafe
        pos mustEqual Vector3(4695.138f, 3969.5388f, 217.86671f)
      case _ =>
        ko
    }
  }

  "encode" in {
    val msg = InvalidTerrainMessage(
      PlanetSideGUID(9836),
      PlanetSideGUID(8654),
      TerrainCondition.Unsafe,
      Vector3(4695.138f, 3969.5388f, 217.86671f)
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector
    pkt mustEqual string
  }
}
