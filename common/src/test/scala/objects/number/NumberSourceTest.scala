// Copyright (c) 2017 PSForever
package objects.number

import net.psforever.objects.guid.AvailabilityPolicy
import net.psforever.objects.guid.key.{LoanedKey, SecureKey}
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable.Specification

class NumberSourceTest extends Specification {
  import net.psforever.objects.entity.IdentifiableEntity
  private class TestClass extends IdentifiableEntity

  "LimitedNumberSource" should {
    import net.psforever.objects.guid.source.LimitedNumberSource
    "construct" in {
      val obj = LimitedNumberSource(25)
      obj.Size mustEqual 26
      obj.CountAvailable mustEqual 26
      obj.CountUsed mustEqual 0
    }

    "get a number" in {
      val obj = LimitedNumberSource(25)
      val result : Option[LoanedKey] = obj.Available(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object.isEmpty mustEqual true
      obj.Size mustEqual 26
      obj.CountAvailable mustEqual 25
      obj.CountUsed mustEqual 1
    }

    "assign the number" in {
      val obj = LimitedNumberSource(25)
      val result : Option[LoanedKey] = obj.Available(5)
      result.isDefined mustEqual true
      result.get.Object = new TestClass()
      ok
    }

    "return a number (unused)" in {
      val obj = LimitedNumberSource(25)
      val result : Option[LoanedKey] = obj.Available(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      obj.CountUsed mustEqual 1
      val ret = obj.Return(result.get)
      ret.isEmpty mustEqual true
      obj.CountUsed mustEqual 0
    }

    "return a number (assigned)" in {
      val obj = LimitedNumberSource(25)
      val test = new TestClass()
      val result : Option[LoanedKey] = obj.Available(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Object = test
      obj.CountUsed mustEqual 1
      val ret = obj.Return(result.get)
      ret.contains(test) mustEqual true
      obj.CountUsed mustEqual 0
    }

    "restrict a number (unassigned)" in {
      val obj = LimitedNumberSource(25)
      val result : Option[LoanedKey] = obj.Restrict(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Restricted
      result.get.Object.isEmpty mustEqual true
    }

    "restrict a number (assigned + multiple assignments)" in {
      val obj = LimitedNumberSource(25)
      val test1 = new TestClass()
      val test2 = new TestClass()
      val result : Option[LoanedKey] = obj.Restrict(5)
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Restricted
      result.get.Object.isEmpty mustEqual true
      result.get.Object = None //assignment 1
      result.get.Object.isEmpty mustEqual true //still unassigned
      result.get.Object = test1 //assignment 2
      result.get.Object.contains(test1) mustEqual true
      result.get.Object = test2 //assignment 3
      result.get.Object.contains(test1) mustEqual true //same as above
    }

    "return a restricted number (correctly fail)" in {
      val obj = LimitedNumberSource(25)
      val test = new TestClass()
      val result : Option[LoanedKey] = obj.Restrict(5)
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Restricted
      result.get.Object = test

      obj.Return(5)
      val result2 : Option[SecureKey] = obj.Get(5)
      result2.get.GUID mustEqual 5
      result2.get.Policy mustEqual AvailabilityPolicy.Restricted
      result2.get.Object.contains(test) mustEqual true
    }

    "return a secure key" in {
      val obj = LimitedNumberSource(25)
      val test = new TestClass()

      val result1 : Option[LoanedKey] = obj.Available(5)
      result1.get.Object = test
      test.GUID mustEqual PlanetSideGUID(5)

      val result2 : Option[SecureKey] = obj.Get(5)
      obj.Return(result2.get).contains(test) mustEqual true
    }

    "restrict a previously-assigned number" in {
      val obj = LimitedNumberSource(25)
      val test = new TestClass()
      val result1 : Option[LoanedKey] = obj.Available(5)
      result1.isDefined mustEqual true
      result1.get.Policy mustEqual AvailabilityPolicy.Leased
      result1.get.Object = test
      val result2 : Option[LoanedKey] = obj.Restrict(5)
      result2.isDefined mustEqual true
      result2.get.Policy mustEqual AvailabilityPolicy.Restricted
      result2.get.Object.contains(test) mustEqual true
    }

    "check a number (not previously gotten)" in {
      val obj = LimitedNumberSource(25)
      val result2 : Option[SecureKey] = obj.Get(5)
      result2.get.GUID mustEqual 5
      result2.get.Policy mustEqual AvailabilityPolicy.Available
      result2.get.Object.isEmpty mustEqual true
    }

    "check a number (previously gotten)" in {
      val obj = LimitedNumberSource(25)
      val result : Option[LoanedKey] = obj.Available(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object.isEmpty mustEqual true
      val result2 : Option[SecureKey] = obj.Get(5)
      result2.get.GUID mustEqual 5
      result2.get.Policy mustEqual AvailabilityPolicy.Leased
      result2.get.Object.isEmpty mustEqual true
    }

    "check a number (assigned)" in {
      val obj = LimitedNumberSource(25)
      val result : Option[LoanedKey] = obj.Available(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object = new TestClass()
      val result2 : Option[SecureKey] = obj.Get(5)
      result2.get.GUID mustEqual 5
      result2.get.Policy mustEqual AvailabilityPolicy.Leased
      result2.get.Object mustEqual result.get.Object
    }

    "check a number (assigned and returned)" in {
      val obj = LimitedNumberSource(25)
      val test = new TestClass()
      val result : Option[LoanedKey] = obj.Available(5)
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object = test
      val result2 : Option[SecureKey] = obj.Get(5)
      result2.get.Policy mustEqual AvailabilityPolicy.Leased
      result2.get.Object.get mustEqual test
      obj.Return(5).contains(test) mustEqual true
      val result3 : Option[SecureKey] = obj.Get(5)
      result3.get.Policy mustEqual AvailabilityPolicy.Available
      result3.get.Object.isEmpty mustEqual true
    }

    "clear" in {
      val obj = LimitedNumberSource(25)
      val test1 = new TestClass()
      val test2 = new TestClass()
      val test3 = new TestClass()
      obj.Available(5) //no assignment
      obj.Available(10).get.Object = test1
      obj.Available(15).get.Object = test2
      obj.Restrict(15)
      obj.Restrict(20).get.Object = test3
      obj.CountUsed mustEqual 4

      val list : List[IdentifiableEntity] = obj.Clear()
      obj.CountUsed mustEqual 0
      list.size mustEqual 3
      list.count(obj => obj == test1) mustEqual 1
      list.count(obj => obj == test2) mustEqual 1
      list.count(obj => obj == test3) mustEqual 1
    }
  }
}
