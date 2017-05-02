// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.Codec
import scodec.codecs._

/**
  * Enumerate the player-displayed merit commendation awards granted for excellence (or tenacity) in combat.
  * These are the medals players wish to brandish on their left pauldron.<br>
  * <br>
  * All merit commendation ribbons are represented by a 32-bit signature.
  * The default "no-ribbon" value is `0xFFFFFFFF`, although some illegal values will also work.
  * The term of service ribbon can not be modified by the user and will apply itself to its slot automatically when valid.
  * @param upper the "top" configurable merit ribbon
  * @param middle the central configurable merit ribbon
  * @param lower the lower configurable merit ribbon
  * @param tos the top-most term of service merit ribbon
  */
final case class RibbonBars(upper : Long = RibbonBars.noRibbon,
                            middle : Long = RibbonBars.noRibbon,
                            lower : Long = RibbonBars.noRibbon,
                            tos : Long = RibbonBars.noRibbon) extends StreamBitSize {
  override def bitsize : Long = 128L
}

object RibbonBars extends Marshallable[RibbonBars] {
  val noRibbon : Long = 0xFFFFFFFFL

  implicit val codec : Codec[RibbonBars] = (
    ("upper" | uint32L) ::
      ("middle" | uint32L) ::
      ("lower" | uint32L) ::
      ("tos" | uint32L)
    ).as[RibbonBars]
}
