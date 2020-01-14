// Copyright (c) 2017 PSForever
package net.psforever.objects

import akka.actor.ActorRef
import net.psforever.objects.definition.VehicleDefinition
import net.psforever.objects.equipment.{Equipment, EquipmentSize, EquipmentSlot, JammableUnit}
import net.psforever.objects.inventory.{Container, GridInventory, InventoryTile}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.structures.AmenityOwner
import net.psforever.objects.vehicles._
import net.psforever.objects.vital.{DamageResistanceModel, StandardResistanceProfile, Vitality}
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID}

import scala.annotation.tailrec

/**
  * The server-side support object that represents a vehicle.<br>
  * <br>
  * All infantry seating, all mounted weapons, and the trunk space are considered part of the same index hierarchy.
  * Generally, all seating is declared first - the driver and passengers and and gunners.
  * Following that are the mounted weapons and other utilities.
  * Trunk space starts being indexed afterwards.
  * To keep it simple, infantry seating, mounted weapons, and utilities are stored separately herein.
  * The `Map` of `Utility` objects is given using the same inventory index positions.
  * Positive indices and zero are considered "represented" and must be assigned a globally unique identifier
  * and must be present in the containing vehicle's `ObjectCreateMessage` packet.
  * The index is the seat position, reflecting the position in the zero-index inventory.
  * Negative indices are expected to be excluded from this conversion.
  * The value of the negative index does not have a specific meaning.<br>
  * <br>
  * The importance of a vehicle's owner can not be overlooked.
  * The owner is someone who can control who can sit in the vehicle's seats
  * either through broad categorization or discriminating selection ("kicking")
  * and who has access to and can allow access to the vehicle's trunk capacity.
  * The driver is the only player that can access a vehicle's saved loadouts through a repair/rearm silo
  * and can procure equipment from the said silo.
  * The owner of a vehicle and the driver of a vehicle as mostly interchangeable terms for this reason
  * and it can be summarized that the player who has access to the driver seat meets the qualifications for the "owner"
  * so long as that player is the last person to have sat in that seat.
  * All previous ownership information is replaced just as soon as someone else sits in the driver's seat.
  * Ownership is also transferred as players die and respawn (from and to the same client)
  * and when they leave a continent without taking the vehicle they currently own with them.
  * (They also lose ownership when they leave the game, of course.)<br>
  * <br>
  * All seats have vehicle-level properties on top of their own internal properties.
  * A seat has a glyph projected onto the ground when the vehicle is not moving
  * that is used to mark where the seat can be accessed, as well as broadcasting the current access condition of the seat.
  * As indicated previously, seats are composed into categories and the categories used to control access.
  * The "driver" group has already been mentioned and is usually composed of a single seat, the "first" one.
  * The driver seat is typically locked to the person who can sit in it - the owner - unless manually unlocked.
  * Any seat besides the "driver" that has a weapon controlled from the seat is called a "gunner" seats.
  * Any other seat besides the "driver" seat and "gunner" seats is called a "passenger" seat.
  * All of these seats are typically unlocked normally.
  * The "trunk" also counts as an access group even though it is not directly attached to a seat and starts as "locked."
  * The categories all have their own glyphs,
  * sharing a red cross glyph as a "can not access" state,
  * and may also use their lack of visibility to express state.
  * In terms of individual access, each seat can have its current occupant ejected, save for the driver's seat.
  * @see `Vehicle.EquipmentUtilities`
  * @param vehicleDef the vehicle's definition entry;
  *                   stores and unloads pertinent information about the `Vehicle`'s configuration;
  *                   used in the initialization process (`loadVehicleDefinition`)
  */
