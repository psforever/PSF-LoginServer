// Copyright (c) 2020 PSForever
package objects

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.{ActorRef, Props}
import akka.actor.typed.scaladsl.adapter._
import akka.testkit.TestProbe
import base.{ActorTest, FreedContextActorTest}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.{Avatar, PlayerControl}
import net.psforever.objects.ce.DeployedItem
import net.psforever.objects._
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.environment.interaction.{InteractingWithEnvironment, RespondsToZoneEnvironment}
import net.psforever.objects.serverobject.environment.interaction.common.Watery.OxygenStateTarget
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.sourcing.VehicleSource
import net.psforever.objects.vehicles.VehicleLockState
import net.psforever.objects.vehicles.control.VehicleControl
import net.psforever.objects.vehicles.interaction.WithWater
import net.psforever.objects.vital.{ShieldCharge, SpawningActivity, Vitality}
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.packet.game._
import net.psforever.services.ServiceManager
import net.psforever.services.vehicle.{VehicleAction, VehicleServiceMessage}
import net.psforever.types._

import scala.concurrent.duration._

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
  vehicle.Seats(1).mount(player1) //passenger mount
  player1.VehicleSeated = vehicle.GUID
  expectNoMessage(200 milliseconds)

  "VehicleControl" should {
    "kick all players when marked for deconstruction" in {
      vehicle.Actor ! Vehicle.Deconstruct()

      val vehicle_msg = vehicleProbe.receiveN(1, 500 milliseconds)
      vehicle_msg.head match {
        case VehicleServiceMessage("test", VehicleAction.KickPassenger(PlanetSideGUID(2), 4, true, PlanetSideGUID(1))) => ;
        case _ =>
          assert(false, s"VehicleControlPrepareForDeletionPassengerTest: ${vehicle_msg.head}")
      }
      assert(player1.VehicleSeated.isEmpty)
      assert(vehicle.Seats(1).occupant.isEmpty)
    }
  }
}
//todo figure out why this test never passes tomorrow
//class VehicleControlPrepareForDeletionMountedInTest extends FreedContextActorTest {
//  ServiceManager.boot
//  val guid = new NumberPoolHub(new MaxNumberSource(10))
//  val zone = new Zone("test", new ZoneMap("test"), 0) {
//    GUID(guid)
//
//    override def SetupNumberPools(): Unit = {}
//  }
//  zone.actor = system.spawn(ZoneActor(zone), "test-zone-actor")
//  // crappy workaround but without it the zone doesn't get initialized in time
//  expectNoMessage(400 milliseconds)
//
//  val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
//  vehicle.Faction = PlanetSideEmpire.TR
//  vehicle.Zone = zone
//  val lodestar = Vehicle(GlobalDefinitions.lodestar)
//  lodestar.Faction = PlanetSideEmpire.TR
//  lodestar.Zone = zone
//  val player1 = Player(VehicleTest.avatar1) //name="test1"
//  val player2 = Player(VehicleTest.avatar2) //name="test2"
//
//  guid.register(vehicle, 1)
//  guid.register(lodestar, 2)
//  guid.register(player1, 3)
//  guid.register(player2, 4)
//  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test-cargo")
//  lodestar.Actor = system.actorOf(Props(classOf[CargoCarrierControl], lodestar), "vehicle-test-carrier")
//  var utilityId = 5
//  lodestar.Utilities.values.foreach { util =>
//    guid.register(util(), utilityId)
//    utilityId += 1
//  }
//  vehicle.Seats(1).mount(player1) //passenger mount
//  player1.VehicleSeated = vehicle.GUID
//  lodestar.Seats(0).mount(player2)
//  player2.VehicleSeated = lodestar.GUID
//  lodestar.CargoHolds(1).mount(vehicle)
//  vehicle.MountedIn = lodestar.GUID
//
//  val vehicleProbe = new TestProbe(system)
//  zone.VehicleEvents = vehicleProbe.ref
//  zone.Transport ! Zone.Vehicle.Spawn(lodestar) //can not fake this
//
//  "VehicleControl" should {
//    "self-eject when marked for deconstruction if mounted as cargo" in {
//      assert(player1.VehicleSeated.nonEmpty)
//      assert(vehicle.Seats(1).occupant.nonEmpty)
//      assert(vehicle.MountedIn.nonEmpty)
//      assert(lodestar.CargoHolds(1).isOccupied)
//      vehicle.Actor ! Vehicle.Deconstruct()
//
//      val vehicle_msg = vehicleProbe.receiveN(6, 1 minute)
//      //dismounting as cargo messages
//      vehicle_msg.head match {
//        case VehicleServiceMessage("test", VehicleAction.KickPassenger(PlanetSideGUID(3), 4, true, PlanetSideGUID(1))) => ;
//        case _ =>
//          assert(false, s"VehicleControlPrepareForDeletionMountedInTest-1: ${vehicle_msg.head}")
//      }
//      vehicle_msg(1) match {
//        case VehicleServiceMessage(_, VehicleAction.SendResponse(_, PlanetsideAttributeMessage(PlanetSideGUID(1), 0, _))) => ;
//        case _ =>
//          assert(false, s"VehicleControlPrepareForDeletionMountedInTest-2: ${vehicle_msg(1)}")
//      }
//      vehicle_msg(2) match {
//        case VehicleServiceMessage(_, VehicleAction.SendResponse(_, PlanetsideAttributeMessage(PlanetSideGUID(1), 68, _))) => ;
//        case _ =>
//          assert(false, s"VehicleControlPrepareForDeletionMountedInTest-3: ${vehicle_msg(2)}")
//      }
//      vehicle_msg(3) match {
//        case VehicleServiceMessage("test", VehicleAction.SendResponse(_, CargoMountPointStatusMessage(PlanetSideGUID(2), _, PlanetSideGUID(1), _, 1, CargoStatus.InProgress, 0))) => ;
//        case _ =>
//          assert(false, s"VehicleControlPrepareForDeletionMountedInTest-4: ${vehicle_msg(3)}")
//      }
//      vehicle_msg(4) match {
//        case VehicleServiceMessage("test", VehicleAction.SendResponse(_, ObjectDetachMessage(PlanetSideGUID(2), PlanetSideGUID(1), _, _, _, _))) => ;
//        case _ =>
//          assert(false, s"VehicleControlPrepareForDeletionMountedInTest-5: ${vehicle_msg(4)}")
//      }
//      vehicle_msg(5) match {
//        case VehicleServiceMessage("test", VehicleAction.SendResponse(_, CargoMountPointStatusMessage(PlanetSideGUID(2), _, _, PlanetSideGUID(1), 1, CargoStatus.Empty, 0))) => ;
//        case _ =>
//          assert(false, s"VehicleControlPrepareForDeletionMountedInTest-6: ${vehicle_msg(5)}")
//      }
//      assert(player1.VehicleSeated.isEmpty)
//      assert(vehicle.Seats(1).occupant.isEmpty)
//      assert(vehicle.MountedIn.isEmpty)
//      assert(!lodestar.CargoHolds(1).isOccupied)
//    }
//  }
//}

