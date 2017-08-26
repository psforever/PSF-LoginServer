// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.equipment.{Equipment, EquipmentSize}
import net.psforever.objects.inventory.GridInventory
import net.psforever.objects.vehicles.{Seat, Utility, VehicleLockState}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.DriveState
import net.psforever.types.PlanetSideEmpire

import scala.collection.mutable

/**
  * The server-side support object that represents a vehicle.<br>
  * <br>
  * All infantry seating, all mounted weapons, and the trunk space are considered part of the same index hierarchy.
  * Generally, all seating is declared first - the driver and passengers and and gunners.
  * Following that are the mounted weapons and other utilities.
  * Trunk space starts being indexed afterwards.
  * The first seat is always the op;erator (driver/pilot).
  * "Passengers" are seats that are not the operator and are not in control of a mounted weapon.
  * "Gunners" are seats that are not the operator and ARE in control of a mounted weapon.
  * (The operator can be in control of a weapon - that is the whole point of a turret.)<br>
  * <br>
  * Having said all that, to keep it simple, infantry seating, mounted weapons, and utilities are stored in separate `Map`s.
  * @param vehicleDef the vehicle's definition entry'
  *                   stores and unloads pertinent information about the `Vehicle`'s configuration;
  *                   used in the initialization process (`loadVehicleDefinition`)
  */
class Vehicle(private val vehicleDef : VehicleDefinition) extends PlanetSideGameObject {
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.TR
  private var owner : Option[PlanetSideGUID] = None
  private var health : Int = 1
  private var shields : Int = 0
  private var deployed : DriveState.Value = DriveState.Mobile
  private var decal : Int = 0
  private var trunkLockState : VehicleLockState.Value = VehicleLockState.Locked
  private var trunkAccess : Option[PlanetSideGUID] = None

  private val seats : mutable.HashMap[Int, Seat] = mutable.HashMap()
  private val weapons : mutable.HashMap[Int, EquipmentSlot] = mutable.HashMap()
  private val utilities : mutable.ArrayBuffer[Utility] = mutable.ArrayBuffer()
  private val trunk : GridInventory = GridInventory()

  //init
  LoadDefinition()

  /**
    * Override this method to perform any special setup that is not standardized to `*Definition`.
    * @see `Vehicle.LoadDefinition`
    */
  protected def LoadDefinition() : Unit = {
    Vehicle.LoadDefinition(this)
  }

  def Faction : PlanetSideEmpire.Value = {
    this.faction
  }

  def Faction_=(faction : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
    this.faction = faction
    faction
  }

  def Owner : Option[PlanetSideGUID] = {
    this.owner
  }

  def Owner_=(owner : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    this.owner = owner
    owner
  }

  def Health : Int = {
    this.health
  }

  def Health_=(health : Int) : Int = {
    this.health = health
    health
  }

  def MaxHealth : Int = {
    this.vehicleDef.MaxHealth
  }

  def Shields : Int = {
    this.shields
  }

  def Shields_=(strength : Int) : Int = {
    this.shields = strength
    strength
  }

  def MaxShields : Int = {
    vehicleDef.MaxShields
  }

  def Configuration : DriveState.Value = {
    this.deployed
  }

  def Configuration_=(deploy : DriveState.Value) : DriveState.Value = {
    if(vehicleDef.Deployment) {
      this.deployed = deploy
    }
    Configuration
  }

  def Decal : Int = {
    this.decal
  }

  def Decal_=(decal : Int) : Int = {
    this.decal = decal
    decal
  }

  /**
    * Given the index of an entry mounting point, return the infantry-accessible `Seat` associated with it.
    * @param mountPoint an index representing the seat position / mounting point
    * @return a seat number, or `None`
    */
  def GetSeatFromMountPoint(mountPoint : Int) : Option[Int] = {
    vehicleDef.MountPoints.get(mountPoint)
  }

