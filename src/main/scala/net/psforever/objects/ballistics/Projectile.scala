// Copyright (c) 2017 PSForever
package net.psforever.objects.ballistics

import java.util.concurrent.atomic.AtomicLong

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.{ProjectileDefinition, ToolDefinition}
import net.psforever.objects.entity.SimpleWorldEntity
import net.psforever.objects.equipment.FireModeDefinition
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.types.Vector3

/**
  * A summation of weapon (`Tool`) discharge.
  * @see `ProjectileDefinition`
  * @see `ToolDefinition`
  * @see `FireModeDefinition`
  * @see `SourceEntry`
  * @see `PlayerSource`
  * @param profile an explanation of the damage that can be performed by this discharge
  * @param tool_def the weapon that caused this discharge
  * @param fire_mode the current fire mode of the tool used
  * @param owner the agency that caused the weapon to produce this projectile;
  *              most often a player (`PlayerSource`)
  * @param attribute_to an object ID that refers to the method of death that would be reported;
  *                     usually the same as `tool_def.ObjectId`;
  *                     if not, then it is a type of vehicle (and owner should have a positive `seated` field)
  * @param shot_origin where the projectile started
  * @param shot_angle in which direction the projectile was aimed when it was discharged
  * @param quality na
  * @param id an exclusive identifier for this projectile;
  *           normally generated internally, but can be manually set
  * @param fire_time when the weapon discharged was recorded;
  *                  defaults to `System.nanoTime`
  */
final case class Projectile(
    profile: ProjectileDefinition,
    tool_def: ToolDefinition,
    fire_mode: FireModeDefinition,
    owner: SourceEntry,
    attribute_to: Int,
    shot_origin: Vector3,
    shot_angle: Vector3,
    quality: ProjectileQuality = ProjectileQuality.Normal,
    id: Long = Projectile.idGenerator.getAndIncrement(),
    fire_time: Long = System.currentTimeMillis()
) extends PlanetSideGameObject {
  Position = shot_origin
  Orientation = shot_angle
  Velocity = {
    val initVel: Int     = profile.InitialVelocity              //initial velocity
    val radAngle: Double = math.toRadians(shot_angle.y)         //angle of elevation
    val rise: Float      = initVel * math.sin(radAngle).toFloat //z
    val ground: Float    = initVel * math.cos(radAngle).toFloat //base
    Vector3.Rz(Vector3(0, -ground, 0), shot_angle.z) + Vector3.z(rise)
  }

  /** Information about the current world coordinates and orientation of the projectile */
  val current: SimpleWorldEntity                   = new SimpleWorldEntity()
  private var resolved: ProjectileResolution.Value = ProjectileResolution.Unresolved

  /**
    * Create a copy of this projectile with all the same information
    * save for the quality.
    * Used mainly for aggravated damage.
    * It is important to note that the new projectile shares the (otherwise) exclusive id of the original.
    * @param value the new quality
    * @return a new `Projectile` entity
    */
  def quality(value: ProjectileQuality): Projectile = {
    val projectile = Projectile(
      profile,
      tool_def,
      fire_mode,
      owner,
      attribute_to,
      shot_origin,
      shot_angle,
      value,
      id,
      fire_time
    )
    if(isMiss) projectile.Miss()
    else if(isResolved) projectile.Resolve()
    projectile
  }

  /**
    * Mark the projectile as being "encountered" or "managed" at least once.
    */
  def Resolve(): Unit = {
    resolved = ProjectileResolution.Resolved
  }

  def Miss(): Unit = {
    resolved = ProjectileResolution.MissedShot
  }

  def isResolved: Boolean = resolved == ProjectileResolution.Resolved || resolved == ProjectileResolution.MissedShot

  def isMiss: Boolean = resolved == ProjectileResolution.MissedShot

  def Definition = profile
}

object Projectile {

  /** the first projectile GUID used by all clients internally */
  final val baseUID: Int = 40100

  /** all clients progress through 40100 to 40124 normally, skipping only for long-lived projectiles
    * 40125 to 40149 are being reserved as a guard against undetected overflow
    */
  final val rangeUID: Int = 40150

  private val idGenerator: AtomicLong = new AtomicLong

  /**
    * Overloaded constructor for an `Unresolved` projectile.
    * @param profile an explanation of the damage that can be performed by this discharge
    * @param tool_def the weapon that caused this discharge
    * @param fire_mode the current fire mode of the tool used
    * @param owner the agency that caused the weapon to produce this projectile
    * @param shot_origin where the projectile started
    * @param shot_angle in which direction the projectile was aimed when it was discharged
    * @return the `Projectile` object
    */
  def apply(
      profile: ProjectileDefinition,
      tool_def: ToolDefinition,
      fire_mode: FireModeDefinition,
      owner: PlanetSideGameObject with FactionAffinity,
      shot_origin: Vector3,
      shot_angle: Vector3
  ): Projectile = {
    Projectile(profile, tool_def, fire_mode, SourceEntry(owner), tool_def.ObjectId, shot_origin, shot_angle)
  }

  /**
    * Overloaded constructor for an `Unresolved` projectile.
    * @param profile an explanation of the damage that can be performed by this discharge
    * @param tool_def the weapon that caused this discharge
    * @param fire_mode the current fire mode of the tool used
    * @param owner the agency that caused the weapon to produce this projectile
    * @param attribute_to an object ID that refers to the method of death that would be reported
    * @param shot_origin where the projectile started
    * @param shot_angle in which direction the projectile was aimed when it was discharged
    * @return the `Projectile` object
    */
  def apply(
      profile: ProjectileDefinition,
      tool_def: ToolDefinition,
      fire_mode: FireModeDefinition,
      owner: PlanetSideGameObject with FactionAffinity,
      attribute_to: Int,
      shot_origin: Vector3,
      shot_angle: Vector3
  ): Projectile = {
    Projectile(profile, tool_def, fire_mode, SourceEntry(owner), attribute_to, shot_origin, shot_angle)
  }
}
