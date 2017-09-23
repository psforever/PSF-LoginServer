// Copyright (c) 2017 PSForever
package net.psforever.objects.continent

import akka.actor.ActorContext
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.guid.NumberPoolHub

trait ServerObjectBuilder {
  def Build(implicit context : ActorContext, guid : NumberPoolHub) : PlanetSideGameObject
}
