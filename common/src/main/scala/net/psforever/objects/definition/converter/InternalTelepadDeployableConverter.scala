// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ce.TelepadLike
import net.psforever.packet.game.objectcreate._

import scala.util.{Success, Try}

class InternalTelepadDeployableConverter extends ObjectCreateConverter[PlanetSideGameObject with TelepadLike]() {
  override def ConstructorData(obj : PlanetSideGameObject with TelepadLike) : Try[ContainedTelepadDeployableData] = {
    Success(ContainedTelepadDeployableData(101, obj.Router.get))
  }
}
