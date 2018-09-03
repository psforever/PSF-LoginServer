// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Telepad
import net.psforever.packet.game.objectcreate._

import scala.util.{Success, Try}

class TelepadConverter extends ObjectCreateConverter[Telepad]() {
  override def ConstructorData(obj : Telepad) : Try[TelepadData] =
    Success(TelepadData(0, obj.Router))

  override def DetailedConstructorData(obj : Telepad) : Try[DetailedTelepadData] =
    Success(DetailedTelepadData(0, obj.Router))
}
