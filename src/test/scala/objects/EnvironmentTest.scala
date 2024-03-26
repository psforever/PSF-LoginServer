// Copyright (c) 2020 PSForever
package objects

import net.psforever.objects.avatar.Avatar
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player, Tool, Vehicle}
import net.psforever.objects.definition.{ObjectDefinition, VehicleDefinition}
import net.psforever.objects.serverobject.environment._
import net.psforever.objects.serverobject.terminals.{Terminal, TerminalDefinition}
import net.psforever.objects.vital.Vitality
import net.psforever.packet.game.objectcreate.ObjectClass
import net.psforever.types._
import org.specs2.mutable.Specification

private class PSGOTest() extends PlanetSideGameObject() {
  override def Definition: ObjectDefinition = null
}

class EnvironmentCollisionTest extends Specification {
  "DeepPlane" should {
    val point: Float = 10f
    val plane = DeepPlane(point)

    "have altitude" in {
      plane.altitude mustEqual point
    }

    "must have interaction that passes" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(0,0,10)
      plane.testInteraction(obj, varDepth = -1) mustEqual true
      obj.Position = Vector3(0,0,9)
      plane.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(0,0,8)
      plane.testInteraction(obj, varDepth =  1) mustEqual true
    }

    "must have interaction that fails" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(0,0,11)
      plane.testInteraction(obj, varDepth = -1) mustEqual false
      obj.Position = Vector3(0,0,10)
      plane.testInteraction(obj, varDepth =  0) mustEqual false
      obj.Position = Vector3(0,0,9)
      plane.testInteraction(obj, varDepth =  1) mustEqual false
    }
  }

  "DeepSquare" should {
    val point: Float = 10f
    val square = DeepSquare(point, 9, 9, 1, 1)

    "must have altitude" in {
      square.altitude mustEqual point
    }

    "must have interaction that passes" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(1,1, 0)
      square.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(1,8, 0)
      square.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(8,8, 0)
      square.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(8,1, 0)
      square.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(1,1,10)
      square.testInteraction(obj, varDepth = -1) mustEqual true
      obj.Position = Vector3(1,1, 9)
      square.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(1,1, 8)
      square.testInteraction(obj, varDepth =  1) mustEqual true
    }

    "must have interaction that fails" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(1,0, 0)
      square.testInteraction(obj, varDepth =  0) mustEqual false
      obj.Position = Vector3(1,9, 0)
      square.testInteraction(obj, varDepth =  0) mustEqual false
      obj.Position = Vector3(0,9, 0)
      square.testInteraction(obj, varDepth =  0) mustEqual false
      obj.Position = Vector3(0,1, 0)
      square.testInteraction(obj, varDepth =  0) mustEqual false
      obj.Position = Vector3(1,1,11)
      square.testInteraction(obj, varDepth = -1) mustEqual false
      obj.Position = Vector3(1,1,10)
      square.testInteraction(obj, varDepth =  0) mustEqual false
      obj.Position = Vector3(1,1, 9)
      square.testInteraction(obj, varDepth =  1) mustEqual false
    }
  }

  "DeepSurface" should {
    val point: Float = 10f
    val surface = DeepSurface(point, 9, 9, 1, 1)

    "must have altitude" in {
      surface.altitude mustEqual point
    }

    "must have interaction that passes" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(1,1,0)
      surface.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(1,8,0)
      surface.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(8,8,0)
      surface.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(8,1,0)
      surface.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(1,1,9)
      surface.testInteraction(obj, varDepth = -1) mustEqual true
      obj.Position = Vector3(1,1,9)
      surface.testInteraction(obj, varDepth =  0) mustEqual true
      obj.Position = Vector3(1,1,9)
      surface.testInteraction(obj, varDepth =  1) mustEqual true
    }

    "must have interaction that fails" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(1,0, 0)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
      obj.Position = Vector3(1,9, 0)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
      obj.Position = Vector3(0,9, 0)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
      obj.Position = Vector3(0,1, 0)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
      obj.Position = Vector3(1,1,11)
      surface.testInteraction(obj, varDepth = -1) mustEqual false
      obj.Position = Vector3(1,1,10)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
    }
  }

  "DeepCircularSurface" should {
    val point: Float = 10f
    val center = Vector3(3, 3, point)
    val surface = DeepCircularSurface(center, 3)

    "must have altitude" in {
      surface.altitude mustEqual point
    }

    "must have interaction that passes" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(3,1,0)
      surface.testInteraction(obj, varDepth = 0) mustEqual true
      obj.Position = Vector3(1,3,0)
      surface.testInteraction(obj, varDepth = 0) mustEqual true
      obj.Position = Vector3(3,5,0)
      surface.testInteraction(obj, varDepth = 0) mustEqual true
      obj.Position = Vector3(5,3,0)
      surface.testInteraction(obj, varDepth = 0) mustEqual true
      obj.Position = Vector3(2,2,9)
      surface.testInteraction(obj, varDepth = -1) mustEqual true
      obj.Position = Vector3(2,2,9)
      surface.testInteraction(obj, varDepth = 0) mustEqual true
      obj.Position = Vector3(2,2,9)
      surface.testInteraction(obj, varDepth = 1) mustEqual true
    }

    "must have interaction that fails" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(3,0, 0)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
      obj.Position = Vector3(0,3, 0)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
      obj.Position = Vector3(3,6, 0)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
      obj.Position = Vector3(6,3, 0)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
      obj.Position = Vector3(2,2,11)
      surface.testInteraction(obj, varDepth = -1) mustEqual false
      obj.Position = Vector3(2,2,10)
      surface.testInteraction(obj, varDepth = 0) mustEqual false
    }
  }
}

