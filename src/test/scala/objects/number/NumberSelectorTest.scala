// Copyright (c) 2017 PSForever
package objects.number

import net.psforever.objects.guid.selector.{RandomSequenceSelector, _}
import org.specs2.mutable.Specification

class NumberSelectorTest extends Specification {
  def randArrayGen(n: Int = 26, dx: Int = 0): Array[Int] = {
    val obj = Array.ofDim[Int](n)
    (0 to 25).foreach(x => { obj(x) = x + dx })
    obj
  }

  "RandomSequenceSelector (0, default)" should {
    "construct" in {
      new RandomSequenceSelector
      ok
    }

    "get a number" in {
      val n: Int = 26
      val obj    = new RandomSequenceSelector
      obj.Get(randArrayGen(n)) mustNotEqual -1
    }

    "return a number" in {
      val n: Int = 26
      val obj    = new RandomSequenceSelector
      val ary    = randArrayGen(n)
      val number = obj.Get(ary)
      number mustNotEqual -1
      ary.head mustEqual -1 //regardless of which number we actually got, the head of the array is now -1
      obj.Return(number, ary)
      ary.head mustEqual number //the returned number is at the head of the array
    }

    "get all numbers" in {
      val n   = 26
      val obj = new RandomSequenceSelector
      val ary = randArrayGen(n)
      (0 until n).foreach(_ => { obj.Get(ary) mustNotEqual -1 })
      ok
    }

    "return all numbers" in {
      val n    = 26
      val obj  = new RandomSequenceSelector
      val ary1 = randArrayGen(n)
      val ary2 = randArrayGen(n)
      (0 until n).foreach(index => { ary2(index) = obj.Get(ary1) })                  //move numbers from ary1 to ary2
      ary2.toSet.diff(ary1.toSet).size mustEqual n                                   //no numbers between ary2 and ary1 match
      (0 until n).foreach(index => { obj.Return(ary2(index), ary1) mustEqual true }) //return numbers from ary2 to ary1
      ary2.toSet.diff(ary1.toSet).size mustEqual 0                                   //no difference in the content between ary2 and ary1
    }

    "gets invalid index when exhausted" in {
      val n   = 26
      val obj = new RandomSequenceSelector
      val ary = randArrayGen(n)
      (0 until n).foreach(_ => { obj.Get(ary) mustNotEqual -1 })
      obj.Get(ary) mustEqual -1
    }

    "format an array" in {
      val ary = Array[Int](1, -1, 5, 3, -1, 2)
      (new RandomSequenceSelector).Format(ary)
      ary mustEqual Array[Int](-1, -1, 1, 5, 3, 2)
    }
  }

  "RandomSelector" should {
    "construct" in {
      new RandomSelector
      ok
    }

    "get a number" in {
      val obj = new RandomSelector
      obj.Get(randArrayGen()) mustNotEqual -1
    }

    "return a number" in {
      val obj    = new RandomSelector
      val ary    = randArrayGen()
      val number = obj.Get(ary)
      number mustNotEqual -1
      ary.head mustEqual -1 //regardless of which number we actually got, the head of the array is now -1
      obj.Return(number, ary)
      ary.head mustEqual number //the returned number is at the head of the array
    }

    "get all numbers" in {
      val n   = 26
      val obj = new RandomSelector
      val ary = randArrayGen(n)
      (0 until n).foreach(_ => { obj.Get(ary) mustNotEqual -1 })
      ok
    }

    "return all numbers" in {
      val n    = 26
      val obj  = new RandomSelector
      val ary1 = randArrayGen(n)
      val ary2 = randArrayGen(n)
      (0 until n).foreach(index => { ary2(index) = obj.Get(ary1) })                  //move numbers from ary1 to ary2
      ary2.toSet.diff(ary1.toSet).size mustEqual n                                   //no numbers between ary2 and ary1 match
      (0 until n).foreach(index => { obj.Return(ary2(index), ary1) mustEqual true }) //return numbers from ary2 to ary1
      ary2.toSet.diff(ary1.toSet).size mustEqual 0                                   //no difference in the content between ary2 and ary1
    }

    "gets invalid index when exhausted" in {
      val n   = 26
      val obj = new RandomSelector
      val ary = randArrayGen(n)
      (0 until n).foreach(_ => { obj.Get(ary) mustNotEqual -1 })
      obj.Get(ary) mustEqual -1
    }

    "format an array" in {
      val ary = Array[Int](1, -1, 5, 3, -1, 2)
      (new RandomSelector).Format(ary)
      ary mustEqual Array[Int](-1, -1, 1, 5, 3, 2)
    }
  }

