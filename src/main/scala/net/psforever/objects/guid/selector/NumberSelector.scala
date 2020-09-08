// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.selector

/**
  * The base class for all different sorts of number selection policies.<br>
  * <br>
  * The `Array`s called out as method parameters is always an `Array` of indexes for some other list.
  * The indices in the `Array` are always the complete range of 0 to `n` numbers.
  * It is recommended to initialize the `Array` with the rule `array(number) = number`.
  * When they need to be flagged as "invalid" in some way, use some consistent system of negative numbers.
  * (Recommendation: unless doing something fancy, just use -1.)
  */
abstract class NumberSelector {

  /** The index for the selector when performing a number selection action, then modified to the "next" index. */
  protected var selectionIndex: Int = 0

  /** The index for the selector when performing a number return action, then modified for the "next" index. */
  protected var ret: Int = 0

  def SelectionIndex: Int = selectionIndex

  def ReturnIndex: Int = ret

  /**
    * Accept a provided `pool` and select the next number.<br>
    * <br>
    * The main requirement for valid implementation of a `Get` selector is atomicity.
    * While `Get` could be written to run again for every failure, this should not be anticipated.
    * A success means a "success."
    * A failure means that no "success" would be possible no matter how many times it might be run under the current conditions.
    * The aforementioned conditions may change depending on the nature of the specific selector;
    * but, the previous requirement should not be violated.<br>
    * <br>
    * `Get` is under no obligation to not modify its parameter `Array`.
    * In fact, it should do this by default to provide additional feedback of its process.
    * Pass a copy if data mutation is a concern.
    * @param ary the `Array` of `Int` numbers from which to draw a new number
    * @return an `Int` number
    */
  def Get(ary: Array[Int]): Int

  /**
    * Give a number back to a specific collection following the principles of this selector.<br>
    * <br>
    * By default, a simple policy for returning numbers has been provided.
    * This will not be sufficient for all selection actions that can be implemented so `override` where necessary.
    * <br>
    * `returnNumber` is under no obligation to leave its parameter `Array` unmodified.
    * In fact, it should modify it by default to provide additional feedback of its process.
    * Pass a copy if data mutation is a concern.
    * @param number the number to be returned
    * @param ary the `Array` of `Int` numbers to which the number is to be returned
    * @return `true`, if this return was successful; `false`, otherwise
    */
  def Return(number: Int, ary: Array[Int]): Boolean = {
    if (ary(number) == -1) {
      ary(number) = number
      ret = number
      true
    } else {
      false
    }
  }

  /**
    * Accept the indexing pool from which numbers are selected and returned.
    * Correct its format to suit the current `NumberSelector` algorithms.<br>
    * <br>
    * Moving all of the invalid negative-ones (-1) to the left of the current valid indices works for most selectors.
    * The `selectionIndex` is set to the first valid number available from the left.
    * The `ret` index is set to index zero.
    * @param ary the `Array` of `Int` numbers
    */
  def Format(ary: Array[Int]): Unit = {
    val sorted = ary.sortWith((b, a) =>
      if (b == -1) { a > b }
      else { false }
    )
    sorted.indices.foreach(n => ary(n) = sorted(n))
    selectionIndex = sorted.count(_ == -1)
    ret = 0
  }
}
