// Copyright (c) 2017 PSForever
package objects

import akka.actor.Props
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects._
import net.psforever.objects.ballistics.{PlayerSource, Projectile, ProjectileResolution, ResolvedProjectile}
import net.psforever.objects.definition.{SeatDefinition, VehicleDefinition}
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vehicles._
import net.psforever.objects.vital.{VehicleShieldCharge, Vitality}
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.types.{PlanetSideGUID, _}
import org.specs2.mutable._
import services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration._

class VehicleTest extends Specification {
  import VehicleTest._

  "SeatDefinition" should {
    val seat = new SeatDefinition
    seat.ArmorRestriction = SeatArmorRestriction.MaxOnly
    seat.Bailable = true
    seat.ControlledWeapon = 5

    "define (default)" in {
      val t_seat = new SeatDefinition
      t_seat.ArmorRestriction mustEqual SeatArmorRestriction.NoMax
      t_seat.Bailable mustEqual false
      t_seat.ControlledWeapon mustEqual None
    }

    "define (custom)" in {
      seat.ArmorRestriction mustEqual SeatArmorRestriction.MaxOnly
      seat.Bailable mustEqual true
      seat.ControlledWeapon mustEqual Some(5)
    }
  }

  "VehicleDefinition" should {
    "define" in {
      val fury = GlobalDefinitions.fury
      fury.CanBeOwned mustEqual true
      fury.CanCloak mustEqual false
      fury.Seats.size mustEqual 1
      fury.Seats(0).Bailable mustEqual true
      fury.Seats(0).ControlledWeapon mustEqual Some(1)
      fury.MountPoints.size mustEqual 2
      fury.MountPoints.get(1) mustEqual Some(0)
      fury.MountPoints.get(2) mustEqual Some(0)
      fury.Weapons.size mustEqual 1
      fury.Weapons.get(0) mustEqual None
      fury.Weapons.get(1) mustEqual Some(GlobalDefinitions.fury_weapon_systema)
      fury.TrunkSize.Width mustEqual 11
      fury.TrunkSize.Height mustEqual 11
      fury.TrunkOffset mustEqual 30
    }
  }

  "Seat" should {
    val seat_def = new SeatDefinition
    seat_def.ArmorRestriction = SeatArmorRestriction.MaxOnly
    seat_def.Bailable = true
    seat_def.ControlledWeapon = 5

    "construct" in {
      val seat = new Seat(seat_def)
      seat.ArmorRestriction mustEqual SeatArmorRestriction.MaxOnly
      seat.Bailable mustEqual true
      seat.ControlledWeapon mustEqual Some(5)
      seat.isOccupied mustEqual false
      seat.Occupant mustEqual None
    }

    "player can sit" in {
      val seat = new Seat(seat_def)
      seat.Occupant.isDefined mustEqual false

      val player1 = Player(avatar1)
      player1.ExoSuit = ExoSuitType.MAX
      seat.Occupant = player1
      seat.Occupant.isDefined mustEqual true
      seat.Occupant.contains(player1) mustEqual true
    }

    "one occupant at a time" in {
      val seat = new Seat(seat_def)
      seat.Occupant.isDefined mustEqual false

      val player1 = Player(avatar1)
      player1.ExoSuit = ExoSuitType.MAX
      seat.Occupant = player1
      seat.Occupant.isDefined mustEqual true
      seat.Occupant.contains(player1) mustEqual true

      val player2 = Player(avatar1)
      player2.ExoSuit = ExoSuitType.MAX
      seat.Occupant = player2
      seat.Occupant.isDefined mustEqual true
      seat.Occupant.contains(player1) mustEqual true
    }

    "one player must get out of seat before other can get in" in {
      val seat = new Seat(seat_def)
      seat.Occupant.isDefined mustEqual false

      val player1 = Player(avatar1)
      player1.ExoSuit = ExoSuitType.MAX
      seat.Occupant = player1
      seat.Occupant.isDefined mustEqual true
      seat.Occupant.contains(player1) mustEqual true

      val player2 = Player(avatar2)
      player2.ExoSuit = ExoSuitType.MAX
      seat.Occupant = player2
      seat.Occupant.isDefined mustEqual true
      seat.Occupant.contains(player2) mustEqual false
      seat.Occupant.contains(player1) mustEqual true

      seat.Occupant = None
      seat.Occupant.isDefined mustEqual false
      seat.Occupant = player2
      seat.Occupant.isDefined mustEqual true
      seat.Occupant.contains(player2) mustEqual true
    }
  }

