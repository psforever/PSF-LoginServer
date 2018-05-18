package csr

// Copyright (c) 2017 PSForever
import net.psforever.types.Vector3

import scala.collection.mutable
import scala.util.Random

/*
The following is STILL for development and fun.
*/
/**
  * A crude representation of the information needed to describe a continent (hitherto, a "zone").
  * The information is mainly catered to the simulation of the CSR commands `/zone` and `/warp`.
  * (The exception is `alias` which is maintained for cosmetic purposes and clarification.)
  * @param alias    the common name of the zone
  * @param map      the map name of the zone (this map is loaded)
  * @param zonename the zone's internal name
  */
class CSRZoneImpl(val alias : String, val map : String, val zonename : String) {
  /**
    * A listing of warpgates, geowarps, and island warpgates in this zone.
    * The coordinates specified will only ever drop the user on a specific point within the protective bubble of the warpgate.
    * This breaks from the expected zoning functionality where the user is placed in a random spot under the bubble.
    * There is no prior usage details for the searchability format of this field's key values.
    */
  private val gates : mutable.HashMap[String, Vector3] = mutable.HashMap()
  /**
    * A listing of special locations in this zone, i.e., major faciities, and some landmarks of interest.
    * There is no prior usage details for the searchability format of this field's key values.
    */
  private val locations : mutable.HashMap[String, Vector3] = mutable.HashMap()
}

object CSRZoneImpl {
  /**
    * A listing of all zones that can be visited by their internal name.
    * The keys in this map should be directly usable by the `/zone` command.
    */
  private val zones = Map[String, CSRZoneImpl](
    "z1" -> CSRZoneImpl("Solsar", "map01", "z1"),
    "z2" -> CSRZoneImpl("Hossin", "map02", "z2"),
    "z3" -> CSRZoneImpl("Cyssor", "map03", "z3"),
    "z4" -> CSRZoneImpl("Ishundar", "map04", "z4"),
    "z5" -> CSRZoneImpl("Forseral", "map05", "z5"),
    "z6" -> CSRZoneImpl("Ceryshen", "map06", "z6"),
    "z7" -> CSRZoneImpl("Esamir", "map07", "z7"),
    "z8" -> CSRZoneImpl("Oshur", "map08", "z8"),
    "z9" -> CSRZoneImpl("Searhus", "map09", "z9"),
    "z10" -> CSRZoneImpl("Amerish", "map10", "z10"),
    "home1" -> CSRZoneImpl("NC Sanctuary", "map11", "home1"),
    "home2" -> CSRZoneImpl("TR Sanctuary", "map12", "home2"),
    "home3" -> CSRZoneImpl("VS Sanctuary", "map13", "home3"),
    "tzshtr" -> CSRZoneImpl("VR Shooting Range TR", "map14", "tzshtr"),
    "tzdrtr" -> CSRZoneImpl("VR Driving Range TR", "map15", "tzdrtr"),
    "tzcotr" -> CSRZoneImpl("VR Combat csr.CSRZoneImpl TR", "map16", "tzcotr"),
    "tzshvs" -> CSRZoneImpl("VR Shooting Range VS", "map14", "tzshvs"),
    "tzdrvs" -> CSRZoneImpl("VR Driving Range VS", "map15", "tzdrvs"),
    "tzcovs" -> CSRZoneImpl("VR Combat csr.CSRZoneImpl VS", "map16", "tzcovs"),
    "tzshnc" -> CSRZoneImpl("VR Shooting Range NC", "map14", "tzshnc"),
    "tzdrnc" -> CSRZoneImpl("VR Driving Range NC", "map15", "tzdrnc"),
    "tzconc" -> CSRZoneImpl("VR Combat csr.CSRZoneImpl NC", "map16", "tzconc"),
    "c1" -> CSRZoneImpl("Supai", "ugd01", "c1"),
    "c2" -> CSRZoneImpl("Hunhau", "ugd02", "c2"),
    "c3" -> CSRZoneImpl("Adlivun", "ugd03", "c3"),
    "c4" -> CSRZoneImpl("Byblos", "ugd04", "c4"),
    "c5" -> CSRZoneImpl("Annwn", "ugd05", "c5"),
    "c6" -> CSRZoneImpl("Drugaskan", "ugd06", "c6"),
    "i4" -> CSRZoneImpl("Nexus", "map96", "i4"),
    "i3" -> CSRZoneImpl("Desolation", "map97", "i3"),
    "i2" -> CSRZoneImpl("Ascension", "map98", "i2"),
    "i1" -> CSRZoneImpl("Extinction", "map99", "i1"),
    "homebo" -> CSRZoneImpl("Black_ops_hq", "Black_ops_hq", "homebo"),
    "station1" -> CSRZoneImpl("TR Station", "Station1", "station1"),
    "station2" -> CSRZoneImpl("NC Station", "Station2", "station2"),
    "station3" -> CSRZoneImpl("VS Station", "Station3", "station3")
  )
  /**
    * A listing of all zones that can be visited by their common name.
    * The keys in this map should be directly usable by the `/zone` command.
    * Though the behavior is undocumented, access to this alias list is for the benefit of the user.
    */
  private val alias = Map[String, String](
    "solsar" -> "z1",
    "hossin" -> "z2",
    "cyssor" -> "z3",
    "ishundar" -> "z4",
    "forseral" -> "z5",
    "ceryshen" -> "z6",
    "esamir" -> "z7",
    "oshur" -> "z8",
    "searhus" -> "z9",
    "amerish" -> "z10",
    "nc-sanctuary" -> "home1",
    "tr-sanctuary" -> "home2",
    "vs-sanctuary" -> "home3",
    "tr-shooting" -> "tzshtr",
    "tr-driving" -> "tzdrtr",
    "tr-combat" -> "tzcotr",
    "vs-shooting" -> "tzshvs",
    "vs-driving" -> "tzdrvs",
    "vs-combat" -> "tzcovs",
    "nc-shooting" -> "tzshnc",
    "nc-driving" -> "tzdrnc",
    "nc-combat" -> "tzconc",
    "supai" -> "c1",
    "hunhau" -> "c2",
    "adlivun" -> "c3",
    "byblos" -> "c4",
    "annwn" -> "c5",
    "drugaskan" -> "c6",
    "nexus" -> "i4",
    "desolation" -> "i3",
    "ascension" -> "i2",
    "extinction" -> "i1",
    "Black_ops_hq" -> "homebo",
    "TR-Station" -> "station1",
    "NC-Station" -> "station2",
    "VS-Station" -> "station3"
  )
  /**
    * A value used for selecting where to appear in a zone from the list of locations when the user has no indicated one.
    */
  private val rand = Random
  setup()

