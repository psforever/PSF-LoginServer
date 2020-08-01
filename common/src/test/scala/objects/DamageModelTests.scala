// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects._
import net.psforever.objects.vital.damage.{DamageCalculations, DamageModifiers, DamageProfile}
import DamageCalculations._
import net.psforever.objects.vital.resistance.ResistanceCalculations
import ResistanceCalculations._
import net.psforever.objects.vital.resolution.ResolutionCalculations
import ResolutionCalculations._
import net.psforever.objects.ballistics._
import net.psforever.objects.definition.{ProjectileDefinition, VehicleDefinition}
import net.psforever.objects.vital.{DamageType, Vitality}
import net.psforever.packet.game.objectcreate.ObjectClass
import net.psforever.types._
import org.specs2.mutable.Specification
import net.psforever.objects.avatar.Avatar

class DamageCalculationsTests extends Specification {
  "DamageCalculations" should {
    val wep        = GlobalDefinitions.galaxy_gunship_cannon
    val wep_fmode  = Tool(wep).FireMode
    val wep_prof   = wep_fmode.Add
    val proj       = DamageModelTests.projectile
    val proj_prof  = proj.asInstanceOf[DamageProfile]
    val player     = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
    val projectile = Projectile(proj, wep, wep_fmode, player, Vector3(2, 2, 0), Vector3.Zero)
    val target     = Vehicle(GlobalDefinitions.fury)
    target.Position = Vector3(10, 0, 0)
    val resprojectile = ResolvedProjectile(
      ProjectileResolution.Splash,
      projectile,
      SourceEntry(target),
      target.DamageModel,
      Vector3(15, 0, 0)
    )

    "extract no damage numbers" in {
      AgainstNothing(proj_prof) mustEqual 0
    }

    "extract damage against exosuit target" in {
      AgainstExoSuit(proj_prof) == proj_prof.Damage0 mustEqual true
    }

    "extract damage against MAX target" in {
      AgainstMaxSuit(proj_prof) == proj_prof.Damage3 mustEqual true
    }

    "extract damage against vehicle target" in {
      AgainstVehicle(proj_prof) == proj_prof.Damage1 mustEqual true
    }

    "extract damage against aircraft target" in {
      AgainstAircraft(proj_prof) == proj_prof.Damage2 mustEqual true
    }

    "extract damage against battleframe robotics" in {
      AgainstBFR(proj_prof) == proj_prof.Damage4 mustEqual true
    }

    "no degrade damage modifier" in {
      DamageModifiers.SameHit.Calculate(100, resprojectile) mustEqual 100
    }

    "degrade over distance damage modifier (no degrade)" in {
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3(10, 0, 0)
      )
      DamageModifiers.DistanceDegrade.Calculate(100, resprojectile2) == 100 mustEqual true
    }

    "degrade over distance damage modifier (some degrade)" in {
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3(100, 0, 0)
      )
      val damage = DamageModifiers.DistanceDegrade.Calculate(100, resprojectile2)
      damage < 100 && damage > 0 mustEqual true
    }

    "degrade over distance damage modifier (zero'd)" in {
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3(1000, 0, 0)
      )
      DamageModifiers.DistanceDegrade.Calculate(100, resprojectile2) == 0 mustEqual true
    }

    "degrade at radial distance damage modifier (no degrade)" in {
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3(10, 0, 0)
      )
      DamageModifiers.RadialDegrade.Calculate(100, resprojectile2) == 100 mustEqual true
    }

    "degrade at radial distance damage modifier (some degrade)" in {
      val damage = DamageModifiers.RadialDegrade.Calculate(100, resprojectile)
      damage < 100 && damage > 0 mustEqual true
    }

    "degrade at radial distance damage modifier (zero'd)" in {
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3(1000, 0, 0)
      )
      DamageModifiers.RadialDegrade.Calculate(100, resprojectile2) == 0 mustEqual true
    }

    "lash degrade (no lash; too close)" in {
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Lash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3(5, 0, 0) //compared to Vector3(2, 2, 0)
      )
      DamageModifiers.Lash.Calculate(100, resprojectile2) == 0 mustEqual true
    }

    "lash degrade (lash)" in {
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Lash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3(20, 0, 0)
      )
      val damage = DamageModifiers.Lash.Calculate(100, resprojectile2)
      damage < 100 && damage > 0 mustEqual true
    }

    "lash degrade (no lash; too far)" in {
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Lash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3(1000, 0, 0)
      )
      DamageModifiers.Lash.Calculate(100, resprojectile2) == 0 mustEqual true
    }

    "extract a complete damage profile" in {
      val result1 = DamageModifiers.RadialDegrade.Calculate(
        AgainstVehicle(proj_prof) + AgainstVehicle(wep_prof),
        resprojectile
      )
      val result2 = DamageCalculations.DamageWithModifiers(AgainstVehicle, resprojectile)
      result1 mustEqual result2
    }
  }
}