  "StrictInOrderSelector" should {
    "construct" in {
      new StrictInOrderSelector
      ok
    }

    "get a number" in {
      val obj = new StrictInOrderSelector
      obj.Get(randArrayGen()) mustNotEqual -1
    }

    "return a number" in {
      val obj    = new StrictInOrderSelector
      val ary    = randArrayGen()
      val number = obj.Get(ary)
      number mustNotEqual -1
      ary.head mustEqual -1 //regardless of which number we actually got, the head of the array is now -1
      obj.Return(number, ary)
      ary.head mustEqual number //the returned number is at the head of the array
    }

    "get all numbers" in {
      val n   = 26
      val obj = new StrictInOrderSelector
      val ary = randArrayGen()
      (0 until n).foreach(_ => { obj.Get(ary) mustNotEqual -1 })
      ok
    }

    "return all numbers" in {
      val n    = 26
      val obj  = new StrictInOrderSelector
      val ary1 = randArrayGen(n)
      val ary2 = randArrayGen(n)
      (0 until n).foreach(index => { ary2(index) = obj.Get(ary1) })                  //move numbers from ary1 to ary2
      ary2.toSet.diff(ary1.toSet).size mustEqual n                                   //no numbers between ary2 and ary1 match
      (0 until n).foreach(index => { obj.Return(ary2(index), ary1) mustEqual true }) //return numbers from ary2 to ary1
      ary2.toSet.diff(ary1.toSet).size mustEqual 0                                   //no difference in the content between ary2 and ary1
    }

    "gets invalid index when exhausted" in {
      val n   = 26
      val obj = new StrictInOrderSelector
      val ary = randArrayGen(n)
      (0 until n).foreach(_ => { obj.Get(ary) mustNotEqual -1 })
      obj.Get(ary) mustEqual -1
    }

    "wait until number is available" in {
      val n   = 26
      val obj = new StrictInOrderSelector
      val ary = randArrayGen(n)
      (0 until n).foreach(_ => { obj.Get(ary) mustNotEqual -1 })
      obj.Get(ary) mustEqual -1
      obj.Return(1, ary) //return a number that isn't the one StrictOrder is waiting on
      obj.Get(ary) mustEqual -1
      obj.Return(0, ary) //return the number StrictOrder wants
      obj.Get(ary) mustEqual 0
      obj.Get(ary) mustEqual 1
    }

    "format an array" in {
      val ary = Array[Int](1, -1, 5, 3, -1, 2)
      (new StrictInOrderSelector).Format(ary)
      ary mustEqual Array[Int](-1, 1, 2, 3, -1, 5)
    }
  }

