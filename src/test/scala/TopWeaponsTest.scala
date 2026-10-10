// Copyright (c) 2025 PSForever
import net.psforever.actors.api.TopWeapons
import net.psforever.objects.GlobalDefinitions
import org.specs2.mutable._

/**
  * The gun/vehicle split, which is the part of the weapon leaderboards that can be wrong quietly.
  *
  * A kill record names the weapon and never says what the killer was riding in, so vehicle kills are
  * credited by asking each vehicle what it is armed with. If that mapping is empty or wrong, the
  * vehicle board simply reads zero and the guns board fills up with cannon names -- no error, no
  * failure, just a leaderboard that is silently lying. These assert it is neither.
  */
class TopWeaponsTest extends Specification {

  "Weapon ownership" should {

    "know which vehicle mounts which weapon" in {
      TopWeapons.weaponToVehicle must not(beEmpty)
    }

    "credit a vehicle's own armament to that vehicle" in {
      // Taken from the definition rather than hard-coded, so this follows the vehicle if it is
      // ever rearmed -- the point is that the mapping resolves, not that a particular id does.
      val liberatorWeapons = GlobalDefinitions.liberator.Weapons.values.map(_.ObjectId).toList
      liberatorWeapons must not(beEmpty)
      liberatorWeapons.foreach { id =>
        TopWeapons.weaponToVehicle.get(id).map(_._2) mustEqual Some("Liberator")
      }
      ok
    }

    "keep an infantry weapon off the vehicle board" in {
      // 845 (suppressor) and 140 (beamer) are what real kill rows in this project look like.
      TopWeapons.weaponToVehicle.get(845) must beNone
      TopWeapons.weaponToVehicle.get(140) must beNone
    }

    "split kills into the two boards" in {
      val cannon = GlobalDefinitions.liberator.Weapons.values.map(_.ObjectId).head
      val rows   = Seq(845 -> 10, 140 -> 3, cannon -> 7)

      val (guns, vehicles) = TopWeapons.split(rows, limit = 15)

      // Handheld weapons stay guns, ordered by kills.
      guns.map(_.weaponId) mustEqual Seq(845, 140)
      guns.map(_.kills) mustEqual Seq(10, 3)
      // The cannon is credited to the Liberator rather than appearing as a gun.
      guns.exists(_.weaponId == cannon) mustEqual false
      vehicles.map(_.name) mustEqual Seq("Liberator")
      vehicles.head.kills mustEqual 7
    }

    "sum a vehicle's weapons into one row" in {
      // Every weapon a Liberator carries counts toward the Liberator, not toward one row each.
      val weapons = GlobalDefinitions.liberator.Weapons.values.map(_.ObjectId).toList.distinct
      val rows    = weapons.map(id => id -> 2)

      val (_, vehicles) = TopWeapons.split(rows, limit = 15)

      vehicles.size mustEqual 1
      vehicles.head.kills mustEqual weapons.size * 2
    }

    "honour the row limit on both boards" in {
      val rows = (1000 until 1100).map(id => id -> id)
      val (guns, vehicles) = TopWeapons.split(rows, limit = 5)
      guns.size must beLessThanOrEqualTo(5)
      vehicles.size must beLessThanOrEqualTo(5)
    }
  }
}
