// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.Vehicle
import net.psforever.objects.definition.{CargoDefinition}

/**
  * Server-side support for a slot that vehicles can occupy
  * @param cargoDef the Definition that constructs this item and maintains some of its unchanging fields
  */
class Cargo(private val cargoDef: CargoDefinition) {
  private var occupant: Option[Vehicle] = None

  /**
    * Is the cargo hold occupied?
    * @return The vehicle in the cargo hold, or `None` if it is left vacant
    */
  def Occupant: Option[Vehicle] = {
    this.occupant
  }

  /**
    * A vehicle is trying to board the cargo hold
    * Cargo holds are exclusive positions that can only hold one vehicle at a time.
    * @param vehicle the vehicle boarding the cargo hold, or `None` if the vehicle is leaving
    * @return the vehicle sitting in this seat, or `None` if it is left vacant
    */
  def Occupant_=(vehicle: Vehicle): Option[Vehicle] = Occupant_=(Some(vehicle))

  def Occupant_=(vehicle: Option[Vehicle]): Option[Vehicle] = {
    if (vehicle.isDefined) {
      if (this.occupant.isEmpty) {
        this.occupant = vehicle
      }
    } else {
      this.occupant = None
    }
    this.occupant
  }

  /**
    * Is this cargo hold occupied?
    * @return `true`, if it is occupied; `false`, otherwise
    */
  def isOccupied: Boolean = {
    this.occupant.isDefined
  }

  def CargoRestriction: CargoVehicleRestriction.Value = {
    cargoDef.CargoRestriction
  }

  def Bailable: Boolean = {
    cargoDef.Bailable
  }

  /**
    * Override the string representation to provide additional information.
    * @return the string output
    */
  override def toString: String = {
    Cargo.toString(this)
  }
}

object Cargo {

  /**
    * Overloaded constructor.
    * @return a `Cargo` object
    */
  def apply(cargoDef: CargoDefinition): Cargo = {
    new Cargo(cargoDef)
  }

  /**
    * Provide a fixed string representation.
    * @return the string output
    */
  def toString(obj: Cargo): String = {
    val cargoStr = if (obj.isOccupied) {
      s", occupied by vehicle ${obj.Occupant.get.GUID}"
    } else {
      ""
    }
    s"cargo$cargoStr"
  }
}
