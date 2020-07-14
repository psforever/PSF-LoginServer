package net.psforever.pslogin.zonemaps

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

object Map02 { // Hossin
  val ZoneMap = new ZoneMap("map02") {
    Checksum = 1113780607L

    Building12()

    def Building12(): Unit = { // Name: Ixtab Type: amp_station GUID: 1, MapID: 12
      LocalBuilding(
        "Ixtab",
        1,
        12,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3390f, 3150f, 39.97925f),
            Vector3(0f, 0f, 315f),
            amp_station
          )
        )
      )
      LocalObject(
        186,
        CaptureTerminal.Constructor(Vector3(3387.643f, 3152.361f, 51.48725f), capture_terminal),
        owning_building_guid = 1
      )
      LocalObject(148, Door.Constructor(Vector3(3385.337f, 3145.039f, 52.88125f)), owning_building_guid = 1)
      LocalObject(149, Door.Constructor(Vector3(3394.963f, 3154.661f, 52.88125f)), owning_building_guid = 1)
      LocalObject(334, Door.Constructor(Vector3(3302.688f, 3158.823f, 41.73025f)), owning_building_guid = 1)
      LocalObject(337, Door.Constructor(Vector3(3315.553f, 3171.687f, 49.69325f)), owning_building_guid = 1)
      LocalObject(345, Door.Constructor(Vector3(3342.825f, 3078.072f, 49.69425f)), owning_building_guid = 1)
      LocalObject(346, Door.Constructor(Vector3(3355.689f, 3065.209f, 41.73025f)), owning_building_guid = 1)
      LocalObject(349, Door.Constructor(Vector3(3364.898f, 3124.898f, 46.70025f)), owning_building_guid = 1)
      LocalObject(350, Door.Constructor(Vector3(3365.317f, 3131.442f, 51.70625f)), owning_building_guid = 1)
      LocalObject(351, Door.Constructor(Vector3(3371.725f, 3125.036f, 51.70625f)), owning_building_guid = 1)
      LocalObject(352, Door.Constructor(Vector3(3374.253f, 3230.387f, 41.73025f)), owning_building_guid = 1)
      LocalObject(354, Door.Constructor(Vector3(3387.118f, 3243.252f, 49.69325f)), owning_building_guid = 1)
      LocalObject(355, Door.Constructor(Vector3(3388.239f, 3063.099f, 49.69425f)), owning_building_guid = 1)
      LocalObject(356, Door.Constructor(Vector3(3392.828f, 3260.309f, 41.70025f)), owning_building_guid = 1)
      LocalObject(359, Door.Constructor(Vector3(3401.103f, 3075.963f, 41.73025f)), owning_building_guid = 1)
      LocalObject(360, Door.Constructor(Vector3(3408.572f, 3174.667f, 51.70625f)), owning_building_guid = 1)
      LocalObject(361, Door.Constructor(Vector3(3414.98f, 3168.261f, 51.70625f)), owning_building_guid = 1)
      LocalObject(362, Door.Constructor(Vector3(3415.103f, 3175.103f, 46.70025f)), owning_building_guid = 1)
      LocalObject(364, Door.Constructor(Vector3(3438.567f, 3231.804f, 41.73025f)), owning_building_guid = 1)
      LocalObject(371, Door.Constructor(Vector3(3451.431f, 3218.94f, 49.69425f)), owning_building_guid = 1)
      LocalObject(609, Door.Constructor(Vector3(3353.23f, 3181.113f, 26.70025f)), owning_building_guid = 1)
      LocalObject(610, Door.Constructor(Vector3(3358.887f, 3164.142f, 26.70025f)), owning_building_guid = 1)
      LocalObject(611, Door.Constructor(Vector3(3358.887f, 3181.113f, 26.70025f)), owning_building_guid = 1)
      LocalObject(612, Door.Constructor(Vector3(3358.887f, 3220.711f, 34.20025f)), owning_building_guid = 1)
      LocalObject(613, Door.Constructor(Vector3(3364.544f, 3135.858f, 41.70025f)), owning_building_guid = 1)
      LocalObject(614, Door.Constructor(Vector3(3364.544f, 3141.515f, 41.70025f)), owning_building_guid = 1)
      LocalObject(615, Door.Constructor(Vector3(3370.201f, 3147.172f, 26.70025f)), owning_building_guid = 1)
      LocalObject(616, Door.Constructor(Vector3(3370.201f, 3147.172f, 34.20025f)), owning_building_guid = 1)
      LocalObject(617, Door.Constructor(Vector3(3371.363f, 3131.063f, 51.70025f)), owning_building_guid = 1)
      LocalObject(618, Door.Constructor(Vector3(3373.03f, 3133.03f, 46.70025f)), owning_building_guid = 1)
      LocalObject(619, Door.Constructor(Vector3(3381.515f, 3141.515f, 26.70025f)), owning_building_guid = 1)
      LocalObject(620, Door.Constructor(Vector3(3387.172f, 3164.142f, 34.20025f)), owning_building_guid = 1)
      LocalObject(621, Door.Constructor(Vector3(3387.172f, 3186.77f, 34.20025f)), owning_building_guid = 1)
      LocalObject(623, Door.Constructor(Vector3(3392.828f, 3152.828f, 34.20025f)), owning_building_guid = 1)
      LocalObject(624, Door.Constructor(Vector3(3398.485f, 3164.142f, 26.70025f)), owning_building_guid = 1)
      LocalObject(625, Door.Constructor(Vector3(3398.485f, 3169.799f, 34.20025f)), owning_building_guid = 1)
      LocalObject(626, Door.Constructor(Vector3(3398.485f, 3175.456f, 41.70025f)), owning_building_guid = 1)
      LocalObject(627, Door.Constructor(Vector3(3401.314f, 3133.03f, 41.70025f)), owning_building_guid = 1)
      LocalObject(628, Door.Constructor(Vector3(3404.142f, 3175.456f, 41.70025f)), owning_building_guid = 1)
      LocalObject(629, Door.Constructor(Vector3(3406.97f, 3138.686f, 41.70025f)), owning_building_guid = 1)
      LocalObject(630, Door.Constructor(Vector3(3406.97f, 3166.97f, 46.70025f)), owning_building_guid = 1)
      LocalObject(631, Door.Constructor(Vector3(3408.937f, 3168.637f, 51.70025f)), owning_building_guid = 1)
      LocalObject(786, Door.Constructor(Vector3(3410.647f, 3129.389f, 42.45925f)), owning_building_guid = 1)
      LocalObject(2383, Door.Constructor(Vector3(3369.074f, 3156.406f, 34.53325f)), owning_building_guid = 1)
      LocalObject(2384, Door.Constructor(Vector3(3374.231f, 3161.563f, 34.53325f)), owning_building_guid = 1)
      LocalObject(2385, Door.Constructor(Vector3(3379.385f, 3166.718f, 34.53325f)), owning_building_guid = 1)
      LocalObject(
        826,
        IFFLock.Constructor(Vector3(3414.735f, 3129.601f, 41.65925f), Vector3(0, 0, 135)),
        owning_building_guid = 1,
        door_guid = 786
      )
      LocalObject(
        907,
        IFFLock.Constructor(Vector3(3354.915f, 3181.652f, 26.51525f), Vector3(0, 0, 135)),
        owning_building_guid = 1,
        door_guid = 609
      )
      LocalObject(
        908,
        IFFLock.Constructor(Vector3(3363.313f, 3130.588f, 51.64025f), Vector3(0, 0, 315)),
        owning_building_guid = 1,
        door_guid = 350
      )
      LocalObject(
        909,
        IFFLock.Constructor(Vector3(3365.776f, 3122.888f, 46.64125f), Vector3(0, 0, 225)),
        owning_building_guid = 1,
        door_guid = 349
      )
      LocalObject(
        910,
        IFFLock.Constructor(Vector3(3370.74f, 3145.487f, 34.01525f), Vector3(0, 0, 225)),
        owning_building_guid = 1,
        door_guid = 616
      )
      LocalObject(
        911,
        IFFLock.Constructor(Vector3(3373.76f, 3125.923f, 51.64025f), Vector3(0, 0, 135)),
        owning_building_guid = 1,
        door_guid = 351
      )
      LocalObject(
        912,
        IFFLock.Constructor(Vector3(3379.739f, 3141.068f, 26.51525f), Vector3(0, 0, 315)),
        owning_building_guid = 1,
        door_guid = 619
      )
      LocalObject(
        913,
        IFFLock.Constructor(Vector3(3386.725f, 3165.919f, 34.01525f), Vector3(0, 0, 45)),
        owning_building_guid = 1,
        door_guid = 620
      )
      LocalObject(
        915,
        IFFLock.Constructor(Vector3(3391.967f, 3262.338f, 41.63925f), Vector3(0, 0, 45)),
        owning_building_guid = 1,
        door_guid = 356
      )
      LocalObject(
        916,
        IFFLock.Constructor(Vector3(3406.543f, 3173.818f, 51.64025f), Vector3(0, 0, 315)),
        owning_building_guid = 1,
        door_guid = 360
      )
      LocalObject(
        917,
        IFFLock.Constructor(Vector3(3414.238f, 3177.128f, 46.64125f), Vector3(0, 0, 45)),
        owning_building_guid = 1,
        door_guid = 362
      )
      LocalObject(
        918,
        IFFLock.Constructor(Vector3(3416.994f, 3169.156f, 51.64025f), Vector3(0, 0, 135)),
        owning_building_guid = 1,
        door_guid = 361
      )
      LocalObject(1153, Locker.Constructor(Vector3(3374.234f, 3146.166f, 32.94025f)), owning_building_guid = 1)
      LocalObject(1154, Locker.Constructor(Vector3(3375.057f, 3145.343f, 32.94025f)), owning_building_guid = 1)
      LocalObject(1155, Locker.Constructor(Vector3(3375.868f, 3144.532f, 32.94025f)), owning_building_guid = 1)
      LocalObject(1156, Locker.Constructor(Vector3(3376.681f, 3143.719f, 32.94025f)), owning_building_guid = 1)
      LocalObject(1157, Locker.Constructor(Vector3(3395.911f, 3152.808f, 25.17925f)), owning_building_guid = 1)
      LocalObject(1158, Locker.Constructor(Vector3(3396.847f, 3151.872f, 25.17925f)), owning_building_guid = 1)
      LocalObject(1159, Locker.Constructor(Vector3(3397.792f, 3150.927f, 25.17925f)), owning_building_guid = 1)
      LocalObject(1160, Locker.Constructor(Vector3(3398.737f, 3149.982f, 25.17925f)), owning_building_guid = 1)
      LocalObject(1161, Locker.Constructor(Vector3(3401.947f, 3146.771f, 25.17925f)), owning_building_guid = 1)
      LocalObject(1162, Locker.Constructor(Vector3(3402.884f, 3145.835f, 25.17925f)), owning_building_guid = 1)
      LocalObject(1163, Locker.Constructor(Vector3(3403.828f, 3144.89f, 25.17925f)), owning_building_guid = 1)
      LocalObject(1164, Locker.Constructor(Vector3(3404.774f, 3143.945f, 25.17925f)), owning_building_guid = 1)
      LocalObject(
        1616,
        Terminal.Constructor(Vector3(3374.105f, 3134.108f, 41.50825f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1617,
        Terminal.Constructor(Vector3(3374.111f, 3165.884f, 41.50825f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1618,
        Terminal.Constructor(Vector3(3380.144f, 3147.705f, 34.26925f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1619,
        Terminal.Constructor(Vector3(3382.782f, 3150.343f, 34.26925f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1620,
        Terminal.Constructor(Vector3(3385.462f, 3153.022f, 34.26925f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1621,
        Terminal.Constructor(Vector3(3405.892f, 3165.895f, 41.50825f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2302,
        Terminal.Constructor(Vector3(3367.524f, 3154.435f, 34.81325f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2303,
        Terminal.Constructor(Vector3(3372.677f, 3159.594f, 34.81325f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2304,
        Terminal.Constructor(Vector3(3377.833f, 3164.745f, 34.81325f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2305,
        Terminal.Constructor(Vector3(3382.926f, 3150.71f, 46.70725f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2306,
        Terminal.Constructor(Vector3(3383.966f, 3177.825f, 26.73625f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2307,
        Terminal.Constructor(Vector3(3383.966f, 3211.767f, 34.23625f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2509,
        Terminal.Constructor(Vector3(3414.429f, 3242.205f, 42.08125f), vehicle_terminal_combined),
        owning_building_guid = 1
      )
      LocalObject(
        1543,
        VehicleSpawnPad.Constructor(Vector3(3404.849f, 3232.498f, 37.92425f), mb_pad_creation, Vector3(0, 0, 225)),
        owning_building_guid = 1,
        terminal_guid = 2509
      )
      LocalObject(2170, ResourceSilo.Constructor(Vector3(3366.771f, 3052.32f, 47.21425f)), owning_building_guid = 1)
      LocalObject(
        2203,
        SpawnTube.Constructor(Vector3(3368.02f, 3155.975f, 32.67925f), Vector3(0, 0, 45)),
        owning_building_guid = 1
      )
      LocalObject(
        2204,
        SpawnTube.Constructor(Vector3(3373.176f, 3161.131f, 32.67925f), Vector3(0, 0, 45)),
        owning_building_guid = 1
      )
      LocalObject(
        2205,
        SpawnTube.Constructor(Vector3(3378.329f, 3166.284f, 32.67925f), Vector3(0, 0, 45)),
        owning_building_guid = 1
      )
      LocalObject(
        1561,
        ProximityTerminal.Constructor(Vector3(3374.406f, 3165.586f, 45.17925f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1562,
        ProximityTerminal.Constructor(Vector3(3399.945f, 3148.003f, 25.17925f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1788,
        ProximityTerminal.Constructor(Vector3(3343.899f, 3092.164f, 48.50725f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1789,
        Terminal.Constructor(Vector3(3343.899f, 3092.164f, 48.50725f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1791,
        ProximityTerminal.Constructor(Vector3(3363.5f, 3204.461f, 48.48425f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1792,
        Terminal.Constructor(Vector3(3363.5f, 3204.461f, 48.48425f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1797,
        ProximityTerminal.Constructor(Vector3(3410.889f, 3101.062f, 48.48425f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1798,
        Terminal.Constructor(Vector3(3410.889f, 3101.062f, 48.48425f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1803,
        ProximityTerminal.Constructor(Vector3(3449.403f, 3171.79f, 50.64525f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1804,
        Terminal.Constructor(Vector3(3449.403f, 3171.79f, 50.64525f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2099,
        ProximityTerminal.Constructor(Vector3(3340.578f, 3199.71f, 39.37925f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2100,
        Terminal.Constructor(Vector3(3340.578f, 3199.71f, 39.37925f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2111,
        ProximityTerminal.Constructor(Vector3(3434.348f, 3106.208f, 39.37925f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2112,
        Terminal.Constructor(Vector3(3434.348f, 3106.208f, 39.37925f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1461,
        FacilityTurret.Constructor(Vector3(3264.536f, 3138.514f, 48.68725f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1461, 5000)
      LocalObject(
        1463,
        FacilityTurret.Constructor(Vector3(3305.095f, 3094.805f, 48.68725f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1463, 5001)
      LocalObject(
        1467,
        FacilityTurret.Constructor(Vector3(3373.02f, 3030.016f, 48.68725f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1467, 5002)
      LocalObject(
        1470,
        FacilityTurret.Constructor(Vector3(3407.123f, 3281.142f, 48.68725f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1470, 5003)
      LocalObject(
        1474,
        FacilityTurret.Constructor(Vector3(3472.384f, 3126.231f, 48.68725f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1474, 5004)
      LocalObject(
        1477,
        FacilityTurret.Constructor(Vector3(3515.634f, 3172.617f, 48.68725f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1477, 5005)
      LocalObject(
        1944,
        Painbox.Constructor(Vector3(3342.813f, 3187.453f, 30.05025f), painbox),
        owning_building_guid = 1
      )
      LocalObject(
        1955,
        Painbox.Constructor(Vector3(3380.236f, 3157.765f, 37.62765f), painbox_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1966,
        Painbox.Constructor(Vector3(3354.248f, 3179.026f, 26.63825f), painbox_door_radius),
        owning_building_guid = 1
      )
      LocalObject(
        1983,
        Painbox.Constructor(Vector3(3369.449f, 3146.419f, 33.87925f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1984,
        Painbox.Constructor(Vector3(3388.132f, 3164.998f, 33.94375f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1985,
        Painbox.Constructor(Vector3(3393.836f, 3151.761f, 35.85785f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(254, Generator.Constructor(Vector3(3342.214f, 3192.094f, 23.88525f)), owning_building_guid = 1)
      LocalObject(
        243,
        Terminal.Constructor(Vector3(3348.04f, 3186.335f, 25.17925f), gen_control),
        owning_building_guid = 1
      )
    }

    Building46()

    def Building46(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 4, MapID: 46
      LocalBuilding(
        "bunker_gauntlet",
        4,
        46,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(2598f, 3700f, 41.29307f),
            Vector3(0f, 0f, 270f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(307, Door.Constructor(Vector3(2596.099f, 3675.077f, 42.81407f)), owning_building_guid = 4)
      LocalObject(308, Door.Constructor(Vector3(2596.088f, 3724.898f, 42.81407f)), owning_building_guid = 4)
    }

    Building44()

    def Building44(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 5, MapID: 44
      LocalBuilding(
        "bunker_gauntlet",
        5,
        44,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(3474f, 3242f, 39.97925f),
            Vector3(0f, 0f, 315f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(372, Door.Constructor(Vector3(3455.042f, 3258.254f, 41.50025f)), owning_building_guid = 5)
      LocalObject(379, Door.Constructor(Vector3(3490.279f, 3223.032f, 41.50025f)), owning_building_guid = 5)
    }

    Building47()

    def Building47(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 6, MapID: 47
      LocalBuilding(
        "bunker_gauntlet",
        6,
        47,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4580f, 3462f, 28.62407f),
            Vector3(0f, 0f, 270f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(450, Door.Constructor(Vector3(4578.099f, 3437.077f, 30.14507f)), owning_building_guid = 6)
      LocalObject(451, Door.Constructor(Vector3(4578.088f, 3486.898f, 30.14507f)), owning_building_guid = 6)
    }

    Building45()

    def Building45(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 7, MapID: 45
      LocalBuilding(
        "bunker_gauntlet",
        7,
        45,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(5404f, 3430f, 27.68258f),
            Vector3(0f, 0f, 180f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(479, Door.Constructor(Vector3(5379.077f, 3431.901f, 29.20358f)), owning_building_guid = 7)
      LocalObject(484, Door.Constructor(Vector3(5428.898f, 3431.912f, 29.20358f)), owning_building_guid = 7)
    }

    Building36()

    def Building36(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 8, MapID: 36
      LocalBuilding(
        "bunker_lg",
        8,
        36,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(2234f, 3870f, 33.47842f), Vector3(0f, 0f, 315f), bunker_lg)
        )
      )
      LocalObject(289, Door.Constructor(Vector3(2237.651f, 3869.965f, 34.99942f)), owning_building_guid = 8)
    }

    Building41()

    def Building41(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 9, MapID: 41
      LocalBuilding(
        "bunker_lg",
        9,
        41,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3218f, 5222f, 13.92865f), Vector3(0f, 0f, 44f), bunker_lg)
        )
      )
      LocalObject(325, Door.Constructor(Vector3(3218.098f, 5225.649f, 15.44965f)), owning_building_guid = 9)
    }

    Building34()

    def Building34(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 10, MapID: 34
      LocalBuilding(
        "bunker_lg",
        10,
        34,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3448f, 2944f, 38.54488f), Vector3(0f, 0f, 360f), bunker_lg)
        )
      )
      LocalObject(370, Door.Constructor(Vector3(3450.606f, 2946.557f, 40.06588f)), owning_building_guid = 10)
    }

    Building43()

    def Building43(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 11, MapID: 43
      LocalBuilding(
        "bunker_lg",
        11,
        43,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3544f, 3970f, 30.07141f), Vector3(0f, 0f, 90f), bunker_lg)
        )
      )
      LocalObject(382, Door.Constructor(Vector3(3541.443f, 3972.606f, 31.59241f)), owning_building_guid = 11)
    }

    Building35()

    def Building35(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 12, MapID: 35
      LocalBuilding(
        "bunker_lg",
        12,
        35,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4028f, 3712f, 29.54496f), Vector3(0f, 0f, 270f), bunker_lg)
        )
      )
      LocalObject(405, Door.Constructor(Vector3(4030.557f, 3709.394f, 31.06596f)), owning_building_guid = 12)
    }

    Building38()

    def Building38(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 13, MapID: 38
      LocalBuilding(
        "bunker_lg",
        13,
        38,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5520f, 3804f, 18.05474f), Vector3(0f, 0f, 270f), bunker_lg)
        )
      )
      LocalObject(491, Door.Constructor(Vector3(5522.557f, 3801.394f, 19.57574f)), owning_building_guid = 13)
    }

    Building37()

    def Building37(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 14, MapID: 37
      LocalBuilding(
        "bunker_sm",
        14,
        37,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3826f, 6192f, 29.88374f), Vector3(0f, 0f, 44f), bunker_sm)
        )
      )
      LocalObject(393, Door.Constructor(Vector3(3826.919f, 6192.812f, 31.40474f)), owning_building_guid = 14)
    }

    Building39()

    def Building39(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 15, MapID: 39
      LocalBuilding(
        "bunker_sm",
        15,
        39,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4678f, 3042f, 24.19964f), Vector3(0f, 0f, 89f), bunker_sm)
        )
      )
      LocalObject(452, Door.Constructor(Vector3(4678.076f, 3043.224f, 25.72064f)), owning_building_guid = 15)
    }

    Building40()

    def Building40(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 16, MapID: 40
      LocalBuilding(
        "bunker_sm",
        16,
        40,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(6606f, 2274f, 130.5399f), Vector3(0f, 0f, 270f), bunker_sm)
        )
      )
      LocalObject(520, Door.Constructor(Vector3(6605.945f, 2272.775f, 132.0609f)), owning_building_guid = 16)
    }

    Building48()

    def Building48(): Unit = { // Name: Hurakan Type: comm_station GUID: 17, MapID: 48
      LocalBuilding(
        "Hurakan",
        17,
        48,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(1904f, 2988f, 38.46553f),
            Vector3(0f, 0f, 270f),
            comm_station
          )
        )
      )
      LocalObject(
        183,
        CaptureTerminal.Constructor(Vector3(1980.734f, 2943.911f, 21.16553f), capture_terminal),
        owning_building_guid = 17
      )
      LocalObject(266, Door.Constructor(Vector3(1836.197f, 3012.5f, 40.21653f)), owning_building_guid = 17)
      LocalObject(267, Door.Constructor(Vector3(1836.197f, 3030.693f, 48.18053f)), owning_building_guid = 17)
      LocalObject(268, Door.Constructor(Vector3(1860.5f, 3047.804f, 40.21653f)), owning_building_guid = 17)
      LocalObject(269, Door.Constructor(Vector3(1878.693f, 3047.804f, 48.18053f)), owning_building_guid = 17)
      LocalObject(270, Door.Constructor(Vector3(1893.307f, 2927.799f, 48.18053f)), owning_building_guid = 17)
      LocalObject(271, Door.Constructor(Vector3(1895.231f, 3008f, 45.18653f)), owning_building_guid = 17)
      LocalObject(272, Door.Constructor(Vector3(1896.59f, 2992.375f, 52.62553f)), owning_building_guid = 17)
      LocalObject(273, Door.Constructor(Vector3(1904.295f, 3008f, 45.18653f)), owning_building_guid = 17)
      LocalObject(274, Door.Constructor(Vector3(1911.5f, 2927.799f, 40.21653f)), owning_building_guid = 17)
      LocalObject(275, Door.Constructor(Vector3(1924f, 2980f, 45.18653f)), owning_building_guid = 17)
      LocalObject(276, Door.Constructor(Vector3(1971.977f, 2969.308f, 48.17953f)), owning_building_guid = 17)
      LocalObject(277, Door.Constructor(Vector3(1971.977f, 2987.5f, 40.21653f)), owning_building_guid = 17)
      LocalObject(278, Door.Constructor(Vector3(1980f, 2956f, 40.18653f)), owning_building_guid = 17)
      LocalObject(542, Door.Constructor(Vector3(1900f, 3004f, 45.18653f)), owning_building_guid = 17)
      LocalObject(543, Door.Constructor(Vector3(1908f, 2988f, 32.68653f)), owning_building_guid = 17)
      LocalObject(544, Door.Constructor(Vector3(1912f, 2944f, 30.18653f)), owning_building_guid = 17)
      LocalObject(545, Door.Constructor(Vector3(1912f, 2968f, 35.18653f)), owning_building_guid = 17)
      LocalObject(546, Door.Constructor(Vector3(1912f, 2976f, 40.18653f)), owning_building_guid = 17)
      LocalObject(547, Door.Constructor(Vector3(1912f, 2992f, 22.68653f)), owning_building_guid = 17)
      LocalObject(548, Door.Constructor(Vector3(1916f, 2932f, 22.68653f)), owning_building_guid = 17)
      LocalObject(549, Door.Constructor(Vector3(1916f, 2980f, 45.18653f)), owning_building_guid = 17)
      LocalObject(550, Door.Constructor(Vector3(1916f, 3004f, 40.18653f)), owning_building_guid = 17)
      LocalObject(551, Door.Constructor(Vector3(1920f, 3008f, 32.68653f)), owning_building_guid = 17)
      LocalObject(552, Door.Constructor(Vector3(1924f, 2980f, 35.18653f)), owning_building_guid = 17)
      LocalObject(553, Door.Constructor(Vector3(1928f, 2960f, 22.68653f)), owning_building_guid = 17)
      LocalObject(554, Door.Constructor(Vector3(1928f, 3008f, 22.68653f)), owning_building_guid = 17)
      LocalObject(555, Door.Constructor(Vector3(1932f, 2932f, 30.18653f)), owning_building_guid = 17)
      LocalObject(556, Door.Constructor(Vector3(1936f, 2944f, 30.18653f)), owning_building_guid = 17)
      LocalObject(557, Door.Constructor(Vector3(1936f, 3024f, 32.68653f)), owning_building_guid = 17)
      LocalObject(558, Door.Constructor(Vector3(1940f, 2988f, 32.68653f)), owning_building_guid = 17)
      LocalObject(559, Door.Constructor(Vector3(1944f, 2992f, 22.68653f)), owning_building_guid = 17)
      LocalObject(560, Door.Constructor(Vector3(1952f, 2992f, 32.68653f)), owning_building_guid = 17)
      LocalObject(561, Door.Constructor(Vector3(1952f, 3016f, 32.68653f)), owning_building_guid = 17)
      LocalObject(562, Door.Constructor(Vector3(1956f, 2932f, 22.68653f)), owning_building_guid = 17)
      LocalObject(563, Door.Constructor(Vector3(1960f, 2960f, 22.68653f)), owning_building_guid = 17)
      LocalObject(564, Door.Constructor(Vector3(1972f, 2940f, 22.68653f)), owning_building_guid = 17)
      LocalObject(565, Door.Constructor(Vector3(1972f, 2948f, 22.68653f)), owning_building_guid = 17)
      LocalObject(783, Door.Constructor(Vector3(1899.932f, 2970.293f, 40.95853f)), owning_building_guid = 17)
      LocalObject(2360, Door.Constructor(Vector3(1917.733f, 2951.327f, 30.51953f)), owning_building_guid = 17)
      LocalObject(2361, Door.Constructor(Vector3(1925.026f, 2951.327f, 30.51953f)), owning_building_guid = 17)
      LocalObject(2362, Door.Constructor(Vector3(1932.315f, 2951.327f, 30.51953f)), owning_building_guid = 17)
      LocalObject(
        823,
        IFFLock.Constructor(Vector3(1903.13f, 2967.547f, 40.11753f), Vector3(0, 0, 180)),
        owning_building_guid = 17,
        door_guid = 783
      )
      LocalObject(
        841,
        IFFLock.Constructor(Vector3(1894.42f, 3005.953f, 45.12653f), Vector3(0, 0, 270)),
        owning_building_guid = 17,
        door_guid = 271
      )
      LocalObject(
        842,
        IFFLock.Constructor(Vector3(1894.511f, 2993.183f, 52.54653f), Vector3(0, 0, 0)),
        owning_building_guid = 17,
        door_guid = 272
      )
      LocalObject(
        843,
        IFFLock.Constructor(Vector3(1905.105f, 3010.043f, 45.12653f), Vector3(0, 0, 90)),
        owning_building_guid = 17,
        door_guid = 273
      )
      LocalObject(
        844,
        IFFLock.Constructor(Vector3(1911.19f, 2942.428f, 30.00153f), Vector3(0, 0, 270)),
        owning_building_guid = 17,
        door_guid = 544
      )
      LocalObject(
        845,
        IFFLock.Constructor(Vector3(1914.428f, 2932.94f, 22.50153f), Vector3(0, 0, 0)),
        owning_building_guid = 17,
        door_guid = 548
      )
      LocalObject(
        846,
        IFFLock.Constructor(Vector3(1921.955f, 2980.813f, 45.12653f), Vector3(0, 0, 0)),
        owning_building_guid = 17,
        door_guid = 275
      )
      LocalObject(
        847,
        IFFLock.Constructor(Vector3(1927.06f, 3006.428f, 22.50153f), Vector3(0, 0, 270)),
        owning_building_guid = 17,
        door_guid = 554
      )
      LocalObject(
        848,
        IFFLock.Constructor(Vector3(1936.81f, 2945.572f, 30.00153f), Vector3(0, 0, 90)),
        owning_building_guid = 17,
        door_guid = 556
      )
      LocalObject(
        849,
        IFFLock.Constructor(Vector3(1970.428f, 2948.813f, 22.50153f), Vector3(0, 0, 0)),
        owning_building_guid = 17,
        door_guid = 565
      )
      LocalObject(
        850,
        IFFLock.Constructor(Vector3(1973.572f, 2939.187f, 22.50153f), Vector3(0, 0, 180)),
        owning_building_guid = 17,
        door_guid = 564
      )
      LocalObject(
        851,
        IFFLock.Constructor(Vector3(1982.033f, 2955.222f, 40.11553f), Vector3(0, 0, 180)),
        owning_building_guid = 17,
        door_guid = 278
      )
      LocalObject(1061, Locker.Constructor(Vector3(1914.141f, 2936.977f, 28.92653f)), owning_building_guid = 17)
      LocalObject(1062, Locker.Constructor(Vector3(1914.141f, 2938.126f, 28.92653f)), owning_building_guid = 17)
      LocalObject(1063, Locker.Constructor(Vector3(1914.141f, 2939.273f, 28.92653f)), owning_building_guid = 17)
      LocalObject(1064, Locker.Constructor(Vector3(1914.141f, 2940.437f, 28.92653f)), owning_building_guid = 17)
      LocalObject(1065, Locker.Constructor(Vector3(1934.165f, 2917.272f, 21.16553f)), owning_building_guid = 17)
      LocalObject(1066, Locker.Constructor(Vector3(1934.165f, 2918.609f, 21.16553f)), owning_building_guid = 17)
      LocalObject(1067, Locker.Constructor(Vector3(1934.165f, 2919.945f, 21.16553f)), owning_building_guid = 17)
      LocalObject(1068, Locker.Constructor(Vector3(1934.165f, 2921.269f, 21.16553f)), owning_building_guid = 17)
      LocalObject(1069, Locker.Constructor(Vector3(1934.165f, 2925.809f, 21.16553f)), owning_building_guid = 17)
      LocalObject(1070, Locker.Constructor(Vector3(1934.165f, 2927.146f, 21.16553f)), owning_building_guid = 17)
      LocalObject(1071, Locker.Constructor(Vector3(1934.165f, 2928.482f, 21.16553f)), owning_building_guid = 17)
      LocalObject(1072, Locker.Constructor(Vector3(1934.165f, 2929.806f, 21.16553f)), owning_building_guid = 17)
      LocalObject(
        1577,
        Terminal.Constructor(Vector3(1880.907f, 2997.621f, 45.02553f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1578,
        Terminal.Constructor(Vector3(1894.43f, 2985.669f, 52.42053f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1579,
        Terminal.Constructor(Vector3(1896.547f, 2987.925f, 52.42053f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1580,
        Terminal.Constructor(Vector3(1898.825f, 2985.668f, 52.42053f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1581,
        Terminal.Constructor(Vector3(1919.408f, 2937.346f, 30.25553f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1582,
        Terminal.Constructor(Vector3(1923.139f, 2937.346f, 30.25553f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1583,
        Terminal.Constructor(Vector3(1926.928f, 2937.346f, 30.25553f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2283,
        Terminal.Constructor(Vector3(1879.969f, 2989.49f, 45.28253f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2284,
        Terminal.Constructor(Vector3(1912.591f, 3004.057f, 22.72253f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2285,
        Terminal.Constructor(Vector3(1915.243f, 2951.029f, 30.79953f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2286,
        Terminal.Constructor(Vector3(1922.535f, 2951.033f, 30.79953f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2287,
        Terminal.Constructor(Vector3(1929.823f, 2951.03f, 30.79953f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2288,
        Terminal.Constructor(Vector3(1943.942f, 2988.591f, 32.72253f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2505,
        Terminal.Constructor(Vector3(1958.774f, 2935.542f, 41.35253f), vehicle_terminal_combined),
        owning_building_guid = 17
      )
      LocalObject(
        1538,
        VehicleSpawnPad.Constructor(Vector3(1958.62f, 2949.167f, 37.19453f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 17,
        terminal_guid = 2505
      )
      LocalObject(2167, ResourceSilo.Constructor(Vector3(1834.908f, 3043.77f, 45.68253f)), owning_building_guid = 17)
      LocalObject(
        2180,
        SpawnTube.Constructor(Vector3(1916.683f, 2951.767f, 28.66553f), Vector3(0, 0, 90)),
        owning_building_guid = 17
      )
      LocalObject(
        2181,
        SpawnTube.Constructor(Vector3(1923.974f, 2951.767f, 28.66553f), Vector3(0, 0, 90)),
        owning_building_guid = 17
      )
      LocalObject(
        2182,
        SpawnTube.Constructor(Vector3(1931.262f, 2951.767f, 28.66553f), Vector3(0, 0, 90)),
        owning_building_guid = 17
      )
      LocalObject(
        1555,
        ProximityTerminal.Constructor(Vector3(1875.023f, 2987.136f, 38.66553f), medical_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1556,
        ProximityTerminal.Constructor(Vector3(1933.62f, 2923.556f, 21.16553f), medical_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1764,
        ProximityTerminal.Constructor(Vector3(1959.185f, 3006.846f, 46.90653f), pad_landing_frame),
        owning_building_guid = 17
      )
      LocalObject(
        1765,
        Terminal.Constructor(Vector3(1959.185f, 3006.846f, 46.90653f), air_rearm_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2079,
        ProximityTerminal.Constructor(Vector3(1879.67f, 2926.359f, 38.21553f), repair_silo),
        owning_building_guid = 17
      )
      LocalObject(
        2080,
        Terminal.Constructor(Vector3(1879.67f, 2926.359f, 38.21553f), ground_rearm_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2083,
        ProximityTerminal.Constructor(Vector3(1927.18f, 3049.088f, 38.21553f), repair_silo),
        owning_building_guid = 17
      )
      LocalObject(
        2084,
        Terminal.Constructor(Vector3(1927.18f, 3049.088f, 38.21553f), ground_rearm_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1440,
        FacilityTurret.Constructor(Vector3(1822.388f, 2957.135f, 47.17353f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1440, 5006)
      LocalObject(
        1441,
        FacilityTurret.Constructor(Vector3(1823.565f, 3060.446f, 47.17353f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1441, 5007)
      LocalObject(
        1443,
        FacilityTurret.Constructor(Vector3(1865.501f, 2913.99f, 47.17353f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1443, 5008)
      LocalObject(
        1444,
        FacilityTurret.Constructor(Vector3(1942.652f, 3061.576f, 47.17353f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1444, 5009)
      LocalObject(
        1445,
        FacilityTurret.Constructor(Vector3(1984.435f, 2915.159f, 47.17353f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1445, 5010)
      LocalObject(
        1446,
        FacilityTurret.Constructor(Vector3(1985.75f, 3018.451f, 47.17353f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1446, 5011)
      LocalObject(
        1941,
        Painbox.Constructor(Vector3(1940.089f, 3007.862f, 26.06753f), painbox),
        owning_building_guid = 17
      )
      LocalObject(
        1952,
        Painbox.Constructor(Vector3(1930.17f, 2941.785f, 33.11033f), painbox_continuous),
        owning_building_guid = 17
      )
      LocalObject(
        1963,
        Painbox.Constructor(Vector3(1925.073f, 3007.763f, 23.92423f), painbox_door_radius),
        owning_building_guid = 17
      )
      LocalObject(
        1974,
        Painbox.Constructor(Vector3(1910.076f, 2944.086f, 30.49153f), painbox_door_radius_continuous),
        owning_building_guid = 17
      )
      LocalObject(
        1975,
        Painbox.Constructor(Vector3(1931.827f, 2930.145f, 31.49153f), painbox_door_radius_continuous),
        owning_building_guid = 17
      )
      LocalObject(
        1976,
        Painbox.Constructor(Vector3(1937.42f, 2943.19f, 30.27323f), painbox_door_radius_continuous),
        owning_building_guid = 17
      )
      LocalObject(251, Generator.Constructor(Vector3(1943.555f, 3008.025f, 19.87153f)), owning_building_guid = 17)
      LocalObject(
        240,
        Terminal.Constructor(Vector3(1935.363f, 3007.978f, 21.16553f), gen_control),
        owning_building_guid = 17
      )
    }

    Building13()

    def Building13(): Unit = { // Name: Kisin Type: comm_station GUID: 20, MapID: 13
      LocalBuilding(
        "Kisin",
        20,
        13,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3296f, 5426f, 31.04343f),
            Vector3(0f, 0f, 360f),
            comm_station
          )
        )
      )
      LocalObject(
        185,
        CaptureTerminal.Constructor(Vector3(3340.089f, 5502.734f, 13.74343f), capture_terminal),
        owning_building_guid = 20
      )
      LocalObject(326, Door.Constructor(Vector3(3236.196f, 5382.5f, 32.79443f)), owning_building_guid = 20)
      LocalObject(327, Door.Constructor(Vector3(3236.196f, 5400.693f, 40.75843f)), owning_building_guid = 20)
      LocalObject(328, Door.Constructor(Vector3(3253.307f, 5358.197f, 40.75843f)), owning_building_guid = 20)
      LocalObject(329, Door.Constructor(Vector3(3271.5f, 5358.197f, 32.79443f)), owning_building_guid = 20)
      LocalObject(330, Door.Constructor(Vector3(3276f, 5417.231f, 37.76443f)), owning_building_guid = 20)
      LocalObject(331, Door.Constructor(Vector3(3276f, 5426.295f, 37.76443f)), owning_building_guid = 20)
      LocalObject(332, Door.Constructor(Vector3(3291.625f, 5418.59f, 45.20343f)), owning_building_guid = 20)
      LocalObject(333, Door.Constructor(Vector3(3296.5f, 5493.977f, 32.79443f)), owning_building_guid = 20)
      LocalObject(335, Door.Constructor(Vector3(3304f, 5446f, 37.76443f)), owning_building_guid = 20)
      LocalObject(336, Door.Constructor(Vector3(3314.692f, 5493.977f, 40.75743f)), owning_building_guid = 20)
      LocalObject(344, Door.Constructor(Vector3(3328f, 5502f, 32.76443f)), owning_building_guid = 20)
      LocalObject(347, Door.Constructor(Vector3(3356.201f, 5415.307f, 40.75843f)), owning_building_guid = 20)
      LocalObject(348, Door.Constructor(Vector3(3356.201f, 5433.5f, 32.79443f)), owning_building_guid = 20)
      LocalObject(585, Door.Constructor(Vector3(3260f, 5458f, 25.26443f)), owning_building_guid = 20)
      LocalObject(586, Door.Constructor(Vector3(3268f, 5474f, 25.26443f)), owning_building_guid = 20)
      LocalObject(587, Door.Constructor(Vector3(3276f, 5442f, 25.26443f)), owning_building_guid = 20)
      LocalObject(588, Door.Constructor(Vector3(3276f, 5450f, 15.26443f)), owning_building_guid = 20)
      LocalObject(589, Door.Constructor(Vector3(3280f, 5422f, 37.76443f)), owning_building_guid = 20)
      LocalObject(590, Door.Constructor(Vector3(3280f, 5438f, 32.76443f)), owning_building_guid = 20)
      LocalObject(591, Door.Constructor(Vector3(3292f, 5434f, 15.26443f)), owning_building_guid = 20)
      LocalObject(592, Door.Constructor(Vector3(3292f, 5466f, 15.26443f)), owning_building_guid = 20)
      LocalObject(593, Door.Constructor(Vector3(3292f, 5474f, 25.26443f)), owning_building_guid = 20)
      LocalObject(594, Door.Constructor(Vector3(3296f, 5430f, 25.26443f)), owning_building_guid = 20)
      LocalObject(595, Door.Constructor(Vector3(3296f, 5462f, 25.26443f)), owning_building_guid = 20)
      LocalObject(596, Door.Constructor(Vector3(3304f, 5438f, 37.76443f)), owning_building_guid = 20)
      LocalObject(597, Door.Constructor(Vector3(3304f, 5446f, 27.76443f)), owning_building_guid = 20)
      LocalObject(598, Door.Constructor(Vector3(3308f, 5434f, 32.76443f)), owning_building_guid = 20)
      LocalObject(599, Door.Constructor(Vector3(3316f, 5434f, 27.76443f)), owning_building_guid = 20)
      LocalObject(600, Door.Constructor(Vector3(3324f, 5450f, 15.26443f)), owning_building_guid = 20)
      LocalObject(601, Door.Constructor(Vector3(3324f, 5482f, 15.26443f)), owning_building_guid = 20)
      LocalObject(602, Door.Constructor(Vector3(3336f, 5494f, 15.26443f)), owning_building_guid = 20)
      LocalObject(603, Door.Constructor(Vector3(3340f, 5434f, 22.76443f)), owning_building_guid = 20)
      LocalObject(604, Door.Constructor(Vector3(3340f, 5458f, 22.76443f)), owning_building_guid = 20)
      LocalObject(605, Door.Constructor(Vector3(3344f, 5494f, 15.26443f)), owning_building_guid = 20)
      LocalObject(606, Door.Constructor(Vector3(3352f, 5438f, 15.26443f)), owning_building_guid = 20)
      LocalObject(607, Door.Constructor(Vector3(3352f, 5454f, 22.76443f)), owning_building_guid = 20)
      LocalObject(608, Door.Constructor(Vector3(3352f, 5478f, 15.26443f)), owning_building_guid = 20)
      LocalObject(785, Door.Constructor(Vector3(3313.707f, 5421.932f, 33.53643f)), owning_building_guid = 20)
      LocalObject(2380, Door.Constructor(Vector3(3332.673f, 5439.733f, 23.09743f)), owning_building_guid = 20)
      LocalObject(2381, Door.Constructor(Vector3(3332.673f, 5447.026f, 23.09743f)), owning_building_guid = 20)
      LocalObject(2382, Door.Constructor(Vector3(3332.673f, 5454.315f, 23.09743f)), owning_building_guid = 20)
      LocalObject(
        825,
        IFFLock.Constructor(Vector3(3316.453f, 5425.13f, 32.69543f), Vector3(0, 0, 90)),
        owning_building_guid = 20,
        door_guid = 785
      )
      LocalObject(
        890,
        IFFLock.Constructor(Vector3(3273.957f, 5427.105f, 37.70443f), Vector3(0, 0, 0)),
        owning_building_guid = 20,
        door_guid = 331
      )
      LocalObject(
        891,
        IFFLock.Constructor(Vector3(3277.572f, 5449.06f, 15.07943f), Vector3(0, 0, 180)),
        owning_building_guid = 20,
        door_guid = 588
      )
      LocalObject(
        892,
        IFFLock.Constructor(Vector3(3278.047f, 5416.42f, 37.70443f), Vector3(0, 0, 180)),
        owning_building_guid = 20,
        door_guid = 330
      )
      LocalObject(
        893,
        IFFLock.Constructor(Vector3(3290.817f, 5416.511f, 45.12443f), Vector3(0, 0, 270)),
        owning_building_guid = 20,
        door_guid = 332
      )
      LocalObject(
        894,
        IFFLock.Constructor(Vector3(3303.187f, 5443.955f, 37.70443f), Vector3(0, 0, 270)),
        owning_building_guid = 20,
        door_guid = 335
      )
      LocalObject(
        901,
        IFFLock.Constructor(Vector3(3328.778f, 5504.033f, 32.69343f), Vector3(0, 0, 90)),
        owning_building_guid = 20,
        door_guid = 344
      )
      LocalObject(
        902,
        IFFLock.Constructor(Vector3(3335.187f, 5492.428f, 15.07943f), Vector3(0, 0, 270)),
        owning_building_guid = 20,
        door_guid = 602
      )
      LocalObject(
        903,
        IFFLock.Constructor(Vector3(3338.428f, 5458.81f, 22.57943f), Vector3(0, 0, 0)),
        owning_building_guid = 20,
        door_guid = 604
      )
      LocalObject(
        904,
        IFFLock.Constructor(Vector3(3341.572f, 5433.19f, 22.57943f), Vector3(0, 0, 180)),
        owning_building_guid = 20,
        door_guid = 603
      )
      LocalObject(
        905,
        IFFLock.Constructor(Vector3(3344.813f, 5495.572f, 15.07943f), Vector3(0, 0, 90)),
        owning_building_guid = 20,
        door_guid = 605
      )
      LocalObject(
        906,
        IFFLock.Constructor(Vector3(3351.06f, 5436.428f, 15.07943f), Vector3(0, 0, 270)),
        owning_building_guid = 20,
        door_guid = 606
      )
      LocalObject(1141, Locker.Constructor(Vector3(3343.563f, 5436.141f, 21.50443f)), owning_building_guid = 20)
      LocalObject(1142, Locker.Constructor(Vector3(3344.727f, 5436.141f, 21.50443f)), owning_building_guid = 20)
      LocalObject(1143, Locker.Constructor(Vector3(3345.874f, 5436.141f, 21.50443f)), owning_building_guid = 20)
      LocalObject(1144, Locker.Constructor(Vector3(3347.023f, 5436.141f, 21.50443f)), owning_building_guid = 20)
      LocalObject(1145, Locker.Constructor(Vector3(3354.194f, 5456.165f, 13.74343f)), owning_building_guid = 20)
      LocalObject(1146, Locker.Constructor(Vector3(3355.518f, 5456.165f, 13.74343f)), owning_building_guid = 20)
      LocalObject(1147, Locker.Constructor(Vector3(3356.854f, 5456.165f, 13.74343f)), owning_building_guid = 20)
      LocalObject(1148, Locker.Constructor(Vector3(3358.191f, 5456.165f, 13.74343f)), owning_building_guid = 20)
      LocalObject(1149, Locker.Constructor(Vector3(3362.731f, 5456.165f, 13.74343f)), owning_building_guid = 20)
      LocalObject(1150, Locker.Constructor(Vector3(3364.055f, 5456.165f, 13.74343f)), owning_building_guid = 20)
      LocalObject(1151, Locker.Constructor(Vector3(3365.391f, 5456.165f, 13.74343f)), owning_building_guid = 20)
      LocalObject(1152, Locker.Constructor(Vector3(3366.728f, 5456.165f, 13.74343f)), owning_building_guid = 20)
      LocalObject(
        1606,
        Terminal.Constructor(Vector3(3286.379f, 5402.907f, 37.60343f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1607,
        Terminal.Constructor(Vector3(3296.075f, 5418.547f, 44.99843f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1608,
        Terminal.Constructor(Vector3(3298.331f, 5416.43f, 44.99843f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1609,
        Terminal.Constructor(Vector3(3298.332f, 5420.825f, 44.99843f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1613,
        Terminal.Constructor(Vector3(3346.654f, 5441.408f, 22.83343f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1614,
        Terminal.Constructor(Vector3(3346.654f, 5445.139f, 22.83343f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1615,
        Terminal.Constructor(Vector3(3346.654f, 5448.928f, 22.83343f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2296,
        Terminal.Constructor(Vector3(3279.943f, 5434.591f, 15.30043f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2297,
        Terminal.Constructor(Vector3(3294.51f, 5401.969f, 37.86043f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2298,
        Terminal.Constructor(Vector3(3295.409f, 5465.942f, 25.30043f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2299,
        Terminal.Constructor(Vector3(3332.971f, 5437.243f, 23.37743f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2300,
        Terminal.Constructor(Vector3(3332.967f, 5444.535f, 23.37743f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2301,
        Terminal.Constructor(Vector3(3332.97f, 5451.823f, 23.37743f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2507,
        Terminal.Constructor(Vector3(3348.458f, 5480.774f, 33.93043f), vehicle_terminal_combined),
        owning_building_guid = 20
      )
      LocalObject(
        1542,
        VehicleSpawnPad.Constructor(Vector3(3334.833f, 5480.62f, 29.77243f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 20,
        terminal_guid = 2507
      )
      LocalObject(2169, ResourceSilo.Constructor(Vector3(3240.23f, 5356.908f, 38.26043f)), owning_building_guid = 20)
      LocalObject(
        2200,
        SpawnTube.Constructor(Vector3(3332.233f, 5438.683f, 21.24343f), Vector3(0, 0, 0)),
        owning_building_guid = 20
      )
      LocalObject(
        2201,
        SpawnTube.Constructor(Vector3(3332.233f, 5445.974f, 21.24343f), Vector3(0, 0, 0)),
        owning_building_guid = 20
      )
      LocalObject(
        2202,
        SpawnTube.Constructor(Vector3(3332.233f, 5453.262f, 21.24343f), Vector3(0, 0, 0)),
        owning_building_guid = 20
      )
      LocalObject(
        1559,
        ProximityTerminal.Constructor(Vector3(3296.864f, 5397.023f, 31.24343f), medical_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1560,
        ProximityTerminal.Constructor(Vector3(3360.444f, 5455.62f, 13.74343f), medical_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1785,
        ProximityTerminal.Constructor(Vector3(3277.154f, 5481.185f, 39.48443f), pad_landing_frame),
        owning_building_guid = 20
      )
      LocalObject(
        1786,
        Terminal.Constructor(Vector3(3277.154f, 5481.185f, 39.48443f), air_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2095,
        ProximityTerminal.Constructor(Vector3(3234.912f, 5449.18f, 30.79343f), repair_silo),
        owning_building_guid = 20
      )
      LocalObject(
        2096,
        Terminal.Constructor(Vector3(3234.912f, 5449.18f, 30.79343f), ground_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2103,
        ProximityTerminal.Constructor(Vector3(3357.641f, 5401.67f, 30.79343f), repair_silo),
        owning_building_guid = 20
      )
      LocalObject(
        2104,
        Terminal.Constructor(Vector3(3357.641f, 5401.67f, 30.79343f), ground_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1459,
        FacilityTurret.Constructor(Vector3(3222.424f, 5464.652f, 39.75143f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1459, 5012)
      LocalObject(
        1460,
        FacilityTurret.Constructor(Vector3(3223.554f, 5345.565f, 39.75143f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1460, 5013)
      LocalObject(
        1462,
        FacilityTurret.Constructor(Vector3(3265.549f, 5507.75f, 39.75143f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1462, 5014)
      LocalObject(
        1464,
        FacilityTurret.Constructor(Vector3(3326.865f, 5344.388f, 39.75143f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1464, 5015)
      LocalObject(
        1465,
        FacilityTurret.Constructor(Vector3(3368.841f, 5506.435f, 39.75143f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1465, 5016)
      LocalObject(
        1466,
        FacilityTurret.Constructor(Vector3(3370.01f, 5387.501f, 39.75143f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1466, 5017)
      LocalObject(
        1943,
        Painbox.Constructor(Vector3(3276.138f, 5462.089f, 18.64543f), painbox),
        owning_building_guid = 20
      )
      LocalObject(
        1954,
        Painbox.Constructor(Vector3(3342.215f, 5452.17f, 25.68823f), painbox_continuous),
        owning_building_guid = 20
      )
      LocalObject(
        1965,
        Painbox.Constructor(Vector3(3276.237f, 5447.073f, 16.50213f), painbox_door_radius),
        owning_building_guid = 20
      )
      LocalObject(
        1980,
        Painbox.Constructor(Vector3(3339.914f, 5432.076f, 23.06943f), painbox_door_radius_continuous),
        owning_building_guid = 20
      )
      LocalObject(
        1981,
        Painbox.Constructor(Vector3(3340.81f, 5459.42f, 22.85113f), painbox_door_radius_continuous),
        owning_building_guid = 20
      )
      LocalObject(
        1982,
        Painbox.Constructor(Vector3(3353.855f, 5453.827f, 24.06943f), painbox_door_radius_continuous),
        owning_building_guid = 20
      )
      LocalObject(253, Generator.Constructor(Vector3(3275.975f, 5465.555f, 12.44943f)), owning_building_guid = 20)
      LocalObject(
        242,
        Terminal.Constructor(Vector3(3276.022f, 5457.363f, 13.74343f), gen_control),
        owning_building_guid = 20
      )
    }

    Building5()

    def Building5(): Unit = { // Name: Voltan Type: comm_station GUID: 23, MapID: 5
      LocalBuilding(
        "Voltan",
        23,
        5,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4484f, 3482f, 28.31507f),
            Vector3(0f, 0f, 360f),
            comm_station
          )
        )
      )
      LocalObject(
        190,
        CaptureTerminal.Constructor(Vector3(4528.089f, 3558.734f, 11.01507f), capture_terminal),
        owning_building_guid = 23
      )
      LocalObject(418, Door.Constructor(Vector3(4424.196f, 3438.5f, 30.06607f)), owning_building_guid = 23)
      LocalObject(419, Door.Constructor(Vector3(4424.196f, 3456.693f, 38.03008f)), owning_building_guid = 23)
      LocalObject(421, Door.Constructor(Vector3(4441.307f, 3414.197f, 38.03008f)), owning_building_guid = 23)
      LocalObject(423, Door.Constructor(Vector3(4459.5f, 3414.197f, 30.06607f)), owning_building_guid = 23)
      LocalObject(425, Door.Constructor(Vector3(4464f, 3473.231f, 35.03607f)), owning_building_guid = 23)
      LocalObject(426, Door.Constructor(Vector3(4464f, 3482.295f, 35.03607f)), owning_building_guid = 23)
      LocalObject(427, Door.Constructor(Vector3(4479.625f, 3474.59f, 42.47507f)), owning_building_guid = 23)
      LocalObject(429, Door.Constructor(Vector3(4484.5f, 3549.977f, 30.06607f)), owning_building_guid = 23)
      LocalObject(431, Door.Constructor(Vector3(4492f, 3502f, 35.03607f)), owning_building_guid = 23)
      LocalObject(436, Door.Constructor(Vector3(4502.692f, 3549.977f, 38.02907f)), owning_building_guid = 23)
      LocalObject(445, Door.Constructor(Vector3(4516f, 3558f, 30.03607f)), owning_building_guid = 23)
      LocalObject(448, Door.Constructor(Vector3(4544.201f, 3471.307f, 38.03008f)), owning_building_guid = 23)
      LocalObject(449, Door.Constructor(Vector3(4544.201f, 3489.5f, 30.06607f)), owning_building_guid = 23)
      LocalObject(679, Door.Constructor(Vector3(4448f, 3514f, 22.53607f)), owning_building_guid = 23)
      LocalObject(681, Door.Constructor(Vector3(4456f, 3530f, 22.53607f)), owning_building_guid = 23)
      LocalObject(687, Door.Constructor(Vector3(4464f, 3498f, 22.53607f)), owning_building_guid = 23)
      LocalObject(688, Door.Constructor(Vector3(4464f, 3506f, 12.53607f)), owning_building_guid = 23)
      LocalObject(692, Door.Constructor(Vector3(4468f, 3478f, 35.03607f)), owning_building_guid = 23)
      LocalObject(693, Door.Constructor(Vector3(4468f, 3494f, 30.03607f)), owning_building_guid = 23)
      LocalObject(697, Door.Constructor(Vector3(4480f, 3490f, 12.53607f)), owning_building_guid = 23)
      LocalObject(698, Door.Constructor(Vector3(4480f, 3522f, 12.53607f)), owning_building_guid = 23)
      LocalObject(699, Door.Constructor(Vector3(4480f, 3530f, 22.53607f)), owning_building_guid = 23)
      LocalObject(701, Door.Constructor(Vector3(4484f, 3486f, 22.53607f)), owning_building_guid = 23)
      LocalObject(702, Door.Constructor(Vector3(4484f, 3518f, 22.53607f)), owning_building_guid = 23)
      LocalObject(705, Door.Constructor(Vector3(4492f, 3494f, 35.03607f)), owning_building_guid = 23)
      LocalObject(706, Door.Constructor(Vector3(4492f, 3502f, 25.03607f)), owning_building_guid = 23)
      LocalObject(707, Door.Constructor(Vector3(4496f, 3490f, 30.03607f)), owning_building_guid = 23)
      LocalObject(710, Door.Constructor(Vector3(4504f, 3490f, 25.03607f)), owning_building_guid = 23)
      LocalObject(711, Door.Constructor(Vector3(4512f, 3506f, 12.53607f)), owning_building_guid = 23)
      LocalObject(712, Door.Constructor(Vector3(4512f, 3538f, 12.53607f)), owning_building_guid = 23)
      LocalObject(713, Door.Constructor(Vector3(4524f, 3550f, 12.53607f)), owning_building_guid = 23)
      LocalObject(714, Door.Constructor(Vector3(4528f, 3490f, 20.03607f)), owning_building_guid = 23)
      LocalObject(715, Door.Constructor(Vector3(4528f, 3514f, 20.03607f)), owning_building_guid = 23)
      LocalObject(716, Door.Constructor(Vector3(4532f, 3550f, 12.53607f)), owning_building_guid = 23)
      LocalObject(717, Door.Constructor(Vector3(4540f, 3494f, 12.53607f)), owning_building_guid = 23)
      LocalObject(718, Door.Constructor(Vector3(4540f, 3510f, 20.03607f)), owning_building_guid = 23)
      LocalObject(719, Door.Constructor(Vector3(4540f, 3534f, 12.53607f)), owning_building_guid = 23)
      LocalObject(790, Door.Constructor(Vector3(4501.707f, 3477.932f, 30.80807f)), owning_building_guid = 23)
      LocalObject(2411, Door.Constructor(Vector3(4520.673f, 3495.733f, 20.36907f)), owning_building_guid = 23)
      LocalObject(2412, Door.Constructor(Vector3(4520.673f, 3503.026f, 20.36907f)), owning_building_guid = 23)
      LocalObject(2413, Door.Constructor(Vector3(4520.673f, 3510.315f, 20.36907f)), owning_building_guid = 23)
      LocalObject(
        830,
        IFFLock.Constructor(Vector3(4504.453f, 3481.13f, 29.96707f), Vector3(0, 0, 90)),
        owning_building_guid = 23,
        door_guid = 790
      )
      LocalObject(
        960,
        IFFLock.Constructor(Vector3(4461.957f, 3483.105f, 34.97607f), Vector3(0, 0, 0)),
        owning_building_guid = 23,
        door_guid = 426
      )
      LocalObject(
        962,
        IFFLock.Constructor(Vector3(4465.572f, 3505.06f, 12.35107f), Vector3(0, 0, 180)),
        owning_building_guid = 23,
        door_guid = 688
      )
      LocalObject(
        963,
        IFFLock.Constructor(Vector3(4466.047f, 3472.42f, 34.97607f), Vector3(0, 0, 180)),
        owning_building_guid = 23,
        door_guid = 425
      )
      LocalObject(
        964,
        IFFLock.Constructor(Vector3(4478.817f, 3472.511f, 42.39607f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 427
      )
      LocalObject(
        969,
        IFFLock.Constructor(Vector3(4491.187f, 3499.955f, 34.97607f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 431
      )
      LocalObject(
        981,
        IFFLock.Constructor(Vector3(4516.778f, 3560.033f, 29.96507f), Vector3(0, 0, 90)),
        owning_building_guid = 23,
        door_guid = 445
      )
      LocalObject(
        982,
        IFFLock.Constructor(Vector3(4523.187f, 3548.428f, 12.35107f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 713
      )
      LocalObject(
        983,
        IFFLock.Constructor(Vector3(4526.428f, 3514.81f, 19.85107f), Vector3(0, 0, 0)),
        owning_building_guid = 23,
        door_guid = 715
      )
      LocalObject(
        984,
        IFFLock.Constructor(Vector3(4529.572f, 3489.19f, 19.85107f), Vector3(0, 0, 180)),
        owning_building_guid = 23,
        door_guid = 714
      )
      LocalObject(
        985,
        IFFLock.Constructor(Vector3(4532.813f, 3551.572f, 12.35107f), Vector3(0, 0, 90)),
        owning_building_guid = 23,
        door_guid = 716
      )
      LocalObject(
        986,
        IFFLock.Constructor(Vector3(4539.06f, 3492.428f, 12.35107f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 717
      )
      LocalObject(1283, Locker.Constructor(Vector3(4531.563f, 3492.141f, 18.77607f)), owning_building_guid = 23)
      LocalObject(1284, Locker.Constructor(Vector3(4532.727f, 3492.141f, 18.77607f)), owning_building_guid = 23)
      LocalObject(1285, Locker.Constructor(Vector3(4533.874f, 3492.141f, 18.77607f)), owning_building_guid = 23)
      LocalObject(1286, Locker.Constructor(Vector3(4535.023f, 3492.141f, 18.77607f)), owning_building_guid = 23)
      LocalObject(1287, Locker.Constructor(Vector3(4542.194f, 3512.165f, 11.01507f)), owning_building_guid = 23)
      LocalObject(1288, Locker.Constructor(Vector3(4543.518f, 3512.165f, 11.01507f)), owning_building_guid = 23)
      LocalObject(1289, Locker.Constructor(Vector3(4544.854f, 3512.165f, 11.01507f)), owning_building_guid = 23)
      LocalObject(1290, Locker.Constructor(Vector3(4546.191f, 3512.165f, 11.01507f)), owning_building_guid = 23)
      LocalObject(1291, Locker.Constructor(Vector3(4550.731f, 3512.165f, 11.01507f)), owning_building_guid = 23)
      LocalObject(1292, Locker.Constructor(Vector3(4552.055f, 3512.165f, 11.01507f)), owning_building_guid = 23)
      LocalObject(1293, Locker.Constructor(Vector3(4553.391f, 3512.165f, 11.01507f)), owning_building_guid = 23)
      LocalObject(1294, Locker.Constructor(Vector3(4554.728f, 3512.165f, 11.01507f)), owning_building_guid = 23)
      LocalObject(
        1649,
        Terminal.Constructor(Vector3(4474.379f, 3458.907f, 34.87507f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1650,
        Terminal.Constructor(Vector3(4484.075f, 3474.547f, 42.27007f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1651,
        Terminal.Constructor(Vector3(4486.331f, 3472.43f, 42.27007f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1652,
        Terminal.Constructor(Vector3(4486.332f, 3476.825f, 42.27007f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1662,
        Terminal.Constructor(Vector3(4534.654f, 3497.408f, 20.10507f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1663,
        Terminal.Constructor(Vector3(4534.654f, 3501.139f, 20.10507f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1664,
        Terminal.Constructor(Vector3(4534.654f, 3504.928f, 20.10507f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2324,
        Terminal.Constructor(Vector3(4467.943f, 3490.591f, 12.57207f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2327,
        Terminal.Constructor(Vector3(4482.51f, 3457.969f, 35.13207f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2328,
        Terminal.Constructor(Vector3(4483.409f, 3521.942f, 22.57207f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2332,
        Terminal.Constructor(Vector3(4520.971f, 3493.243f, 20.64907f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2333,
        Terminal.Constructor(Vector3(4520.967f, 3500.535f, 20.64907f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2334,
        Terminal.Constructor(Vector3(4520.97f, 3507.823f, 20.64907f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2512,
        Terminal.Constructor(Vector3(4536.458f, 3536.774f, 31.20207f), vehicle_terminal_combined),
        owning_building_guid = 23
      )
      LocalObject(
        1549,
        VehicleSpawnPad.Constructor(Vector3(4522.833f, 3536.62f, 27.04407f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 23,
        terminal_guid = 2512
      )
      LocalObject(2174, ResourceSilo.Constructor(Vector3(4428.23f, 3412.908f, 35.53207f)), owning_building_guid = 23)
      LocalObject(
        2231,
        SpawnTube.Constructor(Vector3(4520.233f, 3494.683f, 18.51507f), Vector3(0, 0, 0)),
        owning_building_guid = 23
      )
      LocalObject(
        2232,
        SpawnTube.Constructor(Vector3(4520.233f, 3501.974f, 18.51507f), Vector3(0, 0, 0)),
        owning_building_guid = 23
      )
      LocalObject(
        2233,
        SpawnTube.Constructor(Vector3(4520.233f, 3509.262f, 18.51507f), Vector3(0, 0, 0)),
        owning_building_guid = 23
      )
      LocalObject(
        1566,
        ProximityTerminal.Constructor(Vector3(4484.864f, 3453.023f, 28.51507f), medical_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1568,
        ProximityTerminal.Constructor(Vector3(4548.444f, 3511.62f, 11.01507f), medical_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1836,
        ProximityTerminal.Constructor(Vector3(4465.154f, 3537.185f, 36.75607f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        1837,
        Terminal.Constructor(Vector3(4465.154f, 3537.185f, 36.75607f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2127,
        ProximityTerminal.Constructor(Vector3(4422.912f, 3505.18f, 28.06507f), repair_silo),
        owning_building_guid = 23
      )
      LocalObject(
        2128,
        Terminal.Constructor(Vector3(4422.912f, 3505.18f, 28.06507f), ground_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2139,
        ProximityTerminal.Constructor(Vector3(4545.641f, 3457.67f, 28.06507f), repair_silo),
        owning_building_guid = 23
      )
      LocalObject(
        2140,
        Terminal.Constructor(Vector3(4545.641f, 3457.67f, 28.06507f), ground_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1496,
        FacilityTurret.Constructor(Vector3(4410.424f, 3520.652f, 37.02307f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1496, 5018)
      LocalObject(
        1497,
        FacilityTurret.Constructor(Vector3(4411.554f, 3401.565f, 37.02307f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1497, 5019)
      LocalObject(
        1498,
        FacilityTurret.Constructor(Vector3(4453.549f, 3563.75f, 37.02307f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1498, 5020)
      LocalObject(
        1502,
        FacilityTurret.Constructor(Vector3(4514.865f, 3400.388f, 37.02307f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1502, 5021)
      LocalObject(
        1505,
        FacilityTurret.Constructor(Vector3(4556.841f, 3562.435f, 37.02307f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1505, 5022)
      LocalObject(
        1506,
        FacilityTurret.Constructor(Vector3(4558.01f, 3443.501f, 37.02307f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1506, 5023)
      LocalObject(
        1947,
        Painbox.Constructor(Vector3(4464.138f, 3518.089f, 15.91707f), painbox),
        owning_building_guid = 23
      )
      LocalObject(
        1959,
        Painbox.Constructor(Vector3(4530.215f, 3508.17f, 22.95987f), painbox_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        1969,
        Painbox.Constructor(Vector3(4464.237f, 3503.073f, 13.77377f), painbox_door_radius),
        owning_building_guid = 23
      )
      LocalObject(
        1995,
        Painbox.Constructor(Vector3(4527.914f, 3488.076f, 20.34107f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        1996,
        Painbox.Constructor(Vector3(4528.81f, 3515.42f, 20.12277f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        1997,
        Painbox.Constructor(Vector3(4541.855f, 3509.827f, 21.34107f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(257, Generator.Constructor(Vector3(4463.975f, 3521.555f, 9.721073f)), owning_building_guid = 23)
      LocalObject(
        246,
        Terminal.Constructor(Vector3(4464.022f, 3513.363f, 11.01507f), gen_control),
        owning_building_guid = 23
      )
    }

    Building6()

    def Building6(): Unit = { // Name: Naum Type: comm_station_dsp GUID: 26, MapID: 6
      LocalBuilding(
        "Naum",
        26,
        6,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5408f, 3536f, 27.71691f),
            Vector3(0f, 0f, 315f),
            comm_station_dsp
          )
        )
      )
      LocalObject(
        191,
        CaptureTerminal.Constructor(Vector3(5448.18f, 3468.574f, 10.31691f), capture_terminal),
        owning_building_guid = 26
      )
      LocalObject(234, Door.Constructor(Vector3(5506.148f, 3537.503f, 31.09491f)), owning_building_guid = 26)
      LocalObject(471, Door.Constructor(Vector3(5329.868f, 3518.245f, 37.33191f)), owning_building_guid = 26)
      LocalObject(472, Door.Constructor(Vector3(5334.954f, 3547.529f, 29.36791f)), owning_building_guid = 26)
      LocalObject(473, Door.Constructor(Vector3(5342.731f, 3505.381f, 29.36791f)), owning_building_guid = 26)
      LocalObject(478, Door.Constructor(Vector3(5347.817f, 3560.393f, 37.33191f)), owning_building_guid = 26)
      LocalObject(480, Door.Constructor(Vector3(5387.657f, 3543.941f, 34.33791f)), owning_building_guid = 26)
      LocalObject(481, Door.Constructor(Vector3(5394.066f, 3550.351f, 34.33791f)), owning_building_guid = 26)
      LocalObject(482, Door.Constructor(Vector3(5399.667f, 3533.854f, 41.77691f)), owning_building_guid = 26)
      LocalObject(483, Door.Constructor(Vector3(5427.799f, 3544.485f, 34.33791f)), owning_building_guid = 26)
      LocalObject(485, Door.Constructor(Vector3(5455.613f, 3470.576f, 37.33191f)), owning_building_guid = 26)
      LocalObject(486, Door.Constructor(Vector3(5473.806f, 3470.576f, 29.36791f)), owning_building_guid = 26)
      LocalObject(487, Door.Constructor(Vector3(5487.706f, 3625.94f, 29.36791f)), owning_building_guid = 26)
      LocalObject(488, Door.Constructor(Vector3(5500.12f, 3549.027f, 29.33991f)), owning_building_guid = 26)
      LocalObject(489, Door.Constructor(Vector3(5505.899f, 3625.941f, 37.33091f)), owning_building_guid = 26)
      LocalObject(490, Door.Constructor(Vector3(5519.94f, 3571.64f, 34.33391f)), owning_building_guid = 26)
      LocalObject(492, Door.Constructor(Vector3(5528.198f, 3504.909f, 37.33091f)), owning_building_guid = 26)
      LocalObject(493, Door.Constructor(Vector3(5541.061f, 3517.773f, 29.36791f)), owning_building_guid = 26)
      LocalObject(494, Door.Constructor(Vector3(5546.593f, 3510.544f, 29.33791f)), owning_building_guid = 26)
      LocalObject(495, Door.Constructor(Vector3(5557.959f, 3618.471f, 29.36791f)), owning_building_guid = 26)
      LocalObject(497, Door.Constructor(Vector3(5570.823f, 3605.607f, 37.33091f)), owning_building_guid = 26)
      LocalObject(720, Door.Constructor(Vector3(5393.858f, 3544.485f, 34.33791f)), owning_building_guid = 26)
      LocalObject(721, Door.Constructor(Vector3(5405.171f, 3555.799f, 29.33791f)), owning_building_guid = 26)
      LocalObject(722, Door.Constructor(Vector3(5422.142f, 3533.172f, 29.33791f)), owning_building_guid = 26)
      LocalObject(723, Door.Constructor(Vector3(5422.142f, 3538.828f, 34.33791f)), owning_building_guid = 26)
      LocalObject(724, Door.Constructor(Vector3(5427.799f, 3527.515f, 24.33791f)), owning_building_guid = 26)
      LocalObject(725, Door.Constructor(Vector3(5427.799f, 3544.485f, 24.33791f)), owning_building_guid = 26)
      LocalObject(726, Door.Constructor(Vector3(5433.456f, 3499.23f, 19.33791f)), owning_building_guid = 26)
      LocalObject(727, Door.Constructor(Vector3(5433.456f, 3504.887f, 11.83791f)), owning_building_guid = 26)
      LocalObject(728, Door.Constructor(Vector3(5439.113f, 3465.289f, 11.83791f)), owning_building_guid = 26)
      LocalObject(729, Door.Constructor(Vector3(5444.77f, 3459.633f, 11.83791f)), owning_building_guid = 26)
      LocalObject(730, Door.Constructor(Vector3(5444.77f, 3510.544f, 19.33791f)), owning_building_guid = 26)
      LocalObject(731, Door.Constructor(Vector3(5450.426f, 3538.828f, 11.83791f)), owning_building_guid = 26)
      LocalObject(732, Door.Constructor(Vector3(5456.083f, 3465.289f, 11.83791f)), owning_building_guid = 26)
      LocalObject(733, Door.Constructor(Vector3(5456.083f, 3504.887f, 11.83791f)), owning_building_guid = 26)
      LocalObject(734, Door.Constructor(Vector3(5456.083f, 3550.142f, 19.33791f)), owning_building_guid = 26)
      LocalObject(735, Door.Constructor(Vector3(5461.74f, 3527.515f, 19.33791f)), owning_building_guid = 26)
      LocalObject(736, Door.Constructor(Vector3(5467.397f, 3516.201f, 19.33791f)), owning_building_guid = 26)
      LocalObject(737, Door.Constructor(Vector3(5473.054f, 3476.603f, 19.33791f)), owning_building_guid = 26)
      LocalObject(738, Door.Constructor(Vector3(5478.71f, 3550.142f, 11.83791f)), owning_building_guid = 26)
      LocalObject(739, Door.Constructor(Vector3(5495.681f, 3499.23f, 19.33791f)), owning_building_guid = 26)
      LocalObject(740, Door.Constructor(Vector3(5517.087f, 3560.346f, 34.33991f)), owning_building_guid = 26)
      LocalObject(791, Door.Constructor(Vector3(5417.637f, 3520.596f, 30.10891f)), owning_building_guid = 26)
      LocalObject(2424, Door.Constructor(Vector3(5443.643f, 3519.779f, 19.67091f)), owning_building_guid = 26)
      LocalObject(2425, Door.Constructor(Vector3(5448.799f, 3524.936f, 19.67091f)), owning_building_guid = 26)
      LocalObject(2426, Door.Constructor(Vector3(5453.954f, 3530.09f, 19.67091f)), owning_building_guid = 26)
      LocalObject(
        831,
        IFFLock.Constructor(Vector3(5421.82f, 3520.893f, 29.28491f), Vector3(0, 0, 135)),
        owning_building_guid = 26,
        door_guid = 791
      )
      LocalObject(
        1009,
        IFFLock.Constructor(Vector3(5388.526f, 3541.926f, 34.28491f), Vector3(0, 0, 225)),
        owning_building_guid = 26,
        door_guid = 480
      )
      LocalObject(
        1010,
        IFFLock.Constructor(Vector3(5393.195f, 3552.366f, 34.28491f), Vector3(0, 0, 45)),
        owning_building_guid = 26,
        door_guid = 481
      )
      LocalObject(
        1011,
        IFFLock.Constructor(Vector3(5397.627f, 3532.957f, 41.78491f), Vector3(0, 0, 315)),
        owning_building_guid = 26,
        door_guid = 482
      )
      LocalObject(
        1012,
        IFFLock.Constructor(Vector3(5425.787f, 3543.615f, 34.28491f), Vector3(0, 0, 315)),
        owning_building_guid = 26,
        door_guid = 483
      )
      LocalObject(
        1013,
        IFFLock.Constructor(Vector3(5437.336f, 3464.843f, 11.65291f), Vector3(0, 0, 315)),
        owning_building_guid = 26,
        door_guid = 728
      )
      LocalObject(
        1015,
        IFFLock.Constructor(Vector3(5445.309f, 3508.86f, 19.15291f), Vector3(0, 0, 225)),
        owning_building_guid = 26,
        door_guid = 730
      )
      LocalObject(
        1016,
        IFFLock.Constructor(Vector3(5446.456f, 3460.169f, 11.65291f), Vector3(0, 0, 135)),
        owning_building_guid = 26,
        door_guid = 729
      )
      LocalObject(
        1017,
        IFFLock.Constructor(Vector3(5454.307f, 3504.44f, 11.65291f), Vector3(0, 0, 315)),
        owning_building_guid = 26,
        door_guid = 733
      )
      LocalObject(
        1018,
        IFFLock.Constructor(Vector3(5461.293f, 3529.291f, 19.15291f), Vector3(0, 0, 45)),
        owning_building_guid = 26,
        door_guid = 735
      )
      LocalObject(
        1019,
        IFFLock.Constructor(Vector3(5480.487f, 3550.589f, 11.65291f), Vector3(0, 0, 135)),
        owning_building_guid = 26,
        door_guid = 738
      )
      LocalObject(
        1020,
        IFFLock.Constructor(Vector3(5498.111f, 3548.154f, 29.32891f), Vector3(0, 0, 315)),
        owning_building_guid = 26,
        door_guid = 488
      )
      LocalObject(
        1021,
        IFFLock.Constructor(Vector3(5519.065f, 3573.658f, 34.26391f), Vector3(0, 0, 45)),
        owning_building_guid = 26,
        door_guid = 490
      )
      LocalObject(
        1022,
        IFFLock.Constructor(Vector3(5545.717f, 3512.563f, 29.22791f), Vector3(0, 0, 45)),
        owning_building_guid = 26,
        door_guid = 494
      )
      LocalObject(1335, Locker.Constructor(Vector3(5448.803f, 3509.539f, 18.07791f)), owning_building_guid = 26)
      LocalObject(1336, Locker.Constructor(Vector3(5449.626f, 3508.716f, 18.07791f)), owning_building_guid = 26)
      LocalObject(1337, Locker.Constructor(Vector3(5450.437f, 3507.905f, 18.07791f)), owning_building_guid = 26)
      LocalObject(1338, Locker.Constructor(Vector3(5451.25f, 3507.092f, 18.07791f)), owning_building_guid = 26)
      LocalObject(1339, Locker.Constructor(Vector3(5470.479f, 3516.18f, 10.31691f)), owning_building_guid = 26)
      LocalObject(1340, Locker.Constructor(Vector3(5471.416f, 3515.244f, 10.31691f)), owning_building_guid = 26)
      LocalObject(1341, Locker.Constructor(Vector3(5472.36f, 3514.3f, 10.31691f)), owning_building_guid = 26)
      LocalObject(1342, Locker.Constructor(Vector3(5473.306f, 3513.354f, 10.31691f)), owning_building_guid = 26)
      LocalObject(1343, Locker.Constructor(Vector3(5476.516f, 3510.144f, 10.31691f)), owning_building_guid = 26)
      LocalObject(1344, Locker.Constructor(Vector3(5477.452f, 3509.208f, 10.31691f)), owning_building_guid = 26)
      LocalObject(1345, Locker.Constructor(Vector3(5478.396f, 3508.263f, 10.31691f)), owning_building_guid = 26)
      LocalObject(1346, Locker.Constructor(Vector3(5479.342f, 3507.318f, 10.31691f)), owning_building_guid = 26)
      LocalObject(
        236,
        Terminal.Constructor(Vector3(5523.115f, 3566.433f, 33.42091f), dropship_vehicle_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        235,
        VehicleSpawnPad.Constructor(Vector3(5544.602f, 3575.971f, 27.74491f), dropship_pad_doors, Vector3(0, 0, 135)),
        owning_building_guid = 26,
        terminal_guid = 236
      )
      LocalObject(
        1680,
        Terminal.Constructor(Vector3(5384.86f, 3526.468f, 34.17691f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1681,
        Terminal.Constructor(Vector3(5402.783f, 3530.677f, 41.57191f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1682,
        Terminal.Constructor(Vector3(5402.881f, 3527.585f, 41.57191f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1683,
        Terminal.Constructor(Vector3(5405.99f, 3530.692f, 41.57191f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1684,
        Terminal.Constructor(Vector3(5406.007f, 3527.513f, 41.57191f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1685,
        Terminal.Constructor(Vector3(5454.713f, 3511.077f, 19.40691f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1686,
        Terminal.Constructor(Vector3(5457.351f, 3513.716f, 19.40691f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1687,
        Terminal.Constructor(Vector3(5460.03f, 3516.395f, 19.40691f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2335,
        Terminal.Constructor(Vector3(5389.946f, 3520.055f, 34.43391f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2336,
        Terminal.Constructor(Vector3(5442.092f, 3517.808f, 19.95091f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2337,
        Terminal.Constructor(Vector3(5447.246f, 3522.967f, 19.95091f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2338,
        Terminal.Constructor(Vector3(5452.401f, 3528.118f, 19.95091f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2339,
        Terminal.Constructor(Vector3(5452.878f, 3490.286f, 11.84491f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2340,
        Terminal.Constructor(Vector3(5469.849f, 3484.629f, 19.37391f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2341,
        Terminal.Constructor(Vector3(5498.051f, 3525.063f, 11.84491f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2342,
        Terminal.Constructor(Vector3(5498.133f, 3512.914f, 19.37391f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2343,
        Terminal.Constructor(Vector3(5514.072f, 3564.145f, 34.36491f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2513,
        Terminal.Constructor(Vector3(5496.913f, 3599.884f, 30.50391f), ground_vehicle_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1550,
        VehicleSpawnPad.Constructor(Vector3(5487.335f, 3590.182f, 26.34591f), mb_pad_creation, Vector3(0, 0, 225)),
        owning_building_guid = 26,
        terminal_guid = 2513
      )
      LocalObject(2175, ResourceSilo.Constructor(Vector3(5594.573f, 3583.68f, 34.83391f)), owning_building_guid = 26)
      LocalObject(
        2244,
        SpawnTube.Constructor(Vector3(5442.589f, 3519.348f, 17.81691f), Vector3(0, 0, 45)),
        owning_building_guid = 26
      )
      LocalObject(
        2245,
        SpawnTube.Constructor(Vector3(5447.744f, 3524.503f, 17.81691f), Vector3(0, 0, 45)),
        owning_building_guid = 26
      )
      LocalObject(
        2246,
        SpawnTube.Constructor(Vector3(5452.898f, 3529.656f, 17.81691f), Vector3(0, 0, 45)),
        owning_building_guid = 26
      )
      LocalObject(
        1569,
        ProximityTerminal.Constructor(Vector3(5388.113f, 3514.893f, 27.81691f), medical_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1570,
        ProximityTerminal.Constructor(Vector3(5474.513f, 3511.376f, 10.31691f), medical_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1845,
        ProximityTerminal.Constructor(Vector3(5439.806f, 3571.322f, 33.41091f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1846,
        Terminal.Constructor(Vector3(5439.806f, 3571.322f, 33.41091f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1848,
        ProximityTerminal.Constructor(Vector3(5451.632f, 3509.198f, 40.59291f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1849,
        Terminal.Constructor(Vector3(5451.632f, 3509.198f, 40.59291f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1851,
        ProximityTerminal.Constructor(Vector3(5460.715f, 3615.369f, 36.12691f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1852,
        Terminal.Constructor(Vector3(5460.715f, 3615.369f, 36.12691f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1854,
        ProximityTerminal.Constructor(Vector3(5488.066f, 3495.757f, 36.13991f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1855,
        Terminal.Constructor(Vector3(5488.066f, 3495.757f, 36.13991f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2143,
        ProximityTerminal.Constructor(Vector3(5391.654f, 3606.427f, 27.46691f), repair_silo),
        owning_building_guid = 26
      )
      LocalObject(
        2144,
        Terminal.Constructor(Vector3(5391.654f, 3606.427f, 27.46691f), ground_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2147,
        ProximityTerminal.Constructor(Vector3(5513.869f, 3488.328f, 27.46691f), repair_silo),
        owning_building_guid = 26
      )
      LocalObject(
        2148,
        Terminal.Constructor(Vector3(5513.869f, 3488.328f, 27.46691f), ground_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1512,
        FacilityTurret.Constructor(Vector3(5299.897f, 3530.351f, 36.32491f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1512, 5024)
      LocalObject(
        1516,
        FacilityTurret.Constructor(Vector3(5371.813f, 3456.781f, 36.32491f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1516, 5025)
      LocalObject(
        1517,
        FacilityTurret.Constructor(Vector3(5407.656f, 3639.741f, 36.32491f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1517, 5026)
      LocalObject(
        1518,
        FacilityTurret.Constructor(Vector3(5430.308f, 3455.725f, 36.32491f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1518, 5027)
      LocalObject(
        1519,
        FacilityTurret.Constructor(Vector3(5472.426f, 3640.809f, 36.32491f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1519, 5028)
      LocalObject(
        1520,
        FacilityTurret.Constructor(Vector3(5499.575f, 3456.776f, 36.32491f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1520, 5029)
      LocalObject(
        1521,
        FacilityTurret.Constructor(Vector3(5556.212f, 3639.736f, 36.32491f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1521, 5030)
      LocalObject(
        1524,
        FacilityTurret.Constructor(Vector3(5617.73f, 3576.564f, 36.32491f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1524, 5031)
      LocalObject(
        1949,
        Painbox.Constructor(Vector3(5470.568f, 3558.365f, 14.21121f), painbox),
        owning_building_guid = 26
      )
      LocalObject(
        1960,
        Painbox.Constructor(Vector3(5454.856f, 3518.005f, 21.84441f), painbox_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        1971,
        Painbox.Constructor(Vector3(5479.501f, 3547.817f, 13.44911f), painbox_door_radius),
        owning_building_guid = 26
      )
      LocalObject(
        1998,
        Painbox.Constructor(Vector3(5442.276f, 3509.341f, 20.24611f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        1999,
        Painbox.Constructor(Vector3(5463.137f, 3529.061f, 20.71691f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        2000,
        Painbox.Constructor(Vector3(5468.956f, 3514.484f, 21.14721f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(259, Generator.Constructor(Vector3(5467.694f, 3561.124f, 9.022909f)), owning_building_guid = 26)
      LocalObject(
        248,
        Terminal.Constructor(Vector3(5473.52f, 3555.364f, 10.31691f), gen_control),
        owning_building_guid = 26
      )
    }

    Building8()

    def Building8(): Unit = { // Name: Acan Type: cryo_facility GUID: 29, MapID: 8
      LocalBuilding(
        "Acan",
        29,
        8,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3478f, 4056f, 29.82269f),
            Vector3(0f, 0f, 269f),
            cryo_facility
          )
        )
      )
      LocalObject(
        187,
        CaptureTerminal.Constructor(Vector3(3537.747f, 4083.05f, 19.82269f), capture_terminal),
        owning_building_guid = 29
      )
      LocalObject(353, Door.Constructor(Vector3(3385.456f, 4025.611f, 31.34369f)), owning_building_guid = 29)
      LocalObject(357, Door.Constructor(Vector3(3394.676f, 4033.951f, 31.37369f)), owning_building_guid = 29)
      LocalObject(358, Door.Constructor(Vector3(3394.993f, 4052.141f, 39.33769f)), owning_building_guid = 29)
      LocalObject(363, Door.Constructor(Vector3(3426.426f, 4005.966f, 39.33769f)), owning_building_guid = 29)
      LocalObject(365, Door.Constructor(Vector3(3444.616f, 4005.648f, 31.37369f)), owning_building_guid = 29)
      LocalObject(377, Door.Constructor(Vector3(3481.067f, 4051.946f, 41.34369f)), owning_building_guid = 29)
      LocalObject(378, Door.Constructor(Vector3(3483.529f, 4114.89f, 31.37369f)), owning_building_guid = 29)
      LocalObject(380, Door.Constructor(Vector3(3498.276f, 4071.648f, 41.34369f)), owning_building_guid = 29)
      LocalObject(381, Door.Constructor(Vector3(3501.719f, 4114.572f, 39.33769f)), owning_building_guid = 29)
      LocalObject(383, Door.Constructor(Vector3(3546.214f, 4078.946f, 39.33769f)), owning_building_guid = 29)
      LocalObject(384, Door.Constructor(Vector3(3546.531f, 4097.136f, 31.37369f)), owning_building_guid = 29)
      LocalObject(622, Door.Constructor(Vector3(3390.223f, 4069.534f, 23.84369f)), owning_building_guid = 29)
      LocalObject(632, Door.Constructor(Vector3(3418.707f, 4097.041f, 23.84369f)), owning_building_guid = 29)
      LocalObject(633, Door.Constructor(Vector3(3422.358f, 4076.974f, 23.84369f)), owning_building_guid = 29)
      LocalObject(634, Door.Constructor(Vector3(3446.354f, 4076.555f, 21.34369f)), owning_building_guid = 29)
      LocalObject(635, Door.Constructor(Vector3(3450.004f, 4056.489f, 21.34369f)), owning_building_guid = 29)
      LocalObject(636, Door.Constructor(Vector3(3466.84f, 4104.202f, 21.34369f)), owning_building_guid = 29)
      LocalObject(637, Door.Constructor(Vector3(3469.931f, 4052.14f, 41.34369f)), owning_building_guid = 29)
      LocalObject(638, Door.Constructor(Vector3(3474f, 4056.07f, 21.34369f)), owning_building_guid = 29)
      LocalObject(639, Door.Constructor(Vector3(3477.791f, 4044.002f, 21.34369f)), owning_building_guid = 29)
      LocalObject(640, Door.Constructor(Vector3(3478.209f, 4067.998f, 21.34369f)), owning_building_guid = 29)
      LocalObject(641, Door.Constructor(Vector3(3478.628f, 4091.995f, 21.34369f)), owning_building_guid = 29)
      LocalObject(642, Door.Constructor(Vector3(3481.581f, 4031.934f, 13.84369f)), owning_building_guid = 29)
      LocalObject(643, Door.Constructor(Vector3(3497.578f, 4031.655f, 21.34369f)), owning_building_guid = 29)
      LocalObject(644, Door.Constructor(Vector3(3498.276f, 4071.648f, 21.34369f)), owning_building_guid = 29)
      LocalObject(645, Door.Constructor(Vector3(3498.276f, 4071.648f, 31.34369f)), owning_building_guid = 29)
      LocalObject(646, Door.Constructor(Vector3(3498.555f, 4087.646f, 21.34369f)), owning_building_guid = 29)
      LocalObject(647, Door.Constructor(Vector3(3501.787f, 4043.583f, 21.34369f)), owning_building_guid = 29)
      LocalObject(648, Door.Constructor(Vector3(3518.203f, 4067.3f, 21.34369f)), owning_building_guid = 29)
      LocalObject(649, Door.Constructor(Vector3(3518.622f, 4091.296f, 21.34369f)), owning_building_guid = 29)
      LocalObject(650, Door.Constructor(Vector3(3529.992f, 4055.093f, 21.34369f)), owning_building_guid = 29)
      LocalObject(651, Door.Constructor(Vector3(3546.269f, 4070.811f, 21.34369f)), owning_building_guid = 29)
      LocalObject(652, Door.Constructor(Vector3(3546.408f, 4078.81f, 21.34369f)), owning_building_guid = 29)
      LocalObject(653, Door.Constructor(Vector3(3546.548f, 4086.808f, 21.34369f)), owning_building_guid = 29)
      LocalObject(787, Door.Constructor(Vector3(3503.01f, 4051.571f, 32.10569f)), owning_building_guid = 29)
      LocalObject(794, Door.Constructor(Vector3(3477.791f, 4044.002f, 31.34369f)), owning_building_guid = 29)
      LocalObject(795, Door.Constructor(Vector3(3486.069f, 4059.86f, 31.34169f)), owning_building_guid = 29)
      LocalObject(2390, Door.Constructor(Vector3(3483.651f, 4051.228f, 21.67669f)), owning_building_guid = 29)
      LocalObject(2391, Door.Constructor(Vector3(3490.942f, 4051.1f, 21.67669f)), owning_building_guid = 29)
      LocalObject(2392, Door.Constructor(Vector3(3498.23f, 4050.973f, 21.67669f)), owning_building_guid = 29)
      LocalObject(
        827,
        IFFLock.Constructor(Vector3(3505.804f, 4054.745f, 31.30469f), Vector3(0, 0, 91)),
        owning_building_guid = 29,
        door_guid = 787
      )
      LocalObject(
        914,
        IFFLock.Constructor(Vector3(3387.483f, 4024.761f, 31.27469f), Vector3(0, 0, 181)),
        owning_building_guid = 29,
        door_guid = 353
      )
      LocalObject(
        927,
        IFFLock.Constructor(Vector3(3476.953f, 4042.444f, 21.15869f), Vector3(0, 0, 271)),
        owning_building_guid = 29,
        door_guid = 639
      )
      LocalObject(
        928,
        IFFLock.Constructor(Vector3(3480.023f, 4032.771f, 13.65869f), Vector3(0, 0, 1)),
        owning_building_guid = 29,
        door_guid = 642
      )
      LocalObject(
        929,
        IFFLock.Constructor(Vector3(3481.923f, 4053.977f, 41.27469f), Vector3(0, 0, 91)),
        owning_building_guid = 29,
        door_guid = 377
      )
      LocalObject(
        930,
        IFFLock.Constructor(Vector3(3500.305f, 4070.799f, 41.27469f), Vector3(0, 0, 181)),
        owning_building_guid = 29,
        door_guid = 380
      )
      LocalObject(
        931,
        IFFLock.Constructor(Vector3(3502.624f, 4045.141f, 21.15869f), Vector3(0, 0, 91)),
        owning_building_guid = 29,
        door_guid = 647
      )
      LocalObject(
        932,
        IFFLock.Constructor(Vector3(3544.993f, 4087.776f, 21.15869f), Vector3(0, 0, 1)),
        owning_building_guid = 29,
        door_guid = 653
      )
      LocalObject(
        933,
        IFFLock.Constructor(Vector3(3547.966f, 4077.969f, 21.15869f), Vector3(0, 0, 181)),
        owning_building_guid = 29,
        door_guid = 652
      )
      LocalObject(1181, Locker.Constructor(Vector3(3479.695f, 4017.867f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1182, Locker.Constructor(Vector3(3479.713f, 4018.921f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1183, Locker.Constructor(Vector3(3479.732f, 4019.981f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1184, Locker.Constructor(Vector3(3479.75f, 4021.036f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1185, Locker.Constructor(Vector3(3479.769f, 4022.091f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1186, Locker.Constructor(Vector3(3479.787f, 4023.147f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1187, Locker.Constructor(Vector3(3479.809f, 4036.943f, 20.08369f)), owning_building_guid = 29)
      LocalObject(1188, Locker.Constructor(Vector3(3479.829f, 4038.091f, 20.08369f)), owning_building_guid = 29)
      LocalObject(1189, Locker.Constructor(Vector3(3479.849f, 4039.238f, 20.08369f)), owning_building_guid = 29)
      LocalObject(1190, Locker.Constructor(Vector3(3479.869f, 4040.402f, 20.08369f)), owning_building_guid = 29)
      LocalObject(1191, Locker.Constructor(Vector3(3499.693f, 4017.521f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1192, Locker.Constructor(Vector3(3499.711f, 4018.576f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1193, Locker.Constructor(Vector3(3499.729f, 4019.631f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1194, Locker.Constructor(Vector3(3499.748f, 4020.686f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1195, Locker.Constructor(Vector3(3499.767f, 4021.747f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1196, Locker.Constructor(Vector3(3499.785f, 4022.801f, 19.73069f)), owning_building_guid = 29)
      LocalObject(1197, Locker.Constructor(Vector3(3527.139f, 4035.142f, 19.81769f)), owning_building_guid = 29)
      LocalObject(1198, Locker.Constructor(Vector3(3528.395f, 4035.12f, 19.81769f)), owning_building_guid = 29)
      LocalObject(1199, Locker.Constructor(Vector3(3529.656f, 4035.098f, 19.81769f)), owning_building_guid = 29)
      LocalObject(1200, Locker.Constructor(Vector3(3530.918f, 4035.076f, 19.81769f)), owning_building_guid = 29)
      LocalObject(1201, Locker.Constructor(Vector3(3532.17f, 4035.054f, 19.81769f)), owning_building_guid = 29)
      LocalObject(1404, Locker.Constructor(Vector3(3465.47f, 4037.956f, 29.82269f)), owning_building_guid = 29)
      LocalObject(1405, Locker.Constructor(Vector3(3466.504f, 4037.938f, 29.82269f)), owning_building_guid = 29)
      LocalObject(1406, Locker.Constructor(Vector3(3469.021f, 4037.894f, 29.59369f)), owning_building_guid = 29)
      LocalObject(1407, Locker.Constructor(Vector3(3470.054f, 4037.876f, 29.59369f)), owning_building_guid = 29)
      LocalObject(1408, Locker.Constructor(Vector3(3471.108f, 4037.857f, 29.59369f)), owning_building_guid = 29)
      LocalObject(1409, Locker.Constructor(Vector3(3472.142f, 4037.839f, 29.59369f)), owning_building_guid = 29)
      LocalObject(1410, Locker.Constructor(Vector3(3474.664f, 4037.795f, 29.82269f)), owning_building_guid = 29)
      LocalObject(1411, Locker.Constructor(Vector3(3475.698f, 4037.777f, 29.82269f)), owning_building_guid = 29)
      LocalObject(
        194,
        Terminal.Constructor(Vector3(3524.508f, 4038.762f, 19.81269f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        195,
        Terminal.Constructor(Vector3(3524.73f, 4051.46f, 19.81269f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        196,
        Terminal.Constructor(Vector3(3525.931f, 4037.289f, 19.81269f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        197,
        Terminal.Constructor(Vector3(3526.203f, 4052.882f, 19.81269f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        198,
        Terminal.Constructor(Vector3(3533.255f, 4037.161f, 19.81269f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        199,
        Terminal.Constructor(Vector3(3533.527f, 4052.754f, 19.81269f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        200,
        Terminal.Constructor(Vector3(3534.728f, 4038.583f, 19.81269f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        201,
        Terminal.Constructor(Vector3(3534.949f, 4051.281f, 19.81269f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1628,
        Terminal.Constructor(Vector3(3485.081f, 4037.219f, 21.41269f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1629,
        Terminal.Constructor(Vector3(3487.699f, 4065.86f, 31.11769f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1630,
        Terminal.Constructor(Vector3(3488.812f, 4037.155f, 21.41269f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1631,
        Terminal.Constructor(Vector3(3492.6f, 4037.088f, 21.41269f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2308,
        Terminal.Constructor(Vector3(3421.699f, 4073.075f, 23.93569f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2309,
        Terminal.Constructor(Vector3(3464.328f, 4054.333f, 31.40169f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2310,
        Terminal.Constructor(Vector3(3478.105f, 4096.004f, 21.43569f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2311,
        Terminal.Constructor(Vector3(3481.156f, 4050.973f, 21.95669f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2312,
        Terminal.Constructor(Vector3(3488.447f, 4050.85f, 21.95669f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2313,
        Terminal.Constructor(Vector3(3495.734f, 4050.72f, 21.95669f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2314,
        Terminal.Constructor(Vector3(3542f, 4055.476f, 21.43569f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2508,
        Terminal.Constructor(Vector3(3414.167f, 4019.48f, 32.12769f), vehicle_terminal_combined),
        owning_building_guid = 29
      )
      LocalObject(
        1544,
        VehicleSpawnPad.Constructor(Vector3(3414.315f, 4033.119f, 27.96969f), mb_pad_creation, Vector3(0, 0, 1)),
        owning_building_guid = 29,
        terminal_guid = 2508
      )
      LocalObject(2171, ResourceSilo.Constructor(Vector3(3529.896f, 4115.37f, 36.83969f)), owning_building_guid = 29)
      LocalObject(
        2210,
        SpawnTube.Constructor(Vector3(3482.608f, 4051.686f, 19.82269f), Vector3(0, 0, 91)),
        owning_building_guid = 29
      )
      LocalObject(
        2211,
        SpawnTube.Constructor(Vector3(3489.898f, 4051.559f, 19.82269f), Vector3(0, 0, 91)),
        owning_building_guid = 29
      )
      LocalObject(
        2212,
        SpawnTube.Constructor(Vector3(3497.185f, 4051.431f, 19.82269f), Vector3(0, 0, 91)),
        owning_building_guid = 29
      )
      LocalObject(
        139,
        ProximityTerminal.Constructor(Vector3(3470.858f, 4054.141f, 29.63269f), adv_med_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1563,
        ProximityTerminal.Constructor(Vector3(3481.469f, 4028.293f, 19.82269f), medical_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1794,
        ProximityTerminal.Constructor(Vector3(3408.819f, 4059.01f, 38.17469f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1795,
        Terminal.Constructor(Vector3(3408.819f, 4059.01f, 38.17469f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1800,
        ProximityTerminal.Constructor(Vector3(3416.387f, 4075.195f, 40.11569f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1801,
        Terminal.Constructor(Vector3(3416.387f, 4075.195f, 40.11569f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1806,
        ProximityTerminal.Constructor(Vector3(3523.223f, 4053.887f, 40.15469f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1807,
        Terminal.Constructor(Vector3(3523.223f, 4053.887f, 40.15469f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1809,
        ProximityTerminal.Constructor(Vector3(3531.903f, 4069.96f, 38.16469f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1810,
        Terminal.Constructor(Vector3(3531.903f, 4069.96f, 38.16469f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2107,
        ProximityTerminal.Constructor(Vector3(3393.786f, 4092.951f, 29.57269f), repair_silo),
        owning_building_guid = 29
      )
      LocalObject(
        2108,
        Terminal.Constructor(Vector3(3393.786f, 4092.951f, 29.57269f), ground_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2115,
        ProximityTerminal.Constructor(Vector3(3492.942f, 4003.201f, 29.57269f), repair_silo),
        owning_building_guid = 29
      )
      LocalObject(
        2116,
        Terminal.Constructor(Vector3(3492.942f, 4003.201f, 29.57269f), ground_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1468,
        FacilityTurret.Constructor(Vector3(3380.275f, 3994.07f, 38.22469f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1468, 5032)
      LocalObject(
        1469,
        FacilityTurret.Constructor(Vector3(3382.643f, 4129.275f, 38.22469f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1469, 5033)
      LocalObject(
        1476,
        FacilityTurret.Constructor(Vector3(3515.359f, 3990.525f, 38.22469f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1476, 5034)
      LocalObject(
        1478,
        FacilityTurret.Constructor(Vector3(3559.214f, 4032.914f, 38.22469f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1478, 5035)
      LocalObject(
        1479,
        FacilityTurret.Constructor(Vector3(3559.709f, 4126.193f, 38.22469f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1479, 5036)
      LocalObject(
        806,
        ImplantTerminalMech.Constructor(Vector3(3522.186f, 4045.161f, 19.29969f)),
        owning_building_guid = 29
      )
      LocalObject(
        800,
        Terminal.Constructor(Vector3(3522.204f, 4045.161f, 19.29969f), implant_terminal_interface),
        owning_building_guid = 29
      )
      TerminalToInterface(806, 800)
      LocalObject(
        807,
        ImplantTerminalMech.Constructor(Vector3(3537.54f, 4044.905f, 19.29969f)),
        owning_building_guid = 29
      )
      LocalObject(
        801,
        Terminal.Constructor(Vector3(3537.521f, 4044.906f, 19.29969f), implant_terminal_interface),
        owning_building_guid = 29
      )
      TerminalToInterface(807, 801)
      LocalObject(
        1945,
        Painbox.Constructor(Vector3(3458.239f, 4050.751f, 43.85149f), painbox),
        owning_building_guid = 29
      )
      LocalObject(
        1956,
        Painbox.Constructor(Vector3(3485.453f, 4041.115f, 23.89259f), painbox_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        1967,
        Painbox.Constructor(Vector3(3472.726f, 4051.909f, 44.05659f), painbox_door_radius),
        owning_building_guid = 29
      )
      LocalObject(
        1986,
        Painbox.Constructor(Vector3(3475.61f, 4045.5f, 22.17859f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        1987,
        Painbox.Constructor(Vector3(3496.561f, 4027.79f, 23.36359f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        1988,
        Painbox.Constructor(Vector3(3503.609f, 4043.036f, 21.53689f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(255, Generator.Constructor(Vector3(3454.378f, 4052.387f, 38.52869f)), owning_building_guid = 29)
      LocalObject(
        244,
        Terminal.Constructor(Vector3(3462.57f, 4052.291f, 39.82269f), gen_control),
        owning_building_guid = 29
      )
    }

    Building9()

    def Building9(): Unit = { // Name: Bitol Type: cryo_facility GUID: 32, MapID: 9
      LocalBuilding(
        "Bitol",
        32,
        9,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4480f, 2570f, 30.43306f),
            Vector3(0f, 0f, 360f),
            cryo_facility
          )
        )
      )
      LocalObject(
        189,
        CaptureTerminal.Constructor(Vector3(4451.911f, 2629.266f, 20.43306f), capture_terminal),
        owning_building_guid = 32
      )
      LocalObject(416, Door.Constructor(Vector3(4421.023f, 2574.5f, 31.98406f)), owning_building_guid = 32)
      LocalObject(417, Door.Constructor(Vector3(4421.023f, 2592.693f, 39.94806f)), owning_building_guid = 32)
      LocalObject(420, Door.Constructor(Vector3(4437.674f, 2637.803f, 31.98406f)), owning_building_guid = 32)
      LocalObject(422, Door.Constructor(Vector3(4455.867f, 2637.803f, 39.94806f)), owning_building_guid = 32)
      LocalObject(424, Door.Constructor(Vector3(4464f, 2590f, 41.95406f)), owning_building_guid = 32)
      LocalObject(428, Door.Constructor(Vector3(4484f, 2573.137f, 41.95406f)), owning_building_guid = 32)
      LocalObject(430, Door.Constructor(Vector3(4485.307f, 2487.073f, 39.94806f)), owning_building_guid = 32)
      LocalObject(437, Door.Constructor(Vector3(4503.5f, 2487.073f, 31.98406f)), owning_building_guid = 32)
      LocalObject(444, Door.Constructor(Vector3(4512f, 2478f, 31.95406f)), owning_building_guid = 32)
      LocalObject(446, Door.Constructor(Vector3(4530.927f, 2519.307f, 39.94806f)), owning_building_guid = 32)
      LocalObject(447, Door.Constructor(Vector3(4530.927f, 2537.5f, 31.98406f)), owning_building_guid = 32)
      LocalObject(673, Door.Constructor(Vector3(4432f, 2558f, 21.95406f)), owning_building_guid = 32)
      LocalObject(674, Door.Constructor(Vector3(4440f, 2510f, 24.45406f)), owning_building_guid = 32)
      LocalObject(675, Door.Constructor(Vector3(4444f, 2570f, 21.95406f)), owning_building_guid = 32)
      LocalObject(676, Door.Constructor(Vector3(4444f, 2610f, 21.95406f)), owning_building_guid = 32)
      LocalObject(677, Door.Constructor(Vector3(4448f, 2590f, 21.95406f)), owning_building_guid = 32)
      LocalObject(678, Door.Constructor(Vector3(4448f, 2638f, 21.95406f)), owning_building_guid = 32)
      LocalObject(680, Door.Constructor(Vector3(4456f, 2638f, 21.95406f)), owning_building_guid = 32)
      LocalObject(682, Door.Constructor(Vector3(4460f, 2514f, 24.45406f)), owning_building_guid = 32)
      LocalObject(683, Door.Constructor(Vector3(4460f, 2538f, 21.95406f)), owning_building_guid = 32)
      LocalObject(684, Door.Constructor(Vector3(4464f, 2590f, 21.95406f)), owning_building_guid = 32)
      LocalObject(685, Door.Constructor(Vector3(4464f, 2590f, 31.95406f)), owning_building_guid = 32)
      LocalObject(686, Door.Constructor(Vector3(4464f, 2638f, 21.95406f)), owning_building_guid = 32)
      LocalObject(689, Door.Constructor(Vector3(4468f, 2482f, 24.45406f)), owning_building_guid = 32)
      LocalObject(690, Door.Constructor(Vector3(4468f, 2570f, 21.95406f)), owning_building_guid = 32)
      LocalObject(691, Door.Constructor(Vector3(4468f, 2610f, 21.95406f)), owning_building_guid = 32)
      LocalObject(694, Door.Constructor(Vector3(4480f, 2542f, 21.95406f)), owning_building_guid = 32)
      LocalObject(695, Door.Constructor(Vector3(4480f, 2566f, 21.95406f)), owning_building_guid = 32)
      LocalObject(696, Door.Constructor(Vector3(4480f, 2622f, 21.95406f)), owning_building_guid = 32)
      LocalObject(700, Door.Constructor(Vector3(4484f, 2562f, 41.95406f)), owning_building_guid = 32)
      LocalObject(703, Door.Constructor(Vector3(4492f, 2570f, 21.95406f)), owning_building_guid = 32)
      LocalObject(704, Door.Constructor(Vector3(4492f, 2594f, 21.95406f)), owning_building_guid = 32)
      LocalObject(708, Door.Constructor(Vector3(4504f, 2574f, 14.45406f)), owning_building_guid = 32)
      LocalObject(709, Door.Constructor(Vector3(4504f, 2590f, 21.95406f)), owning_building_guid = 32)
      LocalObject(789, Door.Constructor(Vector3(4483.992f, 2595.083f, 32.71606f)), owning_building_guid = 32)
      LocalObject(796, Door.Constructor(Vector3(4476f, 2578f, 31.95206f)), owning_building_guid = 32)
      LocalObject(797, Door.Constructor(Vector3(4492f, 2570f, 31.95406f)), owning_building_guid = 32)
      LocalObject(2404, Door.Constructor(Vector3(4484.673f, 2575.733f, 22.28706f)), owning_building_guid = 32)
      LocalObject(2405, Door.Constructor(Vector3(4484.673f, 2583.026f, 22.28706f)), owning_building_guid = 32)
      LocalObject(2406, Door.Constructor(Vector3(4484.673f, 2590.315f, 22.28706f)), owning_building_guid = 32)
      LocalObject(
        829,
        IFFLock.Constructor(Vector3(4480.77f, 2597.822f, 31.91506f), Vector3(0, 0, 0)),
        owning_building_guid = 32,
        door_guid = 789
      )
      LocalObject(
        958,
        IFFLock.Constructor(Vector3(4447.06f, 2636.428f, 21.76906f), Vector3(0, 0, 270)),
        owning_building_guid = 32,
        door_guid = 678
      )
      LocalObject(
        959,
        IFFLock.Constructor(Vector3(4456.813f, 2639.572f, 21.76906f), Vector3(0, 0, 90)),
        owning_building_guid = 32,
        door_guid = 680
      )
      LocalObject(
        961,
        IFFLock.Constructor(Vector3(4464.814f, 2592.043f, 41.88506f), Vector3(0, 0, 90)),
        owning_building_guid = 32,
        door_guid = 424
      )
      LocalObject(
        965,
        IFFLock.Constructor(Vector3(4481.954f, 2573.958f, 41.88506f), Vector3(0, 0, 0)),
        owning_building_guid = 32,
        door_guid = 428
      )
      LocalObject(
        968,
        IFFLock.Constructor(Vector3(4490.428f, 2594.81f, 21.76906f), Vector3(0, 0, 0)),
        owning_building_guid = 32,
        door_guid = 704
      )
      LocalObject(
        970,
        IFFLock.Constructor(Vector3(4493.572f, 2569.19f, 21.76906f), Vector3(0, 0, 180)),
        owning_building_guid = 32,
        door_guid = 703
      )
      LocalObject(
        973,
        IFFLock.Constructor(Vector3(4503.19f, 2572.428f, 14.26906f), Vector3(0, 0, 270)),
        owning_building_guid = 32,
        door_guid = 708
      )
      LocalObject(
        980,
        IFFLock.Constructor(Vector3(4512.814f, 2480.042f, 31.88506f), Vector3(0, 0, 90)),
        owning_building_guid = 32,
        door_guid = 444
      )
      LocalObject(1246, Locker.Constructor(Vector3(4495.563f, 2572.141f, 20.69406f)), owning_building_guid = 32)
      LocalObject(1249, Locker.Constructor(Vector3(4496.727f, 2572.141f, 20.69406f)), owning_building_guid = 32)
      LocalObject(1252, Locker.Constructor(Vector3(4497.874f, 2572.141f, 20.69406f)), owning_building_guid = 32)
      LocalObject(1253, Locker.Constructor(Vector3(4499.023f, 2572.141f, 20.69406f)), owning_building_guid = 32)
      LocalObject(1256, Locker.Constructor(Vector3(4499.997f, 2619.496f, 20.42806f)), owning_building_guid = 32)
      LocalObject(1257, Locker.Constructor(Vector3(4499.997f, 2620.752f, 20.42806f)), owning_building_guid = 32)
      LocalObject(1258, Locker.Constructor(Vector3(4499.997f, 2622.013f, 20.42806f)), owning_building_guid = 32)
      LocalObject(1259, Locker.Constructor(Vector3(4499.997f, 2623.275f, 20.42806f)), owning_building_guid = 32)
      LocalObject(1260, Locker.Constructor(Vector3(4499.997f, 2624.527f, 20.42806f)), owning_building_guid = 32)
      LocalObject(1267, Locker.Constructor(Vector3(4512.817f, 2572.36f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1268, Locker.Constructor(Vector3(4512.814f, 2592.361f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1271, Locker.Constructor(Vector3(4513.873f, 2572.36f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1272, Locker.Constructor(Vector3(4513.868f, 2592.361f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1273, Locker.Constructor(Vector3(4514.928f, 2572.36f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1274, Locker.Constructor(Vector3(4514.929f, 2592.361f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1277, Locker.Constructor(Vector3(4515.983f, 2572.36f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1278, Locker.Constructor(Vector3(4515.984f, 2592.361f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1279, Locker.Constructor(Vector3(4517.043f, 2572.36f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1280, Locker.Constructor(Vector3(4517.039f, 2592.361f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1281, Locker.Constructor(Vector3(4518.098f, 2572.36f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1282, Locker.Constructor(Vector3(4518.095f, 2592.361f, 20.34106f)), owning_building_guid = 32)
      LocalObject(1412, Locker.Constructor(Vector3(4498.26f, 2557.787f, 30.43306f)), owning_building_guid = 32)
      LocalObject(1413, Locker.Constructor(Vector3(4498.26f, 2558.821f, 30.43306f)), owning_building_guid = 32)
      LocalObject(1414, Locker.Constructor(Vector3(4498.26f, 2561.338f, 30.20406f)), owning_building_guid = 32)
      LocalObject(1415, Locker.Constructor(Vector3(4498.26f, 2562.372f, 30.20406f)), owning_building_guid = 32)
      LocalObject(1416, Locker.Constructor(Vector3(4498.26f, 2563.426f, 30.20406f)), owning_building_guid = 32)
      LocalObject(1417, Locker.Constructor(Vector3(4498.26f, 2564.46f, 30.20406f)), owning_building_guid = 32)
      LocalObject(1418, Locker.Constructor(Vector3(4498.26f, 2566.982f, 30.43306f)), owning_building_guid = 32)
      LocalObject(1419, Locker.Constructor(Vector3(4498.26f, 2568.016f, 30.43306f)), owning_building_guid = 32)
      LocalObject(
        202,
        Terminal.Constructor(Vector3(4482.276f, 2618.25f, 20.42306f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        203,
        Terminal.Constructor(Vector3(4482.276f, 2625.575f, 20.42306f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        204,
        Terminal.Constructor(Vector3(4483.724f, 2616.802f, 20.42306f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        205,
        Terminal.Constructor(Vector3(4483.724f, 2627.023f, 20.42306f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        206,
        Terminal.Constructor(Vector3(4496.424f, 2616.802f, 20.42306f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        207,
        Terminal.Constructor(Vector3(4496.424f, 2627.023f, 20.42306f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        208,
        Terminal.Constructor(Vector3(4497.872f, 2618.25f, 20.42306f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        209,
        Terminal.Constructor(Vector3(4497.872f, 2625.575f, 20.42306f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1648,
        Terminal.Constructor(Vector3(4469.972f, 2579.526f, 31.72806f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1653,
        Terminal.Constructor(Vector3(4498.654f, 2577.408f, 22.02306f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1654,
        Terminal.Constructor(Vector3(4498.654f, 2581.139f, 22.02306f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1655,
        Terminal.Constructor(Vector3(4498.654f, 2584.928f, 22.02306f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2322,
        Terminal.Constructor(Vector3(4440f, 2569.407f, 22.04606f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2323,
        Terminal.Constructor(Vector3(4463.91f, 2513.41f, 24.54606f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2325,
        Terminal.Constructor(Vector3(4479.407f, 2634f, 22.04606f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2326,
        Terminal.Constructor(Vector3(4481.905f, 2556.359f, 32.01206f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2329,
        Terminal.Constructor(Vector3(4484.971f, 2573.243f, 22.56706f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2330,
        Terminal.Constructor(Vector3(4484.967f, 2580.535f, 22.56706f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2331,
        Terminal.Constructor(Vector3(4484.97f, 2587.823f, 22.56706f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2511,
        Terminal.Constructor(Vector3(4517.628f, 2506.814f, 32.73806f), vehicle_terminal_combined),
        owning_building_guid = 32
      )
      LocalObject(
        1548,
        VehicleSpawnPad.Constructor(Vector3(4503.989f, 2506.724f, 28.58006f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 32,
        terminal_guid = 2511
      )
      LocalObject(2173, ResourceSilo.Constructor(Vector3(4419.733f, 2620.852f, 37.45006f)), owning_building_guid = 32)
      LocalObject(
        2224,
        SpawnTube.Constructor(Vector3(4484.233f, 2574.683f, 20.43306f), Vector3(0, 0, 0)),
        owning_building_guid = 32
      )
      LocalObject(
        2225,
        SpawnTube.Constructor(Vector3(4484.233f, 2581.974f, 20.43306f), Vector3(0, 0, 0)),
        owning_building_guid = 32
      )
      LocalObject(
        2226,
        SpawnTube.Constructor(Vector3(4484.233f, 2589.262f, 20.43306f), Vector3(0, 0, 0)),
        owning_building_guid = 32
      )
      LocalObject(
        140,
        ProximityTerminal.Constructor(Vector3(4481.983f, 2562.892f, 30.24306f), adv_med_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1567,
        ProximityTerminal.Constructor(Vector3(4507.642f, 2573.952f, 20.43306f), medical_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1830,
        ProximityTerminal.Constructor(Vector3(4461.883f, 2508.061f, 40.72606f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        1831,
        Terminal.Constructor(Vector3(4461.883f, 2508.061f, 40.72606f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1833,
        ProximityTerminal.Constructor(Vector3(4465.101f, 2623.651f, 38.77506f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        1834,
        Terminal.Constructor(Vector3(4465.101f, 2623.651f, 38.77506f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1839,
        ProximityTerminal.Constructor(Vector3(4478.198f, 2500.777f, 38.78506f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        1840,
        Terminal.Constructor(Vector3(4478.198f, 2500.777f, 38.78506f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1842,
        ProximityTerminal.Constructor(Vector3(4481.323f, 2615.253f, 40.76506f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        1843,
        Terminal.Constructor(Vector3(4481.323f, 2615.253f, 40.76506f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2131,
        ProximityTerminal.Constructor(Vector3(4444.524f, 2485.154f, 30.18306f), repair_silo),
        owning_building_guid = 32
      )
      LocalObject(
        2132,
        Terminal.Constructor(Vector3(4444.524f, 2485.154f, 30.18306f), ground_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2135,
        ProximityTerminal.Constructor(Vector3(4532.53f, 2585.861f, 30.18306f), repair_silo),
        owning_building_guid = 32
      )
      LocalObject(
        2136,
        Terminal.Constructor(Vector3(4532.53f, 2585.861f, 30.18306f), ground_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1494,
        FacilityTurret.Constructor(Vector3(4408.392f, 2650.472f, 38.83506f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1494, 5037)
      LocalObject(
        1495,
        FacilityTurret.Constructor(Vector3(4408.4f, 2473.379f, 38.83506f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1495, 5038)
      LocalObject(
        1500,
        FacilityTurret.Constructor(Vector3(4501.665f, 2651.605f, 38.83506f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1500, 5039)
      LocalObject(
        1503,
        FacilityTurret.Constructor(Vector3(4543.626f, 2473.371f, 38.83506f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1503, 5040)
      LocalObject(
        1504,
        FacilityTurret.Constructor(Vector3(4544.813f, 2608.496f, 38.83506f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1504, 5041)
      LocalObject(
        808,
        ImplantTerminalMech.Constructor(Vector3(4490.066f, 2614.368f, 19.91006f)),
        owning_building_guid = 32
      )
      LocalObject(
        802,
        Terminal.Constructor(Vector3(4490.066f, 2614.386f, 19.91006f), implant_terminal_interface),
        owning_building_guid = 32
      )
      TerminalToInterface(808, 802)
      LocalObject(
        809,
        ImplantTerminalMech.Constructor(Vector3(4490.054f, 2629.724f, 19.91006f)),
        owning_building_guid = 32
      )
      LocalObject(
        803,
        Terminal.Constructor(Vector3(4490.054f, 2629.706f, 19.91006f), implant_terminal_interface),
        owning_building_guid = 32
      )
      TerminalToInterface(809, 803)
      LocalObject(
        1948,
        Painbox.Constructor(Vector3(4485.593f, 2550.334f, 44.46186f), painbox),
        owning_building_guid = 32
      )
      LocalObject(
        1958,
        Painbox.Constructor(Vector3(4494.753f, 2577.712f, 24.50296f), painbox_continuous),
        owning_building_guid = 32
      )
      LocalObject(
        1970,
        Painbox.Constructor(Vector3(4484.182f, 2564.798f, 44.66696f), painbox_door_radius),
        owning_building_guid = 32
      )
      LocalObject(
        1992,
        Painbox.Constructor(Vector3(4490.54f, 2567.793f, 22.78896f), painbox_door_radius_continuous),
        owning_building_guid = 32
      )
      LocalObject(
        1993,
        Painbox.Constructor(Vector3(4492.516f, 2595.831f, 22.14726f), painbox_door_radius_continuous),
        owning_building_guid = 32
      )
      LocalObject(
        1994,
        Painbox.Constructor(Vector3(4507.882f, 2589.05f, 23.97396f), painbox_door_radius_continuous),
        owning_building_guid = 32
      )
      LocalObject(258, Generator.Constructor(Vector3(4484.025f, 2546.445f, 39.13906f)), owning_building_guid = 32)
      LocalObject(
        247,
        Terminal.Constructor(Vector3(4483.978f, 2554.637f, 40.43306f), gen_control),
        owning_building_guid = 32
      )
    }

    Building7()

    def Building7(): Unit = { // Name: Zotz Type: cryo_facility GUID: 35, MapID: 7
      LocalBuilding(
        "Zotz",
        35,
        7,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(6730f, 2292f, 129.2093f),
            Vector3(0f, 0f, 90f),
            cryo_facility
          )
        )
      )
      LocalObject(
        193,
        CaptureTerminal.Constructor(Vector3(6670.734f, 2263.911f, 119.2093f), capture_terminal),
        owning_building_guid = 35
      )
      LocalObject(521, Door.Constructor(Vector3(6662.197f, 2249.674f, 130.7603f)), owning_building_guid = 35)
      LocalObject(522, Door.Constructor(Vector3(6662.197f, 2267.867f, 138.7243f)), owning_building_guid = 35)
      LocalObject(527, Door.Constructor(Vector3(6707.307f, 2233.023f, 138.7243f)), owning_building_guid = 35)
      LocalObject(528, Door.Constructor(Vector3(6710f, 2276f, 140.7303f)), owning_building_guid = 35)
      LocalObject(529, Door.Constructor(Vector3(6725.5f, 2233.023f, 130.7603f)), owning_building_guid = 35)
      LocalObject(530, Door.Constructor(Vector3(6726.863f, 2296f, 140.7303f)), owning_building_guid = 35)
      LocalObject(531, Door.Constructor(Vector3(6762.5f, 2342.927f, 130.7603f)), owning_building_guid = 35)
      LocalObject(532, Door.Constructor(Vector3(6780.693f, 2342.927f, 138.7243f)), owning_building_guid = 35)
      LocalObject(533, Door.Constructor(Vector3(6812.927f, 2297.307f, 138.7243f)), owning_building_guid = 35)
      LocalObject(534, Door.Constructor(Vector3(6812.927f, 2315.5f, 130.7603f)), owning_building_guid = 35)
      LocalObject(535, Door.Constructor(Vector3(6822f, 2324f, 130.7303f)), owning_building_guid = 35)
      LocalObject(760, Door.Constructor(Vector3(6662f, 2260f, 120.7303f)), owning_building_guid = 35)
      LocalObject(761, Door.Constructor(Vector3(6662f, 2268f, 120.7303f)), owning_building_guid = 35)
      LocalObject(762, Door.Constructor(Vector3(6662f, 2276f, 120.7303f)), owning_building_guid = 35)
      LocalObject(763, Door.Constructor(Vector3(6678f, 2292f, 120.7303f)), owning_building_guid = 35)
      LocalObject(764, Door.Constructor(Vector3(6690f, 2256f, 120.7303f)), owning_building_guid = 35)
      LocalObject(765, Door.Constructor(Vector3(6690f, 2280f, 120.7303f)), owning_building_guid = 35)
      LocalObject(766, Door.Constructor(Vector3(6706f, 2304f, 120.7303f)), owning_building_guid = 35)
      LocalObject(767, Door.Constructor(Vector3(6710f, 2260f, 120.7303f)), owning_building_guid = 35)
      LocalObject(768, Door.Constructor(Vector3(6710f, 2276f, 120.7303f)), owning_building_guid = 35)
      LocalObject(769, Door.Constructor(Vector3(6710f, 2276f, 130.7303f)), owning_building_guid = 35)
      LocalObject(770, Door.Constructor(Vector3(6710f, 2316f, 120.7303f)), owning_building_guid = 35)
      LocalObject(771, Door.Constructor(Vector3(6726f, 2316f, 113.2303f)), owning_building_guid = 35)
      LocalObject(772, Door.Constructor(Vector3(6730f, 2256f, 120.7303f)), owning_building_guid = 35)
      LocalObject(773, Door.Constructor(Vector3(6730f, 2280f, 120.7303f)), owning_building_guid = 35)
      LocalObject(774, Door.Constructor(Vector3(6730f, 2304f, 120.7303f)), owning_building_guid = 35)
      LocalObject(775, Door.Constructor(Vector3(6734f, 2292f, 120.7303f)), owning_building_guid = 35)
      LocalObject(776, Door.Constructor(Vector3(6738f, 2296f, 140.7303f)), owning_building_guid = 35)
      LocalObject(777, Door.Constructor(Vector3(6742f, 2244f, 120.7303f)), owning_building_guid = 35)
      LocalObject(778, Door.Constructor(Vector3(6758f, 2292f, 120.7303f)), owning_building_guid = 35)
      LocalObject(779, Door.Constructor(Vector3(6762f, 2272f, 120.7303f)), owning_building_guid = 35)
      LocalObject(780, Door.Constructor(Vector3(6786f, 2272f, 123.2303f)), owning_building_guid = 35)
      LocalObject(781, Door.Constructor(Vector3(6790f, 2252f, 123.2303f)), owning_building_guid = 35)
      LocalObject(782, Door.Constructor(Vector3(6818f, 2280f, 123.2303f)), owning_building_guid = 35)
      LocalObject(793, Door.Constructor(Vector3(6704.917f, 2295.992f, 131.4923f)), owning_building_guid = 35)
      LocalObject(798, Door.Constructor(Vector3(6722f, 2288f, 130.7283f)), owning_building_guid = 35)
      LocalObject(799, Door.Constructor(Vector3(6730f, 2304f, 130.7303f)), owning_building_guid = 35)
      LocalObject(2436, Door.Constructor(Vector3(6709.685f, 2296.673f, 121.0633f)), owning_building_guid = 35)
      LocalObject(2437, Door.Constructor(Vector3(6716.974f, 2296.673f, 121.0633f)), owning_building_guid = 35)
      LocalObject(2438, Door.Constructor(Vector3(6724.267f, 2296.673f, 121.0633f)), owning_building_guid = 35)
      LocalObject(
        833,
        IFFLock.Constructor(Vector3(6702.178f, 2292.77f, 130.6913f), Vector3(0, 0, 270)),
        owning_building_guid = 35,
        door_guid = 793
      )
      LocalObject(
        1041,
        IFFLock.Constructor(Vector3(6660.428f, 2268.813f, 120.5453f), Vector3(0, 0, 0)),
        owning_building_guid = 35,
        door_guid = 761
      )
      LocalObject(
        1042,
        IFFLock.Constructor(Vector3(6663.572f, 2259.06f, 120.5453f), Vector3(0, 0, 180)),
        owning_building_guid = 35,
        door_guid = 760
      )
      LocalObject(
        1047,
        IFFLock.Constructor(Vector3(6705.19f, 2302.428f, 120.5453f), Vector3(0, 0, 270)),
        owning_building_guid = 35,
        door_guid = 766
      )
      LocalObject(
        1048,
        IFFLock.Constructor(Vector3(6707.957f, 2276.814f, 140.6613f), Vector3(0, 0, 0)),
        owning_building_guid = 35,
        door_guid = 528
      )
      LocalObject(
        1049,
        IFFLock.Constructor(Vector3(6726.042f, 2293.954f, 140.6613f), Vector3(0, 0, 270)),
        owning_building_guid = 35,
        door_guid = 530
      )
      LocalObject(
        1050,
        IFFLock.Constructor(Vector3(6727.572f, 2315.19f, 113.0453f), Vector3(0, 0, 180)),
        owning_building_guid = 35,
        door_guid = 771
      )
      LocalObject(
        1051,
        IFFLock.Constructor(Vector3(6730.81f, 2305.572f, 120.5453f), Vector3(0, 0, 90)),
        owning_building_guid = 35,
        door_guid = 774
      )
      LocalObject(
        1052,
        IFFLock.Constructor(Vector3(6819.958f, 2324.814f, 130.6613f), Vector3(0, 0, 0)),
        owning_building_guid = 35,
        door_guid = 535
      )
      LocalObject(1375, Locker.Constructor(Vector3(6675.473f, 2311.997f, 119.2043f)), owning_building_guid = 35)
      LocalObject(1376, Locker.Constructor(Vector3(6676.725f, 2311.997f, 119.2043f)), owning_building_guid = 35)
      LocalObject(1377, Locker.Constructor(Vector3(6677.987f, 2311.997f, 119.2043f)), owning_building_guid = 35)
      LocalObject(1378, Locker.Constructor(Vector3(6679.248f, 2311.997f, 119.2043f)), owning_building_guid = 35)
      LocalObject(1379, Locker.Constructor(Vector3(6680.504f, 2311.997f, 119.2043f)), owning_building_guid = 35)
      LocalObject(1388, Locker.Constructor(Vector3(6707.639f, 2324.814f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1389, Locker.Constructor(Vector3(6707.639f, 2325.868f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1390, Locker.Constructor(Vector3(6707.639f, 2326.929f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1391, Locker.Constructor(Vector3(6707.639f, 2327.984f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1392, Locker.Constructor(Vector3(6707.639f, 2329.039f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1393, Locker.Constructor(Vector3(6707.639f, 2330.095f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1394, Locker.Constructor(Vector3(6727.64f, 2324.817f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1395, Locker.Constructor(Vector3(6727.64f, 2325.873f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1396, Locker.Constructor(Vector3(6727.64f, 2326.928f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1397, Locker.Constructor(Vector3(6727.64f, 2327.983f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1398, Locker.Constructor(Vector3(6727.64f, 2329.043f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1399, Locker.Constructor(Vector3(6727.64f, 2330.098f, 119.1173f)), owning_building_guid = 35)
      LocalObject(1400, Locker.Constructor(Vector3(6727.859f, 2307.563f, 119.4703f)), owning_building_guid = 35)
      LocalObject(1401, Locker.Constructor(Vector3(6727.859f, 2308.727f, 119.4703f)), owning_building_guid = 35)
      LocalObject(1402, Locker.Constructor(Vector3(6727.859f, 2309.874f, 119.4703f)), owning_building_guid = 35)
      LocalObject(1403, Locker.Constructor(Vector3(6727.859f, 2311.023f, 119.4703f)), owning_building_guid = 35)
      LocalObject(1420, Locker.Constructor(Vector3(6731.984f, 2310.26f, 129.2093f)), owning_building_guid = 35)
      LocalObject(1421, Locker.Constructor(Vector3(6733.018f, 2310.26f, 129.2093f)), owning_building_guid = 35)
      LocalObject(1422, Locker.Constructor(Vector3(6735.54f, 2310.26f, 128.9803f)), owning_building_guid = 35)
      LocalObject(1423, Locker.Constructor(Vector3(6736.574f, 2310.26f, 128.9803f)), owning_building_guid = 35)
      LocalObject(1424, Locker.Constructor(Vector3(6737.628f, 2310.26f, 128.9803f)), owning_building_guid = 35)
      LocalObject(1425, Locker.Constructor(Vector3(6738.662f, 2310.26f, 128.9803f)), owning_building_guid = 35)
      LocalObject(1426, Locker.Constructor(Vector3(6741.179f, 2310.26f, 129.2093f)), owning_building_guid = 35)
      LocalObject(1427, Locker.Constructor(Vector3(6742.213f, 2310.26f, 129.2093f)), owning_building_guid = 35)
      LocalObject(
        210,
        Terminal.Constructor(Vector3(6672.977f, 2295.724f, 119.1993f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        211,
        Terminal.Constructor(Vector3(6672.977f, 2308.424f, 119.1993f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        212,
        Terminal.Constructor(Vector3(6674.425f, 2294.276f, 119.1993f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        213,
        Terminal.Constructor(Vector3(6674.425f, 2309.872f, 119.1993f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        214,
        Terminal.Constructor(Vector3(6681.75f, 2294.276f, 119.1993f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        215,
        Terminal.Constructor(Vector3(6681.75f, 2309.872f, 119.1993f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        216,
        Terminal.Constructor(Vector3(6683.198f, 2295.724f, 119.1993f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        217,
        Terminal.Constructor(Vector3(6683.198f, 2308.424f, 119.1993f), cert_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1701,
        Terminal.Constructor(Vector3(6715.072f, 2310.654f, 120.7993f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1702,
        Terminal.Constructor(Vector3(6718.861f, 2310.654f, 120.7993f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1703,
        Terminal.Constructor(Vector3(6720.474f, 2281.972f, 130.5043f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1704,
        Terminal.Constructor(Vector3(6722.592f, 2310.654f, 120.7993f), order_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2351,
        Terminal.Constructor(Vector3(6666f, 2291.407f, 120.8223f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2352,
        Terminal.Constructor(Vector3(6712.177f, 2296.97f, 121.3433f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2353,
        Terminal.Constructor(Vector3(6719.465f, 2296.967f, 121.3433f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2354,
        Terminal.Constructor(Vector3(6726.757f, 2296.971f, 121.3433f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2355,
        Terminal.Constructor(Vector3(6730.593f, 2252f, 120.8223f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2356,
        Terminal.Constructor(Vector3(6743.641f, 2293.905f, 130.7883f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2357,
        Terminal.Constructor(Vector3(6786.59f, 2275.91f, 123.3223f), spawn_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2515,
        Terminal.Constructor(Vector3(6793.186f, 2329.628f, 131.5143f), vehicle_terminal_combined),
        owning_building_guid = 35
      )
      LocalObject(
        1554,
        VehicleSpawnPad.Constructor(Vector3(6793.276f, 2315.989f, 127.3563f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 35,
        terminal_guid = 2515
      )
      LocalObject(2177, ResourceSilo.Constructor(Vector3(6679.148f, 2231.733f, 136.2263f)), owning_building_guid = 35)
      LocalObject(
        2256,
        SpawnTube.Constructor(Vector3(6710.738f, 2296.233f, 119.2093f), Vector3(0, 0, 270)),
        owning_building_guid = 35
      )
      LocalObject(
        2257,
        SpawnTube.Constructor(Vector3(6718.026f, 2296.233f, 119.2093f), Vector3(0, 0, 270)),
        owning_building_guid = 35
      )
      LocalObject(
        2258,
        SpawnTube.Constructor(Vector3(6725.317f, 2296.233f, 119.2093f), Vector3(0, 0, 270)),
        owning_building_guid = 35
      )
      LocalObject(
        141,
        ProximityTerminal.Constructor(Vector3(6737.108f, 2293.983f, 129.0193f), adv_med_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1573,
        ProximityTerminal.Constructor(Vector3(6726.048f, 2319.642f, 119.2093f), medical_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1875,
        ProximityTerminal.Constructor(Vector3(6676.349f, 2277.101f, 137.5513f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1876,
        Terminal.Constructor(Vector3(6676.349f, 2277.101f, 137.5513f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1878,
        ProximityTerminal.Constructor(Vector3(6684.747f, 2293.323f, 139.5413f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1879,
        Terminal.Constructor(Vector3(6684.747f, 2293.323f, 139.5413f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1881,
        ProximityTerminal.Constructor(Vector3(6791.939f, 2273.883f, 139.5023f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1882,
        Terminal.Constructor(Vector3(6791.939f, 2273.883f, 139.5023f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1884,
        ProximityTerminal.Constructor(Vector3(6799.223f, 2290.198f, 137.5613f), pad_landing_frame),
        owning_building_guid = 35
      )
      LocalObject(
        1885,
        Terminal.Constructor(Vector3(6799.223f, 2290.198f, 137.5613f), air_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2159,
        ProximityTerminal.Constructor(Vector3(6714.139f, 2344.53f, 128.9593f), repair_silo),
        owning_building_guid = 35
      )
      LocalObject(
        2160,
        Terminal.Constructor(Vector3(6714.139f, 2344.53f, 128.9593f), ground_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        2163,
        ProximityTerminal.Constructor(Vector3(6814.846f, 2256.525f, 128.9593f), repair_silo),
        owning_building_guid = 35
      )
      LocalObject(
        2164,
        Terminal.Constructor(Vector3(6814.846f, 2256.525f, 128.9593f), ground_rearm_terminal),
        owning_building_guid = 35
      )
      LocalObject(
        1531,
        FacilityTurret.Constructor(Vector3(6648.395f, 2313.665f, 137.6113f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1531, 5042)
      LocalObject(
        1532,
        FacilityTurret.Constructor(Vector3(6649.528f, 2220.392f, 137.6113f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1532, 5043)
      LocalObject(
        1534,
        FacilityTurret.Constructor(Vector3(6691.504f, 2356.813f, 137.6113f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1534, 5044)
      LocalObject(
        1536,
        FacilityTurret.Constructor(Vector3(6826.621f, 2220.4f, 137.6113f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1536, 5045)
      LocalObject(
        1537,
        FacilityTurret.Constructor(Vector3(6826.629f, 2355.626f, 137.6113f), manned_turret),
        owning_building_guid = 35
      )
      TurretToWeapon(1537, 5046)
      LocalObject(
        810,
        ImplantTerminalMech.Constructor(Vector3(6670.276f, 2302.054f, 118.6863f)),
        owning_building_guid = 35
      )
      LocalObject(
        804,
        Terminal.Constructor(Vector3(6670.294f, 2302.054f, 118.6863f), implant_terminal_interface),
        owning_building_guid = 35
      )
      TerminalToInterface(810, 804)
      LocalObject(
        811,
        ImplantTerminalMech.Constructor(Vector3(6685.632f, 2302.066f, 118.6863f)),
        owning_building_guid = 35
      )
      LocalObject(
        805,
        Terminal.Constructor(Vector3(6685.614f, 2302.066f, 118.6863f), implant_terminal_interface),
        owning_building_guid = 35
      )
      TerminalToInterface(811, 805)
      LocalObject(
        1951,
        Painbox.Constructor(Vector3(6749.666f, 2297.594f, 143.2381f), painbox),
        owning_building_guid = 35
      )
      LocalObject(
        1962,
        Painbox.Constructor(Vector3(6722.288f, 2306.753f, 123.2792f), painbox_continuous),
        owning_building_guid = 35
      )
      LocalObject(
        1973,
        Painbox.Constructor(Vector3(6735.202f, 2296.182f, 143.4432f), painbox_door_radius),
        owning_building_guid = 35
      )
      LocalObject(
        2004,
        Painbox.Constructor(Vector3(6704.169f, 2304.515f, 120.9235f), painbox_door_radius_continuous),
        owning_building_guid = 35
      )
      LocalObject(
        2005,
        Painbox.Constructor(Vector3(6710.95f, 2319.882f, 122.7502f), painbox_door_radius_continuous),
        owning_building_guid = 35
      )
      LocalObject(
        2006,
        Painbox.Constructor(Vector3(6732.207f, 2302.54f, 121.5652f), painbox_door_radius_continuous),
        owning_building_guid = 35
      )
      LocalObject(261, Generator.Constructor(Vector3(6753.555f, 2296.025f, 137.9153f)), owning_building_guid = 35)
      LocalObject(
        250,
        Terminal.Constructor(Vector3(6745.363f, 2295.978f, 139.2093f), gen_control),
        owning_building_guid = 35
      )
    }

    Building18907()

    def Building18907(): Unit = { // Name: GW_Hossin_S Type: hst GUID: 39, MapID: 18907
      LocalBuilding(
        "GW_Hossin_S",
        39,
        18907,
        FoundationBuilder(WarpGate.Structure(Vector3(3949.24f, 2391.62f, 16.04f), hst))
      )
    }

    Building18908()

    def Building18908(): Unit = { // Name: GW_Hossin_N Type: hst GUID: 40, MapID: 18908
      LocalBuilding(
        "GW_Hossin_N",
        40,
        18908,
        FoundationBuilder(WarpGate.Structure(Vector3(5240.17f, 4912.36f, 39.88f), hst))
      )
    }

    Building11()

    def Building11(): Unit = { // Name: Ghanon Type: tech_plant GUID: 42, MapID: 11
      LocalBuilding(
        "Ghanon",
        42,
        11,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(2482f, 3762f, 41.20609f),
            Vector3(0f, 0f, 360f),
            tech_plant
          )
        )
      )
      LocalObject(
        184,
        CaptureTerminal.Constructor(Vector3(2486.734f, 3717.911f, 56.30609f), capture_terminal),
        owning_building_guid = 42
      )
      LocalObject(290, Door.Constructor(Vector3(2410.54f, 3691.929f, 42.74809f)), owning_building_guid = 42)
      LocalObject(291, Door.Constructor(Vector3(2410.54f, 3710.121f, 50.71109f)), owning_building_guid = 42)
      LocalObject(292, Door.Constructor(Vector3(2410.54f, 3765.673f, 42.74809f)), owning_building_guid = 42)
      LocalObject(293, Door.Constructor(Vector3(2410.54f, 3783.865f, 50.71109f)), owning_building_guid = 42)
      LocalObject(294, Door.Constructor(Vector3(2442f, 3662f, 42.82709f)), owning_building_guid = 42)
      LocalObject(295, Door.Constructor(Vector3(2468.412f, 3670.802f, 50.82009f)), owning_building_guid = 42)
      LocalObject(296, Door.Constructor(Vector3(2486.605f, 3670.802f, 42.85709f)), owning_building_guid = 42)
      LocalObject(297, Door.Constructor(Vector3(2492.597f, 3738.575f, 57.8271f)), owning_building_guid = 42)
      LocalObject(298, Door.Constructor(Vector3(2499.444f, 3722.574f, 57.8271f)), owning_building_guid = 42)
      LocalObject(299, Door.Constructor(Vector3(2506.559f, 3793.266f, 42.74809f)), owning_building_guid = 42)
      LocalObject(304, Door.Constructor(Vector3(2524.752f, 3793.266f, 50.71109f)), owning_building_guid = 42)
      LocalObject(305, Door.Constructor(Vector3(2561.02f, 3746.914f, 50.71109f)), owning_building_guid = 42)
      LocalObject(306, Door.Constructor(Vector3(2561.02f, 3765.107f, 42.74809f)), owning_building_guid = 42)
      LocalObject(536, Door.Constructor(Vector3(2434f, 3802.002f, 44.94309f)), owning_building_guid = 42)
      LocalObject(539, Door.Constructor(Vector3(2434f, 3746f, 24.94309f)), owning_building_guid = 42)
      LocalObject(566, Door.Constructor(Vector3(2418f, 3694f, 35.32709f)), owning_building_guid = 42)
      LocalObject(567, Door.Constructor(Vector3(2418f, 3718f, 32.8271f)), owning_building_guid = 42)
      LocalObject(568, Door.Constructor(Vector3(2450f, 3718f, 32.8271f)), owning_building_guid = 42)
      LocalObject(569, Door.Constructor(Vector3(2450f, 3742f, 32.8271f)), owning_building_guid = 42)
      LocalObject(570, Door.Constructor(Vector3(2466f, 3670f, 35.32709f)), owning_building_guid = 42)
      LocalObject(571, Door.Constructor(Vector3(2466f, 3710f, 35.32709f)), owning_building_guid = 42)
      LocalObject(572, Door.Constructor(Vector3(2478f, 3722f, 37.82709f)), owning_building_guid = 42)
      LocalObject(573, Door.Constructor(Vector3(2478f, 3722f, 57.8271f)), owning_building_guid = 42)
      LocalObject(574, Door.Constructor(Vector3(2478f, 3738f, 37.82709f)), owning_building_guid = 42)
      LocalObject(575, Door.Constructor(Vector3(2482f, 3686f, 35.32709f)), owning_building_guid = 42)
      LocalObject(576, Door.Constructor(Vector3(2482f, 3726f, 27.82709f)), owning_building_guid = 42)
      LocalObject(577, Door.Constructor(Vector3(2482f, 3730f, 47.82709f)), owning_building_guid = 42)
      LocalObject(578, Door.Constructor(Vector3(2482f, 3734f, 57.8271f)), owning_building_guid = 42)
      LocalObject(579, Door.Constructor(Vector3(2502f, 3714f, 27.82709f)), owning_building_guid = 42)
      LocalObject(580, Door.Constructor(Vector3(2502f, 3714f, 35.32709f)), owning_building_guid = 42)
      LocalObject(581, Door.Constructor(Vector3(2502f, 3738f, 35.32709f)), owning_building_guid = 42)
      LocalObject(582, Door.Constructor(Vector3(2510f, 3746f, 27.82709f)), owning_building_guid = 42)
      LocalObject(583, Door.Constructor(Vector3(2514f, 3718f, 27.82709f)), owning_building_guid = 42)
      LocalObject(584, Door.Constructor(Vector3(2514f, 3734f, 35.32709f)), owning_building_guid = 42)
      LocalObject(784, Door.Constructor(Vector3(2522.213f, 3716.341f, 43.58609f)), owning_building_guid = 42)
      LocalObject(2367, Door.Constructor(Vector3(2494.673f, 3719.733f, 35.66009f)), owning_building_guid = 42)
      LocalObject(2368, Door.Constructor(Vector3(2494.673f, 3727.026f, 35.66009f)), owning_building_guid = 42)
      LocalObject(2369, Door.Constructor(Vector3(2494.673f, 3734.315f, 35.66009f)), owning_building_guid = 42)
      LocalObject(
        824,
        IFFLock.Constructor(Vector3(2525.357f, 3713.603f, 42.78609f), Vector3(0, 0, 180)),
        owning_building_guid = 42,
        door_guid = 784
      )
      LocalObject(
        834,
        IFFLock.Constructor(Vector3(2439.256f, 3804.353f, 42.89409f), Vector3(0, 0, 360)),
        owning_building_guid = 42,
        door_guid = 536
      )
      LocalObject(
        862,
        IFFLock.Constructor(Vector3(2441.186f, 3659.954f, 42.75809f), Vector3(0, 0, 270)),
        owning_building_guid = 42,
        door_guid = 294
      )
      LocalObject(
        863,
        IFFLock.Constructor(Vector3(2476.428f, 3722.94f, 57.64209f), Vector3(0, 0, 0)),
        owning_building_guid = 42,
        door_guid = 573
      )
      LocalObject(
        864,
        IFFLock.Constructor(Vector3(2490.554f, 3739.383f, 57.75209f), Vector3(0, 0, 0)),
        owning_building_guid = 42,
        door_guid = 297
      )
      LocalObject(
        865,
        IFFLock.Constructor(Vector3(2500.428f, 3738.81f, 35.14209f), Vector3(0, 0, 0)),
        owning_building_guid = 42,
        door_guid = 581
      )
      LocalObject(
        866,
        IFFLock.Constructor(Vector3(2501.496f, 3721.775f, 57.75209f), Vector3(0, 0, 180)),
        owning_building_guid = 42,
        door_guid = 298
      )
      LocalObject(
        867,
        IFFLock.Constructor(Vector3(2503.572f, 3713.19f, 35.14209f), Vector3(0, 0, 180)),
        owning_building_guid = 42,
        door_guid = 580
      )
      LocalObject(
        868,
        IFFLock.Constructor(Vector3(2511.572f, 3745.057f, 27.64209f), Vector3(0, 0, 180)),
        owning_building_guid = 42,
        door_guid = 582
      )
      LocalObject(
        869,
        IFFLock.Constructor(Vector3(2513.06f, 3716.428f, 27.64209f), Vector3(0, 0, 270)),
        owning_building_guid = 42,
        door_guid = 583
      )
      LocalObject(1089, Locker.Constructor(Vector3(2505.563f, 3716.141f, 34.06709f)), owning_building_guid = 42)
      LocalObject(1090, Locker.Constructor(Vector3(2506.727f, 3716.141f, 34.06709f)), owning_building_guid = 42)
      LocalObject(1091, Locker.Constructor(Vector3(2507.874f, 3716.141f, 34.06709f)), owning_building_guid = 42)
      LocalObject(1092, Locker.Constructor(Vector3(2509.023f, 3716.141f, 34.06709f)), owning_building_guid = 42)
      LocalObject(1093, Locker.Constructor(Vector3(2516.194f, 3736.165f, 26.30609f)), owning_building_guid = 42)
      LocalObject(1094, Locker.Constructor(Vector3(2517.518f, 3736.165f, 26.30609f)), owning_building_guid = 42)
      LocalObject(1095, Locker.Constructor(Vector3(2518.854f, 3736.165f, 26.30609f)), owning_building_guid = 42)
      LocalObject(1096, Locker.Constructor(Vector3(2520.191f, 3736.165f, 26.30609f)), owning_building_guid = 42)
      LocalObject(1101, Locker.Constructor(Vector3(2524.731f, 3736.165f, 26.30609f)), owning_building_guid = 42)
      LocalObject(1104, Locker.Constructor(Vector3(2526.055f, 3736.165f, 26.30609f)), owning_building_guid = 42)
      LocalObject(1107, Locker.Constructor(Vector3(2527.391f, 3736.165f, 26.30609f)), owning_building_guid = 42)
      LocalObject(1108, Locker.Constructor(Vector3(2528.728f, 3736.165f, 26.30609f)), owning_building_guid = 42)
      LocalObject(
        142,
        Terminal.Constructor(Vector3(2486.673f, 3737.141f, 56.90909f), air_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1540,
        VehicleSpawnPad.Constructor(Vector3(2482.07f, 3757.835f, 53.78409f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 42,
        terminal_guid = 142
      )
      LocalObject(
        143,
        Terminal.Constructor(Vector3(2498.605f, 3737.141f, 56.90909f), air_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1541,
        VehicleSpawnPad.Constructor(Vector3(2503.088f, 3757.835f, 53.78409f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 42,
        terminal_guid = 143
      )
      LocalObject(
        1590,
        Terminal.Constructor(Vector3(2485.058f, 3735.486f, 47.63609f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1591,
        Terminal.Constructor(Vector3(2508.654f, 3721.408f, 35.39609f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1592,
        Terminal.Constructor(Vector3(2508.654f, 3725.139f, 35.39609f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1593,
        Terminal.Constructor(Vector3(2508.654f, 3728.928f, 35.39609f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2289,
        Terminal.Constructor(Vector3(2457.942f, 3666.591f, 35.36309f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2290,
        Terminal.Constructor(Vector3(2489.942f, 3714.591f, 27.86309f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2291,
        Terminal.Constructor(Vector3(2494.971f, 3717.243f, 35.94009f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2292,
        Terminal.Constructor(Vector3(2494.967f, 3724.535f, 35.94009f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2293,
        Terminal.Constructor(Vector3(2494.97f, 3731.823f, 35.94009f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2294,
        Terminal.Constructor(Vector3(2496.532f, 3765.215f, 53.22909f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2295,
        Terminal.Constructor(Vector3(2519.242f, 3741.639f, 47.88809f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2506,
        Terminal.Constructor(Vector3(2433.996f, 3721.423f, 27.02009f), ground_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1539,
        VehicleSpawnPad.Constructor(Vector3(2433.945f, 3732.339f, 18.74309f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 42,
        terminal_guid = 2506
      )
      LocalObject(2168, ResourceSilo.Constructor(Vector3(2543.752f, 3794.555f, 48.21409f)), owning_building_guid = 42)
      LocalObject(
        2187,
        SpawnTube.Constructor(Vector3(2494.233f, 3718.683f, 33.80609f), Vector3(0, 0, 0)),
        owning_building_guid = 42
      )
      LocalObject(
        2188,
        SpawnTube.Constructor(Vector3(2494.233f, 3725.974f, 33.80609f), Vector3(0, 0, 0)),
        owning_building_guid = 42
      )
      LocalObject(
        2189,
        SpawnTube.Constructor(Vector3(2494.233f, 3733.262f, 33.80609f), Vector3(0, 0, 0)),
        owning_building_guid = 42
      )
      LocalObject(
        1557,
        ProximityTerminal.Constructor(Vector3(2485.059f, 3724.901f, 46.30309f), medical_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1558,
        ProximityTerminal.Constructor(Vector3(2522.444f, 3735.62f, 26.30609f), medical_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1767,
        ProximityTerminal.Constructor(Vector3(2420.704f, 3737.661f, 49.40109f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1768,
        Terminal.Constructor(Vector3(2420.704f, 3737.661f, 49.40109f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1770,
        ProximityTerminal.Constructor(Vector3(2440.98f, 3753.833f, 51.75609f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1771,
        Terminal.Constructor(Vector3(2440.98f, 3753.833f, 51.75609f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1773,
        ProximityTerminal.Constructor(Vector3(2491.379f, 3697.474f, 56.65309f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1774,
        Terminal.Constructor(Vector3(2491.379f, 3697.474f, 56.65309f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1776,
        ProximityTerminal.Constructor(Vector3(2507.534f, 3680.628f, 49.41409f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1777,
        Terminal.Constructor(Vector3(2507.534f, 3680.628f, 49.41409f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1779,
        ProximityTerminal.Constructor(Vector3(2543.987f, 3719.855f, 51.85809f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1780,
        Terminal.Constructor(Vector3(2543.987f, 3719.855f, 51.85809f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1782,
        ProximityTerminal.Constructor(Vector3(2550.28f, 3736.102f, 49.41409f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1783,
        Terminal.Constructor(Vector3(2550.28f, 3736.102f, 49.41409f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2087,
        ProximityTerminal.Constructor(Vector3(2482.309f, 3803.637f, 40.93459f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        2088,
        Terminal.Constructor(Vector3(2482.309f, 3803.637f, 40.93459f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2091,
        ProximityTerminal.Constructor(Vector3(2540.637f, 3669.208f, 40.95609f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        2092,
        Terminal.Constructor(Vector3(2540.637f, 3669.208f, 40.95609f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1449,
        FacilityTurret.Constructor(Vector3(2397.906f, 3860.855f, 49.70509f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1449, 5047)
      LocalObject(
        1450,
        FacilityTurret.Constructor(Vector3(2403.413f, 3662.665f, 49.70509f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1450, 5048)
      LocalObject(
        1451,
        FacilityTurret.Constructor(Vector3(2487.601f, 3860.855f, 49.70509f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1451, 5049)
      LocalObject(
        1454,
        FacilityTurret.Constructor(Vector3(2568.154f, 3662.657f, 49.70509f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1454, 5050)
      LocalObject(
        1455,
        FacilityTurret.Constructor(Vector3(2568.154f, 3800.398f, 49.70509f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1455, 5051)
      LocalObject(
        1456,
        FacilityTurret.Constructor(Vector3(2575.881f, 3727.423f, 49.70509f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1456, 5052)
      LocalObject(
        1942,
        Painbox.Constructor(Vector3(2507.737f, 3758.206f, 29.77939f), painbox),
        owning_building_guid = 42
      )
      LocalObject(
        1953,
        Painbox.Constructor(Vector3(2502.832f, 3725.212f, 38.07599f), painbox_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1964,
        Painbox.Constructor(Vector3(2509.7f, 3743.471f, 29.46549f), painbox_door_radius),
        owning_building_guid = 42
      )
      LocalObject(
        1977,
        Painbox.Constructor(Vector3(2501.035f, 3712.278f, 35.93399f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1978,
        Painbox.Constructor(Vector3(2501.861f, 3739.769f, 36.48229f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1979,
        Painbox.Constructor(Vector3(2517.641f, 3732.57f, 37.38839f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(252, Generator.Constructor(Vector3(2509.975f, 3761.555f, 25.01209f)), owning_building_guid = 42)
      LocalObject(
        241,
        Terminal.Constructor(Vector3(2510.022f, 3753.363f, 26.30609f), gen_control),
        owning_building_guid = 42
      )
    }

    Building10()

    def Building10(): Unit = { // Name: Chac Type: tech_plant GUID: 45, MapID: 10
      LocalBuilding(
        "Chac",
        45,
        10,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4020f, 6012f, 39.28953f),
            Vector3(0f, 0f, 360f),
            tech_plant
          )
        )
      )
      LocalObject(
        188,
        CaptureTerminal.Constructor(Vector3(4024.734f, 5967.911f, 54.38953f), capture_terminal),
        owning_building_guid = 45
      )
      LocalObject(398, Door.Constructor(Vector3(3948.54f, 5941.929f, 40.83153f)), owning_building_guid = 45)
      LocalObject(399, Door.Constructor(Vector3(3948.54f, 5960.121f, 48.79453f)), owning_building_guid = 45)
      LocalObject(400, Door.Constructor(Vector3(3948.54f, 6015.673f, 40.83153f)), owning_building_guid = 45)
      LocalObject(401, Door.Constructor(Vector3(3948.54f, 6033.865f, 48.79453f)), owning_building_guid = 45)
      LocalObject(402, Door.Constructor(Vector3(3980f, 5912f, 40.91053f)), owning_building_guid = 45)
      LocalObject(403, Door.Constructor(Vector3(4006.412f, 5920.802f, 48.90353f)), owning_building_guid = 45)
      LocalObject(404, Door.Constructor(Vector3(4024.605f, 5920.802f, 40.94053f)), owning_building_guid = 45)
      LocalObject(406, Door.Constructor(Vector3(4030.597f, 5988.575f, 55.91053f)), owning_building_guid = 45)
      LocalObject(407, Door.Constructor(Vector3(4037.444f, 5972.574f, 55.91053f)), owning_building_guid = 45)
      LocalObject(408, Door.Constructor(Vector3(4044.559f, 6043.266f, 40.83153f)), owning_building_guid = 45)
      LocalObject(409, Door.Constructor(Vector3(4062.752f, 6043.266f, 48.79453f)), owning_building_guid = 45)
      LocalObject(410, Door.Constructor(Vector3(4099.02f, 5996.914f, 48.79453f)), owning_building_guid = 45)
      LocalObject(411, Door.Constructor(Vector3(4099.02f, 6015.107f, 40.83153f)), owning_building_guid = 45)
      LocalObject(537, Door.Constructor(Vector3(3972f, 6052.002f, 43.02653f)), owning_building_guid = 45)
      LocalObject(540, Door.Constructor(Vector3(3972f, 5996f, 23.02653f)), owning_building_guid = 45)
      LocalObject(654, Door.Constructor(Vector3(3956f, 5944f, 33.41053f)), owning_building_guid = 45)
      LocalObject(655, Door.Constructor(Vector3(3956f, 5968f, 30.91053f)), owning_building_guid = 45)
      LocalObject(656, Door.Constructor(Vector3(3988f, 5968f, 30.91053f)), owning_building_guid = 45)
      LocalObject(657, Door.Constructor(Vector3(3988f, 5992f, 30.91053f)), owning_building_guid = 45)
      LocalObject(658, Door.Constructor(Vector3(4004f, 5920f, 33.41053f)), owning_building_guid = 45)
      LocalObject(659, Door.Constructor(Vector3(4004f, 5960f, 33.41053f)), owning_building_guid = 45)
      LocalObject(660, Door.Constructor(Vector3(4016f, 5972f, 35.91053f)), owning_building_guid = 45)
      LocalObject(661, Door.Constructor(Vector3(4016f, 5972f, 55.91053f)), owning_building_guid = 45)
      LocalObject(662, Door.Constructor(Vector3(4016f, 5988f, 35.91053f)), owning_building_guid = 45)
      LocalObject(663, Door.Constructor(Vector3(4020f, 5936f, 33.41053f)), owning_building_guid = 45)
      LocalObject(664, Door.Constructor(Vector3(4020f, 5976f, 25.91053f)), owning_building_guid = 45)
      LocalObject(665, Door.Constructor(Vector3(4020f, 5980f, 45.91053f)), owning_building_guid = 45)
      LocalObject(666, Door.Constructor(Vector3(4020f, 5984f, 55.91053f)), owning_building_guid = 45)
      LocalObject(667, Door.Constructor(Vector3(4040f, 5964f, 25.91053f)), owning_building_guid = 45)
      LocalObject(668, Door.Constructor(Vector3(4040f, 5964f, 33.41053f)), owning_building_guid = 45)
      LocalObject(669, Door.Constructor(Vector3(4040f, 5988f, 33.41053f)), owning_building_guid = 45)
      LocalObject(670, Door.Constructor(Vector3(4048f, 5996f, 25.91053f)), owning_building_guid = 45)
      LocalObject(671, Door.Constructor(Vector3(4052f, 5968f, 25.91053f)), owning_building_guid = 45)
      LocalObject(672, Door.Constructor(Vector3(4052f, 5984f, 33.41053f)), owning_building_guid = 45)
      LocalObject(788, Door.Constructor(Vector3(4060.213f, 5966.341f, 41.66953f)), owning_building_guid = 45)
      LocalObject(2399, Door.Constructor(Vector3(4032.673f, 5969.733f, 33.74353f)), owning_building_guid = 45)
      LocalObject(2400, Door.Constructor(Vector3(4032.673f, 5977.026f, 33.74353f)), owning_building_guid = 45)
      LocalObject(2401, Door.Constructor(Vector3(4032.673f, 5984.315f, 33.74353f)), owning_building_guid = 45)
      LocalObject(
        828,
        IFFLock.Constructor(Vector3(4063.357f, 5963.603f, 40.86953f), Vector3(0, 0, 180)),
        owning_building_guid = 45,
        door_guid = 788
      )
      LocalObject(
        835,
        IFFLock.Constructor(Vector3(3977.256f, 6054.353f, 40.97753f), Vector3(0, 0, 360)),
        owning_building_guid = 45,
        door_guid = 537
      )
      LocalObject(
        946,
        IFFLock.Constructor(Vector3(3979.186f, 5909.954f, 40.84153f), Vector3(0, 0, 270)),
        owning_building_guid = 45,
        door_guid = 402
      )
      LocalObject(
        947,
        IFFLock.Constructor(Vector3(4014.428f, 5972.94f, 55.72553f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 661
      )
      LocalObject(
        948,
        IFFLock.Constructor(Vector3(4028.554f, 5989.383f, 55.83553f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 406
      )
      LocalObject(
        949,
        IFFLock.Constructor(Vector3(4038.428f, 5988.81f, 33.22553f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 669
      )
      LocalObject(
        950,
        IFFLock.Constructor(Vector3(4039.496f, 5971.775f, 55.83553f), Vector3(0, 0, 180)),
        owning_building_guid = 45,
        door_guid = 407
      )
      LocalObject(
        951,
        IFFLock.Constructor(Vector3(4041.572f, 5963.19f, 33.22553f), Vector3(0, 0, 180)),
        owning_building_guid = 45,
        door_guid = 668
      )
      LocalObject(
        952,
        IFFLock.Constructor(Vector3(4049.572f, 5995.057f, 25.72553f), Vector3(0, 0, 180)),
        owning_building_guid = 45,
        door_guid = 670
      )
      LocalObject(
        953,
        IFFLock.Constructor(Vector3(4051.06f, 5966.428f, 25.72553f), Vector3(0, 0, 270)),
        owning_building_guid = 45,
        door_guid = 671
      )
      LocalObject(1226, Locker.Constructor(Vector3(4043.563f, 5966.141f, 32.15053f)), owning_building_guid = 45)
      LocalObject(1227, Locker.Constructor(Vector3(4044.727f, 5966.141f, 32.15053f)), owning_building_guid = 45)
      LocalObject(1228, Locker.Constructor(Vector3(4045.874f, 5966.141f, 32.15053f)), owning_building_guid = 45)
      LocalObject(1229, Locker.Constructor(Vector3(4047.023f, 5966.141f, 32.15053f)), owning_building_guid = 45)
      LocalObject(1230, Locker.Constructor(Vector3(4054.194f, 5986.165f, 24.38953f)), owning_building_guid = 45)
      LocalObject(1231, Locker.Constructor(Vector3(4055.518f, 5986.165f, 24.38953f)), owning_building_guid = 45)
      LocalObject(1232, Locker.Constructor(Vector3(4056.854f, 5986.165f, 24.38953f)), owning_building_guid = 45)
      LocalObject(1233, Locker.Constructor(Vector3(4058.191f, 5986.165f, 24.38953f)), owning_building_guid = 45)
      LocalObject(1234, Locker.Constructor(Vector3(4062.731f, 5986.165f, 24.38953f)), owning_building_guid = 45)
      LocalObject(1235, Locker.Constructor(Vector3(4064.055f, 5986.165f, 24.38953f)), owning_building_guid = 45)
      LocalObject(1236, Locker.Constructor(Vector3(4065.391f, 5986.165f, 24.38953f)), owning_building_guid = 45)
      LocalObject(1237, Locker.Constructor(Vector3(4066.728f, 5986.165f, 24.38953f)), owning_building_guid = 45)
      LocalObject(
        144,
        Terminal.Constructor(Vector3(4024.673f, 5987.141f, 54.99253f), air_vehicle_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1546,
        VehicleSpawnPad.Constructor(Vector3(4020.07f, 6007.835f, 51.86753f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 45,
        terminal_guid = 144
      )
      LocalObject(
        145,
        Terminal.Constructor(Vector3(4036.605f, 5987.141f, 54.99253f), air_vehicle_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1547,
        VehicleSpawnPad.Constructor(Vector3(4041.088f, 6007.835f, 51.86753f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 45,
        terminal_guid = 145
      )
      LocalObject(
        1641,
        Terminal.Constructor(Vector3(4023.058f, 5985.486f, 45.71953f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1642,
        Terminal.Constructor(Vector3(4046.654f, 5971.408f, 33.47953f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1643,
        Terminal.Constructor(Vector3(4046.654f, 5975.139f, 33.47953f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1644,
        Terminal.Constructor(Vector3(4046.654f, 5978.928f, 33.47953f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2315,
        Terminal.Constructor(Vector3(3995.942f, 5916.591f, 33.44653f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2316,
        Terminal.Constructor(Vector3(4027.942f, 5964.591f, 25.94653f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2317,
        Terminal.Constructor(Vector3(4032.971f, 5967.243f, 34.02353f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2318,
        Terminal.Constructor(Vector3(4032.967f, 5974.535f, 34.02353f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2319,
        Terminal.Constructor(Vector3(4032.97f, 5981.823f, 34.02353f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2320,
        Terminal.Constructor(Vector3(4034.532f, 6015.215f, 51.31253f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2321,
        Terminal.Constructor(Vector3(4057.242f, 5991.639f, 45.97153f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2510,
        Terminal.Constructor(Vector3(3971.996f, 5971.423f, 25.10353f), ground_vehicle_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1545,
        VehicleSpawnPad.Constructor(Vector3(3971.945f, 5982.339f, 16.82653f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 45,
        terminal_guid = 2510
      )
      LocalObject(2172, ResourceSilo.Constructor(Vector3(4081.752f, 6044.555f, 46.29753f)), owning_building_guid = 45)
      LocalObject(
        2219,
        SpawnTube.Constructor(Vector3(4032.233f, 5968.683f, 31.88953f), Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        2220,
        SpawnTube.Constructor(Vector3(4032.233f, 5975.974f, 31.88953f), Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        2221,
        SpawnTube.Constructor(Vector3(4032.233f, 5983.262f, 31.88953f), Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        1564,
        ProximityTerminal.Constructor(Vector3(4023.059f, 5974.901f, 44.38653f), medical_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1565,
        ProximityTerminal.Constructor(Vector3(4060.444f, 5985.62f, 24.38953f), medical_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1812,
        ProximityTerminal.Constructor(Vector3(3958.704f, 5987.661f, 47.48453f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        1813,
        Terminal.Constructor(Vector3(3958.704f, 5987.661f, 47.48453f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1815,
        ProximityTerminal.Constructor(Vector3(3978.98f, 6003.833f, 49.83953f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        1816,
        Terminal.Constructor(Vector3(3978.98f, 6003.833f, 49.83953f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1818,
        ProximityTerminal.Constructor(Vector3(4029.379f, 5947.474f, 54.73653f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        1819,
        Terminal.Constructor(Vector3(4029.379f, 5947.474f, 54.73653f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1821,
        ProximityTerminal.Constructor(Vector3(4045.534f, 5930.628f, 47.49753f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        1822,
        Terminal.Constructor(Vector3(4045.534f, 5930.628f, 47.49753f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1824,
        ProximityTerminal.Constructor(Vector3(4081.987f, 5969.855f, 49.94153f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        1825,
        Terminal.Constructor(Vector3(4081.987f, 5969.855f, 49.94153f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1827,
        ProximityTerminal.Constructor(Vector3(4088.28f, 5986.102f, 47.49753f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        1828,
        Terminal.Constructor(Vector3(4088.28f, 5986.102f, 47.49753f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2119,
        ProximityTerminal.Constructor(Vector3(4020.309f, 6053.637f, 39.01803f), repair_silo),
        owning_building_guid = 45
      )
      LocalObject(
        2120,
        Terminal.Constructor(Vector3(4020.309f, 6053.637f, 39.01803f), ground_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2123,
        ProximityTerminal.Constructor(Vector3(4078.637f, 5919.208f, 39.03953f), repair_silo),
        owning_building_guid = 45
      )
      LocalObject(
        2124,
        Terminal.Constructor(Vector3(4078.637f, 5919.208f, 39.03953f), ground_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1486,
        FacilityTurret.Constructor(Vector3(3935.906f, 6110.855f, 47.78853f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1486, 5053)
      LocalObject(
        1487,
        FacilityTurret.Constructor(Vector3(3941.413f, 5912.665f, 47.78853f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1487, 5054)
      LocalObject(
        1488,
        FacilityTurret.Constructor(Vector3(4025.601f, 6110.855f, 47.78853f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1488, 5055)
      LocalObject(
        1489,
        FacilityTurret.Constructor(Vector3(4106.154f, 5912.657f, 47.78853f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1489, 5056)
      LocalObject(
        1490,
        FacilityTurret.Constructor(Vector3(4106.154f, 6050.398f, 47.78853f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1490, 5057)
      LocalObject(
        1491,
        FacilityTurret.Constructor(Vector3(4113.881f, 5977.423f, 47.78853f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1491, 5058)
      LocalObject(
        1946,
        Painbox.Constructor(Vector3(4045.737f, 6008.207f, 27.86283f), painbox),
        owning_building_guid = 45
      )
      LocalObject(
        1957,
        Painbox.Constructor(Vector3(4040.832f, 5975.212f, 36.15943f), painbox_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        1968,
        Painbox.Constructor(Vector3(4047.7f, 5993.471f, 27.54893f), painbox_door_radius),
        owning_building_guid = 45
      )
      LocalObject(
        1989,
        Painbox.Constructor(Vector3(4039.035f, 5962.278f, 34.01743f), painbox_door_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        1990,
        Painbox.Constructor(Vector3(4039.861f, 5989.769f, 34.56573f), painbox_door_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        1991,
        Painbox.Constructor(Vector3(4055.641f, 5982.57f, 35.47183f), painbox_door_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(256, Generator.Constructor(Vector3(4047.975f, 6011.555f, 23.09553f)), owning_building_guid = 45)
      LocalObject(
        245,
        Terminal.Constructor(Vector3(4048.022f, 6003.363f, 24.38953f), gen_control),
        owning_building_guid = 45
      )
    }

    Building14()

    def Building14(): Unit = { // Name: Mulac Type: tech_plant GUID: 48, MapID: 14
      LocalBuilding(
        "Mulac",
        48,
        14,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5668f, 2824f, 36.97623f),
            Vector3(0f, 0f, 268f),
            tech_plant
          )
        )
      )
      LocalObject(
        192,
        CaptureTerminal.Constructor(Vector3(5623.772f, 2820.808f, 52.07623f), capture_terminal),
        owning_building_guid = 48
      )
      LocalObject(496, Door.Constructor(Vector3(5569.457f, 2867.466f, 38.59723f)), owning_building_guid = 48)
      LocalObject(498, Door.Constructor(Vector3(5576.697f, 2822.581f, 38.62723f)), owning_building_guid = 48)
      LocalObject(499, Door.Constructor(Vector3(5577.332f, 2840.762f, 46.59023f)), owning_building_guid = 48)
      LocalObject(500, Door.Constructor(Vector3(5600.466f, 2897.862f, 38.51823f)), owning_building_guid = 48)
      LocalObject(501, Door.Constructor(Vector3(5618.646f, 2897.227f, 46.48123f)), owning_building_guid = 48)
      LocalObject(502, Door.Constructor(Vector3(5627.989f, 2807.943f, 53.59723f)), owning_building_guid = 48)
      LocalObject(503, Door.Constructor(Vector3(5644.219f, 2814.227f, 53.59723f)), owning_building_guid = 48)
      LocalObject(504, Door.Constructor(Vector3(5650.166f, 2745.555f, 46.48123f)), owning_building_guid = 48)
      LocalObject(505, Door.Constructor(Vector3(5668.347f, 2744.92f, 38.51823f)), owning_building_guid = 48)
      LocalObject(506, Door.Constructor(Vector3(5674.165f, 2895.288f, 38.51823f)), owning_building_guid = 48)
      LocalObject(507, Door.Constructor(Vector3(5692.346f, 2894.653f, 46.48123f)), owning_building_guid = 48)
      LocalObject(508, Door.Constructor(Vector3(5697.755f, 2780.183f, 46.48123f)), owning_building_guid = 48)
      LocalObject(509, Door.Constructor(Vector3(5698.39f, 2798.365f, 38.51823f)), owning_building_guid = 48)
      LocalObject(538, Door.Constructor(Vector3(5709.653f, 2870.575f, 40.71323f)), owning_building_guid = 48)
      LocalObject(541, Door.Constructor(Vector3(5653.685f, 2872.529f, 20.71323f)), owning_building_guid = 48)
      LocalObject(741, Door.Constructor(Vector3(5576.614f, 2843.201f, 31.09723f)), owning_building_guid = 48)
      LocalObject(742, Door.Constructor(Vector3(5592.046f, 2826.652f, 31.09723f)), owning_building_guid = 48)
      LocalObject(743, Door.Constructor(Vector3(5602.275f, 2890.334f, 31.09723f)), owning_building_guid = 48)
      LocalObject(744, Door.Constructor(Vector3(5616.59f, 2841.805f, 31.09723f)), owning_building_guid = 48)
      LocalObject(745, Door.Constructor(Vector3(5619.331f, 2805.687f, 23.59723f)), owning_building_guid = 48)
      LocalObject(746, Door.Constructor(Vector3(5619.331f, 2805.687f, 31.09723f)), owning_building_guid = 48)
      LocalObject(747, Door.Constructor(Vector3(5622.91f, 2793.555f, 23.59723f)), owning_building_guid = 48)
      LocalObject(748, Door.Constructor(Vector3(5625.144f, 2857.516f, 28.59723f)), owning_building_guid = 48)
      LocalObject(749, Door.Constructor(Vector3(5626.26f, 2889.497f, 28.59723f)), owning_building_guid = 48)
      LocalObject(750, Door.Constructor(Vector3(5628.164f, 2829.394f, 33.59723f)), owning_building_guid = 48)
      LocalObject(751, Door.Constructor(Vector3(5628.164f, 2829.394f, 53.59723f)), owning_building_guid = 48)
      LocalObject(752, Door.Constructor(Vector3(5632.022f, 2825.256f, 23.59723f)), owning_building_guid = 48)
      LocalObject(753, Door.Constructor(Vector3(5636.02f, 2825.117f, 43.59723f)), owning_building_guid = 48)
      LocalObject(754, Door.Constructor(Vector3(5638.9f, 2792.997f, 31.09723f)), owning_building_guid = 48)
      LocalObject(755, Door.Constructor(Vector3(5640.017f, 2824.977f, 53.59723f)), owning_building_guid = 48)
      LocalObject(756, Door.Constructor(Vector3(5643.316f, 2804.85f, 31.09723f)), owning_building_guid = 48)
      LocalObject(757, Door.Constructor(Vector3(5644.154f, 2828.835f, 33.59723f)), owning_building_guid = 48)
      LocalObject(758, Door.Constructor(Vector3(5649.129f, 2856.678f, 28.59723f)), owning_building_guid = 48)
      LocalObject(759, Door.Constructor(Vector3(5651.033f, 2796.575f, 23.59723f)), owning_building_guid = 48)
      LocalObject(792, Door.Constructor(Vector3(5620.965f, 2785.405f, 39.35623f)), owning_building_guid = 48)
      LocalObject(2427, Door.Constructor(Vector3(5625.316f, 2812.81f, 31.43023f)), owning_building_guid = 48)
      LocalObject(2428, Door.Constructor(Vector3(5632.605f, 2812.555f, 31.43023f)), owning_building_guid = 48)
      LocalObject(2429, Door.Constructor(Vector3(5639.89f, 2812.301f, 31.43023f)), owning_building_guid = 48)
      LocalObject(
        832,
        IFFLock.Constructor(Vector3(5618.119f, 2782.358f, 38.55623f), Vector3(0, 0, 272)),
        owning_building_guid = 48,
        door_guid = 792
      )
      LocalObject(
        836,
        IFFLock.Constructor(Vector3(5711.819f, 2865.24f, 38.66423f), Vector3(0, 0, 92)),
        owning_building_guid = 48,
        door_guid = 538
      )
      LocalObject(
        1023,
        IFFLock.Constructor(Vector3(5567.44f, 2868.351f, 38.52823f), Vector3(0, 0, 2)),
        owning_building_guid = 48,
        door_guid = 496
      )
      LocalObject(
        1024,
        IFFLock.Constructor(Vector3(5618.467f, 2804.145f, 30.91223f), Vector3(0, 0, 272)),
        owning_building_guid = 48,
        door_guid = 746
      )
      LocalObject(
        1025,
        IFFLock.Constructor(Vector3(5621.372f, 2794.549f, 23.41223f), Vector3(0, 0, 2)),
        owning_building_guid = 48,
        door_guid = 747
      )
      LocalObject(
        1026,
        IFFLock.Constructor(Vector3(5627.119f, 2805.92f, 53.52222f), Vector3(0, 0, 272)),
        owning_building_guid = 48,
        door_guid = 502
      )
      LocalObject(
        1027,
        IFFLock.Constructor(Vector3(5629.158f, 2830.932f, 53.41223f), Vector3(0, 0, 92)),
        owning_building_guid = 48,
        door_guid = 751
      )
      LocalObject(
        1028,
        IFFLock.Constructor(Vector3(5644.181f, 2806.393f, 30.91223f), Vector3(0, 0, 92)),
        owning_building_guid = 48,
        door_guid = 756
      )
      LocalObject(
        1029,
        IFFLock.Constructor(Vector3(5645.098f, 2816.24f, 53.52222f), Vector3(0, 0, 92)),
        owning_building_guid = 48,
        door_guid = 503
      )
      LocalObject(
        1030,
        IFFLock.Constructor(Vector3(5650.035f, 2795.037f, 23.41223f), Vector3(0, 0, 272)),
        owning_building_guid = 48,
        door_guid = 759
      )
      LocalObject(1347, Locker.Constructor(Vector3(5621.226f, 2798.594f, 29.83723f)), owning_building_guid = 48)
      LocalObject(1348, Locker.Constructor(Vector3(5621.266f, 2799.742f, 29.83723f)), owning_building_guid = 48)
      LocalObject(1349, Locker.Constructor(Vector3(5621.306f, 2800.888f, 29.83723f)), owning_building_guid = 48)
      LocalObject(1350, Locker.Constructor(Vector3(5621.347f, 2802.052f, 29.83723f)), owning_building_guid = 48)
      LocalObject(1351, Locker.Constructor(Vector3(5640.55f, 2778.202f, 22.07623f)), owning_building_guid = 48)
      LocalObject(1352, Locker.Constructor(Vector3(5640.597f, 2779.538f, 22.07623f)), owning_building_guid = 48)
      LocalObject(1353, Locker.Constructor(Vector3(5640.643f, 2780.874f, 22.07623f)), owning_building_guid = 48)
      LocalObject(1354, Locker.Constructor(Vector3(5640.689f, 2782.197f, 22.07623f)), owning_building_guid = 48)
      LocalObject(1355, Locker.Constructor(Vector3(5640.848f, 2786.734f, 22.07623f)), owning_building_guid = 48)
      LocalObject(1356, Locker.Constructor(Vector3(5640.895f, 2788.07f, 22.07623f)), owning_building_guid = 48)
      LocalObject(1357, Locker.Constructor(Vector3(5640.941f, 2789.405f, 22.07623f)), owning_building_guid = 48)
      LocalObject(1358, Locker.Constructor(Vector3(5640.987f, 2790.729f, 22.07623f)), owning_building_guid = 48)
      LocalObject(
        146,
        Terminal.Constructor(Vector3(5642.577f, 2808.273f, 52.67923f), air_vehicle_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1552,
        VehicleSpawnPad.Constructor(Vector3(5663.102f, 2803.07f, 49.55423f), mb_pad_creation, Vector3(0, 0, 92)),
        owning_building_guid = 48,
        terminal_guid = 146
      )
      LocalObject(
        147,
        Terminal.Constructor(Vector3(5642.993f, 2820.198f, 52.67923f), air_vehicle_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1553,
        VehicleSpawnPad.Constructor(Vector3(5663.835f, 2824.075f, 49.55423f), mb_pad_creation, Vector3(0, 0, 92)),
        owning_building_guid = 48,
        terminal_guid = 147
      )
      LocalObject(
        1688,
        Terminal.Constructor(Vector3(5626.502f, 2798.779f, 31.16623f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1689,
        Terminal.Constructor(Vector3(5630.231f, 2798.649f, 31.16623f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1690,
        Terminal.Constructor(Vector3(5634.018f, 2798.516f, 31.16623f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1691,
        Terminal.Constructor(Vector3(5641.396f, 2821.869f, 43.40623f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2344,
        Terminal.Constructor(Vector3(5573.489f, 2851.373f, 31.13323f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2345,
        Terminal.Constructor(Vector3(5620.343f, 2817.717f, 23.63323f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2346,
        Terminal.Constructor(Vector3(5622.817f, 2812.599f, 31.71023f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2347,
        Terminal.Constructor(Vector3(5630.105f, 2812.348f, 31.71023f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2348,
        Terminal.Constructor(Vector3(5637.389f, 2812.091f, 31.71023f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2349,
        Terminal.Constructor(Vector3(5646.352f, 2787.491f, 43.65823f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2350,
        Terminal.Constructor(Vector3(5670.706f, 2809.365f, 48.99923f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2514,
        Terminal.Constructor(Vector3(5629.123f, 2873.391f, 22.79023f), ground_vehicle_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1551,
        VehicleSpawnPad.Constructor(Vector3(5640.034f, 2873.061f, 14.51323f), mb_pad_creation, Vector3(0, 0, 92)),
        owning_building_guid = 48,
        terminal_guid = 2514
      )
      LocalObject(2176, ResourceSilo.Constructor(Vector3(5698.38f, 2761.149f, 43.98423f)), owning_building_guid = 48)
      LocalObject(
        2247,
        SpawnTube.Constructor(Vector3(5624.282f, 2813.286f, 29.57623f), Vector3(0, 0, 92)),
        owning_building_guid = 48
      )
      LocalObject(
        2248,
        SpawnTube.Constructor(Vector3(5631.569f, 2813.032f, 29.57623f), Vector3(0, 0, 92)),
        owning_building_guid = 48
      )
      LocalObject(
        2249,
        SpawnTube.Constructor(Vector3(5638.853f, 2812.777f, 29.57623f), Vector3(0, 0, 92)),
        owning_building_guid = 48
      )
      LocalObject(
        1571,
        ProximityTerminal.Constructor(Vector3(5630.817f, 2822.238f, 42.07323f), medical_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1572,
        ProximityTerminal.Constructor(Vector3(5640.225f, 2784.501f, 22.07623f), medical_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1857,
        ProximityTerminal.Constructor(Vector3(5585.787f, 2801.321f, 45.18423f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        1858,
        Terminal.Constructor(Vector3(5585.787f, 2801.321f, 45.18423f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1860,
        ProximityTerminal.Constructor(Vector3(5603.186f, 2816.879f, 52.42323f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        1861,
        Terminal.Constructor(Vector3(5603.186f, 2816.879f, 52.42323f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1863,
        ProximityTerminal.Constructor(Vector3(5623.717f, 2763.521f, 47.62823f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        1864,
        Terminal.Constructor(Vector3(5623.717f, 2763.521f, 47.62823f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1866,
        ProximityTerminal.Constructor(Vector3(5639.735f, 2756.666f, 45.18423f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        1867,
        Terminal.Constructor(Vector3(5639.735f, 2756.666f, 45.18423f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1869,
        ProximityTerminal.Constructor(Vector3(5645.815f, 2886.108f, 45.17123f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        1870,
        Terminal.Constructor(Vector3(5645.815f, 2886.108f, 45.17123f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1872,
        ProximityTerminal.Constructor(Vector3(5661.27f, 2865.28f, 47.52623f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        1873,
        Terminal.Constructor(Vector3(5661.27f, 2865.28f, 47.52623f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2151,
        ProximityTerminal.Constructor(Vector3(5573.218f, 2768.637f, 36.72623f), repair_silo),
        owning_building_guid = 48
      )
      LocalObject(
        2152,
        Terminal.Constructor(Vector3(5573.218f, 2768.637f, 36.72623f), ground_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2155,
        ProximityTerminal.Constructor(Vector3(5709.601f, 2822.239f, 36.70473f), repair_silo),
        owning_building_guid = 48
      )
      LocalObject(
        2156,
        Terminal.Constructor(Vector3(5709.601f, 2822.239f, 36.70473f), ground_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1522,
        FacilityTurret.Constructor(Vector3(5565.711f, 2741.365f, 45.47523f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1522, 5059)
      LocalObject(
        1523,
        FacilityTurret.Constructor(Vector3(5571.468f, 2906.006f, 45.47523f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1523, 5060)
      LocalObject(
        1525,
        FacilityTurret.Constructor(Vector3(5630.167f, 2731.383f, 45.47523f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1525, 5061)
      LocalObject(
        1526,
        FacilityTurret.Constructor(Vector3(5703.368f, 2736.558f, 45.47523f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1526, 5062)
      LocalObject(
        1527,
        FacilityTurret.Constructor(Vector3(5766.599f, 2814.952f, 45.47523f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1527, 5063)
      LocalObject(
        1528,
        FacilityTurret.Constructor(Vector3(5769.729f, 2904.593f, 45.47523f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1528, 5064)
      LocalObject(
        1950,
        Painbox.Constructor(Vector3(5663.311f, 2798.411f, 25.54953f), painbox),
        owning_building_guid = 48
      )
      LocalObject(
        1961,
        Painbox.Constructor(Vector3(5630.508f, 2804.464f, 33.84613f), painbox_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        1972,
        Painbox.Constructor(Vector3(5648.516f, 2796.964f, 25.23563f), painbox_door_radius),
        owning_building_guid = 48
      )
      LocalObject(
        2001,
        Painbox.Constructor(Vector3(5617.645f, 2806.712f, 31.70413f), painbox_door_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        2002,
        Painbox.Constructor(Vector3(5637.344f, 2789.408f, 33.15853f), painbox_door_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        2003,
        Painbox.Constructor(Vector3(5645.089f, 2804.927f, 32.25243f), painbox_door_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(260, Generator.Constructor(Vector3(5666.579f, 2796.058f, 20.78223f)), owning_building_guid = 48)
      LocalObject(
        249,
        Terminal.Constructor(Vector3(5658.39f, 2796.296f, 22.07623f), gen_control),
        owning_building_guid = 48
      )
    }

    Building15()

    def Building15(): Unit = { // Name: S_Ceryshen_Warpgate_Tower Type: tower_a GUID: 51, MapID: 15
      LocalBuilding(
        "S_Ceryshen_Warpgate_Tower",
        51,
        15,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1816f, 4282f, 45.38713f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2259,
        CaptureTerminal.Constructor(Vector3(1832.587f, 4281.897f, 55.38613f), secondary_capture),
        owning_building_guid = 51
      )
      LocalObject(262, Door.Constructor(Vector3(1828f, 4274f, 46.90813f)), owning_building_guid = 51)
      LocalObject(263, Door.Constructor(Vector3(1828f, 4274f, 66.90713f)), owning_building_guid = 51)
      LocalObject(264, Door.Constructor(Vector3(1828f, 4290f, 46.90813f)), owning_building_guid = 51)
      LocalObject(265, Door.Constructor(Vector3(1828f, 4290f, 66.90713f)), owning_building_guid = 51)
      LocalObject(2358, Door.Constructor(Vector3(1827.146f, 4270.794f, 36.72313f)), owning_building_guid = 51)
      LocalObject(2359, Door.Constructor(Vector3(1827.146f, 4287.204f, 36.72313f)), owning_building_guid = 51)
      LocalObject(
        837,
        IFFLock.Constructor(Vector3(1825.957f, 4290.811f, 46.84813f), Vector3(0, 0, 0)),
        owning_building_guid = 51,
        door_guid = 264
      )
      LocalObject(
        838,
        IFFLock.Constructor(Vector3(1825.957f, 4290.811f, 66.84813f), Vector3(0, 0, 0)),
        owning_building_guid = 51,
        door_guid = 265
      )
      LocalObject(
        839,
        IFFLock.Constructor(Vector3(1830.047f, 4273.189f, 46.84813f), Vector3(0, 0, 180)),
        owning_building_guid = 51,
        door_guid = 262
      )
      LocalObject(
        840,
        IFFLock.Constructor(Vector3(1830.047f, 4273.189f, 66.84813f), Vector3(0, 0, 180)),
        owning_building_guid = 51,
        door_guid = 263
      )
      LocalObject(1053, Locker.Constructor(Vector3(1831.716f, 4266.963f, 35.38113f)), owning_building_guid = 51)
      LocalObject(1054, Locker.Constructor(Vector3(1831.751f, 4288.835f, 35.38113f)), owning_building_guid = 51)
      LocalObject(1055, Locker.Constructor(Vector3(1833.053f, 4266.963f, 35.38113f)), owning_building_guid = 51)
      LocalObject(1056, Locker.Constructor(Vector3(1833.088f, 4288.835f, 35.38113f)), owning_building_guid = 51)
      LocalObject(1057, Locker.Constructor(Vector3(1835.741f, 4266.963f, 35.38113f)), owning_building_guid = 51)
      LocalObject(1058, Locker.Constructor(Vector3(1835.741f, 4288.835f, 35.38113f)), owning_building_guid = 51)
      LocalObject(1059, Locker.Constructor(Vector3(1837.143f, 4266.963f, 35.38113f)), owning_building_guid = 51)
      LocalObject(1060, Locker.Constructor(Vector3(1837.143f, 4288.835f, 35.38113f)), owning_building_guid = 51)
      LocalObject(
        1574,
        Terminal.Constructor(Vector3(1837.445f, 4272.129f, 36.71913f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1575,
        Terminal.Constructor(Vector3(1837.445f, 4277.853f, 36.71913f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1576,
        Terminal.Constructor(Vector3(1837.445f, 4283.234f, 36.71913f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2178,
        SpawnTube.Constructor(Vector3(1826.706f, 4269.742f, 34.86913f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 51
      )
      LocalObject(
        2179,
        SpawnTube.Constructor(Vector3(1826.706f, 4286.152f, 34.86913f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 51
      )
      LocalObject(
        1439,
        FacilityTurret.Constructor(Vector3(1803.32f, 4269.295f, 64.32912f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1439, 5065)
      LocalObject(
        1442,
        FacilityTurret.Constructor(Vector3(1838.647f, 4294.707f, 64.32912f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1442, 5066)
      LocalObject(
        2007,
        Painbox.Constructor(Vector3(1821.235f, 4275.803f, 36.88623f), painbox_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        2008,
        Painbox.Constructor(Vector3(1832.889f, 4284.086f, 35.48713f), painbox_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        2009,
        Painbox.Constructor(Vector3(1832.975f, 4272.223f, 35.48713f), painbox_radius_continuous),
        owning_building_guid = 51
      )
    }

    Building17()

    def Building17(): Unit = { // Name: NE_Ceryshen_Warpgate_Tower Type: tower_a GUID: 52, MapID: 17
      LocalBuilding(
        "NE_Ceryshen_Warpgate_Tower",
        52,
        17,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2196f, 5162f, 30.38423f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2261,
        CaptureTerminal.Constructor(Vector3(2212.587f, 5161.897f, 40.38323f), secondary_capture),
        owning_building_guid = 52
      )
      LocalObject(285, Door.Constructor(Vector3(2208f, 5154f, 31.90523f)), owning_building_guid = 52)
      LocalObject(286, Door.Constructor(Vector3(2208f, 5154f, 51.90423f)), owning_building_guid = 52)
      LocalObject(287, Door.Constructor(Vector3(2208f, 5170f, 31.90523f)), owning_building_guid = 52)
      LocalObject(288, Door.Constructor(Vector3(2208f, 5170f, 51.90423f)), owning_building_guid = 52)
      LocalObject(2365, Door.Constructor(Vector3(2207.146f, 5150.794f, 21.72023f)), owning_building_guid = 52)
      LocalObject(2366, Door.Constructor(Vector3(2207.146f, 5167.204f, 21.72023f)), owning_building_guid = 52)
      LocalObject(
        858,
        IFFLock.Constructor(Vector3(2205.957f, 5170.811f, 31.84523f), Vector3(0, 0, 0)),
        owning_building_guid = 52,
        door_guid = 287
      )
      LocalObject(
        859,
        IFFLock.Constructor(Vector3(2205.957f, 5170.811f, 51.84523f), Vector3(0, 0, 0)),
        owning_building_guid = 52,
        door_guid = 288
      )
      LocalObject(
        860,
        IFFLock.Constructor(Vector3(2210.047f, 5153.189f, 31.84523f), Vector3(0, 0, 180)),
        owning_building_guid = 52,
        door_guid = 285
      )
      LocalObject(
        861,
        IFFLock.Constructor(Vector3(2210.047f, 5153.189f, 51.84523f), Vector3(0, 0, 180)),
        owning_building_guid = 52,
        door_guid = 286
      )
      LocalObject(1081, Locker.Constructor(Vector3(2211.716f, 5146.963f, 20.37823f)), owning_building_guid = 52)
      LocalObject(1082, Locker.Constructor(Vector3(2211.751f, 5168.835f, 20.37823f)), owning_building_guid = 52)
      LocalObject(1083, Locker.Constructor(Vector3(2213.053f, 5146.963f, 20.37823f)), owning_building_guid = 52)
      LocalObject(1084, Locker.Constructor(Vector3(2213.088f, 5168.835f, 20.37823f)), owning_building_guid = 52)
      LocalObject(1085, Locker.Constructor(Vector3(2215.741f, 5146.963f, 20.37823f)), owning_building_guid = 52)
      LocalObject(1086, Locker.Constructor(Vector3(2215.741f, 5168.835f, 20.37823f)), owning_building_guid = 52)
      LocalObject(1087, Locker.Constructor(Vector3(2217.143f, 5146.963f, 20.37823f)), owning_building_guid = 52)
      LocalObject(1088, Locker.Constructor(Vector3(2217.143f, 5168.835f, 20.37823f)), owning_building_guid = 52)
      LocalObject(
        1587,
        Terminal.Constructor(Vector3(2217.445f, 5152.129f, 21.71623f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1588,
        Terminal.Constructor(Vector3(2217.445f, 5157.853f, 21.71623f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1589,
        Terminal.Constructor(Vector3(2217.445f, 5163.234f, 21.71623f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        2185,
        SpawnTube.Constructor(Vector3(2206.706f, 5149.742f, 19.86623f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 52
      )
      LocalObject(
        2186,
        SpawnTube.Constructor(Vector3(2206.706f, 5166.152f, 19.86623f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 52
      )
      LocalObject(
        1447,
        FacilityTurret.Constructor(Vector3(2183.32f, 5149.295f, 49.32623f), manned_turret),
        owning_building_guid = 52
      )
      TurretToWeapon(1447, 5067)
      LocalObject(
        1448,
        FacilityTurret.Constructor(Vector3(2218.647f, 5174.707f, 49.32623f), manned_turret),
        owning_building_guid = 52
      )
      TurretToWeapon(1448, 5068)
      LocalObject(
        2013,
        Painbox.Constructor(Vector3(2201.235f, 5155.803f, 21.88333f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        2014,
        Painbox.Constructor(Vector3(2212.889f, 5164.086f, 20.48423f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        2015,
        Painbox.Constructor(Vector3(2212.975f, 5152.223f, 20.48423f), painbox_radius_continuous),
        owning_building_guid = 52
      )
    }

    Building28()

    def Building28(): Unit = { // Name: W_Oshur_Warpgate_Tower Type: tower_a GUID: 53, MapID: 28
      LocalBuilding(
        "W_Oshur_Warpgate_Tower",
        53,
        28,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2506f, 2050f, 30.77677f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2262,
        CaptureTerminal.Constructor(Vector3(2522.587f, 2049.897f, 40.77577f), secondary_capture),
        owning_building_guid = 53
      )
      LocalObject(300, Door.Constructor(Vector3(2518f, 2042f, 32.29778f)), owning_building_guid = 53)
      LocalObject(301, Door.Constructor(Vector3(2518f, 2042f, 52.29678f)), owning_building_guid = 53)
      LocalObject(302, Door.Constructor(Vector3(2518f, 2058f, 32.29778f)), owning_building_guid = 53)
      LocalObject(303, Door.Constructor(Vector3(2518f, 2058f, 52.29678f)), owning_building_guid = 53)
      LocalObject(2370, Door.Constructor(Vector3(2517.146f, 2038.794f, 22.11277f)), owning_building_guid = 53)
      LocalObject(2371, Door.Constructor(Vector3(2517.146f, 2055.204f, 22.11277f)), owning_building_guid = 53)
      LocalObject(
        870,
        IFFLock.Constructor(Vector3(2515.957f, 2058.811f, 32.23777f), Vector3(0, 0, 0)),
        owning_building_guid = 53,
        door_guid = 302
      )
      LocalObject(
        871,
        IFFLock.Constructor(Vector3(2515.957f, 2058.811f, 52.23777f), Vector3(0, 0, 0)),
        owning_building_guid = 53,
        door_guid = 303
      )
      LocalObject(
        872,
        IFFLock.Constructor(Vector3(2520.047f, 2041.189f, 32.23777f), Vector3(0, 0, 180)),
        owning_building_guid = 53,
        door_guid = 300
      )
      LocalObject(
        873,
        IFFLock.Constructor(Vector3(2520.047f, 2041.189f, 52.23777f), Vector3(0, 0, 180)),
        owning_building_guid = 53,
        door_guid = 301
      )
      LocalObject(1097, Locker.Constructor(Vector3(2521.716f, 2034.963f, 20.77077f)), owning_building_guid = 53)
      LocalObject(1098, Locker.Constructor(Vector3(2521.751f, 2056.835f, 20.77077f)), owning_building_guid = 53)
      LocalObject(1099, Locker.Constructor(Vector3(2523.053f, 2034.963f, 20.77077f)), owning_building_guid = 53)
      LocalObject(1100, Locker.Constructor(Vector3(2523.088f, 2056.835f, 20.77077f)), owning_building_guid = 53)
      LocalObject(1102, Locker.Constructor(Vector3(2525.741f, 2034.963f, 20.77077f)), owning_building_guid = 53)
      LocalObject(1103, Locker.Constructor(Vector3(2525.741f, 2056.835f, 20.77077f)), owning_building_guid = 53)
      LocalObject(1105, Locker.Constructor(Vector3(2527.143f, 2034.963f, 20.77077f)), owning_building_guid = 53)
      LocalObject(1106, Locker.Constructor(Vector3(2527.143f, 2056.835f, 20.77077f)), owning_building_guid = 53)
      LocalObject(
        1594,
        Terminal.Constructor(Vector3(2527.445f, 2040.129f, 22.10877f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1595,
        Terminal.Constructor(Vector3(2527.445f, 2045.853f, 22.10877f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1596,
        Terminal.Constructor(Vector3(2527.445f, 2051.234f, 22.10877f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2190,
        SpawnTube.Constructor(Vector3(2516.706f, 2037.742f, 20.25877f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 53
      )
      LocalObject(
        2191,
        SpawnTube.Constructor(Vector3(2516.706f, 2054.152f, 20.25877f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 53
      )
      LocalObject(
        1452,
        FacilityTurret.Constructor(Vector3(2493.32f, 2037.295f, 49.71877f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1452, 5069)
      LocalObject(
        1453,
        FacilityTurret.Constructor(Vector3(2528.647f, 2062.707f, 49.71877f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1453, 5070)
      LocalObject(
        2016,
        Painbox.Constructor(Vector3(2511.235f, 2043.803f, 22.27587f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        2017,
        Painbox.Constructor(Vector3(2522.889f, 2052.086f, 20.87677f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        2018,
        Painbox.Constructor(Vector3(2522.975f, 2040.223f, 20.87677f), painbox_radius_continuous),
        owning_building_guid = 53
      )
    }

    Building49()

    def Building49(): Unit = { // Name: Ixtab_tower Type: tower_a GUID: 54, MapID: 49
      LocalBuilding(
        "Ixtab_tower",
        54,
        49,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3652f, 3042f, 38.52847f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2269,
        CaptureTerminal.Constructor(Vector3(3668.587f, 3041.897f, 48.52747f), secondary_capture),
        owning_building_guid = 54
      )
      LocalObject(385, Door.Constructor(Vector3(3664f, 3034f, 40.04947f)), owning_building_guid = 54)
      LocalObject(386, Door.Constructor(Vector3(3664f, 3034f, 60.04847f)), owning_building_guid = 54)
      LocalObject(387, Door.Constructor(Vector3(3664f, 3050f, 40.04947f)), owning_building_guid = 54)
      LocalObject(388, Door.Constructor(Vector3(3664f, 3050f, 60.04847f)), owning_building_guid = 54)
      LocalObject(2393, Door.Constructor(Vector3(3663.146f, 3030.794f, 29.86447f)), owning_building_guid = 54)
      LocalObject(2394, Door.Constructor(Vector3(3663.146f, 3047.204f, 29.86447f)), owning_building_guid = 54)
      LocalObject(
        934,
        IFFLock.Constructor(Vector3(3661.957f, 3050.811f, 39.98947f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 387
      )
      LocalObject(
        935,
        IFFLock.Constructor(Vector3(3661.957f, 3050.811f, 59.98947f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 388
      )
      LocalObject(
        936,
        IFFLock.Constructor(Vector3(3666.047f, 3033.189f, 39.98947f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 385
      )
      LocalObject(
        937,
        IFFLock.Constructor(Vector3(3666.047f, 3033.189f, 59.98947f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 386
      )
      LocalObject(1202, Locker.Constructor(Vector3(3667.716f, 3026.963f, 28.52247f)), owning_building_guid = 54)
      LocalObject(1203, Locker.Constructor(Vector3(3667.751f, 3048.835f, 28.52247f)), owning_building_guid = 54)
      LocalObject(1204, Locker.Constructor(Vector3(3669.053f, 3026.963f, 28.52247f)), owning_building_guid = 54)
      LocalObject(1205, Locker.Constructor(Vector3(3669.088f, 3048.835f, 28.52247f)), owning_building_guid = 54)
      LocalObject(1206, Locker.Constructor(Vector3(3671.741f, 3026.963f, 28.52247f)), owning_building_guid = 54)
      LocalObject(1207, Locker.Constructor(Vector3(3671.741f, 3048.835f, 28.52247f)), owning_building_guid = 54)
      LocalObject(1208, Locker.Constructor(Vector3(3673.143f, 3026.963f, 28.52247f)), owning_building_guid = 54)
      LocalObject(1209, Locker.Constructor(Vector3(3673.143f, 3048.835f, 28.52247f)), owning_building_guid = 54)
      LocalObject(
        1632,
        Terminal.Constructor(Vector3(3673.445f, 3032.129f, 29.86047f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1633,
        Terminal.Constructor(Vector3(3673.445f, 3037.853f, 29.86047f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1634,
        Terminal.Constructor(Vector3(3673.445f, 3043.234f, 29.86047f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2213,
        SpawnTube.Constructor(Vector3(3662.706f, 3029.742f, 28.01047f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        2214,
        SpawnTube.Constructor(Vector3(3662.706f, 3046.152f, 28.01047f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        1480,
        FacilityTurret.Constructor(Vector3(3639.32f, 3029.295f, 57.47047f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(1480, 5071)
      LocalObject(
        1481,
        FacilityTurret.Constructor(Vector3(3674.647f, 3054.707f, 57.47047f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(1481, 5072)
      LocalObject(
        2037,
        Painbox.Constructor(Vector3(3657.235f, 3035.803f, 30.02757f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        2038,
        Painbox.Constructor(Vector3(3668.889f, 3044.086f, 28.62847f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        2039,
        Painbox.Constructor(Vector3(3668.975f, 3032.223f, 28.62847f), painbox_radius_continuous),
        owning_building_guid = 54
      )
    }

    Building32()

    def Building32(): Unit = { // Name: S_Acan_Tower Type: tower_a GUID: 55, MapID: 32
      LocalBuilding(
        "S_Acan_Tower",
        55,
        32,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3834f, 4050f, 25.81256f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2271,
        CaptureTerminal.Constructor(Vector3(3850.587f, 4049.897f, 35.81155f), secondary_capture),
        owning_building_guid = 55
      )
      LocalObject(394, Door.Constructor(Vector3(3846f, 4042f, 27.33356f)), owning_building_guid = 55)
      LocalObject(395, Door.Constructor(Vector3(3846f, 4042f, 47.33256f)), owning_building_guid = 55)
      LocalObject(396, Door.Constructor(Vector3(3846f, 4058f, 27.33356f)), owning_building_guid = 55)
      LocalObject(397, Door.Constructor(Vector3(3846f, 4058f, 47.33256f)), owning_building_guid = 55)
      LocalObject(2397, Door.Constructor(Vector3(3845.146f, 4038.794f, 17.14856f)), owning_building_guid = 55)
      LocalObject(2398, Door.Constructor(Vector3(3845.146f, 4055.204f, 17.14856f)), owning_building_guid = 55)
      LocalObject(
        942,
        IFFLock.Constructor(Vector3(3843.957f, 4058.811f, 27.27356f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 396
      )
      LocalObject(
        943,
        IFFLock.Constructor(Vector3(3843.957f, 4058.811f, 47.27356f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 397
      )
      LocalObject(
        944,
        IFFLock.Constructor(Vector3(3848.047f, 4041.189f, 27.27356f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 394
      )
      LocalObject(
        945,
        IFFLock.Constructor(Vector3(3848.047f, 4041.189f, 47.27356f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 395
      )
      LocalObject(1218, Locker.Constructor(Vector3(3849.716f, 4034.963f, 15.80656f)), owning_building_guid = 55)
      LocalObject(1219, Locker.Constructor(Vector3(3849.751f, 4056.835f, 15.80656f)), owning_building_guid = 55)
      LocalObject(1220, Locker.Constructor(Vector3(3851.053f, 4034.963f, 15.80656f)), owning_building_guid = 55)
      LocalObject(1221, Locker.Constructor(Vector3(3851.088f, 4056.835f, 15.80656f)), owning_building_guid = 55)
      LocalObject(1222, Locker.Constructor(Vector3(3853.741f, 4034.963f, 15.80656f)), owning_building_guid = 55)
      LocalObject(1223, Locker.Constructor(Vector3(3853.741f, 4056.835f, 15.80656f)), owning_building_guid = 55)
      LocalObject(1224, Locker.Constructor(Vector3(3855.143f, 4034.963f, 15.80656f)), owning_building_guid = 55)
      LocalObject(1225, Locker.Constructor(Vector3(3855.143f, 4056.835f, 15.80656f)), owning_building_guid = 55)
      LocalObject(
        1638,
        Terminal.Constructor(Vector3(3855.445f, 4040.129f, 17.14455f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1639,
        Terminal.Constructor(Vector3(3855.445f, 4045.853f, 17.14455f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1640,
        Terminal.Constructor(Vector3(3855.445f, 4051.234f, 17.14455f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        2217,
        SpawnTube.Constructor(Vector3(3844.706f, 4037.742f, 15.29456f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        2218,
        SpawnTube.Constructor(Vector3(3844.706f, 4054.152f, 15.29456f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        1484,
        FacilityTurret.Constructor(Vector3(3821.32f, 4037.295f, 44.75455f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1484, 5073)
      LocalObject(
        1485,
        FacilityTurret.Constructor(Vector3(3856.647f, 4062.707f, 44.75455f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1485, 5074)
      LocalObject(
        2043,
        Painbox.Constructor(Vector3(3839.235f, 4043.803f, 17.31166f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        2044,
        Painbox.Constructor(Vector3(3850.889f, 4052.086f, 15.91256f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        2045,
        Painbox.Constructor(Vector3(3850.975f, 4040.223f, 15.91256f), painbox_radius_continuous),
        owning_building_guid = 55
      )
    }

    Building50()

    def Building50(): Unit = { // Name: WG_Hossin_to_VSSanc_Tower Type: tower_a GUID: 56, MapID: 50
      LocalBuilding(
        "WG_Hossin_to_VSSanc_Tower",
        56,
        50,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5238f, 1902f, 40.67507f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2277,
        CaptureTerminal.Constructor(Vector3(5254.587f, 1901.897f, 50.67407f), secondary_capture),
        owning_building_guid = 56
      )
      LocalObject(463, Door.Constructor(Vector3(5250f, 1894f, 42.19607f)), owning_building_guid = 56)
      LocalObject(464, Door.Constructor(Vector3(5250f, 1894f, 62.19507f)), owning_building_guid = 56)
      LocalObject(465, Door.Constructor(Vector3(5250f, 1910f, 42.19607f)), owning_building_guid = 56)
      LocalObject(466, Door.Constructor(Vector3(5250f, 1910f, 62.19507f)), owning_building_guid = 56)
      LocalObject(2418, Door.Constructor(Vector3(5249.146f, 1890.794f, 32.01107f)), owning_building_guid = 56)
      LocalObject(2419, Door.Constructor(Vector3(5249.146f, 1907.204f, 32.01107f)), owning_building_guid = 56)
      LocalObject(
        997,
        IFFLock.Constructor(Vector3(5247.957f, 1910.811f, 42.13607f), Vector3(0, 0, 0)),
        owning_building_guid = 56,
        door_guid = 465
      )
      LocalObject(
        998,
        IFFLock.Constructor(Vector3(5247.957f, 1910.811f, 62.13607f), Vector3(0, 0, 0)),
        owning_building_guid = 56,
        door_guid = 466
      )
      LocalObject(
        999,
        IFFLock.Constructor(Vector3(5252.047f, 1893.189f, 42.13607f), Vector3(0, 0, 180)),
        owning_building_guid = 56,
        door_guid = 463
      )
      LocalObject(
        1000,
        IFFLock.Constructor(Vector3(5252.047f, 1893.189f, 62.13607f), Vector3(0, 0, 180)),
        owning_building_guid = 56,
        door_guid = 464
      )
      LocalObject(1311, Locker.Constructor(Vector3(5253.716f, 1886.963f, 30.66907f)), owning_building_guid = 56)
      LocalObject(1312, Locker.Constructor(Vector3(5253.751f, 1908.835f, 30.66907f)), owning_building_guid = 56)
      LocalObject(1313, Locker.Constructor(Vector3(5255.053f, 1886.963f, 30.66907f)), owning_building_guid = 56)
      LocalObject(1314, Locker.Constructor(Vector3(5255.088f, 1908.835f, 30.66907f)), owning_building_guid = 56)
      LocalObject(1315, Locker.Constructor(Vector3(5257.741f, 1886.963f, 30.66907f)), owning_building_guid = 56)
      LocalObject(1316, Locker.Constructor(Vector3(5257.741f, 1908.835f, 30.66907f)), owning_building_guid = 56)
      LocalObject(1317, Locker.Constructor(Vector3(5259.143f, 1886.963f, 30.66907f)), owning_building_guid = 56)
      LocalObject(1318, Locker.Constructor(Vector3(5259.143f, 1908.835f, 30.66907f)), owning_building_guid = 56)
      LocalObject(
        1671,
        Terminal.Constructor(Vector3(5259.445f, 1892.129f, 32.00707f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1672,
        Terminal.Constructor(Vector3(5259.445f, 1897.853f, 32.00707f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1673,
        Terminal.Constructor(Vector3(5259.445f, 1903.234f, 32.00707f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2238,
        SpawnTube.Constructor(Vector3(5248.706f, 1889.742f, 30.15707f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 56
      )
      LocalObject(
        2239,
        SpawnTube.Constructor(Vector3(5248.706f, 1906.152f, 30.15707f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 56
      )
      LocalObject(
        1509,
        FacilityTurret.Constructor(Vector3(5225.32f, 1889.295f, 59.61707f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1509, 5075)
      LocalObject(
        1510,
        FacilityTurret.Constructor(Vector3(5260.647f, 1914.707f, 59.61707f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1510, 5076)
      LocalObject(
        2061,
        Painbox.Constructor(Vector3(5243.235f, 1895.803f, 32.17417f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2062,
        Painbox.Constructor(Vector3(5254.889f, 1904.086f, 30.77507f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2063,
        Painbox.Constructor(Vector3(5254.975f, 1892.223f, 30.77507f), painbox_radius_continuous),
        owning_building_guid = 56
      )
    }

    Building23()

    def Building23(): Unit = { // Name: N_Kisin_Tower Type: tower_a GUID: 57, MapID: 23
      LocalBuilding(
        "N_Kisin_Tower",
        57,
        23,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5332f, 4112f, 18.44539f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2279,
        CaptureTerminal.Constructor(Vector3(5348.587f, 4111.897f, 28.44439f), secondary_capture),
        owning_building_guid = 57
      )
      LocalObject(474, Door.Constructor(Vector3(5344f, 4104f, 19.96639f)), owning_building_guid = 57)
      LocalObject(475, Door.Constructor(Vector3(5344f, 4104f, 39.96539f)), owning_building_guid = 57)
      LocalObject(476, Door.Constructor(Vector3(5344f, 4120f, 19.96639f)), owning_building_guid = 57)
      LocalObject(477, Door.Constructor(Vector3(5344f, 4120f, 39.96539f)), owning_building_guid = 57)
      LocalObject(2422, Door.Constructor(Vector3(5343.146f, 4100.794f, 9.781387f)), owning_building_guid = 57)
      LocalObject(2423, Door.Constructor(Vector3(5343.146f, 4117.204f, 9.781387f)), owning_building_guid = 57)
      LocalObject(
        1005,
        IFFLock.Constructor(Vector3(5341.957f, 4120.811f, 19.90639f), Vector3(0, 0, 0)),
        owning_building_guid = 57,
        door_guid = 476
      )
      LocalObject(
        1006,
        IFFLock.Constructor(Vector3(5341.957f, 4120.811f, 39.90639f), Vector3(0, 0, 0)),
        owning_building_guid = 57,
        door_guid = 477
      )
      LocalObject(
        1007,
        IFFLock.Constructor(Vector3(5346.047f, 4103.189f, 19.90639f), Vector3(0, 0, 180)),
        owning_building_guid = 57,
        door_guid = 474
      )
      LocalObject(
        1008,
        IFFLock.Constructor(Vector3(5346.047f, 4103.189f, 39.90639f), Vector3(0, 0, 180)),
        owning_building_guid = 57,
        door_guid = 475
      )
      LocalObject(1327, Locker.Constructor(Vector3(5347.716f, 4096.963f, 8.439387f)), owning_building_guid = 57)
      LocalObject(1328, Locker.Constructor(Vector3(5347.751f, 4118.835f, 8.439387f)), owning_building_guid = 57)
      LocalObject(1329, Locker.Constructor(Vector3(5349.053f, 4096.963f, 8.439387f)), owning_building_guid = 57)
      LocalObject(1330, Locker.Constructor(Vector3(5349.088f, 4118.835f, 8.439387f)), owning_building_guid = 57)
      LocalObject(1331, Locker.Constructor(Vector3(5351.741f, 4096.963f, 8.439387f)), owning_building_guid = 57)
      LocalObject(1332, Locker.Constructor(Vector3(5351.741f, 4118.835f, 8.439387f)), owning_building_guid = 57)
      LocalObject(1333, Locker.Constructor(Vector3(5353.143f, 4096.963f, 8.439387f)), owning_building_guid = 57)
      LocalObject(1334, Locker.Constructor(Vector3(5353.143f, 4118.835f, 8.439387f)), owning_building_guid = 57)
      LocalObject(
        1677,
        Terminal.Constructor(Vector3(5353.445f, 4102.129f, 9.777387f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1678,
        Terminal.Constructor(Vector3(5353.445f, 4107.853f, 9.777387f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1679,
        Terminal.Constructor(Vector3(5353.445f, 4113.234f, 9.777387f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        2242,
        SpawnTube.Constructor(Vector3(5342.706f, 4099.742f, 7.927387f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 57
      )
      LocalObject(
        2243,
        SpawnTube.Constructor(Vector3(5342.706f, 4116.152f, 7.927387f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 57
      )
      LocalObject(
        1513,
        FacilityTurret.Constructor(Vector3(5319.32f, 4099.295f, 37.38739f), manned_turret),
        owning_building_guid = 57
      )
      TurretToWeapon(1513, 5077)
      LocalObject(
        1515,
        FacilityTurret.Constructor(Vector3(5354.647f, 4124.707f, 37.38739f), manned_turret),
        owning_building_guid = 57
      )
      TurretToWeapon(1515, 5078)
      LocalObject(
        2067,
        Painbox.Constructor(Vector3(5337.235f, 4105.803f, 9.944487f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        2068,
        Painbox.Constructor(Vector3(5348.889f, 4114.086f, 8.545387f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        2069,
        Painbox.Constructor(Vector3(5348.975f, 4102.223f, 8.545387f), painbox_radius_continuous),
        owning_building_guid = 57
      )
    }

    Building52()

    def Building52(): Unit = { // Name: E_Naum_Tower Type: tower_a GUID: 58, MapID: 52
      LocalBuilding(
        "E_Naum_Tower",
        58,
        52,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6472f, 3562f, 46.96188f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2281,
        CaptureTerminal.Constructor(Vector3(6488.587f, 3561.897f, 56.96088f), secondary_capture),
        owning_building_guid = 58
      )
      LocalObject(516, Door.Constructor(Vector3(6484f, 3554f, 48.48288f)), owning_building_guid = 58)
      LocalObject(517, Door.Constructor(Vector3(6484f, 3554f, 68.48189f)), owning_building_guid = 58)
      LocalObject(518, Door.Constructor(Vector3(6484f, 3570f, 48.48288f)), owning_building_guid = 58)
      LocalObject(519, Door.Constructor(Vector3(6484f, 3570f, 68.48189f)), owning_building_guid = 58)
      LocalObject(2432, Door.Constructor(Vector3(6483.146f, 3550.794f, 38.29788f)), owning_building_guid = 58)
      LocalObject(2433, Door.Constructor(Vector3(6483.146f, 3567.204f, 38.29788f)), owning_building_guid = 58)
      LocalObject(
        1037,
        IFFLock.Constructor(Vector3(6481.957f, 3570.811f, 48.42288f), Vector3(0, 0, 0)),
        owning_building_guid = 58,
        door_guid = 518
      )
      LocalObject(
        1038,
        IFFLock.Constructor(Vector3(6481.957f, 3570.811f, 68.42288f), Vector3(0, 0, 0)),
        owning_building_guid = 58,
        door_guid = 519
      )
      LocalObject(
        1039,
        IFFLock.Constructor(Vector3(6486.047f, 3553.189f, 48.42288f), Vector3(0, 0, 180)),
        owning_building_guid = 58,
        door_guid = 516
      )
      LocalObject(
        1040,
        IFFLock.Constructor(Vector3(6486.047f, 3553.189f, 68.42288f), Vector3(0, 0, 180)),
        owning_building_guid = 58,
        door_guid = 517
      )
      LocalObject(1367, Locker.Constructor(Vector3(6487.716f, 3546.963f, 36.95588f)), owning_building_guid = 58)
      LocalObject(1368, Locker.Constructor(Vector3(6487.751f, 3568.835f, 36.95588f)), owning_building_guid = 58)
      LocalObject(1369, Locker.Constructor(Vector3(6489.053f, 3546.963f, 36.95588f)), owning_building_guid = 58)
      LocalObject(1370, Locker.Constructor(Vector3(6489.088f, 3568.835f, 36.95588f)), owning_building_guid = 58)
      LocalObject(1371, Locker.Constructor(Vector3(6491.741f, 3546.963f, 36.95588f)), owning_building_guid = 58)
      LocalObject(1372, Locker.Constructor(Vector3(6491.741f, 3568.835f, 36.95588f)), owning_building_guid = 58)
      LocalObject(1373, Locker.Constructor(Vector3(6493.143f, 3546.963f, 36.95588f)), owning_building_guid = 58)
      LocalObject(1374, Locker.Constructor(Vector3(6493.143f, 3568.835f, 36.95588f)), owning_building_guid = 58)
      LocalObject(
        1695,
        Terminal.Constructor(Vector3(6493.445f, 3552.129f, 38.29388f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1696,
        Terminal.Constructor(Vector3(6493.445f, 3557.853f, 38.29388f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1697,
        Terminal.Constructor(Vector3(6493.445f, 3563.234f, 38.29388f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        2252,
        SpawnTube.Constructor(Vector3(6482.706f, 3549.742f, 36.44389f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 58
      )
      LocalObject(
        2253,
        SpawnTube.Constructor(Vector3(6482.706f, 3566.152f, 36.44389f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 58
      )
      LocalObject(
        1529,
        FacilityTurret.Constructor(Vector3(6459.32f, 3549.295f, 65.90388f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1529, 5079)
      LocalObject(
        1530,
        FacilityTurret.Constructor(Vector3(6494.647f, 3574.707f, 65.90388f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1530, 5080)
      LocalObject(
        2073,
        Painbox.Constructor(Vector3(6477.235f, 3555.803f, 38.46098f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        2074,
        Painbox.Constructor(Vector3(6488.889f, 3564.086f, 37.06188f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        2075,
        Painbox.Constructor(Vector3(6488.975f, 3552.223f, 37.06188f), painbox_radius_continuous),
        owning_building_guid = 58
      )
    }

    Building31()

    def Building31(): Unit = { // Name: SW_Ghanon_Tower Type: tower_b GUID: 59, MapID: 31
      LocalBuilding(
        "SW_Ghanon_Tower",
        59,
        31,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1994f, 3310f, 25.44633f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2260,
        CaptureTerminal.Constructor(Vector3(2010.587f, 3309.897f, 45.44534f), secondary_capture),
        owning_building_guid = 59
      )
      LocalObject(279, Door.Constructor(Vector3(2006f, 3302f, 26.96633f)), owning_building_guid = 59)
      LocalObject(280, Door.Constructor(Vector3(2006f, 3302f, 36.96633f)), owning_building_guid = 59)
      LocalObject(281, Door.Constructor(Vector3(2006f, 3302f, 56.96633f)), owning_building_guid = 59)
      LocalObject(282, Door.Constructor(Vector3(2006f, 3318f, 26.96633f)), owning_building_guid = 59)
      LocalObject(283, Door.Constructor(Vector3(2006f, 3318f, 36.96633f)), owning_building_guid = 59)
      LocalObject(284, Door.Constructor(Vector3(2006f, 3318f, 56.96633f)), owning_building_guid = 59)
      LocalObject(2363, Door.Constructor(Vector3(2005.147f, 3298.794f, 16.78233f)), owning_building_guid = 59)
      LocalObject(2364, Door.Constructor(Vector3(2005.147f, 3315.204f, 16.78233f)), owning_building_guid = 59)
      LocalObject(
        852,
        IFFLock.Constructor(Vector3(2003.957f, 3318.811f, 26.90733f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 282
      )
      LocalObject(
        853,
        IFFLock.Constructor(Vector3(2003.957f, 3318.811f, 36.90733f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 283
      )
      LocalObject(
        854,
        IFFLock.Constructor(Vector3(2003.957f, 3318.811f, 56.90733f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 284
      )
      LocalObject(
        855,
        IFFLock.Constructor(Vector3(2008.047f, 3301.189f, 26.90733f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 279
      )
      LocalObject(
        856,
        IFFLock.Constructor(Vector3(2008.047f, 3301.189f, 36.90733f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 280
      )
      LocalObject(
        857,
        IFFLock.Constructor(Vector3(2008.047f, 3301.189f, 56.90733f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 281
      )
      LocalObject(1073, Locker.Constructor(Vector3(2009.716f, 3294.963f, 15.44033f)), owning_building_guid = 59)
      LocalObject(1074, Locker.Constructor(Vector3(2009.751f, 3316.835f, 15.44033f)), owning_building_guid = 59)
      LocalObject(1075, Locker.Constructor(Vector3(2011.053f, 3294.963f, 15.44033f)), owning_building_guid = 59)
      LocalObject(1076, Locker.Constructor(Vector3(2011.088f, 3316.835f, 15.44033f)), owning_building_guid = 59)
      LocalObject(1077, Locker.Constructor(Vector3(2013.741f, 3294.963f, 15.44033f)), owning_building_guid = 59)
      LocalObject(1078, Locker.Constructor(Vector3(2013.741f, 3316.835f, 15.44033f)), owning_building_guid = 59)
      LocalObject(1079, Locker.Constructor(Vector3(2015.143f, 3294.963f, 15.44033f)), owning_building_guid = 59)
      LocalObject(1080, Locker.Constructor(Vector3(2015.143f, 3316.835f, 15.44033f)), owning_building_guid = 59)
      LocalObject(
        1584,
        Terminal.Constructor(Vector3(2015.446f, 3300.129f, 16.77833f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1585,
        Terminal.Constructor(Vector3(2015.446f, 3305.853f, 16.77833f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1586,
        Terminal.Constructor(Vector3(2015.446f, 3311.234f, 16.77833f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        2183,
        SpawnTube.Constructor(Vector3(2004.706f, 3297.742f, 14.92833f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        2184,
        SpawnTube.Constructor(Vector3(2004.706f, 3314.152f, 14.92833f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        2010,
        Painbox.Constructor(Vector3(1999.493f, 3302.849f, 16.73573f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        2011,
        Painbox.Constructor(Vector3(2011.127f, 3300.078f, 15.54633f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        2012,
        Painbox.Constructor(Vector3(2011.259f, 3312.107f, 15.54633f), painbox_radius_continuous),
        owning_building_guid = 59
      )
    }

    Building30()

    def Building30(): Unit = { // Name: SW_Ixtab_Tower Type: tower_b GUID: 60, MapID: 30
      LocalBuilding(
        "SW_Ixtab_Tower",
        60,
        30,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2950f, 2648f, 21.2714f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2264,
        CaptureTerminal.Constructor(Vector3(2966.587f, 2647.897f, 41.2704f), secondary_capture),
        owning_building_guid = 60
      )
      LocalObject(313, Door.Constructor(Vector3(2962f, 2640f, 22.7914f)), owning_building_guid = 60)
      LocalObject(314, Door.Constructor(Vector3(2962f, 2640f, 32.7914f)), owning_building_guid = 60)
      LocalObject(315, Door.Constructor(Vector3(2962f, 2640f, 52.7914f)), owning_building_guid = 60)
      LocalObject(316, Door.Constructor(Vector3(2962f, 2656f, 22.7914f)), owning_building_guid = 60)
      LocalObject(317, Door.Constructor(Vector3(2962f, 2656f, 32.7914f)), owning_building_guid = 60)
      LocalObject(318, Door.Constructor(Vector3(2962f, 2656f, 52.7914f)), owning_building_guid = 60)
      LocalObject(2374, Door.Constructor(Vector3(2961.147f, 2636.794f, 12.6074f)), owning_building_guid = 60)
      LocalObject(2375, Door.Constructor(Vector3(2961.147f, 2653.204f, 12.6074f)), owning_building_guid = 60)
      LocalObject(
        878,
        IFFLock.Constructor(Vector3(2959.957f, 2656.811f, 22.7324f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 316
      )
      LocalObject(
        879,
        IFFLock.Constructor(Vector3(2959.957f, 2656.811f, 32.7324f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 317
      )
      LocalObject(
        880,
        IFFLock.Constructor(Vector3(2959.957f, 2656.811f, 52.7324f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 318
      )
      LocalObject(
        881,
        IFFLock.Constructor(Vector3(2964.047f, 2639.189f, 22.7324f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 313
      )
      LocalObject(
        882,
        IFFLock.Constructor(Vector3(2964.047f, 2639.189f, 32.7324f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 314
      )
      LocalObject(
        883,
        IFFLock.Constructor(Vector3(2964.047f, 2639.189f, 52.7324f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 315
      )
      LocalObject(1117, Locker.Constructor(Vector3(2965.716f, 2632.963f, 11.2654f)), owning_building_guid = 60)
      LocalObject(1118, Locker.Constructor(Vector3(2965.751f, 2654.835f, 11.2654f)), owning_building_guid = 60)
      LocalObject(1119, Locker.Constructor(Vector3(2967.053f, 2632.963f, 11.2654f)), owning_building_guid = 60)
      LocalObject(1120, Locker.Constructor(Vector3(2967.088f, 2654.835f, 11.2654f)), owning_building_guid = 60)
      LocalObject(1121, Locker.Constructor(Vector3(2969.741f, 2632.963f, 11.2654f)), owning_building_guid = 60)
      LocalObject(1122, Locker.Constructor(Vector3(2969.741f, 2654.835f, 11.2654f)), owning_building_guid = 60)
      LocalObject(1123, Locker.Constructor(Vector3(2971.143f, 2632.963f, 11.2654f)), owning_building_guid = 60)
      LocalObject(1124, Locker.Constructor(Vector3(2971.143f, 2654.835f, 11.2654f)), owning_building_guid = 60)
      LocalObject(
        1600,
        Terminal.Constructor(Vector3(2971.446f, 2638.129f, 12.6034f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1601,
        Terminal.Constructor(Vector3(2971.446f, 2643.853f, 12.6034f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1602,
        Terminal.Constructor(Vector3(2971.446f, 2649.234f, 12.6034f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        2194,
        SpawnTube.Constructor(Vector3(2960.706f, 2635.742f, 10.7534f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        2195,
        SpawnTube.Constructor(Vector3(2960.706f, 2652.152f, 10.7534f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        2022,
        Painbox.Constructor(Vector3(2955.493f, 2640.849f, 12.5608f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        2023,
        Painbox.Constructor(Vector3(2967.127f, 2638.078f, 11.3714f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        2024,
        Painbox.Constructor(Vector3(2967.259f, 2650.107f, 11.3714f), painbox_radius_continuous),
        owning_building_guid = 60
      )
    }

    Building16()

    def Building16(): Unit = { // Name: NW_Acan_Tower Type: tower_b GUID: 61, MapID: 16
      LocalBuilding(
        "NW_Acan_Tower",
        61,
        16,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3086f, 4562f, 22.34565f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2265,
        CaptureTerminal.Constructor(Vector3(3102.587f, 4561.897f, 42.34465f), secondary_capture),
        owning_building_guid = 61
      )
      LocalObject(319, Door.Constructor(Vector3(3098f, 4554f, 23.86565f)), owning_building_guid = 61)
      LocalObject(320, Door.Constructor(Vector3(3098f, 4554f, 33.86565f)), owning_building_guid = 61)
      LocalObject(321, Door.Constructor(Vector3(3098f, 4554f, 53.86565f)), owning_building_guid = 61)
      LocalObject(322, Door.Constructor(Vector3(3098f, 4570f, 23.86565f)), owning_building_guid = 61)
      LocalObject(323, Door.Constructor(Vector3(3098f, 4570f, 33.86565f)), owning_building_guid = 61)
      LocalObject(324, Door.Constructor(Vector3(3098f, 4570f, 53.86565f)), owning_building_guid = 61)
      LocalObject(2376, Door.Constructor(Vector3(3097.147f, 4550.794f, 13.68165f)), owning_building_guid = 61)
      LocalObject(2377, Door.Constructor(Vector3(3097.147f, 4567.204f, 13.68165f)), owning_building_guid = 61)
      LocalObject(
        884,
        IFFLock.Constructor(Vector3(3095.957f, 4570.811f, 23.80665f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 322
      )
      LocalObject(
        885,
        IFFLock.Constructor(Vector3(3095.957f, 4570.811f, 33.80666f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 323
      )
      LocalObject(
        886,
        IFFLock.Constructor(Vector3(3095.957f, 4570.811f, 53.80666f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 324
      )
      LocalObject(
        887,
        IFFLock.Constructor(Vector3(3100.047f, 4553.189f, 23.80665f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 319
      )
      LocalObject(
        888,
        IFFLock.Constructor(Vector3(3100.047f, 4553.189f, 33.80666f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 320
      )
      LocalObject(
        889,
        IFFLock.Constructor(Vector3(3100.047f, 4553.189f, 53.80666f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 321
      )
      LocalObject(1125, Locker.Constructor(Vector3(3101.716f, 4546.963f, 12.33965f)), owning_building_guid = 61)
      LocalObject(1126, Locker.Constructor(Vector3(3101.751f, 4568.835f, 12.33965f)), owning_building_guid = 61)
      LocalObject(1127, Locker.Constructor(Vector3(3103.053f, 4546.963f, 12.33965f)), owning_building_guid = 61)
      LocalObject(1128, Locker.Constructor(Vector3(3103.088f, 4568.835f, 12.33965f)), owning_building_guid = 61)
      LocalObject(1129, Locker.Constructor(Vector3(3105.741f, 4546.963f, 12.33965f)), owning_building_guid = 61)
      LocalObject(1130, Locker.Constructor(Vector3(3105.741f, 4568.835f, 12.33965f)), owning_building_guid = 61)
      LocalObject(1131, Locker.Constructor(Vector3(3107.143f, 4546.963f, 12.33965f)), owning_building_guid = 61)
      LocalObject(1132, Locker.Constructor(Vector3(3107.143f, 4568.835f, 12.33965f)), owning_building_guid = 61)
      LocalObject(
        1603,
        Terminal.Constructor(Vector3(3107.446f, 4552.129f, 13.67765f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1604,
        Terminal.Constructor(Vector3(3107.446f, 4557.853f, 13.67765f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1605,
        Terminal.Constructor(Vector3(3107.446f, 4563.234f, 13.67765f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        2196,
        SpawnTube.Constructor(Vector3(3096.706f, 4549.742f, 11.82765f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        2197,
        SpawnTube.Constructor(Vector3(3096.706f, 4566.152f, 11.82765f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        2025,
        Painbox.Constructor(Vector3(3091.493f, 4554.849f, 13.63505f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        2026,
        Painbox.Constructor(Vector3(3103.127f, 4552.078f, 12.44565f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        2027,
        Painbox.Constructor(Vector3(3103.259f, 4564.107f, 12.44565f), painbox_radius_continuous),
        owning_building_guid = 61
      )
    }

    Building19()

    def Building19(): Unit = { // Name: N_Naum_Tower Type: tower_b GUID: 62, MapID: 19
      LocalBuilding(
        "N_Naum_Tower",
        62,
        19,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3306f, 5924f, 24.897f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2266,
        CaptureTerminal.Constructor(Vector3(3322.587f, 5923.897f, 44.896f), secondary_capture),
        owning_building_guid = 62
      )
      LocalObject(338, Door.Constructor(Vector3(3318f, 5916f, 26.417f)), owning_building_guid = 62)
      LocalObject(339, Door.Constructor(Vector3(3318f, 5916f, 36.417f)), owning_building_guid = 62)
      LocalObject(340, Door.Constructor(Vector3(3318f, 5916f, 56.417f)), owning_building_guid = 62)
      LocalObject(341, Door.Constructor(Vector3(3318f, 5932f, 26.417f)), owning_building_guid = 62)
      LocalObject(342, Door.Constructor(Vector3(3318f, 5932f, 36.417f)), owning_building_guid = 62)
      LocalObject(343, Door.Constructor(Vector3(3318f, 5932f, 56.417f)), owning_building_guid = 62)
      LocalObject(2378, Door.Constructor(Vector3(3317.147f, 5912.794f, 16.233f)), owning_building_guid = 62)
      LocalObject(2379, Door.Constructor(Vector3(3317.147f, 5929.204f, 16.233f)), owning_building_guid = 62)
      LocalObject(
        895,
        IFFLock.Constructor(Vector3(3315.957f, 5932.811f, 26.358f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 341
      )
      LocalObject(
        896,
        IFFLock.Constructor(Vector3(3315.957f, 5932.811f, 36.358f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 342
      )
      LocalObject(
        897,
        IFFLock.Constructor(Vector3(3315.957f, 5932.811f, 56.358f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 343
      )
      LocalObject(
        898,
        IFFLock.Constructor(Vector3(3320.047f, 5915.189f, 26.358f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 338
      )
      LocalObject(
        899,
        IFFLock.Constructor(Vector3(3320.047f, 5915.189f, 36.358f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 339
      )
      LocalObject(
        900,
        IFFLock.Constructor(Vector3(3320.047f, 5915.189f, 56.358f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 340
      )
      LocalObject(1133, Locker.Constructor(Vector3(3321.716f, 5908.963f, 14.891f)), owning_building_guid = 62)
      LocalObject(1134, Locker.Constructor(Vector3(3321.751f, 5930.835f, 14.891f)), owning_building_guid = 62)
      LocalObject(1135, Locker.Constructor(Vector3(3323.053f, 5908.963f, 14.891f)), owning_building_guid = 62)
      LocalObject(1136, Locker.Constructor(Vector3(3323.088f, 5930.835f, 14.891f)), owning_building_guid = 62)
      LocalObject(1137, Locker.Constructor(Vector3(3325.741f, 5908.963f, 14.891f)), owning_building_guid = 62)
      LocalObject(1138, Locker.Constructor(Vector3(3325.741f, 5930.835f, 14.891f)), owning_building_guid = 62)
      LocalObject(1139, Locker.Constructor(Vector3(3327.143f, 5908.963f, 14.891f)), owning_building_guid = 62)
      LocalObject(1140, Locker.Constructor(Vector3(3327.143f, 5930.835f, 14.891f)), owning_building_guid = 62)
      LocalObject(
        1610,
        Terminal.Constructor(Vector3(3327.446f, 5914.129f, 16.229f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        1611,
        Terminal.Constructor(Vector3(3327.446f, 5919.853f, 16.229f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        1612,
        Terminal.Constructor(Vector3(3327.446f, 5925.234f, 16.229f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        2198,
        SpawnTube.Constructor(Vector3(3316.706f, 5911.742f, 14.379f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        2199,
        SpawnTube.Constructor(Vector3(3316.706f, 5928.152f, 14.379f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        2028,
        Painbox.Constructor(Vector3(3311.493f, 5916.849f, 16.1864f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2029,
        Painbox.Constructor(Vector3(3323.127f, 5914.078f, 14.997f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2030,
        Painbox.Constructor(Vector3(3323.259f, 5926.107f, 14.997f), painbox_radius_continuous),
        owning_building_guid = 62
      )
    }

    Building22()

    def Building22(): Unit = { // Name: SW_Solsar_Warpgate_Tower Type: tower_b GUID: 63, MapID: 22
      LocalBuilding(
        "SW_Solsar_Warpgate_Tower",
        63,
        22,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4494f, 4096f, 26.09943f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2274,
        CaptureTerminal.Constructor(Vector3(4510.587f, 4095.897f, 46.09843f), secondary_capture),
        owning_building_guid = 63
      )
      LocalObject(438, Door.Constructor(Vector3(4506f, 4088f, 27.61943f)), owning_building_guid = 63)
      LocalObject(439, Door.Constructor(Vector3(4506f, 4088f, 37.61943f)), owning_building_guid = 63)
      LocalObject(440, Door.Constructor(Vector3(4506f, 4088f, 57.61943f)), owning_building_guid = 63)
      LocalObject(441, Door.Constructor(Vector3(4506f, 4104f, 27.61943f)), owning_building_guid = 63)
      LocalObject(442, Door.Constructor(Vector3(4506f, 4104f, 37.61943f)), owning_building_guid = 63)
      LocalObject(443, Door.Constructor(Vector3(4506f, 4104f, 57.61943f)), owning_building_guid = 63)
      LocalObject(2409, Door.Constructor(Vector3(4505.147f, 4084.794f, 17.43543f)), owning_building_guid = 63)
      LocalObject(2410, Door.Constructor(Vector3(4505.147f, 4101.204f, 17.43543f)), owning_building_guid = 63)
      LocalObject(
        974,
        IFFLock.Constructor(Vector3(4503.957f, 4104.811f, 27.56043f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 441
      )
      LocalObject(
        975,
        IFFLock.Constructor(Vector3(4503.957f, 4104.811f, 37.56043f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 442
      )
      LocalObject(
        976,
        IFFLock.Constructor(Vector3(4503.957f, 4104.811f, 57.56043f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 443
      )
      LocalObject(
        977,
        IFFLock.Constructor(Vector3(4508.047f, 4087.189f, 27.56043f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 438
      )
      LocalObject(
        978,
        IFFLock.Constructor(Vector3(4508.047f, 4087.189f, 37.56043f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 439
      )
      LocalObject(
        979,
        IFFLock.Constructor(Vector3(4508.047f, 4087.189f, 57.56043f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 440
      )
      LocalObject(1263, Locker.Constructor(Vector3(4509.716f, 4080.963f, 16.09343f)), owning_building_guid = 63)
      LocalObject(1264, Locker.Constructor(Vector3(4509.751f, 4102.835f, 16.09343f)), owning_building_guid = 63)
      LocalObject(1265, Locker.Constructor(Vector3(4511.053f, 4080.963f, 16.09343f)), owning_building_guid = 63)
      LocalObject(1266, Locker.Constructor(Vector3(4511.088f, 4102.835f, 16.09343f)), owning_building_guid = 63)
      LocalObject(1269, Locker.Constructor(Vector3(4513.741f, 4080.963f, 16.09343f)), owning_building_guid = 63)
      LocalObject(1270, Locker.Constructor(Vector3(4513.741f, 4102.835f, 16.09343f)), owning_building_guid = 63)
      LocalObject(1275, Locker.Constructor(Vector3(4515.143f, 4080.963f, 16.09343f)), owning_building_guid = 63)
      LocalObject(1276, Locker.Constructor(Vector3(4515.143f, 4102.835f, 16.09343f)), owning_building_guid = 63)
      LocalObject(
        1659,
        Terminal.Constructor(Vector3(4515.446f, 4086.129f, 17.43143f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1660,
        Terminal.Constructor(Vector3(4515.446f, 4091.853f, 17.43143f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1661,
        Terminal.Constructor(Vector3(4515.446f, 4097.234f, 17.43143f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        2229,
        SpawnTube.Constructor(Vector3(4504.706f, 4083.742f, 15.58143f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        2230,
        SpawnTube.Constructor(Vector3(4504.706f, 4100.152f, 15.58143f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        2052,
        Painbox.Constructor(Vector3(4499.493f, 4088.849f, 17.38883f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        2053,
        Painbox.Constructor(Vector3(4511.127f, 4086.078f, 16.19943f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        2054,
        Painbox.Constructor(Vector3(4511.259f, 4098.107f, 16.19943f), painbox_radius_continuous),
        owning_building_guid = 63
      )
    }

    Building42()

    def Building42(): Unit = { // Name: Voltan_Tower Type: tower_b GUID: 64, MapID: 42
      LocalBuilding(
        "Voltan_Tower",
        64,
        42,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4780f, 3522f, 21.20083f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2276,
        CaptureTerminal.Constructor(Vector3(4796.587f, 3521.897f, 41.19983f), secondary_capture),
        owning_building_guid = 64
      )
      LocalObject(457, Door.Constructor(Vector3(4792f, 3514f, 22.72083f)), owning_building_guid = 64)
      LocalObject(458, Door.Constructor(Vector3(4792f, 3514f, 32.72083f)), owning_building_guid = 64)
      LocalObject(459, Door.Constructor(Vector3(4792f, 3514f, 52.72083f)), owning_building_guid = 64)
      LocalObject(460, Door.Constructor(Vector3(4792f, 3530f, 22.72083f)), owning_building_guid = 64)
      LocalObject(461, Door.Constructor(Vector3(4792f, 3530f, 32.72083f)), owning_building_guid = 64)
      LocalObject(462, Door.Constructor(Vector3(4792f, 3530f, 52.72083f)), owning_building_guid = 64)
      LocalObject(2416, Door.Constructor(Vector3(4791.147f, 3510.794f, 12.53683f)), owning_building_guid = 64)
      LocalObject(2417, Door.Constructor(Vector3(4791.147f, 3527.204f, 12.53683f)), owning_building_guid = 64)
      LocalObject(
        991,
        IFFLock.Constructor(Vector3(4789.957f, 3530.811f, 22.66183f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 460
      )
      LocalObject(
        992,
        IFFLock.Constructor(Vector3(4789.957f, 3530.811f, 32.66183f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 461
      )
      LocalObject(
        993,
        IFFLock.Constructor(Vector3(4789.957f, 3530.811f, 52.66183f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 462
      )
      LocalObject(
        994,
        IFFLock.Constructor(Vector3(4794.047f, 3513.189f, 22.66183f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 457
      )
      LocalObject(
        995,
        IFFLock.Constructor(Vector3(4794.047f, 3513.189f, 32.66183f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 458
      )
      LocalObject(
        996,
        IFFLock.Constructor(Vector3(4794.047f, 3513.189f, 52.66183f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 459
      )
      LocalObject(1303, Locker.Constructor(Vector3(4795.716f, 3506.963f, 11.19483f)), owning_building_guid = 64)
      LocalObject(1304, Locker.Constructor(Vector3(4795.751f, 3528.835f, 11.19483f)), owning_building_guid = 64)
      LocalObject(1305, Locker.Constructor(Vector3(4797.053f, 3506.963f, 11.19483f)), owning_building_guid = 64)
      LocalObject(1306, Locker.Constructor(Vector3(4797.088f, 3528.835f, 11.19483f)), owning_building_guid = 64)
      LocalObject(1307, Locker.Constructor(Vector3(4799.741f, 3506.963f, 11.19483f)), owning_building_guid = 64)
      LocalObject(1308, Locker.Constructor(Vector3(4799.741f, 3528.835f, 11.19483f)), owning_building_guid = 64)
      LocalObject(1309, Locker.Constructor(Vector3(4801.143f, 3506.963f, 11.19483f)), owning_building_guid = 64)
      LocalObject(1310, Locker.Constructor(Vector3(4801.143f, 3528.835f, 11.19483f)), owning_building_guid = 64)
      LocalObject(
        1668,
        Terminal.Constructor(Vector3(4801.446f, 3512.129f, 12.53283f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1669,
        Terminal.Constructor(Vector3(4801.446f, 3517.853f, 12.53283f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1670,
        Terminal.Constructor(Vector3(4801.446f, 3523.234f, 12.53283f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        2236,
        SpawnTube.Constructor(Vector3(4790.706f, 3509.742f, 10.68283f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 64
      )
      LocalObject(
        2237,
        SpawnTube.Constructor(Vector3(4790.706f, 3526.152f, 10.68283f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 64
      )
      LocalObject(
        2058,
        Painbox.Constructor(Vector3(4785.493f, 3514.849f, 12.49023f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        2059,
        Painbox.Constructor(Vector3(4797.127f, 3512.078f, 11.30083f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        2060,
        Painbox.Constructor(Vector3(4797.259f, 3524.107f, 11.30083f), painbox_radius_continuous),
        owning_building_guid = 64
      )
    }

    Building24()

    def Building24(): Unit = { // Name: NE_Mulac_Tower Type: tower_b GUID: 65, MapID: 24
      LocalBuilding(
        "NE_Mulac_Tower",
        65,
        24,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5784f, 3474f, 24.29884f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2280,
        CaptureTerminal.Constructor(Vector3(5800.587f, 3473.897f, 44.29784f), secondary_capture),
        owning_building_guid = 65
      )
      LocalObject(510, Door.Constructor(Vector3(5796f, 3466f, 25.81884f)), owning_building_guid = 65)
      LocalObject(511, Door.Constructor(Vector3(5796f, 3466f, 35.81884f)), owning_building_guid = 65)
      LocalObject(512, Door.Constructor(Vector3(5796f, 3466f, 55.81884f)), owning_building_guid = 65)
      LocalObject(513, Door.Constructor(Vector3(5796f, 3482f, 25.81884f)), owning_building_guid = 65)
      LocalObject(514, Door.Constructor(Vector3(5796f, 3482f, 35.81884f)), owning_building_guid = 65)
      LocalObject(515, Door.Constructor(Vector3(5796f, 3482f, 55.81884f)), owning_building_guid = 65)
      LocalObject(2430, Door.Constructor(Vector3(5795.147f, 3462.794f, 15.63484f)), owning_building_guid = 65)
      LocalObject(2431, Door.Constructor(Vector3(5795.147f, 3479.204f, 15.63484f)), owning_building_guid = 65)
      LocalObject(
        1031,
        IFFLock.Constructor(Vector3(5793.957f, 3482.811f, 25.75984f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 513
      )
      LocalObject(
        1032,
        IFFLock.Constructor(Vector3(5793.957f, 3482.811f, 35.75984f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 514
      )
      LocalObject(
        1033,
        IFFLock.Constructor(Vector3(5793.957f, 3482.811f, 55.75984f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 515
      )
      LocalObject(
        1034,
        IFFLock.Constructor(Vector3(5798.047f, 3465.189f, 25.75984f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 510
      )
      LocalObject(
        1035,
        IFFLock.Constructor(Vector3(5798.047f, 3465.189f, 35.75984f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 511
      )
      LocalObject(
        1036,
        IFFLock.Constructor(Vector3(5798.047f, 3465.189f, 55.75984f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 512
      )
      LocalObject(1359, Locker.Constructor(Vector3(5799.716f, 3458.963f, 14.29284f)), owning_building_guid = 65)
      LocalObject(1360, Locker.Constructor(Vector3(5799.751f, 3480.835f, 14.29284f)), owning_building_guid = 65)
      LocalObject(1361, Locker.Constructor(Vector3(5801.053f, 3458.963f, 14.29284f)), owning_building_guid = 65)
      LocalObject(1362, Locker.Constructor(Vector3(5801.088f, 3480.835f, 14.29284f)), owning_building_guid = 65)
      LocalObject(1363, Locker.Constructor(Vector3(5803.741f, 3458.963f, 14.29284f)), owning_building_guid = 65)
      LocalObject(1364, Locker.Constructor(Vector3(5803.741f, 3480.835f, 14.29284f)), owning_building_guid = 65)
      LocalObject(1365, Locker.Constructor(Vector3(5805.143f, 3458.963f, 14.29284f)), owning_building_guid = 65)
      LocalObject(1366, Locker.Constructor(Vector3(5805.143f, 3480.835f, 14.29284f)), owning_building_guid = 65)
      LocalObject(
        1692,
        Terminal.Constructor(Vector3(5805.446f, 3464.129f, 15.63084f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1693,
        Terminal.Constructor(Vector3(5805.446f, 3469.853f, 15.63084f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1694,
        Terminal.Constructor(Vector3(5805.446f, 3475.234f, 15.63084f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        2250,
        SpawnTube.Constructor(Vector3(5794.706f, 3461.742f, 13.78084f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        2251,
        SpawnTube.Constructor(Vector3(5794.706f, 3478.152f, 13.78084f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        2070,
        Painbox.Constructor(Vector3(5789.493f, 3466.849f, 15.58824f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        2071,
        Painbox.Constructor(Vector3(5801.127f, 3464.078f, 14.39884f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        2072,
        Painbox.Constructor(Vector3(5801.259f, 3476.107f, 14.39884f), painbox_radius_continuous),
        owning_building_guid = 65
      )
    }

    Building33()

    def Building33(): Unit = { // Name: SE_Ghanon_Tower Type: tower_c GUID: 66, MapID: 33
      LocalBuilding(
        "SE_Ghanon_Tower",
        66,
        33,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2638f, 3496f, 35.68224f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2263,
        CaptureTerminal.Constructor(Vector3(2654.587f, 3495.897f, 45.68124f), secondary_capture),
        owning_building_guid = 66
      )
      LocalObject(309, Door.Constructor(Vector3(2650f, 3488f, 37.20324f)), owning_building_guid = 66)
      LocalObject(310, Door.Constructor(Vector3(2650f, 3488f, 57.20224f)), owning_building_guid = 66)
      LocalObject(311, Door.Constructor(Vector3(2650f, 3504f, 37.20324f)), owning_building_guid = 66)
      LocalObject(312, Door.Constructor(Vector3(2650f, 3504f, 57.20224f)), owning_building_guid = 66)
      LocalObject(2372, Door.Constructor(Vector3(2649.146f, 3484.794f, 27.01824f)), owning_building_guid = 66)
      LocalObject(2373, Door.Constructor(Vector3(2649.146f, 3501.204f, 27.01824f)), owning_building_guid = 66)
      LocalObject(
        874,
        IFFLock.Constructor(Vector3(2647.957f, 3504.811f, 37.14324f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 311
      )
      LocalObject(
        875,
        IFFLock.Constructor(Vector3(2647.957f, 3504.811f, 57.14324f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 312
      )
      LocalObject(
        876,
        IFFLock.Constructor(Vector3(2652.047f, 3487.189f, 37.14324f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 309
      )
      LocalObject(
        877,
        IFFLock.Constructor(Vector3(2652.047f, 3487.189f, 57.14324f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 310
      )
      LocalObject(1109, Locker.Constructor(Vector3(2653.716f, 3480.963f, 25.67624f)), owning_building_guid = 66)
      LocalObject(1110, Locker.Constructor(Vector3(2653.751f, 3502.835f, 25.67624f)), owning_building_guid = 66)
      LocalObject(1111, Locker.Constructor(Vector3(2655.053f, 3480.963f, 25.67624f)), owning_building_guid = 66)
      LocalObject(1112, Locker.Constructor(Vector3(2655.088f, 3502.835f, 25.67624f)), owning_building_guid = 66)
      LocalObject(1113, Locker.Constructor(Vector3(2657.741f, 3480.963f, 25.67624f)), owning_building_guid = 66)
      LocalObject(1114, Locker.Constructor(Vector3(2657.741f, 3502.835f, 25.67624f)), owning_building_guid = 66)
      LocalObject(1115, Locker.Constructor(Vector3(2659.143f, 3480.963f, 25.67624f)), owning_building_guid = 66)
      LocalObject(1116, Locker.Constructor(Vector3(2659.143f, 3502.835f, 25.67624f)), owning_building_guid = 66)
      LocalObject(
        1597,
        Terminal.Constructor(Vector3(2659.445f, 3486.129f, 27.01424f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1598,
        Terminal.Constructor(Vector3(2659.445f, 3491.853f, 27.01424f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1599,
        Terminal.Constructor(Vector3(2659.445f, 3497.234f, 27.01424f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2192,
        SpawnTube.Constructor(Vector3(2648.706f, 3483.742f, 25.16424f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 66
      )
      LocalObject(
        2193,
        SpawnTube.Constructor(Vector3(2648.706f, 3500.152f, 25.16424f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 66
      )
      LocalObject(
        1887,
        ProximityTerminal.Constructor(Vector3(2636.907f, 3490.725f, 63.25224f), pad_landing_tower_frame),
        owning_building_guid = 66
      )
      LocalObject(
        1888,
        Terminal.Constructor(Vector3(2636.907f, 3490.725f, 63.25224f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1890,
        ProximityTerminal.Constructor(Vector3(2636.907f, 3501.17f, 63.25224f), pad_landing_tower_frame),
        owning_building_guid = 66
      )
      LocalObject(
        1891,
        Terminal.Constructor(Vector3(2636.907f, 3501.17f, 63.25224f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1457,
        FacilityTurret.Constructor(Vector3(2623.07f, 3481.045f, 54.62424f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(1457, 5081)
      LocalObject(
        1458,
        FacilityTurret.Constructor(Vector3(2661.497f, 3510.957f, 54.62424f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(1458, 5082)
      LocalObject(
        2019,
        Painbox.Constructor(Vector3(2642.454f, 3488.849f, 27.70174f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2020,
        Painbox.Constructor(Vector3(2654.923f, 3485.54f, 25.78224f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2021,
        Painbox.Constructor(Vector3(2655.113f, 3498.022f, 25.78224f), painbox_radius_continuous),
        owning_building_guid = 66
      )
    }

    Building29()

    def Building29(): Unit = { // Name: NE_Oshur_Warpgate_Tower Type: tower_c GUID: 67, MapID: 29
      LocalBuilding(
        "NE_Oshur_Warpgate_Tower",
        67,
        29,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3434f, 2144f, 25.65386f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2267,
        CaptureTerminal.Constructor(Vector3(3450.587f, 2143.897f, 35.65286f), secondary_capture),
        owning_building_guid = 67
      )
      LocalObject(366, Door.Constructor(Vector3(3446f, 2136f, 27.17486f)), owning_building_guid = 67)
      LocalObject(367, Door.Constructor(Vector3(3446f, 2136f, 47.17386f)), owning_building_guid = 67)
      LocalObject(368, Door.Constructor(Vector3(3446f, 2152f, 27.17486f)), owning_building_guid = 67)
      LocalObject(369, Door.Constructor(Vector3(3446f, 2152f, 47.17386f)), owning_building_guid = 67)
      LocalObject(2386, Door.Constructor(Vector3(3445.146f, 2132.794f, 16.98986f)), owning_building_guid = 67)
      LocalObject(2387, Door.Constructor(Vector3(3445.146f, 2149.204f, 16.98986f)), owning_building_guid = 67)
      LocalObject(
        919,
        IFFLock.Constructor(Vector3(3443.957f, 2152.811f, 27.11486f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 368
      )
      LocalObject(
        920,
        IFFLock.Constructor(Vector3(3443.957f, 2152.811f, 47.11486f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 369
      )
      LocalObject(
        921,
        IFFLock.Constructor(Vector3(3448.047f, 2135.189f, 27.11486f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 366
      )
      LocalObject(
        922,
        IFFLock.Constructor(Vector3(3448.047f, 2135.189f, 47.11486f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 367
      )
      LocalObject(1165, Locker.Constructor(Vector3(3449.716f, 2128.963f, 15.64786f)), owning_building_guid = 67)
      LocalObject(1166, Locker.Constructor(Vector3(3449.751f, 2150.835f, 15.64786f)), owning_building_guid = 67)
      LocalObject(1167, Locker.Constructor(Vector3(3451.053f, 2128.963f, 15.64786f)), owning_building_guid = 67)
      LocalObject(1168, Locker.Constructor(Vector3(3451.088f, 2150.835f, 15.64786f)), owning_building_guid = 67)
      LocalObject(1169, Locker.Constructor(Vector3(3453.741f, 2128.963f, 15.64786f)), owning_building_guid = 67)
      LocalObject(1170, Locker.Constructor(Vector3(3453.741f, 2150.835f, 15.64786f)), owning_building_guid = 67)
      LocalObject(1171, Locker.Constructor(Vector3(3455.143f, 2128.963f, 15.64786f)), owning_building_guid = 67)
      LocalObject(1172, Locker.Constructor(Vector3(3455.143f, 2150.835f, 15.64786f)), owning_building_guid = 67)
      LocalObject(
        1622,
        Terminal.Constructor(Vector3(3455.445f, 2134.129f, 16.98586f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1623,
        Terminal.Constructor(Vector3(3455.445f, 2139.853f, 16.98586f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1624,
        Terminal.Constructor(Vector3(3455.445f, 2145.234f, 16.98586f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        2206,
        SpawnTube.Constructor(Vector3(3444.706f, 2131.742f, 15.13586f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        2207,
        SpawnTube.Constructor(Vector3(3444.706f, 2148.152f, 15.13586f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        1893,
        ProximityTerminal.Constructor(Vector3(3432.907f, 2138.725f, 53.22386f), pad_landing_tower_frame),
        owning_building_guid = 67
      )
      LocalObject(
        1894,
        Terminal.Constructor(Vector3(3432.907f, 2138.725f, 53.22386f), air_rearm_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1896,
        ProximityTerminal.Constructor(Vector3(3432.907f, 2149.17f, 53.22386f), pad_landing_tower_frame),
        owning_building_guid = 67
      )
      LocalObject(
        1897,
        Terminal.Constructor(Vector3(3432.907f, 2149.17f, 53.22386f), air_rearm_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1471,
        FacilityTurret.Constructor(Vector3(3419.07f, 2129.045f, 44.59586f), manned_turret),
        owning_building_guid = 67
      )
      TurretToWeapon(1471, 5083)
      LocalObject(
        1473,
        FacilityTurret.Constructor(Vector3(3457.497f, 2158.957f, 44.59586f), manned_turret),
        owning_building_guid = 67
      )
      TurretToWeapon(1473, 5084)
      LocalObject(
        2031,
        Painbox.Constructor(Vector3(3438.454f, 2136.849f, 17.67336f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        2032,
        Painbox.Constructor(Vector3(3450.923f, 2133.54f, 15.75386f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        2033,
        Painbox.Constructor(Vector3(3451.113f, 2146.022f, 15.75386f), painbox_radius_continuous),
        owning_building_guid = 67
      )
    }

    Building18()

    def Building18(): Unit = { // Name: SE_Naum_Tower Type: tower_c GUID: 68, MapID: 18
      LocalBuilding(
        "SE_Naum_Tower",
        68,
        18,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3456f, 5278f, 21.2775f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2268,
        CaptureTerminal.Constructor(Vector3(3472.587f, 5277.897f, 31.2765f), secondary_capture),
        owning_building_guid = 68
      )
      LocalObject(373, Door.Constructor(Vector3(3468f, 5270f, 22.7985f)), owning_building_guid = 68)
      LocalObject(374, Door.Constructor(Vector3(3468f, 5270f, 42.7975f)), owning_building_guid = 68)
      LocalObject(375, Door.Constructor(Vector3(3468f, 5286f, 22.7985f)), owning_building_guid = 68)
      LocalObject(376, Door.Constructor(Vector3(3468f, 5286f, 42.7975f)), owning_building_guid = 68)
      LocalObject(2388, Door.Constructor(Vector3(3467.146f, 5266.794f, 12.6135f)), owning_building_guid = 68)
      LocalObject(2389, Door.Constructor(Vector3(3467.146f, 5283.204f, 12.6135f)), owning_building_guid = 68)
      LocalObject(
        923,
        IFFLock.Constructor(Vector3(3465.957f, 5286.811f, 22.7385f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 375
      )
      LocalObject(
        924,
        IFFLock.Constructor(Vector3(3465.957f, 5286.811f, 42.7385f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 376
      )
      LocalObject(
        925,
        IFFLock.Constructor(Vector3(3470.047f, 5269.189f, 22.7385f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 373
      )
      LocalObject(
        926,
        IFFLock.Constructor(Vector3(3470.047f, 5269.189f, 42.7385f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 374
      )
      LocalObject(1173, Locker.Constructor(Vector3(3471.716f, 5262.963f, 11.2715f)), owning_building_guid = 68)
      LocalObject(1174, Locker.Constructor(Vector3(3471.751f, 5284.835f, 11.2715f)), owning_building_guid = 68)
      LocalObject(1175, Locker.Constructor(Vector3(3473.053f, 5262.963f, 11.2715f)), owning_building_guid = 68)
      LocalObject(1176, Locker.Constructor(Vector3(3473.088f, 5284.835f, 11.2715f)), owning_building_guid = 68)
      LocalObject(1177, Locker.Constructor(Vector3(3475.741f, 5262.963f, 11.2715f)), owning_building_guid = 68)
      LocalObject(1178, Locker.Constructor(Vector3(3475.741f, 5284.835f, 11.2715f)), owning_building_guid = 68)
      LocalObject(1179, Locker.Constructor(Vector3(3477.143f, 5262.963f, 11.2715f)), owning_building_guid = 68)
      LocalObject(1180, Locker.Constructor(Vector3(3477.143f, 5284.835f, 11.2715f)), owning_building_guid = 68)
      LocalObject(
        1625,
        Terminal.Constructor(Vector3(3477.445f, 5268.129f, 12.6095f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1626,
        Terminal.Constructor(Vector3(3477.445f, 5273.853f, 12.6095f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1627,
        Terminal.Constructor(Vector3(3477.445f, 5279.234f, 12.6095f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        2208,
        SpawnTube.Constructor(Vector3(3466.706f, 5265.742f, 10.7595f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        2209,
        SpawnTube.Constructor(Vector3(3466.706f, 5282.152f, 10.7595f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        1899,
        ProximityTerminal.Constructor(Vector3(3454.907f, 5272.725f, 48.8475f), pad_landing_tower_frame),
        owning_building_guid = 68
      )
      LocalObject(
        1900,
        Terminal.Constructor(Vector3(3454.907f, 5272.725f, 48.8475f), air_rearm_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1902,
        ProximityTerminal.Constructor(Vector3(3454.907f, 5283.17f, 48.8475f), pad_landing_tower_frame),
        owning_building_guid = 68
      )
      LocalObject(
        1903,
        Terminal.Constructor(Vector3(3454.907f, 5283.17f, 48.8475f), air_rearm_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1472,
        FacilityTurret.Constructor(Vector3(3441.07f, 5263.045f, 40.21951f), manned_turret),
        owning_building_guid = 68
      )
      TurretToWeapon(1472, 5085)
      LocalObject(
        1475,
        FacilityTurret.Constructor(Vector3(3479.497f, 5292.957f, 40.21951f), manned_turret),
        owning_building_guid = 68
      )
      TurretToWeapon(1475, 5086)
      LocalObject(
        2034,
        Painbox.Constructor(Vector3(3460.454f, 5270.849f, 13.297f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        2035,
        Painbox.Constructor(Vector3(3472.923f, 5267.54f, 11.3775f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        2036,
        Painbox.Constructor(Vector3(3473.113f, 5280.022f, 11.3775f), painbox_radius_continuous),
        owning_building_guid = 68
      )
    }

    Building27()

    def Building27(): Unit = { // Name: SE_Oshur_Warpgate_Tower Type: tower_c GUID: 69, MapID: 27
      LocalBuilding(
        "SE_Oshur_Warpgate_Tower",
        69,
        27,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3716f, 1182f, 34.80941f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2270,
        CaptureTerminal.Constructor(Vector3(3732.587f, 1181.897f, 44.80841f), secondary_capture),
        owning_building_guid = 69
      )
      LocalObject(389, Door.Constructor(Vector3(3728f, 1174f, 36.33041f)), owning_building_guid = 69)
      LocalObject(390, Door.Constructor(Vector3(3728f, 1174f, 56.32941f)), owning_building_guid = 69)
      LocalObject(391, Door.Constructor(Vector3(3728f, 1190f, 36.33041f)), owning_building_guid = 69)
      LocalObject(392, Door.Constructor(Vector3(3728f, 1190f, 56.32941f)), owning_building_guid = 69)
      LocalObject(2395, Door.Constructor(Vector3(3727.146f, 1170.794f, 26.14541f)), owning_building_guid = 69)
      LocalObject(2396, Door.Constructor(Vector3(3727.146f, 1187.204f, 26.14541f)), owning_building_guid = 69)
      LocalObject(
        938,
        IFFLock.Constructor(Vector3(3725.957f, 1190.811f, 36.27041f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 391
      )
      LocalObject(
        939,
        IFFLock.Constructor(Vector3(3725.957f, 1190.811f, 56.27041f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 392
      )
      LocalObject(
        940,
        IFFLock.Constructor(Vector3(3730.047f, 1173.189f, 36.27041f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 389
      )
      LocalObject(
        941,
        IFFLock.Constructor(Vector3(3730.047f, 1173.189f, 56.27041f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 390
      )
      LocalObject(1210, Locker.Constructor(Vector3(3731.716f, 1166.963f, 24.80341f)), owning_building_guid = 69)
      LocalObject(1211, Locker.Constructor(Vector3(3731.751f, 1188.835f, 24.80341f)), owning_building_guid = 69)
      LocalObject(1212, Locker.Constructor(Vector3(3733.053f, 1166.963f, 24.80341f)), owning_building_guid = 69)
      LocalObject(1213, Locker.Constructor(Vector3(3733.088f, 1188.835f, 24.80341f)), owning_building_guid = 69)
      LocalObject(1214, Locker.Constructor(Vector3(3735.741f, 1166.963f, 24.80341f)), owning_building_guid = 69)
      LocalObject(1215, Locker.Constructor(Vector3(3735.741f, 1188.835f, 24.80341f)), owning_building_guid = 69)
      LocalObject(1216, Locker.Constructor(Vector3(3737.143f, 1166.963f, 24.80341f)), owning_building_guid = 69)
      LocalObject(1217, Locker.Constructor(Vector3(3737.143f, 1188.835f, 24.80341f)), owning_building_guid = 69)
      LocalObject(
        1635,
        Terminal.Constructor(Vector3(3737.445f, 1172.129f, 26.14141f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1636,
        Terminal.Constructor(Vector3(3737.445f, 1177.853f, 26.14141f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1637,
        Terminal.Constructor(Vector3(3737.445f, 1183.234f, 26.14141f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2215,
        SpawnTube.Constructor(Vector3(3726.706f, 1169.742f, 24.29141f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        2216,
        SpawnTube.Constructor(Vector3(3726.706f, 1186.152f, 24.29141f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        1905,
        ProximityTerminal.Constructor(Vector3(3714.907f, 1176.725f, 62.37941f), pad_landing_tower_frame),
        owning_building_guid = 69
      )
      LocalObject(
        1906,
        Terminal.Constructor(Vector3(3714.907f, 1176.725f, 62.37941f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1908,
        ProximityTerminal.Constructor(Vector3(3714.907f, 1187.17f, 62.37941f), pad_landing_tower_frame),
        owning_building_guid = 69
      )
      LocalObject(
        1909,
        Terminal.Constructor(Vector3(3714.907f, 1187.17f, 62.37941f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1482,
        FacilityTurret.Constructor(Vector3(3701.07f, 1167.045f, 53.75141f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(1482, 5087)
      LocalObject(
        1483,
        FacilityTurret.Constructor(Vector3(3739.497f, 1196.957f, 53.75141f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(1483, 5088)
      LocalObject(
        2040,
        Painbox.Constructor(Vector3(3720.454f, 1174.849f, 26.82891f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2041,
        Painbox.Constructor(Vector3(3732.923f, 1171.54f, 24.90941f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2042,
        Painbox.Constructor(Vector3(3733.113f, 1184.022f, 24.90941f), painbox_radius_continuous),
        owning_building_guid = 69
      )
    }

    Building20()

    def Building20(): Unit = { // Name: E_Chac_Tower Type: tower_c GUID: 70, MapID: 20
      LocalBuilding(
        "E_Chac_Tower",
        70,
        20,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4288f, 5776f, 26.28254f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2272,
        CaptureTerminal.Constructor(Vector3(4304.587f, 5775.897f, 36.28154f), secondary_capture),
        owning_building_guid = 70
      )
      LocalObject(412, Door.Constructor(Vector3(4300f, 5768f, 27.80354f)), owning_building_guid = 70)
      LocalObject(413, Door.Constructor(Vector3(4300f, 5768f, 47.80254f)), owning_building_guid = 70)
      LocalObject(414, Door.Constructor(Vector3(4300f, 5784f, 27.80354f)), owning_building_guid = 70)
      LocalObject(415, Door.Constructor(Vector3(4300f, 5784f, 47.80254f)), owning_building_guid = 70)
      LocalObject(2402, Door.Constructor(Vector3(4299.146f, 5764.794f, 17.61854f)), owning_building_guid = 70)
      LocalObject(2403, Door.Constructor(Vector3(4299.146f, 5781.204f, 17.61854f)), owning_building_guid = 70)
      LocalObject(
        954,
        IFFLock.Constructor(Vector3(4297.957f, 5784.811f, 27.74354f), Vector3(0, 0, 0)),
        owning_building_guid = 70,
        door_guid = 414
      )
      LocalObject(
        955,
        IFFLock.Constructor(Vector3(4297.957f, 5784.811f, 47.74354f), Vector3(0, 0, 0)),
        owning_building_guid = 70,
        door_guid = 415
      )
      LocalObject(
        956,
        IFFLock.Constructor(Vector3(4302.047f, 5767.189f, 27.74354f), Vector3(0, 0, 180)),
        owning_building_guid = 70,
        door_guid = 412
      )
      LocalObject(
        957,
        IFFLock.Constructor(Vector3(4302.047f, 5767.189f, 47.74354f), Vector3(0, 0, 180)),
        owning_building_guid = 70,
        door_guid = 413
      )
      LocalObject(1238, Locker.Constructor(Vector3(4303.716f, 5760.963f, 16.27654f)), owning_building_guid = 70)
      LocalObject(1239, Locker.Constructor(Vector3(4303.751f, 5782.835f, 16.27654f)), owning_building_guid = 70)
      LocalObject(1240, Locker.Constructor(Vector3(4305.053f, 5760.963f, 16.27654f)), owning_building_guid = 70)
      LocalObject(1241, Locker.Constructor(Vector3(4305.088f, 5782.835f, 16.27654f)), owning_building_guid = 70)
      LocalObject(1242, Locker.Constructor(Vector3(4307.741f, 5760.963f, 16.27654f)), owning_building_guid = 70)
      LocalObject(1243, Locker.Constructor(Vector3(4307.741f, 5782.835f, 16.27654f)), owning_building_guid = 70)
      LocalObject(1244, Locker.Constructor(Vector3(4309.143f, 5760.963f, 16.27654f)), owning_building_guid = 70)
      LocalObject(1245, Locker.Constructor(Vector3(4309.143f, 5782.835f, 16.27654f)), owning_building_guid = 70)
      LocalObject(
        1645,
        Terminal.Constructor(Vector3(4309.445f, 5766.129f, 17.61454f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1646,
        Terminal.Constructor(Vector3(4309.445f, 5771.853f, 17.61454f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1647,
        Terminal.Constructor(Vector3(4309.445f, 5777.234f, 17.61454f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        2222,
        SpawnTube.Constructor(Vector3(4298.706f, 5763.742f, 15.76454f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 70
      )
      LocalObject(
        2223,
        SpawnTube.Constructor(Vector3(4298.706f, 5780.152f, 15.76454f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 70
      )
      LocalObject(
        1911,
        ProximityTerminal.Constructor(Vector3(4286.907f, 5770.725f, 53.85254f), pad_landing_tower_frame),
        owning_building_guid = 70
      )
      LocalObject(
        1912,
        Terminal.Constructor(Vector3(4286.907f, 5770.725f, 53.85254f), air_rearm_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1914,
        ProximityTerminal.Constructor(Vector3(4286.907f, 5781.17f, 53.85254f), pad_landing_tower_frame),
        owning_building_guid = 70
      )
      LocalObject(
        1915,
        Terminal.Constructor(Vector3(4286.907f, 5781.17f, 53.85254f), air_rearm_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1492,
        FacilityTurret.Constructor(Vector3(4273.07f, 5761.045f, 45.22454f), manned_turret),
        owning_building_guid = 70
      )
      TurretToWeapon(1492, 5089)
      LocalObject(
        1493,
        FacilityTurret.Constructor(Vector3(4311.497f, 5790.957f, 45.22454f), manned_turret),
        owning_building_guid = 70
      )
      TurretToWeapon(1493, 5090)
      LocalObject(
        2046,
        Painbox.Constructor(Vector3(4292.454f, 5768.849f, 18.30204f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2047,
        Painbox.Constructor(Vector3(4304.923f, 5765.54f, 16.38254f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2048,
        Painbox.Constructor(Vector3(4305.113f, 5778.022f, 16.38254f), painbox_radius_continuous),
        owning_building_guid = 70
      )
    }

    Building21()

    def Building21(): Unit = { // Name: NW_Solsar_Warpgate_Tower Type: tower_c GUID: 71, MapID: 21
      LocalBuilding(
        "NW_Solsar_Warpgate_Tower",
        71,
        21,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4480f, 4726f, 35.56017f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2273,
        CaptureTerminal.Constructor(Vector3(4496.587f, 4725.897f, 45.55917f), secondary_capture),
        owning_building_guid = 71
      )
      LocalObject(432, Door.Constructor(Vector3(4492f, 4718f, 37.08117f)), owning_building_guid = 71)
      LocalObject(433, Door.Constructor(Vector3(4492f, 4718f, 57.08017f)), owning_building_guid = 71)
      LocalObject(434, Door.Constructor(Vector3(4492f, 4734f, 37.08117f)), owning_building_guid = 71)
      LocalObject(435, Door.Constructor(Vector3(4492f, 4734f, 57.08017f)), owning_building_guid = 71)
      LocalObject(2407, Door.Constructor(Vector3(4491.146f, 4714.794f, 26.89617f)), owning_building_guid = 71)
      LocalObject(2408, Door.Constructor(Vector3(4491.146f, 4731.204f, 26.89617f)), owning_building_guid = 71)
      LocalObject(
        966,
        IFFLock.Constructor(Vector3(4489.957f, 4734.811f, 37.02116f), Vector3(0, 0, 0)),
        owning_building_guid = 71,
        door_guid = 434
      )
      LocalObject(
        967,
        IFFLock.Constructor(Vector3(4489.957f, 4734.811f, 57.02116f), Vector3(0, 0, 0)),
        owning_building_guid = 71,
        door_guid = 435
      )
      LocalObject(
        971,
        IFFLock.Constructor(Vector3(4494.047f, 4717.189f, 37.02116f), Vector3(0, 0, 180)),
        owning_building_guid = 71,
        door_guid = 432
      )
      LocalObject(
        972,
        IFFLock.Constructor(Vector3(4494.047f, 4717.189f, 57.02116f), Vector3(0, 0, 180)),
        owning_building_guid = 71,
        door_guid = 433
      )
      LocalObject(1247, Locker.Constructor(Vector3(4495.716f, 4710.963f, 25.55416f)), owning_building_guid = 71)
      LocalObject(1248, Locker.Constructor(Vector3(4495.751f, 4732.835f, 25.55416f)), owning_building_guid = 71)
      LocalObject(1250, Locker.Constructor(Vector3(4497.053f, 4710.963f, 25.55416f)), owning_building_guid = 71)
      LocalObject(1251, Locker.Constructor(Vector3(4497.088f, 4732.835f, 25.55416f)), owning_building_guid = 71)
      LocalObject(1254, Locker.Constructor(Vector3(4499.741f, 4710.963f, 25.55416f)), owning_building_guid = 71)
      LocalObject(1255, Locker.Constructor(Vector3(4499.741f, 4732.835f, 25.55416f)), owning_building_guid = 71)
      LocalObject(1261, Locker.Constructor(Vector3(4501.143f, 4710.963f, 25.55416f)), owning_building_guid = 71)
      LocalObject(1262, Locker.Constructor(Vector3(4501.143f, 4732.835f, 25.55416f)), owning_building_guid = 71)
      LocalObject(
        1656,
        Terminal.Constructor(Vector3(4501.445f, 4716.129f, 26.89217f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1657,
        Terminal.Constructor(Vector3(4501.445f, 4721.853f, 26.89217f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1658,
        Terminal.Constructor(Vector3(4501.445f, 4727.234f, 26.89217f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        2227,
        SpawnTube.Constructor(Vector3(4490.706f, 4713.742f, 25.04217f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 71
      )
      LocalObject(
        2228,
        SpawnTube.Constructor(Vector3(4490.706f, 4730.152f, 25.04217f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 71
      )
      LocalObject(
        1917,
        ProximityTerminal.Constructor(Vector3(4478.907f, 4720.725f, 63.13017f), pad_landing_tower_frame),
        owning_building_guid = 71
      )
      LocalObject(
        1918,
        Terminal.Constructor(Vector3(4478.907f, 4720.725f, 63.13017f), air_rearm_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1920,
        ProximityTerminal.Constructor(Vector3(4478.907f, 4731.17f, 63.13017f), pad_landing_tower_frame),
        owning_building_guid = 71
      )
      LocalObject(
        1921,
        Terminal.Constructor(Vector3(4478.907f, 4731.17f, 63.13017f), air_rearm_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1499,
        FacilityTurret.Constructor(Vector3(4465.07f, 4711.045f, 54.50217f), manned_turret),
        owning_building_guid = 71
      )
      TurretToWeapon(1499, 5091)
      LocalObject(
        1501,
        FacilityTurret.Constructor(Vector3(4503.497f, 4740.957f, 54.50217f), manned_turret),
        owning_building_guid = 71
      )
      TurretToWeapon(1501, 5092)
      LocalObject(
        2049,
        Painbox.Constructor(Vector3(4484.454f, 4718.849f, 27.57967f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2050,
        Painbox.Constructor(Vector3(4496.923f, 4715.54f, 25.66017f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2051,
        Painbox.Constructor(Vector3(4497.113f, 4728.022f, 25.66017f), painbox_radius_continuous),
        owning_building_guid = 71
      )
    }

    Building26()

    def Building26(): Unit = { // Name: E_Bitol_Tower Type: tower_c GUID: 72, MapID: 26
      LocalBuilding(
        "E_Bitol_Tower",
        72,
        26,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4702f, 2460f, 28.31507f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2275,
        CaptureTerminal.Constructor(Vector3(4718.587f, 2459.897f, 38.31407f), secondary_capture),
        owning_building_guid = 72
      )
      LocalObject(453, Door.Constructor(Vector3(4714f, 2452f, 29.83607f)), owning_building_guid = 72)
      LocalObject(454, Door.Constructor(Vector3(4714f, 2452f, 49.83508f)), owning_building_guid = 72)
      LocalObject(455, Door.Constructor(Vector3(4714f, 2468f, 29.83607f)), owning_building_guid = 72)
      LocalObject(456, Door.Constructor(Vector3(4714f, 2468f, 49.83508f)), owning_building_guid = 72)
      LocalObject(2414, Door.Constructor(Vector3(4713.146f, 2448.794f, 19.65107f)), owning_building_guid = 72)
      LocalObject(2415, Door.Constructor(Vector3(4713.146f, 2465.204f, 19.65107f)), owning_building_guid = 72)
      LocalObject(
        987,
        IFFLock.Constructor(Vector3(4711.957f, 2468.811f, 29.77607f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 455
      )
      LocalObject(
        988,
        IFFLock.Constructor(Vector3(4711.957f, 2468.811f, 49.77607f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 456
      )
      LocalObject(
        989,
        IFFLock.Constructor(Vector3(4716.047f, 2451.189f, 29.77607f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 453
      )
      LocalObject(
        990,
        IFFLock.Constructor(Vector3(4716.047f, 2451.189f, 49.77607f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 454
      )
      LocalObject(1295, Locker.Constructor(Vector3(4717.716f, 2444.963f, 18.30907f)), owning_building_guid = 72)
      LocalObject(1296, Locker.Constructor(Vector3(4717.751f, 2466.835f, 18.30907f)), owning_building_guid = 72)
      LocalObject(1297, Locker.Constructor(Vector3(4719.053f, 2444.963f, 18.30907f)), owning_building_guid = 72)
      LocalObject(1298, Locker.Constructor(Vector3(4719.088f, 2466.835f, 18.30907f)), owning_building_guid = 72)
      LocalObject(1299, Locker.Constructor(Vector3(4721.741f, 2444.963f, 18.30907f)), owning_building_guid = 72)
      LocalObject(1300, Locker.Constructor(Vector3(4721.741f, 2466.835f, 18.30907f)), owning_building_guid = 72)
      LocalObject(1301, Locker.Constructor(Vector3(4723.143f, 2444.963f, 18.30907f)), owning_building_guid = 72)
      LocalObject(1302, Locker.Constructor(Vector3(4723.143f, 2466.835f, 18.30907f)), owning_building_guid = 72)
      LocalObject(
        1665,
        Terminal.Constructor(Vector3(4723.445f, 2450.129f, 19.64707f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1666,
        Terminal.Constructor(Vector3(4723.445f, 2455.853f, 19.64707f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1667,
        Terminal.Constructor(Vector3(4723.445f, 2461.234f, 19.64707f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2234,
        SpawnTube.Constructor(Vector3(4712.706f, 2447.742f, 17.79707f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        2235,
        SpawnTube.Constructor(Vector3(4712.706f, 2464.152f, 17.79707f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        1923,
        ProximityTerminal.Constructor(Vector3(4700.907f, 2454.725f, 55.88507f), pad_landing_tower_frame),
        owning_building_guid = 72
      )
      LocalObject(
        1924,
        Terminal.Constructor(Vector3(4700.907f, 2454.725f, 55.88507f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1926,
        ProximityTerminal.Constructor(Vector3(4700.907f, 2465.17f, 55.88507f), pad_landing_tower_frame),
        owning_building_guid = 72
      )
      LocalObject(
        1927,
        Terminal.Constructor(Vector3(4700.907f, 2465.17f, 55.88507f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1507,
        FacilityTurret.Constructor(Vector3(4687.07f, 2445.045f, 47.25707f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(1507, 5093)
      LocalObject(
        1508,
        FacilityTurret.Constructor(Vector3(4725.497f, 2474.957f, 47.25707f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(1508, 5094)
      LocalObject(
        2055,
        Painbox.Constructor(Vector3(4706.454f, 2452.849f, 20.33457f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2056,
        Painbox.Constructor(Vector3(4718.923f, 2449.54f, 18.41507f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2057,
        Painbox.Constructor(Vector3(4719.113f, 2462.022f, 18.41507f), painbox_radius_continuous),
        owning_building_guid = 72
      )
    }

    Building25()

    def Building25(): Unit = { // Name: NW_Mulac_Tower Type: tower_c GUID: 73, MapID: 25
      LocalBuilding(
        "NW_Mulac_Tower",
        73,
        25,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5308f, 2976f, 27.19199f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2278,
        CaptureTerminal.Constructor(Vector3(5324.587f, 2975.897f, 37.19099f), secondary_capture),
        owning_building_guid = 73
      )
      LocalObject(467, Door.Constructor(Vector3(5320f, 2968f, 28.71299f)), owning_building_guid = 73)
      LocalObject(468, Door.Constructor(Vector3(5320f, 2968f, 48.71199f)), owning_building_guid = 73)
      LocalObject(469, Door.Constructor(Vector3(5320f, 2984f, 28.71299f)), owning_building_guid = 73)
      LocalObject(470, Door.Constructor(Vector3(5320f, 2984f, 48.71199f)), owning_building_guid = 73)
      LocalObject(2420, Door.Constructor(Vector3(5319.146f, 2964.794f, 18.52799f)), owning_building_guid = 73)
      LocalObject(2421, Door.Constructor(Vector3(5319.146f, 2981.204f, 18.52799f)), owning_building_guid = 73)
      LocalObject(
        1001,
        IFFLock.Constructor(Vector3(5317.957f, 2984.811f, 28.65299f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 469
      )
      LocalObject(
        1002,
        IFFLock.Constructor(Vector3(5317.957f, 2984.811f, 48.65299f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 470
      )
      LocalObject(
        1003,
        IFFLock.Constructor(Vector3(5322.047f, 2967.189f, 28.65299f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 467
      )
      LocalObject(
        1004,
        IFFLock.Constructor(Vector3(5322.047f, 2967.189f, 48.65299f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 468
      )
      LocalObject(1319, Locker.Constructor(Vector3(5323.716f, 2960.963f, 17.18599f)), owning_building_guid = 73)
      LocalObject(1320, Locker.Constructor(Vector3(5323.751f, 2982.835f, 17.18599f)), owning_building_guid = 73)
      LocalObject(1321, Locker.Constructor(Vector3(5325.053f, 2960.963f, 17.18599f)), owning_building_guid = 73)
      LocalObject(1322, Locker.Constructor(Vector3(5325.088f, 2982.835f, 17.18599f)), owning_building_guid = 73)
      LocalObject(1323, Locker.Constructor(Vector3(5327.741f, 2960.963f, 17.18599f)), owning_building_guid = 73)
      LocalObject(1324, Locker.Constructor(Vector3(5327.741f, 2982.835f, 17.18599f)), owning_building_guid = 73)
      LocalObject(1325, Locker.Constructor(Vector3(5329.143f, 2960.963f, 17.18599f)), owning_building_guid = 73)
      LocalObject(1326, Locker.Constructor(Vector3(5329.143f, 2982.835f, 17.18599f)), owning_building_guid = 73)
      LocalObject(
        1674,
        Terminal.Constructor(Vector3(5329.445f, 2966.129f, 18.52399f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1675,
        Terminal.Constructor(Vector3(5329.445f, 2971.853f, 18.52399f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1676,
        Terminal.Constructor(Vector3(5329.445f, 2977.234f, 18.52399f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        2240,
        SpawnTube.Constructor(Vector3(5318.706f, 2963.742f, 16.67399f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        2241,
        SpawnTube.Constructor(Vector3(5318.706f, 2980.152f, 16.67399f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        1929,
        ProximityTerminal.Constructor(Vector3(5306.907f, 2970.725f, 54.76199f), pad_landing_tower_frame),
        owning_building_guid = 73
      )
      LocalObject(
        1930,
        Terminal.Constructor(Vector3(5306.907f, 2970.725f, 54.76199f), air_rearm_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1932,
        ProximityTerminal.Constructor(Vector3(5306.907f, 2981.17f, 54.76199f), pad_landing_tower_frame),
        owning_building_guid = 73
      )
      LocalObject(
        1933,
        Terminal.Constructor(Vector3(5306.907f, 2981.17f, 54.76199f), air_rearm_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1511,
        FacilityTurret.Constructor(Vector3(5293.07f, 2961.045f, 46.13399f), manned_turret),
        owning_building_guid = 73
      )
      TurretToWeapon(1511, 5095)
      LocalObject(
        1514,
        FacilityTurret.Constructor(Vector3(5331.497f, 2990.957f, 46.13399f), manned_turret),
        owning_building_guid = 73
      )
      TurretToWeapon(1514, 5096)
      LocalObject(
        2064,
        Painbox.Constructor(Vector3(5312.454f, 2968.849f, 19.21149f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2065,
        Painbox.Constructor(Vector3(5324.923f, 2965.54f, 17.29199f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2066,
        Painbox.Constructor(Vector3(5325.113f, 2978.022f, 17.29199f), painbox_radius_continuous),
        owning_building_guid = 73
      )
    }

    Building51()

    def Building51(): Unit = { // Name: Zotz_Tower Type: tower_c GUID: 74, MapID: 51
      LocalBuilding(
        "Zotz_Tower",
        74,
        51,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6680f, 2510f, 181.3593f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2282,
        CaptureTerminal.Constructor(Vector3(6696.587f, 2509.897f, 191.3583f), secondary_capture),
        owning_building_guid = 74
      )
      LocalObject(523, Door.Constructor(Vector3(6692f, 2502f, 182.8803f)), owning_building_guid = 74)
      LocalObject(524, Door.Constructor(Vector3(6692f, 2502f, 202.8793f)), owning_building_guid = 74)
      LocalObject(525, Door.Constructor(Vector3(6692f, 2518f, 182.8803f)), owning_building_guid = 74)
      LocalObject(526, Door.Constructor(Vector3(6692f, 2518f, 202.8793f)), owning_building_guid = 74)
      LocalObject(2434, Door.Constructor(Vector3(6691.146f, 2498.794f, 172.6953f)), owning_building_guid = 74)
      LocalObject(2435, Door.Constructor(Vector3(6691.146f, 2515.204f, 172.6953f)), owning_building_guid = 74)
      LocalObject(
        1043,
        IFFLock.Constructor(Vector3(6689.957f, 2518.811f, 182.8203f), Vector3(0, 0, 0)),
        owning_building_guid = 74,
        door_guid = 525
      )
      LocalObject(
        1044,
        IFFLock.Constructor(Vector3(6689.957f, 2518.811f, 202.8203f), Vector3(0, 0, 0)),
        owning_building_guid = 74,
        door_guid = 526
      )
      LocalObject(
        1045,
        IFFLock.Constructor(Vector3(6694.047f, 2501.189f, 182.8203f), Vector3(0, 0, 180)),
        owning_building_guid = 74,
        door_guid = 523
      )
      LocalObject(
        1046,
        IFFLock.Constructor(Vector3(6694.047f, 2501.189f, 202.8203f), Vector3(0, 0, 180)),
        owning_building_guid = 74,
        door_guid = 524
      )
      LocalObject(1380, Locker.Constructor(Vector3(6695.716f, 2494.963f, 171.3533f)), owning_building_guid = 74)
      LocalObject(1381, Locker.Constructor(Vector3(6695.751f, 2516.835f, 171.3533f)), owning_building_guid = 74)
      LocalObject(1382, Locker.Constructor(Vector3(6697.053f, 2494.963f, 171.3533f)), owning_building_guid = 74)
      LocalObject(1383, Locker.Constructor(Vector3(6697.088f, 2516.835f, 171.3533f)), owning_building_guid = 74)
      LocalObject(1384, Locker.Constructor(Vector3(6699.741f, 2494.963f, 171.3533f)), owning_building_guid = 74)
      LocalObject(1385, Locker.Constructor(Vector3(6699.741f, 2516.835f, 171.3533f)), owning_building_guid = 74)
      LocalObject(1386, Locker.Constructor(Vector3(6701.143f, 2494.963f, 171.3533f)), owning_building_guid = 74)
      LocalObject(1387, Locker.Constructor(Vector3(6701.143f, 2516.835f, 171.3533f)), owning_building_guid = 74)
      LocalObject(
        1698,
        Terminal.Constructor(Vector3(6701.445f, 2500.129f, 172.6913f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        1699,
        Terminal.Constructor(Vector3(6701.445f, 2505.853f, 172.6913f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        1700,
        Terminal.Constructor(Vector3(6701.445f, 2511.234f, 172.6913f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        2254,
        SpawnTube.Constructor(Vector3(6690.706f, 2497.742f, 170.8413f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 74
      )
      LocalObject(
        2255,
        SpawnTube.Constructor(Vector3(6690.706f, 2514.152f, 170.8413f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 74
      )
      LocalObject(
        1935,
        ProximityTerminal.Constructor(Vector3(6678.907f, 2504.725f, 208.9293f), pad_landing_tower_frame),
        owning_building_guid = 74
      )
      LocalObject(
        1936,
        Terminal.Constructor(Vector3(6678.907f, 2504.725f, 208.9293f), air_rearm_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        1938,
        ProximityTerminal.Constructor(Vector3(6678.907f, 2515.17f, 208.9293f), pad_landing_tower_frame),
        owning_building_guid = 74
      )
      LocalObject(
        1939,
        Terminal.Constructor(Vector3(6678.907f, 2515.17f, 208.9293f), air_rearm_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        1533,
        FacilityTurret.Constructor(Vector3(6665.07f, 2495.045f, 200.3013f), manned_turret),
        owning_building_guid = 74
      )
      TurretToWeapon(1533, 5097)
      LocalObject(
        1535,
        FacilityTurret.Constructor(Vector3(6703.497f, 2524.957f, 200.3013f), manned_turret),
        owning_building_guid = 74
      )
      TurretToWeapon(1535, 5098)
      LocalObject(
        2076,
        Painbox.Constructor(Vector3(6684.454f, 2502.849f, 173.3788f), painbox_radius_continuous),
        owning_building_guid = 74
      )
      LocalObject(
        2077,
        Painbox.Constructor(Vector3(6696.923f, 2499.54f, 171.4593f), painbox_radius_continuous),
        owning_building_guid = 74
      )
      LocalObject(
        2078,
        Painbox.Constructor(Vector3(6697.113f, 2512.022f, 171.4593f), painbox_radius_continuous),
        owning_building_guid = 74
      )
    }

    Building1()

    def Building1(): Unit = { // Name: WG_Hossin_to_Ceryshen Type: warpgate GUID: 75, MapID: 1
      LocalBuilding(
        "WG_Hossin_to_Ceryshen",
        75,
        1,
        FoundationBuilder(WarpGate.Structure(Vector3(1874f, 5018f, 18.43318f)))
      )
    }

    Building4()

    def Building4(): Unit = { // Name: WG_Hossin_to_Oshur Type: warpgate GUID: 76, MapID: 4
      LocalBuilding(
        "WG_Hossin_to_Oshur",
        76,
        4,
        FoundationBuilder(WarpGate.Structure(Vector3(3180f, 1978f, 20.37416f)))
      )
    }

    Building2()

    def Building2(): Unit = { // Name: WG_Hossin_to_Solsar Type: warpgate GUID: 77, MapID: 2
      LocalBuilding(
        "WG_Hossin_to_Solsar",
        77,
        2,
        FoundationBuilder(WarpGate.Structure(Vector3(4774f, 4558f, 26.12384f)))
      )
    }

    Building3()

    def Building3(): Unit = { // Name: WG_Hossin_to_VSSanc Type: warpgate GUID: 78, MapID: 3
      LocalBuilding(
        "WG_Hossin_to_VSSanc",
        78,
        3,
        FoundationBuilder(WarpGate.Structure(Vector3(5466f, 1710f, 25.7149f)))
      )
    }

    def Lattice(): Unit = {
      LatticeLink("Chac", "Kisin")
      LatticeLink("Bitol", "Naum")
      LatticeLink("Naum", "Mulac")
      LatticeLink("Naum", "Zotz")
      LatticeLink("Mulac", "Zotz")
      LatticeLink("Voltan", "Acan")
      LatticeLink("Voltan", "Ixtab")
      LatticeLink("Voltan", "Bitol")
      LatticeLink("Voltan", "Naum")
      LatticeLink("Chac", "WG_Hossin_to_Solsar")
      LatticeLink("Ghanon", "WG_Hossin_to_Ceryshen")
      LatticeLink("Chac", "Acan")
      LatticeLink("Mulac", "WG_Hossin_to_VSSanc")
      LatticeLink("Naum", "GW_Hossin_N")
      LatticeLink("Bitol", "GW_Hossin_S")
      LatticeLink("Hurakan", "WG_Hossin_to_Oshur")
      LatticeLink("Acan", "Naum")
      LatticeLink("Kisin", "Acan")
      LatticeLink("Kisin", "Ghanon")
      LatticeLink("Ghanon", "Ixtab")
      LatticeLink("Ghanon", "Hurakan")
      LatticeLink("Hurakan", "Bitol")
      LatticeLink("Acan", "Ixtab")
      LatticeLink("Ixtab", "Bitol")
    }

    Lattice()

  }
}