  "Vehicle" should {
    "construct" in {
      Vehicle(GlobalDefinitions.fury)
      ok
    }

    "construct (detailed)" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.Owner mustEqual None
      fury_vehicle.Seats.size mustEqual 1
      fury_vehicle.Seats(0).ArmorRestriction mustEqual SeatArmorRestriction.NoMax
      fury_vehicle.Seats(0).isOccupied mustEqual false
      fury_vehicle.Seats(0).Occupant mustEqual None
      fury_vehicle.Seats(0).Bailable mustEqual true
      fury_vehicle.Seats(0).ControlledWeapon mustEqual Some(1)
      fury_vehicle.PermissionGroup(0) mustEqual Some(VehicleLockState.Locked) //driver
      fury_vehicle.PermissionGroup(1) mustEqual Some(VehicleLockState.Empire) //gunner
      fury_vehicle.PermissionGroup(2) mustEqual Some(VehicleLockState.Empire) //passenger
      fury_vehicle.PermissionGroup(3) mustEqual Some(VehicleLockState.Locked) //trunk
      fury_vehicle.Weapons.size mustEqual 1
      fury_vehicle.Weapons.get(0) mustEqual None
      fury_vehicle.Weapons.get(1).isDefined mustEqual true
      fury_vehicle.Weapons(1).Equipment.isDefined mustEqual true
      fury_vehicle.Weapons(1).Equipment.get.Definition mustEqual GlobalDefinitions.fury.Weapons(1)
      fury_vehicle.WeaponControlledFromSeat(0) mustEqual fury_vehicle.Weapons(1).Equipment
      fury_vehicle.Trunk.Width mustEqual 11
      fury_vehicle.Trunk.Height mustEqual 11
      fury_vehicle.Trunk.Offset mustEqual 30
      fury_vehicle.GetSeatFromMountPoint(1) mustEqual Some(0)
      fury_vehicle.GetSeatFromMountPoint(2) mustEqual Some(0)
      fury_vehicle.Decal mustEqual 0
      fury_vehicle.Health mustEqual fury_vehicle.Definition.MaxHealth
    }

    "can be owned by a player" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.Owner.isDefined mustEqual false

