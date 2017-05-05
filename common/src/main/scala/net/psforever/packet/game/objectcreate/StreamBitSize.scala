// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

/**
  * Apply this `trait` to a class that needs to have its size in bits calculated.
  */
trait StreamBitSize {
  /**
    * Performs a "sizeof()" analysis of the given object.<br>
    * <br>
    * The calculation reflects the `scodec Codec` definition rather than the explicit parameter fields.
    * For example, a traditional `Int` is normally a 32-bit number, often rendered as a `32u` number.
    * When parsed with a `uintL(7)`, it's length will be considered 7 bits (`7u`).
    * (Note: being permanently signed, an `scodec` value of `32u` or longer must fit into a `Long` type.)
    * @return the number of bits necessary to measure an object of this class;
    *         defaults to `0L`
    */
  def bitsize : Long = 0L
}

object StreamBitSize {
  /**
    * Calculate the bit size of a Pascal string.
    * @param str a length-prefixed string
    * @param width the width of the character encoding;
    *              defaults to 8 bits
    * @return the size in bits
    */
  def stringBitSize(str : String, width : Int = 8) : Long = {
    val strlen = str.length
    val lenSize = if(strlen > 127) 16L else 8L
    lenSize  + (strlen * width)
  }
}