class EnvironmentAttributeTest extends Specification {
  "Water" should {
    "interact with drownable object" in {
      EnvironmentAttribute.Water.canInteractWith(
        Vehicle(
          new VehicleDefinition(objectId = ObjectClass.apc_tr) { DrownAtMaxDepth = true }
        )
      ) mustEqual true
    }

    "not interact with object that does not drown" in {
      EnvironmentAttribute.Water.canInteractWith(
        Vehicle(
          new VehicleDefinition(objectId = ObjectClass.apc_tr) { DrownAtMaxDepth = false }
        )
      ) mustEqual false
    }

    "interact with depth-disable object" in {
      EnvironmentAttribute.Water.canInteractWith(
        Vehicle(
          new VehicleDefinition(objectId = ObjectClass.apc_tr) { DisableAtMaxDepth = true }
        )
      ) mustEqual true
    }

    "not interact with object that does not depth-disable" in {
      EnvironmentAttribute.Water.canInteractWith(
        Vehicle(
          new VehicleDefinition(objectId = ObjectClass.apc_tr) { DisableAtMaxDepth = false }
        )
      ) mustEqual false
    }
  }

  "Lava" should {
    "interact with a vital object that is damageable" in {
      val obj = Terminal(GlobalDefinitions.order_terminal)
      obj.isInstanceOf[Vitality] mustEqual true
      obj.asInstanceOf[Vitality].Definition.Damageable mustEqual true
      EnvironmentAttribute.Lava.canInteractWith(obj) mustEqual true
    }

    "not interact with a vital object that is not damageable" in {
      val obj = Terminal(new TerminalDefinition(objectId = 455) {
        def Request(player : Player, msg : Any) : Terminal.Exchange = null
        Damageable = false
      })
      obj.isInstanceOf[Vitality] mustEqual true
      obj.asInstanceOf[Vitality].Definition.Damageable mustEqual false
      EnvironmentAttribute.Lava.canInteractWith(obj) mustEqual false
    }

    "not interact with an object that has no vitality" in {
      val obj = Tool(GlobalDefinitions.suppressor)
      obj.isInstanceOf[Vitality] mustEqual false
      EnvironmentAttribute.Lava.canInteractWith(obj) mustEqual false
    }
  }

  "Death" should {
    "interact with a vital object that is damageable" in {
      val obj = Terminal(GlobalDefinitions.order_terminal)
      obj.isInstanceOf[Vitality] mustEqual true
      obj.asInstanceOf[Vitality].Definition.Damageable mustEqual true
      EnvironmentAttribute.Death.canInteractWith(obj) mustEqual true
    }

    "not interact with a vital object that is not damageable" in {
      val obj = Terminal(new TerminalDefinition(objectId = 455) {
        def Request(player : Player, msg : Any) : Terminal.Exchange = null
        Damageable = false
      })
      obj.isInstanceOf[Vitality] mustEqual true
      obj.asInstanceOf[Vitality].Definition.Damageable mustEqual false
      EnvironmentAttribute.Death.canInteractWith(obj) mustEqual false
    }

    "not interact with an object that has no vitality" in {
      val obj = Tool(GlobalDefinitions.suppressor)
      obj.isInstanceOf[Vitality] mustEqual false
      EnvironmentAttribute.Death.canInteractWith(obj) mustEqual false
    }
  }

