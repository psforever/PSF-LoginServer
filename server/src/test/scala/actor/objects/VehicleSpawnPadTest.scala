// Copyright (c) 2017 PSForever
package actor.objects

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import actor.base.ActorTest
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.serverobject.structures.StructureType
import net.psforever.objects.{GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.zones.Zone
import net.psforever.types.{PlanetSideGUID, _}
import net.psforever.services.vehicle.VehicleAction
import akka.actor.typed.scaladsl.adapter._
import net.psforever.actors.zone.ZoneActor
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.services.base.envelope.GenericMessageEnvelope

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._

class VehicleSpawnControl1Test extends ActorTest {
  "VehicleSpawnControl" should {
    "construct" in {
      val obj = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      obj.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], obj), "mb_pad_creation")
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}

class VehicleSpawnControl2Test extends ActorTest {
  "VehicleSpawnControl" should {
    "complete a vehicle order" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe                        = new TestProbe(system, "zone-events")

      zone.VehicleEvents = probe.ref                                      //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order

      val messages = VehicleSpawnPadControlTest.drive(probe, 60.seconds)(
        {
          case _: VehicleSpawnPad.StartPlayerSeatedInVehicle => vehicle.Seats(0).mount(player)
          case _: VehicleSpawnPad.ServerVehicleOverrideEnd   => vehicle.Position = Vector3(20, 0, 0)
          case _                                             => ()
        },
        _.isInstanceOf[VehicleSpawnPad.ResetSpawnPad]
      )
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.ConcealPlayer])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleAction.LoadVehicle])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.AttachToRails])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.PlayerSeatedInVehicle])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.DetachFromRails])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.ServerVehicleOverrideStart])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.ResetSpawnPad])
    }
  }
}

class VehicleSpawnControl3Test extends ActorTest {
  "VehicleSpawnControl" should {
    "block the second vehicle order until the first is completed" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      //we can recycle the vehicle and the player for each order
      val probe   = new TestProbe(system, "zone-events")
      val player2 = Player(Avatar(0, "test2", player.Faction, CharacterSex.Male, 0, CharacterVoice.Mute))
      player2.GUID = PlanetSideGUID(11)
      player2.Continent = zone.id
      player2.Zone = zone
      player2.Spawn()

      zone.VehicleEvents = probe.ref                                       //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal)  //first order
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player2, vehicle, terminal) //second order (vehicle shared)

      var concealCount = 0
      var mounted      = false
      val messages = VehicleSpawnPadControlTest.drive(probe, 120.seconds)(
        {
          case _: VehicleSpawnPad.ConcealPlayer =>
            concealCount += 1
          case _: VehicleSpawnPad.StartPlayerSeatedInVehicle if !mounted =>
            vehicle.Seats(0).mount(player)
            mounted = true
          case _: VehicleSpawnPad.ServerVehicleOverrideEnd if mounted =>
            //free the pad for the second order; the mounted driver keeps the vehicle from being dragged back
            vehicle.Position = Vector3(20, 0, 0)
          case _ => ()
        },
        _ => concealCount == 2
      )
      val payloads = VehicleSpawnPadControlTest.payloads(messages)
      assert(
        payloads.count(_.isInstanceOf[VehicleSpawnPad.ConcealPlayer]) == 2,
        s"expected both orders to be concealed, got $payloads"
      )
      val resetIndex   = payloads.indexWhere(_.isInstanceOf[VehicleSpawnPad.ResetSpawnPad])
      val secondConceal = payloads.indexWhere(_.isInstanceOf[VehicleSpawnPad.ConcealPlayer], from = payloads.indexWhere(_.isInstanceOf[VehicleSpawnPad.ConcealPlayer]) + 1)
      assert(resetIndex >= 0, s"expected the first order to clear the pad, got $payloads")
      assert(
        resetIndex < secondConceal,
        s"expected the second order to wait for the pad to clear, got $payloads"
      )
    }
  }
}

class VehicleSpawnControl4Test extends ActorTest {
  "VehicleSpawnControl" should {
    "clean up the vehicle if the driver-to-be is on the wrong continent" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe                        = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe.ref
      player.Continent = "problem" //problem

      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order
      val messages = VehicleSpawnPadControlTest.collectUntil(probe, 60.seconds)(_.isInstanceOf[VehicleSpawnPad.RevealPlayer])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.ConcealPlayer])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleAction.LoadVehicle])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.AttachToRails])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.DetachFromRails])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.RevealPlayer])
      assert(
        !VehicleSpawnPadControlTest.has(messages, classOf[VehicleSpawnPad.ServerVehicleOverrideStart]),
        s"a driver on the wrong continent must not be given control: ${VehicleSpawnPadControlTest.payloads(messages)}"
      )
    }
  }
}

