package net.psforever.objects.zones

import enumeratum.values.{StringEnum, StringEnumEntry}
import net.psforever.objects.serverobject.environment._
import net.psforever.types.{PlanetSideGUID, Vector3}

sealed abstract class MapInfo(
    val value: String,
    val checksum: Long,
    val scale: MapScale,
    val environment: List[PieceOfEnvironment]
) extends StringEnumEntry {}

case object MapInfo extends StringEnum[MapInfo] {

  case object Map01
      extends MapInfo(
        value = "map01",
        checksum = 2094187456L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(EnvironmentAttribute.Water, 35),
          Pool(EnvironmentAttribute.Water, 44.92f, 5965.164f, 4801.2266f, 5893.1094f, 4730.203f), //east of seth
          Pool(EnvironmentAttribute.Water, 43.625f, 5296.289f, 5356.8594f, 5265.789f, 5315.9062f), //south of bastet
          Pool(EnvironmentAttribute.Water, 43.57f, 6263.2812f, 3742.9375f, 6238.0f, 3712.7188f), //north of aton
          Pool(EnvironmentAttribute.Water, 43.515625f, 4805.5f, 4324.3984f, 4727.867f, 4280.2188f), //north of hapi
          Pool(EnvironmentAttribute.Water, 43.0625f, 3313.1094f, 4746.4844f, 3259.4219f, 4691.2266f), //east of thoth
          Pool(EnvironmentAttribute.Water, 43.51f, 1917.1016f, 4086.8984f, 1893.4844f, 4038.2734f) //between horus and amun
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
            Pool(EnvironmentAttribute.Water, 11, 8192, westVoltan, 0, 0), //west of voltan
            Pool(EnvironmentAttribute.Water, 11, 8192, westNaum, 0, eastVoltan), //between voltan and naum
            Pool(EnvironmentAttribute.Water, 11, 8192, 8192, 0, eastNaum), //east of naum
            Pool(EnvironmentAttribute.Water, 11, 8192, eastVoltan, northVoltan, westVoltan), //north of voltan
            Pool(EnvironmentAttribute.Water, 11, southVoltan, eastVoltan, 0, westVoltan), //south of voltan
            Pool(EnvironmentAttribute.Water, 11, 8192, eastNaum, northNaum, westNaum), //north of naum
            Pool(EnvironmentAttribute.Water, 11, southNaum, eastNaum, 0, westNaum) //south of naum
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
          SeaLevel(EnvironmentAttribute.Water, 35),
          Pool(EnvironmentAttribute.Water, 67.3125f, 3449.586f, 5870.383f, 3313.75f, 5715.3203f), //east of itan, south of kaang
          Pool(EnvironmentAttribute.Water, 53.71875f, 6013.0625f, 1861.7969f, 5947.1406f, 1634.7734f), //E6
          Pool(EnvironmentAttribute.Water, 49.625f, 7181.6953f, 1496.3828f, 6972.992f, 1340.1328f), //east of wele
          Pool(EnvironmentAttribute.Water, 48.71875f, 992.5156f, 1806.5469f, 811.5547f, 1676.3359f), //west, island of leza
          Pool(EnvironmentAttribute.Water, 48.5f, 1327.8125f, 2069.5781f, 152.5234f, 1979.3281f), //east, island of leza
          Pool(EnvironmentAttribute.Water, 46.625f, 2384.9688f, 3659.1172f, 2238.3516f, 3483.3828f), //east of tore
          Pool(EnvironmentAttribute.Water, 39.15625f, 4112.953f, 2509.3438f, 3778.5781f, 2312.789f), //south of hunhau south geowarp
          Pool(EnvironmentAttribute.Water, 39.046875f, 5877.8203f, 7131.664f, 5690.5547f, 6955.383f), //north of gate2
          Pool(EnvironmentAttribute.Water, 37.984375f, 2737.2578f, 3409.9219f, 2648.3984f, 3210.711f), //northeast of tore
          Pool(EnvironmentAttribute.Water, 37.703125f, 4689.1875f, 4788.922f, 4568.8438f, 4665.1016f), //north of gunuku
          Pool(EnvironmentAttribute.Water, 37.53125f, 2701.6797f, 806.6172f, 2648.3984f, 738.4375f), //island with mukuru
          Pool(EnvironmentAttribute.Water, 36.921875f, 3162.1094f, 1689.5703f, 3085.7422f, 1612.7734f), //north of nzame
          Pool(EnvironmentAttribute.Water, 36.390625f, 4143.797f, 4872.3906f, 4021.9766f, 4798.578f), //south of gunuku
          Pool(EnvironmentAttribute.Water, 35.71875f, 2591.336f, 1752.5938f, 2512.7578f, 1663.1172f) //south of nzame
        )
      )

  case object Map04
      extends MapInfo(
        value = "map04",
        checksum = 2455050867L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 19.984375f))
      )

  case object Map05
      extends MapInfo(
        value = "map05",
        checksum = 107922342L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(EnvironmentAttribute.Water, 35.015625f),
          Pool(EnvironmentAttribute.Water, 51.875f, 4571.8125f, 3015.5547f, 4455.8047f, 2852.711f), //down the road, west of bel
          Pool(EnvironmentAttribute.Water, 49.8125f, 4902.336f, 3413.461f, 4754.0938f, 3210.8125f), //west of bel
          Pool(EnvironmentAttribute.Water, 49.515625f, 4044.3984f, 4700.8516f, 3999.9688f, 4517.375f), //southeast of neit
          Pool(EnvironmentAttribute.Water, 48.515625f, 4553.75f, 4110.2188f, 4438.6875f, 3995.3125f), //northwest of neit
          Pool(EnvironmentAttribute.Water, 48.28125f, 4474.3906f, 4551.2812f, 4339.3984f, 4472.4375f), //northeast of neit
          Pool(EnvironmentAttribute.Water, 45.828125f, 3808.0547f, 3901.3828f, 1432.5625f, 3720.9844f), //J17
          Pool(EnvironmentAttribute.Water, 43.765625f, 3997.2812f, 3991.539f, 3937.8906f, 3937.875f), //southwest of neit
          Pool(EnvironmentAttribute.Water, 43.671875f, 2694.2031f, 3079.875f, 2552.414f, 2898.8203f), //west of anu
          Pool(EnvironmentAttribute.Water, 42.671875f, 5174.4844f, 5930.133f, 4981.4297f, 5812.383f), //west of lugh
          Pool(EnvironmentAttribute.Water, 42.203125f, 4935.742f, 5716.086f, 5496.6953f, 5444.5625f), //across road, west of lugh
          Pool(EnvironmentAttribute.Water, 41.765625f, 2073.914f, 4982.5938f, 1995.4688f, 4899.086f), //L15-M16
          Pool(EnvironmentAttribute.Water, 41.3125f, 3761.1484f, 2616.75f, 3627.4297f, 2505.1328f), //G11, south
          Pool(EnvironmentAttribute.Water, 40.421875f, 4058.8281f, 2791.6562f, 3985.1016f, 2685.3672f) //G11, north
        )
      )

  case object Map06
      extends MapInfo(
        value = "map06",
        checksum = 579139514L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(EnvironmentAttribute.Water, 10.03125f),
          Pool(EnvironmentAttribute.Water, 213.03125f, 3116.7266f, 4724.414f, 2685.8281f, 4187.4375f) //southwest of tootega
        )
      )

  case object Map07
      extends MapInfo(
        value = "map07",
        checksum = 1564014762L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 29.984375f))
      )

  case object Map08
      extends MapInfo(
        value = "map08",
        checksum = 0L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 26.078125f))
      )

  case object Map09
      extends MapInfo(
        value = "map09",
        checksum = 1380643455L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(EnvironmentAttribute.Water, 30),
          Pool(EnvironmentAttribute.Water, 41.46875f, 5964.461f, 1947.1328f, 5701.6016f, 1529.8438f), //north of wakea
          Pool(EnvironmentAttribute.Water, 39.21875f, 5694.125f, 6939.8984f, 5516.922f, 6814.211f), //northeast of iva
          Pool(EnvironmentAttribute.Water, 39.078125f, 4381.789f, 6650.8203f, 4071.4766f, 6445.133f), //south of iva
          Pool(EnvironmentAttribute.Lava, DeepCircularSurface(Vector3(3901.5547f, 4422.746f, 224.57812f), 82.6797f)), //upper west lava pool
          Pool(EnvironmentAttribute.Lava, DeepSurface(189.54688f, 4032.914f, 3893.6562f, 3912.3906f, 3666.4453f)), //lower west lava pool
          Pool(EnvironmentAttribute.Lava, DeepSurface(187.57812f, 4288.1484f, 4589.0703f, 3996.3125f, 4355.6406f)), //lower central lava pool
          Pool(EnvironmentAttribute.Lava, DeepSurface(181.45312f, 4635.1953f, 4579.3516f, 4406.3438f, 4303.828f)), //upper central lava pool
          Pool(EnvironmentAttribute.Lava, DeepSurface(176.64062f, 4274.8125f, 4969.9688f, 4101.7734f, 4766.3594f)) //east lava pool
        )
      )

  case object Map10
      extends MapInfo(
        value = "map10",
        checksum = 230810349L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 28))
      )

  case object Map11
      extends MapInfo(
        value = "map11",
        checksum = 4129515529L,
        scale = MapScale.Dim8192,
        environment = List(
          SeaLevel(EnvironmentAttribute.Water, 24),
          Pool(EnvironmentAttribute.Water, 44.453125f, 4289.4766f, 3124.8125f, 4070.7031f, 2892.9922f), //H10
          Pool(EnvironmentAttribute.Water, 39.984375f, 5405.9297f, 2843.8672f, 5190.1562f, 2653.5625f), //southeast of hart c campus
          Pool(EnvironmentAttribute.Water, 36.15625f, 4622.3594f, 3861.6797f, 4497.9844f, 3717.3516f), //J9
          Pool(EnvironmentAttribute.Water, 35.234375f, 5596.086f, 4019.6797f, 5354.078f, 3814.1875f), //south of hart b campus
          Pool(EnvironmentAttribute.Water, 34.96875f, 5899.367f, 3235.5781f, 5573.8516f, 2865.7812f), //northeast of hart c campus
          Pool(EnvironmentAttribute.Water, 34.328125f, 3880.7422f, 5261.508f, 3780.9219f, 5166.953f), //east of hart a campus
          Pool(EnvironmentAttribute.Water, 31.03125f, 4849.797f, 2415.4297f, 4731.8594f, 2252.1484f) //south of hart c campus
        ) ++ MapEnvironment.map11Environment
      )

  case object Map12
      extends MapInfo(
        value = "map12",
        checksum = 962888126L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 20.03125f)) ++
                      MapEnvironment.map12Environment
      )

  case object Map13
      extends MapInfo(
        value = "map13",
        checksum = 3904659548L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 30)) ++
                      MapEnvironment.map13Environment
      )

  case object Map14
      extends MapInfo(
        value = "map14",
        checksum = 0L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 0))
      )

  case object Map15
      extends MapInfo(
        value = "map15",
        checksum = 0L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 0))
      )

  case object Map16
      extends MapInfo(
        value = "map16",
        checksum = 0L,
        scale = MapScale.Dim8192,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 0))
      )

  case object Ugd01
      extends MapInfo(
        value = "ugd01",
        checksum = 3405929729L,
        scale = MapScale.Dim2560,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 50.734375f)) //TODO waterfalls!
      )

  case object Ugd02
      extends MapInfo(
        value = "ugd02",
        checksum = 2702486449L,
        scale = MapScale.Dim2560,
        environment = List(
          Pool(EnvironmentAttribute.Water, 194.89062f, 1763.4141f, 1415.125f, 1333.9531f, 1280.4609f), //east, northern pool
          Pool(EnvironmentAttribute.Water, 192.40625f, 1717.5703f, 1219.3359f, 1572.8828f, 1036.1328f), //bottom, northern pool
          Pool(EnvironmentAttribute.Water, 192.32812f, 1966.1562f, 1252.7344f, 1889.8047f, 1148.5312f), //top, northern pool
          Pool(EnvironmentAttribute.Water, 191.65625f, 1869.1484f, 1195.6406f, 1743.8125f, 1050.7344f), //middle, northern pool
          Pool(EnvironmentAttribute.Water, 183.98438f, 914.33594f, 1369.5f, 626.03906f, 666.3047f), //upper southern pools
          Pool(EnvironmentAttribute.Water, 182.96875f, 580.7578f, 913.52344f, 520.4531f, 843.97656f) //lowest southern pool
        )
      )

  case object Ugd03
      extends MapInfo(
        value = "ugd03",
        checksum = 1673539651L,
        scale = MapScale.Dim2048,
        environment = List(SeaLevel(EnvironmentAttribute.Death, 30)) //not actually lava, but a kill plane if you fall beneath the map
      )

  case object Ugd04
      extends MapInfo(
        value = "ugd04",
        checksum = 3797992164L,
        scale = MapScale.Dim2048,
        environment = List(SeaLevel(EnvironmentAttribute.Death, 51.215f)) //ADB: 51.414f
      )

  case object Ugd05
      extends MapInfo(
        value = "ugd05",
        checksum = 1769572498L,
        scale = MapScale.Dim2048,
        environment = List(SeaLevel(EnvironmentAttribute.Death, 115)) //not actually lava, but a kill plane if you fall beneath the map
      )

  case object Ugd06
      extends MapInfo(
        value = "ugd06",
        checksum = 4274683970L,
        scale = MapScale.Dim2560,
        environment = List(SeaLevel(EnvironmentAttribute.Death, 55)) //not actually lava, but a kill plane if you fall beneath the map
      )

  case object Map96
      extends MapInfo(
        value = "map96",
        checksum = 846603446L,
        scale = MapScale.Dim4096,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 17.015625f))
      )

  case object Map97
      extends MapInfo(
        value = "map97",
        checksum = 2810790213L,
        scale = MapScale.Dim4096,
        environment = List(
          SeaLevel(EnvironmentAttribute.Water, 10.09375f),
          Pool(EnvironmentAttribute.Water, 20.484375f, 2183.8203f, 2086.5078f, 2127.2266f, 1992.5f), //north
          Pool(EnvironmentAttribute.Water, 20.421875f, 1880.4375f, 1961.875f, 1816.1484f, 1915.0625f), //west
          Pool(EnvironmentAttribute.Water, 20.421875f, 2028.1172f, 2232.4375f, 1976.9141f, 2181.0312f) //east
        )
      )

  case object Map98
      extends MapInfo(
        value = "map98",
        checksum = 3654267088L,
        scale = MapScale.Dim4096,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 3.5f))
      )

  case object Map99
      extends MapInfo(
        value = "map99",
        checksum = 4113726460L,
        scale = MapScale.Dim4096,
        environment = List(SeaLevel(EnvironmentAttribute.Water, 44.0625f))
      )

  val values: IndexedSeq[MapInfo] = findValues
}