      val player1 = Player(avatar1)
      player1.GUID = PlanetSideGUID(1)
      fury_vehicle.Owner = player1
      fury_vehicle.Owner.isDefined mustEqual true
      fury_vehicle.Owner.contains(PlanetSideGUID(1)) mustEqual true
    }

    "ownership depends on who last was granted it" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.Owner.isDefined mustEqual false

      val player1 = Player(avatar1)
      player1.GUID = PlanetSideGUID(1)
      fury_vehicle.Owner = player1
      fury_vehicle.Owner.isDefined mustEqual true
      fury_vehicle.Owner.contains(PlanetSideGUID(1)) mustEqual true

      val player2 = Player(avatar2)
      player2.GUID = PlanetSideGUID(2)
      fury_vehicle.Owner = player2
      fury_vehicle.Owner.isDefined mustEqual true
      fury_vehicle.Owner.contains(PlanetSideGUID(2)) mustEqual true
    }

    "can use mount point to get seat number" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.GetSeatFromMountPoint(0) mustEqual None
      fury_vehicle.GetSeatFromMountPoint(1) mustEqual Some(0)
      fury_vehicle.GetSeatFromMountPoint(2) mustEqual Some(0)
      fury_vehicle.GetSeatFromMountPoint(3) mustEqual None
    }

    "has four permission groups" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id) mustEqual Some(VehicleLockState.Locked)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Gunner.id) mustEqual Some(VehicleLockState.Empire)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Passenger.id) mustEqual Some(VehicleLockState.Empire)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Trunk.id) mustEqual Some(VehicleLockState.Locked)
    }

    "set new permission level" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id) mustEqual Some(VehicleLockState.Locked)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id, VehicleLockState.Group.id) mustEqual Some(VehicleLockState.Group)
    }

    "set the same permission level" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id) mustEqual Some(VehicleLockState.Locked)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id, VehicleLockState.Locked.id) mustEqual None
    }

    "alternate permission level indices" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id) mustEqual fury_vehicle.PermissionGroup(10)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Gunner.id) mustEqual fury_vehicle.PermissionGroup(11)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Passenger.id) mustEqual fury_vehicle.PermissionGroup(12)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Trunk.id) mustEqual fury_vehicle.PermissionGroup(13)

      (AccessPermissionGroup.Driver.id + 10) mustEqual 10
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id, VehicleLockState.Group.id) mustEqual Some(VehicleLockState.Group)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id) mustEqual fury_vehicle.PermissionGroup(10)
    }

    "can determine permission group from seat" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      harasser_vehicle.SeatPermissionGroup(0) mustEqual Some(AccessPermissionGroup.Driver)
      harasser_vehicle.SeatPermissionGroup(1) mustEqual Some(AccessPermissionGroup.Gunner)
      harasser_vehicle.SeatPermissionGroup(2) mustEqual None
      //TODO test for AccessPermissionGroup.Passenger later
    }

    "can find a passenger in a seat" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val player1 = Player(avatar1)
      player1.GUID = PlanetSideGUID(1)
      val player2 = Player(avatar2)
      player2.GUID = PlanetSideGUID(2)
      harasser_vehicle.Seat(0).get.Occupant = player1 //don't worry about ownership for now
      harasser_vehicle.Seat(1).get.Occupant = player2

      harasser_vehicle.PassengerInSeat(player1) mustEqual Some(0)
      harasser_vehicle.PassengerInSeat(player2) mustEqual Some(1)
      harasser_vehicle.Seat(0).get.Occupant = None
      harasser_vehicle.PassengerInSeat(player1) mustEqual None
      harasser_vehicle.PassengerInSeat(player2) mustEqual Some(1)
    }

    "can find a weapon controlled from seat" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val chaingun_p = harasser_vehicle.Weapons(2).Equipment
      chaingun_p.isDefined mustEqual true

      harasser_vehicle.WeaponControlledFromSeat(0) mustEqual None
      harasser_vehicle.WeaponControlledFromSeat(1) mustEqual chaingun_p
    }

    "can filter utilities with indices that are natural numbers" in {
      val objDef = VehicleDefinition(1)
      objDef.Utilities += -1 -> UtilityType.order_terminala
      objDef.Utilities += 0 -> UtilityType.order_terminalb
      objDef.Utilities += 2 -> UtilityType.order_terminalb
      val obj = Vehicle(objDef)

      obj.Utilities.size mustEqual 3
      obj.Utilities(-1).UtilType mustEqual UtilityType.order_terminala
      obj.Utilities(0).UtilType mustEqual UtilityType.order_terminalb
      obj.Utilities.get(1) mustEqual None
      obj.Utilities(2).UtilType mustEqual UtilityType.order_terminalb

      val filteredMap = Vehicle.EquipmentUtilities(obj.Utilities)
      filteredMap.size mustEqual 2
      filteredMap.get(-1) mustEqual None
      filteredMap(0).UtilType mustEqual UtilityType.order_terminalb
      filteredMap(2).UtilType mustEqual UtilityType.order_terminalb
    }

    "access its mounted weapons by Slot" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      harasser_vehicle.Weapons(2).Equipment.get.GUID = PlanetSideGUID(10)

      harasser_vehicle.Slot(2).Equipment.get.GUID mustEqual PlanetSideGUID(10)
    }

    "access its trunk by Slot" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val ammobox = AmmoBox(GlobalDefinitions.armor_canister)
      ammobox.GUID = PlanetSideGUID(10)
      harasser_vehicle.Inventory += 30 -> ammobox

      harasser_vehicle.Slot(30).Equipment.get.GUID mustEqual PlanetSideGUID(10)
    }

    "find its mounted weapons by GUID" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      harasser_vehicle.Weapons(2).Equipment.get.GUID = PlanetSideGUID(10)

      harasser_vehicle.Find(PlanetSideGUID(10)) mustEqual Some(2)
    }

    "find items in its trunk by GUID" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val ammobox = AmmoBox(GlobalDefinitions.armor_canister)
      ammobox.GUID = PlanetSideGUID(10)
      harasser_vehicle.Inventory += 30 -> ammobox

      harasser_vehicle.Find(PlanetSideGUID(10)) mustEqual Some(30)
    }
  }
}

