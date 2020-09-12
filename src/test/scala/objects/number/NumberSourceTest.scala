// Copyright (c) 2017 PSForever
package objects.number

import net.psforever.objects.guid.AvailabilityPolicy
import net.psforever.objects.guid.key.{LoanedKey, SecureKey}
import net.psforever.types.PlanetSideGUID
import org.specs2.mutable.Specification

class NumberSourceTest extends Specification {
  import net.psforever.objects.entity.IdentifiableEntity
  private class TestClass extends IdentifiableEntity

  "MaxNumberSource" should {
    import net.psforever.objects.guid.source.MaxNumberSource
    "construct" in {
      val obj = MaxNumberSource(25)
      obj.size mustEqual 26
      obj.countAvailable mustEqual 26
      obj.countUsed mustEqual 0
    }

    "construct failure (negative max value)" in {
      MaxNumberSource(-1) must throwA[IllegalArgumentException]
    }

    "get any number (failure)" in {
      val obj = MaxNumberSource(25)
      obj.getAvailable(number = 50).isDefined mustEqual false
    }

    "get a valid number" in {
      val obj                       = MaxNumberSource(25)
      val result: Option[LoanedKey] = obj.getAvailable(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object.isEmpty mustEqual true
      obj.size mustEqual 26
      obj.countAvailable mustEqual 25
      obj.countUsed mustEqual 1
    }

    "assign the number" in {
      val obj                       = MaxNumberSource(25)
      val result: Option[LoanedKey] = obj.getAvailable(5)
      result.isDefined mustEqual true
      result.get.Object = new TestClass()
      ok
    }

    "return a number (unused)" in {
      val obj                       = MaxNumberSource(25)
      val result: Option[LoanedKey] = obj.getAvailable(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      obj.countUsed mustEqual 1
      val ret = obj.returnNumber(result.get)
      ret.isEmpty mustEqual true
      obj.countUsed mustEqual 0
    }

    "return a number (assigned)" in {
      val obj                       = MaxNumberSource(25)
      val test                      = new TestClass()
      val result: Option[LoanedKey] = obj.getAvailable(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Object = test
      obj.countUsed mustEqual 1
      val ret = obj.returnNumber(result.get)
      ret.contains(test) mustEqual true
      obj.countUsed mustEqual 0
    }

    "restrict a number (unassigned)" in {
      val obj                       = MaxNumberSource(25)
      val result: Option[LoanedKey] = obj.restrictNumber(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Restricted
      result.get.Object.isEmpty mustEqual true
    }

    "restrict a number (assigned + multiple assignments)" in {
      val obj                       = MaxNumberSource(25)
      val test1                     = new TestClass()
      val test2                     = new TestClass()
      val result: Option[LoanedKey] = obj.restrictNumber(5)
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Restricted
      result.get.Object.isEmpty mustEqual true
      result.get.Object = None                 //assignment 1
      result.get.Object.isEmpty mustEqual true //still unassigned
      result.get.Object = test1                //assignment 2
      result.get.Object.contains(test1) mustEqual true
      result.get.Object = test2                        //assignment 3
      result.get.Object.contains(test1) mustEqual true //same as above
    }

    "return a restricted number (correctly fail)" in {
      val obj                       = MaxNumberSource(25)
      val test                      = new TestClass()
      val result: Option[LoanedKey] = obj.restrictNumber(5)
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Restricted
      result.get.Object = test

      obj.returnNumber(5)
      val result2: Option[SecureKey] = obj.get(5)
      result2.get.GUID mustEqual 5
      result2.get.Policy mustEqual AvailabilityPolicy.Restricted
      result2.get.Object.contains(test) mustEqual true
    }

    "return a secure key" in {
      val obj  = MaxNumberSource(25)
      val test = new TestClass()

      val result1: Option[LoanedKey] = obj.getAvailable(5)
      result1.get.Object = test
      test.GUID mustEqual PlanetSideGUID(5)

      val result2: Option[SecureKey] = obj.get(5)
      obj.returnNumber(result2.get).contains(test) mustEqual true
    }

    "restrict a previously-assigned number" in {
      val obj                        = MaxNumberSource(25)
      val test                       = new TestClass()
      val result1: Option[LoanedKey] = obj.getAvailable(5)
      result1.isDefined mustEqual true
      result1.get.Policy mustEqual AvailabilityPolicy.Leased
      result1.get.Object = test
      val result2: Option[LoanedKey] = obj.restrictNumber(5)
      result2.isDefined mustEqual true
      result2.get.Policy mustEqual AvailabilityPolicy.Restricted
      result2.get.Object.contains(test) mustEqual true
    }

    "check a number (not previously gotten)" in {
      val obj                        = MaxNumberSource(25)
      val result2: Option[SecureKey] = obj.get(5)
      result2.get.GUID mustEqual 5
      result2.get.Policy mustEqual AvailabilityPolicy.Available
      result2.get.Object.isEmpty mustEqual true
    }

    "check a number (previously gotten)" in {
      val obj                       = MaxNumberSource(25)
      val result: Option[LoanedKey] = obj.getAvailable(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object.isEmpty mustEqual true
      val result2: Option[SecureKey] = obj.get(5)
      result2.get.GUID mustEqual 5
      result2.get.Policy mustEqual AvailabilityPolicy.Leased
      result2.get.Object.isEmpty mustEqual true
    }

    "check a number (assigned)" in {
      val obj                       = MaxNumberSource(25)
      val result: Option[LoanedKey] = obj.getAvailable(5)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 5
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object = new TestClass()
      val result2: Option[SecureKey] = obj.get(5)
      result2.get.GUID mustEqual 5
      result2.get.Policy mustEqual AvailabilityPolicy.Leased
      result2.get.Object mustEqual result.get.Object
    }

    "check a number (assigned and returned)" in {
      val obj                       = MaxNumberSource(25)
      val test                      = new TestClass()
      val result: Option[LoanedKey] = obj.getAvailable(5)
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object = test
      val result2: Option[SecureKey] = obj.get(5)
      result2.get.Policy mustEqual AvailabilityPolicy.Leased
      result2.get.Object.get mustEqual test
      obj.returnNumber(5).contains(test) mustEqual true
      val result3: Option[SecureKey] = obj.get(5)
      result3.get.Policy mustEqual AvailabilityPolicy.Available
      result3.get.Object.isEmpty mustEqual true
    }

    "clear" in {
      val obj   = MaxNumberSource(25)
      val test1 = new TestClass()
      val test2 = new TestClass()
      val test3 = new TestClass()
      obj.getAvailable(5) //no assignment
      obj.getAvailable(10).get.Object = test1
      obj.getAvailable(15).get.Object = test2
      obj.restrictNumber(15)
      obj.restrictNumber(20).get.Object = test3
      obj.countUsed mustEqual 4

      val list: List[IdentifiableEntity] = obj.clear()
      obj.countUsed mustEqual 0
      list.size mustEqual 3
      list.count(obj => obj == test1) mustEqual 1
      list.count(obj => obj == test2) mustEqual 1
      list.count(obj => obj == test3) mustEqual 1
    }
  }

  "SpecificNumberSource" should {
    import net.psforever.objects.guid.source.SpecificNumberSource
    "construct" in {
      val obj = SpecificNumberSource(List(25))
      obj.size mustEqual 1
      obj.countAvailable mustEqual 1
      obj.countUsed mustEqual 0
    }

    "construct failure (no values)" in {
      SpecificNumberSource(List()) must throwA[IllegalArgumentException]
    }

    "construct failure (at least one value is negative)" in {
      SpecificNumberSource(List(0, 1, -1, 2, 3)) must throwA[IllegalArgumentException]
    }

    "get any number (failure)" in {
      val obj = SpecificNumberSource(List(25))
      obj.getAvailable(number = 5).isDefined mustEqual false
    }

    "get specific number (success)" in {
      val obj                       = SpecificNumberSource(List(25))
      val result: Option[LoanedKey] = obj.getAvailable(25)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 25
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object.isEmpty mustEqual true
      obj.size mustEqual 1
      obj.countAvailable mustEqual 0
      obj.countUsed mustEqual 1
    }

    "assign the number" in {
      val obj                       = SpecificNumberSource(List(25))
      val result: Option[LoanedKey] = obj.getAvailable(number = 25)
      result.isDefined mustEqual true
      result.get.Object = new TestClass()
      ok
    }

    "return a number (unused)" in {
      val obj                       = SpecificNumberSource(List(25))
      val result: Option[LoanedKey] = obj.getAvailable(number = 25)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 25
      obj.countUsed mustEqual 1
      val ret = obj.returnNumber(result.get)
      ret.isEmpty mustEqual true
      obj.countUsed mustEqual 0
    }

    "return a number (assigned)" in {
      val obj                       = SpecificNumberSource(List(25))
      val test                      = new TestClass()
      val result: Option[LoanedKey] = obj.getAvailable(number = 25)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 25
      result.get.Object = test
      obj.countUsed mustEqual 1
      val ret = obj.returnNumber(result.get)
      ret.contains(test) mustEqual true
      obj.countUsed mustEqual 0
    }

    "restrict a number (unassigned)" in {
      val obj                       = SpecificNumberSource(List(25))
      val result: Option[LoanedKey] = obj.restrictNumber(number = 25)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 25
      result.get.Policy mustEqual AvailabilityPolicy.Restricted
      result.get.Object.isEmpty mustEqual true
    }

    "restrict a number (assigned + multiple assignments)" in {
      val obj                       = SpecificNumberSource(List(25, 26))
      val test1                     = new TestClass()
      val test2                     = new TestClass()
      val result: Option[LoanedKey] = obj.restrictNumber(number = 25)
      result.get.GUID mustEqual 25
      result.get.Policy mustEqual AvailabilityPolicy.Restricted
      result.get.Object.isEmpty mustEqual true
      result.get.Object = None                 //assignment 1
      result.get.Object.isEmpty mustEqual true //still unassigned
      result.get.Object = test1                //assignment 2
      result.get.Object.contains(test1) mustEqual true
      result.get.Object = test2                        //assignment 3
      result.get.Object.contains(test1) mustEqual true //same as above
    }

    "return a restricted number (correctly fail)" in {
      val obj                       = SpecificNumberSource(List(25))
      val test                      = new TestClass()
      val result: Option[LoanedKey] = obj.restrictNumber(number = 25)
      result.get.GUID mustEqual 25
      result.get.Policy mustEqual AvailabilityPolicy.Restricted
      result.get.Object = test

      obj.returnNumber(number = 25)
      val result2: Option[SecureKey] = obj.get(25)
      result2.get.GUID mustEqual 25
      result2.get.Policy mustEqual AvailabilityPolicy.Restricted
      result2.get.Object.contains(test) mustEqual true
    }

    "return a secure key" in {
      val obj  = SpecificNumberSource(List(25))
      val test = new TestClass()

      val result1: Option[LoanedKey] = obj.getAvailable(number = 25)
      result1.get.Object = test
      test.GUID mustEqual PlanetSideGUID(25)

      val result2: Option[SecureKey] = obj.get(25)
      obj.returnNumber(result2.get).contains(test) mustEqual true
    }

    "restrict a previously-assigned number" in {
      val obj                        = SpecificNumberSource(List(25))
      val test                       = new TestClass()
      val result1: Option[LoanedKey] = obj.getAvailable(number = 25)
      result1.isDefined mustEqual true
      result1.get.Policy mustEqual AvailabilityPolicy.Leased
      result1.get.Object = test
      val result2: Option[LoanedKey] = obj.restrictNumber(number = 25)
      result2.isDefined mustEqual true
      result2.get.Policy mustEqual AvailabilityPolicy.Restricted
      result2.get.Object.contains(test) mustEqual true
    }

    "check a number (not previously gotten)" in {
      val obj                        = SpecificNumberSource(List(25))
      val result2: Option[SecureKey] = obj.get(25)
      result2.get.GUID mustEqual 25
      result2.get.Policy mustEqual AvailabilityPolicy.Available
      result2.get.Object.isEmpty mustEqual true
    }

    "check a number (previously gotten)" in {
      val obj                       = SpecificNumberSource(List(25))
      val result: Option[LoanedKey] = obj.getAvailable(number = 25)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 25
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object.isEmpty mustEqual true
      val result2: Option[SecureKey] = obj.get(25)
      result2.get.GUID mustEqual 25
      result2.get.Policy mustEqual AvailabilityPolicy.Leased
      result2.get.Object.isEmpty mustEqual true
    }

    "check a number (assigned)" in {
      val obj                       = SpecificNumberSource(List(25))
      val result: Option[LoanedKey] = obj.getAvailable(number = 25)
      result.isDefined mustEqual true
      result.get.GUID mustEqual 25
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object = new TestClass()
      val result2: Option[SecureKey] = obj.get(25)
      result2.get.GUID mustEqual 25
      result2.get.Policy mustEqual AvailabilityPolicy.Leased
      result2.get.Object mustEqual result.get.Object
    }

    "check a number (assigned and returned)" in {
      val obj                       = SpecificNumberSource(List(25))
      val test                      = new TestClass()
      val result: Option[LoanedKey] = obj.getAvailable(number = 25)
      result.get.Policy mustEqual AvailabilityPolicy.Leased
      result.get.Object = test
      val result2: Option[SecureKey] = obj.get(25)
      result2.get.Policy mustEqual AvailabilityPolicy.Leased
      result2.get.Object.get mustEqual test
      obj.returnNumber(number = 25).contains(test) mustEqual true
      val result3: Option[SecureKey] = obj.get(25)
      result3.get.Policy mustEqual AvailabilityPolicy.Available
      result3.get.Object.isEmpty mustEqual true
    }

    "clear" in {
      val obj   = SpecificNumberSource(List(25, 26, 27, 28, 29, 30))
      val test1 = new TestClass()
      val test2 = new TestClass()
      val test3 = new TestClass()
      obj.getAvailable(25) //no assignment
      obj.getAvailable(26).get.Object = test1
      obj.getAvailable(28).get.Object = test2
      obj.restrictNumber(28)
      obj.restrictNumber(30).get.Object = test3
      obj.countUsed mustEqual 4

      val list: List[IdentifiableEntity] = obj.clear()
      obj.countUsed mustEqual 0
      list.size mustEqual 3
      list.count(obj => obj == test1) mustEqual 1
      list.count(obj => obj == test2) mustEqual 1
      list.count(obj => obj == test3) mustEqual 1
    }
  }
}
