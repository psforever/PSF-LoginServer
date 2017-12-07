// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.objectcreate.CommonTerminalData

import scala.util.{Failure, Success, Try}

class ImplantTerminalInterfaceConverter extends ObjectCreateConverter[Terminal]() {
  override def DetailedConstructorData(obj : Terminal) : Try[CommonTerminalData] =
    Failure(new Exception("ImplantTerminalInterfaceConverter should not be used to generate detailed CommonTerminalData"))

  override def ConstructorData(obj : Terminal) : Try[CommonTerminalData] =
    Success(CommonTerminalData(net.psforever.types.PlanetSideEmpire.VS)) //TODO shortcut
}
