//Copyright (c) 2020 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.{PlanetSideGameObject, Player}
import net.psforever.objects.equipment.EquipmentSize
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.base.{DamageResolution, DamageType}
import net.psforever.types.{ImplantType, Vector3}

/**
  * Projectile quality is an external aspect of projectiles
  * that is not dependent on hard-coded definitions of the entities
  * used to compose the projectile such as the knowlegde of the emitting `Tool` (weapon).
  * A flag or a damage modifier, depending on use.
  * To the extent that it can be used as a numeric modifier,
  * insists on defining a numeric modifier component rather to what it is trying to express.
  * That numeric modifier does not have to be used for anything.
  */
sealed trait ProjectileQuality {
  def mod: Float
}

/**
  * Implement the numeric modifier with the value as one.
  */
sealed trait SameAsQuality extends ProjectileQuality {
  def mod: Float = 1f
}

object ProjectileQuality {
  /** Standard projectile quality.  More of a flag than a modifier. */
  case object Normal extends SameAsQuality

  /** Quality that flags the first stage of aggravation (initial damage). */
  case object AggravatesTarget extends SameAsQuality

  /** The complete lack of quality.  Even the numeric modifier is zeroed. */
  case object Zeroed extends ProjectileQuality { def mod = 0f }

  /** Assign a custom numeric qualifier value, usually to be applied to damage calculations. */
  case class Modified(mod: Float) extends ProjectileQuality

  /**
    * na
    * @param projectile the projectile object
    * @param resolution the resolution status to promote the projectile
    * @return a copy of the projectile
    */
  def modifiers(
                 projectile: Projectile,
                 resolution: DamageResolution.Value,
                 target: PlanetSideGameObject with FactionAffinity with Vitality,
                 pos: Vector3,
                 user: Option[Player]
               ): Projectile = {
    projectile.Resolve() //if not yet resolved once
    if (projectile.profile.ProjectileDamageTypes.contains(DamageType.Aggravated)) {
      //aggravated
      val quality = projectile.profile.Aggravated match {
        case Some(aggravation)
          if aggravation.targets.exists(validation => validation.test(target)) &&
             aggravation.info.exists(_.damage_type == AggravatedDamage.basicDamageType(resolution)) =>
          ProjectileQuality.AggravatesTarget
        case _ =>
          ProjectileQuality.Normal
      }
      projectile.quality(quality)
    } else if (projectile.tool_def.Size == EquipmentSize.Melee) {
      //melee
      user match {
        case Some(player) =>
          val quality = player.avatar.implants.flatten.find { entry => entry.definition.implantType == ImplantType.MeleeBooster } match {
            case Some(booster) if booster.active && player.avatar.stamina > 9 =>
              ProjectileQuality.Modified(25f)
            case _ =>
              ProjectileQuality.Normal
          }
          projectile.quality(quality)
        case None =>
          projectile
      }
    } else {
      projectile
    }
  }
}
