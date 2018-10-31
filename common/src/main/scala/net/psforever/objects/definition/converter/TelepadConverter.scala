// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Telepad
import net.psforever.packet.game.objectcreate._

import scala.util.{Failure, Success, Try}

class TelepadConverter extends ObjectCreateConverter[Telepad]() {
  override def ConstructorData(obj : Telepad) : Try[TelepadData] = {
    obj.Router match {
      case Some(_) =>
        Success(TelepadData (0, obj.Router))
      case None =>
        Failure(new IllegalStateException("TelepadConverter: telepad needs to know id of its router"))
    }
  }

  override def DetailedConstructorData(obj : Telepad) : Try[DetailedTelepadData] = {
    obj.Router match {
      case Some(_) =>
        Success(DetailedTelepadData (0, obj.Router))
      case None =>
        Failure(new IllegalStateException("TelepadConverter: telepad needs to know id of its router"))
    }
  }
}
