// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.objects.ce.InteractWithMines
import net.psforever.objects.definition.{ToolDefinition, VehicleDefinition}
import net.psforever.objects.equipment.{Equipment, EquipmentSize, EquipmentSlot, JammableUnit}
import net.psforever.objects.inventory.{Container, GridInventory, InventoryItem, InventoryTile}
import net.psforever.objects.serverobject.mount.{MountableEntity, Seat, SeatDefinition}
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.aura.AuraContainer
import net.psforever.objects.serverobject.deploy.Deployment
import net.psforever.objects.serverobject.environment.InteractWithEnvironment
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.AmenityOwner
import net.psforever.objects.vehicles._
import net.psforever.objects.vital.resistance.StandardResistanceProfile
import net.psforever.objects.vital.Vitality
import net.psforever.objects.vital.resolution.DamageResistanceModel
import net.psforever.objects.zones.InteractsWithZone
import net.psforever.objects.zones.blockmap.BlockMapEntity
import net.psforever.packet.PlanetSideGamePacket
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

import scala.annotation.tailrec
import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Try}

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
  * The index is the mount position, reflecting the position in the zero-index inventory.
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
  * and it can be summarized that the player who has access to the driver mount meets the qualifications for the "owner"
  * so long as that player is the last person to have sat in that mount.
  * All previous ownership information is replaced just as soon as someone else sits in the driver's mount.
  * Ownership is also transferred as players die and respawn (from and to the same client)
  * and when they leave a continent without taking the vehicle they currently own with them.
  * (They also lose ownership when they leave the game, of course.)<br>
  * <br>
  * All seats have vehicle-level properties on top of their own internal properties.
  * A mount has a glyph projected onto the ground when the vehicle is not moving
  * that is used to mark where the mount can be accessed, as well as broadcasting the current access condition of the mount.
  * As indicated previously, seats are composed into categories and the categories used to control access.
  * The "driver" group has already been mentioned and is usually composed of a single mount, the "first" one.
  * The driver mount is typically locked to the person who can sit in it - the owner - unless manually unlocked.
  * Any mount besides the "driver" that has a weapon controlled from the mount is called a "gunner" seats.
  * Any other mount besides the "driver" mount and "gunner" seats is called a "passenger" mount.
  * All of these seats are typically unlocked normally.
  * The "trunk" also counts as an access group even though it is not directly attached to a mount and starts as "locked."
  * The categories all have their own glyphs,
  * sharing a red cross glyph as a "can not access" state,
  * and may also use their lack of visibility to express state.
  * In terms of individual access, each mount can have its current occupant ejected, save for the driver's mount.
  * @see `Vehicle.EquipmentUtilities`
  * @param vehicleDef the vehicle's definition entry;
  *                   stores and unloads pertinent information about the `Vehicle`'s configuration;
  *                   used in the initialization process (`loadVehicleDefinition`)
  */
