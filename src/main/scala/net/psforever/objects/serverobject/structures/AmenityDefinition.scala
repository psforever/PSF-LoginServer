// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.structures

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.environment.{EnvironmentTrait, PieceOfEnvironment}
import net.psforever.objects.vital.damage.DamageCalculations
import net.psforever.objects.vital._
import net.psforever.objects.vital.resistance.ResistanceProfileMutators
import net.psforever.objects.vital.resolution.DamageResistanceModel

import scala.annotation.unused

final case class AutoRepairStats(amount: Float, start: Long, repeat: Long, drain: Float)

trait CreateEnvironmentField {
  //todo a way to probe for this property from create(...)'s output
  def attribute: EnvironmentTrait

  def create(@unused obj: Amenity): PieceOfEnvironment
}

abstract class AmenityDefinition(objectId: Int)
  extends ObjectDefinition(objectId)
    with ResistanceProfileMutators
    with DamageResistanceModel
    with VitalityDefinition {
  Name = "amenity"
  DamageUsing = DamageCalculations.AgainstVehicle
  ResistUsing = StandardAmenityResistance
  Model = SimpleResolutions.calculate

  var autoRepair: Option[AutoRepairStats] = None

  var fields: Seq[CreateEnvironmentField] = Seq()

  def autoRepair_=(auto: AutoRepairStats): Option[AutoRepairStats] = {
    autoRepair = Some(auto)
    autoRepair
  }

  def hasAutoRepair: Boolean = autoRepair.nonEmpty

  def environmentField: Seq[CreateEnvironmentField] = fields

  def environmentField_=(theField: CreateEnvironmentField): Seq[CreateEnvironmentField] = {
    environmentField_=(Seq(theField))
  }

  def environmentField_=(theFields: Seq[CreateEnvironmentField]): Seq[CreateEnvironmentField] = {
    fields = fields ++ theFields
    environmentField
  }
}
