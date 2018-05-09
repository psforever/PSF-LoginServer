// Copyright (c) 2017 PSForever
import net.psforever.objects.zones.ZoneMap
import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.{ProximityTerminal, Terminal}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.types.Vector3

object Maps {
  val map1 = new ZoneMap("map01")

  val map2 = new ZoneMap("map02")

  val map3 = new ZoneMap("map03")

  val map4 = new ZoneMap("map04") {

    Building9()
    Building10()
    Building11()
    Building25()
    Building33()
    Building34()
    Building35()
    Building36()
    Building55()
    Building56()
    Building59()
    Building65()

    def Building9() : Unit = { // Girru
      LocalBuilding(9, FoundationBuilder(Building.Structure(StructureType.Facility, Vector3(4397f, 5895f, 0)))) // Todo change pos
      LocalObject(513, Door.Constructor)
      LocalObject(514, Door.Constructor)
      LocalObject(515, Door.Constructor)
      LocalObject(516, Door.Constructor)
      LocalObject(517, Door.Constructor)
      LocalObject(518, Door.Constructor)
      LocalObject(519, Door.Constructor(Vector3(4342.7266f, 5833.461f, 77.890625f), Vector3(0, 0, 272)))
      LocalObject(520, Door.Constructor)
      LocalObject(521, Door.Constructor)
      LocalObject(522, Door.Constructor(Vector3(4395.7188f, 5908.2188f, 92.90625f), Vector3(0, 0, 2)))
      LocalObject(523, Door.Constructor(Vector3(4402.086f, 5891.914f, 92.90625f), Vector3(0, 0, 182)))
      LocalObject(524, Door.Constructor)
      LocalObject(535, Door.Constructor)
      LocalObject(536, Door.Constructor)
      LocalObject(537, Door.Constructor)
      LocalObject(653, Door.Constructor(Vector3(4339.6953f, 5973.828f, 77.875f), Vector3(0, 0, 2)))
      LocalObject(657, Door.Constructor)
      LocalObject(810, Door.Constructor)
      LocalObject(811, Door.Constructor)
      LocalObject(812, Door.Constructor)
      LocalObject(813, Door.Constructor)
      LocalObject(814, Door.Constructor)
      LocalObject(815, Door.Constructor)
      LocalObject(816, Door.Constructor)
      LocalObject(817, Door.Constructor(Vector3(4380.5938f, 5892.047f, 92.890625f), Vector3(0, 0, 2)))
      LocalObject(818, Door.Constructor)
      LocalObject(819, Door.Constructor)
      LocalObject(820, Door.Constructor)
      LocalObject(821, Door.Constructor)
      LocalObject(822, Door.Constructor)
      LocalObject(823, Door.Constructor)
      LocalObject(824, Door.Constructor(Vector3(4404.328f, 5883.4453f, 70.40625f), Vector3(0, 0, 182)))
      LocalObject(825, Door.Constructor(Vector3(4405.133f, 5907.117f, 70.390625f), Vector3(0, 0, 2)))
      LocalObject(826, Door.Constructor(Vector3(4413.3984f, 5914.9688f, 62.890625f), Vector3(0, 0, 182)))
      LocalObject(827, Door.Constructor(Vector3(4416.4297f, 5886.867f, 62.890625f), Vector3(0, 0, 272)))
      LocalObject(828, Door.Constructor)
      LocalObject(925, Door.Constructor(Vector3(4424.492f, 5884.75f, 77.953125f), Vector3(0, 0, 182)))
      LocalObject(973, IFFLock.Constructor)
      LocalObject(980, IFFLock.Constructor)
      LocalObject(1142, IFFLock.Constructor)
      LocalObject(1143, IFFLock.Constructor)
      LocalObject(1144, IFFLock.Constructor)
      LocalObject(1145, IFFLock.Constructor)
      LocalObject(1146, IFFLock.Constructor)
      LocalObject(1147, IFFLock.Constructor)
      LocalObject(1148, IFFLock.Constructor)
      LocalObject(1149, IFFLock.Constructor)
      LocalObject(1517, Locker.Constructor) // TODO add other lockers !
      LocalObject(2014, Terminal.Constructor(order_terminal))
      LocalObject(2015, Terminal.Constructor(order_terminal))
      LocalObject(2016, Terminal.Constructor(order_terminal))
      LocalObject(2017, Terminal.Constructor(order_terminal))
      LocalObject(2724, SpawnTube.Constructor(Vector3(4396.7656f, 5888.258f, 71.15625f), Vector3(0, 0, 92)))
      LocalObject(2725, SpawnTube.Constructor(Vector3(4397.211f, 5895.547f, 71.15625f), Vector3(0, 0, 92)))
      LocalObject(2726, SpawnTube.Constructor(Vector3(4397.2344f, 5902.8203f, 71.15625f), Vector3(0, 0, 92)))
      LocalObject(2948, Door.Constructor) //spawn tube door
      LocalObject(2949, Door.Constructor) //spawn tube door
      LocalObject(2950, Door.Constructor) //spawn tube door
      LocalObject(2853, Terminal.Constructor(spawn_terminal))
      LocalObject(2854, Terminal.Constructor(spawn_terminal))
      LocalObject(2859, Terminal.Constructor(spawn_terminal))
      LocalObject(3072, Terminal.Constructor(ground_vehicle_terminal))
      LocalObject(501,
        //      VehicleSpawnPad.Constructor(Vector3(4337.0f, 5903.0f, 58.0f), Vector3(0f, 0f, 0f))
        VehicleSpawnPad.Constructor(Vector3(4340.0f, 5985.0f, 78.0f), Vector3(0f, 0f, 0f))) //TODO guid & position not correct
      LocalObject(176, Terminal.Constructor(air_vehicle_terminal))
      LocalObject(502, VehicleSpawnPad.Constructor(Vector3(4386.0f, 5928.0f, 93.0f), Vector3(0f, 0f, 0f))) //TODO guid not correct
      LocalObject(177, Terminal.Constructor(air_vehicle_terminal))
      LocalObject(503, VehicleSpawnPad.Constructor(Vector3(4407.0f, 5927.0f, 93.0f), Vector3(0f, 0f, 0f))) //TODO guid not correct

      LocalObject(1909, ProximityTerminal.Constructor(medical_terminal))
      LocalObject(1910, ProximityTerminal.Constructor(medical_terminal))
      
      ObjectToBuilding(513, 9)
      ObjectToBuilding(514, 9)
      ObjectToBuilding(515, 9)
      ObjectToBuilding(516, 9)
      ObjectToBuilding(517, 9)
      ObjectToBuilding(518, 9)
      ObjectToBuilding(519, 9)
      ObjectToBuilding(520, 9)
      ObjectToBuilding(521, 9)
      ObjectToBuilding(522, 9)
      ObjectToBuilding(523, 9)
      ObjectToBuilding(524, 9)
      ObjectToBuilding(535, 9)
      ObjectToBuilding(536, 9)
      ObjectToBuilding(537, 9)
      ObjectToBuilding(653, 9)
      ObjectToBuilding(657, 9)
      ObjectToBuilding(810, 9)
      ObjectToBuilding(811, 9)
      ObjectToBuilding(812, 9)
      ObjectToBuilding(813, 9)
      ObjectToBuilding(814, 9)
      ObjectToBuilding(815, 9)
      ObjectToBuilding(816, 9)
      ObjectToBuilding(817, 9)
      ObjectToBuilding(818, 9)
      ObjectToBuilding(819, 9)
      ObjectToBuilding(820, 9)
      ObjectToBuilding(821, 9)
      ObjectToBuilding(822, 9)
      ObjectToBuilding(823, 9)
      ObjectToBuilding(824, 9)
      ObjectToBuilding(825, 9)
      ObjectToBuilding(826, 9)
      ObjectToBuilding(827, 9)
      ObjectToBuilding(828, 9)
      ObjectToBuilding(925, 9)
      ObjectToBuilding(973, 9)
      ObjectToBuilding(980, 9)
      ObjectToBuilding(1142, 9)
      ObjectToBuilding(1143, 9)
      ObjectToBuilding(1144, 9)
      ObjectToBuilding(1145, 9)
      ObjectToBuilding(1146, 9)
      ObjectToBuilding(1147, 9)
      ObjectToBuilding(1148, 9)
      ObjectToBuilding(1149, 9)
      ObjectToBuilding(1517, 9)
      ObjectToBuilding(1909, 9)
      ObjectToBuilding(1910, 9)
      ObjectToBuilding(2014, 9)
      ObjectToBuilding(2015, 9)
      ObjectToBuilding(2016, 9)
      ObjectToBuilding(2017, 9)
      ObjectToBuilding(2724, 9)
      ObjectToBuilding(2725, 9)
      ObjectToBuilding(2726, 9)
      ObjectToBuilding(2948, 9)
      ObjectToBuilding(2949, 9)
      ObjectToBuilding(2950, 9)
      ObjectToBuilding(2853, 9)
      ObjectToBuilding(2854, 9)
      ObjectToBuilding(2859, 9)
      ObjectToBuilding(3072, 9)
      ObjectToBuilding(501, 9)
      ObjectToBuilding(176, 9)
      ObjectToBuilding(502, 9)
      ObjectToBuilding(177, 9)
      ObjectToBuilding(503, 9)

      DoorToLock(519, 1142)
      DoorToLock(522, 1144)
      DoorToLock(523, 1146)
      DoorToLock(653, 980)
      DoorToLock(817, 1143)
      DoorToLock(824, 1147)
      DoorToLock(825, 1145)
      DoorToLock(826, 1148)
      DoorToLock(827, 1149)
      DoorToLock(925, 973)
      TerminalToSpawnPad(3072, 501)
      TerminalToSpawnPad(176, 502)
      TerminalToSpawnPad(177, 503)

    }

    def Building10() : Unit = { // Hanish
      LocalBuilding(10, FoundationBuilder(Building.Structure(StructureType.Facility, Vector3(3749f, 5477f, 0)))) // Todo change pos
      LocalObject(464, Door.Constructor)
      LocalObject(470, Door.Constructor(Vector3(3645.3984f, 5451.9688f, 88.890625f), Vector3(0, 0, 182)))
      LocalObject(471, Door.Constructor)
      LocalObject(472, Door.Constructor)
      LocalObject(473, Door.Constructor)
      LocalObject(474, Door.Constructor)
      LocalObject(475, Door.Constructor(Vector3(3740.7422f, 5477.8906f, 98.890625f), Vector3(0, 0, 92)))
      LocalObject(476, Door.Constructor)
      LocalObject(481, Door.Constructor(Vector3(3758.25f, 5497.8047f, 98.890625f), Vector3(0, 0, 182)))
      LocalObject(482, Door.Constructor)
      LocalObject(483, Door.Constructor)
      LocalObject(484, Door.Constructor)
      LocalObject(763, Door.Constructor)
      LocalObject(764, Door.Constructor)
      LocalObject(765, Door.Constructor)
      LocalObject(766, Door.Constructor)
      LocalObject(767, Door.Constructor)
      LocalObject(768, Door.Constructor)
      LocalObject(769, Door.Constructor)
      LocalObject(770, Door.Constructor)
      LocalObject(771, Door.Constructor(Vector3(3738.039f, 5470.0547f, 78.890625f), Vector3(0, 0, 272)))
      LocalObject(772, Door.Constructor)
      LocalObject(773, Door.Constructor)
      LocalObject(774, Door.Constructor(Vector3(3741.5312f, 5457.7734f, 71.390625f), Vector3(0, 0, 2)))
      LocalObject(775, Door.Constructor)
      LocalObject(776, Door.Constructor)
      LocalObject(777, Door.Constructor)
      LocalObject(778, Door.Constructor)
      LocalObject(779, Door.Constructor(Vector3(3761.4531f, 5469.5703f, 78.890625f), Vector3(0, 0, 92)))
      LocalObject(780, Door.Constructor)
      LocalObject(781, Door.Constructor)
      LocalObject(782, Door.Constructor)
      LocalObject(783, Door.Constructor)
      LocalObject(784, Door.Constructor(Vector3(3806.3906f, 5504.9766f, 78.890625f), Vector3(0, 0, 182)))
      LocalObject(785, Door.Constructor(Vector3(3806.5938f, 5512.5703f, 78.890625f), Vector3(0, 0, 2)))
      LocalObject(923, Door.Constructor(Vector3(3762.7266f, 5477.539f, 88.953125f), Vector3(0, 0, 92)))
      LocalObject(932, Door.Constructor)
      LocalObject(933, Door.Constructor)

      LocalObject(971, IFFLock.Constructor)
      LocalObject(1105, IFFLock.Constructor)
      LocalObject(1106, IFFLock.Constructor)
      LocalObject(1108, IFFLock.Constructor)
      LocalObject(1113, IFFLock.Constructor)
      LocalObject(1114, IFFLock.Constructor)
      LocalObject(1115, IFFLock.Constructor)
      LocalObject(1116, IFFLock.Constructor)

      LocalObject(1461, Locker.Constructor)

      LocalObject(238, Terminal.Constructor(cert_terminal))
      LocalObject(239, Terminal.Constructor(cert_terminal))
      LocalObject(240, Terminal.Constructor(cert_terminal))
      LocalObject(241, Terminal.Constructor(cert_terminal))
      LocalObject(242, Terminal.Constructor(cert_terminal))
      LocalObject(243, Terminal.Constructor(cert_terminal))
      LocalObject(244, Terminal.Constructor(cert_terminal))
      LocalObject(245, Terminal.Constructor(cert_terminal))

      LocalObject(948, ImplantTerminalMech.Constructor)
      LocalObject(949, ImplantTerminalMech.Constructor)

      LocalObject(10000, Terminal.Constructor(implant_terminal_interface))
      LocalObject(10001, Terminal.Constructor(implant_terminal_interface))

      LocalObject(1991, Terminal.Constructor(order_terminal))
      LocalObject(1992, Terminal.Constructor(order_terminal))
      LocalObject(1993, Terminal.Constructor(order_terminal))
      LocalObject(1994, Terminal.Constructor(order_terminal))
      LocalObject(2710, SpawnTube.Constructor(Vector3(3742.6016f, 5477.6797f, 79.640625f), Vector3(0, 0, 182)))
      LocalObject(2713, SpawnTube.Constructor(Vector3(3749.9062f, 5477.711f, 79.640625f), Vector3(0, 0, 182)))
      LocalObject(2714, SpawnTube.Constructor(Vector3(3757.1875f, 5477.5312f, 79.640625f), Vector3(0, 0, 182)))
      LocalObject(2934, Door.Constructor) //spawn tube door
      LocalObject(2937, Door.Constructor) //spawn tube door
      LocalObject(2938, Door.Constructor) //spawn tube door

      LocalObject(3070, Terminal.Constructor(vehicle_terminal_combined))
      LocalObject(1886, VehicleSpawnPad.Constructor(Vector3(3675.0f, 5458.0f, 89.0f), Vector3(0f, 0f, 0f))) //TODO guid not correct

      LocalObject(169, ProximityTerminal.Constructor(adv_med_terminal))
      LocalObject(1906, ProximityTerminal.Constructor(medical_terminal))
      
      ObjectToBuilding(169, 10)
      ObjectToBuilding(1906, 10)
      ObjectToBuilding(464, 10)
      ObjectToBuilding(470, 10)
      ObjectToBuilding(471, 10)
      ObjectToBuilding(472, 10)
      ObjectToBuilding(473, 10)
      ObjectToBuilding(474, 10)
      ObjectToBuilding(475, 10)
      ObjectToBuilding(476, 10)
      ObjectToBuilding(481, 10)
      ObjectToBuilding(482, 10)
      ObjectToBuilding(483, 10)
      ObjectToBuilding(484, 10)
      ObjectToBuilding(763, 10)
      ObjectToBuilding(764, 10)
      ObjectToBuilding(765, 10)
      ObjectToBuilding(766, 10)
      ObjectToBuilding(767, 10)
      ObjectToBuilding(768, 10)
      ObjectToBuilding(769, 10)
      ObjectToBuilding(770, 10)
      ObjectToBuilding(771, 10)
      ObjectToBuilding(772, 10)
      ObjectToBuilding(773, 10)
      ObjectToBuilding(774, 10)
      ObjectToBuilding(775, 10)
      ObjectToBuilding(776, 10)
      ObjectToBuilding(777, 10)
      ObjectToBuilding(778, 10)
      ObjectToBuilding(779, 10)
      ObjectToBuilding(780, 10)
      ObjectToBuilding(781, 10)
      ObjectToBuilding(782, 10)
      ObjectToBuilding(783, 10)
      ObjectToBuilding(784, 10)
      ObjectToBuilding(785, 10)
      ObjectToBuilding(923, 10)
      ObjectToBuilding(932, 10)
      ObjectToBuilding(933, 10)

      ObjectToBuilding(971, 10)
      ObjectToBuilding(1105, 10)
      ObjectToBuilding(1106, 10)
      ObjectToBuilding(1108, 10)
      ObjectToBuilding(1113, 10)
      ObjectToBuilding(1114, 10)
      ObjectToBuilding(1115, 10)
      ObjectToBuilding(1116, 10)

      ObjectToBuilding(1461, 10)

      ObjectToBuilding(238, 10)
      ObjectToBuilding(239, 10)
      ObjectToBuilding(240, 10)
      ObjectToBuilding(241, 10)
      ObjectToBuilding(242, 10)
      ObjectToBuilding(243, 10)
      ObjectToBuilding(244, 10)
      ObjectToBuilding(245, 10)

      ObjectToBuilding(948, 10)
      ObjectToBuilding(949, 10)

      ObjectToBuilding(10000, 10)
      ObjectToBuilding(10001, 10)

      ObjectToBuilding(1991, 10)
      ObjectToBuilding(1992, 10)
      ObjectToBuilding(1993, 10)
      ObjectToBuilding(1994, 10)
      ObjectToBuilding(2710, 10)
      ObjectToBuilding(2713, 10)
      ObjectToBuilding(2714, 10)
      ObjectToBuilding(2934, 10)
      ObjectToBuilding(2937, 10)
      ObjectToBuilding(2938, 10)

      ObjectToBuilding(3070, 10)
      ObjectToBuilding(1886, 10)

      DoorToLock(470, 1105)
      DoorToLock(475, 1108)
      DoorToLock(481, 1113)
      DoorToLock(771, 1106)
      DoorToLock(779, 1114)
      DoorToLock(784, 1116)
      DoorToLock(785, 1115)
      DoorToLock(923, 971)

      TerminalToInterface(948, 10000)
      TerminalToInterface(949, 10001)

      TerminalToSpawnPad(3070, 1886)


    }

    def Building11() : Unit = { // Irkalla
      LocalBuilding(11, FoundationBuilder(Building.Structure(StructureType.Facility, Vector3(4812f, 5212f, 0)))) // Todo change pos
      LocalObject(562, Door.Constructor)
      LocalObject(563, Door.Constructor)
      LocalObject(566, Door.Constructor)
      LocalObject(567, Door.Constructor)
      LocalObject(572, Door.Constructor(Vector3(4824.0703f, 5260.3516f, 78.703125f), Vector3(0, 0, 47)))
      LocalObject(573, Door.Constructor(Vector3(4833.711f, 5231.8203f, 71.25f), Vector3(0, 0, 47)))
      LocalObject(574, Door.Constructor(Vector3(4834.7188f, 5271.9766f, 71.25f), Vector3(0, 0, 317)))
      LocalObject(575, Door.Constructor)
      LocalObject(576, Door.Constructor(Vector3(4840.7734f, 5265.5f, 71.25f), Vector3(0, 0, 137)))
      LocalObject(577, Door.Constructor)
      LocalObject(578, Door.Constructor(Vector3(4854.5625f, 5174.6562f, 66.25f), Vector3(0, 0, 227)))
      LocalObject(579, Door.Constructor)
      LocalObject(580, Door.Constructor)
      LocalObject(585, Door.Constructor)
      LocalObject(852, Door.Constructor(Vector3(4793.0547f, 5204.914f, 48.734375f), Vector3(0, 0, 47)))
      LocalObject(853, Door.Constructor(Vector3(4799.3906f, 5216.047f, 56.234375f), Vector3(0, 0, 317)))
      LocalObject(854, Door.Constructor)
      LocalObject(855, Door.Constructor(Vector3(4815.5f, 5198.75f, 56.234375f), Vector3(0, 0, 137)))
      LocalObject(856, Door.Constructor)
      LocalObject(857, Door.Constructor)
      LocalObject(858, Door.Constructor)
      LocalObject(859, Door.Constructor)
      LocalObject(860, Door.Constructor)
      LocalObject(861, Door.Constructor)
      LocalObject(862, Door.Constructor)
      LocalObject(863, Door.Constructor)
      LocalObject(864, Door.Constructor)
      LocalObject(865, Door.Constructor(Vector3(4837.3047f, 5169.672f, 48.75f), Vector3(0, 0, 227)))
      LocalObject(866, Door.Constructor(Vector3(4842.9844f, 5174.8906f, 48.75f), Vector3(0, 0, 47)))
      LocalObject(867, Door.Constructor)
      LocalObject(868, Door.Constructor)
      LocalObject(869, Door.Constructor)
      LocalObject(870, Door.Constructor)
      LocalObject(871, Door.Constructor)
      LocalObject(872, Door.Constructor(Vector3(4857.0547f, 5247.992f, 48.734375f), Vector3(0, 0, 317)))
      LocalObject(873, Door.Constructor)
      LocalObject(874, Door.Constructor)
      LocalObject(875, Door.Constructor)
      LocalObject(927, Door.Constructor(Vector3(4810.4297f, 5243.0703f, 66.296875f), Vector3(0, 0, 227)))

      LocalObject(975, IFFLock.Constructor)
      LocalObject(1182, IFFLock.Constructor)
      LocalObject(1183, IFFLock.Constructor)
      LocalObject(1184, IFFLock.Constructor)
      LocalObject(1187, IFFLock.Constructor)
      LocalObject(1190, IFFLock.Constructor)
      LocalObject(1191, IFFLock.Constructor)
      LocalObject(1192, IFFLock.Constructor)
      LocalObject(1193, IFFLock.Constructor)
      LocalObject(1194, IFFLock.Constructor)
      LocalObject(1195, IFFLock.Constructor)
      LocalObject(1196, IFFLock.Constructor)

      LocalObject(1597, Locker.Constructor)

      LocalObject(2037, Terminal.Constructor(order_terminal))
      LocalObject(2038, Terminal.Constructor(order_terminal))
      LocalObject(2039, Terminal.Constructor(order_terminal))
      LocalObject(2040, Terminal.Constructor(order_terminal))
      LocalObject(2041, Terminal.Constructor(order_terminal))
      LocalObject(2042, Terminal.Constructor(order_terminal))
      LocalObject(2043, Terminal.Constructor(order_terminal))
      LocalObject(2740, SpawnTube.Constructor(Vector3(4808.0234f, 5217.9375f, 57f), Vector3(0, 0, 227)))
      LocalObject(2741, SpawnTube.Constructor(Vector3(4812.992f, 5212.6016f, 57f), Vector3(0, 0, 227)))
      LocalObject(2742, SpawnTube.Constructor(Vector3(4818.047f, 5207.3125f, 57f), Vector3(0, 0, 227)))
      LocalObject(2964, Door.Constructor) //spawn tube door
      LocalObject(2965, Door.Constructor) //spawn tube door
      LocalObject(2966, Door.Constructor) //spawn tube door

      LocalObject(3074, Terminal.Constructor(vehicle_terminal_combined))
      LocalObject(504, VehicleSpawnPad.Constructor(Vector3(4834.0f, 5185.0f, 67.0f), Vector3(0f, 0f, 45.0f))) //TODO guid not correct

      ObjectToBuilding(1912, 11)
      ObjectToBuilding(1913, 11)
      
      ObjectToBuilding(562, 11)
      ObjectToBuilding(563, 11)
      ObjectToBuilding(566, 11)
      ObjectToBuilding(567, 11)
      ObjectToBuilding(572, 11)
      ObjectToBuilding(573, 11)
      ObjectToBuilding(574, 11)
      ObjectToBuilding(575, 11)
      ObjectToBuilding(576, 11)
      ObjectToBuilding(577, 11)
      ObjectToBuilding(578, 11)
      ObjectToBuilding(579, 11)
      ObjectToBuilding(580, 11)
      ObjectToBuilding(585, 11)
      ObjectToBuilding(852, 11)
      ObjectToBuilding(853, 11)
      ObjectToBuilding(854, 11)
      ObjectToBuilding(855, 11)
      ObjectToBuilding(856, 11)
      ObjectToBuilding(857, 11)
      ObjectToBuilding(858, 11)
      ObjectToBuilding(859, 11)
      ObjectToBuilding(860, 11)
      ObjectToBuilding(861, 11)
      ObjectToBuilding(862, 11)
      ObjectToBuilding(863, 11)
      ObjectToBuilding(864, 11)
      ObjectToBuilding(865, 11)
      ObjectToBuilding(866, 11)
      ObjectToBuilding(867, 11)
      ObjectToBuilding(868, 11)
      ObjectToBuilding(869, 11)
      ObjectToBuilding(870, 11)
      ObjectToBuilding(871, 11)
      ObjectToBuilding(872, 11)
      ObjectToBuilding(873, 11)
      ObjectToBuilding(874, 11)
      ObjectToBuilding(875, 11)
      ObjectToBuilding(927, 11)

      ObjectToBuilding(975, 11)
      ObjectToBuilding(1182, 11)
      ObjectToBuilding(1183, 11)
      ObjectToBuilding(1184, 11)
      ObjectToBuilding(1187, 11)
      ObjectToBuilding(1190, 11)
      ObjectToBuilding(1191, 11)
      ObjectToBuilding(1192, 11)
      ObjectToBuilding(1193, 11)
      ObjectToBuilding(1194, 11)
      ObjectToBuilding(1195, 11)
      ObjectToBuilding(1196, 11)

      ObjectToBuilding(1597, 11)

      ObjectToBuilding(2037, 11)
      ObjectToBuilding(2038, 11)
      ObjectToBuilding(2039, 11)
      ObjectToBuilding(2040, 11)
      ObjectToBuilding(2041, 11)
      ObjectToBuilding(2042, 11)
      ObjectToBuilding(2043, 11)
      ObjectToBuilding(2740, 11)
      ObjectToBuilding(2741, 11)
      ObjectToBuilding(2742, 11)
      ObjectToBuilding(2964, 11)
      ObjectToBuilding(2965, 11)
      ObjectToBuilding(2966, 11)

      ObjectToBuilding(3074, 11)
      ObjectToBuilding(504, 11)

      DoorToLock(572, 1187)
      DoorToLock(573, 1191)
      DoorToLock(574, 1190)
      DoorToLock(576, 1194)
      DoorToLock(578, 1195)
      DoorToLock(852, 1182)
      DoorToLock(853, 1183)
      DoorToLock(855, 1184)
      DoorToLock(865, 1192)
      DoorToLock(866, 1193)
      DoorToLock(872, 1196)
      DoorToLock(927, 975)

      TerminalToSpawnPad(3074, 504)
    }
    def Building24() : Unit = { // Akkan Dropship Center, Ishundar (ID: 24)
      LocalBuilding(24, FoundationBuilder(Building.Structure(StructureType.Facility, Vector3(2698.6406f, 4336.914f, 52.046875f)))) //todo ? change ?
      
      //Akkan IDs for courtyard/lobby/dish/bunkers, no access to tower or basement with exception of stairs and backdoor areas only, can't proceed past it into basement)
      
      //Akkan Doors
      LocalObject(281, Door.Constructor)
      LocalObject(378, Door.Constructor)
      LocalObject(379, Door.Constructor)
      LocalObject(380, Door.Constructor)
      LocalObject(381, Door.Constructor)
      LocalObject(382, Door.Constructor)
      LocalObject(383, Door.Constructor(Vector3(2674.0938f, 4317.367f, 44.59375f), Vector3(0.0f, 0.0f, 180.0f)))
      LocalObject(384, Door.Constructor(Vector3(2674.086f, 4321.6953f, 44.59375f), Vector3(0.0f, 357.1875f, 357.1875f)))
      LocalObject(385, Door.Constructor)
      LocalObject(386, Door.Constructor(Vector3(2692.086f, 4316.5703f, 52.03125f), Vector3(0.0f, 2.8125f, 270.0f)))
      LocalObject(387, Door.Constructor)
      LocalObject(388, Door.Constructor(Vector3(2704.7969f, 4344.0625f, 44.59375f), Vector3(0.0f, 354.375f, 272.8125f)))
      LocalObject(393, Door.Constructor)
      LocalObject(394, Door.Constructor(Vector3(2748.039f, 4426.25f, 44.609375f), Vector3(0.0f, 0.0f, 0.0f)))
      LocalObject(395, Door.Constructor(Vector3(2752.5234f, 4398.297f, 39.609375f), Vector3(0.0f, 2.8125f, 272.8125f)))
      LocalObject(396, Door.Constructor)
      LocalObject(397, Door.Constructor)
      LocalObject(398, Door.Constructor)
      LocalObject(399, Door.Constructor)
      LocalObject(400, Door.Constructor)
      LocalObject(401, Door.Constructor(Vector3(2809.9844f, 4401.164f, 39.3125f), Vector3(0.0f, 8.4375f, 0.0f)))
      LocalObject(402, Door.Constructor)
      LocalObject(403, Door.Constructor)
      LocalObject(701, Door.Constructor)
      LocalObject(703, Door.Constructor)
      LocalObject(716, Door.Constructor)
      LocalObject(920, Door.Constructor(Vector3(2708.1953f, 4319.9766f, 39.59375f), Vector3(0.0f, 0.0f, 92.8125f)))
      
      //Akkan Locks
      LocalObject(968, IFFLock.Constructor)
      LocalObject(1036, IFFLock.Constructor)
      LocalObject(1037, IFFLock.Constructor)
      LocalObject(1038, IFFLock.Constructor)
      LocalObject(1039, IFFLock.Constructor)
      LocalObject(1047, IFFLock.Constructor)
      LocalObject(1049, IFFLock.Constructor)
      LocalObject(1053, IFFLock.Constructor)
      
      //Akkan DoorToLock
      DoorToLock(383, 1037)
      DoorToLock(384, 1036)
      DoorToLock(386, 1038)
      DoorToLock(388, 1039)
      DoorToLock(394, 1047)
      DoorToLock(395, 1049)
      DoorToLock(401, 1053)
      DoorToLock(920, 968)

      //Akkan Order Terminals
      LocalObject(1949, Terminal.Constructor(order_terminal))
      LocalObject(1950, Terminal.Constructor(order_terminal))
      LocalObject(1951, Terminal.Constructor(order_terminal))
      LocalObject(1952, Terminal.Constructor(order_terminal))
      LocalObject(1953, Terminal.Constructor(order_terminal))
      
      //Akkan Vehicle Terminals
      LocalObject(3067, Terminal.Constructor(ground_vehicle_terminal))
      LocalObject(1881,
        VehicleSpawnPad.Constructor(Vector3(2711.3438f, 4418.4062f, 40.609375f), Vector3(0.0f, 351.5625f, 182.8125f))
      )
      LocalObject(283, Terminal.Constructor(dropship_vehicle_terminal))
      LocalObject(282,
        VehicleSpawnPad.Constructor(Vector3(2762.414f, 4448.828f, 44.75f), Vector3(0.0f, 2.8125f, 0.0f))
      )
    
      //Akkan Medical Terminals
      LocalObject(1900, ProximityTerminal.Constructor(medical_terminal))
      LocalObject(1901, ProximityTerminal.Constructor(medical_terminal))
      
      //Akkan ObjectToBuilding
      ObjectToBuilding(281, 24)
      ObjectToBuilding(282, 24)
      ObjectToBuilding(283, 24)
      ObjectToBuilding(378, 24)
      ObjectToBuilding(379, 24)
      ObjectToBuilding(380, 24)
      ObjectToBuilding(381, 24)
      ObjectToBuilding(382, 24)
      ObjectToBuilding(383, 24)
      ObjectToBuilding(384, 24)
      ObjectToBuilding(385, 24)
      ObjectToBuilding(386, 24)
      ObjectToBuilding(387, 24)
      ObjectToBuilding(388, 24)
      ObjectToBuilding(393, 24)
      ObjectToBuilding(394, 24)
      ObjectToBuilding(395, 24)
      ObjectToBuilding(396, 24)
      ObjectToBuilding(397, 24)
      ObjectToBuilding(398, 24)
      ObjectToBuilding(399, 24)
      ObjectToBuilding(400, 24)
      ObjectToBuilding(401, 24)
      ObjectToBuilding(402, 24)
      ObjectToBuilding(403, 24)
      ObjectToBuilding(701, 24)
      ObjectToBuilding(703, 24)
      ObjectToBuilding(716, 24)
      ObjectToBuilding(920, 24)
      ObjectToBuilding(968, 24)
      ObjectToBuilding(1036, 24)
      ObjectToBuilding(1037, 24)
      ObjectToBuilding(1038, 24)
      ObjectToBuilding(1039, 24)
      ObjectToBuilding(1047, 24)
      ObjectToBuilding(1049, 24)
      ObjectToBuilding(1053, 24)
      ObjectToBuilding(1881, 24)
      ObjectToBuilding(1900, 24)
      ObjectToBuilding(1901, 24)
      ObjectToBuilding(1949, 24)
      ObjectToBuilding(1950, 24)
      ObjectToBuilding(1951, 24)
      ObjectToBuilding(1952, 24)
      ObjectToBuilding(1953, 24)
      ObjectToBuilding(3067, 24)
      
    }
      
    def Building25() : Unit = { // Gate Outpost Watch Tower (North of Forseral Warpgate), Ishundar (ID: 74)
      LocalBuilding(25, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(5404f, 4222f, 0)))) // TODO loc
      LocalObject(2973, Door.Constructor)
      LocalObject(2974, Door.Constructor)
      LocalObject(590, Door.Constructor(Vector3(5405.992f, 4220.1797f, 39.1875f), Vector3(0, 0, 180)))
      LocalObject(591, Door.Constructor(Vector3(5405.992f, 4220.1797f, 49.1875f), Vector3(0, 0, 180)))
      LocalObject(592, Door.Constructor(Vector3(5405.992f, 4220.1797f, 69.25f), Vector3(0, 0, 180)))
      LocalObject(595, Door.Constructor(Vector3(5405.9844f, 4235.8047f, 69.25f), Vector3(0, 0, 0)))
      LocalObject(593, Door.Constructor(Vector3(5405.9844f, 4235.8047f, 39.1875f), Vector3(0, 0, 0)))
      LocalObject(594, Door.Constructor(Vector3(5405.9844f, 4235.8047f, 49.1875f), Vector3(0, 0, 0)))
      LocalObject(1208, IFFLock.Constructor)
      LocalObject(1209, IFFLock.Constructor)
      LocalObject(1210, IFFLock.Constructor)
      LocalObject(1207, IFFLock.Constructor)
      LocalObject(1205, IFFLock.Constructor)
      LocalObject(1206, IFFLock.Constructor)
      LocalObject(2053, Terminal.Constructor(order_terminal))
      LocalObject(2054, Terminal.Constructor(order_terminal))
      LocalObject(2055, Terminal.Constructor(order_terminal))
      LocalObject(2749, SpawnTube.Constructor(respawn_tube_tower, Vector3(5404.8125f, 4215.7344f, 29.484375f), Vector3(0, 0, 90)))
      LocalObject(2750, SpawnTube.Constructor(respawn_tube_tower, Vector3(5404.7656f, 4232.1562f, 29.484375f), Vector3(0, 0, 90)))

