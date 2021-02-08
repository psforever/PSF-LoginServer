// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.prop.DamageWithPosition
import net.psforever.objects.vital.resolution.DamageAndResistance

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain a server-driven electromagnetic pulse occurring.
  * @see `VitalityDefinition.explodes`
  * @see `VitalityDefinition.innateDamage`
  * @see `Zone.causesSpecialEmp`
  * @param entity the source of the explosive yield
  * @param damageModel the model to be utilized in these calculations;
  *                    typically, but not always, defined by the target
  */
final case class EmpReason(
                            entity: SourceEntry,
                            source: DamageWithPosition,
                            damageModel: DamageAndResistance,
                            override val attribution: Int
                          ) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Splash

  def same(test: DamageReason): Boolean = test match {
    case eer: ExplodingEntityReason => eer.entity eq entity
    case _                          => false
  }

  /** lay the blame on that which caused this emp to occur */
  def adversary: Option[SourceEntry] = Some(entity)
}

object EmpReason {
  def apply(
             owner: PlanetSideGameObject with FactionAffinity,
             source: DamageWithPosition,
             target: PlanetSideServerObject with Vitality
           ): EmpReason = {
    EmpReason(SourceEntry(owner), source, target.DamageModel, owner.Definition.ObjectId)
  }
}
