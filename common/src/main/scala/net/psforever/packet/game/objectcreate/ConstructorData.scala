// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

/**
  * The base type for the representation of any data used to produce objects from `ObjectCreateMessage` packet data.
  * There is no reason to instantiate this class as-is.
  * Children of this class are expected to be able to translate through `scodec` operations into packet data.<br>
  * <br>
  * The object data is uncoupled from the object class as multiple classes use the same format for their data.
  * For example, both the Suppressor and the Gauss will use a "weapon data" format.
  * For example, both 9mm bullets and energy cells will use an "ammunition data" format.
  */
abstract class ConstructorData extends StreamBitSize

object ConstructorData {
  /**
    * This pattern is intended to provide common conversion between all of the `Codec`s of the children of this class.
    * The casting will be performed through use of `exmap` in the child class.
    */
  type genericPattern = Option[ConstructorData]
}
