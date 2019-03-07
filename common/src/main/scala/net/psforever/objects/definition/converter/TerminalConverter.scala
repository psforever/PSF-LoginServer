// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.packet.game.objectcreate.CommonFieldData

import scala.util.{Success, Try}

class TerminalConverter extends ObjectCreateConverter[Terminal]() {
  override def ConstructorData(obj : Terminal) : Try[CommonFieldData] = { Success(CommonFieldData(obj.Faction)(false)) }
}
