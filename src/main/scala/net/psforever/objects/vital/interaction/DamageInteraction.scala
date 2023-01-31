// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.interaction

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.base.{DamageReason, DamageResolution, DamageType}
import net.psforever.objects.vital.resolution.{DamageAndResistance, ResolutionCalculations}
import net.psforever.types.Vector3

/**
  * The recorded encounter of a damage source and a damageable target.
  * @param target the original affected target;
  *               not necessarily the currently affected target
  * @param hitPos the coordinate location where the damage was inflicted
  * @param cause the method by which the damage was produced
  * @param resolution how the damage is being processed
  * @param hitTime when the interaction originally occurred;
  *                defaults to `System.currentTimeMills()` at object creation
  */
final case class DamageInteraction(
                                    target: SourceEntry,
                                    hitPos: Vector3,
                                    cause: DamageReason,
                                    resolution: DamageResolution.Value,
                                    hitTime: Long = System.currentTimeMillis()
                                  ) {
  /**
    * If the cause of the original interaction can be attributed to some agency.
    * @return a connection between offender, victim, and method
    */
  def adversarial: Option[Adversarial] = cause.adversary match {
    case Some(adversity) => Some(Adversarial(adversity, target, cause.attribution))
    case None            => None
  }

  /**
    * Process the primary parameters from the interaction
    * and produce the application function literal that can have a target entity applied to it.
    * @return the function that applies changes to a target entity
    */
  def calculate(): ResolutionCalculations.Output = cause.calculate(data = this)

  /**
    * Process the primary parameters from the interaction
    * including a custom category of damage by which to temporarily reframe the interaction
    * and produce the application function literal that can have a target entity applied to it.
    * @return the function that applies changes to a target entity
    */
  def calculate(dtype: DamageType.Value): ResolutionCalculations.Output = cause.calculate(data = this, dtype)

  /**
    * Process the primary parameters from the interaction
    * in the context a custom damage processing method by which to temporarily reframe the interaction
    * and produce a application function literal where the specified target entity can be applied to it.
    * @param model the custom processing method
    * @param target the target entity
    * @return the outcome of the interaction under the given re-framing
    */
  def calculate(model: DamageAndResistance)(target: PlanetSideGameObject with FactionAffinity): DamageResult = {
    model.calculate(data = this)(target)
  }
}

object DamageInteraction {
  /**
    * Overloaded constructor for an interaction.
    * Shuffle the order of parameters; let time default.
    * @param resolution how the damage is being processed
    * @param target the original affected target
    * @param cause the method by which the damage was produced
    * @param hitPos the coordinate location where the damage was inflicted
    * @return a `DamageInteraction` object
    */
  def apply(resolution: DamageResolution.Value, target: SourceEntry, cause: DamageReason, hitPos: Vector3): DamageInteraction = {
    DamageInteraction(
      target,
      hitPos,
      cause,
      resolution
    )
  }

  /**
    * Overloaded constructor for an interaction.
    * Use the resolution from the reason for the damage, shuffle the parameters, and let time default.
    * @param target the original affected target
    * @param cause the method by which the damage was produced
    * @param hitPos the coordinate location where the damage was inflicted
    * @return a `DamageInteraction` object
    */
  def apply(target: SourceEntry, cause: DamageReason, hitPos: Vector3): DamageInteraction = {
    DamageInteraction(
      target,
      hitPos,
      cause,
      cause.resolution
    )
  }
}
