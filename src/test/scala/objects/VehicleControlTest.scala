// Copyright (c) 2020 PSForever
package objects

import akka.actor.Props
import akka.actor.typed.scaladsl.adapter._
import akka.testkit.TestProbe
import base.{ActorTest, FreedContextActorTest}
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.{Avatar, PlayerControl}
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.source.MaxNumberSource
import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vehicles.{VehicleControl, VehicleLockState}
import net.psforever.objects.vital.VehicleShieldCharge
import net.psforever.objects.zones.{Zone, ZoneMap}
import net.psforever.packet.game.{CargoMountPointStatusMessage, ObjectDetachMessage, PlanetsideAttributeMessage}
import net.psforever.services.ServiceManager
import net.psforever.services.avatar.{AvatarAction, AvatarServiceMessage}
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
      assert(vehicle.Seats(1).occupants.isEmpty)
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
  vehicle.Seats(1).mount(player1) //passenger mount
  player1.VehicleSeated = vehicle.GUID
  lodestar.Seats(0).mount(player2)
  player2.VehicleSeated = lodestar.GUID
  lodestar.CargoHolds(1).mount(vehicle)
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
      assert(vehicle.Seats(1).occupants.isEmpty)
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
  vehicle.Seats(1).mount(player1) //passenger mount
  player1.VehicleSeated = vehicle.GUID
  lodestar.Seats(0).mount(player2)
  player2.VehicleSeated = lodestar.GUID
  lodestar.CargoHolds(1).mount(vehicle)
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
      assert(lodestar.Seats(0).occupants.isEmpty)
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

  val vehicle2 = Vehicle(GlobalDefinitions.lightning)
  vehicle2.Faction = PlanetSideEmpire.TR
  vehicle2.GUID = PlanetSideGUID(11)
  vehicle2.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle2), "vehicle2-test")

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
    "block players from sitting if their exo-suit is not allowed by the mount - apc_tr" in {
      // disallow
      vehicle2.Actor.tell(Mountable.TryMount(player1, 0), probe.ref) // Reinforced in non-reinforced mount
      checkCanNotMount()
      vehicle.Actor.tell(Mountable.TryMount(player2, 0), probe.ref) //MAX in non-Reinforced mount
      checkCanNotMount()
      vehicle.Actor.tell(Mountable.TryMount(player2, 1), probe.ref) //MAX in non-MAX mount
      checkCanNotMount()
      vehicle.Actor.tell(Mountable.TryMount(player1, 9), probe.ref) //Reinforced in MAX-only mount
      checkCanNotMount()
      vehicle.Actor.tell(Mountable.TryMount(player3, 9), probe.ref) //Agile in MAX-only mount
      checkCanNotMount()

      //allow
      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref) // Reinforced in driver mount allowing all except MAX
      checkCanMount()
      // Reset to allow further driver mount mounting tests
      vehicle.Actor ! Mountable.TryDismount(player1, 0)
      vehicle.Owner = None
      vehicle.OwnerName = None
      vehicle.Actor.tell(Mountable.TryMount(player3, 0), probe.ref) // Agile in driver mount allowing all except MAX
      checkCanMount()
      vehicle.Actor.tell(Mountable.TryMount(player1, 1), probe.ref) // Reinforced in passenger mount allowing all except MAX
      checkCanMount()
      vehicle.Actor.tell(Mountable.TryMount(player2, 9), probe.ref) // MAX in MAX-only mount
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
    "block players from sitting if the mount does not allow it" in {

      vehicle.PermissionGroup(2, 3)                                 //passenger group -> empire
      vehicle.Actor.tell(Mountable.TryMount(player1, 3), probe.ref) //passenger mount
      checkCanMount()
      vehicle.PermissionGroup(2, 0)                                 //passenger group -> locked
      vehicle.Actor.tell(Mountable.TryMount(player2, 4), probe.ref) //passenger mount
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
    "allow players to sit in the driver mount, even if it is locked, if the vehicle is unowned" in {
      assert(vehicle.PermissionGroup(0).contains(VehicleLockState.Locked)) //driver group -> locked
      assert(vehicle.Seats(0).occupants.isEmpty)
      assert(vehicle.Owner.isEmpty)
      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref)
      checkCanMount()
      assert(vehicle.Seats(0).occupants.nonEmpty)
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
    "block players that are not the current owner from sitting in the driver mount (locked)" in {
      assert(vehicle.PermissionGroup(0).contains(VehicleLockState.Locked)) //driver group -> locked
      assert(vehicle.Seats(0).occupants.isEmpty)
      vehicle.Owner = player1.GUID

      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref)
      checkCanMount()
      assert(vehicle.Seats(0).occupants.nonEmpty)
      vehicle.Actor.tell(Mountable.TryDismount(player1, 0), probe.ref)
      probe.receiveOne(Duration.create(100, "ms")) //discard
      assert(vehicle.Seats(0).occupants.isEmpty)

      vehicle.Actor.tell(Mountable.TryMount(player2, 0), probe.ref)
      checkCanNotMount()
      assert(vehicle.Seats(0).occupants.isEmpty)
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
    "allow players that are not the current owner to sit in the driver mount (empire)" in {
      vehicle.PermissionGroup(0, 3)                                        //passenger group -> empire
      assert(vehicle.PermissionGroup(0).contains(VehicleLockState.Empire)) //driver group -> empire
      assert(vehicle.Seats(0).occupants.isEmpty)
      vehicle.Owner = player1.GUID //owner set

      vehicle.Actor.tell(Mountable.TryMount(player1, 0), probe.ref)
      checkCanMount()
      assert(vehicle.Seats(0).occupants.nonEmpty)
      vehicle.Actor.tell(Mountable.TryDismount(player1, 0), probe.ref)
      probe.receiveOne(Duration.create(100, "ms")) //discard
      assert(vehicle.Seats(0).occupants.isEmpty)

      vehicle.Actor.tell(Mountable.TryMount(player2, 0), probe.ref)
      checkCanMount()
      assert(vehicle.Seats(0).occupants.nonEmpty)
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
//  val obj = DamageInteraction(p_source, ProjectileReason(DamageResolution.Hit, projectile, fury_dm), Vector3(1.2f, 3.4f, 5.6f))
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

class VehicleControlInteractWithWaterPartialTest extends ActorTest {
  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  val playerProbe = TestProbe()
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val pool = Pool(EnvironmentAttribute.Water, DeepSquare(-1, 10, 10, 0, 0))
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

  guid.register(player1, 1)
  guid.register(vehicle, 2)
  player1.Zone = zone
  player1.Spawn()
  vehicle.Zone = zone
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Seats(0).mount(player1)
  player1.VehicleSeated = vehicle.GUID
  player1.Actor = playerProbe.ref
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-control")

  "VehicleControl" should {
    "causes disability when the vehicle drives too deep in water (check driver messaging)" in {
      vehicle.Position = Vector3(5,5,-3) //right in the pool
      vehicle.zoneInteraction() //trigger

      val msg_drown = playerProbe.receiveOne(250 milliseconds)
      assert(
        msg_drown match {
          case InteractWithEnvironment(
            p1,
            p2,
            Some(OxygenStateTarget(PlanetSideGUID(2), OxygenState.Suffocation, 100f))
          )      => (p1 eq player1) && (p2 eq pool)
          case _ => false
        }
      )
    }
  }
}

class VehicleControlInteractWithWaterTest extends ActorTest {
  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  val avatarProbe = TestProbe()
  val vehicleProbe = TestProbe()
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val pool = Pool(EnvironmentAttribute.Water, DeepSquare(-1, 10, 10, 0, 0))
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
    override def AvatarEvents = avatarProbe.ref
    override def VehicleEvents = vehicleProbe.ref
  }

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
    "causes disability when the vehicle drives too deep in water" in {
      vehicle.Position = Vector3(5,5,-3) //right in the pool
      vehicle.zoneInteraction() //trigger

      val msg_drown = avatarProbe.receiveOne(250 milliseconds)
      assert(
        msg_drown match {
          case AvatarServiceMessage(
            "TestCharacter1",
            AvatarAction.OxygenState(
              OxygenStateTarget(PlanetSideGUID(1), OxygenState.Suffocation, 100f),
              Some(OxygenStateTarget(PlanetSideGUID(2), OxygenState.Suffocation, 100f))
            )
          )      => true
          case _ => false
        }
      )
      //player will die in 60s
      //vehicle will disable in 5s; driver will be kicked
      val msg_kick = vehicleProbe.receiveOne(6 seconds)
      assert(
        msg_kick match {
          case VehicleServiceMessage(
            "test-zone",
            VehicleAction.KickPassenger(PlanetSideGUID(1), 4, _, PlanetSideGUID(2))
          )      => true
          case _ => false
        }
      )
      //player will die, but detailing players death messages is not necessary for this test
    }
  }
}

