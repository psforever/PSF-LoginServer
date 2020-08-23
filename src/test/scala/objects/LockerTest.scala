// Copyright (c) 2017 PSForever
package objects

import akka.actor.Props
import base.ActorTest
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.mblocker.{Locker, LockerControl}
import net.psforever.types.PlanetSideEmpire
import org.specs2.mutable._

class LockerTest extends Specification {
  "LockerDefinition" should {
    "define" in {
      GlobalDefinitions.mb_locker.ObjectId mustEqual 524
      GlobalDefinitions.mb_locker.Name mustEqual "mb_locker"
    }
  }

  "Locker" should {
    "construct" in {
      new Locker()
      ok
    }
  }
}

class LockerControlTest extends ActorTest {
  "LockerControl" should {
    "construct" in {
      val locker = new Locker()
      locker.Actor = system.actorOf(Props(classOf[LockerControl], locker), "test")
      locker.Actor ! FactionAffinity.ConfirmFactionAffinity()
      expectMsg(FactionAffinity.AssertFactionAffinity(locker, PlanetSideEmpire.NEUTRAL))
    }
  }
}
