// Copyright (c) 2020 PSForever
package net.psforever.objects.guid.source

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.AvailabilityPolicy
import net.psforever.objects.guid.key.{LoanedKey, SecureKey}

class SpecificNumberSource(values: Iterable[Int]) extends NumberSource {
  values.filter(_ < 0) match {
    case Nil => ;
    case list => throw new IllegalArgumentException(s"non-negative integers only, not ${list.mkString(" ")}")
  }
  private val ary: Map[Int, Key] = values.map(index => (index, new Key)).toMap

  def max: Int = ary.keys.max

  def size: Int = ary.size

  def countAvailable: Int = ary.values.count(key => key.policy == AvailabilityPolicy.Available)

  def countUsed: Int = ary.values.count(key => key.policy != AvailabilityPolicy.Available)

  def test(number: Int): Boolean = ary.get(number).nonEmpty

  def get(number: Int): Option[SecureKey] = {
    ary.get(number) match {
      case Some(key) => Some(new SecureKey(number, key))
      case _=> None
    }
  }

  def getAvailable(number: Int): Option[LoanedKey] = {
    ary.get(number) match {
      case Some(key) if key.policy == AvailabilityPolicy.Available =>
        key.policy = AvailabilityPolicy.Leased
        Some(new LoanedKey(number, key))
      case _=>
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
      case _=>
        None
    }
  }

  def restrictNumber(number: Int): Option[LoanedKey] = None

  def finalizeRestrictions: List[Int] = Nil

  def clear(): List[IdentifiableEntity] = {
    ary.values.collect {
      case key if key.policy == AvailabilityPolicy.Leased =>
        val out = key.obj
        key.policy = AvailabilityPolicy.Available
        key.obj = None
        out.get
    }.toList
  }
}

object SpecificNumberSource {
  def apply(values: Iterable[Int]): SpecificNumberSource = {
    new SpecificNumberSource(values)
  }
}
