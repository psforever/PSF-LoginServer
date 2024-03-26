// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.interior

import net.psforever.objects.avatar.interaction.WithEntrance
import net.psforever.objects.serverobject.environment.interaction.InteractWithEnvironment
import net.psforever.objects.zones.InteractsWithZone

import scala.annotation.unused

trait InteriorAware {
  def WhichSide: Sidedness
  def WhichSide_=(@unused thisSide: Sidedness): Sidedness
}

trait TraditionalInteriorAware
  extends InteriorAware {
  private var side: Sidedness = Sidedness.StrictlyBetweenSides

  def WhichSide: Sidedness = side

  def WhichSide_=(thisSide: Sidedness): Sidedness = {
    side = thisSide
    WhichSide
  }
}

trait InteriorAwareFromInteraction
  extends InteriorAware {
  awareness: InteractsWithZone =>
  private lazy val withEntrance: Option[WithEntrance] = {
    awareness
      .interaction()
      .collect { case i: InteractWithEnvironment => i.Interactions.values }
      .flatten
      .collectFirst { case i: WithEntrance => i }
  }

  def WhichSide: Sidedness = {
    withEntrance.map(_.WhichSide).getOrElse(Sidedness.StrictlyBetweenSides)
  }

  def WhichSide_=(thisSide: Sidedness): Sidedness = {
    withEntrance.foreach(_.WhichSide = thisSide)
    WhichSide
  }
}
