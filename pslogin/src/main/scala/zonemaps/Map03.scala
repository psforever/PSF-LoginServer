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

object Map03 { // Cyssor
  val ZoneMap = new ZoneMap("map03") {
    Checksum = 1624200906L

    Building1()

    def Building1(): Unit = { // Name: Aja Type: amp_station GUID: 1, MapID: 1
      LocalBuilding(
        "Aja",
        1,
        1,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(710f, 5342f, 48.41151f), Vector3(0f, 0f, 20f), amp_station)
        )
      )
      LocalObject(
        279,
        CaptureTerminal.Constructor(Vector3(706.8641f, 5340.862f, 59.91951f), capture_terminal),
        owning_building_guid = 1
      )
      LocalObject(216, Door.Constructor(Vector3(707.8737f, 5348.468f, 61.31351f)), owning_building_guid = 1)
      LocalObject(217, Door.Constructor(Vector3(712.5257f, 5335.678f, 61.31351f)), owning_building_guid = 1)
      LocalObject(421, Door.Constructor(Vector3(611.2217f, 5391.182f, 50.13251f)), owning_building_guid = 1)
      LocalObject(428, Door.Constructor(Vector3(624.2671f, 5378.798f, 58.12551f)), owning_building_guid = 1)
      LocalObject(431, Door.Constructor(Vector3(630.4894f, 5361.702f, 50.16251f)), owning_building_guid = 1)
      LocalObject(440, Door.Constructor(Vector3(656.3859f, 5420.588f, 50.16251f)), owning_building_guid = 1)
      LocalObject(441, Door.Constructor(Vector3(658.8823f, 5283.693f, 58.12551f)), owning_building_guid = 1)
      LocalObject(442, Door.Constructor(Vector3(665.1046f, 5266.598f, 50.16251f)), owning_building_guid = 1)
      LocalObject(443, Door.Constructor(Vector3(673.4808f, 5426.811f, 58.12651f)), owning_building_guid = 1)
      LocalObject(444, Door.Constructor(Vector3(695.4927f, 5369.257f, 60.13851f)), owning_building_guid = 1)
      LocalObject(445, Door.Constructor(Vector3(697.858f, 5375.36f, 55.13251f)), owning_building_guid = 1)
      LocalObject(446, Door.Constructor(Vector3(704.0069f, 5372.357f, 60.13851f)), owning_building_guid = 1)
      LocalObject(448, Door.Constructor(Vector3(716.3878f, 5311.787f, 60.13851f)), owning_building_guid = 1)
      LocalObject(449, Door.Constructor(Vector3(722.1417f, 5308.641f, 55.13251f)), owning_building_guid = 1)
      LocalObject(450, Door.Constructor(Vector3(724.902f, 5314.887f, 60.13851f)), owning_building_guid = 1)
      LocalObject(451, Door.Constructor(Vector3(755.2517f, 5268.847f, 58.12651f)), owning_building_guid = 1)
      LocalObject(452, Door.Constructor(Vector3(772.3466f, 5275.069f, 50.16251f)), owning_building_guid = 1)
      LocalObject(453, Door.Constructor(Vector3(781.7925f, 5320.773f, 50.16251f)), owning_building_guid = 1)
      LocalObject(454, Door.Constructor(Vector3(788.0146f, 5303.679f, 58.12651f)), owning_building_guid = 1)
      LocalObject(855, Door.Constructor(Vector3(632.7656f, 5343.686f, 42.63251f)), owning_building_guid = 1)
      LocalObject(860, Door.Constructor(Vector3(666.2628f, 5321.824f, 35.13251f)), owning_building_guid = 1)
      LocalObject(861, Door.Constructor(Vector3(668.6535f, 5326.951f, 35.13251f)), owning_building_guid = 1)
      LocalObject(862, Door.Constructor(Vector3(675.4801f, 5354.976f, 42.63251f)), owning_building_guid = 1)
      LocalObject(863, Door.Constructor(Vector3(684.0341f, 5319.779f, 35.13251f)), owning_building_guid = 1)
      LocalObject(864, Door.Constructor(Vector3(690.5152f, 5360.448f, 50.13251f)), owning_building_guid = 1)
      LocalObject(865, Door.Constructor(Vector3(692.9059f, 5365.575f, 50.13251f)), owning_building_guid = 1)
      LocalObject(866, Door.Constructor(Vector3(695.642f, 5358.058f, 42.63251f)), owning_building_guid = 1)
      LocalObject(867, Door.Constructor(Vector3(695.9875f, 5345.413f, 42.63251f)), owning_building_guid = 1)
      LocalObject(868, Door.Constructor(Vector3(700.7689f, 5355.667f, 35.13251f)), owning_building_guid = 1)
      LocalObject(869, Door.Constructor(Vector3(701.1121f, 5367.039f, 60.13251f)), owning_building_guid = 1)
      LocalObject(870, Door.Constructor(Vector3(701.7915f, 5364.553f, 55.13251f)), owning_building_guid = 1)
      LocalObject(871, Door.Constructor(Vector3(704.196f, 5322.861f, 35.13251f)), owning_building_guid = 1)
      LocalObject(872, Door.Constructor(Vector3(704.196f, 5322.861f, 42.63251f)), owning_building_guid = 1)
      LocalObject(873, Door.Constructor(Vector3(706.9322f, 5315.343f, 50.13251f)), owning_building_guid = 1)
      LocalObject(874, Door.Constructor(Vector3(708.6319f, 5345.759f, 42.63251f)), owning_building_guid = 1)
      LocalObject(875, Door.Constructor(Vector3(712.059f, 5312.953f, 50.13251f)), owning_building_guid = 1)
      LocalObject(876, Door.Constructor(Vector3(714.1042f, 5330.724f, 35.13251f)), owning_building_guid = 1)
      LocalObject(877, Door.Constructor(Vector3(718.2085f, 5319.447f, 55.13251f)), owning_building_guid = 1)
      LocalObject(878, Door.Constructor(Vector3(719.2864f, 5317.106f, 60.13251f)), owning_building_guid = 1)
      LocalObject(879, Door.Constructor(Vector3(727.4258f, 5352.599f, 50.13251f)), owning_building_guid = 1)
      LocalObject(880, Door.Constructor(Vector3(730.1619f, 5345.082f, 50.13251f)), owning_building_guid = 1)
      LocalObject(1189, Door.Constructor(Vector3(737.4057f, 5352.002f, 50.89151f)), owning_building_guid = 1)
      LocalObject(3509, Door.Constructor(Vector3(690.3627f, 5339.444f, 42.96551f)), owning_building_guid = 1)
      LocalObject(3510, Door.Constructor(Vector3(692.8557f, 5332.595f, 42.96551f)), owning_building_guid = 1)
      LocalObject(3511, Door.Constructor(Vector3(695.3501f, 5325.742f, 42.96551f)), owning_building_guid = 1)
      LocalObject(
        1253,
        IFFLock.Constructor(Vector3(738.9417f, 5355.797f, 50.09151f), Vector3(0, 0, 70)),
        owning_building_guid = 1,
        door_guid = 1189
      )
      LocalObject(
        1285,
        IFFLock.Constructor(Vector3(609.0185f, 5391.259f, 50.07151f), Vector3(0, 0, 340)),
        owning_building_guid = 1,
        door_guid = 421
      )
      LocalObject(
        1303,
        IFFLock.Constructor(Vector3(666.4863f, 5323.579f, 34.94751f), Vector3(0, 0, 70)),
        owning_building_guid = 1,
        door_guid = 860
      )
      LocalObject(
        1304,
        IFFLock.Constructor(Vector3(694.1885f, 5345.76f, 42.44751f), Vector3(0, 0, 340)),
        owning_building_guid = 1,
        door_guid = 867
      )
      LocalObject(
        1305,
        IFFLock.Constructor(Vector3(695.405f, 5367.06f, 60.07251f), Vector3(0, 0, 250)),
        owning_building_guid = 1,
        door_guid = 444
      )
      LocalObject(
        1306,
        IFFLock.Constructor(Vector3(695.6567f, 5375.432f, 55.07351f), Vector3(0, 0, 340)),
        owning_building_guid = 1,
        door_guid = 445
      )
      LocalObject(
        1307,
        IFFLock.Constructor(Vector3(704.0466f, 5374.561f, 60.07251f), Vector3(0, 0, 70)),
        owning_building_guid = 1,
        door_guid = 446
      )
      LocalObject(
        1308,
        IFFLock.Constructor(Vector3(705.9503f, 5322.637f, 42.44751f), Vector3(0, 0, 160)),
        owning_building_guid = 1,
        door_guid = 872
      )
      LocalObject(
        1309,
        IFFLock.Constructor(Vector3(713.7586f, 5328.925f, 34.94751f), Vector3(0, 0, 250)),
        owning_building_guid = 1,
        door_guid = 876
      )
      LocalObject(
        1310,
        IFFLock.Constructor(Vector3(716.3151f, 5309.609f, 60.07251f), Vector3(0, 0, 250)),
        owning_building_guid = 1,
        door_guid = 448
      )
      LocalObject(
        1311,
        IFFLock.Constructor(Vector3(724.3342f, 5308.587f, 55.07351f), Vector3(0, 0, 160)),
        owning_building_guid = 1,
        door_guid = 449
      )
      LocalObject(
        1312,
        IFFLock.Constructor(Vector3(724.9581f, 5317.107f, 60.07251f), Vector3(0, 0, 70)),
        owning_building_guid = 1,
        door_guid = 450
      )
      LocalObject(1646, Locker.Constructor(Vector3(706.8119f, 5326.091f, 41.37251f)), owning_building_guid = 1)
      LocalObject(1647, Locker.Constructor(Vector3(707.9057f, 5326.489f, 41.37251f)), owning_building_guid = 1)
      LocalObject(1648, Locker.Constructor(Vector3(708.9835f, 5326.881f, 41.37251f)), owning_building_guid = 1)
      LocalObject(1649, Locker.Constructor(Vector3(709.9531f, 5348.543f, 33.61151f)), owning_building_guid = 1)
      LocalObject(1650, Locker.Constructor(Vector3(710.0632f, 5327.274f, 41.37251f)), owning_building_guid = 1)
      LocalObject(1651, Locker.Constructor(Vector3(711.1973f, 5348.997f, 33.61151f)), owning_building_guid = 1)
      LocalObject(1652, Locker.Constructor(Vector3(712.4527f, 5349.454f, 33.61151f)), owning_building_guid = 1)
      LocalObject(1653, Locker.Constructor(Vector3(713.7091f, 5349.911f, 33.61151f)), owning_building_guid = 1)
      LocalObject(1654, Locker.Constructor(Vector3(717.9753f, 5351.463f, 33.61151f)), owning_building_guid = 1)
      LocalObject(1655, Locker.Constructor(Vector3(719.2194f, 5351.916f, 33.61151f)), owning_building_guid = 1)
      LocalObject(1656, Locker.Constructor(Vector3(720.4749f, 5352.373f, 33.61151f)), owning_building_guid = 1)
      LocalObject(1657, Locker.Constructor(Vector3(721.7313f, 5352.831f, 33.61151f)), owning_building_guid = 1)
      LocalObject(
        2373,
        Terminal.Constructor(Vector3(688.8893f, 5334.312f, 49.94051f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2374,
        Terminal.Constructor(Vector3(702.3105f, 5363.121f, 49.94051f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2375,
        Terminal.Constructor(Vector3(705.3431f, 5339.164f, 42.70151f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2376,
        Terminal.Constructor(Vector3(706.639f, 5335.604f, 42.70151f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2377,
        Terminal.Constructor(Vector3(707.915f, 5332.098f, 42.70151f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2378,
        Terminal.Constructor(Vector3(717.6857f, 5320.878f, 49.94051f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3396,
        Terminal.Constructor(Vector3(651.4706f, 5362.635f, 42.66851f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3398,
        Terminal.Constructor(Vector3(682.2317f, 5348.291f, 35.16851f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3399,
        Terminal.Constructor(Vector3(691.4941f, 5337.205f, 43.24551f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3400,
        Terminal.Constructor(Vector3(693.9839f, 5330.355f, 43.24551f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3401,
        Terminal.Constructor(Vector3(696.4818f, 5323.504f, 43.24551f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3402,
        Terminal.Constructor(Vector3(706.367f, 5335.889f, 55.13951f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3718,
        Terminal.Constructor(Vector3(636.7578f, 5403.108f, 50.51351f), vehicle_terminal_combined),
        owning_building_guid = 1
      )
      LocalObject(
        2306,
        VehicleSpawnPad.Constructor(Vector3(641.5068f, 5390.323f, 46.35651f), mb_pad_creation, Vector3(0, 0, 160)),
        owning_building_guid = 1,
        terminal_guid = 3718
      )
      LocalObject(3218, ResourceSilo.Constructor(Vector3(788.7111f, 5279.667f, 55.64651f)), owning_building_guid = 1)
      LocalObject(
        3245,
        SpawnTube.Constructor(Vector3(690.3094f, 5338.305f, 41.11151f), Vector3(0, 0, 340)),
        owning_building_guid = 1
      )
      LocalObject(
        3246,
        SpawnTube.Constructor(Vector3(692.8021f, 5331.456f, 41.11151f), Vector3(0, 0, 340)),
        owning_building_guid = 1
      )
      LocalObject(
        3247,
        SpawnTube.Constructor(Vector3(695.2957f, 5324.605f, 41.11151f), Vector3(0, 0, 340)),
        owning_building_guid = 1
      )
      LocalObject(
        2330,
        ProximityTerminal.Constructor(Vector3(689.2837f, 5334.454f, 53.61151f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2331,
        ProximityTerminal.Constructor(Vector3(716.0126f, 5350.169f, 33.61151f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2657,
        ProximityTerminal.Constructor(Vector3(649.4424f, 5340.999f, 56.91651f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2658,
        Terminal.Constructor(Vector3(649.4424f, 5340.999f, 56.91651f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2660,
        ProximityTerminal.Constructor(Vector3(715.3569f, 5405.046f, 59.07751f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2661,
        Terminal.Constructor(Vector3(715.3569f, 5405.046f, 59.07751f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2663,
        ProximityTerminal.Constructor(Vector3(742.9343f, 5275.775f, 56.93951f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2664,
        Terminal.Constructor(Vector3(742.9343f, 5275.775f, 56.93951f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2666,
        ProximityTerminal.Constructor(Vector3(763.1806f, 5340.25f, 56.91651f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        2667,
        Terminal.Constructor(Vector3(763.1806f, 5340.25f, 56.91651f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3096,
        ProximityTerminal.Constructor(Vector3(644.0612f, 5318.217f, 47.81151f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        3097,
        Terminal.Constructor(Vector3(644.0612f, 5318.217f, 47.81151f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        3100,
        ProximityTerminal.Constructor(Vector3(768.4315f, 5363.686f, 47.81151f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        3101,
        Terminal.Constructor(Vector3(768.4315f, 5363.686f, 47.81151f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2165,
        FacilityTurret.Constructor(Vector3(598.3812f, 5412.941f, 57.11951f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(2165, 5000)
      LocalObject(
        2174,
        FacilityTurret.Constructor(Vector3(667.386f, 5223.437f, 57.11951f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(2174, 5001)
      LocalObject(
        2176,
        FacilityTurret.Constructor(Vector3(724.1415f, 5241.723f, 57.11951f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(2176, 5002)
      LocalObject(
        2177,
        FacilityTurret.Constructor(Vector3(742.5974f, 5465.421f, 57.11951f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(2177, 5003)
      LocalObject(
        2178,
        FacilityTurret.Constructor(Vector3(766.3595f, 5406.62f, 57.11951f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(2178, 5004)
      LocalObject(
        2179,
        FacilityTurret.Constructor(Vector3(811.5665f, 5275.904f, 57.11951f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(2179, 5005)
      LocalObject(
        2881,
        Painbox.Constructor(Vector3(656.1143f, 5315.063f, 38.48251f), painbox),
        owning_building_guid = 1
      )
      LocalObject(
        2898,
        Painbox.Constructor(Vector3(698.8355f, 5336.432f, 46.05991f), painbox_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2915,
        Painbox.Constructor(Vector3(668.5839f, 5321.865f, 35.07051f), painbox_door_radius),
        owning_building_guid = 1
      )
      LocalObject(
        2936,
        Painbox.Constructor(Vector3(695.6177f, 5346.646f, 42.37601f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2937,
        Painbox.Constructor(Vector3(704.5602f, 5321.862f, 42.31151f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        2938,
        Painbox.Constructor(Vector3(710.0254f, 5346.22f, 44.29011f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(383, Generator.Constructor(Vector3(651.6545f, 5316.48f, 32.31751f)), owning_building_guid = 1)
      LocalObject(
        366,
        Terminal.Constructor(Vector3(659.3364f, 5319.327f, 33.61151f), gen_control),
        owning_building_guid = 1
      )
    }

    Building12()

    def Building12(): Unit = { // Name: Nzame Type: amp_station GUID: 4, MapID: 12
      LocalBuilding(
        "Nzame",
        4,
        12,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(1568f, 2688f, 45.10147f),
            Vector3(0f, 0f, 325f),
            amp_station
          )
        )
      )
      LocalObject(
        281,
        CaptureTerminal.Constructor(Vector3(1565.269f, 2689.916f, 56.60947f), capture_terminal),
        owning_building_guid = 4
      )
      LocalObject(218, Door.Constructor(Vector3(1564.27f, 2682.305f, 58.00348f)), owning_building_guid = 4)
      LocalObject(219, Door.Constructor(Vector3(1572.078f, 2693.452f, 58.00348f)), owning_building_guid = 4)
      LocalObject(487, Door.Constructor(Vector3(1480.483f, 2681.527f, 46.85247f)), owning_building_guid = 4)
      LocalObject(488, Door.Constructor(Vector3(1490.918f, 2696.43f, 54.81548f)), owning_building_guid = 4)
      LocalObject(489, Door.Constructor(Vector3(1534.032f, 2608.973f, 54.81647f)), owning_building_guid = 4)
      LocalObject(490, Door.Constructor(Vector3(1538.534f, 2764.432f, 46.85247f)), owning_building_guid = 4)
      LocalObject(493, Door.Constructor(Vector3(1546.915f, 2665.438f, 56.82848f)), owning_building_guid = 4)
      LocalObject(496, Door.Constructor(Vector3(1547.638f, 2658.92f, 51.82248f)), owning_building_guid = 4)
      LocalObject(497, Door.Constructor(Vector3(1548.934f, 2598.539f, 46.85247f)), owning_building_guid = 4)
      LocalObject(498, Door.Constructor(Vector3(1548.969f, 2779.335f, 54.81548f)), owning_building_guid = 4)
      LocalObject(499, Door.Constructor(Vector3(1551.631f, 2797.124f, 46.82248f)), owning_building_guid = 4)
      LocalObject(500, Door.Constructor(Vector3(1554.338f, 2660.241f, 56.82848f)), owning_building_guid = 4)
      LocalObject(501, Door.Constructor(Vector3(1581.356f, 2602.114f, 54.81647f)), owning_building_guid = 4)
      LocalObject(502, Door.Constructor(Vector3(1582.007f, 2715.518f, 56.82848f)), owning_building_guid = 4)
      LocalObject(503, Door.Constructor(Vector3(1588.363f, 2717.081f, 51.82248f)), owning_building_guid = 4)
      LocalObject(504, Door.Constructor(Vector3(1589.429f, 2710.321f, 56.82848f)), owning_building_guid = 4)
      LocalObject(505, Door.Constructor(Vector3(1591.791f, 2617.016f, 46.85247f)), owning_building_guid = 4)
      LocalObject(506, Door.Constructor(Vector3(1601.624f, 2776.995f, 46.85247f)), owning_building_guid = 4)
      LocalObject(507, Door.Constructor(Vector3(1616.526f, 2766.56f, 54.81647f)), owning_building_guid = 4)
      LocalObject(905, Door.Constructor(Vector3(1525.081f, 2752.234f, 39.32248f)), owning_building_guid = 4)
      LocalObject(906, Door.Constructor(Vector3(1526.386f, 2712.255f, 31.82248f)), owning_building_guid = 4)
      LocalObject(907, Door.Constructor(Vector3(1531.957f, 2713.237f, 31.82248f)), owning_building_guid = 4)
      LocalObject(908, Door.Constructor(Vector3(1534.904f, 2696.525f, 31.82248f)), owning_building_guid = 4)
      LocalObject(909, Door.Constructor(Vector3(1544.404f, 2675.223f, 46.82248f)), owning_building_guid = 4)
      LocalObject(910, Door.Constructor(Vector3(1545.387f, 2669.652f, 46.82248f)), owning_building_guid = 4)
      LocalObject(911, Door.Constructor(Vector3(1548.993f, 2681.776f, 31.82248f)), owning_building_guid = 4)
      LocalObject(912, Door.Constructor(Vector3(1548.993f, 2681.776f, 39.32248f)), owning_building_guid = 4)
      LocalObject(913, Door.Constructor(Vector3(1552.934f, 2666.114f, 56.82248f)), owning_building_guid = 4)
      LocalObject(914, Door.Constructor(Vector3(1554.234f, 2668.34f, 51.82248f)), owning_building_guid = 4)
      LocalObject(915, Door.Constructor(Vector3(1558.83f, 2723.72f, 39.32248f)), owning_building_guid = 4)
      LocalObject(916, Door.Constructor(Vector3(1561.117f, 2678.17f, 31.82248f)), owning_building_guid = 4)
      LocalObject(917, Door.Constructor(Vector3(1562.759f, 2701.436f, 39.32248f)), owning_building_guid = 4)
      LocalObject(918, Door.Constructor(Vector3(1570.294f, 2691.277f, 39.32248f)), owning_building_guid = 4)
      LocalObject(919, Door.Constructor(Vector3(1571.936f, 2714.542f, 46.82248f)), owning_building_guid = 4)
      LocalObject(920, Door.Constructor(Vector3(1572.918f, 2708.972f, 39.32248f)), owning_building_guid = 4)
      LocalObject(921, Door.Constructor(Vector3(1573.901f, 2703.401f, 31.82248f)), owning_building_guid = 4)
      LocalObject(922, Door.Constructor(Vector3(1577.507f, 2715.525f, 46.82248f)), owning_building_guid = 4)
      LocalObject(923, Door.Constructor(Vector3(1581.766f, 2707.66f, 51.82248f)), owning_building_guid = 4)
      LocalObject(924, Door.Constructor(Vector3(1582.089f, 2673.252f, 46.82248f)), owning_building_guid = 4)
      LocalObject(925, Door.Constructor(Vector3(1583.413f, 2709.642f, 56.82248f)), owning_building_guid = 4)
      LocalObject(926, Door.Constructor(Vector3(1586.677f, 2679.805f, 46.82248f)), owning_building_guid = 4)
      LocalObject(1191, Door.Constructor(Vector3(1591.913f, 2671.288f, 47.58147f)), owning_building_guid = 4)
      LocalObject(3525, Door.Constructor(Vector3(1546.279f, 2690.675f, 39.65548f)), owning_building_guid = 4)
      LocalObject(3526, Door.Constructor(Vector3(1550.462f, 2696.649f, 39.65548f)), owning_building_guid = 4)
      LocalObject(3527, Door.Constructor(Vector3(1554.643f, 2702.62f, 39.65548f)), owning_building_guid = 4)
      LocalObject(
        1255,
        IFFLock.Constructor(Vector3(1595.902f, 2672.206f, 46.78148f), Vector3(0, 0, 125)),
        owning_building_guid = 4,
        door_guid = 1191
      )
      LocalObject(
        1342,
        IFFLock.Constructor(Vector3(1527.952f, 2713.078f, 31.63747f), Vector3(0, 0, 125)),
        owning_building_guid = 4,
        door_guid = 906
      )
      LocalObject(
        1343,
        IFFLock.Constructor(Vector3(1545.089f, 2664.249f, 56.76247f), Vector3(0, 0, 305)),
        owning_building_guid = 4,
        door_guid = 493
      )
      LocalObject(
        1348,
        IFFLock.Constructor(Vector3(1548.852f, 2657.094f, 51.76347f), Vector3(0, 0, 215)),
        owning_building_guid = 4,
        door_guid = 496
      )
      LocalObject(
        1349,
        IFFLock.Constructor(Vector3(1549.816f, 2680.211f, 39.13747f), Vector3(0, 0, 215)),
        owning_building_guid = 4,
        door_guid = 912
      )
      LocalObject(
        1350,
        IFFLock.Constructor(Vector3(1550.43f, 2798.973f, 46.76147f), Vector3(0, 0, 35)),
        owning_building_guid = 4,
        door_guid = 499
      )
      LocalObject(
        1351,
        IFFLock.Constructor(Vector3(1556.188f, 2661.469f, 56.76247f), Vector3(0, 0, 125)),
        owning_building_guid = 4,
        door_guid = 500
      )
      LocalObject(
        1352,
        IFFLock.Constructor(Vector3(1559.445f, 2677.422f, 31.63747f), Vector3(0, 0, 305)),
        owning_building_guid = 4,
        door_guid = 916
      )
      LocalObject(
        1353,
        IFFLock.Constructor(Vector3(1562.011f, 2703.109f, 39.13747f), Vector3(0, 0, 35)),
        owning_building_guid = 4,
        door_guid = 917
      )
      LocalObject(
        1354,
        IFFLock.Constructor(Vector3(1580.156f, 2714.329f, 56.76247f), Vector3(0, 0, 305)),
        owning_building_guid = 4,
        door_guid = 502
      )
      LocalObject(
        1355,
        IFFLock.Constructor(Vector3(1587.159f, 2718.925f, 51.76347f), Vector3(0, 0, 35)),
        owning_building_guid = 4,
        door_guid = 503
      )
      LocalObject(
        1356,
        IFFLock.Constructor(Vector3(1591.257f, 2711.553f, 56.76247f), Vector3(0, 0, 125)),
        owning_building_guid = 4,
        door_guid = 504
      )
      LocalObject(1708, Locker.Constructor(Vector3(1553.14f, 2681.487f, 38.06247f)), owning_building_guid = 4)
      LocalObject(1709, Locker.Constructor(Vector3(1554.093f, 2680.819f, 38.06247f)), owning_building_guid = 4)
      LocalObject(1711, Locker.Constructor(Vector3(1555.033f, 2680.161f, 38.06247f)), owning_building_guid = 4)
      LocalObject(1712, Locker.Constructor(Vector3(1555.974f, 2679.502f, 38.06247f)), owning_building_guid = 4)
      LocalObject(1714, Locker.Constructor(Vector3(1573.333f, 2691.792f, 30.30148f)), owning_building_guid = 4)
      LocalObject(1715, Locker.Constructor(Vector3(1574.418f, 2691.032f, 30.30148f)), owning_building_guid = 4)
      LocalObject(1716, Locker.Constructor(Vector3(1575.512f, 2690.266f, 30.30148f)), owning_building_guid = 4)
      LocalObject(1717, Locker.Constructor(Vector3(1576.607f, 2689.499f, 30.30148f)), owning_building_guid = 4)
      LocalObject(1718, Locker.Constructor(Vector3(1580.326f, 2686.895f, 30.30148f)), owning_building_guid = 4)
      LocalObject(1719, Locker.Constructor(Vector3(1581.411f, 2686.135f, 30.30148f)), owning_building_guid = 4)
      LocalObject(1720, Locker.Constructor(Vector3(1582.505f, 2685.369f, 30.30148f)), owning_building_guid = 4)
      LocalObject(1721, Locker.Constructor(Vector3(1583.601f, 2684.603f, 30.30148f)), owning_building_guid = 4)
      LocalObject(
        2398,
        Terminal.Constructor(Vector3(1549.594f, 2700.883f, 46.63047f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2401,
        Terminal.Constructor(Vector3(1555.106f, 2669.589f, 46.63047f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2403,
        Terminal.Constructor(Vector3(1558.693f, 2684.028f, 39.39148f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2404,
        Terminal.Constructor(Vector3(1560.833f, 2687.084f, 39.39148f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2405,
        Terminal.Constructor(Vector3(1563.006f, 2690.188f, 39.39148f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2406,
        Terminal.Constructor(Vector3(1580.891f, 2706.413f, 46.63047f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        3409,
        Terminal.Constructor(Vector3(1545.095f, 2688.465f, 39.93547f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        3410,
        Terminal.Constructor(Vector3(1549.275f, 2694.44f, 39.93547f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        3411,
        Terminal.Constructor(Vector3(1551.332f, 2747.78f, 39.35847f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        3412,
        Terminal.Constructor(Vector3(1553.457f, 2700.408f, 39.93547f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        3413,
        Terminal.Constructor(Vector3(1557.226f, 2714.355f, 31.85847f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        3414,
        Terminal.Constructor(Vector3(1560.91f, 2687.471f, 51.82948f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        3720,
        Terminal.Constructor(Vector3(1576.047f, 2783.047f, 47.20348f), vehicle_terminal_combined),
        owning_building_guid = 4
      )
      LocalObject(
        2308,
        VehicleSpawnPad.Constructor(Vector3(1568.298f, 2771.823f, 43.04647f), mb_pad_creation, Vector3(0, 0, 215)),
        owning_building_guid = 4,
        terminal_guid = 3720
      )
      LocalObject(3220, ResourceSilo.Constructor(Vector3(1562.086f, 2587.771f, 52.33648f)), owning_building_guid = 4)
      LocalObject(
        3261,
        SpawnTube.Constructor(Vector3(1545.317f, 2690.068f, 37.80148f), Vector3(0, 0, 35)),
        owning_building_guid = 4
      )
      LocalObject(
        3262,
        SpawnTube.Constructor(Vector3(1549.499f, 2696.04f, 37.80148f), Vector3(0, 0, 35)),
        owning_building_guid = 4
      )
      LocalObject(
        3263,
        SpawnTube.Constructor(Vector3(1553.679f, 2702.01f, 37.80148f), Vector3(0, 0, 35)),
        owning_building_guid = 4
      )
      LocalObject(
        2334,
        ProximityTerminal.Constructor(Vector3(1549.936f, 2700.641f, 50.30148f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2335,
        ProximityTerminal.Constructor(Vector3(1578.14f, 2687.76f, 30.30148f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2672,
        ProximityTerminal.Constructor(Vector3(1532.445f, 2737.031f, 53.60648f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        2673,
        Terminal.Constructor(Vector3(1532.445f, 2737.031f, 53.60648f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2675,
        ProximityTerminal.Constructor(Vector3(1532.642f, 2623.037f, 53.62947f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        2676,
        Terminal.Constructor(Vector3(1532.642f, 2623.037f, 53.62947f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2678,
        ProximityTerminal.Constructor(Vector3(1597.07f, 2643.433f, 53.60648f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        2679,
        Terminal.Constructor(Vector3(1597.07f, 2643.433f, 53.60648f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2681,
        ProximityTerminal.Constructor(Vector3(1622.717f, 2719.774f, 55.76748f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        2682,
        Terminal.Constructor(Vector3(1622.717f, 2719.774f, 55.76748f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        3112,
        ProximityTerminal.Constructor(Vector3(1510.697f, 2728.372f, 44.50148f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        3113,
        Terminal.Constructor(Vector3(1510.697f, 2728.372f, 44.50148f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        3116,
        ProximityTerminal.Constructor(Vector3(1619.279f, 2652.574f, 44.50148f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        3117,
        Terminal.Constructor(Vector3(1619.279f, 2652.574f, 44.50148f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2191,
        FacilityTurret.Constructor(Vector3(1446.436f, 2654.902f, 53.80947f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(2191, 5006)
      LocalObject(
        2193,
        FacilityTurret.Constructor(Vector3(1493.969f, 2618.9f, 53.80947f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(2193, 5007)
      LocalObject(
        2196,
        FacilityTurret.Constructor(Vector3(1562.09f, 2820.123f, 53.80947f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(2196, 5008)
      LocalObject(
        2197,
        FacilityTurret.Constructor(Vector3(1572.113f, 2566.89f, 53.80947f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(2197, 5009)
      LocalObject(
        2198,
        FacilityTurret.Constructor(Vector3(1653.26f, 2678.898f, 53.80947f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(2198, 5010)
      LocalObject(
        2199,
        FacilityTurret.Constructor(Vector3(1687.798f, 2732.089f, 53.80947f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(2199, 5011)
      LocalObject(2883, Painbox.Constructor(Vector3(1515.027f, 2716.69f, 35.17247f), painbox), owning_building_guid = 4)
      LocalObject(
        2900,
        Painbox.Constructor(Vector3(1557.035f, 2693.952f, 42.74987f), painbox_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        2917,
        Painbox.Constructor(Vector3(1527.751f, 2710.377f, 31.76048f), painbox_door_radius),
        owning_building_guid = 4
      )
      LocalObject(
        2942,
        Painbox.Constructor(Vector3(1548.383f, 2680.905f, 39.00148f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        2943,
        Painbox.Constructor(Vector3(1563.556f, 2702.446f, 39.06598f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        2944,
        Painbox.Constructor(Vector3(1571.472f, 2690.4f, 40.98008f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(385, Generator.Constructor(Vector3(1513.63f, 2721.156f, 29.00747f)), owning_building_guid = 4)
      LocalObject(
        368,
        Terminal.Constructor(Vector3(1520.368f, 2716.496f, 30.30148f), gen_control),
        owning_building_guid = 4
      )
    }

    Building5()

    def Building5(): Unit = { // Name: Ekera Type: amp_station GUID: 7, MapID: 5
      LocalBuilding(
        "Ekera",
        7,
        5,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5636f, 6622f, 50.96042f),
            Vector3(0f, 0f, 275f),
            amp_station
          )
        )
      )
      LocalObject(
        289,
        CaptureTerminal.Constructor(Vector3(5635.712f, 6625.324f, 62.46842f), capture_terminal),
        owning_building_guid = 7
      )
      LocalObject(220, Door.Constructor(Vector3(5629.239f, 6621.197f, 63.86242f)), owning_building_guid = 7)
      LocalObject(221, Door.Constructor(Vector3(5642.798f, 6622.38f, 63.86242f)), owning_building_guid = 7)
      LocalObject(693, Door.Constructor(Vector3(5553.628f, 6597.224f, 60.67542f)), owning_building_guid = 7)
      LocalObject(694, Door.Constructor(Vector3(5555.213f, 6579.101f, 52.71142f)), owning_building_guid = 7)
      LocalObject(695, Door.Constructor(Vector3(5574.787f, 6684.881f, 52.71142f)), owning_building_guid = 7)
      LocalObject(696, Door.Constructor(Vector3(5578.792f, 6556.562f, 60.67542f)), owning_building_guid = 7)
      LocalObject(701, Door.Constructor(Vector3(5592.91f, 6686.467f, 60.67442f)), owning_building_guid = 7)
      LocalObject(702, Door.Constructor(Vector3(5596.916f, 6558.147f, 52.71142f)), owning_building_guid = 7)
      LocalObject(703, Door.Constructor(Vector3(5600.635f, 6618.906f, 57.68142f)), owning_building_guid = 7)
      LocalObject(704, Door.Constructor(Vector3(5605.163f, 6623.649f, 62.68742f)), owning_building_guid = 7)
      LocalObject(705, Door.Constructor(Vector3(5605.954f, 6614.623f, 62.68742f)), owning_building_guid = 7)
      LocalObject(706, Door.Constructor(Vector3(5666.083f, 6628.958f, 62.68742f)), owning_building_guid = 7)
      LocalObject(707, Door.Constructor(Vector3(5666.874f, 6619.932f, 62.68742f)), owning_building_guid = 7)
      LocalObject(708, Door.Constructor(Vector3(5671.366f, 6625.094f, 57.68142f)), owning_building_guid = 7)
      LocalObject(709, Door.Constructor(Vector3(5675.609f, 6693.702f, 52.71142f)), owning_building_guid = 7)
      LocalObject(711, Door.Constructor(Vector3(5693.733f, 6695.288f, 60.67442f)), owning_building_guid = 7)
      LocalObject(716, Door.Constructor(Vector3(5709.072f, 6704.683f, 52.68142f)), owning_building_guid = 7)
      LocalObject(717, Door.Constructor(Vector3(5725.787f, 6653.447f, 52.71142f)), owning_building_guid = 7)
      LocalObject(718, Door.Constructor(Vector3(5727.373f, 6635.324f, 60.67542f)), owning_building_guid = 7)
      LocalObject(1079, Door.Constructor(Vector3(5607.409f, 6627.529f, 52.68142f)), owning_building_guid = 7)
      LocalObject(1080, Door.Constructor(Vector3(5609.551f, 6619.473f, 62.68142f)), owning_building_guid = 7)
      LocalObject(1081, Door.Constructor(Vector3(5611.045f, 6631.863f, 52.68142f)), owning_building_guid = 7)
      LocalObject(1082, Door.Constructor(Vector3(5612.091f, 6619.908f, 57.68142f)), owning_building_guid = 7)
      LocalObject(1083, Door.Constructor(Vector3(5619.015f, 6632.56f, 37.68142f)), owning_building_guid = 7)
      LocalObject(1084, Door.Constructor(Vector3(5619.015f, 6632.56f, 45.18142f)), owning_building_guid = 7)
      LocalObject(1085, Door.Constructor(Vector3(5621.257f, 6652.833f, 37.68142f)), owning_building_guid = 7)
      LocalObject(1086, Door.Constructor(Vector3(5624.046f, 6620.954f, 37.68142f)), owning_building_guid = 7)
      LocalObject(1087, Door.Constructor(Vector3(5627.832f, 6669.469f, 37.68142f)), owning_building_guid = 7)
      LocalObject(1088, Door.Constructor(Vector3(5632.165f, 6665.833f, 37.68142f)), owning_building_guid = 7)
      LocalObject(1089, Door.Constructor(Vector3(5633.758f, 6601.728f, 52.68142f)), owning_building_guid = 7)
      LocalObject(1090, Door.Constructor(Vector3(5639.985f, 6622.349f, 45.18142f)), owning_building_guid = 7)
      LocalObject(1091, Door.Constructor(Vector3(5641.728f, 6602.425f, 52.68142f)), owning_building_guid = 7)
      LocalObject(1092, Door.Constructor(Vector3(5642.924f, 6634.651f, 45.18142f)), owning_building_guid = 7)
      LocalObject(1093, Door.Constructor(Vector3(5651.59f, 6627.379f, 37.68142f)), owning_building_guid = 7)
      LocalObject(1094, Door.Constructor(Vector3(5655.227f, 6631.713f, 45.18142f)), owning_building_guid = 7)
      LocalObject(1095, Door.Constructor(Vector3(5657.468f, 6651.985f, 45.18142f)), owning_building_guid = 7)
      LocalObject(1096, Door.Constructor(Vector3(5657.618f, 6696.167f, 45.18142f)), owning_building_guid = 7)
      LocalObject(1097, Door.Constructor(Vector3(5658.863f, 6636.046f, 52.68142f)), owning_building_guid = 7)
      LocalObject(1098, Door.Constructor(Vector3(5659.909f, 6624.092f, 57.68142f)), owning_building_guid = 7)
      LocalObject(1099, Door.Constructor(Vector3(5662.486f, 6624.104f, 62.68142f)), owning_building_guid = 7)
      LocalObject(1100, Door.Constructor(Vector3(5663.196f, 6632.41f, 52.68142f)), owning_building_guid = 7)
      LocalObject(1199, Door.Constructor(Vector3(5638.568f, 6592.939f, 53.44042f)), owning_building_guid = 7)
      LocalObject(3585, Door.Constructor(Vector3(5624.087f, 6640.358f, 45.51442f)), owning_building_guid = 7)
      LocalObject(3586, Door.Constructor(Vector3(5631.353f, 6640.994f, 45.51442f)), owning_building_guid = 7)
      LocalObject(3587, Door.Constructor(Vector3(5638.614f, 6641.629f, 45.51442f)), owning_building_guid = 7)
      LocalObject(
        1263,
        IFFLock.Constructor(Vector3(5641.836f, 6590.474f, 52.64042f), Vector3(0, 0, 175)),
        owning_building_guid = 7,
        door_guid = 1199
      )
      LocalObject(
        1506,
        IFFLock.Constructor(Vector3(5600.016f, 6616.802f, 57.62242f), Vector3(0, 0, 265)),
        owning_building_guid = 7,
        door_guid = 703
      )
      LocalObject(
        1507,
        IFFLock.Constructor(Vector3(5603.079f, 6624.283f, 62.62142f), Vector3(0, 0, 355)),
        owning_building_guid = 7,
        door_guid = 704
      )
      LocalObject(
        1508,
        IFFLock.Constructor(Vector3(5608.083f, 6613.995f, 62.62142f), Vector3(0, 0, 175)),
        owning_building_guid = 7,
        door_guid = 705
      )
      LocalObject(
        1509,
        IFFLock.Constructor(Vector3(5618.345f, 6630.923f, 44.99642f), Vector3(0, 0, 265)),
        owning_building_guid = 7,
        door_guid = 1084
      )
      LocalObject(
        1510,
        IFFLock.Constructor(Vector3(5622.398f, 6621.753f, 37.49642f), Vector3(0, 0, 355)),
        owning_building_guid = 7,
        door_guid = 1086
      )
      LocalObject(
        1511,
        IFFLock.Constructor(Vector3(5629.468f, 6668.799f, 37.49642f), Vector3(0, 0, 175)),
        owning_building_guid = 7,
        door_guid = 1087
      )
      LocalObject(
        1512,
        IFFLock.Constructor(Vector3(5643.724f, 6636.3f, 44.99642f), Vector3(0, 0, 85)),
        owning_building_guid = 7,
        door_guid = 1092
      )
      LocalObject(
        1513,
        IFFLock.Constructor(Vector3(5663.983f, 6629.612f, 62.62142f), Vector3(0, 0, 355)),
        owning_building_guid = 7,
        door_guid = 706
      )
      LocalObject(
        1514,
        IFFLock.Constructor(Vector3(5668.992f, 6619.323f, 62.62142f), Vector3(0, 0, 175)),
        owning_building_guid = 7,
        door_guid = 707
      )
      LocalObject(
        1515,
        IFFLock.Constructor(Vector3(5672.005f, 6627.202f, 57.62242f), Vector3(0, 0, 85)),
        owning_building_guid = 7,
        door_guid = 708
      )
      LocalObject(
        1520,
        IFFLock.Constructor(Vector3(5709.716f, 6706.792f, 52.62042f), Vector3(0, 0, 85)),
        owning_building_guid = 7,
        door_guid = 716
      )
      LocalObject(1977, Locker.Constructor(Vector3(5621.458f, 6629.197f, 43.92142f)), owning_building_guid = 7)
      LocalObject(1978, Locker.Constructor(Vector3(5621.56f, 6628.038f, 43.92142f)), owning_building_guid = 7)
      LocalObject(1979, Locker.Constructor(Vector3(5621.66f, 6626.895f, 43.92142f)), owning_building_guid = 7)
      LocalObject(1980, Locker.Constructor(Vector3(5621.76f, 6625.75f, 43.92142f)), owning_building_guid = 7)
      LocalObject(1981, Locker.Constructor(Vector3(5642.333f, 6620.352f, 36.16042f)), owning_building_guid = 7)
      LocalObject(1982, Locker.Constructor(Vector3(5642.448f, 6619.033f, 36.16042f)), owning_building_guid = 7)
      LocalObject(1983, Locker.Constructor(Vector3(5642.564f, 6617.702f, 36.16042f)), owning_building_guid = 7)
      LocalObject(1984, Locker.Constructor(Vector3(5642.681f, 6616.37f, 36.16042f)), owning_building_guid = 7)
      LocalObject(1985, Locker.Constructor(Vector3(5643.077f, 6611.847f, 36.16042f)), owning_building_guid = 7)
      LocalObject(1986, Locker.Constructor(Vector3(5643.192f, 6610.528f, 36.16042f)), owning_building_guid = 7)
      LocalObject(1987, Locker.Constructor(Vector3(5643.309f, 6609.197f, 36.16042f)), owning_building_guid = 7)
      LocalObject(1988, Locker.Constructor(Vector3(5643.425f, 6607.865f, 36.16042f)), owning_building_guid = 7)
      LocalObject(
        2496,
        Terminal.Constructor(Vector3(5613.608f, 6620.043f, 52.48942f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2497,
        Terminal.Constructor(Vector3(5626.975f, 6626.577f, 45.25042f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2498,
        Terminal.Constructor(Vector3(5630.691f, 6626.902f, 45.25042f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2499,
        Terminal.Constructor(Vector3(5634.038f, 6644.381f, 52.48942f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2500,
        Terminal.Constructor(Vector3(5634.466f, 6627.232f, 45.25042f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2501,
        Terminal.Constructor(Vector3(5658.391f, 6623.961f, 52.48942f), order_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        3465,
        Terminal.Constructor(Vector3(5621.633f, 6639.845f, 45.79442f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        3466,
        Terminal.Constructor(Vector3(5628.897f, 6640.484f, 45.79442f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        3467,
        Terminal.Constructor(Vector3(5631.038f, 6627.091f, 57.68842f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        3468,
        Terminal.Constructor(Vector3(5636.158f, 6641.117f, 45.79442f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        3469,
        Terminal.Constructor(Vector3(5649.264f, 6647.194f, 37.71742f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        3470,
        Terminal.Constructor(Vector3(5671.081f, 6673.194f, 45.21742f), spawn_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        3728,
        Terminal.Constructor(Vector3(5713.982f, 6676.931f, 53.06242f), vehicle_terminal_combined),
        owning_building_guid = 7
      )
      LocalObject(
        2320,
        VehicleSpawnPad.Constructor(Vector3(5700.404f, 6675.652f, 48.90542f), mb_pad_creation, Vector3(0, 0, 265)),
        owning_building_guid = 7,
        terminal_guid = 3728
      )
      LocalObject(3228, ResourceSilo.Constructor(Vector3(5555.418f, 6562.104f, 58.19542f)), owning_building_guid = 7)
      LocalObject(
        3321,
        SpawnTube.Constructor(Vector3(5623.003f, 6640.706f, 43.66042f), Vector3(0, 0, 85)),
        owning_building_guid = 7
      )
      LocalObject(
        3322,
        SpawnTube.Constructor(Vector3(5630.267f, 6641.341f, 43.66042f), Vector3(0, 0, 85)),
        owning_building_guid = 7
      )
      LocalObject(
        3323,
        SpawnTube.Constructor(Vector3(5637.527f, 6641.976f, 43.66042f), Vector3(0, 0, 85)),
        owning_building_guid = 7
      )
      LocalObject(
        2347,
        ProximityTerminal.Constructor(Vector3(5634.072f, 6643.963f, 56.16042f), medical_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2348,
        ProximityTerminal.Constructor(Vector3(5642.334f, 6614.078f, 36.16042f), medical_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2771,
        ProximityTerminal.Constructor(Vector3(5563.508f, 6607.328f, 59.48842f), pad_landing_frame),
        owning_building_guid = 7
      )
      LocalObject(
        2772,
        Terminal.Constructor(Vector3(5563.508f, 6607.328f, 59.48842f), air_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2774,
        ProximityTerminal.Constructor(Vector3(5620.546f, 6571.084f, 59.46542f), pad_landing_frame),
        owning_building_guid = 7
      )
      LocalObject(
        2775,
        Terminal.Constructor(Vector3(5620.546f, 6571.084f, 59.46542f), air_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2777,
        ProximityTerminal.Constructor(Vector3(5650.706f, 6680.753f, 59.46542f), pad_landing_frame),
        owning_building_guid = 7
      )
      LocalObject(
        2778,
        Terminal.Constructor(Vector3(5650.706f, 6680.753f, 59.46542f), air_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2780,
        ProximityTerminal.Constructor(Vector3(5695.512f, 6600.508f, 61.62642f), pad_landing_frame),
        owning_building_guid = 7
      )
      LocalObject(
        2781,
        Terminal.Constructor(Vector3(5695.512f, 6600.508f, 61.62642f), air_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        3176,
        ProximityTerminal.Constructor(Vector3(5630.093f, 6691.848f, 50.36042f), repair_silo),
        owning_building_guid = 7
      )
      LocalObject(
        3177,
        Terminal.Constructor(Vector3(5630.093f, 6691.848f, 50.36042f), ground_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        3180,
        ProximityTerminal.Constructor(Vector3(5641.824f, 6559.947f, 50.36042f), repair_silo),
        owning_building_guid = 7
      )
      LocalObject(
        3181,
        Terminal.Constructor(Vector3(5641.824f, 6559.947f, 50.36042f), ground_rearm_terminal),
        owning_building_guid = 7
      )
      LocalObject(
        2263,
        FacilityTurret.Constructor(Vector3(5532.506f, 6693.848f, 59.66842f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(2263, 5012)
      LocalObject(
        2264,
        FacilityTurret.Constructor(Vector3(5535.48f, 6634.294f, 59.66842f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(2264, 5013)
      LocalObject(
        2265,
        FacilityTurret.Constructor(Vector3(5545.869f, 6541.001f, 59.66842f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(2265, 5014)
      LocalObject(
        2269,
        FacilityTurret.Constructor(Vector3(5683.832f, 6550.836f, 59.66842f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(2269, 5015)
      LocalObject(
        2271,
        FacilityTurret.Constructor(Vector3(5733.413f, 6711.455f, 59.66842f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(2271, 5016)
      LocalObject(
        2273,
        FacilityTurret.Constructor(Vector3(5746.779f, 6558.57f, 59.66842f), manned_turret),
        owning_building_guid = 7
      )
      TurretToWeapon(2273, 5017)
      LocalObject(
        2891,
        Painbox.Constructor(Vector3(5623.927f, 6681.021f, 41.03142f), painbox),
        owning_building_guid = 7
      )
      LocalObject(
        2908,
        Painbox.Constructor(Vector3(5633.512f, 6634.225f, 48.60882f), painbox_continuous),
        owning_building_guid = 7
      )
      LocalObject(
        2925,
        Painbox.Constructor(Vector3(5627.27f, 6667.216f, 37.61942f), painbox_door_radius),
        owning_building_guid = 7
      )
      LocalObject(
        2966,
        Painbox.Constructor(Vector3(5617.956f, 6632.467f, 44.86042f), painbox_door_radius_continuous),
        owning_building_guid = 7
      )
      LocalObject(
        2967,
        Painbox.Constructor(Vector3(5640.07f, 6620.883f, 46.83902f), painbox_door_radius_continuous),
        owning_building_guid = 7
      )
      LocalObject(
        2968,
        Painbox.Constructor(Vector3(5644.209f, 6634.69f, 44.92492f), painbox_door_radius_continuous),
        owning_building_guid = 7
      )
      LocalObject(393, Generator.Constructor(Vector3(5626.451f, 6684.962f, 34.86642f)), owning_building_guid = 7)
      LocalObject(
        376,
        Terminal.Constructor(Vector3(5627.212f, 6676.806f, 36.16042f), gen_control),
        owning_building_guid = 7
      )
    }

    Building20()

    def Building20(): Unit = { // Name: Kaang Type: amp_station GUID: 10, MapID: 20
      LocalBuilding(
        "Kaang",
        10,
        20,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5846f, 3956f, 62.32429f),
            Vector3(0f, 0f, 205f),
            amp_station
          )
        )
      )
      LocalObject(
        290,
        CaptureTerminal.Constructor(Vector3(5849.025f, 3957.407f, 73.83229f), capture_terminal),
        owning_building_guid = 10
      )
      LocalObject(222, Door.Constructor(Vector3(5842.933f, 3962.078f, 75.22629f)), owning_building_guid = 10)
      LocalObject(223, Door.Constructor(Vector3(5848.682f, 3949.742f, 75.22629f)), owning_building_guid = 10)
      LocalObject(719, Door.Constructor(Vector3(5764.942f, 3987.376f, 72.03929f)), owning_building_guid = 10)
      LocalObject(720, Door.Constructor(Vector3(5772.631f, 3970.889f, 64.07529f)), owning_building_guid = 10)
      LocalObject(721, Door.Constructor(Vector3(5778.057f, 4017.242f, 64.07529f)), owning_building_guid = 10)
      LocalObject(722, Door.Constructor(Vector3(5794.545f, 4024.93f, 72.03929f)), owning_building_guid = 10)
      LocalObject(723, Door.Constructor(Vector3(5828.792f, 3981.711f, 74.05129f)), owning_building_guid = 10)
      LocalObject(724, Door.Constructor(Vector3(5830.997f, 3988.174f, 69.04529f)), owning_building_guid = 10)
      LocalObject(725, Door.Constructor(Vector3(5837.003f, 3985.542f, 74.05129f)), owning_building_guid = 10)
      LocalObject(726, Door.Constructor(Vector3(5854.616f, 3926.281f, 74.05129f)), owning_building_guid = 10)
      LocalObject(727, Door.Constructor(Vector3(5861.003f, 3923.825f, 69.04529f)), owning_building_guid = 10)
      LocalObject(728, Door.Constructor(Vector3(5862.828f, 3930.111f, 74.05129f)), owning_building_guid = 10)
      LocalObject(729, Door.Constructor(Vector3(5884.153f, 4035.028f, 64.07529f)), owning_building_guid = 10)
      LocalObject(730, Door.Constructor(Vector3(5889.772f, 3874.695f, 72.03929f)), owning_building_guid = 10)
      LocalObject(735, Door.Constructor(Vector3(5891.841f, 4018.54f, 72.03829f)), owning_building_guid = 10)
      LocalObject(736, Door.Constructor(Vector3(5906.26f, 3882.384f, 64.07529f)), owning_building_guid = 10)
      LocalObject(737, Door.Constructor(Vector3(5926.925f, 3943.303f, 64.07529f)), owning_building_guid = 10)
      LocalObject(738, Door.Constructor(Vector3(5934.614f, 3926.814f, 72.03829f)), owning_building_guid = 10)
      LocalObject(739, Door.Constructor(Vector3(5948.689f, 3915.614f, 64.04529f)), owning_building_guid = 10)
      LocalObject(1101, Door.Constructor(Vector3(5826.184f, 3951.173f, 64.04529f)), owning_building_guid = 10)
      LocalObject(1102, Door.Constructor(Vector3(5829.564f, 3943.922f, 64.04529f)), owning_building_guid = 10)
      LocalObject(1103, Door.Constructor(Vector3(5834.579f, 3979.99f, 74.04529f)), owning_building_guid = 10)
      LocalObject(1104, Door.Constructor(Vector3(5835.857f, 3977.751f, 69.04529f)), owning_building_guid = 10)
      LocalObject(1105, Door.Constructor(Vector3(5840.929f, 3966.876f, 49.04529f)), owning_building_guid = 10)
      LocalObject(1106, Door.Constructor(Vector3(5841.417f, 3984.758f, 64.04529f)), owning_building_guid = 10)
      LocalObject(1107, Door.Constructor(Vector3(5846.733f, 3982.823f, 64.04529f)), owning_building_guid = 10)
      LocalObject(1108, Door.Constructor(Vector3(5847.69f, 3952.375f, 56.54529f)), owning_building_guid = 10)
      LocalObject(1109, Door.Constructor(Vector3(5850.114f, 3975.572f, 49.04529f)), owning_building_guid = 10)
      LocalObject(1110, Door.Constructor(Vector3(5850.114f, 3975.572f, 56.54529f)), owning_building_guid = 10)
      LocalObject(1111, Door.Constructor(Vector3(5856.143f, 3934.249f, 69.04529f)), owning_building_guid = 10)
      LocalObject(1112, Door.Constructor(Vector3(5856.387f, 3943.189f, 49.04529f)), owning_building_guid = 10)
      LocalObject(1113, Door.Constructor(Vector3(5857.037f, 3931.831f, 74.04529f)), owning_building_guid = 10)
      LocalObject(1114, Door.Constructor(Vector3(5860.257f, 3953.821f, 56.54529f)), owning_building_guid = 10)
      LocalObject(1115, Door.Constructor(Vector3(5861.703f, 3941.255f, 56.54529f)), owning_building_guid = 10)
      LocalObject(1116, Door.Constructor(Vector3(5865.084f, 3934.004f, 64.04529f)), owning_building_guid = 10)
      LocalObject(1117, Door.Constructor(Vector3(5867.019f, 3939.32f, 64.04529f)), owning_building_guid = 10)
      LocalObject(1118, Door.Constructor(Vector3(5869.931f, 3980.399f, 49.04529f)), owning_building_guid = 10)
      LocalObject(1119, Door.Constructor(Vector3(5881.52f, 3946.082f, 56.54529f)), owning_building_guid = 10)
      LocalObject(1120, Door.Constructor(Vector3(5885.877f, 3974.595f, 49.04529f)), owning_building_guid = 10)
      LocalObject(1121, Door.Constructor(Vector3(5887.812f, 3979.911f, 49.04529f)), owning_building_guid = 10)
      LocalObject(1122, Door.Constructor(Vector3(5923.087f, 3961.052f, 56.54529f)), owning_building_guid = 10)
      LocalObject(1200, Door.Constructor(Vector3(5819.57f, 3943.647f, 64.80429f)), owning_building_guid = 10)
      LocalObject(3590, Door.Constructor(Vector3(5859.177f, 3973.473f, 56.87829f)), owning_building_guid = 10)
      LocalObject(3591, Door.Constructor(Vector3(5862.259f, 3966.863f, 56.87829f)), owning_building_guid = 10)
      LocalObject(3592, Door.Constructor(Vector3(5865.34f, 3960.257f, 56.87829f)), owning_building_guid = 10)
      LocalObject(
        1264,
        IFFLock.Constructor(Vector3(5818.371f, 3939.733f, 64.00429f), Vector3(0, 0, 245)),
        owning_building_guid = 10,
        door_guid = 1200
      )
      LocalObject(
        1521,
        IFFLock.Constructor(Vector3(5828.808f, 3988.036f, 68.98629f), Vector3(0, 0, 335)),
        owning_building_guid = 10,
        door_guid = 724
      )
      LocalObject(
        1522,
        IFFLock.Constructor(Vector3(5828.929f, 3979.495f, 73.98529f), Vector3(0, 0, 245)),
        owning_building_guid = 10,
        door_guid = 723
      )
      LocalObject(
        1523,
        IFFLock.Constructor(Vector3(5836.886f, 3987.717f, 73.98529f), Vector3(0, 0, 65)),
        owning_building_guid = 10,
        door_guid = 725
      )
      LocalObject(
        1524,
        IFFLock.Constructor(Vector3(5841.116f, 3968.698f, 48.86029f), Vector3(0, 0, 65)),
        owning_building_guid = 10,
        door_guid = 1105
      )
      LocalObject(
        1525,
        IFFLock.Constructor(Vector3(5848.347f, 3975.642f, 56.36029f), Vector3(0, 0, 335)),
        owning_building_guid = 10,
        door_guid = 1110
      )
      LocalObject(
        1526,
        IFFLock.Constructor(Vector3(5854.769f, 3924.082f, 73.98529f), Vector3(0, 0, 245)),
        owning_building_guid = 10,
        door_guid = 726
      )
      LocalObject(
        1527,
        IFFLock.Constructor(Vector3(5862.079f, 3953.633f, 56.36029f), Vector3(0, 0, 155)),
        owning_building_guid = 10,
        door_guid = 1114
      )
      LocalObject(
        1528,
        IFFLock.Constructor(Vector3(5862.724f, 3932.308f, 73.98529f), Vector3(0, 0, 65)),
        owning_building_guid = 10,
        door_guid = 728
      )
      LocalObject(
        1529,
        IFFLock.Constructor(Vector3(5863.203f, 3923.946f, 68.98629f), Vector3(0, 0, 155)),
        owning_building_guid = 10,
        door_guid = 727
      )
      LocalObject(
        1530,
        IFFLock.Constructor(Vector3(5887.743f, 3978.144f, 48.86029f), Vector3(0, 0, 245)),
        owning_building_guid = 10,
        door_guid = 1121
      )
      LocalObject(
        1535,
        IFFLock.Constructor(Vector3(5950.891f, 3915.729f, 63.98429f), Vector3(0, 0, 155)),
        owning_building_guid = 10,
        door_guid = 739
      )
      LocalObject(1997, Locker.Constructor(Vector3(5835.257f, 3944.188f, 47.52429f)), owning_building_guid = 10)
      LocalObject(1998, Locker.Constructor(Vector3(5836.469f, 3944.753f, 47.52429f)), owning_building_guid = 10)
      LocalObject(1999, Locker.Constructor(Vector3(5837.68f, 3945.318f, 47.52429f)), owning_building_guid = 10)
      LocalObject(2000, Locker.Constructor(Vector3(5838.88f, 3945.877f, 47.52429f)), owning_building_guid = 10)
      LocalObject(2001, Locker.Constructor(Vector3(5842.995f, 3947.796f, 47.52429f)), owning_building_guid = 10)
      LocalObject(2002, Locker.Constructor(Vector3(5844.206f, 3948.361f, 47.52429f)), owning_building_guid = 10)
      LocalObject(2003, Locker.Constructor(Vector3(5844.654f, 3970.664f, 55.28529f)), owning_building_guid = 10)
      LocalObject(2004, Locker.Constructor(Vector3(5845.417f, 3948.926f, 47.52429f)), owning_building_guid = 10)
      LocalObject(2005, Locker.Constructor(Vector3(5845.695f, 3971.149f, 55.28529f)), owning_building_guid = 10)
      LocalObject(2006, Locker.Constructor(Vector3(5846.617f, 3949.485f, 47.52429f)), owning_building_guid = 10)
      LocalObject(2007, Locker.Constructor(Vector3(5846.734f, 3971.634f, 55.28529f)), owning_building_guid = 10)
      LocalObject(2008, Locker.Constructor(Vector3(5847.79f, 3972.126f, 55.28529f)), owning_building_guid = 10)
      LocalObject(
        2505,
        Terminal.Constructor(Vector3(5836.502f, 3976.372f, 63.85329f), order_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2506,
        Terminal.Constructor(Vector3(5847.214f, 3966.046f, 56.61429f), order_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2507,
        Terminal.Constructor(Vector3(5848.791f, 3962.665f, 56.61429f), order_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2508,
        Terminal.Constructor(Vector3(5850.392f, 3959.231f, 56.61429f), order_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2509,
        Terminal.Constructor(Vector3(5855.501f, 3935.63f, 63.85329f), order_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2510,
        Terminal.Constructor(Vector3(5866.36f, 3965.499f, 63.85329f), order_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        3471,
        Terminal.Constructor(Vector3(5849.086f, 3962.405f, 69.05229f), spawn_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        3472,
        Terminal.Constructor(Vector3(5857.855f, 3975.604f, 57.15829f), spawn_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        3473,
        Terminal.Constructor(Vector3(5860.94f, 3968.997f, 57.15829f), spawn_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        3474,
        Terminal.Constructor(Vector3(5864.018f, 3962.39f, 57.15829f), spawn_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        3475,
        Terminal.Constructor(Vector3(5874.211f, 3952.153f, 49.08129f), spawn_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        3476,
        Terminal.Constructor(Vector3(5906.105f, 3940.544f, 56.58129f), spawn_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        3729,
        Terminal.Constructor(Vector3(5924.29f, 3901.508f, 64.42629f), vehicle_terminal_combined),
        owning_building_guid = 10
      )
      LocalObject(
        2321,
        VehicleSpawnPad.Constructor(Vector3(5918.444f, 3913.83f, 60.26929f), mb_pad_creation, Vector3(0, 0, -25)),
        owning_building_guid = 10,
        terminal_guid = 3729
      )
      LocalObject(3229, ResourceSilo.Constructor(Vector3(5762.156f, 4011.236f, 69.55929f)), owning_building_guid = 10)
      LocalObject(
        3326,
        SpawnTube.Constructor(Vector3(5859.132f, 3974.611f, 55.02429f), Vector3(0, 0, 155)),
        owning_building_guid = 10
      )
      LocalObject(
        3327,
        SpawnTube.Constructor(Vector3(5862.213f, 3968.003f, 55.02429f), Vector3(0, 0, 155)),
        owning_building_guid = 10
      )
      LocalObject(
        3328,
        SpawnTube.Constructor(Vector3(5865.293f, 3961.397f, 55.02429f), Vector3(0, 0, 155)),
        owning_building_guid = 10
      )
      LocalObject(
        2349,
        ProximityTerminal.Constructor(Vector3(5840.722f, 3947.338f, 47.52429f), medical_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2350,
        ProximityTerminal.Constructor(Vector3(5865.98f, 3965.323f, 67.52429f), medical_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2783,
        ProximityTerminal.Constructor(Vector3(5792.869f, 3953.108f, 70.82929f), pad_landing_frame),
        owning_building_guid = 10
      )
      LocalObject(
        2784,
        Terminal.Constructor(Vector3(5792.869f, 3953.108f, 70.82929f), air_rearm_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2786,
        ProximityTerminal.Constructor(Vector3(5807.419f, 4019.102f, 70.85229f), pad_landing_frame),
        owning_building_guid = 10
      )
      LocalObject(
        2787,
        Terminal.Constructor(Vector3(5807.419f, 4019.102f, 70.85229f), air_rearm_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2789,
        ProximityTerminal.Constructor(Vector3(5846.158f, 3892.727f, 72.9903f), pad_landing_frame),
        owning_building_guid = 10
      )
      LocalObject(
        2790,
        Terminal.Constructor(Vector3(5846.158f, 3892.727f, 72.9903f), air_rearm_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2792,
        ProximityTerminal.Constructor(Vector3(5906.24f, 3962.275f, 70.82929f), pad_landing_frame),
        owning_building_guid = 10
      )
      LocalObject(
        2793,
        Terminal.Constructor(Vector3(5906.24f, 3962.275f, 70.82929f), air_rearm_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        3184,
        ProximityTerminal.Constructor(Vector3(5789.681f, 3929.304f, 61.72429f), repair_silo),
        owning_building_guid = 10
      )
      LocalObject(
        3185,
        Terminal.Constructor(Vector3(5789.681f, 3929.304f, 61.72429f), ground_rearm_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        3188,
        ProximityTerminal.Constructor(Vector3(5909.615f, 3985.44f, 61.72429f), repair_silo),
        owning_building_guid = 10
      )
      LocalObject(
        3189,
        Terminal.Constructor(Vector3(5909.615f, 3985.44f, 61.72429f), ground_rearm_terminal),
        owning_building_guid = 10
      )
      LocalObject(
        2272,
        FacilityTurret.Constructor(Vector3(5739.06f, 4012.993f, 71.03229f), manned_turret),
        owning_building_guid = 10
      )
      TurretToWeapon(2272, 5018)
      LocalObject(
        2274,
        FacilityTurret.Constructor(Vector3(5795.487f, 3886.714f, 71.03229f), manned_turret),
        owning_building_guid = 10
      )
      TurretToWeapon(2274, 5019)
      LocalObject(
        2275,
        FacilityTurret.Constructor(Vector3(5823.173f, 4054.663f, 71.03229f), manned_turret),
        owning_building_guid = 10
      )
      TurretToWeapon(2275, 5020)
      LocalObject(
        2276,
        FacilityTurret.Constructor(Vector3(5824.284f, 3830.208f, 71.03229f), manned_turret),
        owning_building_guid = 10
      )
      TurretToWeapon(2276, 5021)
      LocalObject(
        2278,
        FacilityTurret.Constructor(Vector3(5878.118f, 4077.826f, 71.03229f), manned_turret),
        owning_building_guid = 10
      )
      TurretToWeapon(2278, 5022)
      LocalObject(
        2280,
        FacilityTurret.Constructor(Vector3(5963.377f, 3895.057f, 71.03229f), manned_turret),
        owning_building_guid = 10
      )
      TurretToWeapon(2280, 5023)
      LocalObject(
        2892,
        Painbox.Constructor(Vector3(5897.333f, 3987.531f, 52.39529f), painbox),
        owning_building_guid = 10
      )
      LocalObject(
        2909,
        Painbox.Constructor(Vector3(5856.637f, 3962.52f, 59.97269f), painbox_continuous),
        owning_building_guid = 10
      )
      LocalObject(
        2926,
        Painbox.Constructor(Vector3(5885.503f, 3979.668f, 48.98329f), painbox_door_radius),
        owning_building_guid = 10
      )
      LocalObject(
        2969,
        Painbox.Constructor(Vector3(5846.343f, 3951.793f, 58.20289f), painbox_door_radius_continuous),
        owning_building_guid = 10
      )
      LocalObject(
        2970,
        Painbox.Constructor(Vector3(5849.664f, 3976.536f, 56.22429f), painbox_door_radius_continuous),
        owning_building_guid = 10
      )
      LocalObject(
        2971,
        Painbox.Constructor(Vector3(5860.732f, 3952.626f, 56.28879f), painbox_door_radius_continuous),
        owning_building_guid = 10
      )
      LocalObject(394, Generator.Constructor(Vector3(5901.899f, 3986.507f, 46.23029f)), owning_building_guid = 10)
      LocalObject(
        377,
        Terminal.Constructor(Vector3(5894.495f, 3983.003f, 47.52429f), gen_control),
        owning_building_guid = 10
      )
    }

    Building15()

    def Building15(): Unit = { // Name: Pamba Type: amp_station GUID: 13, MapID: 15
      LocalBuilding(
        "Pamba",
        13,
        15,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(7310f, 3078f, 62.94272f),
            Vector3(0f, 0f, 339f),
            amp_station
          )
        )
      )
      LocalObject(
        293,
        CaptureTerminal.Constructor(Vector3(7306.887f, 3079.198f, 74.45072f), capture_terminal),
        owning_building_guid = 13
      )
      LocalObject(224, Door.Constructor(Vector3(7307.758f, 3071.571f, 75.84472f)), owning_building_guid = 13)
      LocalObject(225, Door.Constructor(Vector3(7312.639f, 3084.276f, 75.84472f)), owning_building_guid = 13)
      LocalObject(786, Door.Constructor(Vector3(7226.648f, 3050.547f, 64.69372f)), owning_building_guid = 13)
      LocalObject(787, Door.Constructor(Vector3(7233.168f, 3067.532f, 72.65672f)), owning_building_guid = 13)
      LocalObject(788, Door.Constructor(Vector3(7262.918f, 3145.033f, 64.69372f)), owning_building_guid = 13)
      LocalObject(789, Door.Constructor(Vector3(7267.717f, 3179.922f, 64.66372f)), owning_building_guid = 13)
      LocalObject(790, Door.Constructor(Vector3(7269.438f, 3162.018f, 72.65672f)), owning_building_guid = 13)
      LocalObject(791, Door.Constructor(Vector3(7294.999f, 3051.007f, 74.66972f)), owning_building_guid = 13)
      LocalObject(792, Door.Constructor(Vector3(7296.16f, 2993.103f, 72.65771f)), owning_building_guid = 13)
      LocalObject(793, Door.Constructor(Vector3(7297.278f, 3044.858f, 69.66372f)), owning_building_guid = 13)
      LocalObject(794, Door.Constructor(Vector3(7303.458f, 3047.761f, 74.66972f)), owning_building_guid = 13)
      LocalObject(795, Door.Constructor(Vector3(7313.143f, 2986.584f, 64.69372f)), owning_building_guid = 13)
      LocalObject(796, Door.Constructor(Vector3(7316.934f, 3108.089f, 74.66972f)), owning_building_guid = 13)
      LocalObject(797, Door.Constructor(Vector3(7321.096f, 3172.485f, 64.69372f)), owning_building_guid = 13)
      LocalObject(798, Door.Constructor(Vector3(7322.723f, 3111.143f, 69.66372f)), owning_building_guid = 13)
      LocalObject(799, Door.Constructor(Vector3(7325.393f, 3104.843f, 74.66972f)), owning_building_guid = 13)
      LocalObject(800, Door.Constructor(Vector3(7338.079f, 3165.966f, 72.65771f)), owning_building_guid = 13)
      LocalObject(801, Door.Constructor(Vector3(7343.737f, 2997.896f, 72.65771f)), owning_building_guid = 13)
      LocalObject(802, Door.Constructor(Vector3(7350.257f, 3014.88f, 64.69372f)), owning_building_guid = 13)
      LocalObject(1165, Door.Constructor(Vector3(7252.816f, 3129.943f, 57.16372f)), owning_building_guid = 13)
      LocalObject(1166, Door.Constructor(Vector3(7263.755f, 3091.467f, 49.66372f)), owning_building_guid = 13)
      LocalObject(1167, Door.Constructor(Vector3(7268.922f, 3093.768f, 49.66372f)), owning_building_guid = 13)
      LocalObject(1168, Door.Constructor(Vector3(7275.825f, 3078.265f, 49.66372f)), owning_building_guid = 13)
      LocalObject(1169, Door.Constructor(Vector3(7290.196f, 3059.895f, 64.66372f)), owning_building_guid = 13)
      LocalObject(1170, Door.Constructor(Vector3(7292.497f, 3054.727f, 64.66372f)), owning_building_guid = 13)
      LocalObject(1171, Door.Constructor(Vector3(7292.46f, 3110.44f, 57.16372f)), owning_building_guid = 13)
      LocalObject(1172, Door.Constructor(Vector3(7293.063f, 3067.363f, 49.66372f)), owning_building_guid = 13)
      LocalObject(1173, Door.Constructor(Vector3(7293.063f, 3067.363f, 57.16372f)), owning_building_guid = 13)
      LocalObject(1174, Door.Constructor(Vector3(7300.676f, 3053.12f, 74.66372f)), owning_building_guid = 13)
      LocalObject(1175, Door.Constructor(Vector3(7301.399f, 3055.594f, 69.66372f)), owning_building_guid = 13)
      LocalObject(1176, Door.Constructor(Vector3(7301.664f, 3089.769f, 57.16372f)), owning_building_guid = 13)
      LocalObject(1177, Door.Constructor(Vector3(7305.7f, 3066.797f, 49.66372f)), owning_building_guid = 13)
      LocalObject(1178, Door.Constructor(Vector3(7307.398f, 3104.706f, 64.66372f)), owning_building_guid = 13)
      LocalObject(1179, Door.Constructor(Vector3(7309.699f, 3099.539f, 57.16372f)), owning_building_guid = 13)
      LocalObject(1180, Door.Constructor(Vector3(7311.434f, 3081.734f, 57.16372f)), owning_building_guid = 13)
      LocalObject(1181, Door.Constructor(Vector3(7312f, 3094.371f, 49.66372f)), owning_building_guid = 13)
      LocalObject(1182, Door.Constructor(Vector3(7312.565f, 3107.007f, 64.66372f)), owning_building_guid = 13)
      LocalObject(1183, Door.Constructor(Vector3(7318.601f, 3100.406f, 69.66372f)), owning_building_guid = 13)
      LocalObject(1184, Door.Constructor(Vector3(7319.719f, 3102.728f, 74.66372f)), owning_building_guid = 13)
      LocalObject(1185, Door.Constructor(Vector3(7327.238f, 3067.098f, 64.66372f)), owning_building_guid = 13)
      LocalObject(1186, Door.Constructor(Vector3(7330.105f, 3074.567f, 64.66372f)), owning_building_guid = 13)
      LocalObject(1203, Door.Constructor(Vector3(7337.246f, 3067.569f, 65.42272f)), owning_building_guid = 13)
      LocalObject(3609, Door.Constructor(Vector3(7288.277f, 3075.341f, 57.49672f)), owning_building_guid = 13)
      LocalObject(3610, Door.Constructor(Vector3(7290.891f, 3082.15f, 57.49672f)), owning_building_guid = 13)
      LocalObject(3611, Door.Constructor(Vector3(7293.503f, 3088.955f, 57.49672f)), owning_building_guid = 13)
      LocalObject(
        1267,
        IFFLock.Constructor(Vector3(7340.894f, 3069.425f, 64.62272f), Vector3(0, 0, 111)),
        owning_building_guid = 13,
        door_guid = 1203
      )
      LocalObject(
        1572,
        IFFLock.Constructor(Vector3(7265.074f, 3092.645f, 49.47872f), Vector3(0, 0, 111)),
        owning_building_guid = 13,
        door_guid = 1166
      )
      LocalObject(
        1573,
        IFFLock.Constructor(Vector3(7266.105f, 3181.426f, 64.60272f), Vector3(0, 0, 21)),
        owning_building_guid = 13,
        door_guid = 789
      )
      LocalObject(
        1574,
        IFFLock.Constructor(Vector3(7293.516f, 3049.412f, 74.60372f), Vector3(0, 0, 291)),
        owning_building_guid = 13,
        door_guid = 791
      )
      LocalObject(
        1575,
        IFFLock.Constructor(Vector3(7294.24f, 3066.043f, 56.97872f), Vector3(0, 0, 201)),
        owning_building_guid = 13,
        door_guid = 1173
      )
      LocalObject(
        1576,
        IFFLock.Constructor(Vector3(7298.897f, 3043.379f, 69.60472f), Vector3(0, 0, 201)),
        owning_building_guid = 13,
        door_guid = 793
      )
      LocalObject(
        1577,
        IFFLock.Constructor(Vector3(7300.534f, 3091.211f, 56.97872f), Vector3(0, 0, 21)),
        owning_building_guid = 13,
        door_guid = 1176
      )
      LocalObject(
        1578,
        IFFLock.Constructor(Vector3(7304.259f, 3065.666f, 49.47872f), Vector3(0, 0, 291)),
        owning_building_guid = 13,
        door_guid = 1177
      )
      LocalObject(
        1579,
        IFFLock.Constructor(Vector3(7304.958f, 3049.399f, 74.60372f), Vector3(0, 0, 111)),
        owning_building_guid = 13,
        door_guid = 794
      )
      LocalObject(
        1580,
        IFFLock.Constructor(Vector3(7315.425f, 3106.488f, 74.60372f), Vector3(0, 0, 291)),
        owning_building_guid = 13,
        door_guid = 796
      )
      LocalObject(
        1581,
        IFFLock.Constructor(Vector3(7321.108f, 3112.641f, 69.60472f), Vector3(0, 0, 21)),
        owning_building_guid = 13,
        door_guid = 798
      )
      LocalObject(
        1582,
        IFFLock.Constructor(Vector3(7326.869f, 3106.479f, 74.60372f), Vector3(0, 0, 111)),
        owning_building_guid = 13,
        door_guid = 799
      )
      LocalObject(2082, Locker.Constructor(Vector3(7297.157f, 3068.085f, 55.90372f)), owning_building_guid = 13)
      LocalObject(2083, Locker.Constructor(Vector3(7298.244f, 3067.668f, 55.90372f)), owning_building_guid = 13)
      LocalObject(2084, Locker.Constructor(Vector3(7299.314f, 3067.257f, 55.90372f)), owning_building_guid = 13)
      LocalObject(2085, Locker.Constructor(Vector3(7300.387f, 3066.845f, 55.90372f)), owning_building_guid = 13)
      LocalObject(2086, Locker.Constructor(Vector3(7314.258f, 3082.969f, 48.14272f)), owning_building_guid = 13)
      LocalObject(2087, Locker.Constructor(Vector3(7315.494f, 3082.495f, 48.14272f)), owning_building_guid = 13)
      LocalObject(2088, Locker.Constructor(Vector3(7316.741f, 3082.016f, 48.14272f)), owning_building_guid = 13)
      LocalObject(2089, Locker.Constructor(Vector3(7317.989f, 3081.537f, 48.14272f)), owning_building_guid = 13)
      LocalObject(2090, Locker.Constructor(Vector3(7322.228f, 3079.91f, 48.14272f)), owning_building_guid = 13)
      LocalObject(2091, Locker.Constructor(Vector3(7323.464f, 3079.435f, 48.14272f)), owning_building_guid = 13)
      LocalObject(2092, Locker.Constructor(Vector3(7324.711f, 3078.957f, 48.14272f)), owning_building_guid = 13)
      LocalObject(2093, Locker.Constructor(Vector3(7325.959f, 3078.478f, 48.14272f)), owning_building_guid = 13)
      LocalObject(
        2534,
        Terminal.Constructor(Vector3(7289.024f, 3086.048f, 64.47172f), order_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2535,
        Terminal.Constructor(Vector3(7301.943f, 3057.017f, 64.47172f), order_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2536,
        Terminal.Constructor(Vector3(7301.93f, 3071.895f, 57.23272f), order_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2537,
        Terminal.Constructor(Vector3(7303.267f, 3075.378f, 57.23272f), order_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2538,
        Terminal.Constructor(Vector3(7304.625f, 3078.915f, 57.23272f), order_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2539,
        Terminal.Constructor(Vector3(7318.053f, 3098.985f, 64.47172f), order_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        3491,
        Terminal.Constructor(Vector3(7279.365f, 3131.972f, 57.19972f), spawn_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        3492,
        Terminal.Constructor(Vector3(7287.663f, 3072.91f, 57.77672f), spawn_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        3493,
        Terminal.Constructor(Vector3(7290.272f, 3079.719f, 57.77672f), spawn_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        3494,
        Terminal.Constructor(Vector3(7292.887f, 3086.522f, 57.77672f), spawn_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        3495,
        Terminal.Constructor(Vector3(7293.17f, 3100.966f, 49.69972f), spawn_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        3496,
        Terminal.Constructor(Vector3(7303.249f, 3075.771f, 69.67072f), spawn_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        3732,
        Terminal.Constructor(Vector3(7294.814f, 3172.17f, 65.04472f), vehicle_terminal_combined),
        owning_building_guid = 13
      )
      LocalObject(
        2326,
        VehicleSpawnPad.Constructor(Vector3(7290.01f, 3159.406f, 60.88772f), mb_pad_creation, Vector3(0, 0, 201)),
        owning_building_guid = 13,
        terminal_guid = 3732
      )
      LocalObject(3232, ResourceSilo.Constructor(Vector3(7328.51f, 2979.317f, 70.17772f)), owning_building_guid = 13)
      LocalObject(
        3345,
        SpawnTube.Constructor(Vector3(7287.49f, 3074.519f, 55.64272f), Vector3(0, 0, 21)),
        owning_building_guid = 13
      )
      LocalObject(
        3346,
        SpawnTube.Constructor(Vector3(7290.103f, 3081.325f, 55.64272f), Vector3(0, 0, 21)),
        owning_building_guid = 13
      )
      LocalObject(
        3347,
        SpawnTube.Constructor(Vector3(7292.715f, 3088.129f, 55.64272f), Vector3(0, 0, 21)),
        owning_building_guid = 13
      )
      LocalObject(
        2354,
        ProximityTerminal.Constructor(Vector3(7289.414f, 3085.896f, 68.14272f), medical_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2355,
        ProximityTerminal.Constructor(Vector3(7319.897f, 3080.221f, 48.14272f), medical_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2825,
        ProximityTerminal.Constructor(Vector3(7263.64f, 3116.974f, 71.44772f), pad_landing_frame),
        owning_building_guid = 13
      )
      LocalObject(
        2826,
        Terminal.Constructor(Vector3(7263.64f, 3116.974f, 71.44772f), air_rearm_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2828,
        ProximityTerminal.Constructor(Vector3(7291.409f, 3006.413f, 71.47072f), pad_landing_frame),
        owning_building_guid = 13
      )
      LocalObject(
        2829,
        Terminal.Constructor(Vector3(7291.409f, 3006.413f, 71.47072f), air_rearm_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2831,
        ProximityTerminal.Constructor(Vector3(7348.988f, 3041.79f, 71.44772f), pad_landing_frame),
        owning_building_guid = 13
      )
      LocalObject(
        2832,
        Terminal.Constructor(Vector3(7348.988f, 3041.79f, 71.44772f), air_rearm_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2834,
        ProximityTerminal.Constructor(Vector3(7355.405f, 3122.067f, 73.60872f), pad_landing_frame),
        owning_building_guid = 13
      )
      LocalObject(
        2835,
        Terminal.Constructor(Vector3(7355.405f, 3122.067f, 73.60872f), air_rearm_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        3208,
        ProximityTerminal.Constructor(Vector3(7244.632f, 3103.31f, 62.34272f), repair_silo),
        owning_building_guid = 13
      )
      LocalObject(
        3209,
        Terminal.Constructor(Vector3(7244.632f, 3103.31f, 62.34272f), ground_rearm_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        3212,
        ProximityTerminal.Constructor(Vector3(7368.326f, 3056.032f, 62.34272f), repair_silo),
        owning_building_guid = 13
      )
      LocalObject(
        3213,
        Terminal.Constructor(Vector3(7368.326f, 3056.032f, 62.34272f), ground_rearm_terminal),
        owning_building_guid = 13
      )
      LocalObject(
        2296,
        FacilityTurret.Constructor(Vector3(7200.055f, 3016.477f, 71.65072f), manned_turret),
        owning_building_guid = 13
      )
      TurretToWeapon(2296, 5024)
      LocalObject(
        2297,
        FacilityTurret.Constructor(Vector3(7254.885f, 2993.042f, 71.65072f), manned_turret),
        owning_building_guid = 13
      )
      TurretToWeapon(2297, 5025)
      LocalObject(
        2298,
        FacilityTurret.Constructor(Vector3(7272.302f, 3204.769f, 71.65072f), manned_turret),
        owning_building_guid = 13
      )
      TurretToWeapon(2298, 5026)
      LocalObject(
        2299,
        FacilityTurret.Constructor(Vector3(7343.29f, 2961.483f, 71.65072f), manned_turret),
        owning_building_guid = 13
      )
      TurretToWeapon(2299, 5027)
      LocalObject(
        2300,
        FacilityTurret.Constructor(Vector3(7394.93f, 3089.794f, 71.65072f), manned_turret),
        owning_building_guid = 13
      )
      TurretToWeapon(2300, 5028)
      LocalObject(
        2301,
        FacilityTurret.Constructor(Vector3(7415.573f, 3149.761f, 71.65072f), manned_turret),
        owning_building_guid = 13
      )
      TurretToWeapon(2301, 5029)
      LocalObject(
        2895,
        Painbox.Constructor(Vector3(7251.66f, 3093.022f, 53.01372f), painbox),
        owning_building_guid = 13
      )
      LocalObject(
        2912,
        Painbox.Constructor(Vector3(7297.921f, 3081.123f, 60.59112f), painbox_continuous),
        owning_building_guid = 13
      )
      LocalObject(
        2929,
        Painbox.Constructor(Vector3(7265.533f, 3089.975f, 49.60172f), painbox_door_radius),
        owning_building_guid = 13
      )
      LocalObject(
        2978,
        Painbox.Constructor(Vector3(7292.683f, 3066.37f, 56.84272f), painbox_door_radius_continuous),
        owning_building_guid = 13
      )
      LocalObject(
        2979,
        Painbox.Constructor(Vector3(7302.193f, 3090.941f, 56.90722f), painbox_door_radius_continuous),
        owning_building_guid = 13
      )
      LocalObject(
        2980,
        Painbox.Constructor(Vector3(7312.788f, 3081.168f, 58.82132f), painbox_door_radius_continuous),
        owning_building_guid = 13
      )
      LocalObject(397, Generator.Constructor(Vector3(7249.224f, 3097.018f, 46.84872f)), owning_building_guid = 13)
      LocalObject(
        380,
        Terminal.Constructor(Vector3(7256.889f, 3094.126f, 48.14272f), gen_control),
        owning_building_guid = 13
      )
    }

    Building59()

    def Building59(): Unit = { // Name: bunkerg2 Type: bunker_gauntlet GUID: 16, MapID: 59
      LocalBuilding(
        "bunkerg2",
        16,
        59,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(2610f, 1338f, 64.04956f),
            Vector3(0f, 0f, 123f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(513, Door.Constructor(Vector3(2598.02f, 1359.938f, 65.57056f)), owning_building_guid = 16)
      LocalObject(514, Door.Constructor(Vector3(2625.164f, 1318.16f, 65.57056f)), owning_building_guid = 16)
    }

    Building58()

    def Building58(): Unit = { // Name: bunkerg1 Type: bunker_gauntlet GUID: 17, MapID: 58
      LocalBuilding(
        "bunkerg1",
        17,
        58,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(3906f, 4474f, 88.9639f),
            Vector3(0f, 0f, 110f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(574, Door.Constructor(Vector3(3899.262f, 4498.07f, 90.4849f)), owning_building_guid = 17)
      LocalObject(575, Door.Constructor(Vector3(3916.312f, 4451.257f, 90.4849f)), owning_building_guid = 17)
    }

    Building60()

    def Building60(): Unit = { // Name: bunkerg3 Type: bunker_gauntlet GUID: 18, MapID: 60
      LocalBuilding(
        "bunkerg3",
        18,
        60,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4870f, 4466f, 53.91644f),
            Vector3(0f, 0f, 350f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(613, Door.Constructor(Vector3(4845.148f, 4468.44f, 55.43744f)), owning_building_guid = 18)
      LocalObject(623, Door.Constructor(Vector3(4894.214f, 4459.8f, 55.43744f)), owning_building_guid = 18)
    }

    Building54()

    def Building54(): Unit = { // Name: bunker11 Type: bunker_lg GUID: 19, MapID: 54
      LocalBuilding(
        "bunker11",
        19,
        54,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(600f, 6826f, 59.91754f), Vector3(0f, 0f, 354f), bunker_lg)
        )
      )
      LocalObject(415, Door.Constructor(Vector3(602.859f, 6828.271f, 61.43854f)), owning_building_guid = 19)
    }

    Building56()

    def Building56(): Unit = { // Name: bunker6 Type: bunker_lg GUID: 20, MapID: 56
      LocalBuilding(
        "bunker6",
        20,
        56,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(708f, 2384f, 53.85228f), Vector3(0f, 0f, 112f), bunker_lg)
        )
      )
      LocalObject(447, Door.Constructor(Vector3(704.653f, 2385.458f, 55.37328f)), owning_building_guid = 20)
    }

    Building52()

    def Building52(): Unit = { // Name: bunker8 Type: bunker_lg GUID: 21, MapID: 52
      LocalBuilding(
        "bunker8",
        21,
        52,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(1464f, 2568f, 45.02957f), Vector3(0f, 0f, 355f), bunker_lg)
        )
      )
      LocalObject(486, Door.Constructor(Vector3(1466.819f, 2570.32f, 46.55057f)), owning_building_guid = 21)
    }

    Building48()

    def Building48(): Unit = { // Name: bunker3 Type: bunker_lg GUID: 22, MapID: 48
      LocalBuilding(
        "bunker3",
        22,
        48,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5132f, 4998f, 40.94622f), Vector3(0f, 0f, 107f), bunker_lg)
        )
      )
      LocalObject(660, Door.Constructor(Vector3(5128.793f, 4999.745f, 42.46722f)), owning_building_guid = 22)
    }

    Building71()

    def Building71(): Unit = { // Name: bunker_lg Type: bunker_lg GUID: 23, MapID: 71
      LocalBuilding(
        "bunker_lg",
        23,
        71,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5208f, 3856f, 37.32201f), Vector3(0f, 0f, 69f), bunker_lg)
        )
      )
      LocalObject(677, Door.Constructor(Vector3(5206.547f, 3859.349f, 38.84301f)), owning_building_guid = 23)
    }

    Building55()

    def Building55(): Unit = { // Name: bunker12 Type: bunker_lg GUID: 24, MapID: 55
      LocalBuilding(
        "bunker12",
        24,
        55,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(6632f, 2306f, 62.59754f), Vector3(0f, 0f, 238f), bunker_lg)
        )
      )
      LocalObject(740, Door.Constructor(Vector3(6632.788f, 2302.435f, 64.11855f)), owning_building_guid = 24)
    }

    Building51()

    def Building51(): Unit = { // Name: bunker7 Type: bunker_sm GUID: 25, MapID: 51
      LocalBuilding(
        "bunker7",
        25,
        51,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(1700f, 2702f, 45.14794f), Vector3(0f, 0f, 141f), bunker_sm)
        )
      )
      LocalObject(508, Door.Constructor(Vector3(1699.083f, 2702.814f, 46.66894f)), owning_building_guid = 25)
    }

    Building49()

    def Building49(): Unit = { // Name: bunker4 Type: bunker_sm GUID: 26, MapID: 49
      LocalBuilding(
        "bunker4",
        26,
        49,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(2958f, 1284f, 48.15265f), Vector3(0f, 0f, 195f), bunker_sm)
        )
      )
      LocalObject(549, Door.Constructor(Vector3(2956.802f, 1283.736f, 49.67365f)), owning_building_guid = 26)
    }

    Building53()

    def Building53(): Unit = { // Name: bunker10 Type: bunker_sm GUID: 27, MapID: 53
      LocalBuilding(
        "bunker10",
        27,
        53,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4238f, 7044f, 51.17726f), Vector3(0f, 0f, 225f), bunker_sm)
        )
      )
      LocalObject(612, Door.Constructor(Vector3(4237.095f, 7043.173f, 52.69826f)), owning_building_guid = 27)
    }

    Building50()

    def Building50(): Unit = { // Name: bunker5 Type: bunker_sm GUID: 28, MapID: 50
      LocalBuilding(
        "bunker5",
        28,
        50,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5022f, 4280f, 53.97397f), Vector3(0f, 0f, 79f), bunker_sm)
        )
      )
      LocalObject(643, Door.Constructor(Vector3(5022.288f, 4281.192f, 55.49497f)), owning_building_guid = 28)
    }

    Building46()

    def Building46(): Unit = { // Name: bunker1 Type: bunker_sm GUID: 29, MapID: 46
      LocalBuilding(
        "bunker1",
        29,
        46,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5160f, 3442f, 48.0232f), Vector3(0f, 0f, 225f), bunker_sm)
        )
      )
      LocalObject(670, Door.Constructor(Vector3(5159.095f, 3441.173f, 49.5442f)), owning_building_guid = 29)
    }

    Building47()

    def Building47(): Unit = { // Name: bunker2 Type: bunker_sm GUID: 30, MapID: 47
      LocalBuilding(
        "bunker2",
        30,
        47,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5220f, 5806f, 56.73086f), Vector3(0f, 0f, 163f), bunker_sm)
        )
      )
      LocalObject(678, Door.Constructor(Vector3(5218.845f, 5806.411f, 58.25186f)), owning_building_guid = 30)
    }

    Building57()

    def Building57(): Unit = { // Name: bunker9 Type: bunker_sm GUID: 31, MapID: 57
      LocalBuilding(
        "bunker9",
        31,
        57,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5694f, 6524f, 50.47251f), Vector3(0f, 0f, 119f), bunker_sm)
        )
      )
      LocalObject(710, Door.Constructor(Vector3(5693.454f, 6525.098f, 51.99351f)), owning_building_guid = 31)
    }

    Building70()

    def Building70(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 32, MapID: 70
      LocalBuilding(
        "bunker_sm",
        32,
        70,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(6854f, 3344f, 36.82418f), Vector3(0f, 0f, 274f), bunker_sm)
        )
      )
      LocalObject(762, Door.Constructor(Vector3(6854.031f, 3342.774f, 38.34518f)), owning_building_guid = 32)
    }

    Building2()

    def Building2(): Unit = { // Name: Bomazi Type: comm_station GUID: 33, MapID: 2
      LocalBuilding(
        "Bomazi",
        33,
        2,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(1194f, 4574f, 57.8283f),
            Vector3(0f, 0f, 331f),
            comm_station
          )
        )
      )
      LocalObject(
        280,
        CaptureTerminal.Constructor(Vector3(1269.762f, 4619.738f, 40.52831f), capture_terminal),
        owning_building_guid = 33
      )
      LocalObject(465, Door.Constructor(Vector3(1120.605f, 4564.948f, 59.5793f)), owning_building_guid = 33)
      LocalObject(466, Door.Constructor(Vector3(1123.788f, 4535.396f, 67.5433f)), owning_building_guid = 33)
      LocalObject(467, Door.Constructor(Vector3(1129.425f, 4580.859f, 67.5433f)), owning_building_guid = 33)
      LocalObject(468, Door.Constructor(Vector3(1139.7f, 4526.576f, 59.5793f)), owning_building_guid = 33)
      LocalObject(469, Door.Constructor(Vector3(1172.256f, 4576.027f, 64.5493f)), owning_building_guid = 33)
      LocalObject(470, Door.Constructor(Vector3(1176.651f, 4583.954f, 64.5493f)), owning_building_guid = 33)
      LocalObject(471, Door.Constructor(Vector3(1186.581f, 4569.64f, 71.9883f)), owning_building_guid = 33)
      LocalObject(472, Door.Constructor(Vector3(1210.693f, 4587.614f, 64.5493f)), owning_building_guid = 33)
      LocalObject(473, Door.Constructor(Vector3(1227.393f, 4633.211f, 59.5793f)), owning_building_guid = 33)
      LocalObject(474, Door.Constructor(Vector3(1241.469f, 4535.461f, 67.5433f)), owning_building_guid = 33)
      LocalObject(475, Door.Constructor(Vector3(1243.304f, 4624.392f, 67.5423f)), owning_building_guid = 33)
      LocalObject(476, Door.Constructor(Vector3(1250.289f, 4551.374f, 59.5793f)), owning_building_guid = 33)
      LocalObject(477, Door.Constructor(Vector3(1258.833f, 4624.957f, 59.5493f)), owning_building_guid = 33)
      LocalObject(881, Door.Constructor(Vector3(1178.067f, 4578.258f, 64.5493f)), owning_building_guid = 33)
      LocalObject(882, Door.Constructor(Vector3(1178.028f, 4619.441f, 52.0493f)), owning_building_guid = 33)
      LocalObject(883, Door.Constructor(Vector3(1184.265f, 4597.69f, 52.0493f)), owning_building_guid = 33)
      LocalObject(884, Door.Constructor(Vector3(1185.824f, 4592.252f, 59.5493f)), owning_building_guid = 33)
      LocalObject(885, Door.Constructor(Vector3(1188.143f, 4604.687f, 42.0493f)), owning_building_guid = 33)
      LocalObject(886, Door.Constructor(Vector3(1192.781f, 4629.557f, 52.0493f)), owning_building_guid = 33)
      LocalObject(887, Door.Constructor(Vector3(1194.38f, 4582.936f, 42.0493f)), owning_building_guid = 33)
      LocalObject(888, Door.Constructor(Vector3(1195.939f, 4577.499f, 52.0493f)), owning_building_guid = 33)
      LocalObject(889, Door.Constructor(Vector3(1206.815f, 4580.617f, 64.5493f)), owning_building_guid = 33)
      LocalObject(890, Door.Constructor(Vector3(1208.374f, 4575.179f, 59.5493f)), owning_building_guid = 33)
      LocalObject(891, Door.Constructor(Vector3(1209.894f, 4610.924f, 42.0493f)), owning_building_guid = 33)
      LocalObject(892, Door.Constructor(Vector3(1210.693f, 4587.614f, 54.5493f)), owning_building_guid = 33)
      LocalObject(893, Door.Constructor(Vector3(1211.453f, 4605.486f, 52.0493f)), owning_building_guid = 33)
      LocalObject(894, Door.Constructor(Vector3(1213.772f, 4617.921f, 52.0493f)), owning_building_guid = 33)
      LocalObject(895, Door.Constructor(Vector3(1215.371f, 4571.301f, 54.5493f)), owning_building_guid = 33)
      LocalObject(896, Door.Constructor(Vector3(1230.125f, 4581.416f, 42.0493f)), owning_building_guid = 33)
      LocalObject(897, Door.Constructor(Vector3(1236.362f, 4559.666f, 49.5493f)), owning_building_guid = 33)
      LocalObject(898, Door.Constructor(Vector3(1245.639f, 4609.404f, 42.0493f)), owning_building_guid = 33)
      LocalObject(899, Door.Constructor(Vector3(1247.997f, 4580.656f, 49.5493f)), owning_building_guid = 33)
      LocalObject(900, Door.Constructor(Vector3(1248.796f, 4557.346f, 42.0493f)), owning_building_guid = 33)
      LocalObject(901, Door.Constructor(Vector3(1256.553f, 4571.34f, 49.5493f)), owning_building_guid = 33)
      LocalObject(902, Door.Constructor(Vector3(1261.952f, 4614.082f, 42.0493f)), owning_building_guid = 33)
      LocalObject(903, Door.Constructor(Vector3(1268.189f, 4592.331f, 42.0493f)), owning_building_guid = 33)
      LocalObject(904, Door.Constructor(Vector3(1268.949f, 4610.203f, 42.0493f)), owning_building_guid = 33)
      LocalObject(1190, Door.Constructor(Vector3(1207.515f, 4561.857f, 60.3213f)), owning_building_guid = 33)
      LocalObject(3516, Door.Constructor(Vector3(1232.733f, 4568.232f, 49.88231f)), owning_building_guid = 33)
      LocalObject(3517, Door.Constructor(Vector3(1236.269f, 4574.61f, 49.88231f)), owning_building_guid = 33)
      LocalObject(3518, Door.Constructor(Vector3(1239.802f, 4580.985f, 49.88231f)), owning_building_guid = 33)
      LocalObject(
        1254,
        IFFLock.Constructor(Vector3(1211.467f, 4563.323f, 59.4803f), Vector3(0, 0, 119)),
        owning_building_guid = 33,
        door_guid = 1190
      )
      LocalObject(
        1323,
        IFFLock.Constructor(Vector3(1173.653f, 4574.325f, 64.4893f), Vector3(0, 0, 209)),
        owning_building_guid = 33,
        door_guid = 469
      )
      LocalObject(
        1324,
        IFFLock.Constructor(Vector3(1175.256f, 4585.653f, 64.4893f), Vector3(0, 0, 29)),
        owning_building_guid = 33,
        door_guid = 470
      )
      LocalObject(
        1325,
        IFFLock.Constructor(Vector3(1184.866f, 4568.213f, 71.9093f), Vector3(0, 0, 299)),
        owning_building_guid = 33,
        door_guid = 471
      )
      LocalObject(
        1326,
        IFFLock.Constructor(Vector3(1189.062f, 4603.103f, 41.8643f), Vector3(0, 0, 209)),
        owning_building_guid = 33,
        door_guid = 885
      )
      LocalObject(
        1327,
        IFFLock.Constructor(Vector3(1208.991f, 4586.219f, 64.4893f), Vector3(0, 0, 299)),
        owning_building_guid = 33,
        door_guid = 472
      )
      LocalObject(
        1328,
        IFFLock.Constructor(Vector3(1237.344f, 4558.195f, 49.3643f), Vector3(0, 0, 209)),
        owning_building_guid = 33,
        door_guid = 897
      )
      LocalObject(
        1329,
        IFFLock.Constructor(Vector3(1247.015f, 4582.127f, 49.3643f), Vector3(0, 0, 29)),
        owning_building_guid = 33,
        door_guid = 899
      )
      LocalObject(
        1330,
        IFFLock.Constructor(Vector3(1247.212f, 4556.427f, 41.8643f), Vector3(0, 0, 299)),
        owning_building_guid = 33,
        door_guid = 900
      )
      LocalObject(
        1331,
        IFFLock.Constructor(Vector3(1260.479f, 4613.101f, 41.8643f), Vector3(0, 0, 299)),
        owning_building_guid = 33,
        door_guid = 902
      )
      LocalObject(
        1332,
        IFFLock.Constructor(Vector3(1260.499f, 4626.358f, 59.47831f), Vector3(0, 0, 119)),
        owning_building_guid = 33,
        door_guid = 477
      )
      LocalObject(
        1333,
        IFFLock.Constructor(Vector3(1270.422f, 4611.184f, 41.8643f), Vector3(0, 0, 119)),
        owning_building_guid = 33,
        door_guid = 904
      )
      LocalObject(1674, Locker.Constructor(Vector3(1240.516f, 4559.811f, 48.28931f)), owning_building_guid = 33)
      LocalObject(1675, Locker.Constructor(Vector3(1241.534f, 4559.246f, 48.28931f)), owning_building_guid = 33)
      LocalObject(1676, Locker.Constructor(Vector3(1242.537f, 4558.69f, 48.28931f)), owning_building_guid = 33)
      LocalObject(1677, Locker.Constructor(Vector3(1243.542f, 4558.133f, 48.28931f)), owning_building_guid = 33)
      LocalObject(1678, Locker.Constructor(Vector3(1259.522f, 4572.17f, 40.52831f)), owning_building_guid = 33)
      LocalObject(1679, Locker.Constructor(Vector3(1260.68f, 4571.528f, 40.52831f)), owning_building_guid = 33)
      LocalObject(1680, Locker.Constructor(Vector3(1261.848f, 4570.88f, 40.52831f)), owning_building_guid = 33)
      LocalObject(1681, Locker.Constructor(Vector3(1263.018f, 4570.232f, 40.52831f)), owning_building_guid = 33)
      LocalObject(1682, Locker.Constructor(Vector3(1266.989f, 4568.031f, 40.52831f)), owning_building_guid = 33)
      LocalObject(1683, Locker.Constructor(Vector3(1268.146f, 4567.389f, 40.52831f)), owning_building_guid = 33)
      LocalObject(1684, Locker.Constructor(Vector3(1269.315f, 4566.742f, 40.52831f)), owning_building_guid = 33)
      LocalObject(1685, Locker.Constructor(Vector3(1270.484f, 4566.093f, 40.52831f)), owning_building_guid = 33)
      LocalObject(
        2385,
        Terminal.Constructor(Vector3(1174.39f, 4558.467f, 64.38831f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2386,
        Terminal.Constructor(Vector3(1190.452f, 4567.445f, 71.7833f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2387,
        Terminal.Constructor(Vector3(1191.399f, 4564.5f, 71.7833f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2388,
        Terminal.Constructor(Vector3(1193.531f, 4568.343f, 71.7833f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2389,
        Terminal.Constructor(Vector3(1245.773f, 4562.918f, 49.61831f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2390,
        Terminal.Constructor(Vector3(1247.582f, 4566.182f, 49.61831f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2391,
        Terminal.Constructor(Vector3(1249.419f, 4569.496f, 49.61831f), order_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3403,
        Terminal.Constructor(Vector3(1181.046f, 4553.705f, 64.6453f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3404,
        Terminal.Constructor(Vector3(1184.121f, 4589.298f, 42.0853f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3405,
        Terminal.Constructor(Vector3(1212.847f, 4609.221f, 52.0853f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3406,
        Terminal.Constructor(Vector3(1231.786f, 4565.91f, 50.1623f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3407,
        Terminal.Constructor(Vector3(1235.318f, 4572.289f, 50.1623f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3408,
        Terminal.Constructor(Vector3(1238.854f, 4578.662f, 50.1623f), spawn_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3719,
        Terminal.Constructor(Vector3(1266.436f, 4596.474f, 60.71531f), vehicle_terminal_combined),
        owning_building_guid = 33
      )
      LocalObject(
        2307,
        VehicleSpawnPad.Constructor(Vector3(1254.444f, 4602.945f, 56.5573f), mb_pad_creation, Vector3(0, 0, -61)),
        owning_building_guid = 33,
        terminal_guid = 3719
      )
      LocalObject(3219, ResourceSilo.Constructor(Vector3(1111.726f, 4540.608f, 65.0453f)), owning_building_guid = 33)
      LocalObject(
        3252,
        SpawnTube.Constructor(Vector3(1231.839f, 4567.527f, 48.02831f), Vector3(0, 0, 29)),
        owning_building_guid = 33
      )
      LocalObject(
        3253,
        SpawnTube.Constructor(Vector3(1235.374f, 4573.903f, 48.02831f), Vector3(0, 0, 29)),
        owning_building_guid = 33
      )
      LocalObject(
        3254,
        SpawnTube.Constructor(Vector3(1238.907f, 4580.278f, 48.02831f), Vector3(0, 0, 29)),
        owning_building_guid = 33
      )
      LocalObject(
        2332,
        ProximityTerminal.Constructor(Vector3(1180.707f, 4548.237f, 58.02831f), medical_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2333,
        ProximityTerminal.Constructor(Vector3(1264.724f, 4568.663f, 40.52831f), medical_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2669,
        ProximityTerminal.Constructor(Vector3(1204.271f, 4631.403f, 66.2693f), pad_landing_frame),
        owning_building_guid = 33
      )
      LocalObject(
        2670,
        Terminal.Constructor(Vector3(1204.271f, 4631.403f, 66.2693f), air_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3104,
        ProximityTerminal.Constructor(Vector3(1151.809f, 4623.89f, 57.5783f), repair_silo),
        owning_building_guid = 33
      )
      LocalObject(
        3105,
        Terminal.Constructor(Vector3(1151.809f, 4623.89f, 57.5783f), ground_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        3108,
        ProximityTerminal.Constructor(Vector3(1236.117f, 4522.836f, 57.5783f), repair_silo),
        owning_building_guid = 33
      )
      LocalObject(
        3109,
        Terminal.Constructor(Vector3(1236.117f, 4522.836f, 57.5783f), ground_rearm_terminal),
        owning_building_guid = 33
      )
      LocalObject(
        2181,
        FacilityTurret.Constructor(Vector3(1091.642f, 4538.772f, 66.5363f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(2181, 5030)
      LocalObject(
        2183,
        FacilityTurret.Constructor(Vector3(1148.388f, 4643.476f, 66.5363f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(2183, 5031)
      LocalObject(
        2184,
        FacilityTurret.Constructor(Vector3(1181.429f, 4487.657f, 66.5363f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(2184, 5032)
      LocalObject(
        2185,
        FacilityTurret.Constructor(Vector3(1207f, 4660.263f, 66.5363f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(2185, 5033)
      LocalObject(
        2186,
        FacilityTurret.Constructor(Vector3(1240.066f, 4504.447f, 66.5363f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(2186, 5034)
      LocalObject(
        2187,
        FacilityTurret.Constructor(Vector3(1296.704f, 4609.036f, 66.5363f), manned_turret),
        owning_building_guid = 33
      )
      TurretToWeapon(2187, 5035)
      LocalObject(
        2882,
        Painbox.Constructor(Vector3(1194.125f, 4615.194f, 45.43031f), painbox),
        owning_building_guid = 33
      )
      LocalObject(
        2899,
        Painbox.Constructor(Vector3(1247.108f, 4574.483f, 52.47311f), painbox_continuous),
        owning_building_guid = 33
      )
      LocalObject(
        2916,
        Painbox.Constructor(Vector3(1186.931f, 4602.012f, 43.287f), painbox_door_radius),
        owning_building_guid = 33
      )
      LocalObject(
        2939,
        Painbox.Constructor(Vector3(1235.354f, 4558.024f, 49.85431f), painbox_door_radius_continuous),
        owning_building_guid = 33
      )
      LocalObject(
        2940,
        Painbox.Constructor(Vector3(1249.394f, 4581.505f, 49.63601f), painbox_door_radius_continuous),
        owning_building_guid = 33
      )
      LocalObject(
        2941,
        Painbox.Constructor(Vector3(1258.092f, 4570.289f, 50.85431f), painbox_door_radius_continuous),
        owning_building_guid = 33
      )
      LocalObject(384, Generator.Constructor(Vector3(1195.662f, 4618.304f, 39.23431f)), owning_building_guid = 33)
      LocalObject(
        367,
        Terminal.Constructor(Vector3(1191.732f, 4611.116f, 40.52831f), gen_control),
        owning_building_guid = 33
      )
    }

    Building11()

    def Building11(): Unit = { // Name: Tore Type: comm_station GUID: 36, MapID: 11
      LocalBuilding(
        "Tore",
        36,
        11,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(2958f, 2328f, 58.28852f),
            Vector3(0f, 0f, 0f),
            comm_station
          )
        )
      )
      LocalObject(
        283,
        CaptureTerminal.Constructor(Vector3(3002.089f, 2404.734f, 40.98852f), capture_terminal),
        owning_building_guid = 36
      )
      LocalObject(542, Door.Constructor(Vector3(2898.196f, 2284.5f, 60.03952f)), owning_building_guid = 36)
      LocalObject(543, Door.Constructor(Vector3(2898.196f, 2302.693f, 68.00352f)), owning_building_guid = 36)
      LocalObject(544, Door.Constructor(Vector3(2915.307f, 2260.197f, 68.00352f)), owning_building_guid = 36)
      LocalObject(545, Door.Constructor(Vector3(2933.5f, 2260.197f, 60.03952f)), owning_building_guid = 36)
      LocalObject(546, Door.Constructor(Vector3(2938f, 2319.231f, 65.00952f)), owning_building_guid = 36)
      LocalObject(547, Door.Constructor(Vector3(2938f, 2328.295f, 65.00952f)), owning_building_guid = 36)
      LocalObject(548, Door.Constructor(Vector3(2953.625f, 2320.59f, 72.44852f)), owning_building_guid = 36)
      LocalObject(550, Door.Constructor(Vector3(2958.5f, 2395.977f, 60.03952f)), owning_building_guid = 36)
      LocalObject(551, Door.Constructor(Vector3(2966f, 2348f, 65.00952f)), owning_building_guid = 36)
      LocalObject(552, Door.Constructor(Vector3(2976.692f, 2395.977f, 68.00252f)), owning_building_guid = 36)
      LocalObject(553, Door.Constructor(Vector3(2990f, 2404f, 60.00952f)), owning_building_guid = 36)
      LocalObject(554, Door.Constructor(Vector3(3018.201f, 2317.307f, 68.00352f)), owning_building_guid = 36)
      LocalObject(555, Door.Constructor(Vector3(3018.201f, 2335.5f, 60.03952f)), owning_building_guid = 36)
      LocalObject(946, Door.Constructor(Vector3(2922f, 2360f, 52.50952f)), owning_building_guid = 36)
      LocalObject(947, Door.Constructor(Vector3(2930f, 2376f, 52.50952f)), owning_building_guid = 36)
      LocalObject(948, Door.Constructor(Vector3(2938f, 2344f, 52.50952f)), owning_building_guid = 36)
      LocalObject(949, Door.Constructor(Vector3(2938f, 2352f, 42.50952f)), owning_building_guid = 36)
      LocalObject(950, Door.Constructor(Vector3(2942f, 2324f, 65.00952f)), owning_building_guid = 36)
      LocalObject(951, Door.Constructor(Vector3(2942f, 2340f, 60.00952f)), owning_building_guid = 36)
      LocalObject(952, Door.Constructor(Vector3(2954f, 2336f, 42.50952f)), owning_building_guid = 36)
      LocalObject(953, Door.Constructor(Vector3(2954f, 2368f, 42.50952f)), owning_building_guid = 36)
      LocalObject(954, Door.Constructor(Vector3(2954f, 2376f, 52.50952f)), owning_building_guid = 36)
      LocalObject(955, Door.Constructor(Vector3(2958f, 2332f, 52.50952f)), owning_building_guid = 36)
      LocalObject(956, Door.Constructor(Vector3(2958f, 2364f, 52.50952f)), owning_building_guid = 36)
      LocalObject(957, Door.Constructor(Vector3(2966f, 2340f, 65.00952f)), owning_building_guid = 36)
      LocalObject(958, Door.Constructor(Vector3(2966f, 2348f, 55.00952f)), owning_building_guid = 36)
      LocalObject(959, Door.Constructor(Vector3(2970f, 2336f, 60.00952f)), owning_building_guid = 36)
      LocalObject(960, Door.Constructor(Vector3(2978f, 2336f, 55.00952f)), owning_building_guid = 36)
      LocalObject(961, Door.Constructor(Vector3(2986f, 2352f, 42.50952f)), owning_building_guid = 36)
      LocalObject(962, Door.Constructor(Vector3(2986f, 2384f, 42.50952f)), owning_building_guid = 36)
      LocalObject(963, Door.Constructor(Vector3(2998f, 2396f, 42.50952f)), owning_building_guid = 36)
      LocalObject(964, Door.Constructor(Vector3(3002f, 2336f, 50.00952f)), owning_building_guid = 36)
      LocalObject(965, Door.Constructor(Vector3(3002f, 2360f, 50.00952f)), owning_building_guid = 36)
      LocalObject(966, Door.Constructor(Vector3(3006f, 2396f, 42.50952f)), owning_building_guid = 36)
      LocalObject(967, Door.Constructor(Vector3(3014f, 2340f, 42.50952f)), owning_building_guid = 36)
      LocalObject(968, Door.Constructor(Vector3(3014f, 2356f, 50.00952f)), owning_building_guid = 36)
      LocalObject(969, Door.Constructor(Vector3(3014f, 2380f, 42.50952f)), owning_building_guid = 36)
      LocalObject(1193, Door.Constructor(Vector3(2975.707f, 2323.932f, 60.78152f)), owning_building_guid = 36)
      LocalObject(3539, Door.Constructor(Vector3(2994.673f, 2341.733f, 50.34252f)), owning_building_guid = 36)
      LocalObject(3540, Door.Constructor(Vector3(2994.673f, 2349.026f, 50.34252f)), owning_building_guid = 36)
      LocalObject(3541, Door.Constructor(Vector3(2994.673f, 2356.315f, 50.34252f)), owning_building_guid = 36)
      LocalObject(
        1257,
        IFFLock.Constructor(Vector3(2978.453f, 2327.13f, 59.94052f), Vector3(0, 0, 90)),
        owning_building_guid = 36,
        door_guid = 1193
      )
      LocalObject(
        1383,
        IFFLock.Constructor(Vector3(2935.957f, 2329.105f, 64.94952f), Vector3(0, 0, 0)),
        owning_building_guid = 36,
        door_guid = 547
      )
      LocalObject(
        1384,
        IFFLock.Constructor(Vector3(2939.572f, 2351.06f, 42.32452f), Vector3(0, 0, 180)),
        owning_building_guid = 36,
        door_guid = 949
      )
      LocalObject(
        1385,
        IFFLock.Constructor(Vector3(2940.047f, 2318.42f, 64.94952f), Vector3(0, 0, 180)),
        owning_building_guid = 36,
        door_guid = 546
      )
      LocalObject(
        1386,
        IFFLock.Constructor(Vector3(2952.817f, 2318.511f, 72.36952f), Vector3(0, 0, 270)),
        owning_building_guid = 36,
        door_guid = 548
      )
      LocalObject(
        1387,
        IFFLock.Constructor(Vector3(2965.187f, 2345.955f, 64.94952f), Vector3(0, 0, 270)),
        owning_building_guid = 36,
        door_guid = 551
      )
      LocalObject(
        1388,
        IFFLock.Constructor(Vector3(2990.778f, 2406.033f, 59.93852f), Vector3(0, 0, 90)),
        owning_building_guid = 36,
        door_guid = 553
      )
      LocalObject(
        1389,
        IFFLock.Constructor(Vector3(2997.187f, 2394.428f, 42.32452f), Vector3(0, 0, 270)),
        owning_building_guid = 36,
        door_guid = 963
      )
      LocalObject(
        1390,
        IFFLock.Constructor(Vector3(3000.428f, 2360.81f, 49.82452f), Vector3(0, 0, 0)),
        owning_building_guid = 36,
        door_guid = 965
      )
      LocalObject(
        1391,
        IFFLock.Constructor(Vector3(3003.572f, 2335.19f, 49.82452f), Vector3(0, 0, 180)),
        owning_building_guid = 36,
        door_guid = 964
      )
      LocalObject(
        1392,
        IFFLock.Constructor(Vector3(3006.813f, 2397.572f, 42.32452f), Vector3(0, 0, 90)),
        owning_building_guid = 36,
        door_guid = 966
      )
      LocalObject(
        1393,
        IFFLock.Constructor(Vector3(3013.06f, 2338.428f, 42.32452f), Vector3(0, 0, 270)),
        owning_building_guid = 36,
        door_guid = 967
      )
      LocalObject(1766, Locker.Constructor(Vector3(3005.563f, 2338.141f, 48.74952f)), owning_building_guid = 36)
      LocalObject(1767, Locker.Constructor(Vector3(3006.727f, 2338.141f, 48.74952f)), owning_building_guid = 36)
      LocalObject(1768, Locker.Constructor(Vector3(3007.874f, 2338.141f, 48.74952f)), owning_building_guid = 36)
      LocalObject(1769, Locker.Constructor(Vector3(3009.023f, 2338.141f, 48.74952f)), owning_building_guid = 36)
      LocalObject(1770, Locker.Constructor(Vector3(3016.194f, 2358.165f, 40.98852f)), owning_building_guid = 36)
      LocalObject(1771, Locker.Constructor(Vector3(3017.518f, 2358.165f, 40.98852f)), owning_building_guid = 36)
      LocalObject(1772, Locker.Constructor(Vector3(3018.854f, 2358.165f, 40.98852f)), owning_building_guid = 36)
      LocalObject(1773, Locker.Constructor(Vector3(3020.191f, 2358.165f, 40.98852f)), owning_building_guid = 36)
      LocalObject(1776, Locker.Constructor(Vector3(3024.731f, 2358.165f, 40.98852f)), owning_building_guid = 36)
      LocalObject(1779, Locker.Constructor(Vector3(3026.055f, 2358.165f, 40.98852f)), owning_building_guid = 36)
      LocalObject(1780, Locker.Constructor(Vector3(3027.391f, 2358.165f, 40.98852f)), owning_building_guid = 36)
      LocalObject(1783, Locker.Constructor(Vector3(3028.728f, 2358.165f, 40.98852f)), owning_building_guid = 36)
      LocalObject(
        2423,
        Terminal.Constructor(Vector3(2948.379f, 2304.907f, 64.84852f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2424,
        Terminal.Constructor(Vector3(2958.075f, 2320.547f, 72.24352f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2425,
        Terminal.Constructor(Vector3(2960.331f, 2318.43f, 72.24352f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2426,
        Terminal.Constructor(Vector3(2960.332f, 2322.825f, 72.24352f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2427,
        Terminal.Constructor(Vector3(3008.654f, 2343.408f, 50.07852f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2428,
        Terminal.Constructor(Vector3(3008.654f, 2347.139f, 50.07852f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2429,
        Terminal.Constructor(Vector3(3008.654f, 2350.928f, 50.07852f), order_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3422,
        Terminal.Constructor(Vector3(2941.943f, 2336.591f, 42.54552f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3423,
        Terminal.Constructor(Vector3(2956.51f, 2303.969f, 65.10552f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3424,
        Terminal.Constructor(Vector3(2957.409f, 2367.942f, 52.54552f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3425,
        Terminal.Constructor(Vector3(2994.971f, 2339.243f, 50.62252f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3426,
        Terminal.Constructor(Vector3(2994.967f, 2346.535f, 50.62252f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3427,
        Terminal.Constructor(Vector3(2994.97f, 2353.823f, 50.62252f), spawn_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3722,
        Terminal.Constructor(Vector3(3010.458f, 2382.774f, 61.17552f), vehicle_terminal_combined),
        owning_building_guid = 36
      )
      LocalObject(
        2312,
        VehicleSpawnPad.Constructor(Vector3(2996.833f, 2382.62f, 57.01752f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 36,
        terminal_guid = 3722
      )
      LocalObject(3222, ResourceSilo.Constructor(Vector3(2902.23f, 2258.908f, 65.50552f)), owning_building_guid = 36)
      LocalObject(
        3275,
        SpawnTube.Constructor(Vector3(2994.233f, 2340.683f, 48.48852f), Vector3(0, 0, 0)),
        owning_building_guid = 36
      )
      LocalObject(
        3276,
        SpawnTube.Constructor(Vector3(2994.233f, 2347.974f, 48.48852f), Vector3(0, 0, 0)),
        owning_building_guid = 36
      )
      LocalObject(
        3277,
        SpawnTube.Constructor(Vector3(2994.233f, 2355.262f, 48.48852f), Vector3(0, 0, 0)),
        owning_building_guid = 36
      )
      LocalObject(
        2338,
        ProximityTerminal.Constructor(Vector3(2958.864f, 2299.023f, 58.48852f), medical_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2339,
        ProximityTerminal.Constructor(Vector3(3022.444f, 2357.62f, 40.98852f), medical_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2702,
        ProximityTerminal.Constructor(Vector3(2939.154f, 2383.185f, 66.72952f), pad_landing_frame),
        owning_building_guid = 36
      )
      LocalObject(
        2703,
        Terminal.Constructor(Vector3(2939.154f, 2383.185f, 66.72952f), air_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3128,
        ProximityTerminal.Constructor(Vector3(2896.912f, 2351.18f, 58.03852f), repair_silo),
        owning_building_guid = 36
      )
      LocalObject(
        3129,
        Terminal.Constructor(Vector3(2896.912f, 2351.18f, 58.03852f), ground_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        3132,
        ProximityTerminal.Constructor(Vector3(3019.641f, 2303.67f, 58.03852f), repair_silo),
        owning_building_guid = 36
      )
      LocalObject(
        3133,
        Terminal.Constructor(Vector3(3019.641f, 2303.67f, 58.03852f), ground_rearm_terminal),
        owning_building_guid = 36
      )
      LocalObject(
        2212,
        FacilityTurret.Constructor(Vector3(2884.424f, 2366.652f, 66.99652f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(2212, 5036)
      LocalObject(
        2213,
        FacilityTurret.Constructor(Vector3(2885.554f, 2247.565f, 66.99652f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(2213, 5037)
      LocalObject(
        2214,
        FacilityTurret.Constructor(Vector3(2927.549f, 2409.75f, 66.99652f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(2214, 5038)
      LocalObject(
        2215,
        FacilityTurret.Constructor(Vector3(2988.865f, 2246.388f, 66.99652f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(2215, 5039)
      LocalObject(
        2218,
        FacilityTurret.Constructor(Vector3(3030.841f, 2408.435f, 66.99652f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(2218, 5040)
      LocalObject(
        2219,
        FacilityTurret.Constructor(Vector3(3032.01f, 2289.501f, 66.99652f), manned_turret),
        owning_building_guid = 36
      )
      TurretToWeapon(2219, 5041)
      LocalObject(
        2885,
        Painbox.Constructor(Vector3(2938.138f, 2364.089f, 45.89052f), painbox),
        owning_building_guid = 36
      )
      LocalObject(
        2902,
        Painbox.Constructor(Vector3(3004.215f, 2354.17f, 52.93332f), painbox_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        2919,
        Painbox.Constructor(Vector3(2938.237f, 2349.073f, 43.74722f), painbox_door_radius),
        owning_building_guid = 36
      )
      LocalObject(
        2948,
        Painbox.Constructor(Vector3(3001.914f, 2334.076f, 50.31452f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        2949,
        Painbox.Constructor(Vector3(3002.81f, 2361.42f, 50.09622f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(
        2950,
        Painbox.Constructor(Vector3(3015.855f, 2355.827f, 51.31452f), painbox_door_radius_continuous),
        owning_building_guid = 36
      )
      LocalObject(387, Generator.Constructor(Vector3(2937.975f, 2367.555f, 39.69452f)), owning_building_guid = 36)
      LocalObject(
        370,
        Terminal.Constructor(Vector3(2938.022f, 2359.363f, 40.98852f), gen_control),
        owning_building_guid = 36
      )
    }

    Building18()

    def Building18(): Unit = { // Name: Gunuku Type: comm_station_dsp GUID: 39, MapID: 18
      LocalBuilding(
        "Gunuku",
        39,
        18,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4936f, 4344f, 53.91644f),
            Vector3(0f, 0f, 360f),
            comm_station_dsp
          )
        )
      )
      LocalObject(
        286,
        CaptureTerminal.Constructor(Vector3(5012.089f, 4324.734f, 36.51644f), capture_terminal),
        owning_building_guid = 39
      )
      LocalObject(357, Door.Constructor(Vector3(5004.339f, 4414.464f, 57.29444f)), owning_building_guid = 39)
      LocalObject(620, Door.Constructor(Vector3(4876.196f, 4300.501f, 55.56744f)), owning_building_guid = 39)
      LocalObject(621, Door.Constructor(Vector3(4876.196f, 4318.693f, 63.53144f)), owning_building_guid = 39)
      LocalObject(622, Door.Constructor(Vector3(4893.307f, 4276.197f, 63.53144f)), owning_building_guid = 39)
      LocalObject(624, Door.Constructor(Vector3(4911.499f, 4276.197f, 55.56744f)), owning_building_guid = 39)
      LocalObject(625, Door.Constructor(Vector3(4916f, 4335.231f, 60.53744f)), owning_building_guid = 39)
      LocalObject(626, Door.Constructor(Vector3(4916f, 4344.295f, 60.53744f)), owning_building_guid = 39)
      LocalObject(627, Door.Constructor(Vector3(4928.763f, 4463.958f, 55.56744f)), owning_building_guid = 39)
      LocalObject(628, Door.Constructor(Vector3(4931.625f, 4336.59f, 67.97644f)), owning_building_guid = 39)
      LocalObject(629, Door.Constructor(Vector3(4941.627f, 4476.823f, 63.53044f)), owning_building_guid = 39)
      LocalObject(630, Door.Constructor(Vector3(4944f, 4364f, 60.53744f)), owning_building_guid = 39)
      LocalObject(635, Door.Constructor(Vector3(4983.721f, 4508.353f, 55.56744f)), owning_building_guid = 39)
      LocalObject(636, Door.Constructor(Vector3(4989.952f, 4448.355f, 60.53344f)), owning_building_guid = 39)
      LocalObject(637, Door.Constructor(Vector3(4991.927f, 4418.35f, 55.53944f)), owning_building_guid = 39)
      LocalObject(638, Door.Constructor(Vector3(5001.914f, 4508.353f, 63.53044f)), owning_building_guid = 39)
      LocalObject(640, Door.Constructor(Vector3(5015.929f, 4331.406f, 63.53144f)), owning_building_guid = 39)
      LocalObject(644, Door.Constructor(Vector3(5028.793f, 4344.27f, 55.56744f)), owning_building_guid = 39)
      LocalObject(645, Door.Constructor(Vector3(5042.977f, 4407.008f, 63.53044f)), owning_building_guid = 39)
      LocalObject(646, Door.Constructor(Vector3(5042.977f, 4425.2f, 55.56744f)), owning_building_guid = 39)
      LocalObject(648, Door.Constructor(Vector3(5052f, 4424f, 55.53744f)), owning_building_guid = 39)
      LocalObject(1016, Door.Constructor(Vector3(4920f, 4340f, 60.53744f)), owning_building_guid = 39)
      LocalObject(1017, Door.Constructor(Vector3(4920f, 4356f, 55.53744f)), owning_building_guid = 39)
      LocalObject(1018, Door.Constructor(Vector3(4944f, 4356f, 60.53744f)), owning_building_guid = 39)
      LocalObject(1019, Door.Constructor(Vector3(4944f, 4364f, 50.53744f)), owning_building_guid = 39)
      LocalObject(1020, Door.Constructor(Vector3(4948f, 4352f, 55.53744f)), owning_building_guid = 39)
      LocalObject(1021, Door.Constructor(Vector3(4956f, 4352f, 50.53744f)), owning_building_guid = 39)
      LocalObject(1022, Door.Constructor(Vector3(4960f, 4388f, 45.53744f)), owning_building_guid = 39)
      LocalObject(1023, Door.Constructor(Vector3(4964f, 4376f, 38.03744f)), owning_building_guid = 39)
      LocalObject(1024, Door.Constructor(Vector3(4976f, 4340f, 38.03744f)), owning_building_guid = 39)
      LocalObject(1025, Door.Constructor(Vector3(4976f, 4404f, 38.03744f)), owning_building_guid = 39)
      LocalObject(1026, Door.Constructor(Vector3(4980f, 4336f, 45.53744f)), owning_building_guid = 39)
      LocalObject(1027, Door.Constructor(Vector3(4980f, 4352f, 45.53744f)), owning_building_guid = 39)
      LocalObject(1028, Door.Constructor(Vector3(4980f, 4376f, 45.53744f)), owning_building_guid = 39)
      LocalObject(1029, Door.Constructor(Vector3(4992f, 4356f, 38.03744f)), owning_building_guid = 39)
      LocalObject(1030, Door.Constructor(Vector3(4992f, 4372f, 45.53744f)), owning_building_guid = 39)
      LocalObject(1031, Door.Constructor(Vector3(4995.921f, 4438.351f, 60.53944f)), owning_building_guid = 39)
      LocalObject(1032, Door.Constructor(Vector3(5008f, 4316f, 38.03744f)), owning_building_guid = 39)
      LocalObject(1033, Door.Constructor(Vector3(5016f, 4316f, 38.03744f)), owning_building_guid = 39)
      LocalObject(1035, Door.Constructor(Vector3(5020f, 4328f, 38.03744f)), owning_building_guid = 39)
      LocalObject(1036, Door.Constructor(Vector3(5024f, 4348f, 45.53744f)), owning_building_guid = 39)
      LocalObject(1037, Door.Constructor(Vector3(5024f, 4380f, 45.53744f)), owning_building_guid = 39)
      LocalObject(1196, Door.Constructor(Vector3(4953.707f, 4339.922f, 56.30844f)), owning_building_guid = 39)
      LocalObject(3566, Door.Constructor(Vector3(4972.673f, 4357.733f, 45.87044f)), owning_building_guid = 39)
      LocalObject(3567, Door.Constructor(Vector3(4972.673f, 4365.026f, 45.87044f)), owning_building_guid = 39)
      LocalObject(3568, Door.Constructor(Vector3(4972.673f, 4372.315f, 45.87044f)), owning_building_guid = 39)
      LocalObject(
        1260,
        IFFLock.Constructor(Vector3(4956.454f, 4343.09f, 55.48444f), Vector3(0, 0, 90)),
        owning_building_guid = 39,
        door_guid = 1196
      )
      LocalObject(
        1448,
        IFFLock.Constructor(Vector3(4913.959f, 4345.104f, 60.48444f), Vector3(0, 0, 0)),
        owning_building_guid = 39,
        door_guid = 626
      )
      LocalObject(
        1449,
        IFFLock.Constructor(Vector3(4918.04f, 4334.42f, 60.48444f), Vector3(0, 0, 180)),
        owning_building_guid = 39,
        door_guid = 625
      )
      LocalObject(
        1450,
        IFFLock.Constructor(Vector3(4930.817f, 4334.514f, 67.98444f), Vector3(0, 0, 270)),
        owning_building_guid = 39,
        door_guid = 628
      )
      LocalObject(
        1451,
        IFFLock.Constructor(Vector3(4943.193f, 4361.962f, 60.48444f), Vector3(0, 0, 270)),
        owning_building_guid = 39,
        door_guid = 630
      )
      LocalObject(
        1456,
        IFFLock.Constructor(Vector3(4976.94f, 4405.572f, 37.85244f), Vector3(0, 0, 90)),
        owning_building_guid = 39,
        door_guid = 1025
      )
      LocalObject(
        1457,
        IFFLock.Constructor(Vector3(4978.428f, 4376.94f, 45.35244f), Vector3(0, 0, 0)),
        owning_building_guid = 39,
        door_guid = 1028
      )
      LocalObject(
        1458,
        IFFLock.Constructor(Vector3(4981.572f, 4351.19f, 45.35244f), Vector3(0, 0, 180)),
        owning_building_guid = 39,
        door_guid = 1027
      )
      LocalObject(
        1459,
        IFFLock.Constructor(Vector3(4987.907f, 4449.163f, 60.46344f), Vector3(0, 0, 0)),
        owning_building_guid = 39,
        door_guid = 636
      )
      LocalObject(
        1460,
        IFFLock.Constructor(Vector3(4991.06f, 4354.428f, 37.85244f), Vector3(0, 0, 270)),
        owning_building_guid = 39,
        door_guid = 1029
      )
      LocalObject(
        1461,
        IFFLock.Constructor(Vector3(4991.124f, 4416.312f, 55.52844f), Vector3(0, 0, 270)),
        owning_building_guid = 39,
        door_guid = 637
      )
      LocalObject(
        1462,
        IFFLock.Constructor(Vector3(5007.06f, 4314.428f, 37.85244f), Vector3(0, 0, 270)),
        owning_building_guid = 39,
        door_guid = 1032
      )
      LocalObject(
        1465,
        IFFLock.Constructor(Vector3(5016.813f, 4317.572f, 37.85244f), Vector3(0, 0, 90)),
        owning_building_guid = 39,
        door_guid = 1033
      )
      LocalObject(
        1466,
        IFFLock.Constructor(Vector3(5049.953f, 4424.808f, 55.42744f), Vector3(0, 0, 0)),
        owning_building_guid = 39,
        door_guid = 648
      )
      LocalObject(1892, Locker.Constructor(Vector3(4983.563f, 4354.141f, 44.27744f)), owning_building_guid = 39)
      LocalObject(1893, Locker.Constructor(Vector3(4984.727f, 4354.141f, 44.27744f)), owning_building_guid = 39)
      LocalObject(1894, Locker.Constructor(Vector3(4985.874f, 4354.141f, 44.27744f)), owning_building_guid = 39)
      LocalObject(1895, Locker.Constructor(Vector3(4987.023f, 4354.141f, 44.27744f)), owning_building_guid = 39)
      LocalObject(1896, Locker.Constructor(Vector3(4994.194f, 4374.165f, 36.51644f)), owning_building_guid = 39)
      LocalObject(1897, Locker.Constructor(Vector3(4995.518f, 4374.165f, 36.51644f)), owning_building_guid = 39)
      LocalObject(1898, Locker.Constructor(Vector3(4996.854f, 4374.165f, 36.51644f)), owning_building_guid = 39)
      LocalObject(1899, Locker.Constructor(Vector3(4998.191f, 4374.165f, 36.51644f)), owning_building_guid = 39)
      LocalObject(1900, Locker.Constructor(Vector3(5002.731f, 4374.165f, 36.51644f)), owning_building_guid = 39)
      LocalObject(1901, Locker.Constructor(Vector3(5004.055f, 4374.165f, 36.51644f)), owning_building_guid = 39)
      LocalObject(1902, Locker.Constructor(Vector3(5005.391f, 4374.165f, 36.51644f)), owning_building_guid = 39)
      LocalObject(1903, Locker.Constructor(Vector3(5006.728f, 4374.165f, 36.51644f)), owning_building_guid = 39)
      LocalObject(
        359,
        Terminal.Constructor(Vector3(4995.879f, 4446.918f, 59.62044f), dropship_vehicle_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        358,
        VehicleSpawnPad.Constructor(Vector3(5004.328f, 4468.856f, 53.94444f), dropship_pad_doors, Vector3(0, 0, 90)),
        owning_building_guid = 39,
        terminal_guid = 359
      )
      LocalObject(
        2462,
        Terminal.Constructor(Vector3(4926.378f, 4320.897f, 60.37644f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2463,
        Terminal.Constructor(Vector3(4936.075f, 4336.547f, 67.77144f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2464,
        Terminal.Constructor(Vector3(4938.331f, 4334.43f, 67.77144f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2465,
        Terminal.Constructor(Vector3(4938.332f, 4338.825f, 67.77144f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2466,
        Terminal.Constructor(Vector3(4940.592f, 4336.59f, 67.77144f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2470,
        Terminal.Constructor(Vector3(4986.654f, 4359.408f, 45.60644f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2471,
        Terminal.Constructor(Vector3(4986.654f, 4363.139f, 45.60644f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2472,
        Terminal.Constructor(Vector3(4986.654f, 4366.928f, 45.60644f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3442,
        Terminal.Constructor(Vector3(4934.509f, 4319.959f, 60.63344f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3443,
        Terminal.Constructor(Vector3(4972.971f, 4355.243f, 46.15044f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3444,
        Terminal.Constructor(Vector3(4972.967f, 4362.535f, 46.15044f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3445,
        Terminal.Constructor(Vector3(4972.97f, 4369.823f, 46.15044f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3446,
        Terminal.Constructor(Vector3(4991.103f, 4438.906f, 60.56444f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3447,
        Terminal.Constructor(Vector3(5000.058f, 4343.409f, 38.04444f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3448,
        Terminal.Constructor(Vector3(5007.409f, 4399.942f, 38.04444f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3450,
        Terminal.Constructor(Vector3(5016.058f, 4351.409f, 45.57344f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3451,
        Terminal.Constructor(Vector3(5016.058f, 4391.409f, 45.57344f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3725,
        Terminal.Constructor(Vector3(4953.698f, 4452.044f, 56.70344f), ground_vehicle_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2315,
        VehicleSpawnPad.Constructor(Vector3(4953.786f, 4438.411f, 52.54544f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 39,
        terminal_guid = 3725
      )
      LocalObject(3225, ResourceSilo.Constructor(Vector3(5034.212f, 4509.642f, 61.03344f)), owning_building_guid = 39)
      LocalObject(
        3302,
        SpawnTube.Constructor(Vector3(4972.233f, 4356.683f, 44.01644f), Vector3(0, 0, 0)),
        owning_building_guid = 39
      )
      LocalObject(
        3303,
        SpawnTube.Constructor(Vector3(4972.233f, 4363.974f, 44.01644f), Vector3(0, 0, 0)),
        owning_building_guid = 39
      )
      LocalObject(
        3304,
        SpawnTube.Constructor(Vector3(4972.233f, 4371.262f, 44.01644f), Vector3(0, 0, 0)),
        owning_building_guid = 39
      )
      LocalObject(
        2342,
        ProximityTerminal.Constructor(Vector3(4936.863f, 4315.013f, 54.01644f), medical_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2343,
        ProximityTerminal.Constructor(Vector3(5000.444f, 4373.62f, 36.51644f), medical_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2729,
        ProximityTerminal.Constructor(Vector3(4917.153f, 4437.398f, 62.32644f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        2730,
        Terminal.Constructor(Vector3(4917.153f, 4437.398f, 62.32644f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2732,
        ProximityTerminal.Constructor(Vector3(4933.514f, 4391.467f, 59.61044f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        2733,
        Terminal.Constructor(Vector3(4933.514f, 4391.467f, 59.61044f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2735,
        ProximityTerminal.Constructor(Vector3(4985.804f, 4355.901f, 66.79244f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        2736,
        Terminal.Constructor(Vector3(4985.804f, 4355.901f, 66.79244f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2738,
        ProximityTerminal.Constructor(Vector3(5021.071f, 4372.159f, 62.33944f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        2739,
        Terminal.Constructor(Vector3(5021.071f, 4372.159f, 62.33944f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3152,
        ProximityTerminal.Constructor(Vector3(4874.643f, 4382.241f, 53.66644f), repair_silo),
        owning_building_guid = 39
      )
      LocalObject(
        3153,
        Terminal.Constructor(Vector3(4874.643f, 4382.241f, 53.66644f), ground_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        3160,
        ProximityTerminal.Constructor(Vector3(5044.57f, 4385.151f, 53.66644f), repair_silo),
        owning_building_guid = 39
      )
      LocalObject(
        3161,
        Terminal.Constructor(Vector3(5044.57f, 4385.151f, 53.66644f), ground_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2238,
        FacilityTurret.Constructor(Vector3(4862.401f, 4417.113f, 62.52444f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(2238, 5042)
      LocalObject(
        2239,
        FacilityTurret.Constructor(Vector3(4863.554f, 4263.565f, 62.52444f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(2239, 5043)
      LocalObject(
        2240,
        FacilityTurret.Constructor(Vector3(4907.445f, 4463.667f, 62.52444f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(2240, 5044)
      LocalObject(
        2242,
        FacilityTurret.Constructor(Vector3(4966.428f, 4262.396f, 62.52444f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(2242, 5045)
      LocalObject(
        2243,
        FacilityTurret.Constructor(Vector3(4967.449f, 4522.154f, 62.52444f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(2243, 5046)
      LocalObject(
        2246,
        FacilityTurret.Constructor(Vector3(5008.537f, 4303.011f, 62.52444f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(2246, 5047)
      LocalObject(
        2248,
        FacilityTurret.Constructor(Vector3(5055.619f, 4520.985f, 62.52444f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(2248, 5048)
      LocalObject(
        2249,
        FacilityTurret.Constructor(Vector3(5056.773f, 4352.733f, 62.52444f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(2249, 5049)
      LocalObject(
        2888,
        Painbox.Constructor(Vector3(4964.428f, 4404.057f, 40.41074f), painbox),
        owning_building_guid = 39
      )
      LocalObject(
        2905,
        Painbox.Constructor(Vector3(4981.857f, 4364.408f, 48.04394f), painbox_continuous),
        owning_building_guid = 39
      )
      LocalObject(
        2922,
        Painbox.Constructor(Vector3(4978.203f, 4402.915f, 39.64864f), painbox_door_radius),
        owning_building_guid = 39
      )
      LocalObject(
        2957,
        Painbox.Constructor(Vector3(4979.087f, 4349.386f, 46.44564f), painbox_door_radius_continuous),
        owning_building_guid = 39
      )
      LocalObject(
        2958,
        Painbox.Constructor(Vector3(4979.895f, 4378.081f, 46.91644f), painbox_door_radius_continuous),
        owning_building_guid = 39
      )
      LocalObject(
        2959,
        Painbox.Constructor(Vector3(4994.317f, 4371.888f, 47.34674f), painbox_door_radius_continuous),
        owning_building_guid = 39
      )
      LocalObject(390, Generator.Constructor(Vector3(4960.445f, 4403.975f, 35.22244f)), owning_building_guid = 39)
      LocalObject(
        373,
        Terminal.Constructor(Vector3(4968.637f, 4404.022f, 36.51644f), gen_control),
        owning_building_guid = 39
      )
    }

    Building21()

    def Building21(): Unit = { // Name: Mukuru Type: cryo_facility GUID: 42, MapID: 21
      LocalBuilding(
        "Mukuru",
        42,
        21,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(590f, 2410f, 54.06358f),
            Vector3(0f, 0f, 284f),
            cryo_facility
          )
        )
      )
      LocalObject(
        278,
        CaptureTerminal.Constructor(Vector3(640.7102f, 2451.592f, 44.06358f), capture_terminal),
        owning_building_guid = 42
      )
      LocalObject(402, Door.Constructor(Vector3(508.4743f, 2356.694f, 55.58458f)), owning_building_guid = 42)
      LocalObject(403, Door.Constructor(Vector3(510.8202f, 2384.789f, 63.57858f)), owning_building_guid = 42)
      LocalObject(405, Door.Constructor(Vector3(515.2214f, 2367.136f, 55.61458f)), owning_building_guid = 42)
      LocalObject(409, Door.Constructor(Vector3(553.1332f, 2348.322f, 63.57858f)), owning_building_guid = 42)
      LocalObject(410, Door.Constructor(Vector3(570.7858f, 2352.723f, 55.61458f)), owning_building_guid = 42)
      LocalObject(412, Door.Constructor(Vector3(580.0985f, 2468.314f, 55.61458f)), owning_building_guid = 42)
      LocalObject(413, Door.Constructor(Vector3(594.0115f, 2406.878f, 65.58458f)), owning_building_guid = 42)
      LocalObject(414, Door.Constructor(Vector3(597.7511f, 2472.715f, 63.57858f)), owning_building_guid = 42)
      LocalObject(420, Door.Constructor(Vector3(605.5352f, 2430.363f, 65.58458f)), owning_building_guid = 42)
      LocalObject(434, Door.Constructor(Vector3(645.5494f, 2467.472f, 55.61458f)), owning_building_guid = 42)
      LocalObject(437, Door.Constructor(Vector3(649.9507f, 2449.819f, 63.57858f)), owning_building_guid = 42)
      LocalObject(819, Door.Constructor(Vector3(501.7109f, 2400.354f, 48.08458f)), owning_building_guid = 42)
      LocalObject(824, Door.Constructor(Vector3(522.1053f, 2434.297f, 48.08458f)), owning_building_guid = 42)
      LocalObject(825, Door.Constructor(Vector3(530.825f, 2415.858f, 48.08458f)), owning_building_guid = 42)
      LocalObject(835, Door.Constructor(Vector3(554.1121f, 2421.664f, 45.58458f)), owning_building_guid = 42)
      LocalObject(836, Door.Constructor(Vector3(562.8317f, 2403.226f, 45.58458f)), owning_building_guid = 42)
      LocalObject(839, Door.Constructor(Vector3(566.7442f, 2453.671f, 45.58458f)), owning_building_guid = 42)
      LocalObject(840, Door.Constructor(Vector3(581.2908f, 2444.931f, 45.58458f)), owning_building_guid = 42)
      LocalObject(841, Door.Constructor(Vector3(583.2053f, 2404.183f, 65.58458f)), owning_building_guid = 42)
      LocalObject(842, Door.Constructor(Vector3(586.1188f, 2409.032f, 45.58458f)), owning_building_guid = 42)
      LocalObject(843, Door.Constructor(Vector3(587.0969f, 2421.644f, 45.58458f)), owning_building_guid = 42)
      LocalObject(844, Door.Constructor(Vector3(592.9031f, 2398.356f, 45.58458f)), owning_building_guid = 42)
      LocalObject(847, Door.Constructor(Vector3(599.6873f, 2387.681f, 38.08458f)), owning_building_guid = 42)
      LocalObject(848, Door.Constructor(Vector3(601.6644f, 2445.888f, 45.58458f)), owning_building_guid = 42)
      LocalObject(849, Door.Constructor(Vector3(605.5352f, 2430.363f, 45.58458f)), owning_building_guid = 42)
      LocalObject(850, Door.Constructor(Vector3(605.5352f, 2430.363f, 55.58458f)), owning_building_guid = 42)
      LocalObject(851, Door.Constructor(Vector3(615.212f, 2391.551f, 45.58458f)), owning_building_guid = 42)
      LocalObject(852, Door.Constructor(Vector3(616.1902f, 2404.163f, 45.58458f)), owning_building_guid = 42)
      LocalObject(853, Door.Constructor(Vector3(620.1027f, 2454.607f, 45.58458f)), owning_building_guid = 42)
      LocalObject(854, Door.Constructor(Vector3(625.9088f, 2431.32f, 45.58458f)), owning_building_guid = 42)
      LocalObject(856, Door.Constructor(Vector3(640.4554f, 2422.58f, 45.58458f)), owning_building_guid = 42)
      LocalObject(857, Door.Constructor(Vector3(648.2386f, 2457.5f, 45.58458f)), owning_building_guid = 42)
      LocalObject(858, Door.Constructor(Vector3(650.174f, 2449.738f, 45.58458f)), owning_building_guid = 42)
      LocalObject(859, Door.Constructor(Vector3(652.1094f, 2441.975f, 45.58458f)), owning_building_guid = 42)
      LocalObject(1188, Door.Constructor(Vector3(615.3037f, 2412.195f, 56.34658f)), owning_building_guid = 42)
      LocalObject(1204, Door.Constructor(Vector3(592.9031f, 2398.356f, 55.58458f)), owning_building_guid = 42)
      LocalObject(1205, Door.Constructor(Vector3(596.7947f, 2415.817f, 55.58258f)), owning_building_guid = 42)
      LocalObject(3500, Door.Constructor(Vector3(596.6932f, 2406.853f, 45.91758f)), owning_building_guid = 42)
      LocalObject(3501, Door.Constructor(Vector3(603.7696f, 2408.617f, 45.91758f)), owning_building_guid = 42)
      LocalObject(3502, Door.Constructor(Vector3(610.842f, 2410.38f, 45.91758f)), owning_building_guid = 42)
      LocalObject(
        1252,
        IFFLock.Constructor(Vector3(617.1818f, 2415.984f, 55.54558f), Vector3(0, 0, 76)),
        owning_building_guid = 42,
        door_guid = 1188
      )
      LocalObject(
        1275,
        IFFLock.Constructor(Vector3(510.6526f, 2356.398f, 55.51558f), Vector3(0, 0, 166)),
        owning_building_guid = 42,
        door_guid = 402
      )
      LocalObject(
        1281,
        IFFLock.Constructor(Vector3(592.4974f, 2396.635f, 45.39958f), Vector3(0, 0, 256)),
        owning_building_guid = 42,
        door_guid = 844
      )
      LocalObject(
        1282,
        IFFLock.Constructor(Vector3(594.3132f, 2409.062f, 65.51558f), Vector3(0, 0, 76)),
        owning_building_guid = 42,
        door_guid = 413
      )
      LocalObject(
        1283,
        IFFLock.Constructor(Vector3(597.9661f, 2388.086f, 37.89958f), Vector3(0, 0, 346)),
        owning_building_guid = 42,
        door_guid = 847
      )
      LocalObject(
        1284,
        IFFLock.Constructor(Vector3(607.7144f, 2430.068f, 65.51558f), Vector3(0, 0, 166)),
        owning_building_guid = 42,
        door_guid = 420
      )
      LocalObject(
        1292,
        IFFLock.Constructor(Vector3(616.5958f, 2405.884f, 45.39958f), Vector3(0, 0, 76)),
        owning_building_guid = 42,
        door_guid = 852
      )
      LocalObject(
        1299,
        IFFLock.Constructor(Vector3(646.4859f, 2458.032f, 45.39958f), Vector3(0, 0, 346)),
        owning_building_guid = 42,
        door_guid = 857
      )
      LocalObject(
        1300,
        IFFLock.Constructor(Vector3(651.896f, 2449.329f, 45.39958f), Vector3(0, 0, 166)),
        owning_building_guid = 42,
        door_guid = 858
      )
      LocalObject(1601, Locker.Constructor(Vector3(595.8424f, 2395.417f, 44.32458f)), owning_building_guid = 42)
      LocalObject(1602, Locker.Constructor(Vector3(596.124f, 2394.288f, 44.32458f)), owning_building_guid = 42)
      LocalObject(1603, Locker.Constructor(Vector3(596.4015f, 2393.175f, 44.32458f)), owning_building_guid = 42)
      LocalObject(1604, Locker.Constructor(Vector3(596.6795f, 2392.06f, 44.32458f)), owning_building_guid = 42)
      LocalObject(1605, Locker.Constructor(Vector3(600.2291f, 2378.729f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1606, Locker.Constructor(Vector3(600.4845f, 2377.704f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1607, Locker.Constructor(Vector3(600.7397f, 2376.68f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1608, Locker.Constructor(Vector3(600.995f, 2375.657f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1609, Locker.Constructor(Vector3(601.2514f, 2374.628f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1610, Locker.Constructor(Vector3(601.5067f, 2373.604f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1615, Locker.Constructor(Vector3(619.6352f, 2383.57f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1616, Locker.Constructor(Vector3(619.8902f, 2382.548f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1617, Locker.Constructor(Vector3(620.1469f, 2381.518f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1618, Locker.Constructor(Vector3(620.4021f, 2380.494f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1619, Locker.Constructor(Vector3(620.6573f, 2379.471f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1620, Locker.Constructor(Vector3(620.9128f, 2378.446f, 43.97158f)), owning_building_guid = 42)
      LocalObject(1633, Locker.Constructor(Vector3(642.8635f, 2402.571f, 44.05858f)), owning_building_guid = 42)
      LocalObject(1634, Locker.Constructor(Vector3(644.0822f, 2402.875f, 44.05858f)), owning_building_guid = 42)
      LocalObject(1635, Locker.Constructor(Vector3(645.3057f, 2403.18f, 44.05858f)), owning_building_guid = 42)
      LocalObject(1636, Locker.Constructor(Vector3(646.5302f, 2403.485f, 44.05858f)), owning_building_guid = 42)
      LocalObject(1637, Locker.Constructor(Vector3(647.7451f, 2403.788f, 44.05858f)), owning_building_guid = 42)
      LocalObject(2102, Locker.Constructor(Vector3(582.5673f, 2389.328f, 54.06358f)), owning_building_guid = 42)
      LocalObject(2103, Locker.Constructor(Vector3(583.5706f, 2389.578f, 54.06358f)), owning_building_guid = 42)
      LocalObject(2104, Locker.Constructor(Vector3(586.0128f, 2390.187f, 53.83458f)), owning_building_guid = 42)
      LocalObject(2105, Locker.Constructor(Vector3(587.0161f, 2390.437f, 53.83458f)), owning_building_guid = 42)
      LocalObject(2106, Locker.Constructor(Vector3(588.0388f, 2390.692f, 53.83458f)), owning_building_guid = 42)
      LocalObject(2107, Locker.Constructor(Vector3(589.0421f, 2390.942f, 53.83458f)), owning_building_guid = 42)
      LocalObject(2108, Locker.Constructor(Vector3(591.4891f, 2391.552f, 54.06358f)), owning_building_guid = 42)
      LocalObject(2109, Locker.Constructor(Vector3(592.4924f, 2391.802f, 54.06358f)), owning_building_guid = 42)
      LocalObject(
        294,
        Terminal.Constructor(Vector3(636.3127f, 2417.709f, 44.05358f), cert_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        295,
        Terminal.Constructor(Vector3(637.3674f, 2419.464f, 44.05358f), cert_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        296,
        Terminal.Constructor(Vector3(639.3851f, 2405.386f, 44.05358f), cert_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        297,
        Terminal.Constructor(Vector3(641.1404f, 2404.332f, 44.05358f), cert_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        298,
        Terminal.Constructor(Vector3(644.4748f, 2421.236f, 44.05358f), cert_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        299,
        Terminal.Constructor(Vector3(646.2301f, 2420.182f, 44.05358f), cert_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        300,
        Terminal.Constructor(Vector3(648.2478f, 2406.104f, 44.05358f), cert_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        301,
        Terminal.Constructor(Vector3(649.3025f, 2407.859f, 44.05358f), cert_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2360,
        Terminal.Constructor(Vector3(596.817f, 2422.035f, 55.35858f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2361,
        Terminal.Constructor(Vector3(601.7007f, 2393.692f, 45.65358f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2362,
        Terminal.Constructor(Vector3(605.3209f, 2394.595f, 45.65358f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2363,
        Terminal.Constructor(Vector3(608.9974f, 2395.511f, 45.65358f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3389,
        Terminal.Constructor(Vector3(531.1984f, 2411.922f, 48.17658f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3391,
        Terminal.Constructor(Vector3(577.225f, 2404.852f, 55.64258f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3392,
        Terminal.Constructor(Vector3(579.7477f, 2448.668f, 45.67658f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3393,
        Terminal.Constructor(Vector3(594.3492f, 2405.961f, 46.19758f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3394,
        Terminal.Constructor(Vector3(601.4237f, 2407.729f, 46.19758f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3395,
        Terminal.Constructor(Vector3(608.4959f, 2409.49f, 46.19758f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3397,
        Terminal.Constructor(Vector3(651.9554f, 2426.058f, 45.67658f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3716,
        Terminal.Constructor(Vector3(537.7939f, 2358.204f, 56.36858f), vehicle_terminal_combined),
        owning_building_guid = 42
      )
      LocalObject(
        2304,
        VehicleSpawnPad.Constructor(Vector3(534.407f, 2371.416f, 52.21058f), mb_pad_creation, Vector3(0, 0, -14)),
        owning_building_guid = 42,
        terminal_guid = 3716
      )
      LocalObject(3217, ResourceSilo.Constructor(Vector3(624.7616f, 2480.779f, 61.08058f)), owning_building_guid = 42)
      LocalObject(
        3236,
        SpawnTube.Constructor(Vector3(595.5679f, 2407.026f, 44.06358f), Vector3(0, 0, 76)),
        owning_building_guid = 42
      )
      LocalObject(
        3237,
        SpawnTube.Constructor(Vector3(602.6424f, 2408.79f, 44.06358f), Vector3(0, 0, 76)),
        owning_building_guid = 42
      )
      LocalObject(
        3238,
        SpawnTube.Constructor(Vector3(609.7139f, 2410.553f, 44.06358f), Vector3(0, 0, 76)),
        owning_building_guid = 42
      )
      LocalObject(
        203,
        ProximityTerminal.Constructor(Vector3(583.5829f, 2406.356f, 53.87358f), adv_med_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2329,
        ProximityTerminal.Constructor(Vector3(600.5218f, 2384.135f, 44.06358f), medical_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2636,
        ProximityTerminal.Constructor(Vector3(522.3973f, 2395.002f, 62.41558f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2637,
        Terminal.Constructor(Vector3(522.3973f, 2395.002f, 62.41558f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2642,
        ProximityTerminal.Constructor(Vector3(525.5179f, 2412.594f, 64.35658f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2643,
        Terminal.Constructor(Vector3(525.5179f, 2412.594f, 64.35658f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2651,
        ProximityTerminal.Constructor(Vector3(634.2289f, 2419.664f, 64.39558f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2652,
        Terminal.Constructor(Vector3(634.2289f, 2419.664f, 64.39558f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2654,
        ProximityTerminal.Constructor(Vector3(638.4529f, 2437.436f, 62.40558f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        2655,
        Terminal.Constructor(Vector3(638.4529f, 2437.436f, 62.40558f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3084,
        ProximityTerminal.Constructor(Vector3(499.0919f, 2423.896f, 53.81358f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        3085,
        Terminal.Constructor(Vector3(499.0919f, 2423.896f, 53.81358f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        3092,
        ProximityTerminal.Constructor(Vector3(618.0981f, 2362.868f, 53.81358f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        3093,
        Terminal.Constructor(Vector3(618.0981f, 2362.868f, 53.81358f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2162,
        FacilityTurret.Constructor(Vector3(478.9274f, 2456.098f, 62.46558f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(2162, 5050)
      LocalObject(
        2163,
        FacilityTurret.Constructor(Vector3(511.6338f, 2324.887f, 62.46558f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(2163, 5051)
      LocalObject(
        2171,
        FacilityTurret.Constructor(Vector3(643.0322f, 2356.425f, 62.46558f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(2171, 5052)
      LocalObject(
        2172,
        FacilityTurret.Constructor(Vector3(650.7581f, 2498.949f, 62.46558f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(2172, 5053)
      LocalObject(
        2175,
        FacilityTurret.Constructor(Vector3(674.4222f, 2408.72f, 62.46558f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(2175, 5054)
      LocalObject(
        1224,
        ImplantTerminalMech.Constructor(Vector3(635.4853f, 2410.967f, 43.54058f)),
        owning_building_guid = 42
      )
      LocalObject(
        1214,
        Terminal.Constructor(Vector3(635.5027f, 2410.971f, 43.54058f), implant_terminal_interface),
        owning_building_guid = 42
      )
      TerminalToInterface(1224, 1214)
      LocalObject(
        1225,
        ImplantTerminalMech.Constructor(Vector3(650.3822f, 2414.693f, 43.54058f)),
        owning_building_guid = 42
      )
      LocalObject(
        1215,
        Terminal.Constructor(Vector3(650.3647f, 2414.689f, 43.54058f), implant_terminal_interface),
        owning_building_guid = 42
      )
      TerminalToInterface(1225, 1215)
      LocalObject(
        2880,
        Painbox.Constructor(Vector3(572.2711f, 2399.815f, 68.09238f), painbox),
        owning_building_guid = 42
      )
      LocalObject(
        2897,
        Painbox.Constructor(Vector3(601.0518f, 2397.551f, 48.13348f), painbox_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        2914,
        Painbox.Constructor(Vector3(585.9642f, 2404.683f, 68.29749f), painbox_door_radius),
        owning_building_guid = 42
      )
      LocalObject(
        2933,
        Painbox.Constructor(Vector3(590.4086f, 2399.24f, 46.41948f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        2934,
        Painbox.Constructor(Vector3(615.2295f, 2387.555f, 47.60448f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        2935,
        Painbox.Constructor(Vector3(618.0916f, 2404.105f, 45.77778f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(382, Generator.Constructor(Vector3(568.1184f, 2400.396f, 62.76958f)), owning_building_guid = 42)
      LocalObject(
        365,
        Terminal.Constructor(Vector3(576.0557f, 2402.424f, 64.06358f), gen_control),
        owning_building_guid = 42
      )
    }

    Building19()

    def Building19(): Unit = { // Name: Honsi Type: cryo_facility GUID: 45, MapID: 19
      LocalBuilding(
        "Honsi",
        45,
        19,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3996f, 4526f, 88.9639f),
            Vector3(0f, 0f, 360f),
            cryo_facility
          )
        )
      )
      LocalObject(
        284,
        CaptureTerminal.Constructor(Vector3(3967.911f, 4585.266f, 78.9639f), capture_terminal),
        owning_building_guid = 45
      )
      LocalObject(576, Door.Constructor(Vector3(3937.023f, 4530.5f, 90.5149f)), owning_building_guid = 45)
      LocalObject(577, Door.Constructor(Vector3(3937.023f, 4548.693f, 98.4789f)), owning_building_guid = 45)
      LocalObject(578, Door.Constructor(Vector3(3953.674f, 4593.803f, 90.5149f)), owning_building_guid = 45)
      LocalObject(579, Door.Constructor(Vector3(3971.867f, 4593.803f, 98.4789f)), owning_building_guid = 45)
      LocalObject(580, Door.Constructor(Vector3(3980f, 4546f, 100.4849f)), owning_building_guid = 45)
      LocalObject(591, Door.Constructor(Vector3(4000f, 4529.137f, 100.4849f)), owning_building_guid = 45)
      LocalObject(592, Door.Constructor(Vector3(4001.307f, 4443.073f, 98.4789f)), owning_building_guid = 45)
      LocalObject(593, Door.Constructor(Vector3(4019.5f, 4443.073f, 90.5149f)), owning_building_guid = 45)
      LocalObject(598, Door.Constructor(Vector3(4028f, 4434f, 90.4849f)), owning_building_guid = 45)
      LocalObject(599, Door.Constructor(Vector3(4046.927f, 4475.307f, 98.4789f)), owning_building_guid = 45)
      LocalObject(600, Door.Constructor(Vector3(4046.927f, 4493.5f, 90.5149f)), owning_building_guid = 45)
      LocalObject(970, Door.Constructor(Vector3(3948f, 4514f, 80.48489f)), owning_building_guid = 45)
      LocalObject(971, Door.Constructor(Vector3(3956f, 4466f, 82.98489f)), owning_building_guid = 45)
      LocalObject(972, Door.Constructor(Vector3(3960f, 4526f, 80.48489f)), owning_building_guid = 45)
      LocalObject(973, Door.Constructor(Vector3(3960f, 4566f, 80.48489f)), owning_building_guid = 45)
      LocalObject(974, Door.Constructor(Vector3(3964f, 4546f, 80.48489f)), owning_building_guid = 45)
      LocalObject(975, Door.Constructor(Vector3(3964f, 4594f, 80.48489f)), owning_building_guid = 45)
      LocalObject(976, Door.Constructor(Vector3(3972f, 4594f, 80.48489f)), owning_building_guid = 45)
      LocalObject(977, Door.Constructor(Vector3(3976f, 4470f, 82.98489f)), owning_building_guid = 45)
      LocalObject(978, Door.Constructor(Vector3(3976f, 4494f, 80.48489f)), owning_building_guid = 45)
      LocalObject(979, Door.Constructor(Vector3(3980f, 4546f, 80.48489f)), owning_building_guid = 45)
      LocalObject(980, Door.Constructor(Vector3(3980f, 4546f, 90.4849f)), owning_building_guid = 45)
      LocalObject(981, Door.Constructor(Vector3(3980f, 4594f, 80.48489f)), owning_building_guid = 45)
      LocalObject(982, Door.Constructor(Vector3(3984f, 4438f, 82.98489f)), owning_building_guid = 45)
      LocalObject(983, Door.Constructor(Vector3(3984f, 4526f, 80.48489f)), owning_building_guid = 45)
      LocalObject(984, Door.Constructor(Vector3(3984f, 4566f, 80.48489f)), owning_building_guid = 45)
      LocalObject(985, Door.Constructor(Vector3(3996f, 4498f, 80.48489f)), owning_building_guid = 45)
      LocalObject(986, Door.Constructor(Vector3(3996f, 4522f, 80.48489f)), owning_building_guid = 45)
      LocalObject(987, Door.Constructor(Vector3(3996f, 4578f, 80.48489f)), owning_building_guid = 45)
      LocalObject(988, Door.Constructor(Vector3(4000f, 4518f, 100.4849f)), owning_building_guid = 45)
      LocalObject(989, Door.Constructor(Vector3(4008f, 4526f, 80.48489f)), owning_building_guid = 45)
      LocalObject(990, Door.Constructor(Vector3(4008f, 4550f, 80.48489f)), owning_building_guid = 45)
      LocalObject(991, Door.Constructor(Vector3(4020f, 4530f, 72.98489f)), owning_building_guid = 45)
      LocalObject(992, Door.Constructor(Vector3(4020f, 4546f, 80.48489f)), owning_building_guid = 45)
      LocalObject(1194, Door.Constructor(Vector3(3999.992f, 4551.083f, 91.24689f)), owning_building_guid = 45)
      LocalObject(1206, Door.Constructor(Vector3(3992f, 4534f, 90.48289f)), owning_building_guid = 45)
      LocalObject(1207, Door.Constructor(Vector3(4008f, 4526f, 90.4849f)), owning_building_guid = 45)
      LocalObject(3554, Door.Constructor(Vector3(4000.673f, 4531.733f, 80.8179f)), owning_building_guid = 45)
      LocalObject(3555, Door.Constructor(Vector3(4000.673f, 4539.026f, 80.8179f)), owning_building_guid = 45)
      LocalObject(3556, Door.Constructor(Vector3(4000.673f, 4546.315f, 80.8179f)), owning_building_guid = 45)
      LocalObject(
        1258,
        IFFLock.Constructor(Vector3(3996.77f, 4553.822f, 90.4459f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 1194
      )
      LocalObject(
        1412,
        IFFLock.Constructor(Vector3(3963.06f, 4592.428f, 80.2999f), Vector3(0, 0, 270)),
        owning_building_guid = 45,
        door_guid = 975
      )
      LocalObject(
        1413,
        IFFLock.Constructor(Vector3(3972.813f, 4595.572f, 80.2999f), Vector3(0, 0, 90)),
        owning_building_guid = 45,
        door_guid = 976
      )
      LocalObject(
        1417,
        IFFLock.Constructor(Vector3(3980.814f, 4548.043f, 100.4159f), Vector3(0, 0, 90)),
        owning_building_guid = 45,
        door_guid = 580
      )
      LocalObject(
        1425,
        IFFLock.Constructor(Vector3(3997.954f, 4529.958f, 100.4159f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 591
      )
      LocalObject(
        1426,
        IFFLock.Constructor(Vector3(4006.428f, 4550.81f, 80.2999f), Vector3(0, 0, 0)),
        owning_building_guid = 45,
        door_guid = 990
      )
      LocalObject(
        1427,
        IFFLock.Constructor(Vector3(4009.572f, 4525.19f, 80.2999f), Vector3(0, 0, 180)),
        owning_building_guid = 45,
        door_guid = 989
      )
      LocalObject(
        1430,
        IFFLock.Constructor(Vector3(4019.19f, 4528.428f, 72.7999f), Vector3(0, 0, 270)),
        owning_building_guid = 45,
        door_guid = 991
      )
      LocalObject(
        1433,
        IFFLock.Constructor(Vector3(4028.814f, 4436.042f, 90.4159f), Vector3(0, 0, 90)),
        owning_building_guid = 45,
        door_guid = 598
      )
      LocalObject(1826, Locker.Constructor(Vector3(4011.563f, 4528.141f, 79.2249f)), owning_building_guid = 45)
      LocalObject(1827, Locker.Constructor(Vector3(4012.727f, 4528.141f, 79.2249f)), owning_building_guid = 45)
      LocalObject(1828, Locker.Constructor(Vector3(4013.874f, 4528.141f, 79.2249f)), owning_building_guid = 45)
      LocalObject(1829, Locker.Constructor(Vector3(4015.023f, 4528.141f, 79.2249f)), owning_building_guid = 45)
      LocalObject(1830, Locker.Constructor(Vector3(4015.997f, 4575.496f, 78.9589f)), owning_building_guid = 45)
      LocalObject(1831, Locker.Constructor(Vector3(4015.997f, 4576.752f, 78.9589f)), owning_building_guid = 45)
      LocalObject(1832, Locker.Constructor(Vector3(4015.997f, 4578.013f, 78.9589f)), owning_building_guid = 45)
      LocalObject(1833, Locker.Constructor(Vector3(4015.997f, 4579.275f, 78.9589f)), owning_building_guid = 45)
      LocalObject(1834, Locker.Constructor(Vector3(4015.997f, 4580.527f, 78.9589f)), owning_building_guid = 45)
      LocalObject(1841, Locker.Constructor(Vector3(4028.817f, 4528.36f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1842, Locker.Constructor(Vector3(4028.814f, 4548.361f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1845, Locker.Constructor(Vector3(4029.873f, 4528.36f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1846, Locker.Constructor(Vector3(4029.868f, 4548.361f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1847, Locker.Constructor(Vector3(4030.928f, 4528.36f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1848, Locker.Constructor(Vector3(4030.929f, 4548.361f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1849, Locker.Constructor(Vector3(4031.983f, 4528.36f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1850, Locker.Constructor(Vector3(4031.984f, 4548.361f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1851, Locker.Constructor(Vector3(4033.043f, 4528.36f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1852, Locker.Constructor(Vector3(4033.039f, 4548.361f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1853, Locker.Constructor(Vector3(4034.098f, 4528.36f, 78.87189f)), owning_building_guid = 45)
      LocalObject(1854, Locker.Constructor(Vector3(4034.095f, 4548.361f, 78.87189f)), owning_building_guid = 45)
      LocalObject(2110, Locker.Constructor(Vector3(4014.26f, 4513.787f, 88.9639f)), owning_building_guid = 45)
      LocalObject(2111, Locker.Constructor(Vector3(4014.26f, 4514.821f, 88.9639f)), owning_building_guid = 45)
      LocalObject(2112, Locker.Constructor(Vector3(4014.26f, 4517.338f, 88.7349f)), owning_building_guid = 45)
      LocalObject(2113, Locker.Constructor(Vector3(4014.26f, 4518.372f, 88.7349f)), owning_building_guid = 45)
      LocalObject(2114, Locker.Constructor(Vector3(4014.26f, 4519.426f, 88.7349f)), owning_building_guid = 45)
      LocalObject(2115, Locker.Constructor(Vector3(4014.26f, 4520.46f, 88.7349f)), owning_building_guid = 45)
      LocalObject(2116, Locker.Constructor(Vector3(4014.26f, 4522.982f, 88.9639f)), owning_building_guid = 45)
      LocalObject(2117, Locker.Constructor(Vector3(4014.26f, 4524.016f, 88.9639f)), owning_building_guid = 45)
      LocalObject(
        302,
        Terminal.Constructor(Vector3(3998.276f, 4574.25f, 78.9539f), cert_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        303,
        Terminal.Constructor(Vector3(3998.276f, 4581.575f, 78.9539f), cert_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        304,
        Terminal.Constructor(Vector3(3999.724f, 4572.802f, 78.9539f), cert_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        305,
        Terminal.Constructor(Vector3(3999.724f, 4583.023f, 78.9539f), cert_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        306,
        Terminal.Constructor(Vector3(4012.424f, 4572.802f, 78.9539f), cert_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        307,
        Terminal.Constructor(Vector3(4012.424f, 4583.023f, 78.9539f), cert_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        308,
        Terminal.Constructor(Vector3(4013.872f, 4574.25f, 78.9539f), cert_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        309,
        Terminal.Constructor(Vector3(4013.872f, 4581.575f, 78.9539f), cert_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2442,
        Terminal.Constructor(Vector3(3985.972f, 4535.526f, 90.2589f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2449,
        Terminal.Constructor(Vector3(4014.654f, 4533.408f, 80.55389f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2450,
        Terminal.Constructor(Vector3(4014.654f, 4537.139f, 80.55389f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2451,
        Terminal.Constructor(Vector3(4014.654f, 4540.928f, 80.55389f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3428,
        Terminal.Constructor(Vector3(3956f, 4525.407f, 80.5769f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3429,
        Terminal.Constructor(Vector3(3979.91f, 4469.41f, 83.0769f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3430,
        Terminal.Constructor(Vector3(3995.407f, 4590f, 80.5769f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3431,
        Terminal.Constructor(Vector3(3997.905f, 4512.359f, 90.5429f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3432,
        Terminal.Constructor(Vector3(4000.971f, 4529.243f, 81.0979f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3433,
        Terminal.Constructor(Vector3(4000.967f, 4536.535f, 81.0979f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3434,
        Terminal.Constructor(Vector3(4000.97f, 4543.823f, 81.0979f), spawn_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3723,
        Terminal.Constructor(Vector3(4033.628f, 4462.814f, 91.2689f), vehicle_terminal_combined),
        owning_building_guid = 45
      )
      LocalObject(
        2313,
        VehicleSpawnPad.Constructor(Vector3(4019.989f, 4462.724f, 87.1109f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 45,
        terminal_guid = 3723
      )
      LocalObject(3223, ResourceSilo.Constructor(Vector3(3935.733f, 4576.852f, 95.9809f)), owning_building_guid = 45)
      LocalObject(
        3290,
        SpawnTube.Constructor(Vector3(4000.233f, 4530.683f, 78.9639f), Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        3291,
        SpawnTube.Constructor(Vector3(4000.233f, 4537.974f, 78.9639f), Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        3292,
        SpawnTube.Constructor(Vector3(4000.233f, 4545.262f, 78.9639f), Vector3(0, 0, 0)),
        owning_building_guid = 45
      )
      LocalObject(
        204,
        ProximityTerminal.Constructor(Vector3(3997.983f, 4518.892f, 88.7739f), adv_med_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2340,
        ProximityTerminal.Constructor(Vector3(4023.642f, 4529.952f, 78.9639f), medical_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2705,
        ProximityTerminal.Constructor(Vector3(3977.883f, 4464.061f, 99.2569f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2706,
        Terminal.Constructor(Vector3(3977.883f, 4464.061f, 99.2569f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2708,
        ProximityTerminal.Constructor(Vector3(3981.101f, 4579.651f, 97.3059f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2709,
        Terminal.Constructor(Vector3(3981.101f, 4579.651f, 97.3059f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2711,
        ProximityTerminal.Constructor(Vector3(3994.198f, 4456.777f, 97.3159f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2712,
        Terminal.Constructor(Vector3(3994.198f, 4456.777f, 97.3159f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2714,
        ProximityTerminal.Constructor(Vector3(3997.323f, 4571.253f, 99.2959f), pad_landing_frame),
        owning_building_guid = 45
      )
      LocalObject(
        2715,
        Terminal.Constructor(Vector3(3997.323f, 4571.253f, 99.2959f), air_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3136,
        ProximityTerminal.Constructor(Vector3(3960.525f, 4441.154f, 88.7139f), repair_silo),
        owning_building_guid = 45
      )
      LocalObject(
        3137,
        Terminal.Constructor(Vector3(3960.525f, 4441.154f, 88.7139f), ground_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        3140,
        ProximityTerminal.Constructor(Vector3(4048.53f, 4541.861f, 88.7139f), repair_silo),
        owning_building_guid = 45
      )
      LocalObject(
        3141,
        Terminal.Constructor(Vector3(4048.53f, 4541.861f, 88.7139f), ground_rearm_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        2224,
        FacilityTurret.Constructor(Vector3(3924.392f, 4606.472f, 97.3659f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(2224, 5055)
      LocalObject(
        2225,
        FacilityTurret.Constructor(Vector3(3924.4f, 4429.379f, 97.3659f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(2225, 5056)
      LocalObject(
        2229,
        FacilityTurret.Constructor(Vector3(4017.665f, 4607.605f, 97.3659f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(2229, 5057)
      LocalObject(
        2231,
        FacilityTurret.Constructor(Vector3(4059.626f, 4429.371f, 97.3659f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(2231, 5058)
      LocalObject(
        2232,
        FacilityTurret.Constructor(Vector3(4060.813f, 4564.496f, 97.3659f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(2232, 5059)
      LocalObject(
        1226,
        ImplantTerminalMech.Constructor(Vector3(4006.066f, 4570.368f, 78.4409f)),
        owning_building_guid = 45
      )
      LocalObject(
        1216,
        Terminal.Constructor(Vector3(4006.066f, 4570.386f, 78.4409f), implant_terminal_interface),
        owning_building_guid = 45
      )
      TerminalToInterface(1226, 1216)
      LocalObject(
        1227,
        ImplantTerminalMech.Constructor(Vector3(4006.054f, 4585.724f, 78.4409f)),
        owning_building_guid = 45
      )
      LocalObject(
        1217,
        Terminal.Constructor(Vector3(4006.054f, 4585.706f, 78.4409f), implant_terminal_interface),
        owning_building_guid = 45
      )
      TerminalToInterface(1227, 1217)
      LocalObject(
        2886,
        Painbox.Constructor(Vector3(4001.594f, 4506.334f, 102.9927f), painbox),
        owning_building_guid = 45
      )
      LocalObject(
        2903,
        Painbox.Constructor(Vector3(4010.753f, 4533.712f, 83.0338f), painbox_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        2920,
        Painbox.Constructor(Vector3(4000.182f, 4520.798f, 103.1978f), painbox_door_radius),
        owning_building_guid = 45
      )
      LocalObject(
        2951,
        Painbox.Constructor(Vector3(4006.54f, 4523.793f, 81.31979f), painbox_door_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        2952,
        Painbox.Constructor(Vector3(4008.515f, 4551.831f, 80.6781f), painbox_door_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        2953,
        Painbox.Constructor(Vector3(4023.882f, 4545.05f, 82.5048f), painbox_door_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(388, Generator.Constructor(Vector3(4000.025f, 4502.445f, 97.6699f)), owning_building_guid = 45)
      LocalObject(
        371,
        Terminal.Constructor(Vector3(3999.978f, 4510.637f, 98.9639f), gen_control),
        owning_building_guid = 45
      )
    }

    Building4()

    def Building4(): Unit = { // Name: Chuku Type: cryo_facility GUID: 48, MapID: 4
      LocalBuilding(
        "Chuku",
        48,
        4,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4162f, 6962f, 54.02264f),
            Vector3(0f, 0f, 360f),
            cryo_facility
          )
        )
      )
      LocalObject(
        285,
        CaptureTerminal.Constructor(Vector3(4133.911f, 7021.266f, 44.02264f), capture_terminal),
        owning_building_guid = 48
      )
      LocalObject(601, Door.Constructor(Vector3(4103.023f, 6966.5f, 55.57364f)), owning_building_guid = 48)
      LocalObject(602, Door.Constructor(Vector3(4103.023f, 6984.693f, 63.53764f)), owning_building_guid = 48)
      LocalObject(603, Door.Constructor(Vector3(4119.674f, 7029.803f, 55.57364f)), owning_building_guid = 48)
      LocalObject(604, Door.Constructor(Vector3(4137.867f, 7029.803f, 63.53764f)), owning_building_guid = 48)
      LocalObject(605, Door.Constructor(Vector3(4146f, 6982f, 65.54364f)), owning_building_guid = 48)
      LocalObject(606, Door.Constructor(Vector3(4166f, 6965.137f, 65.54364f)), owning_building_guid = 48)
      LocalObject(607, Door.Constructor(Vector3(4167.307f, 6879.073f, 63.53764f)), owning_building_guid = 48)
      LocalObject(608, Door.Constructor(Vector3(4185.5f, 6879.073f, 55.57364f)), owning_building_guid = 48)
      LocalObject(609, Door.Constructor(Vector3(4194f, 6870f, 55.54364f)), owning_building_guid = 48)
      LocalObject(610, Door.Constructor(Vector3(4212.927f, 6911.307f, 63.53764f)), owning_building_guid = 48)
      LocalObject(611, Door.Constructor(Vector3(4212.927f, 6929.5f, 55.57364f)), owning_building_guid = 48)
      LocalObject(993, Door.Constructor(Vector3(4114f, 6950f, 45.54364f)), owning_building_guid = 48)
      LocalObject(994, Door.Constructor(Vector3(4122f, 6902f, 48.04364f)), owning_building_guid = 48)
      LocalObject(995, Door.Constructor(Vector3(4126f, 6962f, 45.54364f)), owning_building_guid = 48)
      LocalObject(996, Door.Constructor(Vector3(4126f, 7002f, 45.54364f)), owning_building_guid = 48)
      LocalObject(997, Door.Constructor(Vector3(4130f, 6982f, 45.54364f)), owning_building_guid = 48)
      LocalObject(998, Door.Constructor(Vector3(4130f, 7030f, 45.54364f)), owning_building_guid = 48)
      LocalObject(999, Door.Constructor(Vector3(4138f, 7030f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1000, Door.Constructor(Vector3(4142f, 6906f, 48.04364f)), owning_building_guid = 48)
      LocalObject(1001, Door.Constructor(Vector3(4142f, 6930f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1002, Door.Constructor(Vector3(4146f, 6982f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1003, Door.Constructor(Vector3(4146f, 6982f, 55.54364f)), owning_building_guid = 48)
      LocalObject(1004, Door.Constructor(Vector3(4146f, 7030f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1005, Door.Constructor(Vector3(4150f, 6874f, 48.04364f)), owning_building_guid = 48)
      LocalObject(1006, Door.Constructor(Vector3(4150f, 6962f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1007, Door.Constructor(Vector3(4150f, 7002f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1008, Door.Constructor(Vector3(4162f, 6934f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1009, Door.Constructor(Vector3(4162f, 6958f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1010, Door.Constructor(Vector3(4162f, 7014f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1011, Door.Constructor(Vector3(4166f, 6954f, 65.54364f)), owning_building_guid = 48)
      LocalObject(1012, Door.Constructor(Vector3(4174f, 6962f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1013, Door.Constructor(Vector3(4174f, 6986f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1014, Door.Constructor(Vector3(4186f, 6966f, 38.04364f)), owning_building_guid = 48)
      LocalObject(1015, Door.Constructor(Vector3(4186f, 6982f, 45.54364f)), owning_building_guid = 48)
      LocalObject(1195, Door.Constructor(Vector3(4165.992f, 6987.083f, 56.30564f)), owning_building_guid = 48)
      LocalObject(1208, Door.Constructor(Vector3(4158f, 6970f, 55.54165f)), owning_building_guid = 48)
      LocalObject(1209, Door.Constructor(Vector3(4174f, 6962f, 55.54364f)), owning_building_guid = 48)
      LocalObject(3559, Door.Constructor(Vector3(4166.673f, 6967.733f, 45.87664f)), owning_building_guid = 48)
      LocalObject(3560, Door.Constructor(Vector3(4166.673f, 6975.026f, 45.87664f)), owning_building_guid = 48)
      LocalObject(3561, Door.Constructor(Vector3(4166.673f, 6982.315f, 45.87664f)), owning_building_guid = 48)
      LocalObject(
        1259,
        IFFLock.Constructor(Vector3(4162.77f, 6989.822f, 55.50464f), Vector3(0, 0, 0)),
        owning_building_guid = 48,
        door_guid = 1195
      )
      LocalObject(
        1434,
        IFFLock.Constructor(Vector3(4129.06f, 7028.428f, 45.35864f), Vector3(0, 0, 270)),
        owning_building_guid = 48,
        door_guid = 998
      )
      LocalObject(
        1435,
        IFFLock.Constructor(Vector3(4138.813f, 7031.572f, 45.35864f), Vector3(0, 0, 90)),
        owning_building_guid = 48,
        door_guid = 999
      )
      LocalObject(
        1436,
        IFFLock.Constructor(Vector3(4146.814f, 6984.043f, 65.47464f), Vector3(0, 0, 90)),
        owning_building_guid = 48,
        door_guid = 605
      )
      LocalObject(
        1437,
        IFFLock.Constructor(Vector3(4163.954f, 6965.958f, 65.47464f), Vector3(0, 0, 0)),
        owning_building_guid = 48,
        door_guid = 606
      )
      LocalObject(
        1438,
        IFFLock.Constructor(Vector3(4172.428f, 6986.81f, 45.35864f), Vector3(0, 0, 0)),
        owning_building_guid = 48,
        door_guid = 1013
      )
      LocalObject(
        1439,
        IFFLock.Constructor(Vector3(4175.572f, 6961.19f, 45.35864f), Vector3(0, 0, 180)),
        owning_building_guid = 48,
        door_guid = 1012
      )
      LocalObject(
        1440,
        IFFLock.Constructor(Vector3(4185.19f, 6964.428f, 37.85864f), Vector3(0, 0, 270)),
        owning_building_guid = 48,
        door_guid = 1014
      )
      LocalObject(
        1441,
        IFFLock.Constructor(Vector3(4194.814f, 6872.042f, 55.47464f), Vector3(0, 0, 90)),
        owning_building_guid = 48,
        door_guid = 609
      )
      LocalObject(1855, Locker.Constructor(Vector3(4177.563f, 6964.141f, 44.28365f)), owning_building_guid = 48)
      LocalObject(1856, Locker.Constructor(Vector3(4178.727f, 6964.141f, 44.28365f)), owning_building_guid = 48)
      LocalObject(1857, Locker.Constructor(Vector3(4179.874f, 6964.141f, 44.28365f)), owning_building_guid = 48)
      LocalObject(1858, Locker.Constructor(Vector3(4181.023f, 6964.141f, 44.28365f)), owning_building_guid = 48)
      LocalObject(1859, Locker.Constructor(Vector3(4181.997f, 7011.496f, 44.01764f)), owning_building_guid = 48)
      LocalObject(1860, Locker.Constructor(Vector3(4181.997f, 7012.752f, 44.01764f)), owning_building_guid = 48)
      LocalObject(1861, Locker.Constructor(Vector3(4181.997f, 7014.013f, 44.01764f)), owning_building_guid = 48)
      LocalObject(1862, Locker.Constructor(Vector3(4181.997f, 7015.275f, 44.01764f)), owning_building_guid = 48)
      LocalObject(1863, Locker.Constructor(Vector3(4181.997f, 7016.527f, 44.01764f)), owning_building_guid = 48)
      LocalObject(1864, Locker.Constructor(Vector3(4194.817f, 6964.36f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1865, Locker.Constructor(Vector3(4194.814f, 6984.361f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1866, Locker.Constructor(Vector3(4195.873f, 6964.36f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1867, Locker.Constructor(Vector3(4195.868f, 6984.361f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1868, Locker.Constructor(Vector3(4196.928f, 6964.36f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1869, Locker.Constructor(Vector3(4196.929f, 6984.361f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1870, Locker.Constructor(Vector3(4197.983f, 6964.36f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1871, Locker.Constructor(Vector3(4197.984f, 6984.361f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1872, Locker.Constructor(Vector3(4199.043f, 6964.36f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1873, Locker.Constructor(Vector3(4199.039f, 6984.361f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1874, Locker.Constructor(Vector3(4200.098f, 6964.36f, 43.93064f)), owning_building_guid = 48)
      LocalObject(1875, Locker.Constructor(Vector3(4200.095f, 6984.361f, 43.93064f)), owning_building_guid = 48)
      LocalObject(2118, Locker.Constructor(Vector3(4180.26f, 6949.787f, 54.02264f)), owning_building_guid = 48)
      LocalObject(2119, Locker.Constructor(Vector3(4180.26f, 6950.821f, 54.02264f)), owning_building_guid = 48)
      LocalObject(2120, Locker.Constructor(Vector3(4180.26f, 6953.338f, 53.79364f)), owning_building_guid = 48)
      LocalObject(2121, Locker.Constructor(Vector3(4180.26f, 6954.372f, 53.79364f)), owning_building_guid = 48)
      LocalObject(2122, Locker.Constructor(Vector3(4180.26f, 6955.426f, 53.79364f)), owning_building_guid = 48)
      LocalObject(2123, Locker.Constructor(Vector3(4180.26f, 6956.46f, 53.79364f)), owning_building_guid = 48)
      LocalObject(2124, Locker.Constructor(Vector3(4180.26f, 6958.982f, 54.02264f)), owning_building_guid = 48)
      LocalObject(2125, Locker.Constructor(Vector3(4180.26f, 6960.016f, 54.02264f)), owning_building_guid = 48)
      LocalObject(
        310,
        Terminal.Constructor(Vector3(4164.276f, 7010.25f, 44.01264f), cert_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        311,
        Terminal.Constructor(Vector3(4164.276f, 7017.575f, 44.01264f), cert_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        312,
        Terminal.Constructor(Vector3(4165.724f, 7008.802f, 44.01264f), cert_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        313,
        Terminal.Constructor(Vector3(4165.724f, 7019.023f, 44.01264f), cert_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        314,
        Terminal.Constructor(Vector3(4178.424f, 7008.802f, 44.01264f), cert_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        315,
        Terminal.Constructor(Vector3(4178.424f, 7019.023f, 44.01264f), cert_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        316,
        Terminal.Constructor(Vector3(4179.872f, 7010.25f, 44.01264f), cert_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        317,
        Terminal.Constructor(Vector3(4179.872f, 7017.575f, 44.01264f), cert_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2455,
        Terminal.Constructor(Vector3(4151.972f, 6971.526f, 55.31764f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2456,
        Terminal.Constructor(Vector3(4180.654f, 6969.408f, 45.61264f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2457,
        Terminal.Constructor(Vector3(4180.654f, 6973.139f, 45.61264f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2458,
        Terminal.Constructor(Vector3(4180.654f, 6976.928f, 45.61264f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3435,
        Terminal.Constructor(Vector3(4122f, 6961.407f, 45.63564f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3436,
        Terminal.Constructor(Vector3(4145.91f, 6905.41f, 48.13564f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3437,
        Terminal.Constructor(Vector3(4161.407f, 7026f, 45.63564f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3438,
        Terminal.Constructor(Vector3(4163.905f, 6948.359f, 55.60164f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3439,
        Terminal.Constructor(Vector3(4166.971f, 6965.243f, 46.15664f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3440,
        Terminal.Constructor(Vector3(4166.967f, 6972.535f, 46.15664f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3441,
        Terminal.Constructor(Vector3(4166.97f, 6979.823f, 46.15664f), spawn_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3724,
        Terminal.Constructor(Vector3(4199.628f, 6898.814f, 56.32764f), vehicle_terminal_combined),
        owning_building_guid = 48
      )
      LocalObject(
        2314,
        VehicleSpawnPad.Constructor(Vector3(4185.989f, 6898.724f, 52.16964f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 48,
        terminal_guid = 3724
      )
      LocalObject(3224, ResourceSilo.Constructor(Vector3(4101.733f, 7012.852f, 61.03964f)), owning_building_guid = 48)
      LocalObject(
        3295,
        SpawnTube.Constructor(Vector3(4166.233f, 6966.683f, 44.02264f), Vector3(0, 0, 0)),
        owning_building_guid = 48
      )
      LocalObject(
        3296,
        SpawnTube.Constructor(Vector3(4166.233f, 6973.974f, 44.02264f), Vector3(0, 0, 0)),
        owning_building_guid = 48
      )
      LocalObject(
        3297,
        SpawnTube.Constructor(Vector3(4166.233f, 6981.262f, 44.02264f), Vector3(0, 0, 0)),
        owning_building_guid = 48
      )
      LocalObject(
        205,
        ProximityTerminal.Constructor(Vector3(4163.983f, 6954.892f, 53.83265f), adv_med_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2341,
        ProximityTerminal.Constructor(Vector3(4189.642f, 6965.952f, 44.02264f), medical_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2717,
        ProximityTerminal.Constructor(Vector3(4143.883f, 6900.061f, 64.31564f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2718,
        Terminal.Constructor(Vector3(4143.883f, 6900.061f, 64.31564f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2720,
        ProximityTerminal.Constructor(Vector3(4147.101f, 7015.651f, 62.36464f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2721,
        Terminal.Constructor(Vector3(4147.101f, 7015.651f, 62.36464f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2723,
        ProximityTerminal.Constructor(Vector3(4160.198f, 6892.777f, 62.37465f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2724,
        Terminal.Constructor(Vector3(4160.198f, 6892.777f, 62.37465f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2726,
        ProximityTerminal.Constructor(Vector3(4163.323f, 7007.253f, 64.35464f), pad_landing_frame),
        owning_building_guid = 48
      )
      LocalObject(
        2727,
        Terminal.Constructor(Vector3(4163.323f, 7007.253f, 64.35464f), air_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3144,
        ProximityTerminal.Constructor(Vector3(4126.524f, 6877.154f, 53.77264f), repair_silo),
        owning_building_guid = 48
      )
      LocalObject(
        3145,
        Terminal.Constructor(Vector3(4126.524f, 6877.154f, 53.77264f), ground_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        3148,
        ProximityTerminal.Constructor(Vector3(4214.53f, 6977.861f, 53.77264f), repair_silo),
        owning_building_guid = 48
      )
      LocalObject(
        3149,
        Terminal.Constructor(Vector3(4214.53f, 6977.861f, 53.77264f), ground_rearm_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        2233,
        FacilityTurret.Constructor(Vector3(4090.392f, 7042.472f, 62.42464f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(2233, 5060)
      LocalObject(
        2234,
        FacilityTurret.Constructor(Vector3(4090.4f, 6865.379f, 62.42464f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(2234, 5061)
      LocalObject(
        2235,
        FacilityTurret.Constructor(Vector3(4183.665f, 7043.605f, 62.42464f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(2235, 5062)
      LocalObject(
        2236,
        FacilityTurret.Constructor(Vector3(4225.626f, 6865.371f, 62.42464f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(2236, 5063)
      LocalObject(
        2237,
        FacilityTurret.Constructor(Vector3(4226.813f, 7000.496f, 62.42464f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(2237, 5064)
      LocalObject(
        1228,
        ImplantTerminalMech.Constructor(Vector3(4172.066f, 7006.368f, 43.49965f)),
        owning_building_guid = 48
      )
      LocalObject(
        1218,
        Terminal.Constructor(Vector3(4172.066f, 7006.386f, 43.49965f), implant_terminal_interface),
        owning_building_guid = 48
      )
      TerminalToInterface(1228, 1218)
      LocalObject(
        1229,
        ImplantTerminalMech.Constructor(Vector3(4172.054f, 7021.724f, 43.49965f)),
        owning_building_guid = 48
      )
      LocalObject(
        1219,
        Terminal.Constructor(Vector3(4172.054f, 7021.706f, 43.49965f), implant_terminal_interface),
        owning_building_guid = 48
      )
      TerminalToInterface(1229, 1219)
      LocalObject(
        2887,
        Painbox.Constructor(Vector3(4167.593f, 6942.334f, 68.05145f), painbox),
        owning_building_guid = 48
      )
      LocalObject(
        2904,
        Painbox.Constructor(Vector3(4176.753f, 6969.712f, 48.09254f), painbox_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        2921,
        Painbox.Constructor(Vector3(4166.182f, 6956.798f, 68.25655f), painbox_door_radius),
        owning_building_guid = 48
      )
      LocalObject(
        2954,
        Painbox.Constructor(Vector3(4172.54f, 6959.793f, 46.37854f), painbox_door_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        2955,
        Painbox.Constructor(Vector3(4174.516f, 6987.831f, 45.73684f), painbox_door_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        2956,
        Painbox.Constructor(Vector3(4189.882f, 6981.05f, 47.56355f), painbox_door_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(389, Generator.Constructor(Vector3(4166.025f, 6938.445f, 62.72865f)), owning_building_guid = 48)
      LocalObject(
        372,
        Terminal.Constructor(Vector3(4165.978f, 6946.637f, 64.02264f), gen_control),
        owning_building_guid = 48
      )
    }

    Building8()

    def Building8(): Unit = { // Name: Itan Type: cryo_facility GUID: 51, MapID: 8
      LocalBuilding(
        "Itan",
        51,
        8,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5132f, 3334f, 48.0575f),
            Vector3(0f, 0f, 360f),
            cryo_facility
          )
        )
      )
      LocalObject(
        288,
        CaptureTerminal.Constructor(Vector3(5103.911f, 3393.266f, 38.0575f), capture_terminal),
        owning_building_guid = 51
      )
      LocalObject(651, Door.Constructor(Vector3(5073.023f, 3338.5f, 49.60849f)), owning_building_guid = 51)
      LocalObject(652, Door.Constructor(Vector3(5073.023f, 3356.693f, 57.57249f)), owning_building_guid = 51)
      LocalObject(654, Door.Constructor(Vector3(5089.674f, 3401.803f, 49.60849f)), owning_building_guid = 51)
      LocalObject(656, Door.Constructor(Vector3(5107.867f, 3401.803f, 57.57249f)), owning_building_guid = 51)
      LocalObject(658, Door.Constructor(Vector3(5116f, 3354f, 59.5785f)), owning_building_guid = 51)
      LocalObject(662, Door.Constructor(Vector3(5136f, 3337.137f, 59.5785f)), owning_building_guid = 51)
      LocalObject(663, Door.Constructor(Vector3(5137.307f, 3251.073f, 57.57249f)), owning_building_guid = 51)
      LocalObject(666, Door.Constructor(Vector3(5155.5f, 3251.073f, 49.60849f)), owning_building_guid = 51)
      LocalObject(674, Door.Constructor(Vector3(5164f, 3242f, 49.5785f)), owning_building_guid = 51)
      LocalObject(675, Door.Constructor(Vector3(5182.927f, 3283.307f, 57.57249f)), owning_building_guid = 51)
      LocalObject(676, Door.Constructor(Vector3(5182.927f, 3301.5f, 49.60849f)), owning_building_guid = 51)
      LocalObject(1052, Door.Constructor(Vector3(5084f, 3322f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1056, Door.Constructor(Vector3(5092f, 3274f, 42.0785f)), owning_building_guid = 51)
      LocalObject(1058, Door.Constructor(Vector3(5096f, 3334f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1059, Door.Constructor(Vector3(5096f, 3374f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1060, Door.Constructor(Vector3(5100f, 3354f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1061, Door.Constructor(Vector3(5100f, 3402f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1062, Door.Constructor(Vector3(5108f, 3402f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1063, Door.Constructor(Vector3(5112f, 3278f, 42.0785f)), owning_building_guid = 51)
      LocalObject(1064, Door.Constructor(Vector3(5112f, 3302f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1065, Door.Constructor(Vector3(5116f, 3354f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1066, Door.Constructor(Vector3(5116f, 3354f, 49.5785f)), owning_building_guid = 51)
      LocalObject(1067, Door.Constructor(Vector3(5116f, 3402f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1068, Door.Constructor(Vector3(5120f, 3246f, 42.0785f)), owning_building_guid = 51)
      LocalObject(1069, Door.Constructor(Vector3(5120f, 3334f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1070, Door.Constructor(Vector3(5120f, 3374f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1071, Door.Constructor(Vector3(5132f, 3306f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1072, Door.Constructor(Vector3(5132f, 3330f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1073, Door.Constructor(Vector3(5132f, 3386f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1074, Door.Constructor(Vector3(5136f, 3326f, 59.5785f)), owning_building_guid = 51)
      LocalObject(1075, Door.Constructor(Vector3(5144f, 3334f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1076, Door.Constructor(Vector3(5144f, 3358f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1077, Door.Constructor(Vector3(5156f, 3338f, 32.0785f)), owning_building_guid = 51)
      LocalObject(1078, Door.Constructor(Vector3(5156f, 3354f, 39.5785f)), owning_building_guid = 51)
      LocalObject(1198, Door.Constructor(Vector3(5135.992f, 3359.083f, 50.3405f)), owning_building_guid = 51)
      LocalObject(1210, Door.Constructor(Vector3(5128f, 3342f, 49.5765f)), owning_building_guid = 51)
      LocalObject(1211, Door.Constructor(Vector3(5144f, 3334f, 49.5785f)), owning_building_guid = 51)
      LocalObject(3572, Door.Constructor(Vector3(5136.673f, 3339.733f, 39.9115f)), owning_building_guid = 51)
      LocalObject(3573, Door.Constructor(Vector3(5136.673f, 3347.026f, 39.9115f)), owning_building_guid = 51)
      LocalObject(3574, Door.Constructor(Vector3(5136.673f, 3354.315f, 39.9115f)), owning_building_guid = 51)
      LocalObject(
        1262,
        IFFLock.Constructor(Vector3(5132.77f, 3361.822f, 49.53949f), Vector3(0, 0, 0)),
        owning_building_guid = 51,
        door_guid = 1198
      )
      LocalObject(
        1474,
        IFFLock.Constructor(Vector3(5099.06f, 3400.428f, 39.39349f), Vector3(0, 0, 270)),
        owning_building_guid = 51,
        door_guid = 1061
      )
      LocalObject(
        1475,
        IFFLock.Constructor(Vector3(5108.813f, 3403.572f, 39.39349f), Vector3(0, 0, 90)),
        owning_building_guid = 51,
        door_guid = 1062
      )
      LocalObject(
        1476,
        IFFLock.Constructor(Vector3(5116.814f, 3356.043f, 59.50949f), Vector3(0, 0, 90)),
        owning_building_guid = 51,
        door_guid = 658
      )
      LocalObject(
        1477,
        IFFLock.Constructor(Vector3(5133.954f, 3337.958f, 59.50949f), Vector3(0, 0, 0)),
        owning_building_guid = 51,
        door_guid = 662
      )
      LocalObject(
        1478,
        IFFLock.Constructor(Vector3(5142.428f, 3358.81f, 39.39349f), Vector3(0, 0, 0)),
        owning_building_guid = 51,
        door_guid = 1076
      )
      LocalObject(
        1479,
        IFFLock.Constructor(Vector3(5145.572f, 3333.19f, 39.39349f), Vector3(0, 0, 180)),
        owning_building_guid = 51,
        door_guid = 1075
      )
      LocalObject(
        1480,
        IFFLock.Constructor(Vector3(5155.19f, 3336.428f, 31.8935f), Vector3(0, 0, 270)),
        owning_building_guid = 51,
        door_guid = 1077
      )
      LocalObject(
        1487,
        IFFLock.Constructor(Vector3(5164.814f, 3244.042f, 49.50949f), Vector3(0, 0, 90)),
        owning_building_guid = 51,
        door_guid = 674
      )
      LocalObject(1916, Locker.Constructor(Vector3(5147.563f, 3336.141f, 38.3185f)), owning_building_guid = 51)
      LocalObject(1917, Locker.Constructor(Vector3(5148.727f, 3336.141f, 38.3185f)), owning_building_guid = 51)
      LocalObject(1918, Locker.Constructor(Vector3(5149.874f, 3336.141f, 38.3185f)), owning_building_guid = 51)
      LocalObject(1919, Locker.Constructor(Vector3(5151.023f, 3336.141f, 38.3185f)), owning_building_guid = 51)
      LocalObject(1920, Locker.Constructor(Vector3(5151.997f, 3383.496f, 38.05249f)), owning_building_guid = 51)
      LocalObject(1921, Locker.Constructor(Vector3(5151.997f, 3384.752f, 38.05249f)), owning_building_guid = 51)
      LocalObject(1922, Locker.Constructor(Vector3(5151.997f, 3386.013f, 38.05249f)), owning_building_guid = 51)
      LocalObject(1923, Locker.Constructor(Vector3(5151.997f, 3387.275f, 38.05249f)), owning_building_guid = 51)
      LocalObject(1924, Locker.Constructor(Vector3(5151.997f, 3388.527f, 38.05249f)), owning_building_guid = 51)
      LocalObject(1931, Locker.Constructor(Vector3(5164.817f, 3336.36f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1932, Locker.Constructor(Vector3(5164.814f, 3356.361f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1933, Locker.Constructor(Vector3(5165.873f, 3336.36f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1934, Locker.Constructor(Vector3(5165.868f, 3356.361f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1936, Locker.Constructor(Vector3(5166.928f, 3336.36f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1937, Locker.Constructor(Vector3(5166.929f, 3356.361f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1938, Locker.Constructor(Vector3(5167.983f, 3336.36f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1939, Locker.Constructor(Vector3(5167.984f, 3356.361f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1941, Locker.Constructor(Vector3(5169.043f, 3336.36f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1942, Locker.Constructor(Vector3(5169.039f, 3356.361f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1943, Locker.Constructor(Vector3(5170.098f, 3336.36f, 37.9655f)), owning_building_guid = 51)
      LocalObject(1944, Locker.Constructor(Vector3(5170.095f, 3356.361f, 37.9655f)), owning_building_guid = 51)
      LocalObject(2126, Locker.Constructor(Vector3(5150.26f, 3321.787f, 48.0575f)), owning_building_guid = 51)
      LocalObject(2127, Locker.Constructor(Vector3(5150.26f, 3322.821f, 48.0575f)), owning_building_guid = 51)
      LocalObject(2128, Locker.Constructor(Vector3(5150.26f, 3325.338f, 47.8285f)), owning_building_guid = 51)
      LocalObject(2129, Locker.Constructor(Vector3(5150.26f, 3326.372f, 47.8285f)), owning_building_guid = 51)
      LocalObject(2130, Locker.Constructor(Vector3(5150.26f, 3327.426f, 47.8285f)), owning_building_guid = 51)
      LocalObject(2131, Locker.Constructor(Vector3(5150.26f, 3328.46f, 47.8285f)), owning_building_guid = 51)
      LocalObject(2132, Locker.Constructor(Vector3(5150.26f, 3330.982f, 48.0575f)), owning_building_guid = 51)
      LocalObject(2133, Locker.Constructor(Vector3(5150.26f, 3332.016f, 48.0575f)), owning_building_guid = 51)
      LocalObject(
        318,
        Terminal.Constructor(Vector3(5134.276f, 3382.25f, 38.04749f), cert_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        319,
        Terminal.Constructor(Vector3(5134.276f, 3389.575f, 38.04749f), cert_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        320,
        Terminal.Constructor(Vector3(5135.724f, 3380.802f, 38.04749f), cert_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        321,
        Terminal.Constructor(Vector3(5135.724f, 3391.023f, 38.04749f), cert_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        322,
        Terminal.Constructor(Vector3(5148.424f, 3380.802f, 38.04749f), cert_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        323,
        Terminal.Constructor(Vector3(5148.424f, 3391.023f, 38.04749f), cert_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        324,
        Terminal.Constructor(Vector3(5149.872f, 3382.25f, 38.04749f), cert_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        325,
        Terminal.Constructor(Vector3(5149.872f, 3389.575f, 38.04749f), cert_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2477,
        Terminal.Constructor(Vector3(5121.972f, 3343.526f, 49.35249f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2478,
        Terminal.Constructor(Vector3(5150.654f, 3341.408f, 39.6475f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2479,
        Terminal.Constructor(Vector3(5150.654f, 3345.139f, 39.6475f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2480,
        Terminal.Constructor(Vector3(5150.654f, 3348.928f, 39.6475f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3457,
        Terminal.Constructor(Vector3(5092f, 3333.407f, 39.67049f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3459,
        Terminal.Constructor(Vector3(5115.91f, 3277.41f, 42.17049f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3460,
        Terminal.Constructor(Vector3(5131.407f, 3398f, 39.67049f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3461,
        Terminal.Constructor(Vector3(5133.905f, 3320.359f, 49.63649f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3462,
        Terminal.Constructor(Vector3(5136.971f, 3337.243f, 40.19149f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3463,
        Terminal.Constructor(Vector3(5136.967f, 3344.535f, 40.19149f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3464,
        Terminal.Constructor(Vector3(5136.97f, 3351.823f, 40.19149f), spawn_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3727,
        Terminal.Constructor(Vector3(5169.628f, 3270.814f, 50.3625f), vehicle_terminal_combined),
        owning_building_guid = 51
      )
      LocalObject(
        2319,
        VehicleSpawnPad.Constructor(Vector3(5155.989f, 3270.724f, 46.20449f), mb_pad_creation, Vector3(0, 0, -90)),
        owning_building_guid = 51,
        terminal_guid = 3727
      )
      LocalObject(3226, ResourceSilo.Constructor(Vector3(5071.733f, 3384.852f, 55.07449f)), owning_building_guid = 51)
      LocalObject(
        3308,
        SpawnTube.Constructor(Vector3(5136.233f, 3338.683f, 38.0575f), Vector3(0, 0, 0)),
        owning_building_guid = 51
      )
      LocalObject(
        3309,
        SpawnTube.Constructor(Vector3(5136.233f, 3345.974f, 38.0575f), Vector3(0, 0, 0)),
        owning_building_guid = 51
      )
      LocalObject(
        3310,
        SpawnTube.Constructor(Vector3(5136.233f, 3353.262f, 38.0575f), Vector3(0, 0, 0)),
        owning_building_guid = 51
      )
      LocalObject(
        206,
        ProximityTerminal.Constructor(Vector3(5133.983f, 3326.892f, 47.8675f), adv_med_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2346,
        ProximityTerminal.Constructor(Vector3(5159.642f, 3337.952f, 38.0575f), medical_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2759,
        ProximityTerminal.Constructor(Vector3(5113.883f, 3272.061f, 58.35049f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2760,
        Terminal.Constructor(Vector3(5113.883f, 3272.061f, 58.35049f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2762,
        ProximityTerminal.Constructor(Vector3(5117.101f, 3387.651f, 56.39949f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2763,
        Terminal.Constructor(Vector3(5117.101f, 3387.651f, 56.39949f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2765,
        ProximityTerminal.Constructor(Vector3(5130.198f, 3264.777f, 56.4095f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2766,
        Terminal.Constructor(Vector3(5130.198f, 3264.777f, 56.4095f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2768,
        ProximityTerminal.Constructor(Vector3(5133.323f, 3379.253f, 58.3895f), pad_landing_frame),
        owning_building_guid = 51
      )
      LocalObject(
        2769,
        Terminal.Constructor(Vector3(5133.323f, 3379.253f, 58.3895f), air_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3164,
        ProximityTerminal.Constructor(Vector3(5096.524f, 3249.154f, 47.8075f), repair_silo),
        owning_building_guid = 51
      )
      LocalObject(
        3165,
        Terminal.Constructor(Vector3(5096.524f, 3249.154f, 47.8075f), ground_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        3172,
        ProximityTerminal.Constructor(Vector3(5184.53f, 3349.861f, 47.8075f), repair_silo),
        owning_building_guid = 51
      )
      LocalObject(
        3173,
        Terminal.Constructor(Vector3(5184.53f, 3349.861f, 47.8075f), ground_rearm_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        2250,
        FacilityTurret.Constructor(Vector3(5060.392f, 3414.472f, 56.4595f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(2250, 5065)
      LocalObject(
        2251,
        FacilityTurret.Constructor(Vector3(5060.4f, 3237.379f, 56.4595f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(2251, 5066)
      LocalObject(
        2254,
        FacilityTurret.Constructor(Vector3(5153.665f, 3415.605f, 56.4595f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(2254, 5067)
      LocalObject(
        2255,
        FacilityTurret.Constructor(Vector3(5195.626f, 3237.371f, 56.4595f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(2255, 5068)
      LocalObject(
        2256,
        FacilityTurret.Constructor(Vector3(5196.813f, 3372.496f, 56.4595f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(2256, 5069)
      LocalObject(
        1230,
        ImplantTerminalMech.Constructor(Vector3(5142.066f, 3378.368f, 37.5345f)),
        owning_building_guid = 51
      )
      LocalObject(
        1220,
        Terminal.Constructor(Vector3(5142.066f, 3378.386f, 37.5345f), implant_terminal_interface),
        owning_building_guid = 51
      )
      TerminalToInterface(1230, 1220)
      LocalObject(
        1231,
        ImplantTerminalMech.Constructor(Vector3(5142.054f, 3393.724f, 37.5345f)),
        owning_building_guid = 51
      )
      LocalObject(
        1221,
        Terminal.Constructor(Vector3(5142.054f, 3393.706f, 37.5345f), implant_terminal_interface),
        owning_building_guid = 51
      )
      TerminalToInterface(1231, 1221)
      LocalObject(
        2890,
        Painbox.Constructor(Vector3(5137.593f, 3314.334f, 62.0863f), painbox),
        owning_building_guid = 51
      )
      LocalObject(
        2907,
        Painbox.Constructor(Vector3(5146.753f, 3341.712f, 42.1274f), painbox_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        2924,
        Painbox.Constructor(Vector3(5136.182f, 3328.798f, 62.2914f), painbox_door_radius),
        owning_building_guid = 51
      )
      LocalObject(
        2963,
        Painbox.Constructor(Vector3(5142.54f, 3331.793f, 40.41339f), painbox_door_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        2964,
        Painbox.Constructor(Vector3(5144.516f, 3359.831f, 39.77169f), painbox_door_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        2965,
        Painbox.Constructor(Vector3(5159.882f, 3353.05f, 41.5984f), painbox_door_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(392, Generator.Constructor(Vector3(5136.025f, 3310.445f, 56.7635f)), owning_building_guid = 51)
      LocalObject(
        375,
        Terminal.Constructor(Vector3(5135.978f, 3318.637f, 58.0575f), gen_control),
        owning_building_guid = 51
      )
    }

    Building16()

    def Building16(): Unit = { // Name: Shango Type: cryo_facility GUID: 54, MapID: 16
      LocalBuilding(
        "Shango",
        54,
        16,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(6774f, 2288f, 62.96152f),
            Vector3(0f, 0f, 331f),
            cryo_facility
          )
        )
      )
      LocalObject(
        291,
        CaptureTerminal.Constructor(Vector3(6778.166f, 2353.453f, 52.96152f), capture_terminal),
        owning_building_guid = 54
      )
      LocalObject(751, Door.Constructor(Vector3(6724.599f, 2320.528f, 64.51252f)), owning_building_guid = 54)
      LocalObject(752, Door.Constructor(Vector3(6733.419f, 2336.44f, 72.47652f)), owning_building_guid = 54)
      LocalObject(753, Door.Constructor(Vector3(6738.438f, 2212.897f, 72.47652f)), owning_building_guid = 54)
      LocalObject(754, Door.Constructor(Vector3(6754.35f, 2204.077f, 64.51252f)), owning_building_guid = 54)
      LocalObject(755, Door.Constructor(Vector3(6757.385f, 2192.021f, 64.48252f)), owning_building_guid = 54)
      LocalObject(756, Door.Constructor(Vector3(6769.702f, 2313.249f, 74.48251f)), owning_building_guid = 54)
      LocalObject(757, Door.Constructor(Vector3(6769.853f, 2367.822f, 64.51252f)), owning_building_guid = 54)
      LocalObject(758, Door.Constructor(Vector3(6779.02f, 2288.804f, 74.48251f)), owning_building_guid = 54)
      LocalObject(759, Door.Constructor(Vector3(6785.764f, 2359.002f, 72.47652f)), owning_building_guid = 54)
      LocalObject(760, Door.Constructor(Vector3(6793.965f, 2218.973f, 72.47652f)), owning_building_guid = 54)
      LocalObject(761, Door.Constructor(Vector3(6802.786f, 2234.885f, 64.51252f)), owning_building_guid = 54)
      LocalObject(1123, Door.Constructor(Vector3(6709.927f, 2254.915f, 56.98252f)), owning_building_guid = 54)
      LocalObject(1124, Door.Constructor(Vector3(6720.841f, 2216.851f, 56.98252f)), owning_building_guid = 54)
      LocalObject(1125, Door.Constructor(Vector3(6726.201f, 2300.775f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1126, Door.Constructor(Vector3(6729.358f, 2248.718f, 56.98252f)), owning_building_guid = 54)
      LocalObject(1127, Door.Constructor(Vector3(6740.994f, 2269.708f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1128, Door.Constructor(Vector3(6742.514f, 2305.453f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1129, Door.Constructor(Vector3(6755.708f, 2321.006f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1130, Door.Constructor(Vector3(6760.425f, 2263.511f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1131, Door.Constructor(Vector3(6761.906f, 2340.438f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1132, Door.Constructor(Vector3(6763.504f, 2293.818f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1133, Door.Constructor(Vector3(6769.702f, 2313.249f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1134, Door.Constructor(Vector3(6769.702f, 2313.249f, 64.48252f)), owning_building_guid = 54)
      LocalObject(1135, Door.Constructor(Vector3(6772.061f, 2284.501f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1136, Door.Constructor(Vector3(6773.62f, 2279.064f, 74.48251f)), owning_building_guid = 54)
      LocalObject(1137, Door.Constructor(Vector3(6778.979f, 2362.988f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1138, Door.Constructor(Vector3(6782.897f, 2328.802f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1139, Door.Constructor(Vector3(6784.496f, 2282.182f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1140, Door.Constructor(Vector3(6785.976f, 2359.11f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1141, Door.Constructor(Vector3(6792.973f, 2355.231f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1142, Door.Constructor(Vector3(6796.131f, 2303.173f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1143, Door.Constructor(Vector3(6796.93f, 2279.863f, 46.98252f)), owning_building_guid = 54)
      LocalObject(1144, Door.Constructor(Vector3(6799.21f, 2333.48f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1145, Door.Constructor(Vector3(6804.687f, 2293.857f, 54.48252f)), owning_building_guid = 54)
      LocalObject(1201, Door.Constructor(Vector3(6789.652f, 2308.003f, 65.24451f)), owning_building_guid = 54)
      LocalObject(1212, Door.Constructor(Vector3(6774.38f, 2296.936f, 64.48051f)), owning_building_guid = 54)
      LocalObject(1213, Door.Constructor(Vector3(6784.496f, 2282.182f, 64.48252f)), owning_building_guid = 54)
      LocalObject(3599, Door.Constructor(Vector3(6780.867f, 2290.749f, 54.81552f)), owning_building_guid = 54)
      LocalObject(3600, Door.Constructor(Vector3(6784.402f, 2297.127f, 54.81552f)), owning_building_guid = 54)
      LocalObject(3601, Door.Constructor(Vector3(6787.936f, 2303.502f, 54.81552f)), owning_building_guid = 54)
      LocalObject(
        1265,
        IFFLock.Constructor(Vector3(6788.162f, 2311.96f, 64.44352f), Vector3(0, 0, 29)),
        owning_building_guid = 54,
        door_guid = 1201
      )
      LocalObject(
        1546,
        IFFLock.Constructor(Vector3(6759.087f, 2193.412f, 64.41352f), Vector3(0, 0, 119)),
        owning_building_guid = 54,
        door_guid = 755
      )
      LocalObject(
        1547,
        IFFLock.Constructor(Vector3(6771.405f, 2314.642f, 74.41351f), Vector3(0, 0, 119)),
        owning_building_guid = 54,
        door_guid = 756
      )
      LocalObject(
        1548,
        IFFLock.Constructor(Vector3(6777.395f, 2362.069f, 54.29752f), Vector3(0, 0, 299)),
        owning_building_guid = 54,
        door_guid = 1137
      )
      LocalObject(
        1549,
        IFFLock.Constructor(Vector3(6777.628f, 2290.514f, 74.41351f), Vector3(0, 0, 29)),
        owning_building_guid = 54,
        door_guid = 758
      )
      LocalObject(
        1550,
        IFFLock.Constructor(Vector3(6785.478f, 2280.712f, 54.29752f), Vector3(0, 0, 209)),
        owning_building_guid = 54,
        door_guid = 1139
      )
      LocalObject(
        1551,
        IFFLock.Constructor(Vector3(6787.449f, 2360.09f, 54.29752f), Vector3(0, 0, 119)),
        owning_building_guid = 54,
        door_guid = 1140
      )
      LocalObject(
        1552,
        IFFLock.Constructor(Vector3(6795.148f, 2304.644f, 54.29752f), Vector3(0, 0, 29)),
        owning_building_guid = 54,
        door_guid = 1142
      )
      LocalObject(
        1553,
        IFFLock.Constructor(Vector3(6795.459f, 2278.881f, 46.79752f), Vector3(0, 0, 299)),
        owning_building_guid = 54,
        door_guid = 1143
      )
      LocalObject(2033, Locker.Constructor(Vector3(6788.65f, 2282.327f, 53.22252f)), owning_building_guid = 54)
      LocalObject(2034, Locker.Constructor(Vector3(6789.668f, 2281.763f, 53.22252f)), owning_building_guid = 54)
      LocalObject(2035, Locker.Constructor(Vector3(6790.671f, 2281.207f, 53.22252f)), owning_building_guid = 54)
      LocalObject(2036, Locker.Constructor(Vector3(6791.676f, 2280.65f, 53.22252f)), owning_building_guid = 54)
      LocalObject(2037, Locker.Constructor(Vector3(6803.847f, 2274.154f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2038, Locker.Constructor(Vector3(6804.77f, 2273.642f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2039, Locker.Constructor(Vector3(6805.693f, 2273.131f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2040, Locker.Constructor(Vector3(6806.616f, 2272.619f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2041, Locker.Constructor(Vector3(6807.542f, 2272.105f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2042, Locker.Constructor(Vector3(6808.465f, 2271.594f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2043, Locker.Constructor(Vector3(6813.541f, 2291.649f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2044, Locker.Constructor(Vector3(6814.462f, 2291.138f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2045, Locker.Constructor(Vector3(6815.391f, 2290.624f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2046, Locker.Constructor(Vector3(6815.486f, 2321.595f, 52.95652f)), owning_building_guid = 54)
      LocalObject(2047, Locker.Constructor(Vector3(6816.095f, 2322.694f, 52.95652f)), owning_building_guid = 54)
      LocalObject(2048, Locker.Constructor(Vector3(6816.313f, 2290.112f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2049, Locker.Constructor(Vector3(6816.706f, 2323.797f, 52.95652f)), owning_building_guid = 54)
      LocalObject(2050, Locker.Constructor(Vector3(6817.236f, 2289.601f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2051, Locker.Constructor(Vector3(6817.318f, 2324.901f, 52.95652f)), owning_building_guid = 54)
      LocalObject(2052, Locker.Constructor(Vector3(6817.925f, 2325.996f, 52.95652f)), owning_building_guid = 54)
      LocalObject(2053, Locker.Constructor(Vector3(6818.16f, 2289.089f, 52.86952f)), owning_building_guid = 54)
      LocalObject(2134, Locker.Constructor(Vector3(6784.05f, 2268.466f, 62.96152f)), owning_building_guid = 54)
      LocalObject(2135, Locker.Constructor(Vector3(6784.551f, 2269.37f, 62.96152f)), owning_building_guid = 54)
      LocalObject(2136, Locker.Constructor(Vector3(6785.771f, 2271.572f, 62.73252f)), owning_building_guid = 54)
      LocalObject(2137, Locker.Constructor(Vector3(6786.272f, 2272.476f, 62.73252f)), owning_building_guid = 54)
      LocalObject(2138, Locker.Constructor(Vector3(6786.783f, 2273.398f, 62.73252f)), owning_building_guid = 54)
      LocalObject(2139, Locker.Constructor(Vector3(6787.285f, 2274.302f, 62.73252f)), owning_building_guid = 54)
      LocalObject(2140, Locker.Constructor(Vector3(6788.507f, 2276.508f, 62.96152f)), owning_building_guid = 54)
      LocalObject(2141, Locker.Constructor(Vector3(6789.009f, 2277.412f, 62.96152f)), owning_building_guid = 54)
      LocalObject(
        326,
        Terminal.Constructor(Vector3(6799.383f, 2329.097f, 52.95152f), cert_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        327,
        Terminal.Constructor(Vector3(6799.947f, 2327.128f, 52.95152f), cert_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        328,
        Terminal.Constructor(Vector3(6802.934f, 2335.504f, 52.95152f), cert_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        329,
        Terminal.Constructor(Vector3(6804.902f, 2336.068f, 52.95152f), cert_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        330,
        Terminal.Constructor(Vector3(6811.055f, 2320.971f, 52.95152f), cert_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        331,
        Terminal.Constructor(Vector3(6813.023f, 2321.536f, 52.95152f), cert_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        332,
        Terminal.Constructor(Vector3(6816.01f, 2329.911f, 52.95152f), cert_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        333,
        Terminal.Constructor(Vector3(6816.575f, 2327.942f, 52.95152f), cert_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2520,
        Terminal.Constructor(Vector3(6769.848f, 2301.193f, 64.25652f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2521,
        Terminal.Constructor(Vector3(6793.907f, 2285.436f, 54.55152f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2522,
        Terminal.Constructor(Vector3(6795.715f, 2288.699f, 54.55152f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2523,
        Terminal.Constructor(Vector3(6797.552f, 2292.013f, 54.55152f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3477,
        Terminal.Constructor(Vector3(6732.492f, 2246.306f, 57.07452f), spawn_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3478,
        Terminal.Constructor(Vector3(6738.728f, 2306.874f, 54.57452f), spawn_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3479,
        Terminal.Constructor(Vector3(6769.053f, 2275.146f, 64.54052f), spawn_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3480,
        Terminal.Constructor(Vector3(6779.92f, 2288.427f, 55.09552f), spawn_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3481,
        Terminal.Constructor(Vector3(6783.452f, 2294.806f, 55.09552f), spawn_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3482,
        Terminal.Constructor(Vector3(6786.988f, 2301.179f, 55.09552f), spawn_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3483,
        Terminal.Constructor(Vector3(6804.509f, 2344.263f, 54.57452f), spawn_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3730,
        Terminal.Constructor(Vector3(6776.277f, 2214.494f, 65.26652f), vehicle_terminal_combined),
        owning_building_guid = 54
      )
      LocalObject(
        2322,
        VehicleSpawnPad.Constructor(Vector3(6764.304f, 2221.027f, 61.10852f), mb_pad_creation, Vector3(0, 0, -61)),
        owning_building_guid = 54,
        terminal_guid = 3730
      )
      LocalObject(3230, ResourceSilo.Constructor(Vector3(6745.943f, 2361.694f, 69.97852f)), owning_building_guid = 54)
      LocalObject(
        3335,
        SpawnTube.Constructor(Vector3(6779.973f, 2290.044f, 52.96152f), Vector3(0, 0, 29)),
        owning_building_guid = 54
      )
      LocalObject(
        3336,
        SpawnTube.Constructor(Vector3(6783.507f, 2296.42f, 52.96152f), Vector3(0, 0, 29)),
        owning_building_guid = 54
      )
      LocalObject(
        3337,
        SpawnTube.Constructor(Vector3(6787.041f, 2302.795f, 52.96152f), Vector3(0, 0, 29)),
        owning_building_guid = 54
      )
      LocalObject(
        207,
        ProximityTerminal.Constructor(Vector3(6772.289f, 2280.822f, 62.77152f), adv_med_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2351,
        ProximityTerminal.Constructor(Vector3(6800.092f, 2278.055f, 52.96152f), medical_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2795,
        ProximityTerminal.Constructor(Vector3(6728.126f, 2242.61f, 73.25452f), pad_landing_frame),
        owning_building_guid = 54
      )
      LocalObject(
        2796,
        Terminal.Constructor(Vector3(6728.126f, 2242.61f, 73.25452f), air_rearm_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2798,
        ProximityTerminal.Constructor(Vector3(6738.864f, 2228.33f, 71.31351f), pad_landing_frame),
        owning_building_guid = 54
      )
      LocalObject(
        2799,
        Terminal.Constructor(Vector3(6738.864f, 2228.33f, 71.31351f), air_rearm_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2801,
        ProximityTerminal.Constructor(Vector3(6786.979f, 2342.147f, 71.30352f), pad_landing_frame),
        owning_building_guid = 54
      )
      LocalObject(
        2802,
        Terminal.Constructor(Vector3(6786.979f, 2342.147f, 71.30352f), air_rearm_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2804,
        ProximityTerminal.Constructor(Vector3(6797.096f, 2326.938f, 73.29352f), pad_landing_frame),
        owning_building_guid = 54
      )
      LocalObject(
        2805,
        Terminal.Constructor(Vector3(6797.096f, 2326.938f, 73.29352f), air_rearm_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3192,
        ProximityTerminal.Constructor(Vector3(6701.838f, 2230.991f, 62.71152f), repair_silo),
        owning_building_guid = 54
      )
      LocalObject(
        3193,
        Terminal.Constructor(Vector3(6701.838f, 2230.991f, 62.71152f), ground_rearm_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        3196,
        ProximityTerminal.Constructor(Vector3(6827.633f, 2276.406f, 62.71152f), repair_silo),
        owning_building_guid = 54
      )
      LocalObject(
        3197,
        Terminal.Constructor(Vector3(6827.633f, 2276.406f, 62.71152f), ground_rearm_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        2283,
        FacilityTurret.Constructor(Vector3(6664.535f, 2238.206f, 71.36352f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(2283, 5070)
      LocalObject(
        2284,
        FacilityTurret.Constructor(Vector3(6750.384f, 2393.099f, 71.36352f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(2284, 5071)
      LocalObject(
        2285,
        FacilityTurret.Constructor(Vector3(6782.802f, 2172.64f, 71.36352f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(2285, 5072)
      LocalObject(
        2286,
        FacilityTurret.Constructor(Vector3(6832.512f, 2348.87f, 71.36352f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(2286, 5073)
      LocalObject(
        2287,
        FacilityTurret.Constructor(Vector3(6849.35f, 2290.247f, 71.36352f), manned_turret),
        owning_building_guid = 54
      )
      TurretToWeapon(2287, 5074)
      LocalObject(
        1232,
        ImplantTerminalMech.Constructor(Vector3(6804.314f, 2321.925f, 52.43852f)),
        owning_building_guid = 54
      )
      LocalObject(
        1222,
        Terminal.Constructor(Vector3(6804.323f, 2321.941f, 52.43852f), implant_terminal_interface),
        owning_building_guid = 54
      )
      TerminalToInterface(1232, 1222)
      LocalObject(
        1233,
        ImplantTerminalMech.Constructor(Vector3(6811.748f, 2335.362f, 52.43852f)),
        owning_building_guid = 54
      )
      LocalObject(
        1223,
        Terminal.Constructor(Vector3(6811.739f, 2335.346f, 52.43852f), implant_terminal_interface),
        owning_building_guid = 54
      )
      TerminalToInterface(1233, 1223)
      LocalObject(
        2893,
        Painbox.Constructor(Vector3(6769.358f, 2268.088f, 76.99032f), painbox),
        owning_building_guid = 54
      )
      LocalObject(
        2910,
        Painbox.Constructor(Vector3(6790.642f, 2287.593f, 57.03142f), painbox_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        2927,
        Painbox.Constructor(Vector3(6775.136f, 2281.423f, 77.19542f), painbox_door_radius),
        owning_building_guid = 54
      )
      LocalObject(
        2972,
        Painbox.Constructor(Vector3(6782.148f, 2280.96f, 55.31742f), painbox_door_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        2973,
        Painbox.Constructor(Vector3(6797.469f, 2304.525f, 54.67572f), painbox_door_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        2974,
        Painbox.Constructor(Vector3(6807.622f, 2291.145f, 56.50242f), painbox_door_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(395, Generator.Constructor(Vector3(6766.101f, 2265.447f, 71.66752f)), owning_building_guid = 54)
      LocalObject(
        378,
        Terminal.Constructor(Vector3(6770.031f, 2272.635f, 72.96152f), gen_control),
        owning_building_guid = 54
      )
    }

    Building25936()

    def Building25936(): Unit = { // Name: GW_Cyssor_N Type: hst GUID: 57, MapID: 25936
      LocalBuilding(
        "GW_Cyssor_N",
        57,
        25936,
        FoundationBuilder(WarpGate.Structure(Vector3(2467.29f, 4261.36f, 52.85f), hst))
      )
    }

    Building25937()

    def Building25937(): Unit = { // Name: GW_Cyssor_S Type: hst GUID: 58, MapID: 25937
      LocalBuilding(
        "GW_Cyssor_S",
        58,
        25937,
        FoundationBuilder(WarpGate.Structure(Vector3(4762.97f, 2644.8f, 68.15f), hst))
      )
    }

    Building7()

    def Building7(): Unit = { // Name: Wele Type: tech_plant GUID: 66, MapID: 7
      LocalBuilding(
        "Wele",
        66,
        7,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(532f, 6966f, 59.89929f), Vector3(0f, 0f, 180f), tech_plant)
        )
      )
      LocalObject(
        277,
        CaptureTerminal.Constructor(Vector3(527.266f, 7010.089f, 74.99929f), capture_terminal),
        owning_building_guid = 66
      )
      LocalObject(398, Door.Constructor(Vector3(452.98f, 6962.893f, 61.44129f)), owning_building_guid = 66)
      LocalObject(399, Door.Constructor(Vector3(452.98f, 6981.086f, 69.40429f)), owning_building_guid = 66)
      LocalObject(400, Door.Constructor(Vector3(489.248f, 6934.734f, 69.40429f)), owning_building_guid = 66)
      LocalObject(401, Door.Constructor(Vector3(507.441f, 6934.734f, 61.44129f)), owning_building_guid = 66)
      LocalObject(404, Door.Constructor(Vector3(514.556f, 7005.426f, 76.52029f)), owning_building_guid = 66)
      LocalObject(406, Door.Constructor(Vector3(521.403f, 6989.425f, 76.52029f)), owning_building_guid = 66)
      LocalObject(407, Door.Constructor(Vector3(527.395f, 7057.198f, 61.55029f)), owning_building_guid = 66)
      LocalObject(408, Door.Constructor(Vector3(545.588f, 7057.198f, 69.51329f)), owning_building_guid = 66)
      LocalObject(411, Door.Constructor(Vector3(572f, 7066f, 61.52029f)), owning_building_guid = 66)
      LocalObject(416, Door.Constructor(Vector3(603.46f, 6944.135f, 69.40429f)), owning_building_guid = 66)
      LocalObject(417, Door.Constructor(Vector3(603.46f, 6962.327f, 61.44129f)), owning_building_guid = 66)
      LocalObject(418, Door.Constructor(Vector3(603.46f, 7017.879f, 69.40429f)), owning_building_guid = 66)
      LocalObject(419, Door.Constructor(Vector3(603.46f, 7036.071f, 61.44129f)), owning_building_guid = 66)
      LocalObject(809, Door.Constructor(Vector3(580f, 6925.998f, 63.63629f)), owning_building_guid = 66)
      LocalObject(813, Door.Constructor(Vector3(580f, 6982f, 43.63629f)), owning_building_guid = 66)
      LocalObject(817, Door.Constructor(Vector3(500f, 6994f, 54.02029f)), owning_building_guid = 66)
      LocalObject(818, Door.Constructor(Vector3(500f, 7010f, 46.52029f)), owning_building_guid = 66)
      LocalObject(820, Door.Constructor(Vector3(504f, 6982f, 46.52029f)), owning_building_guid = 66)
      LocalObject(821, Door.Constructor(Vector3(512f, 6990f, 54.02029f)), owning_building_guid = 66)
      LocalObject(822, Door.Constructor(Vector3(512f, 7014f, 46.52029f)), owning_building_guid = 66)
      LocalObject(823, Door.Constructor(Vector3(512f, 7014f, 54.02029f)), owning_building_guid = 66)
      LocalObject(826, Door.Constructor(Vector3(532f, 6994f, 76.52029f)), owning_building_guid = 66)
      LocalObject(827, Door.Constructor(Vector3(532f, 6998f, 66.52029f)), owning_building_guid = 66)
      LocalObject(828, Door.Constructor(Vector3(532f, 7002f, 46.52029f)), owning_building_guid = 66)
      LocalObject(829, Door.Constructor(Vector3(532f, 7042f, 54.02029f)), owning_building_guid = 66)
      LocalObject(830, Door.Constructor(Vector3(536f, 6990f, 56.52029f)), owning_building_guid = 66)
      LocalObject(831, Door.Constructor(Vector3(536f, 7006f, 56.52029f)), owning_building_guid = 66)
      LocalObject(832, Door.Constructor(Vector3(536f, 7006f, 76.52029f)), owning_building_guid = 66)
      LocalObject(833, Door.Constructor(Vector3(548f, 7018f, 54.02029f)), owning_building_guid = 66)
      LocalObject(834, Door.Constructor(Vector3(548f, 7058f, 54.02029f)), owning_building_guid = 66)
      LocalObject(837, Door.Constructor(Vector3(564f, 6986f, 51.52029f)), owning_building_guid = 66)
      LocalObject(838, Door.Constructor(Vector3(564f, 7010f, 51.52029f)), owning_building_guid = 66)
      LocalObject(845, Door.Constructor(Vector3(596f, 7010f, 51.52029f)), owning_building_guid = 66)
      LocalObject(846, Door.Constructor(Vector3(596f, 7034f, 54.02029f)), owning_building_guid = 66)
      LocalObject(1187, Door.Constructor(Vector3(491.787f, 7011.659f, 62.27929f)), owning_building_guid = 66)
      LocalObject(3497, Door.Constructor(Vector3(519.327f, 6993.685f, 54.35329f)), owning_building_guid = 66)
      LocalObject(3498, Door.Constructor(Vector3(519.327f, 7000.974f, 54.35329f)), owning_building_guid = 66)
      LocalObject(3499, Door.Constructor(Vector3(519.327f, 7008.267f, 54.35329f)), owning_building_guid = 66)
      LocalObject(
        1251,
        IFFLock.Constructor(Vector3(488.643f, 7014.397f, 61.47929f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 1187
      )
      LocalObject(
        1268,
        IFFLock.Constructor(Vector3(574.744f, 6923.647f, 61.58729f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 809
      )
      LocalObject(
        1272,
        IFFLock.Constructor(Vector3(500.94f, 7011.572f, 46.33529f), Vector3(0, 0, 90)),
        owning_building_guid = 66,
        door_guid = 818
      )
      LocalObject(
        1273,
        IFFLock.Constructor(Vector3(502.428f, 6982.943f, 46.33529f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 820
      )
      LocalObject(
        1274,
        IFFLock.Constructor(Vector3(510.428f, 7014.81f, 53.83529f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 823
      )
      LocalObject(
        1276,
        IFFLock.Constructor(Vector3(512.504f, 7006.225f, 76.44529f), Vector3(0, 0, 0)),
        owning_building_guid = 66,
        door_guid = 404
      )
      LocalObject(
        1277,
        IFFLock.Constructor(Vector3(513.572f, 6989.19f, 53.83529f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 821
      )
      LocalObject(
        1278,
        IFFLock.Constructor(Vector3(523.446f, 6988.617f, 76.44529f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 406
      )
      LocalObject(
        1279,
        IFFLock.Constructor(Vector3(537.572f, 7005.06f, 76.33529f), Vector3(0, 0, 180)),
        owning_building_guid = 66,
        door_guid = 832
      )
      LocalObject(
        1280,
        IFFLock.Constructor(Vector3(572.814f, 7068.046f, 61.45129f), Vector3(0, 0, 90)),
        owning_building_guid = 66,
        door_guid = 411
      )
      LocalObject(1589, Locker.Constructor(Vector3(485.272f, 6991.835f, 44.99929f)), owning_building_guid = 66)
      LocalObject(1590, Locker.Constructor(Vector3(486.609f, 6991.835f, 44.99929f)), owning_building_guid = 66)
      LocalObject(1591, Locker.Constructor(Vector3(487.945f, 6991.835f, 44.99929f)), owning_building_guid = 66)
      LocalObject(1592, Locker.Constructor(Vector3(489.269f, 6991.835f, 44.99929f)), owning_building_guid = 66)
      LocalObject(1593, Locker.Constructor(Vector3(493.809f, 6991.835f, 44.99929f)), owning_building_guid = 66)
      LocalObject(1594, Locker.Constructor(Vector3(495.146f, 6991.835f, 44.99929f)), owning_building_guid = 66)
      LocalObject(1595, Locker.Constructor(Vector3(496.482f, 6991.835f, 44.99929f)), owning_building_guid = 66)
      LocalObject(1596, Locker.Constructor(Vector3(497.806f, 6991.835f, 44.99929f)), owning_building_guid = 66)
      LocalObject(1597, Locker.Constructor(Vector3(504.977f, 7011.859f, 52.76029f)), owning_building_guid = 66)
      LocalObject(1598, Locker.Constructor(Vector3(506.126f, 7011.859f, 52.76029f)), owning_building_guid = 66)
      LocalObject(1599, Locker.Constructor(Vector3(507.273f, 7011.859f, 52.76029f)), owning_building_guid = 66)
      LocalObject(1600, Locker.Constructor(Vector3(508.437f, 7011.859f, 52.76029f)), owning_building_guid = 66)
      LocalObject(
        208,
        Terminal.Constructor(Vector3(515.395f, 6990.859f, 75.60229f), air_vehicle_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2302,
        VehicleSpawnPad.Constructor(Vector3(510.912f, 6970.165f, 72.47729f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 66,
        terminal_guid = 208
      )
      LocalObject(
        209,
        Terminal.Constructor(Vector3(527.327f, 6990.859f, 75.60229f), air_vehicle_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2303,
        VehicleSpawnPad.Constructor(Vector3(531.93f, 6970.165f, 72.47729f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 66,
        terminal_guid = 209
      )
      LocalObject(
        2356,
        Terminal.Constructor(Vector3(505.346f, 6999.072f, 54.08929f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2357,
        Terminal.Constructor(Vector3(505.346f, 7002.861f, 54.08929f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2358,
        Terminal.Constructor(Vector3(505.346f, 7006.592f, 54.08929f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2359,
        Terminal.Constructor(Vector3(528.942f, 6992.514f, 66.32928f), order_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3383,
        Terminal.Constructor(Vector3(494.758f, 6986.361f, 66.58129f), spawn_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3384,
        Terminal.Constructor(Vector3(517.468f, 6962.785f, 71.92229f), spawn_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3385,
        Terminal.Constructor(Vector3(519.03f, 6996.177f, 54.63329f), spawn_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3386,
        Terminal.Constructor(Vector3(519.033f, 7003.465f, 54.63329f), spawn_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3387,
        Terminal.Constructor(Vector3(519.029f, 7010.757f, 54.63329f), spawn_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3388,
        Terminal.Constructor(Vector3(524.058f, 7013.409f, 46.55629f), spawn_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3390,
        Terminal.Constructor(Vector3(556.058f, 7061.409f, 54.05629f), spawn_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3717,
        Terminal.Constructor(Vector3(580.004f, 7006.577f, 45.71329f), ground_vehicle_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2305,
        VehicleSpawnPad.Constructor(Vector3(580.055f, 6995.661f, 37.43629f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 66,
        terminal_guid = 3717
      )
      LocalObject(3216, ResourceSilo.Constructor(Vector3(470.248f, 6933.445f, 66.90729f)), owning_building_guid = 66)
      LocalObject(
        3233,
        SpawnTube.Constructor(Vector3(519.767f, 6994.738f, 52.49929f), Vector3(0, 0, 180)),
        owning_building_guid = 66
      )
      LocalObject(
        3234,
        SpawnTube.Constructor(Vector3(519.767f, 7002.026f, 52.49929f), Vector3(0, 0, 180)),
        owning_building_guid = 66
      )
      LocalObject(
        3235,
        SpawnTube.Constructor(Vector3(519.767f, 7009.317f, 52.49929f), Vector3(0, 0, 180)),
        owning_building_guid = 66
      )
      LocalObject(
        2327,
        ProximityTerminal.Constructor(Vector3(491.556f, 6992.38f, 44.99929f), medical_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2328,
        ProximityTerminal.Constructor(Vector3(528.941f, 7003.099f, 64.99629f), medical_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2627,
        ProximityTerminal.Constructor(Vector3(463.72f, 6991.898f, 68.10728f), pad_landing_frame),
        owning_building_guid = 66
      )
      LocalObject(
        2628,
        Terminal.Constructor(Vector3(463.72f, 6991.898f, 68.10728f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2630,
        ProximityTerminal.Constructor(Vector3(470.013f, 7008.145f, 70.55128f), pad_landing_frame),
        owning_building_guid = 66
      )
      LocalObject(
        2631,
        Terminal.Constructor(Vector3(470.013f, 7008.145f, 70.55128f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2633,
        ProximityTerminal.Constructor(Vector3(506.466f, 7047.372f, 68.10728f), pad_landing_frame),
        owning_building_guid = 66
      )
      LocalObject(
        2634,
        Terminal.Constructor(Vector3(506.466f, 7047.372f, 68.10728f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2639,
        ProximityTerminal.Constructor(Vector3(522.621f, 7030.526f, 75.34629f), pad_landing_frame),
        owning_building_guid = 66
      )
      LocalObject(
        2640,
        Terminal.Constructor(Vector3(522.621f, 7030.526f, 75.34629f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2645,
        ProximityTerminal.Constructor(Vector3(573.02f, 6974.167f, 70.44929f), pad_landing_frame),
        owning_building_guid = 66
      )
      LocalObject(
        2646,
        Terminal.Constructor(Vector3(573.02f, 6974.167f, 70.44929f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2648,
        ProximityTerminal.Constructor(Vector3(593.296f, 6990.339f, 68.09428f), pad_landing_frame),
        owning_building_guid = 66
      )
      LocalObject(
        2649,
        Terminal.Constructor(Vector3(593.296f, 6990.339f, 68.09428f), air_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3080,
        ProximityTerminal.Constructor(Vector3(473.3634f, 7058.792f, 59.64929f), repair_silo),
        owning_building_guid = 66
      )
      LocalObject(
        3081,
        Terminal.Constructor(Vector3(473.3634f, 7058.792f, 59.64929f), ground_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        3088,
        ProximityTerminal.Constructor(Vector3(531.6914f, 6924.363f, 59.62779f), repair_silo),
        owning_building_guid = 66
      )
      LocalObject(
        3089,
        Terminal.Constructor(Vector3(531.6914f, 6924.363f, 59.62779f), ground_rearm_terminal),
        owning_building_guid = 66
      )
      LocalObject(
        2159,
        FacilityTurret.Constructor(Vector3(438.119f, 7000.577f, 68.39828f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(2159, 5075)
      LocalObject(
        2160,
        FacilityTurret.Constructor(Vector3(445.846f, 6927.602f, 68.39828f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(2160, 5076)
      LocalObject(
        2161,
        FacilityTurret.Constructor(Vector3(445.846f, 7065.343f, 68.39828f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(2161, 5077)
      LocalObject(
        2164,
        FacilityTurret.Constructor(Vector3(526.399f, 6867.145f, 68.39828f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(2164, 5078)
      LocalObject(
        2167,
        FacilityTurret.Constructor(Vector3(610.587f, 7065.335f, 68.39828f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(2167, 5079)
      LocalObject(
        2168,
        FacilityTurret.Constructor(Vector3(616.094f, 6867.145f, 68.39828f), manned_turret),
        owning_building_guid = 66
      )
      TurretToWeapon(2168, 5080)
      LocalObject(
        2879,
        Painbox.Constructor(Vector3(506.2628f, 6969.793f, 48.47259f), painbox),
        owning_building_guid = 66
      )
      LocalObject(
        2896,
        Painbox.Constructor(Vector3(511.1678f, 7002.788f, 56.76919f), painbox_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2913,
        Painbox.Constructor(Vector3(504.3004f, 6984.529f, 48.15869f), painbox_door_radius),
        owning_building_guid = 66
      )
      LocalObject(
        2930,
        Painbox.Constructor(Vector3(496.359f, 6995.43f, 56.08159f), painbox_door_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2931,
        Painbox.Constructor(Vector3(512.1394f, 6988.231f, 55.17549f), painbox_door_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(
        2932,
        Painbox.Constructor(Vector3(512.9652f, 7015.722f, 54.62719f), painbox_door_radius_continuous),
        owning_building_guid = 66
      )
      LocalObject(381, Generator.Constructor(Vector3(504.025f, 6966.445f, 43.70529f)), owning_building_guid = 66)
      LocalObject(
        364,
        Terminal.Constructor(Vector3(503.978f, 6974.637f, 44.99929f), gen_control),
        owning_building_guid = 66
      )
    }

    Building10()

    def Building10(): Unit = { // Name: Leza Type: tech_plant GUID: 69, MapID: 10
      LocalBuilding(
        "Leza",
        69,
        10,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(2674f, 1440f, 63.98816f),
            Vector3(0f, 0f, 121f),
            tech_plant
          )
        )
      )
      LocalObject(
        282,
        CaptureTerminal.Constructor(Vector3(2709.354f, 1466.765f, 79.08816f), capture_terminal),
        owning_building_guid = 69
      )
      LocalObject(515, Door.Constructor(Vector3(2625.181f, 1460.542f, 73.49316f)), owning_building_guid = 69)
      LocalObject(516, Door.Constructor(Vector3(2630.638f, 1506.133f, 65.53016f)), owning_building_guid = 69)
      LocalObject(517, Door.Constructor(Vector3(2634.551f, 1444.948f, 65.53016f)), owning_building_guid = 69)
      LocalObject(518, Door.Constructor(Vector3(2646.233f, 1515.503f, 73.49316f)), owning_building_guid = 69)
      LocalObject(519, Door.Constructor(Vector3(2688.621f, 1461.148f, 80.60915f)), owning_building_guid = 69)
      LocalObject(520, Door.Constructor(Vector3(2692.063f, 1367.485f, 73.49316f)), owning_building_guid = 69)
      LocalObject(521, Door.Constructor(Vector3(2698.81f, 1475.258f, 80.60915f)), owning_building_guid = 69)
      LocalObject(522, Door.Constructor(Vector3(2707.656f, 1376.855f, 65.53016f)), owning_building_guid = 69)
      LocalObject(523, Door.Constructor(Vector3(2749.8f, 1490.918f, 65.63915f)), owning_building_guid = 69)
      LocalObject(524, Door.Constructor(Vector3(2755.274f, 1405.466f, 73.49316f)), owning_building_guid = 69)
      LocalObject(525, Door.Constructor(Vector3(2759.17f, 1475.323f, 73.60216f)), owning_building_guid = 69)
      LocalObject(526, Door.Constructor(Vector3(2770.867f, 1414.836f, 65.53016f)), owning_building_guid = 69)
      LocalObject(527, Door.Constructor(Vector3(2780.318f, 1457.217f, 65.60915f)), owning_building_guid = 69)
      LocalObject(810, Door.Constructor(Vector3(2664.433f, 1378.253f, 67.72516f)), owning_building_guid = 69)
      LocalObject(814, Door.Constructor(Vector3(2712.437f, 1407.097f, 47.72515f)), owning_building_guid = 69)
      LocalObject(927, Door.Constructor(Vector3(2673.294f, 1472.241f, 50.60915f)), owning_building_guid = 69)
      LocalObject(928, Door.Constructor(Vector3(2681.52f, 1481.85f, 58.10915f)), owning_building_guid = 69)
      LocalObject(929, Door.Constructor(Vector3(2684.271f, 1469.504f, 58.10915f)), owning_building_guid = 69)
      LocalObject(930, Door.Constructor(Vector3(2695.234f, 1490.091f, 50.60915f)), owning_building_guid = 69)
      LocalObject(931, Door.Constructor(Vector3(2696.632f, 1448.932f, 60.60915f)), owning_building_guid = 69)
      LocalObject(932, Door.Constructor(Vector3(2698.001f, 1454.421f, 80.60915f)), owning_building_guid = 69)
      LocalObject(933, Door.Constructor(Vector3(2701.429f, 1456.481f, 70.60915f)), owning_building_guid = 69)
      LocalObject(934, Door.Constructor(Vector3(2704.858f, 1458.541f, 50.60915f)), owning_building_guid = 69)
      LocalObject(935, Door.Constructor(Vector3(2704.843f, 1481.865f, 50.60915f)), owning_building_guid = 69)
      LocalObject(936, Door.Constructor(Vector3(2704.843f, 1481.865f, 58.10915f)), owning_building_guid = 69)
      LocalObject(937, Door.Constructor(Vector3(2707.625f, 1422.871f, 55.60915f)), owning_building_guid = 69)
      LocalObject(938, Door.Constructor(Vector3(2710.347f, 1457.173f, 60.60915f)), owning_building_guid = 69)
      LocalObject(939, Door.Constructor(Vector3(2710.347f, 1457.173f, 80.60915f)), owning_building_guid = 69)
      LocalObject(940, Door.Constructor(Vector3(2726.813f, 1453.067f, 58.10915f)), owning_building_guid = 69)
      LocalObject(941, Door.Constructor(Vector3(2728.197f, 1435.232f, 55.60915f)), owning_building_guid = 69)
      LocalObject(942, Door.Constructor(Vector3(2739.145f, 1479.143f, 58.10915f)), owning_building_guid = 69)
      LocalObject(943, Door.Constructor(Vector3(2744.678f, 1407.803f, 55.60915f)), owning_building_guid = 69)
      LocalObject(944, Door.Constructor(Vector3(2761.1f, 1473.669f, 58.10915f)), owning_building_guid = 69)
      LocalObject(945, Door.Constructor(Vector3(2765.25f, 1420.164f, 58.10915f)), owning_building_guid = 69)
      LocalObject(1192, Door.Constructor(Vector3(2692.426f, 1497.985f, 66.36816f)), owning_building_guid = 69)
      LocalObject(3530, Door.Constructor(Vector3(2691.204f, 1465.122f, 58.44215f)), owning_building_guid = 69)
      LocalObject(3531, Door.Constructor(Vector3(2697.451f, 1468.876f, 58.44215f)), owning_building_guid = 69)
      LocalObject(3532, Door.Constructor(Vector3(2703.703f, 1472.632f, 58.44215f)), owning_building_guid = 69)
      LocalObject(
        1256,
        IFFLock.Constructor(Vector3(2693.154f, 1502.09f, 65.56815f), Vector3(0, 0, 59)),
        owning_building_guid = 69,
        door_guid = 1192
      )
      LocalObject(
        1269,
        IFFLock.Constructor(Vector3(2659.711f, 1381.548f, 65.67616f), Vector3(0, 0, 239)),
        owning_building_guid = 69,
        door_guid = 810
      )
      LocalObject(
        1361,
        IFFLock.Constructor(Vector3(2673.292f, 1474.074f, 50.42416f), Vector3(0, 0, 59)),
        owning_building_guid = 69,
        door_guid = 927
      )
      LocalObject(
        1362,
        IFFLock.Constructor(Vector3(2684.386f, 1467.74f, 57.92416f), Vector3(0, 0, 239)),
        owning_building_guid = 69,
        door_guid = 929
      )
      LocalObject(
        1363,
        IFFLock.Constructor(Vector3(2688.981f, 1458.981f, 80.53416f), Vector3(0, 0, 239)),
        owning_building_guid = 69,
        door_guid = 519
      )
      LocalObject(
        1364,
        IFFLock.Constructor(Vector3(2697.066f, 1490.095f, 50.42416f), Vector3(0, 0, 149)),
        owning_building_guid = 69,
        door_guid = 930
      )
      LocalObject(
        1365,
        IFFLock.Constructor(Vector3(2698.438f, 1477.429f, 80.53416f), Vector3(0, 0, 59)),
        owning_building_guid = 69,
        door_guid = 521
      )
      LocalObject(
        1366,
        IFFLock.Constructor(Vector3(2704.728f, 1483.63f, 57.92416f), Vector3(0, 0, 59)),
        owning_building_guid = 69,
        door_guid = 936
      )
      LocalObject(
        1367,
        IFFLock.Constructor(Vector3(2710.351f, 1455.341f, 80.42416f), Vector3(0, 0, 239)),
        owning_building_guid = 69,
        door_guid = 939
      )
      LocalObject(
        1368,
        IFFLock.Constructor(Vector3(2782.491f, 1457.573f, 65.54015f), Vector3(0, 0, 149)),
        owning_building_guid = 69,
        door_guid = 527
      )
      LocalObject(1730, Locker.Constructor(Vector3(2672.078f, 1493.36f, 49.08816f)), owning_building_guid = 69)
      LocalObject(1731, Locker.Constructor(Vector3(2672.767f, 1492.214f, 49.08816f)), owning_building_guid = 69)
      LocalObject(1732, Locker.Constructor(Vector3(2673.455f, 1491.068f, 49.08816f)), owning_building_guid = 69)
      LocalObject(1733, Locker.Constructor(Vector3(2674.137f, 1489.934f, 49.08816f)), owning_building_guid = 69)
      LocalObject(1734, Locker.Constructor(Vector3(2676.475f, 1486.042f, 49.08816f)), owning_building_guid = 69)
      LocalObject(1735, Locker.Constructor(Vector3(2677.164f, 1484.896f, 49.08816f)), owning_building_guid = 69)
      LocalObject(1736, Locker.Constructor(Vector3(2677.852f, 1483.751f, 49.08816f)), owning_building_guid = 69)
      LocalObject(1737, Locker.Constructor(Vector3(2678.534f, 1482.616f, 49.08816f)), owning_building_guid = 69)
      LocalObject(1738, Locker.Constructor(Vector3(2699.391f, 1486.782f, 56.84916f)), owning_building_guid = 69)
      LocalObject(1739, Locker.Constructor(Vector3(2699.983f, 1485.797f, 56.84916f)), owning_building_guid = 69)
      LocalObject(1740, Locker.Constructor(Vector3(2700.573f, 1484.814f, 56.84916f)), owning_building_guid = 69)
      LocalObject(1741, Locker.Constructor(Vector3(2701.173f, 1483.817f, 56.84916f)), owning_building_guid = 69)
      LocalObject(
        210,
        Terminal.Constructor(Vector3(2686.756f, 1467.037f, 79.69115f), air_vehicle_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2309,
        VehicleSpawnPad.Constructor(Vector3(2666.709f, 1460.221f, 76.56615f), mb_pad_creation, Vector3(0, 0, 239)),
        owning_building_guid = 69,
        terminal_guid = 210
      )
      LocalObject(
        211,
        Terminal.Constructor(Vector3(2692.902f, 1456.809f, 79.69115f), air_vehicle_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2310,
        VehicleSpawnPad.Constructor(Vector3(2677.534f, 1442.205f, 76.56615f), mb_pad_creation, Vector3(0, 0, 239)),
        owning_building_guid = 69,
        terminal_guid = 211
      )
      LocalObject(
        2410,
        Terminal.Constructor(Vector3(2688.62f, 1479.88f, 58.17815f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2411,
        Terminal.Constructor(Vector3(2691.868f, 1481.832f, 58.17815f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2412,
        Terminal.Constructor(Vector3(2695.066f, 1483.753f, 58.17815f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2413,
        Terminal.Constructor(Vector3(2695.152f, 1456.277f, 70.41815f), order_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3415,
        Terminal.Constructor(Vector3(2663.76f, 1450.801f, 76.01115f), spawn_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3416,
        Terminal.Constructor(Vector3(2672.272f, 1482.409f, 70.67016f), spawn_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3417,
        Terminal.Constructor(Vector3(2693.187f, 1466.66f, 58.72216f), spawn_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3418,
        Terminal.Constructor(Vector3(2699.435f, 1470.411f, 58.72216f), spawn_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3419,
        Terminal.Constructor(Vector3(2705.684f, 1474.17f, 58.72216f), spawn_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3420,
        Terminal.Constructor(Vector3(2710.547f, 1471.225f, 50.64516f), spawn_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3421,
        Terminal.Constructor(Vector3(2768.172f, 1468.518f, 58.14516f), spawn_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3721,
        Terminal.Constructor(Vector3(2733.505f, 1419.751f, 49.80215f), ground_vehicle_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2311,
        VehicleSpawnPad.Constructor(Vector3(2724.175f, 1414.085f, 41.52515f), mb_pad_creation, Vector3(0, 0, 239)),
        owning_building_guid = 69,
        terminal_guid = 3721
      )
      LocalObject(3221, ResourceSilo.Constructor(Vector3(2614.29f, 1476.165f, 70.99615f)), owning_building_guid = 69)
      LocalObject(
        3266,
        SpawnTube.Constructor(Vector3(2692.333f, 1465.287f, 56.58815f), Vector3(0, 0, 239)),
        owning_building_guid = 69
      )
      LocalObject(
        3267,
        SpawnTube.Constructor(Vector3(2698.58f, 1469.041f, 56.58815f), Vector3(0, 0, 239)),
        owning_building_guid = 69
      )
      LocalObject(
        3268,
        SpawnTube.Constructor(Vector3(2704.829f, 1472.796f, 56.58815f), Vector3(0, 0, 239)),
        owning_building_guid = 69
      )
      LocalObject(
        2336,
        ProximityTerminal.Constructor(Vector3(2675.782f, 1488.254f, 49.08816f), medical_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2337,
        ProximityTerminal.Constructor(Vector3(2704.225f, 1461.729f, 69.08516f), medical_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2684,
        ProximityTerminal.Constructor(Vector3(2661.032f, 1511.866f, 72.19615f), pad_landing_frame),
        owning_building_guid = 69
      )
      LocalObject(
        2685,
        Terminal.Constructor(Vector3(2661.032f, 1511.866f, 72.19615f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2687,
        ProximityTerminal.Constructor(Vector3(2678.2f, 1514.839f, 74.64015f), pad_landing_frame),
        owning_building_guid = 69
      )
      LocalObject(
        2688,
        Terminal.Constructor(Vector3(2678.2f, 1514.839f, 74.64015f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2690,
        ProximityTerminal.Constructor(Vector3(2702.127f, 1409.045f, 74.53815f), pad_landing_frame),
        owning_building_guid = 69
      )
      LocalObject(
        2691,
        Terminal.Constructor(Vector3(2702.127f, 1409.045f, 74.53815f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2693,
        ProximityTerminal.Constructor(Vector3(2724.479f, 1481.273f, 79.43516f), pad_landing_frame),
        owning_building_guid = 69
      )
      LocalObject(
        2694,
        Terminal.Constructor(Vector3(2724.479f, 1481.273f, 79.43516f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2696,
        ProximityTerminal.Constructor(Vector3(2726.432f, 1399.995f, 72.18315f), pad_landing_frame),
        owning_building_guid = 69
      )
      LocalObject(
        2697,
        Terminal.Constructor(Vector3(2726.432f, 1399.995f, 72.18315f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2699,
        ProximityTerminal.Constructor(Vector3(2730.598f, 1503.797f, 72.19615f), pad_landing_frame),
        owning_building_guid = 69
      )
      LocalObject(
        2700,
        Terminal.Constructor(Vector3(2730.598f, 1503.797f, 72.19615f), air_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3120,
        ProximityTerminal.Constructor(Vector3(2638.152f, 1418.82f, 63.71666f), repair_silo),
        owning_building_guid = 69
      )
      LocalObject(
        3121,
        Terminal.Constructor(Vector3(2638.152f, 1418.82f, 63.71666f), ground_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        3124,
        ProximityTerminal.Constructor(Vector3(2723.339f, 1538.053f, 63.73816f), repair_silo),
        owning_building_guid = 69
      )
      LocalObject(
        3125,
        Terminal.Constructor(Vector3(2723.339f, 1538.053f, 63.73816f), ground_rearm_terminal),
        owning_building_guid = 69
      )
      LocalObject(
        2202,
        FacilityTurret.Constructor(Vector3(2586.38f, 1393.887f, 72.48715f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(2202, 5081)
      LocalObject(
        2203,
        FacilityTurret.Constructor(Vector3(2596.714f, 1494.072f, 72.48715f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(2203, 5082)
      LocalObject(
        2204,
        FacilityTurret.Constructor(Vector3(2632.576f, 1317.003f, 72.48715f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(2204, 5083)
      LocalObject(
        2205,
        FacilityTurret.Constructor(Vector3(2655.286f, 1538.28f, 72.48715f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(2205, 5084)
      LocalObject(
        2206,
        FacilityTurret.Constructor(Vector3(2714.781f, 1565.014f, 72.48715f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(2206, 5085)
      LocalObject(
        2207,
        FacilityTurret.Constructor(Vector3(2799.622f, 1423.799f, 72.48715f), manned_turret),
        owning_building_guid = 69
      )
      TurretToWeapon(2207, 5086)
      LocalObject(
        2884,
        Painbox.Constructor(Vector3(2663.996f, 1464.015f, 52.56145f), painbox),
        owning_building_guid = 69
      )
      LocalObject(
        2901,
        Painbox.Constructor(Vector3(2694.804f, 1476.804f, 60.85806f), painbox_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2918,
        Painbox.Constructor(Vector3(2675.616f, 1473.286f, 52.24755f), painbox_door_radius),
        owning_building_guid = 69
      )
      LocalObject(
        2945,
        Painbox.Constructor(Vector3(2680.87f, 1485.708f, 60.17046f), painbox_door_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2946,
        Painbox.Constructor(Vector3(2682.827f, 1468.474f, 59.26435f), painbox_door_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(
        2947,
        Painbox.Constructor(Vector3(2706.816f, 1481.925f, 58.71606f), painbox_door_radius_continuous),
        owning_building_guid = 69
      )
      LocalObject(386, Generator.Constructor(Vector3(2659.973f, 1464.208f, 47.79416f)), owning_building_guid = 69)
      LocalObject(
        369,
        Terminal.Constructor(Vector3(2666.971f, 1468.468f, 49.08816f), gen_control),
        owning_building_guid = 69
      )
    }

    Building6()

    def Building6(): Unit = { // Name: Faro Type: tech_plant GUID: 72, MapID: 6
      LocalBuilding(
        "Faro",
        72,
        6,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5110f, 5790f, 56.74856f),
            Vector3(0f, 0f, 267f),
            tech_plant
          )
        )
      )
      LocalObject(
        287,
        CaptureTerminal.Constructor(Vector3(5065.724f, 5787.58f, 71.84856f), capture_terminal),
        owning_building_guid = 72
      )
      LocalObject(639, Door.Constructor(Vector3(5012.23f, 5835.179f, 58.36956f)), owning_building_guid = 72)
      LocalObject(641, Door.Constructor(Vector3(5018.686f, 5790.174f, 58.39956f)), owning_building_guid = 72)
      LocalObject(642, Door.Constructor(Vector3(5019.638f, 5808.342f, 66.36256f)), owning_building_guid = 72)
      LocalObject(647, Door.Constructor(Vector3(5043.765f, 5865.029f, 58.29056f)), owning_building_guid = 72)
      LocalObject(649, Door.Constructor(Vector3(5061.932f, 5864.077f, 66.25356f)), owning_building_guid = 72)
      LocalObject(650, Door.Constructor(Vector3(5069.715f, 5774.643f, 73.36956f)), owning_building_guid = 72)
      LocalObject(653, Door.Constructor(Vector3(5086.053f, 5780.644f, 73.36956f)), owning_building_guid = 72)
      LocalObject(655, Door.Constructor(Vector3(5090.799f, 5711.878f, 66.25356f)), owning_building_guid = 72)
      LocalObject(657, Door.Constructor(Vector3(5108.967f, 5710.926f, 58.29056f)), owning_building_guid = 72)
      LocalObject(659, Door.Constructor(Vector3(5117.408f, 5861.17f, 58.29056f)), owning_building_guid = 72)
      LocalObject(661, Door.Constructor(Vector3(5135.575f, 5860.218f, 66.25356f)), owning_building_guid = 72)
      LocalObject(664, Door.Constructor(Vector3(5138.986f, 5745.67f, 66.25356f)), owning_building_guid = 72)
      LocalObject(665, Door.Constructor(Vector3(5139.938f, 5763.838f, 58.29056f)), owning_building_guid = 72)
      LocalObject(811, Door.Constructor(Vector3(5152.459f, 5835.841f, 60.48556f)), owning_building_guid = 72)
      LocalObject(815, Door.Constructor(Vector3(5096.534f, 5838.771f, 40.48556f)), owning_building_guid = 72)
      LocalObject(1034, Door.Constructor(Vector3(5018.963f, 5810.793f, 50.86956f)), owning_building_guid = 72)
      LocalObject(1038, Door.Constructor(Vector3(5034.104f, 5793.978f, 50.86956f)), owning_building_guid = 72)
      LocalObject(1039, Door.Constructor(Vector3(5045.443f, 5857.471f, 50.86956f)), owning_building_guid = 72)
      LocalObject(1040, Door.Constructor(Vector3(5058.909f, 5808.7f, 50.86956f)), owning_building_guid = 72)
      LocalObject(1041, Door.Constructor(Vector3(5061.019f, 5772.54f, 43.36956f)), owning_building_guid = 72)
      LocalObject(1042, Door.Constructor(Vector3(5061.019f, 5772.54f, 50.86956f)), owning_building_guid = 72)
      LocalObject(1043, Door.Constructor(Vector3(5064.386f, 5760.347f, 43.36956f)), owning_building_guid = 72)
      LocalObject(1044, Door.Constructor(Vector3(5067.735f, 5824.259f, 48.36956f)), owning_building_guid = 72)
      LocalObject(1045, Door.Constructor(Vector3(5069.41f, 5856.215f, 48.36956f)), owning_building_guid = 72)
      LocalObject(1046, Door.Constructor(Vector3(5070.264f, 5796.088f, 53.36956f)), owning_building_guid = 72)
      LocalObject(1047, Door.Constructor(Vector3(5070.264f, 5796.088f, 73.36956f)), owning_building_guid = 72)
      LocalObject(1048, Door.Constructor(Vector3(5074.049f, 5791.884f, 43.36956f)), owning_building_guid = 72)
      LocalObject(1049, Door.Constructor(Vector3(5078.044f, 5791.675f, 63.36956f)), owning_building_guid = 72)
      LocalObject(1050, Door.Constructor(Vector3(5080.364f, 5759.509f, 50.86956f)), owning_building_guid = 72)
      LocalObject(1051, Door.Constructor(Vector3(5082.039f, 5791.465f, 73.36956f)), owning_building_guid = 72)
      LocalObject(1053, Door.Constructor(Vector3(5084.986f, 5771.284f, 50.86956f)), owning_building_guid = 72)
      LocalObject(1054, Door.Constructor(Vector3(5086.242f, 5795.25f, 53.36956f)), owning_building_guid = 72)
      LocalObject(1055, Door.Constructor(Vector3(5091.702f, 5823.003f, 48.36956f)), owning_building_guid = 72)
      LocalObject(1057, Door.Constructor(Vector3(5092.557f, 5762.876f, 43.36956f)), owning_building_guid = 72)
      LocalObject(1197, Door.Constructor(Vector3(5062.299f, 5752.232f, 59.12856f)), owning_building_guid = 72)
      LocalObject(3569, Door.Constructor(Vector3(5067.127f, 5779.557f, 51.20256f)), owning_building_guid = 72)
      LocalObject(3570, Door.Constructor(Vector3(5074.411f, 5779.175f, 51.20256f)), owning_building_guid = 72)
      LocalObject(3571, Door.Constructor(Vector3(5081.689f, 5778.793f, 51.20256f)), owning_building_guid = 72)
      LocalObject(
        1261,
        IFFLock.Constructor(Vector3(5059.4f, 5749.235f, 58.32856f), Vector3(0, 0, 273)),
        owning_building_guid = 72,
        door_guid = 1197
      )
      LocalObject(
        1270,
        IFFLock.Constructor(Vector3(5154.532f, 5830.469f, 58.43656f), Vector3(0, 0, 93)),
        owning_building_guid = 72,
        door_guid = 811
      )
      LocalObject(
        1464,
        IFFLock.Constructor(Vector3(5010.23f, 5836.099f, 58.30056f), Vector3(0, 0, 3)),
        owning_building_guid = 72,
        door_guid = 639
      )
      LocalObject(
        1467,
        IFFLock.Constructor(Vector3(5060.128f, 5771.012f, 50.68456f), Vector3(0, 0, 273)),
        owning_building_guid = 72,
        door_guid = 1042
      )
      LocalObject(
        1468,
        IFFLock.Constructor(Vector3(5062.865f, 5761.368f, 43.18456f), Vector3(0, 0, 3)),
        owning_building_guid = 72,
        door_guid = 1043
      )
      LocalObject(
        1469,
        IFFLock.Constructor(Vector3(5068.81f, 5772.636f, 73.29456f), Vector3(0, 0, 273)),
        owning_building_guid = 72,
        door_guid = 650
      )
      LocalObject(
        1470,
        IFFLock.Constructor(Vector3(5071.285f, 5797.608f, 73.18456f), Vector3(0, 0, 93)),
        owning_building_guid = 72,
        door_guid = 1047
      )
      LocalObject(
        1471,
        IFFLock.Constructor(Vector3(5085.877f, 5772.811f, 50.68456f), Vector3(0, 0, 93)),
        owning_building_guid = 72,
        door_guid = 1053
      )
      LocalObject(
        1472,
        IFFLock.Constructor(Vector3(5086.966f, 5782.642f, 73.29456f), Vector3(0, 0, 93)),
        owning_building_guid = 72,
        door_guid = 653
      )
      LocalObject(
        1473,
        IFFLock.Constructor(Vector3(5091.533f, 5761.355f, 43.18456f), Vector3(0, 0, 273)),
        owning_building_guid = 72,
        door_guid = 1057
      )
      LocalObject(1904, Locker.Constructor(Vector3(5062.79f, 5765.414f, 49.60956f)), owning_building_guid = 72)
      LocalObject(1905, Locker.Constructor(Vector3(5062.85f, 5766.562f, 49.60956f)), owning_building_guid = 72)
      LocalObject(1906, Locker.Constructor(Vector3(5062.91f, 5767.707f, 49.60956f)), owning_building_guid = 72)
      LocalObject(1907, Locker.Constructor(Vector3(5062.971f, 5768.869f, 49.60956f)), owning_building_guid = 72)
      LocalObject(1908, Locker.Constructor(Vector3(5081.755f, 5744.688f, 41.84856f)), owning_building_guid = 72)
      LocalObject(1909, Locker.Constructor(Vector3(5081.825f, 5746.023f, 41.84856f)), owning_building_guid = 72)
      LocalObject(1910, Locker.Constructor(Vector3(5081.895f, 5747.357f, 41.84856f)), owning_building_guid = 72)
      LocalObject(1911, Locker.Constructor(Vector3(5081.964f, 5748.68f, 41.84856f)), owning_building_guid = 72)
      LocalObject(1912, Locker.Constructor(Vector3(5082.202f, 5753.213f, 41.84856f)), owning_building_guid = 72)
      LocalObject(1913, Locker.Constructor(Vector3(5082.271f, 5754.549f, 41.84856f)), owning_building_guid = 72)
      LocalObject(1914, Locker.Constructor(Vector3(5082.341f, 5755.883f, 41.84856f)), owning_building_guid = 72)
      LocalObject(1915, Locker.Constructor(Vector3(5082.411f, 5757.205f, 41.84856f)), owning_building_guid = 72)
      LocalObject(
        212,
        Terminal.Constructor(Vector3(5084.306f, 5774.719f, 72.45156f), air_vehicle_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2317,
        VehicleSpawnPad.Constructor(Vector3(5104.737f, 5769.159f, 69.32656f), mb_pad_creation, Vector3(0, 0, 93)),
        owning_building_guid = 72,
        terminal_guid = 212
      )
      LocalObject(
        213,
        Terminal.Constructor(Vector3(5084.931f, 5786.634f, 72.45156f), air_vehicle_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2318,
        VehicleSpawnPad.Constructor(Vector3(5105.837f, 5790.148f, 69.32656f), mb_pad_creation, Vector3(0, 0, 93)),
        owning_building_guid = 72,
        terminal_guid = 213
      )
      LocalObject(
        2473,
        Terminal.Constructor(Vector3(5068.069f, 5765.507f, 50.93856f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2474,
        Terminal.Constructor(Vector3(5071.794f, 5765.312f, 50.93856f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2475,
        Terminal.Constructor(Vector3(5075.578f, 5765.113f, 50.93856f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2476,
        Terminal.Constructor(Vector3(5083.362f, 5788.334f, 63.17856f), order_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3449,
        Terminal.Constructor(Vector3(5015.981f, 5819.019f, 50.90556f), spawn_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3452,
        Terminal.Constructor(Vector3(5062.24f, 5784.55f, 43.40556f), spawn_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3453,
        Terminal.Constructor(Vector3(5064.625f, 5779.389f, 51.48256f), spawn_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3454,
        Terminal.Constructor(Vector3(5071.908f, 5779.012f, 51.48256f), spawn_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3455,
        Terminal.Constructor(Vector3(5079.186f, 5778.627f, 51.48256f), spawn_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3456,
        Terminal.Constructor(Vector3(5087.718f, 5753.875f, 63.43056f), spawn_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3458,
        Terminal.Constructor(Vector3(5112.45f, 5775.32f, 68.77156f), spawn_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3726,
        Terminal.Constructor(Vector3(5071.991f, 5840.062f, 42.56256f), ground_vehicle_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2316,
        VehicleSpawnPad.Constructor(Vector3(5082.895f, 5839.542f, 34.28556f), mb_pad_creation, Vector3(0, 0, 93)),
        owning_building_guid = 72,
        terminal_guid = 3726
      )
      LocalObject(3227, ResourceSilo.Constructor(Vector3(5139.278f, 5726.629f, 63.75656f)), owning_building_guid = 72)
      LocalObject(
        3305,
        SpawnTube.Constructor(Vector3(5066.102f, 5780.051f, 49.34856f), Vector3(0, 0, 93)),
        owning_building_guid = 72
      )
      LocalObject(
        3306,
        SpawnTube.Constructor(Vector3(5073.383f, 5779.669f, 49.34856f), Vector3(0, 0, 93)),
        owning_building_guid = 72
      )
      LocalObject(
        3307,
        SpawnTube.Constructor(Vector3(5080.661f, 5779.288f, 49.34856f), Vector3(0, 0, 93)),
        owning_building_guid = 72
      )
      LocalObject(
        2344,
        ProximityTerminal.Constructor(Vector3(5072.792f, 5788.887f, 61.84556f), medical_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2345,
        ProximityTerminal.Constructor(Vector3(5081.54f, 5750.992f, 41.84856f), medical_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2741,
        ProximityTerminal.Constructor(Vector3(5027.403f, 5768.76f, 64.95656f), pad_landing_frame),
        owning_building_guid = 72
      )
      LocalObject(
        2742,
        Terminal.Constructor(Vector3(5027.403f, 5768.76f, 64.95656f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2744,
        ProximityTerminal.Constructor(Vector3(5045.072f, 5784.011f, 72.19556f), pad_landing_frame),
        owning_building_guid = 72
      )
      LocalObject(
        2745,
        Terminal.Constructor(Vector3(5045.072f, 5784.011f, 72.19556f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2747,
        ProximityTerminal.Constructor(Vector3(5064.668f, 5730.304f, 67.40056f), pad_landing_frame),
        owning_building_guid = 72
      )
      LocalObject(
        2748,
        Terminal.Constructor(Vector3(5064.668f, 5730.304f, 67.40056f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2750,
        ProximityTerminal.Constructor(Vector3(5080.564f, 5723.169f, 64.95656f), pad_landing_frame),
        owning_building_guid = 72
      )
      LocalObject(
        2751,
        Terminal.Constructor(Vector3(5080.564f, 5723.169f, 64.95656f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2753,
        ProximityTerminal.Constructor(Vector3(5088.902f, 5852.486f, 64.94356f), pad_landing_frame),
        owning_building_guid = 72
      )
      LocalObject(
        2754,
        Terminal.Constructor(Vector3(5088.902f, 5852.486f, 64.94356f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2756,
        ProximityTerminal.Constructor(Vector3(5103.991f, 5831.391f, 67.29856f), pad_landing_frame),
        owning_building_guid = 72
      )
      LocalObject(
        2757,
        Terminal.Constructor(Vector3(5103.991f, 5831.391f, 67.29856f), air_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3156,
        ProximityTerminal.Constructor(Vector3(5014.266f, 5736.3f, 56.49856f), repair_silo),
        owning_building_guid = 72
      )
      LocalObject(
        3157,
        Terminal.Constructor(Vector3(5014.266f, 5736.3f, 56.49856f), ground_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        3168,
        ProximityTerminal.Constructor(Vector3(5151.563f, 5787.513f, 56.47706f), repair_silo),
        owning_building_guid = 72
      )
      LocalObject(
        3169,
        Terminal.Constructor(Vector3(5151.563f, 5787.513f, 56.47706f), ground_rearm_terminal),
        owning_building_guid = 72
      )
      LocalObject(
        2245,
        FacilityTurret.Constructor(Vector3(5006.284f, 5709.163f, 65.24756f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(2245, 5087)
      LocalObject(
        2247,
        FacilityTurret.Constructor(Vector3(5014.914f, 5873.678f, 65.24756f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(2247, 5088)
      LocalObject(
        2252,
        FacilityTurret.Constructor(Vector3(5070.557f, 5698.057f, 65.24756f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(2252, 5089)
      LocalObject(
        2253,
        FacilityTurret.Constructor(Vector3(5143.836f, 5701.955f, 65.24756f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(2253, 5090)
      LocalObject(
        2257,
        FacilityTurret.Constructor(Vector3(5208.426f, 5779.233f, 65.24756f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(2257, 5091)
      LocalObject(
        2258,
        FacilityTurret.Constructor(Vector3(5213.121f, 5868.805f, 65.24756f), manned_turret),
        owning_building_guid = 72
      )
      TurretToWeapon(2258, 5092)
      LocalObject(
        2889,
        Painbox.Constructor(Vector3(5104.865f, 5764.497f, 45.32186f), painbox),
        owning_building_guid = 72
      )
      LocalObject(
        2906,
        Painbox.Constructor(Vector3(5072.172f, 5771.122f, 53.61846f), painbox_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2923,
        Painbox.Constructor(Vector3(5090.047f, 5763.308f, 45.00796f), painbox_door_radius),
        owning_building_guid = 72
      )
      LocalObject(
        2960,
        Painbox.Constructor(Vector3(5059.35f, 5773.594f, 51.47646f), painbox_door_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2961,
        Painbox.Constructor(Vector3(5078.745f, 5755.948f, 52.93086f), painbox_door_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(
        2962,
        Painbox.Constructor(Vector3(5086.76f, 5771.33f, 52.02476f), painbox_door_radius_continuous),
        owning_building_guid = 72
      )
      LocalObject(391, Generator.Constructor(Vector3(5108.091f, 5762.086f, 40.55456f)), owning_building_guid = 72)
      LocalObject(
        374,
        Terminal.Constructor(Vector3(5099.908f, 5762.468f, 41.84856f), gen_control),
        owning_building_guid = 72
      )
    }

    Building14()

    def Building14(): Unit = { // Name: Orisha Type: tech_plant GUID: 75, MapID: 14
      LocalBuilding(
        "Orisha",
        75,
        14,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(6976f, 1240f, 56.97867f), Vector3(0f, 0f, 84f), tech_plant)
        )
      )
      LocalObject(
        292,
        CaptureTerminal.Constructor(Vector3(7020.342f, 1240.099f, 72.07867f), capture_terminal),
        owning_building_guid = 75
      )
      LocalObject(767, Door.Constructor(Vector3(6946.785f, 1171.217f, 66.48367f)), owning_building_guid = 75)
      LocalObject(768, Door.Constructor(Vector3(6947.472f, 1267.693f, 58.52067f)), owning_building_guid = 75)
      LocalObject(769, Door.Constructor(Vector3(6949.374f, 1285.786f, 66.48367f)), owning_building_guid = 75)
      LocalObject(770, Door.Constructor(Vector3(6964.877f, 1169.315f, 58.52067f)), owning_building_guid = 75)
      LocalObject(771, Door.Constructor(Vector3(6981.17f, 1318.912f, 58.52067f)), owning_building_guid = 75)
      LocalObject(772, Door.Constructor(Vector3(6999.263f, 1317.01f, 66.48367f)), owning_building_guid = 75)
      LocalObject(773, Door.Constructor(Vector3(7000.404f, 1248.09f, 73.59967f)), owning_building_guid = 75)
      LocalObject(774, Door.Constructor(Vector3(7017.033f, 1253.227f, 73.59967f)), owning_building_guid = 75)
      LocalObject(775, Door.Constructor(Vector3(7020.125f, 1163.509f, 66.48367f)), owning_building_guid = 75)
      LocalObject(776, Door.Constructor(Vector3(7038.218f, 1161.607f, 58.52067f)), owning_building_guid = 75)
      LocalObject(783, Door.Constructor(Vector3(7065.278f, 1216.954f, 66.59267f)), owning_building_guid = 75)
      LocalObject(784, Door.Constructor(Vector3(7067.18f, 1235.047f, 58.62967f)), owning_building_guid = 75)
      LocalObject(785, Door.Constructor(Vector3(7071.271f, 1189.766f, 58.59967f)), owning_building_guid = 75)
      LocalObject(812, Door.Constructor(Vector3(6931.2f, 1196.444f, 60.71567f)), owning_building_guid = 75)
      LocalObject(816, Door.Constructor(Vector3(6986.895f, 1190.59f, 40.71567f)), owning_building_guid = 75)
      LocalObject(1146, Door.Constructor(Vector3(6992.545f, 1206.085f, 48.59967f)), owning_building_guid = 75)
      LocalObject(1147, Door.Constructor(Vector3(6994.839f, 1266.174f, 43.59967f)), owning_building_guid = 75)
      LocalObject(1148, Door.Constructor(Vector3(6999.45f, 1233.513f, 53.59967f)), owning_building_guid = 75)
      LocalObject(1149, Door.Constructor(Vector3(7001.959f, 1257.382f, 51.09967f)), owning_building_guid = 75)
      LocalObject(1150, Door.Constructor(Vector3(7003.847f, 1237.073f, 73.59967f)), owning_building_guid = 75)
      LocalObject(1151, Door.Constructor(Vector3(7007.191f, 1268.898f, 51.09967f)), owning_building_guid = 75)
      LocalObject(1152, Door.Constructor(Vector3(7007.825f, 1236.655f, 63.59967f)), owning_building_guid = 75)
      LocalObject(1153, Door.Constructor(Vector3(7011.803f, 1236.237f, 43.59967f)), owning_building_guid = 75)
      LocalObject(1154, Door.Constructor(Vector3(7013.069f, 1171.751f, 48.59967f)), owning_building_guid = 75)
      LocalObject(1155, Door.Constructor(Vector3(7015.363f, 1231.841f, 53.59967f)), owning_building_guid = 75)
      LocalObject(1156, Door.Constructor(Vector3(7015.363f, 1231.841f, 73.59967f)), owning_building_guid = 75)
      LocalObject(1157, Door.Constructor(Vector3(7016.414f, 1203.576f, 48.59967f)), owning_building_guid = 75)
      LocalObject(1158, Door.Constructor(Vector3(7023.104f, 1267.225f, 43.59967f)), owning_building_guid = 75)
      LocalObject(1159, Door.Constructor(Vector3(7025.828f, 1254.873f, 43.59967f)), owning_building_guid = 75)
      LocalObject(1160, Door.Constructor(Vector3(7025.828f, 1254.873f, 51.09967f)), owning_building_guid = 75)
      LocalObject(1161, Door.Constructor(Vector3(7026.042f, 1218.652f, 51.09967f)), owning_building_guid = 75)
      LocalObject(1162, Door.Constructor(Vector3(7036.938f, 1169.243f, 51.09967f)), owning_building_guid = 75)
      LocalObject(1163, Door.Constructor(Vector3(7051.583f, 1232.056f, 51.09967f)), owning_building_guid = 75)
      LocalObject(1164, Door.Constructor(Vector3(7065.824f, 1214.471f, 51.09967f)), owning_building_guid = 75)
      LocalObject(1202, Door.Constructor(Vector3(7025.612f, 1275.22f, 59.35867f)), owning_building_guid = 75)
      LocalObject(3604, Door.Constructor(Vector3(7004.858f, 1249.71f, 51.43267f)), owning_building_guid = 75)
      LocalObject(3605, Door.Constructor(Vector3(7012.107f, 1248.948f, 51.43267f)), owning_building_guid = 75)
      LocalObject(3606, Door.Constructor(Vector3(7019.36f, 1248.185f, 51.43267f)), owning_building_guid = 75)
      LocalObject(
        1266,
        IFFLock.Constructor(Vector3(7028.664f, 1278.061f, 58.55867f), Vector3(0, 0, 96)),
        owning_building_guid = 75,
        door_guid = 1202
      )
      LocalObject(
        1271,
        IFFLock.Constructor(Vector3(6929.411f, 1201.917f, 58.66667f), Vector3(0, 0, 276)),
        owning_building_guid = 75,
        door_guid = 812
      )
      LocalObject(
        1558,
        IFFLock.Constructor(Vector3(6995.941f, 1267.639f, 43.41467f), Vector3(0, 0, 96)),
        owning_building_guid = 75,
        door_guid = 1147
      )
      LocalObject(
        1559,
        IFFLock.Constructor(Vector3(6999.387f, 1246.143f, 73.52467f), Vector3(0, 0, 276)),
        owning_building_guid = 75,
        door_guid = 773
      )
      LocalObject(
        1560,
        IFFLock.Constructor(Vector3(7000.989f, 1255.903f, 50.91467f), Vector3(0, 0, 276)),
        owning_building_guid = 75,
        door_guid = 1149
      )
      LocalObject(
        1561,
        IFFLock.Constructor(Vector3(7014.264f, 1230.376f, 73.41467f), Vector3(0, 0, 276)),
        owning_building_guid = 75,
        door_guid = 1156
      )
      LocalObject(
        1562,
        IFFLock.Constructor(Vector3(7018.042f, 1255.185f, 73.52467f), Vector3(0, 0, 96)),
        owning_building_guid = 75,
        door_guid = 774
      )
      LocalObject(
        1563,
        IFFLock.Constructor(Vector3(7024.569f, 1266.126f, 43.41467f), Vector3(0, 0, 186)),
        owning_building_guid = 75,
        door_guid = 1158
      )
      LocalObject(
        1564,
        IFFLock.Constructor(Vector3(7026.797f, 1256.352f, 50.91467f), Vector3(0, 0, 96)),
        owning_building_guid = 75,
        door_guid = 1160
      )
      LocalObject(
        1571,
        IFFLock.Constructor(Vector3(7073.221f, 1188.743f, 58.53067f), Vector3(0, 0, 186)),
        owning_building_guid = 75,
        door_guid = 785
      )
      LocalObject(2062, Locker.Constructor(Vector3(7005.268f, 1271.306f, 42.07867f)), owning_building_guid = 75)
      LocalObject(2063, Locker.Constructor(Vector3(7005.406f, 1272.623f, 42.07867f)), owning_building_guid = 75)
      LocalObject(2064, Locker.Constructor(Vector3(7005.546f, 1273.952f, 42.07867f)), owning_building_guid = 75)
      LocalObject(2065, Locker.Constructor(Vector3(7005.686f, 1275.281f, 42.07867f)), owning_building_guid = 75)
      LocalObject(2066, Locker.Constructor(Vector3(7006.16f, 1279.796f, 42.07867f)), owning_building_guid = 75)
      LocalObject(2067, Locker.Constructor(Vector3(7006.298f, 1281.113f, 42.07867f)), owning_building_guid = 75)
      LocalObject(2068, Locker.Constructor(Vector3(7006.438f, 1282.442f, 42.07867f)), owning_building_guid = 75)
      LocalObject(2069, Locker.Constructor(Vector3(7006.578f, 1283.771f, 42.07867f)), owning_building_guid = 75)
      LocalObject(2070, Locker.Constructor(Vector3(7024.071f, 1258.64f, 49.83967f)), owning_building_guid = 75)
      LocalObject(2071, Locker.Constructor(Vector3(7024.192f, 1259.798f, 49.83967f)), owning_building_guid = 75)
      LocalObject(2072, Locker.Constructor(Vector3(7024.312f, 1260.939f, 49.83967f)), owning_building_guid = 75)
      LocalObject(2073, Locker.Constructor(Vector3(7024.433f, 1262.081f, 49.83967f)), owning_building_guid = 75)
      LocalObject(
        214,
        Terminal.Constructor(Vector3(7001.211f, 1242.049f, 72.68167f), air_vehicle_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2323,
        VehicleSpawnPad.Constructor(Vector3(6980.149f, 1239.634f, 69.55667f), mb_pad_creation, Vector3(0, 0, -84)),
        owning_building_guid = 75,
        terminal_guid = 214
      )
      LocalObject(
        215,
        Terminal.Constructor(Vector3(7002.458f, 1253.916f, 72.68167f), air_vehicle_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2324,
        VehicleSpawnPad.Constructor(Vector3(6982.347f, 1260.537f, 69.55667f), mb_pad_creation, Vector3(0, 0, -84)),
        owning_building_guid = 75,
        terminal_guid = 215
      )
      LocalObject(
        2527,
        Terminal.Constructor(Vector3(7002.688f, 1240.27f, 63.40867f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2528,
        Terminal.Constructor(Vector3(7011.677f, 1263.051f, 51.16867f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2529,
        Terminal.Constructor(Vector3(7015.445f, 1262.655f, 51.16867f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2530,
        Terminal.Constructor(Vector3(7019.156f, 1262.265f, 51.16867f), order_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3484,
        Terminal.Constructor(Vector3(6974.322f, 1254.788f, 69.00167f), spawn_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3485,
        Terminal.Constructor(Vector3(7000.142f, 1274.91f, 63.66067f), spawn_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3486,
        Terminal.Constructor(Vector3(7007.367f, 1249.745f, 51.71267f), spawn_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3487,
        Terminal.Constructor(Vector3(7014.615f, 1248.98f, 51.71267f), spawn_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3488,
        Terminal.Constructor(Vector3(7021.868f, 1248.222f, 51.71267f), spawn_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3489,
        Terminal.Constructor(Vector3(7023.979f, 1242.943f, 43.63567f), spawn_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3490,
        Terminal.Constructor(Vector3(7068.372f, 1206.101f, 51.13567f), spawn_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3731,
        Terminal.Constructor(Vector3(7011.337f, 1188.018f, 42.79267f), ground_vehicle_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2325,
        VehicleSpawnPad.Constructor(Vector3(7000.476f, 1189.108f, 34.51567f), mb_pad_creation, Vector3(0, 0, -84)),
        owning_building_guid = 75,
        terminal_guid = 3731
      )
      LocalObject(3231, ResourceSilo.Constructor(Vector3(6950.078f, 1304.817f, 63.98667f)), owning_building_guid = 75)
      LocalObject(
        3340,
        SpawnTube.Constructor(Vector3(7005.859f, 1249.162f, 49.57867f), Vector3(0, 0, 276)),
        owning_building_guid = 75
      )
      LocalObject(
        3341,
        SpawnTube.Constructor(Vector3(7013.107f, 1248.4f, 49.57867f), Vector3(0, 0, 276)),
        owning_building_guid = 75
      )
      LocalObject(
        3342,
        SpawnTube.Constructor(Vector3(7020.358f, 1247.638f, 49.57867f), Vector3(0, 0, 276)),
        owning_building_guid = 75
      )
      LocalObject(
        2352,
        ProximityTerminal.Constructor(Vector3(7006.463f, 1277.465f, 42.07867f), medical_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2353,
        ProximityTerminal.Constructor(Vector3(7013.215f, 1239.164f, 62.07567f), medical_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2807,
        ProximityTerminal.Constructor(Vector3(6979.834f, 1198.351f, 67.52867f), pad_landing_frame),
        owning_building_guid = 75
      )
      LocalObject(
        2808,
        Terminal.Constructor(Vector3(6979.834f, 1198.351f, 67.52867f), air_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2810,
        ProximityTerminal.Constructor(Vector3(6993.798f, 1176.496f, 65.17367f), pad_landing_frame),
        owning_building_guid = 75
      )
      LocalObject(
        2811,
        Terminal.Constructor(Vector3(6993.798f, 1176.496f, 65.17367f), air_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2813,
        ProximityTerminal.Constructor(Vector3(7008.894f, 1305.199f, 65.18667f), pad_landing_frame),
        owning_building_guid = 75
      )
      LocalObject(
        2814,
        Terminal.Constructor(Vector3(7008.894f, 1305.199f, 65.18667f), air_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2816,
        ProximityTerminal.Constructor(Vector3(7024.394f, 1297.242f, 67.63067f), pad_landing_frame),
        owning_building_guid = 75
      )
      LocalObject(
        2817,
        Terminal.Constructor(Vector3(7024.394f, 1297.242f, 67.63067f), air_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2819,
        ProximityTerminal.Constructor(Vector3(7041.153f, 1242.583f, 72.42567f), pad_landing_frame),
        owning_building_guid = 75
      )
      LocalObject(
        2820,
        Terminal.Constructor(Vector3(7041.153f, 1242.583f, 72.42567f), air_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2822,
        ProximityTerminal.Constructor(Vector3(7059.595f, 1256.888f, 65.18667f), pad_landing_frame),
        owning_building_guid = 75
      )
      LocalObject(
        2823,
        Terminal.Constructor(Vector3(7059.595f, 1256.888f, 65.18667f), air_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3200,
        ProximityTerminal.Constructor(Vector3(6934.624f, 1244.659f, 56.70717f), repair_silo),
        owning_building_guid = 75
      )
      LocalObject(
        3201,
        Terminal.Constructor(Vector3(6934.624f, 1244.659f, 56.70717f), ground_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        3204,
        ProximityTerminal.Constructor(Vector3(7074.414f, 1288.616f, 56.72867f), repair_silo),
        owning_building_guid = 75
      )
      LocalObject(
        3205,
        Terminal.Constructor(Vector3(7074.414f, 1288.616f, 56.72867f), ground_rearm_terminal),
        owning_building_guid = 75
      )
      LocalObject(
        2288,
        FacilityTurret.Constructor(Vector3(6868.896f, 1166.7f, 65.47767f), manned_turret),
        owning_building_guid = 75
      )
      TurretToWeapon(2288, 5093)
      LocalObject(
        2289,
        FacilityTurret.Constructor(Vector3(6878.272f, 1255.903f, 65.47767f), manned_turret),
        owning_building_guid = 75
      )
      TurretToWeapon(2289, 5094)
      LocalObject(
        2292,
        FacilityTurret.Constructor(Vector3(6946.818f, 1329.696f, 65.47767f), manned_turret),
        owning_building_guid = 75
      )
      TurretToWeapon(2292, 5095)
      LocalObject(
        2293,
        FacilityTurret.Constructor(Vector3(7020.201f, 1329.752f, 65.47767f), manned_turret),
        owning_building_guid = 75
      )
      TurretToWeapon(2293, 5096)
      LocalObject(
        2294,
        FacilityTurret.Constructor(Vector3(7066.576f, 1151.46f, 65.47767f), manned_turret),
        owning_building_guid = 75
      )
      TurretToWeapon(2294, 5097)
      LocalObject(
        2295,
        FacilityTurret.Constructor(Vector3(7083.804f, 1315.298f, 65.47767f), manned_turret),
        owning_building_guid = 75
      )
      TurretToWeapon(2295, 5098)
      LocalObject(2894, Painbox.Constructor(Vector3(6982.463f, 1265.2f, 45.55197f), painbox), owning_building_guid = 75)
      LocalObject(
        2911,
        Painbox.Constructor(Vector3(7014.764f, 1256.873f, 53.84857f), painbox_continuous),
        owning_building_guid = 75
      )
      LocalObject(
        2928,
        Painbox.Constructor(Vector3(6997.323f, 1265.611f, 45.23807f), painbox_door_radius),
        owning_building_guid = 75
      )
      LocalObject(
        2975,
        Painbox.Constructor(Vector3(7000.186f, 1257.428f, 52.25487f), painbox_door_radius_continuous),
        owning_building_guid = 75
      )
      LocalObject(
        2976,
        Painbox.Constructor(Vector3(7008.995f, 1272.37f, 53.16097f), painbox_door_radius_continuous),
        owning_building_guid = 75
      )
      LocalObject(
        2977,
        Painbox.Constructor(Vector3(7027.439f, 1253.733f, 51.70657f), painbox_door_radius_continuous),
        owning_building_guid = 75
      )
      LocalObject(396, Generator.Constructor(Vector3(6979.367f, 1267.775f, 40.78467f)), owning_building_guid = 75)
      LocalObject(
        379,
        Terminal.Constructor(Vector3(6987.519f, 1266.966f, 42.07867f), gen_control),
        owning_building_guid = 75
      )
    }

    Building22()

    def Building22(): Unit = { // Name: S_Wele_Tower Type: tower_a GUID: 78, MapID: 22
      LocalBuilding(
        "S_Wele_Tower",
        78,
        22,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(618f, 6168f, 60.51881f), Vector3(0f, 0f, 17f), tower_a)
        )
      )
      LocalObject(
        3351,
        CaptureTerminal.Constructor(Vector3(633.8923f, 6172.751f, 70.51781f), secondary_capture),
        owning_building_guid = 78
      )
      LocalObject(429, Door.Constructor(Vector3(627.1367f, 6179.159f, 62.03981f)), owning_building_guid = 78)
      LocalObject(430, Door.Constructor(Vector3(627.1367f, 6179.159f, 82.03882f)), owning_building_guid = 78)
      LocalObject(432, Door.Constructor(Vector3(631.8146f, 6163.858f, 62.03981f)), owning_building_guid = 78)
      LocalObject(433, Door.Constructor(Vector3(631.8146f, 6163.858f, 82.03882f)), owning_building_guid = 78)
      LocalObject(3505, Door.Constructor(Vector3(627.1375f, 6176.235f, 51.85481f)), owning_building_guid = 78)
      LocalObject(3506, Door.Constructor(Vector3(631.9353f, 6160.542f, 51.85481f)), owning_building_guid = 78)
      LocalObject(
        1293,
        IFFLock.Constructor(Vector3(624.9459f, 6179.337f, 61.97981f), Vector3(0, 0, 343)),
        owning_building_guid = 78,
        door_guid = 429
      )
      LocalObject(
        1294,
        IFFLock.Constructor(Vector3(624.9459f, 6179.337f, 81.97981f), Vector3(0, 0, 343)),
        owning_building_guid = 78,
        door_guid = 430
      )
      LocalObject(
        1295,
        IFFLock.Constructor(Vector3(634.0093f, 6163.681f, 61.97981f), Vector3(0, 0, 163)),
        owning_building_guid = 78,
        door_guid = 432
      )
      LocalObject(
        1296,
        IFFLock.Constructor(Vector3(634.0093f, 6163.681f, 81.97981f), Vector3(0, 0, 163)),
        owning_building_guid = 78,
        door_guid = 433
      )
      LocalObject(1625, Locker.Constructor(Vector3(631.0644f, 6179.142f, 50.51281f)), owning_building_guid = 78)
      LocalObject(1626, Locker.Constructor(Vector3(632.343f, 6179.532f, 50.51281f)), owning_building_guid = 78)
      LocalObject(1627, Locker.Constructor(Vector3(634.8801f, 6180.308f, 50.51281f)), owning_building_guid = 78)
      LocalObject(1628, Locker.Constructor(Vector3(636.2208f, 6180.718f, 50.51281f)), owning_building_guid = 78)
      LocalObject(1629, Locker.Constructor(Vector3(637.4257f, 6158.215f, 50.51281f)), owning_building_guid = 78)
      LocalObject(1630, Locker.Constructor(Vector3(638.7043f, 6158.606f, 50.51281f)), owning_building_guid = 78)
      LocalObject(1631, Locker.Constructor(Vector3(641.2748f, 6159.392f, 50.51281f)), owning_building_guid = 78)
      LocalObject(1632, Locker.Constructor(Vector3(642.6155f, 6159.802f, 50.51281f)), owning_building_guid = 78)
      LocalObject(
        2367,
        Terminal.Constructor(Vector3(638.1472f, 6175.45f, 51.85081f), order_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        2368,
        Terminal.Constructor(Vector3(639.7204f, 6170.304f, 51.85081f), order_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        2369,
        Terminal.Constructor(Vector3(641.394f, 6164.83f, 51.85081f), order_terminal),
        owning_building_guid = 78
      )
      LocalObject(
        3241,
        SpawnTube.Constructor(Vector3(627.0243f, 6175.101f, 50.00082f), respawn_tube_tower, Vector3(0, 0, 343)),
        owning_building_guid = 78
      )
      LocalObject(
        3242,
        SpawnTube.Constructor(Vector3(631.8221f, 6159.408f, 50.00082f), respawn_tube_tower, Vector3(0, 0, 343)),
        owning_building_guid = 78
      )
      LocalObject(
        2166,
        FacilityTurret.Constructor(Vector3(609.5886f, 6152.143f, 79.46082f), manned_turret),
        owning_building_guid = 78
      )
      TurretToWeapon(2166, 5099)
      LocalObject(
        2170,
        FacilityTurret.Constructor(Vector3(635.9423f, 6186.773f, 79.46082f), manned_turret),
        owning_building_guid = 78
      )
      TurretToWeapon(2170, 5100)
      LocalObject(
        2984,
        Painbox.Constructor(Vector3(624.8181f, 6163.604f, 52.01791f), painbox_radius_continuous),
        owning_building_guid = 78
      )
      LocalObject(
        2985,
        Painbox.Constructor(Vector3(633.5413f, 6174.933f, 50.61881f), painbox_radius_continuous),
        owning_building_guid = 78
      )
      LocalObject(
        2986,
        Painbox.Constructor(Vector3(637.092f, 6163.613f, 50.61881f), painbox_radius_continuous),
        owning_building_guid = 78
      )
    }

    Building61()

    def Building61(): Unit = { // Name: NE_Wele_Tower Type: tower_a GUID: 79, MapID: 61
      LocalBuilding(
        "NE_Wele_Tower",
        79,
        61,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(638f, 7206f, 62.16609f), Vector3(0f, 0f, 16f), tower_a)
        )
      )
      LocalObject(
        3352,
        CaptureTerminal.Constructor(Vector3(653.9728f, 7210.473f, 72.16509f), secondary_capture),
        owning_building_guid = 79
      )
      LocalObject(435, Door.Constructor(Vector3(647.33f, 7216.998f, 63.68709f)), owning_building_guid = 79)
      LocalObject(436, Door.Constructor(Vector3(647.33f, 7216.998f, 83.6861f)), owning_building_guid = 79)
      LocalObject(438, Door.Constructor(Vector3(651.7402f, 7201.618f, 63.68709f)), owning_building_guid = 79)
      LocalObject(439, Door.Constructor(Vector3(651.7402f, 7201.618f, 83.6861f)), owning_building_guid = 79)
      LocalObject(3507, Door.Constructor(Vector3(647.2798f, 7214.075f, 53.50209f)), owning_building_guid = 79)
      LocalObject(3508, Door.Constructor(Vector3(651.803f, 7198.3f, 53.50209f)), owning_building_guid = 79)
      LocalObject(
        1297,
        IFFLock.Constructor(Vector3(645.1426f, 7217.214f, 63.62709f), Vector3(0, 0, 344)),
        owning_building_guid = 79,
        door_guid = 435
      )
      LocalObject(
        1298,
        IFFLock.Constructor(Vector3(645.1426f, 7217.214f, 83.62709f), Vector3(0, 0, 344)),
        owning_building_guid = 79,
        door_guid = 436
      )
      LocalObject(
        1301,
        IFFLock.Constructor(Vector3(653.9315f, 7201.402f, 63.62709f), Vector3(0, 0, 164)),
        owning_building_guid = 79,
        door_guid = 438
      )
      LocalObject(
        1302,
        IFFLock.Constructor(Vector3(653.9315f, 7201.402f, 83.62709f), Vector3(0, 0, 164)),
        owning_building_guid = 79,
        door_guid = 439
      )
      LocalObject(1638, Locker.Constructor(Vector3(651.2568f, 7216.912f, 52.16009f)), owning_building_guid = 79)
      LocalObject(1639, Locker.Constructor(Vector3(652.5421f, 7217.28f, 52.16009f)), owning_building_guid = 79)
      LocalObject(1640, Locker.Constructor(Vector3(655.0923f, 7218.012f, 52.16009f)), owning_building_guid = 79)
      LocalObject(1641, Locker.Constructor(Vector3(656.44f, 7218.398f, 52.16009f)), owning_building_guid = 79)
      LocalObject(1642, Locker.Constructor(Vector3(657.252f, 7195.877f, 52.16009f)), owning_building_guid = 79)
      LocalObject(1643, Locker.Constructor(Vector3(658.5372f, 7196.246f, 52.16009f)), owning_building_guid = 79)
      LocalObject(1644, Locker.Constructor(Vector3(661.121f, 7196.987f, 52.16009f)), owning_building_guid = 79)
      LocalObject(1645, Locker.Constructor(Vector3(662.4687f, 7197.374f, 52.16009f)), owning_building_guid = 79)
      LocalObject(
        2370,
        Terminal.Constructor(Vector3(658.2741f, 7213.097f, 53.49809f), order_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        2371,
        Terminal.Constructor(Vector3(659.7573f, 7207.925f, 53.49809f), order_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        2372,
        Terminal.Constructor(Vector3(661.3351f, 7202.422f, 53.49809f), order_terminal),
        owning_building_guid = 79
      )
      LocalObject(
        3243,
        SpawnTube.Constructor(Vector3(647.1469f, 7212.942f, 51.64809f), respawn_tube_tower, Vector3(0, 0, 344)),
        owning_building_guid = 79
      )
      LocalObject(
        3244,
        SpawnTube.Constructor(Vector3(651.67f, 7197.168f, 51.64809f), respawn_tube_tower, Vector3(0, 0, 344)),
        owning_building_guid = 79
      )
      LocalObject(
        2169,
        FacilityTurret.Constructor(Vector3(629.3132f, 7190.292f, 81.10809f), manned_turret),
        owning_building_guid = 79
      )
      TurretToWeapon(2169, 5101)
      LocalObject(
        2173,
        FacilityTurret.Constructor(Vector3(656.2672f, 7224.457f, 81.10809f), manned_turret),
        owning_building_guid = 79
      )
      TurretToWeapon(2173, 5102)
      LocalObject(
        2987,
        Painbox.Constructor(Vector3(644.7404f, 7201.486f, 53.66519f), painbox_radius_continuous),
        owning_building_guid = 79
      )
      LocalObject(
        2988,
        Painbox.Constructor(Vector3(653.6599f, 7212.661f, 52.26609f), painbox_radius_continuous),
        owning_building_guid = 79
      )
      LocalObject(
        2989,
        Painbox.Constructor(Vector3(657.0125f, 7201.28f, 52.26609f), painbox_radius_continuous),
        owning_building_guid = 79
      )
    }

    Building62()

    def Building62(): Unit = { // Name: S_Nzame_Tower Type: tower_a GUID: 80, MapID: 62
      LocalBuilding(
        "S_Nzame_Tower",
        80,
        62,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1432f, 2500f, 42.88556f), Vector3(0f, 0f, 334f), tower_a)
        )
      )
      LocalObject(
        3356,
        CaptureTerminal.Constructor(Vector3(1446.863f, 2492.636f, 52.88456f), secondary_capture),
        owning_building_guid = 80
      )
      LocalObject(482, Door.Constructor(Vector3(1439.279f, 2487.549f, 44.40656f)), owning_building_guid = 80)
      LocalObject(483, Door.Constructor(Vector3(1439.279f, 2487.549f, 64.40556f)), owning_building_guid = 80)
      LocalObject(484, Door.Constructor(Vector3(1446.292f, 2501.93f, 44.40656f)), owning_building_guid = 80)
      LocalObject(485, Door.Constructor(Vector3(1446.292f, 2501.93f, 64.40556f)), owning_building_guid = 80)
      LocalObject(3521, Door.Constructor(Vector3(1437.106f, 2485.042f, 34.22156f)), owning_building_guid = 80)
      LocalObject(3522, Door.Constructor(Vector3(1444.299f, 2499.791f, 34.22156f)), owning_building_guid = 80)
      LocalObject(
        1338,
        IFFLock.Constructor(Vector3(1440.763f, 2485.923f, 44.34656f), Vector3(0, 0, 206)),
        owning_building_guid = 80,
        door_guid = 482
      )
      LocalObject(
        1339,
        IFFLock.Constructor(Vector3(1440.763f, 2485.923f, 64.34656f), Vector3(0, 0, 206)),
        owning_building_guid = 80,
        door_guid = 483
      )
      LocalObject(
        1340,
        IFFLock.Constructor(Vector3(1444.812f, 2503.554f, 44.34656f), Vector3(0, 0, 26)),
        owning_building_guid = 80,
        door_guid = 484
      )
      LocalObject(
        1341,
        IFFLock.Constructor(Vector3(1444.812f, 2503.554f, 64.34656f), Vector3(0, 0, 26)),
        owning_building_guid = 80,
        door_guid = 485
      )
      LocalObject(1694, Locker.Constructor(Vector3(1439.534f, 2479.595f, 32.87956f)), owning_building_guid = 80)
      LocalObject(1695, Locker.Constructor(Vector3(1440.735f, 2479.009f, 32.87956f)), owning_building_guid = 80)
      LocalObject(1696, Locker.Constructor(Vector3(1443.151f, 2477.831f, 32.87956f)), owning_building_guid = 80)
      LocalObject(1697, Locker.Constructor(Vector3(1444.411f, 2477.216f, 32.87956f)), owning_building_guid = 80)
      LocalObject(1698, Locker.Constructor(Vector3(1449.153f, 2499.239f, 32.87956f)), owning_building_guid = 80)
      LocalObject(1699, Locker.Constructor(Vector3(1450.355f, 2498.652f, 32.87956f)), owning_building_guid = 80)
      LocalObject(1700, Locker.Constructor(Vector3(1452.739f, 2497.489f, 32.87956f)), owning_building_guid = 80)
      LocalObject(1701, Locker.Constructor(Vector3(1454f, 2496.875f, 32.87956f)), owning_building_guid = 80)
      LocalObject(
        2395,
        Terminal.Constructor(Vector3(1446.948f, 2481.727f, 34.21756f), order_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        2396,
        Terminal.Constructor(Vector3(1449.457f, 2486.872f, 34.21756f), order_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        2397,
        Terminal.Constructor(Vector3(1451.816f, 2491.708f, 34.21756f), order_terminal),
        owning_building_guid = 80
      )
      LocalObject(
        3257,
        SpawnTube.Constructor(Vector3(1436.249f, 2484.289f, 32.36756f), respawn_tube_tower, Vector3(0, 0, 26)),
        owning_building_guid = 80
      )
      LocalObject(
        3258,
        SpawnTube.Constructor(Vector3(1443.443f, 2499.039f, 32.36756f), respawn_tube_tower, Vector3(0, 0, 26)),
        owning_building_guid = 80
      )
      LocalObject(
        2189,
        FacilityTurret.Constructor(Vector3(1415.034f, 2494.139f, 61.82756f), manned_turret),
        owning_building_guid = 80
      )
      TurretToWeapon(2189, 5103)
      LocalObject(
        2192,
        FacilityTurret.Constructor(Vector3(1457.925f, 2501.493f, 61.82756f), manned_turret),
        owning_building_guid = 80
      )
      TurretToWeapon(2192, 5104)
      LocalObject(
        2999,
        Painbox.Constructor(Vector3(1433.989f, 2492.135f, 34.38466f), painbox_radius_continuous),
        owning_building_guid = 80
      )
      LocalObject(
        3000,
        Painbox.Constructor(Vector3(1442.971f, 2483.771f, 32.98556f), painbox_radius_continuous),
        owning_building_guid = 80
      )
      LocalObject(
        3001,
        Painbox.Constructor(Vector3(1448.094f, 2494.471f, 32.98556f), painbox_radius_continuous),
        owning_building_guid = 80
      )
    }

    Building27()

    def Building27(): Unit = { // Name: NE_Searhus_Warpgate_Tower Type: tower_a GUID: 81, MapID: 27
      LocalBuilding(
        "NE_Searhus_Warpgate_Tower",
        81,
        27,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1534f, 1914f, 40.9772f), Vector3(0f, 0f, 350f), tower_a)
        )
      )
      LocalObject(
        3357,
        CaptureTerminal.Constructor(Vector3(1550.317f, 1911.018f, 50.9762f), secondary_capture),
        owning_building_guid = 81
      )
      LocalObject(491, Door.Constructor(Vector3(1544.428f, 1904.038f, 42.4982f)), owning_building_guid = 81)
      LocalObject(492, Door.Constructor(Vector3(1544.428f, 1904.038f, 62.4972f)), owning_building_guid = 81)
      LocalObject(494, Door.Constructor(Vector3(1547.207f, 1919.795f, 42.4982f)), owning_building_guid = 81)
      LocalObject(495, Door.Constructor(Vector3(1547.207f, 1919.795f, 62.4972f)), owning_building_guid = 81)
      LocalObject(3523, Door.Constructor(Vector3(1543.031f, 1901.029f, 32.3132f)), owning_building_guid = 81)
      LocalObject(3524, Door.Constructor(Vector3(1545.88f, 1917.189f, 32.3132f)), owning_building_guid = 81)
      LocalObject(
        1344,
        IFFLock.Constructor(Vector3(1545.336f, 1920.948f, 42.4382f), Vector3(0, 0, 10)),
        owning_building_guid = 81,
        door_guid = 494
      )
      LocalObject(
        1345,
        IFFLock.Constructor(Vector3(1545.336f, 1920.948f, 62.4382f), Vector3(0, 0, 10)),
        owning_building_guid = 81,
        door_guid = 495
      )
      LocalObject(
        1346,
        IFFLock.Constructor(Vector3(1546.304f, 1902.884f, 42.4382f), Vector3(0, 0, 190)),
        owning_building_guid = 81,
        door_guid = 491
      )
      LocalObject(
        1347,
        IFFLock.Constructor(Vector3(1546.304f, 1902.884f, 62.4382f), Vector3(0, 0, 190)),
        owning_building_guid = 81,
        door_guid = 492
      )
      LocalObject(1702, Locker.Constructor(Vector3(1546.866f, 1896.462f, 30.9712f)), owning_building_guid = 81)
      LocalObject(1703, Locker.Constructor(Vector3(1548.183f, 1896.23f, 30.9712f)), owning_building_guid = 81)
      LocalObject(1704, Locker.Constructor(Vector3(1550.699f, 1917.996f, 30.9712f)), owning_building_guid = 81)
      LocalObject(1705, Locker.Constructor(Vector3(1550.83f, 1895.763f, 30.9712f)), owning_building_guid = 81)
      LocalObject(1706, Locker.Constructor(Vector3(1552.015f, 1917.764f, 30.9712f)), owning_building_guid = 81)
      LocalObject(1707, Locker.Constructor(Vector3(1552.211f, 1895.52f, 30.9712f)), owning_building_guid = 81)
      LocalObject(1710, Locker.Constructor(Vector3(1554.628f, 1917.303f, 30.9712f)), owning_building_guid = 81)
      LocalObject(1713, Locker.Constructor(Vector3(1556.009f, 1917.06f, 30.9712f)), owning_building_guid = 81)
      LocalObject(
        2399,
        Terminal.Constructor(Vector3(1553.405f, 1900.555f, 32.3092f), order_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        2400,
        Terminal.Constructor(Vector3(1554.399f, 1906.192f, 32.3092f), order_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        2402,
        Terminal.Constructor(Vector3(1555.333f, 1911.491f, 32.3092f), order_terminal),
        owning_building_guid = 81
      )
      LocalObject(
        3259,
        SpawnTube.Constructor(Vector3(1542.415f, 1900.069f, 30.4592f), respawn_tube_tower, Vector3(0, 0, 10)),
        owning_building_guid = 81
      )
      LocalObject(
        3260,
        SpawnTube.Constructor(Vector3(1545.264f, 1916.23f, 30.4592f), respawn_tube_tower, Vector3(0, 0, 10)),
        owning_building_guid = 81
      )
      LocalObject(
        2194,
        FacilityTurret.Constructor(Vector3(1519.306f, 1903.69f, 59.9192f), manned_turret),
        owning_building_guid = 81
      )
      TurretToWeapon(2194, 5105)
      LocalObject(
        2195,
        FacilityTurret.Constructor(Vector3(1558.51f, 1922.581f, 59.9192f), manned_turret),
        owning_building_guid = 81
      )
      TurretToWeapon(2195, 5106)
      LocalObject(
        3002,
        Painbox.Constructor(Vector3(1538.079f, 1906.988f, 32.4763f), painbox_radius_continuous),
        owning_building_guid = 81
      )
      LocalObject(
        3003,
        Painbox.Constructor(Vector3(1549.019f, 1901.423f, 31.0772f), painbox_radius_continuous),
        owning_building_guid = 81
      )
      LocalObject(
        3004,
        Painbox.Constructor(Vector3(1550.995f, 1913.122f, 31.0772f), painbox_radius_continuous),
        owning_building_guid = 81
      )
    }

    Building26()

    def Building26(): Unit = { // Name: NE_Nzame_Tower Type: tower_a GUID: 82, MapID: 26
      LocalBuilding(
        "NE_Nzame_Tower",
        82,
        26,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1950f, 3610f, 55.32143f), Vector3(0f, 0f, 17f), tower_a)
        )
      )
      LocalObject(
        3358,
        CaptureTerminal.Constructor(Vector3(1965.892f, 3614.751f, 65.32043f), secondary_capture),
        owning_building_guid = 82
      )
      LocalObject(509, Door.Constructor(Vector3(1959.137f, 3621.159f, 56.84243f)), owning_building_guid = 82)
      LocalObject(510, Door.Constructor(Vector3(1959.137f, 3621.159f, 76.84143f)), owning_building_guid = 82)
      LocalObject(511, Door.Constructor(Vector3(1963.815f, 3605.858f, 56.84243f)), owning_building_guid = 82)
      LocalObject(512, Door.Constructor(Vector3(1963.815f, 3605.858f, 76.84143f)), owning_building_guid = 82)
      LocalObject(3528, Door.Constructor(Vector3(1959.137f, 3618.235f, 46.65743f)), owning_building_guid = 82)
      LocalObject(3529, Door.Constructor(Vector3(1963.935f, 3602.542f, 46.65743f)), owning_building_guid = 82)
      LocalObject(
        1357,
        IFFLock.Constructor(Vector3(1956.946f, 3621.337f, 56.78243f), Vector3(0, 0, 343)),
        owning_building_guid = 82,
        door_guid = 509
      )
      LocalObject(
        1358,
        IFFLock.Constructor(Vector3(1956.946f, 3621.337f, 76.78243f), Vector3(0, 0, 343)),
        owning_building_guid = 82,
        door_guid = 510
      )
      LocalObject(
        1359,
        IFFLock.Constructor(Vector3(1966.009f, 3605.681f, 56.78243f), Vector3(0, 0, 163)),
        owning_building_guid = 82,
        door_guid = 511
      )
      LocalObject(
        1360,
        IFFLock.Constructor(Vector3(1966.009f, 3605.681f, 76.78243f), Vector3(0, 0, 163)),
        owning_building_guid = 82,
        door_guid = 512
      )
      LocalObject(1722, Locker.Constructor(Vector3(1963.064f, 3621.142f, 45.31543f)), owning_building_guid = 82)
      LocalObject(1723, Locker.Constructor(Vector3(1964.343f, 3621.532f, 45.31543f)), owning_building_guid = 82)
      LocalObject(1724, Locker.Constructor(Vector3(1966.88f, 3622.308f, 45.31543f)), owning_building_guid = 82)
      LocalObject(1725, Locker.Constructor(Vector3(1968.221f, 3622.718f, 45.31543f)), owning_building_guid = 82)
      LocalObject(1726, Locker.Constructor(Vector3(1969.426f, 3600.215f, 45.31543f)), owning_building_guid = 82)
      LocalObject(1727, Locker.Constructor(Vector3(1970.704f, 3600.606f, 45.31543f)), owning_building_guid = 82)
      LocalObject(1728, Locker.Constructor(Vector3(1973.275f, 3601.392f, 45.31543f)), owning_building_guid = 82)
      LocalObject(1729, Locker.Constructor(Vector3(1974.616f, 3601.802f, 45.31543f)), owning_building_guid = 82)
      LocalObject(
        2407,
        Terminal.Constructor(Vector3(1970.147f, 3617.45f, 46.65343f), order_terminal),
        owning_building_guid = 82
      )
      LocalObject(
        2408,
        Terminal.Constructor(Vector3(1971.72f, 3612.304f, 46.65343f), order_terminal),
        owning_building_guid = 82
      )
      LocalObject(
        2409,
        Terminal.Constructor(Vector3(1973.394f, 3606.83f, 46.65343f), order_terminal),
        owning_building_guid = 82
      )
      LocalObject(
        3264,
        SpawnTube.Constructor(Vector3(1959.024f, 3617.101f, 44.80343f), respawn_tube_tower, Vector3(0, 0, 343)),
        owning_building_guid = 82
      )
      LocalObject(
        3265,
        SpawnTube.Constructor(Vector3(1963.822f, 3601.408f, 44.80343f), respawn_tube_tower, Vector3(0, 0, 343)),
        owning_building_guid = 82
      )
      LocalObject(
        2200,
        FacilityTurret.Constructor(Vector3(1941.589f, 3594.143f, 74.26343f), manned_turret),
        owning_building_guid = 82
      )
      TurretToWeapon(2200, 5107)
      LocalObject(
        2201,
        FacilityTurret.Constructor(Vector3(1967.942f, 3628.773f, 74.26343f), manned_turret),
        owning_building_guid = 82
      )
      TurretToWeapon(2201, 5108)
      LocalObject(
        3005,
        Painbox.Constructor(Vector3(1956.818f, 3605.604f, 46.82053f), painbox_radius_continuous),
        owning_building_guid = 82
      )
      LocalObject(
        3006,
        Painbox.Constructor(Vector3(1965.541f, 3616.933f, 45.42143f), painbox_radius_continuous),
        owning_building_guid = 82
      )
      LocalObject(
        3007,
        Painbox.Constructor(Vector3(1969.092f, 3605.613f, 45.42143f), painbox_radius_continuous),
        owning_building_guid = 82
      )
    }

    Building36()

    def Building36(): Unit = { // Name: SE_NCSanc_Warpgate_Tower Type: tower_a GUID: 83, MapID: 36
      LocalBuilding(
        "SE_NCSanc_Warpgate_Tower",
        83,
        36,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2838f, 6204f, 67.4752f), Vector3(0f, 0f, 8f), tower_a)
        )
      )
      LocalObject(
        3361,
        CaptureTerminal.Constructor(Vector3(2854.44f, 6206.207f, 77.47421f), secondary_capture),
        owning_building_guid = 83
      )
      LocalObject(538, Door.Constructor(Vector3(2848.77f, 6213.592f, 68.99621f)), owning_building_guid = 83)
      LocalObject(539, Door.Constructor(Vector3(2848.77f, 6213.592f, 88.99521f)), owning_building_guid = 83)
      LocalObject(540, Door.Constructor(Vector3(2850.997f, 6197.748f, 68.99621f)), owning_building_guid = 83)
      LocalObject(541, Door.Constructor(Vector3(2850.997f, 6197.748f, 88.99521f)), owning_building_guid = 83)
      LocalObject(3537, Door.Constructor(Vector3(2848.313f, 6210.705f, 58.8112f)), owning_building_guid = 83)
      LocalObject(3538, Door.Constructor(Vector3(2850.597f, 6194.454f, 58.8112f)), owning_building_guid = 83)
      LocalObject(
        1379,
        IFFLock.Constructor(Vector3(2846.634f, 6214.111f, 68.9362f), Vector3(0, 0, 352)),
        owning_building_guid = 83,
        door_guid = 538
      )
      LocalObject(
        1380,
        IFFLock.Constructor(Vector3(2846.634f, 6214.111f, 88.9362f), Vector3(0, 0, 352)),
        owning_building_guid = 83,
        door_guid = 539
      )
      LocalObject(
        1381,
        IFFLock.Constructor(Vector3(2853.136f, 6197.229f, 68.9362f), Vector3(0, 0, 172)),
        owning_building_guid = 83,
        door_guid = 540
      )
      LocalObject(
        1382,
        IFFLock.Constructor(Vector3(2853.136f, 6197.229f, 88.9362f), Vector3(0, 0, 172)),
        owning_building_guid = 83,
        door_guid = 541
      )
      LocalObject(1758, Locker.Constructor(Vector3(2852.646f, 6212.96f, 57.4692f)), owning_building_guid = 83)
      LocalObject(1759, Locker.Constructor(Vector3(2853.97f, 6213.146f, 57.4692f)), owning_building_guid = 83)
      LocalObject(1760, Locker.Constructor(Vector3(2855.656f, 6191.296f, 57.4692f)), owning_building_guid = 83)
      LocalObject(1761, Locker.Constructor(Vector3(2856.598f, 6213.516f, 57.4692f)), owning_building_guid = 83)
      LocalObject(1762, Locker.Constructor(Vector3(2856.98f, 6191.482f, 57.4692f)), owning_building_guid = 83)
      LocalObject(1763, Locker.Constructor(Vector3(2857.986f, 6213.711f, 57.4692f)), owning_building_guid = 83)
      LocalObject(1764, Locker.Constructor(Vector3(2859.642f, 6191.857f, 57.4692f)), owning_building_guid = 83)
      LocalObject(1765, Locker.Constructor(Vector3(2861.03f, 6192.052f, 57.4692f)), owning_building_guid = 83)
      LocalObject(
        2420,
        Terminal.Constructor(Vector3(2859.064f, 6208.207f, 58.80721f), order_terminal),
        owning_building_guid = 83
      )
      LocalObject(
        2421,
        Terminal.Constructor(Vector3(2859.813f, 6202.878f, 58.80721f), order_terminal),
        owning_building_guid = 83
      )
      LocalObject(
        2422,
        Terminal.Constructor(Vector3(2860.61f, 6197.209f, 58.80721f), order_terminal),
        owning_building_guid = 83
      )
      LocalObject(
        3273,
        SpawnTube.Constructor(Vector3(2848.024f, 6209.602f, 56.95721f), respawn_tube_tower, Vector3(0, 0, 352)),
        owning_building_guid = 83
      )
      LocalObject(
        3274,
        SpawnTube.Constructor(Vector3(2850.308f, 6193.351f, 56.95721f), respawn_tube_tower, Vector3(0, 0, 352)),
        owning_building_guid = 83
      )
      LocalObject(
        2209,
        FacilityTurret.Constructor(Vector3(2827.212f, 6189.654f, 86.41721f), manned_turret),
        owning_building_guid = 83
      )
      TurretToWeapon(2209, 5109)
      LocalObject(
        2211,
        FacilityTurret.Constructor(Vector3(2858.658f, 6219.735f, 86.41721f), manned_turret),
        owning_building_guid = 83
      )
      TurretToWeapon(2211, 5110)
      LocalObject(
        3012,
        Painbox.Constructor(Vector3(2844.047f, 6198.592f, 58.9743f), painbox_radius_continuous),
        owning_building_guid = 83
      )
      LocalObject(
        3015,
        Painbox.Constructor(Vector3(2854.435f, 6208.417f, 57.5752f), painbox_radius_continuous),
        owning_building_guid = 83
      )
      LocalObject(
        3016,
        Painbox.Constructor(Vector3(2856.171f, 6196.68f, 57.5752f), painbox_radius_continuous),
        owning_building_guid = 83
      )
    }

    Building64()

    def Building64(): Unit = { // Name: N_Tore_Tower Type: tower_a GUID: 84, MapID: 64
      LocalBuilding(
        "N_Tore_Tower",
        84,
        64,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3008f, 2532f, 53.84287f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        3362,
        CaptureTerminal.Constructor(Vector3(3024.587f, 2531.897f, 63.84187f), secondary_capture),
        owning_building_guid = 84
      )
      LocalObject(556, Door.Constructor(Vector3(3020f, 2524f, 55.36387f)), owning_building_guid = 84)
      LocalObject(557, Door.Constructor(Vector3(3020f, 2524f, 75.36287f)), owning_building_guid = 84)
      LocalObject(558, Door.Constructor(Vector3(3020f, 2540f, 55.36387f)), owning_building_guid = 84)
      LocalObject(559, Door.Constructor(Vector3(3020f, 2540f, 75.36287f)), owning_building_guid = 84)
      LocalObject(3542, Door.Constructor(Vector3(3019.146f, 2520.794f, 45.17887f)), owning_building_guid = 84)
      LocalObject(3543, Door.Constructor(Vector3(3019.146f, 2537.204f, 45.17887f)), owning_building_guid = 84)
      LocalObject(
        1394,
        IFFLock.Constructor(Vector3(3017.957f, 2540.811f, 55.30387f), Vector3(0, 0, 0)),
        owning_building_guid = 84,
        door_guid = 558
      )
      LocalObject(
        1395,
        IFFLock.Constructor(Vector3(3017.957f, 2540.811f, 75.30387f), Vector3(0, 0, 0)),
        owning_building_guid = 84,
        door_guid = 559
      )
      LocalObject(
        1396,
        IFFLock.Constructor(Vector3(3022.047f, 2523.189f, 55.30387f), Vector3(0, 0, 180)),
        owning_building_guid = 84,
        door_guid = 556
      )
      LocalObject(
        1397,
        IFFLock.Constructor(Vector3(3022.047f, 2523.189f, 75.30387f), Vector3(0, 0, 180)),
        owning_building_guid = 84,
        door_guid = 557
      )
      LocalObject(1774, Locker.Constructor(Vector3(3023.716f, 2516.963f, 43.83687f)), owning_building_guid = 84)
      LocalObject(1775, Locker.Constructor(Vector3(3023.751f, 2538.835f, 43.83687f)), owning_building_guid = 84)
      LocalObject(1777, Locker.Constructor(Vector3(3025.053f, 2516.963f, 43.83687f)), owning_building_guid = 84)
      LocalObject(1778, Locker.Constructor(Vector3(3025.088f, 2538.835f, 43.83687f)), owning_building_guid = 84)
      LocalObject(1781, Locker.Constructor(Vector3(3027.741f, 2516.963f, 43.83687f)), owning_building_guid = 84)
      LocalObject(1782, Locker.Constructor(Vector3(3027.741f, 2538.835f, 43.83687f)), owning_building_guid = 84)
      LocalObject(1784, Locker.Constructor(Vector3(3029.143f, 2516.963f, 43.83687f)), owning_building_guid = 84)
      LocalObject(1785, Locker.Constructor(Vector3(3029.143f, 2538.835f, 43.83687f)), owning_building_guid = 84)
      LocalObject(
        2430,
        Terminal.Constructor(Vector3(3029.445f, 2522.129f, 45.17487f), order_terminal),
        owning_building_guid = 84
      )
      LocalObject(
        2431,
        Terminal.Constructor(Vector3(3029.445f, 2527.853f, 45.17487f), order_terminal),
        owning_building_guid = 84
      )
      LocalObject(
        2432,
        Terminal.Constructor(Vector3(3029.445f, 2533.234f, 45.17487f), order_terminal),
        owning_building_guid = 84
      )
      LocalObject(
        3278,
        SpawnTube.Constructor(Vector3(3018.706f, 2519.742f, 43.32487f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 84
      )
      LocalObject(
        3279,
        SpawnTube.Constructor(Vector3(3018.706f, 2536.152f, 43.32487f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 84
      )
      LocalObject(
        2216,
        FacilityTurret.Constructor(Vector3(2995.32f, 2519.295f, 72.78487f), manned_turret),
        owning_building_guid = 84
      )
      TurretToWeapon(2216, 5111)
      LocalObject(
        2217,
        FacilityTurret.Constructor(Vector3(3030.647f, 2544.707f, 72.78487f), manned_turret),
        owning_building_guid = 84
      )
      TurretToWeapon(2217, 5112)
      LocalObject(
        3017,
        Painbox.Constructor(Vector3(3013.235f, 2525.803f, 45.34197f), painbox_radius_continuous),
        owning_building_guid = 84
      )
      LocalObject(
        3018,
        Painbox.Constructor(Vector3(3024.889f, 2534.086f, 43.94287f), painbox_radius_continuous),
        owning_building_guid = 84
      )
      LocalObject(
        3019,
        Painbox.Constructor(Vector3(3024.975f, 2522.223f, 43.94287f), painbox_radius_continuous),
        owning_building_guid = 84
      )
    }

    Building35()

    def Building35(): Unit = { // Name: SW_Honsi_Tower Type: tower_a GUID: 85, MapID: 35
      LocalBuilding(
        "SW_Honsi_Tower",
        85,
        35,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3430f, 4030f, 69.08597f), Vector3(0f, 0f, 302f), tower_a)
        )
      )
      LocalObject(
        3363,
        CaptureTerminal.Constructor(Vector3(3438.702f, 4015.879f, 79.08497f), secondary_capture),
        owning_building_guid = 85
      )
      LocalObject(560, Door.Constructor(Vector3(3429.575f, 4015.584f, 70.60697f)), owning_building_guid = 85)
      LocalObject(561, Door.Constructor(Vector3(3429.575f, 4015.584f, 90.60597f)), owning_building_guid = 85)
      LocalObject(562, Door.Constructor(Vector3(3443.143f, 4024.063f, 70.60697f)), owning_building_guid = 85)
      LocalObject(563, Door.Constructor(Vector3(3443.143f, 4024.063f, 90.60597f)), owning_building_guid = 85)
      LocalObject(3544, Door.Constructor(Vector3(3426.403f, 4014.609f, 60.42197f)), owning_building_guid = 85)
      LocalObject(3545, Door.Constructor(Vector3(3440.32f, 4023.305f, 60.42197f)), owning_building_guid = 85)
      LocalObject(
        1398,
        IFFLock.Constructor(Vector3(3429.972f, 4013.418f, 70.54697f), Vector3(0, 0, 238)),
        owning_building_guid = 85,
        door_guid = 560
      )
      LocalObject(
        1399,
        IFFLock.Constructor(Vector3(3429.972f, 4013.418f, 90.54697f), Vector3(0, 0, 238)),
        owning_building_guid = 85,
        door_guid = 561
      )
      LocalObject(
        1400,
        IFFLock.Constructor(Vector3(3442.749f, 4026.225f, 70.54697f), Vector3(0, 0, 58)),
        owning_building_guid = 85,
        door_guid = 562
      )
      LocalObject(
        1401,
        IFFLock.Constructor(Vector3(3442.749f, 4026.225f, 90.54697f), Vector3(0, 0, 58)),
        owning_building_guid = 85,
        door_guid = 563
      )
      LocalObject(1786, Locker.Constructor(Vector3(3425.576f, 4008.704f, 59.07997f)), owning_building_guid = 85)
      LocalObject(1787, Locker.Constructor(Vector3(3426.285f, 4007.57f, 59.07997f)), owning_building_guid = 85)
      LocalObject(1788, Locker.Constructor(Vector3(3427.709f, 4005.29f, 59.07997f)), owning_building_guid = 85)
      LocalObject(1789, Locker.Constructor(Vector3(3428.452f, 4004.101f, 59.07997f)), owning_building_guid = 85)
      LocalObject(1790, Locker.Constructor(Vector3(3444.143f, 4020.264f, 59.07997f)), owning_building_guid = 85)
      LocalObject(1791, Locker.Constructor(Vector3(3444.852f, 4019.131f, 59.07997f)), owning_building_guid = 85)
      LocalObject(1792, Locker.Constructor(Vector3(3446.258f, 4016.881f, 59.07997f)), owning_building_guid = 85)
      LocalObject(1793, Locker.Constructor(Vector3(3447f, 4015.692f, 59.07997f)), owning_building_guid = 85)
      LocalObject(
        2433,
        Terminal.Constructor(Vector3(3432.993f, 4006.583f, 60.41797f), order_terminal),
        owning_building_guid = 85
      )
      LocalObject(
        2434,
        Terminal.Constructor(Vector3(3437.847f, 4009.616f, 60.41797f), order_terminal),
        owning_building_guid = 85
      )
      LocalObject(
        2435,
        Terminal.Constructor(Vector3(3442.411f, 4012.468f, 60.41797f), order_terminal),
        owning_building_guid = 85
      )
      LocalObject(
        3280,
        SpawnTube.Constructor(Vector3(3425.278f, 4014.425f, 58.56797f), respawn_tube_tower, Vector3(0, 0, 58)),
        owning_building_guid = 85
      )
      LocalObject(
        3281,
        SpawnTube.Constructor(Vector3(3439.194f, 4023.121f, 58.56797f), respawn_tube_tower, Vector3(0, 0, 58)),
        owning_building_guid = 85
      )
      LocalObject(
        2220,
        FacilityTurret.Constructor(Vector3(3412.506f, 4034.021f, 88.02797f), manned_turret),
        owning_building_guid = 85
      )
      TurretToWeapon(2220, 5113)
      LocalObject(
        2221,
        FacilityTurret.Constructor(Vector3(3452.777f, 4017.528f, 88.02797f), manned_turret),
        owning_building_guid = 85
      )
      TurretToWeapon(2221, 5114)
      LocalObject(
        3020,
        Painbox.Constructor(Vector3(3427.519f, 4022.277f, 60.58507f), painbox_radius_continuous),
        owning_building_guid = 85
      )
      LocalObject(
        3021,
        Painbox.Constructor(Vector3(3430.704f, 4010.423f, 59.18597f), painbox_radius_continuous),
        owning_building_guid = 85
      )
      LocalObject(
        3022,
        Painbox.Constructor(Vector3(3440.719f, 4016.783f, 59.18597f), painbox_radius_continuous),
        owning_building_guid = 85
      )
    }

    Building67()

    def Building67(): Unit = { // Name: S_Chuku_Tower Type: tower_a GUID: 86, MapID: 67
      LocalBuilding(
        "S_Chuku_Tower",
        86,
        67,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4008f, 5982f, 61.10294f), Vector3(0f, 0f, 0f), tower_a)
        )
      )
      LocalObject(
        3368,
        CaptureTerminal.Constructor(Vector3(4024.587f, 5981.897f, 71.10194f), secondary_capture),
        owning_building_guid = 86
      )
      LocalObject(594, Door.Constructor(Vector3(4020f, 5974f, 62.62394f)), owning_building_guid = 86)
      LocalObject(595, Door.Constructor(Vector3(4020f, 5974f, 82.62294f)), owning_building_guid = 86)
      LocalObject(596, Door.Constructor(Vector3(4020f, 5990f, 62.62394f)), owning_building_guid = 86)
      LocalObject(597, Door.Constructor(Vector3(4020f, 5990f, 82.62294f)), owning_building_guid = 86)
      LocalObject(3557, Door.Constructor(Vector3(4019.146f, 5970.794f, 52.43894f)), owning_building_guid = 86)
      LocalObject(3558, Door.Constructor(Vector3(4019.146f, 5987.204f, 52.43894f)), owning_building_guid = 86)
      LocalObject(
        1428,
        IFFLock.Constructor(Vector3(4017.957f, 5990.811f, 62.56394f), Vector3(0, 0, 0)),
        owning_building_guid = 86,
        door_guid = 596
      )
      LocalObject(
        1429,
        IFFLock.Constructor(Vector3(4017.957f, 5990.811f, 82.56394f), Vector3(0, 0, 0)),
        owning_building_guid = 86,
        door_guid = 597
      )
      LocalObject(
        1431,
        IFFLock.Constructor(Vector3(4022.047f, 5973.189f, 62.56394f), Vector3(0, 0, 180)),
        owning_building_guid = 86,
        door_guid = 594
      )
      LocalObject(
        1432,
        IFFLock.Constructor(Vector3(4022.047f, 5973.189f, 82.56394f), Vector3(0, 0, 180)),
        owning_building_guid = 86,
        door_guid = 595
      )
      LocalObject(1835, Locker.Constructor(Vector3(4023.716f, 5966.963f, 51.09694f)), owning_building_guid = 86)
      LocalObject(1836, Locker.Constructor(Vector3(4023.751f, 5988.835f, 51.09694f)), owning_building_guid = 86)
      LocalObject(1837, Locker.Constructor(Vector3(4025.053f, 5966.963f, 51.09694f)), owning_building_guid = 86)
      LocalObject(1838, Locker.Constructor(Vector3(4025.088f, 5988.835f, 51.09694f)), owning_building_guid = 86)
      LocalObject(1839, Locker.Constructor(Vector3(4027.741f, 5966.963f, 51.09694f)), owning_building_guid = 86)
      LocalObject(1840, Locker.Constructor(Vector3(4027.741f, 5988.835f, 51.09694f)), owning_building_guid = 86)
      LocalObject(1843, Locker.Constructor(Vector3(4029.143f, 5966.963f, 51.09694f)), owning_building_guid = 86)
      LocalObject(1844, Locker.Constructor(Vector3(4029.143f, 5988.835f, 51.09694f)), owning_building_guid = 86)
      LocalObject(
        2452,
        Terminal.Constructor(Vector3(4029.445f, 5972.129f, 52.43494f), order_terminal),
        owning_building_guid = 86
      )
      LocalObject(
        2453,
        Terminal.Constructor(Vector3(4029.445f, 5977.853f, 52.43494f), order_terminal),
        owning_building_guid = 86
      )
      LocalObject(
        2454,
        Terminal.Constructor(Vector3(4029.445f, 5983.234f, 52.43494f), order_terminal),
        owning_building_guid = 86
      )
      LocalObject(
        3293,
        SpawnTube.Constructor(Vector3(4018.706f, 5969.742f, 50.58494f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 86
      )
      LocalObject(
        3294,
        SpawnTube.Constructor(Vector3(4018.706f, 5986.152f, 50.58494f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 86
      )
      LocalObject(
        2227,
        FacilityTurret.Constructor(Vector3(3995.32f, 5969.295f, 80.04494f), manned_turret),
        owning_building_guid = 86
      )
      TurretToWeapon(2227, 5115)
      LocalObject(
        2230,
        FacilityTurret.Constructor(Vector3(4030.647f, 5994.707f, 80.04494f), manned_turret),
        owning_building_guid = 86
      )
      TurretToWeapon(2230, 5116)
      LocalObject(
        3035,
        Painbox.Constructor(Vector3(4013.235f, 5975.803f, 52.60204f), painbox_radius_continuous),
        owning_building_guid = 86
      )
      LocalObject(
        3036,
        Painbox.Constructor(Vector3(4024.889f, 5984.086f, 51.20294f), painbox_radius_continuous),
        owning_building_guid = 86
      )
      LocalObject(
        3037,
        Painbox.Constructor(Vector3(4024.975f, 5972.223f, 51.20294f), painbox_radius_continuous),
        owning_building_guid = 86
      )
    }

    Building33()

    def Building33(): Unit = { // Name: N_Gunuku_Tower Type: tower_a GUID: 87, MapID: 33
      LocalBuilding(
        "N_Gunuku_Tower",
        87,
        33,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4956f, 4786f, 44.49965f), Vector3(0f, 0f, 355f), tower_a)
        )
      )
      LocalObject(
        3370,
        CaptureTerminal.Constructor(Vector3(4972.515f, 4784.452f, 54.49865f), secondary_capture),
        owning_building_guid = 87
      )
      LocalObject(631, Door.Constructor(Vector3(4967.257f, 4776.984f, 46.02065f)), owning_building_guid = 87)
      LocalObject(632, Door.Constructor(Vector3(4967.257f, 4776.984f, 66.01965f)), owning_building_guid = 87)
      LocalObject(633, Door.Constructor(Vector3(4968.651f, 4792.924f, 46.02065f)), owning_building_guid = 87)
      LocalObject(634, Door.Constructor(Vector3(4968.651f, 4792.924f, 66.01965f)), owning_building_guid = 87)
      LocalObject(3564, Door.Constructor(Vector3(4966.127f, 4773.865f, 35.83565f)), owning_building_guid = 87)
      LocalObject(3565, Door.Constructor(Vector3(4967.557f, 4790.213f, 35.83565f)), owning_building_guid = 87)
      LocalObject(
        1452,
        IFFLock.Constructor(Vector3(4966.687f, 4793.91f, 45.96065f), Vector3(0, 0, 5)),
        owning_building_guid = 87,
        door_guid = 633
      )
      LocalObject(
        1453,
        IFFLock.Constructor(Vector3(4966.687f, 4793.91f, 65.96065f), Vector3(0, 0, 5)),
        owning_building_guid = 87,
        door_guid = 634
      )
      LocalObject(
        1454,
        IFFLock.Constructor(Vector3(4969.226f, 4775.998f, 45.96065f), Vector3(0, 0, 185)),
        owning_building_guid = 87,
        door_guid = 631
      )
      LocalObject(
        1455,
        IFFLock.Constructor(Vector3(4969.226f, 4775.998f, 65.96065f), Vector3(0, 0, 185)),
        owning_building_guid = 87,
        door_guid = 632
      )
      LocalObject(1884, Locker.Constructor(Vector3(4970.346f, 4769.65f, 34.49365f)), owning_building_guid = 87)
      LocalObject(1885, Locker.Constructor(Vector3(4971.678f, 4769.534f, 34.49365f)), owning_building_guid = 87)
      LocalObject(1886, Locker.Constructor(Vector3(4972.287f, 4791.436f, 34.49365f)), owning_building_guid = 87)
      LocalObject(1887, Locker.Constructor(Vector3(4973.619f, 4791.32f, 34.49365f)), owning_building_guid = 87)
      LocalObject(1888, Locker.Constructor(Vector3(4974.355f, 4769.3f, 34.49365f)), owning_building_guid = 87)
      LocalObject(1889, Locker.Constructor(Vector3(4975.752f, 4769.177f, 34.49365f)), owning_building_guid = 87)
      LocalObject(1890, Locker.Constructor(Vector3(4976.262f, 4791.088f, 34.49365f)), owning_building_guid = 87)
      LocalObject(1891, Locker.Constructor(Vector3(4977.658f, 4790.966f, 34.49365f)), owning_building_guid = 87)
      LocalObject(
        2467,
        Terminal.Constructor(Vector3(4976.503f, 4774.297f, 35.83165f), order_terminal),
        owning_building_guid = 87
      )
      LocalObject(
        2468,
        Terminal.Constructor(Vector3(4977.002f, 4780f, 35.83165f), order_terminal),
        owning_building_guid = 87
      )
      LocalObject(
        2469,
        Terminal.Constructor(Vector3(4977.471f, 4785.36f, 35.83165f), order_terminal),
        owning_building_guid = 87
      )
      LocalObject(
        3300,
        SpawnTube.Constructor(Vector3(4965.597f, 4772.855f, 33.98165f), respawn_tube_tower, Vector3(0, 0, 5)),
        owning_building_guid = 87
      )
      LocalObject(
        3301,
        SpawnTube.Constructor(Vector3(4967.027f, 4789.203f, 33.98165f), respawn_tube_tower, Vector3(0, 0, 5)),
        owning_building_guid = 87
      )
      LocalObject(
        2241,
        FacilityTurret.Constructor(Vector3(4942.261f, 4774.448f, 63.44165f), manned_turret),
        owning_building_guid = 87
      )
      TurretToWeapon(2241, 5117)
      LocalObject(
        2244,
        FacilityTurret.Constructor(Vector3(4979.668f, 4796.685f, 63.44165f), manned_turret),
        owning_building_guid = 87
      )
      TurretToWeapon(2244, 5118)
      LocalObject(
        3041,
        Painbox.Constructor(Vector3(4960.675f, 4779.37f, 35.99875f), painbox_radius_continuous),
        owning_building_guid = 87
      )
      LocalObject(
        3042,
        Painbox.Constructor(Vector3(4972.058f, 4774.78f, 34.59965f), painbox_radius_continuous),
        owning_building_guid = 87
      )
      LocalObject(
        3043,
        Painbox.Constructor(Vector3(4973.007f, 4786.606f, 34.59965f), painbox_radius_continuous),
        owning_building_guid = 87
      )
    }

    Building38()

    def Building38(): Unit = { // Name: W_Ekera_Tower Type: tower_a GUID: 88, MapID: 38
      LocalBuilding(
        "W_Ekera_Tower",
        88,
        38,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5432f, 6740f, 70.09491f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        3374,
        CaptureTerminal.Constructor(Vector3(5448.587f, 6739.897f, 80.09391f), secondary_capture),
        owning_building_guid = 88
      )
      LocalObject(689, Door.Constructor(Vector3(5444f, 6732f, 71.61591f)), owning_building_guid = 88)
      LocalObject(690, Door.Constructor(Vector3(5444f, 6732f, 91.61491f)), owning_building_guid = 88)
      LocalObject(691, Door.Constructor(Vector3(5444f, 6748f, 71.61591f)), owning_building_guid = 88)
      LocalObject(692, Door.Constructor(Vector3(5444f, 6748f, 91.61491f)), owning_building_guid = 88)
      LocalObject(3581, Door.Constructor(Vector3(5443.146f, 6728.794f, 61.43091f)), owning_building_guid = 88)
      LocalObject(3582, Door.Constructor(Vector3(5443.146f, 6745.204f, 61.43091f)), owning_building_guid = 88)
      LocalObject(
        1498,
        IFFLock.Constructor(Vector3(5441.957f, 6748.811f, 71.55591f), Vector3(0, 0, 0)),
        owning_building_guid = 88,
        door_guid = 691
      )
      LocalObject(
        1499,
        IFFLock.Constructor(Vector3(5441.957f, 6748.811f, 91.55591f), Vector3(0, 0, 0)),
        owning_building_guid = 88,
        door_guid = 692
      )
      LocalObject(
        1500,
        IFFLock.Constructor(Vector3(5446.047f, 6731.189f, 71.55591f), Vector3(0, 0, 180)),
        owning_building_guid = 88,
        door_guid = 689
      )
      LocalObject(
        1501,
        IFFLock.Constructor(Vector3(5446.047f, 6731.189f, 91.55591f), Vector3(0, 0, 180)),
        owning_building_guid = 88,
        door_guid = 690
      )
      LocalObject(1961, Locker.Constructor(Vector3(5447.716f, 6724.963f, 60.08891f)), owning_building_guid = 88)
      LocalObject(1962, Locker.Constructor(Vector3(5447.751f, 6746.835f, 60.08891f)), owning_building_guid = 88)
      LocalObject(1963, Locker.Constructor(Vector3(5449.053f, 6724.963f, 60.08891f)), owning_building_guid = 88)
      LocalObject(1964, Locker.Constructor(Vector3(5449.088f, 6746.835f, 60.08891f)), owning_building_guid = 88)
      LocalObject(1965, Locker.Constructor(Vector3(5451.741f, 6724.963f, 60.08891f)), owning_building_guid = 88)
      LocalObject(1966, Locker.Constructor(Vector3(5451.741f, 6746.835f, 60.08891f)), owning_building_guid = 88)
      LocalObject(1967, Locker.Constructor(Vector3(5453.143f, 6724.963f, 60.08891f)), owning_building_guid = 88)
      LocalObject(1968, Locker.Constructor(Vector3(5453.143f, 6746.835f, 60.08891f)), owning_building_guid = 88)
      LocalObject(
        2490,
        Terminal.Constructor(Vector3(5453.445f, 6730.129f, 61.42691f), order_terminal),
        owning_building_guid = 88
      )
      LocalObject(
        2491,
        Terminal.Constructor(Vector3(5453.445f, 6735.853f, 61.42691f), order_terminal),
        owning_building_guid = 88
      )
      LocalObject(
        2492,
        Terminal.Constructor(Vector3(5453.445f, 6741.234f, 61.42691f), order_terminal),
        owning_building_guid = 88
      )
      LocalObject(
        3317,
        SpawnTube.Constructor(Vector3(5442.706f, 6727.742f, 59.57691f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 88
      )
      LocalObject(
        3318,
        SpawnTube.Constructor(Vector3(5442.706f, 6744.152f, 59.57691f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 88
      )
      LocalObject(
        2261,
        FacilityTurret.Constructor(Vector3(5419.32f, 6727.295f, 89.03691f), manned_turret),
        owning_building_guid = 88
      )
      TurretToWeapon(2261, 5119)
      LocalObject(
        2262,
        FacilityTurret.Constructor(Vector3(5454.647f, 6752.707f, 89.03691f), manned_turret),
        owning_building_guid = 88
      )
      TurretToWeapon(2262, 5120)
      LocalObject(
        3053,
        Painbox.Constructor(Vector3(5437.235f, 6733.803f, 61.59401f), painbox_radius_continuous),
        owning_building_guid = 88
      )
      LocalObject(
        3054,
        Painbox.Constructor(Vector3(5448.889f, 6742.086f, 60.19491f), painbox_radius_continuous),
        owning_building_guid = 88
      )
      LocalObject(
        3055,
        Painbox.Constructor(Vector3(5448.975f, 6730.223f, 60.19491f), painbox_radius_continuous),
        owning_building_guid = 88
      )
    }

    Building31()

    def Building31(): Unit = { // Name: NW_TRSanc_Warpgate_Tower Type: tower_a GUID: 89, MapID: 31
      LocalBuilding(
        "NW_TRSanc_Warpgate_Tower",
        89,
        31,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5580f, 2080f, 59.54527f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        3375,
        CaptureTerminal.Constructor(Vector3(5596.587f, 2079.897f, 69.54427f), secondary_capture),
        owning_building_guid = 89
      )
      LocalObject(697, Door.Constructor(Vector3(5592f, 2072f, 61.06627f)), owning_building_guid = 89)
      LocalObject(698, Door.Constructor(Vector3(5592f, 2072f, 81.06528f)), owning_building_guid = 89)
      LocalObject(699, Door.Constructor(Vector3(5592f, 2088f, 61.06627f)), owning_building_guid = 89)
      LocalObject(700, Door.Constructor(Vector3(5592f, 2088f, 81.06528f)), owning_building_guid = 89)
      LocalObject(3583, Door.Constructor(Vector3(5591.146f, 2068.794f, 50.88127f)), owning_building_guid = 89)
      LocalObject(3584, Door.Constructor(Vector3(5591.146f, 2085.204f, 50.88127f)), owning_building_guid = 89)
      LocalObject(
        1502,
        IFFLock.Constructor(Vector3(5589.957f, 2088.811f, 61.00627f), Vector3(0, 0, 0)),
        owning_building_guid = 89,
        door_guid = 699
      )
      LocalObject(
        1503,
        IFFLock.Constructor(Vector3(5589.957f, 2088.811f, 81.00627f), Vector3(0, 0, 0)),
        owning_building_guid = 89,
        door_guid = 700
      )
      LocalObject(
        1504,
        IFFLock.Constructor(Vector3(5594.047f, 2071.189f, 61.00627f), Vector3(0, 0, 180)),
        owning_building_guid = 89,
        door_guid = 697
      )
      LocalObject(
        1505,
        IFFLock.Constructor(Vector3(5594.047f, 2071.189f, 81.00627f), Vector3(0, 0, 180)),
        owning_building_guid = 89,
        door_guid = 698
      )
      LocalObject(1969, Locker.Constructor(Vector3(5595.716f, 2064.963f, 49.53927f)), owning_building_guid = 89)
      LocalObject(1970, Locker.Constructor(Vector3(5595.751f, 2086.835f, 49.53927f)), owning_building_guid = 89)
      LocalObject(1971, Locker.Constructor(Vector3(5597.053f, 2064.963f, 49.53927f)), owning_building_guid = 89)
      LocalObject(1972, Locker.Constructor(Vector3(5597.088f, 2086.835f, 49.53927f)), owning_building_guid = 89)
      LocalObject(1973, Locker.Constructor(Vector3(5599.741f, 2064.963f, 49.53927f)), owning_building_guid = 89)
      LocalObject(1974, Locker.Constructor(Vector3(5599.741f, 2086.835f, 49.53927f)), owning_building_guid = 89)
      LocalObject(1975, Locker.Constructor(Vector3(5601.143f, 2064.963f, 49.53927f)), owning_building_guid = 89)
      LocalObject(1976, Locker.Constructor(Vector3(5601.143f, 2086.835f, 49.53927f)), owning_building_guid = 89)
      LocalObject(
        2493,
        Terminal.Constructor(Vector3(5601.445f, 2070.129f, 50.87727f), order_terminal),
        owning_building_guid = 89
      )
      LocalObject(
        2494,
        Terminal.Constructor(Vector3(5601.445f, 2075.853f, 50.87727f), order_terminal),
        owning_building_guid = 89
      )
      LocalObject(
        2495,
        Terminal.Constructor(Vector3(5601.445f, 2081.234f, 50.87727f), order_terminal),
        owning_building_guid = 89
      )
      LocalObject(
        3319,
        SpawnTube.Constructor(Vector3(5590.706f, 2067.742f, 49.02728f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 89
      )
      LocalObject(
        3320,
        SpawnTube.Constructor(Vector3(5590.706f, 2084.152f, 49.02728f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 89
      )
      LocalObject(
        2266,
        FacilityTurret.Constructor(Vector3(5567.32f, 2067.295f, 78.48727f), manned_turret),
        owning_building_guid = 89
      )
      TurretToWeapon(2266, 5121)
      LocalObject(
        2267,
        FacilityTurret.Constructor(Vector3(5602.647f, 2092.707f, 78.48727f), manned_turret),
        owning_building_guid = 89
      )
      TurretToWeapon(2267, 5122)
      LocalObject(
        3056,
        Painbox.Constructor(Vector3(5585.235f, 2073.803f, 51.04437f), painbox_radius_continuous),
        owning_building_guid = 89
      )
      LocalObject(
        3057,
        Painbox.Constructor(Vector3(5596.889f, 2082.086f, 49.64527f), painbox_radius_continuous),
        owning_building_guid = 89
      )
      LocalObject(
        3058,
        Painbox.Constructor(Vector3(5596.975f, 2070.223f, 49.64527f), painbox_radius_continuous),
        owning_building_guid = 89
      )
    }

    Building43()

    def Building43(): Unit = { // Name: S_Kaang_Tower Type: tower_a GUID: 90, MapID: 43
      LocalBuilding(
        "S_Kaang_Tower",
        90,
        43,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5878f, 3642f, 93.63689f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        3377,
        CaptureTerminal.Constructor(Vector3(5894.587f, 3641.897f, 103.6359f), secondary_capture),
        owning_building_guid = 90
      )
      LocalObject(731, Door.Constructor(Vector3(5890f, 3634f, 95.1579f)), owning_building_guid = 90)
      LocalObject(732, Door.Constructor(Vector3(5890f, 3634f, 115.1569f)), owning_building_guid = 90)
      LocalObject(733, Door.Constructor(Vector3(5890f, 3650f, 95.1579f)), owning_building_guid = 90)
      LocalObject(734, Door.Constructor(Vector3(5890f, 3650f, 115.1569f)), owning_building_guid = 90)
      LocalObject(3593, Door.Constructor(Vector3(5889.146f, 3630.794f, 84.97289f)), owning_building_guid = 90)
      LocalObject(3594, Door.Constructor(Vector3(5889.146f, 3647.204f, 84.97289f)), owning_building_guid = 90)
      LocalObject(
        1531,
        IFFLock.Constructor(Vector3(5887.957f, 3650.811f, 95.09789f), Vector3(0, 0, 0)),
        owning_building_guid = 90,
        door_guid = 733
      )
      LocalObject(
        1532,
        IFFLock.Constructor(Vector3(5887.957f, 3650.811f, 115.0979f), Vector3(0, 0, 0)),
        owning_building_guid = 90,
        door_guid = 734
      )
      LocalObject(
        1533,
        IFFLock.Constructor(Vector3(5892.047f, 3633.189f, 95.09789f), Vector3(0, 0, 180)),
        owning_building_guid = 90,
        door_guid = 731
      )
      LocalObject(
        1534,
        IFFLock.Constructor(Vector3(5892.047f, 3633.189f, 115.0979f), Vector3(0, 0, 180)),
        owning_building_guid = 90,
        door_guid = 732
      )
      LocalObject(2009, Locker.Constructor(Vector3(5893.716f, 3626.963f, 83.6309f)), owning_building_guid = 90)
      LocalObject(2010, Locker.Constructor(Vector3(5893.751f, 3648.835f, 83.6309f)), owning_building_guid = 90)
      LocalObject(2011, Locker.Constructor(Vector3(5895.053f, 3626.963f, 83.6309f)), owning_building_guid = 90)
      LocalObject(2012, Locker.Constructor(Vector3(5895.088f, 3648.835f, 83.6309f)), owning_building_guid = 90)
      LocalObject(2013, Locker.Constructor(Vector3(5897.741f, 3626.963f, 83.6309f)), owning_building_guid = 90)
      LocalObject(2014, Locker.Constructor(Vector3(5897.741f, 3648.835f, 83.6309f)), owning_building_guid = 90)
      LocalObject(2015, Locker.Constructor(Vector3(5899.143f, 3626.963f, 83.6309f)), owning_building_guid = 90)
      LocalObject(2016, Locker.Constructor(Vector3(5899.143f, 3648.835f, 83.6309f)), owning_building_guid = 90)
      LocalObject(
        2511,
        Terminal.Constructor(Vector3(5899.445f, 3632.129f, 84.96889f), order_terminal),
        owning_building_guid = 90
      )
      LocalObject(
        2512,
        Terminal.Constructor(Vector3(5899.445f, 3637.853f, 84.96889f), order_terminal),
        owning_building_guid = 90
      )
      LocalObject(
        2513,
        Terminal.Constructor(Vector3(5899.445f, 3643.234f, 84.96889f), order_terminal),
        owning_building_guid = 90
      )
      LocalObject(
        3329,
        SpawnTube.Constructor(Vector3(5888.706f, 3629.742f, 83.1189f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 90
      )
      LocalObject(
        3330,
        SpawnTube.Constructor(Vector3(5888.706f, 3646.152f, 83.1189f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 90
      )
      LocalObject(
        2277,
        FacilityTurret.Constructor(Vector3(5865.32f, 3629.295f, 112.5789f), manned_turret),
        owning_building_guid = 90
      )
      TurretToWeapon(2277, 5123)
      LocalObject(
        2279,
        FacilityTurret.Constructor(Vector3(5900.647f, 3654.707f, 112.5789f), manned_turret),
        owning_building_guid = 90
      )
      TurretToWeapon(2279, 5124)
      LocalObject(
        3062,
        Painbox.Constructor(Vector3(5883.235f, 3635.803f, 85.13599f), painbox_radius_continuous),
        owning_building_guid = 90
      )
      LocalObject(
        3063,
        Painbox.Constructor(Vector3(5894.889f, 3644.086f, 83.73689f), painbox_radius_continuous),
        owning_building_guid = 90
      )
      LocalObject(
        3064,
        Painbox.Constructor(Vector3(5894.975f, 3632.223f, 83.73689f), painbox_radius_continuous),
        owning_building_guid = 90
      )
    }

    Building41()

    def Building41(): Unit = { // Name: NW_Pamba_Tower Type: tower_a GUID: 91, MapID: 41
      LocalBuilding(
        "NW_Pamba_Tower",
        91,
        41,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6626f, 3680f, 47.49107f), Vector3(0f, 0f, 18f), tower_a)
        )
      )
      LocalObject(
        3378,
        CaptureTerminal.Constructor(Vector3(6641.807f, 3685.028f, 57.49007f), secondary_capture),
        owning_building_guid = 91
      )
      LocalObject(741, Door.Constructor(Vector3(6634.94f, 3691.317f, 49.01207f)), owning_building_guid = 91)
      LocalObject(742, Door.Constructor(Vector3(6634.94f, 3691.317f, 69.01108f)), owning_building_guid = 91)
      LocalObject(743, Door.Constructor(Vector3(6639.885f, 3676.1f, 49.01207f)), owning_building_guid = 91)
      LocalObject(744, Door.Constructor(Vector3(6639.885f, 3676.1f, 69.01108f)), owning_building_guid = 91)
      LocalObject(3595, Door.Constructor(Vector3(6634.992f, 3688.394f, 38.82707f)), owning_building_guid = 91)
      LocalObject(3596, Door.Constructor(Vector3(6640.063f, 3672.787f, 38.82707f)), owning_building_guid = 91)
      LocalObject(
        1536,
        IFFLock.Constructor(Vector3(6632.747f, 3691.457f, 48.95207f), Vector3(0, 0, 342)),
        owning_building_guid = 91,
        door_guid = 741
      )
      LocalObject(
        1537,
        IFFLock.Constructor(Vector3(6632.747f, 3691.457f, 68.95207f), Vector3(0, 0, 342)),
        owning_building_guid = 91,
        door_guid = 742
      )
      LocalObject(
        1538,
        IFFLock.Constructor(Vector3(6642.082f, 3675.961f, 48.95207f), Vector3(0, 0, 162)),
        owning_building_guid = 91,
        door_guid = 743
      )
      LocalObject(
        1539,
        IFFLock.Constructor(Vector3(6642.082f, 3675.961f, 68.95207f), Vector3(0, 0, 162)),
        owning_building_guid = 91,
        door_guid = 744
      )
      LocalObject(2017, Locker.Constructor(Vector3(6638.868f, 3691.368f, 37.48507f)), owning_building_guid = 91)
      LocalObject(2018, Locker.Constructor(Vector3(6640.14f, 3691.781f, 37.48507f)), owning_building_guid = 91)
      LocalObject(2019, Locker.Constructor(Vector3(6642.663f, 3692.601f, 37.48507f)), owning_building_guid = 91)
      LocalObject(2020, Locker.Constructor(Vector3(6643.996f, 3693.034f, 37.48507f)), owning_building_guid = 91)
      LocalObject(2021, Locker.Constructor(Vector3(6645.593f, 3670.555f, 37.48507f)), owning_building_guid = 91)
      LocalObject(2022, Locker.Constructor(Vector3(6646.865f, 3670.969f, 37.48507f)), owning_building_guid = 91)
      LocalObject(2023, Locker.Constructor(Vector3(6649.421f, 3671.799f, 37.48507f)), owning_building_guid = 91)
      LocalObject(2024, Locker.Constructor(Vector3(6650.755f, 3672.232f, 37.48507f)), owning_building_guid = 91)
      LocalObject(
        2514,
        Terminal.Constructor(Vector3(6646.014f, 3687.801f, 38.82307f), order_terminal),
        owning_building_guid = 91
      )
      LocalObject(
        2515,
        Terminal.Constructor(Vector3(6647.677f, 3682.683f, 38.82307f), order_terminal),
        owning_building_guid = 91
      )
      LocalObject(
        2516,
        Terminal.Constructor(Vector3(6649.446f, 3677.239f, 38.82307f), order_terminal),
        owning_building_guid = 91
      )
      LocalObject(
        3331,
        SpawnTube.Constructor(Vector3(6634.899f, 3687.257f, 36.97308f), respawn_tube_tower, Vector3(0, 0, 342)),
        owning_building_guid = 91
      )
      LocalObject(
        3332,
        SpawnTube.Constructor(Vector3(6639.97f, 3671.65f, 36.97308f), respawn_tube_tower, Vector3(0, 0, 342)),
        owning_building_guid = 91
      )
      LocalObject(
        2281,
        FacilityTurret.Constructor(Vector3(6617.867f, 3663.999f, 66.43307f), manned_turret),
        owning_building_guid = 91
      )
      TurretToWeapon(2281, 5125)
      LocalObject(
        2282,
        FacilityTurret.Constructor(Vector3(6643.612f, 3699.083f, 66.43307f), manned_turret),
        owning_building_guid = 91
      )
      TurretToWeapon(2282, 5126)
      LocalObject(
        3065,
        Painbox.Constructor(Vector3(6632.894f, 3675.724f, 38.99017f), painbox_radius_continuous),
        owning_building_guid = 91
      )
      LocalObject(
        3066,
        Painbox.Constructor(Vector3(6641.418f, 3687.203f, 37.59107f), painbox_radius_continuous),
        owning_building_guid = 91
      )
      LocalObject(
        3067,
        Painbox.Constructor(Vector3(6645.166f, 3675.947f, 37.59107f), painbox_radius_continuous),
        owning_building_guid = 91
      )
    }

    Building65()

    def Building65(): Unit = { // Name: E_Shango_Tower Type: tower_a GUID: 92, MapID: 65
      LocalBuilding(
        "E_Shango_Tower",
        92,
        65,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6918f, 2176f, 78.13105f), Vector3(0f, 0f, 317f), tower_a)
        )
      )
      LocalObject(
        3380,
        CaptureTerminal.Constructor(Vector3(6930.061f, 2164.612f, 88.13005f), secondary_capture),
        owning_building_guid = 92
      )
      LocalObject(763, Door.Constructor(Vector3(6921.32f, 2161.965f, 79.65205f)), owning_building_guid = 92)
      LocalObject(764, Door.Constructor(Vector3(6921.32f, 2161.965f, 99.65105f)), owning_building_guid = 92)
      LocalObject(765, Door.Constructor(Vector3(6932.232f, 2173.667f, 79.65205f)), owning_building_guid = 92)
      LocalObject(766, Door.Constructor(Vector3(6932.232f, 2173.667f, 99.65105f)), owning_building_guid = 92)
      LocalObject(3602, Door.Constructor(Vector3(6918.509f, 2160.203f, 69.46705f)), owning_building_guid = 92)
      LocalObject(3603, Door.Constructor(Vector3(6929.701f, 2172.204f, 69.46705f)), owning_building_guid = 92)
      LocalObject(
        1554,
        IFFLock.Constructor(Vector3(6922.264f, 2159.976f, 79.59205f), Vector3(0, 0, 223)),
        owning_building_guid = 92,
        door_guid = 763
      )
      LocalObject(
        1555,
        IFFLock.Constructor(Vector3(6922.264f, 2159.976f, 99.59205f), Vector3(0, 0, 223)),
        owning_building_guid = 92,
        door_guid = 764
      )
      LocalObject(
        1556,
        IFFLock.Constructor(Vector3(6931.291f, 2175.653f, 79.59205f), Vector3(0, 0, 43)),
        owning_building_guid = 92,
        door_guid = 765
      )
      LocalObject(
        1557,
        IFFLock.Constructor(Vector3(6931.291f, 2175.653f, 99.59205f), Vector3(0, 0, 43)),
        owning_building_guid = 92,
        door_guid = 766
      )
      LocalObject(2054, Locker.Constructor(Vector3(6919.239f, 2154.284f, 68.12505f)), owning_building_guid = 92)
      LocalObject(2055, Locker.Constructor(Vector3(6920.217f, 2153.373f, 68.12505f)), owning_building_guid = 92)
      LocalObject(2056, Locker.Constructor(Vector3(6922.183f, 2151.539f, 68.12505f)), owning_building_guid = 92)
      LocalObject(2057, Locker.Constructor(Vector3(6923.208f, 2150.583f, 68.12505f)), owning_building_guid = 92)
      LocalObject(2058, Locker.Constructor(Vector3(6934.181f, 2170.257f, 68.12505f)), owning_building_guid = 92)
      LocalObject(2059, Locker.Constructor(Vector3(6935.159f, 2169.345f, 68.12505f)), owning_building_guid = 92)
      LocalObject(2060, Locker.Constructor(Vector3(6937.099f, 2167.535f, 68.12505f)), owning_building_guid = 92)
      LocalObject(2061, Locker.Constructor(Vector3(6938.125f, 2166.579f, 68.12505f)), owning_building_guid = 92)
      LocalObject(
        2524,
        Terminal.Constructor(Vector3(6926.952f, 2154.155f, 69.46305f), order_terminal),
        owning_building_guid = 92
      )
      LocalObject(
        2525,
        Terminal.Constructor(Vector3(6930.855f, 2158.342f, 69.46305f), order_terminal),
        owning_building_guid = 92
      )
      LocalObject(
        2526,
        Terminal.Constructor(Vector3(6934.525f, 2162.277f, 69.46305f), order_terminal),
        owning_building_guid = 92
      )
      LocalObject(
        3338,
        SpawnTube.Constructor(Vector3(6917.47f, 2159.734f, 67.61305f), respawn_tube_tower, Vector3(0, 0, 43)),
        owning_building_guid = 92
      )
      LocalObject(
        3339,
        SpawnTube.Constructor(Vector3(6928.662f, 2171.735f, 67.61305f), respawn_tube_tower, Vector3(0, 0, 43)),
        owning_building_guid = 92
      )
      LocalObject(
        2290,
        FacilityTurret.Constructor(Vector3(6900.062f, 2175.356f, 97.07305f), manned_turret),
        owning_building_guid = 92
      )
      TurretToWeapon(2290, 5127)
      LocalObject(
        2291,
        FacilityTurret.Constructor(Vector3(6943.229f, 2169.848f, 97.07305f), manned_turret),
        owning_building_guid = 92
      )
      TurretToWeapon(2291, 5128)
      LocalObject(
        3071,
        Painbox.Constructor(Vector3(6917.602f, 2167.897f, 69.63015f), painbox_radius_continuous),
        owning_building_guid = 92
      )
      LocalObject(
        3072,
        Painbox.Constructor(Vector3(6923.747f, 2157.272f, 68.23105f), painbox_radius_continuous),
        owning_building_guid = 92
      )
      LocalObject(
        3073,
        Painbox.Constructor(Vector3(6931.775f, 2166.007f, 68.23105f), painbox_radius_continuous),
        owning_building_guid = 92
      )
    }

    Building23()

    def Building23(): Unit = { // Name: N_Aja_Tower Type: tower_b GUID: 93, MapID: 23
      LocalBuilding(
        "N_Aja_Tower",
        93,
        23,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(602f, 5716f, 58.60714f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        3350,
        CaptureTerminal.Constructor(Vector3(618.587f, 5715.897f, 78.60614f), secondary_capture),
        owning_building_guid = 93
      )
      LocalObject(422, Door.Constructor(Vector3(614f, 5708f, 60.12714f)), owning_building_guid = 93)
      LocalObject(423, Door.Constructor(Vector3(614f, 5708f, 70.12714f)), owning_building_guid = 93)
      LocalObject(424, Door.Constructor(Vector3(614f, 5708f, 90.12714f)), owning_building_guid = 93)
      LocalObject(425, Door.Constructor(Vector3(614f, 5724f, 60.12714f)), owning_building_guid = 93)
      LocalObject(426, Door.Constructor(Vector3(614f, 5724f, 70.12714f)), owning_building_guid = 93)
      LocalObject(427, Door.Constructor(Vector3(614f, 5724f, 90.12714f)), owning_building_guid = 93)
      LocalObject(3503, Door.Constructor(Vector3(613.147f, 5704.794f, 49.94314f)), owning_building_guid = 93)
      LocalObject(3504, Door.Constructor(Vector3(613.147f, 5721.204f, 49.94314f)), owning_building_guid = 93)
      LocalObject(
        1286,
        IFFLock.Constructor(Vector3(611.957f, 5724.811f, 60.06813f), Vector3(0, 0, 0)),
        owning_building_guid = 93,
        door_guid = 425
      )
      LocalObject(
        1287,
        IFFLock.Constructor(Vector3(611.957f, 5724.811f, 70.06814f), Vector3(0, 0, 0)),
        owning_building_guid = 93,
        door_guid = 426
      )
      LocalObject(
        1288,
        IFFLock.Constructor(Vector3(611.957f, 5724.811f, 90.06814f), Vector3(0, 0, 0)),
        owning_building_guid = 93,
        door_guid = 427
      )
      LocalObject(
        1289,
        IFFLock.Constructor(Vector3(616.047f, 5707.189f, 60.06813f), Vector3(0, 0, 180)),
        owning_building_guid = 93,
        door_guid = 422
      )
      LocalObject(
        1290,
        IFFLock.Constructor(Vector3(616.047f, 5707.189f, 70.06814f), Vector3(0, 0, 180)),
        owning_building_guid = 93,
        door_guid = 423
      )
      LocalObject(
        1291,
        IFFLock.Constructor(Vector3(616.047f, 5707.189f, 90.06814f), Vector3(0, 0, 180)),
        owning_building_guid = 93,
        door_guid = 424
      )
      LocalObject(1611, Locker.Constructor(Vector3(617.716f, 5700.963f, 48.60114f)), owning_building_guid = 93)
      LocalObject(1612, Locker.Constructor(Vector3(617.751f, 5722.835f, 48.60114f)), owning_building_guid = 93)
      LocalObject(1613, Locker.Constructor(Vector3(619.053f, 5700.963f, 48.60114f)), owning_building_guid = 93)
      LocalObject(1614, Locker.Constructor(Vector3(619.088f, 5722.835f, 48.60114f)), owning_building_guid = 93)
      LocalObject(1621, Locker.Constructor(Vector3(621.741f, 5700.963f, 48.60114f)), owning_building_guid = 93)
      LocalObject(1622, Locker.Constructor(Vector3(621.741f, 5722.835f, 48.60114f)), owning_building_guid = 93)
      LocalObject(1623, Locker.Constructor(Vector3(623.143f, 5700.963f, 48.60114f)), owning_building_guid = 93)
      LocalObject(1624, Locker.Constructor(Vector3(623.143f, 5722.835f, 48.60114f)), owning_building_guid = 93)
      LocalObject(
        2364,
        Terminal.Constructor(Vector3(623.446f, 5706.129f, 49.93914f), order_terminal),
        owning_building_guid = 93
      )
      LocalObject(
        2365,
        Terminal.Constructor(Vector3(623.446f, 5711.853f, 49.93914f), order_terminal),
        owning_building_guid = 93
      )
      LocalObject(
        2366,
        Terminal.Constructor(Vector3(623.446f, 5717.234f, 49.93914f), order_terminal),
        owning_building_guid = 93
      )
      LocalObject(
        3239,
        SpawnTube.Constructor(Vector3(612.706f, 5703.742f, 48.08913f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 93
      )
      LocalObject(
        3240,
        SpawnTube.Constructor(Vector3(612.706f, 5720.152f, 48.08913f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 93
      )
      LocalObject(
        2981,
        Painbox.Constructor(Vector3(607.4928f, 5708.849f, 49.89654f), painbox_radius_continuous),
        owning_building_guid = 93
      )
      LocalObject(
        2982,
        Painbox.Constructor(Vector3(619.1271f, 5706.078f, 48.70714f), painbox_radius_continuous),
        owning_building_guid = 93
      )
      LocalObject(
        2983,
        Painbox.Constructor(Vector3(619.2594f, 5718.107f, 48.70714f), painbox_radius_continuous),
        owning_building_guid = 93
      )
    }

    Building63()

    def Building63(): Unit = { // Name: E_Mukuru_Tower Type: tower_b GUID: 94, MapID: 63
      LocalBuilding(
        "E_Mukuru_Tower",
        94,
        63,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(870f, 2290f, 48.92483f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        3353,
        CaptureTerminal.Constructor(Vector3(886.587f, 2289.897f, 68.92383f), secondary_capture),
        owning_building_guid = 94
      )
      LocalObject(455, Door.Constructor(Vector3(882f, 2282f, 50.44483f)), owning_building_guid = 94)
      LocalObject(456, Door.Constructor(Vector3(882f, 2282f, 60.44483f)), owning_building_guid = 94)
      LocalObject(457, Door.Constructor(Vector3(882f, 2282f, 80.44483f)), owning_building_guid = 94)
      LocalObject(458, Door.Constructor(Vector3(882f, 2298f, 50.44483f)), owning_building_guid = 94)
      LocalObject(459, Door.Constructor(Vector3(882f, 2298f, 60.44483f)), owning_building_guid = 94)
      LocalObject(460, Door.Constructor(Vector3(882f, 2298f, 80.44483f)), owning_building_guid = 94)
      LocalObject(3512, Door.Constructor(Vector3(881.147f, 2278.794f, 40.26083f)), owning_building_guid = 94)
      LocalObject(3513, Door.Constructor(Vector3(881.147f, 2295.204f, 40.26083f)), owning_building_guid = 94)
      LocalObject(
        1313,
        IFFLock.Constructor(Vector3(879.957f, 2298.811f, 50.38583f), Vector3(0, 0, 0)),
        owning_building_guid = 94,
        door_guid = 458
      )
      LocalObject(
        1314,
        IFFLock.Constructor(Vector3(879.957f, 2298.811f, 60.38583f), Vector3(0, 0, 0)),
        owning_building_guid = 94,
        door_guid = 459
      )
      LocalObject(
        1315,
        IFFLock.Constructor(Vector3(879.957f, 2298.811f, 80.38583f), Vector3(0, 0, 0)),
        owning_building_guid = 94,
        door_guid = 460
      )
      LocalObject(
        1316,
        IFFLock.Constructor(Vector3(884.047f, 2281.189f, 50.38583f), Vector3(0, 0, 180)),
        owning_building_guid = 94,
        door_guid = 455
      )
      LocalObject(
        1317,
        IFFLock.Constructor(Vector3(884.047f, 2281.189f, 60.38583f), Vector3(0, 0, 180)),
        owning_building_guid = 94,
        door_guid = 456
      )
      LocalObject(
        1318,
        IFFLock.Constructor(Vector3(884.047f, 2281.189f, 80.38583f), Vector3(0, 0, 180)),
        owning_building_guid = 94,
        door_guid = 457
      )
      LocalObject(1658, Locker.Constructor(Vector3(885.716f, 2274.963f, 38.91883f)), owning_building_guid = 94)
      LocalObject(1659, Locker.Constructor(Vector3(885.751f, 2296.835f, 38.91883f)), owning_building_guid = 94)
      LocalObject(1660, Locker.Constructor(Vector3(887.053f, 2274.963f, 38.91883f)), owning_building_guid = 94)
      LocalObject(1661, Locker.Constructor(Vector3(887.088f, 2296.835f, 38.91883f)), owning_building_guid = 94)
      LocalObject(1662, Locker.Constructor(Vector3(889.741f, 2274.963f, 38.91883f)), owning_building_guid = 94)
      LocalObject(1663, Locker.Constructor(Vector3(889.741f, 2296.835f, 38.91883f)), owning_building_guid = 94)
      LocalObject(1664, Locker.Constructor(Vector3(891.143f, 2274.963f, 38.91883f)), owning_building_guid = 94)
      LocalObject(1665, Locker.Constructor(Vector3(891.143f, 2296.835f, 38.91883f)), owning_building_guid = 94)
      LocalObject(
        2379,
        Terminal.Constructor(Vector3(891.446f, 2280.129f, 40.25683f), order_terminal),
        owning_building_guid = 94
      )
      LocalObject(
        2380,
        Terminal.Constructor(Vector3(891.446f, 2285.853f, 40.25683f), order_terminal),
        owning_building_guid = 94
      )
      LocalObject(
        2381,
        Terminal.Constructor(Vector3(891.446f, 2291.234f, 40.25683f), order_terminal),
        owning_building_guid = 94
      )
      LocalObject(
        3248,
        SpawnTube.Constructor(Vector3(880.706f, 2277.742f, 38.40683f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 94
      )
      LocalObject(
        3249,
        SpawnTube.Constructor(Vector3(880.706f, 2294.152f, 38.40683f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 94
      )
      LocalObject(
        2990,
        Painbox.Constructor(Vector3(875.4928f, 2282.849f, 40.21423f), painbox_radius_continuous),
        owning_building_guid = 94
      )
      LocalObject(
        2991,
        Painbox.Constructor(Vector3(887.1271f, 2280.078f, 39.02483f), painbox_radius_continuous),
        owning_building_guid = 94
      )
      LocalObject(
        2992,
        Painbox.Constructor(Vector3(887.2594f, 2292.107f, 39.02483f), painbox_radius_continuous),
        owning_building_guid = 94
      )
    }

    Building28()

    def Building28(): Unit = { // Name: S_Leza_Tower Type: tower_b GUID: 95, MapID: 28
      LocalBuilding(
        "S_Leza_Tower",
        95,
        28,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2778f, 1254f, 64.07666f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        3359,
        CaptureTerminal.Constructor(Vector3(2794.587f, 1253.897f, 84.07566f), secondary_capture),
        owning_building_guid = 95
      )
      LocalObject(528, Door.Constructor(Vector3(2790f, 1246f, 65.59666f)), owning_building_guid = 95)
      LocalObject(529, Door.Constructor(Vector3(2790f, 1246f, 75.59666f)), owning_building_guid = 95)
      LocalObject(530, Door.Constructor(Vector3(2790f, 1246f, 95.59666f)), owning_building_guid = 95)
      LocalObject(531, Door.Constructor(Vector3(2790f, 1262f, 65.59666f)), owning_building_guid = 95)
      LocalObject(532, Door.Constructor(Vector3(2790f, 1262f, 75.59666f)), owning_building_guid = 95)
      LocalObject(533, Door.Constructor(Vector3(2790f, 1262f, 95.59666f)), owning_building_guid = 95)
      LocalObject(3533, Door.Constructor(Vector3(2789.147f, 1242.794f, 55.41266f)), owning_building_guid = 95)
      LocalObject(3534, Door.Constructor(Vector3(2789.147f, 1259.204f, 55.41266f)), owning_building_guid = 95)
      LocalObject(
        1369,
        IFFLock.Constructor(Vector3(2787.957f, 1262.811f, 65.53766f), Vector3(0, 0, 0)),
        owning_building_guid = 95,
        door_guid = 531
      )
      LocalObject(
        1370,
        IFFLock.Constructor(Vector3(2787.957f, 1262.811f, 75.53766f), Vector3(0, 0, 0)),
        owning_building_guid = 95,
        door_guid = 532
      )
      LocalObject(
        1371,
        IFFLock.Constructor(Vector3(2787.957f, 1262.811f, 95.53766f), Vector3(0, 0, 0)),
        owning_building_guid = 95,
        door_guid = 533
      )
      LocalObject(
        1372,
        IFFLock.Constructor(Vector3(2792.047f, 1245.189f, 65.53766f), Vector3(0, 0, 180)),
        owning_building_guid = 95,
        door_guid = 528
      )
      LocalObject(
        1373,
        IFFLock.Constructor(Vector3(2792.047f, 1245.189f, 75.53766f), Vector3(0, 0, 180)),
        owning_building_guid = 95,
        door_guid = 529
      )
      LocalObject(
        1374,
        IFFLock.Constructor(Vector3(2792.047f, 1245.189f, 95.53766f), Vector3(0, 0, 180)),
        owning_building_guid = 95,
        door_guid = 530
      )
      LocalObject(1742, Locker.Constructor(Vector3(2793.716f, 1238.963f, 54.07066f)), owning_building_guid = 95)
      LocalObject(1743, Locker.Constructor(Vector3(2793.751f, 1260.835f, 54.07066f)), owning_building_guid = 95)
      LocalObject(1744, Locker.Constructor(Vector3(2795.053f, 1238.963f, 54.07066f)), owning_building_guid = 95)
      LocalObject(1745, Locker.Constructor(Vector3(2795.088f, 1260.835f, 54.07066f)), owning_building_guid = 95)
      LocalObject(1746, Locker.Constructor(Vector3(2797.741f, 1238.963f, 54.07066f)), owning_building_guid = 95)
      LocalObject(1747, Locker.Constructor(Vector3(2797.741f, 1260.835f, 54.07066f)), owning_building_guid = 95)
      LocalObject(1748, Locker.Constructor(Vector3(2799.143f, 1238.963f, 54.07066f)), owning_building_guid = 95)
      LocalObject(1749, Locker.Constructor(Vector3(2799.143f, 1260.835f, 54.07066f)), owning_building_guid = 95)
      LocalObject(
        2414,
        Terminal.Constructor(Vector3(2799.446f, 1244.129f, 55.40866f), order_terminal),
        owning_building_guid = 95
      )
      LocalObject(
        2415,
        Terminal.Constructor(Vector3(2799.446f, 1249.853f, 55.40866f), order_terminal),
        owning_building_guid = 95
      )
      LocalObject(
        2416,
        Terminal.Constructor(Vector3(2799.446f, 1255.234f, 55.40866f), order_terminal),
        owning_building_guid = 95
      )
      LocalObject(
        3269,
        SpawnTube.Constructor(Vector3(2788.706f, 1241.742f, 53.55866f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 95
      )
      LocalObject(
        3270,
        SpawnTube.Constructor(Vector3(2788.706f, 1258.152f, 53.55866f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 95
      )
      LocalObject(
        3008,
        Painbox.Constructor(Vector3(2783.493f, 1246.849f, 55.36606f), painbox_radius_continuous),
        owning_building_guid = 95
      )
      LocalObject(
        3009,
        Painbox.Constructor(Vector3(2795.127f, 1244.078f, 54.17666f), painbox_radius_continuous),
        owning_building_guid = 95
      )
      LocalObject(
        3010,
        Painbox.Constructor(Vector3(2795.259f, 1256.107f, 54.17666f), painbox_radius_continuous),
        owning_building_guid = 95
      )
    }

    Building30()

    def Building30(): Unit = { // Name: NE_Tore_Tower Type: tower_b GUID: 96, MapID: 30
      LocalBuilding(
        "NE_Tore_Tower",
        96,
        30,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3580f, 3226f, 46.48213f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        3364,
        CaptureTerminal.Constructor(Vector3(3596.587f, 3225.897f, 66.48113f), secondary_capture),
        owning_building_guid = 96
      )
      LocalObject(564, Door.Constructor(Vector3(3592f, 3218f, 48.00213f)), owning_building_guid = 96)
      LocalObject(565, Door.Constructor(Vector3(3592f, 3218f, 58.00213f)), owning_building_guid = 96)
      LocalObject(566, Door.Constructor(Vector3(3592f, 3218f, 78.00214f)), owning_building_guid = 96)
      LocalObject(567, Door.Constructor(Vector3(3592f, 3234f, 48.00213f)), owning_building_guid = 96)
      LocalObject(568, Door.Constructor(Vector3(3592f, 3234f, 58.00213f)), owning_building_guid = 96)
      LocalObject(569, Door.Constructor(Vector3(3592f, 3234f, 78.00214f)), owning_building_guid = 96)
      LocalObject(3546, Door.Constructor(Vector3(3591.147f, 3214.794f, 37.81813f)), owning_building_guid = 96)
      LocalObject(3547, Door.Constructor(Vector3(3591.147f, 3231.204f, 37.81813f)), owning_building_guid = 96)
      LocalObject(
        1402,
        IFFLock.Constructor(Vector3(3589.957f, 3234.811f, 47.94313f), Vector3(0, 0, 0)),
        owning_building_guid = 96,
        door_guid = 567
      )
      LocalObject(
        1403,
        IFFLock.Constructor(Vector3(3589.957f, 3234.811f, 57.94313f), Vector3(0, 0, 0)),
        owning_building_guid = 96,
        door_guid = 568
      )
      LocalObject(
        1404,
        IFFLock.Constructor(Vector3(3589.957f, 3234.811f, 77.94313f), Vector3(0, 0, 0)),
        owning_building_guid = 96,
        door_guid = 569
      )
      LocalObject(
        1405,
        IFFLock.Constructor(Vector3(3594.047f, 3217.189f, 47.94313f), Vector3(0, 0, 180)),
        owning_building_guid = 96,
        door_guid = 564
      )
      LocalObject(
        1406,
        IFFLock.Constructor(Vector3(3594.047f, 3217.189f, 57.94313f), Vector3(0, 0, 180)),
        owning_building_guid = 96,
        door_guid = 565
      )
      LocalObject(
        1407,
        IFFLock.Constructor(Vector3(3594.047f, 3217.189f, 77.94313f), Vector3(0, 0, 180)),
        owning_building_guid = 96,
        door_guid = 566
      )
      LocalObject(1794, Locker.Constructor(Vector3(3595.716f, 3210.963f, 36.47613f)), owning_building_guid = 96)
      LocalObject(1795, Locker.Constructor(Vector3(3595.751f, 3232.835f, 36.47613f)), owning_building_guid = 96)
      LocalObject(1796, Locker.Constructor(Vector3(3597.053f, 3210.963f, 36.47613f)), owning_building_guid = 96)
      LocalObject(1797, Locker.Constructor(Vector3(3597.088f, 3232.835f, 36.47613f)), owning_building_guid = 96)
      LocalObject(1798, Locker.Constructor(Vector3(3599.741f, 3210.963f, 36.47613f)), owning_building_guid = 96)
      LocalObject(1799, Locker.Constructor(Vector3(3599.741f, 3232.835f, 36.47613f)), owning_building_guid = 96)
      LocalObject(1800, Locker.Constructor(Vector3(3601.143f, 3210.963f, 36.47613f)), owning_building_guid = 96)
      LocalObject(1801, Locker.Constructor(Vector3(3601.143f, 3232.835f, 36.47613f)), owning_building_guid = 96)
      LocalObject(
        2436,
        Terminal.Constructor(Vector3(3601.446f, 3216.129f, 37.81413f), order_terminal),
        owning_building_guid = 96
      )
      LocalObject(
        2437,
        Terminal.Constructor(Vector3(3601.446f, 3221.853f, 37.81413f), order_terminal),
        owning_building_guid = 96
      )
      LocalObject(
        2438,
        Terminal.Constructor(Vector3(3601.446f, 3227.234f, 37.81413f), order_terminal),
        owning_building_guid = 96
      )
      LocalObject(
        3282,
        SpawnTube.Constructor(Vector3(3590.706f, 3213.742f, 35.96413f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 96
      )
      LocalObject(
        3283,
        SpawnTube.Constructor(Vector3(3590.706f, 3230.152f, 35.96413f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 96
      )
      LocalObject(
        3023,
        Painbox.Constructor(Vector3(3585.493f, 3218.849f, 37.77153f), painbox_radius_continuous),
        owning_building_guid = 96
      )
      LocalObject(
        3024,
        Painbox.Constructor(Vector3(3597.127f, 3216.078f, 36.58213f), painbox_radius_continuous),
        owning_building_guid = 96
      )
      LocalObject(
        3025,
        Painbox.Constructor(Vector3(3597.259f, 3228.107f, 36.58213f), painbox_radius_continuous),
        owning_building_guid = 96
      )
    }

    Building68()

    def Building68(): Unit = { // Name: N_Honsi_Tower Type: tower_b GUID: 97, MapID: 68
      LocalBuilding(
        "N_Honsi_Tower",
        97,
        68,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3970f, 4816f, 91.19419f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        3366,
        CaptureTerminal.Constructor(Vector3(3986.587f, 4815.897f, 111.1932f), secondary_capture),
        owning_building_guid = 97
      )
      LocalObject(581, Door.Constructor(Vector3(3982f, 4808f, 92.71419f)), owning_building_guid = 97)
      LocalObject(582, Door.Constructor(Vector3(3982f, 4808f, 102.7142f)), owning_building_guid = 97)
      LocalObject(583, Door.Constructor(Vector3(3982f, 4808f, 122.7142f)), owning_building_guid = 97)
      LocalObject(584, Door.Constructor(Vector3(3982f, 4824f, 92.71419f)), owning_building_guid = 97)
      LocalObject(585, Door.Constructor(Vector3(3982f, 4824f, 102.7142f)), owning_building_guid = 97)
      LocalObject(586, Door.Constructor(Vector3(3982f, 4824f, 122.7142f)), owning_building_guid = 97)
      LocalObject(3550, Door.Constructor(Vector3(3981.147f, 4804.794f, 82.53019f)), owning_building_guid = 97)
      LocalObject(3551, Door.Constructor(Vector3(3981.147f, 4821.204f, 82.53019f)), owning_building_guid = 97)
      LocalObject(
        1414,
        IFFLock.Constructor(Vector3(3979.957f, 4824.811f, 92.65519f), Vector3(0, 0, 0)),
        owning_building_guid = 97,
        door_guid = 584
      )
      LocalObject(
        1415,
        IFFLock.Constructor(Vector3(3979.957f, 4824.811f, 102.6552f), Vector3(0, 0, 0)),
        owning_building_guid = 97,
        door_guid = 585
      )
      LocalObject(
        1416,
        IFFLock.Constructor(Vector3(3979.957f, 4824.811f, 122.6552f), Vector3(0, 0, 0)),
        owning_building_guid = 97,
        door_guid = 586
      )
      LocalObject(
        1418,
        IFFLock.Constructor(Vector3(3984.047f, 4807.189f, 92.65519f), Vector3(0, 0, 180)),
        owning_building_guid = 97,
        door_guid = 581
      )
      LocalObject(
        1419,
        IFFLock.Constructor(Vector3(3984.047f, 4807.189f, 102.6552f), Vector3(0, 0, 180)),
        owning_building_guid = 97,
        door_guid = 582
      )
      LocalObject(
        1420,
        IFFLock.Constructor(Vector3(3984.047f, 4807.189f, 122.6552f), Vector3(0, 0, 180)),
        owning_building_guid = 97,
        door_guid = 583
      )
      LocalObject(1810, Locker.Constructor(Vector3(3985.716f, 4800.963f, 81.18819f)), owning_building_guid = 97)
      LocalObject(1811, Locker.Constructor(Vector3(3985.751f, 4822.835f, 81.18819f)), owning_building_guid = 97)
      LocalObject(1812, Locker.Constructor(Vector3(3987.053f, 4800.963f, 81.18819f)), owning_building_guid = 97)
      LocalObject(1813, Locker.Constructor(Vector3(3987.088f, 4822.835f, 81.18819f)), owning_building_guid = 97)
      LocalObject(1814, Locker.Constructor(Vector3(3989.741f, 4800.963f, 81.18819f)), owning_building_guid = 97)
      LocalObject(1815, Locker.Constructor(Vector3(3989.741f, 4822.835f, 81.18819f)), owning_building_guid = 97)
      LocalObject(1816, Locker.Constructor(Vector3(3991.143f, 4800.963f, 81.18819f)), owning_building_guid = 97)
      LocalObject(1817, Locker.Constructor(Vector3(3991.143f, 4822.835f, 81.18819f)), owning_building_guid = 97)
      LocalObject(
        2443,
        Terminal.Constructor(Vector3(3991.446f, 4806.129f, 82.52619f), order_terminal),
        owning_building_guid = 97
      )
      LocalObject(
        2444,
        Terminal.Constructor(Vector3(3991.446f, 4811.853f, 82.52619f), order_terminal),
        owning_building_guid = 97
      )
      LocalObject(
        2445,
        Terminal.Constructor(Vector3(3991.446f, 4817.234f, 82.52619f), order_terminal),
        owning_building_guid = 97
      )
      LocalObject(
        3286,
        SpawnTube.Constructor(Vector3(3980.706f, 4803.742f, 80.67619f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 97
      )
      LocalObject(
        3287,
        SpawnTube.Constructor(Vector3(3980.706f, 4820.152f, 80.67619f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 97
      )
      LocalObject(
        3029,
        Painbox.Constructor(Vector3(3975.493f, 4808.849f, 82.48359f), painbox_radius_continuous),
        owning_building_guid = 97
      )
      LocalObject(
        3031,
        Painbox.Constructor(Vector3(3987.127f, 4806.078f, 81.29419f), painbox_radius_continuous),
        owning_building_guid = 97
      )
      LocalObject(
        3032,
        Painbox.Constructor(Vector3(3987.259f, 4818.107f, 81.29419f), painbox_radius_continuous),
        owning_building_guid = 97
      )
    }

    Building45()

    def Building45(): Unit = { // Name: SW_Itan_Tower Type: tower_b GUID: 98, MapID: 45
      LocalBuilding(
        "SW_Itan_Tower",
        98,
        45,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4846f, 3116f, 59.06708f), Vector3(0f, 0f, 331f), tower_b)
        )
      )
      LocalObject(
        3369,
        CaptureTerminal.Constructor(Vector3(4860.458f, 3107.868f, 79.06608f), secondary_capture),
        owning_building_guid = 98
      )
      LocalObject(614, Door.Constructor(Vector3(4852.617f, 3103.185f, 60.58708f)), owning_building_guid = 98)
      LocalObject(615, Door.Constructor(Vector3(4852.617f, 3103.185f, 70.58708f)), owning_building_guid = 98)
      LocalObject(616, Door.Constructor(Vector3(4852.617f, 3103.185f, 90.58708f)), owning_building_guid = 98)
      LocalObject(617, Door.Constructor(Vector3(4860.374f, 3117.179f, 60.58708f)), owning_building_guid = 98)
      LocalObject(618, Door.Constructor(Vector3(4860.374f, 3117.179f, 70.58708f)), owning_building_guid = 98)
      LocalObject(619, Door.Constructor(Vector3(4860.374f, 3117.179f, 90.58708f)), owning_building_guid = 98)
      LocalObject(3562, Door.Constructor(Vector3(4850.316f, 3100.795f, 50.40308f)), owning_building_guid = 98)
      LocalObject(3563, Door.Constructor(Vector3(4858.272f, 3115.147f, 50.40308f)), owning_building_guid = 98)
      LocalObject(
        1442,
        IFFLock.Constructor(Vector3(4854.014f, 3101.484f, 60.52808f), Vector3(0, 0, 209)),
        owning_building_guid = 98,
        door_guid = 614
      )
      LocalObject(
        1443,
        IFFLock.Constructor(Vector3(4854.014f, 3101.484f, 70.52808f), Vector3(0, 0, 209)),
        owning_building_guid = 98,
        door_guid = 615
      )
      LocalObject(
        1444,
        IFFLock.Constructor(Vector3(4854.014f, 3101.484f, 90.52808f), Vector3(0, 0, 209)),
        owning_building_guid = 98,
        door_guid = 616
      )
      LocalObject(
        1445,
        IFFLock.Constructor(Vector3(4858.98f, 3118.879f, 60.52808f), Vector3(0, 0, 29)),
        owning_building_guid = 98,
        door_guid = 617
      )
      LocalObject(
        1446,
        IFFLock.Constructor(Vector3(4858.98f, 3118.879f, 70.52808f), Vector3(0, 0, 29)),
        owning_building_guid = 98,
        door_guid = 618
      )
      LocalObject(
        1447,
        IFFLock.Constructor(Vector3(4858.98f, 3118.879f, 90.52808f), Vector3(0, 0, 29)),
        owning_building_guid = 98,
        door_guid = 619
      )
      LocalObject(1876, Locker.Constructor(Vector3(4852.456f, 3095.229f, 49.06108f)), owning_building_guid = 98)
      LocalObject(1877, Locker.Constructor(Vector3(4853.625f, 3094.581f, 49.06108f)), owning_building_guid = 98)
      LocalObject(1878, Locker.Constructor(Vector3(4855.976f, 3093.278f, 49.06108f)), owning_building_guid = 98)
      LocalObject(1879, Locker.Constructor(Vector3(4857.202f, 3092.598f, 49.06108f)), owning_building_guid = 98)
      LocalObject(1880, Locker.Constructor(Vector3(4863.09f, 3114.342f, 49.06108f)), owning_building_guid = 98)
      LocalObject(1881, Locker.Constructor(Vector3(4864.259f, 3113.694f, 49.06108f)), owning_building_guid = 98)
      LocalObject(1882, Locker.Constructor(Vector3(4866.58f, 3112.407f, 49.06108f)), owning_building_guid = 98)
      LocalObject(1883, Locker.Constructor(Vector3(4867.806f, 3111.728f, 49.06108f)), owning_building_guid = 98)
      LocalObject(
        2459,
        Terminal.Constructor(Vector3(4859.972f, 3096.969f, 50.39908f), order_terminal),
        owning_building_guid = 98
      )
      LocalObject(
        2460,
        Terminal.Constructor(Vector3(4862.747f, 3101.976f, 50.39908f), order_terminal),
        owning_building_guid = 98
      )
      LocalObject(
        2461,
        Terminal.Constructor(Vector3(4865.355f, 3106.682f, 50.39908f), order_terminal),
        owning_building_guid = 98
      )
      LocalObject(
        3298,
        SpawnTube.Constructor(Vector3(4849.421f, 3100.089f, 48.54908f), respawn_tube_tower, Vector3(0, 0, 29)),
        owning_building_guid = 98
      )
      LocalObject(
        3299,
        SpawnTube.Constructor(Vector3(4857.376f, 3114.441f, 48.54908f), respawn_tube_tower, Vector3(0, 0, 29)),
        owning_building_guid = 98
      )
      LocalObject(
        3038,
        Painbox.Constructor(Vector3(4847.337f, 3107.083f, 50.35648f), painbox_radius_continuous),
        owning_building_guid = 98
      )
      LocalObject(
        3039,
        Painbox.Constructor(Vector3(4856.169f, 3099.019f, 49.16708f), painbox_radius_continuous),
        owning_building_guid = 98
      )
      LocalObject(
        3040,
        Painbox.Constructor(Vector3(4862.117f, 3109.475f, 49.16708f), painbox_radius_continuous),
        owning_building_guid = 98
      )
    }

    Building32()

    def Building32(): Unit = { // Name: SE_Gunuku_Tower Type: tower_b GUID: 99, MapID: 32
      LocalBuilding(
        "SE_Gunuku_Tower",
        99,
        32,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5146f, 3968f, 50.28779f), Vector3(0f, 0f, 349f), tower_b)
        )
      )
      LocalObject(
        3371,
        CaptureTerminal.Constructor(Vector3(5162.263f, 3964.734f, 70.28679f), secondary_capture),
        owning_building_guid = 99
      )
      LocalObject(667, Door.Constructor(Vector3(5156.253f, 3957.857f, 51.80779f)), owning_building_guid = 99)
      LocalObject(668, Door.Constructor(Vector3(5156.253f, 3957.857f, 61.80779f)), owning_building_guid = 99)
      LocalObject(669, Door.Constructor(Vector3(5156.253f, 3957.857f, 81.80779f)), owning_building_guid = 99)
      LocalObject(671, Door.Constructor(Vector3(5159.306f, 3973.563f, 51.80779f)), owning_building_guid = 99)
      LocalObject(672, Door.Constructor(Vector3(5159.306f, 3973.563f, 61.80779f)), owning_building_guid = 99)
      LocalObject(673, Door.Constructor(Vector3(5159.306f, 3973.563f, 81.80779f)), owning_building_guid = 99)
      LocalObject(3575, Door.Constructor(Vector3(5154.804f, 3954.873f, 41.62379f)), owning_building_guid = 99)
      LocalObject(3576, Door.Constructor(Vector3(5157.935f, 3970.981f, 41.62379f)), owning_building_guid = 99)
      LocalObject(
        1481,
        IFFLock.Constructor(Vector3(5157.455f, 3974.749f, 51.74879f), Vector3(0, 0, 11)),
        owning_building_guid = 99,
        door_guid = 671
      )
      LocalObject(
        1482,
        IFFLock.Constructor(Vector3(5157.455f, 3974.749f, 61.74879f), Vector3(0, 0, 11)),
        owning_building_guid = 99,
        door_guid = 672
      )
      LocalObject(
        1483,
        IFFLock.Constructor(Vector3(5157.455f, 3974.749f, 81.74879f), Vector3(0, 0, 11)),
        owning_building_guid = 99,
        door_guid = 673
      )
      LocalObject(
        1484,
        IFFLock.Constructor(Vector3(5158.108f, 3956.671f, 51.74879f), Vector3(0, 0, 191)),
        owning_building_guid = 99,
        door_guid = 667
      )
      LocalObject(
        1485,
        IFFLock.Constructor(Vector3(5158.108f, 3956.671f, 61.74879f), Vector3(0, 0, 191)),
        owning_building_guid = 99,
        door_guid = 668
      )
      LocalObject(
        1486,
        IFFLock.Constructor(Vector3(5158.108f, 3956.671f, 81.74879f), Vector3(0, 0, 191)),
        owning_building_guid = 99,
        door_guid = 669
      )
      LocalObject(1925, Locker.Constructor(Vector3(5158.558f, 3950.24f, 40.28179f)), owning_building_guid = 99)
      LocalObject(1926, Locker.Constructor(Vector3(5159.871f, 3949.985f, 40.28179f)), owning_building_guid = 99)
      LocalObject(1927, Locker.Constructor(Vector3(5162.509f, 3949.472f, 40.28179f)), owning_building_guid = 99)
      LocalObject(1928, Locker.Constructor(Vector3(5162.766f, 3971.704f, 40.28179f)), owning_building_guid = 99)
      LocalObject(1929, Locker.Constructor(Vector3(5163.885f, 3949.205f, 40.28179f)), owning_building_guid = 99)
      LocalObject(1930, Locker.Constructor(Vector3(5164.078f, 3971.449f, 40.28179f)), owning_building_guid = 99)
      LocalObject(1935, Locker.Constructor(Vector3(5166.683f, 3970.943f, 40.28179f)), owning_building_guid = 99)
      LocalObject(1940, Locker.Constructor(Vector3(5168.059f, 3970.675f, 40.28179f)), owning_building_guid = 99)
      LocalObject(
        2481,
        Terminal.Constructor(Vector3(5165.168f, 3954.218f, 41.61979f), order_terminal),
        owning_building_guid = 99
      )
      LocalObject(
        2482,
        Terminal.Constructor(Vector3(5166.261f, 3959.837f, 41.61979f), order_terminal),
        owning_building_guid = 99
      )
      LocalObject(
        2483,
        Terminal.Constructor(Vector3(5167.288f, 3965.119f, 41.61979f), order_terminal),
        owning_building_guid = 99
      )
      LocalObject(
        3311,
        SpawnTube.Constructor(Vector3(5154.17f, 3953.924f, 39.76979f), respawn_tube_tower, Vector3(0, 0, 11)),
        owning_building_guid = 99
      )
      LocalObject(
        3312,
        SpawnTube.Constructor(Vector3(5157.302f, 3970.033f, 39.76979f), respawn_tube_tower, Vector3(0, 0, 11)),
        owning_building_guid = 99
      )
      LocalObject(
        3044,
        Painbox.Constructor(Vector3(5150.027f, 3959.932f, 41.57719f), painbox_radius_continuous),
        owning_building_guid = 99
      )
      LocalObject(
        3045,
        Painbox.Constructor(Vector3(5160.919f, 3954.992f, 40.38779f), painbox_radius_continuous),
        owning_building_guid = 99
      )
      LocalObject(
        3046,
        Painbox.Constructor(Vector3(5163.344f, 3966.775f, 40.38779f), painbox_radius_continuous),
        owning_building_guid = 99
      )
    }

    Building34()

    def Building34(): Unit = { // Name: S_Faro_Tower Type: tower_b GUID: 100, MapID: 34
      LocalBuilding(
        "S_Faro_Tower",
        100,
        34,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5266f, 5302f, 54.97296f), Vector3(0f, 0f, 286f), tower_b)
        )
      )
      LocalObject(
        3372,
        CaptureTerminal.Constructor(Vector3(5270.473f, 5286.027f, 74.97195f), secondary_capture),
        owning_building_guid = 100
      )
      LocalObject(679, Door.Constructor(Vector3(5261.618f, 5288.26f, 56.49296f)), owning_building_guid = 100)
      LocalObject(680, Door.Constructor(Vector3(5261.618f, 5288.26f, 66.49296f)), owning_building_guid = 100)
      LocalObject(681, Door.Constructor(Vector3(5261.618f, 5288.26f, 86.49296f)), owning_building_guid = 100)
      LocalObject(682, Door.Constructor(Vector3(5276.998f, 5292.67f, 56.49296f)), owning_building_guid = 100)
      LocalObject(683, Door.Constructor(Vector3(5276.998f, 5292.67f, 66.49296f)), owning_building_guid = 100)
      LocalObject(684, Door.Constructor(Vector3(5276.998f, 5292.67f, 86.49296f)), owning_building_guid = 100)
      LocalObject(3577, Door.Constructor(Vector3(5258.301f, 5288.196f, 46.30896f)), owning_building_guid = 100)
      LocalObject(3578, Door.Constructor(Vector3(5274.075f, 5292.719f, 46.30896f)), owning_building_guid = 100)
      LocalObject(
        1488,
        IFFLock.Constructor(Vector3(5261.402f, 5286.068f, 56.43396f), Vector3(0, 0, 254)),
        owning_building_guid = 100,
        door_guid = 679
      )
      LocalObject(
        1489,
        IFFLock.Constructor(Vector3(5261.402f, 5286.068f, 66.43396f), Vector3(0, 0, 254)),
        owning_building_guid = 100,
        door_guid = 680
      )
      LocalObject(
        1490,
        IFFLock.Constructor(Vector3(5261.402f, 5286.068f, 86.43396f), Vector3(0, 0, 254)),
        owning_building_guid = 100,
        door_guid = 681
      )
      LocalObject(
        1491,
        IFFLock.Constructor(Vector3(5277.214f, 5294.857f, 56.43396f), Vector3(0, 0, 74)),
        owning_building_guid = 100,
        door_guid = 682
      )
      LocalObject(
        1492,
        IFFLock.Constructor(Vector3(5277.214f, 5294.857f, 66.43396f), Vector3(0, 0, 74)),
        owning_building_guid = 100,
        door_guid = 683
      )
      LocalObject(
        1493,
        IFFLock.Constructor(Vector3(5277.214f, 5294.857f, 86.43396f), Vector3(0, 0, 74)),
        owning_building_guid = 100,
        door_guid = 684
      )
      LocalObject(1945, Locker.Constructor(Vector3(5255.877f, 5282.748f, 44.96696f)), owning_building_guid = 100)
      LocalObject(1946, Locker.Constructor(Vector3(5256.246f, 5281.463f, 44.96696f)), owning_building_guid = 100)
      LocalObject(1947, Locker.Constructor(Vector3(5256.987f, 5278.879f, 44.96696f)), owning_building_guid = 100)
      LocalObject(1948, Locker.Constructor(Vector3(5257.374f, 5277.531f, 44.96696f)), owning_building_guid = 100)
      LocalObject(1949, Locker.Constructor(Vector3(5276.912f, 5288.743f, 44.96696f)), owning_building_guid = 100)
      LocalObject(1950, Locker.Constructor(Vector3(5277.28f, 5287.458f, 44.96696f)), owning_building_guid = 100)
      LocalObject(1951, Locker.Constructor(Vector3(5278.012f, 5284.908f, 44.96696f)), owning_building_guid = 100)
      LocalObject(1952, Locker.Constructor(Vector3(5278.398f, 5283.56f, 44.96696f)), owning_building_guid = 100)
      LocalObject(
        2484,
        Terminal.Constructor(Vector3(5262.423f, 5278.664f, 46.30496f), order_terminal),
        owning_building_guid = 100
      )
      LocalObject(
        2485,
        Terminal.Constructor(Vector3(5267.925f, 5280.242f, 46.30496f), order_terminal),
        owning_building_guid = 100
      )
      LocalObject(
        2486,
        Terminal.Constructor(Vector3(5273.098f, 5281.725f, 46.30496f), order_terminal),
        owning_building_guid = 100
      )
      LocalObject(
        3313,
        SpawnTube.Constructor(Vector3(5257.168f, 5288.33f, 44.45496f), respawn_tube_tower, Vector3(0, 0, 74)),
        owning_building_guid = 100
      )
      LocalObject(
        3314,
        SpawnTube.Constructor(Vector3(5272.942f, 5292.853f, 44.45496f), respawn_tube_tower, Vector3(0, 0, 74)),
        owning_building_guid = 100
      )
      LocalObject(
        3047,
        Painbox.Constructor(Vector3(5260.64f, 5294.749f, 46.26236f), painbox_radius_continuous),
        owning_building_guid = 100
      )
      LocalObject(
        3048,
        Painbox.Constructor(Vector3(5261.183f, 5282.801f, 45.07296f), painbox_radius_continuous),
        owning_building_guid = 100
      )
      LocalObject(
        3049,
        Painbox.Constructor(Vector3(5272.782f, 5285.99f, 45.07296f), painbox_radius_continuous),
        owning_building_guid = 100
      )
    }

    Building42()

    def Building42(): Unit = { // Name: W_Orisha_Tower Type: tower_b GUID: 101, MapID: 42
      LocalBuilding(
        "W_Orisha_Tower",
        101,
        42,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6660f, 1302f, 51.17282f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        3379,
        CaptureTerminal.Constructor(Vector3(6676.587f, 1301.897f, 71.17183f), secondary_capture),
        owning_building_guid = 101
      )
      LocalObject(745, Door.Constructor(Vector3(6672f, 1294f, 52.69283f)), owning_building_guid = 101)
      LocalObject(746, Door.Constructor(Vector3(6672f, 1294f, 62.69283f)), owning_building_guid = 101)
      LocalObject(747, Door.Constructor(Vector3(6672f, 1294f, 82.69283f)), owning_building_guid = 101)
      LocalObject(748, Door.Constructor(Vector3(6672f, 1310f, 52.69283f)), owning_building_guid = 101)
      LocalObject(749, Door.Constructor(Vector3(6672f, 1310f, 62.69283f)), owning_building_guid = 101)
      LocalObject(750, Door.Constructor(Vector3(6672f, 1310f, 82.69283f)), owning_building_guid = 101)
      LocalObject(3597, Door.Constructor(Vector3(6671.147f, 1290.794f, 42.50883f)), owning_building_guid = 101)
      LocalObject(3598, Door.Constructor(Vector3(6671.147f, 1307.204f, 42.50883f)), owning_building_guid = 101)
      LocalObject(
        1540,
        IFFLock.Constructor(Vector3(6669.957f, 1310.811f, 52.63382f), Vector3(0, 0, 0)),
        owning_building_guid = 101,
        door_guid = 748
      )
      LocalObject(
        1541,
        IFFLock.Constructor(Vector3(6669.957f, 1310.811f, 62.63383f), Vector3(0, 0, 0)),
        owning_building_guid = 101,
        door_guid = 749
      )
      LocalObject(
        1542,
        IFFLock.Constructor(Vector3(6669.957f, 1310.811f, 82.63383f), Vector3(0, 0, 0)),
        owning_building_guid = 101,
        door_guid = 750
      )
      LocalObject(
        1543,
        IFFLock.Constructor(Vector3(6674.047f, 1293.189f, 52.63382f), Vector3(0, 0, 180)),
        owning_building_guid = 101,
        door_guid = 745
      )
      LocalObject(
        1544,
        IFFLock.Constructor(Vector3(6674.047f, 1293.189f, 62.63383f), Vector3(0, 0, 180)),
        owning_building_guid = 101,
        door_guid = 746
      )
      LocalObject(
        1545,
        IFFLock.Constructor(Vector3(6674.047f, 1293.189f, 82.63383f), Vector3(0, 0, 180)),
        owning_building_guid = 101,
        door_guid = 747
      )
      LocalObject(2025, Locker.Constructor(Vector3(6675.716f, 1286.963f, 41.16682f)), owning_building_guid = 101)
      LocalObject(2026, Locker.Constructor(Vector3(6675.751f, 1308.835f, 41.16682f)), owning_building_guid = 101)
      LocalObject(2027, Locker.Constructor(Vector3(6677.053f, 1286.963f, 41.16682f)), owning_building_guid = 101)
      LocalObject(2028, Locker.Constructor(Vector3(6677.088f, 1308.835f, 41.16682f)), owning_building_guid = 101)
      LocalObject(2029, Locker.Constructor(Vector3(6679.741f, 1286.963f, 41.16682f)), owning_building_guid = 101)
      LocalObject(2030, Locker.Constructor(Vector3(6679.741f, 1308.835f, 41.16682f)), owning_building_guid = 101)
      LocalObject(2031, Locker.Constructor(Vector3(6681.143f, 1286.963f, 41.16682f)), owning_building_guid = 101)
      LocalObject(2032, Locker.Constructor(Vector3(6681.143f, 1308.835f, 41.16682f)), owning_building_guid = 101)
      LocalObject(
        2517,
        Terminal.Constructor(Vector3(6681.446f, 1292.129f, 42.50483f), order_terminal),
        owning_building_guid = 101
      )
      LocalObject(
        2518,
        Terminal.Constructor(Vector3(6681.446f, 1297.853f, 42.50483f), order_terminal),
        owning_building_guid = 101
      )
      LocalObject(
        2519,
        Terminal.Constructor(Vector3(6681.446f, 1303.234f, 42.50483f), order_terminal),
        owning_building_guid = 101
      )
      LocalObject(
        3333,
        SpawnTube.Constructor(Vector3(6670.706f, 1289.742f, 40.65482f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 101
      )
      LocalObject(
        3334,
        SpawnTube.Constructor(Vector3(6670.706f, 1306.152f, 40.65482f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 101
      )
      LocalObject(
        3068,
        Painbox.Constructor(Vector3(6665.493f, 1294.849f, 42.46223f), painbox_radius_continuous),
        owning_building_guid = 101
      )
      LocalObject(
        3069,
        Painbox.Constructor(Vector3(6677.127f, 1292.078f, 41.27283f), painbox_radius_continuous),
        owning_building_guid = 101
      )
      LocalObject(
        3070,
        Painbox.Constructor(Vector3(6677.259f, 1304.107f, 41.27283f), painbox_radius_continuous),
        owning_building_guid = 101
      )
    }

    Building40()

    def Building40(): Unit = { // Name: SW_Solsar_Warpgate_Tower Type: tower_b GUID: 102, MapID: 40
      LocalBuilding(
        "SW_Solsar_Warpgate_Tower",
        102,
        40,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(7036f, 4998f, 54.60317f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        3381,
        CaptureTerminal.Constructor(Vector3(7052.587f, 4997.897f, 74.60217f), secondary_capture),
        owning_building_guid = 102
      )
      LocalObject(777, Door.Constructor(Vector3(7048f, 4990f, 56.12317f)), owning_building_guid = 102)
      LocalObject(778, Door.Constructor(Vector3(7048f, 4990f, 66.12317f)), owning_building_guid = 102)
      LocalObject(779, Door.Constructor(Vector3(7048f, 4990f, 86.12317f)), owning_building_guid = 102)
      LocalObject(780, Door.Constructor(Vector3(7048f, 5006f, 56.12317f)), owning_building_guid = 102)
      LocalObject(781, Door.Constructor(Vector3(7048f, 5006f, 66.12317f)), owning_building_guid = 102)
      LocalObject(782, Door.Constructor(Vector3(7048f, 5006f, 86.12317f)), owning_building_guid = 102)
      LocalObject(3607, Door.Constructor(Vector3(7047.147f, 4986.794f, 45.93917f)), owning_building_guid = 102)
      LocalObject(3608, Door.Constructor(Vector3(7047.147f, 5003.204f, 45.93917f)), owning_building_guid = 102)
      LocalObject(
        1565,
        IFFLock.Constructor(Vector3(7045.957f, 5006.811f, 56.06417f), Vector3(0, 0, 0)),
        owning_building_guid = 102,
        door_guid = 780
      )
      LocalObject(
        1566,
        IFFLock.Constructor(Vector3(7045.957f, 5006.811f, 66.06417f), Vector3(0, 0, 0)),
        owning_building_guid = 102,
        door_guid = 781
      )
      LocalObject(
        1567,
        IFFLock.Constructor(Vector3(7045.957f, 5006.811f, 86.06417f), Vector3(0, 0, 0)),
        owning_building_guid = 102,
        door_guid = 782
      )
      LocalObject(
        1568,
        IFFLock.Constructor(Vector3(7050.047f, 4989.189f, 56.06417f), Vector3(0, 0, 180)),
        owning_building_guid = 102,
        door_guid = 777
      )
      LocalObject(
        1569,
        IFFLock.Constructor(Vector3(7050.047f, 4989.189f, 66.06417f), Vector3(0, 0, 180)),
        owning_building_guid = 102,
        door_guid = 778
      )
      LocalObject(
        1570,
        IFFLock.Constructor(Vector3(7050.047f, 4989.189f, 86.06417f), Vector3(0, 0, 180)),
        owning_building_guid = 102,
        door_guid = 779
      )
      LocalObject(2074, Locker.Constructor(Vector3(7051.716f, 4982.963f, 44.59717f)), owning_building_guid = 102)
      LocalObject(2075, Locker.Constructor(Vector3(7051.751f, 5004.835f, 44.59717f)), owning_building_guid = 102)
      LocalObject(2076, Locker.Constructor(Vector3(7053.053f, 4982.963f, 44.59717f)), owning_building_guid = 102)
      LocalObject(2077, Locker.Constructor(Vector3(7053.088f, 5004.835f, 44.59717f)), owning_building_guid = 102)
      LocalObject(2078, Locker.Constructor(Vector3(7055.741f, 4982.963f, 44.59717f)), owning_building_guid = 102)
      LocalObject(2079, Locker.Constructor(Vector3(7055.741f, 5004.835f, 44.59717f)), owning_building_guid = 102)
      LocalObject(2080, Locker.Constructor(Vector3(7057.143f, 4982.963f, 44.59717f)), owning_building_guid = 102)
      LocalObject(2081, Locker.Constructor(Vector3(7057.143f, 5004.835f, 44.59717f)), owning_building_guid = 102)
      LocalObject(
        2531,
        Terminal.Constructor(Vector3(7057.446f, 4988.129f, 45.93517f), order_terminal),
        owning_building_guid = 102
      )
      LocalObject(
        2532,
        Terminal.Constructor(Vector3(7057.446f, 4993.853f, 45.93517f), order_terminal),
        owning_building_guid = 102
      )
      LocalObject(
        2533,
        Terminal.Constructor(Vector3(7057.446f, 4999.234f, 45.93517f), order_terminal),
        owning_building_guid = 102
      )
      LocalObject(
        3343,
        SpawnTube.Constructor(Vector3(7046.706f, 4985.742f, 44.08517f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 102
      )
      LocalObject(
        3344,
        SpawnTube.Constructor(Vector3(7046.706f, 5002.152f, 44.08517f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 102
      )
      LocalObject(
        3074,
        Painbox.Constructor(Vector3(7041.493f, 4990.849f, 45.89257f), painbox_radius_continuous),
        owning_building_guid = 102
      )
      LocalObject(
        3075,
        Painbox.Constructor(Vector3(7053.127f, 4988.078f, 44.70317f), painbox_radius_continuous),
        owning_building_guid = 102
      )
      LocalObject(
        3076,
        Painbox.Constructor(Vector3(7053.259f, 5000.107f, 44.70317f), painbox_radius_continuous),
        owning_building_guid = 102
      )
    }

    Building66()

    def Building66(): Unit = { // Name: SE_Pamba_Tower Type: tower_b GUID: 103, MapID: 66
      LocalBuilding(
        "SE_Pamba_Tower",
        103,
        66,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(7524f, 2910f, 56.98198f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        3382,
        CaptureTerminal.Constructor(Vector3(7540.587f, 2909.897f, 76.98099f), secondary_capture),
        owning_building_guid = 103
      )
      LocalObject(803, Door.Constructor(Vector3(7536f, 2902f, 58.50198f)), owning_building_guid = 103)
      LocalObject(804, Door.Constructor(Vector3(7536f, 2902f, 68.50198f)), owning_building_guid = 103)
      LocalObject(805, Door.Constructor(Vector3(7536f, 2902f, 88.50198f)), owning_building_guid = 103)
      LocalObject(806, Door.Constructor(Vector3(7536f, 2918f, 58.50198f)), owning_building_guid = 103)
      LocalObject(807, Door.Constructor(Vector3(7536f, 2918f, 68.50198f)), owning_building_guid = 103)
      LocalObject(808, Door.Constructor(Vector3(7536f, 2918f, 88.50198f)), owning_building_guid = 103)
      LocalObject(3612, Door.Constructor(Vector3(7535.147f, 2898.794f, 48.31799f)), owning_building_guid = 103)
      LocalObject(3613, Door.Constructor(Vector3(7535.147f, 2915.204f, 48.31799f)), owning_building_guid = 103)
      LocalObject(
        1583,
        IFFLock.Constructor(Vector3(7533.957f, 2918.811f, 58.44298f), Vector3(0, 0, 0)),
        owning_building_guid = 103,
        door_guid = 806
      )
      LocalObject(
        1584,
        IFFLock.Constructor(Vector3(7533.957f, 2918.811f, 68.44299f), Vector3(0, 0, 0)),
        owning_building_guid = 103,
        door_guid = 807
      )
      LocalObject(
        1585,
        IFFLock.Constructor(Vector3(7533.957f, 2918.811f, 88.44299f), Vector3(0, 0, 0)),
        owning_building_guid = 103,
        door_guid = 808
      )
      LocalObject(
        1586,
        IFFLock.Constructor(Vector3(7538.047f, 2901.189f, 58.44298f), Vector3(0, 0, 180)),
        owning_building_guid = 103,
        door_guid = 803
      )
      LocalObject(
        1587,
        IFFLock.Constructor(Vector3(7538.047f, 2901.189f, 68.44299f), Vector3(0, 0, 180)),
        owning_building_guid = 103,
        door_guid = 804
      )
      LocalObject(
        1588,
        IFFLock.Constructor(Vector3(7538.047f, 2901.189f, 88.44299f), Vector3(0, 0, 180)),
        owning_building_guid = 103,
        door_guid = 805
      )
      LocalObject(2094, Locker.Constructor(Vector3(7539.716f, 2894.963f, 46.97598f)), owning_building_guid = 103)
      LocalObject(2095, Locker.Constructor(Vector3(7539.751f, 2916.835f, 46.97598f)), owning_building_guid = 103)
      LocalObject(2096, Locker.Constructor(Vector3(7541.053f, 2894.963f, 46.97598f)), owning_building_guid = 103)
      LocalObject(2097, Locker.Constructor(Vector3(7541.088f, 2916.835f, 46.97598f)), owning_building_guid = 103)
      LocalObject(2098, Locker.Constructor(Vector3(7543.741f, 2894.963f, 46.97598f)), owning_building_guid = 103)
      LocalObject(2099, Locker.Constructor(Vector3(7543.741f, 2916.835f, 46.97598f)), owning_building_guid = 103)
      LocalObject(2100, Locker.Constructor(Vector3(7545.143f, 2894.963f, 46.97598f)), owning_building_guid = 103)
      LocalObject(2101, Locker.Constructor(Vector3(7545.143f, 2916.835f, 46.97598f)), owning_building_guid = 103)
      LocalObject(
        2540,
        Terminal.Constructor(Vector3(7545.446f, 2900.129f, 48.31398f), order_terminal),
        owning_building_guid = 103
      )
      LocalObject(
        2541,
        Terminal.Constructor(Vector3(7545.446f, 2905.853f, 48.31398f), order_terminal),
        owning_building_guid = 103
      )
      LocalObject(
        2542,
        Terminal.Constructor(Vector3(7545.446f, 2911.234f, 48.31398f), order_terminal),
        owning_building_guid = 103
      )
      LocalObject(
        3348,
        SpawnTube.Constructor(Vector3(7534.706f, 2897.742f, 46.46398f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 103
      )
      LocalObject(
        3349,
        SpawnTube.Constructor(Vector3(7534.706f, 2914.152f, 46.46398f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 103
      )
      LocalObject(
        3077,
        Painbox.Constructor(Vector3(7529.493f, 2902.849f, 48.27139f), painbox_radius_continuous),
        owning_building_guid = 103
      )
      LocalObject(
        3078,
        Painbox.Constructor(Vector3(7541.127f, 2900.078f, 47.08199f), painbox_radius_continuous),
        owning_building_guid = 103
      )
      LocalObject(
        3079,
        Painbox.Constructor(Vector3(7541.259f, 2912.107f, 47.08199f), painbox_radius_continuous),
        owning_building_guid = 103
      )
    }

    Building25()

    def Building25(): Unit = { // Name: S_Bomazi_Tower Type: tower_c GUID: 104, MapID: 25
      LocalBuilding(
        "S_Bomazi_Tower",
        104,
        25,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1080f, 4328f, 68.30714f), Vector3(0f, 0f, 345f), tower_c)
        )
      )
      LocalObject(
        3354,
        CaptureTerminal.Constructor(Vector3(1095.995f, 4323.607f, 78.30614f), secondary_capture),
        owning_building_guid = 104
      )
      LocalObject(461, Door.Constructor(Vector3(1089.521f, 4317.167f, 69.82814f)), owning_building_guid = 104)
      LocalObject(462, Door.Constructor(Vector3(1089.521f, 4317.167f, 89.82713f)), owning_building_guid = 104)
      LocalObject(463, Door.Constructor(Vector3(1093.662f, 4332.622f, 69.82814f)), owning_building_guid = 104)
      LocalObject(464, Door.Constructor(Vector3(1093.662f, 4332.622f, 89.82713f)), owning_building_guid = 104)
      LocalObject(3514, Door.Constructor(Vector3(1087.866f, 4314.291f, 59.64314f)), owning_building_guid = 104)
      LocalObject(3515, Door.Constructor(Vector3(1092.113f, 4330.142f, 59.64314f)), owning_building_guid = 104)
      LocalObject(
        1319,
        IFFLock.Constructor(Vector3(1091.288f, 4315.854f, 69.76814f), Vector3(0, 0, 195)),
        owning_building_guid = 104,
        door_guid = 461
      )
      LocalObject(
        1320,
        IFFLock.Constructor(Vector3(1091.288f, 4315.854f, 89.76814f), Vector3(0, 0, 195)),
        owning_building_guid = 104,
        door_guid = 462
      )
      LocalObject(
        1321,
        IFFLock.Constructor(Vector3(1091.898f, 4333.934f, 69.76814f), Vector3(0, 0, 15)),
        owning_building_guid = 104,
        door_guid = 463
      )
      LocalObject(
        1322,
        IFFLock.Constructor(Vector3(1091.898f, 4333.934f, 89.76814f), Vector3(0, 0, 15)),
        owning_building_guid = 104,
        door_guid = 464
      )
      LocalObject(1666, Locker.Constructor(Vector3(1091.289f, 4309.408f, 58.30114f)), owning_building_guid = 104)
      LocalObject(1667, Locker.Constructor(Vector3(1092.58f, 4309.062f, 58.30114f)), owning_building_guid = 104)
      LocalObject(1668, Locker.Constructor(Vector3(1095.177f, 4308.366f, 58.30114f)), owning_building_guid = 104)
      LocalObject(1669, Locker.Constructor(Vector3(1096.531f, 4308.003f, 58.30114f)), owning_building_guid = 104)
      LocalObject(1670, Locker.Constructor(Vector3(1096.983f, 4330.525f, 58.30114f)), owning_building_guid = 104)
      LocalObject(1671, Locker.Constructor(Vector3(1098.275f, 4330.179f, 58.30114f)), owning_building_guid = 104)
      LocalObject(1672, Locker.Constructor(Vector3(1100.837f, 4329.493f, 58.30114f)), owning_building_guid = 104)
      LocalObject(1673, Locker.Constructor(Vector3(1102.192f, 4329.13f, 58.30114f)), owning_building_guid = 104)
      LocalObject(
        2382,
        Terminal.Constructor(Vector3(1098.159f, 4312.915f, 59.63914f), order_terminal),
        owning_building_guid = 104
      )
      LocalObject(
        2383,
        Terminal.Constructor(Vector3(1099.641f, 4318.444f, 59.63914f), order_terminal),
        owning_building_guid = 104
      )
      LocalObject(
        2384,
        Terminal.Constructor(Vector3(1101.034f, 4323.642f, 59.63914f), order_terminal),
        owning_building_guid = 104
      )
      LocalObject(
        3250,
        SpawnTube.Constructor(Vector3(1087.169f, 4313.389f, 57.78914f), respawn_tube_tower, Vector3(0, 0, 15)),
        owning_building_guid = 104
      )
      LocalObject(
        3251,
        SpawnTube.Constructor(Vector3(1091.416f, 4329.24f, 57.78914f), respawn_tube_tower, Vector3(0, 0, 15)),
        owning_building_guid = 104
      )
      LocalObject(
        2837,
        ProximityTerminal.Constructor(Vector3(1077.579f, 4323.188f, 95.87714f), pad_landing_tower_frame),
        owning_building_guid = 104
      )
      LocalObject(
        2838,
        Terminal.Constructor(Vector3(1077.579f, 4323.188f, 95.87714f), air_rearm_terminal),
        owning_building_guid = 104
      )
      LocalObject(
        2840,
        ProximityTerminal.Constructor(Vector3(1080.282f, 4333.277f, 95.87714f), pad_landing_tower_frame),
        owning_building_guid = 104
      )
      LocalObject(
        2841,
        Terminal.Constructor(Vector3(1080.282f, 4333.277f, 95.87714f), air_rearm_terminal),
        owning_building_guid = 104
      )
      LocalObject(
        2180,
        FacilityTurret.Constructor(Vector3(1061.708f, 4317.419f, 87.24914f), manned_turret),
        owning_building_guid = 104
      )
      TurretToWeapon(2180, 5129)
      LocalObject(
        2182,
        FacilityTurret.Constructor(Vector3(1106.568f, 4336.366f, 87.24914f), manned_turret),
        owning_building_guid = 104
      )
      TurretToWeapon(2182, 5130)
      LocalObject(
        2993,
        Painbox.Constructor(Vector3(1082.451f, 4319.94f, 60.32664f), painbox_radius_continuous),
        owning_building_guid = 104
      )
      LocalObject(
        2994,
        Painbox.Constructor(Vector3(1093.639f, 4313.516f, 58.40714f), painbox_radius_continuous),
        owning_building_guid = 104
      )
      LocalObject(
        2995,
        Painbox.Constructor(Vector3(1097.053f, 4325.524f, 58.40714f), painbox_radius_continuous),
        owning_building_guid = 104
      )
    }

    Building24()

    def Building24(): Unit = { // Name: NE_Aja_Tower Type: tower_c GUID: 105, MapID: 24
      LocalBuilding(
        "NE_Aja_Tower",
        105,
        24,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1410f, 5814f, 38.33094f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        3355,
        CaptureTerminal.Constructor(Vector3(1426.587f, 5813.897f, 48.32994f), secondary_capture),
        owning_building_guid = 105
      )
      LocalObject(478, Door.Constructor(Vector3(1422f, 5806f, 39.85194f)), owning_building_guid = 105)
      LocalObject(479, Door.Constructor(Vector3(1422f, 5806f, 59.85094f)), owning_building_guid = 105)
      LocalObject(480, Door.Constructor(Vector3(1422f, 5822f, 39.85194f)), owning_building_guid = 105)
      LocalObject(481, Door.Constructor(Vector3(1422f, 5822f, 59.85094f)), owning_building_guid = 105)
      LocalObject(3519, Door.Constructor(Vector3(1421.146f, 5802.794f, 29.66694f)), owning_building_guid = 105)
      LocalObject(3520, Door.Constructor(Vector3(1421.146f, 5819.204f, 29.66694f)), owning_building_guid = 105)
      LocalObject(
        1334,
        IFFLock.Constructor(Vector3(1419.957f, 5822.811f, 39.79193f), Vector3(0, 0, 0)),
        owning_building_guid = 105,
        door_guid = 480
      )
      LocalObject(
        1335,
        IFFLock.Constructor(Vector3(1419.957f, 5822.811f, 59.79194f), Vector3(0, 0, 0)),
        owning_building_guid = 105,
        door_guid = 481
      )
      LocalObject(
        1336,
        IFFLock.Constructor(Vector3(1424.047f, 5805.189f, 39.79193f), Vector3(0, 0, 180)),
        owning_building_guid = 105,
        door_guid = 478
      )
      LocalObject(
        1337,
        IFFLock.Constructor(Vector3(1424.047f, 5805.189f, 59.79194f), Vector3(0, 0, 180)),
        owning_building_guid = 105,
        door_guid = 479
      )
      LocalObject(1686, Locker.Constructor(Vector3(1425.716f, 5798.963f, 28.32494f)), owning_building_guid = 105)
      LocalObject(1687, Locker.Constructor(Vector3(1425.751f, 5820.835f, 28.32494f)), owning_building_guid = 105)
      LocalObject(1688, Locker.Constructor(Vector3(1427.053f, 5798.963f, 28.32494f)), owning_building_guid = 105)
      LocalObject(1689, Locker.Constructor(Vector3(1427.088f, 5820.835f, 28.32494f)), owning_building_guid = 105)
      LocalObject(1690, Locker.Constructor(Vector3(1429.741f, 5798.963f, 28.32494f)), owning_building_guid = 105)
      LocalObject(1691, Locker.Constructor(Vector3(1429.741f, 5820.835f, 28.32494f)), owning_building_guid = 105)
      LocalObject(1692, Locker.Constructor(Vector3(1431.143f, 5798.963f, 28.32494f)), owning_building_guid = 105)
      LocalObject(1693, Locker.Constructor(Vector3(1431.143f, 5820.835f, 28.32494f)), owning_building_guid = 105)
      LocalObject(
        2392,
        Terminal.Constructor(Vector3(1431.445f, 5804.129f, 29.66294f), order_terminal),
        owning_building_guid = 105
      )
      LocalObject(
        2393,
        Terminal.Constructor(Vector3(1431.445f, 5809.853f, 29.66294f), order_terminal),
        owning_building_guid = 105
      )
      LocalObject(
        2394,
        Terminal.Constructor(Vector3(1431.445f, 5815.234f, 29.66294f), order_terminal),
        owning_building_guid = 105
      )
      LocalObject(
        3255,
        SpawnTube.Constructor(Vector3(1420.706f, 5801.742f, 27.81294f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 105
      )
      LocalObject(
        3256,
        SpawnTube.Constructor(Vector3(1420.706f, 5818.152f, 27.81294f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 105
      )
      LocalObject(
        2843,
        ProximityTerminal.Constructor(Vector3(1408.907f, 5808.725f, 65.90094f), pad_landing_tower_frame),
        owning_building_guid = 105
      )
      LocalObject(
        2844,
        Terminal.Constructor(Vector3(1408.907f, 5808.725f, 65.90094f), air_rearm_terminal),
        owning_building_guid = 105
      )
      LocalObject(
        2846,
        ProximityTerminal.Constructor(Vector3(1408.907f, 5819.17f, 65.90094f), pad_landing_tower_frame),
        owning_building_guid = 105
      )
      LocalObject(
        2847,
        Terminal.Constructor(Vector3(1408.907f, 5819.17f, 65.90094f), air_rearm_terminal),
        owning_building_guid = 105
      )
      LocalObject(
        2188,
        FacilityTurret.Constructor(Vector3(1395.07f, 5799.045f, 57.27293f), manned_turret),
        owning_building_guid = 105
      )
      TurretToWeapon(2188, 5131)
      LocalObject(
        2190,
        FacilityTurret.Constructor(Vector3(1433.497f, 5828.957f, 57.27293f), manned_turret),
        owning_building_guid = 105
      )
      TurretToWeapon(2190, 5132)
      LocalObject(
        2996,
        Painbox.Constructor(Vector3(1414.454f, 5806.849f, 30.35044f), painbox_radius_continuous),
        owning_building_guid = 105
      )
      LocalObject(
        2997,
        Painbox.Constructor(Vector3(1426.923f, 5803.54f, 28.43094f), painbox_radius_continuous),
        owning_building_guid = 105
      )
      LocalObject(
        2998,
        Painbox.Constructor(Vector3(1427.113f, 5816.022f, 28.43094f), painbox_radius_continuous),
        owning_building_guid = 105
      )
    }

    Building69()

    def Building69(): Unit = { // Name: Outpost_Tower Type: tower_c GUID: 106, MapID: 69
      LocalBuilding(
        "Outpost_Tower",
        106,
        69,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2830f, 4342f, 89.42412f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        3360,
        CaptureTerminal.Constructor(Vector3(2846.587f, 4341.897f, 99.42312f), secondary_capture),
        owning_building_guid = 106
      )
      LocalObject(534, Door.Constructor(Vector3(2842f, 4334f, 90.94512f)), owning_building_guid = 106)
      LocalObject(535, Door.Constructor(Vector3(2842f, 4334f, 110.9441f)), owning_building_guid = 106)
      LocalObject(536, Door.Constructor(Vector3(2842f, 4350f, 90.94512f)), owning_building_guid = 106)
      LocalObject(537, Door.Constructor(Vector3(2842f, 4350f, 110.9441f)), owning_building_guid = 106)
      LocalObject(3535, Door.Constructor(Vector3(2841.146f, 4330.794f, 80.76012f)), owning_building_guid = 106)
      LocalObject(3536, Door.Constructor(Vector3(2841.146f, 4347.204f, 80.76012f)), owning_building_guid = 106)
      LocalObject(
        1375,
        IFFLock.Constructor(Vector3(2839.957f, 4350.811f, 90.88512f), Vector3(0, 0, 0)),
        owning_building_guid = 106,
        door_guid = 536
      )
      LocalObject(
        1376,
        IFFLock.Constructor(Vector3(2839.957f, 4350.811f, 110.8851f), Vector3(0, 0, 0)),
        owning_building_guid = 106,
        door_guid = 537
      )
      LocalObject(
        1377,
        IFFLock.Constructor(Vector3(2844.047f, 4333.189f, 90.88512f), Vector3(0, 0, 180)),
        owning_building_guid = 106,
        door_guid = 534
      )
      LocalObject(
        1378,
        IFFLock.Constructor(Vector3(2844.047f, 4333.189f, 110.8851f), Vector3(0, 0, 180)),
        owning_building_guid = 106,
        door_guid = 535
      )
      LocalObject(1750, Locker.Constructor(Vector3(2845.716f, 4326.963f, 79.41812f)), owning_building_guid = 106)
      LocalObject(1751, Locker.Constructor(Vector3(2845.751f, 4348.835f, 79.41812f)), owning_building_guid = 106)
      LocalObject(1752, Locker.Constructor(Vector3(2847.053f, 4326.963f, 79.41812f)), owning_building_guid = 106)
      LocalObject(1753, Locker.Constructor(Vector3(2847.088f, 4348.835f, 79.41812f)), owning_building_guid = 106)
      LocalObject(1754, Locker.Constructor(Vector3(2849.741f, 4326.963f, 79.41812f)), owning_building_guid = 106)
      LocalObject(1755, Locker.Constructor(Vector3(2849.741f, 4348.835f, 79.41812f)), owning_building_guid = 106)
      LocalObject(1756, Locker.Constructor(Vector3(2851.143f, 4326.963f, 79.41812f)), owning_building_guid = 106)
      LocalObject(1757, Locker.Constructor(Vector3(2851.143f, 4348.835f, 79.41812f)), owning_building_guid = 106)
      LocalObject(
        2417,
        Terminal.Constructor(Vector3(2851.445f, 4332.129f, 80.75612f), order_terminal),
        owning_building_guid = 106
      )
      LocalObject(
        2418,
        Terminal.Constructor(Vector3(2851.445f, 4337.853f, 80.75612f), order_terminal),
        owning_building_guid = 106
      )
      LocalObject(
        2419,
        Terminal.Constructor(Vector3(2851.445f, 4343.234f, 80.75612f), order_terminal),
        owning_building_guid = 106
      )
      LocalObject(
        3271,
        SpawnTube.Constructor(Vector3(2840.706f, 4329.742f, 78.90612f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 106
      )
      LocalObject(
        3272,
        SpawnTube.Constructor(Vector3(2840.706f, 4346.152f, 78.90612f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 106
      )
      LocalObject(
        2849,
        ProximityTerminal.Constructor(Vector3(2828.907f, 4336.725f, 116.9941f), pad_landing_tower_frame),
        owning_building_guid = 106
      )
      LocalObject(
        2850,
        Terminal.Constructor(Vector3(2828.907f, 4336.725f, 116.9941f), air_rearm_terminal),
        owning_building_guid = 106
      )
      LocalObject(
        2852,
        ProximityTerminal.Constructor(Vector3(2828.907f, 4347.17f, 116.9941f), pad_landing_tower_frame),
        owning_building_guid = 106
      )
      LocalObject(
        2853,
        Terminal.Constructor(Vector3(2828.907f, 4347.17f, 116.9941f), air_rearm_terminal),
        owning_building_guid = 106
      )
      LocalObject(
        2208,
        FacilityTurret.Constructor(Vector3(2815.07f, 4327.045f, 108.3661f), manned_turret),
        owning_building_guid = 106
      )
      TurretToWeapon(2208, 5133)
      LocalObject(
        2210,
        FacilityTurret.Constructor(Vector3(2853.497f, 4356.957f, 108.3661f), manned_turret),
        owning_building_guid = 106
      )
      TurretToWeapon(2210, 5134)
      LocalObject(
        3011,
        Painbox.Constructor(Vector3(2834.454f, 4334.849f, 81.44362f), painbox_radius_continuous),
        owning_building_guid = 106
      )
      LocalObject(
        3013,
        Painbox.Constructor(Vector3(2846.923f, 4331.54f, 79.52412f), painbox_radius_continuous),
        owning_building_guid = 106
      )
      LocalObject(
        3014,
        Painbox.Constructor(Vector3(2847.113f, 4344.022f, 79.52412f), painbox_radius_continuous),
        owning_building_guid = 106
      )
    }

    Building29()

    def Building29(): Unit = { // Name: SE_Tore_Tower Type: tower_c GUID: 107, MapID: 29
      LocalBuilding(
        "SE_Tore_Tower",
        107,
        29,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3730f, 1642f, 46.48213f), Vector3(0f, 0f, 38f), tower_c)
        )
      )
      LocalObject(
        3365,
        CaptureTerminal.Constructor(Vector3(3743.134f, 1652.131f, 56.48113f), secondary_capture),
        owning_building_guid = 107
      )
      LocalObject(570, Door.Constructor(Vector3(3734.531f, 1655.692f, 48.00313f)), owning_building_guid = 107)
      LocalObject(571, Door.Constructor(Vector3(3734.531f, 1655.692f, 68.00214f)), owning_building_guid = 107)
      LocalObject(572, Door.Constructor(Vector3(3744.381f, 1643.084f, 48.00313f)), owning_building_guid = 107)
      LocalObject(573, Door.Constructor(Vector3(3744.381f, 1643.084f, 68.00214f)), owning_building_guid = 107)
      LocalObject(3548, Door.Constructor(Vector3(3735.579f, 1652.963f, 37.81813f)), owning_building_guid = 107)
      LocalObject(3549, Door.Constructor(Vector3(3745.682f, 1640.032f, 37.81813f)), owning_building_guid = 107)
      LocalObject(
        1408,
        IFFLock.Constructor(Vector3(3732.422f, 1655.073f, 47.94313f), Vector3(0, 0, 322)),
        owning_building_guid = 107,
        door_guid = 570
      )
      LocalObject(
        1409,
        IFFLock.Constructor(Vector3(3732.422f, 1655.073f, 67.94313f), Vector3(0, 0, 322)),
        owning_building_guid = 107,
        door_guid = 571
      )
      LocalObject(
        1410,
        IFFLock.Constructor(Vector3(3746.494f, 1643.705f, 47.94313f), Vector3(0, 0, 142)),
        owning_building_guid = 107,
        door_guid = 572
      )
      LocalObject(
        1411,
        IFFLock.Constructor(Vector3(3746.494f, 1643.705f, 67.94313f), Vector3(0, 0, 142)),
        owning_building_guid = 107,
        door_guid = 573
      )
      LocalObject(1802, Locker.Constructor(Vector3(3738.204f, 1657.083f, 36.47613f)), owning_building_guid = 107)
      LocalObject(1803, Locker.Constructor(Vector3(3739.258f, 1657.906f, 36.47613f)), owning_building_guid = 107)
      LocalObject(1804, Locker.Constructor(Vector3(3741.348f, 1659.54f, 36.47613f)), owning_building_guid = 107)
      LocalObject(1805, Locker.Constructor(Vector3(3742.453f, 1660.403f, 36.47613f)), owning_building_guid = 107)
      LocalObject(1806, Locker.Constructor(Vector3(3751.642f, 1639.826f, 36.47613f)), owning_building_guid = 107)
      LocalObject(1807, Locker.Constructor(Vector3(3752.696f, 1640.65f, 36.47613f)), owning_building_guid = 107)
      LocalObject(1808, Locker.Constructor(Vector3(3754.814f, 1642.304f, 36.47613f)), owning_building_guid = 107)
      LocalObject(1809, Locker.Constructor(Vector3(3755.919f, 1643.168f, 36.47613f)), owning_building_guid = 107)
      LocalObject(
        2439,
        Terminal.Constructor(Vector3(3746.139f, 1656.175f, 37.81413f), order_terminal),
        owning_building_guid = 107
      )
      LocalObject(
        2440,
        Terminal.Constructor(Vector3(3749.452f, 1651.935f, 37.81413f), order_terminal),
        owning_building_guid = 107
      )
      LocalObject(
        2441,
        Terminal.Constructor(Vector3(3752.976f, 1647.424f, 37.81413f), order_terminal),
        owning_building_guid = 107
      )
      LocalObject(
        3284,
        SpawnTube.Constructor(Vector3(3735.88f, 1651.863f, 35.96413f), respawn_tube_tower, Vector3(0, 0, 322)),
        owning_building_guid = 107
      )
      LocalObject(
        3285,
        SpawnTube.Constructor(Vector3(3745.983f, 1638.932f, 35.96413f), respawn_tube_tower, Vector3(0, 0, 322)),
        owning_building_guid = 107
      )
      LocalObject(
        2855,
        ProximityTerminal.Constructor(Vector3(3725.956f, 1645.401f, 74.05213f), pad_landing_tower_frame),
        owning_building_guid = 107
      )
      LocalObject(
        2856,
        Terminal.Constructor(Vector3(3725.956f, 1645.401f, 74.05213f), air_rearm_terminal),
        owning_building_guid = 107
      )
      LocalObject(
        2858,
        ProximityTerminal.Constructor(Vector3(3732.386f, 1637.17f, 74.05213f), pad_landing_tower_frame),
        owning_building_guid = 107
      )
      LocalObject(
        2859,
        Terminal.Constructor(Vector3(3732.386f, 1637.17f, 74.05213f), air_rearm_terminal),
        owning_building_guid = 107
      )
      LocalObject(
        2222,
        FacilityTurret.Constructor(Vector3(3727.442f, 1621.023f, 65.42413f), manned_turret),
        owning_building_guid = 107
      )
      TurretToWeapon(2222, 5135)
      LocalObject(
        2223,
        FacilityTurret.Constructor(Vector3(3739.307f, 1668.252f, 65.42413f), manned_turret),
        owning_building_guid = 107
      )
      TurretToWeapon(2223, 5136)
      LocalObject(
        3026,
        Painbox.Constructor(Vector3(3737.912f, 1639.107f, 38.50163f), painbox_radius_continuous),
        owning_building_guid = 107
      )
      LocalObject(
        3027,
        Painbox.Constructor(Vector3(3742.24f, 1654.129f, 36.58213f), painbox_radius_continuous),
        owning_building_guid = 107
      )
      LocalObject(
        3028,
        Painbox.Constructor(Vector3(3749.776f, 1644.176f, 36.58213f), painbox_radius_continuous),
        owning_building_guid = 107
      )
    }

    Building37()

    def Building37(): Unit = { // Name: W_Chuku_Tower Type: tower_c GUID: 108, MapID: 37
      LocalBuilding(
        "W_Chuku_Tower",
        108,
        37,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3978f, 7028f, 49.14362f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        3367,
        CaptureTerminal.Constructor(Vector3(3994.587f, 7027.897f, 59.14262f), secondary_capture),
        owning_building_guid = 108
      )
      LocalObject(587, Door.Constructor(Vector3(3990f, 7020f, 50.66462f)), owning_building_guid = 108)
      LocalObject(588, Door.Constructor(Vector3(3990f, 7020f, 70.66362f)), owning_building_guid = 108)
      LocalObject(589, Door.Constructor(Vector3(3990f, 7036f, 50.66462f)), owning_building_guid = 108)
      LocalObject(590, Door.Constructor(Vector3(3990f, 7036f, 70.66362f)), owning_building_guid = 108)
      LocalObject(3552, Door.Constructor(Vector3(3989.146f, 7016.794f, 40.47961f)), owning_building_guid = 108)
      LocalObject(3553, Door.Constructor(Vector3(3989.146f, 7033.204f, 40.47961f)), owning_building_guid = 108)
      LocalObject(
        1421,
        IFFLock.Constructor(Vector3(3987.957f, 7036.811f, 50.60461f), Vector3(0, 0, 0)),
        owning_building_guid = 108,
        door_guid = 589
      )
      LocalObject(
        1422,
        IFFLock.Constructor(Vector3(3987.957f, 7036.811f, 70.60461f), Vector3(0, 0, 0)),
        owning_building_guid = 108,
        door_guid = 590
      )
      LocalObject(
        1423,
        IFFLock.Constructor(Vector3(3992.047f, 7019.189f, 50.60461f), Vector3(0, 0, 180)),
        owning_building_guid = 108,
        door_guid = 587
      )
      LocalObject(
        1424,
        IFFLock.Constructor(Vector3(3992.047f, 7019.189f, 70.60461f), Vector3(0, 0, 180)),
        owning_building_guid = 108,
        door_guid = 588
      )
      LocalObject(1818, Locker.Constructor(Vector3(3993.716f, 7012.963f, 39.13762f)), owning_building_guid = 108)
      LocalObject(1819, Locker.Constructor(Vector3(3993.751f, 7034.835f, 39.13762f)), owning_building_guid = 108)
      LocalObject(1820, Locker.Constructor(Vector3(3995.053f, 7012.963f, 39.13762f)), owning_building_guid = 108)
      LocalObject(1821, Locker.Constructor(Vector3(3995.088f, 7034.835f, 39.13762f)), owning_building_guid = 108)
      LocalObject(1822, Locker.Constructor(Vector3(3997.741f, 7012.963f, 39.13762f)), owning_building_guid = 108)
      LocalObject(1823, Locker.Constructor(Vector3(3997.741f, 7034.835f, 39.13762f)), owning_building_guid = 108)
      LocalObject(1824, Locker.Constructor(Vector3(3999.143f, 7012.963f, 39.13762f)), owning_building_guid = 108)
      LocalObject(1825, Locker.Constructor(Vector3(3999.143f, 7034.835f, 39.13762f)), owning_building_guid = 108)
      LocalObject(
        2446,
        Terminal.Constructor(Vector3(3999.445f, 7018.129f, 40.47562f), order_terminal),
        owning_building_guid = 108
      )
      LocalObject(
        2447,
        Terminal.Constructor(Vector3(3999.445f, 7023.853f, 40.47562f), order_terminal),
        owning_building_guid = 108
      )
      LocalObject(
        2448,
        Terminal.Constructor(Vector3(3999.445f, 7029.234f, 40.47562f), order_terminal),
        owning_building_guid = 108
      )
      LocalObject(
        3288,
        SpawnTube.Constructor(Vector3(3988.706f, 7015.742f, 38.62562f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 108
      )
      LocalObject(
        3289,
        SpawnTube.Constructor(Vector3(3988.706f, 7032.152f, 38.62562f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 108
      )
      LocalObject(
        2861,
        ProximityTerminal.Constructor(Vector3(3976.907f, 7022.725f, 76.71362f), pad_landing_tower_frame),
        owning_building_guid = 108
      )
      LocalObject(
        2862,
        Terminal.Constructor(Vector3(3976.907f, 7022.725f, 76.71362f), air_rearm_terminal),
        owning_building_guid = 108
      )
      LocalObject(
        2864,
        ProximityTerminal.Constructor(Vector3(3976.907f, 7033.17f, 76.71362f), pad_landing_tower_frame),
        owning_building_guid = 108
      )
      LocalObject(
        2865,
        Terminal.Constructor(Vector3(3976.907f, 7033.17f, 76.71362f), air_rearm_terminal),
        owning_building_guid = 108
      )
      LocalObject(
        2226,
        FacilityTurret.Constructor(Vector3(3963.07f, 7013.045f, 68.08562f), manned_turret),
        owning_building_guid = 108
      )
      TurretToWeapon(2226, 5137)
      LocalObject(
        2228,
        FacilityTurret.Constructor(Vector3(4001.497f, 7042.957f, 68.08562f), manned_turret),
        owning_building_guid = 108
      )
      TurretToWeapon(2228, 5138)
      LocalObject(
        3030,
        Painbox.Constructor(Vector3(3982.454f, 7020.849f, 41.16312f), painbox_radius_continuous),
        owning_building_guid = 108
      )
      LocalObject(
        3033,
        Painbox.Constructor(Vector3(3994.923f, 7017.54f, 39.24361f), painbox_radius_continuous),
        owning_building_guid = 108
      )
      LocalObject(
        3034,
        Painbox.Constructor(Vector3(3995.113f, 7030.022f, 39.24361f), painbox_radius_continuous),
        owning_building_guid = 108
      )
    }

    Building39()

    def Building39(): Unit = { // Name: E_Faro_Tower Type: tower_c GUID: 109, MapID: 39
      LocalBuilding(
        "E_Faro_Tower",
        109,
        39,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5340f, 5834f, 72.75002f), Vector3(0f, 0f, 331f), tower_c)
        )
      )
      LocalObject(
        3373,
        CaptureTerminal.Constructor(Vector3(5354.458f, 5825.868f, 82.74902f), secondary_capture),
        owning_building_guid = 109
      )
      LocalObject(685, Door.Constructor(Vector3(5346.617f, 5821.186f, 74.27103f)), owning_building_guid = 109)
      LocalObject(686, Door.Constructor(Vector3(5346.617f, 5821.186f, 94.27002f)), owning_building_guid = 109)
      LocalObject(687, Door.Constructor(Vector3(5354.374f, 5835.179f, 74.27103f)), owning_building_guid = 109)
      LocalObject(688, Door.Constructor(Vector3(5354.374f, 5835.179f, 94.27002f)), owning_building_guid = 109)
      LocalObject(3579, Door.Constructor(Vector3(5344.316f, 5818.795f, 64.08602f)), owning_building_guid = 109)
      LocalObject(3580, Door.Constructor(Vector3(5352.271f, 5833.148f, 64.08602f)), owning_building_guid = 109)
      LocalObject(
        1494,
        IFFLock.Constructor(Vector3(5348.014f, 5819.483f, 74.21102f), Vector3(0, 0, 209)),
        owning_building_guid = 109,
        door_guid = 685
      )
      LocalObject(
        1495,
        IFFLock.Constructor(Vector3(5348.014f, 5819.483f, 94.21102f), Vector3(0, 0, 209)),
        owning_building_guid = 109,
        door_guid = 686
      )
      LocalObject(
        1496,
        IFFLock.Constructor(Vector3(5352.98f, 5836.879f, 74.21102f), Vector3(0, 0, 29)),
        owning_building_guid = 109,
        door_guid = 687
      )
      LocalObject(
        1497,
        IFFLock.Constructor(Vector3(5352.98f, 5836.879f, 94.21102f), Vector3(0, 0, 29)),
        owning_building_guid = 109,
        door_guid = 688
      )
      LocalObject(1953, Locker.Constructor(Vector3(5346.456f, 5813.229f, 62.74402f)), owning_building_guid = 109)
      LocalObject(1954, Locker.Constructor(Vector3(5347.625f, 5812.581f, 62.74402f)), owning_building_guid = 109)
      LocalObject(1955, Locker.Constructor(Vector3(5349.976f, 5811.278f, 62.74402f)), owning_building_guid = 109)
      LocalObject(1956, Locker.Constructor(Vector3(5351.202f, 5810.598f, 62.74402f)), owning_building_guid = 109)
      LocalObject(1957, Locker.Constructor(Vector3(5357.09f, 5832.342f, 62.74402f)), owning_building_guid = 109)
      LocalObject(1958, Locker.Constructor(Vector3(5358.259f, 5831.693f, 62.74402f)), owning_building_guid = 109)
      LocalObject(1959, Locker.Constructor(Vector3(5360.58f, 5830.407f, 62.74402f)), owning_building_guid = 109)
      LocalObject(1960, Locker.Constructor(Vector3(5361.806f, 5829.728f, 62.74402f)), owning_building_guid = 109)
      LocalObject(
        2487,
        Terminal.Constructor(Vector3(5353.971f, 5814.97f, 64.08202f), order_terminal),
        owning_building_guid = 109
      )
      LocalObject(
        2488,
        Terminal.Constructor(Vector3(5356.746f, 5819.976f, 64.08202f), order_terminal),
        owning_building_guid = 109
      )
      LocalObject(
        2489,
        Terminal.Constructor(Vector3(5359.354f, 5824.683f, 64.08202f), order_terminal),
        owning_building_guid = 109
      )
      LocalObject(
        3315,
        SpawnTube.Constructor(Vector3(5343.421f, 5818.088f, 62.23203f), respawn_tube_tower, Vector3(0, 0, 29)),
        owning_building_guid = 109
      )
      LocalObject(
        3316,
        SpawnTube.Constructor(Vector3(5351.376f, 5832.441f, 62.23203f), respawn_tube_tower, Vector3(0, 0, 29)),
        owning_building_guid = 109
      )
      LocalObject(
        2867,
        ProximityTerminal.Constructor(Vector3(5336.487f, 5829.917f, 100.32f), pad_landing_tower_frame),
        owning_building_guid = 109
      )
      LocalObject(
        2868,
        Terminal.Constructor(Vector3(5336.487f, 5829.917f, 100.32f), air_rearm_terminal),
        owning_building_guid = 109
      )
      LocalObject(
        2870,
        ProximityTerminal.Constructor(Vector3(5341.55f, 5839.052f, 100.32f), pad_landing_tower_frame),
        owning_building_guid = 109
      )
      LocalObject(
        2871,
        Terminal.Constructor(Vector3(5341.55f, 5839.052f, 100.32f), air_rearm_terminal),
        owning_building_guid = 109
      )
      LocalObject(
        2259,
        FacilityTurret.Constructor(Vector3(5319.691f, 5828.158f, 91.69202f), manned_turret),
        owning_building_guid = 109
      )
      TurretToWeapon(2259, 5139)
      LocalObject(
        2260,
        FacilityTurret.Constructor(Vector3(5367.802f, 5835.69f, 91.69202f), manned_turret),
        owning_building_guid = 109
      )
      TurretToWeapon(2260, 5140)
      LocalObject(
        3050,
        Painbox.Constructor(Vector3(5340.429f, 5825.586f, 64.76952f), painbox_radius_continuous),
        owning_building_guid = 109
      )
      LocalObject(
        3051,
        Painbox.Constructor(Vector3(5349.73f, 5816.646f, 62.85002f), painbox_radius_continuous),
        owning_building_guid = 109
      )
      LocalObject(
        3052,
        Painbox.Constructor(Vector3(5355.947f, 5827.472f, 62.85002f), painbox_radius_continuous),
        owning_building_guid = 109
      )
    }

    Building44()

    def Building44(): Unit = { // Name: N_Kanng_Tower Type: tower_c GUID: 110, MapID: 44
      LocalBuilding(
        "N_Kanng_Tower",
        110,
        44,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5688f, 4896f, 46.99545f), Vector3(0f, 0f, 339f), tower_c)
        )
      )
      LocalObject(
        3376,
        CaptureTerminal.Constructor(Vector3(5703.448f, 4889.959f, 56.99445f), secondary_capture),
        owning_building_guid = 110
      )
      LocalObject(712, Door.Constructor(Vector3(5696.336f, 4884.231f, 48.51645f)), owning_building_guid = 110)
      LocalObject(713, Door.Constructor(Vector3(5696.336f, 4884.231f, 68.51546f)), owning_building_guid = 110)
      LocalObject(714, Door.Constructor(Vector3(5702.07f, 4899.168f, 48.51645f)), owning_building_guid = 110)
      LocalObject(715, Door.Constructor(Vector3(5702.07f, 4899.168f, 68.51546f)), owning_building_guid = 110)
      LocalObject(3588, Door.Constructor(Vector3(5694.39f, 4881.544f, 38.33145f)), owning_building_guid = 110)
      LocalObject(3589, Door.Constructor(Vector3(5700.271f, 4896.864f, 38.33145f)), owning_building_guid = 110)
      LocalObject(
        1516,
        IFFLock.Constructor(Vector3(5697.957f, 4882.74f, 48.45645f), Vector3(0, 0, 201)),
        owning_building_guid = 110,
        door_guid = 712
      )
      LocalObject(
        1517,
        IFFLock.Constructor(Vector3(5697.957f, 4882.74f, 68.45645f), Vector3(0, 0, 201)),
        owning_building_guid = 110,
        door_guid = 713
      )
      LocalObject(
        1518,
        IFFLock.Constructor(Vector3(5700.453f, 4900.658f, 48.45645f), Vector3(0, 0, 21)),
        owning_building_guid = 110,
        door_guid = 714
      )
      LocalObject(
        1519,
        IFFLock.Constructor(Vector3(5700.453f, 4900.658f, 68.45645f), Vector3(0, 0, 21)),
        owning_building_guid = 110,
        door_guid = 715
      )
      LocalObject(1989, Locker.Constructor(Vector3(5697.283f, 4876.33f, 36.98945f)), owning_building_guid = 110)
      LocalObject(1990, Locker.Constructor(Vector3(5698.532f, 4875.851f, 36.98945f)), owning_building_guid = 110)
      LocalObject(1991, Locker.Constructor(Vector3(5701.041f, 4874.887f, 36.98945f)), owning_building_guid = 110)
      LocalObject(1992, Locker.Constructor(Vector3(5702.35f, 4874.385f, 36.98945f)), owning_building_guid = 110)
      LocalObject(1993, Locker.Constructor(Vector3(5705.154f, 4896.736f, 36.98945f)), owning_building_guid = 110)
      LocalObject(1994, Locker.Constructor(Vector3(5706.402f, 4896.257f, 36.98945f)), owning_building_guid = 110)
      LocalObject(1995, Locker.Constructor(Vector3(5708.879f, 4895.307f, 36.98945f)), owning_building_guid = 110)
      LocalObject(1996, Locker.Constructor(Vector3(5710.188f, 4894.804f, 36.98945f)), owning_building_guid = 110)
      LocalObject(
        2502,
        Terminal.Constructor(Vector3(5704.483f, 4879.1f, 38.32745f), order_terminal),
        owning_building_guid = 110
      )
      LocalObject(
        2503,
        Terminal.Constructor(Vector3(5706.535f, 4884.443f, 38.32745f), order_terminal),
        owning_building_guid = 110
      )
      LocalObject(
        2504,
        Terminal.Constructor(Vector3(5708.463f, 4889.467f, 38.32745f), order_terminal),
        owning_building_guid = 110
      )
      LocalObject(
        3324,
        SpawnTube.Constructor(Vector3(5693.602f, 4880.72f, 36.47746f), respawn_tube_tower, Vector3(0, 0, 21)),
        owning_building_guid = 110
      )
      LocalObject(
        3325,
        SpawnTube.Constructor(Vector3(5699.483f, 4896.04f, 36.47746f), respawn_tube_tower, Vector3(0, 0, 21)),
        owning_building_guid = 110
      )
      LocalObject(
        2873,
        ProximityTerminal.Constructor(Vector3(5685.089f, 4891.467f, 74.56545f), pad_landing_tower_frame),
        owning_building_guid = 110
      )
      LocalObject(
        2874,
        Terminal.Constructor(Vector3(5685.089f, 4891.467f, 74.56545f), air_rearm_terminal),
        owning_building_guid = 110
      )
      LocalObject(
        2876,
        ProximityTerminal.Constructor(Vector3(5688.833f, 4901.218f, 74.56545f), pad_landing_tower_frame),
        owning_building_guid = 110
      )
      LocalObject(
        2877,
        Terminal.Constructor(Vector3(5688.833f, 4901.218f, 74.56545f), air_rearm_terminal),
        owning_building_guid = 110
      )
      LocalObject(
        2268,
        FacilityTurret.Constructor(Vector3(5668.702f, 4887.389f, 65.93745f), manned_turret),
        owning_building_guid = 110
      )
      TurretToWeapon(2268, 5141)
      LocalObject(
        2270,
        FacilityTurret.Constructor(Vector3(5715.296f, 4901.543f, 65.93745f), manned_turret),
        owning_building_guid = 110
      )
      TurretToWeapon(2270, 5142)
      LocalObject(
        3059,
        Painbox.Constructor(Vector3(5689.596f, 4887.728f, 39.01495f), painbox_radius_continuous),
        owning_building_guid = 110
      )
      LocalObject(
        3060,
        Painbox.Constructor(Vector3(5700.05f, 4880.169f, 37.09545f), painbox_radius_continuous),
        owning_building_guid = 110
      )
      LocalObject(
        3061,
        Painbox.Constructor(Vector3(5704.701f, 4891.755f, 37.09545f), painbox_radius_continuous),
        owning_building_guid = 110
      )
    }

    Building9()

    def Building9(): Unit = { // Name: WG_Cyssor_to_Searhus Type: warpgate GUID: 111, MapID: 9
      LocalBuilding(
        "WG_Cyssor_to_Searhus",
        111,
        9,
        FoundationBuilder(WarpGate.Structure(Vector3(1196f, 1470f, 67.54601f)))
      )
    }

    Building3()

    def Building3(): Unit = { // Name: WG_Cyssor_to_NCSanc Type: warpgate GUID: 112, MapID: 3
      LocalBuilding(
        "WG_Cyssor_to_NCSanc",
        112,
        3,
        FoundationBuilder(WarpGate.Structure(Vector3(2614f, 6722f, 61.31535f)))
      )
    }

    Building13()

    def Building13(): Unit = { // Name: WG_Cyssor_to_TRSanc Type: warpgate GUID: 113, MapID: 13
      LocalBuilding(
        "WG_Cyssor_to_TRSanc",
        113,
        13,
        FoundationBuilder(WarpGate.Structure(Vector3(5946f, 1892f, 62.92611f)))
      )
    }

    Building17()

    def Building17(): Unit = { // Name: WG_Cyssor_to_Solsar Type: warpgate GUID: 114, MapID: 17
      LocalBuilding(
        "WG_Cyssor_to_Solsar",
        114,
        17,
        FoundationBuilder(WarpGate.Structure(Vector3(7112f, 5252f, 57.40348f)))
      )
    }

    def Lattice(): Unit = {
      LatticeLink("Gunuku", "Faro")
      LatticeLink("Tore", "Itan")
      LatticeLink("Itan", "Shango")
      LatticeLink("Shango", "Orisha")
      LatticeLink("Shango", "Pamba")
      LatticeLink("Pamba", "Kaang")
      LatticeLink("Kaang", "Itan")
      LatticeLink("Gunuku", "Itan")
      LatticeLink("Honsi", "Faro")
      LatticeLink("Gunuku", "Kaang")
      LatticeLink("Honsi", "Nzame")
      LatticeLink("Wele", "Aja")
      LatticeLink("Faro", "Ekera")
      LatticeLink("Ekera", "Chuku")
      LatticeLink("Bomazi", "GW_Cyssor_N")
      LatticeLink("Orisha", "WG_Cyssor_to_TRSanc")
      LatticeLink("Wele", "WG_Cyssor_to_NCSanc")
      LatticeLink("Leza", "WG_Cyssor_to_Searhus")
      LatticeLink("Shango", "GW_Cyssor_S")
      LatticeLink("Ekera", "WG_Cyssor_to_Solsar")
      LatticeLink("Aja", "Bomazi")
      LatticeLink("Bomazi", "Nzame")
      LatticeLink("Aja", "Chuku")
      LatticeLink("Nzame", "Mukuru")
      LatticeLink("Mukuru", "Leza")
      LatticeLink("Leza", "Tore")
      LatticeLink("Tore", "Nzame")
    }

    Lattice()

  }
}
