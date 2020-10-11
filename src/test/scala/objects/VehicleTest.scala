// Copyright (c) 2017 PSForever
package objects

import akka.actor.Props
import akka.testkit.TestProbe
import base.{ActorTest, FreedContextActorTest}
import net.psforever.objects._
import net.psforever.objects.definition.{SeatDefinition, VehicleDefinition}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vehicles._
import net.psforever.objects.vital.VehicleShieldCharge
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.packet.game.{CargoMountPointStatusMessage, ObjectDetachMessage, PlanetsideAttributeMessage}
import net.psforever.types.{PlanetSideGUID, _}
import org.specs2.mutable._
import net.psforever.services.ServiceManager
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}

import scala.concurrent.duration._
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.ZoneActor

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
      t_seat.ControlledWeapon.isEmpty mustEqual true
    }

    "define (custom)" in {
      seat.ArmorRestriction mustEqual SeatArmorRestriction.MaxOnly
      seat.Bailable mustEqual true
      seat.ControlledWeapon.contains(5)
    }
  }

  "VehicleDefinition" should {
    "define" in {
      val fury = GlobalDefinitions.fury
      fury.CanBeOwned mustEqual true
      fury.CanCloak mustEqual false
      fury.Seats.size mustEqual 1
      fury.Seats(0).Bailable mustEqual true
      fury.Seats(0).ControlledWeapon.contains(1)
      fury.MountPoints.size mustEqual 2
      fury.MountPoints.get(1).contains(0)
      fury.MountPoints.get(2).contains(0)
      fury.Weapons.size mustEqual 1
      fury.Weapons.get(0).isEmpty mustEqual true
      fury.Weapons.get(1).contains(GlobalDefinitions.fury_weapon_systema)
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
      seat.ControlledWeapon.contains(5)
      seat.isOccupied mustEqual false
      seat.Occupant.isEmpty mustEqual true
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
      fury_vehicle.Owner.isEmpty mustEqual true
      fury_vehicle.Seats.size mustEqual 1
      fury_vehicle.Seats(0).ArmorRestriction mustEqual SeatArmorRestriction.NoMax
      fury_vehicle.Seats(0).isOccupied mustEqual false
      fury_vehicle.Seats(0).Occupant.isEmpty mustEqual true
      fury_vehicle.Seats(0).Bailable mustEqual true
      fury_vehicle.Seats(0).ControlledWeapon.contains(1)
      fury_vehicle.PermissionGroup(0).contains(VehicleLockState.Locked) //driver
      fury_vehicle.PermissionGroup(1).contains(VehicleLockState.Empire) //gunner
      fury_vehicle.PermissionGroup(2).contains(VehicleLockState.Empire) //passenger
      fury_vehicle.PermissionGroup(3).contains(VehicleLockState.Locked) //trunk
      fury_vehicle.Weapons.size mustEqual 1
      fury_vehicle.Weapons.get(0).isEmpty mustEqual true
      fury_vehicle.Weapons.get(1).isDefined mustEqual true
      fury_vehicle.Weapons(1).Equipment.isDefined mustEqual true
      fury_vehicle.Weapons(1).Equipment.get.Definition mustEqual GlobalDefinitions.fury.Weapons(1)
      fury_vehicle.WeaponControlledFromSeat(0) mustEqual fury_vehicle.Weapons(1).Equipment
      fury_vehicle.Trunk.Width mustEqual 11
      fury_vehicle.Trunk.Height mustEqual 11
      fury_vehicle.Trunk.Offset mustEqual 30
      fury_vehicle.GetSeatFromMountPoint(1).contains(0)
      fury_vehicle.GetSeatFromMountPoint(2).contains(0)
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
      fury_vehicle.GetSeatFromMountPoint(0).isEmpty mustEqual true
      fury_vehicle.GetSeatFromMountPoint(1).contains(0)
      fury_vehicle.GetSeatFromMountPoint(2).contains(0)
      fury_vehicle.GetSeatFromMountPoint(3).isEmpty mustEqual true
    }

    "has four permission groups" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id).contains(VehicleLockState.Locked)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Gunner.id).contains(VehicleLockState.Empire)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Passenger.id).contains(VehicleLockState.Empire)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Trunk.id).contains(VehicleLockState.Locked)
    }

    "set new permission level" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id).contains(VehicleLockState.Locked)
      fury_vehicle
        .PermissionGroup(AccessPermissionGroup.Driver.id, VehicleLockState.Group.id)
        .contains(VehicleLockState.Group)
    }

    "set the same permission level" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id).contains(VehicleLockState.Locked)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id, VehicleLockState.Locked.id).isEmpty mustEqual true
    }

    "alternate permission level indices" in {
      val fury_vehicle = Vehicle(GlobalDefinitions.fury)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id) mustEqual fury_vehicle.PermissionGroup(10)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Gunner.id) mustEqual fury_vehicle.PermissionGroup(11)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Passenger.id) mustEqual fury_vehicle.PermissionGroup(12)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Trunk.id) mustEqual fury_vehicle.PermissionGroup(13)

      (AccessPermissionGroup.Driver.id + 10) mustEqual 10
      fury_vehicle
        .PermissionGroup(AccessPermissionGroup.Driver.id, VehicleLockState.Group.id)
        .contains(VehicleLockState.Group)
      fury_vehicle.PermissionGroup(AccessPermissionGroup.Driver.id) mustEqual fury_vehicle.PermissionGroup(10)
    }

    "can determine permission group from seat" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      harasser_vehicle.SeatPermissionGroup(0).contains(AccessPermissionGroup.Driver)
      harasser_vehicle.SeatPermissionGroup(1).contains(AccessPermissionGroup.Gunner)
      harasser_vehicle.SeatPermissionGroup(2).isEmpty mustEqual true
      //TODO test for AccessPermissionGroup.Passenger later
    }

    "can find a passenger in a seat" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val player1          = Player(avatar1)
      player1.GUID = PlanetSideGUID(1)
      val player2 = Player(avatar2)
      player2.GUID = PlanetSideGUID(2)
      harasser_vehicle.Seat(0).get.Occupant = player1 //don't worry about ownership for now
      harasser_vehicle.Seat(1).get.Occupant = player2

      harasser_vehicle.PassengerInSeat(player1).contains(0)
      harasser_vehicle.PassengerInSeat(player2).contains(1)
      harasser_vehicle.Seat(0).get.Occupant = None
      harasser_vehicle.PassengerInSeat(player1).isEmpty mustEqual true
      harasser_vehicle.PassengerInSeat(player2).contains(1)
    }

    "can find a weapon controlled from seat" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val chaingun_p       = harasser_vehicle.Weapons(2).Equipment
      chaingun_p.isDefined mustEqual true

      harasser_vehicle.WeaponControlledFromSeat(0).isEmpty mustEqual true
      harasser_vehicle.WeaponControlledFromSeat(1) mustEqual chaingun_p
    }

    "can filter utilities with indices that are natural numbers" in {
      val objDef = VehicleDefinition(1)
      objDef.Utilities += -1 -> UtilityType.order_terminala
      objDef.Utilities += 0  -> UtilityType.order_terminalb
      objDef.Utilities += 2  -> UtilityType.order_terminalb
      val obj = Vehicle(objDef)

      obj.Utilities.size mustEqual 3
      obj.Utilities(-1).UtilType mustEqual UtilityType.order_terminala
      obj.Utilities(0).UtilType mustEqual UtilityType.order_terminalb
      obj.Utilities.get(1).isEmpty mustEqual true
      obj.Utilities(2).UtilType mustEqual UtilityType.order_terminalb

      val filteredMap = Vehicle.EquipmentUtilities(obj.Utilities)
      filteredMap.size mustEqual 2
      filteredMap.get(-1).isEmpty mustEqual true
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
      val ammobox          = AmmoBox(GlobalDefinitions.armor_canister)
      ammobox.GUID = PlanetSideGUID(10)
      harasser_vehicle.Inventory += 30 -> ammobox

      harasser_vehicle.Slot(30).Equipment.get.GUID mustEqual PlanetSideGUID(10)
    }

    "find its mounted weapons by GUID" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      harasser_vehicle.Weapons(2).Equipment.get.GUID = PlanetSideGUID(10)

      harasser_vehicle.Find(PlanetSideGUID(10)).contains(2)
    }

    "find items in its trunk by GUID" in {
      val harasser_vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
      val ammobox          = AmmoBox(GlobalDefinitions.armor_canister)
      ammobox.GUID = PlanetSideGUID(10)
      harasser_vehicle.Inventory += 30 -> ammobox

      harasser_vehicle.Find(PlanetSideGUID(10)).contains(30)
    }
  }
}