object MapEnvironment {
  /** the pattern of mount points for the HART gantries in most facilities;
    * eight values - 1-8 - listed as four downstairs - NE SE NW SW - then four upstairs - same
    */
  private val hartMountPoints: Seq[Int] = Seq(6,5, 2,1, 8,7, 4,3)
  /** the pattern of mount points for the HART gantries in VS sanctuary facilities;
    * eight values - 1-8 - listed as four downstairs - NE SE NW SW - then four upstairs - same
    */
  private val vsHartMountPoints: Seq[Int] = Seq(1,2, 5,6, 3,4, 7,8)

  /** HART denial fields for the New Conglomerate sanctuary */
  final val map11Environment: List[PieceOfEnvironment] =
    hartGantryDenialFields(PlanetSideGUID(840), Vector3(2258, 5538, 65.20142f), hartMountPoints) ++
    hartGantryDenialFields(PlanetSideGUID(841), Vector3(4152, 6070, 43.8766136f), hartMountPoints) ++
    specialHartGantryDenialFields(PlanetSideGUID(842))

  /** HART denial fields for the Terran Republic sanctuary */
  final val map12Environment: List[PieceOfEnvironment] =
    hartGantryDenialFields(PlanetSideGUID(808), Vector3(2922, 5230, 35.9989929f), hartMountPoints) ++
    hartGantryDenialFields(PlanetSideGUID(809), Vector3(3006, 2984, 34.919342f), hartMountPoints) ++
    hartGantryDenialFields(PlanetSideGUID(810), Vector3(5232, 3908, 35.9291039f), hartMountPoints)

