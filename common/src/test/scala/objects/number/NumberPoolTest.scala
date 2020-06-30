// Copyright (c) 2017 PSForever
package objects.number

import net.psforever.objects.guid.pool.{ExclusivePool, GenericPool, SimplePool}
import net.psforever.objects.guid.selector.SpecificSelector
import org.specs2.mutable.Specification

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Success

class NumberPoolTest extends Specification {
  "SimplePool" should {
    "construct" in {
      new SimplePool(0 :: 1 :: 2 :: Nil)
      ok
    }

    "fail to construct 1 (number less than zero)" in {
      new SimplePool(-1 :: Nil) must throwA[IllegalArgumentException]
    }

    "fail to construct 2 (duplicate numbers)" in {
      new SimplePool(1 :: 1 :: Nil) must throwA[IllegalArgumentException]
    }

    "get a number" in {
      val min    = 10
      val max    = 20
      val domain = (min to max).toList
      val obj    = new SimplePool(domain)
      obj.Get() match {
        case Success(number) =>
          (min <= number && number <= max) mustEqual true
        case _ =>
          ko
      }
      ok
    }

    "used number count is always zero" in {
      val obj = new SimplePool((0 to 10).toList)
      obj.Count mustEqual 0
      obj.Get()
      obj.Count mustEqual 0
    }

    "return a number" in {
      //returning a number for a SimplePool is actually just a way of checking that the number is in the "pool" at all
      val obj = new SimplePool((0 to 10).toList)
      obj.Get() match {
        case Success(number) =>
          obj.Return(number) mustEqual true
          obj.Return(11) mustEqual false
          obj.Return(number) mustEqual true
        case _ =>
          ko
      }
    }

    "numbers remain available" in {
      val obj = new SimplePool((0 to 10).toList)
      obj.Selector = new SpecificSelector
      obj.Selector.asInstanceOf[SpecificSelector].SelectionIndex = 8
      obj.Get() mustEqual Success(8)
      obj.Get() mustEqual Success(8) //compare to how SpecificSelector works otherwise - it would be an invalid return
    }
  }

  "ExclusivePool" should {
    "construct" in {
      new ExclusivePool(0 :: 1 :: 2 :: Nil)
      ok
    }

    "get a number" in {
      val min    = 10
      val max    = 20
      val domain = (min to max).toList
      val obj    = new ExclusivePool(domain)
      obj.Get() match {
        case Success(number) =>
          (min <= number && number <= max) mustEqual true
        case _ =>
          ko
      }
      ok
    }

    "get all the numbers" in {
      val min    = 10
      val max    = 20
      val domain = (min to max).toList
      val obj    = new ExclusivePool(domain)
      domain.foreach(_ => {
        obj.Get() match {
          case Success(number) =>
            (min <= number && number <= max) mustEqual true
          case _ =>
            ko
        }
      })
      ok
    }

    "return a number" in {
      val obj = new ExclusivePool((0 to 10).toList)
      obj.Get() match {
        case Success(number) =>
          try { obj.Return(number) mustEqual true }
          catch { case _: Exception => ko }
        case _ =>
          ko
      }
    }

    "return all the numbers" in {
      val range                 = 0 to 10
      val obj                   = new ExclusivePool((0 to 10).toList)
      val list: ListBuffer[Int] = ListBuffer[Int]()
      range.foreach(_ => {
        obj.Get() match {
          case Success(number) =>
            list += number
          case _ =>
        }
      })
      list.foreach(number => {
        try { obj.Return(number) mustEqual true }
        catch { case _: Exception => ko }
      })
      ok
    }
  }

  "GenericPool" should {
    "construct" in {
      new GenericPool(mutable.LongMap[String](), 11)
      ok
    }

    "get a provided number" in {
      val map = mutable.LongMap[String]()
      val obj = new GenericPool(map, 11)
      obj.Numbers.isEmpty mustEqual true
      obj.Selector.asInstanceOf[SpecificSelector].SelectionIndex = 5
      obj.Get() match {
        case Success(number) =>
          number mustEqual 5
          map.contains(5) mustEqual true
          map(5) mustEqual "generic"
          obj.Numbers.contains(5) mustEqual true
        case _ =>
          ko
      }
    }

    "return a number" in {
      val map = mutable.LongMap[String]()
      val obj = new GenericPool(map, 11)
      obj.Selector.asInstanceOf[SpecificSelector].SelectionIndex = 5
      obj.Get()
      map.get(5).contains("generic") mustEqual true
      obj.Numbers.contains(5) mustEqual true
      obj.Return(5) mustEqual true
      map.get(5).isEmpty mustEqual true
      obj.Numbers.isEmpty mustEqual true
    }

    "block on numbers that are already defined" in {
      val map = mutable.LongMap[String]()
      map += 5L -> "test" //5 is defined
      val obj = new GenericPool(map, 11)
      obj.Numbers.isEmpty mustEqual true
      obj.Selector.asInstanceOf[SpecificSelector].SelectionIndex = 5 //5 is requested
      obj.Get() match {
        case Success(_) =>
          ko
        case _ =>
          obj.Numbers.isEmpty mustEqual true
      }
    }

    "get a free number on own if none provided" in {
      val map = mutable.LongMap[String]()
      val obj = new GenericPool(map, 11)
      obj.Get() match {
        case Success(number) =>
          number mustEqual 5
        case _ =>
          ko
      }
    }

    "get a free number that is not already defined" in {
      val map = mutable.LongMap[String]()
      map += 5L -> "test" //5 is defined; think, -1 :: 5 :: 11
      val obj = new GenericPool(map, 11)
      obj.Get() match {
        case Success(number) =>
          number mustEqual 2 // think, -1 :: 2 :: 5 :: 11
        case _ => ko
      }

    }

    "get a free number that represents half of the largest delta" in {
      val map = mutable.LongMap[String]()
      map += 5L -> "test" //5 is defined; think, -1 :: 5 :: 11
      map += 4L -> "test" //4 is defined; think, -1 :: 4 :: 5 :: 11
      val obj = new GenericPool(map, 11)
      obj.Get() match {
        case Success(number) =>
          number mustEqual 8 // think, -1 :: 4 :: 5 :: 8 :: 11
        case _ =>
          ko
      }
    }
  }
}
