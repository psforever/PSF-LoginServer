// Copyright (c) 2017 PSForever
package objects.number

import akka.actor.{ActorRef, ActorSystem, Props}
import base.ActorTest
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.actor.{NumberPoolActor, Register, UniqueNumberSystem, Unregister}
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.types.PlanetSideGUID

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class AllocateNumberPoolActors extends ActorTest {
  "AllocateNumberPoolActors" in {
    val src : LimitedNumberSource = LimitedNumberSource(6000)
    val guid : NumberPoolHub = new NumberPoolHub(src)
    guid.AddPool("pool1", (1001 to 2000).toList)
    guid.AddPool("pool2", (3001 to 4000).toList)
    guid.AddPool("pool3", (5001 to 6000).toList)
    val actorMap = UniqueNumberSystemTest.AllocateNumberPoolActors(guid)
    assert(actorMap.size == 4)
    assert(actorMap.get("generic").isDefined) //automatically generated
    assert(actorMap.get("pool1").isDefined)
    assert(actorMap.get("pool2").isDefined)
    assert(actorMap.get("pool3").isDefined)
  }
}

class UniqueNumberSystemTest extends ActorTest() {
  "UniqueNumberSystem" should {
    "constructor" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      guid.AddPool("pool1", (1001 to 2000).toList)
      guid.AddPool("pool2", (3001 to 4000).toList)
      guid.AddPool("pool3", (5001 to 6000).toList)
      system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      //as long as it constructs ...
    }
  }
}

class UniqueNumberSystemTest1 extends ActorTest() {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "Register (success)" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      val pool1 = (1001 to 2000).toList
      val pool2 = (3001 to 4000).toList
      val pool3 = (5001 to 6000).toList
      guid.AddPool("pool1", pool1).Selector = new RandomSelector
      guid.AddPool("pool2", pool2).Selector = new RandomSelector
      guid.AddPool("pool3", pool3).Selector = new RandomSelector
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      assert(src.CountUsed == 0)
      //pool1
      for(_  <- 1 to 100) {
        val testObj = new EntityTestClass()
        uns ! Register(testObj, "pool1")
        val msg = receiveOne(Duration.create(500, "ms"))
        assert(msg.isInstanceOf[Success[_]])
        assert(pool1.contains(testObj.GUID.guid))
      }
      //pool2
      for(_  <- 1 to 100) {
        val testObj = new EntityTestClass()
        uns ! Register(testObj, "pool2")
        val msg = receiveOne(Duration.create(500, "ms"))
        assert(msg.isInstanceOf[Success[_]])
        assert(pool2.contains(testObj.GUID.guid))
      }
      //pool3
      for(_  <- 1 to 100) {
        val testObj = new EntityTestClass()
        uns ! Register(testObj, "pool3")
        val msg = receiveOne(Duration.create(500, "ms"))
        assert(msg.isInstanceOf[Success[_]])
        assert(pool3.contains(testObj.GUID.guid))
      }
      assert(src.CountUsed == 300)
    }
  }
}

class UniqueNumberSystemTest2 extends ActorTest() {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "Register (success; already registered)" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      guid.AddPool("pool1", (1001 to 2000).toList).Selector = new RandomSelector
      guid.AddPool("pool2", (3001 to 4000).toList).Selector = new RandomSelector
      guid.AddPool("pool3", (5001 to 6000).toList).Selector = new RandomSelector
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      val testObj = new EntityTestClass()
      assert(!testObj.HasGUID)
      assert(src.CountUsed == 0)

      uns ! Register(testObj, "pool1")
      val msg1 = receiveOne(Duration.create(500, "ms"))
      assert(msg1.isInstanceOf[Success[_]])
      assert(testObj.HasGUID)
      assert(src.CountUsed == 1)

      val id = testObj.GUID.guid
      uns ! Register(testObj, "pool2") //different pool; makes no difference
      val msg2 = receiveOne(Duration.create(500, "ms"))
      assert(msg2.isInstanceOf[Success[_]])
      assert(testObj.HasGUID)
      assert(src.CountUsed == 1)
      assert(testObj.GUID.guid == id) //unchanged
    }
  }
  //a log.warn should have been generated during this test
}

class UniqueNumberSystemTest3 extends ActorTest() {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "Register (failure; no pool)" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      guid.AddPool("pool1", (1001 to 2000).toList).Selector = new RandomSelector
      guid.AddPool("pool2", (3001 to 4000).toList).Selector = new RandomSelector
      guid.AddPool("pool3", (5001 to 6000).toList).Selector = new RandomSelector
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      val testObj = new EntityTestClass()
      assert(!testObj.HasGUID)
      assert(src.CountUsed == 0)