class VehicleControlPrepareForDeletionMountedCargoTest extends FreedContextActorTest {
  val eventsProbe = new TestProbe(system)
  val cargoProbe = new TestProbe(system)
  val guid = new NumberPoolHub(new MaxNumberSource(10))
  ServiceManager.boot
  val zone = new Zone("test", new ZoneMap("test"), 0) {
    GUID(guid)

    override def SetupNumberPools(): Unit = {}
    override def AvatarEvents = eventsProbe.ref
    override def LocalEvents = eventsProbe.ref
    override def VehicleEvents = eventsProbe.ref
  }
  zone.actor = system.spawn(ZoneActor(zone), "test-zone-actor")
  // crappy workaround but without it the zone doesn't get initialized in time
  expectNoMessage(200 milliseconds)

  val vehicle = Vehicle(GlobalDefinitions.two_man_assault_buggy)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Zone = zone
  vehicle.Actor = cargoProbe.ref
  val lodestar = Vehicle(GlobalDefinitions.lodestar)
  lodestar.Faction = PlanetSideEmpire.TR
  lodestar.Zone = zone
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
  vehicle.Seats(1).mount(player1) //passenger mount
  player1.VehicleSeated = vehicle.GUID
  lodestar.Seats(0).mount(player2)
  player2.VehicleSeated = lodestar.GUID
  lodestar.CargoHolds(1).mount(vehicle)
  vehicle.MountedIn = lodestar.GUID
  lodestar.Definition.Initialize(lodestar, context)

  "VehicleControl" should {
    "if with mounted cargo, eject it when marked for deconstruction" in {
      lodestar.Actor ! Vehicle.Deconstruct()

      val vehicleMsgs = eventsProbe.receiveN(6, 10.seconds)
      val cargoMsgs = cargoProbe.receiveN(1, 1.seconds)
      vehicleMsgs.head match {
        case VehicleServiceMessage("test", VehicleAction.KickPassenger(PlanetSideGUID(4), 4, true, PlanetSideGUID(2))) => ()
        case _ =>
          assert(false, s"VehicleControlPrepareForDeletionMountedCargoTest-1: ${vehicleMsgs.head}")
      }
      assert(player2.VehicleSeated.isEmpty)
      assert(lodestar.Seats(0).occupant.isEmpty)
      //cargo dismounting messages
      vehicleMsgs(1) match {
        case VehicleServiceMessage(_, VehicleAction.SendResponse(_, PlanetsideAttributeMessage(PlanetSideGUID(1), 0, _))) => ()
        case _ =>
          assert(false, s"VehicleControlPrepareForDeletionMountedCargoTest-2: ${vehicleMsgs(1)}")
      }
      vehicleMsgs(2) match {
        case VehicleServiceMessage(_, VehicleAction.SendResponse(_, PlanetsideAttributeMessage(PlanetSideGUID(1), 68, _))) => ()
        case _ =>
          assert(false, s"VehicleControlPrepareForDeletionMountedCargoTest-3: ${vehicleMsgs(2)}")
      }
      vehicleMsgs(3) match {
        case VehicleServiceMessage("test", VehicleAction.SendResponse(_, CargoMountPointStatusMessage(PlanetSideGUID(2), _, PlanetSideGUID(1), _, 1, CargoStatus.InProgress, 0))) => ;
        case _ =>
          assert(false, s"VehicleControlPrepareForDeletionMountedCargoTest-4: ${vehicleMsgs(3)}")
      }
      vehicleMsgs(4) match {
        case VehicleServiceMessage("test", VehicleAction.SendResponse(_, ObjectDetachMessage(PlanetSideGUID(2), PlanetSideGUID(1), _, _, _, _))) => ()
        case _ =>
          assert(false, s"VehicleControlPrepareForDeletionMountedCargoTest-5: ${vehicleMsgs(4)}")
      }
      vehicleMsgs(5) match {
        case VehicleServiceMessage("test", VehicleAction.SendResponse(_, CargoMountPointStatusMessage(PlanetSideGUID(2), _, _, PlanetSideGUID(1), 1, CargoStatus.Empty, 0))) => ()
        case _ =>
          assert(false, s"VehicleControlPrepareForDeletionMountedCargoTest-6: ${vehicleMsgs(5)}")
      }
      cargoMsgs.head match {
        case Vehicle.Deconstruct(_) => ()
        case _ =>
          assert(false, s"VehicleControlPrepareForDeletionMountedCargoTest-7: ${cargoMsgs.head}")
      }
    }
  }
}

