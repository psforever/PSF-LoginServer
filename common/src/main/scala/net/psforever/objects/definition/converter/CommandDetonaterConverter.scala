// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.SimpleItem
import net.psforever.packet.game.objectcreate.{CommandDetonaterData, DetailedCommandDetonaterData}

import scala.util.{Success, Try}

class CommandDetonaterConverter extends ObjectCreateConverter[SimpleItem]() {
  override def ConstructorData(obj : SimpleItem) : Try[CommandDetonaterData] = {
    Success(CommandDetonaterData())
  }

  override def DetailedConstructorData(obj : SimpleItem) : Try[DetailedCommandDetonaterData] = {
    Success(DetailedCommandDetonaterData())
  }
}
