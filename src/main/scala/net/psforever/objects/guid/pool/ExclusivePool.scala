// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.pool

import net.psforever.objects.guid.selector.{NumberSelector, StrictInOrderSelector}

import scala.util.{Failure, Success, Try}

class ExclusivePool(numbers: List[Int]) extends NumberPool {
  if (numbers.count(_ < 0) > 0) {
    throw new IllegalArgumentException("negative numbers not allowed in number pool")
  } else if (numbers.length != numbers.toSet.size) {
    throw new IllegalArgumentException("duplicate numbers not allowed in number pool")
  }
  private var selector: NumberSelector = new StrictInOrderSelector
  private val length: Int = numbers.length
  private val pool: Array[Int] = Array.ofDim[Int](numbers.length)
  numbers.indices.foreach(i => { pool(i) = i })

  /* Constant-time views over `numbers`, which is a List: an indexed copy for resolving a
     selector index to its number, and a reverse lookup for resolving a number back to its
     index. Both operations are hot -- the configured pools are large (deployables 16000,
     ammo and kits 13500 each) and unregistration arrives in bulk when a player dies or a
     vehicle is deconstructed, all serialised through one pool actor.
     Neither structure is mutated after construction, and duplicates have already been
     rejected, so the reverse lookup is unambiguous.
     The pools a zone defines are contiguous ascending ranges. For those, both views are
     arithmetic on the first number, and the structures are left empty. */
  private val first: Int = numbers.headOption.getOrElse(0)
  private val contiguous: Boolean = numbers.iterator.zipWithIndex.forall { case (number, i) => number == first + i }
  private val numbersByIndex: Array[Int] = if (contiguous) Array.emptyIntArray else numbers.toArray
  private val indexByNumber: Map[Int, Int] = if (contiguous) Map.empty else numbers.zipWithIndex.toMap

  def Numbers: List[Int] = List.tabulate(length)(NumberAt)

  override def IndexOf(number: Int): Int = {
    if (contiguous) {
      val index = number - first
      if (index >= 0 && index < length) index else -1
    } else {
      indexByNumber.getOrElse(number, -1)
    }
  }

  private def NumberAt(index: Int): Int = if (contiguous) first + index else numbersByIndex(index)

  override def Count: Int = pool.count(value => value == -1)

  def Selector: NumberSelector = selector

  override def Selector_=(slctr: NumberSelector): Unit = {
    selector = slctr
    slctr.Format(pool)
  }

  override def Get(): Try[Int] = {
    val index: Int = Selector.Get(pool)
    if (index == -1) {
      Failure(new Exception("there are no numbers available in the pool"))
    } else {
      Success(NumberAt(index))
    }
  }

  override def Return(number: Int): Boolean = {
    val index = IndexOf(number)
    index > -1 && Selector.Return(index, pool)
  }
}