class VehicleControlMountingBlockedExosuitTest extends ActorTest {
  val catchallProbe = new TestProbe(system)
  val catchall = catchallProbe.ref
  val zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools(): Unit = {}
    override def AvatarEvents: ActorRef = catchall
    override def LocalEvents: ActorRef = catchall
    override def VehicleEvents: ActorRef = catchall
    override def Activity: ActorRef = catchall
    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Zone = zone
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")

  val vehicle2 = Vehicle(GlobalDefinitions.lightning)
  vehicle2.Faction = PlanetSideEmpire.TR
  vehicle2.GUID = PlanetSideGUID(11)
  vehicle2.Zone = zone
  vehicle2.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle2), "vehicle2-test")

  val player1 = Player(VehicleTest.avatar1)
  player1.ExoSuit = ExoSuitType.Reinforced
  player1.GUID = PlanetSideGUID(1)
  player1.Zone = zone
  val player2 = Player(VehicleTest.avatar1)
  player2.ExoSuit = ExoSuitType.MAX
  player2.GUID = PlanetSideGUID(2)
  player2.Zone = zone
  val player3 = Player(VehicleTest.avatar1)
  player3.ExoSuit = ExoSuitType.Agile
  player3.GUID = PlanetSideGUID(3)
  player3.Zone = zone

  "Vehicle Control" should {
    "block players from sitting if their exo-suit is not allowed by the mount - apc_tr" in {
      val probe = new TestProbe(system)
      // disallow
      vehicle.Actor.tell(Mountable.TryMount(player2, 1), probe.ref) //MAX in non-Max mount
      VehicleControlTest.checkCanNotMount(probe, "MAX in non-Max mount 1")
      vehicle.Actor.tell(Mountable.TryMount(player2, 2), probe.ref) //MAX in non-MAX mount
      VehicleControlTest.checkCanNotMount(probe, "MAX in non-MAX mount 2")
      vehicle.Actor.tell(Mountable.TryMount(player1, 11), probe.ref) //Reinforced in MAX-only mount
      VehicleControlTest.checkCanNotMount(probe, "Reinforced in MAX-only mount")
      vehicle.Actor.tell(Mountable.TryMount(player3, 11), probe.ref) //Agile in MAX-only mount
      VehicleControlTest.checkCanNotMount(probe, "Agile in MAX-only mount")

      //allow
      vehicle.Actor.tell(Mountable.TryMount(player1, 1), probe.ref) // Reinforced in driver mount allowing all except MAX
      VehicleControlTest.checkCanMount(probe, "Reinforced in driver mount allowing all except MAX")
      // Reset to allow further driver mount mounting tests
      vehicle.Actor.tell(Mountable.TryDismount(player1, 0), probe.ref)
      probe.receiveOne(500 milliseconds) //discard
      vehicle.OwnerGuid = None //ensure
      //vehicle.OwnerName = None //ensure
      vehicle.Actor.tell(Mountable.TryMount(player3, 1), probe.ref) // Agile in driver mount allowing all except MAX
      VehicleControlTest.checkCanMount(probe, "Agile in driver mount allowing all except MAX")
      vehicle.Actor.tell(Mountable.TryMount(player1, 3), probe.ref) // Reinforced in passenger mount allowing all except MAX
      VehicleControlTest.checkCanMount(probe, "Reinforced in passenger mount allowing all except MAX")
      vehicle.Actor.tell(Mountable.TryMount(player2, 11), probe.ref) // MAX in MAX-only mount
      VehicleControlTest.checkCanMount(probe, "MAX in MAX-only mount")
    }
  }
}

class VehicleControlMountingBlockedSeatPermissionTest extends ActorTest {
  val probe = new TestProbe(system)
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools(): Unit = {}
    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }

  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)
  val player2 = Player(VehicleTest.avatar1)
  player2.GUID = PlanetSideGUID(2)

  "Vehicle Control" should {
    //11 June 2018: Group is not supported yet so do not bother testing it
    "block players from sitting if the mount does not allow it" in {

      vehicle.PermissionGroup(2, 3)                  //passenger group -> empire
      vehicle.Actor.tell(Mountable.TryMount(player1, 4), probe.ref) //passenger mount
      VehicleControlTest.checkCanMount(probe, "")
      vehicle.PermissionGroup(2, 0)                  //passenger group -> locked
      vehicle.Actor.tell(Mountable.TryMount(player2, 5), probe.ref) //passenger mount
      VehicleControlTest.checkCanNotMount(probe, "")
    }
  }
}

class VehicleControlMountingDriverSeatTest extends ActorTest {
  val probe = new TestProbe(system)
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools(): Unit = {}
    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }
  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)

  "Vehicle Control" should {
    "allow players to sit in the driver mount, even if it is locked, if the vehicle is unowned" in {
      assert(vehicle.PermissionGroup(0).contains(VehicleLockState.Locked)) //driver group -> locked
      assert(vehicle.Seats(0).occupant.isEmpty)
      assert(vehicle.OwnerGuid.isEmpty)
      vehicle.Actor.tell(Mountable.TryMount(player1, 1), probe.ref)
      VehicleControlTest.checkCanMount(probe, "")
      assert(vehicle.Seats(0).occupant.nonEmpty)
    }
  }
}

class VehicleControlMountingOwnedLockedDriverSeatTest extends ActorTest {
  val probe = new TestProbe(system)
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools(): Unit = {}
    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }

  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)
  val player2 = Player(VehicleTest.avatar1.copy(basic = VehicleTest.avatar1.basic.copy(faction = PlanetSideEmpire.NC)))
  player2.GUID = PlanetSideGUID(2)

  "Vehicle Control" should {
    "block players that are not the current owner from sitting in the driver mount (locked)" in {
      assert(vehicle.PermissionGroup(0).contains(VehicleLockState.Locked)) //driver group -> locked
      assert(vehicle.Seats(0).occupant.isEmpty)
      vehicle.OwnerGuid = player1.GUID

      vehicle.Actor.tell(Mountable.TryMount(player1, 1), probe.ref)
      VehicleControlTest.checkCanMount(probe, "")
      assert(vehicle.Seats(0).occupant.nonEmpty)
      vehicle.Actor.tell(Mountable.TryDismount(player1, 0), probe.ref)
      probe.receiveOne(Duration.create(100, "ms")) //discard
      assert(vehicle.Seats(0).occupant.isEmpty)

      vehicle.Actor.tell(Mountable.TryMount(player2, 1), probe.ref)
      VehicleControlTest.checkCanNotMount(probe, "")
      assert(vehicle.Seats(0).occupant.isEmpty)
    }
  }
}

