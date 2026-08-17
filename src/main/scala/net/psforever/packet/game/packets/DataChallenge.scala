package net.psforever.packet.game.packets

import net.psforever.packet.PacketHelpers
import scodec.codecs.ulongL

object DataChallenge {
  val codec = PacketHelpers.encodedString :: ulongL(bits = 32)
}