  /**
    * An abbreviated constructor for creating `CSRZone`s without invocation of `new`.
    * @param alias    the common name of the zone
    * @param map      the map name of the zone (this map is loaded)
    * @param zonename the zone's internal name
    */
  def apply(alias : String, map : String, zonename : String) : CSRZoneImpl = new CSRZoneImpl(alias, map, zonename)

  /**
    * Get a valid `CSRZone`'s information.
    * @param zoneId a name that describes the zone and should be searchable
    * @return the `CSRZone`, or `None`
    */
  def get(zoneId : String) : Option[CSRZoneImpl] = {
    var zId = zoneId.toLowerCase
    if(alias.get(zId).isDefined)
      zId = alias(zId)
    zones.get(zId)
  }

  /**
    * Get a location within the `CSRZone`.
    * The location should be a facility or a warpgate or interesting.
    * @param zone  the `CSRZone`
    * @param locId a name that describes a known location in the provided `CSRZone` and is searchable
    * @return the coordinates of that location, or None
    */
  def getWarpLocation(zone : CSRZoneImpl, locId : String) : Option[Vector3] = {
    val low_locId = locId.toLowerCase
    var location = zone.locations.get(low_locId)
    if(location.isEmpty)
      location = zone.gates.get(low_locId)
    location
  }

