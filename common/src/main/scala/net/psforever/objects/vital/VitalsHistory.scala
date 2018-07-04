// Copyright (c) 2017 PSForever
package net.psforever.objects.vital

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ballistics.ResolvedProjectile

trait VitalsHistory {
  this : PlanetSideGameObject =>

  private var projectileHistory : List[ResolvedProjectile] = List.empty

  def History(projectile : ResolvedProjectile) : List[ResolvedProjectile] = {
    projectileHistory = projectileHistory :+ projectile
    projectileHistory
  }

  def LastShot : Option[ResolvedProjectile] = projectileHistory.lastOption
}
