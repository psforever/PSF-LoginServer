// Copyright (c) 2017 PSForever
import net.psforever.objects.zones.ZoneMap
import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.serverobject.ServerObjectBuilder
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.types.Vector3

object Maps {
  val map1 = new ZoneMap("map01")

  val map2 = new ZoneMap("map02")

  val map3 = new ZoneMap("map03")

  val map4 = new ZoneMap("map04") {

    // Hanish
    LocalObject(ServerObjectBuilder(470, Door.Constructor))
    LocalObject(ServerObjectBuilder(471, Door.Constructor))
    LocalObject(ServerObjectBuilder(472, Door.Constructor))
    LocalObject(ServerObjectBuilder(473, Door.Constructor))
    LocalObject(ServerObjectBuilder(474, Door.Constructor))
    LocalObject(ServerObjectBuilder(475, Door.Constructor))
    LocalObject(ServerObjectBuilder(476, Door.Constructor))
    LocalObject(ServerObjectBuilder(481, Door.Constructor))
    LocalObject(ServerObjectBuilder(482, Door.Constructor))
    LocalObject(ServerObjectBuilder(483, Door.Constructor))
    LocalObject(ServerObjectBuilder(484, Door.Constructor))
    LocalObject(ServerObjectBuilder(763, Door.Constructor))
    LocalObject(ServerObjectBuilder(764, Door.Constructor))
    LocalObject(ServerObjectBuilder(765, Door.Constructor))
    LocalObject(ServerObjectBuilder(766, Door.Constructor))
    LocalObject(ServerObjectBuilder(767, Door.Constructor))
    LocalObject(ServerObjectBuilder(768, Door.Constructor))
    LocalObject(ServerObjectBuilder(769, Door.Constructor))
    LocalObject(ServerObjectBuilder(770, Door.Constructor))
    LocalObject(ServerObjectBuilder(771, Door.Constructor))
    LocalObject(ServerObjectBuilder(772, Door.Constructor))
    LocalObject(ServerObjectBuilder(773, Door.Constructor))
    LocalObject(ServerObjectBuilder(774, Door.Constructor))
    LocalObject(ServerObjectBuilder(775, Door.Constructor))
    LocalObject(ServerObjectBuilder(776, Door.Constructor))
    LocalObject(ServerObjectBuilder(777, Door.Constructor))
    LocalObject(ServerObjectBuilder(778, Door.Constructor))
    LocalObject(ServerObjectBuilder(779, Door.Constructor))
    LocalObject(ServerObjectBuilder(780, Door.Constructor))
    LocalObject(ServerObjectBuilder(781, Door.Constructor))
    LocalObject(ServerObjectBuilder(782, Door.Constructor))
    LocalObject(ServerObjectBuilder(783, Door.Constructor))
    LocalObject(ServerObjectBuilder(784, Door.Constructor))
    LocalObject(ServerObjectBuilder(785, Door.Constructor))
    LocalObject(ServerObjectBuilder(923, Door.Constructor))
    LocalObject(ServerObjectBuilder(932, Door.Constructor))
    LocalObject(ServerObjectBuilder(933, Door.Constructor))

    LocalObject(ServerObjectBuilder(971, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1105, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1106, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1108, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1113, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1114, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1115, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1116, IFFLock.Constructor))

    LocalObject(ServerObjectBuilder(238, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(239, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(240, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(241, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(242, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(243, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(244, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(245, Terminal.Constructor(cert_terminal)))

    LocalObject(ServerObjectBuilder(948, ImplantTerminalMech.Constructor))
    LocalObject(ServerObjectBuilder(949, ImplantTerminalMech.Constructor))

    LocalObject(ServerObjectBuilder(10000, Terminal.Constructor(implant_terminal_interface)))
    LocalObject(ServerObjectBuilder(10001, Terminal.Constructor(implant_terminal_interface)))

    LocalObject(ServerObjectBuilder(1991, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(1992, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(1993, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(1994, Terminal.Constructor(order_terminal)))

    LocalObject(ServerObjectBuilder(3070, Terminal.Constructor(vehicle_terminal_combined)))
    LocalObject(ServerObjectBuilder(1886,
      VehicleSpawnPad.Constructor(Vector3(3675.0f, 5458.0f, 89.0f), Vector3(0f, 0f, 0f))
    )) //TODO guid not correct

    // Girru
    LocalObject(ServerObjectBuilder(513, Door.Constructor))
    LocalObject(ServerObjectBuilder(514, Door.Constructor))
    LocalObject(ServerObjectBuilder(515, Door.Constructor))
    LocalObject(ServerObjectBuilder(516, Door.Constructor))
    LocalObject(ServerObjectBuilder(517, Door.Constructor))
    LocalObject(ServerObjectBuilder(518, Door.Constructor))
    LocalObject(ServerObjectBuilder(519, Door.Constructor))
    LocalObject(ServerObjectBuilder(520, Door.Constructor))
    LocalObject(ServerObjectBuilder(521, Door.Constructor))
    LocalObject(ServerObjectBuilder(522, Door.Constructor))
    LocalObject(ServerObjectBuilder(523, Door.Constructor))
    LocalObject(ServerObjectBuilder(524, Door.Constructor))
    LocalObject(ServerObjectBuilder(535, Door.Constructor))
    LocalObject(ServerObjectBuilder(536, Door.Constructor))
    LocalObject(ServerObjectBuilder(537, Door.Constructor))
    LocalObject(ServerObjectBuilder(653, Door.Constructor))
    LocalObject(ServerObjectBuilder(657, Door.Constructor))
    LocalObject(ServerObjectBuilder(810, Door.Constructor))
    LocalObject(ServerObjectBuilder(811, Door.Constructor))
    LocalObject(ServerObjectBuilder(812, Door.Constructor))
    LocalObject(ServerObjectBuilder(813, Door.Constructor))
    LocalObject(ServerObjectBuilder(814, Door.Constructor))
    LocalObject(ServerObjectBuilder(815, Door.Constructor))
    LocalObject(ServerObjectBuilder(816, Door.Constructor))
    LocalObject(ServerObjectBuilder(817, Door.Constructor))
    LocalObject(ServerObjectBuilder(818, Door.Constructor))
    LocalObject(ServerObjectBuilder(819, Door.Constructor))
    LocalObject(ServerObjectBuilder(820, Door.Constructor))
    LocalObject(ServerObjectBuilder(821, Door.Constructor))
    LocalObject(ServerObjectBuilder(822, Door.Constructor))
    LocalObject(ServerObjectBuilder(823, Door.Constructor))
    LocalObject(ServerObjectBuilder(824, Door.Constructor))
    LocalObject(ServerObjectBuilder(825, Door.Constructor))
    LocalObject(ServerObjectBuilder(826, Door.Constructor))
    LocalObject(ServerObjectBuilder(827, Door.Constructor))
    LocalObject(ServerObjectBuilder(828, Door.Constructor))
    LocalObject(ServerObjectBuilder(925, Door.Constructor))

    LocalObject(ServerObjectBuilder(973, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(980, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1142, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1143, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1144, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1145, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1146, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1147, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1148, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1149, IFFLock.Constructor))


    LocalObject(ServerObjectBuilder(2014, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(2015, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(2016, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(2017, Terminal.Constructor(order_terminal)))

    LocalObject(ServerObjectBuilder(3072, Terminal.Constructor(ground_vehicle_terminal)))
    LocalObject(ServerObjectBuilder(501,
      VehicleSpawnPad.Constructor(Vector3(4337.0f, 5903.0f, 58.0f), Vector3(0f, 0f, 0f))
    )) //TODO guid not correct
    LocalObject(ServerObjectBuilder(176, Terminal.Constructor(air_vehicle_terminal)))
    LocalObject(ServerObjectBuilder(502,
      VehicleSpawnPad.Constructor(Vector3(4386.0f, 5928.0f, 93.0f), Vector3(0f, 0f, 0f))
    )) //TODO guid not correct
    LocalObject(ServerObjectBuilder(177, Terminal.Constructor(air_vehicle_terminal)))
    LocalObject(ServerObjectBuilder(503,
      VehicleSpawnPad.Constructor(Vector3(4407.0f, 5927.0f, 93.0f), Vector3(0f, 0f, 0f))
    )) //TODO guid not correct

    // Irkalla
    LocalObject(ServerObjectBuilder(562, Door.Constructor))
    LocalObject(ServerObjectBuilder(563, Door.Constructor))
    LocalObject(ServerObjectBuilder(566, Door.Constructor))
    LocalObject(ServerObjectBuilder(567, Door.Constructor))
    LocalObject(ServerObjectBuilder(572, Door.Constructor))
    LocalObject(ServerObjectBuilder(573, Door.Constructor))
    LocalObject(ServerObjectBuilder(574, Door.Constructor))
    LocalObject(ServerObjectBuilder(575, Door.Constructor))
    LocalObject(ServerObjectBuilder(576, Door.Constructor))
    LocalObject(ServerObjectBuilder(577, Door.Constructor))
    LocalObject(ServerObjectBuilder(578, Door.Constructor))
    LocalObject(ServerObjectBuilder(579, Door.Constructor))
    LocalObject(ServerObjectBuilder(580, Door.Constructor))
    LocalObject(ServerObjectBuilder(585, Door.Constructor))
    LocalObject(ServerObjectBuilder(852, Door.Constructor))
    LocalObject(ServerObjectBuilder(853, Door.Constructor))
    LocalObject(ServerObjectBuilder(854, Door.Constructor))
    LocalObject(ServerObjectBuilder(855, Door.Constructor))
    LocalObject(ServerObjectBuilder(856, Door.Constructor))
    LocalObject(ServerObjectBuilder(857, Door.Constructor))
    LocalObject(ServerObjectBuilder(858, Door.Constructor))
    LocalObject(ServerObjectBuilder(859, Door.Constructor))
    LocalObject(ServerObjectBuilder(860, Door.Constructor))
    LocalObject(ServerObjectBuilder(861, Door.Constructor))
    LocalObject(ServerObjectBuilder(862, Door.Constructor))
    LocalObject(ServerObjectBuilder(863, Door.Constructor))
    LocalObject(ServerObjectBuilder(864, Door.Constructor))
    LocalObject(ServerObjectBuilder(865, Door.Constructor))
    LocalObject(ServerObjectBuilder(866, Door.Constructor))
    LocalObject(ServerObjectBuilder(867, Door.Constructor))
    LocalObject(ServerObjectBuilder(868, Door.Constructor))
    LocalObject(ServerObjectBuilder(869, Door.Constructor))
    LocalObject(ServerObjectBuilder(870, Door.Constructor))
    LocalObject(ServerObjectBuilder(871, Door.Constructor))
    LocalObject(ServerObjectBuilder(872, Door.Constructor))
    LocalObject(ServerObjectBuilder(873, Door.Constructor))
    LocalObject(ServerObjectBuilder(874, Door.Constructor))
    LocalObject(ServerObjectBuilder(875, Door.Constructor))
    LocalObject(ServerObjectBuilder(927, Door.Constructor))

    LocalObject(ServerObjectBuilder(975, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1182, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1183, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1184, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1187, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1190, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1191, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1192, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1193, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1194, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1195, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(1196, IFFLock.Constructor))


    LocalObject(ServerObjectBuilder(2037, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(2038, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(2039, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(2040, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(2041, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(2042, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(2043, Terminal.Constructor(order_terminal)))

    LocalObject(ServerObjectBuilder(3074, Terminal.Constructor(vehicle_terminal_combined)))
    LocalObject(ServerObjectBuilder(504,
      VehicleSpawnPad.Constructor(Vector3(4834.0f, 5185.0f, 67.0f), Vector3(0f, 0f, 45.0f))
    )) //TODO guid not correct

    LocalBases = 100


    // Hanish
    ObjectToBase(470, 30)
    ObjectToBase(471, 30)
    ObjectToBase(472, 30)
    ObjectToBase(473, 30)
    ObjectToBase(474, 30)
    ObjectToBase(475, 30)
    ObjectToBase(476, 30)
    ObjectToBase(481, 30)
    ObjectToBase(482, 30)
    ObjectToBase(483, 30)
    ObjectToBase(484, 30)
    ObjectToBase(763, 30)
    ObjectToBase(764, 30)
    ObjectToBase(765, 30)
    ObjectToBase(766, 30)
    ObjectToBase(767, 30)
    ObjectToBase(768, 30)
    ObjectToBase(769, 30)
    ObjectToBase(770, 30)
    ObjectToBase(771, 30)
    ObjectToBase(772, 30)
    ObjectToBase(773, 30)
    ObjectToBase(774, 30)
    ObjectToBase(775, 30)
    ObjectToBase(776, 30)
    ObjectToBase(777, 30)
    ObjectToBase(778, 30)
    ObjectToBase(779, 30)
    ObjectToBase(780, 30)
    ObjectToBase(781, 30)
    ObjectToBase(782, 30)
    ObjectToBase(783, 30)
    ObjectToBase(784, 30)
    ObjectToBase(785, 30)
    ObjectToBase(923, 30)
    ObjectToBase(932, 30)
    ObjectToBase(933, 30)

    ObjectToBase(971, 30)
    ObjectToBase(1105, 30)
    ObjectToBase(1106, 30)
    ObjectToBase(1108, 30)
    ObjectToBase(1113, 30)
    ObjectToBase(1114, 30)
    ObjectToBase(1115, 30)
    ObjectToBase(1116, 30)

    ObjectToBase(948, 30)
    ObjectToBase(949, 30)

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

    ObjectToBase(3070, 30)
    ObjectToBase(1886, 30)
    TerminalToSpawnPad(3070, 1886)

    // Girru
    ObjectToBase(513, 48)
    ObjectToBase(514, 48)
    ObjectToBase(515, 48)
    ObjectToBase(516, 48)
    ObjectToBase(517, 48)
    ObjectToBase(518, 48)
    ObjectToBase(519, 48)
    ObjectToBase(520, 48)
    ObjectToBase(521, 48)
    ObjectToBase(522, 48)
    ObjectToBase(523, 48)
    ObjectToBase(524, 48)
    ObjectToBase(535, 48)
    ObjectToBase(536, 48)
    ObjectToBase(537, 48)
    ObjectToBase(653, 48)
    ObjectToBase(657, 48)
    ObjectToBase(810, 48)
    ObjectToBase(811, 48)
    ObjectToBase(812, 48)
    ObjectToBase(813, 48)
    ObjectToBase(814, 48)
    ObjectToBase(815, 48)
    ObjectToBase(816, 48)
    ObjectToBase(817, 48)
    ObjectToBase(818, 48)
    ObjectToBase(819, 48)
    ObjectToBase(820, 48)
    ObjectToBase(821, 48)
    ObjectToBase(822, 48)
    ObjectToBase(823, 48)
    ObjectToBase(824, 48)
    ObjectToBase(825, 48)
    ObjectToBase(826, 48)
    ObjectToBase(827, 48)
    ObjectToBase(828, 48)
    ObjectToBase(925, 48)

    ObjectToBase(973, 48)
    ObjectToBase(980, 48)
    ObjectToBase(1142, 48)
    ObjectToBase(1143, 48)
    ObjectToBase(1144, 48)
    ObjectToBase(1145, 48)
    ObjectToBase(1146, 48)
    ObjectToBase(1147, 48)
    ObjectToBase(1148, 48)
    ObjectToBase(1149, 48)

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

    ObjectToBase(3072, 48)
    TerminalToSpawnPad(3072, 501)
    ObjectToBase(176, 48)
    TerminalToSpawnPad(176, 502)
    ObjectToBase(177, 48)
    TerminalToSpawnPad(177, 503)

    // Irkalla
    ObjectToBase(562, 21)
    ObjectToBase(563, 21)
    ObjectToBase(566, 21)
    ObjectToBase(567, 21)
    ObjectToBase(572, 21)
    ObjectToBase(573, 21)
    ObjectToBase(574, 21)
    ObjectToBase(575, 21)
    ObjectToBase(576, 21)
    ObjectToBase(577, 21)
    ObjectToBase(578, 21)
    ObjectToBase(579, 21)
    ObjectToBase(580, 21)
    ObjectToBase(585, 21)
    ObjectToBase(852, 21)
    ObjectToBase(853, 21)
    ObjectToBase(854, 21)
    ObjectToBase(855, 21)
    ObjectToBase(856, 21)
    ObjectToBase(857, 21)
    ObjectToBase(858, 21)
    ObjectToBase(859, 21)
    ObjectToBase(860, 21)
    ObjectToBase(861, 21)
    ObjectToBase(862, 21)
    ObjectToBase(863, 21)
    ObjectToBase(864, 21)
    ObjectToBase(865, 21)
    ObjectToBase(866, 21)
    ObjectToBase(867, 21)
    ObjectToBase(868, 21)
    ObjectToBase(869, 21)
    ObjectToBase(870, 21)
    ObjectToBase(871, 21)
    ObjectToBase(872, 21)
    ObjectToBase(873, 21)
    ObjectToBase(874, 21)
    ObjectToBase(875, 21)
    ObjectToBase(927, 21)

    ObjectToBase(1187, 21)
    ObjectToBase(1191, 21)
    ObjectToBase(1190, 21)
    ObjectToBase(1194, 21)
    ObjectToBase(1195, 21)
    ObjectToBase(1182, 21)
    ObjectToBase(1183, 21)
    ObjectToBase(1184, 21)
    ObjectToBase(1192, 21)
    ObjectToBase(1193, 21)
    ObjectToBase(1196, 21)
    ObjectToBase(975, 21)

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

    ObjectToBase(3074, 21)
    ObjectToBase(504, 21)
    TerminalToSpawnPad(3074, 504)


    //Dagon : 27
    // lahar : 36
    // baal : 42

  }

  val map5 = new ZoneMap("map05")

  val map6 = new ZoneMap("map06")

  val map7 = new ZoneMap("map07")

  val map8 = new ZoneMap("map08")

  val map9 = new ZoneMap("map09")

  val map10 = new ZoneMap("map10")

  val map11 = new ZoneMap("map11")

  val map12 = new ZoneMap("map12")

  val map13 = new ZoneMap("map13") {
    LocalObject(ServerObjectBuilder(330, Door.Constructor))
    LocalObject(ServerObjectBuilder(332, Door.Constructor))
    LocalObject(ServerObjectBuilder(362, Door.Constructor))
    LocalObject(ServerObjectBuilder(370, Door.Constructor))
    LocalObject(ServerObjectBuilder(371, Door.Constructor))
    LocalObject(ServerObjectBuilder(372, Door.Constructor))
    LocalObject(ServerObjectBuilder(373, Door.Constructor))
    LocalObject(ServerObjectBuilder(374, Door.Constructor))
    LocalObject(ServerObjectBuilder(375, Door.Constructor))
    LocalObject(ServerObjectBuilder(394, Door.Constructor))
    LocalObject(ServerObjectBuilder(395, Door.Constructor))
    LocalObject(ServerObjectBuilder(396, Door.Constructor))
    LocalObject(ServerObjectBuilder(397, Door.Constructor))
    LocalObject(ServerObjectBuilder(398, Door.Constructor))
    LocalObject(ServerObjectBuilder(462, Door.Constructor))
    LocalObject(ServerObjectBuilder(463, Door.Constructor))
    LocalObject(ServerObjectBuilder(520, ImplantTerminalMech.Constructor)) //Hart B
    LocalObject(ServerObjectBuilder(522, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(523, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(524, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(525, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(526, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(527, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(528, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(529, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(556, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(558, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(186, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(187, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(188, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(842, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(843, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(844, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(845, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(853, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(855, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(860, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(1081, Terminal.Constructor(implant_terminal_interface))) //tube 520
    LocalObject(ServerObjectBuilder(1082, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1083, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1084, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1085, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1086, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1087, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1088, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1089, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1063, Terminal.Constructor(ground_vehicle_terminal)))
    LocalObject(ServerObjectBuilder(500,
      VehicleSpawnPad.Constructor(Vector3(3506.0f, 2820.0f, 92.0f), Vector3(0f, 0f, 270.0f))
    )) //TODO guid not correct
    LocalObject(ServerObjectBuilder(304, Terminal.Constructor(dropship_vehicle_terminal)))
    LocalObject(ServerObjectBuilder(501,
      VehicleSpawnPad.Constructor(Vector3(3508.9844f, 2895.961f, 92.296875f), Vector3(0f, 0f, 270.0f))
    )) //TODO guid not correct

    LocalBases = 30

    ObjectToBase(330, 29)
    ObjectToBase(332, 29)
    //ObjectToBase(520, 29)
    ObjectToBase(522, 29)
    ObjectToBase(523, 29)
    ObjectToBase(524, 29)
    ObjectToBase(525, 29)
    ObjectToBase(526, 29)
    ObjectToBase(527, 29)
    ObjectToBase(528, 29)
    ObjectToBase(529, 29)
    ObjectToBase(556, 29)
    ObjectToBase(558, 29)
    ObjectToBase(1081, 29)
    ObjectToBase(1063, 2) //TODO unowned courtyard terminal?
    ObjectToBase(500, 2) //TODO unowned courtyard spawnpad?
    ObjectToBase(304, 2) //TODO unowned courtyard terminal?
    ObjectToBase(501, 2) //TODO unowned courtyard spawnpad?

    DoorToLock(330, 558)
    DoorToLock(332, 556)
    TerminalToSpawnPad(1063, 500)
    TerminalToSpawnPad(304, 501)
    TerminalToInterface(520, 1081)
    TerminalToInterface(522, 1082)
    TerminalToInterface(523, 1083)
    TerminalToInterface(524, 1084)
    TerminalToInterface(525, 1085)
    TerminalToInterface(526, 1086)
    TerminalToInterface(527, 1087)
    TerminalToInterface(528, 1088)
    TerminalToInterface(529, 1089)
  }

  val map14 = new ZoneMap("map13")

  val map15 = new ZoneMap("map13")

  val map16 = new ZoneMap("map13")

  val ugd01 = new ZoneMap("ugd01")

  val ugd02 = new ZoneMap("ugd02")

  val ugd03 = new ZoneMap("ugd03")

  val ugd04 = new ZoneMap("ugd04")

  val ugd05 = new ZoneMap("ugd05")

  val ugd06 = new ZoneMap("ugd06")

  val map96 = new ZoneMap("ugd06")

  val map97 = new ZoneMap("map97")

  val map98 = new ZoneMap("map98")

  val map99 = new ZoneMap("map99")
}
