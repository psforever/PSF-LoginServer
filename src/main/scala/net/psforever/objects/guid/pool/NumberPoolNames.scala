// Copyright (c) 2026 PSForever
package net.psforever.objects.guid.pool

import scala.collection.mutable

/**
  * Which pool, by name, holds each number from 0 up to but excluding `size`.
  * @param size how many numbers, counting from 0
  */
class NumberPoolNames(size: Int) {
  private val ids: Array[Byte] = new Array[Byte](size)
  /* the name with id `i` is `names(i - 1)`; a name keeps its id once given, even after its numbers are removed,
     and ids are read unsigned, so up to 255 names can be told apart */
  private val names: mutable.ArrayBuffer[String] = mutable.ArrayBuffer[String]()

  private def inRange(number: Long): Boolean = number >= 0 && number < size

  def contains(number: Long): Boolean = inRange(number) && ids(number.toInt) != 0

  def get(number: Long): Option[String] = {
    if (contains(number)) Some(names((ids(number.toInt) & 0xff) - 1)) else None
  }

  def apply(number: Long): String = {
    get(number).getOrElse(throw new NoSuchElementException(s"key not found: $number"))
  }

  def +=(entry: (Long, String)): this.type = {
    val (number, name) = entry
    if (inRange(number)) {
      ids(number.toInt) = idOf(name)
    }
    this
  }

  def ++=(entries: IterableOnce[(Long, String)]): this.type = {
    entries.iterator.foreach(entry => this += entry)
    this
  }

  def -=(number: Long): this.type = {
    if (inRange(number)) {
      ids(number.toInt) = 0
    }
    this
  }

  /** every number held by a pool, ascending */
  def keys: Iterable[Long] = (0 until size).view.filter(n => ids(n) != 0).map(_.toLong)

  private def idOf(name: String): Byte = {
    val index = names.indexOf(name)
    if (index > -1) {
      (index + 1).toByte
    } else if (names.size < 255) {
      names += name
      names.size.toByte
    } else {
      throw new IllegalStateException(s"can not record pool $name - no more than 255 pool names are supported")
    }
  }
}
