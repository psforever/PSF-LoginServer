// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.definition.{ProjectileDefinition, ToolDefinition}
import net.psforever.objects.entity.SimpleWorldEntity
import net.psforever.types.Vector3

/**
  * A summation of weapon (`Tool`) discharge.
  * @param profile an explanation of the damage that can be performed by this discharge
  * @param tool_def the weapon that caused this discharge
  * @param shot_origin where the projectile started
  * @param shot_angle in which direction the projectile was aimed when it was discharged
  * @param resolution whether this projectile has encountered a target or wall;
  *                   defaults to `Unresolved`
  * @param fire_time when the weapon discharged was recorded;
  *                  defaults to `System.nanoTime`
  * @param hit_time when the discharge had its resolution status updated
  */
final case class Projectile(profile : ProjectileDefinition,
                            tool_def : ToolDefinition,
                            shot_origin : Vector3,
                            shot_angle : Vector3,
                            resolution : ProjectileResolution.Value,
                            fire_time : Long = System.nanoTime,
                            hit_time : Long = 0) {
  /** Information about the current world coordinates and orientation of the projectile */
  val current : SimpleWorldEntity = new SimpleWorldEntity()

  /**
    * Give the projectile the suggested resolution status.
    * Update the world coordinates and orientation.
    * @param pos the current position
    * @param ang the current orientation
    * @param resolution the resolution status
    * @return a new projectile with the suggested resolution status, or the original projectile
    */
  def Resolve(pos : Vector3, ang : Vector3, resolution : ProjectileResolution.Value) : Projectile = {
    val obj = Resolve(resolution)
    obj.current.Position = pos
    obj.current.Orientation = ang
    obj
  }

  /**
    * Give the projectile the suggested resolution status.
    * @param resolution the resolution status
    * @return a new projectile with the suggested resolution status, or the original projectile
    */
  def Resolve(resolution : ProjectileResolution.Value) : Projectile = {
    resolution match {
      case ProjectileResolution.Unresolved =>
        this
      case _ =>
        Projectile(profile, tool_def, shot_origin, shot_angle, resolution, fire_time, System.nanoTime)
    }
  }
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
  def apply(profile : ProjectileDefinition, tool_def : ToolDefinition, shot_origin : Vector3, shot_angle : Vector3) : Projectile = {
    Projectile(profile, tool_def, shot_origin, shot_angle, ProjectileResolution.Unresolved)
  }
}