      uns ! Register(testObj, "pool4")
      val msg1 = receiveOne(Duration.create(500, "ms"))
      assert(msg1.isInstanceOf[Failure[_]])
      assert(!testObj.HasGUID)
      assert(src.CountUsed == 0)
    }
  }
}

class UniqueNumberSystemTest4 extends ActorTest() {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "Register (failure; empty pool)" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      guid.AddPool("pool1", (1001 to 2000).toList).Selector = new RandomSelector
      guid.AddPool("pool2", (3001 to 4000).toList).Selector = new RandomSelector
      guid.AddPool("pool3", (5001 to 6000).toList).Selector = new RandomSelector
      guid.AddPool("pool4", 50 :: Nil).Selector = new RandomSelector //list of one element; can not add an empty list
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")

      val testObj1 = new EntityTestClass()
      uns ! Register(testObj1, "pool4")
      val msg1 = receiveOne(Duration.create(500, "ms"))
      assert(msg1.isInstanceOf[Success[_]]) //pool4 is now empty

      val testObj2 = new EntityTestClass()
      uns ! Register(testObj2, "pool4")
      val msg2 = receiveOne(Duration.create(500, "ms"))
      assert(msg2.isInstanceOf[Failure[_]])
    }
  }
}

class UniqueNumberSystemTest5 extends ActorTest() {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "Unregister (success)" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      val pool2 = (3001 to 4000).toList
      guid.AddPool("pool1", (1001 to 2000).toList).Selector = new RandomSelector
      guid.AddPool("pool2", pool2).Selector = new RandomSelector
      guid.AddPool("pool3", (5001 to 6000).toList).Selector = new RandomSelector
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      val testObj = new EntityTestClass()
      assert(!testObj.HasGUID)
      assert(src.CountUsed == 0)

      uns ! Register(testObj, "pool2")
      val msg1 = receiveOne(Duration.create(500, "ms"))
      assert(msg1.isInstanceOf[Success[_]])
      assert(testObj.HasGUID)
      assert(pool2.contains(testObj.GUID.guid))
      assert(src.CountUsed == 1)

      uns ! Unregister(testObj)
      val msg2 = receiveOne(Duration.create(500, "ms"))
      assert(msg2.isInstanceOf[Success[_]])
      assert(!testObj.HasGUID)
      assert(src.CountUsed == 0)
    }
  }
}

class UniqueNumberSystemTest6 extends ActorTest() {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "Unregister (success; object not registered at all)" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      guid.AddPool("pool1", (1001 to 2000).toList).Selector = new RandomSelector
      guid.AddPool("pool2", (3001 to 4000).toList).Selector = new RandomSelector
      guid.AddPool("pool3", (5001 to 6000).toList).Selector = new RandomSelector
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      val testObj = new EntityTestClass()
      assert(!testObj.HasGUID)
      assert(src.CountUsed == 0)

      uns ! Unregister(testObj)
      val msg1 = receiveOne(Duration.create(500, "ms"))
      assert(msg1.isInstanceOf[Success[_]])
      assert(!testObj.HasGUID)
      assert(src.CountUsed == 0)
    }
  }
}

class UniqueNumberSystemTest7 extends ActorTest() {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "Unregister (failure; number not in system)" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      guid.AddPool("pool1", (1001 to 2000).toList).Selector = new RandomSelector
      guid.AddPool("pool2", (3001 to 4000).toList).Selector = new RandomSelector
      guid.AddPool("pool3", (5001 to 6000).toList).Selector = new RandomSelector
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      val testObj = new EntityTestClass()
      testObj.GUID =PlanetSideGUID(6001) //fake registering; number too high
      assert(testObj.HasGUID)
      assert(src.CountUsed == 0)

      uns ! Unregister(testObj)
      val msg1 = receiveOne(Duration.create(500, "ms"))
      assert(msg1.isInstanceOf[Failure[_]])
      assert(testObj.HasGUID)
      assert(src.CountUsed == 0)
    }
  }
}

