package zonemaps

import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.painbox.Painbox
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.{CaptureTerminal, ProximityTerminal, Terminal}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.serverobject.zipline.ZipLinePath
import net.psforever.objects.zones.{MapScale, ZoneMap}
import net.psforever.types.Vector3

object Ugd01 { // Supai
  val ZoneMap = new ZoneMap("ugd01") {
    Scale = MapScale.Dim2560
    Checksum = 3405929729L

    Building10140()

    def Building10140(): Unit = { // Name: ceiling_bldg_a_10140 Type: ceiling_bldg_a GUID: 1, MapID: 10140
      LocalBuilding("ceiling_bldg_a_10140", 1, 10140, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(850.56f, 1038.27f, 170.61f), ceiling_bldg_a)))
      LocalObject(571, Door.Constructor(Vector3(836.576f, 1045.967f, 172.389f)), owning_building_guid = 1)
      LocalObject(580, Door.Constructor(Vector3(865.815f, 1027.238f, 177.895f)), owning_building_guid = 1)
      LocalObject(581, Door.Constructor(Vector3(865.815f, 1051.238f, 177.895f)), owning_building_guid = 1)
      LocalObject(583, Door.Constructor(Vector3(867.315f, 1042.238f, 172.389f)), owning_building_guid = 1)
    }

    Building10141()

    def Building10141(): Unit = { // Name: ceiling_bldg_b_10141 Type: ceiling_bldg_b GUID: 2, MapID: 10141
      LocalBuilding("ceiling_bldg_b_10141", 2, 10141, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(950.1f, 921.84f, 163.91f), ceiling_bldg_b)))
      LocalObject(595, Door.Constructor(Vector3(952.115f, 938.33f, 165.689f)), owning_building_guid = 2)
      LocalObject(596, Door.Constructor(Vector3(956.116f, 920.83f, 165.689f)), owning_building_guid = 2)
    }

    Building10138()

    def Building10138(): Unit = { // Name: ceiling_bldg_c_10138 Type: ceiling_bldg_c GUID: 3, MapID: 10138
      LocalBuilding("ceiling_bldg_c_10138", 3, 10138, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1077.18f, 1115.65f, 154.15f), ceiling_bldg_c)))
      LocalObject(620, Door.Constructor(Vector3(1033.541f, 1097.152f, 155.929f)), owning_building_guid = 3)
      LocalObject(623, Door.Constructor(Vector3(1052.197f, 1072.395f, 155.929f)), owning_building_guid = 3)
      LocalObject(624, Door.Constructor(Vector3(1080.784f, 1113.377f, 155.929f)), owning_building_guid = 3)
    }

    Building10312()

    def Building10312(): Unit = { // Name: ceiling_bldg_d_10312 Type: ceiling_bldg_d GUID: 4, MapID: 10312
      LocalBuilding("ceiling_bldg_d_10312", 4, 10312, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(993.7f, 1408.09f, 161.96f), ceiling_bldg_d)))
      LocalObject(601, Door.Constructor(Vector3(976.21f, 1408.106f, 163.695f)), owning_building_guid = 4)
      LocalObject(610, Door.Constructor(Vector3(993.716f, 1390.58f, 163.695f)), owning_building_guid = 4)
      LocalObject(611, Door.Constructor(Vector3(993.716f, 1425.58f, 163.695f)), owning_building_guid = 4)
      LocalObject(615, Door.Constructor(Vector3(1011.21f, 1408.106f, 163.695f)), owning_building_guid = 4)
      LocalObject(779, Painbox.Constructor(Vector3(993.6073f, 1407.887f, 170.268f), painbox_continuous), owning_building_guid = 4)
    }

    Building10139()

    def Building10139(): Unit = { // Name: ceiling_bldg_d_10139 Type: ceiling_bldg_d GUID: 5, MapID: 10139
      LocalBuilding("ceiling_bldg_d_10139", 5, 10139, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1287.02f, 1242.45f, 159.65f), ceiling_bldg_d)))
      LocalObject(643, Door.Constructor(Vector3(1269.53f, 1242.466f, 161.385f)), owning_building_guid = 5)
      LocalObject(648, Door.Constructor(Vector3(1287.036f, 1224.94f, 161.385f)), owning_building_guid = 5)
      LocalObject(649, Door.Constructor(Vector3(1287.036f, 1259.94f, 161.385f)), owning_building_guid = 5)
      LocalObject(651, Door.Constructor(Vector3(1304.53f, 1242.466f, 161.385f)), owning_building_guid = 5)
      LocalObject(780, Painbox.Constructor(Vector3(1286.927f, 1242.247f, 167.958f), painbox_continuous), owning_building_guid = 5)
    }

    Building10143()

    def Building10143(): Unit = { // Name: ceiling_bldg_e_10143 Type: ceiling_bldg_e GUID: 6, MapID: 10143
      LocalBuilding("ceiling_bldg_e_10143", 6, 10143, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1408.75f, 1242.78f, 167.66f), ceiling_bldg_e)))
      LocalObject(685, Door.Constructor(Vector3(1396.734f, 1241.79f, 169.439f)), owning_building_guid = 6)
      LocalObject(691, Door.Constructor(Vector3(1412.734f, 1275.29f, 174.939f)), owning_building_guid = 6)
      LocalObject(693, Door.Constructor(Vector3(1427.76f, 1270.796f, 169.439f)), owning_building_guid = 6)
      LocalObject(696, Door.Constructor(Vector3(1433.76f, 1250.796f, 174.939f)), owning_building_guid = 6)
    }

    Building10142()

    def Building10142(): Unit = { // Name: ceiling_bldg_f_10142 Type: ceiling_bldg_f GUID: 7, MapID: 10142
      LocalBuilding("ceiling_bldg_f_10142", 7, 10142, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1376.58f, 1348.91f, 159.37f), ceiling_bldg_f)))
      LocalObject(668, Door.Constructor(Vector3(1352.277f, 1329.785f, 161.149f)), owning_building_guid = 7)
      LocalObject(686, Door.Constructor(Vector3(1404.189f, 1356.455f, 161.149f)), owning_building_guid = 7)
    }

    Building10145()

    def Building10145(): Unit = { // Name: ceiling_bldg_g_10145 Type: ceiling_bldg_g GUID: 8, MapID: 10145
      LocalBuilding("ceiling_bldg_g_10145", 8, 10145, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1374.9f, 1025.85f, 162.66f), ceiling_bldg_g)))
      LocalObject(671, Door.Constructor(Vector3(1363.438f, 1039.06f, 164.439f)), owning_building_guid = 8)
      LocalObject(676, Door.Constructor(Vector3(1379.706f, 1008.152f, 164.439f)), owning_building_guid = 8)
    }

    Building10146()

    def Building10146(): Unit = { // Name: ceiling_bldg_h_10146 Type: ceiling_bldg_h GUID: 9, MapID: 10146
      LocalBuilding("ceiling_bldg_h_10146", 9, 10146, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1311.84f, 904.66f, 161.7f), ceiling_bldg_h)))
      LocalObject(650, Door.Constructor(Vector3(1295.35f, 900.676f, 163.479f)), owning_building_guid = 9)
      LocalObject(658, Door.Constructor(Vector3(1315.824f, 921.17f, 163.479f)), owning_building_guid = 9)
      LocalObject(663, Door.Constructor(Vector3(1322.925f, 893.472f, 165.979f)), owning_building_guid = 9)
    }

    Building10144()

    def Building10144(): Unit = { // Name: ceiling_bldg_i_10144 Type: ceiling_bldg_i GUID: 10, MapID: 10144
      LocalBuilding("ceiling_bldg_i_10144", 10, 10144, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1206.94f, 1100.55f, 172.13f), ceiling_bldg_i)))
      LocalObject(636, Door.Constructor(Vector3(1182.45f, 1104.066f, 173.909f)), owning_building_guid = 10)
      LocalObject(642, Door.Constructor(Vector3(1232.45f, 1104.066f, 173.909f)), owning_building_guid = 10)
    }

    Building10311()

    def Building10311(): Unit = { // Name: ceiling_bldg_j_10311 Type: ceiling_bldg_j GUID: 11, MapID: 10311
      LocalBuilding("ceiling_bldg_j_10311", 11, 10311, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(940.03f, 1345.53f, 162.79f), ceiling_bldg_j)))
      LocalObject(592, Door.Constructor(Vector3(940.046f, 1333.02f, 164.569f)), owning_building_guid = 11)
      LocalObject(593, Door.Constructor(Vector3(940.046f, 1358.02f, 164.569f)), owning_building_guid = 11)
    }

    Building10310()

    def Building10310(): Unit = { // Name: ceiling_bldg_z_10310 Type: ceiling_bldg_z GUID: 12, MapID: 10310
      LocalBuilding("ceiling_bldg_z_10310", 12, 10310, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1055.73f, 1181.27f, 165.51f), ceiling_bldg_z)))
      LocalObject(619, Door.Constructor(Vector3(1032.086f, 1173.73f, 167.289f)), owning_building_guid = 12)
      LocalObject(625, Door.Constructor(Vector3(1082.873f, 1199.608f, 167.289f)), owning_building_guid = 12)
    }

    Building10029()

    def Building10029(): Unit = { // Name: ground_bldg_b_10029 Type: ground_bldg_b GUID: 61, MapID: 10029
      LocalBuilding("ground_bldg_b_10029", 61, 10029, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(794.78f, 864.61f, 103.74f), ground_bldg_b)))
      LocalObject(552, Door.Constructor(Vector3(779.3734f, 870.8243f, 105.519f)), owning_building_guid = 61)
      LocalObject(554, Door.Constructor(Vector3(795.3158f, 889.8535f, 111.019f)), owning_building_guid = 61)
      LocalObject(556, Door.Constructor(Vector3(797.3127f, 870.1596f, 105.519f)), owning_building_guid = 61)
    }

    Building10016()

    def Building10016(): Unit = { // Name: ground_bldg_b_10016 Type: ground_bldg_b GUID: 62, MapID: 10016
      LocalBuilding("ground_bldg_b_10016", 62, 10016, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(865.1f, 916.28f, 85.19f), ground_bldg_b)))
      LocalObject(573, Door.Constructor(Vector3(848.4634f, 897.2867f, 92.469f)), owning_building_guid = 62)
      LocalObject(578, Door.Constructor(Vector3(859.5927f, 913.6567f, 86.969f)), owning_building_guid = 62)
      LocalObject(584, Door.Constructor(Vector3(872.9077f, 901.6165f, 86.969f)), owning_building_guid = 62)
    }

    Building10077()

    def Building10077(): Unit = { // Name: ground_bldg_b_10077 Type: ground_bldg_b GUID: 63, MapID: 10077
      LocalBuilding("ground_bldg_b_10077", 63, 10077, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(998.78f, 1510.64f, 87.06f), ground_bldg_b)))
      LocalObject(607, Door.Constructor(Vector3(989.4824f, 1524.407f, 88.839f)), owning_building_guid = 63)
      LocalObject(614, Door.Constructor(Vector3(1003.983f, 1513.825f, 88.839f)), owning_building_guid = 63)
      LocalObject(616, Door.Constructor(Vector3(1013.34f, 1531.268f, 94.339f)), owning_building_guid = 63)
    }

    Building10066()

    def Building10066(): Unit = { // Name: ground_bldg_b_10066 Type: ground_bldg_b GUID: 64, MapID: 10066
      LocalBuilding("ground_bldg_b_10066", 64, 10066, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1041.45f, 1317.49f, 90.89f), ground_bldg_b)))
      LocalObject(618, Door.Constructor(Vector3(1025.56f, 1322.338f, 92.669f)), owning_building_guid = 64)
      LocalObject(621, Door.Constructor(Vector3(1039.784f, 1342.684f, 98.169f)), owning_building_guid = 64)
      LocalObject(622, Door.Constructor(Vector3(1043.489f, 1323.239f, 92.669f)), owning_building_guid = 64)
    }

    Building10031()

    def Building10031(): Unit = { // Name: ground_bldg_b_10031 Type: ground_bldg_b GUID: 65, MapID: 10031
      LocalBuilding("ground_bldg_b_10031", 65, 10031, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1384.96f, 1262.28f, 85.22f), ground_bldg_b)))
      LocalObject(670, Door.Constructor(Vector3(1360.715f, 1269.331f, 92.499f)), owning_building_guid = 65)
      LocalObject(674, Door.Constructor(Vector3(1374.97f, 1249.007f, 86.999f)), owning_building_guid = 65)
      LocalObject(677, Door.Constructor(Vector3(1380.255f, 1266.163f, 86.999f)), owning_building_guid = 65)
    }

    Building10025()

    def Building10025(): Unit = { // Name: ground_bldg_b_10025 Type: ground_bldg_b GUID: 66, MapID: 10025
      LocalBuilding("ground_bldg_b_10025", 66, 10025, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1493.42f, 1346.97f, 87.93f), ground_bldg_b)))
      LocalObject(707, Door.Constructor(Vector3(1490.413f, 1341.662f, 89.709f)), owning_building_guid = 66)
      LocalObject(708, Door.Constructor(Vector3(1490.686f, 1321.869f, 95.209f)), owning_building_guid = 66)
      LocalObject(714, Door.Constructor(Vector3(1508.226f, 1339.437f, 89.709f)), owning_building_guid = 66)
    }

    Building10017()

    def Building10017(): Unit = { // Name: ground_bldg_c_10017 Type: ground_bldg_c GUID: 67, MapID: 10017
      LocalBuilding("ground_bldg_c_10017", 67, 10017, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(786.51f, 954.37f, 105.26f), ground_bldg_c)))
      LocalObject(553, Door.Constructor(Vector3(787.5582f, 958.4996f, 107.039f)), owning_building_guid = 67)
      LocalObject(562, Door.Constructor(Vector3(817.5875f, 918.5826f, 107.039f)), owning_building_guid = 67)
      LocalObject(570, Door.Constructor(Vector3(835.3683f, 943.9763f, 107.039f)), owning_building_guid = 67)
    }

    Building10030()

    def Building10030(): Unit = { // Name: ground_bldg_c_10030 Type: ground_bldg_c GUID: 68, MapID: 10030
      LocalBuilding("ground_bldg_c_10030", 68, 10030, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(857.33f, 852.26f, 107.42f), ground_bldg_c)))
      LocalObject(560, Door.Constructor(Vector3(813.0717f, 835.2968f, 109.199f)), owning_building_guid = 68)
      LocalObject(567, Door.Constructor(Vector3(830.8526f, 809.903f, 109.199f)), owning_building_guid = 68)
      LocalObject(579, Door.Constructor(Vector3(860.8521f, 849.8626f, 109.199f)), owning_building_guid = 68)
    }

    Building10067()

    def Building10067(): Unit = { // Name: ground_bldg_c_10067 Type: ground_bldg_c GUID: 69, MapID: 10067
      LocalBuilding("ground_bldg_c_10067", 69, 10067, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(973.9f, 1275.59f, 108.53f), ground_bldg_c)))
      LocalObject(597, Door.Constructor(Vector3(966.4615f, 1228.78f, 110.309f)), owning_building_guid = 69)
      LocalObject(603, Door.Constructor(Vector3(977.7372f, 1277.441f, 110.309f)), owning_building_guid = 69)
      LocalObject(613, Door.Constructor(Vector3(997.3436f, 1231.481f, 110.309f)), owning_building_guid = 69)
    }

    Building10305()

    def Building10305(): Unit = { // Name: ground_bldg_c_10305 Type: ground_bldg_c GUID: 70, MapID: 10305
      LocalBuilding("ground_bldg_c_10305", 70, 10305, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1271.71f, 996.36f, 107.32f), ground_bldg_c)))
      LocalObject(644, Door.Constructor(Vector3(1272.394f, 1000.565f, 109.099f)), owning_building_guid = 70)
      LocalObject(653, Door.Constructor(Vector3(1305.788f, 963.4174f, 109.099f)), owning_building_guid = 70)
      LocalObject(661, Door.Constructor(Vector3(1321.288f, 990.2642f, 109.099f)), owning_building_guid = 70)
    }

    Building10022()

    def Building10022(): Unit = { // Name: ground_bldg_c_10022 Type: ground_bldg_c GUID: 71, MapID: 10022
      LocalBuilding("ground_bldg_c_10022", 71, 10022, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1456.97f, 1345.86f, 103.88f), ground_bldg_c)))
      LocalObject(688, Door.Constructor(Vector3(1409.203f, 1360.472f, 105.659f)), owning_building_guid = 71)
      LocalObject(694, Door.Constructor(Vector3(1429.13f, 1384.22f, 105.659f)), owning_building_guid = 71)
      LocalObject(698, Door.Constructor(Vector3(1455.566f, 1341.837f, 105.659f)), owning_building_guid = 71)
    }

    Building10096()

    def Building10096(): Unit = { // Name: ground_bldg_c_10096 Type: ground_bldg_c GUID: 72, MapID: 10096
      LocalBuilding("ground_bldg_c_10096", 72, 10096, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1481.1f, 1136.17f, 101.01f), ground_bldg_c)))
      LocalObject(703, Door.Constructor(Vector3(1481.044f, 1140.43f, 102.789f)), owning_building_guid = 72)
      LocalObject(718, Door.Constructor(Vector3(1520.381f, 1109.646f, 102.789f)), owning_building_guid = 72)
      LocalObject(719, Door.Constructor(Vector3(1530.984f, 1138.776f, 102.789f)), owning_building_guid = 72)
    }

    Building10027()

    def Building10027(): Unit = { // Name: ground_bldg_c_10027 Type: ground_bldg_c GUID: 73, MapID: 10027
      LocalBuilding("ground_bldg_c_10027", 73, 10027, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1493.89f, 1467.82f, 105.26f), ground_bldg_c)))
      LocalObject(709, Door.Constructor(Vector3(1495.639f, 1471.705f, 107.039f)), owning_building_guid = 73)
      LocalObject(717, Door.Constructor(Vector3(1518.281f, 1427.18f, 107.039f)), owning_building_guid = 73)
      LocalObject(720, Door.Constructor(Vector3(1540.201f, 1449.1f, 107.039f)), owning_building_guid = 73)
    }

    Building10028()

    def Building10028(): Unit = { // Name: ground_bldg_d_10028 Type: ground_bldg_d GUID: 74, MapID: 10028
      LocalBuilding("ground_bldg_d_10028", 74, 10028, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(748.19f, 914.57f, 104.86f), ground_bldg_d)))
      LocalObject(547, Door.Constructor(Vector3(733.8558f, 904.5135f, 106.595f)), owning_building_guid = 74)
      LocalObject(548, Door.Constructor(Vector3(738.1335f, 928.9042f, 106.595f)), owning_building_guid = 74)
      LocalObject(550, Door.Constructor(Vector3(758.2087f, 900.2339f, 106.595f)), owning_building_guid = 74)
      LocalObject(551, Door.Constructor(Vector3(762.5261f, 924.5887f, 106.595f)), owning_building_guid = 74)
    }

    Building10074()

    def Building10074(): Unit = { // Name: ground_bldg_d_10074 Type: ground_bldg_d GUID: 75, MapID: 10074
      LocalBuilding("ground_bldg_d_10074", 75, 10074, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(838.93f, 1166.25f, 99.66f), ground_bldg_d)))
      LocalObject(565, Door.Constructor(Vector3(824.5775f, 1176.28f, 101.395f)), owning_building_guid = 75)
      LocalObject(566, Door.Constructor(Vector3(828.8998f, 1151.897f, 101.395f)), owning_building_guid = 75)
      LocalObject(574, Door.Constructor(Vector3(848.975f, 1180.568f, 101.395f)), owning_building_guid = 75)
      LocalObject(575, Door.Constructor(Vector3(853.2478f, 1156.205f, 101.395f)), owning_building_guid = 75)
    }

    Building10018()

    def Building10018(): Unit = { // Name: ground_bldg_d_10018 Type: ground_bldg_d GUID: 76, MapID: 10018
      LocalBuilding("ground_bldg_d_10018", 76, 10018, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(838.98f, 1013.84f, 90.81f), ground_bldg_d)))
      LocalObject(564, Door.Constructor(Vector3(822.5205f, 1019.814f, 92.545f)), owning_building_guid = 76)
      LocalObject(568, Door.Constructor(Vector3(832.983f, 997.4103f, 92.545f)), owning_building_guid = 76)
      LocalObject(572, Door.Constructor(Vector3(844.9537f, 1030.3f, 92.545f)), owning_building_guid = 76)
      LocalObject(576, Door.Constructor(Vector3(855.4097f, 1007.843f, 92.545f)), owning_building_guid = 76)
    }

    Building10078()

    def Building10078(): Unit = { // Name: ground_bldg_d_10078 Type: ground_bldg_d GUID: 77, MapID: 10078
      LocalBuilding("ground_bldg_d_10078", 77, 10078, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1107.02f, 1332.16f, 95.93f), ground_bldg_d)))
      LocalObject(626, Door.Constructor(Vector3(1093.795f, 1343.635f, 97.665f)), owning_building_guid = 77)
      LocalObject(627, Door.Constructor(Vector3(1095.545f, 1318.935f, 97.665f)), owning_building_guid = 77)
      LocalObject(629, Door.Constructor(Vector3(1118.507f, 1345.349f, 97.665f)), owning_building_guid = 77)
      LocalObject(630, Door.Constructor(Vector3(1120.209f, 1320.673f, 97.665f)), owning_building_guid = 77)
    }

    Building10093()

    def Building10093(): Unit = { // Name: ground_bldg_d_10093 Type: ground_bldg_d GUID: 78, MapID: 10093
      LocalBuilding("ground_bldg_d_10093", 78, 10093, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1201.91f, 1035.28f, 97.66f), ground_bldg_d)))
      LocalObject(637, Door.Constructor(Vector3(1185.001f, 1030.733f, 99.395f)), owning_building_guid = 78)
      LocalObject(638, Door.Constructor(Vector3(1197.399f, 1052.178f, 99.395f)), owning_building_guid = 78)
      LocalObject(639, Door.Constructor(Vector3(1206.457f, 1018.371f, 99.395f)), owning_building_guid = 78)
      LocalObject(640, Door.Constructor(Vector3(1218.808f, 1039.791f, 99.395f)), owning_building_guid = 78)
    }

    Building10023()

    def Building10023(): Unit = { // Name: ground_bldg_d_10023 Type: ground_bldg_d GUID: 79, MapID: 10023
      LocalBuilding("ground_bldg_d_10023", 79, 10023, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1363.37f, 1403.85f, 88.96f), ground_bldg_d)))
      LocalObject(667, Door.Constructor(Vector3(1348.231f, 1412.609f, 90.695f)), owning_building_guid = 79)
      LocalObject(669, Door.Constructor(Vector3(1354.611f, 1388.711f, 90.695f)), owning_building_guid = 79)
      LocalObject(673, Door.Constructor(Vector3(1372.111f, 1419.022f, 90.695f)), owning_building_guid = 79)
      LocalObject(675, Door.Constructor(Vector3(1378.542f, 1395.109f, 90.695f)), owning_building_guid = 79)
    }

    Building10094()

    def Building10094(): Unit = { // Name: ground_bldg_d_10094 Type: ground_bldg_d GUID: 80, MapID: 10094
      LocalBuilding("ground_bldg_d_10094", 80, 10094, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1399.93f, 1061.29f, 97.66f), ground_bldg_d)))
      LocalObject(680, Door.Constructor(Vector3(1387.56f, 1048.897f, 99.395f)), owning_building_guid = 80)
      LocalObject(681, Door.Constructor(Vector3(1387.537f, 1073.66f, 99.395f)), owning_building_guid = 80)
      LocalObject(689, Door.Constructor(Vector3(1412.286f, 1048.911f, 99.395f)), owning_building_guid = 80)
      LocalObject(690, Door.Constructor(Vector3(1412.309f, 1073.646f, 99.395f)), owning_building_guid = 80)
    }

    Building10026()

    def Building10026(): Unit = { // Name: ground_bldg_d_10026 Type: ground_bldg_d GUID: 81, MapID: 10026
      LocalBuilding("ground_bldg_d_10026", 81, 10026, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1492.58f, 1397.16f, 90.81f), ground_bldg_d)))
      LocalObject(702, Door.Constructor(Vector3(1477.441f, 1405.919f, 92.545f)), owning_building_guid = 81)
      LocalObject(705, Door.Constructor(Vector3(1483.821f, 1382.021f, 92.545f)), owning_building_guid = 81)
      LocalObject(711, Door.Constructor(Vector3(1501.321f, 1412.332f, 92.545f)), owning_building_guid = 81)
      LocalObject(713, Door.Constructor(Vector3(1507.752f, 1388.419f, 92.545f)), owning_building_guid = 81)
    }

    Building10020()

    def Building10020(): Unit = { // Name: ground_bldg_i_10020 Type: ground_bldg_i GUID: 82, MapID: 10020
      LocalBuilding("ground_bldg_i_10020", 82, 10020, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(745.29f, 997.76f, 95.81f), ground_bldg_i)))
      LocalObject(546, Door.Constructor(Vector3(730.0001f, 978.309f, 97.589f)), owning_building_guid = 82)
      LocalObject(549, Door.Constructor(Vector3(755.0001f, 1021.61f, 97.589f)), owning_building_guid = 82)
    }

    Building10019()

    def Building10019(): Unit = { // Name: ground_bldg_i_10019 Type: ground_bldg_i GUID: 83, MapID: 10019
      LocalBuilding("ground_bldg_i_10019", 83, 10019, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(906.71f, 986.08f, 94.15f), ground_bldg_i)))
      LocalObject(586, Door.Constructor(Vector3(883.743f, 976.8799f, 95.929f)), owning_building_guid = 83)
      LocalObject(591, Door.Constructor(Vector3(927.0443f, 1001.88f, 95.929f)), owning_building_guid = 83)
    }

    Building10095()

    def Building10095(): Unit = { // Name: ground_bldg_i_10095 Type: ground_bldg_i GUID: 84, MapID: 10095
      LocalBuilding("ground_bldg_i_10095", 84, 10095, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1253.14f, 913.31f, 102.44f), ground_bldg_i)))
      LocalObject(641, Door.Constructor(Vector3(1229.29f, 923.0201f, 104.219f)), owning_building_guid = 84)
      LocalObject(645, Door.Constructor(Vector3(1272.591f, 898.0201f, 104.219f)), owning_building_guid = 84)
    }

    Building10092()

    def Building10092(): Unit = { // Name: ground_bldg_i_10092 Type: ground_bldg_i GUID: 85, MapID: 10092
      LocalBuilding("ground_bldg_i_10092", 85, 10092, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1307.38f, 1163.11f, 92.1f), ground_bldg_i)))
      LocalObject(647, Door.Constructor(Vector3(1281.87f, 1159.594f, 93.879f)), owning_building_guid = 85)
      LocalObject(666, Door.Constructor(Vector3(1331.87f, 1159.594f, 93.879f)), owning_building_guid = 85)
    }

    Building10024()

    def Building10024(): Unit = { // Name: ground_bldg_i_10024 Type: ground_bldg_i GUID: 86, MapID: 10024
      LocalBuilding("ground_bldg_i_10024", 86, 10024, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1439.95f, 1456.47f, 91.36f), ground_bldg_i)))
      LocalObject(692, Door.Constructor(Vector3(1419.24f, 1470.006f, 93.139f)), owning_building_guid = 86)
      LocalObject(700, Door.Constructor(Vector3(1464.556f, 1448.876f, 93.139f)), owning_building_guid = 86)
    }

    Building10021()

    def Building10021(): Unit = { // Name: ground_bldg_i_10021 Type: ground_bldg_i GUID: 87, MapID: 10021
      LocalBuilding("ground_bldg_i_10021", 87, 10021, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1451.54f, 1274.29f, 94.15f), ground_bldg_i)))
      LocalObject(695, Door.Constructor(Vector3(1430.52f, 1261.242f, 95.929f)), owning_building_guid = 87)
      LocalObject(701, Door.Constructor(Vector3(1468.822f, 1293.381f, 95.929f)), owning_building_guid = 87)
    }

    Building10425()

    def Building10425(): Unit = { // Name: N_Redoubt Type: redoubt GUID: 88, MapID: 10425
      LocalBuilding("N_Redoubt", 88, 10425, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(882.39f, 1471.4f, 90.88f), redoubt)))
      LocalObject(832, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 88)
      LocalObject(582, Door.Constructor(Vector3(866.5455f, 1478.806f, 92.615f)), owning_building_guid = 88)
      LocalObject(585, Door.Constructor(Vector3(876.3672f, 1487.536f, 102.659f)), owning_building_guid = 88)
      LocalObject(587, Door.Constructor(Vector3(883.8498f, 1484.043f, 102.639f)), owning_building_guid = 88)
      LocalObject(588, Door.Constructor(Vector3(891.1319f, 1480.682f, 102.639f)), owning_building_guid = 88)
      LocalObject(589, Door.Constructor(Vector3(898.2662f, 1464.015f, 92.615f)), owning_building_guid = 88)
      LocalObject(590, Door.Constructor(Vector3(898.6056f, 1477.203f, 102.659f)), owning_building_guid = 88)
      LocalObject(844, Terminal.Constructor(Vector3(874.4547f, 1454.679f, 90.8358f), vanu_equipment_term), owning_building_guid = 88)
      LocalObject(847, Terminal.Constructor(Vector3(890.3133f, 1488.354f, 90.8335f), vanu_equipment_term), owning_building_guid = 88)
      LocalObject(791, SpawnTube.Constructor(Vector3(882.39f, 1471.4f, 90.88f), Vector3(0, 0, 205)), owning_building_guid = 88)
      LocalObject(777, Painbox.Constructor(Vector3(882.5502f, 1471.646f, 98.669f), painbox_continuous), owning_building_guid = 88)
    }

    Building10883()

    def Building10883(): Unit = { // Name: S_Redoubt Type: redoubt GUID: 89, MapID: 10883
      LocalBuilding("S_Redoubt", 89, 10883, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(1399.41f, 850.5f, 91.78f), redoubt)))
      LocalObject(835, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 89)
      LocalObject(678, Door.Constructor(Vector3(1384.128f, 842.5555f, 103.559f)), owning_building_guid = 89)
      LocalObject(679, Door.Constructor(Vector3(1386.684f, 850.4081f, 103.539f)), owning_building_guid = 89)
      LocalObject(682, Door.Constructor(Vector3(1389.132f, 858.0455f, 103.539f)), owning_building_guid = 89)
      LocalObject(683, Door.Constructor(Vector3(1391.674f, 865.8876f, 103.559f)), owning_building_guid = 89)
      LocalObject(684, Door.Constructor(Vector3(1393.99f, 833.871f, 93.515f)), owning_building_guid = 89)
      LocalObject(687, Door.Constructor(Vector3(1404.806f, 867.158f, 93.515f)), owning_building_guid = 89)
      LocalObject(873, Terminal.Constructor(Vector3(1381.617f, 856.2981f, 91.7335f), vanu_equipment_term), owning_building_guid = 89)
      LocalObject(878, Terminal.Constructor(Vector3(1416.974f, 844.6616f, 91.7358f), vanu_equipment_term), owning_building_guid = 89)
      LocalObject(792, SpawnTube.Constructor(Vector3(1399.41f, 850.5f, 91.78f), Vector3(0, 0, 108)), owning_building_guid = 89)
      LocalObject(782, Painbox.Constructor(Vector3(1399.146f, 850.6291f, 99.569f), painbox_continuous), owning_building_guid = 89)
    }

    Building10175()

    def Building10175(): Unit = { // Name: S_Stasis Type: vanu_control_point GUID: 171, MapID: 10175
      LocalBuilding("S_Stasis", 171, 10175, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(983.99f, 1034.17f, 94.51f), vanu_control_point)))
      LocalObject(833, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 171)
      LocalObject(594, Door.Constructor(Vector3(950.054f, 1050.163f, 96.289f)), owning_building_guid = 171)
      LocalObject(598, Door.Constructor(Vector3(967.2758f, 983.8116f, 96.289f)), owning_building_guid = 171)
      LocalObject(599, Door.Constructor(Vector3(968.8129f, 1037.51f, 126.289f)), owning_building_guid = 171)
      LocalObject(600, Door.Constructor(Vector3(970.6356f, 1029.553f, 101.23f)), owning_building_guid = 171)
      LocalObject(602, Door.Constructor(Vector3(976.9498f, 1038.902f, 126.269f)), owning_building_guid = 171)
      LocalObject(604, Door.Constructor(Vector3(980.1362f, 1043.598f, 101.23f)), owning_building_guid = 171)
      LocalObject(605, Door.Constructor(Vector3(984.6804f, 1020.052f, 101.23f)), owning_building_guid = 171)
      LocalObject(606, Door.Constructor(Vector3(984.8152f, 1040.464f, 126.269f)), owning_building_guid = 171)
      LocalObject(608, Door.Constructor(Vector3(991.2167f, 1070.448f, 96.289f)), owning_building_guid = 171)
      LocalObject(609, Door.Constructor(Vector3(992.8783f, 1042.22f, 126.289f)), owning_building_guid = 171)
      LocalObject(612, Door.Constructor(Vector3(994.1946f, 1034.132f, 101.23f)), owning_building_guid = 171)
      LocalObject(617, Door.Constructor(Vector3(1024.07f, 1007.534f, 96.289f)), owning_building_guid = 171)
      LocalObject(851, Terminal.Constructor(Vector3(972.808f, 1035.897f, 99.523f), vanu_equipment_term), owning_building_guid = 171)
      LocalObject(852, Terminal.Constructor(Vector3(975.0327f, 1039.189f, 99.527f), vanu_equipment_term), owning_building_guid = 171)
      LocalObject(853, Terminal.Constructor(Vector3(975.1244f, 1024.373f, 99.527f), vanu_equipment_term), owning_building_guid = 171)
      LocalObject(854, Terminal.Constructor(Vector3(978.3792f, 1022.209f, 99.523f), vanu_equipment_term), owning_building_guid = 171)
      LocalObject(855, Terminal.Constructor(Vector3(986.3745f, 1041.467f, 99.523f), vanu_equipment_term), owning_building_guid = 171)
      LocalObject(856, Terminal.Constructor(Vector3(989.6664f, 1039.242f, 99.527f), vanu_equipment_term), owning_building_guid = 171)
      LocalObject(857, Terminal.Constructor(Vector3(989.7583f, 1024.426f, 99.527f), vanu_equipment_term), owning_building_guid = 171)
      LocalObject(858, Terminal.Constructor(Vector3(991.983f, 1027.718f, 99.523f), vanu_equipment_term), owning_building_guid = 171)
      LocalObject(897, SpawnTube.Constructor(Vector3(982.4083f, 1031.825f, 99.649f), Vector3(0, 0, 259)), owning_building_guid = 171)
      LocalObject(778, Painbox.Constructor(Vector3(982.0579f, 1032.05f, 108.8518f), painbox_continuous), owning_building_guid = 171)
      LocalObject(783, Painbox.Constructor(Vector3(968.9275f, 1029.522f, 103.4f), painbox_door_radius_continuous), owning_building_guid = 171)
      LocalObject(784, Painbox.Constructor(Vector3(979.7823f, 1045.896f, 102.8f), painbox_door_radius_continuous), owning_building_guid = 171)
      LocalObject(785, Painbox.Constructor(Vector3(985.6085f, 1017.388f, 103.4f), painbox_door_radius_continuous), owning_building_guid = 171)
      LocalObject(786, Painbox.Constructor(Vector3(996.1259f, 1034.449f, 103.4f), painbox_door_radius_continuous), owning_building_guid = 171)
    }

    Building10000()

    def Building10000(): Unit = { // Name: N_Stasis Type: vanu_control_point GUID: 172, MapID: 10000
      LocalBuilding("N_Stasis", 172, 10000, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1316.27f, 1321.48f, 88.98f), vanu_control_point)))
      LocalObject(834, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 172)
      LocalObject(646, Door.Constructor(Vector3(1280.496f, 1312.072f, 90.759f)), owning_building_guid = 172)
      LocalObject(652, Door.Constructor(Vector3(1305.138f, 1325.94f, 120.759f)), owning_building_guid = 172)
      LocalObject(654, Door.Constructor(Vector3(1309.486f, 1313.883f, 95.7f)), owning_building_guid = 172)
      LocalObject(655, Door.Constructor(Vector3(1310.251f, 1319.463f, 120.739f)), owning_building_guid = 172)
      LocalObject(656, Door.Constructor(Vector3(1311.83f, 1330.668f, 95.7f)), owning_building_guid = 172)
      LocalObject(657, Door.Constructor(Vector3(1315.104f, 1313.078f, 120.739f)), owning_building_guid = 172)
      LocalObject(659, Door.Constructor(Vector3(1316.773f, 1283.968f, 90.759f)), owning_building_guid = 172)
      LocalObject(660, Door.Constructor(Vector3(1319.922f, 1306.375f, 120.759f)), owning_building_guid = 172)
      LocalObject(662, Door.Constructor(Vector3(1322.64f, 1369.18f, 90.759f)), owning_building_guid = 172)
      LocalObject(664, Door.Constructor(Vector3(1326.274f, 1311.501f, 95.7f)), owning_building_guid = 172)
      LocalObject(665, Door.Constructor(Vector3(1328.656f, 1328.289f, 95.7f)), owning_building_guid = 172)
      LocalObject(672, Door.Constructor(Vector3(1368.859f, 1328.533f, 90.759f)), owning_building_guid = 172)
      LocalObject(863, Terminal.Constructor(Vector3(1308.666f, 1320.424f, 93.993f), vanu_equipment_term), owning_building_guid = 172)
      LocalObject(864, Terminal.Constructor(Vector3(1309.223f, 1324.358f, 93.997f), vanu_equipment_term), owning_building_guid = 172)
      LocalObject(865, Terminal.Constructor(Vector3(1315.686f, 1311.229f, 93.997f), vanu_equipment_term), owning_building_guid = 172)
      LocalObject(866, Terminal.Constructor(Vector3(1318.565f, 1331.493f, 93.993f), vanu_equipment_term), owning_building_guid = 172)
      LocalObject(867, Terminal.Constructor(Vector3(1319.62f, 1310.673f, 93.993f), vanu_equipment_term), owning_building_guid = 172)
      LocalObject(868, Terminal.Constructor(Vector3(1322.5f, 1330.936f, 93.997f), vanu_equipment_term), owning_building_guid = 172)
      LocalObject(869, Terminal.Constructor(Vector3(1328.962f, 1317.806f, 93.997f), vanu_equipment_term), owning_building_guid = 172)
      LocalObject(870, Terminal.Constructor(Vector3(1329.48f, 1321.681f, 93.993f), vanu_equipment_term), owning_building_guid = 172)
      LocalObject(898, SpawnTube.Constructor(Vector3(1319.071f, 1321.086f, 94.119f), Vector3(0, 0, 143)), owning_building_guid = 172)
      LocalObject(781, Painbox.Constructor(Vector3(1319.023f, 1320.673f, 103.3218f), painbox_continuous), owning_building_guid = 172)
      LocalObject(787, Painbox.Constructor(Vector3(1307.576f, 1312.558f, 97.27f), painbox_door_radius_continuous), owning_building_guid = 172)
      LocalObject(788, Painbox.Constructor(Vector3(1310.699f, 1332.265f, 97.87f), painbox_door_radius_continuous), owning_building_guid = 172)
      LocalObject(789, Painbox.Constructor(Vector3(1327.051f, 1309.979f, 97.87f), painbox_door_radius_continuous), owning_building_guid = 172)
      LocalObject(790, Painbox.Constructor(Vector3(1330.644f, 1330.292f, 97.87f), painbox_door_radius_continuous), owning_building_guid = 172)
    }

    Building10015()

    def Building10015(): Unit = { // Name: Core Type: vanu_core GUID: 173, MapID: 10015
      LocalBuilding("Core", 173, 10015, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1151.45f, 1177.46f, 95.63f), vanu_core)))
      LocalObject(628, Door.Constructor(Vector3(1118.471f, 1188.026f, 102.418f)), owning_building_guid = 173)
      LocalObject(631, Door.Constructor(Vector3(1123.351f, 1148.027f, 102.418f)), owning_building_guid = 173)
      LocalObject(632, Door.Constructor(Vector3(1123.351f, 1148.027f, 107.418f)), owning_building_guid = 173)
      LocalObject(633, Door.Constructor(Vector3(1158.469f, 1192.906f, 107.418f)), owning_building_guid = 173)
      LocalObject(634, Door.Constructor(Vector3(1158.469f, 1192.906f, 112.418f)), owning_building_guid = 173)
      LocalObject(635, Door.Constructor(Vector3(1163.349f, 1152.907f, 112.418f)), owning_building_guid = 173)
    }

    Building10001()

    def Building10001(): Unit = { // Name: N_ATPlant Type: vanu_vehicle_station GUID: 216, MapID: 10001
      LocalBuilding("N_ATPlant", 216, 10001, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(851.77f, 1352.04f, 100.81f), vanu_vehicle_station)))
      LocalObject(831, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 216)
      LocalObject(555, Door.Constructor(Vector3(795.9202f, 1354.002f, 132.589f)), owning_building_guid = 216)
      LocalObject(557, Door.Constructor(Vector3(801.1134f, 1360.407f, 132.569f)), owning_building_guid = 216)
      LocalObject(558, Door.Constructor(Vector3(806.1349f, 1366.66f, 132.569f)), owning_building_guid = 216)
      LocalObject(559, Door.Constructor(Vector3(811.3259f, 1373.026f, 132.589f)), owning_building_guid = 216)
      LocalObject(561, Door.Constructor(Vector3(816.9444f, 1316.404f, 102.589f)), owning_building_guid = 216)
      LocalObject(563, Door.Constructor(Vector3(821.9714f, 1322.612f, 122.513f)), owning_building_guid = 216)
      LocalObject(569, Door.Constructor(Vector3(833.7928f, 1375.346f, 122.501f)), owning_building_guid = 216)
      LocalObject(577, Door.Constructor(Vector3(857.54f, 1366.535f, 102.589f)), owning_building_guid = 216)
      LocalObject(721, Door.Constructor(Vector3(790.5573f, 1379.225f, 107.443f)), owning_building_guid = 216)
      LocalObject(826, Terminal.Constructor(Vector3(814.9385f, 1336.645f, 120.727f), vanu_air_vehicle_term), owning_building_guid = 216)
      LocalObject(900, VehicleSpawnPad.Constructor(Vector3(823.2328f, 1339.834f, 120.726f), vanu_vehicle_creation_pad, Vector3(0, 0, 129)), owning_building_guid = 216, terminal_guid = 826)
      LocalObject(827, Terminal.Constructor(Vector3(837.2114f, 1364.164f, 120.727f), vanu_air_vehicle_term), owning_building_guid = 216)
      LocalObject(901, VehicleSpawnPad.Constructor(Vector3(835.8293f, 1355.389f, 120.726f), vanu_vehicle_creation_pad, Vector3(0, 0, 129)), owning_building_guid = 216, terminal_guid = 827)
      LocalObject(839, Terminal.Constructor(Vector3(813.1694f, 1345.502f, 103.31f), vanu_equipment_term), owning_building_guid = 216)
      LocalObject(840, Terminal.Constructor(Vector3(828.273f, 1364.154f, 103.31f), vanu_equipment_term), owning_building_guid = 216)
      LocalObject(905, Terminal.Constructor(Vector3(816.3887f, 1358.16f, 105.81f), vanu_vehicle_term), owning_building_guid = 216)
      LocalObject(899, VehicleSpawnPad.Constructor(Vector3(804.6929f, 1367.77f, 103.215f), vanu_vehicle_creation_pad, Vector3(0, 0, -51)), owning_building_guid = 216, terminal_guid = 905)
    }

    Building10314()

    def Building10314(): Unit = { // Name: S_ATPlant Type: vanu_vehicle_station GUID: 217, MapID: 10314
      LocalBuilding("S_ATPlant", 217, 10314, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1467.03f, 931.27f, 94.78f), vanu_vehicle_station)))
      LocalObject(836, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 217)
      LocalObject(697, Door.Constructor(Vector3(1452.924f, 924.604f, 96.559f)), owning_building_guid = 217)
      LocalObject(699, Door.Constructor(Vector3(1464.283f, 901.9645f, 116.471f)), owning_building_guid = 217)
      LocalObject(704, Door.Constructor(Vector3(1482.297f, 888.3389f, 126.559f)), owning_building_guid = 217)
      LocalObject(706, Door.Constructor(Vector3(1490.434f, 889.4543f, 126.539f)), owning_building_guid = 217)
      LocalObject(710, Door.Constructor(Vector3(1498.372f, 890.6031f, 126.539f)), owning_building_guid = 217)
      LocalObject(712, Door.Constructor(Vector3(1506.538f, 891.7458f, 126.559f)), owning_building_guid = 217)
      LocalObject(715, Door.Constructor(Vector3(1508.893f, 932.47f, 116.483f)), owning_building_guid = 217)
      LocalObject(716, Door.Constructor(Vector3(1516.804f, 933.5817f, 96.559f)), owning_building_guid = 217)
      LocalObject(722, Door.Constructor(Vector3(1493.258f, 869.6415f, 101.413f)), owning_building_guid = 217)
      LocalObject(828, Terminal.Constructor(Vector3(1469.409f, 912.4742f, 114.697f), vanu_air_vehicle_term), owning_building_guid = 217)
      LocalObject(902, VehicleSpawnPad.Constructor(Vector3(1476.404f, 917.949f, 114.696f), vanu_vehicle_creation_pad, Vector3(0, 0, -8)), owning_building_guid = 217, terminal_guid = 828)
      LocalObject(829, Terminal.Constructor(Vector3(1504.466f, 917.4103f, 114.697f), vanu_air_vehicle_term), owning_building_guid = 217)
      LocalObject(904, VehicleSpawnPad.Constructor(Vector3(1496.225f, 920.7347f, 114.696f), vanu_vehicle_creation_pad, Vector3(0, 0, -8)), owning_building_guid = 217, terminal_guid = 829)
      LocalObject(881, Terminal.Constructor(Vector3(1475.953f, 906.3857f, 97.28f), vanu_equipment_term), owning_building_guid = 217)
      LocalObject(882, Terminal.Constructor(Vector3(1499.719f, 909.7258f, 97.28f), vanu_equipment_term), owning_building_guid = 217)
      LocalObject(906, Terminal.Constructor(Vector3(1488.732f, 902.664f, 99.78f), vanu_vehicle_term), owning_building_guid = 217)
      LocalObject(903, VehicleSpawnPad.Constructor(Vector3(1490.732f, 887.6593f, 97.185f), vanu_vehicle_creation_pad, Vector3(0, 0, 172)), owning_building_guid = 217, terminal_guid = 906)
    }

    Building10230()

    def Building10230(): Unit = { // Name: GW_Cavern1_W Type: warpgate_cavern GUID: 218, MapID: 10230
      LocalBuilding("GW_Cavern1_W", 218, 10230, FoundationBuilder(WarpGate.Structure(Vector3(157.44f, 1032.63f, 70.89f))))
    }

    Building10229()

    def Building10229(): Unit = { // Name: GW_Cavern1_S Type: warpgate_cavern GUID: 219, MapID: 10229
      LocalBuilding("GW_Cavern1_S", 219, 10229, FoundationBuilder(WarpGate.Structure(Vector3(1017.57f, 318.45f, 90.98f))))
    }

    Building10231()

    def Building10231(): Unit = { // Name: GW_Cavern1_N Type: warpgate_cavern GUID: 220, MapID: 10231
      LocalBuilding("GW_Cavern1_N", 220, 10231, FoundationBuilder(WarpGate.Structure(Vector3(1030.12f, 2080.82f, 91.42f))))
    }

    Building10228()

    def Building10228(): Unit = { // Name: GW_Cavern1_E Type: warpgate_cavern GUID: 221, MapID: 10228
      LocalBuilding("GW_Cavern1_E", 221, 10228, FoundationBuilder(WarpGate.Structure(Vector3(2143.2f, 1401.86f, 91.44f))))
    }

    ZoneOwnedObjects()

    def ZoneOwnedObjects(): Unit = {
      LocalObject(837, Terminal.Constructor(Vector3(788.95f, 883.67f, 109.24f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(838, Terminal.Constructor(Vector3(797.79f, 881.34f, 109.24f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(841, Terminal.Constructor(Vector3(835f, 1015.28f, 92.77f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(842, Terminal.Constructor(Vector3(843.21f, 1012.32f, 92.77f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(843, Terminal.Constructor(Vector3(869.22f, 1466.66f, 90.84f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(845, Terminal.Constructor(Vector3(877.77f, 1484.74f, 90.84f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(846, Terminal.Constructor(Vector3(886.94f, 1458.3f, 90.84f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(848, Terminal.Constructor(Vector3(895.74f, 1476.22f, 90.84f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(849, Terminal.Constructor(Vector3(924.69f, 1345.47f, 162.79f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(850, Terminal.Constructor(Vector3(955.27f, 1345.54f, 162.79f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(859, Terminal.Constructor(Vector3(1029.84f, 1332.43f, 96.39f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(860, Terminal.Constructor(Vector3(1042.81f, 1333.65f, 96.39f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(861, Terminal.Constructor(Vector3(1197.72f, 1034.24f, 99.62f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(862, Terminal.Constructor(Vector3(1206.08f, 1036.35f, 99.62f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(871, Terminal.Constructor(Vector3(1369.5f, 1032.42f, 165.16f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(872, Terminal.Constructor(Vector3(1380.5f, 1019.15f, 165.16f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(874, Terminal.Constructor(Vector3(1387.02f, 846.09f, 91.74f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(875, Terminal.Constructor(Vector3(1393.14f, 864.93f, 91.74f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(876, Terminal.Constructor(Vector3(1405.83f, 839.8f, 91.74f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(877, Terminal.Constructor(Vector3(1411.95f, 858.67f, 91.74f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(879, Terminal.Constructor(Vector3(1447.85f, 1272.05f, 94.15f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(880, Terminal.Constructor(Vector3(1451.55f, 1282.54f, 94.15f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(883, Terminal.Constructor(Vector3(1513.39f, 1453.92f, 105.26f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(884, Terminal.Constructor(Vector3(1532.47f, 1434.84f, 105.26f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(13, ProximityTerminal.Constructor(Vector3(1028.75f, 1057.12f, 86.98f), crystals_health_a), owning_building_guid = 0)
      LocalObject(14, ProximityTerminal.Constructor(Vector3(1228.83f, 1326.29f, 80.38f), crystals_health_a), owning_building_guid = 0)
      LocalObject(15, ProximityTerminal.Constructor(Vector3(1339.39f, 889.47f, 83.68f), crystals_health_a), owning_building_guid = 0)
      LocalObject(16, ProximityTerminal.Constructor(Vector3(933.67f, 1249.01f, 80.57f), crystals_health_b), owning_building_guid = 0)
      LocalObject(17, ProximityTerminal.Constructor(Vector3(1412.5f, 1170.17f, 82.79f), crystals_health_b), owning_building_guid = 0)
      LocalObject(747, ProximityTerminal.Constructor(Vector3(747.68f, 914.68f, 106.82f), crystals_health_a), owning_building_guid = 0)
      LocalObject(748, ProximityTerminal.Constructor(Vector3(976.42f, 1227.64f, 108.53f), crystals_health_a), owning_building_guid = 0)
      LocalObject(749, ProximityTerminal.Constructor(Vector3(1007.68f, 1525.71f, 92.55f), crystals_health_a), owning_building_guid = 0)
      LocalObject(750, ProximityTerminal.Constructor(Vector3(1318.15f, 980.96f, 107.52f), crystals_health_a), owning_building_guid = 0)
      LocalObject(751, ProximityTerminal.Constructor(Vector3(1367.72f, 1265.01f, 90.71f), crystals_health_a), owning_building_guid = 0)
      LocalObject(752, ProximityTerminal.Constructor(Vector3(839.12f, 1165.86f, 101.62f), crystals_health_b), owning_building_guid = 0)
      LocalObject(753, ProximityTerminal.Constructor(Vector3(854.97f, 902.18f, 90.68f), crystals_health_b), owning_building_guid = 0)
      LocalObject(754, ProximityTerminal.Constructor(Vector3(965.94f, 927.78f, 169.4f), crystals_health_b), owning_building_guid = 0)
      LocalObject(755, ProximityTerminal.Constructor(Vector3(1036.99f, 1176.35f, 160.71f), crystals_health_b), owning_building_guid = 0)
      LocalObject(756, ProximityTerminal.Constructor(Vector3(1202.76f, 1099.57f, 172.13f), crystals_health_b), owning_building_guid = 0)
      LocalObject(757, ProximityTerminal.Constructor(Vector3(1378.14f, 1364.07f, 159.37f), crystals_health_b), owning_building_guid = 0)
      LocalObject(758, ProximityTerminal.Constructor(Vector3(1400.04f, 1061.4f, 99.62f), crystals_health_b), owning_building_guid = 0)
      LocalObject(759, ProximityTerminal.Constructor(Vector3(1493f, 1396.91f, 92.77f), crystals_health_b), owning_building_guid = 0)
      LocalObject(174, FacilityTurret.Constructor(Vector3(720.34f, 879.21f, 107.12f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(174, 5000)
      LocalObject(175, FacilityTurret.Constructor(Vector3(740.47f, 873.42f, 105.56f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(175, 5001)
      LocalObject(176, FacilityTurret.Constructor(Vector3(772.77f, 1316.41f, 111.98f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(176, 5002)
      LocalObject(177, FacilityTurret.Constructor(Vector3(775.35f, 1074.02f, 87.97f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(177, 5003)
      LocalObject(178, FacilityTurret.Constructor(Vector3(793.33f, 1423.68f, 109.83f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(178, 5004)
      LocalObject(179, FacilityTurret.Constructor(Vector3(808.76f, 1186.28f, 92.98f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(179, 5005)
      LocalObject(180, FacilityTurret.Constructor(Vector3(845.37f, 966.24f, 85.29f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(180, 5006)
      LocalObject(181, FacilityTurret.Constructor(Vector3(860.94f, 953.48f, 86.97f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(181, 5007)
      LocalObject(182, FacilityTurret.Constructor(Vector3(861.43f, 1323.56f, 110.16f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(182, 5008)
      LocalObject(183, FacilityTurret.Constructor(Vector3(873.54f, 1419.93f, 97f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(183, 5009)
      LocalObject(184, FacilityTurret.Constructor(Vector3(886.06f, 1246.28f, 100.59f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(184, 5010)
      LocalObject(185, FacilityTurret.Constructor(Vector3(906.88f, 838.1f, 84.73f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(185, 5011)
      LocalObject(186, FacilityTurret.Constructor(Vector3(914.64f, 1059.49f, 93.58f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(186, 5012)
      LocalObject(187, FacilityTurret.Constructor(Vector3(935.76f, 958.02f, 93.48f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(187, 5013)
      LocalObject(188, FacilityTurret.Constructor(Vector3(942.76f, 1368.39f, 99.29f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(188, 5014)
      LocalObject(189, FacilityTurret.Constructor(Vector3(955.06f, 1068.5f, 124.51f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(189, 5015)
      LocalObject(190, FacilityTurret.Constructor(Vector3(978.59f, 1189.75f, 92.88f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(190, 5016)
      LocalObject(191, FacilityTurret.Constructor(Vector3(1008.32f, 1024.12f, 124.51f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(191, 5017)
      LocalObject(192, FacilityTurret.Constructor(Vector3(1037.51f, 1353.92f, 96.39f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(192, 5018)
      LocalObject(193, FacilityTurret.Constructor(Vector3(1085.44f, 1343.35f, 95.93f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(193, 5019)
      LocalObject(194, FacilityTurret.Constructor(Vector3(1191.17f, 911.53f, 105.76f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(194, 5020)
      LocalObject(195, FacilityTurret.Constructor(Vector3(1248.76f, 1092.9f, 97.89f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(195, 5021)
      LocalObject(196, FacilityTurret.Constructor(Vector3(1265.67f, 890.42f, 111.02f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(196, 5022)
      LocalObject(197, FacilityTurret.Constructor(Vector3(1283.75f, 1144.15f, 99.78f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(197, 5023)
      LocalObject(198, FacilityTurret.Constructor(Vector3(1285.54f, 1321.81f, 118.98f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(198, 5024)
      LocalObject(199, FacilityTurret.Constructor(Vector3(1343.9f, 1308.33f, 118.98f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(199, 5025)
      LocalObject(200, FacilityTurret.Constructor(Vector3(1358.42f, 1146.2f, 96.45f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(200, 5026)
      LocalObject(201, FacilityTurret.Constructor(Vector3(1380.38f, 1299.1f, 86.78f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(201, 5027)
      LocalObject(202, FacilityTurret.Constructor(Vector3(1380.88f, 954.88f, 101.67f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(202, 5028)
      LocalObject(203, FacilityTurret.Constructor(Vector3(1388.61f, 1370.67f, 89.69f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(203, 5029)
      LocalObject(204, FacilityTurret.Constructor(Vector3(1414.46f, 1410f, 90.45f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(204, 5030)
      LocalObject(205, FacilityTurret.Constructor(Vector3(1416.89f, 1043.36f, 97.66f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(205, 5031)
      LocalObject(206, FacilityTurret.Constructor(Vector3(1427.18f, 1482.32f, 99.94f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(206, 5032)
      LocalObject(207, FacilityTurret.Constructor(Vector3(1457.56f, 1304.19f, 101.83f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(207, 5033)
      LocalObject(208, FacilityTurret.Constructor(Vector3(1457.62f, 959.51f, 103.81f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(208, 5034)
      LocalObject(209, FacilityTurret.Constructor(Vector3(1470.74f, 864.76f, 119.48f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(209, 5035)
      LocalObject(210, FacilityTurret.Constructor(Vector3(1471.01f, 1253.19f, 103.63f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(210, 5036)
      LocalObject(211, FacilityTurret.Constructor(Vector3(1477f, 1378.7f, 90.81f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(211, 5037)
      LocalObject(212, FacilityTurret.Constructor(Vector3(1513.88f, 946.28f, 114.7f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(212, 5038)
      LocalObject(213, FacilityTurret.Constructor(Vector3(1519.17f, 1499.98f, 94.38f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(213, 5039)
      LocalObject(214, FacilityTurret.Constructor(Vector3(1545.19f, 1398.01f, 96.71f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(214, 5040)
      LocalObject(215, FacilityTurret.Constructor(Vector3(1556.23f, 1120.6f, 94.85f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(215, 5041)
    }

    def Lattice(): Unit = {
      LatticeLink("N_Redoubt", "N_ATPlant")
      LatticeLink("GW_Cavern1_W", "N_ATPlant")
      LatticeLink("N_Stasis", "Core")
      LatticeLink("S_Stasis", "Core")
      LatticeLink("S_Redoubt", "S_ATPlant")
      LatticeLink("N_Redoubt", "N_Stasis")
      LatticeLink("S_Redoubt", "S_Stasis")
      LatticeLink("S_Stasis", "N_ATPlant")
      LatticeLink("N_Stasis", "S_ATPlant")
      LatticeLink("GW_Cavern1_N", "N_Redoubt")
      LatticeLink("GW_Cavern1_S", "S_Redoubt")
      LatticeLink("GW_Cavern1_E", "S_ATPlant")
    }

    Lattice()

    def ZipLines(): Unit = {
      ZipLinePaths(new ZipLinePath(1, false, List(Vector3(728.092f, 1006.186f, 106.143f), Vector3(829.164f, 1042.8f, 170.939f), Vector3(830.768f, 1043.381f, 171.46f))))
      ZipLinePaths(new ZipLinePath(2, false, List(Vector3(734.749f, 961.909f, 109.207f), Vector3(697.971f, 956.655f, 132.489f), Vector3(695.519f, 956.305f, 132.975f))))
      ZipLinePaths(new ZipLinePath(3, false, List(Vector3(757.247f, 1017.942f, 106.143f), Vector3(848.92f, 1145.895f, 100.51f))))
      ZipLinePaths(new ZipLinePath(4, false, List(Vector3(778.772f, 817.665f, 111.702f), Vector3(949.992f, 916.26f, 164.246f), Vector3(951.34f, 917.036f, 164.778f))))
      ZipLinePaths(new ZipLinePath(5, false, List(Vector3(781.841f, 919.105f, 130.869f), Vector3(830.709f, 1019.833f, 171.021f), Vector3(833.967f, 1026.548f, 171.488f))))
      ZipLinePaths(new ZipLinePath(6, true, List(Vector3(786.778f, 861.66f, 104.079f), Vector3(786.585f, 860.31f, 109.589f))))
      ZipLinePaths(new ZipLinePath(7, true, List(Vector3(808.902f, 1324.021f, 103.66f), Vector3(804.094f, 1317.348f, 121.086f))))
      ZipLinePaths(new ZipLinePath(8, false, List(Vector3(820.334f, 1148.641f, 100.51f), Vector3(734.781f, 1047.986f, 105.623f))))
      ZipLinePaths(new ZipLinePath(9, true, List(Vector3(851.876f, 1380.436f, 121.086f), Vector3(847.951f, 1372.628f, 103.66f))))
      ZipLinePaths(new ZipLinePath(10, false, List(Vector3(876.944f, 1460.559f, 101.73f), Vector3(958.145f, 1415.262f, 162.353f), Vector3(961.393f, 1414.24f, 162.789f))))
      ZipLinePaths(new ZipLinePath(11, false, List(Vector3(860.918f, 1314.696f, 111.015f), Vector3(900.307f, 1332.997f, 108.401f))))
      ZipLinePaths(new ZipLinePath(12, true, List(Vector3(873.693f, 913.825f, 85.532f), Vector3(874.142f, 914.083f, 91.039f))))
      ZipLinePaths(new ZipLinePath(13, false, List(Vector3(874.525f, 1033.849f, 171.461f), Vector3(959.388f, 945.044f, 164.768f))))
      ZipLinePaths(new ZipLinePath(14, true, List(Vector3(880.884f, 830.239f, 85.076f), Vector3(886.746f, 828.151f, 118.33f))))
      ZipLinePaths(new ZipLinePath(15, true, List(Vector3(886.448f, 835.152f, 118.33f), Vector3(897.551f, 832.477f, 85.076f))))
      ZipLinePaths(new ZipLinePath(16, false, List(Vector3(911.301f, 1329.164f, 108.381f), Vector3(970.776f, 1280.269f, 109.38f))))
      ZipLinePaths(new ZipLinePath(17, false, List(Vector3(932.081f, 1322.242f, 163.637f), Vector3(1051.566f, 1199.372f, 154.901f))))
      ZipLinePaths(new ZipLinePath(18, false, List(Vector3(938.644f, 941.755f, 164.787f), Vector3(792.952f, 878.137f, 119.212f))))
      ZipLinePaths(new ZipLinePath(19, true, List(Vector3(944.449f, 928.994f, 164.252f), Vector3(943.808f, 928.722f, 169.759f))))
      ZipLinePaths(new ZipLinePath(20, false, List(Vector3(960.616f, 1218.649f, 109.291f), Vector3(862.937f, 1157.651f, 100.511f))))
      ZipLinePaths(new ZipLinePath(21, false, List(Vector3(966.723f, 1345.001f, 163.636f), Vector3(992.911f, 1381.507f, 162.815f))))
      ZipLinePaths(new ZipLinePath(22, false, List(Vector3(961.889f, 1401.845f, 162.791f), Vector3(939.72f, 1368.197f, 163.637f))))
      ZipLinePaths(new ZipLinePath(23, false, List(Vector3(980.961f, 925.541f, 164.804f), Vector3(1047.716f, 1064.001f, 155.001f))))
      ZipLinePaths(new ZipLinePath(24, false, List(Vector3(993.093f, 1516.838f, 107.591f), Vector3(991.266f, 1436.266f, 162.379f), Vector3(991.207f, 1433.667f, 162.803f))))
      ZipLinePaths(new ZipLinePath(25, false, List(Vector3(1023.794f, 1313.803f, 111.42f), Vector3(1005.777f, 1235.883f, 109.341f))))
      ZipLinePaths(new ZipLinePath(26, false, List(Vector3(1027.815f, 1097.179f, 155.004f), Vector3(869.866f, 1035.819f, 176.964f))))
      ZipLinePaths(new ZipLinePath(27, false, List(Vector3(1029.881f, 982.751f, 102.375f), Vector3(1022.505f, 1015.015f, 110.347f), Vector3(1022.013f, 1017.166f, 110.893f))))
      ZipLinePaths(new ZipLinePath(28, false, List(Vector3(1030.959f, 1102.622f, 155.007f), Vector3(1028.145f, 1165.008f, 166.456f), Vector3(1027.957f, 1169.167f, 166.367f))))
      ZipLinePaths(new ZipLinePath(29, true, List(Vector3(1064.594f, 1191.324f, 154.4f), Vector3(1077.757f, 1197.047f, 161.06f))))
      ZipLinePaths(new ZipLinePath(30, false, List(Vector3(1089.529f, 1198.269f, 166.36f), Vector3(1254.864f, 1249.875f, 160.478f))))
      ZipLinePaths(new ZipLinePath(31, false, List(Vector3(1091.897f, 1357.937f, 122.583f), Vector3(1267.006f, 1337.662f, 109.171f))))
      ZipLinePaths(new ZipLinePath(32, false, List(Vector3(1095.955f, 1193.434f, 95.453f), Vector3(1106.521f, 1186.623f, 100.976f), Vector3(1107.225f, 1186.169f, 101.502f))))
      ZipLinePaths(new ZipLinePath(33, false, List(Vector3(1110.201f, 1149.249f, 106.481f), Vector3(1096.384f, 1127.12f, 154.467f), Vector3(1095.463f, 1125.645f, 155.001f))))
      ZipLinePaths(new ZipLinePath(34, false, List(Vector3(1119.014f, 1123.571f, 91.177f), Vector3(1124.145f, 1135.41f, 100.964f), Vector3(1124.878f, 1137.101f, 101.429f))))
      ZipLinePaths(new ZipLinePath(35, true, List(Vector3(1119.495f, 1167.509f, 100.98f), Vector3(1119.44f, 1167.508f, 105.98f))))
      ZipLinePaths(new ZipLinePath(36, true, List(Vector3(1138.384f, 1190.526f, 95.98f), Vector3(1138.423f, 1190.587f, 100.98f))))
      ZipLinePaths(new ZipLinePath(37, true, List(Vector3(1143.706f, 1149.857f, 95.98f), Vector3(1143.71f, 1149.773f, 100.98f))))
      ZipLinePaths(new ZipLinePath(38, true, List(Vector3(1162.813f, 1142.615f, 110.912f), Vector3(1166.217f, 1148.317f, 82.567f))))
      ZipLinePaths(new ZipLinePath(39, true, List(Vector3(1165.452f, 1173.568f, 105.98f), Vector3(1165.372f, 1173.577f, 110.98f))))
      ZipLinePaths(new ZipLinePath(40, false, List(Vector3(1167.06f, 1111.942f, 172.99f), Vector3(1078.79f, 1081.819f, 152.235f))))
      ZipLinePaths(new ZipLinePath(41, true, List(Vector3(1168.507f, 1194.822f, 105.993f), Vector3(1183.559f, 1193.418f, 82.467f))))
      ZipLinePaths(new ZipLinePath(42, false, List(Vector3(1233.414f, 925.048f, 112.774f), Vector3(1039.055f, 975.065f, 102.352f))))
      ZipLinePaths(new ZipLinePath(43, true, List(Vector3(1234.297f, 1083.106f, 85.33f), Vector3(1239.218f, 1085.932f, 98.231f))))
      ZipLinePaths(new ZipLinePath(44, true, List(Vector3(1240.283f, 1097.896f, 98.223f), Vector3(1241.528f, 1099.065f, 85.33f))))
      ZipLinePaths(new ZipLinePath(45, false, List(Vector3(1253.271f, 1236.121f, 160.518f), Vector3(1169.33f, 1195.406f, 111.531f))))
      ZipLinePaths(new ZipLinePath(46, false, List(Vector3(1271.897f, 1348.201f, 109.172f), Vector3(1315.166f, 1362.907f, 105.351f))))
      ZipLinePaths(new ZipLinePath(47, false, List(Vector3(1276.015f, 890.527f, 103.291f), Vector3(1358.52f, 878.489f, 98.582f))))
      ZipLinePaths(new ZipLinePath(48, false, List(Vector3(1286.969f, 885.244f, 162.235f), Vector3(1285.726f, 885.873f, 161.69f), Vector3(1207.434f, 925.508f, 108.171f))))
      ZipLinePaths(new ZipLinePath(49, false, List(Vector3(1287.639f, 1215.968f, 160.509f), Vector3(1247.467f, 1108.693f, 172.979f))))
      ZipLinePaths(new ZipLinePath(50, false, List(Vector3(1291.198f, 1238.773f, 104.612f), Vector3(1325.749f, 1230.146f, 85.164f))))
      ZipLinePaths(new ZipLinePath(51, false, List(Vector3(1291.852f, 933.318f, 163.861f), Vector3(1377.723f, 1001.816f, 163.51f))))
      ZipLinePaths(new ZipLinePath(52, false, List(Vector3(1296.924f, 1279.436f, 119.881f), Vector3(1296.11f, 1277.555f, 119.179f), Vector3(1283.903f, 1249.341f, 104.662f))))
      ZipLinePaths(new ZipLinePath(53, false, List(Vector3(1320.281f, 1238.297f, 160.523f), Vector3(1392.448f, 1236.987f, 168.496f))))
      ZipLinePaths(new ZipLinePath(54, false, List(Vector3(1330.534f, 1020.952f, 96.131f), Vector3(1360.046f, 1059.981f, 96.074f))))
      ZipLinePaths(new ZipLinePath(55, false, List(Vector3(1333.036f, 990.539f, 108.142f), Vector3(1335.545f, 988.369f, 107.657f), Vector3(1373.179f, 955.828f, 94.447f))))
      ZipLinePaths(new ZipLinePath(56, false, List(Vector3(1345.506f, 1327.547f, 160.182f), Vector3(1320.126f, 1248.518f, 160.517f))))
      ZipLinePaths(new ZipLinePath(57, false, List(Vector3(1361.149f, 1322.011f, 160.194f), Vector3(1407.339f, 1279.782f, 174.015f))))
      ZipLinePaths(new ZipLinePath(58, false, List(Vector3(1369.539f, 1047.413f, 163.51f), Vector3(1405.054f, 1233.856f, 168.561f))))
      ZipLinePaths(new ZipLinePath(59, false, List(Vector3(1376.224f, 1112.759f, 96.075f), Vector3(1340.386f, 1139.509f, 92.592f))))
      ZipLinePaths(new ZipLinePath(60, false, List(Vector3(1383.992f, 936.642f, 104.198f), Vector3(1386.949f, 1004.481f, 162.883f), Vector3(1386.996f, 1005.558f, 163.51f))))
      ZipLinePaths(new ZipLinePath(61, true, List(Vector3(1386.401f, 1253.506f, 85.562f), Vector3(1386.755f, 1253.317f, 91.069f))))
      ZipLinePaths(new ZipLinePath(62, false, List(Vector3(1387.362f, 814.224f, 97.989f), Vector3(1323.164f, 885.178f, 165.05f))))
      ZipLinePaths(new ZipLinePath(63, false, List(Vector3(1408.407f, 992.162f, 113.845f), Vector3(1403.502f, 1039.051f, 98.536f))))
      ZipLinePaths(new ZipLinePath(64, false, List(Vector3(1418.274f, 1281.224f, 174.016f), Vector3(1403.258f, 1346.119f, 160.196f))))
      ZipLinePaths(new ZipLinePath(65, true, List(Vector3(1445.963f, 1403.374f, 104.101f), Vector3(1447.786f, 1400.135f, 81.47f))))
      ZipLinePaths(new ZipLinePath(66, true, List(Vector3(1447.99f, 911.912f, 115.056f), Vector3(1456.023f, 914.153f, 97.63f))))
      ZipLinePaths(new ZipLinePath(67, false, List(Vector3(1486.297f, 1033.346f, 100.602f), Vector3(1512.289f, 1077.211f, 101.158f), Vector3(1526.559f, 1101.294f, 101.778f))))
      ZipLinePaths(new ZipLinePath(68, false, List(Vector3(1491.442f, 1308.886f, 94.285f), Vector3(1440.693f, 1249.149f, 173.51f), Vector3(1439.056f, 1247.222f, 174.001f))))
      ZipLinePaths(new ZipLinePath(69, false, List(Vector3(1496.411f, 1291.616f, 94.928f), Vector3(1513.727f, 1246.264f, 118.798f), Vector3(1514.286f, 1244.801f, 119.316f))))
      ZipLinePaths(new ZipLinePath(70, false, List(Vector3(1510.739f, 1234.732f, 119.268f), Vector3(1477.652f, 1143.291f, 101.868f))))
      ZipLinePaths(new ZipLinePath(71, false, List(Vector3(1513.12f, 1102.706f, 101.832f), Vector3(1421.222f, 1075.085f, 98.51f))))
      ZipLinePaths(new ZipLinePath(72, true, List(Vector3(1518.094f, 922.897f, 97.63f), Vector3(1526.562f, 924.662f, 115.056f))))
      ZipLinePaths(new ZipLinePath(73, false, List(Vector3(1521.523f, 1415.007f, 106.059f), Vector3(1413.48f, 1359.402f, 159.712f), Vector3(1409.995f, 1357.609f, 160.249f))))
      ZipLinePaths(new ZipLinePath(74, false, List(Vector3(1529.09f, 1148.223f, 101.821f), Vector3(1423.234f, 1259.869f, 95f))))
      ZipLinePaths(new ZipLinePath(75, false, List(Vector3(1575.633f, 1422.701f, 142.971f), Vector3(1573.754f, 1422.279f, 142.448f), Vector3(1455.389f, 1395.726f, 104.616f))))
      ZipLinePaths(new ZipLinePath(76, true, List(Vector3(1443.656f, 1407.144f, 81.33f), Vector3(1447.871f, 1399.239f, 104.101f))))
      ZipLinePaths(new ZipLinePath(77, false, List(Vector3(1444.535f, 1293.125f, 104.483f), Vector3(1409.668f, 1349.802f, 159.747f), Vector3(1407.343f, 1353.58f, 160.199f))))
      ZipLinePaths(new ZipLinePath(78, false, List(Vector3(1460.814f, 1471.557f, 101.716f), Vector3(1404.733f, 1385.491f, 159.076f), Vector3(1402.924f, 1382.714f, 159.607f))))
      ZipLinePaths(new ZipLinePath(79, false, List(Vector3(1461.855f, 1344.335f, 104.731f), Vector3(1431.469f, 1279.321f, 168.724f), Vector3(1430.489f, 1277.224f, 168.74f))))
      ZipLinePaths(new ZipLinePath(80, false, List(Vector3(1507.157f, 1349.035f, 109.286f), Vector3(1573.383f, 1404.752f, 142.412f), Vector3(1577.798f, 1408.467f, 142.948f))))
      ZipLinePaths(new ZipLinePath(81, false, List(Vector3(1341.441f, 1316.326f, 119.836f), Vector3(1294.201f, 1275.704f, 160.11f), Vector3(1287.396f, 1269.586f, 160.499f))))
      ZipLinePaths(new ZipLinePath(82, false, List(Vector3(1327.414f, 1137.7f, 99.733f), Vector3(1314.162f, 1035.606f, 96.131f))))
      ZipLinePaths(new ZipLinePath(83, false, List(Vector3(1222.673f, 1027.613f, 98.518f), Vector3(1264.198f, 999.281f, 107.7f), Vector3(1266.641f, 997.614f, 108.178f))))
      ZipLinePaths(new ZipLinePath(84, false, List(Vector3(1202.686f, 1010.169f, 98.51f), Vector3(1181.993f, 958.767f, 96.83f))))
      ZipLinePaths(new ZipLinePath(85, false, List(Vector3(1163.954f, 1595.853f, 148.196f), Vector3(1159.488f, 1593.995f, 147.755f), Vector3(1021.035f, 1536.395f, 93.444f))))
      ZipLinePaths(new ZipLinePath(86, false, List(Vector3(1247.125f, 1603.267f, 88.816f), Vector3(1189.521f, 1599.835f, 147.901f), Vector3(1185.681f, 1599.606f, 147.894f))))
      ZipLinePaths(new ZipLinePath(87, true, List(Vector3(1400.35f, 974.977f, 91.93f), Vector3(1401.252f, 979.522f, 113.331f))))
      ZipLinePaths(new ZipLinePath(88, true, List(Vector3(1413.085f, 980.32f, 113.34f), Vector3(1396.903f, 981.324f, 91.93f))))
      ZipLinePaths(new ZipLinePath(89, false, List(Vector3(1199.138f, 1060.892f, 98.515f), Vector3(1174.509f, 1151.691f, 111.554f))))
      ZipLinePaths(new ZipLinePath(90, false, List(Vector3(1112.192f, 1285.293f, 91.134f), Vector3(1117.61f, 1203.347f, 101.058f), Vector3(1117.971f, 1197.884f, 101.51f))))
      ZipLinePaths(new ZipLinePath(91, false, List(Vector3(1366.348f, 897.049f, 101.075f), Vector3(1313.494f, 953.057f, 108.076f))))
      ZipLinePaths(new ZipLinePath(92, false, List(Vector3(1386.164f, 891.069f, 104.871f), Vector3(1436.442f, 889.065f, 115.11f), Vector3(1453.201f, 888.396f, 115.552f))))
      ZipLinePaths(new ZipLinePath(93, true, List(Vector3(1287.875f, 1302.571f, 94.329f), Vector3(1343.758f, 1308.594f, 110.937f))))
      ZipLinePaths(new ZipLinePath(94, true, List(Vector3(1319.908f, 1359.025f, 104.849f), Vector3(1348.35f, 1328.836f, 91.829f))))
      ZipLinePaths(new ZipLinePath(95, true, List(Vector3(1263.636f, 1349.32f, 82.93f), Vector3(1266.592f, 1341.195f, 108.67f))))
      ZipLinePaths(new ZipLinePath(96, true, List(Vector3(1273.495f, 1342.655f, 108.67f), Vector3(1269.478f, 1342.713f, 82.93f))))
      ZipLinePaths(new ZipLinePath(97, false, List(Vector3(1389.222f, 1252.343f, 106.545f), Vector3(1424.476f, 1273.113f, 103.584f))))
      ZipLinePaths(new ZipLinePath(98, false, List(Vector3(1466.484f, 1291.616f, 104.483f), Vector3(1489.982f, 1325.159f, 104.208f))))
      ZipLinePaths(new ZipLinePath(99, false, List(Vector3(1430.153f, 1430.279f, 101.695f), Vector3(1424.616f, 1393.735f, 104.651f))))
      ZipLinePaths(new ZipLinePath(100, false, List(Vector3(1419.415f, 1441.384f, 102.614f), Vector3(1380.672f, 1422.284f, 89.831f))))
      ZipLinePaths(new ZipLinePath(101, false, List(Vector3(1396.405f, 1359.469f, 104.686f), Vector3(1374.748f, 1329.47f, 103.487f))))
      ZipLinePaths(new ZipLinePath(102, false, List(Vector3(1455.862f, 1435.587f, 104.393f), Vector3(1509.459f, 1423.314f, 106.078f))))
      ZipLinePaths(new ZipLinePath(103, false, List(Vector3(1352.319f, 1302.489f, 109.54f), Vector3(1354.026f, 1299.139f, 109.058f), Vector3(1365.971f, 1275.692f, 97.974f))))
      ZipLinePaths(new ZipLinePath(104, false, List(Vector3(1318.518f, 1147.515f, 103.353f), Vector3(1357.622f, 1045.178f, 163.53f))))
      ZipLinePaths(new ZipLinePath(105, false, List(Vector3(1180.068f, 1022.799f, 98.55f), Vector3(1282.493f, 911.315f, 162.097f), Vector3(1285.797f, 907.719f, 162.64f))))
      ZipLinePaths(new ZipLinePath(106, true, List(Vector3(1172.061f, 1007.936f, 90.354f), Vector3(1160.936f, 1016.351f, 116.62f))))
      ZipLinePaths(new ZipLinePath(107, true, List(Vector3(1164.341f, 1012.921f, 116.62f), Vector3(1171.351f, 1015.839f, 90.354f))))
      ZipLinePaths(new ZipLinePath(108, true, List(Vector3(1333.537f, 845.263f, 88.13f), Vector3(1331.391f, 854.587f, 118.13f))))
      ZipLinePaths(new ZipLinePath(109, true, List(Vector3(1339.384f, 853.526f, 118.13f), Vector3(1334.989f, 854.531f, 88.13f))))
      ZipLinePaths(new ZipLinePath(110, false, List(Vector3(1019.618f, 1006.659f, 110.879f), Vector3(1051.218f, 1057.452f, 154.542f), Vector3(1053.325f, 1060.839f, 155f))))
      ZipLinePaths(new ZipLinePath(111, true, List(Vector3(975.914f, 1001.793f, 97.359f), Vector3(960.395f, 1014.087f, 116.467f))))
      ZipLinePaths(new ZipLinePath(112, true, List(Vector3(1011.91f, 1014.587f, 110.379f), Vector3(979.003f, 1067.291f, 99.859f))))
      ZipLinePaths(new ZipLinePath(113, true, List(Vector3(1044.776f, 974.04f, 84.33f), Vector3(1042.263f, 974.486f, 101.501f), Vector3(1035.728f, 975.644f, 101.851f))))
      ZipLinePaths(new ZipLinePath(114, true, List(Vector3(1028.874f, 972.119f, 101.843f), Vector3(1033.871f, 975.957f, 84.33f))))
      ZipLinePaths(new ZipLinePath(115, false, List(Vector3(950.294f, 1010.662f, 115.049f), Vector3(907.387f, 1011.592f, 104.498f))))
      ZipLinePaths(new ZipLinePath(116, false, List(Vector3(932.836f, 983.518f, 107.184f), Vector3(958.721f, 980.339f, 109.018f))))
      ZipLinePaths(new ZipLinePath(117, false, List(Vector3(859.555f, 856.841f, 108.282f), Vector3(861.234f, 860.752f, 107.928f), Vector3(872.988f, 888.128f, 100.746f))))
      ZipLinePaths(new ZipLinePath(118, false, List(Vector3(823.748f, 908.416f, 106.055f), Vector3(829.011f, 907.394f, 105.693f), Vector3(844.8f, 904.328f, 97.942f))))
      ZipLinePaths(new ZipLinePath(119, false, List(Vector3(847.683f, 942.238f, 106.066f), Vector3(863.593f, 915.671f, 104.714f))))
      ZipLinePaths(new ZipLinePath(120, false, List(Vector3(880.683f, 916.696f, 107.418f), Vector3(904.488f, 954.484f, 101.787f))))
      ZipLinePaths(new ZipLinePath(121, false, List(Vector3(879.234f, 989.582f, 103.583f), Vector3(837.231f, 952.58f, 106.075f))))
      ZipLinePaths(new ZipLinePath(122, false, List(Vector3(871.672f, 905.395f, 105.726f), Vector3(823.879f, 892.325f, 112.638f), Vector3(802.849f, 886.573f, 116.464f))))
      ZipLinePaths(new ZipLinePath(123, false, List(Vector3(769.124f, 986.574f, 106.143f), Vector3(781.102f, 955.846f, 106.11f))))
      ZipLinePaths(new ZipLinePath(124, false, List(Vector3(725.343f, 990.882f, 107.057f), Vector3(692.244f, 962.514f, 132.431f), Vector3(690.038f, 960.623f, 132.964f))))
      ZipLinePaths(new ZipLinePath(125, false, List(Vector3(778.287f, 866.552f, 124.271f), Vector3(691.082f, 941.073f, 133.163f))))
      ZipLinePaths(new ZipLinePath(126, false, List(Vector3(1489.014f, 1467.955f, 106.115f), Vector3(1460.493f, 1455.135f, 101.698f))))
      ZipLinePaths(new ZipLinePath(127, false, List(Vector3(1483.446f, 1325.031f, 100.653f), Vector3(1458.632f, 1338.057f, 104.735f))))
      ZipLinePaths(new ZipLinePath(128, false, List(Vector3(1471.073f, 1301.982f, 95f), Vector3(1485.411f, 1312.821f, 94.284f))))
      ZipLinePaths(new ZipLinePath(129, false, List(Vector3(755.07f, 1030.528f, 96.662f), Vector3(816.714f, 1025.964f, 91.662f))))
      ZipLinePaths(new ZipLinePath(130, false, List(Vector3(777.204f, 996.672f, 107.073f), Vector3(820.392f, 1000.457f, 91.69f))))
      ZipLinePaths(new ZipLinePath(131, true, List(Vector3(790.674f, 923.262f, 103.779f), Vector3(784.461f, 914.388f, 130.332f))))
      ZipLinePaths(new ZipLinePath(132, true, List(Vector3(782.729f, 908.18f, 130.323f), Vector3(789.14f, 914.457f, 103.779f))))
      ZipLinePaths(new ZipLinePath(133, true, List(Vector3(910.317f, 1345.869f, 86.23f), Vector3(906.549f, 1335.888f, 107.892f))))
      ZipLinePaths(new ZipLinePath(134, true, List(Vector3(908.223f, 1341.816f, 107.892f), Vector3(905.648f, 1338.911f, 86.23f))))
      ZipLinePaths(new ZipLinePath(135, false, List(Vector3(833.232f, 1393.407f, 121.577f), Vector3(915.637f, 1438.261f, 116.831f))))
      ZipLinePaths(new ZipLinePath(136, false, List(Vector3(982.46f, 1518.909f, 107.592f), Vector3(917.577f, 1450.059f, 116.832f))))
      ZipLinePaths(new ZipLinePath(137, true, List(Vector3(924.848f, 1451.53f, 86.33f), Vector3(915.991f, 1444.015f, 116.33f))))
      ZipLinePaths(new ZipLinePath(138, true, List(Vector3(920.928f, 1445.093f, 116.33f), Vector3(919.855f, 1444.008f, 115.98f), Vector3(916.421f, 1442.794f, 86.33f))))
      ZipLinePaths(new ZipLinePath(139, true, List(Vector3(828.811f, 1428.899f, 90.331f), Vector3(811.334f, 1398.369f, 121.076f))))
      ZipLinePaths(new ZipLinePath(140, false, List(Vector3(936.083f, 1393.338f, 95.643f), Vector3(1026.755f, 1400.969f, 95.813f))))
      ZipLinePaths(new ZipLinePath(141, false, List(Vector3(856.993f, 1184.88f, 100.512f), Vector3(839.109f, 1250.231f, 106.497f))))
      ZipLinePaths(new ZipLinePath(142, true, List(Vector3(1093.28f, 1376.753f, 90.63f), Vector3(1091.116f, 1365.163f, 122.091f))))
      ZipLinePaths(new ZipLinePath(143, true, List(Vector3(1096.055f, 1369.18f, 122.091f), Vector3(1093.978f, 1367.448f, 90.63f))))
      ZipLinePaths(new ZipLinePath(144, false, List(Vector3(1084.243f, 1366.022f, 122.611f), Vector3(1033.715f, 1403.167f, 162.301f), Vector3(1026.497f, 1408.473f, 162.841f))))
      ZipLinePaths(new ZipLinePath(145, false, List(Vector3(956.997f, 1231.56f, 109.347f), Vector3(954.537f, 1320.416f, 163.139f), Vector3(954.457f, 1323.283f, 163.636f))))
      ZipLinePaths(new ZipLinePath(146, false, List(Vector3(1064.878f, 1173.023f, 154.9f), Vector3(1089.589f, 1126.197f, 155f))))
      ZipLinePaths(new ZipLinePath(147, false, List(Vector3(1006.476f, 1222.634f, 109.337f), Vector3(1024.688f, 1177.964f, 165.896f), Vector3(1025.902f, 1174.985f, 166.36f))))
      ZipLinePaths(new ZipLinePath(148, false, List(Vector3(1086.897f, 1315.525f, 96.781f), Vector3(1084.806f, 1209.158f, 166.286f), Vector3(1084.739f, 1205.727f, 166.361f))))
      ZipLinePaths(new ZipLinePath(149, false, List(Vector3(1305.024f, 1142.624f, 102.434f), Vector3(1251.996f, 1106.28f, 172.469f), Vector3(1248.461f, 1103.857f, 172.979f))))
      ZipLinePaths(new ZipLinePath(150, false, List(Vector3(1161.901f, 1023.071f, 117.15f), Vector3(1094.77f, 1099.419f, 153.244f))))
      ZipLinePaths(new ZipLinePath(151, true, List(Vector3(889.775f, 1487.223f, 91.186f), Vector3(869.803f, 1471.874f, 101.23f))))
      ZipLinePaths(new ZipLinePath(152, true, List(Vector3(889.786f, 1463.643f, 101.23f), Vector3(875.143f, 1455.936f, 91.186f))))
      ZipLinePaths(new ZipLinePath(153, true, List(Vector3(1415.815f, 847.147f, 92.086f), Vector3(1400.802f, 839.966f, 102.13f))))
      ZipLinePaths(new ZipLinePath(154, true, List(Vector3(1406.021f, 860.572f, 102.13f), Vector3(1383.189f, 857.817f, 92.086f))))
      ZipLinePaths(new ZipLinePath(155, false, List(Vector3(1043.41f, 408.428f, 103.135f), Vector3(1034.259f, 441.256f, 107.824f), Vector3(1003.909f, 461.383f, 108.513f), Vector3(964.41f, 480.238f, 108.113f), Vector3(932.811f, 494.262f, 103.135f))))
      ZipLinePaths(new ZipLinePath(156, false, List(Vector3(879.653f, 506.603f, 103.138f), Vector3(877.332f, 531.712f, 105.171f), Vector3(877.61f, 556.821f, 91.207f), Vector3(878.967f, 627.04f, 92.694f), Vector3(885.924f, 657.259f, 88.68f), Vector3(904.345f, 693.461f, 83.138f))))
      ZipLinePaths(new ZipLinePath(157, false, List(Vector3(945.611f, 828.543f, 94.343f), Vector3(969.089f, 916.077f, 164.71f))))
      ZipLinePaths(new ZipLinePath(158, false, List(Vector3(251.005f, 1006.488f, 83.141f), Vector3(278.893f, 1007.049f, 97.949f), Vector3(317.358f, 1008.267f, 109.649f), Vector3(355.824f, 986.086f, 105.349f), Vector3(376.98f, 977.201f, 103.141f))))
      ZipLinePaths(new ZipLinePath(159, false, List(Vector3(375.611f, 1014.701f, 103.149f), Vector3(381.423f, 1010.204f, 105.862f), Vector3(447.637f, 1008.626f, 90.749f), Vector3(461.244f, 1020.038f, 83.149f))))
      ZipLinePaths(new ZipLinePath(160, false, List(Vector3(545.645f, 1102.242f, 83.116f), Vector3(596.275f, 1099.627f, 84.612f), Vector3(644.906f, 1119.313f, 85.692f), Vector3(660.903f, 1127.65f, 83.135f))))
      ZipLinePaths(new ZipLinePath(161, false, List(Vector3(902.807f, 1481.246f, 101.73f), Vector3(986.877f, 1529.592f, 102.592f))))
      ZipLinePaths(new ZipLinePath(162, false, List(Vector3(814.772f, 1175.312f, 100.51f), Vector3(781.371f, 1305.043f, 103.731f))))
      ZipLinePaths(new ZipLinePath(163, false, List(Vector3(1420.106f, 1047.818f, 98.511f), Vector3(1478.651f, 956.067f, 104.985f))))
      ZipLinePaths(new ZipLinePath(164, false, List(Vector3(1177.881f, 1157.518f, 111.616f), Vector3(1274.135f, 1154.874f, 92.959f))))
      ZipLinePaths(new ZipLinePath(165, true, List(Vector3(1151.406f, 1200.873f, 82.467f), Vector3(1155.773f, 1201.314f, 105.98f))))
      ZipLinePaths(new ZipLinePath(166, false, List(Vector3(1100.745f, 333.019f, 103.177f), Vector3(1120.674f, 333.769f, 106.796f), Vector3(1180.687f, 333.911f, 91.323f), Vector3(1212.362f, 344.543f, 87.2f), Vector3(1232.066f, 378.678f, 83.094f))))
      ZipLinePaths(new ZipLinePath(167, false, List(Vector3(1242.328f, 432.765f, 83.142f), Vector3(1257.104f, 433.065f, 86.714f), Vector3(1292.681f, 433.366f, 72.293f), Vector3(1343.033f, 433.967f, 68.738f), Vector3(1367.716f, 434.261f, 63.142f))))
      ZipLinePaths(new ZipLinePath(168, false, List(Vector3(1424.5f, 440.843f, 63.137f), Vector3(1426.096f, 511.412f, 67.62f), Vector3(1456.893f, 569.707f, 68.225f), Vector3(1459.384f, 625.83f, 79.208f))))
      ZipLinePaths(new ZipLinePath(169, false, List(Vector3(2056.6f, 1425.645f, 103.14f), Vector3(1983.076f, 1426.488f, 105.286f), Vector3(1957.552f, 1427.33f, 117.037f), Vector3(1931.8f, 1427.768f, 120.163f))))
      ZipLinePaths(new ZipLinePath(170, false, List(Vector3(1918.56f, 1424.729f, 123.115f), Vector3(1853.468f, 1394.922f, 125.845f), Vector3(1787.707f, 1392.389f, 103.141f))))
      ZipLinePaths(new ZipLinePath(171, false, List(Vector3(1777.914f, 1349.613f, 103.138f), Vector3(1778.386f, 1335.218f, 106.052f), Vector3(1778.857f, 1283.823f, 87.669f), Vector3(1746.873f, 1217.987f, 86.395f), Vector3(1746.738f, 1162.087f, 83.138f))))
      ZipLinePaths(new ZipLinePath(172, false, List(Vector3(1701.419f, 1137.503f, 83.135f), Vector3(1651.437f, 1138.699f, 85.328f), Vector3(1645.439f, 1138.843f, 83.135f))))
      ZipLinePaths(new ZipLinePath(173, false, List(Vector3(1045.419f, 1970.538f, 103.14f), Vector3(1120.109f, 1970.619f, 106.09f), Vector3(1146.398f, 1970.7f, 117.88f), Vector3(1173.128f, 1970.743f, 121.443f))))
      ZipLinePaths(new ZipLinePath(174, false, List(Vector3(1208.467f, 1895.413f, 123.148f), Vector3(1247.813f, 1870.331f, 125.945f), Vector3(1310.558f, 1869.625f, 109.069f), Vector3(1349.94f, 1869.559f, 95.826f), Vector3(1369.513f, 1870.568f, 84.545f))))
      ZipLinePaths(new ZipLinePath(175, false, List(Vector3(1421.55f, 1866.085f, 83.138f), Vector3(1458.246f, 1791.885f, 86.285f), Vector3(1457.935f, 1731.766f, 68.567f), Vector3(1459.208f, 1682.72f, 63.138f))))
      ZipLinePaths(new ZipLinePath(176, false, List(Vector3(948.021f, 2065.12f, 103.188f), Vector3(866.812f, 2065.721f, 128.666f), Vector3(816.304f, 2066.094f, 126.726f), Vector3(809.371f, 2066.146f, 123.14f))))
      ZipLinePaths(new ZipLinePath(177, false, List(Vector3(785.86f, 2020.291f, 123.141f), Vector3(787.358f, 1953.837f, 126.085f), Vector3(787.114f, 1889.222f, 106.325f), Vector3(755.214f, 1825.132f, 106.961f), Vector3(754.919f, 1770.084f, 85.079f))))
      ZipLinePaths(new ZipLinePath(178, false, List(Vector3(727.728f, 1734.672f, 83.134f), Vector3(747.409f, 1715.699f, 84.587f), Vector3(770.091f, 1709.127f, 86.541f), Vector3(834.453f, 1676.582f, 85.648f), Vector3(845.365f, 1664.437f, 87.156f), Vector3(845.236f, 1613.224f, 83.135f))))
      ZipLinePaths(new ZipLinePath(179, false, List(Vector3(1176.424f, 1629.973f, 143.358f), Vector3(1176.427f, 1641.788f, 140.92f), Vector3(1176.43f, 1679.603f, 128.496f), Vector3(1176.434f, 1713.663f, 123.333f))))
      ZipLinePaths(new ZipLinePath(180, false, List(Vector3(1176.147f, 1746.984f, 132.155f), Vector3(1175.973f, 1795.737f, 117.847f), Vector3(1175.949f, 1802.428f, 113.756f))))
      ZipLinePaths(new ZipLinePath(181, false, List(Vector3(1199.978f, 1815.422f, 118.22f), Vector3(1205.888f, 1813.125f, 118.397f), Vector3(1213.843f, 1799.985f, 118.398f), Vector3(1243.82f, 1799.915f, 111.399f), Vector3(1258.809f, 1799.88f, 106.563f), Vector3(1273.798f, 1799.845f, 98.843f))))
      ZipLinePaths(new ZipLinePath(182, false, List(Vector3(1324.259f, 1783.597f, 98.484f), Vector3(1332.029f, 1782.574f, 98.551f), Vector3(1341.288f, 1768.443f, 98.735f), Vector3(1355.698f, 1767.96f, 95.7f), Vector3(1370.107f, 1767.476f, 91.066f), Vector3(1398.927f, 1766.51f, 83.897f), Vector3(1399.036f, 1754.426f, 82.312f), Vector3(1399.146f, 1734.343f, 75.628f), Vector3(1399.255f, 1718.259f, 71.043f), Vector3(1399.365f, 1702.176f, 63.859f))))
      ZipLinePaths(new ZipLinePath(183, false, List(Vector3(1153.673f, 1816.245f, 108.336f), Vector3(1115.768f, 1816.778f, 98.537f))))
      ZipLinePaths(new ZipLinePath(184, false, List(Vector3(1096.022f, 1848.979f, 98.955f), Vector3(1097.069f, 1893.221f, 98.075f), Vector3(1134.716f, 1896.163f, 108.395f), Vector3(1171.409f, 1895.447f, 119.434f), Vector3(1177.339f, 1895.326f, 119.74f))))
      ZipLinePaths(new ZipLinePath(185, false, List(Vector3(1081.349f, 1837.281f, 103.218f), Vector3(1076.7f, 1845.944f, 103.449f), Vector3(1065.451f, 1850.307f, 103.181f), Vector3(1064.126f, 1871.715f, 108.399f), Vector3(1063.133f, 1910.11f, 98.08f), Vector3(1049.936f, 1910.607f, 97.92f), Vector3(1046.238f, 1897.155f, 97.84f), Vector3(1035.789f, 1897.13f, 97.8f), Vector3(1033.74f, 1905.104f, 98.261f))))
      ZipLinePaths(new ZipLinePath(186, false, List(Vector3(1328.07f, 680.513f, 143.204f), Vector3(1314.583f, 679.882f, 144.113f), Vector3(1301.096f, 679.251f, 143.222f))))
      ZipLinePaths(new ZipLinePath(187, false, List(Vector3(1298.966f, 663.593f, 143.212f), Vector3(1334.459f, 661.413f, 143.449f), Vector3(1335.652f, 596.233f, 144.186f), Vector3(1335.731f, 589.947f, 143.346f))))
      ZipLinePaths(new ZipLinePath(188, false, List(Vector3(1325.227f, 568.298f, 148.411f), Vector3(1312.914f, 568.05f, 153.289f), Vector3(1256.247f, 566.409f, 138.091f), Vector3(1256.38f, 556.749f, 137.691f), Vector3(1256.514f, 547.089f, 134.392f), Vector3(1256.78f, 527.769f, 128.209f))))
      ZipLinePaths(new ZipLinePath(189, false, List(Vector3(1232.488f, 503.972f, 118.271f), Vector3(1177.355f, 503.447f, 103.257f), Vector3(1177.288f, 493.906f, 103.122f), Vector3(1177.221f, 484.365f, 100.387f), Vector3(1177.088f, 465.284f, 94.516f), Vector3(1176.822f, 427.121f, 83.308f))))
      ZipLinePaths(new ZipLinePath(190, true, List(Vector3(1460.291f, 635.074f, 82.052f), Vector3(1420.606f, 658.542f, 103.255f), Vector3(1348.379f, 701.254f, 155.5f), Vector3(1338.461f, 711.356f, 142.652f))))
      ZipLinePaths(new ZipLinePath(191, true, List(Vector3(1460.121f, 1669.773f, 62.635f), Vector3(1462.283f, 1665.474f, 65.87f), Vector3(1507.263f, 1576.061f, 93.342f), Vector3(1529.32f, 1532.214f, 107.198f), Vector3(1550.945f, 1489.227f, 120.782f), Vector3(1572.57f, 1446.24f, 134.366f), Vector3(1583.382f, 1424.747f, 142.313f), Vector3(1585.577f, 1418.788f, 142.568f))))
      ZipLinePaths(new ZipLinePath(192, true, List(Vector3(843.171f, 1604.511f, 82.635f), Vector3(1081.584f, 1606.379f, 130.495f), Vector3(1132.6f, 1606.779f, 140.524f), Vector3(1173.225f, 1605.795f, 147.478f))))
      ZipLinePaths(new ZipLinePath(193, false, List(Vector3(1614.569f, 1415.707f, 138.328f), Vector3(1626.72f, 1415.726f, 136.133f), Vector3(1638.871f, 1415.745f, 131.063f), Vector3(1663.172f, 1415.782f, 124.323f), Vector3(1711.775f, 1415.857f, 108.883f), Vector3(1721.305f, 1415.871f, 103.923f))))
      ZipLinePaths(new ZipLinePath(194, false, List(Vector3(1735.537f, 1404.107f, 103.196f), Vector3(1721.896f, 1399.206f, 103.634f), Vector3(1721.852f, 1370.805f, 103.243f))))
      ZipLinePaths(new ZipLinePath(195, false, List(Vector3(658.194f, 952.284f, 128.38f), Vector3(652.236f, 952.266f, 127.377f), Vector3(646.278f, 952.248f, 125.698f), Vector3(634.363f, 952.212f, 122.04f), Vector3(610.532f, 952.14f, 114.424f), Vector3(562.869f, 951.997f, 99.305f), Vector3(540.468f, 951.929f, 92.103f), Vector3(529.267f, 951.896f, 89.102f), Vector3(518.067f, 951.862f, 83.825f))))
      ZipLinePaths(new ZipLinePath(196, false, List(Vector3(503.81f, 962.852f, 83.211f), Vector3(518.406f, 970.708f, 83.699f), Vector3(518.408f, 994.66f, 83.232f))))
      ZipLinePaths(new ZipLinePath(197, false, List(Vector3(492.349f, 1008.389f, 83.135f), Vector3(511.868f, 1054.627f, 86.076f), Vector3(526.104f, 1080.919f, 83.135f))))
      ZipLinePaths(new ZipLinePath(198, false, List(Vector3(911.833f, 714.846f, 82.993f), Vector3(936.165f, 763.965f, 96.014f), Vector3(954.414f, 800.804f, 98.783f), Vector3(960.497f, 813.084f, 98.139f))))
      ZipLinePaths(new ZipLinePath(199, false, List(Vector3(676.057f, 1134.606f, 83.178f), Vector3(725.964f, 1144.654f, 93.349f), Vector3(775.871f, 1154.702f, 95.839f), Vector3(805.229f, 1160.613f, 93.832f))))
      ZipLinePaths(new ZipLinePath(200, false, List(Vector3(1459.819f, 1651.959f, 64.416f), Vector3(1456.288f, 1610.835f, 86.097f), Vector3(1453.64f, 1579.993f, 91.127f), Vector3(1452.757f, 1569.712f, 90.906f))))
      ZipLinePaths(new ZipLinePath(201, false, List(Vector3(1458.457f, 1545.826f, 90.791f), Vector3(1479.788f, 1501.533f, 100.359f), Vector3(1492.161f, 1475.843f, 106.12f))))
      ZipLinePaths(new ZipLinePath(202, false, List(Vector3(1444.372f, 1548.139f, 90.82f), Vector3(1432.067f, 1503.254f, 110.53f), Vector3(1419.763f, 1458.368f, 128.818f), Vector3(1407.458f, 1413.483f, 147.106f), Vector3(1399.337f, 1383.859f, 159.624f))))
      ZipLinePaths(new ZipLinePath(203, false, List(Vector3(1551.372f, 1446.798f, 106.066f), Vector3(1575.322f, 1428.6f, 142.446f), Vector3(1578.744f, 1426.001f, 143.072f))))
      ZipLinePaths(new ZipLinePath(204, false, List(Vector3(1224.112f, 1046.429f, 98.511f), Vector3(1258.135f, 1084.269f, 96.285f), Vector3(1286.821f, 1116.172f, 92.591f))))
      ZipLinePaths(new ZipLinePath(205, false, List(Vector3(841.821f, 1278.922f, 97.951f), Vector3(915.183f, 1336.824f, 163.083f), Vector3(917.549f, 1338.692f, 163.636f))))
      ZipLinePaths(new ZipLinePath(206, false, List(Vector3(878.453f, 1386.561f, 97.851f), Vector3(955.117f, 1405.787f, 162.33f), Vector3(960.228f, 1407.069f, 162.789f))))
      ZipLinePaths(new ZipLinePath(207, false, List(Vector3(1526.881f, 964.484f, 92.484f), Vector3(1400.135f, 1008.218f, 163.631f), Vector3(1394.22f, 1010.259f, 163.564f))))
      ZipLinePaths(new ZipLinePath(208, false, List(Vector3(1435.638f, 937.309f, 94.831f), Vector3(1340.67f, 920.751f, 162.022f), Vector3(1334.338f, 919.647f, 162.236f))))
      ZipLinePaths(new ZipLinePath(209, false, List(Vector3(1043.013f, 1001.463f, 94.331f), Vector3(1059.675f, 1063.189f, 154.808f), Vector3(1060.786f, 1067.304f, 155f))))
      ZipLinePaths(new ZipLinePath(210, false, List(Vector3(1262.21f, 1329.985f, 87.831f), Vector3(1278.973f, 1277.129f, 159.708f), Vector3(1281.368f, 1269.578f, 160.491f))))
      ZipLinePaths(new ZipLinePath(211, false, List(Vector3(1310.729f, 1248.042f, 87.731f), Vector3(1351.306f, 1316.913f, 159.718f), Vector3(1352.615f, 1319.135f, 160.182f))))
      ZipLinePaths(new ZipLinePath(212, false, List(Vector3(922.83f, 1034.618f, 94.431f), Vector3(946.139f, 947.914f, 164.469f), Vector3(946.891f, 945.117f, 164.767f))))
      ZipLinePaths(new ZipLinePath(213, false, List(Vector3(981.12f, 1104.231f, 94.331f), Vector3(873.547f, 1056.63f, 176.978f), Vector3(870.077f, 1055.095f, 176.961f))))
      ZipLinePaths(new ZipLinePath(214, false, List(Vector3(1369.892f, 1371.893f, 87.431f), Vector3(1410.201f, 1283.777f, 174.098f), Vector3(1411.501f, 1280.935f, 174.004f))))
      ZipLinePaths(new ZipLinePath(215, false, List(Vector3(1624.058f, 1140.65f, 82.998f), Vector3(1575.346f, 1141.37f, 98.857f), Vector3(1545.144f, 1141.817f, 101.83f))))
      ZipLinePaths(new ZipLinePath(216, false, List(Vector3(1514.803f, 1155.469f, 95.704f), Vector3(1441.047f, 1240.007f, 174.127f), Vector3(1438.668f, 1242.734f, 174.386f))))
      ZipLinePaths(new ZipLinePath(217, false, List(Vector3(1476.13f, 1135.492f, 101.86f), Vector3(1414.559f, 1079.969f, 98.51f))))
      ZipLinePaths(new ZipLinePath(218, true, List(Vector3(905.255f, 699.031f, 82.635f), Vector3(903.263f, 701.231f, 84.745f), Vector3(858.767f, 750.357f, 93.63f), Vector3(854.782f, 754.756f, 97.496f), Vector3(853.454f, 756.222f, 100.281f), Vector3(818.919f, 794.349f, 107.871f), Vector3(785.049f, 831.744f, 115.315f), Vector3(751.179f, 869.137f, 122.759f), Vector3(717.309f, 906.531f, 130.203f), Vector3(683.438f, 943.925f, 137.646f), Vector3(679.089f, 949.558f, 132.684f))))
      ZipLinePaths(new ZipLinePath(219, true, List(Vector3(1165.727f, 1602.266f, 147.746f), Vector3(1116.728f, 1601.143f, 139.366f), Vector3(886.441f, 1595.865f, 129.447f), Vector3(843.985f, 1594.922f, 82.706f))))
      ZipLinePaths(new ZipLinePath(220, false, List(Vector3(1453.183f, 659.325f, 83.215f), Vector3(1437.37f, 707.601f, 91.141f), Vector3(1421.557f, 755.877f, 95.635f), Vector3(1405.744f, 804.154f, 100.129f), Vector3(1395.202f, 836.338f, 102.633f))))
      ZipLinePaths(new ZipLinePath(221, false, List(Vector3(845.714f, 1588.779f, 82.734f), Vector3(855.47f, 1540.323f, 95.6f), Vector3(865.227f, 1491.868f, 102.057f), Vector3(867.568f, 1480.238f, 101.737f))))
      ZipLinePaths(new ZipLinePath(222, true, List(Vector3(686.309f, 945.118f, 132.633f), Vector3(720.441f, 907.992f, 125.999f), Vector3(754.573f, 870.865f, 118.411f), Vector3(776.658f, 846.842f, 120.555f), Vector3(811.459f, 808.988f, 112.818f), Vector3(845.591f, 771.862f, 105.23f), Vector3(908.97f, 709.205f, 82.552f))))
      ZipLinePaths(new ZipLinePath(223, true, List(Vector3(1344.796f, 718.476f, 142.725f), Vector3(1411.682f, 675.619f, 123.587f), Vector3(1450.89f, 650.495f, 102.795f), Vector3(1457.81f, 646.062f, 82.762f))))
      ZipLinePaths(new ZipLinePath(224, true, List(Vector3(1585.758f, 1426.108f, 142.747f), Vector3(1562.474f, 1469.058f, 129.343f), Vector3(1539.19f, 1512.008f, 114.71f), Vector3(1515.907f, 1554.959f, 100.077f), Vector3(1459.865f, 1658.76f, 63.264f))))
      ZipLinePaths(new ZipLinePath(225, false, List(Vector3(978.333f, 1281.95f, 109.419f), Vector3(998.278f, 1375.536f, 162.879f), Vector3(999.608f, 1381.775f, 162.859f))))
      ZipLinePaths(new ZipLinePath(226, false, List(Vector3(1323.318f, 999.538f, 108.132f), Vector3(1352.668f, 1032.694f, 163.149f), Vector3(1354.625f, 1034.905f, 163.51f))))
      ZipLinePaths(new ZipLinePath(227, false, List(Vector3(1416.023f, 1100.672f, 96.11f), Vector3(1396.498f, 1232.051f, 169.279f), Vector3(1395.869f, 1236.289f, 168.543f))))
      ZipLinePaths(new ZipLinePath(228, false, List(Vector3(1355.816f, 1040.459f, 163.511f), Vector3(1246.45f, 1095.878f, 172.978f))))
      ZipLinePaths(new ZipLinePath(229, false, List(Vector3(1363.911f, 1075.694f, 96.074f), Vector3(1252.925f, 1098.509f, 173.077f), Vector3(1249.345f, 1099.245f, 172.98f))))
      ZipLinePaths(new ZipLinePath(230, false, List(Vector3(1296.075f, 1036.364f, 96.131f), Vector3(1246.252f, 1040.264f, 97.856f), Vector3(1227.319f, 1041.746f, 98.51f))))
      ZipLinePaths(new ZipLinePath(231, false, List(Vector3(1030.411f, 1318.939f, 111.42f), Vector3(1028.982f, 1397.782f, 161.917f), Vector3(1028.886f, 1403.039f, 162.832f))))
      ZipLinePaths(new ZipLinePath(232, false, List(Vector3(1346.985f, 726.494f, 143.072f), Vector3(1377.341f, 766.739f, 135.821f), Vector3(1407.697f, 806.984f, 128.102f), Vector3(1438.053f, 847.229f, 120.384f), Vector3(1458.291f, 874.06f, 115.551f))))
      ZipLinePaths(new ZipLinePath(233, false, List(Vector3(1342.67f, 730.091f, 142.989f), Vector3(1344.678f, 735.667f, 142.661f), Vector3(1374.8f, 819.304f, 99.518f), Vector3(1379.39f, 832.049f, 88.689f))))
      ZipLinePaths(new ZipLinePath(234, false, List(Vector3(869.078f, 1467.295f, 101.744f), Vector3(840.171f, 1428.178f, 116.211f), Vector3(830.054f, 1414.486f, 120.844f), Vector3(824.996f, 1407.641f, 121.96f), Vector3(819.937f, 1400.795f, 121.59f))))
    }

    ZipLines()

  }
}