class VehicleControlMountingOwnedUnlockedDriverSeatTest extends ActorTest {
  val probe = new TestProbe(system)
  val vehicle = Vehicle(GlobalDefinitions.apc_tr)
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.GUID = PlanetSideGUID(10)
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-test")
  vehicle.Zone = new Zone("test", new ZoneMap("test-map"), 0) {
    override def SetupNumberPools(): Unit = {}
    this.actor = new TestProbe(system).ref.toTyped[ZoneActor.Command]
  }

  val player1 = Player(VehicleTest.avatar1)
  player1.GUID = PlanetSideGUID(1)
  val player2 = Player(VehicleTest.avatar1)
  player2.GUID = PlanetSideGUID(2)

  "Vehicle Control" should {
    "allow players that are not the current owner to sit in the driver mount (empire)" in {
      vehicle.PermissionGroup(0, 3)                                        //passenger group -> empire
      assert(vehicle.PermissionGroup(0).contains(VehicleLockState.Empire)) //driver group -> empire
      assert(vehicle.Seats(0).occupant.isEmpty)
      vehicle.OwnerGuid = player1.GUID //owner set

      vehicle.Actor.tell(Mountable.TryMount(player1, 1), probe.ref)
      VehicleControlTest.checkCanMount(probe, "")
      assert(vehicle.Seats(0).occupant.nonEmpty)
      vehicle.Actor.tell(Mountable.TryDismount(player1, 0), probe.ref)
      probe.receiveOne(Duration.create(100, "ms")) //discard
      assert(vehicle.Seats(0).occupant.isEmpty)

      vehicle.Actor.tell(Mountable.TryMount(player2, 1), probe.ref)
      VehicleControlTest.checkCanMount(probe, "")
      assert(vehicle.Seats(0).occupant.nonEmpty)
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
    assert(!vehicle.History.exists({ p => p.isInstanceOf[ShieldCharge] }))

    vehicle.Actor ! CommonMessages.ChargeShields(15, None)
    val msg = probe.receiveOne(500 milliseconds)
    assert(msg match {
      case VehicleServiceMessage(_, VehicleAction.PlanetsideAttribute(_, PlanetSideGUID(10), 68, 15)) => true
      case _                                                                                          => false
    })
    assert(vehicle.Shields == 15)
    assert(vehicle.History.exists({ p => p.isInstanceOf[ShieldCharge] }))
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
    assert(!vehicle.History.exists({ p => p.isInstanceOf[ShieldCharge] }))
    vehicle.Actor.tell(CommonMessages.ChargeShields(15, None), probe.ref)

    probe.expectNoMessage(1 seconds)
    assert(vehicle.Shields == 0)
    assert(!vehicle.History.exists({ p => p.isInstanceOf[ShieldCharge] }))
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
    assert(!vehicle.History.exists({ p => p.isInstanceOf[ShieldCharge] }))
    vehicle.Actor ! CommonMessages.ChargeShields(15, None)

    probe.expectNoMessage(1 seconds)
    assert(!vehicle.History.exists({ p => p.isInstanceOf[ShieldCharge] }))
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

    vehicle.Actor ! CommonMessages.ChargeShields(15, None)
    val msg = probe.receiveOne(200 milliseconds)
    //assert(msg.isInstanceOf[Vehicle.UpdateShieldsCharge])
    assert(msg match {
      case VehicleServiceMessage(_, VehicleAction.PlanetsideAttribute(_, PlanetSideGUID(10), 68, 15)) => true
      case _                                                                                          => false
    })
    assert(vehicle.Shields == 15)

    vehicle.Actor ! CommonMessages.ChargeShields(15, None)
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
//  val p_source = PlayerSource( Player(Avatar(0, "TestTarget", PlanetSideEmpire.NC, CharacterSex.Female, 1, CharacterVoice.Mute)) )
//  val projectile = Projectile(beamer_wep.Projectile, GlobalDefinitions.beamer, beamer_wep.FireMode, p_source, GlobalDefinitions.beamer.ObjectId, Vector3.Zero, Vector3.Zero)
//  val fury_dm = Vehicle(GlobalDefinitions.fury).DamageModel
//  val obj = DamageInteraction(p_source, ProjectileReason(DamageResolution.Hit, projectile, fury_dm), Vector3(1.2f, 3.4f, 5.6f))
//
//  "not charge vehicle shields if recently damaged" in {
//    assert(vehicle.Shields == 0)
//    vehicle.Actor.tell(Vitality.Damage({case v : Vehicle => v.History(obj); obj }), probe.ref)
//
//    val msg = probe.receiveOne(200 milliseconds)
//    assert(msg.isInstanceOf[Vitality.DamageResolution])
//    assert(vehicle.Shields == 0)
//    vehicle.Actor.tell(CommonMessages.ChargeShields(15, None), probe.ref)
//
//    probe.expectNoMessage(200 milliseconds)
//    assert(vehicle.Shields == 0)
//  }
//}

class VehicleControlInteractWithWaterWadingTest extends ActorTest {
  val playerProbe = TestProbe()
  val vehicleProbe = TestProbe()
  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
  val player1 = Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)) //guid=1
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val pool = Pool(EnvironmentAttribute.Water, DeepSquare(5, 2, 2, 0, 0))
  val zone = new Zone(
    id = "test-zone",
    new ZoneMap(name = "test-map") {
      environment = List(pool)
    },
    zoneNumber = 0
  ) {
    override def SetupNumberPools() = {}
    GUID(guid)
    override def LivePlayers = List(player1)
    override def Vehicles = List(vehicle)
  }
  zone.blockMap.addTo(vehicle)
  zone.blockMap.addTo(pool)

  guid.register(player1, 1)
  guid.register(vehicle, 2)
  player1.Zone = zone
  player1.Spawn()
  vehicle.Zone = zone
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Seats(0).mount(player1)
  player1.VehicleSeated = vehicle.GUID
  player1.Actor = playerProbe.ref
  vehicle.Actor = vehicleProbe.ref

  "VehicleControl" should {
    "report when the vehicle starts treading water" in {
      vehicle.Position = Vector3(1, 1, 6)
      vehicle.zoneInteractions()
      vehicleProbe.expectNoMessage(2.seconds)
      playerProbe.expectNoMessage()

      vehicle.Position = Vector3(1, 1, 4f)
      vehicle.zoneInteractions()
      val vehicleMsgs = vehicleProbe.receiveN(1, 5.seconds)
      vehicleMsgs.head match {
        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
        case _ =>
          assert(false, "")
      }
      playerProbe.expectNoMessage()
    }
  }
}

class VehicleControlInteractWithWaterStartDrowningTest extends ActorTest {
  val playerProbe = TestProbe()
  val vehicleProbe = TestProbe()
  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
  val player1 = Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)) //guid=1
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val pool = Pool(EnvironmentAttribute.Water, DeepSquare(5, 2, 2, 0, 0))
  val zone = new Zone(
    id = "test-zone",
    new ZoneMap(name = "test-map") {
      environment = List(pool)
    },
    zoneNumber = 0
  ) {
    override def SetupNumberPools() = {}
    GUID(guid)
    override def LivePlayers = List(player1)
    override def Vehicles = List(vehicle)
  }
  zone.blockMap.addTo(vehicle)
  zone.blockMap.addTo(pool)

  guid.register(player1, 1)
  guid.register(vehicle, 2)
  player1.Zone = zone
  player1.Spawn()
  vehicle.Zone = zone
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Seats(0).mount(player1)
  player1.VehicleSeated = vehicle.GUID
  player1.Actor = playerProbe.ref
  vehicle.Actor = vehicleProbe.ref

  "VehicleControl" should {
    "report when the vehicle starts drowning" in {
      vehicle.Position = Vector3(1, 1, 6)
      vehicle.zoneInteractions()
      vehicleProbe.expectNoMessage(2.seconds)
      playerProbe.expectNoMessage()

      vehicle.Position = Vector3(1, 1, 0f)
      vehicle.zoneInteractions()
      val vehicleMsgs = vehicleProbe.receiveN(3, 5.seconds)
      val playerMsgs = playerProbe.receiveN(1, 1.seconds)
      vehicleMsgs.head match {
        case RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction) => ()
        case _ =>
          assert(false, "")
      }
      vehicleMsgs(1) match {
        case RespondsToZoneEnvironment.Timer(WithWater.WaterAction, _, _, _) => ()
        case _ =>
          assert(false, "")
      }
      vehicleMsgs(2) match {
        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
        case _ =>
          assert(false, "")
      }
      playerMsgs.head match {
        case InteractingWithEnvironment(somePool, Some(OxygenStateTarget(ValidPlanetSideGUID(2), _, OxygenState.Suffocation, 100.0f)))
          if somePool eq pool => ()
        case _ =>
          assert(false, "")
      }
    }
  }
}

