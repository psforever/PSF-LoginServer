// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * The client is telling the server that the player wants to go to the training zones.<br>
  * <br>
  * This message is dispatched when a player enters the VR hallway / VR teleport and accepts the "Shooting Range" or the "Vehicle Training Area" options on the prompt.
  * The packet sends indication to the server (as if it didn't know?) in regards to which training grounds the player should be sent.
  * Players are sent to their respective empire's area by default.<br>
  * @param unk1 na;
  *             19 (`13`) when shooting range;
  *             22 (`16`) when ground vehicle range
  * @param unk2 na; always zero?
  * @param unk3 na; always zero?
  * @param unk4 na; always zero?
  */
final case class TrainingZoneMessage(unk1 : Int,
                                     unk2 : Int,
                                     unk3 : Int,
                                     unk4 : Int)
  extends PlanetSideGamePacket {
  type Packet = TrainingZoneMessage
  def opcode = GamePacketOpcode.TrainingZoneMessage
  def encode = TrainingZoneMessage.encode(this)
}

object TrainingZoneMessage extends Marshallable[TrainingZoneMessage] {
  implicit val codec : Codec[TrainingZoneMessage] = (
      ("unk1" | uint8L) ::
        ("unk2" | uint8L) ::
        ("unk3" | uint8L) ::
        ("unk4" | uint8L)
    ).as[TrainingZoneMessage]
}
