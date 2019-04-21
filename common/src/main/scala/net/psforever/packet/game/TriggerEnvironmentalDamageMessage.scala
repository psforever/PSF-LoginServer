// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the server to render a "damage cloud" around a target.<br>
  * <br>
  * Exploration:<br>
  * This is not common but it happened while on Gemini Live.
  * Why does it happen?
  * @param unk1 na;
  *             usually 2;
  *             when 2, will generate a short dust cloud around the `target_guid`;
  *             if a player, will cause the "damage grunt animation" to occur, whether or not there is a dust cloud
  * @param target_guid the target around which to generate the temporary damage effect
  * @param unk2 na;
  *             usually 5L
  */

  /*
    BETA CLIENT DEBUG INFO:
    Message type:   %d (%s)\n        length: %d\n
        Environment Type: %u (%s)\n
        Guid: %d\n
        Damage Amount: %u\n
   */
final case class TriggerEnvironmentalDamageMessage(unk1 : Int,
                                                   target_guid : PlanetSideGUID,
                                                   unk2 : Long)
  extends PlanetSideGamePacket {
  type Packet = TriggerEnvironmentalDamageMessage
  def opcode = GamePacketOpcode.TriggerEnvironmentalDamageMessage
  def encode = TriggerEnvironmentalDamageMessage.encode(this)
}

object TriggerEnvironmentalDamageMessage extends Marshallable[TriggerEnvironmentalDamageMessage] {
  implicit val codec : Codec[TriggerEnvironmentalDamageMessage] = (
    ("unk1" | uint2L) ::
      ("target_guid" | PlanetSideGUID.codec) ::
      ("unk2" | uint32L)
    ).as[TriggerEnvironmentalDamageMessage]
}
