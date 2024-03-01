// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.sourcing.{DeployableSource, SourceEntry}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.DamageAndResistance

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain an `ExplosiveDeployable` being detonated
  * by incursion of an acceptable target into its automatic triggering range.
  * @see `ExplosiveDeployable`
  * @param mine na
  * @param owner na
  */
final case class TrippedMineReason(mine: DeployableSource, owner: SourceEntry)
  extends DamageReason {

  def source: DamageProperties = TrippedMineReason.triggered

  def resolution: DamageResolution.Value = DamageResolution.Resolved

  def same(test: DamageReason): Boolean = test match {
    case trip: TrippedMineReason => mine.unique == trip.mine.unique && owner.unique == owner.unique
    case _                       => false
  }

  /** lay the blame on the player who laid this mine, if possible */
  def adversary: Option[SourceEntry] = Some(owner)

  override def damageModel: DamageAndResistance = mine.Definition

  override def attribution: Int = mine.Definition.ObjectId
}

object TrippedMineReason {
  private val triggered = new DamageProperties {
    Damage0 = 1 //token damage
    SympatheticExplosion = true //sets off mine
  }
}