  "OpportunisticSelector" should {
    "construct" in {
      new OpportunisticSelector
      ok
    }

    "get a number" in {
      val obj = new OpportunisticSelector
      obj.Get(randArrayGen()) mustNotEqual -1
    }

    "return a number" in {
      val obj    = new OpportunisticSelector
      val ary    = randArrayGen()
      val number = obj.Get(ary)
      number mustNotEqual -1
      ary.head mustEqual -1 //regardless of which number we actually got, the head of the array is now -1
      obj.Return(number, ary)
      ary.head mustEqual number //the returned number is at the head of the array
    }

    "get all numbers" in {
      val obj = new OpportunisticSelector
      val ary = randArrayGen()
      (0 to 25).foreach(_ => { obj.Get(ary) mustNotEqual -1 })
      ok
    }

    "return all numbers" in {
      val n    = 26
      val obj  = new OpportunisticSelector
      val ary1 = randArrayGen(n)
      val ary2 = randArrayGen(n)
      (0 until n).foreach(index => { ary2(index) = obj.Get(ary1) })                  //move numbers from ary1 to ary2
      ary2.toSet.diff(ary1.toSet).size mustEqual n                                   //no numbers between ary2 and ary1 match
      (0 until n).foreach(index => { obj.Return(ary2(index), ary1) mustEqual true }) //return numbers from ary2 to ary1
      ary2.toSet.diff(ary1.toSet).size mustEqual 0                                   //no difference in the content between ary2 and ary1
    }

    "gets invalid index when exhausted" in {
      val n   = 26
      val obj = new OpportunisticSelector
      val ary = randArrayGen(n)
      (0 until n).foreach(_ => { obj.Get(ary) mustNotEqual -1 })
      obj.Get(ary) mustEqual -1
    }

    "format an array" in {
      val ary = Array[Int](1, -1, 5, 3, -1, 2)
      (new OpportunisticSelector).Format(ary)
      ary mustEqual Array[Int](-1, -1, 1, 5, 3, 2)
    }
  }

  "SpecificSelector" should {
    "construct" in {
      new SpecificSelector
      ok
    }

    "get a number" in {
      val obj = new SpecificSelector
      val ary = randArrayGen()
      obj.SelectionIndex = 5
      obj.Get(ary) mustEqual 5
      obj.Get(ary) mustEqual -1 //now that 5 has been selected, the selector will only get a -1 from that position
    }

    "return a number" in {
      val obj = new SpecificSelector
      val ary = randArrayGen()
      obj.SelectionIndex = 5
      val number = obj.Get(ary)
      number mustEqual 5
      obj.Get(ary) mustEqual -1
      obj.Return(number, ary)
      obj.Get(ary) mustEqual number //the returned number is at the head of the array
    }

    "return a number (2)" in {
      val obj = new SpecificSelector
      val ary = randArrayGen()
      obj.SelectionIndex = 5
      val number = obj.Get(ary)
      number mustEqual 5
      obj.Get(ary) mustEqual -1
      ary(number) mustEqual -1

      obj.SelectionIndex = 10 //even if we move the selection index, the number will return to its last position
      obj.Return(number, ary)
      ary(number) mustEqual number //the returned number at the original index
      obj.Get(
        ary
      ) mustEqual 10 //of course, with the selection index changed, we will not get the same position next time
    }

    "get all numbers" in {
      val n   = 26
      val obj = new SpecificSelector
      val ary = randArrayGen(n)
      (0 until n).foreach(i => {
        obj.SelectionIndex = i
        obj.Get(ary) mustEqual i
      })
      ok
    }

    "return all numbers" in {
      val n    = 26
      val obj  = new SpecificSelector
      val ary1 = randArrayGen(n)
      val ary2 = randArrayGen(n)
      (0 until n).foreach(index => {
        obj.SelectionIndex = index
        ary2(index) = obj.Get(ary1)
      })                                                                             //move numbers from ary1 to ary2
      ary2.toSet.diff(ary1.toSet).size mustEqual n                                   //no numbers between ary2 and ary1 match
      (0 until n).foreach(index => { obj.Return(ary2(index), ary1) mustEqual true }) //return numbers from ary2 to ary1
      ary2.toSet.diff(ary1.toSet).size mustEqual 0                                   //no difference in the content between ary2 and ary1
    }

    "gets invalid index when exhausted" in {
      val obj = new SpecificSelector
      val ary = randArrayGen()
      obj.SelectionIndex = 5
      obj.Get(ary) mustEqual 5
      obj.Get(ary) mustEqual -1 //yes, it really is that simple
    }

    "format an array" in {
      val ary = Array[Int](1, -1, 5, 3, -1, 2)
      (new SpecificSelector).Format(ary)
      ary mustEqual Array[Int](-1, 1, 2, 3, -1, 5)
    }
  }
}