class ResistanceCalculationsTests extends Specification {
  val wep        = GlobalDefinitions.galaxy_gunship_cannon
  val wep_fmode  = Tool(wep).FireMode
  val proj       = DamageModelTests.projectile
  val player     = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  val projectile = Projectile(proj, wep, wep_fmode, player, Vector3(2, 2, 0), Vector3.Zero)

  "ResistanceCalculations" should {
    "ignore all targets" in {
      val target = Vehicle(GlobalDefinitions.fury)
      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )
      InvalidTarget(resprojectile).isFailure mustEqual true
    }

    "discern standard infantry targets" in {
      val target = player
      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )
      ValidInfantryTarget(resprojectile).isSuccess mustEqual true
      ValidMaxTarget(resprojectile).isSuccess mustEqual false
      ValidVehicleTarget(resprojectile).isSuccess mustEqual false
      ValidAircraftTarget(resprojectile).isSuccess mustEqual false
    }

    "discern mechanized infantry targets" in {
      val target = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      target.ExoSuit = ExoSuitType.MAX
      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )
      ValidInfantryTarget(resprojectile).isSuccess mustEqual false
      ValidMaxTarget(resprojectile).isSuccess mustEqual true
      ValidVehicleTarget(resprojectile).isSuccess mustEqual false
      ValidAircraftTarget(resprojectile).isSuccess mustEqual false
    }

    "discern ground vehicle targets" in {
      val target = Vehicle(GlobalDefinitions.fury)
      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )
      ValidInfantryTarget(resprojectile).isSuccess mustEqual false
      ValidMaxTarget(resprojectile).isSuccess mustEqual false
      ValidVehicleTarget(resprojectile).isSuccess mustEqual true
      ValidAircraftTarget(resprojectile).isSuccess mustEqual false
    }

    "discern flying vehicle targets" in {
      val target = Vehicle(GlobalDefinitions.mosquito)
      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )
      ValidInfantryTarget(resprojectile).isSuccess mustEqual false
      ValidMaxTarget(resprojectile).isSuccess mustEqual false
      ValidVehicleTarget(resprojectile).isSuccess mustEqual false
      ValidAircraftTarget(resprojectile).isSuccess mustEqual true
    }

    "extract no resistance values" in {
      NoResistExtractor(SourceEntry(player)) mustEqual 0
    }

    "extract resistance values from exo-suit" in {
      val pSource = PlayerSource(player)
      ExoSuitDirectExtractor(pSource) mustEqual 4
      ExoSuitSplashExtractor(pSource) mustEqual 15
      ExoSuitAggravatedExtractor(pSource) mustEqual 8
      ExoSuitRadiationExtractor(pSource) mustEqual 0
    }

    "extract resistance values from vehicle" in {
      val vSource = VehicleSource(Vehicle(GlobalDefinitions.fury))
      VehicleDirectExtractor(vSource) mustEqual 0
      VehicleSplashExtractor(vSource) mustEqual 0
      VehicleAggravatedExtractor(vSource) mustEqual 0
      VehicleRadiationExtractor(vSource) mustEqual 0
    }
  }
}

class ResolutionCalculationsTests extends Specification {
  val wep       = GlobalDefinitions.galaxy_gunship_cannon
  val wep_fmode = Tool(wep).FireMode
  val proj      = DamageModelTests.projectile
  val player    = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val projectile = Projectile(proj, wep, wep_fmode, player, Vector3(2, 2, 0), Vector3.Zero)

