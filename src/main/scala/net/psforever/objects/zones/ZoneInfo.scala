package net.psforever.objects.zones

import enumeratum.values.{IntEnum, IntEnumEntry}

sealed abstract class ZoneInfo(
    val value: Int,
    val name: String,
    val id: String,
    val map: MapInfo,
    val aliases: Seq[String] = Seq()
) extends IntEnumEntry {}

case object ZoneInfo extends IntEnum[ZoneInfo] {

  case object Solsar
      extends ZoneInfo(
        value = 1,
        name = "Solsar",
        id = "z1",
        map = MapInfo.Map01
      )

  case object Hossin
      extends ZoneInfo(
        value = 2,
        name = "Hossin",
        id = "z2",
        map = MapInfo.Map02
      )

  case object Cyssor
      extends ZoneInfo(
        value = 3,
        name = "Cyssor",
        id = "z3",
        map = MapInfo.Map03
      )

  case object Ishundar
      extends ZoneInfo(
        value = 4,
        name = "Ishundar",
        id = "z4",
        map = MapInfo.Map04
      )

  case object Forseral
      extends ZoneInfo(
        value = 5,
        name = "Forseral",
        id = "z5",
        map = MapInfo.Map05
      )

  case object Ceryshen
      extends ZoneInfo(
        value = 6,
        name = "Ceryshen",
        id = "z6",
        map = MapInfo.Map06
      )

  case object Esamir
      extends ZoneInfo(
        value = 7,
        name = "Esamir",
        id = "z7",
        map = MapInfo.Map07
      )

  case object Oshur
      extends ZoneInfo(
        value = 8,
        name = "Oshur",
        id = "z8",
        map = MapInfo.Map08
      )

  case object Searhus
      extends ZoneInfo(
        value = 9,
        name = "Searhus",
        id = "z9",
        map = MapInfo.Map09
      )

  case object Amerish
      extends ZoneInfo(
        value = 10,
        name = "Amerish",
        id = "z10",
        map = MapInfo.Map10
      )

  case object NcSanctuary
      extends ZoneInfo(
        value = 11,
        name = "NC Sanctuary",
        id = "home1",
        map = MapInfo.Map11,
        aliases = Seq("nc-sanctuary")
      )

  case object TrSanctuary
      extends ZoneInfo(
        value = 12,
        name = "TR Sanctuary",
        id = "home2",
        map = MapInfo.Map12,
        aliases = Seq("tr-sanctuary")
      )

  case object VsSanctuary
      extends ZoneInfo(
        value = 13,
        name = "VS Sanctuary",
        id = "home3",
        map = MapInfo.Map13,
        aliases = Seq("vs-sanctuary")
      )

  case object tzshtr
      extends ZoneInfo(
        value = 14,
        name = "tzshtr",
        id = "tzshtr",
        map = MapInfo.Map14
      )

  case object tzdrtr
      extends ZoneInfo(
        value = 15,
        name = "tzdrtr",
        id = "tzdrtr",
        map = MapInfo.Map15
      )

  case object tzcotr
      extends ZoneInfo(
        value = 16,
        name = "tzcotr",
        id = "tzcotr",
        map = MapInfo.Map16
      )

  case object tzshnc
      extends ZoneInfo(
        value = 17,
        name = "tzshnc",
        id = "tzshnc",
        map = MapInfo.Map14
      )

  case object tzdrnc
      extends ZoneInfo(
        value = 18,
        name = "tzdrnc",
        id = "tzdrnc",
        map = MapInfo.Map15
      )

  case object tzconc
      extends ZoneInfo(
        value = 19,
        name = "tzconc",
        id = "tzconc",
        map = MapInfo.Map16
      )

  case object tzshvs
      extends ZoneInfo(
        value = 20,
        name = "tzshvs",
        id = "tzshvs",
        map = MapInfo.Map14
      )

  case object tzdrvs
      extends ZoneInfo(
        value = 21,
        name = "tzdrvs",
        id = "tzdrvs",
        map = MapInfo.Map15
      )

  case object tzcovs
      extends ZoneInfo(
        value = 22,
        name = "tzcovs",
        id = "tzcovs",
        map = MapInfo.Map16
      )

  case object Supai
      extends ZoneInfo(
        value = 23,
        name = "Supai",
        id = "c1",
        map = MapInfo.Ugd01
      )

  case object Hunhau
      extends ZoneInfo(
        value = 24,
        name = "Hunhau",
        id = "c2",
        map = MapInfo.Ugd02
      )

  case object Adlivun
      extends ZoneInfo(
        value = 25,
        name = "Adlivun",
        id = "c3",
        map = MapInfo.Ugd03
      )

  case object Byblos
      extends ZoneInfo(
        value = 26,
        name = "Byblos",
        id = "c4",
        map = MapInfo.Ugd04
      )

  case object Annwn
      extends ZoneInfo(
        value = 27,
        name = "Annwn",
        id = "c5",
        map = MapInfo.Ugd05
      )

  case object Drugaskan
      extends ZoneInfo(
        value = 28,
        name = "Drugaskan",
        id = "c6",
        map = MapInfo.Ugd06
      )

  case object Extinction
      extends ZoneInfo(
        value = 29,
        name = "Extinction",
        id = "i1",
        map = MapInfo.Map99
      )

  case object Ascension
      extends ZoneInfo(
        value = 30,
        name = "Ascension",
        id = "i2",
        map = MapInfo.Map98
      )

  case object Desolation
      extends ZoneInfo(
        value = 31,
        name = "Desolation",
        id = "i3",
        map = MapInfo.Map97
      )

  case object Nexus
      extends ZoneInfo(
        value = 32,
        name = "Nexus",
        id = "i4",
        map = MapInfo.Map96
      )

  val values: IndexedSeq[ZoneInfo] = findValues

  def findName(name: String): ZoneInfo = findNameOpt(name).get

  def findNameOpt(name: String): Option[ZoneInfo] =
    values.find(v =>
      v.name.toLowerCase() == name.toLowerCase() || v.aliases.map(_.toLowerCase()).contains(name.toLowerCase())
    )

}