class VehicleControlPrepareForDeletionTest extends ActorTest {
  val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  val vehicleProbe = new TestProbe(system)
  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
    VehicleEvents = vehicleProbe.ref
  }

  vehicle.GUID = PlanetSideGUID(1)
  expectNoMessage(200 milliseconds)

  "VehicleControl" should {
    "submit for unregistering when marked for deconstruction" in {
      vehicle.Actor ! Vehicle.Deconstruct()
      vehicleProbe.expectNoMessage(5 seconds)
    }
  }
}

class VehicleControlPrepareForDeletionPassengerTest extends ActorTest {
  val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  val vehicleProbe = new TestProbe(system)
  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
    VehicleEvents = vehicleProbe.ref
  }
  val player1 = Player(VehicleTest.avatar1)

  vehicle.GUID = PlanetSideGUID(1)
  player1.GUID = PlanetSideGUID(2)
  vehicle.Seats(1).Occupant = player1 //passenger seat
  player1.VehicleSeated = vehicle.GUID
  expectNoMessage(200 milliseconds)

  "VehicleControl" should {
    "kick all players when marked for deconstruction" in {
      vehicle.Actor ! Vehicle.Deconstruct()

      val vehicle_msg = vehicleProbe.receiveN(1, 500 milliseconds)
      assert(
        vehicle_msg.head match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.KickPassenger(PlanetSideGUID(2), 4, false, PlanetSideGUID(1))
              ) =>
            true
          case _ => false
        }
      )
      assert(player1.VehicleSeated.isEmpty)
      assert(vehicle.Seats(1).Occupant.isEmpty)
    }
  }
}

