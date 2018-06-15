// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import net.psforever.objects.definition.{ProjectileDefinition, ToolDefinition}
import net.psforever.objects.entity.SimpleWorldEntity
import net.psforever.types.Vector3

final case class Projectile(profile : ProjectileDefinition,
                            tool_def : ToolDefinition,
                            shot_origin : Vector3,
                            shot_angle : Vector3,
                            resolution : ProjectileResolution.Value,
                            fire_time : Long = System.nanoTime,
                            hit_time : Long = 0) {
  val current : SimpleWorldEntity = new SimpleWorldEntity()

  def Resolve(hitPos : Vector3, hitAng : Vector3, resolution : ProjectileResolution.Value) : Projectile = {
    val obj = Resolve(resolution)
    obj.current.Position = hitPos
    obj.current.Orientation = hitAng
    obj
  }

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

  def apply(profile : ProjectileDefinition, tool_def : ToolDefinition, shot_origin : Vector3, shot_angle : Vector3) : Projectile = {
    Projectile(profile, tool_def, shot_origin, shot_angle, ProjectileResolution.Unresolved)
  }
}
