// Copyright (c) 2020 PSForever
package net.psforever.objects.guid.source

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.AvailabilityPolicy
import net.psforever.objects.guid.key.{LoanedKey, SecureKey}

/**
  * A `NumberSource` is considered a master "pool" of numbers from which all numbers are available to be drawn.
  * Produce a series of numbers from 0 to a maximum number (inclusive) to be used as globally unique identifiers (GUID's).
  * @param values the domain of numbers to be used by this source;
  *               must only be positive integers or zero
  * @throws IllegalArgumentException if no numbers are provided
  * @throws IllegalArgumentException if any of the numbers provided are negative
  */
class SpecificNumberSource(values: Iterable[Int]) extends NumberSource {
  if (values.isEmpty) {
    throw new IllegalArgumentException(s"must provide one or more positive integers (or zero)")
  }
  values.filter(_ < 0) match {
    case Nil => ;
    case list => throw new IllegalArgumentException(s"non-negative integers only, not ${list.mkString(" ")}")
  }
  private val ary : Map[Int, Key] = values.map(index => (index, new Key)).toMap

  def max : Int = ary.keys.max

  def size : Int = ary.size

  def countAvailable : Int = ary.values.count(key => key.policy == AvailabilityPolicy.Available)

  def countUsed : Int = ary.values.count(_.policy != AvailabilityPolicy.Available)

  def test(number : Int) : Boolean = ary.get(number).nonEmpty

  def get(number : Int) : Option[SecureKey] = {
    ary.get(number) match {
      case Some(key) => Some(new SecureKey(number, key))
      case _ => None
    }
  }

  def get(obj : IdentifiableEntity) : Option[SecureKey] = {
    ary.find { case (_, key) =>
      key.obj match {
        case Some(o) => o eq obj
        case _ => false
      }
    } match {
      case Some((number, key)) => Some(new SecureKey(number, key))
      case _ => None
    }
  }

  def getAvailable(number : Int) : Option[LoanedKey] = {
    ary.get(number) match {
      case Some(key) if key.policy == AvailabilityPolicy.Available =>
        key.policy = AvailabilityPolicy.Leased
        Some(new LoanedKey(number, key))
      case _ =>
        None
    }
  }

  def returnNumber(number : Int) : Option[IdentifiableEntity] = {
    ary.get(number) match {
      case Some(key) if key.policy == AvailabilityPolicy.Leased =>
        val out = key.obj
        key.policy = AvailabilityPolicy.Available
        key.obj = None
        out
      case _ =>
        None
    }
  }

  def restrictNumber(number : Int) : Option[LoanedKey] = {
    ary.get(number) match {
      case Some(key) if key.policy != AvailabilityPolicy.Restricted =>
        key.policy = AvailabilityPolicy.Restricted
        Some(new LoanedKey(number, key))
      case _ =>
        None
    }
  }

  def finalizeRestrictions : List[Int] = {
    ary
      .filter { case (_, key : Key) => key.policy == AvailabilityPolicy.Restricted }
      .keys
      .toList
  }

  def clear(): List[IdentifiableEntity] = {
    val leased = ary.values.filter(_.policy != AvailabilityPolicy.Available)
    leased collect { case key if key.obj.isEmpty =>
      key.policy = AvailabilityPolicy.Available
    }
    leased.toList collect { case key if key.obj.nonEmpty =>
      key.policy = AvailabilityPolicy.Available
      val out = key.obj.get
      key.obj = None
      out
    }
  }
}

object SpecificNumberSource {
  def apply(values: Iterable[Int]): SpecificNumberSource = {
    new SpecificNumberSource(values)
  }
}
