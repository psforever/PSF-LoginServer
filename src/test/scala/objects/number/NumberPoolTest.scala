// Copyright (c) 2017 PSForever
package objects.number

import net.psforever.objects.guid.pool.{ExclusivePool, GenericPool, SimplePool}
import net.psforever.objects.guid.selector.{RandomSelector, SpecificSelector}
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

    "fail to construct with a negative number" in {
      new ExclusivePool(-1 :: 0 :: 1 :: Nil) must throwA[IllegalArgumentException]
      new ExclusivePool(3 :: -1 :: 7 :: Nil) must throwA[IllegalArgumentException]
    }

    "fail to construct with duplicate numbers" in {
      new ExclusivePool(3 :: 7 :: 3 :: Nil) must throwA[IllegalArgumentException]
    }

    "enumerate its numbers in the order given, contiguous or not" in {
      new ExclusivePool((10 to 20).toList).Numbers mustEqual (10 to 20).toList
      val scattered = List(40, 3, 17, 25, 4)
      new ExclusivePool(scattered).Numbers mustEqual scattered
    }

    "find the index of each of its numbers, and -1 for any other, contiguous or not" in {
      val contiguous = new ExclusivePool((10 to 20).toList)
      (10 to 20).map(contiguous.IndexOf) mustEqual (0 to 10)
      Seq(-1, 0, 9, 21, Int.MaxValue).map(contiguous.IndexOf) mustEqual Seq(-1, -1, -1, -1, -1)
      val scattered = List(40, 3, 17, 25, 4)
      val pool      = new ExclusivePool(scattered)
      scattered.map(pool.IndexOf) mustEqual scattered.indices.toList
      Seq(-1, 0, 5, 18, 41, Int.MaxValue).map(pool.IndexOf) mustEqual Seq(-1, -1, -1, -1, -1, -1)
    }

    "get and return every number of a pool that is not contiguous" in {
      val scattered = List(40, 3, 17, 25, 4)
      val obj       = new ExclusivePool(scattered)
      obj.Selector = new RandomSelector
      val drawn = scattered.map(_ => obj.Get().get)
      drawn.sorted mustEqual scattered.sorted
      obj.Get().isFailure mustEqual true
      obj.Count mustEqual scattered.size
      drawn.forall(obj.Return) mustEqual true
      obj.Count mustEqual 0
      obj.Return(5) mustEqual false //not in the pool
    }

    "get a specific number through a SpecificSelector, contiguous or not" in {
      Seq((10 to 20).toList, List(40, 3, 17, 25, 4)).forall { numbers =>
        val obj      = new ExclusivePool(numbers)
        val specific = new SpecificSelector
        obj.Selector = specific
        specific.SelectionIndex = obj.IndexOf(numbers(3))
        obj.Get() == Success(numbers(3))
      } mustEqual true
    }
  }

  "GenericPool" should {
    "construct" in {
      GenericPool(mutable.LongMap[String](), max = 11, poolName = "generic")
      ok
    }

    "get a provided number" in {
      val map = mutable.LongMap[String]()
      val obj = GenericPool(map, max = 11, poolName = "generic")
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
      val obj = GenericPool(map, max = 11, poolName = "generic")
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
      val obj = GenericPool(map, max = 11, poolName = "generic")
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
      val obj = GenericPool(map, max = 11, poolName = "generic")
      obj.Get() match {
        case Success(number) =>
          number mustEqual 1
        case _ =>
          ko
      }
    }

    "get a free number that is not already defined" in {
      val map = mutable.LongMap[String]()
      map += 5L -> "test" //5 is defined; think, -1 :: 5 :: 11
      val obj = GenericPool(map, max = 11, poolName = "generic")
      obj.Get() match {
        case Success(number) =>
          number mustEqual 1 // think, -1 :: 2 :: 5 :: 11
        case _ => ko
      }

    }

    "get the lowest free number when several low numbers are defined" in {
      val map = mutable.LongMap[String]()
      map += 5L -> "test" //5 is defined; think, -1 :: 5 :: 11
      map += 4L -> "test" //4 is defined; think, -1 :: 4 :: 5 :: 11
      val obj = GenericPool(map, max = 11, poolName = "generic")
      obj.Get() match {
        case Success(number) =>
          number mustEqual 1 // think, -1 :: 4 :: 5 :: 8 :: 11
        case _ =>
          ko
      }
    }

    "get successive numbers, stepping over those defined" in {
      val map = mutable.LongMap[String]()
      map ++= Seq(1L -> "test", 2L -> "test", 4L -> "test")
      val obj = GenericPool(map, max = 11, poolName = "generic")
      (1 to 4).map(_ => obj.Get().get) mustEqual Seq(3, 5, 6, 7)
    }

    "step over numbers defined after construction" in {
      val map = mutable.LongMap[String]()
      val obj = GenericPool(map, max = 11, poolName = "generic")
      obj.Get().get mustEqual 1
      map ++= Seq(2L -> "test", 3L -> "test") //another pool takes 2 and 3
      obj.Get().get mustEqual 4
    }

    "reuse the lowest returned number" in {
      val map = mutable.LongMap[String]()
      val obj = GenericPool(map, max = 11, poolName = "generic")
      (1 to 5).foreach(_ => obj.Get())
      obj.Return(4) mustEqual true
      obj.Return(2) mustEqual true
      obj.Get().get mustEqual 2
      obj.Get().get mustEqual 4
      obj.Get().get mustEqual 6
    }

    "fail when every number is taken, then succeed after a return" in {
      val map = mutable.LongMap[String]()
      val obj = GenericPool(map, max = 4, poolName = "generic") //numbers 1, 2, 3
      (1 to 3).map(_ => obj.Get().get) mustEqual Seq(1, 2, 3)
      obj.Get().isFailure mustEqual true
      obj.Return(2) mustEqual true
      obj.Get().get mustEqual 2
      obj.Get().isFailure mustEqual true
    }

    "always get the lowest free number over a random sequence of operations" in {
      val max = 1024
      val map = mutable.LongMap[String]()
      val obj = GenericPool(map, max, poolName = "generic")
      val rng = new scala.util.Random(1400)
      def expectedLowest: Option[Int] = (1 until max).find(n => !map.contains(n.toLong))
      val mismatches = (1 to 5000).flatMap { step =>
        rng.nextInt(10) match {
          case r if r < 5 => //draw
            val expected = expectedLowest
            val got      = obj.Get().toOption
            if (got != expected) Some(s"step $step: got $got, expected $expected") else None
          case 9 => //another pool takes a free number
            val n = (1 + rng.nextInt(max - 1)).toLong
            if (!map.contains(n)) map += n -> "test"
            None
          case _ => //return one of ours
            val held = obj.Numbers
            if (held.nonEmpty) obj.Return(held(rng.nextInt(held.size)))
            None
        }
      }
      mismatches must beEmpty
    }
  }
}
