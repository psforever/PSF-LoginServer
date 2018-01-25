// Copyright (c) 2017 PSForever
import net.psforever.objects.zones.ZoneMap
import net.psforever.objects.GlobalDefinitions._
import net.psforever.objects.serverobject.ServerObjectBuilder
import net.psforever.objects.serverobject.doors.Door
import net.psforever.objects.serverobject.implantmech.ImplantTerminalMech
import net.psforever.objects.serverobject.locks.IFFLock
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.serverobject.pad.VehicleSpawnPad
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.types.Vector3

object Maps {
  val map1 = new ZoneMap("map01")

  val map2 = new ZoneMap("map02")

  val map3 = new ZoneMap("map03")

  val map4 = new ZoneMap("map04")

  val map5 = new ZoneMap("map05")

  val map6 = new ZoneMap("map06")

  val map7 = new ZoneMap("map07")

  val map8 = new ZoneMap("map08")

  val map9 = new ZoneMap("map09")

  val map10 = new ZoneMap("map10")

  val map11 = new ZoneMap("map11")

  val map12 = new ZoneMap("map12")

  val map13 = new ZoneMap("map13") {
    LocalObject(ServerObjectBuilder(330, Door.Constructor))
    LocalObject(ServerObjectBuilder(332, Door.Constructor))
    LocalObject(ServerObjectBuilder(362, Door.Constructor))
    LocalObject(ServerObjectBuilder(370, Door.Constructor))
    LocalObject(ServerObjectBuilder(371, Door.Constructor))
    LocalObject(ServerObjectBuilder(372, Door.Constructor))
    LocalObject(ServerObjectBuilder(373, Door.Constructor))
    LocalObject(ServerObjectBuilder(374, Door.Constructor))
    LocalObject(ServerObjectBuilder(375, Door.Constructor))
    LocalObject(ServerObjectBuilder(394, Door.Constructor))
    LocalObject(ServerObjectBuilder(395, Door.Constructor))
    LocalObject(ServerObjectBuilder(396, Door.Constructor))
    LocalObject(ServerObjectBuilder(397, Door.Constructor))
    LocalObject(ServerObjectBuilder(398, Door.Constructor))
    LocalObject(ServerObjectBuilder(462, Door.Constructor))
    LocalObject(ServerObjectBuilder(463, Door.Constructor))
    LocalObject(ServerObjectBuilder(520, ImplantTerminalMech.Constructor)) //Hart B
    LocalObject(ServerObjectBuilder(522, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(523, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(524, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(525, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(526, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(527, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(528, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(529, ImplantTerminalMech.Constructor)) //Hart C
    LocalObject(ServerObjectBuilder(556, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(558, IFFLock.Constructor))
    LocalObject(ServerObjectBuilder(686, Locker.Constructor))
    LocalObject(ServerObjectBuilder(687, Locker.Constructor))
    LocalObject(ServerObjectBuilder(688, Locker.Constructor))
    LocalObject(ServerObjectBuilder(689, Locker.Constructor))
    LocalObject(ServerObjectBuilder(690, Locker.Constructor))
    LocalObject(ServerObjectBuilder(691, Locker.Constructor))
    LocalObject(ServerObjectBuilder(692, Locker.Constructor))
    LocalObject(ServerObjectBuilder(693, Locker.Constructor))
    LocalObject(ServerObjectBuilder(186, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(187, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(188, Terminal.Constructor(cert_terminal)))
    LocalObject(ServerObjectBuilder(842, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(843, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(844, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(845, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(853, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(855, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(860, Terminal.Constructor(order_terminal)))
    LocalObject(ServerObjectBuilder(1081, Terminal.Constructor(implant_terminal_interface))) //tube 520
    LocalObject(ServerObjectBuilder(1082, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1083, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1084, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1085, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1086, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1087, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1088, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1089, Terminal.Constructor(implant_terminal_interface))) //TODO guid not correct
    LocalObject(ServerObjectBuilder(1063, Terminal.Constructor(ground_vehicle_terminal)))
    LocalObject(ServerObjectBuilder(500,
      VehicleSpawnPad.Constructor(Vector3(3506.0f, 2820.0f, 92.0f), Vector3(0f, 0f, 270.0f))
    )) //TODO guid not correct
    LocalObject(ServerObjectBuilder(304, Terminal.Constructor(dropship_vehicle_terminal)))
    LocalObject(ServerObjectBuilder(501,
      VehicleSpawnPad.Constructor(Vector3(3508.9844f, 2895.961f, 92.296875f), Vector3(0f, 0f, 270.0f))
    )) //TODO guid not correct

    LocalBases = 30

    ObjectToBase(330, 29)
    ObjectToBase(332, 29)
    //ObjectToBase(520, 29)
    ObjectToBase(522, 2)
    ObjectToBase(523, 2)
    ObjectToBase(524, 2)
    ObjectToBase(525, 2)
    ObjectToBase(526, 2)
    ObjectToBase(527, 2)
    ObjectToBase(528, 2)
    ObjectToBase(529, 2)
    ObjectToBase(556, 29)
    ObjectToBase(558, 29)
    ObjectToBase(1081, 2)
    ObjectToBase(1063, 2) //TODO unowned courtyard terminal?
    ObjectToBase(500, 2) //TODO unowned courtyard spawnpad?
    ObjectToBase(304, 2) //TODO unowned courtyard terminal?
    ObjectToBase(501, 2) //TODO unowned courtyard spawnpad?

    DoorToLock(330, 558)
    DoorToLock(332, 556)
    TerminalToSpawnPad(1063, 500)
    TerminalToSpawnPad(304, 501)
    TerminalToInterface(520, 1081)
    TerminalToInterface(522, 1082)
    TerminalToInterface(523, 1083)
    TerminalToInterface(524, 1084)
    TerminalToInterface(525, 1085)
    TerminalToInterface(526, 1086)
    TerminalToInterface(527, 1087)
    TerminalToInterface(528, 1088)
    TerminalToInterface(529, 1089)
  }

  val map14 = new ZoneMap("map13")

  val map15 = new ZoneMap("map13")

  val map16 = new ZoneMap("map13")

  val ugd01 = new ZoneMap("ugd01")

  val ugd02 = new ZoneMap("ugd02")

  val ugd03 = new ZoneMap("ugd03")

  val ugd04 = new ZoneMap("ugd04")

  val ugd05 = new ZoneMap("ugd05")

  val ugd06 = new ZoneMap("ugd06")

  val map96 = new ZoneMap("ugd06")

  val map97 = new ZoneMap("map97")

  val map98 = new ZoneMap("map98")

  val map99 = new ZoneMap("map99")
}
