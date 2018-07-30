// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.ballistics._
import net.psforever.objects._
import net.psforever.objects.vital._
import net.psforever.types._
import org.specs2.mutable.Specification

class VitalityTest extends Specification {
  "Vitality" should {
    val wep = GlobalDefinitions.galaxy_gunship_cannon
    val wep_fmode = Tool(wep).FireMode
    val proj = wep.ProjectileTypes.head
    val vehicle = Vehicle(GlobalDefinitions.fury)
    val vSource = VehicleSource(vehicle)

    "accept a variety of events" in {
      val player = Player(Avatar("TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      val pSource = PlayerSource(player)
      val projectile = Projectile(proj, wep, wep_fmode, player, Vector3(2, 2, 0), Vector3.Zero)
      val resprojectile = ResolvedProjectile(ProjectileResolution.Splash, projectile, SourceEntry(player), player.DamageModel, Vector3(50, 50, 0))

      player.History(resprojectile) //ResolvedProjectile, straight-up
      player.History(DamageFromProjectile(resprojectile))
      player.History(HealFromKit(pSource, 10, GlobalDefinitions.medkit))
      player.History(HealFromTerm(pSource, 10, 0, GlobalDefinitions.order_terminal))
      player.History(HealFromImplant(pSource, 10, ImplantType.AdvancedRegen))
      player.History(HealFromExoSuitChange(pSource, ExoSuitType.Standard))
      player.History(RepairFromTerm(vSource, 10, GlobalDefinitions.order_terminal))
      player.History(VehicleShieldCharge(vSource, 10))
      player.History(PlayerSuicide(pSource))
      ok
    }

    "return and clear the former list of vital activities" in {
      val player = Player(Avatar("TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      val pSource = PlayerSource(player)

      player.History(HealFromKit(pSource, 10, GlobalDefinitions.medkit))
      player.History(HealFromTerm(pSource, 10, 0, GlobalDefinitions.order_terminal))
      player.History(HealFromImplant(pSource, 10, ImplantType.AdvancedRegen))
      player.History(HealFromExoSuitChange(pSource, ExoSuitType.Standard))
      player.History(RepairFromTerm(vSource, 10, GlobalDefinitions.order_terminal))
      player.History(VehicleShieldCharge(vSource, 10))
      player.History(PlayerSuicide(pSource))
      player.History.size mustEqual 7

      val list = player.ClearHistory()
      player.History.size mustEqual 0
      list.head.isInstanceOf[PlayerSuicide] mustEqual true
      list(1).isInstanceOf[VehicleShieldCharge] mustEqual true
      list(2).isInstanceOf[RepairFromTerm] mustEqual true
      list(3).isInstanceOf[HealFromExoSuitChange] mustEqual true
      list(4).isInstanceOf[HealFromImplant] mustEqual true
      list(5).isInstanceOf[HealFromTerm] mustEqual true
      list(6).isInstanceOf[HealFromKit] mustEqual true
    }

    "get exactly one entry that was caused by projectile damage" in {
      val player = Player(Avatar("TestCharacter", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
      val pSource = PlayerSource(player)
      val projectile = Projectile(proj, wep, wep_fmode, player, Vector3(2, 2, 0), Vector3.Zero)
      val resprojectile = ResolvedProjectile(ProjectileResolution.Splash, projectile, SourceEntry(player), player.DamageModel, Vector3(50, 50, 0))

      player.History(DamageFromProjectile(resprojectile))
      player.History(HealFromKit(pSource, 10, GlobalDefinitions.medkit))
      player.History(HealFromTerm(pSource, 10, 0, GlobalDefinitions.order_terminal))
      player.History(HealFromImplant(pSource, 10, ImplantType.AdvancedRegen))
      player.History(HealFromExoSuitChange(pSource, ExoSuitType.Standard))
      player.History(RepairFromTerm(vSource, 10, GlobalDefinitions.order_terminal))
      player.History(VehicleShieldCharge(vSource, 10))
      player.History(PlayerSuicide(pSource))

      player.LastShot match {
        case Some(resolved_projectile) =>
          resolved_projectile.projectile mustEqual projectile
        case None =>
          ko
      }
    }
  }
}
