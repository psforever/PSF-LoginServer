package objects

import akka.testkit.TestProbe
import base.ActorTest
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject
import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.environment.interaction.{EscapeFromEnvironment, InteractWithEnvironment, InteractingWithEnvironment}
import net.psforever.objects.vital.{Vitality, VitalityDefinition}
import net.psforever.objects.zones.{InteractsWithZone, Zone, ZoneMap}
import net.psforever.types.{PlanetSideEmpire, Vector3}

import scala.concurrent.duration._

class InteractsWithZoneEnvironmentTest extends ActorTest {
  val pool1 = Pool(EnvironmentAttribute.Water, DeepSquare(-1, 10, 10, 0, 0))
  val pool2 = Pool(EnvironmentAttribute.Water, DeepSquare(-1, 10, 15, 5, 10))
  val pool3 = Pool(EnvironmentAttribute.Lava, DeepSquare(-1, 15, 10, 10, 5))
  val testZone = {
    val testMap = new ZoneMap(name = "test-map") {
      environment = List(pool1, pool2, pool3)
    }
    new Zone("test-zone", testMap, zoneNumber = 0)
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

      assert(obj.Position == Vector3.Zero)
      obj.zoneInteractions()
      testProbe.expectNoMessage(max = 500 milliseconds)
    }

    "acknowledge interaction when moved into the critical region of a registered environment object (just once)" in {
      val testProbe = TestProbe()
      val obj = InteractsWithZoneEnvironmentTest.testObject()
      obj.Zone = testZone
      obj.Actor = testProbe.ref

      obj.Position = Vector3(1,1,-2)
      obj.zoneInteractions()
      val msg = testProbe.receiveOne(max = 250 milliseconds)
      assert(
        msg match {
          case InteractingWithEnvironment(b, _) => (b eq pool1)
          case _ => false
        }
      )
      obj.zoneInteractions()
      testProbe.expectNoMessage(max = 500 milliseconds)
    }

    "acknowledge ceasation of interaction when moved out of a previous occupied the critical region (just once)" in {
      val testProbe = TestProbe()
      val obj = InteractsWithZoneEnvironmentTest.testObject()
      obj.Zone = testZone
      obj.Actor = testProbe.ref

      obj.Position = Vector3(1,1,-2)
      obj.zoneInteractions()
      val msg1 = testProbe.receiveOne(max = 250 milliseconds)
      assert(
        msg1 match {
          case InteractingWithEnvironment(b, _) => (b eq pool1)
          case _ => false
        }
      )

      obj.Position = Vector3(1,1,1)
      obj.zoneInteractions()
      val msg2 = testProbe.receiveOne(max = 250 milliseconds)
      assert(
        msg2 match {
          case EscapeFromEnvironment(b, _) => (b eq pool1)
          case _ => false
        }
      )
      obj.zoneInteractions()
      testProbe.expectNoMessage(max = 500 milliseconds)
    }

    "transition between two different critical regions when the regions that the same attribute" in {
      val testProbe = TestProbe()
      val obj = InteractsWithZoneEnvironmentTest.testObject()
      obj.Zone = testZone
      obj.Actor = testProbe.ref

      obj.Position = Vector3(7,7,-2)
      obj.zoneInteractions()
      val msg1 = testProbe.receiveOne(max = 250 milliseconds)
      assert(
        msg1 match {
          case InteractingWithEnvironment(b, _) => (b eq pool1)
          case _ => false
        }
      )

      obj.Position = Vector3(12,7,-2)
      obj.zoneInteractions()
      val msg2 = testProbe.receiveOne(max = 250 milliseconds)
      assert(
        msg2 match {
          case InteractingWithEnvironment(b, _) => (b eq pool1)
          case _ => false
        }
      )
      assert(pool1.attribute == pool2.attribute)
    }

    "transition between two different critical regions when the regions have different attributes" in {
      val testProbe = TestProbe()
      val obj = InteractsWithZoneEnvironmentTest.testObject()
      obj.Zone = testZone
      obj.Actor = testProbe.ref

      obj.Position = Vector3(7,7,-2)
      obj.zoneInteractions()
      val msg1 = testProbe.receiveOne(max = 250 milliseconds)
      assert(
        msg1 match {
          case InteractingWithEnvironment(b, _) => (b eq pool1)
          case _ => false
        }
      )

      obj.Position = Vector3(7,12,-2)
      obj.zoneInteractions()
      val msgs = testProbe.receiveN(2, max = 250 milliseconds)
      assert(
        msgs.head match {
          case EscapeFromEnvironment(b, _) => (b eq pool1)
          case _ => false
        }
      )
      assert(
        msgs(1) match {
          case InteractingWithEnvironment(b, _) => (b eq pool1)
          case _ => false
        }
      )
      assert(pool1.attribute != pool3.attribute)
    }
  }

  "when interactions are disallowed, end any current interaction" in {
    val testProbe = TestProbe()
    val obj = InteractsWithZoneEnvironmentTest.testObject()
    obj.Zone = testZone
    obj.Actor = testProbe.ref

    obj.Position = Vector3(1,1,-2)
    obj.zoneInteractions()
    val msg1 = testProbe.receiveOne(max = 250 milliseconds)
    assert(
      msg1 match {
        case InteractingWithEnvironment(b, _) => (b eq pool1)
        case _ => false
      }
    )

    obj.allowInteraction = false
    val msg2 = testProbe.receiveOne(max = 250 milliseconds)
    msg2 match {
      case EscapeFromEnvironment(b, _) => (b eq pool1)
      case _ => assert( false)
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
    obj.Position = Vector3(1,1,-2)
    obj.zoneInteractions()
    testProbe.expectNoMessage(max = 500 milliseconds)

    obj.allowInteraction = true
    val msg1 = testProbe.receiveOne(max = 250 milliseconds)
    assert(
      msg1 match {
        case InteractingWithEnvironment(b, _) => (b eq pool1)
        case _ => false
      }
    )
  }
}

object InteractsWithZoneEnvironmentTest {
  def testObject(): PlanetSideServerObject with InteractsWithZone = {
    new PlanetSideServerObject
      with InteractsWithZone {
      interaction(new InteractWithEnvironment())
      def Faction: PlanetSideEmpire.Value = PlanetSideEmpire.VS
      def DamageModel = null
      def Definition: ObjectDefinition with VitalityDefinition = new ObjectDefinition(objectId = 0) with VitalityDefinition {
        Damageable = true
        DrownAtMaxDepth = true
      }
    }
  }
}
