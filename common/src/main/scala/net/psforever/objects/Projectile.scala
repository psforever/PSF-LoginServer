// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.{ProjectileDefinition, ToolDefinition}
import net.psforever.objects.entity.SimpleWorldEntity
import net.psforever.objects.ballistics.ProjectileResolution
import net.psforever.types.Vector3

final case class Projectile(profile : ProjectileDefinition,
                            tool_def : ToolDefinition,
                            pos : Vector3,
                            orient : Vector3,
                            resolution : ProjectileResolution.Value,
                            fire_time : Long = System.nanoTime,
                            hit_time : Long = 0) {
  val current : SimpleWorldEntity = new SimpleWorldEntity()

  def Resolve(hitPos : Vector3, hitAng : Vector3, hitVel : Vector3, resolution : ProjectileResolution.Value) : Projectile = {
    val obj = Resolve(resolution)
    obj.current.Position = hitPos
    obj.current.Orientation = hitAng
    obj.current.Velocity = hitVel
    obj
  }

  def Resolve(resolution : ProjectileResolution.Value) : Projectile = {
    resolution match {
      case ProjectileResolution.Unresolved =>
        this
      case _ =>
        Projectile(profile, tool_def, pos, orient, resolution, fire_time, System.nanoTime)
    }
  }
}

object Projectile {
  final val BaseUID : Int = 40100

  def apply(profile : ProjectileDefinition, tool_def : ToolDefinition, pos : Vector3, orient : Vector3) : Projectile = {
    Projectile(profile, tool_def, pos, orient, ProjectileResolution.Unresolved)
  }
}