class Vehicle(private val vehicleDef : VehicleDefinition) extends AmenityOwner
  with FactionAffinity
  with Mountable
  with MountedWeapons
  with Deployment
  with Vitality
  with OwnableByPlayer
  with StandardResistanceProfile
  with JammableUnit
  with Container {
  private var faction : PlanetSideEmpire.Value = PlanetSideEmpire.TR
  private var health : Int = 1
  private var shields : Int = 0
  private var isDead : Boolean = false
  private var decal : Int = 0
  private var trunkAccess : Option[PlanetSideGUID] = None
  private var jammered : Boolean = false
  private var cloaked : Boolean = false
  private var flying : Boolean = false
  private var ntuCapacitor : Int = 0
  private var capacitor : Int = 0
  /**
    * Permissions control who gets to access different parts of the vehicle;
    * the groups are Driver (seat), Gunner (seats), Passenger (seats), and the Trunk
    */
  private val groupPermissions : Array[VehicleLockState.Value] = Array(VehicleLockState.Locked, VehicleLockState.Empire, VehicleLockState.Empire, VehicleLockState.Locked)
  private var seats : Map[Int, Seat] = Map.empty
  private var cargoHolds : Map[Int, Cargo] = Map.empty
  private var weapons : Map[Int, EquipmentSlot] = Map.empty
  private var utilities : Map[Int, Utility] = Map()
  private val trunk : GridInventory = GridInventory()

  /**
    * Records the GUID of the cargo vehicle (galaxy/lodestar) this vehicle is stored in for DismountVehicleCargoMsg use
    * DismountVehicleCargoMsg only passes the player_guid and this vehicle's guid
    */
  private var mountedIn : Option[PlanetSideGUID] = None

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

  override def Faction_=(faction : PlanetSideEmpire.Value) : PlanetSideEmpire.Value = {
    this.faction = faction
    faction
  }

  /** How long it takes to jack the vehicle in seconds, based on the hacker's certification level */
  def JackingDuration: Array[Int] = Definition.JackingDuration

  def MountedIn : Option[PlanetSideGUID] = {
    this.mountedIn
  }

  def MountedIn_=(cargo_vehicle_guid : PlanetSideGUID) : Option[PlanetSideGUID] = MountedIn_=(Some(cargo_vehicle_guid))

  def MountedIn_=(cargo_vehicle_guid : Option[PlanetSideGUID]) : Option[PlanetSideGUID] = {
    cargo_vehicle_guid match {
      case Some(_) =>
        this.mountedIn = cargo_vehicle_guid
      case None =>
        this.mountedIn = None
    }
    MountedIn
  }

  def IsDead : Boolean = {
    isDead
  }

  def Health : Int = {
    health
  }

  def Health_=(assignHealth : Int) : Int = {
    if(!isDead) {
      health = math.min(math.max(0, assignHealth), MaxHealth)
    }

    if(health == 0) {
      isDead = true
    }

    health
  }

  def MaxHealth : Int = {
    Definition.MaxHealth
  }

  def Shields : Int = {
    shields
  }

  def Shields_=(strength : Int) : Int = {
    shields = math.min(math.max(0, strength), MaxShields)
    Shields
  }

  def MaxShields : Int = {
    Definition.MaxShields
  }

  def Decal : Int = {
    decal
  }

  def Decal_=(logo : Int) : Int = {
    decal = logo
    Decal
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

  def Flying : Boolean = flying

  def Flying_=(isFlying : Boolean) : Boolean = {
    flying = isFlying
    Flying
  }

  def NtuCapacitor : Int = ntuCapacitor

  def NtuCapacitor_=(value: Int) : Int = {
    if(value > Definition.MaxNtuCapacitor) {
      ntuCapacitor = Definition.MaxNtuCapacitor
    } else if (value < 0) {
      ntuCapacitor = 0
    } else {
      ntuCapacitor = value
    }
    NtuCapacitor
  }

  def Capacitor : Int = capacitor

  def Capacitor_=(value: Int) : Int = {
    if(value > Definition.MaxCapacitor) {
      capacitor = Definition.MaxCapacitor
    } else if (value < 0) {
      capacitor = 0
    } else {
      capacitor = value
    }
    Capacitor
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

  def CargoHold(cargoNumber : Int) : Option[Cargo] = {
    if(cargoNumber >= 0) {
      this.cargoHolds.get(cargoNumber)
    }
    else {
      None
    }
  }

  def CargoHolds : Map[Int, Cargo] = {
    cargoHolds
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
          CargoHold(seatNumber) match {
            case Some(_) =>
              Some(AccessPermissionGroup.Passenger)
            case None =>
              None
          }
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

  def Utilities : Map[Int, Utility] = utilities

  /**
    * Get a reference to a certain `Utility` attached to this `Vehicle`.
    * @param utilNumber the attachment number of the `Utility`
    * @return the `Utility` or `None` (if invalid)
    */
  def Utility(utilNumber : Int) : Option[PlanetSideServerObject] = {
    if(utilNumber >= 0 && utilNumber < this.utilities.size) {
      this.utilities.get(utilNumber) match {
        case Some(util) =>
          Some(util())
        case None =>
          None
      }
    }
    else {
      None
    }
  }

  def Utility(utilType : UtilityType.Value) : Option[PlanetSideServerObject] = {
    utilities.values.find(_.UtilType == utilType) match {
      case Some(util) =>
        Some(util())
      case None =>
        None
    }
  }

  override def DeployTime = Definition.DeployTime

  override def UndeployTime = Definition.UndeployTime

  def Inventory : GridInventory = trunk

  def VisibleSlots : Set[Int] = weapons.keySet

  override def Slot(slotNum : Int) : EquipmentSlot = {
    weapons.get(slotNum)
//      .orElse(utilities.get(slotNum) match {
//        case Some(_) =>
//          //TODO what do now?
//          None
//        case None => ;
//          None
//      })
      .orElse(Some(Inventory.Slot(slotNum))).get
  }

  override def Find(guid : PlanetSideGUID) : Option[Int] = {
    weapons.find({ case (_, obj) =>
      obj.Equipment match {
        case Some(item) =>
          if(item.HasGUID && item.GUID == guid) {
            true
          }
          else {
            false
          }
        case None =>
          false
      }
    }) match {
      case Some((index, _)) =>
        Some(index)
      case None =>
        Inventory.Find(guid)
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
          Owner.isEmpty || (Owner.isDefined && player.GUID == Owner.get)
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

  def DamageModel = Definition.asInstanceOf[DamageResistanceModel]

  /**
    * This is the definition entry that is used to store and unload pertinent information about the `Vehicle`.
    * @return the vehicle's definition entry
    */
  def Definition : VehicleDefinition = vehicleDef

  def canEqual(other: Any): Boolean = other.isInstanceOf[Vehicle]

  override def equals(other : Any) : Boolean = other match {
    case that: Vehicle =>
      (that canEqual this) &&
        hashCode() == that.hashCode()
    case _ =>
      false
  }

  override def hashCode() : Int = {
    Actor match {
      case ActorRef.noSender =>
        super.hashCode()
      case actor =>
        actor.hashCode()
    }
  }

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
    * Usually, it does this to await deconstruction and clean-up.
    * @see `VehicleControl`
    */
  final case class PrepareForDeletion()

  /**
    * The `Vehicle` will resume previous unresponsiveness to player activity.
    * @see `VehicleControl`
    */
  final case class Reactivate()

  /**
    * A request has been made to charge this vehicle's shields.
    * @see `FacilityBenefitShieldChargeRequestMessage`
    * @param amount the number of points to charge
    */
  final case class ChargeShields(amount : Int)

  /**
    * Following a successful shield charge tick, display the results of the update.
    * @see `FacilityBenefitShieldChargeRequestMessage`
    * @param vehicle the updated vehicle
    */
  final case class UpdateShieldsCharge(vehicle : Vehicle)

  /**
    * Overloaded constructor.
    * @param vehicleDef the vehicle's definition entry
    * @return a `Vehicle` object
    */
  def apply(vehicleDef : VehicleDefinition) : Vehicle = {
    new Vehicle(vehicleDef)
  }

  /**
    * Given a `Map` of `Utility` objects, only return the objects with a positive or zero-index position.
    * @return a map of applicable utilities
    */
  def EquipmentUtilities(utilities : Map[Int, Utility]) : Map[Int, Utility] = {
    utilities.filter({ case(index : Int, _ : Utility) => index > -1 })
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
    // create cargo holds
    vehicle.cargoHolds = vdef.Cargo.map({ case(num, definition) => num -> Cargo(definition)}).toMap

    //create utilities
    vehicle.utilities = vdef.Utilities.map({
      case(num, util) =>
        val obj = Utility(util, vehicle)
        val utilObj = obj()
        vehicle.Amenities = utilObj
        utilObj.LocationOffset = vdef.UtilityOffset.get(num)
        num -> obj
    }).toMap
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
