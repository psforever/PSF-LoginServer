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

object Ugd05 { // Annwn
  val ZoneMap = new ZoneMap("ugd05") {
    Scale = MapScale.Dim2048
    Checksum = 1769572498L

    Building10116()

    def Building10116(): Unit = { // Name: ceiling_bldg_a_10116 Type: ceiling_bldg_a GUID: 1, MapID: 10116
      LocalBuilding("ceiling_bldg_a_10116", 1, 10116, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1287.36f, 953.39f, 272.03f), Vector3(0f, 0f, 279f), ceiling_bldg_a)))
      LocalObject(478, Door.Constructor(Vector3(1278.85f, 936.597f, 279.315f)), owning_building_guid = 1)
      LocalObject(479, Door.Constructor(Vector3(1292.775f, 968.4059f, 273.809f)), owning_building_guid = 1)
      LocalObject(480, Door.Constructor(Vector3(1293.9f, 937.462f, 273.809f)), owning_building_guid = 1)
      LocalObject(481, Door.Constructor(Vector3(1302.555f, 940.3514f, 279.315f)), owning_building_guid = 1)
    }

    Building10014()

    def Building10014(): Unit = { // Name: ceiling_bldg_b_10014 Type: ceiling_bldg_b GUID: 2, MapID: 10014
      LocalBuilding("ceiling_bldg_b_10014", 2, 10014, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(978.8f, 980.41f, 242.69f), Vector3(0f, 0f, 317f), ceiling_bldg_b)))
      LocalObject(391, Door.Constructor(Vector3(982.511f, 975.5684f, 244.469f)), owning_building_guid = 2)
      LocalObject(396, Door.Constructor(Vector3(991.5198f, 991.0958f, 244.469f)), owning_building_guid = 2)
    }

    Building10002()

    def Building10002(): Unit = { // Name: ceiling_bldg_b_10002 Type: ceiling_bldg_b GUID: 3, MapID: 10002
      LocalBuilding("ceiling_bldg_b_10002", 3, 10002, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1192.36f, 1130.14f, 256.99f), Vector3(0f, 0f, 0f), ceiling_bldg_b)))
      LocalObject(445, Door.Constructor(Vector3(1194.375f, 1146.63f, 258.769f)), owning_building_guid = 3)
      LocalObject(448, Door.Constructor(Vector3(1198.376f, 1129.13f, 258.769f)), owning_building_guid = 3)
    }

    Building10004()

    def Building10004(): Unit = { // Name: ceiling_bldg_d_10004 Type: ceiling_bldg_d GUID: 4, MapID: 10004
      LocalBuilding("ceiling_bldg_d_10004", 4, 10004, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1208.22f, 913.48f, 275.19f), Vector3(0f, 0f, 185f), ceiling_bldg_d)))
      LocalObject(442, Door.Constructor(Vector3(1190.778f, 911.9379f, 276.925f)), owning_building_guid = 4)
      LocalObject(450, Door.Constructor(Vector3(1206.678f, 930.9219f, 276.925f)), owning_building_guid = 4)
      LocalObject(452, Door.Constructor(Vector3(1209.728f, 896.0551f, 276.925f)), owning_building_guid = 4)
      LocalObject(462, Door.Constructor(Vector3(1225.645f, 914.9884f, 276.925f)), owning_building_guid = 4)
      LocalObject(506, Painbox.Constructor(Vector3(1208.295f, 913.6905f, 283.498f), painbox_continuous), owning_building_guid = 4)
    }

    Building10036()

    def Building10036(): Unit = { // Name: ceiling_bldg_g_10036 Type: ceiling_bldg_g GUID: 5, MapID: 10036
      LocalBuilding("ceiling_bldg_g_10036", 5, 10036, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1142.28f, 928.25f, 402.85f), Vector3(0f, 0f, 0f), ceiling_bldg_g)))
      LocalObject(430, Door.Constructor(Vector3(1134.296f, 911.74f, 404.629f)), owning_building_guid = 5)
      LocalObject(431, Door.Constructor(Vector3(1142.296f, 945.74f, 404.629f)), owning_building_guid = 5)
    }

    Building10013()

    def Building10013(): Unit = { // Name: ceiling_bldg_i_10013 Type: ceiling_bldg_i GUID: 6, MapID: 10013
      LocalBuilding("ceiling_bldg_i_10013", 6, 10013, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(989.84f, 1093.24f, 286.02f), Vector3(0f, 0f, 0f), ceiling_bldg_i)))
      LocalObject(384, Door.Constructor(Vector3(965.35f, 1096.756f, 287.799f)), owning_building_guid = 6)
      LocalObject(408, Door.Constructor(Vector3(1015.35f, 1096.756f, 287.799f)), owning_building_guid = 6)
    }

    Building10117()

    def Building10117(): Unit = { // Name: ceiling_bldg_j_10117 Type: ceiling_bldg_j GUID: 7, MapID: 10117
      LocalBuilding("ceiling_bldg_j_10117", 7, 10117, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1013.22f, 1222.5f, 246.4f), Vector3(0f, 0f, 107f), ceiling_bldg_j)))
      LocalObject(404, Door.Constructor(Vector3(1001.271f, 1218.864f, 248.179f)), owning_building_guid = 7)
      LocalObject(409, Door.Constructor(Vector3(1025.179f, 1226.173f, 248.179f)), owning_building_guid = 7)
    }

    Building10114()

    def Building10114(): Unit = { // Name: ceiling_bldg_j_10114 Type: ceiling_bldg_j GUID: 8, MapID: 10114
      LocalBuilding("ceiling_bldg_j_10114", 8, 10114, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1048.57f, 1135.38f, 275.22f), Vector3(0f, 0f, 126f), ceiling_bldg_j)))
      LocalObject(411, Door.Constructor(Vector3(1038.456f, 1128.052f, 276.999f)), owning_building_guid = 8)
      LocalObject(414, Door.Constructor(Vector3(1058.681f, 1142.746f, 276.999f)), owning_building_guid = 8)
    }

    Building10115()

    def Building10115(): Unit = { // Name: ceiling_bldg_j_10115 Type: ceiling_bldg_j GUID: 9, MapID: 10115
      LocalBuilding("ceiling_bldg_j_10115", 9, 10115, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1128.81f, 798.93f, 197.93f), Vector3(0f, 0f, 0f), ceiling_bldg_j)))
      LocalObject(428, Door.Constructor(Vector3(1128.826f, 786.42f, 199.709f)), owning_building_guid = 9)
      LocalObject(429, Door.Constructor(Vector3(1128.826f, 811.42f, 199.709f)), owning_building_guid = 9)
    }

    Building10037()

    def Building10037(): Unit = { // Name: ceiling_bldg_z_10037 Type: ceiling_bldg_z GUID: 10, MapID: 10037
      LocalBuilding("ceiling_bldg_z_10037", 10, 10037, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1015.45f, 1060.53f, 401.85f), Vector3(0f, 0f, 0f), ceiling_bldg_z)))
      LocalObject(395, Door.Constructor(Vector3(990.96f, 1064.546f, 403.629f)), owning_building_guid = 10)
      LocalObject(412, Door.Constructor(Vector3(1047.96f, 1064.546f, 403.629f)), owning_building_guid = 10)
    }

    Building10009()

    def Building10009(): Unit = { // Name: ceiling_bldg_z_10009 Type: ceiling_bldg_z GUID: 11, MapID: 10009
      LocalBuilding("ceiling_bldg_z_10009", 11, 10009, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1068.84f, 852.69f, 258.04f), Vector3(0f, 0f, 317f), ceiling_bldg_z)))
      LocalObject(413, Door.Constructor(Vector3(1053.668f, 872.3293f, 259.819f)), owning_building_guid = 11)
      LocalObject(421, Door.Constructor(Vector3(1095.355f, 833.4553f, 259.819f)), owning_building_guid = 11)
    }

    Building10031()

    def Building10031(): Unit = { // Name: ceiling_bldg_z_10031 Type: ceiling_bldg_z GUID: 12, MapID: 10031
      LocalBuilding("ceiling_bldg_z_10031", 12, 10031, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1193.06f, 1036.8f, 234.92f), Vector3(0f, 0f, 0f), ceiling_bldg_z)))
      LocalObject(436, Door.Constructor(Vector3(1168.57f, 1040.816f, 236.699f)), owning_building_guid = 12)
      LocalObject(461, Door.Constructor(Vector3(1225.57f, 1040.816f, 236.699f)), owning_building_guid = 12)
    }

    Building10038()

    def Building10038(): Unit = { // Name: ground_bldg_a_10038 Type: ground_bldg_a GUID: 23, MapID: 10038
      LocalBuilding("ground_bldg_a_10038", 23, 10038, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1235.85f, 1129.52f, 189.73f), Vector3(0f, 0f, 54f), ground_bldg_a)))
      LocalObject(457, Door.Constructor(Vector3(1221.054f, 1122.782f, 191.509f)), owning_building_guid = 23)
      LocalObject(472, Door.Constructor(Vector3(1242.228f, 1145.131f, 191.509f)), owning_building_guid = 23)
    }

    Building10005()

    def Building10005(): Unit = { // Name: ground_bldg_b_10005 Type: ground_bldg_b GUID: 24, MapID: 10005
      LocalBuilding("ground_bldg_b_10005", 24, 10005, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1113.49f, 784.48f, 241.05f), Vector3(0f, 0f, 39f), ground_bldg_b)))
      LocalObject(423, Door.Constructor(Vector3(1104.678f, 798.5632f, 242.829f)), owning_building_guid = 24)
      LocalObject(426, Door.Constructor(Vector3(1118.801f, 787.4811f, 242.829f)), owning_building_guid = 24)
      LocalObject(427, Door.Constructor(Vector3(1128.761f, 804.5875f, 248.329f)), owning_building_guid = 24)
    }

    Building10213()

    def Building10213(): Unit = { // Name: ground_bldg_c_10213 Type: ground_bldg_c GUID: 25, MapID: 10213
      LocalBuilding("ground_bldg_c_10213", 25, 10213, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(996.82f, 984.32f, 310.7f), Vector3(0f, 0f, 192f), ground_bldg_c)))
      LocalObject(378, Door.Constructor(Vector3(949.452f, 985.9983f, 312.479f)), owning_building_guid = 25)
      LocalObject(381, Door.Constructor(Vector3(955.8972f, 955.6757f, 312.479f)), owning_building_guid = 25)
      LocalObject(401, Door.Constructor(Vector3(999.1253f, 980.737f, 312.479f)), owning_building_guid = 25)
    }

    Building10006()

    def Building10006(): Unit = { // Name: ground_bldg_c_10006 Type: ground_bldg_c GUID: 26, MapID: 10006
      LocalBuilding("ground_bldg_c_10006", 26, 10006, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1260.71f, 1059.42f, 379.18f), Vector3(0f, 0f, 192f), ground_bldg_c)))
      LocalObject(454, Door.Constructor(Vector3(1213.342f, 1061.098f, 380.959f)), owning_building_guid = 26)
      LocalObject(456, Door.Constructor(Vector3(1219.787f, 1030.776f, 380.959f)), owning_building_guid = 26)
      LocalObject(476, Door.Constructor(Vector3(1263.015f, 1055.837f, 380.959f)), owning_building_guid = 26)
    }

    Building10001()

    def Building10001(): Unit = { // Name: ground_bldg_d_10001 Type: ground_bldg_d GUID: 27, MapID: 10001
      LocalBuilding("ground_bldg_d_10001", 27, 10001, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1193.17f, 1133.19f, 320.03f), Vector3(0f, 0f, 0f), ground_bldg_d)))
      LocalObject(440, Door.Constructor(Vector3(1175.68f, 1133.206f, 321.765f)), owning_building_guid = 27)
      LocalObject(443, Door.Constructor(Vector3(1193.186f, 1115.68f, 321.765f)), owning_building_guid = 27)
      LocalObject(444, Door.Constructor(Vector3(1193.186f, 1150.68f, 321.765f)), owning_building_guid = 27)
      LocalObject(453, Door.Constructor(Vector3(1210.68f, 1133.206f, 321.765f)), owning_building_guid = 27)
    }

    Building10020()

    def Building10020(): Unit = { // Name: ground_bldg_e_10020 Type: ground_bldg_e GUID: 28, MapID: 10020
      LocalBuilding("ground_bldg_e_10020", 28, 10020, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(978.04f, 851.77f, 235.98f), Vector3(0f, 0f, 0f), ground_bldg_e)))
      LocalObject(385, Door.Constructor(Vector3(966.024f, 850.78f, 237.759f)), owning_building_guid = 28)
      LocalObject(390, Door.Constructor(Vector3(982.024f, 884.28f, 243.259f)), owning_building_guid = 28)
      LocalObject(400, Door.Constructor(Vector3(997.05f, 879.786f, 237.759f)), owning_building_guid = 28)
      LocalObject(406, Door.Constructor(Vector3(1003.05f, 859.786f, 243.259f)), owning_building_guid = 28)
    }

    Building10007()

    def Building10007(): Unit = { // Name: ground_bldg_f_10007 Type: ground_bldg_f GUID: 29, MapID: 10007
      LocalBuilding("ground_bldg_f_10007", 29, 10007, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1090.77f, 844.07f, 321.32f), Vector3(0f, 0f, 325f), ground_bldg_f)))
      LocalObject(416, Door.Constructor(Vector3(1062.523f, 839.4526f, 323.099f)), owning_building_guid = 29)
      LocalObject(424, Door.Constructor(Vector3(1116.939f, 860.5503f, 323.099f)), owning_building_guid = 29)
    }

    Building10022()

    def Building10022(): Unit = { // Name: ground_bldg_f_10022 Type: ground_bldg_f GUID: 30, MapID: 10022
      LocalBuilding("ground_bldg_f_10022", 30, 10022, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1123.7f, 1147.15f, 234.98f), Vector3(0f, 0f, 346f), ground_bldg_f)))
      LocalObject(422, Door.Constructor(Vector3(1098.984f, 1132.717f, 236.759f)), owning_building_guid = 30)
      LocalObject(432, Door.Constructor(Vector3(1142.225f, 1171.914f, 236.759f)), owning_building_guid = 30)
    }

    Building10024()

    def Building10024(): Unit = { // Name: ground_bldg_i_10024 Type: ground_bldg_i GUID: 31, MapID: 10024
      LocalBuilding("ground_bldg_i_10024", 31, 10024, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1218.75f, 1051.94f, 287.43f), Vector3(0f, 0f, 24f), ground_bldg_i)))
      LocalObject(446, Door.Constructor(Vector3(1194.947f, 1045.191f, 289.209f)), owning_building_guid = 31)
      LocalObject(471, Door.Constructor(Vector3(1240.625f, 1065.528f, 289.209f)), owning_building_guid = 31)
    }

    Building10039()

    def Building10039(): Unit = { // Name: ground_bldg_z_10039 Type: ground_bldg_z GUID: 32, MapID: 10039
      LocalBuilding("ground_bldg_z_10039", 32, 10039, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(954.47f, 849.74f, 178.4f), Vector3(0f, 0f, 293f), ground_bldg_z)))
      LocalObject(372, Door.Constructor(Vector3(935.0992f, 848.0531f, 191.179f)), owning_building_guid = 32)
      LocalObject(373, Door.Constructor(Vector3(937.5516f, 869.1636f, 180.179f)), owning_building_guid = 32)
      LocalObject(376, Door.Constructor(Vector3(944.9157f, 872.2894f, 196.679f)), owning_building_guid = 32)
      LocalObject(379, Door.Constructor(Vector3(953.1809f, 832.3434f, 185.679f)), owning_building_guid = 32)
      LocalObject(380, Door.Constructor(Vector3(955.7963f, 867.1307f, 185.679f)), owning_building_guid = 32)
      LocalObject(383, Door.Constructor(Vector3(964.0615f, 827.1847f, 196.679f)), owning_building_guid = 32)
      LocalObject(387, Door.Constructor(Vector3(971.4255f, 830.3105f, 180.179f)), owning_building_guid = 32)
      LocalObject(388, Door.Constructor(Vector3(973.8467f, 851.4641f, 191.179f)), owning_building_guid = 32)
    }

    Building10209()

    def Building10209(): Unit = { // Name: NW_Redoubt Type: redoubt GUID: 33, MapID: 10209
      LocalBuilding("NW_Redoubt", 33, 10209, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(887.44f, 1064.63f, 163.23f), Vector3(0f, 0f, 4f), redoubt)))
      LocalObject(552, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 33)
      LocalObject(363, Door.Constructor(Vector3(869.9915f, 1063.426f, 164.965f)), owning_building_guid = 33)
      LocalObject(364, Door.Constructor(Vector3(874.3493f, 1075.823f, 175.009f)), owning_building_guid = 33)
      LocalObject(365, Door.Constructor(Vector3(882.5875f, 1076.395f, 174.989f)), owning_building_guid = 33)
      LocalObject(366, Door.Constructor(Vector3(890.5857f, 1076.987f, 174.989f)), owning_building_guid = 33)
      LocalObject(367, Door.Constructor(Vector3(898.8093f, 1077.567f, 175.009f)), owning_building_guid = 33)
      LocalObject(368, Door.Constructor(Vector3(904.9062f, 1065.867f, 164.965f)), owning_building_guid = 33)
      LocalObject(559, Terminal.Constructor(Vector3(886.1505f, 1083.3f, 163.1835f), vanu_equipment_term), owning_building_guid = 33)
      LocalObject(560, Terminal.Constructor(Vector3(888.6063f, 1046.158f, 163.1858f), vanu_equipment_term), owning_building_guid = 33)
      LocalObject(516, SpawnTube.Constructor(Vector3(887.44f, 1064.63f, 163.23f), Vector3(0, 0, 176)), owning_building_guid = 33)
      LocalObject(503, Painbox.Constructor(Vector3(887.4609f, 1064.923f, 171.019f), painbox_continuous), owning_building_guid = 33)
    }

    Building10210()

    def Building10210(): Unit = { // Name: SE_Redoubt Type: redoubt GUID: 34, MapID: 10210
      LocalBuilding("SE_Redoubt", 34, 10210, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(1157.92f, 849.04f, 158.73f), Vector3(0f, 0f, 310f), redoubt)))
      LocalObject(555, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 34)
      LocalObject(433, Door.Constructor(Vector3(1146.69f, 862.4484f, 160.465f)), owning_building_guid = 34)
      LocalObject(434, Door.Constructor(Vector3(1159.281f, 866.2097f, 170.509f)), owning_building_guid = 34)
      LocalObject(435, Door.Constructor(Vector3(1164.586f, 859.8812f, 170.489f)), owning_building_guid = 34)
      LocalObject(437, Door.Constructor(Vector3(1169.188f, 835.6368f, 160.465f)), owning_building_guid = 34)
      LocalObject(438, Door.Constructor(Vector3(1169.766f, 853.7581f, 170.489f)), owning_building_guid = 34)
      LocalObject(439, Door.Constructor(Vector3(1175.069f, 847.446f, 170.509f)), owning_building_guid = 34)
      LocalObject(586, Terminal.Constructor(Vector3(1143.661f, 837.2388f, 158.6858f), vanu_equipment_term), owning_building_guid = 34)
      LocalObject(590, Terminal.Constructor(Vector3(1172.266f, 861.0569f, 158.6835f), vanu_equipment_term), owning_building_guid = 34)
      LocalObject(517, SpawnTube.Constructor(Vector3(1157.92f, 849.04f, 158.73f), Vector3(0, 0, 230)), owning_building_guid = 34)
      LocalObject(505, Painbox.Constructor(Vector3(1158.169f, 849.1951f, 166.519f), painbox_continuous), owning_building_guid = 34)
    }

    Building10012()

    def Building10012(): Unit = { // Name: NW_Stasis Type: vanu_control_point GUID: 68, MapID: 10012
      LocalBuilding("NW_Stasis", 68, 10012, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(995.33f, 1106.69f, 329.62f), Vector3(0f, 0f, 222f), vanu_control_point)))
      LocalObject(554, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 68)
      LocalObject(375, Door.Constructor(Vector3(943.5559f, 1095.08f, 331.399f)), owning_building_guid = 68)
      LocalObject(392, Door.Constructor(Vector3(983.5845f, 1098.827f, 336.34f)), owning_building_guid = 68)
      LocalObject(393, Door.Constructor(Vector3(984.4945f, 1115.759f, 336.34f)), owning_building_guid = 68)
      LocalObject(394, Door.Constructor(Vector3(990.3757f, 1121.419f, 361.399f)), owning_building_guid = 68)
      LocalObject(397, Door.Constructor(Vector3(991.5599f, 1144.016f, 331.399f)), owning_building_guid = 68)
      LocalObject(398, Door.Constructor(Vector3(993.1413f, 1058.616f, 331.399f)), owning_building_guid = 68)
      LocalObject(399, Door.Constructor(Vector3(995.7598f, 1115.161f, 361.379f)), owning_building_guid = 68)
      LocalObject(402, Door.Constructor(Vector3(1000.554f, 1097.923f, 336.34f)), owning_building_guid = 68)
      LocalObject(403, Door.Constructor(Vector3(1001.15f, 1109.224f, 361.379f)), owning_building_guid = 68)
      LocalObject(405, Door.Constructor(Vector3(1001.426f, 1114.849f, 336.34f)), owning_building_guid = 68)
      LocalObject(407, Door.Constructor(Vector3(1006.808f, 1103.217f, 361.399f)), owning_building_guid = 68)
      LocalObject(410, Door.Constructor(Vector3(1030.148f, 1119.18f, 331.399f)), owning_building_guid = 68)
      LocalObject(573, Terminal.Constructor(Vector3(982.1874f, 1105.339f, 334.633f), vanu_equipment_term), owning_building_guid = 68)
      LocalObject(574, Terminal.Constructor(Vector3(982.3665f, 1109.244f, 334.637f), vanu_equipment_term), owning_building_guid = 68)
      LocalObject(576, Terminal.Constructor(Vector3(989.9484f, 1096.727f, 334.637f), vanu_equipment_term), owning_building_guid = 68)
      LocalObject(577, Terminal.Constructor(Vector3(991.0513f, 1117.164f, 334.633f), vanu_equipment_term), owning_building_guid = 68)
      LocalObject(578, Terminal.Constructor(Vector3(993.916f, 1096.515f, 334.633f), vanu_equipment_term), owning_building_guid = 68)
      LocalObject(579, Terminal.Constructor(Vector3(995.0189f, 1116.953f, 334.637f), vanu_equipment_term), owning_building_guid = 68)
      LocalObject(580, Terminal.Constructor(Vector3(1002.601f, 1104.437f, 334.637f), vanu_equipment_term), owning_building_guid = 68)
      LocalObject(581, Terminal.Constructor(Vector3(1002.813f, 1108.404f, 334.633f), vanu_equipment_term), owning_building_guid = 68)
      LocalObject(615, SpawnTube.Constructor(Vector3(992.5055f, 1106.838f, 334.759f), Vector3(0, 0, 318)), owning_building_guid = 68)
      LocalObject(504, Painbox.Constructor(Vector3(992.5176f, 1107.254f, 343.9618f), painbox_continuous), owning_building_guid = 68)
      LocalObject(508, Painbox.Constructor(Vector3(981.7784f, 1096.659f, 338.51f), painbox_door_radius_continuous), owning_building_guid = 68)
      LocalObject(509, Painbox.Constructor(Vector3(983.5879f, 1117.207f, 338.51f), painbox_door_radius_continuous), owning_building_guid = 68)
      LocalObject(510, Painbox.Constructor(Vector3(1001.82f, 1096.431f, 338.51f), painbox_door_radius_continuous), owning_building_guid = 68)
      LocalObject(511, Painbox.Constructor(Vector3(1003.214f, 1116.336f, 337.91f), painbox_door_radius_continuous), owning_building_guid = 68)
    }

    Building10003()

    def Building10003(): Unit = { // Name: SE_Stasis Type: vanu_control_point GUID: 69, MapID: 10003
      LocalBuilding("SE_Stasis", 69, 10003, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1232.73f, 922.42f, 322.62f), Vector3(0f, 0f, 224f), vanu_control_point)))
      LocalObject(557, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 69)
      LocalObject(441, Door.Constructor(Vector3(1181.393f, 909.0106f, 324.399f)), owning_building_guid = 69)
      LocalObject(458, Door.Constructor(Vector3(1221.266f, 914.1519f, 329.34f)), owning_building_guid = 69)
      LocalObject(459, Door.Constructor(Vector3(1221.585f, 931.1053f, 329.34f)), owning_building_guid = 69)
      LocalObject(463, Door.Constructor(Vector3(1227.265f, 936.9673f, 354.399f)), owning_building_guid = 69)
      LocalObject(464, Door.Constructor(Vector3(1227.66f, 959.5913f, 324.399f)), owning_building_guid = 69)
      LocalObject(465, Door.Constructor(Vector3(1232.22f, 874.2994f, 324.399f)), owning_building_guid = 69)
      LocalObject(467, Door.Constructor(Vector3(1232.864f, 930.9014f, 354.379f)), owning_building_guid = 69)
      LocalObject(468, Door.Constructor(Vector3(1238.256f, 913.8412f, 329.34f)), owning_building_guid = 69)
      LocalObject(469, Door.Constructor(Vector3(1238.458f, 925.1559f, 354.379f)), owning_building_guid = 69)
      LocalObject(470, Door.Constructor(Vector3(1238.538f, 930.7868f, 329.34f)), owning_building_guid = 69)
      LocalObject(473, Door.Constructor(Vector3(1244.322f, 919.3499f, 354.399f)), owning_building_guid = 69)
      LocalObject(477, Door.Constructor(Vector3(1267.091f, 936.1172f, 324.399f)), owning_building_guid = 69)
      LocalObject(594, Terminal.Constructor(Vector3(1219.643f, 920.611f, 327.633f), vanu_equipment_term), owning_building_guid = 69)
      LocalObject(595, Terminal.Constructor(Vector3(1219.685f, 924.5197f, 327.637f), vanu_equipment_term), owning_building_guid = 69)
      LocalObject(596, Terminal.Constructor(Vector3(1227.699f, 912.2751f, 327.637f), vanu_equipment_term), owning_building_guid = 69)
      LocalObject(597, Terminal.Constructor(Vector3(1228.088f, 932.7386f, 327.633f), vanu_equipment_term), owning_building_guid = 69)
      LocalObject(598, Terminal.Constructor(Vector3(1231.672f, 912.2022f, 327.633f), vanu_equipment_term), owning_building_guid = 69)
      LocalObject(599, Terminal.Constructor(Vector3(1232.061f, 932.6658f, 327.637f), vanu_equipment_term), owning_building_guid = 69)
      LocalObject(601, Terminal.Constructor(Vector3(1240.076f, 920.4219f, 327.637f), vanu_equipment_term), owning_building_guid = 69)
      LocalObject(602, Terminal.Constructor(Vector3(1240.148f, 924.3945f, 327.633f), vanu_equipment_term), owning_building_guid = 69)
      LocalObject(616, SpawnTube.Constructor(Vector3(1229.902f, 922.4694f, 327.759f), Vector3(0, 0, 316)), owning_building_guid = 69)
      LocalObject(507, Painbox.Constructor(Vector3(1229.9f, 922.8856f, 336.9618f), painbox_continuous), owning_building_guid = 69)
      LocalObject(512, Painbox.Constructor(Vector3(1219.537f, 911.9223f, 331.51f), painbox_door_radius_continuous), owning_building_guid = 69)
      LocalObject(513, Painbox.Constructor(Vector3(1220.628f, 932.5208f, 331.51f), painbox_door_radius_continuous), owning_building_guid = 69)
      LocalObject(514, Painbox.Constructor(Vector3(1239.574f, 912.394f, 331.51f), painbox_door_radius_continuous), owning_building_guid = 69)
      LocalObject(515, Painbox.Constructor(Vector3(1240.272f, 932.335f, 330.91f), painbox_door_radius_continuous), owning_building_guid = 69)
    }

    Building10000()

    def Building10000(): Unit = { // Name: Core Type: vanu_core GUID: 70, MapID: 10000
      LocalBuilding("Core", 70, 10000, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1093.35f, 991.55f, 292.73f), Vector3(0f, 0f, 0f), vanu_core)))
      LocalObject(415, Door.Constructor(Vector3(1060.857f, 979.572f, 299.518f)), owning_building_guid = 70)
      LocalObject(417, Door.Constructor(Vector3(1089.328f, 951.057f, 299.518f)), owning_building_guid = 70)
      LocalObject(418, Door.Constructor(Vector3(1089.328f, 951.057f, 304.518f)), owning_building_guid = 70)
      LocalObject(419, Door.Constructor(Vector3(1089.372f, 1008.043f, 304.518f)), owning_building_guid = 70)
      LocalObject(420, Door.Constructor(Vector3(1089.372f, 1008.043f, 309.518f)), owning_building_guid = 70)
      LocalObject(425, Door.Constructor(Vector3(1117.843f, 979.528f, 309.518f)), owning_building_guid = 70)
    }

    Building10044()

    def Building10044(): Unit = { // Name: NW_ATPlant Type: vanu_vehicle_station GUID: 97, MapID: 10044
      LocalBuilding("NW_ATPlant", 97, 10044, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(930.3f, 1141.53f, 164.95f), Vector3(0f, 0f, 82f), vanu_vehicle_station)))
      LocalObject(553, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 97)
      LocalObject(369, Door.Constructor(Vector3(914.9034f, 1139.01f, 166.729f)), owning_building_guid = 97)
      LocalObject(370, Door.Constructor(Vector3(919.5815f, 1114.117f, 186.641f)), owning_building_guid = 97)
      LocalObject(371, Door.Constructor(Vector3(933.1416f, 1096.054f, 196.729f)), owning_building_guid = 97)
      LocalObject(374, Door.Constructor(Vector3(941.2718f, 1094.883f, 196.709f)), owning_building_guid = 97)
      LocalObject(377, Door.Constructor(Vector3(949.2183f, 1093.8f, 196.709f)), owning_building_guid = 97)
      LocalObject(382, Door.Constructor(Vector3(957.3834f, 1092.647f, 196.729f)), owning_building_guid = 97)
      LocalObject(386, Door.Constructor(Vector3(970.8724f, 1131.144f, 186.653f)), owning_building_guid = 97)
      LocalObject(389, Door.Constructor(Vector3(978.7827f, 1130.033f, 166.729f)), owning_building_guid = 97)
      LocalObject(482, Door.Constructor(Vector3(938.5251f, 1075.059f, 171.583f)), owning_building_guid = 97)
      LocalObject(547, Terminal.Constructor(Vector3(927.406f, 1122.807f, 184.867f), vanu_air_vehicle_term), owning_building_guid = 97)
      LocalObject(617, VehicleSpawnPad.Constructor(Vector3(935.6392f, 1126.141f, 184.866f), vanu_vehicle_creation_pad, Vector3(0, 0, 8)), owning_building_guid = 97, terminal_guid = 547)
      LocalObject(548, Terminal.Constructor(Vector3(962.4658f, 1117.888f, 184.867f), vanu_air_vehicle_term), owning_building_guid = 97)
      LocalObject(619, VehicleSpawnPad.Constructor(Vector3(955.4604f, 1123.355f, 184.866f), vanu_vehicle_creation_pad, Vector3(0, 0, 8)), owning_building_guid = 97, terminal_guid = 548)
      LocalObject(562, Terminal.Constructor(Vector3(932.0184f, 1115.15f, 167.45f), vanu_equipment_term), owning_building_guid = 97)
      LocalObject(567, Terminal.Constructor(Vector3(955.7848f, 1111.81f, 167.45f), vanu_equipment_term), owning_building_guid = 97)
      LocalObject(623, Terminal.Constructor(Vector3(943.2768f, 1108.05f, 169.95f), vanu_vehicle_term), owning_building_guid = 97)
      LocalObject(618, VehicleSpawnPad.Constructor(Vector3(941.0632f, 1093.076f, 167.355f), vanu_vehicle_creation_pad, Vector3(0, 0, 188)), owning_building_guid = 97, terminal_guid = 623)
    }

    Building10197()

    def Building10197(): Unit = { // Name: SE_ATPlant Type: vanu_vehicle_station GUID: 98, MapID: 10197
      LocalBuilding("SE_ATPlant", 98, 10197, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1244.82f, 841.97f, 159.95f), Vector3(0f, 0f, 274f), vanu_vehicle_station)))
      LocalObject(556, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 98)
      LocalObject(447, Door.Constructor(Vector3(1195.006f, 843.136f, 161.729f)), owning_building_guid = 98)
      LocalObject(449, Door.Constructor(Vector3(1202.975f, 843.6932f, 181.653f)), owning_building_guid = 98)
      LocalObject(451, Door.Constructor(Vector3(1208.165f, 884.1538f, 191.729f)), owning_building_guid = 98)
      LocalObject(455, Door.Constructor(Vector3(1216.391f, 884.7241f, 191.709f)), owning_building_guid = 98)
      LocalObject(460, Door.Constructor(Vector3(1224.39f, 885.3164f, 191.709f)), owning_building_guid = 98)
      LocalObject(466, Door.Constructor(Vector3(1232.585f, 885.8615f, 191.729f)), owning_building_guid = 98)
      LocalObject(474, Door.Constructor(Vector3(1249.605f, 871.0125f, 181.641f)), owning_building_guid = 98)
      LocalObject(475, Door.Constructor(Vector3(1259.356f, 847.6357f, 161.729f)), owning_building_guid = 98)
      LocalObject(483, Door.Constructor(Vector3(1222.955f, 905.278f, 166.583f)), owning_building_guid = 98)
      LocalObject(549, Terminal.Constructor(Vector3(1208.442f, 858.4074f, 179.867f), vanu_air_vehicle_term), owning_building_guid = 98)
      LocalObject(620, VehicleSpawnPad.Constructor(Vector3(1216.431f, 854.5162f, 179.866f), vanu_vehicle_creation_pad, Vector3(0, 0, 176)), owning_building_guid = 98, terminal_guid = 549)
      LocalObject(550, Terminal.Constructor(Vector3(1243.758f, 860.886f, 179.867f), vanu_air_vehicle_term), owning_building_guid = 98)
      LocalObject(622, VehicleSpawnPad.Constructor(Vector3(1236.398f, 855.9124f, 179.866f), vanu_vehicle_creation_pad, Vector3(0, 0, 176)), owning_building_guid = 98, terminal_guid = 550)
      LocalObject(593, Terminal.Constructor(Vector3(1213.713f, 865.7419f, 162.45f), vanu_equipment_term), owning_building_guid = 98)
      LocalObject(600, Terminal.Constructor(Vector3(1237.654f, 867.4161f, 162.45f), vanu_equipment_term), owning_building_guid = 98)
      LocalObject(624, Terminal.Constructor(Vector3(1225.166f, 872.0201f, 164.95f), vanu_vehicle_term), owning_building_guid = 98)
      LocalObject(621, VehicleSpawnPad.Constructor(Vector3(1224.218f, 887.1278f, 162.355f), vanu_vehicle_creation_pad, Vector3(0, 0, -4)), owning_building_guid = 98, terminal_guid = 624)
    }

    Building10104()

    def Building10104(): Unit = { // Name: GW_Cavern5_W Type: warpgate_cavern GUID: 99, MapID: 10104
      LocalBuilding("GW_Cavern5_W", 99, 10104, FoundationBuilder(WarpGate.Structure(Vector3(253.29f, 1123.65f, 220.95f))))
    }

    Building10105()

    def Building10105(): Unit = { // Name: GW_Cavern5_S Type: warpgate_cavern GUID: 100, MapID: 10105
      LocalBuilding("GW_Cavern5_S", 100, 10105, FoundationBuilder(WarpGate.Structure(Vector3(1018.63f, 190.66f, 230.9f))))
    }

    Building10107()

    def Building10107(): Unit = { // Name: GW_Cavern5_N Type: warpgate_cavern GUID: 101, MapID: 10107
      LocalBuilding("GW_Cavern5_N", 101, 10107, FoundationBuilder(WarpGate.Structure(Vector3(1217.45f, 1790.01f, 231.29f))))
    }

    Building10106()

    def Building10106(): Unit = { // Name: GW_Cavern5_E Type: warpgate_cavern GUID: 102, MapID: 10106
      LocalBuilding("GW_Cavern5_E", 102, 10106, FoundationBuilder(WarpGate.Structure(Vector3(1860.2f, 893.34f, 231.08f))))
    }

    ZoneOwnedObjects()

    def ZoneOwnedObjects(): Unit = {
      LocalObject(558, Terminal.Constructor(Vector3(878.19f, 1054.2f, 163.19f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(561, Terminal.Constructor(Vector3(896.75f, 1075.17f, 163.19f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(563, Terminal.Constructor(Vector3(941.54f, 850.15f, 189.36f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(564, Terminal.Constructor(Vector3(945.16f, 843.7f, 183.9f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(565, Terminal.Constructor(Vector3(950.41f, 852.49f, 178.37f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(566, Terminal.Constructor(Vector3(953.54f, 844.73f, 178.41f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(568, Terminal.Constructor(Vector3(956.79f, 837.39f, 178.38f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(569, Terminal.Constructor(Vector3(963.51f, 855.72f, 183.88f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(570, Terminal.Constructor(Vector3(967.12f, 849.74f, 189.34f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(571, Terminal.Constructor(Vector3(979.19f, 862.66f, 241.45f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(572, Terminal.Constructor(Vector3(979.19f, 873.02f, 241.48f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(575, Terminal.Constructor(Vector3(989f, 872.94f, 235.95f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(582, Terminal.Constructor(Vector3(1008.85f, 1236.36f, 246.4f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(583, Terminal.Constructor(Vector3(1057.9f, 849.87f, 253.23f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(584, Terminal.Constructor(Vector3(1076.5f, 869.5f, 253.22f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(585, Terminal.Constructor(Vector3(1113.28f, 798.92f, 197.92f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(587, Terminal.Constructor(Vector3(1143.84f, 850.5f, 158.69f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(588, Terminal.Constructor(Vector3(1144.35f, 798.89f, 197.92f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(589, Terminal.Constructor(Vector3(1171.79f, 847.49f, 158.69f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(591, Terminal.Constructor(Vector3(1187.06f, 1054.3f, 230.13f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(592, Terminal.Constructor(Vector3(1207.07f, 1027.25f, 230.13f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(15, ProximityTerminal.Constructor(Vector3(974.48f, 848.12f, 235.98f), crystals_health_b), owning_building_guid = 0)
      LocalObject(16, ProximityTerminal.Constructor(Vector3(1111.91f, 984.81f, 150.41f), crystals_health_b), owning_building_guid = 0)
      LocalObject(498, ProximityTerminal.Constructor(Vector3(951.89f, 848.7f, 189.55f), crystals_health_b), owning_building_guid = 0)
      LocalObject(499, ProximityTerminal.Constructor(Vector3(1058.51f, 849.13f, 258.04f), crystals_health_b), owning_building_guid = 0)
      LocalObject(71, FacilityTurret.Constructor(Vector3(903.84f, 1038.79f, 162.83f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(71, 5000)
      LocalObject(72, FacilityTurret.Constructor(Vector3(917.33f, 1096.84f, 328.62f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(72, 5001)
      LocalObject(73, FacilityTurret.Constructor(Vector3(941.49f, 1065.67f, 265.85f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(73, 5002)
      LocalObject(74, FacilityTurret.Constructor(Vector3(972.24f, 908.83f, 167.96f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(74, 5003)
      LocalObject(75, FacilityTurret.Constructor(Vector3(987.05f, 936.85f, 289.22f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(75, 5004)
      LocalObject(76, FacilityTurret.Constructor(Vector3(1006.95f, 1040.83f, 328.62f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(76, 5005)
      LocalObject(77, FacilityTurret.Constructor(Vector3(1016.37f, 1093.24f, 162.83f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(77, 5006)
      LocalObject(78, FacilityTurret.Constructor(Vector3(1025.41f, 863.67f, 167.94f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(78, 5007)
      LocalObject(79, FacilityTurret.Constructor(Vector3(1048.02f, 893.23f, 313.55f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(79, 5008)
      LocalObject(80, FacilityTurret.Constructor(Vector3(1078.71f, 970.7f, 150.32f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(80, 5009)
      LocalObject(81, FacilityTurret.Constructor(Vector3(1090.27f, 1143.01f, 328.62f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(81, 5010)
      LocalObject(82, FacilityTurret.Constructor(Vector3(1114.24f, 862.02f, 156.83f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(82, 5011)
      LocalObject(83, FacilityTurret.Constructor(Vector3(1118.88f, 1017.24f, 150.31f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(83, 5012)
      LocalObject(84, FacilityTurret.Constructor(Vector3(1148.99f, 1139.1f, 309.56f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(84, 5013)
      LocalObject(85, FacilityTurret.Constructor(Vector3(1151.31f, 1053.73f, 272.84f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(85, 5014)
      LocalObject(86, FacilityTurret.Constructor(Vector3(1155.55f, 768.04f, 281.56f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(86, 5015)
      LocalObject(87, FacilityTurret.Constructor(Vector3(1157.01f, 907.45f, 321.58f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(87, 5016)
      LocalObject(88, FacilityTurret.Constructor(Vector3(1169.41f, 1134.44f, 226.08f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(88, 5017)
      LocalObject(89, FacilityTurret.Constructor(Vector3(1176.74f, 1075.21f, 158.51f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(89, 5018)
      LocalObject(90, FacilityTurret.Constructor(Vector3(1191.93f, 930.74f, 156.83f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(90, 5019)
      LocalObject(91, FacilityTurret.Constructor(Vector3(1220.76f, 1109.93f, 309.56f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(91, 5020)
      LocalObject(92, FacilityTurret.Constructor(Vector3(1221.9f, 1003.16f, 357.58f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(92, 5021)
      LocalObject(93, FacilityTurret.Constructor(Vector3(1251.81f, 1016.94f, 273.16f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(93, 5022)
      LocalObject(94, FacilityTurret.Constructor(Vector3(1272.36f, 985.48f, 321.58f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(94, 5023)
      LocalObject(95, FacilityTurret.Constructor(Vector3(1273.43f, 1033.94f, 171.65f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(95, 5024)
      LocalObject(96, FacilityTurret.Constructor(Vector3(1342.12f, 1076.58f, 301.56f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(96, 5025)
    }

    def Lattice(): Unit = {
      LatticeLink("NW_Redoubt", "NW_ATPlant")
      LatticeLink("NW_Stasis", "Core")
      LatticeLink("SE_Stasis", "Core")
      LatticeLink("SE_ATPlant", "SE_Stasis")
      LatticeLink("NW_ATPlant", "NW_Stasis")
      LatticeLink("SE_Redoubt", "SE_ATPlant")
      LatticeLink("NW_Redoubt", "NW_Stasis")
      LatticeLink("SE_Redoubt", "SE_Stasis")
      LatticeLink("NW_Redoubt", "SE_Redoubt")
      LatticeLink("GW_Cavern5_W", "NW_Redoubt")
      LatticeLink("GW_Cavern5_N", "NW_ATPlant")
      LatticeLink("GW_Cavern5_S", "SE_Redoubt")
      LatticeLink("GW_Cavern5_E", "SE_ATPlant")
    }

    Lattice()

    def ZipLines(): Unit = {
      ZipLinePaths(new ZipLinePath(1, false, List(Vector3(836.538f, 971.683f, 284.533f), Vector3(847.701f, 992.693f, 286.866f), Vector3(858.864f, 1013.704f, 281.7f), Vector3(888.189f, 1055.323f, 278.792f), Vector3(903.14f, 1076.54f, 276.458f))))
      ZipLinePaths(new ZipLinePath(2, false, List(Vector3(891.205f, 996.755f, 333.27f), Vector3(915.423f, 1041.517f, 331.172f), Vector3(934.891f, 1077.502f, 329.469f))))
      ZipLinePaths(new ZipLinePath(3, false, List(Vector3(914.981f, 1087.151f, 329.469f), Vector3(900.695f, 1039.306f, 331.566f), Vector3(887.839f, 996.247f, 333.27f))))
      ZipLinePaths(new ZipLinePath(4, false, List(Vector3(933.653f, 971.335f, 168.794f), Vector3(937.301f, 1022.074f, 165.936f), Vector3(938.446f, 1037.992f, 163.681f))))
      ZipLinePaths(new ZipLinePath(5, false, List(Vector3(934.919f, 1077.717f, 266.709f), Vector3(961.063f, 1120.966f, 259.938f), Vector3(987.208f, 1164.215f, 253.129f), Vector3(1007.713f, 1198.136f, 247.251f))))
      ZipLinePaths(new ZipLinePath(6, false, List(Vector3(938.361f, 988.199f, 311.465f), Vector3(923.02f, 985.78f, 329.386f), Vector3(915.349f, 984.57f, 332.477f), Vector3(907.679f, 983.361f, 333.269f))))
      ZipLinePaths(new ZipLinePath(7, true, List(Vector3(939.932f, 1041.648f, 266.204f), Vector3(940.903f, 1041.5f, 298.33f))))
      ZipLinePaths(new ZipLinePath(8, false, List(Vector3(943.877f, 1037.999f, 298.844f), Vector3(952.428f, 1001.712f, 290.08f))))
      ZipLinePaths(new ZipLinePath(9, true, List(Vector3(954.385f, 849.776f, 184.25f), Vector3(955.581f, 843.708f, 189.734f))))
      ZipLinePaths(new ZipLinePath(10, false, List(Vector3(963.215f, 1070.363f, 195.797f), Vector3(973.055f, 987.338f, 186.251f), Vector3(982.896f, 904.313f, 169.711f))))
      ZipLinePaths(new ZipLinePath(11, false, List(Vector3(966.382f, 981.995f, 161.207f), Vector3(1015.066f, 995.9f, 155.835f), Vector3(1044.658f, 1004.352f, 151.164f))))
      ZipLinePaths(new ZipLinePath(12, false, List(Vector3(974.465f, 889.406f, 242.321f), Vector3(982.997f, 939.684f, 243.607f), Vector3(988.779f, 964.53f, 243.502f))))
      ZipLinePaths(new ZipLinePath(13, false, List(Vector3(977.059f, 975.908f, 243.599f), Vector3(955.688f, 987.508f, 259.673f), Vector3(934.317f, 999.109f, 266.758f))))
      ZipLinePaths(new ZipLinePath(14, false, List(Vector3(978.289f, 856.17f, 190.258f), Vector3(1014.241f, 891.248f, 199.82f), Vector3(1050.194f, 926.326f, 208.633f), Vector3(1086.147f, 961.404f, 217.447f), Vector3(1122.1f, 996.482f, 226.26f), Vector3(1158.052f, 1031.559f, 235.073f), Vector3(1162.987f, 1036.374f, 235.778f))))
      ZipLinePaths(new ZipLinePath(15, false, List(Vector3(979.631f, 833.334f, 184.752f), Vector3(1026.998f, 815.027f, 190.209f), Vector3(1074.362f, 796.721f, 194.915f), Vector3(1095.651f, 787.956f, 198.196f), Vector3(1116.94f, 779.191f, 198.779f))))
      ZipLinePaths(new ZipLinePath(16, false, List(Vector3(985.905f, 1139.875f, 185.717f), Vector3(1035.448f, 1151.71f, 183.938f), Vector3(1084.991f, 1163.545f, 181.413f), Vector3(1199.62f, 1190.927f, 173.376f))))
      ZipLinePaths(new ZipLinePath(17, false, List(Vector3(990.859f, 893.127f, 243.457f), Vector3(1015.97f, 937.469f, 246.208f), Vector3(1041.079f, 981.81f, 248.211f), Vector3(1066.187f, 1026.152f, 250.214f), Vector3(1091.295f, 1070.491f, 252.217f), Vector3(1112.956f, 1108.744f, 251.464f))))
      ZipLinePaths(new ZipLinePath(18, false, List(Vector3(993.026f, 1140.513f, 163.68f), Vector3(1043.97f, 1138.533f, 167.62f), Vector3(1094.914f, 1136.553f, 167.313f), Vector3(1138.865f, 1134.845f, 159.356f))))
      ZipLinePaths(new ZipLinePath(19, false, List(Vector3(995.568f, 812.403f, 230.182f), Vector3(1041.682f, 791.189f, 235.778f), Vector3(1087.887f, 771.408f, 238.747f))))
      ZipLinePaths(new ZipLinePath(20, false, List(Vector3(992.183f, 1215.784f, 247.281f), Vector3(981.853f, 1198.469f, 247.506f), Vector3(967.423f, 1179.655f, 230.129f), Vector3(938.562f, 1142.027f, 214.269f), Vector3(914.896f, 1111.172f, 198.717f))))
      ZipLinePaths(new ZipLinePath(21, true, List(Vector3(1008.642f, 1065.179f, 390.74f), Vector3(1007.733f, 1064.686f, 397.4f))))
      ZipLinePaths(new ZipLinePath(22, false, List(Vector3(1012.554f, 945.371f, 374.294f), Vector3(1015.582f, 995.593f, 382.861f), Vector3(1018.849f, 1049.753f, 391.246f))))
      ZipLinePaths(new ZipLinePath(23, true, List(Vector3(1013.884f, 1025.019f, 142.543f), Vector3(1020.3f, 1023.318f, 175.782f))))
      ZipLinePaths(new ZipLinePath(24, false, List(Vector3(1016.788f, 976.956f, 290.079f), Vector3(1037.293f, 987.803f, 297.838f), Vector3(1052.799f, 987.151f, 298.604f))))
      ZipLinePaths(new ZipLinePath(25, true, List(Vector3(1017.555f, 1209.063f, 246.75f), Vector3(1026.884f, 1205.121f, 278.991f))))
      ZipLinePaths(new ZipLinePath(26, true, List(Vector3(1028.886f, 1103.328f, 286.367f), Vector3(1055.503f, 1125.922f, 275.57f))))
      ZipLinePaths(new ZipLinePath(27, false, List(Vector3(1022.812f, 888.35f, 314.685f), Vector3(1017.773f, 897.973f, 313.941f), Vector3(1012.734f, 907.596f, 309.402f), Vector3(1002.656f, 926.842f, 298.124f), Vector3(995.734f, 936.909f, 290.113f))))
      ZipLinePaths(new ZipLinePath(28, false, List(Vector3(1036.004f, 936.681f, 386.369f), Vector3(1059.533f, 929.476f, 395.648f), Vector3(1127.297f, 908.725f, 403.708f))))
      ZipLinePaths(new ZipLinePath(29, false, List(Vector3(1037.586f, 1219.441f, 247.248f), Vector3(1094.923f, 1205.287f, 251.306f), Vector3(1144.261f, 1193.533f, 256.623f), Vector3(1172.316f, 1186.849f, 258.35f))))
      ZipLinePaths(new ZipLinePath(30, false, List(Vector3(1046.508f, 1118.947f, 329.469f), Vector3(1060.289f, 1101.823f, 351.796f), Vector3(1072.008f, 1099.988f, 353.752f))))
      ZipLinePaths(new ZipLinePath(31, false, List(Vector3(1048.769f, 877.431f, 258.896f), Vector3(1025.9f, 919.197f, 256.184f), Vector3(1004.233f, 963.463f, 243.602f))))
      ZipLinePaths(new ZipLinePath(32, false, List(Vector3(1056.116f, 1209.807f, 279.498f), Vector3(1081.23f, 1208.288f, 281.785f), Vector3(1106.344f, 1206.769f, 271.873f), Vector3(1156.572f, 1203.731f, 263.593f), Vector3(1178.239f, 1202.421f, 258.356f))))
      ZipLinePaths(new ZipLinePath(33, false, List(Vector3(1052.422f, 1061.776f, 402.752f), Vector3(1070.817f, 1049.448f, 400.654f), Vector3(1089.212f, 1037.22f, 382.509f), Vector3(1139.78f, 998.768f, 343.26f), Vector3(1190.349f, 960.317f, 322.483f))))
      ZipLinePaths(new ZipLinePath(34, false, List(Vector3(1055.905f, 1160.678f, 276.088f), Vector3(1045.246f, 1178.08f, 272.165f), Vector3(1034.588f, 1195.482f, 249.548f), Vector3(1026.775f, 1204.718f, 247.268f))))
      ZipLinePaths(new ZipLinePath(35, false, List(Vector3(1064.229f, 937.492f, 177.28f), Vector3(1046.544f, 929.061f, 185.858f), Vector3(945.523f, 879.87f, 179.25f))))
      ZipLinePaths(new ZipLinePath(36, true, List(Vector3(1065.916f, 938.66f, 141.799f), Vector3(1067.133f, 939.027f, 176.78f))))
      ZipLinePaths(new ZipLinePath(37, false, List(Vector3(1072.997f, 941.22f, 177.28f), Vector3(1109.124f, 932.355f, 212.906f), Vector3(1145.251f, 923.49f, 247.789f), Vector3(1160.018f, 920.113f, 270.727f), Vector3(1174.786f, 916.736f, 276.061f))))
      ZipLinePaths(new ZipLinePath(38, true, List(Vector3(1070.568f, 856.858f, 246.887f), Vector3(1082.745f, 845f, 253.59f))))
      ZipLinePaths(new ZipLinePath(39, false, List(Vector3(1079.844f, 944.446f, 298.543f), Vector3(1073.849f, 937.71f, 296.436f), Vector3(1067.854f, 930.974f, 291.128f), Vector3(1058.565f, 910.402f, 271.726f), Vector3(1053.718f, 878.816f, 258.901f))))
      ZipLinePaths(new ZipLinePath(40, true, List(Vector3(1079.369f, 865.469f, 314.138f), Vector3(1080.588f, 868.474f, 346.32f))))
      ZipLinePaths(new ZipLinePath(41, false, List(Vector3(1084.523f, 1069.966f, 353.759f), Vector3(1081.809f, 1063.602f, 357.778f), Vector3(1034.51f, 952.682f, 374.294f))))
      ZipLinePaths(new ZipLinePath(42, false, List(Vector3(1085.074f, 1117.049f, 329.482f), Vector3(1126.078f, 1090.896f, 340.496f), Vector3(1158.06f, 1070.496f, 369.607f), Vector3(1190.043f, 1050.096f, 358.773f))))
      ZipLinePaths(new ZipLinePath(43, false, List(Vector3(1098.34f, 1019.147f, 303.582f), Vector3(1119.333f, 1053.316f, 306.9f), Vector3(1136.027f, 1072.485f, 308.32f), Vector3(1164.178f, 1104.808f, 310.373f))))
      ZipLinePaths(new ZipLinePath(44, false, List(Vector3(1105.737f, 792.054f, 198.806f), Vector3(1070.649f, 827.286f, 214.8f), Vector3(1054.607f, 839.119f, 226.703f), Vector3(1038.566f, 850.953f, 230.21f))))
      ZipLinePaths(new ZipLinePath(45, true, List(Vector3(1116.13f, 1100.883f, 144.66f), Vector3(1110.411f, 1100.113f, 158.152f))))
      ZipLinePaths(new ZipLinePath(46, true, List(Vector3(1114.444f, 979.815f, 293.08f), Vector3(1109.115f, 962.793f, 308.08f))))
      ZipLinePaths(new ZipLinePath(47, true, List(Vector3(1116.152f, 1049.326f, 142.011f), Vector3(1115.709f, 1049.785f, 176.78f))))
      ZipLinePaths(new ZipLinePath(48, false, List(Vector3(1117.339f, 1055.876f, 177.28f), Vector3(1086.425f, 1067.357f, 215.618f), Vector3(1055.51f, 1078.838f, 253.203f), Vector3(1041.598f, 1084.004f, 287.634f), Vector3(1027.687f, 1089.171f, 286.868f))))
      ZipLinePaths(new ZipLinePath(49, false, List(Vector3(1122.141f, 828.5f, 238.746f), Vector3(1122.477f, 879.466f, 236.861f), Vector3(1122.813f, 930.432f, 235.059f), Vector3(1123.15f, 981.397f, 233.257f), Vector3(1123.486f, 1032.363f, 231.454f), Vector3(1123.822f, 1083.325f, 229.652f), Vector3(1123.901f, 1095.316f, 226.948f))))
      ZipLinePaths(new ZipLinePath(50, false, List(Vector3(1134.036f, 816.858f, 247.409f), Vector3(1151.499f, 864.219f, 255.407f), Vector3(1168.962f, 911.58f, 262.664f), Vector3(1186.424f, 958.942f, 275.422f), Vector3(1200.805f, 997.945f, 273.992f))))
      ZipLinePaths(new ZipLinePath(51, false, List(Vector3(1139.77f, 957.718f, 176.286f), Vector3(1132.14f, 908.884f, 185.036f), Vector3(1124.511f, 860.05f, 192.594f), Vector3(1115.539f, 816.446f, 198.783f))))
      ZipLinePaths(new ZipLinePath(52, true, List(Vector3(1142.063f, 961.706f, 141.954f), Vector3(1141.833f, 962.65f, 175.78f))))
      ZipLinePaths(new ZipLinePath(53, false, List(Vector3(1145.255f, 912.243f, 322.464f), Vector3(1103.666f, 923.587f, 370.23f), Vector3(1044.747f, 939.657f, 374.346f))))
      ZipLinePaths(new ZipLinePath(54, false, List(Vector3(1146.629f, 1086.09f, 159.359f), Vector3(1139.974f, 1061.468f, 161.182f), Vector3(1133.32f, 1036.847f, 154.174f), Vector3(1127.272f, 1018.487f, 151.164f))))
      ZipLinePaths(new ZipLinePath(55, false, List(Vector3(1147.433f, 952.501f, 403.715f), Vector3(1174.421f, 980.424f, 402.039f), Vector3(1196.024f, 997.07f, 385.462f), Vector3(1217.428f, 1021.717f, 379.623f))))
      ZipLinePaths(new ZipLinePath(56, false, List(Vector3(1146.091f, 1104.1f, 226.953f), Vector3(1153.881f, 1075.162f, 231.716f), Vector3(1157.777f, 1060.694f, 233.893f), Vector3(1161.672f, 1046.225f, 235.775f))))
      ZipLinePaths(new ZipLinePath(57, false, List(Vector3(1155.908f, 799.632f, 198.811f), Vector3(1168.474f, 810.457f, 198.687f), Vector3(1189.041f, 833.683f, 180.751f))))
      ZipLinePaths(new ZipLinePath(58, false, List(Vector3(1166.637f, 1138.424f, 172.501f), Vector3(1141.554f, 1141.091f, 174.082f), Vector3(1116.472f, 1143.758f, 176.164f), Vector3(1066.306f, 1149.092f, 177.327f), Vector3(963.976f, 1160.06f, 175.154f))))
      ZipLinePaths(new ZipLinePath(59, false, List(Vector3(1174.081f, 905.841f, 276.08f), Vector3(1130.6f, 885.114f, 263.381f), Vector3(1087.118f, 864.386f, 249.969f), Vector3(1085.379f, 863.557f, 247.451f))))
      ZipLinePaths(new ZipLinePath(60, false, List(Vector3(1187.233f, 1047.423f, 288.282f), Vector3(1137.464f, 1046.202f, 284.343f), Vector3(1087.696f, 1044.981f, 279.663f), Vector3(1037.928f, 1043.76f, 274.983f), Vector3(988.16f, 1042.54f, 270.303f), Vector3(957.304f, 1041.783f, 266.706f))))
      ZipLinePaths(new ZipLinePath(61, true, List(Vector3(1195.866f, 1024.645f, 358.26f), Vector3(1196.547f, 1025.43f, 364.697f))))
      ZipLinePaths(new ZipLinePath(62, false, List(Vector3(1202.279f, 1065.203f, 379.973f), Vector3(1158.316f, 1091.763f, 366.789f), Vector3(1087.976f, 1134.259f, 329.503f))))
      ZipLinePaths(new ZipLinePath(63, false, List(Vector3(1218.329f, 982.117f, 322.441f), Vector3(1167.462f, 1031.49f, 333.463f), Vector3(1135.987f, 1060.504f, 343.613f), Vector3(1120.25f, 1075.012f, 352.689f), Vector3(1107.313f, 1084.519f, 353.763f))))
      ZipLinePaths(new ZipLinePath(64, false, List(Vector3(1205.974f, 885.662f, 276.074f), Vector3(1174.101f, 851.537f, 258.901f), Vector3(1142.229f, 817.412f, 238.784f))))
      ZipLinePaths(new ZipLinePath(65, false, List(Vector3(1209.137f, 1025.929f, 379.998f), Vector3(1164.331f, 1045.57f, 370.44f), Vector3(1098.915f, 1074.246f, 353.764f))))
      ZipLinePaths(new ZipLinePath(66, false, List(Vector3(1210.968f, 1035.861f, 159.356f), Vector3(1220.734f, 986.826f, 159.704f), Vector3(1229.132f, 944.655f, 157.68f))))
      ZipLinePaths(new ZipLinePath(67, false, List(Vector3(1211.43f, 918.994f, 193.301f), Vector3(1162.516f, 912.134f, 181.339f), Vector3(1113.603f, 905.273f, 173.561f), Vector3(1064.689f, 898.413f, 165.783f), Vector3(1038.276f, 894.708f, 161.212f))))
      ZipLinePaths(new ZipLinePath(68, false, List(Vector3(1212.954f, 1184.48f, 258.387f), Vector3(1241.887f, 1146.113f, 277.704f), Vector3(1267.32f, 1107.245f, 296.218f), Vector3(1292.754f, 1068.378f, 314.731f), Vector3(1305.725f, 1048.556f, 330.249f), Vector3(1318.696f, 1028.734f, 333.772f))))
      ZipLinePaths(new ZipLinePath(69, false, List(Vector3(1223.233f, 1112.938f, 190.58f), Vector3(1231.772f, 1063.768f, 188.214f), Vector3(1240.31f, 1014.599f, 187.788f), Vector3(1248.849f, 965.432f, 185.763f), Vector3(1257.388f, 916.265f, 183.037f), Vector3(1259.362f, 898.065f, 180.716f))))
      ZipLinePaths(new ZipLinePath(70, false, List(Vector3(1230.107f, 1045.135f, 235.77f), Vector3(1244.028f, 1094.167f, 238.081f), Vector3(1257.948f, 1143.2f, 239.652f), Vector3(1264.226f, 1165.313f, 237.807f))))
      ZipLinePaths(new ZipLinePath(71, false, List(Vector3(1231.002f, 1035.87f, 235.822f), Vector3(1254.525f, 996.863f, 257.139f), Vector3(1271.932f, 967.997f, 272.932f))))
      ZipLinePaths(new ZipLinePath(72, false, List(Vector3(1234.91f, 912.664f, 157.928f), Vector3(1250.804f, 960.648f, 165.181f), Vector3(1266.698f, 1008.632f, 171.942f), Vector3(1270.75f, 1020.863f, 172.504f))))
      ZipLinePaths(new ZipLinePath(73, false, List(Vector3(1255.485f, 1182.81f, 237.946f), Vector3(1206.913f, 1195.741f, 241.873f), Vector3(1195.984f, 1200.291f, 246.675f), Vector3(1144.741f, 1209.557f, 249.088f), Vector3(1096.169f, 1221.288f, 250.963f), Vector3(1047.598f, 1233.019f, 251.938f), Vector3(1032.812f, 1235.4f, 247.246f))))
      ZipLinePaths(new ZipLinePath(74, true, List(Vector3(1265.476f, 927.817f, 279.476f), Vector3(1239.733f, 924.791f, 275.521f))))
      ZipLinePaths(new ZipLinePath(75, false, List(Vector3(1313.487f, 1015.418f, 333.739f), Vector3(1293.092f, 1007.868f, 334.409f), Vector3(1270.097f, 998.818f, 326.879f), Vector3(1241.583f, 987.596f, 322.436f))))
      ZipLinePaths(new ZipLinePath(76, true, List(Vector3(845.661f, 953.94f, 284.032f), Vector3(833.71f, 937.902f, 284.032f))))
      ZipLinePaths(new ZipLinePath(77, false, List(Vector3(1101.112f, 1092.318f, 158.581f), Vector3(1043.9f, 1067.37f, 165.921f), Vector3(986.689f, 1042.422f, 164.515f))))
      ZipLinePaths(new ZipLinePath(78, false, List(Vector3(1053.642f, 972.17f, 298.563f), Vector3(1016.137f, 939.284f, 295.811f), Vector3(988.785f, 915.864f, 290.753f))))
      ZipLinePaths(new ZipLinePath(79, true, List(Vector3(1074.661f, 994.089f, 303.08f), Vector3(1074.232f, 994.714f, 308.08f))))
      ZipLinePaths(new ZipLinePath(80, true, List(Vector3(1104.434f, 964.517f, 298.08f), Vector3(1104.444f, 964.415f, 303.08f))))
      ZipLinePaths(new ZipLinePath(81, false, List(Vector3(1208.005f, 939.596f, 276.045f), Vector3(1208.715f, 962.582f, 276.316f), Vector3(1209.426f, 985.568f, 273.988f))))
      ZipLinePaths(new ZipLinePath(82, false, List(Vector3(1179.915f, 1003.14f, 273.987f), Vector3(1160.46f, 995.32f, 293.853f), Vector3(1141.005f, 987.5f, 305.972f), Vector3(1127.232f, 982.307f, 308.622f))))
      ZipLinePaths(new ZipLinePath(83, false, List(Vector3(1127.071f, 968.872f, 308.503f), Vector3(1123.104f, 919.425f, 315.516f), Vector3(1121.438f, 898.657f, 314.64f))))
      ZipLinePaths(new ZipLinePath(84, false, List(Vector3(1046.507f, 915.223f, 314.639f), Vector3(1025.3f, 953.238f, 318.323f), Vector3(1014.696f, 972.745f, 314.793f), Vector3(1002.593f, 984.253f, 311.55f))))
      ZipLinePaths(new ZipLinePath(85, false, List(Vector3(1096.244f, 942.801f, 298.563f), Vector3(1099.948f, 933.29f, 297.281f), Vector3(1103.653f, 921.579f, 289.539f), Vector3(1111.063f, 900.358f, 272.895f), Vector3(1125.882f, 857.914f, 247.906f), Vector3(1133.885f, 834.994f, 238.784f))))
      ZipLinePaths(new ZipLinePath(86, false, List(Vector3(1097.66f, 942.464f, 303.601f), Vector3(1135.823f, 912.095f, 324.74f), Vector3(1147.795f, 902.568f, 322.452f))))
      ZipLinePaths(new ZipLinePath(87, false, List(Vector3(1082.528f, 1018.227f, 303.597f), Vector3(1079.498f, 1050.159f, 301.494f), Vector3(1075.269f, 1067.092f, 291.668f), Vector3(1066.51f, 1121.357f, 276.083f))))
      ZipLinePaths(new ZipLinePath(88, false, List(Vector3(1098.415f, 1019.645f, 308.636f), Vector3(1117.982f, 1034.342f, 306.546f), Vector3(1137.549f, 1046.439f, 290.602f), Vector3(1150.978f, 1055.634f, 280.545f), Vector3(1164.406f, 1064.828f, 274.032f))))
      ZipLinePaths(new ZipLinePath(89, false, List(Vector3(1178.956f, 772.235f, 282.489f), Vector3(1204.251f, 811.257f, 304.124f), Vector3(1229.045f, 845.879f, 324.961f), Vector3(1231.033f, 852.574f, 322.473f))))
      ZipLinePaths(new ZipLinePath(90, false, List(Vector3(1112.743f, 751.134f, 282.462f), Vector3(1088.902f, 756.732f, 284.043f), Vector3(1065.062f, 762.33f, 273.139f), Vector3(1017.381f, 773.527f, 263.073f), Vector3(969.701f, 784.724f, 253.007f), Vector3(951.396f, 782.683f, 243.001f))))
      ZipLinePaths(new ZipLinePath(91, false, List(Vector3(943.422f, 797.726f, 243.265f), Vector3(916.11f, 837.921f, 259.473f), Vector3(888.799f, 878.117f, 281.944f), Vector3(868.449f, 908.066f, 284.555f))))
      ZipLinePaths(new ZipLinePath(92, false, List(Vector3(879.92f, 943.053f, 284.535f), Vector3(902.968f, 950.268f, 293.299f), Vector3(926.016f, 957.483f, 290.07f))))
      ZipLinePaths(new ZipLinePath(93, false, List(Vector3(1315.471f, 956.81f, 322.442f), Vector3(1332.853f, 1004.046f, 316.074f), Vector3(1347.126f, 1047.02f, 302.549f))))
      ZipLinePaths(new ZipLinePath(94, false, List(Vector3(1319.352f, 1102.721f, 302.539f), Vector3(1274.436f, 1124.169f, 308.086f), Vector3(1251.978f, 1134.893f, 312.979f), Vector3(1229.521f, 1145.617f, 310.373f))))
      ZipLinePaths(new ZipLinePath(95, false, List(Vector3(1174.169f, 1166.809f, 310.388f), Vector3(1131.408f, 1190.417f, 300.433f), Vector3(1088.647f, 1214.025f, 289.744f), Vector3(1067.266f, 1225.83f, 279.474f))))
      ZipLinePaths(new ZipLinePath(96, false, List(Vector3(975.449f, 1215.112f, 279.493f), Vector3(951.71f, 1171.246f, 280.186f), Vector3(927.97f, 1127.38f, 273.129f), Vector3(907.554f, 1089.655f, 266.706f))))
      ZipLinePaths(new ZipLinePath(97, false, List(Vector3(1310.139f, 1087.265f, 302.549f), Vector3(1262.453f, 1075.564f, 293.844f), Vector3(1245.285f, 1071.352f, 288.291f))))
      ZipLinePaths(new ZipLinePath(98, true, List(Vector3(837.962f, 950.527f, 284.032f), Vector3(839.502f, 937.417f, 297.199f))))
      ZipLinePaths(new ZipLinePath(99, false, List(Vector3(1107.188f, 867.405f, 157.685f), Vector3(1057.731f, 866.462f, 167.734f), Vector3(1033.002f, 865.991f, 168.798f))))
      ZipLinePaths(new ZipLinePath(100, false, List(Vector3(1034.19f, 1151.24f, 276.07f), Vector3(1027.155f, 1171.499f, 282.137f), Vector3(1020.12f, 1186.259f, 279.493f))))
      ZipLinePaths(new ZipLinePath(101, false, List(Vector3(1182.343f, 1165.265f, 258.349f), Vector3(1182.153f, 1158.271f, 259.739f), Vector3(1181.964f, 1151.276f, 257.847f))))
      ZipLinePaths(new ZipLinePath(102, false, List(Vector3(1188.742f, 1150.284f, 226.948f), Vector3(1191.712f, 1126.486f, 218.923f), Vector3(1194.683f, 1102.689f, 201.903f), Vector3(1197.654f, 1078.891f, 176.509f), Vector3(1200.624f, 1055.093f, 159.356f))))
      ZipLinePaths(new ZipLinePath(103, true, List(Vector3(845.985f, 940.029f, 284.032f), Vector3(857.812f, 940.784f, 284.032f))))
      ZipLinePaths(new ZipLinePath(104, true, List(Vector3(913.217f, 1126.673f, 167.8f), Vector3(905.95f, 1128.761f, 185.226f))))
      ZipLinePaths(new ZipLinePath(105, true, List(Vector3(985.057f, 1117.279f, 185.226f), Vector3(977.383f, 1119.22f, 167.8f))))
      ZipLinePaths(new ZipLinePath(106, true, List(Vector3(1194.756f, 855.428f, 162.8f), Vector3(1186.752f, 855.411f, 180.226f))))
      ZipLinePaths(new ZipLinePath(107, true, List(Vector3(1266.08f, 858.242f, 180.226f), Vector3(1258.221f, 859.327f, 162.8f))))
      ZipLinePaths(new ZipLinePath(108, false, List(Vector3(1022.699f, 1016.759f, 176.299f), Vector3(1059.517f, 982.522f, 168.496f), Vector3(1096.335f, 948.284f, 159.955f), Vector3(1133.154f, 914.047f, 150.468f))))
      ZipLinePaths(new ZipLinePath(109, true, List(Vector3(999.063f, 970.346f, 243.032f), Vector3(992.956f, 976.089f, 248.532f))))
      ZipLinePaths(new ZipLinePath(110, true, List(Vector3(1213.98f, 1136.817f, 257.332f), Vector3(1205.284f, 1135.873f, 262.831f))))
      ZipLinePaths(new ZipLinePath(111, false, List(Vector3(1195.134f, 1025.819f, 224.31f), Vector3(1196.763f, 1005.379f, 221.528f), Vector3(1198.393f, 984.938f, 196.447f), Vector3(1201.652f, 944.057f, 167.843f), Vector3(1202.63f, 931.792f, 157.68f))))
      ZipLinePaths(new ZipLinePath(112, false, List(Vector3(1195.142f, 1055.611f, 224.31f), Vector3(1176.148f, 1102.906f, 226.827f), Vector3(1167.582f, 1124.236f, 226.948f))))
      ZipLinePaths(new ZipLinePath(113, true, List(Vector3(1187.101f, 1041.722f, 223.81f), Vector3(1187.004f, 1030.267f, 230.47f))))
      ZipLinePaths(new ZipLinePath(114, false, List(Vector3(1007.547f, 268.934f, 243.11f), Vector3(1007.527f, 292.646f, 244.644f), Vector3(1018.508f, 316.357f, 244.279f), Vector3(1024.908f, 330.77f, 243.857f), Vector3(1035.909f, 347.383f, 243.135f))))
      ZipLinePaths(new ZipLinePath(115, false, List(Vector3(1068.89f, 344.904f, 243.148f), Vector3(1070.937f, 349.779f, 243.278f), Vector3(1072.784f, 354.654f, 243.921f), Vector3(1074.728f, 380.202f, 237.744f), Vector3(1075.073f, 405.551f, 231.566f), Vector3(1072.861f, 425.05f, 229.663f), Vector3(1073.449f, 444.349f, 228.36f), Vector3(1075.437f, 463.847f, 225.656f), Vector3(1075.625f, 483.346f, 225.553f), Vector3(1076.013f, 534.043f, 210.998f), Vector3(1075.593f, 583.763f, 198.865f), Vector3(1072.743f, 599.762f, 185.09f))))
      ZipLinePaths(new ZipLinePath(116, false, List(Vector3(1049.463f, 621.931f, 183.136f), Vector3(1053.439f, 640.477f, 184.404f), Vector3(1058.226f, 649.75f, 184.889f), Vector3(1063.214f, 659.024f, 185.773f), Vector3(1070.682f, 677.638f, 184.48f), Vector3(1070.351f, 696.253f, 182.787f), Vector3(1075.419f, 712.467f, 181.094f), Vector3(1072.688f, 730.282f, 177.102f))))
      ZipLinePaths(new ZipLinePath(117, false, List(Vector3(993.254f, 456.81f, 223.2f), Vector3(1005.741f, 456.241f, 223.7f), Vector3(1011.984f, 455.957f, 223.7f), Vector3(1018.228f, 455.673f, 223.201f))))
      ZipLinePaths(new ZipLinePath(118, false, List(Vector3(960.308f, 504.697f, 218.191f), Vector3(938.836f, 502.633f, 219.836f), Vector3(929.464f, 487.568f, 219.381f), Vector3(919.607f, 494.021f, 219.521f), Vector3(919.542f, 520.634f, 222.656f), Vector3(919.478f, 547.248f, 223.79f), Vector3(938.214f, 551.461f, 223.725f), Vector3(967.915f, 554.863f, 223.617f), Vector3(967.766f, 571.564f, 223.363f), Vector3(966.692f, 580.615f, 223.236f), Vector3(952.817f, 588.865f, 223.202f))))
      ZipLinePaths(new ZipLinePath(119, false, List(Vector3(941.828f, 613.476f, 223.231f), Vector3(925.918f, 616.335f, 223.995f), Vector3(920.563f, 629.564f, 227.029f), Vector3(921.008f, 642.794f, 228.864f), Vector3(934.83f, 647.518f, 228.224f))))
      ZipLinePaths(new ZipLinePath(120, false, List(Vector3(948.435f, 649.569f, 228.256f), Vector3(952.455f, 664.02f, 228.749f), Vector3(956.475f, 678.47f, 228.243f))))
      ZipLinePaths(new ZipLinePath(121, false, List(Vector3(942.908f, 710.963f, 233.188f), Vector3(936.267f, 720.101f, 235.096f), Vector3(936.451f, 734.912f, 237.896f), Vector3(922.235f, 750.819f, 238.936f), Vector3(922.627f, 767.772f, 243.356f), Vector3(922.219f, 784.725f, 243.576f), Vector3(921.702f, 787.641f, 243.208f))))
      ZipLinePaths(new ZipLinePath(122, false, List(Vector3(1111.15f, 207.047f, 243.135f), Vector3(1157.929f, 227.358f, 243.888f), Vector3(1204.708f, 247.669f, 243.885f), Vector3(1209.294f, 249.66f, 243.135f))))
      ZipLinePaths(new ZipLinePath(123, false, List(Vector3(1198.789f, 297.547f, 243.146f), Vector3(1197.337f, 322.502f, 247.098f), Vector3(1197.485f, 347.457f, 249.86f), Vector3(1197.781f, 397.368f, 264.825f), Vector3(1197.277f, 447.279f, 277.09f), Vector3(1197.109f, 463.915f, 281.276f), Vector3(1197.37f, 477.616f, 283.749f), Vector3(1196.881f, 486.424f, 283.146f))))
      ZipLinePaths(new ZipLinePath(124, false, List(Vector3(1202.583f, 536.019f, 283.135f), Vector3(1204.13f, 561.193f, 285.171f), Vector3(1204.677f, 586.368f, 286.707f), Vector3(1202.824f, 611.543f, 287.818f), Vector3(1191.372f, 636.718f, 288.528f), Vector3(1179.116f, 659.424f, 288.253f), Vector3(1170.564f, 676.207f, 286.677f), Vector3(1170.612f, 692.99f, 284.7f), Vector3(1173.108f, 726.557f, 282.447f))))
      ZipLinePaths(new ZipLinePath(125, false, List(Vector3(1769.642f, 876.984f, 243.134f), Vector3(1724.537f, 898.566f, 244.689f), Vector3(1701.985f, 909.357f, 243.135f))))
      ZipLinePaths(new ZipLinePath(126, false, List(Vector3(1687.12f, 945.911f, 241.987f), Vector3(1635.308f, 946.216f, 228.89f), Vector3(1608.802f, 954.568f, 229.295f), Vector3(1583.497f, 970.921f, 230.9f), Vector3(1575.821f, 972.773f, 223.142f))))
      ZipLinePaths(new ZipLinePath(127, false, List(Vector3(1574.066f, 944.007f, 223.145f), Vector3(1564.26f, 943.102f, 223.317f), Vector3(1539.255f, 940.054f, 217.963f), Vector3(1514.25f, 939.205f, 212.809f), Vector3(1501.992f, 941.023f, 209.431f), Vector3(1489.735f, 940.042f, 206.053f), Vector3(1464.82f, 941.279f, 198.096f), Vector3(1440.505f, 942.916f, 190.639f), Vector3(1416.191f, 944.553f, 188.783f), Vector3(1367.161f, 943.427f, 184.27f), Vector3(1349.51f, 950.758f, 178.695f))))
      ZipLinePaths(new ZipLinePath(128, false, List(Vector3(1844.368f, 962.199f, 243.288f), Vector3(1843.209f, 986.147f, 246.165f), Vector3(1826.049f, 1030.095f, 248.742f), Vector3(1813.796f, 1045.782f, 243.135f))))
      ZipLinePaths(new ZipLinePath(129, false, List(Vector3(1770.828f, 1066.209f, 243.135f), Vector3(1726.199f, 1088.756f, 243.877f), Vector3(1703.885f, 1100.029f, 243.135f))))
      ZipLinePaths(new ZipLinePath(130, false, List(Vector3(1706.104f, 1133.887f, 243.154f), Vector3(1681.993f, 1139.631f, 252.471f), Vector3(1657.881f, 1141.375f, 259.707f), Vector3(1609.657f, 1136.062f, 277.52f), Vector3(1593.98f, 1137.395f, 283.015f), Vector3(1577.906f, 1137.772f, 284.225f), Vector3(1561.433f, 1132.349f, 283.154f))))
      ZipLinePaths(new ZipLinePath(131, false, List(Vector3(1551.103f, 1093.186f, 283.14f), Vector3(1526.704f, 1077.822f, 287.951f), Vector3(1502.305f, 1068.658f, 289.866f), Vector3(1480.834f, 1067.718f, 294.486f), Vector3(1459.363f, 1067.777f, 299.906f), Vector3(1443.772f, 1069.81f, 303.887f), Vector3(1399.83f, 1066.129f, 303.655f))))
      ZipLinePaths(new ZipLinePath(132, false, List(Vector3(1222.883f, 1155.354f, 310.372f), Vector3(1233.227f, 1169.735f, 309.998f), Vector3(1229.371f, 1162.916f, 262.124f), Vector3(1235.989f, 1170.63f, 212.147f), Vector3(1240.919f, 1176.377f, 172.5f))))
      ZipLinePaths(new ZipLinePath(133, false, List(Vector3(1175.587f, 1239.146f, 173.631f), Vector3(1171.886f, 1262.498f, 181.983f), Vector3(1171.393f, 1276.12f, 185.559f), Vector3(1170.751f, 1301.419f, 189.13f), Vector3(1171.108f, 1326.718f, 195.702f), Vector3(1171.043f, 1376.342f, 206.614f), Vector3(1172.179f, 1425.966f, 221.727f), Vector3(1170.049f, 1436.669f, 223.181f), Vector3(1171.12f, 1447.372f, 223.149f))))
      ZipLinePaths(new ZipLinePath(134, false, List(Vector3(1166.209f, 1497.571f, 223.137f), Vector3(1166.17f, 1547.911f, 226.843f), Vector3(1170.072f, 1588.874f, 234.821f), Vector3(1168.974f, 1629.837f, 243.999f), Vector3(1189.267f, 1681.165f, 247.254f), Vector3(1198.153f, 1698.932f, 244.947f), Vector3(1193.839f, 1716.699f, 243.141f))))
      ZipLinePaths(new ZipLinePath(135, false, List(Vector3(1158.399f, 1784.486f, 243.843f), Vector3(1111.506f, 1772.128f, 245.649f), Vector3(1065.55f, 1750.117f, 243.135f))))
      ZipLinePaths(new ZipLinePath(136, false, List(Vector3(1045.72f, 1705.07f, 243.135f), Vector3(1022.973f, 1660.539f, 243.887f), Vector3(1014.33f, 1643.617f, 243.135f))))
      ZipLinePaths(new ZipLinePath(137, false, List(Vector3(978.861f, 1641.87f, 243.153f), Vector3(970.657f, 1593.573f, 258.04f), Vector3(971.092f, 1584.88f, 260.944f), Vector3(973.55f, 1521.127f, 280.863f), Vector3(973.104f, 1507.604f, 283.349f), Vector3(979.459f, 1494.081f, 283.153f))))
      ZipLinePaths(new ZipLinePath(138, false, List(Vector3(1027.608f, 1471.856f, 283.135f), Vector3(1044.922f, 1421.966f, 285.242f), Vector3(1042.036f, 1372.076f, 292.602f), Vector3(1042.75f, 1322.185f, 291.763f), Vector3(1040.202f, 1282.273f, 281.511f))))
      ZipLinePaths(new ZipLinePath(139, false, List(Vector3(1140.673f, 973.599f, 176.313f), Vector3(1138.394f, 1020.478f, 196.964f), Vector3(1136.116f, 1067.357f, 216.913f), Vector3(1135.446f, 1081.145f, 225.28f), Vector3(1134.776f, 1094.932f, 226.989f))))
      ZipLinePaths(new ZipLinePath(140, false, List(Vector3(1150.479f, 968.34f, 176.29f), Vector3(1170.472f, 984.147f, 176.49f), Vector3(1190.466f, 999.954f, 176.39f), Vector3(1230.453f, 1031.568f, 174.132f), Vector3(1253.974f, 1050.164f, 172.501f))))
      ZipLinePaths(new ZipLinePath(141, false, List(Vector3(1106.342f, 1106.197f, 226.95f), Vector3(1054.565f, 1048.43f, 236.347f), Vector3(1002.788f, 990.663f, 243.55f))))
      ZipLinePaths(new ZipLinePath(142, false, List(Vector3(984.255f, 1002.808f, 243.547f), Vector3(985.338f, 1010.52f, 242.265f), Vector3(986.422f, 1018.232f, 227.484f), Vector3(988.588f, 1033.656f, 203.92f), Vector3(992.666f, 1062.689f, 163.68f))))
      ZipLinePaths(new ZipLinePath(143, false, List(Vector3(1116.114f, 1046.375f, 177.28f), Vector3(1147.41f, 1042.571f, 218.111f), Vector3(1153.853f, 1041.788f, 233.89f), Vector3(1160.296f, 1041.005f, 235.77f))))
      ZipLinePaths(new ZipLinePath(144, false, List(Vector3(991.811f, 888.539f, 243.475f), Vector3(1039.377f, 875.966f, 257.645f), Vector3(1046.137f, 873.294f, 258.908f))))
      ZipLinePaths(new ZipLinePath(145, false, List(Vector3(1098.527f, 830.352f, 258.89f), Vector3(1102.497f, 826.527f, 258.518f), Vector3(1106.468f, 822.701f, 238.745f))))
      ZipLinePaths(new ZipLinePath(146, false, List(Vector3(1094.341f, 828.58f, 258.89f), Vector3(1089.367f, 823.039f, 258.018f), Vector3(1085.594f, 827.098f, 235.645f), Vector3(1076.848f, 825.616f, 212.9f), Vector3(1059.355f, 822.653f, 166.153f), Vector3(1055.157f, 821.942f, 152.602f))))
      ZipLinePaths(new ZipLinePath(147, false, List(Vector3(1101.107f, 835.443f, 258.89f), Vector3(1105.861f, 841.292f, 255.843f), Vector3(1108.616f, 842.741f, 235.297f), Vector3(1115.326f, 847.639f, 212.204f), Vector3(1129.545f, 859.835f, 164.77f), Vector3(1130.939f, 861.031f, 157.68f))))
      ZipLinePaths(new ZipLinePath(148, false, List(Vector3(1118.348f, 875.124f, 150.465f), Vector3(1118.914f, 870.112f, 157.173f), Vector3(1119.479f, 865.099f, 157.68f))))
      ZipLinePaths(new ZipLinePath(149, false, List(Vector3(1205.55f, 1041.13f, 224.344f), Vector3(1217.347f, 1039.221f, 223.849f), Vector3(1229.145f, 1037.313f, 222.554f), Vector3(1252.74f, 1033.497f, 207.297f), Vector3(1299.93f, 1025.864f, 189.531f), Vector3(1336.016f, 1020.029f, 174.385f))))
      ZipLinePaths(new ZipLinePath(150, false, List(Vector3(1122.96f, 1201.699f, 226.948f), Vector3(1121.853f, 1205.357f, 226.351f), Vector3(1120.746f, 1209.015f, 223.454f), Vector3(1118.532f, 1215.13f, 206.461f), Vector3(1114.104f, 1228.561f, 186.473f), Vector3(1111.624f, 1236.082f, 172.915f))))
      ZipLinePaths(new ZipLinePath(151, false, List(Vector3(1058.953f, 1136.079f, 226.996f), Vector3(1036.782f, 1130.339f, 224.438f), Vector3(1014.612f, 1124.599f, 207.629f), Vector3(999.536f, 1120.695f, 199.391f))))
      ZipLinePaths(new ZipLinePath(152, false, List(Vector3(980.433f, 1115.376f, 198.361f), Vector3(969.648f, 1109.169f, 197.774f), Vector3(958.864f, 1102.962f, 195.794f))))
      ZipLinePaths(new ZipLinePath(153, false, List(Vector3(904.04f, 1103.835f, 185.723f), Vector3(892.316f, 1095.63f, 184.78f), Vector3(886.454f, 1091.528f, 183.762f), Vector3(883.524f, 1089.477f, 183.053f), Vector3(880.593f, 1087.427f, 182.544f), Vector3(876.55f, 1084.598f, 182.144f), Vector3(835.315f, 1055.742f, 171.252f))))
      ZipLinePaths(new ZipLinePath(154, false, List(Vector3(1133.265f, 778.345f, 198.777f), Vector3(1131.568f, 771.562f, 198.189f), Vector3(1129.872f, 764.779f, 194.804f), Vector3(1126.478f, 751.214f, 169.036f))))
      ZipLinePaths(new ZipLinePath(155, false, List(Vector3(1108.775f, 811.315f, 198.776f), Vector3(1106.722f, 813.77f, 198.054f), Vector3(1104.669f, 816.226f, 175.432f), Vector3(1102.963f, 817.937f, 153.088f))))
      ZipLinePaths(new ZipLinePath(156, false, List(Vector3(306.846f, 1041.014f, 233.036f), Vector3(305.881f, 1023.7f, 236.553f), Vector3(342.015f, 951.186f, 234.539f), Vector3(393.984f, 925.058f, 236.511f), Vector3(405.219f, 918.196f, 233.135f))))
      ZipLinePaths(new ZipLinePath(157, false, List(Vector3(422.655f, 878.634f, 234.823f), Vector3(462.703f, 878.635f, 252.642f), Vector3(512.448f, 879.331f, 268.067f), Vector3(537.916f, 878.972f, 272.078f))))
      ZipLinePaths(new ZipLinePath(158, false, List(Vector3(550.586f, 914.859f, 273.135f), Vector3(609.031f, 945.539f, 276.386f), Vector3(648.677f, 946.718f, 274.915f), Vector3(662.14f, 946.656f, 273.135f))))
      ZipLinePaths(new ZipLinePath(159, false, List(Vector3(675.759f, 912.206f, 273.905f), Vector3(733.511f, 909.282f, 296.348f), Vector3(785.289f, 906.66f, 296.654f), Vector3(816.157f, 905.097f, 284.534f))))
      ZipLinePaths(new ZipLinePath(160, false, List(Vector3(336.043f, 1137.71f, 233.109f), Vector3(351.767f, 1137.575f, 237.165f), Vector3(416.64f, 1106.569f, 235.944f), Vector3(456.726f, 1105.644f, 223.541f), Vector3(470.089f, 1105.336f, 215.777f))))
      ZipLinePaths(new ZipLinePath(161, false, List(Vector3(487.928f, 1066.814f, 213.142f), Vector3(543.338f, 1039.103f, 216.896f), Vector3(599.192f, 1039.599f, 194.845f))))
      ZipLinePaths(new ZipLinePath(162, false, List(Vector3(616.478f, 1073.332f, 192.31f), Vector3(671.491f, 1073.589f, 177.53f), Vector3(736.004f, 1042.347f, 176.388f), Vector3(809.069f, 1041.654f, 174.357f))))
      ZipLinePaths(new ZipLinePath(163, true, List(Vector3(1201.936f, 910.752f, 325.469f), Vector3(1222.351f, 877.804f, 341.059f))))
      ZipLinePaths(new ZipLinePath(164, true, List(Vector3(1201.024f, 928.787f, 344.577f), Vector3(1248.519f, 941.266f, 331.252f), Vector3(1257.731f, 943.513f, 327.969f))))
      ZipLinePaths(new ZipLinePath(165, true, List(Vector3(964.275f, 1096.252f, 332.469f), Vector3(992.303f, 1071.769f, 345.489f))))
      ZipLinePaths(new ZipLinePath(166, true, List(Vector3(961.598f, 1106.636f, 349.565f), Vector3(1008.607f, 1122.679f, 338.887f), Vector3(1021.012f, 1127.083f, 334.969f))))
    }

    ZipLines()

  }
}