class VehicleControlStopMountingTest extends ActorTest {
  "Vehicle Control" should {
    "deactivate and stop handling mount messages" in {
      val player1 = Player(VehicleTest.avatar1)
      player1.GUID = PlanetSideGUID(1)
      val player2 = Player(VehicleTest.avatar2)
      val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      vehicle.Faction = PlanetSideEmpire.TR
      vehicle.GUID = PlanetSideGUID(3)
      vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
      vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
        VehicleEvents = new TestProbe(system).ref //necessary
      }
      vehicle.Weapons(2).Equipment.get.GUID = PlanetSideGUID(4)
      val probe = new TestProbe(system)

      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref)
      val reply = probe.receiveOne(Duration.create(200, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])

      vehicle.Actor.tell(Vehicle.PrepareForDeletion(), probe.ref)
      vehicle.Actor.tell(Mountable.TryMount(player2, 1), probe.ref)
      probe.expectNoMsg(Duration.create(200, "ms")) //assertion failed: received unexpected message MountMessages(CanMount
    }
  }
}

class VehicleControlRestartMountingTest extends ActorTest {
  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)
  val player2 = Player(VehicleTest.avatar2)
  player2.GUID = PlanetSideGUID(2)
  val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(3)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
    VehicleEvents = new TestProbe(system).ref
  }
  vehicle.Weapons(2).Equipment.get.GUID = PlanetSideGUID(4)
  val probe = new TestProbe(system)

  "Vehicle Control" should {
    "reactivate and resume handling mount messages" in {
      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref)
      probe.receiveOne(Duration.create(200, "ms")) //discard
      vehicle.Actor.tell(Vehicle.PrepareForDeletion(), probe.ref)
      vehicle.Actor.tell(Mountable.TryMount(player2, 1), probe.ref)
      probe.expectNoMsg(Duration.create(200, "ms"))

      vehicle.Actor.tell(Vehicle.Reactivate(), probe.ref)
      vehicle.Actor.tell(Mountable.TryMount(player2, 1), probe.ref)
      val reply = probe.receiveOne(Duration.create(200, "ms"))
      assert(reply.isInstanceOf[Mountable.MountMessages])
    }
  }
}

class VehicleControlAlwaysDismountTest extends ActorTest {
  val probe = new TestProbe(system)
  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)
  val player2 = Player(VehicleTest.avatar2)
  player2.GUID = PlanetSideGUID(2)
  val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(3)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
    VehicleEvents = new TestProbe(system).ref
  }
  vehicle.Weapons(2).Equipment.get.GUID = PlanetSideGUID(4)

  "Vehicle Control" should {
    "always allow dismount messages" in {
      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref)
      probe.receiveOne(Duration.create(100, "ms")) //discard
      vehicle.Actor.tell(Mountable.TryMount(player2, 1), probe.ref)
      probe.receiveOne(Duration.create(100, "ms")) //discard

      vehicle.Actor.tell(Mountable.TryDismount(player2, 1), probe.ref) //player2 requests dismount
      val reply1 = probe.receiveOne(Duration.create(100, "ms"))
      assert(reply1.isInstanceOf[Mountable.MountMessages])
      assert(reply1.asInstanceOf[Mountable.MountMessages].response.isInstanceOf[Mountable.CanDismount]) //player2 dismounts

      vehicle.Actor.tell(Vehicle.PrepareForDeletion(), probe.ref)
      vehicle.Actor.tell(Mountable.TryDismount(player1, 0), probe.ref) //player1 requests dismount
      val reply2 = probe.receiveOne(Duration.create(100, "ms"))
      assert(reply2.isInstanceOf[Mountable.MountMessages])
      assert(reply2.asInstanceOf[Mountable.MountMessages].response.isInstanceOf[Mountable.CanDismount]) //player1 dismounts
    }
  }
}

