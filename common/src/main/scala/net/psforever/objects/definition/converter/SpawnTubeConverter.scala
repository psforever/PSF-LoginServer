// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.serverobject.tube.SpawnTube
import net.psforever.packet.game.objectcreate.CommonTerminalData

import scala.util.{Success, Try}

class SpawnTubeConverter extends ObjectCreateConverter[SpawnTube]() {
  override def ConstructorData(obj : SpawnTube) : Try[CommonTerminalData] = { Success(CommonTerminalData(obj.Faction)) }
}
