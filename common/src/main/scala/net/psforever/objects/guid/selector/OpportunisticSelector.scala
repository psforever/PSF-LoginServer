// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.selector

/**
  * Get whichever number is next available.
  * It is similar to `StrictInOrderSelector` but it does not stop if it runs into an unavailable number.
  * It attempts to get each number in its listed incrementally from a starting index.
  * The search wraps back around to the zero index to the same start index if necessary.
  */
class OpportunisticSelector extends NumberSelector {
  override def Get(ary: Array[Int]): Int = {
    val start: Int = selectionIndex
    if (ary(selectionIndex) == -1) {
      val len: Int = ary.length
      do {
        selectionIndex = (selectionIndex + 1) % len
      } while (ary(selectionIndex) == -1 && selectionIndex != start)
    }
    val out: Int = ary(selectionIndex)
    ary(selectionIndex) = -1
    selectionIndex =
      (selectionIndex + (out >> 31) + 1) % ary.length //(out >> 31): 0 if positive or zero, -1 if negative
    out
  }
}
