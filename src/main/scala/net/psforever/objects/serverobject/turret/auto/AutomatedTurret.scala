// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.turret.auto

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.turret.{TurretDefinition, WeaponTurret}
import net.psforever.objects.sourcing.{SourceEntry, SourceUniqueness}
import net.psforever.objects.vital.Vitality

trait AutomatedTurret
  extends PlanetSideServerObject
    with WeaponTurret {
  import AutomatedTurret.Target
  private var currentTarget: Option[Target] = None

  private var targets: List[Target] = List[Target]()

  /**
   * The entity that claims responsibility for the actions of the turret
   * or has authoritative management over the turret.
   * When no one else steps up to the challenge, the turret can be its own person.
   * @return owner entity
   */
  def TurretOwner: SourceEntry

  def Target: Option[Target] = currentTarget

  def Target_=(newTarget: Target): Option[Target] = {
    Target_=(Some(newTarget))
  }

  def Target_=(newTarget: Option[Target]): Option[Target] = {
    if (newTarget.isDefined != currentTarget.isDefined) {
      currentTarget = newTarget
    }
    currentTarget
  }

  def Targets: List[Target] = targets

  def Detected(target: Target): Option[Target] = {
    val unique = SourceEntry(target).unique
    targets.find(SourceEntry(_).unique == unique)
  }

  def Detected(target: SourceUniqueness): Option[Target] = {
    targets.find(SourceEntry(_).unique == target)
  }

  def AddTarget(target: Target): Unit = {
    targets = targets :+ target
  }

  def RemoveTarget(target: Target): Unit = {
    val unique = SourceEntry(target).unique
    targets = targets.filterNot(SourceEntry(_).unique == unique)
  }

  def Clear(): List[Target] = {
    val oldTargets = targets
    targets = Nil
    oldTargets
  }

  def Definition: ObjectDefinition with TurretDefinition
}

object AutomatedTurret {
  type Target = PlanetSideServerObject with Vitality
}