//class VehicleControlInteractWithWaterStopDrowningTest extends ActorTest {
//  val playerProbe = TestProbe()
//  val vehicleProbe = TestProbe()
//  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
//  val player1 = Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)) //guid=1
//  val guid = new NumberPoolHub(new MaxNumberSource(15))
//  val pool = Pool(EnvironmentAttribute.Water, DeepSquare(5, 2, 2, 0, 0))
//  val zone = new Zone(
//    id = "test-zone",
//    new ZoneMap(name = "test-map") {
//      environment = List(pool)
//    },
//    zoneNumber = 0
//  ) {
//    override def SetupNumberPools() = {}
//    GUID(guid)
//    override def LivePlayers = List(player1)
//    override def Vehicles = List(vehicle)
//  }
//  zone.blockMap.addTo(vehicle)
//  zone.blockMap.addTo(pool)
//
//  guid.register(player1, 1)
//  guid.register(vehicle, 2)
//  player1.Zone = zone
//  player1.Spawn()
//  vehicle.Zone = zone
//  vehicle.Faction = PlanetSideEmpire.TR
//  vehicle.Seats(0).mount(player1)
//  player1.VehicleSeated = vehicle.GUID
//  player1.Actor = playerProbe.ref
//  vehicle.Actor = vehicleProbe.ref
//
//  "VehicleControl" should {
//    "report when the vehicle stops drowning" in {
//      vehicle.Position = Vector3(1, 1, 6)
//      vehicle.zoneInteractions()
//      vehicleProbe.expectNoMessage(2.seconds)
//      playerProbe.expectNoMessage()
//
//      vehicle.Position = Vector3(1, 1, 0f)
//      vehicle.zoneInteractions()
//      val vehicleMsgs = vehicleProbe.receiveN(3, 5.seconds)
//      val playerMsgs = playerProbe.receiveN(1, 1.seconds)
//      vehicleMsgs.head match {
//        case RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction) => ()
//        case _ =>
//          assert(false, "")
//      }
//      vehicleMsgs(1) match {
//        case RespondsToZoneEnvironment.Timer(WithWater.WaterAction, _, _, _) => ()
//        case _ =>
//          assert(false, "")
//      }
//      vehicleMsgs(2) match {
//        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
//        case _ =>
//          assert(false, "")
//      }
//      playerMsgs.head match {
//        case InteractingWithEnvironment(somePool, Some(OxygenStateTarget(ValidPlanetSideGUID(2), _, OxygenState.Suffocation, 100.0f)))
//          if somePool eq pool => ()
//        case _ =>
//          assert(false, "")
//      }
//
//      //escape drowning
//      vehicle.Position = Vector3(1, 1, 4.7f)
//      vehicle.zoneInteractions()
//      val vehicleMsgs2 = vehicleProbe.receiveN(2, 5.seconds)
//      val playerMsgs2 = playerProbe.receiveN(1, 1.seconds)
//      vehicleMsgs2.head match {
//        case RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction) => ()
//        case _ =>
//          assert(false, "")
//      }
//      vehicleMsgs2(1) match {
//        case RespondsToZoneEnvironment.Timer(WithWater.WaterAction, _, _, _) => ()
//        case _ =>
//          assert(false, "")
//      }
//      playerMsgs2.head match {
//        case EscapeFromEnvironment(somePool, Some(OxygenStateTarget(ValidPlanetSideGUID(2), _, OxygenState.Recovery, _)))
//          if somePool eq pool => ()
//        case _ =>
//          assert(false, "")
//      }
//    }
//  }
//}