  /** HART denial fields for the Vanu Sovereignty sanctuary */
  final val map13Environment: List[PieceOfEnvironment] =
    hartGantryDenialFields(PlanetSideGUID(786), Vector3(2978, 4834, 56.085392f), vsHartMountPoints) ++
    hartGantryDenialFields(PlanetSideGUID(787), Vector3(3688, 2808, 90.85312f), vsHartMountPoints) ++
    hartGantryDenialFields(PlanetSideGUID(788), Vector3(5610, 4238, 103.228859f), vsHartMountPoints)

  /**
    * Generate eight environmental representations that serve to eject players
    * from the high altitude rapid transport (HART) building boarding gantry hallways
    * when the HART shuttle associated with that building is no longer boarding
    * and the doors to those hallways should deny entrance.
    * When kicked out of the hallway,
    * ejected players should be placed in the same position as if the player willingly dismounted the shuttle.<br>
    * <br>
    * While this task seems daunting, HART buildings are formulaic, not only in layout but in orientation.
    * @param obbasemesh the globally unique identifier of the orbital shuttle pad,
    *                   an amenity of an `orbital_building_*`
    * @param position a very specific position near the center of the `orbital_building_*` building
    * @param mountPoints the assignment of mount point for each denial field
    * @return a list of environmental representations
    */
  private def hartGantryDenialFields(
                                      obbasemesh: PlanetSideGUID,
                                      position: Vector3,
                                      mountPoints: Seq[Int]
                                    ): List[PieceOfEnvironment] = {
    val px: Float     = position.x
    val py: Float     = position.y
    val pz: Float     = position.z
    val wall: Float  = 14.7188f
    val door: Float   = 55.9219f
    val gantry: Float = 45.9297f
    val lower: Float  = pz + 6.164608f
    val upper: Float  = pz + 17.508358f
    //downstairs lobbies are listed before upstairs lobbies to ensure they are tested first
    List(
      GantryDenialField(obbasemesh, mountPoints(0), DeepSurface(lower, py + wall, px + door,   py + 1,    px + gantry)), //NE
      GantryDenialField(obbasemesh, mountPoints(1), DeepSurface(lower, py - 1,    px + door,   py - wall, px + gantry)), //SE
      GantryDenialField(obbasemesh, mountPoints(2), DeepSurface(lower, py + wall, px - gantry, py + 1,    px - door)),   //NW
      GantryDenialField(obbasemesh, mountPoints(3), DeepSurface(lower, py - 1,    px - gantry, py - wall, px - door)),   //SW
      GantryDenialField(obbasemesh, mountPoints(4), DeepSurface(upper, py + wall, px + door,   py + 1,    px + gantry)), //NE
      GantryDenialField(obbasemesh, mountPoints(5), DeepSurface(upper, py - 1,    px + door,   py - wall, px + gantry)), //SE
      GantryDenialField(obbasemesh, mountPoints(6), DeepSurface(upper, py + wall, px - gantry, py + 1,    px - door)),   //NW
      GantryDenialField(obbasemesh, mountPoints(7), DeepSurface(upper, py - 1,    px - gantry, py - wall, px - door))    //SW
    )
  }