  "ResolutionCalculations" should {
    "calculate no damage" in {
      val target = player
      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target),
        target.DamageModel,
        Vector3.Zero
      )
      ResolutionCalculations.NoDamage(resprojectile)(50, 50) mustEqual 0
    }

    "calculate no infantry damage for vehicles" in {
      val target1 = Vehicle(GlobalDefinitions.fury) //!
      val resprojectile1 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target1),
        target1.DamageModel,
        Vector3.Zero
      )
      InfantryDamageAfterResist(resprojectile1)(50, 10) mustEqual (0, 0)

      val target2 = player
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target2),
        target2.DamageModel,
        Vector3.Zero
      )
      InfantryDamageAfterResist(resprojectile2)(50, 10) mustEqual (40, 10)
    }

    "calculate health and armor damage for infantry target" in {
      InfantryDamageAfterResist(100, 100)(50, 10) mustEqual (40, 10)
    }

    "calculate health and armor damage, with bleed through damage, for infantry target" in {
      //health = 100, armor = 5 -> resist 10 but only have 5, so rollover extra -> damages (40+5, 5)
      InfantryDamageAfterResist(100, 5)(50, 10) mustEqual (45, 5)
    }

    "calculate health damage for infantry target" in {
      //health = 100, armor = 0
      InfantryDamageAfterResist(100, 0)(50, 10) mustEqual (50, 0)
    }

    "calculate armor damage for infantry target" in {
      //resistance > damage
      InfantryDamageAfterResist(100, 100)(50, 60) mustEqual (0, 50)
    }

    val player2 = Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
    player2.ExoSuit = ExoSuitType.MAX
    player2.Spawn()
    "calculate no max damage for vehicles" in {
      val target1 = Vehicle(GlobalDefinitions.fury) //!
      val resprojectile1 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target1),
        target1.DamageModel,
        Vector3.Zero
      )
      MaxDamageAfterResist(resprojectile1)(50, 10) mustEqual (0, 0)

      val target2 = player2
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target2),
        target2.DamageModel,
        Vector3.Zero
      )
      MaxDamageAfterResist(resprojectile2)(50, 10) mustEqual (0, 40)
    }

    "calculate health and armor damage for max target" in {
      MaxDamageAfterResist(100, 5)(50, 10) mustEqual (35, 5)
    }

    "calculate health damage for max target" in {
      //health = 100, armor = 0
      MaxDamageAfterResist(100, 0)(50, 10) mustEqual (40, 0)
    }

    "calculate armor damage for max target" in {
      //resistance > damage
      MaxDamageAfterResist(100, 100)(50, 10) mustEqual (0, 40)
    }

    "do not care if target is infantry for vehicle calculations" in {
      val target1 = player
      val resprojectile1 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target1),
        target1.DamageModel,
        Vector3.Zero
      )
      VehicleDamageAfterResist(resprojectile1)(50, 10) mustEqual 40

      val target2 = Vehicle(GlobalDefinitions.fury) //!
      val resprojectile2 = ResolvedProjectile(
        ProjectileResolution.Splash,
        projectile,
        SourceEntry(target2),
        target2.DamageModel,
        Vector3.Zero
      )
      VehicleDamageAfterResist(resprojectile2)(50, 10) mustEqual 40
    }

    "calculate resisted damage for vehicle target" in {
      VehicleDamageAfterResist(50, 10) mustEqual 40
    }

    "calculate un-resisted damage for vehicle target" in {
      VehicleDamageAfterResist(50, 0) mustEqual 50
    }
  }
}

class DamageModelTests extends Specification {
  val wep       = GlobalDefinitions.galaxy_gunship_cannon
  val wep_tool  = Tool(wep)
  val wep_fmode = wep_tool.FireMode
  val proj      = DamageModelTests.projectile
  val player    = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  player.Spawn()
  val projectile = Projectile(proj, wep, wep_fmode, player, Vector3(2, 2, 0), Vector3.Zero)

  "DamageModel" should {
    "be a part of vitality" in {
      player.isInstanceOf[Vitality] mustEqual true
      try {
        player.getClass.getDeclaredMethod("DamageModel").hashCode()
      } catch {
        case _: Exception =>
          ko //the method doesn't exist
      }

      wep_tool.isInstanceOf[Vitality] mustEqual false
      try {
        wep_tool.getClass.getDeclaredMethod("DamageModel").hashCode()
        ko
      } catch {
        case _: Exception =>
          ok //the method doesn't exist
      }
      ok
    }

    "resolve infantry targets" in {
      val tplayer =
        Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      tplayer.Spawn()
      tplayer.Health mustEqual 100
      tplayer.Armor mustEqual 50

      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Hit,
        projectile,
        SourceEntry(tplayer),
        tplayer.DamageModel,
        Vector3.Zero
      )
      val func: Any => ResolvedProjectile = resprojectile.damage_model.Calculate(resprojectile)

      func(tplayer)
      tplayer.Health mustEqual 54
      tplayer.Armor mustEqual 46
    }

