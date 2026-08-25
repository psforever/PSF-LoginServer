// Copyright (c) 2026 PSForever
package net.psforever.packet.game.objectcreate

import scodec.Codec
import scodec.codecs._

case class ToolPatternData(
                            u1: Int,
                            u2: Int,
                            u3: Boolean,
                            u4: Boolean,
                            u5: Option[InventoryData],
                            u6: Boolean
                          ) extends StreamBitSize {
  /**
   * The base size is 28L.
   * If inventory is defined under `u5`,
   * add 10L for the inventory data (`InventoryData`) plus the size of the inventory fields.
   * @return the number of bits necessary to measure an object of this class
   */
  override def bitsize: Long = {
    val u5Size: Long = u5.map(_.bitsize).getOrElse(0L)
    ToolPatternData.bitsize + u5Size
  }
}

object ToolPatternData {
  final val bitsize: Long = 28L

  val codec: Codec[ToolPatternData] = (
    ("u1" | uint8) ::
      ("u2" | uint16) ::
      ("u3" | bool) ::
      ("u4" | bool) ::
      ("u5" | optional(bool, InventoryData.codec_detailed)) ::
      ("u6" | bool)
    ).as[ToolPatternData]
}
