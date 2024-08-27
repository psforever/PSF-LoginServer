package objects

import akka.actor.ActorRef
import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.Player
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.aura.{Aura, AuraEffectBehavior}
import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.environment.interaction.RespondsToZoneEnvironment
import net.psforever.objects.vital.Vitality
import net.psforever.objects.zones.{InteractsWithZone, Zone, ZoneMap}
import net.psforever.types.{CharacterSex, CharacterVoice, PlanetSideEmpire, PlanetSideGUID, Vector3}

import scala.concurrent.duration._

class InteractsWithZoneEnvironmentTest extends ActorTest {
  val pool1: Pool = Pool(EnvironmentAttribute.Water, DeepSquare(3, 2, 2, 0, 0))
  val pool2: Pool = Pool(EnvironmentAttribute.Water, DeepSquare(3, 4, 2, 2, 0))
  val pool3: Pool = Pool(EnvironmentAttribute.Lava, DeepSquare(3, 2, 4, 0, 2))
  val zoneEvents: TestProbe = TestProbe()
  val testZone: Zone = {
    val testMap = new ZoneMap(name = "test-map") {
      environment = List(pool1, pool2, pool3)
    }
    new Zone("test-zone", testMap, zoneNumber = 0) {
      override def AvatarEvents: ActorRef = zoneEvents.ref
    }
  }
  testZone.blockMap.addTo(pool1)
  testZone.blockMap.addTo(pool2)
  testZone.blockMap.addTo(pool3)

  "InteractsWithZoneEnvironment" should {
    "not interact with any environment when it does not encroach any environment" in {
      val testProbe = TestProbe()
      val obj = InteractsWithZoneEnvironmentTest.testObject()
      obj.Zone = testZone
      obj.Actor = testProbe.ref
      obj.Position = Vector3(10,10,0)

      obj.zoneInteractions()
      testProbe.expectNoMessage(max = 500 milliseconds)
    }

    "acknowledge interaction when moved into the critical region of a registered environment object" in {
      val testProbe = TestProbe()
      val obj = InteractsWithZoneEnvironmentTest.testObject()
      obj.Zone = testZone
      obj.Actor = testProbe.ref

      obj.Position = Vector3(1, 1, 2.7f)
      obj.zoneInteractions()
      val msg = testProbe.receiveOne(4.seconds)
      msg match {
        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
        case _ => assert(InteractsWithZoneEnvironmentTest.fail, s"$msg")
      }

      obj.zoneInteractions()
      testProbe.expectNoMessage(max = 500 milliseconds)
    }

    "acknowledge cessation of interaction when moved out of a previous occupied the critical region (just once)" in {
      val testProbe = TestProbe()
      val obj = InteractsWithZoneEnvironmentTest.testObject()
      obj.Zone = testZone
      obj.Actor = testProbe.ref

      obj.Position = Vector3(1, 1, 2.7f)
      obj.zoneInteractions()
      val msg1 = testProbe.receiveOne(4.seconds)
      msg1 match {
        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
        case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
      }

      obj.Position = Vector3(1,1,5)
      obj.zoneInteractions()
      val msg2 = testProbe.receiveOne(4.seconds)
      msg2 match {
        case RespondsToZoneEnvironment.StopTimer(EnvironmentAttribute.Water) => ()
        case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
      }

      obj.zoneInteractions()
      testProbe.expectNoMessage(max = 500 milliseconds)
    }

    "transition between two different critical regions when the regions have the same attribute" in {
      val testProbe = TestProbe()
      val obj = InteractsWithZoneEnvironmentTest.testObject()
      obj.Zone = testZone
      obj.Actor = testProbe.ref

      obj.Position = Vector3(1, 1, 2.7f)
      obj.zoneInteractions()
      val msg1 = testProbe.receiveOne(4.seconds)
      msg1 match {
        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
        case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
      }

      obj.Position = Vector3(1, 3, 2.7f)
      obj.zoneInteractions()
//      val msg2 = testProbe.receiveOne(4.seconds)
//      msg2 match {
//        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
//        case _ => assert(false, "")
//      }
      testProbe.expectNoMessage()
    }

    "transition between two different critical regions when the regions have different attributes" in {
      val testProbe = TestProbe()
      val obj = InteractsWithZoneEnvironmentTest.testObject()
      obj.Zone = testZone
      obj.Actor = testProbe.ref

      obj.Position = Vector3(1, 1, 2.7f)
      obj.zoneInteractions()
      val msg1 = testProbe.receiveOne(4.seconds)
      msg1 match {
        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => ()
        case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
      }

      obj.Position = Vector3(3, 1, 2.7f)
      obj.zoneInteractions()
      val msgs = testProbe.receiveN(4, 4.seconds)
      msgs.head match {
        case Vitality.Damage(_) => ()
        case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
      }
      msgs(1) match {
        case AuraEffectBehavior.StartEffect(Aura.Fire, _) => ()
        case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
      }
      msgs(2) match {
        case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Lava, _, _, _) => ()
        case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
      }
      msgs(3) match {
        case RespondsToZoneEnvironment.StopTimer(EnvironmentAttribute.Water) => ()
        case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
      }
    }
  }

  "when interactions are disallowed, end any current interaction" in {
    val testProbe = TestProbe()
    val obj = InteractsWithZoneEnvironmentTest.testObject()
    obj.Zone = testZone
    obj.Actor = testProbe.ref

    obj.Position = Vector3(1, 1, 2.7f)
    obj.zoneInteractions()
    val msg1 = testProbe.receiveOne(max = 250 milliseconds)
    msg1 match {
      case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => true
      case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
    }

    obj.allowInteraction = false
    val msg2 = testProbe.receiveOne(max = 250 milliseconds)
    msg2 match {
      case RespondsToZoneEnvironment.StopTimer(EnvironmentAttribute.Water) => true
      case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
    }

    obj.zoneInteractions()
    testProbe.expectNoMessage(max = 500 milliseconds)
  }

  "when interactions are allowed, after having been disallowed, engage in any detected interaction" in {
    val testProbe = TestProbe()
    val obj = InteractsWithZoneEnvironmentTest.testObject()
    obj.Zone = testZone
    obj.Actor = testProbe.ref

    obj.allowInteraction = false
    obj.Position = Vector3(1, 1, 2.7f)
    obj.zoneInteractions()
    testProbe.expectNoMessage(max = 500 milliseconds)

    obj.allowInteraction = true
    val msg1 = testProbe.receiveOne(max = 250 milliseconds)
    msg1 match {
      case RespondsToZoneEnvironment.Timer(EnvironmentAttribute.Water, _, _, _) => true
      case _ => assert(InteractsWithZoneEnvironmentTest.fail, "")
    }
  }
}

object InteractsWithZoneEnvironmentTest {
  val fail: Boolean = false

  def testObject(): PlanetSideServerObject with InteractsWithZone = {
    val p = new Player(Avatar(1, "test", PlanetSideEmpire.VS, CharacterSex.Male, 1, CharacterVoice.Mute))
    p.GUID = PlanetSideGUID(1)
    p.Spawn()
    p
  }
}