  /**
    * Get the seat at the index.
    * The specified "seat" can only accommodate a player as opposed to weapon mounts which share the same indexing system.
    * @param seatNumber an index representing the seat position / mounting point
    * @return a `Seat`, or `None`
    */
  def Seat(seatNumber : Int) : Option[Seat] = {
    if(seatNumber >= 0 && seatNumber < this.seats.size) {
      this.seats.get(seatNumber)
    }
    else {
      None
    }
  }

  def Seats : List[Seat] = {
    seats.values.toList
  }

  def Weapons : mutable.HashMap[Int, EquipmentSlot] = weapons

  /**
    * Get the weapon at the index.
    * @param wepNumber an index representing the seat position / mounting point
    * @return a weapon, or `None`
    */
  def ControlledWeapon(wepNumber : Int) : Option[Equipment] = {
    val slot = this.weapons.get(wepNumber)
    if(slot.isDefined) {
      slot.get.Equipment
    }
    else {
      None
    }
  }

  /**
    * Given a player who may be a passenger, retrieve an index where this player is seated.
    * @param player the player
    * @return a seat by index, or `None` if the `player` is not actually seated in this `Vehicle`
    */
  def PassengerInSeat(player : Player) : Option[Int] = {
    var outSeat : Option[Int] = None
    val GUID = player.GUID
    for((seatNumber, seat) <- this.seats) {
      val occupant : Option[PlanetSideGUID] = seat.Occupant
      if(occupant.isDefined && occupant.get == GUID) {
        outSeat = Some(seatNumber)
      }
    }
    outSeat
  }

  /**
    * Given a valid seat number, retrieve an index where a weapon controlled from this seat is attached.
    * @param seatNumber the seat number
    * @return a mounted weapon by index, or `None` if either the seat doesn't exist or there is no controlled weapon
    */
  def WeaponControlledFromSeat(seatNumber : Int) : Option[Tool] = {
    Seat(seatNumber) match {
      case Some(seat) =>
        wepFromSeat(seat)
      case None =>
        None
    }
  }

  private def wepFromSeat(seat : Seat) : Option[Tool] = {
    seat.ControlledWeapon match {
      case Some(index) =>
        wepFromSeat(index)
      case None =>
        None
    }
  }

  private def wepFromSeat(wepIndex : Int) : Option[Tool] = {
    weapons.get(wepIndex) match {
      case Some(wep) =>
        wep.Equipment.asInstanceOf[Option[Tool]]
      case None =>
        None
    }
  }

  def Utilities : mutable.ArrayBuffer[Utility] = utilities

  /**
    * Get a referenece ot a certain `Utility` attached to this `Vehicle`.
    * @param utilNumber the attachment number of the `Utility`
    * @return the `Utility` or `None` (if invalid)
    */
  def Utility(utilNumber : Int) : Option[Utility] = {
    if(utilNumber >= 0 && utilNumber < this.utilities.size) {
      Some(this.utilities(utilNumber))
    }
    else {
      None
    }
  }

  /**
    * A reference to the `Vehicle` `Trunk` space.
    * @return this `Vehicle` `Trunk`
    */
  def Trunk : GridInventory = {
    this.trunk
  }

  def AccessingTrunk : Option[PlanetSideGUID] = trunkAccess

  def AccessingTrunk_=(guid : PlanetSideGUID) : Option[PlanetSideGUID] = {
    AccessingTrunk = Some(guid)
  }

  /**
    * Change which player has access to the trunk of this vehicle.
    * A player may only gain access to the trunk if no one else has access to the trunk at the moment.
    * @param guid the player who wishes to access the trunk
    * @return the player who is currently allowed to access the trunk
    */
  def AccessingTrunk_=(guid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    guid match {
      case None =>
        trunkAccess = None
      case Some(player) =>
        if(trunkAccess.isEmpty) {
          trunkAccess = Some(player)
        }
    }
    AccessingTrunk
  }