class VehicleControlPrepareForDeletionMountedInTest extends FreedContextActorTest {
  ServiceManager.boot
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    GUID(guid)

    override def SetupNumberPools(): Unit = {}
  }
  zone.actor = system.spawn(ZoneActor(zone), "test-zone-actor")
  // crappy workaround but without it the zone doesn't get initialized in time
  expectNoMessage(400 milliseconds)

  val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test-cargo")
  vehicle.Zone = zone
  val lodestar = Vehicle(GlobalDefinitions.lodestar)
  lodestar.Faction = PlanetSideEmpire.TR
  val player1 = Player(VehicleTest.avatar1) //name="test1"
  val player2 = Player(VehicleTest.avatar2) //name="test2"

  guid.register(vehicle, 1)
  guid.register(lodestar, 2)
  player1.GUID = PlanetSideGUID(3)
  var utilityId = 10
  lodestar.Utilities.values.foreach { util =>
    util().GUID = PlanetSideGUID(utilityId)
    utilityId += 1
  }
  vehicle.Seats(1).Occupant = player1 //passenger seat
  player1.VehicleSeated = vehicle.GUID
  lodestar.Seats(0).Occupant = player2
  player2.VehicleSeated = lodestar.GUID
  lodestar.CargoHolds(1).Occupant = vehicle
  vehicle.MountedIn = lodestar.GUID

  val vehicleProbe = new TestProbe(system)
  zone.VehicleEvents = vehicleProbe.ref
  zone.Transport ! Zone.Vehicle.Spawn(lodestar) //can not fake this
  expectNoMessage(200 milliseconds)

  "VehicleControl" should {
    "if mounted as cargo, self-eject when marked for deconstruction" in {
      vehicle.Actor ! Vehicle.Deconstruct()

      val vehicle_msg = vehicleProbe.receiveN(6, 500 milliseconds)
      //dismounting as cargo messages
      assert(
        vehicle_msg.head match {
          case VehicleServiceMessage(
                _,
                VehicleAction.SendResponse(_, PlanetsideAttributeMessage(PlanetSideGUID(1), 0, _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        vehicle_msg(1) match {
          case VehicleServiceMessage(
                _,
                VehicleAction.SendResponse(_, PlanetsideAttributeMessage(PlanetSideGUID(1), 68, _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        vehicle_msg(2) match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.SendResponse(
                  _,
                  CargoMountPointStatusMessage(PlanetSideGUID(2), _, PlanetSideGUID(1), _, 1, CargoStatus.InProgress, 0)
                )
              ) =>
            true
          case _ => false
        }
      )
      assert(
        vehicle_msg(3) match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.SendResponse(_, ObjectDetachMessage(PlanetSideGUID(2), PlanetSideGUID(1), _, _, _, _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        vehicle_msg(4) match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.SendResponse(
                  _,
                  CargoMountPointStatusMessage(PlanetSideGUID(2), _, _, PlanetSideGUID(1), 1, CargoStatus.Empty, 0)
                )
              ) =>
            true
          case _ => false
        }
      )
      //dismounting as cargo messages
      //TODO: does not actually kick out the cargo, but instigates the process
      assert(
        vehicle_msg(5) match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.KickPassenger(PlanetSideGUID(3), 4, false, PlanetSideGUID(1))
              ) =>
            true
          case _ => false
        }
      )
      assert(player1.VehicleSeated.isEmpty)
      assert(vehicle.Seats(1).Occupant.isEmpty)
    }
  }
}

class VehicleControlPrepareForDeletionMountedCargoTest extends FreedContextActorTest {
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  ServiceManager.boot
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    GUID(guid)

    override def SetupNumberPools(): Unit = {}
  }
  zone.actor = system.spawn(ZoneActor(zone), "test-zone-actor")
  // crappy workaround but without it the zone doesn't get initialized in time
  expectNoMessage(200 milliseconds)

  val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Zone = zone
  val cargoProbe = new TestProbe(system)
  vehicle.Actor = cargoProbe.ref
  val lodestar = Vehicle(GlobalDefinitions.lodestar)
  lodestar.Faction = PlanetSideEmpire.TR
  val player1 = Player(VehicleTest.avatar1) //name="test1"
  val player2 = Player(VehicleTest.avatar2) //name="test2"

  guid.register(vehicle, 1)
  guid.register(lodestar, 2)
  player1.GUID = PlanetSideGUID(3)
  player2.GUID = PlanetSideGUID(4)
  var utilityId = 10
  lodestar.Utilities.values.foreach { util =>
    util().GUID = PlanetSideGUID(utilityId)
    utilityId += 1
  }
  vehicle.Seats(1).Occupant = player1 //passenger seat
  player1.VehicleSeated = vehicle.GUID
  lodestar.Seats(0).Occupant = player2
  player2.VehicleSeated = lodestar.GUID
  lodestar.CargoHolds(1).Occupant = vehicle
  vehicle.MountedIn = lodestar.GUID

  val vehicleProbe = new TestProbe(system)
  zone.VehicleEvents = vehicleProbe.ref
  zone.Transport ! Zone.Vehicle.Spawn(lodestar) //can not fake this
  expectNoMessage(200 milliseconds)

  "VehicleControl" should {
    "if with mounted cargo, eject it when marked for deconstruction" in {
      lodestar.Actor ! Vehicle.Deconstruct()

      val vehicle_msg = vehicleProbe.receiveN(6, 500 milliseconds)
      assert(
        vehicle_msg.head match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.KickPassenger(PlanetSideGUID(4), 4, false, PlanetSideGUID(2))
              ) =>
            true
          case _ => false
        }
      )
      assert(player2.VehicleSeated.isEmpty)
      assert(lodestar.Seats(0).Occupant.isEmpty)
      //cargo dismounting messages
      assert(
        vehicle_msg(1) match {
          case VehicleServiceMessage(
                _,
                VehicleAction.SendResponse(_, PlanetsideAttributeMessage(PlanetSideGUID(1), 0, _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        vehicle_msg(2) match {
          case VehicleServiceMessage(
                _,
                VehicleAction.SendResponse(_, PlanetsideAttributeMessage(PlanetSideGUID(1), 68, _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        vehicle_msg(3) match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.SendResponse(
                  _,
                  CargoMountPointStatusMessage(PlanetSideGUID(2), _, PlanetSideGUID(1), _, 1, CargoStatus.InProgress, 0)
                )
              ) =>
            true
          case _ => false
        }
      )
      assert(
        vehicle_msg(4) match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.SendResponse(_, ObjectDetachMessage(PlanetSideGUID(2), PlanetSideGUID(1), _, _, _, _))
              ) =>
            true
          case _ => false
        }
      )
      assert(
        vehicle_msg(5) match {
          case VehicleServiceMessage(
                "test",
                VehicleAction.SendResponse(
                  _,
                  CargoMountPointStatusMessage(PlanetSideGUID(2), _, _, PlanetSideGUID(1), 1, CargoStatus.Empty, 0)
                )
              ) =>
            true
          case _ => false
        }
      )
    }
  }
}

class VehicleControlMountingBlockedExosuitTest extends ActorTest {
  val probe = new TestProbe(system)
  def checkCanNotMount(): Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanNotMount])
      case _ =>
        assert(false)
    }
  }

  def checkCanMount(): Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
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
  def checkCanNotMount(): Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanNotMount])
      case _ =>
        assert(false)
    }
  }

  def checkCanMount(): Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
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

      vehicle.PermissionGroup(2, 3)                                 //passenger group -> empire
      vehicle.Actor.tell(Mountable.TryMount(player1, 3), probe.ref) //passenger seat
      checkCanMount()
      vehicle.PermissionGroup(2, 0)                                 //passenger group -> locked
      vehicle.Actor.tell(Mountable.TryMount(player2, 4), probe.ref) //passenger seat
      checkCanNotMount()
    }
  }
}