class VehicleControlMountingBlockedExosuitTest extends ActorTest {
  val probe = new TestProbe(system)
  def checkCanNotMount() : Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg : Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanNotMount])
      case _ =>
        assert(false)
    }
  }

  def checkCanMount() : Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg : Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanMount])
      case _ =>
        assert(false)
    }
  }
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")

  val player1 = Player(VehicleTest.avatar1)
  player1.ExoSuit = ExoSuitType.Reinforced
  player1.GUID = PlanetSideGUID(1)
  val player2 = Player(VehicleTest.avatar1)
  player2.ExoSuit = ExoSuitType.MAX
  player2.GUID = PlanetSideGUID(2)
  val player3 = Player(VehicleTest.avatar1)
  player3.ExoSuit = ExoSuitType.Agile
  player3.GUID = PlanetSideGUID(3)

  "Vehicle Control" should {
    "block players from sitting if their exo-suit is not allowed by the seat" in {
      //disallow
      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref) //Reinforced in non-MAX seat
      checkCanNotMount()
      vehicle.Actor.tell(Mountable.TryMount(player2, 0), probe.ref) //MAX in non-Reinforced seat
      checkCanNotMount()
      vehicle.Actor.tell(Mountable.TryMount(player2, 1), probe.ref) //MAX in non-MAX seat
      checkCanNotMount()
      vehicle.Actor.tell(Mountable.TryMount(player1, 9), probe.ref) //Reinforced in MAX-only seat
      checkCanNotMount()
      vehicle.Actor.tell(Mountable.TryMount(player3, 9), probe.ref) //Agile in MAX-only seat
      checkCanNotMount()

      //allow
      vehicle.Actor.tell(Mountable.TryMount(player1, 1), probe.ref)
      checkCanMount()
      vehicle.Actor.tell(Mountable.TryMount(player2, 9), probe.ref)
      checkCanMount()
      vehicle.Actor.tell(Mountable.TryMount(player3, 0), probe.ref)
      checkCanMount()
    }
  }
}

class VehicleControlMountingBlockedSeatPermissionTest extends ActorTest {
  val probe = new TestProbe(system)
  def checkCanNotMount() : Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg : Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanNotMount])
      case _ =>
        assert(false)
    }
  }

  def checkCanMount() : Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg : Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanMount])
      case _ =>
        assert(false)
    }
  }
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")

  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)
  val player2 = Player(VehicleTest.avatar1)
  player2.GUID = PlanetSideGUID(2)

  "Vehicle Control" should {
    //11 June 2018: Group is not supported yet so do not bother testing it
    "block players from sitting if the seat does not allow it" in {

      vehicle.PermissionGroup(2,3) //passenger group -> empire
      vehicle.Actor.tell(Mountable.TryMount(player1, 3), probe.ref) //passenger seat
      checkCanMount()
      vehicle.PermissionGroup(2,0) //passenger group -> locked
      vehicle.Actor.tell(Mountable.TryMount(player2, 4), probe.ref) //passenger seat
      checkCanNotMount()
    }
  }
}

class VehicleControlMountingDriverSeatTest extends ActorTest {
  val probe = new TestProbe(system)
  def checkCanMount() : Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg : Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanMount])
      case _ =>
        assert(false)
    }
  }
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)

  "Vehicle Control" should {
    "allow players to sit in the driver seat, even if it is locked, if the vehicle is unowned" in {
      assert(vehicle.PermissionGroup(0).contains(VehicleLockState.Locked)) //driver group -> locked
      assert(vehicle.Seats(0).Occupant.isEmpty)
      assert(vehicle.Owner.isEmpty)
      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref)
      checkCanMount()
      assert(vehicle.Seats(0).Occupant.nonEmpty)
    }
  }
}

