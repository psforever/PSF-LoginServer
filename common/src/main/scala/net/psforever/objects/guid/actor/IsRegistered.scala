// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.actor

import net.psforever.objects.entity.IdentifiableEntity

/**
  * A message for requesting information about the registration status of an object or a number.
  * @param obj the optional object
  * @param number the optional number
  */
final case class IsRegistered(obj : Option[IdentifiableEntity], number : Option[Int])

object IsRegistered {
  /**
    * Overloaded constructor for querying an object's status.
    * @param obj the object
    * @return an `IsRegistered` object
    */
  def apply(obj : IdentifiableEntity) : IsRegistered = {
    new IsRegistered(Some(obj), None)
  }

  /**
    * Overloaded constructor for querying a number's status.
    * @param number the number
    * @return an `IsRegistered` object
    */
  def apply(number : Int) : IsRegistered = {
    new IsRegistered(None, Some(number))
  }
}
