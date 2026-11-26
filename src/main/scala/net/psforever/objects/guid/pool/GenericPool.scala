// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.pool

import net.psforever.objects.guid.selector.{NumberSelector, SpecificSelector}

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class GenericPool(
                   private val hub: NumberPoolNames,
                   private val max: Int,
                   private val poolName: String
                 ) extends NumberPool {
  private val numbers: mutable.ListBuffer[Int]   = mutable.ListBuffer[Int]()
  private val selector: SpecificSelector = new SpecificSelector
  selector.SelectionIndex = -1

  private var searchFrom: Int = 1

  def Numbers: List[Int] = numbers.toList

  def Count: Int = numbers.length

  def Selector: NumberSelector = selector

  def Selector_=(slctr: NumberSelector): Unit = { /* intentionally blank */ }

  def Get(): Try[Int] = {
    val specific = selector.SelectionIndex
    selector.SelectionIndex = -1 //clear
    if (specific == -1) {
      val number = lowestFree()
      if (number > -1) {
        hub += number.toLong -> poolName
        numbers += number
        searchFrom = number + 1
        Success(number)
      } else {
        Failure(new Exception("no numbers available in this pool"))
      }
    } else if (hub.get(specific).isEmpty) {
      hub += specific.toLong -> poolName
      numbers += specific
      Success(specific)
    } else {
      Failure(new Exception("selector may not have been initialized properly"))
    }
  }

  def Return(number: Int): Boolean = {
    val index: Int = numbers.indexOf(number)
    if (index > -1) {
      numbers.remove(index)
      hub -= number
      if (number < searchFrom) {
        searchFrom = number
      }
      true
    } else {
      false
    }
  }

  private def lowestFree(): Int = {
    var number = math.max(searchFrom, 1)
    while (number < max && hub.contains(number.toLong)) {
      number += 1
    }
    searchFrom = number
    if (number < max) number else -1
  }
}

object GenericPool {
  /**
    * Overloaded constructor.
    * @param hub every number known to the hub, mapped to the name of the pool that holds it
    * @param max how many numbers the source supports
    * @param poolName the name recorded in `hub` for numbers this pool takes
    * @return a `GenericPool` entity
    */
  def apply(
             hub: NumberPoolNames,
             max: Int,
             poolName: String
           ): GenericPool =
    new GenericPool(hub, max, poolName)
}
