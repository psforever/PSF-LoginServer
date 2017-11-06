// Copyright (c) 2017 PSForever
package objects

import akka.actor.{ActorRef, Props}
import net.psforever.objects.serverobject.CommonMessages
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.objects.serverobject.locks.{IFFLock, IFFLockControl}
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
      val lock = IFFLock(GlobalDefinitions.lock_external)
      lock.Actor = system.actorOf(Props(classOf[IFFLockControl], lock), "lock-control")
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
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
      val lock = IFFLock(GlobalDefinitions.lock_external)
      lock.Actor = system.actorOf(Props(classOf[IFFLockControl], lock), "lock-control")
      val player = Player("test", PlanetSideEmpire.TR, CharacterGender.Male, 0, 0)
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