      ObjectToBuilding(2973, 25)
      ObjectToBuilding(2974, 25)
      ObjectToBuilding(590, 25)
      ObjectToBuilding(591, 25)
      ObjectToBuilding(592, 25)
      ObjectToBuilding(595, 25)
      ObjectToBuilding(593, 25)
      ObjectToBuilding(594, 25)
      ObjectToBuilding(1208, 25)
      ObjectToBuilding(1209, 25)
      ObjectToBuilding(1210, 25)
      ObjectToBuilding(1207, 25)
      ObjectToBuilding(1205, 25)
      ObjectToBuilding(1206, 25)
      ObjectToBuilding(2053, 25)
      ObjectToBuilding(2054, 25)
      ObjectToBuilding(2055, 25)
      ObjectToBuilding(2749, 25)
      ObjectToBuilding(2750, 25)

      DoorToLock(590, 1208)
      DoorToLock(591, 1209)
      DoorToLock(592, 1210)
      DoorToLock(595, 1207)
      DoorToLock(593, 1205)
      DoorToLock(594, 1206)

    }
    def Building33() : Unit = { // East Girru Gun Tower, Ishundar (ID: 62)
      LocalBuilding(33, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4624f, 5915f, 0)))) // TODO loc
      LocalObject(2957, Door.Constructor)
      LocalObject(2958, Door.Constructor)
      LocalObject(542, Door.Constructor(Vector3(4625.9844f, 5910.211f, 55.75f), Vector3(0, 0, 180)))
      LocalObject(543, Door.Constructor(Vector3(4625.9844f, 5910.211f, 75.75f), Vector3(0, 0, 180)))
      LocalObject(544, Door.Constructor(Vector3(4626.0312f, 5925.836f, 55.75f), Vector3(0, 0, 0)))
      LocalObject(545, Door.Constructor(Vector3(4626.0312f, 5925.836f, 75.75f), Vector3(0, 0, 0)))
      LocalObject(1164, IFFLock.Constructor)
      LocalObject(1165, IFFLock.Constructor)
      LocalObject(1166, IFFLock.Constructor)
      LocalObject(1167, IFFLock.Constructor)
      LocalObject(2027, Terminal.Constructor(order_terminal))
      LocalObject(2028, Terminal.Constructor(order_terminal))
      LocalObject(2029, Terminal.Constructor(order_terminal))
      LocalObject(2733, SpawnTube.Constructor(respawn_tube_tower, Vector3(4624.758f, 5905.7344f, 45.984375f), Vector3(0, 0, 90)))
      LocalObject(2734, SpawnTube.Constructor(respawn_tube_tower, Vector3(4624.7266f, 5922.1484f, 45.984375f), Vector3(0, 0, 90)))

      ObjectToBuilding(2957, 33)
      ObjectToBuilding(2958, 33)
      ObjectToBuilding(542, 33)
      ObjectToBuilding(543, 33)
      ObjectToBuilding(544, 33)
      ObjectToBuilding(545, 33)
      ObjectToBuilding(1164, 33)
      ObjectToBuilding(1165, 33)
      ObjectToBuilding(1166, 33)
      ObjectToBuilding(1167, 33)
      ObjectToBuilding(2027, 33)
      ObjectToBuilding(2028, 33)
      ObjectToBuilding(2029, 33)
      ObjectToBuilding(2733, 33)
      ObjectToBuilding(2734, 33)

      DoorToLock(542, 1166)
      DoorToLock(543, 1167)
      DoorToLock(544, 1164)
      DoorToLock(545, 1165)

    }
    def Building34() : Unit = { // SE Hanish Gun Tower (ID: 60)
      LocalBuilding(34, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4422f, 4852f, 0)))) // TODO loc
      LocalObject(2951, Door.Constructor)
      LocalObject(2952, Door.Constructor)
      LocalObject(525, Door.Constructor(Vector3(4423.9766f, 4850.164f, 86.203125f), Vector3(0, 0, 180)))
      LocalObject(526, Door.Constructor(Vector3(4423.9766f, 4850.164f, 106.140625f), Vector3(0, 0, 180)))
      LocalObject(527, Door.Constructor(Vector3(4423.9688f, 4865.8594f, 86.203125f), Vector3(0, 0, 0)))
      LocalObject(528, Door.Constructor(Vector3(4423.9688f, 4865.8594f, 106.140625f), Vector3(0, 0, 0)))
      LocalObject(1155, IFFLock.Constructor)
      LocalObject(1156, IFFLock.Constructor)
      LocalObject(1150, IFFLock.Constructor)
      LocalObject(1151, IFFLock.Constructor)
      LocalObject(2018, Terminal.Constructor(order_terminal))
      LocalObject(2019, Terminal.Constructor(order_terminal))
      LocalObject(2020, Terminal.Constructor(order_terminal))
      LocalObject(2727, SpawnTube.Constructor(respawn_tube_tower, Vector3(4422.8203f, 4845.711f, 76.4375f), Vector3(0, 0, 90)))
      LocalObject(2728, SpawnTube.Constructor(respawn_tube_tower, Vector3(4422.7344f, 4862.1406f, 76.4375f), Vector3(0, 0, 90)))

      ObjectToBuilding(2951, 34)
      ObjectToBuilding(2952, 34)
      ObjectToBuilding(525, 34)
      ObjectToBuilding(526, 34)
      ObjectToBuilding(527, 34)
      ObjectToBuilding(528, 34)
      ObjectToBuilding(1155, 34)
      ObjectToBuilding(1156, 34)
      ObjectToBuilding(1150, 34)
      ObjectToBuilding(1151, 34)
      ObjectToBuilding(2018, 34)
      ObjectToBuilding(2019, 34)
      ObjectToBuilding(2020, 34)
      ObjectToBuilding(2727, 34)
      ObjectToBuilding(2728, 34)

      DoorToLock(525, 1155)
      DoorToLock(526, 1156)
      DoorToLock(527, 1150)
      DoorToLock(528, 1151)
    }
    def Building35() : Unit = { // NE Akkan Watch Tower, Ishundar (ID: 69)
      LocalBuilding(35, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3096f, 5037f, 0)))) // TODO loc
      LocalObject(2917, Door.Constructor)
      LocalObject(2918, Door.Constructor)
      LocalObject(414, Door.Constructor(Vector3(3098.0f, 5032.1484f, 81.9375f), Vector3(0, 0, 180)))
      LocalObject(415, Door.Constructor(Vector3(3098.0f, 5032.1484f, 91.875f), Vector3(0, 0, 180)))
      LocalObject(416, Door.Constructor(Vector3(3098.0f, 5032.1484f, 111.875f), Vector3(0, 0, 180)))
      LocalObject(417, Door.Constructor(Vector3(3097.9922f, 5047.875f, 81.953125f), Vector3(0, 0, 0)))
      LocalObject(418, Door.Constructor(Vector3(3097.9922f, 5047.875f, 91.875f), Vector3(0, 0, 0)))
      LocalObject(419, Door.Constructor(Vector3(3097.9922f, 5047.875f, 111.875f), Vector3(0, 0, 0)))
      LocalObject(1062, IFFLock.Constructor)
      LocalObject(1063, IFFLock.Constructor)
      LocalObject(1064, IFFLock.Constructor)
      LocalObject(1066, IFFLock.Constructor)
      LocalObject(1067, IFFLock.Constructor)
      LocalObject(1068, IFFLock.Constructor)
      LocalObject(1966, Terminal.Constructor(order_terminal))
      LocalObject(1967, Terminal.Constructor(order_terminal))
      LocalObject(1968, Terminal.Constructor(order_terminal))
      LocalObject(2693, SpawnTube.Constructor(respawn_tube_tower, Vector3(3096.6562f, 5027.742f, 72.1875f), Vector3(0, 0, 90)))
      LocalObject(2694, SpawnTube.Constructor(respawn_tube_tower, Vector3(3096.7812f, 5044.1562f, 72.1875f), Vector3(0, 0, 90)))

      ObjectToBuilding(2917, 35)
      ObjectToBuilding(2918, 35)
      ObjectToBuilding(414, 35)
      ObjectToBuilding(415, 35)
      ObjectToBuilding(416, 35)
      ObjectToBuilding(417, 35)
      ObjectToBuilding(418, 35)
      ObjectToBuilding(419, 35)
      ObjectToBuilding(1062, 35)
      ObjectToBuilding(1063, 35)
      ObjectToBuilding(1064, 35)
      ObjectToBuilding(1066, 35)
      ObjectToBuilding(1067, 35)
      ObjectToBuilding(1068, 35)
      ObjectToBuilding(1966, 35)
      ObjectToBuilding(1967, 35)
      ObjectToBuilding(1968, 35)
      ObjectToBuilding(2693, 35)
      ObjectToBuilding(2694, 35)

      DoorToLock(414, 1066)
      DoorToLock(415, 1067)
      DoorToLock(416, 1068)
      DoorToLock(417, 1062)
      DoorToLock(418, 1063)
      DoorToLock(419, 1064)
    }
    def Building36() : Unit = { // West Girru Air Tower, Ishundar (ID: 83)
      LocalBuilding(36, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3748f, 6042f, 0)))) // TODO loc
      LocalObject(2935, Door.Constructor)
      LocalObject(2936, Door.Constructor)
      LocalObject(477, Door.Constructor(Vector3(3750.0f, 6040.164f, 56.203125f), Vector3(0, 0, 180)))
      LocalObject(478, Door.Constructor(Vector3(3750.0f, 6040.164f, 76.15625f), Vector3(0, 0, 180)))
      LocalObject(479, Door.Constructor(Vector3(3750.0078f, 6055.789f, 56.203125f), Vector3(0, 0, 0)))
      LocalObject(480, Door.Constructor(Vector3(3750.0078f, 6055.789f, 76.15625f), Vector3(0, 0, 0)))
      LocalObject(1109, IFFLock.Constructor)
      LocalObject(1110, IFFLock.Constructor)
      LocalObject(1111, IFFLock.Constructor)
      LocalObject(1112, IFFLock.Constructor)
      LocalObject(1995, Terminal.Constructor(order_terminal))
      LocalObject(1996, Terminal.Constructor(order_terminal))
      LocalObject(1997, Terminal.Constructor(order_terminal))
      LocalObject(2711, SpawnTube.Constructor(respawn_tube_tower, Vector3(3748.7266f, 6035.7344f, 46.453125f), Vector3(0, 0, 90)))
      LocalObject(2712, SpawnTube.Constructor(respawn_tube_tower, Vector3(3748.6328f, 6052.125f, 46.453125f), Vector3(0, 0, 90)))

      ObjectToBuilding(2935, 36)
      ObjectToBuilding(2936, 36)
      ObjectToBuilding(477, 36)
      ObjectToBuilding(478, 36)
      ObjectToBuilding(479, 36)
      ObjectToBuilding(480, 36)
      ObjectToBuilding(1109, 36)
      ObjectToBuilding(1110, 36)
      ObjectToBuilding(1111, 36)
      ObjectToBuilding(1112, 36)
      ObjectToBuilding(1995, 36)
      ObjectToBuilding(1996, 36)
      ObjectToBuilding(1997, 36)
      ObjectToBuilding(2711, 36)
      ObjectToBuilding(2712, 36)

      DoorToLock(477, 1111)
      DoorToLock(478, 1112)
      DoorToLock(479, 1109)
      DoorToLock(480, 1110)
    }
    def Building55() : Unit = { // South Irkalla Air Tower, Ishundar (ID: 86)
      LocalBuilding(55, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4894f, 4935f, 0)))) // TODO loc
      LocalObject(2969, Door.Constructor)
      LocalObject(2970, Door.Constructor)
      LocalObject(581, Door.Constructor(Vector3(4896.0156f, 4932.125f, 67.75f), Vector3(0, 0, 180)))
      LocalObject(582, Door.Constructor(Vector3(4895.992f, 4932.1484f, 87.75f), Vector3(0, 0, 180)))
      LocalObject(583, Door.Constructor(Vector3(4896.0156f, 4947.8516f, 67.75f), Vector3(0, 0, 0)))
      LocalObject(584, Door.Constructor(Vector3(4896.008f, 4947.8438f, 87.75f), Vector3(0, 0, 0)))
      LocalObject(1199, IFFLock.Constructor)
      LocalObject(1200, IFFLock.Constructor)
      LocalObject(1197, IFFLock.Constructor)
      LocalObject(1198, IFFLock.Constructor)
      LocalObject(2047, Terminal.Constructor(order_terminal))
      LocalObject(2048, Terminal.Constructor(order_terminal))
      LocalObject(2049, Terminal.Constructor(order_terminal))
      LocalObject(2745, SpawnTube.Constructor(respawn_tube_tower, Vector3(4894.7734f, 4927.742f, 57.984375f), Vector3(0, 0, 90)))
      LocalObject(2746, SpawnTube.Constructor(respawn_tube_tower, Vector3(4894.7734f, 4944.117f, 57.984375f), Vector3(0, 0, 90)))

      ObjectToBuilding(2969, 55)
      ObjectToBuilding(2970, 55)
      ObjectToBuilding(581, 55)
      ObjectToBuilding(582, 55)
      ObjectToBuilding(583, 55)
      ObjectToBuilding(584, 55)
      ObjectToBuilding(1199, 55)
      ObjectToBuilding(1200, 55)
      ObjectToBuilding(1197, 55)
      ObjectToBuilding(1198, 55)
      ObjectToBuilding(2047, 55)
      ObjectToBuilding(2048, 55)
      ObjectToBuilding(2049, 55)
      ObjectToBuilding(2745, 55)
      ObjectToBuilding(2746, 55)

      DoorToLock(581, 1199)
      DoorToLock(582, 1200)
      DoorToLock(583, 1197)
      DoorToLock(584, 1198)
    }
    def Building56() : Unit = { //  SW Hanish Air Tower, Ishundar (ID: 82)
      LocalBuilding(56, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3590f, 5290f, 0)))) // TODO loc
      LocalObject(2932, Door.Constructor)
      LocalObject(2933, Door.Constructor)
      LocalObject(466, Door.Constructor(Vector3(3592.0f, 5286.1562f, 69.390625f), Vector3(0, 0, 180)))
      LocalObject(467, Door.Constructor(Vector3(3592.0f, 5286.1562f, 89.453125f), Vector3(0, 0, 180)))
      LocalObject(468, Door.Constructor(Vector3(3592.0f, 5301.797f, 69.390625f), Vector3(0, 0, 0)))
      LocalObject(469, Door.Constructor(Vector3(3592.0f, 5301.797f, 89.4375f), Vector3(0, 0, 0)))
      LocalObject(1101, IFFLock.Constructor)
      LocalObject(1102, IFFLock.Constructor)
      LocalObject(1103, IFFLock.Constructor)
      LocalObject(1104, IFFLock.Constructor)
      LocalObject(1988, Terminal.Constructor(order_terminal))
      LocalObject(1989, Terminal.Constructor(order_terminal))
      LocalObject(1990, Terminal.Constructor(order_terminal))
      LocalObject(2708, SpawnTube.Constructor(respawn_tube_tower, Vector3(3590.9062f, 5281.742f, 59.6875f), Vector3(0, 0, 90)))
      LocalObject(2709, SpawnTube.Constructor(respawn_tube_tower, Vector3(3590.836f, 5298.1484f, 59.6875f), Vector3(0, 0, 90)))

      ObjectToBuilding(2932, 56)
      ObjectToBuilding(2933, 56)
      ObjectToBuilding(466, 56)
      ObjectToBuilding(467, 56)
      ObjectToBuilding(468, 56)
      ObjectToBuilding(469, 56)
      ObjectToBuilding(1101, 56)
      ObjectToBuilding(1102, 56)
      ObjectToBuilding(1103, 56)
      ObjectToBuilding(1104, 56)
      ObjectToBuilding(1988, 56)
      ObjectToBuilding(1989, 56)
      ObjectToBuilding(1990, 56)
      ObjectToBuilding(2708, 56)
      ObjectToBuilding(2709, 56)

      DoorToLock(466, 1103)
      DoorToLock(467, 1104)
      DoorToLock(468, 1101)
      DoorToLock(469, 1102)
    }
    def Building59() : Unit = { // Gate Outpost Watch Tower (South of Cyssor Warpgate), Ishundar (ID: 73)
      LocalBuilding(59, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4668f, 6625f, 0)))) // TODO loc
      LocalObject(2959, Door.Constructor)
      LocalObject(2960, Door.Constructor)
      LocalObject(546, Door.Constructor(Vector3(4669.992f, 6620.1562f, 42.875f), Vector3(0, 0, 180)))
      LocalObject(547, Door.Constructor(Vector3(4669.992f, 6620.1562f, 52.8125f), Vector3(0, 0, 180)))
      LocalObject(548, Door.Constructor(Vector3(4669.992f, 6620.1562f, 72.8125f), Vector3(0, 0, 180)))
      LocalObject(549, Door.Constructor(Vector3(4669.9844f, 6635.8203f, 42.859375f), Vector3(0, 0, 0)))
      LocalObject(550, Door.Constructor(Vector3(4669.9844f, 6635.8203f, 52.8125f), Vector3(0, 0, 0)))
      LocalObject(551, Door.Constructor(Vector3(4669.9844f, 6635.8203f, 72.8125f), Vector3(0, 0, 0)))
      LocalObject(1168, IFFLock.Constructor)
      LocalObject(1169, IFFLock.Constructor)
      LocalObject(1170, IFFLock.Constructor)
      LocalObject(1171, IFFLock.Constructor)
      LocalObject(1172, IFFLock.Constructor)
      LocalObject(1173, IFFLock.Constructor)
      LocalObject(2030, Terminal.Constructor(order_terminal))
      LocalObject(2031, Terminal.Constructor(order_terminal))
      LocalObject(2032, Terminal.Constructor(order_terminal))
      LocalObject(2735, SpawnTube.Constructor(respawn_tube_tower, Vector3(4668.7656f, 6615.7344f, 33.109375f), Vector3(0, 0, 90)))
      LocalObject(2736, SpawnTube.Constructor(respawn_tube_tower, Vector3(4668.742f, 6632.1562f, 33.109375f), Vector3(0, 0, 90)))

      ObjectToBuilding(2959, 59)
      ObjectToBuilding(2960, 59)
      ObjectToBuilding(546, 59)
      ObjectToBuilding(547, 59)
      ObjectToBuilding(548, 59)
      ObjectToBuilding(549, 59)
      ObjectToBuilding(550, 59)
      ObjectToBuilding(551, 59)
      ObjectToBuilding(1168, 59)
      ObjectToBuilding(1169, 59)
      ObjectToBuilding(1170, 59)
      ObjectToBuilding(1171, 59)
      ObjectToBuilding(1172, 59)
      ObjectToBuilding(1173, 59)
      ObjectToBuilding(2030, 59)
      ObjectToBuilding(2031, 59)
      ObjectToBuilding(2032, 59)
      ObjectToBuilding(2735, 59)
      ObjectToBuilding(2736, 59)

      DoorToLock(546, 1171)
      DoorToLock(547, 1172)
      DoorToLock(548, 1173)
      DoorToLock(549, 1168)
      DoorToLock(550, 1169)
      DoorToLock(551, 1170)
    }
    def Building65() : Unit = { // West Hanish Gun Tower, Ishundar (ID: 56)
      LocalBuilding(65, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3012f, 5701f, 0)))) // TODO loc
      LocalObject(2914, Door.Constructor)
      LocalObject(2915, Door.Constructor)
      LocalObject(404, Door.Constructor(Vector3(3003.9688f, 5706.1484f, 56.3125f), Vector3(0, 0, 180)))
      LocalObject(405, Door.Constructor(Vector3(3003.9688f, 5706.1484f, 76.25f), Vector3(0, 0, 180)))
      LocalObject(406, Door.Constructor(Vector3(3004.0469f, 5721.875f, 56.328125f), Vector3(0, 0, 0)))
      LocalObject(407, Door.Constructor(Vector3(3004.0469f, 5721.875f, 76.25f), Vector3(0, 0, 0)))
      LocalObject(1054, IFFLock.Constructor)
      LocalObject(1055, IFFLock.Constructor)
      LocalObject(1056, IFFLock.Constructor)
      LocalObject(1057, IFFLock.Constructor)
      LocalObject(1960, Terminal.Constructor(order_terminal))
      LocalObject(1961, Terminal.Constructor(order_terminal))
      LocalObject(1962, Terminal.Constructor(order_terminal))
      LocalObject(2690, SpawnTube.Constructor(respawn_tube_tower, Vector3(3022.711f, 5701.758f, 46.5625f), Vector3(0, 0, 90)))
      LocalObject(2691, SpawnTube.Constructor(respawn_tube_tower, Vector3(3002.7188f, 5718.1562f, 46.5625f), Vector3(0, 0, 90)))

      ObjectToBuilding(2914, 65)
      ObjectToBuilding(2915, 65)
      ObjectToBuilding(404, 65)
      ObjectToBuilding(405, 65)
      ObjectToBuilding(406, 65)
      ObjectToBuilding(407, 65)
      ObjectToBuilding(1054, 65)
      ObjectToBuilding(1055, 65)
      ObjectToBuilding(1056, 65)
      ObjectToBuilding(1057, 65)
      ObjectToBuilding(1960, 65)
      ObjectToBuilding(1961, 65)
      ObjectToBuilding(1962, 65)
      ObjectToBuilding(2690, 65)
      ObjectToBuilding(2691, 65)

      DoorToLock(404, 1056)
      DoorToLock(405, 1057)
      DoorToLock(406, 1054)
      DoorToLock(407, 1055)
    }



