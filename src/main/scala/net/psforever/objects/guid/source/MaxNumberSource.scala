// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.source

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.key.{LoanedKey, SecureKey}
import net.psforever.objects.guid.AvailabilityPolicy

/**
  * A `NumberSource` is considered a master "pool" of numbers from which all numbers are available to be drawn.
  * The numbers are considered to be exclusive.<br>
  * <br>
  * Produce a series of numbers from 0 to a maximum number (inclusive) to be used as globally unique identifiers (GUIDs).
  * @param max the highest number to be generated by this source;
  *            must be a positive integer or zero
  * @throws IllegalArgumentException if `max` is less than zero (therefore the count of generated numbers is at most zero)
  * @throws java.lang.NegativeArraySizeException if the count of numbers generated due to max is negative
  */
class MaxNumberSource(val max: Int) extends NumberSource {
  if (max < 0) {
    throw new IllegalArgumentException(s"non-negative integers only, not $max")
  }
  private val ary: Array[Key] = Array.ofDim[Key](max + 1)
  (0 to max).foreach(x => { ary(x) = new Key })
  private var allowRestrictions: Boolean = true

  def size: Int = ary.length

  def countAvailable: Int = ary.count(key => key.policy == AvailabilityPolicy.Available)

  def countUsed: Int = ary.count(key => key.policy != AvailabilityPolicy.Available)

  def test(number: Int): Boolean = -1 < number && number < size

  def get(number: Int): Option[SecureKey] = {
    if (test(number)) {
      Some(new SecureKey(number, ary(number)))
    } else {
      None
    }
  }

  def get(obj: IdentifiableEntity) : Option[SecureKey] = {
    ary.zipWithIndex.find { case (key, _) =>
      key.obj match {
        case Some(o) => o eq obj
        case _ => false
      }
    } match {
      case Some((key, number)) => Some(new SecureKey(number, key))
      case _=> None
    }
  }

  def getAvailable(number: Int): Option[LoanedKey] = {
    var out: Option[LoanedKey] = None
    if (test(number)) {
      val key: Key = ary(number)
      if (key.policy == AvailabilityPolicy.Available) {
        key.policy = AvailabilityPolicy.Leased
        out = Some(new LoanedKey(number, key))
      }
    }
    out
  }

  /**
    * Consume the number of a `Monitor` and release that number from its previous assignment/use.
    * @param number the number
    * @return any object previously using this number
    */
  def returnNumber(number: Int): Option[IdentifiableEntity] = {
    var out: Option[IdentifiableEntity] = None
    if (test(number)) {
      val existing: Key = ary(number)
      if (existing.policy == AvailabilityPolicy.Leased) {
        out = existing.obj
        existing.policy = AvailabilityPolicy.Available
        existing.obj = None
      }
    }
    out
  }

  /**
    * Produce a modifiable wrapper for the `Monitor` for this number, only if the number has not been used.
    * This wrapped `Monitor` can only be assigned once and the number may not be `returnNumber`ed to this source.
    * @param number the number
    * @return the wrapped `Monitor`
    * @throws ArrayIndexOutOfBoundsException if the requested number is above or below the range
    */
  def restrictNumber(number: Int): Option[LoanedKey] = {
    if (allowRestrictions && test(number)) {
      val key: Key = ary(number)
      key.policy = AvailabilityPolicy.Restricted
      Some(new LoanedKey(number, key))
    } else {
      None
    }
  }

  def finalizeRestrictions: List[Int] = {
    allowRestrictions = false
    ary.zipWithIndex.filter(entry => entry._1.policy == AvailabilityPolicy.Restricted).map(entry => entry._2).toList
  }

  def clear(): List[IdentifiableEntity] = {
    ary.toList collect {
      case key if key.policy == AvailabilityPolicy.Leased =>
        key.policy = AvailabilityPolicy.Available
        val out = key.obj.get
        key.obj = None
        out
    }
  }
}

object MaxNumberSource {
  def apply(max: Int): MaxNumberSource = {
    new MaxNumberSource(max)
  }
}
