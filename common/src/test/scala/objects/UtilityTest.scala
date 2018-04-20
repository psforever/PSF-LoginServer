// Copyright (c) 2017 PSForever
package objects

import akka.actor.{Actor, ActorRef, Props}
import net.psforever.objects.{GlobalDefinitions, Vehicle}
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.vehicles._
import net.psforever.packet.game.PlanetSideGUID
import org.specs2.mutable._

import scala.concurrent.duration.Duration

class UtilityTest extends Specification {
  "Utility" should {
    "create an order_terminala object" in {
      val obj = Utility(UtilityType.order_terminala, UtilityTest.vehicle)
      obj.UtilType mustEqual UtilityType.order_terminala
      obj().isInstanceOf[Terminal] mustEqual true
      obj().asInstanceOf[Terminal].Definition.ObjectId mustEqual 613
      obj().asInstanceOf[Terminal].Actor == ActorRef.noSender
    }

    "create an order_terminalb object" in {
      val obj = Utility(UtilityType.order_terminalb, UtilityTest.vehicle)
      obj.UtilType mustEqual UtilityType.order_terminalb
      obj().isInstanceOf[Terminal] mustEqual true
      obj().asInstanceOf[Terminal].Definition.ObjectId mustEqual 614
      obj().asInstanceOf[Terminal].Actor == ActorRef.noSender
    }

    "create a matrix_terminalc object" in {
      val obj = Utility(UtilityType.matrix_terminalc, UtilityTest.vehicle)
      obj.UtilType mustEqual UtilityType.matrix_terminalc
      obj().isInstanceOf[Terminal] mustEqual true
      obj().asInstanceOf[Terminal].Definition.ObjectId mustEqual 519
      obj().asInstanceOf[Terminal].Actor == ActorRef.noSender
    }

    "create an ams_respawn_tube object" in {
      import net.psforever.objects.serverobject.tube.SpawnTube
      val obj = Utility(UtilityType.ams_respawn_tube, UtilityTest.vehicle)
      obj.UtilType mustEqual UtilityType.ams_respawn_tube
      obj().isInstanceOf[SpawnTube] mustEqual true
      obj().asInstanceOf[SpawnTube].Definition.ObjectId mustEqual 49
      obj().asInstanceOf[SpawnTube].Actor == ActorRef.noSender
    }

    "be located with their owner (terminal)" in {
      val veh = Vehicle(GlobalDefinitions.quadstealth)
      val obj = Utility(UtilityType.order_terminala, veh)
      obj().Position mustEqual veh.Position
      obj().Orientation mustEqual veh.Orientation

      import net.psforever.types.Vector3
      veh.Position = Vector3(1, 2, 3)
      veh.Orientation = Vector3(4, 5, 6)
      obj().Position mustEqual veh.Position
      obj().Orientation mustEqual veh.Orientation
    }

    "be located with their owner (spawn tube)" in {
      val veh = Vehicle(GlobalDefinitions.quadstealth)
      val obj = Utility(UtilityType.ams_respawn_tube, veh)
      obj().Position mustEqual veh.Position
      obj().Orientation mustEqual veh.Orientation

      import net.psforever.types.Vector3
      veh.Position = Vector3(1, 2, 3)
      veh.Orientation = Vector3(4, 5, 6)
      obj().Position mustEqual veh.Position
      obj().Orientation mustEqual veh.Orientation
    }
  }
}

class Utility1Test extends ActorTest() {
  "Utility" should {
    "wire an order_terminala Actor" in {
      val obj = Utility(UtilityType.order_terminala, UtilityTest.vehicle)
      obj().GUID = PlanetSideGUID(1)
      assert(obj().Actor == ActorRef.noSender)

      system.actorOf(Props(classOf[UtilityTest.SetupControl], obj), "test") ! ""
      receiveOne(Duration.create(100, "ms")) //consume and discard
      assert(obj().Actor != ActorRef.noSender)
    }
  }
}

class Utility2Test extends ActorTest() {
  "Utility" should {
    "wire an order_terminalb Actor" in {
      val obj = Utility(UtilityType.order_terminalb, UtilityTest.vehicle)
      obj().GUID = PlanetSideGUID(1)
      assert(obj().Actor == ActorRef.noSender)

      system.actorOf(Props(classOf[UtilityTest.SetupControl], obj), "test") ! ""
      receiveOne(Duration.create(100, "ms")) //consume and discard
      assert(obj().Actor != ActorRef.noSender)
    }
  }
}

class Utility3Test extends ActorTest() {
  "Utility" should {
    "wire a matrix_terminalc Actor" in {
      val obj = Utility(UtilityType.matrix_terminalc, UtilityTest.vehicle)
      obj().GUID = PlanetSideGUID(1)
      assert(obj().Actor == ActorRef.noSender)

      system.actorOf(Props(classOf[UtilityTest.SetupControl], obj), "test") ! ""
      receiveOne(Duration.create(100, "ms")) //consume and discard
      assert(obj().Actor != ActorRef.noSender)
    }
  }
}

class Utility4Test extends ActorTest() {
  "Utility" should {
    "wire an ams_respawn_tube Actor" in {
      val obj = Utility(UtilityType.ams_respawn_tube, UtilityTest.vehicle)
      obj().GUID = PlanetSideGUID(1)
      assert(obj().Actor == ActorRef.noSender)

      system.actorOf(Props(classOf[UtilityTest.SetupControl], obj), "test") ! ""
      receiveOne(Duration.create(100, "ms")) //consume and discard
      assert(obj().Actor != ActorRef.noSender)
    }
  }
}

object UtilityTest {
  val vehicle = Vehicle(GlobalDefinitions.quadstealth)

  class SetupControl(obj : Utility) extends Actor {
    def receive : Receive = {
      case _ =>
        obj.Setup(context)
        sender ! ""
    }
  }
}
