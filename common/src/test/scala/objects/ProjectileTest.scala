// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.{GlobalDefinitions, LocalProjectile, Tool}
import net.psforever.objects.ballistics.{DamageType, Projectile, ProjectileResolution, Projectiles}
import net.psforever.objects.definition.ProjectileDefinition
import net.psforever.types.Vector3
import org.specs2.mutable.Specification

class ProjectileTest extends Specification {
  "LocalProjectile" should {
    "construct" in {
      val obj = new LocalProjectile() //since they're just placeholders, they only need to construct
      obj.Definition.ObjectId mustEqual 0
      obj.Definition.Name mustEqual "projectile"
    }
  }

  "ProjectileDefinition" should {
    "define (default)" in {
      val obj = new ProjectileDefinition(31) //9mmbullet_projectile

      obj.ProjectileType mustEqual Projectiles.bullet_9mm_projectile
      obj.ObjectId mustEqual 31
      obj.Damage0 mustEqual 0
      obj.Damage1 mustEqual 0
      obj.Damage2 mustEqual 0
      obj.Damage3 mustEqual 0
      obj.Damage4 mustEqual 0
      obj.Acceleration mustEqual 0
      obj.AccelerationUntil mustEqual 0f
      obj.ProjectileDamageType mustEqual DamageType.None
      obj.ProjectileDamageTypeSecondary mustEqual DamageType.None
      obj.DegradeDelay mustEqual 1f
      obj.DegradeMultiplier mustEqual 1f
      obj.InitialVelocity mustEqual 1
      obj.Lifespan mustEqual 1f
      obj.DamageAtEdge mustEqual 1f
      obj.DamageRadius mustEqual 1f
      obj.UseDamage1Subtract mustEqual false
    }

    "define (custom)" in {
      val obj = new ProjectileDefinition(31) //9mmbullet_projectile
      obj.Damage0 = 2
      obj.Damage1 = 4
      obj.Damage2 = 8
      obj.Damage3 = 16
      obj.Damage4 = 32
      obj.Acceleration = 5
      obj.AccelerationUntil = 5.5f
      obj.ProjectileDamageType = DamageType.Splash
      obj.ProjectileDamageTypeSecondary = DamageType.Radiation
      obj.DegradeDelay = 11.1f
      obj.DegradeMultiplier = 22.2f
      obj.InitialVelocity = 50
      obj.Lifespan = 11.2f
      obj.DamageAtEdge = 3f
      obj.DamageRadius = 3f
      obj.UseDamage1Subtract = true

      obj.Damage0 mustEqual 2
      obj.Damage1 mustEqual 4
      obj.Damage2 mustEqual 8
      obj.Damage3 mustEqual 16
      obj.Damage4 mustEqual 32
      obj.Acceleration mustEqual 5
      obj.AccelerationUntil mustEqual 5.5f
      obj.ProjectileDamageType mustEqual DamageType.Splash
      obj.ProjectileDamageTypeSecondary mustEqual DamageType.Radiation
      obj.DegradeDelay mustEqual 11.1f
      obj.DegradeMultiplier mustEqual 22.2f
      obj.InitialVelocity mustEqual 50
      obj.Lifespan mustEqual 11.2f
      obj.DamageAtEdge mustEqual 3f
      obj.DamageRadius mustEqual 3f
      obj.UseDamage1Subtract mustEqual true
    }

    "define (failure)" in {
      Projectiles(31) mustEqual Projectiles.bullet_9mm_projectile
      try {
        ProjectileDefinition(Projectiles.bullet_9mm_projectile) //passes
      }
      catch {
        case _ : NoSuchElementException =>
          ko
      }

      Projectiles(2) must throwA[NoSuchElementException]
      new ProjectileDefinition(2) must throwA[NoSuchElementException]
    }

    "cascade damage values" in {
      val obj = new ProjectileDefinition(31) //9mmbullet_projectile
      obj.Damage4 = 32
      obj.Damage3 = 16
      obj.Damage2 = 8
      obj.Damage1 = 4
      obj.Damage0 = 2

      //initial
      obj.Damage4 mustEqual 32
      obj.Damage3 mustEqual 16
      obj.Damage2 mustEqual 8
      obj.Damage1 mustEqual 4
      obj.Damage0 mustEqual 2
      //negate Damage4
      obj.Damage4 = None
      obj.Damage4 mustEqual 16
      obj.Damage3 mustEqual 16
      obj.Damage2 mustEqual 8
      obj.Damage1 mustEqual 4
      obj.Damage0 mustEqual 2
      //negate Damage3
      obj.Damage3 = None
      obj.Damage4 mustEqual 8
      obj.Damage3 mustEqual 8
      obj.Damage2 mustEqual 8
      obj.Damage1 mustEqual 4
      obj.Damage0 mustEqual 2
      //negate Damage2
      obj.Damage2 = None
      obj.Damage4 mustEqual 4
      obj.Damage3 mustEqual 4
      obj.Damage2 mustEqual 4
      obj.Damage1 mustEqual 4
      obj.Damage0 mustEqual 2
      //negate Damage1
      obj.Damage1 = None
      obj.Damage4 mustEqual 2
      obj.Damage3 mustEqual 2
      obj.Damage2 mustEqual 2
      obj.Damage1 mustEqual 2
      obj.Damage0 mustEqual 2
      //negate Damage0
      obj.Damage0 = None
      obj.Damage4 mustEqual 0
      obj.Damage3 mustEqual 0
      obj.Damage2 mustEqual 0
      obj.Damage1 mustEqual 0
      obj.Damage0 mustEqual 0
      //set Damage3, set Damage0
      obj.Damage3 = 13
      obj.Damage0 = 7
      obj.Damage4 mustEqual 13
      obj.Damage3 mustEqual 13
      obj.Damage2 mustEqual 7
      obj.Damage1 mustEqual 7
      obj.Damage0 mustEqual 7
    }
  }