    "resolve infantry targets in a specific way" in {
      val tplayer =
        Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      tplayer.Spawn()
      tplayer.Health mustEqual 100
      tplayer.Armor mustEqual 50

      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Hit,
        projectile,
        SourceEntry(tplayer),
        tplayer.DamageModel,
        Vector3.Zero
      )
      val func: Any => ResolvedProjectile =
        resprojectile.damage_model.Calculate(resprojectile, ProjectileResolution.Splash)

      func(tplayer)
      tplayer.Health mustEqual 65
      tplayer.Armor mustEqual 35
    }

    "resolve infantry targets, with damage overflow" in {
      val tplayer =
        Player(Avatar(0, "TestCharacter2", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      tplayer.Spawn()
      tplayer.Health mustEqual 100
      tplayer.Armor mustEqual 50

      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Hit,
        projectile,
        SourceEntry(tplayer),
        tplayer.DamageModel,
        Vector3.Zero
      )
      val func: Any => ResolvedProjectile = resprojectile.damage_model.Calculate(resprojectile)
      tplayer.Armor = 0

      func(tplayer)
      tplayer.Health mustEqual 50
      tplayer.Armor mustEqual 0
    }

    "resolve vehicle targets" in {
      val vehicle = Vehicle(DamageModelTests.vehicle)
      vehicle.Health mustEqual 650

      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Hit,
        projectile,
        SourceEntry(vehicle),
        vehicle.DamageModel,
        Vector3.Zero
      )
      val func: Any => ResolvedProjectile = resprojectile.damage_model.Calculate(resprojectile)

      func(vehicle)
      vehicle.Health mustEqual 518
    }

    "resolve vehicle targets (with shields)" in {
      val vehicle = Vehicle(DamageModelTests.vehicle)
      vehicle.Shields = 10
      vehicle.Health mustEqual 650
      vehicle.Shields mustEqual 10

      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Hit,
        projectile,
        SourceEntry(vehicle),
        vehicle.DamageModel,
        Vector3.Zero
      )
      val func: Any => ResolvedProjectile = resprojectile.damage_model.Calculate(resprojectile)

      func(vehicle)
      vehicle.Health mustEqual 528
      vehicle.Shields mustEqual 0
    }

    "resolve vehicle targets (losing shields)" in {
      val vehicle = Vehicle(DamageModelTests.vehicle)
      vehicle.Shields = 10
      vehicle.Health mustEqual 650
      vehicle.Shields mustEqual 10

      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Hit,
        projectile,
        SourceEntry(vehicle),
        vehicle.DamageModel,
        Vector3.Zero
      )
      val func: Any => ResolvedProjectile = resprojectile.damage_model.Calculate(resprojectile)

      func(vehicle)
      vehicle.Health mustEqual 528
      vehicle.Shields mustEqual 0
      func(vehicle)
      vehicle.Health mustEqual 396
      vehicle.Shields mustEqual 0
    }

    "resolve vehicle targets in a specific way" in {
      val vehicle = Vehicle(DamageModelTests.vehicle)
      vehicle.Health mustEqual 650

      val resprojectile = ResolvedProjectile(
        ProjectileResolution.Hit,
        projectile,
        SourceEntry(vehicle),
        vehicle.DamageModel,
        Vector3.Zero
      )
      val func: Any => ResolvedProjectile =
        resprojectile.damage_model.Calculate(resprojectile, ProjectileResolution.Splash)

      func(vehicle)
      vehicle.Health mustEqual 518
    }
  }
}

object DamageModelTests {
  final val projectile = new ProjectileDefinition(Projectiles.heavy_grenade_projectile.id) {
    Damage0 = 50
    Damage1 = 82
    Damage2 = 82
    Damage3 = 75
    Damage4 = 66
    DamageAtEdge = 0.1f
    DamageRadius = 5f
    DegradeMultiplier = 0.5f
    LashRadius = 5f
    ProjectileDamageType = DamageType.Splash
    InitialVelocity = 75
    Lifespan = 5f
    ProjectileDefinition.CalculateDerivedFields(pdef = this)
    Modifiers = DamageModifiers.RadialDegrade
  }

  final val vehicle = new VehicleDefinition(ObjectClass.fury) {
    MaxHealth = 650
    Damageable = true
    Repairable = true
    RepairIfDestroyed = false
    MaxShields = 130 + 1
  }
}
