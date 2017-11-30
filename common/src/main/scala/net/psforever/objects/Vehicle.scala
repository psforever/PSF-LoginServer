// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.equipment.{Equipment, EquipmentSize}
import net.psforever.objects.inventory.{GridInventory, InventoryTile}
import net.psforever.objects.mount.Mountable
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.vehicles.{AccessPermissionGroup, Seat, Utility, VehicleLockState}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.packet.game.objectcreate.DriveState
import net.psforever.types.PlanetSideEmpire

import scala.annotation.tailrec
import scala.collection.mutable

/**
  * The server-side support object that represents a vehicle.<br>
  * <br>
  * All infantry seating, all mounted weapons, and the trunk space are considered part of the same index hierarchy.
  * Generally, all seating is declared first - the driver and passengers and and gunners.
  * Following that are the mounted weapons and other utilities.
  * Trunk space starts being indexed afterwards.<br>
  * <br>
  * To keep it simple, infantry seating, mounted weapons, and utilities are stored separately.
  * @param vehicleDef the vehicle's definition entry';
  *                   stores and unloads pertinent information about the `Vehicle`'s configuration;
  *                   used in the initialization process (`loadVehicleDefinition`)
  */
class Vehicle(private val vehicleDef : VehicleDefinition) extends PlanetSideServerObject with Mountable {
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.TR
  private var owner : Option[PlanetSideGUID] = None
  private var health : Int = 1
  private var shields : Int = 0
  private var deployed : DriveState.Value = DriveState.Mobile
  private var decal : Int = 0
  private var trunkAccess : Option[PlanetSideGUID] = None
  private var jammered : Boolean = false
  private var cloaked : Boolean = false

  /**
    * Permissions control who gets to access different parts of the vehicle;
    * the groups are Driver (seat), Gunner (seats), Passenger (seats), and the Trunk
    */
  private val groupPermissions : Array[VehicleLockState.Value] = Array(VehicleLockState.Locked, VehicleLockState.Empire, VehicleLockState.Empire, VehicleLockState.Locked)
  private var seats : Map[Int, Seat] = Map.empty
  private var weapons : Map[Int, EquipmentSlot] = Map.empty
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

  def Owner_=(owner : PlanetSideGUID) : Option[PlanetSideGUID] = Owner_=(Some(owner))

  def Owner_=(owner : Player) : Option[PlanetSideGUID] = Owner_=(Some(owner.GUID))

  def Owner_=(owner : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    owner match {
      case Some(_) =>
        if(Definition.CanBeOwned) { //e.g., base turrets
          this.owner = owner
        }
      case None =>
        this.owner = None
    }
    Owner
  }

  def Health : Int = {
    this.health
  }

  def Health_=(health : Int) : Int = {
    this.health = health
    health
  }

  def MaxHealth : Int = {
    Definition.MaxHealth
  }

  def Shields : Int = {
    this.shields
  }

  def Shields_=(strength : Int) : Int = {
    this.shields = strength
    Shields
  }

  def MaxShields : Int = {
    Definition.MaxShields
  }

  def Drive : DriveState.Value = {
    this.deployed
  }

  def Drive_=(deploy : DriveState.Value) : DriveState.Value = {
    if(Definition.Deployment) {
      this.deployed = deploy
    }
    Drive
  }

  def Decal : Int = {
    this.decal
  }

  def Decal_=(decal : Int) : Int = {
    this.decal = decal
    decal
  }

  def Jammered : Boolean = jammered

  def Jammered_=(jamState : Boolean) : Boolean = {
    jammered = jamState
    Jammered
  }

  def Cloaked : Boolean = cloaked

  def Cloaked_=(isCloaked : Boolean) : Boolean = {
    cloaked = isCloaked
    Cloaked
  }

  /**
    * Given the index of an entry mounting point, return the infantry-accessible `Seat` associated with it.
    * @param mountPoint an index representing the seat position / mounting point
    * @return a seat number, or `None`
    */
  def GetSeatFromMountPoint(mountPoint : Int) : Option[Int] = {
    Definition.MountPoints.get(mountPoint)
  }

  def MountPoints : Map[Int, Int] = Definition.MountPoints.toMap

  /**
    * What are the access permissions for a position on this vehicle, seats or trunk?
    * @param group the group index
    * @return what sort of access permission exist for this group
    */
  def PermissionGroup(group : Int) : Option[VehicleLockState.Value] = {
    reindexPermissionsGroup(group) match {
      case Some(index) =>
        Some(groupPermissions(index))
      case None =>
        None
    }
  }

  /**
    * Change the access permissions for a position on this vehicle, seats or trunk.
    * @param group the group index
    * @param level the new permission for this group
    * @return the new access permission for this group;
    *         `None`, if the group does not exist or the level of permission was not changed
    */
  def PermissionGroup(group : Int, level : Long) : Option[VehicleLockState.Value] = {
    reindexPermissionsGroup(group) match {
      case Some(index) =>
        val current = groupPermissions(index)
        val next = try { VehicleLockState(level.toInt) } catch { case _ : Exception => groupPermissions(index) }
        if(current != next) {
          groupPermissions(index) = next
          PermissionGroup(index)
        }
        else {
          None
        }
      case None =>
        None
    }
  }