class VehicleControlMountingDriverSeatTest extends ActorTest {
  val probe = new TestProbe(system)
  def checkCanMount(): Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
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
  def checkCanNotMount(): Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanNotMount])
      case _ =>
        assert(false)
    }
  }

  def checkCanMount(): Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
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
  def checkCanMount(): Unit = {
    val reply = probe.receiveOne(Duration.create(100, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
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
      vehicle.PermissionGroup(0, 3)                                        //passenger group -> empire
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
  val probe   = new TestProbe(system)
  val vehicle = Vehicle(GlobalDefinitions.fury)
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test"), 0) {
    VehicleEvents = probe.ref
  }

  "charge vehicle shields" in {
    assert(vehicle.Shields == 0)
    assert(!vehicle.History.exists({ p => p.isInstanceOf[VehicleShieldCharge] }))

    vehicle.Actor ! Vehicle.ChargeShields(15)
    val msg = probe.receiveOne(500 milliseconds)
    assert(msg match {
      case VehicleServiceMessage(_, VehicleAction.PlanetsideAttribute(_, PlanetSideGUID(10), 68, 15)) => true
      case _                                                                                          => false
    })
    assert(vehicle.Shields == 15)
    assert(vehicle.History.exists({ p => p.isInstanceOf[VehicleShieldCharge] }))
  }
}

class VehicleControlShieldsNotChargingVehicleDeadTest extends ActorTest {
  val probe   = new TestProbe(system)
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
    assert(!vehicle.History.exists({ p => p.isInstanceOf[VehicleShieldCharge] }))
    vehicle.Actor.tell(Vehicle.ChargeShields(15), probe.ref)

    probe.expectNoMessage(1 seconds)
    assert(vehicle.Shields == 0)
    assert(!vehicle.History.exists({ p => p.isInstanceOf[VehicleShieldCharge] }))
  }
}

