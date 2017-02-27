// Copyright (c) 2016 PSForever.net to present
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs._

object TransactionType extends Enumeration {
  type Type = Value
  val Unk0,
      Unk1,
      Buy,
      Sell,
      Unk4,
      Unk5,
      Infantry_Loadout,
      Unk7
      = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(3))
}
