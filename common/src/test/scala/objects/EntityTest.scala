// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.entity.NoGUIDException
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3
import org.specs2.mutable._

class EntityTest extends Specification {
  //both WorldEntity and IdentifiableEntity are components of PlanetSideGameObject
  private class EntityTestClass extends PlanetSideGameObject {
    def Definition : ObjectDefinition = new ObjectDefinition(0) { }
  }

  "SimpleWorldEntity" should {
    "construct" in {
      new EntityTestClass()
      ok
    }

    "initialize" in {
      val obj : EntityTestClass = new EntityTestClass()
      obj.Position mustEqual Vector3(0f, 0f, 0f)
      obj.Orientation mustEqual Vector3(0f, 0f, 0f)
      obj.Velocity.isEmpty mustEqual true
    }

    "mutate and access" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.Position = Vector3(1f, 1f, 1f)
      obj.Orientation = Vector3(2f, 2f, 2f)
      obj.Velocity = Vector3(3f, 3f, 3f)

      obj.Position mustEqual Vector3(1f, 1f, 1f)
      obj.Orientation mustEqual Vector3(2f, 2f, 2f)
      obj.Velocity.contains(Vector3(3f, 3f, 3f)) mustEqual true
    }

    "clamp Orientation" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.Orientation = Vector3(-1f, 361f, -0f)
      obj.Orientation mustEqual Vector3(359f, 1f, 0f)
    }

    "is moving (at all)" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.Velocity.isEmpty mustEqual true
      obj.isMoving mustEqual false

      obj.Velocity = Vector3.Zero
      obj.isMoving mustEqual false
      obj.Velocity = Vector3(1,0,0)
      obj.isMoving mustEqual true
      obj.Velocity = None
      obj.isMoving mustEqual false
    }

    "is moving (Vector3 comparison)" in {
      val obj : EntityTestClass = new EntityTestClass
      val test1 = Vector3(1,0,0)
      val test2 = Vector3(2,0,0)
      obj.Velocity.isEmpty mustEqual true
      obj.isMoving mustEqual false
      obj.isMoving(test1) mustEqual false
      obj.isMoving(test2) mustEqual false

      obj.Velocity = Vector3(1,0,0)
      obj.isMoving(test1) mustEqual true
      obj.isMoving(test2) mustEqual false
      obj.Velocity = Vector3(3,0,0)
      obj.isMoving(test1) mustEqual true
      obj.isMoving(test2) mustEqual true
      obj.Velocity = Vector3(1,1,0)
      obj.isMoving(test1) mustEqual true
      obj.isMoving(test2) mustEqual false
    }

    "is moving (Float comparison)" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.Velocity.isEmpty mustEqual true
      obj.isMoving mustEqual false
      obj.isMoving(1) mustEqual false
      obj.isMoving(2) mustEqual false
      obj.isMoving(4) mustEqual false

      obj.Velocity = Vector3(1,0,0)
      obj.isMoving(1) mustEqual true
      obj.isMoving(2) mustEqual false
      obj.isMoving(4) mustEqual false
      obj.Velocity = Vector3(3,0,0)
      obj.isMoving(1) mustEqual true
      obj.isMoving(2) mustEqual true
      obj.isMoving(4) mustEqual true
      obj.Velocity = Vector3(1,1,1)
      obj.isMoving(1) mustEqual true
      obj.isMoving(2) mustEqual true
      obj.isMoving(4) mustEqual false
    }
  }

  "IdentifiableEntity" should {
    "construct" in {
      new EntityTestClass()
      ok
    }

    "error while unset" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID must throwA[NoGUIDException]
    }

    "work after mutation" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
    }

    "work after multiple mutations" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID = PlanetSideGUID(30052)
      obj.GUID mustEqual PlanetSideGUID(30052)
      obj.GUID = PlanetSideGUID(62)
      obj.GUID mustEqual PlanetSideGUID(62)
    }

    "invalidate and resume error" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.Invalidate()
      obj.GUID must throwA[NoGUIDException]
    }
  }
}
