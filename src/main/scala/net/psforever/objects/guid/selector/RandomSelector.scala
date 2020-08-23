// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.selector

/**
  * Get a pseudorandom number from a pool of numbers.
  * The contained logic is similar to `RandomSequenceSelector`.
  * It is not reliant of a shrinking pool that composes into some sequence of all the numbers, however;
  * the numbers are re-introduced to the selection as long as the pool is used.
  * This allows for the sequence to contain repeat numbers far before ever visiting all of the numbers once.<br>
  * <br>
  * During the selection process:<br>
  * The index is the position from where the selection begins, and the end of the `Array` is where the selection ends.
  * Once a position between those two indices is selected, that number is extracted.
  * The number at the start position is swapped into the position where the selection number was extracted.
  * The start position is then set to an invalid number, and the start index is advanced.
  * Repeat next request.<br>
  * <br>
  * During the return process:<br>
  * The returned number is added to the input `Array` at the position just before the current selection position.
  * The selection index is then reversedback to re-include the returned number.
  * The normal return index is not used in this algorithm.
  * @see `RandomSequenceSelector`
  */
class RandomSelector extends NumberSelector {
  private val rand: scala.util.Random = new scala.util.Random(System.currentTimeMillis())

  /**
    * Accept a provided `pool` and select the next number.<br>
    * <br>
    * ...
    * @param ary the `Array` of `Int` numbers from which to draw a new number
    * @return an `Int` number
    */
  override def Get(ary: Array[Int]): Int = {
    if (ary.length > selectionIndex) {
      val selection: Int = rand.nextInt(ary.length - selectionIndex) + selectionIndex
      val out: Int       = ary(selection)
      ary(selection) = ary(selectionIndex)
      ary(selectionIndex) = -1
      selectionIndex = selectionIndex + (out >> 31) + 1 //(out >> 31): 0 if positive or zero, -1 if negative
      out
    } else {
      -1
    }
  }

  /**
    * Give a number back to a specific collection following the principles of this selector.<br>
    * <br>
    * The number is always returned to a "used" index position near the front of the array.
    * It locates this position by incrementally traversing the `Array` behind the position used in `Get`.
    * Asides from selection, a disorderly reinsertion of numbers back into the pool is also a source of randomness.
    * @param number the number to be returned
    * @param ary the `Array` of `Int` numbers to which the number is to be returned
    * @return `true`, if this return was successful; `false`, otherwise
    */
  override def Return(number: Int, ary: Array[Int]): Boolean = {
    if (selectionIndex > 0) {
      ary(selectionIndex - 1) = number
      selectionIndex -= 1
      true
    } else {
      false
    }
  }
}
