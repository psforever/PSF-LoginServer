// Copyright (c) 2026 PSForever
package net.psforever.packet.game.packets

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched from the server to cause a bot avatar to perform a random emote
  * @param bot_guid the target bot's global unique identifier
  */
final case class TriggerBotAction(bot_guid: PlanetSideGUID, unk1: Long = 0, unk2: Long = 0, unk3: Long = 4294967295L)
  extends PlanetSideGamePacket {
  type Packet = TriggerBotAction
  def opcode = GamePacketOpcode.TriggerBotAction
  def encode = TriggerBotAction.encode(this)
}

object TriggerBotAction extends Marshallable[TriggerBotAction] {
  implicit val codec : Codec[TriggerBotAction] = (
    ("bot_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint32L) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint32L)
    ).as[TriggerBotAction]
}
