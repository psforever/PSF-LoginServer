// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.Codec
import scodec.codecs._

/**
  * Enumerate the player-displayed merit commendation awards granted for excellence (or tenacity) in combat.
  * These are the medals players wish to brandish on their left pauldron.<br>
  * <br>
  * All merit commendation ribbons are represented by a 32-bit signature.
  * The default "no-ribbon" value is `0xFFFFFFFF`.
  * @param upper the "top" configurable merit ribbon
  * @param middle the central configurable merit ribbon
  * @param lower the lower configurable merit ribbon
  * @param tos the automatic top-most term of service merit ribbon
  */
case class RibbonBars(upper : Long = 0xFFFFFFFFL,
                      middle : Long = 0xFFFFFFFFL,
                      lower : Long = 0xFFFFFFFFL,
                      tos : Long = 0xFFFFFFFFL) {
  /**
    * Performs a "sizeof()" analysis of the given object.
    * @see ConstructorData.bitsize
    * @return the number of bits necessary to represent this object
    */
  def bitsize : Long = 128L
}

object RibbonBars extends Marshallable[RibbonBars] {
  implicit val codec : Codec[RibbonBars] = (
    ("upper" | uint32L) ::
      ("middle" | uint32L) ::
      ("lower" | uint32L) ::
      ("tos" | uint32L)
    ).as[RibbonBars]
}
