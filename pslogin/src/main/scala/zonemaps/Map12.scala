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

object Map12 {
  // HOME2 (TERRAN REPUBLIC SANCTUARY)
  val ZoneMap = new ZoneMap("map12") {
    Checksum = 962888126L

    Building4()

    def Building4(): Unit = { // Name: Hart_Ishundar Type: orbital_building_tr GUID: 1, MapID: 4
      LocalBuilding("Hart_Ishundar", 1, 4, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2922f, 5230f, 35.99899f), orbital_building_tr)))
      LocalObject(382, Door.Constructor(Vector3(2842f, 5217.99f, 40.10499f)), owning_building_guid = 1)
      LocalObject(383, Door.Constructor(Vector3(2842f, 5241.99f, 40.10499f)), owning_building_guid = 1)
      LocalObject(390, Door.Constructor(Vector3(3002f, 5218.01f, 40.10499f)), owning_building_guid = 1)
      LocalObject(391, Door.Constructor(Vector3(3002f, 5242.01f, 40.10499f)), owning_building_guid = 1)
      LocalObject(410, Door.Constructor(Vector3(2860.34f, 5230f, 40.10499f)), owning_building_guid = 1)
      LocalObject(411, Door.Constructor(Vector3(2860.34f, 5230f, 50.10499f)), owning_building_guid = 1)
      LocalObject(412, Door.Constructor(Vector3(2906f, 5277.99f, 55.105f)), owning_building_guid = 1)
      LocalObject(413, Door.Constructor(Vector3(2938f, 5277.99f, 55.105f)), owning_building_guid = 1)
      LocalObject(416, Door.Constructor(Vector3(2983.64f, 5230f, 40.10499f)), owning_building_guid = 1)
      LocalObject(417, Door.Constructor(Vector3(2983.64f, 5230f, 50.10499f)), owning_building_guid = 1)
      LocalObject(482, Door.Constructor(Vector3(2866f, 5250.01f, 50.10499f)), owning_building_guid = 1)
      LocalObject(484, Door.Constructor(Vector3(2978f, 5249.99f, 50.10499f)), owning_building_guid = 1)
      LocalObject(488, Door.Constructor(Vector3(2866f, 5218.015f, 40.10499f)), owning_building_guid = 1)
      LocalObject(489, Door.Constructor(Vector3(2866f, 5218.015f, 50.10499f)), owning_building_guid = 1)
      LocalObject(490, Door.Constructor(Vector3(2866f, 5242.015f, 40.10499f)), owning_building_guid = 1)
      LocalObject(491, Door.Constructor(Vector3(2866f, 5242.015f, 50.10499f)), owning_building_guid = 1)
      LocalObject(496, Door.Constructor(Vector3(2978f, 5217.985f, 40.10499f)), owning_building_guid = 1)
      LocalObject(497, Door.Constructor(Vector3(2978f, 5217.985f, 50.10499f)), owning_building_guid = 1)
      LocalObject(498, Door.Constructor(Vector3(2978f, 5241.985f, 40.10499f)), owning_building_guid = 1)
      LocalObject(499, Door.Constructor(Vector3(2978f, 5241.985f, 50.10499f)), owning_building_guid = 1)
      LocalObject(700, Locker.Constructor(Vector3(2917.402f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(701, Locker.Constructor(Vector3(2918.732f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(702, Locker.Constructor(Vector3(2920.049f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(703, Locker.Constructor(Vector3(2921.325f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(704, Locker.Constructor(Vector3(2922.614f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(705, Locker.Constructor(Vector3(2923.934f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(706, Locker.Constructor(Vector3(2925.276f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(707, Locker.Constructor(Vector3(2926.64f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(186, Terminal.Constructor(Vector3(2843.212f, 5222.806f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(187, Terminal.Constructor(Vector3(2843.212f, 5224.713f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(188, Terminal.Constructor(Vector3(2843.212f, 5226.644f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(189, Terminal.Constructor(Vector3(2843.296f, 5233.669f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(190, Terminal.Constructor(Vector3(2843.296f, 5235.576f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(191, Terminal.Constructor(Vector3(2843.296f, 5237.507f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(192, Terminal.Constructor(Vector3(2854.556f, 5214.06f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(193, Terminal.Constructor(Vector3(2854.556f, 5217.963f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(194, Terminal.Constructor(Vector3(2854.556f, 5221.801f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(195, Terminal.Constructor(Vector3(2854.628f, 5238.44f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(196, Terminal.Constructor(Vector3(2854.628f, 5242.319f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(197, Terminal.Constructor(Vector3(2854.628f, 5246.191f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(210, Terminal.Constructor(Vector3(2989.371f, 5238.203f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(211, Terminal.Constructor(Vector3(2989.371f, 5242.041f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(212, Terminal.Constructor(Vector3(2989.371f, 5245.944f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(213, Terminal.Constructor(Vector3(2989.383f, 5213.869f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(214, Terminal.Constructor(Vector3(2989.383f, 5217.741f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(215, Terminal.Constructor(Vector3(2989.383f, 5221.619f, 48.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(216, Terminal.Constructor(Vector3(3000.715f, 5222.552f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(217, Terminal.Constructor(Vector3(3000.715f, 5224.483f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(218, Terminal.Constructor(Vector3(3000.715f, 5226.391f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(219, Terminal.Constructor(Vector3(3000.715f, 5233.36f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(220, Terminal.Constructor(Vector3(3000.715f, 5235.291f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(221, Terminal.Constructor(Vector3(3000.715f, 5237.198f, 38.49899f), cert_terminal), owning_building_guid = 1)
      LocalObject(829, Terminal.Constructor(Vector3(2846.069f, 5228.397f, 39.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(830, Terminal.Constructor(Vector3(2846.09f, 5231.595f, 39.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(831, Terminal.Constructor(Vector3(2849.959f, 5228.397f, 39.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(832, Terminal.Constructor(Vector3(2850.104f, 5231.595f, 39.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(836, Terminal.Constructor(Vector3(2864.478f, 5233.834f, 49.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(837, Terminal.Constructor(Vector3(2864.478f, 5236.735f, 49.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(838, Terminal.Constructor(Vector3(2864.485f, 5223.262f, 49.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(839, Terminal.Constructor(Vector3(2864.485f, 5226.183f, 49.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(840, Terminal.Constructor(Vector3(2911.52f, 5279.783f, 54.79199f), order_terminal), owning_building_guid = 1)
      LocalObject(841, Terminal.Constructor(Vector3(2914f, 5279.783f, 54.79199f), order_terminal), owning_building_guid = 1)
      LocalObject(848, Terminal.Constructor(Vector3(2930f, 5279.783f, 54.79199f), order_terminal), owning_building_guid = 1)
      LocalObject(851, Terminal.Constructor(Vector3(2932.39f, 5279.783f, 54.79199f), order_terminal), owning_building_guid = 1)
      LocalObject(858, Terminal.Constructor(Vector3(2979.508f, 5223.236f, 49.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(859, Terminal.Constructor(Vector3(2979.508f, 5226.137f, 49.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(860, Terminal.Constructor(Vector3(2979.505f, 5233.817f, 49.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(861, Terminal.Constructor(Vector3(2979.505f, 5236.738f, 49.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(862, Terminal.Constructor(Vector3(2993.908f, 5228.397f, 39.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(863, Terminal.Constructor(Vector3(2993.968f, 5231.592f, 39.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(865, Terminal.Constructor(Vector3(2997.858f, 5231.592f, 39.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(866, Terminal.Constructor(Vector3(2997.922f, 5228.397f, 39.79599f), order_terminal), owning_building_guid = 1)
      LocalObject(796, ProximityTerminal.Constructor(Vector3(2851.311f, 5207.145f, 38.48999f), medical_terminal), owning_building_guid = 1)
      LocalObject(797, ProximityTerminal.Constructor(Vector3(2851.333f, 5252.849f, 38.48999f), medical_terminal), owning_building_guid = 1)
      LocalObject(800, ProximityTerminal.Constructor(Vector3(2992.631f, 5252.849f, 38.48999f), medical_terminal), owning_building_guid = 1)
      LocalObject(801, ProximityTerminal.Constructor(Vector3(2992.693f, 5207.145f, 38.48999f), medical_terminal), owning_building_guid = 1)
      LocalObject(536, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(512, Terminal.Constructor(Vector3(2857.677f, 5251.711f, 38.49899f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(536, 512)
      LocalObject(537, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(513, Terminal.Constructor(Vector3(2857.702f, 5208.303f, 38.49899f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(537, 513)
      LocalObject(538, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(514, Terminal.Constructor(Vector3(2863.005f, 5208.303f, 38.49899f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(538, 514)
      LocalObject(539, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(515, Terminal.Constructor(Vector3(2863.084f, 5251.711f, 38.49899f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(539, 515)
      LocalObject(544, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(520, Terminal.Constructor(Vector3(2980.927f, 5208.299f, 38.49899f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(544, 520)
      LocalObject(545, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(521, Terminal.Constructor(Vector3(2980.922f, 5251.701f, 38.49899f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(545, 521)
      LocalObject(546, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(522, Terminal.Constructor(Vector3(2986.225f, 5251.701f, 38.49899f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(546, 522)
      LocalObject(547, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(523, Terminal.Constructor(Vector3(2986.335f, 5208.299f, 38.49899f), implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(547, 523)
    }

    Building40()

    def Building40(): Unit = { // Name: Hart_Cyssor Type: orbital_building_tr GUID: 2, MapID: 40
      LocalBuilding("Hart_Cyssor", 2, 40, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3006f, 2984f, 34.91934f), orbital_building_tr)))
      LocalObject(387, Door.Constructor(Vector3(2926f, 2971.99f, 39.02534f)), owning_building_guid = 2)
      LocalObject(388, Door.Constructor(Vector3(2926f, 2995.99f, 39.02534f)), owning_building_guid = 2)
      LocalObject(394, Door.Constructor(Vector3(3086f, 2972.01f, 39.02534f)), owning_building_guid = 2)
      LocalObject(395, Door.Constructor(Vector3(3086f, 2996.01f, 39.02534f)), owning_building_guid = 2)
      LocalObject(414, Door.Constructor(Vector3(2944.34f, 2984f, 39.02534f)), owning_building_guid = 2)
      LocalObject(415, Door.Constructor(Vector3(2944.34f, 2984f, 49.02534f)), owning_building_guid = 2)
      LocalObject(418, Door.Constructor(Vector3(2990f, 3031.99f, 54.02534f)), owning_building_guid = 2)
      LocalObject(419, Door.Constructor(Vector3(3022f, 3031.99f, 54.02534f)), owning_building_guid = 2)
      LocalObject(420, Door.Constructor(Vector3(3067.64f, 2984f, 39.02534f)), owning_building_guid = 2)
      LocalObject(421, Door.Constructor(Vector3(3067.64f, 2984f, 49.02534f)), owning_building_guid = 2)
      LocalObject(483, Door.Constructor(Vector3(2950f, 3004.01f, 49.02534f)), owning_building_guid = 2)
      LocalObject(485, Door.Constructor(Vector3(3062f, 3003.99f, 49.02534f)), owning_building_guid = 2)
      LocalObject(492, Door.Constructor(Vector3(2950f, 2972.015f, 39.02534f)), owning_building_guid = 2)
      LocalObject(493, Door.Constructor(Vector3(2950f, 2972.015f, 49.02534f)), owning_building_guid = 2)
      LocalObject(494, Door.Constructor(Vector3(2950f, 2996.015f, 39.02534f)), owning_building_guid = 2)
      LocalObject(495, Door.Constructor(Vector3(2950f, 2996.015f, 49.02534f)), owning_building_guid = 2)
      LocalObject(500, Door.Constructor(Vector3(3062f, 2971.985f, 39.02534f)), owning_building_guid = 2)
      LocalObject(501, Door.Constructor(Vector3(3062f, 2971.985f, 49.02534f)), owning_building_guid = 2)
      LocalObject(502, Door.Constructor(Vector3(3062f, 2995.985f, 39.02534f)), owning_building_guid = 2)
      LocalObject(503, Door.Constructor(Vector3(3062f, 2995.985f, 49.02534f)), owning_building_guid = 2)
      LocalObject(708, Locker.Constructor(Vector3(3001.402f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(709, Locker.Constructor(Vector3(3002.732f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(710, Locker.Constructor(Vector3(3004.049f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(711, Locker.Constructor(Vector3(3005.325f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(712, Locker.Constructor(Vector3(3006.614f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(713, Locker.Constructor(Vector3(3007.934f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(714, Locker.Constructor(Vector3(3009.276f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(715, Locker.Constructor(Vector3(3010.64f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(198, Terminal.Constructor(Vector3(2927.212f, 2976.806f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(199, Terminal.Constructor(Vector3(2927.212f, 2978.713f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(200, Terminal.Constructor(Vector3(2927.212f, 2980.644f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(201, Terminal.Constructor(Vector3(2927.296f, 2987.669f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(202, Terminal.Constructor(Vector3(2927.296f, 2989.576f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(203, Terminal.Constructor(Vector3(2927.296f, 2991.507f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(204, Terminal.Constructor(Vector3(2938.556f, 2968.06f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(205, Terminal.Constructor(Vector3(2938.556f, 2971.963f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(206, Terminal.Constructor(Vector3(2938.556f, 2975.801f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(207, Terminal.Constructor(Vector3(2938.628f, 2992.44f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(208, Terminal.Constructor(Vector3(2938.628f, 2996.319f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(209, Terminal.Constructor(Vector3(2938.628f, 3000.191f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(222, Terminal.Constructor(Vector3(3073.371f, 2992.203f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(223, Terminal.Constructor(Vector3(3073.371f, 2996.041f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(224, Terminal.Constructor(Vector3(3073.371f, 2999.944f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(225, Terminal.Constructor(Vector3(3073.383f, 2967.869f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(226, Terminal.Constructor(Vector3(3073.383f, 2971.741f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(227, Terminal.Constructor(Vector3(3073.383f, 2975.619f, 47.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(228, Terminal.Constructor(Vector3(3084.715f, 2976.552f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(229, Terminal.Constructor(Vector3(3084.715f, 2978.483f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(230, Terminal.Constructor(Vector3(3084.715f, 2980.391f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(231, Terminal.Constructor(Vector3(3084.715f, 2987.36f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(232, Terminal.Constructor(Vector3(3084.715f, 2989.291f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(233, Terminal.Constructor(Vector3(3084.715f, 2991.198f, 37.41934f), cert_terminal), owning_building_guid = 2)
      LocalObject(849, Terminal.Constructor(Vector3(2930.069f, 2982.397f, 38.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(850, Terminal.Constructor(Vector3(2930.09f, 2985.595f, 38.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(852, Terminal.Constructor(Vector3(2933.959f, 2982.397f, 38.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(853, Terminal.Constructor(Vector3(2934.104f, 2985.595f, 38.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(854, Terminal.Constructor(Vector3(2948.478f, 2987.834f, 48.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(855, Terminal.Constructor(Vector3(2948.478f, 2990.735f, 48.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(856, Terminal.Constructor(Vector3(2948.485f, 2977.262f, 48.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(857, Terminal.Constructor(Vector3(2948.485f, 2980.183f, 48.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(864, Terminal.Constructor(Vector3(2995.52f, 3033.783f, 53.71234f), order_terminal), owning_building_guid = 2)
      LocalObject(867, Terminal.Constructor(Vector3(2998f, 3033.783f, 53.71234f), order_terminal), owning_building_guid = 2)
      LocalObject(868, Terminal.Constructor(Vector3(3014f, 3033.783f, 53.71234f), order_terminal), owning_building_guid = 2)
      LocalObject(869, Terminal.Constructor(Vector3(3016.39f, 3033.783f, 53.71234f), order_terminal), owning_building_guid = 2)
      LocalObject(870, Terminal.Constructor(Vector3(3063.508f, 2977.236f, 48.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(871, Terminal.Constructor(Vector3(3063.508f, 2980.137f, 48.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(872, Terminal.Constructor(Vector3(3063.505f, 2987.817f, 48.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(873, Terminal.Constructor(Vector3(3063.505f, 2990.738f, 48.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(878, Terminal.Constructor(Vector3(3077.908f, 2982.397f, 38.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(879, Terminal.Constructor(Vector3(3077.968f, 2985.592f, 38.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(882, Terminal.Constructor(Vector3(3081.858f, 2985.592f, 38.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(883, Terminal.Constructor(Vector3(3081.922f, 2982.397f, 38.71634f), order_terminal), owning_building_guid = 2)
      LocalObject(798, ProximityTerminal.Constructor(Vector3(2935.311f, 2961.145f, 37.41034f), medical_terminal), owning_building_guid = 2)
      LocalObject(799, ProximityTerminal.Constructor(Vector3(2935.333f, 3006.849f, 37.41034f), medical_terminal), owning_building_guid = 2)
      LocalObject(802, ProximityTerminal.Constructor(Vector3(3076.631f, 3006.849f, 37.41034f), medical_terminal), owning_building_guid = 2)
      LocalObject(803, ProximityTerminal.Constructor(Vector3(3076.693f, 2961.145f, 37.41034f), medical_terminal), owning_building_guid = 2)
      LocalObject(540, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(516, Terminal.Constructor(Vector3(2941.677f, 3005.711f, 37.41934f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(540, 516)
      LocalObject(541, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(517, Terminal.Constructor(Vector3(2941.702f, 2962.303f, 37.41934f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(541, 517)
      LocalObject(542, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(518, Terminal.Constructor(Vector3(2947.005f, 2962.303f, 37.41934f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(542, 518)
      LocalObject(543, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(519, Terminal.Constructor(Vector3(2947.084f, 3005.711f, 37.41934f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(543, 519)
      LocalObject(548, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(524, Terminal.Constructor(Vector3(3064.927f, 2962.299f, 37.41934f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(548, 524)
      LocalObject(549, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(525, Terminal.Constructor(Vector3(3064.922f, 3005.701f, 37.41934f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(549, 525)
      LocalObject(550, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(526, Terminal.Constructor(Vector3(3070.225f, 3005.701f, 37.41934f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(550, 526)
      LocalObject(551, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(527, Terminal.Constructor(Vector3(3070.335f, 2962.299f, 37.41934f), implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(551, 527)
    }

    Building22()

    def Building22(): Unit = { // Name: Hart_Forseral Type: orbital_building_tr GUID: 3, MapID: 22
      LocalBuilding("Hart_Forseral", 3, 22, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5232f, 3908f, 35.9291f), orbital_building_tr)))
      LocalObject(402, Door.Constructor(Vector3(5152f, 3895.99f, 40.0351f)), owning_building_guid = 3)
      LocalObject(403, Door.Constructor(Vector3(5152f, 3919.99f, 40.0351f)), owning_building_guid = 3)
      LocalObject(406, Door.Constructor(Vector3(5312f, 3896.01f, 40.0351f)), owning_building_guid = 3)
      LocalObject(407, Door.Constructor(Vector3(5312f, 3920.01f, 40.0351f)), owning_building_guid = 3)
      LocalObject(422, Door.Constructor(Vector3(5170.34f, 3908f, 40.0351f)), owning_building_guid = 3)
      LocalObject(423, Door.Constructor(Vector3(5170.34f, 3908f, 50.0351f)), owning_building_guid = 3)
      LocalObject(424, Door.Constructor(Vector3(5216f, 3955.99f, 55.0351f)), owning_building_guid = 3)
      LocalObject(425, Door.Constructor(Vector3(5248f, 3955.99f, 55.0351f)), owning_building_guid = 3)
      LocalObject(426, Door.Constructor(Vector3(5293.64f, 3908f, 40.0351f)), owning_building_guid = 3)
      LocalObject(427, Door.Constructor(Vector3(5293.64f, 3908f, 50.0351f)), owning_building_guid = 3)
      LocalObject(486, Door.Constructor(Vector3(5176f, 3928.01f, 50.0351f)), owning_building_guid = 3)
      LocalObject(487, Door.Constructor(Vector3(5288f, 3927.99f, 50.0351f)), owning_building_guid = 3)
      LocalObject(504, Door.Constructor(Vector3(5176f, 3896.015f, 40.0351f)), owning_building_guid = 3)
      LocalObject(505, Door.Constructor(Vector3(5176f, 3896.015f, 50.0351f)), owning_building_guid = 3)
      LocalObject(506, Door.Constructor(Vector3(5176f, 3920.015f, 40.0351f)), owning_building_guid = 3)
      LocalObject(507, Door.Constructor(Vector3(5176f, 3920.015f, 50.0351f)), owning_building_guid = 3)
      LocalObject(508, Door.Constructor(Vector3(5288f, 3895.985f, 40.0351f)), owning_building_guid = 3)
      LocalObject(509, Door.Constructor(Vector3(5288f, 3895.985f, 50.0351f)), owning_building_guid = 3)
      LocalObject(510, Door.Constructor(Vector3(5288f, 3919.985f, 40.0351f)), owning_building_guid = 3)
      LocalObject(511, Door.Constructor(Vector3(5288f, 3919.985f, 50.0351f)), owning_building_guid = 3)
      LocalObject(716, Locker.Constructor(Vector3(5227.402f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(717, Locker.Constructor(Vector3(5228.732f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(718, Locker.Constructor(Vector3(5230.049f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(719, Locker.Constructor(Vector3(5231.325f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(720, Locker.Constructor(Vector3(5232.614f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(721, Locker.Constructor(Vector3(5233.934f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(722, Locker.Constructor(Vector3(5235.276f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(723, Locker.Constructor(Vector3(5236.64f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(234, Terminal.Constructor(Vector3(5153.212f, 3900.806f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(235, Terminal.Constructor(Vector3(5153.212f, 3902.713f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(236, Terminal.Constructor(Vector3(5153.212f, 3904.644f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(237, Terminal.Constructor(Vector3(5153.296f, 3911.669f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(238, Terminal.Constructor(Vector3(5153.296f, 3913.576f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(239, Terminal.Constructor(Vector3(5153.296f, 3915.507f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(240, Terminal.Constructor(Vector3(5164.556f, 3892.06f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(241, Terminal.Constructor(Vector3(5164.556f, 3895.963f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(242, Terminal.Constructor(Vector3(5164.556f, 3899.801f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(243, Terminal.Constructor(Vector3(5164.628f, 3916.44f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(244, Terminal.Constructor(Vector3(5164.628f, 3920.319f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(245, Terminal.Constructor(Vector3(5164.628f, 3924.191f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(246, Terminal.Constructor(Vector3(5299.371f, 3916.203f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(247, Terminal.Constructor(Vector3(5299.371f, 3920.041f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(248, Terminal.Constructor(Vector3(5299.371f, 3923.944f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(249, Terminal.Constructor(Vector3(5299.383f, 3891.869f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(250, Terminal.Constructor(Vector3(5299.383f, 3895.741f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(251, Terminal.Constructor(Vector3(5299.383f, 3899.619f, 48.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(252, Terminal.Constructor(Vector3(5310.715f, 3900.552f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(253, Terminal.Constructor(Vector3(5310.715f, 3902.483f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(254, Terminal.Constructor(Vector3(5310.715f, 3904.391f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(255, Terminal.Constructor(Vector3(5310.715f, 3911.36f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(256, Terminal.Constructor(Vector3(5310.715f, 3913.291f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(257, Terminal.Constructor(Vector3(5310.715f, 3915.198f, 38.4291f), cert_terminal), owning_building_guid = 3)
      LocalObject(920, Terminal.Constructor(Vector3(5156.069f, 3906.397f, 39.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(921, Terminal.Constructor(Vector3(5156.09f, 3909.595f, 39.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(922, Terminal.Constructor(Vector3(5159.959f, 3906.397f, 39.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(923, Terminal.Constructor(Vector3(5160.104f, 3909.595f, 39.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(924, Terminal.Constructor(Vector3(5174.478f, 3911.834f, 49.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(925, Terminal.Constructor(Vector3(5174.478f, 3914.735f, 49.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(926, Terminal.Constructor(Vector3(5174.485f, 3901.262f, 49.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(927, Terminal.Constructor(Vector3(5174.485f, 3904.183f, 49.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(928, Terminal.Constructor(Vector3(5221.52f, 3957.783f, 54.7221f), order_terminal), owning_building_guid = 3)
      LocalObject(929, Terminal.Constructor(Vector3(5224f, 3957.783f, 54.7221f), order_terminal), owning_building_guid = 3)
      LocalObject(936, Terminal.Constructor(Vector3(5240f, 3957.783f, 54.7221f), order_terminal), owning_building_guid = 3)
      LocalObject(937, Terminal.Constructor(Vector3(5242.39f, 3957.783f, 54.7221f), order_terminal), owning_building_guid = 3)
      LocalObject(938, Terminal.Constructor(Vector3(5289.508f, 3901.236f, 49.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(939, Terminal.Constructor(Vector3(5289.508f, 3904.137f, 49.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(940, Terminal.Constructor(Vector3(5289.505f, 3911.817f, 49.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(941, Terminal.Constructor(Vector3(5289.505f, 3914.738f, 49.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(942, Terminal.Constructor(Vector3(5303.908f, 3906.397f, 39.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(943, Terminal.Constructor(Vector3(5303.968f, 3909.592f, 39.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(944, Terminal.Constructor(Vector3(5307.858f, 3909.592f, 39.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(945, Terminal.Constructor(Vector3(5307.922f, 3906.397f, 39.7261f), order_terminal), owning_building_guid = 3)
      LocalObject(804, ProximityTerminal.Constructor(Vector3(5161.311f, 3885.145f, 38.4201f), medical_terminal), owning_building_guid = 3)
      LocalObject(805, ProximityTerminal.Constructor(Vector3(5161.333f, 3930.849f, 38.4201f), medical_terminal), owning_building_guid = 3)
      LocalObject(806, ProximityTerminal.Constructor(Vector3(5302.631f, 3930.849f, 38.4201f), medical_terminal), owning_building_guid = 3)
      LocalObject(807, ProximityTerminal.Constructor(Vector3(5302.693f, 3885.145f, 38.4201f), medical_terminal), owning_building_guid = 3)
      LocalObject(552, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(528, Terminal.Constructor(Vector3(5167.677f, 3929.711f, 38.4291f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(552, 528)
      LocalObject(553, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(529, Terminal.Constructor(Vector3(5167.702f, 3886.303f, 38.4291f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(553, 529)
      LocalObject(554, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(530, Terminal.Constructor(Vector3(5173.005f, 3886.303f, 38.4291f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(554, 530)
      LocalObject(555, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(531, Terminal.Constructor(Vector3(5173.084f, 3929.711f, 38.4291f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(555, 531)
      LocalObject(556, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(532, Terminal.Constructor(Vector3(5290.927f, 3886.299f, 38.4291f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(556, 532)
      LocalObject(557, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(533, Terminal.Constructor(Vector3(5290.922f, 3929.701f, 38.4291f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(557, 533)
      LocalObject(558, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(534, Terminal.Constructor(Vector3(5296.225f, 3929.701f, 38.4291f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(558, 534)
      LocalObject(559, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(535, Terminal.Constructor(Vector3(5296.335f, 3886.299f, 38.4291f), implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(559, 535)
    }

    Building58()

    def Building58(): Unit = { // Name: NW_Ishundar_WG_tower Type: tower_a GUID: 52, MapID: 58
      LocalBuilding("NW_Ishundar_WG_tower", 52, 58, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(2620f, 5418f, 35.99397f), tower_a)))
      LocalObject(1037, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 52)
      LocalObject(336, Door.Constructor(Vector3(2632f, 5410f, 37.51497f)), owning_building_guid = 52)
      LocalObject(337, Door.Constructor(Vector3(2632f, 5410f, 57.51397f)), owning_building_guid = 52)
      LocalObject(338, Door.Constructor(Vector3(2632f, 5426f, 37.51497f)), owning_building_guid = 52)
      LocalObject(339, Door.Constructor(Vector3(2632f, 5426f, 57.51397f)), owning_building_guid = 52)
      LocalObject(1047, Door.Constructor(Vector3(2631.146f, 5406.794f, 27.32997f)), owning_building_guid = 52)
      LocalObject(1048, Door.Constructor(Vector3(2631.146f, 5423.204f, 27.32997f)), owning_building_guid = 52)
      LocalObject(560, IFFLock.Constructor(Vector3(2629.957f, 5426.811f, 37.45497f), Vector3(0, 0, 0)), owning_building_guid = 52, door_guid = 338)
      LocalObject(561, IFFLock.Constructor(Vector3(2629.957f, 5426.811f, 57.45497f), Vector3(0, 0, 0)), owning_building_guid = 52, door_guid = 339)
      LocalObject(562, IFFLock.Constructor(Vector3(2634.047f, 5409.189f, 37.45497f), Vector3(0, 0, 180)), owning_building_guid = 52, door_guid = 336)
      LocalObject(563, IFFLock.Constructor(Vector3(2634.047f, 5409.189f, 57.45497f), Vector3(0, 0, 180)), owning_building_guid = 52, door_guid = 337)
      LocalObject(604, Locker.Constructor(Vector3(2635.716f, 5402.963f, 25.98797f)), owning_building_guid = 52)
      LocalObject(605, Locker.Constructor(Vector3(2635.751f, 5424.835f, 25.98797f)), owning_building_guid = 52)
      LocalObject(606, Locker.Constructor(Vector3(2637.053f, 5402.963f, 25.98797f)), owning_building_guid = 52)
      LocalObject(607, Locker.Constructor(Vector3(2637.088f, 5424.835f, 25.98797f)), owning_building_guid = 52)
      LocalObject(608, Locker.Constructor(Vector3(2639.741f, 5402.963f, 25.98797f)), owning_building_guid = 52)
      LocalObject(609, Locker.Constructor(Vector3(2639.741f, 5424.835f, 25.98797f)), owning_building_guid = 52)
      LocalObject(610, Locker.Constructor(Vector3(2641.143f, 5402.963f, 25.98797f)), owning_building_guid = 52)
      LocalObject(611, Locker.Constructor(Vector3(2641.143f, 5424.835f, 25.98797f)), owning_building_guid = 52)
      LocalObject(811, Terminal.Constructor(Vector3(2641.445f, 5408.129f, 27.32597f), order_terminal), owning_building_guid = 52)
      LocalObject(812, Terminal.Constructor(Vector3(2641.445f, 5413.853f, 27.32597f), order_terminal), owning_building_guid = 52)
      LocalObject(813, Terminal.Constructor(Vector3(2641.445f, 5419.234f, 27.32597f), order_terminal), owning_building_guid = 52)
      LocalObject(1017, SpawnTube.Constructor(Vector3(2630.706f, 5405.742f, 25.47597f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 52)
      LocalObject(1018, SpawnTube.Constructor(Vector3(2630.706f, 5422.152f, 25.47597f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 52)
      LocalObject(684, FacilityTurret.Constructor(Vector3(2607.32f, 5405.295f, 54.93597f), manned_turret), owning_building_guid = 52)
      TurretToWeapon(684, 5000)
      LocalObject(685, FacilityTurret.Constructor(Vector3(2642.647f, 5430.707f, 54.93597f), manned_turret), owning_building_guid = 52)
      TurretToWeapon(685, 5001)
      LocalObject(987, Painbox.Constructor(Vector3(2625.235f, 5411.803f, 27.49307f), painbox_radius_continuous), owning_building_guid = 52)
      LocalObject(988, Painbox.Constructor(Vector3(2636.889f, 5420.086f, 26.09397f), painbox_radius_continuous), owning_building_guid = 52)
      LocalObject(989, Painbox.Constructor(Vector3(2636.975f, 5408.223f, 26.09397f), painbox_radius_continuous), owning_building_guid = 52)
    }

    Building66()

    def Building66(): Unit = { // Name: W_Cyssor_WG_tower Type: tower_a GUID: 53, MapID: 66
      LocalBuilding("W_Cyssor_WG_tower", 53, 66, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(2806f, 3292f, 32.44872f), tower_a)))
      LocalObject(1039, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 53)
      LocalObject(346, Door.Constructor(Vector3(2818f, 3284f, 33.96972f)), owning_building_guid = 53)
      LocalObject(347, Door.Constructor(Vector3(2818f, 3284f, 53.96872f)), owning_building_guid = 53)
      LocalObject(348, Door.Constructor(Vector3(2818f, 3300f, 33.96972f)), owning_building_guid = 53)
      LocalObject(349, Door.Constructor(Vector3(2818f, 3300f, 53.96872f)), owning_building_guid = 53)
      LocalObject(1051, Door.Constructor(Vector3(2817.146f, 3280.794f, 23.78472f)), owning_building_guid = 53)
      LocalObject(1052, Door.Constructor(Vector3(2817.146f, 3297.204f, 23.78472f)), owning_building_guid = 53)
      LocalObject(570, IFFLock.Constructor(Vector3(2815.957f, 3300.811f, 33.90971f), Vector3(0, 0, 0)), owning_building_guid = 53, door_guid = 348)
      LocalObject(571, IFFLock.Constructor(Vector3(2815.957f, 3300.811f, 53.90971f), Vector3(0, 0, 0)), owning_building_guid = 53, door_guid = 349)
      LocalObject(572, IFFLock.Constructor(Vector3(2820.047f, 3283.189f, 33.90971f), Vector3(0, 0, 180)), owning_building_guid = 53, door_guid = 346)
      LocalObject(573, IFFLock.Constructor(Vector3(2820.047f, 3283.189f, 53.90971f), Vector3(0, 0, 180)), owning_building_guid = 53, door_guid = 347)
      LocalObject(620, Locker.Constructor(Vector3(2821.716f, 3276.963f, 22.44271f)), owning_building_guid = 53)
      LocalObject(621, Locker.Constructor(Vector3(2821.751f, 3298.835f, 22.44271f)), owning_building_guid = 53)
      LocalObject(622, Locker.Constructor(Vector3(2823.053f, 3276.963f, 22.44271f)), owning_building_guid = 53)
      LocalObject(623, Locker.Constructor(Vector3(2823.088f, 3298.835f, 22.44271f)), owning_building_guid = 53)
      LocalObject(624, Locker.Constructor(Vector3(2825.741f, 3276.963f, 22.44271f)), owning_building_guid = 53)
      LocalObject(625, Locker.Constructor(Vector3(2825.741f, 3298.835f, 22.44271f)), owning_building_guid = 53)
      LocalObject(626, Locker.Constructor(Vector3(2827.143f, 3276.963f, 22.44271f)), owning_building_guid = 53)
      LocalObject(627, Locker.Constructor(Vector3(2827.143f, 3298.835f, 22.44271f)), owning_building_guid = 53)
      LocalObject(823, Terminal.Constructor(Vector3(2827.445f, 3282.129f, 23.78072f), order_terminal), owning_building_guid = 53)
      LocalObject(824, Terminal.Constructor(Vector3(2827.445f, 3287.853f, 23.78072f), order_terminal), owning_building_guid = 53)
      LocalObject(825, Terminal.Constructor(Vector3(2827.445f, 3293.234f, 23.78072f), order_terminal), owning_building_guid = 53)
      LocalObject(1021, SpawnTube.Constructor(Vector3(2816.706f, 3279.742f, 21.93072f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 53)
      LocalObject(1022, SpawnTube.Constructor(Vector3(2816.706f, 3296.152f, 21.93072f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 53)
      LocalObject(686, FacilityTurret.Constructor(Vector3(2793.32f, 3279.295f, 51.39072f), manned_turret), owning_building_guid = 53)
      TurretToWeapon(686, 5002)
      LocalObject(687, FacilityTurret.Constructor(Vector3(2828.647f, 3304.707f, 51.39072f), manned_turret), owning_building_guid = 53)
      TurretToWeapon(687, 5003)
      LocalObject(992, Painbox.Constructor(Vector3(2811.235f, 3285.803f, 23.94781f), painbox_radius_continuous), owning_building_guid = 53)
      LocalObject(994, Painbox.Constructor(Vector3(2822.889f, 3294.086f, 22.54872f), painbox_radius_continuous), owning_building_guid = 53)
      LocalObject(995, Painbox.Constructor(Vector3(2822.975f, 3282.223f, 22.54872f), painbox_radius_continuous), owning_building_guid = 53)
    }

    Building60()

    def Building60(): Unit = { // Name: W_Forseral_WG_tower Type: tower_a GUID: 54, MapID: 60
      LocalBuilding("W_Forseral_WG_tower", 54, 60, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4564f, 4572f, 54.12079f), tower_a)))
      LocalObject(1044, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 54)
      LocalObject(368, Door.Constructor(Vector3(4576f, 4564f, 55.64179f)), owning_building_guid = 54)
      LocalObject(369, Door.Constructor(Vector3(4576f, 4564f, 75.64079f)), owning_building_guid = 54)
      LocalObject(370, Door.Constructor(Vector3(4576f, 4580f, 55.64179f)), owning_building_guid = 54)
      LocalObject(371, Door.Constructor(Vector3(4576f, 4580f, 75.64079f)), owning_building_guid = 54)
      LocalObject(1061, Door.Constructor(Vector3(4575.146f, 4560.794f, 45.45679f)), owning_building_guid = 54)
      LocalObject(1062, Door.Constructor(Vector3(4575.146f, 4577.204f, 45.45679f)), owning_building_guid = 54)
      LocalObject(592, IFFLock.Constructor(Vector3(4573.957f, 4580.811f, 55.58179f), Vector3(0, 0, 0)), owning_building_guid = 54, door_guid = 370)
      LocalObject(593, IFFLock.Constructor(Vector3(4573.957f, 4580.811f, 75.58179f), Vector3(0, 0, 0)), owning_building_guid = 54, door_guid = 371)
      LocalObject(594, IFFLock.Constructor(Vector3(4578.047f, 4563.189f, 55.58179f), Vector3(0, 0, 180)), owning_building_guid = 54, door_guid = 368)
      LocalObject(595, IFFLock.Constructor(Vector3(4578.047f, 4563.189f, 75.58179f), Vector3(0, 0, 180)), owning_building_guid = 54, door_guid = 369)
      LocalObject(660, Locker.Constructor(Vector3(4579.716f, 4556.963f, 44.11479f)), owning_building_guid = 54)
      LocalObject(661, Locker.Constructor(Vector3(4579.751f, 4578.835f, 44.11479f)), owning_building_guid = 54)
      LocalObject(662, Locker.Constructor(Vector3(4581.053f, 4556.963f, 44.11479f)), owning_building_guid = 54)
      LocalObject(663, Locker.Constructor(Vector3(4581.088f, 4578.835f, 44.11479f)), owning_building_guid = 54)
      LocalObject(664, Locker.Constructor(Vector3(4583.741f, 4556.963f, 44.11479f)), owning_building_guid = 54)
      LocalObject(665, Locker.Constructor(Vector3(4583.741f, 4578.835f, 44.11479f)), owning_building_guid = 54)
      LocalObject(666, Locker.Constructor(Vector3(4585.143f, 4556.963f, 44.11479f)), owning_building_guid = 54)
      LocalObject(667, Locker.Constructor(Vector3(4585.143f, 4578.835f, 44.11479f)), owning_building_guid = 54)
      LocalObject(908, Terminal.Constructor(Vector3(4585.445f, 4562.129f, 45.45279f), order_terminal), owning_building_guid = 54)
      LocalObject(909, Terminal.Constructor(Vector3(4585.445f, 4567.853f, 45.45279f), order_terminal), owning_building_guid = 54)
      LocalObject(910, Terminal.Constructor(Vector3(4585.445f, 4573.234f, 45.45279f), order_terminal), owning_building_guid = 54)
      LocalObject(1031, SpawnTube.Constructor(Vector3(4574.706f, 4559.742f, 43.60279f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 54)
      LocalObject(1032, SpawnTube.Constructor(Vector3(4574.706f, 4576.152f, 43.60279f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 54)
      LocalObject(694, FacilityTurret.Constructor(Vector3(4551.32f, 4559.295f, 73.06279f), manned_turret), owning_building_guid = 54)
      TurretToWeapon(694, 5004)
      LocalObject(695, FacilityTurret.Constructor(Vector3(4586.647f, 4584.707f, 73.06279f), manned_turret), owning_building_guid = 54)
      TurretToWeapon(695, 5005)
      LocalObject(1008, Painbox.Constructor(Vector3(4569.235f, 4565.803f, 45.61989f), painbox_radius_continuous), owning_building_guid = 54)
      LocalObject(1009, Painbox.Constructor(Vector3(4580.889f, 4574.086f, 44.22079f), painbox_radius_continuous), owning_building_guid = 54)
      LocalObject(1010, Painbox.Constructor(Vector3(4580.975f, 4562.223f, 44.22079f), painbox_radius_continuous), owning_building_guid = 54)
    }

    Building63()

    def Building63(): Unit = { // Name: SW_Forseral_WG_tower Type: tower_a GUID: 55, MapID: 63
      LocalBuilding("SW_Forseral_WG_tower", 55, 63, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4972f, 3678f, 35.9291f), tower_a)))
      LocalObject(1045, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 55)
      LocalObject(372, Door.Constructor(Vector3(4984f, 3670f, 37.4501f)), owning_building_guid = 55)
      LocalObject(373, Door.Constructor(Vector3(4984f, 3670f, 57.4491f)), owning_building_guid = 55)
      LocalObject(374, Door.Constructor(Vector3(4984f, 3686f, 37.4501f)), owning_building_guid = 55)
      LocalObject(375, Door.Constructor(Vector3(4984f, 3686f, 57.4491f)), owning_building_guid = 55)
      LocalObject(1063, Door.Constructor(Vector3(4983.146f, 3666.794f, 27.2651f)), owning_building_guid = 55)
      LocalObject(1064, Door.Constructor(Vector3(4983.146f, 3683.204f, 27.2651f)), owning_building_guid = 55)
      LocalObject(596, IFFLock.Constructor(Vector3(4981.957f, 3686.811f, 37.3901f), Vector3(0, 0, 0)), owning_building_guid = 55, door_guid = 374)
      LocalObject(597, IFFLock.Constructor(Vector3(4981.957f, 3686.811f, 57.39011f), Vector3(0, 0, 0)), owning_building_guid = 55, door_guid = 375)
      LocalObject(598, IFFLock.Constructor(Vector3(4986.047f, 3669.189f, 37.3901f), Vector3(0, 0, 180)), owning_building_guid = 55, door_guid = 372)
      LocalObject(599, IFFLock.Constructor(Vector3(4986.047f, 3669.189f, 57.39011f), Vector3(0, 0, 180)), owning_building_guid = 55, door_guid = 373)
      LocalObject(668, Locker.Constructor(Vector3(4987.716f, 3662.963f, 25.9231f)), owning_building_guid = 55)
      LocalObject(669, Locker.Constructor(Vector3(4987.751f, 3684.835f, 25.9231f)), owning_building_guid = 55)
      LocalObject(670, Locker.Constructor(Vector3(4989.053f, 3662.963f, 25.9231f)), owning_building_guid = 55)
      LocalObject(671, Locker.Constructor(Vector3(4989.088f, 3684.835f, 25.9231f)), owning_building_guid = 55)
      LocalObject(672, Locker.Constructor(Vector3(4991.741f, 3662.963f, 25.9231f)), owning_building_guid = 55)
      LocalObject(673, Locker.Constructor(Vector3(4991.741f, 3684.835f, 25.9231f)), owning_building_guid = 55)
      LocalObject(674, Locker.Constructor(Vector3(4993.143f, 3662.963f, 25.9231f)), owning_building_guid = 55)
      LocalObject(675, Locker.Constructor(Vector3(4993.143f, 3684.835f, 25.9231f)), owning_building_guid = 55)
      LocalObject(911, Terminal.Constructor(Vector3(4993.445f, 3668.129f, 27.2611f), order_terminal), owning_building_guid = 55)
      LocalObject(912, Terminal.Constructor(Vector3(4993.445f, 3673.853f, 27.2611f), order_terminal), owning_building_guid = 55)
      LocalObject(913, Terminal.Constructor(Vector3(4993.445f, 3679.234f, 27.2611f), order_terminal), owning_building_guid = 55)
      LocalObject(1033, SpawnTube.Constructor(Vector3(4982.706f, 3665.742f, 25.4111f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 55)
      LocalObject(1034, SpawnTube.Constructor(Vector3(4982.706f, 3682.152f, 25.4111f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 55)
      LocalObject(696, FacilityTurret.Constructor(Vector3(4959.32f, 3665.295f, 54.8711f), manned_turret), owning_building_guid = 55)
      TurretToWeapon(696, 5006)
      LocalObject(697, FacilityTurret.Constructor(Vector3(4994.647f, 3690.707f, 54.8711f), manned_turret), owning_building_guid = 55)
      TurretToWeapon(697, 5007)
      LocalObject(1011, Painbox.Constructor(Vector3(4977.235f, 3671.803f, 27.4282f), painbox_radius_continuous), owning_building_guid = 55)
      LocalObject(1012, Painbox.Constructor(Vector3(4988.889f, 3680.086f, 26.0291f), painbox_radius_continuous), owning_building_guid = 55)
      LocalObject(1013, Painbox.Constructor(Vector3(4988.975f, 3668.223f, 26.0291f), painbox_radius_continuous), owning_building_guid = 55)
    }

    Building65()

    def Building65(): Unit = { // Name: SW_Ishundar_WG_tower Type: tower_b GUID: 56, MapID: 65
      LocalBuilding("SW_Ishundar_WG_tower", 56, 65, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(2794f, 4210f, 36.31348f), tower_b)))
      LocalObject(1038, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 56)
      LocalObject(340, Door.Constructor(Vector3(2806f, 4202f, 37.83348f)), owning_building_guid = 56)
      LocalObject(341, Door.Constructor(Vector3(2806f, 4202f, 47.83348f)), owning_building_guid = 56)
      LocalObject(342, Door.Constructor(Vector3(2806f, 4202f, 67.83348f)), owning_building_guid = 56)
      LocalObject(343, Door.Constructor(Vector3(2806f, 4218f, 37.83348f)), owning_building_guid = 56)
      LocalObject(344, Door.Constructor(Vector3(2806f, 4218f, 47.83348f)), owning_building_guid = 56)
      LocalObject(345, Door.Constructor(Vector3(2806f, 4218f, 67.83348f)), owning_building_guid = 56)
      LocalObject(1049, Door.Constructor(Vector3(2805.147f, 4198.794f, 27.64948f)), owning_building_guid = 56)
      LocalObject(1050, Door.Constructor(Vector3(2805.147f, 4215.204f, 27.64948f)), owning_building_guid = 56)
      LocalObject(564, IFFLock.Constructor(Vector3(2803.957f, 4218.811f, 37.77448f), Vector3(0, 0, 0)), owning_building_guid = 56, door_guid = 343)
      LocalObject(565, IFFLock.Constructor(Vector3(2803.957f, 4218.811f, 47.77448f), Vector3(0, 0, 0)), owning_building_guid = 56, door_guid = 344)
      LocalObject(566, IFFLock.Constructor(Vector3(2803.957f, 4218.811f, 67.77448f), Vector3(0, 0, 0)), owning_building_guid = 56, door_guid = 345)
      LocalObject(567, IFFLock.Constructor(Vector3(2808.047f, 4201.189f, 37.77448f), Vector3(0, 0, 180)), owning_building_guid = 56, door_guid = 340)
      LocalObject(568, IFFLock.Constructor(Vector3(2808.047f, 4201.189f, 47.77448f), Vector3(0, 0, 180)), owning_building_guid = 56, door_guid = 341)
      LocalObject(569, IFFLock.Constructor(Vector3(2808.047f, 4201.189f, 67.77448f), Vector3(0, 0, 180)), owning_building_guid = 56, door_guid = 342)
      LocalObject(612, Locker.Constructor(Vector3(2809.716f, 4194.963f, 26.30748f)), owning_building_guid = 56)
      LocalObject(613, Locker.Constructor(Vector3(2809.751f, 4216.835f, 26.30748f)), owning_building_guid = 56)
      LocalObject(614, Locker.Constructor(Vector3(2811.053f, 4194.963f, 26.30748f)), owning_building_guid = 56)
      LocalObject(615, Locker.Constructor(Vector3(2811.088f, 4216.835f, 26.30748f)), owning_building_guid = 56)
      LocalObject(616, Locker.Constructor(Vector3(2813.741f, 4194.963f, 26.30748f)), owning_building_guid = 56)
      LocalObject(617, Locker.Constructor(Vector3(2813.741f, 4216.835f, 26.30748f)), owning_building_guid = 56)
      LocalObject(618, Locker.Constructor(Vector3(2815.143f, 4194.963f, 26.30748f)), owning_building_guid = 56)
      LocalObject(619, Locker.Constructor(Vector3(2815.143f, 4216.835f, 26.30748f)), owning_building_guid = 56)
      LocalObject(820, Terminal.Constructor(Vector3(2815.446f, 4200.129f, 27.64548f), order_terminal), owning_building_guid = 56)
      LocalObject(821, Terminal.Constructor(Vector3(2815.446f, 4205.853f, 27.64548f), order_terminal), owning_building_guid = 56)
      LocalObject(822, Terminal.Constructor(Vector3(2815.446f, 4211.234f, 27.64548f), order_terminal), owning_building_guid = 56)
      LocalObject(1019, SpawnTube.Constructor(Vector3(2804.706f, 4197.742f, 25.79548f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 56)
      LocalObject(1020, SpawnTube.Constructor(Vector3(2804.706f, 4214.152f, 25.79548f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 56)
      LocalObject(990, Painbox.Constructor(Vector3(2799.493f, 4202.849f, 27.60288f), painbox_radius_continuous), owning_building_guid = 56)
      LocalObject(991, Painbox.Constructor(Vector3(2811.127f, 4200.078f, 26.41348f), painbox_radius_continuous), owning_building_guid = 56)
      LocalObject(993, Painbox.Constructor(Vector3(2811.259f, 4212.107f, 26.41348f), painbox_radius_continuous), owning_building_guid = 56)
    }

    Building64()

    def Building64(): Unit = { // Name: E_Cyssor_WG_tower Type: tower_b GUID: 57, MapID: 64
      LocalBuilding("E_Cyssor_WG_tower", 57, 64, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4484f, 3080f, 50.39579f), tower_b)))
      LocalObject(1043, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 57)
      LocalObject(362, Door.Constructor(Vector3(4496f, 3072f, 51.91579f)), owning_building_guid = 57)
      LocalObject(363, Door.Constructor(Vector3(4496f, 3072f, 61.91579f)), owning_building_guid = 57)
      LocalObject(364, Door.Constructor(Vector3(4496f, 3072f, 81.91579f)), owning_building_guid = 57)
      LocalObject(365, Door.Constructor(Vector3(4496f, 3088f, 51.91579f)), owning_building_guid = 57)
      LocalObject(366, Door.Constructor(Vector3(4496f, 3088f, 61.91579f)), owning_building_guid = 57)
      LocalObject(367, Door.Constructor(Vector3(4496f, 3088f, 81.91579f)), owning_building_guid = 57)
      LocalObject(1059, Door.Constructor(Vector3(4495.147f, 3068.794f, 41.7318f)), owning_building_guid = 57)
      LocalObject(1060, Door.Constructor(Vector3(4495.147f, 3085.204f, 41.7318f)), owning_building_guid = 57)
      LocalObject(586, IFFLock.Constructor(Vector3(4493.957f, 3088.811f, 51.85679f), Vector3(0, 0, 0)), owning_building_guid = 57, door_guid = 365)
      LocalObject(587, IFFLock.Constructor(Vector3(4493.957f, 3088.811f, 61.8568f), Vector3(0, 0, 0)), owning_building_guid = 57, door_guid = 366)
      LocalObject(588, IFFLock.Constructor(Vector3(4493.957f, 3088.811f, 81.8568f), Vector3(0, 0, 0)), owning_building_guid = 57, door_guid = 367)
      LocalObject(589, IFFLock.Constructor(Vector3(4498.047f, 3071.189f, 51.85679f), Vector3(0, 0, 180)), owning_building_guid = 57, door_guid = 362)
      LocalObject(590, IFFLock.Constructor(Vector3(4498.047f, 3071.189f, 61.8568f), Vector3(0, 0, 180)), owning_building_guid = 57, door_guid = 363)
      LocalObject(591, IFFLock.Constructor(Vector3(4498.047f, 3071.189f, 81.8568f), Vector3(0, 0, 180)), owning_building_guid = 57, door_guid = 364)
      LocalObject(652, Locker.Constructor(Vector3(4499.716f, 3064.963f, 40.38979f)), owning_building_guid = 57)
      LocalObject(653, Locker.Constructor(Vector3(4499.751f, 3086.835f, 40.38979f)), owning_building_guid = 57)
      LocalObject(654, Locker.Constructor(Vector3(4501.053f, 3064.963f, 40.38979f)), owning_building_guid = 57)
      LocalObject(655, Locker.Constructor(Vector3(4501.088f, 3086.835f, 40.38979f)), owning_building_guid = 57)
      LocalObject(656, Locker.Constructor(Vector3(4503.741f, 3064.963f, 40.38979f)), owning_building_guid = 57)
      LocalObject(657, Locker.Constructor(Vector3(4503.741f, 3086.835f, 40.38979f)), owning_building_guid = 57)
      LocalObject(658, Locker.Constructor(Vector3(4505.143f, 3064.963f, 40.38979f)), owning_building_guid = 57)
      LocalObject(659, Locker.Constructor(Vector3(4505.143f, 3086.835f, 40.38979f)), owning_building_guid = 57)
      LocalObject(905, Terminal.Constructor(Vector3(4505.446f, 3070.129f, 41.72779f), order_terminal), owning_building_guid = 57)
      LocalObject(906, Terminal.Constructor(Vector3(4505.446f, 3075.853f, 41.72779f), order_terminal), owning_building_guid = 57)
      LocalObject(907, Terminal.Constructor(Vector3(4505.446f, 3081.234f, 41.72779f), order_terminal), owning_building_guid = 57)
      LocalObject(1029, SpawnTube.Constructor(Vector3(4494.706f, 3067.742f, 39.87779f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 57)
      LocalObject(1030, SpawnTube.Constructor(Vector3(4494.706f, 3084.152f, 39.87779f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 57)
      LocalObject(1005, Painbox.Constructor(Vector3(4489.493f, 3072.849f, 41.6852f), painbox_radius_continuous), owning_building_guid = 57)
      LocalObject(1006, Painbox.Constructor(Vector3(4501.127f, 3070.078f, 40.4958f), painbox_radius_continuous), owning_building_guid = 57)
      LocalObject(1007, Painbox.Constructor(Vector3(4501.259f, 3082.107f, 40.4958f), painbox_radius_continuous), owning_building_guid = 57)
    }

    Building59()

    def Building59(): Unit = { // Name: N_Ishundar_WG_tower Type: tower_c GUID: 58, MapID: 59
      LocalBuilding("N_Ishundar_WG_tower", 58, 59, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3236f, 5040f, 37.58544f), tower_c)))
      LocalObject(1040, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 58)
      LocalObject(350, Door.Constructor(Vector3(3248f, 5032f, 39.10644f)), owning_building_guid = 58)
      LocalObject(351, Door.Constructor(Vector3(3248f, 5032f, 59.10544f)), owning_building_guid = 58)
      LocalObject(352, Door.Constructor(Vector3(3248f, 5048f, 39.10644f)), owning_building_guid = 58)
      LocalObject(353, Door.Constructor(Vector3(3248f, 5048f, 59.10544f)), owning_building_guid = 58)
      LocalObject(1053, Door.Constructor(Vector3(3247.146f, 5028.794f, 28.92144f)), owning_building_guid = 58)
      LocalObject(1054, Door.Constructor(Vector3(3247.146f, 5045.204f, 28.92144f)), owning_building_guid = 58)
      LocalObject(574, IFFLock.Constructor(Vector3(3245.957f, 5048.811f, 39.04644f), Vector3(0, 0, 0)), owning_building_guid = 58, door_guid = 352)
      LocalObject(575, IFFLock.Constructor(Vector3(3245.957f, 5048.811f, 59.04644f), Vector3(0, 0, 0)), owning_building_guid = 58, door_guid = 353)
      LocalObject(576, IFFLock.Constructor(Vector3(3250.047f, 5031.189f, 39.04644f), Vector3(0, 0, 180)), owning_building_guid = 58, door_guid = 350)
      LocalObject(577, IFFLock.Constructor(Vector3(3250.047f, 5031.189f, 59.04644f), Vector3(0, 0, 180)), owning_building_guid = 58, door_guid = 351)
      LocalObject(628, Locker.Constructor(Vector3(3251.716f, 5024.963f, 27.57944f)), owning_building_guid = 58)
      LocalObject(629, Locker.Constructor(Vector3(3251.751f, 5046.835f, 27.57944f)), owning_building_guid = 58)
      LocalObject(630, Locker.Constructor(Vector3(3253.053f, 5024.963f, 27.57944f)), owning_building_guid = 58)
      LocalObject(631, Locker.Constructor(Vector3(3253.088f, 5046.835f, 27.57944f)), owning_building_guid = 58)
      LocalObject(632, Locker.Constructor(Vector3(3255.741f, 5024.963f, 27.57944f)), owning_building_guid = 58)
      LocalObject(633, Locker.Constructor(Vector3(3255.741f, 5046.835f, 27.57944f)), owning_building_guid = 58)
      LocalObject(634, Locker.Constructor(Vector3(3257.143f, 5024.963f, 27.57944f)), owning_building_guid = 58)
      LocalObject(635, Locker.Constructor(Vector3(3257.143f, 5046.835f, 27.57944f)), owning_building_guid = 58)
      LocalObject(896, Terminal.Constructor(Vector3(3257.445f, 5030.129f, 28.91744f), order_terminal), owning_building_guid = 58)
      LocalObject(897, Terminal.Constructor(Vector3(3257.445f, 5035.853f, 28.91744f), order_terminal), owning_building_guid = 58)
      LocalObject(898, Terminal.Constructor(Vector3(3257.445f, 5041.234f, 28.91744f), order_terminal), owning_building_guid = 58)
      LocalObject(1023, SpawnTube.Constructor(Vector3(3246.706f, 5027.742f, 27.06744f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 58)
      LocalObject(1024, SpawnTube.Constructor(Vector3(3246.706f, 5044.152f, 27.06744f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 58)
      LocalObject(963, ProximityTerminal.Constructor(Vector3(3234.907f, 5034.725f, 65.15544f), pad_landing_tower_frame), owning_building_guid = 58)
      LocalObject(964, Terminal.Constructor(Vector3(3234.907f, 5034.725f, 65.15544f), air_rearm_terminal), owning_building_guid = 58)
      LocalObject(966, ProximityTerminal.Constructor(Vector3(3234.907f, 5045.17f, 65.15544f), pad_landing_tower_frame), owning_building_guid = 58)
      LocalObject(967, Terminal.Constructor(Vector3(3234.907f, 5045.17f, 65.15544f), air_rearm_terminal), owning_building_guid = 58)
      LocalObject(688, FacilityTurret.Constructor(Vector3(3221.07f, 5025.045f, 56.52744f), manned_turret), owning_building_guid = 58)
      TurretToWeapon(688, 5008)
      LocalObject(690, FacilityTurret.Constructor(Vector3(3259.497f, 5054.957f, 56.52744f), manned_turret), owning_building_guid = 58)
      TurretToWeapon(690, 5009)
      LocalObject(996, Painbox.Constructor(Vector3(3240.454f, 5032.849f, 29.60494f), painbox_radius_continuous), owning_building_guid = 58)
      LocalObject(997, Painbox.Constructor(Vector3(3252.923f, 5029.54f, 27.68544f), painbox_radius_continuous), owning_building_guid = 58)
      LocalObject(998, Painbox.Constructor(Vector3(3253.113f, 5042.022f, 27.68544f), painbox_radius_continuous), owning_building_guid = 58)
    }

    Building67()

    def Building67(): Unit = { // Name: SW_Cyssor_WG_tower Type: tower_c GUID: 59, MapID: 67
      LocalBuilding("SW_Cyssor_WG_tower", 59, 67, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3260f, 2758f, 34.92906f), tower_c)))
      LocalObject(1041, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 59)
      LocalObject(354, Door.Constructor(Vector3(3272f, 2750f, 36.45006f)), owning_building_guid = 59)
      LocalObject(355, Door.Constructor(Vector3(3272f, 2750f, 56.44906f)), owning_building_guid = 59)
      LocalObject(356, Door.Constructor(Vector3(3272f, 2766f, 36.45006f)), owning_building_guid = 59)
      LocalObject(357, Door.Constructor(Vector3(3272f, 2766f, 56.44906f)), owning_building_guid = 59)
      LocalObject(1055, Door.Constructor(Vector3(3271.146f, 2746.794f, 26.26506f)), owning_building_guid = 59)
      LocalObject(1056, Door.Constructor(Vector3(3271.146f, 2763.204f, 26.26506f)), owning_building_guid = 59)
      LocalObject(578, IFFLock.Constructor(Vector3(3269.957f, 2766.811f, 36.39006f), Vector3(0, 0, 0)), owning_building_guid = 59, door_guid = 356)
      LocalObject(579, IFFLock.Constructor(Vector3(3269.957f, 2766.811f, 56.39006f), Vector3(0, 0, 0)), owning_building_guid = 59, door_guid = 357)
      LocalObject(580, IFFLock.Constructor(Vector3(3274.047f, 2749.189f, 36.39006f), Vector3(0, 0, 180)), owning_building_guid = 59, door_guid = 354)
      LocalObject(581, IFFLock.Constructor(Vector3(3274.047f, 2749.189f, 56.39006f), Vector3(0, 0, 180)), owning_building_guid = 59, door_guid = 355)
      LocalObject(636, Locker.Constructor(Vector3(3275.716f, 2742.963f, 24.92306f)), owning_building_guid = 59)
      LocalObject(637, Locker.Constructor(Vector3(3275.751f, 2764.835f, 24.92306f)), owning_building_guid = 59)
      LocalObject(638, Locker.Constructor(Vector3(3277.053f, 2742.963f, 24.92306f)), owning_building_guid = 59)
      LocalObject(639, Locker.Constructor(Vector3(3277.088f, 2764.835f, 24.92306f)), owning_building_guid = 59)
      LocalObject(640, Locker.Constructor(Vector3(3279.741f, 2742.963f, 24.92306f)), owning_building_guid = 59)
      LocalObject(641, Locker.Constructor(Vector3(3279.741f, 2764.835f, 24.92306f)), owning_building_guid = 59)
      LocalObject(642, Locker.Constructor(Vector3(3281.143f, 2742.963f, 24.92306f)), owning_building_guid = 59)
      LocalObject(643, Locker.Constructor(Vector3(3281.143f, 2764.835f, 24.92306f)), owning_building_guid = 59)
      LocalObject(899, Terminal.Constructor(Vector3(3281.445f, 2748.129f, 26.26106f), order_terminal), owning_building_guid = 59)
      LocalObject(900, Terminal.Constructor(Vector3(3281.445f, 2753.853f, 26.26106f), order_terminal), owning_building_guid = 59)
      LocalObject(901, Terminal.Constructor(Vector3(3281.445f, 2759.234f, 26.26106f), order_terminal), owning_building_guid = 59)
      LocalObject(1025, SpawnTube.Constructor(Vector3(3270.706f, 2745.742f, 24.41106f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 59)
      LocalObject(1026, SpawnTube.Constructor(Vector3(3270.706f, 2762.152f, 24.41106f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 59)
      LocalObject(969, ProximityTerminal.Constructor(Vector3(3258.907f, 2752.725f, 62.49906f), pad_landing_tower_frame), owning_building_guid = 59)
      LocalObject(970, Terminal.Constructor(Vector3(3258.907f, 2752.725f, 62.49906f), air_rearm_terminal), owning_building_guid = 59)
      LocalObject(972, ProximityTerminal.Constructor(Vector3(3258.907f, 2763.17f, 62.49906f), pad_landing_tower_frame), owning_building_guid = 59)
      LocalObject(973, Terminal.Constructor(Vector3(3258.907f, 2763.17f, 62.49906f), air_rearm_terminal), owning_building_guid = 59)
      LocalObject(689, FacilityTurret.Constructor(Vector3(3245.07f, 2743.045f, 53.87106f), manned_turret), owning_building_guid = 59)
      TurretToWeapon(689, 5010)
      LocalObject(691, FacilityTurret.Constructor(Vector3(3283.497f, 2772.957f, 53.87106f), manned_turret), owning_building_guid = 59)
      TurretToWeapon(691, 5011)
      LocalObject(999, Painbox.Constructor(Vector3(3264.454f, 2750.849f, 26.94856f), painbox_radius_continuous), owning_building_guid = 59)
      LocalObject(1000, Painbox.Constructor(Vector3(3276.923f, 2747.54f, 25.02906f), painbox_radius_continuous), owning_building_guid = 59)
      LocalObject(1001, Painbox.Constructor(Vector3(3277.113f, 2760.022f, 25.02906f), painbox_radius_continuous), owning_building_guid = 59)
    }

    Building61()

    def Building61(): Unit = { // Name: N_Cyssor_WG_tower Type: tower_c GUID: 60, MapID: 61
      LocalBuilding("N_Cyssor_WG_tower", 60, 61, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3906f, 3832f, 46.40522f), tower_c)))
      LocalObject(1042, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 60)
      LocalObject(358, Door.Constructor(Vector3(3918f, 3824f, 47.92622f)), owning_building_guid = 60)
      LocalObject(359, Door.Constructor(Vector3(3918f, 3824f, 67.92522f)), owning_building_guid = 60)
      LocalObject(360, Door.Constructor(Vector3(3918f, 3840f, 47.92622f)), owning_building_guid = 60)
      LocalObject(361, Door.Constructor(Vector3(3918f, 3840f, 67.92522f)), owning_building_guid = 60)
      LocalObject(1057, Door.Constructor(Vector3(3917.146f, 3820.794f, 37.74123f)), owning_building_guid = 60)
      LocalObject(1058, Door.Constructor(Vector3(3917.146f, 3837.204f, 37.74123f)), owning_building_guid = 60)
      LocalObject(582, IFFLock.Constructor(Vector3(3915.957f, 3840.811f, 47.86622f), Vector3(0, 0, 0)), owning_building_guid = 60, door_guid = 360)
      LocalObject(583, IFFLock.Constructor(Vector3(3915.957f, 3840.811f, 67.86623f), Vector3(0, 0, 0)), owning_building_guid = 60, door_guid = 361)
      LocalObject(584, IFFLock.Constructor(Vector3(3920.047f, 3823.189f, 47.86622f), Vector3(0, 0, 180)), owning_building_guid = 60, door_guid = 358)
      LocalObject(585, IFFLock.Constructor(Vector3(3920.047f, 3823.189f, 67.86623f), Vector3(0, 0, 180)), owning_building_guid = 60, door_guid = 359)
      LocalObject(644, Locker.Constructor(Vector3(3921.716f, 3816.963f, 36.39922f)), owning_building_guid = 60)
      LocalObject(645, Locker.Constructor(Vector3(3921.751f, 3838.835f, 36.39922f)), owning_building_guid = 60)
      LocalObject(646, Locker.Constructor(Vector3(3923.053f, 3816.963f, 36.39922f)), owning_building_guid = 60)
      LocalObject(647, Locker.Constructor(Vector3(3923.088f, 3838.835f, 36.39922f)), owning_building_guid = 60)
      LocalObject(648, Locker.Constructor(Vector3(3925.741f, 3816.963f, 36.39922f)), owning_building_guid = 60)
      LocalObject(649, Locker.Constructor(Vector3(3925.741f, 3838.835f, 36.39922f)), owning_building_guid = 60)
      LocalObject(650, Locker.Constructor(Vector3(3927.143f, 3816.963f, 36.39922f)), owning_building_guid = 60)
      LocalObject(651, Locker.Constructor(Vector3(3927.143f, 3838.835f, 36.39922f)), owning_building_guid = 60)
      LocalObject(902, Terminal.Constructor(Vector3(3927.445f, 3822.129f, 37.73722f), order_terminal), owning_building_guid = 60)
      LocalObject(903, Terminal.Constructor(Vector3(3927.445f, 3827.853f, 37.73722f), order_terminal), owning_building_guid = 60)
      LocalObject(904, Terminal.Constructor(Vector3(3927.445f, 3833.234f, 37.73722f), order_terminal), owning_building_guid = 60)
      LocalObject(1027, SpawnTube.Constructor(Vector3(3916.706f, 3819.742f, 35.88722f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 60)
      LocalObject(1028, SpawnTube.Constructor(Vector3(3916.706f, 3836.152f, 35.88722f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 60)
      LocalObject(975, ProximityTerminal.Constructor(Vector3(3904.907f, 3826.725f, 73.97522f), pad_landing_tower_frame), owning_building_guid = 60)
      LocalObject(976, Terminal.Constructor(Vector3(3904.907f, 3826.725f, 73.97522f), air_rearm_terminal), owning_building_guid = 60)
      LocalObject(978, ProximityTerminal.Constructor(Vector3(3904.907f, 3837.17f, 73.97522f), pad_landing_tower_frame), owning_building_guid = 60)
      LocalObject(979, Terminal.Constructor(Vector3(3904.907f, 3837.17f, 73.97522f), air_rearm_terminal), owning_building_guid = 60)
      LocalObject(692, FacilityTurret.Constructor(Vector3(3891.07f, 3817.045f, 65.34722f), manned_turret), owning_building_guid = 60)
      TurretToWeapon(692, 5012)
      LocalObject(693, FacilityTurret.Constructor(Vector3(3929.497f, 3846.957f, 65.34722f), manned_turret), owning_building_guid = 60)
      TurretToWeapon(693, 5013)
      LocalObject(1002, Painbox.Constructor(Vector3(3910.454f, 3824.849f, 38.42472f), painbox_radius_continuous), owning_building_guid = 60)
      LocalObject(1003, Painbox.Constructor(Vector3(3922.923f, 3821.54f, 36.50523f), painbox_radius_continuous), owning_building_guid = 60)
      LocalObject(1004, Painbox.Constructor(Vector3(3923.113f, 3834.022f, 36.50523f), painbox_radius_continuous), owning_building_guid = 60)
    }

    Building62()

    def Building62(): Unit = { // Name: SE_Forseral_WG_tower Type: tower_c GUID: 61, MapID: 62
      LocalBuilding("SE_Forseral_WG_tower", 61, 62, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(5488f, 4168f, 35.9291f), tower_c)))
      LocalObject(1046, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 61)
      LocalObject(376, Door.Constructor(Vector3(5500f, 4160f, 37.4501f)), owning_building_guid = 61)
      LocalObject(377, Door.Constructor(Vector3(5500f, 4160f, 57.4491f)), owning_building_guid = 61)
      LocalObject(378, Door.Constructor(Vector3(5500f, 4176f, 37.4501f)), owning_building_guid = 61)
      LocalObject(379, Door.Constructor(Vector3(5500f, 4176f, 57.4491f)), owning_building_guid = 61)
      LocalObject(1065, Door.Constructor(Vector3(5499.146f, 4156.794f, 27.2651f)), owning_building_guid = 61)
      LocalObject(1066, Door.Constructor(Vector3(5499.146f, 4173.204f, 27.2651f)), owning_building_guid = 61)
      LocalObject(600, IFFLock.Constructor(Vector3(5497.957f, 4176.811f, 37.3901f), Vector3(0, 0, 0)), owning_building_guid = 61, door_guid = 378)
      LocalObject(601, IFFLock.Constructor(Vector3(5497.957f, 4176.811f, 57.39011f), Vector3(0, 0, 0)), owning_building_guid = 61, door_guid = 379)
      LocalObject(602, IFFLock.Constructor(Vector3(5502.047f, 4159.189f, 37.3901f), Vector3(0, 0, 180)), owning_building_guid = 61, door_guid = 376)
      LocalObject(603, IFFLock.Constructor(Vector3(5502.047f, 4159.189f, 57.39011f), Vector3(0, 0, 180)), owning_building_guid = 61, door_guid = 377)
      LocalObject(676, Locker.Constructor(Vector3(5503.716f, 4152.963f, 25.9231f)), owning_building_guid = 61)
      LocalObject(677, Locker.Constructor(Vector3(5503.751f, 4174.835f, 25.9231f)), owning_building_guid = 61)
      LocalObject(678, Locker.Constructor(Vector3(5505.053f, 4152.963f, 25.9231f)), owning_building_guid = 61)
      LocalObject(679, Locker.Constructor(Vector3(5505.088f, 4174.835f, 25.9231f)), owning_building_guid = 61)
      LocalObject(680, Locker.Constructor(Vector3(5507.741f, 4152.963f, 25.9231f)), owning_building_guid = 61)
      LocalObject(681, Locker.Constructor(Vector3(5507.741f, 4174.835f, 25.9231f)), owning_building_guid = 61)
      LocalObject(682, Locker.Constructor(Vector3(5509.143f, 4152.963f, 25.9231f)), owning_building_guid = 61)
      LocalObject(683, Locker.Constructor(Vector3(5509.143f, 4174.835f, 25.9231f)), owning_building_guid = 61)
      LocalObject(952, Terminal.Constructor(Vector3(5509.445f, 4158.129f, 27.2611f), order_terminal), owning_building_guid = 61)
      LocalObject(953, Terminal.Constructor(Vector3(5509.445f, 4163.853f, 27.2611f), order_terminal), owning_building_guid = 61)
      LocalObject(954, Terminal.Constructor(Vector3(5509.445f, 4169.234f, 27.2611f), order_terminal), owning_building_guid = 61)
      LocalObject(1035, SpawnTube.Constructor(Vector3(5498.706f, 4155.742f, 25.4111f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 61)
      LocalObject(1036, SpawnTube.Constructor(Vector3(5498.706f, 4172.152f, 25.4111f), respawn_tube_tower, Vector3(0, 0, 0)), owning_building_guid = 61)
      LocalObject(981, ProximityTerminal.Constructor(Vector3(5486.907f, 4162.725f, 63.4991f), pad_landing_tower_frame), owning_building_guid = 61)
      LocalObject(982, Terminal.Constructor(Vector3(5486.907f, 4162.725f, 63.4991f), air_rearm_terminal), owning_building_guid = 61)
      LocalObject(984, ProximityTerminal.Constructor(Vector3(5486.907f, 4173.17f, 63.4991f), pad_landing_tower_frame), owning_building_guid = 61)
      LocalObject(985, Terminal.Constructor(Vector3(5486.907f, 4173.17f, 63.4991f), air_rearm_terminal), owning_building_guid = 61)
      LocalObject(698, FacilityTurret.Constructor(Vector3(5473.07f, 4153.045f, 54.8711f), manned_turret), owning_building_guid = 61)
      TurretToWeapon(698, 5014)
      LocalObject(699, FacilityTurret.Constructor(Vector3(5511.497f, 4182.957f, 54.8711f), manned_turret), owning_building_guid = 61)
      TurretToWeapon(699, 5015)
      LocalObject(1014, Painbox.Constructor(Vector3(5492.454f, 4160.849f, 27.9486f), painbox_radius_continuous), owning_building_guid = 61)
      LocalObject(1015, Painbox.Constructor(Vector3(5504.923f, 4157.54f, 26.0291f), painbox_radius_continuous), owning_building_guid = 61)
      LocalObject(1016, Painbox.Constructor(Vector3(5505.113f, 4170.022f, 26.0291f), painbox_radius_continuous), owning_building_guid = 61)
    }

    Building14()

    def Building14(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 62, MapID: 14
      LocalBuilding("VT_building_tr", 62, 14, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2728f, 5288f, 35.99899f), VT_building_tr)))
      LocalObject(258, Door.Constructor(Vector3(2759.958f, 5246.54f, 38.07499f)), owning_building_guid = 62)
      LocalObject(259, Door.Constructor(Vector3(2764.546f, 5241.93f, 38.07499f)), owning_building_guid = 62)
      LocalObject(260, Door.Constructor(Vector3(2764.7f, 5251.282f, 38.07499f)), owning_building_guid = 62)
      LocalObject(261, Door.Constructor(Vector3(2769.288f, 5246.672f, 38.07499f)), owning_building_guid = 62)
      LocalObject(262, Door.Constructor(Vector3(2769.442f, 5256.024f, 38.07499f)), owning_building_guid = 62)
      LocalObject(263, Door.Constructor(Vector3(2774.03f, 5251.413f, 38.07499f)), owning_building_guid = 62)
      LocalObject(380, Door.Constructor(Vector3(2766.455f, 5232.55f, 37.70899f)), owning_building_guid = 62)
      LocalObject(381, Door.Constructor(Vector3(2783.464f, 5249.559f, 37.70899f)), owning_building_guid = 62)
      LocalObject(428, Door.Constructor(Vector3(2713.84f, 5251.231f, 39.17399f)), owning_building_guid = 62)
      LocalObject(429, Door.Constructor(Vector3(2728.005f, 5259.768f, 39.17399f)), owning_building_guid = 62)
      LocalObject(430, Door.Constructor(Vector3(2742.143f, 5251.248f, 39.17399f)), owning_building_guid = 62)
      LocalObject(431, Door.Constructor(Vector3(2756.232f, 5288.005f, 39.17399f)), owning_building_guid = 62)
      LocalObject(432, Door.Constructor(Vector3(2764.752f, 5302.143f, 39.17399f)), owning_building_guid = 62)
      LocalObject(433, Door.Constructor(Vector3(2764.769f, 5273.84f, 39.17399f)), owning_building_guid = 62)
      LocalObject(814, Terminal.Constructor(Vector3(2758.388f, 5253.812f, 37.35899f), order_terminal), owning_building_guid = 62)
      LocalObject(815, Terminal.Constructor(Vector3(2760.247f, 5255.671f, 37.35899f), order_terminal), owning_building_guid = 62)
      LocalObject(816, Terminal.Constructor(Vector3(2762.155f, 5257.58f, 37.35899f), order_terminal), owning_building_guid = 62)
      LocalObject(817, Terminal.Constructor(Vector3(2771.832f, 5240.335f, 37.35899f), order_terminal), owning_building_guid = 62)
      LocalObject(818, Terminal.Constructor(Vector3(2773.741f, 5242.243f, 37.35899f), order_terminal), owning_building_guid = 62)
      LocalObject(819, Terminal.Constructor(Vector3(2775.6f, 5244.102f, 37.35899f), order_terminal), owning_building_guid = 62)
      LocalObject(742, SpawnTube.Constructor(Vector3(2760.571f, 5245.914f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 315)), owning_building_guid = 62)
      LocalObject(743, SpawnTube.Constructor(Vector3(2763.933f, 5242.552f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 135)), owning_building_guid = 62)
      LocalObject(744, SpawnTube.Constructor(Vector3(2765.314f, 5250.658f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 315)), owning_building_guid = 62)
      LocalObject(745, SpawnTube.Constructor(Vector3(2768.677f, 5247.296f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 135)), owning_building_guid = 62)
      LocalObject(746, SpawnTube.Constructor(Vector3(2770.058f, 5255.401f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 315)), owning_building_guid = 62)
      LocalObject(747, SpawnTube.Constructor(Vector3(2773.42f, 5252.039f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 135)), owning_building_guid = 62)
    }

    Building43()

    def Building43(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 63, MapID: 43
      LocalBuilding("VT_building_tr", 63, 43, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2798f, 3038f, 34.92469f), VT_building_tr)))
      LocalObject(264, Door.Constructor(Vector3(2849.914f, 3031.281f, 37.00069f)), owning_building_guid = 63)
      LocalObject(265, Door.Constructor(Vector3(2849.914f, 3037.987f, 37.00069f)), owning_building_guid = 63)
      LocalObject(266, Door.Constructor(Vector3(2849.914f, 3044.693f, 37.00069f)), owning_building_guid = 63)
      LocalObject(267, Door.Constructor(Vector3(2856.418f, 3031.266f, 37.00069f)), owning_building_guid = 63)
      LocalObject(268, Door.Constructor(Vector3(2856.418f, 3037.972f, 37.00069f)), owning_building_guid = 63)
      LocalObject(269, Door.Constructor(Vector3(2856.419f, 3044.677f, 37.00069f)), owning_building_guid = 63)
      LocalObject(384, Door.Constructor(Vector3(2864.401f, 3025.983f, 36.63469f)), owning_building_guid = 63)
      LocalObject(385, Door.Constructor(Vector3(2864.401f, 3050.037f, 36.63469f)), owning_building_guid = 63)
      LocalObject(434, Door.Constructor(Vector3(2813.987f, 3001.988f, 38.09969f)), owning_building_guid = 63)
      LocalObject(435, Door.Constructor(Vector3(2813.987f, 3073.988f, 38.09969f)), owning_building_guid = 63)
      LocalObject(436, Door.Constructor(Vector3(2817.959f, 3057.967f, 38.09969f)), owning_building_guid = 63)
      LocalObject(437, Door.Constructor(Vector3(2817.967f, 3018.041f, 38.09969f)), owning_building_guid = 63)
      LocalObject(438, Door.Constructor(Vector3(2833.988f, 3022.013f, 38.09969f)), owning_building_guid = 63)
      LocalObject(439, Door.Constructor(Vector3(2834.012f, 3053.987f, 38.09969f)), owning_building_guid = 63)
      LocalObject(826, Terminal.Constructor(Vector3(2843.662f, 3035.313f, 36.28469f), order_terminal), owning_building_guid = 63)
      LocalObject(827, Terminal.Constructor(Vector3(2843.662f, 3037.942f, 36.28469f), order_terminal), owning_building_guid = 63)
      LocalObject(828, Terminal.Constructor(Vector3(2843.661f, 3040.641f, 36.28469f), order_terminal), owning_building_guid = 63)
      LocalObject(833, Terminal.Constructor(Vector3(2862.698f, 3035.29f, 36.28469f), order_terminal), owning_building_guid = 63)
      LocalObject(834, Terminal.Constructor(Vector3(2862.699f, 3037.989f, 36.28469f), order_terminal), owning_building_guid = 63)
      LocalObject(835, Terminal.Constructor(Vector3(2862.699f, 3040.618f, 36.28469f), order_terminal), owning_building_guid = 63)
      LocalObject(748, SpawnTube.Constructor(Vector3(2850.79f, 3037.98f, 36.95669f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 63)
      LocalObject(749, SpawnTube.Constructor(Vector3(2850.791f, 3031.272f, 36.95669f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 63)
      LocalObject(750, SpawnTube.Constructor(Vector3(2850.791f, 3044.688f, 36.95669f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 63)
      LocalObject(751, SpawnTube.Constructor(Vector3(2855.545f, 3031.272f, 36.95669f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 63)
      LocalObject(752, SpawnTube.Constructor(Vector3(2855.545f, 3037.981f, 36.95669f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 63)
      LocalObject(753, SpawnTube.Constructor(Vector3(2855.545f, 3044.688f, 36.95669f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 63)
    }

    Building13()

    def Building13(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 64, MapID: 13
      LocalBuilding("VT_building_tr", 64, 13, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2920f, 5052f, 35.99899f), VT_building_tr)))
      LocalObject(270, Door.Constructor(Vector3(2913.307f, 5103.915f, 38.07499f)), owning_building_guid = 64)
      LocalObject(271, Door.Constructor(Vector3(2913.323f, 5110.419f, 38.07499f)), owning_building_guid = 64)
      LocalObject(272, Door.Constructor(Vector3(2920.013f, 5103.915f, 38.07499f)), owning_building_guid = 64)
      LocalObject(273, Door.Constructor(Vector3(2920.028f, 5110.418f, 38.07499f)), owning_building_guid = 64)
      LocalObject(274, Door.Constructor(Vector3(2926.719f, 5103.915f, 38.07499f)), owning_building_guid = 64)
      LocalObject(275, Door.Constructor(Vector3(2926.734f, 5110.418f, 38.07499f)), owning_building_guid = 64)
      LocalObject(386, Door.Constructor(Vector3(2907.963f, 5118.401f, 37.70899f)), owning_building_guid = 64)
      LocalObject(389, Door.Constructor(Vector3(2932.017f, 5118.401f, 37.70899f)), owning_building_guid = 64)
      LocalObject(440, Door.Constructor(Vector3(2884.012f, 5067.987f, 39.17399f)), owning_building_guid = 64)
      LocalObject(441, Door.Constructor(Vector3(2900.033f, 5071.959f, 39.17399f)), owning_building_guid = 64)
      LocalObject(442, Door.Constructor(Vector3(2904.013f, 5088.012f, 39.17399f)), owning_building_guid = 64)
      LocalObject(443, Door.Constructor(Vector3(2935.987f, 5087.988f, 39.17399f)), owning_building_guid = 64)
      LocalObject(444, Door.Constructor(Vector3(2939.959f, 5071.967f, 39.17399f)), owning_building_guid = 64)
      LocalObject(445, Door.Constructor(Vector3(2956.012f, 5067.987f, 39.17399f)), owning_building_guid = 64)
      LocalObject(842, Terminal.Constructor(Vector3(2917.359f, 5097.662f, 37.35899f), order_terminal), owning_building_guid = 64)
      LocalObject(843, Terminal.Constructor(Vector3(2917.382f, 5116.699f, 37.35899f), order_terminal), owning_building_guid = 64)
      LocalObject(844, Terminal.Constructor(Vector3(2920.011f, 5116.699f, 37.35899f), order_terminal), owning_building_guid = 64)
      LocalObject(845, Terminal.Constructor(Vector3(2920.058f, 5097.662f, 37.35899f), order_terminal), owning_building_guid = 64)
      LocalObject(846, Terminal.Constructor(Vector3(2922.687f, 5097.662f, 37.35899f), order_terminal), owning_building_guid = 64)
      LocalObject(847, Terminal.Constructor(Vector3(2922.71f, 5116.698f, 37.35899f), order_terminal), owning_building_guid = 64)
      LocalObject(754, SpawnTube.Constructor(Vector3(2913.312f, 5104.791f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 180)), owning_building_guid = 64)
      LocalObject(755, SpawnTube.Constructor(Vector3(2913.312f, 5109.545f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 0)), owning_building_guid = 64)
      LocalObject(756, SpawnTube.Constructor(Vector3(2920.02f, 5104.79f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 180)), owning_building_guid = 64)
      LocalObject(757, SpawnTube.Constructor(Vector3(2920.019f, 5109.545f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 0)), owning_building_guid = 64)
      LocalObject(758, SpawnTube.Constructor(Vector3(2926.728f, 5104.791f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 180)), owning_building_guid = 64)
      LocalObject(759, SpawnTube.Constructor(Vector3(2926.728f, 5109.545f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 0)), owning_building_guid = 64)
    }

    Building15()

    def Building15(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 65, MapID: 15
      LocalBuilding("VT_building_tr", 65, 15, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3112f, 5286f, 35.99899f), VT_building_tr)))
      LocalObject(276, Door.Constructor(Vector3(3065.93f, 5249.454f, 38.07499f)), owning_building_guid = 65)
      LocalObject(277, Door.Constructor(Vector3(3070.54f, 5254.042f, 38.07499f)), owning_building_guid = 65)
      LocalObject(278, Door.Constructor(Vector3(3070.672f, 5244.712f, 38.07499f)), owning_building_guid = 65)
      LocalObject(279, Door.Constructor(Vector3(3075.282f, 5249.3f, 38.07499f)), owning_building_guid = 65)
      LocalObject(280, Door.Constructor(Vector3(3075.413f, 5239.97f, 38.07499f)), owning_building_guid = 65)
      LocalObject(281, Door.Constructor(Vector3(3080.024f, 5244.558f, 38.07499f)), owning_building_guid = 65)
      LocalObject(392, Door.Constructor(Vector3(3056.55f, 5247.545f, 37.70899f)), owning_building_guid = 65)
      LocalObject(393, Door.Constructor(Vector3(3073.559f, 5230.536f, 37.70899f)), owning_building_guid = 65)
      LocalObject(446, Door.Constructor(Vector3(3075.231f, 5300.16f, 39.17399f)), owning_building_guid = 65)
      LocalObject(447, Door.Constructor(Vector3(3075.248f, 5271.857f, 39.17399f)), owning_building_guid = 65)
      LocalObject(448, Door.Constructor(Vector3(3083.768f, 5285.995f, 39.17399f)), owning_building_guid = 65)
      LocalObject(449, Door.Constructor(Vector3(3097.84f, 5249.231f, 39.17399f)), owning_building_guid = 65)
      LocalObject(450, Door.Constructor(Vector3(3112.005f, 5257.768f, 39.17399f)), owning_building_guid = 65)
      LocalObject(451, Door.Constructor(Vector3(3126.143f, 5249.248f, 39.17399f)), owning_building_guid = 65)
      LocalObject(874, Terminal.Constructor(Vector3(3064.335f, 5242.168f, 37.35899f), order_terminal), owning_building_guid = 65)
      LocalObject(875, Terminal.Constructor(Vector3(3066.243f, 5240.259f, 37.35899f), order_terminal), owning_building_guid = 65)
      LocalObject(876, Terminal.Constructor(Vector3(3068.102f, 5238.4f, 37.35899f), order_terminal), owning_building_guid = 65)
      LocalObject(877, Terminal.Constructor(Vector3(3077.812f, 5255.612f, 37.35899f), order_terminal), owning_building_guid = 65)
      LocalObject(880, Terminal.Constructor(Vector3(3079.671f, 5253.753f, 37.35899f), order_terminal), owning_building_guid = 65)
      LocalObject(881, Terminal.Constructor(Vector3(3081.58f, 5251.845f, 37.35899f), order_terminal), owning_building_guid = 65)
      LocalObject(760, SpawnTube.Constructor(Vector3(3066.552f, 5250.067f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 225)), owning_building_guid = 65)
      LocalObject(761, SpawnTube.Constructor(Vector3(3069.914f, 5253.429f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 45)), owning_building_guid = 65)
      LocalObject(762, SpawnTube.Constructor(Vector3(3071.296f, 5245.323f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 225)), owning_building_guid = 65)
      LocalObject(763, SpawnTube.Constructor(Vector3(3074.658f, 5248.686f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 45)), owning_building_guid = 65)
      LocalObject(764, SpawnTube.Constructor(Vector3(3076.039f, 5240.58f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 225)), owning_building_guid = 65)
      LocalObject(765, SpawnTube.Constructor(Vector3(3079.401f, 5243.942f, 38.03099f), respawn_tube_sanctuary, Vector3(0, 0, 45)), owning_building_guid = 65)
    }

    Building42()

    def Building42(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 66, MapID: 42
      LocalBuilding("VT_building_tr", 66, 42, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3164f, 3102f, 34.9224f), VT_building_tr)))
      LocalObject(288, Door.Constructor(Vector3(3157.266f, 3043.582f, 36.9984f)), owning_building_guid = 66)
      LocalObject(289, Door.Constructor(Vector3(3157.281f, 3050.086f, 36.9984f)), owning_building_guid = 66)
      LocalObject(290, Door.Constructor(Vector3(3163.972f, 3043.582f, 36.9984f)), owning_building_guid = 66)
      LocalObject(291, Door.Constructor(Vector3(3163.987f, 3050.086f, 36.9984f)), owning_building_guid = 66)
      LocalObject(292, Door.Constructor(Vector3(3170.677f, 3043.581f, 36.9984f)), owning_building_guid = 66)
      LocalObject(293, Door.Constructor(Vector3(3170.693f, 3050.086f, 36.9984f)), owning_building_guid = 66)
      LocalObject(398, Door.Constructor(Vector3(3151.983f, 3035.599f, 36.6324f)), owning_building_guid = 66)
      LocalObject(399, Door.Constructor(Vector3(3176.037f, 3035.599f, 36.6324f)), owning_building_guid = 66)
      LocalObject(452, Door.Constructor(Vector3(3127.988f, 3086.013f, 38.0974f)), owning_building_guid = 66)
      LocalObject(453, Door.Constructor(Vector3(3144.041f, 3082.033f, 38.0974f)), owning_building_guid = 66)
      LocalObject(454, Door.Constructor(Vector3(3148.013f, 3066.012f, 38.0974f)), owning_building_guid = 66)
      LocalObject(459, Door.Constructor(Vector3(3179.987f, 3065.988f, 38.0974f)), owning_building_guid = 66)
      LocalObject(462, Door.Constructor(Vector3(3183.967f, 3082.041f, 38.0974f)), owning_building_guid = 66)
      LocalObject(463, Door.Constructor(Vector3(3199.988f, 3086.013f, 38.0974f)), owning_building_guid = 66)
      LocalObject(890, Terminal.Constructor(Vector3(3161.29f, 3037.302f, 36.2824f), order_terminal), owning_building_guid = 66)
      LocalObject(891, Terminal.Constructor(Vector3(3161.313f, 3056.338f, 36.2824f), order_terminal), owning_building_guid = 66)
      LocalObject(892, Terminal.Constructor(Vector3(3163.942f, 3056.338f, 36.2824f), order_terminal), owning_building_guid = 66)
      LocalObject(893, Terminal.Constructor(Vector3(3163.989f, 3037.301f, 36.2824f), order_terminal), owning_building_guid = 66)
      LocalObject(894, Terminal.Constructor(Vector3(3166.618f, 3037.301f, 36.2824f), order_terminal), owning_building_guid = 66)
      LocalObject(895, Terminal.Constructor(Vector3(3166.641f, 3056.339f, 36.2824f), order_terminal), owning_building_guid = 66)
      LocalObject(772, SpawnTube.Constructor(Vector3(3157.272f, 3044.455f, 36.9544f), respawn_tube_sanctuary, Vector3(0, 0, 180)), owning_building_guid = 66)
      LocalObject(773, SpawnTube.Constructor(Vector3(3157.272f, 3049.209f, 36.9544f), respawn_tube_sanctuary, Vector3(0, 0, 0)), owning_building_guid = 66)
      LocalObject(774, SpawnTube.Constructor(Vector3(3163.981f, 3044.455f, 36.9544f), respawn_tube_sanctuary, Vector3(0, 0, 180)), owning_building_guid = 66)
      LocalObject(775, SpawnTube.Constructor(Vector3(3163.98f, 3049.21f, 36.9544f), respawn_tube_sanctuary, Vector3(0, 0, 0)), owning_building_guid = 66)
      LocalObject(776, SpawnTube.Constructor(Vector3(3170.688f, 3044.455f, 36.9544f), respawn_tube_sanctuary, Vector3(0, 0, 180)), owning_building_guid = 66)
      LocalObject(777, SpawnTube.Constructor(Vector3(3170.688f, 3049.209f, 36.9544f), respawn_tube_sanctuary, Vector3(0, 0, 0)), owning_building_guid = 66)
    }

    Building41()

    def Building41(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 67, MapID: 41
      LocalBuilding("VT_building_tr", 67, 41, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3196f, 2882f, 34.92251f), VT_building_tr)))
      LocalObject(282, Door.Constructor(Vector3(3137.581f, 2875.323f, 36.99851f)), owning_building_guid = 67)
      LocalObject(283, Door.Constructor(Vector3(3137.582f, 2882.028f, 36.99851f)), owning_building_guid = 67)
      LocalObject(284, Door.Constructor(Vector3(3137.582f, 2888.734f, 36.99851f)), owning_building_guid = 67)
      LocalObject(285, Door.Constructor(Vector3(3144.086f, 2875.307f, 36.99851f)), owning_building_guid = 67)
      LocalObject(286, Door.Constructor(Vector3(3144.086f, 2882.013f, 36.99851f)), owning_building_guid = 67)
      LocalObject(287, Door.Constructor(Vector3(3144.086f, 2888.719f, 36.99851f)), owning_building_guid = 67)
      LocalObject(396, Door.Constructor(Vector3(3129.599f, 2869.963f, 36.63251f)), owning_building_guid = 67)
      LocalObject(397, Door.Constructor(Vector3(3129.599f, 2894.017f, 36.63251f)), owning_building_guid = 67)
      LocalObject(455, Door.Constructor(Vector3(3159.988f, 2866.013f, 38.09751f)), owning_building_guid = 67)
      LocalObject(456, Door.Constructor(Vector3(3160.012f, 2897.987f, 38.09751f)), owning_building_guid = 67)
      LocalObject(457, Door.Constructor(Vector3(3176.033f, 2901.959f, 38.09751f)), owning_building_guid = 67)
      LocalObject(458, Door.Constructor(Vector3(3176.041f, 2862.033f, 38.09751f)), owning_building_guid = 67)
      LocalObject(460, Door.Constructor(Vector3(3180.013f, 2846.012f, 38.09751f)), owning_building_guid = 67)
      LocalObject(461, Door.Constructor(Vector3(3180.013f, 2918.012f, 38.09751f)), owning_building_guid = 67)
      LocalObject(884, Terminal.Constructor(Vector3(3131.301f, 2879.382f, 36.28251f), order_terminal), owning_building_guid = 67)
      LocalObject(885, Terminal.Constructor(Vector3(3131.301f, 2882.011f, 36.28251f), order_terminal), owning_building_guid = 67)
      LocalObject(886, Terminal.Constructor(Vector3(3131.302f, 2884.71f, 36.28251f), order_terminal), owning_building_guid = 67)
      LocalObject(887, Terminal.Constructor(Vector3(3150.339f, 2879.359f, 36.28251f), order_terminal), owning_building_guid = 67)
      LocalObject(888, Terminal.Constructor(Vector3(3150.338f, 2882.058f, 36.28251f), order_terminal), owning_building_guid = 67)
      LocalObject(889, Terminal.Constructor(Vector3(3150.338f, 2884.687f, 36.28251f), order_terminal), owning_building_guid = 67)
      LocalObject(766, SpawnTube.Constructor(Vector3(3138.455f, 2875.312f, 36.95451f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 67)
      LocalObject(767, SpawnTube.Constructor(Vector3(3138.455f, 2882.019f, 36.95451f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 67)
      LocalObject(768, SpawnTube.Constructor(Vector3(3138.455f, 2888.728f, 36.95451f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 67)
      LocalObject(769, SpawnTube.Constructor(Vector3(3143.209f, 2875.312f, 36.95451f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 67)
      LocalObject(770, SpawnTube.Constructor(Vector3(3143.209f, 2888.728f, 36.95451f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 67)
      LocalObject(771, SpawnTube.Constructor(Vector3(3143.21f, 2882.02f, 36.95451f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 67)
    }

    Building25()

    def Building25(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 68, MapID: 25
      LocalBuilding("VT_building_tr", 68, 25, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5026f, 3908f, 35.9291f), VT_building_tr)))
      LocalObject(294, Door.Constructor(Vector3(5077.915f, 3901.281f, 38.0051f)), owning_building_guid = 68)
      LocalObject(295, Door.Constructor(Vector3(5077.915f, 3907.987f, 38.0051f)), owning_building_guid = 68)
      LocalObject(296, Door.Constructor(Vector3(5077.915f, 3914.693f, 38.0051f)), owning_building_guid = 68)
      LocalObject(297, Door.Constructor(Vector3(5084.418f, 3901.266f, 38.0051f)), owning_building_guid = 68)
      LocalObject(298, Door.Constructor(Vector3(5084.418f, 3907.972f, 38.0051f)), owning_building_guid = 68)
      LocalObject(299, Door.Constructor(Vector3(5084.419f, 3914.677f, 38.0051f)), owning_building_guid = 68)
      LocalObject(400, Door.Constructor(Vector3(5092.401f, 3895.983f, 37.6391f)), owning_building_guid = 68)
      LocalObject(401, Door.Constructor(Vector3(5092.401f, 3920.037f, 37.6391f)), owning_building_guid = 68)
      LocalObject(464, Door.Constructor(Vector3(5041.987f, 3871.988f, 39.1041f)), owning_building_guid = 68)
      LocalObject(465, Door.Constructor(Vector3(5041.987f, 3943.988f, 39.1041f)), owning_building_guid = 68)
      LocalObject(466, Door.Constructor(Vector3(5045.959f, 3927.967f, 39.1041f)), owning_building_guid = 68)
      LocalObject(467, Door.Constructor(Vector3(5045.967f, 3888.041f, 39.1041f)), owning_building_guid = 68)
      LocalObject(468, Door.Constructor(Vector3(5061.988f, 3892.013f, 39.1041f)), owning_building_guid = 68)
      LocalObject(469, Door.Constructor(Vector3(5062.012f, 3923.987f, 39.1041f)), owning_building_guid = 68)
      LocalObject(914, Terminal.Constructor(Vector3(5071.662f, 3905.313f, 37.2891f), order_terminal), owning_building_guid = 68)
      LocalObject(915, Terminal.Constructor(Vector3(5071.662f, 3907.942f, 37.2891f), order_terminal), owning_building_guid = 68)
      LocalObject(916, Terminal.Constructor(Vector3(5071.662f, 3910.641f, 37.2891f), order_terminal), owning_building_guid = 68)
      LocalObject(917, Terminal.Constructor(Vector3(5090.698f, 3905.29f, 37.2891f), order_terminal), owning_building_guid = 68)
      LocalObject(918, Terminal.Constructor(Vector3(5090.699f, 3907.989f, 37.2891f), order_terminal), owning_building_guid = 68)
      LocalObject(919, Terminal.Constructor(Vector3(5090.699f, 3910.618f, 37.2891f), order_terminal), owning_building_guid = 68)
      LocalObject(778, SpawnTube.Constructor(Vector3(5078.79f, 3907.98f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 68)
      LocalObject(779, SpawnTube.Constructor(Vector3(5078.791f, 3901.272f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 68)
      LocalObject(780, SpawnTube.Constructor(Vector3(5078.791f, 3914.688f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 68)
      LocalObject(781, SpawnTube.Constructor(Vector3(5083.545f, 3901.272f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 68)
      LocalObject(782, SpawnTube.Constructor(Vector3(5083.545f, 3907.981f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 68)
      LocalObject(783, SpawnTube.Constructor(Vector3(5083.545f, 3914.688f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 68)
    }

    Building23()

    def Building23(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 69, MapID: 23
      LocalBuilding("VT_building_tr", 69, 23, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5230f, 3732f, 35.9291f), VT_building_tr)))
      LocalObject(300, Door.Constructor(Vector3(5223.307f, 3783.914f, 38.0051f)), owning_building_guid = 69)
      LocalObject(301, Door.Constructor(Vector3(5223.323f, 3790.419f, 38.0051f)), owning_building_guid = 69)
      LocalObject(302, Door.Constructor(Vector3(5230.013f, 3783.914f, 38.0051f)), owning_building_guid = 69)
      LocalObject(303, Door.Constructor(Vector3(5230.028f, 3790.418f, 38.0051f)), owning_building_guid = 69)
      LocalObject(304, Door.Constructor(Vector3(5236.719f, 3783.914f, 38.0051f)), owning_building_guid = 69)
      LocalObject(305, Door.Constructor(Vector3(5236.734f, 3790.418f, 38.0051f)), owning_building_guid = 69)
      LocalObject(404, Door.Constructor(Vector3(5217.963f, 3798.401f, 37.6391f)), owning_building_guid = 69)
      LocalObject(405, Door.Constructor(Vector3(5242.017f, 3798.401f, 37.6391f)), owning_building_guid = 69)
      LocalObject(470, Door.Constructor(Vector3(5194.012f, 3747.987f, 39.1041f)), owning_building_guid = 69)
      LocalObject(471, Door.Constructor(Vector3(5210.033f, 3751.959f, 39.1041f)), owning_building_guid = 69)
      LocalObject(472, Door.Constructor(Vector3(5214.013f, 3768.012f, 39.1041f)), owning_building_guid = 69)
      LocalObject(473, Door.Constructor(Vector3(5245.987f, 3767.988f, 39.1041f)), owning_building_guid = 69)
      LocalObject(474, Door.Constructor(Vector3(5249.959f, 3751.967f, 39.1041f)), owning_building_guid = 69)
      LocalObject(475, Door.Constructor(Vector3(5266.012f, 3747.987f, 39.1041f)), owning_building_guid = 69)
      LocalObject(930, Terminal.Constructor(Vector3(5227.359f, 3777.661f, 37.2891f), order_terminal), owning_building_guid = 69)
      LocalObject(931, Terminal.Constructor(Vector3(5227.382f, 3796.699f, 37.2891f), order_terminal), owning_building_guid = 69)
      LocalObject(932, Terminal.Constructor(Vector3(5230.011f, 3796.699f, 37.2891f), order_terminal), owning_building_guid = 69)
      LocalObject(933, Terminal.Constructor(Vector3(5230.058f, 3777.662f, 37.2891f), order_terminal), owning_building_guid = 69)
      LocalObject(934, Terminal.Constructor(Vector3(5232.687f, 3777.662f, 37.2891f), order_terminal), owning_building_guid = 69)
      LocalObject(935, Terminal.Constructor(Vector3(5232.71f, 3796.698f, 37.2891f), order_terminal), owning_building_guid = 69)
      LocalObject(784, SpawnTube.Constructor(Vector3(5223.312f, 3784.791f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 180)), owning_building_guid = 69)
      LocalObject(785, SpawnTube.Constructor(Vector3(5223.312f, 3789.545f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 0)), owning_building_guid = 69)
      LocalObject(786, SpawnTube.Constructor(Vector3(5230.019f, 3789.545f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 0)), owning_building_guid = 69)
      LocalObject(787, SpawnTube.Constructor(Vector3(5230.02f, 3784.79f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 180)), owning_building_guid = 69)
      LocalObject(788, SpawnTube.Constructor(Vector3(5236.728f, 3784.791f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 180)), owning_building_guid = 69)
      LocalObject(789, SpawnTube.Constructor(Vector3(5236.728f, 3789.545f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 0)), owning_building_guid = 69)
    }

    Building24()

    def Building24(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 70, MapID: 24
      LocalBuilding("VT_building_tr", 70, 24, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5440f, 3906f, 35.9291f), VT_building_tr)))
      LocalObject(306, Door.Constructor(Vector3(5381.581f, 3899.323f, 38.0051f)), owning_building_guid = 70)
      LocalObject(307, Door.Constructor(Vector3(5381.582f, 3906.028f, 38.0051f)), owning_building_guid = 70)
      LocalObject(308, Door.Constructor(Vector3(5381.582f, 3912.734f, 38.0051f)), owning_building_guid = 70)
      LocalObject(309, Door.Constructor(Vector3(5388.085f, 3899.307f, 38.0051f)), owning_building_guid = 70)
      LocalObject(310, Door.Constructor(Vector3(5388.085f, 3906.013f, 38.0051f)), owning_building_guid = 70)
      LocalObject(311, Door.Constructor(Vector3(5388.085f, 3912.719f, 38.0051f)), owning_building_guid = 70)
      LocalObject(408, Door.Constructor(Vector3(5373.599f, 3893.963f, 37.6391f)), owning_building_guid = 70)
      LocalObject(409, Door.Constructor(Vector3(5373.599f, 3918.017f, 37.6391f)), owning_building_guid = 70)
      LocalObject(476, Door.Constructor(Vector3(5403.988f, 3890.013f, 39.1041f)), owning_building_guid = 70)
      LocalObject(477, Door.Constructor(Vector3(5404.012f, 3921.987f, 39.1041f)), owning_building_guid = 70)
      LocalObject(478, Door.Constructor(Vector3(5420.033f, 3925.959f, 39.1041f)), owning_building_guid = 70)
      LocalObject(479, Door.Constructor(Vector3(5420.041f, 3886.033f, 39.1041f)), owning_building_guid = 70)
      LocalObject(480, Door.Constructor(Vector3(5424.013f, 3870.012f, 39.1041f)), owning_building_guid = 70)
      LocalObject(481, Door.Constructor(Vector3(5424.013f, 3942.012f, 39.1041f)), owning_building_guid = 70)
      LocalObject(946, Terminal.Constructor(Vector3(5375.301f, 3903.382f, 37.2891f), order_terminal), owning_building_guid = 70)
      LocalObject(947, Terminal.Constructor(Vector3(5375.301f, 3906.011f, 37.2891f), order_terminal), owning_building_guid = 70)
      LocalObject(948, Terminal.Constructor(Vector3(5375.302f, 3908.71f, 37.2891f), order_terminal), owning_building_guid = 70)
      LocalObject(949, Terminal.Constructor(Vector3(5394.338f, 3903.359f, 37.2891f), order_terminal), owning_building_guid = 70)
      LocalObject(950, Terminal.Constructor(Vector3(5394.338f, 3906.058f, 37.2891f), order_terminal), owning_building_guid = 70)
      LocalObject(951, Terminal.Constructor(Vector3(5394.338f, 3908.687f, 37.2891f), order_terminal), owning_building_guid = 70)
      LocalObject(790, SpawnTube.Constructor(Vector3(5382.455f, 3899.312f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 70)
      LocalObject(791, SpawnTube.Constructor(Vector3(5382.455f, 3906.019f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 70)
      LocalObject(792, SpawnTube.Constructor(Vector3(5382.455f, 3912.728f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 270)), owning_building_guid = 70)
      LocalObject(793, SpawnTube.Constructor(Vector3(5387.209f, 3899.312f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 70)
      LocalObject(794, SpawnTube.Constructor(Vector3(5387.209f, 3912.728f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 70)
      LocalObject(795, SpawnTube.Constructor(Vector3(5387.21f, 3906.02f, 37.96111f), respawn_tube_sanctuary, Vector3(0, 0, 90)), owning_building_guid = 70)
    }

    Building8()

    def Building8(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 71, MapID: 8
      LocalBuilding("vt_dropship", 71, 8, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2754f, 5088f, 35.99899f), vt_dropship)))
      LocalObject(324, Terminal.Constructor(Vector3(2770.8f, 5104.39f, 38.86699f), dropship_vehicle_terminal), owning_building_guid = 71)
      LocalObject(312, VehicleSpawnPad.Constructor(Vector3(2756.568f, 5090.507f, 32.01399f), dropship_pad_doors, Vector3(0, 0, 136)), owning_building_guid = 71, terminal_guid = 324)
    }

    Building5()

    def Building5(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 72, MapID: 5
      LocalBuilding("vt_dropship", 72, 5, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2780f, 5414f, 35.99899f), vt_dropship)))
      LocalObject(325, Terminal.Constructor(Vector3(2796.39f, 5397.201f, 38.86699f), dropship_vehicle_terminal), owning_building_guid = 72)
      LocalObject(313, VehicleSpawnPad.Constructor(Vector3(2782.508f, 5411.432f, 32.01399f), dropship_pad_doors, Vector3(0, 0, 226)), owning_building_guid = 72, terminal_guid = 325)
    }

    Building44()

    def Building44(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 73, MapID: 44
      LocalBuilding("vt_dropship", 73, 44, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2818f, 2864f, 34.92273f), vt_dropship)))
      LocalObject(326, Terminal.Constructor(Vector3(2841.47f, 2864.12f, 37.79073f), dropship_vehicle_terminal), owning_building_guid = 73)
      LocalObject(314, VehicleSpawnPad.Constructor(Vector3(2821.589f, 2864.02f, 30.93773f), dropship_pad_doors, Vector3(0, 0, 180)), owning_building_guid = 73, terminal_guid = 326)
    }

    Building48()

    def Building48(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 74, MapID: 48
      LocalBuilding("vt_dropship", 74, 48, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2848f, 3148f, 34.92273f), vt_dropship)))
      LocalObject(327, Terminal.Constructor(Vector3(2848.12f, 3124.53f, 37.79073f), dropship_vehicle_terminal), owning_building_guid = 74)
      LocalObject(315, VehicleSpawnPad.Constructor(Vector3(2848.02f, 3144.411f, 30.93773f), dropship_pad_doors, Vector3(0, 0, -90)), owning_building_guid = 74, terminal_guid = 327)
    }

    Building49()

    def Building49(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 75, MapID: 49
      LocalBuilding("vt_dropship", 75, 49, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3050f, 3186f, 34.91574f), vt_dropship)))
      LocalObject(329, Terminal.Constructor(Vector3(3050.12f, 3162.53f, 37.78374f), dropship_vehicle_terminal), owning_building_guid = 75)
      LocalObject(316, VehicleSpawnPad.Constructor(Vector3(3050.02f, 3182.411f, 30.93074f), dropship_pad_doors, Vector3(0, 0, -90)), owning_building_guid = 75, terminal_guid = 329)
    }

    Building6()

    def Building6(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 76, MapID: 6
      LocalBuilding("vt_dropship", 76, 6, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3062f, 5416f, 35.99899f), vt_dropship)))
      LocalObject(328, Terminal.Constructor(Vector3(3045.2f, 5399.61f, 38.86699f), dropship_vehicle_terminal), owning_building_guid = 76)
      LocalObject(317, VehicleSpawnPad.Constructor(Vector3(3059.432f, 5413.493f, 32.01399f), dropship_pad_doors, Vector3(0, 0, -44)), owning_building_guid = 76, terminal_guid = 328)
    }

    Building46()

    def Building46(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 77, MapID: 46
      LocalBuilding("vt_dropship", 77, 46, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3088f, 2784f, 34.92098f), vt_dropship)))
      LocalObject(331, Terminal.Constructor(Vector3(3087.88f, 2807.47f, 37.78898f), dropship_vehicle_terminal), owning_building_guid = 77)
      LocalObject(319, VehicleSpawnPad.Constructor(Vector3(3087.98f, 2787.589f, 30.93598f), dropship_pad_doors, Vector3(0, 0, 90)), owning_building_guid = 77, terminal_guid = 331)
    }

    Building7()

    def Building7(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 78, MapID: 7
      LocalBuilding("vt_dropship", 78, 7, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3090f, 5088f, 35.99899f), vt_dropship)))
      LocalObject(330, Terminal.Constructor(Vector3(3073.61f, 5104.799f, 38.86699f), dropship_vehicle_terminal), owning_building_guid = 78)
      LocalObject(318, VehicleSpawnPad.Constructor(Vector3(3087.492f, 5090.568f, 32.01399f), dropship_pad_doors, Vector3(0, 0, 46)), owning_building_guid = 78, terminal_guid = 330)
    }

    Building26()

    def Building26(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 79, MapID: 26
      LocalBuilding("vt_dropship", 79, 26, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5050f, 3804f, 35.9291f), vt_dropship)))
      LocalObject(333, Terminal.Constructor(Vector3(5073.47f, 3804.12f, 38.7971f), dropship_vehicle_terminal), owning_building_guid = 79)
      LocalObject(321, VehicleSpawnPad.Constructor(Vector3(5053.589f, 3804.02f, 31.9441f), dropship_pad_doors, Vector3(0, 0, 180)), owning_building_guid = 79, terminal_guid = 333)
    }

    Building30()

    def Building30(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 80, MapID: 30
      LocalBuilding("vt_dropship", 80, 30, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5050f, 4014f, 35.9291f), vt_dropship)))
      LocalObject(332, Terminal.Constructor(Vector3(5050.12f, 3990.53f, 38.7971f), dropship_vehicle_terminal), owning_building_guid = 80)
      LocalObject(320, VehicleSpawnPad.Constructor(Vector3(5050.02f, 4010.411f, 31.9441f), dropship_pad_doors, Vector3(0, 0, -90)), owning_building_guid = 80, terminal_guid = 332)
    }

    Building31()

    def Building31(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 81, MapID: 31
      LocalBuilding("vt_dropship", 81, 31, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5414f, 4010f, 35.9291f), vt_dropship)))
      LocalObject(335, Terminal.Constructor(Vector3(5414.12f, 3986.53f, 38.7971f), dropship_vehicle_terminal), owning_building_guid = 81)
      LocalObject(323, VehicleSpawnPad.Constructor(Vector3(5414.02f, 4006.411f, 31.9441f), dropship_pad_doors, Vector3(0, 0, -90)), owning_building_guid = 81, terminal_guid = 335)
    }

    Building28()

    def Building28(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 82, MapID: 28
      LocalBuilding("vt_dropship", 82, 28, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5416f, 3812f, 35.9291f), vt_dropship)))
      LocalObject(334, Terminal.Constructor(Vector3(5392.53f, 3811.88f, 38.7971f), dropship_vehicle_terminal), owning_building_guid = 82)
      LocalObject(322, VehicleSpawnPad.Constructor(Vector3(5412.411f, 3811.98f, 31.9441f), dropship_pad_doors, Vector3(0, 0, 0)), owning_building_guid = 82, terminal_guid = 334)
    }

    Building17()

    def Building17(): Unit = { // Name: TR_NW_Tport_03 Type: vt_spawn GUID: 83, MapID: 17
      LocalBuilding("TR_NW_Tport_03", 83, 17, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2842f, 5166f, 35.99899f), vt_spawn)))
    }

    Building19()

    def Building19(): Unit = { // Name: TR_NW_Tport_01 Type: vt_spawn GUID: 84, MapID: 19
      LocalBuilding("TR_NW_Tport_01", 84, 19, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2852f, 5312f, 35.99899f), vt_spawn)))
    }

    Building54()

    def Building54(): Unit = { // Name: TR_SW_Tport_03 Type: vt_spawn GUID: 85, MapID: 54
      LocalBuilding("TR_SW_Tport_03", 85, 54, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2928f, 2910f, 34.92174f), vt_spawn)))
    }

    Building56()

    def Building56(): Unit = { // Name: TR_SW_Tport_01 Type: vt_spawn GUID: 86, MapID: 56
      LocalBuilding("TR_SW_Tport_01", 86, 56, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2938f, 3056f, 34.91694f), vt_spawn)))
    }

    Building16()

    def Building16(): Unit = { // Name: TR_NW_Tport_02 Type: vt_spawn GUID: 87, MapID: 16
      LocalBuilding("TR_NW_Tport_02", 87, 16, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2990f, 5312f, 35.99899f), vt_spawn)))
    }

    Building18()

    def Building18(): Unit = { // Name: TR_NW_Tport_04 Type: vt_spawn GUID: 88, MapID: 18
      LocalBuilding("TR_NW_Tport_04", 88, 18, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2998f, 5166f, 35.99899f), vt_spawn)))
    }

    Building57()

    def Building57(): Unit = { // Name: TR_SW_Tport_02 Type: vt_spawn GUID: 89, MapID: 57
      LocalBuilding("TR_SW_Tport_02", 89, 57, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3076f, 3056f, 34.91595f), vt_spawn)))
    }

    Building55()

    def Building55(): Unit = { // Name: TR_SW_Tport_04 Type: vt_spawn GUID: 90, MapID: 55
      LocalBuilding("TR_SW_Tport_04", 90, 55, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3084f, 2910f, 34.91639f), vt_spawn)))
    }

    Building36()

    def Building36(): Unit = { // Name: TR_E_Tport_03 Type: vt_spawn GUID: 91, MapID: 36
      LocalBuilding("TR_E_Tport_03", 91, 36, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5154f, 3834f, 35.9291f), vt_spawn)))
    }

    Building38()

    def Building38(): Unit = { // Name: TR_E_Tport_01 Type: vt_spawn GUID: 92, MapID: 38
      LocalBuilding("TR_E_Tport_01", 92, 38, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5164f, 3982f, 35.9291f), vt_spawn)))
    }

    Building39()

    def Building39(): Unit = { // Name: TR_E_Tport_02 Type: vt_spawn GUID: 93, MapID: 39
      LocalBuilding("TR_E_Tport_02", 93, 39, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5302f, 3982f, 35.9291f), vt_spawn)))
    }

    Building37()

    def Building37(): Unit = { // Name: TR_E_Tport_04 Type: vt_spawn GUID: 94, MapID: 37
      LocalBuilding("TR_E_Tport_04", 94, 37, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5310f, 3834f, 35.9291f), vt_spawn)))
    }

    Building11()

    def Building11(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 95, MapID: 11
      LocalBuilding("vt_vehicle", 95, 11, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2780f, 5160f, 35.99899f), vt_vehicle)))
      LocalObject(1079, Terminal.Constructor(Vector3(2790.242f, 5170.253f, 38.68599f), ground_vehicle_terminal), owning_building_guid = 95)
      LocalObject(724, VehicleSpawnPad.Constructor(Vector3(2779.915f, 5159.877f, 34.52799f), mb_pad_creation, Vector3(0, 0, 225)), owning_building_guid = 95, terminal_guid = 1079)
    }

    Building12()

    def Building12(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 96, MapID: 12
      LocalBuilding("vt_vehicle", 96, 12, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2826f, 5114f, 35.99899f), vt_vehicle)))
      LocalObject(1080, Terminal.Constructor(Vector3(2836.242f, 5124.253f, 38.68599f), ground_vehicle_terminal), owning_building_guid = 96)
      LocalObject(725, VehicleSpawnPad.Constructor(Vector3(2825.915f, 5113.877f, 34.52799f), mb_pad_creation, Vector3(0, 0, 225)), owning_building_guid = 96, terminal_guid = 1080)
    }

    Building47()

    def Building47(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 97, MapID: 47
      LocalBuilding("vt_vehicle", 97, 47, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2848f, 2946f, 34.92273f), vt_vehicle)))
      LocalObject(1081, Terminal.Constructor(Vector3(2862.492f, 2946.008f, 37.60973f), ground_vehicle_terminal), owning_building_guid = 97)
      LocalObject(726, VehicleSpawnPad.Constructor(Vector3(2847.853f, 2945.973f, 33.45173f), mb_pad_creation, Vector3(0, 0, -90)), owning_building_guid = 97, terminal_guid = 1081)
    }

    Building20()

    def Building20(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 98, MapID: 20
      LocalBuilding("vt_vehicle", 98, 20, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2874f, 5402f, 35.99899f), vt_vehicle)))
      LocalObject(1082, Terminal.Constructor(Vector3(2874.008f, 5387.508f, 38.68599f), ground_vehicle_terminal), owning_building_guid = 98)
      LocalObject(727, VehicleSpawnPad.Constructor(Vector3(2873.973f, 5402.147f, 34.52799f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 98, terminal_guid = 1082)
    }

    Building52()

    def Building52(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 99, MapID: 52
      LocalBuilding("vt_vehicle", 99, 52, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2908f, 2814f, 34.91781f), vt_vehicle)))
      LocalObject(1083, Terminal.Constructor(Vector3(2907.992f, 2828.492f, 37.60481f), ground_vehicle_terminal), owning_building_guid = 99)
      LocalObject(728, VehicleSpawnPad.Constructor(Vector3(2908.027f, 2813.853f, 33.44681f), mb_pad_creation, Vector3(0, 0, 180)), owning_building_guid = 99, terminal_guid = 1083)
    }

    Building53()

    def Building53(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 100, MapID: 53
      LocalBuilding("vt_vehicle", 100, 53, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2934f, 3140f, 34.91683f), vt_vehicle)))
      LocalObject(1084, Terminal.Constructor(Vector3(2934.008f, 3125.508f, 37.60383f), ground_vehicle_terminal), owning_building_guid = 100)
      LocalObject(729, VehicleSpawnPad.Constructor(Vector3(2933.973f, 3140.147f, 33.44583f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 100, terminal_guid = 1084)
    }

    Building21()

    def Building21(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 101, MapID: 21
      LocalBuilding("vt_vehicle", 101, 21, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2962f, 5402f, 35.99899f), vt_vehicle)))
      LocalObject(1085, Terminal.Constructor(Vector3(2962.008f, 5387.508f, 38.68599f), ground_vehicle_terminal), owning_building_guid = 101)
      LocalObject(730, VehicleSpawnPad.Constructor(Vector3(2961.973f, 5402.147f, 34.52799f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 101, terminal_guid = 1085)
    }

    Building45()

    def Building45(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 102, MapID: 45
      LocalBuilding("vt_vehicle", 102, 45, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3012f, 2816f, 34.91574f), vt_vehicle)))
      LocalObject(1087, Terminal.Constructor(Vector3(3011.992f, 2830.492f, 37.60274f), ground_vehicle_terminal), owning_building_guid = 102)
      LocalObject(731, VehicleSpawnPad.Constructor(Vector3(3012.027f, 2815.853f, 33.44474f), mb_pad_creation, Vector3(0, 0, 180)), owning_building_guid = 102, terminal_guid = 1087)
    }

    Building10()

    def Building10(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 103, MapID: 10
      LocalBuilding("vt_vehicle", 103, 10, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3018f, 5116f, 35.99899f), vt_vehicle)))
      LocalObject(1086, Terminal.Constructor(Vector3(3007.747f, 5126.242f, 38.68599f), ground_vehicle_terminal), owning_building_guid = 103)
      LocalObject(732, VehicleSpawnPad.Constructor(Vector3(3018.123f, 5115.915f, 34.52799f), mb_pad_creation, Vector3(0, 0, 135)), owning_building_guid = 103, terminal_guid = 1086)
    }

    Building9()

    def Building9(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 104, MapID: 9
      LocalBuilding("vt_vehicle", 104, 9, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3064f, 5162f, 35.99899f), vt_vehicle)))
      LocalObject(1088, Terminal.Constructor(Vector3(3053.747f, 5172.242f, 38.68599f), ground_vehicle_terminal), owning_building_guid = 104)
      LocalObject(733, VehicleSpawnPad.Constructor(Vector3(3064.123f, 5161.915f, 34.52799f), mb_pad_creation, Vector3(0, 0, 135)), owning_building_guid = 104, terminal_guid = 1088)
    }

    Building51()

    def Building51(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 105, MapID: 51
      LocalBuilding("vt_vehicle", 105, 51, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3136f, 3182f, 34.92273f), vt_vehicle)))
      LocalObject(1089, Terminal.Constructor(Vector3(3136.008f, 3167.508f, 37.60973f), ground_vehicle_terminal), owning_building_guid = 105)
      LocalObject(734, VehicleSpawnPad.Constructor(Vector3(3135.973f, 3182.147f, 33.45173f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 105, terminal_guid = 1089)
    }

    Building50()

    def Building50(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 106, MapID: 50
      LocalBuilding("vt_vehicle", 106, 50, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3222f, 2984f, 34.92273f), vt_vehicle)))
      LocalObject(1090, Terminal.Constructor(Vector3(3207.508f, 2983.992f, 37.60973f), ground_vehicle_terminal), owning_building_guid = 106)
      LocalObject(735, VehicleSpawnPad.Constructor(Vector3(3222.147f, 2984.027f, 33.45173f), mb_pad_creation, Vector3(0, 0, 90)), owning_building_guid = 106, terminal_guid = 1090)
    }

    Building32()

    def Building32(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 107, MapID: 32
      LocalBuilding("vt_vehicle", 107, 32, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5118f, 4012f, 35.9291f), vt_vehicle)))
      LocalObject(1091, Terminal.Constructor(Vector3(5118.008f, 3997.508f, 38.6161f), ground_vehicle_terminal), owning_building_guid = 107)
      LocalObject(736, VehicleSpawnPad.Constructor(Vector3(5117.973f, 4012.147f, 34.4581f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 107, terminal_guid = 1091)
    }

    Building27()

    def Building27(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 108, MapID: 27
      LocalBuilding("vt_vehicle", 108, 27, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5126f, 3740f, 35.9291f), vt_vehicle)))
      LocalObject(1092, Terminal.Constructor(Vector3(5125.992f, 3754.492f, 38.6161f), ground_vehicle_terminal), owning_building_guid = 108)
      LocalObject(737, VehicleSpawnPad.Constructor(Vector3(5126.027f, 3739.853f, 34.4581f), mb_pad_creation, Vector3(0, 0, 180)), owning_building_guid = 108, terminal_guid = 1092)
    }

    Building34()

    def Building34(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 109, MapID: 34
      LocalBuilding("vt_vehicle", 109, 34, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5202f, 4038f, 35.9291f), vt_vehicle)))
      LocalObject(1093, Terminal.Constructor(Vector3(5202.008f, 4023.508f, 38.6161f), ground_vehicle_terminal), owning_building_guid = 109)
      LocalObject(738, VehicleSpawnPad.Constructor(Vector3(5201.973f, 4038.147f, 34.4581f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 109, terminal_guid = 1093)
    }

    Building35()

    def Building35(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 110, MapID: 35
      LocalBuilding("vt_vehicle", 110, 35, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5258f, 4038f, 35.9291f), vt_vehicle)))
      LocalObject(1094, Terminal.Constructor(Vector3(5258.008f, 4023.508f, 38.6161f), ground_vehicle_terminal), owning_building_guid = 110)
      LocalObject(739, VehicleSpawnPad.Constructor(Vector3(5257.973f, 4038.147f, 34.4581f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 110, terminal_guid = 1094)
    }

    Building29()

    def Building29(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 111, MapID: 29
      LocalBuilding("vt_vehicle", 111, 29, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5338f, 3740f, 35.9291f), vt_vehicle)))
      LocalObject(1095, Terminal.Constructor(Vector3(5337.992f, 3754.492f, 38.6161f), ground_vehicle_terminal), owning_building_guid = 111)
      LocalObject(740, VehicleSpawnPad.Constructor(Vector3(5338.027f, 3739.853f, 34.4581f), mb_pad_creation, Vector3(0, 0, 180)), owning_building_guid = 111, terminal_guid = 1095)
    }

    Building33()

    def Building33(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 112, MapID: 33
      LocalBuilding("vt_vehicle", 112, 33, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5342f, 4014f, 35.9291f), vt_vehicle)))
      LocalObject(1096, Terminal.Constructor(Vector3(5342.008f, 3999.508f, 38.6161f), ground_vehicle_terminal), owning_building_guid = 112)
      LocalObject(741, VehicleSpawnPad.Constructor(Vector3(5341.973f, 4014.147f, 34.4581f), mb_pad_creation, Vector3(0, 0, 0)), owning_building_guid = 112, terminal_guid = 1096)
    }

    Building1()

    def Building1(): Unit = { // Name: WG_TRSanc_to_Ishundar Type: warpgate GUID: 113, MapID: 1
      LocalBuilding("WG_TRSanc_to_Ishundar", 113, 1, FoundationBuilder(WarpGate.Structure(Vector3(3254f, 4652f, 37.92788f))))
    }

    Building3()

    def Building3(): Unit = { // Name: WG_TRSanc_to_Cyssor Type: warpgate GUID: 114, MapID: 3
      LocalBuilding("WG_TRSanc_to_Cyssor", 114, 3, FoundationBuilder(WarpGate.Structure(Vector3(3744f, 3076f, 27.0604f))))
    }

    Building2()

    def Building2(): Unit = { // Name: WG_TRSanc_to_Forseral Type: warpgate GUID: 115, MapID: 2
      LocalBuilding("WG_TRSanc_to_Forseral", 115, 2, FoundationBuilder(WarpGate.Structure(Vector3(5300f, 4504f, 40.2132f))))
    }

    def Lattice(): Unit = {
    }

    Lattice()
  }
}