class VehicleSpawnControl5Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "abandon a destroyed vehicle on the spawn pad (blocking)" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)

      val probe = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe.ref
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order

      val before = VehicleSpawnPadControlTest.collectUntil(probe, 60.seconds)(_.isInstanceOf[VehicleSpawnPad.ConcealPlayer])
      vehicle.Health = 0 //problem
      val after = VehicleSpawnPadControlTest.collectUntil(probe, 60.seconds)(_.isInstanceOf[VehicleSpawnPad.RevealPlayer])
      val messages = before ++ after
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleAction.LoadVehicle])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.AttachToRails])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.DetachFromRails])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.RevealPlayer])
      assert(
        !VehicleSpawnPadControlTest.has(messages, classOf[VehicleSpawnPad.ServerVehicleOverrideStart]),
        s"a destroyed vehicle must not be given control: ${VehicleSpawnPadControlTest.payloads(messages)}"
      )
      val blocked = VehicleSpawnPadControlTest.collectUntil(probe, 60.seconds) { payload =>
        VehicleSpawnPadControlTest.isReminder(payload, VehicleSpawnPad.Reminders.Blocked)
      }
      assert(VehicleSpawnPadControlTest.hasReminder(blocked, VehicleSpawnPad.Reminders.Blocked), "expected a blocked reminder")
    }
  }
}

class VehicleSpawnControl6Test extends ActorTest() {
  "VehicleSpawnControl" should {
    "abandon a vehicle on the spawn pad if driver is unfit to drive (blocking)" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)

      val probe = new TestProbe(system, "zone-events")
      zone.VehicleEvents = probe.ref
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order

      val before = VehicleSpawnPadControlTest.collectUntil(probe, 60.seconds)(_.isInstanceOf[VehicleSpawnPad.StartPlayerSeatedInVehicle])
      player.Die
      val after = VehicleSpawnPadControlTest.collectUntil(probe, 60.seconds)(_.isInstanceOf[VehicleSpawnPad.RevealPlayer])
      val messages = before ++ after
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleAction.LoadVehicle])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.AttachToRails])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.DetachFromRails])
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.RevealPlayer])
      assert(
        !VehicleSpawnPadControlTest.has(messages, classOf[VehicleSpawnPad.ServerVehicleOverrideStart]),
        s"a dead driver must not be given control: ${VehicleSpawnPadControlTest.payloads(messages)}"
      )
      val blocked = VehicleSpawnPadControlTest.collectUntil(probe, 60.seconds) { payload =>
        VehicleSpawnPadControlTest.isReminder(payload, VehicleSpawnPad.Reminders.Blocked)
      }
      assert(VehicleSpawnPadControlTest.hasReminder(blocked, VehicleSpawnPad.Reminders.Blocked), "expected a blocked reminder")
    }
  }
}

class VehicleSpawnControl7Test extends ActorTest {
  "VehicleSpawnControl" should {
    "abandon a vehicle on the spawn pad if driver is unfit to drive (blocking)" in {
      val (vehicle, player, pad, terminal, zone) = VehicleSpawnPadControlTest.SetUpAgents(PlanetSideEmpire.TR)
      val probe                        = new TestProbe(system, "zone-events")
      player.ExoSuit = ExoSuitType.MAX

      zone.VehicleEvents = probe.ref                                      //zone events
      pad.Actor ! VehicleSpawnPad.VehicleOrder(player, vehicle, terminal) //order

      val messages = VehicleSpawnPadControlTest.collectUntil(probe, 60.seconds) { payload =>
        VehicleSpawnPadControlTest.isReminder(payload, VehicleSpawnPad.Reminders.Cancelled)
      }
      assert(
        !VehicleSpawnPadControlTest.has(messages, classOf[VehicleSpawnPad.ConcealPlayer]),
        s"an unfit driver must be rejected before concealment: ${VehicleSpawnPadControlTest.payloads(messages)}"
      )
      VehicleSpawnPadControlTest.assertHas(messages, classOf[VehicleSpawnPad.RevealPlayer])
      assert(VehicleSpawnPadControlTest.hasReminder(messages, VehicleSpawnPad.Reminders.Cancelled), "expected a cancelled reminder")
    }
  }
}

object VehicleSpawnPadControlTest {
  import net.psforever.objects.zones.ZoneMap
  private val map = new ZoneMap("test-map")

