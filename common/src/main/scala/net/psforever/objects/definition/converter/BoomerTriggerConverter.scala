// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.SimpleItem
import net.psforever.packet.game.objectcreate.{BoomerTriggerData, DetailedBoomerTriggerData}

import scala.util.{Success, Try}

class BoomerTriggerConverter extends ObjectCreateConverter[SimpleItem]() {
  override def ConstructorData(obj : SimpleItem) : Try[BoomerTriggerData] = {
    Success(BoomerTriggerData())
  }

  override def DetailedConstructorData(obj : SimpleItem) : Try[DetailedBoomerTriggerData] = {
    Success(DetailedBoomerTriggerData())
  }
}
