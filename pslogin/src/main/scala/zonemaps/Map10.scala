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

object Map10 { // Amerish
  val ZoneMap = new ZoneMap("map10") {
    Checksum = 230810349L

    Building13()

    def Building13(): Unit = { // Name: Sungrey Type: amp_station GUID: 1, MapID: 13
      LocalBuilding(
        "Sungrey",
        1,
        13,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4686f, 5546f, 72.05738f),
            Vector3(0f, 0f, 89f),
            amp_station
          )
        )
      )
      LocalObject(
        197,
        CaptureTerminal.Constructor(Vector3(4685.939f, 5542.665f, 83.56538f), capture_terminal),
        owning_building_guid = 1
      )
      LocalObject(151, Door.Constructor(Vector3(4679.2f, 5546.333f, 84.95938f)), owning_building_guid = 1)
      LocalObject(152, Door.Constructor(Vector3(4692.808f, 5546.092f, 84.95938f)), owning_building_guid = 1)
      LocalObject(405, Door.Constructor(Vector3(4593.418f, 5524.11f, 73.80838f)), owning_building_guid = 1)
      LocalObject(406, Door.Constructor(Vector3(4593.735f, 5542.3f, 81.77238f)), owning_building_guid = 1)
      LocalObject(407, Door.Constructor(Vector3(4604.686f, 5471.408f, 73.77838f)), owning_building_guid = 1)
      LocalObject(410, Door.Constructor(Vector3(4620.922f, 5479.148f, 81.77138f)), owning_building_guid = 1)
      LocalObject(413, Door.Constructor(Vector3(4639.113f, 5478.831f, 73.80838f)), owning_building_guid = 1)
      LocalObject(415, Door.Constructor(Vector3(4650.504f, 5546.62f, 78.77838f)), owning_building_guid = 1)
      LocalObject(416, Door.Constructor(Vector3(4655.354f, 5542.224f, 83.78438f)), owning_building_guid = 1)
      LocalObject(417, Door.Constructor(Vector3(4655.512f, 5551.284f, 83.78438f)), owning_building_guid = 1)
      LocalObject(429, Door.Constructor(Vector3(4716.496f, 5541.136f, 83.78438f)), owning_building_guid = 1)
      LocalObject(430, Door.Constructor(Vector3(4716.653f, 5550.196f, 83.78438f)), owning_building_guid = 1)
      LocalObject(431, Door.Constructor(Vector3(4721.495f, 5545.38f, 78.77838f)), owning_building_guid = 1)
      LocalObject(432, Door.Constructor(Vector3(4722.115f, 5477.382f, 81.77138f)), owning_building_guid = 1)
      LocalObject(433, Door.Constructor(Vector3(4731.545f, 5605.417f, 73.80838f)), owning_building_guid = 1)
      LocalObject(435, Door.Constructor(Vector3(4740.305f, 5477.065f, 73.80838f)), owning_building_guid = 1)
      LocalObject(436, Door.Constructor(Vector3(4749.734f, 5605.1f, 81.77238f)), owning_building_guid = 1)
      LocalObject(437, Door.Constructor(Vector3(4770.511f, 5562.031f, 81.77238f)), owning_building_guid = 1)
      LocalObject(438, Door.Constructor(Vector3(4770.828f, 5580.22f, 73.80838f)), owning_building_guid = 1)
      LocalObject(694, Door.Constructor(Vector3(4656.748f, 5474.5f, 66.27838f)), owning_building_guid = 1)
      LocalObject(695, Door.Constructor(Vector3(4657.865f, 5538.49f, 73.77838f)), owning_building_guid = 1)
      LocalObject(698, Door.Constructor(Vector3(4659.439f, 5546.676f, 83.77838f)), owning_building_guid = 1)
      LocalObject(699, Door.Constructor(Vector3(4661.515f, 5518.423f, 66.27838f)), owning_building_guid = 1)
      LocalObject(700, Door.Constructor(Vector3(4661.794f, 5534.421f, 73.77838f)), owning_building_guid = 1)
      LocalObject(702, Door.Constructor(Vector3(4662.003f, 5546.419f, 78.77838f)), owning_building_guid = 1)
      LocalObject(703, Door.Constructor(Vector3(4665.863f, 5538.35f, 66.27838f)), owning_building_guid = 1)
      LocalObject(704, Door.Constructor(Vector3(4669.933f, 5542.28f, 58.77838f)), owning_building_guid = 1)
      LocalObject(706, Door.Constructor(Vector3(4677.792f, 5534.142f, 66.27838f)), owning_building_guid = 1)
      LocalObject(707, Door.Constructor(Vector3(4682f, 5546.07f, 66.27838f)), owning_building_guid = 1)
      LocalObject(708, Door.Constructor(Vector3(4682.35f, 5566.067f, 73.77838f)), owning_building_guid = 1)
      LocalObject(709, Door.Constructor(Vector3(4685.232f, 5502.007f, 58.77838f)), owning_building_guid = 1)
      LocalObject(711, Door.Constructor(Vector3(4689.162f, 5497.938f, 58.77838f)), owning_building_guid = 1)
      LocalObject(712, Door.Constructor(Vector3(4690.349f, 5565.927f, 73.77838f)), owning_building_guid = 1)
      LocalObject(713, Door.Constructor(Vector3(4697.439f, 5513.795f, 58.77838f)), owning_building_guid = 1)
      LocalObject(714, Door.Constructor(Vector3(4697.998f, 5545.791f, 58.77838f)), owning_building_guid = 1)
      LocalObject(715, Door.Constructor(Vector3(4701.788f, 5533.723f, 58.77838f)), owning_building_guid = 1)
      LocalObject(716, Door.Constructor(Vector3(4701.788f, 5533.723f, 66.27838f)), owning_building_guid = 1)
      LocalObject(718, Door.Constructor(Vector3(4709.787f, 5533.583f, 73.77838f)), owning_building_guid = 1)
      LocalObject(719, Door.Constructor(Vector3(4709.997f, 5545.581f, 78.77838f)), owning_building_guid = 1)
      LocalObject(720, Door.Constructor(Vector3(4712.569f, 5545.748f, 83.77838f)), owning_building_guid = 1)
      LocalObject(721, Door.Constructor(Vector3(4713.856f, 5537.513f, 73.77838f)), owning_building_guid = 1)
      LocalObject(836, Door.Constructor(Vector3(4686.483f, 5575.17f, 74.53738f)), owning_building_guid = 1)
      LocalObject(2532, Door.Constructor(Vector3(4681.348f, 5526.751f, 66.61138f)), owning_building_guid = 1)
      LocalObject(2533, Door.Constructor(Vector3(4688.636f, 5526.624f, 66.61138f)), owning_building_guid = 1)
      LocalObject(2534, Door.Constructor(Vector3(4695.928f, 5526.497f, 66.61138f)), owning_building_guid = 1)
      LocalObject(
        884,
        IFFLock.Constructor(Vector3(4683.492f, 5577.964f, 73.73738f), Vector3(0, 0, 1)),
        owning_building_guid = 1,
        door_guid = 836
      )
      LocalObject(
        989,
        IFFLock.Constructor(Vector3(4603.824f, 5469.378f, 73.71738f), Vector3(0, 0, 271)),
        owning_building_guid = 1,
        door_guid = 407
      )
      LocalObject(
        994,
        IFFLock.Constructor(Vector3(4649.649f, 5544.59f, 78.71938f), Vector3(0, 0, 271)),
        owning_building_guid = 1,
        door_guid = 415
      )
      LocalObject(
        996,
        IFFLock.Constructor(Vector3(4653.469f, 5552.111f, 83.71838f), Vector3(0, 0, 1)),
        owning_building_guid = 1,
        door_guid = 417
      )
      LocalObject(
        998,
        IFFLock.Constructor(Vector3(4657.375f, 5541.355f, 83.71838f), Vector3(0, 0, 181)),
        owning_building_guid = 1,
        door_guid = 416
      )
      LocalObject(
        1000,
        IFFLock.Constructor(Vector3(4676.824f, 5532.586f, 66.09338f), Vector3(0, 0, 271)),
        owning_building_guid = 1,
        door_guid = 706
      )
      LocalObject(
        1001,
        IFFLock.Constructor(Vector3(4687.604f, 5498.775f, 58.59338f), Vector3(0, 0, 1)),
        owning_building_guid = 1,
        door_guid = 711
      )
      LocalObject(
        1002,
        IFFLock.Constructor(Vector3(4699.554f, 5544.823f, 58.59338f), Vector3(0, 0, 181)),
        owning_building_guid = 1,
        door_guid = 714
      )
      LocalObject(
        1003,
        IFFLock.Constructor(Vector3(4702.625f, 5535.28f, 66.09338f), Vector3(0, 0, 91)),
        owning_building_guid = 1,
        door_guid = 716
      )
      LocalObject(
        1008,
        IFFLock.Constructor(Vector3(4714.601f, 5551.043f, 83.71838f), Vector3(0, 0, 1)),
        owning_building_guid = 1,
        door_guid = 430
      )
      LocalObject(
        1012,
        IFFLock.Constructor(Vector3(4718.502f, 5540.288f, 83.71838f), Vector3(0, 0, 181)),
        owning_building_guid = 1,
        door_guid = 429
      )
      LocalObject(
        1013,
        IFFLock.Constructor(Vector3(4722.33f, 5547.408f, 78.71938f), Vector3(0, 0, 91)),
        owning_building_guid = 1,
        door_guid = 431
      )
      LocalObject(1289, Locker.Constructor(Vector3(4679.874f, 5548.301f, 57.25738f)), owning_building_guid = 1)
      LocalObject(1290, Locker.Constructor(Vector3(4679.897f, 5549.625f, 57.25738f)), owning_building_guid = 1)
      LocalObject(1291, Locker.Constructor(Vector3(4679.92f, 5550.961f, 57.25738f)), owning_building_guid = 1)
      LocalObject(1292, Locker.Constructor(Vector3(4679.944f, 5552.298f, 57.25738f)), owning_building_guid = 1)
      LocalObject(1293, Locker.Constructor(Vector3(4680.023f, 5556.837f, 57.25738f)), owning_building_guid = 1)
      LocalObject(1294, Locker.Constructor(Vector3(4680.046f, 5558.161f, 57.25738f)), owning_building_guid = 1)
      LocalObject(1295, Locker.Constructor(Vector3(4680.07f, 5559.497f, 57.25738f)), owning_building_guid = 1)
      LocalObject(1296, Locker.Constructor(Vector3(4680.093f, 5560.833f, 57.25738f)), owning_building_guid = 1)
      LocalObject(1297, Locker.Constructor(Vector3(4699.709f, 5537.322f, 65.01838f)), owning_building_guid = 1)
      LocalObject(1298, Locker.Constructor(Vector3(4699.73f, 5538.486f, 65.01838f)), owning_building_guid = 1)
      LocalObject(1299, Locker.Constructor(Vector3(4699.75f, 5539.633f, 65.01838f)), owning_building_guid = 1)
      LocalObject(1300, Locker.Constructor(Vector3(4699.77f, 5540.782f, 65.01838f)), owning_building_guid = 1)
      LocalObject(
        1724,
        Terminal.Constructor(Vector3(4663.526f, 5546.39f, 73.58638f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1725,
        Terminal.Constructor(Vector3(4685.612f, 5523.536f, 73.58638f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1726,
        Terminal.Constructor(Vector3(4686.979f, 5540.636f, 66.34738f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1727,
        Terminal.Constructor(Vector3(4690.767f, 5540.57f, 66.34738f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1728,
        Terminal.Constructor(Vector3(4694.498f, 5540.505f, 66.34738f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1729,
        Terminal.Constructor(Vector3(4708.474f, 5545.606f, 73.58638f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2446,
        Terminal.Constructor(Vector3(4645.76f, 5498.753f, 66.31438f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2450,
        Terminal.Constructor(Vector3(4670.175f, 5522.331f, 58.81438f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2451,
        Terminal.Constructor(Vector3(4683.845f, 5527.005f, 66.89138f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2452,
        Terminal.Constructor(Vector3(4690.403f, 5540.418f, 78.78538f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2453,
        Terminal.Constructor(Vector3(4691.132f, 5526.875f, 66.89138f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2454,
        Terminal.Constructor(Vector3(4698.423f, 5526.751f, 66.89138f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2651,
        Terminal.Constructor(Vector3(4602.703f, 5499.521f, 74.15938f), vehicle_terminal_combined),
        owning_building_guid = 1
      )
      LocalObject(
        1632,
        VehicleSpawnPad.Constructor(Vector3(4616.341f, 5499.374f, 70.00238f), mb_pad_creation, Vector3(0, 0, 91)),
        owning_building_guid = 1,
        terminal_guid = 2651
      )
      LocalObject(2299, ResourceSilo.Constructor(Vector3(4772.401f, 5597.145f, 79.29238f)), owning_building_guid = 1)
      LocalObject(
        2345,
        SpawnTube.Constructor(Vector3(4682.394f, 5526.293f, 64.75738f), Vector3(0, 0, 271)),
        owning_building_guid = 1
      )
      LocalObject(
        2346,
        SpawnTube.Constructor(Vector3(4689.68f, 5526.166f, 64.75738f), Vector3(0, 0, 271)),
        owning_building_guid = 1
      )
      LocalObject(
        2347,
        SpawnTube.Constructor(Vector3(4696.97f, 5526.039f, 64.75738f), Vector3(0, 0, 271)),
        owning_building_guid = 1
      )
      LocalObject(
        1651,
        ProximityTerminal.Constructor(Vector3(4680.528f, 5554.541f, 57.25738f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1652,
        ProximityTerminal.Constructor(Vector3(4685.621f, 5523.955f, 77.25738f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1909,
        ProximityTerminal.Constructor(Vector3(4629.061f, 5573.595f, 82.72338f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1910,
        Terminal.Constructor(Vector3(4629.061f, 5573.595f, 82.72338f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1921,
        ProximityTerminal.Constructor(Vector3(4665.233f, 5489.106f, 80.56238f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1922,
        Terminal.Constructor(Vector3(4665.233f, 5489.106f, 80.56238f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1930,
        ProximityTerminal.Constructor(Vector3(4706.692f, 5595.021f, 80.56238f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1931,
        Terminal.Constructor(Vector3(4706.692f, 5595.021f, 80.56238f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1933,
        ProximityTerminal.Constructor(Vector3(4759.628f, 5553.014f, 80.58538f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1934,
        Terminal.Constructor(Vector3(4759.628f, 5553.014f, 80.58538f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2241,
        ProximityTerminal.Constructor(Vector3(4684.573f, 5475.918f, 71.45738f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2242,
        Terminal.Constructor(Vector3(4684.573f, 5475.918f, 71.45738f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2245,
        ProximityTerminal.Constructor(Vector3(4686.694f, 5608.322f, 71.45738f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        2246,
        Terminal.Constructor(Vector3(4686.694f, 5608.322f, 71.45738f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1566,
        FacilityTurret.Constructor(Vector3(4579.77f, 5467.218f, 80.76538f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1566, 5000)
      LocalObject(
        1567,
        FacilityTurret.Constructor(Vector3(4582.458f, 5620.663f, 80.76538f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1567, 5001)
      LocalObject(
        1568,
        FacilityTurret.Constructor(Vector3(4645.869f, 5621.774f, 80.76538f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1568, 5002)
      LocalObject(
        1572,
        FacilityTurret.Constructor(Vector3(4781.417f, 5463.727f, 80.76538f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1572, 5003)
      LocalObject(
        1573,
        FacilityTurret.Constructor(Vector3(4784.104f, 5617.134f, 80.76538f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1573, 5004)
      LocalObject(
        1574,
        FacilityTurret.Constructor(Vector3(4784.684f, 5523.266f, 80.76538f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1574, 5005)
      LocalObject(2062, Painbox.Constructor(Vector3(4691.837f, 5486.04f, 62.12838f), painbox), owning_building_guid = 1)
      LocalObject(
        2074,
        Painbox.Constructor(Vector3(4687.197f, 5533.582f, 69.70578f), painbox_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2086,
        Painbox.Constructor(Vector3(4689.956f, 5500.119f, 58.71638f), painbox_door_radius),
        owning_building_guid = 1
      )
      LocalObject(
        2110,
        Painbox.Constructor(Vector3(4676.509f, 5534.238f, 66.02188f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2111,
        Painbox.Constructor(Vector3(4682.069f, 5547.536f, 67.93598f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2112,
        Painbox.Constructor(Vector3(4702.852f, 5533.705f, 65.95738f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(278, Generator.Constructor(Vector3(4688.915f, 5482.384f, 55.96338f)), owning_building_guid = 1)
      LocalObject(
        266,
        Terminal.Constructor(Vector3(4689.011f, 5490.576f, 57.25738f), gen_control),
        owning_building_guid = 1
      )
    }

    Building49()

    def Building49(): Unit = { // Name: Verica Type: amp_station GUID: 4, MapID: 49
      LocalBuilding(
        "Verica",
        4,
        49,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4902f, 3534f, 47.26933f),
            Vector3(0f, 0f, 270f),
            amp_station
          )
        )
      )
      LocalObject(
        198,
        CaptureTerminal.Constructor(Vector3(4902.003f, 3537.336f, 58.77732f), capture_terminal),
        owning_building_guid = 4
      )
      LocalObject(153, Door.Constructor(Vector3(4895.195f, 3533.789f, 60.17133f)), owning_building_guid = 4)
      LocalObject(154, Door.Constructor(Vector3(4908.805f, 3533.786f, 60.17133f)), owning_building_guid = 4)
      LocalObject(439, Door.Constructor(Vector3(4817.782f, 3498.305f, 49.02032f)), owning_building_guid = 4)
      LocalObject(440, Door.Constructor(Vector3(4817.782f, 3516.497f, 56.98433f)), owning_building_guid = 4)
      LocalObject(445, Door.Constructor(Vector3(4839.307f, 3473.797f, 56.98433f)), owning_building_guid = 4)
      LocalObject(446, Door.Constructor(Vector3(4846.5f, 3601.977f, 49.02032f)), owning_building_guid = 4)
      LocalObject(447, Door.Constructor(Vector3(4857.499f, 3473.797f, 49.02032f)), owning_building_guid = 4)
      LocalObject(448, Door.Constructor(Vector3(4864.693f, 3601.977f, 56.98333f)), owning_building_guid = 4)
      LocalObject(449, Door.Constructor(Vector3(4866.5f, 3534f, 53.99033f)), owning_building_guid = 4)
      LocalObject(450, Door.Constructor(Vector3(4871.425f, 3529.27f, 58.99633f)), owning_building_guid = 4)
      LocalObject(451, Door.Constructor(Vector3(4871.424f, 3538.331f, 58.99633f)), owning_building_guid = 4)
      LocalObject(452, Door.Constructor(Vector3(4932.576f, 3529.249f, 58.99633f)), owning_building_guid = 4)
      LocalObject(453, Door.Constructor(Vector3(4932.575f, 3538.31f, 58.99633f)), owning_building_guid = 4)
      LocalObject(454, Door.Constructor(Vector3(4937.501f, 3534f, 53.99033f)), owning_building_guid = 4)
      LocalObject(455, Door.Constructor(Vector3(4947.708f, 3601.977f, 49.02032f)), owning_building_guid = 4)
      LocalObject(456, Door.Constructor(Vector3(4965.901f, 3601.977f, 56.98333f)), owning_building_guid = 4)
      LocalObject(457, Door.Constructor(Vector3(4982f, 3610f, 48.99033f)), owning_building_guid = 4)
      LocalObject(462, Door.Constructor(Vector3(4994.186f, 3539.31f, 56.98433f)), owning_building_guid = 4)
      LocalObject(463, Door.Constructor(Vector3(4994.186f, 3557.502f, 49.02032f)), owning_building_guid = 4)
      LocalObject(722, Door.Constructor(Vector3(4874f, 3542f, 48.99033f)), owning_building_guid = 4)
      LocalObject(723, Door.Constructor(Vector3(4875.431f, 3533.788f, 58.99033f)), owning_building_guid = 4)
      LocalObject(724, Door.Constructor(Vector3(4878f, 3534f, 53.99033f)), owning_building_guid = 4)
      LocalObject(725, Door.Constructor(Vector3(4878f, 3546f, 48.99033f)), owning_building_guid = 4)
      LocalObject(726, Door.Constructor(Vector3(4886f, 3546f, 33.99033f)), owning_building_guid = 4)
      LocalObject(727, Door.Constructor(Vector3(4886f, 3546f, 41.49033f)), owning_building_guid = 4)
      LocalObject(728, Door.Constructor(Vector3(4890f, 3534f, 33.99033f)), owning_building_guid = 4)
      LocalObject(729, Door.Constructor(Vector3(4890f, 3566f, 33.99033f)), owning_building_guid = 4)
      LocalObject(730, Door.Constructor(Vector3(4898f, 3514f, 48.99033f)), owning_building_guid = 4)
      LocalObject(731, Door.Constructor(Vector3(4898f, 3582f, 33.99033f)), owning_building_guid = 4)
      LocalObject(732, Door.Constructor(Vector3(4902f, 3578f, 33.99033f)), owning_building_guid = 4)
      LocalObject(733, Door.Constructor(Vector3(4906f, 3514f, 48.99033f)), owning_building_guid = 4)
      LocalObject(734, Door.Constructor(Vector3(4906f, 3534f, 41.49033f)), owning_building_guid = 4)
      LocalObject(735, Door.Constructor(Vector3(4910f, 3546f, 41.49033f)), owning_building_guid = 4)
      LocalObject(736, Door.Constructor(Vector3(4918f, 3538f, 33.99033f)), owning_building_guid = 4)
      LocalObject(737, Door.Constructor(Vector3(4922f, 3542f, 41.49033f)), owning_building_guid = 4)
      LocalObject(738, Door.Constructor(Vector3(4926f, 3534f, 53.99033f)), owning_building_guid = 4)
      LocalObject(739, Door.Constructor(Vector3(4926f, 3546f, 48.99033f)), owning_building_guid = 4)
      LocalObject(740, Door.Constructor(Vector3(4926f, 3562f, 41.49033f)), owning_building_guid = 4)
      LocalObject(741, Door.Constructor(Vector3(4928.569f, 3533.788f, 58.99033f)), owning_building_guid = 4)
      LocalObject(742, Door.Constructor(Vector3(4930f, 3542f, 48.99033f)), owning_building_guid = 4)
      LocalObject(743, Door.Constructor(Vector3(4930f, 3606f, 41.49033f)), owning_building_guid = 4)
      LocalObject(837, Door.Constructor(Vector3(4902.026f, 3504.826f, 49.74932f)), owning_building_guid = 4)
      LocalObject(2539, Door.Constructor(Vector3(4891.733f, 3553.327f, 41.82333f)), owning_building_guid = 4)
      LocalObject(2540, Door.Constructor(Vector3(4899.026f, 3553.327f, 41.82333f)), owning_building_guid = 4)
      LocalObject(2541, Door.Constructor(Vector3(4906.315f, 3553.327f, 41.82333f)), owning_building_guid = 4)
      LocalObject(
        885,
        IFFLock.Constructor(Vector3(4905.066f, 3502.085f, 48.94933f), Vector3(0, 0, 180)),
        owning_building_guid = 4,
        door_guid = 837
      )
      LocalObject(
        1018,
        IFFLock.Constructor(Vector3(4865.7f, 3531.958f, 53.93132f), Vector3(0, 0, 270)),
        owning_building_guid = 4,
        door_guid = 449
      )
      LocalObject(
        1019,
        IFFLock.Constructor(Vector3(4869.403f, 3539.144f, 58.93032f), Vector3(0, 0, 0)),
        owning_building_guid = 4,
        door_guid = 451
      )
      LocalObject(
        1020,
        IFFLock.Constructor(Vector3(4873.492f, 3528.458f, 58.93032f), Vector3(0, 0, 180)),
        owning_building_guid = 4,
        door_guid = 450
      )
      LocalObject(
        1021,
        IFFLock.Constructor(Vector3(4885.19f, 3544.428f, 41.30532f), Vector3(0, 0, 270)),
        owning_building_guid = 4,
        door_guid = 727
      )
      LocalObject(
        1022,
        IFFLock.Constructor(Vector3(4888.428f, 3534.94f, 33.80532f), Vector3(0, 0, 0)),
        owning_building_guid = 4,
        door_guid = 728
      )
      LocalObject(
        1023,
        IFFLock.Constructor(Vector3(4899.572f, 3581.19f, 33.80532f), Vector3(0, 0, 180)),
        owning_building_guid = 4,
        door_guid = 731
      )
      LocalObject(
        1024,
        IFFLock.Constructor(Vector3(4910.941f, 3547.572f, 41.30532f), Vector3(0, 0, 90)),
        owning_building_guid = 4,
        door_guid = 735
      )
      LocalObject(
        1025,
        IFFLock.Constructor(Vector3(4930.54f, 3539.144f, 58.93032f), Vector3(0, 0, 0)),
        owning_building_guid = 4,
        door_guid = 453
      )
      LocalObject(
        1026,
        IFFLock.Constructor(Vector3(4934.633f, 3528.458f, 58.93032f), Vector3(0, 0, 180)),
        owning_building_guid = 4,
        door_guid = 452
      )
      LocalObject(
        1027,
        IFFLock.Constructor(Vector3(4938.321f, 3536.044f, 53.93132f), Vector3(0, 0, 90)),
        owning_building_guid = 4,
        door_guid = 454
      )
      LocalObject(
        1028,
        IFFLock.Constructor(Vector3(4982.826f, 3612.044f, 48.92933f), Vector3(0, 0, 90)),
        owning_building_guid = 4,
        door_guid = 457
      )
      LocalObject(1317, Locker.Constructor(Vector3(4888.141f, 3538.977f, 40.23032f)), owning_building_guid = 4)
      LocalObject(1318, Locker.Constructor(Vector3(4888.141f, 3540.126f, 40.23032f)), owning_building_guid = 4)
      LocalObject(1319, Locker.Constructor(Vector3(4888.141f, 3541.273f, 40.23032f)), owning_building_guid = 4)
      LocalObject(1320, Locker.Constructor(Vector3(4888.141f, 3542.437f, 40.23032f)), owning_building_guid = 4)
      LocalObject(1321, Locker.Constructor(Vector3(4908.165f, 3519.272f, 32.46933f)), owning_building_guid = 4)
      LocalObject(1322, Locker.Constructor(Vector3(4908.165f, 3520.609f, 32.46933f)), owning_building_guid = 4)
      LocalObject(1323, Locker.Constructor(Vector3(4908.165f, 3521.945f, 32.46933f)), owning_building_guid = 4)
      LocalObject(1324, Locker.Constructor(Vector3(4908.165f, 3523.269f, 32.46933f)), owning_building_guid = 4)
      LocalObject(1325, Locker.Constructor(Vector3(4908.165f, 3527.809f, 32.46933f)), owning_building_guid = 4)
      LocalObject(1326, Locker.Constructor(Vector3(4908.165f, 3529.146f, 32.46933f)), owning_building_guid = 4)
      LocalObject(1327, Locker.Constructor(Vector3(4908.165f, 3530.482f, 32.46933f)), owning_building_guid = 4)
      LocalObject(1328, Locker.Constructor(Vector3(4908.165f, 3531.806f, 32.46933f)), owning_building_guid = 4)
      LocalObject(
        1736,
        Terminal.Constructor(Vector3(4879.523f, 3534.002f, 48.79832f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1737,
        Terminal.Constructor(Vector3(4893.408f, 3539.346f, 41.55933f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1738,
        Terminal.Constructor(Vector3(4897.139f, 3539.346f, 41.55933f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1739,
        Terminal.Constructor(Vector3(4900.928f, 3539.346f, 41.55933f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1740,
        Terminal.Constructor(Vector3(4901.996f, 3556.467f, 48.79832f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1741,
        Terminal.Constructor(Vector3(4924.477f, 3534.002f, 48.79832f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2456,
        Terminal.Constructor(Vector3(4889.243f, 3553.029f, 42.10332f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2457,
        Terminal.Constructor(Vector3(4896.535f, 3553.033f, 42.10332f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2458,
        Terminal.Constructor(Vector3(4897.5f, 3539.504f, 53.99733f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2459,
        Terminal.Constructor(Vector3(4903.823f, 3553.03f, 42.10332f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2460,
        Terminal.Constructor(Vector3(4917.409f, 3557.942f, 34.02633f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2461,
        Terminal.Constructor(Vector3(4941.409f, 3581.942f, 41.52633f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2653,
        Terminal.Constructor(Vector3(4984.473f, 3581.925f, 49.37133f), vehicle_terminal_combined),
        owning_building_guid = 4
      )
      LocalObject(
        1634,
        VehicleSpawnPad.Constructor(Vector3(4970.835f, 3581.835f, 45.21432f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 4,
        terminal_guid = 2653
      )
      LocalObject(2300, ResourceSilo.Constructor(Vector3(4816.505f, 3481.355f, 54.50433f)), owning_building_guid = 4)
      LocalObject(
        2352,
        SpawnTube.Constructor(Vector3(4890.683f, 3553.767f, 39.96933f), Vector3(0, 0, 90)),
        owning_building_guid = 4
      )
      LocalObject(
        2353,
        SpawnTube.Constructor(Vector3(4897.974f, 3553.767f, 39.96933f), Vector3(0, 0, 90)),
        owning_building_guid = 4
      )
      LocalObject(
        2354,
        SpawnTube.Constructor(Vector3(4905.262f, 3553.767f, 39.96933f), Vector3(0, 0, 90)),
        owning_building_guid = 4
      )
      LocalObject(
        1653,
        ProximityTerminal.Constructor(Vector3(4901.994f, 3556.048f, 52.46933f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1654,
        ProximityTerminal.Constructor(Vector3(4907.62f, 3525.556f, 32.46933f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1936,
        ProximityTerminal.Constructor(Vector3(4828.505f, 3525.702f, 55.79733f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1937,
        Terminal.Constructor(Vector3(4828.505f, 3525.702f, 55.79733f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1939,
        ProximityTerminal.Constructor(Vector3(4882.167f, 3484.625f, 55.77433f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1940,
        Terminal.Constructor(Vector3(4882.167f, 3484.625f, 55.77433f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1942,
        ProximityTerminal.Constructor(Vector3(4921.771f, 3591.248f, 55.77433f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1943,
        Terminal.Constructor(Vector3(4921.771f, 3591.248f, 55.77433f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1945,
        ProximityTerminal.Constructor(Vector3(4959.412f, 3507.403f, 57.93533f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1946,
        Terminal.Constructor(Vector3(4959.412f, 3507.403f, 57.93533f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2253,
        ProximityTerminal.Constructor(Vector3(4902.204f, 3604.096f, 46.66933f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        2254,
        Terminal.Constructor(Vector3(4902.204f, 3604.096f, 46.66933f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2257,
        ProximityTerminal.Constructor(Vector3(4902.393f, 3471.675f, 46.66933f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        2258,
        Terminal.Constructor(Vector3(4902.393f, 3471.675f, 46.66933f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1575,
        FacilityTurret.Constructor(Vector3(4802.934f, 3555.008f, 55.97733f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1575, 5006)
      LocalObject(
        1576,
        FacilityTurret.Constructor(Vector3(4805.152f, 3461.165f, 55.97733f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1576, 5007)
      LocalObject(
        1577,
        FacilityTurret.Constructor(Vector3(4805.162f, 3614.595f, 55.97733f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1577, 5008)
      LocalObject(
        1580,
        FacilityTurret.Constructor(Vector3(4943.447f, 3458.938f, 55.97733f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1580, 5009)
      LocalObject(
        1583,
        FacilityTurret.Constructor(Vector3(5006.829f, 3461.156f, 55.97733f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1583, 5010)
      LocalObject(
        1584,
        FacilityTurret.Constructor(Vector3(5006.839f, 3614.624f, 55.97733f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1584, 5011)
      LocalObject(
        2063,
        Painbox.Constructor(Vector3(4895.117f, 3593.849f, 37.34032f), painbox),
        owning_building_guid = 4
      )
      LocalObject(
        2075,
        Painbox.Constructor(Vector3(4900.586f, 3546.396f, 44.91772f), painbox_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        2087,
        Painbox.Constructor(Vector3(4897.244f, 3579.805f, 33.92833f), painbox_door_radius),
        owning_building_guid = 4
      )
      LocalObject(
        2113,
        Painbox.Constructor(Vector3(4884.937f, 3546f, 41.16933f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        2114,
        Painbox.Constructor(Vector3(4905.957f, 3532.533f, 43.14793f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        2115,
        Painbox.Constructor(Vector3(4911.284f, 3545.926f, 41.23383f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(279, Generator.Constructor(Vector3(4897.975f, 3597.555f, 31.17533f)), owning_building_guid = 4)
      LocalObject(
        267,
        Terminal.Constructor(Vector3(4898.022f, 3589.363f, 32.46933f), gen_control),
        owning_building_guid = 4
      )
    }

    Building44()

    def Building44(): Unit = { // Name: bunker_gauntlet Type: bunker_gauntlet GUID: 7, MapID: 44
      LocalBuilding(
        "bunker_gauntlet",
        7,
        44,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4418f, 2340f, 47.18137f),
            Vector3(0f, 0f, 270f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(393, Door.Constructor(Vector3(4416.099f, 2315.077f, 48.70237f)), owning_building_guid = 7)
      LocalObject(394, Door.Constructor(Vector3(4416.088f, 2364.898f, 48.70237f)), owning_building_guid = 7)
    }

    Building41()

    def Building41(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 8, MapID: 41
      LocalBuilding(
        "bunker_lg",
        8,
        41,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3754f, 5092f, 57.63699f), Vector3(0f, 0f, 360f), bunker_lg)
        )
      )
      LocalObject(356, Door.Constructor(Vector3(3756.606f, 5094.557f, 59.15799f)), owning_building_guid = 8)
    }

    Building37()

    def Building37(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 9, MapID: 37
      LocalBuilding(
        "bunker_lg",
        9,
        37,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4288f, 2540f, 47.49414f), Vector3(0f, 0f, 225f), bunker_lg)
        )
      )
      LocalObject(385, Door.Constructor(Vector3(4287.965f, 2536.349f, 49.01514f)), owning_building_guid = 9)
    }

    Building42()

    def Building42(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 10, MapID: 42
      LocalBuilding(
        "bunker_lg",
        10,
        42,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4570f, 5668f, 69.13481f), Vector3(0f, 0f, 225f), bunker_lg)
        )
      )
      LocalObject(401, Door.Constructor(Vector3(4569.965f, 5664.349f, 70.65582f)), owning_building_guid = 10)
    }

    Building43()

    def Building43(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 11, MapID: 43
      LocalBuilding(
        "bunker_lg",
        11,
        43,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4734f, 6182f, 69.42805f), Vector3(0f, 0f, 45f), bunker_lg)
        )
      )
      LocalObject(434, Door.Constructor(Vector3(4734.035f, 6185.651f, 70.94905f)), owning_building_guid = 11)
    }

    Building40()

    def Building40(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 12, MapID: 40
      LocalBuilding(
        "bunker_lg",
        12,
        40,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(6116f, 5328f, 61.81373f), Vector3(0f, 0f, 270f), bunker_lg)
        )
      )
      LocalObject(512, Door.Constructor(Vector3(6118.557f, 5325.394f, 63.33473f)), owning_building_guid = 12)
    }

    Building36()

    def Building36(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 13, MapID: 36
      LocalBuilding(
        "bunker_sm",
        13,
        36,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3390f, 2748f, 69.11893f), Vector3(0f, 0f, 270f), bunker_sm)
        )
      )
      LocalObject(326, Door.Constructor(Vector3(3389.945f, 2746.775f, 70.63993f)), owning_building_guid = 13)
    }

    Building45()

    def Building45(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 14, MapID: 45
      LocalBuilding(
        "bunker_sm",
        14,
        45,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4006f, 5186f, 46.02675f), Vector3(0f, 0f, 180f), bunker_sm)
        )
      )
      LocalObject(374, Door.Constructor(Vector3(4004.775f, 5186.055f, 47.54775f)), owning_building_guid = 14)
    }

    Building50()

    def Building50(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 15, MapID: 50
      LocalBuilding(
        "bunker_sm",
        15,
        50,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(6156f, 2592f, 41.59035f), Vector3(0f, 0f, 90f), bunker_sm)
        )
      )
      LocalObject(517, Door.Constructor(Vector3(6156.055f, 2593.225f, 43.11135f)), owning_building_guid = 15)
    }

    Building38()

    def Building38(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 16, MapID: 38
      LocalBuilding(
        "bunker_sm",
        16,
        38,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(6168f, 3532f, 37.92495f), Vector3(0f, 0f, 224f), bunker_sm)
        )
      )
      LocalObject(518, Door.Constructor(Vector3(6167.081f, 3531.189f, 39.44595f)), owning_building_guid = 16)
    }

    Building39()

    def Building39(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 17, MapID: 39
      LocalBuilding(
        "bunker_sm",
        17,
        39,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(6550f, 4324f, 56.2203f), Vector3(0f, 0f, 90f), bunker_sm)
        )
      )
      LocalObject(550, Door.Constructor(Vector3(6550.055f, 4325.225f, 57.7413f)), owning_building_guid = 17)
    }

    Building6()

    def Building6(): Unit = { // Name: Cetan Type: comm_station GUID: 18, MapID: 6
      LocalBuilding(
        "Cetan",
        18,
        6,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3500f, 2544f, 47.96331f),
            Vector3(0f, 0f, 38f),
            comm_station
          )
        )
      )
      LocalObject(
        193,
        CaptureTerminal.Constructor(Vector3(3487.5f, 2631.611f, 30.66331f), capture_terminal),
        owning_building_guid = 18
      )
      LocalObject(329, Door.Constructor(Vector3(3458.543f, 2597.875f, 49.71431f)), owning_building_guid = 18)
      LocalObject(334, Door.Constructor(Vector3(3468.454f, 2487.239f, 57.67831f)), owning_building_guid = 18)
      LocalObject(335, Door.Constructor(Vector3(3472.879f, 2609.074f, 57.67731f)), owning_building_guid = 18)
      LocalObject(336, Door.Constructor(Vector3(3478.426f, 2623.59f, 49.68431f)), owning_building_guid = 18)
      LocalObject(337, Door.Constructor(Vector3(3479.655f, 2472.903f, 49.71431f)), owning_building_guid = 18)
      LocalObject(338, Door.Constructor(Vector3(3484.058f, 2531.919f, 54.68431f)), owning_building_guid = 18)
      LocalObject(339, Door.Constructor(Vector3(3489.638f, 2524.777f, 54.68431f)), owning_building_guid = 18)
      LocalObject(340, Door.Constructor(Vector3(3493.991f, 2564.686f, 54.68431f)), owning_building_guid = 18)
      LocalObject(347, Door.Constructor(Vector3(3501.115f, 2535.467f, 62.12331f)), owning_building_guid = 18)
      LocalObject(348, Door.Constructor(Vector3(3508.101f, 2464.286f, 57.67831f)), owning_building_guid = 18)
      LocalObject(349, Door.Constructor(Vector3(3522.438f, 2475.487f, 49.71431f)), owning_building_guid = 18)
      LocalObject(350, Door.Constructor(Vector3(3542.822f, 2586.974f, 49.71431f)), owning_building_guid = 18)
      LocalObject(351, Door.Constructor(Vector3(3554.022f, 2572.637f, 57.67831f)), owning_building_guid = 18)
      LocalObject(614, Door.Constructor(Vector3(3448.384f, 2564.586f, 42.18431f)), owning_building_guid = 18)
      LocalObject(615, Door.Constructor(Vector3(3451.93f, 2547.052f, 42.18431f)), owning_building_guid = 18)
      LocalObject(616, Door.Constructor(Vector3(3467.296f, 2579.362f, 42.18431f)), owning_building_guid = 18)
      LocalObject(617, Door.Constructor(Vector3(3469.464f, 2550.599f, 32.18431f)), owning_building_guid = 18)
      LocalObject(618, Door.Constructor(Vector3(3472.221f, 2573.058f, 32.18431f)), owning_building_guid = 18)
      LocalObject(619, Door.Constructor(Vector3(3474.389f, 2544.295f, 42.18431f)), owning_building_guid = 18)
      LocalObject(620, Door.Constructor(Vector3(3477.836f, 2572.368f, 42.18431f)), owning_building_guid = 18)
      LocalObject(621, Door.Constructor(Vector3(3480.004f, 2543.605f, 49.68431f)), owning_building_guid = 18)
      LocalObject(622, Door.Constructor(Vector3(3487.587f, 2605.367f, 32.18431f)), owning_building_guid = 18)
      LocalObject(623, Door.Constructor(Vector3(3489.656f, 2622.211f, 32.18431f)), owning_building_guid = 18)
      LocalObject(624, Door.Constructor(Vector3(3489.854f, 2530.997f, 54.68431f)), owning_building_guid = 18)
      LocalObject(625, Door.Constructor(Vector3(3491.923f, 2547.842f, 32.18431f)), owning_building_guid = 18)
      LocalObject(626, Door.Constructor(Vector3(3493.991f, 2564.686f, 44.68431f)), owning_building_guid = 18)
      LocalObject(627, Door.Constructor(Vector3(3495.959f, 2627.136f, 32.18431f)), owning_building_guid = 18)
      LocalObject(628, Door.Constructor(Vector3(3497.537f, 2547.152f, 42.18431f)), owning_building_guid = 18)
      LocalObject(629, Door.Constructor(Vector3(3498.916f, 2558.381f, 54.68431f)), owning_building_guid = 18)
      LocalObject(630, Door.Constructor(Vector3(3504.531f, 2557.692f, 49.68431f)), owning_building_guid = 18)
      LocalObject(631, Door.Constructor(Vector3(3507.288f, 2580.151f, 32.18431f)), owning_building_guid = 18)
      LocalObject(632, Door.Constructor(Vector3(3510.835f, 2562.617f, 44.68431f)), owning_building_guid = 18)
      LocalObject(633, Door.Constructor(Vector3(3512.114f, 2619.454f, 32.18431f)), owning_building_guid = 18)
      LocalObject(634, Door.Constructor(Vector3(3514.971f, 2596.305f, 39.68431f)), owning_building_guid = 18)
      LocalObject(635, Door.Constructor(Vector3(3526.89f, 2600.541f, 39.68431f)), owning_building_guid = 18)
      LocalObject(636, Door.Constructor(Vector3(3529.747f, 2577.393f, 39.68431f)), owning_building_guid = 18)
      LocalObject(637, Door.Constructor(Vector3(3536.741f, 2587.933f, 32.18431f)), owning_building_guid = 18)
      LocalObject(832, Door.Constructor(Vector3(3516.458f, 2551.696f, 50.45631f)), owning_building_guid = 18)
      LocalObject(2512, Door.Constructor(Vector3(3511.466f, 2588.891f, 40.01731f)), owning_building_guid = 18)
      LocalObject(2513, Door.Constructor(Vector3(3515.954f, 2583.147f, 40.01731f)), owning_building_guid = 18)
      LocalObject(2514, Door.Constructor(Vector3(3520.444f, 2577.4f, 40.01731f)), owning_building_guid = 18)
      LocalObject(
        880,
        IFFLock.Constructor(Vector3(3516.653f, 2555.906f, 49.61531f), Vector3(0, 0, 52)),
        owning_building_guid = 18,
        door_guid = 832
      )
      LocalObject(
        935,
        IFFLock.Constructor(Vector3(3471.281f, 2550.826f, 31.99931f), Vector3(0, 0, 142)),
        owning_building_guid = 18,
        door_guid = 617
      )
      LocalObject(
        936,
        IFFLock.Constructor(Vector3(3477.788f, 2625.671f, 49.61332f), Vector3(0, 0, 52)),
        owning_building_guid = 18,
        door_guid = 336
      )
      LocalObject(
        937,
        IFFLock.Constructor(Vector3(3481.949f, 2531.3f, 54.62431f), Vector3(0, 0, 322)),
        owning_building_guid = 18,
        door_guid = 338
      )
      LocalObject(
        938,
        IFFLock.Constructor(Vector3(3489.983f, 2620.472f, 31.99931f), Vector3(0, 0, 232)),
        owning_building_guid = 18,
        door_guid = 623
      )
      LocalObject(
        939,
        IFFLock.Constructor(Vector3(3491.751f, 2525.398f, 54.62431f), Vector3(0, 0, 142)),
        owning_building_guid = 18,
        door_guid = 339
      )
      LocalObject(
        940,
        IFFLock.Constructor(Vector3(3494.609f, 2562.573f, 54.62431f), Vector3(0, 0, 232)),
        owning_building_guid = 18,
        door_guid = 340
      )
      LocalObject(
        941,
        IFFLock.Constructor(Vector3(3495.632f, 2628.876f, 31.99931f), Vector3(0, 0, 52)),
        owning_building_guid = 18,
        door_guid = 627
      )
      LocalObject(
        945,
        IFFLock.Constructor(Vector3(3501.758f, 2533.332f, 62.04432f), Vector3(0, 0, 232)),
        owning_building_guid = 18,
        door_guid = 347
      )
      LocalObject(
        949,
        IFFLock.Constructor(Vector3(3513.234f, 2595.976f, 39.49931f), Vector3(0, 0, 322)),
        owning_building_guid = 18,
        door_guid = 634
      )
      LocalObject(
        950,
        IFFLock.Constructor(Vector3(3531.485f, 2577.723f, 39.49931f), Vector3(0, 0, 142)),
        owning_building_guid = 18,
        door_guid = 636
      )
      LocalObject(
        951,
        IFFLock.Constructor(Vector3(3536.968f, 2586.116f, 31.99931f), Vector3(0, 0, 232)),
        owning_building_guid = 18,
        door_guid = 637
      )
      LocalObject(1209, Locker.Constructor(Vector3(3527.286f, 2603.598f, 30.66331f)), owning_building_guid = 18)
      LocalObject(1210, Locker.Constructor(Vector3(3528.329f, 2604.413f, 30.66331f)), owning_building_guid = 18)
      LocalObject(1211, Locker.Constructor(Vector3(3529.382f, 2605.236f, 30.66331f)), owning_building_guid = 18)
      LocalObject(1212, Locker.Constructor(Vector3(3530.436f, 2606.059f, 30.66331f)), owning_building_guid = 18)
      LocalObject(1213, Locker.Constructor(Vector3(3531.237f, 2581.274f, 38.42432f)), owning_building_guid = 18)
      LocalObject(1214, Locker.Constructor(Vector3(3532.154f, 2581.99f, 38.42432f)), owning_building_guid = 18)
      LocalObject(1215, Locker.Constructor(Vector3(3533.058f, 2582.697f, 38.42432f)), owning_building_guid = 18)
      LocalObject(1216, Locker.Constructor(Vector3(3533.963f, 2583.404f, 38.42432f)), owning_building_guid = 18)
      LocalObject(1217, Locker.Constructor(Vector3(3534.013f, 2608.854f, 30.66331f)), owning_building_guid = 18)
      LocalObject(1218, Locker.Constructor(Vector3(3535.057f, 2609.669f, 30.66331f)), owning_building_guid = 18)
      LocalObject(1219, Locker.Constructor(Vector3(3536.109f, 2610.492f, 30.66331f)), owning_building_guid = 18)
      LocalObject(1220, Locker.Constructor(Vector3(3537.163f, 2611.315f, 30.66331f)), owning_building_guid = 18)
      LocalObject(
        1687,
        Terminal.Constructor(Vector3(3504.648f, 2538.173f, 61.91831f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1688,
        Terminal.Constructor(Vector3(3505.024f, 2541.358f, 61.91831f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1689,
        Terminal.Constructor(Vector3(3506.636f, 2519.879f, 54.52332f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1690,
        Terminal.Constructor(Vector3(3507.729f, 2537.894f, 61.91831f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1694,
        Terminal.Constructor(Vector3(3525.8f, 2593.253f, 39.75331f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1695,
        Terminal.Constructor(Vector3(3528.133f, 2590.267f, 39.75331f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1696,
        Terminal.Constructor(Vector3(3530.43f, 2587.327f, 39.75331f), order_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2424,
        Terminal.Constructor(Vector3(3474.944f, 2575.111f, 42.22031f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2425,
        Terminal.Constructor(Vector3(3482.058f, 2540.884f, 32.22031f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2426,
        Terminal.Constructor(Vector3(3513.235f, 2587.11f, 40.29731f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2427,
        Terminal.Constructor(Vector3(3513.621f, 2524.146f, 54.78032f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2428,
        Terminal.Constructor(Vector3(3517.719f, 2581.365f, 40.29731f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2429,
        Terminal.Constructor(Vector3(3522.212f, 2575.621f, 40.29731f), spawn_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2648,
        Terminal.Constructor(Vector3(3507.615f, 2619.459f, 50.85032f), vehicle_terminal_combined),
        owning_building_guid = 18
      )
      LocalObject(
        1625,
        VehicleSpawnPad.Constructor(Vector3(3496.973f, 2610.949f, 46.69231f), mb_pad_creation, Vector3(0, 0, 232)),
        owning_building_guid = 18,
        terminal_guid = 2648
      )
      LocalObject(2295, ResourceSilo.Constructor(Vector3(3498.59f, 2455.219f, 55.18031f)), owning_building_guid = 18)
      LocalObject(
        2325,
        SpawnTube.Constructor(Vector3(3511.768f, 2587.79f, 38.16331f), Vector3(0, 0, 322)),
        owning_building_guid = 18
      )
      LocalObject(
        2326,
        SpawnTube.Constructor(Vector3(3516.255f, 2582.047f, 38.16331f), Vector3(0, 0, 322)),
        owning_building_guid = 18
      )
      LocalObject(
        2327,
        SpawnTube.Constructor(Vector3(3520.744f, 2576.302f, 38.16331f), Vector3(0, 0, 322)),
        owning_building_guid = 18
      )
      LocalObject(
        1643,
        ProximityTerminal.Constructor(Vector3(3518.521f, 2521.698f, 48.16331f), medical_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1644,
        ProximityTerminal.Constructor(Vector3(3532.547f, 2607.017f, 30.66331f), medical_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1882,
        ProximityTerminal.Constructor(Vector3(3451.174f, 2575.884f, 56.40431f), pad_landing_frame),
        owning_building_guid = 18
      )
      LocalObject(
        1883,
        Terminal.Constructor(Vector3(3451.174f, 2575.884f, 56.40431f), air_rearm_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2213,
        ProximityTerminal.Constructor(Vector3(3437.591f, 2524.656f, 47.71331f), repair_silo),
        owning_building_guid = 18
      )
      LocalObject(
        2214,
        Terminal.Constructor(Vector3(3437.591f, 2524.656f, 47.71331f), ground_rearm_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        2217,
        ProximityTerminal.Constructor(Vector3(3563.553f, 2562.778f, 47.71331f), repair_silo),
        owning_building_guid = 18
      )
      LocalObject(
        2218,
        Terminal.Constructor(Vector3(3563.553f, 2562.778f, 47.71331f), ground_rearm_terminal),
        owning_building_guid = 18
      )
      LocalObject(
        1537,
        FacilityTurret.Constructor(Vector3(3418.225f, 2529.16f, 56.67131f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1537, 5012)
      LocalObject(
        1538,
        FacilityTurret.Constructor(Vector3(3425.674f, 2589.672f, 56.67131f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1538, 5013)
      LocalObject(
        1541,
        FacilityTurret.Constructor(Vector3(3492.433f, 2436.014f, 56.67131f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1541, 5014)
      LocalObject(
        1542,
        FacilityTurret.Constructor(Vector3(3507.879f, 2652.229f, 56.67131f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1542, 5015)
      LocalObject(
        1543,
        FacilityTurret.Constructor(Vector3(3574.567f, 2498.691f, 56.67131f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1543, 5016)
      LocalObject(
        1544,
        FacilityTurret.Constructor(Vector3(3582.023f, 2559.228f, 56.67131f), manned_turret),
        owning_building_guid = 18
      )
      TurretToWeapon(1544, 5017)
      LocalObject(
        2058,
        Painbox.Constructor(Vector3(3462.13f, 2560.211f, 35.56532f), painbox),
        owning_building_guid = 18
      )
      LocalObject(
        2070,
        Painbox.Constructor(Vector3(3520.306f, 2593.075f, 42.60812f), painbox_continuous),
        owning_building_guid = 18
      )
      LocalObject(
        2082,
        Painbox.Constructor(Vector3(3471.452f, 2548.438f, 33.42201f), painbox_door_radius),
        owning_building_guid = 18
      )
      LocalObject(
        2098,
        Painbox.Constructor(Vector3(3514.735f, 2597.923f, 39.77102f), painbox_door_radius_continuous),
        owning_building_guid = 18
      )
      LocalObject(
        2099,
        Painbox.Constructor(Vector3(3528.458f, 2601.547f, 40.98932f), painbox_door_radius_continuous),
        owning_building_guid = 18
      )
      LocalObject(
        2100,
        Painbox.Constructor(Vector3(3530.864f, 2575.824f, 39.98932f), painbox_door_radius_continuous),
        owning_building_guid = 18
      )
      LocalObject(274, Generator.Constructor(Vector3(3459.868f, 2562.841f, 29.36931f)), owning_building_guid = 18)
      LocalObject(
        262,
        Terminal.Constructor(Vector3(3464.948f, 2556.415f, 30.66331f), gen_control),
        owning_building_guid = 18
      )
    }

    Building12()

    def Building12(): Unit = { // Name: Qumu Type: comm_station GUID: 21, MapID: 12
      LocalBuilding(
        "Qumu",
        21,
        12,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3904f, 5172f, 46.34075f),
            Vector3(0f, 0f, 44f),
            comm_station
          )
        )
      )
      LocalObject(
        194,
        CaptureTerminal.Constructor(Vector3(3882.411f, 5257.825f, 29.04075f), capture_terminal),
        owning_building_guid = 21
      )
      LocalObject(357, Door.Constructor(Vector3(3857.139f, 5221.246f, 48.09175f)), owning_building_guid = 21)
      LocalObject(358, Door.Constructor(Vector3(3870.225f, 5233.883f, 56.05475f)), owning_building_guid = 21)
      LocalObject(359, Door.Constructor(Vector3(3874.225f, 5248.899f, 48.06175f)), owning_building_guid = 21)
      LocalObject(360, Door.Constructor(Vector3(3878.56f, 5112.252f, 56.05575f)), owning_building_guid = 21)
      LocalObject(361, Door.Constructor(Vector3(3889.408f, 5158.319f, 53.06175f)), owning_building_guid = 21)
      LocalObject(362, Door.Constructor(Vector3(3891.198f, 5099.166f, 48.09175f)), owning_building_guid = 21)
      LocalObject(363, Door.Constructor(Vector3(3895.705f, 5151.799f, 53.06175f)), owning_building_guid = 21)
      LocalObject(364, Door.Constructor(Vector3(3895.862f, 5191.944f, 53.06175f)), owning_building_guid = 21)
      LocalObject(365, Door.Constructor(Vector3(3906f, 5163.63f, 60.50075f)), owning_building_guid = 21)
      LocalObject(366, Door.Constructor(Vector3(3920.389f, 5093.569f, 56.05575f)), owning_building_guid = 21)
      LocalObject(371, Door.Constructor(Vector3(3933.476f, 5106.208f, 48.09175f)), owning_building_guid = 21)
      LocalObject(372, Door.Constructor(Vector3(3942.095f, 5219.214f, 48.09175f)), owning_building_guid = 21)
      LocalObject(373, Door.Constructor(Vector3(3954.733f, 5206.127f, 56.05575f)), owning_building_guid = 21)
      LocalObject(638, Door.Constructor(Vector3(3850.515f, 5187.078f, 40.56175f)), owning_building_guid = 21)
      LocalObject(639, Door.Constructor(Vector3(3855.875f, 5170.011f, 40.56175f)), owning_building_guid = 21)
      LocalObject(640, Door.Constructor(Vector3(3867.779f, 5203.75f, 40.56175f)), owning_building_guid = 21)
      LocalObject(641, Door.Constructor(Vector3(3872.941f, 5175.371f, 30.56175f)), owning_building_guid = 21)
      LocalObject(642, Door.Constructor(Vector3(3873.336f, 5197.995f, 30.56175f)), owning_building_guid = 21)
      LocalObject(643, Door.Constructor(Vector3(3878.499f, 5169.616f, 40.56175f)), owning_building_guid = 21)
      LocalObject(644, Door.Constructor(Vector3(3878.992f, 5197.896f, 40.56175f)), owning_building_guid = 21)
      LocalObject(645, Door.Constructor(Vector3(3884.155f, 5169.518f, 48.06175f)), owning_building_guid = 21)
      LocalObject(646, Door.Constructor(Vector3(3885.241f, 5231.733f, 30.56175f)), owning_building_guid = 21)
      LocalObject(647, Door.Constructor(Vector3(3885.537f, 5248.702f, 30.56175f)), owning_building_guid = 21)
      LocalObject(648, Door.Constructor(Vector3(3891.292f, 5254.259f, 30.56175f)), owning_building_guid = 21)
      LocalObject(649, Door.Constructor(Vector3(3895.269f, 5158.008f, 53.06175f)), owning_building_guid = 21)
      LocalObject(650, Door.Constructor(Vector3(3895.565f, 5174.976f, 30.56175f)), owning_building_guid = 21)
      LocalObject(651, Door.Constructor(Vector3(3895.862f, 5191.944f, 43.06175f)), owning_building_guid = 21)
      LocalObject(652, Door.Constructor(Vector3(3901.221f, 5174.877f, 40.56175f)), owning_building_guid = 21)
      LocalObject(653, Door.Constructor(Vector3(3901.419f, 5186.189f, 53.06175f)), owning_building_guid = 21)
      LocalObject(654, Door.Constructor(Vector3(3907.075f, 5186.091f, 48.06175f)), owning_building_guid = 21)
      LocalObject(655, Door.Constructor(Vector3(3907.47f, 5208.714f, 30.56175f)), owning_building_guid = 21)
      LocalObject(656, Door.Constructor(Vector3(3908.161f, 5248.307f, 30.56175f)), owning_building_guid = 21)
      LocalObject(657, Door.Constructor(Vector3(3912.83f, 5191.648f, 43.06175f)), owning_building_guid = 21)
      LocalObject(658, Door.Constructor(Vector3(3913.422f, 5225.584f, 38.06175f)), owning_building_guid = 21)
      LocalObject(659, Door.Constructor(Vector3(3924.833f, 5231.042f, 38.06175f)), owning_building_guid = 21)
      LocalObject(660, Door.Constructor(Vector3(3930.094f, 5208.32f, 38.06175f)), owning_building_guid = 21)
      LocalObject(661, Door.Constructor(Vector3(3935.947f, 5219.533f, 30.56175f)), owning_building_guid = 21)
      LocalObject(833, Door.Constructor(Vector3(3919.563f, 5181.374f, 48.83375f)), owning_building_guid = 21)
      LocalObject(2517, Door.Constructor(Vector3(3910.711f, 5217.843f, 38.39475f)), owning_building_guid = 21)
      LocalObject(2518, Door.Constructor(Vector3(3915.774f, 5212.6f, 38.39475f)), owning_building_guid = 21)
      LocalObject(2519, Door.Constructor(Vector3(3920.841f, 5207.354f, 38.39475f)), owning_building_guid = 21)
      LocalObject(
        881,
        IFFLock.Constructor(Vector3(3919.317f, 5185.582f, 47.99275f), Vector3(0, 0, 46)),
        owning_building_guid = 21,
        door_guid = 833
      )
      LocalObject(
        956,
        IFFLock.Constructor(Vector3(3873.372f, 5250.902f, 47.99075f), Vector3(0, 0, 46)),
        owning_building_guid = 21,
        door_guid = 359
      )
      LocalObject(
        957,
        IFFLock.Constructor(Vector3(3874.725f, 5175.787f, 30.37675f), Vector3(0, 0, 136)),
        owning_building_guid = 21,
        door_guid = 641
      )
      LocalObject(
        958,
        IFFLock.Constructor(Vector3(3886.044f, 5247.006f, 30.37675f), Vector3(0, 0, 226)),
        owning_building_guid = 21,
        door_guid = 647
      )
      LocalObject(
        959,
        IFFLock.Constructor(Vector3(3887.376f, 5157.482f, 53.00175f), Vector3(0, 0, 316)),
        owning_building_guid = 21,
        door_guid = 361
      )
      LocalObject(
        960,
        IFFLock.Constructor(Vector3(3890.784f, 5255.954f, 30.37675f), Vector3(0, 0, 46)),
        owning_building_guid = 21,
        door_guid = 648
      )
      LocalObject(
        961,
        IFFLock.Constructor(Vector3(3896.697f, 5189.908f, 53.00175f), Vector3(0, 0, 226)),
        owning_building_guid = 21,
        door_guid = 364
      )
      LocalObject(
        962,
        IFFLock.Constructor(Vector3(3897.74f, 5152.638f, 53.00175f), Vector3(0, 0, 136)),
        owning_building_guid = 21,
        door_guid = 363
      )
      LocalObject(
        963,
        IFFLock.Constructor(Vector3(3906.863f, 5161.574f, 60.42175f), Vector3(0, 0, 226)),
        owning_building_guid = 21,
        door_guid = 365
      )
      LocalObject(
        964,
        IFFLock.Constructor(Vector3(3911.729f, 5225.075f, 37.87675f), Vector3(0, 0, 316)),
        owning_building_guid = 21,
        door_guid = 658
      )
      LocalObject(
        969,
        IFFLock.Constructor(Vector3(3931.787f, 5208.829f, 37.87675f), Vector3(0, 0, 136)),
        owning_building_guid = 21,
        door_guid = 660
      )
      LocalObject(
        970,
        IFFLock.Constructor(Vector3(3936.363f, 5217.749f, 30.37675f), Vector3(0, 0, 226)),
        owning_building_guid = 21,
        door_guid = 661
      )
      LocalObject(1229, Locker.Constructor(Vector3(3924.907f, 5234.124f, 29.04075f)), owning_building_guid = 21)
      LocalObject(1230, Locker.Constructor(Vector3(3925.859f, 5235.043f, 29.04075f)), owning_building_guid = 21)
      LocalObject(1231, Locker.Constructor(Vector3(3926.82f, 5235.972f, 29.04075f)), owning_building_guid = 21)
      LocalObject(1234, Locker.Constructor(Vector3(3927.782f, 5236.9f, 29.04075f)), owning_building_guid = 21)
      LocalObject(1237, Locker.Constructor(Vector3(3931.048f, 5240.054f, 29.04075f)), owning_building_guid = 21)
      LocalObject(1238, Locker.Constructor(Vector3(3931.169f, 5212.335f, 36.80175f)), owning_building_guid = 21)
      LocalObject(1241, Locker.Constructor(Vector3(3932.007f, 5213.144f, 36.80175f)), owning_building_guid = 21)
      LocalObject(1242, Locker.Constructor(Vector3(3932f, 5240.974f, 29.04075f)), owning_building_guid = 21)
      LocalObject(1243, Locker.Constructor(Vector3(3932.832f, 5213.94f, 36.80175f)), owning_building_guid = 21)
      LocalObject(1244, Locker.Constructor(Vector3(3932.961f, 5241.902f, 29.04075f)), owning_building_guid = 21)
      LocalObject(1247, Locker.Constructor(Vector3(3933.658f, 5214.738f, 36.80175f)), owning_building_guid = 21)
      LocalObject(1248, Locker.Constructor(Vector3(3933.923f, 5242.831f, 29.04075f)), owning_building_guid = 21)
      LocalObject(
        1700,
        Terminal.Constructor(Vector3(3909.231f, 5166.691f, 60.29575f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1701,
        Terminal.Constructor(Vector3(3909.272f, 5169.897f, 60.29575f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1702,
        Terminal.Constructor(Vector3(3912.325f, 5166.735f, 60.29575f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1703,
        Terminal.Constructor(Vector3(3913.121f, 5148.705f, 52.90075f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1704,
        Terminal.Constructor(Vector3(3924.51f, 5223.68f, 38.13075f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1705,
        Terminal.Constructor(Vector3(3927.142f, 5220.955f, 38.13075f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1706,
        Terminal.Constructor(Vector3(3929.734f, 5218.271f, 38.13075f), order_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2430,
        Terminal.Constructor(Vector3(3875.829f, 5200.321f, 40.59775f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2431,
        Terminal.Constructor(Vector3(3886.482f, 5167.026f, 30.59775f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2432,
        Terminal.Constructor(Vector3(3912.656f, 5216.257f, 38.67475f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2433,
        Terminal.Constructor(Vector3(3917.716f, 5211.012f, 38.67475f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2434,
        Terminal.Constructor(Vector3(3919.622f, 5153.679f, 53.15775f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2435,
        Terminal.Constructor(Vector3(3922.785f, 5205.77f, 38.67475f), spawn_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2649,
        Terminal.Constructor(Vector3(3903.686f, 5247.841f, 49.22775f), vehicle_terminal_combined),
        owning_building_guid = 21
      )
      LocalObject(
        1626,
        VehicleSpawnPad.Constructor(Vector3(3893.992f, 5238.266f, 45.06975f), mb_pad_creation, Vector3(0, 0, 226)),
        owning_building_guid = 21,
        terminal_guid = 2649
      )
      LocalObject(2296, ResourceSilo.Constructor(Vector3(3911.878f, 5083.558f, 53.55775f)), owning_building_guid = 21)
      LocalObject(
        2330,
        SpawnTube.Constructor(Vector3(3911.126f, 5216.78f, 36.54075f), Vector3(0, 0, 316)),
        owning_building_guid = 21
      )
      LocalObject(
        2331,
        SpawnTube.Constructor(Vector3(3916.189f, 5211.538f, 36.54075f), Vector3(0, 0, 316)),
        owning_building_guid = 21
      )
      LocalObject(
        2332,
        SpawnTube.Constructor(Vector3(3921.253f, 5206.293f, 36.54075f), Vector3(0, 0, 316)),
        owning_building_guid = 21
      )
      LocalObject(
        1645,
        ProximityTerminal.Constructor(Vector3(3924.751f, 5151.756f, 46.54075f), medical_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1646,
        ProximityTerminal.Constructor(Vector3(3929.781f, 5238.073f, 29.04075f), medical_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1885,
        ProximityTerminal.Constructor(Vector3(3852.109f, 5198.605f, 54.78175f), pad_landing_frame),
        owning_building_guid = 21
      )
      LocalObject(
        1886,
        Terminal.Constructor(Vector3(3852.109f, 5198.605f, 54.78175f), air_rearm_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2221,
        ProximityTerminal.Constructor(Vector3(3843.955f, 5146.239f, 46.09075f), repair_silo),
        owning_building_guid = 21
      )
      LocalObject(
        2222,
        Terminal.Constructor(Vector3(3843.955f, 5146.239f, 46.09075f), ground_rearm_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        2225,
        ProximityTerminal.Constructor(Vector3(3965.242f, 5197.318f, 46.09075f), repair_silo),
        owning_building_guid = 21
      )
      LocalObject(
        2226,
        Terminal.Constructor(Vector3(3965.242f, 5197.318f, 46.09075f), ground_rearm_terminal),
        owning_building_guid = 21
      )
      LocalObject(
        1547,
        FacilityTurret.Constructor(Vector3(3824.224f, 5148.694f, 55.04875f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1547, 5018)
      LocalObject(
        1548,
        FacilityTurret.Constructor(Vector3(3825.307f, 5209.653f, 55.04875f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1548, 5019)
      LocalObject(
        1550,
        FacilityTurret.Constructor(Vector3(3900.523f, 5280.459f, 55.04875f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1550, 5020)
      LocalObject(
        1551,
        FacilityTurret.Constructor(Vector3(3907.761f, 5063.814f, 55.04875f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1551, 5021)
      LocalObject(
        1553,
        FacilityTurret.Constructor(Vector3(3982.895f, 5134.734f, 55.04875f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1553, 5022)
      LocalObject(
        1554,
        FacilityTurret.Constructor(Vector3(3983.982f, 5195.718f, 55.04875f), manned_turret),
        owning_building_guid = 21
      )
      TurretToWeapon(1554, 5023)
      LocalObject(
        2059,
        Painbox.Constructor(Vector3(3864.643f, 5184.164f, 33.94275f), painbox),
        owning_building_guid = 21
      )
      LocalObject(
        2071,
        Painbox.Constructor(Vector3(3919.065f, 5222.929f, 40.98555f), painbox_continuous),
        owning_building_guid = 21
      )
      LocalObject(
        2083,
        Painbox.Constructor(Vector3(3875.145f, 5173.43f, 31.79945f), painbox_door_radius),
        owning_building_guid = 21
      )
      LocalObject(
        2101,
        Painbox.Constructor(Vector3(3913.018f, 5227.168f, 38.14845f), painbox_door_radius_continuous),
        owning_building_guid = 21
      )
      LocalObject(
        2102,
        Painbox.Constructor(Vector3(3926.287f, 5232.207f, 39.36675f), painbox_door_radius_continuous),
        owning_building_guid = 21
      )
      LocalObject(
        2103,
        Painbox.Constructor(Vector3(3931.369f, 5206.876f, 38.36675f), painbox_door_radius_continuous),
        owning_building_guid = 21
      )
      LocalObject(275, Generator.Constructor(Vector3(3862.118f, 5186.543f, 27.74675f)), owning_building_guid = 21)
      LocalObject(
        263,
        Terminal.Constructor(Vector3(3867.843f, 5180.683f, 29.04075f), gen_control),
        owning_building_guid = 21
      )
    }

    Building5()

    def Building5(): Unit = { // Name: Azeban Type: comm_station_dsp GUID: 24, MapID: 5
      LocalBuilding(
        "Azeban",
        24,
        5,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(6260f, 5234f, 61.88216f),
            Vector3(0f, 0f, 360f),
            comm_station_dsp
          )
        )
      )
      LocalObject(
        201,
        CaptureTerminal.Constructor(Vector3(6336.089f, 5214.734f, 44.48215f), capture_terminal),
        owning_building_guid = 24
      )
      LocalObject(254, Door.Constructor(Vector3(6328.339f, 5304.464f, 65.26015f)), owning_building_guid = 24)
      LocalObject(523, Door.Constructor(Vector3(6200.196f, 5190.501f, 63.53316f)), owning_building_guid = 24)
      LocalObject(524, Door.Constructor(Vector3(6200.196f, 5208.693f, 71.49715f)), owning_building_guid = 24)
      LocalObject(525, Door.Constructor(Vector3(6217.307f, 5166.197f, 71.49715f)), owning_building_guid = 24)
      LocalObject(526, Door.Constructor(Vector3(6235.499f, 5166.197f, 63.53316f)), owning_building_guid = 24)
      LocalObject(527, Door.Constructor(Vector3(6240f, 5225.231f, 68.50316f)), owning_building_guid = 24)
      LocalObject(528, Door.Constructor(Vector3(6240f, 5234.295f, 68.50316f)), owning_building_guid = 24)
      LocalObject(529, Door.Constructor(Vector3(6252.763f, 5353.958f, 63.53316f)), owning_building_guid = 24)
      LocalObject(530, Door.Constructor(Vector3(6255.625f, 5226.59f, 75.94215f)), owning_building_guid = 24)
      LocalObject(531, Door.Constructor(Vector3(6265.627f, 5366.823f, 71.49615f)), owning_building_guid = 24)
      LocalObject(532, Door.Constructor(Vector3(6268f, 5254f, 68.50316f)), owning_building_guid = 24)
      LocalObject(533, Door.Constructor(Vector3(6307.721f, 5398.353f, 63.53316f)), owning_building_guid = 24)
      LocalObject(534, Door.Constructor(Vector3(6313.952f, 5338.355f, 68.49915f)), owning_building_guid = 24)
      LocalObject(539, Door.Constructor(Vector3(6315.927f, 5308.35f, 63.50516f)), owning_building_guid = 24)
      LocalObject(540, Door.Constructor(Vector3(6325.914f, 5398.353f, 71.49615f)), owning_building_guid = 24)
      LocalObject(541, Door.Constructor(Vector3(6339.929f, 5221.406f, 71.49715f)), owning_building_guid = 24)
      LocalObject(542, Door.Constructor(Vector3(6352.793f, 5234.27f, 63.53316f)), owning_building_guid = 24)
      LocalObject(543, Door.Constructor(Vector3(6366.977f, 5297.008f, 71.49615f)), owning_building_guid = 24)
      LocalObject(544, Door.Constructor(Vector3(6366.977f, 5315.2f, 63.53316f)), owning_building_guid = 24)
      LocalObject(545, Door.Constructor(Vector3(6376f, 5314f, 63.50315f)), owning_building_guid = 24)
      LocalObject(786, Door.Constructor(Vector3(6244f, 5230f, 68.50316f)), owning_building_guid = 24)
      LocalObject(787, Door.Constructor(Vector3(6244f, 5246f, 63.50315f)), owning_building_guid = 24)
      LocalObject(788, Door.Constructor(Vector3(6268f, 5246f, 68.50316f)), owning_building_guid = 24)
      LocalObject(789, Door.Constructor(Vector3(6268f, 5254f, 58.50315f)), owning_building_guid = 24)
      LocalObject(790, Door.Constructor(Vector3(6272f, 5242f, 63.50315f)), owning_building_guid = 24)
      LocalObject(791, Door.Constructor(Vector3(6280f, 5242f, 58.50315f)), owning_building_guid = 24)
      LocalObject(792, Door.Constructor(Vector3(6284f, 5278f, 53.50316f)), owning_building_guid = 24)
      LocalObject(793, Door.Constructor(Vector3(6288f, 5266f, 46.00316f)), owning_building_guid = 24)
      LocalObject(794, Door.Constructor(Vector3(6300f, 5230f, 46.00316f)), owning_building_guid = 24)
      LocalObject(795, Door.Constructor(Vector3(6300f, 5294f, 46.00316f)), owning_building_guid = 24)
      LocalObject(796, Door.Constructor(Vector3(6304f, 5226f, 53.50316f)), owning_building_guid = 24)
      LocalObject(797, Door.Constructor(Vector3(6304f, 5242f, 53.50316f)), owning_building_guid = 24)
      LocalObject(798, Door.Constructor(Vector3(6304f, 5266f, 53.50316f)), owning_building_guid = 24)
      LocalObject(799, Door.Constructor(Vector3(6316f, 5246f, 46.00316f)), owning_building_guid = 24)
      LocalObject(800, Door.Constructor(Vector3(6316f, 5262f, 53.50316f)), owning_building_guid = 24)
      LocalObject(801, Door.Constructor(Vector3(6319.921f, 5328.351f, 68.50516f)), owning_building_guid = 24)
      LocalObject(802, Door.Constructor(Vector3(6332f, 5206f, 46.00316f)), owning_building_guid = 24)
      LocalObject(803, Door.Constructor(Vector3(6340f, 5206f, 46.00316f)), owning_building_guid = 24)
      LocalObject(804, Door.Constructor(Vector3(6344f, 5218f, 46.00316f)), owning_building_guid = 24)
      LocalObject(805, Door.Constructor(Vector3(6348f, 5238f, 53.50316f)), owning_building_guid = 24)
      LocalObject(806, Door.Constructor(Vector3(6348f, 5270f, 53.50316f)), owning_building_guid = 24)
      LocalObject(840, Door.Constructor(Vector3(6277.707f, 5229.922f, 64.27415f)), owning_building_guid = 24)
      LocalObject(2564, Door.Constructor(Vector3(6296.673f, 5247.733f, 53.83616f)), owning_building_guid = 24)
      LocalObject(2565, Door.Constructor(Vector3(6296.673f, 5255.026f, 53.83616f)), owning_building_guid = 24)
      LocalObject(2566, Door.Constructor(Vector3(6296.673f, 5262.315f, 53.83616f)), owning_building_guid = 24)
      LocalObject(
        888,
        IFFLock.Constructor(Vector3(6280.454f, 5233.09f, 63.45016f), Vector3(0, 0, 90)),
        owning_building_guid = 24,
        door_guid = 840
      )
      LocalObject(
        1081,
        IFFLock.Constructor(Vector3(6237.959f, 5235.104f, 68.45016f), Vector3(0, 0, 0)),
        owning_building_guid = 24,
        door_guid = 528
      )
      LocalObject(
        1082,
        IFFLock.Constructor(Vector3(6242.04f, 5224.42f, 68.45016f), Vector3(0, 0, 180)),
        owning_building_guid = 24,
        door_guid = 527
      )
      LocalObject(
        1083,
        IFFLock.Constructor(Vector3(6254.817f, 5224.514f, 75.95016f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 530
      )
      LocalObject(
        1084,
        IFFLock.Constructor(Vector3(6267.193f, 5251.962f, 68.45016f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 532
      )
      LocalObject(
        1085,
        IFFLock.Constructor(Vector3(6300.94f, 5295.572f, 45.81816f), Vector3(0, 0, 90)),
        owning_building_guid = 24,
        door_guid = 795
      )
      LocalObject(
        1086,
        IFFLock.Constructor(Vector3(6302.428f, 5266.94f, 53.31816f), Vector3(0, 0, 0)),
        owning_building_guid = 24,
        door_guid = 798
      )
      LocalObject(
        1087,
        IFFLock.Constructor(Vector3(6305.572f, 5241.19f, 53.31816f), Vector3(0, 0, 180)),
        owning_building_guid = 24,
        door_guid = 797
      )
      LocalObject(
        1088,
        IFFLock.Constructor(Vector3(6311.907f, 5339.163f, 68.42915f), Vector3(0, 0, 0)),
        owning_building_guid = 24,
        door_guid = 534
      )
      LocalObject(
        1091,
        IFFLock.Constructor(Vector3(6315.06f, 5244.428f, 45.81816f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 799
      )
      LocalObject(
        1092,
        IFFLock.Constructor(Vector3(6315.124f, 5306.312f, 63.49416f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 539
      )
      LocalObject(
        1095,
        IFFLock.Constructor(Vector3(6331.06f, 5204.428f, 45.81816f), Vector3(0, 0, 270)),
        owning_building_guid = 24,
        door_guid = 802
      )
      LocalObject(
        1097,
        IFFLock.Constructor(Vector3(6340.813f, 5207.572f, 45.81816f), Vector3(0, 0, 90)),
        owning_building_guid = 24,
        door_guid = 803
      )
      LocalObject(
        1098,
        IFFLock.Constructor(Vector3(6373.953f, 5314.808f, 63.39316f), Vector3(0, 0, 0)),
        owning_building_guid = 24,
        door_guid = 545
      )
      LocalObject(1426, Locker.Constructor(Vector3(6307.563f, 5244.141f, 52.24316f)), owning_building_guid = 24)
      LocalObject(1427, Locker.Constructor(Vector3(6308.727f, 5244.141f, 52.24316f)), owning_building_guid = 24)
      LocalObject(1428, Locker.Constructor(Vector3(6309.874f, 5244.141f, 52.24316f)), owning_building_guid = 24)
      LocalObject(1429, Locker.Constructor(Vector3(6311.023f, 5244.141f, 52.24316f)), owning_building_guid = 24)
      LocalObject(1432, Locker.Constructor(Vector3(6318.194f, 5264.165f, 44.48215f)), owning_building_guid = 24)
      LocalObject(1435, Locker.Constructor(Vector3(6319.518f, 5264.165f, 44.48215f)), owning_building_guid = 24)
      LocalObject(1436, Locker.Constructor(Vector3(6320.854f, 5264.165f, 44.48215f)), owning_building_guid = 24)
      LocalObject(1439, Locker.Constructor(Vector3(6322.191f, 5264.165f, 44.48215f)), owning_building_guid = 24)
      LocalObject(1442, Locker.Constructor(Vector3(6326.731f, 5264.165f, 44.48215f)), owning_building_guid = 24)
      LocalObject(1443, Locker.Constructor(Vector3(6328.055f, 5264.165f, 44.48215f)), owning_building_guid = 24)
      LocalObject(1444, Locker.Constructor(Vector3(6329.391f, 5264.165f, 44.48215f)), owning_building_guid = 24)
      LocalObject(1445, Locker.Constructor(Vector3(6330.728f, 5264.165f, 44.48215f)), owning_building_guid = 24)
      LocalObject(
        256,
        Terminal.Constructor(Vector3(6319.879f, 5336.918f, 67.58616f), dropship_vehicle_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        255,
        VehicleSpawnPad.Constructor(Vector3(6328.328f, 5358.856f, 61.91016f), dropship_pad_doors, Vector3(0, 0, 90)),
        owning_building_guid = 24,
        terminal_guid = 256
      )
      LocalObject(
        1774,
        Terminal.Constructor(Vector3(6250.378f, 5210.897f, 68.34216f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1775,
        Terminal.Constructor(Vector3(6260.075f, 5226.547f, 75.73715f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1776,
        Terminal.Constructor(Vector3(6262.331f, 5224.43f, 75.73715f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1777,
        Terminal.Constructor(Vector3(6262.332f, 5228.825f, 75.73715f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1778,
        Terminal.Constructor(Vector3(6264.592f, 5226.59f, 75.73715f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1779,
        Terminal.Constructor(Vector3(6310.654f, 5249.408f, 53.57215f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1780,
        Terminal.Constructor(Vector3(6310.654f, 5253.139f, 53.57215f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1781,
        Terminal.Constructor(Vector3(6310.654f, 5256.928f, 53.57215f), order_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2476,
        Terminal.Constructor(Vector3(6258.509f, 5209.959f, 68.59916f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2477,
        Terminal.Constructor(Vector3(6296.971f, 5245.243f, 54.11616f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2478,
        Terminal.Constructor(Vector3(6296.967f, 5252.535f, 54.11616f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2479,
        Terminal.Constructor(Vector3(6296.97f, 5259.823f, 54.11616f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2480,
        Terminal.Constructor(Vector3(6315.103f, 5328.906f, 68.53016f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2481,
        Terminal.Constructor(Vector3(6324.058f, 5233.409f, 46.01015f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2482,
        Terminal.Constructor(Vector3(6331.409f, 5289.942f, 46.01015f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2483,
        Terminal.Constructor(Vector3(6340.058f, 5241.409f, 53.53915f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2484,
        Terminal.Constructor(Vector3(6340.058f, 5281.409f, 53.53915f), spawn_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2656,
        Terminal.Constructor(Vector3(6277.698f, 5342.044f, 64.66916f), ground_vehicle_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1639,
        VehicleSpawnPad.Constructor(Vector3(6277.786f, 5328.411f, 60.51116f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 24,
        terminal_guid = 2656
      )
      LocalObject(2303, ResourceSilo.Constructor(Vector3(6358.212f, 5399.642f, 68.99915f)), owning_building_guid = 24)
      LocalObject(
        2377,
        SpawnTube.Constructor(Vector3(6296.233f, 5246.683f, 51.98215f), Vector3(0, 0, 0)),
        owning_building_guid = 24
      )
      LocalObject(
        2378,
        SpawnTube.Constructor(Vector3(6296.233f, 5253.974f, 51.98215f), Vector3(0, 0, 0)),
        owning_building_guid = 24
      )
      LocalObject(
        2379,
        SpawnTube.Constructor(Vector3(6296.233f, 5261.262f, 51.98215f), Vector3(0, 0, 0)),
        owning_building_guid = 24
      )
      LocalObject(
        1658,
        ProximityTerminal.Constructor(Vector3(6260.863f, 5205.013f, 61.98215f), medical_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1659,
        ProximityTerminal.Constructor(Vector3(6324.444f, 5263.62f, 44.48215f), medical_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1978,
        ProximityTerminal.Constructor(Vector3(6241.153f, 5327.398f, 70.29216f), pad_landing_frame),
        owning_building_guid = 24
      )
      LocalObject(
        1979,
        Terminal.Constructor(Vector3(6241.153f, 5327.398f, 70.29216f), air_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1981,
        ProximityTerminal.Constructor(Vector3(6257.514f, 5281.467f, 67.57616f), pad_landing_frame),
        owning_building_guid = 24
      )
      LocalObject(
        1982,
        Terminal.Constructor(Vector3(6257.514f, 5281.467f, 67.57616f), air_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1984,
        ProximityTerminal.Constructor(Vector3(6309.804f, 5245.901f, 74.75816f), pad_landing_frame),
        owning_building_guid = 24
      )
      LocalObject(
        1985,
        Terminal.Constructor(Vector3(6309.804f, 5245.901f, 74.75816f), air_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1987,
        ProximityTerminal.Constructor(Vector3(6345.071f, 5262.159f, 70.30516f), pad_landing_frame),
        owning_building_guid = 24
      )
      LocalObject(
        1988,
        Terminal.Constructor(Vector3(6345.071f, 5262.159f, 70.30516f), air_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2277,
        ProximityTerminal.Constructor(Vector3(6198.643f, 5272.241f, 61.63216f), repair_silo),
        owning_building_guid = 24
      )
      LocalObject(
        2278,
        Terminal.Constructor(Vector3(6198.643f, 5272.241f, 61.63216f), ground_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        2281,
        ProximityTerminal.Constructor(Vector3(6368.57f, 5275.151f, 61.63216f), repair_silo),
        owning_building_guid = 24
      )
      LocalObject(
        2282,
        Terminal.Constructor(Vector3(6368.57f, 5275.151f, 61.63216f), ground_rearm_terminal),
        owning_building_guid = 24
      )
      LocalObject(
        1605,
        FacilityTurret.Constructor(Vector3(6186.401f, 5307.113f, 70.49016f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1605, 5024)
      LocalObject(
        1606,
        FacilityTurret.Constructor(Vector3(6187.554f, 5153.565f, 70.49016f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1606, 5025)
      LocalObject(
        1608,
        FacilityTurret.Constructor(Vector3(6231.445f, 5353.667f, 70.49016f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1608, 5026)
      LocalObject(
        1610,
        FacilityTurret.Constructor(Vector3(6290.428f, 5152.396f, 70.49016f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1610, 5027)
      LocalObject(
        1611,
        FacilityTurret.Constructor(Vector3(6291.449f, 5412.154f, 70.49016f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1611, 5028)
      LocalObject(
        1613,
        FacilityTurret.Constructor(Vector3(6332.537f, 5193.011f, 70.49016f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1613, 5029)
      LocalObject(
        1614,
        FacilityTurret.Constructor(Vector3(6379.619f, 5410.985f, 70.49016f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1614, 5030)
      LocalObject(
        1615,
        FacilityTurret.Constructor(Vector3(6380.773f, 5242.733f, 70.49016f), manned_turret),
        owning_building_guid = 24
      )
      TurretToWeapon(1615, 5031)
      LocalObject(
        2066,
        Painbox.Constructor(Vector3(6288.428f, 5294.057f, 48.37646f), painbox),
        owning_building_guid = 24
      )
      LocalObject(
        2078,
        Painbox.Constructor(Vector3(6305.857f, 5254.408f, 56.00965f), painbox_continuous),
        owning_building_guid = 24
      )
      LocalObject(
        2090,
        Painbox.Constructor(Vector3(6302.203f, 5292.915f, 47.61436f), painbox_door_radius),
        owning_building_guid = 24
      )
      LocalObject(
        2122,
        Painbox.Constructor(Vector3(6303.087f, 5239.386f, 54.41136f), painbox_door_radius_continuous),
        owning_building_guid = 24
      )
      LocalObject(
        2123,
        Painbox.Constructor(Vector3(6303.895f, 5268.081f, 54.88216f), painbox_door_radius_continuous),
        owning_building_guid = 24
      )
      LocalObject(
        2124,
        Painbox.Constructor(Vector3(6318.317f, 5261.888f, 55.31246f), painbox_door_radius_continuous),
        owning_building_guid = 24
      )
      LocalObject(282, Generator.Constructor(Vector3(6284.445f, 5293.975f, 43.18816f)), owning_building_guid = 24)
      LocalObject(
        270,
        Terminal.Constructor(Vector3(6292.637f, 5294.022f, 44.48215f), gen_control),
        owning_building_guid = 24
      )
    }

    Building8()

    def Building8(): Unit = { // Name: Ikanam Type: cryo_facility GUID: 27, MapID: 8
      LocalBuilding(
        "Ikanam",
        27,
        8,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(2694f, 2352f, 57.10244f),
            Vector3(0f, 0f, 360f),
            cryo_facility
          )
        )
      )
      LocalObject(
        191,
        CaptureTerminal.Constructor(Vector3(2665.911f, 2411.266f, 47.10244f), capture_terminal),
        owning_building_guid = 27
      )
      LocalObject(294, Door.Constructor(Vector3(2635.023f, 2356.5f, 58.65344f)), owning_building_guid = 27)
      LocalObject(295, Door.Constructor(Vector3(2635.023f, 2374.693f, 66.61744f)), owning_building_guid = 27)
      LocalObject(296, Door.Constructor(Vector3(2651.674f, 2419.803f, 58.65344f)), owning_building_guid = 27)
      LocalObject(297, Door.Constructor(Vector3(2669.867f, 2419.803f, 66.61744f)), owning_building_guid = 27)
      LocalObject(298, Door.Constructor(Vector3(2678f, 2372f, 68.62344f)), owning_building_guid = 27)
      LocalObject(299, Door.Constructor(Vector3(2698f, 2355.137f, 68.62344f)), owning_building_guid = 27)
      LocalObject(300, Door.Constructor(Vector3(2699.307f, 2269.073f, 66.61744f)), owning_building_guid = 27)
      LocalObject(301, Door.Constructor(Vector3(2717.5f, 2269.073f, 58.65344f)), owning_building_guid = 27)
      LocalObject(302, Door.Constructor(Vector3(2726f, 2260f, 58.62344f)), owning_building_guid = 27)
      LocalObject(303, Door.Constructor(Vector3(2744.927f, 2301.307f, 66.61744f)), owning_building_guid = 27)
      LocalObject(304, Door.Constructor(Vector3(2744.927f, 2319.5f, 58.65344f)), owning_building_guid = 27)
      LocalObject(568, Door.Constructor(Vector3(2646f, 2340f, 48.62344f)), owning_building_guid = 27)
      LocalObject(569, Door.Constructor(Vector3(2654f, 2292f, 51.12344f)), owning_building_guid = 27)
      LocalObject(570, Door.Constructor(Vector3(2658f, 2352f, 48.62344f)), owning_building_guid = 27)
      LocalObject(571, Door.Constructor(Vector3(2658f, 2392f, 48.62344f)), owning_building_guid = 27)
      LocalObject(572, Door.Constructor(Vector3(2662f, 2372f, 48.62344f)), owning_building_guid = 27)
      LocalObject(573, Door.Constructor(Vector3(2662f, 2420f, 48.62344f)), owning_building_guid = 27)
      LocalObject(574, Door.Constructor(Vector3(2670f, 2420f, 48.62344f)), owning_building_guid = 27)
      LocalObject(575, Door.Constructor(Vector3(2674f, 2296f, 51.12344f)), owning_building_guid = 27)
      LocalObject(576, Door.Constructor(Vector3(2674f, 2320f, 48.62344f)), owning_building_guid = 27)
      LocalObject(577, Door.Constructor(Vector3(2678f, 2372f, 48.62344f)), owning_building_guid = 27)
      LocalObject(578, Door.Constructor(Vector3(2678f, 2372f, 58.62344f)), owning_building_guid = 27)
      LocalObject(579, Door.Constructor(Vector3(2678f, 2420f, 48.62344f)), owning_building_guid = 27)
      LocalObject(580, Door.Constructor(Vector3(2682f, 2264f, 51.12344f)), owning_building_guid = 27)
      LocalObject(581, Door.Constructor(Vector3(2682f, 2352f, 48.62344f)), owning_building_guid = 27)
      LocalObject(582, Door.Constructor(Vector3(2682f, 2392f, 48.62344f)), owning_building_guid = 27)
      LocalObject(583, Door.Constructor(Vector3(2694f, 2324f, 48.62344f)), owning_building_guid = 27)
      LocalObject(584, Door.Constructor(Vector3(2694f, 2348f, 48.62344f)), owning_building_guid = 27)
      LocalObject(585, Door.Constructor(Vector3(2694f, 2404f, 48.62344f)), owning_building_guid = 27)
      LocalObject(586, Door.Constructor(Vector3(2698f, 2344f, 68.62344f)), owning_building_guid = 27)
      LocalObject(587, Door.Constructor(Vector3(2706f, 2352f, 48.62344f)), owning_building_guid = 27)
      LocalObject(588, Door.Constructor(Vector3(2706f, 2376f, 48.62344f)), owning_building_guid = 27)
      LocalObject(589, Door.Constructor(Vector3(2718f, 2356f, 41.12344f)), owning_building_guid = 27)
      LocalObject(590, Door.Constructor(Vector3(2718f, 2372f, 48.62344f)), owning_building_guid = 27)
      LocalObject(830, Door.Constructor(Vector3(2697.992f, 2377.083f, 59.38544f)), owning_building_guid = 27)
      LocalObject(842, Door.Constructor(Vector3(2690f, 2360f, 58.62144f)), owning_building_guid = 27)
      LocalObject(843, Door.Constructor(Vector3(2706f, 2352f, 58.62344f)), owning_building_guid = 27)
      LocalObject(2496, Door.Constructor(Vector3(2698.673f, 2357.733f, 48.95644f)), owning_building_guid = 27)
      LocalObject(2497, Door.Constructor(Vector3(2698.673f, 2365.026f, 48.95644f)), owning_building_guid = 27)
      LocalObject(2498, Door.Constructor(Vector3(2698.673f, 2372.315f, 48.95644f)), owning_building_guid = 27)
      LocalObject(
        878,
        IFFLock.Constructor(Vector3(2694.77f, 2379.822f, 58.58444f), Vector3(0, 0, 0)),
        owning_building_guid = 27,
        door_guid = 830
      )
      LocalObject(
        903,
        IFFLock.Constructor(Vector3(2661.06f, 2418.428f, 48.43844f), Vector3(0, 0, 270)),
        owning_building_guid = 27,
        door_guid = 573
      )
      LocalObject(
        904,
        IFFLock.Constructor(Vector3(2670.813f, 2421.572f, 48.43844f), Vector3(0, 0, 90)),
        owning_building_guid = 27,
        door_guid = 574
      )
      LocalObject(
        905,
        IFFLock.Constructor(Vector3(2678.814f, 2374.043f, 68.55444f), Vector3(0, 0, 90)),
        owning_building_guid = 27,
        door_guid = 298
      )
      LocalObject(
        906,
        IFFLock.Constructor(Vector3(2695.954f, 2355.958f, 68.55444f), Vector3(0, 0, 0)),
        owning_building_guid = 27,
        door_guid = 299
      )
      LocalObject(
        907,
        IFFLock.Constructor(Vector3(2704.428f, 2376.81f, 48.43844f), Vector3(0, 0, 0)),
        owning_building_guid = 27,
        door_guid = 588
      )
      LocalObject(
        908,
        IFFLock.Constructor(Vector3(2707.572f, 2351.19f, 48.43844f), Vector3(0, 0, 180)),
        owning_building_guid = 27,
        door_guid = 587
      )
      LocalObject(
        909,
        IFFLock.Constructor(Vector3(2717.19f, 2354.428f, 40.93844f), Vector3(0, 0, 270)),
        owning_building_guid = 27,
        door_guid = 589
      )
      LocalObject(
        910,
        IFFLock.Constructor(Vector3(2726.814f, 2262.042f, 58.55444f), Vector3(0, 0, 90)),
        owning_building_guid = 27,
        door_guid = 302
      )
      LocalObject(1127, Locker.Constructor(Vector3(2709.563f, 2354.141f, 47.36344f)), owning_building_guid = 27)
      LocalObject(1128, Locker.Constructor(Vector3(2710.727f, 2354.141f, 47.36344f)), owning_building_guid = 27)
      LocalObject(1129, Locker.Constructor(Vector3(2711.874f, 2354.141f, 47.36344f)), owning_building_guid = 27)
      LocalObject(1130, Locker.Constructor(Vector3(2713.023f, 2354.141f, 47.36344f)), owning_building_guid = 27)
      LocalObject(1131, Locker.Constructor(Vector3(2713.997f, 2401.496f, 47.09744f)), owning_building_guid = 27)
      LocalObject(1132, Locker.Constructor(Vector3(2713.997f, 2402.752f, 47.09744f)), owning_building_guid = 27)
      LocalObject(1133, Locker.Constructor(Vector3(2713.997f, 2404.013f, 47.09744f)), owning_building_guid = 27)
      LocalObject(1134, Locker.Constructor(Vector3(2713.997f, 2405.275f, 47.09744f)), owning_building_guid = 27)
      LocalObject(1135, Locker.Constructor(Vector3(2713.997f, 2406.527f, 47.09744f)), owning_building_guid = 27)
      LocalObject(1136, Locker.Constructor(Vector3(2726.817f, 2354.36f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1137, Locker.Constructor(Vector3(2726.814f, 2374.361f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1138, Locker.Constructor(Vector3(2727.873f, 2354.36f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1139, Locker.Constructor(Vector3(2727.868f, 2374.361f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1140, Locker.Constructor(Vector3(2728.928f, 2354.36f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1141, Locker.Constructor(Vector3(2728.929f, 2374.361f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1142, Locker.Constructor(Vector3(2729.983f, 2354.36f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1143, Locker.Constructor(Vector3(2729.984f, 2374.361f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1144, Locker.Constructor(Vector3(2731.043f, 2354.36f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1145, Locker.Constructor(Vector3(2731.039f, 2374.361f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1146, Locker.Constructor(Vector3(2732.098f, 2354.36f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1147, Locker.Constructor(Vector3(2732.095f, 2374.361f, 47.01044f)), owning_building_guid = 27)
      LocalObject(1475, Locker.Constructor(Vector3(2712.26f, 2339.787f, 57.10244f)), owning_building_guid = 27)
      LocalObject(1476, Locker.Constructor(Vector3(2712.26f, 2340.821f, 57.10244f)), owning_building_guid = 27)
      LocalObject(1477, Locker.Constructor(Vector3(2712.26f, 2343.338f, 56.87344f)), owning_building_guid = 27)
      LocalObject(1478, Locker.Constructor(Vector3(2712.26f, 2344.372f, 56.87344f)), owning_building_guid = 27)
      LocalObject(1479, Locker.Constructor(Vector3(2712.26f, 2345.426f, 56.87344f)), owning_building_guid = 27)
      LocalObject(1480, Locker.Constructor(Vector3(2712.26f, 2346.46f, 56.87344f)), owning_building_guid = 27)
      LocalObject(1481, Locker.Constructor(Vector3(2712.26f, 2348.982f, 57.10244f)), owning_building_guid = 27)
      LocalObject(1482, Locker.Constructor(Vector3(2712.26f, 2350.016f, 57.10244f)), owning_building_guid = 27)
      LocalObject(
        203,
        Terminal.Constructor(Vector3(2696.276f, 2400.25f, 47.09244f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        204,
        Terminal.Constructor(Vector3(2696.276f, 2407.575f, 47.09244f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        205,
        Terminal.Constructor(Vector3(2697.724f, 2398.802f, 47.09244f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        206,
        Terminal.Constructor(Vector3(2697.724f, 2409.023f, 47.09244f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        207,
        Terminal.Constructor(Vector3(2710.424f, 2398.802f, 47.09244f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        208,
        Terminal.Constructor(Vector3(2710.424f, 2409.023f, 47.09244f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        209,
        Terminal.Constructor(Vector3(2711.872f, 2400.25f, 47.09244f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        210,
        Terminal.Constructor(Vector3(2711.872f, 2407.575f, 47.09244f), cert_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1667,
        Terminal.Constructor(Vector3(2683.972f, 2361.526f, 58.39744f), order_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1668,
        Terminal.Constructor(Vector3(2712.654f, 2359.408f, 48.69244f), order_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1669,
        Terminal.Constructor(Vector3(2712.654f, 2363.139f, 48.69244f), order_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1670,
        Terminal.Constructor(Vector3(2712.654f, 2366.928f, 48.69244f), order_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2410,
        Terminal.Constructor(Vector3(2654f, 2351.407f, 48.71544f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2411,
        Terminal.Constructor(Vector3(2677.91f, 2295.41f, 51.21544f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2412,
        Terminal.Constructor(Vector3(2693.407f, 2416f, 48.71544f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2413,
        Terminal.Constructor(Vector3(2695.905f, 2338.359f, 58.68144f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2414,
        Terminal.Constructor(Vector3(2698.971f, 2355.243f, 49.23644f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2415,
        Terminal.Constructor(Vector3(2698.967f, 2362.535f, 49.23644f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2416,
        Terminal.Constructor(Vector3(2698.97f, 2369.823f, 49.23644f), spawn_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2646,
        Terminal.Constructor(Vector3(2731.628f, 2288.814f, 59.40744f), vehicle_terminal_combined),
        owning_building_guid = 27
      )
      LocalObject(
        1623,
        VehicleSpawnPad.Constructor(Vector3(2717.989f, 2288.724f, 55.24944f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 27,
        terminal_guid = 2646
      )
      LocalObject(2293, ResourceSilo.Constructor(Vector3(2633.733f, 2402.852f, 64.11944f)), owning_building_guid = 27)
      LocalObject(
        2309,
        SpawnTube.Constructor(Vector3(2698.233f, 2356.683f, 47.10244f), Vector3(0, 0, 0)),
        owning_building_guid = 27
      )
      LocalObject(
        2310,
        SpawnTube.Constructor(Vector3(2698.233f, 2363.974f, 47.10244f), Vector3(0, 0, 0)),
        owning_building_guid = 27
      )
      LocalObject(
        2311,
        SpawnTube.Constructor(Vector3(2698.233f, 2371.262f, 47.10244f), Vector3(0, 0, 0)),
        owning_building_guid = 27
      )
      LocalObject(
        141,
        ProximityTerminal.Constructor(Vector3(2695.983f, 2344.892f, 56.91244f), adv_med_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1641,
        ProximityTerminal.Constructor(Vector3(2721.642f, 2355.952f, 47.10244f), medical_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1858,
        ProximityTerminal.Constructor(Vector3(2675.883f, 2290.061f, 67.39544f), pad_landing_frame),
        owning_building_guid = 27
      )
      LocalObject(
        1859,
        Terminal.Constructor(Vector3(2675.883f, 2290.061f, 67.39544f), air_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1861,
        ProximityTerminal.Constructor(Vector3(2679.101f, 2405.651f, 65.44444f), pad_landing_frame),
        owning_building_guid = 27
      )
      LocalObject(
        1862,
        Terminal.Constructor(Vector3(2679.101f, 2405.651f, 65.44444f), air_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1864,
        ProximityTerminal.Constructor(Vector3(2692.198f, 2282.777f, 65.45444f), pad_landing_frame),
        owning_building_guid = 27
      )
      LocalObject(
        1865,
        Terminal.Constructor(Vector3(2692.198f, 2282.777f, 65.45444f), air_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1867,
        ProximityTerminal.Constructor(Vector3(2695.323f, 2397.253f, 67.43444f), pad_landing_frame),
        owning_building_guid = 27
      )
      LocalObject(
        1868,
        Terminal.Constructor(Vector3(2695.323f, 2397.253f, 67.43444f), air_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2197,
        ProximityTerminal.Constructor(Vector3(2658.525f, 2267.154f, 56.85244f), repair_silo),
        owning_building_guid = 27
      )
      LocalObject(
        2198,
        Terminal.Constructor(Vector3(2658.525f, 2267.154f, 56.85244f), ground_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        2201,
        ProximityTerminal.Constructor(Vector3(2746.53f, 2367.861f, 56.85244f), repair_silo),
        owning_building_guid = 27
      )
      LocalObject(
        2202,
        Terminal.Constructor(Vector3(2746.53f, 2367.861f, 56.85244f), ground_rearm_terminal),
        owning_building_guid = 27
      )
      LocalObject(
        1521,
        FacilityTurret.Constructor(Vector3(2622.392f, 2432.472f, 65.50444f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1521, 5032)
      LocalObject(
        1522,
        FacilityTurret.Constructor(Vector3(2622.4f, 2255.379f, 65.50444f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1522, 5033)
      LocalObject(
        1523,
        FacilityTurret.Constructor(Vector3(2715.665f, 2433.605f, 65.50444f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1523, 5034)
      LocalObject(
        1524,
        FacilityTurret.Constructor(Vector3(2757.626f, 2255.371f, 65.50444f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1524, 5035)
      LocalObject(
        1525,
        FacilityTurret.Constructor(Vector3(2758.813f, 2390.496f, 65.50444f), manned_turret),
        owning_building_guid = 27
      )
      TurretToWeapon(1525, 5036)
      LocalObject(
        858,
        ImplantTerminalMech.Constructor(Vector3(2704.066f, 2396.368f, 46.57944f)),
        owning_building_guid = 27
      )
      LocalObject(
        850,
        Terminal.Constructor(Vector3(2704.066f, 2396.386f, 46.57944f), implant_terminal_interface),
        owning_building_guid = 27
      )
      TerminalToInterface(858, 850)
      LocalObject(
        859,
        ImplantTerminalMech.Constructor(Vector3(2704.054f, 2411.724f, 46.57944f)),
        owning_building_guid = 27
      )
      LocalObject(
        851,
        Terminal.Constructor(Vector3(2704.054f, 2411.706f, 46.57944f), implant_terminal_interface),
        owning_building_guid = 27
      )
      TerminalToInterface(859, 851)
      LocalObject(
        2056,
        Painbox.Constructor(Vector3(2699.594f, 2332.334f, 71.13124f), painbox),
        owning_building_guid = 27
      )
      LocalObject(
        2068,
        Painbox.Constructor(Vector3(2708.753f, 2359.712f, 51.17234f), painbox_continuous),
        owning_building_guid = 27
      )
      LocalObject(
        2080,
        Painbox.Constructor(Vector3(2698.182f, 2346.798f, 71.33634f), painbox_door_radius),
        owning_building_guid = 27
      )
      LocalObject(
        2092,
        Painbox.Constructor(Vector3(2704.54f, 2349.793f, 49.45834f), painbox_door_radius_continuous),
        owning_building_guid = 27
      )
      LocalObject(
        2093,
        Painbox.Constructor(Vector3(2706.515f, 2377.831f, 48.81664f), painbox_door_radius_continuous),
        owning_building_guid = 27
      )
      LocalObject(
        2094,
        Painbox.Constructor(Vector3(2721.882f, 2371.05f, 50.64334f), painbox_door_radius_continuous),
        owning_building_guid = 27
      )
      LocalObject(272, Generator.Constructor(Vector3(2698.025f, 2328.445f, 65.80844f)), owning_building_guid = 27)
      LocalObject(
        260,
        Terminal.Constructor(Vector3(2697.978f, 2336.637f, 67.10244f), gen_control),
        owning_building_guid = 27
      )
    }

    Building11()

    def Building11(): Unit = { // Name: Onatha Type: cryo_facility GUID: 30, MapID: 11
      LocalBuilding(
        "Onatha",
        30,
        11,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3350f, 5732f, 47.90467f),
            Vector3(0f, 0f, 360f),
            cryo_facility
          )
        )
      )
      LocalObject(
        192,
        CaptureTerminal.Constructor(Vector3(3321.911f, 5791.266f, 37.90467f), capture_terminal),
        owning_building_guid = 30
      )
      LocalObject(309, Door.Constructor(Vector3(3291.023f, 5736.5f, 49.45567f)), owning_building_guid = 30)
      LocalObject(310, Door.Constructor(Vector3(3291.023f, 5754.693f, 57.41967f)), owning_building_guid = 30)
      LocalObject(311, Door.Constructor(Vector3(3307.674f, 5799.803f, 49.45567f)), owning_building_guid = 30)
      LocalObject(312, Door.Constructor(Vector3(3325.867f, 5799.803f, 57.41967f)), owning_building_guid = 30)
      LocalObject(313, Door.Constructor(Vector3(3334f, 5752f, 59.42567f)), owning_building_guid = 30)
      LocalObject(314, Door.Constructor(Vector3(3354f, 5735.137f, 59.42567f)), owning_building_guid = 30)
      LocalObject(315, Door.Constructor(Vector3(3355.307f, 5649.073f, 57.41967f)), owning_building_guid = 30)
      LocalObject(320, Door.Constructor(Vector3(3373.5f, 5649.073f, 49.45567f)), owning_building_guid = 30)
      LocalObject(325, Door.Constructor(Vector3(3382f, 5640f, 49.42567f)), owning_building_guid = 30)
      LocalObject(327, Door.Constructor(Vector3(3400.927f, 5681.307f, 57.41967f)), owning_building_guid = 30)
      LocalObject(328, Door.Constructor(Vector3(3400.927f, 5699.5f, 49.45567f)), owning_building_guid = 30)
      LocalObject(591, Door.Constructor(Vector3(3302f, 5720f, 39.42567f)), owning_building_guid = 30)
      LocalObject(592, Door.Constructor(Vector3(3310f, 5672f, 41.92567f)), owning_building_guid = 30)
      LocalObject(593, Door.Constructor(Vector3(3314f, 5732f, 39.42567f)), owning_building_guid = 30)
      LocalObject(594, Door.Constructor(Vector3(3314f, 5772f, 39.42567f)), owning_building_guid = 30)
      LocalObject(595, Door.Constructor(Vector3(3318f, 5752f, 39.42567f)), owning_building_guid = 30)
      LocalObject(596, Door.Constructor(Vector3(3318f, 5800f, 39.42567f)), owning_building_guid = 30)
      LocalObject(597, Door.Constructor(Vector3(3326f, 5800f, 39.42567f)), owning_building_guid = 30)
      LocalObject(598, Door.Constructor(Vector3(3330f, 5676f, 41.92567f)), owning_building_guid = 30)
      LocalObject(599, Door.Constructor(Vector3(3330f, 5700f, 39.42567f)), owning_building_guid = 30)
      LocalObject(600, Door.Constructor(Vector3(3334f, 5752f, 39.42567f)), owning_building_guid = 30)
      LocalObject(601, Door.Constructor(Vector3(3334f, 5752f, 49.42567f)), owning_building_guid = 30)
      LocalObject(602, Door.Constructor(Vector3(3334f, 5800f, 39.42567f)), owning_building_guid = 30)
      LocalObject(603, Door.Constructor(Vector3(3338f, 5644f, 41.92567f)), owning_building_guid = 30)
      LocalObject(604, Door.Constructor(Vector3(3338f, 5732f, 39.42567f)), owning_building_guid = 30)
      LocalObject(605, Door.Constructor(Vector3(3338f, 5772f, 39.42567f)), owning_building_guid = 30)
      LocalObject(606, Door.Constructor(Vector3(3350f, 5704f, 39.42567f)), owning_building_guid = 30)
      LocalObject(607, Door.Constructor(Vector3(3350f, 5728f, 39.42567f)), owning_building_guid = 30)
      LocalObject(608, Door.Constructor(Vector3(3350f, 5784f, 39.42567f)), owning_building_guid = 30)
      LocalObject(609, Door.Constructor(Vector3(3354f, 5724f, 59.42567f)), owning_building_guid = 30)
      LocalObject(610, Door.Constructor(Vector3(3362f, 5732f, 39.42567f)), owning_building_guid = 30)
      LocalObject(611, Door.Constructor(Vector3(3362f, 5756f, 39.42567f)), owning_building_guid = 30)
      LocalObject(612, Door.Constructor(Vector3(3374f, 5736f, 31.92567f)), owning_building_guid = 30)
      LocalObject(613, Door.Constructor(Vector3(3374f, 5752f, 39.42567f)), owning_building_guid = 30)
      LocalObject(831, Door.Constructor(Vector3(3353.992f, 5757.083f, 50.18767f)), owning_building_guid = 30)
      LocalObject(844, Door.Constructor(Vector3(3346f, 5740f, 49.42367f)), owning_building_guid = 30)
      LocalObject(845, Door.Constructor(Vector3(3362f, 5732f, 49.42567f)), owning_building_guid = 30)
      LocalObject(2501, Door.Constructor(Vector3(3354.673f, 5737.733f, 39.75867f)), owning_building_guid = 30)
      LocalObject(2502, Door.Constructor(Vector3(3354.673f, 5745.026f, 39.75867f)), owning_building_guid = 30)
      LocalObject(2503, Door.Constructor(Vector3(3354.673f, 5752.315f, 39.75867f)), owning_building_guid = 30)
      LocalObject(
        879,
        IFFLock.Constructor(Vector3(3350.77f, 5759.822f, 49.38667f), Vector3(0, 0, 0)),
        owning_building_guid = 30,
        door_guid = 831
      )
      LocalObject(
        915,
        IFFLock.Constructor(Vector3(3317.06f, 5798.428f, 39.24067f), Vector3(0, 0, 270)),
        owning_building_guid = 30,
        door_guid = 596
      )
      LocalObject(
        916,
        IFFLock.Constructor(Vector3(3326.813f, 5801.572f, 39.24067f), Vector3(0, 0, 90)),
        owning_building_guid = 30,
        door_guid = 597
      )
      LocalObject(
        917,
        IFFLock.Constructor(Vector3(3334.814f, 5754.043f, 59.35667f), Vector3(0, 0, 90)),
        owning_building_guid = 30,
        door_guid = 313
      )
      LocalObject(
        918,
        IFFLock.Constructor(Vector3(3351.954f, 5735.958f, 59.35667f), Vector3(0, 0, 0)),
        owning_building_guid = 30,
        door_guid = 314
      )
      LocalObject(
        919,
        IFFLock.Constructor(Vector3(3360.428f, 5756.81f, 39.24067f), Vector3(0, 0, 0)),
        owning_building_guid = 30,
        door_guid = 611
      )
      LocalObject(
        922,
        IFFLock.Constructor(Vector3(3363.572f, 5731.19f, 39.24067f), Vector3(0, 0, 180)),
        owning_building_guid = 30,
        door_guid = 610
      )
      LocalObject(
        925,
        IFFLock.Constructor(Vector3(3373.19f, 5734.428f, 31.74067f), Vector3(0, 0, 270)),
        owning_building_guid = 30,
        door_guid = 612
      )
      LocalObject(
        930,
        IFFLock.Constructor(Vector3(3382.814f, 5642.042f, 49.35667f), Vector3(0, 0, 90)),
        owning_building_guid = 30,
        door_guid = 325
      )
      LocalObject(1156, Locker.Constructor(Vector3(3365.563f, 5734.141f, 38.16566f)), owning_building_guid = 30)
      LocalObject(1157, Locker.Constructor(Vector3(3366.727f, 5734.141f, 38.16566f)), owning_building_guid = 30)
      LocalObject(1160, Locker.Constructor(Vector3(3367.874f, 5734.141f, 38.16566f)), owning_building_guid = 30)
      LocalObject(1161, Locker.Constructor(Vector3(3369.023f, 5734.141f, 38.16566f)), owning_building_guid = 30)
      LocalObject(1164, Locker.Constructor(Vector3(3369.997f, 5781.496f, 37.89967f)), owning_building_guid = 30)
      LocalObject(1165, Locker.Constructor(Vector3(3369.997f, 5782.752f, 37.89967f)), owning_building_guid = 30)
      LocalObject(1166, Locker.Constructor(Vector3(3369.997f, 5784.013f, 37.89967f)), owning_building_guid = 30)
      LocalObject(1167, Locker.Constructor(Vector3(3369.997f, 5785.275f, 37.89967f)), owning_building_guid = 30)
      LocalObject(1168, Locker.Constructor(Vector3(3369.997f, 5786.527f, 37.89967f)), owning_building_guid = 30)
      LocalObject(1173, Locker.Constructor(Vector3(3382.817f, 5734.36f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1174, Locker.Constructor(Vector3(3382.814f, 5754.361f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1177, Locker.Constructor(Vector3(3383.873f, 5734.36f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1178, Locker.Constructor(Vector3(3383.868f, 5754.361f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1179, Locker.Constructor(Vector3(3384.928f, 5734.36f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1180, Locker.Constructor(Vector3(3384.929f, 5754.361f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1183, Locker.Constructor(Vector3(3385.983f, 5734.36f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1184, Locker.Constructor(Vector3(3385.984f, 5754.361f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1185, Locker.Constructor(Vector3(3387.043f, 5734.36f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1186, Locker.Constructor(Vector3(3387.039f, 5754.361f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1189, Locker.Constructor(Vector3(3388.098f, 5734.36f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1190, Locker.Constructor(Vector3(3388.095f, 5754.361f, 37.81267f)), owning_building_guid = 30)
      LocalObject(1483, Locker.Constructor(Vector3(3368.26f, 5719.787f, 47.90467f)), owning_building_guid = 30)
      LocalObject(1484, Locker.Constructor(Vector3(3368.26f, 5720.821f, 47.90467f)), owning_building_guid = 30)
      LocalObject(1485, Locker.Constructor(Vector3(3368.26f, 5723.338f, 47.67567f)), owning_building_guid = 30)
      LocalObject(1486, Locker.Constructor(Vector3(3368.26f, 5724.372f, 47.67567f)), owning_building_guid = 30)
      LocalObject(1487, Locker.Constructor(Vector3(3368.26f, 5725.426f, 47.67567f)), owning_building_guid = 30)
      LocalObject(1488, Locker.Constructor(Vector3(3368.26f, 5726.46f, 47.67567f)), owning_building_guid = 30)
      LocalObject(1489, Locker.Constructor(Vector3(3368.26f, 5728.982f, 47.90467f)), owning_building_guid = 30)
      LocalObject(1490, Locker.Constructor(Vector3(3368.26f, 5730.016f, 47.90467f)), owning_building_guid = 30)
      LocalObject(
        211,
        Terminal.Constructor(Vector3(3352.276f, 5780.25f, 37.89467f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        212,
        Terminal.Constructor(Vector3(3352.276f, 5787.575f, 37.89467f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        213,
        Terminal.Constructor(Vector3(3353.724f, 5778.802f, 37.89467f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        214,
        Terminal.Constructor(Vector3(3353.724f, 5789.023f, 37.89467f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        215,
        Terminal.Constructor(Vector3(3366.424f, 5778.802f, 37.89467f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        216,
        Terminal.Constructor(Vector3(3366.424f, 5789.023f, 37.89467f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        217,
        Terminal.Constructor(Vector3(3367.872f, 5780.25f, 37.89467f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        218,
        Terminal.Constructor(Vector3(3367.872f, 5787.575f, 37.89467f), cert_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1674,
        Terminal.Constructor(Vector3(3339.972f, 5741.526f, 49.19967f), order_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1675,
        Terminal.Constructor(Vector3(3368.654f, 5739.408f, 39.49467f), order_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1676,
        Terminal.Constructor(Vector3(3368.654f, 5743.139f, 39.49467f), order_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1677,
        Terminal.Constructor(Vector3(3368.654f, 5746.928f, 39.49467f), order_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2417,
        Terminal.Constructor(Vector3(3310f, 5731.407f, 39.51767f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2418,
        Terminal.Constructor(Vector3(3333.91f, 5675.41f, 42.01767f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2419,
        Terminal.Constructor(Vector3(3349.407f, 5796f, 39.51767f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2420,
        Terminal.Constructor(Vector3(3351.905f, 5718.359f, 49.48367f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2421,
        Terminal.Constructor(Vector3(3354.971f, 5735.243f, 40.03867f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2422,
        Terminal.Constructor(Vector3(3354.967f, 5742.535f, 40.03867f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2423,
        Terminal.Constructor(Vector3(3354.97f, 5749.823f, 40.03867f), spawn_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2647,
        Terminal.Constructor(Vector3(3387.628f, 5668.814f, 50.20967f), vehicle_terminal_combined),
        owning_building_guid = 30
      )
      LocalObject(
        1624,
        VehicleSpawnPad.Constructor(Vector3(3373.989f, 5668.724f, 46.05167f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 30,
        terminal_guid = 2647
      )
      LocalObject(2294, ResourceSilo.Constructor(Vector3(3289.733f, 5782.852f, 54.92167f)), owning_building_guid = 30)
      LocalObject(
        2314,
        SpawnTube.Constructor(Vector3(3354.233f, 5736.683f, 37.90467f), Vector3(0, 0, 0)),
        owning_building_guid = 30
      )
      LocalObject(
        2315,
        SpawnTube.Constructor(Vector3(3354.233f, 5743.974f, 37.90467f), Vector3(0, 0, 0)),
        owning_building_guid = 30
      )
      LocalObject(
        2316,
        SpawnTube.Constructor(Vector3(3354.233f, 5751.262f, 37.90467f), Vector3(0, 0, 0)),
        owning_building_guid = 30
      )
      LocalObject(
        142,
        ProximityTerminal.Constructor(Vector3(3351.983f, 5724.892f, 47.71467f), adv_med_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1642,
        ProximityTerminal.Constructor(Vector3(3377.642f, 5735.952f, 37.90467f), medical_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1870,
        ProximityTerminal.Constructor(Vector3(3331.883f, 5670.061f, 58.19767f), pad_landing_frame),
        owning_building_guid = 30
      )
      LocalObject(
        1871,
        Terminal.Constructor(Vector3(3331.883f, 5670.061f, 58.19767f), air_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1873,
        ProximityTerminal.Constructor(Vector3(3335.101f, 5785.651f, 56.24667f), pad_landing_frame),
        owning_building_guid = 30
      )
      LocalObject(
        1874,
        Terminal.Constructor(Vector3(3335.101f, 5785.651f, 56.24667f), air_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1876,
        ProximityTerminal.Constructor(Vector3(3348.198f, 5662.777f, 56.25667f), pad_landing_frame),
        owning_building_guid = 30
      )
      LocalObject(
        1877,
        Terminal.Constructor(Vector3(3348.198f, 5662.777f, 56.25667f), air_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1879,
        ProximityTerminal.Constructor(Vector3(3351.323f, 5777.253f, 58.23667f), pad_landing_frame),
        owning_building_guid = 30
      )
      LocalObject(
        1880,
        Terminal.Constructor(Vector3(3351.323f, 5777.253f, 58.23667f), air_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2205,
        ProximityTerminal.Constructor(Vector3(3314.525f, 5647.154f, 47.65467f), repair_silo),
        owning_building_guid = 30
      )
      LocalObject(
        2206,
        Terminal.Constructor(Vector3(3314.525f, 5647.154f, 47.65467f), ground_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        2209,
        ProximityTerminal.Constructor(Vector3(3402.53f, 5747.861f, 47.65467f), repair_silo),
        owning_building_guid = 30
      )
      LocalObject(
        2210,
        Terminal.Constructor(Vector3(3402.53f, 5747.861f, 47.65467f), ground_rearm_terminal),
        owning_building_guid = 30
      )
      LocalObject(
        1528,
        FacilityTurret.Constructor(Vector3(3278.392f, 5812.472f, 56.30667f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1528, 5037)
      LocalObject(
        1529,
        FacilityTurret.Constructor(Vector3(3278.4f, 5635.379f, 56.30667f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1529, 5038)
      LocalObject(
        1532,
        FacilityTurret.Constructor(Vector3(3371.665f, 5813.605f, 56.30667f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1532, 5039)
      LocalObject(
        1535,
        FacilityTurret.Constructor(Vector3(3413.626f, 5635.371f, 56.30667f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1535, 5040)
      LocalObject(
        1536,
        FacilityTurret.Constructor(Vector3(3414.813f, 5770.496f, 56.30667f), manned_turret),
        owning_building_guid = 30
      )
      TurretToWeapon(1536, 5041)
      LocalObject(
        860,
        ImplantTerminalMech.Constructor(Vector3(3360.066f, 5776.368f, 37.38167f)),
        owning_building_guid = 30
      )
      LocalObject(
        852,
        Terminal.Constructor(Vector3(3360.066f, 5776.386f, 37.38167f), implant_terminal_interface),
        owning_building_guid = 30
      )
      TerminalToInterface(860, 852)
      LocalObject(
        861,
        ImplantTerminalMech.Constructor(Vector3(3360.054f, 5791.724f, 37.38167f)),
        owning_building_guid = 30
      )
      LocalObject(
        853,
        Terminal.Constructor(Vector3(3360.054f, 5791.706f, 37.38167f), implant_terminal_interface),
        owning_building_guid = 30
      )
      TerminalToInterface(861, 853)
      LocalObject(
        2057,
        Painbox.Constructor(Vector3(3355.594f, 5712.334f, 61.93347f), painbox),
        owning_building_guid = 30
      )
      LocalObject(
        2069,
        Painbox.Constructor(Vector3(3364.753f, 5739.712f, 41.97457f), painbox_continuous),
        owning_building_guid = 30
      )
      LocalObject(
        2081,
        Painbox.Constructor(Vector3(3354.182f, 5726.798f, 62.13857f), painbox_door_radius),
        owning_building_guid = 30
      )
      LocalObject(
        2095,
        Painbox.Constructor(Vector3(3360.54f, 5729.793f, 40.26057f), painbox_door_radius_continuous),
        owning_building_guid = 30
      )
      LocalObject(
        2096,
        Painbox.Constructor(Vector3(3362.515f, 5757.831f, 39.61887f), painbox_door_radius_continuous),
        owning_building_guid = 30
      )
      LocalObject(
        2097,
        Painbox.Constructor(Vector3(3377.882f, 5751.05f, 41.44557f), painbox_door_radius_continuous),
        owning_building_guid = 30
      )
      LocalObject(273, Generator.Constructor(Vector3(3354.025f, 5708.445f, 56.61067f)), owning_building_guid = 30)
      LocalObject(
        261,
        Terminal.Constructor(Vector3(3353.978f, 5716.637f, 57.90467f), gen_control),
        owning_building_guid = 30
      )
    }

    Building9()

    def Building9(): Unit = { // Name: Kyoi Type: cryo_facility GUID: 33, MapID: 9
      LocalBuilding(
        "Kyoi",
        33,
        9,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5556f, 2232f, 62.32201f),
            Vector3(0f, 0f, 88f),
            cryo_facility
          )
        )
      )
      LocalObject(
        199,
        CaptureTerminal.Constructor(Vector3(5495.79f, 2205.997f, 52.32201f), capture_terminal),
        owning_building_guid = 33
      )
      LocalObject(484, Door.Constructor(Vector3(5486.761f, 2192.066f, 63.873f)), owning_building_guid = 33)
      LocalObject(485, Door.Constructor(Vector3(5487.396f, 2210.248f, 71.83701f)), owning_building_guid = 33)
      LocalObject(486, Door.Constructor(Vector3(5531.263f, 2173.851f, 71.83701f)), owning_building_guid = 33)
      LocalObject(487, Door.Constructor(Vector3(5535.454f, 2216.708f, 73.843f)), owning_building_guid = 33)
      LocalObject(488, Door.Constructor(Vector3(5549.444f, 2173.216f, 63.873f)), owning_building_guid = 33)
      LocalObject(489, Door.Constructor(Vector3(5553.004f, 2236.107f, 73.843f)), owning_building_guid = 33)
      LocalObject(494, Door.Constructor(Vector3(5590.257f, 2281.762f, 63.873f)), owning_building_guid = 33)
      LocalObject(495, Door.Constructor(Vector3(5608.439f, 2281.127f, 71.83701f)), owning_building_guid = 33)
      LocalObject(496, Door.Constructor(Vector3(5639.062f, 2234.41f, 71.83701f)), owning_building_guid = 33)
      LocalObject(497, Door.Constructor(Vector3(5639.697f, 2252.592f, 63.873f)), owning_building_guid = 33)
      LocalObject(498, Door.Constructor(Vector3(5649.061f, 2260.77f, 63.84301f)), owning_building_guid = 33)
      LocalObject(744, Door.Constructor(Vector3(5486.925f, 2202.393f, 53.84301f)), owning_building_guid = 33)
      LocalObject(745, Door.Constructor(Vector3(5487.204f, 2210.388f, 53.84301f)), owning_building_guid = 33)
      LocalObject(746, Door.Constructor(Vector3(5487.483f, 2218.383f, 53.84301f)), owning_building_guid = 33)
      LocalObject(747, Door.Constructor(Vector3(5504.032f, 2233.815f, 53.84301f)), owning_building_guid = 33)
      LocalObject(748, Door.Constructor(Vector3(5514.768f, 2197.418f, 53.84301f)), owning_building_guid = 33)
      LocalObject(749, Door.Constructor(Vector3(5515.605f, 2221.403f, 53.84301f)), owning_building_guid = 33)
      LocalObject(750, Door.Constructor(Vector3(5532.434f, 2244.83f, 53.84301f)), owning_building_guid = 33)
      LocalObject(751, Door.Constructor(Vector3(5534.896f, 2200.718f, 53.84301f)), owning_building_guid = 33)
      LocalObject(752, Door.Constructor(Vector3(5535.454f, 2216.708f, 53.84301f)), owning_building_guid = 33)
      LocalObject(753, Door.Constructor(Vector3(5535.454f, 2216.708f, 63.84301f)), owning_building_guid = 33)
      LocalObject(754, Door.Constructor(Vector3(5536.85f, 2256.683f, 53.84301f)), owning_building_guid = 33)
      LocalObject(755, Door.Constructor(Vector3(5552.84f, 2256.125f, 46.34301f)), owning_building_guid = 33)
      LocalObject(756, Door.Constructor(Vector3(5554.744f, 2196.022f, 53.84301f)), owning_building_guid = 33)
      LocalObject(757, Door.Constructor(Vector3(5555.581f, 2220.007f, 53.84301f)), owning_building_guid = 33)
      LocalObject(758, Door.Constructor(Vector3(5556.419f, 2243.993f, 53.84301f)), owning_building_guid = 33)
      LocalObject(759, Door.Constructor(Vector3(5559.998f, 2231.86f, 53.84301f)), owning_building_guid = 33)
      LocalObject(760, Door.Constructor(Vector3(5564.135f, 2235.718f, 73.843f)), owning_building_guid = 33)
      LocalObject(761, Door.Constructor(Vector3(5566.317f, 2183.61f, 53.84301f)), owning_building_guid = 33)
      LocalObject(762, Door.Constructor(Vector3(5583.983f, 2231.023f, 53.84301f)), owning_building_guid = 33)
      LocalObject(763, Door.Constructor(Vector3(5587.283f, 2210.896f, 53.84301f)), owning_building_guid = 33)
      LocalObject(764, Door.Constructor(Vector3(5611.268f, 2210.058f, 56.34301f)), owning_building_guid = 33)
      LocalObject(765, Door.Constructor(Vector3(5614.567f, 2189.93f, 56.34301f)), owning_building_guid = 33)
      LocalObject(766, Door.Constructor(Vector3(5643.528f, 2216.936f, 56.34301f)), owning_building_guid = 33)
      LocalObject(838, Door.Constructor(Vector3(5531.072f, 2236.865f, 64.605f)), owning_building_guid = 33)
      LocalObject(846, Door.Constructor(Vector3(5547.865f, 2228.282f, 63.84101f)), owning_building_guid = 33)
      LocalObject(847, Door.Constructor(Vector3(5556.419f, 2243.993f, 63.84301f)), owning_building_guid = 33)
      LocalObject(2552, Door.Constructor(Vector3(5535.86f, 2237.379f, 54.17601f)), owning_building_guid = 33)
      LocalObject(2553, Door.Constructor(Vector3(5543.145f, 2237.125f, 54.17601f)), owning_building_guid = 33)
      LocalObject(2554, Door.Constructor(Vector3(5550.434f, 2236.87f, 54.17601f)), owning_building_guid = 33)
      LocalObject(
        886,
        IFFLock.Constructor(Vector3(5528.222f, 2233.74f, 63.804f), Vector3(0, 0, 272)),
        owning_building_guid = 33,
        door_guid = 838
      )
      LocalObject(
        1053,
        IFFLock.Constructor(Vector3(5485.661f, 2211.255f, 53.658f), Vector3(0, 0, 2)),
        owning_building_guid = 33,
        door_guid = 745
      )
      LocalObject(
        1054,
        IFFLock.Constructor(Vector3(5488.463f, 2201.398f, 53.658f), Vector3(0, 0, 182)),
        owning_building_guid = 33,
        door_guid = 744
      )
      LocalObject(
        1055,
        IFFLock.Constructor(Vector3(5531.569f, 2243.288f, 53.658f), Vector3(0, 0, 272)),
        owning_building_guid = 33,
        door_guid = 750
      )
      LocalObject(
        1056,
        IFFLock.Constructor(Vector3(5533.44f, 2217.593f, 73.774f), Vector3(0, 0, 2)),
        owning_building_guid = 33,
        door_guid = 487
      )
      LocalObject(
        1057,
        IFFLock.Constructor(Vector3(5552.113f, 2234.091f, 73.774f), Vector3(0, 0, 272)),
        owning_building_guid = 33,
        door_guid = 489
      )
      LocalObject(
        1058,
        IFFLock.Constructor(Vector3(5554.383f, 2255.26f, 46.158f), Vector3(0, 0, 182)),
        owning_building_guid = 33,
        door_guid = 755
      )
      LocalObject(
        1059,
        IFFLock.Constructor(Vector3(5557.283f, 2245.535f, 53.658f), Vector3(0, 0, 92)),
        owning_building_guid = 33,
        door_guid = 758
      )
      LocalObject(
        1064,
        IFFLock.Constructor(Vector3(5647.048f, 2261.655f, 63.77401f), Vector3(0, 0, 2)),
        owning_building_guid = 33,
        door_guid = 498
      )
      LocalObject(1369, Locker.Constructor(Vector3(5502.204f, 2253.888f, 52.31701f)), owning_building_guid = 33)
      LocalObject(1370, Locker.Constructor(Vector3(5503.456f, 2253.844f, 52.31701f)), owning_building_guid = 33)
      LocalObject(1371, Locker.Constructor(Vector3(5504.717f, 2253.8f, 52.31701f)), owning_building_guid = 33)
      LocalObject(1372, Locker.Constructor(Vector3(5505.977f, 2253.756f, 52.31701f)), owning_building_guid = 33)
      LocalObject(1373, Locker.Constructor(Vector3(5507.232f, 2253.712f, 52.31701f)), owning_building_guid = 33)
      LocalObject(1374, Locker.Constructor(Vector3(5534.798f, 2265.574f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1375, Locker.Constructor(Vector3(5534.834f, 2266.628f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1376, Locker.Constructor(Vector3(5534.872f, 2267.688f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1377, Locker.Constructor(Vector3(5534.908f, 2268.742f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1378, Locker.Constructor(Vector3(5534.945f, 2269.797f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1379, Locker.Constructor(Vector3(5534.982f, 2270.852f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1380, Locker.Constructor(Vector3(5554.403f, 2247.628f, 52.58301f)), owning_building_guid = 33)
      LocalObject(1381, Locker.Constructor(Vector3(5554.444f, 2248.792f, 52.58301f)), owning_building_guid = 33)
      LocalObject(1382, Locker.Constructor(Vector3(5554.484f, 2249.938f, 52.58301f)), owning_building_guid = 33)
      LocalObject(1383, Locker.Constructor(Vector3(5554.524f, 2251.086f, 52.58301f)), owning_building_guid = 33)
      LocalObject(1384, Locker.Constructor(Vector3(5554.787f, 2264.879f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1385, Locker.Constructor(Vector3(5554.824f, 2265.935f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1386, Locker.Constructor(Vector3(5554.86f, 2266.989f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1387, Locker.Constructor(Vector3(5554.897f, 2268.043f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1388, Locker.Constructor(Vector3(5554.934f, 2269.103f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1389, Locker.Constructor(Vector3(5554.971f, 2270.157f, 52.23001f)), owning_building_guid = 33)
      LocalObject(1491, Locker.Constructor(Vector3(5558.62f, 2250.18f, 62.32201f)), owning_building_guid = 33)
      LocalObject(1492, Locker.Constructor(Vector3(5559.653f, 2250.144f, 62.32201f)), owning_building_guid = 33)
      LocalObject(1493, Locker.Constructor(Vector3(5562.174f, 2250.055f, 62.09301f)), owning_building_guid = 33)
      LocalObject(1494, Locker.Constructor(Vector3(5563.207f, 2250.02f, 62.09301f)), owning_building_guid = 33)
      LocalObject(1495, Locker.Constructor(Vector3(5564.261f, 2249.983f, 62.09301f)), owning_building_guid = 33)
      LocalObject(1496, Locker.Constructor(Vector3(5565.294f, 2249.947f, 62.09301f)), owning_building_guid = 33)
      LocalObject(1497, Locker.Constructor(Vector3(5567.81f, 2249.859f, 62.32201f)), owning_building_guid = 33)
      LocalObject(1498, Locker.Constructor(Vector3(5568.843f, 2249.823f, 62.32201f)), owning_building_guid = 33)
      LocalObject(
        219,
        Terminal.Constructor(Vector3(5499.142f, 2237.712f, 52.312f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        220,
        Terminal.Constructor(Vector3(5499.585f, 2250.404f, 52.312f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        221,
        Terminal.Constructor(Vector3(5500.538f, 2236.214f, 52.312f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        222,
        Terminal.Constructor(Vector3(5501.083f, 2251.801f, 52.312f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        223,
        Terminal.Constructor(Vector3(5507.859f, 2235.958f, 52.312f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        224,
        Terminal.Constructor(Vector3(5508.403f, 2251.545f, 52.312f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        225,
        Terminal.Constructor(Vector3(5509.356f, 2237.355f, 52.312f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        226,
        Terminal.Constructor(Vector3(5509.8f, 2250.047f, 52.312f), cert_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1757,
        Terminal.Constructor(Vector3(5541.732f, 2251.164f, 53.91201f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1758,
        Terminal.Constructor(Vector3(5545.519f, 2251.031f, 53.91201f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1759,
        Terminal.Constructor(Vector3(5546.13f, 2222.311f, 63.617f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1760,
        Terminal.Constructor(Vector3(5549.248f, 2250.901f, 53.91201f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2462,
        Terminal.Constructor(Vector3(5492.018f, 2233.641f, 53.93501f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2463,
        Terminal.Constructor(Vector3(5538.361f, 2237.589f, 54.45601f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2464,
        Terminal.Constructor(Vector3(5545.645f, 2237.332f, 54.45601f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2465,
        Terminal.Constructor(Vector3(5552.933f, 2237.081f, 54.45601f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2466,
        Terminal.Constructor(Vector3(5555.197f, 2192.004f, 53.93501f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2467,
        Terminal.Constructor(Vector3(5569.699f, 2233.428f, 63.901f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2468,
        Terminal.Constructor(Vector3(5611.994f, 2213.945f, 56.43501f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2654,
        Terminal.Constructor(Vector3(5620.461f, 2267.4f, 64.62701f), vehicle_terminal_combined),
        owning_building_guid = 33
      )
      LocalObject(
        1635,
        VehicleSpawnPad.Constructor(Vector3(5620.075f, 2253.766f, 60.46901f), mb_pad_creation, Vector3(0, 0, 182)),
        owning_building_guid = 33,
        terminal_guid = 2654
      )
      LocalObject(2301, ResourceSilo.Constructor(Vector3(5503.076f, 2173.544f, 69.339f)), owning_building_guid = 33)
      LocalObject(
        2365,
        SpawnTube.Constructor(Vector3(5536.897f, 2236.903f, 52.32201f), Vector3(0, 0, 272)),
        owning_building_guid = 33
      )
      LocalObject(
        2366,
        SpawnTube.Constructor(Vector3(5544.181f, 2236.648f, 52.32201f), Vector3(0, 0, 272)),
        owning_building_guid = 33
      )
      LocalObject(
        2367,
        SpawnTube.Constructor(Vector3(5551.468f, 2236.394f, 52.32201f), Vector3(0, 0, 272)),
        owning_building_guid = 33
      )
      LocalObject(
        143,
        ProximityTerminal.Constructor(Vector3(5563.173f, 2233.734f, 62.13201f), adv_med_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1655,
        ProximityTerminal.Constructor(Vector3(5553.015f, 2259.763f, 52.32201f), medical_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1948,
        ProximityTerminal.Constructor(Vector3(5501.862f, 2218.982f, 70.66401f), pad_landing_frame),
        owning_building_guid = 33
      )
      LocalObject(
        1949,
        Terminal.Constructor(Vector3(5501.862f, 2218.982f, 70.66401f), air_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1951,
        ProximityTerminal.Constructor(Vector3(5510.821f, 2234.902f, 72.65401f), pad_landing_frame),
        owning_building_guid = 33
      )
      LocalObject(
        1952,
        Terminal.Constructor(Vector3(5510.821f, 2234.902f, 72.65401f), air_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1954,
        ProximityTerminal.Constructor(Vector3(5617.269f, 2211.732f, 72.61501f), pad_landing_frame),
        owning_building_guid = 33
      )
      LocalObject(
        1955,
        Terminal.Constructor(Vector3(5617.269f, 2211.732f, 72.61501f), air_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1957,
        ProximityTerminal.Constructor(Vector3(5625.118f, 2227.783f, 70.674f), pad_landing_frame),
        owning_building_guid = 33
      )
      LocalObject(
        1958,
        Terminal.Constructor(Vector3(5625.118f, 2227.783f, 70.674f), air_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2261,
        ProximityTerminal.Constructor(Vector3(5541.982f, 2285.051f, 62.07201f), repair_silo),
        owning_building_guid = 33
      )
      LocalObject(
        2262,
        Terminal.Constructor(Vector3(5541.982f, 2285.051f, 62.07201f), ground_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2265,
        ProximityTerminal.Constructor(Vector3(5639.556f, 2193.585f, 62.07201f), repair_silo),
        owning_building_guid = 33
      )
      LocalObject(
        2266,
        Terminal.Constructor(Vector3(5639.556f, 2193.585f, 62.07201f), ground_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        1588,
        FacilityTurret.Constructor(Vector3(5473.078f, 2163.244f, 70.72401f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1588, 5042)
      LocalObject(
        1589,
        FacilityTurret.Constructor(Vector3(5475.201f, 2256.5f, 70.72401f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1589, 5043)
      LocalObject(
        1591,
        FacilityTurret.Constructor(Vector3(5519.79f, 2298.117f, 70.72401f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1591, 5044)
      LocalObject(
        1594,
        FacilityTurret.Constructor(Vector3(5650.063f, 2157.072f, 70.72401f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1594, 5045)
      LocalObject(
        1595,
        FacilityTurret.Constructor(Vector3(5654.791f, 2292.215f, 70.72401f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(1595, 5046)
      LocalObject(
        862,
        ImplantTerminalMech.Constructor(Vector3(5496.663f, 2244.132f, 51.79901f)),
        owning_building_guid = 33
      )
      LocalObject(
        854,
        Terminal.Constructor(Vector3(5496.681f, 2244.132f, 51.79901f), implant_terminal_interface),
        owning_building_guid = 33
      )
      TerminalToInterface(862, 854)
      LocalObject(
        863,
        ImplantTerminalMech.Constructor(Vector3(5512.01f, 2243.608f, 51.79901f)),
        owning_building_guid = 33
      )
      LocalObject(
        855,
        Terminal.Constructor(Vector3(5511.992f, 2243.609f, 51.79901f), implant_terminal_interface),
        owning_building_guid = 33
      )
      TerminalToInterface(863, 855)
      LocalObject(
        2064,
        Painbox.Constructor(Vector3(5575.85f, 2236.904f, 76.35081f), painbox),
        owning_building_guid = 33
      )
      LocalObject(
        2076,
        Painbox.Constructor(Vector3(5548.808f, 2247.013f, 56.39191f), painbox_continuous),
        owning_building_guid = 33
      )
      LocalObject(
        2088,
        Painbox.Constructor(Vector3(5561.345f, 2235.998f, 76.55591f), painbox_door_radius),
        owning_building_guid = 33
      )
      LocalObject(
        2116,
        Painbox.Constructor(Vector3(5530.622f, 2245.409f, 54.03621f), painbox_door_radius_continuous),
        owning_building_guid = 33
      )
      LocalObject(
        2117,
        Painbox.Constructor(Vector3(5537.935f, 2260.53f, 55.86291f), painbox_door_radius_continuous),
        owning_building_guid = 33
      )
      LocalObject(
        2118,
        Painbox.Constructor(Vector3(5558.573f, 2242.456f, 54.67791f), painbox_door_radius_continuous),
        owning_building_guid = 33
      )
      LocalObject(280, Generator.Constructor(Vector3(5579.681f, 2235.2f, 71.02801f)), owning_building_guid = 33)
      LocalObject(
        268,
        Terminal.Constructor(Vector3(5571.493f, 2235.439f, 72.32201f), gen_control),
        owning_building_guid = 33
      )
    }

    Building15()

    def Building15(): Unit = { // Name: Xelas Type: cryo_facility GUID: 36, MapID: 15
      LocalBuilding(
        "Xelas",
        36,
        15,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(6644f, 4402f, 56.31071f),
            Vector3(0f, 0f, 62f),
            cryo_facility
          )
        )
      )
      LocalObject(
        202,
        CaptureTerminal.Constructor(Vector3(6578.484f, 4405.022f, 46.31071f), capture_terminal),
        owning_building_guid = 36
      )
      LocalObject(551, Door.Constructor(Vector3(6564.263f, 4396.46f, 57.86171f)), owning_building_guid = 36)
      LocalObject(552, Door.Constructor(Vector3(6572.804f, 4412.523f, 65.82571f)), owning_building_guid = 36)
      LocalObject(553, Door.Constructor(Vector3(6596.275f, 4360.58f, 65.82571f)), owning_building_guid = 36)
      LocalObject(554, Door.Constructor(Vector3(6612.339f, 4352.039f, 57.86171f)), owning_building_guid = 36)
      LocalObject(555, Door.Constructor(Vector3(6618.83f, 4397.262f, 67.83171f)), owning_building_guid = 36)
      LocalObject(556, Door.Constructor(Vector3(6643.108f, 4407.004f, 67.83171f)), owning_building_guid = 36)
      LocalObject(557, Door.Constructor(Vector3(6696.604f, 4431.708f, 57.86171f)), owning_building_guid = 36)
      LocalObject(558, Door.Constructor(Vector3(6712.668f, 4423.167f, 65.82571f)), owning_building_guid = 36)
      LocalObject(559, Door.Constructor(Vector3(6719.712f, 4367.754f, 65.82571f)), owning_building_guid = 36)
      LocalObject(560, Door.Constructor(Vector3(6728.253f, 4383.817f, 57.86171f)), owning_building_guid = 36)
      LocalObject(561, Door.Constructor(Vector3(6740.254f, 4387.063f, 57.83171f)), owning_building_guid = 36)
      LocalObject(807, Door.Constructor(Vector3(6568.937f, 4405.67f, 47.83171f)), owning_building_guid = 36)
      LocalObject(808, Door.Constructor(Vector3(6572.692f, 4412.733f, 47.83171f)), owning_building_guid = 36)
      LocalObject(809, Door.Constructor(Vector3(6576.448f, 4419.797f, 47.83171f)), owning_building_guid = 36)
      LocalObject(810, Door.Constructor(Vector3(6591.781f, 4388.993f, 47.83171f)), owning_building_guid = 36)
      LocalObject(811, Door.Constructor(Vector3(6598.087f, 4426.413f, 47.83171f)), owning_building_guid = 36)
      LocalObject(812, Door.Constructor(Vector3(6603.048f, 4410.184f, 47.83171f)), owning_building_guid = 36)
      LocalObject(813, Door.Constructor(Vector3(6611.318f, 4383.135f, 47.83171f)), owning_building_guid = 36)
      LocalObject(814, Door.Constructor(Vector3(6618.83f, 4397.262f, 47.83171f)), owning_building_guid = 36)
      LocalObject(815, Door.Constructor(Vector3(6618.83f, 4397.262f, 57.83171f)), owning_building_guid = 36)
      LocalObject(816, Door.Constructor(Vector3(6627.099f, 4370.214f, 47.83171f)), owning_building_guid = 36)
      LocalObject(817, Door.Constructor(Vector3(6628.443f, 4423.863f, 47.83171f)), owning_building_guid = 36)
      LocalObject(818, Door.Constructor(Vector3(6632.061f, 4353.985f, 47.83171f)), owning_building_guid = 36)
      LocalObject(819, Door.Constructor(Vector3(6637.608f, 4432.58f, 47.83171f)), owning_building_guid = 36)
      LocalObject(820, Door.Constructor(Vector3(6638.366f, 4391.405f, 47.83171f)), owning_building_guid = 36)
      LocalObject(821, Door.Constructor(Vector3(6647.532f, 4400.122f, 47.83171f)), owning_building_guid = 36)
      LocalObject(822, Door.Constructor(Vector3(6649.634f, 4412.595f, 47.83171f)), owning_building_guid = 36)
      LocalObject(823, Door.Constructor(Vector3(6651.735f, 4425.069f, 40.33171f)), owning_building_guid = 36)
      LocalObject(824, Door.Constructor(Vector3(6652.941f, 4401.776f, 67.83171f)), owning_building_guid = 36)
      LocalObject(825, Door.Constructor(Vector3(6662.865f, 4369.318f, 47.83171f)), owning_building_guid = 36)
      LocalObject(826, Door.Constructor(Vector3(6668.723f, 4388.855f, 47.83171f)), owning_building_guid = 36)
      LocalObject(827, Door.Constructor(Vector3(6678.198f, 4338.514f, 50.33171f)), owning_building_guid = 36)
      LocalObject(828, Door.Constructor(Vector3(6684.056f, 4358.051f, 50.33171f)), owning_building_guid = 36)
      LocalObject(829, Door.Constructor(Vector3(6716.066f, 4350.091f, 50.33171f)), owning_building_guid = 36)
      LocalObject(841, Door.Constructor(Vector3(6623.727f, 4417.3f, 58.59371f)), owning_building_guid = 36)
      LocalObject(848, Door.Constructor(Vector3(6635.059f, 4402.224f, 57.82971f)), owning_building_guid = 36)
      LocalObject(849, Door.Constructor(Vector3(6649.634f, 4412.595f, 57.83171f)), owning_building_guid = 36)
      LocalObject(2571, Door.Constructor(Vector3(6628.257f, 4415.663f, 48.16471f)), owning_building_guid = 36)
      LocalObject(2572, Door.Constructor(Vector3(6634.692f, 4412.241f, 48.16471f)), owning_building_guid = 36)
      LocalObject(2573, Door.Constructor(Vector3(6641.132f, 4408.817f, 48.16471f)), owning_building_guid = 36)
      LocalObject(
        889,
        IFFLock.Constructor(Vector3(6619.796f, 4415.742f, 57.79271f), Vector3(0, 0, 298)),
        owning_building_guid = 36,
        door_guid = 841
      )
      LocalObject(
        1103,
        IFFLock.Constructor(Vector3(6569.883f, 4404.102f, 47.64671f), Vector3(0, 0, 208)),
        owning_building_guid = 36,
        door_guid = 807
      )
      LocalObject(
        1104,
        IFFLock.Constructor(Vector3(6571.686f, 4414.189f, 47.64671f), Vector3(0, 0, 28)),
        owning_building_guid = 36,
        door_guid = 808
      )
      LocalObject(
        1105,
        IFFLock.Constructor(Vector3(6617.408f, 4398.94f, 67.76271f), Vector3(0, 0, 28)),
        owning_building_guid = 36,
        door_guid = 555
      )
      LocalObject(
        1106,
        IFFLock.Constructor(Vector3(6626.99f, 4422.855f, 47.64671f), Vector3(0, 0, 298)),
        owning_building_guid = 36,
        door_guid = 817
      )
      LocalObject(
        1107,
        IFFLock.Constructor(Vector3(6641.423f, 4405.583f, 67.76271f), Vector3(0, 0, 298)),
        owning_building_guid = 36,
        door_guid = 556
      )
      LocalObject(
        1108,
        IFFLock.Constructor(Vector3(6651.087f, 4413.603f, 47.64671f), Vector3(0, 0, 118)),
        owning_building_guid = 36,
        door_guid = 822
      )
      LocalObject(
        1109,
        IFFLock.Constructor(Vector3(6652.743f, 4423.615f, 40.14671f), Vector3(0, 0, 208)),
        owning_building_guid = 36,
        door_guid = 823
      )
      LocalObject(
        1110,
        IFFLock.Constructor(Vector3(6738.833f, 4388.74f, 57.76271f), Vector3(0, 0, 28)),
        owning_building_guid = 36,
        door_guid = 561
      )
      LocalObject(1454, Locker.Constructor(Vector3(6605.244f, 4445.255f, 46.30571f)), owning_building_guid = 36)
      LocalObject(1455, Locker.Constructor(Vector3(6606.349f, 4444.667f, 46.30571f)), owning_building_guid = 36)
      LocalObject(1456, Locker.Constructor(Vector3(6607.463f, 4444.075f, 46.30571f)), owning_building_guid = 36)
      LocalObject(1457, Locker.Constructor(Vector3(6608.577f, 4443.483f, 46.30571f)), owning_building_guid = 36)
      LocalObject(1458, Locker.Constructor(Vector3(6609.686f, 4442.893f, 46.30571f)), owning_building_guid = 36)
      LocalObject(1459, Locker.Constructor(Vector3(6639.662f, 4441.471f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1460, Locker.Constructor(Vector3(6640.156f, 4442.401f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1461, Locker.Constructor(Vector3(6640.655f, 4443.338f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1462, Locker.Constructor(Vector3(6641.15f, 4444.27f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1463, Locker.Constructor(Vector3(6641.645f, 4445.201f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1464, Locker.Constructor(Vector3(6642.141f, 4446.134f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1465, Locker.Constructor(Vector3(6649.416f, 4416.747f, 46.57171f)), owning_building_guid = 36)
      LocalObject(1466, Locker.Constructor(Vector3(6649.962f, 4417.774f, 46.57171f)), owning_building_guid = 36)
      LocalObject(1467, Locker.Constructor(Vector3(6650.501f, 4418.787f, 46.57171f)), owning_building_guid = 36)
      LocalObject(1468, Locker.Constructor(Vector3(6651.041f, 4419.801f, 46.57171f)), owning_building_guid = 36)
      LocalObject(1469, Locker.Constructor(Vector3(6657.323f, 4432.083f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1470, Locker.Constructor(Vector3(6657.819f, 4433.016f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1471, Locker.Constructor(Vector3(6658.314f, 4433.948f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1472, Locker.Constructor(Vector3(6658.809f, 4434.879f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1473, Locker.Constructor(Vector3(6659.307f, 4435.815f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1474, Locker.Constructor(Vector3(6659.802f, 4436.747f, 46.21871f)), owning_building_guid = 36)
      LocalObject(1499, Locker.Constructor(Vector3(6654.324f, 4417.191f, 56.31071f)), owning_building_guid = 36)
      LocalObject(1500, Locker.Constructor(Vector3(6655.237f, 4416.706f, 56.31071f)), owning_building_guid = 36)
      LocalObject(1501, Locker.Constructor(Vector3(6657.464f, 4415.522f, 56.08171f)), owning_building_guid = 36)
      LocalObject(1502, Locker.Constructor(Vector3(6658.377f, 4415.036f, 56.08171f)), owning_building_guid = 36)
      LocalObject(1503, Locker.Constructor(Vector3(6659.308f, 4414.542f, 56.08171f)), owning_building_guid = 36)
      LocalObject(1504, Locker.Constructor(Vector3(6660.221f, 4414.056f, 56.08171f)), owning_building_guid = 36)
      LocalObject(1505, Locker.Constructor(Vector3(6662.443f, 4412.875f, 56.31071f)), owning_building_guid = 36)
      LocalObject(1506, Locker.Constructor(Vector3(6663.356f, 4412.389f, 56.31071f)), owning_building_guid = 36)
      LocalObject(
        227,
        Terminal.Constructor(Vector3(6595.4f, 4432.059f, 46.30071f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        228,
        Terminal.Constructor(Vector3(6595.999f, 4430.101f, 46.30071f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        229,
        Terminal.Constructor(Vector3(6601.362f, 4443.272f, 46.30071f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        230,
        Terminal.Constructor(Vector3(6602.466f, 4426.662f, 46.30071f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        231,
        Terminal.Constructor(Vector3(6603.321f, 4443.871f, 46.30071f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        232,
        Terminal.Constructor(Vector3(6604.425f, 4427.26f, 46.30071f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        233,
        Terminal.Constructor(Vector3(6609.788f, 4440.432f, 46.30071f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        234,
        Terminal.Constructor(Vector3(6610.387f, 4438.474f, 46.30071f), cert_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1788,
        Terminal.Constructor(Vector3(6630.881f, 4397.618f, 57.60571f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1789,
        Terminal.Constructor(Vector3(6639.577f, 4425.479f, 47.90071f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1790,
        Terminal.Constructor(Vector3(6642.922f, 4423.7f, 47.90071f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1791,
        Terminal.Constructor(Vector3(6646.217f, 4421.948f, 47.90071f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2485,
        Terminal.Constructor(Vector3(6587.213f, 4431.522f, 47.92371f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2486,
        Terminal.Constructor(Vector3(6625.745f, 4366.404f, 47.92371f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2487,
        Terminal.Constructor(Vector3(6630.597f, 4414.756f, 48.44471f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2488,
        Terminal.Constructor(Vector3(6637.03f, 4411.332f, 48.44471f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2489,
        Terminal.Constructor(Vector3(6643.47f, 4407.912f, 48.44471f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2490,
        Terminal.Constructor(Vector3(6656.938f, 4397.278f, 57.88971f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2491,
        Terminal.Constructor(Vector3(6686.412f, 4361.226f, 50.42371f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2657,
        Terminal.Constructor(Vector3(6717.455f, 4405.56f, 58.61571f), vehicle_terminal_combined),
        owning_building_guid = 36
      )
      LocalObject(
        1640,
        VehicleSpawnPad.Constructor(Vector3(6711.131f, 4393.475f, 54.45771f), mb_pad_creation, Vector3(0, 0, 208)),
        owning_building_guid = 36,
        terminal_guid = 2657
      )
      LocalObject(2304, ResourceSilo.Constructor(Vector3(6570.807f, 4372.661f, 63.32771f)), owning_building_guid = 36)
      LocalObject(
        2384,
        SpawnTube.Constructor(Vector3(6628.98f, 4414.78f, 46.31071f), Vector3(0, 0, 298)),
        owning_building_guid = 36
      )
      LocalObject(
        2385,
        SpawnTube.Constructor(Vector3(6635.415f, 4411.359f, 46.31071f), Vector3(0, 0, 298)),
        owning_building_guid = 36
      )
      LocalObject(
        2386,
        SpawnTube.Constructor(Vector3(6641.853f, 4407.936f, 46.31071f), Vector3(0, 0, 298)),
        owning_building_guid = 36
      )
      LocalObject(
        144,
        ProximityTerminal.Constructor(Vector3(6651.207f, 4400.414f, 56.12071f), adv_med_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1660,
        ProximityTerminal.Constructor(Vector3(6653.488f, 4428.262f, 46.31071f), medical_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1990,
        ProximityTerminal.Constructor(Vector3(6589.634f, 4414.033f, 64.65271f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        1991,
        Terminal.Constructor(Vector3(6589.634f, 4414.033f, 64.65271f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1993,
        ProximityTerminal.Constructor(Vector3(6604.665f, 4424.413f, 66.64271f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        1994,
        Terminal.Constructor(Vector3(6604.665f, 4424.413f, 66.64271f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1996,
        ProximityTerminal.Constructor(Vector3(6690.184f, 4356.925f, 66.60371f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        1997,
        Terminal.Constructor(Vector3(6690.184f, 4356.925f, 66.60371f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1999,
        ProximityTerminal.Constructor(Vector3(6704.274f, 4367.911f, 64.66271f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        2000,
        Terminal.Constructor(Vector3(6704.274f, 4367.911f, 64.66271f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2285,
        ProximityTerminal.Constructor(Vector3(6654.657f, 4455.828f, 56.06071f), repair_silo),
        owning_building_guid = 36
      )
      LocalObject(
        2286,
        Terminal.Constructor(Vector3(6654.657f, 4455.828f, 56.06071f), ground_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2289,
        ProximityTerminal.Constructor(Vector3(6702.26f, 4330.844f, 56.06071f), repair_silo),
        owning_building_guid = 36
      )
      LocalObject(
        2290,
        Terminal.Constructor(Vector3(6702.26f, 4330.844f, 56.06071f), ground_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        1618,
        FacilityTurret.Constructor(Vector3(6539.33f, 4376.553f, 64.71271f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1618, 5047)
      LocalObject(
        1619,
        FacilityTurret.Constructor(Vector3(6582.118f, 4459.44f, 64.71271f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1619, 5048)
      LocalObject(
        1620,
        FacilityTurret.Constructor(Vector3(6640.438f, 4477.299f, 64.71271f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1620, 5049)
      LocalObject(
        1621,
        FacilityTurret.Constructor(Vector3(6695.697f, 4293.42f, 64.71271f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1621, 5050)
      LocalObject(
        1622,
        FacilityTurret.Constructor(Vector3(6759.189f, 4412.814f, 64.71271f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(1622, 5051)
      LocalObject(
        864,
        ImplantTerminalMech.Constructor(Vector3(6595.987f, 4438.916f, 45.78771f)),
        owning_building_guid = 36
      )
      LocalObject(
        856,
        Terminal.Constructor(Vector3(6596.003f, 4438.907f, 45.78771f), implant_terminal_interface),
        owning_building_guid = 36
      )
      TerminalToInterface(864, 856)
      LocalObject(
        865,
        ImplantTerminalMech.Constructor(Vector3(6609.551f, 4431.717f, 45.78771f)),
        owning_building_guid = 36
      )
      LocalObject(
        857,
        Terminal.Constructor(Vector3(6609.535f, 4431.726f, 45.78771f), implant_terminal_interface),
        owning_building_guid = 36
      )
      TerminalToInterface(865, 857)
      LocalObject(
        2067,
        Painbox.Constructor(Vector3(6663.99f, 4397.706f, 70.33951f), painbox),
        owning_building_guid = 36
      )
      LocalObject(
        2079,
        Painbox.Constructor(Vector3(6644.117f, 4418.646f, 50.38061f), painbox_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        2091,
        Painbox.Constructor(Vector3(6650.557f, 4403.25f, 70.54461f), painbox_door_radius),
        owning_building_guid = 36
      )
      LocalObject(
        2125,
        Painbox.Constructor(Vector3(6627.068f, 4425.178f, 48.02491f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        2126,
        Painbox.Constructor(Vector3(6640.27f, 4435.562f, 49.85161f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        2127,
        Painbox.Constructor(Vector3(6650.896f, 4410.27f, 48.66661f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(283, Generator.Constructor(Vector3(6666.688f, 4394.496f, 65.01671f)), owning_building_guid = 36)
      LocalObject(
        271,
        Terminal.Constructor(Vector3(6659.432f, 4398.3f, 66.31071f), gen_control),
        owning_building_guid = 36
      )
    }

    Building20900()

    def Building20900(): Unit = { // Name: GW_Amerish_N Type: hst GUID: 40, MapID: 20900
      LocalBuilding(
        "GW_Amerish_N",
        40,
        20900,
        FoundationBuilder(WarpGate.Structure(Vector3(3570.37f, 6924.31f, 40.74f), hst))
      )
    }

    Building20902()

    def Building20902(): Unit = { // Name: GW_Amerish_S Type: hst GUID: 41, MapID: 20902
      LocalBuilding(
        "GW_Amerish_S",
        41,
        20902,
        FoundationBuilder(WarpGate.Structure(Vector3(3723.31f, 3311.24f, 41.48f), hst))
      )
    }

    Building7()

    def Building7(): Unit = { // Name: Heyoka Type: tech_plant GUID: 43, MapID: 7
      LocalBuilding(
        "Heyoka",
        43,
        7,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4298f, 2404f, 47.49414f),
            Vector3(0f, 0f, 360f),
            tech_plant
          )
        )
      )
      LocalObject(
        195,
        CaptureTerminal.Constructor(Vector3(4302.734f, 2359.911f, 62.59414f), capture_terminal),
        owning_building_guid = 43
      )
      LocalObject(379, Door.Constructor(Vector3(4226.54f, 2333.929f, 49.03614f)), owning_building_guid = 43)
      LocalObject(380, Door.Constructor(Vector3(4226.54f, 2352.121f, 56.99914f)), owning_building_guid = 43)
      LocalObject(381, Door.Constructor(Vector3(4226.54f, 2407.673f, 49.03614f)), owning_building_guid = 43)
      LocalObject(382, Door.Constructor(Vector3(4226.54f, 2425.865f, 56.99914f)), owning_building_guid = 43)
      LocalObject(383, Door.Constructor(Vector3(4258f, 2304f, 49.11514f)), owning_building_guid = 43)
      LocalObject(384, Door.Constructor(Vector3(4284.412f, 2312.802f, 57.10814f)), owning_building_guid = 43)
      LocalObject(386, Door.Constructor(Vector3(4302.605f, 2312.802f, 49.14514f)), owning_building_guid = 43)
      LocalObject(387, Door.Constructor(Vector3(4308.597f, 2380.575f, 64.11514f)), owning_building_guid = 43)
      LocalObject(388, Door.Constructor(Vector3(4315.444f, 2364.574f, 64.11514f)), owning_building_guid = 43)
      LocalObject(389, Door.Constructor(Vector3(4322.559f, 2435.266f, 49.03614f)), owning_building_guid = 43)
      LocalObject(390, Door.Constructor(Vector3(4340.752f, 2435.266f, 56.99914f)), owning_building_guid = 43)
      LocalObject(391, Door.Constructor(Vector3(4377.02f, 2388.914f, 56.99914f)), owning_building_guid = 43)
      LocalObject(392, Door.Constructor(Vector3(4377.02f, 2407.107f, 49.03614f)), owning_building_guid = 43)
      LocalObject(562, Door.Constructor(Vector3(4250f, 2444.002f, 51.23114f)), owning_building_guid = 43)
      LocalObject(565, Door.Constructor(Vector3(4250f, 2388f, 31.23114f)), owning_building_guid = 43)
      LocalObject(662, Door.Constructor(Vector3(4234f, 2336f, 41.61514f)), owning_building_guid = 43)
      LocalObject(663, Door.Constructor(Vector3(4234f, 2360f, 39.11514f)), owning_building_guid = 43)
      LocalObject(664, Door.Constructor(Vector3(4266f, 2360f, 39.11514f)), owning_building_guid = 43)
      LocalObject(665, Door.Constructor(Vector3(4266f, 2384f, 39.11514f)), owning_building_guid = 43)
      LocalObject(666, Door.Constructor(Vector3(4282f, 2312f, 41.61514f)), owning_building_guid = 43)
      LocalObject(667, Door.Constructor(Vector3(4282f, 2352f, 41.61514f)), owning_building_guid = 43)
      LocalObject(668, Door.Constructor(Vector3(4294f, 2364f, 44.11514f)), owning_building_guid = 43)
      LocalObject(669, Door.Constructor(Vector3(4294f, 2364f, 64.11514f)), owning_building_guid = 43)
      LocalObject(670, Door.Constructor(Vector3(4294f, 2380f, 44.11514f)), owning_building_guid = 43)
      LocalObject(671, Door.Constructor(Vector3(4298f, 2328f, 41.61514f)), owning_building_guid = 43)
      LocalObject(672, Door.Constructor(Vector3(4298f, 2368f, 34.11514f)), owning_building_guid = 43)
      LocalObject(673, Door.Constructor(Vector3(4298f, 2372f, 54.11514f)), owning_building_guid = 43)
      LocalObject(674, Door.Constructor(Vector3(4298f, 2376f, 64.11514f)), owning_building_guid = 43)
      LocalObject(675, Door.Constructor(Vector3(4318f, 2356f, 34.11514f)), owning_building_guid = 43)
      LocalObject(676, Door.Constructor(Vector3(4318f, 2356f, 41.61514f)), owning_building_guid = 43)
      LocalObject(677, Door.Constructor(Vector3(4318f, 2380f, 41.61514f)), owning_building_guid = 43)
      LocalObject(678, Door.Constructor(Vector3(4326f, 2388f, 34.11514f)), owning_building_guid = 43)
      LocalObject(679, Door.Constructor(Vector3(4330f, 2360f, 34.11514f)), owning_building_guid = 43)
      LocalObject(680, Door.Constructor(Vector3(4330f, 2376f, 41.61514f)), owning_building_guid = 43)
      LocalObject(834, Door.Constructor(Vector3(4338.213f, 2358.341f, 49.87414f)), owning_building_guid = 43)
      LocalObject(2524, Door.Constructor(Vector3(4310.673f, 2361.733f, 41.94814f)), owning_building_guid = 43)
      LocalObject(2525, Door.Constructor(Vector3(4310.673f, 2369.026f, 41.94814f)), owning_building_guid = 43)
      LocalObject(2526, Door.Constructor(Vector3(4310.673f, 2376.315f, 41.94814f)), owning_building_guid = 43)
      LocalObject(
        882,
        IFFLock.Constructor(Vector3(4341.357f, 2355.603f, 49.07414f), Vector3(0, 0, 180)),
        owning_building_guid = 43,
        door_guid = 834
      )
      LocalObject(
        890,
        IFFLock.Constructor(Vector3(4255.256f, 2446.353f, 49.18214f), Vector3(0, 0, 360)),
        owning_building_guid = 43,
        door_guid = 562
      )
      LocalObject(
        975,
        IFFLock.Constructor(Vector3(4257.186f, 2301.954f, 49.04614f), Vector3(0, 0, 270)),
        owning_building_guid = 43,
        door_guid = 383
      )
      LocalObject(
        976,
        IFFLock.Constructor(Vector3(4292.428f, 2364.94f, 63.93014f), Vector3(0, 0, 0)),
        owning_building_guid = 43,
        door_guid = 669
      )
      LocalObject(
        977,
        IFFLock.Constructor(Vector3(4306.554f, 2381.383f, 64.04014f), Vector3(0, 0, 0)),
        owning_building_guid = 43,
        door_guid = 387
      )
      LocalObject(
        978,
        IFFLock.Constructor(Vector3(4316.428f, 2380.81f, 41.43014f), Vector3(0, 0, 0)),
        owning_building_guid = 43,
        door_guid = 677
      )
      LocalObject(
        979,
        IFFLock.Constructor(Vector3(4317.496f, 2363.775f, 64.04014f), Vector3(0, 0, 180)),
        owning_building_guid = 43,
        door_guid = 388
      )
      LocalObject(
        980,
        IFFLock.Constructor(Vector3(4319.572f, 2355.19f, 41.43014f), Vector3(0, 0, 180)),
        owning_building_guid = 43,
        door_guid = 676
      )
      LocalObject(
        981,
        IFFLock.Constructor(Vector3(4327.572f, 2387.057f, 33.93014f), Vector3(0, 0, 180)),
        owning_building_guid = 43,
        door_guid = 678
      )
      LocalObject(
        982,
        IFFLock.Constructor(Vector3(4329.06f, 2358.428f, 33.93014f), Vector3(0, 0, 270)),
        owning_building_guid = 43,
        door_guid = 679
      )
      LocalObject(1257, Locker.Constructor(Vector3(4321.563f, 2358.141f, 40.35514f)), owning_building_guid = 43)
      LocalObject(1258, Locker.Constructor(Vector3(4322.727f, 2358.141f, 40.35514f)), owning_building_guid = 43)
      LocalObject(1259, Locker.Constructor(Vector3(4323.874f, 2358.141f, 40.35514f)), owning_building_guid = 43)
      LocalObject(1260, Locker.Constructor(Vector3(4325.023f, 2358.141f, 40.35514f)), owning_building_guid = 43)
      LocalObject(1261, Locker.Constructor(Vector3(4332.194f, 2378.165f, 32.59414f)), owning_building_guid = 43)
      LocalObject(1262, Locker.Constructor(Vector3(4333.518f, 2378.165f, 32.59414f)), owning_building_guid = 43)
      LocalObject(1263, Locker.Constructor(Vector3(4334.854f, 2378.165f, 32.59414f)), owning_building_guid = 43)
      LocalObject(1264, Locker.Constructor(Vector3(4336.191f, 2378.165f, 32.59414f)), owning_building_guid = 43)
      LocalObject(1265, Locker.Constructor(Vector3(4340.731f, 2378.165f, 32.59414f)), owning_building_guid = 43)
      LocalObject(1266, Locker.Constructor(Vector3(4342.055f, 2378.165f, 32.59414f)), owning_building_guid = 43)
      LocalObject(1267, Locker.Constructor(Vector3(4343.391f, 2378.165f, 32.59414f)), owning_building_guid = 43)
      LocalObject(1268, Locker.Constructor(Vector3(4344.728f, 2378.165f, 32.59414f)), owning_building_guid = 43)
      LocalObject(
        145,
        Terminal.Constructor(Vector3(4302.673f, 2379.141f, 63.19714f), air_vehicle_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1628,
        VehicleSpawnPad.Constructor(Vector3(4298.07f, 2399.835f, 60.07214f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 43,
        terminal_guid = 145
      )
      LocalObject(
        146,
        Terminal.Constructor(Vector3(4314.605f, 2379.141f, 63.19714f), air_vehicle_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1629,
        VehicleSpawnPad.Constructor(Vector3(4319.088f, 2399.835f, 60.07214f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 43,
        terminal_guid = 146
      )
      LocalObject(
        1713,
        Terminal.Constructor(Vector3(4301.058f, 2377.486f, 53.92414f), order_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1714,
        Terminal.Constructor(Vector3(4324.654f, 2363.408f, 41.68414f), order_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1715,
        Terminal.Constructor(Vector3(4324.654f, 2367.139f, 41.68414f), order_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1716,
        Terminal.Constructor(Vector3(4324.654f, 2370.928f, 41.68414f), order_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2436,
        Terminal.Constructor(Vector3(4273.942f, 2308.591f, 41.65114f), spawn_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2437,
        Terminal.Constructor(Vector3(4305.942f, 2356.591f, 34.15114f), spawn_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2438,
        Terminal.Constructor(Vector3(4310.971f, 2359.243f, 42.22814f), spawn_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2439,
        Terminal.Constructor(Vector3(4310.967f, 2366.535f, 42.22814f), spawn_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2440,
        Terminal.Constructor(Vector3(4310.97f, 2373.823f, 42.22814f), spawn_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2441,
        Terminal.Constructor(Vector3(4312.532f, 2407.215f, 59.51714f), spawn_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2442,
        Terminal.Constructor(Vector3(4335.242f, 2383.639f, 54.17614f), spawn_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2650,
        Terminal.Constructor(Vector3(4249.996f, 2363.423f, 33.30814f), ground_vehicle_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1627,
        VehicleSpawnPad.Constructor(Vector3(4249.945f, 2374.339f, 25.03114f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 43,
        terminal_guid = 2650
      )
      LocalObject(2297, ResourceSilo.Constructor(Vector3(4359.752f, 2436.555f, 54.50214f)), owning_building_guid = 43)
      LocalObject(
        2337,
        SpawnTube.Constructor(Vector3(4310.233f, 2360.683f, 40.09414f), Vector3(0, 0, 0)),
        owning_building_guid = 43
      )
      LocalObject(
        2338,
        SpawnTube.Constructor(Vector3(4310.233f, 2367.974f, 40.09414f), Vector3(0, 0, 0)),
        owning_building_guid = 43
      )
      LocalObject(
        2339,
        SpawnTube.Constructor(Vector3(4310.233f, 2375.262f, 40.09414f), Vector3(0, 0, 0)),
        owning_building_guid = 43
      )
      LocalObject(
        1647,
        ProximityTerminal.Constructor(Vector3(4301.059f, 2366.901f, 52.59114f), medical_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1648,
        ProximityTerminal.Constructor(Vector3(4338.444f, 2377.62f, 32.59414f), medical_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1888,
        ProximityTerminal.Constructor(Vector3(4236.704f, 2379.661f, 55.68914f), pad_landing_frame),
        owning_building_guid = 43
      )
      LocalObject(
        1889,
        Terminal.Constructor(Vector3(4236.704f, 2379.661f, 55.68914f), air_rearm_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1891,
        ProximityTerminal.Constructor(Vector3(4256.98f, 2395.833f, 58.04414f), pad_landing_frame),
        owning_building_guid = 43
      )
      LocalObject(
        1892,
        Terminal.Constructor(Vector3(4256.98f, 2395.833f, 58.04414f), air_rearm_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1894,
        ProximityTerminal.Constructor(Vector3(4307.379f, 2339.474f, 62.94114f), pad_landing_frame),
        owning_building_guid = 43
      )
      LocalObject(
        1895,
        Terminal.Constructor(Vector3(4307.379f, 2339.474f, 62.94114f), air_rearm_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1897,
        ProximityTerminal.Constructor(Vector3(4323.534f, 2322.628f, 55.70214f), pad_landing_frame),
        owning_building_guid = 43
      )
      LocalObject(
        1898,
        Terminal.Constructor(Vector3(4323.534f, 2322.628f, 55.70214f), air_rearm_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1900,
        ProximityTerminal.Constructor(Vector3(4359.987f, 2361.855f, 58.14614f), pad_landing_frame),
        owning_building_guid = 43
      )
      LocalObject(
        1901,
        Terminal.Constructor(Vector3(4359.987f, 2361.855f, 58.14614f), air_rearm_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1903,
        ProximityTerminal.Constructor(Vector3(4366.28f, 2378.102f, 55.70214f), pad_landing_frame),
        owning_building_guid = 43
      )
      LocalObject(
        1904,
        Terminal.Constructor(Vector3(4366.28f, 2378.102f, 55.70214f), air_rearm_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2229,
        ProximityTerminal.Constructor(Vector3(4298.309f, 2445.637f, 47.22264f), repair_silo),
        owning_building_guid = 43
      )
      LocalObject(
        2230,
        Terminal.Constructor(Vector3(4298.309f, 2445.637f, 47.22264f), ground_rearm_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        2233,
        ProximityTerminal.Constructor(Vector3(4356.637f, 2311.208f, 47.24414f), repair_silo),
        owning_building_guid = 43
      )
      LocalObject(
        2234,
        Terminal.Constructor(Vector3(4356.637f, 2311.208f, 47.24414f), ground_rearm_terminal),
        owning_building_guid = 43
      )
      LocalObject(
        1557,
        FacilityTurret.Constructor(Vector3(4213.906f, 2502.855f, 55.99314f), manned_turret),
        owning_building_guid = 43
      )
      TurretToWeapon(1557, 5052)
      LocalObject(
        1558,
        FacilityTurret.Constructor(Vector3(4219.413f, 2304.665f, 55.99314f), manned_turret),
        owning_building_guid = 43
      )
      TurretToWeapon(1558, 5053)
      LocalObject(
        1559,
        FacilityTurret.Constructor(Vector3(4303.601f, 2502.855f, 55.99314f), manned_turret),
        owning_building_guid = 43
      )
      TurretToWeapon(1559, 5054)
      LocalObject(
        1560,
        FacilityTurret.Constructor(Vector3(4384.154f, 2304.657f, 55.99314f), manned_turret),
        owning_building_guid = 43
      )
      TurretToWeapon(1560, 5055)
      LocalObject(
        1561,
        FacilityTurret.Constructor(Vector3(4384.154f, 2442.398f, 55.99314f), manned_turret),
        owning_building_guid = 43
      )
      TurretToWeapon(1561, 5056)
      LocalObject(
        1562,
        FacilityTurret.Constructor(Vector3(4391.881f, 2369.423f, 55.99314f), manned_turret),
        owning_building_guid = 43
      )
      TurretToWeapon(1562, 5057)
      LocalObject(
        2060,
        Painbox.Constructor(Vector3(4323.737f, 2400.206f, 36.06744f), painbox),
        owning_building_guid = 43
      )
      LocalObject(
        2072,
        Painbox.Constructor(Vector3(4318.832f, 2367.212f, 44.36404f), painbox_continuous),
        owning_building_guid = 43
      )
      LocalObject(
        2084,
        Painbox.Constructor(Vector3(4325.7f, 2385.471f, 35.75354f), painbox_door_radius),
        owning_building_guid = 43
      )
      LocalObject(
        2104,
        Painbox.Constructor(Vector3(4317.035f, 2354.278f, 42.22204f), painbox_door_radius_continuous),
        owning_building_guid = 43
      )
      LocalObject(
        2105,
        Painbox.Constructor(Vector3(4317.861f, 2381.769f, 42.77034f), painbox_door_radius_continuous),
        owning_building_guid = 43
      )
      LocalObject(
        2106,
        Painbox.Constructor(Vector3(4333.641f, 2374.57f, 43.67644f), painbox_door_radius_continuous),
        owning_building_guid = 43
      )
      LocalObject(276, Generator.Constructor(Vector3(4325.975f, 2403.555f, 31.30014f)), owning_building_guid = 43)
      LocalObject(
        264,
        Terminal.Constructor(Vector3(4326.022f, 2395.363f, 32.59414f), gen_control),
        owning_building_guid = 43
      )
    }

    Building14()

    def Building14(): Unit = { // Name: Tumas Type: tech_plant GUID: 46, MapID: 14
      LocalBuilding(
        "Tumas",
        46,
        14,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(4610f, 6292f, 69.42805f), Vector3(0f, 0f, 89f), tech_plant)
        )
      )
      LocalObject(
        196,
        CaptureTerminal.Constructor(Vector3(4654.165f, 6295.964f, 84.52805f), capture_terminal),
        owning_building_guid = 46
      )
      LocalObject(402, Door.Constructor(Vector3(4579.167f, 6317.101f, 70.97005f)), owning_building_guid = 46)
      LocalObject(403, Door.Constructor(Vector3(4579.485f, 6335.291f, 78.93304f)), owning_building_guid = 46)
      LocalObject(404, Door.Constructor(Vector3(4586.891f, 6220.933f, 78.93304f)), owning_building_guid = 46)
      LocalObject(408, Door.Constructor(Vector3(4605.081f, 6220.615f, 70.97005f)), owning_building_guid = 46)
      LocalObject(409, Door.Constructor(Vector3(4608.272f, 6371.062f, 70.97005f)), owning_building_guid = 46)
      LocalObject(411, Door.Constructor(Vector3(4626.463f, 6370.745f, 78.93304f)), owning_building_guid = 46)
      LocalObject(412, Door.Constructor(Vector3(4633.606f, 6302.187f, 86.04905f)), owning_building_guid = 46)
      LocalObject(414, Door.Constructor(Vector3(4649.725f, 6308.753f, 86.04905f)), owning_building_guid = 46)
      LocalObject(418, Door.Constructor(Vector3(4660.624f, 6219.646f, 78.93304f)), owning_building_guid = 46)
      LocalObject(419, Door.Constructor(Vector3(4678.813f, 6219.328f, 70.97005f)), owning_building_guid = 46)
      LocalObject(420, Door.Constructor(Vector3(4700.947f, 6276.822f, 79.04205f)), owning_building_guid = 46)
      LocalObject(421, Door.Constructor(Vector3(4701.265f, 6295.013f, 71.07905f)), owning_building_guid = 46)
      LocalObject(422, Door.Constructor(Vector3(4709.287f, 6250.261f, 71.04905f)), owning_building_guid = 46)
      LocalObject(563, Door.Constructor(Vector3(4569.167f, 6244.706f, 73.16505f)), owning_building_guid = 46)
      LocalObject(566, Door.Constructor(Vector3(4625.16f, 6243.728f, 53.16505f)), owning_building_guid = 46)
      LocalObject(681, Door.Constructor(Vector3(4626.486f, 6319.716f, 56.04905f)), owning_building_guid = 46)
      LocalObject(682, Door.Constructor(Vector3(4629.438f, 6259.656f, 61.04905f)), owning_building_guid = 46)
      LocalObject(683, Door.Constructor(Vector3(4633.927f, 6287.582f, 66.04905f)), owning_building_guid = 46)
      LocalObject(684, Door.Constructor(Vector3(4634.345f, 6311.578f, 63.54905f)), owning_building_guid = 46)
      LocalObject(685, Door.Constructor(Vector3(4637.996f, 6291.511f, 86.04905f)), owning_building_guid = 46)
      LocalObject(686, Door.Constructor(Vector3(4638.554f, 6323.506f, 63.54905f)), owning_building_guid = 46)
      LocalObject(687, Door.Constructor(Vector3(4641.995f, 6291.441f, 76.04905f)), owning_building_guid = 46)
      LocalObject(688, Door.Constructor(Vector3(4645.995f, 6291.372f, 56.04905f)), owning_building_guid = 46)
      LocalObject(689, Door.Constructor(Vector3(4649.924f, 6287.303f, 66.04905f)), owning_building_guid = 46)
      LocalObject(690, Door.Constructor(Vector3(4649.924f, 6287.303f, 86.04905f)), owning_building_guid = 46)
      LocalObject(691, Door.Constructor(Vector3(4652.876f, 6227.242f, 61.04905f)), owning_building_guid = 46)
      LocalObject(692, Door.Constructor(Vector3(4653.435f, 6259.237f, 61.04905f)), owning_building_guid = 46)
      LocalObject(693, Door.Constructor(Vector3(4654.552f, 6323.227f, 56.04905f)), owning_building_guid = 46)
      LocalObject(696, Door.Constructor(Vector3(4658.342f, 6311.159f, 56.04905f)), owning_building_guid = 46)
      LocalObject(697, Door.Constructor(Vector3(4658.342f, 6311.159f, 63.54905f)), owning_building_guid = 46)
      LocalObject(701, Door.Constructor(Vector3(4661.713f, 6275.095f, 63.54905f)), owning_building_guid = 46)
      LocalObject(705, Door.Constructor(Vector3(4676.873f, 6226.823f, 63.54905f)), owning_building_guid = 46)
      LocalObject(710, Door.Constructor(Vector3(4685.988f, 6290.674f, 63.54905f)), owning_building_guid = 46)
      LocalObject(717, Door.Constructor(Vector3(4701.707f, 6274.397f, 63.54905f)), owning_building_guid = 46)
      LocalObject(835, Door.Constructor(Vector3(4656.354f, 6331.41f, 71.80804f)), owning_building_guid = 46)
      LocalObject(2529, Door.Constructor(Vector3(4637.902f, 6304.188f, 63.88205f)), owning_building_guid = 46)
      LocalObject(2530, Door.Constructor(Vector3(4645.19f, 6304.061f, 63.88205f)), owning_building_guid = 46)
      LocalObject(2531, Door.Constructor(Vector3(4652.482f, 6303.934f, 63.88205f)), owning_building_guid = 46)
      LocalObject(
        883,
        IFFLock.Constructor(Vector3(4659.146f, 6334.506f, 71.00805f), Vector3(0, 0, 91)),
        owning_building_guid = 46,
        door_guid = 835
      )
      LocalObject(
        891,
        IFFLock.Constructor(Vector3(4566.907f, 6250.001f, 71.11605f), Vector3(0, 0, 271)),
        owning_building_guid = 46,
        door_guid = 563
      )
      LocalObject(
        990,
        IFFLock.Constructor(Vector3(4627.457f, 6321.272f, 55.86405f), Vector3(0, 0, 91)),
        owning_building_guid = 46,
        door_guid = 681
      )
      LocalObject(
        991,
        IFFLock.Constructor(Vector3(4632.763f, 6300.158f, 85.97404f), Vector3(0, 0, 271)),
        owning_building_guid = 46,
        door_guid = 412
      )
      LocalObject(
        992,
        IFFLock.Constructor(Vector3(4633.508f, 6310.021f, 63.36405f), Vector3(0, 0, 271)),
        owning_building_guid = 46,
        door_guid = 684
      )
      LocalObject(
        993,
        IFFLock.Constructor(Vector3(4648.957f, 6285.747f, 85.86404f), Vector3(0, 0, 271)),
        owning_building_guid = 46,
        door_guid = 690
      )
      LocalObject(
        995,
        IFFLock.Constructor(Vector3(4650.559f, 6310.791f, 85.97404f), Vector3(0, 0, 91)),
        owning_building_guid = 46,
        door_guid = 414
      )
      LocalObject(
        997,
        IFFLock.Constructor(Vector3(4656.107f, 6322.26f, 55.86405f), Vector3(0, 0, 181)),
        owning_building_guid = 46,
        door_guid = 693
      )
      LocalObject(
        999,
        IFFLock.Constructor(Vector3(4659.179f, 6312.717f, 63.36405f), Vector3(0, 0, 91)),
        owning_building_guid = 46,
        door_guid = 697
      )
      LocalObject(
        1004,
        IFFLock.Constructor(Vector3(4711.318f, 6249.411f, 70.98005f), Vector3(0, 0, 181)),
        owning_building_guid = 46,
        door_guid = 422
      )
      LocalObject(1277, Locker.Constructor(Vector3(4636.428f, 6325.738f, 54.52805f)), owning_building_guid = 46)
      LocalObject(1278, Locker.Constructor(Vector3(4636.451f, 6327.062f, 54.52805f)), owning_building_guid = 46)
      LocalObject(1279, Locker.Constructor(Vector3(4636.474f, 6328.397f, 54.52805f)), owning_building_guid = 46)
      LocalObject(1280, Locker.Constructor(Vector3(4636.498f, 6329.734f, 54.52805f)), owning_building_guid = 46)
      LocalObject(1281, Locker.Constructor(Vector3(4636.577f, 6334.273f, 54.52805f)), owning_building_guid = 46)
      LocalObject(1282, Locker.Constructor(Vector3(4636.6f, 6335.597f, 54.52805f)), owning_building_guid = 46)
      LocalObject(1283, Locker.Constructor(Vector3(4636.623f, 6336.933f, 54.52805f)), owning_building_guid = 46)
      LocalObject(1284, Locker.Constructor(Vector3(4636.646f, 6338.27f, 54.52805f)), owning_building_guid = 46)
      LocalObject(1285, Locker.Constructor(Vector3(4656.263f, 6314.759f, 62.28905f)), owning_building_guid = 46)
      LocalObject(1286, Locker.Constructor(Vector3(4656.284f, 6315.923f, 62.28905f)), owning_building_guid = 46)
      LocalObject(1287, Locker.Constructor(Vector3(4656.304f, 6317.07f, 62.28905f)), owning_building_guid = 46)
      LocalObject(1288, Locker.Constructor(Vector3(4656.324f, 6318.219f, 62.28905f)), owning_building_guid = 46)
      LocalObject(
        147,
        Terminal.Constructor(Vector3(4634.937f, 6296.238f, 85.13105f), air_vehicle_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1630,
        VehicleSpawnPad.Constructor(Vector3(4614.166f, 6291.997f, 82.00605f), mb_pad_creation, Vector3(0, 0, -89)),
        owning_building_guid = 46,
        terminal_guid = 147
      )
      LocalObject(
        148,
        Terminal.Constructor(Vector3(4635.145f, 6308.168f, 85.13105f), air_vehicle_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1631,
        VehicleSpawnPad.Constructor(Vector3(4614.532f, 6313.012f, 82.00605f), mb_pad_creation, Vector3(0, 0, -89)),
        owning_building_guid = 46,
        terminal_guid = 148
      )
      LocalObject(
        1720,
        Terminal.Constructor(Vector3(4636.563f, 6294.595f, 75.85805f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1721,
        Terminal.Constructor(Vector3(4643.532f, 6318.073f, 63.61805f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1722,
        Terminal.Constructor(Vector3(4647.321f, 6318.007f, 63.61805f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1723,
        Terminal.Constructor(Vector3(4651.051f, 6317.941f, 63.61805f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2443,
        Terminal.Constructor(Vector3(4607.039f, 6306.586f, 81.45105f), spawn_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2444,
        Terminal.Constructor(Vector3(4631.008f, 6328.881f, 76.11005f), spawn_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2445,
        Terminal.Constructor(Vector3(4640.399f, 6304.441f, 64.16205f), spawn_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2447,
        Terminal.Constructor(Vector3(4647.686f, 6304.311f, 64.16205f), spawn_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2448,
        Terminal.Constructor(Vector3(4654.977f, 6304.188f, 64.16205f), spawn_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2449,
        Terminal.Constructor(Vector3(4657.541f, 6299.113f, 56.08504f), spawn_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2455,
        Terminal.Constructor(Vector3(4704.975f, 6266.281f, 63.58505f), spawn_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2652,
        Terminal.Constructor(Vector3(4649.733f, 6243.295f, 55.24205f), ground_vehicle_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1633,
        VehicleSpawnPad.Constructor(Vector3(4638.818f, 6243.435f, 46.96505f), mb_pad_creation, Vector3(0, 0, -89)),
        owning_building_guid = 46,
        terminal_guid = 2652
      )
      LocalObject(2298, ResourceSilo.Constructor(Vector3(4578.528f, 6354.311f, 76.43605f)), owning_building_guid = 46)
      LocalObject(
        2342,
        SpawnTube.Constructor(Vector3(4638.947f, 6303.729f, 62.02805f), Vector3(0, 0, 271)),
        owning_building_guid = 46
      )
      LocalObject(
        2343,
        SpawnTube.Constructor(Vector3(4646.234f, 6303.603f, 62.02805f), Vector3(0, 0, 271)),
        owning_building_guid = 46
      )
      LocalObject(
        2344,
        SpawnTube.Constructor(Vector3(4653.524f, 6303.475f, 62.02805f), Vector3(0, 0, 271)),
        owning_building_guid = 46
      )
      LocalObject(
        1649,
        ProximityTerminal.Constructor(Vector3(4637.082f, 6331.978f, 54.52805f), medical_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1650,
        ProximityTerminal.Constructor(Vector3(4647.147f, 6294.411f, 74.52505f), medical_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1906,
        ProximityTerminal.Constructor(Vector3(4617.45f, 6250.844f, 79.97805f), pad_landing_frame),
        owning_building_guid = 46
      )
      LocalObject(
        1907,
        Terminal.Constructor(Vector3(4617.45f, 6250.844f, 79.97805f), air_rearm_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1912,
        ProximityTerminal.Constructor(Vector3(4633.266f, 6230.289f, 77.62305f), pad_landing_frame),
        owning_building_guid = 46
      )
      LocalObject(
        1913,
        Terminal.Constructor(Vector3(4633.266f, 6230.289f, 77.62305f), air_rearm_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1915,
        ProximityTerminal.Constructor(Vector3(4637.086f, 6359.817f, 77.63605f), pad_landing_frame),
        owning_building_guid = 46
      )
      LocalObject(
        1916,
        Terminal.Constructor(Vector3(4637.086f, 6359.817f, 77.63605f), air_rearm_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1918,
        ProximityTerminal.Constructor(Vector3(4653.22f, 6353.242f, 80.08005f), pad_landing_frame),
        owning_building_guid = 46
      )
      LocalObject(
        1919,
        Terminal.Constructor(Vector3(4653.22f, 6353.242f, 80.08005f), air_rearm_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1924,
        ProximityTerminal.Constructor(Vector3(4674.68f, 6300.251f, 84.87505f), pad_landing_frame),
        owning_building_guid = 46
      )
      LocalObject(
        1925,
        Terminal.Constructor(Vector3(4674.68f, 6300.251f, 84.87505f), air_rearm_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1927,
        ProximityTerminal.Constructor(Vector3(4691.805f, 6316.11f, 77.63605f), pad_landing_frame),
        owning_building_guid = 46
      )
      LocalObject(
        1928,
        Terminal.Constructor(Vector3(4691.805f, 6316.11f, 77.63605f), air_rearm_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2237,
        ProximityTerminal.Constructor(Vector3(4568.375f, 6293.035f, 69.15655f), repair_silo),
        owning_building_guid = 46
      )
      LocalObject(
        2238,
        Terminal.Constructor(Vector3(4568.375f, 6293.035f, 69.15655f), ground_rearm_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        2249,
        ProximityTerminal.Constructor(Vector3(4703.802f, 6349.008f, 69.17805f), repair_silo),
        owning_building_guid = 46
      )
      LocalObject(
        2250,
        Terminal.Constructor(Vector3(4703.802f, 6349.008f, 69.17805f), ground_rearm_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1563,
        FacilityTurret.Constructor(Vector3(4509.692f, 6209.644f, 77.92705f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1563, 5058)
      LocalObject(
        1564,
        FacilityTurret.Constructor(Vector3(4511.258f, 6299.325f, 77.92705f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1564, 5059)
      LocalObject(
        1565,
        FacilityTurret.Constructor(Vector3(4573.111f, 6378.811f, 77.92705f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1565, 5060)
      LocalObject(
        1569,
        FacilityTurret.Constructor(Vector3(4646.21f, 6385.263f, 77.92705f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1569, 5061)
      LocalObject(
        1570,
        FacilityTurret.Constructor(Vector3(4707.948f, 6211.691f, 77.92705f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1570, 5062)
      LocalObject(
        1571,
        FacilityTurret.Constructor(Vector3(4710.832f, 6376.407f, 77.92705f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1571, 5063)
      LocalObject(
        2061,
        Painbox.Constructor(Vector3(4614.242f, 6317.667f, 58.00135f), painbox),
        owning_building_guid = 46
      )
      LocalObject(
        2073,
        Painbox.Constructor(Vector3(4647.146f, 6312.187f, 66.29795f), painbox_continuous),
        owning_building_guid = 46
      )
      LocalObject(
        2085,
        Painbox.Constructor(Vector3(4629.009f, 6319.372f, 57.68745f), painbox_door_radius),
        owning_building_guid = 46
      )
      LocalObject(
        2107,
        Painbox.Constructor(Vector3(4632.574f, 6311.47f, 64.70425f), painbox_door_radius_continuous),
        owning_building_guid = 46
      )
      LocalObject(
        2108,
        Painbox.Constructor(Vector3(4640.048f, 6327.122f, 65.61034f), painbox_door_radius_continuous),
        owning_building_guid = 46
      )
      LocalObject(
        2109,
        Painbox.Constructor(Vector3(4660.046f, 6310.164f, 64.15594f), painbox_door_radius_continuous),
        owning_building_guid = 46
      )
      LocalObject(277, Generator.Constructor(Vector3(4610.933f, 6319.963f, 53.23405f)), owning_building_guid = 46)
      LocalObject(
        265,
        Terminal.Constructor(Vector3(4619.125f, 6319.867f, 54.52805f), gen_control),
        owning_building_guid = 46
      )
    }

    Building10()

    def Building10(): Unit = { // Name: Mekala Type: tech_plant GUID: 49, MapID: 10
      LocalBuilding(
        "Mekala",
        49,
        10,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5986f, 3002f, 58.83252f),
            Vector3(0f, 0f, 360f),
            tech_plant
          )
        )
      )
      LocalObject(
        200,
        CaptureTerminal.Constructor(Vector3(5990.734f, 2957.911f, 73.93253f), capture_terminal),
        owning_building_guid = 49
      )
      LocalObject(499, Door.Constructor(Vector3(5914.54f, 2931.929f, 60.37452f)), owning_building_guid = 49)
      LocalObject(500, Door.Constructor(Vector3(5914.54f, 2950.121f, 68.33752f)), owning_building_guid = 49)
      LocalObject(501, Door.Constructor(Vector3(5914.54f, 3005.673f, 60.37452f)), owning_building_guid = 49)
      LocalObject(502, Door.Constructor(Vector3(5914.54f, 3023.865f, 68.33752f)), owning_building_guid = 49)
      LocalObject(503, Door.Constructor(Vector3(5946f, 2902f, 60.45352f)), owning_building_guid = 49)
      LocalObject(504, Door.Constructor(Vector3(5972.412f, 2910.802f, 68.44653f)), owning_building_guid = 49)
      LocalObject(505, Door.Constructor(Vector3(5990.605f, 2910.802f, 60.48352f)), owning_building_guid = 49)
      LocalObject(506, Door.Constructor(Vector3(5996.597f, 2978.575f, 75.45352f)), owning_building_guid = 49)
      LocalObject(507, Door.Constructor(Vector3(6003.444f, 2962.574f, 75.45352f)), owning_building_guid = 49)
      LocalObject(508, Door.Constructor(Vector3(6010.559f, 3033.266f, 60.37452f)), owning_building_guid = 49)
      LocalObject(509, Door.Constructor(Vector3(6028.752f, 3033.266f, 68.33752f)), owning_building_guid = 49)
      LocalObject(510, Door.Constructor(Vector3(6065.02f, 2986.914f, 68.33752f)), owning_building_guid = 49)
      LocalObject(511, Door.Constructor(Vector3(6065.02f, 3005.107f, 60.37452f)), owning_building_guid = 49)
      LocalObject(564, Door.Constructor(Vector3(5938f, 3042.002f, 62.56952f)), owning_building_guid = 49)
      LocalObject(567, Door.Constructor(Vector3(5938f, 2986f, 42.56952f)), owning_building_guid = 49)
      LocalObject(767, Door.Constructor(Vector3(5922f, 2934f, 52.95352f)), owning_building_guid = 49)
      LocalObject(768, Door.Constructor(Vector3(5922f, 2958f, 50.45352f)), owning_building_guid = 49)
      LocalObject(769, Door.Constructor(Vector3(5954f, 2958f, 50.45352f)), owning_building_guid = 49)
      LocalObject(770, Door.Constructor(Vector3(5954f, 2982f, 50.45352f)), owning_building_guid = 49)
      LocalObject(771, Door.Constructor(Vector3(5970f, 2910f, 52.95352f)), owning_building_guid = 49)
      LocalObject(772, Door.Constructor(Vector3(5970f, 2950f, 52.95352f)), owning_building_guid = 49)
      LocalObject(773, Door.Constructor(Vector3(5982f, 2962f, 55.45352f)), owning_building_guid = 49)
      LocalObject(774, Door.Constructor(Vector3(5982f, 2962f, 75.45352f)), owning_building_guid = 49)
      LocalObject(775, Door.Constructor(Vector3(5982f, 2978f, 55.45352f)), owning_building_guid = 49)
      LocalObject(776, Door.Constructor(Vector3(5986f, 2926f, 52.95352f)), owning_building_guid = 49)
      LocalObject(777, Door.Constructor(Vector3(5986f, 2966f, 45.45352f)), owning_building_guid = 49)
      LocalObject(778, Door.Constructor(Vector3(5986f, 2970f, 65.45352f)), owning_building_guid = 49)
      LocalObject(779, Door.Constructor(Vector3(5986f, 2974f, 75.45352f)), owning_building_guid = 49)
      LocalObject(780, Door.Constructor(Vector3(6006f, 2954f, 45.45352f)), owning_building_guid = 49)
      LocalObject(781, Door.Constructor(Vector3(6006f, 2954f, 52.95352f)), owning_building_guid = 49)
      LocalObject(782, Door.Constructor(Vector3(6006f, 2978f, 52.95352f)), owning_building_guid = 49)
      LocalObject(783, Door.Constructor(Vector3(6014f, 2986f, 45.45352f)), owning_building_guid = 49)
      LocalObject(784, Door.Constructor(Vector3(6018f, 2958f, 45.45352f)), owning_building_guid = 49)
      LocalObject(785, Door.Constructor(Vector3(6018f, 2974f, 52.95352f)), owning_building_guid = 49)
      LocalObject(839, Door.Constructor(Vector3(6026.213f, 2956.341f, 61.21252f)), owning_building_guid = 49)
      LocalObject(2557, Door.Constructor(Vector3(5998.673f, 2959.733f, 53.28652f)), owning_building_guid = 49)
      LocalObject(2558, Door.Constructor(Vector3(5998.673f, 2967.026f, 53.28652f)), owning_building_guid = 49)
      LocalObject(2559, Door.Constructor(Vector3(5998.673f, 2974.315f, 53.28652f)), owning_building_guid = 49)
      LocalObject(
        887,
        IFFLock.Constructor(Vector3(6029.357f, 2953.603f, 60.41253f), Vector3(0, 0, 180)),
        owning_building_guid = 49,
        door_guid = 839
      )
      LocalObject(
        892,
        IFFLock.Constructor(Vector3(5943.256f, 3044.353f, 60.52052f), Vector3(0, 0, 360)),
        owning_building_guid = 49,
        door_guid = 564
      )
      LocalObject(
        1065,
        IFFLock.Constructor(Vector3(5945.186f, 2899.954f, 60.38452f), Vector3(0, 0, 270)),
        owning_building_guid = 49,
        door_guid = 503
      )
      LocalObject(
        1066,
        IFFLock.Constructor(Vector3(5980.428f, 2962.94f, 75.26852f), Vector3(0, 0, 0)),
        owning_building_guid = 49,
        door_guid = 774
      )
      LocalObject(
        1067,
        IFFLock.Constructor(Vector3(5994.554f, 2979.383f, 75.37852f), Vector3(0, 0, 0)),
        owning_building_guid = 49,
        door_guid = 506
      )
      LocalObject(
        1068,
        IFFLock.Constructor(Vector3(6004.428f, 2978.81f, 52.76852f), Vector3(0, 0, 0)),
        owning_building_guid = 49,
        door_guid = 782
      )
      LocalObject(
        1069,
        IFFLock.Constructor(Vector3(6005.496f, 2961.775f, 75.37852f), Vector3(0, 0, 180)),
        owning_building_guid = 49,
        door_guid = 507
      )
      LocalObject(
        1070,
        IFFLock.Constructor(Vector3(6007.572f, 2953.19f, 52.76852f), Vector3(0, 0, 180)),
        owning_building_guid = 49,
        door_guid = 781
      )
      LocalObject(
        1071,
        IFFLock.Constructor(Vector3(6015.572f, 2985.057f, 45.26852f), Vector3(0, 0, 180)),
        owning_building_guid = 49,
        door_guid = 783
      )
      LocalObject(
        1072,
        IFFLock.Constructor(Vector3(6017.06f, 2956.428f, 45.26852f), Vector3(0, 0, 270)),
        owning_building_guid = 49,
        door_guid = 784
      )
      LocalObject(1398, Locker.Constructor(Vector3(6009.563f, 2956.141f, 51.69352f)), owning_building_guid = 49)
      LocalObject(1399, Locker.Constructor(Vector3(6010.727f, 2956.141f, 51.69352f)), owning_building_guid = 49)
      LocalObject(1400, Locker.Constructor(Vector3(6011.874f, 2956.141f, 51.69352f)), owning_building_guid = 49)
      LocalObject(1401, Locker.Constructor(Vector3(6013.023f, 2956.141f, 51.69352f)), owning_building_guid = 49)
      LocalObject(1402, Locker.Constructor(Vector3(6020.194f, 2976.165f, 43.93253f)), owning_building_guid = 49)
      LocalObject(1403, Locker.Constructor(Vector3(6021.518f, 2976.165f, 43.93253f)), owning_building_guid = 49)
      LocalObject(1404, Locker.Constructor(Vector3(6022.854f, 2976.165f, 43.93253f)), owning_building_guid = 49)
      LocalObject(1405, Locker.Constructor(Vector3(6024.191f, 2976.165f, 43.93253f)), owning_building_guid = 49)
      LocalObject(1406, Locker.Constructor(Vector3(6028.731f, 2976.165f, 43.93253f)), owning_building_guid = 49)
      LocalObject(1407, Locker.Constructor(Vector3(6030.055f, 2976.165f, 43.93253f)), owning_building_guid = 49)
      LocalObject(1408, Locker.Constructor(Vector3(6031.391f, 2976.165f, 43.93253f)), owning_building_guid = 49)
      LocalObject(1409, Locker.Constructor(Vector3(6032.728f, 2976.165f, 43.93253f)), owning_building_guid = 49)
      LocalObject(
        149,
        Terminal.Constructor(Vector3(5990.673f, 2977.141f, 74.53552f), air_vehicle_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1637,
        VehicleSpawnPad.Constructor(Vector3(5986.07f, 2997.835f, 71.41052f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 49,
        terminal_guid = 149
      )
      LocalObject(
        150,
        Terminal.Constructor(Vector3(6002.605f, 2977.141f, 74.53552f), air_vehicle_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1638,
        VehicleSpawnPad.Constructor(Vector3(6007.088f, 2997.835f, 71.41052f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 49,
        terminal_guid = 150
      )
      LocalObject(
        1764,
        Terminal.Constructor(Vector3(5989.058f, 2975.486f, 65.26252f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1765,
        Terminal.Constructor(Vector3(6012.654f, 2961.408f, 53.02252f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1766,
        Terminal.Constructor(Vector3(6012.654f, 2965.139f, 53.02252f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1767,
        Terminal.Constructor(Vector3(6012.654f, 2968.928f, 53.02252f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2469,
        Terminal.Constructor(Vector3(5961.942f, 2906.591f, 52.98952f), spawn_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2470,
        Terminal.Constructor(Vector3(5993.942f, 2954.591f, 45.48952f), spawn_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2471,
        Terminal.Constructor(Vector3(5998.971f, 2957.243f, 53.56652f), spawn_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2472,
        Terminal.Constructor(Vector3(5998.967f, 2964.535f, 53.56652f), spawn_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2473,
        Terminal.Constructor(Vector3(5998.97f, 2971.823f, 53.56652f), spawn_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2474,
        Terminal.Constructor(Vector3(6000.532f, 3005.215f, 70.85552f), spawn_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2475,
        Terminal.Constructor(Vector3(6023.242f, 2981.639f, 65.51453f), spawn_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2655,
        Terminal.Constructor(Vector3(5937.996f, 2961.423f, 44.64652f), ground_vehicle_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1636,
        VehicleSpawnPad.Constructor(Vector3(5937.945f, 2972.339f, 36.36952f), mb_pad_creation, Vector3(0, 0, 0)),
        owning_building_guid = 49,
        terminal_guid = 2655
      )
      LocalObject(2302, ResourceSilo.Constructor(Vector3(6047.752f, 3034.555f, 65.84052f)), owning_building_guid = 49)
      LocalObject(
        2370,
        SpawnTube.Constructor(Vector3(5998.233f, 2958.683f, 51.43252f), Vector3(0, 0, 0)),
        owning_building_guid = 49
      )
      LocalObject(
        2371,
        SpawnTube.Constructor(Vector3(5998.233f, 2965.974f, 51.43252f), Vector3(0, 0, 0)),
        owning_building_guid = 49
      )
      LocalObject(
        2372,
        SpawnTube.Constructor(Vector3(5998.233f, 2973.262f, 51.43252f), Vector3(0, 0, 0)),
        owning_building_guid = 49
      )
      LocalObject(
        1656,
        ProximityTerminal.Constructor(Vector3(5989.059f, 2964.901f, 63.92952f), medical_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1657,
        ProximityTerminal.Constructor(Vector3(6026.444f, 2975.62f, 43.93253f), medical_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1960,
        ProximityTerminal.Constructor(Vector3(5924.704f, 2977.661f, 67.02753f), pad_landing_frame),
        owning_building_guid = 49
      )
      LocalObject(
        1961,
        Terminal.Constructor(Vector3(5924.704f, 2977.661f, 67.02753f), air_rearm_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1963,
        ProximityTerminal.Constructor(Vector3(5944.98f, 2993.833f, 69.38252f), pad_landing_frame),
        owning_building_guid = 49
      )
      LocalObject(
        1964,
        Terminal.Constructor(Vector3(5944.98f, 2993.833f, 69.38252f), air_rearm_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1966,
        ProximityTerminal.Constructor(Vector3(5995.379f, 2937.474f, 74.27953f), pad_landing_frame),
        owning_building_guid = 49
      )
      LocalObject(
        1967,
        Terminal.Constructor(Vector3(5995.379f, 2937.474f, 74.27953f), air_rearm_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1969,
        ProximityTerminal.Constructor(Vector3(6011.534f, 2920.628f, 67.04053f), pad_landing_frame),
        owning_building_guid = 49
      )
      LocalObject(
        1970,
        Terminal.Constructor(Vector3(6011.534f, 2920.628f, 67.04053f), air_rearm_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1972,
        ProximityTerminal.Constructor(Vector3(6047.987f, 2959.855f, 69.48453f), pad_landing_frame),
        owning_building_guid = 49
      )
      LocalObject(
        1973,
        Terminal.Constructor(Vector3(6047.987f, 2959.855f, 69.48453f), air_rearm_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1975,
        ProximityTerminal.Constructor(Vector3(6054.28f, 2976.102f, 67.04053f), pad_landing_frame),
        owning_building_guid = 49
      )
      LocalObject(
        1976,
        Terminal.Constructor(Vector3(6054.28f, 2976.102f, 67.04053f), air_rearm_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2269,
        ProximityTerminal.Constructor(Vector3(5986.309f, 3043.637f, 58.56102f), repair_silo),
        owning_building_guid = 49
      )
      LocalObject(
        2270,
        Terminal.Constructor(Vector3(5986.309f, 3043.637f, 58.56102f), ground_rearm_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        2273,
        ProximityTerminal.Constructor(Vector3(6044.637f, 2909.208f, 58.58252f), repair_silo),
        owning_building_guid = 49
      )
      LocalObject(
        2274,
        Terminal.Constructor(Vector3(6044.637f, 2909.208f, 58.58252f), ground_rearm_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1596,
        FacilityTurret.Constructor(Vector3(5901.906f, 3100.855f, 67.33152f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1596, 5064)
      LocalObject(
        1597,
        FacilityTurret.Constructor(Vector3(5907.413f, 2902.665f, 67.33152f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1597, 5065)
      LocalObject(
        1598,
        FacilityTurret.Constructor(Vector3(5991.601f, 3100.855f, 67.33152f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1598, 5066)
      LocalObject(
        1599,
        FacilityTurret.Constructor(Vector3(6072.154f, 2902.657f, 67.33152f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1599, 5067)
      LocalObject(
        1600,
        FacilityTurret.Constructor(Vector3(6072.154f, 3040.398f, 67.33152f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1600, 5068)
      LocalObject(
        1601,
        FacilityTurret.Constructor(Vector3(6079.881f, 2967.423f, 67.33152f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1601, 5069)
      LocalObject(
        2065,
        Painbox.Constructor(Vector3(6011.737f, 2998.206f, 47.40582f), painbox),
        owning_building_guid = 49
      )
      LocalObject(
        2077,
        Painbox.Constructor(Vector3(6006.832f, 2965.212f, 55.70242f), painbox_continuous),
        owning_building_guid = 49
      )
      LocalObject(
        2089,
        Painbox.Constructor(Vector3(6013.7f, 2983.471f, 47.09192f), painbox_door_radius),
        owning_building_guid = 49
      )
      LocalObject(
        2119,
        Painbox.Constructor(Vector3(6005.035f, 2952.278f, 53.56042f), painbox_door_radius_continuous),
        owning_building_guid = 49
      )
      LocalObject(
        2120,
        Painbox.Constructor(Vector3(6005.861f, 2979.769f, 54.10872f), painbox_door_radius_continuous),
        owning_building_guid = 49
      )
      LocalObject(
        2121,
        Painbox.Constructor(Vector3(6021.641f, 2972.57f, 55.01482f), painbox_door_radius_continuous),
        owning_building_guid = 49
      )
      LocalObject(281, Generator.Constructor(Vector3(6013.975f, 3001.555f, 42.63852f)), owning_building_guid = 49)
      LocalObject(
        269,
        Terminal.Constructor(Vector3(6014.022f, 2993.363f, 43.93253f), gen_control),
        owning_building_guid = 49
      )
    }

    Building19()

    def Building19(): Unit = { // Name: E_Ikanam_Tower Type: tower_a GUID: 52, MapID: 19
      LocalBuilding(
        "E_Ikanam_Tower",
        52,
        19,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2994f, 2306f, 52.08814f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2389,
        CaptureTerminal.Constructor(Vector3(3010.587f, 2305.897f, 62.08714f), secondary_capture),
        owning_building_guid = 52
      )
      LocalObject(305, Door.Constructor(Vector3(3006f, 2298f, 53.60914f)), owning_building_guid = 52)
      LocalObject(306, Door.Constructor(Vector3(3006f, 2298f, 73.60814f)), owning_building_guid = 52)
      LocalObject(307, Door.Constructor(Vector3(3006f, 2314f, 53.60914f)), owning_building_guid = 52)
      LocalObject(308, Door.Constructor(Vector3(3006f, 2314f, 73.60814f)), owning_building_guid = 52)
      LocalObject(2499, Door.Constructor(Vector3(3005.146f, 2294.794f, 43.42414f)), owning_building_guid = 52)
      LocalObject(2500, Door.Constructor(Vector3(3005.146f, 2311.204f, 43.42414f)), owning_building_guid = 52)
      LocalObject(
        911,
        IFFLock.Constructor(Vector3(3003.957f, 2314.811f, 53.54914f), Vector3(0, 0, 0)),
        owning_building_guid = 52,
        door_guid = 307
      )
      LocalObject(
        912,
        IFFLock.Constructor(Vector3(3003.957f, 2314.811f, 73.54914f), Vector3(0, 0, 0)),
        owning_building_guid = 52,
        door_guid = 308
      )
      LocalObject(
        913,
        IFFLock.Constructor(Vector3(3008.047f, 2297.189f, 53.54914f), Vector3(0, 0, 180)),
        owning_building_guid = 52,
        door_guid = 305
      )
      LocalObject(
        914,
        IFFLock.Constructor(Vector3(3008.047f, 2297.189f, 73.54914f), Vector3(0, 0, 180)),
        owning_building_guid = 52,
        door_guid = 306
      )
      LocalObject(1148, Locker.Constructor(Vector3(3009.716f, 2290.963f, 42.08214f)), owning_building_guid = 52)
      LocalObject(1149, Locker.Constructor(Vector3(3009.751f, 2312.835f, 42.08214f)), owning_building_guid = 52)
      LocalObject(1150, Locker.Constructor(Vector3(3011.053f, 2290.963f, 42.08214f)), owning_building_guid = 52)
      LocalObject(1151, Locker.Constructor(Vector3(3011.088f, 2312.835f, 42.08214f)), owning_building_guid = 52)
      LocalObject(1152, Locker.Constructor(Vector3(3013.741f, 2290.963f, 42.08214f)), owning_building_guid = 52)
      LocalObject(1153, Locker.Constructor(Vector3(3013.741f, 2312.835f, 42.08214f)), owning_building_guid = 52)
      LocalObject(1154, Locker.Constructor(Vector3(3015.143f, 2290.963f, 42.08214f)), owning_building_guid = 52)
      LocalObject(1155, Locker.Constructor(Vector3(3015.143f, 2312.835f, 42.08214f)), owning_building_guid = 52)
      LocalObject(
        1671,
        Terminal.Constructor(Vector3(3015.445f, 2296.129f, 43.42014f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1672,
        Terminal.Constructor(Vector3(3015.445f, 2301.853f, 43.42014f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1673,
        Terminal.Constructor(Vector3(3015.445f, 2307.234f, 43.42014f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        2312,
        SpawnTube.Constructor(Vector3(3004.706f, 2293.742f, 41.57014f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 52
      )
      LocalObject(
        2313,
        SpawnTube.Constructor(Vector3(3004.706f, 2310.152f, 41.57014f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 52
      )
      LocalObject(
        1526,
        FacilityTurret.Constructor(Vector3(2981.32f, 2293.295f, 71.03014f), manned_turret),
        owning_building_guid = 52
      )
      TurretToWeapon(1526, 5070)
      LocalObject(
        1527,
        FacilityTurret.Constructor(Vector3(3016.647f, 2318.707f, 71.03014f), manned_turret),
        owning_building_guid = 52
      )
      TurretToWeapon(1527, 5071)
      LocalObject(
        2134,
        Painbox.Constructor(Vector3(2999.235f, 2299.803f, 43.58724f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        2135,
        Painbox.Constructor(Vector3(3010.889f, 2308.086f, 42.18814f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        2136,
        Painbox.Constructor(Vector3(3010.975f, 2296.223f, 42.18814f), painbox_radius_continuous),
        owning_building_guid = 52
      )
    }

    Building34()

    def Building34(): Unit = { // Name: W_Qumu_Tower Type: tower_a GUID: 53, MapID: 34
      LocalBuilding(
        "W_Qumu_Tower",
        53,
        34,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3448f, 5518f, 54.41446f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2392,
        CaptureTerminal.Constructor(Vector3(3464.587f, 5517.897f, 64.41346f), secondary_capture),
        owning_building_guid = 53
      )
      LocalObject(330, Door.Constructor(Vector3(3460f, 5510f, 55.93546f)), owning_building_guid = 53)
      LocalObject(331, Door.Constructor(Vector3(3460f, 5510f, 75.93446f)), owning_building_guid = 53)
      LocalObject(332, Door.Constructor(Vector3(3460f, 5526f, 55.93546f)), owning_building_guid = 53)
      LocalObject(333, Door.Constructor(Vector3(3460f, 5526f, 75.93446f)), owning_building_guid = 53)
      LocalObject(2508, Door.Constructor(Vector3(3459.146f, 5506.794f, 45.75047f)), owning_building_guid = 53)
      LocalObject(2509, Door.Constructor(Vector3(3459.146f, 5523.204f, 45.75047f)), owning_building_guid = 53)
      LocalObject(
        931,
        IFFLock.Constructor(Vector3(3457.957f, 5526.811f, 55.87546f), Vector3(0, 0, 0)),
        owning_building_guid = 53,
        door_guid = 332
      )
      LocalObject(
        932,
        IFFLock.Constructor(Vector3(3457.957f, 5526.811f, 75.87547f), Vector3(0, 0, 0)),
        owning_building_guid = 53,
        door_guid = 333
      )
      LocalObject(
        933,
        IFFLock.Constructor(Vector3(3462.047f, 5509.189f, 55.87546f), Vector3(0, 0, 180)),
        owning_building_guid = 53,
        door_guid = 330
      )
      LocalObject(
        934,
        IFFLock.Constructor(Vector3(3462.047f, 5509.189f, 75.87547f), Vector3(0, 0, 180)),
        owning_building_guid = 53,
        door_guid = 331
      )
      LocalObject(1193, Locker.Constructor(Vector3(3463.716f, 5502.963f, 44.40846f)), owning_building_guid = 53)
      LocalObject(1194, Locker.Constructor(Vector3(3463.751f, 5524.835f, 44.40846f)), owning_building_guid = 53)
      LocalObject(1195, Locker.Constructor(Vector3(3465.053f, 5502.963f, 44.40846f)), owning_building_guid = 53)
      LocalObject(1196, Locker.Constructor(Vector3(3465.088f, 5524.835f, 44.40846f)), owning_building_guid = 53)
      LocalObject(1197, Locker.Constructor(Vector3(3467.741f, 5502.963f, 44.40846f)), owning_building_guid = 53)
      LocalObject(1198, Locker.Constructor(Vector3(3467.741f, 5524.835f, 44.40846f)), owning_building_guid = 53)
      LocalObject(1199, Locker.Constructor(Vector3(3469.143f, 5502.963f, 44.40846f)), owning_building_guid = 53)
      LocalObject(1200, Locker.Constructor(Vector3(3469.143f, 5524.835f, 44.40846f)), owning_building_guid = 53)
      LocalObject(
        1684,
        Terminal.Constructor(Vector3(3469.445f, 5508.129f, 45.74646f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1685,
        Terminal.Constructor(Vector3(3469.445f, 5513.853f, 45.74646f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1686,
        Terminal.Constructor(Vector3(3469.445f, 5519.234f, 45.74646f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        2321,
        SpawnTube.Constructor(Vector3(3458.706f, 5505.742f, 43.89646f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 53
      )
      LocalObject(
        2322,
        SpawnTube.Constructor(Vector3(3458.706f, 5522.152f, 43.89646f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 53
      )
      LocalObject(
        1539,
        FacilityTurret.Constructor(Vector3(3435.32f, 5505.295f, 73.35646f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1539, 5072)
      LocalObject(
        1540,
        FacilityTurret.Constructor(Vector3(3470.647f, 5530.707f, 73.35646f), manned_turret),
        owning_building_guid = 53
      )
      TurretToWeapon(1540, 5073)
      LocalObject(
        2143,
        Painbox.Constructor(Vector3(3453.235f, 5511.803f, 45.91356f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        2144,
        Painbox.Constructor(Vector3(3464.889f, 5520.086f, 44.51447f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        2145,
        Painbox.Constructor(Vector3(3464.975f, 5508.223f, 44.51447f), painbox_radius_continuous),
        owning_building_guid = 53
      )
    }

    Building20()

    def Building20(): Unit = { // Name: NE_Cetan_Tower Type: tower_a GUID: 54, MapID: 20
      LocalBuilding(
        "NE_Cetan_Tower",
        54,
        20,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3912f, 3212f, 43.86781f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2395,
        CaptureTerminal.Constructor(Vector3(3928.587f, 3211.897f, 53.86681f), secondary_capture),
        owning_building_guid = 54
      )
      LocalObject(367, Door.Constructor(Vector3(3924f, 3204f, 45.38881f)), owning_building_guid = 54)
      LocalObject(368, Door.Constructor(Vector3(3924f, 3204f, 65.38782f)), owning_building_guid = 54)
      LocalObject(369, Door.Constructor(Vector3(3924f, 3220f, 45.38881f)), owning_building_guid = 54)
      LocalObject(370, Door.Constructor(Vector3(3924f, 3220f, 65.38782f)), owning_building_guid = 54)
      LocalObject(2520, Door.Constructor(Vector3(3923.146f, 3200.794f, 35.20381f)), owning_building_guid = 54)
      LocalObject(2521, Door.Constructor(Vector3(3923.146f, 3217.204f, 35.20381f)), owning_building_guid = 54)
      LocalObject(
        965,
        IFFLock.Constructor(Vector3(3921.957f, 3220.811f, 45.32881f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 369
      )
      LocalObject(
        966,
        IFFLock.Constructor(Vector3(3921.957f, 3220.811f, 65.32881f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 370
      )
      LocalObject(
        967,
        IFFLock.Constructor(Vector3(3926.047f, 3203.189f, 45.32881f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 367
      )
      LocalObject(
        968,
        IFFLock.Constructor(Vector3(3926.047f, 3203.189f, 65.32881f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 368
      )
      LocalObject(1232, Locker.Constructor(Vector3(3927.716f, 3196.963f, 33.86181f)), owning_building_guid = 54)
      LocalObject(1233, Locker.Constructor(Vector3(3927.751f, 3218.835f, 33.86181f)), owning_building_guid = 54)
      LocalObject(1235, Locker.Constructor(Vector3(3929.053f, 3196.963f, 33.86181f)), owning_building_guid = 54)
      LocalObject(1236, Locker.Constructor(Vector3(3929.088f, 3218.835f, 33.86181f)), owning_building_guid = 54)
      LocalObject(1239, Locker.Constructor(Vector3(3931.741f, 3196.963f, 33.86181f)), owning_building_guid = 54)
      LocalObject(1240, Locker.Constructor(Vector3(3931.741f, 3218.835f, 33.86181f)), owning_building_guid = 54)
      LocalObject(1245, Locker.Constructor(Vector3(3933.143f, 3196.963f, 33.86181f)), owning_building_guid = 54)
      LocalObject(1246, Locker.Constructor(Vector3(3933.143f, 3218.835f, 33.86181f)), owning_building_guid = 54)
      LocalObject(
        1707,
        Terminal.Constructor(Vector3(3933.445f, 3202.129f, 35.19981f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1708,
        Terminal.Constructor(Vector3(3933.445f, 3207.853f, 35.19981f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1709,
        Terminal.Constructor(Vector3(3933.445f, 3213.234f, 35.19981f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2333,
        SpawnTube.Constructor(Vector3(3922.706f, 3199.742f, 33.34982f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        2334,
        SpawnTube.Constructor(Vector3(3922.706f, 3216.152f, 33.34982f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        1549,
        FacilityTurret.Constructor(Vector3(3899.32f, 3199.295f, 62.80981f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(1549, 5074)
      LocalObject(
        1552,
        FacilityTurret.Constructor(Vector3(3934.647f, 3224.707f, 62.80981f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(1552, 5075)
      LocalObject(
        2152,
        Painbox.Constructor(Vector3(3917.235f, 3205.803f, 35.36691f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        2153,
        Painbox.Constructor(Vector3(3928.889f, 3214.086f, 33.96781f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        2154,
        Painbox.Constructor(Vector3(3928.975f, 3202.223f, 33.96781f), painbox_radius_continuous),
        owning_building_guid = 54
      )
    }

    Building32()

    def Building32(): Unit = { // Name: NW_Sungrey_Tower Type: tower_a GUID: 55, MapID: 32
      LocalBuilding(
        "NW_Sungrey_Tower",
        55,
        32,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4826f, 5260f, 88.36119f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2399,
        CaptureTerminal.Constructor(Vector3(4842.587f, 5259.897f, 98.36019f), secondary_capture),
        owning_building_guid = 55
      )
      LocalObject(441, Door.Constructor(Vector3(4838f, 5252f, 89.88219f)), owning_building_guid = 55)
      LocalObject(442, Door.Constructor(Vector3(4838f, 5252f, 109.8812f)), owning_building_guid = 55)
      LocalObject(443, Door.Constructor(Vector3(4838f, 5268f, 89.88219f)), owning_building_guid = 55)
      LocalObject(444, Door.Constructor(Vector3(4838f, 5268f, 109.8812f)), owning_building_guid = 55)
      LocalObject(2537, Door.Constructor(Vector3(4837.146f, 5248.794f, 79.69719f)), owning_building_guid = 55)
      LocalObject(2538, Door.Constructor(Vector3(4837.146f, 5265.204f, 79.69719f)), owning_building_guid = 55)
      LocalObject(
        1014,
        IFFLock.Constructor(Vector3(4835.957f, 5268.811f, 89.82219f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 443
      )
      LocalObject(
        1015,
        IFFLock.Constructor(Vector3(4835.957f, 5268.811f, 109.8222f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 444
      )
      LocalObject(
        1016,
        IFFLock.Constructor(Vector3(4840.047f, 5251.189f, 89.82219f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 441
      )
      LocalObject(
        1017,
        IFFLock.Constructor(Vector3(4840.047f, 5251.189f, 109.8222f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 442
      )
      LocalObject(1309, Locker.Constructor(Vector3(4841.716f, 5244.963f, 78.35519f)), owning_building_guid = 55)
      LocalObject(1310, Locker.Constructor(Vector3(4841.751f, 5266.835f, 78.35519f)), owning_building_guid = 55)
      LocalObject(1311, Locker.Constructor(Vector3(4843.053f, 5244.963f, 78.35519f)), owning_building_guid = 55)
      LocalObject(1312, Locker.Constructor(Vector3(4843.088f, 5266.835f, 78.35519f)), owning_building_guid = 55)
      LocalObject(1313, Locker.Constructor(Vector3(4845.741f, 5244.963f, 78.35519f)), owning_building_guid = 55)
      LocalObject(1314, Locker.Constructor(Vector3(4845.741f, 5266.835f, 78.35519f)), owning_building_guid = 55)
      LocalObject(1315, Locker.Constructor(Vector3(4847.143f, 5244.963f, 78.35519f)), owning_building_guid = 55)
      LocalObject(1316, Locker.Constructor(Vector3(4847.143f, 5266.835f, 78.35519f)), owning_building_guid = 55)
      LocalObject(
        1733,
        Terminal.Constructor(Vector3(4847.445f, 5250.129f, 79.69319f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1734,
        Terminal.Constructor(Vector3(4847.445f, 5255.853f, 79.69319f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1735,
        Terminal.Constructor(Vector3(4847.445f, 5261.234f, 79.69319f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        2350,
        SpawnTube.Constructor(Vector3(4836.706f, 5247.742f, 77.84319f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        2351,
        SpawnTube.Constructor(Vector3(4836.706f, 5264.152f, 77.84319f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        1578,
        FacilityTurret.Constructor(Vector3(4813.32f, 5247.295f, 107.3032f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1578, 5076)
      LocalObject(
        1579,
        FacilityTurret.Constructor(Vector3(4848.647f, 5272.707f, 107.3032f), manned_turret),
        owning_building_guid = 55
      )
      TurretToWeapon(1579, 5077)
      LocalObject(
        2164,
        Painbox.Constructor(Vector3(4831.235f, 5253.803f, 79.86029f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        2165,
        Painbox.Constructor(Vector3(4842.889f, 5262.086f, 78.46119f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        2166,
        Painbox.Constructor(Vector3(4842.975f, 5250.223f, 78.46119f), painbox_radius_continuous),
        owning_building_guid = 55
      )
    }

    Building23()

    def Building23(): Unit = { // Name: E_Kyoi_Tower Type: tower_a GUID: 56, MapID: 23
      LocalBuilding(
        "E_Kyoi_Tower",
        56,
        23,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5458f, 2004f, 81.14763f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2404,
        CaptureTerminal.Constructor(Vector3(5474.587f, 2003.897f, 91.14663f), secondary_capture),
        owning_building_guid = 56
      )
      LocalObject(480, Door.Constructor(Vector3(5470f, 1996f, 82.66863f)), owning_building_guid = 56)
      LocalObject(481, Door.Constructor(Vector3(5470f, 1996f, 102.6676f)), owning_building_guid = 56)
      LocalObject(482, Door.Constructor(Vector3(5470f, 2012f, 82.66863f)), owning_building_guid = 56)
      LocalObject(483, Door.Constructor(Vector3(5470f, 2012f, 102.6676f)), owning_building_guid = 56)
      LocalObject(2550, Door.Constructor(Vector3(5469.146f, 1992.794f, 72.48363f)), owning_building_guid = 56)
      LocalObject(2551, Door.Constructor(Vector3(5469.146f, 2009.204f, 72.48363f)), owning_building_guid = 56)
      LocalObject(
        1049,
        IFFLock.Constructor(Vector3(5467.957f, 2012.811f, 82.60863f), Vector3(0, 0, 0)),
        owning_building_guid = 56,
        door_guid = 482
      )
      LocalObject(
        1050,
        IFFLock.Constructor(Vector3(5467.957f, 2012.811f, 102.6086f), Vector3(0, 0, 0)),
        owning_building_guid = 56,
        door_guid = 483
      )
      LocalObject(
        1051,
        IFFLock.Constructor(Vector3(5472.047f, 1995.189f, 82.60863f), Vector3(0, 0, 180)),
        owning_building_guid = 56,
        door_guid = 480
      )
      LocalObject(
        1052,
        IFFLock.Constructor(Vector3(5472.047f, 1995.189f, 102.6086f), Vector3(0, 0, 180)),
        owning_building_guid = 56,
        door_guid = 481
      )
      LocalObject(1361, Locker.Constructor(Vector3(5473.716f, 1988.963f, 71.14163f)), owning_building_guid = 56)
      LocalObject(1362, Locker.Constructor(Vector3(5473.751f, 2010.835f, 71.14163f)), owning_building_guid = 56)
      LocalObject(1363, Locker.Constructor(Vector3(5475.053f, 1988.963f, 71.14163f)), owning_building_guid = 56)
      LocalObject(1364, Locker.Constructor(Vector3(5475.088f, 2010.835f, 71.14163f)), owning_building_guid = 56)
      LocalObject(1365, Locker.Constructor(Vector3(5477.741f, 1988.963f, 71.14163f)), owning_building_guid = 56)
      LocalObject(1366, Locker.Constructor(Vector3(5477.741f, 2010.835f, 71.14163f)), owning_building_guid = 56)
      LocalObject(1367, Locker.Constructor(Vector3(5479.143f, 1988.963f, 71.14163f)), owning_building_guid = 56)
      LocalObject(1368, Locker.Constructor(Vector3(5479.143f, 2010.835f, 71.14163f)), owning_building_guid = 56)
      LocalObject(
        1754,
        Terminal.Constructor(Vector3(5479.445f, 1994.129f, 72.47963f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1755,
        Terminal.Constructor(Vector3(5479.445f, 1999.853f, 72.47963f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1756,
        Terminal.Constructor(Vector3(5479.445f, 2005.234f, 72.47963f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        2363,
        SpawnTube.Constructor(Vector3(5468.706f, 1991.742f, 70.62963f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 56
      )
      LocalObject(
        2364,
        SpawnTube.Constructor(Vector3(5468.706f, 2008.152f, 70.62963f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 56
      )
      LocalObject(
        1586,
        FacilityTurret.Constructor(Vector3(5445.32f, 1991.295f, 100.0896f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1586, 5078)
      LocalObject(
        1590,
        FacilityTurret.Constructor(Vector3(5480.647f, 2016.707f, 100.0896f), manned_turret),
        owning_building_guid = 56
      )
      TurretToWeapon(1590, 5079)
      LocalObject(
        2179,
        Painbox.Constructor(Vector3(5463.235f, 1997.803f, 72.64673f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2180,
        Painbox.Constructor(Vector3(5474.889f, 2006.086f, 71.24763f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        2181,
        Painbox.Constructor(Vector3(5474.975f, 1994.223f, 71.24763f), painbox_radius_continuous),
        owning_building_guid = 56
      )
    }

    Building28()

    def Building28(): Unit = { // Name: W_Azeban_Tower Type: tower_a GUID: 57, MapID: 28
      LocalBuilding(
        "W_Azeban_Tower",
        57,
        28,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5554f, 5326f, 43.06631f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2405,
        CaptureTerminal.Constructor(Vector3(5570.587f, 5325.897f, 53.06531f), secondary_capture),
        owning_building_guid = 57
      )
      LocalObject(490, Door.Constructor(Vector3(5566f, 5318f, 44.58731f)), owning_building_guid = 57)
      LocalObject(491, Door.Constructor(Vector3(5566f, 5318f, 64.5863f)), owning_building_guid = 57)
      LocalObject(492, Door.Constructor(Vector3(5566f, 5334f, 44.58731f)), owning_building_guid = 57)
      LocalObject(493, Door.Constructor(Vector3(5566f, 5334f, 64.5863f)), owning_building_guid = 57)
      LocalObject(2555, Door.Constructor(Vector3(5565.146f, 5314.794f, 34.40231f)), owning_building_guid = 57)
      LocalObject(2556, Door.Constructor(Vector3(5565.146f, 5331.204f, 34.40231f)), owning_building_guid = 57)
      LocalObject(
        1060,
        IFFLock.Constructor(Vector3(5563.957f, 5334.811f, 44.52731f), Vector3(0, 0, 0)),
        owning_building_guid = 57,
        door_guid = 492
      )
      LocalObject(
        1061,
        IFFLock.Constructor(Vector3(5563.957f, 5334.811f, 64.52731f), Vector3(0, 0, 0)),
        owning_building_guid = 57,
        door_guid = 493
      )
      LocalObject(
        1062,
        IFFLock.Constructor(Vector3(5568.047f, 5317.189f, 44.52731f), Vector3(0, 0, 180)),
        owning_building_guid = 57,
        door_guid = 490
      )
      LocalObject(
        1063,
        IFFLock.Constructor(Vector3(5568.047f, 5317.189f, 64.52731f), Vector3(0, 0, 180)),
        owning_building_guid = 57,
        door_guid = 491
      )
      LocalObject(1390, Locker.Constructor(Vector3(5569.716f, 5310.963f, 33.06031f)), owning_building_guid = 57)
      LocalObject(1391, Locker.Constructor(Vector3(5569.751f, 5332.835f, 33.06031f)), owning_building_guid = 57)
      LocalObject(1392, Locker.Constructor(Vector3(5571.053f, 5310.963f, 33.06031f)), owning_building_guid = 57)
      LocalObject(1393, Locker.Constructor(Vector3(5571.088f, 5332.835f, 33.06031f)), owning_building_guid = 57)
      LocalObject(1394, Locker.Constructor(Vector3(5573.741f, 5310.963f, 33.06031f)), owning_building_guid = 57)
      LocalObject(1395, Locker.Constructor(Vector3(5573.741f, 5332.835f, 33.06031f)), owning_building_guid = 57)
      LocalObject(1396, Locker.Constructor(Vector3(5575.143f, 5310.963f, 33.06031f)), owning_building_guid = 57)
      LocalObject(1397, Locker.Constructor(Vector3(5575.143f, 5332.835f, 33.06031f)), owning_building_guid = 57)
      LocalObject(
        1761,
        Terminal.Constructor(Vector3(5575.445f, 5316.129f, 34.39831f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1762,
        Terminal.Constructor(Vector3(5575.445f, 5321.853f, 34.39831f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1763,
        Terminal.Constructor(Vector3(5575.445f, 5327.234f, 34.39831f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        2368,
        SpawnTube.Constructor(Vector3(5564.706f, 5313.742f, 32.54831f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 57
      )
      LocalObject(
        2369,
        SpawnTube.Constructor(Vector3(5564.706f, 5330.152f, 32.54831f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 57
      )
      LocalObject(
        1592,
        FacilityTurret.Constructor(Vector3(5541.32f, 5313.295f, 62.00831f), manned_turret),
        owning_building_guid = 57
      )
      TurretToWeapon(1592, 5080)
      LocalObject(
        1593,
        FacilityTurret.Constructor(Vector3(5576.647f, 5338.707f, 62.00831f), manned_turret),
        owning_building_guid = 57
      )
      TurretToWeapon(1593, 5081)
      LocalObject(
        2182,
        Painbox.Constructor(Vector3(5559.235f, 5319.803f, 34.56541f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        2183,
        Painbox.Constructor(Vector3(5570.889f, 5328.086f, 33.16631f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        2184,
        Painbox.Constructor(Vector3(5570.975f, 5316.223f, 33.16631f), painbox_radius_continuous),
        owning_building_guid = 57
      )
    }

    Building48()

    def Building48(): Unit = { // Name: Azeban_Tower Type: tower_a GUID: 58, MapID: 48
      LocalBuilding(
        "Azeban_Tower",
        58,
        48,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6176f, 5550f, 49.46858f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2407,
        CaptureTerminal.Constructor(Vector3(6192.587f, 5549.897f, 59.46758f), secondary_capture),
        owning_building_guid = 58
      )
      LocalObject(519, Door.Constructor(Vector3(6188f, 5542f, 50.98958f)), owning_building_guid = 58)
      LocalObject(520, Door.Constructor(Vector3(6188f, 5542f, 70.98859f)), owning_building_guid = 58)
      LocalObject(521, Door.Constructor(Vector3(6188f, 5558f, 50.98958f)), owning_building_guid = 58)
      LocalObject(522, Door.Constructor(Vector3(6188f, 5558f, 70.98859f)), owning_building_guid = 58)
      LocalObject(2562, Door.Constructor(Vector3(6187.146f, 5538.794f, 40.80458f)), owning_building_guid = 58)
      LocalObject(2563, Door.Constructor(Vector3(6187.146f, 5555.204f, 40.80458f)), owning_building_guid = 58)
      LocalObject(
        1077,
        IFFLock.Constructor(Vector3(6185.957f, 5558.811f, 50.92958f), Vector3(0, 0, 0)),
        owning_building_guid = 58,
        door_guid = 521
      )
      LocalObject(
        1078,
        IFFLock.Constructor(Vector3(6185.957f, 5558.811f, 70.92958f), Vector3(0, 0, 0)),
        owning_building_guid = 58,
        door_guid = 522
      )
      LocalObject(
        1079,
        IFFLock.Constructor(Vector3(6190.047f, 5541.189f, 50.92958f), Vector3(0, 0, 180)),
        owning_building_guid = 58,
        door_guid = 519
      )
      LocalObject(
        1080,
        IFFLock.Constructor(Vector3(6190.047f, 5541.189f, 70.92958f), Vector3(0, 0, 180)),
        owning_building_guid = 58,
        door_guid = 520
      )
      LocalObject(1418, Locker.Constructor(Vector3(6191.716f, 5534.963f, 39.46258f)), owning_building_guid = 58)
      LocalObject(1419, Locker.Constructor(Vector3(6191.751f, 5556.835f, 39.46258f)), owning_building_guid = 58)
      LocalObject(1420, Locker.Constructor(Vector3(6193.053f, 5534.963f, 39.46258f)), owning_building_guid = 58)
      LocalObject(1421, Locker.Constructor(Vector3(6193.088f, 5556.835f, 39.46258f)), owning_building_guid = 58)
      LocalObject(1422, Locker.Constructor(Vector3(6195.741f, 5534.963f, 39.46258f)), owning_building_guid = 58)
      LocalObject(1423, Locker.Constructor(Vector3(6195.741f, 5556.835f, 39.46258f)), owning_building_guid = 58)
      LocalObject(1424, Locker.Constructor(Vector3(6197.143f, 5534.963f, 39.46258f)), owning_building_guid = 58)
      LocalObject(1425, Locker.Constructor(Vector3(6197.143f, 5556.835f, 39.46258f)), owning_building_guid = 58)
      LocalObject(
        1771,
        Terminal.Constructor(Vector3(6197.445f, 5540.129f, 40.80058f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1772,
        Terminal.Constructor(Vector3(6197.445f, 5545.853f, 40.80058f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1773,
        Terminal.Constructor(Vector3(6197.445f, 5551.234f, 40.80058f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        2375,
        SpawnTube.Constructor(Vector3(6186.706f, 5537.742f, 38.95058f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 58
      )
      LocalObject(
        2376,
        SpawnTube.Constructor(Vector3(6186.706f, 5554.152f, 38.95058f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 58
      )
      LocalObject(
        1604,
        FacilityTurret.Constructor(Vector3(6163.32f, 5537.295f, 68.41058f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1604, 5082)
      LocalObject(
        1607,
        FacilityTurret.Constructor(Vector3(6198.647f, 5562.707f, 68.41058f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1607, 5083)
      LocalObject(
        2188,
        Painbox.Constructor(Vector3(6181.235f, 5543.803f, 40.96768f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        2189,
        Painbox.Constructor(Vector3(6192.889f, 5552.086f, 39.56858f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        2190,
        Painbox.Constructor(Vector3(6192.975f, 5540.223f, 39.56858f), painbox_radius_continuous),
        owning_building_guid = 58
      )
    }

    Building30()

    def Building30(): Unit = { // Name: S_Ceryshen_Warpgate_Tower Type: tower_a GUID: 59, MapID: 30
      LocalBuilding(
        "S_Ceryshen_Warpgate_Tower",
        59,
        30,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6302f, 6398f, 106.5124f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        2408,
        CaptureTerminal.Constructor(Vector3(6318.587f, 6397.897f, 116.5114f), secondary_capture),
        owning_building_guid = 59
      )
      LocalObject(535, Door.Constructor(Vector3(6314f, 6390f, 108.0334f)), owning_building_guid = 59)
      LocalObject(536, Door.Constructor(Vector3(6314f, 6390f, 128.0324f)), owning_building_guid = 59)
      LocalObject(537, Door.Constructor(Vector3(6314f, 6406f, 108.0334f)), owning_building_guid = 59)
      LocalObject(538, Door.Constructor(Vector3(6314f, 6406f, 128.0324f)), owning_building_guid = 59)
      LocalObject(2567, Door.Constructor(Vector3(6313.146f, 6386.794f, 97.84837f)), owning_building_guid = 59)
      LocalObject(2568, Door.Constructor(Vector3(6313.146f, 6403.204f, 97.84837f)), owning_building_guid = 59)
      LocalObject(
        1089,
        IFFLock.Constructor(Vector3(6311.957f, 6406.811f, 107.9734f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 537
      )
      LocalObject(
        1090,
        IFFLock.Constructor(Vector3(6311.957f, 6406.811f, 127.9734f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 538
      )
      LocalObject(
        1093,
        IFFLock.Constructor(Vector3(6316.047f, 6389.189f, 107.9734f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 535
      )
      LocalObject(
        1094,
        IFFLock.Constructor(Vector3(6316.047f, 6389.189f, 127.9734f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 536
      )
      LocalObject(1430, Locker.Constructor(Vector3(6317.716f, 6382.963f, 96.50638f)), owning_building_guid = 59)
      LocalObject(1431, Locker.Constructor(Vector3(6317.751f, 6404.835f, 96.50638f)), owning_building_guid = 59)
      LocalObject(1433, Locker.Constructor(Vector3(6319.053f, 6382.963f, 96.50638f)), owning_building_guid = 59)
      LocalObject(1434, Locker.Constructor(Vector3(6319.088f, 6404.835f, 96.50638f)), owning_building_guid = 59)
      LocalObject(1437, Locker.Constructor(Vector3(6321.741f, 6382.963f, 96.50638f)), owning_building_guid = 59)
      LocalObject(1438, Locker.Constructor(Vector3(6321.741f, 6404.835f, 96.50638f)), owning_building_guid = 59)
      LocalObject(1440, Locker.Constructor(Vector3(6323.143f, 6382.963f, 96.50638f)), owning_building_guid = 59)
      LocalObject(1441, Locker.Constructor(Vector3(6323.143f, 6404.835f, 96.50638f)), owning_building_guid = 59)
      LocalObject(
        1782,
        Terminal.Constructor(Vector3(6323.445f, 6388.129f, 97.84438f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1783,
        Terminal.Constructor(Vector3(6323.445f, 6393.853f, 97.84438f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1784,
        Terminal.Constructor(Vector3(6323.445f, 6399.234f, 97.84438f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        2380,
        SpawnTube.Constructor(Vector3(6312.706f, 6385.742f, 95.99438f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        2381,
        SpawnTube.Constructor(Vector3(6312.706f, 6402.152f, 95.99438f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        1609,
        FacilityTurret.Constructor(Vector3(6289.32f, 6385.295f, 125.4544f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1609, 5084)
      LocalObject(
        1612,
        FacilityTurret.Constructor(Vector3(6324.647f, 6410.707f, 125.4544f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1612, 5085)
      LocalObject(
        2191,
        Painbox.Constructor(Vector3(6307.235f, 6391.803f, 98.01147f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        2192,
        Painbox.Constructor(Vector3(6318.889f, 6400.086f, 96.61237f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        2193,
        Painbox.Constructor(Vector3(6318.975f, 6388.223f, 96.61237f), painbox_radius_continuous),
        owning_building_guid = 59
      )
    }

    Building16()

    def Building16(): Unit = { // Name: NE_Solsar_Warpgate_Tower Type: tower_b GUID: 60, MapID: 16
      LocalBuilding(
        "NE_Solsar_Warpgate_Tower",
        60,
        16,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1910f, 1664f, 43.16405f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2387,
        CaptureTerminal.Constructor(Vector3(1926.587f, 1663.897f, 63.16305f), secondary_capture),
        owning_building_guid = 60
      )
      LocalObject(284, Door.Constructor(Vector3(1922f, 1656f, 44.68405f)), owning_building_guid = 60)
      LocalObject(285, Door.Constructor(Vector3(1922f, 1656f, 54.68405f)), owning_building_guid = 60)
      LocalObject(286, Door.Constructor(Vector3(1922f, 1656f, 74.68405f)), owning_building_guid = 60)
      LocalObject(287, Door.Constructor(Vector3(1922f, 1672f, 44.68405f)), owning_building_guid = 60)
      LocalObject(288, Door.Constructor(Vector3(1922f, 1672f, 54.68405f)), owning_building_guid = 60)
      LocalObject(289, Door.Constructor(Vector3(1922f, 1672f, 74.68405f)), owning_building_guid = 60)
      LocalObject(2492, Door.Constructor(Vector3(1921.147f, 1652.794f, 34.50005f)), owning_building_guid = 60)
      LocalObject(2493, Door.Constructor(Vector3(1921.147f, 1669.204f, 34.50005f)), owning_building_guid = 60)
      LocalObject(
        893,
        IFFLock.Constructor(Vector3(1919.957f, 1672.811f, 44.62505f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 287
      )
      LocalObject(
        894,
        IFFLock.Constructor(Vector3(1919.957f, 1672.811f, 54.62505f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 288
      )
      LocalObject(
        895,
        IFFLock.Constructor(Vector3(1919.957f, 1672.811f, 74.62505f), Vector3(0, 0, 0)),
        owning_building_guid = 60,
        door_guid = 289
      )
      LocalObject(
        896,
        IFFLock.Constructor(Vector3(1924.047f, 1655.189f, 44.62505f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 284
      )
      LocalObject(
        897,
        IFFLock.Constructor(Vector3(1924.047f, 1655.189f, 54.62505f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 285
      )
      LocalObject(
        898,
        IFFLock.Constructor(Vector3(1924.047f, 1655.189f, 74.62505f), Vector3(0, 0, 180)),
        owning_building_guid = 60,
        door_guid = 286
      )
      LocalObject(1111, Locker.Constructor(Vector3(1925.716f, 1648.963f, 33.15805f)), owning_building_guid = 60)
      LocalObject(1112, Locker.Constructor(Vector3(1925.751f, 1670.835f, 33.15805f)), owning_building_guid = 60)
      LocalObject(1113, Locker.Constructor(Vector3(1927.053f, 1648.963f, 33.15805f)), owning_building_guid = 60)
      LocalObject(1114, Locker.Constructor(Vector3(1927.088f, 1670.835f, 33.15805f)), owning_building_guid = 60)
      LocalObject(1115, Locker.Constructor(Vector3(1929.741f, 1648.963f, 33.15805f)), owning_building_guid = 60)
      LocalObject(1116, Locker.Constructor(Vector3(1929.741f, 1670.835f, 33.15805f)), owning_building_guid = 60)
      LocalObject(1117, Locker.Constructor(Vector3(1931.143f, 1648.963f, 33.15805f)), owning_building_guid = 60)
      LocalObject(1118, Locker.Constructor(Vector3(1931.143f, 1670.835f, 33.15805f)), owning_building_guid = 60)
      LocalObject(
        1661,
        Terminal.Constructor(Vector3(1931.446f, 1654.129f, 34.49605f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1662,
        Terminal.Constructor(Vector3(1931.446f, 1659.853f, 34.49605f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1663,
        Terminal.Constructor(Vector3(1931.446f, 1665.234f, 34.49605f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        2305,
        SpawnTube.Constructor(Vector3(1920.706f, 1651.742f, 32.64605f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        2306,
        SpawnTube.Constructor(Vector3(1920.706f, 1668.152f, 32.64605f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 60
      )
      LocalObject(
        2128,
        Painbox.Constructor(Vector3(1915.493f, 1656.849f, 34.45345f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        2129,
        Painbox.Constructor(Vector3(1927.127f, 1654.078f, 33.26405f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        2130,
        Painbox.Constructor(Vector3(1927.259f, 1666.107f, 33.26405f), painbox_radius_continuous),
        owning_building_guid = 60
      )
    }

    Building26()

    def Building26(): Unit = { // Name: NE_NCSanc_Warpgate_Tower Type: tower_b GUID: 61, MapID: 26
      LocalBuilding(
        "NE_NCSanc_Warpgate_Tower",
        61,
        26,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3488f, 4702f, 60.32801f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2393,
        CaptureTerminal.Constructor(Vector3(3504.587f, 4701.897f, 80.32701f), secondary_capture),
        owning_building_guid = 61
      )
      LocalObject(341, Door.Constructor(Vector3(3500f, 4694f, 61.84801f)), owning_building_guid = 61)
      LocalObject(342, Door.Constructor(Vector3(3500f, 4694f, 71.84801f)), owning_building_guid = 61)
      LocalObject(343, Door.Constructor(Vector3(3500f, 4694f, 91.84801f)), owning_building_guid = 61)
      LocalObject(344, Door.Constructor(Vector3(3500f, 4710f, 61.84801f)), owning_building_guid = 61)
      LocalObject(345, Door.Constructor(Vector3(3500f, 4710f, 71.84801f)), owning_building_guid = 61)
      LocalObject(346, Door.Constructor(Vector3(3500f, 4710f, 91.84801f)), owning_building_guid = 61)
      LocalObject(2510, Door.Constructor(Vector3(3499.147f, 4690.794f, 51.66402f)), owning_building_guid = 61)
      LocalObject(2511, Door.Constructor(Vector3(3499.147f, 4707.204f, 51.66402f)), owning_building_guid = 61)
      LocalObject(
        942,
        IFFLock.Constructor(Vector3(3497.957f, 4710.811f, 61.78901f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 344
      )
      LocalObject(
        943,
        IFFLock.Constructor(Vector3(3497.957f, 4710.811f, 71.78902f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 345
      )
      LocalObject(
        944,
        IFFLock.Constructor(Vector3(3497.957f, 4710.811f, 91.78902f), Vector3(0, 0, 0)),
        owning_building_guid = 61,
        door_guid = 346
      )
      LocalObject(
        946,
        IFFLock.Constructor(Vector3(3502.047f, 4693.189f, 61.78901f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 341
      )
      LocalObject(
        947,
        IFFLock.Constructor(Vector3(3502.047f, 4693.189f, 71.78902f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 342
      )
      LocalObject(
        948,
        IFFLock.Constructor(Vector3(3502.047f, 4693.189f, 91.78902f), Vector3(0, 0, 180)),
        owning_building_guid = 61,
        door_guid = 343
      )
      LocalObject(1201, Locker.Constructor(Vector3(3503.716f, 4686.963f, 50.32201f)), owning_building_guid = 61)
      LocalObject(1202, Locker.Constructor(Vector3(3503.751f, 4708.835f, 50.32201f)), owning_building_guid = 61)
      LocalObject(1203, Locker.Constructor(Vector3(3505.053f, 4686.963f, 50.32201f)), owning_building_guid = 61)
      LocalObject(1204, Locker.Constructor(Vector3(3505.088f, 4708.835f, 50.32201f)), owning_building_guid = 61)
      LocalObject(1205, Locker.Constructor(Vector3(3507.741f, 4686.963f, 50.32201f)), owning_building_guid = 61)
      LocalObject(1206, Locker.Constructor(Vector3(3507.741f, 4708.835f, 50.32201f)), owning_building_guid = 61)
      LocalObject(1207, Locker.Constructor(Vector3(3509.143f, 4686.963f, 50.32201f)), owning_building_guid = 61)
      LocalObject(1208, Locker.Constructor(Vector3(3509.143f, 4708.835f, 50.32201f)), owning_building_guid = 61)
      LocalObject(
        1691,
        Terminal.Constructor(Vector3(3509.446f, 4692.129f, 51.66002f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1692,
        Terminal.Constructor(Vector3(3509.446f, 4697.853f, 51.66002f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1693,
        Terminal.Constructor(Vector3(3509.446f, 4703.234f, 51.66002f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        2323,
        SpawnTube.Constructor(Vector3(3498.706f, 4689.742f, 49.81001f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        2324,
        SpawnTube.Constructor(Vector3(3498.706f, 4706.152f, 49.81001f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 61
      )
      LocalObject(
        2146,
        Painbox.Constructor(Vector3(3493.493f, 4694.849f, 51.61742f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        2147,
        Painbox.Constructor(Vector3(3505.127f, 4692.078f, 50.42802f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        2148,
        Painbox.Constructor(Vector3(3505.259f, 4704.107f, 50.42802f), painbox_radius_continuous),
        owning_building_guid = 61
      )
    }

    Building21()

    def Building21(): Unit = { // Name: SE_Heyoka_Tower Type: tower_b GUID: 62, MapID: 21
      LocalBuilding(
        "SE_Heyoka_Tower",
        62,
        21,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4456f, 2654f, 69.41827f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2397,
        CaptureTerminal.Constructor(Vector3(4472.587f, 2653.897f, 89.41727f), secondary_capture),
        owning_building_guid = 62
      )
      LocalObject(395, Door.Constructor(Vector3(4468f, 2646f, 70.93827f)), owning_building_guid = 62)
      LocalObject(396, Door.Constructor(Vector3(4468f, 2646f, 80.93828f)), owning_building_guid = 62)
      LocalObject(397, Door.Constructor(Vector3(4468f, 2646f, 100.9383f)), owning_building_guid = 62)
      LocalObject(398, Door.Constructor(Vector3(4468f, 2662f, 70.93827f)), owning_building_guid = 62)
      LocalObject(399, Door.Constructor(Vector3(4468f, 2662f, 80.93828f)), owning_building_guid = 62)
      LocalObject(400, Door.Constructor(Vector3(4468f, 2662f, 100.9383f)), owning_building_guid = 62)
      LocalObject(2527, Door.Constructor(Vector3(4467.147f, 2642.794f, 60.75427f)), owning_building_guid = 62)
      LocalObject(2528, Door.Constructor(Vector3(4467.147f, 2659.204f, 60.75427f)), owning_building_guid = 62)
      LocalObject(
        983,
        IFFLock.Constructor(Vector3(4465.957f, 2662.811f, 70.87927f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 398
      )
      LocalObject(
        984,
        IFFLock.Constructor(Vector3(4465.957f, 2662.811f, 80.87927f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 399
      )
      LocalObject(
        985,
        IFFLock.Constructor(Vector3(4465.957f, 2662.811f, 100.8793f), Vector3(0, 0, 0)),
        owning_building_guid = 62,
        door_guid = 400
      )
      LocalObject(
        986,
        IFFLock.Constructor(Vector3(4470.047f, 2645.189f, 70.87927f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 395
      )
      LocalObject(
        987,
        IFFLock.Constructor(Vector3(4470.047f, 2645.189f, 80.87927f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 396
      )
      LocalObject(
        988,
        IFFLock.Constructor(Vector3(4470.047f, 2645.189f, 100.8793f), Vector3(0, 0, 180)),
        owning_building_guid = 62,
        door_guid = 397
      )
      LocalObject(1269, Locker.Constructor(Vector3(4471.716f, 2638.963f, 59.41227f)), owning_building_guid = 62)
      LocalObject(1270, Locker.Constructor(Vector3(4471.751f, 2660.835f, 59.41227f)), owning_building_guid = 62)
      LocalObject(1271, Locker.Constructor(Vector3(4473.053f, 2638.963f, 59.41227f)), owning_building_guid = 62)
      LocalObject(1272, Locker.Constructor(Vector3(4473.088f, 2660.835f, 59.41227f)), owning_building_guid = 62)
      LocalObject(1273, Locker.Constructor(Vector3(4475.741f, 2638.963f, 59.41227f)), owning_building_guid = 62)
      LocalObject(1274, Locker.Constructor(Vector3(4475.741f, 2660.835f, 59.41227f)), owning_building_guid = 62)
      LocalObject(1275, Locker.Constructor(Vector3(4477.143f, 2638.963f, 59.41227f)), owning_building_guid = 62)
      LocalObject(1276, Locker.Constructor(Vector3(4477.143f, 2660.835f, 59.41227f)), owning_building_guid = 62)
      LocalObject(
        1717,
        Terminal.Constructor(Vector3(4477.446f, 2644.129f, 60.75027f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        1718,
        Terminal.Constructor(Vector3(4477.446f, 2649.853f, 60.75027f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        1719,
        Terminal.Constructor(Vector3(4477.446f, 2655.234f, 60.75027f), order_terminal),
        owning_building_guid = 62
      )
      LocalObject(
        2340,
        SpawnTube.Constructor(Vector3(4466.706f, 2641.742f, 58.90028f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        2341,
        SpawnTube.Constructor(Vector3(4466.706f, 2658.152f, 58.90028f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 62
      )
      LocalObject(
        2158,
        Painbox.Constructor(Vector3(4461.493f, 2646.849f, 60.70767f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2159,
        Painbox.Constructor(Vector3(4473.127f, 2644.078f, 59.51827f), painbox_radius_continuous),
        owning_building_guid = 62
      )
      LocalObject(
        2160,
        Painbox.Constructor(Vector3(4473.259f, 2656.107f, 59.51827f), painbox_radius_continuous),
        owning_building_guid = 62
      )
    }

    Building24()

    def Building24(): Unit = { // Name: NE_Heyoka_Tower Type: tower_b GUID: 63, MapID: 24
      LocalBuilding(
        "NE_Heyoka_Tower",
        63,
        24,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4702f, 3278f, 55.6656f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2398,
        CaptureTerminal.Constructor(Vector3(4718.587f, 3277.897f, 75.6646f), secondary_capture),
        owning_building_guid = 63
      )
      LocalObject(423, Door.Constructor(Vector3(4714f, 3270f, 57.1856f)), owning_building_guid = 63)
      LocalObject(424, Door.Constructor(Vector3(4714f, 3270f, 67.18559f)), owning_building_guid = 63)
      LocalObject(425, Door.Constructor(Vector3(4714f, 3270f, 87.18559f)), owning_building_guid = 63)
      LocalObject(426, Door.Constructor(Vector3(4714f, 3286f, 57.1856f)), owning_building_guid = 63)
      LocalObject(427, Door.Constructor(Vector3(4714f, 3286f, 67.18559f)), owning_building_guid = 63)
      LocalObject(428, Door.Constructor(Vector3(4714f, 3286f, 87.18559f)), owning_building_guid = 63)
      LocalObject(2535, Door.Constructor(Vector3(4713.147f, 3266.794f, 47.00159f)), owning_building_guid = 63)
      LocalObject(2536, Door.Constructor(Vector3(4713.147f, 3283.204f, 47.00159f)), owning_building_guid = 63)
      LocalObject(
        1005,
        IFFLock.Constructor(Vector3(4711.957f, 3286.811f, 57.12659f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 426
      )
      LocalObject(
        1006,
        IFFLock.Constructor(Vector3(4711.957f, 3286.811f, 67.12659f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 427
      )
      LocalObject(
        1007,
        IFFLock.Constructor(Vector3(4711.957f, 3286.811f, 87.12659f), Vector3(0, 0, 0)),
        owning_building_guid = 63,
        door_guid = 428
      )
      LocalObject(
        1009,
        IFFLock.Constructor(Vector3(4716.047f, 3269.189f, 57.12659f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 423
      )
      LocalObject(
        1010,
        IFFLock.Constructor(Vector3(4716.047f, 3269.189f, 67.12659f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 424
      )
      LocalObject(
        1011,
        IFFLock.Constructor(Vector3(4716.047f, 3269.189f, 87.12659f), Vector3(0, 0, 180)),
        owning_building_guid = 63,
        door_guid = 425
      )
      LocalObject(1301, Locker.Constructor(Vector3(4717.716f, 3262.963f, 45.6596f)), owning_building_guid = 63)
      LocalObject(1302, Locker.Constructor(Vector3(4717.751f, 3284.835f, 45.6596f)), owning_building_guid = 63)
      LocalObject(1303, Locker.Constructor(Vector3(4719.053f, 3262.963f, 45.6596f)), owning_building_guid = 63)
      LocalObject(1304, Locker.Constructor(Vector3(4719.088f, 3284.835f, 45.6596f)), owning_building_guid = 63)
      LocalObject(1305, Locker.Constructor(Vector3(4721.741f, 3262.963f, 45.6596f)), owning_building_guid = 63)
      LocalObject(1306, Locker.Constructor(Vector3(4721.741f, 3284.835f, 45.6596f)), owning_building_guid = 63)
      LocalObject(1307, Locker.Constructor(Vector3(4723.143f, 3262.963f, 45.6596f)), owning_building_guid = 63)
      LocalObject(1308, Locker.Constructor(Vector3(4723.143f, 3284.835f, 45.6596f)), owning_building_guid = 63)
      LocalObject(
        1730,
        Terminal.Constructor(Vector3(4723.446f, 3268.129f, 46.9976f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1731,
        Terminal.Constructor(Vector3(4723.446f, 3273.853f, 46.9976f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        1732,
        Terminal.Constructor(Vector3(4723.446f, 3279.234f, 46.9976f), order_terminal),
        owning_building_guid = 63
      )
      LocalObject(
        2348,
        SpawnTube.Constructor(Vector3(4712.706f, 3265.742f, 45.1476f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        2349,
        SpawnTube.Constructor(Vector3(4712.706f, 3282.152f, 45.1476f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 63
      )
      LocalObject(
        2161,
        Painbox.Constructor(Vector3(4707.493f, 3270.849f, 46.95499f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        2162,
        Painbox.Constructor(Vector3(4719.127f, 3268.078f, 45.76559f), painbox_radius_continuous),
        owning_building_guid = 63
      )
      LocalObject(
        2163,
        Painbox.Constructor(Vector3(4719.259f, 3280.107f, 45.76559f), painbox_radius_continuous),
        owning_building_guid = 63
      )
    }

    Building31()

    def Building31(): Unit = { // Name: SE_Tumas_Tower Type: tower_b GUID: 64, MapID: 31
      LocalBuilding(
        "SE_Tumas_Tower",
        64,
        31,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4988f, 6190f, 41.46329f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2401,
        CaptureTerminal.Constructor(Vector3(5004.587f, 6189.897f, 61.46229f), secondary_capture),
        owning_building_guid = 64
      )
      LocalObject(464, Door.Constructor(Vector3(5000f, 6182f, 42.98329f)), owning_building_guid = 64)
      LocalObject(465, Door.Constructor(Vector3(5000f, 6182f, 52.98329f)), owning_building_guid = 64)
      LocalObject(466, Door.Constructor(Vector3(5000f, 6182f, 72.98329f)), owning_building_guid = 64)
      LocalObject(467, Door.Constructor(Vector3(5000f, 6198f, 42.98329f)), owning_building_guid = 64)
      LocalObject(468, Door.Constructor(Vector3(5000f, 6198f, 52.98329f)), owning_building_guid = 64)
      LocalObject(469, Door.Constructor(Vector3(5000f, 6198f, 72.98329f)), owning_building_guid = 64)
      LocalObject(2544, Door.Constructor(Vector3(4999.147f, 6178.794f, 32.79929f)), owning_building_guid = 64)
      LocalObject(2545, Door.Constructor(Vector3(4999.147f, 6195.204f, 32.79929f)), owning_building_guid = 64)
      LocalObject(
        1033,
        IFFLock.Constructor(Vector3(4997.957f, 6198.811f, 42.92429f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 467
      )
      LocalObject(
        1034,
        IFFLock.Constructor(Vector3(4997.957f, 6198.811f, 52.92429f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 468
      )
      LocalObject(
        1035,
        IFFLock.Constructor(Vector3(4997.957f, 6198.811f, 72.92429f), Vector3(0, 0, 0)),
        owning_building_guid = 64,
        door_guid = 469
      )
      LocalObject(
        1036,
        IFFLock.Constructor(Vector3(5002.047f, 6181.189f, 42.92429f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 464
      )
      LocalObject(
        1037,
        IFFLock.Constructor(Vector3(5002.047f, 6181.189f, 52.92429f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 465
      )
      LocalObject(
        1038,
        IFFLock.Constructor(Vector3(5002.047f, 6181.189f, 72.92429f), Vector3(0, 0, 180)),
        owning_building_guid = 64,
        door_guid = 466
      )
      LocalObject(1337, Locker.Constructor(Vector3(5003.716f, 6174.963f, 31.45729f)), owning_building_guid = 64)
      LocalObject(1338, Locker.Constructor(Vector3(5003.751f, 6196.835f, 31.45729f)), owning_building_guid = 64)
      LocalObject(1339, Locker.Constructor(Vector3(5005.053f, 6174.963f, 31.45729f)), owning_building_guid = 64)
      LocalObject(1340, Locker.Constructor(Vector3(5005.088f, 6196.835f, 31.45729f)), owning_building_guid = 64)
      LocalObject(1341, Locker.Constructor(Vector3(5007.741f, 6174.963f, 31.45729f)), owning_building_guid = 64)
      LocalObject(1342, Locker.Constructor(Vector3(5007.741f, 6196.835f, 31.45729f)), owning_building_guid = 64)
      LocalObject(1343, Locker.Constructor(Vector3(5009.143f, 6174.963f, 31.45729f)), owning_building_guid = 64)
      LocalObject(1344, Locker.Constructor(Vector3(5009.143f, 6196.835f, 31.45729f)), owning_building_guid = 64)
      LocalObject(
        1745,
        Terminal.Constructor(Vector3(5009.446f, 6180.129f, 32.79529f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1746,
        Terminal.Constructor(Vector3(5009.446f, 6185.853f, 32.79529f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        1747,
        Terminal.Constructor(Vector3(5009.446f, 6191.234f, 32.79529f), order_terminal),
        owning_building_guid = 64
      )
      LocalObject(
        2357,
        SpawnTube.Constructor(Vector3(4998.706f, 6177.742f, 30.94529f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 64
      )
      LocalObject(
        2358,
        SpawnTube.Constructor(Vector3(4998.706f, 6194.152f, 30.94529f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 64
      )
      LocalObject(
        2168,
        Painbox.Constructor(Vector3(4993.493f, 6182.849f, 32.75269f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        2171,
        Painbox.Constructor(Vector3(5005.127f, 6180.078f, 31.56329f), painbox_radius_continuous),
        owning_building_guid = 64
      )
      LocalObject(
        2172,
        Painbox.Constructor(Vector3(5005.259f, 6192.107f, 31.56329f), painbox_radius_continuous),
        owning_building_guid = 64
      )
    }

    Building25()

    def Building25(): Unit = { // Name: S_Oshur_Warpgate_Tower Type: tower_b GUID: 65, MapID: 25
      LocalBuilding(
        "S_Oshur_Warpgate_Tower",
        65,
        25,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5122f, 4320f, 59.33499f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        2402,
        CaptureTerminal.Constructor(Vector3(5138.587f, 4319.897f, 79.33399f), secondary_capture),
        owning_building_guid = 65
      )
      LocalObject(470, Door.Constructor(Vector3(5134f, 4312f, 60.85499f)), owning_building_guid = 65)
      LocalObject(471, Door.Constructor(Vector3(5134f, 4312f, 70.855f)), owning_building_guid = 65)
      LocalObject(472, Door.Constructor(Vector3(5134f, 4312f, 90.855f)), owning_building_guid = 65)
      LocalObject(473, Door.Constructor(Vector3(5134f, 4328f, 60.85499f)), owning_building_guid = 65)
      LocalObject(474, Door.Constructor(Vector3(5134f, 4328f, 70.855f)), owning_building_guid = 65)
      LocalObject(475, Door.Constructor(Vector3(5134f, 4328f, 90.855f)), owning_building_guid = 65)
      LocalObject(2546, Door.Constructor(Vector3(5133.147f, 4308.794f, 50.67099f)), owning_building_guid = 65)
      LocalObject(2547, Door.Constructor(Vector3(5133.147f, 4325.204f, 50.67099f)), owning_building_guid = 65)
      LocalObject(
        1039,
        IFFLock.Constructor(Vector3(5131.957f, 4328.811f, 60.79599f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 473
      )
      LocalObject(
        1040,
        IFFLock.Constructor(Vector3(5131.957f, 4328.811f, 70.79599f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 474
      )
      LocalObject(
        1041,
        IFFLock.Constructor(Vector3(5131.957f, 4328.811f, 90.79599f), Vector3(0, 0, 0)),
        owning_building_guid = 65,
        door_guid = 475
      )
      LocalObject(
        1042,
        IFFLock.Constructor(Vector3(5136.047f, 4311.189f, 60.79599f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 470
      )
      LocalObject(
        1043,
        IFFLock.Constructor(Vector3(5136.047f, 4311.189f, 70.79599f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 471
      )
      LocalObject(
        1044,
        IFFLock.Constructor(Vector3(5136.047f, 4311.189f, 90.79599f), Vector3(0, 0, 180)),
        owning_building_guid = 65,
        door_guid = 472
      )
      LocalObject(1345, Locker.Constructor(Vector3(5137.716f, 4304.963f, 49.32899f)), owning_building_guid = 65)
      LocalObject(1346, Locker.Constructor(Vector3(5137.751f, 4326.835f, 49.32899f)), owning_building_guid = 65)
      LocalObject(1347, Locker.Constructor(Vector3(5139.053f, 4304.963f, 49.32899f)), owning_building_guid = 65)
      LocalObject(1348, Locker.Constructor(Vector3(5139.088f, 4326.835f, 49.32899f)), owning_building_guid = 65)
      LocalObject(1349, Locker.Constructor(Vector3(5141.741f, 4304.963f, 49.32899f)), owning_building_guid = 65)
      LocalObject(1350, Locker.Constructor(Vector3(5141.741f, 4326.835f, 49.32899f)), owning_building_guid = 65)
      LocalObject(1351, Locker.Constructor(Vector3(5143.143f, 4304.963f, 49.32899f)), owning_building_guid = 65)
      LocalObject(1352, Locker.Constructor(Vector3(5143.143f, 4326.835f, 49.32899f)), owning_building_guid = 65)
      LocalObject(
        1748,
        Terminal.Constructor(Vector3(5143.446f, 4310.129f, 50.66699f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1749,
        Terminal.Constructor(Vector3(5143.446f, 4315.853f, 50.66699f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        1750,
        Terminal.Constructor(Vector3(5143.446f, 4321.234f, 50.66699f), order_terminal),
        owning_building_guid = 65
      )
      LocalObject(
        2359,
        SpawnTube.Constructor(Vector3(5132.706f, 4307.742f, 48.81699f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        2360,
        SpawnTube.Constructor(Vector3(5132.706f, 4324.152f, 48.81699f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 65
      )
      LocalObject(
        2173,
        Painbox.Constructor(Vector3(5127.493f, 4312.849f, 50.62439f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        2174,
        Painbox.Constructor(Vector3(5139.127f, 4310.078f, 49.43499f), painbox_radius_continuous),
        owning_building_guid = 65
      )
      LocalObject(
        2175,
        Painbox.Constructor(Vector3(5139.259f, 4322.107f, 49.43499f), painbox_radius_continuous),
        owning_building_guid = 65
      )
    }

    Building17()

    def Building17(): Unit = { // Name: NW_Ikanam_Tower Type: tower_c GUID: 66, MapID: 17
      LocalBuilding(
        "NW_Ikanam_Tower",
        66,
        17,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2224f, 2782f, 52.59641f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2388,
        CaptureTerminal.Constructor(Vector3(2240.587f, 2781.897f, 62.59541f), secondary_capture),
        owning_building_guid = 66
      )
      LocalObject(290, Door.Constructor(Vector3(2236f, 2774f, 54.11741f)), owning_building_guid = 66)
      LocalObject(291, Door.Constructor(Vector3(2236f, 2774f, 74.11641f)), owning_building_guid = 66)
      LocalObject(292, Door.Constructor(Vector3(2236f, 2790f, 54.11741f)), owning_building_guid = 66)
      LocalObject(293, Door.Constructor(Vector3(2236f, 2790f, 74.11641f)), owning_building_guid = 66)
      LocalObject(2494, Door.Constructor(Vector3(2235.146f, 2770.794f, 43.93241f)), owning_building_guid = 66)
      LocalObject(2495, Door.Constructor(Vector3(2235.146f, 2787.204f, 43.93241f)), owning_building_guid = 66)
      LocalObject(
        899,
        IFFLock.Constructor(Vector3(2233.957f, 2790.811f, 54.05741f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 292
      )
      LocalObject(
        900,
        IFFLock.Constructor(Vector3(2233.957f, 2790.811f, 74.05741f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 293
      )
      LocalObject(
        901,
        IFFLock.Constructor(Vector3(2238.047f, 2773.189f, 54.05741f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 290
      )
      LocalObject(
        902,
        IFFLock.Constructor(Vector3(2238.047f, 2773.189f, 74.05741f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 291
      )
      LocalObject(1119, Locker.Constructor(Vector3(2239.716f, 2766.963f, 42.59041f)), owning_building_guid = 66)
      LocalObject(1120, Locker.Constructor(Vector3(2239.751f, 2788.835f, 42.59041f)), owning_building_guid = 66)
      LocalObject(1121, Locker.Constructor(Vector3(2241.053f, 2766.963f, 42.59041f)), owning_building_guid = 66)
      LocalObject(1122, Locker.Constructor(Vector3(2241.088f, 2788.835f, 42.59041f)), owning_building_guid = 66)
      LocalObject(1123, Locker.Constructor(Vector3(2243.741f, 2766.963f, 42.59041f)), owning_building_guid = 66)
      LocalObject(1124, Locker.Constructor(Vector3(2243.741f, 2788.835f, 42.59041f)), owning_building_guid = 66)
      LocalObject(1125, Locker.Constructor(Vector3(2245.143f, 2766.963f, 42.59041f)), owning_building_guid = 66)
      LocalObject(1126, Locker.Constructor(Vector3(2245.143f, 2788.835f, 42.59041f)), owning_building_guid = 66)
      LocalObject(
        1664,
        Terminal.Constructor(Vector3(2245.445f, 2772.129f, 43.92841f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1665,
        Terminal.Constructor(Vector3(2245.445f, 2777.853f, 43.92841f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1666,
        Terminal.Constructor(Vector3(2245.445f, 2783.234f, 43.92841f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2307,
        SpawnTube.Constructor(Vector3(2234.706f, 2769.742f, 42.07841f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 66
      )
      LocalObject(
        2308,
        SpawnTube.Constructor(Vector3(2234.706f, 2786.152f, 42.07841f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 66
      )
      LocalObject(
        2002,
        ProximityTerminal.Constructor(Vector3(2222.907f, 2776.725f, 80.16641f), pad_landing_tower_frame),
        owning_building_guid = 66
      )
      LocalObject(
        2003,
        Terminal.Constructor(Vector3(2222.907f, 2776.725f, 80.16641f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2005,
        ProximityTerminal.Constructor(Vector3(2222.907f, 2787.17f, 80.16641f), pad_landing_tower_frame),
        owning_building_guid = 66
      )
      LocalObject(
        2006,
        Terminal.Constructor(Vector3(2222.907f, 2787.17f, 80.16641f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        1519,
        FacilityTurret.Constructor(Vector3(2209.07f, 2767.045f, 71.53841f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(1519, 5086)
      LocalObject(
        1520,
        FacilityTurret.Constructor(Vector3(2247.497f, 2796.957f, 71.53841f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(1520, 5087)
      LocalObject(
        2131,
        Painbox.Constructor(Vector3(2228.454f, 2774.849f, 44.61591f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2132,
        Painbox.Constructor(Vector3(2240.923f, 2771.54f, 42.69641f), painbox_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2133,
        Painbox.Constructor(Vector3(2241.113f, 2784.022f, 42.69641f), painbox_radius_continuous),
        owning_building_guid = 66
      )
    }

    Building35()

    def Building35(): Unit = { // Name: N_Onatha_Tower Type: tower_c GUID: 67, MapID: 35
      LocalBuilding(
        "N_Onatha_Tower",
        67,
        35,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3352f, 6674f, 57.33703f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2390,
        CaptureTerminal.Constructor(Vector3(3368.587f, 6673.897f, 67.33603f), secondary_capture),
        owning_building_guid = 67
      )
      LocalObject(316, Door.Constructor(Vector3(3364f, 6666f, 58.85803f)), owning_building_guid = 67)
      LocalObject(317, Door.Constructor(Vector3(3364f, 6666f, 78.85703f)), owning_building_guid = 67)
      LocalObject(318, Door.Constructor(Vector3(3364f, 6682f, 58.85803f)), owning_building_guid = 67)
      LocalObject(319, Door.Constructor(Vector3(3364f, 6682f, 78.85703f)), owning_building_guid = 67)
      LocalObject(2504, Door.Constructor(Vector3(3363.146f, 6662.794f, 48.67303f)), owning_building_guid = 67)
      LocalObject(2505, Door.Constructor(Vector3(3363.146f, 6679.204f, 48.67303f)), owning_building_guid = 67)
      LocalObject(
        920,
        IFFLock.Constructor(Vector3(3361.957f, 6682.811f, 58.79803f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 318
      )
      LocalObject(
        921,
        IFFLock.Constructor(Vector3(3361.957f, 6682.811f, 78.79803f), Vector3(0, 0, 0)),
        owning_building_guid = 67,
        door_guid = 319
      )
      LocalObject(
        923,
        IFFLock.Constructor(Vector3(3366.047f, 6665.189f, 58.79803f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 316
      )
      LocalObject(
        924,
        IFFLock.Constructor(Vector3(3366.047f, 6665.189f, 78.79803f), Vector3(0, 0, 180)),
        owning_building_guid = 67,
        door_guid = 317
      )
      LocalObject(1158, Locker.Constructor(Vector3(3367.716f, 6658.963f, 47.33103f)), owning_building_guid = 67)
      LocalObject(1159, Locker.Constructor(Vector3(3367.751f, 6680.835f, 47.33103f)), owning_building_guid = 67)
      LocalObject(1162, Locker.Constructor(Vector3(3369.053f, 6658.963f, 47.33103f)), owning_building_guid = 67)
      LocalObject(1163, Locker.Constructor(Vector3(3369.088f, 6680.835f, 47.33103f)), owning_building_guid = 67)
      LocalObject(1169, Locker.Constructor(Vector3(3371.741f, 6658.963f, 47.33103f)), owning_building_guid = 67)
      LocalObject(1170, Locker.Constructor(Vector3(3371.741f, 6680.835f, 47.33103f)), owning_building_guid = 67)
      LocalObject(1171, Locker.Constructor(Vector3(3373.143f, 6658.963f, 47.33103f)), owning_building_guid = 67)
      LocalObject(1172, Locker.Constructor(Vector3(3373.143f, 6680.835f, 47.33103f)), owning_building_guid = 67)
      LocalObject(
        1678,
        Terminal.Constructor(Vector3(3373.445f, 6664.129f, 48.66903f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1679,
        Terminal.Constructor(Vector3(3373.445f, 6669.853f, 48.66903f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1680,
        Terminal.Constructor(Vector3(3373.445f, 6675.234f, 48.66903f), order_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        2317,
        SpawnTube.Constructor(Vector3(3362.706f, 6661.742f, 46.81903f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        2318,
        SpawnTube.Constructor(Vector3(3362.706f, 6678.152f, 46.81903f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 67
      )
      LocalObject(
        2008,
        ProximityTerminal.Constructor(Vector3(3350.907f, 6668.725f, 84.90703f), pad_landing_tower_frame),
        owning_building_guid = 67
      )
      LocalObject(
        2009,
        Terminal.Constructor(Vector3(3350.907f, 6668.725f, 84.90703f), air_rearm_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        2011,
        ProximityTerminal.Constructor(Vector3(3350.907f, 6679.17f, 84.90703f), pad_landing_tower_frame),
        owning_building_guid = 67
      )
      LocalObject(
        2012,
        Terminal.Constructor(Vector3(3350.907f, 6679.17f, 84.90703f), air_rearm_terminal),
        owning_building_guid = 67
      )
      LocalObject(
        1530,
        FacilityTurret.Constructor(Vector3(3337.07f, 6659.045f, 76.27903f), manned_turret),
        owning_building_guid = 67
      )
      TurretToWeapon(1530, 5088)
      LocalObject(
        1533,
        FacilityTurret.Constructor(Vector3(3375.497f, 6688.957f, 76.27903f), manned_turret),
        owning_building_guid = 67
      )
      TurretToWeapon(1533, 5089)
      LocalObject(
        2137,
        Painbox.Constructor(Vector3(3356.454f, 6666.849f, 49.35653f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        2138,
        Painbox.Constructor(Vector3(3368.923f, 6663.54f, 47.43703f), painbox_radius_continuous),
        owning_building_guid = 67
      )
      LocalObject(
        2139,
        Painbox.Constructor(Vector3(3369.113f, 6676.022f, 47.43703f), painbox_radius_continuous),
        owning_building_guid = 67
      )
    }

    Building18()

    def Building18(): Unit = { // Name: NW_Cetan_Tower Type: tower_c GUID: 68, MapID: 18
      LocalBuilding(
        "NW_Cetan_Tower",
        68,
        18,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3368f, 3472f, 49.55655f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2391,
        CaptureTerminal.Constructor(Vector3(3384.587f, 3471.897f, 59.55555f), secondary_capture),
        owning_building_guid = 68
      )
      LocalObject(321, Door.Constructor(Vector3(3380f, 3464f, 51.07755f)), owning_building_guid = 68)
      LocalObject(322, Door.Constructor(Vector3(3380f, 3464f, 71.07655f)), owning_building_guid = 68)
      LocalObject(323, Door.Constructor(Vector3(3380f, 3480f, 51.07755f)), owning_building_guid = 68)
      LocalObject(324, Door.Constructor(Vector3(3380f, 3480f, 71.07655f)), owning_building_guid = 68)
      LocalObject(2506, Door.Constructor(Vector3(3379.146f, 3460.794f, 40.89256f)), owning_building_guid = 68)
      LocalObject(2507, Door.Constructor(Vector3(3379.146f, 3477.204f, 40.89256f)), owning_building_guid = 68)
      LocalObject(
        926,
        IFFLock.Constructor(Vector3(3377.957f, 3480.811f, 51.01755f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 323
      )
      LocalObject(
        927,
        IFFLock.Constructor(Vector3(3377.957f, 3480.811f, 71.01756f), Vector3(0, 0, 0)),
        owning_building_guid = 68,
        door_guid = 324
      )
      LocalObject(
        928,
        IFFLock.Constructor(Vector3(3382.047f, 3463.189f, 51.01755f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 321
      )
      LocalObject(
        929,
        IFFLock.Constructor(Vector3(3382.047f, 3463.189f, 71.01756f), Vector3(0, 0, 180)),
        owning_building_guid = 68,
        door_guid = 322
      )
      LocalObject(1175, Locker.Constructor(Vector3(3383.716f, 3456.963f, 39.55055f)), owning_building_guid = 68)
      LocalObject(1176, Locker.Constructor(Vector3(3383.751f, 3478.835f, 39.55055f)), owning_building_guid = 68)
      LocalObject(1181, Locker.Constructor(Vector3(3385.053f, 3456.963f, 39.55055f)), owning_building_guid = 68)
      LocalObject(1182, Locker.Constructor(Vector3(3385.088f, 3478.835f, 39.55055f)), owning_building_guid = 68)
      LocalObject(1187, Locker.Constructor(Vector3(3387.741f, 3456.963f, 39.55055f)), owning_building_guid = 68)
      LocalObject(1188, Locker.Constructor(Vector3(3387.741f, 3478.835f, 39.55055f)), owning_building_guid = 68)
      LocalObject(1191, Locker.Constructor(Vector3(3389.143f, 3456.963f, 39.55055f)), owning_building_guid = 68)
      LocalObject(1192, Locker.Constructor(Vector3(3389.143f, 3478.835f, 39.55055f)), owning_building_guid = 68)
      LocalObject(
        1681,
        Terminal.Constructor(Vector3(3389.445f, 3462.129f, 40.88855f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1682,
        Terminal.Constructor(Vector3(3389.445f, 3467.853f, 40.88855f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1683,
        Terminal.Constructor(Vector3(3389.445f, 3473.234f, 40.88855f), order_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        2319,
        SpawnTube.Constructor(Vector3(3378.706f, 3459.742f, 39.03855f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        2320,
        SpawnTube.Constructor(Vector3(3378.706f, 3476.152f, 39.03855f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 68
      )
      LocalObject(
        2014,
        ProximityTerminal.Constructor(Vector3(3366.907f, 3466.725f, 77.12656f), pad_landing_tower_frame),
        owning_building_guid = 68
      )
      LocalObject(
        2015,
        Terminal.Constructor(Vector3(3366.907f, 3466.725f, 77.12656f), air_rearm_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        2017,
        ProximityTerminal.Constructor(Vector3(3366.907f, 3477.17f, 77.12656f), pad_landing_tower_frame),
        owning_building_guid = 68
      )
      LocalObject(
        2018,
        Terminal.Constructor(Vector3(3366.907f, 3477.17f, 77.12656f), air_rearm_terminal),
        owning_building_guid = 68
      )
      LocalObject(
        1531,
        FacilityTurret.Constructor(Vector3(3353.07f, 3457.045f, 68.49855f), manned_turret),
        owning_building_guid = 68
      )
      TurretToWeapon(1531, 5090)
      LocalObject(
        1534,
        FacilityTurret.Constructor(Vector3(3391.497f, 3486.957f, 68.49855f), manned_turret),
        owning_building_guid = 68
      )
      TurretToWeapon(1534, 5091)
      LocalObject(
        2140,
        Painbox.Constructor(Vector3(3372.454f, 3464.849f, 41.57605f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        2141,
        Painbox.Constructor(Vector3(3384.923f, 3461.54f, 39.65656f), painbox_radius_continuous),
        owning_building_guid = 68
      )
      LocalObject(
        2142,
        Painbox.Constructor(Vector3(3385.113f, 3474.022f, 39.65656f), painbox_radius_continuous),
        owning_building_guid = 68
      )
    }

    Building47()

    def Building47(): Unit = { // Name: Cetan_Tower Type: tower_c GUID: 69, MapID: 47
      LocalBuilding(
        "Cetan_Tower",
        69,
        47,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3686f, 2664f, 68.50925f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2394,
        CaptureTerminal.Constructor(Vector3(3702.587f, 2663.897f, 78.50825f), secondary_capture),
        owning_building_guid = 69
      )
      LocalObject(352, Door.Constructor(Vector3(3698f, 2656f, 70.03025f)), owning_building_guid = 69)
      LocalObject(353, Door.Constructor(Vector3(3698f, 2656f, 90.02925f)), owning_building_guid = 69)
      LocalObject(354, Door.Constructor(Vector3(3698f, 2672f, 70.03025f)), owning_building_guid = 69)
      LocalObject(355, Door.Constructor(Vector3(3698f, 2672f, 90.02925f)), owning_building_guid = 69)
      LocalObject(2515, Door.Constructor(Vector3(3697.146f, 2652.794f, 59.84525f)), owning_building_guid = 69)
      LocalObject(2516, Door.Constructor(Vector3(3697.146f, 2669.204f, 59.84525f)), owning_building_guid = 69)
      LocalObject(
        952,
        IFFLock.Constructor(Vector3(3695.957f, 2672.811f, 69.97025f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 354
      )
      LocalObject(
        953,
        IFFLock.Constructor(Vector3(3695.957f, 2672.811f, 89.97025f), Vector3(0, 0, 0)),
        owning_building_guid = 69,
        door_guid = 355
      )
      LocalObject(
        954,
        IFFLock.Constructor(Vector3(3700.047f, 2655.189f, 69.97025f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 352
      )
      LocalObject(
        955,
        IFFLock.Constructor(Vector3(3700.047f, 2655.189f, 89.97025f), Vector3(0, 0, 180)),
        owning_building_guid = 69,
        door_guid = 353
      )
      LocalObject(1221, Locker.Constructor(Vector3(3701.716f, 2648.963f, 58.50325f)), owning_building_guid = 69)
      LocalObject(1222, Locker.Constructor(Vector3(3701.751f, 2670.835f, 58.50325f)), owning_building_guid = 69)
      LocalObject(1223, Locker.Constructor(Vector3(3703.053f, 2648.963f, 58.50325f)), owning_building_guid = 69)
      LocalObject(1224, Locker.Constructor(Vector3(3703.088f, 2670.835f, 58.50325f)), owning_building_guid = 69)
      LocalObject(1225, Locker.Constructor(Vector3(3705.741f, 2648.963f, 58.50325f)), owning_building_guid = 69)
      LocalObject(1226, Locker.Constructor(Vector3(3705.741f, 2670.835f, 58.50325f)), owning_building_guid = 69)
      LocalObject(1227, Locker.Constructor(Vector3(3707.143f, 2648.963f, 58.50325f)), owning_building_guid = 69)
      LocalObject(1228, Locker.Constructor(Vector3(3707.143f, 2670.835f, 58.50325f)), owning_building_guid = 69)
      LocalObject(
        1697,
        Terminal.Constructor(Vector3(3707.445f, 2654.129f, 59.84125f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1698,
        Terminal.Constructor(Vector3(3707.445f, 2659.853f, 59.84125f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1699,
        Terminal.Constructor(Vector3(3707.445f, 2665.234f, 59.84125f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2328,
        SpawnTube.Constructor(Vector3(3696.706f, 2651.742f, 57.99125f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        2329,
        SpawnTube.Constructor(Vector3(3696.706f, 2668.152f, 57.99125f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 69
      )
      LocalObject(
        2020,
        ProximityTerminal.Constructor(Vector3(3684.907f, 2658.725f, 96.07925f), pad_landing_tower_frame),
        owning_building_guid = 69
      )
      LocalObject(
        2021,
        Terminal.Constructor(Vector3(3684.907f, 2658.725f, 96.07925f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2023,
        ProximityTerminal.Constructor(Vector3(3684.907f, 2669.17f, 96.07925f), pad_landing_tower_frame),
        owning_building_guid = 69
      )
      LocalObject(
        2024,
        Terminal.Constructor(Vector3(3684.907f, 2669.17f, 96.07925f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        1545,
        FacilityTurret.Constructor(Vector3(3671.07f, 2649.045f, 87.45125f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(1545, 5092)
      LocalObject(
        1546,
        FacilityTurret.Constructor(Vector3(3709.497f, 2678.957f, 87.45125f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(1546, 5093)
      LocalObject(
        2149,
        Painbox.Constructor(Vector3(3690.454f, 2656.849f, 60.52875f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2150,
        Painbox.Constructor(Vector3(3702.923f, 2653.54f, 58.60925f), painbox_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2151,
        Painbox.Constructor(Vector3(3703.113f, 2666.022f, 58.60925f), painbox_radius_continuous),
        owning_building_guid = 69
      )
    }

    Building33()

    def Building33(): Unit = { // Name: NW_Oshur_Warpgate_Tower Type: tower_c GUID: 70, MapID: 33
      LocalBuilding(
        "NW_Oshur_Warpgate_Tower",
        70,
        33,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4000f, 5430f, 66.66187f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2396,
        CaptureTerminal.Constructor(Vector3(4016.587f, 5429.897f, 76.66087f), secondary_capture),
        owning_building_guid = 70
      )
      LocalObject(375, Door.Constructor(Vector3(4012f, 5422f, 68.18288f)), owning_building_guid = 70)
      LocalObject(376, Door.Constructor(Vector3(4012f, 5422f, 88.18187f)), owning_building_guid = 70)
      LocalObject(377, Door.Constructor(Vector3(4012f, 5438f, 68.18288f)), owning_building_guid = 70)
      LocalObject(378, Door.Constructor(Vector3(4012f, 5438f, 88.18187f)), owning_building_guid = 70)
      LocalObject(2522, Door.Constructor(Vector3(4011.146f, 5418.794f, 57.99787f)), owning_building_guid = 70)
      LocalObject(2523, Door.Constructor(Vector3(4011.146f, 5435.204f, 57.99787f)), owning_building_guid = 70)
      LocalObject(
        971,
        IFFLock.Constructor(Vector3(4009.957f, 5438.811f, 68.12287f), Vector3(0, 0, 0)),
        owning_building_guid = 70,
        door_guid = 377
      )
      LocalObject(
        972,
        IFFLock.Constructor(Vector3(4009.957f, 5438.811f, 88.12287f), Vector3(0, 0, 0)),
        owning_building_guid = 70,
        door_guid = 378
      )
      LocalObject(
        973,
        IFFLock.Constructor(Vector3(4014.047f, 5421.189f, 68.12287f), Vector3(0, 0, 180)),
        owning_building_guid = 70,
        door_guid = 375
      )
      LocalObject(
        974,
        IFFLock.Constructor(Vector3(4014.047f, 5421.189f, 88.12287f), Vector3(0, 0, 180)),
        owning_building_guid = 70,
        door_guid = 376
      )
      LocalObject(1249, Locker.Constructor(Vector3(4015.716f, 5414.963f, 56.65587f)), owning_building_guid = 70)
      LocalObject(1250, Locker.Constructor(Vector3(4015.751f, 5436.835f, 56.65587f)), owning_building_guid = 70)
      LocalObject(1251, Locker.Constructor(Vector3(4017.053f, 5414.963f, 56.65587f)), owning_building_guid = 70)
      LocalObject(1252, Locker.Constructor(Vector3(4017.088f, 5436.835f, 56.65587f)), owning_building_guid = 70)
      LocalObject(1253, Locker.Constructor(Vector3(4019.741f, 5414.963f, 56.65587f)), owning_building_guid = 70)
      LocalObject(1254, Locker.Constructor(Vector3(4019.741f, 5436.835f, 56.65587f)), owning_building_guid = 70)
      LocalObject(1255, Locker.Constructor(Vector3(4021.143f, 5414.963f, 56.65587f)), owning_building_guid = 70)
      LocalObject(1256, Locker.Constructor(Vector3(4021.143f, 5436.835f, 56.65587f)), owning_building_guid = 70)
      LocalObject(
        1710,
        Terminal.Constructor(Vector3(4021.445f, 5420.129f, 57.99387f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1711,
        Terminal.Constructor(Vector3(4021.445f, 5425.853f, 57.99387f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1712,
        Terminal.Constructor(Vector3(4021.445f, 5431.234f, 57.99387f), order_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        2335,
        SpawnTube.Constructor(Vector3(4010.706f, 5417.742f, 56.14388f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 70
      )
      LocalObject(
        2336,
        SpawnTube.Constructor(Vector3(4010.706f, 5434.152f, 56.14388f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 70
      )
      LocalObject(
        2026,
        ProximityTerminal.Constructor(Vector3(3998.907f, 5424.725f, 94.23187f), pad_landing_tower_frame),
        owning_building_guid = 70
      )
      LocalObject(
        2027,
        Terminal.Constructor(Vector3(3998.907f, 5424.725f, 94.23187f), air_rearm_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        2029,
        ProximityTerminal.Constructor(Vector3(3998.907f, 5435.17f, 94.23187f), pad_landing_tower_frame),
        owning_building_guid = 70
      )
      LocalObject(
        2030,
        Terminal.Constructor(Vector3(3998.907f, 5435.17f, 94.23187f), air_rearm_terminal),
        owning_building_guid = 70
      )
      LocalObject(
        1555,
        FacilityTurret.Constructor(Vector3(3985.07f, 5415.045f, 85.60387f), manned_turret),
        owning_building_guid = 70
      )
      TurretToWeapon(1555, 5094)
      LocalObject(
        1556,
        FacilityTurret.Constructor(Vector3(4023.497f, 5444.957f, 85.60387f), manned_turret),
        owning_building_guid = 70
      )
      TurretToWeapon(1556, 5095)
      LocalObject(
        2155,
        Painbox.Constructor(Vector3(4004.454f, 5422.849f, 58.68137f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2156,
        Painbox.Constructor(Vector3(4016.923f, 5419.54f, 56.76187f), painbox_radius_continuous),
        owning_building_guid = 70
      )
      LocalObject(
        2157,
        Painbox.Constructor(Vector3(4017.113f, 5432.022f, 56.76187f), painbox_radius_continuous),
        owning_building_guid = 70
      )
    }

    Building22()

    def Building22(): Unit = { // Name: NW_Kyoi_Tower Type: tower_c GUID: 71, MapID: 22
      LocalBuilding(
        "NW_Kyoi_Tower",
        71,
        22,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4978f, 2646f, 62.90847f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2400,
        CaptureTerminal.Constructor(Vector3(4994.587f, 2645.897f, 72.90747f), secondary_capture),
        owning_building_guid = 71
      )
      LocalObject(458, Door.Constructor(Vector3(4990f, 2638f, 64.42947f)), owning_building_guid = 71)
      LocalObject(459, Door.Constructor(Vector3(4990f, 2638f, 84.42847f)), owning_building_guid = 71)
      LocalObject(460, Door.Constructor(Vector3(4990f, 2654f, 64.42947f)), owning_building_guid = 71)
      LocalObject(461, Door.Constructor(Vector3(4990f, 2654f, 84.42847f)), owning_building_guid = 71)
      LocalObject(2542, Door.Constructor(Vector3(4989.146f, 2634.794f, 54.24448f)), owning_building_guid = 71)
      LocalObject(2543, Door.Constructor(Vector3(4989.146f, 2651.204f, 54.24448f)), owning_building_guid = 71)
      LocalObject(
        1029,
        IFFLock.Constructor(Vector3(4987.957f, 2654.811f, 64.36948f), Vector3(0, 0, 0)),
        owning_building_guid = 71,
        door_guid = 460
      )
      LocalObject(
        1030,
        IFFLock.Constructor(Vector3(4987.957f, 2654.811f, 84.36948f), Vector3(0, 0, 0)),
        owning_building_guid = 71,
        door_guid = 461
      )
      LocalObject(
        1031,
        IFFLock.Constructor(Vector3(4992.047f, 2637.189f, 64.36948f), Vector3(0, 0, 180)),
        owning_building_guid = 71,
        door_guid = 458
      )
      LocalObject(
        1032,
        IFFLock.Constructor(Vector3(4992.047f, 2637.189f, 84.36948f), Vector3(0, 0, 180)),
        owning_building_guid = 71,
        door_guid = 459
      )
      LocalObject(1329, Locker.Constructor(Vector3(4993.716f, 2630.963f, 52.90247f)), owning_building_guid = 71)
      LocalObject(1330, Locker.Constructor(Vector3(4993.751f, 2652.835f, 52.90247f)), owning_building_guid = 71)
      LocalObject(1331, Locker.Constructor(Vector3(4995.053f, 2630.963f, 52.90247f)), owning_building_guid = 71)
      LocalObject(1332, Locker.Constructor(Vector3(4995.088f, 2652.835f, 52.90247f)), owning_building_guid = 71)
      LocalObject(1333, Locker.Constructor(Vector3(4997.741f, 2630.963f, 52.90247f)), owning_building_guid = 71)
      LocalObject(1334, Locker.Constructor(Vector3(4997.741f, 2652.835f, 52.90247f)), owning_building_guid = 71)
      LocalObject(1335, Locker.Constructor(Vector3(4999.143f, 2630.963f, 52.90247f)), owning_building_guid = 71)
      LocalObject(1336, Locker.Constructor(Vector3(4999.143f, 2652.835f, 52.90247f)), owning_building_guid = 71)
      LocalObject(
        1742,
        Terminal.Constructor(Vector3(4999.445f, 2636.129f, 54.24047f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1743,
        Terminal.Constructor(Vector3(4999.445f, 2641.853f, 54.24047f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1744,
        Terminal.Constructor(Vector3(4999.445f, 2647.234f, 54.24047f), order_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        2355,
        SpawnTube.Constructor(Vector3(4988.706f, 2633.742f, 52.39047f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 71
      )
      LocalObject(
        2356,
        SpawnTube.Constructor(Vector3(4988.706f, 2650.152f, 52.39047f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 71
      )
      LocalObject(
        2032,
        ProximityTerminal.Constructor(Vector3(4976.907f, 2640.725f, 90.47847f), pad_landing_tower_frame),
        owning_building_guid = 71
      )
      LocalObject(
        2033,
        Terminal.Constructor(Vector3(4976.907f, 2640.725f, 90.47847f), air_rearm_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        2035,
        ProximityTerminal.Constructor(Vector3(4976.907f, 2651.17f, 90.47847f), pad_landing_tower_frame),
        owning_building_guid = 71
      )
      LocalObject(
        2036,
        Terminal.Constructor(Vector3(4976.907f, 2651.17f, 90.47847f), air_rearm_terminal),
        owning_building_guid = 71
      )
      LocalObject(
        1581,
        FacilityTurret.Constructor(Vector3(4963.07f, 2631.045f, 81.85047f), manned_turret),
        owning_building_guid = 71
      )
      TurretToWeapon(1581, 5096)
      LocalObject(
        1582,
        FacilityTurret.Constructor(Vector3(5001.497f, 2660.957f, 81.85047f), manned_turret),
        owning_building_guid = 71
      )
      TurretToWeapon(1582, 5097)
      LocalObject(
        2167,
        Painbox.Constructor(Vector3(4982.454f, 2638.849f, 54.92797f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2169,
        Painbox.Constructor(Vector3(4994.923f, 2635.54f, 53.00848f), painbox_radius_continuous),
        owning_building_guid = 71
      )
      LocalObject(
        2170,
        Painbox.Constructor(Vector3(4995.113f, 2648.022f, 53.00848f), painbox_radius_continuous),
        owning_building_guid = 71
      )
    }

    Building29()

    def Building29(): Unit = { // Name: W_Ceryshen_Warpgate_Tower Type: tower_c GUID: 72, MapID: 29
      LocalBuilding(
        "W_Ceryshen_Warpgate_Tower",
        72,
        29,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5424f, 7052f, 61.30546f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2403,
        CaptureTerminal.Constructor(Vector3(5440.587f, 7051.897f, 71.30446f), secondary_capture),
        owning_building_guid = 72
      )
      LocalObject(476, Door.Constructor(Vector3(5436f, 7044f, 62.82646f)), owning_building_guid = 72)
      LocalObject(477, Door.Constructor(Vector3(5436f, 7044f, 82.82546f)), owning_building_guid = 72)
      LocalObject(478, Door.Constructor(Vector3(5436f, 7060f, 62.82646f)), owning_building_guid = 72)
      LocalObject(479, Door.Constructor(Vector3(5436f, 7060f, 82.82546f)), owning_building_guid = 72)
      LocalObject(2548, Door.Constructor(Vector3(5435.146f, 7040.794f, 52.64146f)), owning_building_guid = 72)
      LocalObject(2549, Door.Constructor(Vector3(5435.146f, 7057.204f, 52.64146f)), owning_building_guid = 72)
      LocalObject(
        1045,
        IFFLock.Constructor(Vector3(5433.957f, 7060.811f, 62.76646f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 478
      )
      LocalObject(
        1046,
        IFFLock.Constructor(Vector3(5433.957f, 7060.811f, 82.76646f), Vector3(0, 0, 0)),
        owning_building_guid = 72,
        door_guid = 479
      )
      LocalObject(
        1047,
        IFFLock.Constructor(Vector3(5438.047f, 7043.189f, 62.76646f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 476
      )
      LocalObject(
        1048,
        IFFLock.Constructor(Vector3(5438.047f, 7043.189f, 82.76646f), Vector3(0, 0, 180)),
        owning_building_guid = 72,
        door_guid = 477
      )
      LocalObject(1353, Locker.Constructor(Vector3(5439.716f, 7036.963f, 51.29946f)), owning_building_guid = 72)
      LocalObject(1354, Locker.Constructor(Vector3(5439.751f, 7058.835f, 51.29946f)), owning_building_guid = 72)
      LocalObject(1355, Locker.Constructor(Vector3(5441.053f, 7036.963f, 51.29946f)), owning_building_guid = 72)
      LocalObject(1356, Locker.Constructor(Vector3(5441.088f, 7058.835f, 51.29946f)), owning_building_guid = 72)
      LocalObject(1357, Locker.Constructor(Vector3(5443.741f, 7036.963f, 51.29946f)), owning_building_guid = 72)
      LocalObject(1358, Locker.Constructor(Vector3(5443.741f, 7058.835f, 51.29946f)), owning_building_guid = 72)
      LocalObject(1359, Locker.Constructor(Vector3(5445.143f, 7036.963f, 51.29946f)), owning_building_guid = 72)
      LocalObject(1360, Locker.Constructor(Vector3(5445.143f, 7058.835f, 51.29946f)), owning_building_guid = 72)
      LocalObject(
        1751,
        Terminal.Constructor(Vector3(5445.445f, 7042.129f, 52.63746f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1752,
        Terminal.Constructor(Vector3(5445.445f, 7047.853f, 52.63746f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1753,
        Terminal.Constructor(Vector3(5445.445f, 7053.234f, 52.63746f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2361,
        SpawnTube.Constructor(Vector3(5434.706f, 7039.742f, 50.78746f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        2362,
        SpawnTube.Constructor(Vector3(5434.706f, 7056.152f, 50.78746f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 72
      )
      LocalObject(
        2038,
        ProximityTerminal.Constructor(Vector3(5422.907f, 7046.725f, 88.87546f), pad_landing_tower_frame),
        owning_building_guid = 72
      )
      LocalObject(
        2039,
        Terminal.Constructor(Vector3(5422.907f, 7046.725f, 88.87546f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2041,
        ProximityTerminal.Constructor(Vector3(5422.907f, 7057.17f, 88.87546f), pad_landing_tower_frame),
        owning_building_guid = 72
      )
      LocalObject(
        2042,
        Terminal.Constructor(Vector3(5422.907f, 7057.17f, 88.87546f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        1585,
        FacilityTurret.Constructor(Vector3(5409.07f, 7037.045f, 80.24746f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(1585, 5098)
      LocalObject(
        1587,
        FacilityTurret.Constructor(Vector3(5447.497f, 7066.957f, 80.24746f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(1587, 5099)
      LocalObject(
        2176,
        Painbox.Constructor(Vector3(5428.454f, 7044.849f, 53.32496f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2177,
        Painbox.Constructor(Vector3(5440.923f, 7041.54f, 51.40546f), painbox_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2178,
        Painbox.Constructor(Vector3(5441.113f, 7054.022f, 51.40546f), painbox_radius_continuous),
        owning_building_guid = 72
      )
    }

    Building46()

    def Building46(): Unit = { // Name: Mekala_Tower Type: tower_c GUID: 73, MapID: 46
      LocalBuilding(
        "Mekala_Tower",
        73,
        46,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6134f, 3198f, 54.44196f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2406,
        CaptureTerminal.Constructor(Vector3(6150.587f, 3197.897f, 64.44096f), secondary_capture),
        owning_building_guid = 73
      )
      LocalObject(513, Door.Constructor(Vector3(6146f, 3190f, 55.96296f)), owning_building_guid = 73)
      LocalObject(514, Door.Constructor(Vector3(6146f, 3190f, 75.96196f)), owning_building_guid = 73)
      LocalObject(515, Door.Constructor(Vector3(6146f, 3206f, 55.96296f)), owning_building_guid = 73)
      LocalObject(516, Door.Constructor(Vector3(6146f, 3206f, 75.96196f)), owning_building_guid = 73)
      LocalObject(2560, Door.Constructor(Vector3(6145.146f, 3186.794f, 45.77795f)), owning_building_guid = 73)
      LocalObject(2561, Door.Constructor(Vector3(6145.146f, 3203.204f, 45.77795f)), owning_building_guid = 73)
      LocalObject(
        1073,
        IFFLock.Constructor(Vector3(6143.957f, 3206.811f, 55.90295f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 515
      )
      LocalObject(
        1074,
        IFFLock.Constructor(Vector3(6143.957f, 3206.811f, 75.90295f), Vector3(0, 0, 0)),
        owning_building_guid = 73,
        door_guid = 516
      )
      LocalObject(
        1075,
        IFFLock.Constructor(Vector3(6148.047f, 3189.189f, 55.90295f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 513
      )
      LocalObject(
        1076,
        IFFLock.Constructor(Vector3(6148.047f, 3189.189f, 75.90295f), Vector3(0, 0, 180)),
        owning_building_guid = 73,
        door_guid = 514
      )
      LocalObject(1410, Locker.Constructor(Vector3(6149.716f, 3182.963f, 44.43596f)), owning_building_guid = 73)
      LocalObject(1411, Locker.Constructor(Vector3(6149.751f, 3204.835f, 44.43596f)), owning_building_guid = 73)
      LocalObject(1412, Locker.Constructor(Vector3(6151.053f, 3182.963f, 44.43596f)), owning_building_guid = 73)
      LocalObject(1413, Locker.Constructor(Vector3(6151.088f, 3204.835f, 44.43596f)), owning_building_guid = 73)
      LocalObject(1414, Locker.Constructor(Vector3(6153.741f, 3182.963f, 44.43596f)), owning_building_guid = 73)
      LocalObject(1415, Locker.Constructor(Vector3(6153.741f, 3204.835f, 44.43596f)), owning_building_guid = 73)
      LocalObject(1416, Locker.Constructor(Vector3(6155.143f, 3182.963f, 44.43596f)), owning_building_guid = 73)
      LocalObject(1417, Locker.Constructor(Vector3(6155.143f, 3204.835f, 44.43596f)), owning_building_guid = 73)
      LocalObject(
        1768,
        Terminal.Constructor(Vector3(6155.445f, 3188.129f, 45.77396f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1769,
        Terminal.Constructor(Vector3(6155.445f, 3193.853f, 45.77396f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1770,
        Terminal.Constructor(Vector3(6155.445f, 3199.234f, 45.77396f), order_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        2373,
        SpawnTube.Constructor(Vector3(6144.706f, 3185.742f, 43.92396f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        2374,
        SpawnTube.Constructor(Vector3(6144.706f, 3202.152f, 43.92396f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 73
      )
      LocalObject(
        2044,
        ProximityTerminal.Constructor(Vector3(6132.907f, 3192.725f, 82.01196f), pad_landing_tower_frame),
        owning_building_guid = 73
      )
      LocalObject(
        2045,
        Terminal.Constructor(Vector3(6132.907f, 3192.725f, 82.01196f), air_rearm_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        2047,
        ProximityTerminal.Constructor(Vector3(6132.907f, 3203.17f, 82.01196f), pad_landing_tower_frame),
        owning_building_guid = 73
      )
      LocalObject(
        2048,
        Terminal.Constructor(Vector3(6132.907f, 3203.17f, 82.01196f), air_rearm_terminal),
        owning_building_guid = 73
      )
      LocalObject(
        1602,
        FacilityTurret.Constructor(Vector3(6119.07f, 3183.045f, 73.38396f), manned_turret),
        owning_building_guid = 73
      )
      TurretToWeapon(1602, 5100)
      LocalObject(
        1603,
        FacilityTurret.Constructor(Vector3(6157.497f, 3212.957f, 73.38396f), manned_turret),
        owning_building_guid = 73
      )
      TurretToWeapon(1603, 5101)
      LocalObject(
        2185,
        Painbox.Constructor(Vector3(6138.454f, 3190.849f, 46.46146f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2186,
        Painbox.Constructor(Vector3(6150.923f, 3187.54f, 44.54195f), painbox_radius_continuous),
        owning_building_guid = 73
      )
      LocalObject(
        2187,
        Painbox.Constructor(Vector3(6151.113f, 3200.022f, 44.54195f), painbox_radius_continuous),
        owning_building_guid = 73
      )
    }

    Building27()

    def Building27(): Unit = { // Name: NW_Xelas_Tower Type: tower_c GUID: 74, MapID: 27
      LocalBuilding(
        "NW_Xelas_Tower",
        74,
        27,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6444f, 4606f, 49.63475f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        2409,
        CaptureTerminal.Constructor(Vector3(6460.587f, 4605.897f, 59.63375f), secondary_capture),
        owning_building_guid = 74
      )
      LocalObject(546, Door.Constructor(Vector3(6456f, 4598f, 51.15575f)), owning_building_guid = 74)
      LocalObject(547, Door.Constructor(Vector3(6456f, 4598f, 71.15475f)), owning_building_guid = 74)
      LocalObject(548, Door.Constructor(Vector3(6456f, 4614f, 51.15575f)), owning_building_guid = 74)
      LocalObject(549, Door.Constructor(Vector3(6456f, 4614f, 71.15475f)), owning_building_guid = 74)
      LocalObject(2569, Door.Constructor(Vector3(6455.146f, 4594.794f, 40.97075f)), owning_building_guid = 74)
      LocalObject(2570, Door.Constructor(Vector3(6455.146f, 4611.204f, 40.97075f)), owning_building_guid = 74)
      LocalObject(
        1099,
        IFFLock.Constructor(Vector3(6453.957f, 4614.811f, 51.09575f), Vector3(0, 0, 0)),
        owning_building_guid = 74,
        door_guid = 548
      )
      LocalObject(
        1100,
        IFFLock.Constructor(Vector3(6453.957f, 4614.811f, 71.09575f), Vector3(0, 0, 0)),
        owning_building_guid = 74,
        door_guid = 549
      )
      LocalObject(
        1101,
        IFFLock.Constructor(Vector3(6458.047f, 4597.189f, 51.09575f), Vector3(0, 0, 180)),
        owning_building_guid = 74,
        door_guid = 546
      )
      LocalObject(
        1102,
        IFFLock.Constructor(Vector3(6458.047f, 4597.189f, 71.09575f), Vector3(0, 0, 180)),
        owning_building_guid = 74,
        door_guid = 547
      )
      LocalObject(1446, Locker.Constructor(Vector3(6459.716f, 4590.963f, 39.62875f)), owning_building_guid = 74)
      LocalObject(1447, Locker.Constructor(Vector3(6459.751f, 4612.835f, 39.62875f)), owning_building_guid = 74)
      LocalObject(1448, Locker.Constructor(Vector3(6461.053f, 4590.963f, 39.62875f)), owning_building_guid = 74)
      LocalObject(1449, Locker.Constructor(Vector3(6461.088f, 4612.835f, 39.62875f)), owning_building_guid = 74)
      LocalObject(1450, Locker.Constructor(Vector3(6463.741f, 4590.963f, 39.62875f)), owning_building_guid = 74)
      LocalObject(1451, Locker.Constructor(Vector3(6463.741f, 4612.835f, 39.62875f)), owning_building_guid = 74)
      LocalObject(1452, Locker.Constructor(Vector3(6465.143f, 4590.963f, 39.62875f)), owning_building_guid = 74)
      LocalObject(1453, Locker.Constructor(Vector3(6465.143f, 4612.835f, 39.62875f)), owning_building_guid = 74)
      LocalObject(
        1785,
        Terminal.Constructor(Vector3(6465.445f, 4596.129f, 40.96675f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        1786,
        Terminal.Constructor(Vector3(6465.445f, 4601.853f, 40.96675f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        1787,
        Terminal.Constructor(Vector3(6465.445f, 4607.234f, 40.96675f), order_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        2382,
        SpawnTube.Constructor(Vector3(6454.706f, 4593.742f, 39.11674f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 74
      )
      LocalObject(
        2383,
        SpawnTube.Constructor(Vector3(6454.706f, 4610.152f, 39.11674f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 74
      )
      LocalObject(
        2050,
        ProximityTerminal.Constructor(Vector3(6442.907f, 4600.725f, 77.20474f), pad_landing_tower_frame),
        owning_building_guid = 74
      )
      LocalObject(
        2051,
        Terminal.Constructor(Vector3(6442.907f, 4600.725f, 77.20474f), air_rearm_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        2053,
        ProximityTerminal.Constructor(Vector3(6442.907f, 4611.17f, 77.20474f), pad_landing_tower_frame),
        owning_building_guid = 74
      )
      LocalObject(
        2054,
        Terminal.Constructor(Vector3(6442.907f, 4611.17f, 77.20474f), air_rearm_terminal),
        owning_building_guid = 74
      )
      LocalObject(
        1616,
        FacilityTurret.Constructor(Vector3(6429.07f, 4591.045f, 68.57674f), manned_turret),
        owning_building_guid = 74
      )
      TurretToWeapon(1616, 5102)
      LocalObject(
        1617,
        FacilityTurret.Constructor(Vector3(6467.497f, 4620.957f, 68.57674f), manned_turret),
        owning_building_guid = 74
      )
      TurretToWeapon(1617, 5103)
      LocalObject(
        2194,
        Painbox.Constructor(Vector3(6448.454f, 4598.849f, 41.65425f), painbox_radius_continuous),
        owning_building_guid = 74
      )
      LocalObject(
        2195,
        Painbox.Constructor(Vector3(6460.923f, 4595.54f, 39.73475f), painbox_radius_continuous),
        owning_building_guid = 74
      )
      LocalObject(
        2196,
        Painbox.Constructor(Vector3(6461.113f, 4608.022f, 39.73475f), painbox_radius_continuous),
        owning_building_guid = 74
      )
    }

    Building1()

    def Building1(): Unit = { // Name: WG_Amerish_to_Solsar Type: warpgate GUID: 75, MapID: 1
      LocalBuilding(
        "WG_Amerish_to_Solsar",
        75,
        1,
        FoundationBuilder(WarpGate.Structure(Vector3(1756f, 1374f, 39.99712f)))
      )
    }

    Building2()

    def Building2(): Unit = { // Name: WG_Amerish_to_NCSanc Type: warpgate GUID: 76, MapID: 2
      LocalBuilding(
        "WG_Amerish_to_NCSanc",
        76,
        2,
        FoundationBuilder(WarpGate.Structure(Vector3(3152f, 3652f, 49.55655f)))
      )
    }

    Building3()

    def Building3(): Unit = { // Name: WG_Amerish_to_Oshur Type: warpgate GUID: 77, MapID: 3
      LocalBuilding(
        "WG_Amerish_to_Oshur",
        77,
        3,
        FoundationBuilder(WarpGate.Structure(Vector3(4960f, 4526f, 60.27914f)))
      )
    }

    Building4()

    def Building4(): Unit = { // Name: WG_Amerish_to_Ceryshen Type: warpgate GUID: 78, MapID: 4
      LocalBuilding(
        "WG_Amerish_to_Ceryshen",
        78,
        4,
        FoundationBuilder(WarpGate.Structure(Vector3(6152f, 6754f, 69.20323f)))
      )
    }

    def Lattice(): Unit = {
      LatticeLink("Onatha", "Qumu")
      LatticeLink("Kyoi", "Mekala")
      LatticeLink("Tumas", "GW_Amerish_N")
      LatticeLink("Mekala", "Xelas")
      LatticeLink("Xelas", "Azeban")
      LatticeLink("Azeban", "Sungrey")
      LatticeLink("Verica", "Cetan")
      LatticeLink("Verica", "Kyoi")
      LatticeLink("Sungrey", "WG_Amerish_to_Oshur")
      LatticeLink("Verica", "Qumu")
      LatticeLink("Ikanam", "WG_Amerish_to_Solsar")
      LatticeLink("Qumu", "Cetan")
      LatticeLink("Onatha", "WG_Amerish_to_NCSanc")
      LatticeLink("Azeban", "WG_Amerish_to_Ceryshen")
      LatticeLink("Verica", "Xelas")
      LatticeLink("Ikanam", "GW_Amerish_S")
      LatticeLink("Qumu", "Sungrey")
      LatticeLink("Sungrey", "Tumas")
      LatticeLink("Tumas", "Onatha")
      LatticeLink("Cetan", "Ikanam")
      LatticeLink("Ikanam", "Heyoka")
      LatticeLink("Heyoka", "Cetan")
      LatticeLink("Heyoka", "Kyoi")
    }

    Lattice()

  }
}
