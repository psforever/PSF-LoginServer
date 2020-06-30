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

object Map06 { // Ceryshen
  val ZoneMap = new ZoneMap("map06") {
    Checksum = 579139514L

    Building1()

    def Building1(): Unit = { // Name: Akna Type: amp_station GUID: 1, MapID: 1
      LocalBuilding(
        "Akna",
        1,
        1,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4406f, 3738f, 218.9079f),
            Vector3(0f, 0f, 312f),
            amp_station
          )
        )
      )
      LocalObject(
        181,
        CaptureTerminal.Constructor(Vector3(4403.77f, 3740.481f, 230.4159f), capture_terminal),
        owning_building_guid = 1
      )
      LocalObject(146, Door.Constructor(Vector3(4401.084f, 3733.29f, 231.8099f)), owning_building_guid = 1)
      LocalObject(147, Door.Constructor(Vector3(4411.2f, 3742.394f, 231.8099f)), owning_building_guid = 1)
      LocalObject(423, Door.Constructor(Vector3(4319.27f, 3751.38f, 220.6589f)), owning_building_guid = 1)
      LocalObject(424, Door.Constructor(Vector3(4332.79f, 3763.553f, 228.6219f)), owning_building_guid = 1)
      LocalObject(425, Door.Constructor(Vector3(4355.125f, 3668.64f, 228.6229f)), owning_building_guid = 1)
      LocalObject(434, Door.Constructor(Vector3(4367.298f, 3655.121f, 220.6589f)), owning_building_guid = 1)
      LocalObject(435, Door.Constructor(Vector3(4379.618f, 3714.246f, 225.6289f)), owning_building_guid = 1)
      LocalObject(436, Door.Constructor(Vector3(4380.379f, 3720.759f, 230.6349f)), owning_building_guid = 1)
      LocalObject(437, Door.Constructor(Vector3(4386.443f, 3714.026f, 230.6349f)), owning_building_guid = 1)
      LocalObject(439, Door.Constructor(Vector3(4394.482f, 3819.101f, 220.6589f)), owning_building_guid = 1)
      LocalObject(440, Door.Constructor(Vector3(4399.694f, 3651.311f, 228.6229f)), owning_building_guid = 1)
      LocalObject(441, Door.Constructor(Vector3(4408.002f, 3831.275f, 228.6219f)), owning_building_guid = 1)
      LocalObject(442, Door.Constructor(Vector3(4413.213f, 3663.483f, 220.6589f)), owning_building_guid = 1)
      LocalObject(443, Door.Constructor(Vector3(4414.598f, 3848.01f, 220.6289f)), owning_building_guid = 1)
      LocalObject(444, Door.Constructor(Vector3(4425.838f, 3761.662f, 230.6349f)), owning_building_guid = 1)
      LocalObject(445, Door.Constructor(Vector3(4431.901f, 3754.929f, 230.6349f)), owning_building_guid = 1)
      LocalObject(446, Door.Constructor(Vector3(4432.382f, 3761.755f, 225.6289f)), owning_building_guid = 1)
      LocalObject(447, Door.Constructor(Vector3(4458.782f, 3817.15f, 220.6589f)), owning_building_guid = 1)
      LocalObject(448, Door.Constructor(Vector3(4470.955f, 3803.631f, 228.6229f)), owning_building_guid = 1)
      LocalObject(647, Door.Constructor(Vector3(4370.909f, 3770.994f, 205.6289f)), owning_building_guid = 1)
      LocalObject(648, Door.Constructor(Vector3(4375.67f, 3753.751f, 205.6289f)), owning_building_guid = 1)
      LocalObject(649, Door.Constructor(Vector3(4376.558f, 3770.698f, 205.6289f)), owning_building_guid = 1)
      LocalObject(650, Door.Constructor(Vector3(4378.631f, 3810.242f, 213.1289f)), owning_building_guid = 1)
      LocalObject(651, Door.Constructor(Vector3(4379.839f, 3725.209f, 220.6289f)), owning_building_guid = 1)
      LocalObject(652, Door.Constructor(Vector3(4380.135f, 3730.859f, 220.6289f)), owning_building_guid = 1)
      LocalObject(653, Door.Constructor(Vector3(4386.08f, 3736.212f, 205.6289f)), owning_building_guid = 1)
      LocalObject(654, Door.Constructor(Vector3(4386.08f, 3736.212f, 213.1289f)), owning_building_guid = 1)
      LocalObject(655, Door.Constructor(Vector3(4386.397f, 3720.064f, 230.6289f)), owning_building_guid = 1)
      LocalObject(656, Door.Constructor(Vector3(4388.165f, 3721.941f, 225.6289f)), owning_building_guid = 1)
      LocalObject(657, Door.Constructor(Vector3(4397.082f, 3729.97f, 205.6289f)), owning_building_guid = 1)
      LocalObject(658, Door.Constructor(Vector3(4403.916f, 3752.271f, 213.1289f)), owning_building_guid = 1)
      LocalObject(659, Door.Constructor(Vector3(4405.1f, 3774.867f, 213.1289f)), owning_building_guid = 1)
      LocalObject(660, Door.Constructor(Vector3(4408.973f, 3740.677f, 213.1289f)), owning_building_guid = 1)
      LocalObject(661, Door.Constructor(Vector3(4415.214f, 3751.679f, 205.6289f)), owning_building_guid = 1)
      LocalObject(662, Door.Constructor(Vector3(4415.51f, 3757.328f, 213.1289f)), owning_building_guid = 1)
      LocalObject(663, Door.Constructor(Vector3(4415.806f, 3762.977f, 220.6289f)), owning_building_guid = 1)
      LocalObject(664, Door.Constructor(Vector3(4416.41f, 3720.461f, 220.6289f)), owning_building_guid = 1)
      LocalObject(665, Door.Constructor(Vector3(4421.455f, 3762.681f, 220.6289f)), owning_building_guid = 1)
      LocalObject(666, Door.Constructor(Vector3(4422.355f, 3725.814f, 220.6289f)), owning_building_guid = 1)
      LocalObject(667, Door.Constructor(Vector3(4423.835f, 3754.059f, 225.6289f)), owning_building_guid = 1)
      LocalObject(668, Door.Constructor(Vector3(4425.886f, 3755.621f, 230.6289f)), owning_building_guid = 1)
      LocalObject(717, Door.Constructor(Vector3(4425.541f, 3716.337f, 221.3879f)), owning_building_guid = 1)
      LocalObject(2335, Door.Constructor(Vector3(4385.438f, 3745.493f, 213.4619f)), owning_building_guid = 1)
      LocalObject(2336, Door.Constructor(Vector3(4390.857f, 3750.373f, 213.4619f)), owning_building_guid = 1)
      LocalObject(2337, Door.Constructor(Vector3(4396.274f, 3755.25f, 213.4619f)), owning_building_guid = 1)
      LocalObject(
        753,
        IFFLock.Constructor(Vector3(4429.634f, 3716.334f, 220.5879f), Vector3(0, 0, 138)),
        owning_building_guid = 1,
        door_guid = 717
      )
      LocalObject(
        908,
        IFFLock.Constructor(Vector3(4372.619f, 3771.444f, 205.4439f), Vector3(0, 0, 138)),
        owning_building_guid = 1,
        door_guid = 647
      )
      LocalObject(
        909,
        IFFLock.Constructor(Vector3(4378.333f, 3720.011f, 230.5689f), Vector3(0, 0, 318)),
        owning_building_guid = 1,
        door_guid = 436
      )
      LocalObject(
        910,
        IFFLock.Constructor(Vector3(4380.39f, 3712.193f, 225.5699f), Vector3(0, 0, 228)),
        owning_building_guid = 1,
        door_guid = 435
      )
      LocalObject(
        911,
        IFFLock.Constructor(Vector3(4386.53f, 3734.501f, 212.9439f), Vector3(0, 0, 228)),
        owning_building_guid = 1,
        door_guid = 654
      )
      LocalObject(
        912,
        IFFLock.Constructor(Vector3(4388.523f, 3714.806f, 230.5689f), Vector3(0, 0, 138)),
        owning_building_guid = 1,
        door_guid = 437
      )
      LocalObject(
        913,
        IFFLock.Constructor(Vector3(4395.285f, 3729.617f, 205.4439f), Vector3(0, 0, 318)),
        owning_building_guid = 1,
        door_guid = 657
      )
      LocalObject(
        914,
        IFFLock.Constructor(Vector3(4403.563f, 3754.069f, 212.9439f), Vector3(0, 0, 48)),
        owning_building_guid = 1,
        door_guid = 658
      )
      LocalObject(
        915,
        IFFLock.Constructor(Vector3(4413.844f, 3850.081f, 220.5679f), Vector3(0, 0, 48)),
        owning_building_guid = 1,
        door_guid = 443
      )
      LocalObject(
        916,
        IFFLock.Constructor(Vector3(4423.768f, 3760.92f, 230.5689f), Vector3(0, 0, 318)),
        owning_building_guid = 1,
        door_guid = 444
      )
      LocalObject(
        917,
        IFFLock.Constructor(Vector3(4431.624f, 3763.823f, 225.5699f), Vector3(0, 0, 48)),
        owning_building_guid = 1,
        door_guid = 446
      )
      LocalObject(
        918,
        IFFLock.Constructor(Vector3(4433.959f, 3755.717f, 230.5689f), Vector3(0, 0, 138)),
        owning_building_guid = 1,
        door_guid = 445
      )
      LocalObject(1229, Locker.Constructor(Vector3(4390.055f, 3734.996f, 211.8689f)), owning_building_guid = 1)
      LocalObject(1230, Locker.Constructor(Vector3(4390.834f, 3734.131f, 211.8689f)), owning_building_guid = 1)
      LocalObject(1231, Locker.Constructor(Vector3(4391.602f, 3733.279f, 211.8689f)), owning_building_guid = 1)
      LocalObject(1232, Locker.Constructor(Vector3(4392.371f, 3732.425f, 211.8689f)), owning_building_guid = 1)
      LocalObject(1233, Locker.Constructor(Vector3(4412.05f, 3740.495f, 204.1079f)), owning_building_guid = 1)
      LocalObject(1234, Locker.Constructor(Vector3(4412.936f, 3739.511f, 204.1079f)), owning_building_guid = 1)
      LocalObject(1235, Locker.Constructor(Vector3(4413.83f, 3738.518f, 204.1079f)), owning_building_guid = 1)
      LocalObject(1236, Locker.Constructor(Vector3(4414.724f, 3737.524f, 204.1079f)), owning_building_guid = 1)
      LocalObject(1237, Locker.Constructor(Vector3(4417.762f, 3734.15f, 204.1079f)), owning_building_guid = 1)
      LocalObject(1238, Locker.Constructor(Vector3(4418.648f, 3733.167f, 204.1079f)), owning_building_guid = 1)
      LocalObject(1239, Locker.Constructor(Vector3(4419.542f, 3732.174f, 204.1079f)), owning_building_guid = 1)
      LocalObject(1240, Locker.Constructor(Vector3(4420.437f, 3731.18f, 204.1079f)), owning_building_guid = 1)
      LocalObject(
        1594,
        Terminal.Constructor(Vector3(4389.295f, 3722.961f, 220.4369f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1595,
        Terminal.Constructor(Vector3(4390.964f, 3754.694f, 220.4369f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1596,
        Terminal.Constructor(Vector3(4396.038f, 3736.224f, 213.1979f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1597,
        Terminal.Constructor(Vector3(4398.811f, 3738.72f, 213.1979f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1598,
        Terminal.Constructor(Vector3(4401.626f, 3741.256f, 213.1979f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1599,
        Terminal.Constructor(Vector3(4422.702f, 3753.042f, 220.4369f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2255,
        Terminal.Constructor(Vector3(4383.787f, 3743.605f, 213.7419f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2256,
        Terminal.Constructor(Vector3(4389.203f, 3748.488f, 213.7419f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2257,
        Terminal.Constructor(Vector3(4394.621f, 3753.362f, 213.7419f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2258,
        Terminal.Constructor(Vector3(4398.973f, 3739.079f, 225.6359f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2259,
        Terminal.Constructor(Vector3(4401.431f, 3766.103f, 205.6649f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2260,
        Terminal.Constructor(Vector3(4403.207f, 3799.998f, 213.1649f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2420,
        Terminal.Constructor(Vector3(4435.221f, 3828.801f, 221.0099f), vehicle_terminal_combined),
        owning_building_guid = 1
      )
      LocalObject(
        1480,
        VehicleSpawnPad.Constructor(Vector3(4425.146f, 3819.608f, 216.8529f), mb_pad_creation, Vector3(0, 0, 228)),
        owning_building_guid = 1,
        terminal_guid = 2420
      )
      LocalObject(2095, ResourceSilo.Constructor(Vector3(4377.691f, 3641.67f, 226.1429f)), owning_building_guid = 1)
      LocalObject(
        2158,
        SpawnTube.Constructor(Vector3(4384.363f, 3745.117f, 211.6079f), Vector3(0, 0, 48)),
        owning_building_guid = 1
      )
      LocalObject(
        2159,
        SpawnTube.Constructor(Vector3(4389.781f, 3749.996f, 211.6079f), Vector3(0, 0, 48)),
        owning_building_guid = 1
      )
      LocalObject(
        2160,
        SpawnTube.Constructor(Vector3(4395.197f, 3754.873f, 211.6079f), Vector3(0, 0, 48)),
        owning_building_guid = 1
      )
      LocalObject(
        1495,
        ProximityTerminal.Constructor(Vector3(4391.243f, 3754.381f, 224.1079f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1496,
        ProximityTerminal.Constructor(Vector3(4415.827f, 3735.485f, 204.1079f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1768,
        ProximityTerminal.Constructor(Vector3(4356.935f, 3682.656f, 227.4359f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1769,
        Terminal.Constructor(Vector3(4356.935f, 3682.656f, 227.4359f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1771,
        ProximityTerminal.Constructor(Vector3(4382.386f, 3793.773f, 227.4129f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1772,
        Terminal.Constructor(Vector3(4382.386f, 3793.773f, 227.4129f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1774,
        ProximityTerminal.Constructor(Vector3(4424.299f, 3688.036f, 227.4129f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1775,
        Terminal.Constructor(Vector3(4424.299f, 3688.036f, 227.4129f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1777,
        ProximityTerminal.Constructor(Vector3(4466.462f, 3756.651f, 229.5739f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1778,
        Terminal.Constructor(Vector3(4466.462f, 3756.651f, 229.5739f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2065,
        ProximityTerminal.Constructor(Vector3(4359.248f, 3790.228f, 218.3079f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2066,
        Terminal.Constructor(Vector3(4359.248f, 3790.228f, 218.3079f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2069,
        ProximityTerminal.Constructor(Vector3(4447.996f, 3691.947f, 218.3079f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2070,
        Terminal.Constructor(Vector3(4447.996f, 3691.947f, 218.3079f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1437,
        FacilityTurret.Constructor(Vector3(4280.107f, 3733.096f, 227.6159f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1437, 5000)
      LocalObject(
        1438,
        FacilityTurret.Constructor(Vector3(4318.323f, 3687.324f, 227.6159f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1438, 5001)
      LocalObject(
        1443,
        FacilityTurret.Constructor(Vector3(4382.764f, 3619.069f, 227.6159f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1443, 5002)
      LocalObject(
        1444,
        FacilityTurret.Constructor(Vector3(4429.962f, 3868.066f, 227.6159f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1444, 5003)
      LocalObject(
        1445,
        FacilityTurret.Constructor(Vector3(4487.027f, 3709.951f, 227.6159f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1445, 5004)
      LocalObject(
        1446,
        FacilityTurret.Constructor(Vector3(4532.646f, 3754.011f, 227.6159f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1446, 5005)
      LocalObject(
        1882,
        Painbox.Constructor(Vector3(4360.838f, 3777.871f, 208.9789f), painbox),
        owning_building_guid = 1
      )
      LocalObject(
        1891,
        Painbox.Constructor(Vector3(4396.655f, 3746.266f, 216.5563f), painbox_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1900,
        Painbox.Constructor(Vector3(4371.816f, 3768.857f, 205.5669f), painbox_door_radius),
        owning_building_guid = 1
      )
      LocalObject(
        1921,
        Painbox.Constructor(Vector3(4385.29f, 3735.5f, 212.8079f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1922,
        Painbox.Constructor(Vector3(4404.919f, 3753.075f, 212.8724f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1923,
        Painbox.Constructor(Vector3(4409.922f, 3739.557f, 214.7865f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(243, Generator.Constructor(Vector3(4360.482f, 3782.537f, 202.8139f)), owning_building_guid = 1)
      LocalObject(
        234,
        Terminal.Constructor(Vector3(4365.999f, 3776.481f, 204.1079f), gen_control),
        owning_building_guid = 1
      )
    }

    Building43()

    def Building43(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 4, MapID: 43
      LocalBuilding(
        "bunker_gauntlet",
        4,
        43,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(3072f, 2262f, 237.2147f),
            Vector3(0f, 0f, 315f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(287, Door.Constructor(Vector3(3053.042f, 2278.254f, 238.7357f)), owning_building_guid = 4)
      LocalObject(289, Door.Constructor(Vector3(3088.279f, 2243.032f, 238.7357f)), owning_building_guid = 4)
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
            Vector3(4084f, 5194f, 232.2395f),
            Vector3(0f, 0f, 270f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(405, Door.Constructor(Vector3(4082.099f, 5169.077f, 233.7605f)), owning_building_guid = 5)
      LocalObject(406, Door.Constructor(Vector3(4082.088f, 5218.898f, 233.7605f)), owning_building_guid = 5)
    }

    Building42()

    def Building42(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 6, MapID: 42
      LocalBuilding(
        "bunker_gauntlet",
        6,
        42,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4086f, 4288f, 266.4175f),
            Vector3(0f, 0f, 270f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(407, Door.Constructor(Vector3(4084.099f, 4263.077f, 267.9385f)), owning_building_guid = 6)
      LocalObject(408, Door.Constructor(Vector3(4084.088f, 4312.898f, 267.9385f)), owning_building_guid = 6)
    }

    Building41()

    def Building41(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 7, MapID: 41
      LocalBuilding(
        "bunker_gauntlet",
        7,
        41,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(5866f, 3550f, 96.40965f),
            Vector3(0f, 0f, 360f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(496, Door.Constructor(Vector3(5841.102f, 3548.088f, 97.93066f)), owning_building_guid = 7)
      LocalObject(501, Door.Constructor(Vector3(5890.923f, 3548.099f, 97.93066f)), owning_building_guid = 7)
    }

    Building33()

    def Building33(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 8, MapID: 33
      LocalBuilding(
        "bunker_lg",
        8,
        33,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(2958f, 2048f, 238.7697f), Vector3(0f, 0f, 45f), bunker_lg)
        )
      )
      LocalObject(281, Door.Constructor(Vector3(2958.035f, 2051.651f, 240.2907f)), owning_building_guid = 8)
    }

    Building40()

    def Building40(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 9, MapID: 40
      LocalBuilding(
        "bunker_lg",
        9,
        40,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3566f, 5902f, 219.7465f), Vector3(0f, 0f, 180f), bunker_lg)
        )
      )
      LocalObject(326, Door.Constructor(Vector3(3563.394f, 5899.443f, 221.2675f)), owning_building_guid = 9)
    }

    Building35()

    def Building35(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 10, MapID: 35
      LocalBuilding(
        "bunker_lg",
        10,
        35,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3608f, 3584f, 222.4627f), Vector3(0f, 0f, 0f), bunker_lg)
        )
      )
      LocalObject(333, Door.Constructor(Vector3(3610.606f, 3586.557f, 223.9837f)), owning_building_guid = 10)
    }

    Building38()

    def Building38(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 11, MapID: 38
      LocalBuilding(
        "bunker_lg",
        11,
        38,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3800f, 4326f, 268.4381f), Vector3(0f, 0f, 225f), bunker_lg)
        )
      )
      LocalObject(362, Door.Constructor(Vector3(3799.965f, 4322.349f, 269.9591f)), owning_building_guid = 11)
    }

    Building36()

    def Building36(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 12, MapID: 36
      LocalBuilding(
        "bunker_lg",
        12,
        36,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5122f, 3188f, 217.751f), Vector3(0f, 0f, 90f), bunker_lg)
        )
      )
      LocalObject(470, Door.Constructor(Vector3(5119.443f, 3190.606f, 219.272f)), owning_building_guid = 12)
    }

    Building45()

    def Building45(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 13, MapID: 45
      LocalBuilding(
        "bunker_sm",
        13,
        45,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3198f, 5836f, 234.3076f), Vector3(0f, 0f, 360f), bunker_sm)
        )
      )
      LocalObject(290, Door.Constructor(Vector3(3199.225f, 5835.945f, 235.8286f)), owning_building_guid = 13)
    }

    Building39()

    def Building39(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 14, MapID: 39
      LocalBuilding(
        "bunker_sm",
        14,
        39,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3858f, 5178f, 233.2084f), Vector3(0f, 0f, 45f), bunker_sm)
        )
      )
      LocalObject(363, Door.Constructor(Vector3(3858.905f, 5178.827f, 234.7294f)), owning_building_guid = 14)
    }

    Building34()

    def Building34(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 15, MapID: 34
      LocalBuilding(
        "bunker_sm",
        15,
        34,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3922f, 1994f, 260.3741f), Vector3(0f, 0f, 225f), bunker_sm)
        )
      )
      LocalObject(374, Door.Constructor(Vector3(3921.095f, 1993.173f, 261.8951f)), owning_building_guid = 15)
    }

    Building37()

    def Building37(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 16, MapID: 37
      LocalBuilding(
        "bunker_sm",
        16,
        37,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4394f, 3876f, 218.9932f), Vector3(0f, 0f, 224f), bunker_sm)
        )
      )
      LocalObject(438, Door.Constructor(Vector3(4393.081f, 3875.189f, 220.5142f)), owning_building_guid = 16)
    }

    Building4()

    def Building4(): Unit = { // Name: Keelut Type: comm_station GUID: 17, MapID: 4
      LocalBuilding(
        "Keelut",
        17,
        4,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3700f, 1956f, 265.1138f),
            Vector3(0f, 0f, 269f),
            comm_station
          )
        )
      )
      LocalObject(
        178,
        CaptureTerminal.Constructor(Vector3(3775.953f, 1910.578f, 247.8138f), capture_terminal),
        owning_building_guid = 17
      )
      LocalObject(336, Door.Constructor(Vector3(3632.635f, 1981.68f, 266.8648f)), owning_building_guid = 17)
      LocalObject(337, Door.Constructor(Vector3(3632.952f, 1999.87f, 274.8288f)), owning_building_guid = 17)
      LocalObject(344, Door.Constructor(Vector3(3657.55f, 2016.554f, 266.8648f)), owning_building_guid = 17)
      LocalObject(346, Door.Constructor(Vector3(3675.74f, 2016.237f, 274.8288f)), owning_building_guid = 17)
      LocalObject(347, Door.Constructor(Vector3(3688.258f, 1895.995f, 274.8288f)), owning_building_guid = 17)
      LocalObject(348, Door.Constructor(Vector3(3691.581f, 1976.15f, 271.8348f)), owning_building_guid = 17)
      LocalObject(349, Door.Constructor(Vector3(3692.667f, 1960.504f, 279.2738f)), owning_building_guid = 17)
      LocalObject(351, Door.Constructor(Vector3(3700.644f, 1975.992f, 271.8348f)), owning_building_guid = 17)
      LocalObject(352, Door.Constructor(Vector3(3706.448f, 1895.677f, 266.8648f)), owning_building_guid = 17)
      LocalObject(354, Door.Constructor(Vector3(3719.857f, 1947.652f, 271.8348f)), owning_building_guid = 17)
      LocalObject(359, Door.Constructor(Vector3(3767.64f, 1936.125f, 274.8278f)), owning_building_guid = 17)
      LocalObject(360, Door.Constructor(Vector3(3767.958f, 1954.314f, 266.8648f)), owning_building_guid = 17)
      LocalObject(361, Door.Constructor(Vector3(3775.43f, 1922.678f, 266.8348f)), owning_building_guid = 17)
      LocalObject(579, Door.Constructor(Vector3(3696.28f, 1972.067f, 271.8348f)), owning_building_guid = 17)
      LocalObject(580, Door.Constructor(Vector3(3704f, 1955.93f, 259.3348f)), owning_building_guid = 17)
      LocalObject(581, Door.Constructor(Vector3(3707.231f, 1911.867f, 256.8348f)), owning_building_guid = 17)
      LocalObject(582, Door.Constructor(Vector3(3707.65f, 1935.863f, 261.8348f)), owning_building_guid = 17)
      LocalObject(583, Door.Constructor(Vector3(3707.789f, 1943.862f, 266.8348f)), owning_building_guid = 17)
      LocalObject(584, Door.Constructor(Vector3(3708.069f, 1959.86f, 249.3348f)), owning_building_guid = 17)
      LocalObject(585, Door.Constructor(Vector3(3711.021f, 1899.799f, 249.3348f)), owning_building_guid = 17)
      LocalObject(586, Door.Constructor(Vector3(3711.859f, 1947.792f, 271.8348f)), owning_building_guid = 17)
      LocalObject(587, Door.Constructor(Vector3(3712.277f, 1971.788f, 266.8348f)), owning_building_guid = 17)
      LocalObject(588, Door.Constructor(Vector3(3716.347f, 1975.718f, 259.3348f)), owning_building_guid = 17)
      LocalObject(589, Door.Constructor(Vector3(3719.857f, 1947.652f, 261.8348f)), owning_building_guid = 17)
      LocalObject(590, Door.Constructor(Vector3(3723.508f, 1927.585f, 249.3348f)), owning_building_guid = 17)
      LocalObject(591, Door.Constructor(Vector3(3724.345f, 1975.578f, 249.3348f)), owning_building_guid = 17)
      LocalObject(592, Door.Constructor(Vector3(3727.018f, 1899.52f, 256.8348f)), owning_building_guid = 17)
      LocalObject(593, Door.Constructor(Vector3(3731.227f, 1911.448f, 256.8348f)), owning_building_guid = 17)
      LocalObject(594, Door.Constructor(Vector3(3732.624f, 1991.436f, 259.3348f)), owning_building_guid = 17)
      LocalObject(595, Door.Constructor(Vector3(3735.995f, 1955.372f, 259.3348f)), owning_building_guid = 17)
      LocalObject(596, Door.Constructor(Vector3(3740.064f, 1959.301f, 249.3348f)), owning_building_guid = 17)
      LocalObject(597, Door.Constructor(Vector3(3748.062f, 1959.162f, 259.3348f)), owning_building_guid = 17)
      LocalObject(598, Door.Constructor(Vector3(3748.481f, 1983.158f, 259.3348f)), owning_building_guid = 17)
      LocalObject(599, Door.Constructor(Vector3(3751.015f, 1899.101f, 249.3348f)), owning_building_guid = 17)
      LocalObject(600, Door.Constructor(Vector3(3755.503f, 1927.027f, 249.3348f)), owning_building_guid = 17)
      LocalObject(601, Door.Constructor(Vector3(3767.152f, 1906.821f, 249.3348f)), owning_building_guid = 17)
      LocalObject(602, Door.Constructor(Vector3(3767.292f, 1914.819f, 249.3348f)), owning_building_guid = 17)
      LocalObject(714, Door.Constructor(Vector3(3695.624f, 1938.367f, 267.6068f)), owning_building_guid = 17)
      LocalObject(2310, Door.Constructor(Vector3(3713.091f, 1919.093f, 257.1678f)), owning_building_guid = 17)
      LocalObject(2311, Door.Constructor(Vector3(3720.383f, 1918.966f, 257.1678f)), owning_building_guid = 17)
      LocalObject(2312, Door.Constructor(Vector3(3727.671f, 1918.838f, 257.1678f)), owning_building_guid = 17)
      LocalObject(
        750,
        IFFLock.Constructor(Vector3(3698.773f, 1935.565f, 266.7658f), Vector3(0, 0, 181)),
        owning_building_guid = 17,
        door_guid = 714
      )
      LocalObject(
        839,
        IFFLock.Constructor(Vector3(3690.603f, 1961.348f, 279.1948f), Vector3(0, 0, 1)),
        owning_building_guid = 17,
        door_guid = 349
      )
      LocalObject(
        840,
        IFFLock.Constructor(Vector3(3690.735f, 1974.117f, 271.7748f), Vector3(0, 0, 271)),
        owning_building_guid = 17,
        door_guid = 348
      )
      LocalObject(
        841,
        IFFLock.Constructor(Vector3(3701.49f, 1978.02f, 271.7748f), Vector3(0, 0, 91)),
        owning_building_guid = 17,
        door_guid = 351
      )
      LocalObject(
        842,
        IFFLock.Constructor(Vector3(3706.394f, 1910.309f, 256.6498f), Vector3(0, 0, 271)),
        owning_building_guid = 17,
        door_guid = 581
      )
      LocalObject(
        843,
        IFFLock.Constructor(Vector3(3709.466f, 1900.766f, 249.1498f), Vector3(0, 0, 1)),
        owning_building_guid = 17,
        door_guid = 585
      )
      LocalObject(
        844,
        IFFLock.Constructor(Vector3(3717.827f, 1948.501f, 271.7748f), Vector3(0, 0, 1)),
        owning_building_guid = 17,
        door_guid = 354
      )
      LocalObject(
        845,
        IFFLock.Constructor(Vector3(3723.378f, 1974.023f, 249.1498f), Vector3(0, 0, 271)),
        owning_building_guid = 17,
        door_guid = 591
      )
      LocalObject(
        846,
        IFFLock.Constructor(Vector3(3732.064f, 1913.006f, 256.6498f), Vector3(0, 0, 91)),
        owning_building_guid = 17,
        door_guid = 593
      )
      LocalObject(
        851,
        IFFLock.Constructor(Vector3(3765.734f, 1915.66f, 249.1498f), Vector3(0, 0, 1)),
        owning_building_guid = 17,
        door_guid = 602
      )
      LocalObject(
        852,
        IFFLock.Constructor(Vector3(3768.709f, 1905.98f, 249.1498f), Vector3(0, 0, 181)),
        owning_building_guid = 17,
        door_guid = 601
      )
      LocalObject(
        853,
        IFFLock.Constructor(Vector3(3777.449f, 1921.865f, 266.7638f), Vector3(0, 0, 181)),
        owning_building_guid = 17,
        door_guid = 361
      )
      LocalObject(1120, Locker.Constructor(Vector3(3709.249f, 1904.808f, 255.5748f)), owning_building_guid = 17)
      LocalObject(1121, Locker.Constructor(Vector3(3709.269f, 1905.957f, 255.5748f)), owning_building_guid = 17)
      LocalObject(1122, Locker.Constructor(Vector3(3709.289f, 1907.103f, 255.5748f)), owning_building_guid = 17)
      LocalObject(1123, Locker.Constructor(Vector3(3709.309f, 1908.267f, 255.5748f)), owning_building_guid = 17)
      LocalObject(1124, Locker.Constructor(Vector3(3728.926f, 1884.756f, 247.8138f)), owning_building_guid = 17)
      LocalObject(1125, Locker.Constructor(Vector3(3728.949f, 1886.093f, 247.8138f)), owning_building_guid = 17)
      LocalObject(1126, Locker.Constructor(Vector3(3728.973f, 1887.429f, 247.8138f)), owning_building_guid = 17)
      LocalObject(1127, Locker.Constructor(Vector3(3728.996f, 1888.753f, 247.8138f)), owning_building_guid = 17)
      LocalObject(1128, Locker.Constructor(Vector3(3729.075f, 1893.292f, 247.8138f)), owning_building_guid = 17)
      LocalObject(1129, Locker.Constructor(Vector3(3729.098f, 1894.629f, 247.8138f)), owning_building_guid = 17)
      LocalObject(1130, Locker.Constructor(Vector3(3729.122f, 1895.965f, 247.8138f)), owning_building_guid = 17)
      LocalObject(1131, Locker.Constructor(Vector3(3729.145f, 1897.288f, 247.8138f)), owning_building_guid = 17)
      LocalObject(
        1551,
        Terminal.Constructor(Vector3(3677.078f, 1966.023f, 271.6738f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1552,
        Terminal.Constructor(Vector3(3690.391f, 1953.836f, 279.0688f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1553,
        Terminal.Constructor(Vector3(3692.547f, 1956.055f, 279.0688f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1554,
        Terminal.Constructor(Vector3(3694.785f, 1953.759f, 279.0688f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1555,
        Terminal.Constructor(Vector3(3714.522f, 1905.085f, 256.9038f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1556,
        Terminal.Constructor(Vector3(3718.252f, 1905.02f, 256.9038f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1557,
        Terminal.Constructor(Vector3(3722.041f, 1904.954f, 256.9038f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2233,
        Terminal.Constructor(Vector3(3675.999f, 1957.909f, 271.9308f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2234,
        Terminal.Constructor(Vector3(3708.87f, 1971.905f, 249.3708f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2235,
        Terminal.Constructor(Vector3(3710.596f, 1918.838f, 257.4478f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2236,
        Terminal.Constructor(Vector3(3717.887f, 1918.715f, 257.4478f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2237,
        Terminal.Constructor(Vector3(3725.174f, 1918.585f, 257.4478f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2238,
        Terminal.Constructor(Vector3(3739.946f, 1955.894f, 259.3708f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2417,
        Terminal.Constructor(Vector3(3753.85f, 1902.594f, 268.0008f), vehicle_terminal_combined),
        owning_building_guid = 17
      )
      LocalObject(
        1477,
        VehicleSpawnPad.Constructor(Vector3(3753.934f, 1916.22f, 263.8428f), mb_pad_creation, Vector3(0, 0, 1)),
        owning_building_guid = 17,
        terminal_guid = 2417
      )
      LocalObject(2092, ResourceSilo.Constructor(Vector3(3631.892f, 2012.967f, 272.3308f)), owning_building_guid = 17)
      LocalObject(
        2133,
        SpawnTube.Constructor(Vector3(3712.049f, 1919.551f, 255.3138f), Vector3(0, 0, 91)),
        owning_building_guid = 17
      )
      LocalObject(
        2134,
        SpawnTube.Constructor(Vector3(3719.339f, 1919.424f, 255.3138f), Vector3(0, 0, 91)),
        owning_building_guid = 17
      )
      LocalObject(
        2135,
        SpawnTube.Constructor(Vector3(3726.625f, 1919.297f, 255.3138f), Vector3(0, 0, 91)),
        owning_building_guid = 17
      )
      LocalObject(
        1490,
        ProximityTerminal.Constructor(Vector3(3671.012f, 1955.642f, 265.3138f), medical_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1491,
        ProximityTerminal.Constructor(Vector3(3728.491f, 1891.049f, 247.8138f), medical_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1741,
        ProximityTerminal.Constructor(Vector3(3755.506f, 1973.88f, 273.5548f), pad_landing_frame),
        owning_building_guid = 17
      )
      LocalObject(
        1742,
        Terminal.Constructor(Vector3(3755.506f, 1973.88f, 273.5548f), air_rearm_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2041,
        ProximityTerminal.Constructor(Vector3(3674.598f, 1894.793f, 264.8638f), repair_silo),
        owning_building_guid = 17
      )
      LocalObject(
        2042,
        Terminal.Constructor(Vector3(3674.598f, 1894.793f, 264.8638f), ground_rearm_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2045,
        ProximityTerminal.Constructor(Vector3(3724.242f, 2016.674f, 264.8638f), repair_silo),
        owning_building_guid = 17
      )
      LocalObject(
        2046,
        Terminal.Constructor(Vector3(3724.242f, 2016.674f, 264.8638f), ground_rearm_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1405,
        FacilityTurret.Constructor(Vector3(3617.862f, 1926.564f, 273.8218f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1405, 5006)
      LocalObject(
        1406,
        FacilityTurret.Constructor(Vector3(3620.842f, 2029.839f, 273.8218f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1406, 5007)
      LocalObject(
        1410,
        FacilityTurret.Constructor(Vector3(3660.215f, 1882.673f, 273.8218f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1410, 5008)
      LocalObject(
        1413,
        FacilityTurret.Constructor(Vector3(3739.93f, 2028.89f, 273.8218f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1413, 5009)
      LocalObject(
        1416,
        FacilityTurret.Constructor(Vector3(3779.152f, 1881.766f, 273.8218f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1416, 5010)
      LocalObject(
        1417,
        FacilityTurret.Constructor(Vector3(3782.269f, 1985.02f, 273.8218f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1417, 5011)
      LocalObject(
        1879,
        Painbox.Constructor(Vector3(3736.431f, 1975.229f, 252.7158f), painbox),
        owning_building_guid = 17
      )
      LocalObject(
        1888,
        Painbox.Constructor(Vector3(3725.359f, 1909.335f, 259.7586f), painbox_continuous),
        owning_building_guid = 17
      )
      LocalObject(
        1897,
        Painbox.Constructor(Vector3(3721.415f, 1975.392f, 250.5725f), painbox_door_radius),
        owning_building_guid = 17
      )
      LocalObject(
        1912,
        Painbox.Constructor(Vector3(3705.308f, 1911.986f, 257.1398f), painbox_door_radius_continuous),
        owning_building_guid = 17
      )
      LocalObject(
        1913,
        Painbox.Constructor(Vector3(3726.813f, 1897.668f, 258.1398f), painbox_door_radius_continuous),
        owning_building_guid = 17
      )
      LocalObject(
        1914,
        Painbox.Constructor(Vector3(3732.633f, 1910.613f, 256.9215f), painbox_door_radius_continuous),
        owning_building_guid = 17
      )
      LocalObject(240, Generator.Constructor(Vector3(3739.898f, 1975.332f, 246.5198f)), owning_building_guid = 17)
      LocalObject(
        231,
        Terminal.Constructor(Vector3(3731.707f, 1975.428f, 247.8138f), gen_control),
        owning_building_guid = 17
      )
    }

    Building2()

    def Building2(): Unit = { // Name: Anguta Type: comm_station_dsp GUID: 20, MapID: 2
      LocalBuilding(
        "Anguta",
        20,
        2,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3944f, 4240f, 266.4438f),
            Vector3(0f, 0f, 360f),
            comm_station_dsp
          )
        )
      )
      LocalObject(
        180,
        CaptureTerminal.Constructor(Vector3(4020.089f, 4220.734f, 249.0439f), capture_terminal),
        owning_building_guid = 20
      )
      LocalObject(222, Door.Constructor(Vector3(4012.339f, 4310.464f, 269.8218f)), owning_building_guid = 20)
      LocalObject(370, Door.Constructor(Vector3(3884.196f, 4196.501f, 268.0948f)), owning_building_guid = 20)
      LocalObject(371, Door.Constructor(Vector3(3884.196f, 4214.693f, 276.0588f)), owning_building_guid = 20)
      LocalObject(372, Door.Constructor(Vector3(3901.307f, 4172.197f, 276.0588f)), owning_building_guid = 20)
      LocalObject(373, Door.Constructor(Vector3(3919.499f, 4172.197f, 268.0948f)), owning_building_guid = 20)
      LocalObject(375, Door.Constructor(Vector3(3924f, 4231.231f, 273.0648f)), owning_building_guid = 20)
      LocalObject(376, Door.Constructor(Vector3(3924f, 4240.295f, 273.0648f)), owning_building_guid = 20)
      LocalObject(383, Door.Constructor(Vector3(3936.763f, 4359.958f, 268.0948f)), owning_building_guid = 20)
      LocalObject(384, Door.Constructor(Vector3(3939.625f, 4232.59f, 280.5038f)), owning_building_guid = 20)
      LocalObject(385, Door.Constructor(Vector3(3949.627f, 4372.823f, 276.0579f)), owning_building_guid = 20)
      LocalObject(387, Door.Constructor(Vector3(3952f, 4260f, 273.0648f)), owning_building_guid = 20)
      LocalObject(391, Door.Constructor(Vector3(3991.721f, 4404.353f, 268.0948f)), owning_building_guid = 20)
      LocalObject(393, Door.Constructor(Vector3(3997.952f, 4344.355f, 273.0609f)), owning_building_guid = 20)
      LocalObject(394, Door.Constructor(Vector3(3999.927f, 4314.35f, 268.0668f)), owning_building_guid = 20)
      LocalObject(396, Door.Constructor(Vector3(4009.914f, 4404.353f, 276.0579f)), owning_building_guid = 20)
      LocalObject(398, Door.Constructor(Vector3(4023.929f, 4227.406f, 276.0588f)), owning_building_guid = 20)
      LocalObject(399, Door.Constructor(Vector3(4036.793f, 4240.27f, 268.0948f)), owning_building_guid = 20)
      LocalObject(402, Door.Constructor(Vector3(4050.977f, 4303.008f, 276.0579f)), owning_building_guid = 20)
      LocalObject(403, Door.Constructor(Vector3(4050.977f, 4321.2f, 268.0948f)), owning_building_guid = 20)
      LocalObject(404, Door.Constructor(Vector3(4060f, 4320f, 268.0648f)), owning_building_guid = 20)
      LocalObject(603, Door.Constructor(Vector3(3928f, 4236f, 273.0648f)), owning_building_guid = 20)
      LocalObject(604, Door.Constructor(Vector3(3928f, 4252f, 268.0648f)), owning_building_guid = 20)
      LocalObject(605, Door.Constructor(Vector3(3952f, 4252f, 273.0648f)), owning_building_guid = 20)
      LocalObject(606, Door.Constructor(Vector3(3952f, 4260f, 263.0648f)), owning_building_guid = 20)
      LocalObject(607, Door.Constructor(Vector3(3956f, 4248f, 268.0648f)), owning_building_guid = 20)
      LocalObject(610, Door.Constructor(Vector3(3964f, 4248f, 263.0648f)), owning_building_guid = 20)
      LocalObject(611, Door.Constructor(Vector3(3968f, 4284f, 258.0648f)), owning_building_guid = 20)
      LocalObject(614, Door.Constructor(Vector3(3972f, 4272f, 250.5648f)), owning_building_guid = 20)
      LocalObject(619, Door.Constructor(Vector3(3984f, 4236f, 250.5648f)), owning_building_guid = 20)
      LocalObject(620, Door.Constructor(Vector3(3984f, 4300f, 250.5648f)), owning_building_guid = 20)
      LocalObject(621, Door.Constructor(Vector3(3988f, 4232f, 258.0648f)), owning_building_guid = 20)
      LocalObject(622, Door.Constructor(Vector3(3988f, 4248f, 258.0648f)), owning_building_guid = 20)
      LocalObject(623, Door.Constructor(Vector3(3988f, 4272f, 258.0648f)), owning_building_guid = 20)
      LocalObject(630, Door.Constructor(Vector3(4000f, 4252f, 250.5648f)), owning_building_guid = 20)
      LocalObject(631, Door.Constructor(Vector3(4000f, 4268f, 258.0648f)), owning_building_guid = 20)
      LocalObject(634, Door.Constructor(Vector3(4003.921f, 4334.351f, 273.0668f)), owning_building_guid = 20)
      LocalObject(638, Door.Constructor(Vector3(4016f, 4212f, 250.5648f)), owning_building_guid = 20)
      LocalObject(642, Door.Constructor(Vector3(4024f, 4212f, 250.5648f)), owning_building_guid = 20)
      LocalObject(643, Door.Constructor(Vector3(4028f, 4224f, 250.5648f)), owning_building_guid = 20)
      LocalObject(645, Door.Constructor(Vector3(4032f, 4244f, 258.0648f)), owning_building_guid = 20)
      LocalObject(646, Door.Constructor(Vector3(4032f, 4276f, 258.0648f)), owning_building_guid = 20)
      LocalObject(715, Door.Constructor(Vector3(3961.707f, 4235.922f, 268.8358f)), owning_building_guid = 20)
      LocalObject(2322, Door.Constructor(Vector3(3980.673f, 4253.733f, 258.3979f)), owning_building_guid = 20)
      LocalObject(2323, Door.Constructor(Vector3(3980.673f, 4261.026f, 258.3979f)), owning_building_guid = 20)
      LocalObject(2324, Door.Constructor(Vector3(3980.673f, 4268.315f, 258.3979f)), owning_building_guid = 20)
      LocalObject(
        751,
        IFFLock.Constructor(Vector3(3964.454f, 4239.09f, 268.0118f), Vector3(0, 0, 90)),
        owning_building_guid = 20,
        door_guid = 715
      )
      LocalObject(
        860,
        IFFLock.Constructor(Vector3(3921.959f, 4241.104f, 273.0118f), Vector3(0, 0, 0)),
        owning_building_guid = 20,
        door_guid = 376
      )
      LocalObject(
        863,
        IFFLock.Constructor(Vector3(3926.04f, 4230.42f, 273.0118f), Vector3(0, 0, 180)),
        owning_building_guid = 20,
        door_guid = 375
      )
      LocalObject(
        866,
        IFFLock.Constructor(Vector3(3938.817f, 4230.514f, 280.5118f), Vector3(0, 0, 270)),
        owning_building_guid = 20,
        door_guid = 384
      )
      LocalObject(
        868,
        IFFLock.Constructor(Vector3(3951.193f, 4257.962f, 273.0118f), Vector3(0, 0, 270)),
        owning_building_guid = 20,
        door_guid = 387
      )
      LocalObject(
        873,
        IFFLock.Constructor(Vector3(3984.94f, 4301.572f, 250.3799f), Vector3(0, 0, 90)),
        owning_building_guid = 20,
        door_guid = 620
      )
      LocalObject(
        874,
        IFFLock.Constructor(Vector3(3986.428f, 4272.94f, 257.8799f), Vector3(0, 0, 0)),
        owning_building_guid = 20,
        door_guid = 623
      )
      LocalObject(
        875,
        IFFLock.Constructor(Vector3(3989.572f, 4247.19f, 257.8799f), Vector3(0, 0, 180)),
        owning_building_guid = 20,
        door_guid = 622
      )
      LocalObject(
        876,
        IFFLock.Constructor(Vector3(3995.907f, 4345.163f, 272.9908f), Vector3(0, 0, 0)),
        owning_building_guid = 20,
        door_guid = 393
      )
      LocalObject(
        878,
        IFFLock.Constructor(Vector3(3999.06f, 4250.428f, 250.3799f), Vector3(0, 0, 270)),
        owning_building_guid = 20,
        door_guid = 630
      )
      LocalObject(
        879,
        IFFLock.Constructor(Vector3(3999.124f, 4312.312f, 268.0558f), Vector3(0, 0, 270)),
        owning_building_guid = 20,
        door_guid = 394
      )
      LocalObject(
        882,
        IFFLock.Constructor(Vector3(4015.06f, 4210.428f, 250.3799f), Vector3(0, 0, 270)),
        owning_building_guid = 20,
        door_guid = 638
      )
      LocalObject(
        884,
        IFFLock.Constructor(Vector3(4024.813f, 4213.572f, 250.3799f), Vector3(0, 0, 90)),
        owning_building_guid = 20,
        door_guid = 642
      )
      LocalObject(
        885,
        IFFLock.Constructor(Vector3(4057.953f, 4320.808f, 267.9548f), Vector3(0, 0, 0)),
        owning_building_guid = 20,
        door_guid = 404
      )
      LocalObject(1177, Locker.Constructor(Vector3(3991.563f, 4250.141f, 256.8048f)), owning_building_guid = 20)
      LocalObject(1178, Locker.Constructor(Vector3(3992.727f, 4250.141f, 256.8048f)), owning_building_guid = 20)
      LocalObject(1179, Locker.Constructor(Vector3(3993.874f, 4250.141f, 256.8048f)), owning_building_guid = 20)
      LocalObject(1180, Locker.Constructor(Vector3(3995.023f, 4250.141f, 256.8048f)), owning_building_guid = 20)
      LocalObject(1181, Locker.Constructor(Vector3(4002.194f, 4270.165f, 249.0439f)), owning_building_guid = 20)
      LocalObject(1182, Locker.Constructor(Vector3(4003.518f, 4270.165f, 249.0439f)), owning_building_guid = 20)
      LocalObject(1183, Locker.Constructor(Vector3(4004.854f, 4270.165f, 249.0439f)), owning_building_guid = 20)
      LocalObject(1184, Locker.Constructor(Vector3(4006.191f, 4270.165f, 249.0439f)), owning_building_guid = 20)
      LocalObject(1185, Locker.Constructor(Vector3(4010.731f, 4270.165f, 249.0439f)), owning_building_guid = 20)
      LocalObject(1186, Locker.Constructor(Vector3(4012.055f, 4270.165f, 249.0439f)), owning_building_guid = 20)
      LocalObject(1187, Locker.Constructor(Vector3(4013.391f, 4270.165f, 249.0439f)), owning_building_guid = 20)
      LocalObject(1188, Locker.Constructor(Vector3(4014.728f, 4270.165f, 249.0439f)), owning_building_guid = 20)
      LocalObject(
        224,
        Terminal.Constructor(Vector3(4003.879f, 4342.918f, 272.1479f), dropship_vehicle_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        223,
        VehicleSpawnPad.Constructor(Vector3(4012.328f, 4364.856f, 266.4719f), dropship_pad_doors, Vector3(0, 0, 90)),
        owning_building_guid = 20,
        terminal_guid = 224
      )
      LocalObject(
        1564,
        Terminal.Constructor(Vector3(3934.378f, 4216.897f, 272.9038f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1568,
        Terminal.Constructor(Vector3(3944.075f, 4232.547f, 280.2989f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1569,
        Terminal.Constructor(Vector3(3946.331f, 4230.43f, 280.2989f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1570,
        Terminal.Constructor(Vector3(3946.332f, 4234.825f, 280.2989f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1571,
        Terminal.Constructor(Vector3(3948.592f, 4232.59f, 280.2989f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1576,
        Terminal.Constructor(Vector3(3994.654f, 4255.408f, 258.1339f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1577,
        Terminal.Constructor(Vector3(3994.654f, 4259.139f, 258.1339f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1578,
        Terminal.Constructor(Vector3(3994.654f, 4262.928f, 258.1339f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2239,
        Terminal.Constructor(Vector3(3942.509f, 4215.959f, 273.1609f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2244,
        Terminal.Constructor(Vector3(3980.971f, 4251.243f, 258.6779f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2245,
        Terminal.Constructor(Vector3(3980.967f, 4258.535f, 258.6779f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2246,
        Terminal.Constructor(Vector3(3980.97f, 4265.823f, 258.6779f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2249,
        Terminal.Constructor(Vector3(3999.103f, 4334.906f, 273.0919f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2250,
        Terminal.Constructor(Vector3(4008.058f, 4239.409f, 250.5719f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2251,
        Terminal.Constructor(Vector3(4015.409f, 4295.942f, 250.5719f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2253,
        Terminal.Constructor(Vector3(4024.058f, 4247.409f, 258.1009f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2254,
        Terminal.Constructor(Vector3(4024.058f, 4287.409f, 258.1009f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2419,
        Terminal.Constructor(Vector3(3961.698f, 4348.044f, 269.2308f), ground_vehicle_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1479,
        VehicleSpawnPad.Constructor(Vector3(3961.786f, 4334.411f, 265.0728f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 20,
        terminal_guid = 2419
      )
      LocalObject(2094, ResourceSilo.Constructor(Vector3(4042.212f, 4405.642f, 273.5609f)), owning_building_guid = 20)
      LocalObject(
        2145,
        SpawnTube.Constructor(Vector3(3980.233f, 4252.683f, 256.5439f), Vector3(0, 0, 0)),
        owning_building_guid = 20
      )
      LocalObject(
        2146,
        SpawnTube.Constructor(Vector3(3980.233f, 4259.974f, 256.5439f), Vector3(0, 0, 0)),
        owning_building_guid = 20
      )
      LocalObject(
        2147,
        SpawnTube.Constructor(Vector3(3980.233f, 4267.262f, 256.5439f), Vector3(0, 0, 0)),
        owning_building_guid = 20
      )
      LocalObject(
        1492,
        ProximityTerminal.Constructor(Vector3(3944.863f, 4211.013f, 266.5439f), medical_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1494,
        ProximityTerminal.Constructor(Vector3(4008.444f, 4269.62f, 249.0439f), medical_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1744,
        ProximityTerminal.Constructor(Vector3(3925.153f, 4333.398f, 274.8539f), pad_landing_frame),
        owning_building_guid = 20
      )
      LocalObject(
        1745,
        Terminal.Constructor(Vector3(3925.153f, 4333.398f, 274.8539f), air_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1747,
        ProximityTerminal.Constructor(Vector3(3941.514f, 4287.467f, 272.1378f), pad_landing_frame),
        owning_building_guid = 20
      )
      LocalObject(
        1748,
        Terminal.Constructor(Vector3(3941.514f, 4287.467f, 272.1378f), air_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1756,
        ProximityTerminal.Constructor(Vector3(3993.804f, 4251.901f, 279.3199f), pad_landing_frame),
        owning_building_guid = 20
      )
      LocalObject(
        1757,
        Terminal.Constructor(Vector3(3993.804f, 4251.901f, 279.3199f), air_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1765,
        ProximityTerminal.Constructor(Vector3(4029.071f, 4268.159f, 274.8669f), pad_landing_frame),
        owning_building_guid = 20
      )
      LocalObject(
        1766,
        Terminal.Constructor(Vector3(4029.071f, 4268.159f, 274.8669f), air_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2049,
        ProximityTerminal.Constructor(Vector3(3882.642f, 4278.241f, 266.1938f), repair_silo),
        owning_building_guid = 20
      )
      LocalObject(
        2050,
        Terminal.Constructor(Vector3(3882.642f, 4278.241f, 266.1938f), ground_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2061,
        ProximityTerminal.Constructor(Vector3(4052.57f, 4281.151f, 266.1938f), repair_silo),
        owning_building_guid = 20
      )
      LocalObject(
        2062,
        Terminal.Constructor(Vector3(4052.57f, 4281.151f, 266.1938f), ground_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1418,
        FacilityTurret.Constructor(Vector3(3870.401f, 4313.113f, 275.0518f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1418, 5012)
      LocalObject(
        1419,
        FacilityTurret.Constructor(Vector3(3871.554f, 4159.565f, 275.0518f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1419, 5013)
      LocalObject(
        1421,
        FacilityTurret.Constructor(Vector3(3915.445f, 4359.667f, 275.0518f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1421, 5014)
      LocalObject(
        1426,
        FacilityTurret.Constructor(Vector3(3974.428f, 4158.396f, 275.0518f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1426, 5015)
      LocalObject(
        1427,
        FacilityTurret.Constructor(Vector3(3975.449f, 4418.154f, 275.0518f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1427, 5016)
      LocalObject(
        1428,
        FacilityTurret.Constructor(Vector3(4016.537f, 4199.011f, 275.0518f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1428, 5017)
      LocalObject(
        1431,
        FacilityTurret.Constructor(Vector3(4063.619f, 4416.985f, 275.0518f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1431, 5018)
      LocalObject(
        1432,
        FacilityTurret.Constructor(Vector3(4064.773f, 4248.733f, 275.0518f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1432, 5019)
      LocalObject(
        1880,
        Painbox.Constructor(Vector3(3972.428f, 4300.057f, 252.9381f), painbox),
        owning_building_guid = 20
      )
      LocalObject(
        1890,
        Painbox.Constructor(Vector3(3989.857f, 4260.408f, 260.5714f), painbox_continuous),
        owning_building_guid = 20
      )
      LocalObject(
        1899,
        Painbox.Constructor(Vector3(3986.203f, 4298.915f, 252.176f), painbox_door_radius),
        owning_building_guid = 20
      )
      LocalObject(
        1918,
        Painbox.Constructor(Vector3(3987.087f, 4245.386f, 258.9731f), painbox_door_radius_continuous),
        owning_building_guid = 20
      )
      LocalObject(
        1919,
        Painbox.Constructor(Vector3(3987.895f, 4274.081f, 259.4438f), painbox_door_radius_continuous),
        owning_building_guid = 20
      )
      LocalObject(
        1920,
        Painbox.Constructor(Vector3(4002.317f, 4267.888f, 259.8741f), painbox_door_radius_continuous),
        owning_building_guid = 20
      )
      LocalObject(241, Generator.Constructor(Vector3(3968.445f, 4299.975f, 247.7498f)), owning_building_guid = 20)
      LocalObject(
        232,
        Terminal.Constructor(Vector3(3976.637f, 4300.022f, 249.0439f), gen_control),
        owning_building_guid = 20
      )
    }

    Building8()

    def Building8(): Unit = { // Name: Tarqaq Type: cryo_facility GUID: 23, MapID: 8
      LocalBuilding(
        "Tarqaq",
        23,
        8,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(2980f, 2228f, 237.2173f),
            Vector3(0f, 0f, 218f),
            cryo_facility
          )
        )
      )
      LocalObject(
        175,
        CaptureTerminal.Constructor(Vector3(3038.622f, 2198.591f, 227.2173f), capture_terminal),
        owning_building_guid = 23
      )
      LocalObject(272, Door.Constructor(Vector3(2898.143f, 2280.796f, 238.7383f)), owning_building_guid = 23)
      LocalObject(273, Door.Constructor(Vector3(2908.659f, 2236.593f, 246.7323f)), owning_building_guid = 23)
      LocalObject(274, Door.Constructor(Vector3(2910.427f, 2278.879f, 238.7683f)), owning_building_guid = 23)
      LocalObject(275, Door.Constructor(Vector3(2919.86f, 2222.257f, 238.7683f)), owning_building_guid = 23)
      LocalObject(276, Door.Constructor(Vector3(2924.763f, 2290.08f, 246.7323f)), owning_building_guid = 23)
      LocalObject(282, Door.Constructor(Vector3(2978.779f, 2223.065f, 248.7383f)), owning_building_guid = 23)
      LocalObject(283, Door.Constructor(Vector3(3004.921f, 2222.09f, 248.7383f)), owning_building_guid = 23)
      LocalObject(284, Door.Constructor(Vector3(3029.245f, 2260.764f, 238.7683f)), owning_building_guid = 23)
      LocalObject(285, Door.Constructor(Vector3(3040.446f, 2246.427f, 246.7323f)), owning_building_guid = 23)
      LocalObject(286, Door.Constructor(Vector3(3040.761f, 2189.428f, 246.7323f)), owning_building_guid = 23)
      LocalObject(288, Door.Constructor(Vector3(3055.097f, 2200.629f, 238.7683f)), owning_building_guid = 23)
      LocalObject(518, Door.Constructor(Vector3(2935.278f, 2304.733f, 231.2383f)), owning_building_guid = 23)
      LocalObject(519, Door.Constructor(Vector3(2961.283f, 2284.442f, 231.2383f)), owning_building_guid = 23)
      LocalObject(520, Door.Constructor(Vector3(2962.761f, 2250.064f, 228.7383f)), owning_building_guid = 23)
      LocalObject(521, Door.Constructor(Vector3(2963.55f, 2210.072f, 221.2383f)), owning_building_guid = 23)
      LocalObject(522, Door.Constructor(Vector3(2970.544f, 2220.612f, 228.7383f)), owning_building_guid = 23)
      LocalObject(523, Door.Constructor(Vector3(2971.923f, 2231.842f, 248.7383f)), owning_building_guid = 23)
      LocalObject(524, Door.Constructor(Vector3(2973.401f, 2197.464f, 228.7383f)), owning_building_guid = 23)
      LocalObject(525, Door.Constructor(Vector3(2974.581f, 2299.907f, 231.2383f)), owning_building_guid = 23)
      LocalObject(526, Door.Constructor(Vector3(2976.059f, 2265.53f, 228.7383f)), owning_building_guid = 23)
      LocalObject(527, Door.Constructor(Vector3(2977.537f, 2231.152f, 228.7383f)), owning_building_guid = 23)
      LocalObject(528, Door.Constructor(Vector3(2985.32f, 2201.7f, 228.7383f)), owning_building_guid = 23)
      LocalObject(529, Door.Constructor(Vector3(2989.456f, 2235.388f, 228.7383f)), owning_building_guid = 23)
      LocalObject(530, Door.Constructor(Vector3(3004.921f, 2222.09f, 228.7383f)), owning_building_guid = 23)
      LocalObject(531, Door.Constructor(Vector3(3004.921f, 2222.09f, 238.7383f)), owning_building_guid = 23)
      LocalObject(532, Door.Constructor(Vector3(3008.368f, 2250.164f, 228.7383f)), owning_building_guid = 23)
      LocalObject(533, Door.Constructor(Vector3(3010.437f, 2267.008f, 228.7383f)), owning_building_guid = 23)
      LocalObject(534, Door.Constructor(Vector3(3012.014f, 2187.023f, 228.7383f)), owning_building_guid = 23)
      LocalObject(535, Door.Constructor(Vector3(3014.083f, 2203.867f, 228.7383f)), owning_building_guid = 23)
      LocalObject(536, Door.Constructor(Vector3(3017.53f, 2231.941f, 228.7383f)), owning_building_guid = 23)
      LocalObject(537, Door.Constructor(Vector3(3032.995f, 2218.643f, 228.7383f)), owning_building_guid = 23)
      LocalObject(538, Door.Constructor(Vector3(3034.473f, 2184.266f, 228.7383f)), owning_building_guid = 23)
      LocalObject(539, Door.Constructor(Vector3(3040.777f, 2189.191f, 228.7383f)), owning_building_guid = 23)
      LocalObject(540, Door.Constructor(Vector3(3047.081f, 2194.116f, 228.7383f)), owning_building_guid = 23)
      LocalObject(711, Door.Constructor(Vector3(2992.297f, 2205.777f, 239.5003f)), owning_building_guid = 23)
      LocalObject(720, Door.Constructor(Vector3(2970.544f, 2220.612f, 238.7383f)), owning_building_guid = 23)
      LocalObject(721, Door.Constructor(Vector3(2988.077f, 2224.158f, 238.7363f)), owning_building_guid = 23)
      LocalObject(2289, Door.Constructor(Vector3(2979.847f, 2220.605f, 229.0713f)), owning_building_guid = 23)
      LocalObject(2290, Door.Constructor(Vector3(2984.337f, 2214.858f, 229.0713f)), owning_building_guid = 23)
      LocalObject(2291, Door.Constructor(Vector3(2988.825f, 2209.115f, 229.0713f)), owning_building_guid = 23)
      LocalObject(
        747,
        IFFLock.Constructor(Vector3(2996.522f, 2205.602f, 238.6993f), Vector3(0, 0, 142)),
        owning_building_guid = 23,
        door_guid = 711
      )
      LocalObject(
        785,
        IFFLock.Constructor(Vector3(2898.759f, 2278.686f, 238.6693f), Vector3(0, 0, 232)),
        owning_building_guid = 23,
        door_guid = 272
      )
      LocalObject(
        790,
        IFFLock.Constructor(Vector3(2963.221f, 2211.81f, 221.0533f), Vector3(0, 0, 52)),
        owning_building_guid = 23,
        door_guid = 521
      )
      LocalObject(
        791,
        IFFLock.Constructor(Vector3(2968.806f, 2220.282f, 228.5533f), Vector3(0, 0, 322)),
        owning_building_guid = 23,
        door_guid = 522
      )
      LocalObject(
        792,
        IFFLock.Constructor(Vector3(2980.897f, 2223.678f, 248.6693f), Vector3(0, 0, 142)),
        owning_building_guid = 23,
        door_guid = 282
      )
      LocalObject(
        793,
        IFFLock.Constructor(Vector3(2987.057f, 2202.029f, 228.5533f), Vector3(0, 0, 142)),
        owning_building_guid = 23,
        door_guid = 528
      )
      LocalObject(
        794,
        IFFLock.Constructor(Vector3(3005.538f, 2219.979f, 248.6693f), Vector3(0, 0, 232)),
        owning_building_guid = 23,
        door_guid = 283
      )
      LocalObject(
        795,
        IFFLock.Constructor(Vector3(3041.104f, 2187.452f, 228.5533f), Vector3(0, 0, 232)),
        owning_building_guid = 23,
        door_guid = 539
      )
      LocalObject(
        796,
        IFFLock.Constructor(Vector3(3046.854f, 2195.934f, 228.5533f), Vector3(0, 0, 52)),
        owning_building_guid = 23,
        door_guid = 540
      )
      LocalObject(1027, Locker.Constructor(Vector3(2951.431f, 2202.685f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1028, Locker.Constructor(Vector3(2952.263f, 2203.334f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1029, Locker.Constructor(Vector3(2953.098f, 2203.987f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1030, Locker.Constructor(Vector3(2953.929f, 2204.636f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1031, Locker.Constructor(Vector3(2954.761f, 2205.286f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1032, Locker.Constructor(Vector3(2955.593f, 2205.936f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1033, Locker.Constructor(Vector3(2963.748f, 2186.926f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1034, Locker.Constructor(Vector3(2964.58f, 2187.576f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1035, Locker.Constructor(Vector3(2965.411f, 2188.225f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1036, Locker.Constructor(Vector3(2966.242f, 2188.875f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1037, Locker.Constructor(Vector3(2966.328f, 2214.601f, 227.4783f)), owning_building_guid = 23)
      LocalObject(1038, Locker.Constructor(Vector3(2967.078f, 2189.528f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1039, Locker.Constructor(Vector3(2967.233f, 2215.309f, 227.4783f)), owning_building_guid = 23)
      LocalObject(1040, Locker.Constructor(Vector3(2967.909f, 2190.177f, 227.1253f)), owning_building_guid = 23)
      LocalObject(1041, Locker.Constructor(Vector3(2968.137f, 2216.015f, 227.4783f)), owning_building_guid = 23)
      LocalObject(1042, Locker.Constructor(Vector3(2969.054f, 2216.731f, 227.4783f)), owning_building_guid = 23)
      LocalObject(1043, Locker.Constructor(Vector3(2994.715f, 2176.685f, 227.2123f)), owning_building_guid = 23)
      LocalObject(1044, Locker.Constructor(Vector3(2995.488f, 2175.696f, 227.2123f)), owning_building_guid = 23)
      LocalObject(1045, Locker.Constructor(Vector3(2996.265f, 2174.702f, 227.2123f)), owning_building_guid = 23)
      LocalObject(1046, Locker.Constructor(Vector3(2997.042f, 2173.707f, 227.2123f)), owning_building_guid = 23)
      LocalObject(1047, Locker.Constructor(Vector3(2997.812f, 2172.721f, 227.2123f)), owning_building_guid = 23)
      LocalObject(1338, Locker.Constructor(Vector3(2958.092f, 2226.382f, 237.2173f)), owning_building_guid = 23)
      LocalObject(1339, Locker.Constructor(Vector3(2958.729f, 2225.567f, 237.2173f)), owning_building_guid = 23)
      LocalObject(1340, Locker.Constructor(Vector3(2960.278f, 2223.584f, 236.9883f)), owning_building_guid = 23)
      LocalObject(1341, Locker.Constructor(Vector3(2960.915f, 2222.769f, 236.9883f)), owning_building_guid = 23)
      LocalObject(1342, Locker.Constructor(Vector3(2961.563f, 2221.938f, 236.9883f)), owning_building_guid = 23)
      LocalObject(1343, Locker.Constructor(Vector3(2962.2f, 2221.124f, 236.9883f)), owning_building_guid = 23)
      LocalObject(1344, Locker.Constructor(Vector3(2963.753f, 2219.136f, 237.2173f)), owning_building_guid = 23)
      LocalObject(1345, Locker.Constructor(Vector3(2964.389f, 2218.322f, 237.2173f)), owning_building_guid = 23)
      LocalObject(
        184,
        Terminal.Constructor(Vector3(2995.622f, 2178.975f, 227.2073f), cert_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        185,
        Terminal.Constructor(Vector3(2995.872f, 2181.008f, 227.2073f), cert_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        186,
        Terminal.Constructor(Vector3(3000.132f, 2173.203f, 227.2073f), cert_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        187,
        Terminal.Constructor(Vector3(3002.165f, 2172.954f, 227.2073f), cert_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        188,
        Terminal.Constructor(Vector3(3005.88f, 2188.827f, 227.2073f), cert_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        189,
        Terminal.Constructor(Vector3(3007.912f, 2188.577f, 227.2073f), cert_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        190,
        Terminal.Constructor(Vector3(3012.172f, 2180.772f, 227.2073f), cert_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        191,
        Terminal.Constructor(Vector3(3012.422f, 2182.805f, 227.2073f), cert_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1521,
        Terminal.Constructor(Vector3(2969.861f, 2210.678f, 228.8073f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1522,
        Terminal.Constructor(Vector3(2972.158f, 2207.738f, 228.8073f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1523,
        Terminal.Constructor(Vector3(2974.491f, 2204.752f, 228.8073f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1524,
        Terminal.Constructor(Vector3(2993.767f, 2226.667f, 238.5123f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2212,
        Terminal.Constructor(Vector3(2957.839f, 2282.5f, 231.3303f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2213,
        Terminal.Constructor(Vector3(2970.101f, 2237.576f, 238.7963f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2214,
        Terminal.Constructor(Vector3(2978.079f, 2222.384f, 229.3513f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2215,
        Terminal.Constructor(Vector3(2982.572f, 2216.64f, 229.3513f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2216,
        Terminal.Constructor(Vector3(2987.057f, 2210.896f, 229.3513f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2217,
        Terminal.Constructor(Vector3(3011.155f, 2253.094f, 228.8303f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2218,
        Terminal.Constructor(Vector3(3019.87f, 2177.932f, 228.8303f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2414,
        Terminal.Constructor(Vector3(2911.448f, 2254.625f, 239.5223f), vehicle_terminal_combined),
        owning_building_guid = 23
      )
      LocalObject(
        1470,
        VehicleSpawnPad.Constructor(Vector3(2922.14f, 2263.093f, 235.3643f), mb_pad_creation, Vector3(0, 0, 52)),
        owning_building_guid = 23,
        terminal_guid = 2414
      )
      LocalObject(2089, ResourceSilo.Constructor(Vector3(3058.799f, 2225.032f, 244.2343f)), owning_building_guid = 23)
      LocalObject(
        2112,
        SpawnTube.Constructor(Vector3(2979.548f, 2221.704f, 227.2173f), Vector3(0, 0, 142)),
        owning_building_guid = 23
      )
      LocalObject(
        2113,
        SpawnTube.Constructor(Vector3(2984.036f, 2215.958f, 227.2173f), Vector3(0, 0, 142)),
        owning_building_guid = 23
      )
      LocalObject(
        2114,
        SpawnTube.Constructor(Vector3(2988.523f, 2210.215f, 227.2173f), Vector3(0, 0, 142)),
        owning_building_guid = 23
      )
      LocalObject(
        137,
        ProximityTerminal.Constructor(Vector3(2974.061f, 2232.38f, 237.0273f), adv_med_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1485,
        ProximityTerminal.Constructor(Vector3(2960.651f, 2207.868f, 227.2173f), medical_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1693,
        ProximityTerminal.Constructor(Vector3(2938.802f, 2283.658f, 245.5693f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        1694,
        Terminal.Constructor(Vector3(2938.802f, 2283.658f, 245.5693f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1696,
        ProximityTerminal.Constructor(Vector3(2956.143f, 2287.963f, 247.5103f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        1697,
        Terminal.Constructor(Vector3(2956.143f, 2287.963f, 247.5103f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1699,
        ProximityTerminal.Constructor(Vector3(3006.818f, 2191.526f, 247.5493f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        1700,
        Terminal.Constructor(Vector3(3006.818f, 2191.526f, 247.5493f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1702,
        ProximityTerminal.Constructor(Vector3(3024.771f, 2194.895f, 245.5593f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        1703,
        Terminal.Constructor(Vector3(3024.771f, 2194.895f, 245.5593f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2017,
        ProximityTerminal.Constructor(Vector3(2948.371f, 2183.161f, 236.9673f), repair_silo),
        owning_building_guid = 23
      )
      LocalObject(
        2018,
        Terminal.Constructor(Vector3(2948.371f, 2183.161f, 236.9673f), ground_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2021,
        ProximityTerminal.Constructor(Vector3(2955.719f, 2316.7f, 236.9673f), repair_silo),
        owning_building_guid = 23
      )
      LocalObject(
        2022,
        Terminal.Constructor(Vector3(2955.719f, 2316.7f, 236.9673f), ground_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1381,
        FacilityTurret.Constructor(Vector3(2870.371f, 2264.973f, 245.6193f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1381, 5020)
      LocalObject(
        1384,
        FacilityTurret.Constructor(Vector3(2952.627f, 2157.762f, 245.6193f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1384, 5021)
      LocalObject(
        1385,
        FacilityTurret.Constructor(Vector3(2976.936f, 2348.22f, 245.6193f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1385, 5022)
      LocalObject(
        1386,
        FacilityTurret.Constructor(Vector3(3013.169f, 2150.356f, 245.6193f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1386, 5023)
      LocalObject(
        1387,
        FacilityTurret.Constructor(Vector3(3085.971f, 2208.674f, 245.6193f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1387, 5024)
      LocalObject(
        732,
        ImplantTerminalMech.Constructor(Vector3(2999.384f, 2186.84f, 226.6943f)),
        owning_building_guid = 23
      )
      LocalObject(
        726,
        Terminal.Constructor(Vector3(2999.395f, 2186.826f, 226.6943f), implant_terminal_interface),
        owning_building_guid = 23
      )
      TerminalToInterface(732, 726)
      LocalObject(
        733,
        ImplantTerminalMech.Constructor(Vector3(3008.847f, 2174.747f, 226.6943f)),
        owning_building_guid = 23
      )
      LocalObject(
        727,
        Terminal.Constructor(Vector3(3008.836f, 2174.761f, 226.6943f), implant_terminal_interface),
        owning_building_guid = 23
      )
      TerminalToInterface(733, 727)
      LocalObject(
        1876,
        Painbox.Constructor(Vector3(2963.485f, 2240.053f, 251.2461f), painbox),
        owning_building_guid = 23
      )
      LocalObject(
        1885,
        Painbox.Constructor(Vector3(2973.123f, 2212.84f, 231.2872f), painbox_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        1894,
        Painbox.Constructor(Vector3(2973.501f, 2229.524f, 251.4512f), painbox_door_radius),
        owning_building_guid = 23
      )
      LocalObject(
        1903,
        Painbox.Constructor(Vector3(2969.758f, 2195.823f, 230.7582f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        1904,
        Painbox.Constructor(Vector3(2970.336f, 2223.25f, 229.5732f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        1905,
        Painbox.Constructor(Vector3(2986.041f, 2199.939f, 228.9315f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(237, Generator.Constructor(Vector3(2962.326f, 2244.083f, 245.9233f)), owning_building_guid = 23)
      LocalObject(
        228,
        Terminal.Constructor(Vector3(2967.407f, 2237.657f, 247.2173f), gen_control),
        owning_building_guid = 23
      )
    }

    Building7()

    def Building7(): Unit = { // Name: Sedna Type: cryo_facility GUID: 26, MapID: 7
      LocalBuilding(
        "Sedna",
        26,
        7,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3982f, 5224f, 232.2285f),
            Vector3(0f, 0f, 179f),
            cryo_facility
          )
        )
      )
      LocalObject(
        179,
        CaptureTerminal.Constructor(Vector3(4009.05f, 5164.253f, 222.2285f), capture_terminal),
        owning_building_guid = 26
      )
      LocalObject(381, Door.Constructor(Vector3(3931.648f, 5257.384f, 233.7795f)), owning_building_guid = 26)
      LocalObject(382, Door.Constructor(Vector3(3931.966f, 5275.574f, 241.7435f)), owning_building_guid = 26)
      LocalObject(386, Door.Constructor(Vector3(3951.611f, 5316.544f, 233.7495f)), owning_building_guid = 26)
      LocalObject(388, Door.Constructor(Vector3(3959.951f, 5307.325f, 233.7795f)), owning_building_guid = 26)
      LocalObject(389, Door.Constructor(Vector3(3977.946f, 5220.933f, 243.7495f)), owning_building_guid = 26)
      LocalObject(390, Door.Constructor(Vector3(3978.141f, 5307.007f, 241.7435f)), owning_building_guid = 26)
      LocalObject(392, Door.Constructor(Vector3(3997.648f, 5203.724f, 243.7495f)), owning_building_guid = 26)
      LocalObject(395, Door.Constructor(Vector3(4004.946f, 5155.786f, 241.7435f)), owning_building_guid = 26)
      LocalObject(397, Door.Constructor(Vector3(4023.136f, 5155.469f, 233.7795f)), owning_building_guid = 26)
      LocalObject(400, Door.Constructor(Vector3(4040.572f, 5200.281f, 241.7435f)), owning_building_guid = 26)
      LocalObject(401, Door.Constructor(Vector3(4040.889f, 5218.471f, 233.7795f)), owning_building_guid = 26)
      LocalObject(608, Door.Constructor(Vector3(3957.655f, 5204.422f, 223.7495f)), owning_building_guid = 26)
      LocalObject(609, Door.Constructor(Vector3(3957.934f, 5220.419f, 216.2495f)), owning_building_guid = 26)
      LocalObject(612, Door.Constructor(Vector3(3969.583f, 5200.213f, 223.7495f)), owning_building_guid = 26)
      LocalObject(613, Door.Constructor(Vector3(3970.002f, 5224.209f, 223.7495f)), owning_building_guid = 26)
      LocalObject(615, Door.Constructor(Vector3(3978.14f, 5232.068f, 243.7495f)), owning_building_guid = 26)
      LocalObject(616, Door.Constructor(Vector3(3981.093f, 5172.008f, 223.7495f)), owning_building_guid = 26)
      LocalObject(617, Door.Constructor(Vector3(3982.07f, 5228f, 223.7495f)), owning_building_guid = 26)
      LocalObject(618, Door.Constructor(Vector3(3982.489f, 5251.996f, 223.7495f)), owning_building_guid = 26)
      LocalObject(624, Door.Constructor(Vector3(3993.3f, 5183.797f, 223.7495f)), owning_building_guid = 26)
      LocalObject(625, Door.Constructor(Vector3(3993.998f, 5223.791f, 223.7495f)), owning_building_guid = 26)
      LocalObject(626, Door.Constructor(Vector3(3995.534f, 5311.777f, 226.2495f)), owning_building_guid = 26)
      LocalObject(627, Door.Constructor(Vector3(3996.811f, 5155.731f, 223.7495f)), owning_building_guid = 26)
      LocalObject(628, Door.Constructor(Vector3(3997.648f, 5203.724f, 223.7495f)), owning_building_guid = 26)
      LocalObject(629, Door.Constructor(Vector3(3997.648f, 5203.724f, 233.7495f)), owning_building_guid = 26)
      LocalObject(632, Door.Constructor(Vector3(4002.555f, 5255.646f, 223.7495f)), owning_building_guid = 26)
      LocalObject(633, Door.Constructor(Vector3(4002.974f, 5279.643f, 226.2495f)), owning_building_guid = 26)
      LocalObject(635, Door.Constructor(Vector3(4004.81f, 5155.591f, 223.7495f)), owning_building_guid = 26)
      LocalObject(636, Door.Constructor(Vector3(4012.808f, 5155.452f, 223.7495f)), owning_building_guid = 26)
      LocalObject(637, Door.Constructor(Vector3(4013.646f, 5203.444f, 223.7495f)), owning_building_guid = 26)
      LocalObject(639, Door.Constructor(Vector3(4017.296f, 5183.378f, 223.7495f)), owning_building_guid = 26)
      LocalObject(640, Door.Constructor(Vector3(4017.995f, 5223.372f, 223.7495f)), owning_building_guid = 26)
      LocalObject(641, Door.Constructor(Vector3(4023.041f, 5283.293f, 226.2495f)), owning_building_guid = 26)
      LocalObject(644, Door.Constructor(Vector3(4030.202f, 5235.161f, 223.7495f)), owning_building_guid = 26)
      LocalObject(716, Door.Constructor(Vector3(3977.571f, 5198.991f, 234.5115f)), owning_building_guid = 26)
      LocalObject(722, Door.Constructor(Vector3(3970.002f, 5224.209f, 233.7495f)), owning_building_guid = 26)
      LocalObject(723, Door.Constructor(Vector3(3985.86f, 5215.932f, 233.7475f)), owning_building_guid = 26)
      LocalObject(2319, Door.Constructor(Vector3(3976.973f, 5203.77f, 224.0825f)), owning_building_guid = 26)
      LocalObject(2320, Door.Constructor(Vector3(3977.1f, 5211.058f, 224.0825f)), owning_building_guid = 26)
      LocalObject(2321, Door.Constructor(Vector3(3977.228f, 5218.35f, 224.0825f)), owning_building_guid = 26)
      LocalObject(
        752,
        IFFLock.Constructor(Vector3(3980.745f, 5196.196f, 233.7105f), Vector3(0, 0, 181)),
        owning_building_guid = 26,
        door_guid = 716
      )
      LocalObject(
        867,
        IFFLock.Constructor(Vector3(3950.761f, 5314.517f, 233.6805f), Vector3(0, 0, 271)),
        owning_building_guid = 26,
        door_guid = 386
      )
      LocalObject(
        869,
        IFFLock.Constructor(Vector3(3958.771f, 5221.977f, 216.0645f), Vector3(0, 0, 91)),
        owning_building_guid = 26,
        door_guid = 609
      )
      LocalObject(
        870,
        IFFLock.Constructor(Vector3(3968.444f, 5225.047f, 223.5645f), Vector3(0, 0, 1)),
        owning_building_guid = 26,
        door_guid = 613
      )
      LocalObject(
        871,
        IFFLock.Constructor(Vector3(3971.141f, 5199.376f, 223.5645f), Vector3(0, 0, 181)),
        owning_building_guid = 26,
        door_guid = 612
      )
      LocalObject(
        872,
        IFFLock.Constructor(Vector3(3979.977f, 5220.077f, 243.6805f), Vector3(0, 0, 181)),
        owning_building_guid = 26,
        door_guid = 389
      )
      LocalObject(
        877,
        IFFLock.Constructor(Vector3(3996.799f, 5201.695f, 243.6805f), Vector3(0, 0, 271)),
        owning_building_guid = 26,
        door_guid = 392
      )
      LocalObject(
        880,
        IFFLock.Constructor(Vector3(4003.969f, 5154.034f, 223.5645f), Vector3(0, 0, 271)),
        owning_building_guid = 26,
        door_guid = 635
      )
      LocalObject(
        881,
        IFFLock.Constructor(Vector3(4013.776f, 5157.007f, 223.5645f), Vector3(0, 0, 91)),
        owning_building_guid = 26,
        door_guid = 636
      )
      LocalObject(1156, Locker.Constructor(Vector3(3943.521f, 5202.307f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1157, Locker.Constructor(Vector3(3943.867f, 5222.305f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1158, Locker.Constructor(Vector3(3944.576f, 5202.289f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1159, Locker.Constructor(Vector3(3944.921f, 5222.287f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1160, Locker.Constructor(Vector3(3945.631f, 5202.271f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1161, Locker.Constructor(Vector3(3945.981f, 5222.269f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1162, Locker.Constructor(Vector3(3946.686f, 5202.252f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1163, Locker.Constructor(Vector3(3947.036f, 5222.25f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1164, Locker.Constructor(Vector3(3947.747f, 5202.233f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1165, Locker.Constructor(Vector3(3948.091f, 5222.231f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1166, Locker.Constructor(Vector3(3948.801f, 5202.215f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1167, Locker.Constructor(Vector3(3949.147f, 5222.213f, 222.1365f)), owning_building_guid = 26)
      LocalObject(1168, Locker.Constructor(Vector3(3961.054f, 5169.83f, 222.2235f)), owning_building_guid = 26)
      LocalObject(1169, Locker.Constructor(Vector3(3961.076f, 5171.082f, 222.2235f)), owning_building_guid = 26)
      LocalObject(1170, Locker.Constructor(Vector3(3961.098f, 5172.344f, 222.2235f)), owning_building_guid = 26)
      LocalObject(1171, Locker.Constructor(Vector3(3961.12f, 5173.604f, 222.2235f)), owning_building_guid = 26)
      LocalObject(1172, Locker.Constructor(Vector3(3961.142f, 5174.86f, 222.2235f)), owning_building_guid = 26)
      LocalObject(1173, Locker.Constructor(Vector3(3962.943f, 5222.191f, 222.4895f)), owning_building_guid = 26)
      LocalObject(1174, Locker.Constructor(Vector3(3964.091f, 5222.171f, 222.4895f)), owning_building_guid = 26)
      LocalObject(1175, Locker.Constructor(Vector3(3965.238f, 5222.151f, 222.4895f)), owning_building_guid = 26)
      LocalObject(1176, Locker.Constructor(Vector3(3966.402f, 5222.131f, 222.4895f)), owning_building_guid = 26)
      LocalObject(1346, Locker.Constructor(Vector3(3963.777f, 5226.302f, 232.2285f)), owning_building_guid = 26)
      LocalObject(1347, Locker.Constructor(Vector3(3963.795f, 5227.336f, 232.2285f)), owning_building_guid = 26)
      LocalObject(1348, Locker.Constructor(Vector3(3963.839f, 5229.858f, 231.9995f)), owning_building_guid = 26)
      LocalObject(1349, Locker.Constructor(Vector3(3963.857f, 5230.892f, 231.9995f)), owning_building_guid = 26)
      LocalObject(1350, Locker.Constructor(Vector3(3963.876f, 5231.945f, 231.9995f)), owning_building_guid = 26)
      LocalObject(1351, Locker.Constructor(Vector3(3963.894f, 5232.979f, 231.9995f)), owning_building_guid = 26)
      LocalObject(1352, Locker.Constructor(Vector3(3963.938f, 5235.496f, 232.2285f)), owning_building_guid = 26)
      LocalObject(1353, Locker.Constructor(Vector3(3963.956f, 5236.53f, 232.2285f)), owning_building_guid = 26)
      LocalObject(
        192,
        Terminal.Constructor(Vector3(3963.161f, 5168.746f, 222.2185f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        193,
        Terminal.Constructor(Vector3(3963.289f, 5176.069f, 222.2185f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        194,
        Terminal.Constructor(Vector3(3964.583f, 5167.272f, 222.2185f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        195,
        Terminal.Constructor(Vector3(3964.762f, 5177.492f, 222.2185f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        196,
        Terminal.Constructor(Vector3(3977.281f, 5167.051f, 222.2185f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        197,
        Terminal.Constructor(Vector3(3977.46f, 5177.27f, 222.2185f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        198,
        Terminal.Constructor(Vector3(3978.754f, 5168.473f, 222.2185f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        199,
        Terminal.Constructor(Vector3(3978.882f, 5175.797f, 222.2185f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1572,
        Terminal.Constructor(Vector3(3963.088f, 5209.4f, 223.8185f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1573,
        Terminal.Constructor(Vector3(3963.155f, 5213.188f, 223.8185f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1574,
        Terminal.Constructor(Vector3(3963.219f, 5216.918f, 223.8185f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1575,
        Terminal.Constructor(Vector3(3991.86f, 5214.3f, 233.5235f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2240,
        Terminal.Constructor(Vector3(3976.72f, 5206.267f, 224.3625f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2241,
        Terminal.Constructor(Vector3(3976.85f, 5213.553f, 224.3625f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2242,
        Terminal.Constructor(Vector3(3976.973f, 5220.844f, 224.3625f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2243,
        Terminal.Constructor(Vector3(3980.333f, 5237.672f, 233.8075f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2247,
        Terminal.Constructor(Vector3(3981.476f, 5160f, 223.8415f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2248,
        Terminal.Constructor(Vector3(3999.075f, 5280.301f, 226.3415f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2252,
        Terminal.Constructor(Vector3(4022.004f, 5223.895f, 223.8415f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2418,
        Terminal.Constructor(Vector3(3945.48f, 5287.833f, 234.5335f), vehicle_terminal_combined),
        owning_building_guid = 26
      )
      LocalObject(
        1478,
        VehicleSpawnPad.Constructor(Vector3(3959.119f, 5287.685f, 230.3755f), mb_pad_creation, Vector3(0, 0, 91)),
        owning_building_guid = 26,
        terminal_guid = 2418
      )
      LocalObject(2093, ResourceSilo.Constructor(Vector3(4041.37f, 5172.104f, 239.2455f)), owning_building_guid = 26)
      LocalObject(
        2142,
        SpawnTube.Constructor(Vector3(3977.431f, 5204.815f, 222.2285f), Vector3(0, 0, 181)),
        owning_building_guid = 26
      )
      LocalObject(
        2143,
        SpawnTube.Constructor(Vector3(3977.559f, 5212.102f, 222.2285f), Vector3(0, 0, 181)),
        owning_building_guid = 26
      )
      LocalObject(
        2144,
        SpawnTube.Constructor(Vector3(3977.686f, 5219.392f, 222.2285f), Vector3(0, 0, 181)),
        owning_building_guid = 26
      )
      LocalObject(
        138,
        ProximityTerminal.Constructor(Vector3(3980.141f, 5231.142f, 232.0385f), adv_med_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1493,
        ProximityTerminal.Constructor(Vector3(3954.293f, 5220.531f, 222.2285f), medical_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1750,
        ProximityTerminal.Constructor(Vector3(3979.887f, 5178.777f, 242.5605f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1751,
        Terminal.Constructor(Vector3(3979.887f, 5178.777f, 242.5605f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1753,
        ProximityTerminal.Constructor(Vector3(3985.01f, 5293.181f, 240.5805f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1754,
        Terminal.Constructor(Vector3(3985.01f, 5293.181f, 240.5805f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1759,
        ProximityTerminal.Constructor(Vector3(3995.96f, 5170.097f, 240.5705f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1760,
        Terminal.Constructor(Vector3(3995.96f, 5170.097f, 240.5705f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1762,
        ProximityTerminal.Constructor(Vector3(4001.195f, 5285.613f, 242.5215f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1763,
        Terminal.Constructor(Vector3(4001.195f, 5285.613f, 242.5215f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2053,
        ProximityTerminal.Constructor(Vector3(3929.201f, 5209.058f, 231.9785f), repair_silo),
        owning_building_guid = 26
      )
      LocalObject(
        2054,
        Terminal.Constructor(Vector3(3929.201f, 5209.058f, 231.9785f), ground_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2057,
        ProximityTerminal.Constructor(Vector3(4018.951f, 5308.214f, 231.9785f), repair_silo),
        owning_building_guid = 26
      )
      LocalObject(
        2058,
        Terminal.Constructor(Vector3(4018.951f, 5308.214f, 231.9785f), ground_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1422,
        FacilityTurret.Constructor(Vector3(3916.525f, 5186.641f, 240.6305f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1422, 5025)
      LocalObject(
        1423,
        FacilityTurret.Constructor(Vector3(3920.07f, 5321.725f, 240.6305f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1423, 5026)
      LocalObject(
        1425,
        FacilityTurret.Constructor(Vector3(3958.914f, 5142.786f, 240.6305f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1425, 5027)
      LocalObject(
        1429,
        FacilityTurret.Constructor(Vector3(4052.193f, 5142.291f, 240.6305f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1429, 5028)
      LocalObject(
        1430,
        FacilityTurret.Constructor(Vector3(4055.275f, 5319.357f, 240.6305f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1430, 5029)
      LocalObject(
        734,
        ImplantTerminalMech.Constructor(Vector3(3970.905f, 5164.46f, 221.7055f)),
        owning_building_guid = 26
      )
      LocalObject(
        728,
        Terminal.Constructor(Vector3(3970.906f, 5164.479f, 221.7055f), implant_terminal_interface),
        owning_building_guid = 26
      )
      TerminalToInterface(734, 728)
      LocalObject(
        735,
        ImplantTerminalMech.Constructor(Vector3(3971.161f, 5179.814f, 221.7055f)),
        owning_building_guid = 26
      )
      LocalObject(
        729,
        Terminal.Constructor(Vector3(3971.161f, 5179.796f, 221.7055f), implant_terminal_interface),
        owning_building_guid = 26
      )
      TerminalToInterface(735, 729)
      LocalObject(
        1881,
        Painbox.Constructor(Vector3(3976.751f, 5243.761f, 246.2573f), painbox),
        owning_building_guid = 26
      )
      LocalObject(
        1889,
        Painbox.Constructor(Vector3(3967.115f, 5216.547f, 226.2984f), painbox_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        1898,
        Painbox.Constructor(Vector3(3977.909f, 5229.274f, 246.4624f), painbox_door_radius),
        owning_building_guid = 26
      )
      LocalObject(
        1915,
        Painbox.Constructor(Vector3(3953.79f, 5205.439f, 225.7694f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        1916,
        Painbox.Constructor(Vector3(3969.036f, 5198.391f, 223.9427f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        1917,
        Painbox.Constructor(Vector3(3971.5f, 5226.391f, 224.5844f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(242, Generator.Constructor(Vector3(3978.387f, 5247.622f, 240.9345f)), owning_building_guid = 26)
      LocalObject(
        233,
        Terminal.Constructor(Vector3(3978.291f, 5239.43f, 242.2285f), gen_control),
        owning_building_guid = 26
      )
    }

    Building9()

    def Building9(): Unit = { // Name: Tootega Type: cryo_facility GUID: 29, MapID: 9
      LocalBuilding(
        "Tootega",
        29,
        9,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5106f, 3298f, 217.0609f),
            Vector3(0f, 0f, 271f),
            cryo_facility
          )
        )
      )
      LocalObject(
        182,
        CaptureTerminal.Constructor(Vector3(5164.767f, 3327.119f, 207.0609f), capture_terminal),
        owning_building_guid = 29
      )
      LocalObject(459, Door.Constructor(Vector3(5014.572f, 3264.399f, 218.5819f)), owning_building_guid = 29)
      LocalObject(460, Door.Constructor(Vector3(5023.178f, 3291.247f, 226.5759f)), owning_building_guid = 29)
      LocalObject(461, Door.Constructor(Vector3(5023.496f, 3273.056f, 218.6119f)), owning_building_guid = 29)
      LocalObject(466, Door.Constructor(Vector3(5056.204f, 3246.196f, 226.5759f)), owning_building_guid = 29)
      LocalObject(467, Door.Constructor(Vector3(5074.394f, 3246.514f, 218.6119f)), owning_building_guid = 29)
      LocalObject(468, Door.Constructor(Vector3(5109.207f, 3294.055f, 228.5819f)), owning_building_guid = 29)
      LocalObject(469, Door.Constructor(Vector3(5109.47f, 3357.047f, 218.6119f)), owning_building_guid = 29)
      LocalObject(471, Door.Constructor(Vector3(5125.718f, 3314.347f, 228.5819f)), owning_building_guid = 29)
      LocalObject(472, Door.Constructor(Vector3(5127.66f, 3357.364f, 226.5759f)), owning_building_guid = 29)
      LocalObject(479, Door.Constructor(Vector3(5173.054f, 3341.503f, 218.6119f)), owning_building_guid = 29)
      LocalObject(480, Door.Constructor(Vector3(5173.372f, 3323.313f, 226.5759f)), owning_building_guid = 29)
      LocalObject(669, Door.Constructor(Vector3(5017.804f, 3308.462f, 211.0819f)), owning_building_guid = 29)
      LocalObject(670, Door.Constructor(Vector3(5045.311f, 3336.947f, 211.0819f)), owning_building_guid = 29)
      LocalObject(671, Door.Constructor(Vector3(5049.66f, 3317.02f, 211.0819f)), owning_building_guid = 29)
      LocalObject(672, Door.Constructor(Vector3(5073.656f, 3317.438f, 208.5819f)), owning_building_guid = 29)
      LocalObject(673, Door.Constructor(Vector3(5078.004f, 3297.511f, 208.5819f)), owning_building_guid = 29)
      LocalObject(674, Door.Constructor(Vector3(5093.164f, 3345.783f, 208.5819f)), owning_building_guid = 29)
      LocalObject(675, Door.Constructor(Vector3(5098.071f, 3293.861f, 228.5819f)), owning_building_guid = 29)
      LocalObject(676, Door.Constructor(Vector3(5102f, 3297.93f, 208.5819f)), owning_building_guid = 29)
      LocalObject(677, Door.Constructor(Vector3(5105.372f, 3333.995f, 208.5819f)), owning_building_guid = 29)
      LocalObject(678, Door.Constructor(Vector3(5105.791f, 3309.998f, 208.5819f)), owning_building_guid = 29)
      LocalObject(679, Door.Constructor(Vector3(5106.209f, 3286.002f, 208.5819f)), owning_building_guid = 29)
      LocalObject(680, Door.Constructor(Vector3(5110.418f, 3274.073f, 201.0819f)), owning_building_guid = 29)
      LocalObject(681, Door.Constructor(Vector3(5125.438f, 3330.344f, 208.5819f)), owning_building_guid = 29)
      LocalObject(682, Door.Constructor(Vector3(5125.718f, 3314.347f, 208.5819f)), owning_building_guid = 29)
      LocalObject(683, Door.Constructor(Vector3(5125.718f, 3314.347f, 218.5819f)), owning_building_guid = 29)
      LocalObject(684, Door.Constructor(Vector3(5126.416f, 3274.353f, 208.5819f)), owning_building_guid = 29)
      LocalObject(685, Door.Constructor(Vector3(5130.206f, 3286.421f, 208.5819f)), owning_building_guid = 29)
      LocalObject(686, Door.Constructor(Vector3(5145.366f, 3334.693f, 208.5819f)), owning_building_guid = 29)
      LocalObject(687, Door.Constructor(Vector3(5145.785f, 3310.696f, 208.5819f)), owning_building_guid = 29)
      LocalObject(688, Door.Constructor(Vector3(5157.992f, 3298.907f, 208.5819f)), owning_building_guid = 29)
      LocalObject(689, Door.Constructor(Vector3(5173.431f, 3331.182f, 208.5819f)), owning_building_guid = 29)
      LocalObject(690, Door.Constructor(Vector3(5173.571f, 3323.183f, 208.5819f)), owning_building_guid = 29)
      LocalObject(691, Door.Constructor(Vector3(5173.71f, 3315.184f, 208.5819f)), owning_building_guid = 29)
      LocalObject(718, Door.Constructor(Vector3(5131.149f, 3294.446f, 219.3439f)), owning_building_guid = 29)
      LocalObject(724, Door.Constructor(Vector3(5106.209f, 3286.002f, 218.5819f)), owning_building_guid = 29)
      LocalObject(725, Door.Constructor(Vector3(5113.929f, 3302.139f, 218.5799f)), owning_building_guid = 29)
      LocalObject(2344, Door.Constructor(Vector3(5111.813f, 3293.428f, 208.9149f)), owning_building_guid = 29)
      LocalObject(2345, Door.Constructor(Vector3(5119.105f, 3293.555f, 208.9149f)), owning_building_guid = 29)
      LocalObject(2346, Door.Constructor(Vector3(5126.394f, 3293.682f, 208.9149f)), owning_building_guid = 29)
      LocalObject(
        754,
        IFFLock.Constructor(Vector3(5133.831f, 3297.716f, 218.5429f), Vector3(0, 0, 89)),
        owning_building_guid = 29,
        door_guid = 718
      )
      LocalObject(
        929,
        IFFLock.Constructor(Vector3(5016.628f, 3263.621f, 218.5129f), Vector3(0, 0, 179)),
        owning_building_guid = 29,
        door_guid = 459
      )
      LocalObject(
        934,
        IFFLock.Constructor(Vector3(5105.427f, 3284.416f, 208.3969f), Vector3(0, 0, 269)),
        owning_building_guid = 29,
        door_guid = 679
      )
      LocalObject(
        935,
        IFFLock.Constructor(Vector3(5108.833f, 3274.856f, 200.8969f), Vector3(0, 0, 359)),
        owning_building_guid = 29,
        door_guid = 680
      )
      LocalObject(
        936,
        IFFLock.Constructor(Vector3(5109.992f, 3296.115f, 228.5129f), Vector3(0, 0, 89)),
        owning_building_guid = 29,
        door_guid = 468
      )
      LocalObject(
        937,
        IFFLock.Constructor(Vector3(5127.774f, 3313.568f, 228.5129f), Vector3(0, 0, 179)),
        owning_building_guid = 29,
        door_guid = 471
      )
      LocalObject(
        941,
        IFFLock.Constructor(Vector3(5130.988f, 3288.007f, 208.3969f), Vector3(0, 0, 89)),
        owning_building_guid = 29,
        door_guid = 685
      )
      LocalObject(
        945,
        IFFLock.Constructor(Vector3(5171.843f, 3332.094f, 208.3969f), Vector3(0, 0, 359)),
        owning_building_guid = 29,
        door_guid = 689
      )
      LocalObject(
        946,
        IFFLock.Constructor(Vector3(5175.157f, 3322.398f, 208.3969f), Vector3(0, 0, 179)),
        owning_building_guid = 29,
        door_guid = 690
      )
      LocalObject(1265, Locker.Constructor(Vector3(5108.473f, 3279.017f, 207.3219f)), owning_building_guid = 29)
      LocalObject(1266, Locker.Constructor(Vector3(5108.453f, 3280.166f, 207.3219f)), owning_building_guid = 29)
      LocalObject(1267, Locker.Constructor(Vector3(5108.433f, 3281.313f, 207.3219f)), owning_building_guid = 29)
      LocalObject(1268, Locker.Constructor(Vector3(5108.412f, 3282.477f, 207.3219f)), owning_building_guid = 29)
      LocalObject(1269, Locker.Constructor(Vector3(5108.988f, 3262.064f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1270, Locker.Constructor(Vector3(5108.969f, 3263.118f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1271, Locker.Constructor(Vector3(5108.951f, 3264.173f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1272, Locker.Constructor(Vector3(5108.933f, 3265.229f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1273, Locker.Constructor(Vector3(5109.024f, 3259.949f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1274, Locker.Constructor(Vector3(5109.006f, 3261.004f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1275, Locker.Constructor(Vector3(5128.986f, 3262.412f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1276, Locker.Constructor(Vector3(5128.967f, 3263.467f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1277, Locker.Constructor(Vector3(5128.949f, 3264.527f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1278, Locker.Constructor(Vector3(5128.93f, 3265.581f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1279, Locker.Constructor(Vector3(5129.022f, 3260.301f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1280, Locker.Constructor(Vector3(5129.004f, 3261.357f, 206.9689f)), owning_building_guid = 29)
      LocalObject(1289, Locker.Constructor(Vector3(5155.837f, 3278.87f, 207.0559f)), owning_building_guid = 29)
      LocalObject(1290, Locker.Constructor(Vector3(5157.093f, 3278.892f, 207.0559f)), owning_building_guid = 29)
      LocalObject(1291, Locker.Constructor(Vector3(5158.354f, 3278.914f, 207.0559f)), owning_building_guid = 29)
      LocalObject(1292, Locker.Constructor(Vector3(5159.616f, 3278.936f, 207.0559f)), owning_building_guid = 29)
      LocalObject(1293, Locker.Constructor(Vector3(5160.868f, 3278.958f, 207.0559f)), owning_building_guid = 29)
      LocalObject(1354, Locker.Constructor(Vector3(5094.107f, 3279.53f, 217.0609f)), owning_building_guid = 29)
      LocalObject(1355, Locker.Constructor(Vector3(5095.142f, 3279.548f, 217.0609f)), owning_building_guid = 29)
      LocalObject(1356, Locker.Constructor(Vector3(5097.658f, 3279.592f, 216.8319f)), owning_building_guid = 29)
      LocalObject(1357, Locker.Constructor(Vector3(5098.692f, 3279.61f, 216.8319f)), owning_building_guid = 29)
      LocalObject(1358, Locker.Constructor(Vector3(5099.746f, 3279.628f, 216.8319f)), owning_building_guid = 29)
      LocalObject(1359, Locker.Constructor(Vector3(5100.779f, 3279.646f, 216.8319f)), owning_building_guid = 29)
      LocalObject(1360, Locker.Constructor(Vector3(5103.301f, 3279.69f, 217.0609f)), owning_building_guid = 29)
      LocalObject(1361, Locker.Constructor(Vector3(5104.335f, 3279.708f, 217.0609f)), owning_building_guid = 29)
      LocalObject(
        200,
        Terminal.Constructor(Vector3(5152.86f, 3295.093f, 207.0509f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        201,
        Terminal.Constructor(Vector3(5153.082f, 3282.395f, 207.0509f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        202,
        Terminal.Constructor(Vector3(5154.282f, 3296.566f, 207.0509f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        203,
        Terminal.Constructor(Vector3(5154.555f, 3280.973f, 207.0509f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        204,
        Terminal.Constructor(Vector3(5161.606f, 3296.694f, 207.0509f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        205,
        Terminal.Constructor(Vector3(5161.878f, 3281.101f, 207.0509f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        206,
        Terminal.Constructor(Vector3(5163.079f, 3295.272f, 207.0509f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        207,
        Terminal.Constructor(Vector3(5163.301f, 3282.574f, 207.0509f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1609,
        Terminal.Constructor(Vector3(5113.732f, 3279.478f, 208.6509f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1610,
        Terminal.Constructor(Vector3(5115.35f, 3308.193f, 218.3559f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1611,
        Terminal.Constructor(Vector3(5117.463f, 3279.543f, 208.6509f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1612,
        Terminal.Constructor(Vector3(5121.251f, 3279.609f, 208.6509f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2261,
        Terminal.Constructor(Vector3(5049.138f, 3313.1f, 211.1739f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2262,
        Terminal.Constructor(Vector3(5092.395f, 3295.857f, 218.6399f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2263,
        Terminal.Constructor(Vector3(5104.709f, 3337.984f, 208.6739f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2264,
        Terminal.Constructor(Vector3(5109.329f, 3293.086f, 209.1949f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2265,
        Terminal.Constructor(Vector3(5116.62f, 3293.218f, 209.1949f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2266,
        Terminal.Constructor(Vector3(5123.907f, 3293.342f, 209.1949f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2267,
        Terminal.Constructor(Vector3(5169.98f, 3299.71f, 208.6739f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2421,
        Terminal.Constructor(Vector3(5043.48f, 3259.275f, 219.3659f), vehicle_terminal_combined),
        owning_building_guid = 29
      )
      LocalObject(
        1481,
        VehicleSpawnPad.Constructor(Vector3(5043.152f, 3272.91f, 215.2079f), mb_pad_creation, Vector3(0, 0, -1)),
        owning_building_guid = 29,
        terminal_guid = 2421
      )
      LocalObject(2096, ResourceSilo.Constructor(Vector3(5155.792f, 3359.145f, 224.0779f)), owning_building_guid = 29)
      LocalObject(
        2167,
        SpawnTube.Constructor(Vector3(5110.756f, 3293.849f, 207.0609f), Vector3(0, 0, 89)),
        owning_building_guid = 29
      )
      LocalObject(
        2168,
        SpawnTube.Constructor(Vector3(5118.046f, 3293.977f, 207.0609f), Vector3(0, 0, 89)),
        owning_building_guid = 29
      )
      LocalObject(
        2169,
        SpawnTube.Constructor(Vector3(5125.333f, 3294.104f, 207.0609f), Vector3(0, 0, 89)),
        owning_building_guid = 29
      )
      LocalObject(
        139,
        ProximityTerminal.Constructor(Vector3(5098.928f, 3295.893f, 216.8709f), adv_med_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1497,
        ProximityTerminal.Constructor(Vector3(5110.434f, 3270.431f, 207.0609f), medical_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1780,
        ProximityTerminal.Constructor(Vector3(5036.756f, 3298.594f, 225.4129f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1781,
        Terminal.Constructor(Vector3(5036.756f, 3298.594f, 225.4129f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1783,
        ProximityTerminal.Constructor(Vector3(5043.754f, 3315.033f, 227.3539f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1784,
        Terminal.Constructor(Vector3(5043.754f, 3315.033f, 227.3539f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1786,
        ProximityTerminal.Constructor(Vector3(5151.269f, 3297.467f, 227.3929f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1787,
        Terminal.Constructor(Vector3(5151.269f, 3297.467f, 227.3929f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1789,
        ProximityTerminal.Constructor(Vector3(5159.383f, 3313.833f, 225.4029f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1790,
        Terminal.Constructor(Vector3(5159.383f, 3313.833f, 225.4029f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2073,
        ProximityTerminal.Constructor(Vector3(5020.548f, 3331.989f, 216.8109f), repair_silo),
        owning_building_guid = 29
      )
      LocalObject(
        2074,
        Terminal.Constructor(Vector3(5020.548f, 3331.989f, 216.8109f), ground_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2077,
        ProximityTerminal.Constructor(Vector3(5122.775f, 3245.755f, 216.8109f), repair_silo),
        owning_building_guid = 29
      )
      LocalObject(
        2078,
        Terminal.Constructor(Vector3(5122.775f, 3245.755f, 216.8109f), ground_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1449,
        FacilityTurret.Constructor(Vector3(5008.144f, 3367.903f, 225.4629f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1449, 5030)
      LocalObject(
        1450,
        FacilityTurret.Constructor(Vector3(5010.496f, 3232.697f, 225.4629f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1450, 5031)
      LocalObject(
        1453,
        FacilityTurret.Constructor(Vector3(5145.621f, 3233.869f, 225.4629f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1453, 5032)
      LocalObject(
        1454,
        FacilityTurret.Constructor(Vector3(5185.21f, 3371.001f, 225.4629f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1454, 5033)
      LocalObject(
        1455,
        FacilityTurret.Constructor(Vector3(5187.971f, 3277.762f, 225.4629f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1455, 5034)
      LocalObject(
        736,
        ImplantTerminalMech.Constructor(Vector3(5150.537f, 3288.71f, 206.5379f)),
        owning_building_guid = 29
      )
      LocalObject(
        730,
        Terminal.Constructor(Vector3(5150.555f, 3288.71f, 206.5379f), implant_terminal_interface),
        owning_building_guid = 29
      )
      TerminalToInterface(736, 730)
      LocalObject(
        737,
        ImplantTerminalMech.Constructor(Vector3(5165.89f, 3288.99f, 206.5379f)),
        owning_building_guid = 29
      )
      LocalObject(
        731,
        Terminal.Constructor(Vector3(5165.873f, 3288.99f, 206.5379f), implant_terminal_interface),
        owning_building_guid = 29
      )
      TerminalToInterface(737, 731)
      LocalObject(
        1883,
        Painbox.Constructor(Vector3(5086.435f, 3292.064f, 231.0897f), painbox),
        owning_building_guid = 29
      )
      LocalObject(
        1892,
        Painbox.Constructor(Vector3(5113.968f, 3283.384f, 211.1308f), painbox_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        1901,
        Painbox.Constructor(Vector3(5100.872f, 3293.728f, 231.2948f), painbox_door_radius),
        owning_building_guid = 29
      )
      LocalObject(
        1924,
        Painbox.Constructor(Vector3(5103.978f, 3287.423f, 209.4168f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        1925,
        Painbox.Constructor(Vector3(5125.534f, 3270.455f, 210.6018f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        1926,
        Painbox.Constructor(Vector3(5132.045f, 3285.937f, 208.7751f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(244, Generator.Constructor(Vector3(5082.519f, 3293.564f, 225.7669f)), owning_building_guid = 29)
      LocalObject(
        235,
        Terminal.Constructor(Vector3(5090.709f, 3293.754f, 227.0609f), gen_control),
        owning_building_guid = 29
      )
    }

    Building18657()

    def Building18657(): Unit = { // Name: GW_Ceryshen_S Type: hst GUID: 33, MapID: 18657
      LocalBuilding(
        "GW_Ceryshen_S",
        33,
        18657,
        FoundationBuilder(WarpGate.Structure(Vector3(2248.14f, 1712.18f, 224.5f), hst))
      )
    }

    Building18658()

    def Building18658(): Unit = { // Name: GW_Ceryshen_N Type: hst GUID: 34, MapID: 18658
      LocalBuilding(
        "GW_Ceryshen_N",
        34,
        18658,
        FoundationBuilder(WarpGate.Structure(Vector3(3175.87f, 5324.2f, 232.33f), hst))
      )
    }

    Building3()

    def Building3(): Unit = { // Name: Igaluk Type: tech_plant GUID: 36, MapID: 3
      LocalBuilding(
        "Igaluk",
        36,
        3,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3320f, 5756f, 234.4307f),
            Vector3(0f, 0f, 269f),
            tech_plant
          )
        )
      )
      LocalObject(
        176,
        CaptureTerminal.Constructor(Vector3(3275.835f, 5752.036f, 249.5307f), capture_terminal),
        owning_building_guid = 36
      )
      LocalObject(299, Door.Constructor(Vector3(3220.713f, 5797.739f, 236.0517f)), owning_building_guid = 36)
      LocalObject(300, Door.Constructor(Vector3(3228.736f, 5752.987f, 236.0817f)), owning_building_guid = 36)
      LocalObject(301, Door.Constructor(Vector3(3229.053f, 5771.178f, 244.0447f)), owning_building_guid = 36)
      LocalObject(302, Door.Constructor(Vector3(3251.187f, 5828.672f, 235.9727f)), owning_building_guid = 36)
      LocalObject(303, Door.Constructor(Vector3(3269.376f, 5828.354f, 243.9357f)), owning_building_guid = 36)
      LocalObject(304, Door.Constructor(Vector3(3280.276f, 5739.247f, 251.0517f)), owning_building_guid = 36)
      LocalObject(311, Door.Constructor(Vector3(3296.394f, 5745.813f, 251.0517f)), owning_building_guid = 36)
      LocalObject(312, Door.Constructor(Vector3(3303.537f, 5677.255f, 243.9357f)), owning_building_guid = 36)
      LocalObject(313, Door.Constructor(Vector3(3321.728f, 5676.938f, 235.9727f)), owning_building_guid = 36)
      LocalObject(314, Door.Constructor(Vector3(3324.92f, 5827.385f, 235.9727f)), owning_building_guid = 36)
      LocalObject(315, Door.Constructor(Vector3(3343.109f, 5827.067f, 243.9357f)), owning_building_guid = 36)
      LocalObject(316, Door.Constructor(Vector3(3350.515f, 5712.709f, 243.9357f)), owning_building_guid = 36)
      LocalObject(317, Door.Constructor(Vector3(3350.833f, 5730.899f, 235.9727f)), owning_building_guid = 36)
      LocalObject(512, Door.Constructor(Vector3(3360.834f, 5803.294f, 238.1677f)), owning_building_guid = 36)
      LocalObject(515, Door.Constructor(Vector3(3304.84f, 5804.272f, 218.1677f)), owning_building_guid = 36)
      LocalObject(541, Door.Constructor(Vector3(3228.293f, 5773.603f, 228.5517f)), owning_building_guid = 36)
      LocalObject(542, Door.Constructor(Vector3(3244.011f, 5757.326f, 228.5517f)), owning_building_guid = 36)
      LocalObject(543, Door.Constructor(Vector3(3253.127f, 5821.177f, 228.5517f)), owning_building_guid = 36)
      LocalObject(544, Door.Constructor(Vector3(3268.287f, 5772.905f, 228.5517f)), owning_building_guid = 36)
      LocalObject(545, Door.Constructor(Vector3(3271.658f, 5736.841f, 221.0517f)), owning_building_guid = 36)
      LocalObject(546, Door.Constructor(Vector3(3271.658f, 5736.841f, 228.5517f)), owning_building_guid = 36)
      LocalObject(547, Door.Constructor(Vector3(3275.448f, 5724.773f, 221.0517f)), owning_building_guid = 36)
      LocalObject(548, Door.Constructor(Vector3(3276.565f, 5788.763f, 226.0517f)), owning_building_guid = 36)
      LocalObject(549, Door.Constructor(Vector3(3277.124f, 5820.758f, 226.0517f)), owning_building_guid = 36)
      LocalObject(550, Door.Constructor(Vector3(3280.076f, 5760.697f, 231.0517f)), owning_building_guid = 36)
      LocalObject(551, Door.Constructor(Vector3(3280.076f, 5760.697f, 251.0517f)), owning_building_guid = 36)
      LocalObject(552, Door.Constructor(Vector3(3284.005f, 5756.628f, 221.0517f)), owning_building_guid = 36)
      LocalObject(553, Door.Constructor(Vector3(3288.005f, 5756.559f, 241.0517f)), owning_building_guid = 36)
      LocalObject(554, Door.Constructor(Vector3(3291.446f, 5724.494f, 228.5517f)), owning_building_guid = 36)
      LocalObject(555, Door.Constructor(Vector3(3292.004f, 5756.489f, 251.0517f)), owning_building_guid = 36)
      LocalObject(556, Door.Constructor(Vector3(3295.655f, 5736.422f, 228.5517f)), owning_building_guid = 36)
      LocalObject(557, Door.Constructor(Vector3(3296.073f, 5760.418f, 231.0517f)), owning_building_guid = 36)
      LocalObject(558, Door.Constructor(Vector3(3300.562f, 5788.344f, 226.0517f)), owning_building_guid = 36)
      LocalObject(559, Door.Constructor(Vector3(3303.514f, 5728.284f, 221.0517f)), owning_building_guid = 36)
      LocalObject(712, Door.Constructor(Vector3(3273.646f, 5716.59f, 236.8107f)), owning_building_guid = 36)
      LocalObject(2296, Door.Constructor(Vector3(3277.518f, 5744.066f, 228.8847f)), owning_building_guid = 36)
      LocalObject(2297, Door.Constructor(Vector3(3284.81f, 5743.939f, 228.8847f)), owning_building_guid = 36)
      LocalObject(2298, Door.Constructor(Vector3(3292.098f, 5743.812f, 228.8847f)), owning_building_guid = 36)
      LocalObject(
        748,
        IFFLock.Constructor(Vector3(3270.854f, 5713.494f, 236.0107f), Vector3(0, 0, 271)),
        owning_building_guid = 36,
        door_guid = 712
      )
      LocalObject(
        756,
        IFFLock.Constructor(Vector3(3363.093f, 5797.999f, 236.1187f), Vector3(0, 0, 91)),
        owning_building_guid = 36,
        door_guid = 512
      )
      LocalObject(
        803,
        IFFLock.Constructor(Vector3(3218.682f, 5798.589f, 235.9827f), Vector3(0, 0, 1)),
        owning_building_guid = 36,
        door_guid = 299
      )
      LocalObject(
        806,
        IFFLock.Constructor(Vector3(3270.821f, 5735.283f, 228.3667f), Vector3(0, 0, 271)),
        owning_building_guid = 36,
        door_guid = 546
      )
      LocalObject(
        807,
        IFFLock.Constructor(Vector3(3273.893f, 5725.74f, 220.8667f), Vector3(0, 0, 1)),
        owning_building_guid = 36,
        door_guid = 547
      )
      LocalObject(
        808,
        IFFLock.Constructor(Vector3(3279.441f, 5737.209f, 250.9767f), Vector3(0, 0, 271)),
        owning_building_guid = 36,
        door_guid = 304
      )
      LocalObject(
        809,
        IFFLock.Constructor(Vector3(3281.043f, 5762.253f, 250.8667f), Vector3(0, 0, 91)),
        owning_building_guid = 36,
        door_guid = 551
      )
      LocalObject(
        816,
        IFFLock.Constructor(Vector3(3296.492f, 5737.979f, 228.3667f), Vector3(0, 0, 91)),
        owning_building_guid = 36,
        door_guid = 556
      )
      LocalObject(
        817,
        IFFLock.Constructor(Vector3(3297.237f, 5747.842f, 250.9767f), Vector3(0, 0, 91)),
        owning_building_guid = 36,
        door_guid = 311
      )
      LocalObject(
        818,
        IFFLock.Constructor(Vector3(3302.543f, 5726.728f, 220.8667f), Vector3(0, 0, 271)),
        owning_building_guid = 36,
        door_guid = 559
      )
      LocalObject(1064, Locker.Constructor(Vector3(3273.676f, 5729.781f, 227.2917f)), owning_building_guid = 36)
      LocalObject(1065, Locker.Constructor(Vector3(3273.697f, 5730.93f, 227.2917f)), owning_building_guid = 36)
      LocalObject(1066, Locker.Constructor(Vector3(3273.717f, 5732.077f, 227.2917f)), owning_building_guid = 36)
      LocalObject(1067, Locker.Constructor(Vector3(3273.737f, 5733.241f, 227.2917f)), owning_building_guid = 36)
      LocalObject(1068, Locker.Constructor(Vector3(3293.354f, 5709.73f, 219.5307f)), owning_building_guid = 36)
      LocalObject(1069, Locker.Constructor(Vector3(3293.377f, 5711.067f, 219.5307f)), owning_building_guid = 36)
      LocalObject(1070, Locker.Constructor(Vector3(3293.4f, 5712.403f, 219.5307f)), owning_building_guid = 36)
      LocalObject(1071, Locker.Constructor(Vector3(3293.423f, 5713.727f, 219.5307f)), owning_building_guid = 36)
      LocalObject(1072, Locker.Constructor(Vector3(3293.502f, 5718.266f, 219.5307f)), owning_building_guid = 36)
      LocalObject(1073, Locker.Constructor(Vector3(3293.526f, 5719.603f, 219.5307f)), owning_building_guid = 36)
      LocalObject(1074, Locker.Constructor(Vector3(3293.549f, 5720.938f, 219.5307f)), owning_building_guid = 36)
      LocalObject(1075, Locker.Constructor(Vector3(3293.572f, 5722.262f, 219.5307f)), owning_building_guid = 36)
      LocalObject(
        140,
        Terminal.Constructor(Vector3(3294.855f, 5739.832f, 250.1337f), air_vehicle_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1472,
        VehicleSpawnPad.Constructor(Vector3(3315.468f, 5734.988f, 247.0087f), mb_pad_creation, Vector3(0, 0, 91)),
        owning_building_guid = 36,
        terminal_guid = 140
      )
      LocalObject(
        141,
        Terminal.Constructor(Vector3(3295.063f, 5751.762f, 250.1337f), air_vehicle_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1473,
        VehicleSpawnPad.Constructor(Vector3(3315.834f, 5756.003f, 247.0087f), mb_pad_creation, Vector3(0, 0, 91)),
        owning_building_guid = 36,
        terminal_guid = 141
      )
      LocalObject(
        1531,
        Terminal.Constructor(Vector3(3278.949f, 5730.059f, 228.6207f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1532,
        Terminal.Constructor(Vector3(3282.679f, 5729.993f, 228.6207f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1533,
        Terminal.Constructor(Vector3(3286.468f, 5729.927f, 228.6207f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1534,
        Terminal.Constructor(Vector3(3293.437f, 5753.405f, 240.8607f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2219,
        Terminal.Constructor(Vector3(3225.025f, 5781.719f, 228.5877f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2220,
        Terminal.Constructor(Vector3(3272.46f, 5748.887f, 221.0877f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2221,
        Terminal.Constructor(Vector3(3275.023f, 5743.812f, 229.1647f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2222,
        Terminal.Constructor(Vector3(3282.314f, 5743.689f, 229.1647f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2223,
        Terminal.Constructor(Vector3(3289.601f, 5743.559f, 229.1647f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2224,
        Terminal.Constructor(Vector3(3298.992f, 5719.119f, 241.1127f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2225,
        Terminal.Constructor(Vector3(3322.961f, 5741.414f, 246.4537f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2415,
        Terminal.Constructor(Vector3(3280.267f, 5804.705f, 220.2447f), ground_vehicle_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1471,
        VehicleSpawnPad.Constructor(Vector3(3291.182f, 5804.565f, 211.9677f), mb_pad_creation, Vector3(0, 0, 91)),
        owning_building_guid = 36,
        terminal_guid = 2415
      )
      LocalObject(2090, ResourceSilo.Constructor(Vector3(3351.472f, 5693.689f, 241.4387f)), owning_building_guid = 36)
      LocalObject(
        2119,
        SpawnTube.Constructor(Vector3(3276.476f, 5744.525f, 227.0307f), Vector3(0, 0, 91)),
        owning_building_guid = 36
      )
      LocalObject(
        2120,
        SpawnTube.Constructor(Vector3(3283.766f, 5744.397f, 227.0307f), Vector3(0, 0, 91)),
        owning_building_guid = 36
      )
      LocalObject(
        2121,
        SpawnTube.Constructor(Vector3(3291.053f, 5744.271f, 227.0307f), Vector3(0, 0, 91)),
        owning_building_guid = 36
      )
      LocalObject(
        1486,
        ProximityTerminal.Constructor(Vector3(3282.853f, 5753.589f, 239.5277f), medical_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1487,
        ProximityTerminal.Constructor(Vector3(3292.918f, 5716.022f, 219.5307f), medical_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1705,
        ProximityTerminal.Constructor(Vector3(3238.195f, 5731.89f, 242.6387f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        1706,
        Terminal.Constructor(Vector3(3238.195f, 5731.89f, 242.6387f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1708,
        ProximityTerminal.Constructor(Vector3(3255.32f, 5747.749f, 249.8777f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        1709,
        Terminal.Constructor(Vector3(3255.32f, 5747.749f, 249.8777f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1711,
        ProximityTerminal.Constructor(Vector3(3276.78f, 5694.758f, 245.0827f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        1712,
        Terminal.Constructor(Vector3(3276.78f, 5694.758f, 245.0827f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1714,
        ProximityTerminal.Constructor(Vector3(3292.914f, 5688.183f, 242.6387f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        1715,
        Terminal.Constructor(Vector3(3292.914f, 5688.183f, 242.6387f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1717,
        ProximityTerminal.Constructor(Vector3(3296.734f, 5817.711f, 242.6257f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        1718,
        Terminal.Constructor(Vector3(3296.734f, 5817.711f, 242.6257f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1720,
        ProximityTerminal.Constructor(Vector3(3312.55f, 5797.156f, 244.9807f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        1721,
        Terminal.Constructor(Vector3(3312.55f, 5797.156f, 244.9807f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2025,
        ProximityTerminal.Constructor(Vector3(3226.198f, 5698.992f, 234.1807f), repair_silo),
        owning_building_guid = 36
      )
      LocalObject(
        2026,
        Terminal.Constructor(Vector3(3226.198f, 5698.992f, 234.1807f), ground_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2029,
        ProximityTerminal.Constructor(Vector3(3361.625f, 5754.965f, 234.1592f), repair_silo),
        owning_building_guid = 36
      )
      LocalObject(
        2030,
        Terminal.Constructor(Vector3(3361.625f, 5754.965f, 234.1592f), ground_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1391,
        FacilityTurret.Constructor(Vector3(3219.168f, 5671.593f, 242.9297f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1391, 5035)
      LocalObject(
        1392,
        FacilityTurret.Constructor(Vector3(3222.052f, 5836.309f, 242.9297f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1392, 5036)
      LocalObject(
        1394,
        FacilityTurret.Constructor(Vector3(3283.79f, 5662.737f, 242.9297f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1394, 5037)
      LocalObject(
        1395,
        FacilityTurret.Constructor(Vector3(3356.889f, 5669.189f, 242.9297f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1395, 5038)
      LocalObject(
        1398,
        FacilityTurret.Constructor(Vector3(3418.742f, 5748.675f, 242.9297f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1398, 5039)
      LocalObject(
        1399,
        FacilityTurret.Constructor(Vector3(3420.308f, 5838.356f, 242.9297f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1399, 5040)
      LocalObject(
        1877,
        Painbox.Constructor(Vector3(3315.758f, 5730.333f, 223.004f), painbox),
        owning_building_guid = 36
      )
      LocalObject(
        1886,
        Painbox.Constructor(Vector3(3282.854f, 5735.813f, 231.3006f), painbox_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        1895,
        Painbox.Constructor(Vector3(3300.991f, 5728.628f, 222.6901f), painbox_door_radius),
        owning_building_guid = 36
      )
      LocalObject(
        1906,
        Painbox.Constructor(Vector3(3269.954f, 5737.836f, 229.1586f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        1907,
        Painbox.Constructor(Vector3(3289.952f, 5720.878f, 230.613f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        1908,
        Painbox.Constructor(Vector3(3297.426f, 5736.53f, 229.7069f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(238, Generator.Constructor(Vector3(3319.067f, 5728.037f, 218.2367f)), owning_building_guid = 36)
      LocalObject(
        229,
        Terminal.Constructor(Vector3(3310.875f, 5728.133f, 219.5307f), gen_control),
        owning_building_guid = 36
      )
    }

    Building5()

    def Building5(): Unit = { // Name: Nerrivik Type: tech_plant GUID: 39, MapID: 5
      LocalBuilding(
        "Nerrivik",
        39,
        5,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3642f, 3710f, 222.4253f),
            Vector3(0f, 0f, 223f),
            tech_plant
          )
        )
      )
      LocalObject(
        177,
        CaptureTerminal.Constructor(Vector3(3608.469f, 3739.016f, 237.5253f), capture_terminal),
        owning_building_guid = 39
      )
      LocalObject(327, Door.Constructor(Vector3(3573.92f, 3667.142f, 231.9303f)), owning_building_guid = 39)
      LocalObject(328, Door.Constructor(Vector3(3576.435f, 3773.557f, 224.0763f)), owning_building_guid = 39)
      LocalObject(329, Door.Constructor(Vector3(3586.327f, 3653.836f, 223.9673f)), owning_building_guid = 39)
      LocalObject(330, Door.Constructor(Vector3(3589.741f, 3785.965f, 232.0393f)), owning_building_guid = 39)
      LocalObject(331, Door.Constructor(Vector3(3602.354f, 3726.938f, 239.0463f)), owning_building_guid = 39)
      LocalObject(332, Door.Constructor(Vector3(3603.054f, 3810.415f, 224.0463f)), owning_building_guid = 39)
      LocalObject(334, Door.Constructor(Vector3(3618.274f, 3719.905f, 239.0463f)), owning_building_guid = 39)
      LocalObject(335, Door.Constructor(Vector3(3632.057f, 3657.977f, 231.9303f)), owning_building_guid = 39)
      LocalObject(338, Door.Constructor(Vector3(3645.362f, 3670.384f, 223.9673f)), owning_building_guid = 39)
      LocalObject(339, Door.Constructor(Vector3(3646.474f, 3809.982f, 223.9673f)), owning_building_guid = 39)
      LocalObject(345, Door.Constructor(Vector3(3658.881f, 3796.677f, 231.9303f)), owning_building_guid = 39)
      LocalObject(350, Door.Constructor(Vector3(3696.768f, 3756.049f, 223.9673f)), owning_building_guid = 39)
      LocalObject(353, Door.Constructor(Vector3(3709.174f, 3742.745f, 231.9303f)), owning_building_guid = 39)
      LocalObject(513, Door.Constructor(Vector3(3704.386f, 3713.48f, 226.1623f)), owning_building_guid = 39)
      LocalObject(516, Door.Constructor(Vector3(3666.193f, 3754.438f, 206.1623f)), owning_building_guid = 39)
      LocalObject(560, Door.Constructor(Vector3(3588.589f, 3720.356f, 209.0463f)), owning_building_guid = 39)
      LocalObject(561, Door.Constructor(Vector3(3590.168f, 3765.583f, 216.5463f)), owning_building_guid = 39)
      LocalObject(562, Door.Constructor(Vector3(3590.958f, 3788.197f, 216.5463f)), owning_building_guid = 39)
      LocalObject(563, Door.Constructor(Vector3(3594.637f, 3731.465f, 209.0463f)), owning_building_guid = 39)
      LocalObject(564, Door.Constructor(Vector3(3594.637f, 3731.465f, 216.5463f)), owning_building_guid = 39)
      LocalObject(565, Door.Constructor(Vector3(3599.501f, 3708.654f, 216.5463f)), owning_building_guid = 39)
      LocalObject(566, Door.Constructor(Vector3(3610.61f, 3702.606f, 209.0463f)), owning_building_guid = 39)
      LocalObject(567, Door.Constructor(Vector3(3611.005f, 3713.913f, 216.5463f)), owning_building_guid = 39)
      LocalObject(568, Door.Constructor(Vector3(3617.448f, 3736.329f, 209.0463f)), owning_building_guid = 39)
      LocalObject(569, Door.Constructor(Vector3(3617.646f, 3741.982f, 219.0463f)), owning_building_guid = 39)
      LocalObject(570, Door.Constructor(Vector3(3617.646f, 3741.982f, 239.0463f)), owning_building_guid = 39)
      LocalObject(571, Door.Constructor(Vector3(3618.238f, 3758.942f, 216.5463f)), owning_building_guid = 39)
      LocalObject(572, Door.Constructor(Vector3(3620.176f, 3733.403f, 229.0463f)), owning_building_guid = 39)
      LocalObject(573, Door.Constructor(Vector3(3622.904f, 3730.478f, 239.0463f)), owning_building_guid = 39)
      LocalObject(574, Door.Constructor(Vector3(3628.557f, 3730.281f, 219.0463f)), owning_building_guid = 39)
      LocalObject(575, Door.Constructor(Vector3(3635.396f, 3764.003f, 214.0463f)), owning_building_guid = 39)
      LocalObject(576, Door.Constructor(Vector3(3642.431f, 3803.38f, 216.5463f)), owning_building_guid = 39)
      LocalObject(577, Door.Constructor(Vector3(3651.763f, 3746.451f, 214.0463f)), owning_building_guid = 39)
      LocalObject(578, Door.Constructor(Vector3(3658.799f, 3785.827f, 214.0463f)), owning_building_guid = 39)
      LocalObject(713, Door.Constructor(Vector3(3581.451f, 3715.968f, 224.8053f)), owning_building_guid = 39)
      LocalObject(2305, Door.Constructor(Vector3(3603.906f, 3732.269f, 216.8793f)), owning_building_guid = 39)
      LocalObject(2306, Door.Constructor(Vector3(3608.879f, 3726.935f, 216.8793f)), owning_building_guid = 39)
      LocalObject(2307, Door.Constructor(Vector3(3613.85f, 3721.604f, 216.8793f)), owning_building_guid = 39)
      LocalObject(
        749,
        IFFLock.Constructor(Vector3(3577.284f, 3715.826f, 224.0053f), Vector3(0, 0, 317)),
        owning_building_guid = 39,
        door_guid = 713
      )
      LocalObject(
        757,
        IFFLock.Constructor(Vector3(3702.146f, 3708.176f, 224.1133f), Vector3(0, 0, 137)),
        owning_building_guid = 39,
        door_guid = 513
      )
      LocalObject(
        827,
        IFFLock.Constructor(Vector3(3588.204f, 3722.146f, 208.8613f), Vector3(0, 0, 47)),
        owning_building_guid = 39,
        door_guid = 560
      )
      LocalObject(
        828,
        IFFLock.Constructor(Vector3(3592.935f, 3730.985f, 216.3613f), Vector3(0, 0, 317)),
        owning_building_guid = 39,
        door_guid = 564
      )
      LocalObject(
        829,
        IFFLock.Constructor(Vector3(3600.308f, 3726.123f, 238.9713f), Vector3(0, 0, 317)),
        owning_building_guid = 39,
        door_guid = 331
      )
      LocalObject(
        830,
        IFFLock.Constructor(Vector3(3602.254f, 3812.467f, 223.9773f), Vector3(0, 0, 47)),
        owning_building_guid = 39,
        door_guid = 332
      )
      LocalObject(
        831,
        IFFLock.Constructor(Vector3(3608.817f, 3702.223f, 208.8613f), Vector3(0, 0, 317)),
        owning_building_guid = 39,
        door_guid = 566
      )
      LocalObject(
        832,
        IFFLock.Constructor(Vector3(3612.707f, 3714.392f, 216.3613f), Vector3(0, 0, 137)),
        owning_building_guid = 39,
        door_guid = 567
      )
      LocalObject(
        833,
        IFFLock.Constructor(Vector3(3619.436f, 3742.367f, 238.8613f), Vector3(0, 0, 137)),
        owning_building_guid = 39,
        door_guid = 570
      )
      LocalObject(
        834,
        IFFLock.Constructor(Vector3(3620.319f, 3720.707f, 238.9713f), Vector3(0, 0, 137)),
        owning_building_guid = 39,
        door_guid = 334
      )
      LocalObject(1100, Locker.Constructor(Vector3(3590.206f, 3697.026f, 207.5253f)), owning_building_guid = 39)
      LocalObject(1101, Locker.Constructor(Vector3(3590.961f, 3725.11f, 215.2863f)), owning_building_guid = 39)
      LocalObject(1102, Locker.Constructor(Vector3(3591.184f, 3697.938f, 207.5253f)), owning_building_guid = 39)
      LocalObject(1103, Locker.Constructor(Vector3(3591.801f, 3725.893f, 215.2863f)), owning_building_guid = 39)
      LocalObject(1104, Locker.Constructor(Vector3(3592.161f, 3698.849f, 207.5253f)), owning_building_guid = 39)
      LocalObject(1105, Locker.Constructor(Vector3(3592.64f, 3726.675f, 215.2863f)), owning_building_guid = 39)
      LocalObject(1106, Locker.Constructor(Vector3(3593.129f, 3699.752f, 207.5253f)), owning_building_guid = 39)
      LocalObject(1107, Locker.Constructor(Vector3(3593.491f, 3727.469f, 215.2863f)), owning_building_guid = 39)
      LocalObject(1108, Locker.Constructor(Vector3(3596.449f, 3702.848f, 207.5253f)), owning_building_guid = 39)
      LocalObject(1109, Locker.Constructor(Vector3(3597.427f, 3703.76f, 207.5253f)), owning_building_guid = 39)
      LocalObject(1110, Locker.Constructor(Vector3(3598.404f, 3704.671f, 207.5253f)), owning_building_guid = 39)
      LocalObject(1111, Locker.Constructor(Vector3(3599.373f, 3705.574f, 207.5253f)), owning_building_guid = 39)
      LocalObject(
        142,
        Terminal.Constructor(Vector3(3612.902f, 3716.856f, 238.1283f), air_vehicle_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1474,
        VehicleSpawnPad.Constructor(Vector3(3623.737f, 3698.664f, 235.0033f), mb_pad_creation, Vector3(0, 0, 137)),
        owning_building_guid = 39,
        terminal_guid = 142
      )
      LocalObject(
        143,
        Terminal.Constructor(Vector3(3621.629f, 3724.994f, 238.1283f), air_vehicle_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1475,
        VehicleSpawnPad.Constructor(Vector3(3639.108f, 3712.998f, 235.0033f), mb_pad_creation, Vector3(0, 0, 137)),
        owning_building_guid = 39,
        terminal_guid = 143
      )
      LocalObject(
        1544,
        Terminal.Constructor(Vector3(3594.823f, 3721.509f, 216.6153f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1545,
        Terminal.Constructor(Vector3(3597.367f, 3718.781f, 216.6153f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1546,
        Terminal.Constructor(Vector3(3599.951f, 3716.009f, 216.6153f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1547,
        Terminal.Constructor(Vector3(3621.681f, 3727.306f, 228.8553f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2226,
        Terminal.Constructor(Vector3(3594.526f, 3796.185f, 216.5823f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2227,
        Terminal.Constructor(Vector3(3600.877f, 3699.492f, 229.1073f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2228,
        Terminal.Constructor(Vector3(3601.99f, 3733.887f, 217.1593f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2229,
        Terminal.Constructor(Vector3(3603.859f, 3739.256f, 209.0823f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2230,
        Terminal.Constructor(Vector3(3606.966f, 3728.557f, 217.1593f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2231,
        Terminal.Constructor(Vector3(3611.934f, 3723.225f, 217.1593f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2232,
        Terminal.Constructor(Vector3(3633.565f, 3697.738f, 234.4483f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2416,
        Terminal.Constructor(Vector3(3649.435f, 3772.415f, 208.2393f), ground_vehicle_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1476,
        VehicleSpawnPad.Constructor(Vector3(3656.917f, 3764.466f, 199.9623f), mb_pad_creation, Vector3(0, 0, 137)),
        owning_building_guid = 39,
        terminal_guid = 2416
      )
      LocalObject(2091, ResourceSilo.Constructor(Vector3(3619.04f, 3644.076f, 229.4333f)), owning_building_guid = 39)
      LocalObject(
        2128,
        SpawnTube.Constructor(Vector3(3603.511f, 3733.337f, 215.0253f), Vector3(0, 0, 137)),
        owning_building_guid = 39
      )
      LocalObject(
        2129,
        SpawnTube.Constructor(Vector3(3608.484f, 3728.005f, 215.0253f), Vector3(0, 0, 137)),
        owning_building_guid = 39
      )
      LocalObject(
        2130,
        SpawnTube.Constructor(Vector3(3613.454f, 3722.675f, 215.0253f), Vector3(0, 0, 137)),
        owning_building_guid = 39
      )
      LocalObject(
        1488,
        ProximityTerminal.Constructor(Vector3(3594.43f, 3701.71f, 207.5253f), medical_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1489,
        ProximityTerminal.Constructor(Vector3(3614.461f, 3735.046f, 227.5223f), medical_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1723,
        ProximityTerminal.Constructor(Vector3(3567.83f, 3752.098f, 230.6333f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1724,
        Terminal.Constructor(Vector3(3567.83f, 3752.098f, 230.6333f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1726,
        ProximityTerminal.Constructor(Vector3(3567.923f, 3698.548f, 233.0773f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1727,
        Terminal.Constructor(Vector3(3567.923f, 3698.548f, 233.0773f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1729,
        ProximityTerminal.Constructor(Vector3(3574.401f, 3682.374f, 230.6333f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1730,
        Terminal.Constructor(Vector3(3574.401f, 3682.374f, 230.6333f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1732,
        ProximityTerminal.Constructor(Vector3(3591.134f, 3750.795f, 237.8723f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1733,
        Terminal.Constructor(Vector3(3591.134f, 3750.795f, 237.8723f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1735,
        ProximityTerminal.Constructor(Vector3(3666.43f, 3743.948f, 232.9753f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1736,
        Terminal.Constructor(Vector3(3666.43f, 3743.948f, 232.9753f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1738,
        ProximityTerminal.Constructor(Vector3(3670.23f, 3769.604f, 230.6203f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1739,
        Terminal.Constructor(Vector3(3670.23f, 3769.604f, 230.6203f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2033,
        ProximityTerminal.Constructor(Vector3(3535.832f, 3737.874f, 222.1753f), repair_silo),
        owning_building_guid = 39
      )
      LocalObject(
        2034,
        Terminal.Constructor(Vector3(3535.832f, 3737.874f, 222.1753f), ground_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2037,
        ProximityTerminal.Constructor(Vector3(3670.17f, 3679.338f, 222.1538f), repair_silo),
        owning_building_guid = 39
      )
      LocalObject(
        2038,
        Terminal.Constructor(Vector3(3670.17f, 3679.338f, 222.1538f), ground_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1402,
        FacilityTurret.Constructor(Vector3(3511.239f, 3723.898f, 230.9243f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1402, 5041)
      LocalObject(
        1403,
        FacilityTurret.Constructor(Vector3(3549.758f, 3671.261f, 230.9243f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1403, 5042)
      LocalObject(
        1404,
        FacilityTurret.Constructor(Vector3(3605.178f, 3623.161f, 230.9243f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1404, 5043)
      LocalObject(
        1408,
        FacilityTurret.Constructor(Vector3(3631.729f, 3836.245f, 230.9243f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1408, 5044)
      LocalObject(
        1411,
        FacilityTurret.Constructor(Vector3(3705.323f, 3633.882f, 230.9243f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1411, 5045)
      LocalObject(
        1415,
        FacilityTurret.Constructor(Vector3(3770.921f, 3695.054f, 230.9243f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1415, 5046)
      LocalObject(
        1878,
        Painbox.Constructor(Vector3(3620.59f, 3695.222f, 210.9986f), painbox),
        owning_building_guid = 39
      )
      LocalObject(
        1887,
        Painbox.Constructor(Vector3(3601.675f, 3722.697f, 219.2952f), painbox_continuous),
        owning_building_guid = 39
      )
      LocalObject(
        1896,
        Painbox.Constructor(Vector3(3609.105f, 3704.66f, 210.6847f), painbox_door_radius),
        owning_building_guid = 39
      )
      LocalObject(
        1909,
        Painbox.Constructor(Vector3(3594.169f, 3733.382f, 217.1532f), painbox_door_radius_continuous),
        owning_building_guid = 39
      )
      LocalObject(
        1910,
        Painbox.Constructor(Vector3(3595.863f, 3707.217f, 218.6076f), painbox_door_radius_continuous),
        owning_building_guid = 39
      )
      LocalObject(
        1911,
        Painbox.Constructor(Vector3(3612.313f, 3712.714f, 217.7015f), painbox_door_radius_continuous),
        owning_building_guid = 39
      )
      LocalObject(239, Generator.Constructor(Vector3(3621.237f, 3691.247f, 206.2313f)), owning_building_guid = 39)
      LocalObject(
        230,
        Terminal.Constructor(Vector3(3615.615f, 3697.206f, 207.5253f), gen_control),
        owning_building_guid = 39
      )
    }

    Building6()

    def Building6(): Unit = { // Name: Pinga Type: tech_plant GUID: 42, MapID: 6
      LocalBuilding(
        "Pinga",
        42,
        6,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(5860f, 3444f, 96.40965f), Vector3(0f, 0f, 90f), tech_plant)
        )
      )
      LocalObject(
        183,
        CaptureTerminal.Constructor(Vector3(5904.089f, 3448.734f, 111.5097f), capture_terminal),
        owning_building_guid = 42
      )
      LocalObject(493, Door.Constructor(Vector3(5828.734f, 3468.559f, 97.95165f)), owning_building_guid = 42)
      LocalObject(494, Door.Constructor(Vector3(5828.734f, 3486.752f, 105.9146f)), owning_building_guid = 42)
      LocalObject(495, Door.Constructor(Vector3(5838.135f, 3372.54f, 105.9146f)), owning_building_guid = 42)
      LocalObject(497, Door.Constructor(Vector3(5856.327f, 3372.54f, 97.95165f)), owning_building_guid = 42)
      LocalObject(498, Door.Constructor(Vector3(5856.893f, 3523.02f, 97.95165f)), owning_building_guid = 42)
      LocalObject(499, Door.Constructor(Vector3(5875.086f, 3523.02f, 105.9146f)), owning_building_guid = 42)
      LocalObject(500, Door.Constructor(Vector3(5883.425f, 3454.597f, 113.0307f)), owning_building_guid = 42)
      LocalObject(502, Door.Constructor(Vector3(5899.426f, 3461.444f, 113.0307f)), owning_building_guid = 42)
      LocalObject(503, Door.Constructor(Vector3(5911.879f, 3372.54f, 105.9146f)), owning_building_guid = 42)
      LocalObject(504, Door.Constructor(Vector3(5930.071f, 3372.54f, 97.95165f)), owning_building_guid = 42)
      LocalObject(505, Door.Constructor(Vector3(5951.198f, 3430.412f, 106.0237f)), owning_building_guid = 42)
      LocalObject(506, Door.Constructor(Vector3(5951.198f, 3448.605f, 98.06065f)), owning_building_guid = 42)
      LocalObject(507, Door.Constructor(Vector3(5960f, 3404f, 98.03065f)), owning_building_guid = 42)
      LocalObject(514, Door.Constructor(Vector3(5819.998f, 3396f, 100.1467f)), owning_building_guid = 42)
      LocalObject(517, Door.Constructor(Vector3(5876f, 3396f, 80.14665f)), owning_building_guid = 42)
      LocalObject(692, Door.Constructor(Vector3(5876f, 3472f, 83.03065f)), owning_building_guid = 42)
      LocalObject(693, Door.Constructor(Vector3(5880f, 3412f, 88.03065f)), owning_building_guid = 42)
      LocalObject(694, Door.Constructor(Vector3(5884f, 3440f, 93.03065f)), owning_building_guid = 42)
      LocalObject(695, Door.Constructor(Vector3(5884f, 3464f, 90.53065f)), owning_building_guid = 42)
      LocalObject(696, Door.Constructor(Vector3(5888f, 3444f, 113.0307f)), owning_building_guid = 42)
      LocalObject(697, Door.Constructor(Vector3(5888f, 3476f, 90.53065f)), owning_building_guid = 42)
      LocalObject(698, Door.Constructor(Vector3(5892f, 3444f, 103.0307f)), owning_building_guid = 42)
      LocalObject(699, Door.Constructor(Vector3(5896f, 3444f, 83.03065f)), owning_building_guid = 42)
      LocalObject(700, Door.Constructor(Vector3(5900f, 3440f, 93.03065f)), owning_building_guid = 42)
      LocalObject(701, Door.Constructor(Vector3(5900f, 3440f, 113.0307f)), owning_building_guid = 42)
      LocalObject(702, Door.Constructor(Vector3(5904f, 3380f, 88.03065f)), owning_building_guid = 42)
      LocalObject(703, Door.Constructor(Vector3(5904f, 3412f, 88.03065f)), owning_building_guid = 42)
      LocalObject(704, Door.Constructor(Vector3(5904f, 3476f, 83.03065f)), owning_building_guid = 42)
      LocalObject(705, Door.Constructor(Vector3(5908f, 3464f, 83.03065f)), owning_building_guid = 42)
      LocalObject(706, Door.Constructor(Vector3(5908f, 3464f, 90.53065f)), owning_building_guid = 42)
      LocalObject(707, Door.Constructor(Vector3(5912f, 3428f, 90.53065f)), owning_building_guid = 42)
      LocalObject(708, Door.Constructor(Vector3(5928f, 3380f, 90.53065f)), owning_building_guid = 42)
      LocalObject(709, Door.Constructor(Vector3(5936f, 3444f, 90.53065f)), owning_building_guid = 42)
      LocalObject(710, Door.Constructor(Vector3(5952f, 3428f, 90.53065f)), owning_building_guid = 42)
      LocalObject(719, Door.Constructor(Vector3(5905.659f, 3484.213f, 98.78965f)), owning_building_guid = 42)
      LocalObject(2355, Door.Constructor(Vector3(5887.685f, 3456.673f, 90.86366f)), owning_building_guid = 42)
      LocalObject(2356, Door.Constructor(Vector3(5894.974f, 3456.673f, 90.86366f)), owning_building_guid = 42)
      LocalObject(2357, Door.Constructor(Vector3(5902.267f, 3456.673f, 90.86366f)), owning_building_guid = 42)
      LocalObject(
        755,
        IFFLock.Constructor(Vector3(5908.397f, 3487.357f, 97.98965f), Vector3(0, 0, 90)),
        owning_building_guid = 42,
        door_guid = 719
      )
      LocalObject(
        758,
        IFFLock.Constructor(Vector3(5817.647f, 3401.256f, 98.09766f), Vector3(0, 0, 270)),
        owning_building_guid = 42,
        door_guid = 514
      )
      LocalObject(
        959,
        IFFLock.Constructor(Vector3(5876.943f, 3473.572f, 82.84565f), Vector3(0, 0, 90)),
        owning_building_guid = 42,
        door_guid = 692
      )
      LocalObject(
        960,
        IFFLock.Constructor(Vector3(5882.617f, 3452.554f, 112.9557f), Vector3(0, 0, 270)),
        owning_building_guid = 42,
        door_guid = 500
      )
      LocalObject(
        961,
        IFFLock.Constructor(Vector3(5883.19f, 3462.428f, 90.34565f), Vector3(0, 0, 270)),
        owning_building_guid = 42,
        door_guid = 695
      )
      LocalObject(
        962,
        IFFLock.Constructor(Vector3(5899.06f, 3438.428f, 112.8457f), Vector3(0, 0, 270)),
        owning_building_guid = 42,
        door_guid = 701
      )
      LocalObject(
        963,
        IFFLock.Constructor(Vector3(5900.225f, 3463.496f, 112.9557f), Vector3(0, 0, 90)),
        owning_building_guid = 42,
        door_guid = 502
      )
      LocalObject(
        964,
        IFFLock.Constructor(Vector3(5905.572f, 3475.06f, 82.84565f), Vector3(0, 0, 180)),
        owning_building_guid = 42,
        door_guid = 704
      )
      LocalObject(
        965,
        IFFLock.Constructor(Vector3(5908.81f, 3465.572f, 90.34565f), Vector3(0, 0, 90)),
        owning_building_guid = 42,
        door_guid = 706
      )
      LocalObject(
        966,
        IFFLock.Constructor(Vector3(5962.046f, 3403.186f, 97.96165f), Vector3(0, 0, 180)),
        owning_building_guid = 42,
        door_guid = 507
      )
      LocalObject(1318, Locker.Constructor(Vector3(5885.835f, 3478.194f, 81.50965f)), owning_building_guid = 42)
      LocalObject(1319, Locker.Constructor(Vector3(5885.835f, 3479.518f, 81.50965f)), owning_building_guid = 42)
      LocalObject(1320, Locker.Constructor(Vector3(5885.835f, 3480.854f, 81.50965f)), owning_building_guid = 42)
      LocalObject(1321, Locker.Constructor(Vector3(5885.835f, 3482.191f, 81.50965f)), owning_building_guid = 42)
      LocalObject(1322, Locker.Constructor(Vector3(5885.835f, 3486.731f, 81.50965f)), owning_building_guid = 42)
      LocalObject(1323, Locker.Constructor(Vector3(5885.835f, 3488.055f, 81.50965f)), owning_building_guid = 42)
      LocalObject(1324, Locker.Constructor(Vector3(5885.835f, 3489.391f, 81.50965f)), owning_building_guid = 42)
      LocalObject(1325, Locker.Constructor(Vector3(5885.835f, 3490.728f, 81.50965f)), owning_building_guid = 42)
      LocalObject(1326, Locker.Constructor(Vector3(5905.859f, 3467.563f, 89.27065f)), owning_building_guid = 42)
      LocalObject(1327, Locker.Constructor(Vector3(5905.859f, 3468.727f, 89.27065f)), owning_building_guid = 42)
      LocalObject(1328, Locker.Constructor(Vector3(5905.859f, 3469.874f, 89.27065f)), owning_building_guid = 42)
      LocalObject(1329, Locker.Constructor(Vector3(5905.859f, 3471.023f, 89.27065f)), owning_building_guid = 42)
      LocalObject(
        144,
        Terminal.Constructor(Vector3(5884.859f, 3448.673f, 112.1127f), air_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1482,
        VehicleSpawnPad.Constructor(Vector3(5864.165f, 3444.07f, 108.9877f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 42,
        terminal_guid = 144
      )
      LocalObject(
        145,
        Terminal.Constructor(Vector3(5884.859f, 3460.605f, 112.1127f), air_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1483,
        VehicleSpawnPad.Constructor(Vector3(5864.165f, 3465.088f, 108.9877f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 42,
        terminal_guid = 145
      )
      LocalObject(
        1625,
        Terminal.Constructor(Vector3(5886.514f, 3447.058f, 102.8397f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1626,
        Terminal.Constructor(Vector3(5893.072f, 3470.654f, 90.59966f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1627,
        Terminal.Constructor(Vector3(5896.861f, 3470.654f, 90.59966f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1628,
        Terminal.Constructor(Vector3(5900.592f, 3470.654f, 90.59966f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2268,
        Terminal.Constructor(Vector3(5856.785f, 3458.532f, 108.4327f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2269,
        Terminal.Constructor(Vector3(5880.361f, 3481.242f, 103.0917f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2270,
        Terminal.Constructor(Vector3(5890.177f, 3456.97f, 91.14365f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2271,
        Terminal.Constructor(Vector3(5897.465f, 3456.967f, 91.14365f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2272,
        Terminal.Constructor(Vector3(5904.757f, 3456.971f, 91.14365f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2273,
        Terminal.Constructor(Vector3(5907.409f, 3451.942f, 83.06665f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2274,
        Terminal.Constructor(Vector3(5955.409f, 3419.942f, 90.56665f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2422,
        Terminal.Constructor(Vector3(5900.577f, 3395.996f, 82.22366f), ground_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1484,
        VehicleSpawnPad.Constructor(Vector3(5889.661f, 3395.945f, 73.94666f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 42,
        terminal_guid = 2422
      )
      LocalObject(2097, ResourceSilo.Constructor(Vector3(5827.445f, 3505.752f, 103.4177f)), owning_building_guid = 42)
      LocalObject(
        2178,
        SpawnTube.Constructor(Vector3(5888.738f, 3456.233f, 89.00965f), Vector3(0, 0, 270)),
        owning_building_guid = 42
      )
      LocalObject(
        2179,
        SpawnTube.Constructor(Vector3(5896.026f, 3456.233f, 89.00965f), Vector3(0, 0, 270)),
        owning_building_guid = 42
      )
      LocalObject(
        2180,
        SpawnTube.Constructor(Vector3(5903.317f, 3456.233f, 89.00965f), Vector3(0, 0, 270)),
        owning_building_guid = 42
      )
      LocalObject(
        1498,
        ProximityTerminal.Constructor(Vector3(5886.38f, 3484.444f, 81.50965f), medical_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1499,
        ProximityTerminal.Constructor(Vector3(5897.099f, 3447.059f, 101.5067f), medical_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1792,
        ProximityTerminal.Constructor(Vector3(5868.167f, 3402.98f, 106.9597f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1793,
        Terminal.Constructor(Vector3(5868.167f, 3402.98f, 106.9597f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1795,
        ProximityTerminal.Constructor(Vector3(5884.339f, 3382.704f, 104.6047f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1796,
        Terminal.Constructor(Vector3(5884.339f, 3382.704f, 104.6047f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1798,
        ProximityTerminal.Constructor(Vector3(5885.898f, 3512.28f, 104.6177f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1799,
        Terminal.Constructor(Vector3(5885.898f, 3512.28f, 104.6177f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1801,
        ProximityTerminal.Constructor(Vector3(5902.145f, 3505.987f, 107.0617f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1802,
        Terminal.Constructor(Vector3(5902.145f, 3505.987f, 107.0617f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1804,
        ProximityTerminal.Constructor(Vector3(5924.526f, 3453.379f, 111.8567f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1805,
        Terminal.Constructor(Vector3(5924.526f, 3453.379f, 111.8567f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1807,
        ProximityTerminal.Constructor(Vector3(5941.372f, 3469.534f, 104.6177f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1808,
        Terminal.Constructor(Vector3(5941.372f, 3469.534f, 104.6177f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2081,
        ProximityTerminal.Constructor(Vector3(5818.363f, 3444.309f, 96.13815f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        2082,
        Terminal.Constructor(Vector3(5818.363f, 3444.309f, 96.13815f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2085,
        ProximityTerminal.Constructor(Vector3(5952.792f, 3502.637f, 96.15965f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        2086,
        Terminal.Constructor(Vector3(5952.792f, 3502.637f, 96.15965f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1461,
        FacilityTurret.Constructor(Vector3(5761.145f, 3359.906f, 104.9087f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1461, 5047)
      LocalObject(
        1462,
        FacilityTurret.Constructor(Vector3(5761.145f, 3449.601f, 104.9087f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1462, 5048)
      LocalObject(
        1464,
        FacilityTurret.Constructor(Vector3(5821.602f, 3530.154f, 104.9087f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1464, 5049)
      LocalObject(
        1465,
        FacilityTurret.Constructor(Vector3(5894.577f, 3537.881f, 104.9087f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1465, 5050)
      LocalObject(
        1466,
        FacilityTurret.Constructor(Vector3(5959.335f, 3365.413f, 104.9087f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1466, 5051)
      LocalObject(
        1467,
        FacilityTurret.Constructor(Vector3(5959.343f, 3530.154f, 104.9087f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1467, 5052)
      LocalObject(
        1884,
        Painbox.Constructor(Vector3(5863.793f, 3469.737f, 84.98296f), painbox),
        owning_building_guid = 42
      )
      LocalObject(
        1893,
        Painbox.Constructor(Vector3(5896.788f, 3464.832f, 93.27956f), painbox_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1902,
        Painbox.Constructor(Vector3(5878.529f, 3471.7f, 84.66905f), painbox_door_radius),
        owning_building_guid = 42
      )
      LocalObject(
        1927,
        Painbox.Constructor(Vector3(5882.231f, 3463.861f, 91.68585f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1928,
        Painbox.Constructor(Vector3(5889.43f, 3479.641f, 92.59195f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1929,
        Painbox.Constructor(Vector3(5909.722f, 3463.035f, 91.13755f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(245, Generator.Constructor(Vector3(5860.445f, 3471.975f, 80.21565f)), owning_building_guid = 42)
      LocalObject(
        236,
        Terminal.Constructor(Vector3(5868.637f, 3472.022f, 81.50965f), gen_control),
        owning_building_guid = 42
      )
    }

    Building14()

    def Building14(): Unit = { // Name: W_Amerish_Warpgate_Tower Type: tower_a GUID: 45, MapID: 14
      LocalBuilding(
        "W_Amerish_Warpgate_Tower",
        45,
        14,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1822f, 2648f, 186.5722f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2183,
        CaptureTerminal.Constructor(Vector3(1838.587f, 2647.897f, 196.5712f), secondary_capture),
        owning_building_guid = 45
      )
      LocalObject(246, Door.Constructor(Vector3(1834f, 2640f, 188.0932f)), owning_building_guid = 45)
      LocalObject(247, Door.Constructor(Vector3(1834f, 2640f, 208.0922f)), owning_building_guid = 45)
      LocalObject(248, Door.Constructor(Vector3(1834f, 2656f, 188.0932f)), owning_building_guid = 45)
      LocalObject(249, Door.Constructor(Vector3(1834f, 2656f, 208.0922f)), owning_building_guid = 45)
      LocalObject(2275, Door.Constructor(Vector3(1833.146f, 2636.794f, 177.9082f)), owning_building_guid = 45)
      LocalObject(2276, Door.Constructor(Vector3(1833.146f, 2653.204f, 177.9082f)), owning_building_guid = 45)
      LocalObject(
        759,
        IFFLock.Constructor(Vector3(1831.957f, 2656.811f, 188.0332f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 248
      )
      LocalObject(
        760,
        IFFLock.Constructor(Vector3(1831.957f, 2656.811f, 208.0332f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 249
      )
      LocalObject(
        761,
        IFFLock.Constructor(Vector3(1836.047f, 2639.189f, 188.0332f), Vector3(0, 0, 180)),
        owning_building_guid = 45,
        door_guid = 246
      )
      LocalObject(
        762,
        IFFLock.Constructor(Vector3(1836.047f, 2639.189f, 208.0332f), Vector3(0, 0, 180)),
        owning_building_guid = 45,
        door_guid = 247
      )
      LocalObject(971, Locker.Constructor(Vector3(1837.716f, 2632.963f, 176.5662f)), owning_building_guid = 45)
      LocalObject(972, Locker.Constructor(Vector3(1837.751f, 2654.835f, 176.5662f)), owning_building_guid = 45)
      LocalObject(973, Locker.Constructor(Vector3(1839.053f, 2632.963f, 176.5662f)), owning_building_guid = 45)
      LocalObject(974, Locker.Constructor(Vector3(1839.088f, 2654.835f, 176.5662f)), owning_building_guid = 45)
      LocalObject(975, Locker.Constructor(Vector3(1841.741f, 2632.963f, 176.5662f)), owning_building_guid = 45)
      LocalObject(976, Locker.Constructor(Vector3(1841.741f, 2654.835f, 176.5662f)), owning_building_guid = 45)
      LocalObject(977, Locker.Constructor(Vector3(1843.143f, 2632.963f, 176.5662f)), owning_building_guid = 45)
      LocalObject(978, Locker.Constructor(Vector3(1843.143f, 2654.835f, 176.5662f)), owning_building_guid = 45)
      LocalObject(
        1500,
        Terminal.Constructor(Vector3(1843.445f, 2638.129f, 177.9042f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1501,
        Terminal.Constructor(Vector3(1843.445f, 2643.853f, 177.9042f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1502,
        Terminal.Constructor(Vector3(1843.445f, 2649.234f, 177.9042f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2098,
        SpawnTube.Constructor(Vector3(1832.706f, 2635.742f, 176.0542f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        2099,
        SpawnTube.Constructor(Vector3(1832.706f, 2652.152f, 176.0542f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        1371,
        FacilityTurret.Constructor(Vector3(1809.32f, 2635.295f, 205.5142f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1371, 5053)
      LocalObject(
        1372,
        FacilityTurret.Constructor(Vector3(1844.647f, 2660.707f, 205.5142f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1372, 5054)
      LocalObject(
        1930,
        Painbox.Constructor(Vector3(1827.235f, 2641.803f, 178.0713f), painbox_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        1931,
        Painbox.Constructor(Vector3(1838.889f, 2650.086f, 176.6722f), painbox_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        1932,
        Painbox.Constructor(Vector3(1838.975f, 2638.223f, 176.6722f), painbox_radius_continuous),
        owning_building_guid = 45
      )
    }

    Building25()

    def Building25(): Unit = { // Name: N_Amerish_Warpgate_Tower Type: tower_a GUID: 46, MapID: 25
      LocalBuilding(
        "N_Amerish_Warpgate_Tower",
        46,
        25,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2616f, 4086f, 21.97363f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2186,
        CaptureTerminal.Constructor(Vector3(2632.587f, 4085.897f, 31.97263f), secondary_capture),
        owning_building_guid = 46
      )
      LocalObject(260, Door.Constructor(Vector3(2628f, 4078f, 23.49463f)), owning_building_guid = 46)
      LocalObject(261, Door.Constructor(Vector3(2628f, 4078f, 43.49363f)), owning_building_guid = 46)
      LocalObject(262, Door.Constructor(Vector3(2628f, 4094f, 23.49463f)), owning_building_guid = 46)
      LocalObject(263, Door.Constructor(Vector3(2628f, 4094f, 43.49363f)), owning_building_guid = 46)
      LocalObject(2281, Door.Constructor(Vector3(2627.146f, 4074.794f, 13.30963f)), owning_building_guid = 46)
      LocalObject(2282, Door.Constructor(Vector3(2627.146f, 4091.204f, 13.30963f)), owning_building_guid = 46)
      LocalObject(
        773,
        IFFLock.Constructor(Vector3(2625.957f, 4094.811f, 23.43463f), Vector3(0, 0, 0)),
        owning_building_guid = 46,
        door_guid = 262
      )
      LocalObject(
        774,
        IFFLock.Constructor(Vector3(2625.957f, 4094.811f, 43.43463f), Vector3(0, 0, 0)),
        owning_building_guid = 46,
        door_guid = 263
      )
      LocalObject(
        775,
        IFFLock.Constructor(Vector3(2630.047f, 4077.189f, 23.43463f), Vector3(0, 0, 180)),
        owning_building_guid = 46,
        door_guid = 260
      )
      LocalObject(
        776,
        IFFLock.Constructor(Vector3(2630.047f, 4077.189f, 43.43463f), Vector3(0, 0, 180)),
        owning_building_guid = 46,
        door_guid = 261
      )
      LocalObject(995, Locker.Constructor(Vector3(2631.716f, 4070.963f, 11.96763f)), owning_building_guid = 46)
      LocalObject(996, Locker.Constructor(Vector3(2631.751f, 4092.835f, 11.96763f)), owning_building_guid = 46)
      LocalObject(997, Locker.Constructor(Vector3(2633.053f, 4070.963f, 11.96763f)), owning_building_guid = 46)
      LocalObject(998, Locker.Constructor(Vector3(2633.088f, 4092.835f, 11.96763f)), owning_building_guid = 46)
      LocalObject(999, Locker.Constructor(Vector3(2635.741f, 4070.963f, 11.96763f)), owning_building_guid = 46)
      LocalObject(1000, Locker.Constructor(Vector3(2635.741f, 4092.835f, 11.96763f)), owning_building_guid = 46)
      LocalObject(1001, Locker.Constructor(Vector3(2637.143f, 4070.963f, 11.96763f)), owning_building_guid = 46)
      LocalObject(1002, Locker.Constructor(Vector3(2637.143f, 4092.835f, 11.96763f)), owning_building_guid = 46)
      LocalObject(
        1509,
        Terminal.Constructor(Vector3(2637.445f, 4076.129f, 13.30563f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1510,
        Terminal.Constructor(Vector3(2637.445f, 4081.853f, 13.30563f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1511,
        Terminal.Constructor(Vector3(2637.445f, 4087.234f, 13.30563f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2104,
        SpawnTube.Constructor(Vector3(2626.706f, 4073.742f, 11.45563f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 46
      )
      LocalObject(
        2105,
        SpawnTube.Constructor(Vector3(2626.706f, 4090.152f, 11.45563f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 46
      )
      LocalObject(
        1375,
        FacilityTurret.Constructor(Vector3(2603.32f, 4073.295f, 40.91563f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1375, 5055)
      LocalObject(
        1377,
        FacilityTurret.Constructor(Vector3(2638.647f, 4098.707f, 40.91563f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1377, 5056)
      LocalObject(
        1939,
        Painbox.Constructor(Vector3(2621.235f, 4079.803f, 13.47273f), painbox_radius_continuous),
        owning_building_guid = 46
      )
      LocalObject(
        1940,
        Painbox.Constructor(Vector3(2632.889f, 4088.086f, 12.07363f), painbox_radius_continuous),
        owning_building_guid = 46
      )
      LocalObject(
        1941,
        Painbox.Constructor(Vector3(2632.975f, 4076.223f, 12.07363f), painbox_radius_continuous),
        owning_building_guid = 46
      )
    }

    Building32()

    def Building32(): Unit = { // Name: S_Tarqaq_Tower Type: tower_a GUID: 47, MapID: 32
      LocalBuilding(
        "S_Tarqaq_Tower",
        47,
        32,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2914f, 1496f, 51.68828f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2189,
        CaptureTerminal.Constructor(Vector3(2930.587f, 1495.897f, 61.68728f), secondary_capture),
        owning_building_guid = 47
      )
      LocalObject(277, Door.Constructor(Vector3(2926f, 1488f, 53.20928f)), owning_building_guid = 47)
      LocalObject(278, Door.Constructor(Vector3(2926f, 1488f, 73.20828f)), owning_building_guid = 47)
      LocalObject(279, Door.Constructor(Vector3(2926f, 1504f, 53.20928f)), owning_building_guid = 47)
      LocalObject(280, Door.Constructor(Vector3(2926f, 1504f, 73.20828f)), owning_building_guid = 47)
      LocalObject(2287, Door.Constructor(Vector3(2925.146f, 1484.794f, 43.02428f)), owning_building_guid = 47)
      LocalObject(2288, Door.Constructor(Vector3(2925.146f, 1501.204f, 43.02428f)), owning_building_guid = 47)
      LocalObject(
        786,
        IFFLock.Constructor(Vector3(2923.957f, 1504.811f, 53.14928f), Vector3(0, 0, 0)),
        owning_building_guid = 47,
        door_guid = 279
      )
      LocalObject(
        787,
        IFFLock.Constructor(Vector3(2923.957f, 1504.811f, 73.14928f), Vector3(0, 0, 0)),
        owning_building_guid = 47,
        door_guid = 280
      )
      LocalObject(
        788,
        IFFLock.Constructor(Vector3(2928.047f, 1487.189f, 53.14928f), Vector3(0, 0, 180)),
        owning_building_guid = 47,
        door_guid = 277
      )
      LocalObject(
        789,
        IFFLock.Constructor(Vector3(2928.047f, 1487.189f, 73.14928f), Vector3(0, 0, 180)),
        owning_building_guid = 47,
        door_guid = 278
      )
      LocalObject(1019, Locker.Constructor(Vector3(2929.716f, 1480.963f, 41.68228f)), owning_building_guid = 47)
      LocalObject(1020, Locker.Constructor(Vector3(2929.751f, 1502.835f, 41.68228f)), owning_building_guid = 47)
      LocalObject(1021, Locker.Constructor(Vector3(2931.053f, 1480.963f, 41.68228f)), owning_building_guid = 47)
      LocalObject(1022, Locker.Constructor(Vector3(2931.088f, 1502.835f, 41.68228f)), owning_building_guid = 47)
      LocalObject(1023, Locker.Constructor(Vector3(2933.741f, 1480.963f, 41.68228f)), owning_building_guid = 47)
      LocalObject(1024, Locker.Constructor(Vector3(2933.741f, 1502.835f, 41.68228f)), owning_building_guid = 47)
      LocalObject(1025, Locker.Constructor(Vector3(2935.143f, 1480.963f, 41.68228f)), owning_building_guid = 47)
      LocalObject(1026, Locker.Constructor(Vector3(2935.143f, 1502.835f, 41.68228f)), owning_building_guid = 47)
      LocalObject(
        1518,
        Terminal.Constructor(Vector3(2935.445f, 1486.129f, 43.02028f), order_terminal),
        owning_building_guid = 47
      )
      LocalObject(
        1519,
        Terminal.Constructor(Vector3(2935.445f, 1491.853f, 43.02028f), order_terminal),
        owning_building_guid = 47
      )
      LocalObject(
        1520,
        Terminal.Constructor(Vector3(2935.445f, 1497.234f, 43.02028f), order_terminal),
        owning_building_guid = 47
      )
      LocalObject(
        2110,
        SpawnTube.Constructor(Vector3(2924.706f, 1483.742f, 41.17028f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 47
      )
      LocalObject(
        2111,
        SpawnTube.Constructor(Vector3(2924.706f, 1500.152f, 41.17028f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 47
      )
      LocalObject(
        1382,
        FacilityTurret.Constructor(Vector3(2901.32f, 1483.295f, 70.63028f), manned_turret),
        owning_building_guid = 47
      )
      TurretToWeapon(1382, 5057)
      LocalObject(
        1383,
        FacilityTurret.Constructor(Vector3(2936.647f, 1508.707f, 70.63028f), manned_turret),
        owning_building_guid = 47
      )
      TurretToWeapon(1383, 5058)
      LocalObject(
        1948,
        Painbox.Constructor(Vector3(2919.235f, 1489.803f, 43.18738f), painbox_radius_continuous),
        owning_building_guid = 47
      )
      LocalObject(
        1949,
        Painbox.Constructor(Vector3(2930.889f, 1498.086f, 41.78828f), painbox_radius_continuous),
        owning_building_guid = 47
      )
      LocalObject(
        1950,
        Painbox.Constructor(Vector3(2930.975f, 1486.223f, 41.78828f), painbox_radius_continuous),
        owning_building_guid = 47
      )
    }

    Building23()

    def Building23(): Unit = { // Name: NW_Anguta_Tower Type: tower_a GUID: 48, MapID: 23
      LocalBuilding(
        "NW_Anguta_Tower",
        48,
        23,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3206f, 4790f, 68.82293f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2191,
        CaptureTerminal.Constructor(Vector3(3222.587f, 4789.897f, 78.82193f), secondary_capture),
        owning_building_guid = 48
      )
      LocalObject(295, Door.Constructor(Vector3(3218f, 4782f, 70.34393f)), owning_building_guid = 48)
      LocalObject(296, Door.Constructor(Vector3(3218f, 4782f, 90.34293f)), owning_building_guid = 48)
      LocalObject(297, Door.Constructor(Vector3(3218f, 4798f, 70.34393f)), owning_building_guid = 48)
      LocalObject(298, Door.Constructor(Vector3(3218f, 4798f, 90.34293f)), owning_building_guid = 48)
      LocalObject(2294, Door.Constructor(Vector3(3217.146f, 4778.794f, 60.15893f)), owning_building_guid = 48)
      LocalObject(2295, Door.Constructor(Vector3(3217.146f, 4795.204f, 60.15893f)), owning_building_guid = 48)
      LocalObject(
        801,
        IFFLock.Constructor(Vector3(3215.957f, 4798.811f, 70.28393f), Vector3(0, 0, 0)),
        owning_building_guid = 48,
        door_guid = 297
      )
      LocalObject(
        802,
        IFFLock.Constructor(Vector3(3215.957f, 4798.811f, 90.28393f), Vector3(0, 0, 0)),
        owning_building_guid = 48,
        door_guid = 298
      )
      LocalObject(
        804,
        IFFLock.Constructor(Vector3(3220.047f, 4781.189f, 70.28393f), Vector3(0, 0, 180)),
        owning_building_guid = 48,
        door_guid = 295
      )
      LocalObject(
        805,
        IFFLock.Constructor(Vector3(3220.047f, 4781.189f, 90.28393f), Vector3(0, 0, 180)),
        owning_building_guid = 48,
        door_guid = 296
      )
      LocalObject(1056, Locker.Constructor(Vector3(3221.716f, 4774.963f, 58.81693f)), owning_building_guid = 48)
      LocalObject(1057, Locker.Constructor(Vector3(3221.751f, 4796.835f, 58.81693f)), owning_building_guid = 48)
      LocalObject(1058, Locker.Constructor(Vector3(3223.053f, 4774.963f, 58.81693f)), owning_building_guid = 48)
      LocalObject(1059, Locker.Constructor(Vector3(3223.088f, 4796.835f, 58.81693f)), owning_building_guid = 48)
      LocalObject(1060, Locker.Constructor(Vector3(3225.741f, 4774.963f, 58.81693f)), owning_building_guid = 48)
      LocalObject(1061, Locker.Constructor(Vector3(3225.741f, 4796.835f, 58.81693f)), owning_building_guid = 48)
      LocalObject(1062, Locker.Constructor(Vector3(3227.143f, 4774.963f, 58.81693f)), owning_building_guid = 48)
      LocalObject(1063, Locker.Constructor(Vector3(3227.143f, 4796.835f, 58.81693f)), owning_building_guid = 48)
      LocalObject(
        1528,
        Terminal.Constructor(Vector3(3227.445f, 4780.129f, 60.15493f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1529,
        Terminal.Constructor(Vector3(3227.445f, 4785.853f, 60.15493f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1530,
        Terminal.Constructor(Vector3(3227.445f, 4791.234f, 60.15493f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2117,
        SpawnTube.Constructor(Vector3(3216.706f, 4777.742f, 58.30493f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 48
      )
      LocalObject(
        2118,
        SpawnTube.Constructor(Vector3(3216.706f, 4794.152f, 58.30493f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 48
      )
      LocalObject(
        1389,
        FacilityTurret.Constructor(Vector3(3193.32f, 4777.295f, 87.76493f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1389, 5059)
      LocalObject(
        1393,
        FacilityTurret.Constructor(Vector3(3228.647f, 4802.707f, 87.76493f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1393, 5060)
      LocalObject(
        1954,
        Painbox.Constructor(Vector3(3211.235f, 4783.803f, 60.32203f), painbox_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        1955,
        Painbox.Constructor(Vector3(3222.889f, 4792.086f, 58.92293f), painbox_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        1956,
        Painbox.Constructor(Vector3(3222.975f, 4780.223f, 58.92293f), painbox_radius_continuous),
        owning_building_guid = 48
      )
    }

    Building46()

    def Building46(): Unit = { // Name: Igaluk_Tower Type: tower_a GUID: 49, MapID: 46
      LocalBuilding(
        "Igaluk_Tower",
        49,
        46,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3386f, 5984f, 215.4855f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2193,
        CaptureTerminal.Constructor(Vector3(3402.587f, 5983.897f, 225.4845f), secondary_capture),
        owning_building_guid = 49
      )
      LocalObject(318, Door.Constructor(Vector3(3398f, 5976f, 217.0065f)), owning_building_guid = 49)
      LocalObject(319, Door.Constructor(Vector3(3398f, 5976f, 237.0056f)), owning_building_guid = 49)
      LocalObject(320, Door.Constructor(Vector3(3398f, 5992f, 217.0065f)), owning_building_guid = 49)
      LocalObject(321, Door.Constructor(Vector3(3398f, 5992f, 237.0056f)), owning_building_guid = 49)
      LocalObject(2301, Door.Constructor(Vector3(3397.146f, 5972.794f, 206.8215f)), owning_building_guid = 49)
      LocalObject(2302, Door.Constructor(Vector3(3397.146f, 5989.204f, 206.8215f)), owning_building_guid = 49)
      LocalObject(
        819,
        IFFLock.Constructor(Vector3(3395.957f, 5992.811f, 216.9465f), Vector3(0, 0, 0)),
        owning_building_guid = 49,
        door_guid = 320
      )
      LocalObject(
        820,
        IFFLock.Constructor(Vector3(3395.957f, 5992.811f, 236.9465f), Vector3(0, 0, 0)),
        owning_building_guid = 49,
        door_guid = 321
      )
      LocalObject(
        821,
        IFFLock.Constructor(Vector3(3400.047f, 5975.189f, 216.9465f), Vector3(0, 0, 180)),
        owning_building_guid = 49,
        door_guid = 318
      )
      LocalObject(
        822,
        IFFLock.Constructor(Vector3(3400.047f, 5975.189f, 236.9465f), Vector3(0, 0, 180)),
        owning_building_guid = 49,
        door_guid = 319
      )
      LocalObject(1084, Locker.Constructor(Vector3(3401.716f, 5968.963f, 205.4796f)), owning_building_guid = 49)
      LocalObject(1085, Locker.Constructor(Vector3(3401.751f, 5990.835f, 205.4796f)), owning_building_guid = 49)
      LocalObject(1086, Locker.Constructor(Vector3(3403.053f, 5968.963f, 205.4796f)), owning_building_guid = 49)
      LocalObject(1087, Locker.Constructor(Vector3(3403.088f, 5990.835f, 205.4796f)), owning_building_guid = 49)
      LocalObject(1088, Locker.Constructor(Vector3(3405.741f, 5968.963f, 205.4796f)), owning_building_guid = 49)
      LocalObject(1089, Locker.Constructor(Vector3(3405.741f, 5990.835f, 205.4796f)), owning_building_guid = 49)
      LocalObject(1090, Locker.Constructor(Vector3(3407.143f, 5968.963f, 205.4796f)), owning_building_guid = 49)
      LocalObject(1091, Locker.Constructor(Vector3(3407.143f, 5990.835f, 205.4796f)), owning_building_guid = 49)
      LocalObject(
        1538,
        Terminal.Constructor(Vector3(3407.445f, 5974.129f, 206.8176f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1539,
        Terminal.Constructor(Vector3(3407.445f, 5979.853f, 206.8176f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1540,
        Terminal.Constructor(Vector3(3407.445f, 5985.234f, 206.8176f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2124,
        SpawnTube.Constructor(Vector3(3396.706f, 5971.742f, 204.9675f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 49
      )
      LocalObject(
        2125,
        SpawnTube.Constructor(Vector3(3396.706f, 5988.152f, 204.9675f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 49
      )
      LocalObject(
        1396,
        FacilityTurret.Constructor(Vector3(3373.32f, 5971.295f, 234.4276f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1396, 5061)
      LocalObject(
        1397,
        FacilityTurret.Constructor(Vector3(3408.647f, 5996.707f, 234.4276f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1397, 5062)
      LocalObject(
        1960,
        Painbox.Constructor(Vector3(3391.235f, 5977.803f, 206.9846f), painbox_radius_continuous),
        owning_building_guid = 49
      )
      LocalObject(
        1961,
        Painbox.Constructor(Vector3(3402.889f, 5986.086f, 205.5856f), painbox_radius_continuous),
        owning_building_guid = 49
      )
      LocalObject(
        1962,
        Painbox.Constructor(Vector3(3402.975f, 5974.223f, 205.5856f), painbox_radius_continuous),
        owning_building_guid = 49
      )
    }

    Building53()

    def Building53(): Unit = { // Name: Keelut_Tower Type: tower_a GUID: 50, MapID: 53
      LocalBuilding(
        "Keelut_Tower",
        50,
        53,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3636f, 1720f, 250.5918f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2195,
        CaptureTerminal.Constructor(Vector3(3652.587f, 1719.897f, 260.5908f), secondary_capture),
        owning_building_guid = 50
      )
      LocalObject(340, Door.Constructor(Vector3(3648f, 1712f, 252.1127f)), owning_building_guid = 50)
      LocalObject(341, Door.Constructor(Vector3(3648f, 1712f, 272.1118f)), owning_building_guid = 50)
      LocalObject(342, Door.Constructor(Vector3(3648f, 1728f, 252.1127f)), owning_building_guid = 50)
      LocalObject(343, Door.Constructor(Vector3(3648f, 1728f, 272.1118f)), owning_building_guid = 50)
      LocalObject(2308, Door.Constructor(Vector3(3647.146f, 1708.794f, 241.9277f)), owning_building_guid = 50)
      LocalObject(2309, Door.Constructor(Vector3(3647.146f, 1725.204f, 241.9277f)), owning_building_guid = 50)
      LocalObject(
        835,
        IFFLock.Constructor(Vector3(3645.957f, 1728.811f, 252.0527f), Vector3(0, 0, 0)),
        owning_building_guid = 50,
        door_guid = 342
      )
      LocalObject(
        836,
        IFFLock.Constructor(Vector3(3645.957f, 1728.811f, 272.0528f), Vector3(0, 0, 0)),
        owning_building_guid = 50,
        door_guid = 343
      )
      LocalObject(
        837,
        IFFLock.Constructor(Vector3(3650.047f, 1711.189f, 252.0527f), Vector3(0, 0, 180)),
        owning_building_guid = 50,
        door_guid = 340
      )
      LocalObject(
        838,
        IFFLock.Constructor(Vector3(3650.047f, 1711.189f, 272.0528f), Vector3(0, 0, 180)),
        owning_building_guid = 50,
        door_guid = 341
      )
      LocalObject(1112, Locker.Constructor(Vector3(3651.716f, 1704.963f, 240.5858f)), owning_building_guid = 50)
      LocalObject(1113, Locker.Constructor(Vector3(3651.751f, 1726.835f, 240.5858f)), owning_building_guid = 50)
      LocalObject(1114, Locker.Constructor(Vector3(3653.053f, 1704.963f, 240.5858f)), owning_building_guid = 50)
      LocalObject(1115, Locker.Constructor(Vector3(3653.088f, 1726.835f, 240.5858f)), owning_building_guid = 50)
      LocalObject(1116, Locker.Constructor(Vector3(3655.741f, 1704.963f, 240.5858f)), owning_building_guid = 50)
      LocalObject(1117, Locker.Constructor(Vector3(3655.741f, 1726.835f, 240.5858f)), owning_building_guid = 50)
      LocalObject(1118, Locker.Constructor(Vector3(3657.143f, 1704.963f, 240.5858f)), owning_building_guid = 50)
      LocalObject(1119, Locker.Constructor(Vector3(3657.143f, 1726.835f, 240.5858f)), owning_building_guid = 50)
      LocalObject(
        1548,
        Terminal.Constructor(Vector3(3657.445f, 1710.129f, 241.9238f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1549,
        Terminal.Constructor(Vector3(3657.445f, 1715.853f, 241.9238f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1550,
        Terminal.Constructor(Vector3(3657.445f, 1721.234f, 241.9238f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        2131,
        SpawnTube.Constructor(Vector3(3646.706f, 1707.742f, 240.0737f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 50
      )
      LocalObject(
        2132,
        SpawnTube.Constructor(Vector3(3646.706f, 1724.152f, 240.0737f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 50
      )
      LocalObject(
        1407,
        FacilityTurret.Constructor(Vector3(3623.32f, 1707.295f, 269.5338f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1407, 5063)
      LocalObject(
        1409,
        FacilityTurret.Constructor(Vector3(3658.647f, 1732.707f, 269.5338f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1409, 5064)
      LocalObject(
        1966,
        Painbox.Constructor(Vector3(3641.235f, 1713.803f, 242.0909f), painbox_radius_continuous),
        owning_building_guid = 50
      )
      LocalObject(
        1967,
        Painbox.Constructor(Vector3(3652.889f, 1722.086f, 240.6918f), painbox_radius_continuous),
        owning_building_guid = 50
      )
      LocalObject(
        1968,
        Painbox.Constructor(Vector3(3652.975f, 1710.223f, 240.6918f), painbox_radius_continuous),
        owning_building_guid = 50
      )
    }

    Building50()

    def Building50(): Unit = { // Name: Nerrivik_Tower Type: tower_a GUID: 51, MapID: 50
      LocalBuilding(
        "Nerrivik_Tower",
        51,
        50,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3724f, 3498f, 224.6254f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2196,
        CaptureTerminal.Constructor(Vector3(3740.587f, 3497.897f, 234.6244f), secondary_capture),
        owning_building_guid = 51
      )
      LocalObject(355, Door.Constructor(Vector3(3736f, 3490f, 226.1464f)), owning_building_guid = 51)
      LocalObject(356, Door.Constructor(Vector3(3736f, 3490f, 246.1454f)), owning_building_guid = 51)
      LocalObject(357, Door.Constructor(Vector3(3736f, 3506f, 226.1464f)), owning_building_guid = 51)
      LocalObject(358, Door.Constructor(Vector3(3736f, 3506f, 246.1454f)), owning_building_guid = 51)
      LocalObject(2313, Door.Constructor(Vector3(3735.146f, 3486.794f, 215.9614f)), owning_building_guid = 51)
      LocalObject(2314, Door.Constructor(Vector3(3735.146f, 3503.204f, 215.9614f)), owning_building_guid = 51)
      LocalObject(
        847,
        IFFLock.Constructor(Vector3(3733.957f, 3506.811f, 226.0864f), Vector3(0, 0, 0)),
        owning_building_guid = 51,
        door_guid = 357
      )
      LocalObject(
        848,
        IFFLock.Constructor(Vector3(3733.957f, 3506.811f, 246.0864f), Vector3(0, 0, 0)),
        owning_building_guid = 51,
        door_guid = 358
      )
      LocalObject(
        849,
        IFFLock.Constructor(Vector3(3738.047f, 3489.189f, 226.0864f), Vector3(0, 0, 180)),
        owning_building_guid = 51,
        door_guid = 355
      )
      LocalObject(
        850,
        IFFLock.Constructor(Vector3(3738.047f, 3489.189f, 246.0864f), Vector3(0, 0, 180)),
        owning_building_guid = 51,
        door_guid = 356
      )
      LocalObject(1132, Locker.Constructor(Vector3(3739.716f, 3482.963f, 214.6194f)), owning_building_guid = 51)
      LocalObject(1133, Locker.Constructor(Vector3(3739.751f, 3504.835f, 214.6194f)), owning_building_guid = 51)
      LocalObject(1134, Locker.Constructor(Vector3(3741.053f, 3482.963f, 214.6194f)), owning_building_guid = 51)
      LocalObject(1135, Locker.Constructor(Vector3(3741.088f, 3504.835f, 214.6194f)), owning_building_guid = 51)
      LocalObject(1136, Locker.Constructor(Vector3(3743.741f, 3482.963f, 214.6194f)), owning_building_guid = 51)
      LocalObject(1137, Locker.Constructor(Vector3(3743.741f, 3504.835f, 214.6194f)), owning_building_guid = 51)
      LocalObject(1138, Locker.Constructor(Vector3(3745.143f, 3482.963f, 214.6194f)), owning_building_guid = 51)
      LocalObject(1139, Locker.Constructor(Vector3(3745.143f, 3504.835f, 214.6194f)), owning_building_guid = 51)
      LocalObject(
        1558,
        Terminal.Constructor(Vector3(3745.445f, 3488.129f, 215.9574f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1559,
        Terminal.Constructor(Vector3(3745.445f, 3493.853f, 215.9574f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1560,
        Terminal.Constructor(Vector3(3745.445f, 3499.234f, 215.9574f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2136,
        SpawnTube.Constructor(Vector3(3734.706f, 3485.742f, 214.1074f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 51
      )
      LocalObject(
        2137,
        SpawnTube.Constructor(Vector3(3734.706f, 3502.152f, 214.1074f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 51
      )
      LocalObject(
        1412,
        FacilityTurret.Constructor(Vector3(3711.32f, 3485.295f, 243.5674f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1412, 5065)
      LocalObject(
        1414,
        FacilityTurret.Constructor(Vector3(3746.647f, 3510.707f, 243.5674f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1414, 5066)
      LocalObject(
        1969,
        Painbox.Constructor(Vector3(3729.235f, 3491.803f, 216.1245f), painbox_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        1970,
        Painbox.Constructor(Vector3(3740.889f, 3500.086f, 214.7254f), painbox_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        1971,
        Painbox.Constructor(Vector3(3740.975f, 3488.223f, 214.7254f), painbox_radius_continuous),
        owning_building_guid = 51
      )
    }

    Building28()

    def Building28(): Unit = { // Name: N_Keelut_Tower Type: tower_a GUID: 52, MapID: 28
      LocalBuilding(
        "N_Keelut_Tower",
        52,
        28,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3914f, 2464f, 243.5026f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2198,
        CaptureTerminal.Constructor(Vector3(3930.587f, 2463.897f, 253.5016f), secondary_capture),
        owning_building_guid = 52
      )
      LocalObject(377, Door.Constructor(Vector3(3926f, 2456f, 245.0236f)), owning_building_guid = 52)
      LocalObject(378, Door.Constructor(Vector3(3926f, 2456f, 265.0226f)), owning_building_guid = 52)
      LocalObject(379, Door.Constructor(Vector3(3926f, 2472f, 245.0236f)), owning_building_guid = 52)
      LocalObject(380, Door.Constructor(Vector3(3926f, 2472f, 265.0226f)), owning_building_guid = 52)
      LocalObject(2317, Door.Constructor(Vector3(3925.146f, 2452.794f, 234.8386f)), owning_building_guid = 52)
      LocalObject(2318, Door.Constructor(Vector3(3925.146f, 2469.204f, 234.8386f)), owning_building_guid = 52)
      LocalObject(
        861,
        IFFLock.Constructor(Vector3(3923.957f, 2472.811f, 244.9636f), Vector3(0, 0, 0)),
        owning_building_guid = 52,
        door_guid = 379
      )
      LocalObject(
        862,
        IFFLock.Constructor(Vector3(3923.957f, 2472.811f, 264.9636f), Vector3(0, 0, 0)),
        owning_building_guid = 52,
        door_guid = 380
      )
      LocalObject(
        864,
        IFFLock.Constructor(Vector3(3928.047f, 2455.189f, 244.9636f), Vector3(0, 0, 180)),
        owning_building_guid = 52,
        door_guid = 377
      )
      LocalObject(
        865,
        IFFLock.Constructor(Vector3(3928.047f, 2455.189f, 264.9636f), Vector3(0, 0, 180)),
        owning_building_guid = 52,
        door_guid = 378
      )
      LocalObject(1148, Locker.Constructor(Vector3(3929.716f, 2448.963f, 233.4966f)), owning_building_guid = 52)
      LocalObject(1149, Locker.Constructor(Vector3(3929.751f, 2470.835f, 233.4966f)), owning_building_guid = 52)
      LocalObject(1150, Locker.Constructor(Vector3(3931.053f, 2448.963f, 233.4966f)), owning_building_guid = 52)
      LocalObject(1151, Locker.Constructor(Vector3(3931.088f, 2470.835f, 233.4966f)), owning_building_guid = 52)
      LocalObject(1152, Locker.Constructor(Vector3(3933.741f, 2448.963f, 233.4966f)), owning_building_guid = 52)
      LocalObject(1153, Locker.Constructor(Vector3(3933.741f, 2470.835f, 233.4966f)), owning_building_guid = 52)
      LocalObject(1154, Locker.Constructor(Vector3(3935.143f, 2448.963f, 233.4966f)), owning_building_guid = 52)
      LocalObject(1155, Locker.Constructor(Vector3(3935.143f, 2470.835f, 233.4966f)), owning_building_guid = 52)
      LocalObject(
        1565,
        Terminal.Constructor(Vector3(3935.445f, 2454.129f, 234.8346f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1566,
        Terminal.Constructor(Vector3(3935.445f, 2459.853f, 234.8346f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1567,
        Terminal.Constructor(Vector3(3935.445f, 2465.234f, 234.8346f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        2140,
        SpawnTube.Constructor(Vector3(3924.706f, 2451.742f, 232.9846f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 52
      )
      LocalObject(
        2141,
        SpawnTube.Constructor(Vector3(3924.706f, 2468.152f, 232.9846f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 52
      )
      LocalObject(
        1420,
        FacilityTurret.Constructor(Vector3(3901.32f, 2451.295f, 262.4446f), manned_turret),
        owning_building_guid = 52
      )
      TurretToWeapon(1420, 5067)
      LocalObject(
        1424,
        FacilityTurret.Constructor(Vector3(3936.647f, 2476.707f, 262.4446f), manned_turret),
        owning_building_guid = 52
      )
      TurretToWeapon(1424, 5068)
      LocalObject(
        1975,
        Painbox.Constructor(Vector3(3919.235f, 2457.803f, 235.0017f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        1976,
        Painbox.Constructor(Vector3(3930.889f, 2466.086f, 233.6026f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        1977,
        Painbox.Constructor(Vector3(3930.975f, 2454.223f, 233.6026f), painbox_radius_continuous),
        owning_building_guid = 52
      )
    }

    Building47()

    def Building47(): Unit = { // Name: Sedna_Tower Type: tower_a GUID: 53, MapID: 47
      LocalBuilding(
        "Sedna_Tower",
        53,
        47,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4174f, 5364f, 226.9693f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2200,
        CaptureTerminal.Constructor(Vector3(4190.587f, 5363.897f, 236.9683f), secondary_capture),
        owning_building_guid = 53
      )
      LocalObject(415, Door.Constructor(Vector3(4186f, 5356f, 228.4903f)), owning_building_guid = 53)
      LocalObject(416, Door.Constructor(Vector3(4186f, 5356f, 248.4893f)), owning_building_guid = 53)
      LocalObject(417, Door.Constructor(Vector3(4186f, 5372f, 228.4903f)), owning_building_guid = 53)
      LocalObject(418, Door.Constructor(Vector3(4186f, 5372f, 248.4893f)), owning_building_guid = 53)
      LocalObject(2327, Door.Constructor(Vector3(4185.146f, 5352.794f, 218.3053f)), owning_building_guid = 53)
      LocalObject(2328, Door.Constructor(Vector3(4185.146f, 5369.204f, 218.3053f)), owning_building_guid = 53)
      LocalObject(
        892,
        IFFLock.Constructor(Vector3(4183.957f, 5372.811f, 228.4303f), Vector3(0, 0, 0)),
        owning_building_guid = 53,
        door_guid = 417
      )
      LocalObject(
        893,
        IFFLock.Constructor(Vector3(4183.957f, 5372.811f, 248.4303f), Vector3(0, 0, 0)),
        owning_building_guid = 53,
        door_guid = 418
      )
      LocalObject(
        894,
        IFFLock.Constructor(Vector3(4188.047f, 5355.189f, 228.4303f), Vector3(0, 0, 180)),
        owning_building_guid = 53,
        door_guid = 415
      )
      LocalObject(
        895,
        IFFLock.Constructor(Vector3(4188.047f, 5355.189f, 248.4303f), Vector3(0, 0, 180)),
        owning_building_guid = 53,
        door_guid = 416
      )
      LocalObject(1197, Locker.Constructor(Vector3(4189.716f, 5348.963f, 216.9633f)), owning_building_guid = 53)
      LocalObject(1198, Locker.Constructor(Vector3(4189.751f, 5370.835f, 216.9633f)), owning_building_guid = 53)
      LocalObject(1199, Locker.Constructor(Vector3(4191.053f, 5348.963f, 216.9633f)), owning_building_guid = 53)
      LocalObject(1200, Locker.Constructor(Vector3(4191.088f, 5370.835f, 216.9633f)), owning_building_guid = 53)
      LocalObject(1201, Locker.Constructor(Vector3(4193.741f, 5348.963f, 216.9633f)), owning_building_guid = 53)
      LocalObject(1202, Locker.Constructor(Vector3(4193.741f, 5370.835f, 216.9633f)), owning_building_guid = 53)
      LocalObject(1203, Locker.Constructor(Vector3(4195.143f, 5348.963f, 216.9633f)), owning_building_guid = 53)
      LocalObject(1204, Locker.Constructor(Vector3(4195.143f, 5370.835f, 216.9633f)), owning_building_guid = 53)
      LocalObject(
        1582,
        Terminal.Constructor(Vector3(4195.445f, 5354.129f, 218.3013f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1583,
        Terminal.Constructor(Vector3(4195.445f, 5359.853f, 218.3013f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1584,
        Terminal.Constructor(Vector3(4195.445f, 5365.234f, 218.3013f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2150,
        SpawnTube.Constructor(Vector3(4184.706f, 5351.742f, 216.4513f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 53
      )
      LocalObject(
        2151,
        SpawnTube.Constructor(Vector3(4184.706f, 5368.152f, 216.4513f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 53
      )
      LocalObject(
        1433,
        FacilityTurret.Constructor(Vector3(4161.32f, 5351.295f, 245.9113f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1433, 5069)
      LocalObject(
        1434,
        FacilityTurret.Constructor(Vector3(4196.647f, 5376.707f, 245.9113f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1434, 5070)
      LocalObject(
        1981,
        Painbox.Constructor(Vector3(4179.235f, 5357.803f, 218.4684f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        1982,
        Painbox.Constructor(Vector3(4190.889f, 5366.086f, 217.0693f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        1983,
        Painbox.Constructor(Vector3(4190.975f, 5354.223f, 217.0693f), painbox_radius_continuous),
        owning_building_guid = 53
      )
    }

    Building27()

    def Building27(): Unit = { // Name: S_Akna_Tower Type: tower_a GUID: 54, MapID: 27
      LocalBuilding(
        "S_Akna_Tower",
        54,
        27,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4254f, 3096f, 218.2696f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2201,
        CaptureTerminal.Constructor(Vector3(4270.587f, 3095.897f, 228.2686f), secondary_capture),
        owning_building_guid = 54
      )
      LocalObject(419, Door.Constructor(Vector3(4266f, 3088f, 219.7906f)), owning_building_guid = 54)
      LocalObject(420, Door.Constructor(Vector3(4266f, 3088f, 239.7896f)), owning_building_guid = 54)
      LocalObject(421, Door.Constructor(Vector3(4266f, 3104f, 219.7906f)), owning_building_guid = 54)
      LocalObject(422, Door.Constructor(Vector3(4266f, 3104f, 239.7896f)), owning_building_guid = 54)
      LocalObject(2329, Door.Constructor(Vector3(4265.146f, 3084.794f, 209.6056f)), owning_building_guid = 54)
      LocalObject(2330, Door.Constructor(Vector3(4265.146f, 3101.204f, 209.6056f)), owning_building_guid = 54)
      LocalObject(
        896,
        IFFLock.Constructor(Vector3(4263.957f, 3104.811f, 219.7306f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 421
      )
      LocalObject(
        897,
        IFFLock.Constructor(Vector3(4263.957f, 3104.811f, 239.7306f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 422
      )
      LocalObject(
        898,
        IFFLock.Constructor(Vector3(4268.047f, 3087.189f, 219.7306f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 419
      )
      LocalObject(
        899,
        IFFLock.Constructor(Vector3(4268.047f, 3087.189f, 239.7306f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 420
      )
      LocalObject(1205, Locker.Constructor(Vector3(4269.716f, 3080.963f, 208.2636f)), owning_building_guid = 54)
      LocalObject(1206, Locker.Constructor(Vector3(4269.751f, 3102.835f, 208.2636f)), owning_building_guid = 54)
      LocalObject(1207, Locker.Constructor(Vector3(4271.053f, 3080.963f, 208.2636f)), owning_building_guid = 54)
      LocalObject(1208, Locker.Constructor(Vector3(4271.088f, 3102.835f, 208.2636f)), owning_building_guid = 54)
      LocalObject(1209, Locker.Constructor(Vector3(4273.741f, 3080.963f, 208.2636f)), owning_building_guid = 54)
      LocalObject(1210, Locker.Constructor(Vector3(4273.741f, 3102.835f, 208.2636f)), owning_building_guid = 54)
      LocalObject(1211, Locker.Constructor(Vector3(4275.143f, 3080.963f, 208.2636f)), owning_building_guid = 54)
      LocalObject(1212, Locker.Constructor(Vector3(4275.143f, 3102.835f, 208.2636f)), owning_building_guid = 54)
      LocalObject(
        1585,
        Terminal.Constructor(Vector3(4275.445f, 3086.129f, 209.6016f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1586,
        Terminal.Constructor(Vector3(4275.445f, 3091.853f, 209.6016f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1587,
        Terminal.Constructor(Vector3(4275.445f, 3097.234f, 209.6016f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2152,
        SpawnTube.Constructor(Vector3(4264.706f, 3083.742f, 207.7516f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        2153,
        SpawnTube.Constructor(Vector3(4264.706f, 3100.152f, 207.7516f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        1435,
        FacilityTurret.Constructor(Vector3(4241.32f, 3083.295f, 237.2116f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(1435, 5071)
      LocalObject(
        1436,
        FacilityTurret.Constructor(Vector3(4276.647f, 3108.707f, 237.2116f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(1436, 5072)
      LocalObject(
        1984,
        Painbox.Constructor(Vector3(4259.235f, 3089.803f, 209.7687f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        1985,
        Painbox.Constructor(Vector3(4270.889f, 3098.086f, 208.3696f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        1986,
        Painbox.Constructor(Vector3(4270.975f, 3086.223f, 208.3696f), painbox_radius_continuous),
        owning_building_guid = 54
      )
    }

    Building22()

    def Building22(): Unit = { // Name: N_Hossin_Warpgate_Tower Type: tower_a GUID: 55, MapID: 22
      LocalBuilding(
        "N_Hossin_Warpgate_Tower",
        55,
        22,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4836f, 5218f, 22.86996f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2204,
        CaptureTerminal.Constructor(Vector3(4852.587f, 5217.897f, 32.86896f), secondary_capture),
        owning_building_guid = 55
      )
      LocalObject(449, Door.Constructor(Vector3(4848f, 5210f, 24.39096f)), owning_building_guid = 55)
      LocalObject(450, Door.Constructor(Vector3(4848f, 5210f, 44.38996f)), owning_building_guid = 55)
      LocalObject(451, Door.Constructor(Vector3(4848f, 5226f, 24.39096f)), owning_building_guid = 55)
      LocalObject(452, Door.Constructor(Vector3(4848f, 5226f, 44.38996f)), owning_building_guid = 55)
      LocalObject(2338, Door.Constructor(Vector3(4847.146f, 5206.794f, 14.20596f)), owning_building_guid = 55)
      LocalObject(2339, Door.Constructor(Vector3(4847.146f, 5223.204f, 14.20596f)), owning_building_guid = 55)
      LocalObject(
        919,
        IFFLock.Constructor(Vector3(4845.957f, 5226.811f, 24.33096f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 451
      )
      LocalObject(
        920,
        IFFLock.Constructor(Vector3(4845.957f, 5226.811f, 44.33096f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 452
      )
      LocalObject(
        921,
        IFFLock.Constructor(Vector3(4850.047f, 5209.189f, 24.33096f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 449
      )
      LocalObject(
        922,
        IFFLock.Constructor(Vector3(4850.047f, 5209.189f, 44.33096f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 450
      )
      LocalObject(1241, Locker.Constructor(Vector3(4851.716f, 5202.963f, 12.86396f)), owning_building_guid = 55)
      LocalObject(1242, Locker.Constructor(Vector3(4851.751f, 5224.835f, 12.86396f)), owning_building_guid = 55)
      LocalObject(1243, Locker.Constructor(Vector3(4853.053f, 5202.963f, 12.86396f)), owning_building_guid = 55)
      LocalObject(1244, Locker.Constructor(Vector3(4853.088f, 5224.835f, 12.86396f)), owning_building_guid = 55)
      LocalObject(1245, Locker.Constructor(Vector3(4855.741f, 5202.963f, 12.86396f)), owning_building_guid = 55)
      LocalObject(1246, Locker.Constructor(Vector3(4855.741f, 5224.835f, 12.86396f)), owning_building_guid = 55)
      LocalObject(1247, Locker.Constructor(Vector3(4857.143f, 5202.963f, 12.86396f)), owning_building_guid = 55)
      LocalObject(1248, Locker.Constructor(Vector3(4857.143f, 5224.835f, 12.86396f)), owning_building_guid = 55)
      LocalObject(
        1600,
        Terminal.Constructor(Vector3(4857.445f, 5208.129f, 14.20196f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1601,
        Terminal.Constructor(Vector3(4857.445f, 5213.853f, 14.20196f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1602,
        Terminal.Constructor(Vector3(4857.445f, 5219.234f, 14.20196f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        2161,
        SpawnTube.Constructor(Vector3(4846.706f, 5205.742f, 12.35196f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        2162,
        SpawnTube.Constructor(Vector3(4846.706f, 5222.152f, 12.35196f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        1447,
        FacilityTurret.Constructor(Vector3(4823.32f, 5205.295f, 41.81196f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1447, 5073)
      LocalObject(
        1448,
        FacilityTurret.Constructor(Vector3(4858.647f, 5230.707f, 41.81196f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1448, 5074)
      LocalObject(
        1993,
        Painbox.Constructor(Vector3(4841.235f, 5211.803f, 14.36906f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        1994,
        Painbox.Constructor(Vector3(4852.889f, 5220.086f, 12.96996f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        1995,
        Painbox.Constructor(Vector3(4852.975f, 5208.223f, 12.96996f), painbox_radius_continuous),
        owning_building_guid = 55
      )
    }

    Building52()

    def Building52(): Unit = { // Name: Tootega_Tower Type: tower_a GUID: 56, MapID: 52
      LocalBuilding(
        "Tootega_Tower",
        56,
        52,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5030f, 3086f, 231.7553f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2206,
        CaptureTerminal.Constructor(Vector3(5046.587f, 3085.897f, 241.7543f), secondary_capture),
        owning_building_guid = 56
      )
      LocalObject(462, Door.Constructor(Vector3(5042f, 3078f, 233.2763f)), owning_building_guid = 56)
      LocalObject(463, Door.Constructor(Vector3(5042f, 3078f, 253.2753f)), owning_building_guid = 56)
      LocalObject(464, Door.Constructor(Vector3(5042f, 3094f, 233.2763f)), owning_building_guid = 56)
      LocalObject(465, Door.Constructor(Vector3(5042f, 3094f, 253.2753f)), owning_building_guid = 56)
      LocalObject(2342, Door.Constructor(Vector3(5041.146f, 3074.794f, 223.0913f)), owning_building_guid = 56)
      LocalObject(2343, Door.Constructor(Vector3(5041.146f, 3091.204f, 223.0913f)), owning_building_guid = 56)
      LocalObject(
        930,
        IFFLock.Constructor(Vector3(5039.957f, 3094.811f, 233.2163f), Vector3(0, 0, 0)),
        owning_building_guid = 56,
        door_guid = 464
      )
      LocalObject(
        931,
        IFFLock.Constructor(Vector3(5039.957f, 3094.811f, 253.2163f), Vector3(0, 0, 0)),
        owning_building_guid = 56,
        door_guid = 465
      )
      LocalObject(
        932,
        IFFLock.Constructor(Vector3(5044.047f, 3077.189f, 233.2163f), Vector3(0, 0, 180)),
        owning_building_guid = 56,
        door_guid = 462
      )
      LocalObject(
        933,
        IFFLock.Constructor(Vector3(5044.047f, 3077.189f, 253.2163f), Vector3(0, 0, 180)),
        owning_building_guid = 56,
        door_guid = 463
      )
      LocalObject(1257, Locker.Constructor(Vector3(5045.716f, 3070.963f, 221.7493f)), owning_building_guid = 56)
      LocalObject(1258, Locker.Constructor(Vector3(5045.751f, 3092.835f, 221.7493f)), owning_building_guid = 56)
      LocalObject(1259, Locker.Constructor(Vector3(5047.053f, 3070.963f, 221.7493f)), owning_building_guid = 56)
      LocalObject(1260, Locker.Constructor(Vector3(5047.088f, 3092.835f, 221.7493f)), owning_building_guid = 56)
      LocalObject(1261, Locker.Constructor(Vector3(5049.741f, 3070.963f, 221.7493f)), owning_building_guid = 56)
      LocalObject(1262, Locker.Constructor(Vector3(5049.741f, 3092.835f, 221.7493f)), owning_building_guid = 56)
      LocalObject(1263, Locker.Constructor(Vector3(5051.143f, 3070.963f, 221.7493f)), owning_building_guid = 56)
      LocalObject(1264, Locker.Constructor(Vector3(5051.143f, 3092.835f, 221.7493f)), owning_building_guid = 56)
      LocalObject(
        1606,
        Terminal.Constructor(Vector3(5051.445f, 3076.129f, 223.0873f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1607,
        Terminal.Constructor(Vector3(5051.445f, 3081.853f, 223.0873f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1608,
        Terminal.Constructor(Vector3(5051.445f, 3087.234f, 223.0873f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2165,
        SpawnTube.Constructor(Vector3(5040.706f, 3073.742f, 221.2373f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 56
      )
      LocalObject(
        2166,
        SpawnTube.Constructor(Vector3(5040.706f, 3090.152f, 221.2373f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 56
      )
      LocalObject(
        1451,
        FacilityTurret.Constructor(Vector3(5017.32f, 3073.295f, 250.6973f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1451, 5075)
      LocalObject(
        1452,
        FacilityTurret.Constructor(Vector3(5052.647f, 3098.707f, 250.6973f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1452, 5076)
      LocalObject(
        1999,
        Painbox.Constructor(Vector3(5035.235f, 3079.803f, 223.2544f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2000,
        Painbox.Constructor(Vector3(5046.889f, 3088.086f, 221.8553f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2001,
        Painbox.Constructor(Vector3(5046.975f, 3076.223f, 221.8553f), painbox_radius_continuous),
        owning_building_guid = 56
      )
    }

    Building15()

    def Building15(): Unit = { // Name: SW_Amerish_Warpgate_Tower Type: tower_b GUID: 57, MapID: 15
      LocalBuilding(
        "SW_Amerish_Warpgate_Tower",
        57,
        15,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2040f, 1690f, 219.3832f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2184,
        CaptureTerminal.Constructor(Vector3(2056.587f, 1689.897f, 239.3822f), secondary_capture),
        owning_building_guid = 57
      )
      LocalObject(250, Door.Constructor(Vector3(2052f, 1682f, 220.9032f)), owning_building_guid = 57)
      LocalObject(251, Door.Constructor(Vector3(2052f, 1682f, 230.9032f)), owning_building_guid = 57)
      LocalObject(252, Door.Constructor(Vector3(2052f, 1682f, 250.9032f)), owning_building_guid = 57)
      LocalObject(253, Door.Constructor(Vector3(2052f, 1698f, 220.9032f)), owning_building_guid = 57)
      LocalObject(254, Door.Constructor(Vector3(2052f, 1698f, 230.9032f)), owning_building_guid = 57)
      LocalObject(255, Door.Constructor(Vector3(2052f, 1698f, 250.9032f)), owning_building_guid = 57)
      LocalObject(2277, Door.Constructor(Vector3(2051.147f, 1678.794f, 210.7192f)), owning_building_guid = 57)
      LocalObject(2278, Door.Constructor(Vector3(2051.147f, 1695.204f, 210.7192f)), owning_building_guid = 57)
      LocalObject(
        763,
        IFFLock.Constructor(Vector3(2049.957f, 1698.811f, 220.8442f), Vector3(0, 0, 0)),
        owning_building_guid = 57,
        door_guid = 253
      )
      LocalObject(
        764,
        IFFLock.Constructor(Vector3(2049.957f, 1698.811f, 230.8442f), Vector3(0, 0, 0)),
        owning_building_guid = 57,
        door_guid = 254
      )
      LocalObject(
        765,
        IFFLock.Constructor(Vector3(2049.957f, 1698.811f, 250.8442f), Vector3(0, 0, 0)),
        owning_building_guid = 57,
        door_guid = 255
      )
      LocalObject(
        766,
        IFFLock.Constructor(Vector3(2054.047f, 1681.189f, 220.8442f), Vector3(0, 0, 180)),
        owning_building_guid = 57,
        door_guid = 250
      )
      LocalObject(
        767,
        IFFLock.Constructor(Vector3(2054.047f, 1681.189f, 230.8442f), Vector3(0, 0, 180)),
        owning_building_guid = 57,
        door_guid = 251
      )
      LocalObject(
        768,
        IFFLock.Constructor(Vector3(2054.047f, 1681.189f, 250.8442f), Vector3(0, 0, 180)),
        owning_building_guid = 57,
        door_guid = 252
      )
      LocalObject(979, Locker.Constructor(Vector3(2055.716f, 1674.963f, 209.3772f)), owning_building_guid = 57)
      LocalObject(980, Locker.Constructor(Vector3(2055.751f, 1696.835f, 209.3772f)), owning_building_guid = 57)
      LocalObject(981, Locker.Constructor(Vector3(2057.053f, 1674.963f, 209.3772f)), owning_building_guid = 57)
      LocalObject(982, Locker.Constructor(Vector3(2057.088f, 1696.835f, 209.3772f)), owning_building_guid = 57)
      LocalObject(983, Locker.Constructor(Vector3(2059.741f, 1674.963f, 209.3772f)), owning_building_guid = 57)
      LocalObject(984, Locker.Constructor(Vector3(2059.741f, 1696.835f, 209.3772f)), owning_building_guid = 57)
      LocalObject(985, Locker.Constructor(Vector3(2061.143f, 1674.963f, 209.3772f)), owning_building_guid = 57)
      LocalObject(986, Locker.Constructor(Vector3(2061.143f, 1696.835f, 209.3772f)), owning_building_guid = 57)
      LocalObject(
        1503,
        Terminal.Constructor(Vector3(2061.446f, 1680.129f, 210.7152f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1504,
        Terminal.Constructor(Vector3(2061.446f, 1685.853f, 210.7152f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1505,
        Terminal.Constructor(Vector3(2061.446f, 1691.234f, 210.7152f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        2100,
        SpawnTube.Constructor(Vector3(2050.706f, 1677.742f, 208.8652f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 57
      )
      LocalObject(
        2101,
        SpawnTube.Constructor(Vector3(2050.706f, 1694.152f, 208.8652f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 57
      )
      LocalObject(
        1933,
        Painbox.Constructor(Vector3(2045.493f, 1682.849f, 210.6726f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        1934,
        Painbox.Constructor(Vector3(2057.127f, 1680.078f, 209.4832f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        1935,
        Painbox.Constructor(Vector3(2057.259f, 1692.107f, 209.4832f), painbox_radius_continuous),
        owning_building_guid = 57
      )
    }

    Building26()

    def Building26(): Unit = { // Name: SW_Nerrivik_Tower Type: tower_b GUID: 58, MapID: 26
      LocalBuilding(
        "SW_Nerrivik_Tower",
        58,
        26,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3282f, 2966f, 243.6248f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2192,
        CaptureTerminal.Constructor(Vector3(3298.587f, 2965.897f, 263.6238f), secondary_capture),
        owning_building_guid = 58
      )
      LocalObject(305, Door.Constructor(Vector3(3294f, 2958f, 245.1448f)), owning_building_guid = 58)
      LocalObject(306, Door.Constructor(Vector3(3294f, 2958f, 255.1448f)), owning_building_guid = 58)
      LocalObject(307, Door.Constructor(Vector3(3294f, 2958f, 275.1448f)), owning_building_guid = 58)
      LocalObject(308, Door.Constructor(Vector3(3294f, 2974f, 245.1448f)), owning_building_guid = 58)
      LocalObject(309, Door.Constructor(Vector3(3294f, 2974f, 255.1448f)), owning_building_guid = 58)
      LocalObject(310, Door.Constructor(Vector3(3294f, 2974f, 275.1448f)), owning_building_guid = 58)
      LocalObject(2299, Door.Constructor(Vector3(3293.147f, 2954.794f, 234.9608f)), owning_building_guid = 58)
      LocalObject(2300, Door.Constructor(Vector3(3293.147f, 2971.204f, 234.9608f)), owning_building_guid = 58)
      LocalObject(
        810,
        IFFLock.Constructor(Vector3(3291.957f, 2974.811f, 245.0858f), Vector3(0, 0, 0)),
        owning_building_guid = 58,
        door_guid = 308
      )
      LocalObject(
        811,
        IFFLock.Constructor(Vector3(3291.957f, 2974.811f, 255.0858f), Vector3(0, 0, 0)),
        owning_building_guid = 58,
        door_guid = 309
      )
      LocalObject(
        812,
        IFFLock.Constructor(Vector3(3291.957f, 2974.811f, 275.0858f), Vector3(0, 0, 0)),
        owning_building_guid = 58,
        door_guid = 310
      )
      LocalObject(
        813,
        IFFLock.Constructor(Vector3(3296.047f, 2957.189f, 245.0858f), Vector3(0, 0, 180)),
        owning_building_guid = 58,
        door_guid = 305
      )
      LocalObject(
        814,
        IFFLock.Constructor(Vector3(3296.047f, 2957.189f, 255.0858f), Vector3(0, 0, 180)),
        owning_building_guid = 58,
        door_guid = 306
      )
      LocalObject(
        815,
        IFFLock.Constructor(Vector3(3296.047f, 2957.189f, 275.0858f), Vector3(0, 0, 180)),
        owning_building_guid = 58,
        door_guid = 307
      )
      LocalObject(1076, Locker.Constructor(Vector3(3297.716f, 2950.963f, 233.6188f)), owning_building_guid = 58)
      LocalObject(1077, Locker.Constructor(Vector3(3297.751f, 2972.835f, 233.6188f)), owning_building_guid = 58)
      LocalObject(1078, Locker.Constructor(Vector3(3299.053f, 2950.963f, 233.6188f)), owning_building_guid = 58)
      LocalObject(1079, Locker.Constructor(Vector3(3299.088f, 2972.835f, 233.6188f)), owning_building_guid = 58)
      LocalObject(1080, Locker.Constructor(Vector3(3301.741f, 2950.963f, 233.6188f)), owning_building_guid = 58)
      LocalObject(1081, Locker.Constructor(Vector3(3301.741f, 2972.835f, 233.6188f)), owning_building_guid = 58)
      LocalObject(1082, Locker.Constructor(Vector3(3303.143f, 2950.963f, 233.6188f)), owning_building_guid = 58)
      LocalObject(1083, Locker.Constructor(Vector3(3303.143f, 2972.835f, 233.6188f)), owning_building_guid = 58)
      LocalObject(
        1535,
        Terminal.Constructor(Vector3(3303.446f, 2956.129f, 234.9568f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1536,
        Terminal.Constructor(Vector3(3303.446f, 2961.853f, 234.9568f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1537,
        Terminal.Constructor(Vector3(3303.446f, 2967.234f, 234.9568f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        2122,
        SpawnTube.Constructor(Vector3(3292.706f, 2953.742f, 233.1068f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 58
      )
      LocalObject(
        2123,
        SpawnTube.Constructor(Vector3(3292.706f, 2970.152f, 233.1068f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 58
      )
      LocalObject(
        1957,
        Painbox.Constructor(Vector3(3287.493f, 2958.849f, 234.9142f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        1958,
        Painbox.Constructor(Vector3(3299.127f, 2956.078f, 233.7248f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        1959,
        Painbox.Constructor(Vector3(3299.259f, 2968.107f, 233.7248f), painbox_radius_continuous),
        owning_building_guid = 58
      )
    }

    Building48()

    def Building48(): Unit = { // Name: Anguta_Tower Type: tower_b GUID: 59, MapID: 48
      LocalBuilding(
        "Anguta_Tower",
        59,
        48,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3860f, 4518f, 268.7284f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2197,
        CaptureTerminal.Constructor(Vector3(3876.587f, 4517.897f, 288.7274f), secondary_capture),
        owning_building_guid = 59
      )
      LocalObject(364, Door.Constructor(Vector3(3872f, 4510f, 270.2484f)), owning_building_guid = 59)
      LocalObject(365, Door.Constructor(Vector3(3872f, 4510f, 280.2484f)), owning_building_guid = 59)
      LocalObject(366, Door.Constructor(Vector3(3872f, 4510f, 300.2484f)), owning_building_guid = 59)
      LocalObject(367, Door.Constructor(Vector3(3872f, 4526f, 270.2484f)), owning_building_guid = 59)
      LocalObject(368, Door.Constructor(Vector3(3872f, 4526f, 280.2484f)), owning_building_guid = 59)
      LocalObject(369, Door.Constructor(Vector3(3872f, 4526f, 300.2484f)), owning_building_guid = 59)
      LocalObject(2315, Door.Constructor(Vector3(3871.147f, 4506.794f, 260.0644f)), owning_building_guid = 59)
      LocalObject(2316, Door.Constructor(Vector3(3871.147f, 4523.204f, 260.0644f)), owning_building_guid = 59)
      LocalObject(
        854,
        IFFLock.Constructor(Vector3(3869.957f, 4526.811f, 270.1894f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 367
      )
      LocalObject(
        855,
        IFFLock.Constructor(Vector3(3869.957f, 4526.811f, 280.1894f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 368
      )
      LocalObject(
        856,
        IFFLock.Constructor(Vector3(3869.957f, 4526.811f, 300.1894f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 369
      )
      LocalObject(
        857,
        IFFLock.Constructor(Vector3(3874.047f, 4509.189f, 270.1894f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 364
      )
      LocalObject(
        858,
        IFFLock.Constructor(Vector3(3874.047f, 4509.189f, 280.1894f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 365
      )
      LocalObject(
        859,
        IFFLock.Constructor(Vector3(3874.047f, 4509.189f, 300.1894f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 366
      )
      LocalObject(1140, Locker.Constructor(Vector3(3875.716f, 4502.963f, 258.7224f)), owning_building_guid = 59)
      LocalObject(1141, Locker.Constructor(Vector3(3875.751f, 4524.835f, 258.7224f)), owning_building_guid = 59)
      LocalObject(1142, Locker.Constructor(Vector3(3877.053f, 4502.963f, 258.7224f)), owning_building_guid = 59)
      LocalObject(1143, Locker.Constructor(Vector3(3877.088f, 4524.835f, 258.7224f)), owning_building_guid = 59)
      LocalObject(1144, Locker.Constructor(Vector3(3879.741f, 4502.963f, 258.7224f)), owning_building_guid = 59)
      LocalObject(1145, Locker.Constructor(Vector3(3879.741f, 4524.835f, 258.7224f)), owning_building_guid = 59)
      LocalObject(1146, Locker.Constructor(Vector3(3881.143f, 4502.963f, 258.7224f)), owning_building_guid = 59)
      LocalObject(1147, Locker.Constructor(Vector3(3881.143f, 4524.835f, 258.7224f)), owning_building_guid = 59)
      LocalObject(
        1561,
        Terminal.Constructor(Vector3(3881.446f, 4508.129f, 260.0604f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1562,
        Terminal.Constructor(Vector3(3881.446f, 4513.853f, 260.0604f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1563,
        Terminal.Constructor(Vector3(3881.446f, 4519.234f, 260.0604f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        2138,
        SpawnTube.Constructor(Vector3(3870.706f, 4505.742f, 258.2104f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        2139,
        SpawnTube.Constructor(Vector3(3870.706f, 4522.152f, 258.2104f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        1972,
        Painbox.Constructor(Vector3(3865.493f, 4510.849f, 260.0178f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        1973,
        Painbox.Constructor(Vector3(3877.127f, 4508.078f, 258.8284f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        1974,
        Painbox.Constructor(Vector3(3877.259f, 4520.107f, 258.8284f), painbox_radius_continuous),
        owning_building_guid = 59
      )
    }

    Building31()

    def Building31(): Unit = { // Name: N_Sedna_Tower Type: tower_b GUID: 60, MapID: 31
      LocalBuilding(
        "N_Sedna_Tower",
        60,
        31,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4088f, 5718f, 141.2804f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2199,
        CaptureTerminal.Constructor(Vector3(4104.587f, 5717.897f, 161.2794f), secondary_capture),
        owning_building_guid = 60
      )
      LocalObject(409, Door.Constructor(Vector3(4100f, 5710f, 142.8004f)), owning_building_guid = 60)
      LocalObject(410, Door.Constructor(Vector3(4100f, 5710f, 152.8004f)), owning_building_guid = 60)
      LocalObject(411, Door.Constructor(Vector3(4100f, 5710f, 172.8004f)), owning_building_guid = 60)
      LocalObject(412, Door.Constructor(Vector3(4100f, 5726f, 142.8004f)), owning_building_guid = 60)
      LocalObject(413, Door.Constructor(Vector3(4100f, 5726f, 152.8004f)), owning_building_guid = 60)
      LocalObject(414, Door.Constructor(Vector3(4100f, 5726f, 172.8004f)), owning_building_guid = 60)
      LocalObject(2325, Door.Constructor(Vector3(4099.147f, 5706.794f, 132.6164f)), owning_building_guid = 60)
      LocalObject(2326, Door.Constructor(Vector3(4099.147f, 5723.204f, 132.6164f)), owning_building_guid = 60)
      LocalObject(
        886,
        IFFLock.Constructor(Vector3(4097.957f, 5726.811f, 142.7414f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 412
      )
      LocalObject(
        887,
        IFFLock.Constructor(Vector3(4097.957f, 5726.811f, 152.7414f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 413
      )
      LocalObject(
        888,
        IFFLock.Constructor(Vector3(4097.957f, 5726.811f, 172.7414f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 414
      )
      LocalObject(
        889,
        IFFLock.Constructor(Vector3(4102.047f, 5709.189f, 142.7414f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 409
      )
      LocalObject(
        890,
        IFFLock.Constructor(Vector3(4102.047f, 5709.189f, 152.7414f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 410
      )
      LocalObject(
        891,
        IFFLock.Constructor(Vector3(4102.047f, 5709.189f, 172.7414f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 411
      )
      LocalObject(1189, Locker.Constructor(Vector3(4103.716f, 5702.963f, 131.2744f)), owning_building_guid = 60)
      LocalObject(1190, Locker.Constructor(Vector3(4103.751f, 5724.835f, 131.2744f)), owning_building_guid = 60)
      LocalObject(1191, Locker.Constructor(Vector3(4105.053f, 5702.963f, 131.2744f)), owning_building_guid = 60)
      LocalObject(1192, Locker.Constructor(Vector3(4105.088f, 5724.835f, 131.2744f)), owning_building_guid = 60)
      LocalObject(1193, Locker.Constructor(Vector3(4107.741f, 5702.963f, 131.2744f)), owning_building_guid = 60)
      LocalObject(1194, Locker.Constructor(Vector3(4107.741f, 5724.835f, 131.2744f)), owning_building_guid = 60)
      LocalObject(1195, Locker.Constructor(Vector3(4109.143f, 5702.963f, 131.2744f)), owning_building_guid = 60)
      LocalObject(1196, Locker.Constructor(Vector3(4109.143f, 5724.835f, 131.2744f)), owning_building_guid = 60)
      LocalObject(
        1579,
        Terminal.Constructor(Vector3(4109.446f, 5708.129f, 132.6124f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1580,
        Terminal.Constructor(Vector3(4109.446f, 5713.853f, 132.6124f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1581,
        Terminal.Constructor(Vector3(4109.446f, 5719.234f, 132.6124f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        2148,
        SpawnTube.Constructor(Vector3(4098.706f, 5705.742f, 130.7624f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        2149,
        SpawnTube.Constructor(Vector3(4098.706f, 5722.152f, 130.7624f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        1978,
        Painbox.Constructor(Vector3(4093.493f, 5710.849f, 132.5698f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        1979,
        Painbox.Constructor(Vector3(4105.127f, 5708.078f, 131.3804f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        1980,
        Painbox.Constructor(Vector3(4105.259f, 5720.107f, 131.3804f), painbox_radius_continuous),
        owning_building_guid = 60
      )
    }

    Building18()

    def Building18(): Unit = { // Name: N_Ishundar_Warpgate_Tower Type: tower_b GUID: 61, MapID: 18
      LocalBuilding(
        "N_Ishundar_Warpgate_Tower",
        61,
        18,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4870f, 2432f, 20.75137f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2205,
        CaptureTerminal.Constructor(Vector3(4886.587f, 2431.897f, 40.75037f), secondary_capture),
        owning_building_guid = 61
      )
      LocalObject(453, Door.Constructor(Vector3(4882f, 2424f, 22.27137f)), owning_building_guid = 61)
      LocalObject(454, Door.Constructor(Vector3(4882f, 2424f, 32.27137f)), owning_building_guid = 61)
      LocalObject(455, Door.Constructor(Vector3(4882f, 2424f, 52.27137f)), owning_building_guid = 61)
      LocalObject(456, Door.Constructor(Vector3(4882f, 2440f, 22.27137f)), owning_building_guid = 61)
      LocalObject(457, Door.Constructor(Vector3(4882f, 2440f, 32.27137f)), owning_building_guid = 61)
      LocalObject(458, Door.Constructor(Vector3(4882f, 2440f, 52.27137f)), owning_building_guid = 61)
      LocalObject(2340, Door.Constructor(Vector3(4881.147f, 2420.794f, 12.08737f)), owning_building_guid = 61)
      LocalObject(2341, Door.Constructor(Vector3(4881.147f, 2437.204f, 12.08737f)), owning_building_guid = 61)
      LocalObject(
        923,
        IFFLock.Constructor(Vector3(4879.957f, 2440.811f, 22.21237f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 456
      )
      LocalObject(
        924,
        IFFLock.Constructor(Vector3(4879.957f, 2440.811f, 32.21236f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 457
      )
      LocalObject(
        925,
        IFFLock.Constructor(Vector3(4879.957f, 2440.811f, 52.21236f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 458
      )
      LocalObject(
        926,
        IFFLock.Constructor(Vector3(4884.047f, 2423.189f, 22.21237f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 453
      )
      LocalObject(
        927,
        IFFLock.Constructor(Vector3(4884.047f, 2423.189f, 32.21236f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 454
      )
      LocalObject(
        928,
        IFFLock.Constructor(Vector3(4884.047f, 2423.189f, 52.21236f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 455
      )
      LocalObject(1249, Locker.Constructor(Vector3(4885.716f, 2416.963f, 10.74537f)), owning_building_guid = 61)
      LocalObject(1250, Locker.Constructor(Vector3(4885.751f, 2438.835f, 10.74537f)), owning_building_guid = 61)
      LocalObject(1251, Locker.Constructor(Vector3(4887.053f, 2416.963f, 10.74537f)), owning_building_guid = 61)
      LocalObject(1252, Locker.Constructor(Vector3(4887.088f, 2438.835f, 10.74537f)), owning_building_guid = 61)
      LocalObject(1253, Locker.Constructor(Vector3(4889.741f, 2416.963f, 10.74537f)), owning_building_guid = 61)
      LocalObject(1254, Locker.Constructor(Vector3(4889.741f, 2438.835f, 10.74537f)), owning_building_guid = 61)
      LocalObject(1255, Locker.Constructor(Vector3(4891.143f, 2416.963f, 10.74537f)), owning_building_guid = 61)
      LocalObject(1256, Locker.Constructor(Vector3(4891.143f, 2438.835f, 10.74537f)), owning_building_guid = 61)
      LocalObject(
        1603,
        Terminal.Constructor(Vector3(4891.446f, 2422.129f, 12.08337f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1604,
        Terminal.Constructor(Vector3(4891.446f, 2427.853f, 12.08337f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1605,
        Terminal.Constructor(Vector3(4891.446f, 2433.234f, 12.08337f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        2163,
        SpawnTube.Constructor(Vector3(4880.706f, 2419.742f, 10.23337f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        2164,
        SpawnTube.Constructor(Vector3(4880.706f, 2436.152f, 10.23337f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        1996,
        Painbox.Constructor(Vector3(4875.493f, 2424.849f, 12.04077f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        1997,
        Painbox.Constructor(Vector3(4887.127f, 2422.078f, 10.85137f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        1998,
        Painbox.Constructor(Vector3(4887.259f, 2434.107f, 10.85137f), painbox_radius_continuous),
        owning_building_guid = 61
      )
    }

    Building20()

    def Building20(): Unit = { // Name: N_Tootega_Tower Type: tower_b GUID: 62, MapID: 20
      LocalBuilding(
        "N_Tootega_Tower",
        62,
        20,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5120f, 3880f, 234.6615f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2207,
        CaptureTerminal.Constructor(Vector3(5136.587f, 3879.897f, 254.6606f), secondary_capture),
        owning_building_guid = 62
      )
      LocalObject(473, Door.Constructor(Vector3(5132f, 3872f, 236.1815f)), owning_building_guid = 62)
      LocalObject(474, Door.Constructor(Vector3(5132f, 3872f, 246.1815f)), owning_building_guid = 62)
      LocalObject(475, Door.Constructor(Vector3(5132f, 3872f, 266.1815f)), owning_building_guid = 62)
      LocalObject(476, Door.Constructor(Vector3(5132f, 3888f, 236.1815f)), owning_building_guid = 62)
      LocalObject(477, Door.Constructor(Vector3(5132f, 3888f, 246.1815f)), owning_building_guid = 62)
      LocalObject(478, Door.Constructor(Vector3(5132f, 3888f, 266.1815f)), owning_building_guid = 62)
      LocalObject(2347, Door.Constructor(Vector3(5131.147f, 3868.794f, 225.9975f)), owning_building_guid = 62)
      LocalObject(2348, Door.Constructor(Vector3(5131.147f, 3885.204f, 225.9975f)), owning_building_guid = 62)
      LocalObject(
        938,
        IFFLock.Constructor(Vector3(5129.957f, 3888.811f, 236.1225f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 476
      )
      LocalObject(
        939,
        IFFLock.Constructor(Vector3(5129.957f, 3888.811f, 246.1225f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 477
      )
      LocalObject(
        940,
        IFFLock.Constructor(Vector3(5129.957f, 3888.811f, 266.1226f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 478
      )
      LocalObject(
        942,
        IFFLock.Constructor(Vector3(5134.047f, 3871.189f, 236.1225f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 473
      )
      LocalObject(
        943,
        IFFLock.Constructor(Vector3(5134.047f, 3871.189f, 246.1225f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 474
      )
      LocalObject(
        944,
        IFFLock.Constructor(Vector3(5134.047f, 3871.189f, 266.1226f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 475
      )
      LocalObject(1281, Locker.Constructor(Vector3(5135.716f, 3864.963f, 224.6555f)), owning_building_guid = 62)
      LocalObject(1282, Locker.Constructor(Vector3(5135.751f, 3886.835f, 224.6555f)), owning_building_guid = 62)
      LocalObject(1283, Locker.Constructor(Vector3(5137.053f, 3864.963f, 224.6555f)), owning_building_guid = 62)
      LocalObject(1284, Locker.Constructor(Vector3(5137.088f, 3886.835f, 224.6555f)), owning_building_guid = 62)
      LocalObject(1285, Locker.Constructor(Vector3(5139.741f, 3864.963f, 224.6555f)), owning_building_guid = 62)
      LocalObject(1286, Locker.Constructor(Vector3(5139.741f, 3886.835f, 224.6555f)), owning_building_guid = 62)
      LocalObject(1287, Locker.Constructor(Vector3(5141.143f, 3864.963f, 224.6555f)), owning_building_guid = 62)
      LocalObject(1288, Locker.Constructor(Vector3(5141.143f, 3886.835f, 224.6555f)), owning_building_guid = 62)
      LocalObject(
        1613,
        Terminal.Constructor(Vector3(5141.446f, 3870.129f, 225.9935f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        1614,
        Terminal.Constructor(Vector3(5141.446f, 3875.853f, 225.9935f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        1615,
        Terminal.Constructor(Vector3(5141.446f, 3881.234f, 225.9935f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        2170,
        SpawnTube.Constructor(Vector3(5130.706f, 3867.742f, 224.1435f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        2171,
        SpawnTube.Constructor(Vector3(5130.706f, 3884.152f, 224.1435f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        2002,
        Painbox.Constructor(Vector3(5125.493f, 3872.849f, 225.9509f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2003,
        Painbox.Constructor(Vector3(5137.127f, 3870.078f, 224.7616f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2004,
        Painbox.Constructor(Vector3(5137.259f, 3882.107f, 224.7616f), painbox_radius_continuous),
        owning_building_guid = 62
      )
    }

    Building29()

    def Building29(): Unit = { // Name: E_Forseral_Warpgate_Tower Type: tower_c GUID: 63, MapID: 29
      LocalBuilding(
        "E_Forseral_Warpgate_Tower",
        63,
        29,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2372f, 5710f, 33.28257f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2185,
        CaptureTerminal.Constructor(Vector3(2388.587f, 5709.897f, 43.28157f), secondary_capture),
        owning_building_guid = 63
      )
      LocalObject(256, Door.Constructor(Vector3(2384f, 5702f, 34.80357f)), owning_building_guid = 63)
      LocalObject(257, Door.Constructor(Vector3(2384f, 5702f, 54.80257f)), owning_building_guid = 63)
      LocalObject(258, Door.Constructor(Vector3(2384f, 5718f, 34.80357f)), owning_building_guid = 63)
      LocalObject(259, Door.Constructor(Vector3(2384f, 5718f, 54.80257f)), owning_building_guid = 63)
      LocalObject(2279, Door.Constructor(Vector3(2383.146f, 5698.794f, 24.61857f)), owning_building_guid = 63)
      LocalObject(2280, Door.Constructor(Vector3(2383.146f, 5715.204f, 24.61857f)), owning_building_guid = 63)
      LocalObject(
        769,
        IFFLock.Constructor(Vector3(2381.957f, 5718.811f, 34.74357f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 258
      )
      LocalObject(
        770,
        IFFLock.Constructor(Vector3(2381.957f, 5718.811f, 54.74357f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 259
      )
      LocalObject(
        771,
        IFFLock.Constructor(Vector3(2386.047f, 5701.189f, 34.74357f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 256
      )
      LocalObject(
        772,
        IFFLock.Constructor(Vector3(2386.047f, 5701.189f, 54.74357f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 257
      )
      LocalObject(987, Locker.Constructor(Vector3(2387.716f, 5694.963f, 23.27657f)), owning_building_guid = 63)
      LocalObject(988, Locker.Constructor(Vector3(2387.751f, 5716.835f, 23.27657f)), owning_building_guid = 63)
      LocalObject(989, Locker.Constructor(Vector3(2389.053f, 5694.963f, 23.27657f)), owning_building_guid = 63)
      LocalObject(990, Locker.Constructor(Vector3(2389.088f, 5716.835f, 23.27657f)), owning_building_guid = 63)
      LocalObject(991, Locker.Constructor(Vector3(2391.741f, 5694.963f, 23.27657f)), owning_building_guid = 63)
      LocalObject(992, Locker.Constructor(Vector3(2391.741f, 5716.835f, 23.27657f)), owning_building_guid = 63)
      LocalObject(993, Locker.Constructor(Vector3(2393.143f, 5694.963f, 23.27657f)), owning_building_guid = 63)
      LocalObject(994, Locker.Constructor(Vector3(2393.143f, 5716.835f, 23.27657f)), owning_building_guid = 63)
      LocalObject(
        1506,
        Terminal.Constructor(Vector3(2393.445f, 5700.129f, 24.61457f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1507,
        Terminal.Constructor(Vector3(2393.445f, 5705.853f, 24.61457f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1508,
        Terminal.Constructor(Vector3(2393.445f, 5711.234f, 24.61457f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        2102,
        SpawnTube.Constructor(Vector3(2382.706f, 5697.742f, 22.76457f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        2103,
        SpawnTube.Constructor(Vector3(2382.706f, 5714.152f, 22.76457f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        1810,
        ProximityTerminal.Constructor(Vector3(2370.907f, 5704.725f, 60.85257f), pad_landing_tower_frame),
        owning_building_guid = 63
      )
      LocalObject(
        1811,
        Terminal.Constructor(Vector3(2370.907f, 5704.725f, 60.85257f), air_rearm_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1813,
        ProximityTerminal.Constructor(Vector3(2370.907f, 5715.17f, 60.85257f), pad_landing_tower_frame),
        owning_building_guid = 63
      )
      LocalObject(
        1814,
        Terminal.Constructor(Vector3(2370.907f, 5715.17f, 60.85257f), air_rearm_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1373,
        FacilityTurret.Constructor(Vector3(2357.07f, 5695.045f, 52.22457f), manned_turret),
        owning_building_guid = 63
      )
      TurretToWeapon(1373, 5077)
      LocalObject(
        1374,
        FacilityTurret.Constructor(Vector3(2395.497f, 5724.957f, 52.22457f), manned_turret),
        owning_building_guid = 63
      )
      TurretToWeapon(1374, 5078)
      LocalObject(
        1936,
        Painbox.Constructor(Vector3(2376.454f, 5702.849f, 25.30207f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        1937,
        Painbox.Constructor(Vector3(2388.923f, 5699.54f, 23.38257f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        1938,
        Painbox.Constructor(Vector3(2389.113f, 5712.022f, 23.38257f), painbox_radius_continuous),
        owning_building_guid = 63
      )
    }

    Building54()

    def Building54(): Unit = { // Name: Amerish_Warpgate_Tower Type: tower_c GUID: 64, MapID: 54
      LocalBuilding(
        "Amerish_Warpgate_Tower",
        64,
        54,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2630f, 2730f, 182.7152f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2187,
        CaptureTerminal.Constructor(Vector3(2646.587f, 2729.897f, 192.7142f), secondary_capture),
        owning_building_guid = 64
      )
      LocalObject(264, Door.Constructor(Vector3(2642f, 2722f, 184.2362f)), owning_building_guid = 64)
      LocalObject(265, Door.Constructor(Vector3(2642f, 2722f, 204.2352f)), owning_building_guid = 64)
      LocalObject(266, Door.Constructor(Vector3(2642f, 2738f, 184.2362f)), owning_building_guid = 64)
      LocalObject(267, Door.Constructor(Vector3(2642f, 2738f, 204.2352f)), owning_building_guid = 64)
      LocalObject(2283, Door.Constructor(Vector3(2641.146f, 2718.794f, 174.0512f)), owning_building_guid = 64)
      LocalObject(2284, Door.Constructor(Vector3(2641.146f, 2735.204f, 174.0512f)), owning_building_guid = 64)
      LocalObject(
        777,
        IFFLock.Constructor(Vector3(2639.957f, 2738.811f, 184.1762f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 266
      )
      LocalObject(
        778,
        IFFLock.Constructor(Vector3(2639.957f, 2738.811f, 204.1762f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 267
      )
      LocalObject(
        779,
        IFFLock.Constructor(Vector3(2644.047f, 2721.189f, 184.1762f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 264
      )
      LocalObject(
        780,
        IFFLock.Constructor(Vector3(2644.047f, 2721.189f, 204.1762f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 265
      )
      LocalObject(1003, Locker.Constructor(Vector3(2645.716f, 2714.963f, 172.7092f)), owning_building_guid = 64)
      LocalObject(1004, Locker.Constructor(Vector3(2645.751f, 2736.835f, 172.7092f)), owning_building_guid = 64)
      LocalObject(1005, Locker.Constructor(Vector3(2647.053f, 2714.963f, 172.7092f)), owning_building_guid = 64)
      LocalObject(1006, Locker.Constructor(Vector3(2647.088f, 2736.835f, 172.7092f)), owning_building_guid = 64)
      LocalObject(1007, Locker.Constructor(Vector3(2649.741f, 2714.963f, 172.7092f)), owning_building_guid = 64)
      LocalObject(1008, Locker.Constructor(Vector3(2649.741f, 2736.835f, 172.7092f)), owning_building_guid = 64)
      LocalObject(1009, Locker.Constructor(Vector3(2651.143f, 2714.963f, 172.7092f)), owning_building_guid = 64)
      LocalObject(1010, Locker.Constructor(Vector3(2651.143f, 2736.835f, 172.7092f)), owning_building_guid = 64)
      LocalObject(
        1512,
        Terminal.Constructor(Vector3(2651.445f, 2720.129f, 174.0472f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1513,
        Terminal.Constructor(Vector3(2651.445f, 2725.853f, 174.0472f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1514,
        Terminal.Constructor(Vector3(2651.445f, 2731.234f, 174.0472f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        2106,
        SpawnTube.Constructor(Vector3(2640.706f, 2717.742f, 172.1972f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 64
      )
      LocalObject(
        2107,
        SpawnTube.Constructor(Vector3(2640.706f, 2734.152f, 172.1972f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 64
      )
      LocalObject(
        1816,
        ProximityTerminal.Constructor(Vector3(2628.907f, 2724.725f, 210.2852f), pad_landing_tower_frame),
        owning_building_guid = 64
      )
      LocalObject(
        1817,
        Terminal.Constructor(Vector3(2628.907f, 2724.725f, 210.2852f), air_rearm_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1819,
        ProximityTerminal.Constructor(Vector3(2628.907f, 2735.17f, 210.2852f), pad_landing_tower_frame),
        owning_building_guid = 64
      )
      LocalObject(
        1820,
        Terminal.Constructor(Vector3(2628.907f, 2735.17f, 210.2852f), air_rearm_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1376,
        FacilityTurret.Constructor(Vector3(2615.07f, 2715.045f, 201.6572f), manned_turret),
        owning_building_guid = 64
      )
      TurretToWeapon(1376, 5079)
      LocalObject(
        1378,
        FacilityTurret.Constructor(Vector3(2653.497f, 2744.957f, 201.6572f), manned_turret),
        owning_building_guid = 64
      )
      TurretToWeapon(1378, 5080)
      LocalObject(
        1942,
        Painbox.Constructor(Vector3(2634.454f, 2722.849f, 174.7347f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        1943,
        Painbox.Constructor(Vector3(2646.923f, 2719.54f, 172.8152f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        1944,
        Painbox.Constructor(Vector3(2647.113f, 2732.022f, 172.8152f), painbox_radius_continuous),
        owning_building_guid = 64
      )
    }

    Building16()

    def Building16(): Unit = { // Name: SW_Tarqaq_Tower Type: tower_c GUID: 65, MapID: 16
      LocalBuilding(
        "SW_Tarqaq_Tower",
        65,
        16,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2820f, 2072f, 237.9481f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2188,
        CaptureTerminal.Constructor(Vector3(2836.587f, 2071.897f, 247.9471f), secondary_capture),
        owning_building_guid = 65
      )
      LocalObject(268, Door.Constructor(Vector3(2832f, 2064f, 239.4691f)), owning_building_guid = 65)
      LocalObject(269, Door.Constructor(Vector3(2832f, 2064f, 259.4681f)), owning_building_guid = 65)
      LocalObject(270, Door.Constructor(Vector3(2832f, 2080f, 239.4691f)), owning_building_guid = 65)
      LocalObject(271, Door.Constructor(Vector3(2832f, 2080f, 259.4681f)), owning_building_guid = 65)
      LocalObject(2285, Door.Constructor(Vector3(2831.146f, 2060.794f, 229.2841f)), owning_building_guid = 65)
      LocalObject(2286, Door.Constructor(Vector3(2831.146f, 2077.204f, 229.2841f)), owning_building_guid = 65)
      LocalObject(
        781,
        IFFLock.Constructor(Vector3(2829.957f, 2080.811f, 239.4091f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 270
      )
      LocalObject(
        782,
        IFFLock.Constructor(Vector3(2829.957f, 2080.811f, 259.4091f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 271
      )
      LocalObject(
        783,
        IFFLock.Constructor(Vector3(2834.047f, 2063.189f, 239.4091f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 268
      )
      LocalObject(
        784,
        IFFLock.Constructor(Vector3(2834.047f, 2063.189f, 259.4091f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 269
      )
      LocalObject(1011, Locker.Constructor(Vector3(2835.716f, 2056.963f, 227.9421f)), owning_building_guid = 65)
      LocalObject(1012, Locker.Constructor(Vector3(2835.751f, 2078.835f, 227.9421f)), owning_building_guid = 65)
      LocalObject(1013, Locker.Constructor(Vector3(2837.053f, 2056.963f, 227.9421f)), owning_building_guid = 65)
      LocalObject(1014, Locker.Constructor(Vector3(2837.088f, 2078.835f, 227.9421f)), owning_building_guid = 65)
      LocalObject(1015, Locker.Constructor(Vector3(2839.741f, 2056.963f, 227.9421f)), owning_building_guid = 65)
      LocalObject(1016, Locker.Constructor(Vector3(2839.741f, 2078.835f, 227.9421f)), owning_building_guid = 65)
      LocalObject(1017, Locker.Constructor(Vector3(2841.143f, 2056.963f, 227.9421f)), owning_building_guid = 65)
      LocalObject(1018, Locker.Constructor(Vector3(2841.143f, 2078.835f, 227.9421f)), owning_building_guid = 65)
      LocalObject(
        1515,
        Terminal.Constructor(Vector3(2841.445f, 2062.129f, 229.2801f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1516,
        Terminal.Constructor(Vector3(2841.445f, 2067.853f, 229.2801f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1517,
        Terminal.Constructor(Vector3(2841.445f, 2073.234f, 229.2801f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        2108,
        SpawnTube.Constructor(Vector3(2830.706f, 2059.742f, 227.4301f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        2109,
        SpawnTube.Constructor(Vector3(2830.706f, 2076.152f, 227.4301f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        1822,
        ProximityTerminal.Constructor(Vector3(2818.907f, 2066.725f, 265.5181f), pad_landing_tower_frame),
        owning_building_guid = 65
      )
      LocalObject(
        1823,
        Terminal.Constructor(Vector3(2818.907f, 2066.725f, 265.5181f), air_rearm_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1825,
        ProximityTerminal.Constructor(Vector3(2818.907f, 2077.17f, 265.5181f), pad_landing_tower_frame),
        owning_building_guid = 65
      )
      LocalObject(
        1826,
        Terminal.Constructor(Vector3(2818.907f, 2077.17f, 265.5181f), air_rearm_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1379,
        FacilityTurret.Constructor(Vector3(2805.07f, 2057.045f, 256.8901f), manned_turret),
        owning_building_guid = 65
      )
      TurretToWeapon(1379, 5081)
      LocalObject(
        1380,
        FacilityTurret.Constructor(Vector3(2843.497f, 2086.957f, 256.8901f), manned_turret),
        owning_building_guid = 65
      )
      TurretToWeapon(1380, 5082)
      LocalObject(
        1945,
        Painbox.Constructor(Vector3(2824.454f, 2064.849f, 229.9676f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        1946,
        Painbox.Constructor(Vector3(2836.923f, 2061.54f, 228.0481f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        1947,
        Painbox.Constructor(Vector3(2837.113f, 2074.022f, 228.0481f), painbox_radius_continuous),
        owning_building_guid = 65
      )
    }

    Building24()

    def Building24(): Unit = { // Name: NW_Nerrivik_Tower Type: tower_c GUID: 66, MapID: 24
      LocalBuilding(
        "NW_Nerrivik_Tower",
        66,
        24,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3188f, 3972f, 187.1799f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2190,
        CaptureTerminal.Constructor(Vector3(3204.587f, 3971.897f, 197.1789f), secondary_capture),
        owning_building_guid = 66
      )
      LocalObject(291, Door.Constructor(Vector3(3200f, 3964f, 188.7009f)), owning_building_guid = 66)
      LocalObject(292, Door.Constructor(Vector3(3200f, 3964f, 208.6999f)), owning_building_guid = 66)
      LocalObject(293, Door.Constructor(Vector3(3200f, 3980f, 188.7009f)), owning_building_guid = 66)
      LocalObject(294, Door.Constructor(Vector3(3200f, 3980f, 208.6999f)), owning_building_guid = 66)
      LocalObject(2292, Door.Constructor(Vector3(3199.146f, 3960.794f, 178.5159f)), owning_building_guid = 66)
      LocalObject(2293, Door.Constructor(Vector3(3199.146f, 3977.204f, 178.5159f)), owning_building_guid = 66)
      LocalObject(
        797,
        IFFLock.Constructor(Vector3(3197.957f, 3980.811f, 188.6409f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 293
      )
      LocalObject(
        798,
        IFFLock.Constructor(Vector3(3197.957f, 3980.811f, 208.6409f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 294
      )
      LocalObject(
        799,
        IFFLock.Constructor(Vector3(3202.047f, 3963.189f, 188.6409f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 291
      )
      LocalObject(
        800,
        IFFLock.Constructor(Vector3(3202.047f, 3963.189f, 208.6409f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 292
      )
      LocalObject(1048, Locker.Constructor(Vector3(3203.716f, 3956.963f, 177.1739f)), owning_building_guid = 66)
      LocalObject(1049, Locker.Constructor(Vector3(3203.751f, 3978.835f, 177.1739f)), owning_building_guid = 66)
      LocalObject(1050, Locker.Constructor(Vector3(3205.053f, 3956.963f, 177.1739f)), owning_building_guid = 66)
      LocalObject(1051, Locker.Constructor(Vector3(3205.088f, 3978.835f, 177.1739f)), owning_building_guid = 66)
      LocalObject(1052, Locker.Constructor(Vector3(3207.741f, 3956.963f, 177.1739f)), owning_building_guid = 66)
      LocalObject(1053, Locker.Constructor(Vector3(3207.741f, 3978.835f, 177.1739f)), owning_building_guid = 66)
      LocalObject(1054, Locker.Constructor(Vector3(3209.143f, 3956.963f, 177.1739f)), owning_building_guid = 66)
      LocalObject(1055, Locker.Constructor(Vector3(3209.143f, 3978.835f, 177.1739f)), owning_building_guid = 66)
      LocalObject(
        1525,
        Terminal.Constructor(Vector3(3209.445f, 3962.129f, 178.5119f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1526,
        Terminal.Constructor(Vector3(3209.445f, 3967.853f, 178.5119f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1527,
        Terminal.Constructor(Vector3(3209.445f, 3973.234f, 178.5119f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2115,
        SpawnTube.Constructor(Vector3(3198.706f, 3959.742f, 176.6619f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 66
      )
      LocalObject(
        2116,
        SpawnTube.Constructor(Vector3(3198.706f, 3976.152f, 176.6619f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 66
      )
      LocalObject(
        1828,
        ProximityTerminal.Constructor(Vector3(3186.907f, 3966.725f, 214.7499f), pad_landing_tower_frame),
        owning_building_guid = 66
      )
      LocalObject(
        1829,
        Terminal.Constructor(Vector3(3186.907f, 3966.725f, 214.7499f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1831,
        ProximityTerminal.Constructor(Vector3(3186.907f, 3977.17f, 214.7499f), pad_landing_tower_frame),
        owning_building_guid = 66
      )
      LocalObject(
        1832,
        Terminal.Constructor(Vector3(3186.907f, 3977.17f, 214.7499f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1388,
        FacilityTurret.Constructor(Vector3(3173.07f, 3957.045f, 206.1219f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(1388, 5083)
      LocalObject(
        1390,
        FacilityTurret.Constructor(Vector3(3211.497f, 3986.957f, 206.1219f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(1390, 5084)
      LocalObject(
        1951,
        Painbox.Constructor(Vector3(3192.454f, 3964.849f, 179.1994f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        1952,
        Painbox.Constructor(Vector3(3204.923f, 3961.54f, 177.2799f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        1953,
        Painbox.Constructor(Vector3(3205.113f, 3974.022f, 177.2799f), painbox_radius_continuous),
        owning_building_guid = 66
      )
    }

    Building30()

    def Building30(): Unit = { // Name: S_Igaluk_Tower Type: tower_c GUID: 67, MapID: 30
      LocalBuilding(
        "S_Igaluk_Tower",
        67,
        30,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3474f, 5228f, 240.8136f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2194,
        CaptureTerminal.Constructor(Vector3(3490.587f, 5227.897f, 250.8126f), secondary_capture),
        owning_building_guid = 67
      )
      LocalObject(322, Door.Constructor(Vector3(3486f, 5220f, 242.3346f)), owning_building_guid = 67)
      LocalObject(323, Door.Constructor(Vector3(3486f, 5220f, 262.3336f)), owning_building_guid = 67)
      LocalObject(324, Door.Constructor(Vector3(3486f, 5236f, 242.3346f)), owning_building_guid = 67)
      LocalObject(325, Door.Constructor(Vector3(3486f, 5236f, 262.3336f)), owning_building_guid = 67)
      LocalObject(2303, Door.Constructor(Vector3(3485.146f, 5216.794f, 232.1496f)), owning_building_guid = 67)
      LocalObject(2304, Door.Constructor(Vector3(3485.146f, 5233.204f, 232.1496f)), owning_building_guid = 67)
      LocalObject(
        823,
        IFFLock.Constructor(Vector3(3483.957f, 5236.811f, 242.2746f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 324
      )
      LocalObject(
        824,
        IFFLock.Constructor(Vector3(3483.957f, 5236.811f, 262.2746f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 325
      )
      LocalObject(
        825,
        IFFLock.Constructor(Vector3(3488.047f, 5219.189f, 242.2746f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 322
      )
      LocalObject(
        826,
        IFFLock.Constructor(Vector3(3488.047f, 5219.189f, 262.2746f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 323
      )
      LocalObject(1092, Locker.Constructor(Vector3(3489.716f, 5212.963f, 230.8076f)), owning_building_guid = 67)
      LocalObject(1093, Locker.Constructor(Vector3(3489.751f, 5234.835f, 230.8076f)), owning_building_guid = 67)
      LocalObject(1094, Locker.Constructor(Vector3(3491.053f, 5212.963f, 230.8076f)), owning_building_guid = 67)
      LocalObject(1095, Locker.Constructor(Vector3(3491.088f, 5234.835f, 230.8076f)), owning_building_guid = 67)
      LocalObject(1096, Locker.Constructor(Vector3(3493.741f, 5212.963f, 230.8076f)), owning_building_guid = 67)
      LocalObject(1097, Locker.Constructor(Vector3(3493.741f, 5234.835f, 230.8076f)), owning_building_guid = 67)
      LocalObject(1098, Locker.Constructor(Vector3(3495.143f, 5212.963f, 230.8076f)), owning_building_guid = 67)
      LocalObject(1099, Locker.Constructor(Vector3(3495.143f, 5234.835f, 230.8076f)), owning_building_guid = 67)
      LocalObject(
        1541,
        Terminal.Constructor(Vector3(3495.445f, 5218.129f, 232.1456f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1542,
        Terminal.Constructor(Vector3(3495.445f, 5223.853f, 232.1456f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1543,
        Terminal.Constructor(Vector3(3495.445f, 5229.234f, 232.1456f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        2126,
        SpawnTube.Constructor(Vector3(3484.706f, 5215.742f, 230.2956f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        2127,
        SpawnTube.Constructor(Vector3(3484.706f, 5232.152f, 230.2956f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        1834,
        ProximityTerminal.Constructor(Vector3(3472.907f, 5222.725f, 268.3836f), pad_landing_tower_frame),
        owning_building_guid = 67
      )
      LocalObject(
        1835,
        Terminal.Constructor(Vector3(3472.907f, 5222.725f, 268.3836f), air_rearm_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1837,
        ProximityTerminal.Constructor(Vector3(3472.907f, 5233.17f, 268.3836f), pad_landing_tower_frame),
        owning_building_guid = 67
      )
      LocalObject(
        1838,
        Terminal.Constructor(Vector3(3472.907f, 5233.17f, 268.3836f), air_rearm_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1400,
        FacilityTurret.Constructor(Vector3(3459.07f, 5213.045f, 259.7556f), manned_turret),
        owning_building_guid = 67
      )
      TurretToWeapon(1400, 5085)
      LocalObject(
        1401,
        FacilityTurret.Constructor(Vector3(3497.497f, 5242.957f, 259.7556f), manned_turret),
        owning_building_guid = 67
      )
      TurretToWeapon(1401, 5086)
      LocalObject(
        1963,
        Painbox.Constructor(Vector3(3478.454f, 5220.849f, 232.8331f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        1964,
        Painbox.Constructor(Vector3(3490.923f, 5217.54f, 230.9136f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        1965,
        Painbox.Constructor(Vector3(3491.113f, 5230.022f, 230.9136f), painbox_radius_continuous),
        owning_building_guid = 67
      )
    }

    Building17()

    def Building17(): Unit = { // Name: E_Keelut_Tower Type: tower_c GUID: 68, MapID: 17
      LocalBuilding(
        "E_Keelut_Tower",
        68,
        17,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4352f, 1834f, 152.6882f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2202,
        CaptureTerminal.Constructor(Vector3(4368.587f, 1833.897f, 162.6872f), secondary_capture),
        owning_building_guid = 68
      )
      LocalObject(426, Door.Constructor(Vector3(4364f, 1826f, 154.2092f)), owning_building_guid = 68)
      LocalObject(427, Door.Constructor(Vector3(4364f, 1826f, 174.2082f)), owning_building_guid = 68)
      LocalObject(428, Door.Constructor(Vector3(4364f, 1842f, 154.2092f)), owning_building_guid = 68)
      LocalObject(429, Door.Constructor(Vector3(4364f, 1842f, 174.2082f)), owning_building_guid = 68)
      LocalObject(2331, Door.Constructor(Vector3(4363.146f, 1822.794f, 144.0242f)), owning_building_guid = 68)
      LocalObject(2332, Door.Constructor(Vector3(4363.146f, 1839.204f, 144.0242f)), owning_building_guid = 68)
      LocalObject(
        900,
        IFFLock.Constructor(Vector3(4361.957f, 1842.811f, 154.1492f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 428
      )
      LocalObject(
        901,
        IFFLock.Constructor(Vector3(4361.957f, 1842.811f, 174.1492f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 429
      )
      LocalObject(
        904,
        IFFLock.Constructor(Vector3(4366.047f, 1825.189f, 154.1492f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 426
      )
      LocalObject(
        905,
        IFFLock.Constructor(Vector3(4366.047f, 1825.189f, 174.1492f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 427
      )
      LocalObject(1213, Locker.Constructor(Vector3(4367.716f, 1818.963f, 142.6822f)), owning_building_guid = 68)
      LocalObject(1214, Locker.Constructor(Vector3(4367.751f, 1840.835f, 142.6822f)), owning_building_guid = 68)
      LocalObject(1215, Locker.Constructor(Vector3(4369.053f, 1818.963f, 142.6822f)), owning_building_guid = 68)
      LocalObject(1216, Locker.Constructor(Vector3(4369.088f, 1840.835f, 142.6822f)), owning_building_guid = 68)
      LocalObject(1221, Locker.Constructor(Vector3(4371.741f, 1818.963f, 142.6822f)), owning_building_guid = 68)
      LocalObject(1222, Locker.Constructor(Vector3(4371.741f, 1840.835f, 142.6822f)), owning_building_guid = 68)
      LocalObject(1223, Locker.Constructor(Vector3(4373.143f, 1818.963f, 142.6822f)), owning_building_guid = 68)
      LocalObject(1224, Locker.Constructor(Vector3(4373.143f, 1840.835f, 142.6822f)), owning_building_guid = 68)
      LocalObject(
        1588,
        Terminal.Constructor(Vector3(4373.445f, 1824.129f, 144.0202f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1589,
        Terminal.Constructor(Vector3(4373.445f, 1829.853f, 144.0202f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1590,
        Terminal.Constructor(Vector3(4373.445f, 1835.234f, 144.0202f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        2154,
        SpawnTube.Constructor(Vector3(4362.706f, 1821.742f, 142.1702f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        2155,
        SpawnTube.Constructor(Vector3(4362.706f, 1838.152f, 142.1702f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        1840,
        ProximityTerminal.Constructor(Vector3(4350.907f, 1828.725f, 180.2582f), pad_landing_tower_frame),
        owning_building_guid = 68
      )
      LocalObject(
        1841,
        Terminal.Constructor(Vector3(4350.907f, 1828.725f, 180.2582f), air_rearm_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1843,
        ProximityTerminal.Constructor(Vector3(4350.907f, 1839.17f, 180.2582f), pad_landing_tower_frame),
        owning_building_guid = 68
      )
      LocalObject(
        1844,
        Terminal.Constructor(Vector3(4350.907f, 1839.17f, 180.2582f), air_rearm_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1439,
        FacilityTurret.Constructor(Vector3(4337.07f, 1819.045f, 171.6302f), manned_turret),
        owning_building_guid = 68
      )
      TurretToWeapon(1439, 5087)
      LocalObject(
        1441,
        FacilityTurret.Constructor(Vector3(4375.497f, 1848.957f, 171.6302f), manned_turret),
        owning_building_guid = 68
      )
      TurretToWeapon(1441, 5088)
      LocalObject(
        1987,
        Painbox.Constructor(Vector3(4356.454f, 1826.849f, 144.7077f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        1989,
        Painbox.Constructor(Vector3(4368.923f, 1823.54f, 142.7882f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        1990,
        Painbox.Constructor(Vector3(4369.113f, 1836.022f, 142.7882f), painbox_radius_continuous),
        owning_building_guid = 68
      )
    }

    Building49()

    def Building49(): Unit = { // Name: Akna_Tower Type: tower_c GUID: 69, MapID: 49
      LocalBuilding(
        "Akna_Tower",
        69,
        49,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4354f, 3990f, 237.69f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2203,
        CaptureTerminal.Constructor(Vector3(4370.587f, 3989.897f, 247.689f), secondary_capture),
        owning_building_guid = 69
      )
      LocalObject(430, Door.Constructor(Vector3(4366f, 3982f, 239.211f)), owning_building_guid = 69)
      LocalObject(431, Door.Constructor(Vector3(4366f, 3982f, 259.2101f)), owning_building_guid = 69)
      LocalObject(432, Door.Constructor(Vector3(4366f, 3998f, 239.211f)), owning_building_guid = 69)
      LocalObject(433, Door.Constructor(Vector3(4366f, 3998f, 259.2101f)), owning_building_guid = 69)
      LocalObject(2333, Door.Constructor(Vector3(4365.146f, 3978.794f, 229.026f)), owning_building_guid = 69)
      LocalObject(2334, Door.Constructor(Vector3(4365.146f, 3995.204f, 229.026f)), owning_building_guid = 69)
      LocalObject(
        902,
        IFFLock.Constructor(Vector3(4363.957f, 3998.811f, 239.151f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 432
      )
      LocalObject(
        903,
        IFFLock.Constructor(Vector3(4363.957f, 3998.811f, 259.1511f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 433
      )
      LocalObject(
        906,
        IFFLock.Constructor(Vector3(4368.047f, 3981.189f, 239.151f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 430
      )
      LocalObject(
        907,
        IFFLock.Constructor(Vector3(4368.047f, 3981.189f, 259.1511f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 431
      )
      LocalObject(1217, Locker.Constructor(Vector3(4369.716f, 3974.963f, 227.6841f)), owning_building_guid = 69)
      LocalObject(1218, Locker.Constructor(Vector3(4369.751f, 3996.835f, 227.6841f)), owning_building_guid = 69)
      LocalObject(1219, Locker.Constructor(Vector3(4371.053f, 3974.963f, 227.6841f)), owning_building_guid = 69)
      LocalObject(1220, Locker.Constructor(Vector3(4371.088f, 3996.835f, 227.6841f)), owning_building_guid = 69)
      LocalObject(1225, Locker.Constructor(Vector3(4373.741f, 3974.963f, 227.6841f)), owning_building_guid = 69)
      LocalObject(1226, Locker.Constructor(Vector3(4373.741f, 3996.835f, 227.6841f)), owning_building_guid = 69)
      LocalObject(1227, Locker.Constructor(Vector3(4375.143f, 3974.963f, 227.6841f)), owning_building_guid = 69)
      LocalObject(1228, Locker.Constructor(Vector3(4375.143f, 3996.835f, 227.6841f)), owning_building_guid = 69)
      LocalObject(
        1591,
        Terminal.Constructor(Vector3(4375.445f, 3980.129f, 229.022f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1592,
        Terminal.Constructor(Vector3(4375.445f, 3985.853f, 229.022f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1593,
        Terminal.Constructor(Vector3(4375.445f, 3991.234f, 229.022f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2156,
        SpawnTube.Constructor(Vector3(4364.706f, 3977.742f, 227.172f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        2157,
        SpawnTube.Constructor(Vector3(4364.706f, 3994.152f, 227.172f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        1846,
        ProximityTerminal.Constructor(Vector3(4352.907f, 3984.725f, 265.26f), pad_landing_tower_frame),
        owning_building_guid = 69
      )
      LocalObject(
        1847,
        Terminal.Constructor(Vector3(4352.907f, 3984.725f, 265.26f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1849,
        ProximityTerminal.Constructor(Vector3(4352.907f, 3995.17f, 265.26f), pad_landing_tower_frame),
        owning_building_guid = 69
      )
      LocalObject(
        1850,
        Terminal.Constructor(Vector3(4352.907f, 3995.17f, 265.26f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1440,
        FacilityTurret.Constructor(Vector3(4339.07f, 3975.045f, 256.632f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(1440, 5089)
      LocalObject(
        1442,
        FacilityTurret.Constructor(Vector3(4377.497f, 4004.957f, 256.632f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(1442, 5090)
      LocalObject(
        1988,
        Painbox.Constructor(Vector3(4358.454f, 3982.849f, 229.7095f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        1991,
        Painbox.Constructor(Vector3(4370.923f, 3979.54f, 227.7901f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        1992,
        Painbox.Constructor(Vector3(4371.113f, 3992.022f, 227.7901f), painbox_radius_continuous),
        owning_building_guid = 69
      )
    }

    Building51()

    def Building51(): Unit = { // Name: Hossin_Warpgate_Tower Type: tower_c GUID: 70, MapID: 51
      LocalBuilding(
        "Hossin_Warpgate_Tower",
        70,
        51,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5342f, 4332f, 28.19361f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2208,
        CaptureTerminal.Constructor(Vector3(5358.587f, 4331.897f, 38.19261f), secondary_capture),
        owning_building_guid = 70
      )
      LocalObject(481, Door.Constructor(Vector3(5354f, 4324f, 29.71461f)), owning_building_guid = 70)
      LocalObject(482, Door.Constructor(Vector3(5354f, 4324f, 49.71361f)), owning_building_guid = 70)
      LocalObject(483, Door.Constructor(Vector3(5354f, 4340f, 29.71461f)), owning_building_guid = 70)
      LocalObject(484, Door.Constructor(Vector3(5354f, 4340f, 49.71361f)), owning_building_guid = 70)
      LocalObject(2349, Door.Constructor(Vector3(5353.146f, 4320.794f, 19.52961f)), owning_building_guid = 70)
      LocalObject(2350, Door.Constructor(Vector3(5353.146f, 4337.204f, 19.52961f)), owning_building_guid = 70)
      LocalObject(
        947,
        IFFLock.Constructor(Vector3(5351.957f, 4340.811f, 29.65461f), Vector3(0, 0, 0)),
        owning_building_guid = 70,
        door_guid = 483
      )
      LocalObject(
        948,
        IFFLock.Constructor(Vector3(5351.957f, 4340.811f, 49.65461f), Vector3(0, 0, 0)),
        owning_building_guid = 70,
        door_guid = 484
      )
      LocalObject(
        949,
        IFFLock.Constructor(Vector3(5356.047f, 4323.189f, 29.65461f), Vector3(0, 0, 180)),
        owning_building_guid = 70,
        door_guid = 481
      )
      LocalObject(
        950,
        IFFLock.Constructor(Vector3(5356.047f, 4323.189f, 49.65461f), Vector3(0, 0, 180)),
        owning_building_guid = 70,
        door_guid = 482
      )
      LocalObject(1294, Locker.Constructor(Vector3(5357.716f, 4316.963f, 18.18761f)), owning_building_guid = 70)
      LocalObject(1295, Locker.Constructor(Vector3(5357.751f, 4338.835f, 18.18761f)), owning_building_guid = 70)
      LocalObject(1296, Locker.Constructor(Vector3(5359.053f, 4316.963f, 18.18761f)), owning_building_guid = 70)
      LocalObject(1297, Locker.Constructor(Vector3(5359.088f, 4338.835f, 18.18761f)), owning_building_guid = 70)
      LocalObject(1298, Locker.Constructor(Vector3(5361.741f, 4316.963f, 18.18761f)), owning_building_guid = 70)
      LocalObject(1299, Locker.Constructor(Vector3(5361.741f, 4338.835f, 18.18761f)), owning_building_guid = 70)
      LocalObject(1300, Locker.Constructor(Vector3(5363.143f, 4316.963f, 18.18761f)), owning_building_guid = 70)
      LocalObject(1301, Locker.Constructor(Vector3(5363.143f, 4338.835f, 18.18761f)), owning_building_guid = 70)
      LocalObject(
        1616,
        Terminal.Constructor(Vector3(5363.445f, 4322.129f, 19.52561f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1617,
        Terminal.Constructor(Vector3(5363.445f, 4327.853f, 19.52561f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1618,
        Terminal.Constructor(Vector3(5363.445f, 4333.234f, 19.52561f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        2172,
        SpawnTube.Constructor(Vector3(5352.706f, 4319.742f, 17.67561f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 70
      )
      LocalObject(
        2173,
        SpawnTube.Constructor(Vector3(5352.706f, 4336.152f, 17.67561f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 70
      )
      LocalObject(
        1852,
        ProximityTerminal.Constructor(Vector3(5340.907f, 4326.725f, 55.76361f), pad_landing_tower_frame),
        owning_building_guid = 70
      )
      LocalObject(
        1853,
        Terminal.Constructor(Vector3(5340.907f, 4326.725f, 55.76361f), air_rearm_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1855,
        ProximityTerminal.Constructor(Vector3(5340.907f, 4337.17f, 55.76361f), pad_landing_tower_frame),
        owning_building_guid = 70
      )
      LocalObject(
        1856,
        Terminal.Constructor(Vector3(5340.907f, 4337.17f, 55.76361f), air_rearm_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1456,
        FacilityTurret.Constructor(Vector3(5327.07f, 4317.045f, 47.13561f), manned_turret),
        owning_building_guid = 70
      )
      TurretToWeapon(1456, 5091)
      LocalObject(
        1457,
        FacilityTurret.Constructor(Vector3(5365.497f, 4346.957f, 47.13561f), manned_turret),
        owning_building_guid = 70
      )
      TurretToWeapon(1457, 5092)
      LocalObject(
        2005,
        Painbox.Constructor(Vector3(5346.454f, 4324.849f, 20.21311f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2006,
        Painbox.Constructor(Vector3(5358.923f, 4321.54f, 18.29361f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2007,
        Painbox.Constructor(Vector3(5359.113f, 4334.022f, 18.29361f), painbox_radius_continuous),
        owning_building_guid = 70
      )
    }

    Building19()

    def Building19(): Unit = { // Name: NE_Ishundar_Warpgate_Tower Type: tower_c GUID: 71, MapID: 19
      LocalBuilding(
        "NE_Ishundar_Warpgate_Tower",
        71,
        19,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5694f, 2434f, 20.01801f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2209,
        CaptureTerminal.Constructor(Vector3(5710.587f, 2433.897f, 30.01701f), secondary_capture),
        owning_building_guid = 71
      )
      LocalObject(485, Door.Constructor(Vector3(5706f, 2426f, 21.53901f)), owning_building_guid = 71)
      LocalObject(486, Door.Constructor(Vector3(5706f, 2426f, 41.53801f)), owning_building_guid = 71)
      LocalObject(487, Door.Constructor(Vector3(5706f, 2442f, 21.53901f)), owning_building_guid = 71)
      LocalObject(488, Door.Constructor(Vector3(5706f, 2442f, 41.53801f)), owning_building_guid = 71)
      LocalObject(2351, Door.Constructor(Vector3(5705.146f, 2422.794f, 11.35401f)), owning_building_guid = 71)
      LocalObject(2352, Door.Constructor(Vector3(5705.146f, 2439.204f, 11.35401f)), owning_building_guid = 71)
      LocalObject(
        951,
        IFFLock.Constructor(Vector3(5703.957f, 2442.811f, 21.47901f), Vector3(0, 0, 0)),
        owning_building_guid = 71,
        door_guid = 487
      )
      LocalObject(
        952,
        IFFLock.Constructor(Vector3(5703.957f, 2442.811f, 41.479f), Vector3(0, 0, 0)),
        owning_building_guid = 71,
        door_guid = 488
      )
      LocalObject(
        953,
        IFFLock.Constructor(Vector3(5708.047f, 2425.189f, 21.47901f), Vector3(0, 0, 180)),
        owning_building_guid = 71,
        door_guid = 485
      )
      LocalObject(
        954,
        IFFLock.Constructor(Vector3(5708.047f, 2425.189f, 41.479f), Vector3(0, 0, 180)),
        owning_building_guid = 71,
        door_guid = 486
      )
      LocalObject(1302, Locker.Constructor(Vector3(5709.716f, 2418.963f, 10.01201f)), owning_building_guid = 71)
      LocalObject(1303, Locker.Constructor(Vector3(5709.751f, 2440.835f, 10.01201f)), owning_building_guid = 71)
      LocalObject(1304, Locker.Constructor(Vector3(5711.053f, 2418.963f, 10.01201f)), owning_building_guid = 71)
      LocalObject(1305, Locker.Constructor(Vector3(5711.088f, 2440.835f, 10.01201f)), owning_building_guid = 71)
      LocalObject(1306, Locker.Constructor(Vector3(5713.741f, 2418.963f, 10.01201f)), owning_building_guid = 71)
      LocalObject(1307, Locker.Constructor(Vector3(5713.741f, 2440.835f, 10.01201f)), owning_building_guid = 71)
      LocalObject(1308, Locker.Constructor(Vector3(5715.143f, 2418.963f, 10.01201f)), owning_building_guid = 71)
      LocalObject(1309, Locker.Constructor(Vector3(5715.143f, 2440.835f, 10.01201f)), owning_building_guid = 71)
      LocalObject(
        1619,
        Terminal.Constructor(Vector3(5715.445f, 2424.129f, 11.35001f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1620,
        Terminal.Constructor(Vector3(5715.445f, 2429.853f, 11.35001f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1621,
        Terminal.Constructor(Vector3(5715.445f, 2435.234f, 11.35001f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        2174,
        SpawnTube.Constructor(Vector3(5704.706f, 2421.742f, 9.500006f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 71
      )
      LocalObject(
        2175,
        SpawnTube.Constructor(Vector3(5704.706f, 2438.152f, 9.500006f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 71
      )
      LocalObject(
        1858,
        ProximityTerminal.Constructor(Vector3(5692.907f, 2428.725f, 47.58801f), pad_landing_tower_frame),
        owning_building_guid = 71
      )
      LocalObject(
        1859,
        Terminal.Constructor(Vector3(5692.907f, 2428.725f, 47.58801f), air_rearm_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1861,
        ProximityTerminal.Constructor(Vector3(5692.907f, 2439.17f, 47.58801f), pad_landing_tower_frame),
        owning_building_guid = 71
      )
      LocalObject(
        1862,
        Terminal.Constructor(Vector3(5692.907f, 2439.17f, 47.58801f), air_rearm_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1458,
        FacilityTurret.Constructor(Vector3(5679.07f, 2419.045f, 38.96001f), manned_turret),
        owning_building_guid = 71
      )
      TurretToWeapon(1458, 5093)
      LocalObject(
        1459,
        FacilityTurret.Constructor(Vector3(5717.497f, 2448.957f, 38.96001f), manned_turret),
        owning_building_guid = 71
      )
      TurretToWeapon(1459, 5094)
      LocalObject(
        2008,
        Painbox.Constructor(Vector3(5698.454f, 2426.849f, 12.03751f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2009,
        Painbox.Constructor(Vector3(5710.923f, 2423.54f, 10.11801f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2010,
        Painbox.Constructor(Vector3(5711.113f, 2436.022f, 10.11801f), painbox_radius_continuous),
        owning_building_guid = 71
      )
    }

    Building55()

    def Building55(): Unit = { // Name: Pinga_Tower Type: tower_c GUID: 72, MapID: 55
      LocalBuilding(
        "Pinga_Tower",
        72,
        55,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5768f, 3202f, 97.4664f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2210,
        CaptureTerminal.Constructor(Vector3(5784.587f, 3201.897f, 107.4654f), secondary_capture),
        owning_building_guid = 72
      )
      LocalObject(489, Door.Constructor(Vector3(5780f, 3194f, 98.9874f)), owning_building_guid = 72)
      LocalObject(490, Door.Constructor(Vector3(5780f, 3194f, 118.9864f)), owning_building_guid = 72)
      LocalObject(491, Door.Constructor(Vector3(5780f, 3210f, 98.9874f)), owning_building_guid = 72)
      LocalObject(492, Door.Constructor(Vector3(5780f, 3210f, 118.9864f)), owning_building_guid = 72)
      LocalObject(2353, Door.Constructor(Vector3(5779.146f, 3190.794f, 88.8024f)), owning_building_guid = 72)
      LocalObject(2354, Door.Constructor(Vector3(5779.146f, 3207.204f, 88.8024f)), owning_building_guid = 72)
      LocalObject(
        955,
        IFFLock.Constructor(Vector3(5777.957f, 3210.811f, 98.9274f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 491
      )
      LocalObject(
        956,
        IFFLock.Constructor(Vector3(5777.957f, 3210.811f, 118.9274f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 492
      )
      LocalObject(
        957,
        IFFLock.Constructor(Vector3(5782.047f, 3193.189f, 98.9274f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 489
      )
      LocalObject(
        958,
        IFFLock.Constructor(Vector3(5782.047f, 3193.189f, 118.9274f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 490
      )
      LocalObject(1310, Locker.Constructor(Vector3(5783.716f, 3186.963f, 87.4604f)), owning_building_guid = 72)
      LocalObject(1311, Locker.Constructor(Vector3(5783.751f, 3208.835f, 87.4604f)), owning_building_guid = 72)
      LocalObject(1312, Locker.Constructor(Vector3(5785.053f, 3186.963f, 87.4604f)), owning_building_guid = 72)
      LocalObject(1313, Locker.Constructor(Vector3(5785.088f, 3208.835f, 87.4604f)), owning_building_guid = 72)
      LocalObject(1314, Locker.Constructor(Vector3(5787.741f, 3186.963f, 87.4604f)), owning_building_guid = 72)
      LocalObject(1315, Locker.Constructor(Vector3(5787.741f, 3208.835f, 87.4604f)), owning_building_guid = 72)
      LocalObject(1316, Locker.Constructor(Vector3(5789.143f, 3186.963f, 87.4604f)), owning_building_guid = 72)
      LocalObject(1317, Locker.Constructor(Vector3(5789.143f, 3208.835f, 87.4604f)), owning_building_guid = 72)
      LocalObject(
        1622,
        Terminal.Constructor(Vector3(5789.445f, 3192.129f, 88.7984f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1623,
        Terminal.Constructor(Vector3(5789.445f, 3197.853f, 88.7984f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1624,
        Terminal.Constructor(Vector3(5789.445f, 3203.234f, 88.7984f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2176,
        SpawnTube.Constructor(Vector3(5778.706f, 3189.742f, 86.9484f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        2177,
        SpawnTube.Constructor(Vector3(5778.706f, 3206.152f, 86.9484f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        1864,
        ProximityTerminal.Constructor(Vector3(5766.907f, 3196.725f, 125.0364f), pad_landing_tower_frame),
        owning_building_guid = 72
      )
      LocalObject(
        1865,
        Terminal.Constructor(Vector3(5766.907f, 3196.725f, 125.0364f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1867,
        ProximityTerminal.Constructor(Vector3(5766.907f, 3207.17f, 125.0364f), pad_landing_tower_frame),
        owning_building_guid = 72
      )
      LocalObject(
        1868,
        Terminal.Constructor(Vector3(5766.907f, 3207.17f, 125.0364f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1460,
        FacilityTurret.Constructor(Vector3(5753.07f, 3187.045f, 116.4084f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(1460, 5095)
      LocalObject(
        1463,
        FacilityTurret.Constructor(Vector3(5791.497f, 3216.957f, 116.4084f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(1463, 5096)
      LocalObject(
        2011,
        Painbox.Constructor(Vector3(5772.454f, 3194.849f, 89.4859f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2012,
        Painbox.Constructor(Vector3(5784.923f, 3191.54f, 87.5664f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2013,
        Painbox.Constructor(Vector3(5785.113f, 3204.022f, 87.5664f), painbox_radius_continuous),
        owning_building_guid = 72
      )
    }

    Building21()

    def Building21(): Unit = { // Name: NE_Pinga_Tower Type: tower_c GUID: 73, MapID: 21
      LocalBuilding(
        "NE_Pinga_Tower",
        73,
        21,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6320f, 3992f, 37.5779f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2211,
        CaptureTerminal.Constructor(Vector3(6336.587f, 3991.897f, 47.5769f), secondary_capture),
        owning_building_guid = 73
      )
      LocalObject(508, Door.Constructor(Vector3(6332f, 3984f, 39.0989f)), owning_building_guid = 73)
      LocalObject(509, Door.Constructor(Vector3(6332f, 3984f, 59.0979f)), owning_building_guid = 73)
      LocalObject(510, Door.Constructor(Vector3(6332f, 4000f, 39.0989f)), owning_building_guid = 73)
      LocalObject(511, Door.Constructor(Vector3(6332f, 4000f, 59.0979f)), owning_building_guid = 73)
      LocalObject(2358, Door.Constructor(Vector3(6331.146f, 3980.794f, 28.9139f)), owning_building_guid = 73)
      LocalObject(2359, Door.Constructor(Vector3(6331.146f, 3997.204f, 28.9139f)), owning_building_guid = 73)
      LocalObject(
        967,
        IFFLock.Constructor(Vector3(6329.957f, 4000.811f, 39.0389f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 510
      )
      LocalObject(
        968,
        IFFLock.Constructor(Vector3(6329.957f, 4000.811f, 59.0389f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 511
      )
      LocalObject(
        969,
        IFFLock.Constructor(Vector3(6334.047f, 3983.189f, 39.0389f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 508
      )
      LocalObject(
        970,
        IFFLock.Constructor(Vector3(6334.047f, 3983.189f, 59.0389f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 509
      )
      LocalObject(1330, Locker.Constructor(Vector3(6335.716f, 3976.963f, 27.5719f)), owning_building_guid = 73)
      LocalObject(1331, Locker.Constructor(Vector3(6335.751f, 3998.835f, 27.5719f)), owning_building_guid = 73)
      LocalObject(1332, Locker.Constructor(Vector3(6337.053f, 3976.963f, 27.5719f)), owning_building_guid = 73)
      LocalObject(1333, Locker.Constructor(Vector3(6337.088f, 3998.835f, 27.5719f)), owning_building_guid = 73)
      LocalObject(1334, Locker.Constructor(Vector3(6339.741f, 3976.963f, 27.5719f)), owning_building_guid = 73)
      LocalObject(1335, Locker.Constructor(Vector3(6339.741f, 3998.835f, 27.5719f)), owning_building_guid = 73)
      LocalObject(1336, Locker.Constructor(Vector3(6341.143f, 3976.963f, 27.5719f)), owning_building_guid = 73)
      LocalObject(1337, Locker.Constructor(Vector3(6341.143f, 3998.835f, 27.5719f)), owning_building_guid = 73)
      LocalObject(
        1629,
        Terminal.Constructor(Vector3(6341.445f, 3982.129f, 28.9099f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1630,
        Terminal.Constructor(Vector3(6341.445f, 3987.853f, 28.9099f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1631,
        Terminal.Constructor(Vector3(6341.445f, 3993.234f, 28.9099f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        2181,
        SpawnTube.Constructor(Vector3(6330.706f, 3979.742f, 27.0599f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        2182,
        SpawnTube.Constructor(Vector3(6330.706f, 3996.152f, 27.0599f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        1870,
        ProximityTerminal.Constructor(Vector3(6318.907f, 3986.725f, 65.1479f), pad_landing_tower_frame),
        owning_building_guid = 73
      )
      LocalObject(
        1871,
        Terminal.Constructor(Vector3(6318.907f, 3986.725f, 65.1479f), air_rearm_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1873,
        ProximityTerminal.Constructor(Vector3(6318.907f, 3997.17f, 65.1479f), pad_landing_tower_frame),
        owning_building_guid = 73
      )
      LocalObject(
        1874,
        Terminal.Constructor(Vector3(6318.907f, 3997.17f, 65.1479f), air_rearm_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1468,
        FacilityTurret.Constructor(Vector3(6305.07f, 3977.045f, 56.5199f), manned_turret),
        owning_building_guid = 73
      )
      TurretToWeapon(1468, 5097)
      LocalObject(
        1469,
        FacilityTurret.Constructor(Vector3(6343.497f, 4006.957f, 56.5199f), manned_turret),
        owning_building_guid = 73
      )
      TurretToWeapon(1469, 5098)
      LocalObject(
        2014,
        Painbox.Constructor(Vector3(6324.454f, 3984.849f, 29.5974f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2015,
        Painbox.Constructor(Vector3(6336.923f, 3981.54f, 27.6779f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2016,
        Painbox.Constructor(Vector3(6337.113f, 3994.022f, 27.6779f), painbox_radius_continuous),
        owning_building_guid = 73
      )
    }

    Building12()

    def Building12(): Unit = { // Name: WG_Ceryshen_to_Forseral Type: warpgate GUID: 74, MapID: 12
      LocalBuilding(
        "WG_Ceryshen_to_Forseral",
        74,
        12,
        FoundationBuilder(WarpGate.Structure(Vector3(2190f, 5506f, 30.16494f)))
      )
    }

    Building11()

    def Building11(): Unit = { // Name: WG_Ceryshen_to_Amerish Type: warpgate GUID: 75, MapID: 11
      LocalBuilding(
        "WG_Ceryshen_to_Amerish",
        75,
        11,
        FoundationBuilder(WarpGate.Structure(Vector3(2588f, 2998f, 186.0018f)))
      )
    }

    Building10()

    def Building10(): Unit = { // Name: WG_Ceryshen_to_Ishundar Type: warpgate GUID: 76, MapID: 10
      LocalBuilding(
        "WG_Ceryshen_to_Ishundar",
        76,
        10,
        FoundationBuilder(WarpGate.Structure(Vector3(4970f, 2082f, 15.07462f)))
      )
    }

    Building13()

    def Building13(): Unit = { // Name: WG_Ceryshen_to_Hossin Type: warpgate GUID: 77, MapID: 13
      LocalBuilding(
        "WG_Ceryshen_to_Hossin",
        77,
        13,
        FoundationBuilder(WarpGate.Structure(Vector3(5028f, 4498f, 40.75579f)))
      )
    }

    def Lattice(): Unit = {
      LatticeLink("Igaluk", "Sedna")
      LatticeLink("Tootega", "Pinga")
      LatticeLink("Anguta", "Sedna")
      LatticeLink("Anguta", "Nerrivik")
      LatticeLink("Anguta", "Akna")
      LatticeLink("Anguta", "Tootega")
      LatticeLink("Igaluk", "WG_Ceryshen_to_Forseral")
      LatticeLink("Tarqaq", "WG_Ceryshen_to_Amerish")
      LatticeLink("Keelut", "WG_Ceryshen_to_Ishundar")
      LatticeLink("Tarqaq", "GW_Ceryshen_S")
      LatticeLink("Igaluk", "GW_Ceryshen_N")
      LatticeLink("Pinga", "WG_Ceryshen_to_Hossin")
      LatticeLink("Igaluk", "Nerrivik")
      LatticeLink("Sedna", "Nerrivik")
      LatticeLink("Nerrivik", "Tarqaq")
      LatticeLink("Tarqaq", "Keelut")
      LatticeLink("Keelut", "Akna")
      LatticeLink("Keelut", "Tootega")
      LatticeLink("Akna", "Nerrivik")
      LatticeLink("Akna", "Tootega")
    }

    Lattice()

  }
}
