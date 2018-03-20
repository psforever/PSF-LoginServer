// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, ActorSystem, Props}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.{Avatar, GlobalDefinitions, Player}
import net.psforever.objects.serverobject.locks.{IFFLock, IFFLockControl}
import net.psforever.objects.serverobject.structures.Building
import net.psforever.objects.zones.Zone
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.{CharacterGender, PlanetSideEmpire}
import org.specs2.mutable.Specification

class IFFLockTest extends Specification {
  "IFFLock" should {
    "construct" in {
      IFFLock(GlobalDefinitions.lock_external)
      ok
    }

    //TODO internal hacking logic will be re-written later
  }
}

class IFFLockControl1Test extends ActorTest() {
  "IFFLockControl" should {
    "construct" in {
      val lock = IFFLock(GlobalDefinitions.lock_external)
      lock.Actor = system.actorOf(Props(classOf[IFFLockControl], lock), "lock-control")
      assert(lock.Actor != ActorRef.noSender)
    }
  }
}

class IFFLockControl2Test extends ActorTest() {
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

class IFFLockControl3Test extends ActorTest() {
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
    lock.Owner = new Building(0, Zone.Nowhere)
    lock.Owner.Faction = faction
    (Player(Avatar("test", faction, CharacterGender.Male, 0, 0)), lock)
  }
}
