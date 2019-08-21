// Copyright (c) 2019 PSForever
package net.psforever.objects.zones

/**
  * An object representing the dimensions of the zone map as its maximum coordinates.
  * @see `InventoryTile`
  * @param width the longitudinal span of the map
  * @param height the latitudinal span of the map
  */
final case class MapScale(width : Float, height : Float)

object MapScale {
  final val Dim512 = MapScale(512, 512) //map49 (unused)
  final val Dim1024 = MapScale(1024, 1024) //homebo, tzsh*, tzco*
  final val Dim2048 = MapScale(2048, 2048) //ugd3 .. ugd5; map44 .. map46, map80, map95 (unused)
  final val Dim2560 = MapScale(2560, 2560) //ugd1, ugd2, ugd6
  final val Dim4096 = MapScale(4096, 4096) //tzdr*
  final val Dim8192 = MapScale(8192, 8192) //common size
}