  /**
    * Can this `player` access the contents of this `Vehicle`'s `Trunk` given its current access permissions?
    * @param player a player attempting to access this `Trunk`
    * @return `true`, if the `player` is permitted access; `false`, otherwise
    */
  def CanAccessTrunk(player : Player) : Boolean = {
    if(trunkAccess.isEmpty || trunkAccess.contains(player.GUID)) {
      trunkLockState match {
        case VehicleLockState.Locked => //only the owner
          owner.isEmpty || (owner.isDefined && player.GUID == owner.get)
        case VehicleLockState.Group => //anyone in the owner's squad or platoon
          faction == player.Faction //TODO this is not correct
        case VehicleLockState.Empire => //anyone of the owner's faction
          faction == player.Faction
      }
    }
    else {
      false
    }
  }

  /**
    * Check access to the `Trunk`.
    * @return the current access value for the `Vehicle` `Trunk`
    */
  def TrunkLockState :  VehicleLockState.Value = {
    this.trunkLockState
  }

  /**
    * Change the access value for the trunk.
    * @param lockState the new access value for the `Vehicle` `Trunk`
    * @return the current access value for the `Vehicle` `Trunk` after the change
    */
  def TrunkLockState_=(lockState :  VehicleLockState.Value) :  VehicleLockState.Value = {
    this.trunkLockState = lockState
    lockState
  }

  /**
    * This is the definition entry that is used to store and unload pertinent information about the `Vehicle`.
    * @return the vehicle's definition entry
    */
  def Definition : VehicleDefinition = vehicleDef

  /**
    * Override the string representation to provide additional information.
    * @return the string output
    */
  override def toString : String = {
    Vehicle.toString(this)
  }
}

object Vehicle {
  /**
    * Overloaded constructor.
    * @param vehicleDef the vehicle's definition entry
    * @return a `Vwehicle` object
    */
  def apply(vehicleDef : VehicleDefinition) : Vehicle = {
    new Vehicle(vehicleDef)
  }
  /**
    * Overloaded constructor.
    * @param vehicleDef the vehicle's definition entry
    * @return a `Vwehicle` object
    */
  def apply(guid : PlanetSideGUID, vehicleDef : VehicleDefinition) : Vehicle = {
    val obj = new Vehicle(vehicleDef)
    obj.GUID = guid
    obj
  }

  /**
    * Use the `*Definition` that was provided to this object to initialize its fields and settings.
    * @param vehicle the `Vehicle` being initialized
    * @see `{object}.LoadDefinition`
    */
  def LoadDefinition(vehicle : Vehicle) : Vehicle = {
    val vdef : VehicleDefinition = vehicle.Definition
    //general stuff
    vehicle.Health = vdef.MaxHealth
    //create weapons
    for((num, definition) <- vdef.Weapons) {
      val slot = EquipmentSlot(EquipmentSize.VehicleWeapon)
      slot.Equipment = Tool(definition)
      vehicle.weapons += num -> slot
      vehicle
    }
    //create seats
    for((num, seatDef) <- vdef.Seats) {
      vehicle.seats += num -> Seat(seatDef, vehicle)
    }
    for(i <- vdef.Utilities) {
      //TODO utilies must be loaded and wired on a case-by-case basis?
      vehicle.Utilities += Utility.Select(i, vehicle)
    }
    //trunk
    vehicle.trunk.Resize(vdef.TrunkSize.width, vdef.TrunkSize.height)
    vehicle.trunk.Offset = vdef.TrunkOffset
    vehicle
  }

  /**
    * Provide a fixed string representation.
    * @return the string output
    */
  def toString(obj : Vehicle) : String = {
    val occupancy = obj.Seats.count(seat => seat.isOccupied)
    s"${obj.Definition.Name}, owned by ${obj.Owner}: (${obj.Health}/${obj.MaxHealth})(${obj.Shields}/${obj.MaxShields}) ($occupancy)"
  }
}