class UniqueNumberSystemTest8 extends ActorTest() {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "Unregister (failure; object is not registered to that number)" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      guid.AddPool("pool1", (1001 to 2000).toList).Selector = new RandomSelector
      guid.AddPool("pool2", (3001 to 4000).toList).Selector = new RandomSelector
      guid.AddPool("pool3", (5001 to 6000).toList).Selector = new RandomSelector
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      val testObj = new EntityTestClass()
      testObj.GUID = PlanetSideGUID(3500) //fake registering
      assert(testObj.HasGUID)
      assert(src.CountUsed == 0)

      uns ! Unregister(testObj)
      val msg1 = receiveOne(Duration.create(500, "ms"))
      assert(msg1.isInstanceOf[Failure[_]])
      assert(testObj.HasGUID)
      assert(src.CountUsed == 0)
    }
  }
}

class UniqueNumberSystemTest9 extends ActorTest() {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "Failures (manually walking the failure cases)" in {
      val src : LimitedNumberSource = LimitedNumberSource(6000)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      guid.AddPool("pool1", (1001 to 2000).toList).Selector = new RandomSelector
      guid.AddPool("pool2", (3001 to 4000).toList).Selector = new RandomSelector
      guid.AddPool("pool3", (5001 to 6000).toList).Selector = new RandomSelector
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      val excp = new Exception("EXCEPTION MESSAGE")
      expectNoMsg(Duration.create(200, "ms"))

      //GiveNumber
      uns ! NumberPoolActor.GiveNumber(1001, Some("test")) //no task associated with id="test"
      uns ! NumberPoolActor.GiveNumber(1000, Some("test")) //no task associated with id="test" and number is not pooled
      uns ! NumberPoolActor.GiveNumber(1000, Some(1)) //the task could theoretically exist, but does not
      //NoNumber
      uns ! NumberPoolActor.NoNumber(excp, Some(1))
      uns ! NumberPoolActor.NoNumber(excp, None)
      uns ! NumberPoolActor.NoNumber(excp, Some("test"))
      //ReturnNumberResult A
      uns ! NumberPoolActor.ReturnNumberResult(1001, None, Some("test"))
      uns ! NumberPoolActor.ReturnNumberResult(1000, None, Some("test"))
      uns ! NumberPoolActor.ReturnNumberResult(1001, None, Some(1))
      uns ! NumberPoolActor.ReturnNumberResult(1000, None, Some(1))
      //ReturnNumberResult B
      uns ! NumberPoolActor.ReturnNumberResult(1001, Some(excp), Some("test"))
      uns ! NumberPoolActor.ReturnNumberResult(1001, Some(excp), Some(1))
    }
  }
}

class UniqueNumberSystemTestA extends ActorTest {
  class EntityTestClass extends IdentifiableEntity

  "UniqueNumberSystem" should {
    "remain consistent between registrations" in {
      val src : LimitedNumberSource = LimitedNumberSource(10)
      val guid : NumberPoolHub = new NumberPoolHub(src)
      guid.AddPool("pool1", (0 until 10).toList).Selector = new RandomSelector
      val uns = system.actorOf(Props(classOf[UniqueNumberSystem], guid, UniqueNumberSystemTest.AllocateNumberPoolActors(guid)), "uns")
      expectNoMsg(Duration.create(200, "ms"))

      assert(src.CountUsed == 0)
      (0 to 4).foreach(i => { assert(guid.register(new EntityTestClass(), i).isSuccess) })
      assert(src.CountUsed == 5)

      (0 to 5).foreach(_ => { uns ! Register(new EntityTestClass(), "pool1") })
      assert(receiveOne(200 milliseconds).isInstanceOf[Success[_]]) //6th
      assert(receiveOne(200 milliseconds).isInstanceOf[Success[_]]) //7th
      assert(receiveOne(200 milliseconds).isInstanceOf[Success[_]]) //8th
      assert(receiveOne(200 milliseconds).isInstanceOf[Success[_]]) //9th
      assert(receiveOne(200 milliseconds).isInstanceOf[Success[_]]) //10th
      assert(receiveOne(200 milliseconds).isInstanceOf[Failure[_]]) //no more
      assert(src.CountUsed == 10)
    }
  }
}

object UniqueNumberSystemTest {
  /**
    * @see `UniqueNumberSystem.AllocateNumberPoolActors(NumberPoolHub)(implicit ActorContext)`
    */
  def AllocateNumberPoolActors(poolSource : NumberPoolHub)(implicit system : ActorSystem) : Map[String, ActorRef] = {
    poolSource.Pools.map({ case (pname, pool) =>
      pname -> system.actorOf(Props(classOf[NumberPoolActor], pool), pname)
    }).toMap
  }
}