  /** Unwrap the payload of an event-service envelope. */
  def unwrap(message: Any): Any = message match {
    case GenericMessageEnvelope(_, _, payload) => payload
    case other                                 => other
  }

  def payloads(messages: Seq[Any]): Seq[Any] = messages.map(unwrap)

  /**
    * Consume messages until `stop` is satisfied, applying `onPayload` to each unwrapped payload.
    * The spawn pad and its process chain are asynchronous and intersperse periodic reminders,
    * so assertions are made on the payloads that were received rather than on a fixed sequence.
    */
  def drive(
             probe: TestProbe,
             timeout: FiniteDuration
           )(onPayload: Any => Unit, stop: Any => Boolean): Seq[Any] = {
    val received = ListBuffer.empty[Any]
    val deadline = timeout.fromNow
    var done     = false
    while (!done && deadline.hasTimeLeft()) {
      probe.receiveOne(deadline.timeLeft) match {
        case null => done = true
        case raw =>
          received += raw
          val payload = unwrap(raw)
          onPayload(payload)
          done = stop(payload)
      }
    }
    received.toSeq
  }

  def collectUntil(probe: TestProbe, timeout: FiniteDuration)(stop: Any => Boolean): Seq[Any] =
    drive(probe, timeout)(_ => (), stop)

  def has[T](messages: Seq[Any], clazz: Class[T]): Boolean =
    payloads(messages).exists(clazz.isInstance)

  def assertHas[T](messages: Seq[Any], clazz: Class[T]): Unit =
    assert(has(messages, clazz), s"expected a ${clazz.getSimpleName}, got ${payloads(messages)}")

  def isReminder(payload: Any, reminder: VehicleSpawnPad.Reminders.Value): Boolean = payload match {
    case VehicleSpawnPad.PeriodicReminder(`reminder`, _) => true
    case _                                               => false
  }

  def hasReminder(messages: Seq[Any], reminder: VehicleSpawnPad.Reminders.Value): Boolean =
    payloads(messages).exists(isReminder(_, reminder))

  def SetUpAgents(
      faction: PlanetSideEmpire.Value
  )(implicit system: ActorSystem): (Vehicle, Player, VehicleSpawnPad, Terminal, Zone) = {
    import net.psforever.objects.guid.NumberPoolHub
    import net.psforever.objects.guid.source.MaxNumberSource
    import net.psforever.objects.serverobject.structures.Building
    import net.psforever.objects.Tool
    import net.psforever.types.CharacterSex

    val terminal            = Terminal(GlobalDefinitions.vehicle_terminal_combined)
    val vehicle             = Vehicle(GlobalDefinitions.two_man_assault_buggy)
    val weapon              = vehicle.WeaponControlledFromSeat(1).head.asInstanceOf[Tool]
    val guid: NumberPoolHub = new NumberPoolHub(MaxNumberSource(5))
    guid.AddPool("test-pool", (0 to 5).toList)
    guid.register(vehicle, "test-pool")
    guid.register(weapon, "test-pool")
    guid.register(weapon.AmmoSlot.Box, "test-pool")
    guid.register(terminal, "test-pool")
    val zone = new Zone("test-zone", map, 0) {
      override def SetupNumberPools(): Unit = {}
    }
    zone.GUID(guid)
    zone.actor = system.spawn(ZoneActor(zone), s"test-zone-${System.currentTimeMillis()}")

    // Hack: Wait for the Zone to finish booting, otherwise later tests will fail randomly due to race conditions
    // with actor probe setting
    // TODO(chord): Remove when Zone supports notification of booting being complete
    Thread.sleep(5000)

    val pad = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
    pad.Actor = system.actorOf(Props(classOf[VehicleSpawnControl], pad), s"test-pad-${System.currentTimeMillis()}")
    pad.Owner =
      new Building("Building", building_guid = 0, map_id = 0, zone, StructureType.Building, GlobalDefinitions.building)
    pad.Owner.Faction = faction
    pad.Zone = zone
    terminal.Zone = zone
    guid.register(pad, "test-pool")
    val player = Player(Avatar(0, "test", faction, CharacterSex.Male, 0, CharacterVoice.Mute))
    guid.register(player, "test-pool")
    player.Zone = zone
    player.Spawn()
    //note: pad and vehicle are both at Vector3(1,0,0) so they count as blocking
    pad.Position = Vector3(1, 0, 0)
    vehicle.Position = Vector3(1, 0, 0)
    (vehicle, player, pad, terminal, zone)
  }
}