class VehicleControlMountingOwnedLockedDriverSeatTest extends ActorTest {
  val probe = new TestProbe(system)
  def checkCanNotMount() : Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg : Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanNotMount])
      case _ =>
        assert(false)
    }
  }

  def checkCanMount() : Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg : Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanMount])
      case _ =>
        assert(false)
    }
  }
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)
  val player2 = Player(VehicleTest.avatar1)
  player2.GUID = PlanetSideGUID(2)

  "Vehicle Control" should {
    "block players that are not the current owner from sitting in the driver seat (locked)" in {
      assert(vehicle.PermissionGroup(0).contains(VehicleLockState.Locked)) //driver group -> locked
      assert(vehicle.Seats(0).Occupant.isEmpty)
      vehicle.Owner = player1.GUID

      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref)
      checkCanMount()
      assert(vehicle.Seats(0).Occupant.nonEmpty)
      vehicle.Actor.tell(Mountable.TryDismount(player1, 0), probe.ref)
      probe.receiveOne(Duration.create(100, "ms")) //discard
      assert(vehicle.Seats(0).Occupant.isEmpty)

      vehicle.Actor.tell(Mountable.TryMount(player2, 0), probe.ref)
      checkCanNotMount()
      assert(vehicle.Seats(0).Occupant.isEmpty)
    }
  }
}

class VehicleControlMountingOwnedUnlockedDriverSeatTest extends ActorTest {
  val probe = new TestProbe(system)
  def checkCanMount() : Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg : Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanMount])
      case _ =>
        assert(false)
    }
  }
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)
  val player2 = Player(VehicleTest.avatar1)
  player2.GUID = PlanetSideGUID(2)

  "Vehicle Control" should {
    "allow players that are not the current owner to sit in the driver seat (empire)" in {
      vehicle.PermissionGroup(0,3) //passenger group -> empire
      assert(vehicle.PermissionGroup(0).contains(VehicleLockState.Empire)) //driver group -> empire
      assert(vehicle.Seats(0).Occupant.isEmpty)
      vehicle.Owner = player1.GUID //owner set

      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref)
      checkCanMount()
      assert(vehicle.Seats(0).Occupant.nonEmpty)
      vehicle.Actor.tell(Mountable.TryDismount(player1, 0), probe.ref)
      probe.receiveOne(Duration.create(100, "ms")) //discard
      assert(vehicle.Seats(0).Occupant.isEmpty)

      vehicle.Actor.tell(Mountable.TryMount(player2, 0), probe.ref)
      checkCanMount()
      assert(vehicle.Seats(0).Occupant.nonEmpty)
    }
  }
}

class VehicleControlShieldsChargingTest extends ActorTest {
  val probe = new TestProbe(system)
  val vehicle = Vehicle(GlobalDefinitions.fury)
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
    VehicleEvents = probe.ref
  }

  "charge vehicle shields" in {
    assert(vehicle.Shields == 0)
    assert(!vehicle.History.exists({p => p.isInstanceOf[VehicleShieldCharge]}))

    vehicle.Actor ! Vehicle.ChargeShields(15)
    val msg = probe.receiveOne(500 milliseconds)
    assert(msg match {
      case VehicleServiceMessage(_, VehicleAction.PlanetsideAttribute(_, PlanetSideGUID(10), 68, 15)) => true
      case _ => false
    })
    assert(vehicle.Shields == 15)
    assert(vehicle.History.exists({p => p.isInstanceOf[VehicleShieldCharge]}))
  }
}

class VehicleControlShieldsNotChargingVehicleDeadTest extends ActorTest {
  val probe = new TestProbe(system)
  val vehicle = Vehicle(GlobalDefinitions.fury)
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
    VehicleEvents = probe.ref
  }

  "not charge vehicle shields if the vehicle is destroyed" in {
    assert(vehicle.Health > 0)
    vehicle.Health = 0
    assert(vehicle.Health == 0)
    assert(vehicle.Shields == 0)
    assert(!vehicle.History.exists({p => p.isInstanceOf[VehicleShieldCharge]}))
    vehicle.Actor.tell(Vehicle.ChargeShields(15), probe.ref)

    probe.expectNoMsg(1 seconds)
    assert(vehicle.Shields == 0)
    assert(!vehicle.History.exists({p => p.isInstanceOf[VehicleShieldCharge]}))
  }
}

