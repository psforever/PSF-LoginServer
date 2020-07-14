// Copyright (c) 2020 PSForever
package net.psforever.objects.inventory

/**
  * Some data in the grid portion of a `GridInventory`
  * does not match against data that is expected to be found in the "list" portion of `GridInventory`.
  * While merely eliminating the old data is possible,
  * the discovery of this errant data could be hiding significantly greater issues,
  * and these greater issues must be explored at a higher level of governance.
  * @param message the explanation of why the exception was thrown
  * @param cause any prior `Exception` that was thrown then wrapped in this one
  */
final case class InventoryDisarrayException(private val message: String = "", private val cause: Throwable)
    extends Exception(message, cause)

object InventoryDisarrayException {

  /**
    * Overloaded constructor that constructs the `Exception` without nesting any prior `Exceptions`.
    * Just the custom error message is included.
    * @param message the explanation of why the exception was thrown
    * @return an `InventoryDisarrayException` object
    */
  def apply(message: String): InventoryDisarrayException =
    InventoryDisarrayException(message, None.orNull)
}