//    // BFR test
////    LocalObject(199, Terminal.Constructor(vehicle_terminal_combined)))
////    LocalObject(505,
////      VehicleSpawnPad.Constructor(Vector3(3707.0f, 5522.0f, 89.0f), Vector3(0f, 0f, 0f))
////    )) //TODO guid not correct
////    ObjectToBuilding(199, 30)
////    ObjectToBuilding(505, 30)
////    TerminalToSpawnPad(199, 505)
//
//    // For Nick's tests
//    LocalObject(320, Door.Constructor))
//    LocalObject(324, Door.Constructor))
//    LocalObject(672, Door.Constructor))
//    LocalObject(318, Door.Constructor))
//    LocalObject(669, Door.Constructor))
//
//
//
//
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//    // Dagon
//    LocalBuilding(27, FoundationBuilder(Building.Structure))
//
//    // Baal
//    LocalBuilding(42, FoundationBuilder(Building.Structure))
//
//    // For Nick's tests
//    LocalBuilding(4, FoundationBuilder(Building.Structure))
//    ObjectToBuilding(320, 4)
//    ObjectToBuilding(324, 4)
//    ObjectToBuilding(672, 4)
//    ObjectToBuilding(318, 4)
//    ObjectToBuilding(669, 4)

  }

  val map5 = new ZoneMap("map05")

  val map6 = new ZoneMap("map06") {
    Building2()
    Building38()
    Building42()
    Building48()
    Building49()

    def Building2() : Unit = {
      //Anguta
      LocalBuilding(2, FoundationBuilder(Building.Structure(StructureType.Facility, Vector3(3974.2344f, 4287.914f, 0))))
      LocalObject(222, Door.Constructor) //air term building, bay door
      LocalObject(370, Door.Constructor) //courtyard
      LocalObject(371, Door.Constructor) //courtyard
      LocalObject(372, Door.Constructor) //courtyard
      LocalObject(373, Door.Constructor) //courtyard
      LocalObject(375, Door.Constructor(Vector3(3924.0f, 4231.2656f, 271.82812f), Vector3(0, 0, 180))) //2nd level door, south
      LocalObject(376, Door.Constructor(Vector3(3924.0f, 4240.2656f, 271.82812f), Vector3(0, 0, 0))) //2nd level door, north
      LocalObject(383, Door.Constructor) //courtyard
      LocalObject(384, Door.Constructor(Vector3(3939.6328f, 4232.547f, 279.26562f), Vector3(0, 0, 270))) //3rd floor door
      LocalObject(385, Door.Constructor) //courtyard
      LocalObject(387, Door.Constructor(Vector3(3951.9531f, 4260.008f, 271.82812f), Vector3(0, 0, 270))) //2nd level door, stairwell
      LocalObject(391, Door.Constructor) //courtyard
      LocalObject(393, Door.Constructor(Vector3(3997.8984f, 4344.3203f, 271.8125f), Vector3(0, 0, 0))) //air term building, upstairs door
      LocalObject(394, Door.Constructor(Vector3(3999.9766f, 4314.3203f, 266.82812f), Vector3(0, 0, 270))) //air term building, f.door
      LocalObject(396, Door.Constructor) //courtyard
      LocalObject(398, Door.Constructor) //courtyard
      LocalObject(399, Door.Constructor) //courtyard
      LocalObject(402, Door.Constructor) //courtyard
      LocalObject(403, Door.Constructor) //courtyard
      LocalObject(404, Door.Constructor(Vector3(4060.0078f, 4319.9766f, 266.8125f), Vector3(0, 0, 0))) //b.door
      LocalObject(603, Door.Constructor)
      LocalObject(604, Door.Constructor)
      LocalObject(605, Door.Constructor)
      LocalObject(606, Door.Constructor)
      LocalObject(607, Door.Constructor)
      LocalObject(610, Door.Constructor)
      LocalObject(611, Door.Constructor)
      LocalObject(614, Door.Constructor)
      LocalObject(619, Door.Constructor)
      LocalObject(620, Door.Constructor(Vector3(3983.9531f, 4299.992f, 249.29688f), Vector3(0, 0, 90))) //generator room door
      LocalObject(621, Door.Constructor)
      LocalObject(622, Door.Constructor(Vector3(3988.0078f, 4248.0156f, 256.82812f), Vector3(0, 0, 180))) //spawn room door
      LocalObject(623, Door.Constructor(Vector3(3988.0078f, 4271.9766f, 256.79688f), Vector3(0, 0, 0))) //spawn room door
      LocalObject(630, Door.Constructor(Vector3(4000.0078f, 4252.0f, 249.29688f), Vector3(0, 0, 270))) //spawn room door
      LocalObject(631, Door.Constructor) //spawn room door, kitchen
      LocalObject(634, Door.Constructor) //air term building, interior
      LocalObject(638, Door.Constructor(Vector3(4016.0078f, 4212.008f, 249.29688f), Vector3(0, 0, 270))) //cc door
      LocalObject(642, Door.Constructor(Vector3(4023.9844f, 4212.008f, 249.32812f), Vector3(0, 0, 90))) //cc door, interior
      LocalObject(643, Door.Constructor) //cc door, exterior
      LocalObject(645, Door.Constructor) //b.door, interior
      LocalObject(646, Door.Constructor) //b.door, interior
      LocalObject(715, Door.Constructor(Vector3(3961.5938f ,4235.8125f, 266.84375f), Vector3(0, 0, 90))) //f.door
      LocalObject(751, IFFLock.Constructor)
      LocalObject(860, IFFLock.Constructor)
      LocalObject(863, IFFLock.Constructor)
      LocalObject(866, IFFLock.Constructor)
      LocalObject(868, IFFLock.Constructor)
      LocalObject(873, IFFLock.Constructor)
      LocalObject(874, IFFLock.Constructor)
      LocalObject(875, IFFLock.Constructor)
      LocalObject(876, IFFLock.Constructor)
      LocalObject(878, IFFLock.Constructor)
      LocalObject(879, IFFLock.Constructor)
      LocalObject(882, IFFLock.Constructor)
      LocalObject(884, IFFLock.Constructor)
      LocalObject(885, IFFLock.Constructor)
      LocalObject(1177, Locker.Constructor)
      LocalObject(1178, Locker.Constructor)
      LocalObject(1179, Locker.Constructor)
      LocalObject(1180, Locker.Constructor)
      LocalObject(1181, Locker.Constructor)
      LocalObject(1182, Locker.Constructor)
      LocalObject(1183, Locker.Constructor)
      LocalObject(1184, Locker.Constructor)
      LocalObject(1185, Locker.Constructor)
      LocalObject(1186, Locker.Constructor)
      LocalObject(1187, Locker.Constructor)
      LocalObject(1188, Locker.Constructor)
      LocalObject(1492, ProximityTerminal.Constructor(medical_terminal)) //lobby
      LocalObject(1494, ProximityTerminal.Constructor(medical_terminal)) //kitchen
      LocalObject(1564, Terminal.Constructor(order_terminal))
      LocalObject(1568, Terminal.Constructor(order_terminal))
      LocalObject(1569, Terminal.Constructor(order_terminal))
      LocalObject(1570, Terminal.Constructor(order_terminal))
      LocalObject(1571, Terminal.Constructor(order_terminal))
      LocalObject(1576, Terminal.Constructor(order_terminal))
      LocalObject(1577, Terminal.Constructor(order_terminal))
      LocalObject(1578, Terminal.Constructor(order_terminal))
      LocalObject(2145, SpawnTube.Constructor(Vector3(3980.4062f, 4252.7656f, 257.5625f), Vector3(0, 0, 90)))
      LocalObject(2146, SpawnTube.Constructor(Vector3(3980.4062f, 4259.992f, 257.5625f), Vector3(0, 0, 90)))
      LocalObject(2147, SpawnTube.Constructor(Vector3(3980.4062f, 4267.3047f, 257.5625f), Vector3(0, 0, 90)))
      LocalObject(2239, Terminal.Constructor(spawn_terminal))
      LocalObject(2244, Terminal.Constructor(spawn_terminal))
      LocalObject(2245, Terminal.Constructor(spawn_terminal))
      LocalObject(2246, Terminal.Constructor(spawn_terminal))
      LocalObject(2248, Terminal.Constructor(spawn_terminal))
      LocalObject(2250, Terminal.Constructor(spawn_terminal))
      LocalObject(2251, Terminal.Constructor(spawn_terminal))
      LocalObject(2253, Terminal.Constructor(spawn_terminal))
      LocalObject(2254, Terminal.Constructor(spawn_terminal))
      LocalObject(2322, Door.Constructor) //spawn tube door
      LocalObject(2323, Door.Constructor) //spawn tube door
      LocalObject(2324, Door.Constructor) //spawn tube door
      LocalObject(2419, Terminal.Constructor(ground_vehicle_terminal))
      LocalObject(1479,
        VehicleSpawnPad.Constructor(Vector3(3962.0f, 4334.0f, 267.75f), Vector3(0f, 0f, 180.0f))
      )
      LocalObject(224, Terminal.Constructor(dropship_vehicle_terminal))
      LocalObject(223,
        VehicleSpawnPad.Constructor(Vector3(4012.3594f, 4364.8047f, 271.90625f), Vector3(0f, 0f, 0f))
      )
      ObjectToBuilding(222, 2)
      ObjectToBuilding(223, 2)
      ObjectToBuilding(224, 2)
      ObjectToBuilding(370, 2)
      ObjectToBuilding(371, 2)
      ObjectToBuilding(372, 2)
      ObjectToBuilding(373, 2)
      ObjectToBuilding(375, 2)
      ObjectToBuilding(376, 2)
      ObjectToBuilding(383, 2)
      ObjectToBuilding(384, 2)
      ObjectToBuilding(385, 2)
      ObjectToBuilding(387, 2)
      ObjectToBuilding(391, 2)
      ObjectToBuilding(393, 2)
      ObjectToBuilding(394, 2)
      ObjectToBuilding(396, 2)
      ObjectToBuilding(398, 2)
      ObjectToBuilding(399, 2)
      ObjectToBuilding(402, 2)
      ObjectToBuilding(403, 2)
      ObjectToBuilding(404, 2)
      ObjectToBuilding(603, 2)
      ObjectToBuilding(604, 2)
      ObjectToBuilding(605, 2)
      ObjectToBuilding(606, 2)
      ObjectToBuilding(607, 2)
      ObjectToBuilding(610, 2)
      ObjectToBuilding(611, 2)
      ObjectToBuilding(614, 2)
      ObjectToBuilding(619, 2)
      ObjectToBuilding(620, 2)
      ObjectToBuilding(621, 2)
      ObjectToBuilding(622, 2)
      ObjectToBuilding(623, 2)
      ObjectToBuilding(630, 2)
      ObjectToBuilding(631, 2)
      ObjectToBuilding(634, 2)
      ObjectToBuilding(638, 2)
      ObjectToBuilding(642, 2)
      ObjectToBuilding(643, 2)
      ObjectToBuilding(645, 2)
      ObjectToBuilding(646, 2)
      ObjectToBuilding(715, 2)
      ObjectToBuilding(751, 2)
      ObjectToBuilding(860, 2)
      ObjectToBuilding(863, 2)
      ObjectToBuilding(866, 2)
      ObjectToBuilding(868, 2)
      ObjectToBuilding(873, 2)
      ObjectToBuilding(874, 2)
      ObjectToBuilding(875, 2)
      ObjectToBuilding(876, 2)
      ObjectToBuilding(878, 2)
      ObjectToBuilding(879, 2)
      ObjectToBuilding(882, 2)
      ObjectToBuilding(884, 2)
      ObjectToBuilding(885, 2)
      ObjectToBuilding(1177, 2)
      ObjectToBuilding(1178, 2)
      ObjectToBuilding(1179, 2)
      ObjectToBuilding(1180, 2)
      ObjectToBuilding(1181, 2)
      ObjectToBuilding(1182, 2)
      ObjectToBuilding(1183, 2)
      ObjectToBuilding(1184, 2)
      ObjectToBuilding(1185, 2)
      ObjectToBuilding(1186, 2)
      ObjectToBuilding(1187, 2)
      ObjectToBuilding(1188, 2)
      ObjectToBuilding(1492, 2)
      ObjectToBuilding(1494, 2)
      ObjectToBuilding(1479, 2)
      ObjectToBuilding(1564, 2)
      ObjectToBuilding(1568, 2)
      ObjectToBuilding(1569, 2)
      ObjectToBuilding(1570, 2)
      ObjectToBuilding(1571, 2)
      ObjectToBuilding(1576, 2)
      ObjectToBuilding(1577, 2)
      ObjectToBuilding(1578, 2)
      ObjectToBuilding(2145, 2)
      ObjectToBuilding(2146, 2)
      ObjectToBuilding(2147, 2)
      ObjectToBuilding(2239, 2)
      ObjectToBuilding(2244, 2)
      ObjectToBuilding(2245, 2)
      ObjectToBuilding(2246, 2)
      ObjectToBuilding(2248, 2)
      ObjectToBuilding(2250, 2)
      ObjectToBuilding(2251, 2)
      ObjectToBuilding(2253, 2)
      ObjectToBuilding(2254, 2)
      ObjectToBuilding(2322, 2)
      ObjectToBuilding(2323, 2)
      ObjectToBuilding(2324, 2)
      ObjectToBuilding(2419, 2)
      DoorToLock(375, 863)
      DoorToLock(376, 860)
      DoorToLock(384, 866)
      DoorToLock(387, 868)
      DoorToLock(393, 876)
      DoorToLock(394, 879)
      DoorToLock(404, 885)
      DoorToLock(620, 873)
      DoorToLock(622, 876)
      DoorToLock(623, 874)
      DoorToLock(630, 878)
      DoorToLock(638, 882)
      DoorToLock(642, 884)
      DoorToLock(715, 751)
      TerminalToSpawnPad(224, 223)
      TerminalToSpawnPad(2419, 1479)
    }

    def Building38() : Unit = {
      //Anguta, West Bunker
      LocalBuilding(38, FoundationBuilder(Building.Structure(StructureType.Bunker)))
      LocalObject(362, Door.Constructor)
      ObjectToBuilding(362, 38)
    }

    def Building42() : Unit = {
      //Anguta, East Bunker(s)
      LocalBuilding(42, FoundationBuilder(Building.Structure(StructureType.Bunker)))
      LocalObject(407, Door.Constructor)
      LocalObject(408, Door.Constructor)
      ObjectToBuilding(407, 42)
      ObjectToBuilding(408, 42)
    }

    def Building48() : Unit = {
      //North Anguta Watchtower
      LocalBuilding(48, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3864.2266f, 4518.0234f, 0))))
      LocalObject(364, Door.Constructor(Vector3(3871.9688f, 4509.992f, 269.65625f), Vector3(0f, 0f, 180f))) //s1
      LocalObject(365, Door.Constructor(Vector3(3871.9688f, 4509.992f, 279.57812f), Vector3(0f, 0f, 180f))) //s2
      LocalObject(366, Door.Constructor(Vector3(3871.9688f, 4509.992f, 299.57812f), Vector3(0f, 0f, 180f))) //s3
      LocalObject(367, Door.Constructor(Vector3(3871.9688f, 4525.9844f, 269.65625f), Vector3(0f, 0f, 0f))) //n1
      LocalObject(368, Door.Constructor(Vector3(3871.9688f, 4525.9844f, 279.57812f), Vector3(0f, 0f, 0f))) //n2
      LocalObject(369, Door.Constructor(Vector3(3871.9688f, 4525.9844f, 299.57812f), Vector3(0f, 0f, 0f))) //n3
      LocalObject(854, IFFLock.Constructor)
      LocalObject(855, IFFLock.Constructor)
      LocalObject(856, IFFLock.Constructor)
      LocalObject(857, IFFLock.Constructor)
      LocalObject(858, IFFLock.Constructor)
      LocalObject(859, IFFLock.Constructor)
      LocalObject(1140, Locker.Constructor)
      LocalObject(1141, Locker.Constructor)
      LocalObject(1142, Locker.Constructor)
      LocalObject(1143, Locker.Constructor)
      LocalObject(1144, Locker.Constructor)
      LocalObject(1145, Locker.Constructor)
      LocalObject(1146, Locker.Constructor)
      LocalObject(1147, Locker.Constructor)
      LocalObject(1561, Terminal.Constructor(order_terminal))
      LocalObject(1562, Terminal.Constructor(order_terminal))
      LocalObject(1563, Terminal.Constructor(order_terminal))
      LocalObject(2138, SpawnTube.Constructor(respawn_tube_tower, Vector3(3870.9688f, 4505.7266f, 259.875f), Vector3(0, 0, 90)))
      LocalObject(2139, SpawnTube.Constructor(respawn_tube_tower, Vector3(3870.9688f, 4522.1562f, 259.875f), Vector3(0, 0, 90)))
      LocalObject(2315, Door.Constructor) //spawn tube door
      LocalObject(2316, Door.Constructor) //spawn tube door
      ObjectToBuilding(364, 48)
      ObjectToBuilding(365, 48)
      ObjectToBuilding(366, 48)
      ObjectToBuilding(367, 48)
      ObjectToBuilding(368, 48)
      ObjectToBuilding(369, 48)
      ObjectToBuilding(854, 48)
      ObjectToBuilding(855, 48)
      ObjectToBuilding(856, 48)
      ObjectToBuilding(857, 48)
      ObjectToBuilding(858, 48)
      ObjectToBuilding(859, 48)
      ObjectToBuilding(1140, 48)
      ObjectToBuilding(1141, 48)
      ObjectToBuilding(1142, 48)
      ObjectToBuilding(1143, 48)
      ObjectToBuilding(1144, 48)
      ObjectToBuilding(1145, 48)
      ObjectToBuilding(1146, 48)
      ObjectToBuilding(1147, 48)
      ObjectToBuilding(1561, 48)
      ObjectToBuilding(1562, 48)
      ObjectToBuilding(1563, 48)
      ObjectToBuilding(2138, 48)
      ObjectToBuilding(2139, 48)
      ObjectToBuilding(2315, 48)
      ObjectToBuilding(2316, 48)
      DoorToLock(364, 857)
      DoorToLock(365, 858)
      DoorToLock(366, 859)
      DoorToLock(367, 854)
      DoorToLock(368, 855)
      DoorToLock(369, 856)
    }

    def Building49() : Unit = {
      //North Akna Air Tower
      LocalBuilding(49, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4358.3203f, 3989.5625f, 0))))
      LocalObject(430, Door.Constructor(Vector3(4366.0156f, 3981.9922f, 237.96875f), Vector3(0f, 0f, 180f))) //s1
      LocalObject(431, Door.Constructor(Vector3(4366.0156f, 3981.9922f, 257.89062f), Vector3(0f, 0f, 180f))) //s2
      LocalObject(432, Door.Constructor(Vector3(4366.0156f, 3997.9297f, 237.96875f), Vector3(0f, 0f, 0f))) //n1
      LocalObject(433, Door.Constructor(Vector3(4366.0156f, 3997.9297f, 257.89062f), Vector3(0f, 0f, 0f))) //n2
      LocalObject(902, IFFLock.Constructor)
      LocalObject(903, IFFLock.Constructor)
      LocalObject(906, IFFLock.Constructor)
      LocalObject(907, IFFLock.Constructor)
      LocalObject(1217, Locker.Constructor)
      LocalObject(1218, Locker.Constructor)
      LocalObject(1219, Locker.Constructor)
      LocalObject(1220, Locker.Constructor)
      LocalObject(1225, Locker.Constructor)
      LocalObject(1226, Locker.Constructor)
      LocalObject(1227, Locker.Constructor)
      LocalObject(1228, Locker.Constructor)
      LocalObject(1591, Terminal.Constructor(order_terminal))
      LocalObject(1592, Terminal.Constructor(order_terminal))
      LocalObject(1593, Terminal.Constructor(order_terminal))
      LocalObject(2156, SpawnTube.Constructor(respawn_tube_tower, Vector3(4364.633f, 3994.125f, 228.1875f), Vector3(0, 0, 90)))
      LocalObject(2157, SpawnTube.Constructor(respawn_tube_tower, Vector3(4364.633f, 3977.7266f, 228.1875f), Vector3(0, 0, 90)))
      LocalObject(2333, Door.Constructor) //spawn tube door
      LocalObject(2334, Door.Constructor) //spawn tube door
      ObjectToBuilding(430, 49)
      ObjectToBuilding(431, 49)
      ObjectToBuilding(432, 49)
      ObjectToBuilding(433, 49)
      ObjectToBuilding(902, 49)
      ObjectToBuilding(903, 49)
      ObjectToBuilding(906, 49)
      ObjectToBuilding(907, 49)
      ObjectToBuilding(1217, 49)
      ObjectToBuilding(1218, 49)
      ObjectToBuilding(1219, 49)
      ObjectToBuilding(1220, 49)
      ObjectToBuilding(1225, 49)
      ObjectToBuilding(1226, 49)
      ObjectToBuilding(1227, 49)
      ObjectToBuilding(1228, 49)
      ObjectToBuilding(1591, 49)
      ObjectToBuilding(1592, 49)
      ObjectToBuilding(1593, 49)
      ObjectToBuilding(2156, 49)
      ObjectToBuilding(2157, 49)
      ObjectToBuilding(2333, 49)
      ObjectToBuilding(2334, 49)
      DoorToLock(430, 906)
      DoorToLock(431, 907)
      DoorToLock(432, 902)
      DoorToLock(433, 903)
    }
  }

  val map7 = new ZoneMap("map07")

  val map8 = new ZoneMap("map08")

  val map9 = new ZoneMap("map09")

  val map10 = new ZoneMap("map10")

  val map11 = new ZoneMap("map11") {
    Building1()
    Building2()
    Building3()
    Building67()

    def Building1() : Unit = {
      //warpgate?
      LocalBuilding(1, FoundationBuilder(WarpGate.Structure))
    }

    def Building2() : Unit = {
      //warpgate?
      LocalBuilding(3, FoundationBuilder(WarpGate.Structure))
    }

    def Building3() : Unit = {
      //warpgate?
      LocalBuilding(3, FoundationBuilder(WarpGate.Structure))
    }

    def Building67() : Unit = {
      //spawn building south of HART C
      LocalBuilding(67, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1, 0, 0))))
      LocalObject(282, Door.Constructor) //spawn tube door
      LocalObject(396, Door.Constructor) //entrance
      LocalObject(766, SpawnTube.Constructor(Vector3(3138.0f, 2875.0f, 36.2f), Vector3(0, 0, 180)))
      ObjectToBuilding(282, 67)
      ObjectToBuilding(396, 67)
      ObjectToBuilding(766, 67)
    }
  }

  val map12 = new ZoneMap("map12") {
    Building1()
    Building2()
    Building3()
    Building67()

    def Building1() : Unit = {
      //warpgate?
      LocalBuilding(1, FoundationBuilder(WarpGate.Structure))
    }

    def Building2() : Unit = {
      //warpgate?
      LocalBuilding(3, FoundationBuilder(WarpGate.Structure))
    }

    def Building3() : Unit = {
      //warpgate?
      LocalBuilding(3, FoundationBuilder(WarpGate.Structure))
    }

    def Building67() : Unit = {
      //spawn building south of HART C
      LocalBuilding(67, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1, 0, 0))))
      LocalObject(282, Door.Constructor) //spawn tube door
      LocalObject(396, Door.Constructor) //entrance
      LocalObject(766, SpawnTube.Constructor(Vector3(3138.0f, 2875.0f, 36.2f), Vector3(0, 0, 180)))
      ObjectToBuilding(282, 67)
      ObjectToBuilding(396, 67)
      ObjectToBuilding(766, 67)
    }
  }

  val map13 = new ZoneMap("map13") {
    Building1()
    Building2()
    Building3()
    Building29()
    Building42()
    Building51()
    Building52()
    Building77()
    Building79()
    Building81()

    def Building1() : Unit = {
      //warpgate?
      LocalBuilding(1, FoundationBuilder(WarpGate.Structure))
    }

    //    LocalBuilding(2, FoundationBuilder(WarpGate.Structure)) //TODO might be wrong?

    def Building3() : Unit = {
      //warpgate?
      LocalBuilding(3, FoundationBuilder(WarpGate.Structure))
    }

//    LocalObject(520, ImplantTerminalMech.Constructor) //Hart B
//    LocalObject(1081, Terminal.Constructor(implant_terminal_interface)) //tube 520
//    TerminalToInterface(520, 1081)

    def Building2() : Unit = {
      //HART building C
      LocalBuilding(2, FoundationBuilder(Building.Structure(StructureType.Building)))
      LocalObject(186, Terminal.Constructor(cert_terminal))
      LocalObject(187, Terminal.Constructor(cert_terminal))
      LocalObject(188, Terminal.Constructor(cert_terminal))
      LocalObject(362, Door.Constructor)
      LocalObject(370, Door.Constructor)
      LocalObject(371, Door.Constructor)
      LocalObject(374, Door.Constructor)
      LocalObject(375, Door.Constructor)
      LocalObject(394, Door.Constructor)
      LocalObject(395, Door.Constructor)
      LocalObject(396, Door.Constructor)
      LocalObject(397, Door.Constructor)
      LocalObject(398, Door.Constructor)
      LocalObject(462, Door.Constructor)
      LocalObject(463, Door.Constructor)
      LocalObject(522, ImplantTerminalMech.Constructor)
      LocalObject(523, ImplantTerminalMech.Constructor)
      LocalObject(524, ImplantTerminalMech.Constructor)
      LocalObject(525, ImplantTerminalMech.Constructor)
      LocalObject(526, ImplantTerminalMech.Constructor)
      LocalObject(527, ImplantTerminalMech.Constructor)
      LocalObject(528, ImplantTerminalMech.Constructor)
      LocalObject(529, ImplantTerminalMech.Constructor)
      LocalObject(686, Locker.Constructor)
      LocalObject(687, Locker.Constructor)
      LocalObject(688, Locker.Constructor)
      LocalObject(689, Locker.Constructor)
      LocalObject(690, Locker.Constructor)
      LocalObject(691, Locker.Constructor)
      LocalObject(692, Locker.Constructor)
      LocalObject(693, Locker.Constructor)
      LocalObject(778, ProximityTerminal.Constructor(medical_terminal))
      LocalObject(779, ProximityTerminal.Constructor(medical_terminal))
      LocalObject(780, ProximityTerminal.Constructor(medical_terminal))
      LocalObject(781, ProximityTerminal.Constructor(medical_terminal))
      LocalObject(842, Terminal.Constructor(order_terminal))
      LocalObject(843, Terminal.Constructor(order_terminal))
      LocalObject(844, Terminal.Constructor(order_terminal))
      LocalObject(845, Terminal.Constructor(order_terminal))
      LocalObject(1082, Terminal.Constructor(implant_terminal_interface)) //TODO guid not correct
      LocalObject(1083, Terminal.Constructor(implant_terminal_interface)) //TODO guid not correct
      LocalObject(1084, Terminal.Constructor(implant_terminal_interface)) //TODO guid not correct
      LocalObject(1085, Terminal.Constructor(implant_terminal_interface)) //TODO guid not correct
      LocalObject(1086, Terminal.Constructor(implant_terminal_interface)) //TODO guid not correct
      LocalObject(1087, Terminal.Constructor(implant_terminal_interface)) //TODO guid not correct
      LocalObject(1088, Terminal.Constructor(implant_terminal_interface)) //TODO guid not correct
      LocalObject(1089, Terminal.Constructor(implant_terminal_interface)) //TODO guid not correct
      ObjectToBuilding(186, 2)
      ObjectToBuilding(187, 2)
      ObjectToBuilding(188, 2)
      ObjectToBuilding(362, 2)
      ObjectToBuilding(370, 2)
      ObjectToBuilding(371, 2)
      ObjectToBuilding(374, 2)
      ObjectToBuilding(375, 2)
      ObjectToBuilding(394, 2)
      ObjectToBuilding(395, 2)
      ObjectToBuilding(396, 2)
      ObjectToBuilding(397, 2)
      ObjectToBuilding(398, 2)
      ObjectToBuilding(462, 2)
      ObjectToBuilding(463, 2)
      ObjectToBuilding(522, 2)
      ObjectToBuilding(523, 2)
      ObjectToBuilding(524, 2)
      ObjectToBuilding(525, 2)
      ObjectToBuilding(526, 2)
      ObjectToBuilding(527, 2)
      ObjectToBuilding(528, 2)
      ObjectToBuilding(529, 2)
      ObjectToBuilding(686, 2)
      ObjectToBuilding(687, 2)
      ObjectToBuilding(688, 2)
      ObjectToBuilding(689, 2)
      ObjectToBuilding(690, 2)
      ObjectToBuilding(691, 2)
      ObjectToBuilding(692, 2)
      ObjectToBuilding(693, 2)
      ObjectToBuilding(778, 2)
      ObjectToBuilding(779, 2)
      ObjectToBuilding(780, 2)
      ObjectToBuilding(781, 2)
      ObjectToBuilding(842, 2)
      ObjectToBuilding(843, 2)
      ObjectToBuilding(844, 2)
      ObjectToBuilding(845, 2)
      ObjectToBuilding(1082, 2)
      ObjectToBuilding(1083, 2)
      ObjectToBuilding(1084, 2)
      ObjectToBuilding(1085, 2)
      ObjectToBuilding(1086, 2)
      ObjectToBuilding(1087, 2)
      ObjectToBuilding(1088, 2)
      ObjectToBuilding(1089, 2)
      TerminalToInterface(522, 1082)
      TerminalToInterface(523, 1083)
      TerminalToInterface(524, 1084)
      TerminalToInterface(525, 1085)
      TerminalToInterface(526, 1086)
      TerminalToInterface(527, 1087)
      TerminalToInterface(528, 1088)
      TerminalToInterface(529, 1089)
    }

    def Building29() : Unit = {
      //South Villa Gun Tower
      LocalBuilding(29, FoundationBuilder(Building.Structure(StructureType.Tower)))
      LocalObject(330, Door.Constructor(Vector3(3979.9219f, 2592.0547f, 91.140625f), Vector3(0, 0, 180)))
      LocalObject(331, Door.Constructor(Vector3(3979.9219f, 2592.0547f, 111.140625f), Vector3(0, 0, 180)))
      LocalObject(332, Door.Constructor(Vector3(3979.9688f, 2608.0625f, 91.140625f), Vector3(0, 0, 0)))
      LocalObject(333, Door.Constructor(Vector3(3979.9688f, 2608.0625f, 111.140625f), Vector3(0, 0, 0)))
      LocalObject(556, IFFLock.Constructor)
      LocalObject(557, IFFLock.Constructor)
      LocalObject(558, IFFLock.Constructor)
      LocalObject(559, IFFLock.Constructor)
      ObjectToBuilding(330, 29)
      ObjectToBuilding(331, 29)
      ObjectToBuilding(332, 29)
      ObjectToBuilding(333, 29)
      ObjectToBuilding(556, 29)
      ObjectToBuilding(557, 29)
      ObjectToBuilding(558, 29)
      ObjectToBuilding(559, 29)
      DoorToLock(330, 558)
      DoorToLock(331, 559)
      DoorToLock(332, 556)
      DoorToLock(333, 557)
    }

    def Building42() : Unit = {
      //spawn building south of HART C
      LocalBuilding(42, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1, 0, 0))))
      LocalObject(258, Door.Constructor) //spawn tube door
      LocalObject(259, Door.Constructor) //spawn tube door
      LocalObject(260, Door.Constructor) //spawn tube door
      LocalObject(261, Door.Constructor) //spawn tube door
      LocalObject(262, Door.Constructor) //spawn tube door
      LocalObject(263, Door.Constructor) //spawn tube door
      LocalObject(372, Door.Constructor) //entrance
      LocalObject(373, Door.Constructor) //entrance
      LocalObject(430, Door.Constructor) //vr door
      LocalObject(431, Door.Constructor) //vr door
      LocalObject(432, Door.Constructor) //vr door
      LocalObject(433, Door.Constructor) //vr door
      LocalObject(434, Door.Constructor) //vr door
      LocalObject(435, Door.Constructor) //vr door
      LocalObject(744, SpawnTube.Constructor(Vector3(3684.336f, 2709.0469f, 91.9f), Vector3(0, 0, 180)))
      LocalObject(745, SpawnTube.Constructor(Vector3(3684.336f, 2713.75f, 91.9f), Vector3(0, 0, 0)))
      LocalObject(746, SpawnTube.Constructor(Vector3(3690.9062f, 2708.4219f, 91.9f), Vector3(0, 0, 180)))
      LocalObject(747, SpawnTube.Constructor(Vector3(3691.0703f, 2713.8672f, 91.9f), Vector3(0, 0, 0)))
      LocalObject(748, SpawnTube.Constructor(Vector3(3697.664f, 2708.3984f, 91.9f), Vector3(0, 0, 180)))
      LocalObject(749, SpawnTube.Constructor(Vector3(3697.711f, 2713.2344f, 91.9f), Vector3(0, 0, 0)))
      LocalObject(852, Terminal.Constructor(order_terminal)) //s. wall
      LocalObject(853, Terminal.Constructor(order_terminal)) //n. wall
      LocalObject(854, Terminal.Constructor(order_terminal)) //s. wall
      LocalObject(855, Terminal.Constructor(order_terminal)) //n. wall
      LocalObject(859, Terminal.Constructor(order_terminal)) //s. wall
      LocalObject(860, Terminal.Constructor(order_terminal)) //n. wall
      ObjectToBuilding(258, 42)
      ObjectToBuilding(259, 42)
      ObjectToBuilding(260, 42)
      ObjectToBuilding(261, 42)
      ObjectToBuilding(262, 42)
      ObjectToBuilding(263, 42)
      ObjectToBuilding(372, 42)
      ObjectToBuilding(373, 42)
      ObjectToBuilding(430, 42)
      ObjectToBuilding(431, 42)
      ObjectToBuilding(432, 42)
      ObjectToBuilding(433, 42)
      ObjectToBuilding(434, 42)
      ObjectToBuilding(435, 42)
      ObjectToBuilding(744, 42)
      ObjectToBuilding(745, 42)
      ObjectToBuilding(746, 42)
      ObjectToBuilding(747, 42)
      ObjectToBuilding(748, 42)
      ObjectToBuilding(749, 42)
      ObjectToBuilding(852, 42)
      ObjectToBuilding(853, 42)
      ObjectToBuilding(854, 42)
      ObjectToBuilding(855, 42)
      ObjectToBuilding(859, 42)
      ObjectToBuilding(860, 42)
    }

    def Building51() : Unit = {
      //air terminal west of HART C
      LocalBuilding(51, FoundationBuilder(Building.Structure(StructureType.Platform)))
      LocalObject(304, Terminal.Constructor(dropship_vehicle_terminal))
      LocalObject(292,
        VehicleSpawnPad.Constructor(Vector3(3508.9844f, 2895.961f, 92.296875f), Vector3(0f, 0f, 90.0f))
      )
      ObjectToBuilding(304, 51)
      ObjectToBuilding(292, 51)
      TerminalToSpawnPad(304, 292)
    }

    def Building52() : Unit = {
      //air terminal southwest of HART C
      LocalBuilding(52, FoundationBuilder(Building.Structure(StructureType.Platform)))
      LocalObject(305, Terminal.Constructor(dropship_vehicle_terminal))
      LocalObject(293,
        VehicleSpawnPad.Constructor(Vector3(3575.0781f, 2654.9766f, 92.296875f), Vector3(0f, 0f, 45.0f))
      )
      ObjectToBuilding(305, 52)
      ObjectToBuilding(293, 52)
      TerminalToSpawnPad(305, 293)
    }

    def Building77() : Unit = {
      //ground terminal west of HART C
      LocalBuilding(77, FoundationBuilder(Building.Structure(StructureType.Platform)))
      LocalObject(1063, Terminal.Constructor(ground_vehicle_terminal))
      LocalObject(706,
        VehicleSpawnPad.Constructor(Vector3(3506.0f, 2820.0f, 92.0625f), Vector3(0f, 0f, 270.0f))
      )
      ObjectToBuilding(1063, 77)
      ObjectToBuilding(706, 77)
      TerminalToSpawnPad(1063, 706)
    }

    def Building79() : Unit = {
      //ground terminal south of HART C
      LocalBuilding(79, FoundationBuilder(Building.Structure(StructureType.Platform)))
      LocalObject(1065, Terminal.Constructor(ground_vehicle_terminal))
      LocalObject(710,
        VehicleSpawnPad.Constructor(Vector3(3659.836f, 2589.875f, 92.0625f), Vector3(0f, 0f, 180.0f))
      )
      ObjectToBuilding(1065, 79)
      ObjectToBuilding(710, 79)
      TerminalToSpawnPad(1065, 710)
    }

    def Building81() : Unit = {
      //ground terminal south of HART C
      LocalBuilding(81, FoundationBuilder(Building.Structure(StructureType.Platform)))
      LocalObject(1067, Terminal.Constructor(ground_vehicle_terminal))
      LocalObject(712,
        VehicleSpawnPad.Constructor(Vector3(3724.0156f, 2589.875f, 92.0625f), Vector3(0f, 0f, 180.0f))
      )
      ObjectToBuilding(1067, 81)
      ObjectToBuilding(712, 81)
      TerminalToSpawnPad(1067, 712)
    }
  }

  val map14 = new ZoneMap("map14")

  val map15 = new ZoneMap("map15")

  val map16 = new ZoneMap("map16")

  val ugd01 = new ZoneMap("ugd01")

  val ugd02 = new ZoneMap("ugd02")

  val ugd03 = new ZoneMap("ugd03")

  val ugd04 = new ZoneMap("ugd04")

  val ugd05 = new ZoneMap("ugd05")

  val ugd06 = new ZoneMap("ugd06")

  val map96 = new ZoneMap("map96")

  val map97 = new ZoneMap("map97")

  val map98 = new ZoneMap("map98")

  val map99 = new ZoneMap("map99")
}
