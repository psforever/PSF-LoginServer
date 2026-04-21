// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.{NoResistanceSelection, SimpleResolutions}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.{DamageAndResistance, DamageResistanceModel}

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain wanting to kill oneself.
  */
final case class SuicideReason()
  extends DamageReason {
  /*
  In my head canon, there is a neverending termite eating into the Auraxian human empires.
  There is no recruitment.
  People are barely alive long enough to feel basic needs like hunger or thirst.
  All that is still thrives on the zealous fervor to keep the army motivated.
  But what do people do if they don't want to fight anymore?
  Do they just never come back from being a speck of thought floating in the air
  and, in frustration at life, endure in a disembodied, solitary limbo of the nanites?
  But that doesn't stop the thoughts, does it?
  Never able to go back to Earth;
  becoming a lifeform between organic and information, wandering the endless void known as space for eternity;
  being unable to die even though they wish for it;
  eventually, they stop logging in.

  Anyway, this has nothing to do with that.
  Most players probably just want to jump to the next base over.
  */
  def source: DamageProperties = SuicideReason.damageProperties

  def resolution: DamageResolution.Value = DamageResolution.Suicide

  def same(test: DamageReason): Boolean = {
    test.source eq source
  }

  def adversary: Option[SourceEntry] = None

  def damageModel: DamageAndResistance = SuicideReason.drm
}

object SuicideReason {
  /** one swift blow that guarantees death */
  val damageProperties = new DamageProperties {
    Damage0 = 99999
    DamageToHealthOnly = true
    DamageToVehicleOnly = true
    DamageToBattleframeOnly = true
  }

  /** damage0, no resisting, quick and simple */
  val drm = new DamageResistanceModel {
    DamageUsing = DamageCalculations.AgainstExoSuit
    ResistUsing = NoResistanceSelection
    Model = SimpleResolutions.calculate
  }
}
