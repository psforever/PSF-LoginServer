// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.AmmoBox
import net.psforever.packet.game.objectcreate.{AmmoBoxData, DetailedAmmoBoxData}

import scala.util.{Success, Try}

class AmmoBoxConverter extends ObjectCreateConverter[AmmoBox] {
  override def ConstructorData(obj : AmmoBox) : Try[AmmoBoxData] = {
    Success(AmmoBoxData())
  }

  override def DetailedConstructorData(obj : AmmoBox) : Try[DetailedAmmoBoxData] = {
    Success(DetailedAmmoBoxData(8, obj.Capacity))
  }
}
