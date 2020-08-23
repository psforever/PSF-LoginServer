// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.selector

/**
  * Get the next number in this pool incrementally.
  * Starting at index 0, for example, select each subsequent number as it is available.
  * Do not progress if a number is not available when requested.
  */
class StrictInOrderSelector extends NumberSelector {
  override def Get(ary: Array[Int]): Int = {
    val out: Int = ary(selectionIndex)
    ary(selectionIndex) = -1
    selectionIndex =
      (selectionIndex + (out >> 31) + 1) % ary.length //(out >> 31): 0 if positive or zero, -1 if negative
    out
  }

  /**
    * Accept the indexing pool from which numbers are selected and returned.
    * Correct its format to suit the current `NumberSelector` algorithms.<br>
    * <br>
    * All of the numbers are sorted to their proper indexed position in the `Array`.
    * Every other number is an invalid negative-one (-1).
    * The `selectionIndex` is set to the index of the first valid number, or zero if there are none.
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
    selectionIndex = ary.find(n => n > -1).getOrElse(0)
    ret = 0
  }
}
