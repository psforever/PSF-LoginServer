// Copyright (c) 2020 PSForever
package net.psforever.objects.vital.environment

import net.psforever.objects.ballistics.SourceEntry
import net.psforever.objects.serverobject.environment.{EnvironmentAttribute, PieceOfEnvironment}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.{DamageAndResistance, DamageResistanceModel}
import net.psforever.objects.vital.{NoResistanceSelection, SimpleResolutions, Vitality}

/**
  * A wrapper for a "damage source" in damage calculations
  * that parameterizes information necessary to explain the environment being antagonistic.
  * @see `DamageCalculations`
  * @param body a representative of an element of the environment
  * @param against for the purposes of damage, what kind of target is being acted upon
  */
final case class EnvironmentReason(body: PieceOfEnvironment, against: DamageCalculations.Selector) extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Hit

  def source: DamageProperties = EnvironmentReason.selectDamage(body)

  def same(test: DamageReason): Boolean = {
    test match {
      case o : EnvironmentReason => body == o.body //TODO eq
      case _ => false
    }
  }

  def adversary: Option[SourceEntry] = None

  def damageModel: DamageAndResistance = EnvironmentReason.drm(against)
}

object EnvironmentReason {
  /**
    * Overloaded constructor.
    * @param body a representative of an element of the environment
    * @param target the target being involved in this interaction
    * @return an `EnvironmentReason` object
    */
  def apply(body: PieceOfEnvironment, target: Vitality): EnvironmentReason =
    EnvironmentReason(body, target.DamageModel.DamageUsing)

  /** variable, no resisting, quick and simple */
  def drm(against: DamageCalculations.Selector) = new DamageResistanceModel {
    DamageUsing = against
    ResistUsing = NoResistanceSelection
    Model = SimpleResolutions.calculate
  }

  /** The flags for calculating an absence of environment damage. */
  private val noDamage = new DamageProperties { }
  /** The flags for calculating lava-based environment damage. */
  private val lavaDamage = new DamageProperties {
    Damage0 = 5 //20 dps per 250ms
    Damage1 = 37 //150 dps per 250ms
    Damage2 = 12 //50 dps per 250ms
    Damage3 = 12 //50 dps per 250ms
    Damage4 = 37 //150 dps per 250ms
    DamageToHealthOnly = true
    DamageToVehicleOnly = true
    DamageToBattleframeOnly = true
    Modifiers = LavaDepth
    //TODO Aggravated?
  }

  /**
    * Given an element in the environment,
    * denote the type of flags and values used in the damage resulting from an interaction.
    * @param environment the environmental element, with a specific attribute
    * @return the damage information flags for that attribute
    */
  def selectDamage(environment: PieceOfEnvironment): DamageProperties = {
    environment.attribute match {
      case EnvironmentAttribute.Lava => lavaDamage
      case _ => noDamage
    }
  }
}
