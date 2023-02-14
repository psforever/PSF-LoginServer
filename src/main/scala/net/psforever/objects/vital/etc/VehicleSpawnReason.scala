// Copyright (c) 2021 PSForever
package net.psforever.objects.vital.etc

import net.psforever.objects.sourcing.SourceEntry
import net.psforever.objects.vital.{NoResistanceSelection, SimpleResolutions}
import net.psforever.objects.vital.base.{DamageReason, DamageResolution}
import net.psforever.objects.vital.damage.DamageCalculations.AgainstNothing
import net.psforever.objects.vital.prop.DamageProperties
import net.psforever.objects.vital.resolution.{DamageAndResistance, DamageResistanceModel}

/**
  * The instigating cause of dying on an operational vehicle spawn pad.
  * @param driver the driver whose vehicle was being created
  * @param vehicle the vehicle being created
  */
final case class VehicleSpawnReason(driver: SourceEntry, vehicle: SourceEntry)
  extends DamageReason {
  def resolution: DamageResolution.Value = DamageResolution.Resolved

  def same(test: DamageReason): Boolean = test match {
    case cause: VehicleSpawnReason =>
      driver.Name.equals(cause.driver.Name) &&
      (vehicle.Definition eq cause.vehicle.Definition)
    case _ =>
      false
  }

  def source: DamageProperties = VehicleSpawnReason.source

  def damageModel: DamageAndResistance = VehicleSpawnReason.drm

  override def adversary : Option[SourceEntry] = Some(driver)

  override def attribution : Int = vehicle.Definition.ObjectId
}

object VehicleSpawnReason {
  private val source = new DamageProperties { /*intentional blank*/ }

  /** basic damage, no resisting, quick and simple */
  private val drm = new DamageResistanceModel {
    DamageUsing = AgainstNothing
    ResistUsing = NoResistanceSelection
    Model = SimpleResolutions.calculate
  }
}
