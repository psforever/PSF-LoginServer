// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.selector

/**
  * Get a specific number from a pool of numbers.
  */
class SpecificSelector extends NumberSelector {

  /**
    * Change the future selection index to match the number the user wants.
    * Call `Get` to complete process.
    * @param number the number
    */
  def SelectionIndex_=(number: Int): Unit = {
    selectionIndex = number
  }

  /**
    * Get the specified number and the specified number only.
    * @param ary the `Array` of `Int` numbers from which to draw a new number
    * @return an `Int` number
    */
  override def Get(ary: Array[Int]): Int = {
    if (-1 < selectionIndex && selectionIndex < ary.length) {
      val out = ary(selectionIndex)
      ary(selectionIndex) = -1
      out
    } else {
      -1
    }
  }

  /**
    * Accept the indexing pool from which numbers are selected and returned.
    * Correct its format to suit the current `NumberSelector` algorithms.<br>
    * <br>
    * All of the numbers are sorted to their proper indexed position in the `Array`.
    * Every other number is an invalid negative-one (-1).
    * The `selectionIndex` is also set to an invalid negative-one, as per the requirements of the selector.
    * The `ret` index is set to index zero.
    * @param ary the `Array` of `Int` numbers
    */
  override def Format(ary: Array[Int]): Unit = {
    val sorted = Array.fill(ary.length)(-1)
    ary.foreach(n => {
      if (n > -1) {
        sorted(n) = n
      }
    })
    sorted.copyToArray(ary)
    selectionIndex = -1
    ret = 0
  }
}
