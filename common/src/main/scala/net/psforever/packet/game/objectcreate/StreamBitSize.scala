// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

/**
  * Apply this trait to a class that needs to have its size in bits calculated.
  */
trait StreamBitSize {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * The calculation reflects the `scodec Codec` definition rather than the explicit parameter fields.
    * For example, an `Int` is normally a 32u number;
    * when parsed with a `uintL(7)`, it's length will be considered 7u.
    * (Note: being permanently signed, an `scodec` 32u value must fit into a `Long` type.)
    * @return the number of bits necessary to represent this object;
    *         defaults to `0L`
    */
  def bitsize : Long = 0L
}
