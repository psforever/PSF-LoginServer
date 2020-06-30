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

object Map01 { // Solsar
  val ZoneMap = new ZoneMap("map01") {
    Checksum = 2094187456L

    Building10()

    def Building10(): Unit = { // Name: Mont Type: amp_station GUID: 1, MapID: 10
      LocalBuilding(
        "Mont",
        1,
        10,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3380f, 4300f, 82.74789f),
            Vector3(0f, 0f, 209f),
            amp_station
          )
        )
      )
      LocalObject(
        153,
        CaptureTerminal.Constructor(Vector3(3382.919f, 4301.615f, 94.25589f), capture_terminal),
        owning_building_guid = 1
      )
      LocalObject(121, Door.Constructor(Vector3(3376.516f, 4305.85f, 95.64989f)), owning_building_guid = 1)
      LocalObject(122, Door.Constructor(Vector3(3383.112f, 4293.944f, 95.64989f)), owning_building_guid = 1)
      LocalObject(268, Door.Constructor(Vector3(3296.951f, 4325.646f, 92.46289f)), owning_building_guid = 1)
      LocalObject(269, Door.Constructor(Vector3(3305.771f, 4309.734f, 84.49889f)), owning_building_guid = 1)
      LocalObject(270, Door.Constructor(Vector3(3307.951f, 4356.354f, 84.49889f)), owning_building_guid = 1)
      LocalObject(271, Door.Constructor(Vector3(3323.862f, 4365.173f, 92.46289f)), owning_building_guid = 1)
      LocalObject(272, Door.Constructor(Vector3(3361.04f, 4324.448f, 94.47488f)), owning_building_guid = 1)
      LocalObject(273, Door.Constructor(Vector3(3362.789f, 4331.049f, 89.46889f)), owning_building_guid = 1)
      LocalObject(274, Door.Constructor(Vector3(3368.964f, 4328.842f, 94.47488f)), owning_building_guid = 1)
      LocalObject(276, Door.Constructor(Vector3(3390.668f, 4270.954f, 94.47488f)), owning_building_guid = 1)
      LocalObject(277, Door.Constructor(Vector3(3397.211f, 4268.95f, 89.46889f)), owning_building_guid = 1)
      LocalObject(278, Door.Constructor(Vector3(3398.593f, 4275.348f, 94.47488f)), owning_building_guid = 1)
      LocalObject(279, Door.Constructor(Vector3(3412.547f, 4381.497f, 84.49889f)), owning_building_guid = 1)
      LocalObject(280, Door.Constructor(Vector3(3421.367f, 4365.585f, 92.46188f)), owning_building_guid = 1)
      LocalObject(281, Door.Constructor(Vector3(3429.337f, 4221.947f, 92.46289f)), owning_building_guid = 1)
      LocalObject(282, Door.Constructor(Vector3(3445.248f, 4230.766f, 84.49889f)), owning_building_guid = 1)
      LocalObject(283, Door.Constructor(Vector3(3461.614f, 4292.979f, 84.49889f)), owning_building_guid = 1)
      LocalObject(284, Door.Constructor(Vector3(3470.434f, 4277.067f, 92.46188f)), owning_building_guid = 1)
      LocalObject(285, Door.Constructor(Vector3(3485.256f, 4266.876f, 84.46889f)), owning_building_guid = 1)
      LocalObject(474, Door.Constructor(Vector3(3360.568f, 4293.802f, 84.46889f)), owning_building_guid = 1)
      LocalObject(475, Door.Constructor(Vector3(3364.447f, 4286.805f, 84.46889f)), owning_building_guid = 1)
      LocalObject(476, Door.Constructor(Vector3(3366.934f, 4323.135f, 94.46889f)), owning_building_guid = 1)
      LocalObject(477, Door.Constructor(Vector3(3368.365f, 4320.991f, 89.46889f)), owning_building_guid = 1)
      LocalObject(478, Door.Constructor(Vector3(3373.422f, 4328.368f, 84.46889f)), owning_building_guid = 1)
      LocalObject(479, Door.Constructor(Vector3(3374.182f, 4310.496f, 69.46889f)), owning_building_guid = 1)
      LocalObject(480, Door.Constructor(Vector3(3378.86f, 4326.809f, 84.46889f)), owning_building_guid = 1)
      LocalObject(481, Door.Constructor(Vector3(3381.939f, 4296.501f, 76.96889f)), owning_building_guid = 1)
      LocalObject(482, Door.Constructor(Vector3(3382.739f, 4319.812f, 69.46889f)), owning_building_guid = 1)
      LocalObject(483, Door.Constructor(Vector3(3382.739f, 4319.812f, 76.96889f)), owning_building_guid = 1)
      LocalObject(484, Door.Constructor(Vector3(3391.255f, 4287.945f, 69.46889f)), owning_building_guid = 1)
      LocalObject(485, Door.Constructor(Vector3(3391.635f, 4279.009f, 89.46889f)), owning_building_guid = 1)
      LocalObject(486, Door.Constructor(Vector3(3392.696f, 4276.66f, 94.46889f)), owning_building_guid = 1)
      LocalObject(487, Door.Constructor(Vector3(3394.374f, 4298.821f, 76.96889f)), owning_building_guid = 1)
      LocalObject(488, Door.Constructor(Vector3(3396.693f, 4286.386f, 76.96889f)), owning_building_guid = 1)
      LocalObject(489, Door.Constructor(Vector3(3400.572f, 4279.389f, 84.46889f)), owning_building_guid = 1)
      LocalObject(490, Door.Constructor(Vector3(3402.131f, 4284.827f, 84.46889f)), owning_building_guid = 1)
      LocalObject(491, Door.Constructor(Vector3(3402.17f, 4326.009f, 69.46889f)), owning_building_guid = 1)
      LocalObject(492, Door.Constructor(Vector3(3416.125f, 4292.584f, 76.96889f)), owning_building_guid = 1)
      LocalObject(493, Door.Constructor(Vector3(3418.483f, 4321.332f, 69.46889f)), owning_building_guid = 1)
      LocalObject(494, Door.Constructor(Vector3(3420.042f, 4326.77f, 69.46889f)), owning_building_guid = 1)
      LocalObject(495, Door.Constructor(Vector3(3456.547f, 4310.417f, 76.96889f)), owning_building_guid = 1)
      LocalObject(650, Door.Constructor(Vector3(3354.496f, 4285.833f, 85.22789f)), owning_building_guid = 1)
      LocalObject(1974, Door.Constructor(Vector3(3391.926f, 4318.35f, 77.30189f)), owning_building_guid = 1)
      LocalObject(1975, Door.Constructor(Vector3(3395.462f, 4311.971f, 77.30189f)), owning_building_guid = 1)
      LocalObject(1976, Door.Constructor(Vector3(3398.996f, 4305.596f, 77.30189f)), owning_building_guid = 1)
      LocalObject(
        686,
        IFFLock.Constructor(Vector3(3353.573f, 4281.846f, 84.42789f), Vector3(0, 0, 241)),
        owning_building_guid = 1,
        door_guid = 650
      )
      LocalObject(
        736,
        IFFLock.Constructor(Vector3(3360.615f, 4330.759f, 89.40989f), Vector3(0, 0, 331)),
        owning_building_guid = 1,
        door_guid = 273
      )
      LocalObject(
        737,
        IFFLock.Constructor(Vector3(3361.332f, 4322.247f, 94.40889f), Vector3(0, 0, 241)),
        owning_building_guid = 1,
        door_guid = 272
      )
      LocalObject(
        738,
        IFFLock.Constructor(Vector3(3368.696f, 4331.004f, 94.40889f), Vector3(0, 0, 61)),
        owning_building_guid = 1,
        door_guid = 274
      )
      LocalObject(
        739,
        IFFLock.Constructor(Vector3(3374.242f, 4312.326f, 69.28389f), Vector3(0, 0, 61)),
        owning_building_guid = 1,
        door_guid = 479
      )
      LocalObject(
        740,
        IFFLock.Constructor(Vector3(3380.971f, 4319.758f, 76.78389f), Vector3(0, 0, 331)),
        owning_building_guid = 1,
        door_guid = 483
      )
      LocalObject(
        741,
        IFFLock.Constructor(Vector3(3390.974f, 4268.771f, 94.40889f), Vector3(0, 0, 241)),
        owning_building_guid = 1,
        door_guid = 276
      )
      LocalObject(
        742,
        IFFLock.Constructor(Vector3(3396.205f, 4298.76f, 76.78389f), Vector3(0, 0, 151)),
        owning_building_guid = 1,
        door_guid = 487
      )
      LocalObject(
        743,
        IFFLock.Constructor(Vector3(3398.335f, 4277.532f, 94.40889f), Vector3(0, 0, 61)),
        owning_building_guid = 1,
        door_guid = 278
      )
      LocalObject(
        744,
        IFFLock.Constructor(Vector3(3399.396f, 4269.224f, 89.40989f), Vector3(0, 0, 151)),
        owning_building_guid = 1,
        door_guid = 277
      )
      LocalObject(
        745,
        IFFLock.Constructor(Vector3(3420.096f, 4325.002f, 69.28389f), Vector3(0, 0, 241)),
        owning_building_guid = 1,
        door_guid = 494
      )
      LocalObject(
        746,
        IFFLock.Constructor(Vector3(3487.444f, 4267.145f, 84.40789f), Vector3(0, 0, 151)),
        owning_building_guid = 1,
        door_guid = 285
      )
      LocalObject(933, Locker.Constructor(Vector3(3370.107f, 4287.468f, 67.94788f)), owning_building_guid = 1)
      LocalObject(934, Locker.Constructor(Vector3(3371.277f, 4288.116f, 67.94788f)), owning_building_guid = 1)
      LocalObject(935, Locker.Constructor(Vector3(3372.445f, 4288.764f, 67.94788f)), owning_building_guid = 1)
      LocalObject(936, Locker.Constructor(Vector3(3373.603f, 4289.405f, 67.94788f)), owning_building_guid = 1)
      LocalObject(937, Locker.Constructor(Vector3(3377.574f, 4291.606f, 67.94788f)), owning_building_guid = 1)
      LocalObject(938, Locker.Constructor(Vector3(3377.634f, 4314.534f, 75.70889f)), owning_building_guid = 1)
      LocalObject(939, Locker.Constructor(Vector3(3378.639f, 4315.091f, 75.70889f)), owning_building_guid = 1)
      LocalObject(940, Locker.Constructor(Vector3(3378.743f, 4292.255f, 67.94788f)), owning_building_guid = 1)
      LocalObject(941, Locker.Constructor(Vector3(3379.642f, 4315.647f, 75.70889f)), owning_building_guid = 1)
      LocalObject(942, Locker.Constructor(Vector3(3379.912f, 4292.902f, 67.94788f)), owning_building_guid = 1)
      LocalObject(943, Locker.Constructor(Vector3(3380.66f, 4316.212f, 75.70889f)), owning_building_guid = 1)
      LocalObject(944, Locker.Constructor(Vector3(3381.07f, 4293.544f, 67.94788f)), owning_building_guid = 1)
      LocalObject(
        1338,
        Terminal.Constructor(Vector3(3369.105f, 4319.66f, 84.27689f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1339,
        Terminal.Constructor(Vector3(3380.51f, 4310.106f, 77.03789f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1340,
        Terminal.Constructor(Vector3(3382.319f, 4306.843f, 77.03789f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1341,
        Terminal.Constructor(Vector3(3384.156f, 4303.529f, 77.03789f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1342,
        Terminal.Constructor(Vector3(3390.899f, 4280.342f, 84.27689f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1343,
        Terminal.Constructor(Vector3(3399.648f, 4310.896f, 84.27689f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1906,
        Terminal.Constructor(Vector3(3382.632f, 4306.604f, 89.47588f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1907,
        Terminal.Constructor(Vector3(3390.458f, 4320.383f, 77.58189f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1908,
        Terminal.Constructor(Vector3(3393.997f, 4314.007f, 77.58189f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1909,
        Terminal.Constructor(Vector3(3397.528f, 4307.631f, 77.58189f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1910,
        Terminal.Constructor(Vector3(3408.411f, 4298.13f, 69.50488f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1911,
        Terminal.Constructor(Vector3(3441.037f, 4288.775f, 77.00488f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2081,
        Terminal.Constructor(Vector3(3461.9f, 4251.102f, 84.84988f), vehicle_terminal_combined),
        owning_building_guid = 1
      )
      LocalObject(
        1285,
        VehicleSpawnPad.Constructor(Vector3(3455.209f, 4262.986f, 80.69289f), mb_pad_creation, Vector3(0, 0, -29)),
        owning_building_guid = 1,
        terminal_guid = 2081
      )
      LocalObject(1802, ResourceSilo.Constructor(Vector3(3292.507f, 4349.253f, 89.98289f)), owning_building_guid = 1)
      LocalObject(
        1825,
        SpawnTube.Constructor(Vector3(3391.802f, 4319.481f, 75.44788f), Vector3(0, 0, 151)),
        owning_building_guid = 1
      )
      LocalObject(
        1826,
        SpawnTube.Constructor(Vector3(3395.337f, 4313.104f, 75.44788f), Vector3(0, 0, 151)),
        owning_building_guid = 1
      )
      LocalObject(
        1827,
        SpawnTube.Constructor(Vector3(3398.87f, 4306.73f, 75.44788f), Vector3(0, 0, 151)),
        owning_building_guid = 1
      )
      LocalObject(
        1299,
        ProximityTerminal.Constructor(Vector3(3375.339f, 4290.991f, 67.94788f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1300,
        ProximityTerminal.Constructor(Vector3(3399.281f, 4310.694f, 87.94788f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1480,
        ProximityTerminal.Constructor(Vector3(3327.2f, 4293.409f, 91.25288f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1481,
        Terminal.Constructor(Vector3(3327.2f, 4293.409f, 91.25288f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1483,
        ProximityTerminal.Constructor(Vector3(3337.111f, 4360.257f, 91.27589f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1484,
        Terminal.Constructor(Vector3(3337.111f, 4360.257f, 91.27589f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1486,
        ProximityTerminal.Constructor(Vector3(3384.572f, 4236.892f, 93.41389f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1487,
        Terminal.Constructor(Vector3(3384.572f, 4236.892f, 93.41389f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1489,
        ProximityTerminal.Constructor(Vector3(3439.656f, 4310.462f, 91.25288f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1490,
        Terminal.Constructor(Vector3(3439.656f, 4310.462f, 91.25288f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1737,
        ProximityTerminal.Constructor(Vector3(3325.68f, 4269.44f, 82.14789f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        1738,
        Terminal.Constructor(Vector3(3325.68f, 4269.44f, 82.14789f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1741,
        ProximityTerminal.Constructor(Vector3(3441.406f, 4333.806f, 82.14789f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        1742,
        Terminal.Constructor(Vector3(3441.406f, 4333.806f, 82.14789f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1219,
        FacilityTurret.Constructor(Vector3(3269.344f, 4349.394f, 91.45589f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1219, 5000)
      LocalObject(
        1220,
        FacilityTurret.Constructor(Vector3(3334.443f, 4227.359f, 91.45589f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1220, 5001)
      LocalObject(
        1221,
        FacilityTurret.Constructor(Vector3(3350.346f, 4396.83f, 91.45589f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1221, 5002)
      LocalObject(
        1222,
        FacilityTurret.Constructor(Vector3(3367.111f, 4172.999f, 91.45589f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1222, 5003)
      LocalObject(
        1223,
        FacilityTurret.Constructor(Vector3(3403.542f, 4423.77f, 91.45589f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1223, 5004)
      LocalObject(
        1224,
        FacilityTurret.Constructor(Vector3(3501.342f, 4247.393f, 91.45589f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1224, 5005)
      LocalObject(
        1616,
        Painbox.Constructor(Vector3(3429.009f, 4335.035f, 72.81889f), painbox),
        owning_building_guid = 1
      )
      LocalObject(
        1625,
        Painbox.Constructor(Vector3(3390.156f, 4307.246f, 80.39629f), painbox_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1634,
        Painbox.Constructor(Vector3(3417.756f, 4326.366f, 69.40689f), painbox_door_radius),
        owning_building_guid = 1
      )
      LocalObject(
        1645,
        Painbox.Constructor(Vector3(3380.635f, 4295.828f, 78.62649f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1646,
        Painbox.Constructor(Vector3(3382.222f, 4320.742f, 76.64789f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1647,
        Painbox.Constructor(Vector3(3394.932f, 4297.662f, 76.71239f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(214, Generator.Constructor(Vector3(3433.635f, 4334.333f, 66.65388f)), owning_building_guid = 1)
      LocalObject(
        205,
        Terminal.Constructor(Vector3(3426.493f, 4330.32f, 67.94788f), gen_control),
        owning_building_guid = 1
      )
    }

    Building6()

    def Building6(): Unit = { // Name: Bastet Type: amp_station GUID: 4, MapID: 6
      LocalBuilding(
        "Bastet",
        4,
        6,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5346f, 5518f, 56.43483f),
            Vector3(0f, 0f, 360f),
            amp_station
          )
        )
      )
      LocalObject(
        160,
        CaptureTerminal.Constructor(Vector3(5342.664f, 5518.003f, 67.94283f), capture_terminal),
        owning_building_guid = 4
      )
      LocalObject(123, Door.Constructor(Vector3(5346.211f, 5511.195f, 69.33682f)), owning_building_guid = 4)
      LocalObject(124, Door.Constructor(Vector3(5346.214f, 5524.805f, 69.33682f)), owning_building_guid = 4)
      LocalObject(420, Door.Constructor(Vector3(5270f, 5598f, 58.15583f)), owning_building_guid = 4)
      LocalObject(421, Door.Constructor(Vector3(5278.023f, 5462.5f, 58.18583f)), owning_building_guid = 4)
      LocalObject(422, Door.Constructor(Vector3(5278.023f, 5480.693f, 66.14883f)), owning_building_guid = 4)
      LocalObject(423, Door.Constructor(Vector3(5278.023f, 5563.708f, 58.18583f)), owning_building_guid = 4)
      LocalObject(424, Door.Constructor(Vector3(5278.023f, 5581.901f, 66.14883f)), owning_building_guid = 4)
      LocalObject(425, Door.Constructor(Vector3(5322.498f, 5610.186f, 58.18583f)), owning_building_guid = 4)
      LocalObject(426, Door.Constructor(Vector3(5340.69f, 5610.186f, 66.14983f)), owning_building_guid = 4)
      LocalObject(427, Door.Constructor(Vector3(5341.669f, 5487.424f, 68.16183f)), owning_building_guid = 4)
      LocalObject(428, Door.Constructor(Vector3(5341.69f, 5548.575f, 68.16183f)), owning_building_guid = 4)
      LocalObject(429, Door.Constructor(Vector3(5346f, 5482.5f, 63.15583f)), owning_building_guid = 4)
      LocalObject(430, Door.Constructor(Vector3(5346f, 5553.501f, 63.15583f)), owning_building_guid = 4)
      LocalObject(431, Door.Constructor(Vector3(5350.73f, 5487.425f, 68.16183f)), owning_building_guid = 4)
      LocalObject(432, Door.Constructor(Vector3(5350.751f, 5548.576f, 68.16183f)), owning_building_guid = 4)
      LocalObject(433, Door.Constructor(Vector3(5363.503f, 5433.782f, 66.14983f)), owning_building_guid = 4)
      LocalObject(434, Door.Constructor(Vector3(5381.695f, 5433.782f, 58.18583f)), owning_building_guid = 4)
      LocalObject(435, Door.Constructor(Vector3(5406.203f, 5455.307f, 66.14983f)), owning_building_guid = 4)
      LocalObject(436, Door.Constructor(Vector3(5406.203f, 5473.499f, 58.18583f)), owning_building_guid = 4)
      LocalObject(627, Door.Constructor(Vector3(5274f, 5546f, 50.65583f)), owning_building_guid = 4)
      LocalObject(628, Door.Constructor(Vector3(5298f, 5514f, 43.15583f)), owning_building_guid = 4)
      LocalObject(629, Door.Constructor(Vector3(5302f, 5518f, 43.15583f)), owning_building_guid = 4)
      LocalObject(630, Door.Constructor(Vector3(5314f, 5506f, 43.15583f)), owning_building_guid = 4)
      LocalObject(631, Door.Constructor(Vector3(5318f, 5542f, 50.65583f)), owning_building_guid = 4)
      LocalObject(632, Door.Constructor(Vector3(5334f, 5494f, 58.15583f)), owning_building_guid = 4)
      LocalObject(633, Door.Constructor(Vector3(5334f, 5502f, 43.15583f)), owning_building_guid = 4)
      LocalObject(634, Door.Constructor(Vector3(5334f, 5502f, 50.65583f)), owning_building_guid = 4)
      LocalObject(635, Door.Constructor(Vector3(5334f, 5526f, 50.65583f)), owning_building_guid = 4)
      LocalObject(636, Door.Constructor(Vector3(5334f, 5542f, 58.15583f)), owning_building_guid = 4)
      LocalObject(637, Door.Constructor(Vector3(5338f, 5490f, 58.15583f)), owning_building_guid = 4)
      LocalObject(638, Door.Constructor(Vector3(5338f, 5538f, 50.65583f)), owning_building_guid = 4)
      LocalObject(639, Door.Constructor(Vector3(5338f, 5546f, 58.15583f)), owning_building_guid = 4)
      LocalObject(640, Door.Constructor(Vector3(5342f, 5534f, 43.15583f)), owning_building_guid = 4)
      LocalObject(641, Door.Constructor(Vector3(5346f, 5494f, 63.15583f)), owning_building_guid = 4)
      LocalObject(642, Door.Constructor(Vector3(5346f, 5506f, 43.15583f)), owning_building_guid = 4)
      LocalObject(643, Door.Constructor(Vector3(5346f, 5522f, 50.65583f)), owning_building_guid = 4)
      LocalObject(644, Door.Constructor(Vector3(5346f, 5542f, 63.15583f)), owning_building_guid = 4)
      LocalObject(645, Door.Constructor(Vector3(5346.212f, 5491.431f, 68.15582f)), owning_building_guid = 4)
      LocalObject(646, Door.Constructor(Vector3(5346.212f, 5544.569f, 68.15582f)), owning_building_guid = 4)
      LocalObject(647, Door.Constructor(Vector3(5366f, 5514f, 58.15583f)), owning_building_guid = 4)
      LocalObject(648, Door.Constructor(Vector3(5366f, 5522f, 58.15583f)), owning_building_guid = 4)
      LocalObject(657, Door.Constructor(Vector3(5375.174f, 5518.026f, 58.91483f)), owning_building_guid = 4)
      LocalObject(2017, Door.Constructor(Vector3(5326.673f, 5507.733f, 50.98883f)), owning_building_guid = 4)
      LocalObject(2018, Door.Constructor(Vector3(5326.673f, 5515.026f, 50.98883f)), owning_building_guid = 4)
      LocalObject(2019, Door.Constructor(Vector3(5326.673f, 5522.315f, 50.98883f)), owning_building_guid = 4)
      LocalObject(
        693,
        IFFLock.Constructor(Vector3(5377.915f, 5521.066f, 58.11483f), Vector3(0, 0, 90)),
        owning_building_guid = 4,
        door_guid = 657
      )
      LocalObject(
        850,
        IFFLock.Constructor(Vector3(5267.956f, 5598.826f, 58.09483f), Vector3(0, 0, 0)),
        owning_building_guid = 4,
        door_guid = 420
      )
      LocalObject(
        851,
        IFFLock.Constructor(Vector3(5298.81f, 5515.572f, 42.97083f), Vector3(0, 0, 90)),
        owning_building_guid = 4,
        door_guid = 628
      )
      LocalObject(
        852,
        IFFLock.Constructor(Vector3(5332.428f, 5526.941f, 50.47083f), Vector3(0, 0, 0)),
        owning_building_guid = 4,
        door_guid = 635
      )
      LocalObject(
        853,
        IFFLock.Constructor(Vector3(5335.572f, 5501.19f, 50.47083f), Vector3(0, 0, 180)),
        owning_building_guid = 4,
        door_guid = 634
      )
      LocalObject(
        854,
        IFFLock.Constructor(Vector3(5340.856f, 5485.403f, 68.09583f), Vector3(0, 0, 270)),
        owning_building_guid = 4,
        door_guid = 427
      )
      LocalObject(
        855,
        IFFLock.Constructor(Vector3(5340.856f, 5546.54f, 68.09583f), Vector3(0, 0, 270)),
        owning_building_guid = 4,
        door_guid = 428
      )
      LocalObject(
        856,
        IFFLock.Constructor(Vector3(5343.956f, 5554.321f, 63.09682f), Vector3(0, 0, 0)),
        owning_building_guid = 4,
        door_guid = 430
      )
      LocalObject(
        857,
        IFFLock.Constructor(Vector3(5345.06f, 5504.428f, 42.97083f), Vector3(0, 0, 270)),
        owning_building_guid = 4,
        door_guid = 642
      )
      LocalObject(
        858,
        IFFLock.Constructor(Vector3(5348.042f, 5481.7f, 63.09682f), Vector3(0, 0, 180)),
        owning_building_guid = 4,
        door_guid = 429
      )
      LocalObject(
        859,
        IFFLock.Constructor(Vector3(5351.542f, 5489.492f, 68.09583f), Vector3(0, 0, 90)),
        owning_building_guid = 4,
        door_guid = 431
      )
      LocalObject(
        860,
        IFFLock.Constructor(Vector3(5351.542f, 5550.633f, 68.09583f), Vector3(0, 0, 90)),
        owning_building_guid = 4,
        door_guid = 432
      )
      LocalObject(1132, Locker.Constructor(Vector3(5337.563f, 5504.141f, 49.39582f)), owning_building_guid = 4)
      LocalObject(1133, Locker.Constructor(Vector3(5338.727f, 5504.141f, 49.39582f)), owning_building_guid = 4)
      LocalObject(1134, Locker.Constructor(Vector3(5339.874f, 5504.141f, 49.39582f)), owning_building_guid = 4)
      LocalObject(1135, Locker.Constructor(Vector3(5341.023f, 5504.141f, 49.39582f)), owning_building_guid = 4)
      LocalObject(1136, Locker.Constructor(Vector3(5348.194f, 5524.165f, 41.63483f)), owning_building_guid = 4)
      LocalObject(1137, Locker.Constructor(Vector3(5349.518f, 5524.165f, 41.63483f)), owning_building_guid = 4)
      LocalObject(1138, Locker.Constructor(Vector3(5350.854f, 5524.165f, 41.63483f)), owning_building_guid = 4)
      LocalObject(1139, Locker.Constructor(Vector3(5352.191f, 5524.165f, 41.63483f)), owning_building_guid = 4)
      LocalObject(1140, Locker.Constructor(Vector3(5356.731f, 5524.165f, 41.63483f)), owning_building_guid = 4)
      LocalObject(1141, Locker.Constructor(Vector3(5358.055f, 5524.165f, 41.63483f)), owning_building_guid = 4)
      LocalObject(1142, Locker.Constructor(Vector3(5359.391f, 5524.165f, 41.63483f)), owning_building_guid = 4)
      LocalObject(1143, Locker.Constructor(Vector3(5360.728f, 5524.165f, 41.63483f)), owning_building_guid = 4)
      LocalObject(
        1404,
        Terminal.Constructor(Vector3(5323.533f, 5517.996f, 57.96383f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1405,
        Terminal.Constructor(Vector3(5340.654f, 5509.408f, 50.72483f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1406,
        Terminal.Constructor(Vector3(5340.654f, 5513.139f, 50.72483f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1407,
        Terminal.Constructor(Vector3(5340.654f, 5516.928f, 50.72483f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1408,
        Terminal.Constructor(Vector3(5345.998f, 5495.523f, 57.96383f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1409,
        Terminal.Constructor(Vector3(5345.998f, 5540.477f, 57.96383f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1953,
        Terminal.Constructor(Vector3(5298.058f, 5557.409f, 50.69183f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1954,
        Terminal.Constructor(Vector3(5322.058f, 5533.409f, 43.19183f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1955,
        Terminal.Constructor(Vector3(5326.971f, 5505.243f, 51.26883f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1956,
        Terminal.Constructor(Vector3(5326.967f, 5512.535f, 51.26883f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1957,
        Terminal.Constructor(Vector3(5326.97f, 5519.823f, 51.26883f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1958,
        Terminal.Constructor(Vector3(5340.496f, 5513.5f, 63.16283f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2088,
        Terminal.Constructor(Vector3(5298.075f, 5600.473f, 58.53683f), vehicle_terminal_combined),
        owning_building_guid = 4
      )
      LocalObject(
        1296,
        VehicleSpawnPad.Constructor(Vector3(5298.165f, 5586.835f, 54.37983f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 4,
        terminal_guid = 2088
      )
      LocalObject(1809, ResourceSilo.Constructor(Vector3(5398.645f, 5432.505f, 63.66983f)), owning_building_guid = 4)
      LocalObject(
        1868,
        SpawnTube.Constructor(Vector3(5326.233f, 5506.683f, 49.13483f), Vector3(0, 0, 0)),
        owning_building_guid = 4
      )
      LocalObject(
        1869,
        SpawnTube.Constructor(Vector3(5326.233f, 5513.974f, 49.13483f), Vector3(0, 0, 0)),
        owning_building_guid = 4
      )
      LocalObject(
        1870,
        SpawnTube.Constructor(Vector3(5326.233f, 5521.262f, 49.13483f), Vector3(0, 0, 0)),
        owning_building_guid = 4
      )
      LocalObject(
        1310,
        ProximityTerminal.Constructor(Vector3(5323.952f, 5517.994f, 61.63483f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1311,
        ProximityTerminal.Constructor(Vector3(5354.444f, 5523.62f, 41.63483f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1567,
        ProximityTerminal.Constructor(Vector3(5288.752f, 5537.771f, 64.93983f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1568,
        Terminal.Constructor(Vector3(5288.752f, 5537.771f, 64.93983f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1570,
        ProximityTerminal.Constructor(Vector3(5354.298f, 5444.505f, 64.96283f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1571,
        Terminal.Constructor(Vector3(5354.298f, 5444.505f, 64.96283f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1573,
        ProximityTerminal.Constructor(Vector3(5372.597f, 5575.412f, 67.10083f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1574,
        Terminal.Constructor(Vector3(5372.597f, 5575.412f, 67.10083f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1576,
        ProximityTerminal.Constructor(Vector3(5395.375f, 5498.167f, 64.93983f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1577,
        Terminal.Constructor(Vector3(5395.375f, 5498.167f, 64.93983f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1793,
        ProximityTerminal.Constructor(Vector3(5275.903f, 5518.204f, 55.83483f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        1794,
        Terminal.Constructor(Vector3(5275.903f, 5518.204f, 55.83483f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1797,
        ProximityTerminal.Constructor(Vector3(5408.325f, 5518.393f, 55.83483f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        1798,
        Terminal.Constructor(Vector3(5408.325f, 5518.393f, 55.83483f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1272,
        FacilityTurret.Constructor(Vector3(5265.376f, 5622.839f, 65.14282f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1272, 5006)
      LocalObject(
        1273,
        FacilityTurret.Constructor(Vector3(5265.405f, 5421.162f, 65.14282f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1273, 5007)
      LocalObject(
        1274,
        FacilityTurret.Constructor(Vector3(5324.992f, 5418.934f, 65.14282f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1274, 5008)
      LocalObject(
        1275,
        FacilityTurret.Constructor(Vector3(5418.835f, 5421.152f, 65.14282f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1275, 5009)
      LocalObject(
        1276,
        FacilityTurret.Constructor(Vector3(5418.844f, 5622.829f, 65.14282f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1276, 5010)
      LocalObject(
        1277,
        FacilityTurret.Constructor(Vector3(5421.062f, 5559.447f, 65.14282f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1277, 5011)
      LocalObject(
        1623,
        Painbox.Constructor(Vector3(5286.151f, 5511.117f, 46.50583f), painbox),
        owning_building_guid = 4
      )
      LocalObject(
        1632,
        Painbox.Constructor(Vector3(5333.604f, 5516.586f, 54.08323f), painbox_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        1641,
        Painbox.Constructor(Vector3(5300.195f, 5513.244f, 43.09383f), painbox_door_radius),
        owning_building_guid = 4
      )
      LocalObject(
        1666,
        Painbox.Constructor(Vector3(5334f, 5500.937f, 50.33483f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        1667,
        Painbox.Constructor(Vector3(5334.074f, 5527.284f, 50.39933f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        1668,
        Painbox.Constructor(Vector3(5347.467f, 5521.957f, 52.31343f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(221, Generator.Constructor(Vector3(5282.445f, 5513.975f, 40.34083f)), owning_building_guid = 4)
      LocalObject(
        212,
        Terminal.Constructor(Vector3(5290.637f, 5514.022f, 41.63483f), gen_control),
        owning_building_guid = 4
      )
    }

    Building34()

    def Building34(): Unit = { // Name: bunkerg2 Type: bunker_gauntlet GUID: 7, MapID: 34
      LocalBuilding(
        "bunkerg2",
        7,
        34,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4318f, 4604f, 59.17406f),
            Vector3(0f, 0f, 335f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(339, Door.Constructor(Vector3(4294.626f, 4612.79f, 60.69506f)), owning_building_guid = 7)
      LocalObject(348, Door.Constructor(Vector3(4339.785f, 4591.744f, 60.69506f)), owning_building_guid = 7)
    }

    Building35()

    def Building35(): Unit = { // Name: bunkerg3 Type: bunker_gauntlet GUID: 8, MapID: 35
      LocalBuilding(
        "bunkerg3",
        8,
        35,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4460f, 3330f, 53.19434f),
            Vector3(0f, 0f, 62f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(359, Door.Constructor(Vector3(4450f, 3307.119f, 54.71534f)), owning_building_guid = 8)
      LocalObject(362, Door.Constructor(Vector3(4473.379f, 3351.113f, 54.71534f)), owning_building_guid = 8)
    }

    Building33()

    def Building33(): Unit = { // Name: bunkerg1 Type: bunker_gauntlet GUID: 9, MapID: 33
      LocalBuilding(
        "bunkerg1",
        9,
        33,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4678f, 6156f, 57.799f),
            Vector3(0f, 0f, 263f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(410, Door.Constructor(Vector3(4673.076f, 6131.495f, 59.32f)), owning_building_guid = 9)
      LocalObject(411, Door.Constructor(Vector3(4679.137f, 6180.945f, 59.32f)), owning_building_guid = 9)
    }

    Building27()

    def Building27(): Unit = { // Name: bunker2 Type: bunker_lg GUID: 10, MapID: 27
      LocalBuilding(
        "bunker2",
        10,
        27,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3730f, 2238f, 72.89651f), Vector3(0f, 0f, 220f), bunker_lg)
        )
      )
      LocalObject(305, Door.Constructor(Vector3(3729.647f, 2234.366f, 74.41752f)), owning_building_guid = 10)
    }

    Building30()

    def Building30(): Unit = { // Name: bunker5 Type: bunker_lg GUID: 11, MapID: 30
      LocalBuilding(
        "bunker5",
        11,
        30,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3846f, 5334f, 54.1717f), Vector3(0f, 0f, 45f), bunker_lg)
        )
      )
      LocalObject(314, Door.Constructor(Vector3(3846.035f, 5337.651f, 55.6927f)), owning_building_guid = 11)
    }

    Building28()

    def Building28(): Unit = { // Name: bunker3 Type: bunker_sm GUID: 12, MapID: 28
      LocalBuilding(
        "bunker3",
        12,
        28,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3068f, 2992f, 74.51704f), Vector3(0f, 0f, 96f), bunker_sm)
        )
      )
      LocalObject(240, Door.Constructor(Vector3(3067.927f, 2993.224f, 76.03805f)), owning_building_guid = 12)
    }

    Building29()

    def Building29(): Unit = { // Name: bunker4 Type: bunker_sm GUID: 13, MapID: 29
      LocalBuilding(
        "bunker4",
        13,
        29,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3376f, 4166f, 82.73873f), Vector3(0f, 0f, 130f), bunker_sm)
        )
      )
      LocalObject(275, Door.Constructor(Vector3(3375.255f, 4166.974f, 84.25974f)), owning_building_guid = 13)
    }

    Building26()

    def Building26(): Unit = { // Name: bunker1 Type: bunker_sm GUID: 14, MapID: 26
      LocalBuilding(
        "bunker1",
        14,
        26,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4494f, 2102f, 68.3874f), Vector3(0f, 0f, 82f), bunker_sm)
        )
      )
      LocalObject(368, Door.Constructor(Vector3(4494.225f, 2103.205f, 69.9084f)), owning_building_guid = 14)
    }

    Building31()

    def Building31(): Unit = { // Name: bunker6 Type: bunker_sm GUID: 15, MapID: 31
      LocalBuilding(
        "bunker6",
        15,
        31,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4656f, 6084f, 57.799f), Vector3(0f, 0f, 200f), bunker_sm)
        )
      )
      LocalObject(409, Door.Constructor(Vector3(4654.83f, 6083.633f, 59.32f)), owning_building_guid = 15)
    }

    Building8()

    def Building8(): Unit = { // Name: Hapi Type: comm_station GUID: 16, MapID: 8
      LocalBuilding(
        "Hapi",
        16,
        8,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4276f, 4512f, 59.18149f),
            Vector3(0f, 0f, 300f),
            comm_station
          )
        )
      )
      LocalObject(
        156,
        CaptureTerminal.Constructor(Vector3(4364.498f, 4512.185f, 41.88149f), capture_terminal),
        owning_building_guid = 16
      )
      LocalObject(332, Door.Constructor(Vector3(4195.935f, 4515.072f, 68.89649f)), owning_building_guid = 16)
      LocalObject(333, Door.Constructor(Vector3(4205.031f, 4499.316f, 60.93249f)), owning_building_guid = 16)
      LocalObject(334, Door.Constructor(Vector3(4208.426f, 4542.042f, 60.93249f)), owning_building_guid = 16)
      LocalObject(335, Door.Constructor(Vector3(4224.182f, 4551.138f, 68.89649f)), owning_building_guid = 16)
      LocalObject(336, Door.Constructor(Vector3(4258.406f, 4524.936f, 65.90249f)), owning_building_guid = 16)
      LocalObject(337, Door.Constructor(Vector3(4266.255f, 4529.468f, 65.90249f)), owning_building_guid = 16)
      LocalObject(338, Door.Constructor(Vector3(4267.395f, 4512.084f, 73.34149f)), owning_building_guid = 16)
      LocalObject(340, Door.Constructor(Vector3(4296.84f, 4454.518f, 68.89649f)), owning_building_guid = 16)
      LocalObject(341, Door.Constructor(Vector3(4297.32f, 4515.072f, 65.90249f)), owning_building_guid = 16)
      LocalObject(346, Door.Constructor(Vector3(4312.596f, 4463.614f, 60.93249f)), owning_building_guid = 16)
      LocalObject(347, Door.Constructor(Vector3(4335.12f, 4545.556f, 60.93249f)), owning_building_guid = 16)
      LocalObject(349, Door.Constructor(Vector3(4344.216f, 4529.801f, 68.89549f)), owning_building_guid = 16)
      LocalObject(352, Door.Constructor(Vector3(4357.818f, 4522.287f, 60.90249f)), owning_building_guid = 16)
      LocalObject(542, Door.Constructor(Vector3(4264.536f, 4523.856f, 65.90249f)), owning_building_guid = 16)
      LocalObject(543, Door.Constructor(Vector3(4278.392f, 4531.856f, 60.90249f)), owning_building_guid = 16)
      LocalObject(544, Door.Constructor(Vector3(4279.464f, 4514f, 53.40249f)), owning_building_guid = 16)
      LocalObject(545, Door.Constructor(Vector3(4279.856f, 4537.32f, 53.40249f)), owning_building_guid = 16)
      LocalObject(546, Door.Constructor(Vector3(4280.928f, 4519.464f, 43.40249f)), owning_building_guid = 16)
      LocalObject(547, Door.Constructor(Vector3(4285.713f, 4559.177f, 53.40249f)), owning_building_guid = 16)
      LocalObject(548, Door.Constructor(Vector3(4286.785f, 4541.32f, 43.40249f)), owning_building_guid = 16)
      LocalObject(549, Door.Constructor(Vector3(4288.928f, 4505.608f, 60.90249f)), owning_building_guid = 16)
      LocalObject(550, Door.Constructor(Vector3(4290.392f, 4511.072f, 65.90249f)), owning_building_guid = 16)
      LocalObject(551, Door.Constructor(Vector3(4292.928f, 4498.68f, 55.90249f)), owning_building_guid = 16)
      LocalObject(552, Door.Constructor(Vector3(4297.32f, 4515.072f, 55.90249f)), owning_building_guid = 16)
      LocalObject(553, Door.Constructor(Vector3(4303.569f, 4560.249f, 53.40249f)), owning_building_guid = 16)
      LocalObject(554, Door.Constructor(Vector3(4304.928f, 4477.895f, 50.90249f)), owning_building_guid = 16)
      LocalObject(555, Door.Constructor(Vector3(4307.177f, 4530f, 53.40249f)), owning_building_guid = 16)
      LocalObject(556, Door.Constructor(Vector3(4308.641f, 4535.464f, 43.40249f)), owning_building_guid = 16)
      LocalObject(557, Door.Constructor(Vector3(4310.785f, 4499.751f, 43.40249f)), owning_building_guid = 16)
      LocalObject(558, Door.Constructor(Vector3(4314.392f, 4469.502f, 43.40249f)), owning_building_guid = 16)
      LocalObject(559, Door.Constructor(Vector3(4315.569f, 4539.464f, 53.40249f)), owning_building_guid = 16)
      LocalObject(560, Door.Constructor(Vector3(4325.713f, 4489.895f, 50.90249f)), owning_building_guid = 16)
      LocalObject(561, Door.Constructor(Vector3(4328.249f, 4477.502f, 50.90249f)), owning_building_guid = 16)
      LocalObject(562, Door.Constructor(Vector3(4338.498f, 4515.751f, 43.40249f)), owning_building_guid = 16)
      LocalObject(563, Door.Constructor(Vector3(4349.033f, 4489.502f, 43.40249f)), owning_building_guid = 16)
      LocalObject(564, Door.Constructor(Vector3(4354.89f, 4511.359f, 43.40249f)), owning_building_guid = 16)
      LocalObject(565, Door.Constructor(Vector3(4358.89f, 4504.431f, 43.40249f)), owning_building_guid = 16)
      LocalObject(653, Door.Constructor(Vector3(4281.331f, 4494.631f, 61.67449f)), owning_building_guid = 16)
      LocalObject(1995, Door.Constructor(Vector3(4306.229f, 4487.107f, 51.23549f)), owning_building_guid = 16)
      LocalObject(1996, Door.Constructor(Vector3(4312.545f, 4490.753f, 51.23549f)), owning_building_guid = 16)
      LocalObject(1997, Door.Constructor(Vector3(4318.858f, 4494.398f, 51.23549f)), owning_building_guid = 16)
      LocalObject(
        689,
        IFFLock.Constructor(Vector3(4285.473f, 4493.852f, 60.83349f), Vector3(0, 0, 150)),
        owning_building_guid = 16,
        door_guid = 653
      )
      LocalObject(
        785,
        IFFLock.Constructor(Vector3(4258.727f, 4522.758f, 65.84249f), Vector3(0, 0, 240)),
        owning_building_guid = 16,
        door_guid = 336
      )
      LocalObject(
        786,
        IFFLock.Constructor(Vector3(4265.191f, 4511.744f, 73.26249f), Vector3(0, 0, 330)),
        owning_building_guid = 16,
        door_guid = 338
      )
      LocalObject(
        787,
        IFFLock.Constructor(Vector3(4265.936f, 4531.642f, 65.84249f), Vector3(0, 0, 60)),
        owning_building_guid = 16,
        door_guid = 337
      )
      LocalObject(
        788,
        IFFLock.Constructor(Vector3(4286.756f, 4539.489f, 43.21749f), Vector3(0, 0, 240)),
        owning_building_guid = 16,
        door_guid = 548
      )
      LocalObject(
        789,
        IFFLock.Constructor(Vector3(4295.143f, 4514.753f, 65.84249f), Vector3(0, 0, 330)),
        owning_building_guid = 16,
        door_guid = 341
      )
      LocalObject(
        792,
        IFFLock.Constructor(Vector3(4305.013f, 4476.128f, 50.71749f), Vector3(0, 0, 240)),
        owning_building_guid = 16,
        door_guid = 554
      )
      LocalObject(
        795,
        IFFLock.Constructor(Vector3(4312.561f, 4469.531f, 43.21749f), Vector3(0, 0, 330)),
        owning_building_guid = 16,
        door_guid = 558
      )
      LocalObject(
        796,
        IFFLock.Constructor(Vector3(4325.628f, 4491.661f, 50.71749f), Vector3(0, 0, 60)),
        owning_building_guid = 16,
        door_guid = 560
      )
      LocalObject(
        797,
        IFFLock.Constructor(Vector3(4353.122f, 4511.277f, 43.21749f), Vector3(0, 0, 330)),
        owning_building_guid = 16,
        door_guid = 564
      )
      LocalObject(
        798,
        IFFLock.Constructor(Vector3(4359.968f, 4522.63f, 60.83149f), Vector3(0, 0, 150)),
        owning_building_guid = 16,
        door_guid = 352
      )
      LocalObject(
        799,
        IFFLock.Constructor(Vector3(4360.658f, 4504.513f, 43.21749f), Vector3(0, 0, 150)),
        owning_building_guid = 16,
        door_guid = 565
      )
      LocalObject(1031, Locker.Constructor(Vector3(4308.564f, 4475.88f, 49.64249f)), owning_building_guid = 16)
      LocalObject(1032, Locker.Constructor(Vector3(4309.146f, 4474.872f, 49.64249f)), owning_building_guid = 16)
      LocalObject(1033, Locker.Constructor(Vector3(4309.719f, 4473.878f, 49.64249f)), owning_building_guid = 16)
      LocalObject(1034, Locker.Constructor(Vector3(4310.294f, 4472.883f, 49.64249f)), owning_building_guid = 16)
      LocalObject(1039, Locker.Constructor(Vector3(4331.221f, 4476.685f, 41.88149f)), owning_building_guid = 16)
      LocalObject(1040, Locker.Constructor(Vector3(4331.883f, 4475.539f, 41.88149f)), owning_building_guid = 16)
      LocalObject(1041, Locker.Constructor(Vector3(4332.551f, 4474.381f, 41.88149f)), owning_building_guid = 16)
      LocalObject(1042, Locker.Constructor(Vector3(4333.219f, 4473.224f, 41.88149f)), owning_building_guid = 16)
      LocalObject(1043, Locker.Constructor(Vector3(4335.489f, 4469.292f, 41.88149f)), owning_building_guid = 16)
      LocalObject(1044, Locker.Constructor(Vector3(4336.151f, 4468.145f, 41.88149f)), owning_building_guid = 16)
      LocalObject(1045, Locker.Constructor(Vector3(4336.819f, 4466.988f, 41.88149f)), owning_building_guid = 16)
      LocalObject(1046, Locker.Constructor(Vector3(4337.488f, 4465.83f, 41.88149f)), owning_building_guid = 16)
      LocalObject(
        1367,
        Terminal.Constructor(Vector3(4251.19f, 4508.786f, 65.74149f), order_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1368,
        Terminal.Constructor(Vector3(4268.877f, 4505.196f, 73.13649f), order_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1369,
        Terminal.Constructor(Vector3(4269.583f, 4508.208f, 73.13649f), order_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1370,
        Terminal.Constructor(Vector3(4272.684f, 4507.393f, 73.13649f), order_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1374,
        Terminal.Constructor(Vector3(4314.671f, 4475.836f, 50.97149f), order_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1375,
        Terminal.Constructor(Vector3(4317.902f, 4477.702f, 50.97149f), order_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1376,
        Terminal.Constructor(Vector3(4321.183f, 4479.596f, 50.97149f), order_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1926,
        Terminal.Constructor(Vector3(4254.443f, 4501.275f, 65.99849f), spawn_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1927,
        Terminal.Constructor(Vector3(4275.412f, 4530.201f, 43.43849f), spawn_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1928,
        Terminal.Constructor(Vector3(4304.222f, 4485.604f, 51.51549f), spawn_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1929,
        Terminal.Constructor(Vector3(4310.295f, 4532.483f, 53.43849f), spawn_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1930,
        Terminal.Constructor(Vector3(4310.535f, 4489.253f, 51.51549f), spawn_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1931,
        Terminal.Constructor(Vector3(4316.848f, 4492.895f, 51.51549f), spawn_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        2084,
        Terminal.Constructor(Vector3(4349.665f, 4493.957f, 62.06849f), vehicle_terminal_combined),
        owning_building_guid = 16
      )
      LocalObject(
        1288,
        VehicleSpawnPad.Constructor(Vector3(4342.719f, 4505.68f, 57.91049f), mb_pad_creation, Vector3(0, 0, -30)),
        owning_building_guid = 16,
        terminal_guid = 2084
      )
      LocalObject(1805, ResourceSilo.Constructor(Vector3(4188.28f, 4525.752f, 66.39849f)), owning_building_guid = 16)
      LocalObject(
        1846,
        SpawnTube.Constructor(Vector3(4305.1f, 4486.963f, 49.38149f), Vector3(0, 0, 60)),
        owning_building_guid = 16
      )
      LocalObject(
        1847,
        SpawnTube.Constructor(Vector3(4311.415f, 4490.608f, 49.38149f), Vector3(0, 0, 60)),
        owning_building_guid = 16
      )
      LocalObject(
        1848,
        SpawnTube.Constructor(Vector3(4317.726f, 4494.252f, 49.38149f), Vector3(0, 0, 60)),
        owning_building_guid = 16
      )
      LocalObject(
        1303,
        ProximityTerminal.Constructor(Vector3(4251.337f, 4496.763f, 59.38149f), medical_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1304,
        ProximityTerminal.Constructor(Vector3(4333.874f, 4471f, 41.88149f), medical_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1516,
        ProximityTerminal.Constructor(Vector3(4314.369f, 4555.914f, 67.62249f), pad_landing_frame),
        owning_building_guid = 16
      )
      LocalObject(
        1517,
        Terminal.Constructor(Vector3(4314.369f, 4555.914f, 67.62249f), air_rearm_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1761,
        ProximityTerminal.Constructor(Vector3(4265.53f, 4576.494f, 58.93149f), repair_silo),
        owning_building_guid = 16
      )
      LocalObject(
        1762,
        Terminal.Constructor(Vector3(4265.53f, 4576.494f, 58.93149f), ground_rearm_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1765,
        ProximityTerminal.Constructor(Vector3(4285.75f, 4446.452f, 58.93149f), repair_silo),
        owning_building_guid = 16
      )
      LocalObject(
        1766,
        Terminal.Constructor(Vector3(4285.75f, 4446.452f, 58.93149f), ground_rearm_terminal),
        owning_building_guid = 16
      )
      LocalObject(
        1243,
        FacilityTurret.Constructor(Vector3(4170.118f, 4534.522f, 67.8895f), manned_turret),
        owning_building_guid = 16
      )
      TurretToWeapon(1243, 5012)
      LocalObject(
        1244,
        FacilityTurret.Constructor(Vector3(4220.754f, 4444.464f, 67.8895f), manned_turret),
        owning_building_guid = 16
      )
      TurretToWeapon(1244, 5013)
      LocalObject(
        1245,
        FacilityTurret.Constructor(Vector3(4272.686f, 4595.045f, 67.8895f), manned_turret),
        owning_building_guid = 16
      )
      TurretToWeapon(1245, 5014)
      LocalObject(
        1246,
        FacilityTurret.Constructor(Vector3(4279.664f, 4428.656f, 67.8895f), manned_turret),
        owning_building_guid = 16
      )
      TurretToWeapon(1246, 5015)
      LocalObject(
        1249,
        FacilityTurret.Constructor(Vector3(4331.572f, 4579.246f, 67.8895f), manned_turret),
        owning_building_guid = 16
      )
      TurretToWeapon(1249, 5016)
      LocalObject(
        1253,
        FacilityTurret.Constructor(Vector3(4382.079f, 4489.135f, 67.8895f), manned_turret),
        owning_building_guid = 16
      )
      TurretToWeapon(1253, 5017)
      LocalObject(
        1619,
        Painbox.Constructor(Vector3(4297.323f, 4547.246f, 46.78349f), painbox),
        owning_building_guid = 16
      )
      LocalObject(
        1628,
        Painbox.Constructor(Vector3(4321.771f, 4485.062f, 53.82629f), painbox_continuous),
        owning_building_guid = 16
      )
      LocalObject(
        1637,
        Painbox.Constructor(Vector3(4284.368f, 4539.652f, 44.64019f), painbox_door_radius),
        owning_building_guid = 16
      )
      LocalObject(
        1654,
        Painbox.Constructor(Vector3(4303.219f, 4477.007f, 51.20749f), painbox_door_radius_continuous),
        owning_building_guid = 16
      )
      LocalObject(
        1655,
        Painbox.Constructor(Vector3(4327.348f, 4489.903f, 50.98919f), painbox_door_radius_continuous),
        owning_building_guid = 16
      )
      LocalObject(
        1656,
        Painbox.Constructor(Vector3(4329.026f, 4475.81f, 52.20749f), painbox_door_radius_continuous),
        owning_building_guid = 16
      )
      LocalObject(217, Generator.Constructor(Vector3(4300.243f, 4549.12f, 40.58749f)), owning_building_guid = 16)
      LocalObject(
        208,
        Terminal.Constructor(Vector3(4293.172f, 4544.983f, 41.88149f), gen_control),
        owning_building_guid = 16
      )
    }

    Building13()

    def Building13(): Unit = { // Name: Sobek Type: comm_station_dsp GUID: 19, MapID: 13
      LocalBuilding(
        "Sobek",
        19,
        13,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3044f, 3086f, 74.5262f),
            Vector3(0f, 0f, 360f),
            comm_station_dsp
          )
        )
      )
      LocalObject(
        152,
        CaptureTerminal.Constructor(Vector3(3120.089f, 3066.734f, 57.1262f), capture_terminal),
        owning_building_guid = 19
      )
      LocalObject(199, Door.Constructor(Vector3(3112.339f, 3156.464f, 77.9042f)), owning_building_guid = 19)
      LocalObject(230, Door.Constructor(Vector3(2984.196f, 3042.501f, 76.1772f)), owning_building_guid = 19)
      LocalObject(231, Door.Constructor(Vector3(2984.196f, 3060.693f, 84.1412f)), owning_building_guid = 19)
      LocalObject(232, Door.Constructor(Vector3(3001.307f, 3018.197f, 84.1412f)), owning_building_guid = 19)
      LocalObject(233, Door.Constructor(Vector3(3019.499f, 3018.197f, 76.1772f)), owning_building_guid = 19)
      LocalObject(234, Door.Constructor(Vector3(3024f, 3077.231f, 81.1472f)), owning_building_guid = 19)
      LocalObject(235, Door.Constructor(Vector3(3024f, 3086.295f, 81.1472f)), owning_building_guid = 19)
      LocalObject(236, Door.Constructor(Vector3(3036.763f, 3205.958f, 76.1772f)), owning_building_guid = 19)
      LocalObject(237, Door.Constructor(Vector3(3039.625f, 3078.59f, 88.5862f)), owning_building_guid = 19)
      LocalObject(238, Door.Constructor(Vector3(3049.627f, 3218.823f, 84.1402f)), owning_building_guid = 19)
      LocalObject(239, Door.Constructor(Vector3(3052f, 3106f, 81.1472f)), owning_building_guid = 19)
      LocalObject(241, Door.Constructor(Vector3(3091.721f, 3250.353f, 76.1772f)), owning_building_guid = 19)
      LocalObject(242, Door.Constructor(Vector3(3097.952f, 3190.355f, 81.1432f)), owning_building_guid = 19)
      LocalObject(243, Door.Constructor(Vector3(3099.927f, 3160.35f, 76.1492f)), owning_building_guid = 19)
      LocalObject(244, Door.Constructor(Vector3(3109.914f, 3250.353f, 84.1402f)), owning_building_guid = 19)
      LocalObject(245, Door.Constructor(Vector3(3123.929f, 3073.406f, 84.1412f)), owning_building_guid = 19)
      LocalObject(250, Door.Constructor(Vector3(3136.793f, 3086.27f, 76.1772f)), owning_building_guid = 19)
      LocalObject(251, Door.Constructor(Vector3(3150.977f, 3149.008f, 84.1402f)), owning_building_guid = 19)
      LocalObject(252, Door.Constructor(Vector3(3150.977f, 3167.2f, 76.1772f)), owning_building_guid = 19)
      LocalObject(255, Door.Constructor(Vector3(3160f, 3166f, 76.1472f)), owning_building_guid = 19)
      LocalObject(453, Door.Constructor(Vector3(3028f, 3082f, 81.1472f)), owning_building_guid = 19)
      LocalObject(454, Door.Constructor(Vector3(3028f, 3098f, 76.1472f)), owning_building_guid = 19)
      LocalObject(455, Door.Constructor(Vector3(3052f, 3098f, 81.1472f)), owning_building_guid = 19)
      LocalObject(456, Door.Constructor(Vector3(3052f, 3106f, 71.1472f)), owning_building_guid = 19)
      LocalObject(457, Door.Constructor(Vector3(3056f, 3094f, 76.1472f)), owning_building_guid = 19)
      LocalObject(458, Door.Constructor(Vector3(3064f, 3094f, 71.1472f)), owning_building_guid = 19)
      LocalObject(459, Door.Constructor(Vector3(3068f, 3130f, 66.1472f)), owning_building_guid = 19)
      LocalObject(460, Door.Constructor(Vector3(3072f, 3118f, 58.6472f)), owning_building_guid = 19)
      LocalObject(461, Door.Constructor(Vector3(3084f, 3082f, 58.6472f)), owning_building_guid = 19)
      LocalObject(462, Door.Constructor(Vector3(3084f, 3146f, 58.6472f)), owning_building_guid = 19)
      LocalObject(463, Door.Constructor(Vector3(3088f, 3078f, 66.1472f)), owning_building_guid = 19)
      LocalObject(464, Door.Constructor(Vector3(3088f, 3094f, 66.1472f)), owning_building_guid = 19)
      LocalObject(465, Door.Constructor(Vector3(3088f, 3118f, 66.1472f)), owning_building_guid = 19)
      LocalObject(466, Door.Constructor(Vector3(3100f, 3098f, 58.6472f)), owning_building_guid = 19)
      LocalObject(467, Door.Constructor(Vector3(3100f, 3114f, 66.1472f)), owning_building_guid = 19)
      LocalObject(468, Door.Constructor(Vector3(3103.921f, 3180.351f, 81.1492f)), owning_building_guid = 19)
      LocalObject(469, Door.Constructor(Vector3(3116f, 3058f, 58.6472f)), owning_building_guid = 19)
      LocalObject(470, Door.Constructor(Vector3(3124f, 3058f, 58.6472f)), owning_building_guid = 19)
      LocalObject(471, Door.Constructor(Vector3(3128f, 3070f, 58.6472f)), owning_building_guid = 19)
      LocalObject(472, Door.Constructor(Vector3(3132f, 3090f, 66.1472f)), owning_building_guid = 19)
      LocalObject(473, Door.Constructor(Vector3(3132f, 3122f, 66.1472f)), owning_building_guid = 19)
      LocalObject(649, Door.Constructor(Vector3(3061.707f, 3081.922f, 76.9182f)), owning_building_guid = 19)
      LocalObject(1963, Door.Constructor(Vector3(3080.673f, 3099.733f, 66.4802f)), owning_building_guid = 19)
      LocalObject(1964, Door.Constructor(Vector3(3080.673f, 3107.026f, 66.4802f)), owning_building_guid = 19)
      LocalObject(1965, Door.Constructor(Vector3(3080.673f, 3114.315f, 66.4802f)), owning_building_guid = 19)
      LocalObject(
        685,
        IFFLock.Constructor(Vector3(3064.454f, 3085.09f, 76.0942f), Vector3(0, 0, 90)),
        owning_building_guid = 19,
        door_guid = 649
      )
      LocalObject(
        704,
        IFFLock.Constructor(Vector3(3021.959f, 3087.104f, 81.0942f), Vector3(0, 0, 0)),
        owning_building_guid = 19,
        door_guid = 235
      )
      LocalObject(
        705,
        IFFLock.Constructor(Vector3(3026.04f, 3076.42f, 81.0942f), Vector3(0, 0, 180)),
        owning_building_guid = 19,
        door_guid = 234
      )
      LocalObject(
        706,
        IFFLock.Constructor(Vector3(3038.817f, 3076.514f, 88.5942f), Vector3(0, 0, 270)),
        owning_building_guid = 19,
        door_guid = 237
      )
      LocalObject(
        707,
        IFFLock.Constructor(Vector3(3051.193f, 3103.962f, 81.0942f), Vector3(0, 0, 270)),
        owning_building_guid = 19,
        door_guid = 239
      )
      LocalObject(
        708,
        IFFLock.Constructor(Vector3(3084.94f, 3147.572f, 58.4622f), Vector3(0, 0, 90)),
        owning_building_guid = 19,
        door_guid = 462
      )
      LocalObject(
        709,
        IFFLock.Constructor(Vector3(3086.428f, 3118.94f, 65.9622f), Vector3(0, 0, 0)),
        owning_building_guid = 19,
        door_guid = 465
      )
      LocalObject(
        710,
        IFFLock.Constructor(Vector3(3089.572f, 3093.19f, 65.9622f), Vector3(0, 0, 180)),
        owning_building_guid = 19,
        door_guid = 464
      )
      LocalObject(
        711,
        IFFLock.Constructor(Vector3(3095.907f, 3191.163f, 81.0732f), Vector3(0, 0, 0)),
        owning_building_guid = 19,
        door_guid = 242
      )
      LocalObject(
        712,
        IFFLock.Constructor(Vector3(3099.06f, 3096.428f, 58.4622f), Vector3(0, 0, 270)),
        owning_building_guid = 19,
        door_guid = 466
      )
      LocalObject(
        713,
        IFFLock.Constructor(Vector3(3099.124f, 3158.312f, 76.1382f), Vector3(0, 0, 270)),
        owning_building_guid = 19,
        door_guid = 243
      )
      LocalObject(
        714,
        IFFLock.Constructor(Vector3(3115.06f, 3056.428f, 58.4622f), Vector3(0, 0, 270)),
        owning_building_guid = 19,
        door_guid = 469
      )
      LocalObject(
        716,
        IFFLock.Constructor(Vector3(3124.813f, 3059.572f, 58.4622f), Vector3(0, 0, 90)),
        owning_building_guid = 19,
        door_guid = 470
      )
      LocalObject(
        723,
        IFFLock.Constructor(Vector3(3157.953f, 3166.808f, 76.0372f), Vector3(0, 0, 0)),
        owning_building_guid = 19,
        door_guid = 255
      )
      LocalObject(889, Locker.Constructor(Vector3(3091.563f, 3096.141f, 64.8872f)), owning_building_guid = 19)
      LocalObject(890, Locker.Constructor(Vector3(3092.727f, 3096.141f, 64.8872f)), owning_building_guid = 19)
      LocalObject(891, Locker.Constructor(Vector3(3093.874f, 3096.141f, 64.8872f)), owning_building_guid = 19)
      LocalObject(892, Locker.Constructor(Vector3(3095.023f, 3096.141f, 64.8872f)), owning_building_guid = 19)
      LocalObject(893, Locker.Constructor(Vector3(3102.194f, 3116.165f, 57.1262f)), owning_building_guid = 19)
      LocalObject(894, Locker.Constructor(Vector3(3103.518f, 3116.165f, 57.1262f)), owning_building_guid = 19)
      LocalObject(895, Locker.Constructor(Vector3(3104.854f, 3116.165f, 57.1262f)), owning_building_guid = 19)
      LocalObject(896, Locker.Constructor(Vector3(3106.191f, 3116.165f, 57.1262f)), owning_building_guid = 19)
      LocalObject(897, Locker.Constructor(Vector3(3110.731f, 3116.165f, 57.1262f)), owning_building_guid = 19)
      LocalObject(898, Locker.Constructor(Vector3(3112.055f, 3116.165f, 57.1262f)), owning_building_guid = 19)
      LocalObject(899, Locker.Constructor(Vector3(3113.391f, 3116.165f, 57.1262f)), owning_building_guid = 19)
      LocalObject(900, Locker.Constructor(Vector3(3114.728f, 3116.165f, 57.1262f)), owning_building_guid = 19)
      LocalObject(
        201,
        Terminal.Constructor(Vector3(3103.879f, 3188.918f, 80.2302f), dropship_vehicle_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        200,
        VehicleSpawnPad.Constructor(Vector3(3112.328f, 3210.856f, 74.5542f), dropship_pad_doors, Vector3(0, 0, 90)),
        owning_building_guid = 19,
        terminal_guid = 201
      )
      LocalObject(
        1318,
        Terminal.Constructor(Vector3(3034.378f, 3062.897f, 80.9862f), order_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1319,
        Terminal.Constructor(Vector3(3044.075f, 3078.547f, 88.3812f), order_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1320,
        Terminal.Constructor(Vector3(3046.331f, 3076.43f, 88.3812f), order_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1321,
        Terminal.Constructor(Vector3(3046.332f, 3080.825f, 88.3812f), order_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1322,
        Terminal.Constructor(Vector3(3048.592f, 3078.59f, 88.3812f), order_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1323,
        Terminal.Constructor(Vector3(3094.654f, 3101.408f, 66.2162f), order_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1324,
        Terminal.Constructor(Vector3(3094.654f, 3105.139f, 66.2162f), order_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1325,
        Terminal.Constructor(Vector3(3094.654f, 3108.928f, 66.2162f), order_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1897,
        Terminal.Constructor(Vector3(3042.509f, 3061.959f, 81.2432f), spawn_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1898,
        Terminal.Constructor(Vector3(3080.971f, 3097.243f, 66.7602f), spawn_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1899,
        Terminal.Constructor(Vector3(3080.967f, 3104.535f, 66.7602f), spawn_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1900,
        Terminal.Constructor(Vector3(3080.97f, 3111.823f, 66.7602f), spawn_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1901,
        Terminal.Constructor(Vector3(3099.103f, 3180.906f, 81.1742f), spawn_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1902,
        Terminal.Constructor(Vector3(3108.058f, 3085.409f, 58.6542f), spawn_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1903,
        Terminal.Constructor(Vector3(3115.409f, 3141.942f, 58.6542f), spawn_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1904,
        Terminal.Constructor(Vector3(3124.058f, 3093.409f, 66.1832f), spawn_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1905,
        Terminal.Constructor(Vector3(3124.058f, 3133.409f, 66.1832f), spawn_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        2080,
        Terminal.Constructor(Vector3(3061.698f, 3194.044f, 77.3132f), ground_vehicle_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1284,
        VehicleSpawnPad.Constructor(Vector3(3061.786f, 3180.411f, 73.1552f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 19,
        terminal_guid = 2080
      )
      LocalObject(1801, ResourceSilo.Constructor(Vector3(3142.212f, 3251.642f, 81.6432f)), owning_building_guid = 19)
      LocalObject(
        1814,
        SpawnTube.Constructor(Vector3(3080.233f, 3098.683f, 64.6262f), Vector3(0, 0, 0)),
        owning_building_guid = 19
      )
      LocalObject(
        1815,
        SpawnTube.Constructor(Vector3(3080.233f, 3105.974f, 64.6262f), Vector3(0, 0, 0)),
        owning_building_guid = 19
      )
      LocalObject(
        1816,
        SpawnTube.Constructor(Vector3(3080.233f, 3113.262f, 64.6262f), Vector3(0, 0, 0)),
        owning_building_guid = 19
      )
      LocalObject(
        1297,
        ProximityTerminal.Constructor(Vector3(3044.863f, 3057.013f, 74.6262f), medical_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1298,
        ProximityTerminal.Constructor(Vector3(3108.444f, 3115.62f, 57.1262f), medical_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1468,
        ProximityTerminal.Constructor(Vector3(3025.153f, 3179.398f, 82.9362f), pad_landing_frame),
        owning_building_guid = 19
      )
      LocalObject(
        1469,
        Terminal.Constructor(Vector3(3025.153f, 3179.398f, 82.9362f), air_rearm_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1471,
        ProximityTerminal.Constructor(Vector3(3041.514f, 3133.467f, 80.2202f), pad_landing_frame),
        owning_building_guid = 19
      )
      LocalObject(
        1472,
        Terminal.Constructor(Vector3(3041.514f, 3133.467f, 80.2202f), air_rearm_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1474,
        ProximityTerminal.Constructor(Vector3(3093.804f, 3097.901f, 87.4022f), pad_landing_frame),
        owning_building_guid = 19
      )
      LocalObject(
        1475,
        Terminal.Constructor(Vector3(3093.804f, 3097.901f, 87.4022f), air_rearm_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1477,
        ProximityTerminal.Constructor(Vector3(3129.071f, 3114.159f, 82.9492f), pad_landing_frame),
        owning_building_guid = 19
      )
      LocalObject(
        1478,
        Terminal.Constructor(Vector3(3129.071f, 3114.159f, 82.9492f), air_rearm_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1729,
        ProximityTerminal.Constructor(Vector3(2982.642f, 3124.24f, 74.2762f), repair_silo),
        owning_building_guid = 19
      )
      LocalObject(
        1730,
        Terminal.Constructor(Vector3(2982.642f, 3124.24f, 74.2762f), ground_rearm_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1733,
        ProximityTerminal.Constructor(Vector3(3152.57f, 3127.152f, 74.2762f), repair_silo),
        owning_building_guid = 19
      )
      LocalObject(
        1734,
        Terminal.Constructor(Vector3(3152.57f, 3127.152f, 74.2762f), ground_rearm_terminal),
        owning_building_guid = 19
      )
      LocalObject(
        1205,
        FacilityTurret.Constructor(Vector3(2970.401f, 3159.113f, 83.1342f), manned_turret),
        owning_building_guid = 19
      )
      TurretToWeapon(1205, 5018)
      LocalObject(
        1206,
        FacilityTurret.Constructor(Vector3(2971.554f, 3005.565f, 83.1342f), manned_turret),
        owning_building_guid = 19
      )
      TurretToWeapon(1206, 5019)
      LocalObject(
        1207,
        FacilityTurret.Constructor(Vector3(3015.445f, 3205.667f, 83.1342f), manned_turret),
        owning_building_guid = 19
      )
      TurretToWeapon(1207, 5020)
      LocalObject(
        1208,
        FacilityTurret.Constructor(Vector3(3074.428f, 3004.396f, 83.1342f), manned_turret),
        owning_building_guid = 19
      )
      TurretToWeapon(1208, 5021)
      LocalObject(
        1209,
        FacilityTurret.Constructor(Vector3(3075.449f, 3264.154f, 83.1342f), manned_turret),
        owning_building_guid = 19
      )
      TurretToWeapon(1209, 5022)
      LocalObject(
        1211,
        FacilityTurret.Constructor(Vector3(3116.537f, 3045.011f, 83.1342f), manned_turret),
        owning_building_guid = 19
      )
      TurretToWeapon(1211, 5023)
      LocalObject(
        1215,
        FacilityTurret.Constructor(Vector3(3163.619f, 3262.985f, 83.1342f), manned_turret),
        owning_building_guid = 19
      )
      TurretToWeapon(1215, 5024)
      LocalObject(
        1216,
        FacilityTurret.Constructor(Vector3(3164.773f, 3094.733f, 83.1342f), manned_turret),
        owning_building_guid = 19
      )
      TurretToWeapon(1216, 5025)
      LocalObject(
        1615,
        Painbox.Constructor(Vector3(3072.428f, 3146.057f, 61.0205f), painbox),
        owning_building_guid = 19
      )
      LocalObject(
        1624,
        Painbox.Constructor(Vector3(3089.857f, 3106.408f, 68.6537f), painbox_continuous),
        owning_building_guid = 19
      )
      LocalObject(
        1633,
        Painbox.Constructor(Vector3(3086.203f, 3144.915f, 60.2584f), painbox_door_radius),
        owning_building_guid = 19
      )
      LocalObject(
        1642,
        Painbox.Constructor(Vector3(3087.087f, 3091.386f, 67.0554f), painbox_door_radius_continuous),
        owning_building_guid = 19
      )
      LocalObject(
        1643,
        Painbox.Constructor(Vector3(3087.895f, 3120.081f, 67.5262f), painbox_door_radius_continuous),
        owning_building_guid = 19
      )
      LocalObject(
        1644,
        Painbox.Constructor(Vector3(3102.317f, 3113.888f, 67.9565f), painbox_door_radius_continuous),
        owning_building_guid = 19
      )
      LocalObject(213, Generator.Constructor(Vector3(3068.445f, 3145.975f, 55.8322f)), owning_building_guid = 19)
      LocalObject(
        204,
        Terminal.Constructor(Vector3(3076.637f, 3146.022f, 57.1262f), gen_control),
        owning_building_guid = 19
      )
    }

    Building12()

    def Building12(): Unit = { // Name: Horus Type: cryo_facility GUID: 22, MapID: 12
      LocalBuilding(
        "Horus",
        22,
        12,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3690f, 2158f, 72.89651f),
            Vector3(0f, 0f, 265f),
            cryo_facility
          )
        )
      )
      LocalObject(
        154,
        CaptureTerminal.Constructor(Vector3(3751.489f, 2180.817f, 62.89651f), capture_terminal),
        owning_building_guid = 22
      )
      LocalObject(290, Door.Constructor(Vector3(3595.561f, 2134.14f, 74.41752f)), owning_building_guid = 22)
      LocalObject(291, Door.Constructor(Vector3(3605.34f, 2141.817f, 74.44752f)), owning_building_guid = 22)
      LocalObject(292, Door.Constructor(Vector3(3606.926f, 2159.941f, 82.41151f)), owning_building_guid = 22)
      LocalObject(293, Door.Constructor(Vector3(3635.061f, 2111.685f, 82.41151f)), owning_building_guid = 22)
      LocalObject(294, Door.Constructor(Vector3(3653.185f, 2110.099f, 74.44752f)), owning_building_guid = 22)
      LocalObject(295, Door.Constructor(Vector3(3692.776f, 2153.742f, 84.41751f)), owning_building_guid = 22)
      LocalObject(296, Door.Constructor(Vector3(3699.623f, 2216.36f, 74.44752f)), owning_building_guid = 22)
      LocalObject(300, Door.Constructor(Vector3(3711.318f, 2172.196f, 84.41751f)), owning_building_guid = 22)
      LocalObject(301, Door.Constructor(Vector3(3717.747f, 2214.775f, 82.41151f)), owning_building_guid = 22)
      LocalObject(306, Door.Constructor(Vector3(3759.648f, 2176.132f, 82.41151f)), owning_building_guid = 22)
      LocalObject(307, Door.Constructor(Vector3(3761.234f, 2194.256f, 74.44752f)), owning_building_guid = 22)
      LocalObject(496, Door.Constructor(Vector3(3603.381f, 2177.624f, 66.91751f)), owning_building_guid = 22)
      LocalObject(497, Door.Constructor(Vector3(3633.715f, 2203.077f, 66.91751f)), owning_building_guid = 22)
      LocalObject(498, Door.Constructor(Vector3(3635.956f, 2182.805f, 66.91751f)), owning_building_guid = 22)
      LocalObject(499, Door.Constructor(Vector3(3659.865f, 2180.713f, 64.41751f)), owning_building_guid = 22)
      LocalObject(500, Door.Constructor(Vector3(3662.106f, 2160.44f, 64.41751f)), owning_building_guid = 22)
      LocalObject(501, Door.Constructor(Vector3(3681.682f, 2154.712f, 84.41751f)), owning_building_guid = 22)
      LocalObject(502, Door.Constructor(Vector3(3682.229f, 2206.863f, 64.41751f)), owning_building_guid = 22)
      LocalObject(503, Door.Constructor(Vector3(3686.015f, 2158.349f, 64.41751f)), owning_building_guid = 22)
      LocalObject(504, Door.Constructor(Vector3(3688.954f, 2146.046f, 64.41751f)), owning_building_guid = 22)
      LocalObject(505, Door.Constructor(Vector3(3691.046f, 2169.954f, 64.41751f)), owning_building_guid = 22)
      LocalObject(506, Door.Constructor(Vector3(3691.893f, 2133.743f, 56.91751f)), owning_building_guid = 22)
      LocalObject(507, Door.Constructor(Vector3(3693.138f, 2193.863f, 64.41751f)), owning_building_guid = 22)
      LocalObject(508, Door.Constructor(Vector3(3707.832f, 2132.348f, 64.41751f)), owning_building_guid = 22)
      LocalObject(509, Door.Constructor(Vector3(3711.318f, 2172.196f, 64.41751f)), owning_building_guid = 22)
      LocalObject(510, Door.Constructor(Vector3(3711.318f, 2172.196f, 74.41752f)), owning_building_guid = 22)
      LocalObject(511, Door.Constructor(Vector3(3712.713f, 2188.135f, 64.41751f)), owning_building_guid = 22)
      LocalObject(512, Door.Constructor(Vector3(3712.863f, 2143.954f, 64.41751f)), owning_building_guid = 22)
      LocalObject(513, Door.Constructor(Vector3(3730.894f, 2166.468f, 64.41751f)), owning_building_guid = 22)
      LocalObject(514, Door.Constructor(Vector3(3732.985f, 2190.377f, 64.41751f)), owning_building_guid = 22)
      LocalObject(515, Door.Constructor(Vector3(3741.802f, 2153.468f, 64.41751f)), owning_building_guid = 22)
      LocalObject(516, Door.Constructor(Vector3(3759.136f, 2168.012f, 64.41751f)), owning_building_guid = 22)
      LocalObject(517, Door.Constructor(Vector3(3759.833f, 2175.982f, 64.41751f)), owning_building_guid = 22)
      LocalObject(518, Door.Constructor(Vector3(3760.53f, 2183.952f, 64.41751f)), owning_building_guid = 22)
      LocalObject(651, Door.Constructor(Vector3(3714.64f, 2151.837f, 75.17951f)), owning_building_guid = 22)
      LocalObject(658, Door.Constructor(Vector3(3688.954f, 2146.046f, 74.41752f)), owning_building_guid = 22)
      LocalObject(659, Door.Constructor(Vector3(3698.318f, 2161.288f, 74.41551f)), owning_building_guid = 22)
      LocalObject(1979, Door.Constructor(Vector3(3695.304f, 2152.845f, 64.75052f)), owning_building_guid = 22)
      LocalObject(1980, Door.Constructor(Vector3(3702.569f, 2152.209f, 64.75052f)), owning_building_guid = 22)
      LocalObject(1982, Door.Constructor(Vector3(3709.83f, 2151.574f, 64.75052f)), owning_building_guid = 22)
      LocalObject(
        687,
        IFFLock.Constructor(Vector3(3717.649f, 2154.808f, 74.37852f), Vector3(0, 0, 95)),
        owning_building_guid = 22,
        door_guid = 651
      )
      LocalObject(
        751,
        IFFLock.Constructor(Vector3(3597.524f, 2133.151f, 74.34852f), Vector3(0, 0, 185)),
        owning_building_guid = 22,
        door_guid = 290
      )
      LocalObject(
        752,
        IFFLock.Constructor(Vector3(3688.01f, 2144.55f, 64.23251f), Vector3(0, 0, 275)),
        owning_building_guid = 22,
        door_guid = 504
      )
      LocalObject(
        753,
        IFFLock.Constructor(Vector3(3690.398f, 2134.687f, 56.73251f), Vector3(0, 0, 5)),
        owning_building_guid = 22,
        door_guid = 506
      )
      LocalObject(
        754,
        IFFLock.Constructor(Vector3(3693.773f, 2155.708f, 84.34851f), Vector3(0, 0, 95)),
        owning_building_guid = 22,
        door_guid = 295
      )
      LocalObject(
        758,
        IFFLock.Constructor(Vector3(3713.283f, 2171.207f, 84.34851f), Vector3(0, 0, 185)),
        owning_building_guid = 22,
        door_guid = 300
      )
      LocalObject(
        759,
        IFFLock.Constructor(Vector3(3713.807f, 2145.449f, 64.23251f), Vector3(0, 0, 95)),
        owning_building_guid = 22,
        door_guid = 512
      )
      LocalObject(
        763,
        IFFLock.Constructor(Vector3(3759.046f, 2185.025f, 64.23251f), Vector3(0, 0, 5)),
        owning_building_guid = 22,
        door_guid = 518
      )
      LocalObject(
        765,
        IFFLock.Constructor(Vector3(3761.328f, 2175.035f, 64.23251f), Vector3(0, 0, 185)),
        owning_building_guid = 22,
        door_guid = 517
      )
      LocalObject(953, Locker.Constructor(Vector3(3689.031f, 2119.841f, 62.80452f)), owning_building_guid = 22)
      LocalObject(954, Locker.Constructor(Vector3(3689.123f, 2120.892f, 62.80452f)), owning_building_guid = 22)
      LocalObject(955, Locker.Constructor(Vector3(3689.215f, 2121.948f, 62.80452f)), owning_building_guid = 22)
      LocalObject(956, Locker.Constructor(Vector3(3689.307f, 2122.999f, 62.80452f)), owning_building_guid = 22)
      LocalObject(957, Locker.Constructor(Vector3(3689.399f, 2124.05f, 62.80452f)), owning_building_guid = 22)
      LocalObject(958, Locker.Constructor(Vector3(3689.491f, 2125.102f, 62.80452f)), owning_building_guid = 22)
      LocalObject(959, Locker.Constructor(Vector3(3690.475f, 2138.863f, 63.15752f)), owning_building_guid = 22)
      LocalObject(960, Locker.Constructor(Vector3(3690.575f, 2140.007f, 63.15752f)), owning_building_guid = 22)
      LocalObject(961, Locker.Constructor(Vector3(3690.675f, 2141.15f, 63.15752f)), owning_building_guid = 22)
      LocalObject(962, Locker.Constructor(Vector3(3690.776f, 2142.31f, 63.15752f)), owning_building_guid = 22)
      LocalObject(967, Locker.Constructor(Vector3(3708.956f, 2118.101f, 62.80452f)), owning_building_guid = 22)
      LocalObject(968, Locker.Constructor(Vector3(3709.048f, 2119.153f, 62.80452f)), owning_building_guid = 22)
      LocalObject(969, Locker.Constructor(Vector3(3709.14f, 2120.204f, 62.80452f)), owning_building_guid = 22)
      LocalObject(970, Locker.Constructor(Vector3(3709.232f, 2121.255f, 62.80452f)), owning_building_guid = 22)
      LocalObject(971, Locker.Constructor(Vector3(3709.324f, 2122.312f, 62.80452f)), owning_building_guid = 22)
      LocalObject(972, Locker.Constructor(Vector3(3709.416f, 2123.362f, 62.80452f)), owning_building_guid = 22)
      LocalObject(977, Locker.Constructor(Vector3(3737.565f, 2133.765f, 62.89151f)), owning_building_guid = 22)
      LocalObject(978, Locker.Constructor(Vector3(3738.816f, 2133.656f, 62.89151f)), owning_building_guid = 22)
      LocalObject(979, Locker.Constructor(Vector3(3740.072f, 2133.546f, 62.89151f)), owning_building_guid = 22)
      LocalObject(980, Locker.Constructor(Vector3(3741.329f, 2133.436f, 62.89151f)), owning_building_guid = 22)
      LocalObject(981, Locker.Constructor(Vector3(3742.577f, 2133.327f, 62.89151f)), owning_building_guid = 22)
      LocalObject(1168, Locker.Constructor(Vector3(3676.242f, 2140.874f, 72.89651f)), owning_building_guid = 22)
      LocalObject(1169, Locker.Constructor(Vector3(3677.272f, 2140.784f, 72.89651f)), owning_building_guid = 22)
      LocalObject(1170, Locker.Constructor(Vector3(3679.78f, 2140.564f, 72.66752f)), owning_building_guid = 22)
      LocalObject(1171, Locker.Constructor(Vector3(3680.81f, 2140.474f, 72.66752f)), owning_building_guid = 22)
      LocalObject(1172, Locker.Constructor(Vector3(3681.86f, 2140.382f, 72.66752f)), owning_building_guid = 22)
      LocalObject(1173, Locker.Constructor(Vector3(3682.89f, 2140.292f, 72.66752f)), owning_building_guid = 22)
      LocalObject(1174, Locker.Constructor(Vector3(3685.402f, 2140.073f, 72.89651f)), owning_building_guid = 22)
      LocalObject(1175, Locker.Constructor(Vector3(3686.432f, 2139.982f, 72.89651f)), owning_building_guid = 22)
      LocalObject(
        161,
        Terminal.Constructor(Vector3(3735.192f, 2137.559f, 62.88651f), cert_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        162,
        Terminal.Constructor(Vector3(3736.299f, 2150.211f, 62.88651f), cert_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        163,
        Terminal.Constructor(Vector3(3736.509f, 2135.991f, 62.88651f), cert_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        164,
        Terminal.Constructor(Vector3(3737.868f, 2151.527f, 62.88651f), cert_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        165,
        Terminal.Constructor(Vector3(3743.806f, 2135.352f, 62.88651f), cert_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        166,
        Terminal.Constructor(Vector3(3745.165f, 2150.889f, 62.88651f), cert_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        167,
        Terminal.Constructor(Vector3(3745.375f, 2136.669f, 62.88651f), cert_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        168,
        Terminal.Constructor(Vector3(3746.481f, 2149.32f, 62.88651f), cert_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1347,
        Terminal.Constructor(Vector3(3695.754f, 2138.771f, 64.48651f), order_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1348,
        Terminal.Constructor(Vector3(3699.471f, 2138.446f, 64.48651f), order_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1349,
        Terminal.Constructor(Vector3(3700.364f, 2167.16f, 74.19151f), order_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1350,
        Terminal.Constructor(Vector3(3703.245f, 2138.116f, 64.48651f), order_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1912,
        Terminal.Constructor(Vector3(3635.028f, 2178.961f, 67.00951f), spawn_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1913,
        Terminal.Constructor(Vector3(3676.245f, 2157.291f, 74.47552f), spawn_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1914,
        Terminal.Constructor(Vector3(3692.797f, 2152.765f, 65.03052f), spawn_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1915,
        Terminal.Constructor(Vector3(3692.896f, 2197.899f, 64.50951f), spawn_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1916,
        Terminal.Constructor(Vector3(3700.062f, 2152.134f, 65.03052f), spawn_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1917,
        Terminal.Constructor(Vector3(3707.322f, 2151.496f, 65.03052f), spawn_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1918,
        Terminal.Constructor(Vector3(3753.808f, 2153.013f, 64.50951f), spawn_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        2082,
        Terminal.Constructor(Vector3(3623.775f, 2126.022f, 75.20152f), vehicle_terminal_combined),
        owning_building_guid = 22
      )
      LocalObject(
        1286,
        VehicleSpawnPad.Constructor(Vector3(3624.874f, 2139.617f, 71.04352f), mb_pad_creation, Vector3(0, 0, 5)),
        owning_building_guid = 22,
        terminal_guid = 2082
      )
      LocalObject(1803, ResourceSilo.Constructor(Vector3(3745.911f, 2213.606f, 79.91351f)), owning_building_guid = 22)
      LocalObject(
        1830,
        SpawnTube.Constructor(Vector3(3694.296f, 2153.375f, 62.89651f), Vector3(0, 0, 95)),
        owning_building_guid = 22
      )
      LocalObject(
        1831,
        SpawnTube.Constructor(Vector3(3701.56f, 2152.74f, 62.89651f), Vector3(0, 0, 95)),
        owning_building_guid = 22
      )
      LocalObject(
        1833,
        SpawnTube.Constructor(Vector3(3708.82f, 2152.104f, 62.89651f), Vector3(0, 0, 95)),
        owning_building_guid = 22
      )
      LocalObject(
        114,
        ProximityTerminal.Constructor(Vector3(3682.746f, 2156.644f, 72.70651f), adv_med_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1301,
        ProximityTerminal.Constructor(Vector3(3691.528f, 2130.119f, 62.89651f), medical_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1492,
        ProximityTerminal.Constructor(Vector3(3621.198f, 2165.828f, 81.24851f), pad_landing_frame),
        owning_building_guid = 22
      )
      LocalObject(
        1493,
        Terminal.Constructor(Vector3(3621.198f, 2165.828f, 81.24851f), air_rearm_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1495,
        ProximityTerminal.Constructor(Vector3(3629.876f, 2181.446f, 83.18951f), pad_landing_frame),
        owning_building_guid = 22
      )
      LocalObject(
        1496,
        Terminal.Constructor(Vector3(3629.876f, 2181.446f, 83.18951f), air_rearm_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1498,
        ProximityTerminal.Constructor(Vector3(3734.966f, 2152.738f, 83.22852f), pad_landing_frame),
        owning_building_guid = 22
      )
      LocalObject(
        1499,
        Terminal.Constructor(Vector3(3734.966f, 2152.738f, 83.22852f), air_rearm_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1501,
        ProximityTerminal.Constructor(Vector3(3744.745f, 2168.166f, 81.23852f), pad_landing_frame),
        owning_building_guid = 22
      )
      LocalObject(
        1502,
        Terminal.Constructor(Vector3(3744.745f, 2168.166f, 81.23852f), air_rearm_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1745,
        ProximityTerminal.Constructor(Vector3(3608.569f, 2200.735f, 72.64651f), repair_silo),
        owning_building_guid = 22
      )
      LocalObject(
        1746,
        Terminal.Constructor(Vector3(3608.569f, 2200.735f, 72.64651f), ground_rearm_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1749,
        ProximityTerminal.Constructor(Vector3(3701.222f, 2104.288f, 72.64651f), repair_silo),
        owning_building_guid = 22
      )
      LocalObject(
        1750,
        Terminal.Constructor(Vector3(3701.222f, 2104.288f, 72.64651f), ground_rearm_terminal),
        owning_building_guid = 22
      )
      LocalObject(
        1226,
        FacilityTurret.Constructor(Vector3(3588.193f, 2103.038f, 81.29852f), manned_turret),
        owning_building_guid = 22
      )
      TurretToWeapon(1226, 5026)
      LocalObject(
        1228,
        FacilityTurret.Constructor(Vector3(3599.987f, 2237.749f, 81.29852f), manned_turret),
        owning_building_guid = 22
      )
      TurretToWeapon(1228, 5027)
      LocalObject(
        1229,
        FacilityTurret.Constructor(Vector3(3722.701f, 2090.078f, 81.29852f), manned_turret),
        owning_building_guid = 22
      )
      TurretToWeapon(1229, 5028)
      LocalObject(
        1232,
        FacilityTurret.Constructor(Vector3(3769.406f, 2129.305f, 81.29852f), manned_turret),
        owning_building_guid = 22
      )
      TurretToWeapon(1232, 5029)
      LocalObject(
        1233,
        FacilityTurret.Constructor(Vector3(3776.407f, 2222.322f, 81.29852f), manned_turret),
        owning_building_guid = 22
      )
      TurretToWeapon(1233, 5030)
      LocalObject(
        670,
        ImplantTerminalMech.Constructor(Vector3(3733.322f, 2144.105f, 62.37352f)),
        owning_building_guid = 22
      )
      LocalObject(
        664,
        Terminal.Constructor(Vector3(3733.34f, 2144.104f, 62.37352f), implant_terminal_interface),
        owning_building_guid = 22
      )
      TerminalToInterface(670, 664)
      LocalObject(
        671,
        ImplantTerminalMech.Constructor(Vector3(3748.62f, 2142.779f, 62.37352f)),
        owning_building_guid = 22
      )
      LocalObject(
        665,
        Terminal.Constructor(Vector3(3748.603f, 2142.781f, 62.37352f), implant_terminal_interface),
        owning_building_guid = 22
      )
      TerminalToInterface(671, 665)
      LocalObject(
        1617,
        Painbox.Constructor(Vector3(3669.921f, 2154.142f, 86.92532f), painbox),
        owning_building_guid = 22
      )
      LocalObject(
        1626,
        Painbox.Constructor(Vector3(3696.397f, 2142.631f, 66.96642f), painbox_continuous),
        owning_building_guid = 22
      )
      LocalObject(
        1635,
        Painbox.Constructor(Vector3(3684.453f, 2154.287f, 87.13042f), painbox_door_radius),
        owning_building_guid = 22
      )
      LocalObject(
        1648,
        Painbox.Constructor(Vector3(3686.883f, 2147.693f, 65.25241f), painbox_door_radius_continuous),
        owning_building_guid = 22
      )
      LocalObject(
        1649,
        Painbox.Constructor(Vector3(3706.548f, 2128.564f, 66.43742f), painbox_door_radius_continuous),
        owning_building_guid = 22
      )
      LocalObject(
        1650,
        Painbox.Constructor(Vector3(3714.642f, 2143.281f, 64.61072f), painbox_door_radius_continuous),
        owning_building_guid = 22
      )
      LocalObject(215, Generator.Constructor(Vector3(3666.184f, 2156.043f, 81.60252f)), owning_building_guid = 22)
      LocalObject(
        206,
        Terminal.Constructor(Vector3(3674.349f, 2155.376f, 82.89651f), gen_control),
        owning_building_guid = 22
      )
    }

    Building7()

    def Building7(): Unit = { // Name: Aton Type: cryo_facility GUID: 25, MapID: 7
      LocalBuilding(
        "Aton",
        25,
        7,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3830f, 5418f, 54.14594f),
            Vector3(0f, 0f, 90f),
            cryo_facility
          )
        )
      )
      LocalObject(
        155,
        CaptureTerminal.Constructor(Vector3(3770.734f, 5389.911f, 44.14594f), capture_terminal),
        owning_building_guid = 25
      )
      LocalObject(308, Door.Constructor(Vector3(3762.197f, 5375.674f, 55.69694f)), owning_building_guid = 25)
      LocalObject(309, Door.Constructor(Vector3(3762.197f, 5393.867f, 63.66094f)), owning_building_guid = 25)
      LocalObject(310, Door.Constructor(Vector3(3807.307f, 5359.023f, 63.66094f)), owning_building_guid = 25)
      LocalObject(311, Door.Constructor(Vector3(3810f, 5402f, 65.66694f)), owning_building_guid = 25)
      LocalObject(312, Door.Constructor(Vector3(3825.5f, 5359.023f, 55.69694f)), owning_building_guid = 25)
      LocalObject(313, Door.Constructor(Vector3(3826.863f, 5422f, 65.66694f)), owning_building_guid = 25)
      LocalObject(315, Door.Constructor(Vector3(3862.5f, 5468.927f, 55.69694f)), owning_building_guid = 25)
      LocalObject(320, Door.Constructor(Vector3(3880.693f, 5468.927f, 63.66094f)), owning_building_guid = 25)
      LocalObject(321, Door.Constructor(Vector3(3912.927f, 5423.307f, 63.66094f)), owning_building_guid = 25)
      LocalObject(322, Door.Constructor(Vector3(3912.927f, 5441.5f, 55.69694f)), owning_building_guid = 25)
      LocalObject(323, Door.Constructor(Vector3(3922f, 5450f, 55.66694f)), owning_building_guid = 25)
      LocalObject(519, Door.Constructor(Vector3(3762f, 5386f, 45.66694f)), owning_building_guid = 25)
      LocalObject(520, Door.Constructor(Vector3(3762f, 5394f, 45.66694f)), owning_building_guid = 25)
      LocalObject(521, Door.Constructor(Vector3(3762f, 5402f, 45.66694f)), owning_building_guid = 25)
      LocalObject(522, Door.Constructor(Vector3(3778f, 5418f, 45.66694f)), owning_building_guid = 25)
      LocalObject(523, Door.Constructor(Vector3(3790f, 5382f, 45.66694f)), owning_building_guid = 25)
      LocalObject(524, Door.Constructor(Vector3(3790f, 5406f, 45.66694f)), owning_building_guid = 25)
      LocalObject(525, Door.Constructor(Vector3(3806f, 5430f, 45.66694f)), owning_building_guid = 25)
      LocalObject(526, Door.Constructor(Vector3(3810f, 5386f, 45.66694f)), owning_building_guid = 25)
      LocalObject(527, Door.Constructor(Vector3(3810f, 5402f, 45.66694f)), owning_building_guid = 25)
      LocalObject(528, Door.Constructor(Vector3(3810f, 5402f, 55.66694f)), owning_building_guid = 25)
      LocalObject(529, Door.Constructor(Vector3(3810f, 5442f, 45.66694f)), owning_building_guid = 25)
      LocalObject(530, Door.Constructor(Vector3(3826f, 5442f, 38.16694f)), owning_building_guid = 25)
      LocalObject(531, Door.Constructor(Vector3(3830f, 5382f, 45.66694f)), owning_building_guid = 25)
      LocalObject(532, Door.Constructor(Vector3(3830f, 5406f, 45.66694f)), owning_building_guid = 25)
      LocalObject(533, Door.Constructor(Vector3(3830f, 5430f, 45.66694f)), owning_building_guid = 25)
      LocalObject(534, Door.Constructor(Vector3(3834f, 5418f, 45.66694f)), owning_building_guid = 25)
      LocalObject(535, Door.Constructor(Vector3(3838f, 5422f, 65.66694f)), owning_building_guid = 25)
      LocalObject(536, Door.Constructor(Vector3(3842f, 5370f, 45.66694f)), owning_building_guid = 25)
      LocalObject(537, Door.Constructor(Vector3(3858f, 5418f, 45.66694f)), owning_building_guid = 25)
      LocalObject(538, Door.Constructor(Vector3(3862f, 5398f, 45.66694f)), owning_building_guid = 25)
      LocalObject(539, Door.Constructor(Vector3(3886f, 5398f, 48.16694f)), owning_building_guid = 25)
      LocalObject(540, Door.Constructor(Vector3(3890f, 5378f, 48.16694f)), owning_building_guid = 25)
      LocalObject(541, Door.Constructor(Vector3(3918f, 5406f, 48.16694f)), owning_building_guid = 25)
      LocalObject(652, Door.Constructor(Vector3(3804.917f, 5421.992f, 56.42894f)), owning_building_guid = 25)
      LocalObject(660, Door.Constructor(Vector3(3822f, 5414f, 55.66494f)), owning_building_guid = 25)
      LocalObject(661, Door.Constructor(Vector3(3830f, 5430f, 55.66694f)), owning_building_guid = 25)
      LocalObject(1984, Door.Constructor(Vector3(3809.685f, 5422.673f, 45.99994f)), owning_building_guid = 25)
      LocalObject(1985, Door.Constructor(Vector3(3816.974f, 5422.673f, 45.99994f)), owning_building_guid = 25)
      LocalObject(1986, Door.Constructor(Vector3(3824.267f, 5422.673f, 45.99994f)), owning_building_guid = 25)
      LocalObject(
        688,
        IFFLock.Constructor(Vector3(3802.178f, 5418.77f, 55.62794f), Vector3(0, 0, 270)),
        owning_building_guid = 25,
        door_guid = 652
      )
      LocalObject(
        764,
        IFFLock.Constructor(Vector3(3760.428f, 5394.813f, 45.48194f), Vector3(0, 0, 0)),
        owning_building_guid = 25,
        door_guid = 520
      )
      LocalObject(
        766,
        IFFLock.Constructor(Vector3(3763.572f, 5385.06f, 45.48194f), Vector3(0, 0, 180)),
        owning_building_guid = 25,
        door_guid = 519
      )
      LocalObject(
        767,
        IFFLock.Constructor(Vector3(3805.19f, 5428.428f, 45.48194f), Vector3(0, 0, 270)),
        owning_building_guid = 25,
        door_guid = 525
      )
      LocalObject(
        768,
        IFFLock.Constructor(Vector3(3807.957f, 5402.814f, 65.59794f), Vector3(0, 0, 0)),
        owning_building_guid = 25,
        door_guid = 311
      )
      LocalObject(
        769,
        IFFLock.Constructor(Vector3(3826.042f, 5419.954f, 65.59794f), Vector3(0, 0, 270)),
        owning_building_guid = 25,
        door_guid = 313
      )
      LocalObject(
        770,
        IFFLock.Constructor(Vector3(3827.572f, 5441.19f, 37.98194f), Vector3(0, 0, 180)),
        owning_building_guid = 25,
        door_guid = 530
      )
      LocalObject(
        771,
        IFFLock.Constructor(Vector3(3830.81f, 5431.572f, 45.48194f), Vector3(0, 0, 90)),
        owning_building_guid = 25,
        door_guid = 533
      )
      LocalObject(
        776,
        IFFLock.Constructor(Vector3(3919.958f, 5450.814f, 55.59794f), Vector3(0, 0, 0)),
        owning_building_guid = 25,
        door_guid = 323
      )
      LocalObject(982, Locker.Constructor(Vector3(3775.473f, 5437.997f, 44.14094f)), owning_building_guid = 25)
      LocalObject(983, Locker.Constructor(Vector3(3776.725f, 5437.997f, 44.14094f)), owning_building_guid = 25)
      LocalObject(984, Locker.Constructor(Vector3(3777.987f, 5437.997f, 44.14094f)), owning_building_guid = 25)
      LocalObject(985, Locker.Constructor(Vector3(3779.248f, 5437.997f, 44.14094f)), owning_building_guid = 25)
      LocalObject(986, Locker.Constructor(Vector3(3780.504f, 5437.997f, 44.14094f)), owning_building_guid = 25)
      LocalObject(987, Locker.Constructor(Vector3(3807.639f, 5450.814f, 44.05394f)), owning_building_guid = 25)
      LocalObject(988, Locker.Constructor(Vector3(3807.639f, 5451.868f, 44.05394f)), owning_building_guid = 25)
      LocalObject(989, Locker.Constructor(Vector3(3807.639f, 5452.929f, 44.05394f)), owning_building_guid = 25)
      LocalObject(990, Locker.Constructor(Vector3(3807.639f, 5453.984f, 44.05394f)), owning_building_guid = 25)
      LocalObject(991, Locker.Constructor(Vector3(3807.639f, 5455.039f, 44.05394f)), owning_building_guid = 25)
      LocalObject(992, Locker.Constructor(Vector3(3807.639f, 5456.095f, 44.05394f)), owning_building_guid = 25)
      LocalObject(993, Locker.Constructor(Vector3(3827.64f, 5450.817f, 44.05394f)), owning_building_guid = 25)
      LocalObject(994, Locker.Constructor(Vector3(3827.64f, 5451.873f, 44.05394f)), owning_building_guid = 25)
      LocalObject(995, Locker.Constructor(Vector3(3827.64f, 5452.928f, 44.05394f)), owning_building_guid = 25)
      LocalObject(996, Locker.Constructor(Vector3(3827.64f, 5453.983f, 44.05394f)), owning_building_guid = 25)
      LocalObject(997, Locker.Constructor(Vector3(3827.64f, 5455.043f, 44.05394f)), owning_building_guid = 25)
      LocalObject(998, Locker.Constructor(Vector3(3827.64f, 5456.098f, 44.05394f)), owning_building_guid = 25)
      LocalObject(999, Locker.Constructor(Vector3(3827.859f, 5433.563f, 44.40694f)), owning_building_guid = 25)
      LocalObject(1000, Locker.Constructor(Vector3(3827.859f, 5434.727f, 44.40694f)), owning_building_guid = 25)
      LocalObject(1001, Locker.Constructor(Vector3(3827.859f, 5435.874f, 44.40694f)), owning_building_guid = 25)
      LocalObject(1002, Locker.Constructor(Vector3(3827.859f, 5437.023f, 44.40694f)), owning_building_guid = 25)
      LocalObject(1176, Locker.Constructor(Vector3(3831.984f, 5436.26f, 54.14594f)), owning_building_guid = 25)
      LocalObject(1177, Locker.Constructor(Vector3(3833.018f, 5436.26f, 54.14594f)), owning_building_guid = 25)
      LocalObject(1178, Locker.Constructor(Vector3(3835.54f, 5436.26f, 53.91694f)), owning_building_guid = 25)
      LocalObject(1179, Locker.Constructor(Vector3(3836.574f, 5436.26f, 53.91694f)), owning_building_guid = 25)
      LocalObject(1180, Locker.Constructor(Vector3(3837.628f, 5436.26f, 53.91694f)), owning_building_guid = 25)
      LocalObject(1181, Locker.Constructor(Vector3(3838.662f, 5436.26f, 53.91694f)), owning_building_guid = 25)
      LocalObject(1182, Locker.Constructor(Vector3(3841.179f, 5436.26f, 54.14594f)), owning_building_guid = 25)
      LocalObject(1183, Locker.Constructor(Vector3(3842.213f, 5436.26f, 54.14594f)), owning_building_guid = 25)
      LocalObject(
        169,
        Terminal.Constructor(Vector3(3772.977f, 5421.724f, 44.13594f), cert_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        170,
        Terminal.Constructor(Vector3(3772.977f, 5434.424f, 44.13594f), cert_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        171,
        Terminal.Constructor(Vector3(3774.425f, 5420.276f, 44.13594f), cert_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        172,
        Terminal.Constructor(Vector3(3774.425f, 5435.872f, 44.13594f), cert_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        173,
        Terminal.Constructor(Vector3(3781.75f, 5420.276f, 44.13594f), cert_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        174,
        Terminal.Constructor(Vector3(3781.75f, 5435.872f, 44.13594f), cert_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        175,
        Terminal.Constructor(Vector3(3783.198f, 5421.724f, 44.13594f), cert_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        176,
        Terminal.Constructor(Vector3(3783.198f, 5434.424f, 44.13594f), cert_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1354,
        Terminal.Constructor(Vector3(3815.072f, 5436.654f, 45.73594f), order_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1355,
        Terminal.Constructor(Vector3(3818.861f, 5436.654f, 45.73594f), order_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1356,
        Terminal.Constructor(Vector3(3820.474f, 5407.972f, 55.44094f), order_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1357,
        Terminal.Constructor(Vector3(3822.592f, 5436.654f, 45.73594f), order_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1919,
        Terminal.Constructor(Vector3(3766f, 5417.407f, 45.75894f), spawn_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1920,
        Terminal.Constructor(Vector3(3812.177f, 5422.97f, 46.27994f), spawn_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1921,
        Terminal.Constructor(Vector3(3819.465f, 5422.967f, 46.27994f), spawn_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1922,
        Terminal.Constructor(Vector3(3826.757f, 5422.971f, 46.27994f), spawn_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1923,
        Terminal.Constructor(Vector3(3830.593f, 5378f, 45.75894f), spawn_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1924,
        Terminal.Constructor(Vector3(3843.641f, 5419.905f, 55.72494f), spawn_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1925,
        Terminal.Constructor(Vector3(3886.59f, 5401.91f, 48.25894f), spawn_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        2083,
        Terminal.Constructor(Vector3(3893.186f, 5455.628f, 56.45094f), vehicle_terminal_combined),
        owning_building_guid = 25
      )
      LocalObject(
        1287,
        VehicleSpawnPad.Constructor(Vector3(3893.276f, 5441.989f, 52.29294f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 25,
        terminal_guid = 2083
      )
      LocalObject(1804, ResourceSilo.Constructor(Vector3(3779.148f, 5357.733f, 61.16294f)), owning_building_guid = 25)
      LocalObject(
        1835,
        SpawnTube.Constructor(Vector3(3810.738f, 5422.233f, 44.14594f), Vector3(0, 0, 270)),
        owning_building_guid = 25
      )
      LocalObject(
        1836,
        SpawnTube.Constructor(Vector3(3818.026f, 5422.233f, 44.14594f), Vector3(0, 0, 270)),
        owning_building_guid = 25
      )
      LocalObject(
        1837,
        SpawnTube.Constructor(Vector3(3825.317f, 5422.233f, 44.14594f), Vector3(0, 0, 270)),
        owning_building_guid = 25
      )
      LocalObject(
        115,
        ProximityTerminal.Constructor(Vector3(3837.108f, 5419.983f, 53.95594f), adv_med_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1302,
        ProximityTerminal.Constructor(Vector3(3826.048f, 5445.642f, 44.14594f), medical_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1504,
        ProximityTerminal.Constructor(Vector3(3776.349f, 5403.101f, 62.48794f), pad_landing_frame),
        owning_building_guid = 25
      )
      LocalObject(
        1505,
        Terminal.Constructor(Vector3(3776.349f, 5403.101f, 62.48794f), air_rearm_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1507,
        ProximityTerminal.Constructor(Vector3(3784.747f, 5419.323f, 64.47794f), pad_landing_frame),
        owning_building_guid = 25
      )
      LocalObject(
        1508,
        Terminal.Constructor(Vector3(3784.747f, 5419.323f, 64.47794f), air_rearm_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1510,
        ProximityTerminal.Constructor(Vector3(3891.939f, 5399.883f, 64.43894f), pad_landing_frame),
        owning_building_guid = 25
      )
      LocalObject(
        1511,
        Terminal.Constructor(Vector3(3891.939f, 5399.883f, 64.43894f), air_rearm_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1513,
        ProximityTerminal.Constructor(Vector3(3899.223f, 5416.198f, 62.49794f), pad_landing_frame),
        owning_building_guid = 25
      )
      LocalObject(
        1514,
        Terminal.Constructor(Vector3(3899.223f, 5416.198f, 62.49794f), air_rearm_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1753,
        ProximityTerminal.Constructor(Vector3(3814.139f, 5470.53f, 53.89594f), repair_silo),
        owning_building_guid = 25
      )
      LocalObject(
        1754,
        Terminal.Constructor(Vector3(3814.139f, 5470.53f, 53.89594f), ground_rearm_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1757,
        ProximityTerminal.Constructor(Vector3(3914.846f, 5382.524f, 53.89594f), repair_silo),
        owning_building_guid = 25
      )
      LocalObject(
        1758,
        Terminal.Constructor(Vector3(3914.846f, 5382.524f, 53.89594f), ground_rearm_terminal),
        owning_building_guid = 25
      )
      LocalObject(
        1230,
        FacilityTurret.Constructor(Vector3(3748.395f, 5439.665f, 62.54794f), manned_turret),
        owning_building_guid = 25
      )
      TurretToWeapon(1230, 5031)
      LocalObject(
        1231,
        FacilityTurret.Constructor(Vector3(3749.528f, 5346.392f, 62.54794f), manned_turret),
        owning_building_guid = 25
      )
      TurretToWeapon(1231, 5032)
      LocalObject(
        1234,
        FacilityTurret.Constructor(Vector3(3791.504f, 5482.813f, 62.54794f), manned_turret),
        owning_building_guid = 25
      )
      TurretToWeapon(1234, 5033)
      LocalObject(
        1237,
        FacilityTurret.Constructor(Vector3(3926.621f, 5346.4f, 62.54794f), manned_turret),
        owning_building_guid = 25
      )
      TurretToWeapon(1237, 5034)
      LocalObject(
        1238,
        FacilityTurret.Constructor(Vector3(3926.629f, 5481.626f, 62.54794f), manned_turret),
        owning_building_guid = 25
      )
      TurretToWeapon(1238, 5035)
      LocalObject(
        672,
        ImplantTerminalMech.Constructor(Vector3(3770.276f, 5428.054f, 43.62294f)),
        owning_building_guid = 25
      )
      LocalObject(
        666,
        Terminal.Constructor(Vector3(3770.294f, 5428.054f, 43.62294f), implant_terminal_interface),
        owning_building_guid = 25
      )
      TerminalToInterface(672, 666)
      LocalObject(
        673,
        ImplantTerminalMech.Constructor(Vector3(3785.632f, 5428.066f, 43.62294f)),
        owning_building_guid = 25
      )
      LocalObject(
        667,
        Terminal.Constructor(Vector3(3785.614f, 5428.066f, 43.62294f), implant_terminal_interface),
        owning_building_guid = 25
      )
      TerminalToInterface(673, 667)
      LocalObject(
        1618,
        Painbox.Constructor(Vector3(3849.666f, 5423.593f, 68.17474f), painbox),
        owning_building_guid = 25
      )
      LocalObject(
        1627,
        Painbox.Constructor(Vector3(3822.288f, 5432.753f, 48.21584f), painbox_continuous),
        owning_building_guid = 25
      )
      LocalObject(
        1636,
        Painbox.Constructor(Vector3(3835.202f, 5422.182f, 68.37984f), painbox_door_radius),
        owning_building_guid = 25
      )
      LocalObject(
        1651,
        Painbox.Constructor(Vector3(3804.169f, 5430.516f, 45.86014f), painbox_door_radius_continuous),
        owning_building_guid = 25
      )
      LocalObject(
        1652,
        Painbox.Constructor(Vector3(3810.95f, 5445.882f, 47.68684f), painbox_door_radius_continuous),
        owning_building_guid = 25
      )
      LocalObject(
        1653,
        Painbox.Constructor(Vector3(3832.207f, 5428.54f, 46.50184f), painbox_door_radius_continuous),
        owning_building_guid = 25
      )
      LocalObject(216, Generator.Constructor(Vector3(3853.555f, 5422.025f, 62.85194f)), owning_building_guid = 25)
      LocalObject(
        207,
        Terminal.Constructor(Vector3(3845.363f, 5421.978f, 64.14594f), gen_control),
        owning_building_guid = 25
      )
    }

    Building9()

    def Building9(): Unit = { // Name: Thoth Type: cryo_facility GUID: 28, MapID: 9
      LocalBuilding(
        "Thoth",
        28,
        9,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4556f, 3338f, 53.19376f),
            Vector3(0f, 0f, 335f),
            cryo_facility
          )
        )
      )
      LocalObject(
        159,
        CaptureTerminal.Constructor(Vector3(4555.59f, 3403.584f, 43.19376f), capture_terminal),
        owning_building_guid = 28
      )
      LocalObject(377, Door.Constructor(Vector3(4504.451f, 3367.003f, 54.74476f)), owning_building_guid = 28)
      LocalObject(387, Door.Constructor(Vector3(4512.139f, 3383.492f, 62.70876f)), owning_building_guid = 28)
      LocalObject(393, Door.Constructor(Vector3(4525.763f, 3260.6f, 62.70876f)), owning_building_guid = 28)
      LocalObject(395, Door.Constructor(Vector3(4542.252f, 3252.911f, 54.74476f)), owning_building_guid = 28)
      LocalObject(397, Door.Constructor(Vector3(4546.121f, 3241.096f, 54.71476f)), owning_building_guid = 28)
      LocalObject(398, Door.Constructor(Vector3(4546.294f, 3417.338f, 54.74476f)), owning_building_guid = 28)
      LocalObject(399, Door.Constructor(Vector3(4549.952f, 3362.888f, 64.71477f)), owning_building_guid = 28)
      LocalObject(400, Door.Constructor(Vector3(4560.951f, 3339.153f, 64.71477f)), owning_building_guid = 28)
      LocalObject(402, Door.Constructor(Vector3(4562.783f, 3409.649f, 62.70876f)), owning_building_guid = 28)
      LocalObject(404, Door.Constructor(Vector3(4580.732f, 3270.534f, 62.70876f)), owning_building_guid = 28)
      LocalObject(405, Door.Constructor(Vector3(4588.42f, 3287.022f, 54.74476f)), owning_building_guid = 28)
      LocalObject(587, Door.Constructor(Vector3(4494.391f, 3300.526f, 47.21476f)), owning_building_guid = 28)
      LocalObject(589, Door.Constructor(Vector3(4507.426f, 3347.41f, 44.71476f)), owning_building_guid = 28)
      LocalObject(590, Door.Constructor(Vector3(4507.934f, 3263.316f, 47.21476f)), owning_building_guid = 28)
      LocalObject(591, Door.Constructor(Vector3(4514.207f, 3295.699f, 47.21476f)), owning_building_guid = 28)
      LocalObject(596, Door.Constructor(Vector3(4523.373f, 3353.214f, 44.71476f)), owning_building_guid = 28)
      LocalObject(597, Door.Constructor(Vector3(4524.35f, 3317.45f, 44.71476f)), owning_building_guid = 28)
      LocalObject(605, Door.Constructor(Vector3(4535.451f, 3369.65f, 44.71476f)), owning_building_guid = 28)
      LocalObject(607, Door.Constructor(Vector3(4540.278f, 3389.467f, 44.71476f)), owning_building_guid = 28)
      LocalObject(610, Door.Constructor(Vector3(4544.167f, 3312.623f, 44.71476f)), owning_building_guid = 28)
      LocalObject(611, Door.Constructor(Vector3(4545.125f, 3343.072f, 44.71476f)), owning_building_guid = 28)
      LocalObject(614, Door.Constructor(Vector3(4549.952f, 3362.888f, 44.71476f)), owning_building_guid = 28)
      LocalObject(615, Door.Constructor(Vector3(4549.952f, 3362.888f, 54.71476f)), owning_building_guid = 28)
      LocalObject(616, Door.Constructor(Vector3(4554.31f, 3334.375f, 44.71476f)), owning_building_guid = 28)
      LocalObject(617, Door.Constructor(Vector3(4555.736f, 3413.153f, 44.71476f)), owning_building_guid = 28)
      LocalObject(618, Door.Constructor(Vector3(4556.244f, 3329.059f, 64.71477f)), owning_building_guid = 28)
      LocalObject(619, Door.Constructor(Vector3(4562.029f, 3379.324f, 44.71476f)), owning_building_guid = 28)
      LocalObject(620, Door.Constructor(Vector3(4562.987f, 3409.772f, 44.71476f)), owning_building_guid = 28)
      LocalObject(621, Door.Constructor(Vector3(4566.875f, 3332.928f, 44.71476f)), owning_building_guid = 28)
      LocalObject(622, Door.Constructor(Vector3(4570.237f, 3406.391f, 44.71476f)), owning_building_guid = 28)
      LocalObject(623, Door.Constructor(Vector3(4577.019f, 3354.68f, 44.71476f)), owning_building_guid = 28)
      LocalObject(624, Door.Constructor(Vector3(4577.976f, 3385.128f, 44.71476f)), owning_building_guid = 28)
      LocalObject(625, Door.Constructor(Vector3(4579.442f, 3331.482f, 37.21476f)), owning_building_guid = 28)
      LocalObject(626, Door.Constructor(Vector3(4586.204f, 3345.983f, 44.71476f)), owning_building_guid = 28)
      LocalObject(656, Door.Constructor(Vector3(4570.219f, 3359.046f, 55.47676f)), owning_building_guid = 28)
      LocalObject(662, Door.Constructor(Vector3(4555.756f, 3346.941f, 54.71276f)), owning_building_guid = 28)
      LocalObject(663, Door.Constructor(Vector3(4566.875f, 3332.928f, 54.71476f)), owning_building_guid = 28)
      LocalObject(2010, Door.Constructor(Vector3(4562.658f, 3341.221f, 45.04776f)), owning_building_guid = 28)
      LocalObject(2011, Door.Constructor(Vector3(4565.74f, 3347.831f, 45.04776f)), owning_building_guid = 28)
      LocalObject(2012, Door.Constructor(Vector3(4568.821f, 3354.437f, 45.04776f)), owning_building_guid = 28)
      LocalObject(
        692,
        IFFLock.Constructor(Vector3(4568.456f, 3362.89f, 54.67576f), Vector3(0, 0, 25)),
        owning_building_guid = 28,
        door_guid = 656
      )
      LocalObject(
        834,
        IFFLock.Constructor(Vector3(4547.722f, 3242.603f, 54.64576f), Vector3(0, 0, 115)),
        owning_building_guid = 28,
        door_guid = 397
      )
      LocalObject(
        835,
        IFFLock.Constructor(Vector3(4551.553f, 3364.396f, 64.64577f), Vector3(0, 0, 115)),
        owning_building_guid = 28,
        door_guid = 399
      )
      LocalObject(
        836,
        IFFLock.Constructor(Vector3(4554.22f, 3412.125f, 44.52976f), Vector3(0, 0, 295)),
        owning_building_guid = 28,
        door_guid = 617
      )
      LocalObject(
        837,
        IFFLock.Constructor(Vector3(4559.444f, 3340.761f, 64.64577f), Vector3(0, 0, 25)),
        owning_building_guid = 28,
        door_guid = 400
      )
      LocalObject(
        838,
        IFFLock.Constructor(Vector3(4564.388f, 3410.853f, 44.52976f), Vector3(0, 0, 115)),
        owning_building_guid = 28,
        door_guid = 620
      )
      LocalObject(
        839,
        IFFLock.Constructor(Vector3(4567.958f, 3331.53f, 44.52976f), Vector3(0, 0, 205)),
        owning_building_guid = 28,
        door_guid = 621
      )
      LocalObject(
        840,
        IFFLock.Constructor(Vector3(4575.936f, 3356.078f, 44.52976f), Vector3(0, 0, 25)),
        owning_building_guid = 28,
        door_guid = 623
      )
      LocalObject(
        841,
        IFFLock.Constructor(Vector3(4578.043f, 3330.4f, 37.02976f), Vector3(0, 0, 295)),
        owning_building_guid = 28,
        door_guid = 625
      )
      LocalObject(1095, Locker.Constructor(Vector3(4571.01f, 3333.363f, 43.45477f)), owning_building_guid = 28)
      LocalObject(1096, Locker.Constructor(Vector3(4572.064f, 3332.871f, 43.45477f)), owning_building_guid = 28)
      LocalObject(1097, Locker.Constructor(Vector3(4573.104f, 3332.386f, 43.45477f)), owning_building_guid = 28)
      LocalObject(1098, Locker.Constructor(Vector3(4574.146f, 3331.901f, 43.45477f)), owning_building_guid = 28)
      LocalObject(1099, Locker.Constructor(Vector3(4586.74f, 3326.27f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1100, Locker.Constructor(Vector3(4587.697f, 3325.823f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1101, Locker.Constructor(Vector3(4588.653f, 3325.378f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1102, Locker.Constructor(Vector3(4589.609f, 3324.932f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1103, Locker.Constructor(Vector3(4590.57f, 3324.484f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1104, Locker.Constructor(Vector3(4591.526f, 3324.038f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1105, Locker.Constructor(Vector3(4595.042f, 3374.407f, 43.18876f)), owning_building_guid = 28)
      LocalObject(1106, Locker.Constructor(Vector3(4595.19f, 3344.398f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1107, Locker.Constructor(Vector3(4595.572f, 3375.546f, 43.18876f)), owning_building_guid = 28)
      LocalObject(1108, Locker.Constructor(Vector3(4596.145f, 3343.953f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1109, Locker.Constructor(Vector3(4596.105f, 3376.689f, 43.18876f)), owning_building_guid = 28)
      LocalObject(1110, Locker.Constructor(Vector3(4596.638f, 3377.833f, 43.18876f)), owning_building_guid = 28)
      LocalObject(1111, Locker.Constructor(Vector3(4597.106f, 3343.504f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1112, Locker.Constructor(Vector3(4597.167f, 3378.967f, 43.18876f)), owning_building_guid = 28)
      LocalObject(1113, Locker.Constructor(Vector3(4598.063f, 3343.058f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1114, Locker.Constructor(Vector3(4599.019f, 3342.613f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1115, Locker.Constructor(Vector3(4599.976f, 3342.166f, 43.10176f)), owning_building_guid = 28)
      LocalObject(1184, Locker.Constructor(Vector3(4567.388f, 3319.214f, 53.19376f)), owning_building_guid = 28)
      LocalObject(1185, Locker.Constructor(Vector3(4567.825f, 3320.151f, 53.19376f)), owning_building_guid = 28)
      LocalObject(1186, Locker.Constructor(Vector3(4568.889f, 3322.433f, 52.96476f)), owning_building_guid = 28)
      LocalObject(1187, Locker.Constructor(Vector3(4569.326f, 3323.37f, 52.96476f)), owning_building_guid = 28)
      LocalObject(1188, Locker.Constructor(Vector3(4569.771f, 3324.325f, 52.96476f)), owning_building_guid = 28)
      LocalObject(1189, Locker.Constructor(Vector3(4570.208f, 3325.262f, 52.96476f)), owning_building_guid = 28)
      LocalObject(1190, Locker.Constructor(Vector3(4571.274f, 3327.548f, 53.19376f)), owning_building_guid = 28)
      LocalObject(1191, Locker.Constructor(Vector3(4571.711f, 3328.485f, 53.19376f)), owning_building_guid = 28)
      LocalObject(
        177,
        Terminal.Constructor(Vector3(4578.454f, 3380.768f, 43.18376f), cert_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        178,
        Terminal.Constructor(Vector3(4579.154f, 3378.843f, 43.18376f), cert_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        179,
        Terminal.Constructor(Vector3(4581.55f, 3387.406f, 43.18376f), cert_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        180,
        Terminal.Constructor(Vector3(4583.474f, 3388.106f, 43.18376f), cert_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        181,
        Terminal.Constructor(Vector3(4590.665f, 3373.476f, 43.18376f), cert_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        182,
        Terminal.Constructor(Vector3(4592.589f, 3374.176f, 43.18376f), cert_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        183,
        Terminal.Constructor(Vector3(4594.984f, 3382.739f, 43.18376f), cert_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        184,
        Terminal.Constructor(Vector3(4595.685f, 3380.815f, 43.18376f), cert_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1394,
        Terminal.Constructor(Vector3(4550.938f, 3350.872f, 54.48876f), order_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1395,
        Terminal.Constructor(Vector3(4576.037f, 3336.83f, 44.78376f), order_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1396,
        Terminal.Constructor(Vector3(4577.614f, 3340.212f, 44.78376f), order_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1397,
        Terminal.Constructor(Vector3(4579.215f, 3343.646f, 44.78376f), order_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1940,
        Terminal.Constructor(Vector3(4517.501f, 3293.512f, 47.30676f), spawn_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1942,
        Terminal.Constructor(Vector3(4519.497f, 3354.367f, 44.80676f), spawn_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1947,
        Terminal.Constructor(Vector3(4551.961f, 3324.832f, 54.77276f), spawn_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1948,
        Terminal.Constructor(Vector3(4561.876f, 3338.838f, 45.32776f), spawn_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1949,
        Terminal.Constructor(Vector3(4564.954f, 3345.449f, 45.32776f), spawn_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1951,
        Terminal.Constructor(Vector3(4568.037f, 3352.053f, 45.32776f), spawn_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1952,
        Terminal.Constructor(Vector3(4582.51f, 3396.254f, 44.80676f), spawn_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        2087,
        Terminal.Constructor(Vector3(4563.399f, 3264.832f, 55.49876f), vehicle_terminal_combined),
        owning_building_guid = 28
      )
      LocalObject(
        1293,
        VehicleSpawnPad.Constructor(Vector3(4551f, 3270.514f, 51.34076f), mb_pad_creation, Vector3(0, 0, -65)),
        owning_building_guid = 28,
        terminal_guid = 2087
      )
      LocalObject(1807, ResourceSilo.Constructor(Vector3(4522.871f, 3409.558f, 60.21076f)), owning_building_guid = 28)
      LocalObject(
        1861,
        SpawnTube.Constructor(Vector3(4561.815f, 3340.455f, 43.19376f), Vector3(0, 0, 25)),
        owning_building_guid = 28
      )
      LocalObject(
        1862,
        SpawnTube.Constructor(Vector3(4564.897f, 3347.063f, 43.19376f), Vector3(0, 0, 25)),
        owning_building_guid = 28
      )
      LocalObject(
        1863,
        SpawnTube.Constructor(Vector3(4567.977f, 3353.668f, 43.19376f), Vector3(0, 0, 25)),
        owning_building_guid = 28
      )
      LocalObject(
        116,
        ProximityTerminal.Constructor(Vector3(4554.793f, 3330.72f, 53.00377f), adv_med_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1309,
        ProximityTerminal.Constructor(Vector3(4582.722f, 3329.9f, 43.19376f), medical_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1543,
        ProximityTerminal.Constructor(Vector3(4513.404f, 3289.521f, 63.48676f), pad_landing_frame),
        owning_building_guid = 28
      )
      LocalObject(
        1544,
        Terminal.Constructor(Vector3(4513.404f, 3289.521f, 63.48676f), air_rearm_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1549,
        ProximityTerminal.Constructor(Vector3(4525.112f, 3276.024f, 61.54576f), pad_landing_frame),
        owning_building_guid = 28
      )
      LocalObject(
        1550,
        Terminal.Constructor(Vector3(4525.112f, 3276.024f, 61.54576f), air_rearm_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1561,
        ProximityTerminal.Constructor(Vector3(4565.171f, 3392.921f, 61.53576f), pad_landing_frame),
        owning_building_guid = 28
      )
      LocalObject(
        1562,
        Terminal.Constructor(Vector3(4565.171f, 3392.921f, 61.53576f), air_rearm_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1564,
        ProximityTerminal.Constructor(Vector3(4576.324f, 3378.454f, 63.52576f), pad_landing_frame),
        owning_building_guid = 28
      )
      LocalObject(
        1565,
        Terminal.Constructor(Vector3(4576.324f, 3378.454f, 63.52576f), air_rearm_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1781,
        ProximityTerminal.Constructor(Vector3(4487.991f, 3276.096f, 52.94376f), repair_silo),
        owning_building_guid = 28
      )
      LocalObject(
        1782,
        Terminal.Constructor(Vector3(4487.991f, 3276.096f, 52.94376f), ground_rearm_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1789,
        ProximityTerminal.Constructor(Vector3(4610.312f, 3330.175f, 52.94376f), repair_silo),
        owning_building_guid = 28
      )
      LocalObject(
        1790,
        Terminal.Constructor(Vector3(4610.312f, 3330.175f, 52.94376f), ground_rearm_terminal),
        owning_building_guid = 28
      )
      LocalObject(
        1255,
        FacilityTurret.Constructor(Vector3(4450.274f, 3280.691f, 61.59576f), manned_turret),
        owning_building_guid = 28
      )
      TurretToWeapon(1255, 5036)
      LocalObject(
        1261,
        FacilityTurret.Constructor(Vector3(4525.11f, 3441.195f, 61.59576f), manned_turret),
        owning_building_guid = 28
      )
      TurretToWeapon(1261, 5037)
      LocalObject(
        1262,
        FacilityTurret.Constructor(Vector3(4572.828f, 3223.535f, 61.59576f), manned_turret),
        owning_building_guid = 28
      )
      TurretToWeapon(1262, 5038)
      LocalObject(
        1264,
        FacilityTurret.Constructor(Vector3(4610.123f, 3402.803f, 61.59576f), manned_turret),
        owning_building_guid = 28
      )
      TurretToWeapon(1264, 5039)
      LocalObject(
        1265,
        FacilityTurret.Constructor(Vector3(4631.01f, 3345.498f, 61.59576f), manned_turret),
        owning_building_guid = 28
      )
      TurretToWeapon(1265, 5040)
      LocalObject(
        674,
        ImplantTerminalMech.Constructor(Vector3(4583.874f, 3373.957f, 42.67076f)),
        owning_building_guid = 28
      )
      LocalObject(
        668,
        Terminal.Constructor(Vector3(4583.881f, 3373.973f, 42.67076f), implant_terminal_interface),
        owning_building_guid = 28
      )
      TerminalToInterface(674, 668)
      LocalObject(
        675,
        ImplantTerminalMech.Constructor(Vector3(4590.353f, 3387.879f, 42.67076f)),
        owning_building_guid = 28
      )
      LocalObject(
        669,
        Terminal.Constructor(Vector3(4590.345f, 3387.863f, 42.67076f), implant_terminal_interface),
        owning_building_guid = 28
      )
      TerminalToInterface(675, 669)
      LocalObject(
        1621,
        Painbox.Constructor(Vector3(4552.758f, 3317.812f, 67.22256f), painbox),
        owning_building_guid = 28
      )
      LocalObject(
        1631,
        Painbox.Constructor(Vector3(4572.629f, 3338.754f, 47.26366f), painbox_continuous),
        owning_building_guid = 28
      )
      LocalObject(
        1640,
        Painbox.Constructor(Vector3(4557.592f, 3331.518f, 67.42767f), painbox_door_radius),
        owning_building_guid = 28
      )
      LocalObject(
        1663,
        Painbox.Constructor(Vector3(4564.62f, 3331.546f, 45.54966f), painbox_door_radius_continuous),
        owning_building_guid = 28
      )
      LocalObject(
        1664,
        Painbox.Constructor(Vector3(4578.26f, 3356.122f, 44.90796f), painbox_door_radius_continuous),
        owning_building_guid = 28
      )
      LocalObject(
        1665,
        Painbox.Constructor(Vector3(4589.32f, 3343.482f, 46.73466f), painbox_door_radius_continuous),
        owning_building_guid = 28
      )
      LocalObject(219, Generator.Constructor(Vector3(4549.693f, 3314.951f, 61.89977f)), owning_building_guid = 28)
      LocalObject(
        210,
        Terminal.Constructor(Vector3(4553.113f, 3322.395f, 63.19376f), gen_control),
        owning_building_guid = 28
      )
    }

    Building19998()

    def Building19998(): Unit = { // Name: GW_Solsar_S Type: hst GUID: 32, MapID: 19998
      LocalBuilding(
        "GW_Solsar_S",
        32,
        19998,
        FoundationBuilder(WarpGate.Structure(Vector3(3735.49f, 2916.53f, 85.87f), hst))
      )
    }

    Building19999()

    def Building19999(): Unit = { // Name: GW_Solsar_N Type: hst GUID: 33, MapID: 19999
      LocalBuilding(
        "GW_Solsar_N",
        33,
        19999,
        FoundationBuilder(WarpGate.Structure(Vector3(5712.8f, 4800.53f, 63.29f), hst))
      )
    }

    Building11()

    def Building11(): Unit = { // Name: Amun Type: tech_plant GUID: 35, MapID: 11
      LocalBuilding(
        "Amun",
        35,
        11,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(4428f, 2220f, 68.3462f), Vector3(0f, 0f, 179f), tech_plant)
        )
      )
      LocalObject(
        157,
        CaptureTerminal.Constructor(Vector3(4424.036f, 2264.165f, 83.4462f), capture_terminal),
        owning_building_guid = 35
      )
      LocalObject(350, Door.Constructor(Vector3(4348.938f, 2218.272f, 69.8882f)), owning_building_guid = 35)
      LocalObject(351, Door.Constructor(Vector3(4349.255f, 2236.463f, 77.8512f)), owning_building_guid = 35)
      LocalObject(353, Door.Constructor(Vector3(4384.709f, 2189.485f, 77.8512f)), owning_building_guid = 35)
      LocalObject(354, Door.Constructor(Vector3(4402.899f, 2189.167f, 69.8882f)), owning_building_guid = 35)
      LocalObject(355, Door.Constructor(Vector3(4411.247f, 2259.724f, 84.9672f)), owning_building_guid = 35)
      LocalObject(356, Door.Constructor(Vector3(4417.813f, 2243.606f, 84.9672f)), owning_building_guid = 35)
      LocalObject(357, Door.Constructor(Vector3(4424.987f, 2311.264f, 69.9972f)), owning_building_guid = 35)
      LocalObject(358, Door.Constructor(Vector3(4443.178f, 2310.947f, 77.9602f)), owning_building_guid = 35)
      LocalObject(360, Door.Constructor(Vector3(4469.739f, 2319.287f, 69.9672f)), owning_building_guid = 35)
      LocalObject(372, Door.Constructor(Vector3(4499.067f, 2196.891f, 77.8512f)), owning_building_guid = 35)
      LocalObject(373, Door.Constructor(Vector3(4499.385f, 2215.08f, 69.8882f)), owning_building_guid = 35)
      LocalObject(374, Door.Constructor(Vector3(4500.354f, 2270.624f, 77.8512f)), owning_building_guid = 35)
      LocalObject(375, Door.Constructor(Vector3(4500.672f, 2288.813f, 69.8882f)), owning_building_guid = 35)
      LocalObject(449, Door.Constructor(Vector3(4475.294f, 2179.166f, 72.0832f)), owning_building_guid = 35)
      LocalObject(451, Door.Constructor(Vector3(4476.272f, 2235.16f, 52.0832f)), owning_building_guid = 35)
      LocalObject(566, Door.Constructor(Vector3(4396.494f, 2248.554f, 62.4672f)), owning_building_guid = 35)
      LocalObject(567, Door.Constructor(Vector3(4396.773f, 2264.552f, 54.9672f)), owning_building_guid = 35)
      LocalObject(568, Door.Constructor(Vector3(4400.284f, 2236.486f, 54.9672f)), owning_building_guid = 35)
      LocalObject(569, Door.Constructor(Vector3(4408.422f, 2244.345f, 62.4672f)), owning_building_guid = 35)
      LocalObject(570, Door.Constructor(Vector3(4408.841f, 2268.342f, 54.9672f)), owning_building_guid = 35)
      LocalObject(571, Door.Constructor(Vector3(4408.841f, 2268.342f, 62.4672f)), owning_building_guid = 35)
      LocalObject(572, Door.Constructor(Vector3(4428.489f, 2247.996f, 84.9672f)), owning_building_guid = 35)
      LocalObject(573, Door.Constructor(Vector3(4428.559f, 2251.995f, 74.9672f)), owning_building_guid = 35)
      LocalObject(574, Door.Constructor(Vector3(4428.628f, 2255.995f, 54.9672f)), owning_building_guid = 35)
      LocalObject(575, Door.Constructor(Vector3(4429.326f, 2295.989f, 62.4672f)), owning_building_guid = 35)
      LocalObject(576, Door.Constructor(Vector3(4432.418f, 2243.927f, 64.9672f)), owning_building_guid = 35)
      LocalObject(577, Door.Constructor(Vector3(4432.697f, 2259.924f, 64.9672f)), owning_building_guid = 35)
      LocalObject(578, Door.Constructor(Vector3(4432.697f, 2259.924f, 84.9672f)), owning_building_guid = 35)
      LocalObject(579, Door.Constructor(Vector3(4444.905f, 2271.713f, 62.4672f)), owning_building_guid = 35)
      LocalObject(580, Door.Constructor(Vector3(4445.603f, 2311.707f, 62.4672f)), owning_building_guid = 35)
      LocalObject(581, Door.Constructor(Vector3(4460.344f, 2239.438f, 59.9672f)), owning_building_guid = 35)
      LocalObject(582, Door.Constructor(Vector3(4460.763f, 2263.435f, 59.9672f)), owning_building_guid = 35)
      LocalObject(585, Door.Constructor(Vector3(4492.758f, 2262.876f, 59.9672f)), owning_building_guid = 35)
      LocalObject(586, Door.Constructor(Vector3(4493.177f, 2286.873f, 62.4672f)), owning_building_guid = 35)
      LocalObject(654, Door.Constructor(Vector3(4388.59f, 2266.354f, 70.7262f)), owning_building_guid = 35)
      LocalObject(1998, Door.Constructor(Vector3(4415.812f, 2247.902f, 62.8002f)), owning_building_guid = 35)
      LocalObject(1999, Door.Constructor(Vector3(4415.939f, 2255.19f, 62.8002f)), owning_building_guid = 35)
      LocalObject(2000, Door.Constructor(Vector3(4416.066f, 2262.482f, 62.8002f)), owning_building_guid = 35)
      LocalObject(
        690,
        IFFLock.Constructor(Vector3(4385.494f, 2269.146f, 69.9262f), Vector3(0, 0, 1)),
        owning_building_guid = 35,
        door_guid = 654
      )
      LocalObject(
        694,
        IFFLock.Constructor(Vector3(4469.999f, 2176.907f, 70.0342f), Vector3(0, 0, 181)),
        owning_building_guid = 35,
        door_guid = 449
      )
      LocalObject(
        800,
        IFFLock.Constructor(Vector3(4397.74f, 2266.107f, 54.7822f), Vector3(0, 0, 91)),
        owning_building_guid = 35,
        door_guid = 567
      )
      LocalObject(
        801,
        IFFLock.Constructor(Vector3(4398.728f, 2237.457f, 54.7822f), Vector3(0, 0, 1)),
        owning_building_guid = 35,
        door_guid = 568
      )
      LocalObject(
        802,
        IFFLock.Constructor(Vector3(4407.283f, 2269.179f, 62.2822f), Vector3(0, 0, 1)),
        owning_building_guid = 35,
        door_guid = 571
      )
      LocalObject(
        803,
        IFFLock.Constructor(Vector3(4409.209f, 2260.559f, 84.8922f), Vector3(0, 0, 1)),
        owning_building_guid = 35,
        door_guid = 355
      )
      LocalObject(
        804,
        IFFLock.Constructor(Vector3(4409.979f, 2243.508f, 62.2822f), Vector3(0, 0, 181)),
        owning_building_guid = 35,
        door_guid = 569
      )
      LocalObject(
        805,
        IFFLock.Constructor(Vector3(4419.842f, 2242.763f, 84.8922f), Vector3(0, 0, 181)),
        owning_building_guid = 35,
        door_guid = 356
      )
      LocalObject(
        806,
        IFFLock.Constructor(Vector3(4434.253f, 2258.957f, 84.7822f), Vector3(0, 0, 181)),
        owning_building_guid = 35,
        door_guid = 578
      )
      LocalObject(
        808,
        IFFLock.Constructor(Vector3(4470.589f, 2321.318f, 69.8982f), Vector3(0, 0, 91)),
        owning_building_guid = 35,
        door_guid = 360
      )
      LocalObject(1047, Locker.Constructor(Vector3(4381.73f, 2246.646f, 53.4462f)), owning_building_guid = 35)
      LocalObject(1048, Locker.Constructor(Vector3(4383.067f, 2246.623f, 53.4462f)), owning_building_guid = 35)
      LocalObject(1049, Locker.Constructor(Vector3(4384.403f, 2246.6f, 53.4462f)), owning_building_guid = 35)
      LocalObject(1050, Locker.Constructor(Vector3(4385.727f, 2246.577f, 53.4462f)), owning_building_guid = 35)
      LocalObject(1051, Locker.Constructor(Vector3(4390.266f, 2246.498f, 53.4462f)), owning_building_guid = 35)
      LocalObject(1052, Locker.Constructor(Vector3(4391.603f, 2246.474f, 53.4462f)), owning_building_guid = 35)
      LocalObject(1053, Locker.Constructor(Vector3(4392.938f, 2246.451f, 53.4462f)), owning_building_guid = 35)
      LocalObject(1054, Locker.Constructor(Vector3(4394.262f, 2246.428f, 53.4462f)), owning_building_guid = 35)
      LocalObject(1055, Locker.Constructor(Vector3(4401.781f, 2266.324f, 61.2072f)), owning_building_guid = 35)
      LocalObject(1056, Locker.Constructor(Vector3(4402.93f, 2266.303f, 61.2072f)), owning_building_guid = 35)
      LocalObject(1057, Locker.Constructor(Vector3(4404.077f, 2266.283f, 61.2072f)), owning_building_guid = 35)
      LocalObject(1058, Locker.Constructor(Vector3(4405.241f, 2266.263f, 61.2072f)), owning_building_guid = 35)
      LocalObject(
        117,
        Terminal.Constructor(Vector3(4411.832f, 2245.145f, 84.0492f), air_vehicle_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1289,
        VehicleSpawnPad.Constructor(Vector3(4406.988f, 2224.532f, 80.9242f), mb_pad_creation, Vector3(0, 0, 181)),
        owning_building_guid = 35,
        terminal_guid = 117
      )
      LocalObject(
        118,
        Terminal.Constructor(Vector3(4423.762f, 2244.937f, 84.0492f), air_vehicle_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1290,
        VehicleSpawnPad.Constructor(Vector3(4428.003f, 2224.166f, 80.9242f), mb_pad_creation, Vector3(0, 0, 181)),
        owning_building_guid = 35,
        terminal_guid = 118
      )
      LocalObject(
        1377,
        Terminal.Constructor(Vector3(4401.927f, 2253.532f, 62.5362f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1378,
        Terminal.Constructor(Vector3(4401.993f, 2257.321f, 62.5362f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1379,
        Terminal.Constructor(Vector3(4402.059f, 2261.051f, 62.5362f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1380,
        Terminal.Constructor(Vector3(4425.405f, 2246.563f, 74.7762f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1932,
        Terminal.Constructor(Vector3(4391.119f, 2241.008f, 75.0282f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1933,
        Terminal.Constructor(Vector3(4413.414f, 2217.039f, 80.3692f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1934,
        Terminal.Constructor(Vector3(4415.559f, 2250.399f, 63.0802f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1935,
        Terminal.Constructor(Vector3(4415.689f, 2257.686f, 63.0802f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1936,
        Terminal.Constructor(Vector3(4415.812f, 2264.977f, 63.0802f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1937,
        Terminal.Constructor(Vector3(4420.887f, 2267.54f, 55.0032f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1938,
        Terminal.Constructor(Vector3(4453.719f, 2314.975f, 62.5032f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2085,
        Terminal.Constructor(Vector3(4476.705f, 2259.733f, 54.1602f), ground_vehicle_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1291,
        VehicleSpawnPad.Constructor(Vector3(4476.565f, 2248.818f, 45.8832f), mb_pad_creation, Vector3(0, 0, 181)),
        owning_building_guid = 35,
        terminal_guid = 2085
      )
      LocalObject(1806, ResourceSilo.Constructor(Vector3(4365.689f, 2188.528f, 75.3542f)), owning_building_guid = 35)
      LocalObject(
        1849,
        SpawnTube.Constructor(Vector3(4416.271f, 2248.947f, 60.9462f), Vector3(0, 0, 181)),
        owning_building_guid = 35
      )
      LocalObject(
        1850,
        SpawnTube.Constructor(Vector3(4416.397f, 2256.234f, 60.9462f), Vector3(0, 0, 181)),
        owning_building_guid = 35
      )
      LocalObject(
        1851,
        SpawnTube.Constructor(Vector3(4416.525f, 2263.524f, 60.9462f), Vector3(0, 0, 181)),
        owning_building_guid = 35
      )
      LocalObject(
        1305,
        ProximityTerminal.Constructor(Vector3(4388.022f, 2247.082f, 53.4462f), medical_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1306,
        ProximityTerminal.Constructor(Vector3(4425.589f, 2257.147f, 73.4432f), medical_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1519,
        ProximityTerminal.Constructor(Vector3(4360.183f, 2247.086f, 76.5542f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1520,
        Terminal.Constructor(Vector3(4360.183f, 2247.086f, 76.5542f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1522,
        ProximityTerminal.Constructor(Vector3(4366.758f, 2263.22f, 78.9982f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1523,
        Terminal.Constructor(Vector3(4366.758f, 2263.22f, 78.9982f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1525,
        ProximityTerminal.Constructor(Vector3(4403.89f, 2301.805f, 76.5542f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1526,
        Terminal.Constructor(Vector3(4403.89f, 2301.805f, 76.5542f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1528,
        ProximityTerminal.Constructor(Vector3(4419.749f, 2284.68f, 83.7932f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1529,
        Terminal.Constructor(Vector3(4419.749f, 2284.68f, 83.7932f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1531,
        ProximityTerminal.Constructor(Vector3(4469.156f, 2227.45f, 78.8962f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1532,
        Terminal.Constructor(Vector3(4469.156f, 2227.45f, 78.8962f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1537,
        ProximityTerminal.Constructor(Vector3(4489.711f, 2243.266f, 76.5412f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1538,
        Terminal.Constructor(Vector3(4489.711f, 2243.266f, 76.5412f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1769,
        ProximityTerminal.Constructor(Vector3(4370.992f, 2313.802f, 68.0962f), repair_silo),
        owning_building_guid = 35
      )
      LocalObject(
        1770,
        Terminal.Constructor(Vector3(4370.992f, 2313.802f, 68.0962f), ground_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1773,
        ProximityTerminal.Constructor(Vector3(4426.965f, 2178.375f, 68.0747f), repair_silo),
        owning_building_guid = 35
      )
      LocalObject(
        1774,
        Terminal.Constructor(Vector3(4426.965f, 2178.375f, 68.0747f), ground_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1250,
        FacilityTurret.Constructor(Vector3(4334.737f, 2256.21f, 76.8452f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1250, 5041)
      LocalObject(
        1251,
        FacilityTurret.Constructor(Vector3(4341.189f, 2183.111f, 76.8452f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1251, 5042)
      LocalObject(
        1252,
        FacilityTurret.Constructor(Vector3(4343.593f, 2320.832f, 76.8452f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1252, 5043)
      LocalObject(
        1254,
        FacilityTurret.Constructor(Vector3(4420.675f, 2121.258f, 76.8452f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1254, 5044)
      LocalObject(
        1258,
        FacilityTurret.Constructor(Vector3(4508.309f, 2317.948f, 76.8452f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1258, 5045)
      LocalObject(
        1259,
        FacilityTurret.Constructor(Vector3(4510.356f, 2119.692f, 76.8452f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1259, 5046)
      LocalObject(
        1620,
        Painbox.Constructor(Vector3(4402.333f, 2224.242f, 56.9195f), painbox),
        owning_building_guid = 35
      )
      LocalObject(
        1629,
        Painbox.Constructor(Vector3(4407.813f, 2257.146f, 65.2161f), painbox_continuous),
        owning_building_guid = 35
      )
      LocalObject(
        1638,
        Painbox.Constructor(Vector3(4400.628f, 2239.009f, 56.6056f), painbox_door_radius),
        owning_building_guid = 35
      )
      LocalObject(
        1657,
        Painbox.Constructor(Vector3(4392.878f, 2250.048f, 64.5285f), painbox_door_radius_continuous),
        owning_building_guid = 35
      )
      LocalObject(
        1658,
        Painbox.Constructor(Vector3(4408.53f, 2242.574f, 63.6224f), painbox_door_radius_continuous),
        owning_building_guid = 35
      )
      LocalObject(
        1659,
        Painbox.Constructor(Vector3(4409.836f, 2270.046f, 63.0741f), painbox_door_radius_continuous),
        owning_building_guid = 35
      )
      LocalObject(218, Generator.Constructor(Vector3(4400.037f, 2220.933f, 52.1522f)), owning_building_guid = 35)
      LocalObject(
        209,
        Terminal.Constructor(Vector3(4400.133f, 2229.125f, 53.4462f), gen_control),
        owning_building_guid = 35
      )
    }

    Building5()

    def Building5(): Unit = { // Name: Seth Type: tech_plant GUID: 38, MapID: 5
      LocalBuilding(
        "Seth",
        38,
        5,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(4566f, 6116f, 57.799f), Vector3(0f, 0f, 265f), tech_plant)
        )
      )
      LocalObject(
        158,
        CaptureTerminal.Constructor(Vector3(4521.666f, 6115.126f, 72.899f), capture_terminal),
        owning_building_guid = 38
      )
      LocalObject(361, Door.Constructor(Vector3(4469.867f, 6164.563f, 59.42f)), owning_building_guid = 38)
      LocalObject(363, Door.Constructor(Vector3(4474.748f, 6119.361f, 59.45f)), owning_building_guid = 38)
      LocalObject(364, Door.Constructor(Vector3(4476.333f, 6137.485f, 67.413f)), owning_building_guid = 38)
      LocalObject(376, Door.Constructor(Vector3(4502.424f, 6193.295f, 59.341f)), owning_building_guid = 38)
      LocalObject(391, Door.Constructor(Vector3(4520.546f, 6191.709f, 67.304f)), owning_building_guid = 38)
      LocalObject(392, Door.Constructor(Vector3(4525.204f, 6102.059f, 74.42001f)), owning_building_guid = 38)
      LocalObject(394, Door.Constructor(Vector3(4541.741f, 6107.485f, 74.42001f)), owning_building_guid = 38)
      LocalObject(396, Door.Constructor(Vector3(4544.084f, 6038.596f, 67.304f)), owning_building_guid = 38)
      LocalObject(401, Door.Constructor(Vector3(4562.208f, 6037.01f, 59.341f)), owning_building_guid = 38)
      LocalObject(403, Door.Constructor(Vector3(4575.887f, 6186.868f, 59.341f)), owning_building_guid = 38)
      LocalObject(406, Door.Constructor(Vector3(4593.421f, 6070.686f, 67.304f)), owning_building_guid = 38)
      LocalObject(407, Door.Constructor(Vector3(4594.01f, 6185.282f, 67.304f)), owning_building_guid = 38)
      LocalObject(408, Door.Constructor(Vector3(4595.006f, 6088.81f, 59.341f)), owning_building_guid = 38)
      LocalObject(450, Door.Constructor(Vector3(4610.033f, 6160.331f, 61.536f)), owning_building_guid = 38)
      LocalObject(452, Door.Constructor(Vector3(4554.244f, 6165.212f, 41.536f)), owning_building_guid = 38)
      LocalObject(583, Door.Constructor(Vector3(4475.745f, 6139.958f, 51.92f)), owning_building_guid = 38)
      LocalObject(584, Door.Constructor(Vector3(4490.289f, 6122.624f, 51.92f)), owning_building_guid = 38)
      LocalObject(588, Door.Constructor(Vector3(4503.837f, 6185.683f, 51.92f)), owning_building_guid = 38)
      LocalObject(592, Door.Constructor(Vector3(4515.592f, 6136.471f, 51.92f)), owning_building_guid = 38)
      LocalObject(593, Door.Constructor(Vector3(4516.439f, 6100.26f, 44.42001f)), owning_building_guid = 38)
      LocalObject(594, Door.Constructor(Vector3(4516.439f, 6100.26f, 51.92f)), owning_building_guid = 38)
      LocalObject(595, Door.Constructor(Vector3(4519.378f, 6087.957f, 44.42001f)), owning_building_guid = 38)
      LocalObject(598, Door.Constructor(Vector3(4524.957f, 6151.713f, 49.42001f)), owning_building_guid = 38)
      LocalObject(599, Door.Constructor(Vector3(4526.501f, 6123.471f, 54.42f)), owning_building_guid = 38)
      LocalObject(600, Door.Constructor(Vector3(4526.501f, 6123.471f, 74.42001f)), owning_building_guid = 38)
      LocalObject(601, Door.Constructor(Vector3(4527.746f, 6183.591f, 49.42001f)), owning_building_guid = 38)
      LocalObject(602, Door.Constructor(Vector3(4530.137f, 6119.138f, 44.42001f)), owning_building_guid = 38)
      LocalObject(603, Door.Constructor(Vector3(4534.122f, 6118.789f, 64.42001f)), owning_building_guid = 38)
      LocalObject(604, Door.Constructor(Vector3(4535.317f, 6086.562f, 51.92f)), owning_building_guid = 38)
      LocalObject(606, Door.Constructor(Vector3(4538.106f, 6118.44f, 74.42001f)), owning_building_guid = 38)
      LocalObject(608, Door.Constructor(Vector3(4540.348f, 6098.168f, 51.92f)), owning_building_guid = 38)
      LocalObject(609, Door.Constructor(Vector3(4542.44f, 6122.077f, 54.42f)), owning_building_guid = 38)
      LocalObject(612, Door.Constructor(Vector3(4547.621f, 6089.501f, 44.42001f)), owning_building_guid = 38)
      LocalObject(613, Door.Constructor(Vector3(4548.865f, 6149.622f, 49.42001f)), owning_building_guid = 38)
      LocalObject(655, Door.Constructor(Vector3(4517.01f, 6079.919f, 60.179f)), owning_building_guid = 38)
      LocalObject(2007, Door.Constructor(Vector3(4522.79f, 6107.059f, 52.253f)), owning_building_guid = 38)
      LocalObject(2008, Door.Constructor(Vector3(4530.055f, 6106.423f, 52.253f)), owning_building_guid = 38)
      LocalObject(2009, Door.Constructor(Vector3(4537.316f, 6105.788f, 52.253f)), owning_building_guid = 38)
      LocalObject(
        691,
        IFFLock.Constructor(Vector3(4514.008f, 6077.026f, 59.37901f), Vector3(0, 0, 275)),
        owning_building_guid = 38,
        door_guid = 655
      )
      LocalObject(
        695,
        IFFLock.Constructor(Vector3(4611.917f, 6154.89f, 59.487f), Vector3(0, 0, 95)),
        owning_building_guid = 38,
        door_guid = 450
      )
      LocalObject(
        807,
        IFFLock.Constructor(Vector3(4467.899f, 6165.553f, 59.35101f), Vector3(0, 0, 5)),
        owning_building_guid = 38,
        door_guid = 361
      )
      LocalObject(
        824,
        IFFLock.Constructor(Vector3(4515.496f, 6098.764f, 51.735f), Vector3(0, 0, 275)),
        owning_building_guid = 38,
        door_guid = 594
      )
      LocalObject(
        828,
        IFFLock.Constructor(Vector3(4517.895f, 6089.03f, 44.235f), Vector3(0, 0, 5)),
        owning_building_guid = 38,
        door_guid = 595
      )
      LocalObject(
        829,
        IFFLock.Constructor(Vector3(4524.229f, 6100.084f, 74.345f), Vector3(0, 0, 275)),
        owning_building_guid = 38,
        door_guid = 392
      )
      LocalObject(
        830,
        IFFLock.Constructor(Vector3(4527.574f, 6124.955f, 74.235f), Vector3(0, 0, 95)),
        owning_building_guid = 38,
        door_guid = 600
      )
      LocalObject(
        831,
        IFFLock.Constructor(Vector3(4541.292f, 6099.663f, 51.735f), Vector3(0, 0, 95)),
        owning_building_guid = 38,
        door_guid = 608
      )
      LocalObject(
        832,
        IFFLock.Constructor(Vector3(4542.724f, 6109.45f, 74.345f), Vector3(0, 0, 95)),
        owning_building_guid = 38,
        door_guid = 394
      )
      LocalObject(
        833,
        IFFLock.Constructor(Vector3(4546.544f, 6088.017f, 44.235f), Vector3(0, 0, 275)),
        owning_building_guid = 38,
        door_guid = 612
      )
      LocalObject(1078, Locker.Constructor(Vector3(4517.96f, 6093.077f, 50.66f)), owning_building_guid = 38)
      LocalObject(1079, Locker.Constructor(Vector3(4518.061f, 6094.221f, 50.66f)), owning_building_guid = 38)
      LocalObject(1080, Locker.Constructor(Vector3(4518.161f, 6095.364f, 50.66f)), owning_building_guid = 38)
      LocalObject(1081, Locker.Constructor(Vector3(4518.262f, 6096.523f, 50.66f)), owning_building_guid = 38)
      LocalObject(1087, Locker.Constructor(Vector3(4536.191f, 6071.702f, 42.899f)), owning_building_guid = 38)
      LocalObject(1088, Locker.Constructor(Vector3(4536.307f, 6073.033f, 42.899f)), owning_building_guid = 38)
      LocalObject(1089, Locker.Constructor(Vector3(4536.424f, 6074.364f, 42.899f)), owning_building_guid = 38)
      LocalObject(1090, Locker.Constructor(Vector3(4536.539f, 6075.683f, 42.899f)), owning_building_guid = 38)
      LocalObject(1091, Locker.Constructor(Vector3(4536.935f, 6080.206f, 42.899f)), owning_building_guid = 38)
      LocalObject(1092, Locker.Constructor(Vector3(4537.051f, 6081.538f, 42.899f)), owning_building_guid = 38)
      LocalObject(1093, Locker.Constructor(Vector3(4537.167f, 6082.869f, 42.899f)), owning_building_guid = 38)
      LocalObject(1094, Locker.Constructor(Vector3(4537.283f, 6084.188f, 42.899f)), owning_building_guid = 38)
      LocalObject(
        119,
        Terminal.Constructor(Vector3(4539.789f, 6101.625f, 73.50201f), air_vehicle_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1294,
        VehicleSpawnPad.Constructor(Vector3(4560.013f, 6095.355f, 70.37701f), mb_pad_creation, Vector3(0, 0, 95)),
        owning_building_guid = 38,
        terminal_guid = 119
      )
      LocalObject(
        120,
        Terminal.Constructor(Vector3(4540.828f, 6113.511f, 73.50201f), air_vehicle_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1295,
        VehicleSpawnPad.Constructor(Vector3(4561.845f, 6116.293f, 70.37701f), mb_pad_creation, Vector3(0, 0, 95)),
        owning_building_guid = 38,
        terminal_guid = 120
      )
      LocalObject(
        1390,
        Terminal.Constructor(Vector3(4523.239f, 6092.985f, 51.989f), order_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1391,
        Terminal.Constructor(Vector3(4526.956f, 6092.66f, 51.989f), order_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1392,
        Terminal.Constructor(Vector3(4530.731f, 6092.33f, 51.989f), order_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1393,
        Terminal.Constructor(Vector3(4539.32f, 6115.265f, 64.229f), order_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1939,
        Terminal.Constructor(Vector3(4473.051f, 6148.282f, 51.95601f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1941,
        Terminal.Constructor(Vector3(4518.079f, 6112.22f, 44.456f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1943,
        Terminal.Constructor(Vector3(4520.283f, 6106.979f, 52.533f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1944,
        Terminal.Constructor(Vector3(4527.547f, 6106.348f, 52.533f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1945,
        Terminal.Constructor(Vector3(4534.808f, 6105.709f, 52.533f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1946,
        Terminal.Constructor(Vector3(4542.471f, 6080.674f, 64.481f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1950,
        Terminal.Constructor(Vector3(4567.936f, 6101.243f, 69.82201f), spawn_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        2086,
        Terminal.Constructor(Vector3(4529.761f, 6167.358f, 43.613f), ground_vehicle_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1292,
        VehicleSpawnPad.Constructor(Vector3(4540.64f, 6166.457f, 35.33601f), mb_pad_creation, Vector3(0, 0, 95)),
        owning_building_guid = 38,
        terminal_guid = 2086
      )
      LocalObject(1808, ResourceSilo.Constructor(Vector3(4593.049f, 6051.646f, 64.80701f)), owning_building_guid = 38)
      LocalObject(
        1858,
        SpawnTube.Constructor(Vector3(4521.782f, 6107.589f, 50.399f), Vector3(0, 0, 95)),
        owning_building_guid = 38
      )
      LocalObject(
        1859,
        SpawnTube.Constructor(Vector3(4529.045f, 6106.954f, 50.399f), Vector3(0, 0, 95)),
        owning_building_guid = 38
      )
      LocalObject(
        1860,
        SpawnTube.Constructor(Vector3(4536.305f, 6106.318f, 50.399f), Vector3(0, 0, 95)),
        owning_building_guid = 38
      )
      LocalObject(
        1307,
        ProximityTerminal.Constructor(Vector3(4528.775f, 6116.186f, 62.896f), medical_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1308,
        ProximityTerminal.Constructor(Vector3(4536.195f, 6078.009f, 42.899f), medical_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1534,
        ProximityTerminal.Constructor(Vector3(4482.712f, 6097.655f, 66.007f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        1535,
        Terminal.Constructor(Vector3(4482.712f, 6097.655f, 66.007f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1540,
        ProximityTerminal.Constructor(Vector3(4500.902f, 6112.28f, 73.246f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        1541,
        Terminal.Constructor(Vector3(4500.902f, 6112.28f, 73.246f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1546,
        ProximityTerminal.Constructor(Vector3(4518.613f, 6057.922f, 68.451f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        1547,
        Terminal.Constructor(Vector3(4518.613f, 6057.922f, 68.451f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1552,
        ProximityTerminal.Constructor(Vector3(4534.25f, 6050.237f, 66.007f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        1553,
        Terminal.Constructor(Vector3(4534.25f, 6050.237f, 66.007f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1555,
        ProximityTerminal.Constructor(Vector3(4547.096f, 6179.184f, 65.994f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        1556,
        Terminal.Constructor(Vector3(4547.096f, 6179.184f, 65.994f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1558,
        ProximityTerminal.Constructor(Vector3(4561.439f, 6157.576f, 68.34901f), pad_landing_frame),
        owning_building_guid = 38
      )
      LocalObject(
        1559,
        Terminal.Constructor(Vector3(4561.439f, 6157.576f, 68.34901f), air_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1777,
        ProximityTerminal.Constructor(Vector3(4468.45f, 6065.674f, 57.549f), repair_silo),
        owning_building_guid = 38
      )
      LocalObject(
        1778,
        Terminal.Constructor(Vector3(4468.45f, 6065.674f, 57.549f), ground_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1785,
        ProximityTerminal.Constructor(Vector3(4607.451f, 6112.063f, 57.5275f), repair_silo),
        owning_building_guid = 38
      )
      LocalObject(
        1786,
        Terminal.Constructor(Vector3(4607.451f, 6112.063f, 57.5275f), ground_rearm_terminal),
        owning_building_guid = 38
      )
      LocalObject(
        1256,
        FacilityTurret.Constructor(Vector3(4459.526f, 6038.832f, 66.298f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1256, 5047)
      LocalObject(
        1257,
        FacilityTurret.Constructor(Vector3(4473.892f, 6202.946f, 66.298f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1257, 5048)
      LocalObject(
        1260,
        FacilityTurret.Constructor(Vector3(4523.372f, 6025.49f, 66.298f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1260, 5049)
      LocalObject(
        1263,
        FacilityTurret.Constructor(Vector3(4596.743f, 6026.827f, 66.298f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1263, 5050)
      LocalObject(
        1266,
        FacilityTurret.Constructor(Vector3(4663.991f, 6101.805f, 66.298f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1266, 5051)
      LocalObject(
        1267,
        FacilityTurret.Constructor(Vector3(4671.808f, 6191.158f, 66.298f), manned_turret),
        owning_building_guid = 38
      )
      TurretToWeapon(1267, 5052)
      LocalObject(
        1622,
        Painbox.Constructor(Vector3(4559.978f, 6090.691f, 46.3723f), painbox),
        owning_building_guid = 38
      )
      LocalObject(
        1630,
        Painbox.Constructor(Vector3(4527.537f, 6098.453f, 54.6689f), painbox_continuous),
        owning_building_guid = 38
      )
      LocalObject(
        1639,
        Painbox.Constructor(Vector3(4545.127f, 6090.021f, 46.0584f), painbox_door_radius),
        owning_building_guid = 38
      )
      LocalObject(
        1660,
        Painbox.Constructor(Vector3(4514.809f, 6101.371f, 52.52691f), painbox_door_radius_continuous),
        owning_building_guid = 38
      )
      LocalObject(
        1661,
        Painbox.Constructor(Vector3(4533.576f, 6083.06f, 53.9813f), painbox_door_radius_continuous),
        owning_building_guid = 38
      )
      LocalObject(
        1662,
        Painbox.Constructor(Vector3(4542.123f, 6098.152f, 53.0752f), painbox_door_radius_continuous),
        owning_building_guid = 38
      )
      LocalObject(220, Generator.Constructor(Vector3(4563.119f, 6088.17f, 41.605f)), owning_building_guid = 38)
      LocalObject(
        211,
        Terminal.Constructor(Vector3(4554.954f, 6088.837f, 42.899f), gen_control),
        owning_building_guid = 38
      )
    }

    Building20()

    def Building20(): Unit = { // Name: W_Sobek_Tower Type: tower_a GUID: 41, MapID: 20
      LocalBuilding(
        "W_Sobek_Tower",
        41,
        20,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2600f, 3180f, 73.48247f), Vector3(0f, 0f, 256f), tower_a)
        )
      )
      LocalObject(
        1877,
        CaptureTerminal.Constructor(Vector3(2595.887f, 3163.931f, 83.48147f), secondary_capture),
        owning_building_guid = 41
      )
      LocalObject(222, Door.Constructor(Vector3(2589.334f, 3170.292f, 75.00347f)), owning_building_guid = 41)
      LocalObject(223, Door.Constructor(Vector3(2589.334f, 3170.292f, 95.00247f)), owning_building_guid = 41)
      LocalObject(224, Door.Constructor(Vector3(2604.859f, 3166.421f, 75.00347f)), owning_building_guid = 41)
      LocalObject(225, Door.Constructor(Vector3(2604.859f, 3166.421f, 95.00247f)), owning_building_guid = 41)
      LocalObject(1959, Door.Constructor(Vector3(2586.43f, 3171.896f, 64.81847f)), owning_building_guid = 41)
      LocalObject(1960, Door.Constructor(Vector3(2602.353f, 3167.926f, 64.81847f)), owning_building_guid = 41)
      LocalObject(
        696,
        IFFLock.Constructor(Vector3(2588.052f, 3168.502f, 74.94347f), Vector3(0, 0, 284)),
        owning_building_guid = 41,
        door_guid = 222
      )
      LocalObject(
        697,
        IFFLock.Constructor(Vector3(2588.052f, 3168.502f, 94.94347f), Vector3(0, 0, 284)),
        owning_building_guid = 41,
        door_guid = 223
      )
      LocalObject(
        698,
        IFFLock.Constructor(Vector3(2606.14f, 3168.207f, 74.94347f), Vector3(0, 0, 104)),
        owning_building_guid = 41,
        door_guid = 224
      )
      LocalObject(
        699,
        IFFLock.Constructor(Vector3(2606.14f, 3168.207f, 94.94347f), Vector3(0, 0, 104)),
        owning_building_guid = 41,
        door_guid = 225
      )
      LocalObject(873, Locker.Constructor(Vector3(2580.295f, 3163.123f, 63.47647f)), owning_building_guid = 41)
      LocalObject(874, Locker.Constructor(Vector3(2580.634f, 3164.483f, 63.47647f)), owning_building_guid = 41)
      LocalObject(875, Locker.Constructor(Vector3(2581.284f, 3167.091f, 63.47647f)), owning_building_guid = 41)
      LocalObject(876, Locker.Constructor(Vector3(2581.608f, 3168.389f, 63.47647f)), owning_building_guid = 41)
      LocalObject(877, Locker.Constructor(Vector3(2601.517f, 3157.832f, 63.47647f)), owning_building_guid = 41)
      LocalObject(878, Locker.Constructor(Vector3(2601.856f, 3159.192f, 63.47647f)), owning_building_guid = 41)
      LocalObject(879, Locker.Constructor(Vector3(2602.498f, 3161.766f, 63.47647f)), owning_building_guid = 41)
      LocalObject(880, Locker.Constructor(Vector3(2602.822f, 3163.063f, 63.47647f)), owning_building_guid = 41)
      LocalObject(
        1312,
        Terminal.Constructor(Vector3(2585.234f, 3161.58f, 64.81447f), order_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        1313,
        Terminal.Constructor(Vector3(2590.788f, 3160.195f, 64.81447f), order_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        1314,
        Terminal.Constructor(Vector3(2596.009f, 3158.894f, 64.81447f), order_terminal),
        owning_building_guid = 41
      )
      LocalObject(
        1810,
        SpawnTube.Constructor(Vector3(2585.516f, 3172.577f, 62.96447f), respawn_tube_tower, Vector3(0, 0, 104)),
        owning_building_guid = 41
      )
      LocalObject(
        1811,
        SpawnTube.Constructor(Vector3(2601.439f, 3168.608f, 62.96447f), respawn_tube_tower, Vector3(0, 0, 104)),
        owning_building_guid = 41
      )
      LocalObject(
        1201,
        FacilityTurret.Constructor(Vector3(2590.74f, 3195.377f, 92.42447f), manned_turret),
        owning_building_guid = 41
      )
      TurretToWeapon(1201, 5053)
      LocalObject(
        1202,
        FacilityTurret.Constructor(Vector3(2606.851f, 3154.952f, 92.42447f), manned_turret),
        owning_building_guid = 41
      )
      TurretToWeapon(1202, 5054)
      LocalObject(
        1669,
        Painbox.Constructor(Vector3(2586.406f, 3165.895f, 63.58247f), painbox_radius_continuous),
        owning_building_guid = 41
      )
      LocalObject(
        1670,
        Painbox.Constructor(Vector3(2592.721f, 3176.42f, 64.98157f), painbox_radius_continuous),
        owning_building_guid = 41
      )
      LocalObject(
        1671,
        Painbox.Constructor(Vector3(2597.938f, 3163.108f, 63.58247f), painbox_radius_continuous),
        owning_building_guid = 41
      )
    }

    Building21()

    def Building21(): Unit = { // Name: S_Mont_Tower Type: tower_a GUID: 42, MapID: 21
      LocalBuilding(
        "S_Mont_Tower",
        42,
        21,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3124f, 3784f, 97.89117f), Vector3(0f, 0f, 5f), tower_a)
        )
      )
      LocalObject(
        1879,
        CaptureTerminal.Constructor(Vector3(3140.533f, 3785.343f, 107.8902f), secondary_capture),
        owning_building_guid = 42
      )
      LocalObject(246, Door.Constructor(Vector3(3135.257f, 3793.015f, 99.41218f)), owning_building_guid = 42)
      LocalObject(247, Door.Constructor(Vector3(3135.257f, 3793.015f, 119.4112f)), owning_building_guid = 42)
      LocalObject(248, Door.Constructor(Vector3(3136.652f, 3777.076f, 99.41218f)), owning_building_guid = 42)
      LocalObject(249, Door.Constructor(Vector3(3136.652f, 3777.076f, 119.4112f)), owning_building_guid = 42)
      LocalObject(1966, Door.Constructor(Vector3(3134.65f, 3790.156f, 89.22717f)), owning_building_guid = 42)
      LocalObject(1967, Door.Constructor(Vector3(3136.08f, 3773.808f, 89.22717f)), owning_building_guid = 42)
      LocalObject(
        717,
        IFFLock.Constructor(Vector3(3133.151f, 3793.645f, 99.35217f), Vector3(0, 0, 355)),
        owning_building_guid = 42,
        door_guid = 246
      )
      LocalObject(
        718,
        IFFLock.Constructor(Vector3(3133.151f, 3793.645f, 119.3522f), Vector3(0, 0, 355)),
        owning_building_guid = 42,
        door_guid = 247
      )
      LocalObject(
        719,
        IFFLock.Constructor(Vector3(3138.761f, 3776.447f, 99.35217f), Vector3(0, 0, 175)),
        owning_building_guid = 42,
        door_guid = 248
      )
      LocalObject(
        720,
        IFFLock.Constructor(Vector3(3138.761f, 3776.447f, 119.3522f), Vector3(0, 0, 175)),
        owning_building_guid = 42,
        door_guid = 249
      )
      LocalObject(901, Locker.Constructor(Vector3(3139.095f, 3792.182f, 87.88518f)), owning_building_guid = 42)
      LocalObject(902, Locker.Constructor(Vector3(3140.427f, 3792.298f, 87.88518f)), owning_building_guid = 42)
      LocalObject(903, Locker.Constructor(Vector3(3140.967f, 3770.39f, 87.88518f)), owning_building_guid = 42)
      LocalObject(904, Locker.Constructor(Vector3(3142.299f, 3770.507f, 87.88518f)), owning_building_guid = 42)
      LocalObject(905, Locker.Constructor(Vector3(3143.07f, 3792.53f, 87.88518f)), owning_building_guid = 42)
      LocalObject(906, Locker.Constructor(Vector3(3144.467f, 3792.652f, 87.88518f)), owning_building_guid = 42)
      LocalObject(907, Locker.Constructor(Vector3(3144.977f, 3770.741f, 87.88518f)), owning_building_guid = 42)
      LocalObject(908, Locker.Constructor(Vector3(3146.373f, 3770.863f, 87.88518f)), owning_building_guid = 42)
      LocalObject(
        1326,
        Terminal.Constructor(Vector3(3145.256f, 3787.098f, 89.22318f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1327,
        Terminal.Constructor(Vector3(3145.725f, 3781.738f, 89.22318f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1328,
        Terminal.Constructor(Vector3(3146.224f, 3776.036f, 89.22318f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1817,
        SpawnTube.Constructor(Vector3(3134.303f, 3789.069f, 87.37318f), respawn_tube_tower, Vector3(0, 0, 355)),
        owning_building_guid = 42
      )
      LocalObject(
        1818,
        SpawnTube.Constructor(Vector3(3135.734f, 3772.722f, 87.37318f), respawn_tube_tower, Vector3(0, 0, 355)),
        owning_building_guid = 42
      )
      LocalObject(
        1210,
        FacilityTurret.Constructor(Vector3(3112.476f, 3770.238f, 116.8332f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1210, 5055)
      LocalObject(
        1213,
        FacilityTurret.Constructor(Vector3(3145.453f, 3798.633f, 116.8332f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1213, 5056)
      LocalObject(
        1675,
        Painbox.Constructor(Vector3(3129.755f, 3778.283f, 89.39027f), painbox_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1676,
        Painbox.Constructor(Vector3(3140.643f, 3787.55f, 87.99117f), painbox_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1677,
        Painbox.Constructor(Vector3(3141.763f, 3775.739f, 87.99117f), painbox_radius_continuous),
        owning_building_guid = 42
      )
    }

    Building14()

    def Building14(): Unit = { // Name: NE_Amerish_Warpgate_Tower Type: tower_a GUID: 43, MapID: 14
      LocalBuilding(
        "NE_Amerish_Warpgate_Tower",
        43,
        14,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3148f, 1398f, 56.7095f), Vector3(0f, 0f, 34f), tower_a)
        )
      )
      LocalObject(
        1880,
        CaptureTerminal.Constructor(Vector3(3161.809f, 1407.19f, 66.7085f), secondary_capture),
        owning_building_guid = 43
      )
      LocalObject(253, Door.Constructor(Vector3(3153.475f, 1411.343f, 58.2305f)), owning_building_guid = 43)
      LocalObject(254, Door.Constructor(Vector3(3153.475f, 1411.343f, 78.22949f)), owning_building_guid = 43)
      LocalObject(256, Door.Constructor(Vector3(3162.422f, 1398.078f, 58.2305f)), owning_building_guid = 43)
      LocalObject(257, Door.Constructor(Vector3(3162.422f, 1398.078f, 78.22949f)), owning_building_guid = 43)
      LocalObject(1968, Door.Constructor(Vector3(3154.33f, 1408.547f, 48.04549f)), owning_building_guid = 43)
      LocalObject(1969, Door.Constructor(Vector3(3163.507f, 1394.943f, 48.04549f)), owning_building_guid = 43)
      LocalObject(
        721,
        IFFLock.Constructor(Vector3(3151.328f, 1410.873f, 58.17049f), Vector3(0, 0, 326)),
        owning_building_guid = 43,
        door_guid = 253
      )
      LocalObject(
        722,
        IFFLock.Constructor(Vector3(3151.328f, 1410.873f, 78.17049f), Vector3(0, 0, 326)),
        owning_building_guid = 43,
        door_guid = 254
      )
      LocalObject(
        727,
        IFFLock.Constructor(Vector3(3164.573f, 1398.55f, 58.17049f), Vector3(0, 0, 146)),
        owning_building_guid = 43,
        door_guid = 256
      )
      LocalObject(
        728,
        IFFLock.Constructor(Vector3(3164.573f, 1398.55f, 78.17049f), Vector3(0, 0, 146)),
        owning_building_guid = 43,
        door_guid = 257
      )
      LocalObject(909, Locker.Constructor(Vector3(3157.236f, 1412.474f, 46.7035f)), owning_building_guid = 43)
      LocalObject(910, Locker.Constructor(Vector3(3158.344f, 1413.222f, 46.7035f)), owning_building_guid = 43)
      LocalObject(911, Locker.Constructor(Vector3(3160.544f, 1414.705f, 46.7035f)), owning_building_guid = 43)
      LocalObject(912, Locker.Constructor(Vector3(3161.706f, 1415.49f, 46.7035f)), owning_building_guid = 43)
      LocalObject(916, Locker.Constructor(Vector3(3169.438f, 1394.322f, 46.7035f)), owning_building_guid = 43)
      LocalObject(918, Locker.Constructor(Vector3(3170.546f, 1395.07f, 46.7035f)), owning_building_guid = 43)
      LocalObject(919, Locker.Constructor(Vector3(3172.775f, 1396.573f, 46.7035f)), owning_building_guid = 43)
      LocalObject(920, Locker.Constructor(Vector3(3173.937f, 1397.357f, 46.7035f)), owning_building_guid = 43)
      LocalObject(
        1329,
        Terminal.Constructor(Vector3(3165.089f, 1411.015f, 48.0415f), order_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1330,
        Terminal.Constructor(Vector3(3168.098f, 1406.554f, 48.0415f), order_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1331,
        Terminal.Constructor(Vector3(3171.299f, 1401.808f, 48.0415f), order_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1819,
        SpawnTube.Constructor(Vector3(3154.554f, 1407.429f, 46.1915f), respawn_tube_tower, Vector3(0, 0, 326)),
        owning_building_guid = 43
      )
      LocalObject(
        1820,
        SpawnTube.Constructor(Vector3(3163.73f, 1393.824f, 46.1915f), respawn_tube_tower, Vector3(0, 0, 326)),
        owning_building_guid = 43
      )
      LocalObject(
        1212,
        FacilityTurret.Constructor(Vector3(3144.592f, 1380.376f, 75.6515f), manned_turret),
        owning_building_guid = 43
      )
      TurretToWeapon(1212, 5057)
      LocalObject(
        1214,
        FacilityTurret.Constructor(Vector3(3159.669f, 1421.199f, 75.6515f), manned_turret),
        owning_building_guid = 43
      )
      TurretToWeapon(1214, 5058)
      LocalObject(
        1678,
        Painbox.Constructor(Vector3(3155.805f, 1395.79f, 48.2086f), painbox_radius_continuous),
        owning_building_guid = 43
      )
      LocalObject(
        1679,
        Painbox.Constructor(Vector3(3160.835f, 1409.174f, 46.80949f), painbox_radius_continuous),
        owning_building_guid = 43
      )
      LocalObject(
        1680,
        Painbox.Constructor(Vector3(3167.541f, 1399.386f, 46.80949f), painbox_radius_continuous),
        owning_building_guid = 43
      )
    }

    Building32()

    def Building32(): Unit = { // Name: SW__Aton_Tower Type: tower_a GUID: 44, MapID: 32
      LocalBuilding(
        "SW__Aton_Tower",
        44,
        32,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3572f, 4886f, 68.03491f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        1883,
        CaptureTerminal.Constructor(Vector3(3588.587f, 4885.897f, 78.03391f), secondary_capture),
        owning_building_guid = 44
      )
      LocalObject(286, Door.Constructor(Vector3(3584f, 4878f, 69.55592f)), owning_building_guid = 44)
      LocalObject(287, Door.Constructor(Vector3(3584f, 4878f, 89.55492f)), owning_building_guid = 44)
      LocalObject(288, Door.Constructor(Vector3(3584f, 4894f, 69.55592f)), owning_building_guid = 44)
      LocalObject(289, Door.Constructor(Vector3(3584f, 4894f, 89.55492f)), owning_building_guid = 44)
      LocalObject(1977, Door.Constructor(Vector3(3583.146f, 4874.794f, 59.37091f)), owning_building_guid = 44)
      LocalObject(1978, Door.Constructor(Vector3(3583.146f, 4891.204f, 59.37091f)), owning_building_guid = 44)
      LocalObject(
        747,
        IFFLock.Constructor(Vector3(3581.957f, 4894.811f, 69.49591f), Vector3(0, 0, 0)),
        owning_building_guid = 44,
        door_guid = 288
      )
      LocalObject(
        748,
        IFFLock.Constructor(Vector3(3581.957f, 4894.811f, 89.49591f), Vector3(0, 0, 0)),
        owning_building_guid = 44,
        door_guid = 289
      )
      LocalObject(
        749,
        IFFLock.Constructor(Vector3(3586.047f, 4877.189f, 69.49591f), Vector3(0, 0, 180)),
        owning_building_guid = 44,
        door_guid = 286
      )
      LocalObject(
        750,
        IFFLock.Constructor(Vector3(3586.047f, 4877.189f, 89.49591f), Vector3(0, 0, 180)),
        owning_building_guid = 44,
        door_guid = 287
      )
      LocalObject(945, Locker.Constructor(Vector3(3587.716f, 4870.963f, 58.02891f)), owning_building_guid = 44)
      LocalObject(946, Locker.Constructor(Vector3(3587.751f, 4892.835f, 58.02891f)), owning_building_guid = 44)
      LocalObject(947, Locker.Constructor(Vector3(3589.053f, 4870.963f, 58.02891f)), owning_building_guid = 44)
      LocalObject(948, Locker.Constructor(Vector3(3589.088f, 4892.835f, 58.02891f)), owning_building_guid = 44)
      LocalObject(949, Locker.Constructor(Vector3(3591.741f, 4870.963f, 58.02891f)), owning_building_guid = 44)
      LocalObject(950, Locker.Constructor(Vector3(3591.741f, 4892.835f, 58.02891f)), owning_building_guid = 44)
      LocalObject(951, Locker.Constructor(Vector3(3593.143f, 4870.963f, 58.02891f)), owning_building_guid = 44)
      LocalObject(952, Locker.Constructor(Vector3(3593.143f, 4892.835f, 58.02891f)), owning_building_guid = 44)
      LocalObject(
        1344,
        Terminal.Constructor(Vector3(3593.445f, 4876.129f, 59.36691f), order_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        1345,
        Terminal.Constructor(Vector3(3593.445f, 4881.853f, 59.36691f), order_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        1346,
        Terminal.Constructor(Vector3(3593.445f, 4887.234f, 59.36691f), order_terminal),
        owning_building_guid = 44
      )
      LocalObject(
        1828,
        SpawnTube.Constructor(Vector3(3582.706f, 4873.742f, 57.51691f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 44
      )
      LocalObject(
        1829,
        SpawnTube.Constructor(Vector3(3582.706f, 4890.152f, 57.51691f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 44
      )
      LocalObject(
        1225,
        FacilityTurret.Constructor(Vector3(3559.32f, 4873.295f, 86.97691f), manned_turret),
        owning_building_guid = 44
      )
      TurretToWeapon(1225, 5059)
      LocalObject(
        1227,
        FacilityTurret.Constructor(Vector3(3594.647f, 4898.707f, 86.97691f), manned_turret),
        owning_building_guid = 44
      )
      TurretToWeapon(1227, 5060)
      LocalObject(
        1687,
        Painbox.Constructor(Vector3(3577.235f, 4879.803f, 59.53401f), painbox_radius_continuous),
        owning_building_guid = 44
      )
      LocalObject(
        1688,
        Painbox.Constructor(Vector3(3588.889f, 4888.086f, 58.13491f), painbox_radius_continuous),
        owning_building_guid = 44
      )
      LocalObject(
        1689,
        Painbox.Constructor(Vector3(3588.975f, 4876.223f, 58.13491f), painbox_radius_continuous),
        owning_building_guid = 44
      )
    }

    Building38()

    def Building38(): Unit = { // Name: NE_Horus_Tower Type: tower_a GUID: 45, MapID: 38
      LocalBuilding(
        "NE_Horus_Tower",
        45,
        38,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3862f, 2326f, 72.06335f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        1885,
        CaptureTerminal.Constructor(Vector3(3878.587f, 2325.897f, 82.06236f), secondary_capture),
        owning_building_guid = 45
      )
      LocalObject(316, Door.Constructor(Vector3(3874f, 2318f, 73.58436f)), owning_building_guid = 45)
      LocalObject(317, Door.Constructor(Vector3(3874f, 2318f, 93.58336f)), owning_building_guid = 45)
      LocalObject(318, Door.Constructor(Vector3(3874f, 2334f, 73.58436f)), owning_building_guid = 45)
      LocalObject(319, Door.Constructor(Vector3(3874f, 2334f, 93.58336f)), owning_building_guid = 45)
      LocalObject(1987, Door.Constructor(Vector3(3873.146f, 2314.794f, 63.39935f)), owning_building_guid = 45)
      LocalObject(1988, Door.Constructor(Vector3(3873.146f, 2331.204f, 63.39935f)), owning_building_guid = 45)
      LocalObject(
        772,
        IFFLock.Constructor(Vector3(3871.957f, 2334.811f, 73.52435f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 318
      )
      LocalObject(
        773,
        IFFLock.Constructor(Vector3(3871.957f, 2334.811f, 93.52435f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 319
      )
      LocalObject(
        774,
        IFFLock.Constructor(Vector3(3876.047f, 2317.189f, 73.52435f), Vector3(0, 0, 180)),
        owning_building_guid = 45,
        door_guid = 316
      )
      LocalObject(
        775,
        IFFLock.Constructor(Vector3(3876.047f, 2317.189f, 93.52435f), Vector3(0, 0, 180)),
        owning_building_guid = 45,
        door_guid = 317
      )
      LocalObject(1003, Locker.Constructor(Vector3(3877.716f, 2310.963f, 62.05735f)), owning_building_guid = 45)
      LocalObject(1004, Locker.Constructor(Vector3(3877.751f, 2332.835f, 62.05735f)), owning_building_guid = 45)
      LocalObject(1005, Locker.Constructor(Vector3(3879.053f, 2310.963f, 62.05735f)), owning_building_guid = 45)
      LocalObject(1006, Locker.Constructor(Vector3(3879.088f, 2332.835f, 62.05735f)), owning_building_guid = 45)
      LocalObject(1007, Locker.Constructor(Vector3(3881.741f, 2310.963f, 62.05735f)), owning_building_guid = 45)
      LocalObject(1008, Locker.Constructor(Vector3(3881.741f, 2332.835f, 62.05735f)), owning_building_guid = 45)
      LocalObject(1009, Locker.Constructor(Vector3(3883.143f, 2310.963f, 62.05735f)), owning_building_guid = 45)
      LocalObject(1010, Locker.Constructor(Vector3(3883.143f, 2332.835f, 62.05735f)), owning_building_guid = 45)
      LocalObject(
        1358,
        Terminal.Constructor(Vector3(3883.445f, 2316.129f, 63.39536f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1359,
        Terminal.Constructor(Vector3(3883.445f, 2321.853f, 63.39536f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1360,
        Terminal.Constructor(Vector3(3883.445f, 2327.234f, 63.39536f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1838,
        SpawnTube.Constructor(Vector3(3872.706f, 2313.742f, 61.54536f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        1839,
        SpawnTube.Constructor(Vector3(3872.706f, 2330.152f, 61.54536f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        1235,
        FacilityTurret.Constructor(Vector3(3849.32f, 2313.295f, 91.00536f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1235, 5061)
      LocalObject(
        1236,
        FacilityTurret.Constructor(Vector3(3884.647f, 2338.707f, 91.00536f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1236, 5062)
      LocalObject(
        1693,
        Painbox.Constructor(Vector3(3867.235f, 2319.803f, 63.56245f), painbox_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        1694,
        Painbox.Constructor(Vector3(3878.889f, 2328.086f, 62.16335f), painbox_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        1695,
        Painbox.Constructor(Vector3(3878.975f, 2316.223f, 62.16335f), painbox_radius_continuous),
        owning_building_guid = 45
      )
    }

    Building17()

    def Building17(): Unit = { // Name: W_Thoth_Tower Type: tower_a GUID: 46, MapID: 17
      LocalBuilding(
        "W_Thoth_Tower",
        46,
        17,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4290f, 3328f, 67.31162f), Vector3(0f, 0f, 25f), tower_a)
        )
      )
      LocalObject(
        1888,
        CaptureTerminal.Constructor(Vector3(4305.077f, 3334.917f, 77.31062f), secondary_capture),
        owning_building_guid = 46
      )
      LocalObject(342, Door.Constructor(Vector3(4297.495f, 3340.322f, 68.83263f)), owning_building_guid = 46)
      LocalObject(343, Door.Constructor(Vector3(4297.495f, 3340.322f, 88.83162f)), owning_building_guid = 46)
      LocalObject(344, Door.Constructor(Vector3(4304.257f, 3325.821f, 68.83263f)), owning_building_guid = 46)
      LocalObject(345, Door.Constructor(Vector3(4304.257f, 3325.821f, 88.83162f)), owning_building_guid = 46)
      LocalObject(1993, Door.Constructor(Vector3(4297.902f, 3337.427f, 58.64762f)), owning_building_guid = 46)
      LocalObject(1994, Door.Constructor(Vector3(4304.837f, 3322.554f, 58.64762f)), owning_building_guid = 46)
      LocalObject(
        790,
        IFFLock.Constructor(Vector3(4295.3f, 3340.194f, 68.77262f), Vector3(0, 0, 335)),
        owning_building_guid = 46,
        door_guid = 342
      )
      LocalObject(
        791,
        IFFLock.Constructor(Vector3(4295.3f, 3340.194f, 88.77262f), Vector3(0, 0, 335)),
        owning_building_guid = 46,
        door_guid = 343
      )
      LocalObject(
        793,
        IFFLock.Constructor(Vector3(4306.455f, 3325.951f, 68.77262f), Vector3(0, 0, 155)),
        owning_building_guid = 46,
        door_guid = 344
      )
      LocalObject(
        794,
        IFFLock.Constructor(Vector3(4306.455f, 3325.951f, 88.77262f), Vector3(0, 0, 155)),
        owning_building_guid = 46,
        door_guid = 345
      )
      LocalObject(1027, Locker.Constructor(Vector3(4301.387f, 3340.851f, 57.30562f)), owning_building_guid = 46)
      LocalObject(1028, Locker.Constructor(Vector3(4302.599f, 3341.416f, 57.30562f)), owning_building_guid = 46)
      LocalObject(1029, Locker.Constructor(Vector3(4305.003f, 3342.538f, 57.30562f)), owning_building_guid = 46)
      LocalObject(1030, Locker.Constructor(Vector3(4306.273f, 3343.13f, 57.30562f)), owning_building_guid = 46)
      LocalObject(1035, Locker.Constructor(Vector3(4310.599f, 3321.014f, 57.30562f)), owning_building_guid = 46)
      LocalObject(1036, Locker.Constructor(Vector3(4311.81f, 3321.579f, 57.30562f)), owning_building_guid = 46)
      LocalObject(1037, Locker.Constructor(Vector3(4314.246f, 3322.715f, 57.30562f)), owning_building_guid = 46)
      LocalObject(1038, Locker.Constructor(Vector3(4315.517f, 3323.307f, 57.30562f)), owning_building_guid = 46)
      LocalObject(
        1371,
        Terminal.Constructor(Vector3(4308.914f, 3338.181f, 58.64362f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1372,
        Terminal.Constructor(Vector3(4311.188f, 3333.305f, 58.64362f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1373,
        Terminal.Constructor(Vector3(4313.607f, 3328.117f, 58.64362f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1844,
        SpawnTube.Constructor(Vector3(4297.948f, 3336.288f, 56.79362f), respawn_tube_tower, Vector3(0, 0, 335)),
        owning_building_guid = 46
      )
      LocalObject(
        1845,
        SpawnTube.Constructor(Vector3(4304.883f, 3321.415f, 56.79362f), respawn_tube_tower, Vector3(0, 0, 335)),
        owning_building_guid = 46
      )
      LocalObject(
        1247,
        FacilityTurret.Constructor(Vector3(4283.877f, 3311.126f, 86.25362f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1247, 5063)
      LocalObject(
        1248,
        FacilityTurret.Constructor(Vector3(4305.155f, 3349.087f, 86.25362f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1248, 5064)
      LocalObject(
        1702,
        Painbox.Constructor(Vector3(4297.363f, 3324.596f, 58.81072f), painbox_radius_continuous),
        owning_building_guid = 46
      )
      LocalObject(
        1703,
        Painbox.Constructor(Vector3(4304.425f, 3337.028f, 57.41162f), painbox_radius_continuous),
        owning_building_guid = 46
      )
      LocalObject(
        1704,
        Painbox.Constructor(Vector3(4309.517f, 3326.313f, 57.41162f), painbox_radius_continuous),
        owning_building_guid = 46
      )
    }

    Building36()

    def Building36(): Unit = { // Name: E_Tower_Seth Type: tower_a GUID: 47, MapID: 36
      LocalBuilding(
        "E_Tower_Seth",
        47,
        36,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4828f, 6192f, 67.78428f), Vector3(0f, 0f, 33f), tower_a)
        )
      )
      LocalObject(
        1892,
        CaptureTerminal.Constructor(Vector3(4841.967f, 6200.948f, 77.78328f), secondary_capture),
        owning_building_guid = 47
      )
      LocalObject(412, Door.Constructor(Vector3(4833.707f, 6205.245f, 69.30528f)), owning_building_guid = 47)
      LocalObject(413, Door.Constructor(Vector3(4833.707f, 6205.245f, 89.30428f)), owning_building_guid = 47)
      LocalObject(414, Door.Constructor(Vector3(4842.421f, 6191.826f, 69.30528f)), owning_building_guid = 47)
      LocalObject(415, Door.Constructor(Vector3(4842.421f, 6191.826f, 89.30428f)), owning_building_guid = 47)
      LocalObject(2013, Door.Constructor(Vector3(4834.514f, 6202.435f, 59.12028f)), owning_building_guid = 47)
      LocalObject(2014, Door.Constructor(Vector3(4843.451f, 6188.672f, 59.12028f)), owning_building_guid = 47)
      LocalObject(
        842,
        IFFLock.Constructor(Vector3(4831.552f, 6204.812f, 69.24528f), Vector3(0, 0, 327)),
        owning_building_guid = 47,
        door_guid = 412
      )
      LocalObject(
        843,
        IFFLock.Constructor(Vector3(4831.552f, 6204.812f, 89.24528f), Vector3(0, 0, 327)),
        owning_building_guid = 47,
        door_guid = 413
      )
      LocalObject(
        844,
        IFFLock.Constructor(Vector3(4844.58f, 6192.261f, 69.24528f), Vector3(0, 0, 147)),
        owning_building_guid = 47,
        door_guid = 414
      )
      LocalObject(
        845,
        IFFLock.Constructor(Vector3(4844.58f, 6192.261f, 89.24528f), Vector3(0, 0, 147)),
        owning_building_guid = 47,
        door_guid = 415
      )
      LocalObject(1116, Locker.Constructor(Vector3(4837.487f, 6206.311f, 57.77828f)), owning_building_guid = 47)
      LocalObject(1117, Locker.Constructor(Vector3(4838.608f, 6207.039f, 57.77828f)), owning_building_guid = 47)
      LocalObject(1118, Locker.Constructor(Vector3(4840.833f, 6208.484f, 57.77828f)), owning_building_guid = 47)
      LocalObject(1119, Locker.Constructor(Vector3(4842.009f, 6209.248f, 57.77828f)), owning_building_guid = 47)
      LocalObject(1120, Locker.Constructor(Vector3(4849.37f, 6187.948f, 57.77828f)), owning_building_guid = 47)
      LocalObject(1121, Locker.Constructor(Vector3(4850.492f, 6188.677f, 57.77828f)), owning_building_guid = 47)
      LocalObject(1122, Locker.Constructor(Vector3(4852.746f, 6190.141f, 57.77828f)), owning_building_guid = 47)
      LocalObject(1123, Locker.Constructor(Vector3(4853.922f, 6190.904f, 57.77828f)), owning_building_guid = 47)
      LocalObject(
        1398,
        Terminal.Constructor(Vector3(4845.313f, 6204.715f, 59.11628f), order_terminal),
        owning_building_guid = 47
      )
      LocalObject(
        1399,
        Terminal.Constructor(Vector3(4848.244f, 6200.202f, 59.11628f), order_terminal),
        owning_building_guid = 47
      )
      LocalObject(
        1400,
        Terminal.Constructor(Vector3(4851.361f, 6195.401f, 59.11628f), order_terminal),
        owning_building_guid = 47
      )
      LocalObject(
        1864,
        SpawnTube.Constructor(Vector3(4834.717f, 6201.313f, 57.26628f), respawn_tube_tower, Vector3(0, 0, 327)),
        owning_building_guid = 47
      )
      LocalObject(
        1865,
        SpawnTube.Constructor(Vector3(4843.655f, 6187.55f, 57.26628f), respawn_tube_tower, Vector3(0, 0, 327)),
        owning_building_guid = 47
      )
      LocalObject(
        1268,
        FacilityTurret.Constructor(Vector3(4824.285f, 6174.438f, 86.72628f), manned_turret),
        owning_building_guid = 47
      )
      TurretToWeapon(1268, 5065)
      LocalObject(
        1269,
        FacilityTurret.Constructor(Vector3(4840.073f, 6214.991f, 86.72628f), manned_turret),
        owning_building_guid = 47
      )
      TurretToWeapon(1269, 5066)
      LocalObject(
        1714,
        Painbox.Constructor(Vector3(4835.766f, 6189.654f, 59.28338f), painbox_radius_continuous),
        owning_building_guid = 47
      )
      LocalObject(
        1715,
        Painbox.Constructor(Vector3(4841.028f, 6202.948f, 57.88428f), painbox_radius_continuous),
        owning_building_guid = 47
      )
      LocalObject(
        1716,
        Painbox.Constructor(Vector3(4847.562f, 6193.045f, 57.88428f), painbox_radius_continuous),
        owning_building_guid = 47
      )
    }

    Building24()

    def Building24(): Unit = { // Name: E_Bastet_Tower Type: tower_a GUID: 48, MapID: 24
      LocalBuilding(
        "E_Bastet_Tower",
        48,
        24,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5524f, 5436f, 74.02264f), Vector3(0f, 0f, 332f), tower_a)
        )
      )
      LocalObject(
        1895,
        CaptureTerminal.Constructor(Vector3(5538.597f, 5428.122f, 84.02164f), secondary_capture),
        owning_building_guid = 48
      )
      LocalObject(441, Door.Constructor(Vector3(5530.839f, 5423.303f, 75.54365f)), owning_building_guid = 48)
      LocalObject(442, Door.Constructor(Vector3(5530.839f, 5423.303f, 95.54265f)), owning_building_guid = 48)
      LocalObject(443, Door.Constructor(Vector3(5538.351f, 5437.43f, 75.54365f)), owning_building_guid = 48)
      LocalObject(444, Door.Constructor(Vector3(5538.351f, 5437.43f, 95.54265f)), owning_building_guid = 48)
      LocalObject(2022, Door.Constructor(Vector3(5528.581f, 5420.873f, 65.35864f)), owning_building_guid = 48)
      LocalObject(2023, Door.Constructor(Vector3(5536.285f, 5435.362f, 65.35864f)), owning_building_guid = 48)
      LocalObject(
        865,
        IFFLock.Constructor(Vector3(5532.266f, 5421.625f, 75.48364f), Vector3(0, 0, 208)),
        owning_building_guid = 48,
        door_guid = 441
      )
      LocalObject(
        866,
        IFFLock.Constructor(Vector3(5532.266f, 5421.625f, 95.48364f), Vector3(0, 0, 208)),
        owning_building_guid = 48,
        door_guid = 442
      )
      LocalObject(
        867,
        IFFLock.Constructor(Vector3(5536.928f, 5439.105f, 75.48364f), Vector3(0, 0, 28)),
        owning_building_guid = 48,
        door_guid = 443
      )
      LocalObject(
        868,
        IFFLock.Constructor(Vector3(5536.928f, 5439.105f, 95.48364f), Vector3(0, 0, 28)),
        owning_building_guid = 48,
        door_guid = 444
      )
      LocalObject(1147, Locker.Constructor(Vector3(5530.817f, 5415.345f, 64.01665f)), owning_building_guid = 48)
      LocalObject(1150, Locker.Constructor(Vector3(5531.998f, 5414.717f, 64.01665f)), owning_building_guid = 48)
      LocalObject(1152, Locker.Constructor(Vector3(5534.371f, 5413.455f, 64.01665f)), owning_building_guid = 48)
      LocalObject(1154, Locker.Constructor(Vector3(5535.609f, 5412.797f, 64.01665f)), owning_building_guid = 48)
      LocalObject(1156, Locker.Constructor(Vector3(5541.116f, 5434.64f, 64.01665f)), owning_building_guid = 48)
      LocalObject(1157, Locker.Constructor(Vector3(5542.297f, 5434.013f, 64.01665f)), owning_building_guid = 48)
      LocalObject(1158, Locker.Constructor(Vector3(5544.639f, 5432.767f, 64.01665f)), owning_building_guid = 48)
      LocalObject(1159, Locker.Constructor(Vector3(5545.877f, 5432.109f, 64.01665f)), owning_building_guid = 48)
      LocalObject(
        1413,
        Terminal.Constructor(Vector3(5538.301f, 5417.217f, 65.35464f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1414,
        Terminal.Constructor(Vector3(5540.988f, 5422.271f, 65.35464f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1415,
        Terminal.Constructor(Vector3(5543.514f, 5427.022f, 65.35464f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1873,
        SpawnTube.Constructor(Vector3(5527.698f, 5420.151f, 63.50465f), respawn_tube_tower, Vector3(0, 0, 28)),
        owning_building_guid = 48
      )
      LocalObject(
        1874,
        SpawnTube.Constructor(Vector3(5535.402f, 5434.64f, 63.50465f), respawn_tube_tower, Vector3(0, 0, 28)),
        owning_building_guid = 48
      )
      LocalObject(
        1279,
        FacilityTurret.Constructor(Vector3(5506.839f, 5430.735f, 92.96465f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1279, 5067)
      LocalObject(
        1281,
        FacilityTurret.Constructor(Vector3(5549.962f, 5436.587f, 92.96465f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1281, 5068)
      LocalObject(
        1721,
        Painbox.Constructor(Vector3(5525.713f, 5428.071f, 65.52174f), painbox_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        1724,
        Painbox.Constructor(Vector3(5534.398f, 5419.398f, 64.12264f), painbox_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        1725,
        Painbox.Constructor(Vector3(5539.892f, 5429.913f, 64.12264f), painbox_radius_continuous),
        owning_building_guid = 48
      )
    }

    Building25()

    def Building25(): Unit = { // Name: NW_Cyssor_Warpgate_Tower Type: tower_a GUID: 49, MapID: 25
      LocalBuilding(
        "NW_Cyssor_Warpgate_Tower",
        49,
        25,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5636f, 3622f, 162.7766f), Vector3(0f, 0f, 342f), tower_a)
        )
      )
      LocalObject(
        1896,
        CaptureTerminal.Constructor(Vector3(5651.743f, 3616.776f, 172.7756f), secondary_capture),
        owning_building_guid = 49
      )
      LocalObject(445, Door.Constructor(Vector3(5644.94f, 3610.683f, 164.2976f)), owning_building_guid = 49)
      LocalObject(446, Door.Constructor(Vector3(5644.94f, 3610.683f, 184.2966f)), owning_building_guid = 49)
      LocalObject(447, Door.Constructor(Vector3(5649.885f, 3625.9f, 164.2976f)), owning_building_guid = 49)
      LocalObject(448, Door.Constructor(Vector3(5649.885f, 3625.9f, 184.2966f)), owning_building_guid = 49)
      LocalObject(2024, Door.Constructor(Vector3(5643.138f, 3607.898f, 154.1126f)), owning_building_guid = 49)
      LocalObject(2025, Door.Constructor(Vector3(5648.208f, 3623.505f, 154.1126f)), owning_building_guid = 49)
      LocalObject(
        869,
        IFFLock.Constructor(Vector3(5646.637f, 3609.28f, 164.2376f), Vector3(0, 0, 198)),
        owning_building_guid = 49,
        door_guid = 445
      )
      LocalObject(
        870,
        IFFLock.Constructor(Vector3(5646.637f, 3609.28f, 184.2376f), Vector3(0, 0, 198)),
        owning_building_guid = 49,
        door_guid = 446
      )
      LocalObject(
        871,
        IFFLock.Constructor(Vector3(5648.192f, 3627.303f, 164.2376f), Vector3(0, 0, 18)),
        owning_building_guid = 49,
        door_guid = 447
      )
      LocalObject(
        872,
        IFFLock.Constructor(Vector3(5648.192f, 3627.303f, 184.2376f), Vector3(0, 0, 18)),
        owning_building_guid = 49,
        door_guid = 448
      )
      LocalObject(1160, Locker.Constructor(Vector3(5646.3f, 3602.843f, 152.7706f)), owning_building_guid = 49)
      LocalObject(1161, Locker.Constructor(Vector3(5647.572f, 3602.429f, 152.7706f)), owning_building_guid = 49)
      LocalObject(1162, Locker.Constructor(Vector3(5650.128f, 3601.599f, 152.7706f)), owning_building_guid = 49)
      LocalObject(1163, Locker.Constructor(Vector3(5651.461f, 3601.166f, 152.7706f)), owning_building_guid = 49)
      LocalObject(1164, Locker.Constructor(Vector3(5653.092f, 3623.633f, 152.7706f)), owning_building_guid = 49)
      LocalObject(1165, Locker.Constructor(Vector3(5654.364f, 3623.22f, 152.7706f)), owning_building_guid = 49)
      LocalObject(1166, Locker.Constructor(Vector3(5656.887f, 3622.4f, 152.7706f)), owning_building_guid = 49)
      LocalObject(1167, Locker.Constructor(Vector3(5658.22f, 3621.967f, 152.7706f)), owning_building_guid = 49)
      LocalObject(
        1416,
        Terminal.Constructor(Vector3(5653.345f, 3605.985f, 154.1086f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1417,
        Terminal.Constructor(Vector3(5655.114f, 3611.429f, 154.1086f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1418,
        Terminal.Constructor(Vector3(5656.777f, 3616.547f, 154.1086f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1875,
        SpawnTube.Constructor(Vector3(5642.394f, 3607.034f, 152.2586f), respawn_tube_tower, Vector3(0, 0, 18)),
        owning_building_guid = 49
      )
      LocalObject(
        1876,
        SpawnTube.Constructor(Vector3(5647.465f, 3622.64f, 152.2586f), respawn_tube_tower, Vector3(0, 0, 18)),
        owning_building_guid = 49
      )
      LocalObject(
        1282,
        FacilityTurret.Constructor(Vector3(5620.015f, 3613.835f, 181.7186f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1282, 5069)
      LocalObject(
        1283,
        FacilityTurret.Constructor(Vector3(5661.465f, 3627.087f, 181.7186f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1283, 5070)
      LocalObject(
        1726,
        Painbox.Constructor(Vector3(5639.064f, 3614.489f, 154.2757f), painbox_radius_continuous),
        owning_building_guid = 49
      )
      LocalObject(
        1727,
        Painbox.Constructor(Vector3(5649.123f, 3607.456f, 152.8766f), painbox_radius_continuous),
        owning_building_guid = 49
      )
      LocalObject(
        1728,
        Painbox.Constructor(Vector3(5652.707f, 3618.765f, 152.8766f), painbox_radius_continuous),
        owning_building_guid = 49
      )
    }

    Building22()

    def Building22(): Unit = { // Name: S_Sobek_Tower Type: tower_b GUID: 50, MapID: 22
      LocalBuilding(
        "S_Sobek_Tower",
        50,
        22,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3162f, 2856f, 70.09491f), Vector3(0f, 0f, 51f), tower_b)
        )
      )
      LocalObject(
        1881,
        CaptureTerminal.Constructor(Vector3(3172.519f, 2868.826f, 90.09391f), secondary_capture),
        owning_building_guid = 50
      )
      LocalObject(258, Door.Constructor(Vector3(3163.335f, 2870.36f, 71.61491f)), owning_building_guid = 50)
      LocalObject(259, Door.Constructor(Vector3(3163.335f, 2870.36f, 81.61491f)), owning_building_guid = 50)
      LocalObject(260, Door.Constructor(Vector3(3163.335f, 2870.36f, 101.6149f)), owning_building_guid = 50)
      LocalObject(261, Door.Constructor(Vector3(3175.769f, 2860.291f, 71.61491f)), owning_building_guid = 50)
      LocalObject(262, Door.Constructor(Vector3(3175.769f, 2860.291f, 81.61491f)), owning_building_guid = 50)
      LocalObject(263, Door.Constructor(Vector3(3175.769f, 2860.291f, 101.6149f)), owning_building_guid = 50)
      LocalObject(1970, Door.Constructor(Vector3(3164.971f, 2867.938f, 61.43091f)), owning_building_guid = 50)
      LocalObject(1971, Door.Constructor(Vector3(3177.724f, 2857.611f, 61.43091f)), owning_building_guid = 50)
      LocalObject(
        724,
        IFFLock.Constructor(Vector3(3161.419f, 2869.283f, 71.55591f), Vector3(0, 0, 309)),
        owning_building_guid = 50,
        door_guid = 258
      )
      LocalObject(
        725,
        IFFLock.Constructor(Vector3(3161.419f, 2869.283f, 81.55591f), Vector3(0, 0, 309)),
        owning_building_guid = 50,
        door_guid = 259
      )
      LocalObject(
        726,
        IFFLock.Constructor(Vector3(3161.419f, 2869.283f, 101.5559f), Vector3(0, 0, 309)),
        owning_building_guid = 50,
        door_guid = 260
      )
      LocalObject(
        729,
        IFFLock.Constructor(Vector3(3177.688f, 2861.372f, 71.55591f), Vector3(0, 0, 129)),
        owning_building_guid = 50,
        door_guid = 261
      )
      LocalObject(
        730,
        IFFLock.Constructor(Vector3(3177.688f, 2861.372f, 81.55591f), Vector3(0, 0, 129)),
        owning_building_guid = 50,
        door_guid = 262
      )
      LocalObject(
        731,
        IFFLock.Constructor(Vector3(3177.688f, 2861.372f, 101.5559f), Vector3(0, 0, 129)),
        owning_building_guid = 50,
        door_guid = 263
      )
      LocalObject(913, Locker.Constructor(Vector3(3166.601f, 2872.542f, 60.08891f)), owning_building_guid = 50)
      LocalObject(914, Locker.Constructor(Vector3(3167.442f, 2873.581f, 60.08891f)), owning_building_guid = 50)
      LocalObject(915, Locker.Constructor(Vector3(3169.112f, 2875.643f, 60.08891f)), owning_building_guid = 50)
      LocalObject(917, Locker.Constructor(Vector3(3169.994f, 2876.733f, 60.08891f)), owning_building_guid = 50)
      LocalObject(921, Locker.Constructor(Vector3(3183.576f, 2858.75f, 60.08891f)), owning_building_guid = 50)
      LocalObject(922, Locker.Constructor(Vector3(3184.418f, 2859.79f, 60.08891f)), owning_building_guid = 50)
      LocalObject(923, Locker.Constructor(Vector3(3186.109f, 2861.879f, 60.08891f)), owning_building_guid = 50)
      LocalObject(924, Locker.Constructor(Vector3(3186.992f, 2862.968f, 60.08891f)), owning_building_guid = 50)
      LocalObject(
        1332,
        Terminal.Constructor(Vector3(3174.537f, 2873.443f, 61.42691f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1333,
        Terminal.Constructor(Vector3(3178.719f, 2870.057f, 61.42691f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1334,
        Terminal.Constructor(Vector3(3183.168f, 2866.455f, 61.42691f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1821,
        SpawnTube.Constructor(Vector3(3165.511f, 2866.933f, 59.57691f), respawn_tube_tower, Vector3(0, 0, 309)),
        owning_building_guid = 50
      )
      LocalObject(
        1822,
        SpawnTube.Constructor(Vector3(3178.264f, 2856.606f, 59.57691f), respawn_tube_tower, Vector3(0, 0, 309)),
        owning_building_guid = 50
      )
      LocalObject(
        1681,
        Painbox.Constructor(Vector3(3171.014f, 2855.769f, 61.38431f), painbox_radius_continuous),
        owning_building_guid = 50
      )
      LocalObject(
        1682,
        Painbox.Constructor(Vector3(3171.224f, 2870.739f, 60.19491f), painbox_radius_continuous),
        owning_building_guid = 50
      )
      LocalObject(
        1683,
        Painbox.Constructor(Vector3(3180.489f, 2863.066f, 60.19491f), painbox_radius_continuous),
        owning_building_guid = 50
      )
    }

    Building37()

    def Building37(): Unit = { // Name: N_Aton_Tower Type: tower_b GUID: 51, MapID: 37
      LocalBuilding(
        "N_Aton_Tower",
        51,
        37,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3708f, 5656f, 81.34708f), Vector3(0f, 0f, 302f), tower_b)
        )
      )
      LocalObject(
        1884,
        CaptureTerminal.Constructor(Vector3(3716.702f, 5641.879f, 101.3461f), secondary_capture),
        owning_building_guid = 51
      )
      LocalObject(297, Door.Constructor(Vector3(3707.575f, 5641.584f, 82.86708f)), owning_building_guid = 51)
      LocalObject(298, Door.Constructor(Vector3(3707.575f, 5641.584f, 92.86708f)), owning_building_guid = 51)
      LocalObject(299, Door.Constructor(Vector3(3707.575f, 5641.584f, 112.8671f)), owning_building_guid = 51)
      LocalObject(302, Door.Constructor(Vector3(3721.143f, 5650.063f, 82.86708f)), owning_building_guid = 51)
      LocalObject(303, Door.Constructor(Vector3(3721.143f, 5650.063f, 92.86708f)), owning_building_guid = 51)
      LocalObject(304, Door.Constructor(Vector3(3721.143f, 5650.063f, 112.8671f)), owning_building_guid = 51)
      LocalObject(1981, Door.Constructor(Vector3(3704.404f, 5640.608f, 72.68308f)), owning_building_guid = 51)
      LocalObject(1983, Door.Constructor(Vector3(3718.32f, 5649.305f, 72.68308f)), owning_building_guid = 51)
      LocalObject(
        755,
        IFFLock.Constructor(Vector3(3707.972f, 5639.418f, 82.80808f), Vector3(0, 0, 238)),
        owning_building_guid = 51,
        door_guid = 297
      )
      LocalObject(
        756,
        IFFLock.Constructor(Vector3(3707.972f, 5639.418f, 92.80808f), Vector3(0, 0, 238)),
        owning_building_guid = 51,
        door_guid = 298
      )
      LocalObject(
        757,
        IFFLock.Constructor(Vector3(3707.972f, 5639.418f, 112.8081f), Vector3(0, 0, 238)),
        owning_building_guid = 51,
        door_guid = 299
      )
      LocalObject(
        760,
        IFFLock.Constructor(Vector3(3720.749f, 5652.225f, 82.80808f), Vector3(0, 0, 58)),
        owning_building_guid = 51,
        door_guid = 302
      )
      LocalObject(
        761,
        IFFLock.Constructor(Vector3(3720.749f, 5652.225f, 92.80808f), Vector3(0, 0, 58)),
        owning_building_guid = 51,
        door_guid = 303
      )
      LocalObject(
        762,
        IFFLock.Constructor(Vector3(3720.749f, 5652.225f, 112.8081f), Vector3(0, 0, 58)),
        owning_building_guid = 51,
        door_guid = 304
      )
      LocalObject(963, Locker.Constructor(Vector3(3703.576f, 5634.704f, 71.34109f)), owning_building_guid = 51)
      LocalObject(964, Locker.Constructor(Vector3(3704.285f, 5633.57f, 71.34109f)), owning_building_guid = 51)
      LocalObject(965, Locker.Constructor(Vector3(3705.709f, 5631.291f, 71.34109f)), owning_building_guid = 51)
      LocalObject(966, Locker.Constructor(Vector3(3706.452f, 5630.102f, 71.34109f)), owning_building_guid = 51)
      LocalObject(973, Locker.Constructor(Vector3(3722.143f, 5646.264f, 71.34109f)), owning_building_guid = 51)
      LocalObject(974, Locker.Constructor(Vector3(3722.852f, 5645.13f, 71.34109f)), owning_building_guid = 51)
      LocalObject(975, Locker.Constructor(Vector3(3724.258f, 5642.881f, 71.34109f)), owning_building_guid = 51)
      LocalObject(976, Locker.Constructor(Vector3(3725f, 5641.692f, 71.34109f)), owning_building_guid = 51)
      LocalObject(
        1351,
        Terminal.Constructor(Vector3(3710.994f, 5632.582f, 72.67908f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1352,
        Terminal.Constructor(Vector3(3715.848f, 5635.615f, 72.67908f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1353,
        Terminal.Constructor(Vector3(3720.411f, 5638.467f, 72.67908f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1832,
        SpawnTube.Constructor(Vector3(3703.278f, 5640.425f, 70.82909f), respawn_tube_tower, Vector3(0, 0, 58)),
        owning_building_guid = 51
      )
      LocalObject(
        1834,
        SpawnTube.Constructor(Vector3(3717.194f, 5649.121f, 70.82909f), respawn_tube_tower, Vector3(0, 0, 58)),
        owning_building_guid = 51
      )
      LocalObject(
        1690,
        Painbox.Constructor(Vector3(3704.846f, 5647.552f, 72.63648f), painbox_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        1691,
        Painbox.Constructor(Vector3(3708.662f, 5636.217f, 71.44708f), painbox_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        1692,
        Painbox.Constructor(Vector3(3718.933f, 5642.479f, 71.44708f), painbox_radius_continuous),
        owning_building_guid = 51
      )
    }

    Building41()

    def Building41(): Unit = { // Name: E_Hossin_Warpgate_Tower Type: tower_b GUID: 52, MapID: 41
      LocalBuilding(
        "E_Hossin_Warpgate_Tower",
        52,
        41,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4484f, 7294f, 61.76336f), Vector3(0f, 0f, 337f), tower_b)
        )
      )
      LocalObject(
        1889,
        CaptureTerminal.Constructor(Vector3(4499.228f, 7287.424f, 81.76236f), secondary_capture),
        owning_building_guid = 52
      )
      LocalObject(365, Door.Constructor(Vector3(4491.92f, 7281.947f, 63.28336f)), owning_building_guid = 52)
      LocalObject(366, Door.Constructor(Vector3(4491.92f, 7281.947f, 73.28336f)), owning_building_guid = 52)
      LocalObject(367, Door.Constructor(Vector3(4491.92f, 7281.947f, 93.28336f)), owning_building_guid = 52)
      LocalObject(369, Door.Constructor(Vector3(4498.172f, 7296.675f, 63.28336f)), owning_building_guid = 52)
      LocalObject(370, Door.Constructor(Vector3(4498.172f, 7296.675f, 73.28336f)), owning_building_guid = 52)
      LocalObject(371, Door.Constructor(Vector3(4498.172f, 7296.675f, 93.28336f)), owning_building_guid = 52)
      LocalObject(2001, Door.Constructor(Vector3(4489.882f, 7279.329f, 53.09936f)), owning_building_guid = 52)
      LocalObject(2002, Door.Constructor(Vector3(4496.294f, 7294.435f, 53.09936f)), owning_building_guid = 52)
      LocalObject(
        809,
        IFFLock.Constructor(Vector3(4493.488f, 7280.401f, 63.22436f), Vector3(0, 0, 203)),
        owning_building_guid = 52,
        door_guid = 365
      )
      LocalObject(
        810,
        IFFLock.Constructor(Vector3(4493.488f, 7280.401f, 73.22436f), Vector3(0, 0, 203)),
        owning_building_guid = 52,
        door_guid = 366
      )
      LocalObject(
        811,
        IFFLock.Constructor(Vector3(4493.488f, 7280.401f, 93.22436f), Vector3(0, 0, 203)),
        owning_building_guid = 52,
        door_guid = 367
      )
      LocalObject(
        812,
        IFFLock.Constructor(Vector3(4496.608f, 7298.22f, 63.22436f), Vector3(0, 0, 23)),
        owning_building_guid = 52,
        door_guid = 369
      )
      LocalObject(
        813,
        IFFLock.Constructor(Vector3(4496.608f, 7298.22f, 73.22436f), Vector3(0, 0, 23)),
        owning_building_guid = 52,
        door_guid = 370
      )
      LocalObject(
        814,
        IFFLock.Constructor(Vector3(4496.608f, 7298.22f, 93.22436f), Vector3(0, 0, 23)),
        owning_building_guid = 52,
        door_guid = 371
      )
      LocalObject(1059, Locker.Constructor(Vector3(4492.591f, 7274.018f, 51.75736f)), owning_building_guid = 52)
      LocalObject(1060, Locker.Constructor(Vector3(4493.822f, 7273.495f, 51.75736f)), owning_building_guid = 52)
      LocalObject(1061, Locker.Constructor(Vector3(4496.296f, 7272.445f, 51.75736f)), owning_building_guid = 52)
      LocalObject(1062, Locker.Constructor(Vector3(4497.587f, 7271.897f, 51.75736f)), owning_building_guid = 52)
      LocalObject(1063, Locker.Constructor(Vector3(4501.169f, 7294.137f, 51.75736f)), owning_building_guid = 52)
      LocalObject(1064, Locker.Constructor(Vector3(4502.4f, 7293.615f, 51.75736f)), owning_building_guid = 52)
      LocalObject(1065, Locker.Constructor(Vector3(4504.842f, 7292.578f, 51.75736f)), owning_building_guid = 52)
      LocalObject(1066, Locker.Constructor(Vector3(4506.133f, 7292.03f, 51.75736f)), owning_building_guid = 52)
      LocalObject(
        1381,
        Terminal.Constructor(Vector3(4499.884f, 7276.534f, 53.09536f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1382,
        Terminal.Constructor(Vector3(4502.121f, 7281.803f, 53.09536f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1383,
        Terminal.Constructor(Vector3(4504.223f, 7286.756f, 53.09536f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1852,
        SpawnTube.Constructor(Vector3(4489.065f, 7278.533f, 51.24536f), respawn_tube_tower, Vector3(0, 0, 23)),
        owning_building_guid = 52
      )
      LocalObject(
        1853,
        SpawnTube.Constructor(Vector3(4495.477f, 7293.639f, 51.24536f), respawn_tube_tower, Vector3(0, 0, 23)),
        owning_building_guid = 52
      )
      LocalObject(
        1705,
        Painbox.Constructor(Vector3(4486.262f, 7285.271f, 53.05276f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        1706,
        Painbox.Constructor(Vector3(4495.889f, 7278.175f, 51.86336f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        1708,
        Painbox.Constructor(Vector3(4500.71f, 7289.195f, 51.86336f), painbox_radius_continuous),
        owning_building_guid = 52
      )
    }

    Building18()

    def Building18(): Unit = { // Name: N_Hapi_Tower Type: tower_b GUID: 53, MapID: 18
      LocalBuilding(
        "N_Hapi_Tower",
        53,
        18,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4494f, 4884f, 74.691f), Vector3(0f, 0f, 5f), tower_b)
        )
      )
      LocalObject(
        1890,
        CaptureTerminal.Constructor(Vector3(4510.533f, 4885.343f, 94.69f), secondary_capture),
        owning_building_guid = 53
      )
      LocalObject(378, Door.Constructor(Vector3(4505.257f, 4893.016f, 76.211f)), owning_building_guid = 53)
      LocalObject(379, Door.Constructor(Vector3(4505.257f, 4893.016f, 86.211f)), owning_building_guid = 53)
      LocalObject(380, Door.Constructor(Vector3(4505.257f, 4893.016f, 106.211f)), owning_building_guid = 53)
      LocalObject(381, Door.Constructor(Vector3(4506.651f, 4877.076f, 76.211f)), owning_building_guid = 53)
      LocalObject(382, Door.Constructor(Vector3(4506.651f, 4877.076f, 86.211f)), owning_building_guid = 53)
      LocalObject(383, Door.Constructor(Vector3(4506.651f, 4877.076f, 106.211f)), owning_building_guid = 53)
      LocalObject(2003, Door.Constructor(Vector3(4504.651f, 4890.156f, 66.027f)), owning_building_guid = 53)
      LocalObject(2004, Door.Constructor(Vector3(4506.081f, 4873.808f, 66.027f)), owning_building_guid = 53)
      LocalObject(
        815,
        IFFLock.Constructor(Vector3(4503.151f, 4893.646f, 76.152f), Vector3(0, 0, 355)),
        owning_building_guid = 53,
        door_guid = 378
      )
      LocalObject(
        816,
        IFFLock.Constructor(Vector3(4503.151f, 4893.646f, 86.152f), Vector3(0, 0, 355)),
        owning_building_guid = 53,
        door_guid = 379
      )
      LocalObject(
        817,
        IFFLock.Constructor(Vector3(4503.151f, 4893.646f, 106.152f), Vector3(0, 0, 355)),
        owning_building_guid = 53,
        door_guid = 380
      )
      LocalObject(
        821,
        IFFLock.Constructor(Vector3(4508.762f, 4876.447f, 76.152f), Vector3(0, 0, 175)),
        owning_building_guid = 53,
        door_guid = 381
      )
      LocalObject(
        822,
        IFFLock.Constructor(Vector3(4508.762f, 4876.447f, 86.152f), Vector3(0, 0, 175)),
        owning_building_guid = 53,
        door_guid = 382
      )
      LocalObject(
        823,
        IFFLock.Constructor(Vector3(4508.762f, 4876.447f, 106.152f), Vector3(0, 0, 175)),
        owning_building_guid = 53,
        door_guid = 383
      )
      LocalObject(1067, Locker.Constructor(Vector3(4509.095f, 4892.182f, 64.68501f)), owning_building_guid = 53)
      LocalObject(1068, Locker.Constructor(Vector3(4510.427f, 4892.298f, 64.68501f)), owning_building_guid = 53)
      LocalObject(1069, Locker.Constructor(Vector3(4510.967f, 4870.39f, 64.68501f)), owning_building_guid = 53)
      LocalObject(1070, Locker.Constructor(Vector3(4512.299f, 4870.506f, 64.68501f)), owning_building_guid = 53)
      LocalObject(1071, Locker.Constructor(Vector3(4513.07f, 4892.529f, 64.68501f)), owning_building_guid = 53)
      LocalObject(1073, Locker.Constructor(Vector3(4514.467f, 4892.652f, 64.68501f)), owning_building_guid = 53)
      LocalObject(1074, Locker.Constructor(Vector3(4514.977f, 4870.741f, 64.68501f)), owning_building_guid = 53)
      LocalObject(1076, Locker.Constructor(Vector3(4516.373f, 4870.863f, 64.68501f)), owning_building_guid = 53)
      LocalObject(
        1384,
        Terminal.Constructor(Vector3(4515.257f, 4887.099f, 66.023f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1385,
        Terminal.Constructor(Vector3(4515.726f, 4881.738f, 66.023f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1386,
        Terminal.Constructor(Vector3(4516.225f, 4876.036f, 66.023f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1854,
        SpawnTube.Constructor(Vector3(4504.303f, 4889.069f, 64.173f), respawn_tube_tower, Vector3(0, 0, 355)),
        owning_building_guid = 53
      )
      LocalObject(
        1855,
        SpawnTube.Constructor(Vector3(4505.733f, 4872.722f, 64.173f), respawn_tube_tower, Vector3(0, 0, 355)),
        owning_building_guid = 53
      )
      LocalObject(
        1707,
        Painbox.Constructor(Vector3(4500.095f, 4877.355f, 65.9804f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        1710,
        Painbox.Constructor(Vector3(4511.01f, 4887.603f, 64.791f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        1711,
        Painbox.Constructor(Vector3(4511.927f, 4875.608f, 64.791f), painbox_radius_continuous),
        owning_building_guid = 53
      )
    }

    Building15()

    def Building15(): Unit = { // Name: S_Amun_Tower Type: tower_b GUID: 54, MapID: 15
      LocalBuilding(
        "S_Amun_Tower",
        54,
        15,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4500f, 1912f, 76.47633f), Vector3(0f, 0f, 12f), tower_b)
        )
      )
      LocalObject(
        1891,
        CaptureTerminal.Constructor(Vector3(4516.246f, 1915.348f, 96.47533f), secondary_capture),
        owning_building_guid = 54
      )
      LocalObject(384, Door.Constructor(Vector3(4510.075f, 1922.32f, 77.99633f)), owning_building_guid = 54)
      LocalObject(385, Door.Constructor(Vector3(4510.075f, 1922.32f, 87.99634f)), owning_building_guid = 54)
      LocalObject(386, Door.Constructor(Vector3(4510.075f, 1922.32f, 107.9963f)), owning_building_guid = 54)
      LocalObject(388, Door.Constructor(Vector3(4513.401f, 1906.67f, 77.99633f)), owning_building_guid = 54)
      LocalObject(389, Door.Constructor(Vector3(4513.401f, 1906.67f, 87.99634f)), owning_building_guid = 54)
      LocalObject(390, Door.Constructor(Vector3(4513.401f, 1906.67f, 107.9963f)), owning_building_guid = 54)
      LocalObject(2005, Door.Constructor(Vector3(4509.821f, 1919.408f, 67.81233f)), owning_building_guid = 54)
      LocalObject(2006, Door.Constructor(Vector3(4513.233f, 1903.356f, 67.81233f)), owning_building_guid = 54)
      LocalObject(
        818,
        IFFLock.Constructor(Vector3(4507.908f, 1922.689f, 77.93733f), Vector3(0, 0, 348)),
        owning_building_guid = 54,
        door_guid = 384
      )
      LocalObject(
        819,
        IFFLock.Constructor(Vector3(4507.908f, 1922.689f, 87.93733f), Vector3(0, 0, 348)),
        owning_building_guid = 54,
        door_guid = 385
      )
      LocalObject(
        820,
        IFFLock.Constructor(Vector3(4507.908f, 1922.689f, 107.9373f), Vector3(0, 0, 348)),
        owning_building_guid = 54,
        door_guid = 386
      )
      LocalObject(
        825,
        IFFLock.Constructor(Vector3(4515.572f, 1906.302f, 77.93733f), Vector3(0, 0, 168)),
        owning_building_guid = 54,
        door_guid = 388
      )
      LocalObject(
        826,
        IFFLock.Constructor(Vector3(4515.572f, 1906.302f, 87.93733f), Vector3(0, 0, 168)),
        owning_building_guid = 54,
        door_guid = 389
      )
      LocalObject(
        827,
        IFFLock.Constructor(Vector3(4515.572f, 1906.302f, 107.9373f), Vector3(0, 0, 168)),
        owning_building_guid = 54,
        door_guid = 390
      )
      LocalObject(1072, Locker.Constructor(Vector3(4513.986f, 1921.96f, 66.47034f)), owning_building_guid = 54)
      LocalObject(1075, Locker.Constructor(Vector3(4515.293f, 1922.238f, 66.47034f)), owning_building_guid = 54)
      LocalObject(1077, Locker.Constructor(Vector3(4517.889f, 1922.79f, 66.47034f)), owning_building_guid = 54)
      LocalObject(1082, Locker.Constructor(Vector3(4518.499f, 1900.559f, 66.47034f)), owning_building_guid = 54)
      LocalObject(1083, Locker.Constructor(Vector3(4519.26f, 1923.082f, 66.47034f)), owning_building_guid = 54)
      LocalObject(1084, Locker.Constructor(Vector3(4519.807f, 1900.837f, 66.47034f)), owning_building_guid = 54)
      LocalObject(1085, Locker.Constructor(Vector3(4522.436f, 1901.396f, 66.47034f)), owning_building_guid = 54)
      LocalObject(1086, Locker.Constructor(Vector3(4523.807f, 1901.688f, 66.47034f)), owning_building_guid = 54)
      LocalObject(
        1387,
        Terminal.Constructor(Vector3(4520.721f, 1917.666f, 67.80833f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1388,
        Terminal.Constructor(Vector3(4521.839f, 1912.402f, 67.80833f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1389,
        Terminal.Constructor(Vector3(4523.03f, 1906.804f, 67.80833f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1856,
        SpawnTube.Constructor(Vector3(4509.609f, 1918.287f, 65.95834f), respawn_tube_tower, Vector3(0, 0, 348)),
        owning_building_guid = 54
      )
      LocalObject(
        1857,
        SpawnTube.Constructor(Vector3(4513.021f, 1902.236f, 65.95834f), respawn_tube_tower, Vector3(0, 0, 348)),
        owning_building_guid = 54
      )
      LocalObject(
        1709,
        Painbox.Constructor(Vector3(4506.859f, 1906.147f, 67.76573f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        1712,
        Painbox.Constructor(Vector3(4516.444f, 1917.649f, 66.57633f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        1713,
        Painbox.Constructor(Vector3(4518.816f, 1905.856f, 66.57633f), painbox_radius_continuous),
        owning_building_guid = 54
      )
    }

    Building39()

    def Building39(): Unit = { // Name: E_Forseral_Warpgate_Tower Type: tower_c GUID: 55, MapID: 39
      LocalBuilding(
        "E_Forseral_Warpgate_Tower",
        55,
        39,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2826f, 5330f, 175.1091f), Vector3(0f, 0f, 346f), tower_c)
        )
      )
      LocalObject(
        1878,
        CaptureTerminal.Constructor(Vector3(2842.069f, 5325.887f, 185.1081f), secondary_capture),
        owning_building_guid = 55
      )
      LocalObject(226, Door.Constructor(Vector3(2835.708f, 5319.334f, 176.6301f)), owning_building_guid = 55)
      LocalObject(227, Door.Constructor(Vector3(2835.708f, 5319.334f, 196.6291f)), owning_building_guid = 55)
      LocalObject(228, Door.Constructor(Vector3(2839.579f, 5334.859f, 176.6301f)), owning_building_guid = 55)
      LocalObject(229, Door.Constructor(Vector3(2839.579f, 5334.859f, 196.6291f)), owning_building_guid = 55)
      LocalObject(1961, Door.Constructor(Vector3(2834.104f, 5316.43f, 166.4451f)), owning_building_guid = 55)
      LocalObject(1962, Door.Constructor(Vector3(2838.074f, 5332.353f, 166.4451f)), owning_building_guid = 55)
      LocalObject(
        700,
        IFFLock.Constructor(Vector3(2837.498f, 5318.052f, 176.5701f), Vector3(0, 0, 194)),
        owning_building_guid = 55,
        door_guid = 226
      )
      LocalObject(
        701,
        IFFLock.Constructor(Vector3(2837.498f, 5318.052f, 196.5701f), Vector3(0, 0, 194)),
        owning_building_guid = 55,
        door_guid = 227
      )
      LocalObject(
        702,
        IFFLock.Constructor(Vector3(2837.793f, 5336.141f, 176.5701f), Vector3(0, 0, 14)),
        owning_building_guid = 55,
        door_guid = 228
      )
      LocalObject(
        703,
        IFFLock.Constructor(Vector3(2837.793f, 5336.141f, 196.5701f), Vector3(0, 0, 14)),
        owning_building_guid = 55,
        door_guid = 229
      )
      LocalObject(881, Locker.Constructor(Vector3(2837.611f, 5311.607f, 165.1031f)), owning_building_guid = 55)
      LocalObject(882, Locker.Constructor(Vector3(2838.909f, 5311.284f, 165.1031f)), owning_building_guid = 55)
      LocalObject(883, Locker.Constructor(Vector3(2841.517f, 5310.634f, 165.1031f)), owning_building_guid = 55)
      LocalObject(884, Locker.Constructor(Vector3(2842.877f, 5310.295f, 165.1031f)), owning_building_guid = 55)
      LocalObject(885, Locker.Constructor(Vector3(2842.937f, 5332.821f, 165.1031f)), owning_building_guid = 55)
      LocalObject(886, Locker.Constructor(Vector3(2844.234f, 5332.498f, 165.1031f)), owning_building_guid = 55)
      LocalObject(887, Locker.Constructor(Vector3(2846.808f, 5331.856f, 165.1031f)), owning_building_guid = 55)
      LocalObject(888, Locker.Constructor(Vector3(2848.168f, 5331.517f, 165.1031f)), owning_building_guid = 55)
      LocalObject(
        1315,
        Terminal.Constructor(Vector3(2844.42f, 5315.234f, 166.4411f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1316,
        Terminal.Constructor(Vector3(2845.805f, 5320.788f, 166.4411f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1317,
        Terminal.Constructor(Vector3(2847.106f, 5326.009f, 166.4411f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1812,
        SpawnTube.Constructor(Vector3(2833.423f, 5315.516f, 164.5911f), respawn_tube_tower, Vector3(0, 0, 14)),
        owning_building_guid = 55
      )
      LocalObject(
        1813,
        SpawnTube.Constructor(Vector3(2837.392f, 5331.438f, 164.5911f), respawn_tube_tower, Vector3(0, 0, 14)),
        owning_building_guid = 55
      )
      LocalObject(
        1579,
        ProximityTerminal.Constructor(Vector3(2823.663f, 5325.146f, 202.6791f), pad_landing_tower_frame),
        owning_building_guid = 55
      )
      LocalObject(
        1580,
        Terminal.Constructor(Vector3(2823.663f, 5325.146f, 202.6791f), air_rearm_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1582,
        ProximityTerminal.Constructor(Vector3(2826.19f, 5335.281f, 202.6791f), pad_landing_tower_frame),
        owning_building_guid = 55
      )
      LocalObject(
        1583,
        Terminal.Constructor(Vector3(2826.19f, 5335.281f, 202.6791f), air_rearm_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1203,
        FacilityTurret.Constructor(Vector3(2807.896f, 5319.101f, 194.0511f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1203, 5071)
      LocalObject(
        1204,
        FacilityTurret.Constructor(Vector3(2852.417f, 5338.828f, 194.0511f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1204, 5072)
      LocalObject(
        1672,
        Painbox.Constructor(Vector3(2828.592f, 5321.984f, 167.1286f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        1673,
        Painbox.Constructor(Vector3(2839.89f, 5315.756f, 165.2091f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        1674,
        Painbox.Constructor(Vector3(2843.094f, 5327.822f, 165.2091f), painbox_radius_continuous),
        owning_building_guid = 55
      )
    }

    Building23()

    def Building23(): Unit = { // Name: NW_Mont_Tower Type: tower_c GUID: 56, MapID: 23
      LocalBuilding(
        "NW_Mont_Tower",
        56,
        23,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3190f, 4412f, 105.591f), Vector3(0f, 0f, 323f), tower_c)
        )
      )
      LocalObject(
        1882,
        CaptureTerminal.Constructor(Vector3(3203.185f, 4401.936f, 115.59f), secondary_capture),
        owning_building_guid = 56
      )
      LocalObject(264, Door.Constructor(Vector3(3194.769f, 4398.389f, 107.112f)), owning_building_guid = 56)
      LocalObject(265, Door.Constructor(Vector3(3194.769f, 4398.389f, 127.111f)), owning_building_guid = 56)
      LocalObject(266, Door.Constructor(Vector3(3204.398f, 4411.167f, 107.112f)), owning_building_guid = 56)
      LocalObject(267, Door.Constructor(Vector3(3204.398f, 4411.167f, 127.111f)), owning_building_guid = 56)
      LocalObject(1972, Door.Constructor(Vector3(3192.158f, 4396.343f, 96.92699f)), owning_building_guid = 56)
      LocalObject(1973, Door.Constructor(Vector3(3202.033f, 4409.448f, 96.92699f)), owning_building_guid = 56)
      LocalObject(
        732,
        IFFLock.Constructor(Vector3(3195.916f, 4396.51f, 107.052f), Vector3(0, 0, 217)),
        owning_building_guid = 56,
        door_guid = 264
      )
      LocalObject(
        733,
        IFFLock.Constructor(Vector3(3195.916f, 4396.51f, 127.052f), Vector3(0, 0, 217)),
        owning_building_guid = 56,
        door_guid = 265
      )
      LocalObject(
        734,
        IFFLock.Constructor(Vector3(3203.255f, 4413.044f, 107.052f), Vector3(0, 0, 37)),
        owning_building_guid = 56,
        door_guid = 266
      )
      LocalObject(
        735,
        IFFLock.Constructor(Vector3(3203.255f, 4413.044f, 127.052f), Vector3(0, 0, 37)),
        owning_building_guid = 56,
        door_guid = 267
      )
      LocalObject(925, Locker.Constructor(Vector3(3193.502f, 4390.533f, 95.58499f)), owning_building_guid = 56)
      LocalObject(926, Locker.Constructor(Vector3(3194.57f, 4389.728f, 95.58499f)), owning_building_guid = 56)
      LocalObject(927, Locker.Constructor(Vector3(3196.716f, 4388.11f, 95.58499f)), owning_building_guid = 56)
      LocalObject(928, Locker.Constructor(Vector3(3197.836f, 4387.267f, 95.58499f)), owning_building_guid = 56)
      LocalObject(929, Locker.Constructor(Vector3(3206.693f, 4407.979f, 95.58499f)), owning_building_guid = 56)
      LocalObject(930, Locker.Constructor(Vector3(3207.76f, 4407.175f, 95.58499f)), owning_building_guid = 56)
      LocalObject(931, Locker.Constructor(Vector3(3209.879f, 4405.578f, 95.58499f)), owning_building_guid = 56)
      LocalObject(932, Locker.Constructor(Vector3(3210.999f, 4404.734f, 95.58499f)), owning_building_guid = 56)
      LocalObject(
        1335,
        Terminal.Constructor(Vector3(3201.186f, 4391.211f, 96.92299f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1336,
        Terminal.Constructor(Vector3(3204.631f, 4395.782f, 96.92299f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1337,
        Terminal.Constructor(Vector3(3207.869f, 4400.08f, 96.92299f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1823,
        SpawnTube.Constructor(Vector3(3191.173f, 4395.767f, 95.07299f), respawn_tube_tower, Vector3(0, 0, 37)),
        owning_building_guid = 56
      )
      LocalObject(
        1824,
        SpawnTube.Constructor(Vector3(3201.049f, 4408.873f, 95.07299f), respawn_tube_tower, Vector3(0, 0, 37)),
        owning_building_guid = 56
      )
      LocalObject(
        1585,
        ProximityTerminal.Constructor(Vector3(3185.953f, 4408.445f, 133.161f), pad_landing_tower_frame),
        owning_building_guid = 56
      )
      LocalObject(
        1586,
        Terminal.Constructor(Vector3(3185.953f, 4408.445f, 133.161f), air_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1588,
        ProximityTerminal.Constructor(Vector3(3192.239f, 4416.787f, 133.161f), pad_landing_tower_frame),
        owning_building_guid = 56
      )
      LocalObject(
        1589,
        Terminal.Constructor(Vector3(3192.239f, 4416.787f, 133.161f), air_rearm_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1217,
        FacilityTurret.Constructor(Vector3(3169.076f, 4409.042f, 124.533f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1217, 5073)
      LocalObject(
        1218,
        FacilityTurret.Constructor(Vector3(3217.767f, 4409.804f, 124.533f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1218, 5074)
      LocalObject(
        1684,
        Painbox.Constructor(Vector3(3189.254f, 4403.608f, 97.61049f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        1685,
        Painbox.Constructor(Vector3(3197.22f, 4393.461f, 95.69099f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        1686,
        Painbox.Constructor(Vector3(3204.884f, 4403.316f, 95.69099f), painbox_radius_continuous),
        owning_building_guid = 56
      )
    }

    Building42()

    def Building42(): Unit = { // Name: E_Amerish_Warpgate_Tower Type: tower_c GUID: 57, MapID: 42
      LocalBuilding(
        "E_Amerish_Warpgate_Tower",
        57,
        42,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4058f, 1262f, 68.20887f), Vector3(0f, 0f, 28f), tower_c)
        )
      )
      LocalObject(
        1886,
        CaptureTerminal.Constructor(Vector3(4072.694f, 1269.696f, 78.20787f), secondary_capture),
        owning_building_guid = 57
      )
      LocalObject(324, Door.Constructor(Vector3(4064.84f, 1274.697f, 69.72987f)), owning_building_guid = 57)
      LocalObject(325, Door.Constructor(Vector3(4064.84f, 1274.697f, 89.72887f)), owning_building_guid = 57)
      LocalObject(326, Door.Constructor(Vector3(4072.351f, 1260.57f, 69.72987f)), owning_building_guid = 57)
      LocalObject(327, Door.Constructor(Vector3(4072.351f, 1260.57f, 89.72887f)), owning_building_guid = 57)
      LocalObject(1989, Door.Constructor(Vector3(4065.398f, 1271.828f, 59.54487f)), owning_building_guid = 57)
      LocalObject(1990, Door.Constructor(Vector3(4073.102f, 1257.338f, 59.54487f)), owning_building_guid = 57)
      LocalObject(
        777,
        IFFLock.Constructor(Vector3(4062.655f, 1274.454f, 69.66987f), Vector3(0, 0, 332)),
        owning_building_guid = 57,
        door_guid = 324
      )
      LocalObject(
        778,
        IFFLock.Constructor(Vector3(4062.655f, 1274.454f, 89.66987f), Vector3(0, 0, 332)),
        owning_building_guid = 57,
        door_guid = 325
      )
      LocalObject(
        779,
        IFFLock.Constructor(Vector3(4074.539f, 1260.815f, 69.66987f), Vector3(0, 0, 152)),
        owning_building_guid = 57,
        door_guid = 326
      )
      LocalObject(
        780,
        IFFLock.Constructor(Vector3(4074.539f, 1260.815f, 89.66987f), Vector3(0, 0, 152)),
        owning_building_guid = 57,
        door_guid = 327
      )
      LocalObject(1011, Locker.Constructor(Vector3(4068.698f, 1275.43f, 58.20287f)), owning_building_guid = 57)
      LocalObject(1012, Locker.Constructor(Vector3(4069.879f, 1276.057f, 58.20287f)), owning_building_guid = 57)
      LocalObject(1013, Locker.Constructor(Vector3(4072.221f, 1277.303f, 58.20287f)), owning_building_guid = 57)
      LocalObject(1014, Locker.Constructor(Vector3(4073.459f, 1277.961f, 58.20287f)), owning_building_guid = 57)
      LocalObject(1015, Locker.Constructor(Vector3(4078.936f, 1256.101f, 58.20287f)), owning_building_guid = 57)
      LocalObject(1016, Locker.Constructor(Vector3(4080.116f, 1256.729f, 58.20287f)), owning_building_guid = 57)
      LocalObject(1017, Locker.Constructor(Vector3(4082.49f, 1257.991f, 58.20287f)), owning_building_guid = 57)
      LocalObject(1018, Locker.Constructor(Vector3(4083.728f, 1258.649f, 58.20287f)), owning_building_guid = 57)
      LocalObject(
        1361,
        Terminal.Constructor(Vector3(4076.355f, 1273.157f, 59.54087f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1362,
        Terminal.Constructor(Vector3(4078.882f, 1268.406f, 59.54087f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1363,
        Terminal.Constructor(Vector3(4081.569f, 1263.352f, 59.54087f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1840,
        SpawnTube.Constructor(Vector3(4065.504f, 1270.692f, 57.69087f), respawn_tube_tower, Vector3(0, 0, 332)),
        owning_building_guid = 57
      )
      LocalObject(
        1841,
        SpawnTube.Constructor(Vector3(4073.208f, 1256.203f, 57.69087f), respawn_tube_tower, Vector3(0, 0, 332)),
        owning_building_guid = 57
      )
      LocalObject(
        1591,
        ProximityTerminal.Constructor(Vector3(4054.608f, 1266.052f, 95.77887f), pad_landing_tower_frame),
        owning_building_guid = 57
      )
      LocalObject(
        1592,
        Terminal.Constructor(Vector3(4054.608f, 1266.052f, 95.77887f), air_rearm_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1594,
        ProximityTerminal.Constructor(Vector3(4059.511f, 1256.829f, 95.77887f), pad_landing_tower_frame),
        owning_building_guid = 57
      )
      LocalObject(
        1595,
        Terminal.Constructor(Vector3(4059.511f, 1256.829f, 95.77887f), air_rearm_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1239,
        FacilityTurret.Constructor(Vector3(4051.839f, 1241.786f, 87.15087f), manned_turret),
        owning_building_guid = 57
      )
      TurretToWeapon(1239, 5075)
      LocalObject(
        1240,
        FacilityTurret.Constructor(Vector3(4071.725f, 1286.237f, 87.15087f), manned_turret),
        owning_building_guid = 57
      )
      TurretToWeapon(1240, 5076)
      LocalObject(
        1696,
        Painbox.Constructor(Vector3(4065.29f, 1257.777f, 60.22837f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        1697,
        Painbox.Constructor(Vector3(4072.16f, 1271.819f, 58.30887f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        1698,
        Painbox.Constructor(Vector3(4077.853f, 1260.709f, 58.30887f), painbox_radius_continuous),
        owning_building_guid = 57
      )
    }

    Building19()

    def Building19(): Unit = { // Name: NW_Seth_Tower Type: tower_c GUID: 58, MapID: 19
      LocalBuilding(
        "NW_Seth_Tower",
        58,
        19,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4148f, 6224f, 93.21268f), Vector3(0f, 0f, 31f), tower_c)
        )
      )
      LocalObject(
        1887,
        CaptureTerminal.Constructor(Vector3(4162.271f, 6232.455f, 103.2117f), secondary_capture),
        owning_building_guid = 58
      )
      LocalObject(328, Door.Constructor(Vector3(4154.166f, 6237.038f, 94.73369f)), owning_building_guid = 58)
      LocalObject(329, Door.Constructor(Vector3(4154.166f, 6237.038f, 114.7327f)), owning_building_guid = 58)
      LocalObject(330, Door.Constructor(Vector3(4162.406f, 6223.323f, 94.73369f)), owning_building_guid = 58)
      LocalObject(331, Door.Constructor(Vector3(4162.406f, 6223.323f, 114.7327f)), owning_building_guid = 58)
      LocalObject(1991, Door.Constructor(Vector3(4154.874f, 6234.201f, 84.54868f)), owning_building_guid = 58)
      LocalObject(1992, Door.Constructor(Vector3(4163.326f, 6220.135f, 84.54868f)), owning_building_guid = 58)
      LocalObject(
        781,
        IFFLock.Constructor(Vector3(4151.997f, 6236.681f, 94.67368f), Vector3(0, 0, 329)),
        owning_building_guid = 58,
        door_guid = 328
      )
      LocalObject(
        782,
        IFFLock.Constructor(Vector3(4151.997f, 6236.681f, 114.6737f), Vector3(0, 0, 329)),
        owning_building_guid = 58,
        door_guid = 329
      )
      LocalObject(
        783,
        IFFLock.Constructor(Vector3(4164.579f, 6223.682f, 94.67368f), Vector3(0, 0, 149)),
        owning_building_guid = 58,
        door_guid = 330
      )
      LocalObject(
        784,
        IFFLock.Constructor(Vector3(4164.579f, 6223.682f, 114.6737f), Vector3(0, 0, 149)),
        owning_building_guid = 58,
        door_guid = 331
      )
      LocalObject(1019, Locker.Constructor(Vector3(4157.981f, 6237.971f, 83.20669f)), owning_building_guid = 58)
      LocalObject(1020, Locker.Constructor(Vector3(4159.127f, 6238.66f, 83.20669f)), owning_building_guid = 58)
      LocalObject(1021, Locker.Constructor(Vector3(4161.401f, 6240.026f, 83.20669f)), owning_building_guid = 58)
      LocalObject(1022, Locker.Constructor(Vector3(4162.603f, 6240.748f, 83.20669f)), owning_building_guid = 58)
      LocalObject(1023, Locker.Constructor(Vector3(4169.216f, 6219.205f, 83.20669f)), owning_building_guid = 58)
      LocalObject(1024, Locker.Constructor(Vector3(4170.362f, 6219.894f, 83.20669f)), owning_building_guid = 58)
      LocalObject(1025, Locker.Constructor(Vector3(4172.666f, 6221.278f, 83.20669f)), owning_building_guid = 58)
      LocalObject(1026, Locker.Constructor(Vector3(4173.868f, 6222f, 83.20669f)), owning_building_guid = 58)
      LocalObject(
        1364,
        Terminal.Constructor(Vector3(4165.747f, 6236.103f, 84.54469f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1365,
        Terminal.Constructor(Vector3(4168.518f, 6231.49f, 84.54469f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1366,
        Terminal.Constructor(Vector3(4171.466f, 6226.584f, 84.54469f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1842,
        SpawnTube.Constructor(Vector3(4155.039f, 6233.073f, 82.69469f), respawn_tube_tower, Vector3(0, 0, 329)),
        owning_building_guid = 58
      )
      LocalObject(
        1843,
        SpawnTube.Constructor(Vector3(4163.49f, 6219.007f, 82.69469f), respawn_tube_tower, Vector3(0, 0, 329)),
        owning_building_guid = 58
      )
      LocalObject(
        1597,
        ProximityTerminal.Constructor(Vector3(4144.4f, 6227.869f, 120.7827f), pad_landing_tower_frame),
        owning_building_guid = 58
      )
      LocalObject(
        1598,
        Terminal.Constructor(Vector3(4144.4f, 6227.869f, 120.7827f), air_rearm_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1600,
        ProximityTerminal.Constructor(Vector3(4149.78f, 6218.916f, 120.7827f), pad_landing_tower_frame),
        owning_building_guid = 58
      )
      LocalObject(
        1601,
        Terminal.Constructor(Vector3(4149.78f, 6218.916f, 120.7827f), air_rearm_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1241,
        FacilityTurret.Constructor(Vector3(4142.905f, 6203.492f, 112.1547f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1241, 5077)
      LocalObject(
        1242,
        FacilityTurret.Constructor(Vector3(4160.438f, 6248.922f, 112.1547f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1242, 5078)
      LocalObject(
        1699,
        Painbox.Constructor(Vector3(4155.501f, 6220.165f, 85.23219f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        1700,
        Painbox.Constructor(Vector3(4161.627f, 6234.547f, 83.31268f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        1701,
        Painbox.Constructor(Vector3(4167.894f, 6223.75f, 83.31268f), painbox_radius_continuous),
        owning_building_guid = 58
      )
    }

    Building16()

    def Building16(): Unit = { // Name: E_Amun_Tower Type: tower_c GUID: 59, MapID: 16
      LocalBuilding(
        "E_Amun_Tower",
        59,
        16,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5080f, 1796f, 76.71438f), Vector3(0f, 0f, 35f), tower_c)
        )
      )
      LocalObject(
        1893,
        CaptureTerminal.Constructor(Vector3(5093.646f, 1805.43f, 86.71338f), secondary_capture),
        owning_building_guid = 59
      )
      LocalObject(416, Door.Constructor(Vector3(5085.241f, 1809.436f, 78.23538f)), owning_building_guid = 59)
      LocalObject(417, Door.Constructor(Vector3(5085.241f, 1809.436f, 98.23438f)), owning_building_guid = 59)
      LocalObject(418, Door.Constructor(Vector3(5094.418f, 1796.33f, 78.23538f)), owning_building_guid = 59)
      LocalObject(419, Door.Constructor(Vector3(5094.418f, 1796.33f, 98.23438f)), owning_building_guid = 59)
      LocalObject(2015, Door.Constructor(Vector3(5086.146f, 1806.656f, 68.05038f)), owning_building_guid = 59)
      LocalObject(2016, Door.Constructor(Vector3(5095.558f, 1793.214f, 68.05038f)), owning_building_guid = 59)
      LocalObject(
        846,
        IFFLock.Constructor(Vector3(5083.103f, 1808.929f, 78.17538f), Vector3(0, 0, 325)),
        owning_building_guid = 59,
        door_guid = 416
      )
      LocalObject(
        847,
        IFFLock.Constructor(Vector3(5083.103f, 1808.929f, 98.17538f), Vector3(0, 0, 325)),
        owning_building_guid = 59,
        door_guid = 417
      )
      LocalObject(
        848,
        IFFLock.Constructor(Vector3(5096.561f, 1796.839f, 78.17538f), Vector3(0, 0, 145)),
        owning_building_guid = 59,
        door_guid = 418
      )
      LocalObject(
        849,
        IFFLock.Constructor(Vector3(5096.561f, 1796.839f, 98.17538f), Vector3(0, 0, 145)),
        owning_building_guid = 59,
        door_guid = 419
      )
      LocalObject(1124, Locker.Constructor(Vector3(5088.982f, 1810.633f, 66.70838f)), owning_building_guid = 59)
      LocalObject(1125, Locker.Constructor(Vector3(5090.077f, 1811.4f, 66.70838f)), owning_building_guid = 59)
      LocalObject(1126, Locker.Constructor(Vector3(5092.25f, 1812.922f, 66.70838f)), owning_building_guid = 59)
      LocalObject(1127, Locker.Constructor(Vector3(5093.399f, 1813.726f, 66.70838f)), owning_building_guid = 59)
      LocalObject(1128, Locker.Constructor(Vector3(5101.499f, 1792.697f, 66.70838f)), owning_building_guid = 59)
      LocalObject(1129, Locker.Constructor(Vector3(5102.594f, 1793.464f, 66.70838f)), owning_building_guid = 59)
      LocalObject(1130, Locker.Constructor(Vector3(5104.796f, 1795.005f, 66.70838f)), owning_building_guid = 59)
      LocalObject(1131, Locker.Constructor(Vector3(5105.944f, 1795.81f, 66.70838f)), owning_building_guid = 59)
      LocalObject(
        1401,
        Terminal.Constructor(Vector3(5096.859f, 1809.311f, 68.04638f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1402,
        Terminal.Constructor(Vector3(5099.945f, 1804.903f, 68.04638f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1403,
        Terminal.Constructor(Vector3(5103.229f, 1800.214f, 68.04638f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1866,
        SpawnTube.Constructor(Vector3(5086.388f, 1805.542f, 66.19638f), respawn_tube_tower, Vector3(0, 0, 325)),
        owning_building_guid = 59
      )
      LocalObject(
        1867,
        SpawnTube.Constructor(Vector3(5095.801f, 1792.099f, 66.19638f), respawn_tube_tower, Vector3(0, 0, 325)),
        owning_building_guid = 59
      )
      LocalObject(
        1603,
        ProximityTerminal.Constructor(Vector3(5076.139f, 1799.608f, 104.2844f), pad_landing_tower_frame),
        owning_building_guid = 59
      )
      LocalObject(
        1604,
        Terminal.Constructor(Vector3(5076.139f, 1799.608f, 104.2844f), air_rearm_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1606,
        ProximityTerminal.Constructor(Vector3(5082.13f, 1791.052f, 104.2844f), pad_landing_tower_frame),
        owning_building_guid = 59
      )
      LocalObject(
        1607,
        Terminal.Constructor(Vector3(5082.13f, 1791.052f, 104.2844f), air_rearm_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1270,
        FacilityTurret.Constructor(Vector3(5076.348f, 1775.186f, 95.65638f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1270, 5079)
      LocalObject(
        1271,
        FacilityTurret.Constructor(Vector3(5090.668f, 1821.729f, 95.65638f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1271, 5080)
      LocalObject(
        1717,
        Painbox.Constructor(Vector3(5087.75f, 1792.697f, 68.73388f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        1718,
        Painbox.Constructor(Vector3(5092.858f, 1807.472f, 66.81438f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        1719,
        Painbox.Constructor(Vector3(5099.862f, 1797.138f, 66.81438f), painbox_radius_continuous),
        owning_building_guid = 59
      )
    }

    Building40()

    def Building40(): Unit = { // Name: N_Bastet_Tower Type: tower_c GUID: 60, MapID: 40
      LocalBuilding(
        "N_Bastet_Tower",
        60,
        40,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5514f, 5964f, 52.92538f), Vector3(0f, 0f, 353f), tower_c)
        )
      )
      LocalObject(
        1894,
        CaptureTerminal.Constructor(Vector3(5530.451f, 5961.876f, 62.92439f), secondary_capture),
        owning_building_guid = 60
      )
      LocalObject(437, Door.Constructor(Vector3(5524.936f, 5954.597f, 54.44638f)), owning_building_guid = 60)
      LocalObject(438, Door.Constructor(Vector3(5524.936f, 5954.597f, 74.44539f)), owning_building_guid = 60)
      LocalObject(439, Door.Constructor(Vector3(5526.886f, 5970.478f, 54.44638f)), owning_building_guid = 60)
      LocalObject(440, Door.Constructor(Vector3(5526.886f, 5970.478f, 74.44539f)), owning_building_guid = 60)
      LocalObject(2020, Door.Constructor(Vector3(5523.697f, 5951.519f, 44.26138f)), owning_building_guid = 60)
      LocalObject(2021, Door.Constructor(Vector3(5525.697f, 5967.807f, 44.26138f)), owning_building_guid = 60)
      LocalObject(
        861,
        IFFLock.Constructor(Vector3(5524.957f, 5971.532f, 54.38638f), Vector3(0, 0, 7)),
        owning_building_guid = 60,
        door_guid = 439
      )
      LocalObject(
        862,
        IFFLock.Constructor(Vector3(5524.957f, 5971.532f, 74.38638f), Vector3(0, 0, 7)),
        owning_building_guid = 60,
        door_guid = 440
      )
      LocalObject(
        863,
        IFFLock.Constructor(Vector3(5526.869f, 5953.543f, 54.38638f), Vector3(0, 0, 187)),
        owning_building_guid = 60,
        door_guid = 437
      )
      LocalObject(
        864,
        IFFLock.Constructor(Vector3(5526.869f, 5953.543f, 74.38638f), Vector3(0, 0, 187)),
        owning_building_guid = 60,
        door_guid = 438
      )
      LocalObject(1144, Locker.Constructor(Vector3(5527.766f, 5947.16f, 42.91938f)), owning_building_guid = 60)
      LocalObject(1145, Locker.Constructor(Vector3(5529.093f, 5946.997f, 42.91938f)), owning_building_guid = 60)
      LocalObject(1146, Locker.Constructor(Vector3(5530.467f, 5968.864f, 42.91938f)), owning_building_guid = 60)
      LocalObject(1148, Locker.Constructor(Vector3(5531.761f, 5946.669f, 42.91938f)), owning_building_guid = 60)
      LocalObject(1149, Locker.Constructor(Vector3(5531.793f, 5968.702f, 42.91938f)), owning_building_guid = 60)
      LocalObject(1151, Locker.Constructor(Vector3(5533.153f, 5946.499f, 42.91938f)), owning_building_guid = 60)
      LocalObject(1153, Locker.Constructor(Vector3(5534.427f, 5968.378f, 42.91938f)), owning_building_guid = 60)
      LocalObject(1155, Locker.Constructor(Vector3(5535.818f, 5968.208f, 42.91938f)), owning_building_guid = 60)
      LocalObject(
        1410,
        Terminal.Constructor(Vector3(5534.082f, 5951.589f, 44.25739f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1411,
        Terminal.Constructor(Vector3(5534.78f, 5957.271f, 44.25739f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1412,
        Terminal.Constructor(Vector3(5535.436f, 5962.611f, 44.25739f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1871,
        SpawnTube.Constructor(Vector3(5523.132f, 5950.529f, 42.40739f), respawn_tube_tower, Vector3(0, 0, 7)),
        owning_building_guid = 60
      )
      LocalObject(
        1872,
        SpawnTube.Constructor(Vector3(5525.132f, 5966.816f, 42.40739f), respawn_tube_tower, Vector3(0, 0, 7)),
        owning_building_guid = 60
      )
      LocalObject(
        1609,
        ProximityTerminal.Constructor(Vector3(5512.272f, 5958.897f, 80.49538f), pad_landing_tower_frame),
        owning_building_guid = 60
      )
      LocalObject(
        1610,
        Terminal.Constructor(Vector3(5512.272f, 5958.897f, 80.49538f), air_rearm_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1612,
        ProximityTerminal.Constructor(Vector3(5513.545f, 5969.265f, 80.49538f), pad_landing_tower_frame),
        owning_building_guid = 60
      )
      LocalObject(
        1613,
        Terminal.Constructor(Vector3(5513.545f, 5969.265f, 80.49538f), air_rearm_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1278,
        FacilityTurret.Constructor(Vector3(5497.359f, 5950.976f, 71.86739f), manned_turret),
        owning_building_guid = 60
      )
      TurretToWeapon(1278, 5081)
      LocalObject(
        1280,
        FacilityTurret.Constructor(Vector3(5539.145f, 5975.982f, 71.86739f), manned_turret),
        owning_building_guid = 60
      )
      TurretToWeapon(1280, 5082)
      LocalObject(
        1720,
        Painbox.Constructor(Vector3(5517.549f, 5956.359f, 44.94489f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        1722,
        Painbox.Constructor(Vector3(5529.522f, 5951.555f, 43.02538f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        1723,
        Painbox.Constructor(Vector3(5531.231f, 5963.921f, 43.02538f), painbox_radius_continuous),
        owning_building_guid = 60
      )
    }

    Building1()

    def Building1(): Unit = { // Name: WG_Solsar_to_Forseral Type: warpgate GUID: 61, MapID: 1
      LocalBuilding(
        "WG_Solsar_to_Forseral",
        61,
        1,
        FoundationBuilder(WarpGate.Structure(Vector3(2506f, 5278f, 176.0704f)))
      )
    }

    Building3()

    def Building3(): Unit = { // Name: WG_Solsar_to_Amerish Type: warpgate GUID: 62, MapID: 3
      LocalBuilding(
        "WG_Solsar_to_Amerish",
        62,
        3,
        FoundationBuilder(WarpGate.Structure(Vector3(2918f, 1122f, 66.00238f)))
      )
    }

    Building4()

    def Building4(): Unit = { // Name: WG_Solsar_to_Hossin Type: warpgate GUID: 63, MapID: 4
      LocalBuilding(
        "WG_Solsar_to_Hossin",
        63,
        4,
        FoundationBuilder(WarpGate.Structure(Vector3(4132f, 7474f, 82.79366f)))
      )
    }

    Building2()

    def Building2(): Unit = { // Name: WG_Solsar_to_Cyssor Type: warpgate GUID: 64, MapID: 2
      LocalBuilding(
        "WG_Solsar_to_Cyssor",
        64,
        2,
        FoundationBuilder(WarpGate.Structure(Vector3(5872f, 3436f, 129.1208f)))
      )
    }

    def Lattice(): Unit = {
      LatticeLink("Seth", "Bastet")
      LatticeLink("Horus", "Amun")
      LatticeLink("Thoth", "Hapi")
      LatticeLink("Thoth", "Mont")
      LatticeLink("Thoth", "Sobek")
      LatticeLink("Thoth", "Amun")
      LatticeLink("Seth", "WG_Solsar_to_Hossin")
      LatticeLink("Aton", "WG_Solsar_to_Forseral")
      LatticeLink("Horus", "WG_Solsar_to_Amerish")
      LatticeLink("Bastet", "WG_Solsar_to_Cyssor")
      LatticeLink("Bastet", "GW_Solsar_N")
      LatticeLink("Seth", "Aton")
      LatticeLink("Sobek", "GW_Solsar_S")
      LatticeLink("Bastet", "Amun")
      LatticeLink("Bastet", "Hapi")
      LatticeLink("Seth", "Hapi")
      LatticeLink("Aton", "Mont")
      LatticeLink("Mont", "Hapi")
      LatticeLink("Mont", "Sobek")
      LatticeLink("Sobek", "Horus")
      LatticeLink("Sobek", "Amun")
    }

    Lattice()

  }
}