class VehicleControlInteractWithWaterStopWadingTest extends ActorTest {
  val playerProbe = TestProbe()
  val vehicleProbe = TestProbe()
  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
  val player1 = Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)) //guid=1
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val pool = Pool(EnvironmentAttribute.Water, DeepSquare(5, 2, 2, 0, 0))
  val zone = new Zone(
    id = "test-zone",
    new ZoneMap(name = "test-map") {
      environment = List(pool)
    },
    zoneNumber = 0
  ) {
    override def SetupNumberPools() = {}
    GUID(guid)
    override def LivePlayers = List(player1)
    override def Vehicles = List(vehicle)
  }
  zone.blockMap.addTo(vehicle)
  zone.blockMap.addTo(pool)

  guid.register(player1, 1)
  guid.register(vehicle, 2)
  player1.Zone = zone
  player1.Spawn()
  vehicle.Zone = zone
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Seats(0).mount(player1)
  player1.VehicleSeated = vehicle.GUID
  player1.Actor = playerProbe.ref
  vehicle.Actor = vehicleProbe.ref

  "VehicleControl" should {
    "report when the vehicle stops wading" in {
      vehicle.Position = Vector3(1, 1, 6)
      vehicle.zoneInteractions()
      vehicleProbe.expectNoMessage(2.seconds)
      playerProbe.expectNoMessage()

      vehicle.Position = Vector3(1, 1, 4f)
      vehicle.zoneInteractions()
      val vehicleMsgs = vehicleProbe.receiveN(1, 5.seconds)
      playerProbe.expectNoMessage()
      vehicleMsgs.head match {
        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
        case _ =>
          assert(false, "")
      }

      //stop wading
      vehicle.Position = Vector3(1, 1, 6f)
      vehicle.zoneInteractions()
      val vehicleMsgs2 = vehicleProbe.receiveN(1, 5.seconds)
      playerProbe.expectNoMessage()
      vehicleMsgs2.head match {
        case RespondsToZoneEnvironment.StopTimer(EnvironmentAttribute.Water) => ()
        case _ =>
          assert(false, "")
      }
    }
  }
}

//class VehicleControlInteractWithWaterFullStopTest extends ActorTest {
//  val playerProbe = TestProbe()
//  val vehicleProbe = TestProbe()
//  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
//  val player1 = Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)) //guid=1
//  val guid = new NumberPoolHub(new MaxNumberSource(15))
//  val pool = Pool(EnvironmentAttribute.Water, DeepSquare(5, 2, 2, 0, 0))
//  val zone = new Zone(
//    id = "test-zone",
//    new ZoneMap(name = "test-map") {
//      environment = List(pool)
//    },
//    zoneNumber = 0
//  ) {
//    override def SetupNumberPools() = {}
//    GUID(guid)
//    override def LivePlayers = List(player1)
//    override def Vehicles = List(vehicle)
//  }
//  zone.blockMap.addTo(vehicle)
//  zone.blockMap.addTo(pool)
//
//  guid.register(player1, 1)
//  guid.register(vehicle, 2)
//  player1.Zone = zone
//  player1.Spawn()
//  vehicle.Zone = zone
//  vehicle.Faction = PlanetSideEmpire.TR
//  vehicle.Seats(0).mount(player1)
//  player1.VehicleSeated = vehicle.GUID
//  player1.Actor = playerProbe.ref
//  vehicle.Actor = vehicleProbe.ref
//
//  "VehicleControl" should {
//    "report when the vehicle stops interacting with water altogether" in {
//      vehicle.Position = Vector3(1, 1, 6)
//      vehicle.zoneInteractions()
//      vehicleProbe.expectNoMessage(2.seconds)
//      playerProbe.expectNoMessage()
//      //wading and drowning
//      vehicle.Position = Vector3(1, 1, 0f)
//      vehicle.zoneInteractions()
//      val vehicleMsgs = vehicleProbe.receiveN(3, 5.seconds)
//      val playerMsgs = playerProbe.receiveN(1, 1.seconds)
//      vehicleMsgs.head match {
//        case RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction) => ()
//        case _ =>
//          assert(false, "")
//      }
//      vehicleMsgs(1) match {
//        case RespondsToZoneEnvironment.Timer(WithWater.WaterAction, _, _, _) => ()
//        case _ =>
//          assert(false, "")
//      }
//      vehicleMsgs(2) match {
//        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
//        case _ =>
//          assert(false, "")
//      }
//      playerMsgs.head match {
//        case InteractingWithEnvironment(somePool, Some(OxygenStateTarget(ValidPlanetSideGUID(2), _, OxygenState.Suffocation, 100.0f)))
//          if somePool eq pool => ()
//        case _ =>
//          assert(false, "")
//      }
//
//      //escape drowning and wading
//      vehicle.Position = Vector3(1, 1, 6f)
//      vehicle.zoneInteractions()
//      val vehicleMsgs2 = vehicleProbe.receiveN(2, 5.seconds)
//      val playerMsgs2 = playerProbe.receiveN(1, 1.seconds)
//      vehicleMsgs2.head match {
//        case RespondsToZoneEnvironment.StopTimer(WithWater.WaterAction) => ()
//        case _ =>
//          assert(false, "")
//      }
//      vehicleMsgs2(1) match {
//        case RespondsToZoneEnvironment.StopTimer(EnvironmentAttribute.Water) => ()
//        case _ =>
//          assert(false, "")
//      }
//      playerMsgs2.head match {
//        case EscapeFromEnvironment(somePool, Some(OxygenStateTarget(ValidPlanetSideGUID(2), _, OxygenState.Recovery, _)))
//          if somePool eq pool => ()
//        case _ =>
//          assert(false, "")
//      }
//    }
//  }
//}

