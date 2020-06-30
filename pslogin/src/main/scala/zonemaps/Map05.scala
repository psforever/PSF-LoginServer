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

object Map05 { // Forseral
  val ZoneMap = new ZoneMap("map05") {
    Checksum = 107922342L

    Building13()

    def Building13(): Unit = { // Name: Eadon Type: amp_station GUID: 1, MapID: 13
      LocalBuilding(
        "Eadon",
        1,
        13,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(2716f, 2956f, 53.31126f),
            Vector3(0f, 0f, 228f),
            amp_station
          )
        )
      )
      LocalObject(
        156,
        CaptureTerminal.Constructor(Vector3(2718.234f, 2958.477f, 64.81926f), capture_terminal),
        owning_building_guid = 1
      )
      LocalObject(122, Door.Constructor(Vector3(2710.802f, 2960.397f, 66.21326f)), owning_building_guid = 1)
      LocalObject(123, Door.Constructor(Vector3(2720.914f, 2951.288f, 66.21326f)), owning_building_guid = 1)
      LocalObject(240, Door.Constructor(Vector3(2629.126f, 2953.21f, 63.02626f)), owning_building_guid = 1)
      LocalObject(241, Door.Constructor(Vector3(2629.529f, 2985.826f, 55.06226f)), owning_building_guid = 1)
      LocalObject(242, Door.Constructor(Vector3(2641.702f, 2999.345f, 63.02626f)), owning_building_guid = 1)
      LocalObject(243, Door.Constructor(Vector3(2642.646f, 2941.037f, 55.06226f)), owning_building_guid = 1)
      LocalObject(245, Door.Constructor(Vector3(2689.618f, 2979.754f, 60.03226f)), owning_building_guid = 1)
      LocalObject(246, Door.Constructor(Vector3(2690.113f, 2972.944f, 65.03825f)), owning_building_guid = 1)
      LocalObject(247, Door.Constructor(Vector3(2696.176f, 2979.678f, 65.03825f)), owning_building_guid = 1)
      LocalObject(249, Door.Constructor(Vector3(2720.241f, 3043.654f, 55.06226f)), owning_building_guid = 1)
      LocalObject(250, Door.Constructor(Vector3(2733.761f, 3031.48f, 63.02526f)), owning_building_guid = 1)
      LocalObject(251, Door.Constructor(Vector3(2735.543f, 2932.01f, 65.03825f)), owning_building_guid = 1)
      LocalObject(252, Door.Constructor(Vector3(2741.606f, 2938.744f, 65.03825f)), owning_building_guid = 1)
      LocalObject(253, Door.Constructor(Vector3(2742.382f, 2932.245f, 60.03226f)), owning_building_guid = 1)
      LocalObject(254, Door.Constructor(Vector3(2788.061f, 2898.262f, 63.02626f)), owning_building_guid = 1)
      LocalObject(255, Door.Constructor(Vector3(2795.453f, 2975.932f, 55.06226f)), owning_building_guid = 1)
      LocalObject(256, Door.Constructor(Vector3(2800.233f, 2911.781f, 55.06226f)), owning_building_guid = 1)
      LocalObject(257, Door.Constructor(Vector3(2808.973f, 2963.759f, 63.02526f)), owning_building_guid = 1)
      LocalObject(258, Door.Constructor(Vector3(2826.305f, 2958.948f, 55.03226f)), owning_building_guid = 1)
      LocalObject(463, Door.Constructor(Vector3(2696.114f, 2973.621f, 65.03226f)), owning_building_guid = 1)
      LocalObject(464, Door.Constructor(Vector3(2698.165f, 2972.059f, 60.03226f)), owning_building_guid = 1)
      LocalObject(465, Door.Constructor(Vector3(2699.645f, 2943.814f, 55.03226f)), owning_building_guid = 1)
      LocalObject(466, Door.Constructor(Vector3(2700.545f, 2980.681f, 55.03226f)), owning_building_guid = 1)
      LocalObject(467, Door.Constructor(Vector3(2705.59f, 2938.461f, 55.03226f)), owning_building_guid = 1)
      LocalObject(468, Door.Constructor(Vector3(2706.194f, 2980.977f, 55.03226f)), owning_building_guid = 1)
      LocalObject(469, Door.Constructor(Vector3(2707.082f, 2964.03f, 40.03226f)), owning_building_guid = 1)
      LocalObject(470, Door.Constructor(Vector3(2712.139f, 2975.624f, 40.03226f)), owning_building_guid = 1)
      LocalObject(471, Door.Constructor(Vector3(2712.139f, 2975.624f, 47.53226f)), owning_building_guid = 1)
      LocalObject(472, Door.Constructor(Vector3(2718.973f, 2953.323f, 47.53226f)), owning_building_guid = 1)
      LocalObject(473, Door.Constructor(Vector3(2728.494f, 2987.81f, 40.03226f)), owning_building_guid = 1)
      LocalObject(474, Door.Constructor(Vector3(2729.975f, 2959.565f, 47.53226f)), owning_building_guid = 1)
      LocalObject(475, Door.Constructor(Vector3(2730.567f, 2948.267f, 40.03226f)), owning_building_guid = 1)
      LocalObject(476, Door.Constructor(Vector3(2733.835f, 2939.941f, 60.03226f)), owning_building_guid = 1)
      LocalObject(477, Door.Constructor(Vector3(2735.603f, 2938.064f, 65.03226f)), owning_building_guid = 1)
      LocalObject(478, Door.Constructor(Vector3(2736.216f, 2948.562f, 47.53226f)), owning_building_guid = 1)
      LocalObject(479, Door.Constructor(Vector3(2741.865f, 2948.859f, 55.03226f)), owning_building_guid = 1)
      LocalObject(480, Door.Constructor(Vector3(2742.161f, 2943.209f, 55.03226f)), owning_building_guid = 1)
      LocalObject(481, Door.Constructor(Vector3(2745.146f, 2994.347f, 40.03226f)), owning_building_guid = 1)
      LocalObject(482, Door.Constructor(Vector3(2745.442f, 2988.698f, 40.03226f)), owning_building_guid = 1)
      LocalObject(483, Door.Constructor(Vector3(2752.571f, 2960.749f, 47.53226f)), owning_building_guid = 1)
      LocalObject(484, Door.Constructor(Vector3(2784.985f, 2990.771f, 47.53226f)), owning_building_guid = 1)
      LocalObject(683, Door.Constructor(Vector3(2696.498f, 2934.302f, 55.79126f)), owning_building_guid = 1)
      LocalObject(1983, Door.Constructor(Vector3(2721.302f, 2977.233f, 47.86526f)), owning_building_guid = 1)
      LocalObject(1984, Door.Constructor(Vector3(2726.722f, 2972.353f, 47.86526f)), owning_building_guid = 1)
      LocalObject(1985, Door.Constructor(Vector3(2732.139f, 2967.475f, 47.86526f)), owning_building_guid = 1)
      LocalObject(
        721,
        IFFLock.Constructor(Vector3(2696.923f, 2930.231f, 54.99126f), Vector3(0, 0, 222)),
        owning_building_guid = 1,
        door_guid = 683
      )
      LocalObject(
        743,
        IFFLock.Constructor(Vector3(2687.657f, 2978.772f, 59.97326f), Vector3(0, 0, 312)),
        owning_building_guid = 1,
        door_guid = 245
      )
      LocalObject(
        744,
        IFFLock.Constructor(Vector3(2691.106f, 2970.957f, 64.97226f), Vector3(0, 0, 222)),
        owning_building_guid = 1,
        door_guid = 246
      )
      LocalObject(
        745,
        IFFLock.Constructor(Vector3(2695.218f, 2981.634f, 64.97226f), Vector3(0, 0, 42)),
        owning_building_guid = 1,
        door_guid = 247
      )
      LocalObject(
        746,
        IFFLock.Constructor(Vector3(2706.543f, 2965.78f, 39.84726f), Vector3(0, 0, 42)),
        owning_building_guid = 1,
        door_guid = 469
      )
      LocalObject(
        747,
        IFFLock.Constructor(Vector3(2710.485f, 2974.998f, 47.34726f), Vector3(0, 0, 312)),
        owning_building_guid = 1,
        door_guid = 471
      )
      LocalObject(
        748,
        IFFLock.Constructor(Vector3(2731.726f, 2960.103f, 47.34726f), Vector3(0, 0, 132)),
        owning_building_guid = 1,
        door_guid = 474
      )
      LocalObject(
        749,
        IFFLock.Constructor(Vector3(2736.543f, 2930.046f, 64.97226f), Vector3(0, 0, 222)),
        owning_building_guid = 1,
        door_guid = 251
      )
      LocalObject(
        750,
        IFFLock.Constructor(Vector3(2740.651f, 2940.726f, 64.97226f), Vector3(0, 0, 42)),
        owning_building_guid = 1,
        door_guid = 252
      )
      LocalObject(
        751,
        IFFLock.Constructor(Vector3(2744.359f, 2933.216f, 59.97326f), Vector3(0, 0, 132)),
        owning_building_guid = 1,
        door_guid = 253
      )
      LocalObject(
        752,
        IFFLock.Constructor(Vector3(2745.772f, 2992.694f, 39.84726f), Vector3(0, 0, 222)),
        owning_building_guid = 1,
        door_guid = 481
      )
      LocalObject(
        753,
        IFFLock.Constructor(Vector3(2828.287f, 2959.915f, 54.97126f), Vector3(0, 0, 132)),
        owning_building_guid = 1,
        door_guid = 258
      )
      LocalObject(925, Locker.Constructor(Vector3(2709.031f, 2968.972f, 46.27225f)), owning_building_guid = 1)
      LocalObject(926, Locker.Constructor(Vector3(2709.8f, 2969.826f, 46.27225f)), owning_building_guid = 1)
      LocalObject(927, Locker.Constructor(Vector3(2710.567f, 2970.678f, 46.27225f)), owning_building_guid = 1)
      LocalObject(928, Locker.Constructor(Vector3(2710.727f, 2940.93f, 38.51126f)), owning_building_guid = 1)
      LocalObject(929, Locker.Constructor(Vector3(2711.346f, 2971.543f, 46.27225f)), owning_building_guid = 1)
      LocalObject(930, Locker.Constructor(Vector3(2711.621f, 2941.923f, 38.51126f)), owning_building_guid = 1)
      LocalObject(931, Locker.Constructor(Vector3(2712.515f, 2942.916f, 38.51126f)), owning_building_guid = 1)
      LocalObject(932, Locker.Constructor(Vector3(2713.401f, 2943.9f, 38.51126f)), owning_building_guid = 1)
      LocalObject(933, Locker.Constructor(Vector3(2716.439f, 2947.274f, 38.51126f)), owning_building_guid = 1)
      LocalObject(934, Locker.Constructor(Vector3(2717.333f, 2948.268f, 38.51126f)), owning_building_guid = 1)
      LocalObject(935, Locker.Constructor(Vector3(2718.228f, 2949.26f, 38.51126f)), owning_building_guid = 1)
      LocalObject(936, Locker.Constructor(Vector3(2719.114f, 2950.244f, 38.51126f)), owning_building_guid = 1)
      LocalObject(
        1346,
        Terminal.Constructor(Vector3(2699.298f, 2971.042f, 54.84026f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1347,
        Terminal.Constructor(Vector3(2713.192f, 2965.722f, 47.60126f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1348,
        Terminal.Constructor(Vector3(2715.965f, 2963.226f, 47.60126f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1349,
        Terminal.Constructor(Vector3(2718.781f, 2960.69f, 47.60126f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1350,
        Terminal.Constructor(Vector3(2731.03f, 2972.699f, 54.84026f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1351,
        Terminal.Constructor(Vector3(2732.705f, 2940.961f, 54.84026f), order_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1911,
        Terminal.Constructor(Vector3(2716.339f, 2963.101f, 60.03926f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1912,
        Terminal.Constructor(Vector3(2719.253f, 2978.677f, 48.14526f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1913,
        Terminal.Constructor(Vector3(2724.674f, 2973.801f, 48.14526f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1914,
        Terminal.Constructor(Vector3(2730.088f, 2968.922f, 48.14526f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1915,
        Terminal.Constructor(Vector3(2743.471f, 2963.482f, 40.06826f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1916,
        Terminal.Constructor(Vector3(2777.366f, 2965.258f, 47.56826f), spawn_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        2103,
        Terminal.Constructor(Vector3(2809.357f, 2936.43f, 55.41326f), vehicle_terminal_combined),
        owning_building_guid = 1
      )
      LocalObject(
        1309,
        VehicleSpawnPad.Constructor(Vector3(2799.162f, 2945.489f, 51.25626f), mb_pad_creation, Vector3(0, 0, -48)),
        owning_building_guid = 1,
        terminal_guid = 2103
      )
      LocalObject(1820, ResourceSilo.Constructor(Vector3(2617.239f, 2974.084f, 60.54626f)), owning_building_guid = 1)
      LocalObject(
        1834,
        SpawnTube.Constructor(Vector3(2720.817f, 2978.262f, 46.01126f), Vector3(0, 0, 132)),
        owning_building_guid = 1
      )
      LocalObject(
        1835,
        SpawnTube.Constructor(Vector3(2726.235f, 2973.384f, 46.01126f), Vector3(0, 0, 132)),
        owning_building_guid = 1
      )
      LocalObject(
        1836,
        SpawnTube.Constructor(Vector3(2731.651f, 2968.507f, 46.01126f), Vector3(0, 0, 132)),
        owning_building_guid = 1
      )
      LocalObject(
        1323,
        ProximityTerminal.Constructor(Vector3(2714.526f, 2945.964f, 38.51126f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1324,
        ProximityTerminal.Constructor(Vector3(2730.749f, 2972.389f, 58.51126f), medical_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1491,
        ProximityTerminal.Constructor(Vector3(2655.83f, 2999.011f, 61.83926f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1492,
        Terminal.Constructor(Vector3(2655.83f, 2999.011f, 61.83926f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1494,
        ProximityTerminal.Constructor(Vector3(2668.223f, 2932.578f, 61.81626f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1495,
        Terminal.Constructor(Vector3(2668.223f, 2932.578f, 61.81626f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1497,
        ProximityTerminal.Constructor(Vector3(2740.869f, 2897.818f, 63.97726f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1498,
        Terminal.Constructor(Vector3(2740.869f, 2897.818f, 63.97726f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1500,
        ProximityTerminal.Constructor(Vector3(2768.999f, 2985.314f, 61.81626f), pad_landing_frame),
        owning_building_guid = 1
      )
      LocalObject(
        1501,
        Terminal.Constructor(Vector3(2768.999f, 2985.314f, 61.81626f), air_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1740,
        ProximityTerminal.Constructor(Vector3(2674.589f, 2909.421f, 52.71126f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        1741,
        Terminal.Constructor(Vector3(2674.589f, 2909.421f, 52.71126f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1744,
        ProximityTerminal.Constructor(Vector3(2763.055f, 3007.956f, 52.71126f), repair_silo),
        owning_building_guid = 1
      )
      LocalObject(
        1745,
        Terminal.Constructor(Vector3(2763.055f, 3007.956f, 52.71126f), ground_rearm_terminal),
        owning_building_guid = 1
      )
      LocalObject(
        1228,
        FacilityTurret.Constructor(Vector3(2595.292f, 2966.677f, 62.01926f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1228, 5000)
      LocalObject(
        1229,
        FacilityTurret.Constructor(Vector3(2656.437f, 3037.9f, 62.01926f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1229, 5001)
      LocalObject(
        1230,
        FacilityTurret.Constructor(Vector3(2696.575f, 2872.485f, 62.01926f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1230, 5002)
      LocalObject(
        1231,
        FacilityTurret.Constructor(Vector3(2697.964f, 3080.691f, 62.01926f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1231, 5003)
      LocalObject(
        1232,
        FacilityTurret.Constructor(Vector3(2745.161f, 2831.722f, 62.01926f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1232, 5004)
      LocalObject(
        1233,
        FacilityTurret.Constructor(Vector3(2847.859f, 2945.764f, 62.01926f), manned_turret),
        owning_building_guid = 1
      )
      TurretToWeapon(1233, 5005)
      LocalObject(
        1629,
        Painbox.Constructor(Vector3(2750.932f, 3005.082f, 43.38226f), painbox),
        owning_building_guid = 1
      )
      LocalObject(
        1639,
        Painbox.Constructor(Vector3(2723.244f, 2966.157f, 50.95966f), painbox_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1649,
        Painbox.Constructor(Vector3(2743.115f, 2993.222f, 39.97026f), painbox_door_radius),
        owning_building_guid = 1
      )
      LocalObject(
        1659,
        Painbox.Constructor(Vector3(2711.349f, 2976.335f, 47.21126f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1660,
        Painbox.Constructor(Vector3(2717.959f, 2952.262f, 49.18986f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(
        1661,
        Painbox.Constructor(Vector3(2730.88f, 2958.651f, 47.27576f), painbox_door_radius_continuous),
        owning_building_guid = 1
      )
      LocalObject(220, Generator.Constructor(Vector3(2755.535f, 3005.924f, 37.21725f)), owning_building_guid = 1)
      LocalObject(
        210,
        Terminal.Constructor(Vector3(2750.089f, 2999.804f, 38.51126f), gen_control),
        owning_building_guid = 1
      )
    }

    Building10()

    def Building10(): Unit = { // Name: Pwyll Type: amp_station GUID: 4, MapID: 10
      LocalBuilding(
        "Pwyll",
        4,
        10,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4738f, 4852f, 103.9842f),
            Vector3(0f, 0f, 191f),
            amp_station
          )
        )
      )
      LocalObject(
        162,
        CaptureTerminal.Constructor(Vector3(4741.275f, 4852.634f, 115.4922f), capture_terminal),
        owning_building_guid = 4
      )
      LocalObject(124, Door.Constructor(Vector3(4736.495f, 4858.64f, 116.8862f)), owning_building_guid = 4)
      LocalObject(125, Door.Constructor(Vector3(4739.088f, 4845.279f, 116.8862f)), owning_building_guid = 4)
      LocalObject(365, Door.Constructor(Vector3(4666.941f, 4902.054f, 113.6992f)), owning_building_guid = 4)
      LocalObject(366, Door.Constructor(Vector3(4670.412f, 4884.196f, 105.7352f)), owning_building_guid = 4)
      LocalObject(368, Door.Constructor(Vector3(4686.891f, 4927.86f, 105.7352f)), owning_building_guid = 4)
      LocalObject(369, Door.Constructor(Vector3(4704.749f, 4931.331f, 113.6992f)), owning_building_guid = 4)
      LocalObject(373, Door.Constructor(Vector3(4727.523f, 4881.111f, 115.7112f)), owning_building_guid = 4)
      LocalObject(374, Door.Constructor(Vector3(4731.226f, 4886.848f, 110.7052f)), owning_building_guid = 4)
      LocalObject(375, Door.Constructor(Vector3(4736.417f, 4882.841f, 115.7112f)), owning_building_guid = 4)
      LocalObject(376, Door.Constructor(Vector3(4739.17f, 4821.079f, 115.7112f)), owning_building_guid = 4)
      LocalObject(381, Door.Constructor(Vector3(4744.774f, 4817.151f, 110.7052f)), owning_building_guid = 4)
      LocalObject(382, Door.Constructor(Vector3(4748.065f, 4822.809f, 115.7112f)), owning_building_guid = 4)
      LocalObject(383, Door.Constructor(Vector3(4760.802f, 4762.521f, 113.6992f)), owning_building_guid = 4)
      LocalObject(384, Door.Constructor(Vector3(4778.66f, 4765.992f, 105.7352f)), owning_building_guid = 4)
      LocalObject(385, Door.Constructor(Vector3(4794.138f, 4919.451f, 105.7352f)), owning_building_guid = 4)
      LocalObject(386, Door.Constructor(Vector3(4797.609f, 4901.592f, 113.6982f)), owning_building_guid = 4)
      LocalObject(387, Door.Constructor(Vector3(4813.45f, 4820.103f, 105.7352f)), owning_building_guid = 4)
      LocalObject(388, Door.Constructor(Vector3(4816.921f, 4802.244f, 113.6982f)), owning_building_guid = 4)
      LocalObject(392, Door.Constructor(Vector3(4827.868f, 4787.971f, 105.7052f)), owning_building_guid = 4)
      LocalObject(595, Door.Constructor(Vector3(4717.604f, 4852.11f, 105.7052f)), owning_building_guid = 4)
      LocalObject(596, Door.Constructor(Vector3(4719.131f, 4844.257f, 105.7052f)), owning_building_guid = 4)
      LocalObject(597, Door.Constructor(Vector3(4732.722f, 4878.041f, 115.7052f)), owning_building_guid = 4)
      LocalObject(598, Door.Constructor(Vector3(4733.42f, 4875.559f, 110.7052f)), owning_building_guid = 4)
      LocalObject(599, Door.Constructor(Vector3(4735.71f, 4863.779f, 90.70519f)), owning_building_guid = 4)
      LocalObject(600, Door.Constructor(Vector3(4738.763f, 4848.074f, 98.20519f)), owning_building_guid = 4)
      LocalObject(601, Door.Constructor(Vector3(4740.51f, 4881.012f, 105.7052f)), owning_building_guid = 4)
      LocalObject(602, Door.Constructor(Vector3(4742.58f, 4828.441f, 110.7052f)), owning_building_guid = 4)
      LocalObject(603, Door.Constructor(Vector3(4742.861f, 4825.879f, 115.7052f)), owning_building_guid = 4)
      LocalObject(604, Door.Constructor(Vector3(4744.979f, 4837.057f, 90.70519f)), owning_building_guid = 4)
      LocalObject(605, Door.Constructor(Vector3(4745.2f, 4877.849f, 105.7052f)), owning_building_guid = 4)
      LocalObject(606, Door.Constructor(Vector3(4746.727f, 4869.996f, 90.70519f)), owning_building_guid = 4)
      LocalObject(607, Door.Constructor(Vector3(4746.727f, 4869.996f, 98.20519f)), owning_building_guid = 4)
      LocalObject(608, Door.Constructor(Vector3(4749.669f, 4833.894f, 98.20519f)), owning_building_guid = 4)
      LocalObject(609, Door.Constructor(Vector3(4751.196f, 4826.041f, 105.7052f)), owning_building_guid = 4)
      LocalObject(610, Door.Constructor(Vector3(4751.306f, 4846.437f, 98.20519f)), owning_building_guid = 4)
      LocalObject(611, Door.Constructor(Vector3(4754.359f, 4830.73f, 105.7052f)), owning_building_guid = 4)
      LocalObject(612, Door.Constructor(Vector3(4767.123f, 4869.885f, 90.70519f)), owning_building_guid = 4)
      LocalObject(613, Door.Constructor(Vector3(4770.065f, 4833.784f, 98.20519f)), owning_building_guid = 4)
      LocalObject(614, Door.Constructor(Vector3(4781.191f, 4860.396f, 90.70519f)), owning_building_guid = 4)
      LocalObject(615, Door.Constructor(Vector3(4784.355f, 4865.085f, 90.70519f)), owning_building_guid = 4)
      LocalObject(616, Door.Constructor(Vector3(4814.02f, 4838.253f, 98.20519f)), owning_building_guid = 4)
      LocalObject(689, Door.Constructor(Vector3(4709.367f, 4846.408f, 106.4642f)), owning_building_guid = 4)
      LocalObject(2019, Door.Constructor(Vector3(4755.013f, 4865.766f, 98.53819f)), owning_building_guid = 4)
      LocalObject(2020, Door.Constructor(Vector3(4756.404f, 4858.607f, 98.53819f)), owning_building_guid = 4)
      LocalObject(2021, Door.Constructor(Vector3(4757.795f, 4851.452f, 98.53819f)), owning_building_guid = 4)
      LocalObject(
        727,
        IFFLock.Constructor(Vector3(4707.256f, 4842.901f, 105.6642f), Vector3(0, 0, 259)),
        owning_building_guid = 4,
        door_guid = 689
      )
      LocalObject(
        839,
        IFFLock.Constructor(Vector3(4727.12f, 4878.927f, 115.6452f), Vector3(0, 0, 259)),
        owning_building_guid = 4,
        door_guid = 373
      )
      LocalObject(
        840,
        IFFLock.Constructor(Vector3(4729.069f, 4887.244f, 110.6462f), Vector3(0, 0, 349)),
        owning_building_guid = 4,
        door_guid = 374
      )
      LocalObject(
        841,
        IFFLock.Constructor(Vector3(4736.333f, 4865.502f, 90.5202f), Vector3(0, 0, 79)),
        owning_building_guid = 4,
        door_guid = 599
      )
      LocalObject(
        842,
        IFFLock.Constructor(Vector3(4736.83f, 4884.979f, 115.6452f), Vector3(0, 0, 79)),
        owning_building_guid = 4,
        door_guid = 375
      )
      LocalObject(
        845,
        IFFLock.Constructor(Vector3(4738.787f, 4818.909f, 115.6452f), Vector3(0, 0, 259)),
        owning_building_guid = 4,
        door_guid = 376
      )
      LocalObject(
        848,
        IFFLock.Constructor(Vector3(4745.029f, 4870.491f, 98.0202f), Vector3(0, 0, 349)),
        owning_building_guid = 4,
        door_guid = 607
      )
      LocalObject(
        849,
        IFFLock.Constructor(Vector3(4746.937f, 4816.736f, 110.6462f), Vector3(0, 0, 169)),
        owning_building_guid = 4,
        door_guid = 381
      )
      LocalObject(
        850,
        IFFLock.Constructor(Vector3(4748.495f, 4824.966f, 115.6452f), Vector3(0, 0, 79)),
        owning_building_guid = 4,
        door_guid = 382
      )
      LocalObject(
        851,
        IFFLock.Constructor(Vector3(4753.029f, 4845.813f, 98.0202f), Vector3(0, 0, 169)),
        owning_building_guid = 4,
        door_guid = 610
      )
      LocalObject(
        852,
        IFFLock.Constructor(Vector3(4783.86f, 4863.388f, 90.5202f), Vector3(0, 0, 259)),
        owning_building_guid = 4,
        door_guid = 615
      )
      LocalObject(
        856,
        IFFLock.Constructor(Vector3(4830.032f, 4787.55f, 105.6442f), Vector3(0, 0, 169)),
        owning_building_guid = 4,
        door_guid = 392
      )
      LocalObject(1079, Locker.Constructor(Vector3(4724.719f, 4843.138f, 89.18419f)), owning_building_guid = 4)
      LocalObject(1080, Locker.Constructor(Vector3(4726.031f, 4843.393f, 89.18419f)), owning_building_guid = 4)
      LocalObject(1081, Locker.Constructor(Vector3(4727.343f, 4843.648f, 89.18419f)), owning_building_guid = 4)
      LocalObject(1082, Locker.Constructor(Vector3(4728.643f, 4843.901f, 89.18419f)), owning_building_guid = 4)
      LocalObject(1083, Locker.Constructor(Vector3(4733.099f, 4844.767f, 89.18419f)), owning_building_guid = 4)
      LocalObject(1084, Locker.Constructor(Vector3(4734.412f, 4845.022f, 89.18419f)), owning_building_guid = 4)
      LocalObject(1085, Locker.Constructor(Vector3(4735.723f, 4845.277f, 89.18419f)), owning_building_guid = 4)
      LocalObject(1086, Locker.Constructor(Vector3(4737.022f, 4845.53f, 89.18419f)), owning_building_guid = 4)
      LocalObject(1087, Locker.Constructor(Vector3(4740.241f, 4866.554f, 96.94519f)), owning_building_guid = 4)
      LocalObject(1088, Locker.Constructor(Vector3(4741.369f, 4866.773f, 96.94519f)), owning_building_guid = 4)
      LocalObject(1089, Locker.Constructor(Vector3(4742.495f, 4866.992f, 96.94519f)), owning_building_guid = 4)
      LocalObject(1090, Locker.Constructor(Vector3(4743.638f, 4867.214f, 96.94519f)), owning_building_guid = 4)
      LocalObject(
        1403,
        Terminal.Constructor(Vector3(4733.713f, 4874.064f, 105.5132f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1404,
        Terminal.Constructor(Vector3(4741.608f, 4861.454f, 98.27419f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1405,
        Terminal.Constructor(Vector3(4742.291f, 4829.937f, 105.5132f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1406,
        Terminal.Constructor(Vector3(4742.32f, 4857.792f, 98.27419f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1407,
        Terminal.Constructor(Vector3(4743.043f, 4854.072f, 98.27419f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1411,
        Terminal.Constructor(Vector3(4760.053f, 4856.291f, 105.5132f), order_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1953,
        Terminal.Constructor(Vector3(4742.544f, 4857.468f, 110.7122f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1954,
        Terminal.Constructor(Vector3(4754.245f, 4868.153f, 98.81819f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1955,
        Terminal.Constructor(Vector3(4755.641f, 4860.996f, 98.81819f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1956,
        Terminal.Constructor(Vector3(4757.028f, 4853.842f, 98.81819f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1957,
        Terminal.Constructor(Vector3(4764.442f, 4841.442f, 90.7412f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1958,
        Terminal.Constructor(Vector3(4792.581f, 4822.463f, 98.2412f), spawn_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        2109,
        Terminal.Constructor(Vector3(4800.781f, 4780.187f, 106.0862f), vehicle_terminal_combined),
        owning_building_guid = 4
      )
      LocalObject(
        1317,
        VehicleSpawnPad.Constructor(Vector3(4798.09f, 4793.557f, 101.9292f), mb_pad_creation, Vector3(0, 0, -11)),
        owning_building_guid = 4,
        terminal_guid = 2109
      )
      LocalObject(1826, ResourceSilo.Constructor(Vector3(4670.009f, 4925.879f, 111.2192f)), owning_building_guid = 4)
      LocalObject(
        1870,
        SpawnTube.Constructor(Vector3(4755.245f, 4866.881f, 96.68419f), Vector3(0, 0, 169)),
        owning_building_guid = 4
      )
      LocalObject(
        1871,
        SpawnTube.Constructor(Vector3(4756.636f, 4859.724f, 96.68419f), Vector3(0, 0, 169)),
        owning_building_guid = 4
      )
      LocalObject(
        1872,
        SpawnTube.Constructor(Vector3(4758.026f, 4852.57f, 96.68419f), Vector3(0, 0, 169)),
        owning_building_guid = 4
      )
      LocalObject(
        1333,
        ProximityTerminal.Constructor(Vector3(4730.784f, 4844.872f, 89.18419f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1334,
        ProximityTerminal.Constructor(Vector3(4759.642f, 4856.213f, 109.1842f), medical_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1557,
        ProximityTerminal.Constructor(Vector3(4685.748f, 4862.047f, 112.4892f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1558,
        Terminal.Constructor(Vector3(4685.748f, 4862.047f, 112.4892f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1560,
        ProximityTerminal.Constructor(Vector3(4715.831f, 4922.562f, 112.5122f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1561,
        Terminal.Constructor(Vector3(4715.831f, 4922.562f, 112.5122f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1566,
        ProximityTerminal.Constructor(Vector3(4722.846f, 4790.568f, 114.6502f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1567,
        Terminal.Constructor(Vector3(4722.846f, 4790.568f, 114.6502f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1569,
        ProximityTerminal.Constructor(Vector3(4797.969f, 4843.516f, 112.4892f), pad_landing_frame),
        owning_building_guid = 4
      )
      LocalObject(
        1570,
        Terminal.Constructor(Vector3(4797.969f, 4843.516f, 112.4892f), air_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1784,
        ProximityTerminal.Constructor(Vector3(4676.896f, 4839.722f, 103.3842f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        1785,
        Terminal.Constructor(Vector3(4676.896f, 4839.722f, 103.3842f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1792,
        ProximityTerminal.Constructor(Vector3(4806.848f, 4865.175f, 103.3842f), repair_silo),
        owning_building_guid = 4
      )
      LocalObject(
        1793,
        Terminal.Constructor(Vector3(4806.848f, 4865.175f, 103.3842f), ground_rearm_terminal),
        owning_building_guid = 4
      )
      LocalObject(
        1273,
        FacilityTurret.Constructor(Vector3(4648.024f, 4933.171f, 112.6922f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1273, 5006)
      LocalObject(
        1274,
        FacilityTurret.Constructor(Vector3(4672.226f, 4796.992f, 112.6922f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1274, 5007)
      LocalObject(
        1275,
        FacilityTurret.Constructor(Vector3(4686.497f, 4735.198f, 112.6922f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1275, 5008)
      LocalObject(
        1279,
        FacilityTurret.Constructor(Vector3(4739.719f, 4953.254f, 112.6922f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1279, 5009)
      LocalObject(
        1282,
        FacilityTurret.Constructor(Vector3(4798.637f, 4962.437f, 112.6922f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1282, 5010)
      LocalObject(
        1283,
        FacilityTurret.Constructor(Vector3(4837.147f, 4764.471f, 112.6922f), manned_turret),
        owning_building_guid = 4
      )
      TurretToWeapon(1283, 5011)
      LocalObject(
        1635,
        Painbox.Constructor(Vector3(4795.436f, 4870.176f, 94.05519f), painbox),
        owning_building_guid = 4
      )
      LocalObject(
        1645,
        Painbox.Constructor(Vector3(4749.898f, 4855.753f, 101.6326f), painbox_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        1655,
        Painbox.Constructor(Vector3(4782.056f, 4865.409f, 90.64319f), painbox_door_radius),
        owning_building_guid = 4
      )
      LocalObject(
        1677,
        Painbox.Constructor(Vector3(4737.315f, 4847.835f, 99.86279f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        1678,
        Painbox.Constructor(Vector3(4746.523f, 4871.04f, 97.88419f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(
        1679,
        Painbox.Constructor(Vector3(4751.479f, 4845.162f, 97.94869f), painbox_door_radius_continuous),
        owning_building_guid = 4
      )
      LocalObject(226, Generator.Constructor(Vector3(4799.619f, 4868.078f, 87.89019f)), owning_building_guid = 4)
      LocalObject(
        216,
        Terminal.Constructor(Vector3(4791.587f, 4866.469f, 89.18419f), gen_control),
        owning_building_guid = 4
      )
    }

    Building33()

    def Building33(): Unit = { // Name: bunkerg1 Type: bunker_gauntlet GUID: 7, MapID: 33
      LocalBuilding(
        "bunkerg1",
        7,
        33,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(2700f, 2850f, 53.31126f),
            Vector3(0f, 0f, 138f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(244, Door.Constructor(Vector3(2682.751f, 2868.089f, 54.83226f)), owning_building_guid = 7)
      LocalObject(248, Door.Constructor(Vector3(2719.782f, 2834.761f, 54.83226f)), owning_building_guid = 7)
    }

    Building34()

    def Building34(): Unit = { // Name: bunkerg2 Type: bunker_gauntlet GUID: 8, MapID: 34
      LocalBuilding(
        "bunkerg2",
        8,
        34,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(4314f, 4146f, 75.96667f),
            Vector3(0f, 0f, 132f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(335, Door.Constructor(Vector3(4298.736f, 4165.793f, 77.48768f)), owning_building_guid = 8)
      LocalObject(339, Door.Constructor(Vector3(4332.081f, 4128.776f, 77.48768f)), owning_building_guid = 8)
    }

    Building35()

    def Building35(): Unit = { // Name: bunkerg3 Type: bunker_gauntlet GUID: 9, MapID: 35
      LocalBuilding(
        "bunkerg3",
        9,
        35,
        FoundationBuilder(
          Building.Structure(
            StructureType.Bunker,
            Vector3(5806f, 4440f, 54.7853f),
            Vector3(0f, 0f, 91f),
            bunker_gauntlet
          )
        )
      )
      LocalObject(415, Door.Constructor(Vector3(5807.466f, 4464.952f, 56.3063f)), owning_building_guid = 9)
      LocalObject(416, Door.Constructor(Vector3(5808.346f, 4415.139f, 56.3063f)), owning_building_guid = 9)
    }

    Building29()

    def Building29(): Unit = { // Name: bunker2 Type: bunker_lg GUID: 10, MapID: 29
      LocalBuilding(
        "bunker2",
        10,
        29,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4302f, 4344f, 75.92862f), Vector3(0f, 0f, 262f), bunker_lg)
        )
      )
      LocalObject(337, Door.Constructor(Vector3(4304.169f, 4341.063f, 77.44962f)), owning_building_guid = 10)
    }

    Building30()

    def Building30(): Unit = { // Name: bunker3 Type: bunker_lg GUID: 11, MapID: 30
      LocalBuilding(
        "bunker3",
        11,
        30,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(5616f, 3768f, 60.98056f), Vector3(0f, 0f, 104f), bunker_lg)
        )
      )
      LocalObject(414, Door.Constructor(Vector3(5612.889f, 3769.91f, 62.50156f)), owning_building_guid = 11)
    }

    Building31()

    def Building31(): Unit = { // Name: bunker4 Type: bunker_sm GUID: 12, MapID: 31
      LocalBuilding(
        "bunker4",
        12,
        31,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3206f, 2552f, 56.18124f), Vector3(0f, 0f, 316f), bunker_sm)
        )
      )
      LocalObject(259, Door.Constructor(Vector3(3206.843f, 2551.109f, 57.70224f)), owning_building_guid = 12)
    }

    Building32()

    def Building32(): Unit = { // Name: bunker5 Type: bunker_sm GUID: 13, MapID: 32
      LocalBuilding(
        "bunker5",
        13,
        32,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3250f, 2604f, 56.18457f), Vector3(0f, 0f, 322f), bunker_sm)
        )
      )
      LocalObject(260, Door.Constructor(Vector3(3250.931f, 2603.202f, 57.70557f)), owning_building_guid = 13)
    }

    Building40()

    def Building40(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 14, MapID: 40
      LocalBuilding(
        "bunker_sm",
        14,
        40,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3650f, 5340f, 50.30309f), Vector3(0f, 0f, 308f), bunker_sm)
        )
      )
      LocalObject(309, Door.Constructor(Vector3(3650.711f, 5339.001f, 51.82409f)), owning_building_guid = 14)
    }

    Building28()

    def Building28(): Unit = { // Name: bunker1 Type: bunker_sm GUID: 15, MapID: 28
      LocalBuilding(
        "bunker1",
        15,
        28,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(3712f, 4644f, 58.56919f), Vector3(0f, 0f, 137f), bunker_sm)
        )
      )
      LocalObject(324, Door.Constructor(Vector3(3711.142f, 4644.875f, 60.09019f)), owning_building_guid = 15)
    }

    Building41()

    def Building41(): Unit = { // Name: bunker_sm Type: bunker_sm GUID: 16, MapID: 41
      LocalBuilding(
        "bunker_sm",
        16,
        41,
        FoundationBuilder(
          Building.Structure(StructureType.Bunker, Vector3(4888f, 2866f, 61.5153f), Vector3(0f, 0f, 222f), bunker_sm)
        )
      )
      LocalObject(396, Door.Constructor(Vector3(4887.053f, 2865.221f, 63.0363f)), owning_building_guid = 16)
    }

    Building36()

    def Building36(): Unit = { // Name: Caer Type: comm_station GUID: 17, MapID: 36
      LocalBuilding(
        "Caer",
        17,
        36,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4658f, 2644f, 55.99231f),
            Vector3(0f, 0f, 255f),
            comm_station
          )
        )
      )
      LocalObject(
        161,
        CaptureTerminal.Constructor(Vector3(4720.708f, 2581.553f, 38.69231f), capture_terminal),
        owning_building_guid = 17
      )
      LocalObject(356, Door.Constructor(Vector3(4598.849f, 2685.214f, 57.74331f)), owning_building_guid = 17)
      LocalObject(357, Door.Constructor(Vector3(4603.557f, 2702.787f, 65.70731f)), owning_building_guid = 17)
      LocalObject(358, Door.Constructor(Vector3(4631.46f, 2713.025f, 57.74331f)), owning_building_guid = 17)
      LocalObject(359, Door.Constructor(Vector3(4632.09f, 2588.618f, 65.70731f)), owning_building_guid = 17)
      LocalObject(360, Door.Constructor(Vector3(4649.034f, 2708.316f, 65.70731f)), owning_building_guid = 17)
      LocalObject(361, Door.Constructor(Vector3(4649.663f, 2583.909f, 57.74331f)), owning_building_guid = 17)
      LocalObject(362, Door.Constructor(Vector3(4651.975f, 2650.144f, 70.15231f)), owning_building_guid = 17)
      LocalObject(363, Door.Constructor(Vector3(4654.706f, 2665.588f, 62.71331f)), owning_building_guid = 17)
      LocalObject(364, Door.Constructor(Vector3(4663.461f, 2663.242f, 62.71331f)), owning_building_guid = 17)
      LocalObject(367, Door.Constructor(Vector3(4675.248f, 2631.096f, 62.71331f)), owning_building_guid = 17)
      LocalObject(370, Door.Constructor(Vector3(4718.823f, 2608.351f, 65.70631f)), owning_building_guid = 17)
      LocalObject(371, Door.Constructor(Vector3(4723.128f, 2593.42f, 57.71331f)), owning_building_guid = 17)
      LocalObject(372, Door.Constructor(Vector3(4723.531f, 2625.923f, 57.74331f)), owning_building_guid = 17)
      LocalObject(571, Door.Constructor(Vector3(4654.339f, 2599.429f, 47.71331f)), owning_building_guid = 17)
      LocalObject(572, Door.Constructor(Vector3(4655.097f, 2586.802f, 40.21331f)), owning_building_guid = 17)
      LocalObject(573, Door.Constructor(Vector3(4658.277f, 2660.49f, 62.71331f)), owning_building_guid = 17)
      LocalObject(574, Door.Constructor(Vector3(4660.551f, 2622.611f, 52.71331f)), owning_building_guid = 17)
      LocalObject(575, Door.Constructor(Vector3(4661.864f, 2642.965f, 50.21331f)), owning_building_guid = 17)
      LocalObject(576, Door.Constructor(Vector3(4662.622f, 2630.338f, 57.71331f)), owning_building_guid = 17)
      LocalObject(577, Door.Constructor(Vector3(4666.763f, 2645.793f, 40.21331f)), owning_building_guid = 17)
      LocalObject(578, Door.Constructor(Vector3(4667.521f, 2633.167f, 62.71331f)), owning_building_guid = 17)
      LocalObject(579, Door.Constructor(Vector3(4670.552f, 2582.661f, 47.71331f)), owning_building_guid = 17)
      LocalObject(580, Door.Constructor(Vector3(4673.732f, 2656.349f, 57.71331f)), owning_building_guid = 17)
      LocalObject(581, Door.Constructor(Vector3(4673.935f, 2610.742f, 40.21331f)), owning_building_guid = 17)
      LocalObject(582, Door.Constructor(Vector3(4675.248f, 2631.096f, 52.71331f)), owning_building_guid = 17)
      LocalObject(583, Door.Constructor(Vector3(4677.521f, 2593.217f, 47.71331f)), owning_building_guid = 17)
      LocalObject(584, Door.Constructor(Vector3(4678.631f, 2659.177f, 50.21331f)), owning_building_guid = 17)
      LocalObject(585, Door.Constructor(Vector3(4686.358f, 2657.107f, 40.21331f)), owning_building_guid = 17)
      LocalObject(586, Door.Constructor(Vector3(4692.773f, 2634.683f, 50.21331f)), owning_building_guid = 17)
      LocalObject(587, Door.Constructor(Vector3(4693.734f, 2576.449f, 40.21331f)), owning_building_guid = 17)
      LocalObject(588, Door.Constructor(Vector3(4697.672f, 2637.511f, 40.21331f)), owning_building_guid = 17)
      LocalObject(589, Door.Constructor(Vector3(4698.227f, 2670.491f, 50.21331f)), owning_building_guid = 17)
      LocalObject(590, Door.Constructor(Vector3(4704.845f, 2602.46f, 40.21331f)), owning_building_guid = 17)
      LocalObject(591, Door.Constructor(Vector3(4705.4f, 2635.44f, 50.21331f)), owning_building_guid = 17)
      LocalObject(592, Door.Constructor(Vector3(4711.26f, 2580.036f, 40.21331f)), owning_building_guid = 17)
      LocalObject(593, Door.Constructor(Vector3(4711.611f, 2658.623f, 50.21331f)), owning_building_guid = 17)
      LocalObject(594, Door.Constructor(Vector3(4713.33f, 2587.763f, 40.21331f)), owning_building_guid = 17)
      LocalObject(688, Door.Constructor(Vector3(4649.488f, 2627.949f, 58.48531f)), owning_building_guid = 17)
      LocalObject(2014, Door.Constructor(Vector3(4661.773f, 2605.022f, 48.04631f)), owning_building_guid = 17)
      LocalObject(2015, Door.Constructor(Vector3(4668.818f, 2603.135f, 48.04631f)), owning_building_guid = 17)
      LocalObject(2016, Door.Constructor(Vector3(4675.858f, 2601.248f, 48.04631f)), owning_building_guid = 17)
      LocalObject(
        726,
        IFFLock.Constructor(Vector3(4651.866f, 2624.469f, 57.64431f), Vector3(0, 0, 195)),
        owning_building_guid = 17,
        door_guid = 688
      )
      LocalObject(
        828,
        IFFLock.Constructor(Vector3(4650.176f, 2651.462f, 70.07331f), Vector3(0, 0, 15)),
        owning_building_guid = 17,
        door_guid = 362
      )
      LocalObject(
        829,
        IFFLock.Constructor(Vector3(4653.15f, 2598.12f, 47.52831f), Vector3(0, 0, 285)),
        owning_building_guid = 17,
        door_guid = 571
      )
      LocalObject(
        830,
        IFFLock.Constructor(Vector3(4653.393f, 2663.821f, 62.65331f), Vector3(0, 0, 285)),
        owning_building_guid = 17,
        door_guid = 363
      )
      LocalObject(
        831,
        IFFLock.Constructor(Vector3(4653.822f, 2588.117f, 40.02831f), Vector3(0, 0, 15)),
        owning_building_guid = 17,
        door_guid = 572
      )
      LocalObject(
        832,
        IFFLock.Constructor(Vector3(4664.772f, 2665.006f, 62.65331f), Vector3(0, 0, 105)),
        owning_building_guid = 17,
        door_guid = 364
      )
      LocalObject(
        833,
        IFFLock.Constructor(Vector3(4673.483f, 2632.411f, 62.65331f), Vector3(0, 0, 15)),
        owning_building_guid = 17,
        door_guid = 367
      )
      LocalObject(
        834,
        IFFLock.Constructor(Vector3(4678.711f, 2594.526f, 47.52831f), Vector3(0, 0, 105)),
        owning_building_guid = 17,
        door_guid = 583
      )
      LocalObject(
        835,
        IFFLock.Constructor(Vector3(4685.044f, 2655.832f, 40.02831f), Vector3(0, 0, 285)),
        owning_building_guid = 17,
        door_guid = 585
      )
      LocalObject(
        836,
        IFFLock.Constructor(Vector3(4712.022f, 2588.955f, 40.02831f), Vector3(0, 0, 15)),
        owning_building_guid = 17,
        door_guid = 594
      )
      LocalObject(
        837,
        IFFLock.Constructor(Vector3(4712.568f, 2578.844f, 40.02831f), Vector3(0, 0, 195)),
        owning_building_guid = 17,
        door_guid = 592
      )
      LocalObject(
        838,
        IFFLock.Constructor(Vector3(4724.891f, 2592.143f, 57.64231f), Vector3(0, 0, 195)),
        owning_building_guid = 17,
        door_guid = 371
      )
      LocalObject(1067, Locker.Constructor(Vector3(4654.59f, 2592.091f, 46.45331f)), owning_building_guid = 17)
      LocalObject(1068, Locker.Constructor(Vector3(4654.887f, 2593.201f, 46.45331f)), owning_building_guid = 17)
      LocalObject(1069, Locker.Constructor(Vector3(4655.184f, 2594.309f, 46.45331f)), owning_building_guid = 17)
      LocalObject(1070, Locker.Constructor(Vector3(4655.485f, 2595.433f, 46.45331f)), owning_building_guid = 17)
      LocalObject(1071, Locker.Constructor(Vector3(4668.832f, 2567.875f, 38.69231f)), owning_building_guid = 17)
      LocalObject(1072, Locker.Constructor(Vector3(4669.177f, 2569.166f, 38.69231f)), owning_building_guid = 17)
      LocalObject(1073, Locker.Constructor(Vector3(4669.523f, 2570.457f, 38.69231f)), owning_building_guid = 17)
      LocalObject(1074, Locker.Constructor(Vector3(4669.866f, 2571.736f, 38.69231f)), owning_building_guid = 17)
      LocalObject(1075, Locker.Constructor(Vector3(4671.041f, 2576.121f, 38.69231f)), owning_building_guid = 17)
      LocalObject(1076, Locker.Constructor(Vector3(4671.387f, 2577.412f, 38.69231f)), owning_building_guid = 17)
      LocalObject(1077, Locker.Constructor(Vector3(4671.733f, 2578.703f, 38.69231f)), owning_building_guid = 17)
      LocalObject(1078, Locker.Constructor(Vector3(4672.075f, 2579.982f, 38.69231f)), owning_building_guid = 17)
      LocalObject(
        1396,
        Terminal.Constructor(Vector3(4638.184f, 2659.27f, 62.55231f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1397,
        Terminal.Constructor(Vector3(4648.153f, 2644.225f, 69.94731f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1398,
        Terminal.Constructor(Vector3(4650.782f, 2645.856f, 69.94731f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1399,
        Terminal.Constructor(Vector3(4652.398f, 2643.087f, 69.94731f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1400,
        Terminal.Constructor(Vector3(4659.773f, 2591.084f, 47.78231f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1401,
        Terminal.Constructor(Vector3(4663.376f, 2590.118f, 47.78231f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1402,
        Terminal.Constructor(Vector3(4667.037f, 2589.138f, 47.78231f), order_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1947,
        Terminal.Constructor(Vector3(4635.173f, 2651.659f, 62.80931f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1948,
        Terminal.Constructor(Vector3(4659.291f, 2605.379f, 48.32631f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1949,
        Terminal.Constructor(Vector3(4666.335f, 2603.495f, 48.32631f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1950,
        Terminal.Constructor(Vector3(4670.454f, 2657.286f, 40.24931f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1951,
        Terminal.Constructor(Vector3(4673.375f, 2601.606f, 48.32631f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1952,
        Terminal.Constructor(Vector3(4696.734f, 2634.233f, 50.24931f), spawn_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        2108,
        Terminal.Constructor(Vector3(4697.331f, 2579.153f, 58.87931f), vehicle_terminal_combined),
        owning_building_guid = 17
      )
      LocalObject(
        1316,
        VehicleSpawnPad.Constructor(Vector3(4700.708f, 2592.354f, 54.72131f), mb_pad_creation, Vector3(0, 0, 15)),
        owning_building_guid = 17,
        terminal_guid = 2108
      )
      LocalObject(1825, ResourceSilo.Constructor(Vector3(4605.697f, 2715.752f, 63.20931f)), owning_building_guid = 17)
      LocalObject(
        1865,
        SpawnTube.Constructor(Vector3(4660.873f, 2605.719f, 46.19231f), Vector3(0, 0, 105)),
        owning_building_guid = 17
      )
      LocalObject(
        1866,
        SpawnTube.Constructor(Vector3(4667.916f, 2603.832f, 46.19231f), Vector3(0, 0, 105)),
        owning_building_guid = 17
      )
      LocalObject(
        1867,
        SpawnTube.Constructor(Vector3(4674.955f, 2601.946f, 46.19231f), Vector3(0, 0, 105)),
        owning_building_guid = 17
      )
      LocalObject(
        1331,
        ProximityTerminal.Constructor(Vector3(4629.787f, 2650.665f, 56.19231f), medical_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1332,
        ProximityTerminal.Constructor(Vector3(4669.931f, 2574.086f, 38.69231f), medical_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1563,
        ProximityTerminal.Constructor(Vector3(4716.182f, 2647.921f, 64.43331f), pad_landing_frame),
        owning_building_guid = 17
      )
      LocalObject(
        1564,
        Terminal.Constructor(Vector3(4716.182f, 2647.921f, 64.43331f), air_rearm_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1780,
        ProximityTerminal.Constructor(Vector3(4618.545f, 2590.757f, 55.74231f), repair_silo),
        owning_building_guid = 17
      )
      LocalObject(
        1781,
        Terminal.Constructor(Vector3(4618.545f, 2590.757f, 55.74231f), ground_rearm_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1788,
        ProximityTerminal.Constructor(Vector3(4696.201f, 2697.007f, 55.74231f), repair_silo),
        owning_building_guid = 17
      )
      LocalObject(
        1789,
        Terminal.Constructor(Vector3(4696.201f, 2697.007f, 55.74231f), ground_rearm_terminal),
        owning_building_guid = 17
      )
      LocalObject(
        1270,
        FacilityTurret.Constructor(Vector3(4571.18f, 2635.309f, 64.70031f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1270, 5012)
      LocalObject(
        1271,
        FacilityTurret.Constructor(Vector3(4599.056f, 2734.796f, 64.70031f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1271, 5013)
      LocalObject(
        1272,
        FacilityTurret.Constructor(Vector3(4601.658f, 2582.476f, 64.70031f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1272, 5014)
      LocalObject(
        1277,
        FacilityTurret.Constructor(Vector3(4714.378f, 2705.065f, 64.70031f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1277, 5015)
      LocalObject(
        1278,
        FacilityTurret.Constructor(Vector3(4716.842f, 2552.823f, 64.70031f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1278, 5016)
      LocalObject(
        1280,
        FacilityTurret.Constructor(Vector3(4744.846f, 2652.255f, 64.70031f), manned_turret),
        owning_building_guid = 17
      )
      TurretToWeapon(1280, 5017)
      LocalObject(1634, Painbox.Constructor(Vector3(4698f, 2653.844f, 43.59431f), painbox), owning_building_guid = 17)
      LocalObject(
        1644,
        Painbox.Constructor(Vector3(4671.317f, 2592.586f, 50.63711f), painbox_continuous),
        owning_building_guid = 17
      )
      LocalObject(
        1654,
        Painbox.Constructor(Vector3(4683.47f, 2657.636f, 41.45101f), painbox_door_radius),
        owning_building_guid = 17
      )
      LocalObject(
        1674,
        Painbox.Constructor(Vector3(4652.503f, 2600.01f, 48.01831f), painbox_door_radius_continuous),
        owning_building_guid = 17
      )
      LocalObject(
        1675,
        Painbox.Constructor(Vector3(4669.905f, 2580.914f, 49.01831f), painbox_door_radius_continuous),
        owning_building_guid = 17
      )
      LocalObject(
        1676,
        Painbox.Constructor(Vector3(4678.684f, 2592.067f, 47.80001f), painbox_door_radius_continuous),
        owning_building_guid = 17
      )
      LocalObject(225, Generator.Constructor(Vector3(4701.39f, 2653.105f, 37.39831f)), owning_building_guid = 17)
      LocalObject(
        215,
        Terminal.Constructor(Vector3(4693.465f, 2655.18f, 38.69231f), gen_control),
        owning_building_guid = 17
      )
    }

    Building9()

    def Building9(): Unit = { // Name: Dagda Type: comm_station GUID: 20, MapID: 9
      LocalBuilding(
        "Dagda",
        20,
        9,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5912f, 4436f, 54.7319f),
            Vector3(0f, 0f, 219f),
            comm_station
          )
        )
      )
      LocalObject(
        164,
        CaptureTerminal.Constructor(Vector3(5926.027f, 4348.621f, 37.4319f), capture_terminal),
        owning_building_guid = 20
      )
      LocalObject(417, Door.Constructor(Vector3(5858.486f, 4406.424f, 64.4469f)), owning_building_guid = 20)
      LocalObject(418, Door.Constructor(Vector3(5869.935f, 4392.286f, 56.48289f)), owning_building_guid = 20)
      LocalObject(419, Door.Constructor(Vector3(5888.37f, 4504.111f, 56.48289f)), owning_building_guid = 20)
      LocalObject(420, Door.Constructor(Vector3(5902.509f, 4515.561f, 64.4469f)), owning_building_guid = 20)
      LocalObject(427, Door.Constructor(Vector3(5910.737f, 4444.512f, 68.89189f)), owning_building_guid = 20)
      LocalObject(428, Door.Constructor(Vector3(5918.369f, 4415.422f, 61.4529f)), owning_building_guid = 20)
      LocalObject(429, Door.Constructor(Vector3(5922.024f, 4455.401f, 61.4529f)), owning_building_guid = 20)
      LocalObject(430, Door.Constructor(Vector3(5927.729f, 4448.357f, 61.4529f)), owning_building_guid = 20)
      LocalObject(431, Door.Constructor(Vector3(5931.101f, 4507.442f, 56.48289f)), owning_building_guid = 20)
      LocalObject(432, Door.Constructor(Vector3(5934.959f, 4356.799f, 56.4529f)), owning_building_guid = 20)
      LocalObject(433, Door.Constructor(Vector3(5940.253f, 4371.409f, 64.44589f)), owning_building_guid = 20)
      LocalObject(434, Door.Constructor(Vector3(5942.55f, 4493.303f, 64.4469f)), owning_building_guid = 20)
      LocalObject(439, Door.Constructor(Vector3(5954.391f, 4382.857f, 56.48289f)), owning_building_guid = 20)
      LocalObject(636, Door.Constructor(Vector3(5876.032f, 4391.432f, 38.9529f)), owning_building_guid = 20)
      LocalObject(637, Door.Constructor(Vector3(5882.84f, 4402.093f, 46.4529f)), owning_building_guid = 20)
      LocalObject(638, Door.Constructor(Vector3(5886.101f, 4378.998f, 46.4529f)), owning_building_guid = 20)
      LocalObject(639, Door.Constructor(Vector3(5897.944f, 4383.441f, 46.4529f)), owning_building_guid = 20)
      LocalObject(640, Door.Constructor(Vector3(5901.205f, 4360.347f, 38.9529f)), owning_building_guid = 20)
      LocalObject(641, Door.Constructor(Vector3(5901.492f, 4417.196f, 51.4529f)), owning_building_guid = 20)
      LocalObject(642, Door.Constructor(Vector3(5905.344f, 4399.728f, 38.9529f)), owning_building_guid = 20)
      LocalObject(643, Door.Constructor(Vector3(5907.709f, 4422.231f, 56.4529f)), owning_building_guid = 20)
      LocalObject(644, Door.Constructor(Vector3(5913.334f, 4421.64f, 61.4529f)), owning_building_guid = 20)
      LocalObject(645, Door.Constructor(Vector3(5914.517f, 4432.892f, 48.9529f)), owning_building_guid = 20)
      LocalObject(646, Door.Constructor(Vector3(5917.491f, 4352.947f, 38.9529f)), owning_building_guid = 20)
      LocalObject(647, Door.Constructor(Vector3(5918.369f, 4415.422f, 51.4529f)), owning_building_guid = 20)
      LocalObject(648, Door.Constructor(Vector3(5920.143f, 4432.3f, 38.9529f)), owning_building_guid = 20)
      LocalObject(649, Door.Constructor(Vector3(5921.917f, 4449.178f, 61.4529f)), owning_building_guid = 20)
      LocalObject(650, Door.Constructor(Vector3(5923.708f, 4357.981f, 38.9529f)), owning_building_guid = 20)
      LocalObject(651, Door.Constructor(Vector3(5925.482f, 4374.859f, 38.9529f)), owning_building_guid = 20)
      LocalObject(652, Door.Constructor(Vector3(5931.986f, 4436.743f, 56.4529f)), owning_building_guid = 20)
      LocalObject(653, Door.Constructor(Vector3(5934.656f, 4408.023f, 48.9529f)), owning_building_guid = 20)
      LocalObject(654, Door.Constructor(Vector3(5937.612f, 4436.152f, 48.9529f)), owning_building_guid = 20)
      LocalObject(655, Door.Constructor(Vector3(5940.281f, 4407.432f, 38.9529f)), owning_building_guid = 20)
      LocalObject(656, Door.Constructor(Vector3(5942.646f, 4429.935f, 38.9529f)), owning_building_guid = 20)
      LocalObject(657, Door.Constructor(Vector3(5945.316f, 4401.214f, 48.9529f)), owning_building_guid = 20)
      LocalObject(658, Door.Constructor(Vector3(5960.116f, 4433.787f, 48.9529f)), owning_building_guid = 20)
      LocalObject(659, Door.Constructor(Vector3(5963.967f, 4416.318f, 48.9529f)), owning_building_guid = 20)
      LocalObject(691, Door.Constructor(Vector3(5895.679f, 4428.018f, 57.2249f)), owning_building_guid = 20)
      LocalObject(2029, Door.Constructor(Vector3(5892.142f, 4402.249f, 46.7859f)), owning_building_guid = 20)
      LocalObject(2030, Door.Constructor(Vector3(5896.732f, 4396.581f, 46.7859f)), owning_building_guid = 20)
      LocalObject(2031, Door.Constructor(Vector3(5901.319f, 4390.916f, 46.7859f)), owning_building_guid = 20)
      LocalObject(
        729,
        IFFLock.Constructor(Vector3(5895.558f, 4423.805f, 56.3839f), Vector3(0, 0, 231)),
        owning_building_guid = 20,
        door_guid = 691
      )
      LocalObject(
        872,
        IFFLock.Constructor(Vector3(5875.773f, 4393.246f, 38.76789f), Vector3(0, 0, 51)),
        owning_building_guid = 20,
        door_guid = 636
      )
      LocalObject(
        873,
        IFFLock.Constructor(Vector3(5881.109f, 4401.733f, 46.26789f), Vector3(0, 0, 321)),
        owning_building_guid = 20,
        door_guid = 637
      )
      LocalObject(
        874,
        IFFLock.Constructor(Vector3(5899.675f, 4383.801f, 46.26789f), Vector3(0, 0, 141)),
        owning_building_guid = 20,
        door_guid = 639
      )
      LocalObject(
        878,
        IFFLock.Constructor(Vector3(5910.056f, 4446.636f, 68.8129f), Vector3(0, 0, 51)),
        owning_building_guid = 20,
        door_guid = 427
      )
      LocalObject(
        882,
        IFFLock.Constructor(Vector3(5917.714f, 4417.523f, 61.39289f), Vector3(0, 0, 51)),
        owning_building_guid = 20,
        door_guid = 428
      )
      LocalObject(
        883,
        IFFLock.Constructor(Vector3(5917.848f, 4351.213f, 38.76789f), Vector3(0, 0, 231)),
        owning_building_guid = 20,
        door_guid = 646
      )
      LocalObject(
        884,
        IFFLock.Constructor(Vector3(5919.923f, 4454.743f, 61.39289f), Vector3(0, 0, 321)),
        owning_building_guid = 20,
        door_guid = 429
      )
      LocalObject(
        885,
        IFFLock.Constructor(Vector3(5923.351f, 4359.714f, 38.76789f), Vector3(0, 0, 51)),
        owning_building_guid = 20,
        door_guid = 650
      )
      LocalObject(
        886,
        IFFLock.Constructor(Vector3(5929.826f, 4449.013f, 61.39289f), Vector3(0, 0, 141)),
        owning_building_guid = 20,
        door_guid = 430
      )
      LocalObject(
        887,
        IFFLock.Constructor(Vector3(5935.634f, 4354.729f, 56.3819f), Vector3(0, 0, 231)),
        owning_building_guid = 20,
        door_guid = 432
      )
      LocalObject(
        888,
        IFFLock.Constructor(Vector3(5940.833f, 4429.676f, 38.76789f), Vector3(0, 0, 321)),
        owning_building_guid = 20,
        door_guid = 656
      )
      LocalObject(1127, Locker.Constructor(Vector3(5876.018f, 4368.047f, 37.4319f)), owning_building_guid = 20)
      LocalObject(1128, Locker.Constructor(Vector3(5877.057f, 4368.888f, 37.4319f)), owning_building_guid = 20)
      LocalObject(1129, Locker.Constructor(Vector3(5878.095f, 4369.729f, 37.4319f)), owning_building_guid = 20)
      LocalObject(1130, Locker.Constructor(Vector3(5878.729f, 4396.009f, 45.19289f)), owning_building_guid = 20)
      LocalObject(1131, Locker.Constructor(Vector3(5879.124f, 4370.562f, 37.4319f)), owning_building_guid = 20)
      LocalObject(1132, Locker.Constructor(Vector3(5879.623f, 4396.732f, 45.19289f)), owning_building_guid = 20)
      LocalObject(1133, Locker.Constructor(Vector3(5880.514f, 4397.454f, 45.19289f)), owning_building_guid = 20)
      LocalObject(1134, Locker.Constructor(Vector3(5881.418f, 4398.187f, 45.19289f)), owning_building_guid = 20)
      LocalObject(1135, Locker.Constructor(Vector3(5882.652f, 4373.419f, 37.4319f)), owning_building_guid = 20)
      LocalObject(1136, Locker.Constructor(Vector3(5883.691f, 4374.261f, 37.4319f)), owning_building_guid = 20)
      LocalObject(1137, Locker.Constructor(Vector3(5884.729f, 4375.102f, 37.4319f)), owning_building_guid = 20)
      LocalObject(1138, Locker.Constructor(Vector3(5885.758f, 4375.935f, 37.4319f)), owning_building_guid = 20)
      LocalObject(
        1422,
        Terminal.Constructor(Vector3(5882.331f, 4392.148f, 46.5219f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1423,
        Terminal.Constructor(Vector3(5884.679f, 4389.249f, 46.5219f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1424,
        Terminal.Constructor(Vector3(5887.063f, 4386.304f, 46.5219f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1425,
        Terminal.Constructor(Vector3(5904.166f, 4441.97f, 68.6869f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1426,
        Terminal.Constructor(Vector3(5904.944f, 4460.001f, 61.2919f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1427,
        Terminal.Constructor(Vector3(5906.931f, 4438.554f, 68.6869f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1428,
        Terminal.Constructor(Vector3(5907.251f, 4441.745f, 68.6869f), order_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1966,
        Terminal.Constructor(Vector3(5890.344f, 4403.996f, 47.0659f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1967,
        Terminal.Constructor(Vector3(5894.936f, 4398.332f, 47.0659f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1968,
        Terminal.Constructor(Vector3(5898.035f, 4455.613f, 61.5489f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1969,
        Terminal.Constructor(Vector3(5899.52f, 4392.666f, 47.0659f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1970,
        Terminal.Constructor(Vector3(5929.885f, 4439.429f, 38.9889f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1971,
        Terminal.Constructor(Vector3(5937.596f, 4405.331f, 48.9889f), spawn_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        2111,
        Terminal.Constructor(Vector3(5905.703f, 4360.42f, 57.6189f), vehicle_terminal_combined),
        owning_building_guid = 20
      )
      LocalObject(
        1321,
        VehicleSpawnPad.Constructor(Vector3(5916.194f, 4369.114f, 53.4609f), mb_pad_creation, Vector3(0, 0, 51)),
        owning_building_guid = 20,
        terminal_guid = 2111
      )
      LocalObject(1828, ResourceSilo.Constructor(Vector3(5911.86f, 4524.792f, 61.94889f)), owning_building_guid = 20)
      LocalObject(
        1880,
        SpawnTube.Constructor(Vector3(5891.823f, 4403.341f, 44.9319f), Vector3(0, 0, 141)),
        owning_building_guid = 20
      )
      LocalObject(
        1881,
        SpawnTube.Constructor(Vector3(5896.412f, 4397.675f, 44.9319f), Vector3(0, 0, 141)),
        owning_building_guid = 20
      )
      LocalObject(
        1882,
        SpawnTube.Constructor(Vector3(5900.998f, 4392.011f, 44.9319f), Vector3(0, 0, 141)),
        owning_building_guid = 20
      )
      LocalObject(
        1337,
        ProximityTerminal.Constructor(Vector3(5880.558f, 4372.425f, 37.4319f), medical_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1338,
        ProximityTerminal.Constructor(Vector3(5893.093f, 4457.976f, 54.9319f), medical_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1590,
        ProximityTerminal.Constructor(Vector3(5961.375f, 4404.973f, 63.1729f), pad_landing_frame),
        owning_building_guid = 20
      )
      LocalObject(
        1591,
        Terminal.Constructor(Vector3(5961.375f, 4404.973f, 63.1729f), air_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1804,
        ProximityTerminal.Constructor(Vector3(5848.785f, 4416.116f, 54.4819f), repair_silo),
        owning_building_guid = 20
      )
      LocalObject(
        1805,
        Terminal.Constructor(Vector3(5848.785f, 4416.116f, 54.4819f), ground_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1808,
        ProximityTerminal.Constructor(Vector3(5974.062f, 4456.43f, 54.4819f), repair_silo),
        owning_building_guid = 20
      )
      LocalObject(
        1809,
        Terminal.Constructor(Vector3(5974.062f, 4456.43f, 54.4819f), ground_rearm_terminal),
        owning_building_guid = 20
      )
      LocalObject(
        1292,
        FacilityTurret.Constructor(Vector3(5830.255f, 4419.343f, 63.4399f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1292, 5018)
      LocalObject(
        1293,
        FacilityTurret.Constructor(Vector3(5836.653f, 4480f, 63.4399f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1293, 5019)
      LocalObject(
        1294,
        FacilityTurret.Constructor(Vector3(5906.011f, 4327.65f, 63.4399f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1294, 5020)
      LocalObject(
        1295,
        FacilityTurret.Constructor(Vector3(5917.682f, 4544.102f, 63.4399f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1295, 5021)
      LocalObject(
        1298,
        FacilityTurret.Constructor(Vector3(5987.112f, 4391.632f, 63.4399f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1298, 5022)
      LocalObject(
        1299,
        FacilityTurret.Constructor(Vector3(5993.504f, 4452.265f, 63.4399f), manned_turret),
        owning_building_guid = 20
      )
      TurretToWeapon(1299, 5023)
      LocalObject(
        1637,
        Painbox.Constructor(Vector3(5950.147f, 4420.453f, 42.3339f), painbox),
        owning_building_guid = 20
      )
      LocalObject(
        1647,
        Painbox.Constructor(Vector3(5892.553f, 4386.578f, 49.37669f), painbox_continuous),
        owning_building_guid = 20
      )
      LocalObject(
        1657,
        Painbox.Constructor(Vector3(5940.621f, 4432.061f, 40.1906f), painbox_door_radius),
        owning_building_guid = 20
      )
      LocalObject(
        1683,
        Painbox.Constructor(Vector3(5881.696f, 4403.642f, 46.7579f), painbox_door_radius_continuous),
        owning_building_guid = 20
      )
      LocalObject(
        1684,
        Painbox.Constructor(Vector3(5884.55f, 4377.965f, 47.7579f), painbox_door_radius_continuous),
        owning_building_guid = 20
      )
      LocalObject(
        1685,
        Painbox.Constructor(Vector3(5898.208f, 4381.828f, 46.5396f), painbox_door_radius_continuous),
        owning_building_guid = 20
      )
      LocalObject(228, Generator.Constructor(Vector3(5952.455f, 4417.862f, 36.13789f)), owning_building_guid = 20)
      LocalObject(
        218,
        Terminal.Constructor(Vector3(5947.263f, 4424.199f, 37.4319f), gen_control),
        owning_building_guid = 20
      )
    }

    Building12()

    def Building12(): Unit = { // Name: Bel Type: comm_station_dsp GUID: 23, MapID: 12
      LocalBuilding(
        "Bel",
        23,
        12,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3612f, 4694f, 57.91497f),
            Vector3(0f, 0f, 360f),
            comm_station_dsp
          )
        )
      )
      LocalObject(
        159,
        CaptureTerminal.Constructor(Vector3(3688.089f, 4674.734f, 40.51498f), capture_terminal),
        owning_building_guid = 23
      )
      LocalObject(205, Door.Constructor(Vector3(3680.339f, 4764.464f, 61.29297f)), owning_building_guid = 23)
      LocalObject(280, Door.Constructor(Vector3(3552.196f, 4650.501f, 59.56598f)), owning_building_guid = 23)
      LocalObject(281, Door.Constructor(Vector3(3552.196f, 4668.693f, 67.52998f)), owning_building_guid = 23)
      LocalObject(284, Door.Constructor(Vector3(3569.307f, 4626.197f, 67.52998f)), owning_building_guid = 23)
      LocalObject(296, Door.Constructor(Vector3(3587.499f, 4626.197f, 59.56598f)), owning_building_guid = 23)
      LocalObject(297, Door.Constructor(Vector3(3592f, 4685.231f, 64.53597f)), owning_building_guid = 23)
      LocalObject(298, Door.Constructor(Vector3(3592f, 4694.295f, 64.53597f)), owning_building_guid = 23)
      LocalObject(299, Door.Constructor(Vector3(3604.763f, 4813.958f, 59.56598f)), owning_building_guid = 23)
      LocalObject(300, Door.Constructor(Vector3(3607.625f, 4686.59f, 71.97498f)), owning_building_guid = 23)
      LocalObject(303, Door.Constructor(Vector3(3617.627f, 4826.823f, 67.52898f)), owning_building_guid = 23)
      LocalObject(304, Door.Constructor(Vector3(3620f, 4714f, 64.53597f)), owning_building_guid = 23)
      LocalObject(310, Door.Constructor(Vector3(3659.721f, 4858.353f, 59.56598f)), owning_building_guid = 23)
      LocalObject(311, Door.Constructor(Vector3(3665.952f, 4798.355f, 64.53197f)), owning_building_guid = 23)
      LocalObject(312, Door.Constructor(Vector3(3667.927f, 4768.35f, 59.53798f)), owning_building_guid = 23)
      LocalObject(317, Door.Constructor(Vector3(3677.914f, 4858.353f, 67.52898f)), owning_building_guid = 23)
      LocalObject(318, Door.Constructor(Vector3(3691.929f, 4681.406f, 67.52998f)), owning_building_guid = 23)
      LocalObject(321, Door.Constructor(Vector3(3704.793f, 4694.27f, 59.56598f)), owning_building_guid = 23)
      LocalObject(325, Door.Constructor(Vector3(3718.977f, 4757.008f, 67.52898f)), owning_building_guid = 23)
      LocalObject(326, Door.Constructor(Vector3(3718.977f, 4775.2f, 59.56598f)), owning_building_guid = 23)
      LocalObject(327, Door.Constructor(Vector3(3728f, 4774f, 59.53597f)), owning_building_guid = 23)
      LocalObject(524, Door.Constructor(Vector3(3596f, 4690f, 64.53597f)), owning_building_guid = 23)
      LocalObject(525, Door.Constructor(Vector3(3596f, 4706f, 59.53597f)), owning_building_guid = 23)
      LocalObject(529, Door.Constructor(Vector3(3620f, 4706f, 64.53597f)), owning_building_guid = 23)
      LocalObject(530, Door.Constructor(Vector3(3620f, 4714f, 54.53597f)), owning_building_guid = 23)
      LocalObject(531, Door.Constructor(Vector3(3624f, 4702f, 59.53597f)), owning_building_guid = 23)
      LocalObject(532, Door.Constructor(Vector3(3632f, 4702f, 54.53597f)), owning_building_guid = 23)
      LocalObject(533, Door.Constructor(Vector3(3636f, 4738f, 49.53597f)), owning_building_guid = 23)
      LocalObject(534, Door.Constructor(Vector3(3640f, 4726f, 42.03597f)), owning_building_guid = 23)
      LocalObject(535, Door.Constructor(Vector3(3652f, 4690f, 42.03597f)), owning_building_guid = 23)
      LocalObject(536, Door.Constructor(Vector3(3652f, 4754f, 42.03597f)), owning_building_guid = 23)
      LocalObject(537, Door.Constructor(Vector3(3656f, 4686f, 49.53597f)), owning_building_guid = 23)
      LocalObject(538, Door.Constructor(Vector3(3656f, 4702f, 49.53597f)), owning_building_guid = 23)
      LocalObject(539, Door.Constructor(Vector3(3656f, 4726f, 49.53597f)), owning_building_guid = 23)
      LocalObject(540, Door.Constructor(Vector3(3668f, 4706f, 42.03597f)), owning_building_guid = 23)
      LocalObject(541, Door.Constructor(Vector3(3668f, 4722f, 49.53597f)), owning_building_guid = 23)
      LocalObject(542, Door.Constructor(Vector3(3671.921f, 4788.351f, 64.53797f)), owning_building_guid = 23)
      LocalObject(543, Door.Constructor(Vector3(3684f, 4666f, 42.03597f)), owning_building_guid = 23)
      LocalObject(544, Door.Constructor(Vector3(3692f, 4666f, 42.03597f)), owning_building_guid = 23)
      LocalObject(545, Door.Constructor(Vector3(3696f, 4678f, 42.03597f)), owning_building_guid = 23)
      LocalObject(546, Door.Constructor(Vector3(3700f, 4698f, 49.53597f)), owning_building_guid = 23)
      LocalObject(547, Door.Constructor(Vector3(3700f, 4730f, 49.53597f)), owning_building_guid = 23)
      LocalObject(686, Door.Constructor(Vector3(3629.707f, 4689.922f, 60.30697f)), owning_building_guid = 23)
      LocalObject(1998, Door.Constructor(Vector3(3648.673f, 4707.733f, 49.86897f)), owning_building_guid = 23)
      LocalObject(1999, Door.Constructor(Vector3(3648.673f, 4715.026f, 49.86897f)), owning_building_guid = 23)
      LocalObject(2000, Door.Constructor(Vector3(3648.673f, 4722.315f, 49.86897f)), owning_building_guid = 23)
      LocalObject(
        724,
        IFFLock.Constructor(Vector3(3632.454f, 4693.09f, 59.48298f), Vector3(0, 0, 90)),
        owning_building_guid = 23,
        door_guid = 686
      )
      LocalObject(
        778,
        IFFLock.Constructor(Vector3(3589.959f, 4695.104f, 64.48297f), Vector3(0, 0, 0)),
        owning_building_guid = 23,
        door_guid = 298
      )
      LocalObject(
        779,
        IFFLock.Constructor(Vector3(3594.04f, 4684.42f, 64.48297f), Vector3(0, 0, 180)),
        owning_building_guid = 23,
        door_guid = 297
      )
      LocalObject(
        780,
        IFFLock.Constructor(Vector3(3606.817f, 4684.514f, 71.98297f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 300
      )
      LocalObject(
        783,
        IFFLock.Constructor(Vector3(3619.193f, 4711.962f, 64.48297f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 304
      )
      LocalObject(
        788,
        IFFLock.Constructor(Vector3(3652.94f, 4755.572f, 41.85098f), Vector3(0, 0, 90)),
        owning_building_guid = 23,
        door_guid = 536
      )
      LocalObject(
        789,
        IFFLock.Constructor(Vector3(3654.428f, 4726.94f, 49.35098f), Vector3(0, 0, 0)),
        owning_building_guid = 23,
        door_guid = 539
      )
      LocalObject(
        790,
        IFFLock.Constructor(Vector3(3657.572f, 4701.19f, 49.35098f), Vector3(0, 0, 180)),
        owning_building_guid = 23,
        door_guid = 538
      )
      LocalObject(
        791,
        IFFLock.Constructor(Vector3(3663.907f, 4799.163f, 64.46198f), Vector3(0, 0, 0)),
        owning_building_guid = 23,
        door_guid = 311
      )
      LocalObject(
        792,
        IFFLock.Constructor(Vector3(3667.06f, 4704.428f, 41.85098f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 540
      )
      LocalObject(
        793,
        IFFLock.Constructor(Vector3(3667.124f, 4766.312f, 59.52697f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 312
      )
      LocalObject(
        798,
        IFFLock.Constructor(Vector3(3683.06f, 4664.428f, 41.85098f), Vector3(0, 0, 270)),
        owning_building_guid = 23,
        door_guid = 543
      )
      LocalObject(
        800,
        IFFLock.Constructor(Vector3(3692.813f, 4667.572f, 41.85098f), Vector3(0, 0, 90)),
        owning_building_guid = 23,
        door_guid = 544
      )
      LocalObject(
        805,
        IFFLock.Constructor(Vector3(3725.953f, 4774.808f, 59.42598f), Vector3(0, 0, 0)),
        owning_building_guid = 23,
        door_guid = 327
      )
      LocalObject(994, Locker.Constructor(Vector3(3659.563f, 4704.141f, 48.27597f)), owning_building_guid = 23)
      LocalObject(995, Locker.Constructor(Vector3(3660.727f, 4704.141f, 48.27597f)), owning_building_guid = 23)
      LocalObject(996, Locker.Constructor(Vector3(3661.874f, 4704.141f, 48.27597f)), owning_building_guid = 23)
      LocalObject(997, Locker.Constructor(Vector3(3663.023f, 4704.141f, 48.27597f)), owning_building_guid = 23)
      LocalObject(998, Locker.Constructor(Vector3(3670.194f, 4724.165f, 40.51498f)), owning_building_guid = 23)
      LocalObject(999, Locker.Constructor(Vector3(3671.518f, 4724.165f, 40.51498f)), owning_building_guid = 23)
      LocalObject(1000, Locker.Constructor(Vector3(3672.854f, 4724.165f, 40.51498f)), owning_building_guid = 23)
      LocalObject(1001, Locker.Constructor(Vector3(3674.191f, 4724.165f, 40.51498f)), owning_building_guid = 23)
      LocalObject(1004, Locker.Constructor(Vector3(3678.731f, 4724.165f, 40.51498f)), owning_building_guid = 23)
      LocalObject(1007, Locker.Constructor(Vector3(3680.055f, 4724.165f, 40.51498f)), owning_building_guid = 23)
      LocalObject(1008, Locker.Constructor(Vector3(3681.391f, 4724.165f, 40.51498f)), owning_building_guid = 23)
      LocalObject(1011, Locker.Constructor(Vector3(3682.728f, 4724.165f, 40.51498f)), owning_building_guid = 23)
      LocalObject(
        207,
        Terminal.Constructor(Vector3(3671.879f, 4796.918f, 63.61897f), dropship_vehicle_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        206,
        VehicleSpawnPad.Constructor(Vector3(3680.328f, 4818.856f, 57.94297f), dropship_pad_doors, Vector3(0, 0, 90)),
        owning_building_guid = 23,
        terminal_guid = 207
      )
      LocalObject(
        1366,
        Terminal.Constructor(Vector3(3602.378f, 4670.897f, 64.37498f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1367,
        Terminal.Constructor(Vector3(3612.075f, 4686.547f, 71.76997f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1368,
        Terminal.Constructor(Vector3(3614.331f, 4684.43f, 71.76997f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1369,
        Terminal.Constructor(Vector3(3614.332f, 4688.825f, 71.76997f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1370,
        Terminal.Constructor(Vector3(3616.592f, 4686.59f, 71.76997f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1374,
        Terminal.Constructor(Vector3(3662.654f, 4709.408f, 49.60497f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1375,
        Terminal.Constructor(Vector3(3662.654f, 4713.139f, 49.60497f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1376,
        Terminal.Constructor(Vector3(3662.654f, 4716.928f, 49.60497f), order_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1931,
        Terminal.Constructor(Vector3(3610.509f, 4669.959f, 64.63197f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1932,
        Terminal.Constructor(Vector3(3648.971f, 4705.243f, 50.14898f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1933,
        Terminal.Constructor(Vector3(3648.967f, 4712.535f, 50.14898f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1934,
        Terminal.Constructor(Vector3(3648.97f, 4719.823f, 50.14898f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1935,
        Terminal.Constructor(Vector3(3667.103f, 4788.906f, 64.56297f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1936,
        Terminal.Constructor(Vector3(3676.058f, 4693.409f, 42.04298f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1937,
        Terminal.Constructor(Vector3(3683.409f, 4749.942f, 42.04298f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1938,
        Terminal.Constructor(Vector3(3692.058f, 4701.409f, 49.57198f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1939,
        Terminal.Constructor(Vector3(3692.058f, 4741.409f, 49.57198f), spawn_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        2106,
        Terminal.Constructor(Vector3(3629.698f, 4802.044f, 60.70197f), ground_vehicle_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1314,
        VehicleSpawnPad.Constructor(Vector3(3629.786f, 4788.411f, 56.54398f), mb_pad_creation, Vector3(0, 0, 180)),
        owning_building_guid = 23,
        terminal_guid = 2106
      )
      LocalObject(1823, ResourceSilo.Constructor(Vector3(3710.212f, 4859.642f, 65.03197f)), owning_building_guid = 23)
      LocalObject(
        1849,
        SpawnTube.Constructor(Vector3(3648.233f, 4706.683f, 48.01498f), Vector3(0, 0, 0)),
        owning_building_guid = 23
      )
      LocalObject(
        1850,
        SpawnTube.Constructor(Vector3(3648.233f, 4713.974f, 48.01498f), Vector3(0, 0, 0)),
        owning_building_guid = 23
      )
      LocalObject(
        1851,
        SpawnTube.Constructor(Vector3(3648.233f, 4721.262f, 48.01498f), Vector3(0, 0, 0)),
        owning_building_guid = 23
      )
      LocalObject(
        1328,
        ProximityTerminal.Constructor(Vector3(3612.863f, 4665.013f, 58.01497f), medical_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1329,
        ProximityTerminal.Constructor(Vector3(3676.444f, 4723.62f, 40.51498f), medical_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1530,
        ProximityTerminal.Constructor(Vector3(3593.153f, 4787.398f, 66.32497f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        1531,
        Terminal.Constructor(Vector3(3593.153f, 4787.398f, 66.32497f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1536,
        ProximityTerminal.Constructor(Vector3(3609.514f, 4741.467f, 63.60897f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        1537,
        Terminal.Constructor(Vector3(3609.514f, 4741.467f, 63.60897f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1539,
        ProximityTerminal.Constructor(Vector3(3661.804f, 4705.901f, 70.79098f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        1540,
        Terminal.Constructor(Vector3(3661.804f, 4705.901f, 70.79098f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1542,
        ProximityTerminal.Constructor(Vector3(3697.071f, 4722.159f, 66.33797f), pad_landing_frame),
        owning_building_guid = 23
      )
      LocalObject(
        1543,
        Terminal.Constructor(Vector3(3697.071f, 4722.159f, 66.33797f), air_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1764,
        ProximityTerminal.Constructor(Vector3(3550.642f, 4732.241f, 57.66497f), repair_silo),
        owning_building_guid = 23
      )
      LocalObject(
        1765,
        Terminal.Constructor(Vector3(3550.642f, 4732.241f, 57.66497f), ground_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1768,
        ProximityTerminal.Constructor(Vector3(3720.57f, 4735.151f, 57.66497f), repair_silo),
        owning_building_guid = 23
      )
      LocalObject(
        1769,
        Terminal.Constructor(Vector3(3720.57f, 4735.151f, 57.66497f), ground_rearm_terminal),
        owning_building_guid = 23
      )
      LocalObject(
        1242,
        FacilityTurret.Constructor(Vector3(3538.401f, 4767.113f, 66.52297f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1242, 5024)
      LocalObject(
        1243,
        FacilityTurret.Constructor(Vector3(3539.554f, 4613.565f, 66.52297f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1243, 5025)
      LocalObject(
        1247,
        FacilityTurret.Constructor(Vector3(3583.445f, 4813.667f, 66.52297f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1247, 5026)
      LocalObject(
        1252,
        FacilityTurret.Constructor(Vector3(3642.428f, 4612.396f, 66.52297f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1252, 5027)
      LocalObject(
        1253,
        FacilityTurret.Constructor(Vector3(3643.449f, 4872.154f, 66.52297f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1253, 5028)
      LocalObject(
        1256,
        FacilityTurret.Constructor(Vector3(3684.537f, 4653.011f, 66.52297f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1256, 5029)
      LocalObject(
        1259,
        FacilityTurret.Constructor(Vector3(3731.619f, 4870.985f, 66.52297f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1259, 5030)
      LocalObject(
        1260,
        FacilityTurret.Constructor(Vector3(3732.773f, 4702.733f, 66.52297f), manned_turret),
        owning_building_guid = 23
      )
      TurretToWeapon(1260, 5031)
      LocalObject(
        1632,
        Painbox.Constructor(Vector3(3640.428f, 4754.057f, 44.40928f), painbox),
        owning_building_guid = 23
      )
      LocalObject(
        1642,
        Painbox.Constructor(Vector3(3657.857f, 4714.408f, 52.04247f), painbox_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        1652,
        Painbox.Constructor(Vector3(3654.203f, 4752.915f, 43.64717f), painbox_door_radius),
        owning_building_guid = 23
      )
      LocalObject(
        1668,
        Painbox.Constructor(Vector3(3655.087f, 4699.386f, 50.44418f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        1669,
        Painbox.Constructor(Vector3(3655.895f, 4728.081f, 50.91497f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(
        1670,
        Painbox.Constructor(Vector3(3670.317f, 4721.888f, 51.34528f), painbox_door_radius_continuous),
        owning_building_guid = 23
      )
      LocalObject(223, Generator.Constructor(Vector3(3636.445f, 4753.975f, 39.22097f)), owning_building_guid = 23)
      LocalObject(
        213,
        Terminal.Constructor(Vector3(3644.637f, 4754.022f, 40.51498f), gen_control),
        owning_building_guid = 23
      )
    }

    Building5()

    def Building5(): Unit = { // Name: Ogma Type: cryo_facility GUID: 26, MapID: 5
      LocalBuilding(
        "Ogma",
        26,
        5,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(3538f, 3288f, 113.9927f),
            Vector3(0f, 0f, 260f),
            cryo_facility
          )
        )
      )
      LocalObject(
        158,
        CaptureTerminal.Constructor(Vector3(3601.243f, 3305.371f, 103.9927f), capture_terminal),
        owning_building_guid = 26
      )
      LocalObject(274, Door.Constructor(Vector3(3441.841f, 3272.462f, 115.5137f)), owning_building_guid = 26)
      LocalObject(275, Door.Constructor(Vector3(3452.252f, 3279.257f, 115.5437f)), owning_building_guid = 26)
      LocalObject(276, Door.Constructor(Vector3(3455.411f, 3297.174f, 123.5077f)), owning_building_guid = 26)
      LocalObject(277, Door.Constructor(Vector3(3479.234f, 3246.649f, 123.5077f)), owning_building_guid = 26)
      LocalObject(278, Door.Constructor(Vector3(3497.15f, 3243.49f, 115.5437f)), owning_building_guid = 26)
      LocalObject(279, Door.Constructor(Vector3(3540.395f, 3283.516f, 125.5137f)), owning_building_guid = 26)
      LocalObject(282, Door.Constructor(Vector3(3552.673f, 3345.3f, 115.5437f)), owning_building_guid = 26)
      LocalObject(283, Door.Constructor(Vector3(3560.475f, 3300.284f, 125.5137f)), owning_building_guid = 26)
      LocalObject(285, Door.Constructor(Vector3(3570.59f, 3342.14f, 123.5077f)), owning_building_guid = 26)
      LocalObject(301, Door.Constructor(Vector3(3608.964f, 3299.992f, 123.5077f)), owning_building_guid = 26)
      LocalObject(302, Door.Constructor(Vector3(3612.123f, 3317.909f, 115.5437f)), owning_building_guid = 26)
      LocalObject(504, Door.Constructor(Vector3(3453.421f, 3315.099f, 108.0137f)), owning_building_guid = 26)
      LocalObject(505, Door.Constructor(Vector3(3485.857f, 3337.811f, 108.0137f)), owning_building_guid = 26)
      LocalObject(506, Door.Constructor(Vector3(3486.324f, 3317.42f, 108.0137f)), owning_building_guid = 26)
      LocalObject(507, Door.Constructor(Vector3(3509.959f, 3313.253f, 105.5137f)), owning_building_guid = 26)
      LocalObject(508, Door.Constructor(Vector3(3510.425f, 3292.862f, 105.5137f)), owning_building_guid = 26)
      LocalObject(509, Door.Constructor(Vector3(3529.427f, 3285.45f, 125.5137f)), owning_building_guid = 26)
      LocalObject(510, Door.Constructor(Vector3(3534.061f, 3288.695f, 105.5137f)), owning_building_guid = 26)
      LocalObject(511, Door.Constructor(Vector3(3534.517f, 3337.354f, 105.5137f)), owning_building_guid = 26)
      LocalObject(512, Door.Constructor(Vector3(3535.916f, 3276.182f, 105.5137f)), owning_building_guid = 26)
      LocalObject(513, Door.Constructor(Vector3(3537.772f, 3263.67f, 98.01373f)), owning_building_guid = 26)
      LocalObject(514, Door.Constructor(Vector3(3540.084f, 3299.818f, 105.5137f)), owning_building_guid = 26)
      LocalObject(515, Door.Constructor(Vector3(3544.251f, 3323.453f, 105.5137f)), owning_building_guid = 26)
      LocalObject(516, Door.Constructor(Vector3(3553.529f, 3260.892f, 105.5137f)), owning_building_guid = 26)
      LocalObject(517, Door.Constructor(Vector3(3559.552f, 3272.015f, 105.5137f)), owning_building_guid = 26)
      LocalObject(518, Door.Constructor(Vector3(3560.475f, 3300.284f, 105.5137f)), owning_building_guid = 26)
      LocalObject(519, Door.Constructor(Vector3(3560.475f, 3300.284f, 115.5137f)), owning_building_guid = 26)
      LocalObject(520, Door.Constructor(Vector3(3563.253f, 3316.041f, 105.5137f)), owning_building_guid = 26)
      LocalObject(521, Door.Constructor(Vector3(3579.476f, 3292.872f, 105.5137f)), owning_building_guid = 26)
      LocalObject(522, Door.Constructor(Vector3(3583.644f, 3316.507f, 105.5137f)), owning_building_guid = 26)
      LocalObject(523, Door.Constructor(Vector3(3589.21f, 3278.97f, 105.5137f)), owning_building_guid = 26)
      LocalObject(526, Door.Constructor(Vector3(3607.745f, 3291.949f, 105.5137f)), owning_building_guid = 26)
      LocalObject(527, Door.Constructor(Vector3(3609.135f, 3299.827f, 105.5137f)), owning_building_guid = 26)
      LocalObject(528, Door.Constructor(Vector3(3610.524f, 3307.706f, 105.5137f)), owning_building_guid = 26)
      LocalObject(685, Door.Constructor(Vector3(3562.009f, 3279.713f, 116.2757f)), owning_building_guid = 26)
      LocalObject(693, Door.Constructor(Vector3(3535.916f, 3276.182f, 115.5137f)), owning_building_guid = 26)
      LocalObject(694, Door.Constructor(Vector3(3546.573f, 3290.55f, 115.5117f)), owning_building_guid = 26)
      LocalObject(1989, Door.Constructor(Vector3(3542.834f, 3282.403f, 105.8467f)), owning_building_guid = 26)
      LocalObject(1990, Door.Constructor(Vector3(3550.017f, 3281.136f, 105.8467f)), owning_building_guid = 26)
      LocalObject(1991, Door.Constructor(Vector3(3557.195f, 3279.87f, 105.8467f)), owning_building_guid = 26)
      LocalObject(
        723,
        IFFLock.Constructor(Vector3(3565.266f, 3282.41f, 115.4747f), Vector3(0, 0, 100)),
        owning_building_guid = 26,
        door_guid = 685
      )
      LocalObject(
        762,
        IFFLock.Constructor(Vector3(3443.71f, 3271.306f, 115.4447f), Vector3(0, 0, 190)),
        owning_building_guid = 26,
        door_guid = 274
      )
      LocalObject(
        763,
        IFFLock.Constructor(Vector3(3534.845f, 3274.775f, 105.3287f), Vector3(0, 0, 280)),
        owning_building_guid = 26,
        door_guid = 512
      )
      LocalObject(
        764,
        IFFLock.Constructor(Vector3(3536.364f, 3264.741f, 97.82874f), Vector3(0, 0, 10)),
        owning_building_guid = 26,
        door_guid = 513
      )
      LocalObject(
        765,
        IFFLock.Constructor(Vector3(3541.559f, 3285.388f, 125.4447f), Vector3(0, 0, 100)),
        owning_building_guid = 26,
        door_guid = 279
      )
      LocalObject(
        766,
        IFFLock.Constructor(Vector3(3560.622f, 3273.422f, 105.3287f), Vector3(0, 0, 100)),
        owning_building_guid = 26,
        door_guid = 517
      )
      LocalObject(
        767,
        IFFLock.Constructor(Vector3(3562.345f, 3299.127f, 125.4447f), Vector3(0, 0, 190)),
        owning_building_guid = 26,
        door_guid = 283
      )
      LocalObject(
        781,
        IFFLock.Constructor(Vector3(3609.139f, 3308.905f, 105.3287f), Vector3(0, 0, 10)),
        owning_building_guid = 26,
        door_guid = 528
      )
      LocalObject(
        782,
        IFFLock.Constructor(Vector3(3610.542f, 3298.754f, 105.3287f), Vector3(0, 0, 190)),
        owning_building_guid = 26,
        door_guid = 527
      )
      LocalObject(949, Locker.Constructor(Vector3(3533.708f, 3250.071f, 103.9007f)), owning_building_guid = 26)
      LocalObject(950, Locker.Constructor(Vector3(3533.892f, 3251.11f, 103.9007f)), owning_building_guid = 26)
      LocalObject(951, Locker.Constructor(Vector3(3534.076f, 3252.154f, 103.9007f)), owning_building_guid = 26)
      LocalObject(952, Locker.Constructor(Vector3(3534.259f, 3253.193f, 103.9007f)), owning_building_guid = 26)
      LocalObject(953, Locker.Constructor(Vector3(3534.442f, 3254.232f, 103.9007f)), owning_building_guid = 26)
      LocalObject(954, Locker.Constructor(Vector3(3534.625f, 3255.272f, 103.9007f)), owning_building_guid = 26)
      LocalObject(955, Locker.Constructor(Vector3(3536.805f, 3268.894f, 104.2537f)), owning_building_guid = 26)
      LocalObject(956, Locker.Constructor(Vector3(3537.005f, 3270.026f, 104.2537f)), owning_building_guid = 26)
      LocalObject(957, Locker.Constructor(Vector3(3537.204f, 3271.155f, 104.2537f)), owning_building_guid = 26)
      LocalObject(958, Locker.Constructor(Vector3(3537.406f, 3272.302f, 104.2537f)), owning_building_guid = 26)
      LocalObject(959, Locker.Constructor(Vector3(3553.406f, 3246.601f, 103.9007f)), owning_building_guid = 26)
      LocalObject(960, Locker.Constructor(Vector3(3553.59f, 3247.641f, 103.9007f)), owning_building_guid = 26)
      LocalObject(961, Locker.Constructor(Vector3(3553.773f, 3248.68f, 103.9007f)), owning_building_guid = 26)
      LocalObject(962, Locker.Constructor(Vector3(3553.956f, 3249.719f, 103.9007f)), owning_building_guid = 26)
      LocalObject(963, Locker.Constructor(Vector3(3554.14f, 3250.764f, 103.9007f)), owning_building_guid = 26)
      LocalObject(964, Locker.Constructor(Vector3(3554.323f, 3251.802f, 103.9007f)), owning_building_guid = 26)
      LocalObject(969, Locker.Constructor(Vector3(3583.271f, 3259.712f, 103.9877f)), owning_building_guid = 26)
      LocalObject(972, Locker.Constructor(Vector3(3584.509f, 3259.494f, 103.9877f)), owning_building_guid = 26)
      LocalObject(975, Locker.Constructor(Vector3(3585.75f, 3259.275f, 103.9877f)), owning_building_guid = 26)
      LocalObject(976, Locker.Constructor(Vector3(3586.993f, 3259.056f, 103.9877f)), owning_building_guid = 26)
      LocalObject(979, Locker.Constructor(Vector3(3588.226f, 3258.838f, 103.9877f)), owning_building_guid = 26)
      LocalObject(1192, Locker.Constructor(Vector3(3522.802f, 3272.138f, 113.9927f)), owning_building_guid = 26)
      LocalObject(1193, Locker.Constructor(Vector3(3523.82f, 3271.959f, 113.9927f)), owning_building_guid = 26)
      LocalObject(1194, Locker.Constructor(Vector3(3526.299f, 3271.521f, 113.7637f)), owning_building_guid = 26)
      LocalObject(1195, Locker.Constructor(Vector3(3527.317f, 3271.342f, 113.7637f)), owning_building_guid = 26)
      LocalObject(1196, Locker.Constructor(Vector3(3528.355f, 3271.159f, 113.7637f)), owning_building_guid = 26)
      LocalObject(1197, Locker.Constructor(Vector3(3529.373f, 3270.979f, 113.7637f)), owning_building_guid = 26)
      LocalObject(1198, Locker.Constructor(Vector3(3531.857f, 3270.542f, 113.9927f)), owning_building_guid = 26)
      LocalObject(1199, Locker.Constructor(Vector3(3532.875f, 3270.362f, 113.9927f)), owning_building_guid = 26)
      LocalObject(
        166,
        Terminal.Constructor(Vector3(3581.239f, 3263.698f, 103.9827f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        167,
        Terminal.Constructor(Vector3(3582.414f, 3262.021f, 103.9827f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        168,
        Terminal.Constructor(Vector3(3583.444f, 3276.206f, 103.9827f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        169,
        Terminal.Constructor(Vector3(3585.122f, 3277.38f, 103.9827f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        170,
        Terminal.Constructor(Vector3(3589.627f, 3260.749f, 103.9827f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        171,
        Terminal.Constructor(Vector3(3591.305f, 3261.924f, 103.9827f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        172,
        Terminal.Constructor(Vector3(3592.335f, 3276.108f, 103.9827f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        173,
        Terminal.Constructor(Vector3(3593.51f, 3274.431f, 103.9827f), cert_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1356,
        Terminal.Constructor(Vector3(3542.056f, 3268.343f, 105.5827f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1357,
        Terminal.Constructor(Vector3(3545.73f, 3267.695f, 105.5827f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1358,
        Terminal.Constructor(Vector3(3549.123f, 3296.221f, 115.2877f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1359,
        Terminal.Constructor(Vector3(3549.462f, 3267.037f, 105.5827f), order_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1924,
        Terminal.Constructor(Vector3(3485.064f, 3313.672f, 108.1057f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1925,
        Terminal.Constructor(Vector3(3524.235f, 3288.493f, 115.5717f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1926,
        Terminal.Constructor(Vector3(3540.331f, 3282.541f, 106.1267f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1927,
        Terminal.Constructor(Vector3(3544.362f, 3327.495f, 105.6057f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1928,
        Terminal.Constructor(Vector3(3547.512f, 3281.279f, 106.1267f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1929,
        Terminal.Constructor(Vector3(3554.689f, 3280.01f, 106.1267f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1930,
        Terminal.Constructor(Vector3(3601.131f, 3277.47f, 105.6057f), spawn_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        2105,
        Terminal.Constructor(Vector3(3469.24f, 3261.916f, 116.2977f), vehicle_terminal_combined),
        owning_building_guid = 26
      )
      LocalObject(
        1313,
        VehicleSpawnPad.Constructor(Vector3(3471.52f, 3275.363f, 112.1397f), mb_pad_creation, Vector3(0, 0, 10)),
        owning_building_guid = 26,
        terminal_guid = 2105
      )
      LocalObject(1822, ResourceSilo.Constructor(Vector3(3598.545f, 3338.521f, 121.0097f)), owning_building_guid = 26)
      LocalObject(
        1840,
        SpawnTube.Constructor(Vector3(3541.877f, 3283.018f, 103.9927f), Vector3(0, 0, 100)),
        owning_building_guid = 26
      )
      LocalObject(
        1841,
        SpawnTube.Constructor(Vector3(3549.057f, 3281.752f, 103.9927f), Vector3(0, 0, 100)),
        owning_building_guid = 26
      )
      LocalObject(
        1842,
        SpawnTube.Constructor(Vector3(3556.234f, 3280.487f, 103.9927f), Vector3(0, 0, 100)),
        owning_building_guid = 26
      )
      LocalObject(
        115,
        ProximityTerminal.Constructor(Vector3(3530.656f, 3287.281f, 113.8027f), adv_med_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1327,
        ProximityTerminal.Constructor(Vector3(3537.092f, 3260.092f, 103.9927f), medical_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1521,
        ProximityTerminal.Constructor(Vector3(3470.142f, 3301.795f, 122.3447f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1522,
        Terminal.Constructor(Vector3(3470.142f, 3301.795f, 122.3447f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1524,
        ProximityTerminal.Constructor(Vector3(3480.148f, 3316.597f, 124.2857f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1525,
        Terminal.Constructor(Vector3(3480.148f, 3316.597f, 124.2857f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1527,
        ProximityTerminal.Constructor(Vector3(3582.336f, 3278.839f, 124.3247f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1528,
        Terminal.Constructor(Vector3(3582.336f, 3278.839f, 124.3247f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1533,
        ProximityTerminal.Constructor(Vector3(3593.423f, 3293.356f, 122.3347f), pad_landing_frame),
        owning_building_guid = 26
      )
      LocalObject(
        1534,
        Terminal.Constructor(Vector3(3593.423f, 3293.356f, 122.3347f), air_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1752,
        ProximityTerminal.Constructor(Vector3(3460.603f, 3337.67f, 113.7427f), repair_silo),
        owning_building_guid = 26
      )
      LocalObject(
        1753,
        Terminal.Constructor(Vector3(3460.603f, 3337.67f, 113.7427f), ground_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1760,
        ProximityTerminal.Constructor(Vector3(3544.499f, 3233.514f, 113.7427f), repair_silo),
        owning_building_guid = 26
      )
      LocalObject(
        1761,
        Terminal.Constructor(Vector3(3544.499f, 3233.514f, 113.7427f), ground_rearm_terminal),
        owning_building_guid = 26
      )
      LocalObject(
        1238,
        FacilityTurret.Constructor(Vector3(3431.791f, 3242.12f, 122.3947f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1238, 5032)
      LocalObject(
        1240,
        FacilityTurret.Constructor(Vector3(3455.28f, 3375.29f, 122.3947f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1240, 5033)
      LocalObject(
        1244,
        FacilityTurret.Constructor(Vector3(3564.656f, 3217.487f, 122.3947f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1244, 5034)
      LocalObject(
        1249,
        FacilityTurret.Constructor(Vector3(3614.603f, 3252.494f, 122.3947f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1249, 5035)
      LocalObject(
        1250,
        FacilityTurret.Constructor(Vector3(3629.684f, 3344.546f, 122.3947f), manned_turret),
        owning_building_guid = 26
      )
      TurretToWeapon(1250, 5036)
      LocalObject(
        705,
        ImplantTerminalMech.Constructor(Vector3(3579.946f, 3270.383f, 103.4697f)),
        owning_building_guid = 26
      )
      LocalObject(
        699,
        Terminal.Constructor(Vector3(3579.964f, 3270.379f, 103.4697f), implant_terminal_interface),
        owning_building_guid = 26
      )
      TerminalToInterface(705, 699)
      LocalObject(
        706,
        ImplantTerminalMech.Constructor(Vector3(3595.071f, 3267.728f, 103.4697f)),
        owning_building_guid = 26
      )
      LocalObject(
        700,
        Terminal.Constructor(Vector3(3595.053f, 3267.731f, 103.4697f), implant_terminal_interface),
        owning_building_guid = 26
      )
      TerminalToInterface(706, 700)
      LocalObject(
        1631,
        Painbox.Constructor(Vector3(3517.661f, 3285.906f, 128.0215f), painbox),
        owning_building_guid = 26
      )
      LocalObject(
        1641,
        Painbox.Constructor(Vector3(3543.033f, 3272.132f, 108.0626f), painbox_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        1651,
        Painbox.Constructor(Vector3(3532.151f, 3284.785f, 128.2266f), painbox_door_radius),
        owning_building_guid = 26
      )
      LocalObject(
        1665,
        Painbox.Constructor(Vector3(3533.997f, 3278.003f, 106.3486f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        1666,
        Painbox.Constructor(Vector3(3551.919f, 3257.234f, 107.5336f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(
        1667,
        Painbox.Constructor(Vector3(3561.265f, 3271.189f, 105.7069f), painbox_door_radius_continuous),
        owning_building_guid = 26
      )
      LocalObject(222, Generator.Constructor(Vector3(3514.104f, 3288.126f, 122.6987f)), owning_building_guid = 26)
      LocalObject(
        212,
        Terminal.Constructor(Vector3(3522.18f, 3286.75f, 123.9927f), gen_control),
        owning_building_guid = 26
      )
    }

    Building6()

    def Building6(): Unit = { // Name: Neit Type: cryo_facility GUID: 29, MapID: 6
      LocalBuilding(
        "Neit",
        29,
        6,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(4350f, 4238f, 75.96667f),
            Vector3(0f, 0f, 42f),
            cryo_facility
          )
        )
      )
      LocalObject(
        160,
        CaptureTerminal.Constructor(Vector3(4289.469f, 4263.248f, 65.96667f), capture_terminal),
        owning_building_guid = 29
      )
      LocalObject(332, Door.Constructor(Vector3(4273.177f, 4260.066f, 77.51768f)), owning_building_guid = 29)
      LocalObject(333, Door.Constructor(Vector3(4286.697f, 4272.239f, 85.48167f)), owning_building_guid = 29)
      LocalObject(334, Door.Constructor(Vector3(4290.987f, 4215.401f, 85.48167f)), owning_building_guid = 29)
      LocalObject(336, Door.Constructor(Vector3(4303.161f, 4201.881f, 77.51768f)), owning_building_guid = 29)
      LocalObject(338, Door.Constructor(Vector3(4324.727f, 4242.157f, 87.48767f)), owning_building_guid = 29)
      LocalObject(340, Door.Constructor(Vector3(4350.874f, 4243.008f, 87.48767f)), owning_building_guid = 29)
      LocalObject(341, Door.Constructor(Vector3(4409.433f, 4179.924f, 85.48167f)), owning_building_guid = 29)
      LocalObject(342, Door.Constructor(Vector3(4409.593f, 4247.925f, 77.51768f)), owning_building_guid = 29)
      LocalObject(343, Door.Constructor(Vector3(4421.767f, 4234.405f, 85.48167f)), owning_building_guid = 29)
      LocalObject(344, Door.Constructor(Vector3(4422.953f, 4192.098f, 77.51768f)), owning_building_guid = 29)
      LocalObject(345, Door.Constructor(Vector3(4435.341f, 4191.043f, 77.48768f)), owning_building_guid = 29)
      LocalObject(548, Door.Constructor(Vector3(4280.718f, 4267.122f, 67.48767f)), owning_building_guid = 29)
      LocalObject(549, Door.Constructor(Vector3(4286.664f, 4272.475f, 67.48767f)), owning_building_guid = 29)
      LocalObject(550, Door.Constructor(Vector3(4292.609f, 4277.828f, 67.48767f)), owning_building_guid = 29)
      LocalObject(551, Door.Constructor(Vector3(4296.481f, 4243.637f, 67.48767f)), owning_building_guid = 29)
      LocalObject(552, Door.Constructor(Vector3(4312.837f, 4231.451f, 67.48767f)), owning_building_guid = 29)
      LocalObject(553, Door.Constructor(Vector3(4314.317f, 4259.696f, 67.48767f)), owning_building_guid = 29)
      LocalObject(554, Door.Constructor(Vector3(4315.205f, 4276.644f, 67.48767f)), owning_building_guid = 29)
      LocalObject(555, Door.Constructor(Vector3(4322.358f, 4196.964f, 67.48767f)), owning_building_guid = 29)
      LocalObject(556, Door.Constructor(Vector3(4323.247f, 4213.911f, 67.48767f)), owning_building_guid = 29)
      LocalObject(557, Door.Constructor(Vector3(4324.727f, 4242.157f, 67.48767f)), owning_building_guid = 29)
      LocalObject(558, Door.Constructor(Vector3(4324.727f, 4242.157f, 77.48768f)), owning_building_guid = 29)
      LocalObject(559, Door.Constructor(Vector3(4341.082f, 4229.97f, 67.48767f)), owning_building_guid = 29)
      LocalObject(560, Door.Constructor(Vector3(4342.858f, 4263.865f, 67.48767f)), owning_building_guid = 29)
      LocalObject(561, Door.Constructor(Vector3(4352.677f, 4235.027f, 67.48767f)), owning_building_guid = 29)
      LocalObject(562, Door.Constructor(Vector3(4354.453f, 4268.922f, 67.48767f)), owning_building_guid = 29)
      LocalObject(563, Door.Constructor(Vector3(4356.549f, 4200.837f, 67.48767f)), owning_building_guid = 29)
      LocalObject(564, Door.Constructor(Vector3(4358.326f, 4234.731f, 87.48767f)), owning_building_guid = 29)
      LocalObject(565, Door.Constructor(Vector3(4358.918f, 4246.03f, 67.48767f)), owning_building_guid = 29)
      LocalObject(566, Door.Constructor(Vector3(4360.422f, 4166.646f, 69.98767f)), owning_building_guid = 29)
      LocalObject(567, Door.Constructor(Vector3(4365.159f, 4257.032f, 59.98767f)), owning_building_guid = 29)
      LocalObject(568, Door.Constructor(Vector3(4368.736f, 4217.192f, 67.48767f)), owning_building_guid = 29)
      LocalObject(569, Door.Constructor(Vector3(4372.608f, 4183.001f, 69.98767f)), owning_building_guid = 29)
      LocalObject(570, Door.Constructor(Vector3(4399.966f, 4164.574f, 69.98767f)), owning_building_guid = 29)
      LocalObject(687, Door.Constructor(Vector3(4336.183f, 4259.312f, 78.24967f)), owning_building_guid = 29)
      LocalObject(695, Door.Constructor(Vector3(4341.674f, 4241.269f, 77.48567f)), owning_building_guid = 29)
      LocalObject(696, Door.Constructor(Vector3(4358.918f, 4246.03f, 77.48768f)), owning_building_guid = 29)
      LocalObject(2007, Door.Constructor(Vector3(4339.879f, 4256.224f, 67.82068f)), owning_building_guid = 29)
      LocalObject(2008, Door.Constructor(Vector3(4344.757f, 4250.807f, 67.82068f)), owning_building_guid = 29)
      LocalObject(2009, Door.Constructor(Vector3(4349.637f, 4245.387f, 67.82068f)), owning_building_guid = 29)
      LocalObject(
        725,
        IFFLock.Constructor(Vector3(4331.956f, 4259.191f, 77.44868f), Vector3(0, 0, 318)),
        owning_building_guid = 29,
        door_guid = 687
      )
      LocalObject(
        810,
        IFFLock.Constructor(Vector3(4281.072f, 4265.325f, 67.30267f), Vector3(0, 0, 228)),
        owning_building_guid = 29,
        door_guid = 548
      )
      LocalObject(
        811,
        IFFLock.Constructor(Vector3(4286.216f, 4274.187f, 67.30267f), Vector3(0, 0, 48)),
        owning_building_guid = 29,
        door_guid = 549
      )
      LocalObject(
        812,
        IFFLock.Constructor(Vector3(4323.965f, 4244.22f, 87.41867f), Vector3(0, 0, 48)),
        owning_building_guid = 29,
        door_guid = 338
      )
      LocalObject(
        813,
        IFFLock.Constructor(Vector3(4341.148f, 4263.415f, 67.30267f), Vector3(0, 0, 318)),
        owning_building_guid = 29,
        door_guid = 560
      )
      LocalObject(
        814,
        IFFLock.Constructor(Vector3(4348.804f, 4242.249f, 87.41867f), Vector3(0, 0, 318)),
        owning_building_guid = 29,
        door_guid = 340
      )
      LocalObject(
        815,
        IFFLock.Constructor(Vector3(4360.628f, 4246.479f, 67.30267f), Vector3(0, 0, 138)),
        owning_building_guid = 29,
        door_guid = 565
      )
      LocalObject(
        816,
        IFFLock.Constructor(Vector3(4365.609f, 4255.321f, 59.80267f), Vector3(0, 0, 228)),
        owning_building_guid = 29,
        door_guid = 567
      )
      LocalObject(
        817,
        IFFLock.Constructor(Vector3(4434.579f, 4193.105f, 77.41868f), Vector3(0, 0, 48)),
        owning_building_guid = 29,
        door_guid = 345
      )
      LocalObject(1030, Locker.Constructor(Vector3(4328.375f, 4291.902f, 65.96168f)), owning_building_guid = 29)
      LocalObject(1031, Locker.Constructor(Vector3(4329.213f, 4290.972f, 65.96168f)), owning_building_guid = 29)
      LocalObject(1032, Locker.Constructor(Vector3(4330.057f, 4290.034f, 65.96168f)), owning_building_guid = 29)
      LocalObject(1033, Locker.Constructor(Vector3(4330.901f, 4289.097f, 65.96168f)), owning_building_guid = 29)
      LocalObject(1034, Locker.Constructor(Vector3(4331.741f, 4288.163f, 65.96168f)), owning_building_guid = 29)
      LocalObject(1035, Locker.Constructor(Vector3(4359.423f, 4276.574f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1036, Locker.Constructor(Vector3(4360.133f, 4250.005f, 66.22768f)), owning_building_guid = 29)
      LocalObject(1037, Locker.Constructor(Vector3(4360.207f, 4277.28f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1038, Locker.Constructor(Vector3(4360.998f, 4250.784f, 66.22768f)), owning_building_guid = 29)
      LocalObject(1039, Locker.Constructor(Vector3(4360.995f, 4277.99f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1040, Locker.Constructor(Vector3(4361.779f, 4278.695f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1041, Locker.Constructor(Vector3(4361.851f, 4251.551f, 66.22768f)), owning_building_guid = 29)
      LocalObject(1042, Locker.Constructor(Vector3(4362.563f, 4279.401f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1043, Locker.Constructor(Vector3(4362.704f, 4252.32f, 66.22768f)), owning_building_guid = 29)
      LocalObject(1044, Locker.Constructor(Vector3(4363.348f, 4280.108f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1045, Locker.Constructor(Vector3(4372.809f, 4261.713f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1046, Locker.Constructor(Vector3(4373.593f, 4262.419f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1047, Locker.Constructor(Vector3(4374.377f, 4263.125f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1048, Locker.Constructor(Vector3(4375.162f, 4263.831f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1049, Locker.Constructor(Vector3(4375.949f, 4264.541f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1050, Locker.Constructor(Vector3(4376.733f, 4265.247f, 65.87467f)), owning_building_guid = 29)
      LocalObject(1200, Locker.Constructor(Vector3(4364.897f, 4248.744f, 75.96667f)), owning_building_guid = 29)
      LocalObject(1201, Locker.Constructor(Vector3(4365.589f, 4247.976f, 75.96667f)), owning_building_guid = 29)
      LocalObject(1202, Locker.Constructor(Vector3(4367.277f, 4246.101f, 75.73768f)), owning_building_guid = 29)
      LocalObject(1203, Locker.Constructor(Vector3(4367.969f, 4245.333f, 75.73768f)), owning_building_guid = 29)
      LocalObject(1204, Locker.Constructor(Vector3(4368.674f, 4244.55f, 75.73768f)), owning_building_guid = 29)
      LocalObject(1205, Locker.Constructor(Vector3(4369.366f, 4243.781f, 75.73768f)), owning_building_guid = 29)
      LocalObject(1206, Locker.Constructor(Vector3(4371.05f, 4241.911f, 75.96667f)), owning_building_guid = 29)
      LocalObject(1207, Locker.Constructor(Vector3(4371.742f, 4241.142f, 75.96667f)), owning_building_guid = 29)
      LocalObject(
        174,
        Terminal.Constructor(Vector3(4314.504f, 4280.823f, 65.95667f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        175,
        Terminal.Constructor(Vector3(4314.612f, 4282.868f, 65.95667f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        176,
        Terminal.Constructor(Vector3(4319.406f, 4275.38f, 65.95667f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        177,
        Terminal.Constructor(Vector3(4321.451f, 4275.272f, 65.95667f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        178,
        Terminal.Constructor(Vector3(4324.05f, 4291.366f, 65.95667f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        179,
        Terminal.Constructor(Vector3(4326.095f, 4291.259f, 65.95667f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        180,
        Terminal.Constructor(Vector3(4330.889f, 4283.771f, 65.95667f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        181,
        Terminal.Constructor(Vector3(4330.996f, 4285.815f, 65.95667f), cert_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1386,
        Terminal.Constructor(Vector3(4336.174f, 4238.369f, 77.26167f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1387,
        Terminal.Constructor(Vector3(4353.874f, 4261.576f, 67.55667f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1388,
        Terminal.Constructor(Vector3(4356.409f, 4258.76f, 67.55667f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1389,
        Terminal.Constructor(Vector3(4358.906f, 4255.987f, 67.55667f), order_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1940,
        Terminal.Constructor(Vector3(4306.735f, 4285.165f, 67.57967f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1941,
        Terminal.Constructor(Vector3(4320.671f, 4210.794f, 67.57967f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1942,
        Terminal.Constructor(Vector3(4341.768f, 4254.571f, 68.10068f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1943,
        Terminal.Constructor(Vector3(4346.642f, 4249.153f, 68.10068f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1944,
        Terminal.Constructor(Vector3(4351.524f, 4243.736f, 68.10068f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1945,
        Terminal.Constructor(Vector3(4360.543f, 4229.138f, 77.54568f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1946,
        Terminal.Constructor(Vector3(4375.909f, 4185.179f, 70.07967f), spawn_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        2107,
        Terminal.Constructor(Vector3(4420.243f, 4216.222f, 78.27168f), vehicle_terminal_combined),
        owning_building_guid = 29
      )
      LocalObject(
        1315,
        VehicleSpawnPad.Constructor(Vector3(4410.167f, 4207.028f, 74.11368f), mb_pad_creation, Vector3(0, 0, 228)),
        owning_building_guid = 29,
        terminal_guid = 2107
      )
      LocalObject(1824, ResourceSilo.Constructor(Vector3(4271.186f, 4235.464f, 82.98367f)), owning_building_guid = 29)
      LocalObject(
        1858,
        SpawnTube.Constructor(Vector3(4340.257f, 4255.147f, 65.96667f), Vector3(0, 0, 318)),
        owning_building_guid = 29
      )
      LocalObject(
        1859,
        SpawnTube.Constructor(Vector3(4345.134f, 4249.731f, 65.96667f), Vector3(0, 0, 318)),
        owning_building_guid = 29
      )
      LocalObject(
        1860,
        SpawnTube.Constructor(Vector3(4350.012f, 4244.312f, 65.96667f), Vector3(0, 0, 318)),
        owning_building_guid = 29
      )
      LocalObject(
        116,
        ProximityTerminal.Constructor(Vector3(4356.23f, 4234.044f, 75.77667f), adv_med_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1330,
        ProximityTerminal.Constructor(Vector3(4367.897f, 4259.433f, 65.96667f), medical_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1545,
        ProximityTerminal.Constructor(Vector3(4303.028f, 4267.901f, 84.30868f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1546,
        Terminal.Constructor(Vector3(4303.028f, 4267.901f, 84.30868f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1548,
        ProximityTerminal.Constructor(Vector3(4320.703f, 4272.515f, 86.29868f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1549,
        Terminal.Constructor(Vector3(4320.703f, 4272.515f, 86.29868f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1551,
        ProximityTerminal.Constructor(Vector3(4377.982f, 4179.848f, 86.25967f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1552,
        Terminal.Constructor(Vector3(4377.982f, 4179.848f, 86.25967f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1554,
        ProximityTerminal.Constructor(Vector3(4394.98f, 4185.352f, 84.31867f), pad_landing_frame),
        owning_building_guid = 29
      )
      LocalObject(
        1555,
        Terminal.Constructor(Vector3(4394.98f, 4185.352f, 84.31867f), air_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1772,
        ProximityTerminal.Constructor(Vector3(4378.424f, 4284.937f, 75.71667f), repair_silo),
        owning_building_guid = 29
      )
      LocalObject(
        1773,
        Terminal.Constructor(Vector3(4378.424f, 4284.937f, 75.71667f), ground_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1776,
        ProximityTerminal.Constructor(Vector3(4380.41f, 4151.209f, 75.71667f), repair_silo),
        owning_building_guid = 29
      )
      LocalObject(
        1777,
        Terminal.Constructor(Vector3(4380.41f, 4151.209f, 75.71667f), ground_rearm_terminal),
        owning_building_guid = 29
      )
      LocalObject(
        1263,
        FacilityTurret.Constructor(Vector3(4242.938f, 4249.887f, 84.36868f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1263, 5037)
      LocalObject(
        1264,
        FacilityTurret.Constructor(Vector3(4311.496f, 4313.141f, 84.36868f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1264, 5038)
      LocalObject(
        1265,
        FacilityTurret.Constructor(Vector3(4361.443f, 4118.287f, 84.36868f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1265, 5039)
      LocalObject(
        1266,
        FacilityTurret.Constructor(Vector3(4372.407f, 4309.977f, 84.36868f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1266, 5040)
      LocalObject(
        1267,
        FacilityTurret.Constructor(Vector3(4461.941f, 4208.765f, 84.36868f), manned_turret),
        owning_building_guid = 29
      )
      TurretToWeapon(1267, 5041)
      LocalObject(
        707,
        ImplantTerminalMech.Constructor(Vector3(4317.508f, 4289.111f, 65.44367f)),
        owning_building_guid = 29
      )
      LocalObject(
        701,
        Terminal.Constructor(Vector3(4317.521f, 4289.098f, 65.44367f), implant_terminal_interface),
        owning_building_guid = 29
      )
      TerminalToInterface(707, 701)
      LocalObject(
        708,
        ImplantTerminalMech.Constructor(Vector3(4327.792f, 4277.708f, 65.44367f)),
        owning_building_guid = 29
      )
      LocalObject(
        702,
        Terminal.Constructor(Vector3(4327.78f, 4277.721f, 65.44367f), implant_terminal_interface),
        owning_building_guid = 29
      )
      TerminalToInterface(708, 702)
      LocalObject(
        1633,
        Painbox.Constructor(Vector3(4367.316f, 4227.128f, 89.99548f), painbox),
        owning_building_guid = 29
      )
      LocalObject(
        1643,
        Painbox.Constructor(Vector3(4355.803f, 4253.603f, 70.03658f), painbox_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        1653,
        Painbox.Constructor(Vector3(4356.589f, 4236.933f, 90.20058f), painbox_door_radius),
        owning_building_guid = 29
      )
      LocalObject(
        1671,
        Painbox.Constructor(Vector3(4342.017f, 4265.571f, 67.68088f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        1672,
        Painbox.Constructor(Vector3(4357.973f, 4270.813f, 69.50758f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(
        1673,
        Painbox.Constructor(Vector3(4359.309f, 4243.413f, 68.32257f), painbox_door_radius_continuous),
        owning_building_guid = 29
      )
      LocalObject(224, Generator.Constructor(Vector3(4368.752f, 4223.188f, 84.67268f)), owning_building_guid = 29)
      LocalObject(
        214,
        Terminal.Constructor(Vector3(4363.236f, 4229.245f, 85.96667f), gen_control),
        owning_building_guid = 29
      )
    }

    Building7()

    def Building7(): Unit = { // Name: Lugh Type: cryo_facility GUID: 32, MapID: 7
      LocalBuilding(
        "Lugh",
        32,
        7,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(6148f, 5118f, 71.64068f),
            Vector3(0f, 0f, 165f),
            cryo_facility
          )
        )
      )
      LocalObject(
        165,
        CaptureTerminal.Constructor(Vector3(6159.792f, 5053.483f, 61.64068f), capture_terminal),
        owning_building_guid = 32
      )
      LocalObject(444, Door.Constructor(Vector3(6107.22f, 5162.573f, 73.19168f)), owning_building_guid = 32)
      LocalObject(445, Door.Constructor(Vector3(6111.929f, 5180.146f, 81.15568f)), owning_building_guid = 32)
      LocalObject(446, Door.Constructor(Vector3(6140.902f, 5215.147f, 73.16168f)), owning_building_guid = 32)
      LocalObject(447, Door.Constructor(Vector3(6143.324f, 5116.005f, 83.16168f)), owning_building_guid = 32)
      LocalObject(448, Door.Constructor(Vector3(6146.764f, 5204.184f, 73.19168f)), owning_building_guid = 32)
      LocalObject(449, Door.Constructor(Vector3(6153.762f, 5046.261f, 81.15568f)), owning_building_guid = 32)
      LocalObject(450, Door.Constructor(Vector3(6158.278f, 5094.541f, 83.16168f)), owning_building_guid = 32)
      LocalObject(451, Door.Constructor(Vector3(6164.337f, 5199.475f, 81.15568f)), owning_building_guid = 32)
      LocalObject(452, Door.Constructor(Vector3(6171.335f, 5041.553f, 73.19168f)), owning_building_guid = 32)
      LocalObject(453, Door.Constructor(Vector3(6199.094f, 5080.816f, 81.15568f)), owning_building_guid = 32)
      LocalObject(454, Door.Constructor(Vector3(6203.803f, 5098.389f, 73.19168f)), owning_building_guid = 32)
      LocalObject(660, Door.Constructor(Vector3(6119.642f, 5104.893f, 63.16168f)), owning_building_guid = 32)
      LocalObject(661, Door.Constructor(Vector3(6123.783f, 5120.348f, 55.66168f)), owning_building_guid = 32)
      LocalObject(662, Door.Constructor(Vector3(6130.197f, 5097.924f, 63.16168f)), owning_building_guid = 32)
      LocalObject(663, Door.Constructor(Vector3(6134.542f, 5067.772f, 63.16168f)), owning_building_guid = 32)
      LocalObject(664, Door.Constructor(Vector3(6136.409f, 5121.106f, 63.16168f)), owning_building_guid = 32)
      LocalObject(665, Door.Constructor(Vector3(6145.855f, 5048.176f, 63.16168f)), owning_building_guid = 32)
      LocalObject(666, Door.Constructor(Vector3(6146.207f, 5126.763f, 83.16168f)), owning_building_guid = 32)
      LocalObject(667, Door.Constructor(Vector3(6149.035f, 5121.864f, 63.16168f)), owning_building_guid = 32)
      LocalObject(668, Door.Constructor(Vector3(6149.238f, 5076.257f, 63.16168f)), owning_building_guid = 32)
      LocalObject(669, Door.Constructor(Vector3(6153.583f, 5046.105f, 63.16168f)), owning_building_guid = 32)
      LocalObject(670, Door.Constructor(Vector3(6155.247f, 5145.046f, 63.16168f)), owning_building_guid = 32)
      LocalObject(671, Door.Constructor(Vector3(6158.278f, 5094.541f, 63.16168f)), owning_building_guid = 32)
      LocalObject(672, Door.Constructor(Vector3(6158.278f, 5094.541f, 73.16168f)), owning_building_guid = 32)
      LocalObject(673, Door.Constructor(Vector3(6159.591f, 5114.894f, 63.16168f)), owning_building_guid = 32)
      LocalObject(674, Door.Constructor(Vector3(6161.31f, 5044.035f, 63.16168f)), owning_building_guid = 32)
      LocalObject(675, Door.Constructor(Vector3(6172.42f, 5070.045f, 63.16168f)), owning_building_guid = 32)
      LocalObject(676, Door.Constructor(Vector3(6173.733f, 5090.399f, 63.16168f)), owning_building_guid = 32)
      LocalObject(677, Door.Constructor(Vector3(6175.601f, 5143.733f, 63.16168f)), owning_building_guid = 32)
      LocalObject(678, Door.Constructor(Vector3(6181.812f, 5166.916f, 65.66168f)), owning_building_guid = 32)
      LocalObject(679, Door.Constructor(Vector3(6182.367f, 5199.896f, 65.66168f)), owning_building_guid = 32)
      LocalObject(680, Door.Constructor(Vector3(6182.773f, 5108.683f, 63.16168f)), owning_building_guid = 32)
      LocalObject(681, Door.Constructor(Vector3(6197.47f, 5117.168f, 63.16168f)), owning_building_guid = 32)
      LocalObject(682, Door.Constructor(Vector3(6202.166f, 5165.603f, 65.66168f)), owning_building_guid = 32)
      LocalObject(692, Door.Constructor(Vector3(6137.652f, 5094.805f, 73.92368f)), owning_building_guid = 32)
      LocalObject(697, Door.Constructor(Vector3(6136.409f, 5121.106f, 73.16168f)), owning_building_guid = 32)
      LocalObject(698, Door.Constructor(Vector3(6149.793f, 5109.237f, 73.15968f)), owning_building_guid = 32)
      LocalObject(2038, Door.Constructor(Vector3(6138.229f, 5099.587f, 63.49468f)), owning_building_guid = 32)
      LocalObject(2039, Door.Constructor(Vector3(6140.115f, 5106.627f, 63.49468f)), owning_building_guid = 32)
      LocalObject(2040, Door.Constructor(Vector3(6142.002f, 5113.672f, 63.49468f)), owning_building_guid = 32)
      LocalObject(
        730,
        IFFLock.Constructor(Vector3(6140.055f, 5091.325f, 73.12268f), Vector3(0, 0, 195)),
        owning_building_guid = 32,
        door_guid = 692
      )
      LocalObject(
        897,
        IFFLock.Constructor(Vector3(6124.972f, 5121.657f, 55.47668f), Vector3(0, 0, 105)),
        owning_building_guid = 32,
        door_guid = 661
      )
      LocalObject(
        898,
        IFFLock.Constructor(Vector3(6131.506f, 5096.734f, 62.97668f), Vector3(0, 0, 195)),
        owning_building_guid = 32,
        door_guid = 662
      )
      LocalObject(
        899,
        IFFLock.Constructor(Vector3(6135.1f, 5122.295f, 62.97668f), Vector3(0, 0, 15)),
        owning_building_guid = 32,
        door_guid = 664
      )
      LocalObject(
        900,
        IFFLock.Constructor(Vector3(6139.587f, 5213.386f, 73.09268f), Vector3(0, 0, 285)),
        owning_building_guid = 32,
        door_guid = 446
      )
      LocalObject(
        901,
        IFFLock.Constructor(Vector3(6145.088f, 5114.683f, 83.09268f), Vector3(0, 0, 195)),
        owning_building_guid = 32,
        door_guid = 447
      )
      LocalObject(
        902,
        IFFLock.Constructor(Vector3(6152.39f, 5044.797f, 62.97668f), Vector3(0, 0, 285)),
        owning_building_guid = 32,
        door_guid = 669
      )
      LocalObject(
        903,
        IFFLock.Constructor(Vector3(6156.963f, 5092.778f, 83.09268f), Vector3(0, 0, 285)),
        owning_building_guid = 32,
        door_guid = 450
      )
      LocalObject(
        904,
        IFFLock.Constructor(Vector3(6162.625f, 5045.31f, 62.97668f), Vector3(0, 0, 105)),
        owning_building_guid = 32,
        door_guid = 674
      )
      LocalObject(1163, Locker.Constructor(Vector3(6105.416f, 5106.261f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1164, Locker.Constructor(Vector3(6106.436f, 5105.987f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1165, Locker.Constructor(Vector3(6107.455f, 5105.714f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1166, Locker.Constructor(Vector3(6108.474f, 5105.441f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1167, Locker.Constructor(Vector3(6109.499f, 5105.167f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1168, Locker.Constructor(Vector3(6110.517f, 5104.894f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1169, Locker.Constructor(Vector3(6110.589f, 5125.581f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1170, Locker.Constructor(Vector3(6111.608f, 5125.308f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1171, Locker.Constructor(Vector3(6112.632f, 5125.034f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1172, Locker.Constructor(Vector3(6113.651f, 5124.76f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1173, Locker.Constructor(Vector3(6114.572f, 5070.506f, 61.63568f)), owning_building_guid = 32)
      LocalObject(1174, Locker.Constructor(Vector3(6114.67f, 5124.487f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1175, Locker.Constructor(Vector3(6114.896f, 5071.716f, 61.63568f)), owning_building_guid = 32)
      LocalObject(1176, Locker.Constructor(Vector3(6115.223f, 5072.935f, 61.63568f)), owning_building_guid = 32)
      LocalObject(1177, Locker.Constructor(Vector3(6115.549f, 5074.153f, 61.63568f)), owning_building_guid = 32)
      LocalObject(1178, Locker.Constructor(Vector3(6115.69f, 5124.214f, 61.54868f)), owning_building_guid = 32)
      LocalObject(1179, Locker.Constructor(Vector3(6115.874f, 5075.366f, 61.63568f)), owning_building_guid = 32)
      LocalObject(1180, Locker.Constructor(Vector3(6129.071f, 5120.855f, 61.90168f)), owning_building_guid = 32)
      LocalObject(1181, Locker.Constructor(Vector3(6130.181f, 5120.558f, 61.90168f)), owning_building_guid = 32)
      LocalObject(1182, Locker.Constructor(Vector3(6131.289f, 5120.261f, 61.90168f)), owning_building_guid = 32)
      LocalObject(1183, Locker.Constructor(Vector3(6132.413f, 5119.96f, 61.90168f)), owning_building_guid = 32)
      LocalObject(1208, Locker.Constructor(Vector3(6130.875f, 5124.643f, 71.64068f)), owning_building_guid = 32)
      LocalObject(1209, Locker.Constructor(Vector3(6131.144f, 5125.641f, 71.64068f)), owning_building_guid = 32)
      LocalObject(1210, Locker.Constructor(Vector3(6131.796f, 5128.077f, 71.41168f)), owning_building_guid = 32)
      LocalObject(1211, Locker.Constructor(Vector3(6132.063f, 5129.076f, 71.41168f)), owning_building_guid = 32)
      LocalObject(1212, Locker.Constructor(Vector3(6132.336f, 5130.094f, 71.41168f)), owning_building_guid = 32)
      LocalObject(1213, Locker.Constructor(Vector3(6132.604f, 5131.093f, 71.41168f)), owning_building_guid = 32)
      LocalObject(1214, Locker.Constructor(Vector3(6133.255f, 5133.524f, 71.64068f)), owning_building_guid = 32)
      LocalObject(1215, Locker.Constructor(Vector3(6133.523f, 5134.523f, 71.64068f)), owning_building_guid = 32)
      LocalObject(
        182,
        Terminal.Constructor(Vector3(6116.353f, 5068.944f, 61.63068f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        183,
        Terminal.Constructor(Vector3(6117.377f, 5067.171f, 61.63068f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        184,
        Terminal.Constructor(Vector3(6118.249f, 5076.02f, 61.63068f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        185,
        Terminal.Constructor(Vector3(6120.022f, 5077.043f, 61.63068f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        186,
        Terminal.Constructor(Vector3(6129.644f, 5063.884f, 61.63068f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        187,
        Terminal.Constructor(Vector3(6131.417f, 5064.908f, 61.63068f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        188,
        Terminal.Constructor(Vector3(6132.29f, 5073.756f, 61.63068f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        189,
        Terminal.Constructor(Vector3(6133.313f, 5071.983f, 61.63068f), cert_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1438,
        Terminal.Constructor(Vector3(6126.118f, 5108.409f, 63.23068f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1439,
        Terminal.Constructor(Vector3(6127.099f, 5112.068f, 63.23068f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1440,
        Terminal.Constructor(Vector3(6128.064f, 5115.672f, 63.23068f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1441,
        Terminal.Constructor(Vector3(6155.221f, 5106.203f, 72.93568f), order_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1972,
        Terminal.Constructor(Vector3(6132.008f, 5056.027f, 63.25368f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1973,
        Terminal.Constructor(Vector3(6138.586f, 5102.071f, 63.77468f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1974,
        Terminal.Constructor(Vector3(6140.476f, 5109.109f, 63.77468f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1975,
        Terminal.Constructor(Vector3(6142.359f, 5116.154f, 63.77468f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1976,
        Terminal.Constructor(Vector3(6149.69f, 5131.669f, 73.21968f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1977,
        Terminal.Constructor(Vector3(6178.188f, 5168.498f, 65.75368f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1978,
        Terminal.Constructor(Vector3(6186.791f, 5108.22f, 63.25368f), spawn_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        2112,
        Terminal.Constructor(Vector3(6128.008f, 5188.772f, 73.94568f), vehicle_terminal_combined),
        owning_building_guid = 32
      )
      LocalObject(
        1322,
        VehicleSpawnPad.Constructor(Vector3(6141.206f, 5185.329f, 69.78768f), mb_pad_creation, Vector3(0, 0, 105)),
        owning_building_guid = 32,
        terminal_guid = 2112
      )
      LocalObject(1829, ResourceSilo.Constructor(Vector3(6193.052f, 5053.283f, 78.65768f)), owning_building_guid = 32)
      LocalObject(
        1889,
        SpawnTube.Constructor(Vector3(6138.926f, 5100.49f, 61.64068f), Vector3(0, 0, 195)),
        owning_building_guid = 32
      )
      LocalObject(
        1890,
        SpawnTube.Constructor(Vector3(6140.812f, 5107.53f, 61.64068f), Vector3(0, 0, 195)),
        owning_building_guid = 32
      )
      LocalObject(
        1891,
        SpawnTube.Constructor(Vector3(6142.699f, 5114.572f, 61.64068f), Vector3(0, 0, 195)),
        owning_building_guid = 32
      )
      LocalObject(
        117,
        ProximityTerminal.Constructor(Vector3(6147.924f, 5125.379f, 71.45068f), adv_med_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1339,
        ProximityTerminal.Constructor(Vector3(6120.277f, 5121.337f, 61.64068f), medical_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1593,
        ProximityTerminal.Constructor(Vector3(6135.01f, 5074.631f, 81.97268f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        1594,
        Terminal.Constructor(Vector3(6135.01f, 5074.631f, 81.97268f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1596,
        ProximityTerminal.Constructor(Vector3(6148.505f, 5062.321f, 79.98268f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        1597,
        Terminal.Constructor(Vector3(6148.505f, 5062.321f, 79.98268f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1599,
        ProximityTerminal.Constructor(Vector3(6167.657f, 5184.398f, 79.99268f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        1600,
        Terminal.Constructor(Vector3(6167.657f, 5184.398f, 79.99268f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1602,
        ProximityTerminal.Constructor(Vector3(6181.531f, 5173.14f, 81.93368f), pad_landing_frame),
        owning_building_guid = 32
      )
      LocalObject(
        1603,
        Terminal.Constructor(Vector3(6181.531f, 5173.14f, 81.93368f), air_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1812,
        ProximityTerminal.Constructor(Vector3(6093.155f, 5116.275f, 71.39068f), repair_silo),
        owning_building_guid = 32
      )
      LocalObject(
        1813,
        Terminal.Constructor(Vector3(6093.155f, 5116.275f, 71.39068f), ground_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1816,
        ProximityTerminal.Constructor(Vector3(6204.227f, 5190.773f, 71.39068f), repair_silo),
        owning_building_guid = 32
      )
      LocalObject(
        1817,
        Terminal.Constructor(Vector3(6204.227f, 5190.773f, 71.39068f), ground_rearm_terminal),
        owning_building_guid = 32
      )
      LocalObject(
        1302,
        FacilityTurret.Constructor(Vector3(6075.432f, 5097.59f, 80.04268f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1302, 5042)
      LocalObject(
        1303,
        FacilityTurret.Constructor(Vector3(6105.952f, 5044.783f, 80.04268f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1303, 5043)
      LocalObject(
        1304,
        FacilityTurret.Constructor(Vector3(6111.551f, 5227.804f, 80.04268f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1304, 5044)
      LocalObject(
        1305,
        FacilityTurret.Constructor(Vector3(6196.34f, 5021.736f, 80.04268f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1305, 5045)
      LocalObject(
        1306,
        FacilityTurret.Constructor(Vector3(6242.167f, 5192.797f, 80.04268f), manned_turret),
        owning_building_guid = 32
      )
      TurretToWeapon(1306, 5046)
      LocalObject(
        709,
        ImplantTerminalMech.Constructor(Vector3(6122.831f, 5062.913f, 61.11768f)),
        owning_building_guid = 32
      )
      LocalObject(
        703,
        Terminal.Constructor(Vector3(6122.835f, 5062.931f, 61.11768f), implant_terminal_interface),
        owning_building_guid = 32
      )
      TerminalToInterface(709, 703)
      LocalObject(
        710,
        ImplantTerminalMech.Constructor(Vector3(6126.794f, 5077.749f, 61.11768f)),
        owning_building_guid = 32
      )
      LocalObject(
        704,
        Terminal.Constructor(Vector3(6126.789f, 5077.731f, 61.11768f), implant_terminal_interface),
        owning_building_guid = 32
      )
      TerminalToInterface(710, 704)
      LocalObject(
        1638,
        Painbox.Constructor(Vector3(6147.687f, 5138.444f, 85.66948f), painbox),
        owning_building_guid = 32
      )
      LocalObject(
        1648,
        Painbox.Constructor(Vector3(6131.754f, 5114.369f, 65.71058f), painbox_continuous),
        owning_building_guid = 32
      )
      LocalObject(
        1658,
        Painbox.Constructor(Vector3(6145.307f, 5124.107f, 85.87458f), painbox_door_radius),
        owning_building_guid = 32
      )
      LocalObject(
        1686,
        Painbox.Constructor(Vector3(6116.138f, 5106.815f, 65.18158f), painbox_door_radius_continuous),
        owning_building_guid = 32
      )
      LocalObject(
        1687,
        Painbox.Constructor(Vector3(6129.226f, 5096.288f, 63.35488f), painbox_door_radius_continuous),
        owning_building_guid = 32
      )
      LocalObject(
        1688,
        Painbox.Constructor(Vector3(6138.391f, 5122.859f, 63.99658f), painbox_door_radius_continuous),
        owning_building_guid = 32
      )
      LocalObject(229, Generator.Constructor(Vector3(6150.208f, 5141.794f, 80.34668f)), owning_building_guid = 32)
      LocalObject(
        219,
        Terminal.Constructor(Vector3(6148.134f, 5133.869f, 81.64068f), gen_control),
        owning_building_guid = 32
      )
    }

    Building21074()

    def Building21074(): Unit = { // Name: GW_Forseral_N Type: hst GUID: 36, MapID: 21074
      LocalBuilding(
        "GW_Forseral_N",
        36,
        21074,
        FoundationBuilder(WarpGate.Structure(Vector3(4908.22f, 5684.55f, 39.16f), hst))
      )
    }

    Building21078()

    def Building21078(): Unit = { // Name: GW_Forseral_S Type: hst GUID: 37, MapID: 21078
      LocalBuilding(
        "GW_Forseral_S",
        37,
        21078,
        FoundationBuilder(WarpGate.Structure(Vector3(5364.4f, 2560.16f, 44.87f), hst))
      )
    }

    Building11()

    def Building11(): Unit = { // Name: Anu Type: tech_plant GUID: 39, MapID: 11
      LocalBuilding(
        "Anu",
        39,
        11,
        FoundationBuilder(
          Building.Structure(StructureType.Facility, Vector3(3360f, 2538f, 56.18457f), Vector3(0f, 0f, 47f), tech_plant)
        )
      )
      LocalObject(
        157,
        CaptureTerminal.Constructor(Vector3(3395.473f, 2511.394f, 71.28458f), capture_terminal),
        owning_building_guid = 39
      )
      LocalObject(261, Door.Constructor(Vector3(3295.273f, 2500.649f, 65.68958f)), owning_building_guid = 39)
      LocalObject(262, Door.Constructor(Vector3(3308.578f, 2488.242f, 57.72657f)), owning_building_guid = 39)
      LocalObject(263, Door.Constructor(Vector3(3349.206f, 2450.356f, 65.68958f)), owning_building_guid = 39)
      LocalObject(264, Door.Constructor(Vector3(3353.883f, 2577.285f, 57.72657f)), owning_building_guid = 39)
      LocalObject(265, Door.Constructor(Vector3(3362.511f, 2437.949f, 57.72657f)), owning_building_guid = 39)
      LocalObject(266, Door.Constructor(Vector3(3366.29f, 2590.59f, 65.68958f)), owning_building_guid = 39)
      LocalObject(267, Door.Constructor(Vector3(3384.359f, 2529.774f, 72.80557f)), owning_building_guid = 39)
      LocalObject(268, Door.Constructor(Vector3(3400.731f, 2523.869f, 72.80557f)), owning_building_guid = 39)
      LocalObject(269, Door.Constructor(Vector3(3405.855f, 2440.546f, 57.80557f)), owning_building_guid = 39)
      LocalObject(270, Door.Constructor(Vector3(3411.619f, 2597.911f, 57.72657f)), owning_building_guid = 39)
      LocalObject(271, Door.Constructor(Vector3(3417.431f, 2465.865f, 65.79858f)), owning_building_guid = 39)
      LocalObject(272, Door.Constructor(Vector3(3424.925f, 2585.503f, 65.68958f)), owning_building_guid = 39)
      LocalObject(273, Door.Constructor(Vector3(3429.839f, 2479.171f, 57.83558f)), owning_building_guid = 39)
      LocalObject(459, Door.Constructor(Vector3(3298.009f, 2530.176f, 59.92157f)), owning_building_guid = 39)
      LocalObject(461, Door.Constructor(Vector3(3338.966f, 2491.983f, 39.92157f)), owning_building_guid = 39)
      LocalObject(485, Door.Constructor(Vector3(3348.532f, 2461.186f, 47.80557f)), owning_building_guid = 39)
      LocalObject(486, Door.Constructor(Vector3(3352.803f, 2500.957f, 47.80557f)), owning_building_guid = 39)
      LocalObject(487, Door.Constructor(Vector3(3366.084f, 2444.817f, 50.30557f)), owning_building_guid = 39)
      LocalObject(488, Door.Constructor(Vector3(3370.356f, 2484.589f, 47.80557f)), owning_building_guid = 39)
      LocalObject(489, Door.Constructor(Vector3(3374.824f, 2518.707f, 52.80557f)), owning_building_guid = 39)
      LocalObject(490, Door.Constructor(Vector3(3380.478f, 2518.904f, 72.80557f)), owning_building_guid = 39)
      LocalObject(491, Door.Constructor(Vector3(3383.403f, 2516.176f, 62.80557f)), owning_building_guid = 39)
      LocalObject(492, Door.Constructor(Vector3(3386.329f, 2513.448f, 42.80557f)), owning_building_guid = 39)
      LocalObject(493, Door.Constructor(Vector3(3386.526f, 2507.795f, 52.80557f)), owning_building_guid = 39)
      LocalObject(494, Door.Constructor(Vector3(3386.526f, 2507.795f, 72.80557f)), owning_building_guid = 39)
      LocalObject(495, Door.Constructor(Vector3(3387.118f, 2490.834f, 50.30557f)), owning_building_guid = 39)
      LocalObject(496, Door.Constructor(Vector3(3390.798f, 2547.566f, 42.80557f)), owning_building_guid = 39)
      LocalObject(497, Door.Constructor(Vector3(3391.192f, 2536.259f, 50.30557f)), owning_building_guid = 39)
      LocalObject(498, Door.Constructor(Vector3(3402.302f, 2542.307f, 50.30557f)), owning_building_guid = 39)
      LocalObject(499, Door.Constructor(Vector3(3408.745f, 2519.891f, 42.80557f)), owning_building_guid = 39)
      LocalObject(500, Door.Constructor(Vector3(3408.745f, 2519.891f, 50.30557f)), owning_building_guid = 39)
      LocalObject(501, Door.Constructor(Vector3(3414.003f, 2531.396f, 42.80557f)), owning_building_guid = 39)
      LocalObject(502, Door.Constructor(Vector3(3415.583f, 2486.168f, 50.30557f)), owning_building_guid = 39)
      LocalObject(503, Door.Constructor(Vector3(3416.373f, 2463.554f, 50.30557f)), owning_building_guid = 39)
      LocalObject(684, Door.Constructor(Vector3(3420.818f, 2536.271f, 58.56458f)), owning_building_guid = 39)
      LocalObject(1986, Door.Constructor(Vector3(3388.89f, 2528.387f, 50.63857f)), owning_building_guid = 39)
      LocalObject(1987, Door.Constructor(Vector3(3394.221f, 2523.416f, 50.63857f)), owning_building_guid = 39)
      LocalObject(1988, Door.Constructor(Vector3(3399.555f, 2518.442f, 50.63857f)), owning_building_guid = 39)
      LocalObject(
        722,
        IFFLock.Constructor(Vector3(3424.965f, 2536.703f, 57.76458f), Vector3(0, 0, 133)),
        owning_building_guid = 39,
        door_guid = 684
      )
      LocalObject(
        731,
        IFFLock.Constructor(Vector3(3299.874f, 2535.624f, 57.87257f), Vector3(0, 0, 313)),
        owning_building_guid = 39,
        door_guid = 459
      )
      LocalObject(
        754,
        IFFLock.Constructor(Vector3(3382.375f, 2528.831f, 72.73058f), Vector3(0, 0, 313)),
        owning_building_guid = 39,
        door_guid = 267
      )
      LocalObject(
        755,
        IFFLock.Constructor(Vector3(3384.767f, 2507.286f, 72.62057f), Vector3(0, 0, 313)),
        owning_building_guid = 39,
        door_guid = 494
      )
      LocalObject(
        756,
        IFFLock.Constructor(Vector3(3389.528f, 2535.662f, 50.12057f), Vector3(0, 0, 313)),
        owning_building_guid = 39,
        door_guid = 497
      )
      LocalObject(
        757,
        IFFLock.Constructor(Vector3(3392.559f, 2548.073f, 42.62057f), Vector3(0, 0, 133)),
        owning_building_guid = 39,
        door_guid = 496
      )
      LocalObject(
        758,
        IFFLock.Constructor(Vector3(3402.715f, 2524.825f, 72.73058f), Vector3(0, 0, 133)),
        owning_building_guid = 39,
        door_guid = 268
      )
      LocalObject(
        759,
        IFFLock.Constructor(Vector3(3406.797f, 2438.555f, 57.73657f), Vector3(0, 0, 223)),
        owning_building_guid = 39,
        door_guid = 269
      )
      LocalObject(
        760,
        IFFLock.Constructor(Vector3(3410.409f, 2520.489f, 50.12057f), Vector3(0, 0, 133)),
        owning_building_guid = 39,
        door_guid = 500
      )
      LocalObject(
        761,
        IFFLock.Constructor(Vector3(3414.512f, 2529.636f, 42.62057f), Vector3(0, 0, 223)),
        owning_building_guid = 39,
        door_guid = 501
      )
      LocalObject(937, Locker.Constructor(Vector3(3402.215f, 2545.388f, 41.28458f)), owning_building_guid = 39)
      LocalObject(938, Locker.Constructor(Vector3(3403.118f, 2546.357f, 41.28458f)), owning_building_guid = 39)
      LocalObject(939, Locker.Constructor(Vector3(3404.029f, 2547.334f, 41.28458f)), owning_building_guid = 39)
      LocalObject(940, Locker.Constructor(Vector3(3404.941f, 2548.312f, 41.28458f)), owning_building_guid = 39)
      LocalObject(941, Locker.Constructor(Vector3(3408.037f, 2551.632f, 41.28458f)), owning_building_guid = 39)
      LocalObject(942, Locker.Constructor(Vector3(3408.94f, 2552.6f, 41.28458f)), owning_building_guid = 39)
      LocalObject(943, Locker.Constructor(Vector3(3409.609f, 2523.957f, 49.04557f)), owning_building_guid = 39)
      LocalObject(944, Locker.Constructor(Vector3(3409.851f, 2553.577f, 41.28458f)), owning_building_guid = 39)
      LocalObject(945, Locker.Constructor(Vector3(3410.403f, 2524.808f, 49.04557f)), owning_building_guid = 39)
      LocalObject(946, Locker.Constructor(Vector3(3410.763f, 2554.555f, 41.28458f)), owning_building_guid = 39)
      LocalObject(947, Locker.Constructor(Vector3(3411.185f, 2525.647f, 49.04557f)), owning_building_guid = 39)
      LocalObject(948, Locker.Constructor(Vector3(3411.969f, 2526.488f, 49.04557f)), owning_building_guid = 39)
      LocalObject(
        118,
        Terminal.Constructor(Vector3(3381.368f, 2524.464f, 71.88757f), air_vehicle_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1311,
        VehicleSpawnPad.Constructor(Vector3(3363.094f, 2535.211f, 68.76257f), mb_pad_creation, Vector3(0, 0, -47)),
        owning_building_guid = 39,
        terminal_guid = 118
      )
      LocalObject(
        119,
        Terminal.Constructor(Vector3(3389.505f, 2533.19f, 71.88757f), air_vehicle_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1312,
        VehicleSpawnPad.Constructor(Vector3(3377.428f, 2550.582f, 68.76257f), mb_pad_creation, Vector3(0, 0, -47)),
        owning_building_guid = 39,
        terminal_guid = 119
      )
      LocalObject(
        1352,
        Terminal.Constructor(Vector3(3381.477f, 2522.154f, 62.61457f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1353,
        Terminal.Constructor(Vector3(3402.365f, 2534.938f, 50.37457f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1354,
        Terminal.Constructor(Vector3(3405.136f, 2532.354f, 50.37457f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1355,
        Terminal.Constructor(Vector3(3407.865f, 2529.81f, 50.37457f), order_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1917,
        Terminal.Constructor(Vector3(3367.56f, 2550.821f, 68.20757f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1918,
        Terminal.Constructor(Vector3(3390.916f, 2526.905f, 50.91858f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1919,
        Terminal.Constructor(Vector3(3396.244f, 2521.932f, 50.91858f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1920,
        Terminal.Constructor(Vector3(3400.089f, 2511.476f, 42.84158f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1921,
        Terminal.Constructor(Vector3(3400.29f, 2551.351f, 62.86657f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1922,
        Terminal.Constructor(Vector3(3401.579f, 2516.962f, 50.91858f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1923,
        Terminal.Constructor(Vector3(3413.37f, 2455.336f, 50.34158f), spawn_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        2104,
        Terminal.Constructor(Vector3(3356.938f, 2475.219f, 41.99857f), ground_vehicle_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1310,
        VehicleSpawnPad.Constructor(Vector3(3348.919f, 2482.626f, 33.72157f), mb_pad_creation, Vector3(0, 0, -47)),
        owning_building_guid = 39,
        terminal_guid = 2104
      )
      LocalObject(1821, ResourceSilo.Constructor(Vector3(3378.306f, 2605.365f, 63.19257f)), owning_building_guid = 39)
      LocalObject(
        1837,
        SpawnTube.Constructor(Vector3(3389.361f, 2527.347f, 48.78457f), Vector3(0, 0, 313)),
        owning_building_guid = 39
      )
      LocalObject(
        1838,
        SpawnTube.Constructor(Vector3(3394.691f, 2522.377f, 48.78457f), Vector3(0, 0, 313)),
        owning_building_guid = 39
      )
      LocalObject(
        1839,
        SpawnTube.Constructor(Vector3(3400.023f, 2517.405f, 48.78457f), Vector3(0, 0, 313)),
        owning_building_guid = 39
      )
      LocalObject(
        1325,
        ProximityTerminal.Constructor(Vector3(3389.219f, 2514.936f, 61.28157f), medical_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1326,
        ProximityTerminal.Constructor(Vector3(3406.876f, 2549.588f, 41.28458f), medical_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1503,
        ProximityTerminal.Constructor(Vector3(3335.997f, 2476.572f, 64.37958f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1504,
        Terminal.Constructor(Vector3(3335.997f, 2476.572f, 64.37958f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1506,
        ProximityTerminal.Constructor(Vector3(3337.997f, 2502.43f, 66.73457f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1507,
        Terminal.Constructor(Vector3(3337.997f, 2502.43f, 66.73457f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1509,
        ProximityTerminal.Constructor(Vector3(3413.588f, 2500.853f, 71.63158f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1510,
        Terminal.Constructor(Vector3(3413.588f, 2500.853f, 71.63158f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1512,
        ProximityTerminal.Constructor(Vector3(3425.507f, 2570.274f, 64.39258f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1513,
        Terminal.Constructor(Vector3(3425.507f, 2570.274f, 64.39258f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1515,
        ProximityTerminal.Constructor(Vector3(3433.098f, 2554.592f, 66.83658f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1516,
        Terminal.Constructor(Vector3(3433.098f, 2554.592f, 66.83658f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1518,
        ProximityTerminal.Constructor(Vector3(3436.926f, 2501.179f, 64.39258f), pad_landing_frame),
        owning_building_guid = 39
      )
      LocalObject(
        1519,
        Terminal.Constructor(Vector3(3436.926f, 2501.179f, 64.39258f), air_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1748,
        ProximityTerminal.Constructor(Vector3(3329.759f, 2566.622f, 55.91307f), repair_silo),
        owning_building_guid = 39
      )
      LocalObject(
        1749,
        Terminal.Constructor(Vector3(3329.759f, 2566.622f, 55.91307f), ground_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1756,
        ProximityTerminal.Constructor(Vector3(3467.854f, 2517.6f, 55.93457f), repair_silo),
        owning_building_guid = 39
      )
      LocalObject(
        1757,
        Terminal.Constructor(Vector3(3467.854f, 2517.6f, 55.93457f), ground_rearm_terminal),
        owning_building_guid = 39
      )
      LocalObject(
        1234,
        FacilityTurret.Constructor(Vector3(3230.35f, 2543.917f, 64.68357f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1234, 5047)
      LocalObject(
        1235,
        FacilityTurret.Constructor(Vector3(3291.522f, 2609.515f, 64.68357f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1235, 5048)
      LocalObject(
        1236,
        FacilityTurret.Constructor(Vector3(3379.053f, 2412.779f, 64.68357f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1236, 5049)
      LocalObject(
        1237,
        FacilityTurret.Constructor(Vector3(3390.674f, 2627.197f, 64.68357f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1237, 5050)
      LocalObject(
        1239,
        FacilityTurret.Constructor(Vector3(3449.315f, 2583.079f, 64.68357f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1239, 5051)
      LocalObject(
        1241,
        FacilityTurret.Constructor(Vector3(3491.412f, 2533.257f, 64.68357f), manned_turret),
        owning_building_guid = 39
      )
      TurretToWeapon(1241, 5052)
      LocalObject(
        1630,
        Painbox.Constructor(Vector3(3380.327f, 2554.236f, 44.75787f), painbox),
        owning_building_guid = 39
      )
      LocalObject(
        1640,
        Painbox.Constructor(Vector3(3401.112f, 2528.146f, 53.05447f), painbox_continuous),
        owning_building_guid = 39
      )
      LocalObject(
        1650,
        Painbox.Constructor(Vector3(3392.442f, 2545.622f, 44.44397f), painbox_door_radius),
        owning_building_guid = 39
      )
      LocalObject(
        1662,
        Painbox.Constructor(Vector3(3389.804f, 2537.364f, 51.46077f), painbox_door_radius_continuous),
        owning_building_guid = 39
      )
      LocalObject(
        1663,
        Painbox.Constructor(Vector3(3405.831f, 2543.995f, 52.36687f), painbox_door_radius_continuous),
        owning_building_guid = 39
      )
      LocalObject(
        1664,
        Painbox.Constructor(Vector3(3409.346f, 2518.011f, 50.91248f), painbox_door_radius_continuous),
        owning_building_guid = 39
      )
      LocalObject(221, Generator.Constructor(Vector3(3379.404f, 2558.156f, 39.99057f)), owning_building_guid = 39)
      LocalObject(
        211,
        Terminal.Constructor(Vector3(3385.428f, 2552.604f, 41.28458f), gen_control),
        owning_building_guid = 39
      )
    }

    Building8()

    def Building8(): Unit = { // Name: Gwydion Type: tech_plant GUID: 42, MapID: 8
      LocalBuilding(
        "Gwydion",
        42,
        8,
        FoundationBuilder(
          Building.Structure(
            StructureType.Facility,
            Vector3(5548f, 3858f, 60.98056f),
            Vector3(0f, 0f, 316f),
            tech_plant
          )
        )
      )
      LocalObject(
        163,
        CaptureTerminal.Constructor(Vector3(5520.778f, 3822.997f, 76.08056f), capture_terminal),
        owning_building_guid = 42
      )
      LocalObject(401, Door.Constructor(Vector3(5447.92f, 3857.235f, 62.52256f)), owning_building_guid = 42)
      LocalObject(402, Door.Constructor(Vector3(5449.761f, 3813.852f, 62.60156f)), owning_building_guid = 42)
      LocalObject(403, Door.Constructor(Vector3(5460.558f, 3870.322f, 70.48556f)), owning_building_guid = 42)
      LocalObject(404, Door.Constructor(Vector3(5474.874f, 3801.837f, 70.59456f)), owning_building_guid = 42)
      LocalObject(405, Door.Constructor(Vector3(5487.961f, 3789.199f, 62.63156f)), owning_building_guid = 42)
      LocalObject(406, Door.Constructor(Vector3(5499.147f, 3910.282f, 62.52256f)), owning_building_guid = 42)
      LocalObject(407, Door.Constructor(Vector3(5511.785f, 3923.369f, 70.48556f)), owning_building_guid = 42)
      LocalObject(408, Door.Constructor(Vector3(5533.161f, 3817.522f, 77.60156f)), owning_building_guid = 42)
      LocalObject(409, Door.Constructor(Vector3(5539.351f, 3833.788f, 77.60156f)), owning_building_guid = 42)
      LocalObject(410, Door.Constructor(Vector3(5587.385f, 3863.431f, 62.52256f)), owning_building_guid = 42)
      LocalObject(411, Door.Constructor(Vector3(5594.363f, 3792.256f, 70.48556f)), owning_building_guid = 42)
      LocalObject(412, Door.Constructor(Vector3(5600.472f, 3850.793f, 70.48556f)), owning_building_guid = 42)
      LocalObject(413, Door.Constructor(Vector3(5607f, 3805.343f, 62.52256f)), owning_building_guid = 42)
      LocalObject(460, Door.Constructor(Vector3(5541.259f, 3920.119f, 64.71756f)), owning_building_guid = 42)
      LocalObject(462, Door.Constructor(Vector3(5502.357f, 3879.834f, 44.71756f)), owning_building_guid = 42)
      LocalObject(617, Door.Constructor(Vector3(5454.726f, 3853.543f, 55.10156f)), owning_building_guid = 42)
      LocalObject(618, Door.Constructor(Vector3(5471.397f, 3870.807f, 52.60156f)), owning_building_guid = 42)
      LocalObject(619, Door.Constructor(Vector3(5472.582f, 3802.935f, 55.10156f)), owning_building_guid = 42)
      LocalObject(620, Door.Constructor(Vector3(5494.416f, 3848.578f, 52.60156f)), owning_building_guid = 42)
      LocalObject(621, Door.Constructor(Vector3(5495.206f, 3803.33f, 55.10156f)), owning_building_guid = 42)
      LocalObject(622, Door.Constructor(Vector3(5500.368f, 3831.709f, 55.10156f)), owning_building_guid = 42)
      LocalObject(623, Door.Constructor(Vector3(5511.088f, 3865.842f, 52.60156f)), owning_building_guid = 42)
      LocalObject(624, Door.Constructor(Vector3(5517.336f, 3832.005f, 57.60156f)), owning_building_guid = 42)
      LocalObject(625, Door.Constructor(Vector3(5517.336f, 3832.005f, 77.60156f)), owning_building_guid = 42)
      LocalObject(626, Door.Constructor(Vector3(5522.992f, 3832.104f, 47.60156f)), owning_building_guid = 42)
      LocalObject(627, Door.Constructor(Vector3(5525.771f, 3834.981f, 67.60156f)), owning_building_guid = 42)
      LocalObject(628, Door.Constructor(Vector3(5528.451f, 3843.514f, 57.60156f)), owning_building_guid = 42)
      LocalObject(629, Door.Constructor(Vector3(5528.55f, 3837.858f, 77.60156f)), owning_building_guid = 42)
      LocalObject(630, Door.Constructor(Vector3(5529.043f, 3809.579f, 47.60156f)), owning_building_guid = 42)
      LocalObject(631, Door.Constructor(Vector3(5529.043f, 3809.579f, 55.10156f)), owning_building_guid = 42)
      LocalObject(632, Door.Constructor(Vector3(5540.454f, 3804.12f, 47.60156f)), owning_building_guid = 42)
      LocalObject(633, Door.Constructor(Vector3(5545.715f, 3826.843f, 55.10156f)), owning_building_guid = 42)
      LocalObject(634, Door.Constructor(Vector3(5551.568f, 3815.629f, 55.10156f)), owning_building_guid = 42)
      LocalObject(635, Door.Constructor(Vector3(5557.027f, 3827.04f, 47.60156f)), owning_building_guid = 42)
      LocalObject(690, Door.Constructor(Vector3(5545.209f, 3797.221f, 63.36056f)), owning_building_guid = 42)
      LocalObject(2026, Door.Constructor(Vector3(5527.755f, 3818.792f, 55.43456f)), owning_building_guid = 42)
      LocalObject(2027, Door.Constructor(Vector3(5532.821f, 3824.038f, 55.43456f)), owning_building_guid = 42)
      LocalObject(2028, Door.Constructor(Vector3(5537.885f, 3829.282f, 55.43456f)), owning_building_guid = 42)
      LocalObject(
        728,
        IFFLock.Constructor(Vector3(5545.569f, 3793.068f, 62.56056f), Vector3(0, 0, 224)),
        owning_building_guid = 42,
        door_guid = 690
      )
      LocalObject(
        732,
        IFFLock.Constructor(Vector3(5546.673f, 3918.159f, 62.66856f), Vector3(0, 0, 404)),
        owning_building_guid = 42,
        door_guid = 460
      )
      LocalObject(
        864,
        IFFLock.Constructor(Vector3(5447.754f, 3812.946f, 62.53256f), Vector3(0, 0, 314)),
        owning_building_guid = 42,
        door_guid = 402
      )
      LocalObject(
        865,
        IFFLock.Constructor(Vector3(5516.858f, 3833.773f, 77.41656f), Vector3(0, 0, 44)),
        owning_building_guid = 42,
        door_guid = 625
      )
      LocalObject(
        866,
        IFFLock.Constructor(Vector3(5529.611f, 3807.904f, 54.91656f), Vector3(0, 0, 224)),
        owning_building_guid = 42,
        door_guid = 631
      )
      LocalObject(
        867,
        IFFLock.Constructor(Vector3(5534.082f, 3815.521f, 77.52656f), Vector3(0, 0, 224)),
        owning_building_guid = 42,
        door_guid = 408
      )
      LocalObject(
        868,
        IFFLock.Constructor(Vector3(5538.442f, 3835.789f, 77.52656f), Vector3(0, 0, 44)),
        owning_building_guid = 42,
        door_guid = 409
      )
      LocalObject(
        869,
        IFFLock.Constructor(Vector3(5538.686f, 3803.642f, 47.41656f), Vector3(0, 0, 314)),
        owning_building_guid = 42,
        door_guid = 632
      )
      LocalObject(
        870,
        IFFLock.Constructor(Vector3(5545.147f, 3828.517f, 54.91656f), Vector3(0, 0, 44)),
        owning_building_guid = 42,
        door_guid = 633
      )
      LocalObject(
        871,
        IFFLock.Constructor(Vector3(5557.503f, 3825.27f, 47.41656f), Vector3(0, 0, 224)),
        owning_building_guid = 42,
        door_guid = 635
      )
      LocalObject(1115, Locker.Constructor(Vector3(5533.093f, 3808.644f, 53.84156f)), owning_building_guid = 42)
      LocalObject(1116, Locker.Constructor(Vector3(5533.931f, 3807.835f, 53.84156f)), owning_building_guid = 42)
      LocalObject(1117, Locker.Constructor(Vector3(5534.756f, 3807.038f, 53.84156f)), owning_building_guid = 42)
      LocalObject(1118, Locker.Constructor(Vector3(5535.583f, 3806.24f, 53.84156f)), owning_building_guid = 42)
      LocalObject(1119, Locker.Constructor(Vector3(5554.65f, 3815.663f, 46.08056f)), owning_building_guid = 42)
      LocalObject(1120, Locker.Constructor(Vector3(5555.603f, 3814.743f, 46.08056f)), owning_building_guid = 42)
      LocalObject(1121, Locker.Constructor(Vector3(5556.564f, 3813.815f, 46.08056f)), owning_building_guid = 42)
      LocalObject(1122, Locker.Constructor(Vector3(5557.526f, 3812.886f, 46.08056f)), owning_building_guid = 42)
      LocalObject(1123, Locker.Constructor(Vector3(5560.792f, 3809.732f, 46.08056f)), owning_building_guid = 42)
      LocalObject(1124, Locker.Constructor(Vector3(5561.744f, 3808.813f, 46.08056f)), owning_building_guid = 42)
      LocalObject(1125, Locker.Constructor(Vector3(5562.705f, 3807.885f, 46.08056f)), owning_building_guid = 42)
      LocalObject(1126, Locker.Constructor(Vector3(5563.667f, 3806.956f, 46.08056f)), owning_building_guid = 42)
      LocalObject(
        120,
        Terminal.Constructor(Vector3(5534.093f, 3836.872f, 76.68356f), air_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1319,
        VehicleSpawnPad.Constructor(Vector3(5545.157f, 3854.955f, 73.55856f), mb_pad_creation, Vector3(0, 0, 44)),
        owning_building_guid = 42,
        terminal_guid = 120
      )
      LocalObject(
        121,
        Terminal.Constructor(Vector3(5542.676f, 3828.583f, 76.68356f), air_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1320,
        VehicleSpawnPad.Constructor(Vector3(5560.276f, 3840.355f, 73.55856f), mb_pad_creation, Vector3(0, 0, 44)),
        owning_building_guid = 42,
        terminal_guid = 121
      )
      LocalObject(
        1418,
        Terminal.Constructor(Vector3(5531.782f, 3836.803f, 67.41056f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1419,
        Terminal.Constructor(Vector3(5538.976f, 3810.285f, 55.17056f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1420,
        Terminal.Constructor(Vector3(5541.567f, 3812.969f, 55.17056f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1421,
        Terminal.Constructor(Vector3(5544.2f, 3815.695f, 55.17056f), order_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1959,
        Terminal.Constructor(Vector3(5464.417f, 3806.081f, 55.13756f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1960,
        Terminal.Constructor(Vector3(5520.78f, 3818.38f, 47.63756f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1961,
        Terminal.Constructor(Vector3(5526.24f, 3816.794f, 55.71456f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1962,
        Terminal.Constructor(Vector3(5531.302f, 3822.042f, 55.71456f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1963,
        Terminal.Constructor(Vector3(5536.367f, 3827.283f, 55.71456f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1964,
        Terminal.Constructor(Vector3(5560.646f, 3817.483f, 67.66256f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1965,
        Terminal.Constructor(Vector3(5560.687f, 3850.218f, 73.00356f), spawn_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        2110,
        Terminal.Constructor(Vector3(5485.282f, 3862.158f, 46.79456f), ground_vehicle_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1318,
        VehicleSpawnPad.Constructor(Vector3(5492.828f, 3870.045f, 38.51756f), mb_pad_creation, Vector3(0, 0, 44)),
        owning_building_guid = 42,
        terminal_guid = 2110
      )
      LocalObject(1827, ResourceSilo.Constructor(Vector3(5615.035f, 3838.521f, 67.98856f)), owning_building_guid = 42)
      LocalObject(
        1877,
        SpawnTube.Constructor(Vector3(5526.709f, 3818.343f, 53.58056f), Vector3(0, 0, 44)),
        owning_building_guid = 42
      )
      LocalObject(
        1878,
        SpawnTube.Constructor(Vector3(5531.774f, 3823.587f, 53.58056f), Vector3(0, 0, 44)),
        owning_building_guid = 42
      )
      LocalObject(
        1879,
        SpawnTube.Constructor(Vector3(5536.836f, 3828.83f, 53.58056f), Vector3(0, 0, 44)),
        owning_building_guid = 42
      )
      LocalObject(
        1335,
        ProximityTerminal.Constructor(Vector3(5524.429f, 3829.188f, 66.07756f), medical_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1336,
        ProximityTerminal.Constructor(Vector3(5558.768f, 3810.929f, 46.08056f), medical_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1572,
        ProximityTerminal.Constructor(Vector3(5487f, 3883.072f, 69.17556f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1573,
        Terminal.Constructor(Vector3(5487f, 3883.072f, 69.17556f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1575,
        ProximityTerminal.Constructor(Vector3(5509.842f, 3781.729f, 69.18856f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1576,
        Terminal.Constructor(Vector3(5509.842f, 3781.729f, 69.18856f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1578,
        ProximityTerminal.Constructor(Vector3(5509.923f, 3805.069f, 76.42756f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1579,
        Terminal.Constructor(Vector3(5509.923f, 3805.069f, 76.42756f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1581,
        ProximityTerminal.Constructor(Vector3(5512.819f, 3880.62f, 71.53056f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1582,
        Terminal.Constructor(Vector3(5512.819f, 3880.62f, 71.53056f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1584,
        ProximityTerminal.Constructor(Vector3(5563.313f, 3784.624f, 71.63256f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1585,
        Terminal.Constructor(Vector3(5563.313f, 3784.624f, 71.63256f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1587,
        ProximityTerminal.Constructor(Vector3(5579.126f, 3791.939f, 69.18856f), pad_landing_frame),
        owning_building_guid = 42
      )
      LocalObject(
        1588,
        Terminal.Constructor(Vector3(5579.126f, 3791.939f, 69.18856f), air_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1796,
        ProximityTerminal.Constructor(Vector3(5525.721f, 3750.518f, 60.73056f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        1797,
        Terminal.Constructor(Vector3(5525.721f, 3750.518f, 60.73056f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1800,
        ProximityTerminal.Constructor(Vector3(5577.145f, 3887.737f, 60.70906f), repair_silo),
        owning_building_guid = 42
      )
      LocalObject(
        1801,
        Terminal.Constructor(Vector3(5577.145f, 3887.737f, 60.70906f), ground_rearm_terminal),
        owning_building_guid = 42
      )
      LocalObject(
        1285,
        FacilityTurret.Constructor(Vector3(5422.465f, 3841.135f, 69.47956f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1285, 5053)
      LocalObject(
        1287,
        FacilityTurret.Constructor(Vector3(5540.964f, 3726.691f, 69.47956f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1287, 5054)
      LocalObject(
        1288,
        FacilityTurret.Constructor(Vector3(5556.178f, 3987.527f, 69.47956f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1288, 5055)
      LocalObject(
        1289,
        FacilityTurret.Constructor(Vector3(5591.513f, 3767.912f, 69.47956f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1289, 5056)
      LocalObject(
        1290,
        FacilityTurret.Constructor(Vector3(5620.7f, 3925.219f, 69.47956f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1290, 5057)
      LocalObject(
        1291,
        FacilityTurret.Constructor(Vector3(5636.647f, 3825.774f, 69.47956f), manned_turret),
        owning_building_guid = 42
      )
      TurretToWeapon(1291, 5058)
      LocalObject(
        1636,
        Painbox.Constructor(Vector3(5563.878f, 3837.393f, 49.55386f), painbox),
        owning_building_guid = 42
      )
      LocalObject(
        1646,
        Painbox.Constructor(Vector3(5537.431f, 3817.066f, 57.85046f), painbox_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1656,
        Painbox.Constructor(Vector3(5555.054f, 3825.43f, 49.23996f), painbox_door_radius),
        owning_building_guid = 42
      )
      LocalObject(
        1680,
        Painbox.Constructor(Vector3(5527.153f, 3809.01f, 55.70846f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1681,
        Painbox.Constructor(Vector3(5546.843f, 3828.212f, 56.25676f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(
        1682,
        Painbox.Constructor(Vector3(5553.194f, 3812.071f, 57.16286f), painbox_door_radius_continuous),
        owning_building_guid = 42
      )
      LocalObject(227, Generator.Constructor(Vector3(5567.814f, 3838.247f, 44.78656f)), owning_building_guid = 42)
      LocalObject(
        217,
        Terminal.Constructor(Vector3(5562.158f, 3832.321f, 46.08056f), gen_control),
        owning_building_guid = 42
      )
    }

    Building14()

    def Building14(): Unit = { // Name: S_Solsar_Warpgate_Tower Type: tower_a GUID: 45, MapID: 14
      LocalBuilding(
        "S_Solsar_Warpgate_Tower",
        45,
        14,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(1598f, 3414f, 49.07939f), Vector3(0f, 0f, 335f), tower_a)
        )
      )
      LocalObject(
        1894,
        CaptureTerminal.Constructor(Vector3(1612.989f, 3406.897f, 59.07839f), secondary_capture),
        owning_building_guid = 45
      )
      LocalObject(230, Door.Constructor(Vector3(1605.495f, 3401.678f, 50.60039f)), owning_building_guid = 45)
      LocalObject(231, Door.Constructor(Vector3(1605.495f, 3401.678f, 70.5994f)), owning_building_guid = 45)
      LocalObject(232, Door.Constructor(Vector3(1612.257f, 3416.179f, 50.60039f)), owning_building_guid = 45)
      LocalObject(233, Door.Constructor(Vector3(1612.257f, 3416.179f, 70.5994f)), owning_building_guid = 45)
      LocalObject(1979, Door.Constructor(Vector3(1603.366f, 3399.133f, 40.41539f)), owning_building_guid = 45)
      LocalObject(1980, Door.Constructor(Vector3(1610.301f, 3414.006f, 40.41539f)), owning_building_guid = 45)
      LocalObject(
        733,
        IFFLock.Constructor(Vector3(1607.007f, 3400.078f, 50.54039f), Vector3(0, 0, 205)),
        owning_building_guid = 45,
        door_guid = 230
      )
      LocalObject(
        734,
        IFFLock.Constructor(Vector3(1607.007f, 3400.078f, 70.54039f), Vector3(0, 0, 205)),
        owning_building_guid = 45,
        door_guid = 231
      )
      LocalObject(
        735,
        IFFLock.Constructor(Vector3(1610.748f, 3417.778f, 50.54039f), Vector3(0, 0, 25)),
        owning_building_guid = 45,
        door_guid = 232
      )
      LocalObject(
        736,
        IFFLock.Constructor(Vector3(1610.748f, 3417.778f, 70.54039f), Vector3(0, 0, 25)),
        owning_building_guid = 45,
        door_guid = 233
      )
      LocalObject(909, Locker.Constructor(Vector3(1605.889f, 3393.73f, 39.07339f)), owning_building_guid = 45)
      LocalObject(910, Locker.Constructor(Vector3(1607.1f, 3393.165f, 39.07339f)), owning_building_guid = 45)
      LocalObject(911, Locker.Constructor(Vector3(1609.536f, 3392.029f, 39.07339f)), owning_building_guid = 45)
      LocalObject(912, Locker.Constructor(Vector3(1610.807f, 3391.437f, 39.07339f)), owning_building_guid = 45)
      LocalObject(913, Locker.Constructor(Vector3(1615.164f, 3413.538f, 39.07339f)), owning_building_guid = 45)
      LocalObject(914, Locker.Constructor(Vector3(1616.376f, 3412.973f, 39.07339f)), owning_building_guid = 45)
      LocalObject(915, Locker.Constructor(Vector3(1618.78f, 3411.852f, 39.07339f)), owning_building_guid = 45)
      LocalObject(916, Locker.Constructor(Vector3(1620.051f, 3411.259f, 39.07339f)), owning_building_guid = 45)
      LocalObject(
        1340,
        Terminal.Constructor(Vector3(1613.264f, 3395.991f, 40.41139f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1341,
        Terminal.Constructor(Vector3(1615.683f, 3401.178f, 40.41139f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1342,
        Terminal.Constructor(Vector3(1617.957f, 3406.055f, 40.41139f), order_terminal),
        owning_building_guid = 45
      )
      LocalObject(
        1830,
        SpawnTube.Constructor(Vector3(1602.522f, 3398.366f, 38.56139f), respawn_tube_tower, Vector3(0, 0, 25)),
        owning_building_guid = 45
      )
      LocalObject(
        1831,
        SpawnTube.Constructor(Vector3(1609.458f, 3413.239f, 38.56139f), respawn_tube_tower, Vector3(0, 0, 25)),
        owning_building_guid = 45
      )
      LocalObject(
        1226,
        FacilityTurret.Constructor(Vector3(1581.139f, 3407.844f, 68.02139f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1226, 5059)
      LocalObject(
        1227,
        FacilityTurret.Constructor(Vector3(1623.895f, 3415.945f, 68.02139f), manned_turret),
        owning_building_guid = 45
      )
      TurretToWeapon(1227, 5060)
      LocalObject(
        1689,
        Painbox.Constructor(Vector3(1600.126f, 3406.171f, 40.57849f), painbox_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        1690,
        Painbox.Constructor(Vector3(1609.253f, 3397.965f, 39.17939f), painbox_radius_continuous),
        owning_building_guid = 45
      )
      LocalObject(
        1691,
        Painbox.Constructor(Vector3(1614.188f, 3408.753f, 39.17939f), painbox_radius_continuous),
        owning_building_guid = 45
      )
    }

    Building17()

    def Building17(): Unit = { // Name: NE_TRSanc_Warpgate_Tower Type: tower_a GUID: 46, MapID: 17
      LocalBuilding(
        "NE_TRSanc_Warpgate_Tower",
        46,
        17,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3612f, 1746f, 51.49541f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        1898,
        CaptureTerminal.Constructor(Vector3(3628.587f, 1745.897f, 61.49441f), secondary_capture),
        owning_building_guid = 46
      )
      LocalObject(305, Door.Constructor(Vector3(3624f, 1738f, 53.01641f)), owning_building_guid = 46)
      LocalObject(306, Door.Constructor(Vector3(3624f, 1738f, 73.01541f)), owning_building_guid = 46)
      LocalObject(307, Door.Constructor(Vector3(3624f, 1754f, 53.01641f)), owning_building_guid = 46)
      LocalObject(308, Door.Constructor(Vector3(3624f, 1754f, 73.01541f)), owning_building_guid = 46)
      LocalObject(1996, Door.Constructor(Vector3(3623.146f, 1734.794f, 42.83141f)), owning_building_guid = 46)
      LocalObject(1997, Door.Constructor(Vector3(3623.146f, 1751.204f, 42.83141f)), owning_building_guid = 46)
      LocalObject(
        784,
        IFFLock.Constructor(Vector3(3621.957f, 1754.811f, 52.95641f), Vector3(0, 0, 0)),
        owning_building_guid = 46,
        door_guid = 307
      )
      LocalObject(
        785,
        IFFLock.Constructor(Vector3(3621.957f, 1754.811f, 72.95641f), Vector3(0, 0, 0)),
        owning_building_guid = 46,
        door_guid = 308
      )
      LocalObject(
        786,
        IFFLock.Constructor(Vector3(3626.047f, 1737.189f, 52.95641f), Vector3(0, 0, 180)),
        owning_building_guid = 46,
        door_guid = 305
      )
      LocalObject(
        787,
        IFFLock.Constructor(Vector3(3626.047f, 1737.189f, 72.95641f), Vector3(0, 0, 180)),
        owning_building_guid = 46,
        door_guid = 306
      )
      LocalObject(986, Locker.Constructor(Vector3(3627.716f, 1730.963f, 41.48941f)), owning_building_guid = 46)
      LocalObject(987, Locker.Constructor(Vector3(3627.751f, 1752.835f, 41.48941f)), owning_building_guid = 46)
      LocalObject(988, Locker.Constructor(Vector3(3629.053f, 1730.963f, 41.48941f)), owning_building_guid = 46)
      LocalObject(989, Locker.Constructor(Vector3(3629.088f, 1752.835f, 41.48941f)), owning_building_guid = 46)
      LocalObject(990, Locker.Constructor(Vector3(3631.741f, 1730.963f, 41.48941f)), owning_building_guid = 46)
      LocalObject(991, Locker.Constructor(Vector3(3631.741f, 1752.835f, 41.48941f)), owning_building_guid = 46)
      LocalObject(992, Locker.Constructor(Vector3(3633.143f, 1730.963f, 41.48941f)), owning_building_guid = 46)
      LocalObject(993, Locker.Constructor(Vector3(3633.143f, 1752.835f, 41.48941f)), owning_building_guid = 46)
      LocalObject(
        1371,
        Terminal.Constructor(Vector3(3633.445f, 1736.129f, 42.82741f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1372,
        Terminal.Constructor(Vector3(3633.445f, 1741.853f, 42.82741f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1373,
        Terminal.Constructor(Vector3(3633.445f, 1747.234f, 42.82741f), order_terminal),
        owning_building_guid = 46
      )
      LocalObject(
        1847,
        SpawnTube.Constructor(Vector3(3622.706f, 1733.742f, 40.97741f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 46
      )
      LocalObject(
        1848,
        SpawnTube.Constructor(Vector3(3622.706f, 1750.152f, 40.97741f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 46
      )
      LocalObject(
        1248,
        FacilityTurret.Constructor(Vector3(3599.32f, 1733.295f, 70.43741f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1248, 5061)
      LocalObject(
        1251,
        FacilityTurret.Constructor(Vector3(3634.647f, 1758.707f, 70.43741f), manned_turret),
        owning_building_guid = 46
      )
      TurretToWeapon(1251, 5062)
      LocalObject(
        1701,
        Painbox.Constructor(Vector3(3617.235f, 1739.803f, 42.99451f), painbox_radius_continuous),
        owning_building_guid = 46
      )
      LocalObject(
        1702,
        Painbox.Constructor(Vector3(3628.889f, 1748.086f, 41.59541f), painbox_radius_continuous),
        owning_building_guid = 46
      )
      LocalObject(
        1703,
        Painbox.Constructor(Vector3(3628.975f, 1736.223f, 41.59541f), painbox_radius_continuous),
        owning_building_guid = 46
      )
    }

    Building37()

    def Building37(): Unit = { // Name: N_Ogma_Tower Type: tower_a GUID: 47, MapID: 37
      LocalBuilding(
        "N_Ogma_Tower",
        47,
        37,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3662f, 3532f, 101.2391f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        1899,
        CaptureTerminal.Constructor(Vector3(3678.587f, 3531.897f, 111.2381f), secondary_capture),
        owning_building_guid = 47
      )
      LocalObject(313, Door.Constructor(Vector3(3674f, 3524f, 102.7601f)), owning_building_guid = 47)
      LocalObject(314, Door.Constructor(Vector3(3674f, 3524f, 122.759f)), owning_building_guid = 47)
      LocalObject(315, Door.Constructor(Vector3(3674f, 3540f, 102.7601f)), owning_building_guid = 47)
      LocalObject(316, Door.Constructor(Vector3(3674f, 3540f, 122.759f)), owning_building_guid = 47)
      LocalObject(2001, Door.Constructor(Vector3(3673.146f, 3520.794f, 92.57505f)), owning_building_guid = 47)
      LocalObject(2002, Door.Constructor(Vector3(3673.146f, 3537.204f, 92.57505f)), owning_building_guid = 47)
      LocalObject(
        794,
        IFFLock.Constructor(Vector3(3671.957f, 3540.811f, 102.7001f), Vector3(0, 0, 0)),
        owning_building_guid = 47,
        door_guid = 315
      )
      LocalObject(
        795,
        IFFLock.Constructor(Vector3(3671.957f, 3540.811f, 122.7001f), Vector3(0, 0, 0)),
        owning_building_guid = 47,
        door_guid = 316
      )
      LocalObject(
        796,
        IFFLock.Constructor(Vector3(3676.047f, 3523.189f, 102.7001f), Vector3(0, 0, 180)),
        owning_building_guid = 47,
        door_guid = 313
      )
      LocalObject(
        797,
        IFFLock.Constructor(Vector3(3676.047f, 3523.189f, 122.7001f), Vector3(0, 0, 180)),
        owning_building_guid = 47,
        door_guid = 314
      )
      LocalObject(1002, Locker.Constructor(Vector3(3677.716f, 3516.963f, 91.23306f)), owning_building_guid = 47)
      LocalObject(1003, Locker.Constructor(Vector3(3677.751f, 3538.835f, 91.23306f)), owning_building_guid = 47)
      LocalObject(1005, Locker.Constructor(Vector3(3679.053f, 3516.963f, 91.23306f)), owning_building_guid = 47)
      LocalObject(1006, Locker.Constructor(Vector3(3679.088f, 3538.835f, 91.23306f)), owning_building_guid = 47)
      LocalObject(1009, Locker.Constructor(Vector3(3681.741f, 3516.963f, 91.23306f)), owning_building_guid = 47)
      LocalObject(1010, Locker.Constructor(Vector3(3681.741f, 3538.835f, 91.23306f)), owning_building_guid = 47)
      LocalObject(1012, Locker.Constructor(Vector3(3683.143f, 3516.963f, 91.23306f)), owning_building_guid = 47)
      LocalObject(1013, Locker.Constructor(Vector3(3683.143f, 3538.835f, 91.23306f)), owning_building_guid = 47)
      LocalObject(
        1377,
        Terminal.Constructor(Vector3(3683.445f, 3522.129f, 92.57105f), order_terminal),
        owning_building_guid = 47
      )
      LocalObject(
        1378,
        Terminal.Constructor(Vector3(3683.445f, 3527.853f, 92.57105f), order_terminal),
        owning_building_guid = 47
      )
      LocalObject(
        1379,
        Terminal.Constructor(Vector3(3683.445f, 3533.234f, 92.57105f), order_terminal),
        owning_building_guid = 47
      )
      LocalObject(
        1852,
        SpawnTube.Constructor(Vector3(3672.706f, 3519.742f, 90.72105f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 47
      )
      LocalObject(
        1853,
        SpawnTube.Constructor(Vector3(3672.706f, 3536.152f, 90.72105f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 47
      )
      LocalObject(
        1254,
        FacilityTurret.Constructor(Vector3(3649.32f, 3519.295f, 120.1811f), manned_turret),
        owning_building_guid = 47
      )
      TurretToWeapon(1254, 5063)
      LocalObject(
        1257,
        FacilityTurret.Constructor(Vector3(3684.647f, 3544.707f, 120.1811f), manned_turret),
        owning_building_guid = 47
      )
      TurretToWeapon(1257, 5064)
      LocalObject(
        1704,
        Painbox.Constructor(Vector3(3667.235f, 3525.803f, 92.73815f), painbox_radius_continuous),
        owning_building_guid = 47
      )
      LocalObject(
        1705,
        Painbox.Constructor(Vector3(3678.889f, 3534.086f, 91.33905f), painbox_radius_continuous),
        owning_building_guid = 47
      )
      LocalObject(
        1706,
        Painbox.Constructor(Vector3(3678.975f, 3522.223f, 91.33905f), painbox_radius_continuous),
        owning_building_guid = 47
      )
    }

    Building27()

    def Building27(): Unit = { // Name: S_Bel_Tower Type: tower_a GUID: 48, MapID: 27
      LocalBuilding(
        "S_Bel_Tower",
        48,
        27,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3694f, 4482f, 48.92117f), Vector3(0f, 0f, 304f), tower_a)
        )
      )
      LocalObject(
        1900,
        CaptureTerminal.Constructor(Vector3(3703.19f, 4468.191f, 58.92017f), secondary_capture),
        owning_building_guid = 48
      )
      LocalObject(319, Door.Constructor(Vector3(3694.078f, 4467.578f, 50.44217f)), owning_building_guid = 48)
      LocalObject(320, Door.Constructor(Vector3(3694.078f, 4467.578f, 70.44117f)), owning_building_guid = 48)
      LocalObject(322, Door.Constructor(Vector3(3707.343f, 4476.525f, 50.44217f)), owning_building_guid = 48)
      LocalObject(323, Door.Constructor(Vector3(3707.343f, 4476.525f, 70.44117f)), owning_building_guid = 48)
      LocalObject(2003, Door.Constructor(Vector3(3690.943f, 4466.493f, 40.25717f)), owning_building_guid = 48)
      LocalObject(2004, Door.Constructor(Vector3(3704.547f, 4475.669f, 40.25717f)), owning_building_guid = 48)
      LocalObject(
        801,
        IFFLock.Constructor(Vector3(3694.55f, 4465.427f, 50.38217f), Vector3(0, 0, 236)),
        owning_building_guid = 48,
        door_guid = 319
      )
      LocalObject(
        802,
        IFFLock.Constructor(Vector3(3694.55f, 4465.427f, 70.38217f), Vector3(0, 0, 236)),
        owning_building_guid = 48,
        door_guid = 320
      )
      LocalObject(
        803,
        IFFLock.Constructor(Vector3(3706.873f, 4478.672f, 50.38217f), Vector3(0, 0, 56)),
        owning_building_guid = 48,
        door_guid = 322
      )
      LocalObject(
        804,
        IFFLock.Constructor(Vector3(3706.873f, 4478.672f, 70.38217f), Vector3(0, 0, 56)),
        owning_building_guid = 48,
        door_guid = 323
      )
      LocalObject(1014, Locker.Constructor(Vector3(3690.322f, 4460.562f, 38.91517f)), owning_building_guid = 48)
      LocalObject(1015, Locker.Constructor(Vector3(3691.07f, 4459.454f, 38.91517f)), owning_building_guid = 48)
      LocalObject(1016, Locker.Constructor(Vector3(3692.573f, 4457.226f, 38.91517f)), owning_building_guid = 48)
      LocalObject(1017, Locker.Constructor(Vector3(3693.357f, 4456.063f, 38.91517f)), owning_building_guid = 48)
      LocalObject(1018, Locker.Constructor(Vector3(3708.474f, 4472.764f, 38.91517f)), owning_building_guid = 48)
      LocalObject(1019, Locker.Constructor(Vector3(3709.222f, 4471.655f, 38.91517f)), owning_building_guid = 48)
      LocalObject(1020, Locker.Constructor(Vector3(3710.706f, 4469.456f, 38.91517f)), owning_building_guid = 48)
      LocalObject(1021, Locker.Constructor(Vector3(3711.49f, 4468.294f, 38.91517f)), owning_building_guid = 48)
      LocalObject(
        1380,
        Terminal.Constructor(Vector3(3697.808f, 4458.702f, 40.25317f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1381,
        Terminal.Constructor(Vector3(3702.554f, 4461.902f, 40.25317f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1382,
        Terminal.Constructor(Vector3(3707.015f, 4464.911f, 40.25317f), order_terminal),
        owning_building_guid = 48
      )
      LocalObject(
        1854,
        SpawnTube.Constructor(Vector3(3689.824f, 4466.27f, 38.40317f), respawn_tube_tower, Vector3(0, 0, 56)),
        owning_building_guid = 48
      )
      LocalObject(
        1855,
        SpawnTube.Constructor(Vector3(3703.429f, 4475.446f, 38.40317f), respawn_tube_tower, Vector3(0, 0, 56)),
        owning_building_guid = 48
      )
      LocalObject(
        1255,
        FacilityTurret.Constructor(Vector3(3676.376f, 4485.408f, 67.86317f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1255, 5065)
      LocalObject(
        1258,
        FacilityTurret.Constructor(Vector3(3717.199f, 4470.331f, 67.86317f), manned_turret),
        owning_building_guid = 48
      )
      TurretToWeapon(1258, 5066)
      LocalObject(
        1707,
        Painbox.Constructor(Vector3(3691.79f, 4474.195f, 40.42027f), painbox_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        1708,
        Painbox.Constructor(Vector3(3695.386f, 4462.459f, 39.02117f), painbox_radius_continuous),
        owning_building_guid = 48
      )
      LocalObject(
        1709,
        Painbox.Constructor(Vector3(3705.174f, 4469.165f, 39.02117f), painbox_radius_continuous),
        owning_building_guid = 48
      )
    }

    Building25()

    def Building25(): Unit = { // Name: SE_Ceryshen_Warpgate_Tower Type: tower_a GUID: 49, MapID: 25
      LocalBuilding(
        "SE_Ceryshen_Warpgate_Tower",
        49,
        25,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3832f, 6326f, 50.13886f), Vector3(0f, 0f, 314f), tower_a)
        )
      )
      LocalObject(
        1901,
        CaptureTerminal.Constructor(Vector3(3843.448f, 6313.997f, 60.13786f), secondary_capture),
        owning_building_guid = 49
      )
      LocalObject(328, Door.Constructor(Vector3(3834.581f, 6311.811f, 51.65986f)), owning_building_guid = 49)
      LocalObject(329, Door.Constructor(Vector3(3834.581f, 6311.811f, 71.65886f)), owning_building_guid = 49)
      LocalObject(330, Door.Constructor(Vector3(3846.091f, 6322.925f, 51.65986f)), owning_building_guid = 49)
      LocalObject(331, Door.Constructor(Vector3(3846.091f, 6322.925f, 71.65886f)), owning_building_guid = 49)
      LocalObject(2005, Door.Constructor(Vector3(3831.682f, 6310.198f, 41.47486f)), owning_building_guid = 49)
      LocalObject(2006, Door.Constructor(Vector3(3843.486f, 6321.597f, 41.47486f)), owning_building_guid = 49)
      LocalObject(
        806,
        IFFLock.Constructor(Vector3(3835.42f, 6309.775f, 51.59986f), Vector3(0, 0, 226)),
        owning_building_guid = 49,
        door_guid = 328
      )
      LocalObject(
        807,
        IFFLock.Constructor(Vector3(3835.42f, 6309.775f, 71.59986f), Vector3(0, 0, 226)),
        owning_building_guid = 49,
        door_guid = 329
      )
      LocalObject(
        808,
        IFFLock.Constructor(Vector3(3845.255f, 6324.958f, 51.59986f), Vector3(0, 0, 46)),
        owning_building_guid = 49,
        door_guid = 330
      )
      LocalObject(
        809,
        IFFLock.Constructor(Vector3(3845.255f, 6324.958f, 71.59986f), Vector3(0, 0, 46)),
        owning_building_guid = 49,
        door_guid = 331
      )
      LocalObject(1022, Locker.Constructor(Vector3(3832.101f, 6304.25f, 40.13286f)), owning_building_guid = 49)
      LocalObject(1023, Locker.Constructor(Vector3(3833.029f, 6303.288f, 40.13286f)), owning_building_guid = 49)
      LocalObject(1024, Locker.Constructor(Vector3(3834.896f, 6301.354f, 40.13286f)), owning_building_guid = 49)
      LocalObject(1025, Locker.Constructor(Vector3(3835.87f, 6300.345f, 40.13286f)), owning_building_guid = 49)
      LocalObject(1026, Locker.Constructor(Vector3(3847.858f, 6319.417f, 40.13286f)), owning_building_guid = 49)
      LocalObject(1027, Locker.Constructor(Vector3(3848.787f, 6318.456f, 40.13286f)), owning_building_guid = 49)
      LocalObject(1028, Locker.Constructor(Vector3(3850.63f, 6316.547f, 40.13286f)), owning_building_guid = 49)
      LocalObject(1029, Locker.Constructor(Vector3(3851.604f, 6315.539f, 40.13286f)), owning_building_guid = 49)
      LocalObject(
        1383,
        Terminal.Constructor(Vector3(3839.796f, 6303.717f, 41.47086f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1384,
        Terminal.Constructor(Vector3(3843.914f, 6307.693f, 41.47086f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1385,
        Terminal.Constructor(Vector3(3847.785f, 6311.431f, 41.47086f), order_terminal),
        owning_building_guid = 49
      )
      LocalObject(
        1856,
        SpawnTube.Constructor(Vector3(3830.619f, 6309.784f, 39.62086f), respawn_tube_tower, Vector3(0, 0, 46)),
        owning_building_guid = 49
      )
      LocalObject(
        1857,
        SpawnTube.Constructor(Vector3(3842.424f, 6321.183f, 39.62086f), respawn_tube_tower, Vector3(0, 0, 46)),
        owning_building_guid = 49
      )
      LocalObject(
        1261,
        FacilityTurret.Constructor(Vector3(3814.052f, 6326.295f, 69.08086f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1261, 5067)
      LocalObject(
        1262,
        FacilityTurret.Constructor(Vector3(3856.873f, 6318.536f, 69.08086f), manned_turret),
        owning_building_guid = 49
      )
      TurretToWeapon(1262, 5068)
      LocalObject(
        1710,
        Painbox.Constructor(Vector3(3831.179f, 6317.93f, 41.63796f), painbox_radius_continuous),
        owning_building_guid = 49
      )
      LocalObject(
        1711,
        Painbox.Constructor(Vector3(3836.759f, 6306.997f, 40.23886f), painbox_radius_continuous),
        owning_building_guid = 49
      )
      LocalObject(
        1712,
        Painbox.Constructor(Vector3(3845.233f, 6315.3f, 40.23886f), painbox_radius_continuous),
        owning_building_guid = 49
      )
    }

    Building39()

    def Building39(): Unit = { // Name: W_Pwyll_Tower Type: tower_a GUID: 50, MapID: 39
      LocalBuilding(
        "W_Pwyll_Tower",
        50,
        39,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4486f, 4862f, 90.87802f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        1903,
        CaptureTerminal.Constructor(Vector3(4502.587f, 4861.897f, 100.877f), secondary_capture),
        owning_building_guid = 50
      )
      LocalObject(352, Door.Constructor(Vector3(4498f, 4854f, 92.39902f)), owning_building_guid = 50)
      LocalObject(353, Door.Constructor(Vector3(4498f, 4854f, 112.398f)), owning_building_guid = 50)
      LocalObject(354, Door.Constructor(Vector3(4498f, 4870f, 92.39902f)), owning_building_guid = 50)
      LocalObject(355, Door.Constructor(Vector3(4498f, 4870f, 112.398f)), owning_building_guid = 50)
      LocalObject(2012, Door.Constructor(Vector3(4497.146f, 4850.794f, 82.21402f)), owning_building_guid = 50)
      LocalObject(2013, Door.Constructor(Vector3(4497.146f, 4867.204f, 82.21402f)), owning_building_guid = 50)
      LocalObject(
        824,
        IFFLock.Constructor(Vector3(4495.957f, 4870.811f, 92.33902f), Vector3(0, 0, 0)),
        owning_building_guid = 50,
        door_guid = 354
      )
      LocalObject(
        825,
        IFFLock.Constructor(Vector3(4495.957f, 4870.811f, 112.339f), Vector3(0, 0, 0)),
        owning_building_guid = 50,
        door_guid = 355
      )
      LocalObject(
        826,
        IFFLock.Constructor(Vector3(4500.047f, 4853.189f, 92.33902f), Vector3(0, 0, 180)),
        owning_building_guid = 50,
        door_guid = 352
      )
      LocalObject(
        827,
        IFFLock.Constructor(Vector3(4500.047f, 4853.189f, 112.339f), Vector3(0, 0, 180)),
        owning_building_guid = 50,
        door_guid = 353
      )
      LocalObject(1059, Locker.Constructor(Vector3(4501.716f, 4846.963f, 80.87202f)), owning_building_guid = 50)
      LocalObject(1060, Locker.Constructor(Vector3(4501.751f, 4868.835f, 80.87202f)), owning_building_guid = 50)
      LocalObject(1061, Locker.Constructor(Vector3(4503.053f, 4846.963f, 80.87202f)), owning_building_guid = 50)
      LocalObject(1062, Locker.Constructor(Vector3(4503.088f, 4868.835f, 80.87202f)), owning_building_guid = 50)
      LocalObject(1063, Locker.Constructor(Vector3(4505.741f, 4846.963f, 80.87202f)), owning_building_guid = 50)
      LocalObject(1064, Locker.Constructor(Vector3(4505.741f, 4868.835f, 80.87202f)), owning_building_guid = 50)
      LocalObject(1065, Locker.Constructor(Vector3(4507.143f, 4846.963f, 80.87202f)), owning_building_guid = 50)
      LocalObject(1066, Locker.Constructor(Vector3(4507.143f, 4868.835f, 80.87202f)), owning_building_guid = 50)
      LocalObject(
        1393,
        Terminal.Constructor(Vector3(4507.445f, 4852.129f, 82.21002f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1394,
        Terminal.Constructor(Vector3(4507.445f, 4857.853f, 82.21002f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1395,
        Terminal.Constructor(Vector3(4507.445f, 4863.234f, 82.21002f), order_terminal),
        owning_building_guid = 50
      )
      LocalObject(
        1863,
        SpawnTube.Constructor(Vector3(4496.706f, 4849.742f, 80.36002f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 50
      )
      LocalObject(
        1864,
        SpawnTube.Constructor(Vector3(4496.706f, 4866.152f, 80.36002f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 50
      )
      LocalObject(
        1268,
        FacilityTurret.Constructor(Vector3(4473.32f, 4849.295f, 109.82f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1268, 5069)
      LocalObject(
        1269,
        FacilityTurret.Constructor(Vector3(4508.647f, 4874.707f, 109.82f), manned_turret),
        owning_building_guid = 50
      )
      TurretToWeapon(1269, 5070)
      LocalObject(
        1716,
        Painbox.Constructor(Vector3(4491.235f, 4855.803f, 82.37712f), painbox_radius_continuous),
        owning_building_guid = 50
      )
      LocalObject(
        1717,
        Painbox.Constructor(Vector3(4502.889f, 4864.086f, 80.97802f), painbox_radius_continuous),
        owning_building_guid = 50
      )
      LocalObject(
        1718,
        Painbox.Constructor(Vector3(4502.975f, 4852.223f, 80.97802f), painbox_radius_continuous),
        owning_building_guid = 50
      )
    }

    Building20()

    def Building20(): Unit = { // Name: NW_Gwydion_Tower Type: tower_a GUID: 51, MapID: 20
      LocalBuilding(
        "NW_Gwydion_Tower",
        51,
        20,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5410f, 4044f, 59.84832f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        1906,
        CaptureTerminal.Constructor(Vector3(5426.587f, 4043.897f, 69.84732f), secondary_capture),
        owning_building_guid = 51
      )
      LocalObject(397, Door.Constructor(Vector3(5422f, 4036f, 61.36932f)), owning_building_guid = 51)
      LocalObject(398, Door.Constructor(Vector3(5422f, 4036f, 81.36832f)), owning_building_guid = 51)
      LocalObject(399, Door.Constructor(Vector3(5422f, 4052f, 61.36932f)), owning_building_guid = 51)
      LocalObject(400, Door.Constructor(Vector3(5422f, 4052f, 81.36832f)), owning_building_guid = 51)
      LocalObject(2024, Door.Constructor(Vector3(5421.146f, 4032.794f, 51.18433f)), owning_building_guid = 51)
      LocalObject(2025, Door.Constructor(Vector3(5421.146f, 4049.204f, 51.18433f)), owning_building_guid = 51)
      LocalObject(
        860,
        IFFLock.Constructor(Vector3(5419.957f, 4052.811f, 61.30932f), Vector3(0, 0, 0)),
        owning_building_guid = 51,
        door_guid = 399
      )
      LocalObject(
        861,
        IFFLock.Constructor(Vector3(5419.957f, 4052.811f, 81.30933f), Vector3(0, 0, 0)),
        owning_building_guid = 51,
        door_guid = 400
      )
      LocalObject(
        862,
        IFFLock.Constructor(Vector3(5424.047f, 4035.189f, 61.30932f), Vector3(0, 0, 180)),
        owning_building_guid = 51,
        door_guid = 397
      )
      LocalObject(
        863,
        IFFLock.Constructor(Vector3(5424.047f, 4035.189f, 81.30933f), Vector3(0, 0, 180)),
        owning_building_guid = 51,
        door_guid = 398
      )
      LocalObject(1107, Locker.Constructor(Vector3(5425.716f, 4028.963f, 49.84232f)), owning_building_guid = 51)
      LocalObject(1108, Locker.Constructor(Vector3(5425.751f, 4050.835f, 49.84232f)), owning_building_guid = 51)
      LocalObject(1109, Locker.Constructor(Vector3(5427.053f, 4028.963f, 49.84232f)), owning_building_guid = 51)
      LocalObject(1110, Locker.Constructor(Vector3(5427.088f, 4050.835f, 49.84232f)), owning_building_guid = 51)
      LocalObject(1111, Locker.Constructor(Vector3(5429.741f, 4028.963f, 49.84232f)), owning_building_guid = 51)
      LocalObject(1112, Locker.Constructor(Vector3(5429.741f, 4050.835f, 49.84232f)), owning_building_guid = 51)
      LocalObject(1113, Locker.Constructor(Vector3(5431.143f, 4028.963f, 49.84232f)), owning_building_guid = 51)
      LocalObject(1114, Locker.Constructor(Vector3(5431.143f, 4050.835f, 49.84232f)), owning_building_guid = 51)
      LocalObject(
        1415,
        Terminal.Constructor(Vector3(5431.445f, 4034.129f, 51.18032f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1416,
        Terminal.Constructor(Vector3(5431.445f, 4039.853f, 51.18032f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1417,
        Terminal.Constructor(Vector3(5431.445f, 4045.234f, 51.18032f), order_terminal),
        owning_building_guid = 51
      )
      LocalObject(
        1875,
        SpawnTube.Constructor(Vector3(5420.706f, 4031.742f, 49.33032f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 51
      )
      LocalObject(
        1876,
        SpawnTube.Constructor(Vector3(5420.706f, 4048.152f, 49.33032f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 51
      )
      LocalObject(
        1284,
        FacilityTurret.Constructor(Vector3(5397.32f, 4031.295f, 78.79032f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1284, 5071)
      LocalObject(
        1286,
        FacilityTurret.Constructor(Vector3(5432.647f, 4056.707f, 78.79032f), manned_turret),
        owning_building_guid = 51
      )
      TurretToWeapon(1286, 5072)
      LocalObject(
        1725,
        Painbox.Constructor(Vector3(5415.235f, 4037.803f, 51.34742f), painbox_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        1726,
        Painbox.Constructor(Vector3(5426.889f, 4046.086f, 49.94833f), painbox_radius_continuous),
        owning_building_guid = 51
      )
      LocalObject(
        1727,
        Painbox.Constructor(Vector3(5426.975f, 4034.223f, 49.94833f), painbox_radius_continuous),
        owning_building_guid = 51
      )
    }

    Building22()

    def Building22(): Unit = { // Name: NW_Lugh_Tower Type: tower_a GUID: 52, MapID: 22
      LocalBuilding(
        "NW_Lugh_Tower",
        52,
        22,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5940f, 5298f, 87.00064f), Vector3(0f, 0f, 360f), tower_a)
        )
      )
      LocalObject(
        1908,
        CaptureTerminal.Constructor(Vector3(5956.587f, 5297.897f, 96.99964f), secondary_capture),
        owning_building_guid = 52
      )
      LocalObject(435, Door.Constructor(Vector3(5952f, 5290f, 88.52164f)), owning_building_guid = 52)
      LocalObject(436, Door.Constructor(Vector3(5952f, 5290f, 108.5206f)), owning_building_guid = 52)
      LocalObject(437, Door.Constructor(Vector3(5952f, 5306f, 88.52164f)), owning_building_guid = 52)
      LocalObject(438, Door.Constructor(Vector3(5952f, 5306f, 108.5206f)), owning_building_guid = 52)
      LocalObject(2034, Door.Constructor(Vector3(5951.146f, 5286.794f, 78.33664f)), owning_building_guid = 52)
      LocalObject(2035, Door.Constructor(Vector3(5951.146f, 5303.204f, 78.33664f)), owning_building_guid = 52)
      LocalObject(
        889,
        IFFLock.Constructor(Vector3(5949.957f, 5306.811f, 88.46164f), Vector3(0, 0, 0)),
        owning_building_guid = 52,
        door_guid = 437
      )
      LocalObject(
        890,
        IFFLock.Constructor(Vector3(5949.957f, 5306.811f, 108.4616f), Vector3(0, 0, 0)),
        owning_building_guid = 52,
        door_guid = 438
      )
      LocalObject(
        891,
        IFFLock.Constructor(Vector3(5954.047f, 5289.189f, 88.46164f), Vector3(0, 0, 180)),
        owning_building_guid = 52,
        door_guid = 435
      )
      LocalObject(
        892,
        IFFLock.Constructor(Vector3(5954.047f, 5289.189f, 108.4616f), Vector3(0, 0, 180)),
        owning_building_guid = 52,
        door_guid = 436
      )
      LocalObject(1147, Locker.Constructor(Vector3(5955.716f, 5282.963f, 76.99464f)), owning_building_guid = 52)
      LocalObject(1148, Locker.Constructor(Vector3(5955.751f, 5304.835f, 76.99464f)), owning_building_guid = 52)
      LocalObject(1149, Locker.Constructor(Vector3(5957.053f, 5282.963f, 76.99464f)), owning_building_guid = 52)
      LocalObject(1150, Locker.Constructor(Vector3(5957.088f, 5304.835f, 76.99464f)), owning_building_guid = 52)
      LocalObject(1151, Locker.Constructor(Vector3(5959.741f, 5282.963f, 76.99464f)), owning_building_guid = 52)
      LocalObject(1152, Locker.Constructor(Vector3(5959.741f, 5304.835f, 76.99464f)), owning_building_guid = 52)
      LocalObject(1153, Locker.Constructor(Vector3(5961.143f, 5282.963f, 76.99464f)), owning_building_guid = 52)
      LocalObject(1154, Locker.Constructor(Vector3(5961.143f, 5304.835f, 76.99464f)), owning_building_guid = 52)
      LocalObject(
        1432,
        Terminal.Constructor(Vector3(5961.445f, 5288.129f, 78.33264f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1433,
        Terminal.Constructor(Vector3(5961.445f, 5293.853f, 78.33264f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1434,
        Terminal.Constructor(Vector3(5961.445f, 5299.234f, 78.33264f), order_terminal),
        owning_building_guid = 52
      )
      LocalObject(
        1885,
        SpawnTube.Constructor(Vector3(5950.706f, 5285.742f, 76.48264f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 52
      )
      LocalObject(
        1886,
        SpawnTube.Constructor(Vector3(5950.706f, 5302.152f, 76.48264f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 52
      )
      LocalObject(
        1296,
        FacilityTurret.Constructor(Vector3(5927.32f, 5285.295f, 105.9426f), manned_turret),
        owning_building_guid = 52
      )
      TurretToWeapon(1296, 5073)
      LocalObject(
        1297,
        FacilityTurret.Constructor(Vector3(5962.647f, 5310.707f, 105.9426f), manned_turret),
        owning_building_guid = 52
      )
      TurretToWeapon(1297, 5074)
      LocalObject(
        1731,
        Painbox.Constructor(Vector3(5945.235f, 5291.803f, 78.49974f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        1732,
        Painbox.Constructor(Vector3(5956.889f, 5300.086f, 77.10064f), painbox_radius_continuous),
        owning_building_guid = 52
      )
      LocalObject(
        1733,
        Painbox.Constructor(Vector3(5956.975f, 5288.223f, 77.10064f), painbox_radius_continuous),
        owning_building_guid = 52
      )
    }

    Building15()

    def Building15(): Unit = { // Name: NW_Eadon_Tower Type: tower_b GUID: 53, MapID: 15
      LocalBuilding(
        "NW_Eadon_Tower",
        53,
        15,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(2588f, 3168f, 49.78637f), Vector3(0f, 0f, 318f), tower_b)
        )
      )
      LocalObject(
        1895,
        CaptureTerminal.Constructor(Vector3(2600.258f, 3156.825f, 69.78537f), secondary_capture),
        owning_building_guid = 53
      )
      LocalObject(234, Door.Constructor(Vector3(2591.565f, 3154.025f, 51.30637f)), owning_building_guid = 53)
      LocalObject(235, Door.Constructor(Vector3(2591.565f, 3154.025f, 61.30637f)), owning_building_guid = 53)
      LocalObject(236, Door.Constructor(Vector3(2591.565f, 3154.025f, 81.30637f)), owning_building_guid = 53)
      LocalObject(237, Door.Constructor(Vector3(2602.271f, 3165.916f, 51.30637f)), owning_building_guid = 53)
      LocalObject(238, Door.Constructor(Vector3(2602.271f, 3165.916f, 61.30637f)), owning_building_guid = 53)
      LocalObject(239, Door.Constructor(Vector3(2602.271f, 3165.916f, 81.30637f)), owning_building_guid = 53)
      LocalObject(1981, Door.Constructor(Vector3(2588.786f, 3152.214f, 41.12237f)), owning_building_guid = 53)
      LocalObject(1982, Door.Constructor(Vector3(2599.766f, 3164.408f, 41.12237f)), owning_building_guid = 53)
      LocalObject(
        737,
        IFFLock.Constructor(Vector3(2592.543f, 3152.053f, 51.24737f), Vector3(0, 0, 222)),
        owning_building_guid = 53,
        door_guid = 234
      )
      LocalObject(
        738,
        IFFLock.Constructor(Vector3(2592.543f, 3152.053f, 61.24737f), Vector3(0, 0, 222)),
        owning_building_guid = 53,
        door_guid = 235
      )
      LocalObject(
        739,
        IFFLock.Constructor(Vector3(2592.543f, 3152.053f, 81.24737f), Vector3(0, 0, 222)),
        owning_building_guid = 53,
        door_guid = 236
      )
      LocalObject(
        740,
        IFFLock.Constructor(Vector3(2601.295f, 3167.885f, 51.24737f), Vector3(0, 0, 42)),
        owning_building_guid = 53,
        door_guid = 237
      )
      LocalObject(
        741,
        IFFLock.Constructor(Vector3(2601.295f, 3167.885f, 61.24737f), Vector3(0, 0, 42)),
        owning_building_guid = 53,
        door_guid = 238
      )
      LocalObject(
        742,
        IFFLock.Constructor(Vector3(2601.295f, 3167.885f, 81.24737f), Vector3(0, 0, 42)),
        owning_building_guid = 53,
        door_guid = 239
      )
      LocalObject(917, Locker.Constructor(Vector3(2589.617f, 3146.309f, 39.78037f)), owning_building_guid = 53)
      LocalObject(918, Locker.Constructor(Vector3(2590.611f, 3145.415f, 39.78037f)), owning_building_guid = 53)
      LocalObject(919, Locker.Constructor(Vector3(2592.609f, 3143.616f, 39.78037f)), owning_building_guid = 53)
      LocalObject(920, Locker.Constructor(Vector3(2593.651f, 3142.678f, 39.78037f)), owning_building_guid = 53)
      LocalObject(921, Locker.Constructor(Vector3(2604.279f, 3162.54f, 39.78037f)), owning_building_guid = 53)
      LocalObject(922, Locker.Constructor(Vector3(2605.272f, 3161.645f, 39.78037f)), owning_building_guid = 53)
      LocalObject(923, Locker.Constructor(Vector3(2607.244f, 3159.87f, 39.78037f)), owning_building_guid = 53)
      LocalObject(924, Locker.Constructor(Vector3(2608.286f, 3158.932f, 39.78037f)), owning_building_guid = 53)
      LocalObject(
        1343,
        Terminal.Constructor(Vector3(2597.333f, 3146.314f, 41.11837f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1344,
        Terminal.Constructor(Vector3(2601.163f, 3150.568f, 41.11837f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1345,
        Terminal.Constructor(Vector3(2604.763f, 3154.567f, 41.11837f), order_terminal),
        owning_building_guid = 53
      )
      LocalObject(
        1832,
        SpawnTube.Constructor(Vector3(2587.754f, 3151.727f, 39.26837f), respawn_tube_tower, Vector3(0, 0, 42)),
        owning_building_guid = 53
      )
      LocalObject(
        1833,
        SpawnTube.Constructor(Vector3(2598.734f, 3163.922f, 39.26837f), respawn_tube_tower, Vector3(0, 0, 42)),
        owning_building_guid = 53
      )
      LocalObject(
        1692,
        Painbox.Constructor(Vector3(2587.297f, 3159.01f, 41.07577f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        1693,
        Painbox.Constructor(Vector3(2594.089f, 3149.166f, 39.88637f), painbox_radius_continuous),
        owning_building_guid = 53
      )
      LocalObject(
        1694,
        Painbox.Constructor(Vector3(2602.236f, 3158.017f, 39.88637f), painbox_radius_continuous),
        owning_building_guid = 53
      )
    }

    Building26()

    def Building26(): Unit = { // Name: N_Bel_Tower Type: tower_b GUID: 54, MapID: 26
      LocalBuilding(
        "N_Bel_Tower",
        54,
        26,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3568f, 5310f, 53.99487f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        1897,
        CaptureTerminal.Constructor(Vector3(3584.587f, 5309.897f, 73.99387f), secondary_capture),
        owning_building_guid = 54
      )
      LocalObject(288, Door.Constructor(Vector3(3580f, 5302f, 55.51487f)), owning_building_guid = 54)
      LocalObject(289, Door.Constructor(Vector3(3580f, 5302f, 65.51488f)), owning_building_guid = 54)
      LocalObject(290, Door.Constructor(Vector3(3580f, 5302f, 85.51488f)), owning_building_guid = 54)
      LocalObject(291, Door.Constructor(Vector3(3580f, 5318f, 55.51487f)), owning_building_guid = 54)
      LocalObject(292, Door.Constructor(Vector3(3580f, 5318f, 65.51488f)), owning_building_guid = 54)
      LocalObject(293, Door.Constructor(Vector3(3580f, 5318f, 85.51488f)), owning_building_guid = 54)
      LocalObject(1993, Door.Constructor(Vector3(3579.147f, 5298.794f, 45.33087f)), owning_building_guid = 54)
      LocalObject(1994, Door.Constructor(Vector3(3579.147f, 5315.204f, 45.33087f)), owning_building_guid = 54)
      LocalObject(
        770,
        IFFLock.Constructor(Vector3(3577.957f, 5318.811f, 55.45587f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 291
      )
      LocalObject(
        771,
        IFFLock.Constructor(Vector3(3577.957f, 5318.811f, 65.45587f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 292
      )
      LocalObject(
        772,
        IFFLock.Constructor(Vector3(3577.957f, 5318.811f, 85.45587f), Vector3(0, 0, 0)),
        owning_building_guid = 54,
        door_guid = 293
      )
      LocalObject(
        773,
        IFFLock.Constructor(Vector3(3582.047f, 5301.189f, 55.45587f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 288
      )
      LocalObject(
        774,
        IFFLock.Constructor(Vector3(3582.047f, 5301.189f, 65.45587f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 289
      )
      LocalObject(
        775,
        IFFLock.Constructor(Vector3(3582.047f, 5301.189f, 85.45587f), Vector3(0, 0, 180)),
        owning_building_guid = 54,
        door_guid = 290
      )
      LocalObject(970, Locker.Constructor(Vector3(3583.716f, 5294.963f, 43.98887f)), owning_building_guid = 54)
      LocalObject(971, Locker.Constructor(Vector3(3583.751f, 5316.835f, 43.98887f)), owning_building_guid = 54)
      LocalObject(973, Locker.Constructor(Vector3(3585.053f, 5294.963f, 43.98887f)), owning_building_guid = 54)
      LocalObject(974, Locker.Constructor(Vector3(3585.088f, 5316.835f, 43.98887f)), owning_building_guid = 54)
      LocalObject(977, Locker.Constructor(Vector3(3587.741f, 5294.963f, 43.98887f)), owning_building_guid = 54)
      LocalObject(978, Locker.Constructor(Vector3(3587.741f, 5316.835f, 43.98887f)), owning_building_guid = 54)
      LocalObject(980, Locker.Constructor(Vector3(3589.143f, 5294.963f, 43.98887f)), owning_building_guid = 54)
      LocalObject(981, Locker.Constructor(Vector3(3589.143f, 5316.835f, 43.98887f)), owning_building_guid = 54)
      LocalObject(
        1361,
        Terminal.Constructor(Vector3(3589.446f, 5300.129f, 45.32687f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1362,
        Terminal.Constructor(Vector3(3589.446f, 5305.853f, 45.32687f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1363,
        Terminal.Constructor(Vector3(3589.446f, 5311.234f, 45.32687f), order_terminal),
        owning_building_guid = 54
      )
      LocalObject(
        1844,
        SpawnTube.Constructor(Vector3(3578.706f, 5297.742f, 43.47688f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        1845,
        SpawnTube.Constructor(Vector3(3578.706f, 5314.152f, 43.47688f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 54
      )
      LocalObject(
        1695,
        Painbox.Constructor(Vector3(3573.493f, 5302.849f, 45.28427f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        1698,
        Painbox.Constructor(Vector3(3585.127f, 5300.078f, 44.09487f), painbox_radius_continuous),
        owning_building_guid = 54
      )
      LocalObject(
        1699,
        Painbox.Constructor(Vector3(3585.259f, 5312.107f, 44.09487f), painbox_radius_continuous),
        owning_building_guid = 54
      )
    }

    Building38()

    def Building38(): Unit = { // Name: S_Neit_Tower Type: tower_b GUID: 55, MapID: 38
      LocalBuilding(
        "S_Neit_Tower",
        55,
        38,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4440f, 3986f, 67.92352f), Vector3(0f, 0f, 360f), tower_b)
        )
      )
      LocalObject(
        1902,
        CaptureTerminal.Constructor(Vector3(4456.587f, 3985.897f, 87.92252f), secondary_capture),
        owning_building_guid = 55
      )
      LocalObject(346, Door.Constructor(Vector3(4452f, 3978f, 69.44352f)), owning_building_guid = 55)
      LocalObject(347, Door.Constructor(Vector3(4452f, 3978f, 79.44353f)), owning_building_guid = 55)
      LocalObject(348, Door.Constructor(Vector3(4452f, 3978f, 99.44353f)), owning_building_guid = 55)
      LocalObject(349, Door.Constructor(Vector3(4452f, 3994f, 69.44352f)), owning_building_guid = 55)
      LocalObject(350, Door.Constructor(Vector3(4452f, 3994f, 79.44353f)), owning_building_guid = 55)
      LocalObject(351, Door.Constructor(Vector3(4452f, 3994f, 99.44353f)), owning_building_guid = 55)
      LocalObject(2010, Door.Constructor(Vector3(4451.147f, 3974.794f, 59.25952f)), owning_building_guid = 55)
      LocalObject(2011, Door.Constructor(Vector3(4451.147f, 3991.204f, 59.25952f)), owning_building_guid = 55)
      LocalObject(
        818,
        IFFLock.Constructor(Vector3(4449.957f, 3994.811f, 69.38452f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 349
      )
      LocalObject(
        819,
        IFFLock.Constructor(Vector3(4449.957f, 3994.811f, 79.38452f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 350
      )
      LocalObject(
        820,
        IFFLock.Constructor(Vector3(4449.957f, 3994.811f, 99.38452f), Vector3(0, 0, 0)),
        owning_building_guid = 55,
        door_guid = 351
      )
      LocalObject(
        821,
        IFFLock.Constructor(Vector3(4454.047f, 3977.189f, 69.38452f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 346
      )
      LocalObject(
        822,
        IFFLock.Constructor(Vector3(4454.047f, 3977.189f, 79.38452f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 347
      )
      LocalObject(
        823,
        IFFLock.Constructor(Vector3(4454.047f, 3977.189f, 99.38452f), Vector3(0, 0, 180)),
        owning_building_guid = 55,
        door_guid = 348
      )
      LocalObject(1051, Locker.Constructor(Vector3(4455.716f, 3970.963f, 57.91752f)), owning_building_guid = 55)
      LocalObject(1052, Locker.Constructor(Vector3(4455.751f, 3992.835f, 57.91752f)), owning_building_guid = 55)
      LocalObject(1053, Locker.Constructor(Vector3(4457.053f, 3970.963f, 57.91752f)), owning_building_guid = 55)
      LocalObject(1054, Locker.Constructor(Vector3(4457.088f, 3992.835f, 57.91752f)), owning_building_guid = 55)
      LocalObject(1055, Locker.Constructor(Vector3(4459.741f, 3970.963f, 57.91752f)), owning_building_guid = 55)
      LocalObject(1056, Locker.Constructor(Vector3(4459.741f, 3992.835f, 57.91752f)), owning_building_guid = 55)
      LocalObject(1057, Locker.Constructor(Vector3(4461.143f, 3970.963f, 57.91752f)), owning_building_guid = 55)
      LocalObject(1058, Locker.Constructor(Vector3(4461.143f, 3992.835f, 57.91752f)), owning_building_guid = 55)
      LocalObject(
        1390,
        Terminal.Constructor(Vector3(4461.446f, 3976.129f, 59.25552f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1391,
        Terminal.Constructor(Vector3(4461.446f, 3981.853f, 59.25552f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1392,
        Terminal.Constructor(Vector3(4461.446f, 3987.234f, 59.25552f), order_terminal),
        owning_building_guid = 55
      )
      LocalObject(
        1861,
        SpawnTube.Constructor(Vector3(4450.706f, 3973.742f, 57.40553f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        1862,
        SpawnTube.Constructor(Vector3(4450.706f, 3990.152f, 57.40553f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 55
      )
      LocalObject(
        1713,
        Painbox.Constructor(Vector3(4445.493f, 3978.849f, 59.21292f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        1714,
        Painbox.Constructor(Vector3(4457.127f, 3976.078f, 58.02352f), painbox_radius_continuous),
        owning_building_guid = 55
      )
      LocalObject(
        1715,
        Painbox.Constructor(Vector3(4457.259f, 3988.107f, 58.02352f), painbox_radius_continuous),
        owning_building_guid = 55
      )
    }

    Building18()

    def Building18(): Unit = { // Name: E_TRSanc_Warpgate_Tower Type: tower_b GUID: 56, MapID: 18
      LocalBuilding(
        "E_TRSanc_Warpgate_Tower",
        56,
        18,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4822f, 2820f, 67.8167f), Vector3(0f, 0f, 34f), tower_b)
        )
      )
      LocalObject(
        1905,
        CaptureTerminal.Constructor(Vector3(4835.809f, 2829.19f, 87.8157f), secondary_capture),
        owning_building_guid = 56
      )
      LocalObject(389, Door.Constructor(Vector3(4827.475f, 2833.343f, 69.3367f)), owning_building_guid = 56)
      LocalObject(390, Door.Constructor(Vector3(4827.475f, 2833.343f, 79.3367f)), owning_building_guid = 56)
      LocalObject(391, Door.Constructor(Vector3(4827.475f, 2833.343f, 99.3367f)), owning_building_guid = 56)
      LocalObject(393, Door.Constructor(Vector3(4836.422f, 2820.078f, 69.3367f)), owning_building_guid = 56)
      LocalObject(394, Door.Constructor(Vector3(4836.422f, 2820.078f, 79.3367f)), owning_building_guid = 56)
      LocalObject(395, Door.Constructor(Vector3(4836.422f, 2820.078f, 99.3367f)), owning_building_guid = 56)
      LocalObject(2022, Door.Constructor(Vector3(4828.331f, 2830.548f, 59.1527f)), owning_building_guid = 56)
      LocalObject(2023, Door.Constructor(Vector3(4837.508f, 2816.943f, 59.1527f)), owning_building_guid = 56)
      LocalObject(
        853,
        IFFLock.Constructor(Vector3(4825.328f, 2832.873f, 69.2777f), Vector3(0, 0, 326)),
        owning_building_guid = 56,
        door_guid = 389
      )
      LocalObject(
        854,
        IFFLock.Constructor(Vector3(4825.328f, 2832.873f, 79.2777f), Vector3(0, 0, 326)),
        owning_building_guid = 56,
        door_guid = 390
      )
      LocalObject(
        855,
        IFFLock.Constructor(Vector3(4825.328f, 2832.873f, 99.2777f), Vector3(0, 0, 326)),
        owning_building_guid = 56,
        door_guid = 391
      )
      LocalObject(
        857,
        IFFLock.Constructor(Vector3(4838.573f, 2820.55f, 69.2777f), Vector3(0, 0, 146)),
        owning_building_guid = 56,
        door_guid = 393
      )
      LocalObject(
        858,
        IFFLock.Constructor(Vector3(4838.573f, 2820.55f, 79.2777f), Vector3(0, 0, 146)),
        owning_building_guid = 56,
        door_guid = 394
      )
      LocalObject(
        859,
        IFFLock.Constructor(Vector3(4838.573f, 2820.55f, 99.2777f), Vector3(0, 0, 146)),
        owning_building_guid = 56,
        door_guid = 395
      )
      LocalObject(1099, Locker.Constructor(Vector3(4831.236f, 2834.474f, 57.8107f)), owning_building_guid = 56)
      LocalObject(1100, Locker.Constructor(Vector3(4832.345f, 2835.222f, 57.8107f)), owning_building_guid = 56)
      LocalObject(1101, Locker.Constructor(Vector3(4834.544f, 2836.706f, 57.8107f)), owning_building_guid = 56)
      LocalObject(1102, Locker.Constructor(Vector3(4835.706f, 2837.49f, 57.8107f)), owning_building_guid = 56)
      LocalObject(1103, Locker.Constructor(Vector3(4843.438f, 2816.322f, 57.8107f)), owning_building_guid = 56)
      LocalObject(1104, Locker.Constructor(Vector3(4844.546f, 2817.07f, 57.8107f)), owning_building_guid = 56)
      LocalObject(1105, Locker.Constructor(Vector3(4846.774f, 2818.573f, 57.8107f)), owning_building_guid = 56)
      LocalObject(1106, Locker.Constructor(Vector3(4847.937f, 2819.357f, 57.8107f)), owning_building_guid = 56)
      LocalObject(
        1412,
        Terminal.Constructor(Vector3(4839.089f, 2833.015f, 59.1487f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1413,
        Terminal.Constructor(Vector3(4842.099f, 2828.554f, 59.1487f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1414,
        Terminal.Constructor(Vector3(4845.299f, 2823.809f, 59.1487f), order_terminal),
        owning_building_guid = 56
      )
      LocalObject(
        1873,
        SpawnTube.Constructor(Vector3(4828.554f, 2829.429f, 57.29871f), respawn_tube_tower, Vector3(0, 0, 326)),
        owning_building_guid = 56
      )
      LocalObject(
        1874,
        SpawnTube.Constructor(Vector3(4837.73f, 2815.824f, 57.29871f), respawn_tube_tower, Vector3(0, 0, 326)),
        owning_building_guid = 56
      )
      LocalObject(
        1722,
        Painbox.Constructor(Vector3(4830.553f, 2817.143f, 59.1061f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        1723,
        Painbox.Constructor(Vector3(4835.131f, 2831.398f, 57.9167f), painbox_radius_continuous),
        owning_building_guid = 56
      )
      LocalObject(
        1724,
        Painbox.Constructor(Vector3(4841.748f, 2821.352f, 57.9167f), painbox_radius_continuous),
        owning_building_guid = 56
      )
    }

    Building21()

    def Building21(): Unit = { // Name: N_Dagda_Tower Type: tower_b GUID: 57, MapID: 21
      LocalBuilding(
        "N_Dagda_Tower",
        57,
        21,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(5896f, 4678f, 50.33113f), Vector3(0f, 0f, 14f), tower_b)
        )
      )
      LocalObject(
        1907,
        CaptureTerminal.Constructor(Vector3(5912.119f, 4681.913f, 70.33012f), secondary_capture),
        owning_building_guid = 57
      )
      LocalObject(421, Door.Constructor(Vector3(5905.708f, 4688.666f, 51.85113f)), owning_building_guid = 57)
      LocalObject(422, Door.Constructor(Vector3(5905.708f, 4688.666f, 61.85113f)), owning_building_guid = 57)
      LocalObject(423, Door.Constructor(Vector3(5905.708f, 4688.666f, 81.85113f)), owning_building_guid = 57)
      LocalObject(424, Door.Constructor(Vector3(5909.579f, 4673.141f, 51.85113f)), owning_building_guid = 57)
      LocalObject(425, Door.Constructor(Vector3(5909.579f, 4673.141f, 61.85113f)), owning_building_guid = 57)
      LocalObject(426, Door.Constructor(Vector3(5909.579f, 4673.141f, 81.85113f)), owning_building_guid = 57)
      LocalObject(2032, Door.Constructor(Vector3(5905.557f, 4685.746f, 41.66713f)), owning_building_guid = 57)
      LocalObject(2033, Door.Constructor(Vector3(5909.527f, 4669.824f, 41.66713f)), owning_building_guid = 57)
      LocalObject(
        875,
        IFFLock.Constructor(Vector3(5903.53f, 4688.958f, 51.79213f), Vector3(0, 0, 346)),
        owning_building_guid = 57,
        door_guid = 421
      )
      LocalObject(
        876,
        IFFLock.Constructor(Vector3(5903.53f, 4688.958f, 61.79213f), Vector3(0, 0, 346)),
        owning_building_guid = 57,
        door_guid = 422
      )
      LocalObject(
        877,
        IFFLock.Constructor(Vector3(5903.53f, 4688.958f, 81.79213f), Vector3(0, 0, 346)),
        owning_building_guid = 57,
        door_guid = 423
      )
      LocalObject(
        879,
        IFFLock.Constructor(Vector3(5911.761f, 4672.849f, 51.79213f), Vector3(0, 0, 166)),
        owning_building_guid = 57,
        door_guid = 424
      )
      LocalObject(
        880,
        IFFLock.Constructor(Vector3(5911.761f, 4672.849f, 61.79213f), Vector3(0, 0, 166)),
        owning_building_guid = 57,
        door_guid = 425
      )
      LocalObject(
        881,
        IFFLock.Constructor(Vector3(5911.761f, 4672.849f, 81.79213f), Vector3(0, 0, 166)),
        owning_building_guid = 57,
        door_guid = 426
      )
      LocalObject(1139, Locker.Constructor(Vector3(5909.629f, 4688.442f, 40.32513f)), owning_building_guid = 57)
      LocalObject(1140, Locker.Constructor(Vector3(5910.927f, 4688.766f, 40.32513f)), owning_building_guid = 57)
      LocalObject(1141, Locker.Constructor(Vector3(5913.501f, 4689.408f, 40.32513f)), owning_building_guid = 57)
      LocalObject(1142, Locker.Constructor(Vector3(5914.887f, 4667.212f, 40.32513f)), owning_building_guid = 57)
      LocalObject(1143, Locker.Constructor(Vector3(5914.861f, 4689.747f, 40.32513f)), owning_building_guid = 57)
      LocalObject(1144, Locker.Constructor(Vector3(5916.184f, 4667.535f, 40.32513f)), owning_building_guid = 57)
      LocalObject(1145, Locker.Constructor(Vector3(5918.792f, 4668.186f, 40.32513f)), owning_building_guid = 57)
      LocalObject(1146, Locker.Constructor(Vector3(5920.153f, 4668.524f, 40.32513f)), owning_building_guid = 57)
      LocalObject(
        1429,
        Terminal.Constructor(Vector3(5916.51f, 4684.386f, 41.66313f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1430,
        Terminal.Constructor(Vector3(5917.812f, 4679.165f, 41.66313f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1431,
        Terminal.Constructor(Vector3(5919.197f, 4673.61f, 41.66313f), order_terminal),
        owning_building_guid = 57
      )
      LocalObject(
        1883,
        SpawnTube.Constructor(Vector3(5905.383f, 4684.619f, 39.81313f), respawn_tube_tower, Vector3(0, 0, 346)),
        owning_building_guid = 57
      )
      LocalObject(
        1884,
        SpawnTube.Constructor(Vector3(5909.354f, 4668.696f, 39.81313f), respawn_tube_tower, Vector3(0, 0, 346)),
        owning_building_guid = 57
      )
      LocalObject(
        1728,
        Painbox.Constructor(Vector3(5903.06f, 4672.39f, 41.62053f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        1729,
        Painbox.Constructor(Vector3(5912.237f, 4684.22f, 40.43113f), painbox_radius_continuous),
        owning_building_guid = 57
      )
      LocalObject(
        1730,
        Painbox.Constructor(Vector3(5915.019f, 4672.516f, 40.43113f), painbox_radius_continuous),
        owning_building_guid = 57
      )
    }

    Building16()

    def Building16(): Unit = { // Name: NE_Anu_Tower Type: tower_c GUID: 58, MapID: 16
      LocalBuilding(
        "NE_Anu_Tower",
        58,
        16,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(3572f, 2702f, 43.39885f), Vector3(0f, 0f, 42f), tower_c)
        )
      )
      LocalObject(
        1896,
        CaptureTerminal.Constructor(Vector3(3584.396f, 2713.022f, 53.39785f), secondary_capture),
        owning_building_guid = 58
      )
      LocalObject(286, Door.Constructor(Vector3(3575.565f, 2715.975f, 44.91985f)), owning_building_guid = 58)
      LocalObject(287, Door.Constructor(Vector3(3575.565f, 2715.975f, 64.91885f)), owning_building_guid = 58)
      LocalObject(294, Door.Constructor(Vector3(3586.271f, 2704.084f, 44.91985f)), owning_building_guid = 58)
      LocalObject(295, Door.Constructor(Vector3(3586.271f, 2704.084f, 64.91885f)), owning_building_guid = 58)
      LocalObject(1992, Door.Constructor(Vector3(3576.801f, 2713.325f, 34.73485f)), owning_building_guid = 58)
      LocalObject(1995, Door.Constructor(Vector3(3587.781f, 2701.13f, 34.73485f)), owning_building_guid = 58)
      LocalObject(
        768,
        IFFLock.Constructor(Vector3(3573.504f, 2715.21f, 44.85984f), Vector3(0, 0, 318)),
        owning_building_guid = 58,
        door_guid = 286
      )
      LocalObject(
        769,
        IFFLock.Constructor(Vector3(3573.504f, 2715.21f, 64.85985f), Vector3(0, 0, 318)),
        owning_building_guid = 58,
        door_guid = 287
      )
      LocalObject(
        776,
        IFFLock.Constructor(Vector3(3588.335f, 2704.851f, 44.85984f), Vector3(0, 0, 138)),
        owning_building_guid = 58,
        door_guid = 294
      )
      LocalObject(
        777,
        IFFLock.Constructor(Vector3(3588.335f, 2704.851f, 64.85985f), Vector3(0, 0, 138)),
        owning_building_guid = 58,
        door_guid = 295
      )
      LocalObject(965, Locker.Constructor(Vector3(3579.132f, 2717.619f, 33.39285f)), owning_building_guid = 58)
      LocalObject(966, Locker.Constructor(Vector3(3580.125f, 2718.513f, 33.39285f)), owning_building_guid = 58)
      LocalObject(967, Locker.Constructor(Vector3(3582.097f, 2720.289f, 33.39285f)), owning_building_guid = 58)
      LocalObject(968, Locker.Constructor(Vector3(3583.139f, 2721.227f, 33.39285f)), owning_building_guid = 58)
      LocalObject(982, Locker.Constructor(Vector3(3593.741f, 2701.341f, 33.39285f)), owning_building_guid = 58)
      LocalObject(983, Locker.Constructor(Vector3(3594.735f, 2702.236f, 33.39285f)), owning_building_guid = 58)
      LocalObject(984, Locker.Constructor(Vector3(3596.732f, 2704.035f, 33.39285f)), owning_building_guid = 58)
      LocalObject(985, Locker.Constructor(Vector3(3597.774f, 2704.973f, 33.39285f)), owning_building_guid = 58)
      LocalObject(
        1360,
        Terminal.Constructor(Vector3(3587.111f, 2717.267f, 34.73085f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1364,
        Terminal.Constructor(Vector3(3590.712f, 2713.268f, 34.73085f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1365,
        Terminal.Constructor(Vector3(3594.542f, 2709.014f, 34.73085f), order_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1843,
        SpawnTube.Constructor(Vector3(3577.178f, 2712.249f, 32.88084f), respawn_tube_tower, Vector3(0, 0, 318)),
        owning_building_guid = 58
      )
      LocalObject(
        1846,
        SpawnTube.Constructor(Vector3(3588.158f, 2700.054f, 32.88084f), respawn_tube_tower, Vector3(0, 0, 318)),
        owning_building_guid = 58
      )
      LocalObject(
        1605,
        ProximityTerminal.Constructor(Vector3(3567.728f, 2705.111f, 70.96884f), pad_landing_tower_frame),
        owning_building_guid = 58
      )
      LocalObject(
        1606,
        Terminal.Constructor(Vector3(3567.728f, 2705.111f, 70.96884f), air_rearm_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1608,
        ProximityTerminal.Constructor(Vector3(3574.717f, 2697.349f, 70.96884f), pad_landing_tower_frame),
        owning_building_guid = 58
      )
      LocalObject(
        1609,
        Terminal.Constructor(Vector3(3574.717f, 2697.349f, 70.96884f), air_rearm_terminal),
        owning_building_guid = 58
      )
      LocalObject(
        1245,
        FacilityTurret.Constructor(Vector3(3570.912f, 2680.896f, 62.34084f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1245, 5075)
      LocalObject(
        1246,
        FacilityTurret.Constructor(Vector3(3579.453f, 2728.838f, 62.34084f), manned_turret),
        owning_building_guid = 58
      )
      TurretToWeapon(1246, 5076)
      LocalObject(
        1696,
        Painbox.Constructor(Vector3(3580.095f, 2699.666f, 35.41835f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        1697,
        Painbox.Constructor(Vector3(3583.364f, 2714.953f, 33.49885f), painbox_radius_continuous),
        owning_building_guid = 58
      )
      LocalObject(
        1700,
        Painbox.Constructor(Vector3(3591.576f, 2705.55f, 33.49885f), painbox_radius_continuous),
        owning_building_guid = 58
      )
    }

    Building24()

    def Building24(): Unit = { // Name: N_Pwyll_Tower Type: tower_c GUID: 59, MapID: 24
      LocalBuilding(
        "N_Pwyll_Tower",
        59,
        24,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(4728f, 5584f, 48.18415f), Vector3(0f, 0f, 360f), tower_c)
        )
      )
      LocalObject(
        1904,
        CaptureTerminal.Constructor(Vector3(4744.587f, 5583.897f, 58.18315f), secondary_capture),
        owning_building_guid = 59
      )
      LocalObject(377, Door.Constructor(Vector3(4740f, 5576f, 49.70515f)), owning_building_guid = 59)
      LocalObject(378, Door.Constructor(Vector3(4740f, 5576f, 69.70415f)), owning_building_guid = 59)
      LocalObject(379, Door.Constructor(Vector3(4740f, 5592f, 49.70515f)), owning_building_guid = 59)
      LocalObject(380, Door.Constructor(Vector3(4740f, 5592f, 69.70415f)), owning_building_guid = 59)
      LocalObject(2017, Door.Constructor(Vector3(4739.146f, 5572.794f, 39.52015f)), owning_building_guid = 59)
      LocalObject(2018, Door.Constructor(Vector3(4739.146f, 5589.204f, 39.52015f)), owning_building_guid = 59)
      LocalObject(
        843,
        IFFLock.Constructor(Vector3(4737.957f, 5592.811f, 49.64515f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 379
      )
      LocalObject(
        844,
        IFFLock.Constructor(Vector3(4737.957f, 5592.811f, 69.64515f), Vector3(0, 0, 0)),
        owning_building_guid = 59,
        door_guid = 380
      )
      LocalObject(
        846,
        IFFLock.Constructor(Vector3(4742.047f, 5575.189f, 49.64515f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 377
      )
      LocalObject(
        847,
        IFFLock.Constructor(Vector3(4742.047f, 5575.189f, 69.64515f), Vector3(0, 0, 180)),
        owning_building_guid = 59,
        door_guid = 378
      )
      LocalObject(1091, Locker.Constructor(Vector3(4743.716f, 5568.963f, 38.17815f)), owning_building_guid = 59)
      LocalObject(1092, Locker.Constructor(Vector3(4743.751f, 5590.835f, 38.17815f)), owning_building_guid = 59)
      LocalObject(1093, Locker.Constructor(Vector3(4745.053f, 5568.963f, 38.17815f)), owning_building_guid = 59)
      LocalObject(1094, Locker.Constructor(Vector3(4745.088f, 5590.835f, 38.17815f)), owning_building_guid = 59)
      LocalObject(1095, Locker.Constructor(Vector3(4747.741f, 5568.963f, 38.17815f)), owning_building_guid = 59)
      LocalObject(1096, Locker.Constructor(Vector3(4747.741f, 5590.835f, 38.17815f)), owning_building_guid = 59)
      LocalObject(1097, Locker.Constructor(Vector3(4749.143f, 5568.963f, 38.17815f)), owning_building_guid = 59)
      LocalObject(1098, Locker.Constructor(Vector3(4749.143f, 5590.835f, 38.17815f)), owning_building_guid = 59)
      LocalObject(
        1408,
        Terminal.Constructor(Vector3(4749.445f, 5574.129f, 39.51615f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1409,
        Terminal.Constructor(Vector3(4749.445f, 5579.853f, 39.51615f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1410,
        Terminal.Constructor(Vector3(4749.445f, 5585.234f, 39.51615f), order_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1868,
        SpawnTube.Constructor(Vector3(4738.706f, 5571.742f, 37.66615f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        1869,
        SpawnTube.Constructor(Vector3(4738.706f, 5588.152f, 37.66615f), respawn_tube_tower, Vector3(0, 0, 0)),
        owning_building_guid = 59
      )
      LocalObject(
        1611,
        ProximityTerminal.Constructor(Vector3(4726.907f, 5578.725f, 75.75415f), pad_landing_tower_frame),
        owning_building_guid = 59
      )
      LocalObject(
        1612,
        Terminal.Constructor(Vector3(4726.907f, 5578.725f, 75.75415f), air_rearm_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1614,
        ProximityTerminal.Constructor(Vector3(4726.907f, 5589.17f, 75.75415f), pad_landing_tower_frame),
        owning_building_guid = 59
      )
      LocalObject(
        1615,
        Terminal.Constructor(Vector3(4726.907f, 5589.17f, 75.75415f), air_rearm_terminal),
        owning_building_guid = 59
      )
      LocalObject(
        1276,
        FacilityTurret.Constructor(Vector3(4713.07f, 5569.045f, 67.12614f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1276, 5077)
      LocalObject(
        1281,
        FacilityTurret.Constructor(Vector3(4751.497f, 5598.957f, 67.12614f), manned_turret),
        owning_building_guid = 59
      )
      TurretToWeapon(1281, 5078)
      LocalObject(
        1719,
        Painbox.Constructor(Vector3(4732.454f, 5576.849f, 40.20365f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        1720,
        Painbox.Constructor(Vector3(4744.923f, 5573.54f, 38.28415f), painbox_radius_continuous),
        owning_building_guid = 59
      )
      LocalObject(
        1721,
        Painbox.Constructor(Vector3(4745.113f, 5586.022f, 38.28415f), painbox_radius_continuous),
        owning_building_guid = 59
      )
    }

    Building19()

    def Building19(): Unit = { // Name: S_Gwydion_Tower Type: tower_c GUID: 60, MapID: 19
      LocalBuilding(
        "S_Gwydion_Tower",
        60,
        19,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(6018f, 2726f, 46.91305f), Vector3(0f, 0f, 308f), tower_c)
        )
      )
      LocalObject(
        1909,
        CaptureTerminal.Constructor(Vector3(6028.131f, 2712.866f, 56.91205f), secondary_capture),
        owning_building_guid = 60
      )
      LocalObject(440, Door.Constructor(Vector3(6019.084f, 2711.619f, 48.43405f)), owning_building_guid = 60)
      LocalObject(441, Door.Constructor(Vector3(6019.084f, 2711.619f, 68.43305f)), owning_building_guid = 60)
      LocalObject(442, Door.Constructor(Vector3(6031.692f, 2721.469f, 48.43405f)), owning_building_guid = 60)
      LocalObject(443, Door.Constructor(Vector3(6031.692f, 2721.469f, 68.43305f)), owning_building_guid = 60)
      LocalObject(2036, Door.Constructor(Vector3(6016.032f, 2710.318f, 38.24905f)), owning_building_guid = 60)
      LocalObject(2037, Door.Constructor(Vector3(6028.963f, 2720.421f, 38.24905f)), owning_building_guid = 60)
      LocalObject(
        893,
        IFFLock.Constructor(Vector3(6019.705f, 2709.506f, 48.37405f), Vector3(0, 0, 232)),
        owning_building_guid = 60,
        door_guid = 440
      )
      LocalObject(
        894,
        IFFLock.Constructor(Vector3(6019.705f, 2709.506f, 68.37405f), Vector3(0, 0, 232)),
        owning_building_guid = 60,
        door_guid = 441
      )
      LocalObject(
        895,
        IFFLock.Constructor(Vector3(6031.073f, 2723.578f, 48.37405f), Vector3(0, 0, 52)),
        owning_building_guid = 60,
        door_guid = 442
      )
      LocalObject(
        896,
        IFFLock.Constructor(Vector3(6031.073f, 2723.578f, 68.37405f), Vector3(0, 0, 52)),
        owning_building_guid = 60,
        door_guid = 443
      )
      LocalObject(1155, Locker.Constructor(Vector3(6015.827f, 2704.358f, 36.90705f)), owning_building_guid = 60)
      LocalObject(1156, Locker.Constructor(Vector3(6016.649f, 2703.304f, 36.90705f)), owning_building_guid = 60)
      LocalObject(1157, Locker.Constructor(Vector3(6018.305f, 2701.186f, 36.90705f)), owning_building_guid = 60)
      LocalObject(1158, Locker.Constructor(Vector3(6019.167f, 2700.081f, 36.90705f)), owning_building_guid = 60)
      LocalObject(1159, Locker.Constructor(Vector3(6033.083f, 2717.796f, 36.90705f)), owning_building_guid = 60)
      LocalObject(1160, Locker.Constructor(Vector3(6033.906f, 2716.742f, 36.90705f)), owning_building_guid = 60)
      LocalObject(1161, Locker.Constructor(Vector3(6035.54f, 2714.652f, 36.90705f)), owning_building_guid = 60)
      LocalObject(1162, Locker.Constructor(Vector3(6036.403f, 2713.547f, 36.90705f)), owning_building_guid = 60)
      LocalObject(
        1435,
        Terminal.Constructor(Vector3(6023.424f, 2703.024f, 38.24505f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1436,
        Terminal.Constructor(Vector3(6027.935f, 2706.548f, 38.24505f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1437,
        Terminal.Constructor(Vector3(6032.175f, 2709.861f, 38.24505f), order_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1887,
        SpawnTube.Constructor(Vector3(6014.932f, 2710.017f, 36.39505f), respawn_tube_tower, Vector3(0, 0, 52)),
        owning_building_guid = 60
      )
      LocalObject(
        1888,
        SpawnTube.Constructor(Vector3(6027.863f, 2720.12f, 36.39505f), respawn_tube_tower, Vector3(0, 0, 52)),
        owning_building_guid = 60
      )
      LocalObject(
        1617,
        ProximityTerminal.Constructor(Vector3(6013.17f, 2723.614f, 74.48305f), pad_landing_tower_frame),
        owning_building_guid = 60
      )
      LocalObject(
        1618,
        Terminal.Constructor(Vector3(6013.17f, 2723.614f, 74.48305f), air_rearm_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1620,
        ProximityTerminal.Constructor(Vector3(6021.401f, 2730.044f, 74.48305f), pad_landing_tower_frame),
        owning_building_guid = 60
      )
      LocalObject(
        1621,
        Terminal.Constructor(Vector3(6021.401f, 2730.044f, 74.48305f), air_rearm_terminal),
        owning_building_guid = 60
      )
      LocalObject(
        1300,
        FacilityTurret.Constructor(Vector3(5997.023f, 2728.558f, 65.85505f), manned_turret),
        owning_building_guid = 60
      )
      TurretToWeapon(1300, 5079)
      LocalObject(
        1301,
        FacilityTurret.Constructor(Vector3(6044.252f, 2716.693f, 65.85505f), manned_turret),
        owning_building_guid = 60
      )
      TurretToWeapon(1301, 5080)
      LocalObject(
        1734,
        Painbox.Constructor(Vector3(6015.107f, 2718.088f, 38.93255f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        1735,
        Painbox.Constructor(Vector3(6020.176f, 2706.224f, 37.01305f), painbox_radius_continuous),
        owning_building_guid = 60
      )
      LocalObject(
        1736,
        Painbox.Constructor(Vector3(6030.129f, 2713.76f, 37.01305f), painbox_radius_continuous),
        owning_building_guid = 60
      )
    }

    Building23()

    def Building23(): Unit = { // Name: W_Oshur_Warpgate_Tower Type: tower_c GUID: 61, MapID: 23
      LocalBuilding(
        "W_Oshur_Warpgate_Tower",
        61,
        23,
        FoundationBuilder(
          Building.Structure(StructureType.Tower, Vector3(7008f, 3838f, 49.3027f), Vector3(0f, 0f, 106f), tower_c)
        )
      )
      LocalObject(
        1910,
        CaptureTerminal.Constructor(Vector3(7003.527f, 3853.973f, 59.3017f), secondary_capture),
        owning_building_guid = 61
      )
      LocalObject(455, Door.Constructor(Vector3(6997.002f, 3847.33f, 50.8237f)), owning_building_guid = 61)
      LocalObject(456, Door.Constructor(Vector3(6997.002f, 3847.33f, 70.8227f)), owning_building_guid = 61)
      LocalObject(457, Door.Constructor(Vector3(7012.382f, 3851.74f, 50.8237f)), owning_building_guid = 61)
      LocalObject(458, Door.Constructor(Vector3(7012.382f, 3851.74f, 70.8227f)), owning_building_guid = 61)
      LocalObject(2041, Door.Constructor(Vector3(6999.925f, 3847.28f, 40.6387f)), owning_building_guid = 61)
      LocalObject(2042, Door.Constructor(Vector3(7015.7f, 3851.803f, 40.6387f)), owning_building_guid = 61)
      LocalObject(
        905,
        IFFLock.Constructor(Vector3(6996.786f, 3845.143f, 50.7637f), Vector3(0, 0, 254)),
        owning_building_guid = 61,
        door_guid = 455
      )
      LocalObject(
        906,
        IFFLock.Constructor(Vector3(6996.786f, 3845.143f, 70.7637f), Vector3(0, 0, 254)),
        owning_building_guid = 61,
        door_guid = 456
      )
      LocalObject(
        907,
        IFFLock.Constructor(Vector3(7012.598f, 3853.931f, 50.7637f), Vector3(0, 0, 74)),
        owning_building_guid = 61,
        door_guid = 457
      )
      LocalObject(
        908,
        IFFLock.Constructor(Vector3(7012.598f, 3853.931f, 70.7637f), Vector3(0, 0, 74)),
        owning_building_guid = 61,
        door_guid = 458
      )
      LocalObject(1184, Locker.Constructor(Vector3(6995.602f, 3856.44f, 39.2967f)), owning_building_guid = 61)
      LocalObject(1185, Locker.Constructor(Vector3(6995.988f, 3855.092f, 39.2967f)), owning_building_guid = 61)
      LocalObject(1186, Locker.Constructor(Vector3(6996.72f, 3852.542f, 39.2967f)), owning_building_guid = 61)
      LocalObject(1187, Locker.Constructor(Vector3(6997.088f, 3851.257f, 39.2967f)), owning_building_guid = 61)
      LocalObject(1188, Locker.Constructor(Vector3(7016.626f, 3862.469f, 39.2967f)), owning_building_guid = 61)
      LocalObject(1189, Locker.Constructor(Vector3(7017.013f, 3861.121f, 39.2967f)), owning_building_guid = 61)
      LocalObject(1190, Locker.Constructor(Vector3(7017.754f, 3858.537f, 39.2967f)), owning_building_guid = 61)
      LocalObject(1191, Locker.Constructor(Vector3(7018.123f, 3857.252f, 39.2967f)), owning_building_guid = 61)
      LocalObject(
        1442,
        Terminal.Constructor(Vector3(7000.903f, 3858.274f, 40.6347f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1443,
        Terminal.Constructor(Vector3(7006.075f, 3859.757f, 40.6347f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1444,
        Terminal.Constructor(Vector3(7011.578f, 3861.335f, 40.6347f), order_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1892,
        SpawnTube.Constructor(Vector3(7001.058f, 3847.147f, 38.7847f), respawn_tube_tower, Vector3(0, 0, 254)),
        owning_building_guid = 61
      )
      LocalObject(
        1893,
        SpawnTube.Constructor(Vector3(7016.832f, 3851.67f, 38.7847f), respawn_tube_tower, Vector3(0, 0, 254)),
        owning_building_guid = 61
      )
      LocalObject(
        1623,
        ProximityTerminal.Constructor(Vector3(7003.332f, 3835.524f, 76.8727f), pad_landing_tower_frame),
        owning_building_guid = 61
      )
      LocalObject(
        1624,
        Terminal.Constructor(Vector3(7003.332f, 3835.524f, 76.8727f), air_rearm_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1626,
        ProximityTerminal.Constructor(Vector3(7013.372f, 3838.403f, 76.8727f), pad_landing_tower_frame),
        owning_building_guid = 61
      )
      LocalObject(
        1627,
        Terminal.Constructor(Vector3(7013.372f, 3838.403f, 76.8727f), air_rearm_terminal),
        owning_building_guid = 61
      )
      LocalObject(
        1307,
        FacilityTurret.Constructor(Vector3(6987.146f, 3856.464f, 68.2447f), manned_turret),
        owning_building_guid = 61
      )
      TurretToWeapon(1307, 5081)
      LocalObject(
        1308,
        FacilityTurret.Constructor(Vector3(7026.491f, 3827.771f, 68.2447f), manned_turret),
        owning_building_guid = 61
      )
      TurretToWeapon(1308, 5082)
      LocalObject(
        1737,
        Painbox.Constructor(Vector3(7001.339f, 3853.893f, 39.4027f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        1738,
        Painbox.Constructor(Vector3(7013.391f, 3857.151f, 39.4027f), painbox_radius_continuous),
        owning_building_guid = 61
      )
      LocalObject(
        1739,
        Painbox.Constructor(Vector3(7013.646f, 3844.252f, 41.3222f), painbox_radius_continuous),
        owning_building_guid = 61
      )
    }

    Building3()

    def Building3(): Unit = { // Name: WG_Forseral_to_Solsar Type: warpgate GUID: 62, MapID: 3
      LocalBuilding(
        "WG_Forseral_to_Solsar",
        62,
        3,
        FoundationBuilder(WarpGate.Structure(Vector3(1556f, 3690f, 53.5783f)))
      )
    }

    Building1()

    def Building1(): Unit = { // Name: WG_Forseral_to_TRSanc Type: warpgate GUID: 63, MapID: 1
      LocalBuilding(
        "WG_Forseral_to_TRSanc",
        63,
        1,
        FoundationBuilder(WarpGate.Structure(Vector3(3330f, 1338f, 42.85409f)))
      )
    }

    Building2()

    def Building2(): Unit = { // Name: WG_Forseral_to_Ceryshen Type: warpgate GUID: 64, MapID: 2
      LocalBuilding(
        "WG_Forseral_to_Ceryshen",
        64,
        2,
        FoundationBuilder(WarpGate.Structure(Vector3(3432f, 6630f, 73.72356f)))
      )
    }

    Building4()

    def Building4(): Unit = { // Name: WG_Forseral_to_Oshur Type: warpgate GUID: 65, MapID: 4
      LocalBuilding(
        "WG_Forseral_to_Oshur",
        65,
        4,
        FoundationBuilder(WarpGate.Structure(Vector3(7328f, 3850f, 47.09464f)))
      )
    }

    def Lattice(): Unit = {
      LatticeLink("Eadon", "Anu")
      LatticeLink("Gwydion", "Pwyll")
      LatticeLink("Gwydion", "Caer")
      LatticeLink("Neit", "Ogma")
      LatticeLink("Neit", "Pwyll")
      LatticeLink("Neit", "Gwydion")
      LatticeLink("Neit", "Caer")
      LatticeLink("Eadon", "WG_Forseral_to_Solsar")
      LatticeLink("Anu", "WG_Forseral_to_TRSanc")
      LatticeLink("Bel", "WG_Forseral_to_Ceryshen")
      LatticeLink("Dagda", "WG_Forseral_to_Oshur")
      LatticeLink("Anu", "Caer")
      LatticeLink("Pwyll", "GW_Forseral_N")
      LatticeLink("Caer", "GW_Forseral_S")
      LatticeLink("Caer", "Ogma")
      LatticeLink("Ogma", "Eadon")
      LatticeLink("Ogma", "Bel")
      LatticeLink("Bel", "Pwyll")
      LatticeLink("Pwyll", "Lugh")
      LatticeLink("Lugh", "Dagda")
      LatticeLink("Dagda", "Gwydion")
    }

    Lattice()

  }
}
