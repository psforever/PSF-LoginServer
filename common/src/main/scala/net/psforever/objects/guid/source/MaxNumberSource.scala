// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.source

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.key.{LoanedKey, SecureKey}
import net.psforever.objects.guid.AvailabilityPolicy

/**
  * A `NumberSource` is considered a master "pool" of numbers from which all numbers are available to be drawn.
  * The numbers are considered to be exclusive.<br>
  * <br>
  * This source utilizes all positive integers (to `Int.MaxValue`, anyway) and zero.
  * It allocates number `Monitors` as it needs them.
  * While this allows for a wide range of possible numbers, the internal structure expands and contracts as needed.
  * The underlying flexible structure is a `LongMap` and is subject to constraints regarding `LongMap` growth.
  */
class MaxNumberSource() extends NumberSource {
  import scala.collection.mutable
  private val hash : mutable.LongMap[Key] = mutable.LongMap[Key]() //TODO consider seeding an initialBufferSize
  private var allowRestrictions : Boolean = true

  def Size : Int = Int.MaxValue

  def CountAvailable : Int = Size - CountUsed

  def CountUsed : Int = hash.size

  override def Test(guid : Int) : Boolean = guid > -1

  def Get(number : Int) : Option[SecureKey] = {
    if(!Test(number)) {
      None
    }
    else {
      val existing : Option[Key] = hash.get(number).orElse({
        val key : Key = new Key
        key.Policy = AvailabilityPolicy.Available
        hash.put(number, key)
        Some(key)
      })
      Some(new SecureKey(number, existing.get))
    }
  }

//  def GetAll(list : List[Int]) : List[SecureKey] = {
//    list.map(number =>
//      hash.get(number) match {
//        case Some(key) =>
//          new SecureKey(number, key)
//        case _ =>
//          new SecureKey(number, new Key { Policy = AvailabilityPolicy.Available })
//      }
//    )
//  }
//
//  def GetAll( p : Key => Boolean ) : List[SecureKey] = {
//    hash.filter(entry => p.apply(entry._2)).map(entry => new SecureKey(entry._1.toInt, entry._2)).toList
//  }

  def Available(number : Int) : Option[LoanedKey] = {
    if(!Test(number)) {
      throw new IndexOutOfBoundsException("number can not be negative")
    }
    hash.get(number) match {
      case Some(_) =>
        None
      case _ =>
        val key : Key = new Key
        key.Policy = AvailabilityPolicy.Leased
        hash.put(number, key)
        Some(new LoanedKey(number, key))
    }
  }

  def Return(number : Int) : Option[IdentifiableEntity] = {
    val existing = hash.get(number)
    if(existing.isDefined && existing.get.Policy == AvailabilityPolicy.Leased) {
      hash -= number
      val obj = existing.get.Object
      existing.get.Object = None
      obj
    }
    else {
      None
    }
  }

  def Restrict(number : Int) : Option[LoanedKey] = {
    if(allowRestrictions) {
      val existing : Key = hash.get(number).orElse({
        val key : Key = new Key
        hash.put(number, key)
        Some(key)
      }).get
      existing.Policy = AvailabilityPolicy.Restricted
      Some(new LoanedKey(number, existing))
    }
    else {
      None
    }
  }

  def FinalizeRestrictions : List[Int] = {
    allowRestrictions = false
    hash.filter(entry => entry._2.Policy == AvailabilityPolicy.Restricted).map(entry => entry._1.toInt).toList
  }

  def Clear() : List[IdentifiableEntity] = {
    val list : List[IdentifiableEntity] = hash.values.filter(key => key.Object.isDefined).map(key => key.Object.get).toList
    hash.clear()
    list
  }
}

object MaxNumberSource {
  def apply() : MaxNumberSource = {
    new MaxNumberSource()
  }
}