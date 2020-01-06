package net.psforever.types

import scodec.codecs.uint16L

case class PlanetSideGUID(guid : Int)

object PlanetSideGUID {
  implicit val codec = uint16L.as[PlanetSideGUID]
}