  /**
    * Get the position of a warpgate within the zone.
    * @param zone   the `CSRZone`
    * @param gateId a name that describes a known warpgate in the provided `CSRZone` and is searchable
    * @return the coordinates of that warpgate, or None
    */
  def getWarpgate(zone : CSRZoneImpl, gateId : String) : Option[Vector3] = {
    zone.gates.get(gateId.toLowerCase)
  }

  /**
    * Get the names for all of the `CSRZones` that can be visited.
    * @return all of the zonenames
    */
  def list : String = {
    "zonenames: z1 - z10, home1 - home3, tzshnc, tzdrnc, tzconc, tzshtr, tzdrtr, tzcotr, tzshvs, tzdrvs, tzcovs, c1 - c6, i1 - i4; zones are also aliased to their continent name"
  }

  /**
    * Get the name for all of the locations that can be visited in this `CSRZone`, excluding warpgates.
    * @param zone the `CSRZone`
    * @return all of the location keys
    */
  def listLocations(zone : CSRZoneImpl) : String = {
    var out : String = "warps: "
    if(zone.locations.nonEmpty) {
      out += zone.locations.keys.toArray.sorted.mkString(", ")
    }
    else
      out = "none"
    out
  }

  /**
    * Get the name for all of the warpgates that can be visited in this `CSRZone`.
    * @param zone the `CSRZone`
    * @return all of the warpgate keys
    */
  def listWarpgates(zone : CSRZoneImpl) : String = {
    var out : String = "gatenames: "
    if(zone.gates.isEmpty)
      out += "none"
    else
      out += zone.gates.keys.toArray.sorted.mkString(", ")
    out
  }

  /**
    * Select, of all the `CSRZone` locations and warpgates, a pseudorandom destination to spawn the player in the zone if none has been specified.
    * @param zone the `CSRZone`
    * @return the coordinates of the spawn point
    */
  def selectRandom(zone : CSRZoneImpl) : Vector3 = {
    var outlets = zone.locations //random location?
    if(outlets.nonEmpty) {
      return outlets.values.toArray.apply(rand.nextInt(outlets.size))
    }
    outlets = zone.gates //random warpgate?
    if(outlets.nonEmpty) {
      return outlets.values.toArray.apply(rand.nextInt(outlets.size))
    }
    Vector3.Zero //fallback coordinates (that will always be valid)
  }

