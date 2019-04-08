package zonemaps

import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.structures.{Building, FoundationBuilder, StructureType, WarpGate}
import net.psforever.objects.serverobject.terminals.{CaptureTerminal, ProximityTerminal, Terminal}
import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.objects.serverobject.turret.FacilityTurret
import net.psforever.objects.zones.ZoneMap
import net.psforever.types.Vector3

object Map13 {
  // HOME3 (VANU SOVREIGNTY SANCTUARY)
  val ZoneMap = new ZoneMap("map13") {
    Building22()

    def Building22() : Unit = { // Name: Hart_Ishundar Type: orbital_building_vs GUID: 1, MapID: 22
      LocalBuilding(1, 22, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2978f, 4834f, 56.08539f))))
      LocalObject(360, Door.Constructor(Vector3(2898f, 4821.99f, 60.19139f)), owning_building_guid = 1)
      LocalObject(361, Door.Constructor(Vector3(2898f, 4845.99f, 60.19139f)), owning_building_guid = 1)
      LocalObject(362, Door.Constructor(Vector3(3058f, 4822.01f, 60.19139f)), owning_building_guid = 1)
      LocalObject(363, Door.Constructor(Vector3(3058f, 4846.01f, 60.19139f)), owning_building_guid = 1)
      LocalObject(388, Door.Constructor(Vector3(2916.36f, 4834f, 60.19139f)), owning_building_guid = 1)
      LocalObject(389, Door.Constructor(Vector3(2916.36f, 4834f, 70.19139f)), owning_building_guid = 1)
      LocalObject(390, Door.Constructor(Vector3(2962f, 4786.01f, 75.19139f)), owning_building_guid = 1)
      LocalObject(391, Door.Constructor(Vector3(2994f, 4786.01f, 75.19139f)), owning_building_guid = 1)
      LocalObject(392, Door.Constructor(Vector3(3039.66f, 4834f, 60.19139f)), owning_building_guid = 1)
      LocalObject(393, Door.Constructor(Vector3(3039.66f, 4834f, 70.19139f)), owning_building_guid = 1)
      LocalObject(460, Door.Constructor(Vector3(2922f, 4814.01f, 70.19139f)), owning_building_guid = 1)
      LocalObject(461, Door.Constructor(Vector3(3034f, 4813.99f, 70.19139f)), owning_building_guid = 1)
      LocalObject(466, Door.Constructor(Vector3(2922f, 4822.015f, 60.19139f)), owning_building_guid = 1)
      LocalObject(467, Door.Constructor(Vector3(2922f, 4822.015f, 70.19139f)), owning_building_guid = 1)
      LocalObject(468, Door.Constructor(Vector3(2922f, 4846.015f, 60.19139f)), owning_building_guid = 1)
      LocalObject(469, Door.Constructor(Vector3(2922f, 4846.015f, 70.19139f)), owning_building_guid = 1)
      LocalObject(470, Door.Constructor(Vector3(3034f, 4821.985f, 60.19139f)), owning_building_guid = 1)
      LocalObject(471, Door.Constructor(Vector3(3034f, 4821.985f, 70.19139f)), owning_building_guid = 1)
      LocalObject(472, Door.Constructor(Vector3(3034f, 4845.985f, 60.19139f)), owning_building_guid = 1)
      LocalObject(473, Door.Constructor(Vector3(3034f, 4845.985f, 70.19139f)), owning_building_guid = 1)
      LocalObject(678, Locker.Constructor(Vector3(2973.36f, 4782.185f, 73.19539f)), owning_building_guid = 1)
      LocalObject(679, Locker.Constructor(Vector3(2974.724f, 4782.185f, 73.19539f)), owning_building_guid = 1)
      LocalObject(680, Locker.Constructor(Vector3(2976.066f, 4782.185f, 73.19539f)), owning_building_guid = 1)
      LocalObject(681, Locker.Constructor(Vector3(2977.386f, 4782.185f, 73.19539f)), owning_building_guid = 1)
      LocalObject(682, Locker.Constructor(Vector3(2978.675f, 4782.185f, 73.19539f)), owning_building_guid = 1)
      LocalObject(683, Locker.Constructor(Vector3(2979.951f, 4782.185f, 73.19539f)), owning_building_guid = 1)
      LocalObject(684, Locker.Constructor(Vector3(2981.268f, 4782.185f, 73.19539f)), owning_building_guid = 1)
      LocalObject(685, Locker.Constructor(Vector3(2982.598f, 4782.185f, 73.19539f)), owning_building_guid = 1)
      LocalObject(162, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(163, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(164, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(165, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(166, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(167, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
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
      LocalObject(180, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(181, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(182, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(183, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(184, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(185, Terminal.Constructor(cert_terminal), owning_building_guid = 1)
      LocalObject(798, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(799, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(800, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(801, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(802, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(803, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(804, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(805, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(806, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(807, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(808, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(809, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(810, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(811, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(812, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(813, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(814, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(815, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(816, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(817, Terminal.Constructor(order_terminal), owning_building_guid = 1)
      LocalObject(774, ProximityTerminal.Constructor(medical_terminal, Vector3(2907.307f, 4856.855f, 58.57639f)), owning_building_guid = 1)
      LocalObject(775, ProximityTerminal.Constructor(medical_terminal, Vector3(2907.369f, 4811.151f, 58.57639f)), owning_building_guid = 1)
      LocalObject(776, ProximityTerminal.Constructor(medical_terminal, Vector3(3048.667f, 4811.151f, 58.57639f)), owning_building_guid = 1)
      LocalObject(777, ProximityTerminal.Constructor(medical_terminal, Vector3(3048.689f, 4856.855f, 58.57639f)), owning_building_guid = 1)
      LocalObject(514, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(490, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(514, 490)
      LocalObject(515, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(491, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(515, 491)
      LocalObject(516, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(492, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(516, 492)
      LocalObject(517, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(493, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(517, 493)
      LocalObject(518, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(494, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(518, 494)
      LocalObject(519, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(495, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(519, 495)
      LocalObject(520, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(496, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(520, 496)
      LocalObject(521, ImplantTerminalMech.Constructor, owning_building_guid = 1)
      LocalObject(497, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 1)
      TerminalToInterface(521, 497)
    }

    Building4()

    def Building4() : Unit = { // Name: Hart_Esamir Type: orbital_building_vs GUID: 2, MapID: 4
      LocalBuilding(2, 4, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3688f, 2808f, 90.85312f))))
      LocalObject(370, Door.Constructor(Vector3(3608f, 2795.99f, 94.95912f)), owning_building_guid = 2)
      LocalObject(371, Door.Constructor(Vector3(3608f, 2819.99f, 94.95912f)), owning_building_guid = 2)
      LocalObject(374, Door.Constructor(Vector3(3768f, 2796.01f, 94.95912f)), owning_building_guid = 2)
      LocalObject(375, Door.Constructor(Vector3(3768f, 2820.01f, 94.95912f)), owning_building_guid = 2)
      LocalObject(394, Door.Constructor(Vector3(3626.36f, 2808f, 94.95912f)), owning_building_guid = 2)
      LocalObject(395, Door.Constructor(Vector3(3626.36f, 2808f, 104.9591f)), owning_building_guid = 2)
      LocalObject(396, Door.Constructor(Vector3(3672f, 2760.01f, 109.9591f)), owning_building_guid = 2)
      LocalObject(397, Door.Constructor(Vector3(3704f, 2760.01f, 109.9591f)), owning_building_guid = 2)
      LocalObject(398, Door.Constructor(Vector3(3749.66f, 2808f, 94.95912f)), owning_building_guid = 2)
      LocalObject(399, Door.Constructor(Vector3(3749.66f, 2808f, 104.9591f)), owning_building_guid = 2)
      LocalObject(462, Door.Constructor(Vector3(3632f, 2788.01f, 104.9591f)), owning_building_guid = 2)
      LocalObject(463, Door.Constructor(Vector3(3744f, 2787.99f, 104.9591f)), owning_building_guid = 2)
      LocalObject(474, Door.Constructor(Vector3(3632f, 2796.015f, 94.95912f)), owning_building_guid = 2)
      LocalObject(475, Door.Constructor(Vector3(3632f, 2796.015f, 104.9591f)), owning_building_guid = 2)
      LocalObject(476, Door.Constructor(Vector3(3632f, 2820.015f, 94.95912f)), owning_building_guid = 2)
      LocalObject(477, Door.Constructor(Vector3(3632f, 2820.015f, 104.9591f)), owning_building_guid = 2)
      LocalObject(478, Door.Constructor(Vector3(3744f, 2795.985f, 94.95912f)), owning_building_guid = 2)
      LocalObject(479, Door.Constructor(Vector3(3744f, 2795.985f, 104.9591f)), owning_building_guid = 2)
      LocalObject(480, Door.Constructor(Vector3(3744f, 2819.985f, 94.95912f)), owning_building_guid = 2)
      LocalObject(481, Door.Constructor(Vector3(3744f, 2819.985f, 104.9591f)), owning_building_guid = 2)
      LocalObject(686, Locker.Constructor(Vector3(3683.36f, 2756.185f, 107.9631f)), owning_building_guid = 2)
      LocalObject(687, Locker.Constructor(Vector3(3684.724f, 2756.185f, 107.9631f)), owning_building_guid = 2)
      LocalObject(688, Locker.Constructor(Vector3(3686.066f, 2756.185f, 107.9631f)), owning_building_guid = 2)
      LocalObject(689, Locker.Constructor(Vector3(3687.386f, 2756.185f, 107.9631f)), owning_building_guid = 2)
      LocalObject(690, Locker.Constructor(Vector3(3688.675f, 2756.185f, 107.9631f)), owning_building_guid = 2)
      LocalObject(691, Locker.Constructor(Vector3(3689.951f, 2756.185f, 107.9631f)), owning_building_guid = 2)
      LocalObject(692, Locker.Constructor(Vector3(3691.268f, 2756.185f, 107.9631f)), owning_building_guid = 2)
      LocalObject(693, Locker.Constructor(Vector3(3692.598f, 2756.185f, 107.9631f)), owning_building_guid = 2)
      LocalObject(186, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(187, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(188, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(189, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(190, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(191, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(192, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(193, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(194, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(195, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(196, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(197, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(198, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(199, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(200, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(201, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(202, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(203, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(204, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(205, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(206, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(207, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(208, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(209, Terminal.Constructor(cert_terminal), owning_building_guid = 2)
      LocalObject(842, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(843, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(844, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(845, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(846, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(847, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(848, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(849, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(850, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(851, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(861, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(862, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(863, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(864, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(865, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(866, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(867, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(868, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(869, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(870, Terminal.Constructor(order_terminal), owning_building_guid = 2)
      LocalObject(778, ProximityTerminal.Constructor(medical_terminal, Vector3(3617.307f, 2830.855f, 93.34412f)), owning_building_guid = 2)
      LocalObject(779, ProximityTerminal.Constructor(medical_terminal, Vector3(3617.369f, 2785.151f, 93.34412f)), owning_building_guid = 2)
      LocalObject(780, ProximityTerminal.Constructor(medical_terminal, Vector3(3758.667f, 2785.151f, 93.34412f)), owning_building_guid = 2)
      LocalObject(781, ProximityTerminal.Constructor(medical_terminal, Vector3(3758.689f, 2830.855f, 93.34412f)), owning_building_guid = 2)
      LocalObject(522, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(498, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(522, 498)
      LocalObject(523, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(499, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(523, 499)
      LocalObject(524, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(500, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(524, 500)
      LocalObject(525, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(501, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(525, 501)
      LocalObject(526, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(502, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(526, 502)
      LocalObject(527, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(503, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(527, 503)
      LocalObject(528, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(504, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(528, 504)
      LocalObject(529, ImplantTerminalMech.Constructor, owning_building_guid = 2)
      LocalObject(505, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 2)
      TerminalToInterface(529, 505)
    }

    Building40()

    def Building40() : Unit = { // Name: Hart_Hossin Type: orbital_building_vs GUID: 3, MapID: 40
      LocalBuilding(3, 40, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5610f, 4238f, 103.2289f))))
      LocalObject(382, Door.Constructor(Vector3(5530f, 4225.99f, 107.3349f)), owning_building_guid = 3)
      LocalObject(383, Door.Constructor(Vector3(5530f, 4249.99f, 107.3349f)), owning_building_guid = 3)
      LocalObject(384, Door.Constructor(Vector3(5690f, 4226.01f, 107.3349f)), owning_building_guid = 3)
      LocalObject(385, Door.Constructor(Vector3(5690f, 4250.01f, 107.3349f)), owning_building_guid = 3)
      LocalObject(400, Door.Constructor(Vector3(5548.36f, 4238f, 107.3349f)), owning_building_guid = 3)
      LocalObject(401, Door.Constructor(Vector3(5548.36f, 4238f, 117.3349f)), owning_building_guid = 3)
      LocalObject(402, Door.Constructor(Vector3(5594f, 4190.01f, 122.3349f)), owning_building_guid = 3)
      LocalObject(403, Door.Constructor(Vector3(5626f, 4190.01f, 122.3349f)), owning_building_guid = 3)
      LocalObject(404, Door.Constructor(Vector3(5671.66f, 4238f, 107.3349f)), owning_building_guid = 3)
      LocalObject(405, Door.Constructor(Vector3(5671.66f, 4238f, 117.3349f)), owning_building_guid = 3)
      LocalObject(464, Door.Constructor(Vector3(5554f, 4218.01f, 117.3349f)), owning_building_guid = 3)
      LocalObject(465, Door.Constructor(Vector3(5666f, 4217.99f, 117.3349f)), owning_building_guid = 3)
      LocalObject(482, Door.Constructor(Vector3(5554f, 4226.015f, 107.3349f)), owning_building_guid = 3)
      LocalObject(483, Door.Constructor(Vector3(5554f, 4226.015f, 117.3349f)), owning_building_guid = 3)
      LocalObject(484, Door.Constructor(Vector3(5554f, 4250.015f, 107.3349f)), owning_building_guid = 3)
      LocalObject(485, Door.Constructor(Vector3(5554f, 4250.015f, 117.3349f)), owning_building_guid = 3)
      LocalObject(486, Door.Constructor(Vector3(5666f, 4225.985f, 107.3349f)), owning_building_guid = 3)
      LocalObject(487, Door.Constructor(Vector3(5666f, 4225.985f, 117.3349f)), owning_building_guid = 3)
      LocalObject(488, Door.Constructor(Vector3(5666f, 4249.985f, 107.3349f)), owning_building_guid = 3)
      LocalObject(489, Door.Constructor(Vector3(5666f, 4249.985f, 117.3349f)), owning_building_guid = 3)
      LocalObject(694, Locker.Constructor(Vector3(5605.36f, 4186.185f, 120.3389f)), owning_building_guid = 3)
      LocalObject(695, Locker.Constructor(Vector3(5606.724f, 4186.185f, 120.3389f)), owning_building_guid = 3)
      LocalObject(696, Locker.Constructor(Vector3(5608.066f, 4186.185f, 120.3389f)), owning_building_guid = 3)
      LocalObject(697, Locker.Constructor(Vector3(5609.386f, 4186.185f, 120.3389f)), owning_building_guid = 3)
      LocalObject(698, Locker.Constructor(Vector3(5610.675f, 4186.185f, 120.3389f)), owning_building_guid = 3)
      LocalObject(699, Locker.Constructor(Vector3(5611.951f, 4186.185f, 120.3389f)), owning_building_guid = 3)
      LocalObject(700, Locker.Constructor(Vector3(5613.268f, 4186.185f, 120.3389f)), owning_building_guid = 3)
      LocalObject(701, Locker.Constructor(Vector3(5614.598f, 4186.185f, 120.3389f)), owning_building_guid = 3)
      LocalObject(210, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(211, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(212, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(213, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(214, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(215, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(216, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(217, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(218, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(219, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(220, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(221, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(222, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(223, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(224, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(225, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(226, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(227, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(228, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(229, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(230, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(231, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(232, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(233, Terminal.Constructor(cert_terminal), owning_building_guid = 3)
      LocalObject(904, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(905, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(906, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(907, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(908, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(909, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(910, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(911, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(912, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(913, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(914, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(915, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(916, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(917, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(918, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(919, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(920, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(921, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(922, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(923, Terminal.Constructor(order_terminal), owning_building_guid = 3)
      LocalObject(782, ProximityTerminal.Constructor(medical_terminal, Vector3(5539.307f, 4260.855f, 105.7199f)), owning_building_guid = 3)
      LocalObject(783, ProximityTerminal.Constructor(medical_terminal, Vector3(5539.369f, 4215.151f, 105.7199f)), owning_building_guid = 3)
      LocalObject(784, ProximityTerminal.Constructor(medical_terminal, Vector3(5680.667f, 4215.151f, 105.7199f)), owning_building_guid = 3)
      LocalObject(785, ProximityTerminal.Constructor(medical_terminal, Vector3(5680.689f, 4260.855f, 105.7199f)), owning_building_guid = 3)
      LocalObject(530, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(506, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(530, 506)
      LocalObject(531, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(507, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(531, 507)
      LocalObject(532, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(508, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(532, 508)
      LocalObject(533, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(509, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(533, 509)
      LocalObject(534, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(510, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(534, 510)
      LocalObject(535, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(511, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(535, 511)
      LocalObject(536, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(512, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(536, 512)
      LocalObject(537, ImplantTerminalMech.Constructor, owning_building_guid = 3)
      LocalObject(513, Terminal.Constructor(implant_terminal_interface), owning_building_guid = 3)
      TerminalToInterface(537, 513)
    }

    Building58()

    def Building58() : Unit = { // Name: S_Ishundar_WG_tower Type: tower_a GUID: 28, MapID: 58
      LocalBuilding(28, 58, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(2708f, 5084f, 56.14882f))))
      LocalObject(1015, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 28)
      LocalObject(312, Door.Constructor(Vector3(2720f, 5076f, 57.66982f)), owning_building_guid = 28)
      LocalObject(313, Door.Constructor(Vector3(2720f, 5076f, 77.66882f)), owning_building_guid = 28)
      LocalObject(314, Door.Constructor(Vector3(2720f, 5092f, 57.66982f)), owning_building_guid = 28)
      LocalObject(315, Door.Constructor(Vector3(2720f, 5092f, 77.66882f)), owning_building_guid = 28)
      LocalObject(1025, Door.Constructor(Vector3(2719.146f, 5072.794f, 47.48482f)), owning_building_guid = 28)
      LocalObject(1026, Door.Constructor(Vector3(2719.146f, 5089.204f, 47.48482f)), owning_building_guid = 28)
      LocalObject(538, IFFLock.Constructor(Vector3(2717.957f, 5092.811f, 57.60981f), Vector3(0, 0, 0)), owning_building_guid = 28, door_guid = 314)
      LocalObject(539, IFFLock.Constructor(Vector3(2717.957f, 5092.811f, 77.60982f), Vector3(0, 0, 0)), owning_building_guid = 28, door_guid = 315)
      LocalObject(540, IFFLock.Constructor(Vector3(2722.047f, 5075.189f, 57.60981f), Vector3(0, 0, 180)), owning_building_guid = 28, door_guid = 312)
      LocalObject(541, IFFLock.Constructor(Vector3(2722.047f, 5075.189f, 77.60982f), Vector3(0, 0, 180)), owning_building_guid = 28, door_guid = 313)
      LocalObject(584, Locker.Constructor(Vector3(2723.716f, 5068.963f, 46.14281f)), owning_building_guid = 28)
      LocalObject(585, Locker.Constructor(Vector3(2723.751f, 5090.835f, 46.14281f)), owning_building_guid = 28)
      LocalObject(586, Locker.Constructor(Vector3(2725.053f, 5068.963f, 46.14281f)), owning_building_guid = 28)
      LocalObject(587, Locker.Constructor(Vector3(2725.088f, 5090.835f, 46.14281f)), owning_building_guid = 28)
      LocalObject(588, Locker.Constructor(Vector3(2727.741f, 5068.963f, 46.14281f)), owning_building_guid = 28)
      LocalObject(589, Locker.Constructor(Vector3(2727.741f, 5090.835f, 46.14281f)), owning_building_guid = 28)
      LocalObject(590, Locker.Constructor(Vector3(2729.143f, 5068.963f, 46.14281f)), owning_building_guid = 28)
      LocalObject(591, Locker.Constructor(Vector3(2729.143f, 5090.835f, 46.14281f)), owning_building_guid = 28)
      LocalObject(789, Terminal.Constructor(order_terminal), owning_building_guid = 28)
      LocalObject(790, Terminal.Constructor(order_terminal), owning_building_guid = 28)
      LocalObject(791, Terminal.Constructor(order_terminal), owning_building_guid = 28)
      LocalObject(995, SpawnTube.Constructor(respawn_tube_tower, Vector3(2718.706f, 5071.742f, 45.63081f), Vector3(0, 0, 0)), owning_building_guid = 28)
      LocalObject(996, SpawnTube.Constructor(respawn_tube_tower, Vector3(2718.706f, 5088.152f, 45.63081f), Vector3(0, 0, 0)), owning_building_guid = 28)
      LocalObject(664, FacilityTurret.Constructor(manned_turret, Vector3(2695.32f, 5071.295f, 75.09081f)), owning_building_guid = 28)
      TurretToWeapon(664, 5000)
      LocalObject(665, FacilityTurret.Constructor(manned_turret, Vector3(2730.647f, 5096.707f, 75.09081f)), owning_building_guid = 28)
      TurretToWeapon(665, 5001)
    }

    Building63()

    def Building63() : Unit = { // Name: Esamir_WG_tower Type: tower_a GUID: 29, MapID: 63
      LocalBuilding(29, 63, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3968f, 2600f, 90.86123f))))
      LocalObject(1019, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 29)
      LocalObject(330, Door.Constructor(Vector3(3980f, 2592f, 92.38223f)), owning_building_guid = 29)
      LocalObject(331, Door.Constructor(Vector3(3980f, 2592f, 112.3812f)), owning_building_guid = 29)
      LocalObject(332, Door.Constructor(Vector3(3980f, 2608f, 92.38223f)), owning_building_guid = 29)
      LocalObject(333, Door.Constructor(Vector3(3980f, 2608f, 112.3812f)), owning_building_guid = 29)
      LocalObject(1033, Door.Constructor(Vector3(3979.146f, 2588.794f, 82.19723f)), owning_building_guid = 29)
      LocalObject(1034, Door.Constructor(Vector3(3979.146f, 2605.204f, 82.19723f)), owning_building_guid = 29)
      LocalObject(556, IFFLock.Constructor(Vector3(3977.957f, 2608.811f, 92.32223f), Vector3(0, 0, 0)), owning_building_guid = 29, door_guid = 332)
      LocalObject(557, IFFLock.Constructor(Vector3(3977.957f, 2608.811f, 112.3222f), Vector3(0, 0, 0)), owning_building_guid = 29, door_guid = 333)
      LocalObject(558, IFFLock.Constructor(Vector3(3982.047f, 2591.189f, 92.32223f), Vector3(0, 0, 180)), owning_building_guid = 29, door_guid = 330)
      LocalObject(559, IFFLock.Constructor(Vector3(3982.047f, 2591.189f, 112.3222f), Vector3(0, 0, 180)), owning_building_guid = 29, door_guid = 331)
      LocalObject(616, Locker.Constructor(Vector3(3983.716f, 2584.963f, 80.85523f)), owning_building_guid = 29)
      LocalObject(617, Locker.Constructor(Vector3(3983.751f, 2606.835f, 80.85523f)), owning_building_guid = 29)
      LocalObject(618, Locker.Constructor(Vector3(3985.053f, 2584.963f, 80.85523f)), owning_building_guid = 29)
      LocalObject(619, Locker.Constructor(Vector3(3985.088f, 2606.835f, 80.85523f)), owning_building_guid = 29)
      LocalObject(620, Locker.Constructor(Vector3(3987.741f, 2584.963f, 80.85523f)), owning_building_guid = 29)
      LocalObject(621, Locker.Constructor(Vector3(3987.741f, 2606.835f, 80.85523f)), owning_building_guid = 29)
      LocalObject(622, Locker.Constructor(Vector3(3989.143f, 2584.963f, 80.85523f)), owning_building_guid = 29)
      LocalObject(623, Locker.Constructor(Vector3(3989.143f, 2606.835f, 80.85523f)), owning_building_guid = 29)
      LocalObject(877, Terminal.Constructor(order_terminal), owning_building_guid = 29)
      LocalObject(878, Terminal.Constructor(order_terminal), owning_building_guid = 29)
      LocalObject(879, Terminal.Constructor(order_terminal), owning_building_guid = 29)
      LocalObject(1003, SpawnTube.Constructor(respawn_tube_tower, Vector3(3978.706f, 2587.742f, 80.34323f), Vector3(0, 0, 0)), owning_building_guid = 29)
      LocalObject(1004, SpawnTube.Constructor(respawn_tube_tower, Vector3(3978.706f, 2604.152f, 80.34323f), Vector3(0, 0, 0)), owning_building_guid = 29)
      LocalObject(670, FacilityTurret.Constructor(manned_turret, Vector3(3955.32f, 2587.295f, 109.8032f)), owning_building_guid = 29)
      TurretToWeapon(670, 5002)
      LocalObject(671, FacilityTurret.Constructor(manned_turret, Vector3(3990.647f, 2612.707f, 109.8032f)), owning_building_guid = 29)
      TurretToWeapon(671, 5003)
    }

    Building66()

    def Building66() : Unit = { // Name: SE_Hossin_WG_tower Type: tower_a GUID: 30, MapID: 66
      LocalBuilding(30, 66, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(6006f, 4414f, 97.87095f))))
      LocalObject(1024, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 30)
      LocalObject(354, Door.Constructor(Vector3(6018f, 4406f, 99.39195f)), owning_building_guid = 30)
      LocalObject(355, Door.Constructor(Vector3(6018f, 4406f, 119.3909f)), owning_building_guid = 30)
      LocalObject(356, Door.Constructor(Vector3(6018f, 4422f, 99.39195f)), owning_building_guid = 30)
      LocalObject(357, Door.Constructor(Vector3(6018f, 4422f, 119.3909f)), owning_building_guid = 30)
      LocalObject(1043, Door.Constructor(Vector3(6017.146f, 4402.794f, 89.20695f)), owning_building_guid = 30)
      LocalObject(1044, Door.Constructor(Vector3(6017.146f, 4419.204f, 89.20695f)), owning_building_guid = 30)
      LocalObject(580, IFFLock.Constructor(Vector3(6015.957f, 4422.811f, 99.33195f), Vector3(0, 0, 0)), owning_building_guid = 30, door_guid = 356)
      LocalObject(581, IFFLock.Constructor(Vector3(6015.957f, 4422.811f, 119.3319f), Vector3(0, 0, 0)), owning_building_guid = 30, door_guid = 357)
      LocalObject(582, IFFLock.Constructor(Vector3(6020.047f, 4405.189f, 99.33195f), Vector3(0, 0, 180)), owning_building_guid = 30, door_guid = 354)
      LocalObject(583, IFFLock.Constructor(Vector3(6020.047f, 4405.189f, 119.3319f), Vector3(0, 0, 180)), owning_building_guid = 30, door_guid = 355)
      LocalObject(656, Locker.Constructor(Vector3(6021.716f, 4398.963f, 87.86495f)), owning_building_guid = 30)
      LocalObject(657, Locker.Constructor(Vector3(6021.751f, 4420.835f, 87.86495f)), owning_building_guid = 30)
      LocalObject(658, Locker.Constructor(Vector3(6023.053f, 4398.963f, 87.86495f)), owning_building_guid = 30)
      LocalObject(659, Locker.Constructor(Vector3(6023.088f, 4420.835f, 87.86495f)), owning_building_guid = 30)
      LocalObject(660, Locker.Constructor(Vector3(6025.741f, 4398.963f, 87.86495f)), owning_building_guid = 30)
      LocalObject(661, Locker.Constructor(Vector3(6025.741f, 4420.835f, 87.86495f)), owning_building_guid = 30)
      LocalObject(662, Locker.Constructor(Vector3(6027.143f, 4398.963f, 87.86495f)), owning_building_guid = 30)
      LocalObject(663, Locker.Constructor(Vector3(6027.143f, 4420.835f, 87.86495f)), owning_building_guid = 30)
      LocalObject(930, Terminal.Constructor(order_terminal), owning_building_guid = 30)
      LocalObject(931, Terminal.Constructor(order_terminal), owning_building_guid = 30)
      LocalObject(932, Terminal.Constructor(order_terminal), owning_building_guid = 30)
      LocalObject(1013, SpawnTube.Constructor(respawn_tube_tower, Vector3(6016.706f, 4401.742f, 87.35295f), Vector3(0, 0, 0)), owning_building_guid = 30)
      LocalObject(1014, SpawnTube.Constructor(respawn_tube_tower, Vector3(6016.706f, 4418.152f, 87.35295f), Vector3(0, 0, 0)), owning_building_guid = 30)
      LocalObject(676, FacilityTurret.Constructor(manned_turret, Vector3(5993.32f, 4401.295f, 116.813f)), owning_building_guid = 30)
      TurretToWeapon(676, 5004)
      LocalObject(677, FacilityTurret.Constructor(manned_turret, Vector3(6028.647f, 4426.707f, 116.813f)), owning_building_guid = 30)
      TurretToWeapon(677, 5005)
    }

    Building60()

    def Building60() : Unit = { // Name: NW_Esamir_WG_tower Type: tower_b GUID: 31, MapID: 60
      LocalBuilding(31, 60, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3208f, 3524f, 92.25745f))))
      LocalObject(1016, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 31)
      LocalObject(316, Door.Constructor(Vector3(3220f, 3516f, 93.77745f)), owning_building_guid = 31)
      LocalObject(317, Door.Constructor(Vector3(3220f, 3516f, 103.7775f)), owning_building_guid = 31)
      LocalObject(318, Door.Constructor(Vector3(3220f, 3516f, 123.7775f)), owning_building_guid = 31)
      LocalObject(319, Door.Constructor(Vector3(3220f, 3532f, 93.77745f)), owning_building_guid = 31)
      LocalObject(320, Door.Constructor(Vector3(3220f, 3532f, 103.7775f)), owning_building_guid = 31)
      LocalObject(321, Door.Constructor(Vector3(3220f, 3532f, 123.7775f)), owning_building_guid = 31)
      LocalObject(1027, Door.Constructor(Vector3(3219.147f, 3512.794f, 83.59345f)), owning_building_guid = 31)
      LocalObject(1028, Door.Constructor(Vector3(3219.147f, 3529.204f, 83.59345f)), owning_building_guid = 31)
      LocalObject(542, IFFLock.Constructor(Vector3(3217.957f, 3532.811f, 93.71845f), Vector3(0, 0, 0)), owning_building_guid = 31, door_guid = 319)
      LocalObject(543, IFFLock.Constructor(Vector3(3217.957f, 3532.811f, 103.7185f), Vector3(0, 0, 0)), owning_building_guid = 31, door_guid = 320)
      LocalObject(544, IFFLock.Constructor(Vector3(3217.957f, 3532.811f, 123.7185f), Vector3(0, 0, 0)), owning_building_guid = 31, door_guid = 321)
      LocalObject(545, IFFLock.Constructor(Vector3(3222.047f, 3515.189f, 93.71845f), Vector3(0, 0, 180)), owning_building_guid = 31, door_guid = 316)
      LocalObject(546, IFFLock.Constructor(Vector3(3222.047f, 3515.189f, 103.7185f), Vector3(0, 0, 180)), owning_building_guid = 31, door_guid = 317)
      LocalObject(547, IFFLock.Constructor(Vector3(3222.047f, 3515.189f, 123.7185f), Vector3(0, 0, 180)), owning_building_guid = 31, door_guid = 318)
      LocalObject(592, Locker.Constructor(Vector3(3223.716f, 3508.963f, 82.25146f)), owning_building_guid = 31)
      LocalObject(593, Locker.Constructor(Vector3(3223.751f, 3530.835f, 82.25146f)), owning_building_guid = 31)
      LocalObject(594, Locker.Constructor(Vector3(3225.053f, 3508.963f, 82.25146f)), owning_building_guid = 31)
      LocalObject(595, Locker.Constructor(Vector3(3225.088f, 3530.835f, 82.25146f)), owning_building_guid = 31)
      LocalObject(596, Locker.Constructor(Vector3(3227.741f, 3508.963f, 82.25146f)), owning_building_guid = 31)
      LocalObject(597, Locker.Constructor(Vector3(3227.741f, 3530.835f, 82.25146f)), owning_building_guid = 31)
      LocalObject(598, Locker.Constructor(Vector3(3229.143f, 3508.963f, 82.25146f)), owning_building_guid = 31)
      LocalObject(599, Locker.Constructor(Vector3(3229.143f, 3530.835f, 82.25146f)), owning_building_guid = 31)
      LocalObject(830, Terminal.Constructor(order_terminal), owning_building_guid = 31)
      LocalObject(831, Terminal.Constructor(order_terminal), owning_building_guid = 31)
      LocalObject(832, Terminal.Constructor(order_terminal), owning_building_guid = 31)
      LocalObject(997, SpawnTube.Constructor(respawn_tube_tower, Vector3(3218.706f, 3511.742f, 81.73946f), Vector3(0, 0, 0)), owning_building_guid = 31)
      LocalObject(998, SpawnTube.Constructor(respawn_tube_tower, Vector3(3218.706f, 3528.152f, 81.73946f), Vector3(0, 0, 0)), owning_building_guid = 31)
    }

    Building64()

    def Building64() : Unit = { // Name: W_Hossin_WG_tower Type: tower_b GUID: 32, MapID: 64
      LocalBuilding(32, 64, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3988f, 4380f, 87.9162f))))
      LocalObject(1020, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 32)
      LocalObject(334, Door.Constructor(Vector3(4000f, 4372f, 89.4362f)), owning_building_guid = 32)
      LocalObject(335, Door.Constructor(Vector3(4000f, 4372f, 99.4362f)), owning_building_guid = 32)
      LocalObject(336, Door.Constructor(Vector3(4000f, 4372f, 119.4362f)), owning_building_guid = 32)
      LocalObject(337, Door.Constructor(Vector3(4000f, 4388f, 89.4362f)), owning_building_guid = 32)
      LocalObject(338, Door.Constructor(Vector3(4000f, 4388f, 99.4362f)), owning_building_guid = 32)
      LocalObject(339, Door.Constructor(Vector3(4000f, 4388f, 119.4362f)), owning_building_guid = 32)
      LocalObject(1035, Door.Constructor(Vector3(3999.147f, 4368.794f, 79.2522f)), owning_building_guid = 32)
      LocalObject(1036, Door.Constructor(Vector3(3999.147f, 4385.204f, 79.2522f)), owning_building_guid = 32)
      LocalObject(560, IFFLock.Constructor(Vector3(3997.957f, 4388.811f, 89.3772f), Vector3(0, 0, 0)), owning_building_guid = 32, door_guid = 337)
      LocalObject(561, IFFLock.Constructor(Vector3(3997.957f, 4388.811f, 99.3772f), Vector3(0, 0, 0)), owning_building_guid = 32, door_guid = 338)
      LocalObject(562, IFFLock.Constructor(Vector3(3997.957f, 4388.811f, 119.3772f), Vector3(0, 0, 0)), owning_building_guid = 32, door_guid = 339)
      LocalObject(563, IFFLock.Constructor(Vector3(4002.047f, 4371.189f, 89.3772f), Vector3(0, 0, 180)), owning_building_guid = 32, door_guid = 334)
      LocalObject(564, IFFLock.Constructor(Vector3(4002.047f, 4371.189f, 99.3772f), Vector3(0, 0, 180)), owning_building_guid = 32, door_guid = 335)
      LocalObject(565, IFFLock.Constructor(Vector3(4002.047f, 4371.189f, 119.3772f), Vector3(0, 0, 180)), owning_building_guid = 32, door_guid = 336)
      LocalObject(624, Locker.Constructor(Vector3(4003.716f, 4364.963f, 77.9102f)), owning_building_guid = 32)
      LocalObject(625, Locker.Constructor(Vector3(4003.751f, 4386.835f, 77.9102f)), owning_building_guid = 32)
      LocalObject(626, Locker.Constructor(Vector3(4005.053f, 4364.963f, 77.9102f)), owning_building_guid = 32)
      LocalObject(627, Locker.Constructor(Vector3(4005.088f, 4386.835f, 77.9102f)), owning_building_guid = 32)
      LocalObject(628, Locker.Constructor(Vector3(4007.741f, 4364.963f, 77.9102f)), owning_building_guid = 32)
      LocalObject(629, Locker.Constructor(Vector3(4007.741f, 4386.835f, 77.9102f)), owning_building_guid = 32)
      LocalObject(630, Locker.Constructor(Vector3(4009.143f, 4364.963f, 77.9102f)), owning_building_guid = 32)
      LocalObject(631, Locker.Constructor(Vector3(4009.143f, 4386.835f, 77.9102f)), owning_building_guid = 32)
      LocalObject(880, Terminal.Constructor(order_terminal), owning_building_guid = 32)
      LocalObject(881, Terminal.Constructor(order_terminal), owning_building_guid = 32)
      LocalObject(882, Terminal.Constructor(order_terminal), owning_building_guid = 32)
      LocalObject(1005, SpawnTube.Constructor(respawn_tube_tower, Vector3(3998.706f, 4367.742f, 77.3982f), Vector3(0, 0, 0)), owning_building_guid = 32)
      LocalObject(1006, SpawnTube.Constructor(respawn_tube_tower, Vector3(3998.706f, 4384.152f, 77.3982f), Vector3(0, 0, 0)), owning_building_guid = 32)
    }

    Building67()

    def Building67() : Unit = { // Name: NE_Esamir_WG_tower Type: tower_b GUID: 33, MapID: 67
      LocalBuilding(33, 67, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(5098f, 2978f, 97.9873f))))
      LocalObject(1022, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 33)
      LocalObject(344, Door.Constructor(Vector3(5110f, 2970f, 99.5073f)), owning_building_guid = 33)
      LocalObject(345, Door.Constructor(Vector3(5110f, 2970f, 109.5073f)), owning_building_guid = 33)
      LocalObject(346, Door.Constructor(Vector3(5110f, 2970f, 129.5073f)), owning_building_guid = 33)
      LocalObject(347, Door.Constructor(Vector3(5110f, 2986f, 99.5073f)), owning_building_guid = 33)
      LocalObject(348, Door.Constructor(Vector3(5110f, 2986f, 109.5073f)), owning_building_guid = 33)
      LocalObject(349, Door.Constructor(Vector3(5110f, 2986f, 129.5073f)), owning_building_guid = 33)
      LocalObject(1039, Door.Constructor(Vector3(5109.147f, 2966.794f, 89.3233f)), owning_building_guid = 33)
      LocalObject(1040, Door.Constructor(Vector3(5109.147f, 2983.204f, 89.3233f)), owning_building_guid = 33)
      LocalObject(570, IFFLock.Constructor(Vector3(5107.957f, 2986.811f, 99.4483f), Vector3(0, 0, 0)), owning_building_guid = 33, door_guid = 347)
      LocalObject(571, IFFLock.Constructor(Vector3(5107.957f, 2986.811f, 109.4483f), Vector3(0, 0, 0)), owning_building_guid = 33, door_guid = 348)
      LocalObject(572, IFFLock.Constructor(Vector3(5107.957f, 2986.811f, 129.4483f), Vector3(0, 0, 0)), owning_building_guid = 33, door_guid = 349)
      LocalObject(573, IFFLock.Constructor(Vector3(5112.047f, 2969.189f, 99.4483f), Vector3(0, 0, 180)), owning_building_guid = 33, door_guid = 344)
      LocalObject(574, IFFLock.Constructor(Vector3(5112.047f, 2969.189f, 109.4483f), Vector3(0, 0, 180)), owning_building_guid = 33, door_guid = 345)
      LocalObject(575, IFFLock.Constructor(Vector3(5112.047f, 2969.189f, 129.4483f), Vector3(0, 0, 180)), owning_building_guid = 33, door_guid = 346)
      LocalObject(640, Locker.Constructor(Vector3(5113.716f, 2962.963f, 87.98131f)), owning_building_guid = 33)
      LocalObject(641, Locker.Constructor(Vector3(5113.751f, 2984.835f, 87.98131f)), owning_building_guid = 33)
      LocalObject(642, Locker.Constructor(Vector3(5115.053f, 2962.963f, 87.98131f)), owning_building_guid = 33)
      LocalObject(643, Locker.Constructor(Vector3(5115.088f, 2984.835f, 87.98131f)), owning_building_guid = 33)
      LocalObject(644, Locker.Constructor(Vector3(5117.741f, 2962.963f, 87.98131f)), owning_building_guid = 33)
      LocalObject(645, Locker.Constructor(Vector3(5117.741f, 2984.835f, 87.98131f)), owning_building_guid = 33)
      LocalObject(646, Locker.Constructor(Vector3(5119.143f, 2962.963f, 87.98131f)), owning_building_guid = 33)
      LocalObject(647, Locker.Constructor(Vector3(5119.143f, 2984.835f, 87.98131f)), owning_building_guid = 33)
      LocalObject(886, Terminal.Constructor(order_terminal), owning_building_guid = 33)
      LocalObject(887, Terminal.Constructor(order_terminal), owning_building_guid = 33)
      LocalObject(888, Terminal.Constructor(order_terminal), owning_building_guid = 33)
      LocalObject(1009, SpawnTube.Constructor(respawn_tube_tower, Vector3(5108.706f, 2965.742f, 87.46931f), Vector3(0, 0, 0)), owning_building_guid = 33)
      LocalObject(1010, SpawnTube.Constructor(respawn_tube_tower, Vector3(5108.706f, 2982.152f, 87.46931f), Vector3(0, 0, 0)), owning_building_guid = 33)
    }

    Building59()

    def Building59() : Unit = { // Name: SE_Ishundar_WG_tower Type: tower_c GUID: 34, MapID: 59
      LocalBuilding(34, 59, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3224f, 4594f, 56.08539f))))
      LocalObject(1017, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 34)
      LocalObject(322, Door.Constructor(Vector3(3236f, 4586f, 57.60639f)), owning_building_guid = 34)
      LocalObject(323, Door.Constructor(Vector3(3236f, 4586f, 77.60539f)), owning_building_guid = 34)
      LocalObject(324, Door.Constructor(Vector3(3236f, 4602f, 57.60639f)), owning_building_guid = 34)
      LocalObject(325, Door.Constructor(Vector3(3236f, 4602f, 77.60539f)), owning_building_guid = 34)
      LocalObject(1029, Door.Constructor(Vector3(3235.146f, 4582.794f, 47.42139f)), owning_building_guid = 34)
      LocalObject(1030, Door.Constructor(Vector3(3235.146f, 4599.204f, 47.42139f)), owning_building_guid = 34)
      LocalObject(548, IFFLock.Constructor(Vector3(3233.957f, 4602.811f, 57.54639f), Vector3(0, 0, 0)), owning_building_guid = 34, door_guid = 324)
      LocalObject(549, IFFLock.Constructor(Vector3(3233.957f, 4602.811f, 77.54639f), Vector3(0, 0, 0)), owning_building_guid = 34, door_guid = 325)
      LocalObject(550, IFFLock.Constructor(Vector3(3238.047f, 4585.189f, 57.54639f), Vector3(0, 0, 180)), owning_building_guid = 34, door_guid = 322)
      LocalObject(551, IFFLock.Constructor(Vector3(3238.047f, 4585.189f, 77.54639f), Vector3(0, 0, 180)), owning_building_guid = 34, door_guid = 323)
      LocalObject(600, Locker.Constructor(Vector3(3239.716f, 4578.963f, 46.07939f)), owning_building_guid = 34)
      LocalObject(601, Locker.Constructor(Vector3(3239.751f, 4600.835f, 46.07939f)), owning_building_guid = 34)
      LocalObject(602, Locker.Constructor(Vector3(3241.053f, 4578.963f, 46.07939f)), owning_building_guid = 34)
      LocalObject(603, Locker.Constructor(Vector3(3241.088f, 4600.835f, 46.07939f)), owning_building_guid = 34)
      LocalObject(604, Locker.Constructor(Vector3(3243.741f, 4578.963f, 46.07939f)), owning_building_guid = 34)
      LocalObject(605, Locker.Constructor(Vector3(3243.741f, 4600.835f, 46.07939f)), owning_building_guid = 34)
      LocalObject(606, Locker.Constructor(Vector3(3245.143f, 4578.963f, 46.07939f)), owning_building_guid = 34)
      LocalObject(607, Locker.Constructor(Vector3(3245.143f, 4600.835f, 46.07939f)), owning_building_guid = 34)
      LocalObject(833, Terminal.Constructor(order_terminal), owning_building_guid = 34)
      LocalObject(834, Terminal.Constructor(order_terminal), owning_building_guid = 34)
      LocalObject(835, Terminal.Constructor(order_terminal), owning_building_guid = 34)
      LocalObject(999, SpawnTube.Constructor(respawn_tube_tower, Vector3(3234.706f, 4581.742f, 45.56739f), Vector3(0, 0, 0)), owning_building_guid = 34)
      LocalObject(1000, SpawnTube.Constructor(respawn_tube_tower, Vector3(3234.706f, 4598.152f, 45.56739f), Vector3(0, 0, 0)), owning_building_guid = 34)
      LocalObject(941, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3222.907f, 4588.725f, 83.6554f)), owning_building_guid = 34)
      LocalObject(942, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 34)
      LocalObject(944, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3222.907f, 4599.17f, 83.6554f)), owning_building_guid = 34)
      LocalObject(945, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 34)
      LocalObject(666, FacilityTurret.Constructor(manned_turret, Vector3(3209.07f, 4579.045f, 75.02739f)), owning_building_guid = 34)
      TurretToWeapon(666, 5006)
      LocalObject(667, FacilityTurret.Constructor(manned_turret, Vector3(3247.497f, 4608.957f, 75.02739f)), owning_building_guid = 34)
      TurretToWeapon(667, 5007)
    }

    Building62()

    def Building62() : Unit = { // Name: N_Esamir_WG_tower Type: tower_c GUID: 35, MapID: 62
      LocalBuilding(35, 62, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(3670f, 3180f, 89.51079f))))
      LocalObject(1018, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 35)
      LocalObject(326, Door.Constructor(Vector3(3682f, 3172f, 91.03179f)), owning_building_guid = 35)
      LocalObject(327, Door.Constructor(Vector3(3682f, 3172f, 111.0308f)), owning_building_guid = 35)
      LocalObject(328, Door.Constructor(Vector3(3682f, 3188f, 91.03179f)), owning_building_guid = 35)
      LocalObject(329, Door.Constructor(Vector3(3682f, 3188f, 111.0308f)), owning_building_guid = 35)
      LocalObject(1031, Door.Constructor(Vector3(3681.146f, 3168.794f, 80.84679f)), owning_building_guid = 35)
      LocalObject(1032, Door.Constructor(Vector3(3681.146f, 3185.204f, 80.84679f)), owning_building_guid = 35)
      LocalObject(552, IFFLock.Constructor(Vector3(3679.957f, 3188.811f, 90.97179f), Vector3(0, 0, 0)), owning_building_guid = 35, door_guid = 328)
      LocalObject(553, IFFLock.Constructor(Vector3(3679.957f, 3188.811f, 110.9718f), Vector3(0, 0, 0)), owning_building_guid = 35, door_guid = 329)
      LocalObject(554, IFFLock.Constructor(Vector3(3684.047f, 3171.189f, 90.97179f), Vector3(0, 0, 180)), owning_building_guid = 35, door_guid = 326)
      LocalObject(555, IFFLock.Constructor(Vector3(3684.047f, 3171.189f, 110.9718f), Vector3(0, 0, 180)), owning_building_guid = 35, door_guid = 327)
      LocalObject(608, Locker.Constructor(Vector3(3685.716f, 3164.963f, 79.50479f)), owning_building_guid = 35)
      LocalObject(609, Locker.Constructor(Vector3(3685.751f, 3186.835f, 79.50479f)), owning_building_guid = 35)
      LocalObject(610, Locker.Constructor(Vector3(3687.053f, 3164.963f, 79.50479f)), owning_building_guid = 35)
      LocalObject(611, Locker.Constructor(Vector3(3687.088f, 3186.835f, 79.50479f)), owning_building_guid = 35)
      LocalObject(612, Locker.Constructor(Vector3(3689.741f, 3164.963f, 79.50479f)), owning_building_guid = 35)
      LocalObject(613, Locker.Constructor(Vector3(3689.741f, 3186.835f, 79.50479f)), owning_building_guid = 35)
      LocalObject(614, Locker.Constructor(Vector3(3691.143f, 3164.963f, 79.50479f)), owning_building_guid = 35)
      LocalObject(615, Locker.Constructor(Vector3(3691.143f, 3186.835f, 79.50479f)), owning_building_guid = 35)
      LocalObject(856, Terminal.Constructor(order_terminal), owning_building_guid = 35)
      LocalObject(857, Terminal.Constructor(order_terminal), owning_building_guid = 35)
      LocalObject(858, Terminal.Constructor(order_terminal), owning_building_guid = 35)
      LocalObject(1001, SpawnTube.Constructor(respawn_tube_tower, Vector3(3680.706f, 3167.742f, 78.99279f), Vector3(0, 0, 0)), owning_building_guid = 35)
      LocalObject(1002, SpawnTube.Constructor(respawn_tube_tower, Vector3(3680.706f, 3184.152f, 78.99279f), Vector3(0, 0, 0)), owning_building_guid = 35)
      LocalObject(947, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3668.907f, 3174.725f, 117.0808f)), owning_building_guid = 35)
      LocalObject(948, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 35)
      LocalObject(950, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(3668.907f, 3185.17f, 117.0808f)), owning_building_guid = 35)
      LocalObject(951, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 35)
      LocalObject(668, FacilityTurret.Constructor(manned_turret, Vector3(3655.07f, 3165.045f, 108.4528f)), owning_building_guid = 35)
      TurretToWeapon(668, 5008)
      LocalObject(669, FacilityTurret.Constructor(manned_turret, Vector3(3693.497f, 3194.957f, 108.4528f)), owning_building_guid = 35)
      TurretToWeapon(669, 5009)
    }

    Building61()

    def Building61() : Unit = { // Name: Continent_Central_tower Type: tower_c GUID: 36, MapID: 61
      LocalBuilding(36, 61, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(4410f, 3728f, 83.92174f))))
      LocalObject(1021, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 36)
      LocalObject(340, Door.Constructor(Vector3(4422f, 3720f, 85.44274f)), owning_building_guid = 36)
      LocalObject(341, Door.Constructor(Vector3(4422f, 3720f, 105.4417f)), owning_building_guid = 36)
      LocalObject(342, Door.Constructor(Vector3(4422f, 3736f, 85.44274f)), owning_building_guid = 36)
      LocalObject(343, Door.Constructor(Vector3(4422f, 3736f, 105.4417f)), owning_building_guid = 36)
      LocalObject(1037, Door.Constructor(Vector3(4421.146f, 3716.794f, 75.25774f)), owning_building_guid = 36)
      LocalObject(1038, Door.Constructor(Vector3(4421.146f, 3733.204f, 75.25774f)), owning_building_guid = 36)
      LocalObject(566, IFFLock.Constructor(Vector3(4419.957f, 3736.811f, 85.38274f), Vector3(0, 0, 0)), owning_building_guid = 36, door_guid = 342)
      LocalObject(567, IFFLock.Constructor(Vector3(4419.957f, 3736.811f, 105.3827f), Vector3(0, 0, 0)), owning_building_guid = 36, door_guid = 343)
      LocalObject(568, IFFLock.Constructor(Vector3(4424.047f, 3719.189f, 85.38274f), Vector3(0, 0, 180)), owning_building_guid = 36, door_guid = 340)
      LocalObject(569, IFFLock.Constructor(Vector3(4424.047f, 3719.189f, 105.3827f), Vector3(0, 0, 180)), owning_building_guid = 36, door_guid = 341)
      LocalObject(632, Locker.Constructor(Vector3(4425.716f, 3712.963f, 73.91574f)), owning_building_guid = 36)
      LocalObject(633, Locker.Constructor(Vector3(4425.751f, 3734.835f, 73.91574f)), owning_building_guid = 36)
      LocalObject(634, Locker.Constructor(Vector3(4427.053f, 3712.963f, 73.91574f)), owning_building_guid = 36)
      LocalObject(635, Locker.Constructor(Vector3(4427.088f, 3734.835f, 73.91574f)), owning_building_guid = 36)
      LocalObject(636, Locker.Constructor(Vector3(4429.741f, 3712.963f, 73.91574f)), owning_building_guid = 36)
      LocalObject(637, Locker.Constructor(Vector3(4429.741f, 3734.835f, 73.91574f)), owning_building_guid = 36)
      LocalObject(638, Locker.Constructor(Vector3(4431.143f, 3712.963f, 73.91574f)), owning_building_guid = 36)
      LocalObject(639, Locker.Constructor(Vector3(4431.143f, 3734.835f, 73.91574f)), owning_building_guid = 36)
      LocalObject(883, Terminal.Constructor(order_terminal), owning_building_guid = 36)
      LocalObject(884, Terminal.Constructor(order_terminal), owning_building_guid = 36)
      LocalObject(885, Terminal.Constructor(order_terminal), owning_building_guid = 36)
      LocalObject(1007, SpawnTube.Constructor(respawn_tube_tower, Vector3(4420.706f, 3715.742f, 73.40374f), Vector3(0, 0, 0)), owning_building_guid = 36)
      LocalObject(1008, SpawnTube.Constructor(respawn_tube_tower, Vector3(4420.706f, 3732.152f, 73.40374f), Vector3(0, 0, 0)), owning_building_guid = 36)
      LocalObject(953, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(4408.907f, 3722.725f, 111.4917f)), owning_building_guid = 36)
      LocalObject(954, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 36)
      LocalObject(956, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(4408.907f, 3733.17f, 111.4917f)), owning_building_guid = 36)
      LocalObject(957, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 36)
      LocalObject(672, FacilityTurret.Constructor(manned_turret, Vector3(4395.07f, 3713.045f, 102.8637f)), owning_building_guid = 36)
      TurretToWeapon(672, 5010)
      LocalObject(673, FacilityTurret.Constructor(manned_turret, Vector3(4433.497f, 3742.957f, 102.8637f)), owning_building_guid = 36)
      TurretToWeapon(673, 5011)
    }

    Building65()

    def Building65() : Unit = { // Name: SW_Hossin_WG_tower Type: tower_c GUID: 37, MapID: 65
      LocalBuilding(37, 65, FoundationBuilder(Building.Structure(StructureType.Tower, Vector3(5160f, 4266f, 93.25694f))))
      LocalObject(1023, CaptureTerminal.Constructor(secondary_capture), owning_building_guid = 37)
      LocalObject(350, Door.Constructor(Vector3(5172f, 4258f, 94.77794f)), owning_building_guid = 37)
      LocalObject(351, Door.Constructor(Vector3(5172f, 4258f, 114.7769f)), owning_building_guid = 37)
      LocalObject(352, Door.Constructor(Vector3(5172f, 4274f, 94.77794f)), owning_building_guid = 37)
      LocalObject(353, Door.Constructor(Vector3(5172f, 4274f, 114.7769f)), owning_building_guid = 37)
      LocalObject(1041, Door.Constructor(Vector3(5171.146f, 4254.794f, 84.59293f)), owning_building_guid = 37)
      LocalObject(1042, Door.Constructor(Vector3(5171.146f, 4271.204f, 84.59293f)), owning_building_guid = 37)
      LocalObject(576, IFFLock.Constructor(Vector3(5169.957f, 4274.811f, 94.71793f), Vector3(0, 0, 0)), owning_building_guid = 37, door_guid = 352)
      LocalObject(577, IFFLock.Constructor(Vector3(5169.957f, 4274.811f, 114.7179f), Vector3(0, 0, 0)), owning_building_guid = 37, door_guid = 353)
      LocalObject(578, IFFLock.Constructor(Vector3(5174.047f, 4257.189f, 94.71793f), Vector3(0, 0, 180)), owning_building_guid = 37, door_guid = 350)
      LocalObject(579, IFFLock.Constructor(Vector3(5174.047f, 4257.189f, 114.7179f), Vector3(0, 0, 180)), owning_building_guid = 37, door_guid = 351)
      LocalObject(648, Locker.Constructor(Vector3(5175.716f, 4250.963f, 83.25094f)), owning_building_guid = 37)
      LocalObject(649, Locker.Constructor(Vector3(5175.751f, 4272.835f, 83.25094f)), owning_building_guid = 37)
      LocalObject(650, Locker.Constructor(Vector3(5177.053f, 4250.963f, 83.25094f)), owning_building_guid = 37)
      LocalObject(651, Locker.Constructor(Vector3(5177.088f, 4272.835f, 83.25094f)), owning_building_guid = 37)
      LocalObject(652, Locker.Constructor(Vector3(5179.741f, 4250.963f, 83.25094f)), owning_building_guid = 37)
      LocalObject(653, Locker.Constructor(Vector3(5179.741f, 4272.835f, 83.25094f)), owning_building_guid = 37)
      LocalObject(654, Locker.Constructor(Vector3(5181.143f, 4250.963f, 83.25094f)), owning_building_guid = 37)
      LocalObject(655, Locker.Constructor(Vector3(5181.143f, 4272.835f, 83.25094f)), owning_building_guid = 37)
      LocalObject(889, Terminal.Constructor(order_terminal), owning_building_guid = 37)
      LocalObject(890, Terminal.Constructor(order_terminal), owning_building_guid = 37)
      LocalObject(891, Terminal.Constructor(order_terminal), owning_building_guid = 37)
      LocalObject(1011, SpawnTube.Constructor(respawn_tube_tower, Vector3(5170.706f, 4253.742f, 82.73894f), Vector3(0, 0, 0)), owning_building_guid = 37)
      LocalObject(1012, SpawnTube.Constructor(respawn_tube_tower, Vector3(5170.706f, 4270.152f, 82.73894f), Vector3(0, 0, 0)), owning_building_guid = 37)
      LocalObject(959, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(5158.907f, 4260.725f, 120.8269f)), owning_building_guid = 37)
      LocalObject(960, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 37)
      LocalObject(962, ProximityTerminal.Constructor(pad_landing_tower_frame, Vector3(5158.907f, 4271.17f, 120.8269f)), owning_building_guid = 37)
      LocalObject(963, Terminal.Constructor(air_rearm_terminal), owning_building_guid = 37)
      LocalObject(674, FacilityTurret.Constructor(manned_turret, Vector3(5145.07f, 4251.045f, 112.1989f)), owning_building_guid = 37)
      TurretToWeapon(674, 5012)
      LocalObject(675, FacilityTurret.Constructor(manned_turret, Vector3(5183.497f, 4280.957f, 112.1989f)), owning_building_guid = 37)
      TurretToWeapon(675, 5013)
    }

    Building33()

    def Building33() : Unit = { // Name: VT_building_vs Type: VT_building_vs GUID: 38, MapID: 33
      LocalBuilding(38, 33, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2848f, 4948f, 56.08539f))))
      LocalObject(234, Door.Constructor(Vector3(2801.93f, 4911.454f, 58.16139f)), owning_building_guid = 38)
      LocalObject(235, Door.Constructor(Vector3(2806.54f, 4916.042f, 58.16139f)), owning_building_guid = 38)
      LocalObject(236, Door.Constructor(Vector3(2806.672f, 4906.712f, 58.16139f)), owning_building_guid = 38)
      LocalObject(237, Door.Constructor(Vector3(2811.282f, 4911.3f, 58.16139f)), owning_building_guid = 38)
      LocalObject(238, Door.Constructor(Vector3(2811.413f, 4901.97f, 58.16139f)), owning_building_guid = 38)
      LocalObject(239, Door.Constructor(Vector3(2816.024f, 4906.558f, 58.16139f)), owning_building_guid = 38)
      LocalObject(358, Door.Constructor(Vector3(2792.55f, 4909.545f, 57.79539f)), owning_building_guid = 38)
      LocalObject(359, Door.Constructor(Vector3(2809.559f, 4892.536f, 57.79539f)), owning_building_guid = 38)
      LocalObject(406, Door.Constructor(Vector3(2811.231f, 4962.16f, 59.26039f)), owning_building_guid = 38)
      LocalObject(407, Door.Constructor(Vector3(2811.248f, 4933.857f, 59.26039f)), owning_building_guid = 38)
      LocalObject(408, Door.Constructor(Vector3(2819.768f, 4947.995f, 59.26039f)), owning_building_guid = 38)
      LocalObject(409, Door.Constructor(Vector3(2833.84f, 4911.231f, 59.26039f)), owning_building_guid = 38)
      LocalObject(410, Door.Constructor(Vector3(2848.005f, 4919.768f, 59.26039f)), owning_building_guid = 38)
      LocalObject(411, Door.Constructor(Vector3(2862.143f, 4911.248f, 59.26039f)), owning_building_guid = 38)
      LocalObject(792, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(793, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(794, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(795, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(796, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(797, Terminal.Constructor(order_terminal), owning_building_guid = 38)
      LocalObject(720, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(2802.552f, 4912.067f, 58.11739f), Vector3(0, 0, 225)), owning_building_guid = 38)
      LocalObject(721, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(2805.914f, 4915.429f, 58.11739f), Vector3(0, 0, 45)), owning_building_guid = 38)
      LocalObject(722, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(2807.296f, 4907.323f, 58.11739f), Vector3(0, 0, 225)), owning_building_guid = 38)
      LocalObject(723, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(2810.658f, 4910.686f, 58.11739f), Vector3(0, 0, 45)), owning_building_guid = 38)
      LocalObject(724, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(2812.039f, 4902.58f, 58.11739f), Vector3(0, 0, 225)), owning_building_guid = 38)
      LocalObject(725, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(2815.401f, 4905.942f, 58.11739f), Vector3(0, 0, 45)), owning_building_guid = 38)
    }

    Building35()

    def Building35() : Unit = { // Name: VT_building_vs Type: VT_building_vs GUID: 39, MapID: 35
      LocalBuilding(39, 35, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3122f, 4690f, 56.08539f))))
      LocalObject(240, Door.Constructor(Vector3(3075.97f, 4726.587f, 58.16139f)), owning_building_guid = 39)
      LocalObject(241, Door.Constructor(Vector3(3080.558f, 4721.976f, 58.16139f)), owning_building_guid = 39)
      LocalObject(242, Door.Constructor(Vector3(3080.712f, 4731.328f, 58.16139f)), owning_building_guid = 39)
      LocalObject(243, Door.Constructor(Vector3(3085.3f, 4726.718f, 58.16139f)), owning_building_guid = 39)
      LocalObject(244, Door.Constructor(Vector3(3085.454f, 4736.07f, 58.16139f)), owning_building_guid = 39)
      LocalObject(245, Door.Constructor(Vector3(3090.042f, 4731.46f, 58.16139f)), owning_building_guid = 39)
      LocalObject(364, Door.Constructor(Vector3(3066.536f, 4728.441f, 57.79539f)), owning_building_guid = 39)
      LocalObject(365, Door.Constructor(Vector3(3083.545f, 4745.45f, 57.79539f)), owning_building_guid = 39)
      LocalObject(412, Door.Constructor(Vector3(3085.231f, 4704.16f, 59.26039f)), owning_building_guid = 39)
      LocalObject(413, Door.Constructor(Vector3(3085.248f, 4675.857f, 59.26039f)), owning_building_guid = 39)
      LocalObject(414, Door.Constructor(Vector3(3093.768f, 4689.995f, 59.26039f)), owning_building_guid = 39)
      LocalObject(415, Door.Constructor(Vector3(3107.857f, 4726.752f, 59.26039f)), owning_building_guid = 39)
      LocalObject(417, Door.Constructor(Vector3(3121.995f, 4718.232f, 59.26039f)), owning_building_guid = 39)
      LocalObject(420, Door.Constructor(Vector3(3136.16f, 4726.769f, 59.26039f)), owning_building_guid = 39)
      LocalObject(818, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(819, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(820, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(821, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(822, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(823, Terminal.Constructor(order_terminal), owning_building_guid = 39)
      LocalObject(726, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3076.58f, 4725.961f, 58.11739f), Vector3(0, 0, 315)), owning_building_guid = 39)
      LocalObject(727, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3079.942f, 4722.599f, 58.11739f), Vector3(0, 0, 135)), owning_building_guid = 39)
      LocalObject(728, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3081.323f, 4730.704f, 58.11739f), Vector3(0, 0, 315)), owning_building_guid = 39)
      LocalObject(729, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3084.686f, 4727.342f, 58.11739f), Vector3(0, 0, 135)), owning_building_guid = 39)
      LocalObject(730, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3086.067f, 4735.448f, 58.11739f), Vector3(0, 0, 315)), owning_building_guid = 39)
      LocalObject(731, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3089.429f, 4732.086f, 58.11739f), Vector3(0, 0, 135)), owning_building_guid = 39)
    }

    Building34()

    def Building34() : Unit = { // Name: VT_building_vs Type: VT_building_vs GUID: 40, MapID: 34
      LocalBuilding(40, 34, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3144f, 4930f, 56.08539f))))
      LocalObject(246, Door.Constructor(Vector3(3137.266f, 4871.582f, 58.16139f)), owning_building_guid = 40)
      LocalObject(247, Door.Constructor(Vector3(3137.281f, 4878.085f, 58.16139f)), owning_building_guid = 40)
      LocalObject(248, Door.Constructor(Vector3(3143.972f, 4871.582f, 58.16139f)), owning_building_guid = 40)
      LocalObject(249, Door.Constructor(Vector3(3143.987f, 4878.085f, 58.16139f)), owning_building_guid = 40)
      LocalObject(250, Door.Constructor(Vector3(3150.677f, 4871.581f, 58.16139f)), owning_building_guid = 40)
      LocalObject(251, Door.Constructor(Vector3(3150.693f, 4878.085f, 58.16139f)), owning_building_guid = 40)
      LocalObject(366, Door.Constructor(Vector3(3131.983f, 4863.599f, 57.79539f)), owning_building_guid = 40)
      LocalObject(367, Door.Constructor(Vector3(3156.037f, 4863.599f, 57.79539f)), owning_building_guid = 40)
      LocalObject(416, Door.Constructor(Vector3(3107.988f, 4914.013f, 59.26039f)), owning_building_guid = 40)
      LocalObject(418, Door.Constructor(Vector3(3124.041f, 4910.033f, 59.26039f)), owning_building_guid = 40)
      LocalObject(419, Door.Constructor(Vector3(3128.013f, 4894.012f, 59.26039f)), owning_building_guid = 40)
      LocalObject(421, Door.Constructor(Vector3(3159.987f, 4893.988f, 59.26039f)), owning_building_guid = 40)
      LocalObject(422, Door.Constructor(Vector3(3163.967f, 4910.041f, 59.26039f)), owning_building_guid = 40)
      LocalObject(423, Door.Constructor(Vector3(3179.988f, 4914.013f, 59.26039f)), owning_building_guid = 40)
      LocalObject(824, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(825, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(826, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(827, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(828, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(829, Terminal.Constructor(order_terminal), owning_building_guid = 40)
      LocalObject(732, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3137.272f, 4872.455f, 58.11739f), Vector3(0, 0, 180)), owning_building_guid = 40)
      LocalObject(733, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3137.272f, 4877.209f, 58.11739f), Vector3(0, 0, 0)), owning_building_guid = 40)
      LocalObject(734, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3143.98f, 4877.21f, 58.11739f), Vector3(0, 0, 0)), owning_building_guid = 40)
      LocalObject(735, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3143.981f, 4872.455f, 58.11739f), Vector3(0, 0, 180)), owning_building_guid = 40)
      LocalObject(736, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3150.688f, 4872.455f, 58.11739f), Vector3(0, 0, 180)), owning_building_guid = 40)
      LocalObject(737, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3150.688f, 4877.209f, 58.11739f), Vector3(0, 0, 0)), owning_building_guid = 40)
    }

    Building7()

    def Building7() : Unit = { // Name: VT_building_vs Type: VT_building_vs GUID: 41, MapID: 7
      LocalBuilding(41, 7, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3592f, 2966f, 90.8536f))))
      LocalObject(252, Door.Constructor(Vector3(3584.247f, 2907.708f, 92.9296f)), owning_building_guid = 41)
      LocalObject(253, Door.Constructor(Vector3(3584.376f, 2914.211f, 92.9296f)), owning_building_guid = 41)
      LocalObject(254, Door.Constructor(Vector3(3590.952f, 2907.591f, 92.9296f)), owning_building_guid = 41)
      LocalObject(255, Door.Constructor(Vector3(3591.081f, 2914.094f, 92.9296f)), owning_building_guid = 41)
      LocalObject(256, Door.Constructor(Vector3(3597.657f, 2907.473f, 92.9296f)), owning_building_guid = 41)
      LocalObject(257, Door.Constructor(Vector3(3597.786f, 2913.977f, 92.9296f)), owning_building_guid = 41)
      LocalObject(368, Door.Constructor(Vector3(3578.826f, 2899.819f, 92.5636f)), owning_building_guid = 41)
      LocalObject(369, Door.Constructor(Vector3(3602.876f, 2899.399f, 92.5636f)), owning_building_guid = 41)
      LocalObject(424, Door.Constructor(Vector3(3555.714f, 2950.644f, 94.0286f)), owning_building_guid = 41)
      LocalObject(425, Door.Constructor(Vector3(3571.695f, 2946.385f, 94.0286f)), owning_building_guid = 41)
      LocalObject(426, Door.Constructor(Vector3(3575.387f, 2930.296f, 94.0286f)), owning_building_guid = 41)
      LocalObject(427, Door.Constructor(Vector3(3607.356f, 2929.714f, 94.0286f)), owning_building_guid = 41)
      LocalObject(428, Door.Constructor(Vector3(3611.615f, 2945.695f, 94.0286f)), owning_building_guid = 41)
      LocalObject(429, Door.Constructor(Vector3(3627.704f, 2949.387f, 94.0286f)), owning_building_guid = 41)
      LocalObject(836, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(837, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(838, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(839, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(840, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(841, Terminal.Constructor(order_terminal), owning_building_guid = 41)
      LocalObject(738, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3584.269f, 2908.581f, 92.8856f), Vector3(0, 0, 181)), owning_building_guid = 41)
      LocalObject(739, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3584.352f, 2913.335f, 92.8856f), Vector3(0, 0, 1)), owning_building_guid = 41)
      LocalObject(740, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3590.977f, 2908.464f, 92.8856f), Vector3(0, 0, 181)), owning_building_guid = 41)
      LocalObject(741, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3591.059f, 2913.219f, 92.8856f), Vector3(0, 0, 1)), owning_building_guid = 41)
      LocalObject(742, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3597.683f, 2908.347f, 92.8856f), Vector3(0, 0, 181)), owning_building_guid = 41)
      LocalObject(743, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3597.766f, 2913.101f, 92.8856f), Vector3(0, 0, 1)), owning_building_guid = 41)
    }

    Building12()

    def Building12() : Unit = { // Name: VT_building_vs Type: VT_building_vs GUID: 42, MapID: 12
      LocalBuilding(42, 12, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3690f, 2656f, 90.84919f))))
      LocalObject(258, Door.Constructor(Vector3(3684.214f, 2708.023f, 92.92519f)), owning_building_guid = 42)
      LocalObject(259, Door.Constructor(Vector3(3684.343f, 2714.527f, 92.92519f)), owning_building_guid = 42)
      LocalObject(260, Door.Constructor(Vector3(3690.919f, 2707.906f, 92.92519f)), owning_building_guid = 42)
      LocalObject(261, Door.Constructor(Vector3(3691.048f, 2714.409f, 92.92519f)), owning_building_guid = 42)
      LocalObject(262, Door.Constructor(Vector3(3697.624f, 2707.789f, 92.92519f)), owning_building_guid = 42)
      LocalObject(263, Door.Constructor(Vector3(3697.753f, 2714.292f, 92.92519f)), owning_building_guid = 42)
      LocalObject(372, Door.Constructor(Vector3(3679.124f, 2722.601f, 92.55919f)), owning_building_guid = 42)
      LocalObject(373, Door.Constructor(Vector3(3703.174f, 2722.181f, 92.55919f)), owning_building_guid = 42)
      LocalObject(430, Door.Constructor(Vector3(3654.296f, 2672.613f, 94.02419f)), owning_building_guid = 42)
      LocalObject(431, Door.Constructor(Vector3(3670.385f, 2676.305f, 94.02419f)), owning_building_guid = 42)
      LocalObject(432, Door.Constructor(Vector3(3674.644f, 2692.286f, 94.02419f)), owning_building_guid = 42)
      LocalObject(433, Door.Constructor(Vector3(3706.613f, 2691.704f, 94.02419f)), owning_building_guid = 42)
      LocalObject(434, Door.Constructor(Vector3(3710.305f, 2675.615f, 94.02419f)), owning_building_guid = 42)
      LocalObject(435, Door.Constructor(Vector3(3726.286f, 2671.356f, 94.02419f)), owning_building_guid = 42)
      LocalObject(852, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(853, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(854, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(855, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(859, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(860, Terminal.Constructor(order_terminal), owning_building_guid = 42)
      LocalObject(744, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3684.234f, 2708.899f, 92.88119f), Vector3(0, 0, 181)), owning_building_guid = 42)
      LocalObject(745, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3684.317f, 2713.653f, 92.88119f), Vector3(0, 0, 1)), owning_building_guid = 42)
      LocalObject(746, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3690.941f, 2708.781f, 92.88119f), Vector3(0, 0, 181)), owning_building_guid = 42)
      LocalObject(747, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3691.023f, 2713.536f, 92.88119f), Vector3(0, 0, 1)), owning_building_guid = 42)
      LocalObject(748, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3697.648f, 2708.665f, 92.88119f), Vector3(0, 0, 181)), owning_building_guid = 42)
      LocalObject(749, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3697.731f, 2713.419f, 92.88119f), Vector3(0, 0, 1)), owning_building_guid = 42)
    }

    Building13()

    def Building13() : Unit = { // Name: VT_building_vs Type: VT_building_vs GUID: 43, MapID: 13
      LocalBuilding(43, 13, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3788f, 2968f, 90.8505f))))
      LocalObject(264, Door.Constructor(Vector3(3780.247f, 2909.708f, 92.9265f)), owning_building_guid = 43)
      LocalObject(265, Door.Constructor(Vector3(3780.376f, 2916.211f, 92.9265f)), owning_building_guid = 43)
      LocalObject(266, Door.Constructor(Vector3(3786.952f, 2909.591f, 92.9265f)), owning_building_guid = 43)
      LocalObject(267, Door.Constructor(Vector3(3787.081f, 2916.094f, 92.9265f)), owning_building_guid = 43)
      LocalObject(268, Door.Constructor(Vector3(3793.657f, 2909.473f, 92.9265f)), owning_building_guid = 43)
      LocalObject(269, Door.Constructor(Vector3(3793.786f, 2915.977f, 92.9265f)), owning_building_guid = 43)
      LocalObject(376, Door.Constructor(Vector3(3774.826f, 2901.819f, 92.5605f)), owning_building_guid = 43)
      LocalObject(377, Door.Constructor(Vector3(3798.876f, 2901.399f, 92.5605f)), owning_building_guid = 43)
      LocalObject(436, Door.Constructor(Vector3(3751.714f, 2952.644f, 94.02551f)), owning_building_guid = 43)
      LocalObject(437, Door.Constructor(Vector3(3767.695f, 2948.385f, 94.02551f)), owning_building_guid = 43)
      LocalObject(438, Door.Constructor(Vector3(3771.387f, 2932.296f, 94.02551f)), owning_building_guid = 43)
      LocalObject(439, Door.Constructor(Vector3(3803.356f, 2931.714f, 94.02551f)), owning_building_guid = 43)
      LocalObject(440, Door.Constructor(Vector3(3807.615f, 2947.695f, 94.02551f)), owning_building_guid = 43)
      LocalObject(441, Door.Constructor(Vector3(3823.704f, 2951.387f, 94.02551f)), owning_building_guid = 43)
      LocalObject(871, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(872, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(873, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(874, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(875, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(876, Terminal.Constructor(order_terminal), owning_building_guid = 43)
      LocalObject(750, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3780.269f, 2910.581f, 92.8825f), Vector3(0, 0, 181)), owning_building_guid = 43)
      LocalObject(751, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3780.352f, 2915.335f, 92.8825f), Vector3(0, 0, 1)), owning_building_guid = 43)
      LocalObject(752, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3786.977f, 2910.464f, 92.8825f), Vector3(0, 0, 181)), owning_building_guid = 43)
      LocalObject(753, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3787.059f, 2915.219f, 92.8825f), Vector3(0, 0, 1)), owning_building_guid = 43)
      LocalObject(754, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3793.683f, 2910.347f, 92.8825f), Vector3(0, 0, 181)), owning_building_guid = 43)
      LocalObject(755, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(3793.766f, 2915.101f, 92.8825f), Vector3(0, 0, 1)), owning_building_guid = 43)
    }

    Building50()

    def Building50() : Unit = { // Name: VT_building_vs Type: VT_building_vs GUID: 44, MapID: 50
      LocalBuilding(44, 50, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5470f, 4094f, 103.2367f))))
      LocalObject(276, Door.Constructor(Vector3(5501.976f, 4135.442f, 105.3127f)), owning_building_guid = 44)
      LocalObject(277, Door.Constructor(Vector3(5506.587f, 4140.03f, 105.3127f)), owning_building_guid = 44)
      LocalObject(278, Door.Constructor(Vector3(5506.718f, 4130.7f, 105.3127f)), owning_building_guid = 44)
      LocalObject(279, Door.Constructor(Vector3(5511.328f, 4135.288f, 105.3127f)), owning_building_guid = 44)
      LocalObject(280, Door.Constructor(Vector3(5511.46f, 4125.958f, 105.3127f)), owning_building_guid = 44)
      LocalObject(281, Door.Constructor(Vector3(5516.07f, 4130.546f, 105.3127f)), owning_building_guid = 44)
      LocalObject(380, Door.Constructor(Vector3(5508.441f, 4149.464f, 104.9467f)), owning_building_guid = 44)
      LocalObject(381, Door.Constructor(Vector3(5525.45f, 4132.455f, 104.9467f)), owning_building_guid = 44)
      LocalObject(442, Door.Constructor(Vector3(5455.857f, 4130.752f, 106.4117f)), owning_building_guid = 44)
      LocalObject(444, Door.Constructor(Vector3(5469.995f, 4122.232f, 106.4117f)), owning_building_guid = 44)
      LocalObject(447, Door.Constructor(Vector3(5484.16f, 4130.769f, 106.4117f)), owning_building_guid = 44)
      LocalObject(448, Door.Constructor(Vector3(5498.232f, 4094.005f, 106.4117f)), owning_building_guid = 44)
      LocalObject(449, Door.Constructor(Vector3(5506.752f, 4108.143f, 106.4117f)), owning_building_guid = 44)
      LocalObject(450, Door.Constructor(Vector3(5506.769f, 4079.84f, 106.4117f)), owning_building_guid = 44)
      LocalObject(898, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(899, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(900, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(901, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(902, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(903, Terminal.Constructor(order_terminal), owning_building_guid = 44)
      LocalObject(762, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5502.599f, 4136.058f, 105.2687f), Vector3(0, 0, 225)), owning_building_guid = 44)
      LocalObject(763, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5505.961f, 4139.42f, 105.2687f), Vector3(0, 0, 45)), owning_building_guid = 44)
      LocalObject(764, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5507.342f, 4131.314f, 105.2687f), Vector3(0, 0, 225)), owning_building_guid = 44)
      LocalObject(765, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5510.704f, 4134.677f, 105.2687f), Vector3(0, 0, 45)), owning_building_guid = 44)
      LocalObject(766, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5512.086f, 4126.571f, 105.2687f), Vector3(0, 0, 225)), owning_building_guid = 44)
      LocalObject(767, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5515.448f, 4129.933f, 105.2687f), Vector3(0, 0, 45)), owning_building_guid = 44)
    }

    Building49()

    def Building49() : Unit = { // Name: VT_building_vs Type: VT_building_vs GUID: 45, MapID: 49
      LocalBuilding(45, 49, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5494f, 4394f, 103.232f))))
      LocalObject(270, Door.Constructor(Vector3(5487.266f, 4335.582f, 105.308f)), owning_building_guid = 45)
      LocalObject(271, Door.Constructor(Vector3(5487.281f, 4342.085f, 105.308f)), owning_building_guid = 45)
      LocalObject(272, Door.Constructor(Vector3(5493.972f, 4335.582f, 105.308f)), owning_building_guid = 45)
      LocalObject(273, Door.Constructor(Vector3(5493.987f, 4342.085f, 105.308f)), owning_building_guid = 45)
      LocalObject(274, Door.Constructor(Vector3(5500.677f, 4335.581f, 105.308f)), owning_building_guid = 45)
      LocalObject(275, Door.Constructor(Vector3(5500.693f, 4342.085f, 105.308f)), owning_building_guid = 45)
      LocalObject(378, Door.Constructor(Vector3(5481.983f, 4327.599f, 104.942f)), owning_building_guid = 45)
      LocalObject(379, Door.Constructor(Vector3(5506.037f, 4327.599f, 104.942f)), owning_building_guid = 45)
      LocalObject(443, Door.Constructor(Vector3(5457.988f, 4378.013f, 106.407f)), owning_building_guid = 45)
      LocalObject(445, Door.Constructor(Vector3(5474.041f, 4374.033f, 106.407f)), owning_building_guid = 45)
      LocalObject(446, Door.Constructor(Vector3(5478.013f, 4358.012f, 106.407f)), owning_building_guid = 45)
      LocalObject(451, Door.Constructor(Vector3(5509.987f, 4357.988f, 106.407f)), owning_building_guid = 45)
      LocalObject(452, Door.Constructor(Vector3(5513.967f, 4374.041f, 106.407f)), owning_building_guid = 45)
      LocalObject(453, Door.Constructor(Vector3(5529.988f, 4378.013f, 106.407f)), owning_building_guid = 45)
      LocalObject(892, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(893, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(894, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(895, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(896, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(897, Terminal.Constructor(order_terminal), owning_building_guid = 45)
      LocalObject(756, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5487.272f, 4336.455f, 105.264f), Vector3(0, 0, 180)), owning_building_guid = 45)
      LocalObject(757, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5487.272f, 4341.209f, 105.264f), Vector3(0, 0, 0)), owning_building_guid = 45)
      LocalObject(758, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5493.98f, 4341.21f, 105.264f), Vector3(0, 0, 0)), owning_building_guid = 45)
      LocalObject(759, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5493.981f, 4336.455f, 105.264f), Vector3(0, 0, 180)), owning_building_guid = 45)
      LocalObject(760, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5500.688f, 4336.455f, 105.264f), Vector3(0, 0, 180)), owning_building_guid = 45)
      LocalObject(761, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5500.688f, 4341.209f, 105.264f), Vector3(0, 0, 0)), owning_building_guid = 45)
    }

    Building48()

    def Building48() : Unit = { // Name: VT_building_vs Type: VT_building_vs GUID: 46, MapID: 48
      LocalBuilding(46, 48, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5796f, 4330f, 103.2358f))))
      LocalObject(282, Door.Constructor(Vector3(5749.93f, 4293.454f, 105.3118f)), owning_building_guid = 46)
      LocalObject(283, Door.Constructor(Vector3(5754.54f, 4298.042f, 105.3118f)), owning_building_guid = 46)
      LocalObject(284, Door.Constructor(Vector3(5754.672f, 4288.712f, 105.3118f)), owning_building_guid = 46)
      LocalObject(285, Door.Constructor(Vector3(5759.282f, 4293.3f, 105.3118f)), owning_building_guid = 46)
      LocalObject(286, Door.Constructor(Vector3(5759.413f, 4283.97f, 105.3118f)), owning_building_guid = 46)
      LocalObject(287, Door.Constructor(Vector3(5764.024f, 4288.558f, 105.3118f)), owning_building_guid = 46)
      LocalObject(386, Door.Constructor(Vector3(5740.55f, 4291.545f, 104.9458f)), owning_building_guid = 46)
      LocalObject(387, Door.Constructor(Vector3(5757.559f, 4274.536f, 104.9458f)), owning_building_guid = 46)
      LocalObject(454, Door.Constructor(Vector3(5759.231f, 4344.16f, 106.4108f)), owning_building_guid = 46)
      LocalObject(455, Door.Constructor(Vector3(5759.248f, 4315.857f, 106.4108f)), owning_building_guid = 46)
      LocalObject(456, Door.Constructor(Vector3(5767.768f, 4329.995f, 106.4108f)), owning_building_guid = 46)
      LocalObject(457, Door.Constructor(Vector3(5781.84f, 4293.231f, 106.4108f)), owning_building_guid = 46)
      LocalObject(458, Door.Constructor(Vector3(5796.005f, 4301.768f, 106.4108f)), owning_building_guid = 46)
      LocalObject(459, Door.Constructor(Vector3(5810.143f, 4293.248f, 106.4108f)), owning_building_guid = 46)
      LocalObject(924, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(925, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(926, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(927, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(928, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(929, Terminal.Constructor(order_terminal), owning_building_guid = 46)
      LocalObject(768, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5750.552f, 4294.067f, 105.2678f), Vector3(0, 0, 225)), owning_building_guid = 46)
      LocalObject(769, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5753.914f, 4297.429f, 105.2678f), Vector3(0, 0, 45)), owning_building_guid = 46)
      LocalObject(770, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5755.296f, 4289.323f, 105.2678f), Vector3(0, 0, 225)), owning_building_guid = 46)
      LocalObject(771, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5758.658f, 4292.686f, 105.2678f), Vector3(0, 0, 45)), owning_building_guid = 46)
      LocalObject(772, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5760.039f, 4284.58f, 105.2678f), Vector3(0, 0, 225)), owning_building_guid = 46)
      LocalObject(773, SpawnTube.Constructor(respawn_tube_sanctuary, Vector3(5763.401f, 4287.942f, 105.2678f), Vector3(0, 0, 45)), owning_building_guid = 46)
    }

    Building37()

    def Building37() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 47, MapID: 37
      LocalBuilding(47, 37, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2764f, 4842f, 56.08539f))))
      LocalObject(300, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 47)
      LocalObject(288, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(2767.589f, 4841.958f, 52.10039f), Vector3(0, 0, 181)), owning_building_guid = 47, terminal_guid = 300)
    }

    Building24()

    def Building24() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 48, MapID: 24
      LocalBuilding(48, 24, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2928f, 5050f, 56.08539f))))
      LocalObject(301, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 48)
      LocalObject(289, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(2927.957f, 5046.411f, 52.10039f), Vector3(0, 0, -89)), owning_building_guid = 48, terminal_guid = 301)
    }

    Building36()

    def Building36() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 49, MapID: 36
      LocalBuilding(49, 36, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3018f, 4618f, 56.08539f))))
      LocalObject(302, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 49)
      LocalObject(290, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(3018.043f, 4621.589f, 52.10039f), Vector3(0, 0, 91)), owning_building_guid = 49, terminal_guid = 302)
    }

    Building23()

    def Building23() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 50, MapID: 23
      LocalBuilding(50, 23, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3212f, 4768f, 56.08539f))))
      LocalObject(303, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 50)
      LocalObject(291, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(3208.411f, 4768.042f, 52.10039f), Vector3(0, 0, 1)), owning_building_guid = 50, terminal_guid = 303)
    }

    Building6()

    def Building6() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 51, MapID: 6
      LocalBuilding(51, 6, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3506f, 2896f, 90.85538f))))
      LocalObject(304, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 51)
      LocalObject(292, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(3509.589f, 2895.957f, 86.87038f), Vector3(0, 0, 181)), owning_building_guid = 51, terminal_guid = 304)
    }

    Building15()

    def Building15() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 52, MapID: 15
      LocalBuilding(52, 15, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3572f, 2652f, 90.85646f))))
      LocalObject(305, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 52)
      LocalObject(293, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(3574.568f, 2654.508f, 86.87146f), Vector3(0, 0, 136)), owning_building_guid = 52, terminal_guid = 305)
    }

    Building14()

    def Building14() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 53, MapID: 14
      LocalBuilding(53, 14, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3808f, 2654f, 90.85372f))))
      LocalObject(306, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 53)
      LocalObject(294, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(3805.492f, 2656.568f, 86.86872f), Vector3(0, 0, 46)), owning_building_guid = 53, terminal_guid = 306)
    }

    Building5()

    def Building5() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 54, MapID: 5
      LocalBuilding(54, 5, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3876f, 2896f, 90.85098f))))
      LocalObject(307, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 54)
      LocalObject(295, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(3872.411f, 2896.043f, 86.86597f), Vector3(0, 0, 1)), owning_building_guid = 54, terminal_guid = 307)
    }

    Building52()

    def Building52() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 55, MapID: 52
      LocalBuilding(55, 52, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5394f, 4238f, 103.2297f))))
      LocalObject(308, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 55)
      LocalObject(296, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(5397.589f, 4237.958f, 99.24469f), Vector3(0, 0, 181)), owning_building_guid = 55, terminal_guid = 308)
    }

    Building51()

    def Building51() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 56, MapID: 51
      LocalBuilding(56, 51, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5600f, 4034f, 103.2345f))))
      LocalObject(309, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 56)
      LocalObject(297, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(5600.042f, 4037.589f, 99.24946f), Vector3(0, 0, 91)), owning_building_guid = 56, terminal_guid = 309)
    }

    Building42()

    def Building42() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 57, MapID: 42
      LocalBuilding(57, 42, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5622f, 4456f, 103.2351f))))
      LocalObject(310, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 57)
      LocalObject(298, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(5621.958f, 4452.411f, 99.25006f), Vector3(0, 0, -89)), owning_building_guid = 57, terminal_guid = 310)
    }

    Building41()

    def Building41() : Unit = { // Name: vt_dropship Type: vt_dropship GUID: 58, MapID: 41
      LocalBuilding(58, 41, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5834f, 4218f, 103.2365f))))
      LocalObject(311, Terminal.Constructor(dropship_vehicle_terminal), owning_building_guid = 58)
      LocalObject(299, VehicleSpawnPad.Constructor(dropship_pad_doors, Vector3(5830.411f, 4218.042f, 99.25149f), Vector3(0, 0, 1)), owning_building_guid = 58, terminal_guid = 311)
    }

    Building30()

    def Building30() : Unit = { // Name: VS_NW_Tport_01 Type: vt_spawn GUID: 59, MapID: 30
      LocalBuilding(59, 30, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2900f, 4758f, 56.08539f))))
    }

    Building29()

    def Building29() : Unit = { // Name: VS_NW_Tport_02 Type: vt_spawn GUID: 60, MapID: 29
      LocalBuilding(60, 29, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2940f, 4932f, 56.08539f))))
    }

    Building32()

    def Building32() : Unit = { // Name: VS_NW_Tport_03 Type: vt_spawn GUID: 61, MapID: 32
      LocalBuilding(61, 32, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3060f, 4908f, 56.08539f))))
    }

    Building31()

    def Building31() : Unit = { // Name: VS_NW_Tport_04 Type: vt_spawn GUID: 62, MapID: 31
      LocalBuilding(62, 31, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3124f, 4776f, 56.08539f))))
    }

    Building19()

    def Building19() : Unit = { // Name: VS_S_Tport_03 Type: vt_spawn GUID: 63, MapID: 19
      LocalBuilding(63, 19, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3610f, 2732f, 90.85205f))))
    }

    Building18()

    def Building18() : Unit = { // Name: VS_S_Tport_01 Type: vt_spawn GUID: 64, MapID: 18
      LocalBuilding(64, 18, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3652f, 2908f, 90.8536f))))
    }

    Building21()

    def Building21() : Unit = { // Name: VS_S_Tport_02 Type: vt_spawn GUID: 65, MapID: 21
      LocalBuilding(65, 21, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3730f, 2908f, 90.8536f))))
    }

    Building20()

    def Building20() : Unit = { // Name: VS_S_Tport_04 Type: vt_spawn GUID: 66, MapID: 20
      LocalBuilding(66, 20, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3766f, 2732f, 90.84919f))))
    }

    Building45()

    def Building45() : Unit = { // Name: VS_NE_Tport_04 Type: vt_spawn GUID: 67, MapID: 45
      LocalBuilding(67, 45, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5484f, 4208f, 103.2298f))))
    }

    Building44()

    def Building44() : Unit = { // Name: VS_NE_Tport_01 Type: vt_spawn GUID: 68, MapID: 44
      LocalBuilding(68, 44, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5552f, 4344f, 103.2289f))))
    }

    Building46()

    def Building46() : Unit = { // Name: VS_NE_Tport_03 Type: vt_spawn GUID: 69, MapID: 46
      LocalBuilding(69, 46, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5690f, 4164f, 103.2289f))))
    }

    Building47()

    def Building47() : Unit = { // Name: VS_NE_Tport_02 Type: vt_spawn GUID: 70, MapID: 47
      LocalBuilding(70, 47, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5692f, 4312f, 103.2289f))))
    }

    Building26()

    def Building26() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 71, MapID: 26
      LocalBuilding(71, 26, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2768f, 4768f, 56.08539f))))
      LocalObject(1057, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 71)
      LocalObject(702, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(2767.853f, 4767.976f, 54.61439f), Vector3(0, 0, -89)), owning_building_guid = 71, terminal_guid = 1057)
    }

    Building38()

    def Building38() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 72, MapID: 38
      LocalBuilding(72, 38, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2816f, 4700f, 56.08539f))))
      LocalObject(1058, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 72)
      LocalObject(703, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(2815.915f, 4699.877f, 54.61439f), Vector3(0, 0, 225)), owning_building_guid = 72, terminal_guid = 1058)
    }

    Building28()

    def Building28() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 73, MapID: 28
      LocalBuilding(73, 28, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(2946f, 4622f, 56.08539f))))
      LocalObject(1059, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 73)
      LocalObject(704, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(2946.024f, 4621.853f, 54.61439f), Vector3(0, 0, 181)), owning_building_guid = 73, terminal_guid = 1059)
    }

    Building25()

    def Building25() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 74, MapID: 25
      LocalBuilding(74, 25, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3000f, 5048f, 56.08539f))))
      LocalObject(1060, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 74)
      LocalObject(705, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(2999.976f, 5048.147f, 54.61439f), Vector3(0, 0, 1)), owning_building_guid = 74, terminal_guid = 1060)
    }

    Building39()

    def Building39() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 75, MapID: 39
      LocalBuilding(75, 39, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3094f, 5010f, 56.08539f))))
      LocalObject(1061, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 75)
      LocalObject(706, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(3094.085f, 5010.123f, 54.61439f), Vector3(0, 0, 45)), owning_building_guid = 75, terminal_guid = 1061)
    }

    Building27()

    def Building27() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 76, MapID: 27
      LocalBuilding(76, 27, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3212f, 4842f, 56.08539f))))
      LocalObject(1062, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 76)
      LocalObject(707, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(3212.147f, 4842.024f, 54.61439f), Vector3(0, 0, 91)), owning_building_guid = 76, terminal_guid = 1062)
    }

    Building17()

    def Building17() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 77, MapID: 17
      LocalBuilding(77, 17, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3506f, 2820f, 90.8536f))))
      LocalObject(1063, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 77)
      LocalObject(708, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(3505.853f, 2819.976f, 89.3826f), Vector3(0, 0, -89)), owning_building_guid = 77, terminal_guid = 1063)
    }

    Building8()

    def Building8() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 78, MapID: 8
      LocalBuilding(78, 8, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3658f, 3020f, 90.8536f))))
      LocalObject(1064, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 78)
      LocalObject(709, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(3657.976f, 3020.147f, 89.3826f), Vector3(0, 0, 1)), owning_building_guid = 78, terminal_guid = 1064)
    }

    Building11()

    def Building11() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 79, MapID: 11
      LocalBuilding(79, 11, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3660f, 2590f, 90.8536f))))
      LocalObject(1065, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 79)
      LocalObject(710, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(3660.024f, 2589.853f, 89.3826f), Vector3(0, 0, 181)), owning_building_guid = 79, terminal_guid = 1065)
    }

    Building9()

    def Building9() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 80, MapID: 9
      LocalBuilding(80, 9, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3722f, 3020f, 90.8536f))))
      LocalObject(1066, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 80)
      LocalObject(711, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(3721.976f, 3020.147f, 89.3826f), Vector3(0, 0, 1)), owning_building_guid = 80, terminal_guid = 1066)
    }

    Building10()

    def Building10() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 81, MapID: 10
      LocalBuilding(81, 10, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3724f, 2588f, 90.85265f))))
      LocalObject(1067, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 81)
      LocalObject(712, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(3724.024f, 2587.853f, 89.38165f), Vector3(0, 0, 181)), owning_building_guid = 81, terminal_guid = 1067)
    }

    Building16()

    def Building16() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 82, MapID: 16
      LocalBuilding(82, 16, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(3878f, 2824f, 90.8536f))))
      LocalObject(1068, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 82)
      LocalObject(713, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(3878.147f, 2824.024f, 89.3826f), Vector3(0, 0, 91)), owning_building_guid = 82, terminal_guid = 1068)
    }

    Building55()

    def Building55() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 83, MapID: 55
      LocalBuilding(83, 55, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5420f, 4174f, 103.2318f))))
      LocalObject(1069, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 83)
      LocalObject(714, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(5419.853f, 4173.976f, 101.7608f), Vector3(0, 0, -89)), owning_building_guid = 83, terminal_guid = 1069)
    }

    Building43()

    def Building43() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 84, MapID: 43
      LocalBuilding(84, 43, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5420f, 4298f, 103.2333f))))
      LocalObject(1070, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 84)
      LocalObject(715, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(5419.853f, 4297.976f, 101.7623f), Vector3(0, 0, -89)), owning_building_guid = 84, terminal_guid = 1070)
    }

    Building54()

    def Building54() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 85, MapID: 54
      LocalBuilding(85, 54, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5686f, 4420f, 103.2329f))))
      LocalObject(1071, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 85)
      LocalObject(716, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(5686.085f, 4420.123f, 101.7619f), Vector3(0, 0, 45)), owning_building_guid = 85, terminal_guid = 1071)
    }

    Building57()

    def Building57() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 86, MapID: 57
      LocalBuilding(86, 57, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5710f, 4046f, 103.2289f))))
      LocalObject(1072, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 86)
      LocalObject(717, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(5710.123f, 4045.915f, 101.7579f), Vector3(0, 0, 135)), owning_building_guid = 86, terminal_guid = 1072)
    }

    Building53()

    def Building53() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 87, MapID: 53
      LocalBuilding(87, 53, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5752f, 4088f, 103.2289f))))
      LocalObject(1073, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 87)
      LocalObject(718, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(5752.123f, 4087.915f, 101.7579f), Vector3(0, 0, 135)), owning_building_guid = 87, terminal_guid = 1073)
    }

    Building56()

    def Building56() : Unit = { // Name: vt_vehicle Type: vt_vehicle GUID: 88, MapID: 56
      LocalBuilding(88, 56, FoundationBuilder(Building.Structure(StructureType.Building, Vector3(5794f, 4132f, 103.2289f))))
      LocalObject(1074, Terminal.Constructor(ground_vehicle_terminal), owning_building_guid = 88)
      LocalObject(719, VehicleSpawnPad.Constructor(mb_pad_creation, Vector3(5794.123f, 4131.915f, 101.7579f), Vector3(0, 0, 135)), owning_building_guid = 88, terminal_guid = 1074)
    }

    Building1()

    def Building1() : Unit = { // Name: ??? Type: ??? GUID: 90, MapID: 1
      LocalBuilding(90, 1, FoundationBuilder(WarpGate.Structure(Vector3(2514.6094f, 5311.7656f, 65.984375f))))
    }

    Building3()

    def Building3() : Unit = { // Name: ??? Type: ??? GUID: 89, MapID: 3
      LocalBuilding(89, 3, FoundationBuilder(WarpGate.Structure(Vector3(4175.6484f, 2402.8125f, 163.9375f))))
    }
  }
}
