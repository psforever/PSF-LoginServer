// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.pool

import net.psforever.objects.guid.selector.{NumberSelector, SpecificSelector}

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class GenericPool(
                   private val hub: mutable.LongMap[String],
                   private val max: Int,
                   private val poolName: String,
                   private val selectionFunc: (List[Long], Int) => Int
                 ) extends NumberPool {
  private val numbers: mutable.ListBuffer[Int]   = mutable.ListBuffer[Int]()
  private val selector: SpecificSelector = new SpecificSelector
  selector.SelectionIndex = -1

  def Numbers: List[Int] = numbers.toList

  def Count: Int = numbers.length

  def Selector: NumberSelector = selector

  def Selector_=(slctr: NumberSelector): Unit = { /* intentionally blank */ }

  def Get(): Try[Int] = {
    val specific = selector.SelectionIndex
    selector.SelectionIndex = -1 //clear
    if (specific == -1) {
      val number = selectionFunc(hub.keys.toList, max)
      if (number > -1) {
        hub += number.toLong -> poolName
        numbers += number
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
      true
    } else {
      false
    }
  }
}

object GenericPool {
  /**
    * Overloaded constructor that assigns a "numerical first discovery" function for number selection.
    * @param hub na
    * @param max na
    * @param poolName na
    * @return a `GenericPool` entity
    */
  def apply(
             hub: mutable.LongMap[String],
             max: Int,
             poolName: String
           ): GenericPool =
    new GenericPool(hub, max, poolName, GenericPool.first)

  /**
    * Get some number that is not accounted for in any other fixed pool, making it available in this generic one.<br>
    * <br>
    * Returns the first number that is detected as available between two sorted numbers.
    * @param list all of the non-repeating numbers to be compared
    * @param domainSize how many numbers can be supported
    * @return the next available number, or -1
    */
  def first(list: List[Long], domainSize: Int): Int = {
    if (list.size < domainSize) {
      val sortedList: List[Long] = 0L +: list.sorted :+ domainSize
      var index: Int = 0
      val listLen = sortedList.length - 1
      while(index < listLen && index < domainSize) {
        val curr = sortedList(index + 1) - sortedList(index)
        if (curr > 1) {
          return sortedList(index).toInt + 1
        }
        index += 1
      }
    }
    -1
  }

  /**
    * Get some number that is not accounted for in any other fixed pool, making it available in this generic one.<br>
    * <br>
    * Although called "`rand`," this algorithm is not actually random.
    * From a sorted list of numbers, with a minimum and a maximum value appended,
    * it finds the two adjacent numbers that are the most distant.
    * It finds an average whole integer number between the two.<br>
    * <br>
    * This solution gets expensive as the count of numbers in `list` increases.
    * @param list all of the non-repeating numbers to be compared
    * @param domainSize how many numbers can be supported
    * @return midpoint of the largest distance between any two of the existing numbers, or -1
    */
  def rand(list: List[Long], domainSize: Int): Int = {
    if (list.size < domainSize) {
      //get a list of all assigned numbers with an appended min and max
      val sortedList: List[Long] = -1L +: list.sorted :+ domainSize.toLong
      //compare the delta between every two entries and find the start of that greatest delta comparison
      var maxDelta: Long = -1
      var maxDeltaIndex  = -1
      for (index <- 0 until (sortedList.length - 1)) {
        val curr = sortedList(index + 1) - sortedList(index)
        if (curr > maxDelta) {
          maxDelta = curr
          maxDeltaIndex = index
        }
      }
      //find half of the distance between the two numbers with the greatest delta value
      if (maxDelta == 2) { sortedList(maxDeltaIndex).toInt + 1 }
      else if (maxDelta > 1) { ((sortedList(maxDeltaIndex + 1) + sortedList(maxDeltaIndex)) / 2f).toInt }
      else { -1 }
    } else {
      -1
    }
  }
}
