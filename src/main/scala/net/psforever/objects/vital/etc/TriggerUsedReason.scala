// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.BoomerTrigger
import net.psforever.objects.ballistics.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.{NoResistanceSelection, SimpleResolutions}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.damage.DamageCalculations.AgainstExoSuit
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.{DamageAndResistance, DamageResistanceModel}

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain a `BoomerDeployable` being detonated
  * using its complementary trigger.
  * Should be applied as the reason applied to the Boomer
  * in `DamageInteractions` that lead up to the Boomer exploding
  * which will carry the trigger as the reason and the user as the culprit.
  * Due to faction affiliation complicity between the user and the Boomer, however,
  * normal `Damageable` functionality would have to interject in a way where the trigger works anyway.
  * @see `BoomerDeployable`
  * @see `BoomerTrigger`
  * @see `DamageCalculations`
  * @see `VitalityDefinition.DamageableByFriendlyFire`
  * @param user the player who is holding the trigger
  * @param item the trigger
  */
final case class TriggerUsedReason(user: PlayerSource, item: BoomerTrigger)
  extends DamageReason {
  def source: DamageProperties = TriggerUsedReason.triggered

  def resolution: DamageResolution.Value = DamageResolution.Resolved

  def same(test: DamageReason): Boolean = test match {
    case tur: TriggerUsedReason => tur.item eq item
    case _                      => false
  }

  /** lay the blame on the player who caused this explosion to occur */
  def adversary: Option[SourceEntry] = Some(user)

  override def damageModel : DamageAndResistance = TriggerUsedReason.drm

  /** while weird, the trigger was accredited as the method of death on Gemini Live;
    * even though its icon looks like an misshapen AMS */
  override def attribution: Int = item.Definition.ObjectId
}

object TriggerUsedReason {
  private val triggered = new DamageProperties {
    Damage0 = 1 //token damage
    SympatheticExplosion = true //sets off a boomer
  }

  /** basic damage, no resisting, quick and simple */
  private val drm = new DamageResistanceModel {
    DamageUsing = AgainstExoSuit
    ResistUsing = NoResistanceSelection
    Model = SimpleResolutions.calculate
  }
}