  /**
    * When the access permission group is communicated via `PlanetsideAttributeMessage`, the index is between 10 and 13.
    * Internally, permission groups are stored as an `Array`, so the respective re-indexing plots 10 -> 0 and 13 -> 3.
    * @param group the group index
    * @return the modified group index
    */
  private def reindexPermissionsGroup(group : Int) : Option[Int] = if(group > 9 && group < 14) {
    Some(group - 10)
  }
  else if(group > -1 && group < 4) {
    Some(group)
  }
  else {
    None
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

  def Seats : Map[Int, Seat] = {
    seats
  }

  def SeatPermissionGroup(seatNumber : Int) : Option[AccessPermissionGroup.Value] = {
    if(seatNumber == 0) {
      Some(AccessPermissionGroup.Driver)
    }
    else {
      Seat(seatNumber) match {
        case Some(seat) =>
          seat.ControlledWeapon match {
            case Some(_) =>
              Some(AccessPermissionGroup.Gunner)
            case None =>
              Some(AccessPermissionGroup.Passenger)
          }
        case None =>
          None
      }
    }
  }

  def Weapons : Map[Int, EquipmentSlot] = weapons

  /**
    * Get the weapon at the index.
    * @param wepNumber an index representing the seat position / mounting point
    * @return a weapon, or `None`
    */
  def ControlledWeapon(wepNumber : Int) : Option[Equipment] = {
    weapons.get(wepNumber) match {
      case Some(mount) =>
        mount.Equipment
      case None =>
        None
    }
  }

  /**
    * Given a player who may be an occupant, retrieve an number of the seat where this player is sat.
    * @param player the player
    * @return a seat number, or `None` if the `player` is not actually seated in this vehicle
    */
  def PassengerInSeat(player : Player) : Option[Int] = recursivePassengerInSeat(seats.iterator, player)

  @tailrec private def recursivePassengerInSeat(iter : Iterator[(Int, Seat)], player : Player) : Option[Int] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val (seatNumber, seat) = iter.next
      if(seat.Occupant.contains(player)) {
        Some(seatNumber)
      }
      else {
        recursivePassengerInSeat(iter, player)
      }
    }
  }

  /**
    * Given a valid seat number, retrieve an index where the weapon controlled from this seat is mounted.
    * @param seatNumber the seat number
    * @return a mounted weapon by index, or `None` if either the seat doesn't exist or there is no controlled weapon
    */
  def WeaponControlledFromSeat(seatNumber : Int) : Option[Equipment] = {
    Seat(seatNumber) match {
      case Some(seat) =>
        wepFromSeat(seat)
      case None =>
        None
    }
  }

  private def wepFromSeat(seat : Seat) : Option[Equipment] = {
    seat.ControlledWeapon match {
      case Some(index) =>
        wepFromSeat(index)
      case None =>
        None
    }
  }

  private def wepFromSeat(wepIndex : Int) : Option[Equipment] = {
    weapons.get(wepIndex) match {
      case Some(wep) =>
        wep.Equipment
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
      groupPermissions(3) match {
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
  def TrunkLockState :  VehicleLockState.Value = groupPermissions(3)

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
    * A basic `Trait` connecting all of the actionable `Vehicle` response messages.
    */
  sealed trait Exchange

  /**
    * Message that carries the result of the processed request message back to the original user (`player`).
    * @param player the player who sent this request message
    * @param response the result of the processed request
    */
  final case class VehicleMessages(player : Player, response : Exchange)

  /**
    * The `Vehicle` will become unresponsive to player activity.
    * Usually, it does this to await deconstruction and clean-up
    * @see `VehicleControl`
    */
  final case class PrepareForDeletion()

  /**
    * Overloaded constructor.
    * @param vehicleDef the vehicle's definition entry
    * @return a `Vehicle` object
    */
  def apply(vehicleDef : VehicleDefinition) : Vehicle = {
    new Vehicle(vehicleDef)
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
    vehicle.weapons = vdef.Weapons.map({case (num, definition) =>
      val slot = EquipmentSlot(EquipmentSize.VehicleWeapon)
      slot.Equipment = Tool(definition)
      num -> slot
    }).toMap
    //create seats
    vehicle.seats = vdef.Seats.map({ case(num, definition) => num -> Seat(definition)}).toMap
    for(i <- vdef.Utilities) {
      //TODO utilies must be loaded and wired on a case-by-case basis?
      vehicle.Utilities += Utility.Select(i, vehicle)
    }
    //trunk
    vdef.TrunkSize match {
      case InventoryTile.None => ;
      case dim =>
        vehicle.trunk.Resize(dim.Width, dim.Height)
        vehicle.trunk.Offset = vdef.TrunkOffset
    }
    vehicle
  }

  /**
    * Provide a fixed string representation.
    * @return the string output
    */
  def toString(obj : Vehicle) : String = {
    val occupancy = obj.Seats.values.count(seat => seat.isOccupied)
    s"${obj.Definition.Name}, owned by ${obj.Owner}: (${obj.Health}/${obj.MaxHealth})(${obj.Shields}/${obj.MaxShields}) ($occupancy)"
  }
}