class VehicleControlStopInteractWithWaterTest extends ActorTest {
  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  val playerProbe = TestProbe()
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val pool = Pool(EnvironmentAttribute.Water, DeepSquare(-1, 10, 10, 0, 0))
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

  guid.register(player1, 1)
  guid.register(vehicle, 2)
  player1.Zone = zone
  player1.Spawn()
  vehicle.Zone = zone
  vehicle.Faction = PlanetSideEmpire.TR
  vehicle.Seats(0).mount(player1)
  player1.VehicleSeated = vehicle.GUID
  player1.Actor = playerProbe.ref
  vehicle.Actor = system.actorOf(Props(classOf[VehicleControl], vehicle), "vehicle-control")

  "VehicleControl" should {
    "stop becoming disabled if the vehicle drives out of the water" in {
      vehicle.Position = Vector3(5,5,-3) //right in the pool
      vehicle.zoneInteraction() //trigger
      val msg_drown = playerProbe.receiveOne(250 milliseconds)
      assert(
        msg_drown match {
          case InteractWithEnvironment(
            p1,
            p2,
            Some(OxygenStateTarget(PlanetSideGUID(2), OxygenState.Suffocation, 100f))
          )      => (p1 eq player1) && (p2 eq pool)
          case _ => false
        }
      )

      vehicle.Position = Vector3.Zero //that's enough of that
      vehicle.zoneInteraction()
      val msg_recover = playerProbe.receiveOne(250 milliseconds)
      assert(
        msg_recover match {
          case EscapeFromEnvironment(
            p1,
            p2,
            Some(OxygenStateTarget(PlanetSideGUID(2), OxygenState.Recovery, _))
          )      => (p1 eq player1) && (p2 eq pool)
          case _ => false
        }
      )
    }
  }
}

