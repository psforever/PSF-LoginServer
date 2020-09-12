package objects

import net.psforever.objects.{LocalLockerItem, LocalProjectile}
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable.Specification

class LocalTest extends Specification {
  "LocalProjectile" should {
    "construct" in {
      val obj = new LocalProjectile() //since they're just placeholders, they only need to construct
      obj.Definition.ObjectId mustEqual 0
      obj.Definition.Name mustEqual "projectile"
    }
  }

  "LocalLockerItem" should {
    "construct" in {
      val obj = new LocalLockerItem() //since they're just placeholders, they only need to construct
      obj.Faction mustEqual PlanetSideEmpire.NEUTRAL
      obj.Definition.ObjectId mustEqual 0
      obj.Definition.Name mustEqual "locker-equipment"
    }
  }
}