// Copyright (c) 2021 PSForever
package net.psforever.objects

import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.equipment.{EffectTarget, TargetValidation}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.{Vitality, VitalityDefinition}
import net.psforever.objects.vital.base.DamageType
import net.psforever.objects.vital.etc.EmpReason
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.projectile.MaxDistanceCutoff
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideEmpire, Vector3}

/**
  * Information and functions useful for the construction of a server-side electromagnetic pulse
  * (not intigated by any specific thing the client does).
  */
object SpecialEmp {
  /** A defaulted emp definition.
    * Any projectile definition can be used. */
  final val emp = new DamageWithPosition {
    CausesDamageType = DamageType.Splash
    SympatheticExplosion = true
    Damage0 = 0
    DamageAtEdge = 1.0f
    DamageRadius = 5f
    AdditionalEffect = true
    JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Player,
      EffectTarget.Validation.Player
    ) -> 1000
    JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.AMS
    ) -> 5000
    JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.MotionSensor
    ) -> 30000
    JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Deployable,
      EffectTarget.Validation.Spitfire
    ) -> 30000
    JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Turret,
      EffectTarget.Validation.Turret
    ) -> 30000
    JammedEffectDuration += TargetValidation(
      EffectTarget.Category.Vehicle,
      EffectTarget.Validation.VehicleNotAMS
    ) -> 10000
    Modifiers = MaxDistanceCutoff
  }

  /** The definition for a proxy object that represents a physical component of the source of the electromagnetic pulse. */
  private val proxy_definition = new ObjectDefinition(objectId = 420) with VitalityDefinition {
    Name = "emp"
    MaxHealth = 1
    Damageable = false
    Repairable = false
    innateDamage = emp
  }

  /**
    * Trigger an electromagnetic pulse.
    */
  final case class Burst()

  /**
    * The damage interaction for an electromagnetic pulse effect.
    * @param empEffect information about the effect
    * @param position where the effect occurs
    * @param source a game object that represents the source of the EMP
    * @param target a game object that is affected by the EMP
    * @return a `DamageInteraction` object
    */
  def createEmpInteraction(
                            empEffect: DamageWithPosition,
                            position: Vector3
                          )
                          (
                            source: PlanetSideGameObject with FactionAffinity with Vitality,
                            target: PlanetSideGameObject with FactionAffinity with Vitality
                          ): DamageInteraction = {
    DamageInteraction(
      SourceEntry(target),
      EmpReason(source, empEffect, target),
      position
    )
  }

  /**
    * The "within affected distance" test for special electromagnetic pulses
    * is not necessarily centered around a game object as the source of that EMP.
    * A proxy entity is generated to perform the measurements and
    * is given token information that connects it with another object for the proper attribution.
    * @see `OwnableByPlayer`
    * @see `PlanetSideServerObject`
    * @see `SpecialEmp.distanceCheck`
    * @param owner the formal entity to which the EMP is attributed
    * @param position the coordinate location of the EMP
    * @param faction the affinity of the EMP
    * @return a function that determines if two game entities are near enough to each other
    */
  def prepareDistanceCheck(
                     owner: PlanetSideGameObject,
                     position: Vector3,
                     faction: PlanetSideEmpire.Value
                   ): (PlanetSideGameObject, PlanetSideGameObject, Float) => Boolean = {
    distanceCheck(new PlanetSideServerObject with OwnableByPlayer {
      Owner = Some(owner.GUID)
      OwnerName = owner match {
        case p: Player          => p.Name
        case o: OwnableByPlayer => o.OwnerName.getOrElse("")
        case _                  => ""
      }
      Position = position
      def Faction = faction
      def Definition = proxy_definition
    })
  }

  /**
    * The "within affected distance" test for special electromagnetic pulses
    * is not necessarily centered around a game object as the source of that EMP.
    * A proxy entity is provided to perform the measurements and
    * is given token information that connects it with another object for the proper attribution.
    * @see `Zone.distanceCheck`
    * @param obj1 a game entity, should be the source of the damage
    * @param obj2 a game entity, should be the target of the damage
    * @param maxDistance the square of the maximum distance permissible between game entities
    *                    before they are no longer considered "near"
    * @return `true`, if the two entities are near enough to each other;
    *        `false`, otherwise
    */
  def distanceCheck(
                     proxy: PlanetSideGameObject
                   )
                   (
                     obj1: PlanetSideGameObject,
                     obj2: PlanetSideGameObject,
                     maxDistance: Float
                   ): Boolean = {
    Zone.distanceCheck(proxy, obj2, maxDistance)
  }

  /**
    * A sort of `SpecialEmp` that only affects deployed boomer explosives
    * must find affected deployed boomer explosive entities.
    * @see `BoomerDeployable`
    * @param range the distance to search for applicable sector
    * @param zone the zone in which to search
    * @param obj a game entity that is excluded from results
    * @param properties information about the effect/damage
    * @return two lists of objects with different characteristics;
    *         the first list is `PlanetSideServerObject` entities with `Vitality`;
    *         since only boomer explosives are returned, this second list can be ignored
    */
  def findAllBoomers(
                      range: Float
                    )
                    (
                      zone: Zone,
                      obj: PlanetSideGameObject with FactionAffinity with Vitality,
                      properties: DamageWithPosition
                    ): List[PlanetSideServerObject with Vitality] = {
    zone
      .blockMap
      .sector(obj.Position, range)
      .deployableList
      .collect { case o: BoomerDeployable if !o.Destroyed && (o ne obj) => o }
  }
}
