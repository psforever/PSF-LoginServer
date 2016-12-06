// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.Codec
import scodec.codecs._

case class RibbonBars(upper : Long = 0xFFFFFFFFL, //0xFFFFFFFF means no merit (for all ...)
                      middle : Long = 0xFFFFFFFFL,
                      lower : Long = 0xFFFFFFFFL,
                      tos : Long = 0xFFFFFFFFL) {
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
