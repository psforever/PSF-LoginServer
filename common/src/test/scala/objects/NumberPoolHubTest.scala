// Copyright (c) 2017 PSForever
package objects

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.selector.RandomSelector
import net.psforever.objects.guid.source.LimitedNumberSource
import net.psforever.packet.game.PlanetSideGUID
import org.specs2.mutable.Specification

import scala.util.Success

class NumberPoolHubTest extends Specification {
  val numberList = 0 :: 1 :: 2 :: 3 :: 5 :: 8 :: 13 :: 21 :: Nil
  val numberList1 = 0 :: 1 :: 2 :: 3 :: 5 :: Nil
  val numberList2 = 8 :: 13 :: 21 :: 34 :: Nil
  val numberSet1 = numberList1.toSet
  val numberSet2 = numberList2.toSet
  class EntityTestClass extends IdentifiableEntity

  "NumberPoolHub" should {
    "construct" in {
      new NumberPoolHub(new LimitedNumberSource(51))
      ok
    }

    "get a pool" in {
      val obj = new NumberPoolHub(new LimitedNumberSource(51))
      obj.GetPool("generic").isDefined mustEqual true //default pool
    }

    "add a pool" in {
      val obj = new NumberPoolHub(new LimitedNumberSource(51))
      obj.Numbers.isEmpty mustEqual true
      obj.AddPool("fibonacci", numberList)
      obj.Numbers.toSet.equals(numberList.toSet) mustEqual true
      val pool = obj.GetPool("fibonacci")
      pool.isDefined mustEqual true
      pool.get.Numbers.equals(numberList)
    }

    "enumerate the content of all pools" in {
      val obj = new NumberPoolHub(new LimitedNumberSource(51))
      obj.AddPool("fibonacci1", numberList1)
      obj.AddPool("fibonacci2", numberList2)
      numberSet1.intersect(obj.Numbers.toSet) mustEqual numberSet1
      numberSet2.intersect(obj.Numbers.toSet) mustEqual numberSet2
      obj.Numbers.toSet.diff(numberSet1) mustEqual numberSet2
    }

    "remove a pool" in {
      val obj = new NumberPoolHub(new LimitedNumberSource(51))
      obj.Numbers.isEmpty mustEqual true
      obj.AddPool("fibonacci", numberList)
      obj.Numbers.toSet.equals(numberList.toSet) mustEqual true
      obj.RemovePool("fibonacci").toSet.equals(numberList.toSet) mustEqual true
      obj.Numbers.isEmpty mustEqual true
      obj.GetPool("fibonacci") mustEqual None
    }

    "block removing the default 'generic' pool" in {
      val obj = new NumberPoolHub(new LimitedNumberSource(51))
      obj.RemovePool("generic") must throwA[IllegalArgumentException]
    }

    "block adding pools that use already-included numbers" in {
      val obj = new NumberPoolHub(new LimitedNumberSource(51))
      obj.AddPool("fibonacci1", numberList)
      val numberList4 = 3 :: 7 :: 21 :: 34 :: 45 :: Nil
      obj.AddPool("fibonacci2", numberList4) must throwA[IllegalArgumentException]
    }

    "enumerate only the content of all current pools" in {
      val obj = new NumberPoolHub(new LimitedNumberSource(51))
      obj.AddPool("fibonacci1", numberList1)
      obj.AddPool("fibonacci2", numberList2)
      numberSet1.intersect(obj.Numbers.toSet) mustEqual numberSet1
      numberSet2.intersect(obj.Numbers.toSet) mustEqual numberSet2
      obj.RemovePool("fibonacci1")
      numberSet1.intersect(obj.Numbers.toSet) mustEqual Set() //no intersect
      numberSet2.intersect(obj.Numbers.toSet) mustEqual numberSet2
    }

    "register an object to a pool" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci", numberList)
      val obj = new EntityTestClass()
      obj.GUID must throwA[Exception]
      hub.register(obj, "fibonacci") match {
        case Success(number) =>
          obj.GUID mustEqual PlanetSideGUID(number)
        case _ =>
          ko
      }
    }

