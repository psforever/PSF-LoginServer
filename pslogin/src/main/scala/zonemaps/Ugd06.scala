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

object Ugd06 { // Drugaskan
  val ZoneMap = new ZoneMap("ugd06") {
    Scale = MapScale.Dim2560
    Cavern = true
    Checksum = 4274683970L

    Building10077()

    def Building10077(): Unit = { // Name: ceiling_bldg_a_10077 Type: ceiling_bldg_a GUID: 1, MapID: 10077
      LocalBuilding("ceiling_bldg_a_10077", 1, 10077, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1260.11f, 1049.16f, 170.44f), ceiling_bldg_a)))
      LocalObject(666, Door.Constructor(Vector3(1241.352f, 1050.759f, 177.725f)), owning_building_guid = 1)
      LocalObject(668, Door.Constructor(Vector3(1247.792f, 1037.129f, 172.219f)), owning_building_guid = 1)
      LocalObject(669, Door.Constructor(Vector3(1253.713f, 1030.187f, 177.725f)), owning_building_guid = 1)
      LocalObject(676, Door.Constructor(Vector3(1276.061f, 1049.765f, 172.219f)), owning_building_guid = 1)
    }

    Building10079()

    def Building10079(): Unit = { // Name: ceiling_bldg_b_10079 Type: ceiling_bldg_b GUID: 2, MapID: 10079
      LocalBuilding("ceiling_bldg_b_10079", 2, 10079, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1014.06f, 873.97f, 162.94f), ceiling_bldg_b)))
      LocalObject(608, Door.Constructor(Vector3(997.8083f, 877.4145f, 164.719f)), owning_building_guid = 2)
      LocalObject(611, Door.Constructor(Vector3(1015.59f, 879.8751f, 164.719f)), owning_building_guid = 2)
    }

    Building10241()

    def Building10241(): Unit = { // Name: ceiling_bldg_c_10241 Type: ceiling_bldg_c GUID: 3, MapID: 10241
      LocalBuilding("ceiling_bldg_c_10241", 3, 10241, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1125.87f, 1035.29f, 171.46f), ceiling_bldg_c)))
      LocalObject(638, Door.Constructor(Vector3(1122.978f, 1038.418f, 173.239f)), owning_building_guid = 3)
      LocalObject(649, Door.Constructor(Vector3(1161.197f, 1070.605f, 173.239f)), owning_building_guid = 3)
      LocalObject(651, Door.Constructor(Vector3(1172.81f, 1041.863f, 173.239f)), owning_building_guid = 3)
    }

    Building10006()

    def Building10006(): Unit = { // Name: ceiling_bldg_d_10006 Type: ceiling_bldg_d GUID: 4, MapID: 10006
      LocalBuilding("ceiling_bldg_d_10006", 4, 10006, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1196.16f, 965.32f, 165.44f), ceiling_bldg_d)))
      LocalObject(653, Door.Constructor(Vector3(1182.001f, 955.0526f, 167.175f)), owning_building_guid = 4)
      LocalObject(656, Door.Constructor(Vector3(1185.893f, 979.4791f, 167.175f)), owning_building_guid = 4)
      LocalObject(659, Door.Constructor(Vector3(1206.465f, 951.1635f, 167.175f)), owning_building_guid = 4)
      LocalObject(660, Door.Constructor(Vector3(1210.317f, 975.6251f, 167.175f)), owning_building_guid = 4)
      LocalObject(729, Painbox.Constructor(Vector3(1196.204f, 965.1011f, 173.748f), painbox_continuous), owning_building_guid = 4)
    }

    Building10081()

    def Building10081(): Unit = { // Name: ceiling_bldg_e_10081 Type: ceiling_bldg_e GUID: 5, MapID: 10081
      LocalBuilding("ceiling_bldg_e_10081", 5, 10081, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1128.06f, 914.38f, 179.45f), ceiling_bldg_e)))
      LocalObject(635, Door.Constructor(Vector3(1108.343f, 940.5333f, 186.729f)), owning_building_guid = 5)
      LocalObject(636, Door.Constructor(Vector3(1120.104f, 905.3209f, 181.229f)), owning_building_guid = 5)
      LocalObject(637, Door.Constructor(Vector3(1122.273f, 947.7385f, 181.229f)), owning_building_guid = 5)
      LocalObject(642, Door.Constructor(Vector3(1140.482f, 937.5197f, 186.729f)), owning_building_guid = 5)
    }

    Building10080()

    def Building10080(): Unit = { // Name: ceiling_bldg_f_10080 Type: ceiling_bldg_f GUID: 6, MapID: 10080
      LocalBuilding("ceiling_bldg_f_10080", 6, 10080, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1059.44f, 1115.66f, 174.49f), ceiling_bldg_f)))
      LocalObject(618, Door.Constructor(Vector3(1028.562f, 1113.933f, 176.269f)), owning_building_guid = 6)
      LocalObject(633, Door.Constructor(Vector3(1086.384f, 1106.004f, 176.269f)), owning_building_guid = 6)
    }

    Building10078()

    def Building10078(): Unit = { // Name: ceiling_bldg_g_10078 Type: ceiling_bldg_g GUID: 7, MapID: 10078
      LocalBuilding("ceiling_bldg_g_10078", 7, 10078, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(828.34f, 1023.68f, 163.71f), ceiling_bldg_g)))
      LocalObject(565, Door.Constructor(Vector3(820.356f, 1007.17f, 165.489f)), owning_building_guid = 7)
      LocalObject(567, Door.Constructor(Vector3(828.356f, 1041.17f, 165.489f)), owning_building_guid = 7)
    }

    Building10007()

    def Building10007(): Unit = { // Name: ceiling_bldg_h_10007 Type: ceiling_bldg_h GUID: 8, MapID: 10007
      LocalBuilding("ceiling_bldg_h_10007", 8, 10007, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(904.92f, 964.1f, 173.54f), ceiling_bldg_h)))
      LocalObject(581, Door.Constructor(Vector3(888.43f, 960.116f, 175.319f)), owning_building_guid = 8)
      LocalObject(591, Door.Constructor(Vector3(908.904f, 980.61f, 175.319f)), owning_building_guid = 8)
      LocalObject(593, Door.Constructor(Vector3(916.005f, 952.912f, 177.819f)), owning_building_guid = 8)
    }

    Building10008()

    def Building10008(): Unit = { // Name: ceiling_bldg_i_10008 Type: ceiling_bldg_i GUID: 9, MapID: 10008
      LocalBuilding("ceiling_bldg_i_10008", 9, 10008, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(971.61f, 1193.75f, 179.41f), ceiling_bldg_i)))
      LocalObject(597, Door.Constructor(Vector3(946.8376f, 1186.718f, 181.189f)), owning_building_guid = 9)
      LocalObject(607, Door.Constructor(Vector3(996.351f, 1193.677f, 181.189f)), owning_building_guid = 9)
    }

    Building10238()

    def Building10238(): Unit = { // Name: ceiling_bldg_z_10238 Type: ceiling_bldg_z GUID: 10, MapID: 10238
      LocalBuilding("ceiling_bldg_z_10238", 10, 10238, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(925.94f, 1113.67f, 188.72f), ceiling_bldg_z)))
      LocalObject(590, Door.Constructor(Vector3(904.9026f, 1088.561f, 190.499f)), owning_building_guid = 10)
      LocalObject(596, Door.Constructor(Vector3(946.5898f, 1127.435f, 190.499f)), owning_building_guid = 10)
    }

    Building10009()

    def Building10009(): Unit = { // Name: ground_bldg_a_10009 Type: ground_bldg_a GUID: 34, MapID: 10009
      LocalBuilding("ground_bldg_a_10009", 34, 10009, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1349.99f, 1151.14f, 89.94f), ground_bldg_a)))
      LocalObject(681, Door.Constructor(Vector3(1339.435f, 1163.506f, 91.719f)), owning_building_guid = 34)
      LocalObject(686, Door.Constructor(Vector3(1366.754f, 1149.312f, 91.719f)), owning_building_guid = 34)
    }

    Building10003()

    def Building10003(): Unit = { // Name: ground_bldg_b_10003 Type: ground_bldg_b GUID: 35, MapID: 10003
      LocalBuilding("ground_bldg_b_10003", 35, 10003, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(985.91f, 922.1f, 92.93f), ground_bldg_b)))
      LocalObject(600, Door.Constructor(Vector3(986.4801f, 938.7028f, 94.709f)), owning_building_guid = 35)
      LocalObject(603, Door.Constructor(Vector3(991.9911f, 921.6182f, 94.709f)), owning_building_guid = 35)
      LocalObject(610, Door.Constructor(Vector3(1009.814f, 930.2303f, 100.209f)), owning_building_guid = 35)
    }

    Building10004()

    def Building10004(): Unit = { // Name: ground_bldg_b_10004 Type: ground_bldg_b GUID: 36, MapID: 10004
      LocalBuilding("ground_bldg_b_10004", 36, 10004, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1006.48f, 1145.29f, 91.97f), ground_bldg_b)))
      LocalObject(604, Door.Constructor(Vector3(994.1558f, 1123.253f, 99.249f)), owning_building_guid = 36)
      LocalObject(609, Door.Constructor(Vector3(1001.638f, 1141.579f, 93.749f)), owning_building_guid = 36)
      LocalObject(612, Door.Constructor(Vector3(1017.166f, 1132.57f, 93.749f)), owning_building_guid = 36)
    }

    Building10034()

    def Building10034(): Unit = { // Name: ground_bldg_c_10034 Type: ground_bldg_c GUID: 37, MapID: 10034
      LocalBuilding("ground_bldg_c_10034", 37, 10034, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(825.03f, 1055.17f, 89.45f), ground_bldg_c)))
      LocalObject(566, Door.Constructor(Vector3(822.3052f, 1058.445f, 91.229f)), owning_building_guid = 37)
      LocalObject(571, Door.Constructor(Vector3(862.1569f, 1088.588f, 91.229f)), owning_building_guid = 37)
      LocalObject(576, Door.Constructor(Vector3(872.2495f, 1059.277f, 91.229f)), owning_building_guid = 37)
    }

    Building10088()

    def Building10088(): Unit = { // Name: ground_bldg_d_10088 Type: ground_bldg_d GUID: 38, MapID: 10088
      LocalBuilding("ground_bldg_d_10088", 38, 10088, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(975.7f, 689.58f, 94.32f), ground_bldg_d)))
      LocalObject(598, Door.Constructor(Vector3(962.0821f, 700.587f, 96.055f)), owning_building_guid = 38)
      LocalObject(599, Door.Constructor(Vector3(964.6931f, 675.9621f, 96.055f)), owning_building_guid = 38)
      LocalObject(601, Door.Constructor(Vector3(986.7192f, 703.1622f, 96.055f)), owning_building_guid = 38)
      LocalObject(602, Door.Constructor(Vector3(989.2822f, 678.5608f, 96.055f)), owning_building_guid = 38)
    }

    Building10304()

    def Building10304(): Unit = { // Name: ground_bldg_d_10304 Type: ground_bldg_d GUID: 39, MapID: 10304
      LocalBuilding("ground_bldg_d_10304", 39, 10304, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1055.42f, 1071.61f, 100.3f), ground_bldg_d)))
      LocalObject(620, Door.Constructor(Vector3(1038.179f, 1068.554f, 102.035f)), owning_building_guid = 39)
      LocalObject(624, Door.Constructor(Vector3(1052.364f, 1088.851f, 102.035f)), owning_building_guid = 39)
      LocalObject(627, Door.Constructor(Vector3(1058.441f, 1054.383f, 102.035f)), owning_building_guid = 39)
      LocalObject(631, Door.Constructor(Vector3(1072.647f, 1074.631f, 102.035f)), owning_building_guid = 39)
    }

    Building10015()

    def Building10015(): Unit = { // Name: ground_bldg_e_10015 Type: ground_bldg_e GUID: 40, MapID: 10015
      LocalBuilding("ground_bldg_e_10015", 40, 10015, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1033.29f, 654.74f, 94.57f), ground_bldg_e)))
      LocalObject(617, Door.Constructor(Vector3(1027.727f, 629.0727f, 101.849f)), owning_building_guid = 40)
      LocalObject(621, Door.Constructor(Vector3(1038.441f, 665.6412f, 96.349f)), owning_building_guid = 40)
      LocalObject(622, Door.Constructor(Vector3(1048.048f, 624.2689f, 96.349f)), owning_building_guid = 40)
      LocalObject(628, Door.Constructor(Vector3(1059.453f, 635.0347f, 101.849f)), owning_building_guid = 40)
    }

    Building10386()

    def Building10386(): Unit = { // Name: ground_bldg_f_10386 Type: ground_bldg_f GUID: 41, MapID: 10386
      LocalBuilding("ground_bldg_f_10386", 41, 10386, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1007.11f, 851.17f, 96.4f), ground_bldg_f)))
      LocalObject(605, Door.Constructor(Vector3(995.3392f, 877.2592f, 98.179f)), owning_building_guid = 41)
      LocalObject(619, Door.Constructor(Vector3(1029.802f, 830.1577f, 98.179f)), owning_building_guid = 41)
    }

    Building10032()

    def Building10032(): Unit = { // Name: ground_bldg_g_10032 Type: ground_bldg_g GUID: 42, MapID: 10032
      LocalBuilding("ground_bldg_g_10032", 42, 10032, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1263.21f, 1006.27f, 98.33f), ground_bldg_g)))
      LocalObject(671, Door.Constructor(Vector3(1255.226f, 989.76f, 100.109f)), owning_building_guid = 42)
      LocalObject(673, Door.Constructor(Vector3(1263.226f, 1023.76f, 100.109f)), owning_building_guid = 42)
    }

    Building10242()

    def Building10242(): Unit = { // Name: ground_bldg_h_10242 Type: ground_bldg_h GUID: 43, MapID: 10242
      LocalBuilding("ground_bldg_h_10242", 43, 10242, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1063.36f, 1197.96f, 104.38f), ground_bldg_h)))
      LocalObject(623, Door.Constructor(Vector3(1050.501f, 1207.053f, 108.659f)), owning_building_guid = 43)
      LocalObject(629, Door.Constructor(Vector3(1062.303f, 1181.009f, 106.159f)), owning_building_guid = 43)
      LocalObject(632, Door.Constructor(Vector3(1078.908f, 1204.747f, 106.159f)), owning_building_guid = 43)
    }

    Building10340()

    def Building10340(): Unit = { // Name: ground_bldg_i_10340 Type: ground_bldg_i GUID: 44, MapID: 10340
      LocalBuilding("ground_bldg_i_10340", 44, 10340, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(781.58f, 1112.31f, 97.55f), ground_bldg_i)))
      LocalObject(558, Door.Constructor(Vector3(760.7067f, 1097.229f, 99.329f)), owning_building_guid = 44)
      LocalObject(564, Door.Constructor(Vector3(804.8541f, 1120.703f, 99.329f)), owning_building_guid = 44)
    }

    Building10035()

    def Building10035(): Unit = { // Name: ground_bldg_j_10035 Type: ground_bldg_j GUID: 45, MapID: 10035
      LocalBuilding("ground_bldg_j_10035", 45, 10035, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(853.9f, 1131.82f, 87.35f), ground_bldg_j)))
      LocalObject(568, Door.Constructor(Vector3(841.6863f, 1134.432f, 89.129f)), owning_building_guid = 45)
      LocalObject(573, Door.Constructor(Vector3(866.14f, 1129.235f, 89.129f)), owning_building_guid = 45)
    }

    Building10005()

    def Building10005(): Unit = { // Name: ground_bldg_j_10005 Type: ground_bldg_j GUID: 46, MapID: 10005
      LocalBuilding("ground_bldg_j_10005", 46, 10005, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1062.19f, 885.69f, 92.65f), ground_bldg_j)))
      LocalObject(626, Door.Constructor(Vector3(1055.949f, 874.848f, 94.429f)), owning_building_guid = 46)
      LocalObject(630, Door.Constructor(Vector3(1068.449f, 896.4987f, 94.429f)), owning_building_guid = 46)
    }

    Building10229()

    def Building10229(): Unit = { // Name: ground_bldg_z_10229 Type: ground_bldg_z GUID: 47, MapID: 10229
      LocalBuilding("ground_bldg_z_10229", 47, 10229, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(875.59f, 1275.13f, 89.54f), ground_bldg_z)))
      LocalObject(569, Door.Constructor(Vector3(856.4307f, 1292.347f, 91.319f)), owning_building_guid = 47)
      LocalObject(570, Door.Constructor(Vector3(856.5692f, 1271.095f, 102.319f)), owning_building_guid = 47)
      LocalObject(572, Door.Constructor(Vector3(863.3589f, 1296.347f, 107.819f)), owning_building_guid = 47)
      LocalObject(577, Door.Constructor(Vector3(874.7871f, 1292.553f, 96.819f)), owning_building_guid = 47)
      LocalObject(578, Door.Constructor(Vector3(876.4307f, 1257.706f, 96.819f)), owning_building_guid = 47)
      LocalObject(579, Door.Constructor(Vector3(887.8589f, 1253.912f, 107.819f)), owning_building_guid = 47)
      LocalObject(585, Door.Constructor(Vector3(894.6122f, 1279.203f, 102.319f)), owning_building_guid = 47)
      LocalObject(586, Door.Constructor(Vector3(894.7871f, 1257.912f, 91.319f)), owning_building_guid = 47)
    }

    Building10082()

    def Building10082(): Unit = { // Name: ground_bldg_z_10082 Type: ground_bldg_z GUID: 48, MapID: 10082
      LocalBuilding("ground_bldg_z_10082", 48, 10082, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1207.32f, 852.88f, 95.64f), ground_bldg_z)))
      LocalObject(654, Door.Constructor(Vector3(1182.81f, 844.864f, 97.419f)), owning_building_guid = 48)
      LocalObject(655, Door.Constructor(Vector3(1182.81f, 852.864f, 113.919f)), owning_building_guid = 48)
      LocalObject(657, Door.Constructor(Vector3(1191.81f, 860.864f, 102.919f)), owning_building_guid = 48)
      LocalObject(658, Door.Constructor(Vector3(1201.336f, 834.37f, 108.419f)), owning_building_guid = 48)
      LocalObject(661, Door.Constructor(Vector3(1213.336f, 871.37f, 108.419f)), owning_building_guid = 48)
      LocalObject(662, Door.Constructor(Vector3(1222.81f, 844.864f, 102.919f)), owning_building_guid = 48)
      LocalObject(663, Door.Constructor(Vector3(1231.81f, 852.864f, 113.919f)), owning_building_guid = 48)
      LocalObject(664, Door.Constructor(Vector3(1231.81f, 860.864f, 97.419f)), owning_building_guid = 48)
    }

    Building10257()

    def Building10257(): Unit = { // Name: N_Redoubt Type: redoubt GUID: 49, MapID: 10257
      LocalBuilding("N_Redoubt", 49, 10257, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(760.72f, 1359.42f, 99.62f), redoubt)))
      LocalObject(791, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 49)
      LocalObject(557, Door.Constructor(Vector3(748.8035f, 1372.222f, 101.355f)), owning_building_guid = 49)
      LocalObject(559, Door.Constructor(Vector3(761.1804f, 1376.637f, 111.399f)), owning_building_guid = 49)
      LocalObject(560, Door.Constructor(Vector3(766.8094f, 1370.595f, 111.379f)), owning_building_guid = 49)
      LocalObject(561, Door.Constructor(Vector3(772.3025f, 1364.752f, 111.379f)), owning_building_guid = 49)
      LocalObject(562, Door.Constructor(Vector3(772.6735f, 1346.625f, 101.355f)), owning_building_guid = 49)
      LocalObject(563, Door.Constructor(Vector3(777.9285f, 1358.726f, 111.399f)), owning_building_guid = 49)
      LocalObject(799, Terminal.Constructor(Vector3(747.0984f, 1346.889f, 99.57581f), vanu_equipment_term), owning_building_guid = 49)
      LocalObject(802, Terminal.Constructor(Vector3(774.4174f, 1372.171f, 99.5735f), vanu_equipment_term), owning_building_guid = 49)
      LocalObject(739, SpawnTube.Constructor(Vector3(760.72f, 1359.42f, 99.62f), Vector3(0, 0, 227)), owning_building_guid = 49)
      LocalObject(726, Painbox.Constructor(Vector3(760.9607f, 1359.588f, 107.409f), painbox_continuous), owning_building_guid = 49)
    }

    Building10259()

    def Building10259(): Unit = { // Name: S_Redoubt Type: redoubt GUID: 50, MapID: 10259
      LocalBuilding("S_Redoubt", 50, 10259, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(1253.48f, 805.8f, 85.1f), redoubt)))
      LocalObject(794, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 50)
      LocalObject(665, Door.Constructor(Vector3(1237.635f, 813.2061f, 86.835f)), owning_building_guid = 50)
      LocalObject(667, Door.Constructor(Vector3(1247.457f, 821.9362f, 96.879f)), owning_building_guid = 50)
      LocalObject(670, Door.Constructor(Vector3(1254.94f, 818.4426f, 96.859f)), owning_building_guid = 50)
      LocalObject(672, Door.Constructor(Vector3(1262.222f, 815.0822f, 96.859f)), owning_building_guid = 50)
      LocalObject(674, Door.Constructor(Vector3(1269.356f, 798.4144f, 86.835f)), owning_building_guid = 50)
      LocalObject(675, Door.Constructor(Vector3(1269.696f, 811.6027f, 96.879f)), owning_building_guid = 50)
      LocalObject(839, Terminal.Constructor(Vector3(1245.545f, 789.0786f, 85.0558f), vanu_equipment_term), owning_building_guid = 50)
      LocalObject(842, Terminal.Constructor(Vector3(1261.403f, 822.7539f, 85.0535f), vanu_equipment_term), owning_building_guid = 50)
      LocalObject(740, SpawnTube.Constructor(Vector3(1253.48f, 805.8f, 85.1f), Vector3(0, 0, 205)), owning_building_guid = 50)
      LocalObject(730, Painbox.Constructor(Vector3(1253.64f, 806.0459f, 92.889f), painbox_continuous), owning_building_guid = 50)
    }

    Building10001()

    def Building10001(): Unit = { // Name: S_Stasis Type: vanu_control_point GUID: 203, MapID: 10001
      LocalBuilding("S_Stasis", 203, 10001, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(899.15f, 874.23f, 93.26f), vanu_control_point)))
      LocalObject(792, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 203)
      LocalObject(574, Door.Constructor(Vector3(867.9634f, 853.3779f, 95.039f)), owning_building_guid = 203)
      LocalObject(575, Door.Constructor(Vector3(871.7762f, 899.1088f, 95.039f)), owning_building_guid = 203)
      LocalObject(580, Door.Constructor(Vector3(888.4708f, 862.9407f, 125.039f)), owning_building_guid = 203)
      LocalObject(582, Door.Constructor(Vector3(889.0839f, 875.7824f, 99.98f)), owning_building_guid = 203)
      LocalObject(583, Door.Constructor(Vector3(891.4683f, 870.6323f, 125.019f)), owning_building_guid = 203)
      LocalObject(584, Door.Constructor(Vector3(894.1799f, 878.179f, 125.019f)), owning_building_guid = 203)
      LocalObject(587, Door.Constructor(Vector3(896.2295f, 860.4051f, 99.98f)), owning_building_guid = 203)
      LocalObject(588, Door.Constructor(Vector3(896.8278f, 885.9948f, 125.039f)), owning_building_guid = 203)
      LocalObject(589, Door.Constructor(Vector3(904.438f, 882.9578f, 99.98f)), owning_building_guid = 203)
      LocalObject(592, Door.Constructor(Vector3(911.6068f, 867.5507f, 99.98f)), owning_building_guid = 203)
      LocalObject(594, Door.Constructor(Vector3(933.7072f, 833.9666f, 95.039f)), owning_building_guid = 203)
      LocalObject(595, Door.Constructor(Vector3(942.624f, 894.8666f, 95.039f)), owning_building_guid = 203)
      LocalObject(808, Terminal.Constructor(Vector3(890.2345f, 869.1371f, 98.277f), vanu_equipment_term), owning_building_guid = 203)
      LocalObject(809, Terminal.Constructor(Vector3(891.9105f, 865.5347f, 98.273f), vanu_equipment_term), owning_building_guid = 203)
      LocalObject(810, Terminal.Constructor(Vector3(894.1234f, 880.0321f, 98.273f), vanu_equipment_term), owning_building_guid = 203)
      LocalObject(811, Terminal.Constructor(Vector3(897.7258f, 881.7081f, 98.277f), vanu_equipment_term), owning_building_guid = 203)
      LocalObject(813, Terminal.Constructor(Vector3(902.9813f, 861.585f, 98.277f), vanu_equipment_term), owning_building_guid = 203)
      LocalObject(814, Terminal.Constructor(Vector3(906.5131f, 863.2601f, 98.273f), vanu_equipment_term), owning_building_guid = 203)
      LocalObject(815, Terminal.Constructor(Vector3(908.7975f, 877.7581f, 98.273f), vanu_equipment_term), owning_building_guid = 203)
      LocalObject(816, Terminal.Constructor(Vector3(910.4735f, 874.1556f, 98.277f), vanu_equipment_term), owning_building_guid = 203)
      LocalObject(860, SpawnTube.Constructor(Vector3(900.3454f, 871.6666f, 98.399f), Vector3(0, 0, 200)), owning_building_guid = 203)
      LocalObject(727, Painbox.Constructor(Vector3(899.9723f, 871.4819f, 107.6018f), painbox_continuous), owning_building_guid = 203)
      LocalObject(731, Painbox.Constructor(Vector3(886.9322f, 876.6623f, 101.55f), painbox_door_radius_continuous), owning_building_guid = 203)
      LocalObject(732, Painbox.Constructor(Vector3(895.3766f, 858.9249f, 102.15f), painbox_door_radius_continuous), owning_building_guid = 203)
      LocalObject(733, Painbox.Constructor(Vector3(905.1611f, 884.7763f, 102.15f), painbox_door_radius_continuous), owning_building_guid = 203)
      LocalObject(734, Painbox.Constructor(Vector3(914.3688f, 866.9739f, 102.15f), painbox_door_radius_continuous), owning_building_guid = 203)
    }

    Building10002()

    def Building10002(): Unit = { // Name: N_Stasis Type: vanu_control_point GUID: 204, MapID: 10002
      LocalBuilding("N_Stasis", 204, 10002, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1149.27f, 1132.96f, 107.22f), vanu_control_point)))
      LocalObject(793, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 204)
      LocalObject(634, Door.Constructor(Vector3(1101.842f, 1109.17f, 108.999f)), owning_building_guid = 204)
      LocalObject(639, Door.Constructor(Vector3(1136.562f, 1139.138f, 113.94f)), owning_building_guid = 204)
      LocalObject(640, Door.Constructor(Vector3(1136.582f, 1168.265f, 108.999f)), owning_building_guid = 204)
      LocalObject(641, Door.Constructor(Vector3(1139.776f, 1122.489f, 113.94f)), owning_building_guid = 204)
      LocalObject(643, Door.Constructor(Vector3(1140.9f, 1146.053f, 138.999f)), owning_building_guid = 204)
      LocalObject(644, Door.Constructor(Vector3(1147.638f, 1141.284f, 138.979f)), owning_building_guid = 204)
      LocalObject(645, Door.Constructor(Vector3(1153.212f, 1142.351f, 113.94f)), owning_building_guid = 204)
      LocalObject(646, Door.Constructor(Vector3(1154.304f, 1136.827f, 138.979f)), owning_building_guid = 204)
      LocalObject(647, Door.Constructor(Vector3(1156.459f, 1125.718f, 113.94f)), owning_building_guid = 204)
      LocalObject(648, Door.Constructor(Vector3(1158.776f, 1085.785f, 108.999f)), owning_building_guid = 204)
      LocalObject(650, Door.Constructor(Vector3(1161.247f, 1132.367f, 138.999f)), owning_building_guid = 204)
      LocalObject(652, Door.Constructor(Vector3(1180.032f, 1153.502f, 108.999f)), owning_building_guid = 204)
      LocalObject(826, Terminal.Constructor(Vector3(1136.074f, 1132.302f, 112.237f), vanu_equipment_term), owning_building_guid = 204)
      LocalObject(827, Terminal.Constructor(Vector3(1136.845f, 1128.469f, 112.233f), vanu_equipment_term), owning_building_guid = 204)
      LocalObject(828, Terminal.Constructor(Vector3(1142.584f, 1142.088f, 112.233f), vanu_equipment_term), owning_building_guid = 204)
      LocalObject(829, Terminal.Constructor(Vector3(1146.458f, 1121.991f, 112.237f), vanu_equipment_term), owning_building_guid = 204)
      LocalObject(830, Terminal.Constructor(Vector3(1146.485f, 1142.843f, 112.237f), vanu_equipment_term), owning_building_guid = 204)
      LocalObject(831, Terminal.Constructor(Vector3(1150.359f, 1122.745f, 112.233f), vanu_equipment_term), owning_building_guid = 204)
      LocalObject(832, Terminal.Constructor(Vector3(1156.116f, 1136.434f, 112.233f), vanu_equipment_term), owning_building_guid = 204)
      LocalObject(833, Terminal.Constructor(Vector3(1156.87f, 1132.533f, 112.237f), vanu_equipment_term), owning_building_guid = 204)
      LocalObject(861, SpawnTube.Constructor(Vector3(1146.494f, 1132.42f, 112.359f), Vector3(0, 0, 304)), owning_building_guid = 204)
      LocalObject(728, Painbox.Constructor(Vector3(1146.405f, 1132.827f, 121.5618f), painbox_continuous), owning_building_guid = 204)
      LocalObject(735, Painbox.Constructor(Vector3(1135.332f, 1140.324f, 116.11f), painbox_door_radius_continuous), owning_building_guid = 204)
      LocalObject(736, Painbox.Constructor(Vector3(1138.548f, 1119.949f, 116.11f), painbox_door_radius_continuous), owning_building_guid = 204)
      LocalObject(737, Painbox.Constructor(Vector3(1154.586f, 1144.226f, 115.51f), painbox_door_radius_continuous), owning_building_guid = 204)
      LocalObject(738, Painbox.Constructor(Vector3(1158.049f, 1124.576f, 116.11f), painbox_door_radius_continuous), owning_building_guid = 204)
    }

    Building10000()

    def Building10000(): Unit = { // Name: Core Type: vanu_core GUID: 205, MapID: 10000
      LocalBuilding("Core", 205, 10000, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1028.8f, 1012.78f, 165.78f), vanu_core)))
      LocalObject(606, Door.Constructor(Vector3(996.3071f, 1000.802f, 172.568f)), owning_building_guid = 205)
      LocalObject(613, Door.Constructor(Vector3(1024.778f, 972.287f, 172.568f)), owning_building_guid = 205)
      LocalObject(614, Door.Constructor(Vector3(1024.778f, 972.287f, 177.568f)), owning_building_guid = 205)
      LocalObject(615, Door.Constructor(Vector3(1024.822f, 1029.273f, 177.568f)), owning_building_guid = 205)
      LocalObject(616, Door.Constructor(Vector3(1024.822f, 1029.273f, 182.568f)), owning_building_guid = 205)
      LocalObject(625, Door.Constructor(Vector3(1053.293f, 1000.758f, 182.568f)), owning_building_guid = 205)
    }

    Building10246()

    def Building10246(): Unit = { // Name: N_ATPlant Type: vanu_vehicle_station GUID: 231, MapID: 10246
      LocalBuilding("N_ATPlant", 231, 10246, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(739.86f, 1264.38f, 82.47f), vanu_vehicle_station)))
      LocalObject(790, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 231)
      LocalObject(549, Door.Constructor(Vector3(684.2806f, 1258.55f, 114.249f)), owning_building_guid = 231)
      LocalObject(550, Door.Constructor(Vector3(688.5319f, 1265.615f, 114.229f)), owning_building_guid = 231)
      LocalObject(551, Door.Constructor(Vector3(692.6342f, 1272.507f, 114.229f)), owning_building_guid = 231)
      LocalObject(552, Door.Constructor(Vector3(696.8887f, 1279.533f, 114.249f)), owning_building_guid = 231)
      LocalObject(553, Door.Constructor(Vector3(710.3329f, 1224.244f, 84.249f)), owning_building_guid = 231)
      LocalObject(554, Door.Constructor(Vector3(714.447f, 1231.091f, 104.173f)), owning_building_guid = 231)
      LocalObject(555, Door.Constructor(Vector3(718.8141f, 1284.958f, 104.161f)), owning_building_guid = 231)
      LocalObject(556, Door.Constructor(Vector3(743.5565f, 1279.537f, 84.249f)), owning_building_guid = 231)
      LocalObject(687, Door.Constructor(Vector3(675.4597f, 1282.781f, 89.103f)), owning_building_guid = 231)
      LocalObject(785, Terminal.Constructor(Vector3(705.5295f, 1244.009f, 102.387f), vanu_air_vehicle_term), owning_building_guid = 231)
      LocalObject(863, VehicleSpawnPad.Constructor(Vector3(713.2993f, 1248.321f, 102.386f), vanu_vehicle_creation_pad, Vector3(0, 0, 121)), owning_building_guid = 231, terminal_guid = 785)
      LocalObject(786, Terminal.Constructor(Vector3(723.7557f, 1274.36f, 102.387f), vanu_air_vehicle_term), owning_building_guid = 231)
      LocalObject(864, VehicleSpawnPad.Constructor(Vector3(723.6083f, 1265.478f, 102.386f), vanu_vehicle_creation_pad, Vector3(0, 0, 121)), owning_building_guid = 231, terminal_guid = 786)
      LocalObject(796, Terminal.Constructor(Vector3(702.5449f, 1252.534f, 84.97f), vanu_equipment_term), owning_building_guid = 231)
      LocalObject(797, Terminal.Constructor(Vector3(714.9058f, 1273.106f, 84.97f), vanu_equipment_term), owning_building_guid = 231)
      LocalObject(868, Terminal.Constructor(Vector3(703.9712f, 1265.516f, 87.47f), vanu_vehicle_term), owning_building_guid = 231)
      LocalObject(862, VehicleSpawnPad.Constructor(Vector3(691.0519f, 1273.405f, 84.875f), vanu_vehicle_creation_pad, Vector3(0, 0, -59)), owning_building_guid = 231, terminal_guid = 868)
    }

    Building10248()

    def Building10248(): Unit = { // Name: S_ATPlant Type: vanu_vehicle_station GUID: 232, MapID: 10248
      LocalBuilding("S_ATPlant", 232, 10248, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1304.12f, 799.11f, 83.07f), vanu_vehicle_station)))
      LocalObject(795, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 232)
      LocalObject(677, Door.Constructor(Vector3(1298.604f, 784.5164f, 84.849f)), owning_building_guid = 232)
      LocalObject(678, Door.Constructor(Vector3(1322.501f, 776.121f, 104.761f)), owning_building_guid = 232)
      LocalObject(679, Door.Constructor(Vector3(1333.401f, 829.0539f, 104.773f)), owning_building_guid = 232)
      LocalObject(680, Door.Constructor(Vector3(1338.318f, 835.3486f, 84.849f)), owning_building_guid = 232)
      LocalObject(682, Door.Constructor(Vector3(1344.924f, 778.8329f, 114.849f)), owning_building_guid = 232)
      LocalObject(683, Door.Constructor(Vector3(1350.003f, 785.2883f, 114.829f)), owning_building_guid = 232)
      LocalObject(684, Door.Constructor(Vector3(1354.915f, 791.6285f, 114.829f)), owning_building_guid = 232)
      LocalObject(685, Door.Constructor(Vector3(1359.996f, 798.1234f, 114.849f)), owning_building_guid = 232)
      LocalObject(688, Door.Constructor(Vector3(1365.798f, 772.9979f, 89.703f)), owning_building_guid = 232)
      LocalObject(787, Terminal.Constructor(Vector3(1318.888f, 787.242f, 102.987f), vanu_air_vehicle_term), owning_building_guid = 232)
      LocalObject(865, VehicleSpawnPad.Constructor(Vector3(1320.117f, 796.0395f, 102.986f), vanu_vehicle_creation_pad, Vector3(0, 0, -52)), owning_building_guid = 232, terminal_guid = 787)
      LocalObject(788, Terminal.Constructor(Vector3(1340.677f, 815.1455f, 102.987f), vanu_air_vehicle_term), owning_building_guid = 232)
      LocalObject(866, VehicleSpawnPad.Constructor(Vector3(1332.44f, 811.8123f, 102.986f), vanu_vehicle_creation_pad, Vector3(0, 0, -52)), owning_building_guid = 232, terminal_guid = 788)
      LocalObject(846, Terminal.Constructor(Vector3(1327.825f, 787.4082f, 85.57f), vanu_equipment_term), owning_building_guid = 232)
      LocalObject(847, Terminal.Constructor(Vector3(1342.601f, 806.3204f, 85.57f), vanu_equipment_term), owning_building_guid = 232)
      LocalObject(869, Terminal.Constructor(Vector3(1339.603f, 793.6083f, 88.07f), vanu_vehicle_term), owning_building_guid = 232)
      LocalObject(867, VehicleSpawnPad.Constructor(Vector3(1351.464f, 784.2039f, 85.475f), vanu_vehicle_creation_pad, Vector3(0, 0, 128)), owning_building_guid = 232, terminal_guid = 869)
    }

    Building10225()

    def Building10225(): Unit = { // Name: GW_Cavern6_W Type: warpgate_cavern GUID: 233, MapID: 10225
      LocalBuilding("GW_Cavern6_W", 233, 10225, FoundationBuilder(WarpGate.Structure(Vector3(281.98f, 637.77f, 60.83f))))
    }

    Building10224()

    def Building10224(): Unit = { // Name: GW_Cavern6_N Type: warpgate_cavern GUID: 234, MapID: 10224
      LocalBuilding("GW_Cavern6_N", 234, 10224, FoundationBuilder(WarpGate.Structure(Vector3(1093.93f, 2082.3f, 80.82f))))
    }

    Building10226()

    def Building10226(): Unit = { // Name: GW_Cavern6_S Type: warpgate_cavern GUID: 235, MapID: 10226
      LocalBuilding("GW_Cavern6_S", 235, 10226, FoundationBuilder(WarpGate.Structure(Vector3(1218.27f, 218.05f, 100.82f))))
    }

    Building10227()

    def Building10227(): Unit = { // Name: GW_Cavern6_E Type: warpgate_cavern GUID: 236, MapID: 10227
      LocalBuilding("GW_Cavern6_E", 236, 10227, FoundationBuilder(WarpGate.Structure(Vector3(1986.27f, 922.08f, 20.82f))))
    }

    ZoneOwnedObjects()

    def ZoneOwnedObjects(): Unit = {
      LocalObject(798, Terminal.Constructor(Vector3(746.59f, 1360.06f, 99.58f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(800, Terminal.Constructor(Vector3(760.13f, 1345.53f, 99.58f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(801, Terminal.Constructor(Vector3(761.34f, 1373.51f, 99.58f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(803, Terminal.Constructor(Vector3(774.88f, 1358.94f, 99.58f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(804, Terminal.Constructor(Vector3(867.18f, 1080.28f, 89.45f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(805, Terminal.Constructor(Vector3(871.23f, 1068f, 89.45f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(806, Terminal.Constructor(Vector3(873.46f, 1273.87f, 100.52f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(807, Terminal.Constructor(Vector3(877.77f, 1276.37f, 100.52f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(812, Terminal.Constructor(Vector3(901.33f, 949.3f, 176.04f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(817, Terminal.Constructor(Vector3(919.88f, 968.41f, 176.04f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(818, Terminal.Constructor(Vector3(968.73f, 1210.66f, 179.41f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(819, Terminal.Constructor(Vector3(974.72f, 1169.48f, 179.41f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(820, Terminal.Constructor(Vector3(996.15f, 1132.13f, 97.47f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(821, Terminal.Constructor(Vector3(1007.58f, 1125.96f, 91.97f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(822, Terminal.Constructor(Vector3(1015.53f, 643.92f, 94.56f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(823, Terminal.Constructor(Vector3(1048.62f, 890.34f, 92.65f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(824, Terminal.Constructor(Vector3(1050.16f, 654.59f, 94.56f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(825, Terminal.Constructor(Vector3(1075.73f, 880.84f, 92.65f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(834, Terminal.Constructor(Vector3(1193.54f, 969.01f, 167.4f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(835, Terminal.Constructor(Vector3(1198.76f, 961.62f, 167.4f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(836, Terminal.Constructor(Vector3(1207.29f, 837.52f, 116.64f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(837, Terminal.Constructor(Vector3(1207.35f, 868.13f, 116.64f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(838, Terminal.Constructor(Vector3(1240.36f, 801.01f, 85.06f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(840, Terminal.Constructor(Vector3(1248.79f, 818.76f, 85.06f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(841, Terminal.Constructor(Vector3(1258.22f, 792.85f, 85.06f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(843, Terminal.Constructor(Vector3(1263.08f, 997.75f, 100.83f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(844, Terminal.Constructor(Vector3(1263.42f, 1014.84f, 100.83f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(845, Terminal.Constructor(Vector3(1266.79f, 810.47f, 85.06f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(12, ProximityTerminal.Constructor(Vector3(1172.58f, 709.8f, 81.4f), crystals_health_a), owning_building_guid = 0)
      LocalObject(703, ProximityTerminal.Constructor(Vector3(857.28f, 1126.68f, 87.35f), crystals_health_a), owning_building_guid = 0)
      LocalObject(704, ProximityTerminal.Constructor(Vector3(978.1f, 685.44f, 96.28f), crystals_health_a), owning_building_guid = 0)
      LocalObject(705, ProximityTerminal.Constructor(Vector3(997.47f, 936.4f, 98.43f), crystals_health_a), owning_building_guid = 0)
      LocalObject(706, ProximityTerminal.Constructor(Vector3(1144.78f, 920.28f, 179.44f), crystals_health_a), owning_building_guid = 0)
      LocalObject(707, ProximityTerminal.Constructor(Vector3(1207.19f, 849.69f, 106.62f), crystals_health_a), owning_building_guid = 0)
      LocalObject(708, ProximityTerminal.Constructor(Vector3(1256.2f, 1050.93f, 170.44f), crystals_health_a), owning_building_guid = 0)
      LocalObject(709, ProximityTerminal.Constructor(Vector3(1350.98f, 1147.12f, 89.94f), crystals_health_a), owning_building_guid = 0)
      LocalObject(710, ProximityTerminal.Constructor(Vector3(828.19f, 1024.07f, 166.21f), crystals_health_b), owning_building_guid = 0)
      LocalObject(711, ProximityTerminal.Constructor(Vector3(887.15f, 1281.71f, 110.54f), crystals_health_b), owning_building_guid = 0)
      LocalObject(712, ProximityTerminal.Constructor(Vector3(1055.56f, 1071.61f, 102.26f), crystals_health_b), owning_building_guid = 0)
      LocalObject(713, ProximityTerminal.Constructor(Vector3(1069.99f, 1127.29f, 174.49f), crystals_health_b), owning_building_guid = 0)
      LocalObject(206, FacilityTurret.Constructor(Vector3(740.28f, 1248.13f, 102.39f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(206, 5000)
      LocalObject(207, FacilityTurret.Constructor(Vector3(809.59f, 1107.17f, 106.13f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(207, 5001)
      LocalObject(208, FacilityTurret.Constructor(Vector3(850.2f, 1289.34f, 95.04f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(208, 5002)
      LocalObject(209, FacilityTurret.Constructor(Vector3(882.78f, 1056.81f, 89.36f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(209, 5003)
      LocalObject(210, FacilityTurret.Constructor(Vector3(883.18f, 900.09f, 123.26f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(210, 5004)
      LocalObject(211, FacilityTurret.Constructor(Vector3(897.52f, 1111.75f, 95.42f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(211, 5005)
      LocalObject(212, FacilityTurret.Constructor(Vector3(901.13f, 1260.92f, 95.04f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(212, 5006)
      LocalObject(213, FacilityTurret.Constructor(Vector3(933.25f, 896.94f, 108.78f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(213, 5007)
      LocalObject(214, FacilityTurret.Constructor(Vector3(980.25f, 1277.41f, 81.61f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(214, 5008)
      LocalObject(215, FacilityTurret.Constructor(Vector3(989.24f, 1112.19f, 97.47f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(215, 5009)
      LocalObject(216, FacilityTurret.Constructor(Vector3(1010.06f, 1207.81f, 100.65f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(216, 5010)
      LocalObject(217, FacilityTurret.Constructor(Vector3(1020.39f, 934.87f, 98.43f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(217, 5011)
      LocalObject(218, FacilityTurret.Constructor(Vector3(1024.5f, 824.39f, 109.9f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(218, 5012)
      LocalObject(219, FacilityTurret.Constructor(Vector3(1046.17f, 1093.7f, 100.3f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(219, 5013)
      LocalObject(220, FacilityTurret.Constructor(Vector3(1103.21f, 909.09f, 81.32f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(220, 5014)
      LocalObject(221, FacilityTurret.Constructor(Vector3(1106.4f, 1091.2f, 115.81f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(221, 5015)
      LocalObject(222, FacilityTurret.Constructor(Vector3(1143.93f, 1173.62f, 137.22f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(222, 5016)
      LocalObject(223, FacilityTurret.Constructor(Vector3(1158.52f, 731.05f, 90.93f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(223, 5017)
      LocalObject(224, FacilityTurret.Constructor(Vector3(1181.47f, 838.23f, 101.14f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(224, 5018)
      LocalObject(225, FacilityTurret.Constructor(Vector3(1212.43f, 868.94f, 120.92f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(225, 5019)
      LocalObject(226, FacilityTurret.Constructor(Vector3(1219.12f, 993.4f, 102.26f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(226, 5020)
      LocalObject(227, FacilityTurret.Constructor(Vector3(1232.83f, 867.35f, 101.14f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(227, 5021)
      LocalObject(228, FacilityTurret.Constructor(Vector3(1306.63f, 814.39f, 102.99f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(228, 5022)
      LocalObject(229, FacilityTurret.Constructor(Vector3(1338.8f, 750.12f, 102.99f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(229, 5023)
      LocalObject(230, FacilityTurret.Constructor(Vector3(1356.64f, 861.39f, 92.4f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(230, 5024)
    }

    def Lattice(): Unit = {
      LatticeLink("N_Redoubt", "N_ATPlant")
      LatticeLink("GW_Cavern6_W", "N_ATPlant")
      LatticeLink("N_Stasis", "Core")
      LatticeLink("S_Stasis", "Core")
      LatticeLink("S_Redoubt", "S_ATPlant")
      LatticeLink("N_Redoubt", "N_Stasis")
      LatticeLink("S_Redoubt", "S_Stasis")
      LatticeLink("S_Stasis", "N_ATPlant")
      LatticeLink("N_Stasis", "S_ATPlant")
      LatticeLink("GW_Cavern6_N", "N_Redoubt")
      LatticeLink("GW_Cavern6_E", "S_Redoubt")
      LatticeLink("GW_Cavern6_S", "S_ATPlant")
    }

    Lattice()

    def ZipLines(): Unit = {
      ZipLinePaths(new ZipLinePath(1, true, List(Vector3(736.484f, 1292.551f, 102.746f), Vector3(733.296f, 1284.238f, 85.32f))))
      ZipLinePaths(new ZipLinePath(2, false, List(Vector3(797.258f, 1095.976f, 107.883f), Vector3(821.79f, 1046.854f, 164.56f))))
      ZipLinePaths(new ZipLinePath(3, false, List(Vector3(830.324f, 1048.307f, 164.563f), Vector3(930.348f, 1184.376f, 180.261f))))
      ZipLinePaths(new ZipLinePath(4, false, List(Vector3(831.267f, 1000.379f, 164.567f), Vector3(878.283f, 967.178f, 174.45f))))
      ZipLinePaths(new ZipLinePath(5, false, List(Vector3(850.911f, 1030.282f, 164.806f), Vector3(851.274f, 1033.241f, 164.292f), Vector3(862.522f, 1124.985f, 97.195f))))
      ZipLinePaths(new ZipLinePath(6, true, List(Vector3(872.544f, 1280.257f, 106.39f), Vector3(873.173f, 1280.791f, 100.874f))))
      ZipLinePaths(new ZipLinePath(7, false, List(Vector3(877.178f, 991.846f, 175.71f), Vector3(851.866f, 1023.792f, 167.996f))))
      ZipLinePaths(new ZipLinePath(8, true, List(Vector3(877.484f, 1269.249f, 100.874f), Vector3(879.568f, 1268.493f, 106.39f))))
      ZipLinePaths(new ZipLinePath(9, false, List(Vector3(890.759f, 1249.269f, 106.891f), Vector3(929.721f, 1191.344f, 180.248f), Vector3(930.978f, 1189.476f, 180.258f))))
      ZipLinePaths(new ZipLinePath(10, false, List(Vector3(906.216f, 993.369f, 174.363f), Vector3(904.343f, 1081.134f, 189.577f))))
      ZipLinePaths(new ZipLinePath(11, false, List(Vector3(897.3f, 1088.55f, 189.596f), Vector3(893.393f, 1085.86f, 189.107f), Vector3(834.797f, 1045.518f, 164.587f))))
      ZipLinePaths(new ZipLinePath(12, false, List(Vector3(919.03f, 892.661f, 124.113f), Vector3(946.681f, 941.791f, 118.324f))))
      ZipLinePaths(new ZipLinePath(13, false, List(Vector3(935.446f, 1097.096f, 178.111f), Vector3(987.271f, 1008.196f, 171.632f))))
      ZipLinePaths(new ZipLinePath(14, false, List(Vector3(931.487f, 1179.55f, 180.267f), Vector3(945.998f, 1134.391f, 189.579f))))
      ZipLinePaths(new ZipLinePath(15, false, List(Vector3(941.35f, 952.9f, 118.301f), Vector3(997.19f, 982.882f, 116.193f))))
      ZipLinePaths(new ZipLinePath(16, true, List(Vector3(979.744f, 928.34f, 93.272f), Vector3(979.439f, 928.257f, 98.779f))))
      ZipLinePaths(new ZipLinePath(17, false, List(Vector3(988.07f, 993.856f, 171.603f), Vector3(930.176f, 982.283f, 174.075f))))
      ZipLinePaths(new ZipLinePath(18, false, List(Vector3(991.738f, 887.327f, 163.802f), Vector3(925.305f, 953.451f, 176.895f))))
      ZipLinePaths(new ZipLinePath(19, false, List(Vector3(1004.142f, 905.185f, 163.947f), Vector3(1015.49f, 963.547f, 171.569f))))
      ZipLinePaths(new ZipLinePath(20, false, List(Vector3(1005.402f, 1400.078f, 123.017f), Vector3(864.789f, 1301.178f, 106.891f))))
      ZipLinePaths(new ZipLinePath(21, true, List(Vector3(1006.212f, 1018.303f, 176.13f), Vector3(1006.335f, 1018.239f, 181.13f))))
      ZipLinePaths(new ZipLinePath(22, true, List(Vector3(1006.301f, 869.097f, 163.281f), Vector3(1006.262f, 868.834f, 168.788f))))
      ZipLinePaths(new ZipLinePath(23, true, List(Vector3(1007.402f, 983.651f, 171.13f), Vector3(1007.46f, 983.67f, 176.13f))))
      ZipLinePaths(new ZipLinePath(24, true, List(Vector3(1008.926f, 1018.168f, 166.13f), Vector3(1008.886f, 1018.146f, 171.13f))))
      ZipLinePaths(new ZipLinePath(25, false, List(Vector3(1006.662f, 832.842f, 112.39f), Vector3(918.117f, 946.097f, 176.938f))))
      ZipLinePaths(new ZipLinePath(26, false, List(Vector3(1012.99f, 1201.236f, 180.278f), Vector3(1020.024f, 1402.671f, 123.084f))))
      ZipLinePaths(new ZipLinePath(27, false, List(Vector3(1014.401f, 1192.4f, 180.259f), Vector3(1024.248f, 1120.21f, 175.306f))))
      ZipLinePaths(new ZipLinePath(28, true, List(Vector3(1015.772f, 1144.392f, 92.312f), Vector3(1015.304f, 1144.907f, 97.818f))))
      ZipLinePaths(new ZipLinePath(29, false, List(Vector3(1018.083f, 962.352f, 176.63f), Vector3(1015.899f, 960.875f, 176.124f), Vector3(948.199f, 915.089f, 102.837f))))
      ZipLinePaths(new ZipLinePath(30, false, List(Vector3(1019.599f, 1038.769f, 181.6f), Vector3(1026.673f, 1104.455f, 175.304f))))
      ZipLinePaths(new ZipLinePath(31, false, List(Vector3(1019.603f, 1108.067f, 175.312f), Vector3(957.962f, 1127.949f, 189.078f), Vector3(953.853f, 1129.275f, 189.58f))))
      ZipLinePaths(new ZipLinePath(32, false, List(Vector3(1028.221f, 1036.732f, 176.631f), Vector3(1014.174f, 1136.949f, 112.501f))))
      ZipLinePaths(new ZipLinePath(33, false, List(Vector3(1029.711f, 964.586f, 176.63f), Vector3(1030.75f, 962.27f, 176.113f), Vector3(1062.96f, 890.468f, 102.494f))))
      ZipLinePaths(new ZipLinePath(34, false, List(Vector3(1030.361f, 963.633f, 171.579f), Vector3(1102.77f, 942.405f, 185.802f))))
      ZipLinePaths(new ZipLinePath(35, true, List(Vector3(1041.412f, 1017.873f, 166.13f), Vector3(1041.577f, 1018.02f, 176.13f))))
      ZipLinePaths(new ZipLinePath(36, true, List(Vector3(1042.555f, 983.339f, 176.13f), Vector3(1042.784f, 983.227f, 181.13f))))
      ZipLinePaths(new ZipLinePath(37, true, List(Vector3(1042.968f, 984.255f, 166.13f), Vector3(1043.026f, 984.175f, 171.13f))))
      ZipLinePaths(new ZipLinePath(38, false, List(Vector3(1049.448f, 1122.065f, 106.348f), Vector3(925.736f, 986.791f, 174.108f))))
      ZipLinePaths(new ZipLinePath(39, false, List(Vector3(1050.181f, 996.471f, 116.194f), Vector3(1094.395f, 1021.971f, 119.888f), Vector3(1100.711f, 1025.614f, 119.731f))))
      ZipLinePaths(new ZipLinePath(40, false, List(Vector3(1062.232f, 1012.159f, 181.756f), Vector3(1108.189f, 1029.232f, 172.319f))))
      ZipLinePaths(new ZipLinePath(41, false, List(Vector3(1064.671f, 626.333f, 102.048f), Vector3(1150.823f, 607.773f, 107.952f))))
      ZipLinePaths(new ZipLinePath(42, false, List(Vector3(1080.843f, 1098.006f, 175.309f), Vector3(1033.702f, 1041.054f, 181.641f))))
      ZipLinePaths(new ZipLinePath(43, false, List(Vector3(1094.15f, 1103.925f, 175.33f), Vector3(1131.292f, 1068.319f, 169.536f))))
      ZipLinePaths(new ZipLinePath(44, false, List(Vector3(1100.778f, 938.989f, 185.807f), Vector3(1021.232f, 892.206f, 163.757f))))
      ZipLinePaths(new ZipLinePath(45, false, List(Vector3(1111.653f, 1051.984f, 172.311f), Vector3(1086.377f, 1100.131f, 175.317f))))
      ZipLinePaths(new ZipLinePath(46, false, List(Vector3(1119.839f, 953.53f, 180.529f), Vector3(1064.102f, 992.538f, 181.578f))))
      ZipLinePaths(new ZipLinePath(47, false, List(Vector3(1122.09f, 1126.956f, 138.079f), Vector3(1100.116f, 1034.584f, 119.739f))))
      ZipLinePaths(new ZipLinePath(48, false, List(Vector3(1154.562f, 615.219f, 107.973f), Vector3(1060.824f, 665.928f, 95.423f))))
      ZipLinePaths(new ZipLinePath(49, false, List(Vector3(1173.53f, 947.864f, 166.273f), Vector3(1146.927f, 940.176f, 185.27f), Vector3(1145.154f, 939.664f, 185.791f))))
      ZipLinePaths(new ZipLinePath(50, false, List(Vector3(1177.347f, 857.502f, 112.991f), Vector3(1134.971f, 902.085f, 179.808f), Vector3(1133.604f, 903.523f, 180.296f))))
      ZipLinePaths(new ZipLinePath(51, false, List(Vector3(1177.818f, 984.919f, 166.285f), Vector3(1171.66f, 1033.014f, 172.314f))))
      ZipLinePaths(new ZipLinePath(52, true, List(Vector3(1202.167f, 852.92f, 112.49f), Vector3(1201.179f, 854.737f, 106.974f))))
      ZipLinePaths(new ZipLinePath(53, true, List(Vector3(1213.359f, 851.26f, 106.974f), Vector3(1212.823f, 852.659f, 112.49f))))
      ZipLinePaths(new ZipLinePath(54, false, List(Vector3(1213.7f, 680.904f, 133.639f), Vector3(1210.939f, 676.835f, 133.114f), Vector3(1169.527f, 615.806f, 108.011f))))
      ZipLinePaths(new ZipLinePath(55, false, List(Vector3(1224.856f, 696.306f, 132.937f), Vector3(1237.086f, 848.842f, 112.994f))))
      ZipLinePaths(new ZipLinePath(56, false, List(Vector3(1251.865f, 1025.854f, 176.802f), Vector3(1221.815f, 988.86f, 166.322f))))
      ZipLinePaths(new ZipLinePath(57, false, List(Vector3(1280.512f, 1055.381f, 171.316f), Vector3(1376.507f, 1189.917f, 117.236f))))
      ZipLinePaths(new ZipLinePath(58, true, List(Vector3(1304.143f, 770.517f, 103.346f), Vector3(1307.887f, 777.753f, 85.92f))))
      ZipLinePaths(new ZipLinePath(59, true, List(Vector3(1346.959f, 827.541f, 85.92f), Vector3(1351.629f, 834.391f, 103.346f))))
      ZipLinePaths(new ZipLinePath(60, true, List(Vector3(1373.997f, 1180.755f, 83.698f), Vector3(1376.644f, 1197.211f, 116.71f))))
      ZipLinePaths(new ZipLinePath(61, false, List(Vector3(1376.099f, 1104.824f, 108.05f), Vector3(1271.454f, 1067.596f, 170.802f), Vector3(1269.793f, 1067.005f, 171.301f))))
      ZipLinePaths(new ZipLinePath(62, true, List(Vector3(1381.237f, 1195.832f, 116.71f), Vector3(1384.165f, 1181.39f, 83.698f))))
      ZipLinePaths(new ZipLinePath(63, false, List(Vector3(1381.497f, 1187.474f, 117.214f), Vector3(1389.994f, 1112.664f, 107.947f))))
      ZipLinePaths(new ZipLinePath(64, false, List(Vector3(627.092f, 916.732f, 118.239f), Vector3(822.396f, 868.007f, 92.658f))))
      ZipLinePaths(new ZipLinePath(65, false, List(Vector3(854.65f, 866.898f, 124.13f), Vector3(816.091f, 1002.187f, 164.58f))))
      ZipLinePaths(new ZipLinePath(66, true, List(Vector3(1260.882f, 821.364f, 85.406f), Vector3(1240.71f, 805.898f, 95.45f))))
      ZipLinePaths(new ZipLinePath(67, true, List(Vector3(1260.807f, 798f, 95.45f), Vector3(1246.127f, 790.09f, 85.406f))))
      ZipLinePaths(new ZipLinePath(68, false, List(Vector3(993.401f, 705.41f, 95.203f), Vector3(1117.34f, 894.464f, 180.386f), Vector3(1119.307f, 897.465f, 180.316f))))
      ZipLinePaths(new ZipLinePath(69, false, List(Vector3(1231.631f, 873.279f, 101.99f), Vector3(1252.107f, 977.047f, 99.195f))))
      ZipLinePaths(new ZipLinePath(70, false, List(Vector3(1297.144f, 960.064f, 110.671f), Vector3(1230.772f, 978.768f, 166.333f), Vector3(1226.347f, 980.015f, 166.311f))))
      ZipLinePaths(new ZipLinePath(71, false, List(Vector3(1184.828f, 1039.951f, 169.627f), Vector3(1238.524f, 1034.453f, 171.286f))))
      ZipLinePaths(new ZipLinePath(72, false, List(Vector3(1300.449f, 972.002f, 110.737f), Vector3(1183.438f, 1032.292f, 173.593f), Vector3(1175.637f, 1036.312f, 172.356f))))
      ZipLinePaths(new ZipLinePath(73, false, List(Vector3(1216.71f, 879.307f, 107.531f), Vector3(1241.241f, 1027.486f, 170.833f), Vector3(1242.032f, 1032.266f, 171.265f))))
      ZipLinePaths(new ZipLinePath(74, false, List(Vector3(1176.666f, 1142.367f, 138.11f), Vector3(1168.297f, 1081.591f, 171.87f), Vector3(1167.739f, 1077.54f, 172.35f))))
      ZipLinePaths(new ZipLinePath(75, false, List(Vector3(1152.799f, 1174.853f, 138.109f), Vector3(1103.141f, 1132.14f, 174.215f), Vector3(1099.831f, 1129.292f, 174.744f))))
      ZipLinePaths(new ZipLinePath(76, false, List(Vector3(1123.628f, 1194.305f, 107.551f), Vector3(1020.93f, 1196.254f, 180.315f), Vector3(1014.084f, 1196.384f, 180.259f))))
      ZipLinePaths(new ZipLinePath(77, true, List(Vector3(747.901f, 1347.392f, 99.926f), Vector3(749.063f, 1364.266f, 109.97f))))
      ZipLinePaths(new ZipLinePath(78, true, List(Vector3(764.978f, 1349.413f, 109.97f), Vector3(773.404f, 1371.263f, 99.926f))))
      ZipLinePaths(new ZipLinePath(79, false, List(Vector3(832.845f, 1322.466f, 92.854f), Vector3(852.713f, 1315.965f, 90.02f))))
      ZipLinePaths(new ZipLinePath(80, false, List(Vector3(827.617f, 1279.218f, 92.551f), Vector3(811.451f, 1305.666f, 92.85f))))
      ZipLinePaths(new ZipLinePath(81, false, List(Vector3(735.485f, 1339.063f, 99.673f), Vector3(715.854f, 1302.093f, 103.238f))))
      ZipLinePaths(new ZipLinePath(82, false, List(Vector3(767.646f, 1325.436f, 99.674f), Vector3(782.281f, 1261.401f, 92.453f))))
      ZipLinePaths(new ZipLinePath(83, false, List(Vector3(854.91f, 1298.24f, 106.892f), Vector3(794.221f, 1358.634f, 99.673f))))
      ZipLinePaths(new ZipLinePath(84, false, List(Vector3(833.177f, 1339.31f, 92.853f), Vector3(855.999f, 1333.167f, 90.019f))))
      ZipLinePaths(new ZipLinePath(85, false, List(Vector3(909.731f, 849.693f, 124.164f), Vector3(990.096f, 872.508f, 163.851f))))
      ZipLinePaths(new ZipLinePath(86, false, List(Vector3(980.443f, 931.687f, 114.255f), Vector3(892.726f, 938.268f, 173.636f), Vector3(886.878f, 938.707f, 174.076f))))
      ZipLinePaths(new ZipLinePath(87, false, List(Vector3(1202.692f, 861.073f, 121.781f), Vector3(1207.235f, 934.6f, 165.81f), Vector3(1207.537f, 939.502f, 166.299f))))
      ZipLinePaths(new ZipLinePath(88, false, List(Vector3(1236.684f, 860.28f, 112.99f), Vector3(1300.068f, 959.856f, 110.671f))))
      ZipLinePaths(new ZipLinePath(89, true, List(Vector3(1314.086f, 962.603f, 87.15f), Vector3(1305.694f, 964.636f, 110.181f))))
      ZipLinePaths(new ZipLinePath(90, true, List(Vector3(1310.533f, 963.493f, 110.206f), Vector3(1305.583f, 964.179f, 87.15f))))
      ZipLinePaths(new ZipLinePath(91, false, List(Vector3(1183.282f, 855.405f, 102.024f), Vector3(1020.01f, 874.695f, 163.806f))))
      ZipLinePaths(new ZipLinePath(92, false, List(Vector3(1074.935f, 889.632f, 101.685f), Vector3(1160.119f, 949.134f, 165.832f), Vector3(1165.798f, 953.101f, 166.268f))))
      ZipLinePaths(new ZipLinePath(93, false, List(Vector3(1007.636f, 845.108f, 113.11f), Vector3(1013.388f, 849.192f, 112.44f), Vector3(1053.653f, 877.782f, 102.503f))))
      ZipLinePaths(new ZipLinePath(94, false, List(Vector3(992.283f, 891.888f, 163.798f), Vector3(991.451f, 896.417f, 163.742f), Vector3(985.627f, 928.117f, 113.461f))))
      ZipLinePaths(new ZipLinePath(95, false, List(Vector3(985.317f, 826.497f, 110.014f), Vector3(943.562f, 831.141f, 107.768f))))
      ZipLinePaths(new ZipLinePath(96, false, List(Vector3(1002.807f, 925.681f, 108.385f), Vector3(1046.205f, 892.296f, 102.497f))))
      ZipLinePaths(new ZipLinePath(97, false, List(Vector3(1018.81f, 2027.017f, 92.44f), Vector3(992.566f, 2029.269f, 96.041f), Vector3(927.929f, 2030.134f, 96.384f), Vector3(863.292f, 1998.2f, 96.728f), Vector3(852.6f, 1989.914f, 93.135f))))
      ZipLinePaths(new ZipLinePath(98, false, List(Vector3(816.912f, 1988.393f, 93.135f), Vector3(795.001f, 1943.548f, 95.243f), Vector3(772.69f, 1898.803f, 94.692f), Vector3(753.502f, 1860.322f, 93.135f))))
      ZipLinePaths(new ZipLinePath(99, false, List(Vector3(761.037f, 1809.202f, 93.135f), Vector3(738.964f, 1764.341f, 94.477f), Vector3(725.537f, 1735.63f, 93.135f))))
      ZipLinePaths(new ZipLinePath(100, false, List(Vector3(808.409f, 1678.306f, 92.894f), Vector3(758.409f, 1678.177f, 94.519f), Vector3(742.409f, 1678.136f, 93.135f))))
      ZipLinePaths(new ZipLinePath(101, false, List(Vector3(850.044f, 1661.524f, 93.131f), Vector3(817.963f, 1601.012f, 96.672f), Vector3(817.205f, 1534.27f, 76.708f), Vector3(794.955f, 1487.474f, 75.549f), Vector3(791.099f, 1479.906f, 73.138f))))
      ZipLinePaths(new ZipLinePath(102, false, List(Vector3(849.551f, 1730.123f, 93.131f), Vector3(825.82f, 1775.918f, 94.548f), Vector3(806.79f, 1821.714f, 93.938f), Vector3(800.292f, 1830.693f, 93.135f))))
      ZipLinePaths(new ZipLinePath(103, false, List(Vector3(875f, 1703.396f, 88.662f), Vector3(886.523f, 1699.949f, 88.08f), Vector3(888.484f, 1673.276f, 88.04f), Vector3(900.146f, 1672.403f, 88.525f))))
      ZipLinePaths(new ZipLinePath(104, false, List(Vector3(923.115f, 1671.849f, 88.633f), Vector3(934.566f, 1676.01f, 87.893f), Vector3(939.217f, 1685.07f, 88.753f), Vector3(953.325f, 1687.438f, 88.211f))))
      ZipLinePaths(new ZipLinePath(105, false, List(Vector3(1024.561f, 1703.441f, 88.18f), Vector3(1042.067f, 1704.672f, 84.078f), Vector3(1048.72f, 1715.187f, 83.93f), Vector3(1056.773f, 1719.103f, 83.186f))))
      ZipLinePaths(new ZipLinePath(106, false, List(Vector3(1084.358f, 1704.206f, 78.452f), Vector3(1093.939f, 1707.391f, 78.598f), Vector3(1097.02f, 1731.276f, 78.245f), Vector3(1112.424f, 1742.594f, 79.008f), Vector3(1112.125f, 1762.854f, 78.739f), Vector3(1109.527f, 1770.413f, 73.671f))))
      ZipLinePaths(new ZipLinePath(107, false, List(Vector3(1000.29f, 1625.633f, 98.859f), Vector3(1000.181f, 1617.133f, 103.185f), Vector3(999.672f, 1597.633f, 103.259f), Vector3(984.842f, 1588.277f, 104.119f), Vector3(984.027f, 1574.749f, 107.048f), Vector3(983.612f, 1563.521f, 108.283f))))
      ZipLinePaths(new ZipLinePath(108, false, List(Vector3(996.661f, 1495.754f, 113.22f), Vector3(1021.481f, 1495.402f, 118.224f), Vector3(1035.501f, 1495.85f, 118.262f))))
      ZipLinePaths(new ZipLinePath(109, false, List(Vector3(1036.79f, 1464.458f, 118.185f), Vector3(1018.161f, 1461.497f, 118.106f), Vector3(1016.947f, 1438.117f, 118.333f))))
      ZipLinePaths(new ZipLinePath(110, false, List(Vector3(1070.138f, 1989.771f, 93.138f), Vector3(1069.612f, 1920.429f, 96.74f), Vector3(1070.374f, 1855.956f, 78.424f), Vector3(1086.804f, 1820.319f, 73.139f))))
      ZipLinePaths(new ZipLinePath(111, false, List(Vector3(1117.331f, 1840.065f, 73.142f), Vector3(1183.426f, 1841.149f, 97.117f), Vector3(1215.745f, 1828.174f, 97.599f), Vector3(1232.464f, 1793.299f, 98.381f), Vector3(1191.496f, 1745.11f, 93.142f))))
      ZipLinePaths(new ZipLinePath(112, false, List(Vector3(1134.661f, 1721.993f, 93.137f), Vector3(1134.547f, 1663.691f, 98.085f), Vector3(1135.071f, 1607.797f, 80.745f), Vector3(1140.725f, 1579.552f, 76.669f), Vector3(1191.406f, 1546.395f, 79.469f), Vector3(1216.006f, 1501.982f, 80.37f), Vector3(1225.64f, 1482.405f, 73.137f))))
      ZipLinePaths(new ZipLinePath(113, true, List(Vector3(1228.182f, 1476.191f, 72.635f), Vector3(1222.636f, 1475.527f, 151.831f), Vector3(1141.796f, 1450.049f, 163.334f), Vector3(1095.336f, 1435.407f, 174.611f), Vector3(1048.876f, 1420.764f, 185.888f), Vector3(1013.488f, 1415.158f, 122.539f))))
      ZipLinePaths(new ZipLinePath(114, false, List(Vector3(1251.686f, 1518.522f, 73.137f), Vector3(1276.598f, 1564.053f, 77.383f), Vector3(1294.51f, 1597.684f, 77.847f), Vector3(1293.755f, 1663.148f, 97.995f), Vector3(1303.481f, 1693.585f, 97.716f), Vector3(1337.207f, 1713.821f, 93.137f))))
      ZipLinePaths(new ZipLinePath(115, false, List(Vector3(1394.471f, 1732.147f, 93.138f), Vector3(1394.459f, 1791.418f, 97.651f), Vector3(1426.63f, 1857.554f, 99.082f), Vector3(1426.438f, 1911.439f, 74.996f))))
      ZipLinePaths(new ZipLinePath(116, false, List(Vector3(1379.692f, 1934.355f, 73.137f), Vector3(1313.917f, 1933.904f, 96.93f), Vector3(1251.132f, 1933.473f, 117.324f), Vector3(1186.307f, 1933.425f, 98.423f), Vector3(1130.546f, 1932.646f, 93.137f))))
      ZipLinePaths(new ZipLinePath(117, true, List(Vector3(1025.864f, 1409.726f, 122.407f), Vector3(1070.85f, 1431.534f, 113.042f), Vector3(1208.454f, 1498.242f, 148.385f), Vector3(1244.62f, 1515.774f, 72.635f))))
      ZipLinePaths(new ZipLinePath(118, false, List(Vector3(1202.008f, 297.806f, 113.226f), Vector3(1202.584f, 322.137f, 118.691f), Vector3(1235.36f, 389.367f, 113.135f))))
      ZipLinePaths(new ZipLinePath(119, false, List(Vector3(1242.126f, 432.375f, 113.151f), Vector3(1274.428f, 433.633f, 111.467f), Vector3(1315.73f, 433.691f, 98.599f), Vector3(1376.535f, 434.707f, 78.133f), Vector3(1389.545f, 446.139f, 77.8f), Vector3(1386.464f, 499.904f, 73.151f))))
      ZipLinePaths(new ZipLinePath(120, false, List(Vector3(1131.491f, 241.737f, 113.146f), Vector3(1053.841f, 241.809f, 118.259f), Vector3(1003.713f, 241.856f, 103.067f), Vector3(954.568f, 241.902f, 88.559f), Vector3(924.097f, 241.93f, 73.144f))))
      ZipLinePaths(new ZipLinePath(121, false, List(Vector3(879.356f, 250.391f, 73.135f), Vector3(877.875f, 322.009f, 75.591f), Vector3(865.652f, 333.714f, 76.216f), Vector3(800.368f, 333.08f, 76.774f), Vector3(770.187f, 344.511f, 76.742f), Vector3(753.401f, 374.752f, 73.135f))))
      ZipLinePaths(new ZipLinePath(122, false, List(Vector3(724.372f, 439.993f, 73.135f), Vector3(702.543f, 484.976f, 75.034f), Vector3(695.558f, 499.37f, 73.135f))))
      ZipLinePaths(new ZipLinePath(123, false, List(Vector3(927.581f, 302.259f, 73.066f), Vector3(941.583f, 316.887f, 77.329f), Vector3(941.783f, 375.246f, 79.629f), Vector3(963.228f, 424.3f, 76.137f), Vector3(965.963f, 433.919f, 73.135f))))
      ZipLinePaths(new ZipLinePath(124, false, List(Vector3(1160.627f, 580.379f, 108.277f), Vector3(1161.721f, 570.879f, 108.041f), Vector3(1189.315f, 566.479f, 108.504f), Vector3(1191.699f, 543.808f, 108.218f))))
      ZipLinePaths(new ZipLinePath(125, false, List(Vector3(1207.802f, 538.221f, 113.201f), Vector3(1208.144f, 548.124f, 113.056f), Vector3(1210.215f, 566.825f, 108.333f), Vector3(1234.185f, 567.526f, 113.811f), Vector3(1237.758f, 564.185f, 113.25f))))
      ZipLinePaths(new ZipLinePath(126, false, List(Vector3(1224.007f, 664.636f, 133.221f), Vector3(1223.934f, 652.419f, 133.673f), Vector3(1223.16f, 636.401f, 128.433f), Vector3(1208.114f, 626.266f, 129.353f), Vector3(1208.169f, 613.814f, 123.77f))))
      ZipLinePaths(new ZipLinePath(127, false, List(Vector3(1225.626f, 600.261f, 123.186f), Vector3(1228.418f, 600.63f, 123.819f), Vector3(1251.711f, 599.7f, 118.858f), Vector3(1258.795f, 586.139f, 118.536f), Vector3(1272.363f, 579.017f, 118.191f), Vector3(1271.831f, 575.195f, 118.569f), Vector3(1272.099f, 565.373f, 113.706f))))
      ZipLinePaths(new ZipLinePath(128, false, List(Vector3(1271.691f, 531.861f, 108.485f), Vector3(1271.658f, 509.334f, 108.41f), Vector3(1271.644f, 502.251f, 103.845f))))
      ZipLinePaths(new ZipLinePath(129, false, List(Vector3(1287.978f, 474.746f, 103.686f), Vector3(1288.009f, 467.239f, 107.927f), Vector3(1286.339f, 457.632f, 108.568f), Vector3(1251.901f, 455.918f, 108.65f), Vector3(1242.902f, 456.115f, 108.661f))))
      ZipLinePaths(new ZipLinePath(130, true, List(Vector3(969.919f, 441.728f, 72.635f), Vector3(1009.025f, 473.71f, 80.954f), Vector3(1048.129f, 505.692f, 87.942f), Vector3(1087.232f, 537.674f, 94.93f), Vector3(1126.335f, 569.654f, 101.918f), Vector3(1162.137f, 599.373f, 107.659f))))
      ZipLinePaths(new ZipLinePath(131, true, List(Vector3(1387.976f, 507.419f, 72.636f), Vector3(1385.3f, 510.225f, 74.718f), Vector3(1383.962f, 511.628f, 76.512f), Vector3(1381.955f, 513.733f, 137.228f), Vector3(1381.286f, 514.435f, 137.473f), Vector3(1322.408f, 576.175f, 149.977f), Vector3(1261.523f, 640.021f, 162.48f), Vector3(1227.4f, 675.802f, 174.983f), Vector3(1226.262f, 680.206f, 132.59f))))
      ZipLinePaths(new ZipLinePath(132, true, List(Vector3(1234.923f, 685.242f, 132.733f), Vector3(1267.406f, 649.47f, 121.373f), Vector3(1305.087f, 607.973f, 108.519f), Vector3(1337.569f, 572.201f, 95.665f), Vector3(1377.848f, 527.842f, 82.811f), Vector3(1388.443f, 517.526f, 72.656f))))
      ZipLinePaths(new ZipLinePath(133, true, List(Vector3(1153.869f, 604.137f, 107.796f), Vector3(1116.755f, 572.207f, 101.227f), Vector3(1107.583f, 564.663f, 117.562f), Vector3(1068.605f, 532.605f, 110.2f), Vector3(1030.391f, 501.175f, 102.982f), Vector3(992.177f, 469.747f, 95.765f), Vector3(972.07f, 453.432f, 72.607f))))
      ZipLinePaths(new ZipLinePath(134, true, List(Vector3(694.353f, 506.02f, 72.635f), Vector3(693.408f, 510.898f, 75.838f), Vector3(683.572f, 561.632f, 81.593f), Vector3(673.925f, 611.39f, 87.237f), Vector3(664.279f, 661.148f, 92.88f), Vector3(654.632f, 710.906f, 98.524f), Vector3(644.986f, 760.664f, 104.168f), Vector3(635.339f, 810.422f, 109.812f), Vector3(625.693f, 860.18f, 115.456f), Vector3(620.397f, 887.498f, 134.035f), Vector3(618.538f, 904.381f, 117.681f))))
      ZipLinePaths(new ZipLinePath(135, true, List(Vector3(627.84f, 909.247f, 117.827f), Vector3(636.839f, 859.105f, 112.711f), Vector3(644.838f, 810.063f, 107.141f), Vector3(652.836f, 761.022f, 101.57f), Vector3(660.835f, 711.98f, 96f), Vector3(668.834f, 662.938f, 90.43f), Vector3(676.832f, 613.896f, 84.86f), Vector3(684.831f, 564.855f, 79.29f), Vector3(692.51f, 517.775f, 72.71f))))
      ZipLinePaths(new ZipLinePath(136, false, List(Vector3(861.866f, 1183.37f, 77.95f), Vector3(853.767f, 1165.099f, 77.75f))))
      ZipLinePaths(new ZipLinePath(137, false, List(Vector3(862.428f, 1129.783f, 97.194f), Vector3(986.978f, 1119.254f, 98.247f))))
      ZipLinePaths(new ZipLinePath(138, false, List(Vector3(844.896f, 1130.823f, 97.194f), Vector3(811.383f, 1125.083f, 98.4f))))
      ZipLinePaths(new ZipLinePath(139, false, List(Vector3(851.715f, 1113.668f, 97.231f), Vector3(856.403f, 1096.555f, 90.295f))))
      ZipLinePaths(new ZipLinePath(140, true, List(Vector3(1094.656f, 1028.66f, 85.95f), Vector3(1101.679f, 1029.985f, 119.23f))))
      ZipLinePaths(new ZipLinePath(141, true, List(Vector3(1105.375f, 1032.63f, 119.23f), Vector3(1102.402f, 1030.171f, 85.95f))))
      ZipLinePaths(new ZipLinePath(142, true, List(Vector3(933.254f, 939.081f, 92.25f), Vector3(941.062f, 944.592f, 117.812f))))
      ZipLinePaths(new ZipLinePath(143, true, List(Vector3(935.9f, 940.753f, 117.811f), Vector3(938.783f, 942.43f, 92.25f))))
      ZipLinePaths(new ZipLinePath(144, false, List(Vector3(872.334f, 1048.719f, 90.263f), Vector3(933.133f, 967.339f, 92.651f))))
      ZipLinePaths(new ZipLinePath(145, false, List(Vector3(982.409f, 729.272f, 86.85f), Vector3(992.811f, 822.687f, 88.85f))))
      ZipLinePaths(new ZipLinePath(146, true, List(Vector3(922.643f, 850.812f, 96.109f), Vector3(909.595f, 831.634f, 113.284f))))
      ZipLinePaths(new ZipLinePath(147, true, List(Vector3(929.96f, 861.944f, 117.749f), Vector3(868.055f, 887.015f, 98.609f))))
      ZipLinePaths(new ZipLinePath(148, true, List(Vector3(1120.997f, 1115.475f, 110.069f), Vector3(1119.491f, 1123.132f, 126.696f), Vector3(1117.841f, 1135.263f, 129.177f))))
      ZipLinePaths(new ZipLinePath(149, true, List(Vector3(1155.227f, 1098.523f, 123.089f), Vector3(1169.031f, 1159.746f, 112.569f))))
      ZipLinePaths(new ZipLinePath(150, false, List(Vector3(1004.971f, 1143.408f, 111.496f), Vector3(880.245f, 1250.32f, 95.892f))))
      ZipLinePaths(new ZipLinePath(151, true, List(Vector3(1019.69f, 1007.623f, 115.693f), Vector3(1000.005f, 1001.177f, 166.13f))))
      ZipLinePaths(new ZipLinePath(152, true, List(Vector3(1050.232f, 1000.81f, 166.13f), Vector3(1021.86f, 1002.474f, 115.693f))))
      ZipLinePaths(new ZipLinePath(153, false, List(Vector3(1891.956f, 942.414f, 33.135f), Vector3(1821.932f, 977.7f, 35.084f), Vector3(1811.37f, 987.494f, 36.308f), Vector3(1778.408f, 1055.287f, 37.532f), Vector3(1776.673f, 1083.246f, 33.135f))))
      ZipLinePaths(new ZipLinePath(154, false, List(Vector3(1742.077f, 1117.64f, 33.158f), Vector3(1743.128f, 1166.486f, 50.314f), Vector3(1742.632f, 1178.937f, 52.387f))))
      ZipLinePaths(new ZipLinePath(155, false, List(Vector3(1730.829f, 1199.918f, 53.138f), Vector3(1665.225f, 1198.474f, 77.439f), Vector3(1599.621f, 1197.031f, 76.243f), Vector3(1552.051f, 1172.147f, 73.138f))))
      ZipLinePaths(new ZipLinePath(156, true, List(Vector3(1539.981f, 1165.92f, 72.635f), Vector3(1535.523f, 1163.592f, 75.147f), Vector3(1515.908f, 1154.668f, 145.491f), Vector3(1470.436f, 1133.98f, 155.771f), Vector3(1425.856f, 1113.698f, 165.85f), Vector3(1386.517f, 1096.256f, 107.574f))))
      ZipLinePaths(new ZipLinePath(157, false, List(Vector3(952.148f, 897.258f, 92.851f), Vector3(922.937f, 946.221f, 176.437f), Vector3(920.99f, 949.486f, 176.89f))))
      ZipLinePaths(new ZipLinePath(158, false, List(Vector3(932.443f, 806.082f, 92.651f), Vector3(991.259f, 862.472f, 163.374f), Vector3(993.156f, 864.292f, 163.797f))))
      ZipLinePaths(new ZipLinePath(159, false, List(Vector3(879.015f, 930.909f, 92.708f), Vector3(932.22f, 1092.636f, 178.167f))))
      ZipLinePaths(new ZipLinePath(160, false, List(Vector3(1243f, 987.886f, 99.197f), Vector3(1174.26f, 1066.841f, 107.552f))))
      ZipLinePaths(new ZipLinePath(161, false, List(Vector3(1103.05f, 1078.253f, 107.708f), Vector3(1180.178f, 992.405f, 165.77f), Vector3(1182.666f, 989.636f, 166.342f))))
      ZipLinePaths(new ZipLinePath(162, false, List(Vector3(722.901f, 1218.822f, 103.265f), Vector3(912.849f, 1117.522f, 178.139f))))
      ZipLinePaths(new ZipLinePath(163, false, List(Vector3(1331.164f, 1158.065f, 90.792f), Vector3(1248.261f, 1136.533f, 99.005f))))
      ZipLinePaths(new ZipLinePath(164, true, List(Vector3(1210.313f, 1084.792f, 100.121f), Vector3(1199.343f, 1073.476f, 106.5f), Vector3(1196.969f, 1072.153f, 106.85f))))
      ZipLinePaths(new ZipLinePath(165, true, List(Vector3(1220.939f, 1186.566f, 98.502f), Vector3(1213.099f, 1184.104f, 106.75f))))
      ZipLinePaths(new ZipLinePath(166, true, List(Vector3(1172.896f, 1233.173f, 103.636f), Vector3(1162.954f, 1227.012f, 106.95f))))
      ZipLinePaths(new ZipLinePath(167, true, List(Vector3(1199.205f, 1200.656f, 103.636f), Vector3(1196.948f, 1196.556f, 106.95f))))
      ZipLinePaths(new ZipLinePath(168, true, List(Vector3(1222.933f, 1107.801f, 98.138f), Vector3(1216.995f, 1100.217f, 100.121f))))
      ZipLinePaths(new ZipLinePath(169, true, List(Vector3(884.118f, 939.871f, 85.012f), Vector3(877.122f, 945.369f, 86.745f))))
      ZipLinePaths(new ZipLinePath(170, true, List(Vector3(835.184f, 956.37f, 82.33f), Vector3(845.422f, 955.435f, 86.745f))))
      ZipLinePaths(new ZipLinePath(171, true, List(Vector3(798.088f, 933.249f, 82.33f), Vector3(801.227f, 929.672f, 85.391f))))
      ZipLinePaths(new ZipLinePath(172, true, List(Vector3(817.542f, 872.74f, 85.391f), Vector3(821.367f, 877.892f, 91.804f), Vector3(824.646f, 882.308f, 92.154f))))
      ZipLinePaths(new ZipLinePath(173, true, List(Vector3(809.042f, 966.006f, 72.637f), Vector3(806.015f, 962.745f, 81.98f), Vector3(804.286f, 960.882f, 82.33f))))
      ZipLinePaths(new ZipLinePath(174, true, List(Vector3(782.321f, 946.101f, 72.637f), Vector3(782.209f, 943.098f, 81.216f), Vector3(782.096f, 940.095f, 82.33f))))
      ZipLinePaths(new ZipLinePath(175, true, List(Vector3(771.194f, 921.372f, 72.637f), Vector3(773.573f, 922.573f, 78.9f), Vector3(775.951f, 923.774f, 79.25f))))
      ZipLinePaths(new ZipLinePath(176, true, List(Vector3(785.799f, 928.492f, 79.25f), Vector3(787.866f, 933.334f, 82.33f))))
      ZipLinePaths(new ZipLinePath(177, true, List(Vector3(773.105f, 877.692f, 72.637f), Vector3(776f, 883.044f, 85.041f), Vector3(776.762f, 884.99f, 85.391f))))
      ZipLinePaths(new ZipLinePath(178, true, List(Vector3(1260.095f, 1122.392f, 72.637f), Vector3(1248.111f, 1131.053f, 98.152f), Vector3(1246.528f, 1132.553f, 98.502f))))
      ZipLinePaths(new ZipLinePath(179, true, List(Vector3(1247.407f, 1189.7f, 72.637f), Vector3(1242.866f, 1188.597f, 85.416f), Vector3(1237.21f, 1185.933f, 98.152f), Vector3(1235.197f, 1185.562f, 98.502f))))
      ZipLinePaths(new ZipLinePath(180, true, List(Vector3(1227.751f, 1204.986f, 85.766f), Vector3(1230.308f, 1198.845f, 97.468f), Vector3(1231.522f, 1197.036f, 98.502f))))
      ZipLinePaths(new ZipLinePath(181, true, List(Vector3(1217.513f, 1218.632f, 85.766f), Vector3(1214.723f, 1224.759f, 89.205f))))
      ZipLinePaths(new ZipLinePath(182, true, List(Vector3(1229.038f, 1246.627f, 72.637f), Vector3(1230.799f, 1238.957f, 85.421f), Vector3(1231.386f, 1236.401f, 85.767f))))
      ZipLinePaths(new ZipLinePath(183, true, List(Vector3(1150.235f, 1233.13f, 89.205f), Vector3(1145.005f, 1230.417f, 91.211f))))
      ZipLinePaths(new ZipLinePath(184, true, List(Vector3(1143.722f, 1247.649f, 72.637f), Vector3(1150.984f, 1246.704f, 88.641f), Vector3(1154.614f, 1246.232f, 89.205f))))
      ZipLinePaths(new ZipLinePath(185, true, List(Vector3(1093.149f, 1249.843f, 72.637f), Vector3(1092.856f, 1240.249f, 90.861f), Vector3(1092.764f, 1237.251f, 91.211f))))
      ZipLinePaths(new ZipLinePath(186, true, List(Vector3(1042.911f, 1261.351f, 72.637f), Vector3(1044.372f, 1257.098f, 80f), Vector3(1049.484f, 1242.212f, 91.146f))))
      ZipLinePaths(new ZipLinePath(187, true, List(Vector3(837.728f, 828.382f, 82.742f), Vector3(840.4f, 833.819f, 91.804f), Vector3(841.491f, 837.298f, 92.154f))))
      ZipLinePaths(new ZipLinePath(188, true, List(Vector3(873.715f, 759.255f, 84.319f), Vector3(867.275f, 761.983f, 86.937f))))
      ZipLinePaths(new ZipLinePath(189, true, List(Vector3(814.836f, 852.508f, 72.637f), Vector3(823.482f, 854.756f, 91.804f), Vector3(825.644f, 855.318f, 92.154f))))
      ZipLinePaths(new ZipLinePath(190, true, List(Vector3(901.862f, 755.126f, 72.637f), Vector3(903.995f, 759.861f, 83.969f), Vector3(905.077f, 762.454f, 84.319f))))
      ZipLinePaths(new ZipLinePath(191, true, List(Vector3(836.436f, 790.696f, 72.637f), Vector3(836.032f, 796.342f, 82.392f), Vector3(835.897f, 798.223f, 82.742f))))
      ZipLinePaths(new ZipLinePath(192, true, List(Vector3(842.566f, 807.217f, 82.742f), Vector3(845.266f, 803.584f, 86.394f), Vector3(846.346f, 802.131f, 86.937f))))
      ZipLinePaths(new ZipLinePath(193, true, List(Vector3(840.797f, 960.721f, 72.637f), Vector3(837.499f, 957.71f, 81.98f), Vector3(830.352f, 951.186f, 86.745f))))
      ZipLinePaths(new ZipLinePath(194, true, List(Vector3(861.487f, 756.065f, 72.637f), Vector3(861.344f, 761.414f, 86.484f), Vector3(861.273f, 764.088f, 86.937f))))
      ZipLinePaths(new ZipLinePath(195, false, List(Vector3(1064.899f, 1048.02f, 101.15f), Vector3(1120.375f, 956.607f, 179.814f), Vector3(1122.165f, 953.658f, 180.287f))))
      ZipLinePaths(new ZipLinePath(196, false, List(Vector3(1163.953f, 891.526f, 94.91f), Vector3(1188.363f, 882.792f, 92.952f))))
      ZipLinePaths(new ZipLinePath(197, true, List(Vector3(701.124f, 1230.701f, 85.32f), Vector3(696.653f, 1222.976f, 102.746f))))
      ZipLinePaths(new ZipLinePath(198, true, List(Vector3(1393.029f, 1101.697f, 107.755f), Vector3(1438.339f, 1122.279f, 97.577f), Vector3(1452.554f, 1128.736f, 156.164f), Vector3(1497.864f, 1149.318f, 148.448f), Vector3(1528.959f, 1163.443f, 72.797f))))
      ZipLinePaths(new ZipLinePath(199, false, List(Vector3(306.221f, 763.282f, 73.135f), Vector3(306.698f, 800.603f, 76.521f), Vector3(340.381f, 872.043f, 73.135f))))
      ZipLinePaths(new ZipLinePath(200, false, List(Vector3(345.801f, 912.178f, 73.135f), Vector3(363.505f, 919.044f, 73.324f), Vector3(381.408f, 931.811f, 74.014f), Vector3(408.416f, 942.444f, 75.392f), Vector3(429.031f, 949.809f, 75.579f), Vector3(455.238f, 969.842f, 75.672f), Vector3(481.246f, 977.475f, 75.765f), Vector3(528.556f, 979.943f, 73.135f))))
      ZipLinePaths(new ZipLinePath(201, true, List(Vector3(539.196f, 980.064f, 72.635f), Vector3(574.053f, 948.591f, 93.851f), Vector3(608.91f, 917.118f, 113.732f), Vector3(618.311f, 910.212f, 117.698f))))
      ZipLinePaths(new ZipLinePath(202, true, List(Vector3(607.495f, 909.335f, 117.666f), Vector3(578.291f, 944.643f, 96.61f), Vector3(549.087f, 979.952f, 72.716f))))
      ZipLinePaths(new ZipLinePath(203, false, List(Vector3(614.076f, 918.037f, 118.092f), Vector3(648.718f, 955.203f, 114.812f), Vector3(682.681f, 991.641f, 110.851f))))
      ZipLinePaths(new ZipLinePath(204, false, List(Vector3(716.336f, 1022.474f, 110.856f), Vector3(755.976f, 1089.243f, 98.406f))))
      ZipLinePaths(new ZipLinePath(205, false, List(Vector3(730.061f, 994.571f, 110.85f), Vector3(808.623f, 1005.013f, 164.303f), Vector3(813.861f, 1005.709f, 164.56f))))
      ZipLinePaths(new ZipLinePath(206, true, List(Vector3(1021.601f, 1075.097f, 91.155f), Vector3(1015.346f, 1066.557f, 97.276f))))
      ZipLinePaths(new ZipLinePath(207, true, List(Vector3(998.898f, 1042.539f, 97.276f), Vector3(995.256f, 1040.166f, 101.431f), Vector3(993.799f, 1039.216f, 101.794f))))
      ZipLinePaths(new ZipLinePath(208, true, List(Vector3(987.398f, 1053.149f, 91.156f), Vector3(994.469f, 1055.576f, 97.276f))))
      ZipLinePaths(new ZipLinePath(209, true, List(Vector3(1056.106f, 1034.411f, 91.25f), Vector3(1051.638f, 1035.824f, 96.926f), Vector3(1049.649f, 1036.895f, 97.276f))))
      ZipLinePaths(new ZipLinePath(210, true, List(Vector3(979.786f, 1011.902f, 101.811f), Vector3(980.707f, 1006.752f, 107.616f), Vector3(981.167f, 1004.177f, 107.966f))))
      ZipLinePaths(new ZipLinePath(211, true, List(Vector3(960.739f, 1004.397f, 92.05f), Vector3(966.398f, 997.912f, 101.464f), Vector3(967.427f, 996.733f, 101.82f))))
      ZipLinePaths(new ZipLinePath(212, true, List(Vector3(986.563f, 956.368f, 89.55f), Vector3(983.367f, 961.629f, 101.444f), Vector3(982.204f, 963.542f, 101.794f))))
      ZipLinePaths(new ZipLinePath(213, true, List(Vector3(983.637f, 981.525f, 101.794f), Vector3(989.245f, 975.315f, 107.966f))))
      ZipLinePaths(new ZipLinePath(214, true, List(Vector3(988.599f, 996.118f, 107.966f), Vector3(991.802f, 995.824f, 114.824f), Vector3(994.365f, 995.59f, 115.693f))))
      ZipLinePaths(new ZipLinePath(215, true, List(Vector3(1066.595f, 1027.52f, 91.25f), Vector3(1069.864f, 1017.647f, 96.926f), Vector3(1071.349f, 1013.159f, 97.276f))))
      ZipLinePaths(new ZipLinePath(216, true, List(Vector3(1074.69f, 990.196f, 97.276f), Vector3(1071.034f, 992.737f, 104.342f), Vector3(1068.596f, 994.432f, 104.692f))))
      ZipLinePaths(new ZipLinePath(217, true, List(Vector3(1091.584f, 998.45f, 89.35f), Vector3(1087.866f, 993.893f, 96.728f), Vector3(1086.472f, 992.184f, 97.276f))))
      ZipLinePaths(new ZipLinePath(218, true, List(Vector3(1068.438f, 954.245f, 89.351f), Vector3(1062.46f, 953.254f, 96.926f), Vector3(1060.965f, 953.006f, 97.276f))))
      ZipLinePaths(new ZipLinePath(219, true, List(Vector3(1066.35f, 973.397f, 97.276f), Vector3(1058.251f, 973.884f, 104.692f))))
      ZipLinePaths(new ZipLinePath(220, true, List(Vector3(1032.003f, 936.282f, 89.55f), Vector3(1038.859f, 936.207f, 96.926f), Vector3(1042.287f, 936.169f, 97.276f))))
      ZipLinePaths(new ZipLinePath(221, true, List(Vector3(1019.28f, 950.5f, 89.55f), Vector3(1026.949f, 951.393f, 97.276f))))
      ZipLinePaths(new ZipLinePath(222, true, List(Vector3(1042.968f, 954.078f, 97.276f), Vector3(1044.525f, 958.776f, 104.342f), Vector3(1045.563f, 961.908f, 104.692f))))
      ZipLinePaths(new ZipLinePath(223, true, List(Vector3(1043.88f, 975.722f, 104.692f), Vector3(1041.782f, 981.626f, 115.343f), Vector3(1041.083f, 983.594f, 115.693f))))
      ZipLinePaths(new ZipLinePath(224, true, List(Vector3(1058.822f, 1006.575f, 104.692f), Vector3(1054.133f, 1008.733f, 115.343f), Vector3(1052.375f, 1009.542f, 115.693f))))
      ZipLinePaths(new ZipLinePath(225, true, List(Vector3(1001.752f, 1027.373f, 107.966f), Vector3(1004.216f, 1023.209f, 115.343f), Vector3(1005.42f, 1021.419f, 115.693f))))
      ZipLinePaths(new ZipLinePath(226, false, List(Vector3(1391.743f, 1080.478f, 108.201f), Vector3(1398.365f, 1079.972f, 107.639f), Vector3(1412.786f, 1079.466f, 102.885f), Vector3(1416.622f, 1072.662f, 103.533f), Vector3(1416.958f, 1050.859f, 97.981f), Vector3(1430.894f, 1044.656f, 98.029f), Vector3(1431.862f, 1036.555f, 97.553f), Vector3(1432.946f, 1018.004f, 93.115f), Vector3(1440.43f, 1017.453f, 93.186f))))
      ZipLinePaths(new ZipLinePath(227, false, List(Vector3(1519.21f, 999.864f, 83.184f), Vector3(1540.844f, 1000.889f, 78.393f), Vector3(1548.161f, 1015.301f, 78.498f), Vector3(1558.54f, 1015.504f, 77.025f), Vector3(1568.92f, 1015.707f, 73.351f), Vector3(1576.299f, 1006.71f, 73.177f), Vector3(1566.244f, 999.461f, 73.309f), Vector3(1560.188f, 992.212f, 73.44f), Vector3(1560.333f, 976.463f, 69.522f), Vector3(1566.478f, 967.713f, 68.203f))))
      ZipLinePaths(new ZipLinePath(228, false, List(Vector3(1626.306f, 967.026f, 58.235f), Vector3(1639.898f, 967.223f, 61.771f), Vector3(1653.49f, 967.419f, 63.507f), Vector3(1655.783f, 957.315f, 62.743f), Vector3(1657.975f, 938.211f, 58.578f), Vector3(1671.203f, 932.356f, 58.341f), Vector3(1673.016f, 905.329f, 53.772f), Vector3(1688.03f, 904.501f, 53.203f))))
      ZipLinePaths(new ZipLinePath(229, false, List(Vector3(1711.197f, 924.758f, 53.139f), Vector3(1710.369f, 942.404f, 54.049f), Vector3(1709.94f, 990.049f, 36.063f), Vector3(1740.884f, 1044.941f, 35.233f), Vector3(1742.134f, 1084.117f, 33.139f))))
      ZipLinePaths(new ZipLinePath(230, false, List(Vector3(1897.005f, 909.845f, 33.139f), Vector3(1890.302f, 910.029f, 36.637f), Vector3(1877.651f, 905.72f, 35.937f), Vector3(1847.499f, 885.412f, 36.038f), Vector3(1832.994f, 880.879f, 36.882f), Vector3(1761.44f, 877.355f, 56.523f), Vector3(1741.073f, 885.183f, 54.579f), Vector3(1724.506f, 898.41f, 53.139f))))
      ZipLinePaths(new ZipLinePath(231, false, List(Vector3(607.175f, 888.218f, 118.163f), Vector3(570.208f, 886.839f, 108.58f), Vector3(566.924f, 874.849f, 108.339f), Vector3(553.182f, 869.605f, 108.219f), Vector3(552.912f, 851.132f, 112.959f), Vector3(552.641f, 832.66f, 113.199f))))
      ZipLinePaths(new ZipLinePath(232, false, List(Vector3(551.781f, 772.243f, 103.542f), Vector3(550.899f, 762.266f, 103.537f), Vector3(523.357f, 759.878f, 102.185f), Vector3(508.587f, 758.684f, 98.309f), Vector3(501.216f, 744.69f, 98.033f), Vector3(481.289f, 744.323f, 98.193f))))
      ZipLinePaths(new ZipLinePath(233, false, List(Vector3(435.564f, 775.856f, 83.45f), Vector3(410.779f, 775.127f, 83.255f), Vector3(405.683f, 761.182f, 83.307f), Vector3(378.802f, 758.763f, 83.249f), Vector3(372.661f, 744.254f, 83.519f), Vector3(361.876f, 744.126f, 82.212f), Vector3(351.091f, 743.999f, 78.805f), Vector3(330.306f, 745.471f, 73.448f), Vector3(329.521f, 752.744f, 73.19f))))
      ZipLinePaths(new ZipLinePath(234, false, List(Vector3(694.79f, 533.179f, 73.154f), Vector3(704.33f, 582.022f, 88.963f), Vector3(713.871f, 630.865f, 102.305f), Vector3(723.412f, 679.708f, 113.447f), Vector3(732.953f, 728.55f, 119.589f), Vector3(734.636f, 737.17f, 115.86f))))
      ZipLinePaths(new ZipLinePath(235, false, List(Vector3(1227.559f, 1464.788f, 73.065f), Vector3(1204.683f, 1422.484f, 98.524f), Vector3(1181.807f, 1380.181f, 111.615f), Vector3(1170.369f, 1359.029f, 112.1f), Vector3(1167.167f, 1353.107f, 110.872f))))
      ZipLinePaths(new ZipLinePath(236, false, List(Vector3(1151.041f, 1307.142f, 110.85f), Vector3(1149.808f, 1257.197f, 109.965f), Vector3(1148.896f, 1220.238f, 107.451f))))
      ZipLinePaths(new ZipLinePath(237, false, List(Vector3(1119.158f, 1351.924f, 110.853f), Vector3(1075.835f, 1376.326f, 117.478f), Vector3(1032.512f, 1400.728f, 122.747f), Vector3(1025.581f, 1404.632f, 122.843f))))
      ZipLinePaths(new ZipLinePath(238, false, List(Vector3(1120.059f, 1320.54f, 110.862f), Vector3(1095.096f, 1278.566f, 101.21f), Vector3(1074.626f, 1244.147f, 91.658f))))
      ZipLinePaths(new ZipLinePath(239, false, List(Vector3(1168.393f, 1314.06f, 110.852f), Vector3(1209.694f, 1284.47f, 109.355f), Vector3(1250.994f, 1254.88f, 104.931f), Vector3(1292.295f, 1225.29f, 100.507f), Vector3(1297.964f, 1221.228f, 96.852f))))
      ZipLinePaths(new ZipLinePath(240, false, List(Vector3(1520.99f, 1163.069f, 73.227f), Vector3(1471.094f, 1163.271f, 87.591f), Vector3(1421.197f, 1163.472f, 92.633f), Vector3(1385.272f, 1163.617f, 84.199f))))
      ZipLinePaths(new ZipLinePath(241, false, List(Vector3(1352.209f, 1198.899f, 87.497f), Vector3(1352.861f, 1204.463f, 96.7f), Vector3(1352.762f, 1206.433f, 96.85f))))
      ZipLinePaths(new ZipLinePath(242, false, List(Vector3(1370.127f, 1176.595f, 84.198f), Vector3(1368.367f, 1178.316f, 87.247f), Vector3(1366.677f, 1179.496f, 87.498f))))
      ZipLinePaths(new ZipLinePath(243, false, List(Vector3(1132.846f, 1216.479f, 91.711f), Vector3(1142.058f, 1210.391f, 107.329f), Vector3(1143.9f, 1209.173f, 107.451f))))
      ZipLinePaths(new ZipLinePath(244, false, List(Vector3(1078.17f, 1148.271f, 91.351f), Vector3(1090.02f, 1146.1f, 107.634f), Vector3(1091.414f, 1145.844f, 107.751f))))
      ZipLinePaths(new ZipLinePath(245, false, List(Vector3(1081.421f, 1072.106f, 101.202f), Vector3(1088.084f, 1079.606f, 107.87f), Vector3(1089.901f, 1081.652f, 107.702f))))
      ZipLinePaths(new ZipLinePath(246, false, List(Vector3(1136.448f, 1054.578f, 91.95f), Vector3(1141.153f, 1061.864f, 107.3f), Vector3(1143.254f, 1063.402f, 107.55f))))
      ZipLinePaths(new ZipLinePath(247, false, List(Vector3(1390.277f, 526.009f, 73.06f), Vector3(1380.297f, 575.581f, 84.344f), Vector3(1370.318f, 625.152f, 92.475f), Vector3(1360.338f, 674.723f, 99.805f), Vector3(1350.358f, 724.294f, 104.935f), Vector3(1344.879f, 751.51f, 103.84f))))
      ZipLinePaths(new ZipLinePath(248, false, List(Vector3(971.093f, 460.616f, 73.02f), Vector3(987.394f, 508.286f, 87.196f), Vector3(1003.696f, 555.955f, 98.121f), Vector3(1019.998f, 603.624f, 102.646f), Vector3(1026.39f, 622.318f, 100.918f))))
      ZipLinePaths(new ZipLinePath(249, false, List(Vector3(763.983f, 784.294f, 115.871f), Vector3(792.938f, 823.826f, 131.416f), Vector3(821.893f, 863.359f, 145.548f), Vector3(850.849f, 902.891f, 159.679f), Vector3(879.804f, 942.424f, 173.81f), Vector3(880.939f, 943.974f, 174.096f))))
      ZipLinePaths(new ZipLinePath(250, false, List(Vector3(749.571f, 785.971f, 115.85f), Vector3(714.337f, 822.839f, 117.331f), Vector3(679.103f, 859.707f, 117.868f), Vector3(643.87f, 896.575f, 118.406f), Vector3(628.671f, 912.479f, 118.29f))))
      ZipLinePaths(new ZipLinePath(251, false, List(Vector3(779.486f, 745.268f, 115.853f), Vector3(827.456f, 732.543f, 111.557f), Vector3(875.427f, 719.817f, 105.484f), Vector3(923.398f, 707.091f, 99.411f), Vector3(952.18f, 699.456f, 95.173f))))
      ZipLinePaths(new ZipLinePath(252, false, List(Vector3(780.058f, 775.855f, 115.868f), Vector3(787.884f, 785.361f, 116.604f), Vector3(811.364f, 813.881f, 108.464f), Vector3(834.689f, 842.214f, 92.672f))))
      ZipLinePaths(new ZipLinePath(253, false, List(Vector3(555.581f, 981.167f, 73.266f), Vector3(603.655f, 991.018f, 94.61f), Vector3(651.729f, 1000.87f, 107.893f), Vector3(684.721f, 1007.632f, 110.87f))))
      ZipLinePaths(new ZipLinePath(254, false, List(Vector3(702.522f, 1030.162f, 110.85f), Vector3(699.16f, 1081.037f, 109.82f), Vector3(695.798f, 1131.913f, 108.875f), Vector3(695.205f, 1140.891f, 107.779f))))
      ZipLinePaths(new ZipLinePath(255, false, List(Vector3(696.454f, 1171.992f, 107.781f), Vector3(715.169f, 1212.74f, 103.239f))))
      ZipLinePaths(new ZipLinePath(256, false, List(Vector3(787.305f, 1465.731f, 73.517f), Vector3(781.021f, 1442.654f, 86.618f), Vector3(774.737f, 1419.577f, 95.82f), Vector3(770.59f, 1404.345f, 99.458f), Vector3(766.443f, 1389.114f, 99.697f))))
      ZipLinePaths(new ZipLinePath(257, false, List(Vector3(728.914f, 969.705f, 110.895f), Vector3(739.989f, 966.686f, 110.459f), Vector3(790.937f, 952.799f, 82.875f))))
      ZipLinePaths(new ZipLinePath(258, false, List(Vector3(1302.013f, 1185.204f, 96.853f), Vector3(1252.343f, 1186.784f, 102.446f), Vector3(1205.652f, 1188.27f, 107.253f))))
      ZipLinePaths(new ZipLinePath(259, false, List(Vector3(1078.239f, 1125.433f, 107.751f), Vector3(1035.593f, 1046.301f, 176.325f), Vector3(1032.75f, 1041.026f, 176.63f))))
      ZipLinePaths(new ZipLinePath(260, false, List(Vector3(1168.453f, 1061.073f, 107.351f), Vector3(1236.382f, 1044.695f, 175.95f), Vector3(1240.911f, 1043.603f, 176.79f))))
      ZipLinePaths(new ZipLinePath(261, false, List(Vector3(1031.964f, 828.333f, 110.751f), Vector3(1082.946f, 827.829f, 109.239f), Vector3(1133.927f, 827.324f, 108.222f), Vector3(1184.908f, 826.82f, 107.205f), Vector3(1197.904f, 826.692f, 107.49f))))
      ZipLinePaths(new ZipLinePath(262, false, List(Vector3(1337.353f, 851.122f, 83.075f), Vector3(1219.221f, 945.439f, 165.999f), Vector3(1217.346f, 946.937f, 166.325f))))
      ZipLinePaths(new ZipLinePath(263, false, List(Vector3(1284.797f, 766.939f, 81.981f), Vector3(1234.37f, 700.07f, 132.568f), Vector3(1231.008f, 695.612f, 132.959f))))
      ZipLinePaths(new ZipLinePath(264, false, List(Vector3(1330.746f, 755.47f, 103.852f), Vector3(1290.194f, 728.822f, 116.781f), Vector3(1249.642f, 702.174f, 128.861f), Vector3(1235.044f, 692.581f, 133.139f))))
      ZipLinePaths(new ZipLinePath(265, false, List(Vector3(1218.624f, 694.117f, 133.015f), Vector3(1214.347f, 744.806f, 129.669f), Vector3(1210.07f, 795.495f, 126.036f), Vector3(1206.463f, 838.232f, 121.775f))))
      ZipLinePaths(new ZipLinePath(266, false, List(Vector3(704.794f, 1142.453f, 107.813f), Vector3(817.239f, 1046.672f, 164.281f), Vector3(819.024f, 1045.152f, 164.594f))))
      ZipLinePaths(new ZipLinePath(267, false, List(Vector3(690.815f, 1208.531f, 83.177f), Vector3(690.381f, 1191.88f, 101.623f), Vector3(690.055f, 1179.392f, 107.984f), Vector3(689.946f, 1175.229f, 107.779f))))
      ZipLinePaths(new ZipLinePath(268, false, List(Vector3(758.955f, 1305.325f, 80.95f), Vector3(763.554f, 1322.557f, 99.347f), Vector3(764.154f, 1325.222f, 99.671f))))
      ZipLinePaths(new ZipLinePath(269, false, List(Vector3(799.991f, 1259.203f, 92.603f), Vector3(910.702f, 1128.801f, 178.363f), Vector3(918.083f, 1120.108f, 178.162f))))
      ZipLinePaths(new ZipLinePath(270, false, List(Vector3(1034.138f, 973.675f, 116.193f), Vector3(1105.112f, 948.65f, 185.221f), Vector3(1107.402f, 947.843f, 185.796f))))
      ZipLinePaths(new ZipLinePath(271, false, List(Vector3(1010.579f, 1029.295f, 116.193f), Vector3(941.146f, 1096.884f, 177.675f), Vector3(938.906f, 1099.064f, 178.11f))))
      ZipLinePaths(new ZipLinePath(272, false, List(Vector3(988.457f, 921.752f, 112.454f), Vector3(993.639f, 872.029f, 111.752f), Vector3(994.158f, 867.057f, 111.196f))))
      ZipLinePaths(new ZipLinePath(273, false, List(Vector3(976.528f, 927.49f, 114.256f), Vector3(941.289f, 893.713f, 109.631f))))
    }

    ZipLines()

  }
}
