// Copyright (c) 2017 PSForever
package net.psforever.objects.vehicles

import net.psforever.objects.definition.SeatDefinition
import net.psforever.objects.Player

/**
  * Server-side support for a slot that infantry players can occupy, ostensibly called a "seat" and treated like a "seat."
  * (Players can sit in it.)
  * @param seatDef the Definition that constructs this item and maintains some of its unchanging fields
  */
class Seat(private val seatDef: SeatDefinition) {
  private var occupant: Option[Player] = None
//  private var lockState : VehicleLockState.Value = VehicleLockState.Empire

  /**
    * Is this seat occupied?
    * @return the Player object of the player sitting in this seat, or `None` if it is left vacant
    */
  def Occupant: Option[Player] = {
    this.occupant
  }

  /**
    * The player is trying to sit down.
    * Seats are exclusive positions that can only hold one occupant at a time.
    * @param player the player who wants to sit, or `None` if the occupant is getting up
    * @return the Player object of the player sitting in this seat, or `None` if it is left vacant
    */
  def Occupant_=(player: Player): Option[Player] = Occupant_=(Some(player))

  def Occupant_=(player: Option[Player]): Option[Player] = {
    if (player.isDefined) {
      if (this.occupant.isEmpty) {
        this.occupant = player
      }
    } else {
      this.occupant = None
    }
    this.occupant
  }

  /**
    * Is this seat occupied?
    * @return `true`, if it is occupied; `false`, otherwise
    */
  def isOccupied: Boolean = {
    this.occupant.isDefined
  }

//  def SeatLockState : VehicleLockState.Value = {
//    this.lockState
//  }
//
//  def SeatLockState_=(lockState : VehicleLockState.Value) :  VehicleLockState.Value = {
//    this.lockState = lockState
//    SeatLockState
//  }

  def ArmorRestriction: SeatArmorRestriction.Value = {
    seatDef.ArmorRestriction
  }

  def Bailable: Boolean = {
    seatDef.Bailable
  }

  def ControlledWeapon: Option[Int] = {
    seatDef.ControlledWeapon
  }

  /**
    * Override the string representation to provide additional information.
    * @return the string output
    */
  override def toString: String = {
    Seat.toString(this)
  }
}

object Seat {

  /**
    * Overloaded constructor.
    * @return a `Seat` object
    */
  def apply(seatDef: SeatDefinition): Seat = {
    new Seat(seatDef)
  }

  /**
    * Provide a fixed string representation.
    * @return the string output
    */
  def toString(obj: Seat): String = {
    val seatStr = if (obj.isOccupied) {
      s", occupied by player ${obj.Occupant.get.GUID}"
    } else {
      ""
    }
    s"seat$seatStr"
  }
}