  /**
    * Generate eight environmental representations that serve to eject players
    * from the high altitude rapid transport (HART) building boarding hallways
    * when the HART shuttle associated with that building is no longer boarding
    * and the doors to those hallways should deny entrance.
    * When kicked out of the hallway,
    * ejected players should be placed in the same position as if the player willingly dismounted the shuttle.<br>
    * <br>
    * The New Conglomerate HART A campus building is at an ordinal angle
    * which makes the typical axis-aligned environment geometry unsuitable for representation of the denial field.
    * Instead of rectangles, circles will be used.
    * This facility is centered at 4816, 3506, 68.73806 (x ,y, z).
    * @param obbasemesh the globally unique identifier of the orbital shuttle pad,
    *                   an amenity of an `orbital_building_*`
    * @return a list of environmental representations
    */
  def specialHartGantryDenialFields(obbasemesh: PlanetSideGUID): List[PieceOfEnvironment] = {
    val lower: Float  = 74.902668f
    val upper: Float  = 86.246418f
    val radius: Float = 6.5f
    //downstairs lobbies are listed before upstairs lobbies to ensure they are tested first
    List(
      GantryDenialField(obbasemesh, 1, DeepCircularSurface(Vector3(4846f, 3547.6016f, lower), radius)), //N
      GantryDenialField(obbasemesh, 2, DeepCircularSurface(Vector3(4857.5234f, 3536f, lower), radius)), //E
      GantryDenialField(obbasemesh, 5, DeepCircularSurface(Vector3(4774.3516f, 3476f, lower), radius)), //W
      GantryDenialField(obbasemesh, 6, DeepCircularSurface(Vector3(4786f, 3464.4453f, lower), radius)), //S
      GantryDenialField(obbasemesh, 3, DeepCircularSurface(Vector3(4846f, 3547.6016f, upper), radius)), //N
      GantryDenialField(obbasemesh, 4, DeepCircularSurface(Vector3(4857.5234f, 3536f, upper), radius)), //E
      GantryDenialField(obbasemesh, 7, DeepCircularSurface(Vector3(4774.3516f, 3476f, upper), radius)), //W
      GantryDenialField(obbasemesh, 8, DeepCircularSurface(Vector3(4786f, 3464.4453f, upper), radius))  //S
    )
  }
}

