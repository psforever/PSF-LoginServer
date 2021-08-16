// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.source

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.key.{AvailabilityPolicy, LoanedKey, SecureKey}

/**
  * A `NumberSource` is considered a master "pool" of numbers from which all numbers are available to be drawn.
  * The numbers are considered to be exclusive.<br>
  * <br>
  * The following are guidelines for implementing classes.
  * The numbers allocated to this source are from zero up through positive integers.
  * When a number is drawn from the pool, it is flagged internally and can not be selected for drawing again until the flag is removed.
  * Some flagging states are allowed to restrict that number for the whole lifespan of the source.
  * This internal flagging is maintained by a "monitor" that should not directly get exposed.
  * Use the provided indirect referencing containers - `SecureKey` and `LoanedKey`.<br>
  * <br>
  * The purpose of a `NumberSource` is to help facilitate globally unique identifiers (GUID, pl. GUIDs).
  */
trait NumberSource {
  /**
    * The maximum number that can be produced by this source.
    * @return the max
    */
  def max: Int

  /**
    * The count of numbers allocated to this source.
    * @return the count
    */
  def size: Int

  /**
    * Select the type of count desired based on the allocation policy of the key.
    * @param policy the allocation policy
    * @return the number of keys belonging to this policy
    */
  def count(policy: AvailabilityPolicy): Int = {
    policy match {
      case AvailabilityPolicy.Available => countAvailable
      case AvailabilityPolicy.Leased => countUsed
      case AvailabilityPolicy.Dangling => countDangling
    }
  }


  /**
    * The count of numbers that can still be drawn.
    * @return the count
    */
  def countAvailable: Int

  /**
    * The count of numbers that can not be drawn.
    * @return the count
    */
  def countUsed: Int

  /**
    * The count of numbers that can not be drawn but have not yet been assigned to an entity.
    * Could only ever be a non-zero count if the number of used keys is a non-zero count.
    * @return the count
    */
  def countDangling: Int

  /**
    * Is this number a member of this number source?
    * @param number the number
    * @return `true`, if it is a member; `false`, otherwise
    */
  def test(number: Int): Boolean

  /**
    * Produce an un-modifiable wrapper for the `Monitor` for this number.
    * @param number the number
    * @return the wrapped `Monitor`
    */
  def get(number: Int): Option[SecureKey]

  /**
    * Produce an un-modifiable wrapper for the `Monitor` for this entity,
    * if the entity is discovered being represented in this source.
    * @param obj the entity
    * @return the wrapped `Monitor`
    */
  def get(obj: IdentifiableEntity) : Option[SecureKey]

  /**
    * Produce a modifiable wrapper for the `Monitor` for this number, only if the number has not been used.
    * The `Monitor` should be updated before being wrapped, if necessary.
    * @param number the number
    * @return the wrapped `Monitor`, or `None`
    */
  def getAvailable(number: Int): Option[LoanedKey]

  /**
    * Consume a wrapped `Monitor` and release its number from its previous assignment/use.
    * @param monitor the `Monitor`
    * @return any object previously using this `Monitor`
    */
  def returnNumber(monitor: SecureKey): Option[IdentifiableEntity] = {
    returnNumber(monitor.GUID)
  }

  /**
    * Consume a wrapped `Monitor` and release its number from its previous assignment/use.
    * @param monitor the `Monitor`
    * @return any object previously using this `Monitor`
    */
  def returnNumber(monitor: LoanedKey): Option[IdentifiableEntity] = {
    returnNumber(monitor.GUID)
  }

  /**
    * Consume the number of a `Monitor` and release that number from its previous assignment/use.
    * @param number the number
    * @return any object previously using this number
    */
  def returnNumber(number: Int): Option[IdentifiableEntity]

  /**
    * Reset all number `Monitor`s so that their underlying number is not longer treated as assigned.
    * Perform some level of housecleaning to ensure that all dependencies are resolved in some manner.
    * @return a `List` of assignments maintained by all the currently-used number `Monitors`
    */
  def clear(): List[IdentifiableEntity]
}
