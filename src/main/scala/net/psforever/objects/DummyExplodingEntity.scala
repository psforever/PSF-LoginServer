// Copyright (c) 2022 PSForever
package net.psforever.objects

import net.psforever.objects.ballistics.Projectile
import net.psforever.objects.definition.{ObjectDefinition, ProjectileDefinition}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.resolution.{DamageAndResistance, DamageResistanceModel}
import net.psforever.objects.vital.{Vitality, VitalityDefinition}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

class DummyExplodingEntity(
                            private val obj: PlanetSideGameObject,
                            private val faction: PlanetSideEmpire.Value
                          )
  extends PlanetSideGameObject
  with FactionAffinity
  with Vitality {
  override def GUID: PlanetSideGUID = obj.GUID

  override def Position: Vector3 = {
    if (super.Position == Vector3.Zero) {
      obj.Position
    } else {
      super.Position
    }
  }

  override def Orientation: Vector3 = {
    if (super.Orientation == Vector3.Zero) {
      obj.Orientation
    } else {
      super.Orientation
    }
  }

  override def Velocity : Option[Vector3] = {
    super.Velocity.orElse(obj.Velocity)
  }

  def Faction: PlanetSideEmpire.Value = faction

  def DamageModel: DamageAndResistance = DummyExplodingEntity.DefaultDamageResistanceModel

  def Definition: ObjectDefinition with VitalityDefinition = {
    new DefinitionWrappedInVitality(obj)
  }
}

private class DefinitionWrappedInVitality(private val entity: PlanetSideGameObject)
  extends ObjectDefinition(entity.Definition.ObjectId)
  with VitalityDefinition {
  private val internalDefinition = entity.Definition

  innateDamage = internalDefinition match {
    case v: VitalityDefinition if v.innateDamage.nonEmpty => v.innateDamage.get
    case p: ProjectileDefinition                          => p
    case _                                                => GlobalDefinitions.no_projectile
  }

  Name = internalDefinition.Name

  DefaultHealth = 1 //just cuz

  override def ObjectId: Int = entity match {
    case p: Projectile => p.tool_def.ObjectId //projectiles point back to the weapon of origin
    case _             => internalDefinition.ObjectId //what are we?
  }
}

object DummyExplodingEntity {
  final val DefaultDamageResistanceModel = new DamageResistanceModel { }

  def apply(obj: PlanetSideGameObject): DummyExplodingEntity = new DummyExplodingEntity(obj, PlanetSideEmpire.NEUTRAL)
}
