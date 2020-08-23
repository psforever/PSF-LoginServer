// Copyright (c) 2017 PSForever
package net.psforever.objects.vital.resolution

/**
  * Maintain information about four target types as the entry points for damage calculation.
  */
trait ResolutionSelection {
  def Infantry: ResolutionCalculations.Form
  def Max: ResolutionCalculations.Form
  def Vehicle: ResolutionCalculations.Form
  def Aircraft: ResolutionCalculations.Form
  def SimpleDeployables: ResolutionCalculations.Form
  def ComplexDeployables: ResolutionCalculations.Form
  def FacilityTurrets: ResolutionCalculations.Form
  def Amenities: ResolutionCalculations.Form
}
