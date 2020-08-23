// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.source

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.key.{LoanedKey, SecureKey}

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
    * The count of numbers allocated to this source.
    * @return the count
    */
  def Size: Int

  /**
    * The count of numbers that can still be drawn.
    * @return the count
    */
  def CountAvailable: Int

  /**
    * The count of numbers that can not be drawn.
    * @return the count
    */
  def CountUsed: Int

  /**
    * Is this number a member of this number source?
    * @param number the number
    * @return `true`, if it is a member; `false`, otherwise
    */
  def Test(number: Int): Boolean = -1 < number && number < Size

  /**
    * Produce an un-modifiable wrapper for the `Monitor` for this number.
    * @param number the number
    * @return the wrapped `Monitor`
    */
  def Get(number: Int): Option[SecureKey]

  //def GetAll(list : List[Int]) : List[SecureKey]

  //def GetAll(p : Key => Boolean) : List[SecureKey]

  /**
    * Produce a modifiable wrapper for the `Monitor` for this number, only if the number has not been used.
    * The `Monitor` should be updated before being wrapped, if necessary.
    * @param number the number
    * @return the wrapped `Monitor`, or `None`
    */
  def Available(number: Int): Option[LoanedKey]

  /**
    * Consume a wrapped `Monitor` and release its number from its previous assignment/use.
    * @param monitor the `Monitor`
    * @return any object previously using this `Monitor`
    */
  def Return(monitor: SecureKey): Option[IdentifiableEntity] = {
    Return(monitor.GUID)
  }

  /**
    * Consume a wrapped `Monitor` and release its number from its previous assignment/use.
    * @param monitor the `Monitor`
    * @return any object previously using this `Monitor`
    */
  def Return(monitor: LoanedKey): Option[IdentifiableEntity] = {
    Return(monitor.GUID)
  }

  /**
    * Consume the number of a `Monitor` and release that number from its previous assignment/use.
    * @param number the number
    * @return any object previously using this number
    */
  def Return(number: Int): Option[IdentifiableEntity]

  /**
    * Produce a modifiable wrapper for the `Monitor` for this number, only if the number has not been used.
    * This wrapped `Monitor` can only be assigned once and the number may not be `Return`ed to this source.
    * @param number the number
    * @return the wrapped `Monitor`
    */
  def Restrict(number: Int): Option[LoanedKey]

  /**
    * Numbers from this source may not longer be marked as `Restricted`.
    * @return the `List` of all numbers that have been restricted
    */
  def FinalizeRestrictions: List[Int]

  import net.psforever.objects.entity.IdentifiableEntity

  /**
    * Reset all number `Monitor`s so that their underlying number is not longer treated as assigned.
    * Perform some level of housecleaning to ensure that all dependencies are resolved in some manner.
    * This is the only way to free `Monitors` that are marked as `Restricted`.
    * @return a `List` of assignments maintained by all the currently-used number `Monitors`
    */
  def Clear(): List[IdentifiableEntity]
}