class VehicleControlInteractWithLavaTest extends ActorTest {
  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)) //guid=1
  val avatarProbe = TestProbe()
  val vehicleProbe = TestProbe()
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val pool = Pool(EnvironmentAttribute.Lava, DeepSquare(-1, 10, 10, 0, 0))
  val zone = new Zone(
    id = "test-zone",
    new ZoneMap(name = "test-map") {
      environment = List(pool)
    },
    zoneNumber = 0
  ) {
    actor = ActorTestKit().createTestProbe[ZoneActor.Command]().ref
    override def SetupNumberPools() = {}
    GUID(guid)
    override def LivePlayers = List(player1)
    override def Vehicles = List(vehicle)
    override def AvatarEvents = avatarProbe.ref
    override def VehicleEvents = vehicleProbe.ref
    override def Activity = TestProbe().ref
  }
  zone.blockMap.addTo(vehicle)
  zone.blockMap.addTo(pool)

  guid.register(player1, 1)
  guid.register(vehicle, 2)
  guid.register(player1.avatar.locker, 5)
  player1.Zone = zone
  player1.Spawn()
  vehicle.Zone = zone
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Seats(0).mount(player1)
  player1.VehicleSeated = vehicle.GUID
  val (probe, avatarActor) = PlayerControlTest.DummyAvatar(system)
  player1.Actor = system.actorOf(Props(classOf[PlayerControl], player1, avatarActor), "player1-control")
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-control")

  "VehicleControl" should {
    "take continuous damage if vehicle drives into lava" in {
      assert(vehicle.Health > 0) //alive
      assert(player1.Health == 100) //alive
      vehicle.Position = Vector3(5,5,-3) //right in the pool
      vehicle.zoneInteractions() //trigger

      val msg_burn = vehicleProbe.receiveN(3,1 seconds)
      msg_burn.foreach { msg =>
        assert(
          msg match {
            case VehicleServiceMessage("test-zone", VehicleAction.PlanetsideAttribute(_, PlanetSideGUID(2), 0, _)) => true
            case _ => false
          }
        )
      }
      //etc..
      probe.receiveOne(65 seconds) //wait until player1's implants deinitialize
      assert(vehicle.Health == 0) //ded
      assert(player1.Health == 0) //ded
    }
  }
}

class VehicleControlInteractWithDeathTest extends ActorTest {
  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)) //guid=1
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val pool = Pool(EnvironmentAttribute.Death, DeepSquare(5, 10, 10, 0, 0))
  val zone = new Zone(
    id = "test-zone",
    new ZoneMap(name = "test-map") {
      environment = List(pool)
    },
    zoneNumber = 0
  ) {
    actor = ActorTestKit().createTestProbe[ZoneActor.Command]().ref
    override def SetupNumberPools() = {}
    GUID(guid)
    override def LivePlayers = List(player1)
    override def Vehicles = List(vehicle)
    override def AvatarEvents = TestProbe().ref
    override def VehicleEvents = TestProbe().ref
  }
  zone.blockMap.addTo(vehicle)
  zone.blockMap.addTo(pool)

  guid.register(player1, 1)
  guid.register(vehicle, 2)
  guid.register(player1.avatar.locker, 5)
  player1.Zone = zone
  player1.Spawn()
  vehicle.Zone = zone
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Seats(0).mount(player1)
  player1.VehicleSeated = vehicle.GUID
  val (probe, avatarActor) = PlayerControlTest.DummyAvatar(system)
  player1.Actor = system.actorOf(Props(classOf[PlayerControl], player1, avatarActor), "player1-control")
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-control")
  vehicle.LogActivity(SpawningActivity(VehicleSource(vehicle), 0, None))

  "VehicleControl" should {
    "take continuous damage if vehicle drives into a pool of death" in {
      assert(vehicle.Health > 0) //alive
      assert(player1.Health == 100) //alive
      vehicle.Position = Vector3(5,5,1) //right in the pool
      probe.expectNoMessage(5 seconds)
      vehicle.zoneInteractions() //trigger

      probe.receiveOne(2 seconds)
      assert(vehicle.Health == 0) //ded
      assert(player1.Health == 0) //ded
    }
  }
}

class ApcControlCanChargeCapacitor extends FreedContextActorTest {
  val apc = Vehicle(GlobalDefinitions.apc_tr) //guid=1, weapons not registered

  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val localProbe = TestProbe()
  val vehicleProbe = TestProbe()
  val catchall = TestProbe()
  val zone = new Zone(id = "test-zone", new ZoneMap(name = "test-map"), zoneNumber = 0) {
    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def Vehicles = List(apc)
    override def VehicleEvents = vehicleProbe.ref
    override def LocalEvents = localProbe.ref
    override def AvatarEvents = catchall.ref
    override def Activity = catchall.ref
  }

