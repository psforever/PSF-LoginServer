// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.SimpleItem
import net.psforever.packet.game.objectcreate.{DetailedREKData, REKData}

import scala.util.{Success, Try}

class REKConverter extends ObjectCreateConverter[SimpleItem]() {
  override def ConstructorData(obj : SimpleItem) : Try[REKData] = {
    Success(REKData(8,0))
  }

  override def DetailedConstructorData(obj : SimpleItem) : Try[DetailedREKData] = {
    Success(DetailedREKData(8))
  }
}