  /**
    * Load all zones with selected places of interest and the coordinates to place the player nearby that given place of interest.
    * All of these keys should be searchable under the `/warp` command.
    * Only the warpgate keys are searchable by the `/zone` command.
    */
  def setup() : Unit = {
    zones("z1").gates += (
      "gate1" -> Vector3(4150, 7341, 82),
      "gate2" -> Vector3(5698, 3404, 129),
      "gate3" -> Vector3(2650, 5363, 176),
      "gate4" -> Vector3(3022, 1225, 66),
      "geowarp1" -> Vector3(3678, 2895, 108),
      "geowarp2" -> Vector3(5672, 4750, 70)
    )
    zones("z1").locations += (
      "amun" -> Vector3(4337, 2278, 68),
      "aton" -> Vector3(3772, 5463, 54),
      "bastet" -> Vector3(5412, 5588, 56),
      "hapi" -> Vector3(4256, 4436, 59),
      "horus" -> Vector3(3725, 2114, 73),
      "mont" -> Vector3(3354, 4205, 83),
      "seth" -> Vector3(4495, 6026, 58),
      "sobek" -> Vector3(3094, 3027, 75),
      "thoth" -> Vector3(4615, 3373, 53),
      "lake" -> Vector3(4317, 4008, 37),
      "monolith" -> Vector3(5551, 5047, 64)
    )
    zones("z2").gates += (
      "gate1" -> Vector3(1881, 4873, 19),
      "gate2" -> Vector3(4648, 4625, 28),
      "gate3" -> Vector3(3296, 2045, 21),
      "gate4" -> Vector3(5614, 1781, 32),
      "geowarp1" -> Vector3(5199, 4869, 39),
      "geowarp2" -> Vector3(3911, 2407, 15)
    )
    zones("z2").locations += (
      "acan" -> Vector3(3534, 4015, 30),
      "bitol" -> Vector3(4525, 2632, 30),
      "chac" -> Vector3(4111, 5950, 39),
      "ghanon" -> Vector3(2565, 3707, 41),
      "hurakan" -> Vector3(1840, 2934, 38),
      "ixtab" -> Vector3(3478, 3143, 40),
      "kisin" -> Vector3(3356, 5374, 31),
      "mulac" -> Vector3(5592, 2738, 37),
      "naum" -> Vector3(5390, 3454, 28),
      "voltan" -> Vector3(4529, 3414, 28),
      "zotz" -> Vector3(6677, 2342, 129),
      "monolith" -> Vector3(2938, 2485, 14)
    )
    zones("z3").gates += (
      "gate1" -> Vector3(2616, 6567, 58),
      "gate2" -> Vector3(6980, 5336, 57),
      "gate3" -> Vector3(1199, 1332, 66),
      "gate4" -> Vector3(5815, 1974, 63),
      "geowarp1" -> Vector3(2403, 4278, 60),
      "geowarp2" -> Vector3(4722, 2665, 78)
    )
    zones("z3").locations += (
      "aja" -> Vector3(754, 5435, 48),
      "chuku" -> Vector3(4208, 7021, 54),
      "bomazi" -> Vector3(1198, 4492, 58),
      "ekera" -> Vector3(5719, 6555, 51),
      "faro" -> Vector3(5030, 5700, 57),
      "gunuku" -> Vector3(4994, 4286, 54),
      "honsi" -> Vector3(4042, 4588, 89),
      "itan" -> Vector3(5175, 3393, 48),
      "kaang" -> Vector3(5813, 3862, 62),
      "leza" -> Vector3(2691, 1561, 64),
      "mukuru" -> Vector3(661, 2380, 54),
      "nzame" -> Vector3(1670, 2706, 45),
      "orisha" -> Vector3(7060, 1327, 59),
      "pamba" -> Vector3(7403, 3123, 63),
      "shango" -> Vector3(6846, 2319, 63),
      "tore" -> Vector3(3017, 2272, 58),
      "wele" -> Vector3(436, 7040, 60),
      "monolith" -> Vector3(4515, 4105, 38),
      "peak" -> Vector3(3215, 5063, 579)
    )
    zones("z4").gates += (
      "gate1" -> Vector3(4702, 6768, 30),
      "gate2" -> Vector3(5515, 3368, 69),
      "gate3" -> Vector3(1564, 3356, 46),
      "gate4" -> Vector3(3889, 1118, 56),
      "geowarp1" -> Vector3(4202, 4325, 68),
      "geowarp2" -> Vector3(2384, 1925, 37)
    )
    zones("z4").locations += (
      "akkan" -> Vector3(2746, 4260, 39),
      "baal" -> Vector3(825, 5470, 72),
      "dagon" -> Vector3(1739, 5681, 40),
      "enkidu" -> Vector3(3217, 3574, 37),
      "girru" -> Vector3(4475, 5853, 78),
      "hanish" -> Vector3(3794, 5540, 89),
      "irkall" -> Vector3(4742, 5270, 66),
      "kusag" -> Vector3(6532, 4692, 46),
      "lahar" -> Vector3(6965, 5306, 38),
      "marduk" -> Vector3(3059, 2144, 70),
      "neti" -> Vector3(3966, 2417, 80),
      "zaqar" -> Vector3(4796, 2177, 75),
      "monolith" -> Vector3(5165, 4083, 35),
      "stonehenge" -> Vector3(4992, 3776, 56)
    )
    zones("z5").gates += (
      "gate1" -> Vector3(3432, 6498, 73),
      "gate2" -> Vector3(7196, 3917, 47),
      "gate3" -> Vector3(1533, 3540, 56),
      "gate4" -> Vector3(3197, 1390, 45),
      "geowarp1" -> Vector3(4899, 5633, 38),
      "geowarp2" -> Vector3(5326, 2558, 54)
    )
    zones("z5").locations += (
      "anu" -> Vector3(3479, 2556, 56),
      "bel" -> Vector3(3665, 4626, 58),
      "caer" -> Vector3(4570, 2601, 56),
      "dagd" -> Vector3(5825, 4449, 55),
      "eadon" -> Vector3(2725, 2853, 53),
      "gwydion" -> Vector3(5566, 3739, 61),
      "lugh" -> Vector3(6083, 5069, 72),
      "neit" -> Vector3(4345, 4319, 76),
      "ogma" -> Vector3(3588, 3227, 114),
      "pwyll" -> Vector3(4683, 4764, 104),
      "monolith" -> Vector3(3251, 3245, 160),
      "islands1" -> Vector3(6680, 6217, 125),
      "islands2" -> Vector3(1059, 6213, 120)
    )
    zones("z6").gates += (
      "gate1" -> Vector3(5040, 4327, 46),
      "gate2" -> Vector3(2187, 5338, 30),
      "gate3" -> Vector3(4960, 1922, 15),
      "gate4" -> Vector3(2464, 3088, 189),
      "geowarp1" -> Vector3(3221, 5328, 242),
      "geowarp2" -> Vector3(2237, 1783, 238)
    )
    zones("z6").locations += (
      "akna" -> Vector3(4509, 3732, 219),
      "anguta" -> Vector3(3999, 4170, 266),
      "igaluk" -> Vector3(3241, 5658, 235),
      "keelut" -> Vector3(3630, 1904, 265),
      "nerrivik" -> Vector3(3522, 3703, 322),
      "pinga" -> Vector3(5938, 3545, 96),
      "sedna" -> Vector3(3932, 5160, 232),
      "tarqaq" -> Vector3(2980, 2155, 237),
      "tootega" -> Vector3(5171, 3251, 217),
      "monolith" -> Vector3(4011, 4851, 32),
      "bridge" -> Vector3(3729, 4859, 234)
    )
    zones("z7").gates += (
      "gate1" -> Vector3(1516, 6448, 61),
      "gate2" -> Vector3(5249, 3819, 69),
      "gate3" -> Vector3(2763, 2961, 86),
      "gate4" -> Vector3(6224, 1152, 78),
      "geowarp1" -> Vector3(6345, 4802, 90),
      "geowarp2" -> Vector3(3800, 2197, 64)
    )
    zones("z7").locations += (
      "andvari" -> Vector3(3233, 7207, 78),
      "dagur" -> Vector3(4026, 6191, 60),
      "eisa" -> Vector3(3456, 4513, 75),
      "freyr" -> Vector3(2853, 3840, 56),
      "gjallar" -> Vector3(1056, 2656, 74),
      "helheim" -> Vector3(5542, 2532, 53),
      "jarl" -> Vector3(1960, 5462, 68),
      "kvasir" -> Vector3(4096, 1571, 69),
      "mani" -> Vector3(5057, 4989, 58),
      "nott" -> Vector3(6783, 4329, 46),
      "ran" -> Vector3(2378, 1919, 85),
      "vidar" -> Vector3(3772, 3024, 67),
      "ymir" -> Vector3(1911, 4008, 69),
      "monolith" -> Vector3(6390, 1622, 63)
    )
    zones("z8").gates += (
      "gate1" -> Vector3(5437, 5272, 32),
      "gate2" -> Vector3(3251, 5650, 60),
      "gate3" -> Vector3(5112, 2616, 40),
      "gate4" -> Vector3(2666, 1665, 45),
      "geowarp1" -> Vector3(3979, 5370, 47),
      "geowarp2" -> Vector3(6018, 3136, 35)
    )
    zones("z8").locations += (
      "atar" -> Vector3(3609, 2730, 47),
      "dahaka" -> Vector3(4633, 5379, 54),
      "hvar" -> Vector3(3857, 4764, 49),
      "izha" -> Vector3(5396, 3852, 51),
      "jamshid" -> Vector3(2371, 3378, 52),
      "mithra" -> Vector3(2480, 4456, 44),
      "rashnu" -> Vector3(3098, 3961, 59),
      "yazata" -> Vector3(4620, 3983, 62),
      "zal" -> Vector3(3966, 2164, 61),
      "arch1" -> Vector3(4152, 3285, 31),
      "arch2" -> Vector3(4688, 5272, 68),
      "pride" -> Vector3(2913, 4412, 63)
    )
    zones("z9").gates += (
      "gate1" -> Vector3(1505, 6981, 65),
      "gate2" -> Vector3(6835, 3517, 56),
      "gate3" -> Vector3(1393, 1376, 53),
      "geowarp1" -> Vector3(7081, 5552, 46),
      "geowarp2" -> Vector3(3776, 1092, 49)
    )
    zones("z9").locations += (
      "akua" -> Vector3(5258, 4041, 346),
      "drakulu" -> Vector3(3806, 2647, 151),
      "hiro" -> Vector3(4618, 5761, 190),
      "iva" -> Vector3(6387, 5199, 55),
      "karihi" -> Vector3(3879, 5574, 236),
      "laka" -> Vector3(4720, 6718, 49),
      "matagi" -> Vector3(5308, 5093, 239),
      "ngaru" -> Vector3(4103, 4077, 205),
      "oro" -> Vector3(4849, 4456, 208),
      "pele" -> Vector3(4549, 3712, 208),
      "rehua" -> Vector3(3843, 2195, 60),
      "sina" -> Vector3(5919, 2177, 91),
      "tara" -> Vector3(1082, 4225, 60),
      "wakea" -> Vector3(1785, 5241, 63),
      "monolith" -> Vector3(3246, 6507, 105)
    )
    zones("z10").gates += (
      "gate1" -> Vector3(6140, 6599, 71),
      "gate2" -> Vector3(4814, 4608, 59),
      "gate3" -> Vector3(3152, 3480, 54),
      "gate4" -> Vector3(1605, 1446, 40),
      "geowarp1" -> Vector3(3612, 6918, 38),
      "geowarp2" -> Vector3(3668, 3327, 55)
    )
    zones("z10").locations += (
      "azeban" -> Vector3(6316, 5160, 62),
      "cetan" -> Vector3(3587, 2522, 48),
      "heyoka" -> Vector3(4395, 2327, 47),
      "ikanam" -> Vector3(2740, 2412, 57),
      "kyoi" -> Vector3(5491, 2284, 62),
      "mekala" -> Vector3(6087, 2925, 59),
      "onatha" -> Vector3(3397, 5799, 48),
      "qumu" -> Vector3(3990, 5152, 46),
      "sungrey" -> Vector3(4609, 5624, 72),
      "tumas" -> Vector3(4687, 6392, 69),
      "verica" -> Vector3(4973, 3459, 47),
      "xelas" -> Vector3(6609, 4479, 56),
      "monolith" -> Vector3(5651, 6024, 38)
    )
    zones("home1").gates += (
      "gate1" -> Vector3(4158, 6344, 44),
      "gate2" -> Vector3(2214, 5797, 48),
      "gate3" -> Vector3(5032, 3241, 53)
    )
    zones("home1").locations += "hart_c" -> Vector3(2352, 5523, 66)
    zones("home2").gates += (
      "gate1" -> Vector3(5283, 4317, 44),
      "gate2" -> Vector3(3139, 4809, 40),
      "gate3" -> Vector3(3659, 2894, 26)
    )
    zones("home2").locations += "hart_c" -> Vector3(3125, 2864, 35)
    zones("home3").gates += (
      "gate1" -> Vector3(5657, 4681, 98),
      "gate2" -> Vector3(2639, 5366, 57),
      "gate3" -> Vector3(4079, 2467, 155)
    )
    zones("home3").locations += "hart_c" -> Vector3(3675, 2727, 91)
    zones("tzshtr").locations += "roof" -> Vector3(499, 1568, 25)
    zones("tzcotr").locations += "spawn" -> Vector3(960, 1002, 32)
    zones("tzdrtr").locations += (
      "start" -> Vector3(2457, 1864, 23),
      "air_pad" -> Vector3(1700, 1900, 32)
    )
    zones("tzshvs").locations += "roof" -> Vector3(499, 1568, 25)
    zones("tzcovs").locations += "spawn" -> Vector3(960, 1002, 32)
    zones("tzdrvs").locations += (
      "start" -> Vector3(2457, 1864, 23),
      "air_pad" -> Vector3(1700, 1900, 32)
    )
    zones("tzshnc").locations += "roof" -> Vector3(499, 1568, 25)
    zones("tzconc").locations += "spawn" -> Vector3(960, 1002, 32)
    zones("tzdrnc").locations += (
      "start" -> Vector3(2457, 1864, 23),
      "air_pad" -> Vector3(1700, 1900, 32)
    )
    zones("c1").gates += (
      "geowarp1" -> Vector3(998, 2038, 103),
      "geowarp2" -> Vector3(231, 1026, 82),
      "geowarp3" -> Vector3(2071, 1405, 102),
      "geowarp4" -> Vector3(1051, 370, 103)
    )
    zones("c2").gates += (
      "geowarp1" -> Vector3(999, 2386, 243),
      "geowarp2" -> Vector3(283, 1249, 172),
      "geowarp3" -> Vector3(1887, 1307, 192),
      "geowarp4" -> Vector3(1039, 155, 143)
    )
    zones("c3").gates += (
      "geowarp1" -> Vector3(1095, 1725, 25),
      "geowarp2" -> Vector3(226, 832, 42),
      "geowarp3" -> Vector3(1832, 1026, 43),
      "geowarp4" -> Vector3(981, 320, 46)
    )
    zones("c4").gates += (
      "geowarp1" -> Vector3(902, 1811, 93),
      "geowarp2" -> Vector3(185, 922, 113),
      "geowarp3" -> Vector3(1696, 1188, 92),
      "geowarp4" -> Vector3(887, 227, 115)
    )
    zones("c5").gates += (
      "geowarp1" -> Vector3(1195, 1752, 244),
      "geowarp2" -> Vector3(290, 1104, 235),
      "geowarp3" -> Vector3(1803, 899, 243),
      "geowarp4" -> Vector3(1042, 225, 246)
    )
    zones("c6").gates += (
      "geowarp1" -> Vector3(1067, 2044, 95),
      "geowarp2" -> Vector3(290, 693, 73),
      "geowarp3" -> Vector3(1922, 928, 33),
      "geowarp4" -> Vector3(1174, 249, 114)
    )
    zones("i3").gates += (
      "gate1" -> Vector3(1219, 2580, 30),
      "gate2" -> Vector3(2889, 2919, 33),
      "gate3" -> Vector3(2886, 1235, 32)
    )
    zones("i3").locations += (
      "dahaka" -> Vector3(1421, 2216, 30),
      "jamshid" -> Vector3(2500, 2543, 30),
      "izha" -> Vector3(2569, 1544, 30),
      "oasis" -> Vector3(2084, 1935, 40)
    )
    zones("i2").gates += (
      "gate1" -> Vector3(1243, 1393, 12),
      "gate2" -> Vector3(2510, 2544, 12),
      "gate3" -> Vector3(2634, 1477, 12)
    )
    zones("i2").locations += (
      "rashnu" -> Vector3(1709, 1802, 91),
      "sraosha" -> Vector3(2729, 2349, 91),
      "zal" -> Vector3(1888, 2728, 91),
      "center" -> Vector3(2082, 2192, 160),
      "vpad" -> Vector3(1770, 2686, 92)
    )
    zones("i1").gates += (
      "gate1" -> Vector3(1225, 2036, 67),
      "gate2" -> Vector3(2548, 2801, 65),
      "gate3" -> Vector3(2481, 1194, 89)
    )
    zones("i1").locations += (
      "hvar" -> Vector3(1559, 1268, 88),
      "mithra" -> Vector3(2855, 2850, 89),
      "yazata" -> Vector3(1254, 2583, 88),
      "south_of_volcano" -> Vector3(2068, 1686, 88)
    )
    zones("i4").gates += (
      "gate1" -> Vector3(2359, 2717, 36),
      "gate2" -> Vector3(2732, 1355, 36),
      "geowarp" -> Vector3(1424, 1640, 45)
    )
    zones("i4").locations += "atar" -> Vector3(1915, 1936, 43)
  }
}