class VehicleControlInteractWithLavaTest extends ActorTest {
  val vehicle = Vehicle(GlobalDefinitions.fury) //guid=2
  val player1 =
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
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
    override def SetupNumberPools() = {}
    GUID(guid)
    override def LivePlayers = List(player1)
    override def Vehicles = List(vehicle)
    override def AvatarEvents = avatarProbe.ref
    override def VehicleEvents = vehicleProbe.ref
    override def Activity = TestProbe().ref
  }

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
      vehicle.zoneInteraction() //trigger

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
    Player(Avatar(0, "TestCharacter1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)) //guid=1
  val guid = new NumberPoolHub(new MaxNumberSource(15))
  val pool = Pool(EnvironmentAttribute.Death, DeepSquare(-1, 10, 10, 0, 0))
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
    override def AvatarEvents = TestProbe().ref
    override def VehicleEvents = TestProbe().ref
  }

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
    "take continuous damage if vehicle drives into a pool of death" in {
      assert(vehicle.Health > 0) //alive
      assert(player1.Health == 100) //alive
      vehicle.Position = Vector3(5,5,-3) //right in the pool
      vehicle.zoneInteraction() //trigger

      probe.receiveOne(2 seconds) //wait until player1's implants deinitialize
      assert(vehicle.Health == 0) //ded
      assert(player1.Health == 0) //ded
    }
  }
}

object VehicleControlTest {
  import net.psforever.objects.avatar.Avatar
  import net.psforever.types.{CharacterGender, PlanetSideEmpire}

  val avatar1 = Avatar(0, "test1", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
  val avatar2 = Avatar(1, "test2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute)
}
