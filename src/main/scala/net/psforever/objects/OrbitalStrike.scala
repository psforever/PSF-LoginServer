// Copyright (c) 2025 PSForever
package net.psforever.objects

import net.psforever.objects.sourcing.{PlayerSource, SourceEntry}
import net.psforever.objects.vital.{NoResistanceSelection, SimpleResolutions}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution, DamageType}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital.prop.{DamageProperties, DamageWithPosition}
import net.psforever.objects.vital.resolution.{DamageAndResistance, DamageResistanceModel}

final case class OrbitalStrike(player: PlayerSource)
  extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Hit

  def same(test: DamageReason): Boolean = {
    test.source eq source
  }

  def source: DamageProperties = OrbitalStrike.source

  def damageModel: DamageAndResistance = OrbitalStrike.drm

  override def adversary : Option[SourceEntry] = Some(player)

}

object OrbitalStrike {

  final val cr4_os = new DamageWithPosition {
    CausesDamageType = DamageType.Splash
    SympatheticExplosion = true
    Damage0 = 10000
    DamageAtEdge = 0.1f
    DamageRadius = 10f
  }

  final val cr5_os = new DamageWithPosition {
    CausesDamageType = DamageType.Splash
    SympatheticExplosion = true
    Damage0 = 10000
    DamageAtEdge = 0.1f
    DamageRadius = 20f
  }

  private val source = new DamageProperties {
    Damage0 = 10000
    Damage1 = 10000
    Damage2 = 10000
    Damage3 = 10000
    Damage4 = 10000
    DamageToHealthOnly = true
    DamageToVehicleOnly = true
    DamageToBattleframeOnly = true
  }

  private val drm = new DamageResistanceModel {
    DamageUsing = DamageCalculations.AgainstExoSuit
    ResistUsing = NoResistanceSelection
    Model = SimpleResolutions.calculate
  }
}