    "lookup a registered object" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci", numberList)
      val obj = new EntityTestClass()
      hub.register(obj, "fibonacci") match {
        case Success(number) =>
          val objFromNumber = hub(number)
          objFromNumber mustEqual Some(obj)
        case _ =>
          ko
      }
    }

    "lookup the pool of a(n unassigned) number" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci1", numberList1)
      hub.AddPool("fibonacci2", numberList2)
      hub.WhichPool(13) mustEqual Some("fibonacci2")
    }

    "lookup the pool of a registered object" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci", numberList1)
      val obj = new EntityTestClass()
      hub.register(obj, "fibonacci")
      hub.WhichPool(obj) mustEqual Some("fibonacci")
    }

    "register an object to a specific, unused number; it is assigned to pool 'generic'" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci", numberList1)
      val obj = new EntityTestClass()
      obj.GUID must throwA[Exception]
      hub.register(obj, 44) match {
        case Success(number) =>
          obj.GUID mustEqual PlanetSideGUID(number)
          hub.WhichPool(obj) mustEqual Some("generic")
        case _ =>
          ko
      }
    }

    "register an object to a specific, pooled number" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci", numberList)
      val obj = new EntityTestClass()
      obj.GUID must throwA[Exception]
      hub.register(obj, 13) match {
        case Success(number) =>
          obj.GUID mustEqual PlanetSideGUID(number)
          hub.WhichPool(obj) mustEqual Some("fibonacci")
        case _ =>
          ko
      }
    }

    "register an object without extra specifications; it is assigned to pool 'generic'" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      val obj = new EntityTestClass()
      hub.register(obj)
      hub.WhichPool(obj) mustEqual Some("generic")
    }

    "unregister an object" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci", numberList)
      val obj = new EntityTestClass()
      hub.register(obj, "fibonacci")
      hub.WhichPool(obj) mustEqual Some("fibonacci")
      try { obj.GUID } catch { case _ : Exception => ko } //passes

      hub.unregister(obj)
      hub.WhichPool(obj) mustEqual None
      obj.GUID must throwA[Exception] //fails
    }

    "not register an object to a different pool" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci1", numberList1)
      hub.AddPool("fibonacci2", numberList2)
      val obj = new EntityTestClass()
      hub.register(obj, "fibonacci1")
      hub.register(obj, "fibonacci2")
      hub.WhichPool(obj).contains("fibonacci1") mustEqual true
    }

    "fail to unregister an object that is not registered to this hub" in {
      val hub1 = new NumberPoolHub(new LimitedNumberSource(51))
      val hub2 = new NumberPoolHub(new LimitedNumberSource(51))
      hub1.AddPool("fibonacci", numberList)
      hub2.AddPool("fibonacci", numberList)
      val obj = new EntityTestClass()
      hub1.register(obj, "fibonacci")
      hub2.unregister(obj) must throwA[Exception]
    }

    "pre-register a specific, unused number" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.register(13) match {
        case Success(_) =>
          ok
        case _ =>
          ko
      }
    }

    "pre-register a specific, pooled number" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci", numberList)
      hub.register(13) match {
        case Success(key) =>
          key.GUID mustEqual 13
        case _ =>
          ko
      }
    }

    "pre-register a number from a known pool" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci", numberList).Selector = new RandomSelector
      hub.register("fibonacci") match {
        case Success(key) =>
          numberList.contains(key.GUID) mustEqual true
        case _ =>
          ko
      }
    }

    "unregister a number" in {
      val hub = new NumberPoolHub(new LimitedNumberSource(51))
      hub.AddPool("fibonacci", numberList).Selector = new RandomSelector //leave this tagged on
      val obj = new EntityTestClass()
      hub.register(13) match {
        case Success(key) =>
          key.Object = obj
        case _ =>
          ko
      }
      hub.WhichPool(obj) mustEqual Some("fibonacci")
      hub.unregister(13) match {
        case Success(thing) =>
          thing mustEqual Some(obj)
          thing.get.GUID must throwA[Exception]
        case _ =>
          ko
      }
    }

    "not affect the hidden restricted pool by adding a new pool" in {
      val src = new LimitedNumberSource(51)
      src.Restrict(4)
      src.Restrict(8) //in fibonacci
      src.Restrict(10)
      src.Restrict(12)
      val hub = new NumberPoolHub(src)
      hub.AddPool("fibonacci", numberList) must throwA[IllegalArgumentException]
    }

    "not register an object to a number belonging to the restricted pool" in {
      val src = new LimitedNumberSource(51)
      src.Restrict(4)
      val hub = new NumberPoolHub(src)
      val obj = new EntityTestClass()
      hub.register(obj, 4).isFailure mustEqual true
    }

    "not register an object to the restricted pool directly" in {
      val src = new LimitedNumberSource(51)
//      src.Restrict(4)
      val hub = new NumberPoolHub(src)
      val obj = new EntityTestClass()
      hub.register(obj, "").isFailure mustEqual true //the empty string represents the restricted pool
    }

    "not register a number belonging to the restricted pool" in {
      val src = new LimitedNumberSource(51)
      src.Restrict(4)
      val hub = new NumberPoolHub(src)
      hub.register(4).isFailure mustEqual true
    }

    "not unregister a number belonging to the restricted pool" in {
      val src = new LimitedNumberSource(51)
      src.Restrict(4)
      val hub = new NumberPoolHub(src)
      hub.unregister(4).isFailure mustEqual true
    }
  }
}
