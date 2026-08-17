// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.MeritCommendation
import scodec.Codec
import scodec.codecs._

/**
  * Enumerate the player-displayed merit commendation awards granted for excellence (or tenacity) in combat.
  * These are the medals players wish to brandish on their left pauldron.
  * @param upper the "top" configurable merit ribbon
  * @param middle the central configurable merit ribbon
  * @param lower the lower configurable merit ribbon
  * @param tos the top-most term of service merit ribbon
  * @see `MeritCommendation`
  * @see `DisplayedAwardMessage`
  */
final case class RibbonBars(
    upper: MeritCommendation.Value = MeritCommendation.None,
    middle: MeritCommendation.Value = MeritCommendation.None,
    lower: MeritCommendation.Value = MeritCommendation.None,
    tos: MeritCommendation.Value = MeritCommendation.None
) extends StreamBitSize {
  override def bitsize: Long = 128L
}

object RibbonBars extends Marshallable[RibbonBars] {
  implicit val codec: Codec[RibbonBars] = (
    ("upper" | MeritCommendation.codec) ::
      ("middle" | MeritCommendation.codec) ::
      ("lower" | MeritCommendation.codec) ::
      ("tos" | MeritCommendation.codec)
  ).as[RibbonBars]
}
