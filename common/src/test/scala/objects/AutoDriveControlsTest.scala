// Copyright (c) 2017 PSForever
package objects

import akka.actor.Props
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.serverobject.pad.{VehicleSpawnControl, VehicleSpawnPad}
import net.psforever.objects.{Avatar, GlobalDefinitions, Player, Vehicle}
import net.psforever.objects.serverobject.pad.process.{AutoDriveControls, VehicleSpawnControlGuided}
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire, Vector3}
import org.specs2.mutable.Specification

import scala.concurrent.duration._

class AutoDriveControlsTest extends Specification {
  "CancelEntry" should {
    val vehicle = Vehicle(GlobalDefinitions.fury)
    def exampleTest(vehicle : Vehicle) : Boolean = { vehicle.Position == Vector3(1,1,1) }

    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.CancelEarly(exampleTest)
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Cancel
      setting.Data mustEqual None
      setting.Delay mustEqual 200L
    }

    "validate" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.CancelEarly(exampleTest)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Position mustEqual Vector3.Zero
      setting.Validate(vehicle) mustEqual false
      vehicle.Position = Vector3(1,1,1)
      setting.Validate(vehicle) mustEqual true
    }

    "completion" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.CancelEarly(exampleTest)
      val setting : AutoDriveControls.Setting = config.Create
      setting.CompletionTest(vehicle) mustEqual true //always true
    }
  }

  "Climb" should {
    val vehicle = Vehicle(GlobalDefinitions.mosquito)

    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Climb(10.5f)
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Climb
      setting.Data mustEqual Some(10.5f)
      setting.Delay mustEqual 200L
    }

    "validate" in {
      val vehicle_fury = Vehicle(GlobalDefinitions.fury)
      val config : AutoDriveControls.Configuration = AutoDriveControls.Climb(10.5f)
      val setting : AutoDriveControls.Setting = config.Create

      setting.Validate(vehicle) mustEqual true //mosquito is a flying vehicle
      setting.Validate(vehicle_fury) mustEqual false
    }

    "completion" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Climb(10.5f)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Position mustEqual Vector3.Zero
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Position = Vector3(0,0,10.5f)
      setting.CompletionTest(vehicle) mustEqual true
    }
  }

  "Distance" should {
    val vehicle = Vehicle(GlobalDefinitions.fury)

    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Distance(Vector3.Zero, 10.5f)
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Wait
      setting.Data mustEqual None
      setting.Delay mustEqual 200L
    }
    "validate" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Distance(Vector3.Zero, 10.5f)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Velocity mustEqual None
      setting.Validate(vehicle) mustEqual false
      vehicle.Velocity = Vector3.Zero
      setting.Validate(vehicle) mustEqual false
      vehicle.Velocity = Vector3(1,0,0)
      setting.Validate(vehicle) mustEqual true
    }

    "completion" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Distance(Vector3.Zero, 10.5f)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Position = Vector3(0,0,0)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Position = Vector3(10.5f,0,0)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Position = Vector3(11,0,0)
      setting.CompletionTest(vehicle) mustEqual true
      vehicle.Position = Vector3(0,11,0)
      setting.CompletionTest(vehicle) mustEqual true
      vehicle.Position = Vector3(0,0,11)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Position = Vector3(7.5f,7.5f,0)
      setting.CompletionTest(vehicle) mustEqual true
    }
  }

  "DistanceFromHere" should {
    val vehicle = Vehicle(GlobalDefinitions.fury)

    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.DistanceFromHere(10.5f)
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Wait
      setting.Data mustEqual None
      setting.Delay mustEqual 200L
    }

    "validate" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.DistanceFromHere(10.5f)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Velocity mustEqual None
      setting.Validate(vehicle) mustEqual false
      vehicle.Velocity = Vector3.Zero
      setting.Validate(vehicle) mustEqual false
      vehicle.Velocity = Vector3(1,0,0)
      setting.Validate(vehicle) mustEqual true
    }

    "completion" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.DistanceFromHere(10.5f)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Position = Vector3(0,0,0)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Position = Vector3(10.5f,0,0)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Position = Vector3(11,0,0)
      setting.CompletionTest(vehicle) mustEqual true
      vehicle.Position = Vector3(0,11,0)
      setting.CompletionTest(vehicle) mustEqual true
      vehicle.Position = Vector3(0,0,11)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Position = Vector3(7.5f,7.5f,0)
      setting.CompletionTest(vehicle) mustEqual true
    }
  }

  "Drive" should {
    val vehicle = Vehicle(GlobalDefinitions.fury)

    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Drive(3)
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Drive
      setting.Data mustEqual Some(3)
      setting.Delay mustEqual 200L
    }

    "validate" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Drive(3)
      val setting : AutoDriveControls.Setting = config.Create
      setting.Validate(vehicle) mustEqual true
    }

    "completion" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Drive(3)
      val setting : AutoDriveControls.Setting = config.Create
      vehicle.Velocity mustEqual None
      setting.CompletionTest(vehicle) mustEqual false

      vehicle.Velocity = Vector3.Zero
      vehicle.Velocity mustEqual Some(Vector3.Zero)
      setting.CompletionTest(vehicle) mustEqual false

      vehicle.Velocity = Vector3(1,0,0)
      vehicle.Velocity mustEqual Some(Vector3(1,0,0))
      setting.CompletionTest(vehicle) mustEqual true

    }
  }

  "FirstGear" should {
    val veh_def = GlobalDefinitions.mediumtransport
    val vehicle = Vehicle(veh_def)

    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.FirstGear()
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Drive
      setting.Data mustEqual Some(0)
      setting.Delay mustEqual 200L
    }

    "validate" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.FirstGear()
      val setting : AutoDriveControls.Setting = config.Create
      setting.Validate(vehicle) mustEqual true //always true
    }

    "completion" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.FirstGear()
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Velocity mustEqual None
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Velocity = Vector3.Zero
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Velocity = Vector3(1,0,0)
      setting.CompletionTest(vehicle) mustEqual true
    }

    "data" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.FirstGear()
      val setting : AutoDriveControls.Setting = config.Create

      setting.Data mustEqual Some(0)
      setting.Validate(vehicle)
      setting.Data mustEqual Some(veh_def.AutoPilotSpeed1)
    }
  }

  "ForTime" should {
    val veh_def = GlobalDefinitions.mediumtransport
    val vehicle = Vehicle(veh_def)

    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.ForTime(1200L)
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Wait
      setting.Data mustEqual None
      setting.Delay mustEqual 200L
    }

    "validate" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.ForTime(1200L)
      val setting : AutoDriveControls.Setting = config.Create
      setting.Validate(vehicle) mustEqual true //always true
    }

    "completion" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.ForTime(1200L)
      val setting : AutoDriveControls.Setting = config.Create
      setting.CompletionTest(vehicle) mustEqual false

      Thread.sleep(1100)
      setting.CompletionTest(vehicle) mustEqual false

      Thread.sleep(200)
      setting.CompletionTest(vehicle) mustEqual true
    }
  }

  "SecondGear" should {
    val veh_def = GlobalDefinitions.mediumtransport
    val vehicle = Vehicle(veh_def)

    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.SecondGear()
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Drive
      setting.Data mustEqual Some(0)
      setting.Delay mustEqual 200L
    }

    "validate" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.SecondGear()
      val setting : AutoDriveControls.Setting = config.Create
      setting.Validate(vehicle) mustEqual true //always true
    }

    "completion" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.SecondGear()
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Velocity mustEqual None
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Velocity = Vector3.Zero
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Velocity = Vector3(1,0,0)
      setting.CompletionTest(vehicle) mustEqual true
    }

    "data" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.SecondGear()
      val setting : AutoDriveControls.Setting = config.Create

      setting.Data mustEqual Some(0)
      setting.Validate(vehicle)
      setting.Data mustEqual Some(veh_def.AutoPilotSpeed2)
    }
  }

  "Stop" should {
    val vehicle = Vehicle(GlobalDefinitions.mediumtransport)

    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Stop()
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Stop
      setting.Data mustEqual None
      setting.Delay mustEqual 200L
    }

    "validate" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Stop()
      val setting : AutoDriveControls.Setting = config.Create
      setting.Validate(vehicle) mustEqual true //always true
    }

    "completion" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.Stop()
      val setting : AutoDriveControls.Setting = config.Create
      setting.CompletionTest(vehicle) mustEqual true //always true
    }
  }

  "TurnBy" should {
    "create" in {
      val config : AutoDriveControls.Configuration = AutoDriveControls.TurnBy(35.5f, 23)
      val setting : AutoDriveControls.Setting = config.Create
      setting.Type mustEqual AutoDriveControls.State.Turn
      setting.Data mustEqual Some(23)
      setting.Delay mustEqual 100L
    }

    "validate (velocity)" in {
      val vehicle = Vehicle(GlobalDefinitions.mediumtransport)
      val config : AutoDriveControls.Configuration = AutoDriveControls.TurnBy(35.5f, 23)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Velocity mustEqual None
      setting.Validate(vehicle) mustEqual false
      vehicle.Velocity = Vector3(1,1,1)
      setting.Validate(vehicle) mustEqual true
    }

    "validate (wheel direction = 15)" in {
      val vehicle = Vehicle(GlobalDefinitions.mediumtransport)
      val config : AutoDriveControls.Configuration = AutoDriveControls.TurnBy(35.5f, 15)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Velocity = Vector3(1,1,1)
      setting.Validate(vehicle) mustEqual false
    }

    "completion (passing 35.5-up)" in {
      val vehicle = Vehicle(GlobalDefinitions.mediumtransport)
      val config : AutoDriveControls.Configuration = AutoDriveControls.TurnBy(35.5f, 25)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Orientation mustEqual Vector3.Zero
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Orientation = Vector3(0,0,34.5f)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Orientation = Vector3(0,0,35f)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Orientation = Vector3(0,0,36.0f)
      setting.CompletionTest(vehicle) mustEqual true
    }

    "completion (passing 35.5 down)" in {
      val vehicle = Vehicle(GlobalDefinitions.mediumtransport)
      val config : AutoDriveControls.Configuration = AutoDriveControls.TurnBy(-35.5f, 25)
      val setting : AutoDriveControls.Setting = config.Create

      vehicle.Orientation = Vector3(0,0,40f)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Orientation = Vector3(0,0,5f)
      setting.CompletionTest(vehicle) mustEqual false
      vehicle.Orientation = Vector3(0,0,4f)
      setting.CompletionTest(vehicle) mustEqual true
    }
  }
}

