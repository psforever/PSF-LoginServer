// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.pool

import net.psforever.objects.guid.selector.NumberSelector

import scala.util.{Failure, Success, Try}

class ExclusivePool(override val numbers: List[Int]) extends SimplePool(numbers) {
  private val pool: Array[Int] = Array.ofDim[Int](numbers.length)
  numbers.indices.foreach(i => { pool(i) = i })

  /* `numbers` is a List, so both hot operations walked it: Get did numbers(index), which is
     O(index), and Return did an O(n) indexOf. The configured pools are large -- deployables
     16000, ammo and kits 13500 each -- and unregistration happens in bulk when a player dies
     or a vehicle is deconstructed, all serialised through one pool actor.
     An indexed copy and a reverse lookup make both constant time. Neither is mutated after
     construction, and SimplePool has already rejected duplicates. */
  private val numbersByIndex: Array[Int] = numbers.toArray
  private val indexByNumber: Map[Int, Int] = numbers.zipWithIndex.toMap

  override def Count: Int = pool.count(value => value == -1)

  override def Selector_=(slctr: NumberSelector): Unit = {
    super.Selector_=(slctr)
    slctr.Format(pool)
  }

  override def Get(): Try[Int] = {
    val index: Int = Selector.Get(pool)
    if (index == -1) {
      Failure(new Exception("there are no numbers available in the pool"))
    } else {
      Success(numbersByIndex(index))
    }
  }

  override def Return(number: Int): Boolean = {
    indexByNumber.get(number).exists(index => Selector.Return(index, pool))
  }
}