class Vehicle(private val vehicleDef: VehicleDefinition)
    extends AmenityOwner
    with BlockMapEntity
    with MountableWeapons
    with InteractsWithZone
    with Hackable
    with FactionAffinity
    with Deployment
    with Vitality
    with OwnableByPlayer
    with StandardResistanceProfile
    with JammableUnit
    with CommonNtuContainer
    with Container
    with AuraContainer
    with MountableEntity {
  interaction(new InteractWithEnvironment())
  interaction(new InteractWithMines(range = 20))
  interaction(new InteractWithRadiationCloudsSeatedInVehicle(obj = this, range = 20))

  private var faction: PlanetSideEmpire.Value     = PlanetSideEmpire.NEUTRAL
  private var shields: Int                        = 0
  private var decal: Int                          = 0
  private var trunkAccess: Option[PlanetSideGUID] = None

  private var cloaked: Boolean                    = false
  private var flying: Option[Int]                 = None
  private var capacitor: Int                      = 0

  /**
    * Permissions control who gets to access different parts of the vehicle;
    * the groups are Driver (mount), Gunner (seats), Passenger (seats), and the Trunk
    */
  private val groupPermissions: Array[VehicleLockState.Value] =
    Array(VehicleLockState.Locked, VehicleLockState.Empire, VehicleLockState.Empire, VehicleLockState.Locked)
  private var cargoHolds: Map[Int, Cargo]        = Map.empty
  private var utilities: Map[Int, Utility]       = Map.empty
  private var subsystems: List[VehicleSubsystem] = Nil
  private val trunk: GridInventory               = GridInventory()

  /*
    * Records the GUID of the cargo vehicle (galaxy/lodestar) this vehicle is stored in for DismountVehicleCargoMsg use
    * DismountVehicleCargoMsg only passes the player_guid and this vehicle's guid
    */
  //private var mountedIn: Option[PlanetSideGUID] = None

  private var vehicleGatingManifest: Option[VehicleManifest]         = None
  private var previousVehicleGatingManifest: Option[VehicleManifest] = None

  //init
  LoadDefinition()

  /**
    * Override this method to perform any special setup that is not standardized to `*Definition`.
    * @see `Vehicle.LoadDefinition`
    */
  protected def LoadDefinition(): Unit = {
    Vehicle.LoadDefinition(vehicle = this)
  }

  def Faction: PlanetSideEmpire.Value = {
    this.faction
  }

  override def Faction_=(faction: PlanetSideEmpire.Value): PlanetSideEmpire.Value = {
    this.faction = faction
    faction
  }

  /** How long it takes to jack the vehicle in seconds, based on the hacker's certification level */
  def JackingDuration: Array[Int] = Definition.JackingDuration

  override def Health_=(assignHealth: Int): Int = {
    //TODO should vehicle class enforce this?
    if (!Destroyed) {
      super.Health_=(assignHealth)
    }
    Health
  }

  def Shields: Int = {
    shields
  }

  def Shields_=(strength: Int): Int = {
    shields = math.min(math.max(0, strength), MaxShields)
    Shields
  }

  def MaxShields: Int = {
    Definition.MaxShields
  }

  def Decal: Int = {
    decal
  }

  def Decal_=(logo: Int): Int = {
    decal = logo
    Decal
  }

  def Cloaked: Boolean = cloaked

  def Cloaked_=(isCloaked: Boolean): Boolean = {
    cloaked = isCloaked
    Cloaked
  }

  def isFlying: Boolean = flying.nonEmpty

  def Flying: Option[Int] = flying

  def Flying_=(isFlying: Int): Option[Int] = Flying_=(Some(isFlying))

  def Flying_=(isFlying: Option[Int]): Option[Int] = {
    flying = isFlying
    Flying
  }

  def Capacitor: Int = capacitor

  def Capacitor_=(value: Int): Int = {
    if (value > Definition.MaxCapacitor) {
      capacitor = Definition.MaxCapacitor
    } else if (value < 0) {
      capacitor = 0
    } else {
      capacitor = value
    }
    Capacitor
  }

  /**
    * What are the access permissions for a position on this vehicle, seats or trunk?
    * @param group the group index
    * @return what sort of access permission exist for this group
    */
  def PermissionGroup(group: Int): Option[VehicleLockState.Value] = {
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
  def PermissionGroup(group: Int, level: Long): Option[VehicleLockState.Value] = {
    reindexPermissionsGroup(group) match {
      case Some(index) =>
        val current = groupPermissions(index)
        val next =
          try { VehicleLockState(level.toInt) }
          catch { case _: Exception => groupPermissions(index) }
        if (current != next) {
          groupPermissions(index) = next
          PermissionGroup(index)
        } else {
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
  private def reindexPermissionsGroup(group: Int): Option[Int] =
    if (group > 9 && group < 14) {
      Some(group - 10)
    } else if (group > -1 && group < 4) {
      Some(group)
    } else {
      None
    }

  def CargoHold(cargoNumber: Int): Option[Cargo] = {
    if (cargoNumber >= 0) {
      this.cargoHolds.get(cargoNumber)
    } else {
      None
    }
  }

  def CargoHolds: Map[Int, Cargo] = {
    cargoHolds
  }

  def SeatPermissionGroup(seatNumber: Int): Option[AccessPermissionGroup.Value] = {
    if (seatNumber == 0) { //valid in almost all cases
      Some(AccessPermissionGroup.Driver)
    } else {
      Seat(seatNumber) match {
        case Some(_) =>
          Definition.controlledWeapons().get(seatNumber) match {
            case Some(_) =>
              Some(AccessPermissionGroup.Gunner)
            case None =>
              Some(AccessPermissionGroup.Passenger)
          }
        case None =>
          CargoHold(seatNumber) match {
            case Some(_) =>
              Some(AccessPermissionGroup.Passenger) //TODO confirm this
            case None =>
              if (seatNumber >= trunk.Offset && seatNumber < trunk.Offset + trunk.TotalCapacity) {
                Some(AccessPermissionGroup.Trunk)
              } else {
                None
              }
          }
      }
    }
  }

  def Utilities: Map[Int, Utility] = utilities

  /**
    * Get a reference to a certain `Utility` attached to this `Vehicle`.
    * @param utilNumber the attachment number of the `Utility`
    * @return the `Utility` or `None` (if invalid)
    */
  def Utility(utilNumber: Int): Option[PlanetSideServerObject] = {
    if (utilNumber >= 0 && utilNumber < this.utilities.size) {
      this.utilities.get(utilNumber) match {
        case Some(util) =>
          Some(util())
        case None =>
          None
      }
    } else {
      None
    }
  }

  def Utility(utilType: UtilityType.Value): Option[PlanetSideServerObject] = {
    utilities.values.find(_.UtilType == utilType) match {
      case Some(util) =>
        Some(util())
      case None =>
        None
    }
  }

  def Subsystems(): List[VehicleSubsystem] = subsystems

  def Subsystems(sys: VehicleSubsystemEntry): Option[VehicleSubsystem] = subsystems.find { _.sys == sys }

  def Subsystems(sys: String): Option[VehicleSubsystem] = subsystems.find { _.sys.name.contains(sys) }

  def SubsystemMessages(): List[PlanetSideGamePacket] = {
    subsystems
      .filter { sub => sub.Enabled != sub.sys.defaultState }
      .flatMap { _.getMessage(vehicle = this) }
  }

  def SubsystemStatus(sys: String): Option[Boolean] = {
    val elems = sys.split("\\.")
    if (elems.length < 2) {
      None
    } else {
      Subsystems(elems.head) match {
        case Some(sub) => sub.stateOfStatus(elems(1))
        case None      => Some(false)
      }
    }
  }

  def SubsystemStatusMultiplier(sys: String): Float = {
    val elems = sys.split("\\.")
    if (elems.length < 2) {
      1f
    } else {
      Subsystems(elems.head) match {
        case Some(sub) => sub.multiplierOfStatus(elems(1))
        case None      => 1f
      }
    }
  }

  override def DeployTime = Definition.DeployTime

  override def UndeployTime = Definition.UndeployTime

  def Inventory: GridInventory = trunk

  def VisibleSlots: Set[Int] = weapons.keys.toSet

  override def Slot(slotNum: Int): EquipmentSlot = {
    weapons
      .get(slotNum)
      .orElse(Some(Inventory.Slot(slotNum)))
      .get
  }

  override def SlotMapResolution(slot: Int): Int = {
    if (GlobalDefinitions.isBattleFrameVehicle(vehicleDef)) {
      //for the benefit of BFR equipment slots interacting with MoveItemMessage
      if (VisibleSlots.size == 2) {
        if (slot == 0) 1 else if (slot == 1) 2 else slot //*_flight
      } else {
        if (slot == 0) 2 else if (slot == 1) 3 else if (slot == 2) 4 else slot //*_gunner
      }
    } else {
      slot
    }
  }

  override def Find(guid: PlanetSideGUID): Option[Int] = {
    weapons.find({
      case (_, obj) =>
        obj.Equipment match {
          case Some(item) => item.HasGUID && item.GUID == guid
          case None       => false
        }
    }) match {
      case Some((index, _)) => Some(index)
      case None             => Inventory.Find(guid)
    }
  }

  override def Fit(obj: Equipment): Option[Int] = {
    recursiveSlotFit(weapons.iterator, obj.Size) match {
      case Some(index) =>
        Some(index)
      case None =>
        trunk.Fit(obj.Definition.Tile)
    }
  }

  @tailrec private def recursiveSlotFit(
                                         iter: Iterator[(Int, EquipmentSlot)],
                                         objSize: EquipmentSize.Value
                                       ): Option[Int] = {
    if (!iter.hasNext) {
      None
    } else {
      val (index, slot) = iter.next()
      if (slot.Equipment.isEmpty && slot.Size.equals(objSize)) {
        Some(index)
      } else {
        recursiveSlotFit(iter, objSize)
      }
    }
  }

  override def Collisions(dest: Int, width: Int, height: Int): Try[List[InventoryItem]] = {
    weapons.get(dest) match {
      case Some(slot) =>
        slot.Equipment match {
          case Some(item) => Success(List(InventoryItem(item, dest)))
          case None       => Success(List())
        }
      case None           => super.Collisions(dest, width, height)
    }
  }

  /**
    * A reference to the `Vehicle` `Trunk` space.
    * @return this `Vehicle` `Trunk`
    */
  def Trunk: GridInventory = {
    this.trunk
  }

  def AccessingTrunk: Option[PlanetSideGUID] = trunkAccess

  def AccessingTrunk_=(guid: PlanetSideGUID): Option[PlanetSideGUID] = {
    AccessingTrunk = Some(guid)
  }

  /**
    * Change which player has access to the trunk of this vehicle.
    * A player may only gain access to the trunk if no one else has access to the trunk at the moment.
    * @param guid the player who wishes to access the trunk
    * @return the player who is currently allowed to access the trunk
    */
  def AccessingTrunk_=(guid: Option[PlanetSideGUID]): Option[PlanetSideGUID] = {
    guid match {
      case None =>
        trunkAccess = None
      case Some(player) =>
        if (trunkAccess.isEmpty) {
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
  def CanAccessTrunk(player: Player): Boolean = {
    if (trunkAccess.isEmpty || trunkAccess.contains(player.GUID)) {
      groupPermissions(3) match {
        case VehicleLockState.Locked => //only the owner
          Owner.isEmpty || (Owner.isDefined && player.GUID == Owner.get)
        case VehicleLockState.Group => //anyone in the owner's squad or platoon
          faction == player.Faction //TODO this is not correct
        case VehicleLockState.Empire => //anyone of the owner's faction
          faction == player.Faction
      }
    } else {
      false
    }
  }

  /**
    * Check access to the `Trunk`.
    * @return the current access value for the `Vehicle` `Trunk`
    */
  def TrunkLockState: VehicleLockState.Value = groupPermissions(3)

  /**
    * Trunk locations are stored as the orientation zero point being to the East. We need to convert that to a North = 0 orientation before returning the location
    * @return A Vector3 of the current trunk location, orientated with North as the zero point
    */
  def TrunkLocation: Vector3 = {
    val rotationRadians = -math.toRadians(Orientation.z - 90f).toFloat
    Vector3.PlanarRotateAroundPoint(Position + Definition.TrunkLocation, Position, rotationRadians)
  }

  def PrepareGatingManifest(): VehicleManifest = {
    val manifest = VehicleManifest(this)
    seats.collect { case (index: Int, seat: Seat) if index > 0 => seat.unmount(seat.occupant) }
    vehicleGatingManifest = Some(manifest)
    previousVehicleGatingManifest = None
    manifest
  }

  def PublishGatingManifest(): Option[VehicleManifest] = {
    val out = vehicleGatingManifest
    previousVehicleGatingManifest = vehicleGatingManifest
    vehicleGatingManifest = None
    out
  }

  def PreviousGatingManifest(): Option[VehicleManifest] = previousVehicleGatingManifest

  def DamageModel = Definition.asInstanceOf[DamageResistanceModel]

  override def BailProtection_=(protect: Boolean): Boolean = {
    !Definition.CanFly && super.BailProtection_=(protect)
  }

  /**
    * This is the definition entry that is used to store and unload pertinent information about the `Vehicle`.
    * @return the vehicle's definition entry
    */
  def Definition: VehicleDefinition = vehicleDef

  def canEqual(other: Any): Boolean = other.isInstanceOf[Vehicle]

  override def equals(other: Any): Boolean =
    other match {
      case that: Vehicle =>
        (that canEqual this) &&
          hashCode() == that.hashCode()
      case _ =>
        false
    }

  override def hashCode(): Int = Actor.hashCode()

  /**
    * Override the string representation to provide additional information.
    * @return the string output
    */
  override def toString: String = {
    Vehicle.toString(this)
  }

  def MaxNtuCapacitor: Float = Definition.MaxNtuCapacitor
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
  final case class VehicleMessages(player: Player, response: Exchange)

  /**
    * Initiate vehicle deconstruction.
    * @see `VehicleControl`
    * @param time the delay before deconstruction should initiate;
    *             should initiate instantly when `None`
    */
  final case class Deconstruct(time: Option[FiniteDuration] = None)

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
  final case class ChargeShields(amount: Int)

  /**
    * Following a successful shield charge tick, display the results of the update.
    * @see `FacilityBenefitShieldChargeRequestMessage`
    * @param vehicle the updated vehicle
    */
  final case class UpdateShieldsCharge(vehicle: Vehicle)

  final case class UpdateSubsystemStates(toChannel: String, stateToUpdateFor: Option[Boolean] = None)

  /**
    * Change a vehicle's internal ownership property to match that of the target player.
    * @param player the person who will own the vehicle, or `None` if the vehicle will go unowned
    */
  final case class Ownership(player: Option[Player])

  object Ownership {
    def apply(player: Player): Ownership = Ownership(Some(player))
  }

  /**
    * For vehicles, this pertains mainly to resending information needs to display the the drowning red progress bar
    * that is a product of the `OxygenStateMessage` packet to vehicle passengers.
    * It also forces passengers to update their internal understanding of their own drowning state.
    * @param passenger a player mounted in the vehicle
    */
  final case class UpdateZoneInteractionProgressUI(passenger : Player)

  /**
    * Overloaded constructor.
    * @param vehicleDef the vehicle's definition entry
    * @return a `Vehicle` object
    */
  def apply(vehicleDef: VehicleDefinition): Vehicle = {
    new Vehicle(vehicleDef)
  }

  /**
    * Given a `Map` of `Utility` objects, only return the objects with a positive or zero-index position.
    * @return a map of applicable utilities
    */
  def EquipmentUtilities(utilities: Map[Int, Utility]): Map[Int, Utility] = {
    utilities.filter({ case (index: Int, _: Utility) => index > -1 })
  }

  /**
    * Use the `*Definition` that was provided to this object to initialize its fields and settings.
    * @param vehicle the `Vehicle` being initialized
    * @see `{object}.LoadDefinition`
    */
  def LoadDefinition(vehicle: Vehicle): Vehicle = {
    val vdef: VehicleDefinition = vehicle.Definition
    //general stuff
    vehicle.Health = vdef.DefaultHealth
    vehicle.Shields = vdef.DefaultShields
    vehicle.Capacitor = vdef.DefaultCapacitor
    //create weapons
    vehicle.weapons = vdef.Weapons.map[Int, EquipmentSlot] {
      case (num: Int, definition: ToolDefinition) =>
        val slot = EquipmentSlot(definition.Size)
        slot.Equipment = Tool(definition)
        num -> slot
    }.toMap
    //create seats
    vehicle.seats = vdef.Seats.map[Int, Seat] {
      case (num: Int, definition: SeatDefinition) =>
        num -> new Seat(definition)
    }.toMap
    // create cargo holds
    vehicle.cargoHolds = vdef.Cargo.map[Int, Cargo] {
      case (num, definition) =>
        num -> new Cargo(definition)
    }.toMap
    //create utilities
    vehicle.utilities = vdef.Utilities.map[Int, Utility] {
      case (num: Int, util: UtilityType.Value) =>
        val obj     = Utility(util, vehicle)
        val utilObj = obj()
        vehicle.Amenities = utilObj
        utilObj.LocationOffset = vdef.UtilityOffset.get(num)
        num -> obj
    }.toMap
    //subsystems
    vehicle.subsystems = vdef.subsystems.map { entry => new VehicleSubsystem(entry) }
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
  def toString(obj: Vehicle): String = {
    val occupancy = obj.Seats.values.count(seat => seat.isOccupied)
    s"${obj.Definition.Name}, owned by ${obj.Owner}: (${obj.Health}/${obj.MaxHealth})(${obj.Shields}/${obj.MaxShields}) ($occupancy)"
  }
}
