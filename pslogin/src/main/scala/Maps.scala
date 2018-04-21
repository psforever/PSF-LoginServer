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

  val map4 = new ZoneMap("map04")

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

  val map11 = new ZoneMap("map11")

  val map12 = new ZoneMap("map12")

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
        VehicleSpawnPad.Constructor(Vector3(3508.9844f, 2895.961f, 92.296875f), Vector3(0f, 0f, 270.0f))
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
        VehicleSpawnPad.Constructor(Vector3(3575.0781f, 2654.9766f, 92.296875f), Vector3(0f, 0f, 225.0f))
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
        VehicleSpawnPad.Constructor(Vector3(3659.836f, 2589.875f, 92.0625f), Vector3(0f, 0f, 270.0f))
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