  guid.register(apc, number = 1)
  apc.Faction = PlanetSideEmpire.VS
  apc.Zone = zone
  //apc.Definition.Initialize(apc, context) //do later ...
  zone.blockMap.addTo(apc)

  val maxCapacitor = apc.Definition.MaxCapacitor

  "ApcControl" should {
    "charge its capacitors when initialized" in {
      assert(apc.Capacitor == 0)
      apc.Definition.Initialize(apc, context)
      do {
        val msg = vehicleProbe.receiveOne(3.seconds)
        msg match {
          case VehicleServiceMessage(_, VehicleAction.PlanetsideAttribute(_, PlanetSideGUID(1), 113, capacitance)) =>
            assert(capacitance > 0)
          case _ =>
            assert(false)
        }
      }
      while(apc.Capacitor < maxCapacitor)
      vehicleProbe.expectNoMessage(5.seconds)
      assert(apc.Capacitor == maxCapacitor)
    }
  }
}

class ApcControlCanEmp extends FreedContextActorTest {
  val apc = Vehicle(GlobalDefinitions.apc_vs) //guid=1, weapons not registered
  val fury = Vehicle(GlobalDefinitions.fury) //guid=2, weapons not registered
  val boomer = Deployables.Make(DeployedItem.boomer)() //guid=3, no trigger
  val boomer2 = Deployables.Make(DeployedItem.boomer)() //guid=4, no trigger

  val guid = new NumberPoolHub(new MaxNumberSource(max = 5))
  val localProbe = TestProbe()
  val vehicleProbe = TestProbe()
  val catchall = TestProbe()
  val zone = new Zone(id = "test-zone", new ZoneMap(name = "test-map"), zoneNumber = 0) {
    override def SetupNumberPools(): Unit = {}
    GUID(guid)
    override def Vehicles = List(apc, fury)
    override def DeployableList = List(boomer, boomer2)
    override def VehicleEvents = vehicleProbe.ref
    override def LocalEvents = localProbe.ref
    override def AvatarEvents = catchall.ref
    override def Activity = catchall.ref
  }

  guid.register(apc, number = 1)
  apc.Faction = PlanetSideEmpire.VS
  apc.Zone = zone
  apc.Capacitor = apc.Definition.MaxCapacitor
  apc.Definition.Initialize(apc, context)
  zone.blockMap.addTo(apc)

  val furyProbe = TestProbe()
  guid.register(fury, number = 2)
  fury.Position = Vector3(4, 0, 0) //within 15m of apc
  fury.Faction = PlanetSideEmpire.TR
  fury.Zone = zone
  fury.Actor = furyProbe.ref
  zone.blockMap.addTo(fury)

  val boomerProbe = TestProbe()
  guid.register(boomer, number = 3)
  boomer.Position = Vector3(0, 14, 0) //within 15m of apc
  boomer.Faction = PlanetSideEmpire.TR
  boomer.Zone = zone
  boomer.Actor = boomerProbe.ref
  zone.blockMap.addTo(boomer)

  val boomer2Probe = TestProbe()
  guid.register(boomer2, number = 4)
  boomer2.Position = Vector3(0, 30, 0) //beyond 15m of apc
  boomer2.Faction = PlanetSideEmpire.TR
  boomer2.Zone = zone
  boomer2.Actor = boomer2Probe.ref
  zone.blockMap.addTo(boomer2)

  "ApcControl" should {
    "charge its capacitors when initialized" in {
      assert(apc.Capacitor == apc.Definition.MaxCapacitor)
      apc.Definition.Initialize(apc, context)
      vehicleProbe.expectNoMessage(5.seconds) //the capacitor is max, so no charging is needed

      apc.Actor ! SpecialEmp.Burst()
      val vehicleMsgs = vehicleProbe.receiveN(2, 500.milliseconds)
      vehicleMsgs.head match {
        case VehicleServiceMessage(_, VehicleAction.PlanetsideAttribute(_, PlanetSideGUID(1), 113, 0)) => ;
        case _ => assert(false)
      }
      vehicleMsgs(1) match {
        case VehicleServiceMessage(
        "test-zone",
          VehicleAction.SendResponse(
            _,
            TriggerEffectMessage(_, "apc_explosion_emp_vs", None, Some(TriggeredEffectLocation(Vector3.Zero, Vector3.Zero)))
          )
        ) => ;
        case _ => assert(false)
      }
      assert(apc.Capacitor == 0)

      val furyMsg = furyProbe.receiveOne(200.milliseconds)
      furyMsg match {
        case Vitality.Damage(_) => ;
        case _ => assert(false)
      }
      val boomerMsg = boomerProbe.receiveOne(200.milliseconds)
      boomerMsg match {
        case Vitality.Damage(_) => ;
        case _ => assert(false)
      }
      boomer2Probe.expectNoMessage(400.milliseconds) //out of range
    }
  }
}

object VehicleControlTest {
  import net.psforever.objects.avatar.Avatar
  import net.psforever.types.{CharacterSex, PlanetSideEmpire}

  val avatar1 = Avatar(0, "test1", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)
  val avatar2 = Avatar(1, "test2", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute)

  def checkCanNotMount(probe: TestProbe, id: String): Unit = {
    val reply = probe.receiveOne(Duration.create(250, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanNotMount], s"test $id")
      case _ =>
        assert(false, s"test $id-b")
    }
  }

  def checkCanMount(probe: TestProbe, id: String): Unit = {
    val reply = probe.receiveOne(Duration.create(250, "ms"))
    reply match {
      case msg: Mountable.MountMessages =>
        assert(msg.response.isInstanceOf[Mountable.CanMount], s" - test: $id")
      case _ =>
        assert(false, s" - test: $id-b")
    }
  }
}
