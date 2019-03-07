package zonemaps

import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType}
import net.psforever.objects.serverobject.terminals.{CaptureTerminal, ProximityTerminal, Terminal}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.zones.ZoneMap
import net.psforever.types.Vector3

object Map12 {
  // HOME2 (TERRAN REPUBLIC SANCTUARY)
  val ZoneMap = new ZoneMap("map12") {

    Building4()

    def Building4(): Unit = { // Name: Hart_Ishundar Type: orbital_building_tr GUID: 1, MapID: 4
      LocalBuilding(1, 4, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2922f, 5230f, 35.99899f))))
      LocalObject(340, Door.Constructor(Vector3(2842f, 5217.99f, 40.10499f)), owning_building_guid = 1)
      LocalObject(341, Door.Constructor(Vector3(2842f, 5241.99f, 40.10499f)), owning_building_guid = 1)
      LocalObject(348, Door.Constructor(Vector3(3002f, 5218.01f, 40.10499f)), owning_building_guid = 1)
      LocalObject(349, Door.Constructor(Vector3(3002f, 5242.01f, 40.10499f)), owning_building_guid = 1)
      LocalObject(368, Door.Constructor(Vector3(2860.34f, 5230f, 40.10499f)), owning_building_guid = 1)
      LocalObject(369, Door.Constructor(Vector3(2860.34f, 5230f, 50.10499f)), owning_building_guid = 1)
      LocalObject(370, Door.Constructor(Vector3(2906f, 5277.99f, 55.105f)), owning_building_guid = 1)
      LocalObject(371, Door.Constructor(Vector3(2938f, 5277.99f, 55.105f)), owning_building_guid = 1)
      LocalObject(374, Door.Constructor(Vector3(2983.64f, 5230f, 40.10499f)), owning_building_guid = 1)
      LocalObject(375, Door.Constructor(Vector3(2983.64f, 5230f, 50.10499f)), owning_building_guid = 1)
      LocalObject(440, Door.Constructor(Vector3(2866f, 5250.01f, 50.10499f)), owning_building_guid = 1)
      LocalObject(442, Door.Constructor(Vector3(2978f, 5249.99f, 50.10499f)), owning_building_guid = 1)
      LocalObject(446, Door.Constructor(Vector3(2866f, 5218.015f, 40.10499f)), owning_building_guid = 1)
      LocalObject(447, Door.Constructor(Vector3(2866f, 5218.015f, 50.10499f)), owning_building_guid = 1)
      LocalObject(448, Door.Constructor(Vector3(2866f, 5242.015f, 40.10499f)), owning_building_guid = 1)
      LocalObject(449, Door.Constructor(Vector3(2866f, 5242.015f, 50.10499f)), owning_building_guid = 1)
      LocalObject(454, Door.Constructor(Vector3(2978f, 5217.985f, 40.10499f)), owning_building_guid = 1)
      LocalObject(455, Door.Constructor(Vector3(2978f, 5217.985f, 50.10499f)), owning_building_guid = 1)
      LocalObject(456, Door.Constructor(Vector3(2978f, 5241.985f, 40.10499f)), owning_building_guid = 1)
      LocalObject(457, Door.Constructor(Vector3(2978f, 5241.985f, 50.10499f)), owning_building_guid = 1)
      LocalObject(658, Locker.Constructor(Vector3(2917.402f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(659, Locker.Constructor(Vector3(2918.732f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(660, Locker.Constructor(Vector3(2920.049f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(661, Locker.Constructor(Vector3(2921.325f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(662, Locker.Constructor(Vector3(2922.614f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(663, Locker.Constructor(Vector3(2923.934f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(664, Locker.Constructor(Vector3(2925.276f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(665, Locker.Constructor(Vector3(2926.64f, 5281.815f, 53.10899f)), owning_building_guid = 1)
      LocalObject(144, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(145, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(146, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(147, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(148, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(149, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(150, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(151, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(152, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(153, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(154, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(155, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(168, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(169, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(170, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(171, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(172, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(173, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(174, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(175, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(176, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(177, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(178, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(179, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(787, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(788, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(789, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(790, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(794, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(795, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(796, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(797, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(798, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(799, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(806, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(809, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(816, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(817, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(818, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(819, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(820, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(821, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(823, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(824, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(754, ProximityTerminal.Constructor(medical_terminal, Vector3(2851.311f, 5207.145f, 38.48999f)), owning_building_guid = 1)
      LocalObject(755, ProximityTerminal.Constructor(medical_terminal, Vector3(2851.333f, 5252.849f, 38.48999f)), owning_building_guid = 1)
      LocalObject(758, ProximityTerminal.Constructor(medical_terminal, Vector3(2992.631f, 5252.849f, 38.48999f)), owning_building_guid = 1)
      LocalObject(759, ProximityTerminal.Constructor(medical_terminal, Vector3(2992.693f, 5207.145f, 38.48999f)), owning_building_guid = 1)
      LocalObject(494, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(470, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(494, 470)
      LocalObject(495, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(471, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(495, 471)
      LocalObject(496, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(472, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(496, 472)
      LocalObject(497, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(473, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(497, 473)
      LocalObject(502, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(478, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(502, 478)
      LocalObject(503, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(479, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(503, 479)
      LocalObject(504, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(480, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(504, 480)
      LocalObject(505, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(481, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(505, 481)
    }

    Building40()

    def Building40(): Unit = { // Name: Hart_Cyssor Type: orbital_building_tr GUID: 2, MapID: 40
      LocalBuilding(2, 40, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3006f, 2984f, 34.91934f))))
      LocalObject(345, Door.Constructor(Vector3(2926f, 2971.99f, 39.02534f)), owning_building_guid = 2)
      LocalObject(346, Door.Constructor(Vector3(2926f, 2995.99f, 39.02534f)), owning_building_guid = 2)
      LocalObject(352, Door.Constructor(Vector3(3086f, 2972.01f, 39.02534f)), owning_building_guid = 2)
      LocalObject(353, Door.Constructor(Vector3(3086f, 2996.01f, 39.02534f)), owning_building_guid = 2)
      LocalObject(372, Door.Constructor(Vector3(2944.34f, 2984f, 39.02534f)), owning_building_guid = 2)
      LocalObject(373, Door.Constructor(Vector3(2944.34f, 2984f, 49.02534f)), owning_building_guid = 2)
      LocalObject(376, Door.Constructor(Vector3(2990f, 3031.99f, 54.02534f)), owning_building_guid = 2)
      LocalObject(377, Door.Constructor(Vector3(3022f, 3031.99f, 54.02534f)), owning_building_guid = 2)
      LocalObject(378, Door.Constructor(Vector3(3067.64f, 2984f, 39.02534f)), owning_building_guid = 2)
      LocalObject(379, Door.Constructor(Vector3(3067.64f, 2984f, 49.02534f)), owning_building_guid = 2)
      LocalObject(441, Door.Constructor(Vector3(2950f, 3004.01f, 49.02534f)), owning_building_guid = 2)
      LocalObject(443, Door.Constructor(Vector3(3062f, 3003.99f, 49.02534f)), owning_building_guid = 2)
      LocalObject(450, Door.Constructor(Vector3(2950f, 2972.015f, 39.02534f)), owning_building_guid = 2)
      LocalObject(451, Door.Constructor(Vector3(2950f, 2972.015f, 49.02534f)), owning_building_guid = 2)
      LocalObject(452, Door.Constructor(Vector3(2950f, 2996.015f, 39.02534f)), owning_building_guid = 2)
      LocalObject(453, Door.Constructor(Vector3(2950f, 2996.015f, 49.02534f)), owning_building_guid = 2)
      LocalObject(458, Door.Constructor(Vector3(3062f, 2971.985f, 39.02534f)), owning_building_guid = 2)
      LocalObject(459, Door.Constructor(Vector3(3062f, 2971.985f, 49.02534f)), owning_building_guid = 2)
      LocalObject(460, Door.Constructor(Vector3(3062f, 2995.985f, 39.02534f)), owning_building_guid = 2)
      LocalObject(461, Door.Constructor(Vector3(3062f, 2995.985f, 49.02534f)), owning_building_guid = 2)
      LocalObject(666, Locker.Constructor(Vector3(3001.402f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(667, Locker.Constructor(Vector3(3002.732f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(668, Locker.Constructor(Vector3(3004.049f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(669, Locker.Constructor(Vector3(3005.325f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(670, Locker.Constructor(Vector3(3006.614f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(671, Locker.Constructor(Vector3(3007.934f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(672, Locker.Constructor(Vector3(3009.276f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(673, Locker.Constructor(Vector3(3010.64f, 3035.815f, 52.02934f)), owning_building_guid = 2)
      LocalObject(156, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(157, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(158, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(159, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(160, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(161, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(162, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(163, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(164, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(165, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(166, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(167, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(180, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(181, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(182, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(183, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(184, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(185, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(186, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(187, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(188, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(189, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(190, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(191, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(807, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(808, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(810, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(811, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(812, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(813, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(814, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(815, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(822, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(825, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(826, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(827, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(828, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(829, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(830, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(831, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(836, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(837, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(840, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(841, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(756, ProximityTerminal.Constructor(medical_terminal, Vector3(2935.311f, 2961.145f, 37.41034f)), owning_building_guid = 2)
      LocalObject(757, ProximityTerminal.Constructor(medical_terminal, Vector3(2935.333f, 3006.849f, 37.41034f)), owning_building_guid = 2)
      LocalObject(760, ProximityTerminal.Constructor(medical_terminal, Vector3(3076.631f, 3006.849f, 37.41034f)), owning_building_guid = 2)
      LocalObject(761, ProximityTerminal.Constructor(medical_terminal, Vector3(3076.693f, 2961.145f, 37.41034f)), owning_building_guid = 2)
      LocalObject(498, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(474, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(498, 474)
      LocalObject(499, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(475, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(499, 475)
      LocalObject(500, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(476, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(500, 476)
      LocalObject(501, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(477, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(501, 477)
      LocalObject(506, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(482, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(506, 482)
      LocalObject(507, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(483, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(507, 483)
      LocalObject(508, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(484, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(508, 484)
      LocalObject(509, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(485, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(509, 485)
    }

    Building22()

    def Building22(): Unit = { // Name: Hart_Forseral Type: orbital_building_tr GUID: 3, MapID: 22
      LocalBuilding(3, 22, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5232f, 3908f, 35.9291f))))
      LocalObject(360, Door.Constructor(Vector3(5152f, 3895.99f, 40.0351f)), owning_building_guid = 3)
      LocalObject(361, Door.Constructor(Vector3(5152f, 3919.99f, 40.0351f)), owning_building_guid = 3)
      LocalObject(364, Door.Constructor(Vector3(5312f, 3896.01f, 40.0351f)), owning_building_guid = 3)
      LocalObject(365, Door.Constructor(Vector3(5312f, 3920.01f, 40.0351f)), owning_building_guid = 3)
      LocalObject(380, Door.Constructor(Vector3(5170.34f, 3908f, 40.0351f)), owning_building_guid = 3)
      LocalObject(381, Door.Constructor(Vector3(5170.34f, 3908f, 50.0351f)), owning_building_guid = 3)
      LocalObject(382, Door.Constructor(Vector3(5216f, 3955.99f, 55.0351f)), owning_building_guid = 3)
      LocalObject(383, Door.Constructor(Vector3(5248f, 3955.99f, 55.0351f)), owning_building_guid = 3)
      LocalObject(384, Door.Constructor(Vector3(5293.64f, 3908f, 40.0351f)), owning_building_guid = 3)
      LocalObject(385, Door.Constructor(Vector3(5293.64f, 3908f, 50.0351f)), owning_building_guid = 3)
      LocalObject(444, Door.Constructor(Vector3(5176f, 3928.01f, 50.0351f)), owning_building_guid = 3)
      LocalObject(445, Door.Constructor(Vector3(5288f, 3927.99f, 50.0351f)), owning_building_guid = 3)
      LocalObject(462, Door.Constructor(Vector3(5176f, 3896.015f, 40.0351f)), owning_building_guid = 3)
      LocalObject(463, Door.Constructor(Vector3(5176f, 3896.015f, 50.0351f)), owning_building_guid = 3)
      LocalObject(464, Door.Constructor(Vector3(5176f, 3920.015f, 40.0351f)), owning_building_guid = 3)
      LocalObject(465, Door.Constructor(Vector3(5176f, 3920.015f, 50.0351f)), owning_building_guid = 3)
      LocalObject(466, Door.Constructor(Vector3(5288f, 3895.985f, 40.0351f)), owning_building_guid = 3)
      LocalObject(467, Door.Constructor(Vector3(5288f, 3895.985f, 50.0351f)), owning_building_guid = 3)
      LocalObject(468, Door.Constructor(Vector3(5288f, 3919.985f, 40.0351f)), owning_building_guid = 3)
      LocalObject(469, Door.Constructor(Vector3(5288f, 3919.985f, 50.0351f)), owning_building_guid = 3)
      LocalObject(674, Locker.Constructor(Vector3(5227.402f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(675, Locker.Constructor(Vector3(5228.732f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(676, Locker.Constructor(Vector3(5230.049f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(677, Locker.Constructor(Vector3(5231.325f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(678, Locker.Constructor(Vector3(5232.614f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(679, Locker.Constructor(Vector3(5233.934f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(680, Locker.Constructor(Vector3(5235.276f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(681, Locker.Constructor(Vector3(5236.64f, 3959.815f, 53.0391f)), owning_building_guid = 3)
      LocalObject(192, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(193, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(194, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(195, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(196, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(197, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(198, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(199, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(200, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(201, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(202, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(203, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(204, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(205, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(206, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(207, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(208, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(209, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(210, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(211, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(212, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(213, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(214, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(215, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(878, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(879, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(880, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(881, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(882, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(883, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(884, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(885, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(886, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(887, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(894, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(895, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(896, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(897, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(898, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(899, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(900, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(901, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(902, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(903, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(762, ProximityTerminal.Constructor(medical_terminal, Vector3(5161.311f, 3885.145f, 38.4201f)), owning_building_guid = 3)
      LocalObject(763, ProximityTerminal.Constructor(medical_terminal, Vector3(5161.333f, 3930.849f, 38.4201f)), owning_building_guid = 3)
      LocalObject(764, ProximityTerminal.Constructor(medical_terminal, Vector3(5302.631f, 3930.849f, 38.4201f)), owning_building_guid = 3)
      LocalObject(765, ProximityTerminal.Constructor(medical_terminal, Vector3(5302.693f, 3885.145f, 38.4201f)), owning_building_guid = 3)
      LocalObject(510, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(486, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(510, 486)
      LocalObject(511, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(487, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(511, 487)
      LocalObject(512, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(488, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(512, 488)
      LocalObject(513, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(489, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(513, 489)
      LocalObject(514, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(490, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(514, 490)
      LocalObject(515, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(491, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(515, 491)
      LocalObject(516, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(492, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(516, 492)
      LocalObject(517, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(493, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(517, 493)
    }

    Building58()

    def Building58(): Unit = { // Name: NW_Ishundar_WG_tower Type: tower_a GUID: 28, MapID: 58
      LocalBuilding(28, 58, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(2620f, 5418f, 35.99397f))))
      LocalObject(995, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 28)
      LocalObject(294, Door.Constructor(Vector3(2632f, 5410f, 37.51497f)), owning_building_guid = 28)
      LocalObject(295, Door.Constructor(Vector3(2632f, 5410f, 57.51397f)), owning_building_guid = 28)
      LocalObject(296, Door.Constructor(Vector3(2632f, 5426f, 37.51497f)), owning_building_guid = 28)
      LocalObject(297, Door.Constructor(Vector3(2632f, 5426f, 57.51397f)), owning_building_guid = 28)
      LocalObject(1005, Door.Constructor(Vector3(2631.146f, 5406.794f, 27.32997f)), owning_building_guid = 28)
      LocalObject(1006, Door.Constructor(Vector3(2631.146f, 5423.204f, 27.32997f)), owning_building_guid = 28)
      LocalObject(518, IFFLock.Constructor(Vector3(2629.957f, 5426.811f, 37.45497f), Vector3(0, 0, 0)), owning_building_guid = 28, door_guid = 296)
      LocalObject(519, IFFLock.Constructor(Vector3(2629.957f, 5426.811f, 57.45497f), Vector3(0, 0, 0)), owning_building_guid = 28, door_guid = 297)
      LocalObject(520, IFFLock.Constructor(Vector3(2634.047f, 5409.189f, 37.45497f), Vector3(0, 0, 180)), owning_building_guid = 28, door_guid = 294)
      LocalObject(521, IFFLock.Constructor(Vector3(2634.047f, 5409.189f, 57.45497f), Vector3(0, 0, 180)), owning_building_guid = 28, door_guid = 295)
      LocalObject(562, Locker.Constructor(Vector3(2635.716f, 5402.963f, 25.98797f)), owning_building_guid = 28)
      LocalObject(563, Locker.Constructor(Vector3(2635.751f, 5424.835f, 25.98797f)), owning_building_guid = 28)
      LocalObject(564, Locker.Constructor(Vector3(2637.053f, 5402.963f, 25.98797f)), owning_building_guid = 28)
      LocalObject(565, Locker.Constructor(Vector3(2637.088f, 5424.835f, 25.98797f)), owning_building_guid = 28)
      LocalObject(566, Locker.Constructor(Vector3(2639.741f, 5402.963f, 25.98797f)), owning_building_guid = 28)
      LocalObject(567, Locker.Constructor(Vector3(2639.741f, 5424.835f, 25.98797f)), owning_building_guid = 28)
      LocalObject(568, Locker.Constructor(Vector3(2641.143f, 5402.963f, 25.98797f)), owning_building_guid = 28)
      LocalObject(569, Locker.Constructor(Vector3(2641.143f, 5424.835f, 25.98797f)), owning_building_guid = 28)
      LocalObject(769, Terminal.Constructor(order_terminal), owning_building_guid = 28)
      LocalObject(770, Terminal.Constructor(order_terminal), owning_building_guid = 28)
      LocalObject(771, Terminal.Constructor(order_terminal), owning_building_guid = 28)
      LocalObject(975, SpawnTube.Constructor(respawn_tube_tower, Vector3(2630.706f, 5405.742f, 25.47597f), Vector3(0, 0, 0)), owning_building_guid = 28)
      LocalObject(976, SpawnTube.Constructor(respawn_tube_tower, Vector3(2630.706f, 5422.152f, 25.47597f), Vector3(0, 0, 0)), owning_building_guid = 28)
      LocalObject(642, FacilityTurret.Constructor(manned_turret, Vector3(2607.32f, 5405.295f, 54.93597f)), owning_building_guid = 28)
      TurretToWeapon(642, 5000)
      LocalObject(643, FacilityTurret.Constructor(manned_turret, Vector3(2642.647f, 5430.707f, 54.93597f)), owning_building_guid = 28)
      TurretToWeapon(643, 5001)
    }

    Building66()

    def Building66(): Unit = { // Name: W_Cyssor_WG_tower Type: tower_a GUID: 29, MapID: 66
      LocalBuilding(29, 66, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(2806f, 3292f, 32.44872f))))
      LocalObject(997, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 29)
      LocalObject(304, Door.Constructor(Vector3(2818f, 3284f, 33.96972f)), owning_building_guid = 29)
      LocalObject(305, Door.Constructor(Vector3(2818f, 3284f, 53.96872f)), owning_building_guid = 29)
      LocalObject(306, Door.Constructor(Vector3(2818f, 3300f, 33.96972f)), owning_building_guid = 29)
      LocalObject(307, Door.Constructor(Vector3(2818f, 3300f, 53.96872f)), owning_building_guid = 29)
      LocalObject(1009, Door.Constructor(Vector3(2817.146f, 3280.794f, 23.78472f)), owning_building_guid = 29)
      LocalObject(1010, Door.Constructor(Vector3(2817.146f, 3297.204f, 23.78472f)), owning_building_guid = 29)
      LocalObject(528, IFFLock.Constructor(Vector3(2815.957f, 3300.811f, 33.90971f), Vector3(0, 0, 0)), owning_building_guid = 29, door_guid = 306)
      LocalObject(529, IFFLock.Constructor(Vector3(2815.957f, 3300.811f, 53.90971f), Vector3(0, 0, 0)), owning_building_guid = 29, door_guid = 307)
      LocalObject(530, IFFLock.Constructor(Vector3(2820.047f, 3283.189f, 33.90971f), Vector3(0, 0, 180)), owning_building_guid = 29, door_guid = 304)
      LocalObject(531, IFFLock.Constructor(Vector3(2820.047f, 3283.189f, 53.90971f), Vector3(0, 0, 180)), owning_building_guid = 29, door_guid = 305)
      LocalObject(578, Locker.Constructor(Vector3(2821.716f, 3276.963f, 22.44271f)), owning_building_guid = 29)
      LocalObject(579, Locker.Constructor(Vector3(2821.751f, 3298.835f, 22.44271f)), owning_building_guid = 29)
      LocalObject(580, Locker.Constructor(Vector3(2823.053f, 3276.963f, 22.44271f)), owning_building_guid = 29)
      LocalObject(581, Locker.Constructor(Vector3(2823.088f, 3298.835f, 22.44271f)), owning_building_guid = 29)
      LocalObject(582, Locker.Constructor(Vector3(2825.741f, 3276.963f, 22.44271f)), owning_building_guid = 29)
      LocalObject(583, Locker.Constructor(Vector3(2825.741f, 3298.835f, 22.44271f)), owning_building_guid = 29)
      LocalObject(584, Locker.Constructor(Vector3(2827.143f, 3276.963f, 22.44271f)), owning_building_guid = 29)
      LocalObject(585, Locker.Constructor(Vector3(2827.143f, 3298.835f, 22.44271f)), owning_building_guid = 29)
      LocalObject(781, Terminal.Constructor(order_terminal), owning_building_guid = 29)
      LocalObject(782, Terminal.Constructor(order_terminal), owning_building_guid = 29)
      LocalObject(783, Terminal.Constructor(order_terminal), owning_building_guid = 29)
      LocalObject(979, SpawnTube.Constructor(respawn_tube_tower, Vector3(2816.706f, 3279.742f, 21.93072f), Vector3(0, 0, 0)), owning_building_guid = 29)
      LocalObject(980, SpawnTube.Constructor(respawn_tube_tower, Vector3(2816.706f, 3296.152f, 21.93072f), Vector3(0, 0, 0)), owning_building_guid = 29)
      LocalObject(644, FacilityTurret.Constructor(manned_turret, Vector3(2793.32f, 3279.295f, 51.39072f)), owning_building_guid = 29)
      TurretToWeapon(644, 5002)
      LocalObject(645, FacilityTurret.Constructor(manned_turret, Vector3(2828.647f, 3304.707f, 51.39072f)), owning_building_guid = 29)
      TurretToWeapon(645, 5003)
    }

    Building60()

    def Building60(): Unit = { // Name: W_Forseral_WG_tower Type: tower_a GUID: 30, MapID: 60
      LocalBuilding(30, 60, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4564f, 4572f, 54.12079f))))
      LocalObject(1002, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 30)
      LocalObject(326, Door.Constructor(Vector3(4576f, 4564f, 55.64179f)), owning_building_guid = 30)
      LocalObject(327, Door.Constructor(Vector3(4576f, 4564f, 75.64079f)), owning_building_guid = 30)
      LocalObject(328, Door.Constructor(Vector3(4576f, 4580f, 55.64179f)), owning_building_guid = 30)
      LocalObject(329, Door.Constructor(Vector3(4576f, 4580f, 75.64079f)), owning_building_guid = 30)
      LocalObject(1019, Door.Constructor(Vector3(4575.146f, 4560.794f, 45.45679f)), owning_building_guid = 30)
      LocalObject(1020, Door.Constructor(Vector3(4575.146f, 4577.204f, 45.45679f)), owning_building_guid = 30)
      LocalObject(550, IFFLock.Constructor(Vector3(4573.957f, 4580.811f, 55.58179f), Vector3(0, 0, 0)), owning_building_guid = 30, door_guid = 328)
      LocalObject(551, IFFLock.Constructor(Vector3(4573.957f, 4580.811f, 75.58179f), Vector3(0, 0, 0)), owning_building_guid = 30, door_guid = 329)
      LocalObject(552, IFFLock.Constructor(Vector3(4578.047f, 4563.189f, 55.58179f), Vector3(0, 0, 180)), owning_building_guid = 30, door_guid = 326)
      LocalObject(553, IFFLock.Constructor(Vector3(4578.047f, 4563.189f, 75.58179f), Vector3(0, 0, 180)), owning_building_guid = 30, door_guid = 327)
      LocalObject(618, Locker.Constructor(Vector3(4579.716f, 4556.963f, 44.11479f)), owning_building_guid = 30)
      LocalObject(619, Locker.Constructor(Vector3(4579.751f, 4578.835f, 44.11479f)), owning_building_guid = 30)
      LocalObject(620, Locker.Constructor(Vector3(4581.053f, 4556.963f, 44.11479f)), owning_building_guid = 30)
      LocalObject(621, Locker.Constructor(Vector3(4581.088f, 4578.835f, 44.11479f)), owning_building_guid = 30)
      LocalObject(622, Locker.Constructor(Vector3(4583.741f, 4556.963f, 44.11479f)), owning_building_guid = 30)
      LocalObject(623, Locker.Constructor(Vector3(4583.741f, 4578.835f, 44.11479f)), owning_building_guid = 30)
      LocalObject(624, Locker.Constructor(Vector3(4585.143f, 4556.963f, 44.11479f)), owning_building_guid = 30)
      LocalObject(625, Locker.Constructor(Vector3(4585.143f, 4578.835f, 44.11479f)), owning_building_guid = 30)
      LocalObject(866, Terminal.Constructor(order_terminal), owning_building_guid = 30)
      LocalObject(867, Terminal.Constructor(order_terminal), owning_building_guid = 30)
      LocalObject(868, Terminal.Constructor(order_terminal), owning_building_guid = 30)
      LocalObject(989, SpawnTube.Constructor(respawn_tube_tower, Vector3(4574.706f, 4559.742f, 43.60279f), Vector3(0, 0, 0)), owning_building_guid = 30)
      LocalObject(990, SpawnTube.Constructor(respawn_tube_tower, Vector3(4574.706f, 4576.152f, 43.60279f), Vector3(0, 0, 0)), owning_building_guid = 30)
      LocalObject(652, FacilityTurret.Constructor(manned_turret, Vector3(4551.32f, 4559.295f, 73.06279f)), owning_building_guid = 30)
      TurretToWeapon(652, 5004)
      LocalObject(653, FacilityTurret.Constructor(manned_turret, Vector3(4586.647f, 4584.707f, 73.06279f)), owning_building_guid = 30)
      TurretToWeapon(653, 5005)
    }

    Building63()

    def Building63(): Unit = { // Name: SW_Forseral_WG_tower Type: tower_a GUID: 31, MapID: 63
      LocalBuilding(31, 63, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4972f, 3678f, 35.9291f))))
      LocalObject(1003, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 31)
      LocalObject(330, Door.Constructor(Vector3(4984f, 3670f, 37.4501f)), owning_building_guid = 31)
      LocalObject(331, Door.Constructor(Vector3(4984f, 3670f, 57.4491f)), owning_building_guid = 31)
      LocalObject(332, Door.Constructor(Vector3(4984f, 3686f, 37.4501f)), owning_building_guid = 31)
      LocalObject(333, Door.Constructor(Vector3(4984f, 3686f, 57.4491f)), owning_building_guid = 31)
      LocalObject(1021, Door.Constructor(Vector3(4983.146f, 3666.794f, 27.2651f)), owning_building_guid = 31)
      LocalObject(1022, Door.Constructor(Vector3(4983.146f, 3683.204f, 27.2651f)), owning_building_guid = 31)
      LocalObject(554, IFFLock.Constructor(Vector3(4981.957f, 3686.811f, 37.3901f), Vector3(0, 0, 0)), owning_building_guid = 31, door_guid = 332)
      LocalObject(555, IFFLock.Constructor(Vector3(4981.957f, 3686.811f, 57.39011f), Vector3(0, 0, 0)), owning_building_guid = 31, door_guid = 333)
      LocalObject(556, IFFLock.Constructor(Vector3(4986.047f, 3669.189f, 37.3901f), Vector3(0, 0, 180)), owning_building_guid = 31, door_guid = 330)
      LocalObject(557, IFFLock.Constructor(Vector3(4986.047f, 3669.189f, 57.39011f), Vector3(0, 0, 180)), owning_building_guid = 31, door_guid = 331)
      LocalObject(626, Locker.Constructor(Vector3(4987.716f, 3662.963f, 25.9231f)), owning_building_guid = 31)
      LocalObject(627, Locker.Constructor(Vector3(4987.751f, 3684.835f, 25.9231f)), owning_building_guid = 31)
      LocalObject(628, Locker.Constructor(Vector3(4989.053f, 3662.963f, 25.9231f)), owning_building_guid = 31)
      LocalObject(629, Locker.Constructor(Vector3(4989.088f, 3684.835f, 25.9231f)), owning_building_guid = 31)
      LocalObject(630, Locker.Constructor(Vector3(4991.741f, 3662.963f, 25.9231f)), owning_building_guid = 31)
      LocalObject(631, Locker.Constructor(Vector3(4991.741f, 3684.835f, 25.9231f)), owning_building_guid = 31)
      LocalObject(632, Locker.Constructor(Vector3(4993.143f, 3662.963f, 25.9231f)), owning_building_guid = 31)
      LocalObject(633, Locker.Constructor(Vector3(4993.143f, 3684.835f, 25.9231f)), owning_building_guid = 31)
      LocalObject(869, Terminal.Constructor(order_terminal), owning_building_guid = 31)
      LocalObject(870, Terminal.Constructor(order_terminal), owning_building_guid = 31)
      LocalObject(871, Terminal.Constructor(order_terminal), owning_building_guid = 31)
      LocalObject(991, SpawnTube.Constructor(respawn_tube_tower, Vector3(4982.706f, 3665.742f, 25.4111f), Vector3(0, 0, 0)), owning_building_guid = 31)
      LocalObject(992, SpawnTube.Constructor(respawn_tube_tower, Vector3(4982.706f, 3682.152f, 25.4111f), Vector3(0, 0, 0)), owning_building_guid = 31)
      LocalObject(654, FacilityTurret.Constructor(manned_turret, Vector3(4959.32f, 3665.295f, 54.8711f)), owning_building_guid = 31)
      TurretToWeapon(654, 5006)
      LocalObject(655, FacilityTurret.Constructor(manned_turret, Vector3(4994.647f, 3690.707f, 54.8711f)), owning_building_guid = 31)
      TurretToWeapon(655, 5007)
    }

    Building65()

    def Building65(): Unit = { // Name: SW_Ishundar_WG_tower Type: tower_b GUID: 32, MapID: 65
      LocalBuilding(32, 65, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(2794f, 4210f, 36.31348f))))
      LocalObject(996, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 32)
      LocalObject(298, Door.Constructor(Vector3(2806f, 4202f, 37.83348f)), owning_building_guid = 32)
      LocalObject(299, Door.Constructor(Vector3(2806f, 4202f, 47.83348f)), owning_building_guid = 32)
      LocalObject(300, Door.Constructor(Vector3(2806f, 4202f, 67.83348f)), owning_building_guid = 32)
      LocalObject(301, Door.Constructor(Vector3(2806f, 4218f, 37.83348f)), owning_building_guid = 32)
      LocalObject(302, Door.Constructor(Vector3(2806f, 4218f, 47.83348f)), owning_building_guid = 32)
      LocalObject(303, Door.Constructor(Vector3(2806f, 4218f, 67.83348f)), owning_building_guid = 32)
      LocalObject(1007, Door.Constructor(Vector3(2805.147f, 4198.794f, 27.64948f)), owning_building_guid = 32)
      LocalObject(1008, Door.Constructor(Vector3(2805.147f, 4215.204f, 27.64948f)), owning_building_guid = 32)
      LocalObject(522, IFFLock.Constructor(Vector3(2803.957f, 4218.811f, 37.77448f), Vector3(0, 0, 0)), owning_building_guid = 32, door_guid = 301)
      LocalObject(523, IFFLock.Constructor(Vector3(2803.957f, 4218.811f, 47.77448f), Vector3(0, 0, 0)), owning_building_guid = 32, door_guid = 302)
      LocalObject(524, IFFLock.Constructor(Vector3(2803.957f, 4218.811f, 67.77448f), Vector3(0, 0, 0)), owning_building_guid = 32, door_guid = 303)
      LocalObject(525, IFFLock.Constructor(Vector3(2808.047f, 4201.189f, 37.77448f), Vector3(0, 0, 180)), owning_building_guid = 32, door_guid = 298)
      LocalObject(526, IFFLock.Constructor(Vector3(2808.047f, 4201.189f, 47.77448f), Vector3(0, 0, 180)), owning_building_guid = 32, door_guid = 299)
      LocalObject(527, IFFLock.Constructor(Vector3(2808.047f, 4201.189f, 67.77448f), Vector3(0, 0, 180)), owning_building_guid = 32, door_guid = 300)
      LocalObject(570, Locker.Constructor(Vector3(2809.716f, 4194.963f, 26.30748f)), owning_building_guid = 32)
      LocalObject(571, Locker.Constructor(Vector3(2809.751f, 4216.835f, 26.30748f)), owning_building_guid = 32)
      LocalObject(572, Locker.Constructor(Vector3(2811.053f, 4194.963f, 26.30748f)), owning_building_guid = 32)
      LocalObject(573, Locker.Constructor(Vector3(2811.088f, 4216.835f, 26.30748f)), owning_building_guid = 32)
      LocalObject(574, Locker.Constructor(Vector3(2813.741f, 4194.963f, 26.30748f)), owning_building_guid = 32)
      LocalObject(575, Locker.Constructor(Vector3(2813.741f, 4216.835f, 26.30748f)), owning_building_guid = 32)
      LocalObject(576, Locker.Constructor(Vector3(2815.143f, 4194.963f, 26.30748f)), owning_building_guid = 32)
      LocalObject(577, Locker.Constructor(Vector3(2815.143f, 4216.835f, 26.30748f)), owning_building_guid = 32)
      LocalObject(778, Terminal.Constructor(order_terminal), owning_building_guid = 32)
      LocalObject(779, Terminal.Constructor(order_terminal), owning_building_guid = 32)
      LocalObject(780, Terminal.Constructor(order_terminal), owning_building_guid = 32)
      LocalObject(977, SpawnTube.Constructor(respawn_tube_tower, Vector3(2804.706f, 4197.742f, 25.79548f), Vector3(0, 0, 0)), owning_building_guid = 32)
      LocalObject(978, SpawnTube.Constructor(respawn_tube_tower, Vector3(2804.706f, 4214.152f, 25.79548f), Vector3(0, 0, 0)), owning_building_guid = 32)
    }

    Building64()

    def Building64(): Unit = { // Name: E_Cyssor_WG_tower Type: tower_b GUID: 33, MapID: 64
      LocalBuilding(33, 64, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4484f, 3080f, 50.39579f))))
      LocalObject(1001, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 33)
      LocalObject(320, Door.Constructor(Vector3(4496f, 3072f, 51.91579f)), owning_building_guid = 33)
      LocalObject(321, Door.Constructor(Vector3(4496f, 3072f, 61.91579f)), owning_building_guid = 33)
      LocalObject(322, Door.Constructor(Vector3(4496f, 3072f, 81.91579f)), owning_building_guid = 33)
      LocalObject(323, Door.Constructor(Vector3(4496f, 3088f, 51.91579f)), owning_building_guid = 33)
      LocalObject(324, Door.Constructor(Vector3(4496f, 3088f, 61.91579f)), owning_building_guid = 33)
      LocalObject(325, Door.Constructor(Vector3(4496f, 3088f, 81.91579f)), owning_building_guid = 33)
      LocalObject(1017, Door.Constructor(Vector3(4495.147f, 3068.794f, 41.7318f)), owning_building_guid = 33)
      LocalObject(1018, Door.Constructor(Vector3(4495.147f, 3085.204f, 41.7318f)), owning_building_guid = 33)
      LocalObject(544, IFFLock.Constructor(Vector3(4493.957f, 3088.811f, 51.85679f), Vector3(0, 0, 0)), owning_building_guid = 33, door_guid = 323)
      LocalObject(545, IFFLock.Constructor(Vector3(4493.957f, 3088.811f, 61.8568f), Vector3(0, 0, 0)), owning_building_guid = 33, door_guid = 324)
      LocalObject(546, IFFLock.Constructor(Vector3(4493.957f, 3088.811f, 81.8568f), Vector3(0, 0, 0)), owning_building_guid = 33, door_guid = 325)
      LocalObject(547, IFFLock.Constructor(Vector3(4498.047f, 3071.189f, 51.85679f), Vector3(0, 0, 180)), owning_building_guid = 33, door_guid = 320)
      LocalObject(548, IFFLock.Constructor(Vector3(4498.047f, 3071.189f, 61.8568f), Vector3(0, 0, 180)), owning_building_guid = 33, door_guid = 321)
      LocalObject(549, IFFLock.Constructor(Vector3(4498.047f, 3071.189f, 81.8568f), Vector3(0, 0, 180)), owning_building_guid = 33, door_guid = 322)
      LocalObject(610, Locker.Constructor(Vector3(4499.716f, 3064.963f, 40.38979f)), owning_building_guid = 33)
      LocalObject(611, Locker.Constructor(Vector3(4499.751f, 3086.835f, 40.38979f)), owning_building_guid = 33)
      LocalObject(612, Locker.Constructor(Vector3(4501.053f, 3064.963f, 40.38979f)), owning_building_guid = 33)
      LocalObject(613, Locker.Constructor(Vector3(4501.088f, 3086.835f, 40.38979f)), owning_building_guid = 33)
      LocalObject(614, Locker.Constructor(Vector3(4503.741f, 3064.963f, 40.38979f)), owning_building_guid = 33)
      LocalObject(615, Locker.Constructor(Vector3(4503.741f, 3086.835f, 40.38979f)), owning_building_guid = 33)
      LocalObject(616, Locker.Constructor(Vector3(4505.143f, 3064.963f, 40.38979f)), owning_building_guid = 33)
      LocalObject(617, Locker.Constructor(Vector3(4505.143f, 3086.835f, 40.38979f)), owning_building_guid = 33)
      LocalObject(863, Terminal.Constructor(order_terminal), owning_building_guid = 33)
      LocalObject(864, Terminal.Constructor(order_terminal), owning_building_guid = 33)
      LocalObject(865, Terminal.Constructor(order_terminal), owning_building_guid = 33)
      LocalObject(987, SpawnTube.Constructor(respawn_tube_tower, Vector3(4494.706f, 3067.742f, 39.87779f), Vector3(0, 0, 0)), owning_building_guid = 33)
      LocalObject(988, SpawnTube.Constructor(respawn_tube_tower, Vector3(4494.706f, 3084.152f, 39.87779f), Vector3(0, 0, 0)), owning_building_guid = 33)
    }

    Building59()

    def Building59(): Unit = { // Name: N_Ishundar_WG_tower Type: tower_c GUID: 34, MapID: 59
      LocalBuilding(34, 59, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3236f, 5040f, 37.58544f))))
      LocalObject(998, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 34)
      LocalObject(308, Door.Constructor(Vector3(3248f, 5032f, 39.10644f)), owning_building_guid = 34)
      LocalObject(309, Door.Constructor(Vector3(3248f, 5032f, 59.10544f)), owning_building_guid = 34)
      LocalObject(310, Door.Constructor(Vector3(3248f, 5048f, 39.10644f)), owning_building_guid = 34)
      LocalObject(311, Door.Constructor(Vector3(3248f, 5048f, 59.10544f)), owning_building_guid = 34)
      LocalObject(1011, Door.Constructor(Vector3(3247.146f, 5028.794f, 28.92144f)), owning_building_guid = 34)
      LocalObject(1012, Door.Constructor(Vector3(3247.146f, 5045.204f, 28.92144f)), owning_building_guid = 34)
      LocalObject(532, IFFLock.Constructor(Vector3(3245.957f, 5048.811f, 39.04644f), Vector3(0, 0, 0)), owning_building_guid = 34, door_guid = 310)
      LocalObject(533, IFFLock.Constructor(Vector3(3245.957f, 5048.811f, 59.04644f), Vector3(0, 0, 0)), owning_building_guid = 34, door_guid = 311)
      LocalObject(534, IFFLock.Constructor(Vector3(3250.047f, 5031.189f, 39.04644f), Vector3(0, 0, 180)), owning_building_guid = 34, door_guid = 308)
      LocalObject(535, IFFLock.Constructor(Vector3(3250.047f, 5031.189f, 59.04644f), Vector3(0, 0, 180)), owning_building_guid = 34, door_guid = 309)
      LocalObject(586, Locker.Constructor(Vector3(3251.716f, 5024.963f, 27.57944f)), owning_building_guid = 34)
      LocalObject(587, Locker.Constructor(Vector3(3251.751f, 5046.835f, 27.57944f)), owning_building_guid = 34)
      LocalObject(588, Locker.Constructor(Vector3(3253.053f, 5024.963f, 27.57944f)), owning_building_guid = 34)
      LocalObject(589, Locker.Constructor(Vector3(3253.088f, 5046.835f, 27.57944f)), owning_building_guid = 34)
      LocalObject(590, Locker.Constructor(Vector3(3255.741f, 5024.963f, 27.57944f)), owning_building_guid = 34)
      LocalObject(591, Locker.Constructor(Vector3(3255.741f, 5046.835f, 27.57944f)), owning_building_guid = 34)
      LocalObject(592, Locker.Constructor(Vector3(3257.143f, 5024.963f, 27.57944f)), owning_building_guid = 34)
      LocalObject(593, Locker.Constructor(Vector3(3257.143f, 5046.835f, 27.57944f)), owning_building_guid = 34)
      LocalObject(854, Terminal.Constructor(order_terminal), owning_building_guid = 34)
      LocalObject(855, Terminal.Constructor(order_terminal), owning_building_guid = 34)
      LocalObject(856, Terminal.Constructor(order_terminal), owning_building_guid = 34)
      LocalObject(981, SpawnTube.Constructor(respawn_tube_tower, Vector3(3246.706f, 5027.742f, 27.06744f), Vector3(0, 0, 0)), owning_building_guid = 34)
      LocalObject(982, SpawnTube.Constructor(respawn_tube_tower, Vector3(3246.706f, 5044.152f, 27.06744f), Vector3(0, 0, 0)), owning_building_guid = 34)
      LocalObject(921, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3234.907f, 5034.725f, 65.15544f)), owning_building_guid = 34)
      LocalObject(922, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 34)
      LocalObject(924, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3234.907f, 5045.17f, 65.15544f)), owning_building_guid = 34)
      LocalObject(925, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 34)
      LocalObject(646, FacilityTurret.Constructor(manned_turret, Vector3(3221.07f, 5025.045f, 56.52744f)), owning_building_guid = 34)
      TurretToWeapon(646, 5008)
      LocalObject(648, FacilityTurret.Constructor(manned_turret, Vector3(3259.497f, 5054.957f, 56.52744f)), owning_building_guid = 34)
      TurretToWeapon(648, 5009)
    }

    Building67()

    def Building67(): Unit = { // Name: SW_Cyssor_WG_tower Type: tower_c GUID: 35, MapID: 67
      LocalBuilding(35, 67, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3260f, 2758f, 34.92906f))))
      LocalObject(999, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 35)
      LocalObject(312, Door.Constructor(Vector3(3272f, 2750f, 36.45006f)), owning_building_guid = 35)
      LocalObject(313, Door.Constructor(Vector3(3272f, 2750f, 56.44906f)), owning_building_guid = 35)
      LocalObject(314, Door.Constructor(Vector3(3272f, 2766f, 36.45006f)), owning_building_guid = 35)
      LocalObject(315, Door.Constructor(Vector3(3272f, 2766f, 56.44906f)), owning_building_guid = 35)
      LocalObject(1013, Door.Constructor(Vector3(3271.146f, 2746.794f, 26.26506f)), owning_building_guid = 35)
      LocalObject(1014, Door.Constructor(Vector3(3271.146f, 2763.204f, 26.26506f)), owning_building_guid = 35)
      LocalObject(536, IFFLock.Constructor(Vector3(3269.957f, 2766.811f, 36.39006f), Vector3(0, 0, 0)), owning_building_guid = 35, door_guid = 314)
      LocalObject(537, IFFLock.Constructor(Vector3(3269.957f, 2766.811f, 56.39006f), Vector3(0, 0, 0)), owning_building_guid = 35, door_guid = 315)
      LocalObject(538, IFFLock.Constructor(Vector3(3274.047f, 2749.189f, 36.39006f), Vector3(0, 0, 180)), owning_building_guid = 35, door_guid = 312)
      LocalObject(539, IFFLock.Constructor(Vector3(3274.047f, 2749.189f, 56.39006f), Vector3(0, 0, 180)), owning_building_guid = 35, door_guid = 313)
      LocalObject(594, Locker.Constructor(Vector3(3275.716f, 2742.963f, 24.92306f)), owning_building_guid = 35)
      LocalObject(595, Locker.Constructor(Vector3(3275.751f, 2764.835f, 24.92306f)), owning_building_guid = 35)
      LocalObject(596, Locker.Constructor(Vector3(3277.053f, 2742.963f, 24.92306f)), owning_building_guid = 35)
      LocalObject(597, Locker.Constructor(Vector3(3277.088f, 2764.835f, 24.92306f)), owning_building_guid = 35)
      LocalObject(598, Locker.Constructor(Vector3(3279.741f, 2742.963f, 24.92306f)), owning_building_guid = 35)
      LocalObject(599, Locker.Constructor(Vector3(3279.741f, 2764.835f, 24.92306f)), owning_building_guid = 35)
      LocalObject(600, Locker.Constructor(Vector3(3281.143f, 2742.963f, 24.92306f)), owning_building_guid = 35)
      LocalObject(601, Locker.Constructor(Vector3(3281.143f, 2764.835f, 24.92306f)), owning_building_guid = 35)
      LocalObject(857, Terminal.Constructor(order_terminal), owning_building_guid = 35)
      LocalObject(858, Terminal.Constructor(order_terminal), owning_building_guid = 35)
      LocalObject(859, Terminal.Constructor(order_terminal), owning_building_guid = 35)
      LocalObject(983, SpawnTube.Constructor(respawn_tube_tower, Vector3(3270.706f, 2745.742f, 24.41106f), Vector3(0, 0, 0)), owning_building_guid = 35)
      LocalObject(984, SpawnTube.Constructor(respawn_tube_tower, Vector3(3270.706f, 2762.152f, 24.41106f), Vector3(0, 0, 0)), owning_building_guid = 35)
      LocalObject(927, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3258.907f, 2752.725f, 62.49906f)), owning_building_guid = 35)
      LocalObject(928, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 35)
      LocalObject(930, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3258.907f, 2763.17f, 62.49906f)), owning_building_guid = 35)
      LocalObject(931, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 35)
      LocalObject(647, FacilityTurret.Constructor(manned_turret, Vector3(3245.07f, 2743.045f, 53.87106f)), owning_building_guid = 35)
      TurretToWeapon(647, 5010)
      LocalObject(649, FacilityTurret.Constructor(manned_turret, Vector3(3283.497f, 2772.957f, 53.87106f)), owning_building_guid = 35)
      TurretToWeapon(649, 5011)
    }

    Building61()

    def Building61(): Unit = { // Name: N_Cyssor_WG_tower Type: tower_c GUID: 36, MapID: 61
      LocalBuilding(36, 61, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3906f, 3832f, 46.40522f))))
      LocalObject(1000, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 36)
      LocalObject(316, Door.Constructor(Vector3(3918f, 3824f, 47.92622f)), owning_building_guid = 36)
      LocalObject(317, Door.Constructor(Vector3(3918f, 3824f, 67.92522f)), owning_building_guid = 36)
      LocalObject(318, Door.Constructor(Vector3(3918f, 3840f, 47.92622f)), owning_building_guid = 36)
      LocalObject(319, Door.Constructor(Vector3(3918f, 3840f, 67.92522f)), owning_building_guid = 36)
      LocalObject(1015, Door.Constructor(Vector3(3917.146f, 3820.794f, 37.74123f)), owning_building_guid = 36)
      LocalObject(1016, Door.Constructor(Vector3(3917.146f, 3837.204f, 37.74123f)), owning_building_guid = 36)
      LocalObject(540, IFFLock.Constructor(Vector3(3915.957f, 3840.811f, 47.86622f), Vector3(0, 0, 0)), owning_building_guid = 36, door_guid = 318)
      LocalObject(541, IFFLock.Constructor(Vector3(3915.957f, 3840.811f, 67.86623f), Vector3(0, 0, 0)), owning_building_guid = 36, door_guid = 319)
      LocalObject(542, IFFLock.Constructor(Vector3(3920.047f, 3823.189f, 47.86622f), Vector3(0, 0, 180)), owning_building_guid = 36, door_guid = 316)
      LocalObject(543, IFFLock.Constructor(Vector3(3920.047f, 3823.189f, 67.86623f), Vector3(0, 0, 180)), owning_building_guid = 36, door_guid = 317)
      LocalObject(602, Locker.Constructor(Vector3(3921.716f, 3816.963f, 36.39922f)), owning_building_guid = 36)
      LocalObject(603, Locker.Constructor(Vector3(3921.751f, 3838.835f, 36.39922f)), owning_building_guid = 36)
      LocalObject(604, Locker.Constructor(Vector3(3923.053f, 3816.963f, 36.39922f)), owning_building_guid = 36)
      LocalObject(605, Locker.Constructor(Vector3(3923.088f, 3838.835f, 36.39922f)), owning_building_guid = 36)
      LocalObject(606, Locker.Constructor(Vector3(3925.741f, 3816.963f, 36.39922f)), owning_building_guid = 36)
      LocalObject(607, Locker.Constructor(Vector3(3925.741f, 3838.835f, 36.39922f)), owning_building_guid = 36)
      LocalObject(608, Locker.Constructor(Vector3(3927.143f, 3816.963f, 36.39922f)), owning_building_guid = 36)
      LocalObject(609, Locker.Constructor(Vector3(3927.143f, 3838.835f, 36.39922f)), owning_building_guid = 36)
      LocalObject(860, Terminal.Constructor(order_terminal), owning_building_guid = 36)
      LocalObject(861, Terminal.Constructor(order_terminal), owning_building_guid = 36)
      LocalObject(862, Terminal.Constructor(order_terminal), owning_building_guid = 36)
      LocalObject(985, SpawnTube.Constructor(respawn_tube_tower, Vector3(3916.706f, 3819.742f, 35.88722f), Vector3(0, 0, 0)), owning_building_guid = 36)
      LocalObject(986, SpawnTube.Constructor(respawn_tube_tower, Vector3(3916.706f, 3836.152f, 35.88722f), Vector3(0, 0, 0)), owning_building_guid = 36)
      LocalObject(933, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3904.907f, 3826.725f, 73.97522f)), owning_building_guid = 36)
      LocalObject(934, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 36)
      LocalObject(936, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3904.907f, 3837.17f, 73.97522f)), owning_building_guid = 36)
      LocalObject(937, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 36)
      LocalObject(650, FacilityTurret.Constructor(manned_turret, Vector3(3891.07f, 3817.045f, 65.34722f)), owning_building_guid = 36)
      TurretToWeapon(650, 5012)
      LocalObject(651, FacilityTurret.Constructor(manned_turret, Vector3(3929.497f, 3846.957f, 65.34722f)), owning_building_guid = 36)
      TurretToWeapon(651, 5013)
    }

    Building62()

    def Building62(): Unit = { // Name: SE_Forseral_WG_tower Type: tower_c GUID: 37, MapID: 62
      LocalBuilding(37, 62, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(5488f, 4168f, 35.9291f))))
      LocalObject(1004, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 37)
      LocalObject(334, Door.Constructor(Vector3(5500f, 4160f, 37.4501f)), owning_building_guid = 37)
      LocalObject(335, Door.Constructor(Vector3(5500f, 4160f, 57.4491f)), owning_building_guid = 37)
      LocalObject(336, Door.Constructor(Vector3(5500f, 4176f, 37.4501f)), owning_building_guid = 37)
      LocalObject(337, Door.Constructor(Vector3(5500f, 4176f, 57.4491f)), owning_building_guid = 37)
      LocalObject(1023, Door.Constructor(Vector3(5499.146f, 4156.794f, 27.2651f)), owning_building_guid = 37)
      LocalObject(1024, Door.Constructor(Vector3(5499.146f, 4173.204f, 27.2651f)), owning_building_guid = 37)
      LocalObject(558, IFFLock.Constructor(Vector3(5497.957f, 4176.811f, 37.3901f), Vector3(0, 0, 0)), owning_building_guid = 37, door_guid = 336)
      LocalObject(559, IFFLock.Constructor(Vector3(5497.957f, 4176.811f, 57.39011f), Vector3(0, 0, 0)), owning_building_guid = 37, door_guid = 337)
      LocalObject(560, IFFLock.Constructor(Vector3(5502.047f, 4159.189f, 37.3901f), Vector3(0, 0, 180)), owning_building_guid = 37, door_guid = 334)
      LocalObject(561, IFFLock.Constructor(Vector3(5502.047f, 4159.189f, 57.39011f), Vector3(0, 0, 180)), owning_building_guid = 37, door_guid = 335)
      LocalObject(634, Locker.Constructor(Vector3(5503.716f, 4152.963f, 25.9231f)), owning_building_guid = 37)
      LocalObject(635, Locker.Constructor(Vector3(5503.751f, 4174.835f, 25.9231f)), owning_building_guid = 37)
      LocalObject(636, Locker.Constructor(Vector3(5505.053f, 4152.963f, 25.9231f)), owning_building_guid = 37)
      LocalObject(637, Locker.Constructor(Vector3(5505.088f, 4174.835f, 25.9231f)), owning_building_guid = 37)
      LocalObject(638, Locker.Constructor(Vector3(5507.741f, 4152.963f, 25.9231f)), owning_building_guid = 37)
      LocalObject(639, Locker.Constructor(Vector3(5507.741f, 4174.835f, 25.9231f)), owning_building_guid = 37)
      LocalObject(640, Locker.Constructor(Vector3(5509.143f, 4152.963f, 25.9231f)), owning_building_guid = 37)
      LocalObject(641, Locker.Constructor(Vector3(5509.143f, 4174.835f, 25.9231f)), owning_building_guid = 37)
      LocalObject(910, Terminal.Constructor(order_terminal), owning_building_guid = 37)
      LocalObject(911, Terminal.Constructor(order_terminal), owning_building_guid = 37)
      LocalObject(912, Terminal.Constructor(order_terminal), owning_building_guid = 37)
      LocalObject(993, SpawnTube.Constructor(respawn_tube_tower, Vector3(5498.706f, 4155.742f, 25.4111f), Vector3(0, 0, 0)), owning_building_guid = 37)
      LocalObject(994, SpawnTube.Constructor(respawn_tube_tower, Vector3(5498.706f, 4172.152f, 25.4111f), Vector3(0, 0, 0)), owning_building_guid = 37)
      LocalObject(939, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(5486.907f, 4162.725f, 63.4991f)), owning_building_guid = 37)
      LocalObject(940, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 37)
      LocalObject(942, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(5486.907f, 4173.17f, 63.4991f)), owning_building_guid = 37)
      LocalObject(943, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 37)
      LocalObject(656, FacilityTurret.Constructor(manned_turret, Vector3(5473.07f, 4153.045f, 54.8711f)), owning_building_guid = 37)
      TurretToWeapon(656, 5014)
      LocalObject(657, FacilityTurret.Constructor(manned_turret, Vector3(5511.497f, 4182.957f, 54.8711f)), owning_building_guid = 37)
      TurretToWeapon(657, 5015)
    }

    Building14()

    def Building14(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 38, MapID: 14
      LocalBuilding(38, 14, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2728f, 5288f, 35.99899f))))
      LocalObject(216, Door.Constructor(Vector3(2759.958f, 5246.54f, 38.07499f)), owning_building_guid = 38)
      LocalObject(217, Door.Constructor(Vector3(2764.546f, 5241.93f, 38.07499f)), owning_building_guid = 38)
      LocalObject(218, Door.Constructor(Vector3(2764.7f, 5251.282f, 38.07499f)), owning_building_guid = 38)
      LocalObject(219, Door.Constructor(Vector3(2769.288f, 5246.672f, 38.07499f)), owning_building_guid = 38)
      LocalObject(220, Door.Constructor(Vector3(2769.442f, 5256.024f, 38.07499f)), owning_building_guid = 38)
      LocalObject(221, Door.Constructor(Vector3(2774.03f, 5251.413f, 38.07499f)), owning_building_guid = 38)
      LocalObject(338, Door.Constructor(Vector3(2766.455f, 5232.55f, 37.70899f)), owning_building_guid = 38)
      LocalObject(339, Door.Constructor(Vector3(2783.464f, 5249.559f, 37.70899f)), owning_building_guid = 38)
      LocalObject(386, Door.Constructor(Vector3(2713.84f, 5251.231f, 39.17399f)), owning_building_guid = 38)
      LocalObject(387, Door.Constructor(Vector3(2728.005f, 5259.768f, 39.17399f)), owning_building_guid = 38)
      LocalObject(388, Door.Constructor(Vector3(2742.143f, 5251.248f, 39.17399f)), owning_building_guid = 38)
      LocalObject(389, Door.Constructor(Vector3(2756.232f, 5288.005f, 39.17399f)), owning_building_guid = 38)
      LocalObject(390, Door.Constructor(Vector3(2764.752f, 5302.143f, 39.17399f)), owning_building_guid = 38)
      LocalObject(391, Door.Constructor(Vector3(2764.769f, 5273.84f, 39.17399f)), owning_building_guid = 38)
      LocalObject(772, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(773, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(774, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(775, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(776, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(777, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(700, SpawnTube.Constructor(Vector3(2760.571f, 5245.914f, 38.03099f), Vector3(0, 0, 315)), owning_building_guid = 38)
      LocalObject(701, SpawnTube.Constructor(Vector3(2763.933f, 5242.552f, 38.03099f), Vector3(0, 0, 135)), owning_building_guid = 38)
      LocalObject(702, SpawnTube.Constructor(Vector3(2765.314f, 5250.658f, 38.03099f), Vector3(0, 0, 315)), owning_building_guid = 38)
      LocalObject(703, SpawnTube.Constructor(Vector3(2768.677f, 5247.296f, 38.03099f), Vector3(0, 0, 135)), owning_building_guid = 38)
      LocalObject(704, SpawnTube.Constructor(Vector3(2770.058f, 5255.401f, 38.03099f), Vector3(0, 0, 315)), owning_building_guid = 38)
      LocalObject(705, SpawnTube.Constructor(Vector3(2773.42f, 5252.039f, 38.03099f), Vector3(0, 0, 135)), owning_building_guid = 38)
    }

    Building43()

    def Building43(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 39, MapID: 43
      LocalBuilding(39, 43, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2798f, 3038f, 34.92469f))))
      LocalObject(222, Door.Constructor(Vector3(2849.914f, 3031.281f, 37.00069f)), owning_building_guid = 39)
      LocalObject(223, Door.Constructor(Vector3(2849.914f, 3037.987f, 37.00069f)), owning_building_guid = 39)
      LocalObject(224, Door.Constructor(Vector3(2849.914f, 3044.693f, 37.00069f)), owning_building_guid = 39)
      LocalObject(225, Door.Constructor(Vector3(2856.418f, 3037.972f, 37.00069f)), owning_building_guid = 39)
      LocalObject(226, Door.Constructor(Vector3(2856.418f, 3031.266f, 37.00069f)), owning_building_guid = 39)
      LocalObject(227, Door.Constructor(Vector3(2856.419f, 3044.677f, 37.00069f)), owning_building_guid = 39)
      LocalObject(342, Door.Constructor(Vector3(2864.401f, 3025.983f, 36.63469f)), owning_building_guid = 39)
      LocalObject(343, Door.Constructor(Vector3(2864.401f, 3050.037f, 36.63469f)), owning_building_guid = 39)
      LocalObject(392, Door.Constructor(Vector3(2813.987f, 3001.988f, 38.09969f)), owning_building_guid = 39)
      LocalObject(393, Door.Constructor(Vector3(2813.987f, 3073.988f, 38.09969f)), owning_building_guid = 39)
      LocalObject(394, Door.Constructor(Vector3(2817.959f, 3057.967f, 38.09969f)), owning_building_guid = 39)
      LocalObject(395, Door.Constructor(Vector3(2817.967f, 3018.041f, 38.09969f)), owning_building_guid = 39)
      LocalObject(396, Door.Constructor(Vector3(2833.988f, 3022.013f, 38.09969f)), owning_building_guid = 39)
      LocalObject(397, Door.Constructor(Vector3(2834.012f, 3053.987f, 38.09969f)), owning_building_guid = 39)
      LocalObject(784, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(785, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(786, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(791, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(792, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(793, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(706, SpawnTube.Constructor(Vector3(2850.79f, 3037.98f, 36.95669f), Vector3(0, 0, 270)), owning_building_guid = 39)
      LocalObject(707, SpawnTube.Constructor(Vector3(2850.791f, 3031.272f, 36.95669f), Vector3(0, 0, 270)), owning_building_guid = 39)
      LocalObject(708, SpawnTube.Constructor(Vector3(2850.791f, 3044.688f, 36.95669f), Vector3(0, 0, 270)), owning_building_guid = 39)
      LocalObject(709, SpawnTube.Constructor(Vector3(2855.545f, 3031.272f, 36.95669f), Vector3(0, 0, 90)), owning_building_guid = 39)
      LocalObject(710, SpawnTube.Constructor(Vector3(2855.545f, 3037.981f, 36.95669f), Vector3(0, 0, 90)), owning_building_guid = 39)
      LocalObject(711, SpawnTube.Constructor(Vector3(2855.545f, 3044.688f, 36.95669f), Vector3(0, 0, 90)), owning_building_guid = 39)
    }

    Building13()

    def Building13(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 40, MapID: 13
      LocalBuilding(40, 13, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2920f, 5052f, 35.99899f))))
      LocalObject(228, Door.Constructor(Vector3(2913.307f, 5103.915f, 38.07499f)), owning_building_guid = 40)
      LocalObject(229, Door.Constructor(Vector3(2913.323f, 5110.419f, 38.07499f)), owning_building_guid = 40)
      LocalObject(230, Door.Constructor(Vector3(2920.013f, 5103.915f, 38.07499f)), owning_building_guid = 40)
      LocalObject(231, Door.Constructor(Vector3(2920.028f, 5110.418f, 38.07499f)), owning_building_guid = 40)
      LocalObject(232, Door.Constructor(Vector3(2926.719f, 5103.915f, 38.07499f)), owning_building_guid = 40)
      LocalObject(233, Door.Constructor(Vector3(2926.734f, 5110.418f, 38.07499f)), owning_building_guid = 40)
      LocalObject(344, Door.Constructor(Vector3(2907.963f, 5118.401f, 37.70899f)), owning_building_guid = 40)
      LocalObject(347, Door.Constructor(Vector3(2932.017f, 5118.401f, 37.70899f)), owning_building_guid = 40)
      LocalObject(398, Door.Constructor(Vector3(2884.012f, 5067.987f, 39.17399f)), owning_building_guid = 40)
      LocalObject(399, Door.Constructor(Vector3(2900.033f, 5071.959f, 39.17399f)), owning_building_guid = 40)
      LocalObject(400, Door.Constructor(Vector3(2904.013f, 5088.012f, 39.17399f)), owning_building_guid = 40)
      LocalObject(401, Door.Constructor(Vector3(2935.987f, 5087.988f, 39.17399f)), owning_building_guid = 40)
      LocalObject(402, Door.Constructor(Vector3(2939.959f, 5071.967f, 39.17399f)), owning_building_guid = 40)
      LocalObject(403, Door.Constructor(Vector3(2956.012f, 5067.987f, 39.17399f)), owning_building_guid = 40)
      LocalObject(800, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(801, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(802, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(803, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(804, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(805, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(712, SpawnTube.Constructor(Vector3(2913.312f, 5104.791f, 38.03099f), Vector3(0, 0, 180)), owning_building_guid = 40)
      LocalObject(713, SpawnTube.Constructor(Vector3(2913.312f, 5109.545f, 38.03099f), Vector3(0, 0, 0)), owning_building_guid = 40)
      LocalObject(714, SpawnTube.Constructor(Vector3(2920.019f, 5109.545f, 38.03099f), Vector3(0, 0, 0)), owning_building_guid = 40)
      LocalObject(715, SpawnTube.Constructor(Vector3(2920.02f, 5104.79f, 38.03099f), Vector3(0, 0, 180)), owning_building_guid = 40)
      LocalObject(716, SpawnTube.Constructor(Vector3(2926.728f, 5104.791f, 38.03099f), Vector3(0, 0, 180)), owning_building_guid = 40)
      LocalObject(717, SpawnTube.Constructor(Vector3(2926.728f, 5109.545f, 38.03099f), Vector3(0, 0, 0)), owning_building_guid = 40)
    }

    Building15()

    def Building15(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 41, MapID: 15
      LocalBuilding(41, 15, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3112f, 5286f, 35.99899f))))
      LocalObject(234, Door.Constructor(Vector3(3065.93f, 5249.454f, 38.07499f)), owning_building_guid = 41)
      LocalObject(235, Door.Constructor(Vector3(3070.54f, 5254.042f, 38.07499f)), owning_building_guid = 41)
      LocalObject(236, Door.Constructor(Vector3(3070.672f, 5244.712f, 38.07499f)), owning_building_guid = 41)
      LocalObject(237, Door.Constructor(Vector3(3075.282f, 5249.3f, 38.07499f)), owning_building_guid = 41)
      LocalObject(238, Door.Constructor(Vector3(3075.413f, 5239.97f, 38.07499f)), owning_building_guid = 41)
      LocalObject(239, Door.Constructor(Vector3(3080.024f, 5244.558f, 38.07499f)), owning_building_guid = 41)
      LocalObject(350, Door.Constructor(Vector3(3056.55f, 5247.545f, 37.70899f)), owning_building_guid = 41)
      LocalObject(351, Door.Constructor(Vector3(3073.559f, 5230.536f, 37.70899f)), owning_building_guid = 41)
      LocalObject(404, Door.Constructor(Vector3(3075.231f, 5300.16f, 39.17399f)), owning_building_guid = 41)
      LocalObject(405, Door.Constructor(Vector3(3075.248f, 5271.857f, 39.17399f)), owning_building_guid = 41)
      LocalObject(406, Door.Constructor(Vector3(3083.768f, 5285.995f, 39.17399f)), owning_building_guid = 41)
      LocalObject(407, Door.Constructor(Vector3(3097.84f, 5249.231f, 39.17399f)), owning_building_guid = 41)
      LocalObject(408, Door.Constructor(Vector3(3112.005f, 5257.768f, 39.17399f)), owning_building_guid = 41)
      LocalObject(409, Door.Constructor(Vector3(3126.143f, 5249.248f, 39.17399f)), owning_building_guid = 41)
      LocalObject(832, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(833, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(834, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(835, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(838, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(839, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(718, SpawnTube.Constructor(Vector3(3066.552f, 5250.067f, 38.03099f), Vector3(0, 0, 225)), owning_building_guid = 41)
      LocalObject(719, SpawnTube.Constructor(Vector3(3069.914f, 5253.429f, 38.03099f), Vector3(0, 0, 45)), owning_building_guid = 41)
      LocalObject(720, SpawnTube.Constructor(Vector3(3071.296f, 5245.323f, 38.03099f), Vector3(0, 0, 225)), owning_building_guid = 41)
      LocalObject(721, SpawnTube.Constructor(Vector3(3074.658f, 5248.686f, 38.03099f), Vector3(0, 0, 45)), owning_building_guid = 41)
      LocalObject(722, SpawnTube.Constructor(Vector3(3076.039f, 5240.58f, 38.03099f), Vector3(0, 0, 225)), owning_building_guid = 41)
      LocalObject(723, SpawnTube.Constructor(Vector3(3079.401f, 5243.942f, 38.03099f), Vector3(0, 0, 45)), owning_building_guid = 41)
    }

    Building42()

    def Building42(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 42, MapID: 42
      LocalBuilding(42, 42, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3164f, 3102f, 34.9224f))))
      LocalObject(246, Door.Constructor(Vector3(3157.266f, 3043.582f, 36.9984f)), owning_building_guid = 42)
      LocalObject(247, Door.Constructor(Vector3(3157.281f, 3050.086f, 36.9984f)), owning_building_guid = 42)
      LocalObject(248, Door.Constructor(Vector3(3163.972f, 3043.582f, 36.9984f)), owning_building_guid = 42)
      LocalObject(249, Door.Constructor(Vector3(3163.987f, 3050.086f, 36.9984f)), owning_building_guid = 42)
      LocalObject(250, Door.Constructor(Vector3(3170.677f, 3043.581f, 36.9984f)), owning_building_guid = 42)
      LocalObject(251, Door.Constructor(Vector3(3170.693f, 3050.086f, 36.9984f)), owning_building_guid = 42)
      LocalObject(356, Door.Constructor(Vector3(3151.983f, 3035.599f, 36.6324f)), owning_building_guid = 42)
      LocalObject(357, Door.Constructor(Vector3(3176.037f, 3035.599f, 36.6324f)), owning_building_guid = 42)
      LocalObject(410, Door.Constructor(Vector3(3127.988f, 3086.013f, 38.0974f)), owning_building_guid = 42)
      LocalObject(411, Door.Constructor(Vector3(3144.041f, 3082.033f, 38.0974f)), owning_building_guid = 42)
      LocalObject(412, Door.Constructor(Vector3(3148.013f, 3066.012f, 38.0974f)), owning_building_guid = 42)
      LocalObject(417, Door.Constructor(Vector3(3179.987f, 3065.988f, 38.0974f)), owning_building_guid = 42)
      LocalObject(420, Door.Constructor(Vector3(3183.967f, 3082.041f, 38.0974f)), owning_building_guid = 42)
      LocalObject(421, Door.Constructor(Vector3(3199.988f, 3086.013f, 38.0974f)), owning_building_guid = 42)
      LocalObject(848, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(849, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(850, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(851, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(852, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(853, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(730, SpawnTube.Constructor(Vector3(3157.272f, 3044.455f, 36.9544f), Vector3(0, 0, 180)), owning_building_guid = 42)
      LocalObject(731, SpawnTube.Constructor(Vector3(3157.272f, 3049.209f, 36.9544f), Vector3(0, 0, 0)), owning_building_guid = 42)
      LocalObject(732, SpawnTube.Constructor(Vector3(3163.98f, 3049.21f, 36.9544f), Vector3(0, 0, 0)), owning_building_guid = 42)
      LocalObject(733, SpawnTube.Constructor(Vector3(3163.981f, 3044.455f, 36.9544f), Vector3(0, 0, 180)), owning_building_guid = 42)
      LocalObject(734, SpawnTube.Constructor(Vector3(3170.688f, 3044.455f, 36.9544f), Vector3(0, 0, 180)), owning_building_guid = 42)
      LocalObject(735, SpawnTube.Constructor(Vector3(3170.688f, 3049.209f, 36.9544f), Vector3(0, 0, 0)), owning_building_guid = 42)
    }

    Building41()

    def Building41(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 43, MapID: 41
      LocalBuilding(43, 41, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3196f, 2882f, 34.92251f))))
      LocalObject(240, Door.Constructor(Vector3(3137.581f, 2875.323f, 36.99851f)), owning_building_guid = 43)
      LocalObject(241, Door.Constructor(Vector3(3137.582f, 2888.734f, 36.99851f)), owning_building_guid = 43)
      LocalObject(242, Door.Constructor(Vector3(3137.582f, 2882.028f, 36.99851f)), owning_building_guid = 43)
      LocalObject(243, Door.Constructor(Vector3(3144.086f, 2875.307f, 36.99851f)), owning_building_guid = 43)
      LocalObject(244, Door.Constructor(Vector3(3144.086f, 2882.013f, 36.99851f)), owning_building_guid = 43)
      LocalObject(245, Door.Constructor(Vector3(3144.086f, 2888.719f, 36.99851f)), owning_building_guid = 43)
      LocalObject(354, Door.Constructor(Vector3(3129.599f, 2869.963f, 36.63251f)), owning_building_guid = 43)
      LocalObject(355, Door.Constructor(Vector3(3129.599f, 2894.017f, 36.63251f)), owning_building_guid = 43)
      LocalObject(413, Door.Constructor(Vector3(3159.988f, 2866.013f, 38.09751f)), owning_building_guid = 43)
      LocalObject(414, Door.Constructor(Vector3(3160.012f, 2897.987f, 38.09751f)), owning_building_guid = 43)
      LocalObject(415, Door.Constructor(Vector3(3176.033f, 2901.959f, 38.09751f)), owning_building_guid = 43)
      LocalObject(416, Door.Constructor(Vector3(3176.041f, 2862.033f, 38.09751f)), owning_building_guid = 43)
      LocalObject(418, Door.Constructor(Vector3(3180.013f, 2846.012f, 38.09751f)), owning_building_guid = 43)
      LocalObject(419, Door.Constructor(Vector3(3180.013f, 2918.012f, 38.09751f)), owning_building_guid = 43)
      LocalObject(842, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(843, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(844, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(845, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(846, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(847, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(724, SpawnTube.Constructor(Vector3(3138.455f, 2875.312f, 36.95451f), Vector3(0, 0, 270)), owning_building_guid = 43)
      LocalObject(725, SpawnTube.Constructor(Vector3(3138.455f, 2882.019f, 36.95451f), Vector3(0, 0, 270)), owning_building_guid = 43)
      LocalObject(726, SpawnTube.Constructor(Vector3(3138.455f, 2888.728f, 36.95451f), Vector3(0, 0, 270)), owning_building_guid = 43)
      LocalObject(727, SpawnTube.Constructor(Vector3(3143.209f, 2875.312f, 36.95451f), Vector3(0, 0, 90)), owning_building_guid = 43)
      LocalObject(728, SpawnTube.Constructor(Vector3(3143.209f, 2888.728f, 36.95451f), Vector3(0, 0, 90)), owning_building_guid = 43)
      LocalObject(729, SpawnTube.Constructor(Vector3(3143.21f, 2882.02f, 36.95451f), Vector3(0, 0, 90)), owning_building_guid = 43)
    }

    Building25()

    def Building25(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 44, MapID: 25
      LocalBuilding(44, 25, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5026f, 3908f, 35.9291f))))
      LocalObject(252, Door.Constructor(Vector3(5077.915f, 3901.281f, 38.0051f)), owning_building_guid = 44)
      LocalObject(253, Door.Constructor(Vector3(5077.915f, 3907.987f, 38.0051f)), owning_building_guid = 44)
      LocalObject(254, Door.Constructor(Vector3(5077.915f, 3914.693f, 38.0051f)), owning_building_guid = 44)
      LocalObject(255, Door.Constructor(Vector3(5084.418f, 3901.266f, 38.0051f)), owning_building_guid = 44)
      LocalObject(256, Door.Constructor(Vector3(5084.418f, 3907.972f, 38.0051f)), owning_building_guid = 44)
      LocalObject(257, Door.Constructor(Vector3(5084.419f, 3914.677f, 38.0051f)), owning_building_guid = 44)
      LocalObject(358, Door.Constructor(Vector3(5092.401f, 3895.983f, 37.6391f)), owning_building_guid = 44)
      LocalObject(359, Door.Constructor(Vector3(5092.401f, 3920.037f, 37.6391f)), owning_building_guid = 44)
      LocalObject(422, Door.Constructor(Vector3(5041.987f, 3871.988f, 39.1041f)), owning_building_guid = 44)
      LocalObject(423, Door.Constructor(Vector3(5041.987f, 3943.988f, 39.1041f)), owning_building_guid = 44)
      LocalObject(424, Door.Constructor(Vector3(5045.959f, 3927.967f, 39.1041f)), owning_building_guid = 44)
      LocalObject(425, Door.Constructor(Vector3(5045.967f, 3888.041f, 39.1041f)), owning_building_guid = 44)
      LocalObject(426, Door.Constructor(Vector3(5061.988f, 3892.013f, 39.1041f)), owning_building_guid = 44)
      LocalObject(427, Door.Constructor(Vector3(5062.012f, 3923.987f, 39.1041f)), owning_building_guid = 44)
      LocalObject(872, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(873, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(874, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(875, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(876, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(877, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(736, SpawnTube.Constructor(Vector3(5078.79f, 3907.98f, 37.96111f), Vector3(0, 0, 270)), owning_building_guid = 44)
      LocalObject(737, SpawnTube.Constructor(Vector3(5078.791f, 3901.272f, 37.96111f), Vector3(0, 0, 270)), owning_building_guid = 44)
      LocalObject(738, SpawnTube.Constructor(Vector3(5078.791f, 3914.688f, 37.96111f), Vector3(0, 0, 270)), owning_building_guid = 44)
      LocalObject(739, SpawnTube.Constructor(Vector3(5083.545f, 3901.272f, 37.96111f), Vector3(0, 0, 90)), owning_building_guid = 44)
      LocalObject(740, SpawnTube.Constructor(Vector3(5083.545f, 3907.981f, 37.96111f), Vector3(0, 0, 90)), owning_building_guid = 44)
      LocalObject(741, SpawnTube.Constructor(Vector3(5083.545f, 3914.688f, 37.96111f), Vector3(0, 0, 90)), owning_building_guid = 44)
    }

    Building23()

    def Building23(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 45, MapID: 23
      LocalBuilding(45, 23, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5230f, 3732f, 35.9291f))))
      LocalObject(258, Door.Constructor(Vector3(5223.307f, 3783.914f, 38.0051f)), owning_building_guid = 45)
      LocalObject(259, Door.Constructor(Vector3(5223.323f, 3790.419f, 38.0051f)), owning_building_guid = 45)
      LocalObject(260, Door.Constructor(Vector3(5230.013f, 3783.914f, 38.0051f)), owning_building_guid = 45)
      LocalObject(261, Door.Constructor(Vector3(5230.028f, 3790.418f, 38.0051f)), owning_building_guid = 45)
      LocalObject(262, Door.Constructor(Vector3(5236.719f, 3783.914f, 38.0051f)), owning_building_guid = 45)
      LocalObject(263, Door.Constructor(Vector3(5236.734f, 3790.418f, 38.0051f)), owning_building_guid = 45)
      LocalObject(362, Door.Constructor(Vector3(5217.963f, 3798.401f, 37.6391f)), owning_building_guid = 45)
      LocalObject(363, Door.Constructor(Vector3(5242.017f, 3798.401f, 37.6391f)), owning_building_guid = 45)
      LocalObject(428, Door.Constructor(Vector3(5194.012f, 3747.987f, 39.1041f)), owning_building_guid = 45)
      LocalObject(429, Door.Constructor(Vector3(5210.033f, 3751.959f, 39.1041f)), owning_building_guid = 45)
      LocalObject(430, Door.Constructor(Vector3(5214.013f, 3768.012f, 39.1041f)), owning_building_guid = 45)
      LocalObject(431, Door.Constructor(Vector3(5245.987f, 3767.988f, 39.1041f)), owning_building_guid = 45)
      LocalObject(432, Door.Constructor(Vector3(5249.959f, 3751.967f, 39.1041f)), owning_building_guid = 45)
      LocalObject(433, Door.Constructor(Vector3(5266.012f, 3747.987f, 39.1041f)), owning_building_guid = 45)
      LocalObject(888, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(889, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(890, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(891, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(892, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(893, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(742, SpawnTube.Constructor(Vector3(5223.312f, 3784.791f, 37.96111f), Vector3(0, 0, 180)), owning_building_guid = 45)
      LocalObject(743, SpawnTube.Constructor(Vector3(5223.312f, 3789.545f, 37.96111f), Vector3(0, 0, 0)), owning_building_guid = 45)
      LocalObject(744, SpawnTube.Constructor(Vector3(5230.019f, 3789.545f, 37.96111f), Vector3(0, 0, 0)), owning_building_guid = 45)
      LocalObject(745, SpawnTube.Constructor(Vector3(5230.02f, 3784.79f, 37.96111f), Vector3(0, 0, 180)), owning_building_guid = 45)
      LocalObject(746, SpawnTube.Constructor(Vector3(5236.728f, 3784.791f, 37.96111f), Vector3(0, 0, 180)), owning_building_guid = 45)
      LocalObject(747, SpawnTube.Constructor(Vector3(5236.728f, 3789.545f, 37.96111f), Vector3(0, 0, 0)), owning_building_guid = 45)
    }

    Building24()

    def Building24(): Unit = { // Name: VT_building_tr Type: VT_building_tr GUID: 46, MapID: 24
      LocalBuilding(46, 24, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5440f, 3906f, 35.9291f))))
      LocalObject(264, Door.Constructor(Vector3(5381.581f, 3899.323f, 38.0051f)), owning_building_guid = 46)
      LocalObject(265, Door.Constructor(Vector3(5381.582f, 3906.028f, 38.0051f)), owning_building_guid = 46)
      LocalObject(266, Door.Constructor(Vector3(5381.582f, 3912.734f, 38.0051f)), owning_building_guid = 46)
      LocalObject(267, Door.Constructor(Vector3(5388.085f, 3899.307f, 38.0051f)), owning_building_guid = 46)
      LocalObject(268, Door.Constructor(Vector3(5388.085f, 3906.013f, 38.0051f)), owning_building_guid = 46)
      LocalObject(269, Door.Constructor(Vector3(5388.085f, 3912.719f, 38.0051f)), owning_building_guid = 46)
      LocalObject(366, Door.Constructor(Vector3(5373.599f, 3893.963f, 37.6391f)), owning_building_guid = 46)
      LocalObject(367, Door.Constructor(Vector3(5373.599f, 3918.017f, 37.6391f)), owning_building_guid = 46)
      LocalObject(434, Door.Constructor(Vector3(5403.988f, 3890.013f, 39.1041f)), owning_building_guid = 46)
      LocalObject(435, Door.Constructor(Vector3(5404.012f, 3921.987f, 39.1041f)), owning_building_guid = 46)
      LocalObject(436, Door.Constructor(Vector3(5420.033f, 3925.959f, 39.1041f)), owning_building_guid = 46)
      LocalObject(437, Door.Constructor(Vector3(5420.041f, 3886.033f, 39.1041f)), owning_building_guid = 46)
      LocalObject(438, Door.Constructor(Vector3(5424.013f, 3870.012f, 39.1041f)), owning_building_guid = 46)
      LocalObject(439, Door.Constructor(Vector3(5424.013f, 3942.012f, 39.1041f)), owning_building_guid = 46)
      LocalObject(904, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(905, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(906, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(907, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(908, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(909, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(748, SpawnTube.Constructor(Vector3(5382.455f, 3899.312f, 37.96111f), Vector3(0, 0, 270)), owning_building_guid = 46)
      LocalObject(749, SpawnTube.Constructor(Vector3(5382.455f, 3906.019f, 37.96111f), Vector3(0, 0, 270)), owning_building_guid = 46)
      LocalObject(750, SpawnTube.Constructor(Vector3(5382.455f, 3912.728f, 37.96111f), Vector3(0, 0, 270)), owning_building_guid = 46)
      LocalObject(751, SpawnTube.Constructor(Vector3(5387.209f, 3899.312f, 37.96111f), Vector3(0, 0, 90)), owning_building_guid = 46)
      LocalObject(752, SpawnTube.Constructor(Vector3(5387.209f, 3912.728f, 37.96111f), Vector3(0, 0, 90)), owning_building_guid = 46)
      LocalObject(753, SpawnTube.Constructor(Vector3(5387.21f, 3906.02f, 37.96111f), Vector3(0, 0, 90)), owning_building_guid = 46)
    }

    Building8()

    def Building8(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 47, MapID: 8
      LocalBuilding(47, 8, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2754f, 5088f, 35.99899f))))
      LocalObject(282, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 47)
      LocalObject(270, VehicleSpawnPad.Constructor(Vector3(2756.568f, 5090.507f, 32.01399f), Vector3(0f, 0f, 0f)), owning_building_guid = 47, terminal_guid = 282)
    }

    Building5()

    def Building5(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 48, MapID: 5
      LocalBuilding(48, 5, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2780f, 5414f, 35.99899f))))
      LocalObject(283, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 48)
      LocalObject(271, VehicleSpawnPad.Constructor(Vector3(2782.508f, 5411.432f, 32.01399f), Vector3(0f, 0f, 0f)), owning_building_guid = 48, terminal_guid = 283)
    }

    Building44()

    def Building44(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 49, MapID: 44
      LocalBuilding(49, 44, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2818f, 2864f, 34.92273f))))
      LocalObject(284, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 49)
      LocalObject(272, VehicleSpawnPad.Constructor(Vector3(2821.589f, 2864.02f, 30.93773f), Vector3(0f, 0f, 0f)), owning_building_guid = 49, terminal_guid = 284)
    }

    Building48()

    def Building48(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 50, MapID: 48
      LocalBuilding(50, 48, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2848f, 3148f, 34.92273f))))
      LocalObject(285, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 50)
      LocalObject(273, VehicleSpawnPad.Constructor(Vector3(2848.02f, 3144.411f, 30.93773f), Vector3(0f, 0f, 0f)), owning_building_guid = 50, terminal_guid = 285)
    }

    Building49()

    def Building49(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 51, MapID: 49
      LocalBuilding(51, 49, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3050f, 3186f, 34.91574f))))
      LocalObject(287, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 51)
      LocalObject(274, VehicleSpawnPad.Constructor(Vector3(3050.02f, 3182.411f, 30.93074f), Vector3(0f, 0f, 0f)), owning_building_guid = 51, terminal_guid = 287)
    }

    Building6()

    def Building6(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 52, MapID: 6
      LocalBuilding(52, 6, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3062f, 5416f, 35.99899f))))
      LocalObject(286, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 52)
      LocalObject(275, VehicleSpawnPad.Constructor(Vector3(3059.432f, 5413.493f, 32.01399f), Vector3(0f, 0f, 0f)), owning_building_guid = 52, terminal_guid = 286)
    }

    Building46()

    def Building46(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 53, MapID: 46
      LocalBuilding(53, 46, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3088f, 2784f, 34.92098f))))
      LocalObject(289, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 53)
      LocalObject(277, VehicleSpawnPad.Constructor(Vector3(3087.98f, 2787.589f, 30.93598f), Vector3(0f, 0f, 0f)), owning_building_guid = 53, terminal_guid = 289)
    }

    Building7()

    def Building7(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 54, MapID: 7
      LocalBuilding(54, 7, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3090f, 5088f, 35.99899f))))
      LocalObject(288, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 54)
      LocalObject(276, VehicleSpawnPad.Constructor(Vector3(3087.492f, 5090.568f, 32.01399f), Vector3(0f, 0f, 0f)), owning_building_guid = 54, terminal_guid = 288)
    }

    Building26()

    def Building26(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 55, MapID: 26
      LocalBuilding(55, 26, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5050f, 3804f, 35.9291f))))
      LocalObject(291, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 55)
      LocalObject(279, VehicleSpawnPad.Constructor(Vector3(5053.589f, 3804.02f, 31.9441f), Vector3(0f, 0f, 0f)), owning_building_guid = 55, terminal_guid = 291)
    }

    Building30()

    def Building30(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 56, MapID: 30
      LocalBuilding(56, 30, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5050f, 4014f, 35.9291f))))
      LocalObject(290, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 56)
      LocalObject(278, VehicleSpawnPad.Constructor(Vector3(5050.02f, 4010.411f, 31.9441f), Vector3(0f, 0f, 0f)), owning_building_guid = 56, terminal_guid = 290)
    }

    Building31()

    def Building31(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 57, MapID: 31
      LocalBuilding(57, 31, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5414f, 4010f, 35.9291f))))
      LocalObject(293, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 57)
      LocalObject(281, VehicleSpawnPad.Constructor(Vector3(5414.02f, 4006.411f, 31.9441f), Vector3(0f, 0f, 0f)), owning_building_guid = 57, terminal_guid = 293)
    }

    Building28()

    def Building28(): Unit = { // Name: vt_dropship Type: vt_dropship GUID: 58, MapID: 28
      LocalBuilding(58, 28, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5416f, 3812f, 35.9291f))))
      LocalObject(292, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 58)
      LocalObject(280, VehicleSpawnPad.Constructor(Vector3(5412.411f, 3811.98f, 31.9441f), Vector3(0f, 0f, 0f)), owning_building_guid = 58, terminal_guid = 292)
    }

    Building17()

    def Building17(): Unit = { // Name: TR_NW_Tport_03 Type: vt_spawn GUID: 59, MapID: 17
      LocalBuilding(59, 17, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2842f, 5166f, 35.99899f))))
    }

    Building19()

    def Building19(): Unit = { // Name: TR_NW_Tport_01 Type: vt_spawn GUID: 60, MapID: 19
      LocalBuilding(60, 19, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2852f, 5312f, 35.99899f))))
    }

    Building54()

    def Building54(): Unit = { // Name: TR_SW_Tport_03 Type: vt_spawn GUID: 61, MapID: 54
      LocalBuilding(61, 54, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2928f, 2910f, 34.92174f))))
    }

    Building56()

    def Building56(): Unit = { // Name: TR_SW_Tport_01 Type: vt_spawn GUID: 62, MapID: 56
      LocalBuilding(62, 56, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2938f, 3056f, 34.91694f))))
    }

    Building16()

    def Building16(): Unit = { // Name: TR_NW_Tport_02 Type: vt_spawn GUID: 63, MapID: 16
      LocalBuilding(63, 16, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2990f, 5312f, 35.99899f))))
    }

    Building18()

    def Building18(): Unit = { // Name: TR_NW_Tport_04 Type: vt_spawn GUID: 64, MapID: 18
      LocalBuilding(64, 18, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2998f, 5166f, 35.99899f))))
    }

    Building57()

    def Building57(): Unit = { // Name: TR_SW_Tport_02 Type: vt_spawn GUID: 65, MapID: 57
      LocalBuilding(65, 57, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3076f, 3056f, 34.91595f))))
    }

    Building55()

    def Building55(): Unit = { // Name: TR_SW_Tport_04 Type: vt_spawn GUID: 66, MapID: 55
      LocalBuilding(66, 55, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3084f, 2910f, 34.91639f))))
    }

    Building36()

    def Building36(): Unit = { // Name: TR_E_Tport_03 Type: vt_spawn GUID: 67, MapID: 36
      LocalBuilding(67, 36, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5154f, 3834f, 35.9291f))))
    }

    Building38()

    def Building38(): Unit = { // Name: TR_E_Tport_01 Type: vt_spawn GUID: 68, MapID: 38
      LocalBuilding(68, 38, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5164f, 3982f, 35.9291f))))
    }

    Building39()

    def Building39(): Unit = { // Name: TR_E_Tport_02 Type: vt_spawn GUID: 69, MapID: 39
      LocalBuilding(69, 39, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5302f, 3982f, 35.9291f))))
    }

    Building37()

    def Building37(): Unit = { // Name: TR_E_Tport_04 Type: vt_spawn GUID: 70, MapID: 37
      LocalBuilding(70, 37, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5310f, 3834f, 35.9291f))))
    }

    Building11()

    def Building11(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 71, MapID: 11
      LocalBuilding(71, 11, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2780f, 5160f, 35.99899f))))
      LocalObject(1037, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 71)
      LocalObject(682, VehicleSpawnPad.Constructor(Vector3(2779.915f, 5159.877f, 34.52799f), Vector3(0f, 0f, 0f)), owning_building_guid = 71, terminal_guid = 1037)
    }

    Building12()

    def Building12(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 72, MapID: 12
      LocalBuilding(72, 12, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2826f, 5114f, 35.99899f))))
      LocalObject(1038, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 72)
      LocalObject(683, VehicleSpawnPad.Constructor(Vector3(2825.915f, 5113.877f, 34.52799f), Vector3(0f, 0f, 0f)), owning_building_guid = 72, terminal_guid = 1038)
    }

    Building47()

    def Building47(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 73, MapID: 47
      LocalBuilding(73, 47, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2848f, 2946f, 34.92273f))))
      LocalObject(1039, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 73)
      LocalObject(684, VehicleSpawnPad.Constructor(Vector3(2847.853f, 2945.973f, 33.45173f), Vector3(0f, 0f, 0f)), owning_building_guid = 73, terminal_guid = 1039)
    }

    Building20()

    def Building20(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 74, MapID: 20
      LocalBuilding(74, 20, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2874f, 5402f, 35.99899f))))
      LocalObject(1040, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 74)
      LocalObject(685, VehicleSpawnPad.Constructor(Vector3(2873.973f, 5402.147f, 34.52799f), Vector3(0f, 0f, 0f)), owning_building_guid = 74, terminal_guid = 1040)
    }

    Building52()

    def Building52(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 75, MapID: 52
      LocalBuilding(75, 52, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2908f, 2814f, 34.91781f))))
      LocalObject(1041, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 75)
      LocalObject(686, VehicleSpawnPad.Constructor(Vector3(2908.027f, 2813.853f, 33.44681f), Vector3(0f, 0f, 0f)), owning_building_guid = 75, terminal_guid = 1041)
    }

    Building53()

    def Building53(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 76, MapID: 53
      LocalBuilding(76, 53, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2934f, 3140f, 34.91683f))))
      LocalObject(1042, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 76)
      LocalObject(687, VehicleSpawnPad.Constructor(Vector3(2933.973f, 3140.147f, 33.44583f), Vector3(0f, 0f, 0f)), owning_building_guid = 76, terminal_guid = 1042)
    }

    Building21()

    def Building21(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 77, MapID: 21
      LocalBuilding(77, 21, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2962f, 5402f, 35.99899f))))
      LocalObject(1043, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 77)
      LocalObject(688, VehicleSpawnPad.Constructor(Vector3(2961.973f, 5402.147f, 34.52799f), Vector3(0f, 0f, 0f)), owning_building_guid = 77, terminal_guid = 1043)
    }

    Building45()

    def Building45(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 78, MapID: 45
      LocalBuilding(78, 45, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3012f, 2816f, 34.91574f))))
      LocalObject(1045, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 78)
      LocalObject(689, VehicleSpawnPad.Constructor(Vector3(3012.027f, 2815.853f, 33.44474f), Vector3(0f, 0f, 0f)), owning_building_guid = 78, terminal_guid = 1045)
    }

    Building10()

    def Building10(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 79, MapID: 10
      LocalBuilding(79, 10, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3018f, 5116f, 35.99899f))))
      LocalObject(1044, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 79)
      LocalObject(690, VehicleSpawnPad.Constructor(Vector3(3018.123f, 5115.915f, 34.52799f), Vector3(0f, 0f, 0f)), owning_building_guid = 79, terminal_guid = 1044)
    }

    Building9()

    def Building9(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 80, MapID: 9
      LocalBuilding(80, 9, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3064f, 5162f, 35.99899f))))
      LocalObject(1046, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 80)
      LocalObject(691, VehicleSpawnPad.Constructor(Vector3(3064.123f, 5161.915f, 34.52799f), Vector3(0f, 0f, 0f)), owning_building_guid = 80, terminal_guid = 1046)
    }

    Building51()

    def Building51(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 81, MapID: 51
      LocalBuilding(81, 51, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3136f, 3182f, 34.92273f))))
      LocalObject(1047, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 81)
      LocalObject(692, VehicleSpawnPad.Constructor(Vector3(3135.973f, 3182.147f, 33.45173f), Vector3(0f, 0f, 0f)), owning_building_guid = 81, terminal_guid = 1047)
    }

    Building50()

    def Building50(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 82, MapID: 50
      LocalBuilding(82, 50, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3222f, 2984f, 34.92273f))))
      LocalObject(1048, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 82)
      LocalObject(693, VehicleSpawnPad.Constructor(Vector3(3222.147f, 2984.027f, 33.45173f), Vector3(0f, 0f, 0f)), owning_building_guid = 82, terminal_guid = 1048)
    }

    Building32()

    def Building32(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 83, MapID: 32
      LocalBuilding(83, 32, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5118f, 4012f, 35.9291f))))
      LocalObject(1049, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 83)
      LocalObject(694, VehicleSpawnPad.Constructor(Vector3(5117.973f, 4012.147f, 34.4581f), Vector3(0f, 0f, 0f)), owning_building_guid = 83, terminal_guid = 1049)
    }

    Building27()

    def Building27(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 84, MapID: 27
      LocalBuilding(84, 27, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5126f, 3740f, 35.9291f))))
      LocalObject(1050, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 84)
      LocalObject(695, VehicleSpawnPad.Constructor(Vector3(5126.027f, 3739.853f, 34.4581f), Vector3(0f, 0f, 0f)), owning_building_guid = 84, terminal_guid = 1050)
    }

    Building34()

    def Building34(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 85, MapID: 34
      LocalBuilding(85, 34, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5202f, 4038f, 35.9291f))))
      LocalObject(1051, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 85)
      LocalObject(696, VehicleSpawnPad.Constructor(Vector3(5201.973f, 4038.147f, 34.4581f), Vector3(0f, 0f, 0f)), owning_building_guid = 85, terminal_guid = 1051)
    }

    Building35()

    def Building35(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 86, MapID: 35
      LocalBuilding(86, 35, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5258f, 4038f, 35.9291f))))
      LocalObject(1052, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 86)
      LocalObject(697, VehicleSpawnPad.Constructor(Vector3(5257.973f, 4038.147f, 34.4581f), Vector3(0f, 0f, 0f)), owning_building_guid = 86, terminal_guid = 1052)
    }

    Building29()

    def Building29(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 87, MapID: 29
      LocalBuilding(87, 29, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5338f, 3740f, 35.9291f))))
      LocalObject(1053, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 87)
      LocalObject(698, VehicleSpawnPad.Constructor(Vector3(5338.027f, 3739.853f, 34.4581f), Vector3(0f, 0f, 0f)), owning_building_guid = 87, terminal_guid = 1053)
    }

    Building33()

    def Building33(): Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 88, MapID: 33
      LocalBuilding(88, 33, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5342f, 4014f, 35.9291f))))
      LocalObject(1054, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 88)
      LocalObject(699, VehicleSpawnPad.Constructor(Vector3(5341.973f, 4014.147f, 34.4581f), Vector3(0f, 0f, 0f)), owning_building_guid = 88, terminal_guid = 1054)
    }
  }
}
