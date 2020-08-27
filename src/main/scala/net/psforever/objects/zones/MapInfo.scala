package net.psforever.objects.zones

import enumeratum.values.{StringEnumEntry, StringEnum}

sealed abstract class MapInfo(
    val value: String,
    val checksum: Long,
    val scale: MapScale
) extends StringEnumEntry {}

case object MapInfo extends StringEnum[MapInfo] {

  case object Map01
      extends MapInfo(
        value = "map01",
        checksum = 2094187456L,
        scale = MapScale.Dim8192
      )

  case object Map02
      extends MapInfo(
        value = "map02",
        checksum = 1113780607L,
        scale = MapScale.Dim8192
      )

  case object Map03
      extends MapInfo(
        value = "map03",
        checksum = 1624200906L,
        scale = MapScale.Dim8192
      )

  case object Map04
      extends MapInfo(
        value = "map04",
        checksum = 2455050867L,
        scale = MapScale.Dim8192
      )

  case object Map05
      extends MapInfo(
        value = "map05",
        checksum = 107922342L,
        scale = MapScale.Dim8192
      )

  case object Map06
      extends MapInfo(
        value = "map06",
        checksum = 579139514L,
        scale = MapScale.Dim8192
      )

  case object Map07
      extends MapInfo(
        value = "map07",
        checksum = 1564014762L,
        scale = MapScale.Dim8192
      )

  case object Map08
      extends MapInfo(
        value = "map08",
        checksum = 0L,
        scale = MapScale.Dim8192
      )

  case object Map09
      extends MapInfo(
        value = "map09",
        checksum = 1380643455L,
        scale = MapScale.Dim8192
      )

  case object Map10
      extends MapInfo(
        value = "map10",
        checksum = 230810349L,
        scale = MapScale.Dim8192
      )

  case object Map11
      extends MapInfo(
        value = "map11",
        checksum = 4129515529L,
        scale = MapScale.Dim8192
      )

  case object Map12
      extends MapInfo(
        value = "map12",
        checksum = 962888126L,
        scale = MapScale.Dim8192
      )

  case object Map13
      extends MapInfo(
        value = "map13",
        checksum = 3904659548L,
        scale = MapScale.Dim8192
      )

  case object Map14
      extends MapInfo(
        value = "map14",
        checksum = 0L,
        scale = MapScale.Dim8192
      )

  case object Map15
      extends MapInfo(
        value = "map15",
        checksum = 0L,
        scale = MapScale.Dim8192
      )

  case object Map16
      extends MapInfo(
        value = "map16",
        checksum = 0L,
        scale = MapScale.Dim8192
      )

  case object Ugd01
      extends MapInfo(
        value = "ugd01",
        checksum = 3405929729L,
        scale = MapScale.Dim2560
      )

  case object Ugd02
      extends MapInfo(
        value = "ugd02",
        checksum = 2702486449L,
        scale = MapScale.Dim2560
      )

  case object Ugd03
      extends MapInfo(
        value = "ugd03",
        checksum = 1673539651L,
        scale = MapScale.Dim2048
      )

  case object Ugd04
      extends MapInfo(
        value = "ugd04",
        checksum = 3797992164L,
        scale = MapScale.Dim2048
      )

  case object Ugd05
      extends MapInfo(
        value = "ugd05",
        checksum = 1769572498L,
        scale = MapScale.Dim2048
      )

  case object Ugd06
      extends MapInfo(
        value = "ugd06",
        checksum = 4274683970L,
        scale = MapScale.Dim2560
      )

  case object Map96
      extends MapInfo(
        value = "map96",
        checksum = 846603446L,
        scale = MapScale.Dim4096
      )

  case object Map97
      extends MapInfo(
        value = "map97",
        checksum = 2810790213L,
        scale = MapScale.Dim4096
      )

  case object Map98
      extends MapInfo(
        value = "map98",
        checksum = 3654267088L,
        scale = MapScale.Dim4096
      )

  case object Map99
      extends MapInfo(
        value = "map99",
        checksum = 4113726460L,
        scale = MapScale.Dim4096
      )

  val values: IndexedSeq[MapInfo] = findValues

}