class VehicleControlShieldsNotChargingVehicleShieldsFullTest extends ActorTest {
  val probe = new TestProbe(system)
  val vehicle = Vehicle(GlobalDefinitions.fury)
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
    VehicleEvents = probe.ref
  }

  "not charge vehicle shields if the vehicle is destroyed" in {
    assert(vehicle.Shields == 0)
    vehicle.Shields = vehicle.MaxShields
    assert(vehicle.Shields == vehicle.MaxShields)
    assert(!vehicle.History.exists({p => p.isInstanceOf[VehicleShieldCharge]}))
    vehicle.Actor ! Vehicle.ChargeShields(15)

    probe.expectNoMsg(1 seconds)
    assert(!vehicle.History.exists({p => p.isInstanceOf[VehicleShieldCharge]}))
  }
}

class VehicleControlShieldsNotChargingTooEarlyTest extends ActorTest {
  val probe = new TestProbe(system)
  val vehicle = Vehicle(GlobalDefinitions.fury)
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
    VehicleEvents = probe.ref
  }

  "charge vehicle shields" in {
    assert(vehicle.Shields == 0)

    vehicle.Actor ! Vehicle.ChargeShields(15)
    val msg = probe.receiveOne(200 milliseconds)
    //assert(msg.isInstanceOf[Vehicle.UpdateShieldsCharge])
    assert(msg match {
      case VehicleServiceMessage(_, VehicleAction.PlanetsideAttribute(_, PlanetSideGUID(10), 68, 15)) => true
      case _ => false
    })
    assert(vehicle.Shields == 15)

    vehicle.Actor ! Vehicle.ChargeShields(15)
    probe.expectNoMsg(200 milliseconds)
    assert(vehicle.Shields == 15)
  }
}

//TODO implement message protocol for zone startup completion
//class VehicleControlShieldsNotChargingDamagedTest extends ActorTest {
//  val probe = new TestProbe(system)
//  val vehicle = Vehicle(GlobalDefinitions.fury)
//  vehicle.GUID = PlanetSideGUID(10)
//  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
//  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
//    VehicleEvents = probe.ref
//  }
//  //
//  val beamer_wep = Tool(GlobalDefinitions.beamer)
//  val p_source = PlayerSource( Player(Avatar("TestTarget", PlanetSideEmpire.NC, CharacterGender.Female, 1, CharacterVoice.Mute)) )
//  val projectile = Projectile(beamer_wep.Projectile, GlobalDefinitions.beamer, beamer_wep.FireMode, p_source, GlobalDefinitions.beamer.ObjectId, Vector3.Zero, Vector3.Zero)
//  val fury_dm = Vehicle(GlobalDefinitions.fury).DamageModel
//  val obj = ResolvedProjectile(ProjectileResolution.Hit, projectile, p_source, fury_dm, Vector3(1.2f, 3.4f, 5.6f))
//
//  "not charge vehicle shields if recently damaged" in {
//    assert(vehicle.Shields == 0)
//    vehicle.Actor.tell(Vitality.Damage({case v : Vehicle => v.History(obj); obj }), probe.ref)
//
//    val msg = probe.receiveOne(200 milliseconds)
//    assert(msg.isInstanceOf[Vitality.DamageResolution])
//    assert(vehicle.Shields == 0)
//    vehicle.Actor.tell(Vehicle.ChargeShields(15), probe.ref)
//
//    probe.expectNoMsg(200 milliseconds)
//    assert(vehicle.Shields == 0)
//  }
//}

object VehicleTest {
  import net.psforever.objects.Avatar
  import net.psforever.types.{CharacterGender, PlanetSideEmpire}
  val avatar1 = Avatar("test1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
  val avatar2 = Avatar("test2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
}
