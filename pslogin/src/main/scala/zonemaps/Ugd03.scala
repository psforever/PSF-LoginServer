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

object Ugd03 { // Adlivun
  val ZoneMap = new ZoneMap("ugd03") {
    Scale = MapScale.Dim2048
    Checksum = 1673539651L

    Building10020()

    def Building10020(): Unit = { // Name: ceiling_bldg_a_10020 Type: ceiling_bldg_a GUID: 1, MapID: 10020
      LocalBuilding("ceiling_bldg_a_10020", 1, 10020, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(845.85f, 782.72f, 106.07f), ceiling_bldg_a)))
      LocalObject(808, Door.Constructor(Vector3(844.4113f, 798.6173f, 107.849f)), owning_building_guid = 1)
      LocalObject(809, Door.Constructor(Vector3(845.2345f, 763.904f, 113.355f)), owning_building_guid = 1)
      LocalObject(810, Door.Constructor(Vector3(858.5089f, 771.0483f, 107.849f)), owning_building_guid = 1)
      LocalObject(811, Door.Constructor(Vector3(865.1314f, 777.3246f, 113.355f)), owning_building_guid = 1)
    }

    Building10028()

    def Building10028(): Unit = { // Name: ceiling_bldg_a_10028 Type: ceiling_bldg_a GUID: 2, MapID: 10028
      LocalBuilding("ceiling_bldg_a_10028", 2, 10028, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1089.56f, 1080.75f, 112.69f), ceiling_bldg_a)))
      LocalObject(846, Door.Constructor(Vector3(1081.78f, 1094.688f, 114.469f)), owning_building_guid = 2)
      LocalObject(847, Door.Constructor(Vector3(1096.651f, 1063.31f, 119.975f)), owning_building_guid = 2)
      LocalObject(848, Door.Constructor(Vector3(1105.872f, 1075.236f, 114.469f)), owning_building_guid = 2)
      LocalObject(849, Door.Constructor(Vector3(1109.369f, 1083.664f, 119.975f)), owning_building_guid = 2)
    }

    Building10019()

    def Building10019(): Unit = { // Name: ceiling_bldg_b_10019 Type: ceiling_bldg_b GUID: 3, MapID: 10019
      LocalBuilding("ceiling_bldg_b_10019", 3, 10019, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(677.51f, 874.3f, 101.68f), ceiling_bldg_b)))
      LocalObject(770, Door.Constructor(Vector3(675.7744f, 868.4519f, 103.459f)), owning_building_guid = 3)
      LocalObject(771, Door.Constructor(Vector3(693.6315f, 870.2904f, 103.459f)), owning_building_guid = 3)
    }

    Building10029()

    def Building10029(): Unit = { // Name: ceiling_bldg_b_10029 Type: ceiling_bldg_b GUID: 4, MapID: 10029
      LocalBuilding("ceiling_bldg_b_10029", 4, 10029, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1006.55f, 1224.66f, 101.59f), ceiling_bldg_b)))
      LocalObject(828, Door.Constructor(Vector3(1003.656f, 1241.019f, 103.369f)), owning_building_guid = 4)
      LocalObject(830, Door.Constructor(Vector3(1012.598f, 1225.453f, 103.369f)), owning_building_guid = 4)
    }

    Building10011()

    def Building10011(): Unit = { // Name: ceiling_bldg_b_10011 Type: ceiling_bldg_b GUID: 5, MapID: 10011
      LocalBuilding("ceiling_bldg_b_10011", 5, 10011, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1328.95f, 1247.9f, 111.73f), ceiling_bldg_b)))
      LocalObject(891, Door.Constructor(Vector3(1330.965f, 1264.39f, 113.509f)), owning_building_guid = 5)
      LocalObject(893, Door.Constructor(Vector3(1334.966f, 1246.89f, 113.509f)), owning_building_guid = 5)
    }

    Building10301()

    def Building10301(): Unit = { // Name: ceiling_bldg_c_10301 Type: ceiling_bldg_c GUID: 6, MapID: 10301
      LocalBuilding("ceiling_bldg_c_10301", 6, 10301, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(763.93f, 652.46f, 123.63f), ceiling_bldg_c)))
      LocalObject(793, Door.Constructor(Vector3(760.2493f, 654.6059f, 125.409f)), owning_building_guid = 6)
      LocalObject(800, Door.Constructor(Vector3(787.3882f, 696.5608f, 125.409f)), owning_building_guid = 6)
      LocalObject(802, Door.Constructor(Vector3(806.8972f, 672.4693f, 125.409f)), owning_building_guid = 6)
    }

    Building10085()

    def Building10085(): Unit = { // Name: ceiling_bldg_d_10085 Type: ceiling_bldg_d GUID: 7, MapID: 10085
      LocalBuilding("ceiling_bldg_d_10085", 7, 10085, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1186.2f, 1198.95f, 111.11f), ceiling_bldg_d)))
      LocalObject(853, Door.Constructor(Vector3(1168.71f, 1198.966f, 112.845f)), owning_building_guid = 7)
      LocalObject(854, Door.Constructor(Vector3(1186.216f, 1181.44f, 112.845f)), owning_building_guid = 7)
      LocalObject(855, Door.Constructor(Vector3(1186.216f, 1216.44f, 112.845f)), owning_building_guid = 7)
      LocalObject(856, Door.Constructor(Vector3(1203.71f, 1198.966f, 112.845f)), owning_building_guid = 7)
      LocalObject(971, Painbox.Constructor(Vector3(1186.107f, 1198.747f, 119.418f), painbox_continuous), owning_building_guid = 7)
    }

    Building10024()

    def Building10024(): Unit = { // Name: ceiling_bldg_g_10024 Type: ceiling_bldg_g GUID: 8, MapID: 10024
      LocalBuilding("ceiling_bldg_g_10024", 8, 10024, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(705.96f, 1266.02f, 88.19f), ceiling_bldg_g)))
      LocalObject(776, Door.Constructor(Vector3(701.7747f, 1283.875f, 89.969f)), owning_building_guid = 8)
      LocalObject(781, Door.Constructor(Vector3(716.9544f, 1252.418f, 89.969f)), owning_building_guid = 8)
    }

    Building10026()

    def Building10026(): Unit = { // Name: ceiling_bldg_g_10026 Type: ceiling_bldg_g GUID: 9, MapID: 10026
      LocalBuilding("ceiling_bldg_g_10026", 9, 10026, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(780.44f, 1084.53f, 99.81f), ceiling_bldg_g)))
      LocalObject(796, Door.Constructor(Vector3(772.456f, 1068.02f, 101.589f)), owning_building_guid = 9)
      LocalObject(799, Door.Constructor(Vector3(780.456f, 1102.02f, 101.589f)), owning_building_guid = 9)
    }

    Building10016()

    def Building10016(): Unit = { // Name: ceiling_bldg_g_10016 Type: ceiling_bldg_g GUID: 10, MapID: 10016
      LocalBuilding("ceiling_bldg_g_10016", 10, 10016, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1249.74f, 1307.4f, 94.99f), ceiling_bldg_g)))
      LocalObject(863, Door.Constructor(Vector3(1232.641f, 1300.771f, 96.769f)), owning_building_guid = 10)
      LocalObject(871, Door.Constructor(Vector3(1261.68f, 1320.181f, 96.769f)), owning_building_guid = 10)
    }

    Building10021()

    def Building10021(): Unit = { // Name: ceiling_bldg_g_10021 Type: ceiling_bldg_g GUID: 11, MapID: 10021
      LocalBuilding("ceiling_bldg_g_10021", 11, 10021, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1349.23f, 652.87f, 88.19f), ceiling_bldg_g)))
      LocalObject(896, Door.Constructor(Vector3(1341.246f, 636.36f, 89.969f)), owning_building_guid = 11)
      LocalObject(898, Door.Constructor(Vector3(1349.246f, 670.36f, 89.969f)), owning_building_guid = 11)
    }

    Building10023()

    def Building10023(): Unit = { // Name: ceiling_bldg_h_10023 Type: ceiling_bldg_h GUID: 12, MapID: 10023
      LocalBuilding("ceiling_bldg_h_10023", 12, 10023, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(899.77f, 1249.92f, 103.44f), ceiling_bldg_h)))
      LocalObject(813, Door.Constructor(Vector3(887.8799f, 1262.048f, 105.219f)), owning_building_guid = 12)
      LocalObject(815, Door.Constructor(Vector3(894.4103f, 1233.825f, 105.219f)), owning_building_guid = 12)
      LocalObject(818, Door.Constructor(Vector3(915.1321f, 1253.392f, 107.719f)), owning_building_guid = 12)
    }

    Building10030()

    def Building10030(): Unit = { // Name: ceiling_bldg_h_10030 Type: ceiling_bldg_h GUID: 13, MapID: 10030
      LocalBuilding("ceiling_bldg_h_10030", 13, 10030, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1133.55f, 738.99f, 104.66f), ceiling_bldg_h)))
      LocalObject(850, Door.Constructor(Vector3(1117.06f, 735.006f, 106.439f)), owning_building_guid = 13)
      LocalObject(851, Door.Constructor(Vector3(1137.534f, 755.5f, 106.439f)), owning_building_guid = 13)
      LocalObject(852, Door.Constructor(Vector3(1144.635f, 727.802f, 108.939f)), owning_building_guid = 13)
    }

    Building10017()

    def Building10017(): Unit = { // Name: ceiling_bldg_h_10017 Type: ceiling_bldg_h GUID: 14, MapID: 10017
      LocalBuilding("ceiling_bldg_h_10017", 14, 10017, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1246.32f, 775.32f, 92.82f), ceiling_bldg_h)))
      LocalObject(862, Door.Constructor(Vector3(1232.196f, 782.2896f, 97.099f)), owning_building_guid = 14)
      LocalObject(866, Door.Constructor(Vector3(1247.928f, 758.4124f, 94.599f)), owning_building_guid = 14)
      LocalObject(870, Door.Constructor(Vector3(1260.615f, 784.4556f, 94.599f)), owning_building_guid = 14)
    }

    Building10015()

    def Building10015(): Unit = { // Name: ceiling_bldg_h_10015 Type: ceiling_bldg_h GUID: 15, MapID: 10015
      LocalBuilding("ceiling_bldg_h_10015", 15, 10015, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1385.18f, 1124.38f, 120.28f), ceiling_bldg_h)))
      LocalObject(902, Door.Constructor(Vector3(1368.69f, 1120.396f, 122.059f)), owning_building_guid = 15)
      LocalObject(905, Door.Constructor(Vector3(1389.164f, 1140.89f, 122.059f)), owning_building_guid = 15)
      LocalObject(909, Door.Constructor(Vector3(1396.265f, 1113.192f, 124.559f)), owning_building_guid = 15)
    }

    Building10025()

    def Building10025(): Unit = { // Name: ceiling_bldg_j_10025 Type: ceiling_bldg_j GUID: 16, MapID: 10025
      LocalBuilding("ceiling_bldg_j_10025", 16, 10025, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(744.75f, 1147.08f, 98.87f), ceiling_bldg_j)))
      LocalObject(788, Door.Constructor(Vector3(736.8647f, 1156.792f, 100.649f)), owning_building_guid = 16)
      LocalObject(791, Door.Constructor(Vector3(752.5978f, 1137.363f, 100.649f)), owning_building_guid = 16)
    }

    Building10014()

    def Building10014(): Unit = { // Name: ceiling_bldg_j_10014 Type: ceiling_bldg_j GUID: 17, MapID: 10014
      LocalBuilding("ceiling_bldg_j_10014", 17, 10014, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1255.3f, 1091.51f, 109.62f), ceiling_bldg_j)))
      LocalObject(868, Door.Constructor(Vector3(1255.316f, 1079f, 111.399f)), owning_building_guid = 17)
      LocalObject(869, Door.Constructor(Vector3(1255.316f, 1104f, 111.399f)), owning_building_guid = 17)
    }

    Building10238()

    def Building10238(): Unit = { // Name: ceiling_bldg_j_10238 Type: ceiling_bldg_j GUID: 18, MapID: 10238
      LocalBuilding("ceiling_bldg_j_10238", 18, 10238, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1393.65f, 837.81f, 98.87f), ceiling_bldg_j)))
      LocalObject(907, Door.Constructor(Vector3(1393.666f, 825.3f, 100.649f)), owning_building_guid = 18)
      LocalObject(908, Door.Constructor(Vector3(1393.666f, 850.3f, 100.649f)), owning_building_guid = 18)
    }

    Building10027()

    def Building10027(): Unit = { // Name: ceiling_bldg_z_10027 Type: ceiling_bldg_z GUID: 19, MapID: 10027
      LocalBuilding("ceiling_bldg_z_10027", 19, 10027, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(916.61f, 913.02f, 110.84f), ceiling_bldg_z)))
      LocalObject(814, Door.Constructor(Vector3(892.12f, 917.036f, 112.619f)), owning_building_guid = 19)
      LocalObject(823, Door.Constructor(Vector3(949.12f, 917.036f, 112.619f)), owning_building_guid = 19)
    }

    Building10013()

    def Building10013(): Unit = { // Name: ground_bldg_a_10013 Type: ground_bldg_a GUID: 85, MapID: 10013
      LocalBuilding("ground_bldg_a_10013", 85, 10013, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(933.13f, 698.34f, 81.85f), ground_bldg_a)))
      LocalObject(820, Door.Constructor(Vector3(929.3392f, 714.15f, 83.629f)), owning_building_guid = 85)
      LocalObject(822, Door.Constructor(Vector3(947.2369f, 689.1008f, 83.629f)), owning_building_guid = 85)
    }

    Building10003()

    def Building10003(): Unit = { // Name: ground_bldg_c_10003 Type: ground_bldg_c GUID: 86, MapID: 10003
      LocalBuilding("ground_bldg_c_10003", 86, 10003, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(750.69f, 808.58f, 91.29f), ground_bldg_c)))
      LocalObject(774, Door.Constructor(Vector3(700.9291f, 812.9419f, 93.069f)), owning_building_guid = 86)
      LocalObject(780, Door.Constructor(Vector3(715.4828f, 840.3133f, 93.069f)), owning_building_guid = 86)
      LocalObject(790, Door.Constructor(Vector3(750.1529f, 804.3535f, 93.069f)), owning_building_guid = 86)
    }

    Building10295()

    def Building10295(): Unit = { // Name: ground_bldg_c_10295 Type: ground_bldg_c GUID: 87, MapID: 10295
      LocalBuilding("ground_bldg_c_10295", 87, 10295, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(942.14f, 752.71f, 85.39f), ground_bldg_c)))
      LocalObject(821, Door.Constructor(Vector3(940.63f, 756.694f, 87.169f)), owning_building_guid = 87)
      LocalObject(825, Door.Constructor(Vector3(988.124f, 741.22f, 87.169f)), owning_building_guid = 87)
      LocalObject(826, Door.Constructor(Vector3(988.124f, 772.22f, 87.169f)), owning_building_guid = 87)
    }

    Building10170()

    def Building10170(): Unit = { // Name: ground_bldg_c_10170 Type: ground_bldg_c GUID: 88, MapID: 10170
      LocalBuilding("ground_bldg_c_10170", 88, 10170, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1209.14f, 1162.92f, 80.39f), ground_bldg_c)))
      LocalObject(858, Door.Constructor(Vector3(1211.09f, 1166.708f, 82.169f)), owning_building_guid = 88)
      LocalObject(861, Door.Constructor(Vector3(1231.371f, 1121.059f, 82.169f)), owning_building_guid = 88)
      LocalObject(867, Door.Constructor(Vector3(1254.408f, 1141.802f, 82.169f)), owning_building_guid = 88)
    }

    Building10302()

    def Building10302(): Unit = { // Name: ground_bldg_c_10302 Type: ground_bldg_c GUID: 89, MapID: 10302
      LocalBuilding("ground_bldg_c_10302", 89, 10302, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1236.42f, 689.57f, 84.46f), ground_bldg_c)))
      LocalObject(864, Door.Constructor(Vector3(1234.91f, 693.554f, 86.239f)), owning_building_guid = 89)
      LocalObject(872, Door.Constructor(Vector3(1282.404f, 678.08f, 86.239f)), owning_building_guid = 89)
      LocalObject(873, Door.Constructor(Vector3(1282.404f, 709.08f, 86.239f)), owning_building_guid = 89)
    }

    Building10237()

    def Building10237(): Unit = { // Name: ground_bldg_d_10237 Type: ground_bldg_d GUID: 90, MapID: 10237
      LocalBuilding("ground_bldg_d_10237", 90, 10237, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(713.63f, 978.09f, 54.81f), ground_bldg_d)))
      LocalObject(772, Door.Constructor(Vector3(698.0391f, 970.164f, 56.545f)), owning_building_guid = 90)
      LocalObject(777, Door.Constructor(Vector3(705.704f, 993.681f, 56.545f)), owning_building_guid = 90)
      LocalObject(783, Door.Constructor(Vector3(721.5936f, 962.4958f, 56.545f)), owning_building_guid = 90)
      LocalObject(785, Door.Constructor(Vector3(729.2242f, 986.0536f, 56.545f)), owning_building_guid = 90)
    }

    Building10234()

    def Building10234(): Unit = { // Name: ground_bldg_d_10234 Type: ground_bldg_d GUID: 91, MapID: 10234
      LocalBuilding("ground_bldg_d_10234", 91, 10234, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(763.49f, 732.12f, 94.62f), ground_bldg_d)))
      LocalObject(789, Door.Constructor(Vector3(747.0414f, 726.1162f, 96.355f)), owning_building_guid = 91)
      LocalObject(792, Door.Constructor(Vector3(757.4862f, 748.5685f, 96.355f)), owning_building_guid = 91)
      LocalObject(795, Door.Constructor(Vector3(769.4569f, 715.6793f, 96.355f)), owning_building_guid = 91)
      LocalObject(798, Door.Constructor(Vector3(779.9307f, 738.0869f, 96.355f)), owning_building_guid = 91)
    }

    Building10357()

    def Building10357(): Unit = { // Name: ground_bldg_d_10357 Type: ground_bldg_d GUID: 92, MapID: 10357
      LocalBuilding("ground_bldg_d_10357", 92, 10357, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(902.88f, 1372.77f, 43.35f), ground_bldg_d)))
      LocalObject(812, Door.Constructor(Vector3(885.39f, 1372.786f, 45.085f)), owning_building_guid = 92)
      LocalObject(816, Door.Constructor(Vector3(902.896f, 1355.26f, 45.085f)), owning_building_guid = 92)
      LocalObject(817, Door.Constructor(Vector3(902.896f, 1390.26f, 45.085f)), owning_building_guid = 92)
      LocalObject(819, Door.Constructor(Vector3(920.39f, 1372.786f, 45.085f)), owning_building_guid = 92)
    }

    Building10074()

    def Building10074(): Unit = { // Name: ground_bldg_d_10074 Type: ground_bldg_d GUID: 93, MapID: 10074
      LocalBuilding("ground_bldg_d_10074", 93, 10074, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1054.39f, 1246.85f, 72.94f), ground_bldg_d)))
      LocalObject(836, Door.Constructor(Vector3(1036.9f, 1246.866f, 74.675f)), owning_building_guid = 93)
      LocalObject(839, Door.Constructor(Vector3(1054.406f, 1229.34f, 74.675f)), owning_building_guid = 93)
      LocalObject(840, Door.Constructor(Vector3(1054.406f, 1264.34f, 74.675f)), owning_building_guid = 93)
      LocalObject(845, Door.Constructor(Vector3(1071.9f, 1246.866f, 74.675f)), owning_building_guid = 93)
    }

    Building10291()

    def Building10291(): Unit = { // Name: ground_bldg_d_10291 Type: ground_bldg_d GUID: 94, MapID: 10291
      LocalBuilding("ground_bldg_d_10291", 94, 10291, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1226.43f, 1243.3f, 94.69f), ground_bldg_d)))
      LocalObject(857, Door.Constructor(Vector3(1208.94f, 1243.316f, 96.425f)), owning_building_guid = 94)
      LocalObject(859, Door.Constructor(Vector3(1226.446f, 1225.79f, 96.425f)), owning_building_guid = 94)
      LocalObject(860, Door.Constructor(Vector3(1226.446f, 1260.79f, 96.425f)), owning_building_guid = 94)
      LocalObject(865, Door.Constructor(Vector3(1243.94f, 1243.316f, 96.425f)), owning_building_guid = 94)
    }

    Building10135()

    def Building10135(): Unit = { // Name: ground_bldg_e_10135 Type: ground_bldg_e GUID: 95, MapID: 10135
      LocalBuilding("ground_bldg_e_10135", 95, 10135, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1056.2f, 739.36f, 68.24f), ground_bldg_e)))
      LocalObject(835, Door.Constructor(Vector3(1033.179f, 726.7191f, 75.519f)), owning_building_guid = 95)
      LocalObject(837, Door.Constructor(Vector3(1042.885f, 708.2314f, 70.019f)), owning_building_guid = 95)
      LocalObject(842, Door.Constructor(Vector3(1058.492f, 706.6871f, 75.519f)), owning_building_guid = 95)
      LocalObject(844, Door.Constructor(Vector3(1067.806f, 742.6246f, 70.019f)), owning_building_guid = 95)
    }

    Building10002()

    def Building10002(): Unit = { // Name: ground_bldg_f_10002 Type: ground_bldg_f GUID: 96, MapID: 10002
      LocalBuilding("ground_bldg_f_10002", 96, 10002, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(689.69f, 665.37f, 76.67f), ground_bldg_f)))
      LocalObject(760, Door.Constructor(Vector3(662.3607f, 656.8664f, 78.449f)), owning_building_guid = 96)
      LocalObject(779, Door.Constructor(Vector3(713.3111f, 685.332f, 78.449f)), owning_building_guid = 96)
    }

    Building10061()

    def Building10061(): Unit = { // Name: ground_bldg_f_10061 Type: ground_bldg_f GUID: 97, MapID: 10061
      LocalBuilding("ground_bldg_f_10061", 97, 10061, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(991.58f, 1312.88f, 67.64f), ground_bldg_f)))
      LocalObject(824, Door.Constructor(Vector3(987.9187f, 1282.171f, 69.419f)), owning_building_guid = 97)
      LocalObject(829, Door.Constructor(Vector3(1005.768f, 1337.738f, 69.419f)), owning_building_guid = 97)
    }

    Building10001()

    def Building10001(): Unit = { // Name: ground_bldg_f_10001 Type: ground_bldg_f GUID: 98, MapID: 10001
      LocalBuilding("ground_bldg_f_10001", 98, 10001, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1315.3f, 1276.75f, 76.67f), ground_bldg_f)))
      LocalObject(876, Door.Constructor(Vector3(1286.276f, 1266.071f, 78.449f)), owning_building_guid = 98)
      LocalObject(897, Door.Constructor(Vector3(1343.89f, 1275.394f, 78.449f)), owning_building_guid = 98)
    }

    Building10147()

    def Building10147(): Unit = { // Name: ground_bldg_g_10147 Type: ground_bldg_g GUID: 99, MapID: 10147
      LocalBuilding("ground_bldg_g_10147", 99, 10147, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(624.24f, 967.02f, 72.09f), ground_bldg_g)))
      LocalObject(752, Door.Constructor(Vector3(616.256f, 950.51f, 73.869f)), owning_building_guid = 99)
      LocalObject(754, Door.Constructor(Vector3(624.256f, 984.51f, 73.869f)), owning_building_guid = 99)
    }

    Building10154()

    def Building10154(): Unit = { // Name: ground_bldg_h_10154 Type: ground_bldg_h GUID: 100, MapID: 10154
      LocalBuilding("ground_bldg_h_10154", 100, 10154, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(654.94f, 1129.19f, 68.99f), ground_bldg_h)))
      LocalObject(755, Door.Constructor(Vector3(638.45f, 1125.206f, 70.769f)), owning_building_guid = 100)
      LocalObject(759, Door.Constructor(Vector3(658.924f, 1145.7f, 70.769f)), owning_building_guid = 100)
      LocalObject(764, Door.Constructor(Vector3(666.025f, 1118.002f, 73.269f)), owning_building_guid = 100)
    }

    Building10004()

    def Building10004(): Unit = { // Name: ground_bldg_i_10004 Type: ground_bldg_i GUID: 101, MapID: 10004
      LocalBuilding("ground_bldg_i_10004", 101, 10004, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(641.6f, 811.71f, 80.39f), ground_bldg_i)))
      LocalObject(753, Door.Constructor(Vector3(617.2995f, 807.0613f, 82.169f)), owning_building_guid = 101)
      LocalObject(763, Door.Constructor(Vector3(664.5754f, 823.3397f, 82.169f)), owning_building_guid = 101)
    }

    Building10306()

    def Building10306(): Unit = { // Name: ground_bldg_i_10306 Type: ground_bldg_i GUID: 102, MapID: 10306
      LocalBuilding("ground_bldg_i_10306", 102, 10306, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1310.62f, 1122.78f, 80.99f), ground_bldg_i)))
      LocalObject(875, Door.Constructor(Vector3(1285.052f, 1119.71f, 82.769f)), owning_building_guid = 102)
      LocalObject(894, Door.Constructor(Vector3(1335.045f, 1118.837f, 82.769f)), owning_building_guid = 102)
    }

    Building10075()

    def Building10075(): Unit = { // Name: ground_bldg_j_10075 Type: ground_bldg_j GUID: 103, MapID: 10075
      LocalBuilding("ground_bldg_j_10075", 103, 10075, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1060.18f, 1311.97f, 73.4f), ground_bldg_j)))
      LocalObject(841, Door.Constructor(Vector3(1056.538f, 1300.002f, 75.179f)), owning_building_guid = 103)
      LocalObject(843, Door.Constructor(Vector3(1063.847f, 1323.91f, 75.179f)), owning_building_guid = 103)
    }

    Building10322()

    def Building10322(): Unit = { // Name: NW_Redoubt Type: redoubt GUID: 104, MapID: 10322
      LocalBuilding("NW_Redoubt", 104, 10322, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(718.55f, 1209.17f, 43.36f), redoubt)))
      LocalObject(1023, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 104)
      LocalObject(775, Door.Constructor(Vector3(701.5834f, 1213.417f, 45.095f)), owning_building_guid = 104)
      LocalObject(778, Door.Constructor(Vector3(709.5589f, 1223.861f, 55.139f)), owning_building_guid = 104)
      LocalObject(782, Door.Constructor(Vector3(717.5706f, 1221.859f, 55.119f)), owning_building_guid = 104)
      LocalObject(784, Door.Constructor(Vector3(725.3601f, 1219.95f, 55.119f)), owning_building_guid = 104)
      LocalObject(786, Door.Constructor(Vector3(733.3605f, 1217.96f, 55.139f)), owning_building_guid = 104)
      LocalObject(787, Door.Constructor(Vector3(735.5438f, 1204.949f, 45.095f)), owning_building_guid = 104)
      LocalObject(1045, Terminal.Constructor(Vector3(713.951f, 1191.242f, 43.3158f), vanu_equipment_term), owning_building_guid = 104)
      LocalObject(1047, Terminal.Constructor(Vector3(723.0927f, 1227.324f, 43.3135f), vanu_equipment_term), owning_building_guid = 104)
      LocalObject(982, SpawnTube.Constructor(Vector3(718.55f, 1209.17f, 43.36f), Vector3(0, 0, 194)), owning_building_guid = 104)
      LocalObject(970, Painbox.Constructor(Vector3(718.6603f, 1209.442f, 51.149f), painbox_continuous), owning_building_guid = 104)
    }

    Building10359()

    def Building10359(): Unit = { // Name: SE_Redoubt Type: redoubt GUID: 105, MapID: 10359
      LocalBuilding("SE_Redoubt", 105, 10359, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(1397.63f, 773.07f, 43.36f), redoubt)))
      LocalObject(1027, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 105)
      LocalObject(903, Door.Constructor(Vector3(1380.14f, 773.086f, 45.095f)), owning_building_guid = 105)
      LocalObject(904, Door.Constructor(Vector3(1385.352f, 785.149f, 55.139f)), owning_building_guid = 105)
      LocalObject(906, Door.Constructor(Vector3(1393.61f, 785.145f, 55.119f)), owning_building_guid = 105)
      LocalObject(910, Door.Constructor(Vector3(1401.63f, 785.177f, 55.119f)), owning_building_guid = 105)
      LocalObject(911, Door.Constructor(Vector3(1409.874f, 785.182f, 55.139f)), owning_building_guid = 105)
      LocalObject(912, Door.Constructor(Vector3(1415.14f, 773.086f, 45.095f)), owning_building_guid = 105)
      LocalObject(1082, Terminal.Constructor(Vector3(1397.505f, 754.5616f, 43.3158f), vanu_equipment_term), owning_building_guid = 105)
      LocalObject(1083, Terminal.Constructor(Vector3(1397.646f, 791.784f, 43.3135f), vanu_equipment_term), owning_building_guid = 105)
      LocalObject(983, SpawnTube.Constructor(Vector3(1397.63f, 773.07f, 43.36f), Vector3(0, 0, 180)), owning_building_guid = 105)
      LocalObject(973, Painbox.Constructor(Vector3(1397.671f, 773.3606f, 51.149f), painbox_continuous), owning_building_guid = 105)
    }

    Building10005()

    def Building10005(): Unit = { // Name: SW_Stasis Type: vanu_control_point GUID: 211, MapID: 10005
      LocalBuilding("SW_Stasis", 211, 10005, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(662.53f, 731.92f, 71.25f), vanu_control_point)))
      LocalObject(1022, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 211)
      LocalObject(751, Door.Constructor(Vector3(610.1769f, 740.5507f, 73.029f)), owning_building_guid = 211)
      LocalObject(756, Door.Constructor(Vector3(642.492f, 688.1669f, 73.029f)), owning_building_guid = 211)
      LocalObject(757, Door.Constructor(Vector3(648.6942f, 729.0295f, 77.97f)), owning_building_guid = 211)
      LocalObject(758, Door.Constructor(Vector3(655.8808f, 744.3877f, 77.97f)), owning_building_guid = 211)
      LocalObject(761, Door.Constructor(Vector3(663.4542f, 747.4326f, 103.029f)), owning_building_guid = 211)
      LocalObject(762, Door.Constructor(Vector3(664.0893f, 721.8351f, 77.97f)), owning_building_guid = 211)
      LocalObject(765, Door.Constructor(Vector3(666.1021f, 739.6136f, 103.009f)), owning_building_guid = 211)
      LocalObject(766, Door.Constructor(Vector3(668.8757f, 732.0895f, 103.009f)), owning_building_guid = 211)
      LocalObject(767, Door.Constructor(Vector3(671.239f, 737.2011f, 77.97f)), owning_building_guid = 211)
      LocalObject(768, Door.Constructor(Vector3(671.8712f, 724.4004f, 103.029f)), owning_building_guid = 211)
      LocalObject(769, Door.Constructor(Vector3(673.0169f, 767.94f, 73.029f)), owning_building_guid = 211)
      LocalObject(773, Door.Constructor(Vector3(699.4913f, 730.4573f, 73.029f)), owning_building_guid = 211)
      LocalObject(1032, Terminal.Constructor(Vector3(649.8383f, 735.5905f, 76.263f), vanu_equipment_term), owning_building_guid = 211)
      LocalObject(1033, Terminal.Constructor(Vector3(651.4671f, 739.1439f, 76.267f), vanu_equipment_term), owning_building_guid = 211)
      LocalObject(1034, Terminal.Constructor(Vector3(653.808f, 724.6984f, 76.267f), vanu_equipment_term), owning_building_guid = 211)
      LocalObject(1035, Terminal.Constructor(Vector3(657.4075f, 723.016f, 76.263f), vanu_equipment_term), owning_building_guid = 211)
      LocalObject(1036, Terminal.Constructor(Vector3(662.4866f, 743.2345f, 76.263f), vanu_equipment_term), owning_building_guid = 211)
      LocalObject(1037, Terminal.Constructor(Vector3(666.0861f, 741.5521f, 76.267f), vanu_equipment_term), owning_building_guid = 211)
      LocalObject(1038, Terminal.Constructor(Vector3(668.4279f, 727.1069f, 76.267f), vanu_equipment_term), owning_building_guid = 211)
      LocalObject(1039, Terminal.Constructor(Vector3(670.1102f, 730.7064f, 76.263f), vanu_equipment_term), owning_building_guid = 211)
      LocalObject(1097, SpawnTube.Constructor(Vector3(659.9666f, 733.1154f, 76.389f), Vector3(0, 0, 340)), owning_building_guid = 211)
      LocalObject(969, Painbox.Constructor(Vector3(660.1337f, 733.4966f, 85.5918f), painbox_continuous), owning_building_guid = 211)
      LocalObject(974, Painbox.Constructor(Vector3(646.2076f, 727.696f, 80.14f), painbox_door_radius_continuous), owning_building_guid = 211)
      LocalObject(975, Painbox.Constructor(Vector3(655.5827f, 746.0699f, 80.14f), painbox_door_radius_continuous), owning_building_guid = 211)
      LocalObject(976, Painbox.Constructor(Vector3(664.7043f, 719.9772f, 80.14f), painbox_door_radius_continuous), owning_building_guid = 211)
      LocalObject(977, Painbox.Constructor(Vector3(673.4529f, 737.9102f, 79.54f), painbox_door_radius_continuous), owning_building_guid = 211)
    }

    Building10173()

    def Building10173(): Unit = { // Name: NE_Stasis Type: vanu_control_point GUID: 212, MapID: 10173
      LocalBuilding("NE_Stasis", 212, 10173, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1318.15f, 1204.93f, 71.05f), vanu_control_point)))
      LocalObject(1026, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 212)
      LocalObject(874, Door.Constructor(Vector3(1283.918f, 1218.946f, 72.829f)), owning_building_guid = 212)
      LocalObject(877, Door.Constructor(Vector3(1295.976f, 1174.669f, 72.829f)), owning_building_guid = 212)
      LocalObject(880, Door.Constructor(Vector3(1308.16f, 1202.946f, 77.77f)), owning_building_guid = 212)
      LocalObject(883, Door.Constructor(Vector3(1311.976f, 1190.669f, 102.829f)), owning_building_guid = 212)
      LocalObject(884, Door.Constructor(Vector3(1311.944f, 1215.191f, 102.829f)), owning_building_guid = 212)
      LocalObject(885, Door.Constructor(Vector3(1312.162f, 1198.922f, 102.809f)), owning_building_guid = 212)
      LocalObject(886, Door.Constructor(Vector3(1312.129f, 1206.941f, 102.809f)), owning_building_guid = 212)
      LocalObject(889, Door.Constructor(Vector3(1320.134f, 1190.94f, 77.77f)), owning_building_guid = 212)
      LocalObject(890, Door.Constructor(Vector3(1320.134f, 1214.94f, 77.77f)), owning_building_guid = 212)
      LocalObject(892, Door.Constructor(Vector3(1332.14f, 1202.914f, 77.77f)), owning_building_guid = 212)
      LocalObject(899, Door.Constructor(Vector3(1351.944f, 1239.191f, 72.829f)), owning_building_guid = 212)
      LocalObject(901, Door.Constructor(Vector3(1364.394f, 1178.914f, 72.829f)), owning_building_guid = 212)
      LocalObject(1068, Terminal.Constructor(Vector3(1311.442f, 1208.663f, 76.063f), vanu_equipment_term), owning_building_guid = 212)
      LocalObject(1069, Terminal.Constructor(Vector3(1311.514f, 1197.095f, 76.067f), vanu_equipment_term), owning_building_guid = 212)
      LocalObject(1071, Terminal.Constructor(Vector3(1314.254f, 1211.47f, 76.067f), vanu_equipment_term), owning_building_guid = 212)
      LocalObject(1072, Terminal.Constructor(Vector3(1314.321f, 1194.283f, 76.063f), vanu_equipment_term), owning_building_guid = 212)
      LocalObject(1074, Terminal.Constructor(Vector3(1326.075f, 1194.358f, 76.067f), vanu_equipment_term), owning_building_guid = 212)
      LocalObject(1075, Terminal.Constructor(Vector3(1326.009f, 1211.545f, 76.063f), vanu_equipment_term), owning_building_guid = 212)
      LocalObject(1076, Terminal.Constructor(Vector3(1328.821f, 1197.14f, 76.063f), vanu_equipment_term), owning_building_guid = 212)
      LocalObject(1077, Terminal.Constructor(Vector3(1328.816f, 1208.733f, 76.067f), vanu_equipment_term), owning_building_guid = 212)
      LocalObject(1098, SpawnTube.Constructor(Vector3(1320.15f, 1202.93f, 76.189f), Vector3(0, 0, 180)), owning_building_guid = 212)
      LocalObject(972, Painbox.Constructor(Vector3(1319.863f, 1202.629f, 85.3918f), painbox_continuous), owning_building_guid = 212)
      LocalObject(978, Painbox.Constructor(Vector3(1305.837f, 1203.037f, 79.34f), painbox_door_radius_continuous), owning_building_guid = 212)
      LocalObject(979, Painbox.Constructor(Vector3(1319.839f, 1189.257f, 79.94f), painbox_door_radius_continuous), owning_building_guid = 212)
      LocalObject(980, Painbox.Constructor(Vector3(1320.192f, 1216.896f, 79.94f), painbox_door_radius_continuous), owning_building_guid = 212)
      LocalObject(981, Painbox.Constructor(Vector3(1334.933f, 1203.317f, 79.94f), painbox_door_radius_continuous), owning_building_guid = 212)
    }

    Building10012()

    def Building10012(): Unit = { // Name: Core Type: vanu_core GUID: 213, MapID: 10012
      LocalBuilding("Core", 213, 10012, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1026.95f, 1013.73f, 97.73f), vanu_core)))
      LocalObject(827, Door.Constructor(Vector3(994.457f, 1001.752f, 104.518f)), owning_building_guid = 213)
      LocalObject(831, Door.Constructor(Vector3(1022.928f, 973.237f, 104.518f)), owning_building_guid = 213)
      LocalObject(832, Door.Constructor(Vector3(1022.928f, 973.237f, 109.518f)), owning_building_guid = 213)
      LocalObject(833, Door.Constructor(Vector3(1022.972f, 1030.223f, 109.518f)), owning_building_guid = 213)
      LocalObject(834, Door.Constructor(Vector3(1022.972f, 1030.223f, 114.518f)), owning_building_guid = 213)
      LocalObject(838, Door.Constructor(Vector3(1051.443f, 1001.708f, 114.518f)), owning_building_guid = 213)
    }

    Building10156()

    def Building10156(): Unit = { // Name: NW_ATPlant Type: vanu_vehicle_station GUID: 256, MapID: 10156
      LocalBuilding("NW_ATPlant", 256, 10156, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(772.18f, 1217.7f, 43.18f), vanu_vehicle_station)))
      LocalObject(1024, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 256)
      LocalObject(794, Door.Constructor(Vector3(761.205f, 1206.612f, 44.959f)), owning_building_guid = 256)
      LocalObject(797, Door.Constructor(Vector3(779.6216f, 1189.222f, 64.871f)), owning_building_guid = 256)
      LocalObject(801, Door.Constructor(Vector3(801.209f, 1182.579f, 74.959f)), owning_building_guid = 256)
      LocalObject(803, Door.Constructor(Vector3(808.4747f, 1186.411f, 74.939f)), owning_building_guid = 256)
      LocalObject(804, Door.Constructor(Vector3(811.1083f, 1233.146f, 64.883f)), owning_building_guid = 256)
      LocalObject(805, Door.Constructor(Vector3(815.5405f, 1190.205f, 74.939f)), owning_building_guid = 256)
      LocalObject(806, Door.Constructor(Vector3(818.1613f, 1236.896f, 44.959f)), owning_building_guid = 256)
      LocalObject(807, Door.Constructor(Vector3(822.8236f, 1194.072f, 74.959f)), owning_building_guid = 256)
      LocalObject(913, Door.Constructor(Vector3(817.9047f, 1168.759f, 49.813f)), owning_building_guid = 256)
      LocalObject(1017, Terminal.Constructor(Vector3(780.8441f, 1200.851f, 63.097f), vanu_air_vehicle_term), owning_building_guid = 256)
      LocalObject(1099, VehicleSpawnPad.Constructor(Vector3(785.5449f, 1208.388f, 63.096f), vanu_vehicle_creation_pad, Vector3(0, 0, -28)), owning_building_guid = 256, terminal_guid = 1017)
      LocalObject(1018, Terminal.Constructor(Vector3(812.0989f, 1217.48f, 63.097f), vanu_air_vehicle_term), owning_building_guid = 256)
      LocalObject(1100, VehicleSpawnPad.Constructor(Vector3(803.218f, 1217.785f, 63.096f), vanu_vehicle_creation_pad, Vector3(0, 0, -28)), owning_building_guid = 256, terminal_guid = 1018)
      LocalObject(1052, Terminal.Constructor(Vector3(789.0759f, 1197.368f, 45.68f), vanu_equipment_term), owning_building_guid = 256)
      LocalObject(1053, Terminal.Constructor(Vector3(810.2666f, 1208.635f, 45.68f), vanu_equipment_term), owning_building_guid = 256)
      LocalObject(1105, Terminal.Constructor(Vector3(802.3574f, 1198.242f, 48.18f), vanu_vehicle_term), owning_building_guid = 256)
      LocalObject(1101, VehicleSpawnPad.Constructor(Vector3(809.3684f, 1184.826f, 45.585f), vanu_vehicle_creation_pad, Vector3(0, 0, 152)), owning_building_guid = 256, terminal_guid = 1105)
    }

    Building10007()

    def Building10007(): Unit = { // Name: SE_ATPlant Type: vanu_vehicle_station GUID: 257, MapID: 10007
      LocalBuilding("SE_ATPlant", 257, 10007, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1352.53f, 780.12f, 43.78f), vanu_vehicle_station)))
      LocalObject(1025, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 257)
      LocalObject(878, Door.Constructor(Vector3(1298.814f, 795.5347f, 75.559f)), owning_building_guid = 257)
      LocalObject(879, Door.Constructor(Vector3(1305.402f, 800.4932f, 75.539f)), owning_building_guid = 257)
      LocalObject(881, Door.Constructor(Vector3(1310.118f, 753.9674f, 45.559f)), owning_building_guid = 257)
      LocalObject(882, Door.Constructor(Vector3(1311.787f, 805.3461f, 75.539f)), owning_building_guid = 257)
      LocalObject(887, Door.Constructor(Vector3(1316.497f, 758.7747f, 65.483f)), owning_building_guid = 257)
      LocalObject(888, Door.Constructor(Vector3(1318.364f, 810.2671f, 75.559f)), owning_building_guid = 257)
      LocalObject(895, Door.Constructor(Vector3(1340.725f, 807.0831f, 65.471f)), owning_building_guid = 257)
      LocalObject(900, Door.Constructor(Vector3(1361.635f, 792.7887f, 45.559f)), owning_building_guid = 257)
      LocalObject(914, Door.Constructor(Vector3(1299.712f, 821.3058f, 50.413f)), owning_building_guid = 257)
      LocalObject(1019, Terminal.Constructor(Vector3(1313.068f, 774.0926f, 63.697f), vanu_air_vehicle_term), owning_building_guid = 257)
      LocalObject(1103, VehicleSpawnPad.Constructor(Vector3(1321.888f, 775.1802f, 63.696f), vanu_vehicle_creation_pad, Vector3(0, 0, 143)), owning_building_guid = 257, terminal_guid = 1019)
      LocalObject(1020, Terminal.Constructor(Vector3(1341.337f, 795.4058f, 63.697f), vanu_air_vehicle_term), owning_building_guid = 257)
      LocalObject(1104, VehicleSpawnPad.Constructor(Vector3(1337.873f, 787.2261f, 63.696f), vanu_vehicle_creation_pad, Vector3(0, 0, 143)), owning_building_guid = 257, terminal_guid = 1020)
      LocalObject(1070, Terminal.Constructor(Vector3(1313.494f, 783.1147f, 46.28f), vanu_equipment_term), owning_building_guid = 257)
      LocalObject(1078, Terminal.Constructor(Vector3(1332.662f, 797.5583f, 46.28f), vanu_equipment_term), owning_building_guid = 257)
      LocalObject(1106, Terminal.Constructor(Vector3(1319.68f, 794.6179f, 48.78f), vanu_vehicle_term), owning_building_guid = 257)
      LocalObject(1102, VehicleSpawnPad.Constructor(Vector3(1310.657f, 806.7718f, 46.185f), vanu_vehicle_creation_pad, Vector3(0, 0, -37)), owning_building_guid = 257, terminal_guid = 1106)
    }

    Building10212()

    def Building10212(): Unit = { // Name: GW_Cavern3_W Type: warpgate_cavern GUID: 258, MapID: 10212
      LocalBuilding("GW_Cavern3_W", 258, 10212, FoundationBuilder(WarpGate.Structure(Vector3(154.12f, 833.73f, 32.98f))))
    }

    Building10405()

    def Building10405(): Unit = { // Name: GW_Cavern3_S Type: warpgate_cavern GUID: 259, MapID: 10405
      LocalBuilding("GW_Cavern3_S", 259, 10405, FoundationBuilder(WarpGate.Structure(Vector3(949.84f, 285.73f, 29.82f))))
    }

    Building10214()

    def Building10214(): Unit = { // Name: GW_Cavern3_N Type: warpgate_cavern GUID: 260, MapID: 10214
      LocalBuilding("GW_Cavern3_N", 260, 10214, FoundationBuilder(WarpGate.Structure(Vector3(1128.81f, 1755.28f, 14.5f))))
    }

    Building10215()

    def Building10215(): Unit = { // Name: GW_Cavern3_E Type: warpgate_cavern GUID: 261, MapID: 10215
      LocalBuilding("GW_Cavern3_E", 261, 10215, FoundationBuilder(WarpGate.Structure(Vector3(1889.78f, 1018.32f, 33.14f))))
    }

    ZoneOwnedObjects()

    def ZoneOwnedObjects(): Unit = {
      LocalObject(1028, Terminal.Constructor(Vector3(624.28f, 958.12f, 74.56f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1029, Terminal.Constructor(Vector3(624.38f, 975.77f, 74.56f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1030, Terminal.Constructor(Vector3(634.07f, 835.13f, 80.36f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1031, Terminal.Constructor(Vector3(647.92f, 794.89f, 80.36f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1040, Terminal.Constructor(Vector3(684.98f, 878.61f, 107.18f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1041, Terminal.Constructor(Vector3(689.91f, 646.49f, 76.69f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1042, Terminal.Constructor(Vector3(704.82f, 1254.2f, 88.18f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1043, Terminal.Constructor(Vector3(710.23f, 1277.36f, 88.11f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1044, Terminal.Constructor(Vector3(711.35f, 1221.54f, 43.33f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1046, Terminal.Constructor(Vector3(719.63f, 809.77f, 91.26f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1048, Terminal.Constructor(Vector3(725.86f, 1196.85f, 43.3f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1049, Terminal.Constructor(Vector3(769.95f, 1078.39f, 99.83f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1050, Terminal.Constructor(Vector3(780.02f, 670.64f, 112.67f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1051, Terminal.Constructor(Vector3(788.59f, 1093.02f, 99.81f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1054, Terminal.Constructor(Vector3(844.02f, 778.6f, 106.01f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1055, Terminal.Constructor(Vector3(846.68f, 782.07f, 111.52f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1056, Terminal.Constructor(Vector3(892.74f, 1362.69f, 43.29f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1057, Terminal.Constructor(Vector3(901.96f, 911.49f, 110.81f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1058, Terminal.Constructor(Vector3(913.09f, 1382.92f, 43.27f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1059, Terminal.Constructor(Vector3(931.79f, 694.1f, 81.75f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1060, Terminal.Constructor(Vector3(939.31f, 922.44f, 110.81f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1061, Terminal.Constructor(Vector3(965.76f, 756.59f, 85.54f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1062, Terminal.Constructor(Vector3(970.27f, 1330.25f, 72.43f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1063, Terminal.Constructor(Vector3(1044.21f, 1256.84f, 72.9f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1064, Terminal.Constructor(Vector3(1064.8f, 1236.89f, 72.91f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1065, Terminal.Constructor(Vector3(1134.99f, 739.94f, 107.09f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1066, Terminal.Constructor(Vector3(1226.99f, 1239f, 96.63f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1067, Terminal.Constructor(Vector3(1239.95f, 1091.6f, 109.43f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1073, Terminal.Constructor(Vector3(1318.38f, 909.65f, 59.3f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1079, Terminal.Constructor(Vector3(1362.64f, 657.92f, 88.17f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1080, Terminal.Constructor(Vector3(1387.46f, 783.22f, 43.32f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1081, Terminal.Constructor(Vector3(1388.46f, 1124.9f, 122.73f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(1084, Terminal.Constructor(Vector3(1407.92f, 763.14f, 43.3f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(43, ProximityTerminal.Constructor(Vector3(671.13f, 701.13f, 69.9f), crystals_health_a), owning_building_guid = 0)
      LocalObject(44, ProximityTerminal.Constructor(Vector3(737.38f, 1261.19f, 51.62f), crystals_health_a), owning_building_guid = 0)
      LocalObject(45, ProximityTerminal.Constructor(Vector3(1020.78f, 1217.26f, 64.92f), crystals_health_a), owning_building_guid = 0)
      LocalObject(46, ProximityTerminal.Constructor(Vector3(1340.04f, 1166.81f, 69.59f), crystals_health_a), owning_building_guid = 0)
      LocalObject(47, ProximityTerminal.Constructor(Vector3(579.33f, 1056.62f, 41.42f), crystals_health_b), owning_building_guid = 0)
      LocalObject(48, ProximityTerminal.Constructor(Vector3(636.55f, 1292.57f, 41.36f), crystals_health_b), owning_building_guid = 0)
      LocalObject(49, ProximityTerminal.Constructor(Vector3(687.24f, 830.88f, 70.1f), crystals_health_b), owning_building_guid = 0)
      LocalObject(50, ProximityTerminal.Constructor(Vector3(743.37f, 1368.45f, 59.02f), crystals_health_b), owning_building_guid = 0)
      LocalObject(51, ProximityTerminal.Constructor(Vector3(756.64f, 1114.73f, 59f), crystals_health_b), owning_building_guid = 0)
      LocalObject(52, ProximityTerminal.Constructor(Vector3(913.82f, 1019.54f, 61.84f), crystals_health_b), owning_building_guid = 0)
      LocalObject(53, ProximityTerminal.Constructor(Vector3(928.1f, 1348.9f, 41.36f), crystals_health_b), owning_building_guid = 0)
      LocalObject(54, ProximityTerminal.Constructor(Vector3(1108.67f, 1281.84f, 41.36f), crystals_health_b), owning_building_guid = 0)
      LocalObject(55, ProximityTerminal.Constructor(Vector3(1184.81f, 1112.77f, 41.36f), crystals_health_b), owning_building_guid = 0)
      LocalObject(56, ProximityTerminal.Constructor(Vector3(1286.46f, 1233.98f, 69.59f), crystals_health_b), owning_building_guid = 0)
      LocalObject(57, ProximityTerminal.Constructor(Vector3(1342.38f, 1104.26f, 67.85f), crystals_health_b), owning_building_guid = 0)
      LocalObject(58, ProximityTerminal.Constructor(Vector3(1436.35f, 841.23f, 68.56f), crystals_health_b), owning_building_guid = 0)
      LocalObject(942, ProximityTerminal.Constructor(Vector3(930.26f, 904.42f, 105.99f), crystals_health_a), owning_building_guid = 0)
      LocalObject(943, ProximityTerminal.Constructor(Vector3(930.97f, 903.87f, 106.04f), crystals_health_a), owning_building_guid = 0)
      LocalObject(944, ProximityTerminal.Constructor(Vector3(946.33f, 474.23f, 47.38f), crystals_health_a), owning_building_guid = 0)
      LocalObject(945, ProximityTerminal.Constructor(Vector3(991.84f, 757.6f, 85.29f), crystals_health_a), owning_building_guid = 0)
      LocalObject(946, ProximityTerminal.Constructor(Vector3(992.69f, 757.95f, 85.39f), crystals_health_a), owning_building_guid = 0)
      LocalObject(947, ProximityTerminal.Constructor(Vector3(993.29f, 755.79f, 85.34f), crystals_health_a), owning_building_guid = 0)
      LocalObject(948, ProximityTerminal.Constructor(Vector3(998.74f, 1229.32f, 107.18f), crystals_health_a), owning_building_guid = 0)
      LocalObject(949, ProximityTerminal.Constructor(Vector3(1144.18f, 1543.48f, 47.43f), crystals_health_a), owning_building_guid = 0)
      LocalObject(950, ProximityTerminal.Constructor(Vector3(1287.42f, 693.51f, 84.73f), crystals_health_a), owning_building_guid = 0)
      LocalObject(951, ProximityTerminal.Constructor(Vector3(1402.34f, 845.88f, 98.92f), crystals_health_a), owning_building_guid = 0)
      LocalObject(952, ProximityTerminal.Constructor(Vector3(709.62f, 964.7f, 54.77f), crystals_health_b), owning_building_guid = 0)
      LocalObject(953, ProximityTerminal.Constructor(Vector3(727.34f, 816.47f, 91.39f), crystals_health_b), owning_building_guid = 0)
      LocalObject(954, ProximityTerminal.Constructor(Vector3(992.66f, 756.8f, 85.39f), crystals_health_b), owning_building_guid = 0)
      LocalObject(955, ProximityTerminal.Constructor(Vector3(1074.65f, 1304.35f, 73.4f), crystals_health_b), owning_building_guid = 0)
      LocalObject(956, ProximityTerminal.Constructor(Vector3(1076.72f, 1310.3f, 73.4f), crystals_health_b), owning_building_guid = 0)
      LocalObject(957, ProximityTerminal.Constructor(Vector3(1238.29f, 1314.99f, 97.59f), crystals_health_b), owning_building_guid = 0)
      LocalObject(958, ProximityTerminal.Constructor(Vector3(1246.89f, 776.94f, 95.32f), crystals_health_b), owning_building_guid = 0)
      LocalObject(214, FacilityTurret.Constructor(Vector3(671.05f, 1022.46f, 62.98f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(214, 5000)
      LocalObject(215, FacilityTurret.Constructor(Vector3(681.18f, 1080.93f, 59f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(215, 5001)
      LocalObject(216, FacilityTurret.Constructor(Vector3(689.3f, 651.45f, 95f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(216, 5002)
      LocalObject(217, FacilityTurret.Constructor(Vector3(701.81f, 793.95f, 69.9f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(217, 5003)
      LocalObject(218, FacilityTurret.Constructor(Vector3(728.17f, 881.62f, 69.9f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(218, 5004)
      LocalObject(219, FacilityTurret.Constructor(Vector3(736.87f, 700.59f, 87.5f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(219, 5005)
      LocalObject(220, FacilityTurret.Constructor(Vector3(746.13f, 1229.55f, 41.36f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(220, 5006)
      LocalObject(221, FacilityTurret.Constructor(Vector3(746.73f, 1093.17f, 59f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(221, 5007)
      LocalObject(222, FacilityTurret.Constructor(Vector3(748.15f, 766.38f, 87.5f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(222, 5008)
      LocalObject(223, FacilityTurret.Constructor(Vector3(854.55f, 1248.58f, 66.27f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(223, 5009)
      LocalObject(224, FacilityTurret.Constructor(Vector3(864.98f, 1320.1f, 66.27f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(224, 5010)
      LocalObject(225, FacilityTurret.Constructor(Vector3(877.91f, 1173.83f, 68.41f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(225, 5011)
      LocalObject(226, FacilityTurret.Constructor(Vector3(883.97f, 1017.33f, 54.38f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(226, 5012)
      LocalObject(227, FacilityTurret.Constructor(Vector3(901.31f, 983.05f, 61.74f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(227, 5013)
      LocalObject(228, FacilityTurret.Constructor(Vector3(909.48f, 918.12f, 61.74f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(228, 5014)
      LocalObject(229, FacilityTurret.Constructor(Vector3(917.26f, 787.24f, 63.97f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(229, 5015)
      LocalObject(230, FacilityTurret.Constructor(Vector3(939.49f, 1060.28f, 61.64f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(230, 5016)
      LocalObject(231, FacilityTurret.Constructor(Vector3(941.77f, 1113.86f, 54.38f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(231, 5017)
      LocalObject(232, FacilityTurret.Constructor(Vector3(982.77f, 1110.81f, 61.84f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(232, 5018)
      LocalObject(233, FacilityTurret.Constructor(Vector3(985.8f, 895.48f, 61.84f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(233, 5019)
      LocalObject(234, FacilityTurret.Constructor(Vector3(996.73f, 784.5f, 63.97f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(234, 5020)
      LocalObject(235, FacilityTurret.Constructor(Vector3(1002.9f, 1166.68f, 52.22f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(235, 5021)
      LocalObject(236, FacilityTurret.Constructor(Vector3(1014.38f, 1226.21f, 64.92f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(236, 5022)
      LocalObject(237, FacilityTurret.Constructor(Vector3(1028.06f, 669.31f, 62.98f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(237, 5023)
      LocalObject(238, FacilityTurret.Constructor(Vector3(1056.63f, 874.36f, 61.84f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(238, 5024)
      LocalObject(239, FacilityTurret.Constructor(Vector3(1069.55f, 1275.28f, 62.98f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(239, 5025)
      LocalObject(240, FacilityTurret.Constructor(Vector3(1076.77f, 1068.19f, 62.04f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(240, 5026)
      LocalObject(241, FacilityTurret.Constructor(Vector3(1082.69f, 1164.76f, 64.92f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(241, 5027)
      LocalObject(242, FacilityTurret.Constructor(Vector3(1105.3f, 1003.03f, 62.24f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(242, 5028)
      LocalObject(243, FacilityTurret.Constructor(Vector3(1119.23f, 1353.03f, 52.22f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(243, 5029)
      LocalObject(244, FacilityTurret.Constructor(Vector3(1124.97f, 1310.46f, 62.98f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(244, 5030)
      LocalObject(245, FacilityTurret.Constructor(Vector3(1157.82f, 955.18f, 61.94f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(245, 5031)
      LocalObject(246, FacilityTurret.Constructor(Vector3(1178.75f, 1243.1f, 85.48f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(246, 5032)
      LocalObject(247, FacilityTurret.Constructor(Vector3(1193.11f, 1291.49f, 69.59f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(247, 5033)
      LocalObject(248, FacilityTurret.Constructor(Vector3(1262.41f, 1378.94f, 54.88f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(248, 5034)
      LocalObject(249, FacilityTurret.Constructor(Vector3(1265.56f, 1152.95f, 69.59f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(249, 5035)
      LocalObject(250, FacilityTurret.Constructor(Vector3(1267.66f, 912.72f, 59.3f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(250, 5036)
      LocalObject(251, FacilityTurret.Constructor(Vector3(1270.57f, 730.4f, 66.27f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(251, 5037)
      LocalObject(252, FacilityTurret.Constructor(Vector3(1271.14f, 1252.61f, 85.48f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(252, 5038)
      LocalObject(253, FacilityTurret.Constructor(Vector3(1334.21f, 1161.61f, 69.59f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(253, 5039)
      LocalObject(254, FacilityTurret.Constructor(Vector3(1353.22f, 911.37f, 59f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(254, 5040)
      LocalObject(255, FacilityTurret.Constructor(Vector3(1364.71f, 1255.48f, 68.94f), vanu_sentry_turret), owning_building_guid = 0)
      TurretToWeapon(255, 5041)
    }

    def Lattice(): Unit = {
      LatticeLink("NW_Redoubt", "NW_ATPlant")
      LatticeLink("GW_Cavern3_W", "NW_ATPlant")
      LatticeLink("NE_Stasis", "Core")
      LatticeLink("SW_Stasis", "Core")
      LatticeLink("SE_Redoubt", "SE_ATPlant")
      LatticeLink("NW_Redoubt", "NE_Stasis")
      LatticeLink("SE_Redoubt", "SW_Stasis")
      LatticeLink("SW_Stasis", "NW_ATPlant")
      LatticeLink("NE_Stasis", "SE_ATPlant")
      LatticeLink("GW_Cavern3_N", "NW_Redoubt")
      LatticeLink("GW_Cavern3_S", "SE_Redoubt")
      LatticeLink("GW_Cavern3_E", "SE_ATPlant")
    }

    Lattice()

    def ZipLines(): Unit = {
      ZipLinePaths(new ZipLinePath(1, true, List(Vector3(595.769f, 971.013f, 68.367f), Vector3(617.24f, 982.719f, 85.965f))))
      ZipLinePaths(new ZipLinePath(2, false, List(Vector3(597.42f, 911.907f, 87.951f), Vector3(611.503f, 936.96f, 72.997f))))
      ZipLinePaths(new ZipLinePath(3, true, List(Vector3(622.076f, 1023.44f, 63.331f), Vector3(622.719f, 1018.502f, 93.332f))))
      ZipLinePaths(new ZipLinePath(4, false, List(Vector3(628.066f, 653.647f, 80.947f), Vector3(652.793f, 654.85f, 98.078f), Vector3(677.121f, 654.452f, 121.481f), Vector3(710.524f, 646.688f, 122.73f), Vector3(744.227f, 642.925f, 123.98f))))
      ZipLinePaths(new ZipLinePath(5, false, List(Vector3(631.571f, 1017.425f, 93.82f), Vector3(653.945f, 1018.949f, 91.508f), Vector3(676.32f, 1020.473f, 70.296f), Vector3(688.877f, 1021.349f, 58.994f), Vector3(701.634f, 1022.026f, 51.414f))))
      ZipLinePaths(new ZipLinePath(6, false, List(Vector3(639.683f, 966.037f, 84.225f), Vector3(689.438f, 956.275f, 90.453f), Vector3(739.193f, 946.514f, 95.94f), Vector3(788.948f, 936.752f, 101.427f), Vector3(838.703f, 926.99f, 106.914f), Vector3(883.58f, 918.186f, 111.693f))))
      ZipLinePaths(new ZipLinePath(7, false, List(Vector3(641.629f, 1156.085f, 64.865f), Vector3(665.451f, 1200.132f, 77.38f), Vector3(688.672f, 1243.979f, 89.156f), Vector3(690.138f, 1247.258f, 89.3f))))
      ZipLinePaths(new ZipLinePath(8, true, List(Vector3(647.373f, 1216.143f, 59.351f), Vector3(649.375f, 1217.21f, 92.651f))))
      ZipLinePaths(new ZipLinePath(9, false, List(Vector3(656.468f, 1211.522f, 93.177f), Vector3(681.191f, 1213.828f, 92.881f), Vector3(705.915f, 1216.135f, 82.302f), Vector3(730.384f, 1218.306f, 73.437f), Vector3(754.853f, 1220.477f, 67.488f))))
      ZipLinePaths(new ZipLinePath(10, false, List(Vector3(664.549f, 761.402f, 102.108f), Vector3(675.283f, 781.529f, 101.954f), Vector3(696.017f, 800.657f, 94.508f), Vector3(698.568f, 805.866f, 92.115f))))
      ZipLinePaths(new ZipLinePath(11, false, List(Vector3(667.071f, 1071.46f, 59.851f), Vector3(640.825f, 1029.389f, 67.036f), Vector3(639.251f, 1026.864f, 63.832f))))
      ZipLinePaths(new ZipLinePath(12, true, List(Vector3(681.428f, 851.663f, 102.023f), Vector3(682.274f, 857.674f, 107.525f))))
      ZipLinePaths(new ZipLinePath(13, false, List(Vector3(688.419f, 814.561f, 92.096f), Vector3(651.401f, 848.164f, 92.076f), Vector3(614.383f, 881.766f, 91.308f), Vector3(600.317f, 894.536f, 87.967f))))
      ZipLinePaths(new ZipLinePath(14, false, List(Vector3(690.738f, 1071.926f, 59.857f), Vector3(718.545f, 1031.94f, 51.832f))))
      ZipLinePaths(new ZipLinePath(15, false, List(Vector3(704.126f, 1290.795f, 89.088f), Vector3(725.776f, 1332.843f, 70.701f), Vector3(733.841f, 1348.508f, 59.9f))))
      ZipLinePaths(new ZipLinePath(16, false, List(Vector3(709.91f, 748.459f, 89.816f), Vector3(718.399f, 769.508f, 85.932f), Vector3(726.888f, 790.558f, 75.963f))))
      ZipLinePaths(new ZipLinePath(17, false, List(Vector3(716.417f, 703.283f, 88.381f), Vector3(693.333f, 697.461f, 86.535f), Vector3(670.25f, 691.64f, 73.819f), Vector3(667.48f, 690.942f, 70.78f))))
      ZipLinePaths(new ZipLinePath(18, false, List(Vector3(716.858f, 1244.631f, 89.045f), Vector3(728.226f, 1196.511f, 97.219f), Vector3(734.592f, 1169.563f, 99.721f))))
      ZipLinePaths(new ZipLinePath(19, false, List(Vector3(717.701f, 634.43f, 104.6f), Vector3(687.153f, 675.26f, 104.696f), Vector3(662.594f, 708.085f, 102.1f))))
      ZipLinePaths(new ZipLinePath(20, true, List(Vector3(718.911f, 629.346f, 70.251f), Vector3(717.944f, 629.452f, 104.1f))))
      ZipLinePaths(new ZipLinePath(21, true, List(Vector3(727.332f, 763.987f, 84.168f), Vector3(744.248f, 750.002f, 87.851f))))
      ZipLinePaths(new ZipLinePath(22, false, List(Vector3(732.261f, 1124.261f, 99.745f), Vector3(727.12f, 1101.174f, 100.445f), Vector3(721.979f, 1078.088f, 84.275f), Vector3(711.698f, 1031.915f, 68.074f), Vector3(702.623f, 1001.287f, 55.689f))))
      ZipLinePaths(new ZipLinePath(23, false, List(Vector3(732.939f, 1112.987f, 59.85f), Vector3(694.976f, 1114.277f, 59.85f))))
      ZipLinePaths(new ZipLinePath(24, false, List(Vector3(733.315f, 1043.086f, 44.023f), Vector3(783.468f, 1035.628f, 50.227f), Vector3(833.622f, 1028.17f, 55.683f), Vector3(883.775f, 1020.711f, 61.138f), Vector3(905.443f, 1018.487f, 62.496f))))
      ZipLinePaths(new ZipLinePath(25, false, List(Vector3(734.343f, 605.285f, 69.928f), Vector3(750.482f, 605.113f, 67.796f), Vector3(766.621f, 604.942f, 53.571f))))
      ZipLinePaths(new ZipLinePath(26, false, List(Vector3(737.579f, 931.404f, 51.22f), Vector3(716.598f, 908.304f, 73.219f), Vector3(709.877f, 905.151f, 70.799f))))
      ZipLinePaths(new ZipLinePath(27, false, List(Vector3(746.348f, 668.394f, 122.776f), Vector3(740.464f, 689.729f, 113.792f), Vector3(735.78f, 711.064f, 93.308f), Vector3(735.132f, 714.654f, 88.352f))))
      ZipLinePaths(new ZipLinePath(28, true, List(Vector3(756.1f, 1155.746f, 99.22f), Vector3(760.176f, 1186.748f, 76.063f))))
      ZipLinePaths(new ZipLinePath(29, true, List(Vector3(758.959f, 1194.575f, 63.456f), Vector3(764.26f, 1195.303f, 46.03f))))
      ZipLinePaths(new ZipLinePath(30, false, List(Vector3(759.687f, 1211.316f, 63.947f), Vector3(710.624f, 1201.801f, 63.107f), Vector3(692.961f, 1198.375f, 59.852f))))
      ZipLinePaths(new ZipLinePath(31, false, List(Vector3(764.413f, 1133.386f, 99.716f), Vector3(783.198f, 1108.766f, 100.661f))))
      ZipLinePaths(new ZipLinePath(32, false, List(Vector3(765.981f, 1159.159f, 99.716f), Vector3(810.625f, 1183.744f, 102.261f), Vector3(855.268f, 1211.829f, 104.064f), Vector3(870.587f, 1219.715f, 104.201f), Vector3(885.906f, 1225.201f, 104.337f))))
      ZipLinePaths(new ZipLinePath(33, false, List(Vector3(771.91f, 1172.993f, 63.947f), Vector3(723.732f, 1159.705f, 63.095f), Vector3(690.971f, 1150.67f, 59.851f))))
      ZipLinePaths(new ZipLinePath(34, true, List(Vector3(776.173f, 1384.778f, 59.389f), Vector3(775.563f, 1385.205f, 93.17f))))
      ZipLinePaths(new ZipLinePath(35, true, List(Vector3(776.538f, 590.261f, 41.751f), Vector3(770.792f, 585.914f, 54.699f))))
      ZipLinePaths(new ZipLinePath(36, false, List(Vector3(778.127f, 1380.551f, 93.692f), Vector3(780.733f, 1332.62f, 80.418f), Vector3(783.339f, 1284.689f, 66.407f), Vector3(784.33f, 1266.475f, 59.124f))))
      ZipLinePaths(new ZipLinePath(37, false, List(Vector3(779.095f, 1230.403f, 63.946f), Vector3(739.117f, 1262.004f, 62.663f), Vector3(706.979f, 1287.409f, 59.851f))))
      ZipLinePaths(new ZipLinePath(38, false, List(Vector3(790.673f, 706.23f, 124.487f), Vector3(826.321f, 741.589f, 115.105f), Vector3(834.85f, 750.027f, 114.023f))))
      ZipLinePaths(new ZipLinePath(39, false, List(Vector3(792.934f, 1321.527f, 59.855f), Vector3(842.515f, 1314.349f, 70.136f), Vector3(845.431f, 1313.927f, 67.129f))))
      ZipLinePaths(new ZipLinePath(40, true, List(Vector3(804.465f, 725.206f, 70.251f), Vector3(796.278f, 734.802f, 87.85f))))
      ZipLinePaths(new ZipLinePath(41, false, List(Vector3(826.335f, 762.948f, 110.827f), Vector3(781.175f, 783.752f, 106.282f), Vector3(751.369f, 797.483f, 101.617f))))
      ZipLinePaths(new ZipLinePath(42, true, List(Vector3(826.402f, 1227.598f, 46.03f), Vector3(831.918f, 1228.608f, 63.456f))))
      ZipLinePaths(new ZipLinePath(43, false, List(Vector3(840.839f, 1179.419f, 74.023f), Vector3(884.232f, 1154.925f, 70.614f), Vector3(892.042f, 1150.516f, 69.24f))))
      ZipLinePaths(new ZipLinePath(44, false, List(Vector3(856.866f, 760.258f, 106.973f), Vector3(863.339f, 738.383f, 103.32f), Vector3(869.812f, 716.508f, 92.22f), Vector3(882.757f, 672.758f, 66.763f), Vector3(883.534f, 670.133f, 62.646f))))
      ZipLinePaths(new ZipLinePath(45, false, List(Vector3(863.236f, 1245.084f, 105.631f), Vector3(846.791f, 1264.905f, 103.229f), Vector3(832.947f, 1284.727f, 93.958f), Vector3(811.857f, 1324.369f, 73.568f), Vector3(794.902f, 1350.533f, 61.081f))))
      ZipLinePaths(new ZipLinePath(46, true, List(Vector3(864.625f, 1267.214f, 66.624f), Vector3(863.782f, 1265.734f, 97.64f))))
      ZipLinePaths(new ZipLinePath(47, false, List(Vector3(866.119f, 1262.615f, 98.148f), Vector3(839.194f, 1221.252f, 90.849f), Vector3(818.73f, 1189.815f, 82.843f))))
      ZipLinePaths(new ZipLinePath(48, false, List(Vector3(867.992f, 776.508f, 112.423f), Vector3(897.561f, 733.592f, 107.972f), Vector3(921.895f, 700.86f, 101.848f))))
      ZipLinePaths(new ZipLinePath(49, false, List(Vector3(874.239f, 963.551f, 62.593f), Vector3(846.249f, 1006.177f, 62.656f), Vector3(818.258f, 1048.804f, 61.98f), Vector3(790.268f, 1091.43f, 61.303f), Vector3(786.426f, 1097.281f, 59.851f))))
      ZipLinePaths(new ZipLinePath(50, true, List(Vector3(878.08f, 1211.27f, 68.74f), Vector3(879.584f, 1210.817f, 101.5f))))
      ZipLinePaths(new ZipLinePath(51, false, List(Vector3(883.719f, 1201.219f, 102.011f), Vector3(915.265f, 1162.523f, 105.556f), Vector3(946.811f, 1123.826f, 108.357f), Vector3(978.357f, 1085.13f, 111.159f), Vector3(1009.903f, 1046.434f, 113.96f), Vector3(1016.843f, 1037.921f, 113.549f))))
      ZipLinePaths(new ZipLinePath(52, false, List(Vector3(894.869f, 621.373f, 98.009f), Vector3(854.043f, 649.44f, 105.507f), Vector3(835.995f, 610.128f, 120.408f), Vector3(808.048f, 664.316f, 124.498f))))
      ZipLinePaths(new ZipLinePath(53, false, List(Vector3(895.912f, 1177.993f, 80.149f), Vector3(911.404f, 1158.136f, 80.364f), Vector3(926.896f, 1138.28f, 72.893f), Vector3(957.881f, 1098.567f, 62.501f))))
      ZipLinePaths(new ZipLinePath(54, false, List(Vector3(899.473f, 935f, 62.594f), Vector3(876.765f, 890.548f, 66.273f), Vector3(854.057f, 846.096f, 69.21f), Vector3(831.349f, 801.645f, 72.147f), Vector3(819.086f, 777.641f, 70.751f))))
      ZipLinePaths(new ZipLinePath(55, false, List(Vector3(908.183f, 913.064f, 100.237f), Vector3(867.304f, 885.33f, 93.219f), Vector3(826.426f, 857.596f, 85.468f), Vector3(785.548f, 829.861f, 77.717f), Vector3(764.291f, 815.439f, 70.758f))))
      ZipLinePaths(new ZipLinePath(56, false, List(Vector3(911.107f, 1174.015f, 69.257f), Vector3(961.534f, 1174.277f, 62.381f), Vector3(1011.962f, 1174.538f, 53.076f))))
      ZipLinePaths(new ZipLinePath(57, false, List(Vector3(917.5f, 902.31f, 100.231f), Vector3(877.638f, 869.806f, 98.397f), Vector3(838.975f, 838.202f, 95.82f), Vector3(800.312f, 806.598f, 93.243f), Vector3(761.649f, 774.994f, 90.667f), Vector3(757.956f, 768.465f, 88.352f))))
      ZipLinePaths(new ZipLinePath(58, false, List(Vector3(919.336f, 713.814f, 82.701f), Vector3(871.433f, 727.84f, 86.378f), Vector3(823.53f, 741.866f, 89.315f), Vector3(809.159f, 746.074f, 88.351f))))
      ZipLinePaths(new ZipLinePath(59, true, List(Vector3(920.936f, 918.429f, 98.892f), Vector3(930.88f, 917.027f, 106.39f))))
      ZipLinePaths(new ZipLinePath(60, true, List(Vector3(931.553f, 1025.102f, 68.29f), Vector3(986.02f, 1001.745f, 103.056f))))
      ZipLinePaths(new ZipLinePath(61, true, List(Vector3(942.175f, 878.239f, 62.093f), Vector3(943.987f, 879.733f, 96.131f))))
      ZipLinePaths(new ZipLinePath(62, false, List(Vector3(947.606f, 886.439f, 96.628f), Vector3(939.941f, 912.104f, 98.851f), Vector3(932.276f, 910.769f, 100.281f))))
      ZipLinePaths(new ZipLinePath(63, true, List(Vector3(948.062f, 1085.025f, 61.993f), Vector3(949.074f, 1086.391f, 96.35f))))
      ZipLinePaths(new ZipLinePath(64, false, List(Vector3(948.659f, 1092.809f, 96.85f), Vector3(970.915f, 1138.642f, 99.74f), Vector3(993.172f, 1184.475f, 101.885f), Vector3(1010.628f, 1220.422f, 102.44f))))
      ZipLinePaths(new ZipLinePath(65, false, List(Vector3(948.992f, 1063.97f, 71.392f), Vector3(937.88f, 1016.375f, 82.682f), Vector3(926.769f, 968.78f, 93.233f), Vector3(918.324f, 932.607f, 100.241f))))
      ZipLinePaths(new ZipLinePath(66, false, List(Vector3(953.017f, 912.695f, 111.692f), Vector3(966.054f, 864.624f, 108.046f), Vector3(979.091f, 816.552f, 103.66f), Vector3(991.085f, 772.327f, 97.368f))))
      ZipLinePaths(new ZipLinePath(67, false, List(Vector3(953.159f, 878.01f, 96.653f), Vector3(1001.65f, 866.321f, 86.763f), Vector3(1050.141f, 854.633f, 76.144f), Vector3(1098.632f, 842.944f, 65.526f), Vector3(1136.513f, 834.006f, 55.215f))))
      ZipLinePaths(new ZipLinePath(68, false, List(Vector3(954.303f, 919.458f, 111.69f), Vector3(995.314f, 949.714f, 110.578f), Vector3(1013.81f, 963.359f, 108.58f))))
      ZipLinePaths(new ZipLinePath(69, false, List(Vector3(976.326f, 1280.215f, 80.595f), Vector3(991.029f, 1263.616f, 100.993f), Vector3(1006.532f, 1246.817f, 102.503f))))
      ZipLinePaths(new ZipLinePath(70, false, List(Vector3(976.772f, 907.016f, 70.833f), Vector3(1027.604f, 902.932f, 71.139f), Vector3(1078.436f, 898.848f, 70.695f), Vector3(1129.268f, 894.764f, 70.252f), Vector3(1134.251f, 894.363f, 70.097f))))
      ZipLinePaths(new ZipLinePath(71, false, List(Vector3(980.673f, 1225.401f, 53.07f), Vector3(986.931f, 1201.197f, 53.07f))))
      ZipLinePaths(new ZipLinePath(72, false, List(Vector3(985.328f, 1009.551f, 103.617f), Vector3(962.989f, 1018.146f, 99.408f), Vector3(940.651f, 1026.742f, 86.734f), Vector3(895.973f, 1043.931f, 69.146f), Vector3(885.46f, 1047.976f, 62.529f))))
      ZipLinePaths(new ZipLinePath(73, false, List(Vector3(987.286f, 993.985f, 103.561f), Vector3(951.211f, 959.387f, 102.946f), Vector3(922.351f, 931.709f, 100.23f))))
      ZipLinePaths(new ZipLinePath(74, false, List(Vector3(991.669f, 1240.497f, 102.448f), Vector3(942.154f, 1246.661f, 106.414f), Vector3(922.348f, 1249.127f, 106.791f))))
      ZipLinePaths(new ZipLinePath(75, true, List(Vector3(997.886f, 1001.888f, 98.08f), Vector3(998.308f, 1001.92f, 108.08f))))
      ZipLinePaths(new ZipLinePath(76, false, List(Vector3(999.78f, 736.423f, 86.154f), Vector3(1033.248f, 727.68f, 80.234f))))
      ZipLinePaths(new ZipLinePath(77, true, List(Vector3(1000.418f, 720.566f, 63.331f), Vector3(1002.71f, 707.743f, 77.048f))))
      ZipLinePaths(new ZipLinePath(78, true, List(Vector3(1008.413f, 723.293f, 63.331f), Vector3(1017.74f, 732.348f, 63.331f))))
      ZipLinePaths(new ZipLinePath(79, true, List(Vector3(1010.377f, 989.51f, 103.08f), Vector3(1007.82f, 1015.348f, 113.08f))))
      ZipLinePaths(new ZipLinePath(80, false, List(Vector3(1015.78f, 1119.224f, 62.694f), Vector3(1017.258f, 1170.106f, 66.551f), Vector3(1017.635f, 1183.076f, 65.768f))))
      ZipLinePaths(new ZipLinePath(81, false, List(Vector3(1015.913f, 856.541f, 62.705f), Vector3(1015.379f, 807.652f, 73.915f), Vector3(1015.251f, 795.919f, 76.088f))))
      ZipLinePaths(new ZipLinePath(82, false, List(Vector3(1020.05f, 964.844f, 103.522f), Vector3(1014.081f, 915.391f, 99.916f), Vector3(1008.112f, 865.937f, 95.573f), Vector3(1002.142f, 816.483f, 91.229f), Vector3(998.583f, 781.355f, 86.198f))))
      ZipLinePaths(new ZipLinePath(83, false, List(Vector3(1021.393f, 1038.505f, 113.581f), Vector3(1023.559f, 1062.368f, 112.81f), Vector3(1026.326f, 1087.631f, 101.555f), Vector3(1031.256f, 1136.757f, 88.792f), Vector3(1036.187f, 1185.883f, 76.03f), Vector3(1037.157f, 1195.252f, 72.654f))))
      ZipLinePaths(new ZipLinePath(84, false, List(Vector3(1022.311f, 963.688f, 108.626f), Vector3(1021.282f, 941.241f, 104.749f), Vector3(1020.254f, 918.794f, 88.819f), Vector3(1018.856f, 888.266f, 69.743f))))
      ZipLinePaths(new ZipLinePath(85, true, List(Vector3(1024.216f, 1237.287f, 101.928f), Vector3(1022.885f, 1236.282f, 107.438f))))
      ZipLinePaths(new ZipLinePath(86, false, List(Vector3(1025.717f, 1248.9f, 102.662f), Vector3(1019.157f, 1290.883f, 77.791f))))
      ZipLinePaths(new ZipLinePath(87, false, List(Vector3(1030.526f, 1043.28f, 108.581f), Vector3(1060.287f, 1082.425f, 114.331f), Vector3(1068.936f, 1079.388f, 113.542f))))
      ZipLinePaths(new ZipLinePath(88, true, List(Vector3(1031.956f, 1098.93f, 71.733f), Vector3(1017.253f, 1035.007f, 108.08f))))
      ZipLinePaths(new ZipLinePath(89, true, List(Vector3(1032.161f, 1288.907f, 63.331f), Vector3(1013.199f, 1290.127f, 63.331f))))
      ZipLinePaths(new ZipLinePath(90, false, List(Vector3(1033.844f, 1237.622f, 102.493f), Vector3(1069.373f, 1274.123f, 100.766f), Vector3(1104.903f, 1310.623f, 98.295f), Vector3(1140.432f, 1347.124f, 95.825f), Vector3(1164.118f, 1371.458f, 93.15f))))
      ZipLinePaths(new ZipLinePath(91, false, List(Vector3(1034.374f, 1170.445f, 53.073f), Vector3(1071.018f, 1135.457f, 59.658f), Vector3(1099.04f, 1108.701f, 62.896f))))
      ZipLinePaths(new ZipLinePath(92, true, List(Vector3(1037.704f, 986.625f, 103.08f), Vector3(1008.671f, 1016.569f, 103.08f))))
      ZipLinePaths(new ZipLinePath(93, false, List(Vector3(1038.931f, 1285.894f, 76.25f), Vector3(1047.65f, 1294.159f, 83.937f), Vector3(1056.369f, 1302.425f, 83.265f))))
      ZipLinePaths(new ZipLinePath(94, false, List(Vector3(1042.946f, 1314.831f, 83.284f), Vector3(1029.474f, 1325.092f, 81.244f), Vector3(1016.003f, 1335.353f, 69.474f))))
      ZipLinePaths(new ZipLinePath(95, true, List(Vector3(1048.868f, 1001.705f, 98.08f), Vector3(1048.716f, 1001.779f, 113.08f))))
      ZipLinePaths(new ZipLinePath(96, true, List(Vector3(1050.738f, 892.293f, 70.931f), Vector3(1030.048f, 969.873f, 108.08f))))
      ZipLinePaths(new ZipLinePath(97, false, List(Vector3(1059.265f, 1222.82f, 73.793f), Vector3(1055.342f, 1172.574f, 70.201f), Vector3(1058.848f, 1129.879f, 62.697f))))
      ZipLinePaths(new ZipLinePath(98, false, List(Vector3(1061.142f, 1002.705f, 113.644f), Vector3(1084.951f, 1003.95f, 111.69f), Vector3(1108.761f, 1005.195f, 96.273f), Vector3(1156.381f, 1007.685f, 78.199f), Vector3(1204f, 1010.176f, 60.126f), Vector3(1206.801f, 1010.322f, 55.667f))))
      ZipLinePaths(new ZipLinePath(99, false, List(Vector3(1068.7f, 1088.587f, 72.146f), Vector3(1030.278f, 1056.665f, 70.605f), Vector3(991.859f, 1024.744f, 72.816f), Vector3(953.44f, 992.822f, 77.127f), Vector3(940.041f, 974.242f, 78.338f), Vector3(926.642f, 955.662f, 72.35f))))
      ZipLinePaths(new ZipLinePath(100, true, List(Vector3(1070.703f, 1182.499f, 65.267f), Vector3(1071.896f, 1181.097f, 99.132f))))
      ZipLinePaths(new ZipLinePath(101, false, List(Vector3(1072.49f, 703.019f, 74.582f), Vector3(1122.923f, 696.068f, 72.359f), Vector3(1173.357f, 689.117f, 69.381f), Vector3(1185.223f, 687.481f, 67.424f))))
      ZipLinePaths(new ZipLinePath(102, false, List(Vector3(1075.826f, 1172.464f, 99.643f), Vector3(1059.78f, 1125.381f, 105.503f), Vector3(1043.734f, 1078.299f, 110.607f), Vector3(1031.539f, 1042.516f, 113.589f))))
      ZipLinePaths(new ZipLinePath(103, true, List(Vector3(1086.981f, 909.865f, 75.232f), Vector3(1058.547f, 991.146f, 112.996f))))
      ZipLinePaths(new ZipLinePath(104, true, List(Vector3(1087.994f, 1101.369f, 72.933f), Vector3(1091.579f, 1063.563f, 113.04f))))
      ZipLinePaths(new ZipLinePath(105, false, List(Vector3(1093.367f, 814.482f, 62.395f), Vector3(1097.482f, 865.146f, 67.271f), Vector3(1099.257f, 887.001f, 67.998f))))
      ZipLinePaths(new ZipLinePath(106, false, List(Vector3(1108.968f, 1065.041f, 113.578f), Vector3(1115.996f, 1042.885f, 110.55f), Vector3(1123.024f, 1020.73f, 95.861f), Vector3(1137.081f, 976.421f, 77.441f), Vector3(1141.86f, 961.357f, 70.272f))))
      ZipLinePaths(new ZipLinePath(107, false, List(Vector3(1109.326f, 719.601f, 105.195f), Vector3(1064.788f, 696.899f, 104.857f), Vector3(1020.251f, 674.197f, 103.774f), Vector3(975.714f, 651.495f, 102.692f), Vector3(931.176f, 628.793f, 101.61f), Vector3(916.306f, 624.537f, 98.183f))))
      ZipLinePaths(new ZipLinePath(108, false, List(Vector3(1112.701f, 1072.817f, 113.492f), Vector3(1126.783f, 1092.701f, 112.58f), Vector3(1140.865f, 1112.586f, 99.194f), Vector3(1169.03f, 1152.355f, 84.168f), Vector3(1188.911f, 1180.428f, 70.467f))))
      ZipLinePaths(new ZipLinePath(109, false, List(Vector3(1116.494f, 1011.005f, 75.45f), Vector3(1160.328f, 988.101f, 63.755f), Vector3(1187.832f, 973.73f, 53.559f))))
      ZipLinePaths(new ZipLinePath(110, false, List(Vector3(1117.678f, 834.135f, 51.078f), Vector3(1068.972f, 844.563f, 55.228f))))
      ZipLinePaths(new ZipLinePath(111, true, List(Vector3(1118.78f, 977.911f, 62.593f), Vector3(1118.424f, 973.771f, 92.59f))))
      ZipLinePaths(new ZipLinePath(112, false, List(Vector3(1119.944f, 971.229f, 93.091f), Vector3(1125.29f, 921.608f, 96.903f), Vector3(1130.637f, 871.986f, 99.956f), Vector3(1135.984f, 822.365f, 103.009f), Vector3(1141.33f, 772.743f, 106.063f), Vector3(1142.4f, 762.819f, 105.298f))))
      ZipLinePaths(new ZipLinePath(113, false, List(Vector3(1126.862f, 957.212f, 75.033f), Vector3(1164.602f, 991.481f, 74.353f), Vector3(1202.341f, 1025.749f, 72.915f), Vector3(1235.641f, 1055.984f, 70.441f))))
      ZipLinePaths(new ZipLinePath(114, false, List(Vector3(1138.22f, 776.829f, 60.837f), Vector3(1185.416f, 778.843f, 80.756f), Vector3(1224.284f, 780.501f, 96.211f))))
      ZipLinePaths(new ZipLinePath(115, false, List(Vector3(1145.49f, 1219.031f, 70.445f), Vector3(1110.657f, 1221.727f, 65.772f))))
      ZipLinePaths(new ZipLinePath(116, false, List(Vector3(1145.658f, 1046.987f, 62.895f), Vector3(1173.99f, 1089.16f, 68.031f), Vector3(1202.012f, 1130.178f, 70.442f))))
      ZipLinePaths(new ZipLinePath(117, true, List(Vector3(1145.854f, 848.204f, 41.711f), Vector3(1136.769f, 851.408f, 53.017f))))
      ZipLinePaths(new ZipLinePath(118, false, List(Vector3(1150.993f, 1202.394f, 111.938f), Vector3(1102.384f, 1213.637f, 108.625f), Vector3(1053.776f, 1224.88f, 105.269f), Vector3(1032.86f, 1233.202f, 102.45f))))
      ZipLinePaths(new ZipLinePath(119, false, List(Vector3(1157.936f, 755.227f, 105.205f), Vector3(1194.167f, 722.066f, 96.587f), Vector3(1230.397f, 688.906f, 85.32f))))
      ZipLinePaths(new ZipLinePath(120, false, List(Vector3(1182.377f, 1225.952f, 111.96f), Vector3(1211.207f, 1266.997f, 103.516f), Vector3(1230.428f, 1294.36f, 95.849f))))
      ZipLinePaths(new ZipLinePath(121, false, List(Vector3(1183.368f, 1372.605f, 93.007f), Vector3(1227.066f, 1346.365f, 95.448f), Vector3(1257.054f, 1328.356f, 95.84f))))
      ZipLinePaths(new ZipLinePath(122, false, List(Vector3(1196.442f, 691.273f, 100.784f), Vector3(1157.75f, 723.895f, 107.82f), Vector3(1153.198f, 727.733f, 108.014f))))
      ZipLinePaths(new ZipLinePath(123, false, List(Vector3(1197.646f, 931.98f, 56.108f), Vector3(1208.098f, 981.536f, 62.846f), Vector3(1218.551f, 1031.092f, 68.83f), Vector3(1223.675f, 1055.384f, 70.444f))))
      ZipLinePaths(new ZipLinePath(124, false, List(Vector3(1198.424f, 1256.573f, 86.345f), Vector3(1182.542f, 1269.419f, 86.762f), Vector3(1166.661f, 1282.265f, 83.094f), Vector3(1127.897f, 1310.958f, 69.897f), Vector3(1114.717f, 1320.713f, 64.233f))))
      ZipLinePaths(new ZipLinePath(125, true, List(Vector3(1198.609f, 684.319f, 66.924f), Vector3(1199.063f, 682.308f, 100.28f))))
      ZipLinePaths(new ZipLinePath(126, false, List(Vector3(1199.772f, 1022.014f, 55.634f), Vector3(1150.254f, 1024.653f, 62.833f), Vector3(1148.274f, 1024.758f, 62.898f))))
      ZipLinePaths(new ZipLinePath(127, true, List(Vector3(1203.743f, 1205.042f, 69.941f), Vector3(1200.635f, 1212.965f, 85.83f))))
      ZipLinePaths(new ZipLinePath(128, false, List(Vector3(1203.809f, 691.086f, 100.782f), Vector3(1212.171f, 700.648f, 104.414f), Vector3(1220.534f, 710.211f, 105.048f), Vector3(1237.259f, 729.336f, 97.217f), Vector3(1254.312f, 748.836f, 93.592f))))
      ZipLinePaths(new ZipLinePath(129, false, List(Vector3(1218.115f, 1081.456f, 77.607f), Vector3(1168.182f, 1082.952f, 76.137f), Vector3(1118.249f, 1084.447f, 73.92f), Vector3(1088.289f, 1085.344f, 71.988f))))
      ZipLinePaths(new ZipLinePath(130, false, List(Vector3(1222.88f, 1118.832f, 81.22f), Vector3(1175.596f, 1104.086f, 75.102f), Vector3(1128.312f, 1089.34f, 68.25f), Vector3(1107.507f, 1082.852f, 62.899f))))
      ZipLinePaths(new ZipLinePath(131, false, List(Vector3(1226.808f, 732.805f, 67.43f), Vector3(1191.254f, 768.664f, 61.05f), Vector3(1163.368f, 796.789f, 53.076f))))
      ZipLinePaths(new ZipLinePath(132, false, List(Vector3(1233.261f, 747.07f, 93.355f), Vector3(1223.936f, 727.816f, 90.392f), Vector3(1214.611f, 708.563f, 67.471f))))
      ZipLinePaths(new ZipLinePath(133, false, List(Vector3(1236.826f, 1287.784f, 95.841f), Vector3(1252.226f, 1264.968f, 94.594f), Vector3(1259.126f, 1242.353f, 94.248f), Vector3(1263.97f, 1229.77f, 92.27f), Vector3(1268.914f, 1214.787f, 89.594f))))
      ZipLinePaths(new ZipLinePath(134, false, List(Vector3(1238.011f, 1087.458f, 79.528f), Vector3(1256.071f, 1040.094f, 74.678f), Vector3(1274.132f, 992.73f, 69.08f), Vector3(1292.192f, 945.365f, 63.481f), Vector3(1292.546f, 944.437f, 60.154f))))
      ZipLinePaths(new ZipLinePath(135, true, List(Vector3(1239.379f, 1157.428f, 91.67f), Vector3(1229.535f, 1148.836f, 80.74f))))
      ZipLinePaths(new ZipLinePath(136, false, List(Vector3(1243.286f, 1071.458f, 110.467f), Vector3(1195.837f, 1055.729f, 112.502f), Vector3(1148.388f, 1040f, 113.797f), Vector3(1100.939f, 1024.271f, 115.092f), Vector3(1060.08f, 1011.891f, 113.685f))))
      ZipLinePaths(new ZipLinePath(137, false, List(Vector3(1246.826f, 1113.238f, 110.476f), Vector3(1227.56f, 1124.142f, 107.77f), Vector3(1208.294f, 1135.046f, 87.974f), Vector3(1185.175f, 1148.131f, 70.65f))))
      ZipLinePaths(new ZipLinePath(138, true, List(Vector3(1257.492f, 1082.723f, 77.837f), Vector3(1269.3f, 1091.048f, 109.97f))))
      ZipLinePaths(new ZipLinePath(139, true, List(Vector3(1261.129f, 646.49f, 41.671f), Vector3(1256.709f, 663.872f, 67.12f))))
      ZipLinePaths(new ZipLinePath(140, false, List(Vector3(1261.935f, 800.968f, 93.356f), Vector3(1310.395f, 816.495f, 97.405f), Vector3(1358.855f, 832.022f, 100.714f), Vector3(1370.406f, 836.262f, 99.718f))))
      ZipLinePaths(new ZipLinePath(141, false, List(Vector3(1264.266f, 1055.81f, 71.423f), Vector3(1280.247f, 1103.405f, 81.102f), Vector3(1282.754f, 1110.87f, 81.848f))))
      ZipLinePaths(new ZipLinePath(142, false, List(Vector3(1269.421f, 1295.131f, 96.089f), Vector3(1265.331f, 1245.509f, 92.181f), Vector3(1261.242f, 1195.888f, 87.536f), Vector3(1257.643f, 1152.221f, 81.199f))))
      ZipLinePaths(new ZipLinePath(143, false, List(Vector3(1269.62f, 1116.62f, 81.595f), Vector3(1248.045f, 1106.837f, 81.616f), Vector3(1237.257f, 1110.195f, 81.152f))))
      ZipLinePaths(new ZipLinePath(144, false, List(Vector3(1270.147f, 1315.938f, 95.853f), Vector3(1307.494f, 1284.474f, 107.335f), Vector3(1325.421f, 1269.371f, 112.6f))))
      ZipLinePaths(new ZipLinePath(145, false, List(Vector3(1272.251f, 1072.732f, 110.472f), Vector3(1294.76f, 1061.355f, 111.827f), Vector3(1317.269f, 1049.978f, 103.687f), Vector3(1362.286f, 1027.223f, 96.165f), Vector3(1401.124f, 1007.594f, 87.966f))))
      ZipLinePaths(new ZipLinePath(146, false, List(Vector3(1285.557f, 1176.984f, 101.904f), Vector3(1268.858f, 1130.389f, 109.765f), Vector3(1263.18f, 1114.547f, 110.471f))))
      ZipLinePaths(new ZipLinePath(147, false, List(Vector3(1286.559f, 1195.88f, 101.906f), Vector3(1237.591f, 1202.38f, 110.405f), Vector3(1219.962f, 1204.72f, 111.984f))))
      ZipLinePaths(new ZipLinePath(148, false, List(Vector3(1288.633f, 1214.884f, 101.906f), Vector3(1247.251f, 1187.369f, 97.108f), Vector3(1226.56f, 1173.612f, 90.72f))))
      ZipLinePaths(new ZipLinePath(149, false, List(Vector3(1290.826f, 718.326f, 85.269f), Vector3(1321.687f, 758.543f, 91.79f), Vector3(1352.547f, 798.76f, 97.355f), Vector3(1375.13f, 828.049f, 99.719f))))
      ZipLinePaths(new ZipLinePath(150, false, List(Vector3(1294.656f, 744.295f, 78.185f), Vector3(1325.753f, 706.069f, 70.457f), Vector3(1347.522f, 679.31f, 59.862f))))
      ZipLinePaths(new ZipLinePath(151, true, List(Vector3(1299.9f, 760.768f, 46.63f), Vector3(1296.678f, 760.338f, 64.056f))))
      ZipLinePaths(new ZipLinePath(152, false, List(Vector3(1335.766f, 635.184f, 89.041f), Vector3(1293.37f, 663.502f, 88.496f), Vector3(1283.394f, 670.164f, 85.259f))))
      ZipLinePaths(new ZipLinePath(153, false, List(Vector3(1339.691f, 1169.995f, 91.574f), Vector3(1331.518f, 1128.805f, 91.322f))))
      ZipLinePaths(new ZipLinePath(154, false, List(Vector3(1340.717f, 673.945f, 89.042f), Vector3(1293.814f, 673.438f, 85.22f))))
      ZipLinePaths(new ZipLinePath(155, false, List(Vector3(1346.98f, 1243.609f, 112.552f), Vector3(1365.815f, 1197.503f, 117.758f), Vector3(1383.897f, 1153.241f, 121.128f))))
      ZipLinePaths(new ZipLinePath(156, true, List(Vector3(1351.016f, 1254.782f, 112.074f), Vector3(1348.091f, 1254.931f, 117.579f))))
      ZipLinePaths(new ZipLinePath(157, false, List(Vector3(1355.713f, 1145.351f, 122.442f), Vector3(1308.557f, 1161.641f, 119.865f), Vector3(1261.402f, 1177.931f, 116.539f), Vector3(1218.961f, 1192.593f, 111.982f))))
      ZipLinePaths(new ZipLinePath(158, false, List(Vector3(1356.956f, 1121.272f, 121.169f), Vector3(1344.419f, 1103.447f, 123.439f), Vector3(1331.882f, 1085.622f, 97.41f), Vector3(1320.599f, 1069.579f, 74.574f), Vector3(1309.316f, 1053.536f, 67.839f))))
      ZipLinePaths(new ZipLinePath(159, true, List(Vector3(1357.74f, 895.959f, 59.35f), Vector3(1360.201f, 894.144f, 93.751f))))
      ZipLinePaths(new ZipLinePath(160, true, List(Vector3(1361.935f, 804.341f, 64.056f), Vector3(1357.002f, 801.86f, 46.63f))))
      ZipLinePaths(new ZipLinePath(161, false, List(Vector3(1362.178f, 886.635f, 94.264f), Vector3(1335.084f, 844.963f, 89.573f), Vector3(1315.035f, 814.126f, 83.439f))))
      ZipLinePaths(new ZipLinePath(162, false, List(Vector3(1364.3f, 1153.417f, 122.494f), Vector3(1356.834f, 1163.739f, 122.87f), Vector3(1334.369f, 1184.662f, 101.953f))))
      ZipLinePaths(new ZipLinePath(163, false, List(Vector3(1365.132f, 689.391f, 92.669f), Vector3(1349.931f, 707.329f, 91.865f), Vector3(1334.731f, 725.267f, 76.399f), Vector3(1320.754f, 742.953f, 64.585f))))
      ZipLinePaths(new ZipLinePath(164, true, List(Vector3(1367.319f, 685.024f, 59.35f), Vector3(1369.049f, 684.262f, 92.131f))))
      ZipLinePaths(new ZipLinePath(165, true, List(Vector3(1367.378f, 1307.869f, 69.287f), Vector3(1366.65f, 1309.663f, 99.29f))))
      ZipLinePaths(new ZipLinePath(166, false, List(Vector3(1368.273f, 1305.847f, 99.844f), Vector3(1368.455f, 1295.812f, 98.705f), Vector3(1368.338f, 1284.377f, 95.52f), Vector3(1368.104f, 1261.507f, 80.35f), Vector3(1367.916f, 1243.211f, 69.842f))))
      ZipLinePaths(new ZipLinePath(167, true, List(Vector3(1368.901f, 675.463f, 92.14f), Vector3(1354.678f, 673.232f, 88.541f))))
      ZipLinePaths(new ZipLinePath(168, false, List(Vector3(1370.444f, 896.269f, 94.24f), Vector3(1410.474f, 915.351f, 69.813f), Vector3(1423.818f, 921.711f, 59.851f))))
      ZipLinePaths(new ZipLinePath(169, false, List(Vector3(1374.328f, 851.615f, 99.725f), Vector3(1339.342f, 901.528f, 91.012f), Vector3(1313.456f, 944.441f, 81.564f), Vector3(1287.57f, 987.354f, 72.116f), Vector3(1263.714f, 1026.901f, 61.023f))))
      ZipLinePaths(new ZipLinePath(170, false, List(Vector3(1379.289f, 821.763f, 99.73f), Vector3(1346.288f, 784.372f, 90.076f), Vector3(1319.407f, 753.3f, 78.291f))))
      ZipLinePaths(new ZipLinePath(171, false, List(Vector3(1391.245f, 823.148f, 99.721f), Vector3(1377.189f, 775.284f, 97.042f), Vector3(1363.132f, 727.421f, 93.615f), Vector3(1349.076f, 679.557f, 90.188f), Vector3(1348.233f, 676.685f, 89.041f))))
      ZipLinePaths(new ZipLinePath(172, false, List(Vector3(1401.064f, 994.658f, 87.999f), Vector3(1359.762f, 968.901f, 77.283f), Vector3(1318.459f, 943.144f, 65.823f), Vector3(1309.373f, 937.477f, 60.167f))))
      ZipLinePaths(new ZipLinePath(173, false, List(Vector3(1403.96f, 1159.131f, 102.791f), Vector3(1364.256f, 1189.526f, 103.601f), Vector3(1324.053f, 1219.322f, 103.67f), Vector3(1319.789f, 1223.569f, 101.9f))))
      ZipLinePaths(new ZipLinePath(174, false, List(Vector3(1404.913f, 726.942f, 54.268f), Vector3(1415.572f, 723.945f, 61.051f), Vector3(1426.231f, 720.948f, 59.866f))))
      ZipLinePaths(new ZipLinePath(175, true, List(Vector3(1407.877f, 1157.284f, 68.2f), Vector3(1410.102f, 1155.769f, 102.29f))))
      ZipLinePaths(new ZipLinePath(176, false, List(Vector3(1441.888f, 920.159f, 59.857f), Vector3(1448.313f, 870.457f, 70.052f), Vector3(1449.069f, 864.609f, 69.246f))))
      ZipLinePaths(new ZipLinePath(177, true, List(Vector3(921.825f, 917.111f, 62.093f), Vector3(910.937f, 927.938f, 106.39f))))
      ZipLinePaths(new ZipLinePath(178, false, List(Vector3(936.213f, 867.162f, 62.594f), Vector3(946.107f, 818.178f, 66.55f), Vector3(950.856f, 794.666f, 64.818f))))
      ZipLinePaths(new ZipLinePath(179, false, List(Vector3(619.323f, 951.023f, 83.943f), Vector3(616.715f, 926.938f, 81.874f), Vector3(614.108f, 902.854f, 72.325f), Vector3(613.065f, 893.22f, 67.528f))))
      ZipLinePaths(new ZipLinePath(180, false, List(Vector3(245.714f, 848.102f, 43.132f), Vector3(296.709f, 848.756f, 43.882f), Vector3(323.707f, 849.102f, 43.135f))))
      ZipLinePaths(new ZipLinePath(181, false, List(Vector3(380.613f, 839.591f, 43.291f), Vector3(399.483f, 840.484f, 51.34f), Vector3(418.353f, 841.378f, 53.222f))))
      ZipLinePaths(new ZipLinePath(182, false, List(Vector3(423.423f, 847.719f, 53.214f), Vector3(424.065f, 872.128f, 60.785f), Vector3(424.386f, 884.333f, 66.332f), Vector3(424.707f, 896.537f, 68.202f))))
      ZipLinePaths(new ZipLinePath(183, false, List(Vector3(462.449f, 888.611f, 73.2f), Vector3(511.515f, 888.994f, 87.828f), Vector3(514.401f, 889.017f, 88.205f))))
      ZipLinePaths(new ZipLinePath(184, false, List(Vector3(529.355f, 904.244f, 88.305f), Vector3(543.173f, 903.908f, 93.112f), Vector3(557.977f, 903.548f, 93.203f))))
      ZipLinePaths(new ZipLinePath(185, false, List(Vector3(369.424f, 856.903f, 43.137f), Vector3(370.753f, 870.317f, 43.183f), Vector3(372.082f, 883.73f, 42.93f), Vector3(372.593f, 929.659f, 26.657f), Vector3(392.704f, 974.788f, 25.183f), Vector3(440.126f, 1067.446f, 23.137f))))
      ZipLinePaths(new ZipLinePath(186, false, List(Vector3(461.151f, 1105.585f, 25.899f), Vector3(509.539f, 1102.527f, 43.908f), Vector3(525.022f, 1101.548f, 42.586f))))
      ZipLinePaths(new ZipLinePath(187, false, List(Vector3(546.503f, 1044.498f, 41.76f), Vector3(549.481f, 1035.018f, 62.544f), Vector3(550.578f, 1029.883f, 62.594f))))
      ZipLinePaths(new ZipLinePath(188, false, List(Vector3(588.661f, 1034.477f, 62.594f), Vector3(618.061f, 1076.138f, 62.575f), Vector3(626.132f, 1087.575f, 59.872f))))
      ZipLinePaths(new ZipLinePath(189, false, List(Vector3(567.287f, 1091.792f, 59.85f), Vector3(566.47f, 1087.667f, 59.901f), Vector3(564.019f, 1075.292f, 41.828f))))
      ZipLinePaths(new ZipLinePath(190, false, List(Vector3(674.348f, 1054.132f, 42.211f), Vector3(676.883f, 1066.163f, 59.553f), Vector3(677.417f, 1068.695f, 59.85f))))
      ZipLinePaths(new ZipLinePath(191, true, List(Vector3(715.551f, 1254.657f, 41.711f), Vector3(719.445f, 1248.623f, 52.27f))))
      ZipLinePaths(new ZipLinePath(192, false, List(Vector3(730.179f, 1259.137f, 51.721f), Vector3(725.114f, 1277.521f, 56.281f), Vector3(720.048f, 1295.905f, 59.862f))))
      ZipLinePaths(new ZipLinePath(193, false, List(Vector3(739.333f, 1249.902f, 51.803f), Vector3(747.707f, 1237.638f, 61.155f), Vector3(756.08f, 1225.373f, 67.572f))))
      ZipLinePaths(new ZipLinePath(194, false, List(Vector3(784.345f, 1158.64f, 63.956f), Vector3(786.322f, 1148.447f, 62.598f), Vector3(788.299f, 1138.255f, 59.86f))))
      ZipLinePaths(new ZipLinePath(195, false, List(Vector3(802.066f, 1126.481f, 59.856f), Vector3(845.835f, 1151.296f, 68.917f), Vector3(855.276f, 1156.648f, 69.245f))))
      ZipLinePaths(new ZipLinePath(196, false, List(Vector3(809.589f, 1096.782f, 59.851f), Vector3(859.53f, 1086.64f, 62.576f), Vector3(909.472f, 1076.499f, 64.558f), Vector3(917.306f, 1074.908f, 62.493f))))
      ZipLinePaths(new ZipLinePath(197, false, List(Vector3(739.925f, 1121.005f, 59.851f), Vector3(724.109f, 1169.36f, 58.494f), Vector3(717.596f, 1189.27f, 57.265f), Vector3(716.976f, 1191.166f, 56.882f), Vector3(715.115f, 1196.855f, 54.211f))))
      ZipLinePaths(new ZipLinePath(198, false, List(Vector3(708.861f, 1189.259f, 56.684f), Vector3(723.914f, 1163.117f, 99.532f), Vector3(726.509f, 1159.805f, 99.717f))))
      ZipLinePaths(new ZipLinePath(199, false, List(Vector3(766.786f, 1064.421f, 100.684f), Vector3(744.404f, 1056.068f, 97.217f), Vector3(722.023f, 1047.715f, 86.674f), Vector3(677.259f, 1031.01f, 71.931f), Vector3(656.668f, 1023.326f, 63.855f))))
      ZipLinePaths(new ZipLinePath(200, false, List(Vector3(783.594f, 1061.932f, 100.672f), Vector3(804.372f, 1049.105f, 98.415f), Vector3(825.15f, 1036.279f, 90.67f), Vector3(866.706f, 1010.626f, 79.938f), Vector3(908.262f, 984.973f, 69.207f), Vector3(929.871f, 971.633f, 62.605f))))
      ZipLinePaths(new ZipLinePath(201, false, List(Vector3(936.867f, 757.882f, 86.26f), Vector3(888.988f, 761.779f, 100.866f), Vector3(864.09f, 763.805f, 106.935f))))
      ZipLinePaths(new ZipLinePath(202, false, List(Vector3(937.908f, 750.375f, 86.289f), Vector3(918.478f, 737.428f, 82.43f), Vector3(899.047f, 724.481f, 69.121f), Vector3(894.384f, 721.374f, 62.642f))))
      ZipLinePaths(new ZipLinePath(203, false, List(Vector3(868.071f, 721.611f, 62.603f), Vector3(847.144f, 722.775f, 69.972f), Vector3(826.218f, 723.939f, 70.76f))))
      ZipLinePaths(new ZipLinePath(204, false, List(Vector3(805.619f, 664.018f, 42.212f), Vector3(791.589f, 676f, 70.301f), Vector3(789.047f, 678.172f, 70.751f))))
      ZipLinePaths(new ZipLinePath(205, false, List(Vector3(947.93f, 423.772f, 43.135f), Vector3(903.179f, 446.076f, 45.082f), Vector3(858.428f, 468.38f, 45.882f), Vector3(813.677f, 490.683f, 45.081f), Vector3(790.406f, 502.281f, 43.135f))))
      ZipLinePaths(new ZipLinePath(206, false, List(Vector3(919.391f, 482.058f, 53.372f), Vector3(919.499f, 493.516f, 57.359f), Vector3(919.608f, 504.973f, 61.073f), Vector3(919.716f, 516.431f, 64.786f), Vector3(919.825f, 527.888f, 68.225f))))
      ZipLinePaths(new ZipLinePath(207, false, List(Vector3(903.584f, 556.412f, 83.215f), Vector3(903.852f, 566.832f, 85.348f), Vector3(904.12f, 577.252f, 88.597f), Vector3(904.387f, 587.672f, 92.146f), Vector3(904.655f, 598.092f, 93.811f))))
      ZipLinePaths(new ZipLinePath(208, false, List(Vector3(793.715f, 540.457f, 42.854f), Vector3(827.38f, 578.564f, 47.499f), Vector3(861.045f, 616.671f, 51.401f), Vector3(865.666f, 621.901f, 51.304f))))
      ZipLinePaths(new ZipLinePath(209, false, List(Vector3(889.504f, 637.051f, 42.212f), Vector3(892.089f, 649.448f, 62.344f), Vector3(892.966f, 651.613f, 62.594f))))
      ZipLinePaths(new ZipLinePath(210, false, List(Vector3(951.854f, 636.424f, 40.682f), Vector3(948.828f, 647.958f, 62.17f), Vector3(947.164f, 656.161f, 62.594f))))
      ZipLinePaths(new ZipLinePath(211, false, List(Vector3(1071.14f, 654.136f, 40.718f), Vector3(1064.402f, 662.815f, 63.377f), Vector3(1063.335f, 664.8f, 63.831f))))
      ZipLinePaths(new ZipLinePath(212, false, List(Vector3(1082.249f, 692.541f, 63.831f), Vector3(1089.854f, 706.044f, 63.412f), Vector3(1097.458f, 719.547f, 62.394f))))
      ZipLinePaths(new ZipLinePath(213, false, List(Vector3(1129.616f, 797.599f, 62.785f), Vector3(1175.716f, 818.7f, 69.054f), Vector3(1193.795f, 826.976f, 69.241f))))
      ZipLinePaths(new ZipLinePath(214, false, List(Vector3(1260.129f, 863.553f, 69.249f), Vector3(1278.477f, 876.77f, 67.795f), Vector3(1296.825f, 889.987f, 60.16f))))
      ZipLinePaths(new ZipLinePath(215, false, List(Vector3(1273.776f, 820.533f, 69.268f), Vector3(1276.763f, 813.405f, 75.03f), Vector3(1280.549f, 806.276f, 74.649f))))
      ZipLinePaths(new ZipLinePath(216, false, List(Vector3(1344.93f, 825.989f, 64.548f), Vector3(1346.993f, 873.881f, 59.876f))))
      ZipLinePaths(new ZipLinePath(217, true, List(Vector3(1339.858f, 929.759f, 41.711f), Vector3(1318.114f, 908.199f, 68.808f))))
      ZipLinePaths(new ZipLinePath(218, false, List(Vector3(1030.151f, 1713.887f, 23.14f), Vector3(1006.194f, 1699.563f, 26.603f), Vector3(982.238f, 1687.64f, 29.17f), Vector3(934.325f, 1678.794f, 37.248f), Vector3(898.87f, 1678.435f, 45.05f), Vector3(888.329f, 1676.453f, 43.14f))))
      ZipLinePaths(new ZipLinePath(219, false, List(Vector3(848.815f, 1671.583f, 43.141f), Vector3(845.118f, 1664.51f, 44.601f), Vector3(836.221f, 1647.438f, 46.061f), Vector3(823.827f, 1623.292f, 48.887f), Vector3(812.239f, 1574.001f, 57.282f), Vector3(810.939f, 1556.451f, 61.617f), Vector3(811.09f, 1547.475f, 63.184f), Vector3(819.04f, 1538.3f, 63.456f))))
      ZipLinePaths(new ZipLinePath(220, false, List(Vector3(868.882f, 1435.428f, 42.945f), Vector3(905.951f, 1403.482f, 53.947f), Vector3(943.021f, 1371.536f, 64.215f), Vector3(960.642f, 1362.453f, 62.601f))))
      ZipLinePaths(new ZipLinePath(221, false, List(Vector3(937.234f, 1295.401f, 62.596f), Vector3(915.49f, 1285.599f, 65.559f), Vector3(893.746f, 1275.796f, 67.126f))))
      ZipLinePaths(new ZipLinePath(222, false, List(Vector3(840.873f, 1401.981f, 54.899f), Vector3(854.118f, 1354.514f, 64.089f), Vector3(860.475f, 1331.73f, 67.131f))))
      ZipLinePaths(new ZipLinePath(223, false, List(Vector3(776.421f, 1253.584f, 53.384f), Vector3(735.488f, 1253.999f, 82.838f), Vector3(730.985f, 1254.045f, 87.489f), Vector3(726.483f, 1254.09f, 89.041f))))
      ZipLinePaths(new ZipLinePath(224, false, List(Vector3(687.379f, 1281.924f, 89.056f), Vector3(646.928f, 1254.989f, 78.015f), Vector3(606.477f, 1228.054f, 66.247f), Vector3(593.533f, 1219.435f, 59.867f))))
      ZipLinePaths(new ZipLinePath(225, false, List(Vector3(704.658f, 1288.16f, 89.048f), Vector3(755.051f, 1287.183f, 82.015f), Vector3(805.444f, 1286.205f, 74.249f), Vector3(824.218f, 1285.841f, 67.133f))))
      ZipLinePaths(new ZipLinePath(226, true, List(Vector3(844.815f, 1286.36f, 66.624f), Vector3(847.467f, 1287.213f, 96.622f))))
      ZipLinePaths(new ZipLinePath(227, false, List(Vector3(839.532f, 1286.398f, 97.146f), Vector3(819.924f, 1273.129f, 91.276f), Vector3(800.317f, 1259.86f, 81.805f), Vector3(761.102f, 1233.322f, 65.734f), Vector3(734.741f, 1208.16f, 54.236f))))
      ZipLinePaths(new ZipLinePath(228, false, List(Vector3(832.911f, 1249.75f, 42.211f), Vector3(843.646f, 1254.481f, 66.64f), Vector3(845.363f, 1255.237f, 67.125f))))
      ZipLinePaths(new ZipLinePath(229, false, List(Vector3(863.404f, 1270.313f, 98.145f), Vector3(863.198f, 1285.183f, 97.207f), Vector3(862.992f, 1300.053f, 94.075f))))
      ZipLinePaths(new ZipLinePath(230, false, List(Vector3(865.722f, 1311.216f, 94.071f), Vector3(868.416f, 1330.263f, 85.658f), Vector3(871.11f, 1349.31f, 61.342f), Vector3(873.962f, 1369.478f, 42.045f))))
      ZipLinePaths(new ZipLinePath(231, true, List(Vector3(851.285f, 1311.441f, 66.624f), Vector3(863.553f, 1307.425f, 93.57f))))
      ZipLinePaths(new ZipLinePath(232, false, List(Vector3(914.737f, 1389.299f, 44.225f), Vector3(949.158f, 1356.677f, 63.697f), Vector3(955.907f, 1350.281f, 62.619f))))
      ZipLinePaths(new ZipLinePath(233, false, List(Vector3(924.083f, 1365.718f, 44.222f), Vector3(896.58f, 1326.667f, 59.757f), Vector3(888.604f, 1315.343f, 64.395f), Vector3(880.629f, 1304.018f, 67.156f))))
      ZipLinePaths(new ZipLinePath(234, false, List(Vector3(911.533f, 1328.871f, 52.857f), Vector3(914.633f, 1279.494f, 60.854f), Vector3(917.734f, 1230.116f, 68.106f), Vector3(919.036f, 1209.378f, 69.244f))))
      ZipLinePaths(new ZipLinePath(235, false, List(Vector3(924.721f, 1198.763f, 69.239f), Vector3(972.718f, 1215.952f, 68.696f), Vector3(999.068f, 1225.389f, 65.768f))))
      ZipLinePaths(new ZipLinePath(236, false, List(Vector3(1030.019f, 1253.007f, 73.812f), Vector3(982.302f, 1255.186f, 89.326f), Vector3(934.584f, 1257.365f, 104.505f), Vector3(920.423f, 1258.175f, 106.812f))))
      ZipLinePaths(new ZipLinePath(237, false, List(Vector3(863.545f, 1238.247f, 105.601f), Vector3(849.048f, 1235.318f, 101.887f), Vector3(834.551f, 1232.388f, 77.573f))))
      ZipLinePaths(new ZipLinePath(238, false, List(Vector3(896.085f, 1222.855f, 42.212f), Vector3(893.519f, 1214.151f, 68.902f), Vector3(893.252f, 1213.244f, 69.121f), Vector3(892.984f, 1212.338f, 69.24f))))
      ZipLinePaths(new ZipLinePath(239, false, List(Vector3(890.882f, 1211.784f, 102.01f), Vector3(913.426f, 1221.384f, 96.86f), Vector3(935.971f, 1230.983f, 88.63f), Vector3(981.059f, 1250.183f, 74.518f), Vector3(1004.93f, 1260.348f, 65.788f))))
      ZipLinePaths(new ZipLinePath(240, false, List(Vector3(731.968f, 946.628f, 42.211f), Vector3(730.363f, 950.87f, 51.2f), Vector3(729.36f, 955.145f, 55.24f), Vector3(728.156f, 959.076f, 55.66f))))
      ZipLinePaths(new ZipLinePath(241, false, List(Vector3(690.086f, 971.539f, 55.689f), Vector3(679.401f, 966.341f, 62.045f), Vector3(668.716f, 961.144f, 63.86f))))
      ZipLinePaths(new ZipLinePath(242, false, List(Vector3(622.095f, 943.343f, 72.955f), Vector3(654.892f, 905.636f, 72.118f), Vector3(666.699f, 892.061f, 70.751f))))
      ZipLinePaths(new ZipLinePath(243, false, List(Vector3(645.091f, 888.102f, 42.212f), Vector3(653.24f, 872.267f, 70.744f), Vector3(655.488f, 867.899f, 70.751f))))
      ZipLinePaths(new ZipLinePath(244, false, List(Vector3(620.426f, 919.306f, 42.211f), Vector3(629.595f, 924.542f, 63.781f), Vector3(632.347f, 926.451f, 63.831f))))
      ZipLinePaths(new ZipLinePath(245, false, List(Vector3(649.583f, 1071.378f, 42.211f), Vector3(659.85f, 1083.011f, 59.781f), Vector3(661.806f, 1085.227f, 59.85f))))
      ZipLinePaths(new ZipLinePath(246, false, List(Vector3(734.689f, 1077.906f, 42.211f), Vector3(741.524f, 1083.509f, 59.801f), Vector3(743.532f, 1085.732f, 59.851f))))
      ZipLinePaths(new ZipLinePath(247, false, List(Vector3(709.956f, 1074.547f, 42.212f), Vector3(699.011f, 1081.242f, 59.754f), Vector3(696.383f, 1083.257f, 59.851f))))
      ZipLinePaths(new ZipLinePath(248, false, List(Vector3(820.188f, 1246.386f, 63.948f), Vector3(831.947f, 1257.798f, 66.436f), Vector3(843.707f, 1269.21f, 67.129f))))
      ZipLinePaths(new ZipLinePath(249, false, List(Vector3(877.568f, 1254.981f, 67.125f), Vector3(872.845f, 1220.395f, 69.241f))))
      ZipLinePaths(new ZipLinePath(250, true, List(Vector3(845.981f, 1185.464f, 41.711f), Vector3(859.485f, 1177.367f, 68.74f))))
      ZipLinePaths(new ZipLinePath(251, false, List(Vector3(1144.739f, 1649.306f, 23.148f), Vector3(1194.351f, 1645.963f, 35.819f), Vector3(1212.834f, 1644.717f, 42.718f), Vector3(1222.075f, 1644.094f, 43.276f), Vector3(1231.317f, 1643.472f, 43.148f))))
      ZipLinePaths(new ZipLinePath(252, false, List(Vector3(1227.85f, 1610.461f, 43.135f), Vector3(1229.804f, 1586.178f, 44.07f), Vector3(1228.358f, 1561.896f, 44.905f), Vector3(1240.066f, 1513.33f, 45.129f), Vector3(1251.701f, 1489.533f, 44.117f), Vector3(1266.336f, 1465.736f, 43.21f))))
      ZipLinePaths(new ZipLinePath(253, false, List(Vector3(1134.072f, 1592.592f, 23.217f), Vector3(1151.421f, 1592.184f, 30.682f), Vector3(1167.171f, 1591.775f, 33.188f))))
      ZipLinePaths(new ZipLinePath(254, false, List(Vector3(1175.593f, 1584.054f, 33.224f), Vector3(1174.719f, 1566.227f, 38.716f), Vector3(1174.282f, 1557.313f, 41.722f), Vector3(1174.063f, 1552.857f, 42.824f), Vector3(1173.845f, 1548.4f, 43.246f))))
      ZipLinePaths(new ZipLinePath(255, false, List(Vector3(1158.087f, 1525.105f, 48.26f), Vector3(1159.791f, 1505.263f, 53.135f), Vector3(1160.643f, 1495.342f, 55.83f), Vector3(1161.069f, 1490.382f, 57.377f), Vector3(1161.495f, 1485.421f, 58.24f))))
      ZipLinePaths(new ZipLinePath(256, false, List(Vector3(1168.947f, 1481.166f, 58.294f), Vector3(1184.724f, 1480.379f, 63.525f), Vector3(1192.613f, 1479.985f, 66.403f), Vector3(1200.501f, 1479.591f, 68.205f))))
      ZipLinePaths(new ZipLinePath(257, false, List(Vector3(1208.601f, 1472.023f, 68.227f), Vector3(1208.31f, 1455.703f, 73.09f), Vector3(1208.165f, 1447.544f, 75.782f), Vector3(1208.092f, 1443.464f, 77.129f), Vector3(1208.02f, 1439.384f, 78.198f))))
      ZipLinePaths(new ZipLinePath(258, false, List(Vector3(1169.756f, 1292.65f, 42.212f), Vector3(1179.642f, 1286.138f, 70.391f), Vector3(1183.899f, 1283.421f, 70.441f))))
      ZipLinePaths(new ZipLinePath(259, false, List(Vector3(1102.447f, 1188.444f, 42.212f), Vector3(1095.153f, 1188.352f, 65.517f), Vector3(1090.986f, 1188.609f, 65.768f))))
      ZipLinePaths(new ZipLinePath(260, true, List(Vector3(694.51f, 741.781f, 76.599f), Vector3(688.903f, 728.66f, 101.6f))))
      ZipLinePaths(new ZipLinePath(261, true, List(Vector3(629.166f, 733.948f, 74.099f), Vector3(643.196f, 744.321f, 101.6f))))
      ZipLinePaths(new ZipLinePath(262, false, List(Vector3(1353.63f, 747.113f, 53.986f), Vector3(1391.4f, 713.131f, 59.157f), Vector3(1402.509f, 703.136f, 59.852f))))
      ZipLinePaths(new ZipLinePath(263, false, List(Vector3(1346.229f, 658.639f, 59.857f), Vector3(1330.56f, 672.779f, 65.587f), Vector3(1314.892f, 686.919f, 67.132f))))
      ZipLinePaths(new ZipLinePath(264, true, List(Vector3(1331.453f, 668.413f, 41.711f), Vector3(1350.394f, 668.947f, 59.35f))))
      ZipLinePaths(new ZipLinePath(265, false, List(Vector3(1344.101f, 609.861f, 42.211f), Vector3(1361.166f, 616.692f, 59.11f), Vector3(1363.021f, 617.434f, 59.327f), Vector3(1364.875f, 618.177f, 59.545f), Vector3(1368.585f, 619.662f, 59.881f))))
      ZipLinePaths(new ZipLinePath(266, false, List(Vector3(1439.681f, 676.189f, 59.852f), Vector3(1443.939f, 678.26f, 59.801f), Vector3(1458.843f, 685.511f, 42.212f))))
      ZipLinePaths(new ZipLinePath(267, false, List(Vector3(1390.255f, 604.317f, 59.851f), Vector3(1354.764f, 627.739f, 88.29f), Vector3(1352.605f, 629.307f, 89.415f), Vector3(1350.845f, 630.474f, 89.04f))))
      ZipLinePaths(new ZipLinePath(268, false, List(Vector3(1746.911f, 999.303f, 43.135f), Vector3(1724.334f, 954.686f, 45.081f), Vector3(1714.4f, 935.055f, 43.135f))))
      ZipLinePaths(new ZipLinePath(269, false, List(Vector3(1688.086f, 871.654f, 43.135f), Vector3(1679.453f, 862.013f, 43.74f), Vector3(1676.219f, 844.372f, 44.644f), Vector3(1651.953f, 817.89f, 44.453f), Vector3(1633.164f, 814.917f, 43.103f))))
      ZipLinePaths(new ZipLinePath(270, false, List(Vector3(1620.102f, 856.378f, 43.135f), Vector3(1574.771f, 877.48f, 44.604f), Vector3(1529.44f, 898.583f, 44.513f), Vector3(1485.923f, 918.842f, 42.847f))))
      ZipLinePaths(new ZipLinePath(271, false, List(Vector3(1461.886f, 923.554f, 42.515f), Vector3(1449.823f, 925.983f, 59.4f), Vector3(1446.194f, 926.978f, 59.851f))))
      ZipLinePaths(new ZipLinePath(272, false, List(Vector3(1398.87f, 921.689f, 59.85f), Vector3(1380.721f, 916.061f, 61.55f), Vector3(1362.572f, 910.433f, 59.851f))))
      ZipLinePaths(new ZipLinePath(273, false, List(Vector3(1359.474f, 897.941f, 94.479f), Vector3(1361.148f, 948.464f, 101.977f), Vector3(1362.823f, 998.988f, 108.705f), Vector3(1364.498f, 1049.51f, 115.434f), Vector3(1366.172f, 1100.031f, 122.163f), Vector3(1366.271f, 1103.003f, 120.819f))))
      ZipLinePaths(new ZipLinePath(274, false, List(Vector3(1354.682f, 897.716f, 94.275f), Vector3(1343.985f, 903.132f, 94.229f), Vector3(1333.289f, 908.548f, 88.707f), Vector3(1311.896f, 919.381f, 80.864f), Vector3(1269.11f, 941.045f, 66.717f), Vector3(1226.325f, 962.709f, 52.57f), Vector3(1209.21f, 971.375f, 42.236f))))
      ZipLinePaths(new ZipLinePath(275, false, List(Vector3(1274.319f, 944.513f, 60.151f), Vector3(1267.87f, 995.053f, 58.667f), Vector3(1265.467f, 1013.881f, 55.631f))))
      ZipLinePaths(new ZipLinePath(276, false, List(Vector3(1222.162f, 1005.468f, 55.63f), Vector3(1207.972f, 984.884f, 56.288f), Vector3(1193.781f, 964.3f, 56.846f), Vector3(1181.293f, 946.186f, 55.226f))))
      ZipLinePaths(new ZipLinePath(277, true, List(Vector3(1194.95f, 1107.768f, 41.711f), Vector3(1218.82f, 1105.194f, 69.941f))))
      ZipLinePaths(new ZipLinePath(278, true, List(Vector3(1123.832f, 1080.507f, 41.711f), Vector3(1116.495f, 1068.461f, 62.393f))))
      ZipLinePaths(new ZipLinePath(279, true, List(Vector3(1034.543f, 1133.645f, 41.711f), Vector3(1037.79f, 1115.293f, 62.193f))))
      ZipLinePaths(new ZipLinePath(280, true, List(Vector3(906.983f, 1073.184f, 41.711f), Vector3(914.912f, 1061.557f, 61.993f))))
      ZipLinePaths(new ZipLinePath(281, true, List(Vector3(975.064f, 873.634f, 41.711f), Vector3(977.547f, 890.666f, 62.093f))))
      ZipLinePaths(new ZipLinePath(282, false, List(Vector3(1147.666f, 899.276f, 62.797f), Vector3(1177.835f, 858.782f, 70.663f), Vector3(1180.201f, 855.606f, 69.242f))))
      ZipLinePaths(new ZipLinePath(283, true, List(Vector3(1194.793f, 868.656f, 41.711f), Vector3(1197.048f, 853.824f, 68.739f))))
      ZipLinePaths(new ZipLinePath(284, true, List(Vector3(1238.114f, 831.003f, 68.741f), Vector3(1247.279f, 835.486f, 98.74f))))
      ZipLinePaths(new ZipLinePath(285, false, List(Vector3(1248.833f, 828.196f, 99.256f), Vector3(1251.872f, 816.912f, 97.397f), Vector3(1254.912f, 805.628f, 93.371f))))
      ZipLinePaths(new ZipLinePath(286, false, List(Vector3(1248.384f, 837.722f, 99.256f), Vector3(1248.72f, 862.495f, 95.127f), Vector3(1249.057f, 887.268f, 87.915f), Vector3(1249.729f, 936.814f, 75.849f), Vector3(1250.401f, 986.361f, 63.783f), Vector3(1250.731f, 1010.648f, 55.645f))))
      ZipLinePaths(new ZipLinePath(287, false, List(Vector3(1230.23f, 791.562f, 96.216f), Vector3(1218.028f, 808.975f, 92.428f), Vector3(1205.827f, 827.087f, 77.33f))))
      ZipLinePaths(new ZipLinePath(288, false, List(Vector3(1245.69f, 872.174f, 69.241f), Vector3(1234.739f, 921.78f, 65.479f), Vector3(1223.788f, 971.385f, 60.979f), Vector3(1216.273f, 1005.428f, 55.632f))))
      ZipLinePaths(new ZipLinePath(289, true, List(Vector3(1257.469f, 740.652f, 41.711f), Vector3(1262.312f, 727.352f, 66.624f))))
      ZipLinePaths(new ZipLinePath(290, false, List(Vector3(1310.319f, 709.589f, 67.145f), Vector3(1325.971f, 725.913f, 63.854f), Vector3(1341.622f, 742.237f, 54.005f))))
      ZipLinePaths(new ZipLinePath(291, false, List(Vector3(1066.185f, 345.278f, 43.135f), Vector3(1090.987f, 394.68f, 45.06f), Vector3(1123.79f, 403.783f, 45.673f), Vector3(1156.593f, 422.686f, 46.286f), Vector3(1179.195f, 433.738f, 47.086f), Vector3(1201.797f, 438.591f, 45.486f), Vector3(1237.251f, 434.604f, 43.135f))))
      ZipLinePaths(new ZipLinePath(292, false, List(Vector3(1272.832f, 405.679f, 43.135f), Vector3(1294.179f, 431.04f, 46.641f), Vector3(1302.453f, 466.721f, 48.643f), Vector3(1302.127f, 502.402f, 48.746f), Vector3(1293.351f, 530.733f, 46.31f), Vector3(1282.175f, 551.063f, 45.675f), Vector3(1262.223f, 581.125f, 44.003f), Vector3(1253.687f, 594.169f, 42.981f))))
      ZipLinePaths(new ZipLinePath(293, false, List(Vector3(1200.595f, 640.757f, 52.22f), Vector3(1168.131f, 678.489f, 57.706f), Vector3(1135.666f, 716.221f, 62.455f), Vector3(1131.771f, 720.748f, 62.396f))))
      ZipLinePaths(new ZipLinePath(294, false, List(Vector3(1219.446f, 577.213f, 42.939f), Vector3(1175.25f, 600.595f, 44.341f), Vector3(1153.152f, 612.286f, 44.469f), Vector3(1131.055f, 623.978f, 43.198f), Vector3(1128.403f, 625.381f, 42.211f))))
      ZipLinePaths(new ZipLinePath(295, false, List(Vector3(1113.809f, 605.958f, 42.211f), Vector3(1072.225f, 578.187f, 43.962f), Vector3(1040.622f, 557.081f, 42.211f))))
      ZipLinePaths(new ZipLinePath(296, false, List(Vector3(1012.342f, 557.626f, 50.268f), Vector3(989.945f, 568.578f, 49.742f), Vector3(967.548f, 579.531f, 47.916f), Vector3(922.755f, 601.437f, 43.816f), Vector3(918.275f, 603.627f, 42.373f))))
      ZipLinePaths(new ZipLinePath(297, false, List(Vector3(1033.85f, 552.731f, 51.556f), Vector3(1018.965f, 601.166f, 58.072f), Vector3(1004.079f, 649.601f, 63.85f), Vector3(1002.62f, 654.35f, 63.834f))))
      ZipLinePaths(new ZipLinePath(298, false, List(Vector3(1308.288f, 670.018f, 42.254f), Vector3(1305.111f, 674.774f, 66.289f), Vector3(1301.934f, 679.531f, 67.124f))))
      ZipLinePaths(new ZipLinePath(299, false, List(Vector3(1407.327f, 760.918f, 53.937f), Vector3(1419.914f, 741.887f, 56.392f), Vector3(1426.207f, 732.371f, 59.071f), Vector3(1432.5f, 722.855f, 59.855f))))
      ZipLinePaths(new ZipLinePath(300, false, List(Vector3(1414.956f, 810.604f, 42.452f), Vector3(1425.75f, 818.247f, 68.99f), Vector3(1428.91f, 820.812f, 69.24f))))
      ZipLinePaths(new ZipLinePath(301, false, List(Vector3(1425.967f, 865.543f, 69.246f), Vector3(1402.712f, 873.841f, 67.036f), Vector3(1379.458f, 882.138f, 62.133f), Vector3(1372.016f, 884.794f, 59.857f))))
      ZipLinePaths(new ZipLinePath(302, false, List(Vector3(1268.025f, 904.827f, 60.151f), Vector3(1263.757f, 902.727f, 60.1f), Vector3(1254.573f, 899.329f, 42.212f))))
      ZipLinePaths(new ZipLinePath(303, true, List(Vector3(1428.244f, 1018.152f, 43.262f), Vector3(1408.2f, 1003.259f, 87.531f))))
      ZipLinePaths(new ZipLinePath(304, false, List(Vector3(1379.635f, 1065.961f, 42.212f), Vector3(1371.185f, 1066.559f, 67.74f), Vector3(1365.717f, 1066.945f, 67.839f))))
      ZipLinePaths(new ZipLinePath(305, false, List(Vector3(1326.501f, 1050.083f, 67.866f), Vector3(1328.553f, 1045.581f, 67.818f), Vector3(1334.452f, 1032.638f, 42.212f))))
      ZipLinePaths(new ZipLinePath(306, false, List(Vector3(1292.772f, 1057.474f, 67.839f), Vector3(1288.802f, 1055.895f, 67.589f), Vector3(1280.42f, 1052.562f, 42.211f))))
      ZipLinePaths(new ZipLinePath(307, true, List(Vector3(1284.273f, 1206.771f, 76.399f), Vector3(1295.186f, 1221.284f, 101.4f))))
      ZipLinePaths(new ZipLinePath(308, true, List(Vector3(1348.044f, 1190.75f, 73.899f), Vector3(1330.058f, 1181.224f, 101.4f))))
      ZipLinePaths(new ZipLinePath(309, false, List(Vector3(1329.397f, 1225.476f, 101.9f), Vector3(1329.965f, 1233.585f, 111.129f), Vector3(1330.533f, 1241.695f, 112.558f))))
      ZipLinePaths(new ZipLinePath(310, false, List(Vector3(1345.622f, 1270.449f, 112.588f), Vector3(1348.906f, 1276.707f, 111.787f), Vector3(1353.99f, 1284.564f, 90.887f), Vector3(1358.757f, 1295.48f, 69.788f))))
      ZipLinePaths(new ZipLinePath(311, false, List(Vector3(1339.331f, 1270.575f, 112.644f), Vector3(1335.904f, 1293.307f, 111.479f), Vector3(1332.477f, 1316.04f, 91.271f), Vector3(1326.294f, 1357.048f, 69.844f))))
      ZipLinePaths(new ZipLinePath(312, false, List(Vector3(1293.195f, 1460.466f, 43.003f), Vector3(1304.584f, 1411.778f, 43.654f), Vector3(1307.09f, 1401.067f, 42.884f))))
      ZipLinePaths(new ZipLinePath(313, true, List(Vector3(1289.895f, 1360.424f, 55.23f), Vector3(1300.183f, 1350.843f, 69.287f))))
      ZipLinePaths(new ZipLinePath(314, false, List(Vector3(1259.444f, 1278.484f, 70.442f), Vector3(1253.348f, 1271.49f, 86.369f), Vector3(1250.736f, 1268.492f, 86.33f))))
      ZipLinePaths(new ZipLinePath(315, false, List(Vector3(1274.596f, 1241.349f, 70.441f), Vector3(1263.641f, 1240.252f, 86.68f), Vector3(1255.817f, 1239.468f, 86.331f))))
      ZipLinePaths(new ZipLinePath(316, false, List(Vector3(1214.538f, 1287.232f, 86.33f), Vector3(1193.34f, 1333.233f, 93.024f), Vector3(1177.13f, 1368.41f, 92.951f))))
      ZipLinePaths(new ZipLinePath(317, false, List(Vector3(1113.475f, 1299.019f, 63.833f), Vector3(1150.486f, 1264.243f, 69.233f), Vector3(1165f, 1250.605f, 70.443f))))
      ZipLinePaths(new ZipLinePath(318, false, List(Vector3(747.8f, 1289.149f, 42.211f), Vector3(753.436f, 1302.617f, 59.563f), Vector3(755.512f, 1307.579f, 59.851f))))
      ZipLinePaths(new ZipLinePath(319, true, List(Vector3(804.848f, 1407.705f, 41.797f), Vector3(784.69f, 1401.489f, 59.372f))))
      ZipLinePaths(new ZipLinePath(320, false, List(Vector3(759.088f, 857.776f, 42.212f), Vector3(742.757f, 847.212f, 71.134f), Vector3(740.65f, 845.849f, 70.751f))))
      ZipLinePaths(new ZipLinePath(321, true, List(Vector3(828.794f, 769.675f, 70.251f), Vector3(840.305f, 765.899f, 106.42f))))
      ZipLinePaths(new ZipLinePath(322, false, List(Vector3(913.898f, 667.581f, 100.812f), Vector3(910.081f, 645.955f, 99.928f), Vector3(906.265f, 624.328f, 98.046f))))
      ZipLinePaths(new ZipLinePath(323, false, List(Vector3(1383.419f, 938.651f, 41.978f), Vector3(1391.808f, 942.877f, 59.369f), Vector3(1393.629f, 942.458f, 59.851f))))
      ZipLinePaths(new ZipLinePath(324, false, List(Vector3(1280.215f, 1185.361f, 101.932f), Vector3(1263.28f, 1198.155f, 99.615f), Vector3(1246.344f, 1210.948f, 86.363f))))
    }

    ZipLines()

  }
}
