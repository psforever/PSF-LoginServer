// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.pool

import net.psforever.objects.guid.selector.NumberSelector

import scala.util.{Failure, Success, Try}

class ExclusivePool(override val numbers: List[Int]) extends SimplePool(numbers) {
  private val pool: Array[Int] = Array.ofDim[Int](numbers.length)
  numbers.indices.foreach(i => { pool(i) = i })

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
      Success(numbers(index))
    }
  }

  override def Return(number: Int): Boolean = {
    val index = Numbers.indexOf(number)
    index != -1 && Selector.Return(index, pool)
  }
}
