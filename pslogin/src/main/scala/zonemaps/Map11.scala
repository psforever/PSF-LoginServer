package zonemaps

import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.painbox.Painbox
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.{CaptureTerminal, ProximityTerminal, Terminal}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.zones.ZoneMap
import net.psforever.types.Vector3

object Map11 { // HOME1 (NEW CONGLOMORATE SANCTUARY)
  val ZoneMap = new ZoneMap("map11") {
    Checksum = 4129515529L

    Building37()

    def Building37(): Unit = { // Name: Cyssor_HART Type: orbital_building_nc GUID: 1, MapID: 37
      LocalBuilding("Cyssor_HART", 1, 37, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2258f, 5538f, 65.20142f), orbital_building_nc)))
      LocalObject(371, Door.Constructor(Vector3(2177.802f, 5527.388f, 69.30743f)), owning_building_guid = 1)
      LocalObject(372, Door.Constructor(Vector3(2178.221f, 5551.384f, 69.30743f)), owning_building_guid = 1)
      LocalObject(374, Door.Constructor(Vector3(2337.779f, 5524.616f, 69.30743f)), owning_building_guid = 1)
      LocalObject(375, Door.Constructor(Vector3(2338.198f, 5548.612f, 69.30743f)), owning_building_guid = 1)
      LocalObject(400, Door.Constructor(Vector3(2196.349f, 5539.076f, 69.30743f)), owning_building_guid = 1)
      LocalObject(401, Door.Constructor(Vector3(2196.349f, 5539.076f, 79.30743f)), owning_building_guid = 1)
      LocalObject(402, Door.Constructor(Vector3(2242.84f, 5586.262f, 84.30743f)), owning_building_guid = 1)
      LocalObject(403, Door.Constructor(Vector3(2274.835f, 5585.704f, 84.30743f)), owning_building_guid = 1)
      LocalObject(404, Door.Constructor(Vector3(2319.631f, 5536.924f, 69.30743f)), owning_building_guid = 1)
      LocalObject(405, Door.Constructor(Vector3(2319.631f, 5536.924f, 79.30743f)), owning_building_guid = 1)
      LocalObject(472, Door.Constructor(Vector3(2202.358f, 5558.984f, 79.30743f)), owning_building_guid = 1)
      LocalObject(473, Door.Constructor(Vector3(2314.34f, 5557.01f, 79.30743f)), owning_building_guid = 1)
      LocalObject(478, Door.Constructor(Vector3(2201.799f, 5526.994f, 69.30743f)), owning_building_guid = 1)
      LocalObject(479, Door.Constructor(Vector3(2201.799f, 5526.994f, 79.30743f)), owning_building_guid = 1)
      LocalObject(480, Door.Constructor(Vector3(2202.218f, 5550.991f, 69.30743f)), owning_building_guid = 1)
      LocalObject(481, Door.Constructor(Vector3(2202.218f, 5550.991f, 79.30743f)), owning_building_guid = 1)
      LocalObject(482, Door.Constructor(Vector3(2313.782f, 5525.009f, 69.30743f)), owning_building_guid = 1)
      LocalObject(483, Door.Constructor(Vector3(2313.782f, 5525.009f, 79.30743f)), owning_building_guid = 1)
      LocalObject(484, Door.Constructor(Vector3(2314.201f, 5549.006f, 69.30743f)), owning_building_guid = 1)
      LocalObject(485, Door.Constructor(Vector3(2314.201f, 5549.006f, 79.30743f)), owning_building_guid = 1)
      LocalObject(732, Locker.Constructor(Vector3(2254.307f, 5589.887f, 82.31142f)), owning_building_guid = 1)
      LocalObject(733, Locker.Constructor(Vector3(2255.637f, 5589.864f, 82.31142f)), owning_building_guid = 1)
      LocalObject(734, Locker.Constructor(Vector3(2256.954f, 5589.841f, 82.31142f)), owning_building_guid = 1)
      LocalObject(735, Locker.Constructor(Vector3(2258.229f, 5589.819f, 82.31142f)), owning_building_guid = 1)
      LocalObject(736, Locker.Constructor(Vector3(2259.518f, 5589.796f, 82.31142f)), owning_building_guid = 1)
      LocalObject(737, Locker.Constructor(Vector3(2260.838f, 5589.773f, 82.31142f)), owning_building_guid = 1)
      LocalObject(738, Locker.Constructor(Vector3(2262.18f, 5589.75f, 82.31142f)), owning_building_guid = 1)
      LocalObject(739, Locker.Constructor(Vector3(2263.544f, 5589.726f, 82.31142f)), owning_building_guid = 1)
      LocalObject(162, Terminal.Constructor(Vector3(2179.098f, 5532.182f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(163, Terminal.Constructor(Vector3(2179.132f, 5534.089f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(164, Terminal.Constructor(Vector3(2179.166f, 5536.02f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(165, Terminal.Constructor(Vector3(2179.372f, 5543.042f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(166, Terminal.Constructor(Vector3(2179.405f, 5544.949f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(167, Terminal.Constructor(Vector3(2179.439f, 5546.879f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(168, Terminal.Constructor(Vector3(2190.288f, 5523.239f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(169, Terminal.Constructor(Vector3(2190.356f, 5527.142f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(170, Terminal.Constructor(Vector3(2190.423f, 5530.979f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(171, Terminal.Constructor(Vector3(2190.786f, 5547.615f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(172, Terminal.Constructor(Vector3(2190.853f, 5551.493f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(173, Terminal.Constructor(Vector3(2190.921f, 5555.364f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(174, Terminal.Constructor(Vector3(2325.091f, 5520.695f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(175, Terminal.Constructor(Vector3(2325.159f, 5524.567f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(176, Terminal.Constructor(Vector3(2325.227f, 5528.444f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(177, Terminal.Constructor(Vector3(2325.504f, 5545.026f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(178, Terminal.Constructor(Vector3(2325.571f, 5548.863f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(179, Terminal.Constructor(Vector3(2325.639f, 5552.766f, 77.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(180, Terminal.Constructor(Vector3(2336.573f, 5529.179f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(181, Terminal.Constructor(Vector3(2336.607f, 5531.11f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(182, Terminal.Constructor(Vector3(2336.64f, 5533.018f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(183, Terminal.Constructor(Vector3(2336.762f, 5539.986f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(184, Terminal.Constructor(Vector3(2336.795f, 5541.917f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(185, Terminal.Constructor(Vector3(2336.829f, 5543.823f, 67.70142f), cert_terminal), owning_building_guid = 1)
      LocalObject(855, Terminal.Constructor(Vector3(2182.052f, 5537.723f, 68.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(856, Terminal.Constructor(Vector3(2182.129f, 5540.919f, 68.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(857, Terminal.Constructor(Vector3(2185.942f, 5537.654f, 68.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(858, Terminal.Constructor(Vector3(2186.143f, 5540.85f, 68.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(859, Terminal.Constructor(Vector3(2200.376f, 5532.267f, 78.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(860, Terminal.Constructor(Vector3(2200.427f, 5535.188f, 78.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(861, Terminal.Constructor(Vector3(2200.554f, 5542.837f, 78.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(862, Terminal.Constructor(Vector3(2200.604f, 5545.738f, 78.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(863, Terminal.Constructor(Vector3(2248.39f, 5587.958f, 83.99442f), order_terminal), owning_building_guid = 1)
      LocalObject(864, Terminal.Constructor(Vector3(2250.87f, 5587.915f, 83.99442f), order_terminal), owning_building_guid = 1)
      LocalObject(865, Terminal.Constructor(Vector3(2266.868f, 5587.636f, 83.99442f), order_terminal), owning_building_guid = 1)
      LocalObject(866, Terminal.Constructor(Vector3(2269.257f, 5587.594f, 83.99442f), order_terminal), owning_building_guid = 1)
      LocalObject(867, Terminal.Constructor(Vector3(2315.381f, 5530.233f, 78.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(868, Terminal.Constructor(Vector3(2315.432f, 5533.134f, 78.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(869, Terminal.Constructor(Vector3(2315.563f, 5540.813f, 78.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(870, Terminal.Constructor(Vector3(2315.614f, 5543.733f, 78.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(871, Terminal.Constructor(Vector3(2329.869f, 5535.142f, 68.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(872, Terminal.Constructor(Vector3(2329.985f, 5538.336f, 68.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(873, Terminal.Constructor(Vector3(2333.883f, 5535.072f, 68.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(874, Terminal.Constructor(Vector3(2333.874f, 5538.268f, 68.99842f), order_terminal), owning_building_guid = 1)
      LocalObject(828, ProximityTerminal.Constructor(Vector3(2186.923f, 5516.382f, 67.69242f), medical_terminal), owning_building_guid = 1)
      LocalObject(829, ProximityTerminal.Constructor(Vector3(2187.742f, 5562.079f, 67.69242f), medical_terminal), owning_building_guid = 1)
      LocalObject(830, ProximityTerminal.Constructor(Vector3(2328.283f, 5513.915f, 67.69242f), medical_terminal), owning_building_guid = 1)
      LocalObject(831, ProximityTerminal.Constructor(Vector3(2329.019f, 5559.613f, 67.69242f), medical_terminal), owning_building_guid = 1)
      LocalObject(526, ImplantTerminalMech.Constructor(Vector3(2193.333f, 5517.411f, 67.70142f)), owning_building_guid = 1)
      LocalObject(502, Terminal.Constructor(Vector3(2193.333f, 5517.428f, 67.70142f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(526, 502)
      LocalObject(527, ImplantTerminalMech.Constructor(Vector3(2194.066f, 5560.848f, 67.70142f)), owning_building_guid = 1)
      LocalObject(503, Terminal.Constructor(Vector3(2194.066f, 5560.83f, 67.70142f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(527, 503)
      LocalObject(528, ImplantTerminalMech.Constructor(Vector3(2198.635f, 5517.318f, 67.70142f)), owning_building_guid = 1)
      LocalObject(504, Terminal.Constructor(Vector3(2198.635f, 5517.336f, 67.70142f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(528, 504)
      LocalObject(529, ImplantTerminalMech.Constructor(Vector3(2199.472f, 5560.754f, 67.70142f)), owning_building_guid = 1)
      LocalObject(505, Terminal.Constructor(Vector3(2199.472f, 5560.736f, 67.70142f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(529, 505)
      LocalObject(530, ImplantTerminalMech.Constructor(Vector3(2316.539f, 5515.256f, 67.70142f)), owning_building_guid = 1)
      LocalObject(506, Terminal.Constructor(Vector3(2316.539f, 5515.274f, 67.70142f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(530, 506)
      LocalObject(531, ImplantTerminalMech.Constructor(Vector3(2317.292f, 5558.688f, 67.70142f)), owning_building_guid = 1)
      LocalObject(507, Terminal.Constructor(Vector3(2317.292f, 5558.669f, 67.70142f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(531, 507)
      LocalObject(532, ImplantTerminalMech.Constructor(Vector3(2321.946f, 5515.162f, 67.70142f)), owning_building_guid = 1)
      LocalObject(508, Terminal.Constructor(Vector3(2321.947f, 5515.18f, 67.70142f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(532, 508)
      LocalObject(533, ImplantTerminalMech.Constructor(Vector3(2322.594f, 5558.595f, 67.70142f)), owning_building_guid = 1)
      LocalObject(509, Terminal.Constructor(Vector3(2322.594f, 5558.577f, 67.70142f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(533, 509)
    }

    Building23()

    def Building23(): Unit = { // Name: Amerish_HART Type: orbital_building_nc GUID: 2, MapID: 23
      LocalBuilding("Amerish_HART", 2, 23, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4152f, 6070f, 43.87661f), orbital_building_nc)))
      LocalObject(382, Door.Constructor(Vector3(4071.802f, 6059.388f, 47.98261f)), owning_building_guid = 2)
      LocalObject(383, Door.Constructor(Vector3(4072.221f, 6083.384f, 47.98261f)), owning_building_guid = 2)
      LocalObject(386, Door.Constructor(Vector3(4231.778f, 6056.616f, 47.98261f)), owning_building_guid = 2)
      LocalObject(387, Door.Constructor(Vector3(4232.197f, 6080.612f, 47.98261f)), owning_building_guid = 2)
      LocalObject(406, Door.Constructor(Vector3(4090.349f, 6071.076f, 47.98261f)), owning_building_guid = 2)
      LocalObject(407, Door.Constructor(Vector3(4090.349f, 6071.076f, 57.98261f)), owning_building_guid = 2)
      LocalObject(408, Door.Constructor(Vector3(4136.84f, 6118.262f, 62.98261f)), owning_building_guid = 2)
      LocalObject(409, Door.Constructor(Vector3(4168.835f, 6117.704f, 62.98261f)), owning_building_guid = 2)
      LocalObject(410, Door.Constructor(Vector3(4213.63f, 6068.924f, 47.98261f)), owning_building_guid = 2)
      LocalObject(411, Door.Constructor(Vector3(4213.63f, 6068.924f, 57.98261f)), owning_building_guid = 2)
      LocalObject(474, Door.Constructor(Vector3(4096.358f, 6090.984f, 57.98261f)), owning_building_guid = 2)
      LocalObject(475, Door.Constructor(Vector3(4208.34f, 6089.01f, 57.98261f)), owning_building_guid = 2)
      LocalObject(486, Door.Constructor(Vector3(4095.799f, 6058.994f, 47.98261f)), owning_building_guid = 2)
      LocalObject(487, Door.Constructor(Vector3(4095.799f, 6058.994f, 57.98261f)), owning_building_guid = 2)
      LocalObject(488, Door.Constructor(Vector3(4096.218f, 6082.991f, 47.98261f)), owning_building_guid = 2)
      LocalObject(489, Door.Constructor(Vector3(4096.218f, 6082.991f, 57.98261f)), owning_building_guid = 2)
      LocalObject(490, Door.Constructor(Vector3(4207.782f, 6057.009f, 47.98261f)), owning_building_guid = 2)
      LocalObject(491, Door.Constructor(Vector3(4207.782f, 6057.009f, 57.98261f)), owning_building_guid = 2)
      LocalObject(492, Door.Constructor(Vector3(4208.201f, 6081.006f, 47.98261f)), owning_building_guid = 2)
      LocalObject(493, Door.Constructor(Vector3(4208.201f, 6081.006f, 57.98261f)), owning_building_guid = 2)
      LocalObject(740, Locker.Constructor(Vector3(4148.307f, 6121.887f, 60.98661f)), owning_building_guid = 2)
      LocalObject(741, Locker.Constructor(Vector3(4149.637f, 6121.864f, 60.98661f)), owning_building_guid = 2)
      LocalObject(742, Locker.Constructor(Vector3(4150.954f, 6121.841f, 60.98661f)), owning_building_guid = 2)
      LocalObject(743, Locker.Constructor(Vector3(4152.229f, 6121.819f, 60.98661f)), owning_building_guid = 2)
      LocalObject(744, Locker.Constructor(Vector3(4153.518f, 6121.796f, 60.98661f)), owning_building_guid = 2)
      LocalObject(745, Locker.Constructor(Vector3(4154.838f, 6121.773f, 60.98661f)), owning_building_guid = 2)
      LocalObject(746, Locker.Constructor(Vector3(4156.18f, 6121.75f, 60.98661f)), owning_building_guid = 2)
      LocalObject(747, Locker.Constructor(Vector3(4157.543f, 6121.726f, 60.98661f)), owning_building_guid = 2)
      LocalObject(186, Terminal.Constructor(Vector3(4073.098f, 6064.182f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(187, Terminal.Constructor(Vector3(4073.132f, 6066.089f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(188, Terminal.Constructor(Vector3(4073.166f, 6068.02f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(189, Terminal.Constructor(Vector3(4073.372f, 6075.042f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(190, Terminal.Constructor(Vector3(4073.405f, 6076.949f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(191, Terminal.Constructor(Vector3(4073.439f, 6078.879f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(192, Terminal.Constructor(Vector3(4084.288f, 6055.239f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(193, Terminal.Constructor(Vector3(4084.356f, 6059.142f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(194, Terminal.Constructor(Vector3(4084.423f, 6062.979f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(195, Terminal.Constructor(Vector3(4084.786f, 6079.615f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(196, Terminal.Constructor(Vector3(4084.853f, 6083.493f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(197, Terminal.Constructor(Vector3(4084.921f, 6087.364f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(198, Terminal.Constructor(Vector3(4219.091f, 6052.695f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(199, Terminal.Constructor(Vector3(4219.159f, 6056.567f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(200, Terminal.Constructor(Vector3(4219.227f, 6060.444f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(201, Terminal.Constructor(Vector3(4219.504f, 6077.026f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(202, Terminal.Constructor(Vector3(4219.571f, 6080.863f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(203, Terminal.Constructor(Vector3(4219.639f, 6084.766f, 56.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(204, Terminal.Constructor(Vector3(4230.573f, 6061.179f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(205, Terminal.Constructor(Vector3(4230.607f, 6063.11f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(206, Terminal.Constructor(Vector3(4230.64f, 6065.018f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(207, Terminal.Constructor(Vector3(4230.762f, 6071.986f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(208, Terminal.Constructor(Vector3(4230.795f, 6073.917f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(209, Terminal.Constructor(Vector3(4230.829f, 6075.823f, 46.37661f), cert_terminal), owning_building_guid = 2)
      LocalObject(905, Terminal.Constructor(Vector3(4076.052f, 6069.723f, 47.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(906, Terminal.Constructor(Vector3(4076.129f, 6072.919f, 47.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(907, Terminal.Constructor(Vector3(4079.942f, 6069.654f, 47.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(908, Terminal.Constructor(Vector3(4080.143f, 6072.85f, 47.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(909, Terminal.Constructor(Vector3(4094.376f, 6064.267f, 57.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(910, Terminal.Constructor(Vector3(4094.427f, 6067.188f, 57.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(911, Terminal.Constructor(Vector3(4094.554f, 6074.837f, 57.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(912, Terminal.Constructor(Vector3(4094.604f, 6077.738f, 57.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(913, Terminal.Constructor(Vector3(4142.391f, 6119.958f, 62.66961f), order_terminal), owning_building_guid = 2)
      LocalObject(914, Terminal.Constructor(Vector3(4144.87f, 6119.915f, 62.66961f), order_terminal), owning_building_guid = 2)
      LocalObject(921, Terminal.Constructor(Vector3(4160.868f, 6119.636f, 62.66961f), order_terminal), owning_building_guid = 2)
      LocalObject(925, Terminal.Constructor(Vector3(4163.257f, 6119.594f, 62.66961f), order_terminal), owning_building_guid = 2)
      LocalObject(926, Terminal.Constructor(Vector3(4209.381f, 6062.233f, 57.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(927, Terminal.Constructor(Vector3(4209.432f, 6065.134f, 57.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(928, Terminal.Constructor(Vector3(4209.563f, 6072.813f, 57.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(929, Terminal.Constructor(Vector3(4209.614f, 6075.733f, 57.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(930, Terminal.Constructor(Vector3(4223.869f, 6067.142f, 47.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(931, Terminal.Constructor(Vector3(4223.985f, 6070.336f, 47.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(935, Terminal.Constructor(Vector3(4227.882f, 6067.072f, 47.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(936, Terminal.Constructor(Vector3(4227.874f, 6070.268f, 47.67361f), order_terminal), owning_building_guid = 2)
      LocalObject(832, ProximityTerminal.Constructor(Vector3(4080.923f, 6048.382f, 46.36761f), medical_terminal), owning_building_guid = 2)
      LocalObject(833, ProximityTerminal.Constructor(Vector3(4081.742f, 6094.079f, 46.36761f), medical_terminal), owning_building_guid = 2)
      LocalObject(834, ProximityTerminal.Constructor(Vector3(4222.283f, 6045.915f, 46.36761f), medical_terminal), owning_building_guid = 2)
      LocalObject(835, ProximityTerminal.Constructor(Vector3(4223.019f, 6091.613f, 46.36761f), medical_terminal), owning_building_guid = 2)
      LocalObject(534, ImplantTerminalMech.Constructor(Vector3(4087.333f, 6049.411f, 46.37661f)), owning_building_guid = 2)
      LocalObject(510, Terminal.Constructor(Vector3(4087.333f, 6049.428f, 46.37661f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(534, 510)
      LocalObject(535, ImplantTerminalMech.Constructor(Vector3(4088.066f, 6092.848f, 46.37661f)), owning_building_guid = 2)
      LocalObject(511, Terminal.Constructor(Vector3(4088.066f, 6092.83f, 46.37661f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(535, 511)
      LocalObject(536, ImplantTerminalMech.Constructor(Vector3(4092.635f, 6049.318f, 46.37661f)), owning_building_guid = 2)
      LocalObject(512, Terminal.Constructor(Vector3(4092.635f, 6049.336f, 46.37661f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(536, 512)
      LocalObject(537, ImplantTerminalMech.Constructor(Vector3(4093.472f, 6092.754f, 46.37661f)), owning_building_guid = 2)
      LocalObject(513, Terminal.Constructor(Vector3(4093.472f, 6092.736f, 46.37661f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(537, 513)
      LocalObject(538, ImplantTerminalMech.Constructor(Vector3(4210.539f, 6047.256f, 46.37661f)), owning_building_guid = 2)
      LocalObject(514, Terminal.Constructor(Vector3(4210.539f, 6047.274f, 46.37661f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(538, 514)
      LocalObject(539, ImplantTerminalMech.Constructor(Vector3(4211.292f, 6090.688f, 46.37661f)), owning_building_guid = 2)
      LocalObject(515, Terminal.Constructor(Vector3(4211.292f, 6090.669f, 46.37661f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(539, 515)
      LocalObject(540, ImplantTerminalMech.Constructor(Vector3(4215.946f, 6047.162f, 46.37661f)), owning_building_guid = 2)
      LocalObject(516, Terminal.Constructor(Vector3(4215.946f, 6047.18f, 46.37661f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(540, 516)
      LocalObject(541, ImplantTerminalMech.Constructor(Vector3(4216.594f, 6090.595f, 46.37661f)), owning_building_guid = 2)
      LocalObject(517, Terminal.Constructor(Vector3(4216.594f, 6090.577f, 46.37661f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(541, 517)
    }

    Building4()

    def Building4(): Unit = { // Name: Esamir_HART Type: orbital_building_nc GUID: 3, MapID: 4
      LocalBuilding("Esamir_HART", 3, 4, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4816f, 3506f, 68.73806f), orbital_building_nc)))
      LocalObject(394, Door.Constructor(Vector3(4750.953f, 3457.91f, 72.84406f)), owning_building_guid = 3)
      LocalObject(395, Door.Constructor(Vector3(4767.924f, 3440.939f, 72.84406f)), owning_building_guid = 3)
      LocalObject(396, Door.Constructor(Vector3(4864.076f, 3571.061f, 72.84406f)), owning_building_guid = 3)
      LocalObject(397, Door.Constructor(Vector3(4881.047f, 3554.09f, 72.84406f)), owning_building_guid = 3)
      LocalObject(412, Door.Constructor(Vector3(4772.414f, 3462.414f, 72.84406f)), owning_building_guid = 3)
      LocalObject(413, Door.Constructor(Vector3(4772.414f, 3462.414f, 82.84406f)), owning_building_guid = 3)
      LocalObject(414, Door.Constructor(Vector3(4838.62f, 3460.752f, 87.84406f)), owning_building_guid = 3)
      LocalObject(415, Door.Constructor(Vector3(4859.6f, 3549.6f, 72.84406f)), owning_building_guid = 3)
      LocalObject(416, Door.Constructor(Vector3(4859.6f, 3549.6f, 82.84406f)), owning_building_guid = 3)
      LocalObject(417, Door.Constructor(Vector3(4861.248f, 3483.38f, 87.84406f)), owning_building_guid = 3)
      LocalObject(476, Door.Constructor(Vector3(4790.537f, 3452.267f, 82.84406f)), owning_building_guid = 3)
      LocalObject(477, Door.Constructor(Vector3(4869.747f, 3531.449f, 82.84406f)), owning_building_guid = 3)
      LocalObject(494, Door.Constructor(Vector3(4767.906f, 3474.898f, 72.84406f)), owning_building_guid = 3)
      LocalObject(495, Door.Constructor(Vector3(4767.906f, 3474.898f, 82.84406f)), owning_building_guid = 3)
      LocalObject(496, Door.Constructor(Vector3(4784.876f, 3457.927f, 72.84406f)), owning_building_guid = 3)
      LocalObject(497, Door.Constructor(Vector3(4784.876f, 3457.927f, 82.84406f)), owning_building_guid = 3)
      LocalObject(498, Door.Constructor(Vector3(4847.124f, 3554.073f, 72.84406f)), owning_building_guid = 3)
      LocalObject(499, Door.Constructor(Vector3(4847.124f, 3554.073f, 82.84406f)), owning_building_guid = 3)
      LocalObject(500, Door.Constructor(Vector3(4864.094f, 3537.102f, 72.84406f)), owning_building_guid = 3)
      LocalObject(501, Door.Constructor(Vector3(4864.094f, 3537.102f, 82.84406f)), owning_building_guid = 3)
      LocalObject(748, Locker.Constructor(Vector3(4849.358f, 3466.08f, 85.84806f)), owning_building_guid = 3)
      LocalObject(749, Locker.Constructor(Vector3(4850.322f, 3467.045f, 85.84806f)), owning_building_guid = 3)
      LocalObject(750, Locker.Constructor(Vector3(4851.271f, 3467.994f, 85.84806f)), owning_building_guid = 3)
      LocalObject(751, Locker.Constructor(Vector3(4852.205f, 3468.927f, 85.84806f)), owning_building_guid = 3)
      LocalObject(752, Locker.Constructor(Vector3(4853.116f, 3469.839f, 85.84806f)), owning_building_guid = 3)
      LocalObject(753, Locker.Constructor(Vector3(4854.018f, 3470.741f, 85.84806f)), owning_building_guid = 3)
      LocalObject(754, Locker.Constructor(Vector3(4854.95f, 3471.672f, 85.84806f)), owning_building_guid = 3)
      LocalObject(755, Locker.Constructor(Vector3(4855.89f, 3472.613f, 85.84806f)), owning_building_guid = 3)
      LocalObject(210, Terminal.Constructor(Vector3(4755.074f, 3455.607f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(211, Terminal.Constructor(Vector3(4756.439f, 3454.241f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(212, Terminal.Constructor(Vector3(4756.947f, 3469.759f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(213, Terminal.Constructor(Vector3(4757.788f, 3452.892f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(214, Terminal.Constructor(Vector3(4759.685f, 3467.021f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(215, Terminal.Constructor(Vector3(4762.427f, 3464.279f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(216, Terminal.Constructor(Vector3(4762.716f, 3447.964f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(217, Terminal.Constructor(Vector3(4764.082f, 3446.599f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(218, Terminal.Constructor(Vector3(4765.43f, 3445.25f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(219, Terminal.Constructor(Vector3(4774.162f, 3452.561f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(220, Terminal.Constructor(Vector3(4776.876f, 3449.847f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(221, Terminal.Constructor(Vector3(4779.636f, 3447.087f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(222, Terminal.Constructor(Vector3(4852.419f, 3564.961f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(223, Terminal.Constructor(Vector3(4855.179f, 3562.202f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(224, Terminal.Constructor(Vector3(4857.893f, 3559.488f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(225, Terminal.Constructor(Vector3(4866.625f, 3566.798f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(226, Terminal.Constructor(Vector3(4867.973f, 3565.45f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(227, Terminal.Constructor(Vector3(4869.338f, 3564.084f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(228, Terminal.Constructor(Vector3(4869.607f, 3547.671f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(229, Terminal.Constructor(Vector3(4872.35f, 3544.928f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(230, Terminal.Constructor(Vector3(4874.247f, 3559.058f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(231, Terminal.Constructor(Vector3(4875.088f, 3542.19f, 81.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(232, Terminal.Constructor(Vector3(4875.595f, 3557.709f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(233, Terminal.Constructor(Vector3(4876.96f, 3556.344f, 71.23806f), cert_terminal), owning_building_guid = 3)
      LocalObject(961, Terminal.Constructor(Vector3(4761.182f, 3453.448f, 72.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(962, Terminal.Constructor(Vector3(4763.486f, 3451.235f, 72.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(963, Terminal.Constructor(Vector3(4764.02f, 3456.287f, 72.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(964, Terminal.Constructor(Vector3(4766.237f, 3453.985f, 72.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(965, Terminal.Constructor(Vector3(4770.553f, 3470.119f, 82.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(966, Terminal.Constructor(Vector3(4772.604f, 3468.067f, 82.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(967, Terminal.Constructor(Vector3(4778.037f, 3462.639f, 82.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(968, Terminal.Constructor(Vector3(4780.103f, 3460.573f, 82.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(969, Terminal.Constructor(Vector3(4843.855f, 3463.451f, 87.53106f), order_terminal), owning_building_guid = 3)
      LocalObject(970, Terminal.Constructor(Vector3(4845.545f, 3465.141f, 87.53106f), order_terminal), owning_building_guid = 3)
      LocalObject(971, Terminal.Constructor(Vector3(4851.905f, 3551.434f, 82.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(972, Terminal.Constructor(Vector3(4853.97f, 3549.368f, 82.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(973, Terminal.Constructor(Vector3(4856.859f, 3476.455f, 87.53106f), order_terminal), owning_building_guid = 3)
      LocalObject(974, Terminal.Constructor(Vector3(4858.612f, 3478.208f, 87.53106f), order_terminal), owning_building_guid = 3)
      LocalObject(975, Terminal.Constructor(Vector3(4859.385f, 3543.963f, 82.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(976, Terminal.Constructor(Vector3(4861.437f, 3541.912f, 82.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(977, Terminal.Constructor(Vector3(4865.807f, 3558.074f, 72.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(978, Terminal.Constructor(Vector3(4867.966f, 3555.71f, 72.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(979, Terminal.Constructor(Vector3(4868.558f, 3560.825f, 72.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(980, Terminal.Constructor(Vector3(4870.804f, 3558.549f, 72.53506f), order_terminal), owning_building_guid = 3)
      LocalObject(836, ProximityTerminal.Constructor(Vector3(4749.852f, 3472.173f, 71.22906f), medical_terminal), owning_building_guid = 3)
      LocalObject(837, ProximityTerminal.Constructor(Vector3(4782.213f, 3439.9f, 71.22906f), medical_terminal), owning_building_guid = 3)
      LocalObject(838, ProximityTerminal.Constructor(Vector3(4849.824f, 3572.146f, 71.22906f), medical_terminal), owning_building_guid = 3)
      LocalObject(839, ProximityTerminal.Constructor(Vector3(4882.126f, 3539.812f, 71.22906f), medical_terminal), owning_building_guid = 3)
      LocalObject(542, ImplantTerminalMech.Constructor(Vector3(4755.15f, 3475.866f, 71.23806f)), owning_building_guid = 3)
      LocalObject(518, Terminal.Constructor(Vector3(4755.164f, 3475.853f, 71.23806f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(542, 518)
      LocalObject(543, ImplantTerminalMech.Constructor(Vector3(4758.975f, 3479.69f, 71.23806f)), owning_building_guid = 3)
      LocalObject(519, Terminal.Constructor(Vector3(4758.987f, 3479.677f, 71.23806f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(543, 519)
      LocalObject(544, ImplantTerminalMech.Constructor(Vector3(4785.944f, 3445.229f, 71.23806f)), owning_building_guid = 3)
      LocalObject(520, Terminal.Constructor(Vector3(4785.931f, 3445.241f, 71.23806f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(544, 520)
      LocalObject(545, ImplantTerminalMech.Constructor(Vector3(4789.693f, 3448.978f, 71.23806f)), owning_building_guid = 3)
      LocalObject(521, Terminal.Constructor(Vector3(4789.681f, 3448.991f, 71.23806f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(545, 521)
      LocalObject(546, ImplantTerminalMech.Constructor(Vector3(4842.361f, 3563.071f, 71.23806f)), owning_building_guid = 3)
      LocalObject(522, Terminal.Constructor(Vector3(4842.374f, 3563.058f, 71.23806f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(546, 522)
      LocalObject(547, ImplantTerminalMech.Constructor(Vector3(4846.111f, 3566.82f, 71.23806f)), owning_building_guid = 3)
      LocalObject(523, Terminal.Constructor(Vector3(4846.124f, 3566.808f, 71.23806f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(547, 523)
      LocalObject(548, ImplantTerminalMech.Constructor(Vector3(4873.024f, 3532.295f, 71.23806f)), owning_building_guid = 3)
      LocalObject(524, Terminal.Constructor(Vector3(4873.012f, 3532.308f, 71.23806f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(548, 524)
      LocalObject(549, ImplantTerminalMech.Constructor(Vector3(4876.848f, 3536.118f, 71.23806f)), owning_building_guid = 3)
      LocalObject(525, Terminal.Constructor(Vector3(4876.835f, 3536.131f, 71.23806f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(549, 525)
    }

    Building50()

    def Building50(): Unit = { // Name: nc_SW_Cyssor_Warpgate_Tower Type: tower_a GUID: 28, MapID: 50
      LocalBuilding("nc_SW_Cyssor_Warpgate_Tower", 28, 50, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(1980f, 5718f, 55.22141f), tower_a)))
      LocalObject(1094, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 28)
      LocalObject(316, Door.Constructor(Vector3(1992f, 5710f, 56.74241f)), owning_building_guid = 28)
      LocalObject(317, Door.Constructor(Vector3(1992f, 5710f, 76.74141f)), owning_building_guid = 28)
      LocalObject(318, Door.Constructor(Vector3(1992f, 5726f, 56.74241f)), owning_building_guid = 28)
      LocalObject(319, Door.Constructor(Vector3(1992f, 5726f, 76.74141f)), owning_building_guid = 28)
      LocalObject(1108, Door.Constructor(Vector3(1991.146f, 5706.794f, 46.55741f)), owning_building_guid = 28)
      LocalObject(1109, Door.Constructor(Vector3(1991.146f, 5723.204f, 46.55741f)), owning_building_guid = 28)
      LocalObject(552, IFFLock.Constructor(Vector3(1989.957f, 5726.811f, 56.68241f), Vector3(0, 0, 0)), owning_building_guid = 28, door_guid = 318)
      LocalObject(553, IFFLock.Constructor(Vector3(1989.957f, 5726.811f, 76.68241f), Vector3(0, 0, 0)), owning_building_guid = 28, door_guid = 319)
      LocalObject(556, IFFLock.Constructor(Vector3(1994.047f, 5709.189f, 56.68241f), Vector3(0, 0, 180)), owning_building_guid = 28, door_guid = 316)
      LocalObject(557, IFFLock.Constructor(Vector3(1994.047f, 5709.189f, 76.68241f), Vector3(0, 0, 180)), owning_building_guid = 28, door_guid = 317)
      LocalObject(612, Locker.Constructor(Vector3(1995.716f, 5702.963f, 45.21541f)), owning_building_guid = 28)
      LocalObject(613, Locker.Constructor(Vector3(1995.751f, 5724.835f, 45.21541f)), owning_building_guid = 28)
      LocalObject(614, Locker.Constructor(Vector3(1997.053f, 5702.963f, 45.21541f)), owning_building_guid = 28)
      LocalObject(615, Locker.Constructor(Vector3(1997.088f, 5724.835f, 45.21541f)), owning_building_guid = 28)
      LocalObject(620, Locker.Constructor(Vector3(1999.741f, 5702.963f, 45.21541f)), owning_building_guid = 28)
      LocalObject(621, Locker.Constructor(Vector3(1999.741f, 5724.835f, 45.21541f)), owning_building_guid = 28)
      LocalObject(622, Locker.Constructor(Vector3(2001.143f, 5702.963f, 45.21541f)), owning_building_guid = 28)
      LocalObject(623, Locker.Constructor(Vector3(2001.143f, 5724.835f, 45.21541f)), owning_building_guid = 28)
      LocalObject(846, Terminal.Constructor(Vector3(2001.445f, 5708.129f, 46.55341f), order_terminal), owning_building_guid = 28)
      LocalObject(847, Terminal.Constructor(Vector3(2001.445f, 5713.853f, 46.55341f), order_terminal), owning_building_guid = 28)
      LocalObject(848, Terminal.Constructor(Vector3(2001.445f, 5719.234f, 46.55341f), order_terminal), owning_building_guid = 28)
      LocalObject(1069, SpawnTube.Constructor(Vector3(1990.706f, 5705.742f, 44.70341f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 28)
      LocalObject(1070, SpawnTube.Constructor(Vector3(1990.706f, 5722.152f, 44.70341f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 28)
      LocalObject(713, FacilityTurret.Constructor(Vector3(1967.32f, 5705.295f, 74.16341f), manned_turret), owning_building_guid = 28)
      TurretToWeapon(713, 5000)
      LocalObject(715, FacilityTurret.Constructor(Vector3(2002.647f, 5730.707f, 74.16341f), manned_turret), owning_building_guid = 28)
      TurretToWeapon(715, 5001)
      LocalObject(1029, Painbox.Constructor(Vector3(1985.235f, 5711.803f, 46.72051f), painbox_radius_continuous), owning_building_guid = 28)
      LocalObject(1032, Painbox.Constructor(Vector3(1996.889f, 5720.086f, 45.32141f), painbox_radius_continuous), owning_building_guid = 28)
      LocalObject(1033, Painbox.Constructor(Vector3(1996.975f, 5708.223f, 45.32141f), painbox_radius_continuous), owning_building_guid = 28)
    }

    Building54()

    def Building54(): Unit = { // Name: nc_Far_Cyssor_Tower Type: tower_a GUID: 29, MapID: 54
      LocalBuilding("nc_Far_Cyssor_Tower", 29, 54, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(2940f, 5192f, 47.96524f), tower_a)))
      LocalObject(1096, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 29)
      LocalObject(326, Door.Constructor(Vector3(2952f, 5184f, 49.48624f)), owning_building_guid = 29)
      LocalObject(327, Door.Constructor(Vector3(2952f, 5184f, 69.48524f)), owning_building_guid = 29)
      LocalObject(328, Door.Constructor(Vector3(2952f, 5200f, 49.48624f)), owning_building_guid = 29)
      LocalObject(329, Door.Constructor(Vector3(2952f, 5200f, 69.48524f)), owning_building_guid = 29)
      LocalObject(1112, Door.Constructor(Vector3(2951.146f, 5180.794f, 39.30124f)), owning_building_guid = 29)
      LocalObject(1113, Door.Constructor(Vector3(2951.146f, 5197.204f, 39.30124f)), owning_building_guid = 29)
      LocalObject(564, IFFLock.Constructor(Vector3(2949.957f, 5200.811f, 49.42624f), Vector3(0, 0, 0)), owning_building_guid = 29, door_guid = 328)
      LocalObject(565, IFFLock.Constructor(Vector3(2949.957f, 5200.811f, 69.42624f), Vector3(0, 0, 0)), owning_building_guid = 29, door_guid = 329)
      LocalObject(566, IFFLock.Constructor(Vector3(2954.047f, 5183.189f, 49.42624f), Vector3(0, 0, 180)), owning_building_guid = 29, door_guid = 326)
      LocalObject(567, IFFLock.Constructor(Vector3(2954.047f, 5183.189f, 69.42624f), Vector3(0, 0, 180)), owning_building_guid = 29, door_guid = 327)
      LocalObject(632, Locker.Constructor(Vector3(2955.716f, 5176.963f, 37.95924f)), owning_building_guid = 29)
      LocalObject(633, Locker.Constructor(Vector3(2955.751f, 5198.835f, 37.95924f)), owning_building_guid = 29)
      LocalObject(634, Locker.Constructor(Vector3(2957.053f, 5176.963f, 37.95924f)), owning_building_guid = 29)
      LocalObject(635, Locker.Constructor(Vector3(2957.088f, 5198.835f, 37.95924f)), owning_building_guid = 29)
      LocalObject(636, Locker.Constructor(Vector3(2959.741f, 5176.963f, 37.95924f)), owning_building_guid = 29)
      LocalObject(637, Locker.Constructor(Vector3(2959.741f, 5198.835f, 37.95924f)), owning_building_guid = 29)
      LocalObject(638, Locker.Constructor(Vector3(2961.143f, 5176.963f, 37.95924f)), owning_building_guid = 29)
      LocalObject(639, Locker.Constructor(Vector3(2961.143f, 5198.835f, 37.95924f)), owning_building_guid = 29)
      LocalObject(890, Terminal.Constructor(Vector3(2961.445f, 5182.129f, 39.29724f), order_terminal), owning_building_guid = 29)
      LocalObject(891, Terminal.Constructor(Vector3(2961.445f, 5187.853f, 39.29724f), order_terminal), owning_building_guid = 29)
      LocalObject(892, Terminal.Constructor(Vector3(2961.445f, 5193.234f, 39.29724f), order_terminal), owning_building_guid = 29)
      LocalObject(1073, SpawnTube.Constructor(Vector3(2950.706f, 5179.742f, 37.44724f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 29)
      LocalObject(1074, SpawnTube.Constructor(Vector3(2950.706f, 5196.152f, 37.44724f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 29)
      LocalObject(716, FacilityTurret.Constructor(Vector3(2927.32f, 5179.295f, 66.90723f), manned_turret), owning_building_guid = 29)
      TurretToWeapon(716, 5002)
      LocalObject(717, FacilityTurret.Constructor(Vector3(2962.647f, 5204.707f, 66.90723f), manned_turret), owning_building_guid = 29)
      TurretToWeapon(717, 5003)
      LocalObject(1037, Painbox.Constructor(Vector3(2945.235f, 5185.803f, 39.46434f), painbox_radius_continuous), owning_building_guid = 29)
      LocalObject(1038, Painbox.Constructor(Vector3(2956.889f, 5194.086f, 38.06524f), painbox_radius_continuous), owning_building_guid = 29)
      LocalObject(1039, Painbox.Constructor(Vector3(2956.975f, 5182.223f, 38.06524f), painbox_radius_continuous), owning_building_guid = 29)
    }

    Building55()

    def Building55(): Unit = { // Name: nc_Far_Amerish_Tower Type: tower_a GUID: 30, MapID: 55
      LocalBuilding("nc_Far_Amerish_Tower", 30, 55, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4206f, 5296f, 67.51106f), tower_a)))
      LocalObject(1100, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 30)
      LocalObject(344, Door.Constructor(Vector3(4218f, 5288f, 69.03207f)), owning_building_guid = 30)
      LocalObject(345, Door.Constructor(Vector3(4218f, 5288f, 89.03107f)), owning_building_guid = 30)
      LocalObject(346, Door.Constructor(Vector3(4218f, 5304f, 69.03207f)), owning_building_guid = 30)
      LocalObject(347, Door.Constructor(Vector3(4218f, 5304f, 89.03107f)), owning_building_guid = 30)
      LocalObject(1120, Door.Constructor(Vector3(4217.146f, 5284.794f, 58.84706f)), owning_building_guid = 30)
      LocalObject(1121, Door.Constructor(Vector3(4217.146f, 5301.204f, 58.84706f)), owning_building_guid = 30)
      LocalObject(582, IFFLock.Constructor(Vector3(4215.957f, 5304.811f, 68.97206f), Vector3(0, 0, 0)), owning_building_guid = 30, door_guid = 346)
      LocalObject(583, IFFLock.Constructor(Vector3(4215.957f, 5304.811f, 88.97206f), Vector3(0, 0, 0)), owning_building_guid = 30, door_guid = 347)
      LocalObject(584, IFFLock.Constructor(Vector3(4220.047f, 5287.189f, 68.97206f), Vector3(0, 0, 180)), owning_building_guid = 30, door_guid = 344)
      LocalObject(585, IFFLock.Constructor(Vector3(4220.047f, 5287.189f, 88.97206f), Vector3(0, 0, 180)), owning_building_guid = 30, door_guid = 345)
      LocalObject(664, Locker.Constructor(Vector3(4221.716f, 5280.963f, 57.50506f)), owning_building_guid = 30)
      LocalObject(665, Locker.Constructor(Vector3(4221.751f, 5302.835f, 57.50506f)), owning_building_guid = 30)
      LocalObject(666, Locker.Constructor(Vector3(4223.053f, 5280.963f, 57.50506f)), owning_building_guid = 30)
      LocalObject(667, Locker.Constructor(Vector3(4223.088f, 5302.835f, 57.50506f)), owning_building_guid = 30)
      LocalObject(668, Locker.Constructor(Vector3(4225.741f, 5280.963f, 57.50506f)), owning_building_guid = 30)
      LocalObject(669, Locker.Constructor(Vector3(4225.741f, 5302.835f, 57.50506f)), owning_building_guid = 30)
      LocalObject(670, Locker.Constructor(Vector3(4227.143f, 5280.963f, 57.50506f)), owning_building_guid = 30)
      LocalObject(671, Locker.Constructor(Vector3(4227.143f, 5302.835f, 57.50506f)), owning_building_guid = 30)
      LocalObject(932, Terminal.Constructor(Vector3(4227.445f, 5286.129f, 58.84306f), order_terminal), owning_building_guid = 30)
      LocalObject(933, Terminal.Constructor(Vector3(4227.445f, 5291.853f, 58.84306f), order_terminal), owning_building_guid = 30)
      LocalObject(934, Terminal.Constructor(Vector3(4227.445f, 5297.234f, 58.84306f), order_terminal), owning_building_guid = 30)
      LocalObject(1081, SpawnTube.Constructor(Vector3(4216.706f, 5283.742f, 56.99306f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 30)
      LocalObject(1082, SpawnTube.Constructor(Vector3(4216.706f, 5300.152f, 56.99306f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 30)
      LocalObject(722, FacilityTurret.Constructor(Vector3(4193.32f, 5283.295f, 86.45306f), manned_turret), owning_building_guid = 30)
      TurretToWeapon(722, 5004)
      LocalObject(724, FacilityTurret.Constructor(Vector3(4228.647f, 5308.707f, 86.45306f), manned_turret), owning_building_guid = 30)
      TurretToWeapon(724, 5005)
      LocalObject(1049, Painbox.Constructor(Vector3(4211.235f, 5289.803f, 59.01016f), painbox_radius_continuous), owning_building_guid = 30)
      LocalObject(1050, Painbox.Constructor(Vector3(4222.889f, 5298.086f, 57.61106f), painbox_radius_continuous), owning_building_guid = 30)
      LocalObject(1051, Painbox.Constructor(Vector3(4222.975f, 5286.223f, 57.61106f), painbox_radius_continuous), owning_building_guid = 30)
    }

    Building44()

    def Building44(): Unit = { // Name: nc_Far_Esamir_Tower Type: tower_a GUID: 31, MapID: 44
      LocalBuilding("nc_Far_Esamir_Tower", 31, 44, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4228f, 3982f, 63.52824f), tower_a)))
      LocalObject(1101, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 31)
      LocalObject(348, Door.Constructor(Vector3(4240f, 3974f, 65.04925f)), owning_building_guid = 31)
      LocalObject(349, Door.Constructor(Vector3(4240f, 3974f, 85.04825f)), owning_building_guid = 31)
      LocalObject(350, Door.Constructor(Vector3(4240f, 3990f, 65.04925f)), owning_building_guid = 31)
      LocalObject(351, Door.Constructor(Vector3(4240f, 3990f, 85.04825f)), owning_building_guid = 31)
      LocalObject(1122, Door.Constructor(Vector3(4239.146f, 3970.794f, 54.86424f)), owning_building_guid = 31)
      LocalObject(1123, Door.Constructor(Vector3(4239.146f, 3987.204f, 54.86424f)), owning_building_guid = 31)
      LocalObject(586, IFFLock.Constructor(Vector3(4237.957f, 3990.811f, 64.98924f), Vector3(0, 0, 0)), owning_building_guid = 31, door_guid = 350)
      LocalObject(587, IFFLock.Constructor(Vector3(4237.957f, 3990.811f, 84.98924f), Vector3(0, 0, 0)), owning_building_guid = 31, door_guid = 351)
      LocalObject(588, IFFLock.Constructor(Vector3(4242.047f, 3973.189f, 64.98924f), Vector3(0, 0, 180)), owning_building_guid = 31, door_guid = 348)
      LocalObject(589, IFFLock.Constructor(Vector3(4242.047f, 3973.189f, 84.98924f), Vector3(0, 0, 180)), owning_building_guid = 31, door_guid = 349)
      LocalObject(672, Locker.Constructor(Vector3(4243.716f, 3966.963f, 53.52224f)), owning_building_guid = 31)
      LocalObject(673, Locker.Constructor(Vector3(4243.751f, 3988.835f, 53.52224f)), owning_building_guid = 31)
      LocalObject(674, Locker.Constructor(Vector3(4245.053f, 3966.963f, 53.52224f)), owning_building_guid = 31)
      LocalObject(675, Locker.Constructor(Vector3(4245.088f, 3988.835f, 53.52224f)), owning_building_guid = 31)
      LocalObject(676, Locker.Constructor(Vector3(4247.741f, 3966.963f, 53.52224f)), owning_building_guid = 31)
      LocalObject(677, Locker.Constructor(Vector3(4247.741f, 3988.835f, 53.52224f)), owning_building_guid = 31)
      LocalObject(678, Locker.Constructor(Vector3(4249.143f, 3966.963f, 53.52224f)), owning_building_guid = 31)
      LocalObject(679, Locker.Constructor(Vector3(4249.143f, 3988.835f, 53.52224f)), owning_building_guid = 31)
      LocalObject(937, Terminal.Constructor(Vector3(4249.445f, 3972.129f, 54.86024f), order_terminal), owning_building_guid = 31)
      LocalObject(938, Terminal.Constructor(Vector3(4249.445f, 3977.853f, 54.86024f), order_terminal), owning_building_guid = 31)
      LocalObject(939, Terminal.Constructor(Vector3(4249.445f, 3983.234f, 54.86024f), order_terminal), owning_building_guid = 31)
      LocalObject(1083, SpawnTube.Constructor(Vector3(4238.706f, 3969.742f, 53.01025f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 31)
      LocalObject(1084, SpawnTube.Constructor(Vector3(4238.706f, 3986.152f, 53.01025f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 31)
      LocalObject(723, FacilityTurret.Constructor(Vector3(4215.32f, 3969.295f, 82.47025f), manned_turret), owning_building_guid = 31)
      TurretToWeapon(723, 5006)
      LocalObject(725, FacilityTurret.Constructor(Vector3(4250.647f, 3994.707f, 82.47025f), manned_turret), owning_building_guid = 31)
      TurretToWeapon(725, 5007)
      LocalObject(1052, Painbox.Constructor(Vector3(4233.235f, 3975.803f, 55.02734f), painbox_radius_continuous), owning_building_guid = 31)
      LocalObject(1053, Painbox.Constructor(Vector3(4244.889f, 3984.086f, 53.62824f), painbox_radius_continuous), owning_building_guid = 31)
      LocalObject(1054, Painbox.Constructor(Vector3(4244.975f, 3972.223f, 53.62824f), painbox_radius_continuous), owning_building_guid = 31)
    }

    Building48()

    def Building48(): Unit = { // Name: nc_SE_Amerish_Warpgate_Tower Type: tower_a GUID: 32, MapID: 48
      LocalBuilding("nc_SE_Amerish_Warpgate_Tower", 32, 48, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4458f, 6256f, 43.92254f), tower_a)))
      LocalObject(1102, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 32)
      LocalObject(352, Door.Constructor(Vector3(4470f, 6248f, 45.44354f)), owning_building_guid = 32)
      LocalObject(353, Door.Constructor(Vector3(4470f, 6248f, 65.44254f)), owning_building_guid = 32)
      LocalObject(354, Door.Constructor(Vector3(4470f, 6264f, 45.44354f)), owning_building_guid = 32)
      LocalObject(355, Door.Constructor(Vector3(4470f, 6264f, 65.44254f)), owning_building_guid = 32)
      LocalObject(1124, Door.Constructor(Vector3(4469.146f, 6244.794f, 35.25854f)), owning_building_guid = 32)
      LocalObject(1125, Door.Constructor(Vector3(4469.146f, 6261.204f, 35.25854f)), owning_building_guid = 32)
      LocalObject(590, IFFLock.Constructor(Vector3(4467.957f, 6264.811f, 45.38354f), Vector3(0, 0, 0)), owning_building_guid = 32, door_guid = 354)
      LocalObject(591, IFFLock.Constructor(Vector3(4467.957f, 6264.811f, 65.38354f), Vector3(0, 0, 0)), owning_building_guid = 32, door_guid = 355)
      LocalObject(592, IFFLock.Constructor(Vector3(4472.047f, 6247.189f, 45.38354f), Vector3(0, 0, 180)), owning_building_guid = 32, door_guid = 352)
      LocalObject(593, IFFLock.Constructor(Vector3(4472.047f, 6247.189f, 65.38354f), Vector3(0, 0, 180)), owning_building_guid = 32, door_guid = 353)
      LocalObject(680, Locker.Constructor(Vector3(4473.716f, 6240.963f, 33.91654f)), owning_building_guid = 32)
      LocalObject(681, Locker.Constructor(Vector3(4473.751f, 6262.835f, 33.91654f)), owning_building_guid = 32)
      LocalObject(682, Locker.Constructor(Vector3(4475.053f, 6240.963f, 33.91654f)), owning_building_guid = 32)
      LocalObject(683, Locker.Constructor(Vector3(4475.088f, 6262.835f, 33.91654f)), owning_building_guid = 32)
      LocalObject(684, Locker.Constructor(Vector3(4477.741f, 6240.963f, 33.91654f)), owning_building_guid = 32)
      LocalObject(685, Locker.Constructor(Vector3(4477.741f, 6262.835f, 33.91654f)), owning_building_guid = 32)
      LocalObject(686, Locker.Constructor(Vector3(4479.143f, 6240.963f, 33.91654f)), owning_building_guid = 32)
      LocalObject(687, Locker.Constructor(Vector3(4479.143f, 6262.835f, 33.91654f)), owning_building_guid = 32)
      LocalObject(946, Terminal.Constructor(Vector3(4479.445f, 6246.129f, 35.25454f), order_terminal), owning_building_guid = 32)
      LocalObject(947, Terminal.Constructor(Vector3(4479.445f, 6251.853f, 35.25454f), order_terminal), owning_building_guid = 32)
      LocalObject(948, Terminal.Constructor(Vector3(4479.445f, 6257.234f, 35.25454f), order_terminal), owning_building_guid = 32)
      LocalObject(1085, SpawnTube.Constructor(Vector3(4468.706f, 6243.742f, 33.40454f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 32)
      LocalObject(1086, SpawnTube.Constructor(Vector3(4468.706f, 6260.152f, 33.40454f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 32)
      LocalObject(726, FacilityTurret.Constructor(Vector3(4445.32f, 6243.295f, 62.86454f), manned_turret), owning_building_guid = 32)
      TurretToWeapon(726, 5008)
      LocalObject(727, FacilityTurret.Constructor(Vector3(4480.647f, 6268.707f, 62.86454f), manned_turret), owning_building_guid = 32)
      TurretToWeapon(727, 5009)
      LocalObject(1055, Painbox.Constructor(Vector3(4463.235f, 6249.803f, 35.42164f), painbox_radius_continuous), owning_building_guid = 32)
      LocalObject(1056, Painbox.Constructor(Vector3(4474.889f, 6258.086f, 34.02254f), painbox_radius_continuous), owning_building_guid = 32)
      LocalObject(1057, Painbox.Constructor(Vector3(4474.975f, 6246.223f, 34.02254f), painbox_radius_continuous), owning_building_guid = 32)
    }

    Building46()

    def Building46(): Unit = { // Name: nc_W_Esamir_Warpgate_Tower Type: tower_a GUID: 33, MapID: 46
      LocalBuilding("nc_W_Esamir_Warpgate_Tower", 33, 46, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4886f, 3164f, 48.7764f), tower_a)))
      LocalObject(1103, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 33)
      LocalObject(356, Door.Constructor(Vector3(4898f, 3156f, 50.2974f)), owning_building_guid = 33)
      LocalObject(357, Door.Constructor(Vector3(4898f, 3156f, 70.2964f)), owning_building_guid = 33)
      LocalObject(358, Door.Constructor(Vector3(4898f, 3172f, 50.2974f)), owning_building_guid = 33)
      LocalObject(359, Door.Constructor(Vector3(4898f, 3172f, 70.2964f)), owning_building_guid = 33)
      LocalObject(1126, Door.Constructor(Vector3(4897.146f, 3152.794f, 40.1124f)), owning_building_guid = 33)
      LocalObject(1127, Door.Constructor(Vector3(4897.146f, 3169.204f, 40.1124f)), owning_building_guid = 33)
      LocalObject(594, IFFLock.Constructor(Vector3(4895.957f, 3172.811f, 50.2374f), Vector3(0, 0, 0)), owning_building_guid = 33, door_guid = 358)
      LocalObject(595, IFFLock.Constructor(Vector3(4895.957f, 3172.811f, 70.2374f), Vector3(0, 0, 0)), owning_building_guid = 33, door_guid = 359)
      LocalObject(596, IFFLock.Constructor(Vector3(4900.047f, 3155.189f, 50.2374f), Vector3(0, 0, 180)), owning_building_guid = 33, door_guid = 356)
      LocalObject(597, IFFLock.Constructor(Vector3(4900.047f, 3155.189f, 70.2374f), Vector3(0, 0, 180)), owning_building_guid = 33, door_guid = 357)
      LocalObject(688, Locker.Constructor(Vector3(4901.716f, 3148.963f, 38.7704f)), owning_building_guid = 33)
      LocalObject(689, Locker.Constructor(Vector3(4901.751f, 3170.835f, 38.7704f)), owning_building_guid = 33)
      LocalObject(690, Locker.Constructor(Vector3(4903.053f, 3148.963f, 38.7704f)), owning_building_guid = 33)
      LocalObject(691, Locker.Constructor(Vector3(4903.088f, 3170.835f, 38.7704f)), owning_building_guid = 33)
      LocalObject(692, Locker.Constructor(Vector3(4905.741f, 3148.963f, 38.7704f)), owning_building_guid = 33)
      LocalObject(693, Locker.Constructor(Vector3(4905.741f, 3170.835f, 38.7704f)), owning_building_guid = 33)
      LocalObject(694, Locker.Constructor(Vector3(4907.143f, 3148.963f, 38.7704f)), owning_building_guid = 33)
      LocalObject(695, Locker.Constructor(Vector3(4907.143f, 3170.835f, 38.7704f)), owning_building_guid = 33)
      LocalObject(981, Terminal.Constructor(Vector3(4907.445f, 3154.129f, 40.1084f), order_terminal), owning_building_guid = 33)
      LocalObject(982, Terminal.Constructor(Vector3(4907.445f, 3159.853f, 40.1084f), order_terminal), owning_building_guid = 33)
      LocalObject(983, Terminal.Constructor(Vector3(4907.445f, 3165.234f, 40.1084f), order_terminal), owning_building_guid = 33)
      LocalObject(1087, SpawnTube.Constructor(Vector3(4896.706f, 3151.742f, 38.2584f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 33)
      LocalObject(1088, SpawnTube.Constructor(Vector3(4896.706f, 3168.152f, 38.2584f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 33)
      LocalObject(728, FacilityTurret.Constructor(Vector3(4873.32f, 3151.295f, 67.7184f), manned_turret), owning_building_guid = 33)
      TurretToWeapon(728, 5010)
      LocalObject(729, FacilityTurret.Constructor(Vector3(4908.647f, 3176.707f, 67.7184f), manned_turret), owning_building_guid = 33)
      TurretToWeapon(729, 5011)
      LocalObject(1058, Painbox.Constructor(Vector3(4891.235f, 3157.803f, 40.2755f), painbox_radius_continuous), owning_building_guid = 33)
      LocalObject(1059, Painbox.Constructor(Vector3(4902.889f, 3166.086f, 38.8764f), painbox_radius_continuous), owning_building_guid = 33)
      LocalObject(1060, Painbox.Constructor(Vector3(4902.975f, 3154.223f, 38.8764f), painbox_radius_continuous), owning_building_guid = 33)
    }

    Building49()

    def Building49(): Unit = { // Name: nc_SE_Cyssor_Warpgate_Tower Type: tower_b GUID: 34, MapID: 49
      LocalBuilding("nc_SE_Cyssor_Warpgate_Tower", 34, 49, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(2506f, 5776f, 63.62667f), tower_b)))
      LocalObject(1095, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 34)
      LocalObject(320, Door.Constructor(Vector3(2518f, 5768f, 65.14667f)), owning_building_guid = 34)
      LocalObject(321, Door.Constructor(Vector3(2518f, 5768f, 75.14667f)), owning_building_guid = 34)
      LocalObject(322, Door.Constructor(Vector3(2518f, 5768f, 95.14667f)), owning_building_guid = 34)
      LocalObject(323, Door.Constructor(Vector3(2518f, 5784f, 65.14667f)), owning_building_guid = 34)
      LocalObject(324, Door.Constructor(Vector3(2518f, 5784f, 75.14667f)), owning_building_guid = 34)
      LocalObject(325, Door.Constructor(Vector3(2518f, 5784f, 95.14667f)), owning_building_guid = 34)
      LocalObject(1110, Door.Constructor(Vector3(2517.147f, 5764.794f, 54.96267f)), owning_building_guid = 34)
      LocalObject(1111, Door.Constructor(Vector3(2517.147f, 5781.204f, 54.96267f)), owning_building_guid = 34)
      LocalObject(558, IFFLock.Constructor(Vector3(2515.957f, 5784.811f, 65.08767f), Vector3(0, 0, 0)), owning_building_guid = 34, door_guid = 323)
      LocalObject(559, IFFLock.Constructor(Vector3(2515.957f, 5784.811f, 75.08767f), Vector3(0, 0, 0)), owning_building_guid = 34, door_guid = 324)
      LocalObject(560, IFFLock.Constructor(Vector3(2515.957f, 5784.811f, 95.08767f), Vector3(0, 0, 0)), owning_building_guid = 34, door_guid = 325)
      LocalObject(561, IFFLock.Constructor(Vector3(2520.047f, 5767.189f, 65.08767f), Vector3(0, 0, 180)), owning_building_guid = 34, door_guid = 320)
      LocalObject(562, IFFLock.Constructor(Vector3(2520.047f, 5767.189f, 75.08767f), Vector3(0, 0, 180)), owning_building_guid = 34, door_guid = 321)
      LocalObject(563, IFFLock.Constructor(Vector3(2520.047f, 5767.189f, 95.08767f), Vector3(0, 0, 180)), owning_building_guid = 34, door_guid = 322)
      LocalObject(624, Locker.Constructor(Vector3(2521.716f, 5760.963f, 53.62067f)), owning_building_guid = 34)
      LocalObject(625, Locker.Constructor(Vector3(2521.751f, 5782.835f, 53.62067f)), owning_building_guid = 34)
      LocalObject(626, Locker.Constructor(Vector3(2523.053f, 5760.963f, 53.62067f)), owning_building_guid = 34)
      LocalObject(627, Locker.Constructor(Vector3(2523.088f, 5782.835f, 53.62067f)), owning_building_guid = 34)
      LocalObject(628, Locker.Constructor(Vector3(2525.741f, 5760.963f, 53.62067f)), owning_building_guid = 34)
      LocalObject(629, Locker.Constructor(Vector3(2525.741f, 5782.835f, 53.62067f)), owning_building_guid = 34)
      LocalObject(630, Locker.Constructor(Vector3(2527.143f, 5760.963f, 53.62067f)), owning_building_guid = 34)
      LocalObject(631, Locker.Constructor(Vector3(2527.143f, 5782.835f, 53.62067f)), owning_building_guid = 34)
      LocalObject(887, Terminal.Constructor(Vector3(2527.446f, 5766.129f, 54.95867f), order_terminal), owning_building_guid = 34)
      LocalObject(888, Terminal.Constructor(Vector3(2527.446f, 5771.853f, 54.95867f), order_terminal), owning_building_guid = 34)
      LocalObject(889, Terminal.Constructor(Vector3(2527.446f, 5777.234f, 54.95867f), order_terminal), owning_building_guid = 34)
      LocalObject(1071, SpawnTube.Constructor(Vector3(2516.706f, 5763.742f, 53.10867f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 34)
      LocalObject(1072, SpawnTube.Constructor(Vector3(2516.706f, 5780.152f, 53.10867f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 34)
      LocalObject(1034, Painbox.Constructor(Vector3(2511.493f, 5768.849f, 54.91607f), painbox_radius_continuous), owning_building_guid = 34)
      LocalObject(1035, Painbox.Constructor(Vector3(2523.127f, 5766.078f, 53.72667f), painbox_radius_continuous), owning_building_guid = 34)
      LocalObject(1036, Painbox.Constructor(Vector3(2523.259f, 5778.107f, 53.72667f), painbox_radius_continuous), owning_building_guid = 34)
    }

    Building47()

    def Building47(): Unit = { // Name: nc_SW_Amerish_Warpgate_Tower Type: tower_b GUID: 35, MapID: 47
      LocalBuilding("nc_SW_Amerish_Warpgate_Tower", 35, 47, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3844f, 5888f, 44.39456f), tower_b)))
      LocalObject(1098, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 35)
      LocalObject(334, Door.Constructor(Vector3(3856f, 5880f, 45.91456f)), owning_building_guid = 35)
      LocalObject(335, Door.Constructor(Vector3(3856f, 5880f, 55.91456f)), owning_building_guid = 35)
      LocalObject(336, Door.Constructor(Vector3(3856f, 5880f, 75.91457f)), owning_building_guid = 35)
      LocalObject(337, Door.Constructor(Vector3(3856f, 5896f, 45.91456f)), owning_building_guid = 35)
      LocalObject(338, Door.Constructor(Vector3(3856f, 5896f, 55.91456f)), owning_building_guid = 35)
      LocalObject(339, Door.Constructor(Vector3(3856f, 5896f, 75.91457f)), owning_building_guid = 35)
      LocalObject(1116, Door.Constructor(Vector3(3855.147f, 5876.794f, 35.73056f)), owning_building_guid = 35)
      LocalObject(1117, Door.Constructor(Vector3(3855.147f, 5893.204f, 35.73056f)), owning_building_guid = 35)
      LocalObject(572, IFFLock.Constructor(Vector3(3853.957f, 5896.811f, 45.85556f), Vector3(0, 0, 0)), owning_building_guid = 35, door_guid = 337)
      LocalObject(573, IFFLock.Constructor(Vector3(3853.957f, 5896.811f, 55.85556f), Vector3(0, 0, 0)), owning_building_guid = 35, door_guid = 338)
      LocalObject(574, IFFLock.Constructor(Vector3(3853.957f, 5896.811f, 75.85556f), Vector3(0, 0, 0)), owning_building_guid = 35, door_guid = 339)
      LocalObject(575, IFFLock.Constructor(Vector3(3858.047f, 5879.189f, 45.85556f), Vector3(0, 0, 180)), owning_building_guid = 35, door_guid = 334)
      LocalObject(576, IFFLock.Constructor(Vector3(3858.047f, 5879.189f, 55.85556f), Vector3(0, 0, 180)), owning_building_guid = 35, door_guid = 335)
      LocalObject(577, IFFLock.Constructor(Vector3(3858.047f, 5879.189f, 75.85556f), Vector3(0, 0, 180)), owning_building_guid = 35, door_guid = 336)
      LocalObject(648, Locker.Constructor(Vector3(3859.716f, 5872.963f, 34.38856f)), owning_building_guid = 35)
      LocalObject(649, Locker.Constructor(Vector3(3859.751f, 5894.835f, 34.38856f)), owning_building_guid = 35)
      LocalObject(650, Locker.Constructor(Vector3(3861.053f, 5872.963f, 34.38856f)), owning_building_guid = 35)
      LocalObject(651, Locker.Constructor(Vector3(3861.088f, 5894.835f, 34.38856f)), owning_building_guid = 35)
      LocalObject(652, Locker.Constructor(Vector3(3863.741f, 5872.963f, 34.38856f)), owning_building_guid = 35)
      LocalObject(653, Locker.Constructor(Vector3(3863.741f, 5894.835f, 34.38856f)), owning_building_guid = 35)
      LocalObject(654, Locker.Constructor(Vector3(3865.143f, 5872.963f, 34.38856f)), owning_building_guid = 35)
      LocalObject(655, Locker.Constructor(Vector3(3865.143f, 5894.835f, 34.38856f)), owning_building_guid = 35)
      LocalObject(896, Terminal.Constructor(Vector3(3865.446f, 5878.129f, 35.72656f), order_terminal), owning_building_guid = 35)
      LocalObject(897, Terminal.Constructor(Vector3(3865.446f, 5883.853f, 35.72656f), order_terminal), owning_building_guid = 35)
      LocalObject(898, Terminal.Constructor(Vector3(3865.446f, 5889.234f, 35.72656f), order_terminal), owning_building_guid = 35)
      LocalObject(1077, SpawnTube.Constructor(Vector3(3854.706f, 5875.742f, 33.87656f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 35)
      LocalObject(1078, SpawnTube.Constructor(Vector3(3854.706f, 5892.152f, 33.87656f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 35)
      LocalObject(1043, Painbox.Constructor(Vector3(3849.493f, 5880.849f, 35.68396f), painbox_radius_continuous), owning_building_guid = 35)
      LocalObject(1044, Painbox.Constructor(Vector3(3861.127f, 5878.078f, 34.49456f), painbox_radius_continuous), owning_building_guid = 35)
      LocalObject(1045, Painbox.Constructor(Vector3(3861.259f, 5890.107f, 34.49456f), painbox_radius_continuous), owning_building_guid = 35)
    }

    Building45()

    def Building45(): Unit = { // Name: nc_N_Esamir_Warpgate_Tower Type: tower_b GUID: 36, MapID: 45
      LocalBuilding("nc_N_Esamir_Warpgate_Tower", 36, 45, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(5020f, 3810f, 65.63448f), tower_b)))
      LocalObject(1104, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 36)
      LocalObject(360, Door.Constructor(Vector3(5032f, 3802f, 67.15448f)), owning_building_guid = 36)
      LocalObject(361, Door.Constructor(Vector3(5032f, 3802f, 77.15448f)), owning_building_guid = 36)
      LocalObject(362, Door.Constructor(Vector3(5032f, 3802f, 97.15448f)), owning_building_guid = 36)
      LocalObject(363, Door.Constructor(Vector3(5032f, 3818f, 67.15448f)), owning_building_guid = 36)
      LocalObject(364, Door.Constructor(Vector3(5032f, 3818f, 77.15448f)), owning_building_guid = 36)
      LocalObject(365, Door.Constructor(Vector3(5032f, 3818f, 97.15448f)), owning_building_guid = 36)
      LocalObject(1128, Door.Constructor(Vector3(5031.147f, 3798.794f, 56.97048f)), owning_building_guid = 36)
      LocalObject(1129, Door.Constructor(Vector3(5031.147f, 3815.204f, 56.97048f)), owning_building_guid = 36)
      LocalObject(598, IFFLock.Constructor(Vector3(5029.957f, 3818.811f, 67.09548f), Vector3(0, 0, 0)), owning_building_guid = 36, door_guid = 363)
      LocalObject(599, IFFLock.Constructor(Vector3(5029.957f, 3818.811f, 77.09548f), Vector3(0, 0, 0)), owning_building_guid = 36, door_guid = 364)
      LocalObject(600, IFFLock.Constructor(Vector3(5029.957f, 3818.811f, 97.09548f), Vector3(0, 0, 0)), owning_building_guid = 36, door_guid = 365)
      LocalObject(601, IFFLock.Constructor(Vector3(5034.047f, 3801.189f, 67.09548f), Vector3(0, 0, 180)), owning_building_guid = 36, door_guid = 360)
      LocalObject(602, IFFLock.Constructor(Vector3(5034.047f, 3801.189f, 77.09548f), Vector3(0, 0, 180)), owning_building_guid = 36, door_guid = 361)
      LocalObject(603, IFFLock.Constructor(Vector3(5034.047f, 3801.189f, 97.09548f), Vector3(0, 0, 180)), owning_building_guid = 36, door_guid = 362)
      LocalObject(696, Locker.Constructor(Vector3(5035.716f, 3794.963f, 55.62848f)), owning_building_guid = 36)
      LocalObject(697, Locker.Constructor(Vector3(5035.751f, 3816.835f, 55.62848f)), owning_building_guid = 36)
      LocalObject(698, Locker.Constructor(Vector3(5037.053f, 3794.963f, 55.62848f)), owning_building_guid = 36)
      LocalObject(699, Locker.Constructor(Vector3(5037.088f, 3816.835f, 55.62848f)), owning_building_guid = 36)
      LocalObject(700, Locker.Constructor(Vector3(5039.741f, 3794.963f, 55.62848f)), owning_building_guid = 36)
      LocalObject(701, Locker.Constructor(Vector3(5039.741f, 3816.835f, 55.62848f)), owning_building_guid = 36)
      LocalObject(702, Locker.Constructor(Vector3(5041.143f, 3794.963f, 55.62848f)), owning_building_guid = 36)
      LocalObject(703, Locker.Constructor(Vector3(5041.143f, 3816.835f, 55.62848f)), owning_building_guid = 36)
      LocalObject(990, Terminal.Constructor(Vector3(5041.446f, 3800.129f, 56.96648f), order_terminal), owning_building_guid = 36)
      LocalObject(991, Terminal.Constructor(Vector3(5041.446f, 3805.853f, 56.96648f), order_terminal), owning_building_guid = 36)
      LocalObject(992, Terminal.Constructor(Vector3(5041.446f, 3811.234f, 56.96648f), order_terminal), owning_building_guid = 36)
      LocalObject(1089, SpawnTube.Constructor(Vector3(5030.706f, 3797.742f, 55.11649f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 36)
      LocalObject(1090, SpawnTube.Constructor(Vector3(5030.706f, 3814.152f, 55.11649f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 36)
      LocalObject(1061, Painbox.Constructor(Vector3(5025.493f, 3802.849f, 56.92388f), painbox_radius_continuous), owning_building_guid = 36)
      LocalObject(1062, Painbox.Constructor(Vector3(5037.127f, 3800.078f, 55.73448f), painbox_radius_continuous), owning_building_guid = 36)
      LocalObject(1063, Painbox.Constructor(Vector3(5037.259f, 3812.107f, 55.73448f), painbox_radius_continuous), owning_building_guid = 36)
    }

    Building58()

    def Building58(): Unit = { // Name: nc_Cyssor_Outpost_Tower Type: tower_c GUID: 37, MapID: 58
      LocalBuilding("nc_Cyssor_Outpost_Tower", 37, 58, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(1978f, 4802f, 62.46139f), tower_c)))
      LocalObject(1093, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 37)
      LocalObject(312, Door.Constructor(Vector3(1990f, 4794f, 63.98239f)), owning_building_guid = 37)
      LocalObject(313, Door.Constructor(Vector3(1990f, 4794f, 83.98139f)), owning_building_guid = 37)
      LocalObject(314, Door.Constructor(Vector3(1990f, 4810f, 63.98239f)), owning_building_guid = 37)
      LocalObject(315, Door.Constructor(Vector3(1990f, 4810f, 83.98139f)), owning_building_guid = 37)
      LocalObject(1106, Door.Constructor(Vector3(1989.146f, 4790.794f, 53.79739f)), owning_building_guid = 37)
      LocalObject(1107, Door.Constructor(Vector3(1989.146f, 4807.204f, 53.79739f)), owning_building_guid = 37)
      LocalObject(550, IFFLock.Constructor(Vector3(1987.957f, 4810.811f, 63.92239f), Vector3(0, 0, 0)), owning_building_guid = 37, door_guid = 314)
      LocalObject(551, IFFLock.Constructor(Vector3(1987.957f, 4810.811f, 83.92239f), Vector3(0, 0, 0)), owning_building_guid = 37, door_guid = 315)
      LocalObject(554, IFFLock.Constructor(Vector3(1992.047f, 4793.189f, 63.92239f), Vector3(0, 0, 180)), owning_building_guid = 37, door_guid = 312)
      LocalObject(555, IFFLock.Constructor(Vector3(1992.047f, 4793.189f, 83.92239f), Vector3(0, 0, 180)), owning_building_guid = 37, door_guid = 313)
      LocalObject(608, Locker.Constructor(Vector3(1993.716f, 4786.963f, 52.45539f)), owning_building_guid = 37)
      LocalObject(609, Locker.Constructor(Vector3(1993.751f, 4808.835f, 52.45539f)), owning_building_guid = 37)
      LocalObject(610, Locker.Constructor(Vector3(1995.053f, 4786.963f, 52.45539f)), owning_building_guid = 37)
      LocalObject(611, Locker.Constructor(Vector3(1995.088f, 4808.835f, 52.45539f)), owning_building_guid = 37)
      LocalObject(616, Locker.Constructor(Vector3(1997.741f, 4786.963f, 52.45539f)), owning_building_guid = 37)
      LocalObject(617, Locker.Constructor(Vector3(1997.741f, 4808.835f, 52.45539f)), owning_building_guid = 37)
      LocalObject(618, Locker.Constructor(Vector3(1999.143f, 4786.963f, 52.45539f)), owning_building_guid = 37)
      LocalObject(619, Locker.Constructor(Vector3(1999.143f, 4808.835f, 52.45539f)), owning_building_guid = 37)
      LocalObject(843, Terminal.Constructor(Vector3(1999.445f, 4792.129f, 53.79339f), order_terminal), owning_building_guid = 37)
      LocalObject(844, Terminal.Constructor(Vector3(1999.445f, 4797.853f, 53.79339f), order_terminal), owning_building_guid = 37)
      LocalObject(845, Terminal.Constructor(Vector3(1999.445f, 4803.234f, 53.79339f), order_terminal), owning_building_guid = 37)
      LocalObject(1067, SpawnTube.Constructor(Vector3(1988.706f, 4789.742f, 51.94339f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 37)
      LocalObject(1068, SpawnTube.Constructor(Vector3(1988.706f, 4806.152f, 51.94339f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 37)
      LocalObject(1004, ProximityTerminal.Constructor(Vector3(1976.907f, 4796.725f, 90.03139f), pad_landing_tower_frame), owning_building_guid = 37)
      LocalObject(1005, Terminal.Constructor(Vector3(1976.907f, 4796.725f, 90.03139f), air_rearm_terminal), owning_building_guid = 37)
      LocalObject(1007, ProximityTerminal.Constructor(Vector3(1976.907f, 4807.17f, 90.03139f), pad_landing_tower_frame), owning_building_guid = 37)
      LocalObject(1008, Terminal.Constructor(Vector3(1976.907f, 4807.17f, 90.03139f), air_rearm_terminal), owning_building_guid = 37)
      LocalObject(712, FacilityTurret.Constructor(Vector3(1963.07f, 4787.045f, 81.40339f), manned_turret), owning_building_guid = 37)
      TurretToWeapon(712, 5012)
      LocalObject(714, FacilityTurret.Constructor(Vector3(2001.497f, 4816.957f, 81.40339f), manned_turret), owning_building_guid = 37)
      TurretToWeapon(714, 5013)
      LocalObject(1028, Painbox.Constructor(Vector3(1982.454f, 4794.849f, 54.48089f), painbox_radius_continuous), owning_building_guid = 37)
      LocalObject(1030, Painbox.Constructor(Vector3(1994.923f, 4791.54f, 52.56139f), painbox_radius_continuous), owning_building_guid = 37)
      LocalObject(1031, Painbox.Constructor(Vector3(1995.113f, 4804.022f, 52.56139f), painbox_radius_continuous), owning_building_guid = 37)
    }

    Building51()

    def Building51(): Unit = { // Name: nc_Central_Tower Type: tower_c GUID: 38, MapID: 51
      LocalBuilding("nc_Central_Tower", 38, 51, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3340f, 4668f, 61.79601f), tower_c)))
      LocalObject(1097, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 38)
      LocalObject(330, Door.Constructor(Vector3(3352f, 4660f, 63.31701f)), owning_building_guid = 38)
      LocalObject(331, Door.Constructor(Vector3(3352f, 4660f, 83.31601f)), owning_building_guid = 38)
      LocalObject(332, Door.Constructor(Vector3(3352f, 4676f, 63.31701f)), owning_building_guid = 38)
      LocalObject(333, Door.Constructor(Vector3(3352f, 4676f, 83.31601f)), owning_building_guid = 38)
      LocalObject(1114, Door.Constructor(Vector3(3351.146f, 4656.794f, 53.13201f)), owning_building_guid = 38)
      LocalObject(1115, Door.Constructor(Vector3(3351.146f, 4673.204f, 53.13201f)), owning_building_guid = 38)
      LocalObject(568, IFFLock.Constructor(Vector3(3349.957f, 4676.811f, 63.25701f), Vector3(0, 0, 0)), owning_building_guid = 38, door_guid = 332)
      LocalObject(569, IFFLock.Constructor(Vector3(3349.957f, 4676.811f, 83.25701f), Vector3(0, 0, 0)), owning_building_guid = 38, door_guid = 333)
      LocalObject(570, IFFLock.Constructor(Vector3(3354.047f, 4659.189f, 63.25701f), Vector3(0, 0, 180)), owning_building_guid = 38, door_guid = 330)
      LocalObject(571, IFFLock.Constructor(Vector3(3354.047f, 4659.189f, 83.25701f), Vector3(0, 0, 180)), owning_building_guid = 38, door_guid = 331)
      LocalObject(640, Locker.Constructor(Vector3(3355.716f, 4652.963f, 51.79001f)), owning_building_guid = 38)
      LocalObject(641, Locker.Constructor(Vector3(3355.751f, 4674.835f, 51.79001f)), owning_building_guid = 38)
      LocalObject(642, Locker.Constructor(Vector3(3357.053f, 4652.963f, 51.79001f)), owning_building_guid = 38)
      LocalObject(643, Locker.Constructor(Vector3(3357.088f, 4674.835f, 51.79001f)), owning_building_guid = 38)
      LocalObject(644, Locker.Constructor(Vector3(3359.741f, 4652.963f, 51.79001f)), owning_building_guid = 38)
      LocalObject(645, Locker.Constructor(Vector3(3359.741f, 4674.835f, 51.79001f)), owning_building_guid = 38)
      LocalObject(646, Locker.Constructor(Vector3(3361.143f, 4652.963f, 51.79001f)), owning_building_guid = 38)
      LocalObject(647, Locker.Constructor(Vector3(3361.143f, 4674.835f, 51.79001f)), owning_building_guid = 38)
      LocalObject(893, Terminal.Constructor(Vector3(3361.445f, 4658.129f, 53.12801f), order_terminal), owning_building_guid = 38)
      LocalObject(894, Terminal.Constructor(Vector3(3361.445f, 4663.853f, 53.12801f), order_terminal), owning_building_guid = 38)
      LocalObject(895, Terminal.Constructor(Vector3(3361.445f, 4669.234f, 53.12801f), order_terminal), owning_building_guid = 38)
      LocalObject(1075, SpawnTube.Constructor(Vector3(3350.706f, 4655.742f, 51.27802f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 38)
      LocalObject(1076, SpawnTube.Constructor(Vector3(3350.706f, 4672.152f, 51.27802f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 38)
      LocalObject(1010, ProximityTerminal.Constructor(Vector3(3338.907f, 4662.725f, 89.36601f), pad_landing_tower_frame), owning_building_guid = 38)
      LocalObject(1011, Terminal.Constructor(Vector3(3338.907f, 4662.725f, 89.36601f), air_rearm_terminal), owning_building_guid = 38)
      LocalObject(1013, ProximityTerminal.Constructor(Vector3(3338.907f, 4673.17f, 89.36601f), pad_landing_tower_frame), owning_building_guid = 38)
      LocalObject(1014, Terminal.Constructor(Vector3(3338.907f, 4673.17f, 89.36601f), air_rearm_terminal), owning_building_guid = 38)
      LocalObject(718, FacilityTurret.Constructor(Vector3(3325.07f, 4653.045f, 80.73801f), manned_turret), owning_building_guid = 38)
      TurretToWeapon(718, 5014)
      LocalObject(719, FacilityTurret.Constructor(Vector3(3363.497f, 4682.957f, 80.73801f), manned_turret), owning_building_guid = 38)
      TurretToWeapon(719, 5015)
      LocalObject(1040, Painbox.Constructor(Vector3(3344.454f, 4660.849f, 53.81551f), painbox_radius_continuous), owning_building_guid = 38)
      LocalObject(1041, Painbox.Constructor(Vector3(3356.923f, 4657.54f, 51.89601f), painbox_radius_continuous), owning_building_guid = 38)
      LocalObject(1042, Painbox.Constructor(Vector3(3357.113f, 4670.022f, 51.89601f), painbox_radius_continuous), owning_building_guid = 38)
    }

    Building56()

    def Building56(): Unit = { // Name: nc_Esamir_Outpost_Tower Type: tower_c GUID: 39, MapID: 56
      LocalBuilding("nc_Esamir_Outpost_Tower", 39, 56, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4140f, 2924f, 39.29424f), tower_c)))
      LocalObject(1099, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 39)
      LocalObject(340, Door.Constructor(Vector3(4152f, 2916f, 40.81524f)), owning_building_guid = 39)
      LocalObject(341, Door.Constructor(Vector3(4152f, 2916f, 60.81424f)), owning_building_guid = 39)
      LocalObject(342, Door.Constructor(Vector3(4152f, 2932f, 40.81524f)), owning_building_guid = 39)
      LocalObject(343, Door.Constructor(Vector3(4152f, 2932f, 60.81424f)), owning_building_guid = 39)
      LocalObject(1118, Door.Constructor(Vector3(4151.146f, 2912.794f, 30.63024f)), owning_building_guid = 39)
      LocalObject(1119, Door.Constructor(Vector3(4151.146f, 2929.204f, 30.63024f)), owning_building_guid = 39)
      LocalObject(578, IFFLock.Constructor(Vector3(4149.957f, 2932.811f, 40.75524f), Vector3(0, 0, 0)), owning_building_guid = 39, door_guid = 342)
      LocalObject(579, IFFLock.Constructor(Vector3(4149.957f, 2932.811f, 60.75524f), Vector3(0, 0, 0)), owning_building_guid = 39, door_guid = 343)
      LocalObject(580, IFFLock.Constructor(Vector3(4154.047f, 2915.189f, 40.75524f), Vector3(0, 0, 180)), owning_building_guid = 39, door_guid = 340)
      LocalObject(581, IFFLock.Constructor(Vector3(4154.047f, 2915.189f, 60.75524f), Vector3(0, 0, 180)), owning_building_guid = 39, door_guid = 341)
      LocalObject(656, Locker.Constructor(Vector3(4155.716f, 2908.963f, 29.28824f)), owning_building_guid = 39)
      LocalObject(657, Locker.Constructor(Vector3(4155.751f, 2930.835f, 29.28824f)), owning_building_guid = 39)
      LocalObject(658, Locker.Constructor(Vector3(4157.053f, 2908.963f, 29.28824f)), owning_building_guid = 39)
      LocalObject(659, Locker.Constructor(Vector3(4157.088f, 2930.835f, 29.28824f)), owning_building_guid = 39)
      LocalObject(660, Locker.Constructor(Vector3(4159.741f, 2908.963f, 29.28824f)), owning_building_guid = 39)
      LocalObject(661, Locker.Constructor(Vector3(4159.741f, 2930.835f, 29.28824f)), owning_building_guid = 39)
      LocalObject(662, Locker.Constructor(Vector3(4161.143f, 2908.963f, 29.28824f)), owning_building_guid = 39)
      LocalObject(663, Locker.Constructor(Vector3(4161.143f, 2930.835f, 29.28824f)), owning_building_guid = 39)
      LocalObject(922, Terminal.Constructor(Vector3(4161.445f, 2914.129f, 30.62624f), order_terminal), owning_building_guid = 39)
      LocalObject(923, Terminal.Constructor(Vector3(4161.445f, 2919.853f, 30.62624f), order_terminal), owning_building_guid = 39)
      LocalObject(924, Terminal.Constructor(Vector3(4161.445f, 2925.234f, 30.62624f), order_terminal), owning_building_guid = 39)
      LocalObject(1079, SpawnTube.Constructor(Vector3(4150.706f, 2911.742f, 28.77624f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 39)
      LocalObject(1080, SpawnTube.Constructor(Vector3(4150.706f, 2928.152f, 28.77624f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 39)
      LocalObject(1016, ProximityTerminal.Constructor(Vector3(4138.907f, 2918.725f, 66.86424f), pad_landing_tower_frame), owning_building_guid = 39)
      LocalObject(1017, Terminal.Constructor(Vector3(4138.907f, 2918.725f, 66.86424f), air_rearm_terminal), owning_building_guid = 39)
      LocalObject(1019, ProximityTerminal.Constructor(Vector3(4138.907f, 2929.17f, 66.86424f), pad_landing_tower_frame), owning_building_guid = 39)
      LocalObject(1020, Terminal.Constructor(Vector3(4138.907f, 2929.17f, 66.86424f), air_rearm_terminal), owning_building_guid = 39)
      LocalObject(720, FacilityTurret.Constructor(Vector3(4125.07f, 2909.045f, 58.23624f), manned_turret), owning_building_guid = 39)
      TurretToWeapon(720, 5016)
      LocalObject(721, FacilityTurret.Constructor(Vector3(4163.497f, 2938.957f, 58.23624f), manned_turret), owning_building_guid = 39)
      TurretToWeapon(721, 5017)
      LocalObject(1046, Painbox.Constructor(Vector3(4144.454f, 2916.849f, 31.31374f), painbox_radius_continuous), owning_building_guid = 39)
      LocalObject(1047, Painbox.Constructor(Vector3(4156.923f, 2913.54f, 29.39424f), painbox_radius_continuous), owning_building_guid = 39)
      LocalObject(1048, Painbox.Constructor(Vector3(4157.113f, 2926.022f, 29.39424f), painbox_radius_continuous), owning_building_guid = 39)
    }

    Building57()

    def Building57(): Unit = { // Name: nc_Amerish_Outpost_Tower Type: tower_c GUID: 40, MapID: 57
      LocalBuilding("nc_Amerish_Outpost_Tower", 40, 57, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(5290f, 6160f, 39.10642f), tower_c)))
      LocalObject(1105, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 40)
      LocalObject(366, Door.Constructor(Vector3(5302f, 6152f, 40.62742f)), owning_building_guid = 40)
      LocalObject(367, Door.Constructor(Vector3(5302f, 6152f, 60.62642f)), owning_building_guid = 40)
      LocalObject(368, Door.Constructor(Vector3(5302f, 6168f, 40.62742f)), owning_building_guid = 40)
      LocalObject(369, Door.Constructor(Vector3(5302f, 6168f, 60.62642f)), owning_building_guid = 40)
      LocalObject(1130, Door.Constructor(Vector3(5301.146f, 6148.794f, 30.44242f)), owning_building_guid = 40)
      LocalObject(1131, Door.Constructor(Vector3(5301.146f, 6165.204f, 30.44242f)), owning_building_guid = 40)
      LocalObject(604, IFFLock.Constructor(Vector3(5299.957f, 6168.811f, 40.56742f), Vector3(0, 0, 0)), owning_building_guid = 40, door_guid = 368)
      LocalObject(605, IFFLock.Constructor(Vector3(5299.957f, 6168.811f, 60.56742f), Vector3(0, 0, 0)), owning_building_guid = 40, door_guid = 369)
      LocalObject(606, IFFLock.Constructor(Vector3(5304.047f, 6151.189f, 40.56742f), Vector3(0, 0, 180)), owning_building_guid = 40, door_guid = 366)
      LocalObject(607, IFFLock.Constructor(Vector3(5304.047f, 6151.189f, 60.56742f), Vector3(0, 0, 180)), owning_building_guid = 40, door_guid = 367)
      LocalObject(704, Locker.Constructor(Vector3(5305.716f, 6144.963f, 29.10042f)), owning_building_guid = 40)
      LocalObject(705, Locker.Constructor(Vector3(5305.751f, 6166.835f, 29.10042f)), owning_building_guid = 40)
      LocalObject(706, Locker.Constructor(Vector3(5307.053f, 6144.963f, 29.10042f)), owning_building_guid = 40)
      LocalObject(707, Locker.Constructor(Vector3(5307.088f, 6166.835f, 29.10042f)), owning_building_guid = 40)
      LocalObject(708, Locker.Constructor(Vector3(5309.741f, 6144.963f, 29.10042f)), owning_building_guid = 40)
      LocalObject(709, Locker.Constructor(Vector3(5309.741f, 6166.835f, 29.10042f)), owning_building_guid = 40)
      LocalObject(710, Locker.Constructor(Vector3(5311.143f, 6144.963f, 29.10042f)), owning_building_guid = 40)
      LocalObject(711, Locker.Constructor(Vector3(5311.143f, 6166.835f, 29.10042f)), owning_building_guid = 40)
      LocalObject(993, Terminal.Constructor(Vector3(5311.445f, 6150.129f, 30.43842f), order_terminal), owning_building_guid = 40)
      LocalObject(994, Terminal.Constructor(Vector3(5311.445f, 6155.853f, 30.43842f), order_terminal), owning_building_guid = 40)
      LocalObject(995, Terminal.Constructor(Vector3(5311.445f, 6161.234f, 30.43842f), order_terminal), owning_building_guid = 40)
      LocalObject(1091, SpawnTube.Constructor(Vector3(5300.706f, 6147.742f, 28.58842f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 40)
      LocalObject(1092, SpawnTube.Constructor(Vector3(5300.706f, 6164.152f, 28.58842f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 40)
      LocalObject(1022, ProximityTerminal.Constructor(Vector3(5288.907f, 6154.725f, 66.67642f), pad_landing_tower_frame), owning_building_guid = 40)
      LocalObject(1023, Terminal.Constructor(Vector3(5288.907f, 6154.725f, 66.67642f), air_rearm_terminal), owning_building_guid = 40)
      LocalObject(1025, ProximityTerminal.Constructor(Vector3(5288.907f, 6165.17f, 66.67642f), pad_landing_tower_frame), owning_building_guid = 40)
      LocalObject(1026, Terminal.Constructor(Vector3(5288.907f, 6165.17f, 66.67642f), air_rearm_terminal), owning_building_guid = 40)
      LocalObject(730, FacilityTurret.Constructor(Vector3(5275.07f, 6145.045f, 58.04842f), manned_turret), owning_building_guid = 40)
      TurretToWeapon(730, 5018)
      LocalObject(731, FacilityTurret.Constructor(Vector3(5313.497f, 6174.957f, 58.04842f), manned_turret), owning_building_guid = 40)
      TurretToWeapon(731, 5019)
      LocalObject(1064, Painbox.Constructor(Vector3(5294.454f, 6152.849f, 31.12592f), painbox_radius_continuous), owning_building_guid = 40)
      LocalObject(1065, Painbox.Constructor(Vector3(5306.923f, 6149.54f, 29.20642f), painbox_radius_continuous), owning_building_guid = 40)
      LocalObject(1066, Painbox.Constructor(Vector3(5307.113f, 6162.022f, 29.20642f), painbox_radius_continuous), owning_building_guid = 40)
    }

    Building30()

    def Building30(): Unit = { // Name: Cyssor_Spawn1 Type: VT_building_nc GUID: 41, MapID: 30
      LocalBuilding("Cyssor_Spawn1", 41, 30, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2168f, 5392f, 65.21291f), VT_building_nc)))
      LocalObject(234, Door.Constructor(Vector3(2162.214f, 5444.023f, 67.2889f)), owning_building_guid = 41)
      LocalObject(235, Door.Constructor(Vector3(2162.343f, 5450.527f, 67.2889f)), owning_building_guid = 41)
      LocalObject(236, Door.Constructor(Vector3(2168.919f, 5443.906f, 67.2889f)), owning_building_guid = 41)
      LocalObject(237, Door.Constructor(Vector3(2169.048f, 5450.409f, 67.2889f)), owning_building_guid = 41)
      LocalObject(238, Door.Constructor(Vector3(2175.624f, 5443.789f, 67.2889f)), owning_building_guid = 41)
      LocalObject(239, Door.Constructor(Vector3(2175.753f, 5450.292f, 67.2889f)), owning_building_guid = 41)
      LocalObject(370, Door.Constructor(Vector3(2157.124f, 5458.601f, 66.9229f)), owning_building_guid = 41)
      LocalObject(373, Door.Constructor(Vector3(2181.174f, 5458.181f, 66.9229f)), owning_building_guid = 41)
      LocalObject(418, Door.Constructor(Vector3(2132.296f, 5408.613f, 68.38791f)), owning_building_guid = 41)
      LocalObject(419, Door.Constructor(Vector3(2148.385f, 5412.305f, 68.38791f)), owning_building_guid = 41)
      LocalObject(420, Door.Constructor(Vector3(2152.644f, 5428.286f, 68.38791f)), owning_building_guid = 41)
      LocalObject(421, Door.Constructor(Vector3(2184.613f, 5427.704f, 68.38791f)), owning_building_guid = 41)
      LocalObject(422, Door.Constructor(Vector3(2188.305f, 5411.615f, 68.38791f)), owning_building_guid = 41)
      LocalObject(423, Door.Constructor(Vector3(2204.286f, 5407.356f, 68.38791f)), owning_building_guid = 41)
      LocalObject(849, Terminal.Constructor(Vector3(2166.156f, 5437.701f, 66.57291f), order_terminal), owning_building_guid = 41)
      LocalObject(850, Terminal.Constructor(Vector3(2166.512f, 5456.735f, 66.57291f), order_terminal), owning_building_guid = 41)
      LocalObject(851, Terminal.Constructor(Vector3(2168.855f, 5437.654f, 66.57291f), order_terminal), owning_building_guid = 41)
      LocalObject(852, Terminal.Constructor(Vector3(2169.14f, 5456.689f, 66.57291f), order_terminal), owning_building_guid = 41)
      LocalObject(853, Terminal.Constructor(Vector3(2171.483f, 5437.608f, 66.57291f), order_terminal), owning_building_guid = 41)
      LocalObject(854, Terminal.Constructor(Vector3(2171.839f, 5456.641f, 66.57291f), order_terminal), owning_building_guid = 41)
      LocalObject(774, SpawnTube.Constructor(Vector3(2162.234f, 5444.899f, 67.2449f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 41)
      LocalObject(775, SpawnTube.Constructor(Vector3(2162.317f, 5449.653f, 67.2449f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 41)
      LocalObject(776, SpawnTube.Constructor(Vector3(2168.941f, 5444.781f, 67.2449f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 41)
      LocalObject(777, SpawnTube.Constructor(Vector3(2169.023f, 5449.536f, 67.2449f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 41)
      LocalObject(778, SpawnTube.Constructor(Vector3(2175.648f, 5444.665f, 67.2449f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 41)
      LocalObject(779, SpawnTube.Constructor(Vector3(2175.731f, 5449.419f, 67.2449f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 41)
    }

    Building35()

    def Building35(): Unit = { // Name: Cyssor_Spawn2 Type: VT_building_nc GUID: 42, MapID: 35
      LocalBuilding("Cyssor_Spawn2", 42, 35, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2400f, 5402f, 64.68082f), VT_building_nc)))
      LocalObject(240, Door.Constructor(Vector3(2354.615f, 5439.385f, 66.75681f)), owning_building_guid = 42)
      LocalObject(241, Door.Constructor(Vector3(2359.122f, 5434.694f, 66.75681f)), owning_building_guid = 42)
      LocalObject(242, Door.Constructor(Vector3(2359.439f, 5444.042f, 66.75681f)), owning_building_guid = 42)
      LocalObject(243, Door.Constructor(Vector3(2363.946f, 5439.353f, 66.75681f)), owning_building_guid = 42)
      LocalObject(244, Door.Constructor(Vector3(2364.264f, 5448.701f, 66.75681f)), owning_building_guid = 42)
      LocalObject(245, Door.Constructor(Vector3(2368.771f, 5444.011f, 66.75681f)), owning_building_guid = 42)
      LocalObject(376, Door.Constructor(Vector3(2345.215f, 5441.403f, 66.39082f)), owning_building_guid = 42)
      LocalObject(377, Door.Constructor(Vector3(2362.519f, 5458.113f, 66.39082f)), owning_building_guid = 42)
      LocalObject(424, Door.Constructor(Vector3(2363.007f, 5388.5f, 67.85582f)), owning_building_guid = 42)
      LocalObject(425, Door.Constructor(Vector3(2363.484f, 5416.799f, 67.85582f)), owning_building_guid = 42)
      LocalObject(426, Door.Constructor(Vector3(2371.772f, 5402.488f, 67.85582f)), owning_building_guid = 42)
      LocalObject(427, Door.Constructor(Vector3(2386.5f, 5438.993f, 67.85582f)), owning_building_guid = 42)
      LocalObject(428, Door.Constructor(Vector3(2400.488f, 5430.228f, 67.85582f)), owning_building_guid = 42)
      LocalObject(429, Door.Constructor(Vector3(2414.8f, 5438.516f, 67.85582f)), owning_building_guid = 42)
      LocalObject(875, Terminal.Constructor(Vector3(2353.173f, 5446.722f, 66.04082f), order_terminal), owning_building_guid = 42)
      LocalObject(876, Terminal.Constructor(Vector3(2355.064f, 5448.548f, 66.04082f), order_terminal), owning_building_guid = 42)
      LocalObject(877, Terminal.Constructor(Vector3(2357.007f, 5450.423f, 66.04082f), order_terminal), owning_building_guid = 42)
      LocalObject(878, Terminal.Constructor(Vector3(2366.381f, 5433.011f, 66.04082f), order_terminal), owning_building_guid = 42)
      LocalObject(879, Terminal.Constructor(Vector3(2368.322f, 5434.887f, 66.04082f), order_terminal), owning_building_guid = 42)
      LocalObject(880, Terminal.Constructor(Vector3(2370.213f, 5436.713f, 66.04082f), order_terminal), owning_building_guid = 42)
      LocalObject(780, SpawnTube.Constructor(Vector3(2355.215f, 5438.748f, 66.71281f), respawn_tube_sanctuary, Vector3(0, 0, 316)), owning_building_guid = 42)
      LocalObject(781, SpawnTube.Constructor(Vector3(2358.517f, 5435.328f, 66.71281f), respawn_tube_sanctuary, Vector3(0, 0, 136)), owning_building_guid = 42)
      LocalObject(782, SpawnTube.Constructor(Vector3(2360.04f, 5443.408f, 66.71281f), respawn_tube_sanctuary, Vector3(0, 0, 316)), owning_building_guid = 42)
      LocalObject(783, SpawnTube.Constructor(Vector3(2363.344f, 5439.987f, 66.71281f), respawn_tube_sanctuary, Vector3(0, 0, 136)), owning_building_guid = 42)
      LocalObject(784, SpawnTube.Constructor(Vector3(2364.866f, 5448.068f, 66.71281f), respawn_tube_sanctuary, Vector3(0, 0, 316)), owning_building_guid = 42)
      LocalObject(785, SpawnTube.Constructor(Vector3(2368.168f, 5444.648f, 66.71281f), respawn_tube_sanctuary, Vector3(0, 0, 136)), owning_building_guid = 42)
    }

    Building36()

    def Building36(): Unit = { // Name: Cyssor_Spawn3 Type: VT_building_nc GUID: 43, MapID: 36
      LocalBuilding("Cyssor_Spawn3", 43, 36, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2452f, 5542f, 65.20798f), VT_building_nc)))
      LocalObject(246, Door.Constructor(Vector3(2393.473f, 5547.714f, 67.28398f)), owning_building_guid = 43)
      LocalObject(247, Door.Constructor(Vector3(2393.59f, 5541.009f, 67.28398f)), owning_building_guid = 43)
      LocalObject(248, Door.Constructor(Vector3(2393.706f, 5534.304f, 67.28398f)), owning_building_guid = 43)
      LocalObject(249, Door.Constructor(Vector3(2399.976f, 5547.812f, 67.28398f)), owning_building_guid = 43)
      LocalObject(250, Door.Constructor(Vector3(2400.093f, 5541.107f, 67.28398f)), owning_building_guid = 43)
      LocalObject(251, Door.Constructor(Vector3(2400.21f, 5534.401f, 67.28398f)), owning_building_guid = 43)
      LocalObject(378, Door.Constructor(Vector3(2385.399f, 5552.856f, 66.91798f)), owning_building_guid = 43)
      LocalObject(379, Door.Constructor(Vector3(2385.819f, 5528.806f, 66.91798f)), owning_building_guid = 43)
      LocalObject(430, Door.Constructor(Vector3(2415.738f, 5557.356f, 68.38299f)), owning_building_guid = 43)
      LocalObject(431, Door.Constructor(Vector3(2416.272f, 5525.387f, 68.38299f)), owning_building_guid = 43)
      LocalObject(432, Door.Constructor(Vector3(2431.688f, 5561.608f, 68.38299f)), owning_building_guid = 43)
      LocalObject(433, Door.Constructor(Vector3(2432.392f, 5521.688f, 68.38299f)), owning_building_guid = 43)
      LocalObject(434, Door.Constructor(Vector3(2435.387f, 5577.728f, 68.38299f)), owning_building_guid = 43)
      LocalObject(435, Door.Constructor(Vector3(2436.644f, 5505.738f, 68.38299f)), owning_building_guid = 43)
      LocalObject(881, Terminal.Constructor(Vector3(2387.264f, 5543.581f, 66.56799f), order_terminal), owning_building_guid = 43)
      LocalObject(882, Terminal.Constructor(Vector3(2387.357f, 5538.253f, 66.56799f), order_terminal), owning_building_guid = 43)
      LocalObject(883, Terminal.Constructor(Vector3(2387.311f, 5540.882f, 66.56799f), order_terminal), owning_building_guid = 43)
      LocalObject(884, Terminal.Constructor(Vector3(2406.298f, 5543.89f, 66.56799f), order_terminal), owning_building_guid = 43)
      LocalObject(885, Terminal.Constructor(Vector3(2406.392f, 5538.562f, 66.56799f), order_terminal), owning_building_guid = 43)
      LocalObject(886, Terminal.Constructor(Vector3(2406.344f, 5541.261f, 66.56799f), order_terminal), owning_building_guid = 43)
      LocalObject(786, SpawnTube.Constructor(Vector3(2394.346f, 5547.723f, 67.23998f), respawn_tube_sanctuary, Vector3(0, 0, 269)), owning_building_guid = 43)
      LocalObject(787, SpawnTube.Constructor(Vector3(2394.463f, 5541.015f, 67.23998f), respawn_tube_sanctuary, Vector3(0, 0, 269)), owning_building_guid = 43)
      LocalObject(788, SpawnTube.Constructor(Vector3(2394.58f, 5534.308f, 67.23998f), respawn_tube_sanctuary, Vector3(0, 0, 269)), owning_building_guid = 43)
      LocalObject(789, SpawnTube.Constructor(Vector3(2399.1f, 5547.806f, 67.23998f), respawn_tube_sanctuary, Vector3(0, 0, 89)), owning_building_guid = 43)
      LocalObject(790, SpawnTube.Constructor(Vector3(2399.218f, 5541.099f, 67.23998f), respawn_tube_sanctuary, Vector3(0, 0, 89)), owning_building_guid = 43)
      LocalObject(791, SpawnTube.Constructor(Vector3(2399.334f, 5534.391f, 67.23998f), respawn_tube_sanctuary, Vector3(0, 0, 89)), owning_building_guid = 43)
    }

    Building53()

    def Building53(): Unit = { // Name: amerish_spawn1 Type: VT_building_nc GUID: 44, MapID: 53
      LocalBuilding("amerish_spawn1", 44, 53, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3996f, 5924f, 43.87661f), VT_building_nc)))
      LocalObject(252, Door.Constructor(Vector3(3990.214f, 5976.023f, 45.95261f)), owning_building_guid = 44)
      LocalObject(253, Door.Constructor(Vector3(3990.343f, 5982.527f, 45.95261f)), owning_building_guid = 44)
      LocalObject(254, Door.Constructor(Vector3(3996.919f, 5975.906f, 45.95261f)), owning_building_guid = 44)
      LocalObject(255, Door.Constructor(Vector3(3997.048f, 5982.409f, 45.95261f)), owning_building_guid = 44)
      LocalObject(256, Door.Constructor(Vector3(4003.624f, 5975.789f, 45.95261f)), owning_building_guid = 44)
      LocalObject(257, Door.Constructor(Vector3(4003.753f, 5982.292f, 45.95261f)), owning_building_guid = 44)
      LocalObject(380, Door.Constructor(Vector3(3985.124f, 5990.601f, 45.58661f)), owning_building_guid = 44)
      LocalObject(381, Door.Constructor(Vector3(4009.174f, 5990.181f, 45.58661f)), owning_building_guid = 44)
      LocalObject(436, Door.Constructor(Vector3(3960.296f, 5940.613f, 47.05161f)), owning_building_guid = 44)
      LocalObject(437, Door.Constructor(Vector3(3976.385f, 5944.305f, 47.05161f)), owning_building_guid = 44)
      LocalObject(438, Door.Constructor(Vector3(3980.644f, 5960.286f, 47.05161f)), owning_building_guid = 44)
      LocalObject(439, Door.Constructor(Vector3(4012.613f, 5959.704f, 47.05161f)), owning_building_guid = 44)
      LocalObject(440, Door.Constructor(Vector3(4016.305f, 5943.615f, 47.05161f)), owning_building_guid = 44)
      LocalObject(441, Door.Constructor(Vector3(4032.286f, 5939.356f, 47.05161f)), owning_building_guid = 44)
      LocalObject(899, Terminal.Constructor(Vector3(3994.156f, 5969.701f, 45.23661f), order_terminal), owning_building_guid = 44)
      LocalObject(900, Terminal.Constructor(Vector3(3994.512f, 5988.735f, 45.23661f), order_terminal), owning_building_guid = 44)
      LocalObject(901, Terminal.Constructor(Vector3(3996.855f, 5969.654f, 45.23661f), order_terminal), owning_building_guid = 44)
      LocalObject(902, Terminal.Constructor(Vector3(3997.14f, 5988.689f, 45.23661f), order_terminal), owning_building_guid = 44)
      LocalObject(903, Terminal.Constructor(Vector3(3999.483f, 5969.608f, 45.23661f), order_terminal), owning_building_guid = 44)
      LocalObject(904, Terminal.Constructor(Vector3(3999.839f, 5988.641f, 45.23661f), order_terminal), owning_building_guid = 44)
      LocalObject(792, SpawnTube.Constructor(Vector3(3990.234f, 5976.899f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 44)
      LocalObject(793, SpawnTube.Constructor(Vector3(3990.317f, 5981.653f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 44)
      LocalObject(794, SpawnTube.Constructor(Vector3(3996.941f, 5976.781f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 44)
      LocalObject(795, SpawnTube.Constructor(Vector3(3997.023f, 5981.536f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 44)
      LocalObject(796, SpawnTube.Constructor(Vector3(4003.648f, 5976.665f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 44)
      LocalObject(797, SpawnTube.Constructor(Vector3(4003.731f, 5981.419f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 44)
    }

    Building18()

    def Building18(): Unit = { // Name: amerish_spawn2 Type: VT_building_nc GUID: 45, MapID: 18
      LocalBuilding("amerish_spawn2", 45, 18, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4148f, 5892f, 43.87661f), VT_building_nc)))
      LocalObject(258, Door.Constructor(Vector3(4142.213f, 5944.023f, 45.95261f)), owning_building_guid = 45)
      LocalObject(259, Door.Constructor(Vector3(4142.343f, 5950.527f, 45.95261f)), owning_building_guid = 45)
      LocalObject(260, Door.Constructor(Vector3(4148.919f, 5943.906f, 45.95261f)), owning_building_guid = 45)
      LocalObject(261, Door.Constructor(Vector3(4149.048f, 5950.409f, 45.95261f)), owning_building_guid = 45)
      LocalObject(262, Door.Constructor(Vector3(4155.624f, 5943.789f, 45.95261f)), owning_building_guid = 45)
      LocalObject(263, Door.Constructor(Vector3(4155.753f, 5950.292f, 45.95261f)), owning_building_guid = 45)
      LocalObject(384, Door.Constructor(Vector3(4137.124f, 5958.601f, 45.58661f)), owning_building_guid = 45)
      LocalObject(385, Door.Constructor(Vector3(4161.174f, 5958.181f, 45.58661f)), owning_building_guid = 45)
      LocalObject(442, Door.Constructor(Vector3(4112.296f, 5908.613f, 47.05161f)), owning_building_guid = 45)
      LocalObject(443, Door.Constructor(Vector3(4128.385f, 5912.305f, 47.05161f)), owning_building_guid = 45)
      LocalObject(444, Door.Constructor(Vector3(4132.644f, 5928.286f, 47.05161f)), owning_building_guid = 45)
      LocalObject(445, Door.Constructor(Vector3(4164.613f, 5927.704f, 47.05161f)), owning_building_guid = 45)
      LocalObject(446, Door.Constructor(Vector3(4168.305f, 5911.615f, 47.05161f)), owning_building_guid = 45)
      LocalObject(447, Door.Constructor(Vector3(4184.286f, 5907.356f, 47.05161f)), owning_building_guid = 45)
      LocalObject(915, Terminal.Constructor(Vector3(4146.156f, 5937.701f, 45.23661f), order_terminal), owning_building_guid = 45)
      LocalObject(916, Terminal.Constructor(Vector3(4146.512f, 5956.735f, 45.23661f), order_terminal), owning_building_guid = 45)
      LocalObject(917, Terminal.Constructor(Vector3(4148.855f, 5937.654f, 45.23661f), order_terminal), owning_building_guid = 45)
      LocalObject(918, Terminal.Constructor(Vector3(4149.141f, 5956.689f, 45.23661f), order_terminal), owning_building_guid = 45)
      LocalObject(919, Terminal.Constructor(Vector3(4151.483f, 5937.608f, 45.23661f), order_terminal), owning_building_guid = 45)
      LocalObject(920, Terminal.Constructor(Vector3(4151.839f, 5956.641f, 45.23661f), order_terminal), owning_building_guid = 45)
      LocalObject(798, SpawnTube.Constructor(Vector3(4142.234f, 5944.899f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 45)
      LocalObject(799, SpawnTube.Constructor(Vector3(4142.317f, 5949.653f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 45)
      LocalObject(800, SpawnTube.Constructor(Vector3(4148.941f, 5944.781f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 45)
      LocalObject(801, SpawnTube.Constructor(Vector3(4149.023f, 5949.536f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 45)
      LocalObject(802, SpawnTube.Constructor(Vector3(4155.648f, 5944.665f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 45)
      LocalObject(803, SpawnTube.Constructor(Vector3(4155.731f, 5949.419f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 45)
    }

    Building52()

    def Building52(): Unit = { // Name: amerish_spawn3 Type: VT_building_nc GUID: 46, MapID: 52
      LocalBuilding("amerish_spawn3", 46, 52, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4302f, 5918f, 43.87661f), VT_building_nc)))
      LocalObject(264, Door.Constructor(Vector3(4296.213f, 5970.023f, 45.95261f)), owning_building_guid = 46)
      LocalObject(265, Door.Constructor(Vector3(4296.343f, 5976.527f, 45.95261f)), owning_building_guid = 46)
      LocalObject(266, Door.Constructor(Vector3(4302.919f, 5969.906f, 45.95261f)), owning_building_guid = 46)
      LocalObject(267, Door.Constructor(Vector3(4303.048f, 5976.409f, 45.95261f)), owning_building_guid = 46)
      LocalObject(268, Door.Constructor(Vector3(4309.624f, 5969.789f, 45.95261f)), owning_building_guid = 46)
      LocalObject(269, Door.Constructor(Vector3(4309.753f, 5976.292f, 45.95261f)), owning_building_guid = 46)
      LocalObject(388, Door.Constructor(Vector3(4291.124f, 5984.601f, 45.58661f)), owning_building_guid = 46)
      LocalObject(389, Door.Constructor(Vector3(4315.174f, 5984.181f, 45.58661f)), owning_building_guid = 46)
      LocalObject(448, Door.Constructor(Vector3(4266.296f, 5934.613f, 47.05161f)), owning_building_guid = 46)
      LocalObject(449, Door.Constructor(Vector3(4282.385f, 5938.305f, 47.05161f)), owning_building_guid = 46)
      LocalObject(450, Door.Constructor(Vector3(4286.644f, 5954.286f, 47.05161f)), owning_building_guid = 46)
      LocalObject(451, Door.Constructor(Vector3(4318.613f, 5953.704f, 47.05161f)), owning_building_guid = 46)
      LocalObject(452, Door.Constructor(Vector3(4322.305f, 5937.615f, 47.05161f)), owning_building_guid = 46)
      LocalObject(453, Door.Constructor(Vector3(4338.286f, 5933.356f, 47.05161f)), owning_building_guid = 46)
      LocalObject(940, Terminal.Constructor(Vector3(4300.156f, 5963.701f, 45.23661f), order_terminal), owning_building_guid = 46)
      LocalObject(941, Terminal.Constructor(Vector3(4300.512f, 5982.735f, 45.23661f), order_terminal), owning_building_guid = 46)
      LocalObject(942, Terminal.Constructor(Vector3(4302.855f, 5963.654f, 45.23661f), order_terminal), owning_building_guid = 46)
      LocalObject(943, Terminal.Constructor(Vector3(4303.141f, 5982.689f, 45.23661f), order_terminal), owning_building_guid = 46)
      LocalObject(944, Terminal.Constructor(Vector3(4305.483f, 5963.608f, 45.23661f), order_terminal), owning_building_guid = 46)
      LocalObject(945, Terminal.Constructor(Vector3(4305.839f, 5982.641f, 45.23661f), order_terminal), owning_building_guid = 46)
      LocalObject(804, SpawnTube.Constructor(Vector3(4296.234f, 5970.899f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 46)
      LocalObject(805, SpawnTube.Constructor(Vector3(4296.317f, 5975.653f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 46)
      LocalObject(806, SpawnTube.Constructor(Vector3(4302.941f, 5970.781f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 46)
      LocalObject(807, SpawnTube.Constructor(Vector3(4303.023f, 5975.536f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 46)
      LocalObject(808, SpawnTube.Constructor(Vector3(4309.648f, 5970.665f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 181)), owning_building_guid = 46)
      LocalObject(809, SpawnTube.Constructor(Vector3(4309.731f, 5975.419f, 45.90862f), respawn_tube_sanctuary, Vector3(0, 0, 1)), owning_building_guid = 46)
    }

    Building5()

    def Building5(): Unit = { // Name: Esamir_Spawn1 Type: VT_building_nc GUID: 47, MapID: 5
      LocalBuilding("Esamir_Spawn1", 47, 5, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4662f, 3362f, 68.73806f), VT_building_nc)))
      LocalObject(270, Door.Constructor(Vector3(4695.403f, 3402.301f, 70.81406f)), owning_building_guid = 47)
      LocalObject(271, Door.Constructor(Vector3(4699.977f, 3397.396f, 70.81406f)), owning_building_guid = 47)
      LocalObject(272, Door.Constructor(Vector3(4700.171f, 3406.725f, 70.81406f)), owning_building_guid = 47)
      LocalObject(273, Door.Constructor(Vector3(4704.55f, 3392.492f, 70.81406f)), owning_building_guid = 47)
      LocalObject(274, Door.Constructor(Vector3(4704.744f, 3401.821f, 70.81406f)), owning_building_guid = 47)
      LocalObject(275, Door.Constructor(Vector3(4709.317f, 3396.916f, 70.81406f)), owning_building_guid = 47)
      LocalObject(390, Door.Constructor(Vector3(4702.353f, 3416.089f, 70.44806f)), owning_building_guid = 47)
      LocalObject(391, Door.Constructor(Vector3(4718.758f, 3398.496f, 70.44806f)), owning_building_guid = 47)
      LocalObject(454, Door.Constructor(Vector3(4649.148f, 3399.223f, 71.91306f)), owning_building_guid = 47)
      LocalObject(455, Door.Constructor(Vector3(4662.98f, 3390.215f, 71.91306f)), owning_building_guid = 47)
      LocalObject(456, Door.Constructor(Vector3(4677.435f, 3398.252f, 71.91306f)), owning_building_guid = 47)
      LocalObject(458, Door.Constructor(Vector3(4690.215f, 3361.02f, 71.91306f)), owning_building_guid = 47)
      LocalObject(460, Door.Constructor(Vector3(4698.252f, 3346.565f, 71.91306f)), owning_building_guid = 47)
      LocalObject(461, Door.Constructor(Vector3(4699.223f, 3374.852f, 71.91306f)), owning_building_guid = 47)
      LocalObject(949, Terminal.Constructor(Vector3(4693.593f, 3395.073f, 70.09806f), order_terminal), owning_building_guid = 47)
      LocalObject(950, Terminal.Constructor(Vector3(4695.435f, 3393.099f, 70.09806f), order_terminal), owning_building_guid = 47)
      LocalObject(951, Terminal.Constructor(Vector3(4697.228f, 3391.176f, 70.09806f), order_terminal), owning_building_guid = 47)
      LocalObject(952, Terminal.Constructor(Vector3(4707.533f, 3408.039f, 70.09806f), order_terminal), owning_building_guid = 47)
      LocalObject(953, Terminal.Constructor(Vector3(4709.326f, 3406.116f, 70.09806f), order_terminal), owning_building_guid = 47)
      LocalObject(954, Terminal.Constructor(Vector3(4711.166f, 3404.142f, 70.09806f), order_terminal), owning_building_guid = 47)
      LocalObject(810, SpawnTube.Constructor(Vector3(4696.047f, 3402.895f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 227)), owning_building_guid = 47)
      LocalObject(811, SpawnTube.Constructor(Vector3(4699.524f, 3406.137f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 47)), owning_building_guid = 47)
      LocalObject(812, SpawnTube.Constructor(Vector3(4700.622f, 3397.988f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 227)), owning_building_guid = 47)
      LocalObject(813, SpawnTube.Constructor(Vector3(4704.099f, 3401.232f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 47)), owning_building_guid = 47)
      LocalObject(814, SpawnTube.Constructor(Vector3(4705.197f, 3393.082f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 227)), owning_building_guid = 47)
      LocalObject(815, SpawnTube.Constructor(Vector3(4708.674f, 3396.325f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 47)), owning_building_guid = 47)
    }

    Building7()

    def Building7(): Unit = { // Name: Esamir_Spawn2 Type: VT_building_nc GUID: 48, MapID: 7
      LocalBuilding("Esamir_Spawn2", 48, 7, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4692f, 3636f, 68.73806f), VT_building_nc)))
      LocalObject(276, Door.Constructor(Vector3(4723.958f, 3594.54f, 70.81406f)), owning_building_guid = 48)
      LocalObject(277, Door.Constructor(Vector3(4728.546f, 3589.93f, 70.81406f)), owning_building_guid = 48)
      LocalObject(278, Door.Constructor(Vector3(4728.7f, 3599.282f, 70.81406f)), owning_building_guid = 48)
      LocalObject(279, Door.Constructor(Vector3(4733.288f, 3594.672f, 70.81406f)), owning_building_guid = 48)
      LocalObject(280, Door.Constructor(Vector3(4733.442f, 3604.024f, 70.81406f)), owning_building_guid = 48)
      LocalObject(281, Door.Constructor(Vector3(4738.03f, 3599.413f, 70.81406f)), owning_building_guid = 48)
      LocalObject(392, Door.Constructor(Vector3(4730.455f, 3580.55f, 70.44806f)), owning_building_guid = 48)
      LocalObject(393, Door.Constructor(Vector3(4747.464f, 3597.559f, 70.44806f)), owning_building_guid = 48)
      LocalObject(457, Door.Constructor(Vector3(4677.84f, 3599.231f, 71.91306f)), owning_building_guid = 48)
      LocalObject(459, Door.Constructor(Vector3(4692.005f, 3607.768f, 71.91306f)), owning_building_guid = 48)
      LocalObject(462, Door.Constructor(Vector3(4706.143f, 3599.248f, 71.91306f)), owning_building_guid = 48)
      LocalObject(463, Door.Constructor(Vector3(4720.232f, 3636.005f, 71.91306f)), owning_building_guid = 48)
      LocalObject(464, Door.Constructor(Vector3(4728.769f, 3621.84f, 71.91306f)), owning_building_guid = 48)
      LocalObject(465, Door.Constructor(Vector3(4728.752f, 3650.143f, 71.91306f)), owning_building_guid = 48)
      LocalObject(955, Terminal.Constructor(Vector3(4722.388f, 3601.812f, 70.09806f), order_terminal), owning_building_guid = 48)
      LocalObject(956, Terminal.Constructor(Vector3(4724.247f, 3603.671f, 70.09806f), order_terminal), owning_building_guid = 48)
      LocalObject(957, Terminal.Constructor(Vector3(4726.155f, 3605.58f, 70.09806f), order_terminal), owning_building_guid = 48)
      LocalObject(958, Terminal.Constructor(Vector3(4735.832f, 3588.335f, 70.09806f), order_terminal), owning_building_guid = 48)
      LocalObject(959, Terminal.Constructor(Vector3(4737.741f, 3590.243f, 70.09806f), order_terminal), owning_building_guid = 48)
      LocalObject(960, Terminal.Constructor(Vector3(4739.6f, 3592.102f, 70.09806f), order_terminal), owning_building_guid = 48)
      LocalObject(816, SpawnTube.Constructor(Vector3(4724.571f, 3593.914f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 315)), owning_building_guid = 48)
      LocalObject(817, SpawnTube.Constructor(Vector3(4727.933f, 3590.552f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 135)), owning_building_guid = 48)
      LocalObject(818, SpawnTube.Constructor(Vector3(4729.314f, 3598.658f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 315)), owning_building_guid = 48)
      LocalObject(819, SpawnTube.Constructor(Vector3(4732.677f, 3595.296f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 135)), owning_building_guid = 48)
      LocalObject(820, SpawnTube.Constructor(Vector3(4734.058f, 3603.401f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 315)), owning_building_guid = 48)
      LocalObject(821, SpawnTube.Constructor(Vector3(4737.42f, 3600.039f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 135)), owning_building_guid = 48)
    }

    Building6()

    def Building6(): Unit = { // Name: Esamir_Spawn3 Type: VT_building_nc GUID: 49, MapID: 6
      LocalBuilding("Esamir_Spawn3", 49, 6, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4966f, 3658f, 68.73806f), VT_building_nc)))
      LocalObject(282, Door.Constructor(Vector3(4919.299f, 3622.264f, 70.81406f)), owning_building_guid = 49)
      LocalObject(283, Door.Constructor(Vector3(4923.958f, 3617.439f, 70.81406f)), owning_building_guid = 49)
      LocalObject(284, Door.Constructor(Vector3(4923.989f, 3626.771f, 70.81406f)), owning_building_guid = 49)
      LocalObject(285, Door.Constructor(Vector3(4928.615f, 3612.615f, 70.81406f)), owning_building_guid = 49)
      LocalObject(286, Door.Constructor(Vector3(4928.647f, 3621.946f, 70.81406f)), owning_building_guid = 49)
      LocalObject(287, Door.Constructor(Vector3(4933.306f, 3617.122f, 70.81406f)), owning_building_guid = 49)
      LocalObject(398, Door.Constructor(Vector3(4909.887f, 3620.519f, 70.44806f)), owning_building_guid = 49)
      LocalObject(399, Door.Constructor(Vector3(4926.597f, 3603.215f, 70.44806f)), owning_building_guid = 49)
      LocalObject(466, Door.Constructor(Vector3(4929.007f, 3644.5f, 71.91306f)), owning_building_guid = 49)
      LocalObject(467, Door.Constructor(Vector3(4929.484f, 3672.8f, 71.91306f)), owning_building_guid = 49)
      LocalObject(468, Door.Constructor(Vector3(4937.772f, 3658.488f, 71.91306f)), owning_building_guid = 49)
      LocalObject(469, Door.Constructor(Vector3(4951.201f, 3621.484f, 71.91306f)), owning_building_guid = 49)
      LocalObject(470, Door.Constructor(Vector3(4965.512f, 3629.772f, 71.91306f)), owning_building_guid = 49)
      LocalObject(471, Door.Constructor(Vector3(4979.5f, 3621.007f, 71.91306f)), owning_building_guid = 49)
      LocalObject(984, Terminal.Constructor(Vector3(4917.577f, 3615.007f, 70.09806f), order_terminal), owning_building_guid = 49)
      LocalObject(985, Terminal.Constructor(Vector3(4919.452f, 3613.064f, 70.09806f), order_terminal), owning_building_guid = 49)
      LocalObject(986, Terminal.Constructor(Vector3(4921.278f, 3611.173f, 70.09806f), order_terminal), owning_building_guid = 49)
      LocalObject(987, Terminal.Constructor(Vector3(4931.287f, 3628.213f, 70.09806f), order_terminal), owning_building_guid = 49)
      LocalObject(988, Terminal.Constructor(Vector3(4933.113f, 3626.322f, 70.09806f), order_terminal), owning_building_guid = 49)
      LocalObject(989, Terminal.Constructor(Vector3(4934.989f, 3624.381f, 70.09806f), order_terminal), owning_building_guid = 49)
      LocalObject(822, SpawnTube.Constructor(Vector3(4919.932f, 3622.866f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 226)), owning_building_guid = 49)
      LocalObject(823, SpawnTube.Constructor(Vector3(4923.352f, 3626.168f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 46)), owning_building_guid = 49)
      LocalObject(824, SpawnTube.Constructor(Vector3(4924.592f, 3618.04f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 226)), owning_building_guid = 49)
      LocalObject(825, SpawnTube.Constructor(Vector3(4928.013f, 3621.344f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 46)), owning_building_guid = 49)
      LocalObject(826, SpawnTube.Constructor(Vector3(4929.252f, 3613.215f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 226)), owning_building_guid = 49)
      LocalObject(827, SpawnTube.Constructor(Vector3(4932.672f, 3616.517f, 70.77006f), respawn_tube_sanctuary, Vector3(0, 0, 46)), owning_building_guid = 49)
    }

    Building34()

    def Building34(): Unit = { // Name: Cyssor_Air2 Type: vt_dropship GUID: 50, MapID: 34
      LocalBuilding("Cyssor_Air2", 50, 34, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2078f, 5496f, 65.20686f), vt_dropship)))
      LocalObject(300, Terminal.Constructor(Vector3(2101.469f, 5495.71f, 68.07486f), dropship_vehicle_terminal), owning_building_guid = 50)
      LocalObject(288, VehicleSpawnPad.Constructor(Vector3(2081.589f, 5495.958f, 61.22186f), dropship_pad_doors, Vector3(0, 0, 181)), owning_building_guid = 50, terminal_guid = 300)
    }

    Building38()

    def Building38(): Unit = { // Name: Cyssor_Air1 Type: vt_dropship GUID: 51, MapID: 38
      LocalBuilding("Cyssor_Air1", 51, 38, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2078f, 5572f, 65.20757f), vt_dropship)))
      LocalObject(301, Terminal.Constructor(Vector3(2101.469f, 5571.71f, 68.07558f), dropship_vehicle_terminal), owning_building_guid = 51)
      LocalObject(289, VehicleSpawnPad.Constructor(Vector3(2081.589f, 5571.958f, 61.22257f), dropship_pad_doors, Vector3(0, 0, 181)), owning_building_guid = 51, terminal_guid = 301)
    }

    Building39()

    def Building39(): Unit = { // Name: Cyssor_Air4 Type: vt_dropship GUID: 52, MapID: 39
      LocalBuilding("Cyssor_Air4", 52, 39, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2290f, 5386f, 65.21352f), vt_dropship)))
      LocalObject(302, Terminal.Constructor(Vector3(2290.29f, 5409.469f, 68.08153f), dropship_vehicle_terminal), owning_building_guid = 52)
      LocalObject(290, VehicleSpawnPad.Constructor(Vector3(2290.043f, 5389.589f, 61.22852f), dropship_pad_doors, Vector3(0, 0, 91)), owning_building_guid = 52, terminal_guid = 302)
    }

    Building33()

    def Building33(): Unit = { // Name: Cyssor_Air3 Type: vt_dropship GUID: 53, MapID: 33
      LocalBuilding("Cyssor_Air3", 53, 33, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2464f, 5640f, 65.20798f), vt_dropship)))
      LocalObject(303, Terminal.Constructor(Vector3(2446.639f, 5624.207f, 68.07599f), dropship_vehicle_terminal), owning_building_guid = 53)
      LocalObject(291, VehicleSpawnPad.Constructor(Vector3(2461.346f, 5637.583f, 61.22298f), dropship_pad_doors, Vector3(0, 0, -42)), owning_building_guid = 53, terminal_guid = 303)
    }

    Building22()

    def Building22(): Unit = { // Name: amerish_air3 Type: vt_dropship GUID: 54, MapID: 22
      LocalBuilding("amerish_air3", 54, 22, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3926f, 6060f, 43.87661f), vt_dropship)))
      LocalObject(304, Terminal.Constructor(Vector3(3949.469f, 6059.71f, 46.74461f), dropship_vehicle_terminal), owning_building_guid = 54)
      LocalObject(292, VehicleSpawnPad.Constructor(Vector3(3929.589f, 6059.958f, 39.89161f), dropship_pad_doors, Vector3(0, 0, 181)), owning_building_guid = 54, terminal_guid = 304)
    }

    Building25()

    def Building25(): Unit = { // Name: amerish_air4 Type: vt_dropship GUID: 55, MapID: 25
      LocalBuilding("amerish_air4", 55, 25, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3986f, 6176f, 43.87661f), vt_dropship)))
      LocalObject(305, Terminal.Constructor(Vector3(4009.469f, 6175.71f, 46.74461f), dropship_vehicle_terminal), owning_building_guid = 55)
      LocalObject(293, VehicleSpawnPad.Constructor(Vector3(3989.589f, 6175.958f, 39.89161f), dropship_pad_doors, Vector3(0, 0, 181)), owning_building_guid = 55, terminal_guid = 305)
    }

    Building24()

    def Building24(): Unit = { // Name: amerish_air2 Type: vt_dropship GUID: 56, MapID: 24
      LocalBuilding("amerish_air2", 56, 24, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4320f, 6168f, 43.87661f), vt_dropship)))
      LocalObject(306, Terminal.Constructor(Vector3(4296.531f, 6168.29f, 46.74461f), dropship_vehicle_terminal), owning_building_guid = 56)
      LocalObject(294, VehicleSpawnPad.Constructor(Vector3(4316.411f, 6168.042f, 39.89161f), dropship_pad_doors, Vector3(0, 0, 1)), owning_building_guid = 56, terminal_guid = 306)
    }

    Building21()

    def Building21(): Unit = { // Name: amerish_air1 Type: vt_dropship GUID: 57, MapID: 21
      LocalBuilding("amerish_air1", 57, 21, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4376f, 6058f, 43.87661f), vt_dropship)))
      LocalObject(307, Terminal.Constructor(Vector3(4352.531f, 6058.29f, 46.74461f), dropship_vehicle_terminal), owning_building_guid = 57)
      LocalObject(295, VehicleSpawnPad.Constructor(Vector3(4372.411f, 6058.042f, 39.89161f), dropship_pad_doors, Vector3(0, 0, 1)), owning_building_guid = 57, terminal_guid = 307)
    }

    Building14()

    def Building14(): Unit = { // Name: Esamir_Air2 Type: vt_dropship GUID: 58, MapID: 14
      LocalBuilding("Esamir_Air2", 58, 14, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4620f, 3466f, 68.73806f), vt_dropship)))
      LocalObject(308, Terminal.Constructor(Vector3(4636.511f, 3482.681f, 71.60606f), dropship_vehicle_terminal), owning_building_guid = 58)
      LocalObject(296, VehicleSpawnPad.Constructor(Vector3(4622.523f, 3468.552f, 64.75306f), dropship_pad_doors, Vector3(0, 0, 135)), owning_building_guid = 58, terminal_guid = 308)
    }

    Building17()

    def Building17(): Unit = { // Name: Esamir_Air1 Type: vt_dropship GUID: 59, MapID: 17
      LocalBuilding("Esamir_Air1", 59, 17, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4768f, 3318f, 68.73806f), vt_dropship)))
      LocalObject(309, Terminal.Constructor(Vector3(4784.511f, 3334.681f, 71.60606f), dropship_vehicle_terminal), owning_building_guid = 59)
      LocalObject(297, VehicleSpawnPad.Constructor(Vector3(4770.523f, 3320.552f, 64.75306f), dropship_pad_doors, Vector3(0, 0, 135)), owning_building_guid = 59, terminal_guid = 309)
    }

    Building15()

    def Building15(): Unit = { // Name: Esamir_Air4 Type: vt_dropship GUID: 60, MapID: 15
      LocalBuilding("Esamir_Air4", 60, 15, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4868f, 3696f, 68.73806f), vt_dropship)))
      LocalObject(310, Terminal.Constructor(Vector3(4851.489f, 3679.319f, 71.60606f), dropship_vehicle_terminal), owning_building_guid = 60)
      LocalObject(298, VehicleSpawnPad.Constructor(Vector3(4865.477f, 3693.448f, 64.75306f), dropship_pad_doors, Vector3(0, 0, -45)), owning_building_guid = 60, terminal_guid = 310)
    }

    Building16()

    def Building16(): Unit = { // Name: Esamir_Air3 Type: vt_dropship GUID: 61, MapID: 16
      LocalBuilding("Esamir_Air3", 61, 16, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5010f, 3556f, 68.73806f), vt_dropship)))
      LocalObject(311, Terminal.Constructor(Vector3(4993.489f, 3539.319f, 71.60606f), dropship_vehicle_terminal), owning_building_guid = 61)
      LocalObject(299, VehicleSpawnPad.Constructor(Vector3(5007.477f, 3553.448f, 64.75306f), dropship_pad_doors, Vector3(0, 0, -45)), owning_building_guid = 61, terminal_guid = 311)
    }

    Building67()

    def Building67(): Unit = { // Name: NC_NW_Tport_04 Type: vt_spawn GUID: 62, MapID: 67
      LocalBuilding("NC_NW_Tport_04", 62, 67, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2132f, 5532f, 65.20512f), vt_spawn)))
    }

    Building69()

    def Building69(): Unit = { // Name: NC_NW_Tport_02 Type: vt_spawn GUID: 63, MapID: 69
      LocalBuilding("NC_NW_Tport_02", 63, 69, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2242f, 5432f, 65.2088f), vt_spawn)))
    }

    Building70()

    def Building70(): Unit = { // Name: NC_NW_Tport_01 Type: vt_spawn GUID: 64, MapID: 70
      LocalBuilding("NC_NW_Tport_01", 64, 70, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2258f, 5642f, 65.20429f), vt_spawn)))
    }

    Building68()

    def Building68(): Unit = { // Name: NC_NW_Tport_03 Type: vt_spawn GUID: 65, MapID: 68
      LocalBuilding("NC_NW_Tport_03", 65, 68, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2388f, 5618f, 65.20798f), vt_spawn)))
    }

    Building66()

    def Building66(): Unit = { // Name: NC_NE_Tport_02 Type: vt_spawn GUID: 66, MapID: 66
      LocalBuilding("NC_NE_Tport_02", 66, 66, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4060f, 5986f, 43.87661f), vt_spawn)))
    }

    Building65()

    def Building65(): Unit = { // Name: NC_NE_Tport_03 Type: vt_spawn GUID: 67, MapID: 65
      LocalBuilding("NC_NE_Tport_03", 67, 65, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4156f, 6160f, 43.87661f), vt_spawn)))
    }

    Building63()

    def Building63(): Unit = { // Name: NC_NE_Tport_01 Type: vt_spawn GUID: 68, MapID: 63
      LocalBuilding("NC_NE_Tport_01", 68, 63, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4212f, 5954f, 43.87661f), vt_spawn)))
    }

    Building64()

    def Building64(): Unit = { // Name: NC_NE_Tport_04 Type: vt_spawn GUID: 69, MapID: 64
      LocalBuilding("NC_NE_Tport_04", 69, 64, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4258f, 6148f, 43.87661f), vt_spawn)))
    }

    Building59()

    def Building59(): Unit = { // Name: NC_SE_Tport_04 Type: vt_spawn GUID: 70, MapID: 59
      LocalBuilding("NC_SE_Tport_04", 70, 59, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4698f, 3506f, 68.73806f), vt_spawn)))
    }

    Building60()

    def Building60(): Unit = { // Name: NC_SE_Tport_03 Type: vt_spawn GUID: 71, MapID: 60
      LocalBuilding("NC_SE_Tport_03", 71, 60, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4818f, 3622f, 68.73806f), vt_spawn)))
    }

    Building61()

    def Building61(): Unit = { // Name: NC_SE_Tport_02 Type: vt_spawn GUID: 72, MapID: 61
      LocalBuilding("NC_SE_Tport_02", 72, 61, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4826f, 3398f, 68.73806f), vt_spawn)))
    }

    Building62()

    def Building62(): Unit = { // Name: NC_SE_Tport_01 Type: vt_spawn GUID: 73, MapID: 62
      LocalBuilding("NC_SE_Tport_01", 73, 62, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4922f, 3500f, 68.73806f), vt_spawn)))
    }

    Building42()

    def Building42(): Unit = { // Name: Cyssor_Vehicle6 Type: vt_vehicle GUID: 74, MapID: 42
      LocalBuilding("Cyssor_Vehicle6", 74, 42, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2078f, 5642f, 65.20348f), vt_vehicle)))
      LocalObject(1144, Terminal.Constructor(Vector3(2078.008f, 5627.508f, 67.89047f), ground_vehicle_terminal), owning_building_guid = 74)
      LocalObject(756, VehicleSpawnPad.Constructor(Vector3(2077.973f, 5642.147f, 63.73248f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 74, terminal_guid = 1144)
    }

    Building32()

    def Building32(): Unit = { // Name: Cyssor_Vehicle1 Type: vt_vehicle GUID: 75, MapID: 32
      LocalBuilding("Cyssor_Vehicle1", 75, 32, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2082f, 5418f, 65.21249f), vt_vehicle)))
      LocalObject(1145, Terminal.Constructor(Vector3(2096.492f, 5418.008f, 67.89949f), ground_vehicle_terminal), owning_building_guid = 75)
      LocalObject(757, VehicleSpawnPad.Constructor(Vector3(2081.853f, 5417.973f, 63.74149f), mb_pad_creation, Vector3(0, 0, -90)), owning_building_guid = 75, terminal_guid = 1145)
    }

    Building41()

    def Building41(): Unit = { // Name: Cyssor_Vehicle5 Type: vt_vehicle GUID: 76, MapID: 41
      LocalBuilding("Cyssor_Vehicle5", 76, 41, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2186f, 5662f, 65.20142f), vt_vehicle)))
      LocalObject(1146, Terminal.Constructor(Vector3(2186.008f, 5647.508f, 67.88842f), ground_vehicle_terminal), owning_building_guid = 76)
      LocalObject(758, VehicleSpawnPad.Constructor(Vector3(2185.973f, 5662.147f, 63.73042f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 76, terminal_guid = 1146)
    }

    Building43()

    def Building43(): Unit = { // Name: Cyssor_Vehicle4 Type: vt_vehicle GUID: 77, MapID: 43
      LocalBuilding("Cyssor_Vehicle4", 77, 43, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2328f, 5662f, 65.20798f), vt_vehicle)))
      LocalObject(1147, Terminal.Constructor(Vector3(2327.755f, 5647.51f, 67.89498f), ground_vehicle_terminal), owning_building_guid = 77)
      LocalObject(759, VehicleSpawnPad.Constructor(Vector3(2327.976f, 5662.147f, 63.73698f), mb_pad_creation, Vector3(0, 0, 1)), owning_building_guid = 77, terminal_guid = 1147)
    }

    Building40()

    def Building40(): Unit = { // Name: Cyssor_Vehicle3 Type: vt_vehicle GUID: 78, MapID: 40
      LocalBuilding("Cyssor_Vehicle3", 78, 40, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2410f, 5694f, 65.20798f), vt_vehicle)))
      LocalObject(1148, Terminal.Constructor(Vector3(2419.889f, 5683.407f, 67.89498f), ground_vehicle_terminal), owning_building_guid = 78)
      LocalObject(760, VehicleSpawnPad.Constructor(Vector3(2409.88f, 5694.089f, 63.73698f), mb_pad_creation, Vector3(0, 0, -43)), owning_building_guid = 78, terminal_guid = 1148)
    }

    Building31()

    def Building31(): Unit = { // Name: Cyssor_Vehicle2 Type: vt_vehicle GUID: 79, MapID: 31
      LocalBuilding("Cyssor_Vehicle2", 79, 31, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2468f, 5458f, 65.2127f), vt_vehicle)))
      LocalObject(1149, Terminal.Constructor(Vector3(2453.51f, 5457.739f, 67.8997f), ground_vehicle_terminal), owning_building_guid = 79)
      LocalObject(761, VehicleSpawnPad.Constructor(Vector3(2468.146f, 5458.03f, 63.7417f), mb_pad_creation, Vector3(0, 0, 89)), owning_building_guid = 79, terminal_guid = 1149)
    }

    Building19()

    def Building19(): Unit = { // Name: amerish_vehicle1 Type: vt_vehicle GUID: 80, MapID: 19
      LocalBuilding("amerish_vehicle1", 80, 19, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3990f, 6094f, 43.87661f), vt_vehicle)))
      LocalObject(1151, Terminal.Constructor(Vector3(3999.889f, 6083.407f, 46.56361f), ground_vehicle_terminal), owning_building_guid = 80)
      LocalObject(762, VehicleSpawnPad.Constructor(Vector3(3989.88f, 6094.089f, 42.40561f), mb_pad_creation, Vector3(0, 0, -43)), owning_building_guid = 80, terminal_guid = 1151)
    }

    Building29()

    def Building29(): Unit = { // Name: amerish_vehicle2 Type: vt_vehicle GUID: 81, MapID: 29
      LocalBuilding("amerish_vehicle2", 81, 29, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4000f, 6248f, 43.87661f), vt_vehicle)))
      LocalObject(1150, Terminal.Constructor(Vector3(3999.502f, 6233.517f, 46.56361f), ground_vehicle_terminal), owning_building_guid = 81)
      LocalObject(763, VehicleSpawnPad.Constructor(Vector3(3999.978f, 6248.148f, 42.40561f), mb_pad_creation, Vector3(0, 0, 2)), owning_building_guid = 81, terminal_guid = 1150)
    }

    Building28()

    def Building28(): Unit = { // Name: amerish_vehicle3 Type: vt_vehicle GUID: 82, MapID: 28
      LocalBuilding("amerish_vehicle3", 82, 28, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4104f, 6194f, 43.87661f), vt_vehicle)))
      LocalObject(1152, Terminal.Constructor(Vector3(4103.755f, 6179.51f, 46.56361f), ground_vehicle_terminal), owning_building_guid = 82)
      LocalObject(764, VehicleSpawnPad.Constructor(Vector3(4103.976f, 6194.147f, 42.40561f), mb_pad_creation, Vector3(0, 0, 1)), owning_building_guid = 82, terminal_guid = 1152)
    }

    Building27()

    def Building27(): Unit = { // Name: amerish_vehicle4 Type: vt_vehicle GUID: 83, MapID: 27
      LocalBuilding("amerish_vehicle4", 83, 27, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4200f, 6196f, 43.87661f), vt_vehicle)))
      LocalObject(1153, Terminal.Constructor(Vector3(4199.755f, 6181.51f, 46.56361f), ground_vehicle_terminal), owning_building_guid = 83)
      LocalObject(765, VehicleSpawnPad.Constructor(Vector3(4199.976f, 6196.147f, 42.40561f), mb_pad_creation, Vector3(0, 0, 1)), owning_building_guid = 83, terminal_guid = 1153)
    }

    Building26()

    def Building26(): Unit = { // Name: amerish_vehicle5 Type: vt_vehicle GUID: 84, MapID: 26
      LocalBuilding("amerish_vehicle5", 84, 26, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4304f, 6244f, 43.92173f), vt_vehicle)))
      LocalObject(1154, Terminal.Constructor(Vector3(4303.755f, 6229.51f, 46.60873f), ground_vehicle_terminal), owning_building_guid = 84)
      LocalObject(766, VehicleSpawnPad.Constructor(Vector3(4303.976f, 6244.147f, 42.45073f), mb_pad_creation, Vector3(0, 0, 1)), owning_building_guid = 84, terminal_guid = 1154)
    }

    Building20()

    def Building20(): Unit = { // Name: amerish_vehicle6 Type: vt_vehicle GUID: 85, MapID: 20
      LocalBuilding("amerish_vehicle6", 85, 20, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4316f, 6094f, 43.87661f), vt_vehicle)))
      LocalObject(1155, Terminal.Constructor(Vector3(4305.939f, 6083.57f, 46.56361f), ground_vehicle_terminal), owning_building_guid = 85)
      LocalObject(767, VehicleSpawnPad.Constructor(Vector3(4316.083f, 6094.125f, 42.40561f), mb_pad_creation, Vector3(0, 0, 44)), owning_building_guid = 85, terminal_guid = 1155)
    }

    Building12()

    def Building12(): Unit = { // Name: Esamir_Vehicle1 Type: vt_vehicle GUID: 86, MapID: 12
      LocalBuilding("Esamir_Vehicle1", 86, 12, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4644f, 3540f, 68.73806f), vt_vehicle)))
      LocalObject(1156, Terminal.Constructor(Vector3(4654.061f, 3550.43f, 71.42506f), ground_vehicle_terminal), owning_building_guid = 86)
      LocalObject(768, VehicleSpawnPad.Constructor(Vector3(4643.917f, 3539.875f, 67.26706f), mb_pad_creation, Vector3(0, 0, 224)), owning_building_guid = 86, terminal_guid = 1156)
    }

    Building13()

    def Building13(): Unit = { // Name: Esamir_Vehicle2 Type: vt_vehicle GUID: 87, MapID: 13
      LocalBuilding("Esamir_Vehicle2", 87, 13, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4782f, 3680f, 68.73806f), vt_vehicle)))
      LocalObject(1157, Terminal.Constructor(Vector3(4771.758f, 3669.747f, 71.42506f), ground_vehicle_terminal), owning_building_guid = 87)
      LocalObject(769, VehicleSpawnPad.Constructor(Vector3(4782.085f, 3680.123f, 67.26706f), mb_pad_creation, Vector3(0, 0, 45)), owning_building_guid = 87, terminal_guid = 1157)
    }

    Building8()

    def Building8(): Unit = { // Name: Esamir_Vehicle6 Type: vt_vehicle GUID: 88, MapID: 8
      LocalBuilding("Esamir_Vehicle6", 88, 8, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4834f, 3276f, 68.73806f), vt_vehicle)))
      LocalObject(1158, Terminal.Constructor(Vector3(4823.747f, 3286.242f, 71.42506f), ground_vehicle_terminal), owning_building_guid = 88)
      LocalObject(770, VehicleSpawnPad.Constructor(Vector3(4834.123f, 3275.915f, 67.26706f), mb_pad_creation, Vector3(0, 0, 135)), owning_building_guid = 88, terminal_guid = 1158)
    }

    Building9()

    def Building9(): Unit = { // Name: Esamir_Vehicle5 Type: vt_vehicle GUID: 89, MapID: 9
      LocalBuilding("Esamir_Vehicle5", 89, 9, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4878f, 3378f, 68.73806f), vt_vehicle)))
      LocalObject(1159, Terminal.Constructor(Vector3(4867.747f, 3388.242f, 71.42506f), ground_vehicle_terminal), owning_building_guid = 89)
      LocalObject(771, VehicleSpawnPad.Constructor(Vector3(4878.123f, 3377.915f, 67.26706f), mb_pad_creation, Vector3(0, 0, 135)), owning_building_guid = 89, terminal_guid = 1159)
    }

    Building10()

    def Building10(): Unit = { // Name: Esamir_Vehicle4 Type: vt_vehicle GUID: 90, MapID: 10
      LocalBuilding("Esamir_Vehicle4", 90, 10, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(4948f, 3446f, 68.73806f), vt_vehicle)))
      LocalObject(1160, Terminal.Constructor(Vector3(4937.747f, 3456.242f, 71.42506f), ground_vehicle_terminal), owning_building_guid = 90)
      LocalObject(772, VehicleSpawnPad.Constructor(Vector3(4948.123f, 3445.915f, 67.26706f), mb_pad_creation, Vector3(0, 0, 135)), owning_building_guid = 90, terminal_guid = 1160)
    }

    Building11()

    def Building11(): Unit = { // Name: Esamir_Vehicle3 Type: vt_vehicle GUID: 91, MapID: 11
      LocalBuilding("Esamir_Vehicle3", 91, 11, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5048f, 3492f, 68.73806f), vt_vehicle)))
      LocalObject(1161, Terminal.Constructor(Vector3(5037.927f, 3502.419f, 71.42506f), ground_vehicle_terminal), owning_building_guid = 91)
      LocalObject(773, VehicleSpawnPad.Constructor(Vector3(5048.122f, 3491.913f, 67.26706f), mb_pad_creation, Vector3(0, 0, 136)), owning_building_guid = 91, terminal_guid = 1161)
    }

    Building1()

    def Building1(): Unit = { // Name: WG_NCSanc_to_Cyssor Type: warpgate GUID: 92, MapID: 1
      LocalBuilding("WG_NCSanc_to_Cyssor", 92, 1, FoundationBuilder(WarpGate.Structure(Vector3(2190f, 5954f, 46.85717f))))
    }

    Building2()

    def Building2(): Unit = { // Name: WG_NCSanc_to_Amerish Type: warpgate GUID: 93, MapID: 2
      LocalBuilding("WG_NCSanc_to_Amerish", 93, 2, FoundationBuilder(WarpGate.Structure(Vector3(4156f, 6490f, 43.88584f))))
    }

    Building3()

    def Building3(): Unit = { // Name: WG_NCSanc_to_Esamir Type: warpgate GUID: 94, MapID: 3
      LocalBuilding("WG_NCSanc_to_Esamir", 94, 3, FoundationBuilder(WarpGate.Structure(Vector3(5174f, 3174f, 56.06128f))))
    }

    def Lattice(): Unit = {
    }

    Lattice()

  }
}
