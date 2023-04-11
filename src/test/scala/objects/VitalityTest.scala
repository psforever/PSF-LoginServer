// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.ballistics._
import net.psforever.objects._
import net.psforever.objects.avatar.Avatar
import net.psforever.objects.serverobject.terminals.Terminal
import net.psforever.objects.sourcing.{AmenitySource, PlayerSource, SourceEntry, VehicleSource}
import net.psforever.objects.vital._
import net.psforever.objects.vital.base.DamageResolution
import net.psforever.objects.vital.interaction.DamageInteraction
import net.psforever.objects.vital.projectile.ProjectileReason
import net.psforever.types._
import org.specs2.mutable.Specification

class VitalityTest extends Specification {
  "Vitality" should {
    val wep       = GlobalDefinitions.galaxy_gunship_cannon
    val wep_fmode = Tool(wep).FireMode
    val proj      = wep.ProjectileTypes.head
    val vehicle   = Vehicle(GlobalDefinitions.fury)
    val vSource   = VehicleSource(vehicle)

    "accept a variety of events" in {
      val player     = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      val pSource    = PlayerSource(player)
      val projectile = Projectile(proj, wep, wep_fmode, player, Vector3(2, 2, 0), Vector3.Zero)
      val resprojectile = DamageInteraction(
        SourceEntry(player),
        ProjectileReason(
          DamageResolution.Hit,
          projectile,
          player.DamageModel
        ),
        Vector3(50, 50, 0)
      )
      val result = resprojectile.calculate()(player)
      val term = AmenitySource(new Terminal(GlobalDefinitions.order_terminal) { GUID = PlanetSideGUID(1) })

      player.LogActivity(result) //DamageResult, straight-up
      player.LogActivity(DamageFromProjectile(result))
      player.LogActivity(HealFromKit(GlobalDefinitions.medkit, 10))
      player.LogActivity(HealFromTerminal(term, 10))
      player.LogActivity(HealFromImplant(ImplantType.AdvancedRegen, 10))
      player.LogActivity(RepairFromExoSuitChange(ExoSuitType.Standard, 10))
      player.LogActivity(RepairFromTerminal(term, 10))
      player.LogActivity(ShieldCharge(10, Some(vSource)))
      player.LogActivity(PlayerSuicide(PlayerSource(player)))
      ok
    }

    "return and clear the former list of vital activities" in {
      val player  = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      val pSource = PlayerSource(player)
      val term = AmenitySource(new Terminal(GlobalDefinitions.order_terminal) { GUID = PlanetSideGUID(1) })

      player.LogActivity(HealFromKit(GlobalDefinitions.medkit, 10))
      player.LogActivity(HealFromTerminal(term, 10))
      player.LogActivity(HealFromImplant(ImplantType.AdvancedRegen, 10))
      player.LogActivity(RepairFromExoSuitChange(ExoSuitType.Standard, 10))
      player.LogActivity(RepairFromTerminal(term, 10))
      player.LogActivity(ShieldCharge(10, Some(vSource)))
      player.LogActivity(PlayerSuicide(PlayerSource(player)))
      player.History.size mustEqual 7

      val list = player.ClearHistory()
      player.History.size mustEqual 0
      list.head.isInstanceOf[PlayerSuicide] mustEqual true
      list(1).isInstanceOf[ShieldCharge] mustEqual true
      list(2).isInstanceOf[RepairFromTerminal] mustEqual true
      list(3).isInstanceOf[RepairFromExoSuitChange] mustEqual true
      list(4).isInstanceOf[HealFromImplant] mustEqual true
      list(5).isInstanceOf[HealFromTerminal] mustEqual true
      list(6).isInstanceOf[HealFromKit] mustEqual true
    }

    "get exactly one entry that was caused by projectile damage" in {
      val player     = Player(Avatar(0, "TestCharacter", PlanetSideEmpire.TR, CharacterSex.Male, 0, CharacterVoice.Mute))
      val pSource    = PlayerSource(player)
      val projectile = Projectile(proj, wep, wep_fmode, player, Vector3(2, 2, 0), Vector3.Zero)
      val resprojectile = DamageInteraction(
        SourceEntry(player),
        ProjectileReason(
          DamageResolution.Hit,
          projectile,
          player.DamageModel
        ),
        Vector3(50, 50, 0)
      )
      val result = resprojectile.calculate()(player)
      val term = AmenitySource(new Terminal(GlobalDefinitions.order_terminal) { GUID = PlanetSideGUID(1) })

      player.LogActivity(DamageFromProjectile(result))
      player.LogActivity(HealFromKit(GlobalDefinitions.medkit, 10))
      player.LogActivity(HealFromTerminal(term, 10))
      player.LogActivity(HealFromImplant(ImplantType.AdvancedRegen, 10))
      player.LogActivity(RepairFromExoSuitChange(ExoSuitType.Standard, 10))
      player.LogActivity(RepairFromTerminal(term, 10))
      player.LogActivity(ShieldCharge(10, Some(vSource)))
      player.LogActivity(PlayerSuicide(PlayerSource(player)))

      player.LastShot match {
        case Some(resolved_projectile) =>
          resolved_projectile.interaction.cause match {
            case o: ProjectileReason => o.projectile mustEqual projectile
            case _ => ko
          }
        case None =>
          ko
      }
    }
  }
}
