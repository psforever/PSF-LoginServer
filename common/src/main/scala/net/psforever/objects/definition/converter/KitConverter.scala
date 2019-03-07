// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Kit
import net.psforever.packet.game.objectcreate.{CommonFieldData, DetailedAmmoBoxData}

import scala.util.{Success, Try}

class KitConverter extends ObjectCreateConverter[Kit]() {
  override def ConstructorData(obj : Kit) : Try[CommonFieldData] = {
    Success(CommonFieldData()(false))
  }

  override def DetailedConstructorData(obj : Kit) : Try[DetailedAmmoBoxData] = {
    Success(DetailedAmmoBoxData(0, 1))
  }
}
