package net.psforever.objects.zones

import enumeratum.values.{StringEnum, StringEnumEntry}
import net.psforever.types.Vector3

sealed abstract class MapInfo(
    val value: String,
    val checksum: Long,
    val scale: MapScale,
    val environment: List[FillLine]
) extends StringEnumEntry {}

case object MapInfo extends StringEnum[MapInfo] {

  case object Map01
      extends MapInfo(
        value = "map01",
        checksum = 2094187456L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(FilledWith.Water, 35),
          Pool(FilledWith.Water, 43.57f, 6263.2812f, 3742.9375f, 6238.0f, 3712.7188f), //north of aton
          Pool(FilledWith.Water, 44.92f, 5965.164f, 4801.2266f, 5893.1094f, 4730.203f), //east of seth
          Pool(FilledWith.Water, 43.625f, 5296.289f, 5356.8594f, 5265.789f, 5315.9062f), //south of bastet
          Pool(FilledWith.Water, 43.515625f, 4805.5f, 4324.3984f, 4727.867f, 4280.2188f), //north of hapi
          Pool(FilledWith.Water, 43.0625f, 3313.1094f, 4746.4844f, 3259.4219f, 4691.2266f), //east of thoth
          Pool(FilledWith.Water, 43.51f, 1917.1016f, 4086.8984f, 1893.4844f, 4038.2734f) //between horus and amun
        )
      )

  case object Map02
      extends MapInfo(
        value = "map02",
        checksum = 1113780607L,
        scale = MapScale.Dim8192,
        environment = {
          //exclude parts of voltan and naum due to their generator rooms being below sealevel
          val northVoltan = 3562.4844f
          val southVoltan = 3401.6875f
          val eastVoltan = 4556.703f
          val westVoltan = 4411.6875f
          val northNaum = 3575.8047f
          val southNaum = 3539.5234f
          val eastNaum = 5490.6875f
          val westNaum = 5427.078f
          List(
            Pool(FilledWith.Water, 11, 8192, westVoltan, 0, 0), //west of voltan
            Pool(FilledWith.Water, 11, 8192, westNaum, 0, eastVoltan), //between voltan and naum
            Pool(FilledWith.Water, 11, 8192, 8192, 0, eastNaum), //east of naum
            Pool(FilledWith.Water, 11, 8192, eastVoltan, northVoltan, westVoltan), //north of voltan
            Pool(FilledWith.Water, 11, southVoltan, eastVoltan, 0, westVoltan), //south of voltan
            Pool(FilledWith.Water, 11, 8192, eastNaum, northNaum, westNaum), //north of naum
            Pool(FilledWith.Water, 11, southNaum, eastNaum, 0, westNaum) //south of naum
            //TODO voltan Killplane
            //TODO naum Killplane
          )
        }
      )

  case object Map03
      extends MapInfo(
        value = "map03",
        checksum = 1624200906L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(FilledWith.Water, 35),
          Pool(FilledWith.Water, 67.3125f, 3449.586f, 5870.383f, 3313.75f, 5715.3203f), //east of itan, south of kaang
          Pool(FilledWith.Water, 53.71875f, 6013.0625f, 1861.7969f, 5947.1406f, 1634.7734f), //E6
          Pool(FilledWith.Water, 49.625f, 7181.6953f, 1496.3828f, 6972.992f, 1340.1328f), //east of wele
          Pool(FilledWith.Water, 48.71875f, 992.5156f, 1806.5469f, 811.5547f, 1676.3359f), //west, island of leza
          Pool(FilledWith.Water, 48.5f, 1327.8125f, 2069.5781f, 152.5234f, 1979.3281f), //east, island of leza
          Pool(FilledWith.Water, 46.625f, 2384.9688f, 3659.1172f, 2238.3516f, 3483.3828f), //east of tore
          Pool(FilledWith.Water, 39.15625f, 4112.953f, 2509.3438f, 3778.5781f, 2312.789f), //south of hunhau south geowarp
          Pool(FilledWith.Water, 39.046875f, 5877.8203f, 7131.664f, 5690.5547f, 6955.383f), //north of gate2
          Pool(FilledWith.Water, 37.984375f, 2737.2578f, 3409.9219f, 2648.3984f, 3210.711f), //northeast of tore
          Pool(FilledWith.Water, 37.703125f, 4689.1875f, 4788.922f, 4568.8438f, 4665.1016f), //north of gunuku
          Pool(FilledWith.Water, 37.53125f, 2701.6797f, 806.6172f, 2648.3984f, 738.4375f), //island with mukuru
          Pool(FilledWith.Water, 36.921875f, 3162.1094f, 1689.5703f, 3085.7422f, 1612.7734f), //north of nzame
          Pool(FilledWith.Water, 36.390625f, 4143.797f, 4872.3906f, 4021.9766f, 4798.578f), //south of gunuku
          Pool(FilledWith.Water, 35.71875f, 2591.336f, 1752.5938f, 2512.7578f, 1663.1172f) //south of nzame
        )
      )

  case object Map04
      extends MapInfo(
        value = "map04",
        checksum = 2455050867L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 35))
      )

  case object Map05
      extends MapInfo(
        value = "map05",
        checksum = 107922342L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 35))
      )

  case object Map06
      extends MapInfo(
        value = "map06",
        checksum = 579139514L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(FilledWith.Water, 10.03125f),
          Pool(FilledWith.Water, 213.03125f, 3116.7266f, 4724.414f, 2685.8281f, 4187.4375f) //southwest of tootega
        )
      )

  case object Map07
      extends MapInfo(
        value = "map07",
        checksum = 1564014762L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 29.984375f))
      )

  case object Map08
      extends MapInfo(
        value = "map08",
        checksum = 0L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 35))
      )

  case object Map09
      extends MapInfo(
        value = "map09",
        checksum = 1380643455L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(FilledWith.Water, 30),
          Pool(FilledWith.Water, 41.46875f, 5964.461f, 1947.1328f, 5701.6016f, 1529.8438f), //north of wakea
          Pool(FilledWith.Water, 39.21875f, 5694.125f, 6939.8984f, 5516.922f, 6814.211f), //northeast of iva
          Pool(FilledWith.Water, 39.078125f, 4381.789f, 6650.8203f, 4071.4766f, 6445.133f), //south of iva
          Pool(FilledWith.Lava, DeepCircularSurface(Vector3(3901.5547f, 4422.746f, 224.57812f), 82.6797f)), //upper west lava pool //TODO fudged
          Pool(FilledWith.Lava, DeepSurface(189.54688f, 4032.914f, 3893.6562f, 3912.3906f, 3666.4453f)), //lower west lava pool
          Pool(FilledWith.Lava, DeepSurface(187.57812f, 4288.1484f, 4589.0703f, 3996.3125f, 4355.6406f)), //lower central lava pool
          Pool(FilledWith.Lava, DeepSurface(181.45312f, 4635.1953f, 4579.3516f, 4406.3438f, 4303.828f)), //upper central lava pool
          Pool(FilledWith.Lava, DeepSurface(176.64062f, 4274.8125f, 4969.9688f, 4101.7734f, 4766.3594f)) //east lava pool
        )
      )

  case object Map10
      extends MapInfo(
        value = "map10",
        checksum = 230810349L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 35))
      )

  case object Map11
      extends MapInfo(
        value = "map11",
        checksum = 4129515529L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(FilledWith.Water, 24),
          Pool(FilledWith.Water, 44.453125f, 4289.4766f, 3124.8125f, 4070.7031f, 2892.9922f), //H10
          Pool(FilledWith.Water, 39.984375f, 5405.9297f, 2843.8672f, 5190.1562f, 2653.5625f), //southeast of hart c campus
          Pool(FilledWith.Water, 36.15625f, 4622.3594f, 3861.6797f, 4497.9844f, 3717.3516f), //J9
          Pool(FilledWith.Water, 35.234375f, 5596.086f, 4019.6797f, 5354.078f, 3814.1875f), //south of hart b campus
          Pool(FilledWith.Water, 34.96875f, 5899.367f, 3235.5781f, 5573.8516f, 2865.7812f), //northeast of hart c campus
          Pool(FilledWith.Water, 34.328125f, 3880.7422f, 5261.508f, 3780.9219f, 5166.953f), //east of hart a campus
          Pool(FilledWith.Water, 31.03125f, 4849.797f, 2415.4297f, 4731.8594f, 2252.1484f) //south of hart c campus
        )
      )

  case object Map12
      extends MapInfo(
        value = "map12",
        checksum = 962888126L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 20.03125f))
      )

  case object Map13
      extends MapInfo(
        value = "map13",
        checksum = 3904659548L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 30))
      )

  case object Map14
      extends MapInfo(
        value = "map14",
        checksum = 0L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 35))
      )

  case object Map15
      extends MapInfo(
        value = "map15",
        checksum = 0L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 35))
      )

  case object Map16
      extends MapInfo(
        value = "map16",
        checksum = 0L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(FilledWith.Water, 35))
      )

  case object Ugd01
      extends MapInfo(
        value = "ugd01",
        checksum = 3405929729L,
        scale = MapScale.Dim2560,
        environment = List(SeaLevel(FilledWith.Water, 50.734375f)) //TODO waterfalls!
      )

  case object Ugd02
      extends MapInfo(
        value = "ugd02",
        checksum = 2702486449L,
        scale = MapScale.Dim2560,
        environment = List(
          Pool(FilledWith.Water, 183.98438f, 914.33594f, 1369.5f, 626.03906f, 666.3047f), //upper southern pools
          Pool(FilledWith.Water, 182.96875f, 580.7578f, 913.52344f, 520.4531f, 843.97656f), //lowest southern pool
          Pool(FilledWith.Water, 192.40625f, 1717.5703f, 1219.3359f, 1572.8828f, 1036.1328f), //bottom, northern pool
          Pool(FilledWith.Water, 191.65625f, 1869.1484f, 1195.6406f, 1743.8125f, 1050.7344f), //middle, northern pool
          Pool(FilledWith.Water, 192.32812f, 1966.1562f, 1252.7344f, 1889.8047f, 1148.5312f), //top, northern pool
          Pool(FilledWith.Water, 194.89062f, 1763.4141f, 1415.125f, 1333.9531f, 1280.4609f) //east, northern pool
        )
      )

  case object Ugd03
      extends MapInfo(
        value = "ugd03",
        checksum = 1673539651L,
        scale = MapScale.Dim2048,
        environment = List(SeaLevel(FilledWith.Death, 30)) //not actually lava, but a kill plane if you fall beneath the map
      )

  case object Ugd04
      extends MapInfo(
        value = "ugd04",
        checksum = 3797992164L,
        scale = MapScale.Dim2048,
        environment = List(SeaLevel(FilledWith.Death, 51.215f)) //ADB: 51.414f
      )

  case object Ugd05
      extends MapInfo(
        value = "ugd05",
        checksum = 1769572498L,
        scale = MapScale.Dim2048,
        environment = List(SeaLevel(FilledWith.Death, 115)) //not actually lava, but a kill plane if you fall beneath the map
      )

  case object Ugd06
      extends MapInfo(
        value = "ugd06",
        checksum = 4274683970L,
        scale = MapScale.Dim2560,
        environment = List(SeaLevel(FilledWith.Death, 55)) //not actually lava, but a kill plane if you fall beneath the map
      )

  case object Map96
      extends MapInfo(
        value = "map96",
        checksum = 846603446L,
        scale = MapScale.Dim4096,
        environment = List(SeaLevel(FilledWith.Water, 17.015625f))
      )

  case object Map97
      extends MapInfo(
        value = "map97",
        checksum = 2810790213L,
        scale = MapScale.Dim4096,
        environment = List(
          SeaLevel(FilledWith.Water, 10.09375f),
          Pool(FilledWith.Water, 20.484375f, 2183.8203f, 2086.5078f, 2127.2266f, 1992.5f), //north
          Pool(FilledWith.Water, 20.421875f, 1880.4375f, 1961.875f, 1816.1484f, 1915.0625f), //west
          Pool(FilledWith.Water, 20.421875f, 2028.1172f, 2232.4375f, 1976.9141f, 2181.0312f) //east
        )
      )

  case object Map98
      extends MapInfo(
        value = "map98",
        checksum = 3654267088L,
        scale = MapScale.Dim4096,
        environment = List(SeaLevel(FilledWith.Water, 3.5f))
      )

  case object Map99
      extends MapInfo(
        value = "map99",
        checksum = 4113726460L,
        scale = MapScale.Dim4096,
        environment = List(SeaLevel(FilledWith.Water, 44.0625f))
      )

  val values: IndexedSeq[MapInfo] = findValues
}
