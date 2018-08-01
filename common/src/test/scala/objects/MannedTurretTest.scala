// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, Props}
import base.ActorTest
import net.psforever.objects.{Avatar, GlobalDefinitions, Player, Tool}
import net.psforever.objects.definition.ToolDefinition
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.serverobject.turret.{MannedTurret, MannedTurretControl, MannedTurretDefinition, TurretUpgrade}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire}
import org.specs2.mutable.Specification

import scala.collection.mutable
import scala.concurrent.duration._

class MannedTurretTest extends Specification {
  "MannedTurretTest" should {
    "define" in {
      val obj = new MannedTurretDefinition(480)
      obj.Weapons mustEqual mutable.HashMap.empty[TurretUpgrade.Value, ToolDefinition]
      obj.ReserveAmmunition mustEqual false
      obj.FactionLocked mustEqual true
      obj.MaxHealth mustEqual 100
      obj.MountPoints mustEqual mutable.HashMap.empty[Int,Int]
    }

    "construct" in {
      val obj = MannedTurret(GlobalDefinitions.manned_turret)
      obj.Weapons.size mustEqual 1
      obj.Weapons(1).Equipment match {
        case Some(tool : Tool) =>
          tool.Definition mustEqual GlobalDefinitions.phalanx_sgl_hevgatcan
        case _ =>
          ko
      }
      obj.Seats.size mustEqual 1
      obj.Seats(0).ControlledWeapon mustEqual Some(1)
      obj.MountPoints.size mustEqual 1
      obj.MountPoints(1) mustEqual 0
      obj.Health mustEqual 3600
      obj.Upgrade mustEqual TurretUpgrade.None
      obj.Jammered mustEqual false

      obj.Health = 360
      obj.Health mustEqual 360
      obj.Jammered = true
      obj.Jammered mustEqual true
    }

    "upgrade to a different weapon" in {
      val obj = MannedTurret(GlobalDefinitions.manned_turret)
      obj.Upgrade = TurretUpgrade.None
      obj.Weapons(1).Equipment match {
        case Some(tool : Tool) =>
          tool.Definition mustEqual GlobalDefinitions.phalanx_sgl_hevgatcan
          tool.FireModeIndex mustEqual 0
          tool.NextFireMode
          tool.FireModeIndex mustEqual 0 //one fire mode
        case _ =>
          ko
      }
      //upgrade
      obj.Upgrade = TurretUpgrade.AVCombo
      obj.Weapons(1).Equipment match {
        case Some(tool : Tool) =>
          tool.Definition mustEqual GlobalDefinitions.phalanx_avcombo
          tool.FireModeIndex mustEqual 0
          tool.ProjectileType mustEqual GlobalDefinitions.phalanx_projectile.ProjectileType
          tool.NextFireMode
          tool.FireModeIndex mustEqual 1
          tool.ProjectileType mustEqual GlobalDefinitions.phalanx_av_projectile.ProjectileType
        case _ =>
          ko
      }
      //revert
      obj.Upgrade = TurretUpgrade.None
      obj.Weapons(1).Equipment match {
        case Some(tool : Tool) =>
          tool.Definition mustEqual GlobalDefinitions.phalanx_sgl_hevgatcan
        case _ =>
          ko
      }
    }
  }
}

class MannedTurretControl1Test extends ActorTest {
  "MannedTurretControl" should {
    "construct" in {
      val obj = MannedTurret(GlobalDefinitions.manned_turret)
      obj.Actor = system.actorOf(Props(classOf[MannedTurretControl], obj), "turret-control")
      assert(obj.Actor != ActorRef.noSender)
    }
  }
}

class MannedTurretControl2Test extends ActorTest {
  val player = Player(Avatar("", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  val obj = MannedTurret(GlobalDefinitions.manned_turret)
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[MannedTurretControl], obj), "turret-control")
  val bldg = Building(0, Zone.Nowhere, StructureType.Building)
  bldg.Amenities = obj
  bldg.Faction = PlanetSideEmpire.TR

  "MannedTurretControl" should {
    "seat on faction affiliation when FactionLock is true" in {
      assert(player.Faction == PlanetSideEmpire.TR)
      assert(obj.Faction == PlanetSideEmpire.TR)
      assert(obj.Definition.FactionLocked)

      obj.Actor ! Mountable.TryMount(player, 0)
      val reply = receiveOne(300 milliseconds)
      reply match {
        case msg : Mountable.MountMessages =>
          assert(msg.response.isInstanceOf[Mountable.CanMount])
        case _ =>
          assert(false)
      }
    }
  }
}

class MannedTurretControl3Test extends ActorTest {
  val player = Player(Avatar("", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  val obj = MannedTurret(GlobalDefinitions.manned_turret)
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[MannedTurretControl], obj), "turret-control")
  val bldg = Building(0, Zone.Nowhere, StructureType.Building)
  bldg.Amenities = obj

  "MannedTurretControl" should {
    "block seating on mismatched faction affiliation when FactionLock is true" in {
      assert(player.Faction == PlanetSideEmpire.TR)
      assert(obj.Faction == PlanetSideEmpire.NEUTRAL)
      assert(obj.Definition.FactionLocked)

      obj.Actor ! Mountable.TryMount(player, 0)
      val reply = receiveOne(300 milliseconds)
      reply match {
        case msg : Mountable.MountMessages =>
          assert(msg.response.isInstanceOf[Mountable.CanNotMount])
        case _ =>
          assert(false)
      }
    }
  }
}

class MannedTurretControl4Test extends ActorTest {
  val player = Player(Avatar("", PlanetSideEmpire.TR, CharacterGender.Male, 0, CharacterVoice.Mute))
  val objDef = new MannedTurretDefinition(480)
  objDef.FactionLocked = false
  val obj = MannedTurret(objDef)
  obj.GUID = PlanetSideGUID(1)
  obj.Actor = system.actorOf(Props(classOf[MannedTurretControl], obj), "turret-control")
  val bldg = Building(0, Zone.Nowhere, StructureType.Building)
  bldg.Amenities = obj

  "MannedTurretControl" should {
    "seating even with mismatched faction affiliation when FactionLock is false" in {
      assert(player.Faction == PlanetSideEmpire.TR)
      assert(obj.Faction == PlanetSideEmpire.NEUTRAL)
      assert(!obj.Definition.FactionLocked)

      obj.Actor ! Mountable.TryMount(player, 0)
      val reply = receiveOne(300 milliseconds)
      reply match {
        case msg : Mountable.MountMessages =>
          assert(msg.response.isInstanceOf[Mountable.CanMount])
        case _ =>
          assert(false)
      }
    }
  }
}
