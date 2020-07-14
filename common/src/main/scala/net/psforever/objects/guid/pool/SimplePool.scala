// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.pool

import net.psforever.objects.guid.selector.{NumberSelector, StrictInOrderSelector}

import scala.util.{Success, Try}

class SimplePool(val numbers: List[Int]) extends NumberPool {
  if (numbers.count(_ < 0) > 0) {
    throw new IllegalArgumentException("negative numbers not allowed in number pool")
  } else if (numbers.length != numbers.toSet.size) {
    throw new IllegalArgumentException("duplicate numbers not allowed in number pool")
  }
  private var selector: NumberSelector = new StrictInOrderSelector

  def Numbers: List[Int] = numbers

  def Count: Int = 0

  def Selector: NumberSelector = selector

  def Selector_=(slctr: NumberSelector): Unit = {
    selector = slctr
  }

  def Get(): Try[Int] = {
    val ary   = numbers.indices.toArray
    val index = selector.Get(ary)
    selector.Return(index, ary) //reset, for the benefit of the selector
    Success(numbers(index))
  }

  def Return(number: Int): Boolean = numbers.indexOf(number) > -1
}
