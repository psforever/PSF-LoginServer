// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, ActorSystem, Props}
import base.ActorTest
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
import net.psforever.objects.serverobject.locks.{IFFLock, IFFLockControl}
import net.psforever.objects.serverobject.structures.{Building, StructureType}
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, CharacterVoice, PlanetSideEmpire, Vector3}
import org.specs2.mutable.Specification

class IFFLockTest extends Specification {
  "IFFLock" should {
    "construct" in {
      IFFLock(GlobalDefinitions.lock_external)
      ok
    }

    //TODO internal hacking logic will be re-written later

    "keep track of its orientation as a North-corrected vector" in {
      val ulp = math.ulp(1)
      val lock = IFFLock(GlobalDefinitions.lock_external)

      lock.Orientation = Vector3(0, 0, 0) //face North
      lock.Outwards.x < ulp mustEqual true
      lock.Outwards.y mustEqual 1

      lock.Orientation = Vector3(0, 0, 90) //face East
      lock.Outwards.x mustEqual 1
      lock.Outwards.y < ulp mustEqual true

      lock.Orientation = Vector3(0, 0, 180) //face South
      lock.Outwards.x < ulp mustEqual true
      lock.Outwards.y mustEqual -1

      lock.Orientation = Vector3(0, 0, 270) //face West
      lock.Outwards.x mustEqual -1
      lock.Outwards.y < ulp mustEqual true
    }
  }
}

class IFFLockControl1Test extends ActorTest {
  "IFFLockControl" should {
    "construct" in {
      val lock = IFFLock(GlobalDefinitions.lock_external)
      lock.Actor = system.actorOf(Props(classOf[IFFLockControl], lock), "lock-control")
      assert(lock.Actor != ActorRef.noSender)
    }
  }
}

class IFFLockControl2Test extends ActorTest {
  "IFFLockControl" should {
    "can hack" in {
      val (player, lock) = IFFLockControlTest.SetUpAgents(PlanetSideEmpire.TR)
      player.GUID = PlanetSideGUID(1)
      assert(lock.HackedBy.isEmpty)

      lock.Actor ! CommonMessages.Hack(player)
      Thread.sleep(500L) //blocking
      assert(lock.HackedBy.nonEmpty) //TODO rewrite later
    }
  }
}

class IFFLockControl3Test extends ActorTest {
  "IFFLockControl" should {
    "can hack" in {
      val (player, lock) = IFFLockControlTest.SetUpAgents(PlanetSideEmpire.TR)
      player.GUID = PlanetSideGUID(1)
      assert(lock.HackedBy.isEmpty)

      lock.Actor ! CommonMessages.Hack(player)
      Thread.sleep(500L) //blocking
      assert(lock.HackedBy.nonEmpty) //TODO rewrite later
      lock.Actor ! CommonMessages.ClearHack()
      Thread.sleep(500L) //blocking
      assert(lock.HackedBy.isEmpty) //TODO rewrite
    }
  }
}

object IFFLockControlTest {
  def SetUpAgents(faction : PlanetSideEmpire.Value)(implicit system : ActorSystem) : (Player, IFFLock) = {
    val lock = IFFLock(GlobalDefinitions.lock_external)
    lock.Actor = system.actorOf(Props(classOf[IFFLockControl], lock), "lock-control")
    lock.Owner = new Building(building_guid = 0, map_id = 0, Zone.Nowhere, StructureType.Building)
    lock.Owner.Faction = faction
    (Player(Avatar("test", faction, CharacterGender.Male, 0, CharacterVoice.Mute)), lock)
  }
}
