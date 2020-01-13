package zonemaps

import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.painbox.Painbox
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.{CaptureTerminal, ProximityTerminal, Terminal}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.zipline.ZipLinePath
import net.psforever.objects.zones.{MapScale, ZoneMap}
import net.psforever.types.Vector3

object Ugd04 { // Byblos
  val ZoneMap = new ZoneMap("ugd04") {
    Scale = MapScale.Dim2048
    Checksum = 3797992164L

    Building10076()

    def Building10076(): Unit = { // Name: ceiling_bldg_a_10076 Type: ceiling_bldg_a GUID: 1, MapID: 10076
      LocalBuilding("ceiling_bldg_a_10076", 1, 10076, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1039.99f, 1095.48f, 207.81f), ceiling_bldg_a)))
      LocalObject(667, Door.Constructor(Vector3(1029.274f, 1107.31f, 209.589f)), owning_building_guid = 1)
      LocalObject(672, Door.Constructor(Vector3(1050.822f, 1080.083f, 215.095f)), owning_building_guid = 1)
      LocalObject(673, Door.Constructor(Vector3(1057.124f, 1093.777f, 209.589f)), owning_building_guid = 1)
      LocalObject(674, Door.Constructor(Vector3(1058.636f, 1102.775f, 215.095f)), owning_building_guid = 1)
    }

    Building10031()

    def Building10031(): Unit = { // Name: ceiling_bldg_b_10031 Type: ceiling_bldg_b GUID: 2, MapID: 10031
      LocalBuilding("ceiling_bldg_b_10031", 2, 10031, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(870.26f, 1148.19f, 178.45f), ceiling_bldg_b)))
      LocalObject(648, Door.Constructor(Vector3(872.7868f, 1142.638f, 180.229f)), owning_building_guid = 2)
      LocalObject(652, Door.Constructor(Vector3(885.0576f, 1155.74f, 180.229f)), owning_building_guid = 2)
    }

    Building10290()

    def Building10290(): Unit = { // Name: ceiling_bldg_c_10290 Type: ceiling_bldg_c GUID: 3, MapID: 10290
      LocalBuilding("ceiling_bldg_c_10290", 3, 10290, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(828.74f, 1334.76f, 162.68f), ceiling_bldg_c)))
      LocalObject(630, Door.Constructor(Vector3(786.8789f, 1312.529f, 164.459f)), owning_building_guid = 3)
      LocalObject(636, Door.Constructor(Vector3(807.6219f, 1289.492f, 164.459f)), owning_building_guid = 3)
      LocalObject(641, Door.Constructor(Vector3(832.528f, 1332.81f, 164.459f)), owning_building_guid = 3)
    }

    Building10057()

    def Building10057(): Unit = { // Name: ceiling_bldg_d_10057 Type: ceiling_bldg_d GUID: 4, MapID: 10057
      LocalBuilding("ceiling_bldg_d_10057", 4, 10057, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(709.3f, 999.79f, 174.85f), ceiling_bldg_d)))
      LocalObject(621, Door.Constructor(Vector3(693.3103f, 992.6534f, 176.585f)), owning_building_guid = 4)
      LocalObject(623, Door.Constructor(Vector3(702.1634f, 1015.78f, 176.585f)), owning_building_guid = 4)
      LocalObject(625, Door.Constructor(Vector3(716.3992f, 983.8055f, 176.585f)), owning_building_guid = 4)
      LocalObject(626, Door.Constructor(Vector3(725.2844f, 1006.889f, 176.585f)), owning_building_guid = 4)
      LocalObject(762, Painbox.Constructor(Vector3(709.302f, 1000.013f, 183.158f), painbox_continuous), owning_building_guid = 4)
    }

    Building10279()

    def Building10279(): Unit = { // Name: ceiling_bldg_d_10279 Type: ceiling_bldg_d GUID: 5, MapID: 10279
      LocalBuilding("ceiling_bldg_d_10279", 5, 10279, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1159.27f, 1060.78f, 198.86f), ceiling_bldg_d)))
      LocalObject(688, Door.Constructor(Vector3(1142.284f, 1056.528f, 200.595f)), owning_building_guid = 5)
      LocalObject(691, Door.Constructor(Vector3(1155.054f, 1077.754f, 200.595f)), owning_building_guid = 5)
      LocalObject(692, Door.Constructor(Vector3(1163.522f, 1043.794f, 200.595f)), owning_building_guid = 5)
      LocalObject(694, Door.Constructor(Vector3(1176.244f, 1064.996f, 200.595f)), owning_building_guid = 5)
      LocalObject(765, Painbox.Constructor(Vector3(1159.05f, 1060.821f, 207.168f), painbox_continuous), owning_building_guid = 5)
    }

    Building10056()

    def Building10056(): Unit = { // Name: ceiling_bldg_e_10056 Type: ceiling_bldg_e GUID: 6, MapID: 10056
      LocalBuilding("ceiling_bldg_e_10056", 6, 10056, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(837.17f, 1077.23f, 193.72f), ceiling_bldg_e)))
      LocalObject(640, Door.Constructor(Vector3(825.7902f, 1081.213f, 195.499f)), owning_building_guid = 6)
      LocalObject(644, Door.Constructor(Vector3(854.0325f, 1105.309f, 200.999f)), owning_building_guid = 6)
      LocalObject(646, Door.Constructor(Vector3(863.2781f, 1074.38f, 200.999f)), owning_building_guid = 6)
      LocalObject(647, Door.Constructor(Vector3(865.9316f, 1095.092f, 195.499f)), owning_building_guid = 6)
    }

    Building10058()

    def Building10058(): Unit = { // Name: ceiling_bldg_f_10058 Type: ceiling_bldg_f GUID: 7, MapID: 10058
      LocalBuilding("ceiling_bldg_f_10058", 7, 10058, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(699.42f, 1116.72f, 164.46f), ceiling_bldg_f)))
      LocalObject(622, Door.Constructor(Vector3(695.7913f, 1145.111f, 166.239f)), owning_building_guid = 7)
      LocalObject(624, Door.Constructor(Vector3(714.9769f, 1089.991f, 166.239f)), owning_building_guid = 7)
    }

    Building10077()

    def Building10077(): Unit = { // Name: ceiling_bldg_g_10077 Type: ceiling_bldg_g GUID: 8, MapID: 10077
      LocalBuilding("ceiling_bldg_g_10077", 8, 10077, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1136.66f, 968.13f, 183.11f), ceiling_bldg_g)))
      LocalObject(685, Door.Constructor(Vector3(1119.488f, 964.8085f, 184.889f)), owning_building_guid = 8)
      LocalObject(690, Door.Constructor(Vector3(1154.39f, 963.4429f, 184.889f)), owning_building_guid = 8)
    }

    Building10030()

    def Building10030(): Unit = { // Name: ceiling_bldg_h_10030 Type: ceiling_bldg_h GUID: 9, MapID: 10030
      LocalBuilding("ceiling_bldg_h_10030", 9, 10030, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(894.28f, 1280.67f, 179.98f), ceiling_bldg_h)))
      LocalObject(650, Door.Constructor(Vector3(879.0871f, 1288.218f, 181.759f)), owning_building_guid = 9)
      LocalObject(654, Door.Constructor(Vector3(895.5801f, 1264.974f, 184.259f)), owning_building_guid = 9)
      LocalObject(656, Door.Constructor(Vector3(907.9444f, 1290.757f, 181.759f)), owning_building_guid = 9)
    }

    Building10283()

    def Building10283(): Unit = { // Name: ground_bldg_a_10283 Type: ground_bldg_a GUID: 83, MapID: 10283
      LocalBuilding("ground_bldg_a_10283", 83, 10283, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(872.31f, 871.53f, 124f), ground_bldg_a)))
      LocalObject(645, Door.Constructor(Vector3(858.162f, 879.54f, 125.779f)), owning_building_guid = 83)
      LocalObject(653, Door.Constructor(Vector3(888.688f, 875.546f, 125.779f)), owning_building_guid = 83)
    }

    Building10022()

    def Building10022(): Unit = { // Name: ground_bldg_a_10022 Type: ground_bldg_a GUID: 84, MapID: 10022
      LocalBuilding("ground_bldg_a_10022", 84, 10022, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1063.4f, 1234.86f, 124f), ground_bldg_a)))
      LocalObject(671, Door.Constructor(Vector3(1049.252f, 1242.87f, 125.779f)), owning_building_guid = 84)
      LocalObject(679, Door.Constructor(Vector3(1079.778f, 1238.876f, 125.779f)), owning_building_guid = 84)
    }

    Building10288()

    def Building10288(): Unit = { // Name: ground_bldg_b_10288 Type: ground_bldg_b GUID: 85, MapID: 10288
      LocalBuilding("ground_bldg_b_10288", 85, 10288, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(875.58f, 779.96f, 113f), ground_bldg_b)))
      LocalObject(649, Door.Constructor(Vector3(877.595f, 796.45f, 114.779f)), owning_building_guid = 85)
      LocalObject(651, Door.Constructor(Vector3(881.596f, 778.95f, 114.779f)), owning_building_guid = 85)
      LocalObject(655, Door.Constructor(Vector3(900.102f, 785.976f, 120.279f)), owning_building_guid = 85)
    }

    Building10001()

    def Building10001(): Unit = { // Name: ground_bldg_c_10001 Type: ground_bldg_c GUID: 86, MapID: 10001
      LocalBuilding("ground_bldg_c_10001", 86, 10001, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(789.88f, 853.2f, 131f), ground_bldg_c)))
      LocalObject(628, Door.Constructor(Vector3(778.39f, 807.216f, 132.779f)), owning_building_guid = 86)
      LocalObject(633, Door.Constructor(Vector3(793.864f, 854.71f, 132.779f)), owning_building_guid = 86)
      LocalObject(637, Door.Constructor(Vector3(809.39f, 807.216f, 132.779f)), owning_building_guid = 86)
    }

    Building10222()

    def Building10222(): Unit = { // Name: ground_bldg_c_10222 Type: ground_bldg_c GUID: 87, MapID: 10222
      LocalBuilding("ground_bldg_c_10222", 87, 10222, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1395.18f, 1160.54f, 130.8f), ground_bldg_c)))
      LocalObject(722, Door.Constructor(Vector3(1391.295f, 1162.289f, 132.579f)), owning_building_guid = 87)
      LocalObject(723, Door.Constructor(Vector3(1413.9f, 1206.851f, 132.579f)), owning_building_guid = 87)
      LocalObject(724, Door.Constructor(Vector3(1435.82f, 1184.931f, 132.579f)), owning_building_guid = 87)
    }

    Building10154()

    def Building10154(): Unit = { // Name: ground_bldg_d_10154 Type: ground_bldg_d GUID: 88, MapID: 10154
      LocalBuilding("ground_bldg_d_10154", 88, 10154, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(831.35f, 734.45f, 111f), ground_bldg_d)))
      LocalObject(638, Door.Constructor(Vector3(815.7736f, 742.4045f, 112.735f)), owning_building_guid = 88)
      LocalObject(639, Door.Constructor(Vector3(823.3954f, 718.8736f, 112.735f)), owning_building_guid = 88)
      LocalObject(642, Door.Constructor(Vector3(839.2851f, 750.0588f, 112.735f)), owning_building_guid = 88)
      LocalObject(643, Door.Constructor(Vector3(846.9587f, 726.5149f, 112.735f)), owning_building_guid = 88)
    }

    Building10155()

    def Building10155(): Unit = { // Name: ground_bldg_d_10155 Type: ground_bldg_d GUID: 89, MapID: 10155
      LocalBuilding("ground_bldg_d_10155", 89, 10155, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1132.27f, 1293.23f, 118.79f), ground_bldg_d)))
      LocalObject(684, Door.Constructor(Vector3(1117.745f, 1303.008f, 120.525f)), owning_building_guid = 89)
      LocalObject(686, Door.Constructor(Vector3(1122.492f, 1278.705f, 120.525f)), owning_building_guid = 89)
      LocalObject(687, Door.Constructor(Vector3(1142.064f, 1307.721f, 120.525f)), owning_building_guid = 89)
      LocalObject(689, Door.Constructor(Vector3(1146.761f, 1283.436f, 120.525f)), owning_building_guid = 89)
    }

    Building10023()

    def Building10023(): Unit = { // Name: ground_bldg_e_10023 Type: ground_bldg_e GUID: 90, MapID: 10023
      LocalBuilding("ground_bldg_e_10023", 90, 10023, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1044.11f, 1336.51f, 116.41f), ground_bldg_e)))
      LocalObject(669, Door.Constructor(Vector3(1034.913f, 1344.307f, 118.189f)), owning_building_guid = 90)
      LocalObject(675, Door.Constructor(Vector3(1067.463f, 1324.493f, 123.689f)), owning_building_guid = 90)
      LocalObject(676, Door.Constructor(Vector3(1069.915f, 1356.681f, 123.689f)), owning_building_guid = 90)
      LocalObject(678, Door.Constructor(Vector3(1077.362f, 1342.878f, 118.189f)), owning_building_guid = 90)
    }

    Building10156()

    def Building10156(): Unit = { // Name: ground_bldg_e_10156 Type: ground_bldg_e GUID: 91, MapID: 10156
      LocalBuilding("ground_bldg_e_10156", 91, 10156, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1083.61f, 698.32f, 116.81f), ground_bldg_e)))
      LocalObject(677, Door.Constructor(Vector3(1074.413f, 706.1166f, 118.589f)), owning_building_guid = 91)
      LocalObject(680, Door.Constructor(Vector3(1106.963f, 686.3034f, 124.089f)), owning_building_guid = 91)
      LocalObject(681, Door.Constructor(Vector3(1109.415f, 718.4909f, 124.089f)), owning_building_guid = 91)
      LocalObject(683, Door.Constructor(Vector3(1116.862f, 704.6882f, 118.589f)), owning_building_guid = 91)
    }

    Building10008()

    def Building10008(): Unit = { // Name: ground_bldg_f_10008 Type: ground_bldg_f GUID: 92, MapID: 10008
      LocalBuilding("ground_bldg_f_10008", 92, 10008, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1138.19f, 625.25f, 113.54f), ground_bldg_f)))
      LocalObject(682, Door.Constructor(Vector3(1110.067f, 630.5721f, 115.319f)), owning_building_guid = 92)
      LocalObject(693, Door.Constructor(Vector3(1168.418f, 631.7859f, 115.319f)), owning_building_guid = 92)
    }

    Building10009()

    def Building10009(): Unit = { // Name: ground_bldg_g_10009 Type: ground_bldg_g GUID: 93, MapID: 10009
      LocalBuilding("ground_bldg_g_10009", 93, 10009, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1368.37f, 871.02f, 119f), ground_bldg_g)))
      LocalObject(720, Door.Constructor(Vector3(1360.855f, 887.7485f, 120.779f)), owning_building_guid = 93)
      LocalObject(721, Door.Constructor(Vector3(1381.758f, 859.7654f, 120.779f)), owning_building_guid = 93)
    }

    Building10010()

    def Building10010(): Unit = { // Name: ground_bldg_h_10010 Type: ground_bldg_h GUID: 94, MapID: 10010
      LocalBuilding("ground_bldg_h_10010", 94, 10010, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1312.56f, 941.38f, 118.56f), ground_bldg_h)))
      LocalObject(714, Door.Constructor(Vector3(1303.717f, 926.9027f, 120.339f)), owning_building_guid = 94)
      LocalObject(715, Door.Constructor(Vector3(1303.703f, 955.8715f, 120.339f)), owning_building_guid = 94)
      LocalObject(718, Door.Constructor(Vector3(1328.309f, 941.3072f, 122.839f)), owning_building_guid = 94)
    }

    Building10298()

    def Building10298(): Unit = { // Name: ground_bldg_i_10298 Type: ground_bldg_i GUID: 95, MapID: 10298
      LocalBuilding("ground_bldg_i_10298", 95, 10298, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1290.66f, 1171.46f, 134.92f), ground_bldg_i)))
      LocalObject(704, Door.Constructor(Vector3(1273.733f, 1153.416f, 136.699f)), owning_building_guid = 95)
      LocalObject(712, Door.Constructor(Vector3(1302.412f, 1194.373f, 136.699f)), owning_building_guid = 95)
    }

    Building10350()

    def Building10350(): Unit = { // Name: N_Redoubt Type: redoubt GUID: 96, MapID: 10350
      LocalBuilding("N_Redoubt", 96, 10350, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(781.91f, 1176.43f, 109.13f), redoubt)))
      LocalObject(816, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 96)
      LocalObject(627, Door.Constructor(Vector3(773.4349f, 1161.108f, 110.865f)), owning_building_guid = 96)
      LocalObject(629, Door.Constructor(Vector3(786.5674f, 1159.849f, 120.909f)), owning_building_guid = 96)
      LocalObject(631, Door.Constructor(Vector3(790.4033f, 1191.719f, 110.865f)), owning_building_guid = 96)
      LocalObject(632, Door.Constructor(Vector3(790.5598f, 1167.062f, 120.889f)), owning_building_guid = 96)
      LocalObject(634, Door.Constructor(Vector3(794.4199f, 1174.092f, 120.889f)), owning_building_guid = 96)
      LocalObject(635, Door.Constructor(Vector3(798.427f, 1181.313f, 120.909f)), owning_building_guid = 96)
      LocalObject(830, Terminal.Constructor(Vector3(765.7828f, 1185.513f, 109.0858f), vanu_equipment_term), owning_building_guid = 96)
      LocalObject(836, Terminal.Constructor(Vector3(798.2699f, 1167.343f, 109.0835f), vanu_equipment_term), owning_building_guid = 96)
      LocalObject(775, SpawnTube.Constructor(Vector3(781.91f, 1176.43f, 109.13f), Vector3(0, 0, 299)), owning_building_guid = 96)
      LocalObject(763, Painbox.Constructor(Vector3(782.1441f, 1176.253f, 116.919f), painbox_continuous), owning_building_guid = 96)
    }

    Building10354()

    def Building10354(): Unit = { // Name: S_Redoubt Type: redoubt GUID: 97, MapID: 10354
      LocalBuilding("S_Redoubt", 97, 10354, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(1026.2f, 729.8f, 124.4f), redoubt)))
      LocalObject(817, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 97)
      LocalObject(660, Door.Constructor(Vector3(1009.968f, 735.5583f, 136.179f)), owning_building_guid = 97)
      LocalObject(661, Door.Constructor(Vector3(1010.342f, 722.4229f, 126.135f)), owning_building_guid = 97)
      LocalObject(665, Door.Constructor(Vector3(1017.453f, 739.0447f, 136.159f)), owning_building_guid = 97)
      LocalObject(666, Door.Constructor(Vector3(1024.708f, 742.4631f, 136.159f)), owning_building_guid = 97)
      LocalObject(668, Door.Constructor(Vector3(1032.178f, 745.9517f, 136.179f)), owning_building_guid = 97)
      LocalObject(670, Door.Constructor(Vector3(1042.063f, 737.2145f, 126.135f)), owning_building_guid = 97)
      LocalObject(843, Terminal.Constructor(Vector3(1018.305f, 746.7673f, 124.3535f), vanu_equipment_term), owning_building_guid = 97)
      LocalObject(846, Terminal.Constructor(Vector3(1033.909f, 712.9728f, 124.3558f), vanu_equipment_term), owning_building_guid = 97)
      LocalObject(776, SpawnTube.Constructor(Vector3(1026.2f, 729.8f, 124.4f), Vector3(0, 0, 155)), owning_building_guid = 97)
      LocalObject(764, Painbox.Constructor(Vector3(1026.115f, 730.0808f, 132.189f), painbox_continuous), owning_building_guid = 97)
    }

    Building10285()

    def Building10285(): Unit = { // Name: S_Stasis Type: vanu_control_point GUID: 154, MapID: 10285
      LocalBuilding("S_Stasis", 154, 10285, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(647.64f, 755.35f, 139.61f), vanu_control_point)))
      LocalObject(814, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 154)
      LocalObject(601, Door.Constructor(Vector3(611.1205f, 763.9373f, 141.389f)), owning_building_guid = 154)
      LocalObject(604, Door.Constructor(Vector3(632.1003f, 755.4609f, 171.389f)), owning_building_guid = 154)
      LocalObject(605, Door.Constructor(Vector3(635.5374f, 748.0574f, 146.33f)), owning_building_guid = 154)
      LocalObject(606, Door.Constructor(Vector3(639.7699f, 758.5145f, 171.369f)), owning_building_guid = 154)
      LocalObject(607, Door.Constructor(Vector3(641.7611f, 702.6169f, 141.389f)), owning_building_guid = 154)
      LocalObject(608, Door.Constructor(Vector3(641.9103f, 763.7706f, 146.33f)), owning_building_guid = 154)
      LocalObject(609, Door.Constructor(Vector3(647.1385f, 761.6781f, 171.369f)), owning_building_guid = 154)
      LocalObject(610, Door.Constructor(Vector3(647.1663f, 792.3372f, 141.389f)), owning_building_guid = 154)
      LocalObject(611, Door.Constructor(Vector3(651.2506f, 741.6844f, 146.33f)), owning_building_guid = 154)
      LocalObject(612, Door.Constructor(Vector3(654.6605f, 765.0719f, 171.389f)), owning_building_guid = 154)
      LocalObject(613, Door.Constructor(Vector3(657.6295f, 757.4349f, 146.33f)), owning_building_guid = 154)
      LocalObject(620, Door.Constructor(Vector3(692.3818f, 737.6293f, 141.389f)), owning_building_guid = 154)
      LocalObject(820, Terminal.Constructor(Vector3(636.3433f, 754.7145f, 144.623f), vanu_equipment_term), owning_building_guid = 154)
      LocalObject(821, Terminal.Constructor(Vector3(637.835f, 758.3971f, 144.627f), vanu_equipment_term), owning_building_guid = 154)
      LocalObject(822, Terminal.Constructor(Vector3(641.005f, 743.9242f, 144.627f), vanu_equipment_term), owning_building_guid = 154)
      LocalObject(823, Terminal.Constructor(Vector3(644.6388f, 742.4835f, 144.623f), vanu_equipment_term), owning_building_guid = 154)
      LocalObject(824, Terminal.Constructor(Vector3(648.4553f, 762.9833f, 144.623f), vanu_equipment_term), owning_building_guid = 154)
      LocalObject(826, Terminal.Constructor(Vector3(652.1378f, 761.4916f, 144.627f), vanu_equipment_term), owning_building_guid = 154)
      LocalObject(827, Terminal.Constructor(Vector3(655.3082f, 747.0178f, 144.627f), vanu_equipment_term), owning_building_guid = 154)
      LocalObject(828, Terminal.Constructor(Vector3(656.7999f, 750.7004f, 144.623f), vanu_equipment_term), owning_building_guid = 154)
      LocalObject(880, SpawnTube.Constructor(Vector3(646.5804f, 752.7275f, 144.749f), Vector3(0, 0, 247)), owning_building_guid = 154)
      LocalObject(761, Painbox.Constructor(Vector3(646.191f, 752.8744f, 153.9518f), painbox_continuous), owning_building_guid = 154)
      LocalObject(767, Painbox.Constructor(Vector3(633.8731f, 747.6716f, 148.5f), painbox_door_radius_continuous), owning_building_guid = 154)
      LocalObject(768, Painbox.Constructor(Vector3(641.0864f, 765.9443f, 147.9f), painbox_door_radius_continuous), owning_building_guid = 154)
      LocalObject(769, Painbox.Constructor(Vector3(652.7124f, 739.271f, 148.5f), painbox_door_radius_continuous), owning_building_guid = 154)
      LocalObject(770, Painbox.Constructor(Vector3(659.4526f, 758.1463f, 148.5f), painbox_door_radius_continuous), owning_building_guid = 154)
    }

    Building10215()

    def Building10215(): Unit = { // Name: N_Stasis Type: vanu_control_point GUID: 155, MapID: 10215
      LocalBuilding("N_Stasis", 155, 10215, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1294.35f, 1355.27f, 144.89f), vanu_control_point)))
      LocalObject(819, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 155)
      LocalObject(703, Door.Constructor(Vector3(1247.211f, 1364.952f, 146.669f)), owning_building_guid = 155)
      LocalObject(705, Door.Constructor(Vector3(1284.874f, 1351.482f, 151.61f)), owning_building_guid = 155)
      LocalObject(706, Door.Constructor(Vector3(1288.421f, 1368.101f, 151.61f)), owning_building_guid = 155)
      LocalObject(707, Door.Constructor(Vector3(1289.124f, 1344.477f, 176.669f)), owning_building_guid = 155)
      LocalObject(708, Door.Constructor(Vector3(1290.983f, 1408.223f, 146.669f)), owning_building_guid = 155)
      LocalObject(709, Door.Constructor(Vector3(1295.943f, 1349.125f, 176.649f)), owning_building_guid = 155)
      LocalObject(710, Door.Constructor(Vector3(1301.239f, 1318.927f, 146.669f)), owning_building_guid = 155)
      LocalObject(711, Door.Constructor(Vector3(1301.455f, 1347.972f, 151.61f)), owning_building_guid = 155)
      LocalObject(713, Door.Constructor(Vector3(1302.65f, 1353.52f, 176.649f)), owning_building_guid = 155)
      LocalObject(716, Door.Constructor(Vector3(1305.002f, 1364.553f, 151.61f)), owning_building_guid = 155)
      LocalObject(717, Door.Constructor(Vector3(1309.673f, 1357.859f, 176.669f)), owning_building_guid = 155)
      LocalObject(719, Door.Constructor(Vector3(1331.806f, 1353.155f, 146.669f)), owning_building_guid = 155)
      LocalObject(856, Terminal.Constructor(Vector3(1284.522f, 1358.258f, 149.903f), vanu_equipment_term), owning_building_guid = 155)
      LocalObject(857, Terminal.Constructor(Vector3(1285.351f, 1362.144f, 149.907f), vanu_equipment_term), owning_building_guid = 155)
      LocalObject(858, Terminal.Constructor(Vector3(1290.987f, 1348.441f, 149.907f), vanu_equipment_term), owning_building_guid = 155)
      LocalObject(859, Terminal.Constructor(Vector3(1294.873f, 1347.611f, 149.903f), vanu_equipment_term), owning_building_guid = 155)
      LocalObject(860, Terminal.Constructor(Vector3(1295.071f, 1368.462f, 149.903f), vanu_equipment_term), owning_building_guid = 155)
      LocalObject(861, Terminal.Constructor(Vector3(1298.9f, 1367.674f, 149.907f), vanu_equipment_term), owning_building_guid = 155)
      LocalObject(862, Terminal.Constructor(Vector3(1304.535f, 1353.972f, 149.907f), vanu_equipment_term), owning_building_guid = 155)
      LocalObject(863, Terminal.Constructor(Vector3(1305.365f, 1357.858f, 149.903f), vanu_equipment_term), owning_building_guid = 155)
      LocalObject(881, SpawnTube.Constructor(Vector3(1294.938f, 1358.037f, 150.029f), Vector3(0, 0, 57)), owning_building_guid = 155)
      LocalObject(766, Painbox.Constructor(Vector3(1295.347f, 1357.96f, 159.2318f), painbox_continuous), owning_building_guid = 155)
      LocalObject(771, Painbox.Constructor(Vector3(1283.202f, 1350.465f, 153.78f), painbox_door_radius_continuous), owning_building_guid = 155)
      LocalObject(772, Painbox.Constructor(Vector3(1286.563f, 1370.224f, 153.78f), painbox_door_radius_continuous), owning_building_guid = 155)
      LocalObject(773, Painbox.Constructor(Vector3(1302.644f, 1345.975f, 153.18f), painbox_door_radius_continuous), owning_building_guid = 155)
      LocalObject(774, Painbox.Constructor(Vector3(1306.574f, 1365.222f, 153.78f), painbox_door_radius_continuous), owning_building_guid = 155)
    }

    Building10000()

    def Building10000(): Unit = { // Name: Core Type: vanu_core GUID: 156, MapID: 10000
      LocalBuilding("Core", 156, 10000, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(989.99f, 1015.28f, 166.4f), vanu_core)))
      LocalObject(657, Door.Constructor(Vector3(972.7982f, 985.2183f, 173.188f)), owning_building_guid = 156)
      LocalObject(658, Door.Constructor(Vector3(976.3412f, 1025.357f, 178.188f)), owning_building_guid = 156)
      LocalObject(659, Door.Constructor(Vector3(976.3412f, 1025.357f, 183.188f)), owning_building_guid = 156)
      LocalObject(662, Door.Constructor(Vector3(1012.937f, 981.6753f, 173.188f)), owning_building_guid = 156)
      LocalObject(663, Door.Constructor(Vector3(1012.937f, 981.6753f, 178.188f)), owning_building_guid = 156)
      LocalObject(664, Door.Constructor(Vector3(1016.48f, 1021.814f, 183.188f)), owning_building_guid = 156)
    }

    Building10011()

    def Building10011(): Unit = { // Name: N_ATPlant Type: vanu_vehicle_station GUID: 183, MapID: 10011
      LocalBuilding("N_ATPlant", 183, 10011, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(656.27f, 1262.7f, 108.8f), vanu_vehicle_station)))
      LocalObject(815, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 183)
      LocalObject(602, Door.Constructor(Vector3(621.247f, 1298.142f, 110.579f)), owning_building_guid = 183)
      LocalObject(603, Door.Constructor(Vector3(627.3662f, 1293.008f, 130.503f)), owning_building_guid = 183)
      LocalObject(614, Door.Constructor(Vector3(659.2061f, 1318.507f, 140.579f)), owning_building_guid = 183)
      LocalObject(615, Door.Constructor(Vector3(665.5197f, 1313.203f, 140.559f)), owning_building_guid = 183)
      LocalObject(616, Door.Constructor(Vector3(670.6623f, 1256.678f, 110.579f)), owning_building_guid = 183)
      LocalObject(617, Door.Constructor(Vector3(671.6845f, 1308.073f, 140.559f)), owning_building_guid = 183)
      LocalObject(618, Door.Constructor(Vector3(677.9588f, 1302.772f, 140.579f)), owning_building_guid = 183)
      LocalObject(619, Door.Constructor(Vector3(679.8865f, 1280.268f, 130.491f)), owning_building_guid = 183)
      LocalObject(725, Door.Constructor(Vector3(684.5188f, 1323.429f, 115.433f)), owning_building_guid = 183)
      LocalObject(809, Terminal.Constructor(Vector3(641.5201f, 1299.794f, 128.717f), vanu_air_vehicle_term), owning_building_guid = 183)
      LocalObject(882, VehicleSpawnPad.Constructor(Vector3(644.5638f, 1291.446f, 128.716f), vanu_vehicle_creation_pad, Vector3(0, 0, 220)), owning_building_guid = 183, terminal_guid = 809)
      LocalObject(810, Terminal.Constructor(Vector3(668.6462f, 1277.045f, 128.717f), vanu_air_vehicle_term), owning_building_guid = 183)
      LocalObject(883, VehicleSpawnPad.Constructor(Vector3(659.8969f, 1278.58f, 128.716f), vanu_vehicle_creation_pad, Vector3(0, 0, 220)), owning_building_guid = 183, terminal_guid = 810)
      LocalObject(825, Terminal.Constructor(Vector3(650.4069f, 1301.409f, 111.3f), vanu_equipment_term), owning_building_guid = 183)
      LocalObject(829, Terminal.Constructor(Vector3(668.792f, 1285.982f, 111.3f), vanu_equipment_term), owning_building_guid = 183)
      LocalObject(888, Terminal.Constructor(Vector3(663.0067f, 1297.969f, 113.8f), vanu_vehicle_term), owning_building_guid = 183)
      LocalObject(884, VehicleSpawnPad.Constructor(Vector3(672.8193f, 1309.495f, 111.205f), vanu_vehicle_creation_pad, Vector3(0, 0, 40)), owning_building_guid = 183, terminal_guid = 888)
    }

    Building10002()

    def Building10002(): Unit = { // Name: S_ATPlant Type: vanu_vehicle_station GUID: 184, MapID: 10002
      LocalBuilding("S_ATPlant", 184, 10002, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(1188.77f, 698.47f, 110f), vanu_vehicle_station)))
      LocalObject(818, CaptureTerminal.Constructor(vanu_control_console), owning_building_guid = 184)
      LocalObject(695, Door.Constructor(Vector3(1192.092f, 683.2264f, 111.779f)), owning_building_guid = 184)
      LocalObject(696, Door.Constructor(Vector3(1197.018f, 739.5303f, 131.703f)), owning_building_guid = 184)
      LocalObject(697, Door.Constructor(Vector3(1197.714f, 747.488f, 111.779f)), owning_building_guid = 184)
      LocalObject(698, Door.Constructor(Vector3(1216.706f, 689.2009f, 131.691f)), owning_building_guid = 184)
      LocalObject(699, Door.Constructor(Vector3(1234.035f, 703.6877f, 141.779f)), owning_building_guid = 184)
      LocalObject(700, Door.Constructor(Vector3(1234.779f, 711.868f, 141.759f)), owning_building_guid = 184)
      LocalObject(701, Door.Constructor(Vector3(1235.445f, 719.8604f, 141.759f)), owning_building_guid = 184)
      LocalObject(702, Door.Constructor(Vector3(1236.169f, 728.0746f, 141.779f)), owning_building_guid = 184)
      LocalObject(726, Door.Constructor(Vector3(1254.719f, 710.1626f, 116.633f)), owning_building_guid = 184)
      LocalObject(811, Terminal.Constructor(Vector3(1207.619f, 696.5599f, 129.917f), vanu_air_vehicle_term), owning_building_guid = 184)
      LocalObject(885, VehicleSpawnPad.Constructor(Vector3(1203.858f, 704.6073f, 129.916f), vanu_vehicle_creation_pad, Vector3(0, 0, -85)), owning_building_guid = 184, terminal_guid = 811)
      LocalObject(812, Terminal.Constructor(Vector3(1210.696f, 731.829f, 129.917f), vanu_air_vehicle_term), owning_building_guid = 184)
      LocalObject(886, VehicleSpawnPad.Constructor(Vector3(1205.603f, 724.5471f, 129.916f), vanu_vehicle_creation_pad, Vector3(0, 0, -85)), owning_building_guid = 184, terminal_guid = 812)
      LocalObject(854, Terminal.Constructor(Vector3(1215.024f, 701.5666f, 112.5f), vanu_equipment_term), owning_building_guid = 184)
      LocalObject(855, Terminal.Constructor(Vector3(1217.116f, 725.4753f, 112.5f), vanu_equipment_term), owning_building_guid = 184)
      LocalObject(889, Terminal.Constructor(Vector3(1221.525f, 713.1812f, 115f), vanu_vehicle_term), owning_building_guid = 184)
      LocalObject(887, VehicleSpawnPad.Constructor(Vector3(1236.595f, 711.7543f, 112.405f), vanu_vehicle_creation_pad, Vector3(0, 0, 95)), owning_building_guid = 184, terminal_guid = 889)
    }

    Building10273()

    def Building10273(): Unit = { // Name: GW_Cavern4_W Type: warpgate_cavern GUID: 185, MapID: 10273
      LocalBuilding("GW_Cavern4_W", 185, 10273, FoundationBuilder(WarpGate.Structure(Vector3(125.73f, 933.91f, 100.82f))))
    }

    Building10180()

    def Building10180(): Unit = { // Name: GW_Cavern4_S Type: warpgate_cavern GUID: 186, MapID: 10180
      LocalBuilding("GW_Cavern4_S", 186, 10180, FoundationBuilder(WarpGate.Structure(Vector3(856.92f, 191.61f, 101.24f))))
    }

    Building10178()

    def Building10178(): Unit = { // Name: GW_Cavern4_N Type: warpgate_cavern GUID: 187, MapID: 10178
      LocalBuilding("GW_Cavern4_N", 187, 10178, FoundationBuilder(WarpGate.Structure(Vector3(934.2f, 1858.96f, 81.44f))))
    }

    Building10179()

    def Building10179(): Unit = { // Name: GW_Cavern4_E Type: warpgate_cavern GUID: 188, MapID: 10179
      LocalBuilding("GW_Cavern4_E", 188, 10179, FoundationBuilder(WarpGate.Structure(Vector3(1701.9f, 1247.33f, 81.06f))))
    }

    ZoneOwnedObjects()

    def ZoneOwnedObjects(): Unit = {
      LocalObject(831, Terminal.Constructor(Vector3(768.65f, 1172.64f, 109.09f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(832, Terminal.Constructor(Vector3(778.06f, 1189.82f, 109.09f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(833, Terminal.Constructor(Vector3(785.86f, 1163.1f, 109.09f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(834, Terminal.Constructor(Vector3(791.15f, 1304.88f, 162.68f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(835, Terminal.Constructor(Vector3(795.55f, 1180.29f, 109.09f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(837, Terminal.Constructor(Vector3(799.83f, 1295.35f, 162.68f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(838, Terminal.Constructor(Vector3(835.57f, 1094.07f, 193.71f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(839, Terminal.Constructor(Vector3(845.78f, 1062f, 193.71f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(840, Terminal.Constructor(Vector3(891.69f, 781.33f, 118.5f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(841, Terminal.Constructor(Vector3(892.08f, 790.5f, 118.5f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(842, Terminal.Constructor(Vector3(1013.04f, 734.64f, 124.36f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(844, Terminal.Constructor(Vector3(1021.43f, 716.77f, 124.36f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(845, Terminal.Constructor(Vector3(1030.94f, 742.92f, 124.36f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(847, Terminal.Constructor(Vector3(1039.09f, 725.04f, 124.36f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(848, Terminal.Constructor(Vector3(1046.56f, 1319.12f, 116.4f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(849, Terminal.Constructor(Vector3(1048.6f, 1352.78f, 116.4f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(850, Terminal.Constructor(Vector3(1085.88f, 680.98f, 116.8f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(851, Terminal.Constructor(Vector3(1088.05f, 714.6f, 116.8f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(852, Terminal.Constructor(Vector3(1147.46f, 1067.68f, 198.82f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(853, Terminal.Constructor(Vector3(1171.04f, 1053.54f, 198.82f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(864, Terminal.Constructor(Vector3(1361.59f, 876.44f, 121.5f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(865, Terminal.Constructor(Vector3(1374.98f, 865.79f, 121.5f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(866, Terminal.Constructor(Vector3(1421.6f, 1202.05f, 130.8f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(867, Terminal.Constructor(Vector3(1430.67f, 1192.45f, 130.8f), vanu_equipment_term), owning_building_guid = 0)
      LocalObject(20, ProximityTerminal.Constructor(Vector3(767.07f, 1043.25f, 107.89f), crystals_health_a), owning_building_guid = 0)
      LocalObject(21, ProximityTerminal.Constructor(Vector3(847f, 673.56f, 120f), crystals_health_a), owning_building_guid = 0)
      LocalObject(22, ProximityTerminal.Constructor(Vector3(881.98f, 1036.96f, 108.75f), crystals_health_a), owning_building_guid = 0)
      LocalObject(23, ProximityTerminal.Constructor(Vector3(899.09f, 1236.49f, 108.2f), crystals_health_a), owning_building_guid = 0)
      LocalObject(24, ProximityTerminal.Constructor(Vector3(1110.72f, 824.41f, 121.1f), crystals_health_a), owning_building_guid = 0)
      LocalObject(25, ProximityTerminal.Constructor(Vector3(1134.79f, 901.31f, 121.2f), crystals_health_a), owning_building_guid = 0)
      LocalObject(26, ProximityTerminal.Constructor(Vector3(1253.3f, 841.23f, 131.86f), crystals_health_a), owning_building_guid = 0)
      LocalObject(27, ProximityTerminal.Constructor(Vector3(1360.82f, 675.16f, 127.1f), crystals_health_a), owning_building_guid = 0)
      LocalObject(28, ProximityTerminal.Constructor(Vector3(603.17f, 847.99f, 135.54f), crystals_health_b), owning_building_guid = 0)
      LocalObject(29, ProximityTerminal.Constructor(Vector3(606.13f, 977.59f, 112.1f), crystals_health_b), owning_building_guid = 0)
      LocalObject(30, ProximityTerminal.Constructor(Vector3(619.93f, 1098.07f, 108.2f), crystals_health_b), owning_building_guid = 0)
      LocalObject(31, ProximityTerminal.Constructor(Vector3(732.74f, 1226.37f, 160.93f), crystals_health_b), owning_building_guid = 0)
      LocalObject(32, ProximityTerminal.Constructor(Vector3(798.31f, 1142.05f, 108.2f), crystals_health_b), owning_building_guid = 0)
      LocalObject(33, ProximityTerminal.Constructor(Vector3(844.64f, 1384.9f, 108.2f), crystals_health_b), owning_building_guid = 0)
      LocalObject(34, ProximityTerminal.Constructor(Vector3(984.09f, 1019.83f, 101.27f), crystals_health_b), owning_building_guid = 0)
      LocalObject(35, ProximityTerminal.Constructor(Vector3(1064.74f, 889.79f, 124.1f), crystals_health_b), owning_building_guid = 0)
      LocalObject(36, ProximityTerminal.Constructor(Vector3(1144.21f, 1082.92f, 125.8f), crystals_health_b), owning_building_guid = 0)
      LocalObject(37, ProximityTerminal.Constructor(Vector3(1180.8f, 1176.04f, 162.7f), crystals_health_b), owning_building_guid = 0)
      LocalObject(38, ProximityTerminal.Constructor(Vector3(1370.85f, 1290.92f, 135.45f), crystals_health_b), owning_building_guid = 0)
      LocalObject(743, ProximityTerminal.Constructor(Vector3(1042.73f, 1094.72f, 213.31f), crystals_health_a), owning_building_guid = 0)
      LocalObject(744, ProximityTerminal.Constructor(Vector3(1308.76f, 941.45f, 121.06f), crystals_health_a), owning_building_guid = 0)
      LocalObject(745, ProximityTerminal.Constructor(Vector3(675.37f, 1119.01f, 169.46f), crystals_health_b), owning_building_guid = 0)
      LocalObject(746, ProximityTerminal.Constructor(Vector3(874.59f, 868.25f, 124f), crystals_health_b), owning_building_guid = 0)
      LocalObject(747, ProximityTerminal.Constructor(Vector3(883.88f, 1139.21f, 183.94f), crystals_health_b), owning_building_guid = 0)
      LocalObject(748, ProximityTerminal.Constructor(Vector3(1065.61f, 1231.69f, 124f), crystals_health_b), owning_building_guid = 0)
      LocalObject(749, ProximityTerminal.Constructor(Vector3(1128.28f, 600.54f, 118.54f), crystals_health_b), owning_building_guid = 0)
      LocalObject(750, ProximityTerminal.Constructor(Vector3(1281.84f, 1173.09f, 134.92f), crystals_health_b), owning_building_guid = 0)
    }

    def Lattice(): Unit = {
      LatticeLink("N_Redoubt", "N_ATPlant")
      LatticeLink("GW_Cavern4_W", "N_ATPlant")
      LatticeLink("N_Stasis", "Core")
      LatticeLink("S_Stasis", "Core")
      LatticeLink("S_Redoubt", "S_ATPlant")
      LatticeLink("N_Redoubt", "N_Stasis")
      LatticeLink("S_Redoubt", "S_Stasis")
      LatticeLink("S_Stasis", "N_ATPlant")
      LatticeLink("N_Stasis", "S_ATPlant")
      LatticeLink("GW_Cavern4_N", "N_Redoubt")
      LatticeLink("GW_Cavern4_S", "S_Redoubt")
      LatticeLink("GW_Cavern4_E", "S_ATPlant")
    }

    Lattice()

    def ZipLines(): Unit = {
      ZipLinePaths(new ZipLinePath(1, false, List(Vector3(581.513f, 991.906f, 137.91f), Vector3(647.981f, 1071.977f, 136.15f))))
      ZipLinePaths(new ZipLinePath(2, true, List(Vector3(626.942f, 1309.392f, 111.65f), Vector3(624.976f, 1314.648f, 129.076f))))
      ZipLinePaths(new ZipLinePath(3, true, List(Vector3(664.909f, 1132.887f, 135.55f), Vector3(739.272f, 1093.882f, 108.549f))))
      ZipLinePaths(new ZipLinePath(4, true, List(Vector3(682.606f, 1258.328f, 129.075f), Vector3(681.167f, 1263.188f, 111.65f))))
      ZipLinePaths(new ZipLinePath(5, false, List(Vector3(691.973f, 1020.927f, 175.717f), Vector3(693.182f, 1024.821f, 175.258f), Vector3(711.325f, 1083.23f, 165.286f))))
      ZipLinePaths(new ZipLinePath(6, true, List(Vector3(725.251f, 1223.128f, 139.881f), Vector3(728.817f, 1227.398f, 161.281f))))
      ZipLinePaths(new ZipLinePath(7, true, List(Vector3(737.366f, 827.503f, 151.8f), Vector3(726.502f, 823.006f, 118.25f))))
      ZipLinePaths(new ZipLinePath(8, true, List(Vector3(735.172f, 1225.683f, 108.549f), Vector3(688.37f, 1136.702f, 135.65f))))
      ZipLinePaths(new ZipLinePath(9, true, List(Vector3(736.016f, 1225.55f, 161.283f), Vector3(739.078f, 1230.956f, 139.881f))))
      ZipLinePaths(new ZipLinePath(10, false, List(Vector3(739.34f, 1233.309f, 161.821f), Vector3(707.454f, 1294.883f, 129.621f))))
      ZipLinePaths(new ZipLinePath(11, true, List(Vector3(744.27f, 823.732f, 118.25f), Vector3(733.396f, 821.193f, 151.8f))))
      ZipLinePaths(new ZipLinePath(12, true, List(Vector3(791.397f, 1293.352f, 135.65f), Vector3(758.017f, 1258.464f, 122.944f), Vector3(732.648f, 1231.949f, 116.434f), Vector3(727.975f, 1227.065f, 117.548f), Vector3(723.969f, 1222.879f, 117.311f), Vector3(723.302f, 1222.181f, 108.549f))))
      ZipLinePaths(new ZipLinePath(13, true, List(Vector3(798.178f, 1075.67f, 108.549f), Vector3(661.15f, 1118.363f, 135.65f))))
      ZipLinePaths(new ZipLinePath(14, true, List(Vector3(806.089f, 1287.723f, 135.65f), Vector3(831.731f, 1254.03f, 124.546f), Vector3(838.141f, 1245.607f, 124.791f), Vector3(839.889f, 1243.31f, 126.432f), Vector3(852.71f, 1226.464f, 123.637f), Vector3(863.783f, 1211.914f, 108.549f))))
      ZipLinePaths(new ZipLinePath(15, true, List(Vector3(833.04f, 1260.868f, 108.549f), Vector3(819.031f, 1323.108f, 130.591f))))
      ZipLinePaths(new ZipLinePath(16, false, List(Vector3(851.698f, 1111.512f, 200.062f), Vector3(852.693f, 1113.453f, 199.565f), Vector3(867.626f, 1142.578f, 179.294f))))
      ZipLinePaths(new ZipLinePath(17, true, List(Vector3(867.508f, 786.909f, 109.085f), Vector3(866.403f, 786.948f, 109.085f))))
      ZipLinePaths(new ZipLinePath(18, false, List(Vector3(869.055f, 1094.282f, 194.562f), Vector3(975.227f, 1037.114f, 182.263f))))
      ZipLinePaths(new ZipLinePath(19, true, List(Vector3(872.768f, 1156.774f, 178.792f), Vector3(872.526f, 1156.915f, 184.299f))))
      ZipLinePaths(new ZipLinePath(20, false, List(Vector3(877.117f, 1420.782f, 133.161f), Vector3(857.119f, 1366.35f, 136.091f))))
      ZipLinePaths(new ZipLinePath(21, false, List(Vector3(879.233f, 1301.494f, 180.934f), Vector3(878.289f, 1302.333f, 180.345f), Vector3(849.027f, 1328.33f, 163.573f))))
      ZipLinePaths(new ZipLinePath(22, false, List(Vector3(897.112f, 1147.069f, 179.322f), Vector3(1020.979f, 1092.058f, 208.671f))))
      ZipLinePaths(new ZipLinePath(23, false, List(Vector3(916.429f, 1293.681f, 180.655f), Vector3(918.128f, 1294.889f, 180.196f), Vector3(1025.18f, 1371.005f, 116.593f))))
      ZipLinePaths(new ZipLinePath(24, false, List(Vector3(944.17f, 535.579f, 137.951f), Vector3(921.003f, 693.13f, 130.68f))))
      ZipLinePaths(new ZipLinePath(25, false, List(Vector3(952.003f, 869.708f, 169.18f), Vector3(952.645f, 872.988f, 169.476f), Vector3(972.562f, 974.671f, 172.23f))))
      ZipLinePaths(new ZipLinePath(26, true, List(Vector3(959.837f, 871.038f, 137.15f), Vector3(949.586f, 868.014f, 168.681f))))
      ZipLinePaths(new ZipLinePath(27, false, List(Vector3(961.064f, 985.742f, 172.285f), Vector3(958.734f, 983.378f, 171.793f), Vector3(886.507f, 910.111f, 115.886f))))
      ZipLinePaths(new ZipLinePath(28, true, List(Vector3(961.268f, 870.931f, 168.673f), Vector3(951.672f, 867.573f, 137.15f))))
      ZipLinePaths(new ZipLinePath(29, false, List(Vector3(965.127f, 1028.015f, 177.262f), Vector3(865.98f, 1067.626f, 200.071f))))
      ZipLinePaths(new ZipLinePath(30, true, List(Vector3(972.362f, 1005.172f, 176.75f), Vector3(972.277f, 1005.349f, 181.75f))))
      ZipLinePaths(new ZipLinePath(31, true, List(Vector3(973.345f, 1005.29f, 166.75f), Vector3(973.617f, 1005.373f, 171.75f))))
      ZipLinePaths(new ZipLinePath(32, true, List(Vector3(993.984f, 986.374f, 171.75f), Vector3(992.517f, 978.742f, 176.75f))))
      ZipLinePaths(new ZipLinePath(33, true, List(Vector3(997.002f, 1028.654f, 176.75f), Vector3(998.338f, 1029.743f, 166.75f))))
      ZipLinePaths(new ZipLinePath(34, true, List(Vector3(1015.197f, 1001.46f, 181.75f), Vector3(1015.881f, 1001.237f, 176.75f))))
      ZipLinePaths(new ZipLinePath(35, true, List(Vector3(1015.632f, 1001.636f, 171.75f), Vector3(1015.663f, 1002.099f, 166.75f))))
      ZipLinePaths(new ZipLinePath(36, false, List(Vector3(1055.766f, 1084.677f, 214.167f), Vector3(1146.026f, 1093.29f, 199.697f))))
      ZipLinePaths(new ZipLinePath(37, false, List(Vector3(1081.513f, 906.371f, 141.875f), Vector3(1023.761f, 976.866f, 171.749f), Vector3(1021.898f, 979.14f, 172.226f))))
      ZipLinePaths(new ZipLinePath(38, false, List(Vector3(1114.566f, 973.674f, 183.961f), Vector3(1024.122f, 981.71f, 177.251f))))
      ZipLinePaths(new ZipLinePath(39, false, List(Vector3(1115.268f, 685.916f, 123.153f), Vector3(1181.004f, 708.419f, 130.773f))))
      ZipLinePaths(new ZipLinePath(40, false, List(Vector3(1123.754f, 557.336f, 129.97f), Vector3(1158.099f, 615.656f, 128.58f))))
      ZipLinePaths(new ZipLinePath(41, false, List(Vector3(1124.52f, 1165.66f, 148.45f), Vector3(1148.819f, 1098.341f, 199.221f), Vector3(1150.439f, 1093.853f, 199.695f))))
      ZipLinePaths(new ZipLinePath(42, false, List(Vector3(1134.069f, 1054.513f, 199.724f), Vector3(1065.117f, 1089.592f, 208.634f))))
      ZipLinePaths(new ZipLinePath(43, false, List(Vector3(1164.887f, 1028.954f, 199.752f), Vector3(1164.038f, 1022.243f, 199.227f), Vector3(1158.098f, 975.268f, 183.98f))))
      ZipLinePaths(new ZipLinePath(44, false, List(Vector3(1169.577f, 1152.768f, 195.641f), Vector3(1028.773f, 1021.258f, 182.191f))))
      ZipLinePaths(new ZipLinePath(45, false, List(Vector3(1170.844f, 624.946f, 127.89f), Vector3(1225.847f, 674.335f, 130.77f))))
      ZipLinePaths(new ZipLinePath(46, true, List(Vector3(1171.626f, 1156.954f, 163.054f), Vector3(1173.541f, 1158.297f, 195.14f))))
      ZipLinePaths(new ZipLinePath(47, true, List(Vector3(1173.21f, 1162.139f, 195.14f), Vector3(1167.157f, 1165.41f, 163.054f))))
      ZipLinePaths(new ZipLinePath(48, false, List(Vector3(1189.596f, 755.543f, 130.771f), Vector3(1205.016f, 803.983f, 126.811f))))
      ZipLinePaths(new ZipLinePath(49, true, List(Vector3(1203.213f, 674.437f, 130.276f), Vector3(1203.45f, 682.914f, 112.85f))))
      ZipLinePaths(new ZipLinePath(50, true, List(Vector3(1209.026f, 745.489f, 112.85f), Vector3(1209.818f, 754.294f, 130.276f))))
      ZipLinePaths(new ZipLinePath(51, false, List(Vector3(1263.84f, 868.575f, 164.611f), Vector3(1093.376f, 876.512f, 155.131f))))
      ZipLinePaths(new ZipLinePath(52, false, List(Vector3(1318.75f, 680.79f, 127.95f), Vector3(1250.792f, 681.997f, 130.77f))))
      ZipLinePaths(new ZipLinePath(53, false, List(Vector3(1324.778f, 1160.543f, 121.851f), Vector3(1374.004f, 1173.648f, 125.151f))))
      ZipLinePaths(new ZipLinePath(54, false, List(Vector3(1326.534f, 812.464f, 127.903f), Vector3(1272.628f, 860.86f, 164.679f))))
      ZipLinePaths(new ZipLinePath(55, false, List(Vector3(1395.087f, 937.296f, 108.756f), Vector3(1392.3f, 970.925f, 138.355f), Vector3(1391.902f, 975.729f, 138.032f))))
      ZipLinePaths(new ZipLinePath(56, true, List(Vector3(986.787f, 1005.346f, 110.778f), Vector3(978.945f, 1022.841f, 166.75f))))
      ZipLinePaths(new ZipLinePath(57, true, List(Vector3(1011.11f, 984.732f, 166.75f), Vector3(987.858f, 1005.35f, 101.621f))))
      ZipLinePaths(new ZipLinePath(58, false, List(Vector3(960.539f, 879.005f, 137.679f), Vector3(961.773f, 885.076f, 137.23f), Vector3(980.297f, 976.141f, 102.151f))))
      ZipLinePaths(new ZipLinePath(59, false, List(Vector3(1183.993f, 1068.618f, 199.718f), Vector3(1190.361f, 1066.146f, 199.274f), Vector3(1387.772f, 989.533f, 138.009f))))
      ZipLinePaths(new ZipLinePath(60, false, List(Vector3(1084.42f, 1046.639f, 114.856f), Vector3(1075.727f, 1042.173f, 114.359f), Vector3(1014.875f, 1010.907f, 102.126f))))
      ZipLinePaths(new ZipLinePath(61, false, List(Vector3(1010.405f, 982.383f, 102.144f), Vector3(1050.911f, 927.105f, 124.33f), Vector3(1053.612f, 923.42f, 124.874f))))
      ZipLinePaths(new ZipLinePath(62, false, List(Vector3(677.233f, 983.867f, 175.757f), Vector3(671.145f, 983.323f, 175.268f), Vector3(579.83f, 975.152f, 138.106f))))
      ZipLinePaths(new ZipLinePath(63, false, List(Vector3(847.264f, 1345.05f, 163.558f), Vector3(848.71f, 1347.394f, 163.079f), Vector3(893.565f, 1420.056f, 133.129f))))
      ZipLinePaths(new ZipLinePath(64, false, List(Vector3(1122.172f, 840.008f, 162.85f), Vector3(960.065f, 860.059f, 169.19f))))
      ZipLinePaths(new ZipLinePath(65, false, List(Vector3(1126.119f, 849.46f, 162.859f), Vector3(1159.44f, 957.63f, 183.969f))))
      ZipLinePaths(new ZipLinePath(66, true, List(Vector3(1130.343f, 837.741f, 132.35f), Vector3(1122.353f, 845.266f, 162.35f))))
      ZipLinePaths(new ZipLinePath(67, true, List(Vector3(1128.075f, 844.958f, 162.35f), Vector3(1122.301f, 843.642f, 132.35f))))
      ZipLinePaths(new ZipLinePath(68, false, List(Vector3(1280.401f, 949.391f, 114.45f), Vector3(1180.426f, 1027.86f, 199.264f), Vector3(1175.094f, 1032.045f, 199.72f))))
      ZipLinePaths(new ZipLinePath(69, false, List(Vector3(1070.591f, 880.007f, 141.902f), Vector3(1094.877f, 919.673f, 164.819f), Vector3(1117.258f, 956.228f, 184.011f))))
      ZipLinePaths(new ZipLinePath(70, false, List(Vector3(1037.273f, 821.082f, 124.85f), Vector3(991.292f, 822.466f, 124.95f))))
      ZipLinePaths(new ZipLinePath(71, false, List(Vector3(738.475f, 885.729f, 133.976f), Vector3(732.984f, 829.777f, 152.327f))))
      ZipLinePaths(new ZipLinePath(72, false, List(Vector3(738.037f, 819.262f, 152.3f), Vector3(688.763f, 746.446f, 155.98f))))
      ZipLinePaths(new ZipLinePath(73, false, List(Vector3(1201.659f, 845.23f, 109.05f), Vector3(1202.098f, 839.399f, 126.767f), Vector3(1202.245f, 837.456f, 126.802f))))
      ZipLinePaths(new ZipLinePath(74, false, List(Vector3(1223.133f, 796.668f, 109.055f), Vector3(1221.229f, 800.077f, 126.927f), Vector3(1219.326f, 803.487f, 126.811f))))
      ZipLinePaths(new ZipLinePath(75, false, List(Vector3(1165.269f, 852.174f, 109.043f), Vector3(1164.509f, 857.593f, 122.345f), Vector3(1164.256f, 859.399f, 122.05f))))
      ZipLinePaths(new ZipLinePath(76, true, List(Vector3(766.451f, 1184.81f, 109.436f), Vector3(782.652f, 1188.998f, 119.48f))))
      ZipLinePaths(new ZipLinePath(77, true, List(Vector3(773.648f, 1170.141f, 119.48f), Vector3(797.279f, 1167.873f, 109.436f))))
      ZipLinePaths(new ZipLinePath(78, false, List(Vector3(919.547f, 1267.287f, 118.234f), Vector3(884.979f, 1164.413f, 179.325f))))
      ZipLinePaths(new ZipLinePath(79, false, List(Vector3(1016.912f, 1194.737f, 122.496f), Vector3(899.344f, 1154.515f, 179.438f), Vector3(895.552f, 1153.218f, 179.356f))))
      ZipLinePaths(new ZipLinePath(80, true, List(Vector3(1033.725f, 713.661f, 124.706f), Vector3(1018.227f, 720.002f, 134.75f))))
      ZipLinePaths(new ZipLinePath(81, true, List(Vector3(1036.573f, 730.427f, 134.75f), Vector3(1018.869f, 745.466f, 124.706f))))
      ZipLinePaths(new ZipLinePath(82, false, List(Vector3(1123.651f, 972.872f, 125.04f), Vector3(1139.528f, 1077.659f, 126.65f))))
      ZipLinePaths(new ZipLinePath(83, false, List(Vector3(1349.973f, 835.703f, 127.85f), Vector3(1298.615f, 864.67f, 126.95f))))
      ZipLinePaths(new ZipLinePath(84, false, List(Vector3(614.049f, 988.948f, 120.578f), Vector3(588.185f, 985.552f, 137.464f), Vector3(584.619f, 985.067f, 137.927f))))
      ZipLinePaths(new ZipLinePath(85, false, List(Vector3(753.458f, 1014.26f, 116.412f), Vector3(825.993f, 1066.964f, 194.024f), Vector3(828.333f, 1068.664f, 194.561f))))
      ZipLinePaths(new ZipLinePath(86, false, List(Vector3(850.041f, 1042.272f, 140.472f), Vector3(743.271f, 1019.952f, 175.179f), Vector3(736.153f, 1018.464f, 175.701f))))
      ZipLinePaths(new ZipLinePath(87, false, List(Vector3(928.243f, 1122.438f, 136.017f), Vector3(899.093f, 1259.06f, 183.358f))))
      ZipLinePaths(new ZipLinePath(88, false, List(Vector3(952.197f, 1363.522f, 117.919f), Vector3(898.492f, 1426.42f, 132.868f))))
      ZipLinePaths(new ZipLinePath(89, false, List(Vector3(926.366f, 1092.493f, 136.033f), Vector3(929.541f, 1088.451f, 135.574f), Vector3(977.178f, 1027.828f, 102.161f))))
      ZipLinePaths(new ZipLinePath(90, false, List(Vector3(960.002f, 1008.314f, 102.157f), Vector3(891.126f, 1051.974f, 135.668f), Vector3(886.534f, 1054.885f, 136.126f))))
      ZipLinePaths(new ZipLinePath(91, false, List(Vector3(207.89f, 910.311f, 113.14f), Vector3(293.154f, 909.306f, 116.84f), Vector3(321.318f, 889.302f, 118.053f), Vector3(353.3f, 877.25f, 115.91f), Vector3(378.041f, 884.824f, 115.139f), Vector3(413.882f, 910.698f, 115.967f), Vector3(436.729f, 918.823f, 113.12f))))
      ZipLinePaths(new ZipLinePath(92, false, List(Vector3(427.575f, 953.375f, 113.14f), Vector3(460.915f, 1023.964f, 116.107f), Vector3(474.954f, 1054.653f, 116.138f), Vector3(515.093f, 1076.339f, 115.67f), Vector3(536.745f, 1079.732f, 111.65f))))
      ZipLinePaths(new ZipLinePath(93, false, List(Vector3(479.791f, 936.61f, 113.388f), Vector3(495.652f, 937.365f, 119.11f), Vector3(502.613f, 938.121f, 118.339f), Vector3(503.635f, 963.631f, 123.797f), Vector3(503.538f, 978.868f, 123.3f), Vector3(513.642f, 982.705f, 123.308f))))
      ZipLinePaths(new ZipLinePath(94, false, List(Vector3(1679.359f, 1156.319f, 93.135f), Vector3(1679.016f, 1150.787f, 96.119f), Vector3(1646.872f, 1086.92f, 97.109f), Vector3(1633.827f, 1073.554f, 96.398f), Vector3(1586.838f, 1075.32f, 112.677f), Vector3(1568.938f, 1072.514f, 113.155f))))
      ZipLinePaths(new ZipLinePath(95, false, List(Vector3(1557.496f, 1031.021f, 113.14f), Vector3(1522.762f, 960.766f, 115.661f), Vector3(1506.029f, 928.012f, 115.536f), Vector3(1470.937f, 908.527f, 116.235f), Vector3(1444.142f, 919.885f, 113.738f), Vector3(1432.746f, 921.543f, 109.47f))))
      ZipLinePaths(new ZipLinePath(96, false, List(Vector3(842.904f, 332.934f, 113.14f), Vector3(828.167f, 338.012f, 114.07f), Vector3(816.33f, 353.302f, 115.603f), Vector3(754.962f, 479.348f, 116.62f), Vector3(781.494f, 530.593f, 110.14f))))
      ZipLinePaths(new ZipLinePath(97, true, List(Vector3(788.315f, 539.096f, 109.284f), Vector3(805.739f, 539.416f, 116.084f), Vector3(810.607f, 538.671f, 125.568f), Vector3(835.92f, 534.799f, 143.575f), Vector3(884.599f, 527.353f, 152.231f), Vector3(888.493f, 526.757f, 174.148f), Vector3(938.471f, 520.011f, 137.591f))))
      ZipLinePaths(new ZipLinePath(98, false, List(Vector3(731.311f, 591.258f, 109.766f), Vector3(731.638f, 608.153f, 124.1f), Vector3(736.713f, 637.551f, 137.163f), Vector3(738.405f, 647.35f, 137.407f))))
      ZipLinePaths(new ZipLinePath(99, false, List(Vector3(858.162f, 343.607f, 113.461f), Vector3(858.386f, 357.982f, 113.64f), Vector3(871.709f, 363.657f, 113.074f), Vector3(871.476f, 375.138f, 116.324f), Vector3(871.544f, 386.62f, 118.074f), Vector3(873.379f, 405.483f, 118.475f), Vector3(882.869f, 406.77f, 118.381f))))
      ZipLinePaths(new ZipLinePath(100, false, List(Vector3(920.189f, 433.581f, 128.311f), Vector3(919.775f, 444.103f, 132.255f), Vector3(923.762f, 454.626f, 133.096f), Vector3(934.043f, 460.273f, 133.773f), Vector3(933.925f, 469.42f, 133.551f), Vector3(921.08f, 474.627f, 133.209f), Vector3(922.658f, 483.881f, 133.137f), Vector3(935.235f, 493.135f, 133.566f), Vector3(936.146f, 498.55f, 133.381f))))
      ZipLinePaths(new ZipLinePath(101, false, List(Vector3(1422.417f, 983.83f, 133.314f), Vector3(1428.703f, 983.946f, 132.72f), Vector3(1434.989f, 984.061f, 130.225f), Vector3(1448.161f, 989.292f, 128.737f), Vector3(1448.105f, 1017.954f, 128.183f))))
      ZipLinePaths(new ZipLinePath(102, false, List(Vector3(1494.966f, 1049.656f, 123.828f), Vector3(1498.045f, 1062.638f, 122.913f), Vector3(1511.389f, 1066.815f, 122.819f), Vector3(1511.932f, 1073.712f, 122.582f), Vector3(1512.476f, 1080.609f, 120.845f), Vector3(1513.562f, 1094.403f, 118.271f), Vector3(1526.748f, 1094.198f, 116.998f), Vector3(1539.934f, 1093.992f, 113.226f))))
      ZipLinePaths(new ZipLinePath(103, false, List(Vector3(939.789f, 207.271f, 113.209f), Vector3(958.515f, 207.177f, 115.548f), Vector3(977.242f, 207.084f, 110.91f), Vector3(1027.178f, 206.836f, 95.72f), Vector3(1076.155f, 206.592f, 80.509f), Vector3(1078.076f, 206.583f, 75.2f))))
      ZipLinePaths(new ZipLinePath(104, false, List(Vector3(1083.268f, 239.176f, 74.184f), Vector3(1146.821f, 272.079f, 79.019f), Vector3(1181.375f, 271.281f, 88.881f), Vector3(1201.423f, 272.171f, 95.01f), Vector3(1221.471f, 273.061f, 93.145f))))
      ZipLinePaths(new ZipLinePath(105, false, List(Vector3(1254.623f, 265.032f, 93.14f), Vector3(1277.415f, 310.652f, 95.518f), Vector3(1300.207f, 356.271f, 95.29f), Vector3(1322.999f, 401.891f, 95.062f), Vector3(1324.786f, 405.469f, 93.14f))))
      ZipLinePaths(new ZipLinePath(106, false, List(Vector3(1287.494f, 431.131f, 93.143f), Vector3(1216.539f, 429.215f, 116.06f), Vector3(1183.181f, 442.797f, 115.261f), Vector3(1168.423f, 474.38f, 115.962f), Vector3(1167.006f, 513.135f, 110.994f))))
      ZipLinePaths(new ZipLinePath(107, false, List(Vector3(947.304f, 1751.256f, 93.143f), Vector3(1022.07f, 1715.558f, 97.088f), Vector3(1088.146f, 1715.014f, 116.168f), Vector3(1121.539f, 1698.073f, 114.974f), Vector3(1135.688f, 1672.479f, 113.142f))))
      ZipLinePaths(new ZipLinePath(108, false, List(Vector3(1156.156f, 1614.299f, 113.14f), Vector3(1165.89f, 1600.735f, 115.415f), Vector3(1166.124f, 1536.271f, 116.594f), Vector3(1170.068f, 1480.177f, 113.813f), Vector3(1163.413f, 1447.482f, 109.93f))))
      ZipLinePaths(new ZipLinePath(109, false, List(Vector3(866.689f, 1803.032f, 91.411f), Vector3(823.044f, 1805.965f, 100.645f), Vector3(773.898f, 1805.898f, 113.754f), Vector3(762.335f, 1805.887f, 113.15f))))
      ZipLinePaths(new ZipLinePath(110, false, List(Vector3(719.69f, 1792.509f, 113.143f), Vector3(719.386f, 1776.864f, 111.112f), Vector3(718.475f, 1729.929f, 95.817f), Vector3(687.661f, 1660.449f, 96.011f), Vector3(702.261f, 1629.14f, 93.723f), Vector3(746.162f, 1610.03f, 93.143f))))
      ZipLinePaths(new ZipLinePath(111, false, List(Vector3(785.498f, 1606.097f, 93.146f), Vector3(785.599f, 1535.452f, 116.464f), Vector3(804.299f, 1500.206f, 115.294f), Vector3(814.583f, 1481.916f, 113.147f))))
      ZipLinePaths(new ZipLinePath(112, false, List(Vector3(739.761f, 1220.297f, 161.795f), Vector3(741.855f, 1217.845f, 161.278f), Vector3(773.272f, 1181.066f, 119.988f))))
      ZipLinePaths(new ZipLinePath(113, false, List(Vector3(788.874f, 1190.56f, 120.035f), Vector3(806.9f, 1273.41f, 163.308f), Vector3(808.102f, 1278.933f, 163.587f))))
      ZipLinePaths(new ZipLinePath(114, false, List(Vector3(773.704f, 1162.971f, 120.002f), Vector3(728.518f, 1101.093f, 164.841f), Vector3(725.506f, 1096.968f, 165.278f))))
      ZipLinePaths(new ZipLinePath(115, false, List(Vector3(781.928f, 1311.432f, 163.576f), Vector3(770.548f, 1309.973f, 163.027f), Vector3(705.455f, 1301.629f, 129.618f))))
      ZipLinePaths(new ZipLinePath(116, false, List(Vector3(671.03f, 1148.894f, 164.705f), Vector3(669.801f, 1152.329f, 164.248f), Vector3(631.716f, 1258.818f, 119.037f))))
      ZipLinePaths(new ZipLinePath(117, false, List(Vector3(888.237f, 1456.234f, 128.212f), Vector3(889.896f, 1478.612f, 123.1f), Vector3(900.126f, 1480.202f, 122.403f), Vector3(924.891f, 1479.896f, 114.905f), Vector3(932.607f, 1481.17f, 113.155f), Vector3(936.023f, 1490.444f, 112.406f), Vector3(935.556f, 1500.991f, 108.433f))))
      ZipLinePaths(new ZipLinePath(118, false, List(Vector3(903.953f, 1539.55f, 98.452f), Vector3(904.279f, 1550.875f, 103.037f), Vector3(905.605f, 1575.099f, 97.631f), Vector3(921.619f, 1576.126f, 102.303f), Vector3(934.133f, 1576.452f, 103.576f), Vector3(935.661f, 1588.105f, 103.522f))))
      ZipLinePaths(new ZipLinePath(119, false, List(Vector3(952.101f, 1628.841f, 103.426f), Vector3(951.056f, 1655.014f, 103.109f), Vector3(941.355f, 1655.54f, 103.145f), Vector3(908.955f, 1656.066f, 98.182f), Vector3(903.854f, 1665.792f, 98.018f), Vector3(907.154f, 1687.154f, 92.936f), Vector3(923.854f, 1686.517f, 93.257f))))
      ZipLinePaths(new ZipLinePath(120, false, List(Vector3(855.153f, 612.968f, 148.912f), Vector3(926.126f, 528.2f, 137.883f))))
      ZipLinePaths(new ZipLinePath(121, true, List(Vector3(1289.034f, 1388.523f, 147.739f), Vector3(1293.101f, 1393.799f, 163.074f), Vector3(1294.686f, 1400.643f, 161.175f))))
      ZipLinePaths(new ZipLinePath(122, true, List(Vector3(1311.001f, 1390.936f, 164.914f), Vector3(1311.03f, 1383.145f, 166.497f), Vector3(1311.074f, 1371.459f, 174.89f), Vector3(1311.096f, 1365.615f, 174.89f), Vector3(1311.133f, 1355.877f, 187.506f), Vector3(1311.169f, 1346.138f, 191.6f), Vector3(1311.177f, 1344.19f, 191.6f), Vector3(1310.743f, 1326.36f, 150.239f))))
      ZipLinePaths(new ZipLinePath(123, false, List(Vector3(1206.788f, 1314.093f, 136.06f), Vector3(1239.102f, 1336.775f, 144.7f), Vector3(1243.718f, 1340.015f, 145.26f))))
      ZipLinePaths(new ZipLinePath(124, false, List(Vector3(1305.939f, 1292.807f, 145.176f), Vector3(1307.056f, 1286.93f, 144.781f), Vector3(1314.874f, 1245.797f, 131.299f), Vector3(1316.483f, 1237.336f, 126.475f))))
      ZipLinePaths(new ZipLinePath(125, true, List(Vector3(635.732f, 787.036f, 144.959f), Vector3(641.69f, 782.608f, 169.61f), Vector3(677.814f, 746.027f, 177.406f), Vector3(686.856f, 736.488f, 155.479f))))
      ZipLinePaths(new ZipLinePath(126, true, List(Vector3(638.583f, 708.756f, 155.915f), Vector3(642.303f, 716.658f, 157.733f), Vector3(646.974f, 721.968f, 142.459f))))
      ZipLinePaths(new ZipLinePath(127, false, List(Vector3(700.844f, 755.122f, 137.391f), Vector3(724.482f, 799.439f, 148.167f), Vector3(734.679f, 818.556f, 152.31f))))
      ZipLinePaths(new ZipLinePath(128, false, List(Vector3(651.24f, 839.686f, 137.367f), Vector3(680.057f, 970.631f, 175.22f), Vector3(681.978f, 979.361f, 175.738f))))
      ZipLinePaths(new ZipLinePath(129, false, List(Vector3(832.657f, 618.498f, 150.19f), Vector3(790.345f, 645.137f, 151.52f), Vector3(748.032f, 671.776f, 151.043f), Vector3(705.72f, 698.415f, 150.567f), Vector3(703.181f, 700.013f, 148.89f))))
      ZipLinePaths(new ZipLinePath(130, false, List(Vector3(792.263f, 556.276f, 109.54f), Vector3(806.498f, 568.729f, 132.501f), Vector3(812.997f, 574.414f, 138.376f), Vector3(819.495f, 580.1f, 137.243f))))
      ZipLinePaths(new ZipLinePath(131, false, List(Vector3(905.264f, 797.915f, 121.166f), Vector3(949.727f, 838.795f, 137.088f), Vector3(956.079f, 844.635f, 137.665f))))
      ZipLinePaths(new ZipLinePath(132, false, List(Vector3(866.636f, 793.426f, 134.32f), Vector3(817.722f, 805.405f, 131.811f))))
      ZipLinePaths(new ZipLinePath(133, false, List(Vector3(817.815f, 812.227f, 131.813f), Vector3(851.308f, 872.076f, 124.853f))))
      ZipLinePaths(new ZipLinePath(134, false, List(Vector3(739.172f, 733.542f, 137.481f), Vector3(762.696f, 737.81f, 132.936f), Vector3(786.221f, 742.078f, 122.69f), Vector3(808.362f, 746.094f, 111.881f))))
      ZipLinePaths(new ZipLinePath(135, false, List(Vector3(749.635f, 682.739f, 137.377f), Vector3(771.007f, 693.48f, 136.152f), Vector3(792.38f, 704.22f, 125.454f), Vector3(815.847f, 716.014f, 111.879f))))
      ZipLinePaths(new ZipLinePath(136, false, List(Vector3(770.807f, 810.481f, 131.821f), Vector3(721.399f, 803.395f, 135.775f), Vector3(679.896f, 797.442f, 137.451f))))
      ZipLinePaths(new ZipLinePath(137, false, List(Vector3(668.436f, 839.169f, 126.948f), Vector3(668.162f, 835.637f, 137.7f), Vector3(668.04f, 830.457f, 137.345f))))
      ZipLinePaths(new ZipLinePath(138, false, List(Vector3(696.207f, 769.157f, 126.745f), Vector3(691.358f, 769.345f, 137.5f), Vector3(687.522f, 769.379f, 137.353f))))
      ZipLinePaths(new ZipLinePath(139, false, List(Vector3(777.383f, 693.723f, 109.054f), Vector3(768.922f, 695.463f, 127.2f), Vector3(764.501f, 695.909f, 126.752f))))
      ZipLinePaths(new ZipLinePath(140, false, List(Vector3(714.7f, 806.943f, 118.655f), Vector3(716.023f, 798.557f, 127.8f), Vector3(716.384f, 794.616f, 126.942f))))
      ZipLinePaths(new ZipLinePath(141, false, List(Vector3(837.198f, 679.723f, 109.056f), Vector3(841.194f, 674.804f, 121.161f), Vector3(842.792f, 671.297f, 120.844f))))
      ZipLinePaths(new ZipLinePath(142, false, List(Vector3(980.767f, 823.824f, 124.941f), Vector3(974.252f, 832.646f, 138.475f), Vector3(972.08f, 835.587f, 137.654f))))
      ZipLinePaths(new ZipLinePath(143, false, List(Vector3(854.641f, 623.48f, 147.703f), Vector3(873.038f, 670.732f, 143.255f), Vector3(890.352f, 715.204f, 137.432f))))
      ZipLinePaths(new ZipLinePath(144, false, List(Vector3(897.213f, 726.019f, 137.43f), Vector3(923f, 770.018f, 138.179f), Vector3(948.786f, 814.017f, 138.324f), Vector3(963.955f, 839.899f, 137.65f))))
      ZipLinePaths(new ZipLinePath(145, false, List(Vector3(870.548f, 724.109f, 120.954f), Vector3(877.373f, 728.026f, 137.485f), Vector3(881.138f, 728.81f, 137.433f))))
      ZipLinePaths(new ZipLinePath(146, false, List(Vector3(909.652f, 723.91f, 121.052f), Vector3(906.728f, 720.913f, 137.485f), Vector3(903.654f, 717.805f, 137.441f))))
      ZipLinePaths(new ZipLinePath(147, false, List(Vector3(944.951f, 580.171f, 113.352f), Vector3(939.778f, 573.055f, 127.401f), Vector3(937.12f, 569.75f, 126.95f))))
      ZipLinePaths(new ZipLinePath(148, false, List(Vector3(931.01f, 546.086f, 126.943f), Vector3(933.092f, 538.266f, 137.799f), Vector3(933.786f, 535.659f, 137.95f))))
      ZipLinePaths(new ZipLinePath(149, false, List(Vector3(1009.159f, 752.719f, 124.85f), Vector3(991.619f, 799.006f, 125.778f), Vector3(985.772f, 814.968f, 124.95f))))
      ZipLinePaths(new ZipLinePath(150, false, List(Vector3(628.63f, 911.352f, 126.753f), Vector3(622.808f, 922.572f, 129.078f), Vector3(605.34f, 956.232f, 125.252f), Vector3(591.184f, 983.513f, 118.093f))))
      ZipLinePaths(new ZipLinePath(151, false, List(Vector3(916.791f, 916.103f, 110.541f), Vector3(927.088f, 910.021f, 125.3f), Vector3(931.332f, 907.814f, 124.85f))))
      ZipLinePaths(new ZipLinePath(152, false, List(Vector3(985.233f, 890.658f, 114.677f), Vector3(983.342f, 879.305f, 125.383f), Vector3(982.712f, 875.521f, 124.952f))))
      ZipLinePaths(new ZipLinePath(153, false, List(Vector3(788.586f, 904.202f, 133.771f), Vector3(800.969f, 904.082f, 135.354f), Vector3(838.117f, 903.72f, 125.166f), Vector3(850.742f, 903.597f, 115.87f))))
      ZipLinePaths(new ZipLinePath(154, false, List(Vector3(723.819f, 909.915f, 133.841f), Vector3(716.155f, 968.988f, 175.181f), Vector3(715.644f, 972.926f, 175.708f))))
      ZipLinePaths(new ZipLinePath(155, false, List(Vector3(757.2f, 918.818f, 133.758f), Vector3(752.902f, 967.566f, 125.833f), Vector3(751.772f, 994.343f, 120.207f))))
      ZipLinePaths(new ZipLinePath(156, false, List(Vector3(736.038f, 1019.345f, 113.91f), Vector3(724.248f, 1060.438f, 140.6f), Vector3(723.526f, 1062.954f, 140.141f))))
      ZipLinePaths(new ZipLinePath(157, false, List(Vector3(753.396f, 1079.35f, 140.243f), Vector3(728.852f, 1090.832f, 164.844f), Vector3(725.346f, 1092.473f, 165.261f))))
      ZipLinePaths(new ZipLinePath(158, false, List(Vector3(881.321f, 940.867f, 115.863f), Vector3(870.806f, 989.707f, 129.7f), Vector3(860.291f, 1038.547f, 140.352f), Vector3(859.054f, 1044.292f, 140.464f))))
      ZipLinePaths(new ZipLinePath(159, false, List(Vector3(880.79f, 1033.59f, 109.7f), Vector3(881.104f, 1056.676f, 136.541f), Vector3(881.144f, 1059.561f, 136.088f))))
      ZipLinePaths(new ZipLinePath(160, false, List(Vector3(923.653f, 1069.12f, 109.871f), Vector3(920.338f, 1084.106f, 136.341f), Vector3(919.41f, 1090.862f, 135.986f))))
      ZipLinePaths(new ZipLinePath(161, false, List(Vector3(976.603f, 1041.599f, 177.302f), Vector3(973.324f, 1045.539f, 176.772f), Vector3(924.127f, 1104.639f, 136.049f))))
      ZipLinePaths(new ZipLinePath(162, false, List(Vector3(989.279f, 722.262f, 124.851f), Vector3(942.352f, 727.252f, 122.74f), Vector3(924.486f, 729.048f, 120.951f))))
      ZipLinePaths(new ZipLinePath(163, false, List(Vector3(870.525f, 934.977f, 115.886f), Vector3(748.961f, 1004.118f, 176.348f), Vector3(740.046f, 1009.188f, 175.719f))))
      ZipLinePaths(new ZipLinePath(164, false, List(Vector3(947.755f, 531.199f, 138.181f), Vector3(964.835f, 579.115f, 135.685f), Vector3(981.915f, 627.03f, 132.071f), Vector3(998.995f, 674.945f, 128.457f), Vector3(1009.377f, 704.07f, 124.851f))))
      ZipLinePaths(new ZipLinePath(165, false, List(Vector3(1025.061f, 697.578f, 124.85f), Vector3(1053.263f, 655.104f, 126.921f), Vector3(1081.465f, 612.63f, 128.15f), Vector3(1109.667f, 570.156f, 129.38f), Vector3(1119.068f, 555.998f, 129.97f))))
      ZipLinePaths(new ZipLinePath(166, false, List(Vector3(895.122f, 840.51f, 119.871f), Vector3(873.021f, 797.24f, 135.021f), Vector3(872.137f, 795.509f, 134.621f))))
      ZipLinePaths(new ZipLinePath(167, false, List(Vector3(874.806f, 787.157f, 133.531f), Vector3(887.407f, 738.836f, 137.875f), Vector3(888.415f, 734.97f, 137.431f))))
      ZipLinePaths(new ZipLinePath(168, false, List(Vector3(824.106f, 711.113f, 111.875f), Vector3(849.633f, 715.773f, 120.367f))))
      ZipLinePaths(new ZipLinePath(169, false, List(Vector3(844.487f, 761.271f, 111.351f), Vector3(853.672f, 773.247f, 130.373f), Vector3(862.857f, 785.223f, 135.795f), Vector3(864.898f, 787.885f, 135.215f))))
      ZipLinePaths(new ZipLinePath(170, false, List(Vector3(907.484f, 845.903f, 119.871f), Vector3(910.119f, 849.755f, 126.493f), Vector3(912.755f, 853.606f, 124.88f))))
      ZipLinePaths(new ZipLinePath(171, false, List(Vector3(1112.038f, 682.725f, 123.151f), Vector3(1120.959f, 632.652f, 129.102f), Vector3(1122.358f, 624.798f, 128.352f))))
      ZipLinePaths(new ZipLinePath(172, false, List(Vector3(1122.941f, 604.904f, 128.95f), Vector3(1120.249f, 561.988f, 129.97f))))
      ZipLinePaths(new ZipLinePath(173, false, List(Vector3(1171.009f, 531.472f, 109.666f), Vector3(1168.828f, 536.526f, 118.944f), Vector3(1167.582f, 539.414f, 118.285f))))
      ZipLinePaths(new ZipLinePath(174, false, List(Vector3(1047.224f, 748.42f, 124.85f), Vector3(1043.657f, 799.293f, 125.325f), Vector3(1043.377f, 803.283f, 124.85f))))
      ZipLinePaths(new ZipLinePath(175, false, List(Vector3(1059.966f, 892.132f, 124.857f), Vector3(1072.008f, 890.537f, 142.8f), Vector3(1075.157f, 890.218f, 141.854f))))
      ZipLinePaths(new ZipLinePath(176, false, List(Vector3(853.667f, 663.625f, 120.841f), Vector3(852.606f, 651.134f, 137.7f), Vector3(852.418f, 648.93f, 137.35f))))
      ZipLinePaths(new ZipLinePath(177, false, List(Vector3(1265.763f, 620.851f, 119.889f), Vector3(1247.709f, 666.201f, 129.597f), Vector3(1244.523f, 674.521f, 130.779f))))
      ZipLinePaths(new ZipLinePath(178, false, List(Vector3(1170.515f, 541.815f, 118.29f), Vector3(1218.82f, 551.299f, 125.086f), Vector3(1267.125f, 560.783f, 119.88f))))
      ZipLinePaths(new ZipLinePath(179, false, List(Vector3(1251.983f, 620.364f, 109.047f), Vector3(1260.922f, 617.29f, 120.227f), Vector3(1264.173f, 616.172f, 119.876f))))
      ZipLinePaths(new ZipLinePath(180, false, List(Vector3(1253.004f, 779.277f, 109.057f), Vector3(1250.953f, 784.935f, 123f), Vector3(1249.389f, 788.084f, 122.843f))))
      ZipLinePaths(new ZipLinePath(181, false, List(Vector3(1184.063f, 747.02f, 130.77f), Vector3(1128.565f, 820.543f, 132.85f))))
      ZipLinePaths(new ZipLinePath(182, false, List(Vector3(1185.879f, 718.32f, 130.772f), Vector3(1136.233f, 719.36f, 126.816f), Vector3(1121.339f, 719.673f, 124.292f))))
      ZipLinePaths(new ZipLinePath(183, false, List(Vector3(1140.607f, 823.278f, 121.948f), Vector3(1137.174f, 825.352f, 132.8f), Vector3(1135.352f, 827.061f, 132.852f))))
      ZipLinePaths(new ZipLinePath(184, false, List(Vector3(1050.043f, 744.286f, 124.851f), Vector3(1075.381f, 788.401f, 135.697f), Vector3(1100.719f, 832.515f, 133.264f), Vector3(1101.713f, 834.245f, 132.862f))))
      ZipLinePaths(new ZipLinePath(185, false, List(Vector3(1257.734f, 828.176f, 126.819f), Vector3(1255.031f, 833.706f, 133.337f), Vector3(1254.13f, 835.549f, 132.711f))))
      ZipLinePaths(new ZipLinePath(186, false, List(Vector3(1239.454f, 850.121f, 126.803f), Vector3(1243.189f, 852.848f, 132.837f), Vector3(1244.433f, 853.756f, 132.709f))))
      ZipLinePaths(new ZipLinePath(187, false, List(Vector3(1196.063f, 823.001f, 126.812f), Vector3(1157.416f, 854.391f, 132.165f), Vector3(1143.503f, 865.692f, 132.852f))))
      ZipLinePaths(new ZipLinePath(188, false, List(Vector3(1279.621f, 932.893f, 114.524f), Vector3(1279.227f, 909.182f, 132.704f), Vector3(1279.031f, 897.327f, 134.359f), Vector3(1278.834f, 885.471f, 132.745f))))
      ZipLinePaths(new ZipLinePath(189, false, List(Vector3(1124.713f, 916.18f, 126.843f), Vector3(1115.263f, 905.232f, 142.214f), Vector3(1111.588f, 900.975f, 141.854f))))
      ZipLinePaths(new ZipLinePath(190, false, List(Vector3(1340.865f, 846.029f, 111.852f), Vector3(1335.426f, 835.885f, 127.9f), Vector3(1334.807f, 834.266f, 127.841f))))
      ZipLinePaths(new ZipLinePath(191, false, List(Vector3(1326.86f, 785.22f, 127.855f), Vector3(1262.414f, 741.409f, 142.717f), Vector3(1253.207f, 735.15f, 140.846f))))
      ZipLinePaths(new ZipLinePath(192, false, List(Vector3(1389.355f, 995.955f, 138.183f), Vector3(1362.137f, 1038.774f, 134.124f), Vector3(1334.919f, 1081.594f, 128.97f), Vector3(1310.903f, 1119.376f, 121.854f))))
      ZipLinePaths(new ZipLinePath(193, false, List(Vector3(1327.53f, 1147.16f, 109.05f), Vector3(1318.545f, 1143.592f, 122f), Vector3(1316.298f, 1142.7f, 121.847f))))
      ZipLinePaths(new ZipLinePath(194, false, List(Vector3(1396.002f, 1228.28f, 109.05f), Vector3(1395.947f, 1220.255f, 124.75f), Vector3(1396.165f, 1217.902f, 125.303f))))
      ZipLinePaths(new ZipLinePath(195, false, List(Vector3(1396.44f, 1268.505f, 109.042f), Vector3(1393.148f, 1276.026f, 126.361f), Vector3(1389.556f, 1284.23f, 134.333f), Vector3(1385.964f, 1292.435f, 136.304f))))
      ZipLinePaths(new ZipLinePath(196, false, List(Vector3(1338.752f, 1192.75f, 109.052f), Vector3(1331.086f, 1199.688f, 126.4f), Vector3(1329.17f, 1201.422f, 126.347f))))
      ZipLinePaths(new ZipLinePath(197, false, List(Vector3(1405.842f, 1227.371f, 125.3f), Vector3(1356.422f, 1219.821f, 126.45f), Vector3(1329.735f, 1215.744f, 126.45f))))
      ZipLinePaths(new ZipLinePath(198, false, List(Vector3(1301.27f, 1208.518f, 135.532f), Vector3(1292.542f, 1214.94f, 152.2f), Vector3(1290.359f, 1216.546f, 151.952f))))
      ZipLinePaths(new ZipLinePath(199, false, List(Vector3(1306.218f, 1234.485f, 134.651f), Vector3(1303.755f, 1236.99f, 141.014f), Vector3(1302.278f, 1238.493f, 140.954f))))
      ZipLinePaths(new ZipLinePath(200, false, List(Vector3(1283.492f, 1260.547f, 140.965f), Vector3(1277.346f, 1264.371f, 151.8f), Vector3(1274.929f, 1265.421f, 151.848f))))
      ZipLinePaths(new ZipLinePath(201, false, List(Vector3(1349.313f, 1281.663f, 126.542f), Vector3(1355.484f, 1290.119f, 136.966f), Vector3(1357.541f, 1292.938f, 136.311f))))
      ZipLinePaths(new ZipLinePath(202, false, List(Vector3(1341.799f, 1295.97f, 145.061f), Vector3(1351.709f, 1288.523f, 145.53f), Vector3(1381.437f, 1266.181f, 137.67f), Vector3(1411.749f, 1243.401f, 126.441f))))
      ZipLinePaths(new ZipLinePath(203, false, List(Vector3(1375.896f, 1157.863f, 125.181f), Vector3(1188.609f, 1075.48f, 199.971f), Vector3(1182.567f, 1072.822f, 199.731f))))
      ZipLinePaths(new ZipLinePath(204, false, List(Vector3(1090.567f, 1089.597f, 126.743f), Vector3(1021.866f, 1037.4f, 182.072f), Vector3(1017.744f, 1034.268f, 182.343f))))
      ZipLinePaths(new ZipLinePath(205, false, List(Vector3(1129.258f, 1111.212f, 126.753f), Vector3(1132.878f, 1111.43f, 136.65f), Vector3(1136.499f, 1111.648f, 135.552f))))
      ZipLinePaths(new ZipLinePath(206, false, List(Vector3(1149.892f, 1116.521f, 135.546f), Vector3(1156.366f, 1117.722f, 148.401f), Vector3(1159.485f, 1118.147f, 148.442f))))
      ZipLinePaths(new ZipLinePath(207, false, List(Vector3(1212.807f, 1063.577f, 121.952f), Vector3(1207.698f, 1073.111f, 136.4f), Vector3(1205.341f, 1077.512f, 135.447f))))
      ZipLinePaths(new ZipLinePath(208, false, List(Vector3(1264.462f, 877.804f, 132.71f), Vector3(1246.819f, 925.65f, 134.208f), Vector3(1229.176f, 973.497f, 134.824f), Vector3(1211.532f, 1021.344f, 135.44f), Vector3(1193.889f, 1069.188f, 136.057f), Vector3(1190.083f, 1079.507f, 135.45f))))
      ZipLinePaths(new ZipLinePath(209, false, List(Vector3(1217.471f, 1083.948f, 135.453f), Vector3(1210.336f, 1099.103f, 152.484f), Vector3(1209.063f, 1102.378f, 152.225f))))
      ZipLinePaths(new ZipLinePath(210, false, List(Vector3(1240.544f, 1094.16f, 121.956f), Vector3(1232.679f, 1097.464f, 135.575f), Vector3(1230.057f, 1098.565f, 135.442f))))
      ZipLinePaths(new ZipLinePath(211, false, List(Vector3(1186.014f, 1121.699f, 152.224f), Vector3(1164.017f, 1098.726f, 199.092f), Vector3(1160.498f, 1095.05f, 199.687f))))
      ZipLinePaths(new ZipLinePath(212, false, List(Vector3(1204.12f, 1417.056f, 109.12f), Vector3(1213.4f, 1417.169f, 127.718f), Vector3(1216.493f, 1417.207f, 128.044f))))
      ZipLinePaths(new ZipLinePath(213, false, List(Vector3(1229.154f, 1404.634f, 128.041f), Vector3(1238.575f, 1406.62f, 145.262f), Vector3(1244.193f, 1407.981f, 145.044f))))
      ZipLinePaths(new ZipLinePath(214, false, List(Vector3(1161.559f, 1423.931f, 109.309f), Vector3(1156.185f, 1421.327f, 124.378f), Vector3(1151.826f, 1419.556f, 124.321f))))
      ZipLinePaths(new ZipLinePath(215, false, List(Vector3(1098.527f, 1217.84f, 119.349f), Vector3(1096.811f, 1211.274f, 126.3f), Vector3(1096.106f, 1207.218f, 126.545f))))
      ZipLinePaths(new ZipLinePath(216, false, List(Vector3(1096.231f, 1182.333f, 126.55f), Vector3(1103.04f, 1179.974f, 138.388f), Vector3(1109.849f, 1177.616f, 145.726f), Vector3(1117.458f, 1174.98f, 148.338f), Vector3(1125.068f, 1172.344f, 148.455f))))
      ZipLinePaths(new ZipLinePath(217, false, List(Vector3(929.798f, 1116.506f, 135.992f), Vector3(1010.892f, 1103.806f, 205.572f), Vector3(1023.303f, 1101.565f, 208.656f))))
      ZipLinePaths(new ZipLinePath(218, false, List(Vector3(1028.069f, 1258.8f, 107.069f), Vector3(1021.764f, 1250.92f, 122.861f), Vector3(1017.858f, 1243.717f, 122.444f))))
      ZipLinePaths(new ZipLinePath(219, false, List(Vector3(1104.627f, 1364.967f, 109.072f), Vector3(1094.893f, 1362.829f, 115.616f), Vector3(1085.159f, 1360.69f, 114.962f))))
      ZipLinePaths(new ZipLinePath(220, false, List(Vector3(1108.235f, 1301.184f, 119.665f), Vector3(931.115f, 1285.88f, 180.638f), Vector3(925.402f, 1285.386f, 180.536f))))
      ZipLinePaths(new ZipLinePath(221, false, List(Vector3(1110.124f, 1304.157f, 119.641f), Vector3(1070.944f, 1318.948f, 122.751f))))
      ZipLinePaths(new ZipLinePath(222, false, List(Vector3(1183.721f, 1312.937f, 119.247f), Vector3(1193.741f, 1311.441f, 136.45f), Vector3(1197.081f, 1310.942f, 136.048f))))
      ZipLinePaths(new ZipLinePath(223, false, List(Vector3(1164.092f, 1318.913f, 109.053f), Vector3(1166.906f, 1313.922f, 119.15f), Vector3(1169.72f, 1308.93f, 119.25f))))
      ZipLinePaths(new ZipLinePath(224, false, List(Vector3(1146.193f, 1413.844f, 124.331f), Vector3(1146.462f, 1363.929f, 123.112f), Vector3(1146.73f, 1314.015f, 120.12f), Vector3(1146.747f, 1311.02f, 119.64f))))
      ZipLinePaths(new ZipLinePath(225, false, List(Vector3(1223.321f, 1378.103f, 145.156f), Vector3(1174.206f, 1372.073f, 138.402f), Vector3(1125.091f, 1366.042f, 131.231f), Vector3(1080.888f, 1360.615f, 123.895f))))
      ZipLinePaths(new ZipLinePath(226, false, List(Vector3(1096.022f, 1374.066f, 124.331f), Vector3(1074.29f, 1362.048f, 122.751f))))
      ZipLinePaths(new ZipLinePath(227, false, List(Vector3(1149.499f, 1415.419f, 124.331f), Vector3(1197.765f, 1402.559f, 127.332f), Vector3(1217.072f, 1397.415f, 128.051f))))
      ZipLinePaths(new ZipLinePath(228, false, List(Vector3(814.131f, 1461.802f, 112.384f), Vector3(824.706f, 1416.337f, 138.113f), Vector3(835.28f, 1370.872f, 159.039f), Vector3(842.259f, 1347.165f, 163.573f))))
      ZipLinePaths(new ZipLinePath(229, false, List(Vector3(876.429f, 1343.77f, 136.094f), Vector3(926.629f, 1348.837f, 131.986f), Vector3(976.829f, 1353.905f, 124.559f), Vector3(1023.092f, 1358.575f, 116.544f))))
      ZipLinePaths(new ZipLinePath(230, false, List(Vector3(807.727f, 1380.755f, 109.058f), Vector3(806.274f, 1370.518f, 122.642f), Vector3(804.82f, 1358.281f, 130.736f), Vector3(804.404f, 1354.785f, 131.099f))))
      ZipLinePaths(new ZipLinePath(231, false, List(Vector3(706.406f, 1320.177f, 137.21f), Vector3(738.095f, 1316.223f, 136.05f))))
      ZipLinePaths(new ZipLinePath(232, false, List(Vector3(722.69f, 1345.392f, 109.052f), Vector3(731.902f, 1339.244f, 130f), Vector3(741.728f, 1332.685f, 137.239f), Vector3(744.799f, 1330.635f, 136.056f))))
      ZipLinePaths(new ZipLinePath(233, false, List(Vector3(907.818f, 1320.599f, 109.047f), Vector3(895.035f, 1324.75f, 125.795f), Vector3(882.252f, 1328.9f, 136.141f), Vector3(880.06f, 1329.612f, 136.089f))))
      ZipLinePaths(new ZipLinePath(234, false, List(Vector3(1014.461f, 1221.339f, 122.453f), Vector3(977.636f, 1187.782f, 130.037f), Vector3(940.81f, 1154.225f, 134.301f), Vector3(920.924f, 1136.104f, 135.992f))))
      ZipLinePaths(new ZipLinePath(235, false, List(Vector3(547.329f, 1083.474f, 110.82f), Vector3(622.869f, 1031.432f, 153.65f), Vector3(668.493f, 997.886f, 175.414f), Vector3(675.972f, 992.387f, 175.762f))))
      ZipLinePaths(new ZipLinePath(236, false, List(Vector3(615.829f, 1087.478f, 109.051f), Vector3(622.879f, 1087.122f, 123.044f), Vector3(635.568f, 1086.48f, 136.1f), Vector3(640.503f, 1086.231f, 136.144f))))
      ZipLinePaths(new ZipLinePath(237, false, List(Vector3(620.27f, 1254.43f, 119.008f), Vector3(641.552f, 1210.009f, 128.818f), Vector3(660.281f, 1170.918f, 136.059f))))
      ZipLinePaths(new ZipLinePath(238, false, List(Vector3(635.795f, 1193.298f, 109.043f), Vector3(639.224f, 1187.391f, 122.944f), Vector3(648.75f, 1170.982f, 136.4f), Vector3(650.274f, 1168.356f, 136.04f))))
      ZipLinePaths(new ZipLinePath(239, false, List(Vector3(926.527f, 1170.817f, 109.363f), Vector3(923.763f, 1165.504f, 125.927f), Vector3(918.072f, 1154.567f, 138.117f), Vector3(915.145f, 1148.942f, 137.957f))))
      ZipLinePaths(new ZipLinePath(240, false, List(Vector3(789.839f, 1155.974f, 120.007f), Vector3(794.331f, 1108.861f, 139.687f), Vector3(795.499f, 1096.612f, 140.378f))))
      ZipLinePaths(new ZipLinePath(241, false, List(Vector3(803.454f, 1182.012f, 119.972f), Vector3(830.713f, 1190.844f, 140.6f), Vector3(833.92f, 1191.883f, 140.248f))))
      ZipLinePaths(new ZipLinePath(242, false, List(Vector3(828.546f, 1027.546f, 109.549f), Vector3(837.077f, 1041.018f, 140.638f), Vector3(839.779f, 1044.485f, 140.443f))))
      ZipLinePaths(new ZipLinePath(243, false, List(Vector3(658.973f, 1029.373f, 109.083f), Vector3(664.074f, 1050.972f, 136.7f), Vector3(665.095f, 1055.292f, 136.044f))))
      ZipLinePaths(new ZipLinePath(244, false, List(Vector3(777.972f, 893.413f, 133.751f), Vector3(787.343f, 858.747f, 131.85f))))
      ZipLinePaths(new ZipLinePath(245, false, List(Vector3(685.721f, 1051.781f, 140.05f), Vector3(670.758f, 1004.096f, 141.016f), Vector3(655.795f, 956.412f, 139.45f), Vector3(640.832f, 908.728f, 137.884f), Vector3(632.154f, 881.072f, 136.36f))))
      ZipLinePaths(new ZipLinePath(246, false, List(Vector3(671.383f, 1075.859f, 140.045f), Vector3(701.364f, 1029.762f, 175.213f), Vector3(703.394f, 1026.64f, 175.696f))))
      ZipLinePaths(new ZipLinePath(247, false, List(Vector3(889.471f, 1158.689f, 179.31f), Vector3(890.333f, 1209.645f, 181.438f), Vector3(891.178f, 1259.603f, 183.33f))))
      ZipLinePaths(new ZipLinePath(248, false, List(Vector3(740.683f, 1013.973f, 175.689f), Vector3(779.565f, 1045.775f, 185.238f), Vector3(818.447f, 1077.575f, 194.64f), Vector3(819.972f, 1078.822f, 194.579f))))
      ZipLinePaths(new ZipLinePath(249, false, List(Vector3(833.687f, 1301.586f, 160.773f), Vector3(860.142f, 1284.02f, 180.145f), Vector3(863.922f, 1281.511f, 180.531f))))
      ZipLinePaths(new ZipLinePath(250, false, List(Vector3(1010.241f, 1240.344f, 122.449f), Vector3(930.779f, 1274.878f, 180.319f), Vector3(922.833f, 1278.331f, 180.514f))))
      ZipLinePaths(new ZipLinePath(251, false, List(Vector3(1133.814f, 1048.795f, 199.728f), Vector3(1086.372f, 1035.111f, 191.631f), Vector3(1038.929f, 1021.427f, 183.736f), Vector3(1032.287f, 1019.512f, 182.166f))))
      ZipLinePaths(new ZipLinePath(252, false, List(Vector3(889.938f, 1124.978f, 179.34f), Vector3(966.938f, 1027.831f, 182.22f))))
      ZipLinePaths(new ZipLinePath(253, true, List(Vector3(933.672f, 524.813f, 137.773f), Vector3(885.162f, 531.286f, 172.306f), Vector3(835.681f, 537.888f, 161.853f), Vector3(797.842f, 542.937f, 108.985f))))
      ZipLinePaths(new ZipLinePath(254, true, List(Vector3(541.168f, 1083.547f, 110.794f), Vector3(552.16f, 1043.303f, 211.598f), Vector3(568.619f, 981.507f, 137.554f))))
      ZipLinePaths(new ZipLinePath(255, true, List(Vector3(572.976f, 987.122f, 137.826f), Vector3(560.277f, 1045.507f, 211.626f), Vector3(551.469f, 1085.998f, 109.947f))))
      ZipLinePaths(new ZipLinePath(256, true, List(Vector3(817.329f, 1476.263f, 112.635f), Vector3(822.261f, 1473.141f, 116.242f), Vector3(823.905f, 1472.1f, 116.783f), Vector3(844.456f, 1459.09f, 211.505f), Vector3(885.935f, 1433.592f, 132.763f))))
      ZipLinePaths(new ZipLinePath(257, true, List(Vector3(880.19f, 1426.678f, 132.713f), Vector3(822.094f, 1465.025f, 211.507f), Vector3(819.173f, 1467.223f, 112.528f))))
    }

    ZipLines()

  }
}
