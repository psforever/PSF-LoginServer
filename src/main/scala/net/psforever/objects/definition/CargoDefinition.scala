// Copyright (c) 2017 PSForever
package net.psforever.objects.definition

import net.psforever.objects.vehicles.CargoVehicleRestriction

/**
  * The definition for a cargo hold.
  */
class CargoDefinition extends BasicDefinition {

  /** a restriction on the type of exo-suit a person can wear */
  private var vehicleRestriction: CargoVehicleRestriction.Value = CargoVehicleRestriction.Small

  /** the user can escape while the vehicle is moving */
  private var bailable: Boolean = true
  Name = "cargo"

  def CargoRestriction: CargoVehicleRestriction.Value = {
    this.vehicleRestriction
  }

  def CargoRestriction_=(restriction: CargoVehicleRestriction.Value): CargoVehicleRestriction.Value = {
    this.vehicleRestriction = restriction
    restriction
  }

  def Bailable: Boolean = {
    this.bailable
  }

  def Bailable_=(canBail: Boolean): Boolean = {
    this.bailable = canBail
    canBail
  }
}
