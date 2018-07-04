// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.Player
import net.psforever.objects.definition.{ProjectileDefinition, ToolDefinition}
import net.psforever.objects.entity.SimpleWorldEntity
import net.psforever.objects.equipment.FireModeDefinition
import net.psforever.types.Vector3

/**
  * A summation of weapon (`Tool`) discharge.
  * @param profile an explanation of the damage that can be performed by this discharge
  * @param tool_def the weapon that caused this discharge
  * @param shot_origin where the projectile started
  * @param shot_angle in which direction the projectile was aimed when it was discharged
  * @param fire_time when the weapon discharged was recorded;
  *                  defaults to `System.nanoTime`
  */
final case class Projectile(profile : ProjectileDefinition,
                            tool_def : ToolDefinition,
                            fire_mode : FireModeDefinition,
                            owner : SourceEntry,
                            shot_origin : Vector3,
                            shot_angle : Vector3,
                            fire_time: Long = System.nanoTime) {
  /** Information about the current world coordinates and orientation of the projectile */
  val current : SimpleWorldEntity = new SimpleWorldEntity()
  private var resolved : ProjectileResolution.Value = ProjectileResolution.Unresolved

  /**
    * Mark the projectile as being "encountered" or "managed" at least once.
    */
  def Resolve() : Unit = {
    resolved = ProjectileResolution.Resolved
  }

  def Miss() : Unit = {
    resolved = ProjectileResolution.MissedShot
  }

  def isResolved : Boolean = resolved == ProjectileResolution.Resolved || resolved == ProjectileResolution.MissedShot

  def isMiss : Boolean = resolved == ProjectileResolution.MissedShot
}

object Projectile {
  /** the first projectile GUID used by all clients internally */
  final val BaseUID : Int = 40100
  /** all clients progress through 40100 to 40124 normally, skipping only for long-lived projectiles
    * 40125 to 40149 are being reserved as a guard against undetected overflow */
  final val RangeUID : Int = 40150

  /**
    * Overloaded constructor for an `Unresolved` projectile.
    * @param profile an explanation of the damage that can be performed by this discharge
    * @param tool_def the weapon that caused this discharge
    * @param shot_origin where the projectile started
    * @param shot_angle in which direction the projectile was aimed when it was discharged
    * @return the `Projectile` object
    */
  def apply(profile : ProjectileDefinition, tool_def : ToolDefinition, fire_mode : FireModeDefinition, owner : Player, shot_origin : Vector3, shot_angle : Vector3) : Projectile = {
    Projectile(profile, tool_def, fire_mode, PlayerSource(owner), shot_origin, shot_angle)
  }
}
