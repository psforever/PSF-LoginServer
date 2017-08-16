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
      obj.Velocity mustEqual None
    }

    "mutate and access" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.Position = Vector3(1f, 1f, 1f)
      obj.Orientation = Vector3(2f, 2f, 2f)
      obj.Velocity = Vector3(3f, 3f, 3f)

      obj.Position mustEqual Vector3(1f, 1f, 1f)
      obj.Orientation mustEqual Vector3(2f, 2f, 2f)
      obj.Velocity mustEqual Some(Vector3(3f, 3f, 3f))
    }

    "clamp Orientation" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.Orientation = Vector3(-1f, 361f, -0f)
      obj.Orientation mustEqual Vector3(359f, 1f, 0f)
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
