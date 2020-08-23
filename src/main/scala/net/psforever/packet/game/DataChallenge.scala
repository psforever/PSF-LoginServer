// Copyright (c) 2020 PSForever
package net.psforever.packet.game

import net.psforever.packet.PacketHelpers
import scodec.codecs.ulongL

object DataChallenge {
  val codec = PacketHelpers.encodedString :: ulongL(bits = 32)
}
