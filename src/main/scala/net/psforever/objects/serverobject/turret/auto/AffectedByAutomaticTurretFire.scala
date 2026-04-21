// Copyright (c) 2024 PSForever
package net.psforever.objects.serverobject.turret.auto

import akka.actor.Actor
import net.psforever.objects.Tool
import net.psforever.objects.ballistics.{Projectile, ProjectileQuality}
import net.psforever.objects.serverobject.damage.Damageable
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.base.DamageResolution
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.types.Vector3

/**
 * With a timed messaging cycle from `AutomatedTurretBehavior`,
 * an implementation of this trait should be able to simulate being damaged by a source of automated weapon's fire
 * without needing a player character to experience the damage directly as is usual for a client's user.
 * As a drawback, however, it's not possible to validate against collision detection of any sort
 * so damage could be applied through trees and rocks and walls and other users.
 */
trait AffectedByAutomaticTurretFire extends Damageable {
  _: Actor =>
  def AffectedObject: AutomatedTurret.Target

  val takeAutomatedDamage: Receive = {
    case AiDamage(turret) =>
      performAutomatedDamage(turret)
  }

  protected def performAutomatedDamage(turret: AutomatedTurret): Unit = {
    val target = AffectedObject
    if (!(target.Destroyed || target.isMoving(test = 1f))) {
      val tool = turret.Weapons.values.head.Equipment.collect { case t: Tool => t }.get
      val projectileInfo = tool.Projectile
      val targetPos = target.Position
      val turretPos = turret.Position
      val correctedTargetPosition = targetPos + Vector3.z(value = 1f)
      val angle = Vector3.Unit(targetPos - turretPos)
      turret.Actor ! SelfReportedConfirmShot(target)
      val projectile = new Projectile(
        projectileInfo,
        tool.Definition,
        tool.FireMode,
        None,
        turret.TurretOwner,
        turret.Definition.ObjectId,
        turretPos + Vector3.z(value = 1f),
        angle,
        Some(angle * projectileInfo.FinalVelocity)
      )
      val modProjectile = ProjectileQuality.modifiers(
        projectile,
        DamageResolution.Hit,
        target,
        correctedTargetPosition,
        None
      )
      val resolvedProjectile = DamageInteraction(
        SourceEntry(target),
        ProjectileReason(DamageResolution.Hit, modProjectile, target.DamageModel),
        correctedTargetPosition
      )
      PerformDamageIfVulnerable(target, resolvedProjectile.calculate())
    }
  }
}