  "Projectile" should {
    "construct" in {
      val beamer_wep = Tool(GlobalDefinitions.beamer)
      val projectile = beamer_wep.Projectile
      val obj = Projectile(projectile, beamer_wep.Definition, Vector3(1.2f, 3.4f, 5.6f), Vector3(0.2f, 0.4f, 0.6f))

      obj.profile mustEqual beamer_wep.Projectile
      obj.tool_def mustEqual GlobalDefinitions.beamer
      obj.shot_origin mustEqual Vector3(1.2f, 3.4f, 5.6f)
      obj.shot_angle mustEqual Vector3(0.2f, 0.4f, 0.6f)
      obj.resolution mustEqual ProjectileResolution.Unresolved
      obj.fire_time <= System.nanoTime mustEqual true
      obj.hit_time mustEqual 0
    }

    "resolve" in {
      val beamer_wep = Tool(GlobalDefinitions.beamer)
      val projectile = beamer_wep.Projectile
      val obj = Projectile(projectile, beamer_wep.Definition, Vector3(1.2f, 3.4f, 5.6f), Vector3(0.2f, 0.4f, 0.6f))
      val obj2 = obj.Resolve(ProjectileResolution.MissedShot)

      obj.resolution mustEqual ProjectileResolution.Unresolved
      obj.fire_time <= System.nanoTime mustEqual true
      obj.hit_time mustEqual 0
      obj2.resolution mustEqual ProjectileResolution.MissedShot
      obj2.fire_time == obj.fire_time mustEqual true
      obj2.hit_time <= System.nanoTime mustEqual true
      obj2.fire_time <= obj2.hit_time mustEqual true
    }

    "resolve, with coordinates" in {
      val beamer_wep = Tool(GlobalDefinitions.beamer)
      val projectile = beamer_wep.Projectile
      val obj = Projectile(projectile, beamer_wep.Definition, Vector3(1.2f, 3.4f, 5.6f), Vector3(0.2f, 0.4f, 0.6f))
      val obj2 = obj.Resolve(Vector3(7.2f, 8.4f, 9.6f), Vector3(1.2f, 1.4f, 1.6f), ProjectileResolution.Resolved)

      obj.resolution mustEqual ProjectileResolution.Unresolved
      obj.current.Position mustEqual Vector3.Zero
      obj.current.Orientation mustEqual Vector3.Zero
      obj2.resolution mustEqual ProjectileResolution.Resolved
      obj2.current.Position mustEqual Vector3(7.2f, 8.4f, 9.6f)
      obj2.current.Orientation mustEqual Vector3(1.2f, 1.4f, 1.6f)
    }
  }
}
