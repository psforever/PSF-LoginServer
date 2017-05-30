// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.definition.SeatDefinition
import net.psforever.objects.{Player, Vehicle}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire

/**
  * Server-side support for a slot that infantry players can occupy, ostensibly called a "seat" and treated like a "seat."
  * (Players can sit in it.)
  * @param seatDef the Definition that constructs this item and maintains some of its immutable fields
  * @param vehicle the vehicle where this seat is installed
  */
class Seat(private val seatDef : SeatDefinition, private val vehicle : Vehicle) {
  private var occupant : Option[PlanetSideGUID] = None
  private var lockState :  VehicleLockState.Value =  VehicleLockState.Empire

  /**
    * The faction association of this `Seat` is tied directly to the connected `Vehicle`.
    * @return the faction association
    */
  def Faction : PlanetSideEmpire.Value = {
    vehicle.Faction
  }

  /**
    * Is this seat occupied?
    * @return the GUID of the player sitting in this seat, or `None` if it is left vacant
    */
  def Occupant : Option[PlanetSideGUID] = {
    this.occupant
  }

  /**
    * The player is trying to sit down.
    * Seats are exclusive positions that can only hold one occupant at a time.
    * @param player the player who wants to sit, or `None` if the occupant is getting up
    * @return the GUID of the player sitting in this seat, or `None` if it is left vacant
    */
  def Occupant_=(player : Option[Player]) : Option[PlanetSideGUID] = {
    if(player.isDefined) {
      if(this.occupant.isEmpty) {
        this.occupant = Some(player.get.GUID)
      }
    }
    else {
      this.occupant = None
    }
    this.occupant
  }

  /**
    * Is this seat occupied?
    * @return `true`, if it is occupied; `false`, otherwise
    */
  def isOccupied : Boolean = {
    this.occupant.isDefined
  }

  def SeatLockState : VehicleLockState.Value = {
    this.lockState
  }

  def SeatLockState_=(lockState : VehicleLockState.Value) :  VehicleLockState.Value = {
    this.lockState = lockState
    SeatLockState
  }

  def ArmorRestriction : SeatArmorRestriction.Value = {
    seatDef.ArmorRestriction
  }

  def Bailable : Boolean = {
    seatDef.Bailable
  }

  def ControlledWeapon : Option[Int] = {
    seatDef.ControlledWeapon
  }

  /**
    * Given a player, can they access this `Seat` under its current restrictions and permissions.
    * @param player the player who wants to sit
    * @return `true` if the player can sit down in this `Seat`; `false`, otherwise
    */
  def CanUseSeat(player : Player) : Boolean = {
    var access : Boolean = false
    val owner : Option[PlanetSideGUID] = vehicle.Owner
    lockState match {
      case VehicleLockState.Locked =>
        access = owner.isEmpty || (owner.isDefined && player.GUID == owner.get)
      case VehicleLockState.Group =>
        access = Faction == player.Faction //TODO this is not correct
      case VehicleLockState.Empire =>
        access = Faction == player.Faction
    }
    access
  }

  /**
    * Override the string representation to provide additional information.
    * @return the string output
    */
  override def toString : String = {
    Seat.toString(this)
  }
}

object Seat {
  /**
    * Overloaded constructor.
    * @param vehicle the vehicle where this seat is installed
    * @return a `Seat` object
    */
  def apply(seatDef : SeatDefinition, vehicle : Vehicle) : Seat = {
    new Seat(seatDef, vehicle)
  }

  /**
    * Provide a fixed string representation.
    * @return the string output
    */
  def toString(obj : Seat) : String = {
    val weaponStr = if(obj.ControlledWeapon.isDefined) { " (gunner)" } else { "" }
    val seatStr = if(obj.isOccupied) {
      "occupied by %d".format(obj.Occupant.get.guid)
    }
    else {
      "unoccupied"
    }
    s"{Seat$weaponStr: $seatStr}"
  }
}
