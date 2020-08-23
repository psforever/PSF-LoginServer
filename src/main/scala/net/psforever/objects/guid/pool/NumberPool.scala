// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.pool

import net.psforever.objects.guid.selector.NumberSelector

import scala.util.Try

trait NumberPool {
  def Numbers: List[Int]

  def Count: Int

  def Selector: NumberSelector

  def Selector_=(slctr: NumberSelector): Unit

  def Get(): Try[Int]

  def Return(number: Int): Boolean
}
