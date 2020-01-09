// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.entity.{AssigningGUIDException, NoGUIDException}
import net.psforever.types.{PlanetSideGUID, StalePlanetSideGUID, ValidPlanetSideGUID, Vector3}
import org.specs2.mutable._

class EntityTest extends Specification {
  //both WorldEntity and IdentifiableEntity are components of PlanetSideGameObject
  private class EntityTestClass extends PlanetSideGameObject {
    def Definition : ObjectDefinition = new ObjectDefinition(0) { }
  }

  "PlanetSideGUID" should {
    "construct as valid" in {
      ValidPlanetSideGUID(1).isInstanceOf[PlanetSideGUID] mustEqual true
    }

    "construct as stale" in {
      StalePlanetSideGUID(1).isInstanceOf[PlanetSideGUID] mustEqual true
    }

    "apply construct (as valid)" in {
      val guid = PlanetSideGUID(1)
      guid.isInstanceOf[PlanetSideGUID] mustEqual true
      guid.isInstanceOf[ValidPlanetSideGUID] mustEqual true
      guid.isInstanceOf[StalePlanetSideGUID] mustEqual false
    }

    "valid and stale are equal by guid" in {
      //your linter will complain; let it
      ValidPlanetSideGUID(1) == StalePlanetSideGUID(1) mustEqual true
      ValidPlanetSideGUID(1) == StalePlanetSideGUID(2) mustEqual false
    }

    "valid and stale are pattern-matchable" in {
      val guid1 : PlanetSideGUID = ValidPlanetSideGUID(1)
      val guid2 : PlanetSideGUID = StalePlanetSideGUID(1)
      def getGuid(o : PlanetSideGUID) : PlanetSideGUID = o //distancing the proper type

      getGuid(guid1) match {
        case ValidPlanetSideGUID(1) => ok
        case _ => ko
      }
      getGuid(guid2) match {
        case StalePlanetSideGUID(1) => ok
        case _ => ko
      }
    }
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

    "error while not set" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID must throwA[NoGUIDException]
    }

    "error if set to an invalid GUID before being set to a valid GUID" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID must throwA[NoGUIDException]
      try {
        obj.GUID = StalePlanetSideGUID(1)
        ko
      }
      catch {
        case AssigningGUIDException(_, _, _, _ : StalePlanetSideGUID) => ok
        case _ : Throwable => ko
      }
    }

    "work after valid mutation" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
    }

    "raise complaint about subsequent mutations using a valid GUID" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
      try {
        obj.GUID = ValidPlanetSideGUID(1)
        ko
      }
      catch {
        case AssigningGUIDException(_, _, _, ValidPlanetSideGUID(1)) => ok
        case _ : Throwable => ko
      }
    }

    "ignore subsequent mutations using an invalid GUID" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
      try {
        obj.GUID = StalePlanetSideGUID(1)
        ko
      }
      catch {
        case AssigningGUIDException(_, _, _, _ : StalePlanetSideGUID) => ok
        case _ : Throwable => ko
      }
    }

    "invalidate does nothing by default" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.Invalidate()
      obj.GUID must throwA[NoGUIDException]
    }

    "invalidate changes the nature of the previous valid mutation" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID.isInstanceOf[ValidPlanetSideGUID] mustEqual true
      obj.Invalidate()
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID.isInstanceOf[StalePlanetSideGUID] mustEqual true
    }

    "setting an invalid GUID after invalidating the previous valid mutation still raises complaint" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID.isInstanceOf[ValidPlanetSideGUID] mustEqual true
      obj.Invalidate()
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID.isInstanceOf[StalePlanetSideGUID] mustEqual true
      (obj.GUID = StalePlanetSideGUID(2)) must throwA[AssigningGUIDException]
      obj.GUID mustEqual PlanetSideGUID(1051)
    }

    "setting a valid GUID after invalidating correctly sets the new valid GUID" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID.isInstanceOf[ValidPlanetSideGUID] mustEqual true
      obj.Invalidate()
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID.isInstanceOf[StalePlanetSideGUID] mustEqual true
      obj.GUID = PlanetSideGUID(2)
      obj.GUID mustEqual PlanetSideGUID(2)
      obj.GUID.isInstanceOf[ValidPlanetSideGUID] mustEqual true
    }

    "setting the same valid GUID after invalidating correctly resets the valid GUID" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID.isInstanceOf[ValidPlanetSideGUID] mustEqual true
      obj.Invalidate()
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID.isInstanceOf[StalePlanetSideGUID] mustEqual true
      obj.GUID = PlanetSideGUID(1051)
      obj.GUID mustEqual PlanetSideGUID(1051)
      obj.GUID.isInstanceOf[ValidPlanetSideGUID] mustEqual true
    }

    "report not having a GUID when not set" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.HasGUID mustEqual false
    }

    "report having a GUID when a valid GUID is set" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.HasGUID mustEqual false
      obj.GUID = PlanetSideGUID(1051)
      obj.HasGUID mustEqual true
    }

    "report not having a GUID after invalidating (staleness)" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.HasGUID mustEqual false
      obj.GUID = PlanetSideGUID(1051)
      obj.HasGUID mustEqual true
      obj.Invalidate()
      obj.HasGUID mustEqual false
      //remember that we will still return a GUID in this case
      obj.GUID mustEqual PlanetSideGUID(1051)
    }

    "report having a GUID after setting a valid GUID, after invalidating" in {
      val obj : EntityTestClass = new EntityTestClass
      obj.HasGUID mustEqual false
      obj.GUID = PlanetSideGUID(1051)
      obj.HasGUID mustEqual true
      obj.Invalidate()
      obj.HasGUID mustEqual false
      obj.GUID = PlanetSideGUID(2)
      obj.HasGUID mustEqual true
    }
  }

  "hachCode test" in {
    ValidPlanetSideGUID(1051).hashCode mustEqual ValidPlanetSideGUID(1051).hashCode
    ValidPlanetSideGUID(1051).hashCode mustEqual StalePlanetSideGUID(1051).hashCode
  }
}
