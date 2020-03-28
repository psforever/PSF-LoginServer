// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
import net.psforever.objects.ballistics._
import net.psforever.objects.definition.ProjectileDefinition
import net.psforever.objects.serverobject.mblocker.Locker
import net.psforever.objects.vital.DamageType
import net.psforever.types.{PlanetSideGUID, _}
import org.specs2.mutable.Specification

class ProjectileTest extends Specification {
  val player = Player(Avatar("TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  val fury = Vehicle(GlobalDefinitions.fury)

  "LocalProjectile" should {
    "construct" in {
      val obj = new LocalProjectile() //since they're just placeholders, they only need to construct
      obj.Definition.ObjectId mustEqual 0
      obj.Definition.Name mustEqual "projectile"
    }

    "local projectile range" in {
      Projectile.BaseUID < Projectile.RangeUID mustEqual true
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

  "SourceEntry" should {
    "construct for players" in {
      SourceEntry(player) match {
        case o : PlayerSource =>
          o.Name mustEqual "TestCharacter"
          o.Faction mustEqual PlanetSideEmpire.TR
          o.Seated mustEqual false
          o.ExoSuit mustEqual ExoSuitType.Standard
          o.Health mustEqual 0
          o.Armor mustEqual 0
          o.Definition mustEqual GlobalDefinitions.avatar
          o.Position mustEqual Vector3.Zero
          o.Orientation mustEqual Vector3.Zero
          o.Velocity mustEqual None
        case _ =>
          ko
      }
    }

    "construct for vehicles" in {
      SourceEntry(fury) match {
        case o : VehicleSource =>
          o.Name mustEqual "Fury"
          o.Faction mustEqual PlanetSideEmpire.NEUTRAL
          o.Definition mustEqual GlobalDefinitions.fury
          o.Health mustEqual 650
          o.Shields mustEqual 0
          o.Position mustEqual Vector3.Zero
          o.Orientation mustEqual Vector3.Zero
          o.Velocity mustEqual None
        case _ =>
          ko
      }
    }

    "construct for generic object" in {
      val obj = Locker()
      SourceEntry(obj) match {
        case o : ObjectSource =>
          o.obj mustEqual obj
          o.Name mustEqual "Mb Locker"
          o.Faction mustEqual PlanetSideEmpire.NEUTRAL
          o.Definition mustEqual GlobalDefinitions.mb_locker
          o.Position mustEqual Vector3.Zero
          o.Orientation mustEqual Vector3.Zero
          o.Velocity mustEqual None
        case _ =>
          ko
      }
    }

    "contain timely information" in {
      val obj = Player(Avatar("TestCharacter-alt", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      obj.VehicleSeated = Some(PlanetSideGUID(1))
      obj.Position = Vector3(1.2f, 3.4f, 5.6f)
      obj.Orientation = Vector3(2.1f, 4.3f, 6.5f)
      obj.Velocity = Some(Vector3(1.1f, 2.2f, 3.3f))
      val sobj = SourceEntry(obj)
      obj.VehicleSeated = None
      obj.Position = Vector3.Zero
      obj.Orientation = Vector3.Zero
      obj.Velocity = None
      obj.ExoSuit = ExoSuitType.Agile

      sobj match {
        case o : PlayerSource =>
          o.Name mustEqual "TestCharacter-alt"
          o.Faction mustEqual PlanetSideEmpire.TR
          o.Seated mustEqual true
          o.ExoSuit mustEqual ExoSuitType.Standard
          o.Definition mustEqual GlobalDefinitions.avatar
          o.Position mustEqual Vector3(1.2f, 3.4f, 5.6f)
          o.Orientation mustEqual Vector3(2.1f, 4.3f, 6.5f)
          o.Velocity mustEqual Some(Vector3(1.1f, 2.2f, 3.3f))
        case _ =>
          ko
      }
    }
  }

  "Projectile" should {
    val beamer_def = GlobalDefinitions.beamer
    val beamer_wep = Tool(beamer_def)
    val firemode = beamer_wep.FireMode
    val projectile = beamer_wep.Projectile

    "construct" in {
      val obj = Projectile(beamer_wep.Projectile, beamer_wep.Definition, beamer_wep.FireMode, PlayerSource(player), beamer_def.ObjectId, Vector3(1.2f, 3.4f, 5.6f), Vector3(0.2f, 0.4f, 0.6f))
      obj.profile mustEqual projectile
      obj.tool_def mustEqual beamer_def
      obj.fire_mode mustEqual firemode
      obj.owner match {
        case _ : PlayerSource =>
          ok
        case _ =>
          ko
      }
      obj.attribute_to mustEqual obj.tool_def.ObjectId
      obj.shot_origin mustEqual Vector3(1.2f, 3.4f, 5.6f)
      obj.shot_angle mustEqual Vector3(0.2f, 0.4f, 0.6f)
      obj.fire_time <= System.nanoTime mustEqual true
      obj.isResolved mustEqual false
    }

    "construct (different attribute)" in {
      val obj1 = Projectile(beamer_wep.Projectile, beamer_wep.Definition, beamer_wep.FireMode, player, Vector3(1.2f, 3.4f, 5.6f), Vector3(0.2f, 0.4f, 0.6f))
      obj1.attribute_to mustEqual obj1.tool_def.ObjectId

      val obj2 = Projectile(beamer_wep.Projectile, beamer_wep.Definition, beamer_wep.FireMode, PlayerSource(player), 65, Vector3(1.2f, 3.4f, 5.6f), Vector3(0.2f, 0.4f, 0.6f))
      obj2.attribute_to == obj2.tool_def.ObjectId mustEqual false
      obj2.attribute_to mustEqual 65
    }

    "resolve" in {
      val obj = Projectile(projectile, beamer_def, firemode, PlayerSource(player), beamer_def.ObjectId, Vector3.Zero, Vector3.Zero)
      obj.isResolved mustEqual false
      obj.isMiss mustEqual false

      obj.Resolve()
      obj.isResolved mustEqual true
      obj.isMiss mustEqual false
    }

    "missed" in {
      val obj = Projectile(projectile, beamer_def, firemode, PlayerSource(player), beamer_def.ObjectId, Vector3.Zero, Vector3.Zero)
      obj.isResolved mustEqual false
      obj.isMiss mustEqual false

      obj.Miss()
      obj.isResolved mustEqual true
      obj.isMiss mustEqual true
    }
  }

  "ResolvedProjectile" should {
    val beamer_wep = Tool(GlobalDefinitions.beamer)
    val p_source = PlayerSource(player)
    val player2 = Player(Avatar("TestTarget", PlanetSideEmpire.NC, CharacterGender.Female, 1, CharacterVoice.Mute))
    val p2_source = PlayerSource(player2)
    val projectile = Projectile(beamer_wep.Projectile, GlobalDefinitions.beamer, beamer_wep.FireMode, p_source, GlobalDefinitions.beamer.ObjectId, Vector3.Zero, Vector3.Zero)
    val fury_dm = fury.DamageModel

    "construct" in {
      val obj = ResolvedProjectile(ProjectileResolution.Hit, projectile, PlayerSource(player2), fury_dm, Vector3(1.2f, 3.4f, 5.6f))
      obj.resolution mustEqual ProjectileResolution.Hit
      obj.projectile mustEqual projectile
      obj.target mustEqual p2_source
      obj.damage_model mustEqual fury.DamageModel
      obj.hit_pos mustEqual Vector3(1.2f, 3.4f, 5.6f)
    }
  }
}