class VehicleControlShieldsNotChargingVehicleShieldsFullTest extends ActorTest {
  val probe   = new TestProbe(system)
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
    assert(!vehicle.History.exists({ p => p.isInstanceOf[VehicleShieldCharge] }))
    vehicle.Actor ! Vehicle.ChargeShields(15)

    probe.expectNoMessage(1 seconds)
    assert(!vehicle.History.exists({ p => p.isInstanceOf[VehicleShieldCharge] }))
  }
}

class VehicleControlShieldsNotChargingTooEarlyTest extends ActorTest {
  val probe   = new TestProbe(system)
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
      case _                                                                                          => false
    })
    assert(vehicle.Shields == 15)

    vehicle.Actor ! Vehicle.ChargeShields(15)
    probe.expectNoMessage(200 milliseconds)
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
//  val p_source = PlayerSource( Player(Avatar(0, "TestTarget", PlanetSideEmpire.NC, CharacterGender.Female, 1, CharacterVoice.Mute)) )
//  val projectile = Projectile(beamer_wep.Projectile, GlobalDefinitions.beamer, beamer_wep.FireMode, p_source, GlobalDefinitions.beamer.ObjectId, Vector3.Zero, Vector3.Zero)
//  val fury_dm = Vehicle(GlobalDefinitions.fury).DamageModel
//  val obj = ResolvedProjectile(projectile, p_source, fury_dm, Vector3(1.2f, 3.4f, 5.6f))
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
//    probe.expectNoMessage(200 milliseconds)
//    assert(vehicle.Shields == 0)
//  }
//}

object VehicleTest {

  import net.psforever.objects.avatar.Avatar
  import net.psforever.types.{CharacterGender, PlanetSideEmpire}

  val avatar1 = Avatar(0, "test1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
  val avatar2 = Avatar(1, "test2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
}