class GuidedControlTest1 extends ActorTest {
  "VehicleSpawnControlGuided" should {
    "unguided" in {
      val vehicle = Vehicle(GlobalDefinitions.mediumtransport)
      vehicle.GUID = PlanetSideGUID(1)
      val driver = Player(Avatar("", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      driver.VehicleSeated = vehicle.GUID
      val sendTo = TestProbe()
      val order = VehicleSpawnControl.Order(driver, vehicle, sendTo.ref)
      val pad = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      pad.GUID = PlanetSideGUID(1)
      pad.Railed = false //suppress certain events
      val guided = system.actorOf(Props(classOf[VehicleSpawnControlGuided], pad), "pad")

      guided ! VehicleSpawnControl.Process.StartGuided(order)
      val msg = sendTo.receiveOne(100 milliseconds)
      assert(msg.isInstanceOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
    }
  }
}

class GuidedControlTest2 extends ActorTest {
  "VehicleSpawnControlGuided" should {
    "guided (one)" in {
      val vehicle = Vehicle(GlobalDefinitions.mediumtransport)
      vehicle.GUID = PlanetSideGUID(1)
      vehicle.Velocity = Vector3(1,1,1)
      val driver = Player(Avatar("", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      driver.VehicleSeated = vehicle.GUID
      val sendTo = TestProbe()
      val order = VehicleSpawnControl.Order(driver, vehicle, sendTo.ref)
      val pad = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      pad.Railed = false //suppress certain events
      val guided = system.actorOf(Props(classOf[VehicleSpawnControlGuided], pad), "pad")

      pad.Guide = List(AutoDriveControls.FirstGear())
      guided ! VehicleSpawnControl.Process.StartGuided(order)
      val msg1 = sendTo.receiveOne(100 milliseconds)
      assert(msg1.isInstanceOf[VehicleSpawnControlGuided.GuidedControl])
      assert(msg1.asInstanceOf[VehicleSpawnControlGuided.GuidedControl].command == AutoDriveControls.State.Drive)
      val msg2 = sendTo.receiveOne(200 milliseconds)
      assert(msg2.isInstanceOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
    }
  }
}

class GuidedControlTest3 extends ActorTest {
  "VehicleSpawnControlGuided" should {
    "guided (three)" in {
      val vehicle = Vehicle(GlobalDefinitions.mediumtransport)
      vehicle.GUID = PlanetSideGUID(1)
      vehicle.Velocity = Vector3(1,1,1)
      val driver = Player(Avatar("", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      driver.VehicleSeated = vehicle.GUID
      val sendTo = TestProbe()
      val order = VehicleSpawnControl.Order(driver, vehicle, sendTo.ref)
      val pad = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      pad.Railed = false //suppress certain events
      val guided = system.actorOf(Props(classOf[VehicleSpawnControlGuided], pad), "pad")

      pad.Guide = List(
        AutoDriveControls.FirstGear(),
        AutoDriveControls.ForTime(1000L),
        AutoDriveControls.SecondGear()
      )
      guided ! VehicleSpawnControl.Process.StartGuided(order)
      val msg1 = sendTo.receiveOne(100 milliseconds)
      assert(msg1.isInstanceOf[VehicleSpawnControlGuided.GuidedControl])
      assert(msg1.asInstanceOf[VehicleSpawnControlGuided.GuidedControl].command == AutoDriveControls.State.Drive)
      val msg2 = sendTo.receiveOne(100 milliseconds)
      assert(msg2.isInstanceOf[VehicleSpawnControlGuided.GuidedControl])
      assert(msg2.asInstanceOf[VehicleSpawnControlGuided.GuidedControl].command == AutoDriveControls.State.Wait)
      sendTo.expectNoMsg(1000 milliseconds)
      val msg3 = sendTo.receiveOne(300 milliseconds)
      assert(msg3.isInstanceOf[VehicleSpawnControlGuided.GuidedControl])
      assert(msg3.asInstanceOf[VehicleSpawnControlGuided.GuidedControl].command == AutoDriveControls.State.Drive)
      val msg4 = sendTo.receiveOne(200 milliseconds)
      assert(msg4.isInstanceOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
    }
  }
}

class GuidedControlTest4 extends ActorTest {
  "VehicleSpawnControlGuided" should {
    "fail validation test" in {
      def validationFailure(vehicle : Vehicle) : Boolean = false

      val vehicle = Vehicle(GlobalDefinitions.mediumtransport)
      vehicle.GUID = PlanetSideGUID(1)
      vehicle.Velocity = Vector3(1,1,1)
      val driver = Player(Avatar("", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      driver.VehicleSeated = vehicle.GUID
      val sendTo = TestProbe()
      val order = VehicleSpawnControl.Order(driver, vehicle, sendTo.ref)
      val pad = VehicleSpawnPad(GlobalDefinitions.mb_pad_creation)
      pad.Railed = false //suppress certain events
      val guided = system.actorOf(Props(classOf[VehicleSpawnControlGuided], pad), "pad")

      pad.Guide = List(
        AutoDriveControls.FirstGear(),
        AutoDriveControls.CancelEarly(validationFailure),
        AutoDriveControls.SecondGear()
      )
      guided ! VehicleSpawnControl.Process.StartGuided(order)
      val msg1 = sendTo.receiveOne(100 milliseconds)
      assert(msg1.isInstanceOf[VehicleSpawnControlGuided.GuidedControl])
      assert(msg1.asInstanceOf[VehicleSpawnControlGuided.GuidedControl].command == AutoDriveControls.State.Drive)
      val msg2 = sendTo.receiveOne(200 milliseconds)
      assert(msg2.isInstanceOf[VehicleSpawnPad.ServerVehicleOverrideEnd])
    }
  }
}
