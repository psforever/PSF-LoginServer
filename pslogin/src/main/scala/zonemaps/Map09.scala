package zonemaps

import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.generator.Generator
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.painbox.Painbox
import net.psforever.objects.serverobject.resourcesilo.ResourceSilo
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.{CaptureTerminal, ProximityTerminal, Terminal}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.zones.ZoneMap
import net.psforever.types.Vector3

object Map09 { // Searhus
  val ZoneMap = new ZoneMap("map09") {
    Checksum = 1380643455L

    Building14()

    def Building14(): Unit = { // Name: Rehua Type: amp_station GUID: 1, MapID: 14
      LocalBuilding(
        "Rehua",
        1,
        14,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3736f, 2160f, 59.77203f),
            Vector3(0f, 0f, 333f),
            amp_station
          )
        )
      )
      LocalObject(
        213,
        CaptureTerminal.Constructor(Vector3(3733.029f, 2161.517f, 71.28003f), capture_terminal),
        owning_building_guid = 1
      )
      LocalObject(163, Door.Constructor(Vector3(3733.099f, 2153.841f, 72.67403f)), owning_building_guid = 1)
      LocalObject(164, Door.Constructor(Vector3(3739.28f, 2165.966f, 72.67403f)), owning_building_guid = 1)
      LocalObject(389, Door.Constructor(Vector3(3650.236f, 2141.41f, 61.52303f)), owning_building_guid = 1)
      LocalObject(391, Door.Constructor(Vector3(3658.495f, 2157.62f, 69.48602f)), owning_building_guid = 1)
      LocalObject(399, Door.Constructor(Vector3(3696.183f, 2231.587f, 61.52303f)), owning_building_guid = 1)
      LocalObject(400, Door.Constructor(Vector3(3704.442f, 2247.797f, 69.48602f)), owning_building_guid = 1)
      LocalObject(401, Door.Constructor(Vector3(3704.603f, 2265.784f, 61.49303f)), owning_building_guid = 1)
      LocalObject(403, Door.Constructor(Vector3(3713.361f, 2077.015f, 69.48703f)), owning_building_guid = 1)
      LocalObject(404, Door.Constructor(Vector3(3718.26f, 2134.723f, 71.49902f)), owning_building_guid = 1)
      LocalObject(405, Door.Constructor(Vector3(3719.883f, 2128.369f, 66.49303f)), owning_building_guid = 1)
      LocalObject(407, Door.Constructor(Vector3(3726.334f, 2130.61f, 71.49902f)), owning_building_guid = 1)
      LocalObject(408, Door.Constructor(Vector3(3729.57f, 2068.756f, 61.52303f)), owning_building_guid = 1)
      LocalObject(410, Door.Constructor(Vector3(3746.041f, 2189.199f, 71.49902f)), owning_building_guid = 1)
      LocalObject(412, Door.Constructor(Vector3(3752.117f, 2191.632f, 66.49303f)), owning_building_guid = 1)
      LocalObject(413, Door.Constructor(Vector3(3754.115f, 2185.086f, 71.49902f)), owning_building_guid = 1)
      LocalObject(415, Door.Constructor(Vector3(3756.911f, 2252.808f, 61.52303f)), owning_building_guid = 1)
      LocalObject(417, Door.Constructor(Vector3(3761.179f, 2076.809f, 69.48703f)), owning_building_guid = 1)
      LocalObject(419, Door.Constructor(Vector3(3769.438f, 2093.018f, 61.52303f)), owning_building_guid = 1)
      LocalObject(421, Door.Constructor(Vector3(3773.12f, 2244.549f, 69.48703f)), owning_building_guid = 1)
      LocalObject(673, Door.Constructor(Vector3(3684.559f, 2217.635f, 53.99303f)), owning_building_guid = 1)
      LocalObject(675, Door.Constructor(Vector3(3691.416f, 2178.228f, 46.49303f)), owning_building_guid = 1)
      LocalObject(677, Door.Constructor(Vector3(3696.796f, 2179.976f, 46.49303f)), owning_building_guid = 1)
      LocalObject(678, Door.Constructor(Vector3(3702.04f, 2163.836f, 46.49303f)), owning_building_guid = 1)
      LocalObject(679, Door.Constructor(Vector3(3714.412f, 2144.064f, 61.49303f)), owning_building_guid = 1)
      LocalObject(681, Door.Constructor(Vector3(3716.16f, 2138.684f, 61.49303f)), owning_building_guid = 1)
      LocalObject(682, Door.Constructor(Vector3(3718.044f, 2151.192f, 46.49303f)), owning_building_guid = 1)
      LocalObject(683, Door.Constructor(Vector3(3718.044f, 2151.192f, 53.99303f)), owning_building_guid = 1)
      LocalObject(685, Door.Constructor(Vector3(3721.948f, 2194.096f, 53.99303f)), owning_building_guid = 1)
      LocalObject(687, Door.Constructor(Vector3(3724.127f, 2136.231f, 71.49303f)), owning_building_guid = 1)
      LocalObject(688, Door.Constructor(Vector3(3725.104f, 2138.616f, 66.49303f)), owning_building_guid = 1)
      LocalObject(689, Door.Constructor(Vector3(3728.94f, 2172.576f, 53.99303f)), owning_building_guid = 1)
      LocalObject(690, Door.Constructor(Vector3(3730.552f, 2149.308f, 46.49303f)), owning_building_guid = 1)
      LocalObject(692, Door.Constructor(Vector3(3736.204f, 2186.832f, 61.49303f)), owning_building_guid = 1)
      LocalObject(695, Door.Constructor(Vector3(3737.816f, 2163.564f, 53.99303f)), owning_building_guid = 1)
      LocalObject(696, Door.Constructor(Vector3(3737.952f, 2181.452f, 53.99303f)), owning_building_guid = 1)
      LocalObject(698, Door.Constructor(Vector3(3739.7f, 2176.072f, 46.49303f)), owning_building_guid = 1)
      LocalObject(699, Door.Constructor(Vector3(3741.584f, 2188.58f, 61.49303f)), owning_building_guid = 1)
      LocalObject(702, Door.Constructor(Vector3(3746.896f, 2181.384f, 66.49303f)), owning_building_guid = 1)
      LocalObject(704, Door.Constructor(Vector3(3748.251f, 2183.577f, 71.49303f)), owning_building_guid = 1)
      LocalObject(708, Door.Constructor(Vector3(3752.004f, 2147.356f, 61.49303f)), owning_building_guid = 1)
      LocalObject(709, Door.Constructor(Vector3(3755.636f, 2154.484f, 61.49303f)), owning_building_guid = 1)
      LocalObject(939, Door.Constructor(Vector3(3762.006f, 2146.779f, 62.25203f)), owning_building_guid = 1)
      LocalObject(2762, Door.Constructor(Vector3(3714.118f, 2159.626f, 54.32603f)), owning_building_guid = 1)
      LocalObject(2763, Door.Constructor(Vector3(3717.429f, 2166.125f, 54.32603f)), owning_building_guid = 1)
      LocalObject(2764, Door.Constructor(Vector3(3720.739f, 2172.619f, 54.32603f)), owning_building_guid = 1)
      LocalObject(
        991,
        IFFLock.Constructor(Vector3(3765.828f, 2148.243f, 61.45203f), Vector3(0, 0, 117)),
        owning_building_guid = 1,
        door_guid = 939
      )
      LocalObject(
        1074,
        IFFLock.Constructor(Vector3(3692.851f, 2179.26f, 46.30803f), Vector3(0, 0, 117)),
        owning_building_guid = 1,
        door_guid = 675
      )
      LocalObject(
        1075,
        IFFLock.Constructor(Vector3(3703.156f, 2267.448f, 61.43203f), Vector3(0, 0, 27)),
        owning_building_guid = 1,
        door_guid = 401
      )
      LocalObject(
        1076,
        IFFLock.Constructor(Vector3(3716.618f, 2133.291f, 71.43303f), Vector3(0, 0, 297)),
        owning_building_guid = 1,
        door_guid = 404
      )
      LocalObject(
        1077,
        IFFLock.Constructor(Vector3(3719.077f, 2149.756f, 53.80803f), Vector3(0, 0, 207)),
        owning_building_guid = 1,
        door_guid = 683
      )
      LocalObject(
        1078,
        IFFLock.Constructor(Vector3(3721.34f, 2126.729f, 66.43403f), Vector3(0, 0, 207)),
        owning_building_guid = 1,
        door_guid = 405
      )
      LocalObject(
        1079,
        IFFLock.Constructor(Vector3(3727.996f, 2132.083f, 71.43303f), Vector3(0, 0, 117)),
        owning_building_guid = 1,
        door_guid = 407
      )
      LocalObject(
        1080,
        IFFLock.Constructor(Vector3(3727.966f, 2174.128f, 53.80803f), Vector3(0, 0, 27)),
        owning_building_guid = 1,
        door_guid = 689
      )
      LocalObject(
        1081,
        IFFLock.Constructor(Vector3(3729.001f, 2148.334f, 46.30803f), Vector3(0, 0, 297)),
        owning_building_guid = 1,
        door_guid = 690
      )
      LocalObject(
        1083,
        IFFLock.Constructor(Vector3(3744.374f, 2187.765f, 71.43303f), Vector3(0, 0, 297)),
        owning_building_guid = 1,
        door_guid = 410
      )
      LocalObject(
        1086,
        IFFLock.Constructor(Vector3(3750.668f, 2193.29f, 66.43403f), Vector3(0, 0, 27)),
        owning_building_guid = 1,
        door_guid = 412
      )
      LocalObject(
        1087,
        IFFLock.Constructor(Vector3(3755.753f, 2186.56f, 71.43303f), Vector3(0, 0, 117)),
        owning_building_guid = 1,
        door_guid = 413
      )
      LocalObject(1348, Locker.Constructor(Vector3(3722.191f, 2151.482f, 52.73302f)), owning_building_guid = 1)
      LocalObject(1349, Locker.Constructor(Vector3(3723.228f, 2150.953f, 52.73302f)), owning_building_guid = 1)
      LocalObject(1350, Locker.Constructor(Vector3(3724.25f, 2150.433f, 52.73302f)), owning_building_guid = 1)
      LocalObject(1351, Locker.Constructor(Vector3(3725.274f, 2149.911f, 52.73302f)), owning_building_guid = 1)
      LocalObject(1352, Locker.Constructor(Vector3(3740.754f, 2164.497f, 44.97203f)), owning_building_guid = 1)
      LocalObject(1353, Locker.Constructor(Vector3(3741.933f, 2163.896f, 44.97203f)), owning_building_guid = 1)
      LocalObject(1354, Locker.Constructor(Vector3(3743.124f, 2163.289f, 44.97203f)), owning_building_guid = 1)
      LocalObject(1355, Locker.Constructor(Vector3(3744.315f, 2162.682f, 44.97203f)), owning_building_guid = 1)
      LocalObject(1366, Locker.Constructor(Vector3(3748.36f, 2160.621f, 44.97203f)), owning_building_guid = 1)
      LocalObject(1367, Locker.Constructor(Vector3(3749.54f, 2160.02f, 44.97203f)), owning_building_guid = 1)
      LocalObject(1368, Locker.Constructor(Vector3(3750.73f, 2159.414f, 44.97203f)), owning_building_guid = 1)
      LocalObject(1369, Locker.Constructor(Vector3(3751.922f, 2158.807f, 44.97203f)), owning_building_guid = 1)
      LocalObject(
        1881,
        Terminal.Constructor(Vector3(3715.98f, 2170.196f, 61.30103f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1882,
        Terminal.Constructor(Vector3(3725.794f, 2139.974f, 61.30103f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1883,
        Terminal.Constructor(Vector3(3727.336f, 2154.771f, 54.06203f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1884,
        Terminal.Constructor(Vector3(3729.03f, 2158.096f, 54.06203f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1885,
        Terminal.Constructor(Vector3(3730.75f, 2161.472f, 54.06203f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1886,
        Terminal.Constructor(Vector3(3746.203f, 2180.028f, 61.30103f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2656,
        Terminal.Constructor(Vector3(3711.175f, 2216.879f, 54.02903f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2657,
        Terminal.Constructor(Vector3(3713.253f, 2157.272f, 54.60603f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2658,
        Terminal.Constructor(Vector3(3716.56f, 2163.771f, 54.60603f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2659,
        Terminal.Constructor(Vector3(3719.872f, 2170.264f, 54.60603f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2660,
        Terminal.Constructor(Vector3(3721.663f, 2184.599f, 46.52903f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2661,
        Terminal.Constructor(Vector3(3729.053f, 2158.489f, 66.50002f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2911,
        Terminal.Constructor(Vector3(3730.74f, 2255.241f, 61.87403f), vehicle_terminal_combined),
        owning_building_guid = 1
      )
      LocalObject(
        1801,
        VehicleSpawnPad.Constructor(Vector3(3724.629f, 2243.049f, 57.71703f), mb_pad_creation, Vector3(0, 0, 207)),
        owning_building_guid = 1,
        terminal_guid = 2911
      )
      LocalObject(2519, ResourceSilo.Constructor(Vector3(3744.093f, 2059.923f, 67.00703f)), owning_building_guid = 1)
      LocalObject(
        2557,
        SpawnTube.Constructor(Vector3(3713.25f, 2158.891f, 52.47203f), Vector3(0, 0, 27)),
        owning_building_guid = 1
      )
      LocalObject(
        2558,
        SpawnTube.Constructor(Vector3(3716.56f, 2165.387f, 52.47203f), Vector3(0, 0, 27)),
        owning_building_guid = 1
      )
      LocalObject(
        2559,
        SpawnTube.Constructor(Vector3(3719.868f, 2171.881f, 52.47203f), Vector3(0, 0, 27)),
        owning_building_guid = 1
      )
      LocalObject(
        1820,
        ProximityTerminal.Constructor(Vector3(3716.352f, 2170.004f, 64.97202f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1821,
        ProximityTerminal.Constructor(Vector3(3746.075f, 2161.174f, 44.97203f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2078,
        ProximityTerminal.Constructor(Vector3(3693.968f, 2203.606f, 68.27702f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2079,
        Terminal.Constructor(Vector3(3693.968f, 2203.606f, 68.27702f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2081,
        ProximityTerminal.Constructor(Vector3(3710.028f, 2090.748f, 68.30003f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2082,
        Terminal.Constructor(Vector3(3710.028f, 2090.748f, 68.30003f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2090,
        ProximityTerminal.Constructor(Vector3(3770.99f, 2119.913f, 68.27702f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2091,
        Terminal.Constructor(Vector3(3770.99f, 2119.913f, 68.27702f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2093,
        ProximityTerminal.Constructor(Vector3(3785.763f, 2199.08f, 70.43803f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2094,
        Terminal.Constructor(Vector3(3785.763f, 2199.08f, 70.43803f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2425,
        ProximityTerminal.Constructor(Vector3(3673.636f, 2192.004f, 59.17203f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2426,
        Terminal.Constructor(Vector3(3673.636f, 2192.004f, 59.17203f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2437,
        ProximityTerminal.Constructor(Vector3(3791.71f, 2132.056f, 59.17203f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2438,
        Terminal.Constructor(Vector3(3791.71f, 2132.056f, 59.17203f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1702,
        FacilityTurret.Constructor(Vector3(3620.226f, 2110.306f, 68.48003f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1702, 5000)
      LocalObject(
        1706,
        FacilityTurret.Constructor(Vector3(3672.307f, 2081.269f, 68.48003f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1706, 5001)
      LocalObject(
        1708,
        FacilityTurret.Constructor(Vector3(3711.759f, 2290.015f, 68.48003f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1708, 5002)
      LocalObject(
        1710,
        FacilityTurret.Constructor(Vector3(3756.928f, 2040.641f, 68.48003f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1710, 5003)
      LocalObject(
        1713,
        FacilityTurret.Constructor(Vector3(3821.697f, 2162.852f, 68.48003f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1713, 5004)
      LocalObject(
        1716,
        FacilityTurret.Constructor(Vector3(3848.496f, 2220.333f, 68.48003f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1716, 5005)
      LocalObject(
        2254,
        Painbox.Constructor(Vector3(3679.549f, 2181.039f, 49.84303f), painbox),
        owning_building_guid = 1
      )
      LocalObject(
        2268,
        Painbox.Constructor(Vector3(3724.314f, 2164.368f, 57.42043f), painbox_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2282,
        Painbox.Constructor(Vector3(3693.028f, 2176.558f, 46.43103f), painbox_door_radius),
        owning_building_guid = 1
      )
      LocalObject(
        2300,
        Painbox.Constructor(Vector3(3717.562f, 2150.244f, 53.67203f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2301,
        Painbox.Constructor(Vector3(3729.589f, 2173.687f, 53.73653f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2302,
        Painbox.Constructor(Vector3(3739.104f, 2162.86f, 55.65063f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(299, Generator.Constructor(Vector3(3677.545f, 2185.267f, 43.67802f)), owning_building_guid = 1)
      LocalObject(
        285,
        Terminal.Constructor(Vector3(3684.865f, 2181.59f, 44.97203f), gen_control),
        owning_building_guid = 1
      )
    }

    Building13()

    def Building13(): Unit = { // Name: Pele Type: amp_station GUID: 4, MapID: 13
      LocalBuilding(
        "Pele",
        4,
        13,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4440f, 3688f, 208.002f),
            Vector3(0f, 0f, 327f),
            amp_station
          )
        )
      )
      LocalObject(
        217,
        CaptureTerminal.Constructor(Vector3(4437.204f, 3689.819f, 219.5099f), capture_terminal),
        owning_building_guid = 4
      )
      LocalObject(165, Door.Constructor(Vector3(4436.471f, 3682.178f, 220.904f)), owning_building_guid = 4)
      LocalObject(166, Door.Constructor(Vector3(4443.886f, 3693.591f, 220.904f)), owning_building_guid = 4)
      LocalObject(455, Door.Constructor(Vector3(4352.762f, 3678.477f, 209.753f)), owning_building_guid = 4)
      LocalObject(458, Door.Constructor(Vector3(4362.671f, 3693.735f, 217.716f)), owning_building_guid = 4)
      LocalObject(461, Door.Constructor(Vector3(4407.884f, 3763.357f, 209.753f)), owning_building_guid = 4)
      LocalObject(462, Door.Constructor(Vector3(4408.811f, 3607.836f, 217.7169f)), owning_building_guid = 4)
      LocalObject(463, Door.Constructor(Vector3(4417.792f, 3778.615f, 217.716f)), owning_building_guid = 4)
      LocalObject(464, Door.Constructor(Vector3(4419.715f, 3664.716f, 219.729f)), owning_building_guid = 4)
      LocalObject(465, Door.Constructor(Vector3(4419.832f, 3796.486f, 209.7229f)), owning_building_guid = 4)
      LocalObject(466, Door.Constructor(Vector3(4420.666f, 3658.227f, 214.7229f)), owning_building_guid = 4)
      LocalObject(467, Door.Constructor(Vector3(4424.068f, 3597.928f, 209.753f)), owning_building_guid = 4)
      LocalObject(468, Door.Constructor(Vector3(4427.314f, 3659.781f, 219.729f)), owning_building_guid = 4)
      LocalObject(469, Door.Constructor(Vector3(4453.038f, 3715.99f, 219.729f)), owning_building_guid = 4)
      LocalObject(470, Door.Constructor(Vector3(4456.345f, 3602.632f, 217.7169f)), owning_building_guid = 4)
      LocalObject(471, Door.Constructor(Vector3(4459.335f, 3717.774f, 214.7229f)), owning_building_guid = 4)
      LocalObject(472, Door.Constructor(Vector3(4460.637f, 3711.056f, 219.729f)), owning_building_guid = 4)
      LocalObject(473, Door.Constructor(Vector3(4466.253f, 3617.889f, 209.753f)), owning_building_guid = 4)
      LocalObject(474, Door.Constructor(Vector3(4470.498f, 3778.114f, 209.753f)), owning_building_guid = 4)
      LocalObject(475, Door.Constructor(Vector3(4485.755f, 3768.206f, 217.7169f)), owning_building_guid = 4)
      LocalObject(755, Door.Constructor(Vector3(4394.866f, 3750.697f, 202.2229f)), owning_building_guid = 4)
      LocalObject(756, Door.Constructor(Vector3(4397.565f, 3710.788f, 194.7229f)), owning_building_guid = 4)
      LocalObject(757, Door.Constructor(Vector3(4403.099f, 3711.964f, 194.7229f)), owning_building_guid = 4)
      LocalObject(758, Door.Constructor(Vector3(4406.627f, 3695.365f, 194.7229f)), owning_building_guid = 4)
      LocalObject(759, Door.Constructor(Vector3(4416.865f, 3674.407f, 209.7229f)), owning_building_guid = 4)
      LocalObject(760, Door.Constructor(Vector3(4418.041f, 3668.874f, 209.7229f)), owning_building_guid = 4)
      LocalObject(761, Door.Constructor(Vector3(4421.222f, 3681.117f, 194.7229f)), owning_building_guid = 4)
      LocalObject(762, Door.Constructor(Vector3(4421.222f, 3681.117f, 202.2229f)), owning_building_guid = 4)
      LocalObject(763, Door.Constructor(Vector3(4425.708f, 3665.602f, 219.7229f)), owning_building_guid = 4)
      LocalObject(764, Door.Constructor(Vector3(4426.929f, 3667.872f, 214.7229f)), owning_building_guid = 4)
      LocalObject(765, Door.Constructor(Vector3(4429.588f, 3723.378f, 202.2229f)), owning_building_guid = 4)
      LocalObject(766, Door.Constructor(Vector3(4433.464f, 3677.936f, 194.7229f)), owning_building_guid = 4)
      LocalObject(767, Door.Constructor(Vector3(4434.293f, 3701.245f, 202.2229f)), owning_building_guid = 4)
      LocalObject(768, Door.Constructor(Vector3(4442.179f, 3691.355f, 202.2229f)), owning_building_guid = 4)
      LocalObject(769, Door.Constructor(Vector3(4443.007f, 3714.664f, 209.7229f)), owning_building_guid = 4)
      LocalObject(770, Door.Constructor(Vector3(4444.184f, 3709.131f, 202.2229f)), owning_building_guid = 4)
      LocalObject(771, Door.Constructor(Vector3(4445.359f, 3703.597f, 194.7229f)), owning_building_guid = 4)
      LocalObject(772, Door.Constructor(Vector3(4448.541f, 3715.84f, 209.7229f)), owning_building_guid = 4)
      LocalObject(773, Door.Constructor(Vector3(4453.071f, 3708.128f, 214.7229f)), owning_building_guid = 4)
      LocalObject(774, Door.Constructor(Vector3(4454.595f, 3673.752f, 209.7229f)), owning_building_guid = 4)
      LocalObject(775, Door.Constructor(Vector3(4454.648f, 3710.167f, 219.7229f)), owning_building_guid = 4)
      LocalObject(776, Door.Constructor(Vector3(4458.952f, 3680.462f, 209.7229f)), owning_building_guid = 4)
      LocalObject(943, Door.Constructor(Vector3(4464.481f, 3672.133f, 210.4819f)), owning_building_guid = 4)
      LocalObject(2780, Door.Constructor(Vector3(4418.199f, 3689.916f, 202.556f)), owning_building_guid = 4)
      LocalObject(2781, Door.Constructor(Vector3(4422.171f, 3696.032f, 202.556f)), owning_building_guid = 4)
      LocalObject(2782, Door.Constructor(Vector3(4426.141f, 3702.145f, 202.556f)), owning_building_guid = 4)
      LocalObject(
        995,
        IFFLock.Constructor(Vector3(4468.436f, 3673.189f, 209.6819f), Vector3(0, 0, 123)),
        owning_building_guid = 4,
        door_guid = 943
      )
      LocalObject(
        1120,
        IFFLock.Constructor(Vector3(4399.101f, 3711.665f, 194.5379f), Vector3(0, 0, 123)),
        owning_building_guid = 4,
        door_guid = 756
      )
      LocalObject(
        1121,
        IFFLock.Constructor(Vector3(4417.932f, 3663.463f, 219.6629f), Vector3(0, 0, 303)),
        owning_building_guid = 4,
        door_guid = 464
      )
      LocalObject(
        1122,
        IFFLock.Constructor(Vector3(4418.568f, 3798.292f, 209.662f), Vector3(0, 0, 33)),
        owning_building_guid = 4,
        door_guid = 465
      )
      LocalObject(
        1123,
        IFFLock.Constructor(Vector3(4421.942f, 3656.444f, 214.664f), Vector3(0, 0, 213)),
        owning_building_guid = 4,
        door_guid = 466
      )
      LocalObject(
        1124,
        IFFLock.Constructor(Vector3(4422.099f, 3679.582f, 202.0379f), Vector3(0, 0, 213)),
        owning_building_guid = 4,
        door_guid = 762
      )
      LocalObject(
        1125,
        IFFLock.Constructor(Vector3(4429.122f, 3661.073f, 219.6629f), Vector3(0, 0, 123)),
        owning_building_guid = 4,
        door_guid = 468
      )
      LocalObject(
        1126,
        IFFLock.Constructor(Vector3(4431.82f, 3677.13f, 194.5379f), Vector3(0, 0, 303)),
        owning_building_guid = 4,
        door_guid = 766
      )
      LocalObject(
        1127,
        IFFLock.Constructor(Vector3(4433.487f, 3702.89f, 202.0379f), Vector3(0, 0, 33)),
        owning_building_guid = 4,
        door_guid = 767
      )
      LocalObject(
        1128,
        IFFLock.Constructor(Vector3(4451.23f, 3714.737f, 219.6629f), Vector3(0, 0, 303)),
        owning_building_guid = 4,
        door_guid = 469
      )
      LocalObject(
        1129,
        IFFLock.Constructor(Vector3(4458.067f, 3719.575f, 214.664f), Vector3(0, 0, 33)),
        owning_building_guid = 4,
        door_guid = 471
      )
      LocalObject(
        1130,
        IFFLock.Constructor(Vector3(4462.421f, 3712.35f, 219.6629f), Vector3(0, 0, 123)),
        owning_building_guid = 4,
        door_guid = 472
      )
      LocalObject(1429, Locker.Constructor(Vector3(4425.376f, 3680.972f, 200.963f)), owning_building_guid = 4)
      LocalObject(1430, Locker.Constructor(Vector3(4426.352f, 3680.338f, 200.963f)), owning_building_guid = 4)
      LocalObject(1431, Locker.Constructor(Vector3(4427.314f, 3679.713f, 200.963f)), owning_building_guid = 4)
      LocalObject(1432, Locker.Constructor(Vector3(4428.278f, 3679.088f, 200.963f)), owning_building_guid = 4)
      LocalObject(1433, Locker.Constructor(Vector3(4445.198f, 3691.976f, 193.202f)), owning_building_guid = 4)
      LocalObject(1434, Locker.Constructor(Vector3(4446.308f, 3691.254f, 193.202f)), owning_building_guid = 4)
      LocalObject(1435, Locker.Constructor(Vector3(4447.429f, 3690.527f, 193.202f)), owning_building_guid = 4)
      LocalObject(1436, Locker.Constructor(Vector3(4448.55f, 3689.799f, 193.202f)), owning_building_guid = 4)
      LocalObject(1437, Locker.Constructor(Vector3(4452.357f, 3687.326f, 193.202f)), owning_building_guid = 4)
      LocalObject(1438, Locker.Constructor(Vector3(4453.468f, 3686.605f, 193.202f)), owning_building_guid = 4)
      LocalObject(1439, Locker.Constructor(Vector3(4454.588f, 3685.877f, 193.202f)), owning_building_guid = 4)
      LocalObject(1440, Locker.Constructor(Vector3(4455.709f, 3685.149f, 193.202f)), owning_building_guid = 4)
      LocalObject(
        1908,
        Terminal.Constructor(Vector3(4421.155f, 3700.233f, 209.531f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1909,
        Terminal.Constructor(Vector3(4427.756f, 3669.15f, 209.531f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1910,
        Terminal.Constructor(Vector3(4430.837f, 3683.706f, 202.2919f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1911,
        Terminal.Constructor(Vector3(4432.869f, 3686.835f, 202.2919f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1912,
        Terminal.Constructor(Vector3(4434.933f, 3690.013f, 202.2919f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1913,
        Terminal.Constructor(Vector3(4452.24f, 3706.852f, 209.531f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2682,
        Terminal.Constructor(Vector3(4417.093f, 3687.665f, 202.836f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2683,
        Terminal.Constructor(Vector3(4421.061f, 3693.783f, 202.836f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2684,
        Terminal.Constructor(Vector3(4421.256f, 3747.162f, 202.259f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2685,
        Terminal.Constructor(Vector3(4425.033f, 3699.893f, 202.836f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2686,
        Terminal.Constructor(Vector3(4428.313f, 3713.963f, 194.759f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2687,
        Terminal.Constructor(Vector3(4432.933f, 3687.224f, 214.7299f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2914,
        Terminal.Constructor(Vector3(4444.725f, 3783.27f, 210.104f), vehicle_terminal_combined),
        owning_building_guid = 4
      )
      LocalObject(
        1808,
        VehicleSpawnPad.Constructor(Vector3(4437.373f, 3771.783f, 205.947f), mb_pad_creation, Vector3(0, 0, 213)),
        owning_building_guid = 4,
        terminal_guid = 2914
      )
      LocalObject(2523, ResourceSilo.Constructor(Vector3(4437.588f, 3587.625f, 215.237f)), owning_building_guid = 4)
      LocalObject(
        2575,
        SpawnTube.Constructor(Vector3(4417.258f, 3689.275f, 200.702f), Vector3(0, 0, 33)),
        owning_building_guid = 4
      )
      LocalObject(
        2576,
        SpawnTube.Constructor(Vector3(4421.229f, 3695.389f, 200.702f), Vector3(0, 0, 33)),
        owning_building_guid = 4
      )
      LocalObject(
        2577,
        SpawnTube.Constructor(Vector3(4425.199f, 3701.502f, 200.702f), Vector3(0, 0, 33)),
        owning_building_guid = 4
      )
      LocalObject(
        1827,
        ProximityTerminal.Constructor(Vector3(4421.506f, 3700.003f, 213.202f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1828,
        ProximityTerminal.Constructor(Vector3(4450.143f, 3688.115f, 193.202f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2132,
        ProximityTerminal.Constructor(Vector3(4402.756f, 3735.761f, 216.507f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        2133,
        Terminal.Constructor(Vector3(4402.756f, 3735.761f, 216.507f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2135,
        ProximityTerminal.Constructor(Vector3(4406.931f, 3621.843f, 216.53f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        2136,
        Terminal.Constructor(Vector3(4406.931f, 3621.843f, 216.53f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2138,
        ProximityTerminal.Constructor(Vector3(4470.607f, 3644.475f, 216.507f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        2139,
        Terminal.Constructor(Vector3(4470.607f, 3644.475f, 216.507f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2141,
        ProximityTerminal.Constructor(Vector3(4493.575f, 3721.664f, 218.668f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        2142,
        Terminal.Constructor(Vector3(4493.575f, 3721.664f, 218.668f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2453,
        ProximityTerminal.Constructor(Vector3(4381.323f, 3726.348f, 207.4019f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        2454,
        Terminal.Constructor(Vector3(4381.323f, 3726.348f, 207.4019f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2457,
        ProximityTerminal.Constructor(Vector3(4492.484f, 3654.385f, 207.4019f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        2458,
        Terminal.Constructor(Vector3(4492.484f, 3654.385f, 207.4019f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1729,
        FacilityTurret.Constructor(Vector3(4319.666f, 3650.68f, 216.71f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1729, 5006)
      LocalObject(
        1731,
        FacilityTurret.Constructor(Vector3(4368.426f, 3616.358f, 216.71f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1731, 5007)
      LocalObject(
        1733,
        FacilityTurret.Constructor(Vector3(4429.482f, 3819.836f, 216.71f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1733, 5008)
      LocalObject(
        1734,
        FacilityTurret.Constructor(Vector3(4448.337f, 3567.108f, 216.71f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1734, 5009)
      LocalObject(
        1737,
        FacilityTurret.Constructor(Vector3(4525.526f, 3681.879f, 216.71f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1737, 5010)
      LocalObject(
        1739,
        FacilityTurret.Constructor(Vector3(4558.186f, 3736.243f, 216.71f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1739, 5011)
      LocalObject(2258, Painbox.Constructor(Vector3(4386.058f, 3714.824f, 198.073f), painbox), owning_building_guid = 4)
      LocalObject(
        2272,
        Painbox.Constructor(Vector3(4428.834f, 3693.566f, 205.6504f), painbox_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        2286,
        Painbox.Constructor(Vector3(4398.994f, 3708.958f, 194.6609f), painbox_door_radius),
        owning_building_guid = 4
      )
      LocalObject(
        2312,
        Painbox.Constructor(Vector3(4420.643f, 3680.225f, 201.9019f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        2313,
        Painbox.Constructor(Vector3(4435.054f, 3702.282f, 201.9664f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        2314,
        Painbox.Constructor(Vector3(4443.386f, 3690.52f, 203.8806f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(303, Generator.Constructor(Vector3(4384.506f, 3719.239f, 191.908f)), owning_building_guid = 4)
      LocalObject(
        289,
        Terminal.Constructor(Vector3(4391.402f, 3714.817f, 193.202f), gen_control),
        owning_building_guid = 4
      )
    }

    Building10()

    def Building10(): Unit = { // Name: Matagi Type: amp_station GUID: 7, MapID: 10
      LocalBuilding(
        "Matagi",
        7,
        10,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5268f, 5004f, 239.1049f),
            Vector3(0f, 0f, 21f),
            amp_station
          )
        )
      )
      LocalObject(
        222,
        CaptureTerminal.Constructor(Vector3(5264.884f, 5002.807f, 250.6129f), capture_terminal),
        owning_building_guid = 7
      )
      LocalObject(167, Door.Constructor(Vector3(5265.761f, 5010.43f, 252.0069f)), owning_building_guid = 7)
      LocalObject(168, Door.Constructor(Vector3(5270.636f, 4997.723f, 252.0069f)), owning_building_guid = 7)
      LocalObject(534, Door.Constructor(Vector3(5168.378f, 5051.451f, 240.8259f)), owning_building_guid = 7)
      LocalObject(535, Door.Constructor(Vector3(5181.638f, 5039.296f, 248.8189f)), owning_building_guid = 7)
      LocalObject(536, Door.Constructor(Vector3(5188.158f, 5022.312f, 240.8559f)), owning_building_guid = 7)
      LocalObject(539, Door.Constructor(Vector3(5213.022f, 5081.641f, 240.8559f)), owning_building_guid = 7)
      LocalObject(540, Door.Constructor(Vector3(5217.908f, 4944.81f, 248.8189f)), owning_building_guid = 7)
      LocalObject(542, Door.Constructor(Vector3(5224.427f, 4927.826f, 240.8559f)), owning_building_guid = 7)
      LocalObject(543, Door.Constructor(Vector3(5230.006f, 5088.16f, 248.8199f)), owning_building_guid = 7)
      LocalObject(547, Door.Constructor(Vector3(5253.019f, 5031f, 250.8319f)), owning_building_guid = 7)
      LocalObject(548, Door.Constructor(Vector3(5255.277f, 5037.143f, 245.8259f)), owning_building_guid = 7)
      LocalObject(549, Door.Constructor(Vector3(5261.478f, 5034.248f, 250.8319f)), owning_building_guid = 7)
      LocalObject(551, Door.Constructor(Vector3(5274.914f, 4973.903f, 250.8319f)), owning_building_guid = 7)
      LocalObject(554, Door.Constructor(Vector3(5280.722f, 4970.858f, 245.8259f)), owning_building_guid = 7)
      LocalObject(557, Door.Constructor(Vector3(5283.373f, 4977.151f, 250.8319f)), owning_building_guid = 7)
      LocalObject(558, Door.Constructor(Vector3(5314.521f, 4931.648f, 248.8199f)), owning_building_guid = 7)
      LocalObject(559, Door.Constructor(Vector3(5331.505f, 4938.167f, 240.8559f)), owning_building_guid = 7)
      LocalObject(561, Door.Constructor(Vector3(5340.152f, 4984.03f, 240.8559f)), owning_building_guid = 7)
      LocalObject(564, Door.Constructor(Vector3(5346.671f, 4967.046f, 248.8199f)), owning_building_guid = 7)
      LocalObject(848, Door.Constructor(Vector3(5190.748f, 5004.338f, 233.3259f)), owning_building_guid = 7)
      LocalObject(853, Door.Constructor(Vector3(5224.622f, 4983.064f, 225.8259f)), owning_building_guid = 7)
      LocalObject(854, Door.Constructor(Vector3(5226.922f, 4988.232f, 225.8259f)), owning_building_guid = 7)
      LocalObject(855, Door.Constructor(Vector3(5233.259f, 5016.372f, 233.3259f)), owning_building_guid = 7)
      LocalObject(859, Door.Constructor(Vector3(5242.426f, 4981.329f, 225.8259f)), owning_building_guid = 7)
      LocalObject(860, Door.Constructor(Vector3(5248.196f, 5022.105f, 240.8259f)), owning_building_guid = 7)
      LocalObject(863, Door.Constructor(Vector3(5250.497f, 5027.273f, 240.8259f)), owning_building_guid = 7)
      LocalObject(866, Door.Constructor(Vector3(5253.364f, 5019.805f, 233.3259f)), owning_building_guid = 7)
      LocalObject(867, Door.Constructor(Vector3(5253.93f, 5007.168f, 233.3259f)), owning_building_guid = 7)
      LocalObject(868, Door.Constructor(Vector3(5258.532f, 5017.504f, 225.8259f)), owning_building_guid = 7)
      LocalObject(869, Door.Constructor(Vector3(5258.676f, 5028.88f, 250.8259f)), owning_building_guid = 7)
      LocalObject(870, Door.Constructor(Vector3(5259.399f, 5026.406f, 245.8259f)), owning_building_guid = 7)
      LocalObject(871, Door.Constructor(Vector3(5262.531f, 4984.762f, 225.8259f)), owning_building_guid = 7)
      LocalObject(872, Door.Constructor(Vector3(5262.531f, 4984.762f, 233.3259f)), owning_building_guid = 7)
      LocalObject(874, Door.Constructor(Vector3(5265.398f, 4977.293f, 240.8259f)), owning_building_guid = 7)
      LocalObject(876, Door.Constructor(Vector3(5266.566f, 5007.734f, 233.3259f)), owning_building_guid = 7)
      LocalObject(877, Door.Constructor(Vector3(5270.565f, 4974.993f, 240.8259f)), owning_building_guid = 7)
      LocalObject(878, Door.Constructor(Vector3(5272.3f, 4992.797f, 225.8259f)), owning_building_guid = 7)
      LocalObject(880, Door.Constructor(Vector3(5276.601f, 4981.594f, 245.8259f)), owning_building_guid = 7)
      LocalObject(882, Door.Constructor(Vector3(5277.719f, 4979.271f, 250.8259f)), owning_building_guid = 7)
      LocalObject(885, Door.Constructor(Vector3(5285.238f, 5014.902f, 240.8259f)), owning_building_guid = 7)
      LocalObject(888, Door.Constructor(Vector3(5288.105f, 5007.433f, 240.8259f)), owning_building_guid = 7)
      LocalObject(948, Door.Constructor(Vector3(5295.227f, 5014.479f, 241.5849f)), owning_building_guid = 7)
      LocalObject(2800, Door.Constructor(Vector3(5248.41f, 5001.102f, 233.6589f)), owning_building_guid = 7)
      LocalObject(2801, Door.Constructor(Vector3(5251.022f, 4994.297f, 233.6589f)), owning_building_guid = 7)
      LocalObject(2802, Door.Constructor(Vector3(5253.636f, 4987.489f, 233.6589f)), owning_building_guid = 7)
      LocalObject(
        1000,
        IFFLock.Constructor(Vector3(5296.696f, 5018.3f, 240.7849f), Vector3(0, 0, 69)),
        owning_building_guid = 7,
        door_guid = 948
      )
      LocalObject(
        1179,
        IFFLock.Constructor(Vector3(5166.174f, 5051.489f, 240.7649f), Vector3(0, 0, 339)),
        owning_building_guid = 7,
        door_guid = 534
      )
      LocalObject(
        1182,
        IFFLock.Constructor(Vector3(5224.814f, 4984.822f, 225.6409f), Vector3(0, 0, 69)),
        owning_building_guid = 7,
        door_guid = 853
      )
      LocalObject(
        1184,
        IFFLock.Constructor(Vector3(5252.125f, 5007.483f, 233.1409f), Vector3(0, 0, 339)),
        owning_building_guid = 7,
        door_guid = 867
      )
      LocalObject(
        1185,
        IFFLock.Constructor(Vector3(5252.97f, 5028.801f, 250.7659f), Vector3(0, 0, 249)),
        owning_building_guid = 7,
        door_guid = 547
      )
      LocalObject(
        1186,
        IFFLock.Constructor(Vector3(5253.076f, 5037.176f, 245.7669f), Vector3(0, 0, 339)),
        owning_building_guid = 7,
        door_guid = 548
      )
      LocalObject(
        1187,
        IFFLock.Constructor(Vector3(5261.479f, 5036.452f, 250.7659f), Vector3(0, 0, 69)),
        owning_building_guid = 7,
        door_guid = 549
      )
      LocalObject(
        1189,
        IFFLock.Constructor(Vector3(5264.289f, 4984.569f, 233.1409f), Vector3(0, 0, 159)),
        owning_building_guid = 7,
        door_guid = 872
      )
      LocalObject(
        1190,
        IFFLock.Constructor(Vector3(5271.986f, 4990.993f, 225.6409f), Vector3(0, 0, 249)),
        owning_building_guid = 7,
        door_guid = 878
      )
      LocalObject(
        1192,
        IFFLock.Constructor(Vector3(5274.879f, 4971.725f, 250.7659f), Vector3(0, 0, 249)),
        owning_building_guid = 7,
        door_guid = 551
      )
      LocalObject(
        1195,
        IFFLock.Constructor(Vector3(5282.915f, 4970.843f, 245.7669f), Vector3(0, 0, 159)),
        owning_building_guid = 7,
        door_guid = 554
      )
      LocalObject(
        1197,
        IFFLock.Constructor(Vector3(5283.39f, 4979.372f, 250.7659f), Vector3(0, 0, 69)),
        owning_building_guid = 7,
        door_guid = 557
      )
      LocalObject(1523, Locker.Constructor(Vector3(5265.09f, 4988.038f, 232.0659f)), owning_building_guid = 7)
      LocalObject(1524, Locker.Constructor(Vector3(5266.177f, 4988.455f, 232.0659f)), owning_building_guid = 7)
      LocalObject(1525, Locker.Constructor(Vector3(5267.248f, 4988.866f, 232.0659f)), owning_building_guid = 7)
      LocalObject(1526, Locker.Constructor(Vector3(5267.839f, 5010.542f, 224.3049f)), owning_building_guid = 7)
      LocalObject(1527, Locker.Constructor(Vector3(5268.32f, 4989.278f, 232.0659f)), owning_building_guid = 7)
      LocalObject(1528, Locker.Constructor(Vector3(5269.075f, 5011.016f, 224.3049f)), owning_building_guid = 7)
      LocalObject(1529, Locker.Constructor(Vector3(5270.322f, 5011.495f, 224.3049f)), owning_building_guid = 7)
      LocalObject(1530, Locker.Constructor(Vector3(5271.57f, 5011.974f, 224.3049f)), owning_building_guid = 7)
      LocalObject(1531, Locker.Constructor(Vector3(5275.809f, 5013.601f, 224.3049f)), owning_building_guid = 7)
      LocalObject(1532, Locker.Constructor(Vector3(5277.045f, 5014.076f, 224.3049f)), owning_building_guid = 7)
      LocalObject(1534, Locker.Constructor(Vector3(5278.292f, 5014.554f, 224.3049f)), owning_building_guid = 7)
      LocalObject(1537, Locker.Constructor(Vector3(5279.541f, 5015.034f, 224.3049f)), owning_building_guid = 7)
      LocalObject(
        1944,
        Terminal.Constructor(Vector3(5247.027f, 4995.945f, 240.6339f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        1945,
        Terminal.Constructor(Vector3(5259.943f, 5024.983f, 240.6339f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        1947,
        Terminal.Constructor(Vector3(5263.393f, 5001.083f, 233.3949f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        1948,
        Terminal.Constructor(Vector3(5264.751f, 4997.546f, 233.3949f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        1949,
        Terminal.Constructor(Vector3(5266.088f, 4994.063f, 233.3949f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        1951,
        Terminal.Constructor(Vector3(5276.053f, 4983.015f, 240.6339f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2707,
        Terminal.Constructor(Vector3(5209.119f, 5023.61f, 233.3619f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2709,
        Terminal.Constructor(Vector3(5240.126f, 5009.806f, 225.8619f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2711,
        Terminal.Constructor(Vector3(5249.581f, 4998.882f, 233.9389f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2712,
        Terminal.Constructor(Vector3(5252.189f, 4992.077f, 233.9389f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2713,
        Terminal.Constructor(Vector3(5254.807f, 4985.271f, 233.9389f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2715,
        Terminal.Constructor(Vector3(5264.474f, 4997.827f, 245.8329f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2918,
        Terminal.Constructor(Vector3(5193.703f, 5063.82f, 241.2069f), vehicle_terminal_combined),
        owning_building_guid = 7
      )
      LocalObject(
        1812,
        VehicleSpawnPad.Constructor(Vector3(5198.674f, 5051.121f, 237.0499f), mb_pad_creation, Vector3(0, 0, 159)),
        owning_building_guid = 7,
        terminal_guid = 2918
      )
      LocalObject(2528, ResourceSilo.Constructor(Vector3(5347.787f, 4943.05f, 246.3399f)), owning_building_guid = 7)
      LocalObject(
        2595,
        SpawnTube.Constructor(Vector3(5248.377f, 4999.961f, 231.8049f), Vector3(0, 0, 339)),
        owning_building_guid = 7
      )
      LocalObject(
        2596,
        SpawnTube.Constructor(Vector3(5250.989f, 4993.158f, 231.8049f), Vector3(0, 0, 339)),
        owning_building_guid = 7
      )
      LocalObject(
        2597,
        SpawnTube.Constructor(Vector3(5253.602f, 4986.351f, 231.8049f), Vector3(0, 0, 339)),
        owning_building_guid = 7
      )
      LocalObject(
        1834,
        ProximityTerminal.Constructor(Vector3(5247.418f, 4996.093f, 244.3049f), medical_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        1835,
        ProximityTerminal.Constructor(Vector3(5273.869f, 5012.273f, 224.3049f), medical_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2162,
        ProximityTerminal.Constructor(Vector3(5207.469f, 5001.942f, 247.6099f), pad_landing_frame),
        owning_building_guid = 7
      )
      LocalObject(
        2163,
        Terminal.Constructor(Vector3(5207.469f, 5001.942f, 247.6099f), air_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2171,
        ProximityTerminal.Constructor(Vector3(5272.256f, 5067.13f, 249.7709f), pad_landing_frame),
        owning_building_guid = 7
      )
      LocalObject(
        2172,
        Terminal.Constructor(Vector3(5272.256f, 5067.13f, 249.7709f), air_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2174,
        ProximityTerminal.Constructor(Vector3(5302.085f, 4938.36f, 247.6329f), pad_landing_frame),
        owning_building_guid = 7
      )
      LocalObject(
        2175,
        Terminal.Constructor(Vector3(5302.085f, 4938.36f, 247.6329f), air_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2180,
        ProximityTerminal.Constructor(Vector3(5321.203f, 5003.179f, 247.6099f), pad_landing_frame),
        owning_building_guid = 7
      )
      LocalObject(
        2181,
        Terminal.Constructor(Vector3(5321.203f, 5003.179f, 247.6099f), air_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2485,
        ProximityTerminal.Constructor(Vector3(5202.486f, 4979.07f, 238.5049f), repair_silo),
        owning_building_guid = 7
      )
      LocalObject(
        2486,
        Terminal.Constructor(Vector3(5202.486f, 4979.07f, 238.5049f), ground_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2497,
        ProximityTerminal.Constructor(Vector3(5326.044f, 5026.702f, 238.5049f), repair_silo),
        owning_building_guid = 7
      )
      LocalObject(
        2498,
        Terminal.Constructor(Vector3(5326.044f, 5026.702f, 238.5049f), ground_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        1760,
        FacilityTurret.Constructor(Vector3(5155.16f, 5072.982f, 247.8129f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(1760, 5012)
      LocalObject(
        1763,
        FacilityTurret.Constructor(Vector3(5227.462f, 4884.711f, 247.8129f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(1763, 5013)
      LocalObject(
        1765,
        FacilityTurret.Constructor(Vector3(5283.89f, 4903.985f, 247.8129f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(1765, 5014)
      LocalObject(
        1768,
        FacilityTurret.Constructor(Vector3(5298.438f, 5127.971f, 247.8129f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(1768, 5015)
      LocalObject(
        1770,
        FacilityTurret.Constructor(Vector3(5323.223f, 5069.594f, 247.8129f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(1770, 5016)
      LocalObject(
        1771,
        FacilityTurret.Constructor(Vector3(5370.705f, 4939.687f, 247.8129f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(1771, 5017)
      LocalObject(
        2262,
        Painbox.Constructor(Vector3(5214.593f, 4976.126f, 229.1759f), painbox),
        owning_building_guid = 7
      )
      LocalObject(
        2276,
        Painbox.Constructor(Vector3(5256.935f, 4998.238f, 236.7533f), painbox_continuous),
        owning_building_guid = 7
      )
      LocalObject(
        2290,
        Painbox.Constructor(Vector3(5226.941f, 4983.145f, 225.7639f), painbox_door_radius),
        owning_building_guid = 7
      )
      LocalObject(
        2324,
        Painbox.Constructor(Vector3(5253.539f, 5008.394f, 233.0694f), painbox_door_radius_continuous),
        owning_building_guid = 7
      )
      LocalObject(
        2326,
        Painbox.Constructor(Vector3(5262.913f, 4983.77f, 233.0049f), painbox_door_radius_continuous),
        owning_building_guid = 7
      )
      LocalObject(
        2327,
        Painbox.Constructor(Vector3(5267.952f, 5008.22f, 234.9835f), painbox_door_radius_continuous),
        owning_building_guid = 7
      )
      LocalObject(307, Generator.Constructor(Vector3(5210.109f, 4977.466f, 223.0109f)), owning_building_guid = 7)
      LocalObject(
        293,
        Terminal.Constructor(Vector3(5217.74f, 4980.446f, 224.3049f), gen_control),
        owning_building_guid = 7
      )
    }

    Building42()

    def Building42(): Unit = { // Name: bunkerg6 Type: bunker_gauntlet GUID: 10, MapID: 42
      LocalBuilding(
        "bunkerg6",
        10,
        42,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(1068f, 4234f, 59.97345f),
            Vector3(0f, 0f, 61f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(311, Door.Constructor(Vector3(1057.601f, 4211.297f, 61.49445f)), owning_building_guid = 10)
      LocalObject(312, Door.Constructor(Vector3(1081.746f, 4254.876f, 61.49445f)), owning_building_guid = 10)
    }

    Building43()

    def Building43(): Unit = { // Name: bunkerg7 Type: bunker_gauntlet GUID: 11, MapID: 43
      LocalBuilding(
        "bunkerg7",
        11,
        43,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(3642f, 2072f, 59.77203f),
            Vector3(0f, 0f, 137f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(388, Door.Constructor(Vector3(3625.069f, 2090.388f, 61.29303f)), owning_building_guid = 11)
      LocalObject(392, Door.Constructor(Vector3(3661.513f, 2056.418f, 61.29303f)), owning_building_guid = 11)
    }

    Building41()

    def Building41(): Unit = { // Name: bunkerg5 Type: bunker_gauntlet GUID: 12, MapID: 41
      LocalBuilding(
        "bunkerg5",
        12,
        41,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4532f, 6648f, 48.86586f),
            Vector3(0f, 0f, 175f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(476, Door.Constructor(Vector3(4507.337f, 6652.066f, 50.38686f)), owning_building_guid = 12)
      LocalObject(482, Door.Constructor(Vector3(4556.97f, 6647.735f, 50.38686f)), owning_building_guid = 12)
    }

    Building38()

    def Building38(): Unit = { // Name: bungerg2 Type: bunker_gauntlet GUID: 13, MapID: 38
      LocalBuilding(
        "bungerg2",
        13,
        38,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(5936f, 2184f, 90.95889f),
            Vector3(0f, 0f, 270f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(591, Door.Constructor(Vector3(5934.099f, 2159.077f, 92.4799f)), owning_building_guid = 13)
      LocalObject(592, Door.Constructor(Vector3(5934.088f, 2208.898f, 92.4799f)), owning_building_guid = 13)
    }

    Building39()

    def Building39(): Unit = { // Name: bunkerg3 Type: bunker_gauntlet GUID: 14, MapID: 39
      LocalBuilding(
        "bunkerg3",
        14,
        39,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(6376f, 5224f, 55.30717f),
            Vector3(0f, 0f, 19f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(597, Door.Constructor(Vector3(6353.081f, 5214.086f, 56.82817f)), owning_building_guid = 14)
      LocalObject(600, Door.Constructor(Vector3(6400.184f, 5230.317f, 56.82817f)), owning_building_guid = 14)
    }

    Building34()

    def Building34(): Unit = { // Name: bunker6 Type: bunker_lg GUID: 15, MapID: 34
      LocalBuilding(
        "bunker6",
        15,
        34,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(1192f, 4018f, 59.95798f), Vector3(0f, 0f, 56f), bunker_lg)
        )
      )
      LocalObject(324, Door.Constructor(Vector3(1191.337f, 4021.59f, 61.47898f)), owning_building_guid = 15)
    }

    Building30()

    def Building30(): Unit = { // Name: bunker1 Type: bunker_lg GUID: 16, MapID: 30
      LocalBuilding(
        "bunker1",
        16,
        30,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3838f, 2626f, 151.0666f), Vector3(0f, 0f, 69f), bunker_lg)
        )
      )
      LocalObject(432, Door.Constructor(Vector3(3836.547f, 2629.349f, 152.5876f)), owning_building_guid = 16)
    }

    Building35()

    def Building35(): Unit = { // Name: bunker7 Type: bunker_lg GUID: 17, MapID: 35
      LocalBuilding(
        "bunker7",
        17,
        35,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4780f, 6576f, 39.89838f), Vector3(0f, 0f, 89f), bunker_lg)
        )
      )
      LocalObject(510, Door.Constructor(Vector3(4777.489f, 6578.65f, 41.41938f)), owning_building_guid = 17)
    }

    Building40()

    def Building40(): Unit = { // Name: bunkerg4 Type: bunker_lg GUID: 18, MapID: 40
      LocalBuilding(
        "bunkerg4",
        18,
        40,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(6486f, 5064f, 55.30717f), Vector3(0f, 0f, 70f), bunker_lg)
        )
      )
      LocalObject(610, Door.Constructor(Vector3(6484.488f, 5067.323f, 56.82817f)), owning_building_guid = 18)
    }

    Building36()

    def Building36(): Unit = { // Name: bunker8 Type: bunker_sm GUID: 19, MapID: 36
      LocalBuilding(
        "bunker8",
        19,
        36,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3670f, 5682f, 236.1003f), Vector3(0f, 0f, 303f), bunker_sm)
        )
      )
      LocalObject(395, Door.Constructor(Vector3(3670.621f, 5680.943f, 237.6213f)), owning_building_guid = 19)
    }

    Building33()

    def Building33(): Unit = { // Name: bunker5 Type: bunker_sm GUID: 20, MapID: 33
      LocalBuilding(
        "bunker5",
        20,
        33,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4142f, 4096f, 204.7803f), Vector3(0f, 0f, 132f), bunker_sm)
        )
      )
      LocalObject(448, Door.Constructor(Vector3(4141.221f, 4096.947f, 206.3013f)), owning_building_guid = 20)
    }

    Building32()

    def Building32(): Unit = { // Name: bunker4 Type: bunker_sm GUID: 21, MapID: 32
      LocalBuilding(
        "bunker4",
        21,
        32,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5238f, 3866f, 346.1104f), Vector3(0f, 0f, 47f), bunker_sm)
        )
      )
      LocalObject(545, Door.Constructor(Vector3(5238.875f, 3866.858f, 347.6314f)), owning_building_guid = 21)
    }

    Building31()

    def Building31(): Unit = { // Name: bunker2 Type: bunker_sm GUID: 22, MapID: 31
      LocalBuilding(
        "bunker2",
        22,
        31,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5726f, 2268f, 90.95889f), Vector3(0f, 0f, 24f), bunker_sm)
        )
      )
      LocalObject(571, Door.Constructor(Vector3(5727.142f, 2268.448f, 92.4799f)), owning_building_guid = 22)
    }

    Building17()

    def Building17(): Unit = { // Name: Wakea Type: comm_station GUID: 23, MapID: 17
      LocalBuilding(
        "Wakea",
        23,
        17,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(1738f, 5314f, 62.99478f),
            Vector3(0f, 0f, 360f),
            comm_station
          )
        )
      )
      LocalObject(
        212,
        CaptureTerminal.Constructor(Vector3(1782.089f, 5390.734f, 45.69478f), capture_terminal),
        owning_building_guid = 23
      )
      LocalObject(337, Door.Constructor(Vector3(1678.196f, 5270.5f, 64.74578f)), owning_building_guid = 23)
      LocalObject(338, Door.Constructor(Vector3(1678.196f, 5288.693f, 72.70978f)), owning_building_guid = 23)
      LocalObject(339, Door.Constructor(Vector3(1695.307f, 5246.197f, 72.70978f)), owning_building_guid = 23)
      LocalObject(340, Door.Constructor(Vector3(1713.5f, 5246.197f, 64.74578f)), owning_building_guid = 23)
      LocalObject(341, Door.Constructor(Vector3(1718f, 5305.231f, 69.71578f)), owning_building_guid = 23)
      LocalObject(342, Door.Constructor(Vector3(1718f, 5314.295f, 69.71578f)), owning_building_guid = 23)
      LocalObject(343, Door.Constructor(Vector3(1733.625f, 5306.59f, 77.15479f)), owning_building_guid = 23)
      LocalObject(344, Door.Constructor(Vector3(1738.5f, 5381.977f, 64.74578f)), owning_building_guid = 23)
      LocalObject(345, Door.Constructor(Vector3(1746f, 5334f, 69.71578f)), owning_building_guid = 23)
      LocalObject(346, Door.Constructor(Vector3(1756.692f, 5381.977f, 72.70878f)), owning_building_guid = 23)
      LocalObject(347, Door.Constructor(Vector3(1770f, 5390f, 64.71578f)), owning_building_guid = 23)
      LocalObject(352, Door.Constructor(Vector3(1798.201f, 5303.307f, 72.70978f)), owning_building_guid = 23)
      LocalObject(353, Door.Constructor(Vector3(1798.201f, 5321.5f, 64.74578f)), owning_building_guid = 23)
      LocalObject(648, Door.Constructor(Vector3(1702f, 5346f, 57.21578f)), owning_building_guid = 23)
      LocalObject(649, Door.Constructor(Vector3(1710f, 5362f, 57.21578f)), owning_building_guid = 23)
      LocalObject(650, Door.Constructor(Vector3(1718f, 5330f, 57.21578f)), owning_building_guid = 23)
      LocalObject(651, Door.Constructor(Vector3(1718f, 5338f, 47.21578f)), owning_building_guid = 23)
      LocalObject(652, Door.Constructor(Vector3(1722f, 5310f, 69.71578f)), owning_building_guid = 23)
      LocalObject(653, Door.Constructor(Vector3(1722f, 5326f, 64.71578f)), owning_building_guid = 23)
      LocalObject(654, Door.Constructor(Vector3(1734f, 5322f, 47.21578f)), owning_building_guid = 23)
      LocalObject(655, Door.Constructor(Vector3(1734f, 5354f, 47.21578f)), owning_building_guid = 23)
      LocalObject(656, Door.Constructor(Vector3(1734f, 5362f, 57.21578f)), owning_building_guid = 23)
      LocalObject(657, Door.Constructor(Vector3(1738f, 5318f, 57.21578f)), owning_building_guid = 23)
      LocalObject(658, Door.Constructor(Vector3(1738f, 5350f, 57.21578f)), owning_building_guid = 23)
      LocalObject(659, Door.Constructor(Vector3(1746f, 5326f, 69.71578f)), owning_building_guid = 23)
      LocalObject(660, Door.Constructor(Vector3(1746f, 5334f, 59.71578f)), owning_building_guid = 23)
      LocalObject(661, Door.Constructor(Vector3(1750f, 5322f, 64.71578f)), owning_building_guid = 23)
      LocalObject(662, Door.Constructor(Vector3(1758f, 5322f, 59.71578f)), owning_building_guid = 23)
      LocalObject(663, Door.Constructor(Vector3(1766f, 5338f, 47.21578f)), owning_building_guid = 23)
      LocalObject(664, Door.Constructor(Vector3(1766f, 5370f, 47.21578f)), owning_building_guid = 23)
      LocalObject(665, Door.Constructor(Vector3(1778f, 5382f, 47.21578f)), owning_building_guid = 23)
      LocalObject(666, Door.Constructor(Vector3(1782f, 5322f, 54.71578f)), owning_building_guid = 23)
      LocalObject(667, Door.Constructor(Vector3(1782f, 5346f, 54.71578f)), owning_building_guid = 23)
      LocalObject(668, Door.Constructor(Vector3(1786f, 5382f, 47.21578f)), owning_building_guid = 23)
      LocalObject(669, Door.Constructor(Vector3(1794f, 5326f, 47.21578f)), owning_building_guid = 23)
      LocalObject(670, Door.Constructor(Vector3(1794f, 5342f, 54.71578f)), owning_building_guid = 23)
      LocalObject(671, Door.Constructor(Vector3(1794f, 5366f, 47.21578f)), owning_building_guid = 23)
      LocalObject(938, Door.Constructor(Vector3(1755.707f, 5309.932f, 65.48778f)), owning_building_guid = 23)
      LocalObject(2744, Door.Constructor(Vector3(1774.673f, 5327.733f, 55.04878f)), owning_building_guid = 23)
      LocalObject(2745, Door.Constructor(Vector3(1774.673f, 5335.026f, 55.04878f)), owning_building_guid = 23)
      LocalObject(2746, Door.Constructor(Vector3(1774.673f, 5342.315f, 55.04878f)), owning_building_guid = 23)
      LocalObject(
        990,
        IFFLock.Constructor(Vector3(1758.453f, 5313.13f, 64.64678f), Vector3(0, 0, 90)),
        owning_building_guid = 23,
        door_guid = 938
      )
      LocalObject(
        1024,
        IFFLock.Constructor(Vector3(1715.957f, 5315.105f, 69.65578f), Vector3(0, 0, 0)),
        owning_building_guid = 23,
        door_guid = 342
      )
      LocalObject(
        1025,
        IFFLock.Constructor(Vector3(1719.572f, 5337.06f, 47.03078f), Vector3(0, 0, 180)),
        owning_building_guid = 23,
        door_guid = 651
      )
      LocalObject(
        1026,
        IFFLock.Constructor(Vector3(1720.047f, 5304.42f, 69.65578f), Vector3(0, 0, 180)),
        owning_building_guid = 23,
        door_guid = 341
      )
      LocalObject(
        1027,
        IFFLock.Constructor(Vector3(1732.817f, 5304.511f, 77.07578f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 343
      )
      LocalObject(
        1028,
        IFFLock.Constructor(Vector3(1745.187f, 5331.955f, 69.65578f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 345
      )
      LocalObject(
        1031,
        IFFLock.Constructor(Vector3(1770.778f, 5392.033f, 64.64478f), Vector3(0, 0, 90)),
        owning_building_guid = 23,
        door_guid = 347
      )
      LocalObject(
        1032,
        IFFLock.Constructor(Vector3(1777.187f, 5380.428f, 47.03078f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 665
      )
      LocalObject(
        1033,
        IFFLock.Constructor(Vector3(1780.428f, 5346.81f, 54.53078f), Vector3(0, 0, 0)),
        owning_building_guid = 23,
        door_guid = 667
      )
      LocalObject(
        1034,
        IFFLock.Constructor(Vector3(1783.572f, 5321.19f, 54.53078f), Vector3(0, 0, 180)),
        owning_building_guid = 23,
        door_guid = 666
      )
      LocalObject(
        1035,
        IFFLock.Constructor(Vector3(1786.813f, 5383.572f, 47.03078f), Vector3(0, 0, 90)),
        owning_building_guid = 23,
        door_guid = 668
      )
      LocalObject(
        1038,
        IFFLock.Constructor(Vector3(1793.06f, 5324.428f, 47.03078f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 669
      )
      LocalObject(1276, Locker.Constructor(Vector3(1785.563f, 5324.141f, 53.45578f)), owning_building_guid = 23)
      LocalObject(1277, Locker.Constructor(Vector3(1786.727f, 5324.141f, 53.45578f)), owning_building_guid = 23)
      LocalObject(1278, Locker.Constructor(Vector3(1787.874f, 5324.141f, 53.45578f)), owning_building_guid = 23)
      LocalObject(1279, Locker.Constructor(Vector3(1789.023f, 5324.141f, 53.45578f)), owning_building_guid = 23)
      LocalObject(1283, Locker.Constructor(Vector3(1796.194f, 5344.165f, 45.69478f)), owning_building_guid = 23)
      LocalObject(1285, Locker.Constructor(Vector3(1797.518f, 5344.165f, 45.69478f)), owning_building_guid = 23)
      LocalObject(1286, Locker.Constructor(Vector3(1798.854f, 5344.165f, 45.69478f)), owning_building_guid = 23)
      LocalObject(1287, Locker.Constructor(Vector3(1800.191f, 5344.165f, 45.69478f)), owning_building_guid = 23)
      LocalObject(1288, Locker.Constructor(Vector3(1804.731f, 5344.165f, 45.69478f)), owning_building_guid = 23)
      LocalObject(1289, Locker.Constructor(Vector3(1806.055f, 5344.165f, 45.69478f)), owning_building_guid = 23)
      LocalObject(1290, Locker.Constructor(Vector3(1807.391f, 5344.165f, 45.69478f)), owning_building_guid = 23)
      LocalObject(1291, Locker.Constructor(Vector3(1808.728f, 5344.165f, 45.69478f)), owning_building_guid = 23)
      LocalObject(
        1850,
        Terminal.Constructor(Vector3(1728.379f, 5290.907f, 69.55478f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1851,
        Terminal.Constructor(Vector3(1738.075f, 5306.547f, 76.94978f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1852,
        Terminal.Constructor(Vector3(1740.331f, 5304.43f, 76.94978f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1853,
        Terminal.Constructor(Vector3(1740.332f, 5308.825f, 76.94978f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1856,
        Terminal.Constructor(Vector3(1788.654f, 5329.408f, 54.78478f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1857,
        Terminal.Constructor(Vector3(1788.654f, 5333.139f, 54.78478f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1858,
        Terminal.Constructor(Vector3(1788.654f, 5336.928f, 54.78478f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2649,
        Terminal.Constructor(Vector3(1721.943f, 5322.591f, 47.25178f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2650,
        Terminal.Constructor(Vector3(1736.51f, 5289.969f, 69.81178f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2651,
        Terminal.Constructor(Vector3(1737.409f, 5353.942f, 57.25178f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2652,
        Terminal.Constructor(Vector3(1774.971f, 5325.243f, 55.32878f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2653,
        Terminal.Constructor(Vector3(1774.967f, 5332.535f, 55.32878f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2654,
        Terminal.Constructor(Vector3(1774.97f, 5339.823f, 55.32878f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2909,
        Terminal.Constructor(Vector3(1790.458f, 5368.774f, 65.88178f), vehicle_terminal_combined),
        owning_building_guid = 23
      )
      LocalObject(
        1799,
        VehicleSpawnPad.Constructor(Vector3(1776.833f, 5368.62f, 61.72378f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 23,
        terminal_guid = 2909
      )
      LocalObject(2518, ResourceSilo.Constructor(Vector3(1682.23f, 5244.908f, 70.21178f)), owning_building_guid = 23)
      LocalObject(
        2538,
        SpawnTube.Constructor(Vector3(1774.233f, 5326.683f, 53.19478f), Vector3(0, 0, 0)),
        owning_building_guid = 23
      )
      LocalObject(
        2539,
        SpawnTube.Constructor(Vector3(1774.233f, 5333.974f, 53.19478f), Vector3(0, 0, 0)),
        owning_building_guid = 23
      )
      LocalObject(
        2540,
        SpawnTube.Constructor(Vector3(1774.233f, 5341.262f, 53.19478f), Vector3(0, 0, 0)),
        owning_building_guid = 23
      )
      LocalObject(
        1818,
        ProximityTerminal.Constructor(Vector3(1738.864f, 5285.023f, 63.19478f), medical_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1819,
        ProximityTerminal.Constructor(Vector3(1802.444f, 5343.62f, 45.69478f), medical_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2069,
        ProximityTerminal.Constructor(Vector3(1719.154f, 5369.185f, 71.43578f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        2070,
        Terminal.Constructor(Vector3(1719.154f, 5369.185f, 71.43578f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2413,
        ProximityTerminal.Constructor(Vector3(1676.912f, 5337.18f, 62.74478f), repair_silo),
        owning_building_guid = 23
      )
      LocalObject(
        2414,
        Terminal.Constructor(Vector3(1676.912f, 5337.18f, 62.74478f), ground_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2417,
        ProximityTerminal.Constructor(Vector3(1799.641f, 5289.67f, 62.74478f), repair_silo),
        owning_building_guid = 23
      )
      LocalObject(
        2418,
        Terminal.Constructor(Vector3(1799.641f, 5289.67f, 62.74478f), ground_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1686,
        FacilityTurret.Constructor(Vector3(1664.424f, 5352.652f, 71.70278f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1686, 5018)
      LocalObject(
        1687,
        FacilityTurret.Constructor(Vector3(1665.554f, 5233.565f, 71.70278f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1687, 5019)
      LocalObject(
        1688,
        FacilityTurret.Constructor(Vector3(1707.549f, 5395.75f, 71.70278f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1688, 5020)
      LocalObject(
        1689,
        FacilityTurret.Constructor(Vector3(1768.865f, 5232.388f, 71.70278f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1689, 5021)
      LocalObject(
        1692,
        FacilityTurret.Constructor(Vector3(1810.841f, 5394.435f, 71.70278f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1692, 5022)
      LocalObject(
        1693,
        FacilityTurret.Constructor(Vector3(1812.01f, 5275.501f, 71.70278f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1693, 5023)
      LocalObject(
        2253,
        Painbox.Constructor(Vector3(1718.138f, 5350.089f, 50.59678f), painbox),
        owning_building_guid = 23
      )
      LocalObject(
        2267,
        Painbox.Constructor(Vector3(1784.215f, 5340.17f, 57.63958f), painbox_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        2281,
        Painbox.Constructor(Vector3(1718.237f, 5335.073f, 48.45348f), painbox_door_radius),
        owning_building_guid = 23
      )
      LocalObject(
        2297,
        Painbox.Constructor(Vector3(1781.914f, 5320.076f, 55.02078f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        2298,
        Painbox.Constructor(Vector3(1782.81f, 5347.42f, 54.80248f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        2299,
        Painbox.Constructor(Vector3(1795.855f, 5341.827f, 56.02078f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(298, Generator.Constructor(Vector3(1717.975f, 5353.555f, 44.40078f)), owning_building_guid = 23)
      LocalObject(
        284,
        Terminal.Constructor(Vector3(1718.022f, 5345.363f, 45.69478f), gen_control),
        owning_building_guid = 23
      )
    }

    Building9()

    def Building9(): Unit = { // Name: Laka Type: comm_station GUID: 26, MapID: 9
      LocalBuilding(
        "Laka",
        26,
        9,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4632f, 6704f, 48.95818f),
            Vector3(0f, 0f, 66f),
            comm_station
          )
        )
      )
      LocalObject(
        219,
        CaptureTerminal.Constructor(Vector3(4579.833f, 6775.488f, 31.65818f), capture_terminal),
        owning_building_guid = 26
      )
      LocalObject(483, Door.Constructor(Vector3(4570.104f, 6732.105f, 50.70918f)), owning_building_guid = 26)
      LocalObject(486, Door.Constructor(Vector3(4575.586f, 6764.146f, 50.67918f)), owning_building_guid = 26)
      LocalObject(487, Door.Constructor(Vector3(4577.502f, 6748.725f, 58.67218f)), owning_building_guid = 26)
      LocalObject(490, Door.Constructor(Vector3(4616.983f, 6719.443f, 55.67918f)), owning_building_guid = 26)
      LocalObject(493, Door.Constructor(Vector3(4623.596f, 6685.849f, 55.67918f)), owning_building_guid = 26)
      LocalObject(494, Door.Constructor(Vector3(4630.794f, 6639.073f, 58.67318f)), owning_building_guid = 26)
      LocalObject(495, Door.Constructor(Vector3(4631.876f, 6682.163f, 55.67918f)), owning_building_guid = 26)
      LocalObject(496, Door.Constructor(Vector3(4636.99f, 6696.989f, 63.11818f)), owning_building_guid = 26)
      LocalObject(497, Door.Constructor(Vector3(4647.415f, 6631.673f, 50.70918f)), owning_building_guid = 26)
      LocalObject(498, Door.Constructor(Vector3(4649.634f, 6762.047f, 50.70918f)), owning_building_guid = 26)
      LocalObject(499, Door.Constructor(Vector3(4666.254f, 6754.647f, 58.67318f)), owning_building_guid = 26)
      LocalObject(500, Door.Constructor(Vector3(4676.576f, 6637.42f, 58.67318f)), owning_building_guid = 26)
      LocalObject(501, Door.Constructor(Vector3(4683.976f, 6654.04f, 50.70918f)), owning_building_guid = 26)
      LocalObject(796, Door.Constructor(Vector3(4576.761f, 6697.944f, 43.17918f)), owning_building_guid = 26)
      LocalObject(799, Door.Constructor(Vector3(4586.148f, 6768.2f, 33.17918f)), owning_building_guid = 26)
      LocalObject(800, Door.Constructor(Vector3(4586.523f, 6719.869f, 43.17918f)), owning_building_guid = 26)
      LocalObject(801, Door.Constructor(Vector3(4588.124f, 6684.128f, 43.17918f)), owning_building_guid = 26)
      LocalObject(802, Door.Constructor(Vector3(4589.402f, 6775.508f, 33.17918f)), owning_building_guid = 26)
      LocalObject(803, Door.Constructor(Vector3(4592.23f, 6752.356f, 33.17918f)), owning_building_guid = 26)
      LocalObject(804, Door.Constructor(Vector3(4593.831f, 6716.615f, 33.17918f)), owning_building_guid = 26)
      LocalObject(807, Door.Constructor(Vector3(4599.112f, 6718.643f, 43.17918f)), owning_building_guid = 26)
      LocalObject(808, Door.Constructor(Vector3(4601.94f, 6695.491f, 33.17918f)), owning_building_guid = 26)
      LocalObject(809, Door.Constructor(Vector3(4607.273f, 6776.309f, 33.17918f)), owning_building_guid = 26)
      LocalObject(810, Door.Constructor(Vector3(4609.249f, 6692.237f, 43.17918f)), owning_building_guid = 26)
      LocalObject(811, Door.Constructor(Vector3(4614.53f, 6694.264f, 50.67918f)), owning_building_guid = 26)
      LocalObject(812, Door.Constructor(Vector3(4616.983f, 6719.443f, 45.67918f)), owning_building_guid = 26)
      LocalObject(813, Door.Constructor(Vector3(4620.663f, 6757.211f, 40.67918f)), owning_building_guid = 26)
      LocalObject(814, Door.Constructor(Vector3(4621.463f, 6739.341f, 33.17918f)), owning_building_guid = 26)
      LocalObject(815, Door.Constructor(Vector3(4623.064f, 6703.6f, 33.17918f)), owning_building_guid = 26)
      LocalObject(816, Door.Constructor(Vector3(4624.292f, 6716.189f, 55.67918f)), owning_building_guid = 26)
      LocalObject(817, Door.Constructor(Vector3(4628.346f, 6705.627f, 43.17918f)), owning_building_guid = 26)
      LocalObject(818, Door.Constructor(Vector3(4629.146f, 6687.756f, 55.67918f)), owning_building_guid = 26)
      LocalObject(819, Door.Constructor(Vector3(4629.198f, 6766.547f, 40.67918f)), owning_building_guid = 26)
      LocalObject(820, Door.Constructor(Vector3(4629.572f, 6718.216f, 50.67918f)), owning_building_guid = 26)
      LocalObject(821, Door.Constructor(Vector3(4632.826f, 6725.525f, 45.67918f)), owning_building_guid = 26)
      LocalObject(822, Door.Constructor(Vector3(4642.588f, 6747.45f, 40.67918f)), owning_building_guid = 26)
      LocalObject(823, Door.Constructor(Vector3(4643.815f, 6760.04f, 33.17918f)), owning_building_guid = 26)
      LocalObject(945, Door.Constructor(Vector3(4642.918f, 6718.521f, 51.45118f)), owning_building_guid = 26)
      LocalObject(2786, Door.Constructor(Vector3(4621.049f, 6749.019f, 41.01218f)), owning_building_guid = 26)
      LocalObject(2787, Door.Constructor(Vector3(4627.708f, 6746.055f, 41.01218f)), owning_building_guid = 26)
      LocalObject(2788, Door.Constructor(Vector3(4634.371f, 6743.088f, 41.01218f)), owning_building_guid = 26)
      LocalObject(
        997,
        IFFLock.Constructor(Vector3(4641.114f, 6722.331f, 50.61018f), Vector3(0, 0, 24)),
        owning_building_guid = 26,
        door_guid = 945
      )
      LocalObject(
        1135,
        IFFLock.Constructor(Vector3(4574.045f, 6765.683f, 50.60818f), Vector3(0, 0, 24)),
        owning_building_guid = 26,
        door_guid = 486
      )
      LocalObject(
        1138,
        IFFLock.Constructor(Vector3(4587.254f, 6766.818f, 32.99418f), Vector3(0, 0, 204)),
        owning_building_guid = 26,
        door_guid = 799
      )
      LocalObject(
        1139,
        IFFLock.Constructor(Vector3(4588.297f, 6776.89f, 32.99418f), Vector3(0, 0, 24)),
        owning_building_guid = 26,
        door_guid = 802
      )
      LocalObject(
        1142,
        IFFLock.Constructor(Vector3(4603.438f, 6696.544f, 32.99418f), Vector3(0, 0, 114)),
        owning_building_guid = 26,
        door_guid = 808
      )
      LocalObject(
        1143,
        IFFLock.Constructor(Vector3(4618.521f, 6717.869f, 55.61918f), Vector3(0, 0, 204)),
        owning_building_guid = 26,
        door_guid = 490
      )
      LocalObject(
        1144,
        IFFLock.Constructor(Vector3(4619.284f, 6756.105f, 40.49418f), Vector3(0, 0, 294)),
        owning_building_guid = 26,
        door_guid = 813
      )
      LocalObject(
        1145,
        IFFLock.Constructor(Vector3(4622.025f, 6684.312f, 55.61918f), Vector3(0, 0, 294)),
        owning_building_guid = 26,
        door_guid = 493
      )
      LocalObject(
        1146,
        IFFLock.Constructor(Vector3(4633.45f, 6683.703f, 55.61918f), Vector3(0, 0, 114)),
        owning_building_guid = 26,
        door_guid = 495
      )
      LocalObject(
        1147,
        IFFLock.Constructor(Vector3(4638.561f, 6695.406f, 63.03918f), Vector3(0, 0, 204)),
        owning_building_guid = 26,
        door_guid = 496
      )
      LocalObject(
        1148,
        IFFLock.Constructor(Vector3(4643.967f, 6748.557f, 40.49418f), Vector3(0, 0, 114)),
        owning_building_guid = 26,
        door_guid = 822
      )
      LocalObject(
        1149,
        IFFLock.Constructor(Vector3(4644.869f, 6758.542f, 32.99418f), Vector3(0, 0, 204)),
        owning_building_guid = 26,
        door_guid = 823
      )
      LocalObject(1462, Locker.Constructor(Vector3(4628.112f, 6769.432f, 31.65818f)), owning_building_guid = 26)
      LocalObject(1463, Locker.Constructor(Vector3(4628.651f, 6770.642f, 31.65818f)), owning_building_guid = 26)
      LocalObject(1464, Locker.Constructor(Vector3(4629.194f, 6771.862f, 31.65818f)), owning_building_guid = 26)
      LocalObject(1465, Locker.Constructor(Vector3(4629.738f, 6773.083f, 31.65818f)), owning_building_guid = 26)
      LocalObject(1466, Locker.Constructor(Vector3(4631.585f, 6777.231f, 31.65818f)), owning_building_guid = 26)
      LocalObject(1467, Locker.Constructor(Vector3(4632.124f, 6778.44f, 31.65818f)), owning_building_guid = 26)
      LocalObject(1468, Locker.Constructor(Vector3(4632.667f, 6779.661f, 31.65818f)), owning_building_guid = 26)
      LocalObject(1469, Locker.Constructor(Vector3(4633.21f, 6780.882f, 31.65818f)), owning_building_guid = 26)
      LocalObject(1470, Locker.Constructor(Vector3(4642.082f, 6751.576f, 39.41918f)), owning_building_guid = 26)
      LocalObject(1471, Locker.Constructor(Vector3(4642.555f, 6752.639f, 39.41918f)), owning_building_guid = 26)
      LocalObject(1472, Locker.Constructor(Vector3(4643.021f, 6753.687f, 39.41918f)), owning_building_guid = 26)
      LocalObject(1473, Locker.Constructor(Vector3(4643.489f, 6754.736f, 39.41918f)), owning_building_guid = 26)
      LocalObject(
        1918,
        Terminal.Constructor(Vector3(4631.657f, 6759.601f, 40.74818f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1919,
        Terminal.Constructor(Vector3(4635.119f, 6758.059f, 40.74818f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1920,
        Terminal.Constructor(Vector3(4637.676f, 6704.025f, 62.91318f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1921,
        Terminal.Constructor(Vector3(4638.527f, 6756.542f, 40.74818f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1922,
        Terminal.Constructor(Vector3(4638.839f, 6701.037f, 62.91318f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1923,
        Terminal.Constructor(Vector3(4641.691f, 6702.237f, 62.91318f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1924,
        Terminal.Constructor(Vector3(4649.183f, 6685.818f, 55.51818f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2695,
        Terminal.Constructor(Vector3(4595.271f, 6719.706f, 43.21518f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2696,
        Terminal.Constructor(Vector3(4617.621f, 6692.826f, 33.21518f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2697,
        Terminal.Constructor(Vector3(4623.447f, 6748.277f, 41.29218f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2698,
        Terminal.Constructor(Vector3(4630.103f, 6745.31f, 41.29218f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2699,
        Terminal.Constructor(Vector3(4636.767f, 6742.348f, 41.29218f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2700,
        Terminal.Constructor(Vector3(4653.347f, 6692.865f, 55.77518f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2915,
        Terminal.Constructor(Vector3(4603.298f, 6774.201f, 51.84518f), vehicle_terminal_combined),
        owning_building_guid = 26
      )
      LocalObject(
        1810,
        VehicleSpawnPad.Constructor(Vector3(4597.897f, 6761.692f, 47.68718f), mb_pad_creation, Vector3(0, 0, 204)),
        owning_building_guid = 26,
        terminal_guid = 2915
      )
      LocalObject(2525, ResourceSilo.Constructor(Vector3(4672.435f, 6624.949f, 56.17518f)), owning_building_guid = 26)
      LocalObject(
        2581,
        SpawnTube.Constructor(Vector3(4621.832f, 6748.189f, 39.15818f), Vector3(0, 0, 294)),
        owning_building_guid = 26
      )
      LocalObject(
        2582,
        SpawnTube.Constructor(Vector3(4628.49f, 6745.225f, 39.15818f), Vector3(0, 0, 294)),
        owning_building_guid = 26
      )
      LocalObject(
        2583,
        SpawnTube.Constructor(Vector3(4635.151f, 6742.259f, 39.15818f), Vector3(0, 0, 294)),
        owning_building_guid = 26
      )
      LocalObject(
        1830,
        ProximityTerminal.Constructor(Vector3(4631.152f, 6774.92f, 31.65818f), medical_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1831,
        ProximityTerminal.Constructor(Vector3(4658.823f, 6693.003f, 49.15818f), medical_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2156,
        ProximityTerminal.Constructor(Vector3(4573.92f, 6709.229f, 57.39918f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        2157,
        Terminal.Constructor(Vector3(4573.92f, 6709.229f, 57.39918f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2465,
        ProximityTerminal.Constructor(Vector3(4585.978f, 6657.621f, 48.70818f), repair_silo),
        owning_building_guid = 26
      )
      LocalObject(
        2466,
        Terminal.Constructor(Vector3(4585.978f, 6657.621f, 48.70818f), ground_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2473,
        ProximityTerminal.Constructor(Vector3(4679.298f, 6750.416f, 48.70818f), repair_silo),
        owning_building_guid = 26
      )
      LocalObject(
        2474,
        Terminal.Constructor(Vector3(4679.298f, 6750.416f, 48.70818f), ground_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1738,
        FacilityTurret.Constructor(Vector3(4544.932f, 6709.432f, 57.66618f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1738, 5024)
      LocalObject(
        1740,
        FacilityTurret.Constructor(Vector3(4566.764f, 6652.506f, 57.66618f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1740, 5025)
      LocalObject(
        1741,
        FacilityTurret.Constructor(Vector3(4588.146f, 6803.259f, 57.66618f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1741, 5026)
      LocalObject(
        1746,
        FacilityTurret.Constructor(Vector3(4676.015f, 6605.102f, 57.66618f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1746, 5027)
      LocalObject(
        1747,
        FacilityTurret.Constructor(Vector3(4697.273f, 6755.953f, 57.66618f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1747, 5028)
      LocalObject(
        1749,
        FacilityTurret.Constructor(Vector3(4719.11f, 6699.002f, 57.66618f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1749, 5029)
      LocalObject(
        2260,
        Painbox.Constructor(Vector3(4590.952f, 6700.534f, 36.56018f), painbox),
        owning_building_guid = 26
      )
      LocalObject(
        2274,
        Painbox.Constructor(Vector3(4626.89f, 6756.864f, 43.60298f), painbox_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        2288,
        Painbox.Constructor(Vector3(4604.71f, 6694.517f, 34.41688f), painbox_door_radius),
        owning_building_guid = 26
      )
      LocalObject(
        2318,
        Painbox.Constructor(Vector3(4619.695f, 6758.529f, 40.76588f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        2319,
        Painbox.Constructor(Vector3(4630.111f, 6768.171f, 41.98418f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        2320,
        Painbox.Constructor(Vector3(4644.311f, 6746.589f, 40.98418f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(305, Generator.Constructor(Vector3(4587.72f, 6701.795f, 30.36418f)), owning_building_guid = 26)
      LocalObject(
        291,
        Terminal.Constructor(Vector3(4595.223f, 6698.506f, 31.65818f), gen_control),
        owning_building_guid = 26
      )
    }

    Building12()

    def Building12(): Unit = { // Name: Oro Type: comm_station GUID: 29, MapID: 12
      LocalBuilding(
        "Oro",
        29,
        12,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4896f, 4538f, 208.002f),
            Vector3(0f, 0f, 295f),
            comm_station
          )
        )
      )
      LocalObject(
        220,
        CaptureTerminal.Constructor(Vector3(4984.177f, 4530.471f, 190.702f), capture_terminal),
        owning_building_guid = 29
      )
      LocalObject(511, Door.Constructor(Vector3(4816.507f, 4548.038f, 217.7169f)), owning_building_guid = 29)
      LocalObject(512, Door.Constructor(Vector3(4824.195f, 4531.55f, 209.753f)), owning_building_guid = 29)
      LocalObject(513, Door.Constructor(Vector3(4831.301f, 4573.817f, 209.753f)), owning_building_guid = 29)
      LocalObject(520, Door.Constructor(Vector3(4847.79f, 4581.506f, 217.7169f)), owning_building_guid = 29)
      LocalObject(525, Door.Constructor(Vector3(4879.6f, 4552.42f, 214.7229f)), owning_building_guid = 29)
      LocalObject(526, Door.Constructor(Vector3(4887.436f, 4538.833f, 222.162f)), owning_building_guid = 29)
      LocalObject(527, Door.Constructor(Vector3(4887.815f, 4556.251f, 214.7229f)), owning_building_guid = 29)
      LocalObject(528, Door.Constructor(Vector3(4911.751f, 4478.92f, 217.7169f)), owning_building_guid = 29)
      LocalObject(529, Door.Constructor(Vector3(4917.507f, 4539.202f, 214.7229f)), owning_building_guid = 29)
      LocalObject(530, Door.Constructor(Vector3(4928.239f, 4486.609f, 209.753f)), owning_building_guid = 29)
      LocalObject(531, Door.Constructor(Vector3(4957.819f, 4566.275f, 209.753f)), owning_building_guid = 29)
      LocalObject(532, Door.Constructor(Vector3(4965.508f, 4549.788f, 217.716f)), owning_building_guid = 29)
      LocalObject(533, Door.Constructor(Vector3(4978.403f, 4541.117f, 209.7229f)), owning_building_guid = 29)
      LocalObject(824, Door.Constructor(Vector3(4885.613f, 4550.811f, 214.7229f)), owning_building_guid = 29)
      LocalObject(825, Door.Constructor(Vector3(4899.625f, 4539.69f, 202.2229f)), owning_building_guid = 29)
      LocalObject(826, Door.Constructor(Vector3(4900.114f, 4557.572f, 209.7229f)), owning_building_guid = 29)
      LocalObject(827, Door.Constructor(Vector3(4901.56f, 4545.006f, 192.2229f)), owning_building_guid = 29)
      LocalObject(828, Door.Constructor(Vector3(4902.048f, 4562.888f, 202.2229f)), owning_building_guid = 29)
      LocalObject(829, Door.Constructor(Vector3(4908.322f, 4530.505f, 209.7229f)), owning_building_guid = 29)
      LocalObject(830, Door.Constructor(Vector3(4909.299f, 4566.269f, 192.2229f)), owning_building_guid = 29)
      LocalObject(831, Door.Constructor(Vector3(4909.788f, 4584.151f, 202.2229f)), owning_building_guid = 29)
      LocalObject(832, Door.Constructor(Vector3(4910.257f, 4535.821f, 214.7229f)), owning_building_guid = 29)
      LocalObject(833, Door.Constructor(Vector3(4911.703f, 4523.255f, 204.7229f)), owning_building_guid = 29)
      LocalObject(834, Door.Constructor(Vector3(4917.507f, 4539.202f, 204.7229f)), owning_building_guid = 29)
      LocalObject(835, Door.Constructor(Vector3(4921.846f, 4501.503f, 199.7229f)), owning_building_guid = 29)
      LocalObject(836, Door.Constructor(Vector3(4927.669f, 4583.662f, 202.2229f)), owning_building_guid = 29)
      LocalObject(837, Door.Constructor(Vector3(4928.627f, 4553.214f, 202.2229f)), owning_building_guid = 29)
      LocalObject(838, Door.Constructor(Vector3(4929.584f, 4522.766f, 192.2229f)), owning_building_guid = 29)
      LocalObject(839, Door.Constructor(Vector3(4930.542f, 4492.318f, 192.2229f)), owning_building_guid = 29)
      LocalObject(840, Door.Constructor(Vector3(4930.562f, 4558.53f, 192.2229f)), owning_building_guid = 29)
      LocalObject(841, Door.Constructor(Vector3(4937.812f, 4561.911f, 202.2229f)), owning_building_guid = 29)
      LocalObject(842, Door.Constructor(Vector3(4943.597f, 4511.646f, 199.7229f)), owning_building_guid = 29)
      LocalObject(843, Door.Constructor(Vector3(4945.043f, 4499.08f, 199.7229f)), owning_building_guid = 29)
      LocalObject(844, Door.Constructor(Vector3(4958.586f, 4536.29f, 192.2229f)), owning_building_guid = 29)
      LocalObject(845, Door.Constructor(Vector3(4966.794f, 4509.223f, 192.2229f)), owning_building_guid = 29)
      LocalObject(846, Door.Constructor(Vector3(4974.534f, 4530.486f, 192.2229f)), owning_building_guid = 29)
      LocalObject(847, Door.Constructor(Vector3(4977.915f, 4523.235f, 192.2229f)), owning_building_guid = 29)
      LocalObject(946, Door.Constructor(Vector3(4899.796f, 4520.233f, 210.4949f)), owning_building_guid = 29)
      LocalObject(2797, Door.Constructor(Vector3(4923.945f, 4510.567f, 200.056f)), owning_building_guid = 29)
      LocalObject(2798, Door.Constructor(Vector3(4930.555f, 4513.649f, 200.056f)), owning_building_guid = 29)
      LocalObject(2799, Door.Constructor(Vector3(4937.161f, 4516.729f, 200.056f)), owning_building_guid = 29)
      LocalObject(
        998,
        IFFLock.Constructor(Vector3(4903.855f, 4519.096f, 209.6539f), Vector3(0, 0, 155)),
        owning_building_guid = 29,
        door_guid = 946
      )
      LocalObject(
        1168,
        IFFLock.Constructor(Vector3(4879.73f, 4550.222f, 214.6629f), Vector3(0, 0, 245)),
        owning_building_guid = 29,
        door_guid = 525
      )
      LocalObject(
        1169,
        IFFLock.Constructor(Vector3(4885.209f, 4538.687f, 222.0829f), Vector3(0, 0, 335)),
        owning_building_guid = 29,
        door_guid = 526
      )
      LocalObject(
        1170,
        IFFLock.Constructor(Vector3(4887.686f, 4558.445f, 214.6629f), Vector3(0, 0, 65)),
        owning_building_guid = 29,
        door_guid = 527
      )
      LocalObject(
        1171,
        IFFLock.Constructor(Vector3(4909.111f, 4564.447f, 192.0379f), Vector3(0, 0, 245)),
        owning_building_guid = 29,
        door_guid = 830
      )
      LocalObject(
        1172,
        IFFLock.Constructor(Vector3(4915.31f, 4539.075f, 214.6629f), Vector3(0, 0, 335)),
        owning_building_guid = 29,
        door_guid = 529
      )
      LocalObject(
        1173,
        IFFLock.Constructor(Vector3(4921.776f, 4499.736f, 199.5379f), Vector3(0, 0, 245)),
        owning_building_guid = 29,
        door_guid = 835
      )
      LocalObject(
        1174,
        IFFLock.Constructor(Vector3(4928.72f, 4492.506f, 192.0379f), Vector3(0, 0, 335)),
        owning_building_guid = 29,
        door_guid = 839
      )
      LocalObject(
        1175,
        IFFLock.Constructor(Vector3(4943.667f, 4513.413f, 199.5379f), Vector3(0, 0, 65)),
        owning_building_guid = 29,
        door_guid = 842
      )
      LocalObject(
        1176,
        IFFLock.Constructor(Vector3(4972.765f, 4530.558f, 192.0379f), Vector3(0, 0, 335)),
        owning_building_guid = 29,
        door_guid = 846
      )
      LocalObject(
        1177,
        IFFLock.Constructor(Vector3(4979.683f, 4523.163f, 192.0379f), Vector3(0, 0, 155)),
        owning_building_guid = 29,
        door_guid = 847
      )
      LocalObject(
        1178,
        IFFLock.Constructor(Vector3(4980.575f, 4541.271f, 209.6519f), Vector3(0, 0, 155)),
        owning_building_guid = 29,
        door_guid = 533
      )
      LocalObject(1506, Locker.Constructor(Vector3(4925.292f, 4499.179f, 198.463f)), owning_building_guid = 29)
      LocalObject(1507, Locker.Constructor(Vector3(4925.784f, 4498.124f, 198.463f)), owning_building_guid = 29)
      LocalObject(1508, Locker.Constructor(Vector3(4926.269f, 4497.084f, 198.463f)), owning_building_guid = 29)
      LocalObject(1509, Locker.Constructor(Vector3(4926.754f, 4496.043f, 198.463f)), owning_building_guid = 29)
      LocalObject(1510, Locker.Constructor(Vector3(4947.933f, 4498.007f, 190.702f)), owning_building_guid = 29)
      LocalObject(1511, Locker.Constructor(Vector3(4948.492f, 4496.807f, 190.702f)), owning_building_guid = 29)
      LocalObject(1512, Locker.Constructor(Vector3(4949.057f, 4495.596f, 190.702f)), owning_building_guid = 29)
      LocalObject(1513, Locker.Constructor(Vector3(4949.622f, 4494.384f, 190.702f)), owning_building_guid = 29)
      LocalObject(1514, Locker.Constructor(Vector3(4951.541f, 4490.27f, 190.702f)), owning_building_guid = 29)
      LocalObject(1515, Locker.Constructor(Vector3(4952.1f, 4489.069f, 190.702f)), owning_building_guid = 29)
      LocalObject(1516, Locker.Constructor(Vector3(4952.665f, 4487.859f, 190.702f)), owning_building_guid = 29)
      LocalObject(1517, Locker.Constructor(Vector3(4953.229f, 4486.647f, 190.702f)), owning_building_guid = 29)
      LocalObject(
        1934,
        Terminal.Constructor(Vector3(4871.004f, 4536.96f, 214.562f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1938,
        Terminal.Constructor(Vector3(4888.312f, 4531.843f, 221.957f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1939,
        Terminal.Constructor(Vector3(4889.277f, 4534.782f, 221.957f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1940,
        Terminal.Constructor(Vector3(4892.295f, 4533.699f, 221.957f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1941,
        Terminal.Constructor(Vector3(4931.372f, 4498.604f, 199.7919f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1942,
        Terminal.Constructor(Vector3(4934.753f, 4500.18f, 199.7919f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1943,
        Terminal.Constructor(Vector3(4938.187f, 4501.782f, 199.7919f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2701,
        Terminal.Constructor(Vector3(4873.591f, 4529.194f, 214.819f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2702,
        Terminal.Constructor(Vector3(4897f, 4556.183f, 192.259f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2703,
        Terminal.Constructor(Vector3(4921.814f, 4509.245f, 200.336f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2704,
        Terminal.Constructor(Vector3(4928.421f, 4512.33f, 200.336f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2705,
        Terminal.Constructor(Vector3(4931.95f, 4555.416f, 202.259f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2706,
        Terminal.Constructor(Vector3(4935.028f, 4515.407f, 200.336f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2917,
        Terminal.Constructor(Vector3(4967.812f, 4513.605f, 210.8889f), vehicle_terminal_combined),
        owning_building_guid = 29
      )
      LocalObject(
        1811,
        VehicleSpawnPad.Constructor(Vector3(4961.914f, 4525.889f, 206.731f), mb_pad_creation, Vector3(0, 0, -25)),
        owning_building_guid = 29,
        terminal_guid = 2917
      )
      LocalObject(2526, ResourceSilo.Constructor(Vector3(4809.812f, 4559.345f, 215.2189f)), owning_building_guid = 29)
      LocalObject(
        2592,
        SpawnTube.Constructor(Vector3(4922.808f, 4510.522f, 198.202f), Vector3(0, 0, 65)),
        owning_building_guid = 29
      )
      LocalObject(
        2593,
        SpawnTube.Constructor(Vector3(4929.416f, 4513.603f, 198.202f), Vector3(0, 0, 65)),
        owning_building_guid = 29
      )
      LocalObject(
        2594,
        SpawnTube.Constructor(Vector3(4936.021f, 4516.683f, 198.202f), Vector3(0, 0, 65)),
        owning_building_guid = 29
      )
      LocalObject(
        1832,
        ProximityTerminal.Constructor(Vector3(4870.103f, 4524.971f, 208.202f), medical_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1833,
        ProximityTerminal.Constructor(Vector3(4950.08f, 4492.112f, 190.702f), medical_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2159,
        ProximityTerminal.Constructor(Vector3(4938.05f, 4578.402f, 216.4429f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        2160,
        Terminal.Constructor(Vector3(4938.05f, 4578.402f, 216.4429f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2477,
        ProximityTerminal.Constructor(Vector3(4891.191f, 4603.161f, 207.752f), repair_silo),
        owning_building_guid = 29
      )
      LocalObject(
        2478,
        Terminal.Constructor(Vector3(4891.191f, 4603.161f, 207.752f), ground_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2481,
        ProximityTerminal.Constructor(Vector3(4900f, 4471.852f, 207.752f), repair_silo),
        owning_building_guid = 29
      )
      LocalObject(
        2482,
        Terminal.Constructor(Vector3(4900f, 4471.852f, 207.752f), ground_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1752,
        FacilityTurret.Constructor(Vector3(4792.484f, 4569.665f, 216.71f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1752, 5030)
      LocalObject(
        1753,
        FacilityTurret.Constructor(Vector3(4835.079f, 4475.536f, 216.71f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1753, 5031)
      LocalObject(
        1756,
        FacilityTurret.Constructor(Vector3(4892.386f, 4454.654f, 216.71f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1756, 5032)
      LocalObject(
        1757,
        FacilityTurret.Constructor(Vector3(4899.936f, 4621.018f, 216.71f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1757, 5033)
      LocalObject(
        1758,
        FacilityTurret.Constructor(Vector3(4957.222f, 4600.147f, 216.71f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1758, 5034)
      LocalObject(
        1759,
        FacilityTurret.Constructor(Vector3(4999.683f, 4505.977f, 216.71f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1759, 5035)
      LocalObject(
        2261,
        Painbox.Constructor(Vector3(4920.314f, 4571.253f, 195.604f), painbox),
        owning_building_guid = 29
      )
      LocalObject(
        2275,
        Painbox.Constructor(Vector3(4939.249f, 4507.175f, 202.6468f), painbox_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        2289,
        Painbox.Constructor(Vector3(4906.746f, 4564.817f, 193.4606f), painbox_door_radius),
        owning_building_guid = 29
      )
      LocalObject(
        2321,
        Painbox.Constructor(Vector3(4920.065f, 4500.768f, 200.028f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        2322,
        Painbox.Constructor(Vector3(4945.227f, 4511.512f, 199.8096f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        2323,
        Painbox.Constructor(Vector3(4945.67f, 4497.326f, 201.028f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(306, Generator.Constructor(Vector3(4923.386f, 4572.865f, 189.408f)), owning_building_guid = 29)
      LocalObject(
        292,
        Terminal.Constructor(Vector3(4915.981f, 4569.361f, 190.702f), gen_control),
        owning_building_guid = 29
      )
    }

    Building15()

    def Building15(): Unit = { // Name: Sina Type: comm_station_dsp GUID: 32, MapID: 15
      LocalBuilding(
        "Sina",
        32,
        15,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5838f, 2198f, 90.95889f),
            Vector3(0f, 0f, 39f),
            comm_station_dsp
          )
        )
      )
      LocalObject(
        223,
        CaptureTerminal.Constructor(Vector3(5909.257f, 2230.912f, 73.55889f), capture_terminal),
        owning_building_guid = 32
      )
      LocalObject(277, Door.Constructor(Vector3(5846.765f, 2295.768f, 94.33689f)), owning_building_guid = 32)
      LocalObject(572, Door.Constructor(Vector3(5756.884f, 2286.67f, 92.60989f)), owning_building_guid = 32)
      LocalObject(573, Door.Constructor(Vector3(5758.785f, 2304.764f, 100.5729f)), owning_building_guid = 32)
      LocalObject(574, Door.Constructor(Vector3(5771.655f, 2355.758f, 92.60989f)), owning_building_guid = 32)
      LocalObject(575, Door.Constructor(Vector3(5785.794f, 2367.207f, 100.5729f)), owning_building_guid = 32)
      LocalObject(576, Door.Constructor(Vector3(5807.45f, 2140.697f, 100.5739f)), owning_building_guid = 32)
      LocalObject(577, Door.Constructor(Vector3(5814.256f, 2313.052f, 97.57589f)), owning_building_guid = 32)
      LocalObject(578, Door.Constructor(Vector3(5818.898f, 2126.559f, 92.60989f)), owning_building_guid = 32)
      LocalObject(579, Door.Constructor(Vector3(5822.271f, 2185.643f, 97.5799f)), owning_building_guid = 32)
      LocalObject(580, Door.Constructor(Vector3(5827.976f, 2178.599f, 97.5799f)), owning_building_guid = 32)
      LocalObject(581, Door.Constructor(Vector3(5831.631f, 2218.577f, 97.5799f)), owning_building_guid = 32)
      LocalObject(582, Door.Constructor(Vector3(5834.673f, 2290.977f, 92.58189f)), owning_building_guid = 32)
      LocalObject(583, Door.Constructor(Vector3(5839.263f, 2189.488f, 105.0189f)), owning_building_guid = 32)
      LocalObject(584, Door.Constructor(Vector3(5847.491f, 2118.44f, 100.5739f)), owning_building_guid = 32)
      LocalObject(585, Door.Constructor(Vector3(5861.629f, 2129.888f, 92.60989f)), owning_building_guid = 32)
      LocalObject(586, Door.Constructor(Vector3(5870.036f, 2328.427f, 92.60989f)), owning_building_guid = 32)
      LocalObject(587, Door.Constructor(Vector3(5877.803f, 2333.173f, 92.5799f)), owning_building_guid = 32)
      LocalObject(588, Door.Constructor(Vector3(5881.484f, 2314.289f, 100.5729f)), owning_building_guid = 32)
      LocalObject(589, Door.Constructor(Vector3(5908.042f, 2238.514f, 100.5739f)), owning_building_guid = 32)
      LocalObject(590, Door.Constructor(Vector3(5909.944f, 2256.606f, 92.60989f)), owning_building_guid = 32)
      LocalObject(893, Door.Constructor(Vector3(5818.014f, 2197.257f, 92.5799f)), owning_building_guid = 32)
      LocalObject(894, Door.Constructor(Vector3(5825.19f, 2309.034f, 97.58189f)), owning_building_guid = 32)
      LocalObject(895, Door.Constructor(Vector3(5828.083f, 2184.822f, 97.5799f)), owning_building_guid = 32)
      LocalObject(896, Door.Constructor(Vector3(5828.961f, 2247.298f, 82.5799f)), owning_building_guid = 32)
      LocalObject(897, Door.Constructor(Vector3(5831.327f, 2269.802f, 75.0799f)), owning_building_guid = 32)
      LocalObject(898, Door.Constructor(Vector3(5831.631f, 2218.577f, 87.5799f)), owning_building_guid = 32)
      LocalObject(899, Door.Constructor(Vector3(5836.666f, 2212.36f, 97.5799f)), owning_building_guid = 32)
      LocalObject(900, Door.Constructor(Vector3(5839.622f, 2240.49f, 75.0799f)), owning_building_guid = 32)
      LocalObject(901, Door.Constructor(Vector3(5842.291f, 2211.769f, 92.5799f)), owning_building_guid = 32)
      LocalObject(902, Door.Constructor(Vector3(5848.508f, 2216.803f, 87.5799f)), owning_building_guid = 32)
      LocalObject(903, Door.Constructor(Vector3(5852.056f, 2250.559f, 82.5799f)), owning_building_guid = 32)
      LocalObject(904, Door.Constructor(Vector3(5863.899f, 2255.002f, 82.5799f)), owning_building_guid = 32)
      LocalObject(905, Door.Constructor(Vector3(5867.16f, 2231.907f, 82.5799f)), owning_building_guid = 32)
      LocalObject(906, Door.Constructor(Vector3(5871.603f, 2220.064f, 75.0799f)), owning_building_guid = 32)
      LocalObject(907, Door.Constructor(Vector3(5873.968f, 2242.568f, 75.0799f)), owning_building_guid = 32)
      LocalObject(908, Door.Constructor(Vector3(5877.229f, 2219.473f, 82.5799f)), owning_building_guid = 32)
      LocalObject(909, Door.Constructor(Vector3(5883.733f, 2281.357f, 82.5799f)), owning_building_guid = 32)
      LocalObject(910, Door.Constructor(Vector3(5903.872f, 2256.489f, 82.5799f)), owning_building_guid = 32)
      LocalObject(911, Door.Constructor(Vector3(5911.576f, 2221.551f, 75.0799f)), owning_building_guid = 32)
      LocalObject(912, Door.Constructor(Vector3(5913.35f, 2238.428f, 75.0799f)), owning_building_guid = 32)
      LocalObject(913, Door.Constructor(Vector3(5917.792f, 2226.585f, 75.0799f)), owning_building_guid = 32)
      LocalObject(949, Door.Constructor(Vector3(5854.327f, 2205.974f, 93.35089f)), owning_building_guid = 32)
      LocalObject(2810, Door.Constructor(Vector3(5848.681f, 2243.084f, 82.9129f)), owning_building_guid = 32)
      LocalObject(2811, Door.Constructor(Vector3(5853.268f, 2237.419f, 82.9129f)), owning_building_guid = 32)
      LocalObject(2812, Door.Constructor(Vector3(5857.858f, 2231.752f, 82.9129f)), owning_building_guid = 32)
      LocalObject(
        1001,
        IFFLock.Constructor(Vector3(5854.468f, 2210.165f, 92.52689f), Vector3(0, 0, 51)),
        owning_building_guid = 32,
        door_guid = 949
      )
      LocalObject(
        1206,
        IFFLock.Constructor(Vector3(5812.158f, 2312.393f, 97.50589f), Vector3(0, 0, 321)),
        owning_building_guid = 32,
        door_guid = 577
      )
      LocalObject(
        1207,
        IFFLock.Constructor(Vector3(5820.176f, 2184.987f, 97.52689f), Vector3(0, 0, 321)),
        owning_building_guid = 32,
        door_guid = 579
      )
      LocalObject(
        1208,
        IFFLock.Constructor(Vector3(5830.071f, 2179.252f, 97.52689f), Vector3(0, 0, 141)),
        owning_building_guid = 32,
        door_guid = 580
      )
      LocalObject(
        1209,
        IFFLock.Constructor(Vector3(5831.068f, 2271.615f, 74.8949f), Vector3(0, 0, 51)),
        owning_building_guid = 32,
        door_guid = 897
      )
      LocalObject(
        1210,
        IFFLock.Constructor(Vector3(5832.286f, 2216.486f, 97.52689f), Vector3(0, 0, 231)),
        owning_building_guid = 32,
        door_guid = 581
      )
      LocalObject(
        1211,
        IFFLock.Constructor(Vector3(5835.332f, 2288.888f, 92.57089f), Vector3(0, 0, 231)),
        owning_building_guid = 32,
        door_guid = 582
      )
      LocalObject(
        1212,
        IFFLock.Constructor(Vector3(5839.942f, 2187.366f, 105.0269f), Vector3(0, 0, 231)),
        owning_building_guid = 32,
        door_guid = 583
      )
      LocalObject(
        1213,
        IFFLock.Constructor(Vector3(5850.243f, 2250.3f, 82.39489f), Vector3(0, 0, 321)),
        owning_building_guid = 32,
        door_guid = 903
      )
      LocalObject(
        1214,
        IFFLock.Constructor(Vector3(5868.891f, 2232.267f, 82.39489f), Vector3(0, 0, 141)),
        owning_building_guid = 32,
        door_guid = 905
      )
      LocalObject(
        1215,
        IFFLock.Constructor(Vector3(5874.227f, 2240.754f, 74.8949f), Vector3(0, 0, 231)),
        owning_building_guid = 32,
        door_guid = 907
      )
      LocalObject(
        1216,
        IFFLock.Constructor(Vector3(5875.704f, 2332.512f, 92.46989f), Vector3(0, 0, 321)),
        owning_building_guid = 32,
        door_guid = 587
      )
      LocalObject(
        1217,
        IFFLock.Constructor(Vector3(5911.834f, 2219.738f, 74.8949f), Vector3(0, 0, 231)),
        owning_building_guid = 32,
        door_guid = 911
      )
      LocalObject(
        1219,
        IFFLock.Constructor(Vector3(5917.435f, 2228.319f, 74.8949f), Vector3(0, 0, 51)),
        owning_building_guid = 32,
        door_guid = 913
      )
      LocalObject(1567, Locker.Constructor(Vector3(5864.242f, 2258.065f, 73.55889f)), owning_building_guid = 32)
      LocalObject(1568, Locker.Constructor(Vector3(5865.271f, 2258.898f, 73.55889f)), owning_building_guid = 32)
      LocalObject(1569, Locker.Constructor(Vector3(5866.309f, 2259.739f, 73.55889f)), owning_building_guid = 32)
      LocalObject(1570, Locker.Constructor(Vector3(5867.348f, 2260.581f, 73.55889f)), owning_building_guid = 32)
      LocalObject(1571, Locker.Constructor(Vector3(5868.582f, 2235.813f, 81.31989f)), owning_building_guid = 32)
      LocalObject(1572, Locker.Constructor(Vector3(5869.486f, 2236.546f, 81.31989f)), owning_building_guid = 32)
      LocalObject(1573, Locker.Constructor(Vector3(5870.377f, 2237.268f, 81.31989f)), owning_building_guid = 32)
      LocalObject(1574, Locker.Constructor(Vector3(5870.876f, 2263.438f, 73.55889f)), owning_building_guid = 32)
      LocalObject(1575, Locker.Constructor(Vector3(5871.271f, 2237.991f, 81.31989f)), owning_building_guid = 32)
      LocalObject(1576, Locker.Constructor(Vector3(5871.905f, 2264.271f, 73.55889f)), owning_building_guid = 32)
      LocalObject(1577, Locker.Constructor(Vector3(5872.943f, 2265.112f, 73.55889f)), owning_building_guid = 32)
      LocalObject(1578, Locker.Constructor(Vector3(5873.982f, 2265.953f, 73.55889f)), owning_building_guid = 32)
      LocalObject(
        279,
        Terminal.Constructor(Vector3(5819.766f, 2315.665f, 96.6629f), dropship_vehicle_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        278,
        VehicleSpawnPad.Constructor(Vector3(5812.526f, 2338.031f, 90.98689f), dropship_pad_doors, Vector3(0, 0, 51)),
        owning_building_guid = 32,
        terminal_guid = 279
      )
      LocalObject(
        1960,
        Terminal.Constructor(Vector3(5842.749f, 2192.255f, 104.8139f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1961,
        Terminal.Constructor(Vector3(5843.069f, 2195.446f, 104.8139f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1962,
        Terminal.Constructor(Vector3(5845.062f, 2173.99f, 97.41889f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1963,
        Terminal.Constructor(Vector3(5845.834f, 2192.03f, 104.8139f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1964,
        Terminal.Constructor(Vector3(5846.232f, 2195.131f, 104.8139f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1965,
        Terminal.Constructor(Vector3(5862.937f, 2247.696f, 82.6489f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1966,
        Terminal.Constructor(Vector3(5865.321f, 2244.751f, 82.6489f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1967,
        Terminal.Constructor(Vector3(5867.669f, 2241.852f, 82.6489f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2720,
        Terminal.Constructor(Vector3(5821.097f, 2306.433f, 97.6069f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2721,
        Terminal.Constructor(Vector3(5850.48f, 2241.334f, 83.19289f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2722,
        Terminal.Constructor(Vector3(5851.971f, 2178.378f, 97.6759f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2723,
        Terminal.Constructor(Vector3(5855.064f, 2235.668f, 83.19289f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2724,
        Terminal.Constructor(Vector3(5858.29f, 2286.414f, 75.08689f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2725,
        Terminal.Constructor(Vector3(5859.656f, 2230.004f, 83.19289f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2726,
        Terminal.Constructor(Vector3(5870.381f, 2285.226f, 82.61589f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2727,
        Terminal.Constructor(Vector3(5888.154f, 2237.854f, 75.08689f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2728,
        Terminal.Constructor(Vector3(5895.554f, 2254.14f, 82.61589f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2920,
        Terminal.Constructor(Vector3(5783.76f, 2293.104f, 93.7459f), ground_vehicle_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1814,
        VehicleSpawnPad.Constructor(Vector3(5792.408f, 2282.564f, 89.58789f), mb_pad_creation, Vector3(0, 0, 141)),
        owning_building_guid = 32,
        terminal_guid = 2920
      )
      LocalObject(2529, ResourceSilo.Constructor(Vector3(5810.083f, 2388.535f, 98.07589f)), owning_building_guid = 32)
      LocalObject(
        2605,
        SpawnTube.Constructor(Vector3(5849.002f, 2241.989f, 81.05889f), Vector3(0, 0, 321)),
        owning_building_guid = 32
      )
      LocalObject(
        2606,
        SpawnTube.Constructor(Vector3(5853.588f, 2236.325f, 81.05889f), Vector3(0, 0, 321)),
        owning_building_guid = 32
      )
      LocalObject(
        2607,
        SpawnTube.Constructor(Vector3(5858.177f, 2230.659f, 81.05889f), Vector3(0, 0, 321)),
        owning_building_guid = 32
      )
      LocalObject(
        1837,
        ProximityTerminal.Constructor(Vector3(5856.913f, 2176.016f, 91.05889f), medical_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1838,
        ProximityTerminal.Constructor(Vector3(5869.442f, 2261.575f, 73.55889f), medical_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2186,
        ProximityTerminal.Constructor(Vector3(5764.576f, 2258.723f, 99.3689f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        2187,
        Terminal.Constructor(Vector3(5764.576f, 2258.723f, 99.3689f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2189,
        ProximityTerminal.Constructor(Vector3(5806.196f, 2233.324f, 96.65289f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        2190,
        Terminal.Constructor(Vector3(5806.196f, 2233.324f, 96.65289f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2192,
        ProximityTerminal.Constructor(Vector3(5869.215f, 2238.592f, 103.8349f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        2193,
        Terminal.Constructor(Vector3(5869.215f, 2238.592f, 103.8349f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2195,
        ProximityTerminal.Constructor(Vector3(5886.392f, 2273.421f, 99.3819f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        2196,
        Terminal.Constructor(Vector3(5886.392f, 2273.421f, 99.3819f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2501,
        ProximityTerminal.Constructor(Vector3(5766.25f, 2189.105f, 90.70889f), repair_silo),
        owning_building_guid = 32
      )
      LocalObject(
        2502,
        Terminal.Constructor(Vector3(5766.25f, 2189.105f, 90.70889f), ground_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2505,
        ProximityTerminal.Constructor(Vector3(5896.477f, 2298.306f, 90.70889f), repair_silo),
        owning_building_guid = 32
      )
      LocalObject(
        2506,
        Terminal.Constructor(Vector3(5896.477f, 2298.306f, 90.70889f), ground_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1775,
        FacilityTurret.Constructor(Vector3(5734.792f, 2208.502f, 99.56689f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1775, 5036)
      LocalObject(
        1776,
        FacilityTurret.Constructor(Vector3(5740.5f, 2273.029f, 99.56689f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1776, 5037)
      LocalObject(
        1777,
        FacilityTurret.Constructor(Vector3(5750.325f, 2356.243f, 99.56689f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1777, 5038)
      LocalObject(
        1778,
        FacilityTurret.Constructor(Vector3(5819.581f, 2410.822f, 99.56689f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1778, 5039)
      LocalObject(
        1779,
        FacilityTurret.Constructor(Vector3(5832.318f, 2089.898f, 99.56689f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1779, 5040)
      LocalObject(
        1780,
        FacilityTurret.Constructor(Vector3(5913.002f, 2153.731f, 99.56689f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1780, 5041)
      LocalObject(
        1781,
        FacilityTurret.Constructor(Vector3(5920.167f, 2211.795f, 99.56689f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1781, 5042)
      LocalObject(
        1782,
        FacilityTurret.Constructor(Vector3(5926.362f, 2280.792f, 99.56689f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1782, 5043)
      LocalObject(
        2264,
        Painbox.Constructor(Vector3(5822.298f, 2262.563f, 77.45319f), painbox),
        owning_building_guid = 32
      )
      LocalObject(
        2278,
        Painbox.Constructor(Vector3(5860.794f, 2242.719f, 85.0864f), painbox_continuous),
        owning_building_guid = 32
      )
      LocalObject(
        2292,
        Painbox.Constructor(Vector3(5833.722f, 2270.344f, 76.69109f), painbox_door_radius),
        owning_building_guid = 32
      )
      LocalObject(
        2330,
        Painbox.Constructor(Vector3(5850.665f, 2252.11f, 83.95889f), painbox_door_radius_continuous),
        owning_building_guid = 32
      )
      LocalObject(
        2331,
        Painbox.Constructor(Vector3(5865.77f, 2256.373f, 84.38919f), painbox_door_radius_continuous),
        owning_building_guid = 32
      )
      LocalObject(
        2332,
        Painbox.Constructor(Vector3(5868.096f, 2229.302f, 83.48809f), painbox_door_radius_continuous),
        owning_building_guid = 32
      )
      LocalObject(309, Generator.Constructor(Vector3(5819.254f, 2259.993f, 72.26489f)), owning_building_guid = 32)
      LocalObject(
        295,
        Terminal.Constructor(Vector3(5825.591f, 2265.185f, 73.55889f), gen_control),
        owning_building_guid = 32
      )
    }

    Building5()

    def Building5(): Unit = { // Name: Drakulu Type: cryo_facility GUID: 35, MapID: 5
      LocalBuilding(
        "Drakulu",
        35,
        5,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3746f, 2702f, 151.0666f),
            Vector3(0f, 0f, 266f),
            cryo_facility
          )
        )
      )
      LocalObject(
        215,
        CaptureTerminal.Constructor(Vector3(3807.081f, 2725.886f, 141.0666f), capture_terminal),
        owning_building_guid = 35
      )
      LocalObject(390, Door.Constructor(Vector3(3651.992f, 2676.496f, 152.5876f)), owning_building_guid = 35)
      LocalObject(393, Door.Constructor(Vector3(3661.636f, 2684.342f, 152.6176f)), owning_building_guid = 35)
      LocalObject(394, Door.Constructor(Vector3(3662.905f, 2702.491f, 160.5816f)), owning_building_guid = 35)
      LocalObject(397, Door.Constructor(Vector3(3691.878f, 2654.733f, 160.5816f)), owning_building_guid = 35)
      LocalObject(402, Door.Constructor(Vector3(3710.027f, 2653.464f, 152.6176f)), owning_building_guid = 35)
      LocalObject(411, Door.Constructor(Vector3(3748.85f, 2697.791f, 162.5876f)), owning_building_guid = 35)
      LocalObject(414, Door.Constructor(Vector3(3754.603f, 2760.52f, 152.6176f)), owning_building_guid = 35)
      LocalObject(418, Door.Constructor(Vector3(3767.067f, 2716.566f, 162.5876f)), owning_building_guid = 35)
      LocalObject(420, Door.Constructor(Vector3(3772.752f, 2759.25f, 160.5816f)), owning_building_guid = 35)
      LocalObject(428, Door.Constructor(Vector3(3815.321f, 2721.344f, 160.5816f)), owning_building_guid = 35)
      LocalObject(429, Door.Constructor(Vector3(3816.59f, 2739.493f, 152.6176f)), owning_building_guid = 35)
      LocalObject(672, Door.Constructor(Vector3(3659.052f, 2720.109f, 145.0876f)), owning_building_guid = 35)
      LocalObject(674, Door.Constructor(Vector3(3688.937f, 2746.088f, 145.0876f)), owning_building_guid = 35)
      LocalObject(676, Door.Constructor(Vector3(3691.531f, 2725.858f, 145.0876f)), owning_building_guid = 35)
      LocalObject(680, Door.Constructor(Vector3(3715.473f, 2724.184f, 142.5876f)), owning_building_guid = 35)
      LocalObject(684, Door.Constructor(Vector3(3718.068f, 2703.953f, 142.5876f)), owning_building_guid = 35)
      LocalObject(693, Door.Constructor(Vector3(3737.377f, 2750.72f, 142.5876f)), owning_building_guid = 35)
      LocalObject(694, Door.Constructor(Vector3(3737.74f, 2698.568f, 162.5876f)), owning_building_guid = 35)
      LocalObject(700, Door.Constructor(Vector3(3742.01f, 2702.279f, 142.5876f)), owning_building_guid = 35)
      LocalObject(701, Door.Constructor(Vector3(3745.163f, 2690.029f, 142.5876f)), owning_building_guid = 35)
      LocalObject(703, Door.Constructor(Vector3(3746.837f, 2713.971f, 142.5876f)), owning_building_guid = 35)
      LocalObject(705, Door.Constructor(Vector3(3748.316f, 2677.78f, 135.0876f)), owning_building_guid = 35)
      LocalObject(706, Door.Constructor(Vector3(3748.511f, 2737.912f, 142.5876f)), owning_building_guid = 35)
      LocalObject(710, Door.Constructor(Vector3(3764.277f, 2676.663f, 142.5876f)), owning_building_guid = 35)
      LocalObject(712, Door.Constructor(Vector3(3767.067f, 2716.566f, 142.5876f)), owning_building_guid = 35)
      LocalObject(713, Door.Constructor(Vector3(3767.067f, 2716.566f, 152.5876f)), owning_building_guid = 35)
      LocalObject(714, Door.Constructor(Vector3(3768.184f, 2732.527f, 142.5876f)), owning_building_guid = 35)
      LocalObject(716, Door.Constructor(Vector3(3769.104f, 2688.355f, 142.5876f)), owning_building_guid = 35)
      LocalObject(723, Door.Constructor(Vector3(3786.74f, 2711.18f, 142.5876f)), owning_building_guid = 35)
      LocalObject(725, Door.Constructor(Vector3(3788.414f, 2735.122f, 142.5876f)), owning_building_guid = 35)
      LocalObject(728, Door.Constructor(Vector3(3797.873f, 2698.373f, 142.5876f)), owning_building_guid = 35)
      LocalObject(733, Door.Constructor(Vector3(3814.95f, 2713.218f, 142.5876f)), owning_building_guid = 35)
      LocalObject(734, Door.Constructor(Vector3(3815.509f, 2721.198f, 142.5876f)), owning_building_guid = 35)
      LocalObject(735, Door.Constructor(Vector3(3816.067f, 2729.179f, 142.5876f)), owning_building_guid = 35)
      LocalObject(940, Door.Constructor(Vector3(3770.743f, 2696.268f, 153.3496f)), owning_building_guid = 35)
      LocalObject(951, Door.Constructor(Vector3(3745.163f, 2690.029f, 152.5876f)), owning_building_guid = 35)
      LocalObject(952, Door.Constructor(Vector3(3754.26f, 2705.432f, 152.5856f)), owning_building_guid = 35)
      LocalObject(2765, Door.Constructor(Vector3(3751.393f, 2696.938f, 142.9206f)), owning_building_guid = 35)
      LocalObject(2766, Door.Constructor(Vector3(3758.668f, 2696.43f, 142.9206f)), owning_building_guid = 35)
      LocalObject(2767, Door.Constructor(Vector3(3765.939f, 2695.921f, 142.9206f)), owning_building_guid = 35)
      LocalObject(
        992,
        IFFLock.Constructor(Vector3(3773.7f, 2699.291f, 152.5486f), Vector3(0, 0, 94)),
        owning_building_guid = 35,
        door_guid = 940
      )
      LocalObject(
        1073,
        IFFLock.Constructor(Vector3(3653.972f, 2675.541f, 152.5186f), Vector3(0, 0, 184)),
        owning_building_guid = 35,
        door_guid = 390
      )
      LocalObject(
        1082,
        IFFLock.Constructor(Vector3(3744.245f, 2688.518f, 142.4026f), Vector3(0, 0, 274)),
        owning_building_guid = 35,
        door_guid = 701
      )
      LocalObject(
        1084,
        IFFLock.Constructor(Vector3(3746.804f, 2678.697f, 134.9026f), Vector3(0, 0, 4)),
        owning_building_guid = 35,
        door_guid = 705
      )
      LocalObject(
        1085,
        IFFLock.Constructor(Vector3(3749.812f, 2699.775f, 162.5186f), Vector3(0, 0, 94)),
        owning_building_guid = 35,
        door_guid = 411
      )
      LocalObject(
        1088,
        IFFLock.Constructor(Vector3(3769.049f, 2715.611f, 162.5186f), Vector3(0, 0, 184)),
        owning_building_guid = 35,
        door_guid = 418
      )
      LocalObject(
        1089,
        IFFLock.Constructor(Vector3(3770.022f, 2689.867f, 142.4026f), Vector3(0, 0, 94)),
        owning_building_guid = 35,
        door_guid = 716
      )
      LocalObject(
        1098,
        IFFLock.Constructor(Vector3(3814.564f, 2730.226f, 142.4026f), Vector3(0, 0, 4)),
        owning_building_guid = 35,
        door_guid = 735
      )
      LocalObject(
        1099,
        IFFLock.Constructor(Vector3(3817.02f, 2720.277f, 142.4026f), Vector3(0, 0, 184)),
        owning_building_guid = 35,
        door_guid = 734
      )
      LocalObject(1356, Locker.Constructor(Vector3(3745.697f, 2663.83f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1357, Locker.Constructor(Vector3(3745.77f, 2664.883f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1358, Locker.Constructor(Vector3(3745.844f, 2665.94f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1359, Locker.Constructor(Vector3(3745.918f, 2666.992f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1360, Locker.Constructor(Vector3(3745.991f, 2668.045f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1361, Locker.Constructor(Vector3(3746.065f, 2669.098f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1362, Locker.Constructor(Vector3(3746.809f, 2682.874f, 141.3276f)), owning_building_guid = 35)
      LocalObject(1363, Locker.Constructor(Vector3(3746.889f, 2684.02f, 141.3276f)), owning_building_guid = 35)
      LocalObject(1364, Locker.Constructor(Vector3(3746.969f, 2685.164f, 141.3276f)), owning_building_guid = 35)
      LocalObject(1365, Locker.Constructor(Vector3(3747.05f, 2686.326f, 141.3276f)), owning_building_guid = 35)
      LocalObject(1370, Locker.Constructor(Vector3(3765.649f, 2662.438f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1371, Locker.Constructor(Vector3(3765.723f, 2663.491f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1372, Locker.Constructor(Vector3(3765.796f, 2664.544f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1373, Locker.Constructor(Vector3(3765.87f, 2665.596f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1374, Locker.Constructor(Vector3(3765.944f, 2666.655f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1375, Locker.Constructor(Vector3(3766.018f, 2667.706f, 140.9746f)), owning_building_guid = 35)
      LocalObject(1376, Locker.Constructor(Vector3(3793.98f, 2678.599f, 141.0616f)), owning_building_guid = 35)
      LocalObject(1377, Locker.Constructor(Vector3(3795.233f, 2678.511f, 141.0616f)), owning_building_guid = 35)
      LocalObject(1378, Locker.Constructor(Vector3(3796.491f, 2678.424f, 141.0616f)), owning_building_guid = 35)
      LocalObject(1379, Locker.Constructor(Vector3(3797.75f, 2678.335f, 141.0616f)), owning_building_guid = 35)
      LocalObject(1381, Locker.Constructor(Vector3(3798.999f, 2678.248f, 141.0616f)), owning_building_guid = 35)
      LocalObject(1632, Locker.Constructor(Vector3(3732.543f, 2684.636f, 151.0666f)), owning_building_guid = 35)
      LocalObject(1633, Locker.Constructor(Vector3(3733.574f, 2684.564f, 151.0666f)), owning_building_guid = 35)
      LocalObject(1634, Locker.Constructor(Vector3(3736.085f, 2684.389f, 150.8376f)), owning_building_guid = 35)
      LocalObject(1635, Locker.Constructor(Vector3(3737.117f, 2684.317f, 150.8376f)), owning_building_guid = 35)
      LocalObject(1636, Locker.Constructor(Vector3(3738.168f, 2684.243f, 150.8376f)), owning_building_guid = 35)
      LocalObject(1637, Locker.Constructor(Vector3(3739.2f, 2684.171f, 150.8376f)), owning_building_guid = 35)
      LocalObject(1638, Locker.Constructor(Vector3(3741.716f, 2683.995f, 151.0666f)), owning_building_guid = 35)
      LocalObject(1639, Locker.Constructor(Vector3(3742.747f, 2683.923f, 151.0666f)), owning_building_guid = 35)
      LocalObject(
        225,
        Terminal.Constructor(Vector3(3791.542f, 2682.351f, 141.0566f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        226,
        Terminal.Constructor(Vector3(3792.428f, 2695.02f, 141.0566f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        227,
        Terminal.Constructor(Vector3(3792.886f, 2680.806f, 141.0566f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        228,
        Terminal.Constructor(Vector3(3793.974f, 2696.364f, 141.0566f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        229,
        Terminal.Constructor(Vector3(3800.193f, 2680.295f, 141.0566f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        230,
        Terminal.Constructor(Vector3(3801.281f, 2695.853f, 141.0566f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        231,
        Terminal.Constructor(Vector3(3801.739f, 2681.638f, 141.0566f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        232,
        Terminal.Constructor(Vector3(3802.624f, 2694.307f, 141.0566f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1887,
        Terminal.Constructor(Vector3(3752.089f, 2682.875f, 142.6566f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1888,
        Terminal.Constructor(Vector3(3755.811f, 2682.615f, 142.6566f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1889,
        Terminal.Constructor(Vector3(3756.202f, 2711.339f, 152.3616f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1890,
        Terminal.Constructor(Vector3(3759.59f, 2682.35f, 142.6566f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2655,
        Terminal.Constructor(Vector3(3690.67f, 2721.998f, 145.1796f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2662,
        Terminal.Constructor(Vector3(3732.259f, 2701.051f, 152.6456f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2663,
        Terminal.Constructor(Vector3(3748.199f, 2741.944f, 142.6796f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2664,
        Terminal.Constructor(Vector3(3748.888f, 2696.815f, 143.2006f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2665,
        Terminal.Constructor(Vector3(3756.163f, 2696.31f, 143.2006f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2666,
        Terminal.Constructor(Vector3(3763.433f, 2695.799f, 143.2006f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2674,
        Terminal.Constructor(Vector3(3809.885f, 2698.127f, 142.6796f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2910,
        Terminal.Constructor(Vector3(3680.343f, 2668.871f, 153.3716f), vehicle_terminal_combined),
        owning_building_guid = 35
      )
      LocalObject(
        1800,
        VehicleSpawnPad.Constructor(Vector3(3681.205f, 2682.483f, 149.2136f), mb_pad_creation, Vector3(0, 0, 4)),
        owning_building_guid = 35,
        terminal_guid = 2910
      )
      LocalObject(2521, ResourceSilo.Constructor(Vector3(3800.932f, 2758.573f, 158.0836f)), owning_building_guid = 35)
      LocalObject(
        2560,
        SpawnTube.Constructor(Vector3(3750.376f, 2697.451f, 141.0666f), Vector3(0, 0, 94)),
        owning_building_guid = 35
      )
      LocalObject(
        2561,
        SpawnTube.Constructor(Vector3(3757.65f, 2696.942f, 141.0666f), Vector3(0, 0, 94)),
        owning_building_guid = 35
      )
      LocalObject(
        2562,
        SpawnTube.Constructor(Vector3(3764.92f, 2696.434f, 141.0666f), Vector3(0, 0, 94)),
        owning_building_guid = 35
      )
      LocalObject(
        153,
        ProximityTerminal.Constructor(Vector3(3738.771f, 2700.518f, 150.8766f), adv_med_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1822,
        ProximityTerminal.Constructor(Vector3(3748.014f, 2674.15f, 141.0666f), medical_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2072,
        ProximityTerminal.Constructor(Vector3(3677.071f, 2708.626f, 159.4186f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        2073,
        Terminal.Constructor(Vector3(3677.071f, 2708.626f, 159.4186f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2075,
        ProximityTerminal.Constructor(Vector3(3685.476f, 2724.394f, 161.3596f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        2076,
        Terminal.Constructor(Vector3(3685.476f, 2724.394f, 161.3596f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2096,
        ProximityTerminal.Constructor(Vector3(3791.051f, 2697.523f, 161.3986f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        2097,
        Terminal.Constructor(Vector3(3791.051f, 2697.523f, 161.3986f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2102,
        ProximityTerminal.Constructor(Vector3(3800.56f, 2713.12f, 159.4086f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        2103,
        Terminal.Constructor(Vector3(3800.56f, 2713.12f, 159.4086f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2421,
        ProximityTerminal.Constructor(Vector3(3663.835f, 2743.308f, 150.8166f), repair_silo),
        owning_building_guid = 35
      )
      LocalObject(
        2422,
        Terminal.Constructor(Vector3(3663.835f, 2743.308f, 150.8166f), ground_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2433,
        ProximityTerminal.Constructor(Vector3(3758.158f, 2648.492f, 150.8166f), repair_silo),
        owning_building_guid = 35
      )
      LocalObject(
        2434,
        Terminal.Constructor(Vector3(3758.158f, 2648.492f, 150.8166f), ground_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1704,
        FacilityTurret.Constructor(Vector3(3645.168f, 2645.27f, 159.4686f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1704, 5044)
      LocalObject(
        1705,
        FacilityTurret.Constructor(Vector3(3654.609f, 2780.166f, 159.4686f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1705, 5045)
      LocalObject(
        1711,
        FacilityTurret.Constructor(Vector3(3779.881f, 2634.659f, 159.4686f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1711, 5046)
      LocalObject(
        1714,
        FacilityTurret.Constructor(Vector3(3825.895f, 2674.695f, 159.4686f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1714, 5047)
      LocalObject(
        1715,
        FacilityTurret.Constructor(Vector3(3831.271f, 2767.82f, 159.4686f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1715, 5048)
      LocalObject(
        967,
        ImplantTerminalMech.Constructor(Vector3(3789.558f, 2688.864f, 140.5436f)),
        owning_building_guid = 35
      )
      LocalObject(
        959,
        Terminal.Constructor(Vector3(3789.576f, 2688.862f, 140.5436f), implant_terminal_interface),
        owning_building_guid = 35
      )
      TerminalToInterface(967, 959)
      LocalObject(
        968,
        ImplantTerminalMech.Constructor(Vector3(3804.877f, 2687.804f, 140.5436f)),
        owning_building_guid = 35
      )
      LocalObject(
        960,
        Terminal.Constructor(Vector3(3804.859f, 2687.806f, 140.5436f), implant_terminal_interface),
        owning_building_guid = 35
      )
      TerminalToInterface(968, 960)
      LocalObject(
        2255,
        Painbox.Constructor(Vector3(3725.991f, 2697.792f, 165.0954f), painbox),
        owning_building_guid = 35
      )
      LocalObject(
        2269,
        Painbox.Constructor(Vector3(3752.664f, 2686.745f, 145.1365f), painbox_continuous),
        owning_building_guid = 35
      )
      LocalObject(
        2283,
        Painbox.Constructor(Vector3(3740.519f, 2698.191f, 165.3005f), painbox_door_radius),
        owning_building_guid = 35
      )
      LocalObject(
        2303,
        Painbox.Constructor(Vector3(3743.063f, 2691.64f, 143.4225f), painbox_door_radius_continuous),
        owning_building_guid = 35
      )
      LocalObject(
        2304,
        Painbox.Constructor(Vector3(3763.059f, 2672.857f, 144.6075f), painbox_door_radius_continuous),
        owning_building_guid = 35
      )
      LocalObject(
        2305,
        Painbox.Constructor(Vector3(3770.895f, 2687.713f, 142.7808f), painbox_door_radius_continuous),
        owning_building_guid = 35
      )
      LocalObject(300, Generator.Constructor(Vector3(3722.222f, 2699.628f, 159.7726f)), owning_building_guid = 35)
      LocalObject(
        286,
        Terminal.Constructor(Vector3(3730.397f, 2699.103f, 161.0666f), gen_control),
        owning_building_guid = 35
      )
    }

    Building6()

    def Building6(): Unit = { // Name: Hiro Type: cryo_facility GUID: 38, MapID: 6
      LocalBuilding(
        "Hiro",
        38,
        6,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4570f, 5696f, 190.0754f),
            Vector3(0f, 0f, 360f),
            cryo_facility
          )
        )
      )
      LocalObject(
        218,
        CaptureTerminal.Constructor(Vector3(4541.911f, 5755.266f, 180.0754f), capture_terminal),
        owning_building_guid = 38
      )
      LocalObject(477, Door.Constructor(Vector3(4511.023f, 5700.5f, 191.6264f)), owning_building_guid = 38)
      LocalObject(478, Door.Constructor(Vector3(4511.023f, 5718.693f, 199.5904f)), owning_building_guid = 38)
      LocalObject(479, Door.Constructor(Vector3(4527.674f, 5763.803f, 191.6264f)), owning_building_guid = 38)
      LocalObject(480, Door.Constructor(Vector3(4545.867f, 5763.803f, 199.5904f)), owning_building_guid = 38)
      LocalObject(481, Door.Constructor(Vector3(4554f, 5716f, 201.5964f)), owning_building_guid = 38)
      LocalObject(484, Door.Constructor(Vector3(4574f, 5699.137f, 201.5964f)), owning_building_guid = 38)
      LocalObject(485, Door.Constructor(Vector3(4575.307f, 5613.073f, 199.5904f)), owning_building_guid = 38)
      LocalObject(488, Door.Constructor(Vector3(4593.5f, 5613.073f, 191.6264f)), owning_building_guid = 38)
      LocalObject(489, Door.Constructor(Vector3(4602f, 5604f, 191.5964f)), owning_building_guid = 38)
      LocalObject(491, Door.Constructor(Vector3(4620.927f, 5645.307f, 199.5904f)), owning_building_guid = 38)
      LocalObject(492, Door.Constructor(Vector3(4620.927f, 5663.5f, 191.6264f)), owning_building_guid = 38)
      LocalObject(777, Door.Constructor(Vector3(4522f, 5684f, 181.5964f)), owning_building_guid = 38)
      LocalObject(778, Door.Constructor(Vector3(4530f, 5636f, 184.0964f)), owning_building_guid = 38)
      LocalObject(779, Door.Constructor(Vector3(4534f, 5696f, 181.5964f)), owning_building_guid = 38)
      LocalObject(780, Door.Constructor(Vector3(4534f, 5736f, 181.5964f)), owning_building_guid = 38)
      LocalObject(781, Door.Constructor(Vector3(4538f, 5716f, 181.5964f)), owning_building_guid = 38)
      LocalObject(782, Door.Constructor(Vector3(4538f, 5764f, 181.5964f)), owning_building_guid = 38)
      LocalObject(783, Door.Constructor(Vector3(4546f, 5764f, 181.5964f)), owning_building_guid = 38)
      LocalObject(784, Door.Constructor(Vector3(4550f, 5640f, 184.0964f)), owning_building_guid = 38)
      LocalObject(785, Door.Constructor(Vector3(4550f, 5664f, 181.5964f)), owning_building_guid = 38)
      LocalObject(786, Door.Constructor(Vector3(4554f, 5716f, 181.5964f)), owning_building_guid = 38)
      LocalObject(787, Door.Constructor(Vector3(4554f, 5716f, 191.5964f)), owning_building_guid = 38)
      LocalObject(788, Door.Constructor(Vector3(4554f, 5764f, 181.5964f)), owning_building_guid = 38)
      LocalObject(789, Door.Constructor(Vector3(4558f, 5608f, 184.0964f)), owning_building_guid = 38)
      LocalObject(790, Door.Constructor(Vector3(4558f, 5696f, 181.5964f)), owning_building_guid = 38)
      LocalObject(791, Door.Constructor(Vector3(4558f, 5736f, 181.5964f)), owning_building_guid = 38)
      LocalObject(792, Door.Constructor(Vector3(4570f, 5668f, 181.5964f)), owning_building_guid = 38)
      LocalObject(793, Door.Constructor(Vector3(4570f, 5692f, 181.5964f)), owning_building_guid = 38)
      LocalObject(794, Door.Constructor(Vector3(4570f, 5748f, 181.5964f)), owning_building_guid = 38)
      LocalObject(795, Door.Constructor(Vector3(4574f, 5688f, 201.5964f)), owning_building_guid = 38)
      LocalObject(797, Door.Constructor(Vector3(4582f, 5696f, 181.5964f)), owning_building_guid = 38)
      LocalObject(798, Door.Constructor(Vector3(4582f, 5720f, 181.5964f)), owning_building_guid = 38)
      LocalObject(805, Door.Constructor(Vector3(4594f, 5700f, 174.0964f)), owning_building_guid = 38)
      LocalObject(806, Door.Constructor(Vector3(4594f, 5716f, 181.5964f)), owning_building_guid = 38)
      LocalObject(944, Door.Constructor(Vector3(4573.992f, 5721.083f, 192.3584f)), owning_building_guid = 38)
      LocalObject(953, Door.Constructor(Vector3(4566f, 5704f, 191.5944f)), owning_building_guid = 38)
      LocalObject(954, Door.Constructor(Vector3(4582f, 5696f, 191.5964f)), owning_building_guid = 38)
      LocalObject(2783, Door.Constructor(Vector3(4574.673f, 5701.733f, 181.9294f)), owning_building_guid = 38)
      LocalObject(2784, Door.Constructor(Vector3(4574.673f, 5709.026f, 181.9294f)), owning_building_guid = 38)
      LocalObject(2785, Door.Constructor(Vector3(4574.673f, 5716.315f, 181.9294f)), owning_building_guid = 38)
      LocalObject(
        996,
        IFFLock.Constructor(Vector3(4570.77f, 5723.822f, 191.5574f), Vector3(0, 0, 0)),
        owning_building_guid = 38,
        door_guid = 944
      )
      LocalObject(
        1131,
        IFFLock.Constructor(Vector3(4537.06f, 5762.428f, 181.4114f), Vector3(0, 0, 270)),
        owning_building_guid = 38,
        door_guid = 782
      )
      LocalObject(
        1132,
        IFFLock.Constructor(Vector3(4546.813f, 5765.572f, 181.4114f), Vector3(0, 0, 90)),
        owning_building_guid = 38,
        door_guid = 783
      )
      LocalObject(
        1133,
        IFFLock.Constructor(Vector3(4554.814f, 5718.043f, 201.5274f), Vector3(0, 0, 90)),
        owning_building_guid = 38,
        door_guid = 481
      )
      LocalObject(
        1134,
        IFFLock.Constructor(Vector3(4571.954f, 5699.958f, 201.5274f), Vector3(0, 0, 0)),
        owning_building_guid = 38,
        door_guid = 484
      )
      LocalObject(
        1136,
        IFFLock.Constructor(Vector3(4580.428f, 5720.81f, 181.4114f), Vector3(0, 0, 0)),
        owning_building_guid = 38,
        door_guid = 798
      )
      LocalObject(
        1137,
        IFFLock.Constructor(Vector3(4583.572f, 5695.19f, 181.4114f), Vector3(0, 0, 180)),
        owning_building_guid = 38,
        door_guid = 797
      )
      LocalObject(
        1140,
        IFFLock.Constructor(Vector3(4593.19f, 5698.428f, 173.9114f), Vector3(0, 0, 270)),
        owning_building_guid = 38,
        door_guid = 805
      )
      LocalObject(
        1141,
        IFFLock.Constructor(Vector3(4602.814f, 5606.042f, 191.5274f), Vector3(0, 0, 90)),
        owning_building_guid = 38,
        door_guid = 489
      )
      LocalObject(1441, Locker.Constructor(Vector3(4585.563f, 5698.141f, 180.3364f)), owning_building_guid = 38)
      LocalObject(1442, Locker.Constructor(Vector3(4586.727f, 5698.141f, 180.3364f)), owning_building_guid = 38)
      LocalObject(1443, Locker.Constructor(Vector3(4587.874f, 5698.141f, 180.3364f)), owning_building_guid = 38)
      LocalObject(1444, Locker.Constructor(Vector3(4589.023f, 5698.141f, 180.3364f)), owning_building_guid = 38)
      LocalObject(1445, Locker.Constructor(Vector3(4589.997f, 5745.496f, 180.0704f)), owning_building_guid = 38)
      LocalObject(1446, Locker.Constructor(Vector3(4589.997f, 5746.752f, 180.0704f)), owning_building_guid = 38)
      LocalObject(1447, Locker.Constructor(Vector3(4589.997f, 5748.013f, 180.0704f)), owning_building_guid = 38)
      LocalObject(1448, Locker.Constructor(Vector3(4589.997f, 5749.275f, 180.0704f)), owning_building_guid = 38)
      LocalObject(1449, Locker.Constructor(Vector3(4589.997f, 5750.527f, 180.0704f)), owning_building_guid = 38)
      LocalObject(1450, Locker.Constructor(Vector3(4602.817f, 5698.36f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1451, Locker.Constructor(Vector3(4602.814f, 5718.361f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1452, Locker.Constructor(Vector3(4603.873f, 5698.36f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1453, Locker.Constructor(Vector3(4603.868f, 5718.361f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1454, Locker.Constructor(Vector3(4604.928f, 5698.36f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1455, Locker.Constructor(Vector3(4604.929f, 5718.361f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1456, Locker.Constructor(Vector3(4605.983f, 5698.36f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1457, Locker.Constructor(Vector3(4605.984f, 5718.361f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1458, Locker.Constructor(Vector3(4607.043f, 5698.36f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1459, Locker.Constructor(Vector3(4607.039f, 5718.361f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1460, Locker.Constructor(Vector3(4608.098f, 5698.36f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1461, Locker.Constructor(Vector3(4608.095f, 5718.361f, 179.9834f)), owning_building_guid = 38)
      LocalObject(1640, Locker.Constructor(Vector3(4588.26f, 5683.787f, 190.0754f)), owning_building_guid = 38)
      LocalObject(1641, Locker.Constructor(Vector3(4588.26f, 5684.821f, 190.0754f)), owning_building_guid = 38)
      LocalObject(1642, Locker.Constructor(Vector3(4588.26f, 5687.338f, 189.8464f)), owning_building_guid = 38)
      LocalObject(1643, Locker.Constructor(Vector3(4588.26f, 5688.372f, 189.8464f)), owning_building_guid = 38)
      LocalObject(1644, Locker.Constructor(Vector3(4588.26f, 5689.426f, 189.8464f)), owning_building_guid = 38)
      LocalObject(1645, Locker.Constructor(Vector3(4588.26f, 5690.46f, 189.8464f)), owning_building_guid = 38)
      LocalObject(1646, Locker.Constructor(Vector3(4588.26f, 5692.982f, 190.0754f)), owning_building_guid = 38)
      LocalObject(1647, Locker.Constructor(Vector3(4588.26f, 5694.016f, 190.0754f)), owning_building_guid = 38)
      LocalObject(
        233,
        Terminal.Constructor(Vector3(4572.276f, 5744.25f, 180.0654f), cert_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        234,
        Terminal.Constructor(Vector3(4572.276f, 5751.575f, 180.0654f), cert_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        235,
        Terminal.Constructor(Vector3(4573.724f, 5742.802f, 180.0654f), cert_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        236,
        Terminal.Constructor(Vector3(4573.724f, 5753.023f, 180.0654f), cert_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        237,
        Terminal.Constructor(Vector3(4586.424f, 5742.802f, 180.0654f), cert_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        238,
        Terminal.Constructor(Vector3(4586.424f, 5753.023f, 180.0654f), cert_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        239,
        Terminal.Constructor(Vector3(4587.872f, 5744.25f, 180.0654f), cert_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        240,
        Terminal.Constructor(Vector3(4587.872f, 5751.575f, 180.0654f), cert_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1914,
        Terminal.Constructor(Vector3(4559.972f, 5705.526f, 191.3704f), order_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1915,
        Terminal.Constructor(Vector3(4588.654f, 5703.408f, 181.6654f), order_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1916,
        Terminal.Constructor(Vector3(4588.654f, 5707.139f, 181.6654f), order_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1917,
        Terminal.Constructor(Vector3(4588.654f, 5710.928f, 181.6654f), order_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2688,
        Terminal.Constructor(Vector3(4530f, 5695.407f, 181.6884f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2689,
        Terminal.Constructor(Vector3(4553.91f, 5639.41f, 184.1884f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2690,
        Terminal.Constructor(Vector3(4569.407f, 5760f, 181.6884f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2691,
        Terminal.Constructor(Vector3(4571.905f, 5682.359f, 191.6544f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2692,
        Terminal.Constructor(Vector3(4574.971f, 5699.243f, 182.2094f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2693,
        Terminal.Constructor(Vector3(4574.967f, 5706.535f, 182.2094f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2694,
        Terminal.Constructor(Vector3(4574.97f, 5713.823f, 182.2094f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2916,
        Terminal.Constructor(Vector3(4607.628f, 5632.814f, 192.3804f), vehicle_terminal_combined),
        owning_building_guid = 38
      )
      LocalObject(
        1809,
        VehicleSpawnPad.Constructor(Vector3(4593.989f, 5632.724f, 188.2224f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 38,
        terminal_guid = 2916
      )
      LocalObject(2524, ResourceSilo.Constructor(Vector3(4509.733f, 5746.852f, 197.0924f)), owning_building_guid = 38)
      LocalObject(
        2578,
        SpawnTube.Constructor(Vector3(4574.233f, 5700.683f, 180.0754f), Vector3(0, 0, 0)),
        owning_building_guid = 38
      )
      LocalObject(
        2579,
        SpawnTube.Constructor(Vector3(4574.233f, 5707.974f, 180.0754f), Vector3(0, 0, 0)),
        owning_building_guid = 38
      )
      LocalObject(
        2580,
        SpawnTube.Constructor(Vector3(4574.233f, 5715.262f, 180.0754f), Vector3(0, 0, 0)),
        owning_building_guid = 38
      )
      LocalObject(
        154,
        ProximityTerminal.Constructor(Vector3(4571.983f, 5688.892f, 189.8854f), adv_med_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1829,
        ProximityTerminal.Constructor(Vector3(4597.642f, 5699.952f, 180.0754f), medical_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2144,
        ProximityTerminal.Constructor(Vector3(4551.883f, 5634.061f, 200.3684f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        2145,
        Terminal.Constructor(Vector3(4551.883f, 5634.061f, 200.3684f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2147,
        ProximityTerminal.Constructor(Vector3(4555.101f, 5749.651f, 198.4174f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        2148,
        Terminal.Constructor(Vector3(4555.101f, 5749.651f, 198.4174f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2150,
        ProximityTerminal.Constructor(Vector3(4568.198f, 5626.777f, 198.4274f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        2151,
        Terminal.Constructor(Vector3(4568.198f, 5626.777f, 198.4274f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2153,
        ProximityTerminal.Constructor(Vector3(4571.323f, 5741.253f, 200.4074f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        2154,
        Terminal.Constructor(Vector3(4571.323f, 5741.253f, 200.4074f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2461,
        ProximityTerminal.Constructor(Vector3(4534.524f, 5611.154f, 189.8254f), repair_silo),
        owning_building_guid = 38
      )
      LocalObject(
        2462,
        Terminal.Constructor(Vector3(4534.524f, 5611.154f, 189.8254f), ground_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2469,
        ProximityTerminal.Constructor(Vector3(4622.53f, 5711.861f, 189.8254f), repair_silo),
        owning_building_guid = 38
      )
      LocalObject(
        2470,
        Terminal.Constructor(Vector3(4622.53f, 5711.861f, 189.8254f), ground_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1735,
        FacilityTurret.Constructor(Vector3(4498.392f, 5776.472f, 198.4774f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1735, 5049)
      LocalObject(
        1736,
        FacilityTurret.Constructor(Vector3(4498.4f, 5599.379f, 198.4774f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1736, 5050)
      LocalObject(
        1742,
        FacilityTurret.Constructor(Vector3(4591.665f, 5777.605f, 198.4774f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1742, 5051)
      LocalObject(
        1743,
        FacilityTurret.Constructor(Vector3(4633.626f, 5599.371f, 198.4774f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1743, 5052)
      LocalObject(
        1744,
        FacilityTurret.Constructor(Vector3(4634.813f, 5734.496f, 198.4774f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1744, 5053)
      LocalObject(
        969,
        ImplantTerminalMech.Constructor(Vector3(4580.066f, 5740.368f, 179.5524f)),
        owning_building_guid = 38
      )
      LocalObject(
        961,
        Terminal.Constructor(Vector3(4580.066f, 5740.386f, 179.5524f), implant_terminal_interface),
        owning_building_guid = 38
      )
      TerminalToInterface(969, 961)
      LocalObject(
        970,
        ImplantTerminalMech.Constructor(Vector3(4580.054f, 5755.724f, 179.5524f)),
        owning_building_guid = 38
      )
      LocalObject(
        962,
        Terminal.Constructor(Vector3(4580.054f, 5755.706f, 179.5524f), implant_terminal_interface),
        owning_building_guid = 38
      )
      TerminalToInterface(970, 962)
      LocalObject(
        2259,
        Painbox.Constructor(Vector3(4575.593f, 5676.334f, 204.1042f), painbox),
        owning_building_guid = 38
      )
      LocalObject(
        2273,
        Painbox.Constructor(Vector3(4584.753f, 5703.712f, 184.1453f), painbox_continuous),
        owning_building_guid = 38
      )
      LocalObject(
        2287,
        Painbox.Constructor(Vector3(4574.182f, 5690.798f, 204.3093f), painbox_door_radius),
        owning_building_guid = 38
      )
      LocalObject(
        2315,
        Painbox.Constructor(Vector3(4580.54f, 5693.793f, 182.4313f), painbox_door_radius_continuous),
        owning_building_guid = 38
      )
      LocalObject(
        2316,
        Painbox.Constructor(Vector3(4582.516f, 5721.831f, 181.7896f), painbox_door_radius_continuous),
        owning_building_guid = 38
      )
      LocalObject(
        2317,
        Painbox.Constructor(Vector3(4597.882f, 5715.05f, 183.6163f), painbox_door_radius_continuous),
        owning_building_guid = 38
      )
      LocalObject(304, Generator.Constructor(Vector3(4574.025f, 5672.445f, 198.7814f)), owning_building_guid = 38)
      LocalObject(
        290,
        Terminal.Constructor(Vector3(4573.978f, 5680.637f, 200.0754f), gen_control),
        owning_building_guid = 38
      )
    }

    Building4()

    def Building4(): Unit = { // Name: Akua Type: cryo_facility GUID: 41, MapID: 4
      LocalBuilding(
        "Akua",
        41,
        4,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5274f, 3958f, 346.1272f),
            Vector3(0f, 0f, 50f),
            cryo_facility
          )
        )
      )
      LocalObject(
        221,
        CaptureTerminal.Constructor(Vector3(5210.544f, 3974.578f, 336.1272f), capture_terminal),
        owning_building_guid = 41
      )
      LocalObject(537, Door.Constructor(Vector3(5194.853f, 3969.159f, 347.6782f)), owning_building_guid = 41)
      LocalObject(538, Door.Constructor(Vector3(5206.547f, 3983.096f, 355.6422f)), owning_building_guid = 41)
      LocalObject(541, Door.Constructor(Vector3(5218.707f, 3927.408f, 355.6422f)), owning_building_guid = 41)
      LocalObject(544, Door.Constructor(Vector3(5232.643f, 3915.714f, 347.6782f)), owning_building_guid = 41)
      LocalObject(546, Door.Constructor(Vector3(5248.395f, 3958.599f, 357.6482f)), owning_building_guid = 41)
      LocalObject(550, Door.Constructor(Vector3(5274.168f, 3963.081f, 357.6482f)), owning_building_guid = 41)
      LocalObject(560, Door.Constructor(Vector3(5331.632f, 3976.122f, 347.6782f)), owning_building_guid = 41)
      LocalObject(562, Door.Constructor(Vector3(5340.937f, 3908.761f, 355.6422f)), owning_building_guid = 41)
      LocalObject(563, Door.Constructor(Vector3(5345.568f, 3964.427f, 355.6422f)), owning_building_guid = 41)
      LocalObject(565, Door.Constructor(Vector3(5352.631f, 3922.698f, 347.6782f)), owning_building_guid = 41)
      LocalObject(566, Door.Constructor(Vector3(5365.045f, 3923.377f, 347.6482f)), owning_building_guid = 41)
      LocalObject(849, Door.Constructor(Vector3(5201.34f, 3977.196f, 337.6482f)), owning_building_guid = 41)
      LocalObject(850, Door.Constructor(Vector3(5206.482f, 3983.324f, 337.6482f)), owning_building_guid = 41)
      LocalObject(851, Door.Constructor(Vector3(5211.625f, 3989.453f, 337.6482f)), owning_building_guid = 41)
      LocalObject(852, Door.Constructor(Vector3(5220.218f, 3956.134f, 337.6482f)), owning_building_guid = 41)
      LocalObject(856, Door.Constructor(Vector3(5234.166f, 3991.425f, 337.6482f)), owning_building_guid = 41)
      LocalObject(857, Door.Constructor(Vector3(5235.645f, 3974.519f, 337.6482f)), owning_building_guid = 41)
      LocalObject(858, Door.Constructor(Vector3(5238.11f, 3946.342f, 337.6482f)), owning_building_guid = 41)
      LocalObject(861, Door.Constructor(Vector3(5248.395f, 3958.599f, 337.6482f)), owning_building_guid = 41)
      LocalObject(862, Door.Constructor(Vector3(5248.395f, 3958.599f, 347.6482f)), owning_building_guid = 41)
      LocalObject(864, Door.Constructor(Vector3(5250.86f, 3930.422f, 337.6482f)), owning_building_guid = 41)
      LocalObject(865, Door.Constructor(Vector3(5252.339f, 3913.516f, 337.6482f)), owning_building_guid = 41)
      LocalObject(873, Door.Constructor(Vector3(5263.329f, 3982.619f, 337.6482f)), owning_building_guid = 41)
      LocalObject(875, Door.Constructor(Vector3(5266.287f, 3948.807f, 337.6482f)), owning_building_guid = 41)
      LocalObject(879, Door.Constructor(Vector3(5274.106f, 3989.241f, 337.6482f)), owning_building_guid = 41)
      LocalObject(881, Door.Constructor(Vector3(5277.064f, 3955.429f, 337.6482f)), owning_building_guid = 41)
      LocalObject(883, Door.Constructor(Vector3(5281.713f, 3967.193f, 337.6482f)), owning_building_guid = 41)
      LocalObject(884, Door.Constructor(Vector3(5282.7f, 3955.922f, 357.6482f)), owning_building_guid = 41)
      LocalObject(886, Door.Constructor(Vector3(5285.658f, 3922.11f, 337.6482f)), owning_building_guid = 41)
      LocalObject(887, Door.Constructor(Vector3(5286.363f, 3978.956f, 330.1482f)), owning_building_guid = 41)
      LocalObject(889, Door.Constructor(Vector3(5294.251f, 3888.791f, 340.1482f)), owning_building_guid = 41)
      LocalObject(890, Door.Constructor(Vector3(5295.449f, 3940.002f, 337.6482f)), owning_building_guid = 41)
      LocalObject(891, Door.Constructor(Vector3(5304.043f, 3906.683f, 340.1482f)), owning_building_guid = 41)
      LocalObject(892, Door.Constructor(Vector3(5333.698f, 3892.242f, 340.1482f)), owning_building_guid = 41)
      LocalObject(947, Door.Constructor(Vector3(5257.351f, 3977.181f, 348.4102f)), owning_building_guid = 41)
      LocalObject(955, Door.Constructor(Vector3(5265.3f, 3960.078f, 347.6462f)), owning_building_guid = 41)
      LocalObject(956, Door.Constructor(Vector3(5281.713f, 3967.193f, 347.6482f)), owning_building_guid = 41)
      LocalObject(2803, Door.Constructor(Vector3(5261.441f, 3974.638f, 337.9812f)), owning_building_guid = 41)
      LocalObject(2804, Door.Constructor(Vector3(5267.025f, 3969.953f, 337.9812f)), owning_building_guid = 41)
      LocalObject(2805, Door.Constructor(Vector3(5272.612f, 3965.265f, 337.9812f)), owning_building_guid = 41)
      LocalObject(
        999,
        IFFLock.Constructor(Vector3(5253.182f, 3976.473f, 347.6092f), Vector3(0, 0, 310)),
        owning_building_guid = 41,
        door_guid = 947
      )
      LocalObject(
        1180,
        IFFLock.Constructor(Vector3(5201.94f, 3975.466f, 337.4632f), Vector3(0, 0, 220)),
        owning_building_guid = 41,
        door_guid = 849
      )
      LocalObject(
        1181,
        IFFLock.Constructor(Vector3(5205.8f, 3984.958f, 337.4632f), Vector3(0, 0, 40)),
        owning_building_guid = 41,
        door_guid = 850
      )
      LocalObject(
        1183,
        IFFLock.Constructor(Vector3(5247.353f, 3960.536f, 357.5792f), Vector3(0, 0, 40)),
        owning_building_guid = 41,
        door_guid = 546
      )
      LocalObject(
        1188,
        IFFLock.Constructor(Vector3(5261.697f, 3981.936f, 337.4632f), Vector3(0, 0, 310)),
        owning_building_guid = 41,
        door_guid = 873
      )
      LocalObject(
        1191,
        IFFLock.Constructor(Vector3(5272.224f, 3962.041f, 357.5792f), Vector3(0, 0, 310)),
        owning_building_guid = 41,
        door_guid = 550
      )
      LocalObject(
        1196,
        IFFLock.Constructor(Vector3(5283.344f, 3967.876f, 337.4632f), Vector3(0, 0, 130)),
        owning_building_guid = 41,
        door_guid = 883
      )
      LocalObject(
        1200,
        IFFLock.Constructor(Vector3(5287.046f, 3977.325f, 329.9632f), Vector3(0, 0, 220)),
        owning_building_guid = 41,
        door_guid = 887
      )
      LocalObject(
        1201,
        IFFLock.Constructor(Vector3(5364.004f, 3925.313f, 347.5792f), Vector3(0, 0, 40)),
        owning_building_guid = 41,
        door_guid = 566
      )
      LocalObject(1518, Locker.Constructor(Vector3(5245.083f, 4008.368f, 336.1222f)), owning_building_guid = 41)
      LocalObject(1519, Locker.Constructor(Vector3(5246.043f, 4007.563f, 336.1222f)), owning_building_guid = 41)
      LocalObject(1520, Locker.Constructor(Vector3(5247.01f, 4006.752f, 336.1222f)), owning_building_guid = 41)
      LocalObject(1521, Locker.Constructor(Vector3(5247.976f, 4005.941f, 336.1222f)), owning_building_guid = 41)
      LocalObject(1522, Locker.Constructor(Vector3(5248.938f, 4005.134f, 336.1222f)), owning_building_guid = 41)
      LocalObject(1533, Locker.Constructor(Vector3(5277.963f, 3997.51f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1535, Locker.Constructor(Vector3(5278.641f, 3998.318f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1536, Locker.Constructor(Vector3(5279.322f, 3999.131f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1538, Locker.Constructor(Vector3(5280f, 3999.939f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1539, Locker.Constructor(Vector3(5280.679f, 4000.747f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1540, Locker.Constructor(Vector3(5281.357f, 4001.556f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1542, Locker.Constructor(Vector3(5282.364f, 3971.298f, 336.3882f)), owning_building_guid = 41)
      LocalObject(1543, Locker.Constructor(Vector3(5283.112f, 3972.19f, 336.3882f)), owning_building_guid = 41)
      LocalObject(1545, Locker.Constructor(Vector3(5283.849f, 3973.069f, 336.3882f)), owning_building_guid = 41)
      LocalObject(1546, Locker.Constructor(Vector3(5284.588f, 3973.949f, 336.3882f)), owning_building_guid = 41)
      LocalObject(1553, Locker.Constructor(Vector3(5293.287f, 3984.656f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1554, Locker.Constructor(Vector3(5293.965f, 3985.465f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1555, Locker.Constructor(Vector3(5294.644f, 3986.273f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1556, Locker.Constructor(Vector3(5295.322f, 3987.082f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1557, Locker.Constructor(Vector3(5296.003f, 3987.894f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1558, Locker.Constructor(Vector3(5296.681f, 3988.702f, 336.0352f)), owning_building_guid = 41)
      LocalObject(1648, Locker.Constructor(Vector3(5287.257f, 3970.713f, 346.1272f)), owning_building_guid = 41)
      LocalObject(1649, Locker.Constructor(Vector3(5288.049f, 3970.048f, 346.1272f)), owning_building_guid = 41)
      LocalObject(1650, Locker.Constructor(Vector3(5289.981f, 3968.427f, 345.8982f)), owning_building_guid = 41)
      LocalObject(1651, Locker.Constructor(Vector3(5290.773f, 3967.762f, 345.8982f)), owning_building_guid = 41)
      LocalObject(1652, Locker.Constructor(Vector3(5291.581f, 3967.085f, 345.8982f)), owning_building_guid = 41)
      LocalObject(1653, Locker.Constructor(Vector3(5292.373f, 3966.42f, 345.8982f)), owning_building_guid = 41)
      LocalObject(1654, Locker.Constructor(Vector3(5294.301f, 3964.802f, 346.1272f)), owning_building_guid = 41)
      LocalObject(1655, Locker.Constructor(Vector3(5295.093f, 3964.138f, 346.1272f)), owning_building_guid = 41)
      LocalObject(
        241,
        Terminal.Constructor(Vector3(5232.711f, 3997.506f, 336.1172f), cert_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        242,
        Terminal.Constructor(Vector3(5232.89f, 3995.467f, 336.1172f), cert_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        243,
        Terminal.Constructor(Vector3(5238.501f, 3990.758f, 336.1172f), cert_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        244,
        Terminal.Constructor(Vector3(5240.542f, 3990.937f, 336.1172f), cert_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        245,
        Terminal.Constructor(Vector3(5240.875f, 4007.235f, 336.1172f), cert_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        246,
        Terminal.Constructor(Vector3(5242.915f, 4007.414f, 336.1172f), cert_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        247,
        Terminal.Constructor(Vector3(5248.526f, 4002.705f, 336.1172f), cert_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        248,
        Terminal.Constructor(Vector3(5248.705f, 4000.665f, 336.1172f), cert_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        1946,
        Terminal.Constructor(Vector3(5260.257f, 3956.441f, 347.4222f), order_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        1950,
        Terminal.Constructor(Vector3(5274.555f, 3981.885f, 337.7172f), order_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        1952,
        Terminal.Constructor(Vector3(5277.458f, 3979.45f, 337.7172f), order_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        1953,
        Terminal.Constructor(Vector3(5280.316f, 3977.052f, 337.7172f), order_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2708,
        Terminal.Constructor(Vector3(5224.592f, 3998.684f, 337.7402f), spawn_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2710,
        Terminal.Constructor(Vector3(5248.743f, 3926.977f, 337.7402f), spawn_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2714,
        Terminal.Constructor(Vector3(5263.542f, 3973.264f, 338.2612f), spawn_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2716,
        Terminal.Constructor(Vector3(5269.123f, 3968.577f, 338.2612f), spawn_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2717,
        Terminal.Constructor(Vector3(5274.711f, 3963.893f, 338.2612f), spawn_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2718,
        Terminal.Constructor(Vector3(5285.674f, 3950.691f, 347.7062f), spawn_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2719,
        Terminal.Constructor(Vector3(5307.008f, 3909.299f, 340.2402f), spawn_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2919,
        Terminal.Constructor(Vector3(5346.59f, 3946.209f, 348.4322f), vehicle_terminal_combined),
        owning_building_guid = 41
      )
      LocalObject(
        1813,
        VehicleSpawnPad.Constructor(Vector3(5337.892f, 3935.704f, 344.2742f), mb_pad_creation, Vector3(0, 0, 220)),
        owning_building_guid = 41,
        terminal_guid = 2919
      )
      LocalObject(2527, ResourceSilo.Constructor(Vector3(5196.306f, 3944.52f, 353.1442f)), owning_building_guid = 41)
      LocalObject(
        2598,
        SpawnTube.Constructor(Vector3(5261.965f, 3973.624f, 336.1272f), Vector3(0, 0, 310)),
        owning_building_guid = 41
      )
      LocalObject(
        2599,
        SpawnTube.Constructor(Vector3(5267.548f, 3968.939f, 336.1272f), Vector3(0, 0, 310)),
        owning_building_guid = 41
      )
      LocalObject(
        2600,
        SpawnTube.Constructor(Vector3(5273.133f, 3964.253f, 336.1272f), Vector3(0, 0, 310)),
        owning_building_guid = 41
      )
      LocalObject(
        155,
        ProximityTerminal.Constructor(Vector3(5280.72f, 3954.95f, 345.9372f), adv_med_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        1836,
        ProximityTerminal.Constructor(Vector3(5288.741f, 3981.715f, 336.1272f), medical_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2165,
        ProximityTerminal.Constructor(Vector3(5223.324f, 3981.073f, 354.4692f), pad_landing_frame),
        owning_building_guid = 41
      )
      LocalObject(
        2166,
        Terminal.Constructor(Vector3(5223.324f, 3981.073f, 354.4692f), air_rearm_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2168,
        ProximityTerminal.Constructor(Vector3(5240.185f, 3988.102f, 356.4592f), pad_landing_frame),
        owning_building_guid = 41
      )
      LocalObject(
        2169,
        Terminal.Constructor(Vector3(5240.185f, 3988.102f, 356.4592f), air_rearm_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2177,
        ProximityTerminal.Constructor(Vector3(5309.803f, 3904.308f, 356.4202f), pad_landing_frame),
        owning_building_guid = 41
      )
      LocalObject(
        2178,
        Terminal.Constructor(Vector3(5309.803f, 3904.308f, 356.4202f), air_rearm_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2183,
        ProximityTerminal.Constructor(Vector3(5325.87f, 3912.124f, 354.4792f), pad_landing_frame),
        owning_building_guid = 41
      )
      LocalObject(
        2184,
        Terminal.Constructor(Vector3(5325.87f, 3912.124f, 354.4792f), air_rearm_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2489,
        ProximityTerminal.Constructor(Vector3(5295.615f, 4008.436f, 345.8772f), repair_silo),
        owning_building_guid = 41
      )
      LocalObject(
        2490,
        Terminal.Constructor(Vector3(5295.615f, 4008.436f, 345.8772f), ground_rearm_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        2493,
        ProximityTerminal.Constructor(Vector3(5316.193f, 3876.286f, 345.8772f), repair_silo),
        owning_building_guid = 41
      )
      LocalObject(
        2494,
        Terminal.Constructor(Vector3(5316.193f, 3876.286f, 345.8772f), ground_rearm_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        1761,
        FacilityTurret.Constructor(Vector3(5166.326f, 3954.872f, 354.5292f), manned_turret),
        owning_building_guid = 41
      )
      TurretToWeapon(1761, 5054)
      LocalObject(
        1762,
        FacilityTurret.Constructor(Vector3(5225.413f, 4027.051f, 354.5292f), manned_turret),
        owning_building_guid = 41
      )
      TurretToWeapon(1762, 5055)
      LocalObject(
        1766,
        FacilityTurret.Constructor(Vector3(5286.171f, 4032.394f, 354.5292f), manned_turret),
        owning_building_guid = 41
      )
      TurretToWeapon(1766, 5056)
      LocalObject(
        1769,
        FacilityTurret.Constructor(Vector3(5301.992f, 3841.044f, 354.5292f), manned_turret),
        owning_building_guid = 41
      )
      TurretToWeapon(1769, 5057)
      LocalObject(
        1773,
        FacilityTurret.Constructor(Vector3(5388.92f, 3944.628f, 354.5292f), manned_turret),
        owning_building_guid = 41
      )
      TurretToWeapon(1773, 5058)
      LocalObject(
        971,
        ImplantTerminalMech.Constructor(Vector3(5234.711f, 4004.092f, 335.6042f)),
        owning_building_guid = 41
      )
      LocalObject(
        963,
        Terminal.Constructor(Vector3(5234.725f, 4004.08f, 335.6042f), implant_terminal_interface),
        owning_building_guid = 41
      )
      TerminalToInterface(971, 963)
      LocalObject(
        972,
        ImplantTerminalMech.Constructor(Vector3(5246.482f, 3994.23f, 335.6042f)),
        owning_building_guid = 41
      )
      LocalObject(
        964,
        Terminal.Constructor(Vector3(5246.469f, 3994.242f, 335.6042f), implant_terminal_interface),
        owning_building_guid = 41
      )
      TerminalToInterface(972, 964)
      LocalObject(
        2263,
        Painbox.Constructor(Vector3(5292.661f, 3949.644f, 360.156f), painbox),
        owning_building_guid = 41
      )
      LocalObject(
        2277,
        Painbox.Constructor(Vector3(5277.575f, 3974.258f, 340.1971f), painbox_continuous),
        owning_building_guid = 41
      )
      LocalObject(
        2291,
        Painbox.Constructor(Vector3(5280.673f, 3957.86f, 360.3611f), painbox_door_radius),
        owning_building_guid = 41
      )
      LocalObject(
        2325,
        Painbox.Constructor(Vector3(5262.257f, 3984.191f, 337.8414f), painbox_door_radius_continuous),
        owning_building_guid = 41
      )
      LocalObject(
        2328,
        Painbox.Constructor(Vector3(5277.329f, 3991.604f, 339.6681f), painbox_door_radius_continuous),
        owning_building_guid = 41
      )
      LocalObject(
        2329,
        Painbox.Constructor(Vector3(5282.465f, 3964.656f, 338.4831f), painbox_door_radius_continuous),
        owning_building_guid = 41
      )
      LocalObject(308, Generator.Constructor(Vector3(5294.631f, 3945.942f, 354.8332f)), owning_building_guid = 41)
      LocalObject(
        294,
        Terminal.Constructor(Vector3(5288.326f, 3951.172f, 356.1272f), gen_control),
        owning_building_guid = 41
      )
    }

    Building7()

    def Building7(): Unit = { // Name: Iva Type: cryo_facility GUID: 44, MapID: 7
      LocalBuilding(
        "Iva",
        44,
        7,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(6452f, 5150f, 55.30717f),
            Vector3(0f, 0f, 91f),
            cryo_facility
          )
        )
      )
      LocalObject(
        224,
        CaptureTerminal.Constructor(Vector3(6393.233f, 5120.881f, 45.30717f), capture_terminal),
        owning_building_guid = 44
      )
      LocalObject(598, Door.Constructor(Vector3(6384.628f, 5124.688f, 64.82217f)), owning_building_guid = 44)
      LocalObject(599, Door.Constructor(Vector3(6384.946f, 5106.497f, 56.85817f)), owning_building_guid = 44)
      LocalObject(601, Door.Constructor(Vector3(6430.34f, 5090.636f, 64.82217f)), owning_building_guid = 44)
      LocalObject(602, Door.Constructor(Vector3(6432.282f, 5133.653f, 66.82817f)), owning_building_guid = 44)
      LocalObject(605, Door.Constructor(Vector3(6448.53f, 5090.954f, 56.85817f)), owning_building_guid = 44)
      LocalObject(606, Door.Constructor(Vector3(6448.793f, 5153.945f, 66.82817f)), owning_building_guid = 44)
      LocalObject(609, Door.Constructor(Vector3(6483.606f, 5201.486f, 56.85817f)), owning_building_guid = 44)
      LocalObject(611, Door.Constructor(Vector3(6501.796f, 5201.804f, 64.82217f)), owning_building_guid = 44)
      LocalObject(612, Door.Constructor(Vector3(6534.504f, 5174.944f, 56.85817f)), owning_building_guid = 44)
      LocalObject(613, Door.Constructor(Vector3(6534.822f, 5156.753f, 64.82217f)), owning_building_guid = 44)
      LocalObject(614, Door.Constructor(Vector3(6543.428f, 5183.601f, 56.82817f)), owning_building_guid = 44)
      LocalObject(914, Door.Constructor(Vector3(6384.29f, 5132.815f, 46.82817f)), owning_building_guid = 44)
      LocalObject(915, Door.Constructor(Vector3(6384.429f, 5124.817f, 46.82817f)), owning_building_guid = 44)
      LocalObject(916, Door.Constructor(Vector3(6384.569f, 5116.818f, 46.82817f)), owning_building_guid = 44)
      LocalObject(917, Door.Constructor(Vector3(6400.008f, 5149.092f, 46.82817f)), owning_building_guid = 44)
      LocalObject(918, Door.Constructor(Vector3(6412.215f, 5137.304f, 46.82817f)), owning_building_guid = 44)
      LocalObject(919, Door.Constructor(Vector3(6412.634f, 5113.308f, 46.82817f)), owning_building_guid = 44)
      LocalObject(920, Door.Constructor(Vector3(6427.794f, 5161.579f, 46.82817f)), owning_building_guid = 44)
      LocalObject(921, Door.Constructor(Vector3(6431.584f, 5173.647f, 46.82817f)), owning_building_guid = 44)
      LocalObject(922, Door.Constructor(Vector3(6432.282f, 5133.653f, 46.82817f)), owning_building_guid = 44)
      LocalObject(923, Door.Constructor(Vector3(6432.282f, 5133.653f, 56.82817f)), owning_building_guid = 44)
      LocalObject(924, Door.Constructor(Vector3(6432.562f, 5117.656f, 46.82817f)), owning_building_guid = 44)
      LocalObject(925, Door.Constructor(Vector3(6447.582f, 5173.927f, 39.32817f)), owning_building_guid = 44)
      LocalObject(926, Door.Constructor(Vector3(6451.791f, 5161.998f, 46.82817f)), owning_building_guid = 44)
      LocalObject(927, Door.Constructor(Vector3(6452.209f, 5138.002f, 46.82817f)), owning_building_guid = 44)
      LocalObject(928, Door.Constructor(Vector3(6452.628f, 5114.005f, 46.82817f)), owning_building_guid = 44)
      LocalObject(929, Door.Constructor(Vector3(6456f, 5150.07f, 46.82817f)), owning_building_guid = 44)
      LocalObject(930, Door.Constructor(Vector3(6459.929f, 5154.139f, 66.82817f)), owning_building_guid = 44)
      LocalObject(931, Door.Constructor(Vector3(6464.836f, 5102.217f, 46.82817f)), owning_building_guid = 44)
      LocalObject(932, Door.Constructor(Vector3(6479.996f, 5150.489f, 46.82817f)), owning_building_guid = 44)
      LocalObject(933, Door.Constructor(Vector3(6484.344f, 5130.562f, 46.82817f)), owning_building_guid = 44)
      LocalObject(934, Door.Constructor(Vector3(6508.34f, 5130.98f, 49.32817f)), owning_building_guid = 44)
      LocalObject(935, Door.Constructor(Vector3(6512.689f, 5111.053f, 49.32817f)), owning_building_guid = 44)
      LocalObject(936, Door.Constructor(Vector3(6540.196f, 5139.538f, 49.32817f)), owning_building_guid = 44)
      LocalObject(950, Door.Constructor(Vector3(6426.851f, 5153.554f, 57.59017f)), owning_building_guid = 44)
      LocalObject(957, Door.Constructor(Vector3(6444.071f, 5145.861f, 56.82617f)), owning_building_guid = 44)
      LocalObject(958, Door.Constructor(Vector3(6451.791f, 5161.998f, 56.82817f)), owning_building_guid = 44)
      LocalObject(2815, Door.Constructor(Vector3(6431.606f, 5154.318f, 47.16117f)), owning_building_guid = 44)
      LocalObject(2816, Door.Constructor(Vector3(6438.895f, 5154.445f, 47.16117f)), owning_building_guid = 44)
      LocalObject(2818, Door.Constructor(Vector3(6446.187f, 5154.572f, 47.16117f)), owning_building_guid = 44)
      LocalObject(
        1002,
        IFFLock.Constructor(Vector3(6424.169f, 5150.284f, 56.78917f), Vector3(0, 0, 269)),
        owning_building_guid = 44,
        door_guid = 950
      )
      LocalObject(
        1224,
        IFFLock.Constructor(Vector3(6382.843f, 5125.603f, 46.64317f), Vector3(0, 0, 359)),
        owning_building_guid = 44,
        door_guid = 915
      )
      LocalObject(
        1225,
        IFFLock.Constructor(Vector3(6386.157f, 5115.906f, 46.64317f), Vector3(0, 0, 179)),
        owning_building_guid = 44,
        door_guid = 916
      )
      LocalObject(
        1226,
        IFFLock.Constructor(Vector3(6427.012f, 5159.994f, 46.64317f), Vector3(0, 0, 269)),
        owning_building_guid = 44,
        door_guid = 920
      )
      LocalObject(
        1227,
        IFFLock.Constructor(Vector3(6430.226f, 5134.432f, 66.75917f), Vector3(0, 0, 359)),
        owning_building_guid = 44,
        door_guid = 602
      )
      LocalObject(
        1230,
        IFFLock.Constructor(Vector3(6448.008f, 5151.885f, 66.75917f), Vector3(0, 0, 269)),
        owning_building_guid = 44,
        door_guid = 606
      )
      LocalObject(
        1231,
        IFFLock.Constructor(Vector3(6449.167f, 5173.144f, 39.14317f), Vector3(0, 0, 179)),
        owning_building_guid = 44,
        door_guid = 925
      )
      LocalObject(
        1234,
        IFFLock.Constructor(Vector3(6452.573f, 5163.584f, 46.64317f), Vector3(0, 0, 89)),
        owning_building_guid = 44,
        door_guid = 926
      )
      LocalObject(
        1235,
        IFFLock.Constructor(Vector3(6541.372f, 5184.379f, 56.75917f), Vector3(0, 0, 359)),
        owning_building_guid = 44,
        door_guid = 614
      )
      LocalObject(1587, Locker.Constructor(Vector3(6397.132f, 5169.042f, 45.30217f)), owning_building_guid = 44)
      LocalObject(1588, Locker.Constructor(Vector3(6398.384f, 5169.064f, 45.30217f)), owning_building_guid = 44)
      LocalObject(1589, Locker.Constructor(Vector3(6399.646f, 5169.086f, 45.30217f)), owning_building_guid = 44)
      LocalObject(1590, Locker.Constructor(Vector3(6400.907f, 5169.108f, 45.30217f)), owning_building_guid = 44)
      LocalObject(1591, Locker.Constructor(Vector3(6402.163f, 5169.13f, 45.30217f)), owning_building_guid = 44)
      LocalObject(1592, Locker.Constructor(Vector3(6428.996f, 5186.643f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1593, Locker.Constructor(Vector3(6428.978f, 5187.699f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1594, Locker.Constructor(Vector3(6429.07f, 5182.419f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1595, Locker.Constructor(Vector3(6429.051f, 5183.473f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1596, Locker.Constructor(Vector3(6429.033f, 5184.533f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1597, Locker.Constructor(Vector3(6429.014f, 5185.588f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1602, Locker.Constructor(Vector3(6448.994f, 5186.996f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1603, Locker.Constructor(Vector3(6448.976f, 5188.051f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1604, Locker.Constructor(Vector3(6449.067f, 5182.771f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1605, Locker.Constructor(Vector3(6449.049f, 5183.827f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1606, Locker.Constructor(Vector3(6449.031f, 5184.881f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1607, Locker.Constructor(Vector3(6449.012f, 5185.937f, 45.21517f)), owning_building_guid = 44)
      LocalObject(1608, Locker.Constructor(Vector3(6449.588f, 5165.523f, 45.56817f)), owning_building_guid = 44)
      LocalObject(1609, Locker.Constructor(Vector3(6449.567f, 5166.687f, 45.56817f)), owning_building_guid = 44)
      LocalObject(1610, Locker.Constructor(Vector3(6449.547f, 5167.834f, 45.56817f)), owning_building_guid = 44)
      LocalObject(1611, Locker.Constructor(Vector3(6449.527f, 5168.983f, 45.56817f)), owning_building_guid = 44)
      LocalObject(1656, Locker.Constructor(Vector3(6453.665f, 5168.292f, 55.30717f)), owning_building_guid = 44)
      LocalObject(1657, Locker.Constructor(Vector3(6454.699f, 5168.31f, 55.30717f)), owning_building_guid = 44)
      LocalObject(1658, Locker.Constructor(Vector3(6457.221f, 5168.354f, 55.07817f)), owning_building_guid = 44)
      LocalObject(1659, Locker.Constructor(Vector3(6458.254f, 5168.372f, 55.07817f)), owning_building_guid = 44)
      LocalObject(1660, Locker.Constructor(Vector3(6459.308f, 5168.39f, 55.07817f)), owning_building_guid = 44)
      LocalObject(1661, Locker.Constructor(Vector3(6460.342f, 5168.408f, 55.07817f)), owning_building_guid = 44)
      LocalObject(1662, Locker.Constructor(Vector3(6462.858f, 5168.452f, 55.30717f)), owning_building_guid = 44)
      LocalObject(1663, Locker.Constructor(Vector3(6463.893f, 5168.47f, 55.30717f)), owning_building_guid = 44)
      LocalObject(
        249,
        Terminal.Constructor(Vector3(6394.699f, 5165.426f, 45.29716f), cert_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        250,
        Terminal.Constructor(Vector3(6394.921f, 5152.728f, 45.29716f), cert_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        251,
        Terminal.Constructor(Vector3(6396.122f, 5166.899f, 45.29716f), cert_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        252,
        Terminal.Constructor(Vector3(6396.394f, 5151.306f, 45.29716f), cert_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        253,
        Terminal.Constructor(Vector3(6403.445f, 5167.027f, 45.29716f), cert_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        254,
        Terminal.Constructor(Vector3(6403.718f, 5151.434f, 45.29716f), cert_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        255,
        Terminal.Constructor(Vector3(6404.918f, 5165.604f, 45.29716f), cert_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        256,
        Terminal.Constructor(Vector3(6405.14f, 5152.907f, 45.29716f), cert_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        1971,
        Terminal.Constructor(Vector3(6436.749f, 5168.391f, 46.89717f), order_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        1972,
        Terminal.Constructor(Vector3(6440.537f, 5168.457f, 46.89717f), order_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        1973,
        Terminal.Constructor(Vector3(6442.65f, 5139.807f, 56.60217f), order_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        1974,
        Terminal.Constructor(Vector3(6444.268f, 5168.522f, 46.89717f), order_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2729,
        Terminal.Constructor(Vector3(6388.02f, 5148.29f, 46.92017f), spawn_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2730,
        Terminal.Constructor(Vector3(6434.093f, 5154.658f, 47.44117f), spawn_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2731,
        Terminal.Constructor(Vector3(6441.38f, 5154.782f, 47.44117f), spawn_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2732,
        Terminal.Constructor(Vector3(6448.671f, 5154.914f, 47.44117f), spawn_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2733,
        Terminal.Constructor(Vector3(6453.291f, 5110.017f, 46.92017f), spawn_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2734,
        Terminal.Constructor(Vector3(6465.605f, 5152.143f, 56.88617f), spawn_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2735,
        Terminal.Constructor(Vector3(6508.862f, 5134.9f, 49.42017f), spawn_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2921,
        Terminal.Constructor(Vector3(6514.52f, 5188.725f, 57.61217f), vehicle_terminal_combined),
        owning_building_guid = 44
      )
      LocalObject(
        1815,
        VehicleSpawnPad.Constructor(Vector3(6514.848f, 5175.09f, 53.45417f), mb_pad_creation, Vector3(0, 0, 179)),
        owning_building_guid = 44,
        terminal_guid = 2921
      )
      LocalObject(2530, ResourceSilo.Constructor(Vector3(6402.208f, 5088.854f, 62.32417f)), owning_building_guid = 44)
      LocalObject(
        2610,
        SpawnTube.Constructor(Vector3(6432.667f, 5153.896f, 45.30717f), Vector3(0, 0, 269)),
        owning_building_guid = 44
      )
      LocalObject(
        2612,
        SpawnTube.Constructor(Vector3(6439.954f, 5154.023f, 45.30717f), Vector3(0, 0, 269)),
        owning_building_guid = 44
      )
      LocalObject(
        2613,
        SpawnTube.Constructor(Vector3(6447.244f, 5154.15f, 45.30717f), Vector3(0, 0, 269)),
        owning_building_guid = 44
      )
      LocalObject(
        156,
        ProximityTerminal.Constructor(Vector3(6459.072f, 5152.107f, 55.11717f), adv_med_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        1839,
        ProximityTerminal.Constructor(Vector3(6447.566f, 5177.569f, 45.30717f), medical_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2198,
        ProximityTerminal.Constructor(Vector3(6398.617f, 5134.167f, 63.64917f), pad_landing_frame),
        owning_building_guid = 44
      )
      LocalObject(
        2199,
        Terminal.Constructor(Vector3(6398.617f, 5134.167f, 63.64917f), air_rearm_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2201,
        ProximityTerminal.Constructor(Vector3(6406.731f, 5150.533f, 65.63917f), pad_landing_frame),
        owning_building_guid = 44
      )
      LocalObject(
        2202,
        Terminal.Constructor(Vector3(6406.731f, 5150.533f, 65.63917f), air_rearm_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2204,
        ProximityTerminal.Constructor(Vector3(6514.246f, 5132.967f, 65.60017f), pad_landing_frame),
        owning_building_guid = 44
      )
      LocalObject(
        2205,
        Terminal.Constructor(Vector3(6514.246f, 5132.967f, 65.60017f), air_rearm_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2207,
        ProximityTerminal.Constructor(Vector3(6521.244f, 5149.406f, 63.65917f), pad_landing_frame),
        owning_building_guid = 44
      )
      LocalObject(
        2208,
        Terminal.Constructor(Vector3(6521.244f, 5149.406f, 63.65917f), air_rearm_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2509,
        ProximityTerminal.Constructor(Vector3(6435.225f, 5202.245f, 55.05717f), repair_silo),
        owning_building_guid = 44
      )
      LocalObject(
        2510,
        Terminal.Constructor(Vector3(6435.225f, 5202.245f, 55.05717f), ground_rearm_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        2513,
        ProximityTerminal.Constructor(Vector3(6537.452f, 5116.011f, 55.05717f), repair_silo),
        owning_building_guid = 44
      )
      LocalObject(
        2514,
        Terminal.Constructor(Vector3(6537.452f, 5116.011f, 55.05717f), ground_rearm_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        1785,
        FacilityTurret.Constructor(Vector3(6370.029f, 5170.237f, 63.70917f), manned_turret),
        owning_building_guid = 44
      )
      TurretToWeapon(1785, 5059)
      LocalObject(
        1786,
        FacilityTurret.Constructor(Vector3(6372.79f, 5076.999f, 63.70917f), manned_turret),
        owning_building_guid = 44
      )
      TurretToWeapon(1786, 5060)
      LocalObject(
        1787,
        FacilityTurret.Constructor(Vector3(6412.379f, 5214.131f, 63.70917f), manned_turret),
        owning_building_guid = 44
      )
      TurretToWeapon(1787, 5061)
      LocalObject(
        1790,
        FacilityTurret.Constructor(Vector3(6547.504f, 5215.303f, 63.70917f), manned_turret),
        owning_building_guid = 44
      )
      TurretToWeapon(1790, 5062)
      LocalObject(
        1791,
        FacilityTurret.Constructor(Vector3(6549.856f, 5080.097f, 63.70917f), manned_turret),
        owning_building_guid = 44
      )
      TurretToWeapon(1791, 5063)
      LocalObject(
        973,
        ImplantTerminalMech.Constructor(Vector3(6392.11f, 5159.01f, 44.78417f)),
        owning_building_guid = 44
      )
      LocalObject(
        965,
        Terminal.Constructor(Vector3(6392.127f, 5159.01f, 44.78417f), implant_terminal_interface),
        owning_building_guid = 44
      )
      TerminalToInterface(973, 965)
      LocalObject(
        974,
        ImplantTerminalMech.Constructor(Vector3(6407.463f, 5159.29f, 44.78417f)),
        owning_building_guid = 44
      )
      LocalObject(
        966,
        Terminal.Constructor(Vector3(6407.445f, 5159.29f, 44.78417f), implant_terminal_interface),
        owning_building_guid = 44
      )
      TerminalToInterface(974, 966)
      LocalObject(
        2265,
        Painbox.Constructor(Vector3(6471.565f, 5155.936f, 69.33597f), painbox),
        owning_building_guid = 44
      )
      LocalObject(
        2279,
        Painbox.Constructor(Vector3(6444.032f, 5164.616f, 49.37707f), painbox_continuous),
        owning_building_guid = 44
      )
      LocalObject(
        2293,
        Painbox.Constructor(Vector3(6457.128f, 5154.272f, 69.54107f), painbox_door_radius),
        owning_building_guid = 44
      )
      LocalObject(
        2333,
        Painbox.Constructor(Vector3(6425.955f, 5162.063f, 47.02137f), painbox_door_radius_continuous),
        owning_building_guid = 44
      )
      LocalObject(
        2334,
        Painbox.Constructor(Vector3(6432.466f, 5177.545f, 48.84807f), painbox_door_radius_continuous),
        owning_building_guid = 44
      )
      LocalObject(
        2335,
        Painbox.Constructor(Vector3(6454.022f, 5160.577f, 47.66307f), painbox_door_radius_continuous),
        owning_building_guid = 44
      )
      LocalObject(310, Generator.Constructor(Vector3(6475.481f, 5154.436f, 64.01317f)), owning_building_guid = 44)
      LocalObject(
        296,
        Terminal.Constructor(Vector3(6467.291f, 5154.246f, 65.30717f), gen_control),
        owning_building_guid = 44
      )
    }

    Building23717()

    def Building23717(): Unit = { // Name: GW_Searhus_S Type: hst GUID: 47, MapID: 23717
      LocalBuilding(
        "GW_Searhus_S",
        47,
        23717,
        FoundationBuilder(WarpGate.Structure(Vector3(3773.85f, 1132.25f, 46.53f), hst))
      )
    }

    Building23718()

    def Building23718(): Unit = { // Name: GW_Searhus_N Type: hst GUID: 48, MapID: 23718
      LocalBuilding(
        "GW_Searhus_N",
        48,
        23718,
        FoundationBuilder(WarpGate.Structure(Vector3(7131.17f, 5593.67f, 43.53f), hst))
      )
    }

    Building16()

    def Building16(): Unit = { // Name: Tara Type: tech_plant GUID: 50, MapID: 16
      LocalBuilding(
        "Tara",
        50,
        16,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(1172f, 4140f, 59.97345f),
            Vector3(0f, 0f, 175f),
            tech_plant
          )
        )
      )
      LocalObject(
        211,
        CaptureTerminal.Constructor(Vector3(1171.127f, 4184.334f, 75.07345f), capture_terminal),
        owning_building_guid = 50
      )
      LocalObject(313, Door.Constructor(Vector3(1093.01f, 4143.792f, 61.51545f)), owning_building_guid = 50)
      LocalObject(314, Door.Constructor(Vector3(1094.596f, 4161.916f, 69.47845f)), owning_building_guid = 50)
      LocalObject(315, Door.Constructor(Vector3(1126.686f, 4112.579f, 69.47845f)), owning_building_guid = 50)
      LocalObject(316, Door.Constructor(Vector3(1144.809f, 4110.994f, 61.51545f)), owning_building_guid = 50)
      LocalObject(321, Door.Constructor(Vector3(1158.059f, 4180.796f, 76.59445f)), owning_building_guid = 50)
      LocalObject(322, Door.Constructor(Vector3(1163.485f, 4164.259f, 76.59445f)), owning_building_guid = 50)
      LocalObject(323, Door.Constructor(Vector3(1175.361f, 4231.252f, 61.62445f)), owning_building_guid = 50)
      LocalObject(325, Door.Constructor(Vector3(1193.485f, 4229.667f, 69.58745f)), owning_building_guid = 50)
      LocalObject(326, Door.Constructor(Vector3(1220.563f, 4236.133f, 61.59445f)), owning_building_guid = 50)
      LocalObject(327, Door.Constructor(Vector3(1241.282f, 4111.99f, 69.47845f)), owning_building_guid = 50)
      LocalObject(328, Door.Constructor(Vector3(1242.868f, 4130.113f, 61.51545f)), owning_building_guid = 50)
      LocalObject(329, Door.Constructor(Vector3(1247.71f, 4185.454f, 69.47845f)), owning_building_guid = 50)
      LocalObject(330, Door.Constructor(Vector3(1249.295f, 4203.576f, 61.51545f)), owning_building_guid = 50)
      LocalObject(623, Door.Constructor(Vector3(1216.331f, 4095.967f, 63.71045f)), owning_building_guid = 50)
      LocalObject(626, Door.Constructor(Vector3(1221.212f, 4151.756f, 43.71045f)), owning_building_guid = 50)
      LocalObject(629, Door.Constructor(Vector3(1142.562f, 4170.683f, 54.09445f)), owning_building_guid = 50)
      LocalObject(630, Door.Constructor(Vector3(1143.957f, 4186.622f, 46.59445f)), owning_building_guid = 50)
      LocalObject(631, Door.Constructor(Vector3(1145.501f, 4158.379f, 46.59445f)), owning_building_guid = 50)
      LocalObject(632, Door.Constructor(Vector3(1154.168f, 4165.652f, 54.09445f)), owning_building_guid = 50)
      LocalObject(633, Door.Constructor(Vector3(1156.26f, 4189.561f, 46.59445f)), owning_building_guid = 50)
      LocalObject(634, Door.Constructor(Vector3(1156.26f, 4189.561f, 54.09445f)), owning_building_guid = 50)
      LocalObject(635, Door.Constructor(Vector3(1174.44f, 4167.894f, 76.59445f)), owning_building_guid = 50)
      LocalObject(636, Door.Constructor(Vector3(1174.789f, 4171.878f, 66.59445f)), owning_building_guid = 50)
      LocalObject(637, Door.Constructor(Vector3(1175.138f, 4175.863f, 46.59445f)), owning_building_guid = 50)
      LocalObject(638, Door.Constructor(Vector3(1178.077f, 4163.56f, 56.59445f)), owning_building_guid = 50)
      LocalObject(639, Door.Constructor(Vector3(1178.624f, 4215.711f, 54.09445f)), owning_building_guid = 50)
      LocalObject(640, Door.Constructor(Vector3(1179.471f, 4179.499f, 56.59445f)), owning_building_guid = 50)
      LocalObject(641, Door.Constructor(Vector3(1179.471f, 4179.499f, 76.59445f)), owning_building_guid = 50)
      LocalObject(642, Door.Constructor(Vector3(1192.471f, 4190.408f, 54.09445f)), owning_building_guid = 50)
      LocalObject(643, Door.Constructor(Vector3(1195.957f, 4230.255f, 54.09445f)), owning_building_guid = 50)
      LocalObject(644, Door.Constructor(Vector3(1205.621f, 4157.135f, 51.59445f)), owning_building_guid = 50)
      LocalObject(645, Door.Constructor(Vector3(1207.713f, 4181.043f, 51.59445f)), owning_building_guid = 50)
      LocalObject(646, Door.Constructor(Vector3(1239.591f, 4178.254f, 51.59445f)), owning_building_guid = 50)
      LocalObject(647, Door.Constructor(Vector3(1241.683f, 4202.163f, 54.09445f)), owning_building_guid = 50)
      LocalObject(937, Door.Constructor(Vector3(1135.919f, 4188.99f, 62.35345f)), owning_building_guid = 50)
      LocalObject(2738, Door.Constructor(Vector3(1161.788f, 4168.684f, 54.42745f)), owning_building_guid = 50)
      LocalObject(2739, Door.Constructor(Vector3(1162.423f, 4175.945f, 54.42745f)), owning_building_guid = 50)
      LocalObject(2740, Door.Constructor(Vector3(1163.059f, 4183.21f, 54.42745f)), owning_building_guid = 50)
      LocalObject(
        989,
        IFFLock.Constructor(Vector3(1133.026f, 4191.992f, 61.55345f), Vector3(0, 0, 5)),
        owning_building_guid = 50,
        door_guid = 937
      )
      LocalObject(
        1003,
        IFFLock.Constructor(Vector3(1210.89f, 4094.083f, 61.66145f), Vector3(0, 0, 185)),
        owning_building_guid = 50,
        door_guid = 623
      )
      LocalObject(
        1008,
        IFFLock.Constructor(Vector3(1144.017f, 4159.456f, 46.40945f), Vector3(0, 0, 5)),
        owning_building_guid = 50,
        door_guid = 631
      )
      LocalObject(
        1009,
        IFFLock.Constructor(Vector3(1145.03f, 4188.105f, 46.40945f), Vector3(0, 0, 95)),
        owning_building_guid = 50,
        door_guid = 630
      )
      LocalObject(
        1012,
        IFFLock.Constructor(Vector3(1154.764f, 4190.504f, 53.90945f), Vector3(0, 0, 5)),
        owning_building_guid = 50,
        door_guid = 634
      )
      LocalObject(
        1013,
        IFFLock.Constructor(Vector3(1155.663f, 4164.708f, 53.90945f), Vector3(0, 0, 185)),
        owning_building_guid = 50,
        door_guid = 632
      )
      LocalObject(
        1014,
        IFFLock.Constructor(Vector3(1156.084f, 4181.771f, 76.51945f), Vector3(0, 0, 5)),
        owning_building_guid = 50,
        door_guid = 321
      )
      LocalObject(
        1015,
        IFFLock.Constructor(Vector3(1165.45f, 4163.276f, 76.51945f), Vector3(0, 0, 185)),
        owning_building_guid = 50,
        door_guid = 322
      )
      LocalObject(
        1016,
        IFFLock.Constructor(Vector3(1180.955f, 4178.426f, 76.40945f), Vector3(0, 0, 185)),
        owning_building_guid = 50,
        door_guid = 641
      )
      LocalObject(
        1017,
        IFFLock.Constructor(Vector3(1221.553f, 4238.101f, 61.52545f), Vector3(0, 0, 95)),
        owning_building_guid = 50,
        door_guid = 326
      )
      LocalObject(1244, Locker.Constructor(Vector3(1127.702f, 4169.809f, 45.07345f)), owning_building_guid = 50)
      LocalObject(1245, Locker.Constructor(Vector3(1129.033f, 4169.693f, 45.07345f)), owning_building_guid = 50)
      LocalObject(1246, Locker.Constructor(Vector3(1130.364f, 4169.576f, 45.07345f)), owning_building_guid = 50)
      LocalObject(1247, Locker.Constructor(Vector3(1131.683f, 4169.461f, 45.07345f)), owning_building_guid = 50)
      LocalObject(1248, Locker.Constructor(Vector3(1136.206f, 4169.065f, 45.07345f)), owning_building_guid = 50)
      LocalObject(1249, Locker.Constructor(Vector3(1137.538f, 4168.949f, 45.07345f)), owning_building_guid = 50)
      LocalObject(1250, Locker.Constructor(Vector3(1138.869f, 4168.833f, 45.07345f)), owning_building_guid = 50)
      LocalObject(1251, Locker.Constructor(Vector3(1140.188f, 4168.717f, 45.07345f)), owning_building_guid = 50)
      LocalObject(1252, Locker.Constructor(Vector3(1149.077f, 4188.04f, 52.83445f)), owning_building_guid = 50)
      LocalObject(1255, Locker.Constructor(Vector3(1150.221f, 4187.939f, 52.83445f)), owning_building_guid = 50)
      LocalObject(1258, Locker.Constructor(Vector3(1151.364f, 4187.839f, 52.83445f)), owning_building_guid = 50)
      LocalObject(1259, Locker.Constructor(Vector3(1152.524f, 4187.738f, 52.83445f)), owning_building_guid = 50)
      LocalObject(
        157,
        Terminal.Constructor(Vector3(1157.625f, 4166.211f, 75.67645f), air_vehicle_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1796,
        VehicleSpawnPad.Constructor(Vector3(1151.355f, 4145.987f, 72.55145f), mb_pad_creation, Vector3(0, 0, 185)),
        owning_building_guid = 50,
        terminal_guid = 157
      )
      LocalObject(
        158,
        Terminal.Constructor(Vector3(1169.511f, 4165.172f, 75.67645f), air_vehicle_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1797,
        VehicleSpawnPad.Constructor(Vector3(1172.293f, 4144.155f, 72.55145f), mb_pad_creation, Vector3(0, 0, 185)),
        owning_building_guid = 50,
        terminal_guid = 158
      )
      LocalObject(
        1840,
        Terminal.Constructor(Vector3(1148.33f, 4175.269f, 54.16345f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1841,
        Terminal.Constructor(Vector3(1148.66f, 4179.044f, 54.16345f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1842,
        Terminal.Constructor(Vector3(1148.985f, 4182.761f, 54.16345f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1846,
        Terminal.Constructor(Vector3(1171.265f, 4166.68f, 66.40345f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2642,
        Terminal.Constructor(Vector3(1136.674f, 4163.529f, 66.65545f), spawn_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2643,
        Terminal.Constructor(Vector3(1157.243f, 4138.064f, 71.99645f), spawn_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2644,
        Terminal.Constructor(Vector3(1161.709f, 4171.192f, 54.70745f), spawn_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2645,
        Terminal.Constructor(Vector3(1162.348f, 4178.453f, 54.70745f), spawn_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2646,
        Terminal.Constructor(Vector3(1162.979f, 4185.717f, 54.70745f), spawn_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2647,
        Terminal.Constructor(Vector3(1168.22f, 4187.921f, 46.63045f), spawn_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2648,
        Terminal.Constructor(Vector3(1204.282f, 4232.949f, 54.13045f), spawn_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2908,
        Terminal.Constructor(Vector3(1223.358f, 4176.239f, 45.78745f), ground_vehicle_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1798,
        VehicleSpawnPad.Constructor(Vector3(1222.457f, 4165.36f, 37.51045f), mb_pad_creation, Vector3(0, 0, 185)),
        owning_building_guid = 50,
        terminal_guid = 2908
      )
      LocalObject(2517, ResourceSilo.Constructor(Vector3(1107.646f, 4112.951f, 66.98145f)), owning_building_guid = 50)
      LocalObject(
        2533,
        SpawnTube.Constructor(Vector3(1162.318f, 4169.695f, 52.57345f), Vector3(0, 0, 185)),
        owning_building_guid = 50
      )
      LocalObject(
        2534,
        SpawnTube.Constructor(Vector3(1162.953f, 4176.955f, 52.57345f), Vector3(0, 0, 185)),
        owning_building_guid = 50
      )
      LocalObject(
        2535,
        SpawnTube.Constructor(Vector3(1163.589f, 4184.218f, 52.57345f), Vector3(0, 0, 185)),
        owning_building_guid = 50
      )
      LocalObject(
        1816,
        ProximityTerminal.Constructor(Vector3(1134.009f, 4169.805f, 45.07345f), medical_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1817,
        ProximityTerminal.Constructor(Vector3(1172.186f, 4177.225f, 65.07045f), medical_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2051,
        ProximityTerminal.Constructor(Vector3(1106.237f, 4171.75f, 68.18145f), pad_landing_frame),
        owning_building_guid = 50
      )
      LocalObject(
        2052,
        Terminal.Constructor(Vector3(1106.237f, 4171.75f, 68.18145f), air_rearm_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2054,
        ProximityTerminal.Constructor(Vector3(1113.922f, 4187.387f, 70.62545f), pad_landing_frame),
        owning_building_guid = 50
      )
      LocalObject(
        2055,
        Terminal.Constructor(Vector3(1113.922f, 4187.387f, 70.62545f), air_rearm_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2057,
        ProximityTerminal.Constructor(Vector3(1153.655f, 4223.288f, 68.18145f), pad_landing_frame),
        owning_building_guid = 50
      )
      LocalObject(
        2058,
        Terminal.Constructor(Vector3(1153.655f, 4223.288f, 68.18145f), air_rearm_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2060,
        ProximityTerminal.Constructor(Vector3(1168.281f, 4205.098f, 75.42045f), pad_landing_frame),
        owning_building_guid = 50
      )
      LocalObject(
        2061,
        Terminal.Constructor(Vector3(1168.281f, 4205.098f, 75.42045f), air_rearm_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2063,
        ProximityTerminal.Constructor(Vector3(1213.576f, 4144.561f, 70.52345f), pad_landing_frame),
        owning_building_guid = 50
      )
      LocalObject(
        2064,
        Terminal.Constructor(Vector3(1213.576f, 4144.561f, 70.52345f), air_rearm_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2066,
        ProximityTerminal.Constructor(Vector3(1235.184f, 4158.904f, 68.16845f), pad_landing_frame),
        owning_building_guid = 50
      )
      LocalObject(
        2067,
        Terminal.Constructor(Vector3(1235.184f, 4158.904f, 68.16845f), air_rearm_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2405,
        ProximityTerminal.Constructor(Vector3(1121.674f, 4237.55f, 59.72345f), repair_silo),
        owning_building_guid = 50
      )
      LocalObject(
        2406,
        Terminal.Constructor(Vector3(1121.674f, 4237.55f, 59.72345f), ground_rearm_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2409,
        ProximityTerminal.Constructor(Vector3(1168.064f, 4098.549f, 59.70195f), repair_silo),
        owning_building_guid = 50
      )
      LocalObject(
        2410,
        Terminal.Constructor(Vector3(1168.064f, 4098.549f, 59.70195f), ground_rearm_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1678,
        FacilityTurret.Constructor(Vector3(1081.49f, 4182.628f, 68.47245f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1678, 5064)
      LocalObject(
        1679,
        FacilityTurret.Constructor(Vector3(1082.827f, 4109.257f, 68.47245f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1679, 5065)
      LocalObject(
        1680,
        FacilityTurret.Constructor(Vector3(1094.832f, 4246.474f, 68.47245f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1680, 5066)
      LocalObject(
        1683,
        FacilityTurret.Constructor(Vector3(1157.805f, 4042.009f, 68.47245f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1683, 5067)
      LocalObject(
        1684,
        FacilityTurret.Constructor(Vector3(1247.158f, 4034.192f, 68.47245f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1684, 5068)
      LocalObject(
        1685,
        FacilityTurret.Constructor(Vector3(1258.946f, 4232.108f, 68.47245f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1685, 5069)
      LocalObject(
        2252,
        Painbox.Constructor(Vector3(1146.691f, 4146.022f, 48.54675f), painbox),
        owning_building_guid = 50
      )
      LocalObject(
        2266,
        Painbox.Constructor(Vector3(1154.453f, 4178.463f, 56.84335f), painbox_continuous),
        owning_building_guid = 50
      )
      LocalObject(
        2280,
        Painbox.Constructor(Vector3(1146.021f, 4160.873f, 48.23285f), painbox_door_radius),
        owning_building_guid = 50
      )
      LocalObject(
        2294,
        Painbox.Constructor(Vector3(1139.06f, 4172.424f, 56.15575f), painbox_door_radius_continuous),
        owning_building_guid = 50
      )
      LocalObject(
        2295,
        Painbox.Constructor(Vector3(1154.153f, 4163.877f, 55.24965f), painbox_door_radius_continuous),
        owning_building_guid = 50
      )
      LocalObject(
        2296,
        Painbox.Constructor(Vector3(1157.371f, 4191.191f, 54.70135f), painbox_door_radius_continuous),
        owning_building_guid = 50
      )
      LocalObject(297, Generator.Constructor(Vector3(1144.17f, 4142.881f, 43.77945f)), owning_building_guid = 50)
      LocalObject(
        283,
        Terminal.Constructor(Vector3(1144.837f, 4151.046f, 45.07345f), gen_control),
        owning_building_guid = 50
      )
    }

    Building8()

    def Building8(): Unit = { // Name: Karihi Type: tech_plant GUID: 53, MapID: 8
      LocalBuilding(
        "Karihi",
        53,
        8,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(3756f, 5592f, 236.1003f), Vector3(0f, 0f, 31f), tech_plant)
        )
      )
      LocalObject(
        214,
        CaptureTerminal.Constructor(Vector3(3782.765f, 5556.646f, 251.2003f), capture_terminal),
        owning_building_guid = 53
      )
      LocalObject(396, Door.Constructor(Vector3(3683.486f, 5573.938f, 245.6053f)), owning_building_guid = 53)
      LocalObject(398, Door.Constructor(Vector3(3692.855f, 5558.344f, 237.6423f)), owning_building_guid = 53)
      LocalObject(406, Door.Constructor(Vector3(3721.467f, 5510.727f, 245.6053f)), owning_building_guid = 53)
      LocalObject(409, Door.Constructor(Vector3(3730.836f, 5495.133f, 237.6423f)), owning_building_guid = 53)
      LocalObject(416, Door.Constructor(Vector3(3760.948f, 5631.449f, 237.6423f)), owning_building_guid = 53)
      LocalObject(422, Door.Constructor(Vector3(3773.217f, 5485.682f, 237.7213f)), owning_building_guid = 53)
      LocalObject(423, Door.Constructor(Vector3(3776.542f, 5640.819f, 245.6053f)), owning_building_guid = 53)
      LocalObject(424, Door.Constructor(Vector3(3777.148f, 5577.379f, 252.7213f)), owning_building_guid = 53)
      LocalObject(425, Door.Constructor(Vector3(3791.258f, 5567.189f, 252.7213f)), owning_building_guid = 53)
      LocalObject(426, Door.Constructor(Vector3(3791.323f, 5506.83f, 245.7143f)), owning_building_guid = 53)
      LocalObject(427, Door.Constructor(Vector3(3806.918f, 5516.2f, 237.7513f)), owning_building_guid = 53)
      LocalObject(430, Door.Constructor(Vector3(3822.133f, 5635.361f, 237.6423f)), owning_building_guid = 53)
      LocalObject(431, Door.Constructor(Vector3(3831.503f, 5619.767f, 245.6053f)), owning_building_guid = 53)
      LocalObject(624, Door.Constructor(Vector3(3694.253f, 5601.566f, 239.8373f)), owning_building_guid = 53)
      LocalObject(627, Door.Constructor(Vector3(3723.097f, 5553.563f, 219.8373f)), owning_building_guid = 53)
      LocalObject(686, Door.Constructor(Vector3(3723.803f, 5521.322f, 227.7213f)), owning_building_guid = 53)
      LocalObject(691, Door.Constructor(Vector3(3736.164f, 5500.75f, 230.2213f)), owning_building_guid = 53)
      LocalObject(697, Door.Constructor(Vector3(3738.871f, 5558.375f, 227.7213f)), owning_building_guid = 53)
      LocalObject(707, Door.Constructor(Vector3(3751.232f, 5537.803f, 227.7213f)), owning_building_guid = 53)
      LocalObject(711, Door.Constructor(Vector3(3764.932f, 5569.368f, 232.7213f)), owning_building_guid = 53)
      LocalObject(715, Door.Constructor(Vector3(3769.067f, 5539.187f, 230.2213f)), owning_building_guid = 53)
      LocalObject(717, Door.Constructor(Vector3(3770.421f, 5568f, 252.7213f)), owning_building_guid = 53)
      LocalObject(718, Door.Constructor(Vector3(3772.481f, 5564.571f, 242.7213f)), owning_building_guid = 53)
      LocalObject(719, Door.Constructor(Vector3(3773.173f, 5555.653f, 232.7213f)), owning_building_guid = 53)
      LocalObject(720, Door.Constructor(Vector3(3773.173f, 5555.653f, 252.7213f)), owning_building_guid = 53)
      LocalObject(721, Door.Constructor(Vector3(3774.541f, 5561.142f, 222.7213f)), owning_building_guid = 53)
      LocalObject(722, Door.Constructor(Vector3(3785.504f, 5581.729f, 230.2213f)), owning_building_guid = 53)
      LocalObject(724, Door.Constructor(Vector3(3788.241f, 5592.707f, 222.7213f)), owning_building_guid = 53)
      LocalObject(726, Door.Constructor(Vector3(3789.669f, 5504.9f, 230.2213f)), owning_building_guid = 53)
      LocalObject(727, Door.Constructor(Vector3(3795.143f, 5526.855f, 230.2213f)), owning_building_guid = 53)
      LocalObject(729, Door.Constructor(Vector3(3797.865f, 5561.157f, 222.7213f)), owning_building_guid = 53)
      LocalObject(730, Door.Constructor(Vector3(3797.865f, 5561.157f, 230.2213f)), owning_building_guid = 53)
      LocalObject(731, Door.Constructor(Vector3(3797.85f, 5584.48f, 230.2213f)), owning_building_guid = 53)
      LocalObject(732, Door.Constructor(Vector3(3806.091f, 5570.766f, 222.7213f)), owning_building_guid = 53)
      LocalObject(941, Door.Constructor(Vector3(3813.985f, 5573.574f, 238.4803f)), owning_building_guid = 53)
      LocalObject(2768, Door.Constructor(Vector3(3781.122f, 5574.796f, 230.5543f)), owning_building_guid = 53)
      LocalObject(2769, Door.Constructor(Vector3(3784.876f, 5568.548f, 230.5543f)), owning_building_guid = 53)
      LocalObject(2770, Door.Constructor(Vector3(3788.632f, 5562.297f, 230.5543f)), owning_building_guid = 53)
      LocalObject(
        993,
        IFFLock.Constructor(Vector3(3818.091f, 5572.846f, 237.6803f), Vector3(0, 0, 149)),
        owning_building_guid = 53,
        door_guid = 941
      )
      LocalObject(
        1004,
        IFFLock.Constructor(Vector3(3697.548f, 5606.289f, 237.7883f), Vector3(0, 0, 329)),
        owning_building_guid = 53,
        door_guid = 624
      )
      LocalObject(
        1090,
        IFFLock.Constructor(Vector3(3771.341f, 5555.649f, 252.5363f), Vector3(0, 0, 329)),
        owning_building_guid = 53,
        door_guid = 720
      )
      LocalObject(
        1091,
        IFFLock.Constructor(Vector3(3773.573f, 5483.509f, 237.6523f), Vector3(0, 0, 239)),
        owning_building_guid = 53,
        door_guid = 422
      )
      LocalObject(
        1092,
        IFFLock.Constructor(Vector3(3774.981f, 5577.019f, 252.6463f), Vector3(0, 0, 329)),
        owning_building_guid = 53,
        door_guid = 424
      )
      LocalObject(
        1093,
        IFFLock.Constructor(Vector3(3783.74f, 5581.613f, 230.0363f), Vector3(0, 0, 329)),
        owning_building_guid = 53,
        door_guid = 722
      )
      LocalObject(
        1094,
        IFFLock.Constructor(Vector3(3790.074f, 5592.708f, 222.5363f), Vector3(0, 0, 149)),
        owning_building_guid = 53,
        door_guid = 724
      )
      LocalObject(
        1095,
        IFFLock.Constructor(Vector3(3793.429f, 5567.562f, 252.6463f), Vector3(0, 0, 149)),
        owning_building_guid = 53,
        door_guid = 425
      )
      LocalObject(
        1096,
        IFFLock.Constructor(Vector3(3799.63f, 5561.272f, 230.0363f), Vector3(0, 0, 149)),
        owning_building_guid = 53,
        door_guid = 730
      )
      LocalObject(
        1097,
        IFFLock.Constructor(Vector3(3806.095f, 5568.934f, 222.5363f), Vector3(0, 0, 239)),
        owning_building_guid = 53,
        door_guid = 732
      )
      LocalObject(1380, Locker.Constructor(Vector3(3798.616f, 5587.466f, 221.2003f)), owning_building_guid = 53)
      LocalObject(1382, Locker.Constructor(Vector3(3799.751f, 5588.148f, 221.2003f)), owning_building_guid = 53)
      LocalObject(1383, Locker.Constructor(Vector3(3799.817f, 5564.827f, 228.9613f)), owning_building_guid = 53)
      LocalObject(1384, Locker.Constructor(Vector3(3800.814f, 5565.426f, 228.9613f)), owning_building_guid = 53)
      LocalObject(1385, Locker.Constructor(Vector3(3800.896f, 5588.836f, 221.2003f)), owning_building_guid = 53)
      LocalObject(1386, Locker.Constructor(Vector3(3801.797f, 5566.017f, 228.9613f)), owning_building_guid = 53)
      LocalObject(1387, Locker.Constructor(Vector3(3802.042f, 5589.525f, 221.2003f)), owning_building_guid = 53)
      LocalObject(1388, Locker.Constructor(Vector3(3802.782f, 5566.609f, 228.9613f)), owning_building_guid = 53)
      LocalObject(1389, Locker.Constructor(Vector3(3805.934f, 5591.863f, 221.2003f)), owning_building_guid = 53)
      LocalObject(1390, Locker.Constructor(Vector3(3807.069f, 5592.545f, 221.2003f)), owning_building_guid = 53)
      LocalObject(1391, Locker.Constructor(Vector3(3808.214f, 5593.233f, 221.2003f)), owning_building_guid = 53)
      LocalObject(1392, Locker.Constructor(Vector3(3809.36f, 5593.922f, 221.2003f)), owning_building_guid = 53)
      LocalObject(
        159,
        Terminal.Constructor(Vector3(3772.809f, 5573.099f, 251.8033f), air_vehicle_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1803,
        VehicleSpawnPad.Constructor(Vector3(3758.205f, 5588.466f, 248.6783f), mb_pad_creation, Vector3(0, 0, -31)),
        owning_building_guid = 53,
        terminal_guid = 159
      )
      LocalObject(
        160,
        Terminal.Constructor(Vector3(3783.037f, 5579.244f, 251.8033f), air_vehicle_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1804,
        VehicleSpawnPad.Constructor(Vector3(3776.221f, 5599.291f, 248.6783f), mb_pad_creation, Vector3(0, 0, -31)),
        owning_building_guid = 53,
        terminal_guid = 160
      )
      LocalObject(
        1891,
        Terminal.Constructor(Vector3(3772.277f, 5570.848f, 242.5303f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1892,
        Terminal.Constructor(Vector3(3795.88f, 5577.379f, 230.2903f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1893,
        Terminal.Constructor(Vector3(3797.832f, 5574.132f, 230.2903f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1894,
        Terminal.Constructor(Vector3(3799.753f, 5570.934f, 230.2903f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2667,
        Terminal.Constructor(Vector3(3766.801f, 5602.24f, 248.1233f), spawn_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2668,
        Terminal.Constructor(Vector3(3782.66f, 5572.813f, 230.8343f), spawn_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2669,
        Terminal.Constructor(Vector3(3784.518f, 5497.828f, 230.2573f), spawn_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2670,
        Terminal.Constructor(Vector3(3786.411f, 5566.565f, 230.8343f), spawn_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2671,
        Terminal.Constructor(Vector3(3787.225f, 5555.453f, 222.7573f), spawn_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2672,
        Terminal.Constructor(Vector3(3790.17f, 5560.316f, 230.8343f), spawn_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2673,
        Terminal.Constructor(Vector3(3798.409f, 5593.728f, 242.7823f), spawn_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2912,
        Terminal.Constructor(Vector3(3735.751f, 5532.495f, 221.9143f), ground_vehicle_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1802,
        VehicleSpawnPad.Constructor(Vector3(3730.085f, 5541.825f, 213.6373f), mb_pad_creation, Vector3(0, 0, -31)),
        owning_building_guid = 53,
        terminal_guid = 2912
      )
      LocalObject(2520, ResourceSilo.Constructor(Vector3(3792.165f, 5651.709f, 243.1083f)), owning_building_guid = 53)
      LocalObject(
        2563,
        SpawnTube.Constructor(Vector3(3781.287f, 5573.667f, 228.7003f), Vector3(0, 0, 329)),
        owning_building_guid = 53
      )
      LocalObject(
        2564,
        SpawnTube.Constructor(Vector3(3785.041f, 5567.42f, 228.7003f), Vector3(0, 0, 329)),
        owning_building_guid = 53
      )
      LocalObject(
        2565,
        SpawnTube.Constructor(Vector3(3788.796f, 5561.17f, 228.7003f), Vector3(0, 0, 329)),
        owning_building_guid = 53
      )
      LocalObject(
        1823,
        ProximityTerminal.Constructor(Vector3(3777.729f, 5561.775f, 241.1973f), medical_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1824,
        ProximityTerminal.Constructor(Vector3(3804.254f, 5590.218f, 221.2003f), medical_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2084,
        ProximityTerminal.Constructor(Vector3(3715.995f, 5539.568f, 244.2953f), pad_landing_frame),
        owning_building_guid = 53
      )
      LocalObject(
        2085,
        Terminal.Constructor(Vector3(3715.995f, 5539.568f, 244.2953f), air_rearm_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2087,
        ProximityTerminal.Constructor(Vector3(3725.045f, 5563.873f, 246.6503f), pad_landing_frame),
        owning_building_guid = 53
      )
      LocalObject(
        2088,
        Terminal.Constructor(Vector3(3725.045f, 5563.873f, 246.6503f), air_rearm_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2099,
        ProximityTerminal.Constructor(Vector3(3797.273f, 5541.521f, 251.5473f), pad_landing_frame),
        owning_building_guid = 53
      )
      LocalObject(
        2100,
        Terminal.Constructor(Vector3(3797.273f, 5541.521f, 251.5473f), air_rearm_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2105,
        ProximityTerminal.Constructor(Vector3(3819.797f, 5535.401f, 244.3083f), pad_landing_frame),
        owning_building_guid = 53
      )
      LocalObject(
        2106,
        Terminal.Constructor(Vector3(3819.797f, 5535.401f, 244.3083f), air_rearm_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2108,
        ProximityTerminal.Constructor(Vector3(3827.866f, 5604.968f, 244.3083f), pad_landing_frame),
        owning_building_guid = 53
      )
      LocalObject(
        2109,
        Terminal.Constructor(Vector3(3827.866f, 5604.968f, 244.3083f), air_rearm_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2111,
        ProximityTerminal.Constructor(Vector3(3830.84f, 5587.8f, 246.7523f), pad_landing_frame),
        owning_building_guid = 53
      )
      LocalObject(
        2112,
        Terminal.Constructor(Vector3(3830.84f, 5587.8f, 246.7523f), air_rearm_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2429,
        ProximityTerminal.Constructor(Vector3(3734.82f, 5627.849f, 235.8288f), repair_silo),
        owning_building_guid = 53
      )
      LocalObject(
        2430,
        Terminal.Constructor(Vector3(3734.82f, 5627.849f, 235.8288f), ground_rearm_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2441,
        ProximityTerminal.Constructor(Vector3(3854.053f, 5542.662f, 235.8503f), repair_silo),
        owning_building_guid = 53
      )
      LocalObject(
        2442,
        Terminal.Constructor(Vector3(3854.053f, 5542.662f, 235.8503f), ground_rearm_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1703,
        FacilityTurret.Constructor(Vector3(3633.003f, 5633.424f, 244.5993f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1703, 5070)
      LocalObject(
        1707,
        FacilityTurret.Constructor(Vector3(3709.887f, 5679.62f, 244.5993f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1707, 5071)
      LocalObject(
        1709,
        FacilityTurret.Constructor(Vector3(3739.799f, 5466.378f, 244.5993f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1709, 5072)
      LocalObject(
        1712,
        FacilityTurret.Constructor(Vector3(3810.072f, 5669.286f, 244.5993f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1712, 5073)
      LocalObject(
        1717,
        FacilityTurret.Constructor(Vector3(3854.28f, 5610.714f, 244.5993f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1717, 5074)
      LocalObject(
        1718,
        FacilityTurret.Constructor(Vector3(3881.014f, 5551.219f, 244.5993f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1718, 5075)
      LocalObject(
        2256,
        Painbox.Constructor(Vector3(3780.015f, 5602.004f, 224.6736f), painbox),
        owning_building_guid = 53
      )
      LocalObject(
        2270,
        Painbox.Constructor(Vector3(3792.804f, 5571.196f, 232.9702f), painbox_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        2284,
        Painbox.Constructor(Vector3(3789.286f, 5590.384f, 224.3597f), painbox_door_radius),
        owning_building_guid = 53
      )
      LocalObject(
        2306,
        Painbox.Constructor(Vector3(3784.474f, 5583.173f, 231.3765f), painbox_door_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        2307,
        Painbox.Constructor(Vector3(3797.925f, 5559.184f, 230.8282f), painbox_door_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        2308,
        Painbox.Constructor(Vector3(3801.708f, 5585.13f, 232.2826f), painbox_door_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(301, Generator.Constructor(Vector3(3780.208f, 5606.027f, 219.9063f)), owning_building_guid = 53)
      LocalObject(
        287,
        Terminal.Constructor(Vector3(3784.468f, 5599.029f, 221.2003f), gen_control),
        owning_building_guid = 53
      )
    }

    Building11()

    def Building11(): Unit = { // Name: Ngaru Type: tech_plant GUID: 56, MapID: 11
      LocalBuilding(
        "Ngaru",
        56,
        11,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4100f, 4200f, 204.9974f),
            Vector3(0f, 0f, 309f),
            tech_plant
          )
        )
      )
      LocalObject(
        216,
        CaptureTerminal.Constructor(Vector3(4068.716f, 4168.575f, 220.0974f), capture_terminal),
        owning_building_guid = 56
      )
      LocalObject(433, Door.Constructor(Vector3(3997.113f, 4168.154f, 206.6184f)), owning_building_guid = 56)
      LocalObject(434, Door.Constructor(Vector3(4000.573f, 4211.438f, 206.5394f)), owning_building_guid = 56)
      LocalObject(439, Door.Constructor(Vector3(4014.711f, 4222.886f, 214.5024f)), owning_building_guid = 56)
      LocalObject(440, Door.Constructor(Vector3(4020.575f, 4153.167f, 214.6114f)), owning_building_guid = 56)
      LocalObject(441, Door.Constructor(Vector3(4032.024f, 4139.028f, 206.6484f)), owning_building_guid = 56)
      LocalObject(442, Door.Constructor(Vector3(4057.883f, 4257.846f, 206.5394f)), owning_building_guid = 56)
      LocalObject(443, Door.Constructor(Vector3(4072.021f, 4269.295f, 214.5024f)), owning_building_guid = 56)
      LocalObject(444, Door.Constructor(Vector3(4080.338f, 4161.632f, 221.6184f)), owning_building_guid = 56)
      LocalObject(445, Door.Constructor(Vector3(4088.464f, 4177.023f, 221.6184f)), owning_building_guid = 56)
      LocalObject(446, Door.Constructor(Vector3(4138.005f, 4129.096f, 214.5024f)), owning_building_guid = 56)
      LocalObject(447, Door.Constructor(Vector3(4139.754f, 4200.59f, 206.5394f)), owning_building_guid = 56)
      LocalObject(449, Door.Constructor(Vector3(4151.203f, 4186.452f, 214.5024f)), owning_building_guid = 56)
      LocalObject(450, Door.Constructor(Vector3(4152.144f, 4140.545f, 206.5394f)), owning_building_guid = 56)
      LocalObject(625, Door.Constructor(Vector3(4100.88f, 4262.477f, 208.7344f)), owning_building_guid = 56)
      LocalObject(628, Door.Constructor(Vector3(4057.358f, 4227.234f, 188.7344f)), owning_building_guid = 56)
      LocalObject(736, Door.Constructor(Vector3(4006.878f, 4206.943f, 199.1184f)), owning_building_guid = 56)
      LocalObject(737, Door.Constructor(Vector3(4018.433f, 4154.537f, 199.1184f)), owning_building_guid = 56)
      LocalObject(738, Door.Constructor(Vector3(4025.529f, 4222.047f, 196.6184f)), owning_building_guid = 56)
      LocalObject(739, Door.Constructor(Vector3(4040.937f, 4152.172f, 199.1184f)), owning_building_guid = 56)
      LocalObject(740, Door.Constructor(Vector3(4045.667f, 4197.179f, 196.6184f)), owning_building_guid = 56)
      LocalObject(741, Door.Constructor(Vector3(4049.519f, 4179.709f, 199.1184f)), owning_building_guid = 56)
      LocalObject(742, Door.Constructor(Vector3(4064.319f, 4212.282f, 196.6184f)), owning_building_guid = 56)
      LocalObject(743, Door.Constructor(Vector3(4066.397f, 4177.936f, 201.6184f)), owning_building_guid = 56)
      LocalObject(744, Door.Constructor(Vector3(4066.397f, 4177.936f, 221.6184f)), owning_building_guid = 56)
      LocalObject(745, Door.Constructor(Vector3(4072.023f, 4177.344f, 191.6184f)), owning_building_guid = 56)
      LocalObject(746, Door.Constructor(Vector3(4075.131f, 4179.862f, 211.6184f)), owning_building_guid = 56)
      LocalObject(747, Door.Constructor(Vector3(4075.283f, 4154.25f, 191.6184f)), owning_building_guid = 56)
      LocalObject(748, Door.Constructor(Vector3(4075.283f, 4154.25f, 199.1184f)), owning_building_guid = 56)
      LocalObject(749, Door.Constructor(Vector3(4078.24f, 4182.379f, 221.6184f)), owning_building_guid = 56)
      LocalObject(750, Door.Constructor(Vector3(4078.831f, 4188.005f, 201.6184f)), owning_building_guid = 56)
      LocalObject(751, Door.Constructor(Vector3(4085.944f, 4147.441f, 191.6184f)), owning_building_guid = 56)
      LocalObject(752, Door.Constructor(Vector3(4093.935f, 4169.354f, 199.1184f)), owning_building_guid = 56)
      LocalObject(753, Door.Constructor(Vector3(4098.378f, 4157.51f, 199.1184f)), owning_building_guid = 56)
      LocalObject(754, Door.Constructor(Vector3(4105.187f, 4168.171f, 191.6184f)), owning_building_guid = 56)
      LocalObject(942, Door.Constructor(Vector3(4089.823f, 4140.015f, 207.3774f)), owning_building_guid = 56)
      LocalObject(2773, Door.Constructor(Vector3(4075.128f, 4163.552f, 199.4514f)), owning_building_guid = 56)
      LocalObject(2774, Door.Constructor(Vector3(4080.795f, 4168.142f, 199.4514f)), owning_building_guid = 56)
      LocalObject(2775, Door.Constructor(Vector3(4086.46f, 4172.729f, 199.4514f)), owning_building_guid = 56)
      LocalObject(
        994,
        IFFLock.Constructor(Vector3(4089.674f, 4135.848f, 206.5774f), Vector3(0, 0, 231)),
        owning_building_guid = 56,
        door_guid = 942
      )
      LocalObject(
        1005,
        IFFLock.Constructor(Vector3(4106.015f, 4259.872f, 206.6854f), Vector3(0, 0, 411)),
        owning_building_guid = 56,
        door_guid = 625
      )
      LocalObject(
        1100,
        IFFLock.Constructor(Vector3(3995.01f, 4167.499f, 206.5494f), Vector3(0, 0, 321)),
        owning_building_guid = 56,
        door_guid = 433
      )
      LocalObject(
        1105,
        IFFLock.Constructor(Vector3(4066.138f, 4179.749f, 221.4334f), Vector3(0, 0, 51)),
        owning_building_guid = 56,
        door_guid = 744
      )
      LocalObject(
        1106,
        IFFLock.Constructor(Vector3(4075.643f, 4152.518f, 198.9334f), Vector3(0, 0, 231)),
        owning_building_guid = 56,
        door_guid = 748
      )
      LocalObject(
        1107,
        IFFLock.Constructor(Vector3(4081.009f, 4159.534f, 221.5434f), Vector3(0, 0, 231)),
        owning_building_guid = 56,
        door_guid = 444
      )
      LocalObject(
        1108,
        IFFLock.Constructor(Vector3(4084.131f, 4147.183f, 191.4334f), Vector3(0, 0, 321)),
        owning_building_guid = 56,
        door_guid = 751
      )
      LocalObject(
        1109,
        IFFLock.Constructor(Vector3(4087.806f, 4179.119f, 221.5434f), Vector3(0, 0, 51)),
        owning_building_guid = 56,
        door_guid = 445
      )
      LocalObject(
        1110,
        IFFLock.Constructor(Vector3(4093.575f, 4171.085f, 198.9334f), Vector3(0, 0, 51)),
        owning_building_guid = 56,
        door_guid = 752
      )
      LocalObject(
        1111,
        IFFLock.Constructor(Vector3(4105.443f, 4166.355f, 191.4334f), Vector3(0, 0, 231)),
        owning_building_guid = 56,
        door_guid = 754
      )
      LocalObject(1401, Locker.Constructor(Vector3(4079.189f, 4152.828f, 197.8584f)), owning_building_guid = 56)
      LocalObject(1402, Locker.Constructor(Vector3(4079.922f, 4151.923f, 197.8584f)), owning_building_guid = 56)
      LocalObject(1403, Locker.Constructor(Vector3(4080.644f, 4151.032f, 197.8584f)), owning_building_guid = 56)
      LocalObject(1404, Locker.Constructor(Vector3(4081.367f, 4150.139f, 197.8584f)), owning_building_guid = 56)
      LocalObject(1405, Locker.Constructor(Vector3(4101.441f, 4157.168f, 190.0974f)), owning_building_guid = 56)
      LocalObject(1406, Locker.Constructor(Vector3(4102.274f, 4156.139f, 190.0974f)), owning_building_guid = 56)
      LocalObject(1407, Locker.Constructor(Vector3(4103.115f, 4155.101f, 190.0974f)), owning_building_guid = 56)
      LocalObject(1408, Locker.Constructor(Vector3(4103.957f, 4154.062f, 190.0974f)), owning_building_guid = 56)
      LocalObject(1409, Locker.Constructor(Vector3(4106.814f, 4150.533f, 190.0974f)), owning_building_guid = 56)
      LocalObject(1410, Locker.Constructor(Vector3(4107.647f, 4149.504f, 190.0974f)), owning_building_guid = 56)
      LocalObject(1411, Locker.Constructor(Vector3(4108.488f, 4148.466f, 190.0974f)), owning_building_guid = 56)
      LocalObject(1412, Locker.Constructor(Vector3(4109.329f, 4147.427f, 190.0974f)), owning_building_guid = 56)
      LocalObject(
        161,
        Terminal.Constructor(Vector3(4083.622f, 4180.724f, 220.7004f), air_vehicle_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1806,
        VehicleSpawnPad.Constructor(Vector3(4096.807f, 4197.325f, 217.5754f), mb_pad_creation, Vector3(0, 0, 51)),
        owning_building_guid = 56,
        terminal_guid = 161
      )
      LocalObject(
        162,
        Terminal.Constructor(Vector3(4091.131f, 4171.451f, 220.7004f), air_vehicle_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1807,
        VehicleSpawnPad.Constructor(Vector3(4110.034f, 4180.99f, 217.5754f), mb_pad_creation, Vector3(0, 0, 51)),
        owning_building_guid = 56,
        terminal_guid = 162
      )
      LocalObject(
        1898,
        Terminal.Constructor(Vector3(4081.319f, 4180.938f, 211.4274f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1899,
        Terminal.Constructor(Vector3(4085.228f, 4153.741f, 199.1874f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1900,
        Terminal.Constructor(Vector3(4088.127f, 4156.088f, 199.1874f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1901,
        Terminal.Constructor(Vector3(4091.072f, 4158.473f, 199.1874f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2675,
        Terminal.Constructor(Vector3(4010.713f, 4158.654f, 199.1544f), spawn_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2676,
        Terminal.Constructor(Vector3(4068.154f, 4163.993f, 191.6544f), spawn_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2677,
        Terminal.Constructor(Vector3(4073.38f, 4161.753f, 199.7314f), spawn_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2678,
        Terminal.Constructor(Vector3(4079.045f, 4166.345f, 199.7314f), spawn_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2679,
        Terminal.Constructor(Vector3(4084.71f, 4170.929f, 199.7314f), spawn_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2680,
        Terminal.Constructor(Vector3(4107.614f, 4158.244f, 211.6794f), spawn_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2681,
        Terminal.Constructor(Vector3(4111.644f, 4190.73f, 217.0204f), spawn_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2913,
        Terminal.Constructor(Vector3(4038.256f, 4211.77f, 190.8114f), ground_vehicle_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1805,
        VehicleSpawnPad.Constructor(Vector3(4046.707f, 4218.68f, 182.5344f), mb_pad_creation, Vector3(0, 0, 51)),
        owning_building_guid = 56,
        terminal_guid = 2913
      )
      LocalObject(2522, ResourceSilo.Constructor(Vector3(4164.162f, 4172.497f, 212.0054f)), owning_building_guid = 56)
      LocalObject(
        2568,
        SpawnTube.Constructor(Vector3(4074.035f, 4163.233f, 197.5974f), Vector3(0, 0, 51)),
        owning_building_guid = 56
      )
      LocalObject(
        2569,
        SpawnTube.Constructor(Vector3(4079.701f, 4167.821f, 197.5974f), Vector3(0, 0, 51)),
        owning_building_guid = 56
      )
      LocalObject(
        2570,
        SpawnTube.Constructor(Vector3(4085.365f, 4172.408f, 197.5974f), Vector3(0, 0, 51)),
        owning_building_guid = 56
      )
      LocalObject(
        1825,
        ProximityTerminal.Constructor(Vector3(4073.094f, 4174.275f, 210.0944f), medical_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1826,
        ProximityTerminal.Constructor(Vector3(4104.951f, 4151.968f, 190.0974f), medical_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2114,
        ProximityTerminal.Constructor(Vector3(4042.51f, 4232.319f, 213.1924f), pad_landing_frame),
        owning_building_guid = 56
      )
      LocalObject(
        2115,
        Terminal.Constructor(Vector3(4042.51f, 4232.319f, 213.1924f), air_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2117,
        ProximityTerminal.Constructor(Vector3(4052.831f, 4128.947f, 213.2054f), pad_landing_frame),
        owning_building_guid = 56
      )
      LocalObject(
        2118,
        Terminal.Constructor(Vector3(4052.831f, 4128.947f, 213.2054f), air_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2120,
        ProximityTerminal.Constructor(Vector3(4055.756f, 4152.104f, 220.4444f), pad_landing_frame),
        owning_building_guid = 56
      )
      LocalObject(
        2121,
        Terminal.Constructor(Vector3(4055.756f, 4152.104f, 220.4444f), air_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2123,
        ProximityTerminal.Constructor(Vector3(4067.838f, 4226.739f, 215.5474f), pad_landing_frame),
        owning_building_guid = 56
      )
      LocalObject(
        2124,
        Terminal.Constructor(Vector3(4067.838f, 4226.739f, 215.5474f), air_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2126,
        ProximityTerminal.Constructor(Vector3(4106.257f, 4125.304f, 215.6494f), pad_landing_frame),
        owning_building_guid = 56
      )
      LocalObject(
        2127,
        Terminal.Constructor(Vector3(4106.257f, 4125.304f, 215.6494f), air_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2129,
        ProximityTerminal.Constructor(Vector3(4122.843f, 4130.638f, 213.2054f), pad_landing_frame),
        owning_building_guid = 56
      )
      LocalObject(
        2130,
        Terminal.Constructor(Vector3(4122.843f, 4130.638f, 213.2054f), air_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2445,
        ProximityTerminal.Constructor(Vector3(4064.788f, 4096.035f, 204.7474f), repair_silo),
        owning_building_guid = 56
      )
      LocalObject(
        2446,
        Terminal.Constructor(Vector3(4064.788f, 4096.035f, 204.7474f), ground_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2449,
        ProximityTerminal.Constructor(Vector3(4132.552f, 4225.963f, 204.7259f), repair_silo),
        owning_building_guid = 56
      )
      LocalObject(
        2450,
        Terminal.Constructor(Vector3(4132.552f, 4225.963f, 204.7259f), ground_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1719,
        FacilityTurret.Constructor(Vector3(3973.346f, 4198.56f, 213.4964f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1719, 5076)
      LocalObject(
        1722,
        FacilityTurret.Constructor(Vector3(4077.014f, 4070.527f, 213.4964f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1722, 5077)
      LocalObject(
        1723,
        FacilityTurret.Constructor(Vector3(4123.903f, 4327.565f, 213.4964f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1723, 5078)
      LocalObject(
        1724,
        FacilityTurret.Constructor(Vector3(4132.21f, 4105.281f, 213.4964f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1724, 5079)
      LocalObject(
        1725,
        FacilityTurret.Constructor(Vector3(4180.35f, 4257.859f, 213.4964f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1725, 5080)
      LocalObject(
        1726,
        FacilityTurret.Constructor(Vector3(4184.059f, 4157.21f, 213.4964f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1726, 5081)
      LocalObject(
        2257,
        Painbox.Constructor(Vector3(4113.249f, 4177.611f, 193.5707f), painbox),
        owning_building_guid = 56
      )
      LocalObject(
        2271,
        Painbox.Constructor(Vector3(4084.521f, 4160.659f, 201.8673f), painbox_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2285,
        Painbox.Constructor(Vector3(4103.032f, 4166.813f, 193.2568f), painbox_door_radius),
        owning_building_guid = 56
      )
      LocalObject(
        2309,
        Painbox.Constructor(Vector3(4073.338f, 4153.917f, 199.7253f), painbox_door_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2310,
        Painbox.Constructor(Vector3(4095.222f, 4170.575f, 200.2736f), painbox_door_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2311,
        Painbox.Constructor(Vector3(4099.558f, 4153.781f, 201.1797f), painbox_door_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(302, Generator.Constructor(Vector3(4117.259f, 4177.979f, 188.8034f)), owning_building_guid = 56)
      LocalObject(
        288,
        Terminal.Constructor(Vector3(4110.922f, 4172.788f, 190.0974f), gen_control),
        owning_building_guid = 56
      )
    }

    Building47()

    def Building47(): Unit = { // Name: N_Tara_Tower Type: tower_a GUID: 59, MapID: 47
      LocalBuilding(
        "N_Tara_Tower",
        59,
        47,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1134f, 4500f, 60.84628f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2619,
        CaptureTerminal.Constructor(Vector3(1150.587f, 4499.897f, 70.84528f), secondary_capture),
        owning_building_guid = 59
      )
      LocalObject(317, Door.Constructor(Vector3(1146f, 4492f, 62.36728f)), owning_building_guid = 59)
      LocalObject(318, Door.Constructor(Vector3(1146f, 4492f, 82.36628f)), owning_building_guid = 59)
      LocalObject(319, Door.Constructor(Vector3(1146f, 4508f, 62.36728f)), owning_building_guid = 59)
      LocalObject(320, Door.Constructor(Vector3(1146f, 4508f, 82.36628f)), owning_building_guid = 59)
      LocalObject(2736, Door.Constructor(Vector3(1145.146f, 4488.794f, 52.18228f)), owning_building_guid = 59)
      LocalObject(2737, Door.Constructor(Vector3(1145.146f, 4505.204f, 52.18228f)), owning_building_guid = 59)
      LocalObject(
        1006,
        IFFLock.Constructor(Vector3(1143.957f, 4508.811f, 62.30728f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 319
      )
      LocalObject(
        1007,
        IFFLock.Constructor(Vector3(1143.957f, 4508.811f, 82.30728f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 320
      )
      LocalObject(
        1010,
        IFFLock.Constructor(Vector3(1148.047f, 4491.189f, 62.30728f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 317
      )
      LocalObject(
        1011,
        IFFLock.Constructor(Vector3(1148.047f, 4491.189f, 82.30728f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 318
      )
      LocalObject(1253, Locker.Constructor(Vector3(1149.716f, 4484.963f, 50.84028f)), owning_building_guid = 59)
      LocalObject(1254, Locker.Constructor(Vector3(1149.751f, 4506.835f, 50.84028f)), owning_building_guid = 59)
      LocalObject(1256, Locker.Constructor(Vector3(1151.053f, 4484.963f, 50.84028f)), owning_building_guid = 59)
      LocalObject(1257, Locker.Constructor(Vector3(1151.088f, 4506.835f, 50.84028f)), owning_building_guid = 59)
      LocalObject(1260, Locker.Constructor(Vector3(1153.741f, 4484.963f, 50.84028f)), owning_building_guid = 59)
      LocalObject(1261, Locker.Constructor(Vector3(1153.741f, 4506.835f, 50.84028f)), owning_building_guid = 59)
      LocalObject(1262, Locker.Constructor(Vector3(1155.143f, 4484.963f, 50.84028f)), owning_building_guid = 59)
      LocalObject(1263, Locker.Constructor(Vector3(1155.143f, 4506.835f, 50.84028f)), owning_building_guid = 59)
      LocalObject(
        1843,
        Terminal.Constructor(Vector3(1155.445f, 4490.129f, 52.17828f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1844,
        Terminal.Constructor(Vector3(1155.445f, 4495.853f, 52.17828f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1845,
        Terminal.Constructor(Vector3(1155.445f, 4501.234f, 52.17828f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        2531,
        SpawnTube.Constructor(Vector3(1144.706f, 4487.742f, 50.32828f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        2532,
        SpawnTube.Constructor(Vector3(1144.706f, 4504.152f, 50.32828f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        1681,
        FacilityTurret.Constructor(Vector3(1121.32f, 4487.295f, 79.78828f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1681, 5082)
      LocalObject(
        1682,
        FacilityTurret.Constructor(Vector3(1156.647f, 4512.707f, 79.78828f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1682, 5083)
      LocalObject(
        2336,
        Painbox.Constructor(Vector3(1139.235f, 4493.803f, 52.34538f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        2337,
        Painbox.Constructor(Vector3(1150.889f, 4502.086f, 50.94628f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        2338,
        Painbox.Constructor(Vector3(1150.975f, 4490.223f, 50.94628f), painbox_radius_continuous),
        owning_building_guid = 59
      )
    }

    Building20()

    def Building20(): Unit = { // Name: SE_Wakea_Tower Type: tower_a GUID: 60, MapID: 20
      LocalBuilding(
        "SE_Wakea_Tower",
        60,
        20,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1934f, 5188f, 54.91245f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2622,
        CaptureTerminal.Constructor(Vector3(1950.587f, 5187.897f, 64.91145f), secondary_capture),
        owning_building_guid = 60
      )
      LocalObject(354, Door.Constructor(Vector3(1946f, 5180f, 56.43345f)), owning_building_guid = 60)
      LocalObject(355, Door.Constructor(Vector3(1946f, 5180f, 76.43245f)), owning_building_guid = 60)
      LocalObject(356, Door.Constructor(Vector3(1946f, 5196f, 56.43345f)), owning_building_guid = 60)
      LocalObject(357, Door.Constructor(Vector3(1946f, 5196f, 76.43245f)), owning_building_guid = 60)
      LocalObject(2748, Door.Constructor(Vector3(1945.146f, 5176.794f, 46.24845f)), owning_building_guid = 60)
      LocalObject(2749, Door.Constructor(Vector3(1945.146f, 5193.204f, 46.24845f)), owning_building_guid = 60)
      LocalObject(
        1039,
        IFFLock.Constructor(Vector3(1943.957f, 5196.811f, 56.37345f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 356
      )
      LocalObject(
        1040,
        IFFLock.Constructor(Vector3(1943.957f, 5196.811f, 76.37345f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 357
      )
      LocalObject(
        1041,
        IFFLock.Constructor(Vector3(1948.047f, 5179.189f, 56.37345f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 354
      )
      LocalObject(
        1042,
        IFFLock.Constructor(Vector3(1948.047f, 5179.189f, 76.37345f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 355
      )
      LocalObject(1292, Locker.Constructor(Vector3(1949.716f, 5172.963f, 44.90645f)), owning_building_guid = 60)
      LocalObject(1293, Locker.Constructor(Vector3(1949.751f, 5194.835f, 44.90645f)), owning_building_guid = 60)
      LocalObject(1294, Locker.Constructor(Vector3(1951.053f, 5172.963f, 44.90645f)), owning_building_guid = 60)
      LocalObject(1295, Locker.Constructor(Vector3(1951.088f, 5194.835f, 44.90645f)), owning_building_guid = 60)
      LocalObject(1296, Locker.Constructor(Vector3(1953.741f, 5172.963f, 44.90645f)), owning_building_guid = 60)
      LocalObject(1297, Locker.Constructor(Vector3(1953.741f, 5194.835f, 44.90645f)), owning_building_guid = 60)
      LocalObject(1298, Locker.Constructor(Vector3(1955.143f, 5172.963f, 44.90645f)), owning_building_guid = 60)
      LocalObject(1299, Locker.Constructor(Vector3(1955.143f, 5194.835f, 44.90645f)), owning_building_guid = 60)
      LocalObject(
        1860,
        Terminal.Constructor(Vector3(1955.445f, 5178.129f, 46.24445f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1861,
        Terminal.Constructor(Vector3(1955.445f, 5183.853f, 46.24445f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1862,
        Terminal.Constructor(Vector3(1955.445f, 5189.234f, 46.24445f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        2543,
        SpawnTube.Constructor(Vector3(1944.706f, 5175.742f, 44.39445f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        2544,
        SpawnTube.Constructor(Vector3(1944.706f, 5192.152f, 44.39445f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        1694,
        FacilityTurret.Constructor(Vector3(1921.32f, 5175.295f, 73.85445f), manned_turret),
        owning_building_guid = 60
      )
      TurretToWeapon(1694, 5084)
      LocalObject(
        1695,
        FacilityTurret.Constructor(Vector3(1956.647f, 5200.707f, 73.85445f), manned_turret),
        owning_building_guid = 60
      )
      TurretToWeapon(1695, 5085)
      LocalObject(
        2345,
        Painbox.Constructor(Vector3(1939.235f, 5181.803f, 46.41155f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        2346,
        Painbox.Constructor(Vector3(1950.889f, 5190.086f, 45.01245f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        2347,
        Painbox.Constructor(Vector3(1950.975f, 5178.223f, 45.01245f), painbox_radius_continuous),
        owning_building_guid = 60
      )
    }

    Building18()

    def Building18(): Unit = { // Name: E_Esamir_Warpgate_Tower Type: tower_a GUID: 61, MapID: 18
      LocalBuilding(
        "E_Esamir_Warpgate_Tower",
        61,
        18,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2544f, 1462f, 53.86364f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2624,
        CaptureTerminal.Constructor(Vector3(2560.587f, 1461.897f, 63.86264f), secondary_capture),
        owning_building_guid = 61
      )
      LocalObject(364, Door.Constructor(Vector3(2556f, 1454f, 55.38464f)), owning_building_guid = 61)
      LocalObject(365, Door.Constructor(Vector3(2556f, 1454f, 75.38364f)), owning_building_guid = 61)
      LocalObject(366, Door.Constructor(Vector3(2556f, 1470f, 55.38464f)), owning_building_guid = 61)
      LocalObject(367, Door.Constructor(Vector3(2556f, 1470f, 75.38364f)), owning_building_guid = 61)
      LocalObject(2752, Door.Constructor(Vector3(2555.146f, 1450.794f, 45.19965f)), owning_building_guid = 61)
      LocalObject(2753, Door.Constructor(Vector3(2555.146f, 1467.204f, 45.19965f)), owning_building_guid = 61)
      LocalObject(
        1049,
        IFFLock.Constructor(Vector3(2553.957f, 1470.811f, 55.32464f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 366
      )
      LocalObject(
        1050,
        IFFLock.Constructor(Vector3(2553.957f, 1470.811f, 75.32465f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 367
      )
      LocalObject(
        1051,
        IFFLock.Constructor(Vector3(2558.047f, 1453.189f, 55.32464f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 364
      )
      LocalObject(
        1052,
        IFFLock.Constructor(Vector3(2558.047f, 1453.189f, 75.32465f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 365
      )
      LocalObject(1308, Locker.Constructor(Vector3(2559.716f, 1446.963f, 43.85764f)), owning_building_guid = 61)
      LocalObject(1309, Locker.Constructor(Vector3(2559.751f, 1468.835f, 43.85764f)), owning_building_guid = 61)
      LocalObject(1310, Locker.Constructor(Vector3(2561.053f, 1446.963f, 43.85764f)), owning_building_guid = 61)
      LocalObject(1311, Locker.Constructor(Vector3(2561.088f, 1468.835f, 43.85764f)), owning_building_guid = 61)
      LocalObject(1312, Locker.Constructor(Vector3(2563.741f, 1446.963f, 43.85764f)), owning_building_guid = 61)
      LocalObject(1313, Locker.Constructor(Vector3(2563.741f, 1468.835f, 43.85764f)), owning_building_guid = 61)
      LocalObject(1314, Locker.Constructor(Vector3(2565.143f, 1446.963f, 43.85764f)), owning_building_guid = 61)
      LocalObject(1315, Locker.Constructor(Vector3(2565.143f, 1468.835f, 43.85764f)), owning_building_guid = 61)
      LocalObject(
        1866,
        Terminal.Constructor(Vector3(2565.445f, 1452.129f, 45.19564f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1867,
        Terminal.Constructor(Vector3(2565.445f, 1457.853f, 45.19564f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1868,
        Terminal.Constructor(Vector3(2565.445f, 1463.234f, 45.19564f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        2547,
        SpawnTube.Constructor(Vector3(2554.706f, 1449.742f, 43.34564f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        2548,
        SpawnTube.Constructor(Vector3(2554.706f, 1466.152f, 43.34564f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        1696,
        FacilityTurret.Constructor(Vector3(2531.32f, 1449.295f, 72.80564f), manned_turret),
        owning_building_guid = 61
      )
      TurretToWeapon(1696, 5086)
      LocalObject(
        1697,
        FacilityTurret.Constructor(Vector3(2566.647f, 1474.707f, 72.80564f), manned_turret),
        owning_building_guid = 61
      )
      TurretToWeapon(1697, 5087)
      LocalObject(
        2351,
        Painbox.Constructor(Vector3(2549.235f, 1455.803f, 45.36274f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        2353,
        Painbox.Constructor(Vector3(2560.889f, 1464.086f, 43.96365f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        2354,
        Painbox.Constructor(Vector3(2560.975f, 1452.223f, 43.96365f), painbox_radius_continuous),
        owning_building_guid = 61
      )
    }

    Building26()

    def Building26(): Unit = { // Name: W_Ngaru_Tower Type: tower_a GUID: 62, MapID: 26
      LocalBuilding(
        "W_Ngaru_Tower",
        62,
        26,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3398f, 4100f, 357.944f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2627,
        CaptureTerminal.Constructor(Vector3(3414.587f, 4099.897f, 367.943f), secondary_capture),
        owning_building_guid = 62
      )
      LocalObject(380, Door.Constructor(Vector3(3410f, 4092f, 359.465f)), owning_building_guid = 62)
      LocalObject(381, Door.Constructor(Vector3(3410f, 4092f, 379.464f)), owning_building_guid = 62)
      LocalObject(382, Door.Constructor(Vector3(3410f, 4108f, 359.465f)), owning_building_guid = 62)
      LocalObject(383, Door.Constructor(Vector3(3410f, 4108f, 379.464f)), owning_building_guid = 62)
      LocalObject(2758, Door.Constructor(Vector3(3409.146f, 4088.794f, 349.28f)), owning_building_guid = 62)
      LocalObject(2759, Door.Constructor(Vector3(3409.146f, 4105.204f, 349.28f)), owning_building_guid = 62)
      LocalObject(
        1065,
        IFFLock.Constructor(Vector3(3407.957f, 4108.811f, 359.405f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 382
      )
      LocalObject(
        1066,
        IFFLock.Constructor(Vector3(3407.957f, 4108.811f, 379.405f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 383
      )
      LocalObject(
        1067,
        IFFLock.Constructor(Vector3(3412.047f, 4091.189f, 359.405f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 380
      )
      LocalObject(
        1068,
        IFFLock.Constructor(Vector3(3412.047f, 4091.189f, 379.405f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 381
      )
      LocalObject(1332, Locker.Constructor(Vector3(3413.716f, 4084.963f, 347.938f)), owning_building_guid = 62)
      LocalObject(1333, Locker.Constructor(Vector3(3413.751f, 4106.835f, 347.938f)), owning_building_guid = 62)
      LocalObject(1334, Locker.Constructor(Vector3(3415.053f, 4084.963f, 347.938f)), owning_building_guid = 62)
      LocalObject(1335, Locker.Constructor(Vector3(3415.088f, 4106.835f, 347.938f)), owning_building_guid = 62)
      LocalObject(1336, Locker.Constructor(Vector3(3417.741f, 4084.963f, 347.938f)), owning_building_guid = 62)
      LocalObject(1337, Locker.Constructor(Vector3(3417.741f, 4106.835f, 347.938f)), owning_building_guid = 62)
      LocalObject(1338, Locker.Constructor(Vector3(3419.143f, 4084.963f, 347.938f)), owning_building_guid = 62)
      LocalObject(1339, Locker.Constructor(Vector3(3419.143f, 4106.835f, 347.938f)), owning_building_guid = 62)
      LocalObject(
        1875,
        Terminal.Constructor(Vector3(3419.445f, 4090.129f, 349.276f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        1876,
        Terminal.Constructor(Vector3(3419.445f, 4095.853f, 349.276f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        1877,
        Terminal.Constructor(Vector3(3419.445f, 4101.234f, 349.276f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        2553,
        SpawnTube.Constructor(Vector3(3408.706f, 4087.742f, 347.426f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        2554,
        SpawnTube.Constructor(Vector3(3408.706f, 4104.152f, 347.426f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        1698,
        FacilityTurret.Constructor(Vector3(3385.32f, 4087.295f, 376.886f), manned_turret),
        owning_building_guid = 62
      )
      TurretToWeapon(1698, 5088)
      LocalObject(
        1699,
        FacilityTurret.Constructor(Vector3(3420.647f, 4112.707f, 376.886f), manned_turret),
        owning_building_guid = 62
      )
      TurretToWeapon(1699, 5089)
      LocalObject(
        2360,
        Painbox.Constructor(Vector3(3403.235f, 4093.803f, 349.4431f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2361,
        Painbox.Constructor(Vector3(3414.889f, 4102.086f, 348.044f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2362,
        Painbox.Constructor(Vector3(3414.975f, 4090.223f, 348.044f), painbox_radius_continuous),
        owning_building_guid = 62
      )
    }

    Building46()

    def Building46(): Unit = { // Name: NW_Karihi_Tower Type: tower_a GUID: 63, MapID: 46
      LocalBuilding(
        "NW_Karihi_Tower",
        63,
        46,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3578f, 5818f, 215.015f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2628,
        CaptureTerminal.Constructor(Vector3(3594.587f, 5817.897f, 225.014f), secondary_capture),
        owning_building_guid = 63
      )
      LocalObject(384, Door.Constructor(Vector3(3590f, 5810f, 216.536f)), owning_building_guid = 63)
      LocalObject(385, Door.Constructor(Vector3(3590f, 5810f, 236.535f)), owning_building_guid = 63)
      LocalObject(386, Door.Constructor(Vector3(3590f, 5826f, 216.536f)), owning_building_guid = 63)
      LocalObject(387, Door.Constructor(Vector3(3590f, 5826f, 236.535f)), owning_building_guid = 63)
      LocalObject(2760, Door.Constructor(Vector3(3589.146f, 5806.794f, 206.351f)), owning_building_guid = 63)
      LocalObject(2761, Door.Constructor(Vector3(3589.146f, 5823.204f, 206.351f)), owning_building_guid = 63)
      LocalObject(
        1069,
        IFFLock.Constructor(Vector3(3587.957f, 5826.811f, 216.476f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 386
      )
      LocalObject(
        1070,
        IFFLock.Constructor(Vector3(3587.957f, 5826.811f, 236.476f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 387
      )
      LocalObject(
        1071,
        IFFLock.Constructor(Vector3(3592.047f, 5809.189f, 216.476f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 384
      )
      LocalObject(
        1072,
        IFFLock.Constructor(Vector3(3592.047f, 5809.189f, 236.476f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 385
      )
      LocalObject(1340, Locker.Constructor(Vector3(3593.716f, 5802.963f, 205.009f)), owning_building_guid = 63)
      LocalObject(1341, Locker.Constructor(Vector3(3593.751f, 5824.835f, 205.009f)), owning_building_guid = 63)
      LocalObject(1342, Locker.Constructor(Vector3(3595.053f, 5802.963f, 205.009f)), owning_building_guid = 63)
      LocalObject(1343, Locker.Constructor(Vector3(3595.088f, 5824.835f, 205.009f)), owning_building_guid = 63)
      LocalObject(1344, Locker.Constructor(Vector3(3597.741f, 5802.963f, 205.009f)), owning_building_guid = 63)
      LocalObject(1345, Locker.Constructor(Vector3(3597.741f, 5824.835f, 205.009f)), owning_building_guid = 63)
      LocalObject(1346, Locker.Constructor(Vector3(3599.143f, 5802.963f, 205.009f)), owning_building_guid = 63)
      LocalObject(1347, Locker.Constructor(Vector3(3599.143f, 5824.835f, 205.009f)), owning_building_guid = 63)
      LocalObject(
        1878,
        Terminal.Constructor(Vector3(3599.445f, 5808.129f, 206.347f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1879,
        Terminal.Constructor(Vector3(3599.445f, 5813.853f, 206.347f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1880,
        Terminal.Constructor(Vector3(3599.445f, 5819.234f, 206.347f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        2555,
        SpawnTube.Constructor(Vector3(3588.706f, 5805.742f, 204.497f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        2556,
        SpawnTube.Constructor(Vector3(3588.706f, 5822.152f, 204.497f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        1700,
        FacilityTurret.Constructor(Vector3(3565.32f, 5805.295f, 233.957f), manned_turret),
        owning_building_guid = 63
      )
      TurretToWeapon(1700, 5090)
      LocalObject(
        1701,
        FacilityTurret.Constructor(Vector3(3600.647f, 5830.707f, 233.957f), manned_turret),
        owning_building_guid = 63
      )
      TurretToWeapon(1701, 5091)
      LocalObject(
        2363,
        Painbox.Constructor(Vector3(3583.235f, 5811.803f, 206.5141f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        2364,
        Painbox.Constructor(Vector3(3594.889f, 5820.086f, 205.115f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        2365,
        Painbox.Constructor(Vector3(3594.975f, 5808.223f, 205.115f), painbox_radius_continuous),
        owning_building_guid = 63
      )
    }

    Building48()

    def Building48(): Unit = { // Name: E_Drakulu_Tower Type: tower_a GUID: 64, MapID: 48
      LocalBuilding(
        "E_Drakulu_Tower",
        64,
        48,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3992f, 2634f, 157.9653f), Vector3(0f, 0f, 16f), tower_a)
        )
      )
      LocalObject(
        2629,
        CaptureTerminal.Constructor(Vector3(4007.973f, 2638.473f, 167.9643f), secondary_capture),
        owning_building_guid = 64
      )
      LocalObject(435, Door.Constructor(Vector3(4001.33f, 2644.998f, 159.4863f)), owning_building_guid = 64)
      LocalObject(436, Door.Constructor(Vector3(4001.33f, 2644.998f, 179.4853f)), owning_building_guid = 64)
      LocalObject(437, Door.Constructor(Vector3(4005.74f, 2629.618f, 159.4863f)), owning_building_guid = 64)
      LocalObject(438, Door.Constructor(Vector3(4005.74f, 2629.618f, 179.4853f)), owning_building_guid = 64)
      LocalObject(2771, Door.Constructor(Vector3(4001.28f, 2642.075f, 149.3013f)), owning_building_guid = 64)
      LocalObject(2772, Door.Constructor(Vector3(4005.803f, 2626.3f, 149.3013f)), owning_building_guid = 64)
      LocalObject(
        1101,
        IFFLock.Constructor(Vector3(3999.143f, 2645.214f, 159.4263f), Vector3(0, 0, 344)),
        owning_building_guid = 64,
        door_guid = 435
      )
      LocalObject(
        1102,
        IFFLock.Constructor(Vector3(3999.143f, 2645.214f, 179.4263f), Vector3(0, 0, 344)),
        owning_building_guid = 64,
        door_guid = 436
      )
      LocalObject(
        1103,
        IFFLock.Constructor(Vector3(4007.931f, 2629.402f, 159.4263f), Vector3(0, 0, 164)),
        owning_building_guid = 64,
        door_guid = 437
      )
      LocalObject(
        1104,
        IFFLock.Constructor(Vector3(4007.931f, 2629.402f, 179.4263f), Vector3(0, 0, 164)),
        owning_building_guid = 64,
        door_guid = 438
      )
      LocalObject(1393, Locker.Constructor(Vector3(4005.257f, 2644.912f, 147.9593f)), owning_building_guid = 64)
      LocalObject(1394, Locker.Constructor(Vector3(4006.542f, 2645.28f, 147.9593f)), owning_building_guid = 64)
      LocalObject(1395, Locker.Constructor(Vector3(4009.092f, 2646.011f, 147.9593f)), owning_building_guid = 64)
      LocalObject(1396, Locker.Constructor(Vector3(4010.44f, 2646.398f, 147.9593f)), owning_building_guid = 64)
      LocalObject(1397, Locker.Constructor(Vector3(4011.252f, 2623.877f, 147.9593f)), owning_building_guid = 64)
      LocalObject(1398, Locker.Constructor(Vector3(4012.537f, 2624.246f, 147.9593f)), owning_building_guid = 64)
      LocalObject(1399, Locker.Constructor(Vector3(4015.121f, 2624.987f, 147.9593f)), owning_building_guid = 64)
      LocalObject(1400, Locker.Constructor(Vector3(4016.469f, 2625.373f, 147.9593f)), owning_building_guid = 64)
      LocalObject(
        1895,
        Terminal.Constructor(Vector3(4012.274f, 2641.097f, 149.2973f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1896,
        Terminal.Constructor(Vector3(4013.757f, 2635.925f, 149.2973f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1897,
        Terminal.Constructor(Vector3(4015.335f, 2630.422f, 149.2973f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        2566,
        SpawnTube.Constructor(Vector3(4001.147f, 2640.942f, 147.4473f), respawn_tube_tower, Vector3(0, 0, 344)),
        owning_building_guid = 64
      )
      LocalObject(
        2567,
        SpawnTube.Constructor(Vector3(4005.67f, 2625.168f, 147.4473f), respawn_tube_tower, Vector3(0, 0, 344)),
        owning_building_guid = 64
      )
      LocalObject(
        1720,
        FacilityTurret.Constructor(Vector3(3983.313f, 2618.292f, 176.9073f), manned_turret),
        owning_building_guid = 64
      )
      TurretToWeapon(1720, 5092)
      LocalObject(
        1721,
        FacilityTurret.Constructor(Vector3(4010.267f, 2652.457f, 176.9073f), manned_turret),
        owning_building_guid = 64
      )
      TurretToWeapon(1721, 5093)
      LocalObject(
        2366,
        Painbox.Constructor(Vector3(3998.74f, 2629.486f, 149.4644f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        2367,
        Painbox.Constructor(Vector3(4007.66f, 2640.661f, 148.0653f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        2368,
        Painbox.Constructor(Vector3(4011.012f, 2629.28f, 148.0653f), painbox_radius_continuous),
        owning_building_guid = 64
      )
    }

    Building51()

    def Building51(): Unit = { // Name: N_Ngaru_Tower Type: tower_a GUID: 65, MapID: 51
      LocalBuilding(
        "N_Ngaru_Tower",
        65,
        51,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4212f, 4846f, 220.0201f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2630,
        CaptureTerminal.Constructor(Vector3(4228.587f, 4845.897f, 230.0191f), secondary_capture),
        owning_building_guid = 65
      )
      LocalObject(451, Door.Constructor(Vector3(4224f, 4838f, 221.5411f)), owning_building_guid = 65)
      LocalObject(452, Door.Constructor(Vector3(4224f, 4838f, 241.5401f)), owning_building_guid = 65)
      LocalObject(453, Door.Constructor(Vector3(4224f, 4854f, 221.5411f)), owning_building_guid = 65)
      LocalObject(454, Door.Constructor(Vector3(4224f, 4854f, 241.5401f)), owning_building_guid = 65)
      LocalObject(2776, Door.Constructor(Vector3(4223.146f, 4834.794f, 211.3561f)), owning_building_guid = 65)
      LocalObject(2777, Door.Constructor(Vector3(4223.146f, 4851.204f, 211.3561f)), owning_building_guid = 65)
      LocalObject(
        1112,
        IFFLock.Constructor(Vector3(4221.957f, 4854.811f, 221.4811f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 453
      )
      LocalObject(
        1113,
        IFFLock.Constructor(Vector3(4221.957f, 4854.811f, 241.4811f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 454
      )
      LocalObject(
        1114,
        IFFLock.Constructor(Vector3(4226.047f, 4837.189f, 221.4811f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 451
      )
      LocalObject(
        1115,
        IFFLock.Constructor(Vector3(4226.047f, 4837.189f, 241.4811f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 452
      )
      LocalObject(1413, Locker.Constructor(Vector3(4227.716f, 4830.963f, 210.0141f)), owning_building_guid = 65)
      LocalObject(1414, Locker.Constructor(Vector3(4227.751f, 4852.835f, 210.0141f)), owning_building_guid = 65)
      LocalObject(1415, Locker.Constructor(Vector3(4229.053f, 4830.963f, 210.0141f)), owning_building_guid = 65)
      LocalObject(1416, Locker.Constructor(Vector3(4229.088f, 4852.835f, 210.0141f)), owning_building_guid = 65)
      LocalObject(1417, Locker.Constructor(Vector3(4231.741f, 4830.963f, 210.0141f)), owning_building_guid = 65)
      LocalObject(1418, Locker.Constructor(Vector3(4231.741f, 4852.835f, 210.0141f)), owning_building_guid = 65)
      LocalObject(1419, Locker.Constructor(Vector3(4233.143f, 4830.963f, 210.0141f)), owning_building_guid = 65)
      LocalObject(1420, Locker.Constructor(Vector3(4233.143f, 4852.835f, 210.0141f)), owning_building_guid = 65)
      LocalObject(
        1902,
        Terminal.Constructor(Vector3(4233.445f, 4836.129f, 211.3521f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1903,
        Terminal.Constructor(Vector3(4233.445f, 4841.853f, 211.3521f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1904,
        Terminal.Constructor(Vector3(4233.445f, 4847.234f, 211.3521f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        2571,
        SpawnTube.Constructor(Vector3(4222.706f, 4833.742f, 209.5021f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        2572,
        SpawnTube.Constructor(Vector3(4222.706f, 4850.152f, 209.5021f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        1727,
        FacilityTurret.Constructor(Vector3(4199.32f, 4833.295f, 238.9621f), manned_turret),
        owning_building_guid = 65
      )
      TurretToWeapon(1727, 5094)
      LocalObject(
        1728,
        FacilityTurret.Constructor(Vector3(4234.647f, 4858.707f, 238.9621f), manned_turret),
        owning_building_guid = 65
      )
      TurretToWeapon(1728, 5095)
      LocalObject(
        2369,
        Painbox.Constructor(Vector3(4217.235f, 4839.803f, 211.5192f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        2370,
        Painbox.Constructor(Vector3(4228.889f, 4848.086f, 210.1201f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        2371,
        Painbox.Constructor(Vector3(4228.975f, 4836.223f, 210.1201f), painbox_radius_continuous),
        owning_building_guid = 65
      )
    }

    Building44()

    def Building44(): Unit = { // Name: S_Laka_Tower Type: tower_a GUID: 66, MapID: 44
      LocalBuilding(
        "S_Laka_Tower",
        66,
        44,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4362f, 6216f, 53.49857f), Vector3(0f, 0f, 306f), tower_a)
        )
      )
      LocalObject(
        2631,
        CaptureTerminal.Constructor(Vector3(4371.667f, 6202.521f, 63.49757f), secondary_capture),
        owning_building_guid = 66
      )
      LocalObject(456, Door.Constructor(Vector3(4362.581f, 6201.589f, 55.01957f)), owning_building_guid = 66)
      LocalObject(457, Door.Constructor(Vector3(4362.581f, 6201.589f, 75.01857f)), owning_building_guid = 66)
      LocalObject(459, Door.Constructor(Vector3(4375.525f, 6210.994f, 55.01957f)), owning_building_guid = 66)
      LocalObject(460, Door.Constructor(Vector3(4375.525f, 6210.994f, 75.01857f)), owning_building_guid = 66)
      LocalObject(2778, Door.Constructor(Vector3(4359.486f, 6200.396f, 44.83456f)), owning_building_guid = 66)
      LocalObject(2779, Door.Constructor(Vector3(4372.762f, 6210.042f, 44.83456f)), owning_building_guid = 66)
      LocalObject(
        1116,
        IFFLock.Constructor(Vector3(4363.128f, 6199.457f, 54.95956f), Vector3(0, 0, 234)),
        owning_building_guid = 66,
        door_guid = 456
      )
      LocalObject(
        1117,
        IFFLock.Constructor(Vector3(4363.128f, 6199.457f, 74.95956f), Vector3(0, 0, 234)),
        owning_building_guid = 66,
        door_guid = 457
      )
      LocalObject(
        1118,
        IFFLock.Constructor(Vector3(4374.981f, 6213.124f, 54.95956f), Vector3(0, 0, 54)),
        owning_building_guid = 66,
        door_guid = 459
      )
      LocalObject(
        1119,
        IFFLock.Constructor(Vector3(4374.981f, 6213.124f, 74.95956f), Vector3(0, 0, 54)),
        owning_building_guid = 66,
        door_guid = 460
      )
      LocalObject(1421, Locker.Constructor(Vector3(4359.072f, 6194.447f, 43.49257f)), owning_building_guid = 66)
      LocalObject(1422, Locker.Constructor(Vector3(4359.858f, 6193.365f, 43.49257f)), owning_building_guid = 66)
      LocalObject(1423, Locker.Constructor(Vector3(4361.438f, 6191.19f, 43.49257f)), owning_building_guid = 66)
      LocalObject(1424, Locker.Constructor(Vector3(4362.262f, 6190.057f, 43.49257f)), owning_building_guid = 66)
      LocalObject(1425, Locker.Constructor(Vector3(4376.788f, 6207.275f, 43.49257f)), owning_building_guid = 66)
      LocalObject(1426, Locker.Constructor(Vector3(4377.574f, 6206.193f, 43.49257f)), owning_building_guid = 66)
      LocalObject(1427, Locker.Constructor(Vector3(4379.133f, 6204.047f, 43.49257f)), owning_building_guid = 66)
      LocalObject(1428, Locker.Constructor(Vector3(4379.957f, 6202.913f, 43.49257f)), owning_building_guid = 66)
      LocalObject(
        1905,
        Terminal.Constructor(Vector3(4366.619f, 6192.849f, 44.83057f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1906,
        Terminal.Constructor(Vector3(4371.25f, 6196.213f, 44.83057f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1907,
        Terminal.Constructor(Vector3(4375.604f, 6199.376f, 44.83057f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2573,
        SpawnTube.Constructor(Vector3(4358.376f, 6200.134f, 42.98057f), respawn_tube_tower, Vector3(0, 0, 54)),
        owning_building_guid = 66
      )
      LocalObject(
        2574,
        SpawnTube.Constructor(Vector3(4371.652f, 6209.779f, 42.98057f), respawn_tube_tower, Vector3(0, 0, 54)),
        owning_building_guid = 66
      )
      LocalObject(
        1730,
        FacilityTurret.Constructor(Vector3(4344.269f, 6218.791f, 72.44057f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(1730, 5096)
      LocalObject(
        1732,
        FacilityTurret.Constructor(Vector3(4385.592f, 6205.147f, 72.44057f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(1732, 5097)
      LocalObject(
        2372,
        Painbox.Constructor(Vector3(4360.063f, 6208.122f, 44.99767f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2373,
        Painbox.Constructor(Vector3(4364.068f, 6196.52f, 43.59856f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2374,
        Painbox.Constructor(Vector3(4373.615f, 6203.562f, 43.59856f), painbox_radius_continuous),
        owning_building_guid = 66
      )
    }

    Building52()

    def Building52(): Unit = { // Name: NW_Oro_Tower Type: tower_a GUID: 67, MapID: 52
      LocalBuilding(
        "NW_Oro_Tower",
        67,
        52,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4744f, 4694f, 211.0904f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2633,
        CaptureTerminal.Constructor(Vector3(4760.587f, 4693.897f, 221.0894f), secondary_capture),
        owning_building_guid = 67
      )
      LocalObject(506, Door.Constructor(Vector3(4756f, 4686f, 212.6114f)), owning_building_guid = 67)
      LocalObject(507, Door.Constructor(Vector3(4756f, 4686f, 232.6104f)), owning_building_guid = 67)
      LocalObject(508, Door.Constructor(Vector3(4756f, 4702f, 212.6114f)), owning_building_guid = 67)
      LocalObject(509, Door.Constructor(Vector3(4756f, 4702f, 232.6104f)), owning_building_guid = 67)
      LocalObject(2791, Door.Constructor(Vector3(4755.146f, 4682.794f, 202.4264f)), owning_building_guid = 67)
      LocalObject(2792, Door.Constructor(Vector3(4755.146f, 4699.204f, 202.4264f)), owning_building_guid = 67)
      LocalObject(
        1154,
        IFFLock.Constructor(Vector3(4753.957f, 4702.811f, 212.5514f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 508
      )
      LocalObject(
        1155,
        IFFLock.Constructor(Vector3(4753.957f, 4702.811f, 232.5514f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 509
      )
      LocalObject(
        1156,
        IFFLock.Constructor(Vector3(4758.047f, 4685.189f, 212.5514f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 506
      )
      LocalObject(
        1157,
        IFFLock.Constructor(Vector3(4758.047f, 4685.189f, 232.5514f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 507
      )
      LocalObject(1482, Locker.Constructor(Vector3(4759.716f, 4678.963f, 201.0844f)), owning_building_guid = 67)
      LocalObject(1483, Locker.Constructor(Vector3(4759.751f, 4700.835f, 201.0844f)), owning_building_guid = 67)
      LocalObject(1484, Locker.Constructor(Vector3(4761.053f, 4678.963f, 201.0844f)), owning_building_guid = 67)
      LocalObject(1485, Locker.Constructor(Vector3(4761.088f, 4700.835f, 201.0844f)), owning_building_guid = 67)
      LocalObject(1486, Locker.Constructor(Vector3(4763.741f, 4678.963f, 201.0844f)), owning_building_guid = 67)
      LocalObject(1487, Locker.Constructor(Vector3(4763.741f, 4700.835f, 201.0844f)), owning_building_guid = 67)
      LocalObject(1488, Locker.Constructor(Vector3(4765.143f, 4678.963f, 201.0844f)), owning_building_guid = 67)
      LocalObject(1489, Locker.Constructor(Vector3(4765.143f, 4700.835f, 201.0844f)), owning_building_guid = 67)
      LocalObject(
        1928,
        Terminal.Constructor(Vector3(4765.445f, 4684.129f, 202.4224f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1929,
        Terminal.Constructor(Vector3(4765.445f, 4689.853f, 202.4224f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1930,
        Terminal.Constructor(Vector3(4765.445f, 4695.234f, 202.4224f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        2586,
        SpawnTube.Constructor(Vector3(4754.706f, 4681.742f, 200.5724f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        2587,
        SpawnTube.Constructor(Vector3(4754.706f, 4698.152f, 200.5724f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        1750,
        FacilityTurret.Constructor(Vector3(4731.32f, 4681.295f, 230.0324f), manned_turret),
        owning_building_guid = 67
      )
      TurretToWeapon(1750, 5098)
      LocalObject(
        1751,
        FacilityTurret.Constructor(Vector3(4766.647f, 4706.707f, 230.0324f), manned_turret),
        owning_building_guid = 67
      )
      TurretToWeapon(1751, 5099)
      LocalObject(
        2378,
        Painbox.Constructor(Vector3(4749.235f, 4687.803f, 202.5895f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        2379,
        Painbox.Constructor(Vector3(4760.889f, 4696.086f, 201.1904f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        2380,
        Painbox.Constructor(Vector3(4760.975f, 4684.223f, 201.1904f), painbox_radius_continuous),
        owning_building_guid = 67
      )
    }

    Building45()

    def Building45(): Unit = { // Name: E_Hiro_Tower Type: tower_a GUID: 68, MapID: 45
      LocalBuilding(
        "E_Hiro_Tower",
        68,
        45,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4852f, 5700f, 177.862f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2635,
        CaptureTerminal.Constructor(Vector3(4868.587f, 5699.897f, 187.861f), secondary_capture),
        owning_building_guid = 68
      )
      LocalObject(521, Door.Constructor(Vector3(4864f, 5692f, 179.383f)), owning_building_guid = 68)
      LocalObject(522, Door.Constructor(Vector3(4864f, 5692f, 199.382f)), owning_building_guid = 68)
      LocalObject(523, Door.Constructor(Vector3(4864f, 5708f, 179.383f)), owning_building_guid = 68)
      LocalObject(524, Door.Constructor(Vector3(4864f, 5708f, 199.382f)), owning_building_guid = 68)
      LocalObject(2795, Door.Constructor(Vector3(4863.146f, 5688.794f, 169.198f)), owning_building_guid = 68)
      LocalObject(2796, Door.Constructor(Vector3(4863.146f, 5705.204f, 169.198f)), owning_building_guid = 68)
      LocalObject(
        1164,
        IFFLock.Constructor(Vector3(4861.957f, 5708.811f, 179.323f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 523
      )
      LocalObject(
        1165,
        IFFLock.Constructor(Vector3(4861.957f, 5708.811f, 199.323f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 524
      )
      LocalObject(
        1166,
        IFFLock.Constructor(Vector3(4866.047f, 5691.189f, 179.323f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 521
      )
      LocalObject(
        1167,
        IFFLock.Constructor(Vector3(4866.047f, 5691.189f, 199.323f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 522
      )
      LocalObject(1498, Locker.Constructor(Vector3(4867.716f, 5684.963f, 167.856f)), owning_building_guid = 68)
      LocalObject(1499, Locker.Constructor(Vector3(4867.751f, 5706.835f, 167.856f)), owning_building_guid = 68)
      LocalObject(1500, Locker.Constructor(Vector3(4869.053f, 5684.963f, 167.856f)), owning_building_guid = 68)
      LocalObject(1501, Locker.Constructor(Vector3(4869.088f, 5706.835f, 167.856f)), owning_building_guid = 68)
      LocalObject(1502, Locker.Constructor(Vector3(4871.741f, 5684.963f, 167.856f)), owning_building_guid = 68)
      LocalObject(1503, Locker.Constructor(Vector3(4871.741f, 5706.835f, 167.856f)), owning_building_guid = 68)
      LocalObject(1504, Locker.Constructor(Vector3(4873.143f, 5684.963f, 167.856f)), owning_building_guid = 68)
      LocalObject(1505, Locker.Constructor(Vector3(4873.143f, 5706.835f, 167.856f)), owning_building_guid = 68)
      LocalObject(
        1935,
        Terminal.Constructor(Vector3(4873.445f, 5690.129f, 169.194f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1936,
        Terminal.Constructor(Vector3(4873.445f, 5695.853f, 169.194f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1937,
        Terminal.Constructor(Vector3(4873.445f, 5701.234f, 169.194f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        2590,
        SpawnTube.Constructor(Vector3(4862.706f, 5687.742f, 167.344f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        2591,
        SpawnTube.Constructor(Vector3(4862.706f, 5704.152f, 167.344f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        1754,
        FacilityTurret.Constructor(Vector3(4839.32f, 5687.295f, 196.804f), manned_turret),
        owning_building_guid = 68
      )
      TurretToWeapon(1754, 5100)
      LocalObject(
        1755,
        FacilityTurret.Constructor(Vector3(4874.647f, 5712.707f, 196.804f), manned_turret),
        owning_building_guid = 68
      )
      TurretToWeapon(1755, 5101)
      LocalObject(
        2384,
        Painbox.Constructor(Vector3(4857.235f, 5693.803f, 169.3611f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        2385,
        Painbox.Constructor(Vector3(4868.889f, 5702.086f, 167.9621f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        2386,
        Painbox.Constructor(Vector3(4868.975f, 5690.223f, 167.9621f), painbox_radius_continuous),
        owning_building_guid = 68
      )
    }

    Building19()

    def Building19(): Unit = { // Name: NE_Iva_Tower Type: tower_a GUID: 69, MapID: 19
      LocalBuilding(
        "NE_Iva_Tower",
        69,
        19,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(7004f, 5834f, 43.82611f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2641,
        CaptureTerminal.Constructor(Vector3(7020.587f, 5833.897f, 53.82511f), secondary_capture),
        owning_building_guid = 69
      )
      LocalObject(619, Door.Constructor(Vector3(7016f, 5826f, 45.34711f)), owning_building_guid = 69)
      LocalObject(620, Door.Constructor(Vector3(7016f, 5826f, 65.34611f)), owning_building_guid = 69)
      LocalObject(621, Door.Constructor(Vector3(7016f, 5842f, 45.34711f)), owning_building_guid = 69)
      LocalObject(622, Door.Constructor(Vector3(7016f, 5842f, 65.34611f)), owning_building_guid = 69)
      LocalObject(2822, Door.Constructor(Vector3(7015.146f, 5822.794f, 35.16211f)), owning_building_guid = 69)
      LocalObject(2823, Door.Constructor(Vector3(7015.146f, 5839.204f, 35.16211f)), owning_building_guid = 69)
      LocalObject(
        1240,
        IFFLock.Constructor(Vector3(7013.957f, 5842.811f, 45.28711f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 621
      )
      LocalObject(
        1241,
        IFFLock.Constructor(Vector3(7013.957f, 5842.811f, 65.28711f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 622
      )
      LocalObject(
        1242,
        IFFLock.Constructor(Vector3(7018.047f, 5825.189f, 45.28711f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 619
      )
      LocalObject(
        1243,
        IFFLock.Constructor(Vector3(7018.047f, 5825.189f, 65.28711f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 620
      )
      LocalObject(1624, Locker.Constructor(Vector3(7019.716f, 5818.963f, 33.82011f)), owning_building_guid = 69)
      LocalObject(1625, Locker.Constructor(Vector3(7019.751f, 5840.835f, 33.82011f)), owning_building_guid = 69)
      LocalObject(1626, Locker.Constructor(Vector3(7021.053f, 5818.963f, 33.82011f)), owning_building_guid = 69)
      LocalObject(1627, Locker.Constructor(Vector3(7021.088f, 5840.835f, 33.82011f)), owning_building_guid = 69)
      LocalObject(1628, Locker.Constructor(Vector3(7023.741f, 5818.963f, 33.82011f)), owning_building_guid = 69)
      LocalObject(1629, Locker.Constructor(Vector3(7023.741f, 5840.835f, 33.82011f)), owning_building_guid = 69)
      LocalObject(1630, Locker.Constructor(Vector3(7025.143f, 5818.963f, 33.82011f)), owning_building_guid = 69)
      LocalObject(1631, Locker.Constructor(Vector3(7025.143f, 5840.835f, 33.82011f)), owning_building_guid = 69)
      LocalObject(
        1981,
        Terminal.Constructor(Vector3(7025.445f, 5824.129f, 35.15811f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1982,
        Terminal.Constructor(Vector3(7025.445f, 5829.853f, 35.15811f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1983,
        Terminal.Constructor(Vector3(7025.445f, 5835.234f, 35.15811f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2617,
        SpawnTube.Constructor(Vector3(7014.706f, 5821.742f, 33.30811f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        2618,
        SpawnTube.Constructor(Vector3(7014.706f, 5838.152f, 33.30811f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        1794,
        FacilityTurret.Constructor(Vector3(6991.32f, 5821.295f, 62.7681f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(1794, 5102)
      LocalObject(
        1795,
        FacilityTurret.Constructor(Vector3(7026.647f, 5846.707f, 62.7681f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(1795, 5103)
      LocalObject(
        2402,
        Painbox.Constructor(Vector3(7009.235f, 5827.803f, 35.32521f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2403,
        Painbox.Constructor(Vector3(7020.889f, 5836.086f, 33.92611f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2404,
        Painbox.Constructor(Vector3(7020.975f, 5824.223f, 33.92611f), painbox_radius_continuous),
        owning_building_guid = 69
      )
    }

    Building53()

    def Building53(): Unit = { // Name: S_Ishundar_Warpgate_Tower Type: tower_b GUID: 70, MapID: 53
      LocalBuilding(
        "S_Ishundar_Warpgate_Tower",
        70,
        53,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1258f, 6332f, 41.90735f), Vector3(0f, 0f, 5f), tower_b)
        )
      )
      LocalObject(
        2620,
        CaptureTerminal.Constructor(Vector3(1274.533f, 6333.343f, 61.90635f), secondary_capture),
        owning_building_guid = 70
      )
      LocalObject(331, Door.Constructor(Vector3(1269.257f, 6341.016f, 43.42735f)), owning_building_guid = 70)
      LocalObject(332, Door.Constructor(Vector3(1269.257f, 6341.016f, 53.42735f)), owning_building_guid = 70)
      LocalObject(333, Door.Constructor(Vector3(1269.257f, 6341.016f, 73.42735f)), owning_building_guid = 70)
      LocalObject(334, Door.Constructor(Vector3(1270.652f, 6325.076f, 43.42735f)), owning_building_guid = 70)
      LocalObject(335, Door.Constructor(Vector3(1270.652f, 6325.076f, 53.42735f)), owning_building_guid = 70)
      LocalObject(336, Door.Constructor(Vector3(1270.652f, 6325.076f, 73.42735f)), owning_building_guid = 70)
      LocalObject(2741, Door.Constructor(Vector3(1268.651f, 6338.156f, 33.24335f)), owning_building_guid = 70)
      LocalObject(2742, Door.Constructor(Vector3(1270.081f, 6321.808f, 33.24335f)), owning_building_guid = 70)
      LocalObject(
        1018,
        IFFLock.Constructor(Vector3(1267.151f, 6341.646f, 43.36835f), Vector3(0, 0, 355)),
        owning_building_guid = 70,
        door_guid = 331
      )
      LocalObject(
        1019,
        IFFLock.Constructor(Vector3(1267.151f, 6341.646f, 53.36835f), Vector3(0, 0, 355)),
        owning_building_guid = 70,
        door_guid = 332
      )
      LocalObject(
        1020,
        IFFLock.Constructor(Vector3(1267.151f, 6341.646f, 73.36835f), Vector3(0, 0, 355)),
        owning_building_guid = 70,
        door_guid = 333
      )
      LocalObject(
        1021,
        IFFLock.Constructor(Vector3(1272.761f, 6324.447f, 43.36835f), Vector3(0, 0, 175)),
        owning_building_guid = 70,
        door_guid = 334
      )
      LocalObject(
        1022,
        IFFLock.Constructor(Vector3(1272.761f, 6324.447f, 53.36835f), Vector3(0, 0, 175)),
        owning_building_guid = 70,
        door_guid = 335
      )
      LocalObject(
        1023,
        IFFLock.Constructor(Vector3(1272.761f, 6324.447f, 73.36835f), Vector3(0, 0, 175)),
        owning_building_guid = 70,
        door_guid = 336
      )
      LocalObject(1264, Locker.Constructor(Vector3(1273.095f, 6340.182f, 31.90135f)), owning_building_guid = 70)
      LocalObject(1265, Locker.Constructor(Vector3(1274.427f, 6340.298f, 31.90135f)), owning_building_guid = 70)
      LocalObject(1266, Locker.Constructor(Vector3(1274.967f, 6318.39f, 31.90135f)), owning_building_guid = 70)
      LocalObject(1267, Locker.Constructor(Vector3(1276.299f, 6318.506f, 31.90135f)), owning_building_guid = 70)
      LocalObject(1268, Locker.Constructor(Vector3(1277.07f, 6340.529f, 31.90135f)), owning_building_guid = 70)
      LocalObject(1269, Locker.Constructor(Vector3(1278.467f, 6340.652f, 31.90135f)), owning_building_guid = 70)
      LocalObject(1270, Locker.Constructor(Vector3(1278.976f, 6318.741f, 31.90135f)), owning_building_guid = 70)
      LocalObject(1271, Locker.Constructor(Vector3(1280.373f, 6318.863f, 31.90135f)), owning_building_guid = 70)
      LocalObject(
        1847,
        Terminal.Constructor(Vector3(1279.257f, 6335.099f, 33.23935f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1848,
        Terminal.Constructor(Vector3(1279.726f, 6329.738f, 33.23935f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1849,
        Terminal.Constructor(Vector3(1280.225f, 6324.036f, 33.23935f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        2536,
        SpawnTube.Constructor(Vector3(1268.303f, 6337.069f, 31.38935f), respawn_tube_tower, Vector3(0, 0, 355)),
        owning_building_guid = 70
      )
      LocalObject(
        2537,
        SpawnTube.Constructor(Vector3(1269.734f, 6320.722f, 31.38935f), respawn_tube_tower, Vector3(0, 0, 355)),
        owning_building_guid = 70
      )
      LocalObject(
        2339,
        Painbox.Constructor(Vector3(1264.095f, 6325.355f, 33.19675f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2340,
        Painbox.Constructor(Vector3(1275.01f, 6335.603f, 32.00735f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2341,
        Painbox.Constructor(Vector3(1275.927f, 6323.608f, 32.00735f), painbox_radius_continuous),
        owning_building_guid = 70
      )
    }

    Building28()

    def Building28(): Unit = { // Name: NE_Esamir_Warpgate_Tower Type: tower_b GUID: 71, MapID: 28
      LocalBuilding(
        "NE_Esamir_Warpgate_Tower",
        71,
        28,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2312f, 2572f, 47.95525f), Vector3(0f, 0f, 334f), tower_b)
        )
      )
      LocalObject(
        2623,
        CaptureTerminal.Constructor(Vector3(2326.863f, 2564.636f, 67.95425f), secondary_capture),
        owning_building_guid = 71
      )
      LocalObject(358, Door.Constructor(Vector3(2319.279f, 2559.549f, 49.47525f)), owning_building_guid = 71)
      LocalObject(359, Door.Constructor(Vector3(2319.279f, 2559.549f, 59.47525f)), owning_building_guid = 71)
      LocalObject(360, Door.Constructor(Vector3(2319.279f, 2559.549f, 79.47525f)), owning_building_guid = 71)
      LocalObject(361, Door.Constructor(Vector3(2326.292f, 2573.93f, 49.47525f)), owning_building_guid = 71)
      LocalObject(362, Door.Constructor(Vector3(2326.292f, 2573.93f, 59.47525f)), owning_building_guid = 71)
      LocalObject(363, Door.Constructor(Vector3(2326.292f, 2573.93f, 79.47525f)), owning_building_guid = 71)
      LocalObject(2750, Door.Constructor(Vector3(2317.106f, 2557.042f, 39.29125f)), owning_building_guid = 71)
      LocalObject(2751, Door.Constructor(Vector3(2324.3f, 2571.791f, 39.29125f)), owning_building_guid = 71)
      LocalObject(
        1043,
        IFFLock.Constructor(Vector3(2320.763f, 2557.923f, 49.41625f), Vector3(0, 0, 206)),
        owning_building_guid = 71,
        door_guid = 358
      )
      LocalObject(
        1044,
        IFFLock.Constructor(Vector3(2320.763f, 2557.923f, 59.41625f), Vector3(0, 0, 206)),
        owning_building_guid = 71,
        door_guid = 359
      )
      LocalObject(
        1045,
        IFFLock.Constructor(Vector3(2320.763f, 2557.923f, 79.41625f), Vector3(0, 0, 206)),
        owning_building_guid = 71,
        door_guid = 360
      )
      LocalObject(
        1046,
        IFFLock.Constructor(Vector3(2324.812f, 2575.554f, 49.41625f), Vector3(0, 0, 26)),
        owning_building_guid = 71,
        door_guid = 361
      )
      LocalObject(
        1047,
        IFFLock.Constructor(Vector3(2324.812f, 2575.554f, 59.41625f), Vector3(0, 0, 26)),
        owning_building_guid = 71,
        door_guid = 362
      )
      LocalObject(
        1048,
        IFFLock.Constructor(Vector3(2324.812f, 2575.554f, 79.41625f), Vector3(0, 0, 26)),
        owning_building_guid = 71,
        door_guid = 363
      )
      LocalObject(1300, Locker.Constructor(Vector3(2319.534f, 2551.595f, 37.94925f)), owning_building_guid = 71)
      LocalObject(1301, Locker.Constructor(Vector3(2320.735f, 2551.009f, 37.94925f)), owning_building_guid = 71)
      LocalObject(1302, Locker.Constructor(Vector3(2323.151f, 2549.831f, 37.94925f)), owning_building_guid = 71)
      LocalObject(1303, Locker.Constructor(Vector3(2324.411f, 2549.216f, 37.94925f)), owning_building_guid = 71)
      LocalObject(1304, Locker.Constructor(Vector3(2329.153f, 2571.239f, 37.94925f)), owning_building_guid = 71)
      LocalObject(1305, Locker.Constructor(Vector3(2330.355f, 2570.652f, 37.94925f)), owning_building_guid = 71)
      LocalObject(1306, Locker.Constructor(Vector3(2332.739f, 2569.489f, 37.94925f)), owning_building_guid = 71)
      LocalObject(1307, Locker.Constructor(Vector3(2334f, 2568.875f, 37.94925f)), owning_building_guid = 71)
      LocalObject(
        1863,
        Terminal.Constructor(Vector3(2326.948f, 2553.727f, 39.28725f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1864,
        Terminal.Constructor(Vector3(2329.458f, 2558.871f, 39.28725f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1865,
        Terminal.Constructor(Vector3(2331.816f, 2563.708f, 39.28725f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        2545,
        SpawnTube.Constructor(Vector3(2316.249f, 2556.289f, 37.43726f), respawn_tube_tower, Vector3(0, 0, 26)),
        owning_building_guid = 71
      )
      LocalObject(
        2546,
        SpawnTube.Constructor(Vector3(2323.443f, 2571.039f, 37.43726f), respawn_tube_tower, Vector3(0, 0, 26)),
        owning_building_guid = 71
      )
      LocalObject(
        2348,
        Painbox.Constructor(Vector3(2313.802f, 2563.165f, 39.24465f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2349,
        Painbox.Constructor(Vector3(2323.044f, 2555.574f, 38.05525f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2350,
        Painbox.Constructor(Vector3(2328.436f, 2566.327f, 38.05525f), painbox_radius_continuous),
        owning_building_guid = 71
      )
    }

    Building24()

    def Building24(): Unit = { // Name: E_Ishundar_Warpgate_Tower Type: tower_b GUID: 72, MapID: 24
      LocalBuilding(
        "E_Ishundar_Warpgate_Tower",
        72,
        24,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2552f, 6916f, 52.0414f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2625,
        CaptureTerminal.Constructor(Vector3(2568.587f, 6915.897f, 72.04041f), secondary_capture),
        owning_building_guid = 72
      )
      LocalObject(368, Door.Constructor(Vector3(2564f, 6908f, 53.56141f)), owning_building_guid = 72)
      LocalObject(369, Door.Constructor(Vector3(2564f, 6908f, 63.56141f)), owning_building_guid = 72)
      LocalObject(370, Door.Constructor(Vector3(2564f, 6908f, 83.5614f)), owning_building_guid = 72)
      LocalObject(371, Door.Constructor(Vector3(2564f, 6924f, 53.56141f)), owning_building_guid = 72)
      LocalObject(372, Door.Constructor(Vector3(2564f, 6924f, 63.56141f)), owning_building_guid = 72)
      LocalObject(373, Door.Constructor(Vector3(2564f, 6924f, 83.5614f)), owning_building_guid = 72)
      LocalObject(2754, Door.Constructor(Vector3(2563.147f, 6904.794f, 43.3774f)), owning_building_guid = 72)
      LocalObject(2755, Door.Constructor(Vector3(2563.147f, 6921.204f, 43.3774f)), owning_building_guid = 72)
      LocalObject(
        1053,
        IFFLock.Constructor(Vector3(2561.957f, 6924.811f, 53.5024f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 371
      )
      LocalObject(
        1054,
        IFFLock.Constructor(Vector3(2561.957f, 6924.811f, 63.5024f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 372
      )
      LocalObject(
        1055,
        IFFLock.Constructor(Vector3(2561.957f, 6924.811f, 83.5024f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 373
      )
      LocalObject(
        1056,
        IFFLock.Constructor(Vector3(2566.047f, 6907.189f, 53.5024f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 368
      )
      LocalObject(
        1057,
        IFFLock.Constructor(Vector3(2566.047f, 6907.189f, 63.5024f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 369
      )
      LocalObject(
        1058,
        IFFLock.Constructor(Vector3(2566.047f, 6907.189f, 83.5024f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 370
      )
      LocalObject(1316, Locker.Constructor(Vector3(2567.716f, 6900.963f, 42.0354f)), owning_building_guid = 72)
      LocalObject(1317, Locker.Constructor(Vector3(2567.751f, 6922.835f, 42.0354f)), owning_building_guid = 72)
      LocalObject(1318, Locker.Constructor(Vector3(2569.053f, 6900.963f, 42.0354f)), owning_building_guid = 72)
      LocalObject(1319, Locker.Constructor(Vector3(2569.088f, 6922.835f, 42.0354f)), owning_building_guid = 72)
      LocalObject(1320, Locker.Constructor(Vector3(2571.741f, 6900.963f, 42.0354f)), owning_building_guid = 72)
      LocalObject(1321, Locker.Constructor(Vector3(2571.741f, 6922.835f, 42.0354f)), owning_building_guid = 72)
      LocalObject(1322, Locker.Constructor(Vector3(2573.143f, 6900.963f, 42.0354f)), owning_building_guid = 72)
      LocalObject(1323, Locker.Constructor(Vector3(2573.143f, 6922.835f, 42.0354f)), owning_building_guid = 72)
      LocalObject(
        1869,
        Terminal.Constructor(Vector3(2573.446f, 6906.129f, 43.37341f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1870,
        Terminal.Constructor(Vector3(2573.446f, 6911.853f, 43.37341f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1871,
        Terminal.Constructor(Vector3(2573.446f, 6917.234f, 43.37341f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2549,
        SpawnTube.Constructor(Vector3(2562.706f, 6903.742f, 41.52341f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        2550,
        SpawnTube.Constructor(Vector3(2562.706f, 6920.152f, 41.52341f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        2352,
        Painbox.Constructor(Vector3(2557.493f, 6908.849f, 43.3308f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2355,
        Painbox.Constructor(Vector3(2569.127f, 6906.078f, 42.1414f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2356,
        Painbox.Constructor(Vector3(2569.259f, 6918.107f, 42.1414f), painbox_radius_continuous),
        owning_building_guid = 72
      )
    }

    Building49()

    def Building49(): Unit = { // Name: W_Rehua_Tower Type: tower_b GUID: 73, MapID: 49
      LocalBuilding(
        "W_Rehua_Tower",
        73,
        49,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3354f, 1956f, 54.89647f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2626,
        CaptureTerminal.Constructor(Vector3(3370.587f, 1955.897f, 74.89546f), secondary_capture),
        owning_building_guid = 73
      )
      LocalObject(374, Door.Constructor(Vector3(3366f, 1948f, 56.41647f)), owning_building_guid = 73)
      LocalObject(375, Door.Constructor(Vector3(3366f, 1948f, 66.41647f)), owning_building_guid = 73)
      LocalObject(376, Door.Constructor(Vector3(3366f, 1948f, 86.41647f)), owning_building_guid = 73)
      LocalObject(377, Door.Constructor(Vector3(3366f, 1964f, 56.41647f)), owning_building_guid = 73)
      LocalObject(378, Door.Constructor(Vector3(3366f, 1964f, 66.41647f)), owning_building_guid = 73)
      LocalObject(379, Door.Constructor(Vector3(3366f, 1964f, 86.41647f)), owning_building_guid = 73)
      LocalObject(2756, Door.Constructor(Vector3(3365.147f, 1944.794f, 46.23247f)), owning_building_guid = 73)
      LocalObject(2757, Door.Constructor(Vector3(3365.147f, 1961.204f, 46.23247f)), owning_building_guid = 73)
      LocalObject(
        1059,
        IFFLock.Constructor(Vector3(3363.957f, 1964.811f, 56.35746f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 377
      )
      LocalObject(
        1060,
        IFFLock.Constructor(Vector3(3363.957f, 1964.811f, 66.35747f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 378
      )
      LocalObject(
        1061,
        IFFLock.Constructor(Vector3(3363.957f, 1964.811f, 86.35747f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 379
      )
      LocalObject(
        1062,
        IFFLock.Constructor(Vector3(3368.047f, 1947.189f, 56.35746f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 374
      )
      LocalObject(
        1063,
        IFFLock.Constructor(Vector3(3368.047f, 1947.189f, 66.35747f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 375
      )
      LocalObject(
        1064,
        IFFLock.Constructor(Vector3(3368.047f, 1947.189f, 86.35747f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 376
      )
      LocalObject(1324, Locker.Constructor(Vector3(3369.716f, 1940.963f, 44.89046f)), owning_building_guid = 73)
      LocalObject(1325, Locker.Constructor(Vector3(3369.751f, 1962.835f, 44.89046f)), owning_building_guid = 73)
      LocalObject(1326, Locker.Constructor(Vector3(3371.053f, 1940.963f, 44.89046f)), owning_building_guid = 73)
      LocalObject(1327, Locker.Constructor(Vector3(3371.088f, 1962.835f, 44.89046f)), owning_building_guid = 73)
      LocalObject(1328, Locker.Constructor(Vector3(3373.741f, 1940.963f, 44.89046f)), owning_building_guid = 73)
      LocalObject(1329, Locker.Constructor(Vector3(3373.741f, 1962.835f, 44.89046f)), owning_building_guid = 73)
      LocalObject(1330, Locker.Constructor(Vector3(3375.143f, 1940.963f, 44.89046f)), owning_building_guid = 73)
      LocalObject(1331, Locker.Constructor(Vector3(3375.143f, 1962.835f, 44.89046f)), owning_building_guid = 73)
      LocalObject(
        1872,
        Terminal.Constructor(Vector3(3375.446f, 1946.129f, 46.22847f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1873,
        Terminal.Constructor(Vector3(3375.446f, 1951.853f, 46.22847f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1874,
        Terminal.Constructor(Vector3(3375.446f, 1957.234f, 46.22847f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        2551,
        SpawnTube.Constructor(Vector3(3364.706f, 1943.742f, 44.37846f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        2552,
        SpawnTube.Constructor(Vector3(3364.706f, 1960.152f, 44.37846f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        2357,
        Painbox.Constructor(Vector3(3359.493f, 1948.849f, 46.18587f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2358,
        Painbox.Constructor(Vector3(3371.127f, 1946.078f, 44.99647f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2359,
        Painbox.Constructor(Vector3(3371.259f, 1958.107f, 44.99647f), painbox_radius_continuous),
        owning_building_guid = 73
      )
    }

    Building22()

    def Building22(): Unit = { // Name: W_Sina_Tower Type: tower_b GUID: 74, MapID: 22
      LocalBuilding(
        "W_Sina_Tower",
        74,
        22,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4834f, 1886f, 56.61641f), Vector3(0f, 0f, 16f), tower_b)
        )
      )
      LocalObject(
        2634,
        CaptureTerminal.Constructor(Vector3(4849.973f, 1890.473f, 76.61542f), secondary_capture),
        owning_building_guid = 74
      )
      LocalObject(514, Door.Constructor(Vector3(4843.33f, 1896.998f, 58.13641f)), owning_building_guid = 74)
      LocalObject(515, Door.Constructor(Vector3(4843.33f, 1896.998f, 68.13641f)), owning_building_guid = 74)
      LocalObject(516, Door.Constructor(Vector3(4843.33f, 1896.998f, 88.13641f)), owning_building_guid = 74)
      LocalObject(517, Door.Constructor(Vector3(4847.74f, 1881.618f, 58.13641f)), owning_building_guid = 74)
      LocalObject(518, Door.Constructor(Vector3(4847.74f, 1881.618f, 68.13641f)), owning_building_guid = 74)
      LocalObject(519, Door.Constructor(Vector3(4847.74f, 1881.618f, 88.13641f)), owning_building_guid = 74)
      LocalObject(2793, Door.Constructor(Vector3(4843.281f, 1894.075f, 47.95242f)), owning_building_guid = 74)
      LocalObject(2794, Door.Constructor(Vector3(4847.804f, 1878.301f, 47.95242f)), owning_building_guid = 74)
      LocalObject(
        1158,
        IFFLock.Constructor(Vector3(4841.143f, 1897.214f, 58.07741f), Vector3(0, 0, 344)),
        owning_building_guid = 74,
        door_guid = 514
      )
      LocalObject(
        1159,
        IFFLock.Constructor(Vector3(4841.143f, 1897.214f, 68.07742f), Vector3(0, 0, 344)),
        owning_building_guid = 74,
        door_guid = 515
      )
      LocalObject(
        1160,
        IFFLock.Constructor(Vector3(4841.143f, 1897.214f, 88.07742f), Vector3(0, 0, 344)),
        owning_building_guid = 74,
        door_guid = 516
      )
      LocalObject(
        1161,
        IFFLock.Constructor(Vector3(4849.932f, 1881.402f, 58.07741f), Vector3(0, 0, 164)),
        owning_building_guid = 74,
        door_guid = 517
      )
      LocalObject(
        1162,
        IFFLock.Constructor(Vector3(4849.932f, 1881.402f, 68.07742f), Vector3(0, 0, 164)),
        owning_building_guid = 74,
        door_guid = 518
      )
      LocalObject(
        1163,
        IFFLock.Constructor(Vector3(4849.932f, 1881.402f, 88.07742f), Vector3(0, 0, 164)),
        owning_building_guid = 74,
        door_guid = 519
      )
      LocalObject(1490, Locker.Constructor(Vector3(4847.257f, 1896.912f, 46.61041f)), owning_building_guid = 74)
      LocalObject(1491, Locker.Constructor(Vector3(4848.542f, 1897.28f, 46.61041f)), owning_building_guid = 74)
      LocalObject(1492, Locker.Constructor(Vector3(4851.092f, 1898.012f, 46.61041f)), owning_building_guid = 74)
      LocalObject(1493, Locker.Constructor(Vector3(4852.44f, 1898.398f, 46.61041f)), owning_building_guid = 74)
      LocalObject(1494, Locker.Constructor(Vector3(4853.252f, 1875.877f, 46.61041f)), owning_building_guid = 74)
      LocalObject(1495, Locker.Constructor(Vector3(4854.537f, 1876.246f, 46.61041f)), owning_building_guid = 74)
      LocalObject(1496, Locker.Constructor(Vector3(4857.121f, 1876.987f, 46.61041f)), owning_building_guid = 74)
      LocalObject(1497, Locker.Constructor(Vector3(4858.469f, 1877.373f, 46.61041f)), owning_building_guid = 74)
      LocalObject(
        1931,
        Terminal.Constructor(Vector3(4854.275f, 1893.098f, 47.94841f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        1932,
        Terminal.Constructor(Vector3(4855.758f, 1887.925f, 47.94841f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        1933,
        Terminal.Constructor(Vector3(4857.336f, 1882.423f, 47.94841f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        2588,
        SpawnTube.Constructor(Vector3(4843.147f, 1892.942f, 46.09841f), respawn_tube_tower, Vector3(0, 0, 344)),
        owning_building_guid = 74
      )
      LocalObject(
        2589,
        SpawnTube.Constructor(Vector3(4847.67f, 1877.168f, 46.09841f), respawn_tube_tower, Vector3(0, 0, 344)),
        owning_building_guid = 74
      )
      LocalObject(
        2381,
        Painbox.Constructor(Vector3(4841.251f, 1880.64f, 47.90582f), painbox_radius_continuous),
        owning_building_guid = 74
      )
      LocalObject(
        2382,
        Painbox.Constructor(Vector3(4850.01f, 1892.782f, 46.71642f), painbox_radius_continuous),
        owning_building_guid = 74
      )
      LocalObject(
        2383,
        Painbox.Constructor(Vector3(4853.199f, 1881.183f, 46.71642f), painbox_radius_continuous),
        owning_building_guid = 74
      )
    }

    Building23()

    def Building23(): Unit = { // Name: SE_Tara_Tower Type: tower_c GUID: 75, MapID: 23
      LocalBuilding(
        "SE_Tara_Tower",
        75,
        23,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1772f, 3744f, 43.52397f), Vector3(0f, 0f, 55f), tower_c)
        )
      )
      LocalObject(
        2621,
        CaptureTerminal.Constructor(Vector3(1781.598f, 3757.528f, 53.52297f), secondary_capture),
        owning_building_guid = 75
      )
      LocalObject(348, Door.Constructor(Vector3(1772.33f, 3758.418f, 45.04497f)), owning_building_guid = 75)
      LocalObject(349, Door.Constructor(Vector3(1772.33f, 3758.418f, 65.04398f)), owning_building_guid = 75)
      LocalObject(350, Door.Constructor(Vector3(1785.436f, 3749.241f, 45.04497f)), owning_building_guid = 75)
      LocalObject(351, Door.Constructor(Vector3(1785.436f, 3749.241f, 65.04398f)), owning_building_guid = 75)
      LocalObject(2743, Door.Constructor(Vector3(1774.13f, 3756.115f, 34.85997f)), owning_building_guid = 75)
      LocalObject(2747, Door.Constructor(Vector3(1787.573f, 3746.703f, 34.85997f)), owning_building_guid = 75)
      LocalObject(
        1029,
        IFFLock.Constructor(Vector3(1770.494f, 3757.21f, 44.98497f), Vector3(0, 0, 305)),
        owning_building_guid = 75,
        door_guid = 348
      )
      LocalObject(
        1030,
        IFFLock.Constructor(Vector3(1770.494f, 3757.21f, 64.98497f), Vector3(0, 0, 305)),
        owning_building_guid = 75,
        door_guid = 349
      )
      LocalObject(
        1036,
        IFFLock.Constructor(Vector3(1787.275f, 3750.453f, 44.98497f), Vector3(0, 0, 125)),
        owning_building_guid = 75,
        door_guid = 350
      )
      LocalObject(
        1037,
        IFFLock.Constructor(Vector3(1787.275f, 3750.453f, 64.98497f), Vector3(0, 0, 125)),
        owning_building_guid = 75,
        door_guid = 351
      )
      LocalObject(1272, Locker.Constructor(Vector3(1775.436f, 3760.823f, 33.51797f)), owning_building_guid = 75)
      LocalObject(1273, Locker.Constructor(Vector3(1776.202f, 3761.918f, 33.51797f)), owning_building_guid = 75)
      LocalObject(1274, Locker.Constructor(Vector3(1777.724f, 3764.091f, 33.51797f)), owning_building_guid = 75)
      LocalObject(1275, Locker.Constructor(Vector3(1778.528f, 3765.24f, 33.51797f)), owning_building_guid = 75)
      LocalObject(1280, Locker.Constructor(Vector3(1793.332f, 3748.249f, 33.51797f)), owning_building_guid = 75)
      LocalObject(1281, Locker.Constructor(Vector3(1794.099f, 3749.344f, 33.51797f)), owning_building_guid = 75)
      LocalObject(1282, Locker.Constructor(Vector3(1795.641f, 3751.546f, 33.51797f)), owning_building_guid = 75)
      LocalObject(1284, Locker.Constructor(Vector3(1796.445f, 3752.695f, 33.51797f)), owning_building_guid = 75)
      LocalObject(
        1854,
        Terminal.Constructor(Vector3(1783.29f, 3762.274f, 34.85597f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        1855,
        Terminal.Constructor(Vector3(1787.697f, 3759.188f, 34.85597f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        1859,
        Terminal.Constructor(Vector3(1792.386f, 3755.905f, 34.85597f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2541,
        SpawnTube.Constructor(Vector3(1774.74f, 3755.151f, 33.00597f), respawn_tube_tower, Vector3(0, 0, 305)),
        owning_building_guid = 75
      )
      LocalObject(
        2542,
        SpawnTube.Constructor(Vector3(1788.182f, 3745.739f, 33.00597f), respawn_tube_tower, Vector3(0, 0, 305)),
        owning_building_guid = 75
      )
      LocalObject(
        2210,
        ProximityTerminal.Constructor(Vector3(1767.138f, 3746.07f, 71.09397f), pad_landing_tower_frame),
        owning_building_guid = 75
      )
      LocalObject(
        2211,
        Terminal.Constructor(Vector3(1767.138f, 3746.07f, 71.09397f), air_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2213,
        ProximityTerminal.Constructor(Vector3(1775.694f, 3740.079f, 71.09397f), pad_landing_tower_frame),
        owning_building_guid = 75
      )
      LocalObject(
        2214,
        Terminal.Constructor(Vector3(1775.694f, 3740.079f, 71.09397f), air_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        1690,
        FacilityTurret.Constructor(Vector3(1773.225f, 3771.827f, 62.46597f), manned_turret),
        owning_building_guid = 75
      )
      TurretToWeapon(1690, 5104)
      LocalObject(
        1691,
        FacilityTurret.Constructor(Vector3(1775.687f, 3723.192f, 62.46597f), manned_turret),
        owning_building_guid = 75
      )
      TurretToWeapon(1691, 5105)
      LocalObject(
        2342,
        Painbox.Constructor(Vector3(1780.159f, 3759.178f, 33.62397f), painbox_radius_continuous),
        owning_building_guid = 75
      )
      LocalObject(
        2343,
        Painbox.Constructor(Vector3(1780.412f, 3743.547f, 35.54347f), painbox_radius_continuous),
        owning_building_guid = 75
      )
      LocalObject(
        2344,
        Painbox.Constructor(Vector3(1790.275f, 3751.863f, 33.62397f), painbox_radius_continuous),
        owning_building_guid = 75
      )
    }

    Building21()

    def Building21(): Unit = { // Name: NE_Pele_Tower Type: tower_c GUID: 76, MapID: 21
      LocalBuilding(
        "NE_Pele_Tower",
        76,
        21,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4686f, 3958f, 193.4062f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2632,
        CaptureTerminal.Constructor(Vector3(4702.587f, 3957.897f, 203.4052f), secondary_capture),
        owning_building_guid = 76
      )
      LocalObject(502, Door.Constructor(Vector3(4698f, 3950f, 194.9272f)), owning_building_guid = 76)
      LocalObject(503, Door.Constructor(Vector3(4698f, 3950f, 214.9262f)), owning_building_guid = 76)
      LocalObject(504, Door.Constructor(Vector3(4698f, 3966f, 194.9272f)), owning_building_guid = 76)
      LocalObject(505, Door.Constructor(Vector3(4698f, 3966f, 214.9262f)), owning_building_guid = 76)
      LocalObject(2789, Door.Constructor(Vector3(4697.146f, 3946.794f, 184.7422f)), owning_building_guid = 76)
      LocalObject(2790, Door.Constructor(Vector3(4697.146f, 3963.204f, 184.7422f)), owning_building_guid = 76)
      LocalObject(
        1150,
        IFFLock.Constructor(Vector3(4695.957f, 3966.811f, 194.8672f), Vector3(0, 0, 0)),
        owning_building_guid = 76,
        door_guid = 504
      )
      LocalObject(
        1151,
        IFFLock.Constructor(Vector3(4695.957f, 3966.811f, 214.8672f), Vector3(0, 0, 0)),
        owning_building_guid = 76,
        door_guid = 505
      )
      LocalObject(
        1152,
        IFFLock.Constructor(Vector3(4700.047f, 3949.189f, 194.8672f), Vector3(0, 0, 180)),
        owning_building_guid = 76,
        door_guid = 502
      )
      LocalObject(
        1153,
        IFFLock.Constructor(Vector3(4700.047f, 3949.189f, 214.8672f), Vector3(0, 0, 180)),
        owning_building_guid = 76,
        door_guid = 503
      )
      LocalObject(1474, Locker.Constructor(Vector3(4701.716f, 3942.963f, 183.4002f)), owning_building_guid = 76)
      LocalObject(1475, Locker.Constructor(Vector3(4701.751f, 3964.835f, 183.4002f)), owning_building_guid = 76)
      LocalObject(1476, Locker.Constructor(Vector3(4703.053f, 3942.963f, 183.4002f)), owning_building_guid = 76)
      LocalObject(1477, Locker.Constructor(Vector3(4703.088f, 3964.835f, 183.4002f)), owning_building_guid = 76)
      LocalObject(1478, Locker.Constructor(Vector3(4705.741f, 3942.963f, 183.4002f)), owning_building_guid = 76)
      LocalObject(1479, Locker.Constructor(Vector3(4705.741f, 3964.835f, 183.4002f)), owning_building_guid = 76)
      LocalObject(1480, Locker.Constructor(Vector3(4707.143f, 3942.963f, 183.4002f)), owning_building_guid = 76)
      LocalObject(1481, Locker.Constructor(Vector3(4707.143f, 3964.835f, 183.4002f)), owning_building_guid = 76)
      LocalObject(
        1925,
        Terminal.Constructor(Vector3(4707.445f, 3948.129f, 184.7382f), order_terminal),
        owning_building_guid = 76
      )
      LocalObject(
        1926,
        Terminal.Constructor(Vector3(4707.445f, 3953.853f, 184.7382f), order_terminal),
        owning_building_guid = 76
      )
      LocalObject(
        1927,
        Terminal.Constructor(Vector3(4707.445f, 3959.234f, 184.7382f), order_terminal),
        owning_building_guid = 76
      )
      LocalObject(
        2584,
        SpawnTube.Constructor(Vector3(4696.706f, 3945.742f, 182.8882f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 76
      )
      LocalObject(
        2585,
        SpawnTube.Constructor(Vector3(4696.706f, 3962.152f, 182.8882f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 76
      )
      LocalObject(
        2216,
        ProximityTerminal.Constructor(Vector3(4684.907f, 3952.725f, 220.9762f), pad_landing_tower_frame),
        owning_building_guid = 76
      )
      LocalObject(
        2217,
        Terminal.Constructor(Vector3(4684.907f, 3952.725f, 220.9762f), air_rearm_terminal),
        owning_building_guid = 76
      )
      LocalObject(
        2219,
        ProximityTerminal.Constructor(Vector3(4684.907f, 3963.17f, 220.9762f), pad_landing_tower_frame),
        owning_building_guid = 76
      )
      LocalObject(
        2220,
        Terminal.Constructor(Vector3(4684.907f, 3963.17f, 220.9762f), air_rearm_terminal),
        owning_building_guid = 76
      )
      LocalObject(
        1745,
        FacilityTurret.Constructor(Vector3(4671.07f, 3943.045f, 212.3482f), manned_turret),
        owning_building_guid = 76
      )
      TurretToWeapon(1745, 5106)
      LocalObject(
        1748,
        FacilityTurret.Constructor(Vector3(4709.497f, 3972.957f, 212.3482f), manned_turret),
        owning_building_guid = 76
      )
      TurretToWeapon(1748, 5107)
      LocalObject(
        2375,
        Painbox.Constructor(Vector3(4690.454f, 3950.849f, 185.4257f), painbox_radius_continuous),
        owning_building_guid = 76
      )
      LocalObject(
        2376,
        Painbox.Constructor(Vector3(4702.923f, 3947.54f, 183.5062f), painbox_radius_continuous),
        owning_building_guid = 76
      )
      LocalObject(
        2377,
        Painbox.Constructor(Vector3(4703.113f, 3960.022f, 183.5062f), painbox_radius_continuous),
        owning_building_guid = 76
      )
    }

    Building25()

    def Building25(): Unit = { // Name: SE_Laka_Tower Type: tower_c GUID: 77, MapID: 25
      LocalBuilding(
        "SE_Laka_Tower",
        77,
        25,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5268f, 6372f, 48.24061f), Vector3(0f, 0f, 11f), tower_c)
        )
      )
      LocalObject(
        2636,
        CaptureTerminal.Constructor(Vector3(5284.302f, 6375.064f, 58.23961f), secondary_capture),
        owning_building_guid = 77
      )
      LocalObject(552, Door.Constructor(Vector3(5278.253f, 6382.143f, 49.76161f)), owning_building_guid = 77)
      LocalObject(553, Door.Constructor(Vector3(5278.253f, 6382.143f, 69.7606f)), owning_building_guid = 77)
      LocalObject(555, Door.Constructor(Vector3(5281.306f, 6366.437f, 49.76161f)), owning_building_guid = 77)
      LocalObject(556, Door.Constructor(Vector3(5281.306f, 6366.437f, 69.7606f)), owning_building_guid = 77)
      LocalObject(2806, Door.Constructor(Vector3(5277.948f, 6379.235f, 39.57661f)), owning_building_guid = 77)
      LocalObject(2807, Door.Constructor(Vector3(5281.08f, 6363.126f, 39.57661f)), owning_building_guid = 77)
      LocalObject(
        1193,
        IFFLock.Constructor(Vector3(5276.093f, 6382.549f, 49.70161f), Vector3(0, 0, 349)),
        owning_building_guid = 77,
        door_guid = 552
      )
      LocalObject(
        1194,
        IFFLock.Constructor(Vector3(5276.093f, 6382.549f, 69.70161f), Vector3(0, 0, 349)),
        owning_building_guid = 77,
        door_guid = 553
      )
      LocalObject(
        1198,
        IFFLock.Constructor(Vector3(5283.47f, 6366.031f, 49.70161f), Vector3(0, 0, 169)),
        owning_building_guid = 77,
        door_guid = 555
      )
      LocalObject(
        1199,
        IFFLock.Constructor(Vector3(5283.47f, 6366.031f, 69.70161f), Vector3(0, 0, 169)),
        owning_building_guid = 77,
        door_guid = 556
      )
      LocalObject(1541, Locker.Constructor(Vector3(5282.157f, 6381.715f, 38.23461f)), owning_building_guid = 77)
      LocalObject(1544, Locker.Constructor(Vector3(5283.47f, 6381.97f, 38.23461f)), owning_building_guid = 77)
      LocalObject(1547, Locker.Constructor(Vector3(5286.074f, 6382.476f, 38.23461f)), owning_building_guid = 77)
      LocalObject(1548, Locker.Constructor(Vector3(5286.296f, 6360.238f, 38.23461f)), owning_building_guid = 77)
      LocalObject(1549, Locker.Constructor(Vector3(5287.45f, 6382.744f, 38.23461f)), owning_building_guid = 77)
      LocalObject(1550, Locker.Constructor(Vector3(5287.609f, 6360.493f, 38.23461f)), owning_building_guid = 77)
      LocalObject(1551, Locker.Constructor(Vector3(5290.248f, 6361.006f, 38.23461f)), owning_building_guid = 77)
      LocalObject(1552, Locker.Constructor(Vector3(5291.624f, 6361.273f, 38.23461f)), owning_building_guid = 77)
      LocalObject(
        1954,
        Terminal.Constructor(Vector3(5288.815f, 6377.303f, 39.57261f), order_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        1955,
        Terminal.Constructor(Vector3(5289.842f, 6372.021f, 39.57261f), order_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        1956,
        Terminal.Constructor(Vector3(5290.935f, 6366.402f, 39.57261f), order_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        2601,
        SpawnTube.Constructor(Vector3(5277.717f, 6378.119f, 37.72261f), respawn_tube_tower, Vector3(0, 0, 349)),
        owning_building_guid = 77
      )
      LocalObject(
        2602,
        SpawnTube.Constructor(Vector3(5280.848f, 6362.01f, 37.72261f), respawn_tube_tower, Vector3(0, 0, 349)),
        owning_building_guid = 77
      )
      LocalObject(
        2222,
        ProximityTerminal.Constructor(Vector3(5265.94f, 6376.867f, 75.81061f), pad_landing_tower_frame),
        owning_building_guid = 77
      )
      LocalObject(
        2223,
        Terminal.Constructor(Vector3(5265.94f, 6376.867f, 75.81061f), air_rearm_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        2225,
        ProximityTerminal.Constructor(Vector3(5267.934f, 6366.613f, 75.81061f), pad_landing_tower_frame),
        owning_building_guid = 77
      )
      LocalObject(
        2226,
        Terminal.Constructor(Vector3(5267.934f, 6366.613f, 75.81061f), air_rearm_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        1764,
        FacilityTurret.Constructor(Vector3(5256.198f, 6354.471f, 67.18261f), manned_turret),
        owning_building_guid = 77
      )
      TurretToWeapon(1764, 5108)
      LocalObject(
        1767,
        FacilityTurret.Constructor(Vector3(5288.211f, 6391.166f, 67.18261f), manned_turret),
        owning_building_guid = 77
      )
      TurretToWeapon(1767, 5109)
      LocalObject(
        2387,
        Painbox.Constructor(Vector3(5273.737f, 6365.83f, 40.26011f), painbox_radius_continuous),
        owning_building_guid = 77
      )
      LocalObject(
        2388,
        Painbox.Constructor(Vector3(5284.413f, 6377.25f, 38.34061f), painbox_radius_continuous),
        owning_building_guid = 77
      )
      LocalObject(
        2389,
        Painbox.Constructor(Vector3(5286.608f, 6364.961f, 38.34061f), painbox_radius_continuous),
        owning_building_guid = 77
      )
    }

    Building29()

    def Building29(): Unit = { // Name: NE_Matagi_Tower Type: tower_c GUID: 78, MapID: 29
      LocalBuilding(
        "NE_Matagi_Tower",
        78,
        29,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5380f, 5216f, 236.7046f), Vector3(0f, 0f, 33f), tower_c)
        )
      )
      LocalObject(
        2637,
        CaptureTerminal.Constructor(Vector3(5393.967f, 5224.948f, 246.7036f), secondary_capture),
        owning_building_guid = 78
      )
      LocalObject(567, Door.Constructor(Vector3(5385.707f, 5229.245f, 238.2256f)), owning_building_guid = 78)
      LocalObject(568, Door.Constructor(Vector3(5385.707f, 5229.245f, 258.2246f)), owning_building_guid = 78)
      LocalObject(569, Door.Constructor(Vector3(5394.421f, 5215.826f, 238.2256f)), owning_building_guid = 78)
      LocalObject(570, Door.Constructor(Vector3(5394.421f, 5215.826f, 258.2246f)), owning_building_guid = 78)
      LocalObject(2808, Door.Constructor(Vector3(5386.514f, 5226.435f, 228.0406f)), owning_building_guid = 78)
      LocalObject(2809, Door.Constructor(Vector3(5395.451f, 5212.672f, 228.0406f)), owning_building_guid = 78)
      LocalObject(
        1202,
        IFFLock.Constructor(Vector3(5383.552f, 5228.812f, 238.1656f), Vector3(0, 0, 327)),
        owning_building_guid = 78,
        door_guid = 567
      )
      LocalObject(
        1203,
        IFFLock.Constructor(Vector3(5383.552f, 5228.812f, 258.1656f), Vector3(0, 0, 327)),
        owning_building_guid = 78,
        door_guid = 568
      )
      LocalObject(
        1204,
        IFFLock.Constructor(Vector3(5396.58f, 5216.261f, 238.1656f), Vector3(0, 0, 147)),
        owning_building_guid = 78,
        door_guid = 569
      )
      LocalObject(
        1205,
        IFFLock.Constructor(Vector3(5396.58f, 5216.261f, 258.1656f), Vector3(0, 0, 147)),
        owning_building_guid = 78,
        door_guid = 570
      )
      LocalObject(1559, Locker.Constructor(Vector3(5389.487f, 5230.311f, 226.6986f)), owning_building_guid = 78)
      LocalObject(1560, Locker.Constructor(Vector3(5390.608f, 5231.039f, 226.6986f)), owning_building_guid = 78)
      LocalObject(1561, Locker.Constructor(Vector3(5392.833f, 5232.484f, 226.6986f)), owning_building_guid = 78)
      LocalObject(1562, Locker.Constructor(Vector3(5394.009f, 5233.248f, 226.6986f)), owning_building_guid = 78)
      LocalObject(1563, Locker.Constructor(Vector3(5401.37f, 5211.948f, 226.6986f)), owning_building_guid = 78)
      LocalObject(1564, Locker.Constructor(Vector3(5402.492f, 5212.677f, 226.6986f)), owning_building_guid = 78)
      LocalObject(1565, Locker.Constructor(Vector3(5404.746f, 5214.141f, 226.6986f)), owning_building_guid = 78)
      LocalObject(1566, Locker.Constructor(Vector3(5405.922f, 5214.904f, 226.6986f)), owning_building_guid = 78)
      LocalObject(
        1957,
        Terminal.Constructor(Vector3(5397.313f, 5228.715f, 228.0366f), order_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        1958,
        Terminal.Constructor(Vector3(5400.244f, 5224.202f, 228.0366f), order_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        1959,
        Terminal.Constructor(Vector3(5403.361f, 5219.401f, 228.0366f), order_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        2603,
        SpawnTube.Constructor(Vector3(5386.717f, 5225.313f, 226.1866f), respawn_tube_tower, Vector3(0, 0, 327)),
        owning_building_guid = 78
      )
      LocalObject(
        2604,
        SpawnTube.Constructor(Vector3(5395.655f, 5211.55f, 226.1866f), respawn_tube_tower, Vector3(0, 0, 327)),
        owning_building_guid = 78
      )
      LocalObject(
        2228,
        ProximityTerminal.Constructor(Vector3(5376.268f, 5219.741f, 264.2746f), pad_landing_tower_frame),
        owning_building_guid = 78
      )
      LocalObject(
        2229,
        Terminal.Constructor(Vector3(5376.268f, 5219.741f, 264.2746f), air_rearm_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        2231,
        ProximityTerminal.Constructor(Vector3(5381.957f, 5210.981f, 264.2746f), pad_landing_tower_frame),
        owning_building_guid = 78
      )
      LocalObject(
        2232,
        Terminal.Constructor(Vector3(5381.957f, 5210.981f, 264.2746f), air_rearm_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        1772,
        FacilityTurret.Constructor(Vector3(5375.624f, 5195.326f, 255.6466f), manned_turret),
        owning_building_guid = 78
      )
      TurretToWeapon(1772, 5110)
      LocalObject(
        1774,
        FacilityTurret.Constructor(Vector3(5391.56f, 5241.341f, 255.6466f), manned_turret),
        owning_building_guid = 78
      )
      TurretToWeapon(1774, 5111)
      LocalObject(
        2390,
        Painbox.Constructor(Vector3(5387.63f, 5212.429f, 228.7241f), painbox_radius_continuous),
        owning_building_guid = 78
      )
      LocalObject(
        2391,
        Painbox.Constructor(Vector3(5393.251f, 5227.016f, 226.8046f), painbox_radius_continuous),
        owning_building_guid = 78
      )
      LocalObject(
        2392,
        Painbox.Constructor(Vector3(5399.89f, 5216.444f, 226.8046f), painbox_radius_continuous),
        owning_building_guid = 78
      )
    }

    Building37()

    def Building37(): Unit = { // Name: NE_Sina_Tower Type: tower_c GUID: 79, MapID: 37
      LocalBuilding(
        "NE_Sina_Tower",
        79,
        37,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6202f, 2442f, 49.96948f), Vector3(0f, 0f, 296f), tower_c)
        )
      )
      LocalObject(
        2638,
        CaptureTerminal.Constructor(Vector3(6209.179f, 2427.047f, 59.96848f), secondary_capture),
        owning_building_guid = 79
      )
      LocalObject(593, Door.Constructor(Vector3(6200.07f, 2427.708f, 51.49048f)), owning_building_guid = 79)
      LocalObject(594, Door.Constructor(Vector3(6200.07f, 2427.708f, 71.48949f)), owning_building_guid = 79)
      LocalObject(595, Door.Constructor(Vector3(6214.451f, 2434.721f, 51.49048f)), owning_building_guid = 79)
      LocalObject(596, Door.Constructor(Vector3(6214.451f, 2434.721f, 71.48949f)), owning_building_guid = 79)
      LocalObject(2813, Door.Constructor(Vector3(6196.814f, 2427.07f, 41.30548f)), owning_building_guid = 79)
      LocalObject(2814, Door.Constructor(Vector3(6211.563f, 2434.263f, 41.30548f)), owning_building_guid = 79)
      LocalObject(
        1220,
        IFFLock.Constructor(Vector3(6200.238f, 2425.512f, 51.43048f), Vector3(0, 0, 244)),
        owning_building_guid = 79,
        door_guid = 593
      )
      LocalObject(
        1221,
        IFFLock.Constructor(Vector3(6200.238f, 2425.512f, 71.43048f), Vector3(0, 0, 244)),
        owning_building_guid = 79,
        door_guid = 594
      )
      LocalObject(
        1222,
        IFFLock.Constructor(Vector3(6214.284f, 2436.913f, 51.43048f), Vector3(0, 0, 64)),
        owning_building_guid = 79,
        door_guid = 595
      )
      LocalObject(
        1223,
        IFFLock.Constructor(Vector3(6214.284f, 2436.913f, 71.43048f), Vector3(0, 0, 64)),
        owning_building_guid = 79,
        door_guid = 596
      )
      LocalObject(1579, Locker.Constructor(Vector3(6195.375f, 2421.283f, 39.96348f)), owning_building_guid = 79)
      LocalObject(1580, Locker.Constructor(Vector3(6195.96f, 2420.081f, 39.96348f)), owning_building_guid = 79)
      LocalObject(1581, Locker.Constructor(Vector3(6197.139f, 2417.665f, 39.96348f)), owning_building_guid = 79)
      LocalObject(1582, Locker.Constructor(Vector3(6197.753f, 2416.405f, 39.96348f)), owning_building_guid = 79)
      LocalObject(1583, Locker.Constructor(Vector3(6215.048f, 2430.839f, 39.96348f)), owning_building_guid = 79)
      LocalObject(1584, Locker.Constructor(Vector3(6215.634f, 2429.638f, 39.96348f)), owning_building_guid = 79)
      LocalObject(1585, Locker.Constructor(Vector3(6216.797f, 2427.253f, 39.96348f)), owning_building_guid = 79)
      LocalObject(1586, Locker.Constructor(Vector3(6217.412f, 2425.993f, 39.96348f)), owning_building_guid = 79)
      LocalObject(
        1968,
        Terminal.Constructor(Vector3(6202.529f, 2418.398f, 41.30148f), order_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        1969,
        Terminal.Constructor(Vector3(6207.673f, 2420.907f, 41.30148f), order_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        1970,
        Terminal.Constructor(Vector3(6212.51f, 2423.266f, 41.30148f), order_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        2608,
        SpawnTube.Constructor(Vector3(6195.676f, 2427.004f, 39.45148f), respawn_tube_tower, Vector3(0, 0, 64)),
        owning_building_guid = 79
      )
      LocalObject(
        2609,
        SpawnTube.Constructor(Vector3(6210.425f, 2434.198f, 39.45148f), respawn_tube_tower, Vector3(0, 0, 64)),
        owning_building_guid = 79
      )
      LocalObject(
        2234,
        ProximityTerminal.Constructor(Vector3(6196.78f, 2440.67f, 77.53948f), pad_landing_tower_frame),
        owning_building_guid = 79
      )
      LocalObject(
        2235,
        Terminal.Constructor(Vector3(6196.78f, 2440.67f, 77.53948f), air_rearm_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        2237,
        ProximityTerminal.Constructor(Vector3(6206.167f, 2445.249f, 77.53948f), pad_landing_tower_frame),
        owning_building_guid = 79
      )
      LocalObject(
        2238,
        Terminal.Constructor(Vector3(6206.167f, 2445.249f, 77.53948f), air_rearm_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        1783,
        FacilityTurret.Constructor(Vector3(6182.014f, 2448.863f, 68.91148f), manned_turret),
        owning_building_guid = 79
      )
      TurretToWeapon(1783, 5112)
      LocalObject(
        1784,
        FacilityTurret.Constructor(Vector3(6225.744f, 2427.438f, 68.91148f), manned_turret),
        owning_building_guid = 79
      )
      TurretToWeapon(1784, 5113)
      LocalObject(
        2393,
        Painbox.Constructor(Vector3(6197.525f, 2434.862f, 41.98898f), painbox_radius_continuous),
        owning_building_guid = 79
      )
      LocalObject(
        2394,
        Painbox.Constructor(Vector3(6200.017f, 2422.204f, 40.06948f), painbox_radius_continuous),
        owning_building_guid = 79
      )
      LocalObject(
        2395,
        Painbox.Constructor(Vector3(6211.319f, 2427.505f, 40.06948f), painbox_radius_continuous),
        owning_building_guid = 79
      )
    }

    Building27()

    def Building27(): Unit = { // Name: S_Iva_Tower Type: tower_c GUID: 80, MapID: 27
      LocalBuilding(
        "S_Iva_Tower",
        80,
        27,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6438f, 4958f, 63.71655f), Vector3(0f, 0f, 324f), tower_c)
        )
      )
      LocalObject(
        2639,
        CaptureTerminal.Constructor(Vector3(6451.358f, 4948.167f, 73.71555f), secondary_capture),
        owning_building_guid = 80
      )
      LocalObject(603, Door.Constructor(Vector3(6443.006f, 4944.475f, 65.23755f)), owning_building_guid = 80)
      LocalObject(604, Door.Constructor(Vector3(6443.006f, 4944.475f, 85.23654f)), owning_building_guid = 80)
      LocalObject(607, Door.Constructor(Vector3(6452.411f, 4957.419f, 65.23755f)), owning_building_guid = 80)
      LocalObject(608, Door.Constructor(Vector3(6452.411f, 4957.419f, 85.23654f)), owning_building_guid = 80)
      LocalObject(2817, Door.Constructor(Vector3(6440.431f, 4942.383f, 55.05254f)), owning_building_guid = 80)
      LocalObject(2819, Door.Constructor(Vector3(6450.076f, 4955.659f, 55.05254f)), owning_building_guid = 80)
      LocalObject(
        1228,
        IFFLock.Constructor(Vector3(6444.185f, 4942.615f, 65.17754f), Vector3(0, 0, 216)),
        owning_building_guid = 80,
        door_guid = 603
      )
      LocalObject(
        1229,
        IFFLock.Constructor(Vector3(6444.185f, 4942.615f, 85.17754f), Vector3(0, 0, 216)),
        owning_building_guid = 80,
        door_guid = 604
      )
      LocalObject(
        1232,
        IFFLock.Constructor(Vector3(6451.234f, 4959.276f, 65.17754f), Vector3(0, 0, 36)),
        owning_building_guid = 80,
        door_guid = 607
      )
      LocalObject(
        1233,
        IFFLock.Constructor(Vector3(6451.234f, 4959.276f, 85.17754f), Vector3(0, 0, 36)),
        owning_building_guid = 80,
        door_guid = 608
      )
      LocalObject(1598, Locker.Constructor(Vector3(6441.876f, 4936.597f, 53.71054f)), owning_building_guid = 80)
      LocalObject(1599, Locker.Constructor(Vector3(6442.958f, 4935.812f, 53.71054f)), owning_building_guid = 80)
      LocalObject(1600, Locker.Constructor(Vector3(6445.132f, 4934.231f, 53.71054f)), owning_building_guid = 80)
      LocalObject(1601, Locker.Constructor(Vector3(6446.267f, 4933.407f, 53.71054f)), owning_building_guid = 80)
      LocalObject(1612, Locker.Constructor(Vector3(6454.76f, 4954.271f, 53.71054f)), owning_building_guid = 80)
      LocalObject(1613, Locker.Constructor(Vector3(6455.842f, 4953.485f, 53.71054f)), owning_building_guid = 80)
      LocalObject(1614, Locker.Constructor(Vector3(6457.988f, 4951.926f, 53.71054f)), owning_building_guid = 80)
      LocalObject(1615, Locker.Constructor(Vector3(6459.123f, 4951.102f, 53.71054f)), owning_building_guid = 80)
      LocalObject(
        1975,
        Terminal.Constructor(Vector3(6449.547f, 4937.409f, 55.04855f), order_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        1976,
        Terminal.Constructor(Vector3(6452.912f, 4942.04f, 55.04855f), order_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        1977,
        Terminal.Constructor(Vector3(6456.075f, 4946.393f, 55.04855f), order_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        2611,
        SpawnTube.Constructor(Vector3(6439.456f, 4941.79f, 53.19855f), respawn_tube_tower, Vector3(0, 0, 36)),
        owning_building_guid = 80
      )
      LocalObject(
        2614,
        SpawnTube.Constructor(Vector3(6449.102f, 4955.066f, 53.19855f), respawn_tube_tower, Vector3(0, 0, 36)),
        owning_building_guid = 80
      )
      LocalObject(
        2240,
        ProximityTerminal.Constructor(Vector3(6434.015f, 4954.375f, 91.28654f), pad_landing_tower_frame),
        owning_building_guid = 80
      )
      LocalObject(
        2241,
        Terminal.Constructor(Vector3(6434.015f, 4954.375f, 91.28654f), air_rearm_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        2243,
        ProximityTerminal.Constructor(Vector3(6440.155f, 4962.825f, 91.28654f), pad_landing_tower_frame),
        owning_building_guid = 80
      )
      LocalObject(
        2244,
        Terminal.Constructor(Vector3(6440.155f, 4962.825f, 91.28654f), air_rearm_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        1788,
        FacilityTurret.Constructor(Vector3(6417.131f, 4954.677f, 82.65855f), manned_turret),
        owning_building_guid = 80
      )
      TurretToWeapon(1788, 5114)
      LocalObject(
        1789,
        FacilityTurret.Constructor(Vector3(6465.801f, 4956.289f, 82.65855f), manned_turret),
        owning_building_guid = 80
      )
      TurretToWeapon(1789, 5115)
      LocalObject(
        2396,
        Painbox.Constructor(Vector3(6437.4f, 4949.597f, 55.73605f), painbox_radius_continuous),
        owning_building_guid = 80
      )
      LocalObject(
        2397,
        Painbox.Constructor(Vector3(6445.542f, 4939.59f, 53.81654f), painbox_radius_continuous),
        owning_building_guid = 80
      )
      LocalObject(
        2398,
        Painbox.Constructor(Vector3(6453.033f, 4949.577f, 53.81654f), painbox_radius_continuous),
        owning_building_guid = 80
      )
    }

    Building50()

    def Building50(): Unit = { // Name: W_Cyssor_Warpgate_Tower Type: tower_c GUID: 81, MapID: 50
      LocalBuilding(
        "W_Cyssor_Warpgate_Tower",
        81,
        50,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6630f, 3188f, 51.50481f), Vector3(0f, 0f, 15f), tower_c)
        )
      )
      LocalObject(
        2640,
        CaptureTerminal.Constructor(Vector3(6646.048f, 3192.194f, 61.50381f), secondary_capture),
        owning_building_guid = 81
      )
      LocalObject(615, Door.Constructor(Vector3(6639.521f, 3198.833f, 53.02581f)), owning_building_guid = 81)
      LocalObject(616, Door.Constructor(Vector3(6639.521f, 3198.833f, 73.02481f)), owning_building_guid = 81)
      LocalObject(617, Door.Constructor(Vector3(6643.662f, 3183.378f, 53.02581f)), owning_building_guid = 81)
      LocalObject(618, Door.Constructor(Vector3(6643.662f, 3183.378f, 73.02481f)), owning_building_guid = 81)
      LocalObject(2820, Door.Constructor(Vector3(6639.419f, 3195.911f, 42.84081f)), owning_building_guid = 81)
      LocalObject(2821, Door.Constructor(Vector3(6643.667f, 3180.061f, 42.84081f)), owning_building_guid = 81)
      LocalObject(
        1236,
        IFFLock.Constructor(Vector3(6637.337f, 3199.088f, 52.96581f), Vector3(0, 0, 345)),
        owning_building_guid = 81,
        door_guid = 615
      )
      LocalObject(
        1237,
        IFFLock.Constructor(Vector3(6637.337f, 3199.088f, 72.96581f), Vector3(0, 0, 345)),
        owning_building_guid = 81,
        door_guid = 616
      )
      LocalObject(
        1238,
        IFFLock.Constructor(Vector3(6645.849f, 3183.125f, 52.96581f), Vector3(0, 0, 165)),
        owning_building_guid = 81,
        door_guid = 617
      )
      LocalObject(
        1239,
        IFFLock.Constructor(Vector3(6645.849f, 3183.125f, 72.96581f), Vector3(0, 0, 165)),
        owning_building_guid = 81,
        door_guid = 618
      )
      LocalObject(1616, Locker.Constructor(Vector3(6643.445f, 3198.679f, 41.49881f)), owning_building_guid = 81)
      LocalObject(1617, Locker.Constructor(Vector3(6644.737f, 3199.025f, 41.49881f)), owning_building_guid = 81)
      LocalObject(1618, Locker.Constructor(Vector3(6647.299f, 3199.711f, 41.49881f)), owning_building_guid = 81)
      LocalObject(1619, Locker.Constructor(Vector3(6648.653f, 3200.074f, 41.49881f)), owning_building_guid = 81)
      LocalObject(1620, Locker.Constructor(Vector3(6649.072f, 3177.543f, 41.49881f)), owning_building_guid = 81)
      LocalObject(1621, Locker.Constructor(Vector3(6650.364f, 3177.889f, 41.49881f)), owning_building_guid = 81)
      LocalObject(1622, Locker.Constructor(Vector3(6652.96f, 3178.585f, 41.49881f)), owning_building_guid = 81)
      LocalObject(1623, Locker.Constructor(Vector3(6654.314f, 3178.948f, 41.49881f)), owning_building_guid = 81)
      LocalObject(
        1978,
        Terminal.Constructor(Vector3(6650.395f, 3194.742f, 42.83681f), order_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        1979,
        Terminal.Constructor(Vector3(6651.788f, 3189.545f, 42.83681f), order_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        1980,
        Terminal.Constructor(Vector3(6653.269f, 3184.016f, 42.83681f), order_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        2615,
        SpawnTube.Constructor(Vector3(6639.267f, 3194.781f, 40.98681f), respawn_tube_tower, Vector3(0, 0, 345)),
        owning_building_guid = 81
      )
      LocalObject(
        2616,
        SpawnTube.Constructor(Vector3(6643.514f, 3178.931f, 40.98681f), respawn_tube_tower, Vector3(0, 0, 345)),
        owning_building_guid = 81
      )
      LocalObject(
        2246,
        ProximityTerminal.Constructor(Vector3(6627.606f, 3192.711f, 79.07481f), pad_landing_tower_frame),
        owning_building_guid = 81
      )
      LocalObject(
        2247,
        Terminal.Constructor(Vector3(6627.606f, 3192.711f, 79.07481f), air_rearm_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        2249,
        ProximityTerminal.Constructor(Vector3(6630.31f, 3182.622f, 79.07481f), pad_landing_tower_frame),
        owning_building_guid = 81
      )
      LocalObject(
        2250,
        Terminal.Constructor(Vector3(6630.31f, 3182.622f, 79.07481f), air_rearm_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        1792,
        FacilityTurret.Constructor(Vector3(6619.449f, 3169.69f, 70.44681f), manned_turret),
        owning_building_guid = 81
      )
      TurretToWeapon(1792, 5116)
      LocalObject(
        1793,
        FacilityTurret.Constructor(Vector3(6648.825f, 3208.529f, 70.44681f), manned_turret),
        owning_building_guid = 81
      )
      TurretToWeapon(1793, 5117)
      LocalObject(
        2399,
        Painbox.Constructor(Vector3(6636.153f, 3182.245f, 43.52431f), painbox_radius_continuous),
        owning_building_guid = 81
      )
      LocalObject(
        2400,
        Painbox.Constructor(Vector3(6646.006f, 3194.382f, 41.6048f), painbox_radius_continuous),
        owning_building_guid = 81
      )
      LocalObject(
        2401,
        Painbox.Constructor(Vector3(6649.054f, 3182.276f, 41.6048f), painbox_radius_continuous),
        owning_building_guid = 81
      )
    }

    Building2()

    def Building2(): Unit = { // Name: WG_Searhus_to_Ishundar Type: warpgate GUID: 82, MapID: 2
      LocalBuilding(
        "WG_Searhus_to_Ishundar",
        82,
        2,
        FoundationBuilder(WarpGate.Structure(Vector3(1536f, 7134f, 67.69463f)))
      )
    }

    Building1()

    def Building1(): Unit = { // Name: WG_Searhus_to_Esamir Type: warpgate GUID: 83, MapID: 1
      LocalBuilding(
        "WG_Searhus_to_Esamir",
        83,
        1,
        FoundationBuilder(WarpGate.Structure(Vector3(1540f, 1318f, 53.62445f)))
      )
    }

    Building3()

    def Building3(): Unit = { // Name: WG_Searhus_to_Cyssor Type: warpgate GUID: 84, MapID: 3
      LocalBuilding(
        "WG_Searhus_to_Cyssor",
        84,
        3,
        FoundationBuilder(WarpGate.Structure(Vector3(6990f, 3414f, 53.59508f)))
      )
    }

    def Lattice(): Unit = {
      LatticeLink("Wakea", "Tara")
      LatticeLink("Wakea", "Laka")
      LatticeLink("Karihi", "Laka")
      LatticeLink("Laka", "Hiro")
      LatticeLink("Hiro", "Karihi")
      LatticeLink("Laka", "Iva")
      LatticeLink("Iva", "Sina")
      LatticeLink("Iva", "Akua")
      LatticeLink("Oro", "Ngaru")
      LatticeLink("Oro", "Pele")
      LatticeLink("Oro", "Matagi")
      LatticeLink("Tara", "Rehua")
      LatticeLink("Matagi", "Akua")
      LatticeLink("Hiro", "Matagi")
      LatticeLink("Wakea", "WG_Searhus_to_Ishundar")
      LatticeLink("Rehua", "WG_Searhus_to_Esamir")
      LatticeLink("Iva", "WG_Searhus_to_Cyssor")
      LatticeLink("Iva", "GW_Searhus_N")
      LatticeLink("Rehua", "GW_Searhus_S")
      LatticeLink("Rehua", "Drakulu")
      LatticeLink("Drakulu", "Sina")
      LatticeLink("Rehua", "Sina")
      LatticeLink("Sina", "Akua")
      LatticeLink("Akua", "Pele")
      LatticeLink("Pele", "Ngaru")
      LatticeLink("Pele", "Tara")
    }

    Lattice()

  }
}
