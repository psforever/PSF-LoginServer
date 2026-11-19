// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.pool

import net.psforever.objects.guid.selector.NumberSelector

import scala.util.Try

trait NumberPool {
  def Numbers: List[Int]

  /**
    * The index of `number` in this pool, which is what a `SpecificSelector` takes to select it.
    * @param number the number
    * @return its index, or -1 if it is not in this pool
    */
  def IndexOf(number: Int): Int = Numbers.indexOf(number)

  def Count: Int

  def Selector: NumberSelector

  def Selector_=(slctr: NumberSelector): Unit

  def Get(): Try[Int]

  def Return(number: Int): Boolean
}
