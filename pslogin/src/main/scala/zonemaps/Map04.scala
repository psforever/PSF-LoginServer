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

object Map04 { // Ishundar
  val ZoneMap = new ZoneMap("map04") {
    Checksum = 2455050867L

    Building8()

    def Building8(): Unit = { // Name: Enkidu Type: amp_station GUID: 1, MapID: 8
      LocalBuilding(
        "Enkidu",
        1,
        8,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3318f, 3574f, 37.19054f),
            Vector3(0f, 0f, 134f),
            amp_station
          )
        )
      )
      LocalObject(
        222,
        CaptureTerminal.Constructor(Vector3(3320.315f, 3571.598f, 48.69854f), capture_terminal),
        owning_building_guid = 1
      )
      LocalObject(180, Door.Constructor(Vector3(3312.956f, 3569.427f, 50.09254f)), owning_building_guid = 1)
      LocalObject(181, Door.Constructor(Vector3(3322.749f, 3578.879f, 50.09254f)), owning_building_guid = 1)
      LocalObject(437, Door.Constructor(Vector3(3255.375f, 3506.143f, 46.90554f)), owning_building_guid = 1)
      LocalObject(442, Door.Constructor(Vector3(3268.013f, 3493.056f, 38.94154f)), owning_building_guid = 1)
      LocalObject(443, Door.Constructor(Vector3(3292.463f, 3549.339f, 43.91154f)), owning_building_guid = 1)
      LocalObject(444, Door.Constructor(Vector3(3292.705f, 3556.178f, 48.91755f)), owning_building_guid = 1)
      LocalObject(445, Door.Constructor(Vector3(3299f, 3549.66f, 48.91755f)), owning_building_guid = 1)
      LocalObject(446, Door.Constructor(Vector3(3308.191f, 3648.219f, 38.94154f)), owning_building_guid = 1)
      LocalObject(447, Door.Constructor(Vector3(3313.247f, 3463.758f, 38.91154f)), owning_building_guid = 1)
      LocalObject(448, Door.Constructor(Vector3(3319.254f, 3480.712f, 46.90454f)), owning_building_guid = 1)
      LocalObject(449, Door.Constructor(Vector3(3321.277f, 3660.857f, 46.90554f)), owning_building_guid = 1)
      LocalObject(450, Door.Constructor(Vector3(3332.341f, 3493.35f, 38.94154f)), owning_building_guid = 1)
      LocalObject(451, Door.Constructor(Vector3(3336.708f, 3598.642f, 48.91755f)), owning_building_guid = 1)
      LocalObject(452, Door.Constructor(Vector3(3343.003f, 3592.125f, 48.91755f)), owning_building_guid = 1)
      LocalObject(453, Door.Constructor(Vector3(3343.537f, 3598.66f, 43.91154f)), owning_building_guid = 1)
      LocalObject(454, Door.Constructor(Vector3(3353.786f, 3658.18f, 38.94154f)), owning_building_guid = 1)
      LocalObject(455, Door.Constructor(Vector3(3366.423f, 3645.093f, 46.90554f)), owning_building_guid = 1)
      LocalObject(456, Door.Constructor(Vector3(3392.057f, 3551.017f, 46.90454f)), owning_building_guid = 1)
      LocalObject(457, Door.Constructor(Vector3(3405.144f, 3563.655f, 38.94154f)), owning_building_guid = 1)
      LocalObject(741, Door.Constructor(Vector3(3298.74f, 3555.696f, 48.91154f)), owning_building_guid = 1)
      LocalObject(742, Door.Constructor(Vector3(3300.736f, 3557.328f, 43.91154f)), owning_building_guid = 1)
      LocalObject(743, Door.Constructor(Vector3(3301.229f, 3585.608f, 38.91154f)), owning_building_guid = 1)
      LocalObject(744, Door.Constructor(Vector3(3303.416f, 3548.795f, 38.91154f)), owning_building_guid = 1)
      LocalObject(745, Door.Constructor(Vector3(3306.984f, 3591.166f, 38.91154f)), owning_building_guid = 1)
      LocalObject(746, Door.Constructor(Vector3(3309.072f, 3548.696f, 38.91154f)), owning_building_guid = 1)
      LocalObject(747, Door.Constructor(Vector3(3309.17f, 3554.352f, 31.41154f)), owning_building_guid = 1)
      LocalObject(748, Door.Constructor(Vector3(3309.269f, 3560.008f, 23.91154f)), owning_building_guid = 1)
      LocalObject(749, Door.Constructor(Vector3(3315.123f, 3571.221f, 31.41154f)), owning_building_guid = 1)
      LocalObject(750, Door.Constructor(Vector3(3320.186f, 3537.187f, 31.41154f)), owning_building_guid = 1)
      LocalObject(751, Door.Constructor(Vector3(3320.581f, 3559.811f, 31.41154f)), owning_building_guid = 1)
      LocalObject(752, Door.Constructor(Vector3(3326.632f, 3582.336f, 23.91154f)), owning_building_guid = 1)
      LocalObject(753, Door.Constructor(Vector3(3335.264f, 3590.672f, 43.91154f)), owning_building_guid = 1)
      LocalObject(754, Door.Constructor(Vector3(3336.965f, 3592.609f, 48.91154f)), owning_building_guid = 1)
      LocalObject(755, Door.Constructor(Vector3(3337.845f, 3576.482f, 23.91154f)), owning_building_guid = 1)
      LocalObject(756, Door.Constructor(Vector3(3337.845f, 3576.482f, 31.41154f)), owning_building_guid = 1)
      LocalObject(757, Door.Constructor(Vector3(3343.6f, 3582.04f, 38.91154f)), owning_building_guid = 1)
      LocalObject(758, Door.Constructor(Vector3(3343.699f, 3587.696f, 38.91154f)), owning_building_guid = 1)
      LocalObject(759, Door.Constructor(Vector3(3347.874f, 3502.757f, 31.41154f)), owning_building_guid = 1)
      LocalObject(760, Door.Constructor(Vector3(3348.565f, 3542.349f, 23.91154f)), owning_building_guid = 1)
      LocalObject(761, Door.Constructor(Vector3(3348.861f, 3559.317f, 23.91154f)), owning_building_guid = 1)
      LocalObject(762, Door.Constructor(Vector3(3354.221f, 3542.25f, 23.91154f)), owning_building_guid = 1)
      LocalObject(922, Door.Constructor(Vector3(3297.715f, 3594.968f, 39.67054f)), owning_building_guid = 1)
      LocalObject(2927, Door.Constructor(Vector3(3328.322f, 3557.1f, 31.74454f)), owning_building_guid = 1)
      LocalObject(2928, Door.Constructor(Vector3(3333.565f, 3562.163f, 31.74454f)), owning_building_guid = 1)
      LocalObject(2929, Door.Constructor(Vector3(3338.811f, 3567.229f, 31.74454f)), owning_building_guid = 1)
      LocalObject(
        970,
        IFFLock.Constructor(Vector3(3293.625f, 3594.828f, 38.87054f), Vector3(0, 0, 316)),
        owning_building_guid = 1,
        door_guid = 922
      )
      LocalObject(
        1084,
        IFFLock.Constructor(Vector3(3290.676f, 3555.318f, 48.85154f), Vector3(0, 0, 316)),
        owning_building_guid = 1,
        door_guid = 444
      )
      LocalObject(
        1085,
        IFFLock.Constructor(Vector3(3293.293f, 3547.299f, 43.85254f), Vector3(0, 0, 226)),
        owning_building_guid = 1,
        door_guid = 443
      )
      LocalObject(
        1086,
        IFFLock.Constructor(Vector3(3301.043f, 3550.474f, 48.85154f), Vector3(0, 0, 136)),
        owning_building_guid = 1,
        door_guid = 445
      )
      LocalObject(
        1087,
        IFFLock.Constructor(Vector3(3314.073f, 3461.713f, 38.85054f), Vector3(0, 0, 226)),
        owning_building_guid = 1,
        door_guid = 447
      )
      LocalObject(
        1088,
        IFFLock.Constructor(Vector3(3320.996f, 3558.026f, 31.22654f), Vector3(0, 0, 226)),
        owning_building_guid = 1,
        door_guid = 751
      )
      LocalObject(
        1089,
        IFFLock.Constructor(Vector3(3328.416f, 3582.752f, 23.72654f), Vector3(0, 0, 136)),
        owning_building_guid = 1,
        door_guid = 752
      )
      LocalObject(
        1090,
        IFFLock.Constructor(Vector3(3334.657f, 3597.79f, 48.85154f), Vector3(0, 0, 316)),
        owning_building_guid = 1,
        door_guid = 451
      )
      LocalObject(
        1091,
        IFFLock.Constructor(Vector3(3337.336f, 3578.176f, 31.22654f), Vector3(0, 0, 46)),
        owning_building_guid = 1,
        door_guid = 756
      )
      LocalObject(
        1092,
        IFFLock.Constructor(Vector3(3342.694f, 3600.685f, 43.85254f), Vector3(0, 0, 46)),
        owning_building_guid = 1,
        door_guid = 453
      )
      LocalObject(
        1093,
        IFFLock.Constructor(Vector3(3345.022f, 3592.944f, 48.85154f), Vector3(0, 0, 136)),
        owning_building_guid = 1,
        door_guid = 452
      )
      LocalObject(
        1094,
        IFFLock.Constructor(Vector3(3352.528f, 3541.741f, 23.72654f), Vector3(0, 0, 316)),
        owning_building_guid = 1,
        door_guid = 762
      )
      LocalObject(1424, Locker.Constructor(Vector3(3303.334f, 3580.312f, 22.39054f)), owning_building_guid = 1)
      LocalObject(1425, Locker.Constructor(Vector3(3304.263f, 3579.35f, 22.39054f)), owning_building_guid = 1)
      LocalObject(1426, Locker.Constructor(Vector3(3305.191f, 3578.389f, 22.39054f)), owning_building_guid = 1)
      LocalObject(1427, Locker.Constructor(Vector3(3306.111f, 3577.437f, 22.39054f)), owning_building_guid = 1)
      LocalObject(1428, Locker.Constructor(Vector3(3309.265f, 3574.171f, 22.39054f)), owning_building_guid = 1)
      LocalObject(1429, Locker.Constructor(Vector3(3310.193f, 3573.209f, 22.39054f)), owning_building_guid = 1)
      LocalObject(1430, Locker.Constructor(Vector3(3311.122f, 3572.248f, 22.39054f)), owning_building_guid = 1)
      LocalObject(1431, Locker.Constructor(Vector3(3312.041f, 3571.296f, 22.39054f)), owning_building_guid = 1)
      LocalObject(1432, Locker.Constructor(Vector3(3331.427f, 3580.047f, 30.15154f)), owning_building_guid = 1)
      LocalObject(1433, Locker.Constructor(Vector3(3332.225f, 3579.221f, 30.15154f)), owning_building_guid = 1)
      LocalObject(1434, Locker.Constructor(Vector3(3333.021f, 3578.396f, 30.15154f)), owning_building_guid = 1)
      LocalObject(1435, Locker.Constructor(Vector3(3333.83f, 3577.558f, 30.15154f)), owning_building_guid = 1)
      LocalObject(
        1979,
        Terminal.Constructor(Vector3(3301.833f, 3558.385f, 38.71954f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1980,
        Terminal.Constructor(Vector3(3322.485f, 3570.899f, 31.48055f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1981,
        Terminal.Constructor(Vector3(3325.21f, 3573.531f, 31.48055f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1982,
        Terminal.Constructor(Vector3(3327.894f, 3576.123f, 31.48055f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1983,
        Terminal.Constructor(Vector3(3333.61f, 3557.841f, 38.71954f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1984,
        Terminal.Constructor(Vector3(3334.17f, 3589.612f, 38.71954f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2834,
        Terminal.Constructor(Vector3(3322.955f, 3512.138f, 31.44754f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2835,
        Terminal.Constructor(Vector3(3323.547f, 3546.073f, 23.94754f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2836,
        Terminal.Constructor(Vector3(3325.061f, 3573.167f, 43.91854f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2837,
        Terminal.Constructor(Vector3(3329.908f, 3559.045f, 32.02454f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2838,
        Terminal.Constructor(Vector3(3335.153f, 3564.105f, 32.02454f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2839,
        Terminal.Constructor(Vector3(3340.395f, 3569.173f, 32.02454f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3069,
        Terminal.Constructor(Vector3(3291.965f, 3482.235f, 39.29255f), vehicle_terminal_combined),
        owning_building_guid = 1
      )
      LocalObject(
        1885,
        VehicleSpawnPad.Constructor(Vector3(3301.713f, 3491.774f, 35.13554f), mb_pad_creation, Vector3(0, 0, 46)),
        owning_building_guid = 1,
        terminal_guid = 3069
      )
      LocalObject(2655, ResourceSilo.Constructor(Vector3(3342.93f, 3671.26f, 44.42554f)), owning_building_guid = 1)
      LocalObject(
        2703,
        SpawnTube.Constructor(Vector3(3329.385f, 3557.515f, 29.89054f), Vector3(0, 0, 226)),
        owning_building_guid = 1
      )
      LocalObject(
        2704,
        SpawnTube.Constructor(Vector3(3334.627f, 3562.577f, 29.89054f), Vector3(0, 0, 226)),
        owning_building_guid = 1
      )
      LocalObject(
        2705,
        SpawnTube.Constructor(Vector3(3339.872f, 3567.642f, 29.89054f), Vector3(0, 0, 226)),
        owning_building_guid = 1
      )
      LocalObject(
        1904,
        ProximityTerminal.Constructor(Vector3(3308.092f, 3576.17f, 22.39054f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1905,
        ProximityTerminal.Constructor(Vector3(3333.32f, 3558.144f, 42.39054f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2216,
        ProximityTerminal.Constructor(Vector3(3258.225f, 3553.25f, 47.85654f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2217,
        Terminal.Constructor(Vector3(3258.225f, 3553.25f, 47.85654f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2219,
        ProximityTerminal.Constructor(Vector3(3297.968f, 3623.295f, 45.69555f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2220,
        Terminal.Constructor(Vector3(3297.968f, 3623.295f, 45.69555f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2222,
        ProximityTerminal.Constructor(Vector3(3343.546f, 3519.085f, 45.69555f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2223,
        Terminal.Constructor(Vector3(3343.546f, 3519.085f, 45.69555f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2225,
        ProximityTerminal.Constructor(Vector3(3365.104f, 3631.023f, 45.71854f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2226,
        Terminal.Constructor(Vector3(3365.104f, 3631.023f, 45.71854f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2587,
        ProximityTerminal.Constructor(Vector3(3274.423f, 3618.559f, 36.59055f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2588,
        Terminal.Constructor(Vector3(3274.423f, 3618.559f, 36.59055f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2591,
        ProximityTerminal.Constructor(Vector3(3366.547f, 3523.436f, 36.59055f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2592,
        Terminal.Constructor(Vector3(3366.547f, 3523.436f, 36.59055f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1796,
        FacilityTurret.Constructor(Vector3(3191.991f, 3553.579f, 45.89854f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1796, 5000)
      LocalObject(
        1801,
        FacilityTurret.Constructor(Vector3(3236.043f, 3599.204f, 45.89854f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1801, 5001)
      LocalObject(
        1806,
        FacilityTurret.Constructor(Vector3(3298.591f, 3443.177f, 45.89854f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1806, 5002)
      LocalObject(
        1807,
        FacilityTurret.Constructor(Vector3(3337.071f, 3693.669f, 45.89854f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1807, 5003)
      LocalObject(
        1808,
        FacilityTurret.Constructor(Vector3(3403.855f, 3627.705f, 45.89854f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1808, 5004)
      LocalObject(
        1809,
        FacilityTurret.Constructor(Vector3(3443.646f, 3583.294f, 45.89854f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1809, 5005)
      LocalObject(
        2382,
        Painbox.Constructor(Vector3(3364.526f, 3535.729f, 27.26154f), painbox),
        owning_building_guid = 1
      )
      LocalObject(
        2394,
        Painbox.Constructor(Vector3(3327.627f, 3566.065f, 34.83894f), painbox_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2406,
        Painbox.Constructor(Vector3(3353.24f, 3544.354f, 23.84954f), painbox_door_radius),
        owning_building_guid = 1
      )
      LocalObject(
        2426,
        Painbox.Constructor(Vector3(3314.134f, 3572.307f, 33.06915f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2427,
        Painbox.Constructor(Vector3(3319.606f, 3558.972f, 31.15504f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2428,
        Painbox.Constructor(Vector3(3338.61f, 3577.221f, 31.09054f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(304, Generator.Constructor(Vector3(3365.044f, 3531.078f, 21.09654f)), owning_building_guid = 1)
      LocalObject(
        292,
        Terminal.Constructor(Vector3(3359.32f, 3536.938f, 22.39054f), gen_control),
        owning_building_guid = 1
      )
    }

    Building51()

    def Building51(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 4, MapID: 51
      LocalBuilding(
        "bunker_gauntlet",
        4,
        51,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(966f, 5404f, 72.35047f),
            Vector3(0f, 0f, 180f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(320, Door.Constructor(Vector3(941.077f, 5405.901f, 73.87148f)), owning_building_guid = 4)
      LocalObject(324, Door.Constructor(Vector3(990.898f, 5405.912f, 73.87148f)), owning_building_guid = 4)
    }

    Building54()

    def Building54(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 5, MapID: 54
      LocalBuilding(
        "bunker_gauntlet",
        5,
        54,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(2830f, 4464f, 39.29373f),
            Vector3(0f, 0f, 270f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(402, Door.Constructor(Vector3(2828.099f, 4439.077f, 40.81473f)), owning_building_guid = 5)
      LocalObject(403, Door.Constructor(Vector3(2828.088f, 4488.898f, 40.81473f)), owning_building_guid = 5)
    }

    Building50()

    def Building50(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 6, MapID: 50
      LocalBuilding(
        "bunker_gauntlet",
        6,
        50,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(3212f, 2186f, 70.5193f),
            Vector3(0f, 0f, 225f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(427, Door.Constructor(Vector3(3193.032f, 2169.721f, 72.04031f)), owning_building_guid = 6)
      LocalObject(432, Door.Constructor(Vector3(3228.254f, 2204.958f, 72.04031f)), owning_building_guid = 6)
    }

    Building52()

    def Building52(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 7, MapID: 52
      LocalBuilding(
        "bunker_gauntlet",
        7,
        52,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4288f, 5822f, 77.53156f),
            Vector3(0f, 0f, 134f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(513, Door.Constructor(Vector3(4272.055f, 5841.249f, 79.05257f)), owning_building_guid = 7)
      LocalObject(514, Door.Constructor(Vector3(4306.671f, 5805.418f, 79.05257f)), owning_building_guid = 7)
    }

    Building53()

    def Building53(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 8, MapID: 53
      LocalBuilding(
        "bunker_gauntlet",
        8,
        53,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(7108f, 5242f, 37.86362f),
            Vector3(0f, 0f, 225f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(648, Door.Constructor(Vector3(7089.033f, 5225.721f, 39.38462f)), owning_building_guid = 8)
      LocalObject(650, Door.Constructor(Vector3(7124.253f, 5260.958f, 39.38462f)), owning_building_guid = 8)
    }

    Building42()

    def Building42(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 9, MapID: 42
      LocalBuilding(
        "bunker_lg",
        9,
        42,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(1740f, 5472f, 34.41663f), Vector3(0f, 0f, 44f), bunker_lg)
        )
      )
      LocalObject(355, Door.Constructor(Vector3(1740.098f, 5475.649f, 35.93763f)), owning_building_guid = 9)
    }

    Building43()

    def Building43(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 10, MapID: 43
      LocalBuilding(
        "bunker_lg",
        10,
        43,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(2642f, 4548f, 44.8345f), Vector3(0f, 0f, 180f), bunker_lg)
        )
      )
      LocalObject(380, Door.Constructor(Vector3(2639.394f, 4545.443f, 46.3555f)), owning_building_guid = 10)
    }

    Building45()

    def Building45(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 11, MapID: 45
      LocalBuilding(
        "bunker_lg",
        11,
        45,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4034f, 2330f, 70.24219f), Vector3(0f, 0f, 45f), bunker_lg)
        )
      )
      LocalObject(494, Door.Constructor(Vector3(4034.035f, 2333.651f, 71.76319f)), owning_building_guid = 11)
    }

    Building48()

    def Building48(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 12, MapID: 48
      LocalBuilding(
        "bunker_lg",
        12,
        48,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4740f, 2346f, 86.66288f), Vector3(0f, 0f, 180f), bunker_lg)
        )
      )
      LocalObject(557, Door.Constructor(Vector3(4737.394f, 2343.443f, 88.18388f)), owning_building_guid = 12)
    }

    Building64()

    def Building64(): Unit = { // Name: Dagon_Tower Type: bunker_sm GUID: 13, MapID: 64
      LocalBuilding(
        "Dagon_Tower",
        13,
        64,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(1972f, 5528f, 39.54084f), Vector3(0f, 0f, 90f), bunker_sm)
        )
      )
      LocalObject(369, Door.Constructor(Vector3(1972.055f, 5529.225f, 41.06184f)), owning_building_guid = 13)
    }

    Building46()

    def Building46(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 14, MapID: 46
      LocalBuilding(
        "bunker_sm",
        14,
        46,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3472f, 5470f, 76.47643f), Vector3(0f, 0f, 360f), bunker_sm)
        )
      )
      LocalObject(464, Door.Constructor(Vector3(3473.225f, 5469.945f, 77.99744f)), owning_building_guid = 14)
    }

    Building44()

    def Building44(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 15, MapID: 44
      LocalBuilding(
        "bunker_sm",
        15,
        44,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3540f, 3566f, 28.99432f), Vector3(0f, 0f, 224f), bunker_sm)
        )
      )
      LocalObject(465, Door.Constructor(Vector3(3539.081f, 3565.189f, 30.51532f)), owning_building_guid = 15)
    }

    Building47()

    def Building47(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 16, MapID: 47
      LocalBuilding(
        "bunker_sm",
        16,
        47,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4932f, 5166f, 78.77888f), Vector3(0f, 0f, 135f), bunker_sm)
        )
      )
      LocalObject(585, Door.Constructor(Vector3(4931.173f, 5166.905f, 80.29988f)), owning_building_guid = 16)
    }

    Building49()

    def Building49(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 17, MapID: 49
      LocalBuilding(
        "bunker_sm",
        17,
        49,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(6394f, 4556f, 41.16785f), Vector3(0f, 0f, 45f), bunker_sm)
        )
      )
      LocalObject(612, Door.Constructor(Vector3(6394.905f, 4556.827f, 42.68885f)), owning_building_guid = 17)
    }

    Building15()

    def Building15(): Unit = { // Name: Neti Type: comm_station GUID: 18, MapID: 15
      LocalBuilding(
        "Neti",
        18,
        15,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3990f, 2502f, 79.94147f),
            Vector3(0f, 0f, 310f),
            comm_station
          )
        )
      )
      LocalObject(
        224,
        CaptureTerminal.Constructor(Vector3(4077.122f, 2517.55f, 62.64148f), capture_terminal),
        owning_building_guid = 18
      )
      LocalObject(485, Door.Constructor(Vector3(3910.617f, 2491.122f, 89.65648f)), owning_building_guid = 18)
      LocalObject(486, Door.Constructor(Vector3(3918.236f, 2519.851f, 81.69247f)), owning_building_guid = 18)
      LocalObject(487, Door.Constructor(Vector3(3922.312f, 2477.185f, 81.69247f)), owning_building_guid = 18)
      LocalObject(488, Door.Constructor(Vector3(3932.172f, 2531.545f, 89.65648f)), owning_building_guid = 18)
      LocalObject(489, Door.Constructor(Vector3(3970.427f, 2511.684f, 86.66248f)), owning_building_guid = 18)
      LocalObject(490, Door.Constructor(Vector3(3977.37f, 2517.51f, 86.66248f)), owning_building_guid = 18)
      LocalObject(491, Door.Constructor(Vector3(3981.511f, 2500.588f, 94.10147f)), owning_building_guid = 18)
      LocalObject(492, Door.Constructor(Vector3(4010.463f, 2508.727f, 86.66248f)), owning_building_guid = 18)
      LocalObject(493, Door.Constructor(Vector3(4020.505f, 2449.01f, 89.65648f)), owning_building_guid = 18)
      LocalObject(495, Door.Constructor(Vector3(4034.442f, 2460.704f, 81.69247f)), owning_building_guid = 18)
      LocalObject(496, Door.Constructor(Vector3(4042.395f, 2545.312f, 81.69247f)), owning_building_guid = 18)
      LocalObject(497, Door.Constructor(Vector3(4054.088f, 2531.376f, 89.65547f)), owning_building_guid = 18)
      LocalObject(498, Door.Constructor(Vector3(4068.789f, 2526.338f, 81.66248f)), owning_building_guid = 18)
      LocalObject(786, Door.Constructor(Vector3(3976.651f, 2511.686f, 86.66248f)), owning_building_guid = 18)
      LocalObject(787, Door.Constructor(Vector3(3988.908f, 2521.97f, 81.66248f)), owning_building_guid = 18)
      LocalObject(788, Door.Constructor(Vector3(3989.401f, 2527.605f, 74.16248f)), owning_building_guid = 18)
      LocalObject(789, Door.Constructor(Vector3(3991.373f, 2550.147f, 74.16248f)), owning_building_guid = 18)
      LocalObject(790, Door.Constructor(Vector3(3993.064f, 2504.571f, 74.16248f)), owning_building_guid = 18)
      LocalObject(791, Door.Constructor(Vector3(3993.557f, 2510.207f, 64.16248f)), owning_building_guid = 18)
      LocalObject(792, Door.Constructor(Vector3(3995.529f, 2532.748f, 64.16248f)), owning_building_guid = 18)
      LocalObject(793, Door.Constructor(Vector3(4003.842f, 2497.95f, 81.66248f)), owning_building_guid = 18)
      LocalObject(794, Door.Constructor(Vector3(4004.335f, 2503.585f, 86.66248f)), owning_building_guid = 18)
      LocalObject(795, Door.Constructor(Vector3(4008.772f, 2554.303f, 74.16248f)), owning_building_guid = 18)
      LocalObject(796, Door.Constructor(Vector3(4008.984f, 2491.822f, 76.66248f)), owning_building_guid = 18)
      LocalObject(797, Door.Constructor(Vector3(4010.463f, 2508.727f, 76.66248f)), owning_building_guid = 18)
      LocalObject(798, Door.Constructor(Vector3(4017.578f, 2525.14f, 74.16248f)), owning_building_guid = 18)
      LocalObject(799, Door.Constructor(Vector3(4018.071f, 2530.776f, 64.16248f)), owning_building_guid = 18)
      LocalObject(800, Door.Constructor(Vector3(4024.199f, 2535.918f, 74.16248f)), owning_building_guid = 18)
      LocalObject(801, Door.Constructor(Vector3(4024.411f, 2473.436f, 71.66248f)), owning_building_guid = 18)
      LocalObject(802, Door.Constructor(Vector3(4026.383f, 2495.978f, 64.16248f)), owning_building_guid = 18)
      LocalObject(803, Door.Constructor(Vector3(4035.189f, 2466.815f, 64.16248f)), owning_building_guid = 18)
      LocalObject(804, Door.Constructor(Vector3(4042.796f, 2488.863f, 71.66248f)), owning_building_guid = 18)
      LocalObject(805, Door.Constructor(Vector3(4047.445f, 2477.1f, 71.66248f)), owning_building_guid = 18)
      LocalObject(806, Door.Constructor(Vector3(4050.896f, 2516.547f, 64.16248f)), owning_building_guid = 18)
      LocalObject(807, Door.Constructor(Vector3(4065.83f, 2492.526f, 64.16248f)), owning_building_guid = 18)
      LocalObject(808, Door.Constructor(Vector3(4067.802f, 2515.068f, 64.16248f)), owning_building_guid = 18)
      LocalObject(809, Door.Constructor(Vector3(4072.945f, 2508.939f, 64.16248f)), owning_building_guid = 18)
      LocalObject(924, Door.Constructor(Vector3(3998.266f, 2485.821f, 82.43448f)), owning_building_guid = 18)
      LocalObject(2939, Door.Constructor(Vector3(4024.093f, 2482.734f, 71.99548f)), owning_building_guid = 18)
      LocalObject(2940, Door.Constructor(Vector3(4029.68f, 2487.422f, 71.99548f)), owning_building_guid = 18)
      LocalObject(2941, Door.Constructor(Vector3(4035.263f, 2492.107f, 71.99548f)), owning_building_guid = 18)
      LocalObject(
        972,
        IFFLock.Constructor(Vector3(4002.48f, 2485.773f, 81.59348f), Vector3(0, 0, 140)),
        owning_building_guid = 18,
        door_guid = 924
      )
      LocalObject(
        1117,
        IFFLock.Constructor(Vector3(3971.121f, 2509.595f, 86.60248f), Vector3(0, 0, 230)),
        owning_building_guid = 18,
        door_guid = 489
      )
      LocalObject(
        1118,
        IFFLock.Constructor(Vector3(3976.677f, 2519.596f, 86.60248f), Vector3(0, 0, 50)),
        owning_building_guid = 18,
        door_guid = 490
      )
      LocalObject(
        1119,
        IFFLock.Constructor(Vector3(3979.399f, 2499.871f, 94.02248f), Vector3(0, 0, 320)),
        owning_building_guid = 18,
        door_guid = 491
      )
      LocalObject(
        1120,
        IFFLock.Constructor(Vector3(3995.82f, 2530.939f, 63.97747f), Vector3(0, 0, 230)),
        owning_building_guid = 18,
        door_guid = 792
      )
      LocalObject(
        1121,
        IFFLock.Constructor(Vector3(4008.374f, 2508.036f, 86.60248f), Vector3(0, 0, 320)),
        owning_building_guid = 18,
        door_guid = 492
      )
      LocalObject(
        1122,
        IFFLock.Constructor(Vector3(4024.801f, 2471.711f, 71.47748f), Vector3(0, 0, 230)),
        owning_building_guid = 18,
        door_guid = 801
      )
      LocalObject(
        1123,
        IFFLock.Constructor(Vector3(4033.38f, 2466.525f, 63.97747f), Vector3(0, 0, 320)),
        owning_building_guid = 18,
        door_guid = 803
      )
      LocalObject(
        1124,
        IFFLock.Constructor(Vector3(4042.406f, 2490.588f, 71.47748f), Vector3(0, 0, 50)),
        owning_building_guid = 18,
        door_guid = 804
      )
      LocalObject(
        1125,
        IFFLock.Constructor(Vector3(4066.076f, 2514.68f, 63.97747f), Vector3(0, 0, 320)),
        owning_building_guid = 18,
        door_guid = 808
      )
      LocalObject(
        1126,
        IFFLock.Constructor(Vector3(4070.846f, 2527.049f, 81.59148f), Vector3(0, 0, 140)),
        owning_building_guid = 18,
        door_guid = 498
      )
      LocalObject(
        1127,
        IFFLock.Constructor(Vector3(4074.672f, 2509.327f, 63.97747f), Vector3(0, 0, 140)),
        owning_building_guid = 18,
        door_guid = 809
      )
      LocalObject(1481, Locker.Constructor(Vector3(4028.341f, 2472.083f, 70.40247f)), owning_building_guid = 18)
      LocalObject(1482, Locker.Constructor(Vector3(4029.09f, 2471.191f, 70.40247f)), owning_building_guid = 18)
      LocalObject(1483, Locker.Constructor(Vector3(4029.827f, 2470.313f, 70.40247f)), owning_building_guid = 18)
      LocalObject(1484, Locker.Constructor(Vector3(4030.565f, 2469.433f, 70.40247f)), owning_building_guid = 18)
      LocalObject(1485, Locker.Constructor(Vector3(4050.514f, 2476.811f, 62.64148f)), owning_building_guid = 18)
      LocalObject(1486, Locker.Constructor(Vector3(4051.365f, 2475.796f, 62.64148f)), owning_building_guid = 18)
      LocalObject(1487, Locker.Constructor(Vector3(4052.224f, 2474.773f, 62.64148f)), owning_building_guid = 18)
      LocalObject(1488, Locker.Constructor(Vector3(4053.083f, 2473.749f, 62.64148f)), owning_building_guid = 18)
      LocalObject(1489, Locker.Constructor(Vector3(4056.002f, 2470.271f, 62.64148f)), owning_building_guid = 18)
      LocalObject(1490, Locker.Constructor(Vector3(4056.853f, 2469.257f, 62.64148f)), owning_building_guid = 18)
      LocalObject(1491, Locker.Constructor(Vector3(4057.711f, 2468.233f, 62.64148f)), owning_building_guid = 18)
      LocalObject(1492, Locker.Constructor(Vector3(4058.571f, 2467.209f, 62.64148f)), owning_building_guid = 18)
      LocalObject(
        1998,
        Terminal.Constructor(Vector3(3966.125f, 2494.526f, 86.50147f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1999,
        Terminal.Constructor(Vector3(3984.167f, 2494.063f, 93.89648f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2000,
        Terminal.Constructor(Vector3(3984.339f, 2497.152f, 93.89648f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2001,
        Terminal.Constructor(Vector3(3987.535f, 2496.887f, 93.89648f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2002,
        Terminal.Constructor(Vector3(4034.363f, 2473.101f, 71.73148f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2003,
        Terminal.Constructor(Vector3(4037.221f, 2475.499f, 71.73148f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2004,
        Terminal.Constructor(Vector3(4040.124f, 2477.935f, 71.73148f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2847,
        Terminal.Constructor(Vector3(3970.634f, 2487.695f, 86.75848f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2848,
        Terminal.Constructor(Vector3(3986.26f, 2519.823f, 64.19847f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2849,
        Terminal.Constructor(Vector3(4020.218f, 2528.127f, 74.19847f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2850,
        Terminal.Constructor(Vector3(4022.377f, 2480.906f, 72.27547f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2851,
        Terminal.Constructor(Vector3(4027.96f, 2485.596f, 72.27547f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2852,
        Terminal.Constructor(Vector3(4033.545f, 2490.278f, 72.27547f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        3071,
        Terminal.Constructor(Vector3(4065.679f, 2497.023f, 82.82848f), vehicle_terminal_combined),
        owning_building_guid = 18
      )
      LocalObject(
        1887,
        VehicleSpawnPad.Constructor(Vector3(4056.803f, 2507.361f, 78.67047f), mb_pad_creation, Vector3(0, 0, -40)),
        owning_building_guid = 18,
        terminal_guid = 3071
      )
      LocalObject(2657, ResourceSilo.Constructor(Vector3(3901.224f, 2500.311f, 87.15848f)), owning_building_guid = 18)
      LocalObject(
        2715,
        SpawnTube.Constructor(Vector3(4023.006f, 2482.396f, 70.14147f), Vector3(0, 0, 50)),
        owning_building_guid = 18
      )
      LocalObject(
        2716,
        SpawnTube.Constructor(Vector3(4028.591f, 2487.083f, 70.14147f), Vector3(0, 0, 50)),
        owning_building_guid = 18
      )
      LocalObject(
        2717,
        SpawnTube.Constructor(Vector3(4034.174f, 2491.768f, 70.14147f), Vector3(0, 0, 50)),
        owning_building_guid = 18
      )
      LocalObject(
        1907,
        ProximityTerminal.Constructor(Vector3(3968.358f, 2482.712f, 80.14147f), medical_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1908,
        ProximityTerminal.Constructor(Vector3(4054.114f, 2471.672f, 62.64148f), medical_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2240,
        ProximityTerminal.Constructor(Vector3(4020.16f, 2551.909f, 88.38248f), pad_landing_frame),
        owning_building_guid = 18
      )
      LocalObject(
        2241,
        Terminal.Constructor(Vector3(4020.16f, 2551.909f, 88.38248f), air_rearm_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2603,
        ProximityTerminal.Constructor(Vector3(3968.49f, 2563.696f, 79.69147f), repair_silo),
        owning_building_guid = 18
      )
      LocalObject(
        2604,
        Terminal.Constructor(Vector3(3968.49f, 2563.696f, 79.69147f), ground_rearm_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2607,
        ProximityTerminal.Constructor(Vector3(4010.984f, 2439.141f, 79.69147f), repair_silo),
        owning_building_guid = 18
      )
      LocalObject(
        2608,
        Terminal.Constructor(Vector3(4010.984f, 2439.141f, 79.69147f), ground_rearm_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1819,
        FacilityTurret.Constructor(Vector3(3881.816f, 2505.794f, 88.64948f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1819, 5006)
      LocalObject(
        1820,
        FacilityTurret.Constructor(Vector3(3947.321f, 2425.897f, 88.64948f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1820, 5007)
      LocalObject(
        1821,
        FacilityTurret.Constructor(Vector3(3972.315f, 2583.208f, 88.64948f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1821, 5008)
      LocalObject(
        1822,
        FacilityTurret.Constructor(Vector3(4008.081f, 2420.558f, 88.64948f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1822, 5009)
      LocalObject(
        1823,
        FacilityTurret.Constructor(Vector3(4033.051f, 2577.875f, 88.64948f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1823, 5010)
      LocalObject(
        1825,
        FacilityTurret.Constructor(Vector3(4098.438f, 2497.903f, 88.64948f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1825, 5011)
      LocalObject(
        2384,
        Painbox.Constructor(Vector3(4004.879f, 2540.413f, 67.54347f), painbox),
        owning_building_guid = 18
      )
      LocalObject(
        2396,
        Painbox.Constructor(Vector3(4039.754f, 2483.419f, 74.58627f), painbox_continuous),
        owning_building_guid = 18
      )
      LocalObject(
        2408,
        Painbox.Constructor(Vector3(3993.439f, 2530.685f, 65.40018f), painbox_door_radius),
        owning_building_guid = 18
      )
      LocalObject(
        2432,
        Painbox.Constructor(Vector3(4022.882f, 2472.265f, 71.96748f), painbox_door_radius_continuous),
        owning_building_guid = 18
      )
      LocalObject(
        2433,
        Painbox.Constructor(Vector3(4044.405f, 2489.156f, 71.74918f), painbox_door_radius_continuous),
        owning_building_guid = 18
      )
      LocalObject(
        2434,
        Painbox.Constructor(Vector3(4048.505f, 2475.567f, 72.96748f), painbox_door_radius_continuous),
        owning_building_guid = 18
      )
      LocalObject(306, Generator.Constructor(Vector3(4007.429f, 2542.766f, 61.34747f)), owning_building_guid = 18)
      LocalObject(
        294,
        Terminal.Constructor(Vector3(4001.184f, 2537.464f, 62.64148f), gen_control),
        owning_building_guid = 18
      )
    }

    Building11()

    def Building11(): Unit = { // Name: Irkalla Type: comm_station GUID: 21, MapID: 11
      LocalBuilding(
        "Irkalla",
        21,
        11,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4826f, 5252f, 65.77693f),
            Vector3(0f, 0f, 223f),
            comm_station
          )
        )
      )
      LocalObject(
        227,
        CaptureTerminal.Constructor(Vector3(4846.088f, 5165.812f, 48.47693f), capture_terminal),
        owning_building_guid = 21
      )
      LocalObject(562, Door.Constructor(Vector3(4774.679f, 5218.763f, 75.49193f)), owning_building_guid = 21)
      LocalObject(563, Door.Constructor(Vector3(4787.087f, 5205.458f, 67.52793f)), owning_building_guid = 21)
      LocalObject(566, Door.Constructor(Vector3(4797.677f, 5318.297f, 67.52793f)), owning_building_guid = 21)
      LocalObject(567, Door.Constructor(Vector3(4810.982f, 5330.705f, 75.49193f)), owning_building_guid = 21)
      LocalObject(572, Door.Constructor(Vector3(4824.146f, 5260.403f, 79.93694f)), owning_building_guid = 21)
      LocalObject(573, Door.Constructor(Vector3(4833.789f, 5231.917f, 72.49793f)), owning_building_guid = 21)
      LocalObject(574, Door.Constructor(Vector3(4834.646f, 5272.053f, 72.49793f)), owning_building_guid = 21)
      LocalObject(575, Door.Constructor(Vector3(4840.071f, 5324.6f, 67.52793f)), owning_building_guid = 21)
      LocalObject(576, Door.Constructor(Vector3(4840.828f, 5265.424f, 72.49793f)), owning_building_guid = 21)
      LocalObject(577, Door.Constructor(Vector3(4852.479f, 5311.294f, 75.49193f)), owning_building_guid = 21)
      LocalObject(578, Door.Constructor(Vector3(4854.429f, 5174.593f, 67.49793f)), owning_building_guid = 21)
      LocalObject(579, Door.Constructor(Vector3(4858.69f, 5189.537f, 75.49093f)), owning_building_guid = 21)
      LocalObject(580, Door.Constructor(Vector3(4871.995f, 5201.944f, 67.52793f)), owning_building_guid = 21)
      LocalObject(852, Door.Constructor(Vector3(4793.228f, 5205.032f, 49.99793f)), owning_building_guid = 21)
      LocalObject(853, Door.Constructor(Vector3(4799.276f, 5216.141f, 57.49793f)), owning_building_guid = 21)
      LocalObject(854, Door.Constructor(Vector3(4804.14f, 5193.33f, 57.49793f)), owning_building_guid = 21)
      LocalObject(855, Door.Constructor(Vector3(4815.645f, 5198.589f, 57.49793f)), owning_building_guid = 21)
      LocalObject(856, Door.Constructor(Vector3(4816.829f, 5232.509f, 62.49793f)), owning_building_guid = 21)
      LocalObject(857, Door.Constructor(Vector3(4820.508f, 5175.778f, 49.99793f)), owning_building_guid = 21)
      LocalObject(858, Door.Constructor(Vector3(4821.89f, 5215.352f, 49.99793f)), owning_building_guid = 21)
      LocalObject(859, Door.Constructor(Vector3(4822.68f, 5237.965f, 67.49793f)), owning_building_guid = 21)
      LocalObject(860, Door.Constructor(Vector3(4828.333f, 5237.768f, 72.49793f)), owning_building_guid = 21)
      LocalObject(861, Door.Constructor(Vector3(4828.728f, 5249.075f, 59.99793f)), owning_building_guid = 21)
      LocalObject(862, Door.Constructor(Vector3(4833.789f, 5231.917f, 62.49793f)), owning_building_guid = 21)
      LocalObject(863, Door.Constructor(Vector3(4834.381f, 5248.877f, 49.99793f)), owning_building_guid = 21)
      LocalObject(864, Door.Constructor(Vector3(4834.974f, 5265.837f, 72.49793f)), owning_building_guid = 21)
      LocalObject(865, Door.Constructor(Vector3(4837.271f, 5169.532f, 49.99793f)), owning_building_guid = 21)
      LocalObject(866, Door.Constructor(Vector3(4843.122f, 5174.988f, 49.99793f)), owning_building_guid = 21)
      LocalObject(867, Door.Constructor(Vector3(4843.714f, 5191.948f, 49.99793f)), owning_building_guid = 21)
      LocalObject(868, Door.Constructor(Vector3(4845.886f, 5254.136f, 67.49793f)), owning_building_guid = 21)
      LocalObject(869, Door.Constructor(Vector3(4850.552f, 5225.671f, 59.99793f)), owning_building_guid = 21)
      LocalObject(870, Door.Constructor(Vector3(4851.539f, 5253.938f, 59.99793f)), owning_building_guid = 21)
      LocalObject(871, Door.Constructor(Vector3(4856.206f, 5225.474f, 49.99793f)), owning_building_guid = 21)
      LocalObject(872, Door.Constructor(Vector3(4856.995f, 5248.087f, 49.99793f)), owning_building_guid = 21)
      LocalObject(873, Door.Constructor(Vector3(4861.661f, 5219.623f, 59.99793f)), owning_building_guid = 21)
      LocalObject(874, Door.Constructor(Vector3(4874.153f, 5253.148f, 59.99793f)), owning_building_guid = 21)
      LocalObject(875, Door.Constructor(Vector3(4879.214f, 5235.991f, 59.99793f)), owning_building_guid = 21)
      LocalObject(927, Door.Constructor(Vector3(4810.275f, 5242.899f, 68.26993f)), owning_building_guid = 21)
      LocalObject(2964, Door.Constructor(Vector3(4808.545f, 5216.945f, 57.83093f)), owning_building_guid = 21)
      LocalObject(2965, Door.Constructor(Vector3(4813.519f, 5211.612f, 57.83093f)), owning_building_guid = 21)
      LocalObject(2966, Door.Constructor(Vector3(4818.49f, 5206.281f, 57.83093f)), owning_building_guid = 21)
      LocalObject(
        975,
        IFFLock.Constructor(Vector3(4810.448f, 5238.688f, 67.42893f), Vector3(0, 0, 227)),
        owning_building_guid = 21,
        door_guid = 927
      )
      LocalObject(
        1182,
        IFFLock.Constructor(Vector3(4792.844f, 5206.823f, 49.81293f), Vector3(0, 0, 47)),
        owning_building_guid = 21,
        door_guid = 852
      )
      LocalObject(
        1183,
        IFFLock.Constructor(Vector3(4797.574f, 5215.662f, 57.31293f), Vector3(0, 0, 317)),
        owning_building_guid = 21,
        door_guid = 853
      )
      LocalObject(
        1184,
        IFFLock.Constructor(Vector3(4817.347f, 5199.068f, 57.31293f), Vector3(0, 0, 137)),
        owning_building_guid = 21,
        door_guid = 855
      )
      LocalObject(
        1187,
        IFFLock.Constructor(Vector3(4823.319f, 5262.475f, 79.85793f), Vector3(0, 0, 47)),
        owning_building_guid = 21,
        door_guid = 572
      )
      LocalObject(
        1190,
        IFFLock.Constructor(Vector3(4832.597f, 5271.25f, 72.43793f), Vector3(0, 0, 317)),
        owning_building_guid = 21,
        door_guid = 574
      )
      LocalObject(
        1191,
        IFFLock.Constructor(Vector3(4832.989f, 5233.967f, 72.43793f), Vector3(0, 0, 47)),
        owning_building_guid = 21,
        door_guid = 573
      )
      LocalObject(
        1192,
        IFFLock.Constructor(Vector3(4837.749f, 5167.828f, 49.81293f), Vector3(0, 0, 227)),
        owning_building_guid = 21,
        door_guid = 865
      )
      LocalObject(
        1193,
        IFFLock.Constructor(Vector3(4842.644f, 5176.692f, 49.81293f), Vector3(0, 0, 47)),
        owning_building_guid = 21,
        door_guid = 866
      )
      LocalObject(
        1194,
        IFFLock.Constructor(Vector3(4842.875f, 5266.225f, 72.43793f), Vector3(0, 0, 137)),
        owning_building_guid = 21,
        door_guid = 576
      )
      LocalObject(
        1195,
        IFFLock.Constructor(Vector3(4855.246f, 5172.576f, 67.42693f), Vector3(0, 0, 227)),
        owning_building_guid = 21,
        door_guid = 578
      )
      LocalObject(
        1196,
        IFFLock.Constructor(Vector3(4855.204f, 5247.703f, 49.81293f), Vector3(0, 0, 317)),
        owning_building_guid = 21,
        door_guid = 872
      )
      LocalObject(1590, Locker.Constructor(Vector3(4794.845f, 5181.702f, 48.47693f)), owning_building_guid = 21)
      LocalObject(1591, Locker.Constructor(Vector3(4795.6f, 5209.786f, 56.23793f)), owning_building_guid = 21)
      LocalObject(1592, Locker.Constructor(Vector3(4795.823f, 5182.614f, 48.47693f)), owning_building_guid = 21)
      LocalObject(1593, Locker.Constructor(Vector3(4796.44f, 5210.569f, 56.23793f)), owning_building_guid = 21)
      LocalObject(1594, Locker.Constructor(Vector3(4796.8f, 5183.525f, 48.47693f)), owning_building_guid = 21)
      LocalObject(1595, Locker.Constructor(Vector3(4797.279f, 5211.352f, 56.23793f)), owning_building_guid = 21)
      LocalObject(1596, Locker.Constructor(Vector3(4797.769f, 5184.428f, 48.47693f)), owning_building_guid = 21)
      LocalObject(1597, Locker.Constructor(Vector3(4798.131f, 5212.146f, 56.23793f)), owning_building_guid = 21)
      LocalObject(1598, Locker.Constructor(Vector3(4801.089f, 5187.524f, 48.47693f)), owning_building_guid = 21)
      LocalObject(1599, Locker.Constructor(Vector3(4802.067f, 5188.437f, 48.47693f)), owning_building_guid = 21)
      LocalObject(1600, Locker.Constructor(Vector3(4803.044f, 5189.348f, 48.47693f)), owning_building_guid = 21)
      LocalObject(1601, Locker.Constructor(Vector3(4804.012f, 5190.25f, 48.47693f)), owning_building_guid = 21)
      LocalObject(
        2037,
        Terminal.Constructor(Vector3(4799.462f, 5206.186f, 57.56693f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2038,
        Terminal.Constructor(Vector3(4802.007f, 5203.457f, 57.56693f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2039,
        Terminal.Constructor(Vector3(4804.591f, 5200.686f, 57.56693f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2040,
        Terminal.Constructor(Vector3(4817.287f, 5275.451f, 72.33693f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2041,
        Terminal.Constructor(Vector3(4817.769f, 5257.409f, 79.73193f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2042,
        Terminal.Constructor(Vector3(4820.765f, 5254.194f, 79.73193f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2043,
        Terminal.Constructor(Vector3(4820.862f, 5257.399f, 79.73193f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2867,
        Terminal.Constructor(Vector3(4806.629f, 5218.563f, 58.11093f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2868,
        Terminal.Constructor(Vector3(4810.701f, 5270.591f, 72.59393f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2869,
        Terminal.Constructor(Vector3(4811.605f, 5213.233f, 58.11093f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2870,
        Terminal.Constructor(Vector3(4816.573f, 5207.901f, 58.11093f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2871,
        Terminal.Constructor(Vector3(4843.603f, 5256.668f, 50.03393f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2872,
        Terminal.Constructor(Vector3(4853.673f, 5223.191f, 60.03393f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        3074,
        Terminal.Constructor(Vector3(4824.99f, 5176.165f, 68.66393f), vehicle_terminal_combined),
        owning_building_guid = 21
      )
      LocalObject(
        1892,
        VehicleSpawnPad.Constructor(Vector3(4834.85f, 5185.569f, 64.50593f), mb_pad_creation, Vector3(0, 0, 47)),
        owning_building_guid = 21,
        terminal_guid = 3074
      )
      LocalObject(2660, ResourceSilo.Constructor(Vector3(4819.667f, 5340.566f, 72.99393f)), owning_building_guid = 21)
      LocalObject(
        2740,
        SpawnTube.Constructor(Vector3(4808.151f, 5218.013f, 55.97693f), Vector3(0, 0, 137)),
        owning_building_guid = 21
      )
      LocalObject(
        2741,
        SpawnTube.Constructor(Vector3(4813.123f, 5212.681f, 55.97693f), Vector3(0, 0, 137)),
        owning_building_guid = 21
      )
      LocalObject(
        2742,
        SpawnTube.Constructor(Vector3(4818.093f, 5207.351f, 55.97693f), Vector3(0, 0, 137)),
        owning_building_guid = 21
      )
      LocalObject(
        1912,
        ProximityTerminal.Constructor(Vector3(4799.069f, 5186.387f, 48.47693f), medical_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1913,
        ProximityTerminal.Constructor(Vector3(4805.606f, 5272.603f, 65.97693f), medical_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2273,
        ProximityTerminal.Constructor(Vector3(4877.419f, 5224.493f, 74.21793f), pad_landing_frame),
        owning_building_guid = 21
      )
      LocalObject(
        2274,
        Terminal.Constructor(Vector3(4877.419f, 5224.493f, 74.21793f), air_rearm_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2623,
        ProximityTerminal.Constructor(Vector3(4764.326f, 5227.755f, 65.52693f), repair_silo),
        owning_building_guid = 21
      )
      LocalObject(
        2624,
        Terminal.Constructor(Vector3(4764.326f, 5227.755f, 65.52693f), ground_rearm_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2631,
        ProximityTerminal.Constructor(Vector3(4886.486f, 5276.709f, 65.52693f), repair_silo),
        owning_building_guid = 21
      )
      LocalObject(
        2632,
        Terminal.Constructor(Vector3(4886.486f, 5276.709f, 65.52693f), ground_rearm_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1843,
        FacilityTurret.Constructor(Vector3(4745.616f, 5229.682f, 74.48493f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1843, 5012)
      LocalObject(
        1844,
        FacilityTurret.Constructor(Vector3(4747.768f, 5290.637f, 74.48493f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1844, 5013)
      LocalObject(
        1849,
        FacilityTurret.Constructor(Vector3(4824.127f, 5360.234f, 74.48493f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1849, 5014)
      LocalObject(
        1850,
        FacilityTurret.Constructor(Vector3(4827.584f, 5143.496f, 74.48493f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1850, 5015)
      LocalObject(
        1853,
        FacilityTurret.Constructor(Vector3(4904.024f, 5212.979f, 74.48493f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1853, 5016)
      LocalObject(
        1854,
        FacilityTurret.Constructor(Vector3(4906.171f, 5273.911f, 74.48493f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1854, 5017)
      LocalObject(
        2387,
        Painbox.Constructor(Vector3(4865.139f, 5239.151f, 53.37893f), painbox),
        owning_building_guid = 21
      )
      LocalObject(
        2399,
        Painbox.Constructor(Vector3(4810.048f, 5201.342f, 60.42173f), painbox_continuous),
        owning_building_guid = 21
      )
      LocalObject(
        2411,
        Painbox.Constructor(Vector3(4854.826f, 5250.067f, 51.23563f), painbox_door_radius),
        owning_building_guid = 21
      )
      LocalObject(
        2441,
        Painbox.Constructor(Vector3(4798.027f, 5217.607f, 57.80293f), painbox_door_radius_continuous),
        owning_building_guid = 21
      )
      LocalObject(
        2442,
        Painbox.Constructor(Vector3(4802.666f, 5192.192f, 58.80293f), painbox_door_radius_continuous),
        owning_building_guid = 21
      )
      LocalObject(
        2443,
        Painbox.Constructor(Vector3(4816.021f, 5196.998f, 57.58463f), painbox_door_radius_continuous),
        owning_building_guid = 21
      )
      LocalObject(309, Generator.Constructor(Vector3(4867.622f, 5236.729f, 47.18293f)), owning_building_guid = 21)
      LocalObject(
        297,
        Terminal.Constructor(Vector3(4862f, 5242.688f, 48.47693f), gen_control),
        owning_building_guid = 21
      )
    }

    Building5()

    def Building5(): Unit = { // Name: Akkan Type: comm_station_dsp GUID: 24, MapID: 5
      LocalBuilding(
        "Akkan",
        24,
        5,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(2694f, 4324f, 39.29922f),
            Vector3(0f, 0f, 360f),
            comm_station_dsp
          )
        )
      )
      LocalObject(
        220,
        CaptureTerminal.Constructor(Vector3(2770.089f, 4304.734f, 21.89923f), capture_terminal),
        owning_building_guid = 24
      )
      LocalObject(281, Door.Constructor(Vector3(2762.339f, 4394.464f, 42.67722f)), owning_building_guid = 24)
      LocalObject(378, Door.Constructor(Vector3(2634.196f, 4280.501f, 40.95023f)), owning_building_guid = 24)
      LocalObject(379, Door.Constructor(Vector3(2634.196f, 4298.693f, 48.91422f)), owning_building_guid = 24)
      LocalObject(381, Door.Constructor(Vector3(2651.307f, 4256.197f, 48.91422f)), owning_building_guid = 24)
      LocalObject(382, Door.Constructor(Vector3(2669.499f, 4256.197f, 40.95023f)), owning_building_guid = 24)
      LocalObject(383, Door.Constructor(Vector3(2674f, 4315.231f, 45.92022f)), owning_building_guid = 24)
      LocalObject(384, Door.Constructor(Vector3(2674f, 4324.295f, 45.92022f)), owning_building_guid = 24)
      LocalObject(385, Door.Constructor(Vector3(2686.763f, 4443.958f, 40.95023f)), owning_building_guid = 24)
      LocalObject(386, Door.Constructor(Vector3(2689.625f, 4316.59f, 53.35923f)), owning_building_guid = 24)
      LocalObject(387, Door.Constructor(Vector3(2699.627f, 4456.823f, 48.91322f)), owning_building_guid = 24)
      LocalObject(388, Door.Constructor(Vector3(2702f, 4344f, 45.92022f)), owning_building_guid = 24)
      LocalObject(393, Door.Constructor(Vector3(2741.721f, 4488.353f, 40.95023f)), owning_building_guid = 24)
      LocalObject(394, Door.Constructor(Vector3(2747.952f, 4428.355f, 45.91623f)), owning_building_guid = 24)
      LocalObject(395, Door.Constructor(Vector3(2749.927f, 4398.35f, 40.92223f)), owning_building_guid = 24)
      LocalObject(396, Door.Constructor(Vector3(2759.914f, 4488.353f, 48.91322f)), owning_building_guid = 24)
      LocalObject(397, Door.Constructor(Vector3(2773.929f, 4311.406f, 48.91422f)), owning_building_guid = 24)
      LocalObject(398, Door.Constructor(Vector3(2786.793f, 4324.27f, 40.95023f)), owning_building_guid = 24)
      LocalObject(399, Door.Constructor(Vector3(2800.977f, 4387.008f, 48.91322f)), owning_building_guid = 24)
      LocalObject(400, Door.Constructor(Vector3(2800.977f, 4405.2f, 40.95023f)), owning_building_guid = 24)
      LocalObject(401, Door.Constructor(Vector3(2810f, 4404f, 40.92022f)), owning_building_guid = 24)
      LocalObject(701, Door.Constructor(Vector3(2678f, 4320f, 45.92022f)), owning_building_guid = 24)
      LocalObject(702, Door.Constructor(Vector3(2678f, 4336f, 40.92022f)), owning_building_guid = 24)
      LocalObject(703, Door.Constructor(Vector3(2702f, 4336f, 45.92022f)), owning_building_guid = 24)
      LocalObject(704, Door.Constructor(Vector3(2702f, 4344f, 35.92022f)), owning_building_guid = 24)
      LocalObject(705, Door.Constructor(Vector3(2706f, 4332f, 40.92022f)), owning_building_guid = 24)
      LocalObject(706, Door.Constructor(Vector3(2714f, 4332f, 35.92022f)), owning_building_guid = 24)
      LocalObject(707, Door.Constructor(Vector3(2718f, 4368f, 30.92023f)), owning_building_guid = 24)
      LocalObject(708, Door.Constructor(Vector3(2722f, 4356f, 23.42023f)), owning_building_guid = 24)
      LocalObject(709, Door.Constructor(Vector3(2734f, 4320f, 23.42023f)), owning_building_guid = 24)
      LocalObject(710, Door.Constructor(Vector3(2734f, 4384f, 23.42023f)), owning_building_guid = 24)
      LocalObject(711, Door.Constructor(Vector3(2738f, 4316f, 30.92023f)), owning_building_guid = 24)
      LocalObject(712, Door.Constructor(Vector3(2738f, 4332f, 30.92023f)), owning_building_guid = 24)
      LocalObject(713, Door.Constructor(Vector3(2738f, 4356f, 30.92023f)), owning_building_guid = 24)
      LocalObject(714, Door.Constructor(Vector3(2750f, 4336f, 23.42023f)), owning_building_guid = 24)
      LocalObject(715, Door.Constructor(Vector3(2750f, 4352f, 30.92023f)), owning_building_guid = 24)
      LocalObject(716, Door.Constructor(Vector3(2753.921f, 4418.351f, 45.92223f)), owning_building_guid = 24)
      LocalObject(717, Door.Constructor(Vector3(2766f, 4296f, 23.42023f)), owning_building_guid = 24)
      LocalObject(718, Door.Constructor(Vector3(2774f, 4296f, 23.42023f)), owning_building_guid = 24)
      LocalObject(719, Door.Constructor(Vector3(2778f, 4308f, 23.42023f)), owning_building_guid = 24)
      LocalObject(720, Door.Constructor(Vector3(2782f, 4328f, 30.92023f)), owning_building_guid = 24)
      LocalObject(721, Door.Constructor(Vector3(2782f, 4360f, 30.92023f)), owning_building_guid = 24)
      LocalObject(920, Door.Constructor(Vector3(2711.707f, 4319.922f, 41.69122f)), owning_building_guid = 24)
      LocalObject(2909, Door.Constructor(Vector3(2730.673f, 4337.733f, 31.25323f)), owning_building_guid = 24)
      LocalObject(2910, Door.Constructor(Vector3(2730.673f, 4345.026f, 31.25323f)), owning_building_guid = 24)
      LocalObject(2911, Door.Constructor(Vector3(2730.673f, 4352.315f, 31.25323f)), owning_building_guid = 24)
      LocalObject(
        968,
        IFFLock.Constructor(Vector3(2714.454f, 4323.09f, 40.86723f), Vector3(0, 0, 90)),
        owning_building_guid = 24,
        door_guid = 920
      )
      LocalObject(
        1036,
        IFFLock.Constructor(Vector3(2671.959f, 4325.104f, 45.86723f), Vector3(0, 0, 0)),
        owning_building_guid = 24,
        door_guid = 384
      )
      LocalObject(
        1037,
        IFFLock.Constructor(Vector3(2676.04f, 4314.42f, 45.86723f), Vector3(0, 0, 180)),
        owning_building_guid = 24,
        door_guid = 383
      )
      LocalObject(
        1038,
        IFFLock.Constructor(Vector3(2688.817f, 4314.514f, 53.36723f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 386
      )
      LocalObject(
        1039,
        IFFLock.Constructor(Vector3(2701.193f, 4341.962f, 45.86723f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 388
      )
      LocalObject(
        1040,
        IFFLock.Constructor(Vector3(2734.94f, 4385.572f, 23.23523f), Vector3(0, 0, 90)),
        owning_building_guid = 24,
        door_guid = 710
      )
      LocalObject(
        1043,
        IFFLock.Constructor(Vector3(2736.428f, 4356.94f, 30.73523f), Vector3(0, 0, 0)),
        owning_building_guid = 24,
        door_guid = 713
      )
      LocalObject(
        1044,
        IFFLock.Constructor(Vector3(2739.572f, 4331.19f, 30.73523f), Vector3(0, 0, 180)),
        owning_building_guid = 24,
        door_guid = 712
      )
      LocalObject(
        1047,
        IFFLock.Constructor(Vector3(2745.907f, 4429.163f, 45.84623f), Vector3(0, 0, 0)),
        owning_building_guid = 24,
        door_guid = 394
      )
      LocalObject(
        1048,
        IFFLock.Constructor(Vector3(2749.06f, 4334.428f, 23.23523f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 714
      )
      LocalObject(
        1049,
        IFFLock.Constructor(Vector3(2749.124f, 4396.312f, 40.91122f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 395
      )
      LocalObject(
        1050,
        IFFLock.Constructor(Vector3(2765.06f, 4294.428f, 23.23523f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 717
      )
      LocalObject(
        1052,
        IFFLock.Constructor(Vector3(2774.813f, 4297.572f, 23.23523f), Vector3(0, 0, 90)),
        owning_building_guid = 24,
        door_guid = 718
      )
      LocalObject(
        1053,
        IFFLock.Constructor(Vector3(2807.953f, 4404.808f, 40.81023f), Vector3(0, 0, 0)),
        owning_building_guid = 24,
        door_guid = 401
      )
      LocalObject(1352, Locker.Constructor(Vector3(2741.563f, 4334.141f, 29.66022f)), owning_building_guid = 24)
      LocalObject(1355, Locker.Constructor(Vector3(2742.727f, 4334.141f, 29.66022f)), owning_building_guid = 24)
      LocalObject(1358, Locker.Constructor(Vector3(2743.874f, 4334.141f, 29.66022f)), owning_building_guid = 24)
      LocalObject(1359, Locker.Constructor(Vector3(2745.023f, 4334.141f, 29.66022f)), owning_building_guid = 24)
      LocalObject(1364, Locker.Constructor(Vector3(2752.194f, 4354.165f, 21.89923f)), owning_building_guid = 24)
      LocalObject(1365, Locker.Constructor(Vector3(2753.518f, 4354.165f, 21.89923f)), owning_building_guid = 24)
      LocalObject(1366, Locker.Constructor(Vector3(2754.854f, 4354.165f, 21.89923f)), owning_building_guid = 24)
      LocalObject(1367, Locker.Constructor(Vector3(2756.191f, 4354.165f, 21.89923f)), owning_building_guid = 24)
      LocalObject(1368, Locker.Constructor(Vector3(2760.731f, 4354.165f, 21.89923f)), owning_building_guid = 24)
      LocalObject(1369, Locker.Constructor(Vector3(2762.055f, 4354.165f, 21.89923f)), owning_building_guid = 24)
      LocalObject(1370, Locker.Constructor(Vector3(2763.391f, 4354.165f, 21.89923f)), owning_building_guid = 24)
      LocalObject(1371, Locker.Constructor(Vector3(2764.728f, 4354.165f, 21.89923f)), owning_building_guid = 24)
      LocalObject(
        283,
        Terminal.Constructor(Vector3(2753.879f, 4426.918f, 45.00322f), dropship_vehicle_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        282,
        VehicleSpawnPad.Constructor(Vector3(2762.328f, 4448.856f, 39.32722f), dropship_pad_doors, Vector3(0, 0, 90)),
        owning_building_guid = 24,
        terminal_guid = 283
      )
      LocalObject(
        1949,
        Terminal.Constructor(Vector3(2684.378f, 4300.897f, 45.75922f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1950,
        Terminal.Constructor(Vector3(2694.075f, 4316.547f, 53.15422f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1951,
        Terminal.Constructor(Vector3(2696.331f, 4314.43f, 53.15422f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1952,
        Terminal.Constructor(Vector3(2696.332f, 4318.825f, 53.15422f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1953,
        Terminal.Constructor(Vector3(2698.592f, 4316.59f, 53.15422f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1954,
        Terminal.Constructor(Vector3(2744.654f, 4339.408f, 30.98922f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1955,
        Terminal.Constructor(Vector3(2744.654f, 4343.139f, 30.98922f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1956,
        Terminal.Constructor(Vector3(2744.654f, 4346.928f, 30.98922f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2818,
        Terminal.Constructor(Vector3(2692.509f, 4299.959f, 46.01622f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2819,
        Terminal.Constructor(Vector3(2730.971f, 4335.243f, 31.53323f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2820,
        Terminal.Constructor(Vector3(2730.967f, 4342.535f, 31.53323f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2821,
        Terminal.Constructor(Vector3(2730.97f, 4349.823f, 31.53323f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2822,
        Terminal.Constructor(Vector3(2749.103f, 4418.906f, 45.94722f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2823,
        Terminal.Constructor(Vector3(2758.058f, 4323.409f, 23.42723f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2824,
        Terminal.Constructor(Vector3(2765.409f, 4379.942f, 23.42723f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2825,
        Terminal.Constructor(Vector3(2774.058f, 4331.409f, 30.95622f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2826,
        Terminal.Constructor(Vector3(2774.058f, 4371.409f, 30.95622f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        3067,
        Terminal.Constructor(Vector3(2711.698f, 4432.044f, 42.08622f), ground_vehicle_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1881,
        VehicleSpawnPad.Constructor(Vector3(2711.786f, 4418.411f, 37.92823f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 24,
        terminal_guid = 3067
      )
      LocalObject(2653, ResourceSilo.Constructor(Vector3(2792.212f, 4489.642f, 46.41623f)), owning_building_guid = 24)
      LocalObject(
        2685,
        SpawnTube.Constructor(Vector3(2730.233f, 4336.683f, 29.39923f), Vector3(0, 0, 0)),
        owning_building_guid = 24
      )
      LocalObject(
        2686,
        SpawnTube.Constructor(Vector3(2730.233f, 4343.974f, 29.39923f), Vector3(0, 0, 0)),
        owning_building_guid = 24
      )
      LocalObject(
        2687,
        SpawnTube.Constructor(Vector3(2730.233f, 4351.262f, 29.39923f), Vector3(0, 0, 0)),
        owning_building_guid = 24
      )
      LocalObject(
        1900,
        ProximityTerminal.Constructor(Vector3(2694.863f, 4295.013f, 39.39922f), medical_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1901,
        ProximityTerminal.Constructor(Vector3(2758.444f, 4353.62f, 21.89923f), medical_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2186,
        ProximityTerminal.Constructor(Vector3(2675.153f, 4417.398f, 47.70922f), pad_landing_frame),
        owning_building_guid = 24
      )
      LocalObject(
        2187,
        Terminal.Constructor(Vector3(2675.153f, 4417.398f, 47.70922f), air_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2189,
        ProximityTerminal.Constructor(Vector3(2691.514f, 4371.467f, 44.99323f), pad_landing_frame),
        owning_building_guid = 24
      )
      LocalObject(
        2190,
        Terminal.Constructor(Vector3(2691.514f, 4371.467f, 44.99323f), air_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2192,
        ProximityTerminal.Constructor(Vector3(2743.804f, 4335.901f, 52.17522f), pad_landing_frame),
        owning_building_guid = 24
      )
      LocalObject(
        2193,
        Terminal.Constructor(Vector3(2743.804f, 4335.901f, 52.17522f), air_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2195,
        ProximityTerminal.Constructor(Vector3(2779.071f, 4352.159f, 47.72223f), pad_landing_frame),
        owning_building_guid = 24
      )
      LocalObject(
        2196,
        Terminal.Constructor(Vector3(2779.071f, 4352.159f, 47.72223f), air_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2571,
        ProximityTerminal.Constructor(Vector3(2632.642f, 4362.241f, 39.04922f), repair_silo),
        owning_building_guid = 24
      )
      LocalObject(
        2572,
        Terminal.Constructor(Vector3(2632.642f, 4362.241f, 39.04922f), ground_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2575,
        ProximityTerminal.Constructor(Vector3(2802.57f, 4365.151f, 39.04922f), repair_silo),
        owning_building_guid = 24
      )
      LocalObject(
        2576,
        Terminal.Constructor(Vector3(2802.57f, 4365.151f, 39.04922f), ground_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1780,
        FacilityTurret.Constructor(Vector3(2620.401f, 4397.113f, 47.90723f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1780, 5018)
      LocalObject(
        1781,
        FacilityTurret.Constructor(Vector3(2621.554f, 4243.565f, 47.90723f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1781, 5019)
      LocalObject(
        1782,
        FacilityTurret.Constructor(Vector3(2665.445f, 4443.667f, 47.90723f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1782, 5020)
      LocalObject(
        1784,
        FacilityTurret.Constructor(Vector3(2724.428f, 4242.396f, 47.90723f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1784, 5021)
      LocalObject(
        1785,
        FacilityTurret.Constructor(Vector3(2725.449f, 4502.154f, 47.90723f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1785, 5022)
      LocalObject(
        1787,
        FacilityTurret.Constructor(Vector3(2766.537f, 4283.011f, 47.90723f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1787, 5023)
      LocalObject(
        1788,
        FacilityTurret.Constructor(Vector3(2813.619f, 4500.985f, 47.90723f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1788, 5024)
      LocalObject(
        1789,
        FacilityTurret.Constructor(Vector3(2814.773f, 4332.733f, 47.90723f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1789, 5025)
      LocalObject(
        2380,
        Painbox.Constructor(Vector3(2722.428f, 4384.057f, 25.79353f), painbox),
        owning_building_guid = 24
      )
      LocalObject(
        2392,
        Painbox.Constructor(Vector3(2739.857f, 4344.408f, 33.42672f), painbox_continuous),
        owning_building_guid = 24
      )
      LocalObject(
        2404,
        Painbox.Constructor(Vector3(2736.203f, 4382.915f, 25.03143f), painbox_door_radius),
        owning_building_guid = 24
      )
      LocalObject(
        2420,
        Painbox.Constructor(Vector3(2737.087f, 4329.386f, 31.82842f), painbox_door_radius_continuous),
        owning_building_guid = 24
      )
      LocalObject(
        2421,
        Painbox.Constructor(Vector3(2737.895f, 4358.081f, 32.29922f), painbox_door_radius_continuous),
        owning_building_guid = 24
      )
      LocalObject(
        2422,
        Painbox.Constructor(Vector3(2752.317f, 4351.888f, 32.72953f), painbox_door_radius_continuous),
        owning_building_guid = 24
      )
      LocalObject(302, Generator.Constructor(Vector3(2718.445f, 4383.975f, 20.60522f)), owning_building_guid = 24)
      LocalObject(
        290,
        Terminal.Constructor(Vector3(2726.637f, 4384.022f, 21.89923f), gen_control),
        owning_building_guid = 24
      )
    }

    Building7()

    def Building7(): Unit = { // Name: Dagon Type: cryo_facility GUID: 27, MapID: 7
      LocalBuilding(
        "Dagon",
        27,
        7,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(1784f, 5738f, 39.73853f),
            Vector3(0f, 0f, 178f),
            cryo_facility
          )
        )
      )
      LocalObject(
        219,
        CaptureTerminal.Constructor(Vector3(1810.004f, 5677.79f, 29.73853f), capture_terminal),
        owning_building_guid = 27
      )
      LocalObject(353, Door.Constructor(Vector3(1734.238f, 5772.257f, 41.28953f)), owning_building_guid = 27)
      LocalObject(354, Door.Constructor(Vector3(1734.873f, 5790.439f, 49.25353f)), owning_building_guid = 27)
      LocalObject(356, Door.Constructor(Vector3(1755.23f, 5831.061f, 41.25953f)), owning_building_guid = 27)
      LocalObject(357, Door.Constructor(Vector3(1763.408f, 5821.697f, 41.28953f)), owning_building_guid = 27)
      LocalObject(358, Door.Constructor(Vector3(1779.893f, 5735.004f, 51.25953f)), owning_building_guid = 27)
      LocalObject(359, Door.Constructor(Vector3(1781.59f, 5821.062f, 49.25353f)), owning_building_guid = 27)
      LocalObject(360, Door.Constructor(Vector3(1799.292f, 5717.454f, 51.25953f)), owning_building_guid = 27)
      LocalObject(361, Door.Constructor(Vector3(1805.752f, 5669.396f, 49.25353f)), owning_building_guid = 27)
      LocalObject(362, Door.Constructor(Vector3(1823.934f, 5668.761f, 41.28953f)), owning_building_guid = 27)
      LocalObject(363, Door.Constructor(Vector3(1842.149f, 5713.263f, 49.25353f)), owning_building_guid = 27)
      LocalObject(364, Door.Constructor(Vector3(1842.784f, 5731.444f, 41.28953f)), owning_building_guid = 27)
      LocalObject(678, Door.Constructor(Vector3(1759.317f, 5718.85f, 31.25953f)), owning_building_guid = 27)
      LocalObject(679, Door.Constructor(Vector3(1759.875f, 5734.84f, 23.75953f)), owning_building_guid = 27)
      LocalObject(680, Door.Constructor(Vector3(1771.17f, 5714.434f, 31.25953f)), owning_building_guid = 27)
      LocalObject(681, Door.Constructor(Vector3(1772.007f, 5738.419f, 31.25953f)), owning_building_guid = 27)
      LocalObject(682, Door.Constructor(Vector3(1780.282f, 5746.135f, 51.25953f)), owning_building_guid = 27)
      LocalObject(683, Door.Constructor(Vector3(1782.185f, 5686.032f, 31.25953f)), owning_building_guid = 27)
      LocalObject(684, Door.Constructor(Vector3(1784.14f, 5741.998f, 31.25953f)), owning_building_guid = 27)
      LocalObject(685, Door.Constructor(Vector3(1784.977f, 5765.983f, 31.25953f)), owning_building_guid = 27)
      LocalObject(686, Door.Constructor(Vector3(1794.597f, 5697.605f, 31.25953f)), owning_building_guid = 27)
      LocalObject(687, Door.Constructor(Vector3(1795.993f, 5737.581f, 31.25953f)), owning_building_guid = 27)
      LocalObject(688, Door.Constructor(Vector3(1797.617f, 5669.483f, 31.25953f)), owning_building_guid = 27)
      LocalObject(689, Door.Constructor(Vector3(1799.064f, 5825.528f, 33.75953f)), owning_building_guid = 27)
      LocalObject(690, Door.Constructor(Vector3(1799.292f, 5717.454f, 31.25953f)), owning_building_guid = 27)
      LocalObject(691, Door.Constructor(Vector3(1799.292f, 5717.454f, 41.25953f)), owning_building_guid = 27)
      LocalObject(692, Door.Constructor(Vector3(1805.105f, 5769.283f, 31.25953f)), owning_building_guid = 27)
      LocalObject(693, Door.Constructor(Vector3(1805.612f, 5669.204f, 31.25953f)), owning_building_guid = 27)
      LocalObject(694, Door.Constructor(Vector3(1805.942f, 5793.268f, 33.75953f)), owning_building_guid = 27)
      LocalObject(695, Door.Constructor(Vector3(1813.607f, 5668.925f, 31.25953f)), owning_building_guid = 27)
      LocalObject(696, Door.Constructor(Vector3(1815.282f, 5716.896f, 31.25953f)), owning_building_guid = 27)
      LocalObject(697, Door.Constructor(Vector3(1818.582f, 5696.768f, 31.25953f)), owning_building_guid = 27)
      LocalObject(698, Door.Constructor(Vector3(1819.978f, 5736.744f, 31.25953f)), owning_building_guid = 27)
      LocalObject(699, Door.Constructor(Vector3(1826.07f, 5796.567f, 33.75953f)), owning_building_guid = 27)
      LocalObject(700, Door.Constructor(Vector3(1832.39f, 5748.317f, 31.25953f)), owning_building_guid = 27)
      LocalObject(919, Door.Constructor(Vector3(1779.135f, 5713.072f, 42.02153f)), owning_building_guid = 27)
      LocalObject(930, Door.Constructor(Vector3(1772.007f, 5738.419f, 41.25953f)), owning_building_guid = 27)
      LocalObject(931, Door.Constructor(Vector3(1787.718f, 5729.865f, 41.25753f)), owning_building_guid = 27)
      LocalObject(2900, Door.Constructor(Vector3(1778.621f, 5717.86f, 31.59253f)), owning_building_guid = 27)
      LocalObject(2901, Door.Constructor(Vector3(1778.875f, 5725.145f, 31.59253f)), owning_building_guid = 27)
      LocalObject(2902, Door.Constructor(Vector3(1779.13f, 5732.434f, 31.59253f)), owning_building_guid = 27)
      LocalObject(
        967,
        IFFLock.Constructor(Vector3(1782.26f, 5710.222f, 41.22053f), Vector3(0, 0, 182)),
        owning_building_guid = 27,
        door_guid = 919
      )
      LocalObject(
        1016,
        IFFLock.Constructor(Vector3(1754.345f, 5829.048f, 41.19053f), Vector3(0, 0, 272)),
        owning_building_guid = 27,
        door_guid = 356
      )
      LocalObject(
        1017,
        IFFLock.Constructor(Vector3(1760.739f, 5736.383f, 23.57453f), Vector3(0, 0, 92)),
        owning_building_guid = 27,
        door_guid = 679
      )
      LocalObject(
        1018,
        IFFLock.Constructor(Vector3(1770.464f, 5739.283f, 31.07453f), Vector3(0, 0, 2)),
        owning_building_guid = 27,
        door_guid = 681
      )
      LocalObject(
        1019,
        IFFLock.Constructor(Vector3(1772.713f, 5713.569f, 31.07453f), Vector3(0, 0, 182)),
        owning_building_guid = 27,
        door_guid = 680
      )
      LocalObject(
        1020,
        IFFLock.Constructor(Vector3(1781.909f, 5734.113f, 51.19053f), Vector3(0, 0, 182)),
        owning_building_guid = 27,
        door_guid = 358
      )
      LocalObject(
        1021,
        IFFLock.Constructor(Vector3(1798.407f, 5715.44f, 51.19053f), Vector3(0, 0, 272)),
        owning_building_guid = 27,
        door_guid = 360
      )
      LocalObject(
        1022,
        IFFLock.Constructor(Vector3(1804.745f, 5667.661f, 31.07453f), Vector3(0, 0, 272)),
        owning_building_guid = 27,
        door_guid = 693
      )
      LocalObject(
        1023,
        IFFLock.Constructor(Vector3(1814.602f, 5670.463f, 31.07453f), Vector3(0, 0, 92)),
        owning_building_guid = 27,
        door_guid = 695
      )
      LocalObject(1307, Locker.Constructor(Vector3(1745.148f, 5716.982f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1308, Locker.Constructor(Vector3(1745.843f, 5736.971f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1309, Locker.Constructor(Vector3(1746.203f, 5716.945f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1310, Locker.Constructor(Vector3(1746.897f, 5736.934f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1311, Locker.Constructor(Vector3(1747.258f, 5716.908f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1312, Locker.Constructor(Vector3(1747.957f, 5736.897f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1313, Locker.Constructor(Vector3(1748.312f, 5716.872f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1314, Locker.Constructor(Vector3(1749.011f, 5736.86f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1315, Locker.Constructor(Vector3(1749.372f, 5716.834f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1316, Locker.Constructor(Vector3(1750.065f, 5736.824f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1317, Locker.Constructor(Vector3(1750.426f, 5716.798f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1318, Locker.Constructor(Vector3(1751.121f, 5736.787f, 29.64653f)), owning_building_guid = 27)
      LocalObject(1319, Locker.Constructor(Vector3(1762.112f, 5684.204f, 29.73353f)), owning_building_guid = 27)
      LocalObject(1320, Locker.Constructor(Vector3(1762.156f, 5685.456f, 29.73353f)), owning_building_guid = 27)
      LocalObject(1321, Locker.Constructor(Vector3(1762.2f, 5686.717f, 29.73353f)), owning_building_guid = 27)
      LocalObject(1322, Locker.Constructor(Vector3(1762.244f, 5687.977f, 29.73353f)), owning_building_guid = 27)
      LocalObject(1323, Locker.Constructor(Vector3(1762.288f, 5689.232f, 29.73353f)), owning_building_guid = 27)
      LocalObject(1324, Locker.Constructor(Vector3(1764.914f, 5736.524f, 29.99953f)), owning_building_guid = 27)
      LocalObject(1325, Locker.Constructor(Vector3(1766.062f, 5736.484f, 29.99953f)), owning_building_guid = 27)
      LocalObject(1326, Locker.Constructor(Vector3(1767.208f, 5736.444f, 29.99953f)), owning_building_guid = 27)
      LocalObject(1327, Locker.Constructor(Vector3(1768.372f, 5736.403f, 29.99953f)), owning_building_guid = 27)
      LocalObject(1715, Locker.Constructor(Vector3(1765.82f, 5740.62f, 39.73853f)), owning_building_guid = 27)
      LocalObject(1716, Locker.Constructor(Vector3(1765.856f, 5741.653f, 39.73853f)), owning_building_guid = 27)
      LocalObject(1717, Locker.Constructor(Vector3(1765.944f, 5744.174f, 39.50953f)), owning_building_guid = 27)
      LocalObject(1718, Locker.Constructor(Vector3(1765.981f, 5745.207f, 39.50953f)), owning_building_guid = 27)
      LocalObject(1719, Locker.Constructor(Vector3(1766.017f, 5746.261f, 39.50953f)), owning_building_guid = 27)
      LocalObject(1720, Locker.Constructor(Vector3(1766.053f, 5747.294f, 39.50953f)), owning_building_guid = 27)
      LocalObject(1721, Locker.Constructor(Vector3(1766.141f, 5749.81f, 39.73853f)), owning_building_guid = 27)
      LocalObject(1722, Locker.Constructor(Vector3(1766.177f, 5750.843f, 39.73853f)), owning_building_guid = 27)
      LocalObject(
        230,
        Terminal.Constructor(Vector3(1764.199f, 5683.083f, 29.72853f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        231,
        Terminal.Constructor(Vector3(1764.455f, 5690.403f, 29.72853f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        232,
        Terminal.Constructor(Vector3(1765.596f, 5681.585f, 29.72853f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        233,
        Terminal.Constructor(Vector3(1765.953f, 5691.8f, 29.72853f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        234,
        Terminal.Constructor(Vector3(1778.288f, 5681.142f, 29.72853f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        235,
        Terminal.Constructor(Vector3(1778.645f, 5691.356f, 29.72853f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        236,
        Terminal.Constructor(Vector3(1779.786f, 5682.538f, 29.72853f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        237,
        Terminal.Constructor(Vector3(1780.042f, 5689.859f, 29.72853f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1936,
        Terminal.Constructor(Vector3(1764.836f, 5723.732f, 31.32853f), order_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1937,
        Terminal.Constructor(Vector3(1764.969f, 5727.519f, 31.32853f), order_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1938,
        Terminal.Constructor(Vector3(1765.099f, 5731.248f, 31.32853f), order_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1939,
        Terminal.Constructor(Vector3(1793.689f, 5728.13f, 41.03353f), order_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2811,
        Terminal.Constructor(Vector3(1778.411f, 5720.361f, 31.87253f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2812,
        Terminal.Constructor(Vector3(1778.668f, 5727.645f, 31.87253f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2813,
        Terminal.Constructor(Vector3(1778.919f, 5734.933f, 31.87253f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2814,
        Terminal.Constructor(Vector3(1782.359f, 5674.018f, 31.35153f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2815,
        Terminal.Constructor(Vector3(1782.572f, 5751.699f, 41.31753f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2816,
        Terminal.Constructor(Vector3(1802.055f, 5793.994f, 33.85153f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2817,
        Terminal.Constructor(Vector3(1823.996f, 5737.197f, 31.35153f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        3066,
        Terminal.Constructor(Vector3(1748.6f, 5802.461f, 42.04353f), vehicle_terminal_combined),
        owning_building_guid = 27
      )
      LocalObject(
        1880,
        VehicleSpawnPad.Constructor(Vector3(1762.234f, 5802.075f, 37.88553f), mb_pad_creation, Vector3(0, 0, 92)),
        owning_building_guid = 27,
        terminal_guid = 3066
      )
      LocalObject(2652, ResourceSilo.Constructor(Vector3(1842.456f, 5685.076f, 46.75553f)), owning_building_guid = 27)
      LocalObject(
        2676,
        SpawnTube.Constructor(Vector3(1779.097f, 5718.897f, 29.73853f), Vector3(0, 0, 182)),
        owning_building_guid = 27
      )
      LocalObject(
        2677,
        SpawnTube.Constructor(Vector3(1779.352f, 5726.181f, 29.73853f), Vector3(0, 0, 182)),
        owning_building_guid = 27
      )
      LocalObject(
        2678,
        SpawnTube.Constructor(Vector3(1779.606f, 5733.468f, 29.73853f), Vector3(0, 0, 182)),
        owning_building_guid = 27
      )
      LocalObject(
        168,
        ProximityTerminal.Constructor(Vector3(1782.266f, 5745.173f, 39.54853f), adv_med_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1899,
        ProximityTerminal.Constructor(Vector3(1756.237f, 5735.015f, 29.73853f), medical_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2174,
        ProximityTerminal.Constructor(Vector3(1781.099f, 5692.821f, 50.07053f), pad_landing_frame),
        owning_building_guid = 27
      )
      LocalObject(
        2175,
        Terminal.Constructor(Vector3(1781.099f, 5692.821f, 50.07053f), air_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2177,
        ProximityTerminal.Constructor(Vector3(1788.217f, 5807.118f, 48.09053f), pad_landing_frame),
        owning_building_guid = 27
      )
      LocalObject(
        2178,
        Terminal.Constructor(Vector3(1788.217f, 5807.118f, 48.09053f), air_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2180,
        ProximityTerminal.Constructor(Vector3(1797.018f, 5683.862f, 48.08053f), pad_landing_frame),
        owning_building_guid = 27
      )
      LocalObject(
        2181,
        Terminal.Constructor(Vector3(1797.018f, 5683.862f, 48.08053f), air_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2183,
        ProximityTerminal.Constructor(Vector3(1804.268f, 5799.269f, 50.03153f), pad_landing_frame),
        owning_building_guid = 27
      )
      LocalObject(
        2184,
        Terminal.Constructor(Vector3(1804.268f, 5799.269f, 50.03153f), air_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2563,
        ProximityTerminal.Constructor(Vector3(1730.949f, 5723.982f, 39.48853f), repair_silo),
        owning_building_guid = 27
      )
      LocalObject(
        2564,
        Terminal.Constructor(Vector3(1730.949f, 5723.982f, 39.48853f), ground_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2567,
        ProximityTerminal.Constructor(Vector3(1822.415f, 5821.556f, 39.48853f), repair_silo),
        owning_building_guid = 27
      )
      LocalObject(
        2568,
        Terminal.Constructor(Vector3(1822.415f, 5821.556f, 39.48853f), ground_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1769,
        FacilityTurret.Constructor(Vector3(1717.883f, 5701.79f, 48.14053f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1769, 5026)
      LocalObject(
        1770,
        FacilityTurret.Constructor(Vector3(1723.785f, 5836.791f, 48.14053f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1770, 5027)
      LocalObject(
        1771,
        FacilityTurret.Constructor(Vector3(1759.5f, 5657.201f, 48.14053f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1771, 5028)
      LocalObject(
        1772,
        FacilityTurret.Constructor(Vector3(1852.756f, 5655.078f, 48.14053f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1772, 5029)
      LocalObject(
        1773,
        FacilityTurret.Constructor(Vector3(1858.928f, 5832.063f, 48.14053f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1773, 5030)
      LocalObject(
        946,
        ImplantTerminalMech.Constructor(Vector3(1771.868f, 5678.663f, 29.21553f)),
        owning_building_guid = 27
      )
      LocalObject(
        938,
        Terminal.Constructor(Vector3(1771.868f, 5678.681f, 29.21553f), implant_terminal_interface),
        owning_building_guid = 27
      )
      TerminalToInterface(946, 938)
      LocalObject(
        947,
        ImplantTerminalMech.Constructor(Vector3(1772.392f, 5694.01f, 29.21553f)),
        owning_building_guid = 27
      )
      LocalObject(
        939,
        Terminal.Constructor(Vector3(1772.391f, 5693.992f, 29.21553f), implant_terminal_interface),
        owning_building_guid = 27
      )
      TerminalToInterface(947, 939)
      LocalObject(
        2379,
        Painbox.Constructor(Vector3(1779.096f, 5757.85f, 53.76733f), painbox),
        owning_building_guid = 27
      )
      LocalObject(
        2391,
        Painbox.Constructor(Vector3(1768.987f, 5730.808f, 33.80843f), painbox_continuous),
        owning_building_guid = 27
      )
      LocalObject(
        2403,
        Painbox.Constructor(Vector3(1780.002f, 5743.345f, 53.97243f), painbox_door_radius),
        owning_building_guid = 27
      )
      LocalObject(
        2417,
        Painbox.Constructor(Vector3(1755.471f, 5719.935f, 33.27943f), painbox_door_radius_continuous),
        owning_building_guid = 27
      )
      LocalObject(
        2418,
        Painbox.Constructor(Vector3(1770.591f, 5712.622f, 31.45273f), painbox_door_radius_continuous),
        owning_building_guid = 27
      )
      LocalObject(
        2419,
        Painbox.Constructor(Vector3(1773.544f, 5740.573f, 32.09443f), painbox_door_radius_continuous),
        owning_building_guid = 27
      )
      LocalObject(301, Generator.Constructor(Vector3(1780.8f, 5761.681f, 48.44453f)), owning_building_guid = 27)
      LocalObject(
        289,
        Terminal.Constructor(Vector3(1780.561f, 5753.493f, 49.73853f), gen_control),
        owning_building_guid = 27
      )
    }

    Building10()

    def Building10(): Unit = { // Name: Hanish Type: cryo_facility GUID: 30, MapID: 10
      LocalBuilding(
        "Hanish",
        30,
        10,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3738f, 5482f, 88.62722f),
            Vector3(0f, 0f, 269f),
            cryo_facility
          )
        )
      )
      LocalObject(
        223,
        CaptureTerminal.Constructor(Vector3(3797.747f, 5509.05f, 78.62722f), capture_terminal),
        owning_building_guid = 30
      )
      LocalObject(470, Door.Constructor(Vector3(3645.456f, 5451.61f, 90.14822f)), owning_building_guid = 30)
      LocalObject(471, Door.Constructor(Vector3(3654.676f, 5459.951f, 90.17822f)), owning_building_guid = 30)
      LocalObject(472, Door.Constructor(Vector3(3654.993f, 5478.141f, 98.14222f)), owning_building_guid = 30)
      LocalObject(473, Door.Constructor(Vector3(3686.426f, 5431.965f, 98.14222f)), owning_building_guid = 30)
      LocalObject(474, Door.Constructor(Vector3(3704.616f, 5431.648f, 90.17822f)), owning_building_guid = 30)
      LocalObject(475, Door.Constructor(Vector3(3741.067f, 5477.946f, 100.1482f)), owning_building_guid = 30)
      LocalObject(476, Door.Constructor(Vector3(3743.529f, 5540.89f, 90.17822f)), owning_building_guid = 30)
      LocalObject(481, Door.Constructor(Vector3(3758.276f, 5497.648f, 100.1482f)), owning_building_guid = 30)
      LocalObject(482, Door.Constructor(Vector3(3761.719f, 5540.572f, 98.14222f)), owning_building_guid = 30)
      LocalObject(483, Door.Constructor(Vector3(3806.214f, 5504.946f, 98.14222f)), owning_building_guid = 30)
      LocalObject(484, Door.Constructor(Vector3(3806.531f, 5523.136f, 90.17822f)), owning_building_guid = 30)
      LocalObject(763, Door.Constructor(Vector3(3650.223f, 5495.534f, 82.64822f)), owning_building_guid = 30)
      LocalObject(764, Door.Constructor(Vector3(3678.707f, 5523.041f, 82.64822f)), owning_building_guid = 30)
      LocalObject(765, Door.Constructor(Vector3(3682.358f, 5502.974f, 82.64822f)), owning_building_guid = 30)
      LocalObject(766, Door.Constructor(Vector3(3706.354f, 5502.556f, 80.14822f)), owning_building_guid = 30)
      LocalObject(767, Door.Constructor(Vector3(3710.004f, 5482.489f, 80.14822f)), owning_building_guid = 30)
      LocalObject(768, Door.Constructor(Vector3(3726.84f, 5530.202f, 80.14822f)), owning_building_guid = 30)
      LocalObject(769, Door.Constructor(Vector3(3729.931f, 5478.14f, 100.1482f)), owning_building_guid = 30)
      LocalObject(770, Door.Constructor(Vector3(3734f, 5482.07f, 80.14822f)), owning_building_guid = 30)
      LocalObject(771, Door.Constructor(Vector3(3737.791f, 5470.002f, 80.14822f)), owning_building_guid = 30)
      LocalObject(772, Door.Constructor(Vector3(3738.209f, 5493.998f, 80.14822f)), owning_building_guid = 30)
      LocalObject(773, Door.Constructor(Vector3(3738.628f, 5517.995f, 80.14822f)), owning_building_guid = 30)
      LocalObject(774, Door.Constructor(Vector3(3741.581f, 5457.934f, 72.64822f)), owning_building_guid = 30)
      LocalObject(775, Door.Constructor(Vector3(3757.578f, 5457.655f, 80.14822f)), owning_building_guid = 30)
      LocalObject(776, Door.Constructor(Vector3(3758.276f, 5497.648f, 80.14822f)), owning_building_guid = 30)
      LocalObject(777, Door.Constructor(Vector3(3758.276f, 5497.648f, 90.14822f)), owning_building_guid = 30)
      LocalObject(778, Door.Constructor(Vector3(3758.555f, 5513.646f, 80.14822f)), owning_building_guid = 30)
      LocalObject(779, Door.Constructor(Vector3(3761.787f, 5469.583f, 80.14822f)), owning_building_guid = 30)
      LocalObject(780, Door.Constructor(Vector3(3778.203f, 5493.3f, 80.14822f)), owning_building_guid = 30)
      LocalObject(781, Door.Constructor(Vector3(3778.622f, 5517.296f, 80.14822f)), owning_building_guid = 30)
      LocalObject(782, Door.Constructor(Vector3(3789.992f, 5481.092f, 80.14822f)), owning_building_guid = 30)
      LocalObject(783, Door.Constructor(Vector3(3806.269f, 5496.811f, 80.14822f)), owning_building_guid = 30)
      LocalObject(784, Door.Constructor(Vector3(3806.408f, 5504.81f, 80.14822f)), owning_building_guid = 30)
      LocalObject(785, Door.Constructor(Vector3(3806.548f, 5512.809f, 80.14822f)), owning_building_guid = 30)
      LocalObject(923, Door.Constructor(Vector3(3763.01f, 5477.571f, 90.91022f)), owning_building_guid = 30)
      LocalObject(932, Door.Constructor(Vector3(3737.791f, 5470.002f, 90.14822f)), owning_building_guid = 30)
      LocalObject(933, Door.Constructor(Vector3(3746.069f, 5485.86f, 90.14622f)), owning_building_guid = 30)
      LocalObject(2934, Door.Constructor(Vector3(3743.651f, 5477.228f, 80.48122f)), owning_building_guid = 30)
      LocalObject(2937, Door.Constructor(Vector3(3750.942f, 5477.101f, 80.48122f)), owning_building_guid = 30)
      LocalObject(2938, Door.Constructor(Vector3(3758.23f, 5476.973f, 80.48122f)), owning_building_guid = 30)
      LocalObject(
        971,
        IFFLock.Constructor(Vector3(3765.804f, 5480.745f, 90.10922f), Vector3(0, 0, 91)),
        owning_building_guid = 30,
        door_guid = 923
      )
      LocalObject(
        1105,
        IFFLock.Constructor(Vector3(3647.483f, 5450.761f, 90.07922f), Vector3(0, 0, 181)),
        owning_building_guid = 30,
        door_guid = 470
      )
      LocalObject(
        1106,
        IFFLock.Constructor(Vector3(3736.953f, 5468.444f, 79.96322f), Vector3(0, 0, 271)),
        owning_building_guid = 30,
        door_guid = 771
      )
      LocalObject(
        1107,
        IFFLock.Constructor(Vector3(3740.023f, 5458.771f, 72.46322f), Vector3(0, 0, 1)),
        owning_building_guid = 30,
        door_guid = 774
      )
      LocalObject(
        1108,
        IFFLock.Constructor(Vector3(3741.923f, 5479.977f, 100.0792f), Vector3(0, 0, 91)),
        owning_building_guid = 30,
        door_guid = 475
      )
      LocalObject(
        1113,
        IFFLock.Constructor(Vector3(3760.305f, 5496.799f, 100.0792f), Vector3(0, 0, 181)),
        owning_building_guid = 30,
        door_guid = 481
      )
      LocalObject(
        1114,
        IFFLock.Constructor(Vector3(3762.624f, 5471.141f, 79.96322f), Vector3(0, 0, 91)),
        owning_building_guid = 30,
        door_guid = 779
      )
      LocalObject(
        1115,
        IFFLock.Constructor(Vector3(3804.993f, 5513.776f, 79.96322f), Vector3(0, 0, 1)),
        owning_building_guid = 30,
        door_guid = 785
      )
      LocalObject(
        1116,
        IFFLock.Constructor(Vector3(3807.966f, 5503.969f, 79.96322f), Vector3(0, 0, 181)),
        owning_building_guid = 30,
        door_guid = 784
      )
      LocalObject(1452, Locker.Constructor(Vector3(3739.695f, 5443.867f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1453, Locker.Constructor(Vector3(3739.713f, 5444.921f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1454, Locker.Constructor(Vector3(3739.732f, 5445.981f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1455, Locker.Constructor(Vector3(3739.75f, 5447.036f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1456, Locker.Constructor(Vector3(3739.769f, 5448.091f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1457, Locker.Constructor(Vector3(3739.787f, 5449.147f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1458, Locker.Constructor(Vector3(3739.809f, 5462.942f, 78.88822f)), owning_building_guid = 30)
      LocalObject(1459, Locker.Constructor(Vector3(3739.829f, 5464.091f, 78.88822f)), owning_building_guid = 30)
      LocalObject(1460, Locker.Constructor(Vector3(3739.849f, 5465.238f, 78.88822f)), owning_building_guid = 30)
      LocalObject(1461, Locker.Constructor(Vector3(3739.869f, 5466.402f, 78.88822f)), owning_building_guid = 30)
      LocalObject(1470, Locker.Constructor(Vector3(3759.693f, 5443.521f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1471, Locker.Constructor(Vector3(3759.711f, 5444.576f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1472, Locker.Constructor(Vector3(3759.729f, 5445.631f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1473, Locker.Constructor(Vector3(3759.748f, 5446.686f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1474, Locker.Constructor(Vector3(3759.767f, 5447.747f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1475, Locker.Constructor(Vector3(3759.785f, 5448.801f, 78.53522f)), owning_building_guid = 30)
      LocalObject(1476, Locker.Constructor(Vector3(3787.139f, 5461.142f, 78.62222f)), owning_building_guid = 30)
      LocalObject(1477, Locker.Constructor(Vector3(3788.395f, 5461.12f, 78.62222f)), owning_building_guid = 30)
      LocalObject(1478, Locker.Constructor(Vector3(3789.656f, 5461.098f, 78.62222f)), owning_building_guid = 30)
      LocalObject(1479, Locker.Constructor(Vector3(3790.918f, 5461.076f, 78.62222f)), owning_building_guid = 30)
      LocalObject(1480, Locker.Constructor(Vector3(3792.17f, 5461.054f, 78.62222f)), owning_building_guid = 30)
      LocalObject(1723, Locker.Constructor(Vector3(3725.47f, 5463.956f, 88.62722f)), owning_building_guid = 30)
      LocalObject(1724, Locker.Constructor(Vector3(3726.504f, 5463.938f, 88.62722f)), owning_building_guid = 30)
      LocalObject(1725, Locker.Constructor(Vector3(3729.021f, 5463.894f, 88.39822f)), owning_building_guid = 30)
      LocalObject(1726, Locker.Constructor(Vector3(3730.054f, 5463.876f, 88.39822f)), owning_building_guid = 30)
      LocalObject(1727, Locker.Constructor(Vector3(3731.108f, 5463.857f, 88.39822f)), owning_building_guid = 30)
      LocalObject(1728, Locker.Constructor(Vector3(3732.142f, 5463.839f, 88.39822f)), owning_building_guid = 30)
      LocalObject(1729, Locker.Constructor(Vector3(3734.664f, 5463.795f, 88.62722f)), owning_building_guid = 30)
      LocalObject(1730, Locker.Constructor(Vector3(3735.698f, 5463.777f, 88.62722f)), owning_building_guid = 30)
      LocalObject(
        238,
        Terminal.Constructor(Vector3(3784.508f, 5464.762f, 78.61722f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        239,
        Terminal.Constructor(Vector3(3784.73f, 5477.46f, 78.61722f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        240,
        Terminal.Constructor(Vector3(3785.931f, 5463.289f, 78.61722f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        241,
        Terminal.Constructor(Vector3(3786.203f, 5478.882f, 78.61722f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        242,
        Terminal.Constructor(Vector3(3793.255f, 5463.161f, 78.61722f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        243,
        Terminal.Constructor(Vector3(3793.527f, 5478.754f, 78.61722f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        244,
        Terminal.Constructor(Vector3(3794.728f, 5464.583f, 78.61722f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        245,
        Terminal.Constructor(Vector3(3794.949f, 5477.281f, 78.61722f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1991,
        Terminal.Constructor(Vector3(3745.081f, 5463.22f, 80.21722f), order_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1992,
        Terminal.Constructor(Vector3(3747.699f, 5491.86f, 89.92222f), order_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1993,
        Terminal.Constructor(Vector3(3748.812f, 5463.154f, 80.21722f), order_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1994,
        Terminal.Constructor(Vector3(3752.6f, 5463.088f, 80.21722f), order_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2840,
        Terminal.Constructor(Vector3(3681.699f, 5499.075f, 82.74022f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2841,
        Terminal.Constructor(Vector3(3724.328f, 5480.333f, 90.20622f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2842,
        Terminal.Constructor(Vector3(3738.105f, 5522.004f, 80.24022f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2843,
        Terminal.Constructor(Vector3(3741.156f, 5476.973f, 80.76122f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2844,
        Terminal.Constructor(Vector3(3748.447f, 5476.85f, 80.76122f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2845,
        Terminal.Constructor(Vector3(3755.734f, 5476.72f, 80.76122f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2846,
        Terminal.Constructor(Vector3(3802f, 5481.476f, 80.24022f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        3070,
        Terminal.Constructor(Vector3(3674.167f, 5445.48f, 90.93222f), vehicle_terminal_combined),
        owning_building_guid = 30
      )
      LocalObject(
        1886,
        VehicleSpawnPad.Constructor(Vector3(3674.315f, 5459.119f, 86.77422f), mb_pad_creation, Vector3(0, 0, 1)),
        owning_building_guid = 30,
        terminal_guid = 3070
      )
      LocalObject(2656, ResourceSilo.Constructor(Vector3(3789.896f, 5541.37f, 95.64422f)), owning_building_guid = 30)
      LocalObject(
        2710,
        SpawnTube.Constructor(Vector3(3742.608f, 5477.686f, 78.62722f), Vector3(0, 0, 91)),
        owning_building_guid = 30
      )
      LocalObject(
        2713,
        SpawnTube.Constructor(Vector3(3749.898f, 5477.559f, 78.62722f), Vector3(0, 0, 91)),
        owning_building_guid = 30
      )
      LocalObject(
        2714,
        SpawnTube.Constructor(Vector3(3757.185f, 5477.432f, 78.62722f), Vector3(0, 0, 91)),
        owning_building_guid = 30
      )
      LocalObject(
        169,
        ProximityTerminal.Constructor(Vector3(3730.858f, 5480.141f, 88.43722f), adv_med_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1906,
        ProximityTerminal.Constructor(Vector3(3741.469f, 5454.293f, 78.62722f), medical_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2228,
        ProximityTerminal.Constructor(Vector3(3668.819f, 5485.01f, 96.97922f), pad_landing_frame),
        owning_building_guid = 30
      )
      LocalObject(
        2229,
        Terminal.Constructor(Vector3(3668.819f, 5485.01f, 96.97922f), air_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2231,
        ProximityTerminal.Constructor(Vector3(3676.387f, 5501.195f, 98.92022f), pad_landing_frame),
        owning_building_guid = 30
      )
      LocalObject(
        2232,
        Terminal.Constructor(Vector3(3676.387f, 5501.195f, 98.92022f), air_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2234,
        ProximityTerminal.Constructor(Vector3(3783.223f, 5479.887f, 98.95922f), pad_landing_frame),
        owning_building_guid = 30
      )
      LocalObject(
        2235,
        Terminal.Constructor(Vector3(3783.223f, 5479.887f, 98.95922f), air_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2237,
        ProximityTerminal.Constructor(Vector3(3791.903f, 5495.96f, 96.96922f), pad_landing_frame),
        owning_building_guid = 30
      )
      LocalObject(
        2238,
        Terminal.Constructor(Vector3(3791.903f, 5495.96f, 96.96922f), air_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2595,
        ProximityTerminal.Constructor(Vector3(3653.786f, 5518.951f, 88.37722f), repair_silo),
        owning_building_guid = 30
      )
      LocalObject(
        2596,
        Terminal.Constructor(Vector3(3653.786f, 5518.951f, 88.37722f), ground_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2599,
        ProximityTerminal.Constructor(Vector3(3752.942f, 5429.201f, 88.37722f), repair_silo),
        owning_building_guid = 30
      )
      LocalObject(
        2600,
        Terminal.Constructor(Vector3(3752.942f, 5429.201f, 88.37722f), ground_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1812,
        FacilityTurret.Constructor(Vector3(3640.275f, 5420.07f, 97.02922f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1812, 5031)
      LocalObject(
        1813,
        FacilityTurret.Constructor(Vector3(3642.643f, 5555.275f, 97.02922f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1813, 5032)
      LocalObject(
        1816,
        FacilityTurret.Constructor(Vector3(3775.359f, 5416.525f, 97.02922f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1816, 5033)
      LocalObject(
        1817,
        FacilityTurret.Constructor(Vector3(3819.214f, 5458.914f, 97.02922f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1817, 5034)
      LocalObject(
        1818,
        FacilityTurret.Constructor(Vector3(3819.709f, 5552.193f, 97.02922f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1818, 5035)
      LocalObject(
        948,
        ImplantTerminalMech.Constructor(Vector3(3782.186f, 5471.161f, 78.10422f)),
        owning_building_guid = 30
      )
      LocalObject(
        940,
        Terminal.Constructor(Vector3(3782.204f, 5471.161f, 78.10422f), implant_terminal_interface),
        owning_building_guid = 30
      )
      TerminalToInterface(948, 940)
      LocalObject(
        949,
        ImplantTerminalMech.Constructor(Vector3(3797.54f, 5470.905f, 78.10422f)),
        owning_building_guid = 30
      )
      LocalObject(
        941,
        Terminal.Constructor(Vector3(3797.521f, 5470.905f, 78.10422f), implant_terminal_interface),
        owning_building_guid = 30
      )
      TerminalToInterface(949, 941)
      LocalObject(2383, Painbox.Constructor(Vector3(3718.239f, 5476.75f, 102.656f), painbox), owning_building_guid = 30)
      LocalObject(
        2395,
        Painbox.Constructor(Vector3(3745.453f, 5467.115f, 82.69712f), painbox_continuous),
        owning_building_guid = 30
      )
      LocalObject(
        2407,
        Painbox.Constructor(Vector3(3732.726f, 5477.909f, 102.8611f), painbox_door_radius),
        owning_building_guid = 30
      )
      LocalObject(
        2429,
        Painbox.Constructor(Vector3(3735.61f, 5471.5f, 80.98312f), painbox_door_radius_continuous),
        owning_building_guid = 30
      )
      LocalObject(
        2430,
        Painbox.Constructor(Vector3(3756.561f, 5453.79f, 82.16812f), painbox_door_radius_continuous),
        owning_building_guid = 30
      )
      LocalObject(
        2431,
        Painbox.Constructor(Vector3(3763.609f, 5469.036f, 80.34142f), painbox_door_radius_continuous),
        owning_building_guid = 30
      )
      LocalObject(305, Generator.Constructor(Vector3(3714.378f, 5478.387f, 97.33322f)), owning_building_guid = 30)
      LocalObject(
        293,
        Terminal.Constructor(Vector3(3722.57f, 5478.291f, 98.62722f), gen_control),
        owning_building_guid = 30
      )
    }

    Building16()

    def Building16(): Unit = { // Name: Zaqar Type: cryo_facility GUID: 33, MapID: 16
      LocalBuilding(
        "Zaqar",
        33,
        16,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4740f, 2128f, 74.54427f),
            Vector3(0f, 0f, 359f),
            cryo_facility
          )
        )
      )
      LocalObject(
        226,
        CaptureTerminal.Constructor(Vector3(4712.95f, 2187.747f, 64.54427f), capture_terminal),
        owning_building_guid = 33
      )
      LocalObject(552, Door.Constructor(Vector3(4681.11f, 2133.529f, 76.09527f)), owning_building_guid = 33)
      LocalObject(553, Door.Constructor(Vector3(4681.428f, 2151.719f, 84.05927f)), owning_building_guid = 33)
      LocalObject(554, Door.Constructor(Vector3(4698.864f, 2196.531f, 76.09527f)), owning_building_guid = 33)
      LocalObject(555, Door.Constructor(Vector3(4717.054f, 2196.214f, 84.05927f)), owning_building_guid = 33)
      LocalObject(556, Door.Constructor(Vector3(4724.352f, 2148.276f, 86.06526f)), owning_building_guid = 33)
      LocalObject(558, Door.Constructor(Vector3(4743.859f, 2044.993f, 84.05927f)), owning_building_guid = 33)
      LocalObject(559, Door.Constructor(Vector3(4744.054f, 2131.067f, 86.06526f)), owning_building_guid = 33)
      LocalObject(560, Door.Constructor(Vector3(4762.049f, 2044.676f, 76.09527f)), owning_building_guid = 33)
      LocalObject(561, Door.Constructor(Vector3(4770.39f, 2035.456f, 76.06527f)), owning_building_guid = 33)
      LocalObject(564, Door.Constructor(Vector3(4790.035f, 2076.426f, 84.05927f)), owning_building_guid = 33)
      LocalObject(565, Door.Constructor(Vector3(4790.352f, 2094.616f, 76.09527f)), owning_building_guid = 33)
      LocalObject(829, Door.Constructor(Vector3(4691.798f, 2116.84f, 66.06526f)), owning_building_guid = 33)
      LocalObject(830, Door.Constructor(Vector3(4698.959f, 2068.707f, 68.56526f)), owning_building_guid = 33)
      LocalObject(831, Door.Constructor(Vector3(4704.005f, 2128.628f, 66.06526f)), owning_building_guid = 33)
      LocalObject(832, Door.Constructor(Vector3(4704.704f, 2168.622f, 66.06526f)), owning_building_guid = 33)
      LocalObject(833, Door.Constructor(Vector3(4708.354f, 2148.555f, 66.06526f)), owning_building_guid = 33)
      LocalObject(834, Door.Constructor(Vector3(4709.191f, 2196.548f, 66.06526f)), owning_building_guid = 33)
      LocalObject(835, Door.Constructor(Vector3(4717.19f, 2196.408f, 66.06526f)), owning_building_guid = 33)
      LocalObject(836, Door.Constructor(Vector3(4719.026f, 2072.358f, 68.56526f)), owning_building_guid = 33)
      LocalObject(837, Door.Constructor(Vector3(4719.444f, 2096.354f, 66.06526f)), owning_building_guid = 33)
      LocalObject(838, Door.Constructor(Vector3(4724.352f, 2148.276f, 66.06526f)), owning_building_guid = 33)
      LocalObject(839, Door.Constructor(Vector3(4724.352f, 2148.276f, 76.06527f)), owning_building_guid = 33)
      LocalObject(840, Door.Constructor(Vector3(4725.189f, 2196.269f, 66.06526f)), owning_building_guid = 33)
      LocalObject(841, Door.Constructor(Vector3(4726.466f, 2040.223f, 68.56526f)), owning_building_guid = 33)
      LocalObject(842, Door.Constructor(Vector3(4728.002f, 2128.209f, 66.06526f)), owning_building_guid = 33)
      LocalObject(843, Door.Constructor(Vector3(4728.7f, 2168.203f, 66.06526f)), owning_building_guid = 33)
      LocalObject(844, Door.Constructor(Vector3(4739.511f, 2100.004f, 66.06526f)), owning_building_guid = 33)
      LocalObject(845, Door.Constructor(Vector3(4739.93f, 2124f, 66.06526f)), owning_building_guid = 33)
      LocalObject(846, Door.Constructor(Vector3(4740.908f, 2179.992f, 66.06526f)), owning_building_guid = 33)
      LocalObject(847, Door.Constructor(Vector3(4743.86f, 2119.931f, 86.06526f)), owning_building_guid = 33)
      LocalObject(848, Door.Constructor(Vector3(4751.998f, 2127.791f, 66.06526f)), owning_building_guid = 33)
      LocalObject(849, Door.Constructor(Vector3(4752.417f, 2151.787f, 66.06526f)), owning_building_guid = 33)
      LocalObject(850, Door.Constructor(Vector3(4764.066f, 2131.581f, 58.56527f)), owning_building_guid = 33)
      LocalObject(851, Door.Constructor(Vector3(4764.345f, 2147.578f, 66.06526f)), owning_building_guid = 33)
      LocalObject(926, Door.Constructor(Vector3(4744.429f, 2153.01f, 76.82726f)), owning_building_guid = 33)
      LocalObject(934, Door.Constructor(Vector3(4736.14f, 2136.069f, 76.06326f)), owning_building_guid = 33)
      LocalObject(935, Door.Constructor(Vector3(4751.998f, 2127.791f, 76.06527f)), owning_building_guid = 33)
      LocalObject(2961, Door.Constructor(Vector3(4744.772f, 2133.651f, 66.39827f)), owning_building_guid = 33)
      LocalObject(2962, Door.Constructor(Vector3(4744.899f, 2140.942f, 66.39827f)), owning_building_guid = 33)
      LocalObject(2963, Door.Constructor(Vector3(4745.027f, 2148.23f, 66.39827f)), owning_building_guid = 33)
      LocalObject(
        974,
        IFFLock.Constructor(Vector3(4741.255f, 2155.804f, 76.02627f), Vector3(0, 0, 1)),
        owning_building_guid = 33,
        door_guid = 926
      )
      LocalObject(
        1174,
        IFFLock.Constructor(Vector3(4708.224f, 2194.993f, 65.88026f), Vector3(0, 0, 271)),
        owning_building_guid = 33,
        door_guid = 834
      )
      LocalObject(
        1175,
        IFFLock.Constructor(Vector3(4718.031f, 2197.966f, 65.88026f), Vector3(0, 0, 91)),
        owning_building_guid = 33,
        door_guid = 835
      )
      LocalObject(
        1176,
        IFFLock.Constructor(Vector3(4725.201f, 2150.305f, 85.99626f), Vector3(0, 0, 91)),
        owning_building_guid = 33,
        door_guid = 556
      )
      LocalObject(
        1177,
        IFFLock.Constructor(Vector3(4742.023f, 2131.923f, 85.99626f), Vector3(0, 0, 1)),
        owning_building_guid = 33,
        door_guid = 559
      )
      LocalObject(
        1178,
        IFFLock.Constructor(Vector3(4750.859f, 2152.624f, 65.88026f), Vector3(0, 0, 1)),
        owning_building_guid = 33,
        door_guid = 849
      )
      LocalObject(
        1179,
        IFFLock.Constructor(Vector3(4753.556f, 2126.953f, 65.88026f), Vector3(0, 0, 181)),
        owning_building_guid = 33,
        door_guid = 848
      )
      LocalObject(
        1180,
        IFFLock.Constructor(Vector3(4763.229f, 2130.023f, 58.38026f), Vector3(0, 0, 271)),
        owning_building_guid = 33,
        door_guid = 850
      )
      LocalObject(
        1181,
        IFFLock.Constructor(Vector3(4771.239f, 2037.483f, 75.99627f), Vector3(0, 0, 91)),
        owning_building_guid = 33,
        door_guid = 561
      )
      LocalObject(1569, Locker.Constructor(Vector3(4755.598f, 2129.869f, 64.80527f)), owning_building_guid = 33)
      LocalObject(1570, Locker.Constructor(Vector3(4756.762f, 2129.849f, 64.80527f)), owning_building_guid = 33)
      LocalObject(1571, Locker.Constructor(Vector3(4757.909f, 2129.829f, 64.80527f)), owning_building_guid = 33)
      LocalObject(1572, Locker.Constructor(Vector3(4759.058f, 2129.809f, 64.80527f)), owning_building_guid = 33)
      LocalObject(1573, Locker.Constructor(Vector3(4760.858f, 2177.139f, 64.53927f)), owning_building_guid = 33)
      LocalObject(1574, Locker.Constructor(Vector3(4760.88f, 2178.395f, 64.53927f)), owning_building_guid = 33)
      LocalObject(1575, Locker.Constructor(Vector3(4760.902f, 2179.656f, 64.53927f)), owning_building_guid = 33)
      LocalObject(1576, Locker.Constructor(Vector3(4760.924f, 2180.918f, 64.53927f)), owning_building_guid = 33)
      LocalObject(1577, Locker.Constructor(Vector3(4760.946f, 2182.17f, 64.53927f)), owning_building_guid = 33)
      LocalObject(1578, Locker.Constructor(Vector3(4772.853f, 2129.787f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1579, Locker.Constructor(Vector3(4773.199f, 2149.785f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1580, Locker.Constructor(Vector3(4773.909f, 2129.769f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1581, Locker.Constructor(Vector3(4774.253f, 2149.767f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1582, Locker.Constructor(Vector3(4774.964f, 2129.75f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1583, Locker.Constructor(Vector3(4775.314f, 2149.748f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1584, Locker.Constructor(Vector3(4776.019f, 2129.732f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1585, Locker.Constructor(Vector3(4776.369f, 2149.729f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1586, Locker.Constructor(Vector3(4777.079f, 2129.713f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1587, Locker.Constructor(Vector3(4777.424f, 2149.711f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1588, Locker.Constructor(Vector3(4778.133f, 2129.695f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1589, Locker.Constructor(Vector3(4778.479f, 2149.693f, 64.45226f)), owning_building_guid = 33)
      LocalObject(1731, Locker.Constructor(Vector3(4758.044f, 2115.47f, 74.54427f)), owning_building_guid = 33)
      LocalObject(1732, Locker.Constructor(Vector3(4758.062f, 2116.504f, 74.54427f)), owning_building_guid = 33)
      LocalObject(1733, Locker.Constructor(Vector3(4758.106f, 2119.021f, 74.31527f)), owning_building_guid = 33)
      LocalObject(1734, Locker.Constructor(Vector3(4758.124f, 2120.054f, 74.31527f)), owning_building_guid = 33)
      LocalObject(1735, Locker.Constructor(Vector3(4758.143f, 2121.108f, 74.31527f)), owning_building_guid = 33)
      LocalObject(1736, Locker.Constructor(Vector3(4758.161f, 2122.142f, 74.31527f)), owning_building_guid = 33)
      LocalObject(1737, Locker.Constructor(Vector3(4758.205f, 2124.664f, 74.54427f)), owning_building_guid = 33)
      LocalObject(1738, Locker.Constructor(Vector3(4758.223f, 2125.698f, 74.54427f)), owning_building_guid = 33)
      LocalObject(
        246,
        Terminal.Constructor(Vector3(4743.118f, 2176.203f, 64.53426f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        247,
        Terminal.Constructor(Vector3(4743.246f, 2183.527f, 64.53426f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        248,
        Terminal.Constructor(Vector3(4744.54f, 2174.73f, 64.53426f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        249,
        Terminal.Constructor(Vector3(4744.719f, 2184.949f, 64.53426f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        250,
        Terminal.Constructor(Vector3(4757.238f, 2174.508f, 64.53426f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        251,
        Terminal.Constructor(Vector3(4757.417f, 2184.728f, 64.53426f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        252,
        Terminal.Constructor(Vector3(4758.711f, 2175.931f, 64.53426f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        253,
        Terminal.Constructor(Vector3(4758.839f, 2183.255f, 64.53426f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2033,
        Terminal.Constructor(Vector3(4730.14f, 2137.699f, 75.83926f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2034,
        Terminal.Constructor(Vector3(4758.78f, 2135.081f, 66.13426f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2035,
        Terminal.Constructor(Vector3(4758.846f, 2138.812f, 66.13426f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2036,
        Terminal.Constructor(Vector3(4758.912f, 2142.6f, 66.13426f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2860,
        Terminal.Constructor(Vector3(4699.996f, 2128.105f, 66.15726f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2861,
        Terminal.Constructor(Vector3(4722.925f, 2071.699f, 68.65726f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2862,
        Terminal.Constructor(Vector3(4740.524f, 2192f, 66.15726f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2863,
        Terminal.Constructor(Vector3(4741.667f, 2114.328f, 76.12327f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2864,
        Terminal.Constructor(Vector3(4745.027f, 2131.156f, 66.67827f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2865,
        Terminal.Constructor(Vector3(4745.15f, 2138.447f, 66.67827f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2866,
        Terminal.Constructor(Vector3(4745.28f, 2145.734f, 66.67827f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3073,
        Terminal.Constructor(Vector3(4776.52f, 2064.167f, 76.84927f), vehicle_terminal_combined),
        owning_building_guid = 33
      )
      LocalObject(
        1891,
        VehicleSpawnPad.Constructor(Vector3(4762.881f, 2064.315f, 72.69127f), mb_pad_creation, Vector3(0, 0, -89)),
        owning_building_guid = 33,
        terminal_guid = 3073
      )
      LocalObject(2659, ResourceSilo.Constructor(Vector3(4680.63f, 2179.896f, 81.56126f)), owning_building_guid = 33)
      LocalObject(
        2737,
        SpawnTube.Constructor(Vector3(4744.314f, 2132.608f, 64.54427f), Vector3(0, 0, 1)),
        owning_building_guid = 33
      )
      LocalObject(
        2738,
        SpawnTube.Constructor(Vector3(4744.441f, 2139.898f, 64.54427f), Vector3(0, 0, 1)),
        owning_building_guid = 33
      )
      LocalObject(
        2739,
        SpawnTube.Constructor(Vector3(4744.568f, 2147.185f, 64.54427f), Vector3(0, 0, 1)),
        owning_building_guid = 33
      )
      LocalObject(
        170,
        ProximityTerminal.Constructor(Vector3(4741.859f, 2120.858f, 74.35426f), adv_med_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1911,
        ProximityTerminal.Constructor(Vector3(4767.707f, 2131.469f, 64.54427f), medical_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2261,
        ProximityTerminal.Constructor(Vector3(4720.805f, 2066.387f, 84.83727f), pad_landing_frame),
        owning_building_guid = 33
      )
      LocalObject(
        2262,
        Terminal.Constructor(Vector3(4720.805f, 2066.387f, 84.83727f), air_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2264,
        ProximityTerminal.Constructor(Vector3(4726.04f, 2181.903f, 82.88627f), pad_landing_frame),
        owning_building_guid = 33
      )
      LocalObject(
        2265,
        Terminal.Constructor(Vector3(4726.04f, 2181.903f, 82.88627f), air_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2267,
        ProximityTerminal.Constructor(Vector3(4736.99f, 2058.819f, 82.89626f), pad_landing_frame),
        owning_building_guid = 33
      )
      LocalObject(
        2268,
        Terminal.Constructor(Vector3(4736.99f, 2058.819f, 82.89626f), air_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2270,
        ProximityTerminal.Constructor(Vector3(4742.113f, 2173.223f, 84.87627f), pad_landing_frame),
        owning_building_guid = 33
      )
      LocalObject(
        2271,
        Terminal.Constructor(Vector3(4742.113f, 2173.223f, 84.87627f), air_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2619,
        ProximityTerminal.Constructor(Vector3(4703.049f, 2043.786f, 74.29427f), repair_silo),
        owning_building_guid = 33
      )
      LocalObject(
        2620,
        Terminal.Constructor(Vector3(4703.049f, 2043.786f, 74.29427f), ground_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2627,
        ProximityTerminal.Constructor(Vector3(4792.799f, 2142.942f, 74.29427f), repair_silo),
        owning_building_guid = 33
      )
      LocalObject(
        2628,
        Terminal.Constructor(Vector3(4792.799f, 2142.942f, 74.29427f), ground_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1841,
        FacilityTurret.Constructor(Vector3(4666.725f, 2032.643f, 82.94627f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1841, 5036)
      LocalObject(
        1842,
        FacilityTurret.Constructor(Vector3(4669.807f, 2209.709f, 82.94627f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1842, 5037)
      LocalObject(
        1845,
        FacilityTurret.Constructor(Vector3(4763.086f, 2209.214f, 82.94627f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1845, 5038)
      LocalObject(
        1847,
        FacilityTurret.Constructor(Vector3(4801.93f, 2030.275f, 82.94627f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1847, 5039)
      LocalObject(
        1848,
        FacilityTurret.Constructor(Vector3(4805.475f, 2165.359f, 82.94627f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1848, 5040)
      LocalObject(
        950,
        ImplantTerminalMech.Constructor(Vector3(4750.839f, 2172.186f, 64.02126f)),
        owning_building_guid = 33
      )
      LocalObject(
        942,
        Terminal.Constructor(Vector3(4750.839f, 2172.204f, 64.02126f), implant_terminal_interface),
        owning_building_guid = 33
      )
      TerminalToInterface(950, 942)
      LocalObject(
        951,
        ImplantTerminalMech.Constructor(Vector3(4751.095f, 2187.54f, 64.02126f)),
        owning_building_guid = 33
      )
      LocalObject(
        943,
        Terminal.Constructor(Vector3(4751.095f, 2187.521f, 64.02126f), implant_terminal_interface),
        owning_building_guid = 33
      )
      TerminalToInterface(951, 943)
      LocalObject(
        2386,
        Painbox.Constructor(Vector3(4745.25f, 2108.239f, 88.57307f), painbox),
        owning_building_guid = 33
      )
      LocalObject(
        2398,
        Painbox.Constructor(Vector3(4754.885f, 2135.453f, 68.61417f), painbox_continuous),
        owning_building_guid = 33
      )
      LocalObject(
        2410,
        Painbox.Constructor(Vector3(4744.091f, 2122.726f, 88.77817f), painbox_door_radius),
        owning_building_guid = 33
      )
      LocalObject(
        2438,
        Painbox.Constructor(Vector3(4750.5f, 2125.61f, 66.90016f), painbox_door_radius_continuous),
        owning_building_guid = 33
      )
      LocalObject(
        2439,
        Painbox.Constructor(Vector3(4752.964f, 2153.609f, 66.25847f), painbox_door_radius_continuous),
        owning_building_guid = 33
      )
      LocalObject(
        2440,
        Painbox.Constructor(Vector3(4768.21f, 2146.561f, 68.08517f), painbox_door_radius_continuous),
        owning_building_guid = 33
      )
      LocalObject(308, Generator.Constructor(Vector3(4743.613f, 2104.378f, 83.25027f)), owning_building_guid = 33)
      LocalObject(
        296,
        Terminal.Constructor(Vector3(4743.709f, 2112.57f, 84.54427f), gen_control),
        owning_building_guid = 33
      )
    }

    Building13()

    def Building13(): Unit = { // Name: Lahar Type: cryo_facility GUID: 36, MapID: 13
      LocalBuilding(
        "Lahar",
        36,
        13,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(7044f, 5314f, 37.85578f),
            Vector3(0f, 0f, 136f),
            cryo_facility
          )
        )
      )
      LocalObject(
        229,
        CaptureTerminal.Constructor(Vector3(7023.036f, 5251.855f, 27.85578f), capture_terminal),
        owning_building_guid = 36
      )
      LocalObject(634, Door.Constructor(Vector3(7014.26f, 5248.462f, 47.37078f)), owning_building_guid = 36)
      LocalObject(635, Door.Constructor(Vector3(7027.347f, 5235.825f, 39.40678f)), owning_building_guid = 36)
      LocalObject(636, Door.Constructor(Vector3(7029.942f, 5372.755f, 39.40678f)), owning_building_guid = 36)
      LocalObject(641, Door.Constructor(Vector3(7038.943f, 5314.522f, 49.37678f)), owning_building_guid = 36)
      LocalObject(642, Door.Constructor(Vector3(7041.616f, 5288.499f, 49.37678f)), owning_building_guid = 36)
      LocalObject(643, Door.Constructor(Vector3(7042.581f, 5385.842f, 47.37078f)), owning_building_guid = 36)
      LocalObject(644, Door.Constructor(Vector3(7070.661f, 5256.707f, 47.37078f)), owning_building_guid = 36)
      LocalObject(645, Door.Constructor(Vector3(7083.298f, 5269.794f, 39.40678f)), owning_building_guid = 36)
      LocalObject(646, Door.Constructor(Vector3(7084.702f, 5389.977f, 39.40678f)), owning_building_guid = 36)
      LocalObject(647, Door.Constructor(Vector3(7084.89f, 5402.408f, 39.37678f)), owning_building_guid = 36)
      LocalObject(649, Door.Constructor(Vector3(7097.789f, 5377.339f, 47.37078f)), owning_building_guid = 36)
      LocalObject(895, Door.Constructor(Vector3(7007.878f, 5276.594f, 29.37678f)), owning_building_guid = 36)
      LocalObject(896, Door.Constructor(Vector3(7008.272f, 5253.97f, 29.37678f)), owning_building_guid = 36)
      LocalObject(897, Door.Constructor(Vector3(7012.843f, 5316.285f, 29.37678f)), owning_building_guid = 36)
      LocalObject(898, Door.Constructor(Vector3(7014.027f, 5248.413f, 29.37678f)), owning_building_guid = 36)
      LocalObject(899, Door.Constructor(Vector3(7018.696f, 5305.072f, 29.37678f)), owning_building_guid = 36)
      LocalObject(900, Door.Constructor(Vector3(7019.782f, 5242.856f, 29.37678f)), owning_building_guid = 36)
      LocalObject(901, Door.Constructor(Vector3(7023.957f, 5327.794f, 21.87678f)), owning_building_guid = 36)
      LocalObject(902, Door.Constructor(Vector3(7024.846f, 5276.891f, 29.37678f)), owning_building_guid = 36)
      LocalObject(903, Door.Constructor(Vector3(7035.368f, 5322.336f, 29.37678f)), owning_building_guid = 36)
      LocalObject(904, Door.Constructor(Vector3(7041.616f, 5288.499f, 29.37678f)), owning_building_guid = 36)
      LocalObject(905, Door.Constructor(Vector3(7041.616f, 5288.499f, 39.37678f)), owning_building_guid = 36)
      LocalObject(906, Door.Constructor(Vector3(7042.11f, 5260.219f, 29.37678f)), owning_building_guid = 36)
      LocalObject(907, Door.Constructor(Vector3(7046.68f, 5322.533f, 49.37678f)), owning_building_guid = 36)
      LocalObject(908, Door.Constructor(Vector3(7046.779f, 5316.877f, 29.37678f)), owning_building_guid = 36)
      LocalObject(909, Door.Constructor(Vector3(7052.632f, 5305.664f, 29.37678f)), owning_building_guid = 36)
      LocalObject(910, Door.Constructor(Vector3(7053.125f, 5277.384f, 29.37678f)), owning_building_guid = 36)
      LocalObject(911, Door.Constructor(Vector3(7063.45f, 5334.142f, 29.37678f)), owning_building_guid = 36)
      LocalObject(912, Door.Constructor(Vector3(7069.896f, 5288.992f, 29.37678f)), owning_building_guid = 36)
      LocalObject(913, Door.Constructor(Vector3(7080.616f, 5323.125f, 29.37678f)), owning_building_guid = 36)
      LocalObject(914, Door.Constructor(Vector3(7086.864f, 5289.289f, 29.37678f)), owning_building_guid = 36)
      LocalObject(915, Door.Constructor(Vector3(7097.288f, 5340.39f, 31.87678f)), owning_building_guid = 36)
      LocalObject(916, Door.Constructor(Vector3(7113.762f, 5368.966f, 31.87678f)), owning_building_guid = 36)
      LocalObject(917, Door.Constructor(Vector3(7114.453f, 5329.374f, 31.87678f)), owning_building_guid = 36)
      LocalObject(929, Door.Constructor(Vector3(7023.704f, 5298.73f, 40.13878f)), owning_building_guid = 36)
      LocalObject(936, Door.Constructor(Vector3(7035.368f, 5322.336f, 39.37678f)), owning_building_guid = 36)
      LocalObject(937, Door.Constructor(Vector3(7041.32f, 5305.467f, 39.37478f)), owning_building_guid = 36)
      LocalObject(2988, Door.Constructor(Vector3(7026.526f, 5302.633f, 29.70978f)), owning_building_guid = 36)
      LocalObject(2991, Door.Constructor(Vector3(7031.59f, 5307.876f, 29.70978f)), owning_building_guid = 36)
      LocalObject(2992, Door.Constructor(Vector3(7036.656f, 5313.122f, 29.70978f)), owning_building_guid = 36)
      LocalObject(
        977,
        IFFLock.Constructor(Vector3(7024.119f, 5294.521f, 39.33778f), Vector3(0, 0, 224)),
        owning_building_guid = 36,
        door_guid = 929
      )
      LocalObject(
        1243,
        IFFLock.Constructor(Vector3(7012.351f, 5247.847f, 29.19178f), Vector3(0, 0, 314)),
        owning_building_guid = 36,
        door_guid = 898
      )
      LocalObject(
        1244,
        IFFLock.Constructor(Vector3(7019.264f, 5303.397f, 29.19178f), Vector3(0, 0, 224)),
        owning_building_guid = 36,
        door_guid = 899
      )
      LocalObject(
        1245,
        IFFLock.Constructor(Vector3(7021.55f, 5243.333f, 29.19178f), Vector3(0, 0, 134)),
        owning_building_guid = 36,
        door_guid = 900
      )
      LocalObject(
        1246,
        IFFLock.Constructor(Vector3(7025.632f, 5328.363f, 21.69178f), Vector3(0, 0, 134)),
        owning_building_guid = 36,
        door_guid = 901
      )
      LocalObject(
        1251,
        IFFLock.Constructor(Vector3(7034.8f, 5324.011f, 29.19178f), Vector3(0, 0, 44)),
        owning_building_guid = 36,
        door_guid = 903
      )
      LocalObject(
        1252,
        IFFLock.Constructor(Vector3(7039.611f, 5287.595f, 49.30778f), Vector3(0, 0, 314)),
        owning_building_guid = 36,
        door_guid = 642
      )
      LocalObject(
        1253,
        IFFLock.Constructor(Vector3(7039.845f, 5312.51f, 49.30778f), Vector3(0, 0, 224)),
        owning_building_guid = 36,
        door_guid = 641
      )
      LocalObject(
        1254,
        IFFLock.Constructor(Vector3(7082.886f, 5401.505f, 39.30778f), Vector3(0, 0, 314)),
        owning_building_guid = 36,
        door_guid = 647
      )
      LocalObject(1686, Locker.Constructor(Vector3(6991.738f, 5288.667f, 27.85078f)), owning_building_guid = 36)
      LocalObject(1687, Locker.Constructor(Vector3(6992.607f, 5289.568f, 27.85078f)), owning_building_guid = 36)
      LocalObject(1688, Locker.Constructor(Vector3(6993.484f, 5290.476f, 27.85078f)), owning_building_guid = 36)
      LocalObject(1689, Locker.Constructor(Vector3(6994.36f, 5291.383f, 27.85078f)), owning_building_guid = 36)
      LocalObject(1690, Locker.Constructor(Vector3(6995.232f, 5292.287f, 27.85078f)), owning_building_guid = 36)
      LocalObject(1691, Locker.Constructor(Vector3(7001.063f, 5324.378f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1692, Locker.Constructor(Vector3(7001.823f, 5323.645f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1693, Locker.Constructor(Vector3(7002.582f, 5322.912f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1694, Locker.Constructor(Vector3(7003.341f, 5322.179f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1695, Locker.Constructor(Vector3(7004.104f, 5321.441f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1696, Locker.Constructor(Vector3(7004.862f, 5320.709f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1697, Locker.Constructor(Vector3(7014.955f, 5338.768f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1698, Locker.Constructor(Vector3(7015.714f, 5338.035f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1699, Locker.Constructor(Vector3(7016.477f, 5337.298f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1700, Locker.Constructor(Vector3(7017.235f, 5336.565f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1701, Locker.Constructor(Vector3(7017.995f, 5335.833f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1702, Locker.Constructor(Vector3(7018.754f, 5335.099f, 27.76378f)), owning_building_guid = 36)
      LocalObject(1703, Locker.Constructor(Vector3(7028.829f, 5325.674f, 28.11678f)), owning_building_guid = 36)
      LocalObject(1704, Locker.Constructor(Vector3(7029.655f, 5324.876f, 28.11678f)), owning_building_guid = 36)
      LocalObject(1705, Locker.Constructor(Vector3(7030.48f, 5324.08f, 28.11678f)), owning_building_guid = 36)
      LocalObject(1706, Locker.Constructor(Vector3(7031.318f, 5323.271f, 28.11678f)), owning_building_guid = 36)
      LocalObject(1739, Locker.Constructor(Vector3(7032.243f, 5328.112f, 37.85578f)), owning_building_guid = 36)
      LocalObject(1740, Locker.Constructor(Vector3(7032.961f, 5328.855f, 37.85578f)), owning_building_guid = 36)
      LocalObject(1741, Locker.Constructor(Vector3(7034.713f, 5330.669f, 37.62678f)), owning_building_guid = 36)
      LocalObject(1742, Locker.Constructor(Vector3(7035.432f, 5331.414f, 37.62678f)), owning_building_guid = 36)
      LocalObject(1743, Locker.Constructor(Vector3(7036.164f, 5332.171f, 37.62678f)), owning_building_guid = 36)
      LocalObject(1744, Locker.Constructor(Vector3(7036.882f, 5332.916f, 37.62678f)), owning_building_guid = 36)
      LocalObject(1745, Locker.Constructor(Vector3(7038.63f, 5334.726f, 37.85578f)), owning_building_guid = 36)
      LocalObject(1746, Locker.Constructor(Vector3(7039.349f, 5335.47f, 37.85578f)), owning_building_guid = 36)
      LocalObject(
        254,
        Terminal.Constructor(Vector3(6992.574f, 5284.39f, 27.84578f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        255,
        Terminal.Constructor(Vector3(6992.538f, 5286.438f, 27.84578f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        256,
        Terminal.Constructor(Vector3(6997.626f, 5291.707f, 27.84578f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        257,
        Terminal.Constructor(Vector3(6999.674f, 5291.743f, 27.84578f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        258,
        Terminal.Constructor(Vector3(7001.709f, 5275.568f, 27.84578f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        259,
        Terminal.Constructor(Vector3(7003.757f, 5275.604f, 27.84578f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        260,
        Terminal.Constructor(Vector3(7008.846f, 5280.873f, 27.84578f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        261,
        Terminal.Constructor(Vector3(7008.81f, 5282.92f, 27.84578f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2075,
        Terminal.Constructor(Vector3(7020.211f, 5316.22f, 29.44578f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2076,
        Terminal.Constructor(Vector3(7022.844f, 5318.945f, 29.44578f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2077,
        Terminal.Constructor(Vector3(7025.436f, 5321.629f, 29.44578f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2081,
        Terminal.Constructor(Vector3(7044.596f, 5300.182f, 39.15078f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2880,
        Terminal.Constructor(Vector3(6999.968f, 5267.55f, 29.46878f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2881,
        Terminal.Constructor(Vector3(7028.044f, 5304.632f, 29.98978f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2882,
        Terminal.Constructor(Vector3(7033.109f, 5309.872f, 29.98978f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2883,
        Terminal.Constructor(Vector3(7038.171f, 5315.12f, 29.98978f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2884,
        Terminal.Constructor(Vector3(7052.105f, 5325.136f, 39.43478f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2885,
        Terminal.Constructor(Vector3(7073.186f, 5286.64f, 29.46878f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2886,
        Terminal.Constructor(Vector3(7094.885f, 5343.53f, 31.96878f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3076,
        Terminal.Constructor(Vector3(7060.825f, 5385.591f, 40.16078f), vehicle_terminal_combined),
        owning_building_guid = 36
      )
      LocalObject(
        1896,
        VehicleSpawnPad.Constructor(Vector3(7070.699f, 5376.181f, 36.00278f), mb_pad_creation, Vector3(0, 0, 134)),
        owning_building_guid = 36,
        terminal_guid = 3076
      )
      LocalObject(2662, ResourceSilo.Constructor(Vector3(7052.028f, 5235.555f, 44.87278f)), owning_building_guid = 36)
      LocalObject(
        2764,
        SpawnTube.Constructor(Vector3(7027.575f, 5303.084f, 27.85578f), Vector3(0, 0, 224)),
        owning_building_guid = 36
      )
      LocalObject(
        2767,
        SpawnTube.Constructor(Vector3(7032.637f, 5308.327f, 27.85578f), Vector3(0, 0, 224)),
        owning_building_guid = 36
      )
      LocalObject(
        2768,
        SpawnTube.Constructor(Vector3(7037.702f, 5313.572f, 27.85578f), Vector3(0, 0, 224)),
        owning_building_guid = 36
      )
      LocalObject(
        171,
        ProximityTerminal.Constructor(Vector3(7047.511f, 5320.491f, 37.66578f), adv_med_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1916,
        ProximityTerminal.Constructor(Vector3(7021.371f, 5330.359f, 27.85578f), medical_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2294,
        ProximityTerminal.Constructor(Vector3(7011.613f, 5282.367f, 48.18778f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        2295,
        Terminal.Constructor(Vector3(7011.613f, 5282.367f, 48.18778f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2297,
        ProximityTerminal.Constructor(Vector3(7017.448f, 5265.057f, 46.19778f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        2298,
        Terminal.Constructor(Vector3(7017.448f, 5265.057f, 46.19778f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2300,
        ProximityTerminal.Constructor(Vector3(7093.383f, 5362.543f, 46.20778f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        2301,
        Terminal.Constructor(Vector3(7093.383f, 5362.543f, 46.20778f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2303,
        ProximityTerminal.Constructor(Vector3(7100.059f, 5345.97f, 48.14878f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        2304,
        Terminal.Constructor(Vector3(7100.059f, 5345.97f, 48.14878f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2643,
        ProximityTerminal.Constructor(Vector3(6995.195f, 5339.081f, 37.60578f), repair_silo),
        owning_building_guid = 36
      )
      LocalObject(
        2644,
        Terminal.Constructor(Vector3(6995.195f, 5339.081f, 37.60578f), ground_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2647,
        ProximityTerminal.Constructor(Vector3(7128.458f, 5350.39f, 37.60578f), repair_silo),
        owning_building_guid = 36
      )
      LocalObject(
        2648,
        Terminal.Constructor(Vector3(7128.458f, 5350.39f, 37.60578f), ground_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1870,
        FacilityTurret.Constructor(Vector3(6970.636f, 5331.331f, 46.25778f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1870, 5041)
      LocalObject(
        1871,
        FacilityTurret.Constructor(Vector3(6971.728f, 5270.348f, 46.25778f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1871, 5042)
      LocalObject(
        1873,
        FacilityTurret.Constructor(Vector3(7039.61f, 5206.37f, 46.25778f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1873, 5043)
      LocalObject(
        1875,
        FacilityTurret.Constructor(Vector3(7065.355f, 5427.708f, 46.25778f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1875, 5044)
      LocalObject(
        1876,
        FacilityTurret.Constructor(Vector3(7162.624f, 5333.766f, 46.25778f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1876, 5045)
      LocalObject(
        952,
        ImplantTerminalMech.Constructor(Vector3(6995.28f, 5278.022f, 27.33278f)),
        owning_building_guid = 36
      )
      LocalObject(
        944,
        Terminal.Constructor(Vector3(6995.292f, 5278.035f, 27.33278f), implant_terminal_interface),
        owning_building_guid = 36
      )
      TerminalToInterface(952, 944)
      LocalObject(
        953,
        ImplantTerminalMech.Constructor(Vector3(7005.938f, 5289.077f, 27.33278f)),
        owning_building_guid = 36
      )
      LocalObject(
        945,
        Terminal.Constructor(Vector3(7005.926f, 5289.064f, 27.33278f), implant_terminal_interface),
        owning_building_guid = 36
      )
      TerminalToInterface(953, 945)
      LocalObject(
        2389,
        Painbox.Constructor(Vector3(7053.638f, 5332.032f, 51.88458f), painbox),
        owning_building_guid = 36
      )
      LocalObject(
        2401,
        Painbox.Constructor(Vector3(7028.031f, 5318.701f, 31.92568f), painbox_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        2413,
        Painbox.Constructor(Vector3(7044.605f, 5320.647f, 52.08968f), painbox_door_radius),
        owning_building_guid = 36
      )
      LocalObject(
        2447,
        Painbox.Constructor(Vector3(7010.71f, 5319.665f, 31.39668f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        2448,
        Painbox.Constructor(Vector3(7017.053f, 5304.113f, 29.56998f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        2449,
        Painbox.Constructor(Vector3(7037.951f, 5322.909f, 30.21168f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(311, Generator.Constructor(Vector3(7057.467f, 5333.74f, 46.56178f)), owning_building_guid = 36)
      LocalObject(
        299,
        Terminal.Constructor(Vector3(7051.811f, 5327.814f, 47.85578f), gen_control),
        owning_building_guid = 36
      )
    }

    Building26621()

    def Building26621(): Unit = { // Name: GW_Ishundar_S Type: hst GUID: 39, MapID: 26621
      LocalBuilding(
        "GW_Ishundar_S",
        39,
        26621,
        FoundationBuilder(WarpGate.Structure(Vector3(2440.33f, 1921.26f, 35.77f), hst))
      )
    }

    Building26620()

    def Building26620(): Unit = { // Name: GW_Ishundar_N Type: hst GUID: 40, MapID: 26620
      LocalBuilding(
        "GW_Ishundar_N",
        40,
        26620,
        FoundationBuilder(WarpGate.Structure(Vector3(4163.3f, 4368.72f, 65.16f), hst))
      )
    }

    Building6()

    def Building6(): Unit = { // Name: Baal Type: tech_plant GUID: 42, MapID: 6
      LocalBuilding(
        "Baal",
        42,
        6,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(942f, 5484f, 72.354f), Vector3(0f, 0f, 225f), tech_plant)
        )
      )
      LocalObject(
        218,
        CaptureTerminal.Constructor(Vector3(907.4769f, 5511.828f, 87.45399f), capture_terminal),
        owning_building_guid = 42
      )
      LocalObject(312, Door.Constructor(Vector3(874.2571f, 5545.23f, 74.005f)), owning_building_guid = 42)
      LocalObject(313, Door.Constructor(Vector3(875.457f, 5438.792f, 81.85899f)), owning_building_guid = 42)
      LocalObject(314, Door.Constructor(Vector3(887.1215f, 5558.095f, 81.96799f)), owning_building_guid = 42)
      LocalObject(315, Door.Constructor(Vector3(888.3214f, 5425.927f, 73.896f)), owning_building_guid = 42)
      LocalObject(316, Door.Constructor(Vector3(899.5736f, 5582.995f, 73.975f)), owning_building_guid = 42)
      LocalObject(317, Door.Constructor(Vector3(901.7869f, 5499.543f, 88.975f)), owning_building_guid = 42)
      LocalObject(318, Door.Constructor(Vector3(917.9428f, 5493.071f, 88.975f)), owning_building_guid = 42)
      LocalObject(319, Door.Constructor(Vector3(933.8782f, 5431.661f, 81.85899f)), owning_building_guid = 42)
      LocalObject(321, Door.Constructor(Vector3(942.9822f, 5584.078f, 73.896f)), owning_building_guid = 42)
      LocalObject(322, Door.Constructor(Vector3(946.7426f, 5444.526f, 73.896f)), owning_building_guid = 42)
      LocalObject(323, Door.Constructor(Vector3(955.8458f, 5571.214f, 81.85899f)), owning_building_guid = 42)
      LocalObject(325, Door.Constructor(Vector3(995.1271f, 5531.933f, 73.896f)), owning_building_guid = 42)
      LocalObject(326, Door.Constructor(Vector3(1007.991f, 5519.069f, 81.85899f)), owning_building_guid = 42)
      LocalObject(651, Door.Constructor(Vector3(1004.227f, 5489.655f, 76.091f)), owning_building_guid = 42)
      LocalObject(655, Door.Constructor(Vector3(964.6274f, 5529.255f, 56.091f)), owning_building_guid = 42)
      LocalObject(659, Door.Constructor(Vector3(888.2599f, 5492.485f, 58.975f)), owning_building_guid = 42)
      LocalObject(660, Door.Constructor(Vector3(888.2599f, 5537.74f, 66.475f)), owning_building_guid = 42)
      LocalObject(661, Door.Constructor(Vector3(888.2599f, 5560.368f, 66.475f)), owning_building_guid = 42)
      LocalObject(662, Door.Constructor(Vector3(893.9167f, 5503.799f, 58.975f)), owning_building_guid = 42)
      LocalObject(663, Door.Constructor(Vector3(893.9167f, 5503.799f, 66.475f)), owning_building_guid = 42)
      LocalObject(664, Door.Constructor(Vector3(899.5736f, 5481.171f, 66.475f)), owning_building_guid = 42)
      LocalObject(665, Door.Constructor(Vector3(910.8873f, 5475.515f, 58.975f)), owning_building_guid = 42)
      LocalObject(666, Door.Constructor(Vector3(910.8873f, 5486.829f, 66.475f)), owning_building_guid = 42)
      LocalObject(667, Door.Constructor(Vector3(916.5441f, 5509.456f, 58.975f)), owning_building_guid = 42)
      LocalObject(668, Door.Constructor(Vector3(916.5441f, 5515.113f, 68.975f)), owning_building_guid = 42)
      LocalObject(669, Door.Constructor(Vector3(916.5441f, 5515.113f, 88.975f)), owning_building_guid = 42)
      LocalObject(670, Door.Constructor(Vector3(916.5441f, 5532.083f, 66.475f)), owning_building_guid = 42)
      LocalObject(671, Door.Constructor(Vector3(919.3726f, 5506.627f, 78.975f)), owning_building_guid = 42)
      LocalObject(672, Door.Constructor(Vector3(922.201f, 5503.799f, 88.975f)), owning_building_guid = 42)
      LocalObject(673, Door.Constructor(Vector3(927.8578f, 5503.799f, 68.975f)), owning_building_guid = 42)
      LocalObject(674, Door.Constructor(Vector3(933.5147f, 5537.74f, 63.975f)), owning_building_guid = 42)
      LocalObject(675, Door.Constructor(Vector3(939.1716f, 5577.338f, 66.475f)), owning_building_guid = 42)
      LocalObject(676, Door.Constructor(Vector3(950.4853f, 5520.77f, 63.975f)), owning_building_guid = 42)
      LocalObject(677, Door.Constructor(Vector3(956.1422f, 5560.368f, 63.975f)), owning_building_guid = 42)
      LocalObject(918, Door.Constructor(Vector3(881.2793f, 5487.851f, 74.73399f)), owning_building_guid = 42)
      LocalObject(2887, Door.Constructor(Vector3(903.1516f, 5504.926f, 66.808f)), owning_building_guid = 42)
      LocalObject(2888, Door.Constructor(Vector3(908.3085f, 5499.769f, 66.808f)), owning_building_guid = 42)
      LocalObject(2889, Door.Constructor(Vector3(913.4626f, 5494.615f, 66.808f)), owning_building_guid = 42)
      LocalObject(
        966,
        IFFLock.Constructor(Vector3(877.1201f, 5487.564f, 73.934f), Vector3(0, 0, 315)),
        owning_building_guid = 42,
        door_guid = 918
      )
      LocalObject(
        978,
        IFFLock.Constructor(Vector3(1002.173f, 5484.276f, 74.042f), Vector3(0, 0, 135)),
        owning_building_guid = 42,
        door_guid = 651
      )
      LocalObject(
        982,
        IFFLock.Constructor(Vector3(887.813f, 5494.262f, 58.79f), Vector3(0, 0, 45)),
        owning_building_guid = 42,
        door_guid = 659
      )
      LocalObject(
        983,
        IFFLock.Constructor(Vector3(892.2324f, 5503.26f, 66.28999f), Vector3(0, 0, 315)),
        owning_building_guid = 42,
        door_guid = 663
      )
      LocalObject(
        984,
        IFFLock.Constructor(Vector3(898.7025f, 5585.017f, 73.906f), Vector3(0, 0, 45)),
        owning_building_guid = 42,
        door_guid = 316
      )
      LocalObject(
        985,
        IFFLock.Constructor(Vector3(899.7709f, 5498.658f, 88.89999f), Vector3(0, 0, 315)),
        owning_building_guid = 42,
        door_guid = 317
      )
      LocalObject(
        986,
        IFFLock.Constructor(Vector3(909.1089f, 5475.07f, 58.79f), Vector3(0, 0, 315)),
        owning_building_guid = 42,
        door_guid = 665
      )
      LocalObject(
        987,
        IFFLock.Constructor(Vector3(912.5717f, 5487.367f, 66.28999f), Vector3(0, 0, 135)),
        owning_building_guid = 42,
        door_guid = 666
      )
      LocalObject(
        988,
        IFFLock.Constructor(Vector3(918.3204f, 5515.56f, 88.78999f), Vector3(0, 0, 135)),
        owning_building_guid = 42,
        door_guid = 669
      )
      LocalObject(
        989,
        IFFLock.Constructor(Vector3(919.9588f, 5493.944f, 88.89999f), Vector3(0, 0, 135)),
        owning_building_guid = 42,
        door_guid = 318
      )
      LocalObject(1255, Locker.Constructor(Vector3(890.4647f, 5497.319f, 65.215f)), owning_building_guid = 42)
      LocalObject(1256, Locker.Constructor(Vector3(890.6902f, 5469.227f, 57.45399f)), owning_building_guid = 42)
      LocalObject(1257, Locker.Constructor(Vector3(891.2771f, 5498.131f, 65.215f)), owning_building_guid = 42)
      LocalObject(1258, Locker.Constructor(Vector3(891.6356f, 5470.172f, 57.45399f)), owning_building_guid = 42)
      LocalObject(1259, Locker.Constructor(Vector3(892.0881f, 5498.942f, 65.215f)), owning_building_guid = 42)
      LocalObject(1260, Locker.Constructor(Vector3(892.5803f, 5471.117f, 57.45399f)), owning_building_guid = 42)
      LocalObject(1261, Locker.Constructor(Vector3(892.9113f, 5499.766f, 65.215f)), owning_building_guid = 42)
      LocalObject(1262, Locker.Constructor(Vector3(893.5165f, 5472.053f, 57.45399f)), owning_building_guid = 42)
      LocalObject(1263, Locker.Constructor(Vector3(896.7268f, 5475.263f, 57.45399f)), owning_building_guid = 42)
      LocalObject(1264, Locker.Constructor(Vector3(897.6722f, 5476.208f, 57.45399f)), owning_building_guid = 42)
      LocalObject(1265, Locker.Constructor(Vector3(898.6169f, 5477.153f, 57.45399f)), owning_building_guid = 42)
      LocalObject(1266, Locker.Constructor(Vector3(899.5531f, 5478.089f, 57.45399f)), owning_building_guid = 42)
      LocalObject(
        172,
        Terminal.Constructor(Vector3(912.6805f, 5489.836f, 88.057f), air_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1877,
        VehicleSpawnPad.Constructor(Vector3(924.1434f, 5472.034f, 84.932f), mb_pad_creation, Vector3(0, 0, 135)),
        owning_building_guid = 42,
        terminal_guid = 172
      )
      LocalObject(
        173,
        Terminal.Constructor(Vector3(921.1177f, 5498.273f, 88.057f), air_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1878,
        VehicleSpawnPad.Constructor(Vector3(939.0054f, 5486.896f, 84.932f), mb_pad_creation, Vector3(0, 0, 135)),
        owning_building_guid = 42,
        terminal_guid = 173
      )
      LocalObject(
        1917,
        Terminal.Constructor(Vector3(894.4499f, 5493.855f, 66.544f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1918,
        Terminal.Constructor(Vector3(897.0881f, 5491.217f, 66.544f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1919,
        Terminal.Constructor(Vector3(899.7673f, 5488.538f, 66.544f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1920,
        Terminal.Constructor(Vector3(921.0894f, 5500.586f, 78.784f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2804,
        Terminal.Constructor(Vector3(891.5472f, 5568.476f, 66.51099f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2805,
        Terminal.Constructor(Vector3(901.1801f, 5506.476f, 67.088f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2806,
        Terminal.Constructor(Vector3(901.2686f, 5472.063f, 79.036f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2807,
        Terminal.Constructor(Vector3(902.861f, 5511.907f, 59.01099f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2808,
        Terminal.Constructor(Vector3(906.3392f, 5501.323f, 67.088f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2809,
        Terminal.Constructor(Vector3(911.4905f, 5496.167f, 67.088f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2810,
        Terminal.Constructor(Vector3(933.9977f, 5471.451f, 84.377f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3065,
        Terminal.Constructor(Vector3(947.2517f, 5546.636f, 58.168f), ground_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1879,
        VehicleSpawnPad.Constructor(Vector3(955.0065f, 5538.954f, 49.891f), mb_pad_creation, Vector3(0, 0, 135)),
        owning_building_guid = 42,
        terminal_guid = 3065
      )
      LocalObject(2651, ResourceSilo.Constructor(Vector3(921.3546f, 5417.315f, 79.362f)), owning_building_guid = 42)
      LocalObject(
        2663,
        SpawnTube.Constructor(Vector3(902.7202f, 5505.979f, 64.95399f), Vector3(0, 0, 135)),
        owning_building_guid = 42
      )
      LocalObject(
        2664,
        SpawnTube.Constructor(Vector3(907.8757f, 5500.824f, 64.95399f), Vector3(0, 0, 135)),
        owning_building_guid = 42
      )
      LocalObject(
        2665,
        SpawnTube.Constructor(Vector3(913.0291f, 5495.671f, 64.95399f), Vector3(0, 0, 135)),
        owning_building_guid = 42
      )
      LocalObject(
        1897,
        ProximityTerminal.Constructor(Vector3(894.7483f, 5474.055f, 57.45399f), medical_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1898,
        ProximityTerminal.Constructor(Vector3(913.604f, 5508.07f, 77.451f), medical_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2156,
        ProximityTerminal.Constructor(Vector3(866.4061f, 5523.483f, 80.562f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2157,
        Terminal.Constructor(Vector3(866.4061f, 5523.483f, 80.562f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2159,
        ProximityTerminal.Constructor(Vector3(868.3676f, 5469.97f, 83.006f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2160,
        Terminal.Constructor(Vector3(868.3676f, 5469.97f, 83.006f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2162,
        ProximityTerminal.Constructor(Vector3(875.4061f, 5454.031f, 80.562f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2163,
        Terminal.Constructor(Vector3(875.4061f, 5454.031f, 80.562f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2165,
        ProximityTerminal.Constructor(Vector3(889.7413f, 5522.995f, 87.80099f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2166,
        Terminal.Constructor(Vector3(889.7413f, 5522.995f, 87.80099f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2168,
        ProximityTerminal.Constructor(Vector3(965.2306f, 5518.78f, 82.904f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2169,
        Terminal.Constructor(Vector3(965.2306f, 5518.78f, 82.904f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2171,
        ProximityTerminal.Constructor(Vector3(968.1326f, 5544.553f, 80.549f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2172,
        Terminal.Constructor(Vector3(968.1326f, 5544.553f, 80.549f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2555,
        ProximityTerminal.Constructor(Vector3(834.9235f, 5508.152f, 72.104f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        2556,
        Terminal.Constructor(Vector3(834.9235f, 5508.152f, 72.104f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2559,
        ProximityTerminal.Constructor(Vector3(971.2233f, 5454.34f, 72.0825f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        2560,
        Terminal.Constructor(Vector3(971.2233f, 5454.34f, 72.0825f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1759,
        FacilityTurret.Constructor(Vector3(810.8338f, 5493.326f, 80.853f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1759, 5046)
      LocalObject(
        1760,
        FacilityTurret.Constructor(Vector3(851.1665f, 5442.066f, 80.853f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1760, 5047)
      LocalObject(
        1761,
        FacilityTurret.Constructor(Vector3(908.2314f, 5395.928f, 80.853f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1761, 5048)
      LocalObject(
        1762,
        FacilityTurret.Constructor(Vector3(927.3289f, 5609.81f, 80.853f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1762, 5049)
      LocalObject(
        1763,
        FacilityTurret.Constructor(Vector3(1007.941f, 5410.139f, 80.853f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1763, 5050)
      LocalObject(
        1764,
        FacilityTurret.Constructor(Vector3(1071.365f, 5473.562f, 80.853f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1764, 5051)
      LocalObject(
        2378,
        Painbox.Constructor(Vector3(921.1186f, 5468.483f, 60.9273f), painbox),
        owning_building_guid = 42
      )
      LocalObject(
        2390,
        Painbox.Constructor(Vector3(901.2565f, 5495.282f, 69.2239f), painbox_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        2402,
        Painbox.Constructor(Vector3(909.3116f, 5477.515f, 60.6134f), painbox_door_radius),
        owning_building_guid = 42
      )
      LocalObject(
        2414,
        Painbox.Constructor(Vector3(893.3819f, 5505.699f, 67.08189f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        2415,
        Painbox.Constructor(Vector3(895.9877f, 5479.608f, 68.53629f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        2416,
        Painbox.Constructor(Vector3(912.2366f, 5485.676f, 67.6302f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(300, Generator.Constructor(Vector3(921.9041f, 5464.533f, 56.16f)), owning_building_guid = 42)
      LocalObject(
        288,
        Terminal.Constructor(Vector3(916.0782f, 5470.293f, 57.45399f), gen_control),
        owning_building_guid = 42
      )
    }

    Building14()

    def Building14(): Unit = { // Name: Marduk Type: tech_plant GUID: 45, MapID: 14
      LocalBuilding(
        "Marduk",
        45,
        14,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3136f, 2234f, 70.51518f),
            Vector3(0f, 0f, 269f),
            tech_plant
          )
        )
      )
      LocalObject(
        221,
        CaptureTerminal.Constructor(Vector3(3091.835f, 2230.036f, 85.61518f), capture_terminal),
        owning_building_guid = 45
      )
      LocalObject(408, Door.Constructor(Vector3(3036.713f, 2275.739f, 72.13618f)), owning_building_guid = 45)
      LocalObject(409, Door.Constructor(Vector3(3044.736f, 2230.987f, 72.16618f)), owning_building_guid = 45)
      LocalObject(410, Door.Constructor(Vector3(3045.053f, 2249.177f, 80.12918f)), owning_building_guid = 45)
      LocalObject(411, Door.Constructor(Vector3(3067.187f, 2306.672f, 72.05718f)), owning_building_guid = 45)
      LocalObject(412, Door.Constructor(Vector3(3085.376f, 2306.354f, 80.02018f)), owning_building_guid = 45)
      LocalObject(413, Door.Constructor(Vector3(3096.276f, 2217.247f, 87.13618f)), owning_building_guid = 45)
      LocalObject(420, Door.Constructor(Vector3(3112.394f, 2223.813f, 87.13618f)), owning_building_guid = 45)
      LocalObject(421, Door.Constructor(Vector3(3119.537f, 2155.255f, 80.02018f)), owning_building_guid = 45)
      LocalObject(422, Door.Constructor(Vector3(3137.728f, 2154.938f, 72.05718f)), owning_building_guid = 45)
      LocalObject(423, Door.Constructor(Vector3(3140.92f, 2305.385f, 72.05718f)), owning_building_guid = 45)
      LocalObject(424, Door.Constructor(Vector3(3159.109f, 2305.068f, 80.02018f)), owning_building_guid = 45)
      LocalObject(425, Door.Constructor(Vector3(3166.515f, 2190.709f, 80.02018f)), owning_building_guid = 45)
      LocalObject(426, Door.Constructor(Vector3(3166.833f, 2208.899f, 72.05718f)), owning_building_guid = 45)
      LocalObject(652, Door.Constructor(Vector3(3176.834f, 2281.295f, 74.25218f)), owning_building_guid = 45)
      LocalObject(656, Door.Constructor(Vector3(3120.84f, 2282.272f, 54.25218f)), owning_building_guid = 45)
      LocalObject(722, Door.Constructor(Vector3(3044.293f, 2251.603f, 64.63618f)), owning_building_guid = 45)
      LocalObject(723, Door.Constructor(Vector3(3060.011f, 2235.326f, 64.63618f)), owning_building_guid = 45)
      LocalObject(724, Door.Constructor(Vector3(3069.127f, 2299.177f, 64.63618f)), owning_building_guid = 45)
      LocalObject(725, Door.Constructor(Vector3(3084.287f, 2250.905f, 64.63618f)), owning_building_guid = 45)
      LocalObject(726, Door.Constructor(Vector3(3087.658f, 2214.841f, 57.13618f)), owning_building_guid = 45)
      LocalObject(727, Door.Constructor(Vector3(3087.658f, 2214.841f, 64.63618f)), owning_building_guid = 45)
      LocalObject(728, Door.Constructor(Vector3(3091.448f, 2202.773f, 57.13618f)), owning_building_guid = 45)
      LocalObject(729, Door.Constructor(Vector3(3092.565f, 2266.763f, 62.13618f)), owning_building_guid = 45)
      LocalObject(730, Door.Constructor(Vector3(3093.124f, 2298.758f, 62.13618f)), owning_building_guid = 45)
      LocalObject(731, Door.Constructor(Vector3(3096.076f, 2238.698f, 67.13618f)), owning_building_guid = 45)
      LocalObject(732, Door.Constructor(Vector3(3096.076f, 2238.698f, 87.13618f)), owning_building_guid = 45)
      LocalObject(733, Door.Constructor(Vector3(3100.005f, 2234.628f, 57.13618f)), owning_building_guid = 45)
      LocalObject(734, Door.Constructor(Vector3(3104.005f, 2234.559f, 77.13618f)), owning_building_guid = 45)
      LocalObject(735, Door.Constructor(Vector3(3107.446f, 2202.494f, 64.63618f)), owning_building_guid = 45)
      LocalObject(736, Door.Constructor(Vector3(3108.004f, 2234.489f, 87.13618f)), owning_building_guid = 45)
      LocalObject(737, Door.Constructor(Vector3(3111.655f, 2214.422f, 64.63618f)), owning_building_guid = 45)
      LocalObject(738, Door.Constructor(Vector3(3112.073f, 2238.418f, 67.13618f)), owning_building_guid = 45)
      LocalObject(739, Door.Constructor(Vector3(3116.562f, 2266.344f, 62.13618f)), owning_building_guid = 45)
      LocalObject(740, Door.Constructor(Vector3(3119.514f, 2206.283f, 57.13618f)), owning_building_guid = 45)
      LocalObject(921, Door.Constructor(Vector3(3089.646f, 2194.59f, 72.89518f)), owning_building_guid = 45)
      LocalObject(2916, Door.Constructor(Vector3(3093.518f, 2222.067f, 64.96918f)), owning_building_guid = 45)
      LocalObject(2919, Door.Constructor(Vector3(3100.81f, 2221.939f, 64.96918f)), owning_building_guid = 45)
      LocalObject(2920, Door.Constructor(Vector3(3108.098f, 2221.812f, 64.96918f)), owning_building_guid = 45)
      LocalObject(
        969,
        IFFLock.Constructor(Vector3(3086.854f, 2191.494f, 72.09518f), Vector3(0, 0, 271)),
        owning_building_guid = 45,
        door_guid = 921
      )
      LocalObject(
        979,
        IFFLock.Constructor(Vector3(3179.093f, 2275.998f, 72.20319f), Vector3(0, 0, 91)),
        owning_building_guid = 45,
        door_guid = 652
      )
      LocalObject(
        1058,
        IFFLock.Constructor(Vector3(3034.682f, 2276.589f, 72.06718f), Vector3(0, 0, 1)),
        owning_building_guid = 45,
        door_guid = 408
      )
      LocalObject(
        1059,
        IFFLock.Constructor(Vector3(3086.821f, 2213.283f, 64.45118f), Vector3(0, 0, 271)),
        owning_building_guid = 45,
        door_guid = 727
      )
      LocalObject(
        1060,
        IFFLock.Constructor(Vector3(3089.893f, 2203.74f, 56.95118f), Vector3(0, 0, 1)),
        owning_building_guid = 45,
        door_guid = 728
      )
      LocalObject(
        1061,
        IFFLock.Constructor(Vector3(3095.441f, 2215.209f, 87.06118f), Vector3(0, 0, 271)),
        owning_building_guid = 45,
        door_guid = 413
      )
      LocalObject(
        1065,
        IFFLock.Constructor(Vector3(3097.043f, 2240.253f, 86.95119f), Vector3(0, 0, 91)),
        owning_building_guid = 45,
        door_guid = 732
      )
      LocalObject(
        1069,
        IFFLock.Constructor(Vector3(3112.492f, 2215.979f, 64.45118f), Vector3(0, 0, 91)),
        owning_building_guid = 45,
        door_guid = 737
      )
      LocalObject(
        1070,
        IFFLock.Constructor(Vector3(3113.237f, 2225.842f, 87.06118f), Vector3(0, 0, 91)),
        owning_building_guid = 45,
        door_guid = 420
      )
      LocalObject(
        1071,
        IFFLock.Constructor(Vector3(3118.543f, 2204.728f, 56.95118f), Vector3(0, 0, 271)),
        owning_building_guid = 45,
        door_guid = 740
      )
      LocalObject(1380, Locker.Constructor(Vector3(3089.676f, 2207.781f, 63.37618f)), owning_building_guid = 45)
      LocalObject(1381, Locker.Constructor(Vector3(3089.697f, 2208.93f, 63.37618f)), owning_building_guid = 45)
      LocalObject(1382, Locker.Constructor(Vector3(3089.717f, 2210.077f, 63.37618f)), owning_building_guid = 45)
      LocalObject(1383, Locker.Constructor(Vector3(3089.737f, 2211.241f, 63.37618f)), owning_building_guid = 45)
      LocalObject(1392, Locker.Constructor(Vector3(3109.354f, 2187.73f, 55.61518f)), owning_building_guid = 45)
      LocalObject(1393, Locker.Constructor(Vector3(3109.377f, 2189.067f, 55.61518f)), owning_building_guid = 45)
      LocalObject(1394, Locker.Constructor(Vector3(3109.4f, 2190.403f, 55.61518f)), owning_building_guid = 45)
      LocalObject(1395, Locker.Constructor(Vector3(3109.423f, 2191.726f, 55.61518f)), owning_building_guid = 45)
      LocalObject(1396, Locker.Constructor(Vector3(3109.502f, 2196.266f, 55.61518f)), owning_building_guid = 45)
      LocalObject(1397, Locker.Constructor(Vector3(3109.526f, 2197.603f, 55.61518f)), owning_building_guid = 45)
      LocalObject(1398, Locker.Constructor(Vector3(3109.549f, 2198.938f, 55.61518f)), owning_building_guid = 45)
      LocalObject(1399, Locker.Constructor(Vector3(3109.572f, 2200.262f, 55.61518f)), owning_building_guid = 45)
      LocalObject(
        174,
        Terminal.Constructor(Vector3(3110.855f, 2217.831f, 86.21819f), air_vehicle_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1883,
        VehicleSpawnPad.Constructor(Vector3(3131.468f, 2212.988f, 83.09319f), mb_pad_creation, Vector3(0, 0, 91)),
        owning_building_guid = 45,
        terminal_guid = 174
      )
      LocalObject(
        175,
        Terminal.Constructor(Vector3(3111.063f, 2229.761f, 86.21819f), air_vehicle_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1884,
        VehicleSpawnPad.Constructor(Vector3(3131.834f, 2234.003f, 83.09319f), mb_pad_creation, Vector3(0, 0, 91)),
        owning_building_guid = 45,
        terminal_guid = 175
      )
      LocalObject(
        1963,
        Terminal.Constructor(Vector3(3094.949f, 2208.059f, 64.70518f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1964,
        Terminal.Constructor(Vector3(3098.679f, 2207.993f, 64.70518f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1965,
        Terminal.Constructor(Vector3(3102.468f, 2207.927f, 64.70518f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1969,
        Terminal.Constructor(Vector3(3109.437f, 2231.405f, 76.94518f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2827,
        Terminal.Constructor(Vector3(3041.025f, 2259.719f, 64.67218f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2828,
        Terminal.Constructor(Vector3(3088.46f, 2226.887f, 57.17218f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2829,
        Terminal.Constructor(Vector3(3091.023f, 2221.812f, 65.24918f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2830,
        Terminal.Constructor(Vector3(3098.314f, 2221.689f, 65.24918f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2831,
        Terminal.Constructor(Vector3(3105.601f, 2221.559f, 65.24918f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2832,
        Terminal.Constructor(Vector3(3114.992f, 2197.119f, 77.19718f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2833,
        Terminal.Constructor(Vector3(3138.961f, 2219.414f, 82.53819f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3068,
        Terminal.Constructor(Vector3(3096.267f, 2282.705f, 56.32918f), ground_vehicle_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1882,
        VehicleSpawnPad.Constructor(Vector3(3107.182f, 2282.565f, 48.05219f), mb_pad_creation, Vector3(0, 0, 91)),
        owning_building_guid = 45,
        terminal_guid = 3068
      )
      LocalObject(2654, ResourceSilo.Constructor(Vector3(3167.472f, 2171.689f, 77.52319f)), owning_building_guid = 45)
      LocalObject(
        2692,
        SpawnTube.Constructor(Vector3(3092.476f, 2222.525f, 63.11518f), Vector3(0, 0, 91)),
        owning_building_guid = 45
      )
      LocalObject(
        2695,
        SpawnTube.Constructor(Vector3(3099.766f, 2222.398f, 63.11518f), Vector3(0, 0, 91)),
        owning_building_guid = 45
      )
      LocalObject(
        2696,
        SpawnTube.Constructor(Vector3(3107.053f, 2222.271f, 63.11518f), Vector3(0, 0, 91)),
        owning_building_guid = 45
      )
      LocalObject(
        1902,
        ProximityTerminal.Constructor(Vector3(3098.853f, 2231.589f, 75.61218f), medical_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1903,
        ProximityTerminal.Constructor(Vector3(3108.918f, 2194.022f, 55.61518f), medical_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2198,
        ProximityTerminal.Constructor(Vector3(3054.195f, 2209.89f, 78.72318f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2199,
        Terminal.Constructor(Vector3(3054.195f, 2209.89f, 78.72318f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2201,
        ProximityTerminal.Constructor(Vector3(3071.32f, 2225.749f, 85.96218f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2202,
        Terminal.Constructor(Vector3(3071.32f, 2225.749f, 85.96218f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2204,
        ProximityTerminal.Constructor(Vector3(3092.78f, 2172.758f, 81.16718f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2205,
        Terminal.Constructor(Vector3(3092.78f, 2172.758f, 81.16718f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2207,
        ProximityTerminal.Constructor(Vector3(3108.914f, 2166.182f, 78.72318f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2208,
        Terminal.Constructor(Vector3(3108.914f, 2166.182f, 78.72318f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2210,
        ProximityTerminal.Constructor(Vector3(3112.734f, 2295.711f, 78.71018f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2211,
        Terminal.Constructor(Vector3(3112.734f, 2295.711f, 78.71018f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2213,
        ProximityTerminal.Constructor(Vector3(3128.55f, 2275.156f, 81.06519f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2214,
        Terminal.Constructor(Vector3(3128.55f, 2275.156f, 81.06519f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2579,
        ProximityTerminal.Constructor(Vector3(3042.198f, 2176.992f, 70.26518f), repair_silo),
        owning_building_guid = 45
      )
      LocalObject(
        2580,
        Terminal.Constructor(Vector3(3042.198f, 2176.992f, 70.26518f), ground_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2583,
        ProximityTerminal.Constructor(Vector3(3177.625f, 2232.965f, 70.24368f), repair_silo),
        owning_building_guid = 45
      )
      LocalObject(
        2584,
        Terminal.Constructor(Vector3(3177.625f, 2232.965f, 70.24368f), ground_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1792,
        FacilityTurret.Constructor(Vector3(3035.168f, 2149.593f, 79.01418f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1792, 5052)
      LocalObject(
        1793,
        FacilityTurret.Constructor(Vector3(3038.052f, 2314.309f, 79.01418f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1793, 5053)
      LocalObject(
        1794,
        FacilityTurret.Constructor(Vector3(3099.79f, 2140.737f, 79.01418f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1794, 5054)
      LocalObject(
        1795,
        FacilityTurret.Constructor(Vector3(3172.889f, 2147.189f, 79.01418f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1795, 5055)
      LocalObject(
        1800,
        FacilityTurret.Constructor(Vector3(3234.742f, 2226.675f, 79.01418f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1800, 5056)
      LocalObject(
        1802,
        FacilityTurret.Constructor(Vector3(3236.308f, 2316.356f, 79.01418f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1802, 5057)
      LocalObject(
        2381,
        Painbox.Constructor(Vector3(3131.758f, 2208.333f, 59.08848f), painbox),
        owning_building_guid = 45
      )
      LocalObject(
        2393,
        Painbox.Constructor(Vector3(3098.854f, 2213.813f, 67.38509f), painbox_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        2405,
        Painbox.Constructor(Vector3(3116.991f, 2206.628f, 58.77458f), painbox_door_radius),
        owning_building_guid = 45
      )
      LocalObject(
        2423,
        Painbox.Constructor(Vector3(3085.954f, 2215.836f, 65.24308f), painbox_door_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        2424,
        Painbox.Constructor(Vector3(3105.952f, 2198.878f, 66.69748f), painbox_door_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        2425,
        Painbox.Constructor(Vector3(3113.426f, 2214.531f, 65.79138f), painbox_door_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(303, Generator.Constructor(Vector3(3135.067f, 2206.037f, 54.32118f)), owning_building_guid = 45)
      LocalObject(
        291,
        Terminal.Constructor(Vector3(3126.875f, 2206.133f, 55.61518f), gen_control),
        owning_building_guid = 45
      )
    }

    Building9()

    def Building9(): Unit = { // Name: Girru Type: tech_plant GUID: 48, MapID: 9
      LocalBuilding(
        "Girru",
        48,
        9,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4386f, 5932f, 77.53156f),
            Vector3(0f, 0f, 358f),
            tech_plant
          )
        )
      )
      LocalObject(
        225,
        CaptureTerminal.Constructor(Vector3(4389.192f, 5887.772f, 92.63155f), capture_terminal),
        owning_building_guid = 48
      )
      LocalObject(515, Door.Constructor(Vector3(4312.138f, 5864.466f, 79.07355f)), owning_building_guid = 48)
      LocalObject(516, Door.Constructor(Vector3(4312.773f, 5882.646f, 87.03655f)), owning_building_guid = 48)
      LocalObject(517, Door.Constructor(Vector3(4314.712f, 5938.165f, 79.07355f)), owning_building_guid = 48)
      LocalObject(518, Door.Constructor(Vector3(4315.347f, 5956.346f, 87.03655f)), owning_building_guid = 48)
      LocalObject(519, Door.Constructor(Vector3(4342.534f, 5833.457f, 79.15256f)), owning_building_guid = 48)
      LocalObject(520, Door.Constructor(Vector3(4369.237f, 5841.332f, 87.14555f)), owning_building_guid = 48)
      LocalObject(521, Door.Constructor(Vector3(4387.419f, 5840.697f, 79.18256f)), owning_building_guid = 48)
      LocalObject(522, Door.Constructor(Vector3(4395.773f, 5908.219f, 94.15256f)), owning_building_guid = 48)
      LocalObject(523, Door.Constructor(Vector3(4402.058f, 5891.989f, 94.15256f)), owning_building_guid = 48)
      LocalObject(524, Door.Constructor(Vector3(4411.635f, 5962.39f, 79.07355f)), owning_building_guid = 48)
      LocalObject(535, Door.Constructor(Vector3(4429.817f, 5961.755f, 87.03655f)), owning_building_guid = 48)
      LocalObject(536, Door.Constructor(Vector3(4464.445f, 5914.166f, 87.03655f)), owning_building_guid = 48)
      LocalObject(537, Door.Constructor(Vector3(4465.08f, 5932.347f, 79.07355f)), owning_building_guid = 48)
      LocalObject(653, Door.Constructor(Vector3(4339.425f, 5973.653f, 81.26855f)), owning_building_guid = 48)
      LocalObject(657, Door.Constructor(Vector3(4337.471f, 5917.685f, 61.26855f)), owning_building_guid = 48)
      LocalObject(810, Door.Constructor(Vector3(4319.666f, 5866.275f, 71.65256f)), owning_building_guid = 48)
      LocalObject(811, Door.Constructor(Vector3(4320.503f, 5890.26f, 69.15256f)), owning_building_guid = 48)
      LocalObject(812, Door.Constructor(Vector3(4352.484f, 5889.144f, 69.15256f)), owning_building_guid = 48)
      LocalObject(813, Door.Constructor(Vector3(4353.321f, 5913.129f, 69.15256f)), owning_building_guid = 48)
      LocalObject(814, Door.Constructor(Vector3(4366.799f, 5840.614f, 71.65256f)), owning_building_guid = 48)
      LocalObject(815, Door.Constructor(Vector3(4368.195f, 5880.59f, 71.65256f)), owning_building_guid = 48)
      LocalObject(816, Door.Constructor(Vector3(4380.606f, 5892.164f, 74.15256f)), owning_building_guid = 48)
      LocalObject(817, Door.Constructor(Vector3(4380.606f, 5892.164f, 94.15256f)), owning_building_guid = 48)
      LocalObject(818, Door.Constructor(Vector3(4381.165f, 5908.154f, 74.15256f)), owning_building_guid = 48)
      LocalObject(819, Door.Constructor(Vector3(4383.348f, 5856.046f, 71.65256f)), owning_building_guid = 48)
      LocalObject(820, Door.Constructor(Vector3(4384.744f, 5896.022f, 64.15256f)), owning_building_guid = 48)
      LocalObject(821, Door.Constructor(Vector3(4384.883f, 5900.02f, 84.15256f)), owning_building_guid = 48)
      LocalObject(822, Door.Constructor(Vector3(4385.023f, 5904.017f, 94.15256f)), owning_building_guid = 48)
      LocalObject(823, Door.Constructor(Vector3(4404.312f, 5883.331f, 64.15256f)), owning_building_guid = 48)
      LocalObject(824, Door.Constructor(Vector3(4404.312f, 5883.331f, 71.65256f)), owning_building_guid = 48)
      LocalObject(825, Door.Constructor(Vector3(4405.15f, 5907.316f, 71.65256f)), owning_building_guid = 48)
      LocalObject(826, Door.Constructor(Vector3(4413.424f, 5915.033f, 64.15256f)), owning_building_guid = 48)
      LocalObject(827, Door.Constructor(Vector3(4416.445f, 5886.91f, 64.15256f)), owning_building_guid = 48)
      LocalObject(828, Door.Constructor(Vector3(4417.003f, 5902.9f, 71.65256f)), owning_building_guid = 48)
      LocalObject(925, Door.Constructor(Vector3(4424.595f, 5884.965f, 79.91155f)), owning_building_guid = 48)
      LocalObject(2948, Door.Constructor(Vector3(4397.19f, 5889.316f, 71.98556f)), owning_building_guid = 48)
      LocalObject(2949, Door.Constructor(Vector3(4397.445f, 5896.605f, 71.98556f)), owning_building_guid = 48)
      LocalObject(2950, Door.Constructor(Vector3(4397.699f, 5903.89f, 71.98556f)), owning_building_guid = 48)
      LocalObject(
        973,
        IFFLock.Constructor(Vector3(4427.642f, 5882.119f, 79.11156f), Vector3(0, 0, 182)),
        owning_building_guid = 48,
        door_guid = 925
      )
      LocalObject(
        980,
        IFFLock.Constructor(Vector3(4344.76f, 5975.819f, 79.21956f), Vector3(0, 0, 362)),
        owning_building_guid = 48,
        door_guid = 653
      )
      LocalObject(
        1142,
        IFFLock.Constructor(Vector3(4341.649f, 5831.44f, 79.08356f), Vector3(0, 0, 272)),
        owning_building_guid = 48,
        door_guid = 519
      )
      LocalObject(
        1143,
        IFFLock.Constructor(Vector3(4379.068f, 5893.158f, 93.96756f), Vector3(0, 0, 2)),
        owning_building_guid = 48,
        door_guid = 817
      )
      LocalObject(
        1144,
        IFFLock.Constructor(Vector3(4393.759f, 5909.098f, 94.07755f), Vector3(0, 0, 2)),
        owning_building_guid = 48,
        door_guid = 522
      )
      LocalObject(
        1145,
        IFFLock.Constructor(Vector3(4403.607f, 5908.181f, 71.46755f), Vector3(0, 0, 2)),
        owning_building_guid = 48,
        door_guid = 825
      )
      LocalObject(
        1146,
        IFFLock.Constructor(Vector3(4404.08f, 5891.119f, 94.07755f), Vector3(0, 0, 182)),
        owning_building_guid = 48,
        door_guid = 523
      )
      LocalObject(
        1147,
        IFFLock.Constructor(Vector3(4405.855f, 5882.467f, 71.46755f), Vector3(0, 0, 182)),
        owning_building_guid = 48,
        door_guid = 824
      )
      LocalObject(
        1148,
        IFFLock.Constructor(Vector3(4414.963f, 5914.035f, 63.96756f), Vector3(0, 0, 182)),
        owning_building_guid = 48,
        door_guid = 826
      )
      LocalObject(
        1149,
        IFFLock.Constructor(Vector3(4415.451f, 5885.372f, 63.96756f), Vector3(0, 0, 272)),
        owning_building_guid = 48,
        door_guid = 827
      )
      LocalObject(1517, Locker.Constructor(Vector3(4407.948f, 5885.347f, 70.39256f)), owning_building_guid = 48)
      LocalObject(1518, Locker.Constructor(Vector3(4409.111f, 5885.306f, 70.39256f)), owning_building_guid = 48)
      LocalObject(1519, Locker.Constructor(Vector3(4410.258f, 5885.266f, 70.39256f)), owning_building_guid = 48)
      LocalObject(1520, Locker.Constructor(Vector3(4411.406f, 5885.226f, 70.39256f)), owning_building_guid = 48)
      LocalObject(1521, Locker.Constructor(Vector3(4419.271f, 5904.987f, 62.63155f)), owning_building_guid = 48)
      LocalObject(1522, Locker.Constructor(Vector3(4420.595f, 5904.941f, 62.63155f)), owning_building_guid = 48)
      LocalObject(1523, Locker.Constructor(Vector3(4421.93f, 5904.895f, 62.63155f)), owning_building_guid = 48)
      LocalObject(1524, Locker.Constructor(Vector3(4423.266f, 5904.848f, 62.63155f)), owning_building_guid = 48)
      LocalObject(1527, Locker.Constructor(Vector3(4427.803f, 5904.689f, 62.63155f)), owning_building_guid = 48)
      LocalObject(1530, Locker.Constructor(Vector3(4429.126f, 5904.643f, 62.63155f)), owning_building_guid = 48)
      LocalObject(1533, Locker.Constructor(Vector3(4430.462f, 5904.597f, 62.63155f)), owning_building_guid = 48)
      LocalObject(1538, Locker.Constructor(Vector3(4431.798f, 5904.55f, 62.63155f)), owning_building_guid = 48)
      LocalObject(
        176,
        Terminal.Constructor(Vector3(4389.803f, 5906.993f, 93.23456f), air_vehicle_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1889,
        VehicleSpawnPad.Constructor(Vector3(4385.925f, 5927.835f, 90.10956f), mb_pad_creation, Vector3(0, 0, 2)),
        owning_building_guid = 48,
        terminal_guid = 176
      )
      LocalObject(
        177,
        Terminal.Constructor(Vector3(4401.728f, 5906.577f, 93.23456f), air_vehicle_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1890,
        VehicleSpawnPad.Constructor(Vector3(4406.93f, 5927.102f, 90.10956f), mb_pad_creation, Vector3(0, 0, 2)),
        owning_building_guid = 48,
        terminal_guid = 177
      )
      LocalObject(
        2014,
        Terminal.Constructor(Vector3(4388.131f, 5905.396f, 83.96156f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2015,
        Terminal.Constructor(Vector3(4411.221f, 5890.502f, 71.72156f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2016,
        Terminal.Constructor(Vector3(4411.352f, 5894.231f, 71.72156f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2017,
        Terminal.Constructor(Vector3(4411.483f, 5898.018f, 71.72156f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2853,
        Terminal.Constructor(Vector3(4358.627f, 5837.489f, 71.68855f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2854,
        Terminal.Constructor(Vector3(4392.283f, 5884.343f, 64.18855f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2855,
        Terminal.Constructor(Vector3(4397.401f, 5886.817f, 72.26556f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2856,
        Terminal.Constructor(Vector3(4397.651f, 5894.105f, 72.26556f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2857,
        Terminal.Constructor(Vector3(4397.909f, 5901.389f, 72.26556f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2858,
        Terminal.Constructor(Vector3(4400.635f, 5934.706f, 89.55456f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2859,
        Terminal.Constructor(Vector3(4422.509f, 5910.352f, 84.21355f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3072,
        Terminal.Constructor(Vector3(4336.609f, 5893.123f, 63.34555f), ground_vehicle_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1888,
        VehicleSpawnPad.Constructor(Vector3(4336.939f, 5904.034f, 55.06856f), mb_pad_creation, Vector3(0, 0, 2)),
        owning_building_guid = 48,
        terminal_guid = 3072
      )
      LocalObject(2658, ResourceSilo.Constructor(Vector3(4448.851f, 5962.38f, 84.53956f)), owning_building_guid = 48)
      LocalObject(
        2724,
        SpawnTube.Constructor(Vector3(4396.714f, 5888.282f, 70.13155f), Vector3(0, 0, 2)),
        owning_building_guid = 48
      )
      LocalObject(
        2725,
        SpawnTube.Constructor(Vector3(4396.968f, 5895.569f, 70.13155f), Vector3(0, 0, 2)),
        owning_building_guid = 48
      )
      LocalObject(
        2726,
        SpawnTube.Constructor(Vector3(4397.223f, 5902.853f, 70.13155f), Vector3(0, 0, 2)),
        owning_building_guid = 48
      )
      LocalObject(
        1909,
        ProximityTerminal.Constructor(Vector3(4387.762f, 5894.817f, 82.62856f), medical_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1910,
        ProximityTerminal.Constructor(Vector3(4425.499f, 5904.225f, 62.63155f), medical_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2243,
        ProximityTerminal.Constructor(Vector3(4323.892f, 5909.815f, 85.72655f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2244,
        Terminal.Constructor(Vector3(4323.892f, 5909.815f, 85.72655f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2246,
        ProximityTerminal.Constructor(Vector3(4344.72f, 5925.27f, 88.08156f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2247,
        Terminal.Constructor(Vector3(4344.72f, 5925.27f, 88.08156f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2249,
        ProximityTerminal.Constructor(Vector3(4393.122f, 5867.186f, 92.97855f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2250,
        Terminal.Constructor(Vector3(4393.122f, 5867.186f, 92.97855f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2252,
        ProximityTerminal.Constructor(Vector3(4408.679f, 5849.787f, 85.73956f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2253,
        Terminal.Constructor(Vector3(4408.679f, 5849.787f, 85.73956f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2255,
        ProximityTerminal.Constructor(Vector3(4446.479f, 5887.717f, 88.18356f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2256,
        Terminal.Constructor(Vector3(4446.479f, 5887.717f, 88.18356f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2258,
        ProximityTerminal.Constructor(Vector3(4453.334f, 5903.735f, 85.73956f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2259,
        Terminal.Constructor(Vector3(4453.334f, 5903.735f, 85.73956f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2611,
        ProximityTerminal.Constructor(Vector3(4387.762f, 5973.601f, 77.26006f), repair_silo),
        owning_building_guid = 48
      )
      LocalObject(
        2612,
        Terminal.Constructor(Vector3(4387.762f, 5973.601f, 77.26006f), ground_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2615,
        ProximityTerminal.Constructor(Vector3(4441.362f, 5837.218f, 77.28156f), repair_silo),
        owning_building_guid = 48
      )
      LocalObject(
        2616,
        Terminal.Constructor(Vector3(4441.362f, 5837.218f, 77.28156f), ground_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1829,
        FacilityTurret.Constructor(Vector3(4303.994f, 5835.468f, 86.03056f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1829, 5058)
      LocalObject(
        1830,
        FacilityTurret.Constructor(Vector3(4305.407f, 6033.729f, 86.03056f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1830, 5059)
      LocalObject(
        1831,
        FacilityTurret.Constructor(Vector3(4395.047f, 6030.599f, 86.03056f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1831, 5060)
      LocalObject(
        1834,
        FacilityTurret.Constructor(Vector3(4468.634f, 5829.711f, 86.03056f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1834, 5061)
      LocalObject(
        1835,
        FacilityTurret.Constructor(Vector3(4473.441f, 5967.368f, 86.03056f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1835, 5062)
      LocalObject(
        1836,
        FacilityTurret.Constructor(Vector3(4478.617f, 5894.167f, 86.03056f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1836, 5063)
      LocalObject(
        2385,
        Painbox.Constructor(Vector3(4411.589f, 5927.311f, 66.10486f), painbox),
        owning_building_guid = 48
      )
      LocalObject(
        2397,
        Painbox.Constructor(Vector3(4405.536f, 5894.508f, 74.40146f), painbox_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        2409,
        Painbox.Constructor(Vector3(4413.036f, 5912.516f, 65.79095f), painbox_door_radius),
        owning_building_guid = 48
      )
      LocalObject(
        2435,
        Painbox.Constructor(Vector3(4403.288f, 5881.645f, 72.25945f), painbox_door_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        2436,
        Painbox.Constructor(Vector3(4405.073f, 5909.089f, 72.80775f), painbox_door_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        2437,
        Painbox.Constructor(Vector3(4420.592f, 5901.344f, 73.71385f), painbox_door_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(307, Generator.Constructor(Vector3(4413.942f, 5930.579f, 61.33755f)), owning_building_guid = 48)
      LocalObject(
        295,
        Terminal.Constructor(Vector3(4413.704f, 5922.39f, 62.63155f), gen_control),
        owning_building_guid = 48
      )
    }

    Building12()

    def Building12(): Unit = { // Name: Kusag Type: tech_plant GUID: 51, MapID: 12
      LocalBuilding(
        "Kusag",
        51,
        12,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(6560f, 4574f, 45.59388f),
            Vector3(0f, 0f, 141f),
            tech_plant
          )
        )
      )
      LocalObject(
        228,
        CaptureTerminal.Constructor(Vector3(6584.067f, 4611.243f, 60.69388f), capture_terminal),
        owning_building_guid = 51
      )
      LocalObject(617, Door.Constructor(Vector3(6496.635f, 4621.314f, 47.13588f)), owning_building_guid = 51)
      LocalObject(618, Door.Constructor(Vector3(6507.099f, 4576.606f, 55.09888f)), owning_building_guid = 51)
      LocalObject(619, Door.Constructor(Vector3(6508.084f, 4635.453f, 55.09888f)), owning_building_guid = 51)
      LocalObject(620, Door.Constructor(Vector3(6521.238f, 4565.157f, 47.13588f)), owning_building_guid = 51)
      LocalObject(621, Door.Constructor(Vector3(6566.506f, 4598.874f, 62.21487f)), owning_building_guid = 51)
      LocalObject(622, Door.Constructor(Vector3(6571.255f, 4615.618f, 62.21487f)), owning_building_guid = 51)
      LocalObject(623, Door.Constructor(Vector3(6601.775f, 4512.037f, 55.09888f)), owning_building_guid = 51)
      LocalObject(624, Door.Constructor(Vector3(6613.223f, 4526.174f, 47.13588f)), owning_building_guid = 51)
      LocalObject(625, Door.Constructor(Vector3(6613.814f, 4647.772f, 47.24488f)), owning_building_guid = 51)
      LocalObject(626, Door.Constructor(Vector3(6627.953f, 4636.323f, 55.20788f)), owning_building_guid = 51)
      LocalObject(627, Door.Constructor(Vector3(6648.184f, 4569.346f, 55.09888f)), owning_building_guid = 51)
      LocalObject(628, Door.Constructor(Vector3(6654.018f, 4626.542f, 47.21487f)), owning_building_guid = 51)
      LocalObject(629, Door.Constructor(Vector3(6659.632f, 4583.484f, 47.13588f)), owning_building_guid = 51)
      LocalObject(654, Door.Constructor(Vector3(6572.129f, 4512.705f, 49.33088f)), owning_building_guid = 51)
      LocalObject(658, Door.Constructor(Vector3(6607.372f, 4556.227f, 29.33088f)), owning_building_guid = 51)
      LocalObject(876, Door.Constructor(Vector3(6548.309f, 4604.055f, 32.21487f)), owning_building_guid = 51)
      LocalObject(877, Door.Constructor(Vector3(6552.752f, 4615.898f, 39.71487f)), owning_building_guid = 51)
      LocalObject(878, Door.Constructor(Vector3(6559.561f, 4605.238f, 39.71487f)), owning_building_guid = 51)
      LocalObject(879, Door.Constructor(Vector3(6562.821f, 4628.333f, 32.21487f)), owning_building_guid = 51)
      LocalObject(880, Door.Constructor(Vector3(6574.665f, 4623.89f, 32.21487f)), owning_building_guid = 51)
      LocalObject(881, Door.Constructor(Vector3(6574.665f, 4623.89f, 39.71487f)), owning_building_guid = 51)
      LocalObject(882, Door.Constructor(Vector3(6577.621f, 4595.76f, 62.21487f)), owning_building_guid = 51)
      LocalObject(883, Door.Constructor(Vector3(6578.212f, 4590.134f, 42.21487f)), owning_building_guid = 51)
      LocalObject(884, Door.Constructor(Vector3(6580.138f, 4598.869f, 52.21487f)), owning_building_guid = 51)
      LocalObject(885, Door.Constructor(Vector3(6582.656f, 4601.977f, 32.21487f)), owning_building_guid = 51)
      LocalObject(886, Door.Constructor(Vector3(6588.281f, 4602.568f, 42.21487f)), owning_building_guid = 51)
      LocalObject(887, Door.Constructor(Vector3(6588.281f, 4602.568f, 62.21487f)), owning_building_guid = 51)
      LocalObject(888, Door.Constructor(Vector3(6597.455f, 4569.405f, 37.21487f)), owning_building_guid = 51)
      LocalObject(889, Door.Constructor(Vector3(6605.159f, 4604.342f, 39.71487f)), owning_building_guid = 51)
      LocalObject(890, Door.Constructor(Vector3(6607.828f, 4633.063f, 39.71487f)), owning_building_guid = 51)
      LocalObject(891, Door.Constructor(Vector3(6612.559f, 4588.056f, 37.21487f)), owning_building_guid = 51)
      LocalObject(892, Door.Constructor(Vector3(6630.332f, 4635.428f, 39.71487f)), owning_building_guid = 51)
      LocalObject(893, Door.Constructor(Vector3(6637.427f, 4567.918f, 37.21487f)), owning_building_guid = 51)
      LocalObject(894, Door.Constructor(Vector3(6652.531f, 4586.569f, 39.71487f)), owning_building_guid = 51)
      LocalObject(928, Door.Constructor(Vector3(6557.483f, 4634.791f, 47.97388f)), owning_building_guid = 51)
      LocalObject(2983, Door.Constructor(Vector3(6567.574f, 4603.491f, 40.04787f)), owning_building_guid = 51)
      LocalObject(2984, Door.Constructor(Vector3(6572.161f, 4609.155f, 40.04787f)), owning_building_guid = 51)
      LocalObject(2985, Door.Constructor(Vector3(6576.75f, 4614.823f, 40.04787f)), owning_building_guid = 51)
      LocalObject(
        976,
        IFFLock.Constructor(Vector3(6556.763f, 4638.897f, 47.17388f), Vector3(0, 0, 39)),
        owning_building_guid = 51,
        door_guid = 928
      )
      LocalObject(
        981,
        IFFLock.Constructor(Vector3(6566.565f, 4514.186f, 47.28188f), Vector3(0, 0, 219)),
        owning_building_guid = 51,
        door_guid = 654
      )
      LocalObject(
        1231,
        IFFLock.Constructor(Vector3(6547.681f, 4605.777f, 32.02988f), Vector3(0, 0, 39)),
        owning_building_guid = 51,
        door_guid = 876
      )
      LocalObject(
        1232,
        IFFLock.Constructor(Vector3(6560.272f, 4603.619f, 39.52988f), Vector3(0, 0, 219)),
        owning_building_guid = 51,
        door_guid = 878
      )
      LocalObject(
        1233,
        IFFLock.Constructor(Vector3(6564.541f, 4628.963f, 32.02988f), Vector3(0, 0, 129)),
        owning_building_guid = 51,
        door_guid = 879
      )
      LocalObject(
        1234,
        IFFLock.Constructor(Vector3(6567.585f, 4596.96f, 62.13988f), Vector3(0, 0, 219)),
        owning_building_guid = 51,
        door_guid = 621
      )
      LocalObject(
        1235,
        IFFLock.Constructor(Vector3(6570.163f, 4617.53f, 62.13988f), Vector3(0, 0, 39)),
        owning_building_guid = 51,
        door_guid = 622
      )
      LocalObject(
        1236,
        IFFLock.Constructor(Vector3(6573.953f, 4625.508f, 39.52988f), Vector3(0, 0, 39)),
        owning_building_guid = 51,
        door_guid = 881
      )
      LocalObject(
        1237,
        IFFLock.Constructor(Vector3(6588.912f, 4600.849f, 62.02988f), Vector3(0, 0, 219)),
        owning_building_guid = 51,
        door_guid = 887
      )
      LocalObject(
        1238,
        IFFLock.Constructor(Vector3(6655.938f, 4627.62f, 47.14587f), Vector3(0, 0, 129)),
        owning_building_guid = 51,
        door_guid = 628
      )
      LocalObject(1666, Locker.Constructor(Vector3(6539.944f, 4623.484f, 30.69388f)), owning_building_guid = 51)
      LocalObject(1667, Locker.Constructor(Vector3(6540.983f, 4622.643f, 30.69388f)), owning_building_guid = 51)
      LocalObject(1668, Locker.Constructor(Vector3(6542.021f, 4621.802f, 30.69388f)), owning_building_guid = 51)
      LocalObject(1669, Locker.Constructor(Vector3(6543.05f, 4620.969f, 30.69388f)), owning_building_guid = 51)
      LocalObject(1670, Locker.Constructor(Vector3(6546.579f, 4618.112f, 30.69388f)), owning_building_guid = 51)
      LocalObject(1671, Locker.Constructor(Vector3(6547.618f, 4617.271f, 30.69388f)), owning_building_guid = 51)
      LocalObject(1672, Locker.Constructor(Vector3(6548.656f, 4616.43f, 30.69388f)), owning_building_guid = 51)
      LocalObject(1673, Locker.Constructor(Vector3(6549.685f, 4615.597f, 30.69388f)), owning_building_guid = 51)
      LocalObject(1674, Locker.Constructor(Vector3(6567.859f, 4626.646f, 38.45488f)), owning_building_guid = 51)
      LocalObject(1675, Locker.Constructor(Vector3(6568.752f, 4625.922f, 38.45488f)), owning_building_guid = 51)
      LocalObject(1676, Locker.Constructor(Vector3(6569.644f, 4625.2f, 38.45488f)), owning_building_guid = 51)
      LocalObject(1677, Locker.Constructor(Vector3(6570.548f, 4624.468f, 38.45488f)), owning_building_guid = 51)
      LocalObject(
        178,
        Terminal.Constructor(Vector3(6562.74f, 4603.769f, 61.29688f), air_vehicle_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1893,
        VehicleSpawnPad.Constructor(Vector3(6546.232f, 4590.508f, 58.17188f), mb_pad_creation, Vector3(0, 0, 219)),
        owning_building_guid = 51,
        terminal_guid = 178
      )
      LocalObject(
        179,
        Terminal.Constructor(Vector3(6572.013f, 4596.26f, 61.29688f), air_vehicle_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1894,
        VehicleSpawnPad.Constructor(Vector3(6562.567f, 4577.281f, 58.17188f), mb_pad_creation, Vector3(0, 0, 219)),
        owning_building_guid = 51,
        terminal_guid = 179
      )
      LocalObject(
        2068,
        Terminal.Constructor(Vector3(6560.099f, 4616.476f, 39.78387f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2069,
        Terminal.Constructor(Vector3(6562.483f, 4619.42f, 39.78387f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2070,
        Terminal.Constructor(Vector3(6564.832f, 4622.32f, 39.78387f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2071,
        Terminal.Constructor(Vector3(6574.309f, 4596.53f, 52.02388f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2873,
        Terminal.Constructor(Vector3(6543.871f, 4613.261f, 52.27588f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2874,
        Terminal.Constructor(Vector3(6546.683f, 4580.647f, 57.61687f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2875,
        Terminal.Constructor(Vector3(6568.912f, 4605.614f, 40.32788f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2876,
        Terminal.Constructor(Vector3(6573.5f, 4611.276f, 40.32788f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2877,
        Terminal.Constructor(Vector3(6578.086f, 4616.946f, 40.32788f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2878,
        Terminal.Constructor(Vector3(6583.664f, 4615.842f, 32.25088f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2879,
        Terminal.Constructor(Vector3(6638.739f, 4633.006f, 39.75088f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3075,
        Terminal.Constructor(Vector3(6622.842f, 4575.324f, 31.40788f), ground_vehicle_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1895,
        VehicleSpawnPad.Constructor(Vector3(6616.012f, 4566.809f, 23.13088f), mb_pad_creation, Vector3(0, 0, 219)),
        owning_building_guid = 51,
        terminal_guid = 3075
      )
      LocalObject(2661, ResourceSilo.Constructor(Vector3(6491.522f, 4587.562f, 52.60188f)), owning_building_guid = 51)
      LocalObject(
        2759,
        SpawnTube.Constructor(Vector3(6568.579f, 4604.032f, 38.19387f), Vector3(0, 0, 219)),
        owning_building_guid = 51
      )
      LocalObject(
        2760,
        SpawnTube.Constructor(Vector3(6573.165f, 4609.696f, 38.19387f), Vector3(0, 0, 219)),
        owning_building_guid = 51
      )
      LocalObject(
        2761,
        SpawnTube.Constructor(Vector3(6577.753f, 4615.362f, 38.19387f), Vector3(0, 0, 219)),
        owning_building_guid = 51
      )
      LocalObject(
        1914,
        ProximityTerminal.Constructor(Vector3(6545.17f, 4619.953f, 30.69388f), medical_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1915,
        ProximityTerminal.Constructor(Vector3(6580.97f, 4604.756f, 50.69088f), medical_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2276,
        ProximityTerminal.Constructor(Vector3(6523.234f, 4637.097f, 53.80188f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2277,
        Terminal.Constructor(Vector3(6523.234f, 4637.097f, 53.80188f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2279,
        ProximityTerminal.Constructor(Vector3(6538.35f, 4645.763f, 56.24588f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2280,
        Terminal.Constructor(Vector3(6538.35f, 4645.763f, 56.24588f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2282,
        ProximityTerminal.Constructor(Vector3(6591.365f, 4653.307f, 53.80188f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2283,
        Terminal.Constructor(Vector3(6591.365f, 4653.307f, 53.80188f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2285,
        ProximityTerminal.Constructor(Vector3(6593.319f, 4630.048f, 61.04087f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2286,
        Terminal.Constructor(Vector3(6593.319f, 4630.048f, 61.04087f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2288,
        ProximityTerminal.Constructor(Vector3(6597.018f, 4554.532f, 56.14388f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2289,
        Terminal.Constructor(Vector3(6597.018f, 4554.532f, 56.14388f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2291,
        ProximityTerminal.Constructor(Vector3(6622.953f, 4554.34f, 53.78888f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2292,
        Terminal.Constructor(Vector3(6622.953f, 4554.34f, 53.78888f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2635,
        ProximityTerminal.Constructor(Vector3(6533.558f, 4541.836f, 45.32238f), repair_silo),
        owning_building_guid = 51
      )
      LocalObject(
        2636,
        Terminal.Constructor(Vector3(6533.558f, 4541.836f, 45.32238f), ground_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2639,
        ProximityTerminal.Constructor(Vector3(6572.827f, 4683.015f, 45.34388f), repair_silo),
        owning_building_guid = 51
      )
      LocalObject(
        2640,
        Terminal.Constructor(Vector3(6572.827f, 4683.015f, 45.34388f), ground_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1862,
        FacilityTurret.Constructor(Vector3(6468.881f, 4598.377f, 54.09288f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1862, 5064)
      LocalObject(
        1863,
        FacilityTurret.Constructor(Vector3(6493.436f, 4500.7f, 54.09288f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1863, 5065)
      LocalObject(
        1864,
        FacilityTurret.Constructor(Vector3(6508.801f, 4659.953f, 54.09288f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1864, 5066)
      LocalObject(
        1865,
        FacilityTurret.Constructor(Vector3(6555.564f, 4705.422f, 54.09288f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1865, 5067)
      LocalObject(
        1866,
        FacilityTurret.Constructor(Vector3(6563.142f, 4444.253f, 54.09288f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1866, 5068)
      LocalObject(
        1867,
        FacilityTurret.Constructor(Vector3(6683.587f, 4601.741f, 54.09288f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1867, 5069)
      LocalObject(
        2388,
        Painbox.Constructor(Vector3(6542.386f, 4593.145f, 34.16718f), painbox),
        owning_building_guid = 51
      )
      LocalObject(
        2400,
        Painbox.Constructor(Vector3(6566.961f, 4615.7f, 42.46378f), painbox_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        2412,
        Painbox.Constructor(Vector3(6550.134f, 4605.832f, 33.85328f), painbox_door_radius),
        owning_building_guid = 51
      )
      LocalObject(
        2444,
        Painbox.Constructor(Vector3(6550.823f, 4619.301f, 41.77618f), painbox_door_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        2445,
        Painbox.Constructor(Vector3(6558.556f, 4603.775f, 40.87008f), painbox_door_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        2446,
        Painbox.Constructor(Vector3(6576.498f, 4624.62f, 40.32178f), painbox_door_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(310, Generator.Constructor(Vector3(6538.54f, 4591.951f, 29.39988f)), owning_building_guid = 51)
      LocalObject(
        298,
        Terminal.Constructor(Vector3(6543.658f, 4598.347f, 30.69388f), gen_control),
        owning_building_guid = 51
      )
    }

    Building38()

    def Building38(): Unit = { // Name: NE_Baal_Tower Type: tower_a GUID: 54, MapID: 38
      LocalBuilding(
        "NE_Baal_Tower",
        54,
        38,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1138f, 5212f, 91.18148f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2769,
        CaptureTerminal.Constructor(Vector3(1154.587f, 5211.897f, 101.1805f), secondary_capture),
        owning_building_guid = 54
      )
      LocalObject(327, Door.Constructor(Vector3(1150f, 5204f, 92.70248f)), owning_building_guid = 54)
      LocalObject(328, Door.Constructor(Vector3(1150f, 5204f, 112.7015f)), owning_building_guid = 54)
      LocalObject(329, Door.Constructor(Vector3(1150f, 5220f, 92.70248f)), owning_building_guid = 54)
      LocalObject(330, Door.Constructor(Vector3(1150f, 5220f, 112.7015f)), owning_building_guid = 54)
      LocalObject(2890, Door.Constructor(Vector3(1149.146f, 5200.794f, 82.51748f)), owning_building_guid = 54)
      LocalObject(2891, Door.Constructor(Vector3(1149.146f, 5217.204f, 82.51748f)), owning_building_guid = 54)
      LocalObject(
        990,
        IFFLock.Constructor(Vector3(1147.957f, 5220.811f, 92.64248f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 329
      )
      LocalObject(
        991,
        IFFLock.Constructor(Vector3(1147.957f, 5220.811f, 112.6425f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 330
      )
      LocalObject(
        992,
        IFFLock.Constructor(Vector3(1152.047f, 5203.189f, 92.64248f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 327
      )
      LocalObject(
        993,
        IFFLock.Constructor(Vector3(1152.047f, 5203.189f, 112.6425f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 328
      )
      LocalObject(1267, Locker.Constructor(Vector3(1153.716f, 5196.963f, 81.17548f)), owning_building_guid = 54)
      LocalObject(1268, Locker.Constructor(Vector3(1153.751f, 5218.835f, 81.17548f)), owning_building_guid = 54)
      LocalObject(1269, Locker.Constructor(Vector3(1155.053f, 5196.963f, 81.17548f)), owning_building_guid = 54)
      LocalObject(1270, Locker.Constructor(Vector3(1155.088f, 5218.835f, 81.17548f)), owning_building_guid = 54)
      LocalObject(1271, Locker.Constructor(Vector3(1157.741f, 5196.963f, 81.17548f)), owning_building_guid = 54)
      LocalObject(1272, Locker.Constructor(Vector3(1157.741f, 5218.835f, 81.17548f)), owning_building_guid = 54)
      LocalObject(1273, Locker.Constructor(Vector3(1159.143f, 5196.963f, 81.17548f)), owning_building_guid = 54)
      LocalObject(1274, Locker.Constructor(Vector3(1159.143f, 5218.835f, 81.17548f)), owning_building_guid = 54)
      LocalObject(
        1921,
        Terminal.Constructor(Vector3(1159.445f, 5202.129f, 82.51348f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1922,
        Terminal.Constructor(Vector3(1159.445f, 5207.853f, 82.51348f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1923,
        Terminal.Constructor(Vector3(1159.445f, 5213.234f, 82.51348f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2666,
        SpawnTube.Constructor(Vector3(1148.706f, 5199.742f, 80.66348f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        2667,
        SpawnTube.Constructor(Vector3(1148.706f, 5216.152f, 80.66348f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        1765,
        FacilityTurret.Constructor(Vector3(1125.32f, 5199.295f, 110.1235f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(1765, 5070)
      LocalObject(
        1766,
        FacilityTurret.Constructor(Vector3(1160.647f, 5224.707f, 110.1235f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(1766, 5071)
      LocalObject(
        2450,
        Painbox.Constructor(Vector3(1143.235f, 5205.803f, 82.68058f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        2451,
        Painbox.Constructor(Vector3(1154.889f, 5214.086f, 81.28148f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        2452,
        Painbox.Constructor(Vector3(1154.975f, 5202.223f, 81.28148f), painbox_radius_continuous),
        owning_building_guid = 54
      )
    }

    Building37()

    def Building37(): Unit = { // Name: E_Dagon_Tower Type: tower_a GUID: 55, MapID: 37
      LocalBuilding(
        "E_Dagon_Tower",
        55,
        37,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1904f, 6034f, 43.95589f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2774,
        CaptureTerminal.Constructor(Vector3(1920.587f, 6033.897f, 53.95489f), secondary_capture),
        owning_building_guid = 55
      )
      LocalObject(365, Door.Constructor(Vector3(1916f, 6026f, 45.47689f)), owning_building_guid = 55)
      LocalObject(366, Door.Constructor(Vector3(1916f, 6026f, 65.47589f)), owning_building_guid = 55)
      LocalObject(367, Door.Constructor(Vector3(1916f, 6042f, 45.47689f)), owning_building_guid = 55)
      LocalObject(368, Door.Constructor(Vector3(1916f, 6042f, 65.47589f)), owning_building_guid = 55)
      LocalObject(2903, Door.Constructor(Vector3(1915.146f, 6022.794f, 35.29189f)), owning_building_guid = 55)
      LocalObject(2904, Door.Constructor(Vector3(1915.146f, 6039.204f, 35.29189f)), owning_building_guid = 55)
      LocalObject(
        1024,
        IFFLock.Constructor(Vector3(1913.957f, 6042.811f, 45.41689f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 367
      )
      LocalObject(
        1025,
        IFFLock.Constructor(Vector3(1913.957f, 6042.811f, 65.41689f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 368
      )
      LocalObject(
        1026,
        IFFLock.Constructor(Vector3(1918.047f, 6025.189f, 45.41689f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 365
      )
      LocalObject(
        1027,
        IFFLock.Constructor(Vector3(1918.047f, 6025.189f, 65.41689f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 366
      )
      LocalObject(1328, Locker.Constructor(Vector3(1919.716f, 6018.963f, 33.94989f)), owning_building_guid = 55)
      LocalObject(1329, Locker.Constructor(Vector3(1919.751f, 6040.835f, 33.94989f)), owning_building_guid = 55)
      LocalObject(1330, Locker.Constructor(Vector3(1921.053f, 6018.963f, 33.94989f)), owning_building_guid = 55)
      LocalObject(1331, Locker.Constructor(Vector3(1921.088f, 6040.835f, 33.94989f)), owning_building_guid = 55)
      LocalObject(1332, Locker.Constructor(Vector3(1923.741f, 6018.963f, 33.94989f)), owning_building_guid = 55)
      LocalObject(1333, Locker.Constructor(Vector3(1923.741f, 6040.835f, 33.94989f)), owning_building_guid = 55)
      LocalObject(1334, Locker.Constructor(Vector3(1925.143f, 6018.963f, 33.94989f)), owning_building_guid = 55)
      LocalObject(1335, Locker.Constructor(Vector3(1925.143f, 6040.835f, 33.94989f)), owning_building_guid = 55)
      LocalObject(
        1940,
        Terminal.Constructor(Vector3(1925.445f, 6024.129f, 35.28789f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1941,
        Terminal.Constructor(Vector3(1925.445f, 6029.853f, 35.28789f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1942,
        Terminal.Constructor(Vector3(1925.445f, 6035.234f, 35.28789f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        2679,
        SpawnTube.Constructor(Vector3(1914.706f, 6021.742f, 33.43789f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        2680,
        SpawnTube.Constructor(Vector3(1914.706f, 6038.152f, 33.43789f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        1774,
        FacilityTurret.Constructor(Vector3(1891.32f, 6021.295f, 62.89789f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1774, 5072)
      LocalObject(
        1775,
        FacilityTurret.Constructor(Vector3(1926.647f, 6046.707f, 62.89789f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1775, 5073)
      LocalObject(
        2465,
        Painbox.Constructor(Vector3(1909.235f, 6027.803f, 35.45499f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        2466,
        Painbox.Constructor(Vector3(1920.889f, 6036.086f, 34.05589f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        2467,
        Painbox.Constructor(Vector3(1920.975f, 6024.223f, 34.05589f), painbox_radius_continuous),
        owning_building_guid = 55
      )
    }

    Building65()

    def Building65(): Unit = { // Name: W_Hanish_Tower Type: tower_a GUID: 56, MapID: 65
      LocalBuilding(
        "W_Hanish_Tower",
        56,
        65,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2992f, 5714f, 55.85485f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2778,
        CaptureTerminal.Constructor(Vector3(3008.587f, 5713.897f, 65.85385f), secondary_capture),
        owning_building_guid = 56
      )
      LocalObject(404, Door.Constructor(Vector3(3004f, 5706f, 57.37585f)), owning_building_guid = 56)
      LocalObject(405, Door.Constructor(Vector3(3004f, 5706f, 77.37485f)), owning_building_guid = 56)
      LocalObject(406, Door.Constructor(Vector3(3004f, 5722f, 57.37585f)), owning_building_guid = 56)
      LocalObject(407, Door.Constructor(Vector3(3004f, 5722f, 77.37485f)), owning_building_guid = 56)
      LocalObject(2914, Door.Constructor(Vector3(3003.146f, 5702.794f, 47.19085f)), owning_building_guid = 56)
      LocalObject(2915, Door.Constructor(Vector3(3003.146f, 5719.204f, 47.19085f)), owning_building_guid = 56)
      LocalObject(
        1054,
        IFFLock.Constructor(Vector3(3001.957f, 5722.811f, 57.31585f), Vector3(0, 0, 0)),
        owning_building_guid = 56,
        door_guid = 406
      )
      LocalObject(
        1055,
        IFFLock.Constructor(Vector3(3001.957f, 5722.811f, 77.31585f), Vector3(0, 0, 0)),
        owning_building_guid = 56,
        door_guid = 407
      )
      LocalObject(
        1056,
        IFFLock.Constructor(Vector3(3006.047f, 5705.189f, 57.31585f), Vector3(0, 0, 180)),
        owning_building_guid = 56,
        door_guid = 404
      )
      LocalObject(
        1057,
        IFFLock.Constructor(Vector3(3006.047f, 5705.189f, 77.31585f), Vector3(0, 0, 180)),
        owning_building_guid = 56,
        door_guid = 405
      )
      LocalObject(1372, Locker.Constructor(Vector3(3007.716f, 5698.963f, 45.84885f)), owning_building_guid = 56)
      LocalObject(1373, Locker.Constructor(Vector3(3007.751f, 5720.835f, 45.84885f)), owning_building_guid = 56)
      LocalObject(1374, Locker.Constructor(Vector3(3009.053f, 5698.963f, 45.84885f)), owning_building_guid = 56)
      LocalObject(1375, Locker.Constructor(Vector3(3009.088f, 5720.835f, 45.84885f)), owning_building_guid = 56)
      LocalObject(1376, Locker.Constructor(Vector3(3011.741f, 5698.963f, 45.84885f)), owning_building_guid = 56)
      LocalObject(1377, Locker.Constructor(Vector3(3011.741f, 5720.835f, 45.84885f)), owning_building_guid = 56)
      LocalObject(1378, Locker.Constructor(Vector3(3013.143f, 5698.963f, 45.84885f)), owning_building_guid = 56)
      LocalObject(1379, Locker.Constructor(Vector3(3013.143f, 5720.835f, 45.84885f)), owning_building_guid = 56)
      LocalObject(
        1960,
        Terminal.Constructor(Vector3(3013.445f, 5704.129f, 47.18685f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1961,
        Terminal.Constructor(Vector3(3013.445f, 5709.853f, 47.18685f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1962,
        Terminal.Constructor(Vector3(3013.445f, 5715.234f, 47.18685f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2690,
        SpawnTube.Constructor(Vector3(3002.706f, 5701.742f, 45.33685f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 56
      )
      LocalObject(
        2691,
        SpawnTube.Constructor(Vector3(3002.706f, 5718.152f, 45.33685f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 56
      )
      LocalObject(
        1790,
        FacilityTurret.Constructor(Vector3(2979.32f, 5701.295f, 74.79685f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1790, 5074)
      LocalObject(
        1791,
        FacilityTurret.Constructor(Vector3(3014.647f, 5726.707f, 74.79685f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1791, 5075)
      LocalObject(
        2477,
        Painbox.Constructor(Vector3(2997.235f, 5707.803f, 47.35395f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2478,
        Painbox.Constructor(Vector3(3008.889f, 5716.086f, 45.95485f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2479,
        Painbox.Constructor(Vector3(3008.975f, 5704.223f, 45.95485f), painbox_radius_continuous),
        owning_building_guid = 56
      )
    }

    Building17()

    def Building17(): Unit = { // Name: S_Marduk_Tower Type: tower_a GUID: 57, MapID: 17
      LocalBuilding(
        "S_Marduk_Tower",
        57,
        17,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3212f, 1592f, 50.06226f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2780,
        CaptureTerminal.Constructor(Vector3(3228.587f, 1591.897f, 60.06126f), secondary_capture),
        owning_building_guid = 57
      )
      LocalObject(428, Door.Constructor(Vector3(3224f, 1584f, 51.58326f)), owning_building_guid = 57)
      LocalObject(429, Door.Constructor(Vector3(3224f, 1584f, 71.58226f)), owning_building_guid = 57)
      LocalObject(430, Door.Constructor(Vector3(3224f, 1600f, 51.58326f)), owning_building_guid = 57)
      LocalObject(431, Door.Constructor(Vector3(3224f, 1600f, 71.58226f)), owning_building_guid = 57)
      LocalObject(2921, Door.Constructor(Vector3(3223.146f, 1580.794f, 41.39826f)), owning_building_guid = 57)
      LocalObject(2922, Door.Constructor(Vector3(3223.146f, 1597.204f, 41.39826f)), owning_building_guid = 57)
      LocalObject(
        1072,
        IFFLock.Constructor(Vector3(3221.957f, 1600.811f, 51.52326f), Vector3(0, 0, 0)),
        owning_building_guid = 57,
        door_guid = 430
      )
      LocalObject(
        1073,
        IFFLock.Constructor(Vector3(3221.957f, 1600.811f, 71.52326f), Vector3(0, 0, 0)),
        owning_building_guid = 57,
        door_guid = 431
      )
      LocalObject(
        1074,
        IFFLock.Constructor(Vector3(3226.047f, 1583.189f, 51.52326f), Vector3(0, 0, 180)),
        owning_building_guid = 57,
        door_guid = 428
      )
      LocalObject(
        1075,
        IFFLock.Constructor(Vector3(3226.047f, 1583.189f, 71.52326f), Vector3(0, 0, 180)),
        owning_building_guid = 57,
        door_guid = 429
      )
      LocalObject(1400, Locker.Constructor(Vector3(3227.716f, 1576.963f, 40.05626f)), owning_building_guid = 57)
      LocalObject(1401, Locker.Constructor(Vector3(3227.751f, 1598.835f, 40.05626f)), owning_building_guid = 57)
      LocalObject(1402, Locker.Constructor(Vector3(3229.053f, 1576.963f, 40.05626f)), owning_building_guid = 57)
      LocalObject(1403, Locker.Constructor(Vector3(3229.088f, 1598.835f, 40.05626f)), owning_building_guid = 57)
      LocalObject(1404, Locker.Constructor(Vector3(3231.741f, 1576.963f, 40.05626f)), owning_building_guid = 57)
      LocalObject(1405, Locker.Constructor(Vector3(3231.741f, 1598.835f, 40.05626f)), owning_building_guid = 57)
      LocalObject(1406, Locker.Constructor(Vector3(3233.143f, 1576.963f, 40.05626f)), owning_building_guid = 57)
      LocalObject(1407, Locker.Constructor(Vector3(3233.143f, 1598.835f, 40.05626f)), owning_building_guid = 57)
      LocalObject(
        1970,
        Terminal.Constructor(Vector3(3233.445f, 1582.129f, 41.39426f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1971,
        Terminal.Constructor(Vector3(3233.445f, 1587.853f, 41.39426f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1972,
        Terminal.Constructor(Vector3(3233.445f, 1593.234f, 41.39426f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        2697,
        SpawnTube.Constructor(Vector3(3222.706f, 1579.742f, 39.54427f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 57
      )
      LocalObject(
        2698,
        SpawnTube.Constructor(Vector3(3222.706f, 1596.152f, 39.54427f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 57
      )
      LocalObject(
        1797,
        FacilityTurret.Constructor(Vector3(3199.32f, 1579.295f, 69.00426f), manned_turret),
        owning_building_guid = 57
      )
      TurretToWeapon(1797, 5076)
      LocalObject(
        1799,
        FacilityTurret.Constructor(Vector3(3234.647f, 1604.707f, 69.00426f), manned_turret),
        owning_building_guid = 57
      )
      TurretToWeapon(1799, 5077)
      LocalObject(
        2483,
        Painbox.Constructor(Vector3(3217.235f, 1585.803f, 41.56136f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        2485,
        Painbox.Constructor(Vector3(3228.889f, 1594.086f, 40.16226f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        2486,
        Painbox.Constructor(Vector3(3228.975f, 1582.223f, 40.16226f), painbox_radius_continuous),
        owning_building_guid = 57
      )
    }

    Building29()

    def Building29(): Unit = { // Name: SE_Akkan_Tower Type: tower_a GUID: 58, MapID: 29
      LocalBuilding(
        "SE_Akkan_Tower",
        58,
        29,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3254f, 3908f, 28.96921f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2782,
        CaptureTerminal.Constructor(Vector3(3270.587f, 3907.897f, 38.96821f), secondary_capture),
        owning_building_guid = 58
      )
      LocalObject(438, Door.Constructor(Vector3(3266f, 3900f, 30.49021f)), owning_building_guid = 58)
      LocalObject(439, Door.Constructor(Vector3(3266f, 3900f, 50.48921f)), owning_building_guid = 58)
      LocalObject(440, Door.Constructor(Vector3(3266f, 3916f, 30.49021f)), owning_building_guid = 58)
      LocalObject(441, Door.Constructor(Vector3(3266f, 3916f, 50.48921f)), owning_building_guid = 58)
      LocalObject(2925, Door.Constructor(Vector3(3265.146f, 3896.794f, 20.30521f)), owning_building_guid = 58)
      LocalObject(2926, Door.Constructor(Vector3(3265.146f, 3913.204f, 20.30521f)), owning_building_guid = 58)
      LocalObject(
        1080,
        IFFLock.Constructor(Vector3(3263.957f, 3916.811f, 30.43021f), Vector3(0, 0, 0)),
        owning_building_guid = 58,
        door_guid = 440
      )
      LocalObject(
        1081,
        IFFLock.Constructor(Vector3(3263.957f, 3916.811f, 50.43021f), Vector3(0, 0, 0)),
        owning_building_guid = 58,
        door_guid = 441
      )
      LocalObject(
        1082,
        IFFLock.Constructor(Vector3(3268.047f, 3899.189f, 30.43021f), Vector3(0, 0, 180)),
        owning_building_guid = 58,
        door_guid = 438
      )
      LocalObject(
        1083,
        IFFLock.Constructor(Vector3(3268.047f, 3899.189f, 50.43021f), Vector3(0, 0, 180)),
        owning_building_guid = 58,
        door_guid = 439
      )
      LocalObject(1416, Locker.Constructor(Vector3(3269.716f, 3892.963f, 18.96321f)), owning_building_guid = 58)
      LocalObject(1417, Locker.Constructor(Vector3(3269.751f, 3914.835f, 18.96321f)), owning_building_guid = 58)
      LocalObject(1418, Locker.Constructor(Vector3(3271.053f, 3892.963f, 18.96321f)), owning_building_guid = 58)
      LocalObject(1419, Locker.Constructor(Vector3(3271.088f, 3914.835f, 18.96321f)), owning_building_guid = 58)
      LocalObject(1420, Locker.Constructor(Vector3(3273.741f, 3892.963f, 18.96321f)), owning_building_guid = 58)
      LocalObject(1421, Locker.Constructor(Vector3(3273.741f, 3914.835f, 18.96321f)), owning_building_guid = 58)
      LocalObject(1422, Locker.Constructor(Vector3(3275.143f, 3892.963f, 18.96321f)), owning_building_guid = 58)
      LocalObject(1423, Locker.Constructor(Vector3(3275.143f, 3914.835f, 18.96321f)), owning_building_guid = 58)
      LocalObject(
        1976,
        Terminal.Constructor(Vector3(3275.445f, 3898.129f, 20.30121f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1977,
        Terminal.Constructor(Vector3(3275.445f, 3903.853f, 20.30121f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1978,
        Terminal.Constructor(Vector3(3275.445f, 3909.234f, 20.30121f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        2701,
        SpawnTube.Constructor(Vector3(3264.706f, 3895.742f, 18.45121f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 58
      )
      LocalObject(
        2702,
        SpawnTube.Constructor(Vector3(3264.706f, 3912.152f, 18.45121f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 58
      )
      LocalObject(
        1803,
        FacilityTurret.Constructor(Vector3(3241.32f, 3895.295f, 47.91121f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1803, 5078)
      LocalObject(
        1805,
        FacilityTurret.Constructor(Vector3(3276.647f, 3920.707f, 47.91121f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1805, 5079)
      LocalObject(
        2489,
        Painbox.Constructor(Vector3(3259.235f, 3901.803f, 20.46831f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        2490,
        Painbox.Constructor(Vector3(3270.889f, 3910.086f, 19.06921f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        2491,
        Painbox.Constructor(Vector3(3270.975f, 3898.223f, 19.06921f), painbox_radius_continuous),
        owning_building_guid = 58
      )
    }

    Building18()

    def Building18(): Unit = { // Name: W_Neti_Tower Type: tower_a GUID: 59, MapID: 18
      LocalBuilding(
        "W_Neti_Tower",
        59,
        18,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4252f, 2648f, 108.6534f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2788,
        CaptureTerminal.Constructor(Vector3(4268.587f, 2647.897f, 118.6524f), secondary_capture),
        owning_building_guid = 59
      )
      LocalObject(509, Door.Constructor(Vector3(4264f, 2640f, 110.1744f)), owning_building_guid = 59)
      LocalObject(510, Door.Constructor(Vector3(4264f, 2640f, 130.1734f)), owning_building_guid = 59)
      LocalObject(511, Door.Constructor(Vector3(4264f, 2656f, 110.1744f)), owning_building_guid = 59)
      LocalObject(512, Door.Constructor(Vector3(4264f, 2656f, 130.1734f)), owning_building_guid = 59)
      LocalObject(2946, Door.Constructor(Vector3(4263.146f, 2636.794f, 99.98938f)), owning_building_guid = 59)
      LocalObject(2947, Door.Constructor(Vector3(4263.146f, 2653.204f, 99.98938f)), owning_building_guid = 59)
      LocalObject(
        1138,
        IFFLock.Constructor(Vector3(4261.957f, 2656.811f, 110.1144f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 511
      )
      LocalObject(
        1139,
        IFFLock.Constructor(Vector3(4261.957f, 2656.811f, 130.1144f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 512
      )
      LocalObject(
        1140,
        IFFLock.Constructor(Vector3(4266.047f, 2639.189f, 110.1144f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 509
      )
      LocalObject(
        1141,
        IFFLock.Constructor(Vector3(4266.047f, 2639.189f, 130.1144f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 510
      )
      LocalObject(1509, Locker.Constructor(Vector3(4267.716f, 2632.963f, 98.64738f)), owning_building_guid = 59)
      LocalObject(1510, Locker.Constructor(Vector3(4267.751f, 2654.835f, 98.64738f)), owning_building_guid = 59)
      LocalObject(1511, Locker.Constructor(Vector3(4269.053f, 2632.963f, 98.64738f)), owning_building_guid = 59)
      LocalObject(1512, Locker.Constructor(Vector3(4269.088f, 2654.835f, 98.64738f)), owning_building_guid = 59)
      LocalObject(1513, Locker.Constructor(Vector3(4271.741f, 2632.963f, 98.64738f)), owning_building_guid = 59)
      LocalObject(1514, Locker.Constructor(Vector3(4271.741f, 2654.835f, 98.64738f)), owning_building_guid = 59)
      LocalObject(1515, Locker.Constructor(Vector3(4273.143f, 2632.963f, 98.64738f)), owning_building_guid = 59)
      LocalObject(1516, Locker.Constructor(Vector3(4273.143f, 2654.835f, 98.64738f)), owning_building_guid = 59)
      LocalObject(
        2011,
        Terminal.Constructor(Vector3(4273.445f, 2638.129f, 99.98538f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        2012,
        Terminal.Constructor(Vector3(4273.445f, 2643.853f, 99.98538f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        2013,
        Terminal.Constructor(Vector3(4273.445f, 2649.234f, 99.98538f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        2722,
        SpawnTube.Constructor(Vector3(4262.706f, 2635.742f, 98.13538f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        2723,
        SpawnTube.Constructor(Vector3(4262.706f, 2652.152f, 98.13538f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        1827,
        FacilityTurret.Constructor(Vector3(4239.32f, 2635.295f, 127.5954f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1827, 5080)
      LocalObject(
        1828,
        FacilityTurret.Constructor(Vector3(4274.647f, 2660.707f, 127.5954f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1828, 5081)
      LocalObject(
        2507,
        Painbox.Constructor(Vector3(4257.235f, 2641.803f, 100.1525f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        2508,
        Painbox.Constructor(Vector3(4268.889f, 2650.086f, 98.75338f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        2509,
        Painbox.Constructor(Vector3(4268.975f, 2638.223f, 98.75338f), painbox_radius_continuous),
        owning_building_guid = 59
      )
    }

    Building34()

    def Building34(): Unit = { // Name: SE_Hanish_Tower Type: tower_a GUID: 60, MapID: 34
      LocalBuilding(
        "SE_Hanish_Tower",
        60,
        34,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4412f, 4858f, 85.94116f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2789,
        CaptureTerminal.Constructor(Vector3(4428.587f, 4857.897f, 95.94016f), secondary_capture),
        owning_building_guid = 60
      )
      LocalObject(525, Door.Constructor(Vector3(4424f, 4850f, 87.46217f)), owning_building_guid = 60)
      LocalObject(526, Door.Constructor(Vector3(4424f, 4850f, 107.4612f)), owning_building_guid = 60)
      LocalObject(527, Door.Constructor(Vector3(4424f, 4866f, 87.46217f)), owning_building_guid = 60)
      LocalObject(528, Door.Constructor(Vector3(4424f, 4866f, 107.4612f)), owning_building_guid = 60)
      LocalObject(2951, Door.Constructor(Vector3(4423.146f, 4846.794f, 77.27716f)), owning_building_guid = 60)
      LocalObject(2952, Door.Constructor(Vector3(4423.146f, 4863.204f, 77.27716f)), owning_building_guid = 60)
      LocalObject(
        1150,
        IFFLock.Constructor(Vector3(4421.957f, 4866.811f, 87.40216f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 527
      )
      LocalObject(
        1151,
        IFFLock.Constructor(Vector3(4421.957f, 4866.811f, 107.4022f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 528
      )
      LocalObject(
        1155,
        IFFLock.Constructor(Vector3(4426.047f, 4849.189f, 87.40216f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 525
      )
      LocalObject(
        1156,
        IFFLock.Constructor(Vector3(4426.047f, 4849.189f, 107.4022f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 526
      )
      LocalObject(1525, Locker.Constructor(Vector3(4427.716f, 4842.963f, 75.93517f)), owning_building_guid = 60)
      LocalObject(1526, Locker.Constructor(Vector3(4427.751f, 4864.835f, 75.93517f)), owning_building_guid = 60)
      LocalObject(1528, Locker.Constructor(Vector3(4429.053f, 4842.963f, 75.93517f)), owning_building_guid = 60)
      LocalObject(1529, Locker.Constructor(Vector3(4429.088f, 4864.835f, 75.93517f)), owning_building_guid = 60)
      LocalObject(1536, Locker.Constructor(Vector3(4431.741f, 4842.963f, 75.93517f)), owning_building_guid = 60)
      LocalObject(1537, Locker.Constructor(Vector3(4431.741f, 4864.835f, 75.93517f)), owning_building_guid = 60)
      LocalObject(1539, Locker.Constructor(Vector3(4433.143f, 4842.963f, 75.93517f)), owning_building_guid = 60)
      LocalObject(1540, Locker.Constructor(Vector3(4433.143f, 4864.835f, 75.93517f)), owning_building_guid = 60)
      LocalObject(
        2018,
        Terminal.Constructor(Vector3(4433.445f, 4848.129f, 77.27316f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        2019,
        Terminal.Constructor(Vector3(4433.445f, 4853.853f, 77.27316f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        2020,
        Terminal.Constructor(Vector3(4433.445f, 4859.234f, 77.27316f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        2727,
        SpawnTube.Constructor(Vector3(4422.706f, 4845.742f, 75.42316f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        2728,
        SpawnTube.Constructor(Vector3(4422.706f, 4862.152f, 75.42316f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        1832,
        FacilityTurret.Constructor(Vector3(4399.32f, 4845.295f, 104.8832f), manned_turret),
        owning_building_guid = 60
      )
      TurretToWeapon(1832, 5082)
      LocalObject(
        1833,
        FacilityTurret.Constructor(Vector3(4434.647f, 4870.707f, 104.8832f), manned_turret),
        owning_building_guid = 60
      )
      TurretToWeapon(1833, 5083)
      LocalObject(
        2510,
        Painbox.Constructor(Vector3(4417.235f, 4851.803f, 77.44026f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        2512,
        Painbox.Constructor(Vector3(4428.889f, 4860.086f, 76.04116f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        2513,
        Painbox.Constructor(Vector3(4428.975f, 4848.223f, 76.04116f), painbox_radius_continuous),
        owning_building_guid = 60
      )
    }

    Building19()

    def Building19(): Unit = { // Name: W_Zaqar_Tower Type: tower_a GUID: 61, MapID: 19
      LocalBuilding(
        "W_Zaqar_Tower",
        61,
        19,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4598f, 1928f, 71.5507f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2791,
        CaptureTerminal.Constructor(Vector3(4614.587f, 1927.897f, 81.5497f), secondary_capture),
        owning_building_guid = 61
      )
      LocalObject(538, Door.Constructor(Vector3(4610f, 1920f, 73.0717f)), owning_building_guid = 61)
      LocalObject(539, Door.Constructor(Vector3(4610f, 1920f, 93.07069f)), owning_building_guid = 61)
      LocalObject(540, Door.Constructor(Vector3(4610f, 1936f, 73.0717f)), owning_building_guid = 61)
      LocalObject(541, Door.Constructor(Vector3(4610f, 1936f, 93.07069f)), owning_building_guid = 61)
      LocalObject(2955, Door.Constructor(Vector3(4609.146f, 1916.794f, 62.8867f)), owning_building_guid = 61)
      LocalObject(2956, Door.Constructor(Vector3(4609.146f, 1933.204f, 62.8867f)), owning_building_guid = 61)
      LocalObject(
        1160,
        IFFLock.Constructor(Vector3(4607.957f, 1936.811f, 73.0117f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 540
      )
      LocalObject(
        1161,
        IFFLock.Constructor(Vector3(4607.957f, 1936.811f, 93.0117f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 541
      )
      LocalObject(
        1162,
        IFFLock.Constructor(Vector3(4612.047f, 1919.189f, 73.0117f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 538
      )
      LocalObject(
        1163,
        IFFLock.Constructor(Vector3(4612.047f, 1919.189f, 93.0117f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 539
      )
      LocalObject(1545, Locker.Constructor(Vector3(4613.716f, 1912.963f, 61.5447f)), owning_building_guid = 61)
      LocalObject(1546, Locker.Constructor(Vector3(4613.751f, 1934.835f, 61.5447f)), owning_building_guid = 61)
      LocalObject(1547, Locker.Constructor(Vector3(4615.053f, 1912.963f, 61.5447f)), owning_building_guid = 61)
      LocalObject(1548, Locker.Constructor(Vector3(4615.088f, 1934.835f, 61.5447f)), owning_building_guid = 61)
      LocalObject(1549, Locker.Constructor(Vector3(4617.741f, 1912.963f, 61.5447f)), owning_building_guid = 61)
      LocalObject(1550, Locker.Constructor(Vector3(4617.741f, 1934.835f, 61.5447f)), owning_building_guid = 61)
      LocalObject(1551, Locker.Constructor(Vector3(4619.143f, 1912.963f, 61.5447f)), owning_building_guid = 61)
      LocalObject(1552, Locker.Constructor(Vector3(4619.143f, 1934.835f, 61.5447f)), owning_building_guid = 61)
      LocalObject(
        2024,
        Terminal.Constructor(Vector3(4619.445f, 1918.129f, 62.8827f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        2025,
        Terminal.Constructor(Vector3(4619.445f, 1923.853f, 62.8827f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        2026,
        Terminal.Constructor(Vector3(4619.445f, 1929.234f, 62.8827f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        2731,
        SpawnTube.Constructor(Vector3(4608.706f, 1915.742f, 61.0327f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        2732,
        SpawnTube.Constructor(Vector3(4608.706f, 1932.152f, 61.0327f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        1837,
        FacilityTurret.Constructor(Vector3(4585.32f, 1915.295f, 90.4927f), manned_turret),
        owning_building_guid = 61
      )
      TurretToWeapon(1837, 5084)
      LocalObject(
        1839,
        FacilityTurret.Constructor(Vector3(4620.647f, 1940.707f, 90.4927f), manned_turret),
        owning_building_guid = 61
      )
      TurretToWeapon(1839, 5085)
      LocalObject(
        2516,
        Painbox.Constructor(Vector3(4603.235f, 1921.803f, 63.0498f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        2517,
        Painbox.Constructor(Vector3(4614.889f, 1930.086f, 61.6507f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        2518,
        Painbox.Constructor(Vector3(4614.975f, 1918.223f, 61.6507f), painbox_radius_continuous),
        owning_building_guid = 61
      )
    }

    Building33()

    def Building33(): Unit = { // Name: E_Girru_Tower Type: tower_a GUID: 62, MapID: 33
      LocalBuilding(
        "E_Girru_Tower",
        62,
        33,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4614f, 5918f, 55.49086f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2792,
        CaptureTerminal.Constructor(Vector3(4630.587f, 5917.897f, 65.48985f), secondary_capture),
        owning_building_guid = 62
      )
      LocalObject(542, Door.Constructor(Vector3(4626f, 5910f, 57.01186f)), owning_building_guid = 62)
      LocalObject(543, Door.Constructor(Vector3(4626f, 5910f, 77.01086f)), owning_building_guid = 62)
      LocalObject(544, Door.Constructor(Vector3(4626f, 5926f, 57.01186f)), owning_building_guid = 62)
      LocalObject(545, Door.Constructor(Vector3(4626f, 5926f, 77.01086f)), owning_building_guid = 62)
      LocalObject(2957, Door.Constructor(Vector3(4625.146f, 5906.794f, 46.82686f)), owning_building_guid = 62)
      LocalObject(2958, Door.Constructor(Vector3(4625.146f, 5923.204f, 46.82686f)), owning_building_guid = 62)
      LocalObject(
        1164,
        IFFLock.Constructor(Vector3(4623.957f, 5926.811f, 56.95185f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 544
      )
      LocalObject(
        1165,
        IFFLock.Constructor(Vector3(4623.957f, 5926.811f, 76.95186f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 545
      )
      LocalObject(
        1166,
        IFFLock.Constructor(Vector3(4628.047f, 5909.189f, 56.95185f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 542
      )
      LocalObject(
        1167,
        IFFLock.Constructor(Vector3(4628.047f, 5909.189f, 76.95186f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 543
      )
      LocalObject(1553, Locker.Constructor(Vector3(4629.716f, 5902.963f, 45.48486f)), owning_building_guid = 62)
      LocalObject(1554, Locker.Constructor(Vector3(4629.751f, 5924.835f, 45.48486f)), owning_building_guid = 62)
      LocalObject(1555, Locker.Constructor(Vector3(4631.053f, 5902.963f, 45.48486f)), owning_building_guid = 62)
      LocalObject(1556, Locker.Constructor(Vector3(4631.088f, 5924.835f, 45.48486f)), owning_building_guid = 62)
      LocalObject(1557, Locker.Constructor(Vector3(4633.741f, 5902.963f, 45.48486f)), owning_building_guid = 62)
      LocalObject(1558, Locker.Constructor(Vector3(4633.741f, 5924.835f, 45.48486f)), owning_building_guid = 62)
      LocalObject(1559, Locker.Constructor(Vector3(4635.143f, 5902.963f, 45.48486f)), owning_building_guid = 62)
      LocalObject(1560, Locker.Constructor(Vector3(4635.143f, 5924.835f, 45.48486f)), owning_building_guid = 62)
      LocalObject(
        2027,
        Terminal.Constructor(Vector3(4635.445f, 5908.129f, 46.82286f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        2028,
        Terminal.Constructor(Vector3(4635.445f, 5913.853f, 46.82286f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        2029,
        Terminal.Constructor(Vector3(4635.445f, 5919.234f, 46.82286f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        2733,
        SpawnTube.Constructor(Vector3(4624.706f, 5905.742f, 44.97285f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        2734,
        SpawnTube.Constructor(Vector3(4624.706f, 5922.152f, 44.97285f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        1838,
        FacilityTurret.Constructor(Vector3(4601.32f, 5905.295f, 74.43285f), manned_turret),
        owning_building_guid = 62
      )
      TurretToWeapon(1838, 5086)
      LocalObject(
        1840,
        FacilityTurret.Constructor(Vector3(4636.647f, 5930.707f, 74.43285f), manned_turret),
        owning_building_guid = 62
      )
      TurretToWeapon(1840, 5087)
      LocalObject(
        2519,
        Painbox.Constructor(Vector3(4619.235f, 5911.803f, 46.98996f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2520,
        Painbox.Constructor(Vector3(4630.889f, 5920.086f, 45.59086f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2521,
        Painbox.Constructor(Vector3(4630.975f, 5908.223f, 45.59086f), painbox_radius_continuous),
        owning_building_guid = 62
      )
    }

    Building26()

    def Building26(): Unit = { // Name: SE_Irkalla_Tower Type: tower_a GUID: 63, MapID: 26
      LocalBuilding(
        "SE_Irkalla_Tower",
        63,
        26,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5828f, 3676f, 68.67637f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2799,
        CaptureTerminal.Constructor(Vector3(5844.587f, 3675.897f, 78.67537f), secondary_capture),
        owning_building_guid = 63
      )
      LocalObject(602, Door.Constructor(Vector3(5840f, 3668f, 70.19737f)), owning_building_guid = 63)
      LocalObject(603, Door.Constructor(Vector3(5840f, 3668f, 90.19637f)), owning_building_guid = 63)
      LocalObject(604, Door.Constructor(Vector3(5840f, 3684f, 70.19737f)), owning_building_guid = 63)
      LocalObject(605, Door.Constructor(Vector3(5840f, 3684f, 90.19637f)), owning_building_guid = 63)
      LocalObject(2977, Door.Constructor(Vector3(5839.146f, 3664.794f, 60.01237f)), owning_building_guid = 63)
      LocalObject(2978, Door.Constructor(Vector3(5839.146f, 3681.204f, 60.01237f)), owning_building_guid = 63)
      LocalObject(
        1217,
        IFFLock.Constructor(Vector3(5837.957f, 3684.811f, 70.13737f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 604
      )
      LocalObject(
        1218,
        IFFLock.Constructor(Vector3(5837.957f, 3684.811f, 90.13737f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 605
      )
      LocalObject(
        1219,
        IFFLock.Constructor(Vector3(5842.047f, 3667.189f, 70.13737f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 602
      )
      LocalObject(
        1220,
        IFFLock.Constructor(Vector3(5842.047f, 3667.189f, 90.13737f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 603
      )
      LocalObject(1642, Locker.Constructor(Vector3(5843.716f, 3660.963f, 58.67037f)), owning_building_guid = 63)
      LocalObject(1643, Locker.Constructor(Vector3(5843.751f, 3682.835f, 58.67037f)), owning_building_guid = 63)
      LocalObject(1644, Locker.Constructor(Vector3(5845.053f, 3660.963f, 58.67037f)), owning_building_guid = 63)
      LocalObject(1645, Locker.Constructor(Vector3(5845.088f, 3682.835f, 58.67037f)), owning_building_guid = 63)
      LocalObject(1646, Locker.Constructor(Vector3(5847.741f, 3660.963f, 58.67037f)), owning_building_guid = 63)
      LocalObject(1647, Locker.Constructor(Vector3(5847.741f, 3682.835f, 58.67037f)), owning_building_guid = 63)
      LocalObject(1648, Locker.Constructor(Vector3(5849.143f, 3660.963f, 58.67037f)), owning_building_guid = 63)
      LocalObject(1649, Locker.Constructor(Vector3(5849.143f, 3682.835f, 58.67037f)), owning_building_guid = 63)
      LocalObject(
        2059,
        Terminal.Constructor(Vector3(5849.445f, 3666.129f, 60.00837f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        2060,
        Terminal.Constructor(Vector3(5849.445f, 3671.853f, 60.00837f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        2061,
        Terminal.Constructor(Vector3(5849.445f, 3677.234f, 60.00837f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        2753,
        SpawnTube.Constructor(Vector3(5838.706f, 3663.742f, 58.15837f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        2754,
        SpawnTube.Constructor(Vector3(5838.706f, 3680.152f, 58.15837f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        1858,
        FacilityTurret.Constructor(Vector3(5815.32f, 3663.295f, 87.61837f), manned_turret),
        owning_building_guid = 63
      )
      TurretToWeapon(1858, 5088)
      LocalObject(
        1859,
        FacilityTurret.Constructor(Vector3(5850.647f, 3688.707f, 87.61837f), manned_turret),
        owning_building_guid = 63
      )
      TurretToWeapon(1859, 5089)
      LocalObject(
        2540,
        Painbox.Constructor(Vector3(5833.235f, 3669.803f, 60.17547f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        2541,
        Painbox.Constructor(Vector3(5844.889f, 3678.086f, 58.77637f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        2542,
        Painbox.Constructor(Vector3(5844.975f, 3666.223f, 58.77637f), painbox_radius_continuous),
        owning_building_guid = 63
      )
    }

    Building23()

    def Building23(): Unit = { // Name: S_Kusag_Tower Type: tower_a GUID: 64, MapID: 23
      LocalBuilding(
        "S_Kusag_Tower",
        64,
        23,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6422f, 3936f, 51.51198f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2801,
        CaptureTerminal.Constructor(Vector3(6438.587f, 3935.897f, 61.51098f), secondary_capture),
        owning_building_guid = 64
      )
      LocalObject(613, Door.Constructor(Vector3(6434f, 3928f, 53.03298f)), owning_building_guid = 64)
      LocalObject(614, Door.Constructor(Vector3(6434f, 3928f, 73.03198f)), owning_building_guid = 64)
      LocalObject(615, Door.Constructor(Vector3(6434f, 3944f, 53.03298f)), owning_building_guid = 64)
      LocalObject(616, Door.Constructor(Vector3(6434f, 3944f, 73.03198f)), owning_building_guid = 64)
      LocalObject(2981, Door.Constructor(Vector3(6433.146f, 3924.794f, 42.84798f)), owning_building_guid = 64)
      LocalObject(2982, Door.Constructor(Vector3(6433.146f, 3941.204f, 42.84798f)), owning_building_guid = 64)
      LocalObject(
        1227,
        IFFLock.Constructor(Vector3(6431.957f, 3944.811f, 52.97298f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 615
      )
      LocalObject(
        1228,
        IFFLock.Constructor(Vector3(6431.957f, 3944.811f, 72.97298f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 616
      )
      LocalObject(
        1229,
        IFFLock.Constructor(Vector3(6436.047f, 3927.189f, 52.97298f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 613
      )
      LocalObject(
        1230,
        IFFLock.Constructor(Vector3(6436.047f, 3927.189f, 72.97298f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 614
      )
      LocalObject(1658, Locker.Constructor(Vector3(6437.716f, 3920.963f, 41.50598f)), owning_building_guid = 64)
      LocalObject(1659, Locker.Constructor(Vector3(6437.751f, 3942.835f, 41.50598f)), owning_building_guid = 64)
      LocalObject(1660, Locker.Constructor(Vector3(6439.053f, 3920.963f, 41.50598f)), owning_building_guid = 64)
      LocalObject(1661, Locker.Constructor(Vector3(6439.088f, 3942.835f, 41.50598f)), owning_building_guid = 64)
      LocalObject(1662, Locker.Constructor(Vector3(6441.741f, 3920.963f, 41.50598f)), owning_building_guid = 64)
      LocalObject(1663, Locker.Constructor(Vector3(6441.741f, 3942.835f, 41.50598f)), owning_building_guid = 64)
      LocalObject(1664, Locker.Constructor(Vector3(6443.143f, 3920.963f, 41.50598f)), owning_building_guid = 64)
      LocalObject(1665, Locker.Constructor(Vector3(6443.143f, 3942.835f, 41.50598f)), owning_building_guid = 64)
      LocalObject(
        2065,
        Terminal.Constructor(Vector3(6443.445f, 3926.129f, 42.84398f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        2066,
        Terminal.Constructor(Vector3(6443.445f, 3931.853f, 42.84398f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        2067,
        Terminal.Constructor(Vector3(6443.445f, 3937.234f, 42.84398f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        2757,
        SpawnTube.Constructor(Vector3(6432.706f, 3923.742f, 40.99398f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 64
      )
      LocalObject(
        2758,
        SpawnTube.Constructor(Vector3(6432.706f, 3940.152f, 40.99398f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 64
      )
      LocalObject(
        1860,
        FacilityTurret.Constructor(Vector3(6409.32f, 3923.295f, 70.45398f), manned_turret),
        owning_building_guid = 64
      )
      TurretToWeapon(1860, 5090)
      LocalObject(
        1861,
        FacilityTurret.Constructor(Vector3(6444.647f, 3948.707f, 70.45398f), manned_turret),
        owning_building_guid = 64
      )
      TurretToWeapon(1861, 5091)
      LocalObject(
        2546,
        Painbox.Constructor(Vector3(6427.235f, 3929.803f, 43.01108f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        2547,
        Painbox.Constructor(Vector3(6438.889f, 3938.086f, 41.61198f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        2548,
        Painbox.Constructor(Vector3(6438.975f, 3926.223f, 41.61198f), painbox_radius_continuous),
        owning_building_guid = 64
      )
    }

    Building58()

    def Building58(): Unit = { // Name: Lahar_Tower Type: tower_a GUID: 65, MapID: 58
      LocalBuilding(
        "Lahar_Tower",
        65,
        58,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6928f, 5100f, 54.45535f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2802,
        CaptureTerminal.Constructor(Vector3(6944.587f, 5099.897f, 64.45435f), secondary_capture),
        owning_building_guid = 65
      )
      LocalObject(630, Door.Constructor(Vector3(6940f, 5092f, 55.97635f)), owning_building_guid = 65)
      LocalObject(631, Door.Constructor(Vector3(6940f, 5092f, 75.97534f)), owning_building_guid = 65)
      LocalObject(632, Door.Constructor(Vector3(6940f, 5108f, 55.97635f)), owning_building_guid = 65)
      LocalObject(633, Door.Constructor(Vector3(6940f, 5108f, 75.97534f)), owning_building_guid = 65)
      LocalObject(2986, Door.Constructor(Vector3(6939.146f, 5088.794f, 45.79134f)), owning_building_guid = 65)
      LocalObject(2987, Door.Constructor(Vector3(6939.146f, 5105.204f, 45.79134f)), owning_building_guid = 65)
      LocalObject(
        1239,
        IFFLock.Constructor(Vector3(6937.957f, 5108.811f, 55.91634f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 632
      )
      LocalObject(
        1240,
        IFFLock.Constructor(Vector3(6937.957f, 5108.811f, 75.91634f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 633
      )
      LocalObject(
        1241,
        IFFLock.Constructor(Vector3(6942.047f, 5091.189f, 55.91634f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 630
      )
      LocalObject(
        1242,
        IFFLock.Constructor(Vector3(6942.047f, 5091.189f, 75.91634f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 631
      )
      LocalObject(1678, Locker.Constructor(Vector3(6943.716f, 5084.963f, 44.44934f)), owning_building_guid = 65)
      LocalObject(1679, Locker.Constructor(Vector3(6943.751f, 5106.835f, 44.44934f)), owning_building_guid = 65)
      LocalObject(1680, Locker.Constructor(Vector3(6945.053f, 5084.963f, 44.44934f)), owning_building_guid = 65)
      LocalObject(1681, Locker.Constructor(Vector3(6945.088f, 5106.835f, 44.44934f)), owning_building_guid = 65)
      LocalObject(1682, Locker.Constructor(Vector3(6947.741f, 5084.963f, 44.44934f)), owning_building_guid = 65)
      LocalObject(1683, Locker.Constructor(Vector3(6947.741f, 5106.835f, 44.44934f)), owning_building_guid = 65)
      LocalObject(1684, Locker.Constructor(Vector3(6949.143f, 5084.963f, 44.44934f)), owning_building_guid = 65)
      LocalObject(1685, Locker.Constructor(Vector3(6949.143f, 5106.835f, 44.44934f)), owning_building_guid = 65)
      LocalObject(
        2072,
        Terminal.Constructor(Vector3(6949.445f, 5090.129f, 45.78735f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        2073,
        Terminal.Constructor(Vector3(6949.445f, 5095.853f, 45.78735f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        2074,
        Terminal.Constructor(Vector3(6949.445f, 5101.234f, 45.78735f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        2762,
        SpawnTube.Constructor(Vector3(6938.706f, 5087.742f, 43.93735f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        2763,
        SpawnTube.Constructor(Vector3(6938.706f, 5104.152f, 43.93735f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        1868,
        FacilityTurret.Constructor(Vector3(6915.32f, 5087.295f, 73.39735f), manned_turret),
        owning_building_guid = 65
      )
      TurretToWeapon(1868, 5092)
      LocalObject(
        1869,
        FacilityTurret.Constructor(Vector3(6950.647f, 5112.707f, 73.39735f), manned_turret),
        owning_building_guid = 65
      )
      TurretToWeapon(1869, 5093)
      LocalObject(
        2549,
        Painbox.Constructor(Vector3(6933.235f, 5093.803f, 45.95444f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        2550,
        Painbox.Constructor(Vector3(6944.889f, 5102.086f, 44.55534f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        2551,
        Painbox.Constructor(Vector3(6944.975f, 5090.223f, 44.55534f), painbox_radius_continuous),
        owning_building_guid = 65
      )
    }

    Building32()

    def Building32(): Unit = { // Name: N_Searhus_Warpgate_Tower Type: tower_b GUID: 66, MapID: 32
      LocalBuilding(
        "N_Searhus_Warpgate_Tower",
        66,
        32,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1210f, 3966f, 43.99354f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2770,
        CaptureTerminal.Constructor(Vector3(1226.587f, 3965.897f, 63.99254f), secondary_capture),
        owning_building_guid = 66
      )
      LocalObject(331, Door.Constructor(Vector3(1222f, 3958f, 45.51354f)), owning_building_guid = 66)
      LocalObject(332, Door.Constructor(Vector3(1222f, 3958f, 55.51354f)), owning_building_guid = 66)
      LocalObject(333, Door.Constructor(Vector3(1222f, 3958f, 75.51354f)), owning_building_guid = 66)
      LocalObject(334, Door.Constructor(Vector3(1222f, 3974f, 45.51354f)), owning_building_guid = 66)
      LocalObject(335, Door.Constructor(Vector3(1222f, 3974f, 55.51354f)), owning_building_guid = 66)
      LocalObject(336, Door.Constructor(Vector3(1222f, 3974f, 75.51354f)), owning_building_guid = 66)
      LocalObject(2892, Door.Constructor(Vector3(1221.147f, 3954.794f, 35.32954f)), owning_building_guid = 66)
      LocalObject(2893, Door.Constructor(Vector3(1221.147f, 3971.204f, 35.32954f)), owning_building_guid = 66)
      LocalObject(
        994,
        IFFLock.Constructor(Vector3(1219.957f, 3974.811f, 45.45454f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 334
      )
      LocalObject(
        995,
        IFFLock.Constructor(Vector3(1219.957f, 3974.811f, 55.45454f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 335
      )
      LocalObject(
        996,
        IFFLock.Constructor(Vector3(1219.957f, 3974.811f, 75.45454f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 336
      )
      LocalObject(
        997,
        IFFLock.Constructor(Vector3(1224.047f, 3957.189f, 45.45454f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 331
      )
      LocalObject(
        998,
        IFFLock.Constructor(Vector3(1224.047f, 3957.189f, 55.45454f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 332
      )
      LocalObject(
        999,
        IFFLock.Constructor(Vector3(1224.047f, 3957.189f, 75.45454f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 333
      )
      LocalObject(1275, Locker.Constructor(Vector3(1225.716f, 3950.963f, 33.98754f)), owning_building_guid = 66)
      LocalObject(1276, Locker.Constructor(Vector3(1225.751f, 3972.835f, 33.98754f)), owning_building_guid = 66)
      LocalObject(1277, Locker.Constructor(Vector3(1227.053f, 3950.963f, 33.98754f)), owning_building_guid = 66)
      LocalObject(1278, Locker.Constructor(Vector3(1227.088f, 3972.835f, 33.98754f)), owning_building_guid = 66)
      LocalObject(1279, Locker.Constructor(Vector3(1229.741f, 3950.963f, 33.98754f)), owning_building_guid = 66)
      LocalObject(1280, Locker.Constructor(Vector3(1229.741f, 3972.835f, 33.98754f)), owning_building_guid = 66)
      LocalObject(1281, Locker.Constructor(Vector3(1231.143f, 3950.963f, 33.98754f)), owning_building_guid = 66)
      LocalObject(1282, Locker.Constructor(Vector3(1231.143f, 3972.835f, 33.98754f)), owning_building_guid = 66)
      LocalObject(
        1924,
        Terminal.Constructor(Vector3(1231.446f, 3956.129f, 35.32554f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1925,
        Terminal.Constructor(Vector3(1231.446f, 3961.853f, 35.32554f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1926,
        Terminal.Constructor(Vector3(1231.446f, 3967.234f, 35.32554f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2668,
        SpawnTube.Constructor(Vector3(1220.706f, 3953.742f, 33.47554f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 66
      )
      LocalObject(
        2669,
        SpawnTube.Constructor(Vector3(1220.706f, 3970.152f, 33.47554f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 66
      )
      LocalObject(
        2453,
        Painbox.Constructor(Vector3(1215.493f, 3958.849f, 35.28294f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2454,
        Painbox.Constructor(Vector3(1227.127f, 3956.078f, 34.09354f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2455,
        Painbox.Constructor(Vector3(1227.259f, 3968.107f, 34.09354f), painbox_radius_continuous),
        owning_building_guid = 66
      )
    }

    Building39()

    def Building39(): Unit = { // Name: SE_Baal_Tower Type: tower_b GUID: 67, MapID: 39
      LocalBuilding(
        "SE_Baal_Tower",
        67,
        39,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1548f, 4666f, 89.42425f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2772,
        CaptureTerminal.Constructor(Vector3(1564.587f, 4665.897f, 109.4232f), secondary_capture),
        owning_building_guid = 67
      )
      LocalObject(341, Door.Constructor(Vector3(1560f, 4658f, 90.94424f)), owning_building_guid = 67)
      LocalObject(342, Door.Constructor(Vector3(1560f, 4658f, 100.9442f)), owning_building_guid = 67)
      LocalObject(343, Door.Constructor(Vector3(1560f, 4658f, 120.9442f)), owning_building_guid = 67)
      LocalObject(344, Door.Constructor(Vector3(1560f, 4674f, 90.94424f)), owning_building_guid = 67)
      LocalObject(345, Door.Constructor(Vector3(1560f, 4674f, 100.9442f)), owning_building_guid = 67)
      LocalObject(346, Door.Constructor(Vector3(1560f, 4674f, 120.9442f)), owning_building_guid = 67)
      LocalObject(2896, Door.Constructor(Vector3(1559.147f, 4654.794f, 80.76025f)), owning_building_guid = 67)
      LocalObject(2897, Door.Constructor(Vector3(1559.147f, 4671.204f, 80.76025f)), owning_building_guid = 67)
      LocalObject(
        1004,
        IFFLock.Constructor(Vector3(1557.957f, 4674.811f, 90.88525f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 344
      )
      LocalObject(
        1005,
        IFFLock.Constructor(Vector3(1557.957f, 4674.811f, 100.8852f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 345
      )
      LocalObject(
        1006,
        IFFLock.Constructor(Vector3(1557.957f, 4674.811f, 120.8852f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 346
      )
      LocalObject(
        1007,
        IFFLock.Constructor(Vector3(1562.047f, 4657.189f, 90.88525f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 341
      )
      LocalObject(
        1008,
        IFFLock.Constructor(Vector3(1562.047f, 4657.189f, 100.8852f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 342
      )
      LocalObject(
        1009,
        IFFLock.Constructor(Vector3(1562.047f, 4657.189f, 120.8852f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 343
      )
      LocalObject(1291, Locker.Constructor(Vector3(1563.716f, 4650.963f, 79.41825f)), owning_building_guid = 67)
      LocalObject(1292, Locker.Constructor(Vector3(1563.751f, 4672.835f, 79.41825f)), owning_building_guid = 67)
      LocalObject(1293, Locker.Constructor(Vector3(1565.053f, 4650.963f, 79.41825f)), owning_building_guid = 67)
      LocalObject(1294, Locker.Constructor(Vector3(1565.088f, 4672.835f, 79.41825f)), owning_building_guid = 67)
      LocalObject(1295, Locker.Constructor(Vector3(1567.741f, 4650.963f, 79.41825f)), owning_building_guid = 67)
      LocalObject(1296, Locker.Constructor(Vector3(1567.741f, 4672.835f, 79.41825f)), owning_building_guid = 67)
      LocalObject(1297, Locker.Constructor(Vector3(1569.143f, 4650.963f, 79.41825f)), owning_building_guid = 67)
      LocalObject(1298, Locker.Constructor(Vector3(1569.143f, 4672.835f, 79.41825f)), owning_building_guid = 67)
      LocalObject(
        1930,
        Terminal.Constructor(Vector3(1569.446f, 4656.129f, 80.75625f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1931,
        Terminal.Constructor(Vector3(1569.446f, 4661.853f, 80.75625f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1932,
        Terminal.Constructor(Vector3(1569.446f, 4667.234f, 80.75625f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        2672,
        SpawnTube.Constructor(Vector3(1558.706f, 4653.742f, 78.90625f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        2673,
        SpawnTube.Constructor(Vector3(1558.706f, 4670.152f, 78.90625f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        2459,
        Painbox.Constructor(Vector3(1553.493f, 4658.849f, 80.71365f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        2461,
        Painbox.Constructor(Vector3(1565.127f, 4656.078f, 79.52425f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        2462,
        Painbox.Constructor(Vector3(1565.259f, 4668.107f, 79.52425f), painbox_radius_continuous),
        owning_building_guid = 67
      )
    }

    Building31()

    def Building31(): Unit = { // Name: E_Searhus_Warpgate_Tower Type: tower_b GUID: 68, MapID: 31
      LocalBuilding(
        "E_Searhus_Warpgate_Tower",
        68,
        31,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1558f, 2872f, 31.19086f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2773,
        CaptureTerminal.Constructor(Vector3(1574.587f, 2871.897f, 51.18986f), secondary_capture),
        owning_building_guid = 68
      )
      LocalObject(347, Door.Constructor(Vector3(1570f, 2864f, 32.71085f)), owning_building_guid = 68)
      LocalObject(348, Door.Constructor(Vector3(1570f, 2864f, 42.71085f)), owning_building_guid = 68)
      LocalObject(349, Door.Constructor(Vector3(1570f, 2864f, 62.71085f)), owning_building_guid = 68)
      LocalObject(350, Door.Constructor(Vector3(1570f, 2880f, 32.71085f)), owning_building_guid = 68)
      LocalObject(351, Door.Constructor(Vector3(1570f, 2880f, 42.71085f)), owning_building_guid = 68)
      LocalObject(352, Door.Constructor(Vector3(1570f, 2880f, 62.71085f)), owning_building_guid = 68)
      LocalObject(2898, Door.Constructor(Vector3(1569.147f, 2860.794f, 22.52686f)), owning_building_guid = 68)
      LocalObject(2899, Door.Constructor(Vector3(1569.147f, 2877.204f, 22.52686f)), owning_building_guid = 68)
      LocalObject(
        1010,
        IFFLock.Constructor(Vector3(1567.957f, 2880.811f, 32.65186f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 350
      )
      LocalObject(
        1011,
        IFFLock.Constructor(Vector3(1567.957f, 2880.811f, 42.65186f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 351
      )
      LocalObject(
        1012,
        IFFLock.Constructor(Vector3(1567.957f, 2880.811f, 62.65186f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 352
      )
      LocalObject(
        1013,
        IFFLock.Constructor(Vector3(1572.047f, 2863.189f, 32.65186f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 347
      )
      LocalObject(
        1014,
        IFFLock.Constructor(Vector3(1572.047f, 2863.189f, 42.65186f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 348
      )
      LocalObject(
        1015,
        IFFLock.Constructor(Vector3(1572.047f, 2863.189f, 62.65186f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 349
      )
      LocalObject(1299, Locker.Constructor(Vector3(1573.716f, 2856.963f, 21.18486f)), owning_building_guid = 68)
      LocalObject(1300, Locker.Constructor(Vector3(1573.751f, 2878.835f, 21.18486f)), owning_building_guid = 68)
      LocalObject(1301, Locker.Constructor(Vector3(1575.053f, 2856.963f, 21.18486f)), owning_building_guid = 68)
      LocalObject(1302, Locker.Constructor(Vector3(1575.088f, 2878.835f, 21.18486f)), owning_building_guid = 68)
      LocalObject(1303, Locker.Constructor(Vector3(1577.741f, 2856.963f, 21.18486f)), owning_building_guid = 68)
      LocalObject(1304, Locker.Constructor(Vector3(1577.741f, 2878.835f, 21.18486f)), owning_building_guid = 68)
      LocalObject(1305, Locker.Constructor(Vector3(1579.143f, 2856.963f, 21.18486f)), owning_building_guid = 68)
      LocalObject(1306, Locker.Constructor(Vector3(1579.143f, 2878.835f, 21.18486f)), owning_building_guid = 68)
      LocalObject(
        1933,
        Terminal.Constructor(Vector3(1579.446f, 2862.129f, 22.52285f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1934,
        Terminal.Constructor(Vector3(1579.446f, 2867.853f, 22.52285f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1935,
        Terminal.Constructor(Vector3(1579.446f, 2873.234f, 22.52285f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        2674,
        SpawnTube.Constructor(Vector3(1568.706f, 2859.742f, 20.67286f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        2675,
        SpawnTube.Constructor(Vector3(1568.706f, 2876.152f, 20.67286f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        2460,
        Painbox.Constructor(Vector3(1563.493f, 2864.849f, 22.48026f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        2463,
        Painbox.Constructor(Vector3(1575.127f, 2862.078f, 21.29086f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        2464,
        Painbox.Constructor(Vector3(1575.259f, 2874.107f, 21.29086f), painbox_radius_continuous),
        owning_building_guid = 68
      )
    }

    Building35()

    def Building35(): Unit = { // Name: SW_Hanish_Tower Type: tower_b GUID: 69, MapID: 35
      LocalBuilding(
        "SW_Hanish_Tower",
        69,
        35,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3086f, 5040f, 81.67988f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2779,
        CaptureTerminal.Constructor(Vector3(3102.587f, 5039.897f, 101.6789f), secondary_capture),
        owning_building_guid = 69
      )
      LocalObject(414, Door.Constructor(Vector3(3098f, 5032f, 83.19987f)), owning_building_guid = 69)
      LocalObject(415, Door.Constructor(Vector3(3098f, 5032f, 93.19987f)), owning_building_guid = 69)
      LocalObject(416, Door.Constructor(Vector3(3098f, 5032f, 113.1999f)), owning_building_guid = 69)
      LocalObject(417, Door.Constructor(Vector3(3098f, 5048f, 83.19987f)), owning_building_guid = 69)
      LocalObject(418, Door.Constructor(Vector3(3098f, 5048f, 93.19987f)), owning_building_guid = 69)
      LocalObject(419, Door.Constructor(Vector3(3098f, 5048f, 113.1999f)), owning_building_guid = 69)
      LocalObject(2917, Door.Constructor(Vector3(3097.147f, 5028.794f, 73.01588f)), owning_building_guid = 69)
      LocalObject(2918, Door.Constructor(Vector3(3097.147f, 5045.204f, 73.01588f)), owning_building_guid = 69)
      LocalObject(
        1062,
        IFFLock.Constructor(Vector3(3095.957f, 5048.811f, 83.14088f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 417
      )
      LocalObject(
        1063,
        IFFLock.Constructor(Vector3(3095.957f, 5048.811f, 93.14088f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 418
      )
      LocalObject(
        1064,
        IFFLock.Constructor(Vector3(3095.957f, 5048.811f, 113.1409f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 419
      )
      LocalObject(
        1066,
        IFFLock.Constructor(Vector3(3100.047f, 5031.189f, 83.14088f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 414
      )
      LocalObject(
        1067,
        IFFLock.Constructor(Vector3(3100.047f, 5031.189f, 93.14088f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 415
      )
      LocalObject(
        1068,
        IFFLock.Constructor(Vector3(3100.047f, 5031.189f, 113.1409f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 416
      )
      LocalObject(1384, Locker.Constructor(Vector3(3101.716f, 5024.963f, 71.67388f)), owning_building_guid = 69)
      LocalObject(1385, Locker.Constructor(Vector3(3101.751f, 5046.835f, 71.67388f)), owning_building_guid = 69)
      LocalObject(1386, Locker.Constructor(Vector3(3103.053f, 5024.963f, 71.67388f)), owning_building_guid = 69)
      LocalObject(1387, Locker.Constructor(Vector3(3103.088f, 5046.835f, 71.67388f)), owning_building_guid = 69)
      LocalObject(1388, Locker.Constructor(Vector3(3105.741f, 5024.963f, 71.67388f)), owning_building_guid = 69)
      LocalObject(1389, Locker.Constructor(Vector3(3105.741f, 5046.835f, 71.67388f)), owning_building_guid = 69)
      LocalObject(1390, Locker.Constructor(Vector3(3107.143f, 5024.963f, 71.67388f)), owning_building_guid = 69)
      LocalObject(1391, Locker.Constructor(Vector3(3107.143f, 5046.835f, 71.67388f)), owning_building_guid = 69)
      LocalObject(
        1966,
        Terminal.Constructor(Vector3(3107.446f, 5030.129f, 73.01188f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1967,
        Terminal.Constructor(Vector3(3107.446f, 5035.853f, 73.01188f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1968,
        Terminal.Constructor(Vector3(3107.446f, 5041.234f, 73.01188f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2693,
        SpawnTube.Constructor(Vector3(3096.706f, 5027.742f, 71.16188f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        2694,
        SpawnTube.Constructor(Vector3(3096.706f, 5044.152f, 71.16188f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        2480,
        Painbox.Constructor(Vector3(3091.493f, 5032.849f, 72.96928f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2481,
        Painbox.Constructor(Vector3(3103.127f, 5030.078f, 71.77988f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2482,
        Painbox.Constructor(Vector3(3103.259f, 5042.107f, 71.77988f), painbox_radius_continuous),
        owning_building_guid = 69
      )
    }

    Building28()

    def Building28(): Unit = { // Name: NE_Enkidu_Tower Type: tower_b GUID: 70, MapID: 28
      LocalBuilding(
        "NE_Enkidu_Tower",
        70,
        28,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3410f, 4294f, 30.17417f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2783,
        CaptureTerminal.Constructor(Vector3(3426.587f, 4293.897f, 50.17317f), secondary_capture),
        owning_building_guid = 70
      )
      LocalObject(458, Door.Constructor(Vector3(3422f, 4286f, 31.69417f)), owning_building_guid = 70)
      LocalObject(459, Door.Constructor(Vector3(3422f, 4286f, 41.69417f)), owning_building_guid = 70)
      LocalObject(460, Door.Constructor(Vector3(3422f, 4286f, 61.69417f)), owning_building_guid = 70)
      LocalObject(461, Door.Constructor(Vector3(3422f, 4302f, 31.69417f)), owning_building_guid = 70)
      LocalObject(462, Door.Constructor(Vector3(3422f, 4302f, 41.69417f)), owning_building_guid = 70)
      LocalObject(463, Door.Constructor(Vector3(3422f, 4302f, 61.69417f)), owning_building_guid = 70)
      LocalObject(2930, Door.Constructor(Vector3(3421.147f, 4282.794f, 21.51017f)), owning_building_guid = 70)
      LocalObject(2931, Door.Constructor(Vector3(3421.147f, 4299.204f, 21.51017f)), owning_building_guid = 70)
      LocalObject(
        1095,
        IFFLock.Constructor(Vector3(3419.957f, 4302.811f, 31.63517f), Vector3(0, 0, 0)),
        owning_building_guid = 70,
        door_guid = 461
      )
      LocalObject(
        1096,
        IFFLock.Constructor(Vector3(3419.957f, 4302.811f, 41.63517f), Vector3(0, 0, 0)),
        owning_building_guid = 70,
        door_guid = 462
      )
      LocalObject(
        1097,
        IFFLock.Constructor(Vector3(3419.957f, 4302.811f, 61.63517f), Vector3(0, 0, 0)),
        owning_building_guid = 70,
        door_guid = 463
      )
      LocalObject(
        1098,
        IFFLock.Constructor(Vector3(3424.047f, 4285.189f, 31.63517f), Vector3(0, 0, 180)),
        owning_building_guid = 70,
        door_guid = 458
      )
      LocalObject(
        1099,
        IFFLock.Constructor(Vector3(3424.047f, 4285.189f, 41.63517f), Vector3(0, 0, 180)),
        owning_building_guid = 70,
        door_guid = 459
      )
      LocalObject(
        1100,
        IFFLock.Constructor(Vector3(3424.047f, 4285.189f, 61.63517f), Vector3(0, 0, 180)),
        owning_building_guid = 70,
        door_guid = 460
      )
      LocalObject(1436, Locker.Constructor(Vector3(3425.716f, 4278.963f, 20.16817f)), owning_building_guid = 70)
      LocalObject(1437, Locker.Constructor(Vector3(3425.751f, 4300.835f, 20.16817f)), owning_building_guid = 70)
      LocalObject(1438, Locker.Constructor(Vector3(3427.053f, 4278.963f, 20.16817f)), owning_building_guid = 70)
      LocalObject(1439, Locker.Constructor(Vector3(3427.088f, 4300.835f, 20.16817f)), owning_building_guid = 70)
      LocalObject(1440, Locker.Constructor(Vector3(3429.741f, 4278.963f, 20.16817f)), owning_building_guid = 70)
      LocalObject(1441, Locker.Constructor(Vector3(3429.741f, 4300.835f, 20.16817f)), owning_building_guid = 70)
      LocalObject(1442, Locker.Constructor(Vector3(3431.143f, 4278.963f, 20.16817f)), owning_building_guid = 70)
      LocalObject(1443, Locker.Constructor(Vector3(3431.143f, 4300.835f, 20.16817f)), owning_building_guid = 70)
      LocalObject(
        1985,
        Terminal.Constructor(Vector3(3431.446f, 4284.129f, 21.50617f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1986,
        Terminal.Constructor(Vector3(3431.446f, 4289.853f, 21.50617f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1987,
        Terminal.Constructor(Vector3(3431.446f, 4295.234f, 21.50617f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        2706,
        SpawnTube.Constructor(Vector3(3420.706f, 4281.742f, 19.65617f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 70
      )
      LocalObject(
        2707,
        SpawnTube.Constructor(Vector3(3420.706f, 4298.152f, 19.65617f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 70
      )
      LocalObject(
        2492,
        Painbox.Constructor(Vector3(3415.493f, 4286.849f, 21.46357f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2493,
        Painbox.Constructor(Vector3(3427.127f, 4284.078f, 20.27417f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2494,
        Painbox.Constructor(Vector3(3427.259f, 4296.107f, 20.27417f), painbox_radius_continuous),
        owning_building_guid = 70
      )
    }

    Building21()

    def Building21(): Unit = { // Name: NE_Neti_Tower Type: tower_b GUID: 71, MapID: 21
      LocalBuilding(
        "NE_Neti_Tower",
        71,
        21,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4162f, 3218f, 95.02856f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2787,
        CaptureTerminal.Constructor(Vector3(4178.587f, 3217.897f, 115.0276f), secondary_capture),
        owning_building_guid = 71
      )
      LocalObject(503, Door.Constructor(Vector3(4174f, 3210f, 96.54855f)), owning_building_guid = 71)
      LocalObject(504, Door.Constructor(Vector3(4174f, 3210f, 106.5486f)), owning_building_guid = 71)
      LocalObject(505, Door.Constructor(Vector3(4174f, 3210f, 126.5486f)), owning_building_guid = 71)
      LocalObject(506, Door.Constructor(Vector3(4174f, 3226f, 96.54855f)), owning_building_guid = 71)
      LocalObject(507, Door.Constructor(Vector3(4174f, 3226f, 106.5486f)), owning_building_guid = 71)
      LocalObject(508, Door.Constructor(Vector3(4174f, 3226f, 126.5486f)), owning_building_guid = 71)
      LocalObject(2944, Door.Constructor(Vector3(4173.147f, 3206.794f, 86.36456f)), owning_building_guid = 71)
      LocalObject(2945, Door.Constructor(Vector3(4173.147f, 3223.204f, 86.36456f)), owning_building_guid = 71)
      LocalObject(
        1132,
        IFFLock.Constructor(Vector3(4171.957f, 3226.811f, 96.48956f), Vector3(0, 0, 0)),
        owning_building_guid = 71,
        door_guid = 506
      )
      LocalObject(
        1133,
        IFFLock.Constructor(Vector3(4171.957f, 3226.811f, 106.4896f), Vector3(0, 0, 0)),
        owning_building_guid = 71,
        door_guid = 507
      )
      LocalObject(
        1134,
        IFFLock.Constructor(Vector3(4171.957f, 3226.811f, 126.4896f), Vector3(0, 0, 0)),
        owning_building_guid = 71,
        door_guid = 508
      )
      LocalObject(
        1135,
        IFFLock.Constructor(Vector3(4176.047f, 3209.189f, 96.48956f), Vector3(0, 0, 180)),
        owning_building_guid = 71,
        door_guid = 503
      )
      LocalObject(
        1136,
        IFFLock.Constructor(Vector3(4176.047f, 3209.189f, 106.4896f), Vector3(0, 0, 180)),
        owning_building_guid = 71,
        door_guid = 504
      )
      LocalObject(
        1137,
        IFFLock.Constructor(Vector3(4176.047f, 3209.189f, 126.4896f), Vector3(0, 0, 180)),
        owning_building_guid = 71,
        door_guid = 505
      )
      LocalObject(1501, Locker.Constructor(Vector3(4177.716f, 3202.963f, 85.02256f)), owning_building_guid = 71)
      LocalObject(1502, Locker.Constructor(Vector3(4177.751f, 3224.835f, 85.02256f)), owning_building_guid = 71)
      LocalObject(1503, Locker.Constructor(Vector3(4179.053f, 3202.963f, 85.02256f)), owning_building_guid = 71)
      LocalObject(1504, Locker.Constructor(Vector3(4179.088f, 3224.835f, 85.02256f)), owning_building_guid = 71)
      LocalObject(1505, Locker.Constructor(Vector3(4181.741f, 3202.963f, 85.02256f)), owning_building_guid = 71)
      LocalObject(1506, Locker.Constructor(Vector3(4181.741f, 3224.835f, 85.02256f)), owning_building_guid = 71)
      LocalObject(1507, Locker.Constructor(Vector3(4183.143f, 3202.963f, 85.02256f)), owning_building_guid = 71)
      LocalObject(1508, Locker.Constructor(Vector3(4183.143f, 3224.835f, 85.02256f)), owning_building_guid = 71)
      LocalObject(
        2008,
        Terminal.Constructor(Vector3(4183.446f, 3208.129f, 86.36056f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        2009,
        Terminal.Constructor(Vector3(4183.446f, 3213.853f, 86.36056f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        2010,
        Terminal.Constructor(Vector3(4183.446f, 3219.234f, 86.36056f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        2720,
        SpawnTube.Constructor(Vector3(4172.706f, 3205.742f, 84.51056f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 71
      )
      LocalObject(
        2721,
        SpawnTube.Constructor(Vector3(4172.706f, 3222.152f, 84.51056f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 71
      )
      LocalObject(
        2504,
        Painbox.Constructor(Vector3(4167.493f, 3210.849f, 86.31796f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2505,
        Painbox.Constructor(Vector3(4179.127f, 3208.078f, 85.12856f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2506,
        Painbox.Constructor(Vector3(4179.259f, 3220.107f, 85.12856f), painbox_radius_continuous),
        owning_building_guid = 71
      )
    }

    Building27()

    def Building27(): Unit = { // Name: S_Irkalla_Tower Type: tower_b GUID: 72, MapID: 27
      LocalBuilding(
        "S_Irkalla_Tower",
        72,
        27,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4414f, 4176f, 63.80005f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2790,
        CaptureTerminal.Constructor(Vector3(4430.587f, 4175.897f, 83.79904f), secondary_capture),
        owning_building_guid = 72
      )
      LocalObject(529, Door.Constructor(Vector3(4426f, 4168f, 65.32005f)), owning_building_guid = 72)
      LocalObject(530, Door.Constructor(Vector3(4426f, 4168f, 75.32005f)), owning_building_guid = 72)
      LocalObject(531, Door.Constructor(Vector3(4426f, 4168f, 95.32005f)), owning_building_guid = 72)
      LocalObject(532, Door.Constructor(Vector3(4426f, 4184f, 65.32005f)), owning_building_guid = 72)
      LocalObject(533, Door.Constructor(Vector3(4426f, 4184f, 75.32005f)), owning_building_guid = 72)
      LocalObject(534, Door.Constructor(Vector3(4426f, 4184f, 95.32005f)), owning_building_guid = 72)
      LocalObject(2953, Door.Constructor(Vector3(4425.147f, 4164.794f, 55.13605f)), owning_building_guid = 72)
      LocalObject(2954, Door.Constructor(Vector3(4425.147f, 4181.204f, 55.13605f)), owning_building_guid = 72)
      LocalObject(
        1152,
        IFFLock.Constructor(Vector3(4423.957f, 4184.811f, 65.26105f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 532
      )
      LocalObject(
        1153,
        IFFLock.Constructor(Vector3(4423.957f, 4184.811f, 75.26105f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 533
      )
      LocalObject(
        1154,
        IFFLock.Constructor(Vector3(4423.957f, 4184.811f, 95.26105f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 534
      )
      LocalObject(
        1157,
        IFFLock.Constructor(Vector3(4428.047f, 4167.189f, 65.26105f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 529
      )
      LocalObject(
        1158,
        IFFLock.Constructor(Vector3(4428.047f, 4167.189f, 75.26105f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 530
      )
      LocalObject(
        1159,
        IFFLock.Constructor(Vector3(4428.047f, 4167.189f, 95.26105f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 531
      )
      LocalObject(1531, Locker.Constructor(Vector3(4429.716f, 4160.963f, 53.79404f)), owning_building_guid = 72)
      LocalObject(1532, Locker.Constructor(Vector3(4429.751f, 4182.835f, 53.79404f)), owning_building_guid = 72)
      LocalObject(1534, Locker.Constructor(Vector3(4431.053f, 4160.963f, 53.79404f)), owning_building_guid = 72)
      LocalObject(1535, Locker.Constructor(Vector3(4431.088f, 4182.835f, 53.79404f)), owning_building_guid = 72)
      LocalObject(1541, Locker.Constructor(Vector3(4433.741f, 4160.963f, 53.79404f)), owning_building_guid = 72)
      LocalObject(1542, Locker.Constructor(Vector3(4433.741f, 4182.835f, 53.79404f)), owning_building_guid = 72)
      LocalObject(1543, Locker.Constructor(Vector3(4435.143f, 4160.963f, 53.79404f)), owning_building_guid = 72)
      LocalObject(1544, Locker.Constructor(Vector3(4435.143f, 4182.835f, 53.79404f)), owning_building_guid = 72)
      LocalObject(
        2021,
        Terminal.Constructor(Vector3(4435.446f, 4166.129f, 55.13205f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2022,
        Terminal.Constructor(Vector3(4435.446f, 4171.853f, 55.13205f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2023,
        Terminal.Constructor(Vector3(4435.446f, 4177.234f, 55.13205f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2729,
        SpawnTube.Constructor(Vector3(4424.706f, 4163.742f, 53.28204f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        2730,
        SpawnTube.Constructor(Vector3(4424.706f, 4180.152f, 53.28204f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        2511,
        Painbox.Constructor(Vector3(4419.493f, 4168.849f, 55.08945f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2514,
        Painbox.Constructor(Vector3(4431.127f, 4166.078f, 53.90005f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2515,
        Painbox.Constructor(Vector3(4431.259f, 4178.107f, 53.90005f), painbox_radius_continuous),
        owning_building_guid = 72
      )
    }

    Building59()

    def Building59(): Unit = { // Name: VSSanc_Warpgate_Tower Type: tower_b GUID: 73, MapID: 59
      LocalBuilding(
        "VSSanc_Warpgate_Tower",
        73,
        59,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4658f, 6628f, 42.60658f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2793,
        CaptureTerminal.Constructor(Vector3(4674.587f, 6627.897f, 62.60558f), secondary_capture),
        owning_building_guid = 73
      )
      LocalObject(546, Door.Constructor(Vector3(4670f, 6620f, 44.12658f)), owning_building_guid = 73)
      LocalObject(547, Door.Constructor(Vector3(4670f, 6620f, 54.12658f)), owning_building_guid = 73)
      LocalObject(548, Door.Constructor(Vector3(4670f, 6620f, 74.12659f)), owning_building_guid = 73)
      LocalObject(549, Door.Constructor(Vector3(4670f, 6636f, 44.12658f)), owning_building_guid = 73)
      LocalObject(550, Door.Constructor(Vector3(4670f, 6636f, 54.12658f)), owning_building_guid = 73)
      LocalObject(551, Door.Constructor(Vector3(4670f, 6636f, 74.12659f)), owning_building_guid = 73)
      LocalObject(2959, Door.Constructor(Vector3(4669.147f, 6616.794f, 33.94258f)), owning_building_guid = 73)
      LocalObject(2960, Door.Constructor(Vector3(4669.147f, 6633.204f, 33.94258f)), owning_building_guid = 73)
      LocalObject(
        1168,
        IFFLock.Constructor(Vector3(4667.957f, 6636.811f, 44.06758f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 549
      )
      LocalObject(
        1169,
        IFFLock.Constructor(Vector3(4667.957f, 6636.811f, 54.06758f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 550
      )
      LocalObject(
        1170,
        IFFLock.Constructor(Vector3(4667.957f, 6636.811f, 74.06758f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 551
      )
      LocalObject(
        1171,
        IFFLock.Constructor(Vector3(4672.047f, 6619.189f, 44.06758f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 546
      )
      LocalObject(
        1172,
        IFFLock.Constructor(Vector3(4672.047f, 6619.189f, 54.06758f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 547
      )
      LocalObject(
        1173,
        IFFLock.Constructor(Vector3(4672.047f, 6619.189f, 74.06758f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 548
      )
      LocalObject(1561, Locker.Constructor(Vector3(4673.716f, 6612.963f, 32.60058f)), owning_building_guid = 73)
      LocalObject(1562, Locker.Constructor(Vector3(4673.751f, 6634.835f, 32.60058f)), owning_building_guid = 73)
      LocalObject(1563, Locker.Constructor(Vector3(4675.053f, 6612.963f, 32.60058f)), owning_building_guid = 73)
      LocalObject(1564, Locker.Constructor(Vector3(4675.088f, 6634.835f, 32.60058f)), owning_building_guid = 73)
      LocalObject(1565, Locker.Constructor(Vector3(4677.741f, 6612.963f, 32.60058f)), owning_building_guid = 73)
      LocalObject(1566, Locker.Constructor(Vector3(4677.741f, 6634.835f, 32.60058f)), owning_building_guid = 73)
      LocalObject(1567, Locker.Constructor(Vector3(4679.143f, 6612.963f, 32.60058f)), owning_building_guid = 73)
      LocalObject(1568, Locker.Constructor(Vector3(4679.143f, 6634.835f, 32.60058f)), owning_building_guid = 73)
      LocalObject(
        2030,
        Terminal.Constructor(Vector3(4679.446f, 6618.129f, 33.93858f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        2031,
        Terminal.Constructor(Vector3(4679.446f, 6623.853f, 33.93858f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        2032,
        Terminal.Constructor(Vector3(4679.446f, 6629.234f, 33.93858f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        2735,
        SpawnTube.Constructor(Vector3(4668.706f, 6615.742f, 32.08858f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        2736,
        SpawnTube.Constructor(Vector3(4668.706f, 6632.152f, 32.08858f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        2522,
        Painbox.Constructor(Vector3(4663.493f, 6620.849f, 33.89598f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2523,
        Painbox.Constructor(Vector3(4675.127f, 6618.078f, 32.70658f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2524,
        Painbox.Constructor(Vector3(4675.259f, 6630.107f, 32.70658f), painbox_radius_continuous),
        owning_building_guid = 73
      )
    }

    Building25()

    def Building25(): Unit = { // Name: N_Ceryshen_Warpgate_Tower Type: tower_b GUID: 74, MapID: 25
      LocalBuilding(
        "N_Ceryshen_Warpgate_Tower",
        74,
        25,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5394f, 4228f, 38.99171f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2797,
        CaptureTerminal.Constructor(Vector3(5410.587f, 4227.897f, 58.99071f), secondary_capture),
        owning_building_guid = 74
      )
      LocalObject(590, Door.Constructor(Vector3(5406f, 4220f, 40.51171f)), owning_building_guid = 74)
      LocalObject(591, Door.Constructor(Vector3(5406f, 4220f, 50.51171f)), owning_building_guid = 74)
      LocalObject(592, Door.Constructor(Vector3(5406f, 4220f, 70.5117f)), owning_building_guid = 74)
      LocalObject(593, Door.Constructor(Vector3(5406f, 4236f, 40.51171f)), owning_building_guid = 74)
      LocalObject(594, Door.Constructor(Vector3(5406f, 4236f, 50.51171f)), owning_building_guid = 74)
      LocalObject(595, Door.Constructor(Vector3(5406f, 4236f, 70.5117f)), owning_building_guid = 74)
      LocalObject(2973, Door.Constructor(Vector3(5405.147f, 4216.794f, 30.32771f)), owning_building_guid = 74)
      LocalObject(2974, Door.Constructor(Vector3(5405.147f, 4233.204f, 30.32771f)), owning_building_guid = 74)
      LocalObject(
        1205,
        IFFLock.Constructor(Vector3(5403.957f, 4236.811f, 40.45271f), Vector3(0, 0, 0)),
        owning_building_guid = 74,
        door_guid = 593
      )
      LocalObject(
        1206,
        IFFLock.Constructor(Vector3(5403.957f, 4236.811f, 50.45271f), Vector3(0, 0, 0)),
        owning_building_guid = 74,
        door_guid = 594
      )
      LocalObject(
        1207,
        IFFLock.Constructor(Vector3(5403.957f, 4236.811f, 70.45271f), Vector3(0, 0, 0)),
        owning_building_guid = 74,
        door_guid = 595
      )
      LocalObject(
        1208,
        IFFLock.Constructor(Vector3(5408.047f, 4219.189f, 40.45271f), Vector3(0, 0, 180)),
        owning_building_guid = 74,
        door_guid = 590
      )
      LocalObject(
        1209,
        IFFLock.Constructor(Vector3(5408.047f, 4219.189f, 50.45271f), Vector3(0, 0, 180)),
        owning_building_guid = 74,
        door_guid = 591
      )
      LocalObject(
        1210,
        IFFLock.Constructor(Vector3(5408.047f, 4219.189f, 70.45271f), Vector3(0, 0, 180)),
        owning_building_guid = 74,
        door_guid = 592
      )
      LocalObject(1626, Locker.Constructor(Vector3(5409.716f, 4212.963f, 28.98571f)), owning_building_guid = 74)
      LocalObject(1627, Locker.Constructor(Vector3(5409.751f, 4234.835f, 28.98571f)), owning_building_guid = 74)
      LocalObject(1628, Locker.Constructor(Vector3(5411.053f, 4212.963f, 28.98571f)), owning_building_guid = 74)
      LocalObject(1629, Locker.Constructor(Vector3(5411.088f, 4234.835f, 28.98571f)), owning_building_guid = 74)
      LocalObject(1630, Locker.Constructor(Vector3(5413.741f, 4212.963f, 28.98571f)), owning_building_guid = 74)
      LocalObject(1631, Locker.Constructor(Vector3(5413.741f, 4234.835f, 28.98571f)), owning_building_guid = 74)
      LocalObject(1632, Locker.Constructor(Vector3(5415.143f, 4212.963f, 28.98571f)), owning_building_guid = 74)
      LocalObject(1633, Locker.Constructor(Vector3(5415.143f, 4234.835f, 28.98571f)), owning_building_guid = 74)
      LocalObject(
        2053,
        Terminal.Constructor(Vector3(5415.446f, 4218.129f, 30.32371f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        2054,
        Terminal.Constructor(Vector3(5415.446f, 4223.853f, 30.32371f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        2055,
        Terminal.Constructor(Vector3(5415.446f, 4229.234f, 30.32371f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        2749,
        SpawnTube.Constructor(Vector3(5404.706f, 4215.742f, 28.47371f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 74
      )
      LocalObject(
        2750,
        SpawnTube.Constructor(Vector3(5404.706f, 4232.152f, 28.47371f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 74
      )
      LocalObject(
        2534,
        Painbox.Constructor(Vector3(5399.493f, 4220.849f, 30.28111f), painbox_radius_continuous),
        owning_building_guid = 74
      )
      LocalObject(
        2535,
        Painbox.Constructor(Vector3(5411.127f, 4218.078f, 29.09171f), painbox_radius_continuous),
        owning_building_guid = 74
      )
      LocalObject(
        2536,
        Painbox.Constructor(Vector3(5411.259f, 4230.107f, 29.09171f), painbox_radius_continuous),
        owning_building_guid = 74
      )
    }

    Building22()

    def Building22(): Unit = { // Name: SE_Ceryshen_Warpgate_Tower Type: tower_b GUID: 75, MapID: 22
      LocalBuilding(
        "SE_Ceryshen_Warpgate_Tower",
        75,
        22,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5814f, 2996f, 41.7468f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2798,
        CaptureTerminal.Constructor(Vector3(5830.587f, 2995.897f, 61.7458f), secondary_capture),
        owning_building_guid = 75
      )
      LocalObject(596, Door.Constructor(Vector3(5826f, 2988f, 43.2668f)), owning_building_guid = 75)
      LocalObject(597, Door.Constructor(Vector3(5826f, 2988f, 53.2668f)), owning_building_guid = 75)
      LocalObject(598, Door.Constructor(Vector3(5826f, 2988f, 73.2668f)), owning_building_guid = 75)
      LocalObject(599, Door.Constructor(Vector3(5826f, 3004f, 43.2668f)), owning_building_guid = 75)
      LocalObject(600, Door.Constructor(Vector3(5826f, 3004f, 53.2668f)), owning_building_guid = 75)
      LocalObject(601, Door.Constructor(Vector3(5826f, 3004f, 73.2668f)), owning_building_guid = 75)
      LocalObject(2975, Door.Constructor(Vector3(5825.147f, 2984.794f, 33.08279f)), owning_building_guid = 75)
      LocalObject(2976, Door.Constructor(Vector3(5825.147f, 3001.204f, 33.08279f)), owning_building_guid = 75)
      LocalObject(
        1211,
        IFFLock.Constructor(Vector3(5823.957f, 3004.811f, 43.20779f), Vector3(0, 0, 0)),
        owning_building_guid = 75,
        door_guid = 599
      )
      LocalObject(
        1212,
        IFFLock.Constructor(Vector3(5823.957f, 3004.811f, 53.20779f), Vector3(0, 0, 0)),
        owning_building_guid = 75,
        door_guid = 600
      )
      LocalObject(
        1213,
        IFFLock.Constructor(Vector3(5823.957f, 3004.811f, 73.20779f), Vector3(0, 0, 0)),
        owning_building_guid = 75,
        door_guid = 601
      )
      LocalObject(
        1214,
        IFFLock.Constructor(Vector3(5828.047f, 2987.189f, 43.20779f), Vector3(0, 0, 180)),
        owning_building_guid = 75,
        door_guid = 596
      )
      LocalObject(
        1215,
        IFFLock.Constructor(Vector3(5828.047f, 2987.189f, 53.20779f), Vector3(0, 0, 180)),
        owning_building_guid = 75,
        door_guid = 597
      )
      LocalObject(
        1216,
        IFFLock.Constructor(Vector3(5828.047f, 2987.189f, 73.20779f), Vector3(0, 0, 180)),
        owning_building_guid = 75,
        door_guid = 598
      )
      LocalObject(1634, Locker.Constructor(Vector3(5829.716f, 2980.963f, 31.7408f)), owning_building_guid = 75)
      LocalObject(1635, Locker.Constructor(Vector3(5829.751f, 3002.835f, 31.7408f)), owning_building_guid = 75)
      LocalObject(1636, Locker.Constructor(Vector3(5831.053f, 2980.963f, 31.7408f)), owning_building_guid = 75)
      LocalObject(1637, Locker.Constructor(Vector3(5831.088f, 3002.835f, 31.7408f)), owning_building_guid = 75)
      LocalObject(1638, Locker.Constructor(Vector3(5833.741f, 2980.963f, 31.7408f)), owning_building_guid = 75)
      LocalObject(1639, Locker.Constructor(Vector3(5833.741f, 3002.835f, 31.7408f)), owning_building_guid = 75)
      LocalObject(1640, Locker.Constructor(Vector3(5835.143f, 2980.963f, 31.7408f)), owning_building_guid = 75)
      LocalObject(1641, Locker.Constructor(Vector3(5835.143f, 3002.835f, 31.7408f)), owning_building_guid = 75)
      LocalObject(
        2056,
        Terminal.Constructor(Vector3(5835.446f, 2986.129f, 33.0788f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2057,
        Terminal.Constructor(Vector3(5835.446f, 2991.853f, 33.0788f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2058,
        Terminal.Constructor(Vector3(5835.446f, 2997.234f, 33.0788f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2751,
        SpawnTube.Constructor(Vector3(5824.706f, 2983.742f, 31.2288f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 75
      )
      LocalObject(
        2752,
        SpawnTube.Constructor(Vector3(5824.706f, 3000.152f, 31.2288f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 75
      )
      LocalObject(
        2537,
        Painbox.Constructor(Vector3(5819.493f, 2988.849f, 33.03619f), painbox_radius_continuous),
        owning_building_guid = 75
      )
      LocalObject(
        2538,
        Painbox.Constructor(Vector3(5831.127f, 2986.078f, 31.8468f), painbox_radius_continuous),
        owning_building_guid = 75
      )
      LocalObject(
        2539,
        Painbox.Constructor(Vector3(5831.259f, 2998.107f, 31.8468f), painbox_radius_continuous),
        owning_building_guid = 75
      )
    }

    Building24()

    def Building24(): Unit = { // Name: NW_Kusag_Tower Type: tower_b GUID: 76, MapID: 24
      LocalBuilding(
        "NW_Kusag_Tower",
        76,
        24,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6348f, 4802f, 38.76578f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2800,
        CaptureTerminal.Constructor(Vector3(6364.587f, 4801.897f, 58.76478f), secondary_capture),
        owning_building_guid = 76
      )
      LocalObject(606, Door.Constructor(Vector3(6360f, 4794f, 40.28578f)), owning_building_guid = 76)
      LocalObject(607, Door.Constructor(Vector3(6360f, 4794f, 50.28578f)), owning_building_guid = 76)
      LocalObject(608, Door.Constructor(Vector3(6360f, 4794f, 70.28578f)), owning_building_guid = 76)
      LocalObject(609, Door.Constructor(Vector3(6360f, 4810f, 40.28578f)), owning_building_guid = 76)
      LocalObject(610, Door.Constructor(Vector3(6360f, 4810f, 50.28578f)), owning_building_guid = 76)
      LocalObject(611, Door.Constructor(Vector3(6360f, 4810f, 70.28578f)), owning_building_guid = 76)
      LocalObject(2979, Door.Constructor(Vector3(6359.147f, 4790.794f, 30.10178f)), owning_building_guid = 76)
      LocalObject(2980, Door.Constructor(Vector3(6359.147f, 4807.204f, 30.10178f)), owning_building_guid = 76)
      LocalObject(
        1221,
        IFFLock.Constructor(Vector3(6357.957f, 4810.811f, 40.22678f), Vector3(0, 0, 0)),
        owning_building_guid = 76,
        door_guid = 609
      )
      LocalObject(
        1222,
        IFFLock.Constructor(Vector3(6357.957f, 4810.811f, 50.22678f), Vector3(0, 0, 0)),
        owning_building_guid = 76,
        door_guid = 610
      )
      LocalObject(
        1223,
        IFFLock.Constructor(Vector3(6357.957f, 4810.811f, 70.22678f), Vector3(0, 0, 0)),
        owning_building_guid = 76,
        door_guid = 611
      )
      LocalObject(
        1224,
        IFFLock.Constructor(Vector3(6362.047f, 4793.189f, 40.22678f), Vector3(0, 0, 180)),
        owning_building_guid = 76,
        door_guid = 606
      )
      LocalObject(
        1225,
        IFFLock.Constructor(Vector3(6362.047f, 4793.189f, 50.22678f), Vector3(0, 0, 180)),
        owning_building_guid = 76,
        door_guid = 607
      )
      LocalObject(
        1226,
        IFFLock.Constructor(Vector3(6362.047f, 4793.189f, 70.22678f), Vector3(0, 0, 180)),
        owning_building_guid = 76,
        door_guid = 608
      )
      LocalObject(1650, Locker.Constructor(Vector3(6363.716f, 4786.963f, 28.75978f)), owning_building_guid = 76)
      LocalObject(1651, Locker.Constructor(Vector3(6363.751f, 4808.835f, 28.75978f)), owning_building_guid = 76)
      LocalObject(1652, Locker.Constructor(Vector3(6365.053f, 4786.963f, 28.75978f)), owning_building_guid = 76)
      LocalObject(1653, Locker.Constructor(Vector3(6365.088f, 4808.835f, 28.75978f)), owning_building_guid = 76)
      LocalObject(1654, Locker.Constructor(Vector3(6367.741f, 4786.963f, 28.75978f)), owning_building_guid = 76)
      LocalObject(1655, Locker.Constructor(Vector3(6367.741f, 4808.835f, 28.75978f)), owning_building_guid = 76)
      LocalObject(1656, Locker.Constructor(Vector3(6369.143f, 4786.963f, 28.75978f)), owning_building_guid = 76)
      LocalObject(1657, Locker.Constructor(Vector3(6369.143f, 4808.835f, 28.75978f)), owning_building_guid = 76)
      LocalObject(
        2062,
        Terminal.Constructor(Vector3(6369.446f, 4792.129f, 30.09778f), order_terminal),
        owning_building_guid = 76
      )
      LocalObject(
        2063,
        Terminal.Constructor(Vector3(6369.446f, 4797.853f, 30.09778f), order_terminal),
        owning_building_guid = 76
      )
      LocalObject(
        2064,
        Terminal.Constructor(Vector3(6369.446f, 4803.234f, 30.09778f), order_terminal),
        owning_building_guid = 76
      )
      LocalObject(
        2755,
        SpawnTube.Constructor(Vector3(6358.706f, 4789.742f, 28.24778f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 76
      )
      LocalObject(
        2756,
        SpawnTube.Constructor(Vector3(6358.706f, 4806.152f, 28.24778f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 76
      )
      LocalObject(
        2543,
        Painbox.Constructor(Vector3(6353.493f, 4794.849f, 30.05518f), painbox_radius_continuous),
        owning_building_guid = 76
      )
      LocalObject(
        2544,
        Painbox.Constructor(Vector3(6365.127f, 4792.078f, 28.86578f), painbox_radius_continuous),
        owning_building_guid = 76
      )
      LocalObject(
        2545,
        Painbox.Constructor(Vector3(6365.259f, 4804.107f, 28.86578f), painbox_radius_continuous),
        owning_building_guid = 76
      )
    }

    Building63()

    def Building63(): Unit = { // Name: NW_Dagon_Tower Type: tower_c GUID: 77, MapID: 63
      LocalBuilding(
        "NW_Dagon_Tower",
        77,
        63,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1302f, 6050f, 44.72154f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2771,
        CaptureTerminal.Constructor(Vector3(1318.587f, 6049.897f, 54.72054f), secondary_capture),
        owning_building_guid = 77
      )
      LocalObject(337, Door.Constructor(Vector3(1314f, 6042f, 46.24254f)), owning_building_guid = 77)
      LocalObject(338, Door.Constructor(Vector3(1314f, 6042f, 66.24154f)), owning_building_guid = 77)
      LocalObject(339, Door.Constructor(Vector3(1314f, 6058f, 46.24254f)), owning_building_guid = 77)
      LocalObject(340, Door.Constructor(Vector3(1314f, 6058f, 66.24154f)), owning_building_guid = 77)
      LocalObject(2894, Door.Constructor(Vector3(1313.146f, 6038.794f, 36.05754f)), owning_building_guid = 77)
      LocalObject(2895, Door.Constructor(Vector3(1313.146f, 6055.204f, 36.05754f)), owning_building_guid = 77)
      LocalObject(
        1000,
        IFFLock.Constructor(Vector3(1311.957f, 6058.811f, 46.18254f), Vector3(0, 0, 0)),
        owning_building_guid = 77,
        door_guid = 339
      )
      LocalObject(
        1001,
        IFFLock.Constructor(Vector3(1311.957f, 6058.811f, 66.18254f), Vector3(0, 0, 0)),
        owning_building_guid = 77,
        door_guid = 340
      )
      LocalObject(
        1002,
        IFFLock.Constructor(Vector3(1316.047f, 6041.189f, 46.18254f), Vector3(0, 0, 180)),
        owning_building_guid = 77,
        door_guid = 337
      )
      LocalObject(
        1003,
        IFFLock.Constructor(Vector3(1316.047f, 6041.189f, 66.18254f), Vector3(0, 0, 180)),
        owning_building_guid = 77,
        door_guid = 338
      )
      LocalObject(1283, Locker.Constructor(Vector3(1317.716f, 6034.963f, 34.71554f)), owning_building_guid = 77)
      LocalObject(1284, Locker.Constructor(Vector3(1317.751f, 6056.835f, 34.71554f)), owning_building_guid = 77)
      LocalObject(1285, Locker.Constructor(Vector3(1319.053f, 6034.963f, 34.71554f)), owning_building_guid = 77)
      LocalObject(1286, Locker.Constructor(Vector3(1319.088f, 6056.835f, 34.71554f)), owning_building_guid = 77)
      LocalObject(1287, Locker.Constructor(Vector3(1321.741f, 6034.963f, 34.71554f)), owning_building_guid = 77)
      LocalObject(1288, Locker.Constructor(Vector3(1321.741f, 6056.835f, 34.71554f)), owning_building_guid = 77)
      LocalObject(1289, Locker.Constructor(Vector3(1323.143f, 6034.963f, 34.71554f)), owning_building_guid = 77)
      LocalObject(1290, Locker.Constructor(Vector3(1323.143f, 6056.835f, 34.71554f)), owning_building_guid = 77)
      LocalObject(
        1927,
        Terminal.Constructor(Vector3(1323.445f, 6040.129f, 36.05354f), order_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        1928,
        Terminal.Constructor(Vector3(1323.445f, 6045.853f, 36.05354f), order_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        1929,
        Terminal.Constructor(Vector3(1323.445f, 6051.234f, 36.05354f), order_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        2670,
        SpawnTube.Constructor(Vector3(1312.706f, 6037.742f, 34.20354f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 77
      )
      LocalObject(
        2671,
        SpawnTube.Constructor(Vector3(1312.706f, 6054.152f, 34.20354f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 77
      )
      LocalObject(
        2306,
        ProximityTerminal.Constructor(Vector3(1300.907f, 6044.725f, 72.29153f), pad_landing_tower_frame),
        owning_building_guid = 77
      )
      LocalObject(
        2307,
        Terminal.Constructor(Vector3(1300.907f, 6044.725f, 72.29153f), air_rearm_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        2309,
        ProximityTerminal.Constructor(Vector3(1300.907f, 6055.17f, 72.29153f), pad_landing_tower_frame),
        owning_building_guid = 77
      )
      LocalObject(
        2310,
        Terminal.Constructor(Vector3(1300.907f, 6055.17f, 72.29153f), air_rearm_terminal),
        owning_building_guid = 77
      )
      LocalObject(
        1767,
        FacilityTurret.Constructor(Vector3(1287.07f, 6035.045f, 63.66354f), manned_turret),
        owning_building_guid = 77
      )
      TurretToWeapon(1767, 5094)
      LocalObject(
        1768,
        FacilityTurret.Constructor(Vector3(1325.497f, 6064.957f, 63.66354f), manned_turret),
        owning_building_guid = 77
      )
      TurretToWeapon(1768, 5095)
      LocalObject(
        2456,
        Painbox.Constructor(Vector3(1306.454f, 6042.849f, 36.74104f), painbox_radius_continuous),
        owning_building_guid = 77
      )
      LocalObject(
        2457,
        Painbox.Constructor(Vector3(1318.923f, 6039.54f, 34.82154f), painbox_radius_continuous),
        owning_building_guid = 77
      )
      LocalObject(
        2458,
        Painbox.Constructor(Vector3(1319.113f, 6052.022f, 34.82154f), painbox_radius_continuous),
        owning_building_guid = 77
      )
    }

    Building40()

    def Building40(): Unit = { // Name: S_Dagon_Tower Type: tower_c GUID: 78, MapID: 40
      LocalBuilding(
        "S_Dagon_Tower",
        78,
        40,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2228f, 5158f, 46.85532f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2775,
        CaptureTerminal.Constructor(Vector3(2244.587f, 5157.897f, 56.85432f), secondary_capture),
        owning_building_guid = 78
      )
      LocalObject(370, Door.Constructor(Vector3(2240f, 5150f, 48.37632f)), owning_building_guid = 78)
      LocalObject(371, Door.Constructor(Vector3(2240f, 5150f, 68.37532f)), owning_building_guid = 78)
      LocalObject(372, Door.Constructor(Vector3(2240f, 5166f, 48.37632f)), owning_building_guid = 78)
      LocalObject(373, Door.Constructor(Vector3(2240f, 5166f, 68.37532f)), owning_building_guid = 78)
      LocalObject(2905, Door.Constructor(Vector3(2239.146f, 5146.794f, 38.19131f)), owning_building_guid = 78)
      LocalObject(2906, Door.Constructor(Vector3(2239.146f, 5163.204f, 38.19131f)), owning_building_guid = 78)
      LocalObject(
        1028,
        IFFLock.Constructor(Vector3(2237.957f, 5166.811f, 48.31631f), Vector3(0, 0, 0)),
        owning_building_guid = 78,
        door_guid = 372
      )
      LocalObject(
        1029,
        IFFLock.Constructor(Vector3(2237.957f, 5166.811f, 68.31631f), Vector3(0, 0, 0)),
        owning_building_guid = 78,
        door_guid = 373
      )
      LocalObject(
        1030,
        IFFLock.Constructor(Vector3(2242.047f, 5149.189f, 48.31631f), Vector3(0, 0, 180)),
        owning_building_guid = 78,
        door_guid = 370
      )
      LocalObject(
        1031,
        IFFLock.Constructor(Vector3(2242.047f, 5149.189f, 68.31631f), Vector3(0, 0, 180)),
        owning_building_guid = 78,
        door_guid = 371
      )
      LocalObject(1336, Locker.Constructor(Vector3(2243.716f, 5142.963f, 36.84932f)), owning_building_guid = 78)
      LocalObject(1337, Locker.Constructor(Vector3(2243.751f, 5164.835f, 36.84932f)), owning_building_guid = 78)
      LocalObject(1338, Locker.Constructor(Vector3(2245.053f, 5142.963f, 36.84932f)), owning_building_guid = 78)
      LocalObject(1339, Locker.Constructor(Vector3(2245.088f, 5164.835f, 36.84932f)), owning_building_guid = 78)
      LocalObject(1340, Locker.Constructor(Vector3(2247.741f, 5142.963f, 36.84932f)), owning_building_guid = 78)
      LocalObject(1341, Locker.Constructor(Vector3(2247.741f, 5164.835f, 36.84932f)), owning_building_guid = 78)
      LocalObject(1342, Locker.Constructor(Vector3(2249.143f, 5142.963f, 36.84932f)), owning_building_guid = 78)
      LocalObject(1343, Locker.Constructor(Vector3(2249.143f, 5164.835f, 36.84932f)), owning_building_guid = 78)
      LocalObject(
        1943,
        Terminal.Constructor(Vector3(2249.445f, 5148.129f, 38.18732f), order_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        1944,
        Terminal.Constructor(Vector3(2249.445f, 5153.853f, 38.18732f), order_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        1945,
        Terminal.Constructor(Vector3(2249.445f, 5159.234f, 38.18732f), order_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        2681,
        SpawnTube.Constructor(Vector3(2238.706f, 5145.742f, 36.33732f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 78
      )
      LocalObject(
        2682,
        SpawnTube.Constructor(Vector3(2238.706f, 5162.152f, 36.33732f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 78
      )
      LocalObject(
        2312,
        ProximityTerminal.Constructor(Vector3(2226.907f, 5152.725f, 74.42532f), pad_landing_tower_frame),
        owning_building_guid = 78
      )
      LocalObject(
        2313,
        Terminal.Constructor(Vector3(2226.907f, 5152.725f, 74.42532f), air_rearm_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        2315,
        ProximityTerminal.Constructor(Vector3(2226.907f, 5163.17f, 74.42532f), pad_landing_tower_frame),
        owning_building_guid = 78
      )
      LocalObject(
        2316,
        Terminal.Constructor(Vector3(2226.907f, 5163.17f, 74.42532f), air_rearm_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        1776,
        FacilityTurret.Constructor(Vector3(2213.07f, 5143.045f, 65.79732f), manned_turret),
        owning_building_guid = 78
      )
      TurretToWeapon(1776, 5096)
      LocalObject(
        1777,
        FacilityTurret.Constructor(Vector3(2251.497f, 5172.957f, 65.79732f), manned_turret),
        owning_building_guid = 78
      )
      TurretToWeapon(1777, 5097)
      LocalObject(
        2468,
        Painbox.Constructor(Vector3(2232.454f, 5150.849f, 38.87482f), painbox_radius_continuous),
        owning_building_guid = 78
      )
      LocalObject(
        2469,
        Painbox.Constructor(Vector3(2244.923f, 5147.54f, 36.95531f), painbox_radius_continuous),
        owning_building_guid = 78
      )
      LocalObject(
        2470,
        Painbox.Constructor(Vector3(2245.113f, 5160.022f, 36.95531f), painbox_radius_continuous),
        owning_building_guid = 78
      )
    }

    Building60()

    def Building60(): Unit = { // Name: Akkan_Tower Type: tower_c GUID: 79, MapID: 60
      LocalBuilding(
        "Akkan_Tower",
        79,
        60,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2474f, 4454f, 51.89481f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2776,
        CaptureTerminal.Constructor(Vector3(2490.587f, 4453.897f, 61.89381f), secondary_capture),
        owning_building_guid = 79
      )
      LocalObject(374, Door.Constructor(Vector3(2486f, 4446f, 53.41581f)), owning_building_guid = 79)
      LocalObject(375, Door.Constructor(Vector3(2486f, 4446f, 73.41481f)), owning_building_guid = 79)
      LocalObject(376, Door.Constructor(Vector3(2486f, 4462f, 53.41581f)), owning_building_guid = 79)
      LocalObject(377, Door.Constructor(Vector3(2486f, 4462f, 73.41481f)), owning_building_guid = 79)
      LocalObject(2907, Door.Constructor(Vector3(2485.146f, 4442.794f, 43.2308f)), owning_building_guid = 79)
      LocalObject(2908, Door.Constructor(Vector3(2485.146f, 4459.204f, 43.2308f)), owning_building_guid = 79)
      LocalObject(
        1032,
        IFFLock.Constructor(Vector3(2483.957f, 4462.811f, 53.3558f), Vector3(0, 0, 0)),
        owning_building_guid = 79,
        door_guid = 376
      )
      LocalObject(
        1033,
        IFFLock.Constructor(Vector3(2483.957f, 4462.811f, 73.3558f), Vector3(0, 0, 0)),
        owning_building_guid = 79,
        door_guid = 377
      )
      LocalObject(
        1034,
        IFFLock.Constructor(Vector3(2488.047f, 4445.189f, 53.3558f), Vector3(0, 0, 180)),
        owning_building_guid = 79,
        door_guid = 374
      )
      LocalObject(
        1035,
        IFFLock.Constructor(Vector3(2488.047f, 4445.189f, 73.3558f), Vector3(0, 0, 180)),
        owning_building_guid = 79,
        door_guid = 375
      )
      LocalObject(1344, Locker.Constructor(Vector3(2489.716f, 4438.963f, 41.88881f)), owning_building_guid = 79)
      LocalObject(1345, Locker.Constructor(Vector3(2489.751f, 4460.835f, 41.88881f)), owning_building_guid = 79)
      LocalObject(1346, Locker.Constructor(Vector3(2491.053f, 4438.963f, 41.88881f)), owning_building_guid = 79)
      LocalObject(1347, Locker.Constructor(Vector3(2491.088f, 4460.835f, 41.88881f)), owning_building_guid = 79)
      LocalObject(1348, Locker.Constructor(Vector3(2493.741f, 4438.963f, 41.88881f)), owning_building_guid = 79)
      LocalObject(1349, Locker.Constructor(Vector3(2493.741f, 4460.835f, 41.88881f)), owning_building_guid = 79)
      LocalObject(1350, Locker.Constructor(Vector3(2495.143f, 4438.963f, 41.88881f)), owning_building_guid = 79)
      LocalObject(1351, Locker.Constructor(Vector3(2495.143f, 4460.835f, 41.88881f)), owning_building_guid = 79)
      LocalObject(
        1946,
        Terminal.Constructor(Vector3(2495.445f, 4444.129f, 43.22681f), order_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        1947,
        Terminal.Constructor(Vector3(2495.445f, 4449.853f, 43.22681f), order_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        1948,
        Terminal.Constructor(Vector3(2495.445f, 4455.234f, 43.22681f), order_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        2683,
        SpawnTube.Constructor(Vector3(2484.706f, 4441.742f, 41.37681f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 79
      )
      LocalObject(
        2684,
        SpawnTube.Constructor(Vector3(2484.706f, 4458.152f, 41.37681f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 79
      )
      LocalObject(
        2318,
        ProximityTerminal.Constructor(Vector3(2472.907f, 4448.725f, 79.46481f), pad_landing_tower_frame),
        owning_building_guid = 79
      )
      LocalObject(
        2319,
        Terminal.Constructor(Vector3(2472.907f, 4448.725f, 79.46481f), air_rearm_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        2321,
        ProximityTerminal.Constructor(Vector3(2472.907f, 4459.17f, 79.46481f), pad_landing_tower_frame),
        owning_building_guid = 79
      )
      LocalObject(
        2322,
        Terminal.Constructor(Vector3(2472.907f, 4459.17f, 79.46481f), air_rearm_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        1778,
        FacilityTurret.Constructor(Vector3(2459.07f, 4439.045f, 70.83681f), manned_turret),
        owning_building_guid = 79
      )
      TurretToWeapon(1778, 5098)
      LocalObject(
        1779,
        FacilityTurret.Constructor(Vector3(2497.497f, 4468.957f, 70.83681f), manned_turret),
        owning_building_guid = 79
      )
      TurretToWeapon(1779, 5099)
      LocalObject(
        2471,
        Painbox.Constructor(Vector3(2478.454f, 4446.849f, 43.91431f), painbox_radius_continuous),
        owning_building_guid = 79
      )
      LocalObject(
        2472,
        Painbox.Constructor(Vector3(2490.923f, 4443.54f, 41.9948f), painbox_radius_continuous),
        owning_building_guid = 79
      )
      LocalObject(
        2473,
        Painbox.Constructor(Vector3(2491.113f, 4456.022f, 41.9948f), painbox_radius_continuous),
        owning_building_guid = 79
      )
    }

    Building30()

    def Building30(): Unit = { // Name: SW_Enkidu_Tower Type: tower_c GUID: 80, MapID: 30
      LocalBuilding(
        "SW_Enkidu_Tower",
        80,
        30,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2726f, 3276f, 56.60795f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2777,
        CaptureTerminal.Constructor(Vector3(2742.587f, 3275.897f, 66.60695f), secondary_capture),
        owning_building_guid = 80
      )
      LocalObject(389, Door.Constructor(Vector3(2738f, 3268f, 58.12895f)), owning_building_guid = 80)
      LocalObject(390, Door.Constructor(Vector3(2738f, 3268f, 78.12795f)), owning_building_guid = 80)
      LocalObject(391, Door.Constructor(Vector3(2738f, 3284f, 58.12895f)), owning_building_guid = 80)
      LocalObject(392, Door.Constructor(Vector3(2738f, 3284f, 78.12795f)), owning_building_guid = 80)
      LocalObject(2912, Door.Constructor(Vector3(2737.146f, 3264.794f, 47.94395f)), owning_building_guid = 80)
      LocalObject(2913, Door.Constructor(Vector3(2737.146f, 3281.204f, 47.94395f)), owning_building_guid = 80)
      LocalObject(
        1041,
        IFFLock.Constructor(Vector3(2735.957f, 3284.811f, 58.06895f), Vector3(0, 0, 0)),
        owning_building_guid = 80,
        door_guid = 391
      )
      LocalObject(
        1042,
        IFFLock.Constructor(Vector3(2735.957f, 3284.811f, 78.06895f), Vector3(0, 0, 0)),
        owning_building_guid = 80,
        door_guid = 392
      )
      LocalObject(
        1045,
        IFFLock.Constructor(Vector3(2740.047f, 3267.189f, 58.06895f), Vector3(0, 0, 180)),
        owning_building_guid = 80,
        door_guid = 389
      )
      LocalObject(
        1046,
        IFFLock.Constructor(Vector3(2740.047f, 3267.189f, 78.06895f), Vector3(0, 0, 180)),
        owning_building_guid = 80,
        door_guid = 390
      )
      LocalObject(1353, Locker.Constructor(Vector3(2741.716f, 3260.963f, 46.60195f)), owning_building_guid = 80)
      LocalObject(1354, Locker.Constructor(Vector3(2741.751f, 3282.835f, 46.60195f)), owning_building_guid = 80)
      LocalObject(1356, Locker.Constructor(Vector3(2743.053f, 3260.963f, 46.60195f)), owning_building_guid = 80)
      LocalObject(1357, Locker.Constructor(Vector3(2743.088f, 3282.835f, 46.60195f)), owning_building_guid = 80)
      LocalObject(1360, Locker.Constructor(Vector3(2745.741f, 3260.963f, 46.60195f)), owning_building_guid = 80)
      LocalObject(1361, Locker.Constructor(Vector3(2745.741f, 3282.835f, 46.60195f)), owning_building_guid = 80)
      LocalObject(1362, Locker.Constructor(Vector3(2747.143f, 3260.963f, 46.60195f)), owning_building_guid = 80)
      LocalObject(1363, Locker.Constructor(Vector3(2747.143f, 3282.835f, 46.60195f)), owning_building_guid = 80)
      LocalObject(
        1957,
        Terminal.Constructor(Vector3(2747.445f, 3266.129f, 47.93995f), order_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        1958,
        Terminal.Constructor(Vector3(2747.445f, 3271.853f, 47.93995f), order_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        1959,
        Terminal.Constructor(Vector3(2747.445f, 3277.234f, 47.93995f), order_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        2688,
        SpawnTube.Constructor(Vector3(2736.706f, 3263.742f, 46.08995f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 80
      )
      LocalObject(
        2689,
        SpawnTube.Constructor(Vector3(2736.706f, 3280.152f, 46.08995f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 80
      )
      LocalObject(
        2324,
        ProximityTerminal.Constructor(Vector3(2724.907f, 3270.725f, 84.17795f), pad_landing_tower_frame),
        owning_building_guid = 80
      )
      LocalObject(
        2325,
        Terminal.Constructor(Vector3(2724.907f, 3270.725f, 84.17795f), air_rearm_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        2327,
        ProximityTerminal.Constructor(Vector3(2724.907f, 3281.17f, 84.17795f), pad_landing_tower_frame),
        owning_building_guid = 80
      )
      LocalObject(
        2328,
        Terminal.Constructor(Vector3(2724.907f, 3281.17f, 84.17795f), air_rearm_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        1783,
        FacilityTurret.Constructor(Vector3(2711.07f, 3261.045f, 75.54995f), manned_turret),
        owning_building_guid = 80
      )
      TurretToWeapon(1783, 5100)
      LocalObject(
        1786,
        FacilityTurret.Constructor(Vector3(2749.497f, 3290.957f, 75.54995f), manned_turret),
        owning_building_guid = 80
      )
      TurretToWeapon(1786, 5101)
      LocalObject(
        2474,
        Painbox.Constructor(Vector3(2730.454f, 3268.849f, 48.62745f), painbox_radius_continuous),
        owning_building_guid = 80
      )
      LocalObject(
        2475,
        Painbox.Constructor(Vector3(2742.923f, 3265.54f, 46.70795f), painbox_radius_continuous),
        owning_building_guid = 80
      )
      LocalObject(
        2476,
        Painbox.Constructor(Vector3(2743.113f, 3278.022f, 46.70795f), painbox_radius_continuous),
        owning_building_guid = 80
      )
    }

    Building62()

    def Building62(): Unit = { // Name: Marduk_Tower Type: tower_c GUID: 81, MapID: 62
      LocalBuilding(
        "Marduk_Tower",
        81,
        62,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3218f, 2478f, 75.78687f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2781,
        CaptureTerminal.Constructor(Vector3(3234.587f, 2477.897f, 85.78587f), secondary_capture),
        owning_building_guid = 81
      )
      LocalObject(433, Door.Constructor(Vector3(3230f, 2470f, 77.30788f)), owning_building_guid = 81)
      LocalObject(434, Door.Constructor(Vector3(3230f, 2470f, 97.30687f)), owning_building_guid = 81)
      LocalObject(435, Door.Constructor(Vector3(3230f, 2486f, 77.30788f)), owning_building_guid = 81)
      LocalObject(436, Door.Constructor(Vector3(3230f, 2486f, 97.30687f)), owning_building_guid = 81)
      LocalObject(2923, Door.Constructor(Vector3(3229.146f, 2466.794f, 67.12287f)), owning_building_guid = 81)
      LocalObject(2924, Door.Constructor(Vector3(3229.146f, 2483.204f, 67.12287f)), owning_building_guid = 81)
      LocalObject(
        1076,
        IFFLock.Constructor(Vector3(3227.957f, 2486.811f, 77.24787f), Vector3(0, 0, 0)),
        owning_building_guid = 81,
        door_guid = 435
      )
      LocalObject(
        1077,
        IFFLock.Constructor(Vector3(3227.957f, 2486.811f, 97.24787f), Vector3(0, 0, 0)),
        owning_building_guid = 81,
        door_guid = 436
      )
      LocalObject(
        1078,
        IFFLock.Constructor(Vector3(3232.047f, 2469.189f, 77.24787f), Vector3(0, 0, 180)),
        owning_building_guid = 81,
        door_guid = 433
      )
      LocalObject(
        1079,
        IFFLock.Constructor(Vector3(3232.047f, 2469.189f, 97.24787f), Vector3(0, 0, 180)),
        owning_building_guid = 81,
        door_guid = 434
      )
      LocalObject(1408, Locker.Constructor(Vector3(3233.716f, 2462.963f, 65.78088f)), owning_building_guid = 81)
      LocalObject(1409, Locker.Constructor(Vector3(3233.751f, 2484.835f, 65.78088f)), owning_building_guid = 81)
      LocalObject(1410, Locker.Constructor(Vector3(3235.053f, 2462.963f, 65.78088f)), owning_building_guid = 81)
      LocalObject(1411, Locker.Constructor(Vector3(3235.088f, 2484.835f, 65.78088f)), owning_building_guid = 81)
      LocalObject(1412, Locker.Constructor(Vector3(3237.741f, 2462.963f, 65.78088f)), owning_building_guid = 81)
      LocalObject(1413, Locker.Constructor(Vector3(3237.741f, 2484.835f, 65.78088f)), owning_building_guid = 81)
      LocalObject(1414, Locker.Constructor(Vector3(3239.143f, 2462.963f, 65.78088f)), owning_building_guid = 81)
      LocalObject(1415, Locker.Constructor(Vector3(3239.143f, 2484.835f, 65.78088f)), owning_building_guid = 81)
      LocalObject(
        1973,
        Terminal.Constructor(Vector3(3239.445f, 2468.129f, 67.11887f), order_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        1974,
        Terminal.Constructor(Vector3(3239.445f, 2473.853f, 67.11887f), order_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        1975,
        Terminal.Constructor(Vector3(3239.445f, 2479.234f, 67.11887f), order_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        2699,
        SpawnTube.Constructor(Vector3(3228.706f, 2465.742f, 65.26888f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 81
      )
      LocalObject(
        2700,
        SpawnTube.Constructor(Vector3(3228.706f, 2482.152f, 65.26888f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 81
      )
      LocalObject(
        2330,
        ProximityTerminal.Constructor(Vector3(3216.907f, 2472.725f, 103.3569f), pad_landing_tower_frame),
        owning_building_guid = 81
      )
      LocalObject(
        2331,
        Terminal.Constructor(Vector3(3216.907f, 2472.725f, 103.3569f), air_rearm_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        2333,
        ProximityTerminal.Constructor(Vector3(3216.907f, 2483.17f, 103.3569f), pad_landing_tower_frame),
        owning_building_guid = 81
      )
      LocalObject(
        2334,
        Terminal.Constructor(Vector3(3216.907f, 2483.17f, 103.3569f), air_rearm_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        1798,
        FacilityTurret.Constructor(Vector3(3203.07f, 2463.045f, 94.72887f), manned_turret),
        owning_building_guid = 81
      )
      TurretToWeapon(1798, 5102)
      LocalObject(
        1804,
        FacilityTurret.Constructor(Vector3(3241.497f, 2492.957f, 94.72887f), manned_turret),
        owning_building_guid = 81
      )
      TurretToWeapon(1804, 5103)
      LocalObject(
        2484,
        Painbox.Constructor(Vector3(3222.454f, 2470.849f, 67.80637f), painbox_radius_continuous),
        owning_building_guid = 81
      )
      LocalObject(
        2487,
        Painbox.Constructor(Vector3(3234.923f, 2467.54f, 65.88687f), painbox_radius_continuous),
        owning_building_guid = 81
      )
      LocalObject(
        2488,
        Painbox.Constructor(Vector3(3235.113f, 2480.022f, 65.88687f), painbox_radius_continuous),
        owning_building_guid = 81
      )
    }

    Building56()

    def Building56(): Unit = { // Name: Hanish_Tower Type: tower_c GUID: 82, MapID: 56
      LocalBuilding(
        "Hanish_Tower",
        82,
        56,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3580f, 5294f, 69.18471f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2784,
        CaptureTerminal.Constructor(Vector3(3596.587f, 5293.897f, 79.18371f), secondary_capture),
        owning_building_guid = 82
      )
      LocalObject(466, Door.Constructor(Vector3(3592f, 5286f, 70.70571f)), owning_building_guid = 82)
      LocalObject(467, Door.Constructor(Vector3(3592f, 5286f, 90.70471f)), owning_building_guid = 82)
      LocalObject(468, Door.Constructor(Vector3(3592f, 5302f, 70.70571f)), owning_building_guid = 82)
      LocalObject(469, Door.Constructor(Vector3(3592f, 5302f, 90.70471f)), owning_building_guid = 82)
      LocalObject(2932, Door.Constructor(Vector3(3591.146f, 5282.794f, 60.52071f)), owning_building_guid = 82)
      LocalObject(2933, Door.Constructor(Vector3(3591.146f, 5299.204f, 60.52071f)), owning_building_guid = 82)
      LocalObject(
        1101,
        IFFLock.Constructor(Vector3(3589.957f, 5302.811f, 70.64571f), Vector3(0, 0, 0)),
        owning_building_guid = 82,
        door_guid = 468
      )
      LocalObject(
        1102,
        IFFLock.Constructor(Vector3(3589.957f, 5302.811f, 90.64571f), Vector3(0, 0, 0)),
        owning_building_guid = 82,
        door_guid = 469
      )
      LocalObject(
        1103,
        IFFLock.Constructor(Vector3(3594.047f, 5285.189f, 70.64571f), Vector3(0, 0, 180)),
        owning_building_guid = 82,
        door_guid = 466
      )
      LocalObject(
        1104,
        IFFLock.Constructor(Vector3(3594.047f, 5285.189f, 90.64571f), Vector3(0, 0, 180)),
        owning_building_guid = 82,
        door_guid = 467
      )
      LocalObject(1444, Locker.Constructor(Vector3(3595.716f, 5278.963f, 59.17871f)), owning_building_guid = 82)
      LocalObject(1445, Locker.Constructor(Vector3(3595.751f, 5300.835f, 59.17871f)), owning_building_guid = 82)
      LocalObject(1446, Locker.Constructor(Vector3(3597.053f, 5278.963f, 59.17871f)), owning_building_guid = 82)
      LocalObject(1447, Locker.Constructor(Vector3(3597.088f, 5300.835f, 59.17871f)), owning_building_guid = 82)
      LocalObject(1448, Locker.Constructor(Vector3(3599.741f, 5278.963f, 59.17871f)), owning_building_guid = 82)
      LocalObject(1449, Locker.Constructor(Vector3(3599.741f, 5300.835f, 59.17871f)), owning_building_guid = 82)
      LocalObject(1450, Locker.Constructor(Vector3(3601.143f, 5278.963f, 59.17871f)), owning_building_guid = 82)
      LocalObject(1451, Locker.Constructor(Vector3(3601.143f, 5300.835f, 59.17871f)), owning_building_guid = 82)
      LocalObject(
        1988,
        Terminal.Constructor(Vector3(3601.445f, 5284.129f, 60.51671f), order_terminal),
        owning_building_guid = 82
      )
      LocalObject(
        1989,
        Terminal.Constructor(Vector3(3601.445f, 5289.853f, 60.51671f), order_terminal),
        owning_building_guid = 82
      )
      LocalObject(
        1990,
        Terminal.Constructor(Vector3(3601.445f, 5295.234f, 60.51671f), order_terminal),
        owning_building_guid = 82
      )
      LocalObject(
        2708,
        SpawnTube.Constructor(Vector3(3590.706f, 5281.742f, 58.66671f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 82
      )
      LocalObject(
        2709,
        SpawnTube.Constructor(Vector3(3590.706f, 5298.152f, 58.66671f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 82
      )
      LocalObject(
        2336,
        ProximityTerminal.Constructor(Vector3(3578.907f, 5288.725f, 96.75471f), pad_landing_tower_frame),
        owning_building_guid = 82
      )
      LocalObject(
        2337,
        Terminal.Constructor(Vector3(3578.907f, 5288.725f, 96.75471f), air_rearm_terminal),
        owning_building_guid = 82
      )
      LocalObject(
        2339,
        ProximityTerminal.Constructor(Vector3(3578.907f, 5299.17f, 96.75471f), pad_landing_tower_frame),
        owning_building_guid = 82
      )
      LocalObject(
        2340,
        Terminal.Constructor(Vector3(3578.907f, 5299.17f, 96.75471f), air_rearm_terminal),
        owning_building_guid = 82
      )
      LocalObject(
        1810,
        FacilityTurret.Constructor(Vector3(3565.07f, 5279.045f, 88.12671f), manned_turret),
        owning_building_guid = 82
      )
      TurretToWeapon(1810, 5104)
      LocalObject(
        1811,
        FacilityTurret.Constructor(Vector3(3603.497f, 5308.957f, 88.12671f), manned_turret),
        owning_building_guid = 82
      )
      TurretToWeapon(1811, 5105)
      LocalObject(
        2495,
        Painbox.Constructor(Vector3(3584.454f, 5286.849f, 61.20421f), painbox_radius_continuous),
        owning_building_guid = 82
      )
      LocalObject(
        2496,
        Painbox.Constructor(Vector3(3596.923f, 5283.54f, 59.28471f), painbox_radius_continuous),
        owning_building_guid = 82
      )
      LocalObject(
        2497,
        Painbox.Constructor(Vector3(3597.113f, 5296.022f, 59.28471f), painbox_radius_continuous),
        owning_building_guid = 82
      )
    }

    Building36()

    def Building36(): Unit = { // Name: W_Girru_Tower Type: tower_c GUID: 83, MapID: 36
      LocalBuilding(
        "W_Girru_Tower",
        83,
        36,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3738f, 6048f, 55.94899f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2785,
        CaptureTerminal.Constructor(Vector3(3754.587f, 6047.897f, 65.94799f), secondary_capture),
        owning_building_guid = 83
      )
      LocalObject(477, Door.Constructor(Vector3(3750f, 6040f, 57.46999f)), owning_building_guid = 83)
      LocalObject(478, Door.Constructor(Vector3(3750f, 6040f, 77.46899f)), owning_building_guid = 83)
      LocalObject(479, Door.Constructor(Vector3(3750f, 6056f, 57.46999f)), owning_building_guid = 83)
      LocalObject(480, Door.Constructor(Vector3(3750f, 6056f, 77.46899f)), owning_building_guid = 83)
      LocalObject(2935, Door.Constructor(Vector3(3749.146f, 6036.794f, 47.28499f)), owning_building_guid = 83)
      LocalObject(2936, Door.Constructor(Vector3(3749.146f, 6053.204f, 47.28499f)), owning_building_guid = 83)
      LocalObject(
        1109,
        IFFLock.Constructor(Vector3(3747.957f, 6056.811f, 57.40999f), Vector3(0, 0, 0)),
        owning_building_guid = 83,
        door_guid = 479
      )
      LocalObject(
        1110,
        IFFLock.Constructor(Vector3(3747.957f, 6056.811f, 77.40999f), Vector3(0, 0, 0)),
        owning_building_guid = 83,
        door_guid = 480
      )
      LocalObject(
        1111,
        IFFLock.Constructor(Vector3(3752.047f, 6039.189f, 57.40999f), Vector3(0, 0, 180)),
        owning_building_guid = 83,
        door_guid = 477
      )
      LocalObject(
        1112,
        IFFLock.Constructor(Vector3(3752.047f, 6039.189f, 77.40999f), Vector3(0, 0, 180)),
        owning_building_guid = 83,
        door_guid = 478
      )
      LocalObject(1462, Locker.Constructor(Vector3(3753.716f, 6032.963f, 45.94299f)), owning_building_guid = 83)
      LocalObject(1463, Locker.Constructor(Vector3(3753.751f, 6054.835f, 45.94299f)), owning_building_guid = 83)
      LocalObject(1464, Locker.Constructor(Vector3(3755.053f, 6032.963f, 45.94299f)), owning_building_guid = 83)
      LocalObject(1465, Locker.Constructor(Vector3(3755.088f, 6054.835f, 45.94299f)), owning_building_guid = 83)
      LocalObject(1466, Locker.Constructor(Vector3(3757.741f, 6032.963f, 45.94299f)), owning_building_guid = 83)
      LocalObject(1467, Locker.Constructor(Vector3(3757.741f, 6054.835f, 45.94299f)), owning_building_guid = 83)
      LocalObject(1468, Locker.Constructor(Vector3(3759.143f, 6032.963f, 45.94299f)), owning_building_guid = 83)
      LocalObject(1469, Locker.Constructor(Vector3(3759.143f, 6054.835f, 45.94299f)), owning_building_guid = 83)
      LocalObject(
        1995,
        Terminal.Constructor(Vector3(3759.445f, 6038.129f, 47.28099f), order_terminal),
        owning_building_guid = 83
      )
      LocalObject(
        1996,
        Terminal.Constructor(Vector3(3759.445f, 6043.853f, 47.28099f), order_terminal),
        owning_building_guid = 83
      )
      LocalObject(
        1997,
        Terminal.Constructor(Vector3(3759.445f, 6049.234f, 47.28099f), order_terminal),
        owning_building_guid = 83
      )
      LocalObject(
        2711,
        SpawnTube.Constructor(Vector3(3748.706f, 6035.742f, 45.43099f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 83
      )
      LocalObject(
        2712,
        SpawnTube.Constructor(Vector3(3748.706f, 6052.152f, 45.43099f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 83
      )
      LocalObject(
        2342,
        ProximityTerminal.Constructor(Vector3(3736.907f, 6042.725f, 83.51899f), pad_landing_tower_frame),
        owning_building_guid = 83
      )
      LocalObject(
        2343,
        Terminal.Constructor(Vector3(3736.907f, 6042.725f, 83.51899f), air_rearm_terminal),
        owning_building_guid = 83
      )
      LocalObject(
        2345,
        ProximityTerminal.Constructor(Vector3(3736.907f, 6053.17f, 83.51899f), pad_landing_tower_frame),
        owning_building_guid = 83
      )
      LocalObject(
        2346,
        Terminal.Constructor(Vector3(3736.907f, 6053.17f, 83.51899f), air_rearm_terminal),
        owning_building_guid = 83
      )
      LocalObject(
        1814,
        FacilityTurret.Constructor(Vector3(3723.07f, 6033.045f, 74.89099f), manned_turret),
        owning_building_guid = 83
      )
      TurretToWeapon(1814, 5106)
      LocalObject(
        1815,
        FacilityTurret.Constructor(Vector3(3761.497f, 6062.957f, 74.89099f), manned_turret),
        owning_building_guid = 83
      )
      TurretToWeapon(1815, 5107)
      LocalObject(
        2498,
        Painbox.Constructor(Vector3(3742.454f, 6040.849f, 47.96849f), painbox_radius_continuous),
        owning_building_guid = 83
      )
      LocalObject(
        2499,
        Painbox.Constructor(Vector3(3754.923f, 6037.54f, 46.04899f), painbox_radius_continuous),
        owning_building_guid = 83
      )
      LocalObject(
        2500,
        Painbox.Constructor(Vector3(3755.113f, 6050.022f, 46.04899f), painbox_radius_continuous),
        owning_building_guid = 83
      )
    }

    Building61()

    def Building61(): Unit = { // Name: TRSanc_Warpgate_Tower Type: tower_c GUID: 84, MapID: 61
      LocalBuilding(
        "TRSanc_Warpgate_Tower",
        84,
        61,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4098f, 988f, 52.18349f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2786,
        CaptureTerminal.Constructor(Vector3(4114.587f, 987.897f, 62.1825f), secondary_capture),
        owning_building_guid = 84
      )
      LocalObject(499, Door.Constructor(Vector3(4110f, 980f, 53.70449f)), owning_building_guid = 84)
      LocalObject(500, Door.Constructor(Vector3(4110f, 980f, 73.70349f)), owning_building_guid = 84)
      LocalObject(501, Door.Constructor(Vector3(4110f, 996f, 53.70449f)), owning_building_guid = 84)
      LocalObject(502, Door.Constructor(Vector3(4110f, 996f, 73.70349f)), owning_building_guid = 84)
      LocalObject(2942, Door.Constructor(Vector3(4109.146f, 976.794f, 43.51949f)), owning_building_guid = 84)
      LocalObject(2943, Door.Constructor(Vector3(4109.146f, 993.204f, 43.51949f)), owning_building_guid = 84)
      LocalObject(
        1128,
        IFFLock.Constructor(Vector3(4107.957f, 996.811f, 53.64449f), Vector3(0, 0, 0)),
        owning_building_guid = 84,
        door_guid = 501
      )
      LocalObject(
        1129,
        IFFLock.Constructor(Vector3(4107.957f, 996.811f, 73.64449f), Vector3(0, 0, 0)),
        owning_building_guid = 84,
        door_guid = 502
      )
      LocalObject(
        1130,
        IFFLock.Constructor(Vector3(4112.047f, 979.189f, 53.64449f), Vector3(0, 0, 180)),
        owning_building_guid = 84,
        door_guid = 499
      )
      LocalObject(
        1131,
        IFFLock.Constructor(Vector3(4112.047f, 979.189f, 73.64449f), Vector3(0, 0, 180)),
        owning_building_guid = 84,
        door_guid = 500
      )
      LocalObject(1493, Locker.Constructor(Vector3(4113.716f, 972.963f, 42.17749f)), owning_building_guid = 84)
      LocalObject(1494, Locker.Constructor(Vector3(4113.751f, 994.835f, 42.17749f)), owning_building_guid = 84)
      LocalObject(1495, Locker.Constructor(Vector3(4115.053f, 972.963f, 42.17749f)), owning_building_guid = 84)
      LocalObject(1496, Locker.Constructor(Vector3(4115.088f, 994.835f, 42.17749f)), owning_building_guid = 84)
      LocalObject(1497, Locker.Constructor(Vector3(4117.741f, 972.963f, 42.17749f)), owning_building_guid = 84)
      LocalObject(1498, Locker.Constructor(Vector3(4117.741f, 994.835f, 42.17749f)), owning_building_guid = 84)
      LocalObject(1499, Locker.Constructor(Vector3(4119.143f, 972.963f, 42.17749f)), owning_building_guid = 84)
      LocalObject(1500, Locker.Constructor(Vector3(4119.143f, 994.835f, 42.17749f)), owning_building_guid = 84)
      LocalObject(
        2005,
        Terminal.Constructor(Vector3(4119.445f, 978.129f, 43.5155f), order_terminal),
        owning_building_guid = 84
      )
      LocalObject(
        2006,
        Terminal.Constructor(Vector3(4119.445f, 983.853f, 43.5155f), order_terminal),
        owning_building_guid = 84
      )
      LocalObject(
        2007,
        Terminal.Constructor(Vector3(4119.445f, 989.234f, 43.5155f), order_terminal),
        owning_building_guid = 84
      )
      LocalObject(
        2718,
        SpawnTube.Constructor(Vector3(4108.706f, 975.742f, 41.6655f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 84
      )
      LocalObject(
        2719,
        SpawnTube.Constructor(Vector3(4108.706f, 992.152f, 41.6655f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 84
      )
      LocalObject(
        2348,
        ProximityTerminal.Constructor(Vector3(4096.907f, 982.725f, 79.75349f), pad_landing_tower_frame),
        owning_building_guid = 84
      )
      LocalObject(
        2349,
        Terminal.Constructor(Vector3(4096.907f, 982.725f, 79.75349f), air_rearm_terminal),
        owning_building_guid = 84
      )
      LocalObject(
        2351,
        ProximityTerminal.Constructor(Vector3(4096.907f, 993.17f, 79.75349f), pad_landing_tower_frame),
        owning_building_guid = 84
      )
      LocalObject(
        2352,
        Terminal.Constructor(Vector3(4096.907f, 993.17f, 79.75349f), air_rearm_terminal),
        owning_building_guid = 84
      )
      LocalObject(
        1824,
        FacilityTurret.Constructor(Vector3(4083.07f, 973.045f, 71.1255f), manned_turret),
        owning_building_guid = 84
      )
      TurretToWeapon(1824, 5108)
      LocalObject(
        1826,
        FacilityTurret.Constructor(Vector3(4121.497f, 1002.957f, 71.1255f), manned_turret),
        owning_building_guid = 84
      )
      TurretToWeapon(1826, 5109)
      LocalObject(
        2501,
        Painbox.Constructor(Vector3(4102.454f, 980.849f, 44.203f), painbox_radius_continuous),
        owning_building_guid = 84
      )
      LocalObject(
        2502,
        Painbox.Constructor(Vector3(4114.923f, 977.5395f, 42.28349f), painbox_radius_continuous),
        owning_building_guid = 84
      )
      LocalObject(
        2503,
        Painbox.Constructor(Vector3(4115.113f, 990.0219f, 42.28349f), painbox_radius_continuous),
        owning_building_guid = 84
      )
    }

    Building41()

    def Building41(): Unit = { // Name: W_Ceryshen_Warpgate_Tower Type: tower_c GUID: 85, MapID: 41
      LocalBuilding(
        "W_Ceryshen_Warpgate_Tower",
        85,
        41,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4810f, 3474f, 38.48336f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2794,
        CaptureTerminal.Constructor(Vector3(4826.587f, 3473.897f, 48.48236f), secondary_capture),
        owning_building_guid = 85
      )
      LocalObject(568, Door.Constructor(Vector3(4822f, 3466f, 40.00436f)), owning_building_guid = 85)
      LocalObject(569, Door.Constructor(Vector3(4822f, 3466f, 60.00336f)), owning_building_guid = 85)
      LocalObject(570, Door.Constructor(Vector3(4822f, 3482f, 40.00436f)), owning_building_guid = 85)
      LocalObject(571, Door.Constructor(Vector3(4822f, 3482f, 60.00336f)), owning_building_guid = 85)
      LocalObject(2967, Door.Constructor(Vector3(4821.146f, 3462.794f, 29.81936f)), owning_building_guid = 85)
      LocalObject(2968, Door.Constructor(Vector3(4821.146f, 3479.204f, 29.81936f)), owning_building_guid = 85)
      LocalObject(
        1185,
        IFFLock.Constructor(Vector3(4819.957f, 3482.811f, 39.94436f), Vector3(0, 0, 0)),
        owning_building_guid = 85,
        door_guid = 570
      )
      LocalObject(
        1186,
        IFFLock.Constructor(Vector3(4819.957f, 3482.811f, 59.94437f), Vector3(0, 0, 0)),
        owning_building_guid = 85,
        door_guid = 571
      )
      LocalObject(
        1188,
        IFFLock.Constructor(Vector3(4824.047f, 3465.189f, 39.94436f), Vector3(0, 0, 180)),
        owning_building_guid = 85,
        door_guid = 568
      )
      LocalObject(
        1189,
        IFFLock.Constructor(Vector3(4824.047f, 3465.189f, 59.94437f), Vector3(0, 0, 180)),
        owning_building_guid = 85,
        door_guid = 569
      )
      LocalObject(1602, Locker.Constructor(Vector3(4825.716f, 3458.963f, 28.47736f)), owning_building_guid = 85)
      LocalObject(1603, Locker.Constructor(Vector3(4825.751f, 3480.835f, 28.47736f)), owning_building_guid = 85)
      LocalObject(1604, Locker.Constructor(Vector3(4827.053f, 3458.963f, 28.47736f)), owning_building_guid = 85)
      LocalObject(1605, Locker.Constructor(Vector3(4827.088f, 3480.835f, 28.47736f)), owning_building_guid = 85)
      LocalObject(1606, Locker.Constructor(Vector3(4829.741f, 3458.963f, 28.47736f)), owning_building_guid = 85)
      LocalObject(1607, Locker.Constructor(Vector3(4829.741f, 3480.835f, 28.47736f)), owning_building_guid = 85)
      LocalObject(1608, Locker.Constructor(Vector3(4831.143f, 3458.963f, 28.47736f)), owning_building_guid = 85)
      LocalObject(1609, Locker.Constructor(Vector3(4831.143f, 3480.835f, 28.47736f)), owning_building_guid = 85)
      LocalObject(
        2044,
        Terminal.Constructor(Vector3(4831.445f, 3464.129f, 29.81536f), order_terminal),
        owning_building_guid = 85
      )
      LocalObject(
        2045,
        Terminal.Constructor(Vector3(4831.445f, 3469.853f, 29.81536f), order_terminal),
        owning_building_guid = 85
      )
      LocalObject(
        2046,
        Terminal.Constructor(Vector3(4831.445f, 3475.234f, 29.81536f), order_terminal),
        owning_building_guid = 85
      )
      LocalObject(
        2743,
        SpawnTube.Constructor(Vector3(4820.706f, 3461.742f, 27.96536f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 85
      )
      LocalObject(
        2744,
        SpawnTube.Constructor(Vector3(4820.706f, 3478.152f, 27.96536f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 85
      )
      LocalObject(
        2354,
        ProximityTerminal.Constructor(Vector3(4808.907f, 3468.725f, 66.05336f), pad_landing_tower_frame),
        owning_building_guid = 85
      )
      LocalObject(
        2355,
        Terminal.Constructor(Vector3(4808.907f, 3468.725f, 66.05336f), air_rearm_terminal),
        owning_building_guid = 85
      )
      LocalObject(
        2357,
        ProximityTerminal.Constructor(Vector3(4808.907f, 3479.17f, 66.05336f), pad_landing_tower_frame),
        owning_building_guid = 85
      )
      LocalObject(
        2358,
        Terminal.Constructor(Vector3(4808.907f, 3479.17f, 66.05336f), air_rearm_terminal),
        owning_building_guid = 85
      )
      LocalObject(
        1846,
        FacilityTurret.Constructor(Vector3(4795.07f, 3459.045f, 57.42536f), manned_turret),
        owning_building_guid = 85
      )
      TurretToWeapon(1846, 5110)
      LocalObject(
        1851,
        FacilityTurret.Constructor(Vector3(4833.497f, 3488.957f, 57.42536f), manned_turret),
        owning_building_guid = 85
      )
      TurretToWeapon(1851, 5111)
      LocalObject(
        2525,
        Painbox.Constructor(Vector3(4814.454f, 3466.849f, 30.50286f), painbox_radius_continuous),
        owning_building_guid = 85
      )
      LocalObject(
        2526,
        Painbox.Constructor(Vector3(4826.923f, 3463.54f, 28.58336f), painbox_radius_continuous),
        owning_building_guid = 85
      )
      LocalObject(
        2527,
        Painbox.Constructor(Vector3(4827.113f, 3476.022f, 28.58336f), painbox_radius_continuous),
        owning_building_guid = 85
      )
    }

    Building55()

    def Building55(): Unit = { // Name: Irkalla_Tower Type: tower_c GUID: 86, MapID: 55
      LocalBuilding(
        "Irkalla_Tower",
        86,
        55,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4884f, 4940f, 67.48396f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2795,
        CaptureTerminal.Constructor(Vector3(4900.587f, 4939.897f, 77.48296f), secondary_capture),
        owning_building_guid = 86
      )
      LocalObject(581, Door.Constructor(Vector3(4896f, 4932f, 69.00496f)), owning_building_guid = 86)
      LocalObject(582, Door.Constructor(Vector3(4896f, 4932f, 89.00395f)), owning_building_guid = 86)
      LocalObject(583, Door.Constructor(Vector3(4896f, 4948f, 69.00496f)), owning_building_guid = 86)
      LocalObject(584, Door.Constructor(Vector3(4896f, 4948f, 89.00395f)), owning_building_guid = 86)
      LocalObject(2969, Door.Constructor(Vector3(4895.146f, 4928.794f, 58.81995f)), owning_building_guid = 86)
      LocalObject(2970, Door.Constructor(Vector3(4895.146f, 4945.204f, 58.81995f)), owning_building_guid = 86)
      LocalObject(
        1197,
        IFFLock.Constructor(Vector3(4893.957f, 4948.811f, 68.94495f), Vector3(0, 0, 0)),
        owning_building_guid = 86,
        door_guid = 583
      )
      LocalObject(
        1198,
        IFFLock.Constructor(Vector3(4893.957f, 4948.811f, 88.94495f), Vector3(0, 0, 0)),
        owning_building_guid = 86,
        door_guid = 584
      )
      LocalObject(
        1199,
        IFFLock.Constructor(Vector3(4898.047f, 4931.189f, 68.94495f), Vector3(0, 0, 180)),
        owning_building_guid = 86,
        door_guid = 581
      )
      LocalObject(
        1200,
        IFFLock.Constructor(Vector3(4898.047f, 4931.189f, 88.94495f), Vector3(0, 0, 180)),
        owning_building_guid = 86,
        door_guid = 582
      )
      LocalObject(1610, Locker.Constructor(Vector3(4899.716f, 4924.963f, 57.47795f)), owning_building_guid = 86)
      LocalObject(1611, Locker.Constructor(Vector3(4899.751f, 4946.835f, 57.47795f)), owning_building_guid = 86)
      LocalObject(1612, Locker.Constructor(Vector3(4901.053f, 4924.963f, 57.47795f)), owning_building_guid = 86)
      LocalObject(1613, Locker.Constructor(Vector3(4901.088f, 4946.835f, 57.47795f)), owning_building_guid = 86)
      LocalObject(1614, Locker.Constructor(Vector3(4903.741f, 4924.963f, 57.47795f)), owning_building_guid = 86)
      LocalObject(1615, Locker.Constructor(Vector3(4903.741f, 4946.835f, 57.47795f)), owning_building_guid = 86)
      LocalObject(1616, Locker.Constructor(Vector3(4905.143f, 4924.963f, 57.47795f)), owning_building_guid = 86)
      LocalObject(1617, Locker.Constructor(Vector3(4905.143f, 4946.835f, 57.47795f)), owning_building_guid = 86)
      LocalObject(
        2047,
        Terminal.Constructor(Vector3(4905.445f, 4930.129f, 58.81596f), order_terminal),
        owning_building_guid = 86
      )
      LocalObject(
        2048,
        Terminal.Constructor(Vector3(4905.445f, 4935.853f, 58.81596f), order_terminal),
        owning_building_guid = 86
      )
      LocalObject(
        2049,
        Terminal.Constructor(Vector3(4905.445f, 4941.234f, 58.81596f), order_terminal),
        owning_building_guid = 86
      )
      LocalObject(
        2745,
        SpawnTube.Constructor(Vector3(4894.706f, 4927.742f, 56.96596f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 86
      )
      LocalObject(
        2746,
        SpawnTube.Constructor(Vector3(4894.706f, 4944.152f, 56.96596f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 86
      )
      LocalObject(
        2360,
        ProximityTerminal.Constructor(Vector3(4882.907f, 4934.725f, 95.05396f), pad_landing_tower_frame),
        owning_building_guid = 86
      )
      LocalObject(
        2361,
        Terminal.Constructor(Vector3(4882.907f, 4934.725f, 95.05396f), air_rearm_terminal),
        owning_building_guid = 86
      )
      LocalObject(
        2363,
        ProximityTerminal.Constructor(Vector3(4882.907f, 4945.17f, 95.05396f), pad_landing_tower_frame),
        owning_building_guid = 86
      )
      LocalObject(
        2364,
        Terminal.Constructor(Vector3(4882.907f, 4945.17f, 95.05396f), air_rearm_terminal),
        owning_building_guid = 86
      )
      LocalObject(
        1852,
        FacilityTurret.Constructor(Vector3(4869.07f, 4925.045f, 86.42596f), manned_turret),
        owning_building_guid = 86
      )
      TurretToWeapon(1852, 5112)
      LocalObject(
        1855,
        FacilityTurret.Constructor(Vector3(4907.497f, 4954.957f, 86.42596f), manned_turret),
        owning_building_guid = 86
      )
      TurretToWeapon(1855, 5113)
      LocalObject(
        2528,
        Painbox.Constructor(Vector3(4888.454f, 4932.849f, 59.50346f), painbox_radius_continuous),
        owning_building_guid = 86
      )
      LocalObject(
        2529,
        Painbox.Constructor(Vector3(4900.923f, 4929.54f, 57.58395f), painbox_radius_continuous),
        owning_building_guid = 86
      )
      LocalObject(
        2530,
        Painbox.Constructor(Vector3(4901.113f, 4942.022f, 57.58395f), painbox_radius_continuous),
        owning_building_guid = 86
      )
    }

    Building20()

    def Building20(): Unit = { // Name: E_Zaqar_Tower Type: tower_c GUID: 87, MapID: 20
      LocalBuilding(
        "E_Zaqar_Tower",
        87,
        20,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5312f, 2240f, 31.13437f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2796,
        CaptureTerminal.Constructor(Vector3(5328.587f, 2239.897f, 41.13337f), secondary_capture),
        owning_building_guid = 87
      )
      LocalObject(586, Door.Constructor(Vector3(5324f, 2232f, 32.65537f)), owning_building_guid = 87)
      LocalObject(587, Door.Constructor(Vector3(5324f, 2232f, 52.65437f)), owning_building_guid = 87)
      LocalObject(588, Door.Constructor(Vector3(5324f, 2248f, 32.65537f)), owning_building_guid = 87)
      LocalObject(589, Door.Constructor(Vector3(5324f, 2248f, 52.65437f)), owning_building_guid = 87)
      LocalObject(2971, Door.Constructor(Vector3(5323.146f, 2228.794f, 22.47037f)), owning_building_guid = 87)
      LocalObject(2972, Door.Constructor(Vector3(5323.146f, 2245.204f, 22.47037f)), owning_building_guid = 87)
      LocalObject(
        1201,
        IFFLock.Constructor(Vector3(5321.957f, 2248.811f, 32.59537f), Vector3(0, 0, 0)),
        owning_building_guid = 87,
        door_guid = 588
      )
      LocalObject(
        1202,
        IFFLock.Constructor(Vector3(5321.957f, 2248.811f, 52.59538f), Vector3(0, 0, 0)),
        owning_building_guid = 87,
        door_guid = 589
      )
      LocalObject(
        1203,
        IFFLock.Constructor(Vector3(5326.047f, 2231.189f, 32.59537f), Vector3(0, 0, 180)),
        owning_building_guid = 87,
        door_guid = 586
      )
      LocalObject(
        1204,
        IFFLock.Constructor(Vector3(5326.047f, 2231.189f, 52.59538f), Vector3(0, 0, 180)),
        owning_building_guid = 87,
        door_guid = 587
      )
      LocalObject(1618, Locker.Constructor(Vector3(5327.716f, 2224.963f, 21.12837f)), owning_building_guid = 87)
      LocalObject(1619, Locker.Constructor(Vector3(5327.751f, 2246.835f, 21.12837f)), owning_building_guid = 87)
      LocalObject(1620, Locker.Constructor(Vector3(5329.053f, 2224.963f, 21.12837f)), owning_building_guid = 87)
      LocalObject(1621, Locker.Constructor(Vector3(5329.088f, 2246.835f, 21.12837f)), owning_building_guid = 87)
      LocalObject(1622, Locker.Constructor(Vector3(5331.741f, 2224.963f, 21.12837f)), owning_building_guid = 87)
      LocalObject(1623, Locker.Constructor(Vector3(5331.741f, 2246.835f, 21.12837f)), owning_building_guid = 87)
      LocalObject(1624, Locker.Constructor(Vector3(5333.143f, 2224.963f, 21.12837f)), owning_building_guid = 87)
      LocalObject(1625, Locker.Constructor(Vector3(5333.143f, 2246.835f, 21.12837f)), owning_building_guid = 87)
      LocalObject(
        2050,
        Terminal.Constructor(Vector3(5333.445f, 2230.129f, 22.46637f), order_terminal),
        owning_building_guid = 87
      )
      LocalObject(
        2051,
        Terminal.Constructor(Vector3(5333.445f, 2235.853f, 22.46637f), order_terminal),
        owning_building_guid = 87
      )
      LocalObject(
        2052,
        Terminal.Constructor(Vector3(5333.445f, 2241.234f, 22.46637f), order_terminal),
        owning_building_guid = 87
      )
      LocalObject(
        2747,
        SpawnTube.Constructor(Vector3(5322.706f, 2227.742f, 20.61637f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 87
      )
      LocalObject(
        2748,
        SpawnTube.Constructor(Vector3(5322.706f, 2244.152f, 20.61637f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 87
      )
      LocalObject(
        2366,
        ProximityTerminal.Constructor(Vector3(5310.907f, 2234.725f, 58.70437f), pad_landing_tower_frame),
        owning_building_guid = 87
      )
      LocalObject(
        2367,
        Terminal.Constructor(Vector3(5310.907f, 2234.725f, 58.70437f), air_rearm_terminal),
        owning_building_guid = 87
      )
      LocalObject(
        2369,
        ProximityTerminal.Constructor(Vector3(5310.907f, 2245.17f, 58.70437f), pad_landing_tower_frame),
        owning_building_guid = 87
      )
      LocalObject(
        2370,
        Terminal.Constructor(Vector3(5310.907f, 2245.17f, 58.70437f), air_rearm_terminal),
        owning_building_guid = 87
      )
      LocalObject(
        1856,
        FacilityTurret.Constructor(Vector3(5297.07f, 2225.045f, 50.07637f), manned_turret),
        owning_building_guid = 87
      )
      TurretToWeapon(1856, 5114)
      LocalObject(
        1857,
        FacilityTurret.Constructor(Vector3(5335.497f, 2254.957f, 50.07637f), manned_turret),
        owning_building_guid = 87
      )
      TurretToWeapon(1857, 5115)
      LocalObject(
        2531,
        Painbox.Constructor(Vector3(5316.454f, 2232.849f, 23.15387f), painbox_radius_continuous),
        owning_building_guid = 87
      )
      LocalObject(
        2532,
        Painbox.Constructor(Vector3(5328.923f, 2229.54f, 21.23437f), painbox_radius_continuous),
        owning_building_guid = 87
      )
      LocalObject(
        2533,
        Painbox.Constructor(Vector3(5329.113f, 2242.022f, 21.23437f), painbox_radius_continuous),
        owning_building_guid = 87
      )
    }

    Building57()

    def Building57(): Unit = { // Name: E_Ceryshen_Warpgate_Tower Type: tower_c GUID: 88, MapID: 57
      LocalBuilding(
        "E_Ceryshen_Warpgate_Tower",
        88,
        57,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(7020f, 3444f, 28.58011f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2803,
        CaptureTerminal.Constructor(Vector3(7036.587f, 3443.897f, 38.57911f), secondary_capture),
        owning_building_guid = 88
      )
      LocalObject(637, Door.Constructor(Vector3(7032f, 3436f, 30.10111f)), owning_building_guid = 88)
      LocalObject(638, Door.Constructor(Vector3(7032f, 3436f, 50.10011f)), owning_building_guid = 88)
      LocalObject(639, Door.Constructor(Vector3(7032f, 3452f, 30.10111f)), owning_building_guid = 88)
      LocalObject(640, Door.Constructor(Vector3(7032f, 3452f, 50.10011f)), owning_building_guid = 88)
      LocalObject(2989, Door.Constructor(Vector3(7031.146f, 3432.794f, 19.91611f)), owning_building_guid = 88)
      LocalObject(2990, Door.Constructor(Vector3(7031.146f, 3449.204f, 19.91611f)), owning_building_guid = 88)
      LocalObject(
        1247,
        IFFLock.Constructor(Vector3(7029.957f, 3452.811f, 30.04111f), Vector3(0, 0, 0)),
        owning_building_guid = 88,
        door_guid = 639
      )
      LocalObject(
        1248,
        IFFLock.Constructor(Vector3(7029.957f, 3452.811f, 50.04111f), Vector3(0, 0, 0)),
        owning_building_guid = 88,
        door_guid = 640
      )
      LocalObject(
        1249,
        IFFLock.Constructor(Vector3(7034.047f, 3435.189f, 30.04111f), Vector3(0, 0, 180)),
        owning_building_guid = 88,
        door_guid = 637
      )
      LocalObject(
        1250,
        IFFLock.Constructor(Vector3(7034.047f, 3435.189f, 50.04111f), Vector3(0, 0, 180)),
        owning_building_guid = 88,
        door_guid = 638
      )
      LocalObject(1707, Locker.Constructor(Vector3(7035.716f, 3428.963f, 18.57411f)), owning_building_guid = 88)
      LocalObject(1708, Locker.Constructor(Vector3(7035.751f, 3450.835f, 18.57411f)), owning_building_guid = 88)
      LocalObject(1709, Locker.Constructor(Vector3(7037.053f, 3428.963f, 18.57411f)), owning_building_guid = 88)
      LocalObject(1710, Locker.Constructor(Vector3(7037.088f, 3450.835f, 18.57411f)), owning_building_guid = 88)
      LocalObject(1711, Locker.Constructor(Vector3(7039.741f, 3428.963f, 18.57411f)), owning_building_guid = 88)
      LocalObject(1712, Locker.Constructor(Vector3(7039.741f, 3450.835f, 18.57411f)), owning_building_guid = 88)
      LocalObject(1713, Locker.Constructor(Vector3(7041.143f, 3428.963f, 18.57411f)), owning_building_guid = 88)
      LocalObject(1714, Locker.Constructor(Vector3(7041.143f, 3450.835f, 18.57411f)), owning_building_guid = 88)
      LocalObject(
        2078,
        Terminal.Constructor(Vector3(7041.445f, 3434.129f, 19.91211f), order_terminal),
        owning_building_guid = 88
      )
      LocalObject(
        2079,
        Terminal.Constructor(Vector3(7041.445f, 3439.853f, 19.91211f), order_terminal),
        owning_building_guid = 88
      )
      LocalObject(
        2080,
        Terminal.Constructor(Vector3(7041.445f, 3445.234f, 19.91211f), order_terminal),
        owning_building_guid = 88
      )
      LocalObject(
        2765,
        SpawnTube.Constructor(Vector3(7030.706f, 3431.742f, 18.06211f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 88
      )
      LocalObject(
        2766,
        SpawnTube.Constructor(Vector3(7030.706f, 3448.152f, 18.06211f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 88
      )
      LocalObject(
        2372,
        ProximityTerminal.Constructor(Vector3(7018.907f, 3438.725f, 56.15011f), pad_landing_tower_frame),
        owning_building_guid = 88
      )
      LocalObject(
        2373,
        Terminal.Constructor(Vector3(7018.907f, 3438.725f, 56.15011f), air_rearm_terminal),
        owning_building_guid = 88
      )
      LocalObject(
        2375,
        ProximityTerminal.Constructor(Vector3(7018.907f, 3449.17f, 56.15011f), pad_landing_tower_frame),
        owning_building_guid = 88
      )
      LocalObject(
        2376,
        Terminal.Constructor(Vector3(7018.907f, 3449.17f, 56.15011f), air_rearm_terminal),
        owning_building_guid = 88
      )
      LocalObject(
        1872,
        FacilityTurret.Constructor(Vector3(7005.07f, 3429.045f, 47.52211f), manned_turret),
        owning_building_guid = 88
      )
      TurretToWeapon(1872, 5116)
      LocalObject(
        1874,
        FacilityTurret.Constructor(Vector3(7043.497f, 3458.957f, 47.52211f), manned_turret),
        owning_building_guid = 88
      )
      TurretToWeapon(1874, 5117)
      LocalObject(
        2552,
        Painbox.Constructor(Vector3(7024.454f, 3436.849f, 20.59961f), painbox_radius_continuous),
        owning_building_guid = 88
      )
      LocalObject(
        2553,
        Painbox.Constructor(Vector3(7036.923f, 3433.54f, 18.68011f), painbox_radius_continuous),
        owning_building_guid = 88
      )
      LocalObject(
        2554,
        Painbox.Constructor(Vector3(7037.113f, 3446.022f, 18.68011f), painbox_radius_continuous),
        owning_building_guid = 88
      )
    }

    Building4()

    def Building4(): Unit = { // Name: WG_Ishundar_to_Searhus Type: warpgate GUID: 89, MapID: 4
      LocalBuilding(
        "WG_Ishundar_to_Searhus",
        89,
        4,
        FoundationBuilder(WarpGate.Structure(Vector3(1440f, 3294f, 45.92022f)))
      )
    }

    Building3()

    def Building3(): Unit = { // Name: WG_Ishundar_to_TRSanc Type: warpgate GUID: 90, MapID: 3
      LocalBuilding(
        "WG_Ishundar_to_TRSanc",
        90,
        3,
        FoundationBuilder(WarpGate.Structure(Vector3(3752f, 1050f, 58.47187f)))
      )
    }

    Building2()

    def Building2(): Unit = { // Name: WG_Ishundar_to_VSSanc Type: warpgate GUID: 91, MapID: 2
      LocalBuilding(
        "WG_Ishundar_to_VSSanc",
        91,
        2,
        FoundationBuilder(WarpGate.Structure(Vector3(4728f, 6918f, 30.63858f)))
      )
    }

    Building1()

    def Building1(): Unit = { // Name: WG_Ishundar_to_Ceryshen Type: warpgate GUID: 92, MapID: 1
      LocalBuilding(
        "WG_Ishundar_to_Ceryshen",
        92,
        1,
        FoundationBuilder(WarpGate.Structure(Vector3(5576f, 3538f, 67.25803f)))
      )
    }

    def Lattice(): Unit = {
      LatticeLink("Baal", "Dagon")
      LatticeLink("Kusag", "Lahar")
      LatticeLink("Kusag", "Irkalla")
      LatticeLink("Lahar", "Irkalla")
      LatticeLink("Irkalla", "Girru")
      LatticeLink("Girru", "Hanish")
      LatticeLink("Hanish", "Irkalla")
      LatticeLink("Marduk", "GW_Ishundar_S")
      LatticeLink("Kusag", "WG_Ishundar_to_Ceryshen")
      LatticeLink("Baal", "WG_Ishundar_to_Searhus")
      LatticeLink("Marduk", "WG_Ishundar_to_TRSanc")
      LatticeLink("Baal", "Akkan")
      LatticeLink("Hanish", "GW_Ishundar_N")
      LatticeLink("Enkidu", "Marduk")
      LatticeLink("Girru", "WG_Ishundar_to_VSSanc")
      LatticeLink("Enkidu", "Kusag")
      LatticeLink("Dagon", "Hanish")
      LatticeLink("Enkidu", "Akkan")
      LatticeLink("Marduk", "Neti")
      LatticeLink("Neti", "Zaqar")
      LatticeLink("Enkidu", "Zaqar")
      LatticeLink("Hanish", "Akkan")
    }

    Lattice()

  }
}
