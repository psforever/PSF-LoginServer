// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.selector

/**
  * Get a pseudorandom number from a pool of numbers.
  * The output of this class, operating on an `Array` of `Int` values is contained to some sequence of all the numbers.
  * Only after every number is selected once, may any number repeat.
  * The pseudorandomness of any sequence of numbers is not only provided by an internal system `Random` but by the order or returned numbers.
  * Consequentially, as any single sequence nears completion, the numbers remaining become more and more predictable.<br>
  * <br>
  * During the selection process:<br>
  * The index is the position from where the selection begins, and the end of the `Array` is where the selection ends.
  * Once a position between those two indices is selected, that number is extracted.
  * The number at the start position is swapped into the position where the selection number was extracted.
  * The start position is then set to an invalid number, and the start index is advanced.
  * Repeat next request.<br>
  * <br>
  * The return index trails behind the selection index as far as the order of the array is concerned at first.
  * After some time, the selection index moves to the starting position of the array again and then the order is reversed.
  * Until the return index wraps around to the beginning of the array too, it is considered the valid selection end position.<br>
  * <br>
  * During the return process:<br>
  * As the `Array` empties out from the first to the last index, the return process starts at the first index again.
  * When a number is "returned," it is placed back into the input `Array` at the earliest available index.
  * The return index is advanced.
  * Neither the selection index nor the return index may pass each other,
  * except when one reaches the end of the `Array` and wraps back around to that start.
  * @see `RandomSelector`
  */
class RandomSequenceSelector extends NumberSelector {
  private val rand: scala.util.Random = new scala.util.Random(System.currentTimeMillis())

  /**
    * Accept a provided "pool of numbers" and select the next number.
    * @param ary the `Array` of `Int` numbers from which to draw a new number
    * @return an `Int` number
    */
  override def Get(ary: Array[Int]): Int = {
    val last: Int = if (ret <= selectionIndex) { ary.length }
    else { ret }
    val selection: Int = rand.nextInt(last - selectionIndex) + selectionIndex
    val out: Int       = ary(selection)
    ary(selection) = ary(selectionIndex)
    ary(selectionIndex) = -1
    selectionIndex =
      (selectionIndex + (out >> 31) + 1) % ary.length //(out >> 31): 0 if positive or zero, -1 if negative
    out
  }

  /**
    * Give a number back to a specific collection following the principles of this selector.
    * @param number the number to be returned
    * @param ary the `Array` of `Int` numbers to which the number is to be returned
    * @return `true`, if this return was successful; `false`, otherwise
    */
  override def Return(number: Int, ary: Array[Int]): Boolean = {
    if (ary(ret) == -1) {
      ary(ret) = number
      ret = (ret + 1) % ary.length
      true
    } else {
      false
    }
  }
}
