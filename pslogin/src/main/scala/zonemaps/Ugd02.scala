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

object Ugd02 { // Hunhau
  val ZoneMap = new ZoneMap("ugd02") {
    Scale = MapScale.Dim2560
    Checksum = 2702486449L

    Building10093()

    def Building10093(): Unit = { // Name: ceiling_bldg_a_10093 Type: ceiling_bldg_a GUID: 1, MapID: 10093
      LocalBuilding("ceiling_bldg_a_10093", 1, 10093, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(922.89f, 1195.49f, 317.79f), ceiling_bldg_a)))
      LocalObject(1080, Door.Constructor(Vector3(914.628f, 1209.148f, 319.569f)), owning_building_guid = 1)
      LocalObject(1092, Door.Constructor(Vector3(930.5852f, 1178.308f, 325.075f)), owning_building_guid = 1)
      LocalObject(1093, Door.Constructor(Vector3(939.3843f, 1190.549f, 319.569f)), owning_building_guid = 1)
      LocalObject(1095, Door.Constructor(Vector3(942.5852f, 1199.093f, 325.075f)), owning_building_guid = 1)
    }

    Building10107()

    def Building10107(): Unit = { // Name: ceiling_bldg_a_10107 Type: ceiling_bldg_a GUID: 2, MapID: 10107
      LocalBuilding("ceiling_bldg_a_10107", 2, 10107, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(960.11f, 1317.32f, 314.7f), ceiling_bldg_a)))
      LocalObject(1097, Door.Constructor(Vector3(951.848f, 1330.978f, 316.479f)), owning_building_guid = 2)
      LocalObject(1102, Door.Constructor(Vector3(967.8052f, 1300.138f, 321.985f)), owning_building_guid = 2)
      LocalObject(1105, Door.Constructor(Vector3(976.6042f, 1312.379f, 316.479f)), owning_building_guid = 2)
      LocalObject(1106, Door.Constructor(Vector3(979.8052f, 1320.923f, 321.985f)), owning_building_guid = 2)
    }

    Building10105()

    def Building10105(): Unit = { // Name: ceiling_bldg_a_10105 Type: ceiling_bldg_a GUID: 3, MapID: 10105
      LocalBuilding("ceiling_bldg_a_10105", 3, 10105, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1178.77f, 1916.87f, 236.1f), ceiling_bldg_a)))
      LocalObject(1134, Door.Constructor(Vector3(1170.508f, 1930.528f, 237.879f)), owning_building_guid = 3)
      LocalObject(1137, Door.Constructor(Vector3(1186.465f, 1899.688f, 243.385f)), owning_building_guid = 3)
      LocalObject(1141, Door.Constructor(Vector3(1195.264f, 1911.929f, 237.879f)), owning_building_guid = 3)
      LocalObject(1143, Door.Constructor(Vector3(1198.465f, 1920.473f, 243.385f)), owning_building_guid = 3)
    }

    Building10084()

    def Building10084(): Unit = { // Name: ceiling_bldg_a_10084 Type: ceiling_bldg_a GUID: 4, MapID: 10084
      LocalBuilding("ceiling_bldg_a_10084", 4, 10084, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1331.57f, 907.05f, 299.45f), ceiling_bldg_a)))
      LocalObject(1190, Door.Constructor(Vector3(1317.586f, 914.747f, 301.229f)), owning_building_guid = 4)
      LocalObject(1194, Door.Constructor(Vector3(1346.825f, 896.018f, 306.735f)), owning_building_guid = 4)
      LocalObject(1195, Door.Constructor(Vector3(1346.825f, 920.018f, 306.735f)), owning_building_guid = 4)
      LocalObject(1196, Door.Constructor(Vector3(1348.325f, 911.018f, 301.229f)), owning_building_guid = 4)
    }

    Building10108()

    def Building10108(): Unit = { // Name: ceiling_bldg_b_10108 Type: ceiling_bldg_b GUID: 5, MapID: 10108
      LocalBuilding("ceiling_bldg_b_10108", 5, 10108, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(710.38f, 1451.92f, 264.12f), ceiling_bldg_b)))
      LocalObject(1017, Door.Constructor(Vector3(713.9198f, 1446.952f, 265.899f)), owning_building_guid = 5)
      LocalObject(1020, Door.Constructor(Vector3(723.465f, 1462.155f, 265.899f)), owning_building_guid = 5)
    }

    Building10078()

    def Building10078(): Unit = { // Name: ceiling_bldg_b_10078 Type: ceiling_bldg_b GUID: 6, MapID: 10078
      LocalBuilding("ceiling_bldg_b_10078", 6, 10078, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(861.71f, 528.17f, 218.06f), ceiling_bldg_b)))
      LocalObject(1053, Door.Constructor(Vector3(863.725f, 544.66f, 219.839f)), owning_building_guid = 6)
      LocalObject(1056, Door.Constructor(Vector3(867.726f, 527.16f, 219.839f)), owning_building_guid = 6)
    }

    Building10090()

    def Building10090(): Unit = { // Name: ceiling_bldg_b_10090 Type: ceiling_bldg_b GUID: 7, MapID: 10090
      LocalBuilding("ceiling_bldg_b_10090", 7, 10090, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(973.1f, 1046.64f, 288.2f), ceiling_bldg_b)))
      LocalObject(1104, Door.Constructor(Vector3(971.0607f, 1040.891f, 289.979f)), owning_building_guid = 7)
      LocalObject(1109, Door.Constructor(Vector3(988.9896f, 1041.792f, 289.979f)), owning_building_guid = 7)
    }

    Building10100()

    def Building10100(): Unit = { // Name: ceiling_bldg_b_10100 Type: ceiling_bldg_b GUID: 8, MapID: 10100
      LocalBuilding("ceiling_bldg_b_10100", 8, 10100, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1217.11f, 1317.72f, 291.99f), ceiling_bldg_b)))
      LocalObject(1144, Door.Constructor(Vector3(1200.521f, 1316.841f, 293.769f)), owning_building_guid = 8)
      LocalObject(1150, Door.Constructor(Vector3(1217.06f, 1323.82f, 293.769f)), owning_building_guid = 8)
    }

    Building10664()

    def Building10664(): Unit = { // Name: ceiling_bldg_b_10664 Type: ceiling_bldg_b GUID: 9, MapID: 10664
      LocalBuilding("ceiling_bldg_b_10664", 9, 10664, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1268.66f, 785.69f, 273.36f), ceiling_bldg_b)))
      LocalObject(1159, Door.Constructor(Vector3(1263.517f, 801.4866f, 275.139f)), owning_building_guid = 9)
      LocalObject(1168, Door.Constructor(Vector3(1274.539f, 787.3171f, 275.139f)), owning_building_guid = 9)
    }

    Building10101()

    def Building10101(): Unit = { // Name: ceiling_bldg_c_10101 Type: ceiling_bldg_c GUID: 10, MapID: 10101
      LocalBuilding("ceiling_bldg_c_10101", 10, 10101, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(732.43f, 973.16f, 280.28f), ceiling_bldg_c)))
      LocalObject(1021, Door.Constructor(Vector3(728.5452f, 974.9094f, 282.059f)), owning_building_guid = 10)
      LocalObject(1024, Door.Constructor(Vector3(751.15f, 1019.471f, 282.059f)), owning_building_guid = 10)
      LocalObject(1029, Door.Constructor(Vector3(773.0703f, 997.5509f, 282.059f)), owning_building_guid = 10)
    }

    Building10095()

    def Building10095(): Unit = { // Name: ceiling_bldg_c_10095 Type: ceiling_bldg_c GUID: 11, MapID: 10095
      LocalBuilding("ceiling_bldg_c_10095", 11, 10095, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(834.75f, 1318.05f, 293.22f), ceiling_bldg_c)))
      LocalObject(1042, Door.Constructor(Vector3(830.5447f, 1318.734f, 294.999f)), owning_building_guid = 11)
      LocalObject(1045, Door.Constructor(Vector3(840.8458f, 1367.628f, 294.999f)), owning_building_guid = 11)
      LocalObject(1054, Door.Constructor(Vector3(867.6926f, 1352.128f, 294.999f)), owning_building_guid = 11)
    }

    Building10091()

    def Building10091(): Unit = { // Name: ceiling_bldg_c_10091 Type: ceiling_bldg_c GUID: 12, MapID: 10091
      LocalBuilding("ceiling_bldg_c_10091", 12, 10091, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(860.22f, 1024.66f, 306.94f), ceiling_bldg_c)))
      LocalObject(1049, Door.Constructor(Vector3(856.3351f, 1026.409f, 308.719f)), owning_building_guid = 12)
      LocalObject(1059, Door.Constructor(Vector3(878.9399f, 1070.971f, 308.719f)), owning_building_guid = 12)
      LocalObject(1073, Door.Constructor(Vector3(900.8602f, 1049.051f, 308.719f)), owning_building_guid = 12)
    }

    Building10085()

    def Building10085(): Unit = { // Name: ceiling_bldg_c_10085 Type: ceiling_bldg_c GUID: 13, MapID: 10085
      LocalBuilding("ceiling_bldg_c_10085", 13, 10085, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1463.31f, 980.3f, 265.51f), ceiling_bldg_c)))
      LocalObject(1209, Door.Constructor(Vector3(1459.18f, 981.3482f, 267.289f)), owning_building_guid = 13)
      LocalObject(1210, Door.Constructor(Vector3(1473.704f, 1029.158f, 267.289f)), owning_building_guid = 13)
      LocalObject(1211, Door.Constructor(Vector3(1499.097f, 1011.378f, 267.289f)), owning_building_guid = 13)
    }

    Building10094()

    def Building10094(): Unit = { // Name: ceiling_bldg_e_10094 Type: ceiling_bldg_e GUID: 14, MapID: 10094
      LocalBuilding("ceiling_bldg_e_10094", 14, 10094, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(697.23f, 1294.78f, 269.71f), ceiling_bldg_e)))
      LocalObject(1014, Door.Constructor(Vector3(685.214f, 1293.79f, 271.489f)), owning_building_guid = 14)
      LocalObject(1016, Door.Constructor(Vector3(701.214f, 1327.29f, 276.989f)), owning_building_guid = 14)
      LocalObject(1018, Door.Constructor(Vector3(716.24f, 1322.796f, 271.489f)), owning_building_guid = 14)
      LocalObject(1019, Door.Constructor(Vector3(722.24f, 1302.796f, 276.989f)), owning_building_guid = 14)
    }

    Building10079()

    def Building10079(): Unit = { // Name: ceiling_bldg_e_10079 Type: ceiling_bldg_e GUID: 15, MapID: 10079
      LocalBuilding("ceiling_bldg_e_10079", 15, 10079, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(866.94f, 711.23f, 240.12f), ceiling_bldg_e)))
      LocalObject(1051, Door.Constructor(Vector3(860.9646f, 721.7018f, 241.899f)), owning_building_guid = 15)
      LocalObject(1063, Door.Constructor(Vector3(884.7747f, 691.9509f, 247.399f)), owning_building_guid = 15)
      LocalObject(1070, Door.Constructor(Vector3(898.0878f, 721.3586f, 247.399f)), owning_building_guid = 15)
      LocalObject(1072, Door.Constructor(Vector3(900.3651f, 705.8411f, 241.899f)), owning_building_guid = 15)
    }

    Building10096()

    def Building10096(): Unit = { // Name: ceiling_bldg_e_10096 Type: ceiling_bldg_e GUID: 16, MapID: 10096
      LocalBuilding("ceiling_bldg_e_10096", 16, 10096, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(907.97f, 1468.88f, 268.54f), ceiling_bldg_e)))
      LocalObject(1067, Door.Constructor(Vector3(887.7991f, 1494.685f, 275.819f)), owning_building_guid = 16)
      LocalObject(1071, Door.Constructor(Vector3(900.1734f, 1459.683f, 270.319f)), owning_building_guid = 16)
      LocalObject(1074, Door.Constructor(Vector3(901.6017f, 1502.132f, 270.319f)), owning_building_guid = 16)
      LocalObject(1085, Door.Constructor(Vector3(919.9866f, 1492.233f, 275.819f)), owning_building_guid = 16)
    }

    Building10102()

    def Building10102(): Unit = { // Name: ceiling_bldg_e_10102 Type: ceiling_bldg_e GUID: 17, MapID: 10102
      LocalBuilding("ceiling_bldg_e_10102", 17, 10102, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1051.15f, 1568.76f, 271.69f), ceiling_bldg_e)))
      LocalObject(1112, Door.Constructor(Vector3(1039.134f, 1567.77f, 273.469f)), owning_building_guid = 17)
      LocalObject(1113, Door.Constructor(Vector3(1055.134f, 1601.27f, 278.969f)), owning_building_guid = 17)
      LocalObject(1114, Door.Constructor(Vector3(1070.16f, 1596.776f, 273.469f)), owning_building_guid = 17)
      LocalObject(1115, Door.Constructor(Vector3(1076.16f, 1576.776f, 278.969f)), owning_building_guid = 17)
    }

    Building10086()

    def Building10086(): Unit = { // Name: ceiling_bldg_e_10086 Type: ceiling_bldg_e GUID: 18, MapID: 10086
      LocalBuilding("ceiling_bldg_e_10086", 18, 10086, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1364.98f, 1446.91f, 258.66f), ceiling_bldg_e)))
      LocalObject(1197, Door.Constructor(Vector3(1352.964f, 1445.92f, 260.439f)), owning_building_guid = 18)
      LocalObject(1199, Door.Constructor(Vector3(1368.964f, 1479.42f, 265.939f)), owning_building_guid = 18)
      LocalObject(1202, Door.Constructor(Vector3(1383.99f, 1474.926f, 260.439f)), owning_building_guid = 18)
      LocalObject(1204, Door.Constructor(Vector3(1389.99f, 1454.926f, 265.939f)), owning_building_guid = 18)
    }

    Building10092()

    def Building10092(): Unit = { // Name: ceiling_bldg_f_10092 Type: ceiling_bldg_f GUID: 19, MapID: 10092
      LocalBuilding("ceiling_bldg_f_10092", 19, 10092, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(765.59f, 1161.9f, 289.99f), ceiling_bldg_f)))
      LocalObject(1022, Door.Constructor(Vector3(734.7006f, 1163.41f, 291.769f)), owning_building_guid = 19)
      LocalObject(1033, Door.Constructor(Vector3(791.3768f, 1149.481f, 291.769f)), owning_building_guid = 19)
    }

    Building10778()

    def Building10778(): Unit = { // Name: ceiling_bldg_f_10778 Type: ceiling_bldg_f GUID: 20, MapID: 10778
      LocalBuilding("ceiling_bldg_f_10778", 20, 10778, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(822.13f, 896.66f, 253.11f), ceiling_bldg_f)))
      LocalObject(1036, Door.Constructor(Vector3(810.3592f, 922.7492f, 254.889f)), owning_building_guid = 20)
      LocalObject(1047, Door.Constructor(Vector3(844.8219f, 875.6477f, 254.889f)), owning_building_guid = 20)
    }

    Building10106()

    def Building10106(): Unit = { // Name: ceiling_bldg_f_10106 Type: ceiling_bldg_f GUID: 21, MapID: 10106
      LocalBuilding("ceiling_bldg_f_10106", 21, 10106, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1198.46f, 1426.56f, 309.72f), ceiling_bldg_f)))
      LocalObject(1136, Door.Constructor(Vector3(1184.323f, 1454.066f, 311.499f)), owning_building_guid = 21)
      LocalObject(1145, Door.Constructor(Vector3(1200.598f, 1398.018f, 311.499f)), owning_building_guid = 21)
    }

    Building10080()

    def Building10080(): Unit = { // Name: ceiling_bldg_g_10080 Type: ceiling_bldg_g GUID: 22, MapID: 10080
      LocalBuilding("ceiling_bldg_g_10080", 22, 10080, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(759.5f, 747.28f, 248.37f), ceiling_bldg_g)))
      LocalObject(1025, Door.Constructor(Vector3(751.516f, 730.77f, 250.149f)), owning_building_guid = 22)
      LocalObject(1026, Door.Constructor(Vector3(759.516f, 764.77f, 250.149f)), owning_building_guid = 22)
    }

    Building10087()

    def Building10087(): Unit = { // Name: ceiling_bldg_g_10087 Type: ceiling_bldg_g GUID: 23, MapID: 10087
      LocalBuilding("ceiling_bldg_g_10087", 23, 10087, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1159.94f, 1287.12f, 309.56f), ceiling_bldg_g)))
      LocalObject(1130, Door.Constructor(Vector3(1151.956f, 1270.61f, 311.339f)), owning_building_guid = 23)
      LocalObject(1131, Door.Constructor(Vector3(1159.956f, 1304.61f, 311.339f)), owning_building_guid = 23)
    }

    Building10083()

    def Building10083(): Unit = { // Name: ceiling_bldg_g_10083 Type: ceiling_bldg_g GUID: 24, MapID: 10083
      LocalBuilding("ceiling_bldg_g_10083", 24, 10083, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1214.34f, 914.27f, 295.15f), ceiling_bldg_g)))
      LocalObject(1148, Door.Constructor(Vector3(1209.829f, 931.1682f, 296.929f)), owning_building_guid = 24)
      LocalObject(1149, Door.Constructor(Vector3(1210.901f, 896.2562f, 296.929f)), owning_building_guid = 24)
    }

    Building10081()

    def Building10081(): Unit = { // Name: ceiling_bldg_h_10081 Type: ceiling_bldg_h GUID: 25, MapID: 10081
      LocalBuilding("ceiling_bldg_h_10081", 25, 10081, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(958.95f, 790.67f, 267.91f), ceiling_bldg_h)))
      LocalObject(1094, Door.Constructor(Vector3(942.46f, 786.686f, 269.689f)), owning_building_guid = 25)
      LocalObject(1098, Door.Constructor(Vector3(962.934f, 807.18f, 269.689f)), owning_building_guid = 25)
      LocalObject(1103, Door.Constructor(Vector3(970.035f, 779.482f, 272.189f)), owning_building_guid = 25)
    }

    Building10097()

    def Building10097(): Unit = { // Name: ceiling_bldg_h_10097 Type: ceiling_bldg_h GUID: 26, MapID: 10097
      LocalBuilding("ceiling_bldg_h_10097", 26, 10097, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(979.89f, 1487.96f, 269.47f), ceiling_bldg_h)))
      LocalObject(1099, Door.Constructor(Vector3(963.2613f, 1491.318f, 271.249f)), owning_building_guid = 26)
      LocalObject(1107, Door.Constructor(Vector3(985.2082f, 1473.135f, 273.749f)), owning_building_guid = 26)
      LocalObject(1110, Door.Constructor(Vector3(990.4781f, 1501.239f, 271.249f)), owning_building_guid = 26)
    }

    Building10082()

    def Building10082(): Unit = { // Name: ceiling_bldg_h_10082 Type: ceiling_bldg_h GUID: 27, MapID: 10082
      LocalBuilding("ceiling_bldg_h_10082", 27, 10082, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1111.54f, 899.56f, 290.68f), ceiling_bldg_h)))
      LocalObject(1117, Door.Constructor(Vector3(1095.05f, 895.576f, 292.459f)), owning_building_guid = 27)
      LocalObject(1123, Door.Constructor(Vector3(1115.524f, 916.07f, 292.459f)), owning_building_guid = 27)
      LocalObject(1125, Door.Constructor(Vector3(1122.625f, 888.372f, 294.959f)), owning_building_guid = 27)
    }

    Building10088()

    def Building10088(): Unit = { // Name: ceiling_bldg_h_10088 Type: ceiling_bldg_h GUID: 28, MapID: 10088
      LocalBuilding("ceiling_bldg_h_10088", 28, 10088, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1127.33f, 1060.86f, 311.15f), ceiling_bldg_h)))
      LocalObject(1122, Door.Constructor(Vector3(1110.453f, 1058.957f, 312.929f)), owning_building_guid = 28)
      LocalObject(1126, Door.Constructor(Vector3(1134.052f, 1075.103f, 315.429f)), owning_building_guid = 28)
      LocalObject(1128, Door.Constructor(Vector3(1136.714f, 1046.727f, 312.929f)), owning_building_guid = 28)
    }

    Building10103()

    def Building10103(): Unit = { // Name: ceiling_bldg_h_10103 Type: ceiling_bldg_h GUID: 29, MapID: 10103
      LocalBuilding("ceiling_bldg_h_10103", 29, 10103, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1186.59f, 1648.29f, 264.54f), ceiling_bldg_h)))
      LocalObject(1133, Door.Constructor(Vector3(1170.1f, 1644.306f, 266.319f)), owning_building_guid = 29)
      LocalObject(1139, Door.Constructor(Vector3(1190.574f, 1664.8f, 266.319f)), owning_building_guid = 29)
      LocalObject(1142, Door.Constructor(Vector3(1197.675f, 1637.102f, 268.819f)), owning_building_guid = 29)
    }

    Building10098()

    def Building10098(): Unit = { // Name: ceiling_bldg_h_10098 Type: ceiling_bldg_h GUID: 30, MapID: 10098
      LocalBuilding("ceiling_bldg_h_10098", 30, 10098, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1268.22f, 1049.38f, 307.96f), ceiling_bldg_h)))
      LocalObject(1154, Door.Constructor(Vector3(1251.73f, 1045.396f, 309.739f)), owning_building_guid = 30)
      LocalObject(1166, Door.Constructor(Vector3(1272.204f, 1065.89f, 309.739f)), owning_building_guid = 30)
      LocalObject(1173, Door.Constructor(Vector3(1279.305f, 1038.192f, 312.239f)), owning_building_guid = 30)
    }

    Building10099()

    def Building10099(): Unit = { // Name: ceiling_bldg_i_10099 Type: ceiling_bldg_i GUID: 31, MapID: 10099
      LocalBuilding("ceiling_bldg_i_10099", 31, 10099, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1303.08f, 1215.7f, 303.15f), ceiling_bldg_i)))
      LocalObject(1171, Door.Constructor(Vector3(1278.59f, 1219.216f, 304.929f)), owning_building_guid = 31)
      LocalObject(1192, Door.Constructor(Vector3(1328.59f, 1219.216f, 304.929f)), owning_building_guid = 31)
    }

    Building10104()

    def Building10104(): Unit = { // Name: ceiling_bldg_j_10104 Type: ceiling_bldg_j GUID: 32, MapID: 10104
      LocalBuilding("ceiling_bldg_j_10104", 32, 10104, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1128.96f, 1794.06f, 230.84f), ceiling_bldg_j)))
      LocalObject(1124, Door.Constructor(Vector3(1117.647f, 1799.353f, 232.619f)), owning_building_guid = 32)
      LocalObject(1129, Door.Constructor(Vector3(1140.305f, 1788.788f, 232.619f)), owning_building_guid = 32)
    }

    Building10089()

    def Building10089(): Unit = { // Name: ceiling_bldg_j_10089 Type: ceiling_bldg_j GUID: 33, MapID: 10089
      LocalBuilding("ceiling_bldg_j_10089", 33, 10089, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1443.23f, 1163.19f, 271.2f), ceiling_bldg_j)))
      LocalObject(1207, Door.Constructor(Vector3(1433.652f, 1155.174f, 272.979f)), owning_building_guid = 33)
      LocalObject(1208, Door.Constructor(Vector3(1452.803f, 1171.244f, 272.979f)), owning_building_guid = 33)
    }

    Building10357()

    def Building10357(): Unit = { // Name: ceiling_bldg_z_10357 Type: ceiling_bldg_z GUID: 34, MapID: 10357
      LocalBuilding("ceiling_bldg_z_10357", 34, 10357, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1366.28f, 1322.62f, 306.75f), ceiling_bldg_z)))
      LocalObject(1193, Door.Constructor(Vector3(1341.79f, 1326.636f, 308.529f)), owning_building_guid = 34)
      LocalObject(1206, Door.Constructor(Vector3(1398.79f, 1326.636f, 308.529f)), owning_building_guid = 34)
    }

    Building10016()

    def Building10016(): Unit = { // Name: ground_bldg_b_10016 Type: ground_bldg_b GUID: 273, MapID: 10016
      LocalBuilding("ground_bldg_b_10016", 273, 10016, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(795.23f, 1439.35f, 216f), ground_bldg_b)))
      LocalObject(1028, Door.Constructor(Vector3(772.578f, 1450.504f, 223.279f)), owning_building_guid = 273)
      LocalObject(1030, Door.Constructor(Vector3(783.0869f, 1428.013f, 217.779f)), owning_building_guid = 273)
      LocalObject(1032, Door.Constructor(Vector3(791.2707f, 1443.991f, 217.779f)), owning_building_guid = 273)
    }

    Building10000()

    def Building10000(): Unit = { // Name: ground_bldg_b_10000 Type: ground_bldg_b GUID: 274, MapID: 10000
      LocalBuilding("ground_bldg_b_10000", 274, 10000, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(817.07f, 1033.77f, 211.66f), ground_bldg_b)))
      LocalObject(1037, Door.Constructor(Vector3(819.085f, 1050.26f, 213.439f)), owning_building_guid = 274)
      LocalObject(1040, Door.Constructor(Vector3(823.086f, 1032.76f, 213.439f)), owning_building_guid = 274)
      LocalObject(1046, Door.Constructor(Vector3(841.592f, 1039.786f, 218.939f)), owning_building_guid = 274)
    }

    Building10021()

    def Building10021(): Unit = { // Name: ground_bldg_b_10021 Type: ground_bldg_b GUID: 275, MapID: 10021
      LocalBuilding("ground_bldg_b_10021", 275, 10021, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1193.21f, 1419.07f, 224.66f), ground_bldg_b)))
      LocalObject(1132, Door.Constructor(Vector3(1168.493f, 1413.913f, 231.939f)), owning_building_guid = 275)
      LocalObject(1138, Door.Constructor(Vector3(1187.233f, 1420.289f, 226.439f)), owning_building_guid = 275)
      LocalObject(1140, Door.Constructor(Vector3(1190.621f, 1402.66f, 226.439f)), owning_building_guid = 275)
    }

    Building10030()

    def Building10030(): Unit = { // Name: ground_bldg_b_10030 Type: ground_bldg_b GUID: 276, MapID: 10030
      LocalBuilding("ground_bldg_b_10030", 276, 10030, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1364.87f, 1038.08f, 216f), ground_bldg_b)))
      LocalObject(1200, Door.Constructor(Vector3(1369.506f, 1034.116f, 217.779f)), owning_building_guid = 276)
      LocalObject(1201, Door.Constructor(Vector3(1375.09f, 1051.177f, 217.779f)), owning_building_guid = 276)
      LocalObject(1203, Door.Constructor(Vector3(1388.988f, 1030.607f, 223.279f)), owning_building_guid = 276)
    }

    Building10369()

    def Building10369(): Unit = { // Name: ground_bldg_c_10369 Type: ground_bldg_c GUID: 277, MapID: 10369
      LocalBuilding("ground_bldg_c_10369", 277, 10369, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(915.84f, 988.16f, 228.46f), ground_bldg_c)))
      LocalObject(1058, Door.Constructor(Vector3(875.1998f, 963.769f, 230.239f)), owning_building_guid = 277)
      LocalObject(1068, Door.Constructor(Vector3(897.1201f, 941.8487f, 230.239f)), owning_building_guid = 277)
      LocalObject(1084, Door.Constructor(Vector3(919.7249f, 986.4106f, 230.239f)), owning_building_guid = 277)
    }

    Building10023()

    def Building10023(): Unit = { // Name: ground_bldg_c_10023 Type: ground_bldg_c GUID: 278, MapID: 10023
      LocalBuilding("ground_bldg_c_10023", 278, 10023, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1266.3f, 1371.91f, 241.56f), ground_bldg_c)))
      LocalObject(1153, Door.Constructor(Vector3(1239.094f, 1333.098f, 243.339f)), owning_building_guid = 278)
      LocalObject(1164, Door.Constructor(Vector3(1268.035f, 1321.989f, 243.339f)), owning_building_guid = 278)
      LocalObject(1165, Door.Constructor(Vector3(1270.561f, 1371.892f, 243.339f)), owning_building_guid = 278)
    }

    Building10009()

    def Building10009(): Unit = { // Name: ground_bldg_d_10009 Type: ground_bldg_d GUID: 279, MapID: 10009
      LocalBuilding("ground_bldg_d_10009", 279, 10009, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(807.17f, 1205.79f, 216.95f), ground_bldg_d)))
      LocalObject(1031, Door.Constructor(Vector3(789.7576f, 1203.944f, 218.685f)), owning_building_guid = 279)
      LocalObject(1034, Door.Constructor(Vector3(805.3238f, 1223.202f, 218.685f)), owning_building_guid = 279)
      LocalObject(1035, Door.Constructor(Vector3(808.9823f, 1188.394f, 218.685f)), owning_building_guid = 279)
      LocalObject(1041, Door.Constructor(Vector3(824.5659f, 1207.602f, 218.685f)), owning_building_guid = 279)
    }

    Building10001()

    def Building10001(): Unit = { // Name: ground_bldg_d_10001 Type: ground_bldg_d GUID: 280, MapID: 10001
      LocalBuilding("ground_bldg_d_10001", 280, 10001, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(837f, 990.15f, 218.45f), ground_bldg_d)))
      LocalObject(1039, Door.Constructor(Vector3(819.51f, 990.166f, 220.185f)), owning_building_guid = 280)
      LocalObject(1043, Door.Constructor(Vector3(837.016f, 972.64f, 220.185f)), owning_building_guid = 280)
      LocalObject(1044, Door.Constructor(Vector3(837.016f, 1007.64f, 220.185f)), owning_building_guid = 280)
      LocalObject(1048, Door.Constructor(Vector3(854.51f, 990.166f, 220.185f)), owning_building_guid = 280)
    }

    Building10543()

    def Building10543(): Unit = { // Name: ground_bldg_d_10543 Type: ground_bldg_d GUID: 281, MapID: 10543
      LocalBuilding("ground_bldg_d_10543", 281, 10543, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(978.03f, 1293.93f, 229f), ground_bldg_d)))
      LocalObject(1100, Door.Constructor(Vector3(963.8547f, 1304.209f, 230.735f)), owning_building_guid = 281)
      LocalObject(1101, Door.Constructor(Vector3(967.7367f, 1279.79f, 230.735f)), owning_building_guid = 281)
      LocalObject(1108, Door.Constructor(Vector3(988.3092f, 1308.105f, 230.735f)), owning_building_guid = 281)
      LocalObject(1111, Door.Constructor(Vector3(992.1703f, 1283.637f, 230.735f)), owning_building_guid = 281)
    }

    Building10022()

    def Building10022(): Unit = { // Name: ground_bldg_d_10022 Type: ground_bldg_d GUID: 282, MapID: 10022
      LocalBuilding("ground_bldg_d_10022", 282, 10022, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1201.5f, 1360.56f, 231.45f), ground_bldg_d)))
      LocalObject(1135, Door.Constructor(Vector3(1184.01f, 1360.576f, 233.185f)), owning_building_guid = 282)
      LocalObject(1146, Door.Constructor(Vector3(1201.516f, 1343.05f, 233.185f)), owning_building_guid = 282)
      LocalObject(1147, Door.Constructor(Vector3(1201.516f, 1378.05f, 233.185f)), owning_building_guid = 282)
      LocalObject(1151, Door.Constructor(Vector3(1219.01f, 1360.576f, 233.185f)), owning_building_guid = 282)
    }

    Building10031()

    def Building10031(): Unit = { // Name: ground_bldg_d_10031 Type: ground_bldg_d GUID: 283, MapID: 10031
      LocalBuilding("ground_bldg_d_10031", 283, 10031, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1301.6f, 959.02f, 222f), ground_bldg_d)))
      LocalObject(1180, Door.Constructor(Vector3(1284.428f, 955.6985f, 223.735f)), owning_building_guid = 283)
      LocalObject(1184, Door.Constructor(Vector3(1298.278f, 976.1917f, 223.735f)), owning_building_guid = 283)
      LocalObject(1186, Door.Constructor(Vector3(1304.957f, 941.8348f, 223.735f)), owning_building_guid = 283)
      LocalObject(1191, Door.Constructor(Vector3(1318.785f, 962.3768f, 223.735f)), owning_building_guid = 283)
    }

    Building10013()

    def Building10013(): Unit = { // Name: ground_bldg_i_10013 Type: ground_bldg_i GUID: 284, MapID: 10013
      LocalBuilding("ground_bldg_i_10013", 284, 10013, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(715.89f, 1024.55f, 209f), ground_bldg_i)))
      LocalObject(1015, Door.Constructor(Vector3(695.9173f, 1039.152f, 210.779f)), owning_building_guid = 284)
      LocalObject(1023, Door.Constructor(Vector3(740.0647f, 1015.678f, 210.779f)), owning_building_guid = 284)
    }

    Building10018()

    def Building10018(): Unit = { // Name: ground_bldg_i_10018 Type: ground_bldg_i GUID: 285, MapID: 10018
      LocalBuilding("ground_bldg_i_10018", 285, 10018, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(795.93f, 1513.27f, 225f), ground_bldg_i)))
      LocalObject(1027, Door.Constructor(Vector3(771.4196f, 1509.899f, 226.779f)), owning_building_guid = 285)
      LocalObject(1038, Door.Constructor(Vector3(819.4827f, 1523.681f, 226.779f)), owning_building_guid = 285)
    }

    Building10032()

    def Building10032(): Unit = { // Name: ground_bldg_i_10032 Type: ground_bldg_i GUID: 286, MapID: 10032
      LocalBuilding("ground_bldg_i_10032", 286, 10032, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1375.1f, 964.87f, 225f), ground_bldg_i)))
      LocalObject(1198, Door.Constructor(Vector3(1353.466f, 950.9024f, 226.779f)), owning_building_guid = 286)
      LocalObject(1205, Door.Constructor(Vector3(1398.781f, 972.0333f, 226.779f)), owning_building_guid = 286)
    }

    Building10748()

    def Building10748(): Unit = { // Name: N_Redoubt Type: redoubt GUID: 287, MapID: 10748
      LocalBuilding("N_Redoubt", 287, 10748, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(870.46f, 1493.53f, 214.4f), redoubt)))
      LocalObject(1393, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 287)
      LocalObject(1050, Door.Constructor(Vector3(856.3197f, 1503.823f, 216.135f)), owning_building_guid = 287)
      LocalObject(1055, Door.Constructor(Vector3(867.6268f, 1510.519f, 226.179f)), owning_building_guid = 287)
      LocalObject(1057, Door.Constructor(Vector3(874.3053f, 1505.662f, 226.159f)), owning_building_guid = 287)
      LocalObject(1061, Door.Constructor(Vector3(880.8124f, 1500.974f, 226.159f)), owning_building_guid = 287)
      LocalObject(1062, Door.Constructor(Vector3(884.6353f, 1483.251f, 216.135f)), owning_building_guid = 287)
      LocalObject(1066, Door.Constructor(Vector3(887.4849f, 1496.132f, 226.179f)), owning_building_guid = 287)
      LocalObject(1410, Terminal.Constructor(Vector3(859.4799f, 1478.63f, 214.3558f), vanu_equipment_term), owning_building_guid = 287)
      LocalObject(1416, Terminal.Constructor(Vector3(881.4727f, 1508.661f, 214.3535f), vanu_equipment_term), owning_building_guid = 287)
      LocalObject(1340, SpawnTube.Constructor(Vector3(870.46f, 1493.53f, 214.4f), Vector3(0, 0, 216)), owning_building_guid = 287)
      LocalObject(1328, Painbox.Constructor(Vector3(870.6642f, 1493.741f, 222.189f), painbox_continuous), owning_building_guid = 287)
    }

    Building10751()

    def Building10751(): Unit = { // Name: S_Redoubt Type: redoubt GUID: 288, MapID: 10751
      LocalBuilding("S_Redoubt", 288, 10751, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(1291.34f, 1131.64f, 216.2f), redoubt)))
      LocalObject(1397, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 288)
      LocalObject(1169, Door.Constructor(Vector3(1277.826f, 1142.317f, 227.979f)), owning_building_guid = 288)
      LocalObject(1172, Door.Constructor(Vector3(1278.836f, 1134.135f, 227.959f)), owning_building_guid = 288)
      LocalObject(1176, Door.Constructor(Vector3(1279.845f, 1126.178f, 227.959f)), owning_building_guid = 288)
      LocalObject(1178, Door.Constructor(Vector3(1280.847f, 1117.981f, 227.979f)), owning_building_guid = 288)
      LocalObject(1182, Door.Constructor(Vector3(1289.19f, 1149.018f, 217.935f)), owning_building_guid = 288)
      LocalObject(1183, Door.Constructor(Vector3(1293.456f, 1114.278f, 217.935f)), owning_building_guid = 288)
      LocalObject(1450, Terminal.Constructor(Vector3(1272.764f, 1129.375f, 216.1535f), vanu_equipment_term), owning_building_guid = 288)
      LocalObject(1459, Terminal.Constructor(Vector3(1309.726f, 1133.771f, 216.1558f), vanu_equipment_term), owning_building_guid = 288)
      LocalObject(1341, SpawnTube.Constructor(Vector3(1291.34f, 1131.64f, 216.2f), Vector3(0, 0, 83)), owning_building_guid = 288)
      LocalObject(1331, Painbox.Constructor(Vector3(1291.047f, 1131.646f, 223.989f), painbox_continuous), owning_building_guid = 288)
    }

    Building10354()

    def Building10354(): Unit = { // Name: S_Stasis Type: vanu_control_point GUID: 423, MapID: 10354
      LocalBuilding("S_Stasis", 423, 10354, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(910.38f, 1061.46f, 211.09f), vanu_control_point)))
      LocalObject(1394, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 423)
      LocalObject(1052, Door.Constructor(Vector3(862.2756f, 1062.809f, 212.869f)), owning_building_guid = 423)
      LocalObject(1069, Door.Constructor(Vector3(897.8686f, 1113.024f, 212.869f)), owning_building_guid = 423)
      LocalObject(1075, Door.Constructor(Vector3(901.7061f, 1056.084f, 217.81f)), owning_building_guid = 423)
      LocalObject(1076, Door.Constructor(Vector3(902.3132f, 1073.067f, 217.81f)), owning_building_guid = 423)
      LocalObject(1078, Door.Constructor(Vector3(907.1081f, 1049.923f, 242.869f)), owning_building_guid = 423)
      LocalObject(1079, Door.Constructor(Vector3(913.0156f, 1055.685f, 242.849f)), owning_building_guid = 423)
      LocalObject(1081, Door.Constructor(Vector3(918.6442f, 1055.507f, 217.81f)), owning_building_guid = 423)
      LocalObject(1082, Door.Constructor(Vector3(918.8578f, 1061.178f, 242.849f)), owning_building_guid = 423)
      LocalObject(1083, Door.Constructor(Vector3(919.2585f, 1072.452f, 217.81f)), owning_building_guid = 423)
      LocalObject(1087, Door.Constructor(Vector3(923.4755f, 1026.865f, 212.869f)), owning_building_guid = 423)
      LocalObject(1090, Door.Constructor(Vector3(925.0205f, 1066.671f, 242.869f)), owning_building_guid = 423)
      LocalObject(1096, Door.Constructor(Vector3(947.6342f, 1065.881f, 212.869f)), owning_building_guid = 423)
      LocalObject(1420, Terminal.Constructor(Vector3(900.1823f, 1062.696f, 216.103f), vanu_equipment_term), owning_building_guid = 423)
      LocalObject(1421, Terminal.Constructor(Vector3(900.3245f, 1066.667f, 216.107f), vanu_equipment_term), owning_building_guid = 423)
      LocalObject(1425, Terminal.Constructor(Vector3(908.254f, 1054.15f, 216.107f), vanu_equipment_term), owning_building_guid = 423)
      LocalObject(1426, Terminal.Constructor(Vector3(908.7996f, 1074.577f, 216.103f), vanu_equipment_term), owning_building_guid = 423)
      LocalObject(1427, Terminal.Constructor(Vector3(912.2247f, 1054.008f, 216.103f), vanu_equipment_term), owning_building_guid = 423)
      LocalObject(1428, Terminal.Constructor(Vector3(912.707f, 1074.466f, 216.107f), vanu_equipment_term), owning_building_guid = 423)
      LocalObject(1429, Terminal.Constructor(Vector3(920.6359f, 1061.95f, 216.107f), vanu_equipment_term), owning_building_guid = 423)
      LocalObject(1430, Terminal.Constructor(Vector3(920.7781f, 1065.921f, 216.103f), vanu_equipment_term), owning_building_guid = 423)
      LocalObject(1481, SpawnTube.Constructor(Vector3(910.4787f, 1064.287f, 216.229f), Vector3(0, 0, 47)), owning_building_guid = 423)
      LocalObject(1329, Painbox.Constructor(Vector3(910.895f, 1064.282f, 225.4318f), painbox_continuous), owning_building_guid = 423)
      LocalObject(1332, Painbox.Constructor(Vector3(900.1142f, 1074.834f, 219.98f), painbox_door_radius_continuous), owning_building_guid = 423)
      LocalObject(1333, Painbox.Constructor(Vector3(900.2362f, 1054.792f, 219.98f), painbox_door_radius_continuous), owning_building_guid = 423)
      LocalObject(1334, Painbox.Constructor(Vector3(920.1619f, 1053.746f, 219.38f), painbox_door_radius_continuous), owning_building_guid = 423)
      LocalObject(1335, Painbox.Constructor(Vector3(920.6906f, 1073.384f, 219.98f), painbox_door_radius_continuous), owning_building_guid = 423)
    }

    Building10020()

    def Building10020(): Unit = { // Name: N_Stasis Type: vanu_control_point GUID: 424, MapID: 10020
      LocalBuilding("N_Stasis", 424, 10020, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1271.2f, 1433.74f, 224.19f), vanu_control_point)))
      LocalObject(1398, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 424)
      LocalObject(1152, Door.Constructor(Vector3(1223.096f, 1435.089f, 225.969f)), owning_building_guid = 424)
      LocalObject(1155, Door.Constructor(Vector3(1258.688f, 1485.304f, 225.969f)), owning_building_guid = 424)
      LocalObject(1156, Door.Constructor(Vector3(1262.526f, 1428.364f, 230.91f)), owning_building_guid = 424)
      LocalObject(1157, Door.Constructor(Vector3(1263.133f, 1445.347f, 230.91f)), owning_building_guid = 424)
      LocalObject(1163, Door.Constructor(Vector3(1267.928f, 1422.203f, 255.969f)), owning_building_guid = 424)
      LocalObject(1167, Door.Constructor(Vector3(1273.836f, 1427.965f, 255.949f)), owning_building_guid = 424)
      LocalObject(1174, Door.Constructor(Vector3(1279.464f, 1427.787f, 230.91f)), owning_building_guid = 424)
      LocalObject(1175, Door.Constructor(Vector3(1279.678f, 1433.458f, 255.949f)), owning_building_guid = 424)
      LocalObject(1177, Door.Constructor(Vector3(1280.078f, 1444.732f, 230.91f)), owning_building_guid = 424)
      LocalObject(1179, Door.Constructor(Vector3(1284.295f, 1399.145f, 225.969f)), owning_building_guid = 424)
      LocalObject(1181, Door.Constructor(Vector3(1285.84f, 1438.951f, 255.969f)), owning_building_guid = 424)
      LocalObject(1189, Door.Constructor(Vector3(1308.454f, 1438.161f, 225.969f)), owning_building_guid = 424)
      LocalObject(1445, Terminal.Constructor(Vector3(1261.002f, 1434.976f, 229.203f), vanu_equipment_term), owning_building_guid = 424)
      LocalObject(1446, Terminal.Constructor(Vector3(1261.144f, 1438.947f, 229.207f), vanu_equipment_term), owning_building_guid = 424)
      LocalObject(1448, Terminal.Constructor(Vector3(1269.074f, 1426.43f, 229.207f), vanu_equipment_term), owning_building_guid = 424)
      LocalObject(1449, Terminal.Constructor(Vector3(1269.62f, 1446.857f, 229.203f), vanu_equipment_term), owning_building_guid = 424)
      LocalObject(1451, Terminal.Constructor(Vector3(1273.045f, 1426.288f, 229.203f), vanu_equipment_term), owning_building_guid = 424)
      LocalObject(1452, Terminal.Constructor(Vector3(1273.527f, 1446.746f, 229.207f), vanu_equipment_term), owning_building_guid = 424)
      LocalObject(1453, Terminal.Constructor(Vector3(1281.456f, 1434.23f, 229.207f), vanu_equipment_term), owning_building_guid = 424)
      LocalObject(1454, Terminal.Constructor(Vector3(1281.598f, 1438.201f, 229.203f), vanu_equipment_term), owning_building_guid = 424)
      LocalObject(1482, SpawnTube.Constructor(Vector3(1271.299f, 1436.567f, 229.329f), Vector3(0, 0, 47)), owning_building_guid = 424)
      LocalObject(1330, Painbox.Constructor(Vector3(1271.715f, 1436.562f, 238.5318f), painbox_continuous), owning_building_guid = 424)
      LocalObject(1336, Painbox.Constructor(Vector3(1260.934f, 1447.114f, 233.08f), painbox_door_radius_continuous), owning_building_guid = 424)
      LocalObject(1337, Painbox.Constructor(Vector3(1261.056f, 1427.072f, 233.08f), painbox_door_radius_continuous), owning_building_guid = 424)
      LocalObject(1338, Painbox.Constructor(Vector3(1280.982f, 1426.026f, 232.48f), painbox_door_radius_continuous), owning_building_guid = 424)
      LocalObject(1339, Painbox.Constructor(Vector3(1281.51f, 1445.664f, 233.08f), painbox_door_radius_continuous), owning_building_guid = 424)
    }

    Building10002()

    def Building10002(): Unit = { // Name: Core Type: vanu_core GUID: 425, MapID: 10002
      LocalBuilding("Core", 425, 10002, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1110.13f, 1216.96f, 266.93f), vanu_core)))
      LocalObject(1116, Door.Constructor(Vector3(1077.637f, 1204.982f, 273.718f)), owning_building_guid = 425)
      LocalObject(1118, Door.Constructor(Vector3(1106.108f, 1176.467f, 273.718f)), owning_building_guid = 425)
      LocalObject(1119, Door.Constructor(Vector3(1106.108f, 1176.467f, 278.718f)), owning_building_guid = 425)
      LocalObject(1120, Door.Constructor(Vector3(1106.152f, 1233.453f, 278.718f)), owning_building_guid = 425)
      LocalObject(1121, Door.Constructor(Vector3(1106.152f, 1233.453f, 283.718f)), owning_building_guid = 425)
      LocalObject(1127, Door.Constructor(Vector3(1134.623f, 1204.938f, 283.718f)), owning_building_guid = 425)
    }

    Building10015()

    def Building10015(): Unit = { // Name: N_ATPlant Type: vanu_vehicle_station GUID: 472, MapID: 10015
      LocalBuilding("N_ATPlant", 472, 10015, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(877.25f, 1407.81f, 214f), vanu_vehicle_station)))
      LocalObject(1395, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 472)
      LocalObject(1060, Door.Constructor(Vector3(880.3055f, 1392.511f, 215.779f)), owning_building_guid = 472)
      LocalObject(1064, Door.Constructor(Vector3(886.2134f, 1448.72f, 235.703f)), owning_building_guid = 472)
      LocalObject(1065, Door.Constructor(Vector3(887.0483f, 1456.664f, 215.779f)), owning_building_guid = 472)
      LocalObject(1077, Door.Constructor(Vector3(905.0204f, 1398.055f, 235.691f)), owning_building_guid = 472)
      LocalObject(1086, Door.Constructor(Vector3(922.5992f, 1412.237f, 245.779f)), owning_building_guid = 472)
      LocalObject(1088, Door.Constructor(Vector3(923.4856f, 1420.403f, 245.759f)), owning_building_guid = 472)
      LocalObject(1089, Door.Constructor(Vector3(924.2911f, 1428.383f, 245.759f)), owning_building_guid = 472)
      LocalObject(1091, Door.Constructor(Vector3(925.158f, 1436.583f, 245.779f)), owning_building_guid = 472)
      LocalObject(1212, Door.Constructor(Vector3(943.3931f, 1418.35f, 220.633f)), owning_building_guid = 472)
      LocalObject(1388, Terminal.Constructor(Vector3(896.063f, 1405.571f, 233.917f), vanu_air_vehicle_term), owning_building_guid = 472)
      LocalObject(1483, VehicleSpawnPad.Constructor(Vector3(892.4431f, 1413.683f, 233.916f), vanu_vehicle_creation_pad, Vector3(0, 0, -84)), owning_building_guid = 472, terminal_guid = 1388)
      LocalObject(1389, Terminal.Constructor(Vector3(899.7547f, 1440.781f, 233.917f), vanu_air_vehicle_term), owning_building_guid = 472)
      LocalObject(1484, VehicleSpawnPad.Constructor(Vector3(894.5354f, 1433.589f, 233.916f), vanu_vehicle_creation_pad, Vector3(0, 0, -84)), owning_building_guid = 472, terminal_guid = 1389)
      LocalObject(1423, Terminal.Constructor(Vector3(903.5538f, 1410.448f, 216.5f), vanu_equipment_term), owning_building_guid = 472)
      LocalObject(1424, Terminal.Constructor(Vector3(906.0625f, 1434.317f, 216.5f), vanu_equipment_term), owning_building_guid = 472)
      LocalObject(1489, Terminal.Constructor(Vector3(910.2566f, 1421.947f, 219f), vanu_vehicle_term), owning_building_guid = 472)
      LocalObject(1485, VehicleSpawnPad.Constructor(Vector3(925.2994f, 1420.258f, 216.405f), vanu_vehicle_creation_pad, Vector3(0, 0, 96)), owning_building_guid = 472, terminal_guid = 1489)
    }

    Building10109()

    def Building10109(): Unit = { // Name: S_ATPlant Type: vanu_vehicle_station GUID: 473, MapID: 10109
      LocalBuilding("S_ATPlant", 473, 10109, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1307.08f, 1065.9f, 214f), vanu_vehicle_station)))
      LocalObject(1396, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 473)
      LocalObject(1158, Door.Constructor(Vector3(1263.409f, 1052.901f, 245.779f)), owning_building_guid = 473)
      LocalObject(1160, Door.Constructor(Vector3(1264.097f, 1044.716f, 245.759f)), owning_building_guid = 473)
      LocalObject(1161, Door.Constructor(Vector3(1264.828f, 1036.73f, 245.759f)), owning_building_guid = 473)
      LocalObject(1162, Door.Constructor(Vector3(1265.542f, 1028.515f, 245.779f)), owning_building_guid = 473)
      LocalObject(1170, Door.Constructor(Vector3(1277.958f, 1070.177f, 235.691f)), owning_building_guid = 473)
      LocalObject(1185, Door.Constructor(Vector3(1301.161f, 1080.335f, 215.779f)), owning_building_guid = 473)
      LocalObject(1187, Door.Constructor(Vector3(1306.087f, 1024.031f, 235.703f)), owning_building_guid = 473)
      LocalObject(1188, Door.Constructor(Vector3(1306.783f, 1016.074f, 215.779f)), owning_building_guid = 473)
      LocalObject(1213, Door.Constructor(Vector3(1244.163f, 1042.933f, 220.633f)), owning_building_guid = 473)
      LocalObject(1390, Terminal.Constructor(Vector3(1288.185f, 1064.508f, 233.917f), vanu_air_vehicle_term), owning_building_guid = 473)
      LocalObject(1487, VehicleSpawnPad.Constructor(Vector3(1293.287f, 1057.236f, 233.916f), vanu_vehicle_creation_pad, Vector3(0, 0, 85)), owning_building_guid = 473, terminal_guid = 1390)
      LocalObject(1391, Terminal.Constructor(Vector3(1291.28f, 1029.24f, 233.917f), vanu_air_vehicle_term), owning_building_guid = 473)
      LocalObject(1488, VehicleSpawnPad.Constructor(Vector3(1295.031f, 1037.296f, 233.916f), vanu_vehicle_creation_pad, Vector3(0, 0, 85)), owning_building_guid = 473, terminal_guid = 1391)
      LocalObject(1455, Terminal.Constructor(Vector3(1281.763f, 1058.292f, 216.5f), vanu_equipment_term), owning_building_guid = 473)
      LocalObject(1457, Terminal.Constructor(Vector3(1283.854f, 1034.383f, 216.5f), vanu_equipment_term), owning_building_guid = 473)
      LocalObject(1490, Terminal.Constructor(Vector3(1277.377f, 1045.724f, 219f), vanu_vehicle_term), owning_building_guid = 473)
      LocalObject(1486, VehicleSpawnPad.Constructor(Vector3(1262.289f, 1044.513f, 216.405f), vanu_vehicle_creation_pad, Vector3(0, 0, 265)), owning_building_guid = 473, terminal_guid = 1490)
    }

    Building10112()

    def Building10112(): Unit = { // Name: GW_Cavern2_W Type: warpgate_cavern GUID: 474, MapID: 10112
      LocalBuilding("GW_Cavern2_W", 474, 10112, FoundationBuilder(WarpGate.Structure(Vector3(227.73f, 1255.27f, 161.35f))))
    }

    Building10111()

    def Building10111(): Unit = { // Name: GW_Cavern2_N Type: warpgate_cavern GUID: 475, MapID: 10111
      LocalBuilding("GW_Cavern2_N", 475, 10111, FoundationBuilder(WarpGate.Structure(Vector3(1031.13f, 2429.68f, 231.26f))))
    }

    Building10583()

    def Building10583(): Unit = { // Name: GW_Cavern2_S Type: warpgate_cavern GUID: 476, MapID: 10583
      LocalBuilding("GW_Cavern2_S", 476, 10583, FoundationBuilder(WarpGate.Structure(Vector3(1087.96f, 124.96f, 130.92f))))
    }

    Building10563()

    def Building10563(): Unit = { // Name: GW_Cavern2_E Type: warpgate_cavern GUID: 477, MapID: 10563
      LocalBuilding("GW_Cavern2_E", 477, 10563, FoundationBuilder(WarpGate.Structure(Vector3(1947.33f, 1306.67f, 181.42f))))
    }

    ZoneOwnedObjects()

    def ZoneOwnedObjects(): Unit = {
      LocalObject(1399, Terminal.Constructor(Vector3(698.33f, 1305.7f, 275.21f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1400, Terminal.Constructor(Vector3(698.58f, 1315.74f, 275.19f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1401, Terminal.Constructor(Vector3(708.04f, 1316.01f, 269.6f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1402, Terminal.Constructor(Vector3(712.63f, 1027.1f, 208.95f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1403, Terminal.Constructor(Vector3(780.39f, 1449.89f, 221.48f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1404, Terminal.Constructor(Vector3(781.96f, 1183.78f, 295.04f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1405, Terminal.Constructor(Vector3(793.93f, 893.29f, 258.11f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1406, Terminal.Constructor(Vector3(796.12f, 1214.87f, 216.88f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1407, Terminal.Constructor(Vector3(796.38f, 888.05f, 258.11f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1408, Terminal.Constructor(Vector3(811.1f, 1040.67f, 217.19f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1409, Terminal.Constructor(Vector3(818.56f, 1196.84f, 216.87f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1411, Terminal.Constructor(Vector3(868.14f, 1507.66f, 214.34f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1412, Terminal.Constructor(Vector3(873.05f, 1479.4f, 214.34f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1413, Terminal.Constructor(Vector3(874.34f, 529.76f, 223.56f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1414, Terminal.Constructor(Vector3(874.43f, 541.76f, 223.6f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1415, Terminal.Constructor(Vector3(878.58f, 714.85f, 245.64f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1417, Terminal.Constructor(Vector3(885.9f, 718.47f, 245.59f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1418, Terminal.Constructor(Vector3(886.9f, 719.18f, 240.1f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1419, Terminal.Constructor(Vector3(892.74f, 1062.22f, 306.95f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1422, Terminal.Constructor(Vector3(901.22f, 968.3f, 228.56f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1431, Terminal.Constructor(Vector3(960.27f, 1312.18f, 314.7f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1432, Terminal.Constructor(Vector3(960.85f, 1316.61f, 320.2f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1433, Terminal.Constructor(Vector3(992.3f, 1296.21f, 228.93f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1434, Terminal.Constructor(Vector3(1052.35f, 1589.94f, 277.2f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1435, Terminal.Constructor(Vector3(1052.45f, 1579.18f, 277.16f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1436, Terminal.Constructor(Vector3(1061.86f, 1589.88f, 271.7f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1437, Terminal.Constructor(Vector3(1148.93f, 1280.05f, 309.5f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1438, Terminal.Constructor(Vector3(1177.01f, 1655.31f, 264.53f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1439, Terminal.Constructor(Vector3(1179.59f, 1657.89f, 264.54f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1440, Terminal.Constructor(Vector3(1191.35f, 1350.39f, 231.43f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1441, Terminal.Constructor(Vector3(1211.49f, 1310.12f, 297.49f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1442, Terminal.Constructor(Vector3(1211.81f, 1370.72f, 231.41f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1443, Terminal.Constructor(Vector3(1246.72f, 1328.41f, 241.55f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1444, Terminal.Constructor(Vector3(1259.03f, 1323.69f, 241.59f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1447, Terminal.Constructor(Vector3(1261.61f, 1348.42f, 241.57f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1456, Terminal.Constructor(Vector3(1282.61f, 1120.26f, 216.13f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1458, Terminal.Constructor(Vector3(1300.12f, 1143.05f, 216.15f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1460, Terminal.Constructor(Vector3(1332.57f, 907.17f, 304.92f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1461, Terminal.Constructor(Vector3(1334.08f, 903.35f, 299.39f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1462, Terminal.Constructor(Vector3(1360.28f, 1340.27f, 301.92f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1463, Terminal.Constructor(Vector3(1360.38f, 1313.07f, 301.94f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1464, Terminal.Constructor(Vector3(1366.18f, 1468.81f, 264.11f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1465, Terminal.Constructor(Vector3(1366.43f, 1457.77f, 264.13f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1466, Terminal.Constructor(Vector3(1373.3f, 957.15f, 224.99f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1467, Terminal.Constructor(Vector3(1378.62f, 966.06f, 225.05f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1468, Terminal.Constructor(Vector3(1380.3f, 1313.42f, 301.87f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(111, ProximityTerminal.Constructor(Vector3(630.2f, 1322.82f, 188.62f), crystals_health_a), owning_building_guid = 0)
      LocalObject(112, ProximityTerminal.Constructor(Vector3(630.45f, 1323.4f, 188.57f), crystals_health_a), owning_building_guid = 0)
      LocalObject(113, ProximityTerminal.Constructor(Vector3(679.73f, 1007f, 196.6f), crystals_health_a), owning_building_guid = 0)
      LocalObject(114, ProximityTerminal.Constructor(Vector3(763.73f, 705.8f, 185.44f), crystals_health_a), owning_building_guid = 0)
      LocalObject(115, ProximityTerminal.Constructor(Vector3(764.63f, 705.6f, 185.34f), crystals_health_a), owning_building_guid = 0)
      LocalObject(116, ProximityTerminal.Constructor(Vector3(812.51f, 989.24f, 208.3f), crystals_health_a), owning_building_guid = 0)
      LocalObject(117, ProximityTerminal.Constructor(Vector3(858.69f, 1106.27f, 204.05f), crystals_health_a), owning_building_guid = 0)
      LocalObject(118, ProximityTerminal.Constructor(Vector3(1063.25f, 1071.69f, 214f), crystals_health_a), owning_building_guid = 0)
      LocalObject(119, ProximityTerminal.Constructor(Vector3(1063.83f, 1071.1f, 213.9f), crystals_health_a), owning_building_guid = 0)
      LocalObject(120, ProximityTerminal.Constructor(Vector3(1086.51f, 1897.72f, 193.91f), crystals_health_a), owning_building_guid = 0)
      LocalObject(121, ProximityTerminal.Constructor(Vector3(1135.67f, 1761.38f, 194.24f), crystals_health_a), owning_building_guid = 0)
      LocalObject(122, ProximityTerminal.Constructor(Vector3(1258.38f, 1481.15f, 237.85f), crystals_health_a), owning_building_guid = 0)
      LocalObject(123, ProximityTerminal.Constructor(Vector3(1273.92f, 1488.35f, 221.05f), crystals_health_a), owning_building_guid = 0)
      LocalObject(124, ProximityTerminal.Constructor(Vector3(1276.89f, 1485.76f, 221.15f), crystals_health_a), owning_building_guid = 0)
      LocalObject(125, ProximityTerminal.Constructor(Vector3(1406.77f, 1537.53f, 192.34f), crystals_health_a), owning_building_guid = 0)
      LocalObject(126, ProximityTerminal.Constructor(Vector3(691.95f, 1096.04f, 186.74f), crystals_health_b), owning_building_guid = 0)
      LocalObject(127, ProximityTerminal.Constructor(Vector3(764.38f, 706.85f, 185.44f), crystals_health_b), owning_building_guid = 0)
      LocalObject(128, ProximityTerminal.Constructor(Vector3(859.73f, 1105.49f, 204.15f), crystals_health_b), owning_building_guid = 0)
      LocalObject(129, ProximityTerminal.Constructor(Vector3(869.47f, 1368.33f, 212.2f), crystals_health_b), owning_building_guid = 0)
      LocalObject(130, ProximityTerminal.Constructor(Vector3(911.04f, 1018.53f, 208.5f), crystals_health_b), owning_building_guid = 0)
      LocalObject(131, ProximityTerminal.Constructor(Vector3(946.53f, 1404.77f, 211.83f), crystals_health_b), owning_building_guid = 0)
      LocalObject(132, ProximityTerminal.Constructor(Vector3(961.61f, 1447.42f, 200.9f), crystals_health_b), owning_building_guid = 0)
      LocalObject(133, ProximityTerminal.Constructor(Vector3(983.11f, 1060.58f, 218.9f), crystals_health_b), owning_building_guid = 0)
      LocalObject(134, ProximityTerminal.Constructor(Vector3(994.01f, 999.99f, 204f), crystals_health_b), owning_building_guid = 0)
      LocalObject(135, ProximityTerminal.Constructor(Vector3(1073.71f, 1928.76f, 193.51f), crystals_health_b), owning_building_guid = 0)
      LocalObject(136, ProximityTerminal.Constructor(Vector3(1087.43f, 1898.43f, 194f), crystals_health_b), owning_building_guid = 0)
      LocalObject(137, ProximityTerminal.Constructor(Vector3(1136.71f, 1761.54f, 194.1f), crystals_health_b), owning_building_guid = 0)
      LocalObject(138, ProximityTerminal.Constructor(Vector3(1156.21f, 1196.65f, 219.3f), crystals_health_b), owning_building_guid = 0)
      LocalObject(139, ProximityTerminal.Constructor(Vector3(1234.96f, 1230.91f, 219.5f), crystals_health_b), owning_building_guid = 0)
      LocalObject(140, ProximityTerminal.Constructor(Vector3(1240.89f, 1398.44f, 219.9f), crystals_health_b), owning_building_guid = 0)
      LocalObject(141, ProximityTerminal.Constructor(Vector3(1290.83f, 1356.28f, 219.83f), crystals_health_b), owning_building_guid = 0)
      LocalObject(142, ProximityTerminal.Constructor(Vector3(1431.34f, 1336.17f, 232.1f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1268, ProximityTerminal.Constructor(Vector3(776.85f, 1784.17f, 202.52f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1269, ProximityTerminal.Constructor(Vector3(807.03f, 1498.08f, 224.9f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1270, ProximityTerminal.Constructor(Vector3(807.83f, 1497.78f, 225f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1271, ProximityTerminal.Constructor(Vector3(1082.47f, 500.94f, 167.46f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1272, ProximityTerminal.Constructor(Vector3(1109.76f, 897.5f, 293.08f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1273, ProximityTerminal.Constructor(Vector3(1110.47f, 898.1f, 293.18f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1274, ProximityTerminal.Constructor(Vector3(1110.8f, 897.23f, 292.98f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1275, ProximityTerminal.Constructor(Vector3(1197.16f, 1926.36f, 236.1f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1276, ProximityTerminal.Constructor(Vector3(1197.58f, 1925.73f, 236.27f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1277, ProximityTerminal.Constructor(Vector3(1209.53f, 903.09f, 295.1f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1278, ProximityTerminal.Constructor(Vector3(1209.91f, 903.94f, 295.15f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1279, ProximityTerminal.Constructor(Vector3(1210.24f, 903.12f, 295.1f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1280, ProximityTerminal.Constructor(Vector3(1260.52f, 790.04f, 278.76f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1281, ProximityTerminal.Constructor(Vector3(1359.9f, 1339.75f, 306.85f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1282, ProximityTerminal.Constructor(Vector3(1360.7f, 1339.41f, 306.75f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1283, ProximityTerminal.Constructor(Vector3(1375.04f, 1467.9f, 258.5f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1284, ProximityTerminal.Constructor(Vector3(1375.65f, 1468.45f, 258.65f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1285, ProximityTerminal.Constructor(Vector3(1375.94f, 1467.6f, 258.62f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1286, ProximityTerminal.Constructor(Vector3(1459.76f, 405.36f, 152.45f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1287, ProximityTerminal.Constructor(Vector3(1475.44f, 1004.61f, 265.46f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1288, ProximityTerminal.Constructor(Vector3(1476.42f, 1004.64f, 265.46f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1289, ProximityTerminal.Constructor(Vector3(1476.8f, 1006.25f, 265.51f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1290, ProximityTerminal.Constructor(Vector3(1477.26f, 1005.54f, 265.46f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1291, ProximityTerminal.Constructor(Vector3(1636.75f, 1401.55f, 207.81f), crystals_health_a), owning_building_guid = 0)
      LocalObject(1292, ProximityTerminal.Constructor(Vector3(501.27f, 1611.08f, 212.58f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1293, ProximityTerminal.Constructor(Vector3(923.12f, 1191.17f, 317.89f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1294, ProximityTerminal.Constructor(Vector3(978.21f, 1293.04f, 230.96f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1295, ProximityTerminal.Constructor(Vector3(1031.55f, 2105f, 247.46f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1296, ProximityTerminal.Constructor(Vector3(1259.36f, 789.96f, 278.86f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1297, ProximityTerminal.Constructor(Vector3(1260.33f, 787.69f, 278.86f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1298, ProximityTerminal.Constructor(Vector3(1261.11f, 788.97f, 278.76f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1299, ProximityTerminal.Constructor(Vector3(1302.33f, 959.25f, 224.16f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1300, ProximityTerminal.Constructor(Vector3(1307.68f, 2054.64f, 197.45f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1301, ProximityTerminal.Constructor(Vector3(1476.08f, 1005.56f, 265.51f), crystals_health_b), owning_building_guid = 0)
      LocalObject(1302, ProximityTerminal.Constructor(Vector3(1655.04f, 1594.54f, 232.32f), crystals_health_b), owning_building_guid = 0)
      LocalObject(426, FacilityTurret.Constructor(Vector3(715.91f, 1262.21f, 211f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(426, 5000)
      LocalObject(427, FacilityTurret.Constructor(Vector3(722.12f, 1355.17f, 211.64f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(427, 5001)
      LocalObject(428, FacilityTurret.Constructor(Vector3(732.89f, 1318.6f, 211.64f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(428, 5002)
      LocalObject(429, FacilityTurret.Constructor(Vector3(780.9f, 1163.81f, 208.7f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(429, 5003)
      LocalObject(430, FacilityTurret.Constructor(Vector3(782.07f, 1007.54f, 208.3f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(430, 5004)
      LocalObject(431, FacilityTurret.Constructor(Vector3(793.73f, 1058.82f, 208.3f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(431, 5005)
      LocalObject(432, FacilityTurret.Constructor(Vector3(807.84f, 1248.17f, 211.64f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(432, 5006)
      LocalObject(433, FacilityTurret.Constructor(Vector3(837.27f, 1456.97f, 212.5f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(433, 5007)
      LocalObject(434, FacilityTurret.Constructor(Vector3(862.91f, 1034.45f, 208.5f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(434, 5008)
      LocalObject(435, FacilityTurret.Constructor(Vector3(895.22f, 1542.12f, 212.4f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(435, 5009)
      LocalObject(436, FacilityTurret.Constructor(Vector3(896.33f, 1150.82f, 214f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(436, 5010)
      LocalObject(437, FacilityTurret.Constructor(Vector3(931.05f, 1280.46f, 223.82f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(437, 5011)
      LocalObject(438, FacilityTurret.Constructor(Vector3(934.88f, 815.45f, 269.19f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(438, 5012)
      LocalObject(439, FacilityTurret.Constructor(Vector3(961.35f, 997f, 208.21f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(439, 5013)
      LocalObject(440, FacilityTurret.Constructor(Vector3(972.27f, 1044.02f, 219.12f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(440, 5014)
      LocalObject(441, FacilityTurret.Constructor(Vector3(980.91f, 1117.07f, 231.64f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(441, 5015)
      LocalObject(442, FacilityTurret.Constructor(Vector3(983.58f, 891.05f, 211.1f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(442, 5016)
      LocalObject(443, FacilityTurret.Constructor(Vector3(996.75f, 1324.53f, 224.84f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(443, 5017)
      LocalObject(444, FacilityTurret.Constructor(Vector3(1002.7f, 1385.96f, 211.38f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(444, 5018)
      LocalObject(445, FacilityTurret.Constructor(Vector3(1037.66f, 1067.95f, 232.1f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(445, 5019)
      LocalObject(446, FacilityTurret.Constructor(Vector3(1041.32f, 829f, 195.22f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(446, 5020)
      LocalObject(447, FacilityTurret.Constructor(Vector3(1081.52f, 1415.33f, 224.84f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(447, 5021)
      LocalObject(448, FacilityTurret.Constructor(Vector3(1109.45f, 1088.67f, 219.1f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(448, 5022)
      LocalObject(449, FacilityTurret.Constructor(Vector3(1117.12f, 819.13f, 195.3f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(449, 5023)
      LocalObject(450, FacilityTurret.Constructor(Vector3(1126.53f, 1879.01f, 194.21f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(450, 5024)
      LocalObject(451, FacilityTurret.Constructor(Vector3(1128.36f, 1004.74f, 211.5f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(451, 5025)
      LocalObject(452, FacilityTurret.Constructor(Vector3(1139.19f, 1143.31f, 228.74f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(452, 5026)
      LocalObject(453, FacilityTurret.Constructor(Vector3(1139.22f, 1305.39f, 219.2f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(453, 5027)
      LocalObject(454, FacilityTurret.Constructor(Vector3(1159.69f, 928.13f, 204f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(454, 5028)
      LocalObject(455, FacilityTurret.Constructor(Vector3(1188.22f, 1447.66f, 220f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(455, 5029)
      LocalObject(456, FacilityTurret.Constructor(Vector3(1204.18f, 1130.55f, 219.1f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(456, 5030)
      LocalObject(457, FacilityTurret.Constructor(Vector3(1219.64f, 942.94f, 219.03f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(457, 5031)
      LocalObject(458, FacilityTurret.Constructor(Vector3(1230.47f, 1191.89f, 219.1f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(458, 5032)
      LocalObject(459, FacilityTurret.Constructor(Vector3(1248.71f, 1078.27f, 233.92f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(459, 5033)
      LocalObject(460, FacilityTurret.Constructor(Vector3(1253.84f, 1290.45f, 231.64f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(460, 5034)
      LocalObject(461, FacilityTurret.Constructor(Vector3(1272.67f, 1382.47f, 221.15f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(461, 5035)
      LocalObject(462, FacilityTurret.Constructor(Vector3(1314.52f, 1017.09f, 247.65f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(462, 5036)
      LocalObject(463, FacilityTurret.Constructor(Vector3(1314.52f, 1246.89f, 232.3f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(463, 5037)
      LocalObject(464, FacilityTurret.Constructor(Vector3(1319.98f, 995.12f, 212.4f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(464, 5038)
      LocalObject(465, FacilityTurret.Constructor(Vector3(1328.63f, 1455.16f, 221.15f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(465, 5039)
      LocalObject(466, FacilityTurret.Constructor(Vector3(1332.41f, 1039.83f, 223.35f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(466, 5040)
      LocalObject(467, FacilityTurret.Constructor(Vector3(1351.68f, 1170.29f, 211.24f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(467, 5041)
      LocalObject(468, FacilityTurret.Constructor(Vector3(1378.79f, 1096.12f, 212.5f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(468, 5042)
      LocalObject(469, FacilityTurret.Constructor(Vector3(1408.53f, 1325.02f, 232.1f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(469, 5043)
      LocalObject(470, FacilityTurret.Constructor(Vector3(1447.27f, 1366.32f, 232.1f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(470, 5044)
      LocalObject(471, FacilityTurret.Constructor(Vector3(1507.41f, 1045.66f, 193.2f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(471, 5045)
    }

    def Lattice(): Unit = {
      LatticeLink("N_Redoubt", "N_ATPlant")
      LatticeLink("GW_Cavern2_W", "N_ATPlant")
      LatticeLink("N_Stasis", "Core")
      LatticeLink("S_Stasis", "Core")
      LatticeLink("S_Redoubt", "S_ATPlant")
      LatticeLink("N_Redoubt", "N_Stasis")
      LatticeLink("S_Redoubt", "S_Stasis")
      LatticeLink("S_Stasis", "N_ATPlant")
      LatticeLink("N_Stasis", "S_ATPlant")
      LatticeLink("GW_Cavern2_N", "N_Redoubt")
      LatticeLink("GW_Cavern2_S", "S_Redoubt")
      LatticeLink("GW_Cavern2_E", "S_ATPlant")
    }

    Lattice()

    def ZipLines(): Unit = {
      ZipLinePaths(new ZipLinePath(1, false, List(Vector3(659.834f, 1494.116f, 209.241f), Vector3(707.787f, 1511.106f, 213.516f), Vector3(745.397f, 1524.431f, 213.52f))))
      ZipLinePaths(new ZipLinePath(2, true, List(Vector3(675.978f, 1376.759f, 195.646f), Vector3(662.728f, 1365.682f, 211.993f))))
      ZipLinePaths(new ZipLinePath(3, false, List(Vector3(682.823f, 1290.083f, 270.566f), Vector3(700.533f, 1244.014f, 279.312f), Vector3(718.242f, 1197.945f, 287.32f), Vector3(729.222f, 1169.382f, 290.806f))))
      ZipLinePaths(new ZipLinePath(4, true, List(Vector3(687.66f, 985.624f, 196.95f), Vector3(687.206f, 988.754f, 231.35f))))
      ZipLinePaths(new ZipLinePath(5, false, List(Vector3(689.041f, 994.682f, 231.853f), Vector3(683.022f, 1017.202f, 226.151f), Vector3(677.103f, 1039.422f, 210.753f), Vector3(675.014f, 1047.264f, 199.002f), Vector3(674.625f, 1050.306f, 197.146f))))
      ZipLinePaths(new ZipLinePath(6, false, List(Vector3(703.28f, 1126.36f, 197.032f), Vector3(699.093f, 1176.999f, 202.114f), Vector3(694.906f, 1227.638f, 206.452f), Vector3(690.72f, 1278.276f, 210.79f), Vector3(687.19f, 1320.972f, 212.492f))))
      ZipLinePaths(new ZipLinePath(7, true, List(Vector3(705.314f, 1336.125f, 211.992f), Vector3(699.729f, 1323.205f, 270.055f))))
      ZipLinePaths(new ZipLinePath(8, true, List(Vector3(707.264f, 1106.722f, 194.914f), Vector3(812.54f, 1126.035f, 211.099f))))
      ZipLinePaths(new ZipLinePath(9, false, List(Vector3(716.802f, 1474.828f, 265.013f), Vector3(755.438f, 1503.938f, 249.574f), Vector3(778.922f, 1521.633f, 236.262f))))
      ZipLinePaths(new ZipLinePath(10, false, List(Vector3(718.483f, 1328.007f, 270.791f), Vector3(717.052f, 1377.928f, 269.115f), Vector3(715.62f, 1427.849f, 266.689f), Vector3(715.306f, 1438.831f, 264.931f))))
      ZipLinePaths(new ZipLinePath(11, true, List(Vector3(723.223f, 1029.31f, 209.35f), Vector3(715.985f, 1022.277f, 221.533f))))
      ZipLinePaths(new ZipLinePath(12, false, List(Vector3(722.403f, 1012.416f, 275.682f), Vector3(707.86f, 1047.716f, 242.674f), Vector3(693.317f, 1083.015f, 208.861f), Vector3(689.895f, 1091.321f, 198.099f))))
      ZipLinePaths(new ZipLinePath(13, true, List(Vector3(729.519f, 1441.093f, 264.46f), Vector3(726.664f, 1443.454f, 269.965f))))
      ZipLinePaths(new ZipLinePath(14, false, List(Vector3(729.274f, 999.606f, 222.057f), Vector3(756.718f, 960.897f, 238.545f), Vector3(784.163f, 922.189f, 254.308f), Vector3(785.81f, 919.866f, 253.346f))))
      ZipLinePaths(new ZipLinePath(15, true, List(Vector3(732.337f, 1484.986f, 201.35f), Vector3(725.335f, 1474.407f, 212.551f))))
      ZipLinePaths(new ZipLinePath(16, false, List(Vector3(734.082f, 1235.608f, 221.221f), Vector3(737.893f, 1187.823f, 236.175f), Vector3(741.705f, 1140.039f, 250.411f), Vector3(745.517f, 1092.255f, 264.646f), Vector3(749.328f, 1044.47f, 278.881f), Vector3(750.472f, 1030.135f, 281.149f))))
      ZipLinePaths(new ZipLinePath(17, true, List(Vector3(734.217f, 1276.157f, 191.504f), Vector3(718.859f, 1278.728f, 195.646f))))
      ZipLinePaths(new ZipLinePath(18, true, List(Vector3(738.976f, 1350.788f, 201.35f), Vector3(728.212f, 1351.057f, 211.992f))))
      ZipLinePaths(new ZipLinePath(19, false, List(Vector3(745.07f, 1038.912f, 216.63f), Vector3(776.313f, 1079.198f, 216.124f), Vector3(786.114f, 1091.836f, 214.68f))))
      ZipLinePaths(new ZipLinePath(20, true, List(Vector3(754.226f, 727.774f, 185.791f), Vector3(759.174f, 746.876f, 251.22f))))
      ZipLinePaths(new ZipLinePath(21, false, List(Vector3(759.792f, 1153.511f, 286.565f), Vector3(787.783f, 1112.886f, 295.493f), Vector3(815.773f, 1072.261f, 303.632f), Vector3(842.085f, 1034.073f, 307.795f))))
      ZipLinePaths(new ZipLinePath(22, false, List(Vector3(763.438f, 724.138f, 249.259f), Vector3(780.082f, 707.796f, 246.845f), Vector3(796.727f, 691.454f, 231.971f), Vector3(830.015f, 658.77f, 213.98f), Vector3(863.304f, 626.086f, 195.988f), Vector3(875.288f, 614.319f, 186.049f))))
      ZipLinePaths(new ZipLinePath(23, false, List(Vector3(763.764f, 1513.528f, 225.857f), Vector3(715.885f, 1501.26f, 219.038f), Vector3(677.697f, 1492.136f, 211.116f))))
      ZipLinePaths(new ZipLinePath(24, false, List(Vector3(770.787f, 1423.545f, 231.569f), Vector3(755.314f, 1377.756f, 219.472f), Vector3(747.269f, 1353.946f, 211.866f))))
      ZipLinePaths(new ZipLinePath(25, true, List(Vector3(770.57f, 1226.43f, 209.13f), Vector3(771.447f, 1227.514f, 239.351f))))
      ZipLinePaths(new ZipLinePath(26, false, List(Vector3(773.819f, 1236.97f, 239.888f), Vector3(747.687f, 1274.476f, 260.853f), Vector3(748.995f, 1290.503f, 274.802f), Vector3(727.303f, 1303.731f, 276.097f))))
      ZipLinePaths(new ZipLinePath(27, true, List(Vector3(779.487f, 1454.284f, 228.218f), Vector3(766.751f, 1505.627f, 225.35f))))
      ZipLinePaths(new ZipLinePath(28, true, List(Vector3(774.619f, 1447.611f, 216.34f), Vector3(781.047f, 1442.489f, 221.842f))))
      ZipLinePaths(new ZipLinePath(29, false, List(Vector3(789.47f, 1333.949f, 221.19f), Vector3(837.145f, 1318.935f, 223.263f), Vector3(884.819f, 1303.92f, 224.578f), Vector3(921.052f, 1292.509f, 224.48f))))
      ZipLinePaths(new ZipLinePath(30, false, List(Vector3(800.39f, 1230.588f, 217.801f), Vector3(845.704f, 1252.722f, 245.917f), Vector3(881.918f, 1275.956f, 273.294f), Vector3(918.133f, 1299.19f, 300.671f), Vector3(925.074f, 1313.568f, 314.76f), Vector3(938.015f, 1311.946f, 315.545f))))
      ZipLinePaths(new ZipLinePath(31, false, List(Vector3(801.437f, 1542.691f, 235.379f), Vector3(813.077f, 1552.333f, 236.523f), Vector3(839.518f, 1561.875f, 213.914f), Vector3(846.208f, 1561.9f, 213.489f))))
      ZipLinePaths(new ZipLinePath(32, true, List(Vector3(805.789f, 1393.447f, 212.85f), Vector3(805.676f, 1395.361f, 246.35f))))
      ZipLinePaths(new ZipLinePath(33, false, List(Vector3(806.5f, 1182.009f, 217.802f), Vector3(825.037f, 1135.745f, 214.554f), Vector3(842.09f, 1093.181f, 208.952f))))
      ZipLinePaths(new ZipLinePath(34, false, List(Vector3(806.815f, 1168.614f, 290.224f), Vector3(854.326f, 1180.349f, 305.341f), Vector3(878.081f, 1186.216f, 316.04f), Vector3(899.136f, 1187.584f, 318.661f))))
      ZipLinePaths(new ZipLinePath(35, false, List(Vector3(807.862f, 497.707f, 223.021f), Vector3(853.713f, 519.793f, 220.513f), Vector3(860.905f, 523.258f, 218.701f))))
      ZipLinePaths(new ZipLinePath(36, true, List(Vector3(811.458f, 1453.847f, 219.522f), Vector3(798.914f, 1427.003f, 237.724f))))
      ZipLinePaths(new ZipLinePath(37, false, List(Vector3(812.01f, 1468.508f, 224.2f), Vector3(846.323f, 1483.513f, 225.502f), Vector3(856.965f, 1489.472f, 225.25f))))
      ZipLinePaths(new ZipLinePath(38, false, List(Vector3(812.402f, 1323.358f, 294.095f), Vector3(795.583f, 1338.858f, 290.07f), Vector3(778.764f, 1354.358f, 276.782f), Vector3(745.525f, 1384.658f, 247.539f), Vector3(712.287f, 1414.958f, 222.697f), Vector3(692.083f, 1433.375f, 206.87f))))
      ZipLinePaths(new ZipLinePath(39, false, List(Vector3(813.685f, 1399.673f, 246.861f), Vector3(858.609f, 1408.954f, 243.142f))))
      ZipLinePaths(new ZipLinePath(40, false, List(Vector3(821.339f, 923.858f, 253.949f), Vector3(866.375f, 944.748f, 242.984f), Vector3(871.673f, 947.206f, 238.798f))))
      ZipLinePaths(new ZipLinePath(41, false, List(Vector3(837.946f, 1377.303f, 294.087f), Vector3(848.543f, 1397.396f, 293.118f), Vector3(859.141f, 1417.489f, 271.666f), Vector3(876.18f, 1449.795f, 248.523f))))
      ZipLinePaths(new ZipLinePath(42, true, List(Vector3(838.068f, 1040.527f, 212f), Vector3(832.575f, 1040.197f, 217.504f))))
      ZipLinePaths(new ZipLinePath(43, false, List(Vector3(839.968f, 614.924f, 218.86f), Vector3(821.254f, 568.629f, 222.219f), Vector3(802.541f, 522.334f, 224.818f), Vector3(799.173f, 514.001f, 222.95f))))
      ZipLinePaths(new ZipLinePath(44, true, List(Vector3(843.115f, 1121.634f, 204.5f), Vector3(844.011f, 1124.97f, 230.35f))))
      ZipLinePaths(new ZipLinePath(45, false, List(Vector3(844.556f, 1130.363f, 230.85f), Vector3(804.089f, 1161.26f, 228.65f), Vector3(763.623f, 1192.158f, 225.699f), Vector3(760.449f, 1194.581f, 224.701f))))
      ZipLinePaths(new ZipLinePath(46, false, List(Vector3(847.197f, 1120.008f, 230.85f), Vector3(864.661f, 1084.041f, 230.03f))))
      ZipLinePaths(new ZipLinePath(47, true, List(Vector3(849.632f, 947.519f, 208.35f), Vector3(849.006f, 948.423f, 242.35f))))
      ZipLinePaths(new ZipLinePath(48, true, List(Vector3(849.178f, 619.181f, 185.718f), Vector3(848.213f, 618.726f, 218.352f))))
      ZipLinePaths(new ZipLinePath(49, false, List(Vector3(850.022f, 1063.787f, 302.344f), Vector3(817.191f, 1102.491f, 298.148f), Vector3(784.504f, 1141.537f, 290.814f))))
      ZipLinePaths(new ZipLinePath(50, false, List(Vector3(850.185f, 548.151f, 218.939f), Vector3(804.066f, 562.931f, 207.212f), Vector3(759.793f, 577.119f, 193.039f))))
      ZipLinePaths(new ZipLinePath(51, false, List(Vector3(850.747f, 917.806f, 208.859f), Vector3(839.367f, 893.491f, 210.468f), Vector3(827.988f, 869.176f, 199.585f), Vector3(821.729f, 820.546f, 189.779f), Vector3(819.976f, 806.93f, 186.289f))))
      ZipLinePaths(new ZipLinePath(52, false, List(Vector3(851.115f, 946.293f, 242.855f), Vector3(874.386f, 901.525f, 251.024f), Vector3(897.657f, 856.756f, 258.449f), Vector3(920.928f, 811.988f, 265.873f), Vector3(931.423f, 791.799f, 268.816f))))
      ZipLinePaths(new ZipLinePath(53, true, List(Vector3(855.414f, 535.186f, 218.404f), Vector3(855.499f, 535.135f, 223.909f))))
      ZipLinePaths(new ZipLinePath(54, false, List(Vector3(872.014f, 1425.892f, 234.776f), Vector3(830.064f, 1452.015f, 234.397f), Vector3(788.115f, 1478.138f, 236.189f), Vector3(770.496f, 1489.11f, 230.217f), Vector3(767.979f, 1490.677f, 225.644f), Vector3(747.004f, 1503.738f, 212.856f))))
      ZipLinePaths(new ZipLinePath(55, false, List(Vector3(872.76f, 1078.462f, 307.797f), Vector3(835.192f, 1111.962f, 300.324f), Vector3(797.623f, 1145.462f, 292.117f), Vector3(796.887f, 1146.119f, 290.837f))))
      ZipLinePaths(new ZipLinePath(56, false, List(Vector3(873.773f, 1379.824f, 290.094f), Vector3(851.7f, 1417.619f, 264.733f), Vector3(829.627f, 1455.415f, 238.56f), Vector3(816.609f, 1477.166f, 222.955f))))
      ZipLinePaths(new ZipLinePath(57, true, List(Vector3(874.736f, 1018.944f, 208.85f), Vector3(889.984f, 1006.003f, 208.85f))))
      ZipLinePaths(new ZipLinePath(58, false, List(Vector3(877.022f, 1542.334f, 212.574f), Vector3(918.877f, 1570.679f, 206.656f), Vector3(960.732f, 1599.024f, 199.916f), Vector3(964.835f, 1601.803f, 198.004f))))
      ZipLinePaths(new ZipLinePath(59, false, List(Vector3(890.368f, 540.934f, 219.08f), Vector3(887.523f, 590.935f, 229.443f), Vector3(884.678f, 640.936f, 242.27f), Vector3(882.279f, 686.093f, 246.469f))))
      ZipLinePaths(new ZipLinePath(60, false, List(Vector3(898.306f, 1508.067f, 269.626f), Vector3(877.185f, 1514.283f, 268.505f), Vector3(856.064f, 1520.5f, 246.691f), Vector3(825.649f, 1529.452f, 225.853f))))
      ZipLinePaths(new ZipLinePath(61, false, List(Vector3(899.127f, 727.839f, 246.47f), Vector3(901.011f, 777.946f, 238.043f), Vector3(902.894f, 828.053f, 228.734f), Vector3(904.777f, 878.159f, 219.425f), Vector3(904.35f, 915.319f, 208.86f))))
      ZipLinePaths(new ZipLinePath(62, false, List(Vector3(906.014f, 938.879f, 229.299f), Vector3(953.394f, 929.472f, 246.524f), Vector3(1000.773f, 920.066f, 262.882f), Vector3(1048.152f, 910.659f, 287.84f), Vector3(1085.768f, 903.512f, 291.607f))))
      ZipLinePaths(new ZipLinePath(63, false, List(Vector3(915.004f, 908.784f, 208.853f), Vector3(912.459f, 859.095f, 204.635f), Vector3(909.914f, 809.406f, 199.669f), Vector3(908.794f, 787.543f, 196.113f))))
      ZipLinePaths(new ZipLinePath(64, false, List(Vector3(915.106f, 1072.529f, 303.834f), Vector3(959.239f, 1089.108f, 285.144f), Vector3(1003.372f, 1105.687f, 265.692f), Vector3(1047.505f, 1122.266f, 246.24f), Vector3(1088.916f, 1137.444f, 225.963f))))
      ZipLinePaths(new ZipLinePath(65, true, List(Vector3(917.622f, 1395.863f, 247.228f), Vector3(925.782f, 1415.276f, 253.155f))))
      ZipLinePaths(new ZipLinePath(66, false, List(Vector3(919.772f, 1090.854f, 241.953f), Vector3(901.102f, 1135.904f, 253.824f), Vector3(882.431f, 1180.954f, 264.865f), Vector3(863.76f, 1226.004f, 275.906f), Vector3(845.09f, 1271.054f, 286.947f), Vector3(830.9f, 1305.292f, 293.803f))))
      ZipLinePaths(new ZipLinePath(67, false, List(Vector3(922.784f, 988.051f, 229.32f), Vector3(972.611f, 991.883f, 219.995f), Vector3(1002.898f, 994.212f, 212.162f))))
      ZipLinePaths(new ZipLinePath(68, true, List(Vector3(923.81f, 1020.341f, 236.978f), Vector3(922.546f, 982.492f, 238.284f))))
      ZipLinePaths(new ZipLinePath(69, false, List(Vector3(928.145f, 1430.927f, 253.696f), Vector3(904.539f, 1456.263f, 269.4f))))
      ZipLinePaths(new ZipLinePath(70, false, List(Vector3(928.578f, 1489.868f, 275.282f), Vector3(939.807f, 1485.162f, 274.435f), Vector3(951.236f, 1480.557f, 270.012f))))
      ZipLinePaths(new ZipLinePath(71, false, List(Vector3(940.952f, 765.853f, 268.478f), Vector3(924.79f, 748.903f, 265.315f), Vector3(908.628f, 731.954f, 251.685f), Vector3(902.209f, 724.453f, 246.497f))))
      ZipLinePaths(new ZipLinePath(72, false, List(Vector3(943.605f, 1330.998f, 225.806f), Vector3(942.715f, 1356.015f, 224.659f), Vector3(941.825f, 1381.033f, 216.829f), Vector3(941.755f, 1382.995f, 212.794f))))
      ZipLinePaths(new ZipLinePath(73, false, List(Vector3(945.255f, 1187.767f, 318.54f), Vector3(992.83f, 1206.052f, 317.522f), Vector3(1040.406f, 1224.336f, 315.76f), Vector3(1087.982f, 1242.621f, 313.998f), Vector3(1135.558f, 1260.905f, 312.236f), Vector3(1147.685f, 1265.566f, 310.41f))))
      ZipLinePaths(new ZipLinePath(74, false, List(Vector3(954.358f, 1499.699f, 270.422f), Vector3(947.135f, 1521.815f, 266.047f), Vector3(939.912f, 1543.932f, 251.825f), Vector3(925.465f, 1588.165f, 228.954f), Vector3(913.002f, 1626.326f, 208.144f))))
      ZipLinePaths(new ZipLinePath(75, false, List(Vector3(955.561f, 817.315f, 268.753f), Vector3(944.748f, 837.827f, 265.663f), Vector3(933.435f, 858.94f, 255.123f), Vector3(910.357f, 902.008f, 240.513f), Vector3(896.329f, 928.187f, 229.295f))))
      ZipLinePaths(new ZipLinePath(76, false, List(Vector3(959.479f, 1311.02f, 229.35f), Vector3(936.058f, 1315.299f, 238.807f), Vector3(917.137f, 1324.378f, 257.265f), Vector3(878.738f, 1337.899f, 294.48f), Vector3(871.584f, 1345.104f, 294.076f))))
      ZipLinePaths(new ZipLinePath(77, true, List(Vector3(958.375f, 1463.39f, 196.12f), Vector3(950.221f, 1455.99f, 212.172f))))
      ZipLinePaths(new ZipLinePath(78, true, List(Vector3(960.543f, 1394.809f, 204.35f), Vector3(955.447f, 1389.008f, 212.281f))))
      ZipLinePaths(new ZipLinePath(79, false, List(Vector3(962.372f, 1320.909f, 225.715f), Vector3(973.943f, 1364.715f, 248.139f), Vector3(977.513f, 1414.022f, 260.669f), Vector3(981.084f, 1463.329f, 273.198f), Vector3(981.224f, 1465.262f, 272.846f))))
      ZipLinePaths(new ZipLinePath(80, false, List(Vector3(963.682f, 972.734f, 208.858f), Vector3(982.1f, 963.019f, 212.558f))))
      ZipLinePaths(new ZipLinePath(81, true, List(Vector3(963.71f, 1377.081f, 212.079f), Vector3(982.598f, 1384.136f, 223.736f))))
      ZipLinePaths(new ZipLinePath(82, false, List(Vector3(964.68f, 1283.954f, 322.677f), Vector3(970.913f, 1265.208f, 320.501f), Vector3(978.346f, 1241.462f, 300.857f), Vector3(992.011f, 1198.969f, 278.318f), Vector3(1005.677f, 1156.476f, 255.779f), Vector3(1016.336f, 1123.332f, 233.389f))))
      ZipLinePaths(new ZipLinePath(83, false, List(Vector3(966.06f, 1034.152f, 289.043f), Vector3(942.97f, 1039.214f, 293.837f), Vector3(919.481f, 1044.876f, 306.952f), Vector3(912.776f, 1045.47f, 309.571f), Vector3(906.071f, 1046.064f, 307.813f))))
      ZipLinePaths(new ZipLinePath(84, true, List(Vector3(969.09f, 1626.228f, 197.794f), Vector3(967.362f, 1627.844f, 230.351f))))
      ZipLinePaths(new ZipLinePath(85, false, List(Vector3(969.733f, 1048.776f, 289.053f), Vector3(969.033f, 1089.005f, 258.458f), Vector3(968.333f, 1129.235f, 227.122f), Vector3(968.25f, 1133.968f, 219.747f))))
      ZipLinePaths(new ZipLinePath(86, true, List(Vector3(970.108f, 1021.976f, 210.45f), Vector3(971.104f, 1022.424f, 242.351f))))
      ZipLinePaths(new ZipLinePath(87, false, List(Vector3(970.945f, 1623.276f, 230.839f), Vector3(988.468f, 1603.231f, 238.464f), Vector3(1006.792f, 1599.687f, 261.579f), Vector3(1021.222f, 1590.592f, 283.002f), Vector3(1035.853f, 1581.197f, 285.922f))))
      ZipLinePaths(new ZipLinePath(88, false, List(Vector3(975.137f, 893.285f, 211.98f), Vector3(928.003f, 888.951f, 228.828f), Vector3(880.869f, 884.618f, 244.946f), Vector3(854.473f, 882.191f, 253.948f))))
      ZipLinePaths(new ZipLinePath(89, false, List(Vector3(976.142f, 1112.293f, 232.494f), Vector3(933.521f, 1139.785f, 227.909f), Vector3(890.901f, 1167.278f, 222.566f), Vector3(848.281f, 1194.77f, 217.223f), Vector3(844.938f, 1196.926f, 212.254f))))
      ZipLinePaths(new ZipLinePath(90, false, List(Vector3(977.728f, 813.361f, 268.443f), Vector3(978.126f, 864.072f, 274.605f), Vector3(978.525f, 914.782f, 280.006f), Vector3(978.923f, 965.493f, 285.406f), Vector3(979.314f, 1015.209f, 289.192f))))
      ZipLinePaths(new ZipLinePath(91, false, List(Vector3(979.078f, 923.473f, 211.952f), Vector3(948.789f, 940.58f, 208.852f))))
      ZipLinePaths(new ZipLinePath(92, false, List(Vector3(979.933f, 1302.087f, 315.557f), Vector3(994.753f, 1292.557f, 314.229f), Vector3(1016.774f, 1282.428f, 288.809f), Vector3(1053.614f, 1262.769f, 261.308f), Vector3(1088.982f, 1243.896f, 230.267f))))
      ZipLinePaths(new ZipLinePath(93, false, List(Vector3(980.715f, 1021.867f, 242.874f), Vector3(1024.608f, 1031.666f, 267.647f), Vector3(1068.499f, 1041.465f, 291.687f), Vector3(1086.971f, 1039f, 310.66f), Vector3(1104.044f, 1049.435f, 311.942f))))
      ZipLinePaths(new ZipLinePath(94, true, List(Vector3(991.007f, 1141.841f, 214.35f), Vector3(999.178f, 1143.214f, 219.5f))))
      ZipLinePaths(new ZipLinePath(95, false, List(Vector3(993.009f, 1025.976f, 289.111f), Vector3(1013.284f, 1015.49f, 288.487f), Vector3(1033.56f, 1005.004f, 274.418f), Vector3(1074.112f, 984.032f, 251.628f), Vector3(1115.475f, 962.641f, 222.931f), Vector3(1153.594f, 942.928f, 204.75f))))
      ZipLinePaths(new ZipLinePath(96, false, List(Vector3(996.021f, 1449.051f, 225.69f), Vector3(1037.859f, 1419.909f, 227.523f), Vector3(1078.055f, 1391.909f, 225.69f))))
      ZipLinePaths(new ZipLinePath(97, false, List(Vector3(1006.725f, 1501.894f, 270.008f), Vector3(1006.849f, 1523.173f, 267.384f), Vector3(1006.912f, 1533.812f, 256.523f), Vector3(1006.974f, 1544.452f, 242.663f), Vector3(1007.223f, 1587.01f, 209.361f), Vector3(1007.052f, 1593.917f, 209.696f))))
      ZipLinePaths(new ZipLinePath(98, true, List(Vector3(1021.3f, 1735.504f, 195.26f), Vector3(1028.804f, 1734.83f, 225.08f))))
      ZipLinePaths(new ZipLinePath(99, false, List(Vector3(1025.143f, 712.281f, 233.024f), Vector3(1020.116f, 737.102f, 233.165f), Vector3(1015.089f, 761.924f, 227.811f), Vector3(1005.034f, 811.567f, 221.863f), Vector3(994.98f, 861.21f, 215.914f), Vector3(990.84f, 881.651f, 211.953f))))
      ZipLinePaths(new ZipLinePath(100, false, List(Vector3(1029.198f, 1742.012f, 225.584f), Vector3(1052.55f, 1786.849f, 219.613f), Vector3(1075.902f, 1831.686f, 212.905f), Vector3(1099.254f, 1876.522f, 206.197f), Vector3(1109.328f, 1895.864f, 201.704f))))
      ZipLinePaths(new ZipLinePath(101, false, List(Vector3(1031.114f, 1732.901f, 225.593f), Vector3(1022.527f, 1684.799f, 215.719f), Vector3(1013.939f, 1636.698f, 205.102f), Vector3(1009.302f, 1610.723f, 196.964f))))
      ZipLinePaths(new ZipLinePath(102, false, List(Vector3(1034.274f, 943.094f, 222.9f), Vector3(1082.772f, 927.34f, 224.31f), Vector3(1131.269f, 911.586f, 224.977f), Vector3(1179.766f, 895.832f, 225.643f), Vector3(1213.247f, 885.838f, 221.1f))))
      ZipLinePaths(new ZipLinePath(103, false, List(Vector3(1034.409f, 1563.401f, 272.585f), Vector3(1011.925f, 1558.256f, 268.822f), Vector3(989.441f, 1553.111f, 257.502f), Vector3(944.473f, 1542.82f, 234.711f), Vector3(904.002f, 1533.559f, 214.166f))))
      ZipLinePaths(new ZipLinePath(104, false, List(Vector3(1035.377f, 1080.205f, 262.564f), Vector3(989.509f, 1063.28f, 252.81f), Vector3(955.567f, 1050.755f, 241.954f))))
      ZipLinePaths(new ZipLinePath(105, true, List(Vector3(1044.219f, 1079.095f, 232.45f), Vector3(1040.901f, 1080.062f, 262.05f))))
      ZipLinePaths(new ZipLinePath(106, false, List(Vector3(1044.836f, 889.278f, 205.783f), Vector3(1018.74f, 855.748f, 233.598f), Vector3(992.444f, 822.219f, 260.361f), Vector3(987.466f, 815.848f, 268.503f), Vector3(982.289f, 809.377f, 268.435f))))
      ZipLinePaths(new ZipLinePath(107, false, List(Vector3(1046.498f, 1557.897f, 272.54f), Vector3(1000.009f, 1539.501f, 272.4f), Vector3(953.522f, 1521.105f, 271.521f), Vector3(907.034f, 1502.709f, 269.38f))))
      ZipLinePaths(new ZipLinePath(108, true, List(Vector3(1050.431f, 901.111f, 198.721f), Vector3(1048.107f, 930.118f, 211.95f))))
      ZipLinePaths(new ZipLinePath(109, false, List(Vector3(1051.466f, 1522.68f, 211.782f), Vector3(1078.305f, 1479.63f, 217.733f), Vector3(1093.966f, 1452.951f, 220.322f))))
      ZipLinePaths(new ZipLinePath(110, false, List(Vector3(1055.076f, 1873.539f, 237.931f), Vector3(1079.972f, 1917.996f, 236.679f), Vector3(1103.404f, 1959.838f, 232.911f))))
      ZipLinePaths(new ZipLinePath(111, false, List(Vector3(1061.645f, 1734.927f, 205.23f), Vector3(1096.745f, 1766.528f, 229.978f), Vector3(1110.51f, 1778.92f, 231.729f))))
      ZipLinePaths(new ZipLinePath(112, false, List(Vector3(1062.419f, 1853.797f, 238.042f), Vector3(1100.781f, 1820.478f, 234.502f), Vector3(1111.312f, 1811.331f, 231.692f))))
      ZipLinePaths(new ZipLinePath(113, true, List(Vector3(1071.166f, 1202.403f, 219.85f), Vector3(1069.18f, 1206.548f, 272.258f))))
      ZipLinePaths(new ZipLinePath(114, false, List(Vector3(1071.354f, 1196.264f, 272.775f), Vector3(1029.289f, 1170.421f, 281.462f), Vector3(987.226f, 1144.579f, 289.413f), Vector3(945.163f, 1118.737f, 297.365f), Vector3(903.101f, 1092.894f, 305.316f), Vector3(882.07f, 1079.973f, 307.796f))))
      ZipLinePaths(new ZipLinePath(115, false, List(Vector3(1073.915f, 1593.898f, 272.546f), Vector3(1093.854f, 1581.779f, 270.067f), Vector3(1113.994f, 1567.46f, 260.705f), Vector3(1154.274f, 1538.822f, 248.124f), Vector3(1194.553f, 1510.185f, 235.544f), Vector3(1227.725f, 1486.601f, 222.487f))))
      ZipLinePaths(new ZipLinePath(116, false, List(Vector3(1079.887f, 1860.768f, 193.96f), Vector3(1113.7f, 1823.94f, 195.384f), Vector3(1147.514f, 1787.111f, 196.045f), Vector3(1152.247f, 1781.955f, 194.44f))))
      ZipLinePaths(new ZipLinePath(117, false, List(Vector3(1080.911f, 1572.173f, 278.049f), Vector3(1092.004f, 1550.688f, 276.731f), Vector3(1103.097f, 1529.204f, 265.932f), Vector3(1125.283f, 1486.236f, 253.217f), Vector3(1147.469f, 1443.267f, 240.502f), Vector3(1159.894f, 1419.204f, 231.029f))))
      ZipLinePaths(new ZipLinePath(118, false, List(Vector3(1089.579f, 877.305f, 291.235f), Vector3(1069.444f, 833.967f, 276.797f), Vector3(1049.308f, 790.629f, 262.079f), Vector3(1029.173f, 747.291f, 247.36f), Vector3(1011.858f, 710.02f, 233.124f))))
      ZipLinePaths(new ZipLinePath(119, true, List(Vector3(1091.648f, 1218.925f, 282.28f), Vector3(1120.246f, 1191.143f, 272.28f))))
      ZipLinePaths(new ZipLinePath(120, true, List(Vector3(1091.707f, 1219.346f, 272.28f), Vector3(1120.248f, 1190.548f, 282.28f))))
      ZipLinePaths(new ZipLinePath(121, true, List(Vector3(1093.253f, 1192.72f, 267.28f), Vector3(1093.221f, 1193.133f, 272.28f))))
      ZipLinePaths(new ZipLinePath(122, false, List(Vector3(1093.846f, 1164.617f, 277.782f), Vector3(1061.277f, 1126.854f, 282.223f), Vector3(1028.709f, 1089.092f, 285.924f), Vector3(996.141f, 1051.329f, 289.624f), Vector3(994.838f, 1049.819f, 289.061f))))
      ZipLinePaths(new ZipLinePath(123, false, List(Vector3(1098.524f, 1242.661f, 282.779f), Vector3(1062.263f, 1268.949f, 261.253f), Vector3(1026.002f, 1295.237f, 239.025f), Vector3(1010.048f, 1306.804f, 226.202f))))
      ZipLinePaths(new ZipLinePath(124, false, List(Vector3(1102.365f, 1079.055f, 311.692f), Vector3(1117.761f, 1118.575f, 284.12f), Vector3(1133.157f, 1158.095f, 255.805f), Vector3(1144.931f, 1188.316f, 231.505f))))
      ZipLinePaths(new ZipLinePath(125, true, List(Vector3(1102.429f, 1172.438f, 219.85f), Vector3(1105.492f, 1168.703f, 271.778f), Vector3(1105.793f, 1168.336f, 277.28f))))
      ZipLinePaths(new ZipLinePath(126, false, List(Vector3(1105.881f, 1796.622f, 231.706f), Vector3(1084.406f, 1785.3f, 230.322f), Vector3(1062.931f, 1773.978f, 220.459f), Vector3(1019.981f, 1751.334f, 208.49f), Vector3(1014.827f, 1748.616f, 205.625f))))
      ZipLinePaths(new ZipLinePath(127, false, List(Vector3(1105.921f, 1241.768f, 277.822f), Vector3(1122.474f, 1286.219f, 259.799f), Vector3(1139.028f, 1330.669f, 241.061f), Vector3(1153.31f, 1369.019f, 221.222f))))
      ZipLinePaths(new ZipLinePath(128, false, List(Vector3(1106.815f, 1166.369f, 272.712f), Vector3(1127.341f, 1119.926f, 278.234f), Vector3(1147.866f, 1073.483f, 283.001f), Vector3(1168.392f, 1027.04f, 287.768f), Vector3(1188.918f, 980.598f, 292.536f), Vector3(1207.834f, 937.797f, 296.002f))))
      ZipLinePaths(new ZipLinePath(129, true, List(Vector3(1107.127f, 1238.536f, 219.7f), Vector3(1100.346f, 1235.501f, 282.273f))))
      ZipLinePaths(new ZipLinePath(130, false, List(Vector3(1110.596f, 927.637f, 291.518f), Vector3(1101.755f, 947.852f, 288.767f), Vector3(1092.914f, 968.067f, 268.736f), Vector3(1075.232f, 1008.496f, 242.424f), Vector3(1058.258f, 1047.309f, 219.77f))))
      ZipLinePaths(new ZipLinePath(131, false, List(Vector3(1115.095f, 1244.667f, 282.835f), Vector3(1126.296f, 1264.792f, 280.253f), Vector3(1137.497f, 1284.918f, 268.214f), Vector3(1148.899f, 1304.669f, 253.056f), Vector3(1160.301f, 1324.42f, 237.899f), Vector3(1174.163f, 1348.43f, 221.228f))))
      ZipLinePaths(new ZipLinePath(132, false, List(Vector3(1117.499f, 1963.794f, 233.051f), Vector3(1159.472f, 1935.007f, 237.142f), Vector3(1164.41f, 1931.62f, 236.951f))))
      ZipLinePaths(new ZipLinePath(133, true, List(Vector3(1120.295f, 1190.859f, 277.28f), Vector3(1091.482f, 1219.905f, 277.28f))))
      ZipLinePaths(new ZipLinePath(134, false, List(Vector3(1124.578f, 1549.382f, 212.12f), Vector3(1074.829f, 1544.506f, 214.189f), Vector3(1068.859f, 1543.921f, 211.28f))))
      ZipLinePaths(new ZipLinePath(135, true, List(Vector3(1130.396f, 1210.112f, 219.65f), Vector3(1137.396f, 1214.411f, 282.362f))))
      ZipLinePaths(new ZipLinePath(136, false, List(Vector3(1131.24f, 888.787f, 294.03f), Vector3(1182.184f, 890.508f, 296.375f), Vector3(1208.156f, 891.386f, 296f))))
      ZipLinePaths(new ZipLinePath(137, false, List(Vector3(1142.54f, 1218.66f, 282.912f), Vector3(1169.204f, 1261.86f, 288.479f), Vector3(1195.868f, 1305.059f, 293.298f), Vector3(1197.437f, 1307.6f, 292.852f))))
      ZipLinePaths(new ZipLinePath(138, false, List(Vector3(1144.458f, 1193.852f, 282.719f), Vector3(1140.711f, 1145.805f, 296.788f), Vector3(1136.963f, 1097.758f, 310.119f), Vector3(1134.264f, 1081.383f, 314.518f))))
      ZipLinePaths(new ZipLinePath(139, false, List(Vector3(1149.446f, 1809.731f, 231.685f), Vector3(1167.241f, 1816.872f, 227.954f), Vector3(1190.037f, 1826.814f, 206.722f), Vector3(1205.159f, 1833.178f, 194.758f))))
      ZipLinePaths(new ZipLinePath(140, false, List(Vector3(1151.375f, 1308.43f, 310.411f), Vector3(1107.283f, 1285.017f, 314.004f), Vector3(1063.191f, 1261.604f, 316.848f), Vector3(1019.099f, 1238.191f, 319.692f), Vector3(975.008f, 1214.777f, 322.536f), Vector3(945.907f, 1199.325f, 324.141f))))
      ZipLinePaths(new ZipLinePath(141, false, List(Vector3(1151.752f, 1744.783f, 205.193f), Vector3(1170.417f, 1709.389f, 235.92f), Vector3(1180.476f, 1693.4f, 264.353f), Vector3(1188.335f, 1675.411f, 265.287f))))
      ZipLinePaths(new ZipLinePath(142, false, List(Vector3(1154.103f, 1045.211f, 311.68f), Vector3(1205.072f, 1046.855f, 311.765f), Vector3(1241.05f, 1048.015f, 308.85f))))
      ZipLinePaths(new ZipLinePath(143, false, List(Vector3(1154.402f, 1908.73f, 236.95f), Vector3(1109.303f, 1887.142f, 238.339f), Vector3(1065.106f, 1865.985f, 237.93f))))
      ZipLinePaths(new ZipLinePath(144, false, List(Vector3(1157.948f, 1371.891f, 253.866f), Vector3(1109.132f, 1378.159f, 245.764f), Vector3(1060.317f, 1384.427f, 236.926f), Vector3(1011.501f, 1390.696f, 228.089f), Vector3(981.473f, 1398.468f, 224.307f))))
      ZipLinePaths(new ZipLinePath(145, false, List(Vector3(1159.605f, 1452.411f, 220.533f), Vector3(1186.886f, 1495.243f, 216.614f), Vector3(1195.979f, 1509.521f, 213.003f))))
      ZipLinePaths(new ZipLinePath(146, false, List(Vector3(1164.409f, 1263.605f, 310.411f), Vector3(1211.954f, 1245.315f, 308.766f), Vector3(1259.499f, 1227.024f, 306.377f), Vector3(1265.093f, 1224.872f, 304.001f))))
      ZipLinePaths(new ZipLinePath(147, true, List(Vector3(1168.129f, 1374.494f, 220.68f), Vector3(1164.962f, 1371.182f, 253.351f))))
      ZipLinePaths(new ZipLinePath(148, true, List(Vector3(1167.336f, 1311.256f, 214.3f), Vector3(1166.591f, 1298.844f, 219.85f))))
      ZipLinePaths(new ZipLinePath(149, false, List(Vector3(1169.164f, 1623.147f, 265.079f), Vector3(1191.24f, 1579.293f, 256.342f), Vector3(1213.317f, 1535.44f, 246.87f), Vector3(1235.393f, 1491.586f, 237.399f), Vector3(1238.901f, 1483.6f, 233.64f))))
      ZipLinePaths(new ZipLinePath(150, true, List(Vector3(1171.102f, 1413.013f, 225.003f), Vector3(1175.787f, 1413.078f, 230.506f))))
      ZipLinePaths(new ZipLinePath(151, false, List(Vector3(1171.763f, 1889.791f, 240.854f), Vector3(1152.036f, 1844.142f, 236.32f), Vector3(1141.384f, 1819.492f, 231.694f))))
      ZipLinePaths(new ZipLinePath(152, false, List(Vector3(1173.489f, 1450.182f, 310.544f), Vector3(1126.382f, 1462.615f, 300.085f), Vector3(1079.275f, 1475.048f, 288.844f), Vector3(1032.168f, 1487.481f, 277.603f), Vector3(1010.499f, 1493.2f, 270.014f))))
      ZipLinePaths(new ZipLinePath(153, true, List(Vector3(1182.916f, 1506.86f, 194.35f), Vector3(1181.854f, 1524.924f, 211.925f))))
      ZipLinePaths(new ZipLinePath(154, false, List(Vector3(1183.893f, 814.094f, 216.873f), Vector3(1138.422f, 796.348f, 206.755f), Vector3(1092.951f, 778.602f, 195.908f), Vector3(1055.664f, 764.05f, 185.843f))))
      ZipLinePaths(new ZipLinePath(155, false, List(Vector3(1188.76f, 862.336f, 212.351f), Vector3(1140.34f, 850.007f, 211.072f), Vector3(1091.921f, 837.678f, 209.034f), Vector3(1077.001f, 833.719f, 206.491f))))
      ZipLinePaths(new ZipLinePath(156, false, List(Vector3(1191.935f, 1329.688f, 292.852f), Vector3(1142.362f, 1326.045f, 299.044f), Vector3(1092.79f, 1322.401f, 304.504f), Vector3(1043.217f, 1318.757f, 309.964f), Vector3(993.646f, 1315.113f, 315.423f), Vector3(982.741f, 1314.312f, 315.553f))))
      ZipLinePaths(new ZipLinePath(157, true, List(Vector3(1192.393f, 1929.829f, 196.899f), Vector3(1179.993f, 1912.709f, 236.45f))))
      ZipLinePaths(new ZipLinePath(158, true, List(Vector3(1194.117f, 818.768f, 185.029f), Vector3(1194.088f, 818.005f, 216.35f))))
      ZipLinePaths(new ZipLinePath(159, false, List(Vector3(1196.939f, 1341.983f, 293.059f), Vector3(1174.428f, 1331.787f, 290.82f), Vector3(1152.217f, 1323.691f, 277.494f), Vector3(1107.796f, 1307.498f, 261.211f), Vector3(1063.375f, 1291.306f, 244.928f), Vector3(1018.953f, 1275.113f, 228.646f), Vector3(1016.288f, 1274.141f, 226.178f))))
      ZipLinePaths(new ZipLinePath(160, false, List(Vector3(1198.132f, 1108.054f, 222.911f), Vector3(1148.266f, 1106.413f, 227.001f), Vector3(1098.4f, 1104.771f, 230.337f), Vector3(1048.535f, 1103.129f, 233.672f), Vector3(1041.553f, 1102.899f, 232.951f))))
      ZipLinePaths(new ZipLinePath(161, false, List(Vector3(1200.709f, 1913.954f, 236.974f), Vector3(1217.736f, 1928.55f, 233.206f), Vector3(1234.763f, 1946.146f, 217.583f), Vector3(1249.453f, 1960.033f, 207.743f))))
      ZipLinePaths(new ZipLinePath(162, false, List(Vector3(1201.001f, 932.287f, 296.001f), Vector3(1151.813f, 923.791f, 293.771f), Vector3(1132.138f, 920.393f, 291.211f))))
      ZipLinePaths(new ZipLinePath(163, true, List(Vector3(1201.371f, 1448.316f, 253.36f), Vector3(1192.845f, 1447.079f, 310.07f))))
      ZipLinePaths(new ZipLinePath(164, true, List(Vector3(1201.389f, 1364.36f, 233.756f), Vector3(1201.015f, 1360.354f, 251.467f))))
      ZipLinePaths(new ZipLinePath(165, false, List(Vector3(1201.803f, 1918.873f, 242.472f), Vector3(1226.809f, 1877.857f, 229.309f), Vector3(1251.815f, 1836.841f, 215.424f), Vector3(1276.821f, 1795.826f, 201.539f), Vector3(1283.823f, 1784.341f, 195.591f))))
      ZipLinePaths(new ZipLinePath(166, false, List(Vector3(1201.995f, 1390.422f, 310.6f), Vector3(1204.786f, 1368.885f, 306.222f), Vector3(1206.381f, 1358.416f, 295.706f), Vector3(1207.577f, 1347.348f, 292.929f))))
      ZipLinePaths(new ZipLinePath(167, false, List(Vector3(1204.983f, 964.893f, 219.91f), Vector3(1183.305f, 1011f, 222.92f), Vector3(1161.628f, 1057.107f, 225.178f), Vector3(1139.95f, 1103.214f, 227.436f), Vector3(1119.123f, 1147.512f, 227.91f))))
      ZipLinePaths(new ZipLinePath(168, true, List(Vector3(1205.446f, 1458.927f, 220.37f), Vector3(1206.823f, 1455.583f, 253.35f))))
      ZipLinePaths(new ZipLinePath(169, false, List(Vector3(1212.761f, 1666.804f, 265.077f), Vector3(1238.062f, 1703.093f, 240.457f), Vector3(1263.363f, 1739.382f, 215.085f), Vector3(1279.238f, 1762.152f, 197.042f))))
      ZipLinePaths(new ZipLinePath(170, false, List(Vector3(1212.954f, 780.028f, 193.91f), Vector3(1255.618f, 752.1f, 195.372f), Vector3(1298.282f, 724.172f, 196.091f), Vector3(1340.946f, 696.244f, 196.811f), Vector3(1341.783f, 695.697f, 195.51f))))
      ZipLinePaths(new ZipLinePath(171, false, List(Vector3(1215.651f, 1358.743f, 251.671f), Vector3(1254.53f, 1391.415f, 254.465f), Vector3(1271.301f, 1405.509f, 255.04f))))
      ZipLinePaths(new ZipLinePath(172, false, List(Vector3(1215.938f, 1852.856f, 226.888f), Vector3(1206.275f, 1860.843f, 227.299f), Vector3(1195.412f, 1868.23f, 243.848f), Vector3(1185.887f, 1883.405f, 244.091f))))
      ZipLinePaths(new ZipLinePath(173, true, List(Vector3(1216.935f, 1846.375f, 193.717f), Vector3(1217.026f, 1845.678f, 226.35f))))
      ZipLinePaths(new ZipLinePath(174, false, List(Vector3(1217.901f, 1306.547f, 252.003f), Vector3(1178.924f, 1329.026f, 230.922f), Vector3(1169.569f, 1334.421f, 221.2f))))
      ZipLinePaths(new ZipLinePath(175, false, List(Vector3(1222.97f, 892.562f, 296f), Vector3(1273.851f, 894.346f, 299.697f), Vector3(1292.807f, 895.01f, 299.837f), Vector3(1314.263f, 895.475f, 300.301f))))
      ZipLinePaths(new ZipLinePath(176, true, List(Vector3(1226.215f, 1306.36f, 220.591f), Vector3(1225.078f, 1307.135f, 251.49f))))
      ZipLinePaths(new ZipLinePath(177, false, List(Vector3(1229.979f, 897.9f, 222.686f), Vector3(1248.429f, 856.834f, 250.003f), Vector3(1265.379f, 819.569f, 273.788f), Vector3(1267.59f, 810.425f, 274.254f))))
      ZipLinePaths(new ZipLinePath(178, true, List(Vector3(1234.241f, 1678.155f, 195.187f), Vector3(1235.695f, 1678.25f, 226.352f))))
      ZipLinePaths(new ZipLinePath(179, false, List(Vector3(1243.039f, 1672.894f, 226.877f), Vector3(1257.864f, 1653.72f, 226.354f), Vector3(1272.69f, 1634.546f, 215.348f), Vector3(1302.341f, 1596.197f, 203.081f), Vector3(1318.352f, 1575.489f, 195.226f))))
      ZipLinePaths(new ZipLinePath(180, false, List(Vector3(1249.659f, 799.131f, 274.221f), Vector3(1230.152f, 794.469f, 271.908f), Vector3(1210.046f, 789.308f, 258.099f), Vector3(1171.632f, 780.485f, 217.049f), Vector3(1146.663f, 774.518f, 193.216f))))
      ZipLinePaths(new ZipLinePath(181, false, List(Vector3(1250.666f, 1023.941f, 308.491f), Vector3(1273.371f, 979.444f, 307.138f), Vector3(1296.076f, 934.946f, 305.027f), Vector3(1313.786f, 900.238f, 300.301f))))
      ZipLinePaths(new ZipLinePath(182, false, List(Vector3(1258.081f, 1239.629f, 233.701f), Vector3(1260.773f, 1194.323f, 257.733f), Vector3(1263.464f, 1149.017f, 282.723f), Vector3(1266.156f, 1103.71f, 306.713f), Vector3(1267.717f, 1077.432f, 308.82f))))
      ZipLinePaths(new ZipLinePath(183, true, List(Vector3(1260.415f, 982.55f, 212.75f), Vector3(1257.704f, 982.99f, 246.35f))))
      ZipLinePaths(new ZipLinePath(184, true, List(Vector3(1261.826f, 1507.387f, 221.5f), Vector3(1263.065f, 1509.887f, 253.35f))))
      ZipLinePaths(new ZipLinePath(185, false, List(Vector3(1262.6f, 1515.681f, 253.853f), Vector3(1237.576f, 1558.503f, 260.945f), Vector3(1212.551f, 1601.325f, 267.281f), Vector3(1196.536f, 1628.731f, 267.893f))))
      ZipLinePaths(new ZipLinePath(186, false, List(Vector3(1263.972f, 1211.529f, 304.001f), Vector3(1230.292f, 1174.694f, 307.831f), Vector3(1196.613f, 1137.86f, 310.924f), Vector3(1162.934f, 1101.025f, 314.017f), Vector3(1140.706f, 1076.714f, 314.501f))))
      ZipLinePaths(new ZipLinePath(187, false, List(Vector3(1264.862f, 981.913f, 246.854f), Vector3(1281.838f, 982.081f, 242.85f), Vector3(1298.815f, 982.25f, 222.859f))))
      ZipLinePaths(new ZipLinePath(188, false, List(Vector3(1265.582f, 1404.584f, 253.22f), Vector3(1276.61f, 1372.427f, 251.88f))))
      ZipLinePaths(new ZipLinePath(189, false, List(Vector3(1271.695f, 1083.292f, 247.734f), Vector3(1269.115f, 1109.294f, 246.993f), Vector3(1266.536f, 1135.296f, 241.156f), Vector3(1259.177f, 1185.399f, 235.122f), Vector3(1251.819f, 1235.503f, 234.087f), Vector3(1244.16f, 1267.106f, 232.753f), Vector3(1243.017f, 1295.431f, 221.094f))))
      ZipLinePaths(new ZipLinePath(190, false, List(Vector3(1280.299f, 1356.039f, 251.882f), Vector3(1313.643f, 1317.727f, 257.273f), Vector3(1346.987f, 1279.415f, 261.877f), Vector3(1380.332f, 1241.102f, 266.481f), Vector3(1413.676f, 1202.79f, 271.085f), Vector3(1428.714f, 1185.512f, 272.052f))))
      ZipLinePaths(new ZipLinePath(191, false, List(Vector3(1283.458f, 890.038f, 212.15f), Vector3(1284.727f, 924.985f, 213.25f))))
      ZipLinePaths(new ZipLinePath(192, true, List(Vector3(1284.624f, 801.067f, 273.7f), Vector3(1281.635f, 798.615f, 279.206f))))
      ZipLinePaths(new ZipLinePath(193, false, List(Vector3(1292.551f, 1066.859f, 308.492f), Vector3(1313.109f, 1065.355f, 304.285f), Vector3(1331.767f, 1064.051f, 274.575f), Vector3(1369.083f, 1061.442f, 239.914f), Vector3(1393.96f, 1059.703f, 213.35f))))
      ZipLinePaths(new ZipLinePath(194, true, List(Vector3(1305.056f, 1321.493f, 210.55f), Vector3(1291.481f, 1301.007f, 232.65f))))
      ZipLinePaths(new ZipLinePath(195, false, List(Vector3(1304.347f, 1371.371f, 253.835f), Vector3(1280.917f, 1375.189f, 258.523f), Vector3(1256.888f, 1380.207f, 243.099f), Vector3(1208.829f, 1386.642f, 237.107f), Vector3(1196.148f, 1392.034f, 228.88f), Vector3(1176.148f, 1391.834f, 221.195f))))
      ZipLinePaths(new ZipLinePath(196, true, List(Vector3(1306.335f, 1369.803f, 220.18f), Vector3(1307.635f, 1371.319f, 253.251f))))
      ZipLinePaths(new ZipLinePath(197, false, List(Vector3(1310.992f, 913.011f, 300.297f), Vector3(1290.856f, 918.141f, 295.917f), Vector3(1270.721f, 923.271f, 273.234f), Vector3(1230.45f, 933.531f, 245.426f), Vector3(1197.428f, 941.944f, 219.125f))))
      ZipLinePaths(new ZipLinePath(198, false, List(Vector3(1312.511f, 1753.802f, 198.17f), Vector3(1321.508f, 1704.613f, 199f), Vector3(1330.504f, 1655.425f, 199.087f), Vector3(1333.743f, 1637.717f, 197.87f))))
      ZipLinePaths(new ZipLinePath(199, true, List(Vector3(1312.814f, 1008.901f, 234.266f), Vector3(1296.46f, 998.06f, 247.893f))))
      ZipLinePaths(new ZipLinePath(200, true, List(Vector3(1315.533f, 658.726f, 185.933f), Vector3(1315.364f, 658.643f, 219.55f))))
      ZipLinePaths(new ZipLinePath(201, true, List(Vector3(1313.047f, 747.979f, 185.79f), Vector3(1333.818f, 745.34f, 213.76f))))
      ZipLinePaths(new ZipLinePath(202, false, List(Vector3(1326.858f, 659.881f, 220.097f), Vector3(1343.771f, 687.963f, 227.17f), Vector3(1325.685f, 716.046f, 245.475f), Vector3(1333.212f, 763.211f, 263.353f), Vector3(1336.975f, 786.793f, 272.292f), Vector3(1340.738f, 810.376f, 281.231f), Vector3(1348.265f, 857.541f, 299.109f), Vector3(1350.11f, 869.101f, 306.402f), Vector3(1351.955f, 880.661f, 307.438f))))
      ZipLinePaths(new ZipLinePath(203, false, List(Vector3(1322.759f, 1421.014f, 244.694f), Vector3(1361.905f, 1388.616f, 241.124f), Vector3(1399.517f, 1357.489f, 232.954f))))
      ZipLinePaths(new ZipLinePath(204, true, List(Vector3(1323.032f, 1295.479f, 204.5f), Vector3(1344.755f, 1292.424f, 211.2f))))
      ZipLinePaths(new ZipLinePath(205, false, List(Vector3(1326.35f, 956.514f, 222.853f), Vector3(1335.824f, 945.854f, 223.593f), Vector3(1349.999f, 943.194f, 225.853f))))
      ZipLinePaths(new ZipLinePath(206, false, List(Vector3(1327.717f, 750.522f, 214.26f), Vector3(1290.93f, 785.843f, 214.797f), Vector3(1275.783f, 800.387f, 211.95f))))
      ZipLinePaths(new ZipLinePath(207, true, List(Vector3(1328.937f, 1374.829f, 210.55f), Vector3(1315.126f, 1364.014f, 220.18f))))
      ZipLinePaths(new ZipLinePath(208, false, List(Vector3(1333.52f, 1052.305f, 224.259f), Vector3(1336.439f, 1097.932f, 247.538f), Vector3(1339.359f, 1143.56f, 270.128f), Vector3(1342.279f, 1189.187f, 292.717f), Vector3(1348.909f, 1201.159f, 303.208f), Vector3(1343.939f, 1215.132f, 304.058f))))
      ZipLinePaths(new ZipLinePath(209, false, List(Vector3(1334.659f, 1326.499f, 307.604f), Vector3(1285.117f, 1324.503f, 301.896f), Vector3(1235.575f, 1322.508f, 295.435f), Vector3(1221.775f, 1320.568f, 292.845f))))
      ZipLinePaths(new ZipLinePath(210, false, List(Vector3(1338.097f, 1355.399f, 211.077f), Vector3(1314.922f, 1314.426f, 228.464f), Vector3(1310.055f, 1305.822f, 232.957f), Vector3(1305.188f, 1297.218f, 233.177f))))
      ZipLinePaths(new ZipLinePath(211, true, List(Vector3(1341.036f, 1075.189f, 246.36f), Vector3(1324.315f, 1068.659f, 242.637f))))
      ZipLinePaths(new ZipLinePath(212, false, List(Vector3(1342.657f, 1226.81f, 304.003f), Vector3(1356.708f, 1244.273f, 303.168f), Vector3(1370.759f, 1261.736f, 288.939f), Vector3(1398.861f, 1296.661f, 250.126f), Vector3(1420.902f, 1324.054f, 232.964f))))
      ZipLinePaths(new ZipLinePath(213, true, List(Vector3(1347.83f, 1082.554f, 212.85f), Vector3(1347.547f, 1079.261f, 246.351f))))
      ZipLinePaths(new ZipLinePath(214, true, List(Vector3(1348.324f, 957.395f, 226.25f), Vector3(1352.107f, 965.408f, 237.533f))))
      ZipLinePaths(new ZipLinePath(215, false, List(Vector3(1350.175f, 923.469f, 305.829f), Vector3(1372.266f, 933.302f, 302.84f), Vector3(1394.358f, 943.136f, 290.38f), Vector3(1438.541f, 962.803f, 274.208f), Vector3(1451.536f, 968.587f, 266.392f))))
      ZipLinePaths(new ZipLinePath(216, false, List(Vector3(1352.617f, 954.329f, 235.372f), Vector3(1352.705f, 935.466f, 234.815f), Vector3(1326.093f, 908.504f, 218.798f), Vector3(1324.059f, 902.349f, 214.434f), Vector3(1321.125f, 911.095f, 212.614f))))
      ZipLinePaths(new ZipLinePath(217, false, List(Vector3(1359.87f, 1680.928f, 195.69f), Vector3(1357.305f, 1731.828f, 198.145f), Vector3(1355.846f, 1760.771f, 197.43f))))
      ZipLinePaths(new ZipLinePath(218, false, List(Vector3(1360.625f, 990.729f, 235.33f), Vector3(1331.172f, 1032.362f, 236.479f), Vector3(1317.311f, 1051.953f, 234.77f))))
      ZipLinePaths(new ZipLinePath(219, true, List(Vector3(1370.028f, 1325.967f, 294.809f), Vector3(1380.014f, 1337.961f, 302.3f))))
      ZipLinePaths(new ZipLinePath(220, false, List(Vector3(1386.011f, 1481.11f, 259.778f), Vector3(1371.875f, 1500.316f, 256.032f), Vector3(1357.74f, 1519.522f, 242.425f), Vector3(1329.47f, 1557.934f, 224.371f), Vector3(1301.2f, 1596.346f, 206.317f), Vector3(1292.331f, 1608.397f, 196.629f))))
      ZipLinePaths(new ZipLinePath(221, true, List(Vector3(1386.248f, 1033.103f, 216.339f), Vector3(1378.175f, 1038.077f, 221.843f))))
      ZipLinePaths(new ZipLinePath(222, false, List(Vector3(1396.703f, 1336.476f, 232.95f), Vector3(1352.996f, 1312.22f, 235.003f), Vector3(1309.289f, 1287.965f, 236.307f), Vector3(1308.415f, 1287.48f, 233.15f))))
      ZipLinePaths(new ZipLinePath(223, false, List(Vector3(1402.832f, 1790.362f, 238.275f), Vector3(1357.135f, 1784.082f, 219.665f), Vector3(1311.438f, 1777.801f, 200.357f), Vector3(1306.868f, 1777.173f, 196.593f))))
      ZipLinePaths(new ZipLinePath(224, false, List(Vector3(1403.596f, 1321.775f, 307.617f), Vector3(1418.538f, 1275.358f, 297.279f), Vector3(1433.479f, 1228.941f, 286.22f), Vector3(1446.628f, 1188.094f, 272.067f))))
      ZipLinePaths(new ZipLinePath(225, false, List(Vector3(1407.208f, 968.784f, 225.906f), Vector3(1452.954f, 962.509f, 204.954f), Vector3(1487.039f, 957.833f, 185.799f))))
      ZipLinePaths(new ZipLinePath(226, false, List(Vector3(1410.886f, 1015.38f, 224.85f), Vector3(1402.812f, 977.244f, 225.85f))))
      ZipLinePaths(new ZipLinePath(227, false, List(Vector3(1418.968f, 1157.21f, 272.079f), Vector3(1395.204f, 1155.15f, 268.977f), Vector3(1371.44f, 1153.09f, 261.808f), Vector3(1323.912f, 1148.971f, 243.929f), Vector3(1299.898f, 1147.293f, 230.218f), Vector3(1291.285f, 1147.316f, 227.081f))))
      ZipLinePaths(new ZipLinePath(228, false, List(Vector3(1425.18f, 1789.32f, 206.882f), Vector3(1375.333f, 1789.595f, 203.687f), Vector3(1325.486f, 1789.87f, 199.742f), Vector3(1296.574f, 1790.029f, 196.532f))))
      ZipLinePaths(new ZipLinePath(229, false, List(Vector3(1445.087f, 1032.407f, 213.083f), Vector3(1464.295f, 1018.75f, 210.633f), Vector3(1483.503f, 1005.094f, 197.115f), Vector3(1505.784f, 989.253f, 185.682f))))
      ZipLinePaths(new ZipLinePath(230, false, List(Vector3(1447.744f, 1139.43f, 272.051f), Vector3(1459.359f, 1090.834f, 270.834f), Vector3(1470.974f, 1042.238f, 268.876f), Vector3(1472.136f, 1037.378f, 266.361f))))
      ZipLinePaths(new ZipLinePath(231, false, List(Vector3(1447.873f, 1015.423f, 261.7f), Vector3(1399.228f, 1023.656f, 254.346f), Vector3(1350.583f, 1031.89f, 246.214f), Vector3(1317.738f, 1038.824f, 238.282f), Vector3(1310.909f, 1040.47f, 234.779f))))
      ZipLinePaths(new ZipLinePath(232, false, List(Vector3(1454.058f, 1332.984f, 232.951f), Vector3(1499.766f, 1355.128f, 238.278f), Vector3(1513.21f, 1361.641f, 238.042f))))
      ZipLinePaths(new ZipLinePath(233, false, List(Vector3(1454.086f, 1316.012f, 195.46f), Vector3(1492.612f, 1341.129f, 195.64f))))
      ZipLinePaths(new ZipLinePath(234, true, List(Vector3(1484.619f, 975.574f, 196.717f), Vector3(1486.5f, 1019.848f, 265.86f))))
      ZipLinePaths(new ZipLinePath(235, false, List(Vector3(1495.506f, 1365.02f, 198.175f), Vector3(1460.612f, 1394.349f, 221.756f), Vector3(1425.718f, 1423.677f, 244.627f), Vector3(1410.323f, 1436.616f, 262.064f), Vector3(1394.928f, 1449.556f, 265.053f))))
      ZipLinePaths(new ZipLinePath(236, false, List(Vector3(1508.012f, 1037.494f, 262.404f), Vector3(1484.492f, 1081.185f, 251.434f), Vector3(1460.972f, 1124.876f, 239.663f), Vector3(1437.452f, 1168.567f, 227.892f), Vector3(1413.931f, 1212.258f, 216.121f), Vector3(1410.703f, 1218.255f, 212.973f))))
      ZipLinePaths(new ZipLinePath(237, false, List(Vector3(1511.057f, 1376.957f, 238.038f), Vector3(1487.839f, 1382.544f, 233.932f), Vector3(1464.621f, 1388.132f, 225.158f), Vector3(1418.186f, 1399.308f, 209.16f), Vector3(1387.537f, 1408.048f, 194.108f))))
      ZipLinePaths(new ZipLinePath(238, false, List(Vector3(1533.004f, 1221.827f, 201.162f), Vector3(1487.001f, 1202.784f, 206.58f), Vector3(1440.999f, 1183.741f, 211.247f), Vector3(1415.238f, 1173.077f, 212.962f))))
      ZipLinePaths(new ZipLinePath(239, true, List(Vector3(1541.029f, 1002.385f, 185.132f), Vector3(1539.744f, 1007.686f, 217.352f))))
      ZipLinePaths(new ZipLinePath(240, false, List(Vector3(1544.376f, 1012.95f, 217.861f), Vector3(1544.619f, 1063.813f, 214.894f), Vector3(1544.862f, 1114.676f, 211.185f), Vector3(1545.105f, 1165.539f, 207.476f), Vector3(1545.319f, 1210.418f, 202.922f))))
      ZipLinePaths(new ZipLinePath(241, false, List(Vector3(1576.949f, 990.322f, 223.031f), Vector3(1530.215f, 972.799f, 220.748f), Vector3(1483.48f, 955.276f, 217.726f), Vector3(1436.745f, 937.753f, 214.704f), Vector3(1434.876f, 937.052f, 212.651f))))
      ZipLinePaths(new ZipLinePath(242, true, List(Vector3(1289.844f, 1078.568f, 216.85f), Vector3(1289.422f, 1086.704f, 234.276f))))
      ZipLinePaths(new ZipLinePath(243, true, List(Vector3(1295.275f, 1006.474f, 234.276f), Vector3(1295.289f, 1014.841f, 216.85f))))
      ZipLinePaths(new ZipLinePath(244, true, List(Vector3(889.903f, 1392.847f, 216.85f), Vector3(891.002f, 1383.453f, 234.276f))))
      ZipLinePaths(new ZipLinePath(245, true, List(Vector3(899.389f, 1462.873f, 234.276f), Vector3(899.115f, 1455.859f, 216.85f))))
      ZipLinePaths(new ZipLinePath(246, true, List(Vector3(1206.547f, 1338.16f, 292.333f), Vector3(1207.199f, 1337.626f, 297.84f))))
      ZipLinePaths(new ZipLinePath(247, false, List(Vector3(1320.229f, 668.971f, 220.041f), Vector3(1297.599f, 714.589f, 218.575f), Vector3(1274.969f, 760.207f, 215.169f), Vector3(1255.445f, 799.563f, 211.951f))))
      ZipLinePaths(new ZipLinePath(248, false, List(Vector3(1356.604f, 911.362f, 300.275f), Vector3(1379.938f, 917.926f, 296.868f), Vector3(1403.271f, 924.489f, 285.181f), Vector3(1449.938f, 937.616f, 269.351f), Vector3(1496.605f, 950.742f, 253.521f), Vector3(1543.272f, 963.869f, 237.692f), Vector3(1582.619f, 974.937f, 223.027f))))
      ZipLinePaths(new ZipLinePath(249, true, List(Vector3(1082.643f, 1290.453f, 214.55f), Vector3(1077.063f, 1283.506f, 219.55f))))
      ZipLinePaths(new ZipLinePath(250, false, List(Vector3(871.498f, 436.029f, 198.902f), Vector3(873.737f, 485.704f, 210.959f), Vector3(877.66f, 524.343f, 218.903f))))
      ZipLinePaths(new ZipLinePath(251, true, List(Vector3(1212.524f, 1596.389f, 194.899f), Vector3(1216.017f, 1580.147f, 212.18f))))
      ZipLinePaths(new ZipLinePath(252, false, List(Vector3(764.967f, 771.161f, 249.219f), Vector3(766.426f, 773.295f, 249.101f), Vector3(807.076f, 832.744f, 194.612f))))
      ZipLinePaths(new ZipLinePath(253, false, List(Vector3(676.271f, 684.934f, 196.79f), Vector3(682.995f, 735.485f, 197.148f), Vector3(689.719f, 786.037f, 196.758f), Vector3(696.442f, 836.589f, 196.367f), Vector3(698.024f, 848.484f, 195.52f))))
      ZipLinePaths(new ZipLinePath(254, true, List(Vector3(817.187f, 767.054f, 185.791f), Vector3(816.774f, 767.452f, 217.351f))))
      ZipLinePaths(new ZipLinePath(255, false, List(Vector3(806.255f, 770.376f, 217.869f), Vector3(783.751f, 777.979f, 213.672f), Vector3(761.247f, 785.583f, 203.003f), Vector3(745.045f, 791.057f, 196.219f))))
      ZipLinePaths(new ZipLinePath(256, false, List(Vector3(815.529f, 778.081f, 217.866f), Vector3(828.261f, 826.796f, 210.506f), Vector3(840.992f, 875.511f, 202.4f), Vector3(846.983f, 898.436f, 197.557f))))
      ZipLinePaths(new ZipLinePath(257, false, List(Vector3(819.788f, 763.677f, 217.895f), Vector3(850.532f, 726.309f, 240.426f), Vector3(855.093f, 723.569f, 241.036f))))
      ZipLinePaths(new ZipLinePath(258, false, List(Vector3(905.596f, 706.507f, 241.162f), Vector3(956.466f, 704.68f, 238.794f), Vector3(1007.335f, 702.852f, 233.081f))))
      ZipLinePaths(new ZipLinePath(259, false, List(Vector3(704.784f, 875.597f, 201.11f), Vector3(717.929f, 896.92f, 201.064f), Vector3(731.574f, 918.444f, 200.122f), Vector3(758.864f, 961.49f, 198.395f), Vector3(761.539f, 965.711f, 197.45f))))
      ZipLinePaths(new ZipLinePath(260, false, List(Vector3(893.999f, 456.076f, 196.941f), Vector3(904.293f, 505.965f, 196.623f), Vector3(914.587f, 555.853f, 195.773f), Vector3(918.724f, 575.907f, 192.721f), Vector3(922.862f, 595.96f, 189.971f))))
      ZipLinePaths(new ZipLinePath(261, true, List(Vector3(795.919f, 940.689f, 196.595f), Vector3(830.961f, 955.602f, 210.15f))))
      ZipLinePaths(new ZipLinePath(262, false, List(Vector3(647.03f, 933.05f, 195.93f), Vector3(661.956f, 981.799f, 197.893f), Vector3(665.175f, 992.314f, 197.45f))))
      ZipLinePaths(new ZipLinePath(263, true, List(Vector3(690.613f, 1259.99f, 196.173f), Vector3(706.118f, 1244.747f, 211.35f))))
      ZipLinePaths(new ZipLinePath(264, true, List(Vector3(637.748f, 1318.521f, 196.587f), Vector3(659.951f, 1326.051f, 211.992f))))
      ZipLinePaths(new ZipLinePath(265, false, List(Vector3(706.085f, 1366.379f, 212.49f), Vector3(715.565f, 1385.971f, 213.872f), Vector3(725.045f, 1405.564f, 213.05f))))
      ZipLinePaths(new ZipLinePath(266, true, List(Vector3(1293.009f, 1407.06f, 229.539f), Vector3(1290.49f, 1414.493f, 259.321f))))
      ZipLinePaths(new ZipLinePath(267, true, List(Vector3(1260.779f, 1465.584f, 227.039f), Vector3(1273.877f, 1457.67f, 254.54f))))
      ZipLinePaths(new ZipLinePath(268, true, List(Vector3(900.574f, 1093.87f, 213.939f), Vector3(888.281f, 1107.401f, 233.19f))))
      ZipLinePaths(new ZipLinePath(269, true, List(Vector3(933.058f, 1035.019f, 216.439f), Vector3(929.539f, 1041.274f, 246.221f))))
      ZipLinePaths(new ZipLinePath(270, true, List(Vector3(816.289f, 2213.624f, 242.635f), Vector3(821.393f, 2071.116f, 202.635f))))
      ZipLinePaths(new ZipLinePath(271, true, List(Vector3(844.297f, 2026.099f, 202.635f), Vector3(973.751f, 2000.333f, 201.989f))))
      ZipLinePaths(new ZipLinePath(272, false, List(Vector3(905.182f, 628.614f, 185.95f), Vector3(885.241f, 675.551f, 187.87f), Vector3(866.865f, 718.808f, 186.29f))))
      ZipLinePaths(new ZipLinePath(273, false, List(Vector3(809.87f, 889.825f, 186f), Vector3(769.309f, 860.583f, 187.897f), Vector3(736.049f, 836.604f, 186.28f))))
      ZipLinePaths(new ZipLinePath(274, false, List(Vector3(848.913f, 608.999f, 218.84f), Vector3(861.032f, 560.488f, 219.485f), Vector3(863.613f, 549.756f, 218.417f))))
      ZipLinePaths(new ZipLinePath(275, false, List(Vector3(854.5f, 709.362f, 241.026f), Vector3(814.796f, 688.125f, 219.964f), Vector3(775.887f, 667.312f, 197.944f))))
      ZipLinePaths(new ZipLinePath(276, false, List(Vector3(827.296f, 970.647f, 219.293f), Vector3(813.585f, 932.726f, 254.124f), Vector3(811.665f, 927.417f, 253.938f))))
      ZipLinePaths(new ZipLinePath(277, false, List(Vector3(690.329f, 984.723f, 231.851f), Vector3(704.931f, 936.036f, 236.752f), Vector3(719.533f, 887.349f, 240.911f), Vector3(734.135f, 838.662f, 245.069f), Vector3(748.737f, 789.974f, 249.228f), Vector3(754.177f, 771.836f, 249.222f))))
      ZipLinePaths(new ZipLinePath(278, false, List(Vector3(875.762f, 1394.878f, 234.786f), Vector3(867.423f, 1372.115f, 232.776f), Vector3(859.084f, 1349.352f, 223.285f), Vector3(845.409f, 1312.021f, 212.116f))))
      ZipLinePaths(new ZipLinePath(279, false, List(Vector3(961.517f, 1415.007f, 247.343f), Vector3(979.775f, 1398.183f, 248.537f), Vector3(998.034f, 1381.359f, 242.23f), Vector3(1034.551f, 1347.71f, 236.358f), Vector3(1071.069f, 1314.061f, 230.485f), Vector3(1085.445f, 1301.175f, 227.303f))))
      ZipLinePaths(new ZipLinePath(280, false, List(Vector3(1228.337f, 1309.135f, 251.995f), Vector3(1265.385f, 1276.134f, 246.542f), Vector3(1283.909f, 1259.633f, 242.624f))))
      ZipLinePaths(new ZipLinePath(281, true, List(Vector3(860.587f, 1479.59f, 214.706f), Vector3(868.409f, 1478.511f, 224.479f))))
      ZipLinePaths(new ZipLinePath(282, true, List(Vector3(1274.085f, 1129.327f, 216.506f), Vector3(1301.615f, 1140.312f, 226.271f))))
      ZipLinePaths(new ZipLinePath(283, false, List(Vector3(1016.344f, 669.706f, 227.588f), Vector3(1016.653f, 621.568f, 212.295f), Vector3(1016.85f, 590.759f, 207.982f), Vector3(1017.164f, 541.658f, 193.497f), Vector3(1017.349f, 512.775f, 183.271f))))
      ZipLinePaths(new ZipLinePath(284, false, List(Vector3(1095.865f, 472.467f, 168.211f), Vector3(1096.286f, 460.001f, 170.523f), Vector3(1096.707f, 447.535f, 172.931f), Vector3(1097.314f, 429.584f, 168.664f), Vector3(1097.92f, 411.632f, 163.901f))))
      ZipLinePaths(new ZipLinePath(285, false, List(Vector3(951.858f, 387.939f, 188.212f), Vector3(951.96f, 381.835f, 187.721f), Vector3(952.062f, 375.731f, 186.032f), Vector3(952.266f, 363.522f, 181.666f), Vector3(952.469f, 351.314f, 178.5f), Vector3(952.673f, 339.105f, 174.333f), Vector3(952.877f, 326.897f, 170.356f), Vector3(953.08f, 314.688f, 166.58f), Vector3(953.284f, 302.48f, 164.053f), Vector3(953.488f, 290.271f, 163.926f), Vector3(953.732f, 275.621f, 163.452f))))
      ZipLinePaths(new ZipLinePath(286, false, List(Vector3(837.714f, 264.55f, 163.487f), Vector3(838.193f, 255.85f, 163.337f), Vector3(838.3f, 253.916f, 162.774f), Vector3(839.045f, 240.382f, 158.337f), Vector3(839.79f, 226.849f, 153.417f))))
      ZipLinePaths(new ZipLinePath(287, false, List(Vector3(884.876f, 213.314f, 143.23f), Vector3(887.973f, 162.408f, 143.946f), Vector3(888.216f, 158.416f, 143.28f))))
      ZipLinePaths(new ZipLinePath(288, false, List(Vector3(776.348f, 254.115f, 153.197f), Vector3(776.224f, 266.482f, 153.979f), Vector3(776.1f, 278.848f, 155.562f), Vector3(775.977f, 291.214f, 159.152f), Vector3(775.853f, 303.581f, 164.142f), Vector3(775.61f, 327.828f, 170.323f), Vector3(775.489f, 339.952f, 174.214f), Vector3(775.428f, 346.014f, 176.759f), Vector3(775.368f, 352.076f, 178.215f))))
      ZipLinePaths(new ZipLinePath(289, false, List(Vector3(760.354f, 385.699f, 183.342f), Vector3(759.79f, 410.144f, 191.685f), Vector3(759.509f, 422.366f, 195.616f), Vector3(759.227f, 434.588f, 198.548f), Vector3(758.962f, 446.092f, 202.623f), Vector3(758.829f, 451.843f, 203.263f))))
      ZipLinePaths(new ZipLinePath(290, false, List(Vector3(711.762f, 461.764f, 213.168f), Vector3(713.259f, 480.738f, 218.568f), Vector3(712.907f, 490.925f, 221.321f), Vector3(711.931f, 495.469f, 223.197f), Vector3(712.755f, 499.612f, 223.874f), Vector3(717.35f, 503.016f, 223.69f), Vector3(755.945f, 503.42f, 223.257f))))
      ZipLinePaths(new ZipLinePath(291, false, List(Vector3(1281.979f, 376.763f, 183.18f), Vector3(1316.967f, 413.866f, 183.982f), Vector3(1323.827f, 421.141f, 183.24f))))
      ZipLinePaths(new ZipLinePath(292, false, List(Vector3(1493.118f, 393.605f, 163.24f), Vector3(1494.583f, 403.371f, 162.907f), Vector3(1496.048f, 413.138f, 159.87f), Vector3(1495.979f, 425.872f, 155.056f), Vector3(1495.84f, 451.339f, 154.929f), Vector3(1495.457f, 470.964f, 156.642f), Vector3(1495.465f, 480.976f, 158.248f), Vector3(1494.373f, 490.588f, 154.855f), Vector3(1495.206f, 529.838f, 143.18f), Vector3(1495.623f, 549.462f, 142.818f), Vector3(1496.039f, 569.087f, 148.156f), Vector3(1496.456f, 588.711f, 146.344f), Vector3(1496.872f, 608.336f, 143.831f), Vector3(1485.404f, 616.733f, 143.934f), Vector3(1479.343f, 630.506f, 146.634f), Vector3(1477.886f, 647.664f, 153.035f), Vector3(1479.029f, 659.223f, 157.535f), Vector3(1479.715f, 685.339f, 163.136f), Vector3(1481.462f, 690.033f, 163.2f))))
      ZipLinePaths(new ZipLinePath(293, false, List(Vector3(1488.859f, 696.058f, 163.275f), Vector3(1518.973f, 696.266f, 172.9f), Vector3(1534.516f, 696.374f, 177.82f), Vector3(1551.031f, 696.488f, 182.996f), Vector3(1569.488f, 696.616f, 183.205f))))
      ZipLinePaths(new ZipLinePath(294, false, List(Vector3(1598.739f, 695.95f, 183.191f), Vector3(1647.716f, 694.45f, 198.065f), Vector3(1649.637f, 694.391f, 198.21f))))
      ZipLinePaths(new ZipLinePath(295, false, List(Vector3(1686.968f, 669.616f, 208.213f), Vector3(1686.975f, 675.962f, 207.406f), Vector3(1686.982f, 682.309f, 203.803f), Vector3(1686.989f, 688.656f, 203.35f), Vector3(1686.996f, 695.002f, 200.298f), Vector3(1687.01f, 707.695f, 198.993f), Vector3(1687.024f, 720.389f, 199.488f), Vector3(1687.033f, 728.602f, 199.286f), Vector3(1687.042f, 736.815f, 199.685f), Vector3(1687.06f, 753.242f, 202.782f), Vector3(1687.074f, 766.184f, 198.814f), Vector3(1687.089f, 779.126f, 195.346f), Vector3(1687.103f, 792.068f, 190.679f), Vector3(1687.117f, 805.01f, 187.411f), Vector3(1687.131f, 817.703f, 186.067f), Vector3(1687.145f, 830.396f, 188.723f), Vector3(1687.159f, 843.089f, 192.829f), Vector3(1687.173f, 855.783f, 192.635f), Vector3(1687.201f, 880.671f, 188.202f))))
      ZipLinePaths(new ZipLinePath(296, false, List(Vector3(1688.037f, 913.673f, 188.342f), Vector3(1688.198f, 936.255f, 196.256f), Vector3(1688.358f, 958.837f, 203.292f), Vector3(1688.392f, 963.641f, 203.132f))))
      ZipLinePaths(new ZipLinePath(297, false, List(Vector3(1719.051f, 955.831f, 203.191f), Vector3(1719.054f, 966.319f, 206.35f), Vector3(1719.055f, 971.562f, 206.579f), Vector3(1719.057f, 976.806f, 208.009f), Vector3(1719.058f, 983.299f, 206.171f), Vector3(1719.06f, 989.791f, 203.433f), Vector3(1719.063f, 1002.776f, 200.158f), Vector3(1719.066f, 1015.761f, 195.282f), Vector3(1719.069f, 1028.746f, 191.407f), Vector3(1719.072f, 1041.48f, 188.344f), Vector3(1719.075f, 1054.215f, 183.98f), Vector3(1719.078f, 1066.949f, 179.367f), Vector3(1719.082f, 1079.684f, 179.353f), Vector3(1719.088f, 1105.152f, 178.427f), Vector3(1719.094f, 1130.621f, 186.5f), Vector3(1719.097f, 1142.107f, 192.315f), Vector3(1719.1f, 1153.593f, 193.43f))))
      ZipLinePaths(new ZipLinePath(298, false, List(Vector3(1656.032f, 1221.278f, 193.691f), Vector3(1655.949f, 1231.25f, 198.116f), Vector3(1655.816f, 1247.205f, 203.113f), Vector3(1655.675f, 1264.157f, 199.603f), Vector3(1655.534f, 1281.109f, 198.191f))))
      ZipLinePaths(new ZipLinePath(299, false, List(Vector3(1656.205f, 1310.291f, 198.211f), Vector3(1655.728f, 1334.811f, 204.791f), Vector3(1655.252f, 1359.33f, 212.888f), Vector3(1655.215f, 1361.253f, 213.212f))))
      ZipLinePaths(new ZipLinePath(300, false, List(Vector3(1633.716f, 1367.62f, 213.197f), Vector3(1617.236f, 1367.789f, 218.096f), Vector3(1608.997f, 1367.873f, 220.706f), Vector3(1600.757f, 1367.957f, 222.815f), Vector3(1576.039f, 1368.209f, 227.236f), Vector3(1563.679f, 1368.335f, 230.446f), Vector3(1551.32f, 1368.461f, 234.257f), Vector3(1535.81f, 1368.619f, 238.516f))))
      ZipLinePaths(new ZipLinePath(301, false, List(Vector3(1672.725f, 1446.715f, 198.892f), Vector3(1672.61f, 1454.546f, 202.811f), Vector3(1672.489f, 1462.867f, 205.662f), Vector3(1672.368f, 1471.187f, 207.912f), Vector3(1671.996f, 1496.638f, 209.314f), Vector3(1671.811f, 1509.364f, 211.814f), Vector3(1671.625f, 1522.089f, 214.915f), Vector3(1671.511f, 1529.92f, 215.701f), Vector3(1671.454f, 1533.836f, 217.794f), Vector3(1671.396f, 1537.751f, 218.201f))))
      ZipLinePaths(new ZipLinePath(302, false, List(Vector3(1640.047f, 1548.61f, 223.215f), Vector3(1640.505f, 1568.514f, 228.292f), Vector3(1640.734f, 1578.467f, 231.29f), Vector3(1640.849f, 1583.443f, 232.789f), Vector3(1640.963f, 1588.419f, 233.205f))))
      ZipLinePaths(new ZipLinePath(303, false, List(Vector3(1655.87f, 1606.72f, 233.9f), Vector3(1655.775f, 1612.969f, 237.636f), Vector3(1655.68f, 1619.217f, 238.471f), Vector3(1655.586f, 1625.466f, 241.005f), Vector3(1655.538f, 1628.59f, 242.473f), Vector3(1655.491f, 1631.714f, 242.94f), Vector3(1654.961f, 1666.706f, 233.2f))))
      ZipLinePaths(new ZipLinePath(304, false, List(Vector3(1719.529f, 1570.549f, 223.18f), Vector3(1719.582f, 1564.423f, 222.69f), Vector3(1719.635f, 1558.297f, 220.107f), Vector3(1719.742f, 1546.046f, 216.741f), Vector3(1719.796f, 1539.92f, 215.008f), Vector3(1719.849f, 1533.795f, 213.375f), Vector3(1719.902f, 1527.669f, 210.492f), Vector3(1719.956f, 1521.543f, 209.509f), Vector3(1720.067f, 1508.802f, 209.088f), Vector3(1720.178f, 1496.061f, 208.668f), Vector3(1720.289f, 1483.319f, 208.948f), Vector3(1720.345f, 1476.949f, 212.087f), Vector3(1720.4f, 1470.578f, 213.127f), Vector3(1720.618f, 1445.585f, 204.617f), Vector3(1720.727f, 1433.089f, 201.362f), Vector3(1720.836f, 1420.593f, 197.307f), Vector3(1721.037f, 1397.56f, 189.401f), Vector3(1720.537f, 1386.044f, 186.448f), Vector3(1721.238f, 1374.528f, 183.2f))))
      ZipLinePaths(new ZipLinePath(305, false, List(Vector3(1730.236f, 1367.877f, 183.382f), Vector3(1738.146f, 1367.813f, 186.517f), Vector3(1746.057f, 1367.749f, 188.776f), Vector3(1753.968f, 1367.684f, 191.935f), Vector3(1757.923f, 1367.652f, 192.915f), Vector3(1761.878f, 1367.62f, 193.213f))))
      ZipLinePaths(new ZipLinePath(306, false, List(Vector3(1016.652f, 498.995f, 183.171f), Vector3(1016.125f, 474.041f, 183.533f), Vector3(1015.597f, 449.088f, 184.801f), Vector3(1015.412f, 440.354f, 185.924f), Vector3(1015.319f, 435.987f, 186.986f), Vector3(1015.227f, 431.62f, 188.148f), Vector3(1015.042f, 422.887f, 188.222f), Vector3(1014.857f, 414.153f, 188.201f))))
      ZipLinePaths(new ZipLinePath(307, false, List(Vector3(1008.883f, 408.224f, 188.2f), Vector3(1000.386f, 407.959f, 190.022f), Vector3(996.138f, 407.826f, 192.386f), Vector3(991.89f, 407.693f, 192.949f), Vector3(983.394f, 407.428f, 189.721f), Vector3(974.897f, 407.163f, 188.19f))))
      ZipLinePaths(new ZipLinePath(308, false, List(Vector3(820.337f, 246.805f, 153.19f), Vector3(778.421f, 249.491f, 153.23f))))
      ZipLinePaths(new ZipLinePath(309, false, List(Vector3(602.831f, 599.918f, 183.211f), Vector3(583.872f, 600.199f, 183.368f), Vector3(564.912f, 600.48f, 183.224f), Vector3(548.947f, 600.716f, 183.228f), Vector3(543.957f, 600.79f, 183.153f), Vector3(534.977f, 600.923f, 179.827f), Vector3(525.996f, 601.056f, 178.201f))))
      ZipLinePaths(new ZipLinePath(310, false, List(Vector3(519.788f, 612.558f, 178.21f), Vector3(519.461f, 663.555f, 178.948f), Vector3(519.192f, 705.553f, 178.19f))))
      ZipLinePaths(new ZipLinePath(311, false, List(Vector3(472.031f, 737.508f, 183.333f), Vector3(471.882f, 749.709f, 187.618f), Vector3(471.732f, 761.909f, 191.228f), Vector3(471.583f, 774.11f, 194.637f), Vector3(471.508f, 780.21f, 197.142f), Vector3(471.433f, 786.31f, 199.247f), Vector3(471.337f, 794.205f, 201.757f), Vector3(471.24f, 802.099f, 203.866f), Vector3(471.143f, 809.994f, 206.576f), Vector3(471.095f, 813.941f, 207.731f), Vector3(471.047f, 817.888f, 208.213f))))
      ZipLinePaths(new ZipLinePath(312, false, List(Vector3(425.006f, 836.2f, 203.19f), Vector3(423.432f, 799.231f, 203.17f))))
      ZipLinePaths(new ZipLinePath(313, false, List(Vector3(403.332f, 807.446f, 203.211f), Vector3(391.598f, 808.338f, 205.995f), Vector3(385.732f, 808.784f, 207.642f), Vector3(379.865f, 809.23f, 208.2f))))
      ZipLinePaths(new ZipLinePath(314, false, List(Vector3(375.366f, 818.172f, 208.35f), Vector3(375.41f, 825.167f, 210.7f), Vector3(375.432f, 828.665f, 212.023f), Vector3(375.453f, 832.163f, 212.946f), Vector3(375.534f, 845.155f, 209.365f), Vector3(375.575f, 851.651f, 207.075f), Vector3(375.595f, 854.899f, 205.429f), Vector3(375.615f, 858.147f, 204.184f), Vector3(375.696f, 871.139f, 201.003f), Vector3(375.737f, 877.635f, 198.812f), Vector3(375.777f, 884.131f, 196.821f), Vector3(375.857f, 896.874f, 193.848f), Vector3(375.896f, 903.245f, 194.162f), Vector3(375.936f, 909.616f, 197.576f), Vector3(376.015f, 922.358f, 200.253f), Vector3(376.055f, 928.729f, 203.591f), Vector3(376.095f, 935.1f, 200.33f), Vector3(376.174f, 947.842f, 199.057f), Vector3(376.253f, 960.585f, 203.184f), Vector3(376.293f, 966.956f, 200.597f), Vector3(376.333f, 973.327f, 198.811f), Vector3(376.372f, 979.698f, 196.824f), Vector3(376.412f, 986.069f, 194.438f), Vector3(376.571f, 1011.554f, 188.192f), Vector3(376.65f, 1024.296f, 183.869f), Vector3(376.69f, 1030.667f, 184.707f), Vector3(376.73f, 1037.038f, 187.346f), Vector3(376.809f, 1049.78f, 190.673f), Vector3(376.849f, 1056.151f, 192.937f), Vector3(376.888f, 1062.522f, 191f), Vector3(376.968f, 1075.265f, 188.627f), Vector3(377.007f, 1081.636f, 188.441f), Vector3(377.047f, 1088.007f, 189.654f), Vector3(377.102f, 1096.751f, 189.666f), Vector3(377.156f, 1105.496f, 193.877f), Vector3(377.211f, 1114.241f, 196.188f), Vector3(377.238f, 1118.613f, 197.944f), Vector3(377.265f, 1122.985f, 198.2f))))
      ZipLinePaths(new ZipLinePath(315, false, List(Vector3(455.425f, 1085.946f, 183.201f), Vector3(455.374f, 1103.412f, 183.805f), Vector3(455.348f, 1112.145f, 184.956f), Vector3(455.335f, 1116.511f, 187.332f), Vector3(455.322f, 1120.878f, 187.907f), Vector3(455.303f, 1127.365f, 185.035f), Vector3(455.283f, 1133.852f, 183.163f), Vector3(455.264f, 1140.339f, 181.641f), Vector3(455.245f, 1146.827f, 179.519f), Vector3(455.206f, 1159.801f, 175.424f), Vector3(455.187f, 1166.289f, 173.577f), Vector3(455.178f, 1169.532f, 172.454f), Vector3(455.168f, 1172.776f, 170.93f), Vector3(455.13f, 1185.501f, 169.651f), Vector3(455.092f, 1198.226f, 172.572f), Vector3(455.055f, 1210.951f, 175.993f), Vector3(455.036f, 1217.314f, 178.504f), Vector3(455.017f, 1223.676f, 175.614f), Vector3(454.981f, 1235.653f, 173.357f), Vector3(454.946f, 1247.63f, 173.201f))))
      ZipLinePaths(new ZipLinePath(316, false, List(Vector3(455.962f, 1404.88f, 173.49f), Vector3(456.291f, 1455.873f, 174.03f), Vector3(456.406f, 1473.871f, 173.19f))))
      ZipLinePaths(new ZipLinePath(317, false, List(Vector3(464.578f, 1481.471f, 173.26f), Vector3(476.106f, 1480.883f, 176.977f), Vector3(487.635f, 1480.296f, 179.811f), Vector3(499.164f, 1479.708f, 184.144f), Vector3(504.928f, 1479.415f, 185.911f), Vector3(510.692f, 1479.121f, 187.478f), Vector3(515.496f, 1478.876f, 188.22f))))
      ZipLinePaths(new ZipLinePath(318, false, List(Vector3(535.622f, 1469.87f, 188.21f), Vector3(536.963f, 1506.843f, 188.2f))))
      ZipLinePaths(new ZipLinePath(319, false, List(Vector3(552.012f, 1520.061f, 188.233f), Vector3(551.63f, 1532.271f, 191.871f), Vector3(551.247f, 1544.48f, 195.432f), Vector3(550.864f, 1556.69f, 199.294f), Vector3(550.481f, 1568.9f, 203.555f), Vector3(549.955f, 1585.659f, 208.167f), Vector3(549.693f, 1594.038f, 210.873f), Vector3(549.561f, 1598.228f, 212.426f), Vector3(549.43f, 1602.418f, 213.302f))))
      ZipLinePaths(new ZipLinePath(320, false, List(Vector3(526.961f, 1655.74f, 218.191f), Vector3(535.944f, 1655.735f, 220.4f), Vector3(540.436f, 1655.733f, 222.153f), Vector3(544.928f, 1655.731f, 222.706f), Vector3(557.904f, 1655.724f, 218.829f), Vector3(564.392f, 1655.721f, 216.49f), Vector3(570.88f, 1655.718f, 213.951f), Vector3(577.368f, 1655.715f, 212.562f), Vector3(583.856f, 1655.712f, 209.773f), Vector3(590.344f, 1655.708f, 208.685f), Vector3(596.833f, 1655.705f, 206.196f), Vector3(609.559f, 1655.699f, 203.883f), Vector3(622.286f, 1655.693f, 207.571f), Vector3(635.013f, 1655.687f, 210.658f), Vector3(641.376f, 1655.683f, 213.602f), Vector3(647.739f, 1655.68f, 210.145f), Vector3(660.466f, 1655.674f, 209.683f), Vector3(673.193f, 1655.668f, 213.02f), Vector3(685.919f, 1655.662f, 208.857f), Vector3(692.282f, 1655.659f, 206.376f), Vector3(698.646f, 1655.656f, 204.295f), Vector3(724.099f, 1655.643f, 197.37f), Vector3(736.826f, 1655.637f, 192.907f), Vector3(743.189f, 1655.634f, 194.476f), Vector3(746.371f, 1655.632f, 195.46f), Vector3(749.552f, 1655.631f, 197.444f), Vector3(760.033f, 1655.625f, 201.298f), Vector3(765.274f, 1655.623f, 202.825f), Vector3(770.514f, 1655.62f, 203.251f))))
      ZipLinePaths(new ZipLinePath(321, false, List(Vector3(775.514f, 1789.676f, 203.21f), Vector3(773.658f, 1826.626f, 203.28f))))
      ZipLinePaths(new ZipLinePath(322, false, List(Vector3(762.949f, 1831.455f, 203.699f), Vector3(756.071f, 1831.531f, 207.345f), Vector3(749.193f, 1831.606f, 208.523f), Vector3(742.315f, 1831.681f, 211.4f), Vector3(735.437f, 1831.756f, 213.21f))))
      ZipLinePaths(new ZipLinePath(323, false, List(Vector3(728.262f, 1843.892f, 213.507f), Vector3(728.366f, 1851.282f, 216.603f), Vector3(728.47f, 1858.671f, 219.027f), Vector3(728.574f, 1866.061f, 221.652f), Vector3(728.626f, 1869.755f, 222.564f), Vector3(728.678f, 1873.45f, 223.206f))))
      ZipLinePaths(new ZipLinePath(324, false, List(Vector3(744.063f, 1895.348f, 224.013f), Vector3(744.108f, 1899.326f, 226.214f), Vector3(744.152f, 1903.304f, 228.118f), Vector3(744.332f, 1919.216f, 232.914f), Vector3(744.416f, 1926.674f, 230.973f), Vector3(744.501f, 1934.133f, 228.333f))))
      ZipLinePaths(new ZipLinePath(325, false, List(Vector3(759.746f, 1953.04f, 228.29f), Vector3(759.606f, 1959.903f, 230.621f), Vector3(759.466f, 1966.767f, 233.066f), Vector3(759.145f, 1982.454f, 237.818f), Vector3(758.765f, 2001.083f, 238.211f))))
      ZipLinePaths(new ZipLinePath(326, false, List(Vector3(752.948f, 2008.381f, 238.187f), Vector3(748.352f, 2008.585f, 237.325f), Vector3(743.756f, 2008.789f, 235.782f), Vector3(734.565f, 2009.198f, 232.696f), Vector3(716.182f, 2010.014f, 228.238f))))
      ZipLinePaths(new ZipLinePath(327, false, List(Vector3(712.171f, 2021.266f, 228.702f), Vector3(712.396f, 2027.728f, 231.262f), Vector3(712.622f, 2034.19f, 233.942f), Vector3(713.072f, 2047.113f, 237.902f), Vector3(713.606f, 2062.429f, 242.555f), Vector3(713.807f, 2068.173f, 243.244f))))
      ZipLinePaths(new ZipLinePath(328, false, List(Vector3(728.179f, 2083.034f, 243.41f), Vector3(728.087f, 2090.282f, 246.073f), Vector3(727.996f, 2097.53f, 248.433f), Vector3(727.904f, 2104.778f, 251.193f), Vector3(727.813f, 2112.027f, 252.954f), Vector3(727.629f, 2126.523f, 248.528f), Vector3(727.446f, 2141.02f, 243.4f))))
      ZipLinePaths(new ZipLinePath(329, false, List(Vector3(779.821f, 1928.877f, 228.19f), Vector3(797.77f, 1928.144f, 232.516f), Vector3(806.745f, 1927.778f, 234.128f), Vector3(815.719f, 1927.411f, 237.94f), Vector3(830.718f, 1927.263f, 233.5f), Vector3(845.717f, 1927.115f, 228.36f))))
      ZipLinePaths(new ZipLinePath(330, false, List(Vector3(862.363f, 1910.578f, 228.233f), Vector3(868.217f, 1910.713f, 227.056f), Vector3(874.07f, 1910.849f, 224.805f), Vector3(885.776f, 1911.119f, 221.102f), Vector3(897.483f, 1911.39f, 217.6f), Vector3(909.189f, 1911.661f, 213.425f))))
      ZipLinePaths(new ZipLinePath(331, false, List(Vector3(919.883f, 1905.153f, 213.195f), Vector3(920.13f, 1897.232f, 215.577f), Vector3(920.376f, 1889.31f, 217.866f), Vector3(920.962f, 1870.495f, 218.194f))))
      ZipLinePaths(new ZipLinePath(332, false, List(Vector3(945.907f, 1864.106f, 223.179f), Vector3(970.945f, 1864.053f, 226.924f), Vector3(983.464f, 1864.026f, 229.45f), Vector3(989.723f, 1864.013f, 232.112f), Vector3(995.983f, 1864f, 233.575f), Vector3(1006.783f, 1863.977f, 237.691f), Vector3(1022.493f, 1863.944f, 238.2f))))
      ZipLinePaths(new ZipLinePath(333, false, List(Vector3(1101.254f, 2039.38f, 243.191f), Vector3(1088.795f, 2039.644f, 243.592f), Vector3(1076.336f, 2039.908f, 244.899f), Vector3(1051.419f, 2040.436f, 243.312f), Vector3(1043.445f, 2040.605f, 246.791f))))
      ZipLinePaths(new ZipLinePath(334, false, List(Vector3(1032.137f, 2050.7f, 248.19f), Vector3(1031.324f, 2093.69f, 248.19f))))
      ZipLinePaths(new ZipLinePath(335, false, List(Vector3(1024.946f, 2103.952f, 248.184f), Vector3(1021.128f, 2104.082f, 247.235f), Vector3(1017.31f, 2104.212f, 245.011f), Vector3(1009.674f, 2104.472f, 243.564f), Vector3(1002.038f, 2104.732f, 240.916f), Vector3(994.403f, 2104.991f, 238.396f))))
      ZipLinePaths(new ZipLinePath(336, false, List(Vector3(984.665f, 2115.902f, 238.498f), Vector3(984.551f, 2126.835f, 242.852f), Vector3(984.437f, 2137.768f, 245.33f), Vector3(984.322f, 2148.701f, 249.808f), Vector3(984.208f, 2159.634f, 253.219f))))
      ZipLinePaths(new ZipLinePath(337, false, List(Vector3(1000.114f, 2181.035f, 253.65f), Vector3(999.982f, 2187.783f, 256.05f), Vector3(999.916f, 2191.157f, 257.701f), Vector3(999.85f, 2194.532f, 258.351f), Vector3(999.717f, 2201.28f, 260.952f), Vector3(999.585f, 2208.029f, 262.753f), Vector3(999.252f, 2225.024f, 257.521f), Vector3(998.919f, 2242.02f, 253.19f))))
      ZipLinePaths(new ZipLinePath(338, false, List(Vector3(975.39f, 2247.937f, 253.194f), Vector3(927.624f, 2248.853f, 239.174f), Vector3(912.339f, 2249.146f, 233.254f))))
      ZipLinePaths(new ZipLinePath(339, false, List(Vector3(984.178f, 2266.025f, 248.795f), Vector3(984.368f, 2284.326f, 246.192f), Vector3(984.558f, 2302.628f, 243.196f))))
      ZipLinePaths(new ZipLinePath(340, false, List(Vector3(1022.727f, 2168.098f, 248.291f), Vector3(1029.089f, 2168.073f, 246.96f), Vector3(1035.452f, 2168.049f, 244.927f), Vector3(1041.814f, 2168.024f, 242.894f), Vector3(1044.996f, 2168.011f, 241.278f), Vector3(1048.177f, 2167.999f, 240.262f), Vector3(1060.902f, 2167.949f, 238.496f), Vector3(1073.627f, 2167.899f, 238.731f), Vector3(1089.097f, 2167.839f, 238.904f), Vector3(1096.832f, 2167.808f, 239.591f), Vector3(1100.699f, 2167.793f, 242.334f), Vector3(1104.567f, 2167.778f, 243.181f))))
      ZipLinePaths(new ZipLinePath(341, false, List(Vector3(1111.952f, 2156.833f, 243.2f), Vector3(1112.678f, 2115.833f, 243.24f))))
      ZipLinePaths(new ZipLinePath(342, false, List(Vector3(1120.083f, 2103.886f, 243.225f), Vector3(1131.271f, 2103.613f, 239.605f), Vector3(1142.459f, 2103.341f, 235.91f), Vector3(1153.647f, 2103.068f, 232.514f), Vector3(1164.834f, 2102.796f, 228.445f))))
      ZipLinePaths(new ZipLinePath(343, false, List(Vector3(1182.14f, 2087.616f, 228.225f), Vector3(1187.861f, 2087.641f, 227.269f), Vector3(1193.582f, 2087.666f, 224.633f), Vector3(1205.024f, 2087.716f, 221.561f), Vector3(1216.466f, 2087.766f, 217.889f), Vector3(1227.908f, 2087.816f, 213.546f))))
      ZipLinePaths(new ZipLinePath(344, false, List(Vector3(1246.648f, 2072.441f, 213.193f), Vector3(1252.75f, 2072.281f, 212.004f), Vector3(1258.853f, 2072.121f, 208.938f), Vector3(1271.058f, 2071.8f, 206.006f), Vector3(1295.469f, 2071.159f, 198.263f))))
      ZipLinePaths(new ZipLinePath(345, false, List(Vector3(1308.848f, 2041.255f, 198.2f), Vector3(1321.595f, 2041.049f, 197.872f), Vector3(1327.969f, 2040.946f, 197.56f), Vector3(1334.342f, 2040.844f, 196.647f), Vector3(1347.089f, 2040.638f, 191.921f), Vector3(1353.462f, 2040.535f, 189.358f), Vector3(1356.649f, 2040.484f, 188.676f), Vector3(1359.836f, 2040.433f, 187.995f), Vector3(1377.332f, 2040.151f, 193.147f), Vector3(1386.079f, 2040.01f, 195.923f), Vector3(1390.453f, 2039.939f, 197.511f), Vector3(1394.827f, 2039.869f, 198.4f))))
      ZipLinePaths(new ZipLinePath(346, false, List(Vector3(1416.676f, 1890.96f, 218.215f), Vector3(1416.322f, 1856.951f, 227.305f), Vector3(1415.825f, 1809.339f, 238.135f))))
      ZipLinePaths(new ZipLinePath(347, false, List(Vector3(1447.552f, 1891.489f, 218.239f), Vector3(1447.824f, 1882.556f, 219.813f), Vector3(1448.097f, 1873.623f, 222.71f), Vector3(1448.369f, 1864.691f, 224.806f), Vector3(1448.505f, 1860.224f, 226.654f), Vector3(1448.641f, 1855.758f, 228.22f))))
      ZipLinePaths(new ZipLinePath(348, false, List(Vector3(1603.929f, 1672.213f, 223.508f), Vector3(1609.247f, 1672.265f, 225.317f), Vector3(1614.566f, 1672.316f, 227.644f), Vector3(1622.784f, 1672.396f, 229.569f), Vector3(1631.003f, 1672.476f, 232.694f), Vector3(1639.705f, 1672.561f, 233.209f))))
      ZipLinePaths(new ZipLinePath(349, false, List(Vector3(1071.874f, 217.362f, 143.135f), Vector3(1075.187f, 246.758f, 149.615f), Vector3(1075.001f, 273.955f, 160.196f), Vector3(1074.433f, 288.103f, 163.436f), Vector3(1080.365f, 302.251f, 163.976f), Vector3(1087.747f, 316.399f, 164.267f), Vector3(1095.129f, 330.547f, 163.857f), Vector3(1108.319f, 358.364f, 163.255f))))
      ZipLinePaths(new ZipLinePath(350, false, List(Vector3(1126.922f, 406.781f, 163.147f), Vector3(1139.227f, 412.457f, 164.141f), Vector3(1151.532f, 418.033f, 166.046f), Vector3(1176.141f, 428.985f, 165.457f), Vector3(1200.551f, 434.386f, 171.439f), Vector3(1225.361f, 436.388f, 178.422f), Vector3(1237.425f, 437.112f, 181.369f), Vector3(1243.456f, 435.273f, 182.842f), Vector3(1249.488f, 433.435f, 183.226f))))
      ZipLinePaths(new ZipLinePath(351, false, List(Vector3(976.744f, 153.992f, 143.01f), Vector3(977.459f, 161.322f, 145.813f), Vector3(978.274f, 170.653f, 149.022f), Vector3(979.304f, 188.314f, 155.14f), Vector3(979.884f, 204.475f, 160.058f), Vector3(978.564f, 220.836f, 163.776f), Vector3(968.873f, 240.673f, 164.006f), Vector3(962.182f, 252.51f, 163.15f))))
      ZipLinePaths(new ZipLinePath(352, false, List(Vector3(915.3f, 242.709f, 163.14f), Vector3(892.78f, 254.164f, 163.864f), Vector3(873.959f, 264.419f, 164.493f), Vector3(865.454f, 268.224f, 164.699f), Vector3(857.047f, 272.332f, 163.967f), Vector3(847.54f, 273.24f, 163.14f))))
      ZipLinePaths(new ZipLinePath(353, false, List(Vector3(850.016f, 313.937f, 163.154f), Vector3(851.875f, 330.642f, 168.036f), Vector3(852.733f, 347.447f, 173.436f), Vector3(853.391f, 364.052f, 178.236f), Vector3(851.92f, 372.405f, 181.236f), Vector3(850.449f, 380.757f, 184.236f), Vector3(849.903f, 383.662f, 184.826f), Vector3(857.763f, 404.967f, 185.414f))))
      ZipLinePaths(new ZipLinePath(354, false, List(Vector3(999.272f, 110.243f, 143.14f), Vector3(975.32f, 109.409f, 144.463f), Vector3(951.369f, 108.976f, 146.491f), Vector3(903.465f, 109.909f, 148.29f), Vector3(865.142f, 109.176f, 147.437f), Vector3(840.712f, 115.986f, 146.736f), Vector3(816.281f, 153.796f, 145.236f), Vector3(813.449f, 162.943f, 143.14f))))
      ZipLinePaths(new ZipLinePath(355, false, List(Vector3(793.348f, 215.24f, 143.142f), Vector3(777.178f, 222.349f, 144.83f), Vector3(761.408f, 229.859f, 146.825f), Vector3(724.868f, 249.478f, 148.414f), Vector3(719.867f, 278.066f, 153.799f), Vector3(720.665f, 306.654f, 161.784f), Vector3(713.164f, 336.042f, 165.268f), Vector3(702.462f, 363.83f, 166.353f), Vector3(686.882f, 389.303f, 163.142f))))
      ZipLinePaths(new ZipLinePath(356, false, List(Vector3(633.169f, 397.14f, 163.143f), Vector3(605.993f, 410.372f, 164.411f), Vector3(596.218f, 423.604f, 166.187f), Vector3(589.968f, 447.667f, 168.938f), Vector3(587.268f, 478.38f, 175.401f), Vector3(588.968f, 509.092f, 184.464f), Vector3(610.595f, 560.61f, 185.838f), Vector3(618.614f, 568.535f, 183.144f))))
      ZipLinePaths(new ZipLinePath(357, false, List(Vector3(313.406f, 1229.852f, 173.13f), Vector3(338.439f, 1227.908f, 176.796f), Vector3(363.471f, 1226.764f, 177.758f), Vector3(388.503f, 1228.12f, 176.69f), Vector3(413.535f, 1242.276f, 174.422f), Vector3(421.225f, 1250.419f, 172.87f))))
      ZipLinePaths(new ZipLinePath(358, false, List(Vector3(400.806f, 1286.539f, 173.14f), Vector3(413.261f, 1315.389f, 174.687f), Vector3(421.717f, 1331.039f, 175.139f), Vector3(436.173f, 1358.29f, 175.966f), Vector3(445.129f, 1375.54f, 174.192f), Vector3(455.025f, 1383.393f, 173.25f))))
      ZipLinePaths(new ZipLinePath(359, false, List(Vector3(473.924f, 1357.231f, 173.255f), Vector3(489.493f, 1355.978f, 176.417f), Vector3(532.11f, 1359.281f, 191.664f), Vector3(542.667f, 1357.76f, 193.221f), Vector3(553.225f, 1354.44f, 192.978f), Vector3(574.341f, 1331.2f, 190.491f), Vector3(583.726f, 1319.583f, 188.213f))))
      ZipLinePaths(new ZipLinePath(360, false, List(Vector3(871.063f, 2357.891f, 243.25f), Vector3(853.39f, 2351.203f, 243.371f), Vector3(835.717f, 2344.015f, 244.193f), Vector3(818.045f, 2334.977f, 244.692f), Vector3(800.372f, 2322.739f, 244.992f), Vector3(762.899f, 2305.651f, 245.891f), Vector3(750.563f, 2278.707f, 247.14f), Vector3(738.626f, 2252.763f, 246.19f), Vector3(729.181f, 2236.187f, 245.188f), Vector3(716.957f, 2203.652f, 243.25f))))
      ZipLinePaths(new ZipLinePath(361, false, List(Vector3(701.114f, 2159.581f, 243.13f), Vector3(676.278f, 2156.712f, 244.456f), Vector3(651.441f, 2155.243f, 245.482f), Vector3(640.513f, 2155.181f, 246.605f), Vector3(636.319f, 2153.866f, 245.631f), Vector3(631.572f, 2151.549f, 243.14f))))
      ZipLinePaths(new ZipLinePath(362, false, List(Vector3(584.654f, 2136.17f, 243.142f), Vector3(559.91f, 2085.271f, 246.047f), Vector3(556.012f, 2018.31f, 227.197f), Vector3(568.313f, 1984.829f, 224.771f), Vector3(591.013f, 1968.089f, 223.959f), Vector3(614.664f, 1963.118f, 224.002f), Vector3(621.714f, 1951.348f, 224.446f), Vector3(630.106f, 1927.935f, 225.697f), Vector3(641.698f, 1904.522f, 226.547f), Vector3(649.245f, 1892.815f, 226.173f), Vector3(652.218f, 1886.962f, 226.285f), Vector3(651.491f, 1881.109f, 223.798f), Vector3(651.087f, 1869.402f, 219.423f), Vector3(650.683f, 1857.696f, 214.849f), Vector3(652.529f, 1819.989f, 205.924f), Vector3(675.576f, 1782.283f, 205.199f), Vector3(688.468f, 1776.87f, 204.45f), Vector3(702.021f, 1773.798f, 203.849f), Vector3(712.174f, 1769.926f, 203.252f))))
      ZipLinePaths(new ZipLinePath(363, false, List(Vector3(759.295f, 1749.047f, 203.14f), Vector3(776.975f, 1744.772f, 203.259f), Vector3(785.816f, 1743.035f, 203.971f), Vector3(794.656f, 1739.297f, 204.483f), Vector3(805.403f, 1727.828f, 204.759f), Vector3(810.777f, 1721.494f, 204.497f), Vector3(813.463f, 1716.927f, 204.166f), Vector3(817.55f, 1700.759f, 203.14f))))
      ZipLinePaths(new ZipLinePath(364, false, List(Vector3(1058.578f, 2320.326f, 243.13f), Vector3(1109.572f, 2319.579f, 243.885f), Vector3(1124.57f, 2319.359f, 243.14f))))
      ZipLinePaths(new ZipLinePath(365, false, List(Vector3(1126.83f, 2280.158f, 243.14f), Vector3(1142.435f, 2272.359f, 243.249f), Vector3(1177.84f, 2255.76f, 244.063f), Vector3(1230.451f, 2249.961f, 244.046f), Vector3(1248.101f, 2256.233f, 243.07f))))
      ZipLinePaths(new ZipLinePath(366, false, List(Vector3(1255.276f, 2294.393f, 243.146f), Vector3(1284.893f, 2309.059f, 243.882f), Vector3(1314.511f, 2323.526f, 244.528f), Vector3(1339.759f, 2322.607f, 237.148f), Vector3(1365.007f, 2322.287f, 226.969f), Vector3(1384.228f, 2320.903f, 223.146f))))
      ZipLinePaths(new ZipLinePath(367, false, List(Vector3(1386.825f, 2264.216f, 223.08f), Vector3(1364.395f, 2219.526f, 223.891f), Vector3(1361.703f, 2214.163f, 223.14f))))
      ZipLinePaths(new ZipLinePath(368, false, List(Vector3(1422.85f, 2276.601f, 223.14f), Vector3(1446.379f, 2234.273f, 223.892f), Vector3(1456.732f, 2213.056f, 223.14f))))
      ZipLinePaths(new ZipLinePath(369, false, List(Vector3(1482.817f, 2150.7f, 223.14f), Vector3(1506.01f, 2103.6f, 224.078f), Vector3(1517.143f, 2085.137f, 223.14f))))
      ZipLinePaths(new ZipLinePath(370, false, List(Vector3(1552.37f, 2085.239f, 223.14f), Vector3(1552.544f, 2060.239f, 222.477f), Vector3(1552.718f, 2035.239f, 215.118f), Vector3(1553.066f, 1985.239f, 211.542f), Vector3(1553.414f, 1935.239f, 215.167f), Vector3(1553.762f, 1885.239f, 223.991f), Vector3(1553.776f, 1883.239f, 223.25f))))
      ZipLinePaths(new ZipLinePath(371, false, List(Vector3(1579.071f, 1833.389f, 223.14f), Vector3(1580.239f, 1824.555f, 225.36f), Vector3(1580.081f, 1813.472f, 226.39f), Vector3(1579.087f, 1755.799f, 227.67f), Vector3(1551.894f, 1698.526f, 228.35f), Vector3(1553.643f, 1647.741f, 225.947f), Vector3(1552.481f, 1638.816f, 223.14f))))
      ZipLinePaths(new ZipLinePath(372, false, List(Vector3(1584.84f, 1635.059f, 223.149f), Vector3(1585.738f, 1620.366f, 222.776f), Vector3(1586.635f, 1605.673f, 221.317f), Vector3(1588.031f, 1586.286f, 213.2f), Vector3(1587.426f, 1561.899f, 209.055f), Vector3(1582.821f, 1537.513f, 204.31f), Vector3(1576.281f, 1510.2f, 198.51f))))
      ZipLinePaths(new ZipLinePath(373, false, List(Vector3(1394.807f, 2214.354f, 223.156f), Vector3(1395.435f, 2200.517f, 223.103f), Vector3(1396.064f, 2191.081f, 221.372f), Vector3(1396.734f, 2141.625f, 206.591f), Vector3(1399.101f, 2134.836f, 203.154f))))
      ZipLinePaths(new ZipLinePath(374, false, List(Vector3(1420.624f, 2088.402f, 203.14f), Vector3(1421.17f, 2063.451f, 204.095f), Vector3(1420.516f, 2038.501f, 207.355f), Vector3(1420.06f, 2016.546f, 205.797f), Vector3(1415.199f, 2006.567f, 203.25f))))
      ZipLinePaths(new ZipLinePath(375, false, List(Vector3(1498.604f, 1508.597f, 196.804f), Vector3(1460.323f, 1489.823f, 223.692f), Vector3(1422.042f, 1471.048f, 249.813f), Vector3(1409.026f, 1463.665f, 260.349f), Vector3(1395.211f, 1458.682f, 265.003f))))
      ZipLinePaths(new ZipLinePath(376, false, List(Vector3(1864.299f, 1327.779f, 193.14f), Vector3(1814.317f, 1329.165f, 193.957f), Vector3(1787.327f, 1329.913f, 193.25f))))
      ZipLinePaths(new ZipLinePath(377, false, List(Vector3(1782.437f, 1289.688f, 193.25f), Vector3(1775.027f, 1268.099f, 194.165f), Vector3(1757.218f, 1234.911f, 194.182f), Vector3(1744.608f, 1210.523f, 194.478f), Vector3(1735.598f, 1183.335f, 194.174f), Vector3(1714.544f, 1169.712f, 193.25f))))
      ZipLinePaths(new ZipLinePath(378, false, List(Vector3(1685.673f, 1202.774f, 193.14f), Vector3(1635.673f, 1202.42f, 194.176f), Vector3(1623.673f, 1202.335f, 193.43f))))
      ZipLinePaths(new ZipLinePath(379, false, List(Vector3(589.162f, 1287.888f, 187.36f), Vector3(606.508f, 1241.031f, 190.011f), Vector3(623.855f, 1194.175f, 191.917f), Vector3(641.201f, 1147.318f, 193.824f), Vector3(652.649f, 1116.393f, 194.39f))))
      ZipLinePaths(new ZipLinePath(380, false, List(Vector3(654.164f, 891.092f, 187.24f), Vector3(646.279f, 841.718f, 192.507f), Vector3(638.393f, 792.343f, 194.029f), Vector3(630.507f, 742.969f, 193.551f), Vector3(622.621f, 693.595f, 191.074f), Vector3(618.205f, 665.945f, 185.14f))))
      ZipLinePaths(new ZipLinePath(381, false, List(Vector3(761.584f, 541.531f, 187.14f), Vector3(779.403f, 494.815f, 194.028f), Vector3(797.222f, 448.099f, 193.57f), Vector3(810.765f, 412.594f, 186.55f))))
      ZipLinePaths(new ZipLinePath(382, false, List(Vector3(1222.018f, 631.67f, 196.892f), Vector3(1232.851f, 583.016f, 193.697f), Vector3(1243.685f, 534.361f, 189.756f), Vector3(1254.519f, 485.708f, 185.815f), Vector3(1260.369f, 459.436f, 182.691f))))
      ZipLinePaths(new ZipLinePath(383, false, List(Vector3(1271.689f, 782.181f, 274.208f), Vector3(1277.334f, 762.195f, 272.418f), Vector3(1282.98f, 742.209f, 247.122f), Vector3(1294.272f, 702.237f, 219.284f), Vector3(1301.047f, 678.254f, 201.102f))))
      ZipLinePaths(new ZipLinePath(384, false, List(Vector3(1368.124f, 1341.561f, 296.172f), Vector3(1364.566f, 1365.321f, 292.123f), Vector3(1361.008f, 1389.081f, 279.805f), Vector3(1353.892f, 1436.601f, 262.712f), Vector3(1353.334f, 1440.328f, 259.543f))))
      ZipLinePaths(new ZipLinePath(385, false, List(Vector3(1348.286f, 1441.756f, 259.543f), Vector3(1314.196f, 1425.256f, 255.043f))))
      ZipLinePaths(new ZipLinePath(386, false, List(Vector3(1060.551f, 1605.738f, 278.033f), Vector3(1107.398f, 1625.112f, 273.261f), Vector3(1154.246f, 1644.486f, 267.73f), Vector3(1157.001f, 1645.625f, 265.434f))))
      ZipLinePaths(new ZipLinePath(387, false, List(Vector3(954.548f, 1391.85f, 224.246f), Vector3(946.487f, 1390.942f, 233.408f), Vector3(938.426f, 1390.034f, 234.764f))))
      ZipLinePaths(new ZipLinePath(388, false, List(Vector3(916.348f, 1384.567f, 234.767f), Vector3(930.496f, 1358.334f, 275.657f), Vector3(937.57f, 1345.218f, 311.353f), Vector3(944.644f, 1332.101f, 315.548f))))
      ZipLinePaths(new ZipLinePath(389, false, List(Vector3(1366.672f, 1311.332f, 296.193f), Vector3(1355.793f, 1291.42f, 291.968f), Vector3(1344.913f, 1271.507f, 275.896f), Vector3(1323.154f, 1231.682f, 254.893f), Vector3(1301.395f, 1191.856f, 233.891f), Vector3(1294.367f, 1178.408f, 225.36f))))
      ZipLinePaths(new ZipLinePath(390, false, List(Vector3(783.039f, 1209.045f, 217.8f), Vector3(757.026f, 1252.86f, 216.442f), Vector3(731.012f, 1296.676f, 214.34f), Vector3(718.26f, 1318.154f, 212.49f))))
      ZipLinePaths(new ZipLinePath(391, true, List(Vector3(1374.548f, 1288.463f, 211.2f), Vector3(1373.826f, 1281.376f, 224.2f))))
      ZipLinePaths(new ZipLinePath(392, false, List(Vector3(1026.257f, 843.695f, 185.942f), Vector3(1028.149f, 852.608f, 195.5f), Vector3(1028.419f, 856.395f, 196.141f))))
      ZipLinePaths(new ZipLinePath(393, false, List(Vector3(1166.499f, 889.383f, 196.147f), Vector3(1172.461f, 895.042f, 211.579f), Vector3(1176.159f, 898.61f, 211.891f))))
      ZipLinePaths(new ZipLinePath(394, false, List(Vector3(1118.547f, 990.403f, 204.698f), Vector3(1121.775f, 995.338f, 211.55f), Vector3(1124.542f, 999.568f, 212.35f))))
      ZipLinePaths(new ZipLinePath(395, false, List(Vector3(1205.96f, 995.02f, 212.6f), Vector3(1238.316f, 1003.723f, 250.267f), Vector3(1243.219f, 1000.629f, 251.421f))))
      ZipLinePaths(new ZipLinePath(396, false, List(Vector3(1238.847f, 1034.95f, 254.212f), Vector3(1226.866f, 990.364f, 274.176f), Vector3(1214.885f, 945.778f, 294.579f), Vector3(1212.968f, 938.644f, 296.041f))))
      ZipLinePaths(new ZipLinePath(397, false, List(Vector3(1218.983f, 891.836f, 296.012f), Vector3(1238.388f, 846.056f, 285.407f), Vector3(1255.51f, 805.662f, 274.234f))))
      ZipLinePaths(new ZipLinePath(398, false, List(Vector3(1291.711f, 807.736f, 274.383f), Vector3(1321.944f, 844.617f, 254.485f), Vector3(1351.777f, 880.498f, 233.914f), Vector3(1377.191f, 907.137f, 212.703f))))
      ZipLinePaths(new ZipLinePath(399, false, List(Vector3(1406.765f, 919.786f, 212.679f), Vector3(1407.449f, 916.317f, 212.18f), Vector3(1411.14f, 901.425f, 185.702f))))
      ZipLinePaths(new ZipLinePath(400, false, List(Vector3(1331.335f, 910.391f, 212.572f), Vector3(1331.967f, 904.956f, 212.32f), Vector3(1337.491f, 876.897f, 193.028f))))
      ZipLinePaths(new ZipLinePath(401, false, List(Vector3(1257.393f, 959.594f, 213.244f), Vector3(1257.67f, 955.326f, 212.8f), Vector3(1258.499f, 948.124f, 195.843f))))
      ZipLinePaths(new ZipLinePath(402, false, List(Vector3(1241.54f, 848.057f, 211.951f), Vector3(1238.514f, 847.292f, 211.5f), Vector3(1224.848f, 843.823f, 185.972f))))
      ZipLinePaths(new ZipLinePath(403, false, List(Vector3(996.539f, 837.803f, 195.145f), Vector3(996.865f, 833.708f, 194.678f), Vector3(997.015f, 825.448f, 185.844f))))
      ZipLinePaths(new ZipLinePath(404, false, List(Vector3(872.179f, 908.625f, 208.904f), Vector3(873.49f, 904.836f, 208.601f), Vector3(887.154f, 880.725f, 197.6f), Vector3(893.144f, 869.566f, 187.283f))))
      ZipLinePaths(new ZipLinePath(405, false, List(Vector3(747.407f, 1402.556f, 204.704f), Vector3(755.396f, 1401.347f, 213.3f), Vector3(758.872f, 1401.143f, 213.347f))))
      ZipLinePaths(new ZipLinePath(406, false, List(Vector3(750.093f, 1561.501f, 212.656f), Vector3(753.513f, 1564.077f, 212.532f), Vector3(758.477f, 1569.104f, 200.151f))))
      ZipLinePaths(new ZipLinePath(407, false, List(Vector3(946.019f, 1502.417f, 212.646f), Vector3(945.91f, 1506.223f, 212.4f), Vector3(945.729f, 1510.968f, 197.607f))))
      ZipLinePaths(new ZipLinePath(408, false, List(Vector3(973.283f, 1524.192f, 226.152f), Vector3(971.004f, 1527.568f, 225.9f), Vector3(966.521f, 1539.625f, 197.502f))))
      ZipLinePaths(new ZipLinePath(409, false, List(Vector3(1016.489f, 1476.031f, 201.447f), Vector3(1012.699f, 1481.305f, 225.545f), Vector3(1010.752f, 1483.124f, 226.146f))))
      ZipLinePaths(new ZipLinePath(410, false, List(Vector3(1075.853f, 1401.411f, 225.686f), Vector3(1072.731f, 1402.664f, 225.274f), Vector3(1056.124f, 1409.047f, 204.842f))))
      ZipLinePaths(new ZipLinePath(411, false, List(Vector3(1148.506f, 1446.867f, 220.562f), Vector3(1143.938f, 1454.651f, 219.37f), Vector3(1139.37f, 1462.434f, 214.04f))))
      ZipLinePaths(new ZipLinePath(412, false, List(Vector3(1278.732f, 1509.007f, 222.004f), Vector3(1291.364f, 1524.6f, 220.425f), Vector3(1304.195f, 1540.394f, 213.35f), Vector3(1307.016f, 1543.923f, 209.65f), Vector3(1310.036f, 1547.652f, 193.654f))))
      ZipLinePaths(new ZipLinePath(413, false, List(Vector3(1347.651f, 1449.073f, 220.296f), Vector3(1347.577f, 1452.166f, 220.04f), Vector3(1347.094f, 1466.267f, 194.846f))))
      ZipLinePaths(new ZipLinePath(414, false, List(Vector3(1377.227f, 1428.314f, 220.289f), Vector3(1380.011f, 1429.335f, 220.008f), Vector3(1390.577f, 1434.033f, 194.848f))))
      ZipLinePaths(new ZipLinePath(415, false, List(Vector3(1348.868f, 1387.999f, 220.307f), Vector3(1351.913f, 1371.802f, 218.025f), Vector3(1354.957f, 1355.606f, 211.576f))))
      ZipLinePaths(new ZipLinePath(416, false, List(Vector3(1398.041f, 1347.441f, 232.949f), Vector3(1394.732f, 1347.912f, 232.435f), Vector3(1385.039f, 1349.7f, 211.562f))))
      ZipLinePaths(new ZipLinePath(417, false, List(Vector3(1453.473f, 1359.667f, 232.956f), Vector3(1457.288f, 1359.47f, 232.693f), Vector3(1468.838f, 1358.513f, 187.157f))))
      ZipLinePaths(new ZipLinePath(418, false, List(Vector3(1430.748f, 1320.637f, 232.954f), Vector3(1431.562f, 1316.973f, 232.584f), Vector3(1436.177f, 1305.54f, 194.848f))))
      ZipLinePaths(new ZipLinePath(419, false, List(Vector3(1421.757f, 1366.067f, 232.988f), Vector3(1405.097f, 1410.405f, 252.602f), Vector3(1392.284f, 1443.842f, 266.389f))))
      ZipLinePaths(new ZipLinePath(420, true, List(Vector3(973.389f, 913.595f, 196.95f), Vector3(971.862f, 927.004f, 204.35f))))
    }

    ZipLines()

  }
}
