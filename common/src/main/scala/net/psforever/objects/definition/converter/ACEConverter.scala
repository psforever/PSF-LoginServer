// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.ConstructionItem
import net.psforever.packet.game.objectcreate.{ACEData, DetailedACEData}

import scala.util.{Success, Try}

class ACEConverter extends ObjectCreateConverter[ConstructionItem]() {
  override def ConstructorData(obj : ConstructionItem) : Try[ACEData] = {
    Success(ACEData(0,0))
  }

  override def DetailedConstructorData(obj : ConstructionItem) : Try[DetailedACEData] = {
    Success(DetailedACEData(0))
  }
}