  "GantryDenialField" should {
    "interact with players" in {
      val obj = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      obj.Spawn()
      EnvironmentAttribute.GantryDenialField.canInteractWith(obj) mustEqual true
    }

    "not interact with dead players" in {
      val obj = Player(Avatar(0, "test", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      obj.isAlive mustEqual false
      EnvironmentAttribute.GantryDenialField.canInteractWith(obj) mustEqual false
    }

    "not interact with an object that is not a player" in {
      val obj = Tool(GlobalDefinitions.suppressor)
      obj.isInstanceOf[Vitality] mustEqual false
      EnvironmentAttribute.GantryDenialField.canInteractWith(obj) mustEqual false
    }
  }
}

class SeaLevelTest extends Specification {
  "SeaLevel" should {
    val point: Float = 10f
    val plane = DeepPlane(point)
    val level = SeaLevel(point)

    "have altitude (same as DeepPlane)" in {
      plane.altitude mustEqual level.altitude
    }

    "must have interaction that passes (same as DeepPlane)" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(0,0,10)
      plane.testInteraction(obj, varDepth = -1) mustEqual level.testInteraction(obj, varDepth = -1)
      obj.Position = Vector3(0,0, 9)
      plane.testInteraction(obj, varDepth = 0) mustEqual level.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(0,0, 8)
      plane.testInteraction(obj, varDepth = 1) mustEqual level.testInteraction(obj, varDepth = 1)
    }

    "must have interaction that fails (same as DeepPlane)" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(0,0,11)
      plane.testInteraction(obj, varDepth = -1) mustEqual level.testInteraction(obj, varDepth = -1)
      obj.Position = Vector3(0,0,10)
      plane.testInteraction(obj, varDepth = 0) mustEqual level.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(0,0, 9)
      plane.testInteraction(obj, varDepth = 1) mustEqual level.testInteraction(obj, varDepth = 1)
    }
  }
}

class PoolTest extends Specification {
  "Pool" should {
    val point: Float = 10f
    val square = DeepSquare(point, 1, 10, 10, 1)
    val pool = Pool(EnvironmentAttribute.Water, point, 1, 10, 10, 1)

    "have altitude (same as DeepSquare)" in {
      pool.collision.altitude mustEqual square.altitude
    }

    "must have interaction that passes (same as DeepSquare)" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(1,1, 0)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(1,8, 0)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(8,8, 0)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(8,1, 0)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(1,1,10)
      pool.testInteraction(obj, varDepth = -1) mustEqual square.testInteraction(obj, varDepth = -1)
      obj.Position = Vector3(1,1, 9)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(1,1, 8)
      pool.testInteraction(obj, varDepth = 1) mustEqual square.testInteraction(obj, varDepth = 1)
    }

    "must have interaction that fails (same as DeepSquare)" in {
      val obj = new PSGOTest()
      obj.Position = Vector3(1,0, 0)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(1,9, 0)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(0,9, 0)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(0,1, 0)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(1,1,11)
      pool.testInteraction(obj, varDepth = -1) mustEqual square.testInteraction(obj, varDepth = -1)
      obj.Position = Vector3(1,1,10)
      pool.testInteraction(obj, varDepth = 0) mustEqual square.testInteraction(obj, varDepth = 0)
      obj.Position = Vector3(1,1, 9)
      pool.testInteraction(obj, varDepth = 1) mustEqual square.testInteraction(obj, varDepth = 1)
    }
  }
}

class GantryDenialField extends Specification {
  val square: DeepSquare = DeepSquare(0, 1, 10, 10, 1)

  "GantryDenialField" should {
    "always has the environmental attribute of 'GantryDenialField'" in {
      val obj = GantryDenialField(PlanetSideGUID(0), 0, square)
      obj.attribute mustEqual EnvironmentAttribute.GantryDenialField
    }
  }
}

class PieceOfEnvironmentTest extends Specification {
  "PieceOfEnvironment" should {
    import PieceOfEnvironment.testStepIntoInteraction
    val level = SeaLevel(10f)

    "detect entering a critical region" in {
      testStepIntoInteraction(level, new PSGOTest(), Vector3(0,0,9), Vector3(0,0,11), varDepth = 0).contains(true) mustEqual true
    }

    "detect leaving a critical region" in {
      testStepIntoInteraction(level, new PSGOTest(), Vector3(0,0,11), Vector3(0,0,9), varDepth = 0).contains(false) mustEqual true
    }

    "not detect moving outside of a critical region" in {
      testStepIntoInteraction(level, new PSGOTest(), Vector3(0,0,12), Vector3(0,0,11), varDepth = 0).isEmpty mustEqual true
    }

    "not detect moving within a critical region" in {
      testStepIntoInteraction(level, new PSGOTest(), Vector3(0,0,9), Vector3(0,0,8), varDepth = 0).isEmpty mustEqual true
    }
  }
}

object EnvironmentTest { }
