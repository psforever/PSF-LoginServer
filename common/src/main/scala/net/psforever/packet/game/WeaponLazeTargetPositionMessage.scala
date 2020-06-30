// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched to the server when a position is being marked by a laze pointer tool.<br>
  * <br>
  * When the laze tool is used, a progress bar window is displayed, along with the text "Acquiring Target Position."
  * The player using the tool constantly sends packets to the server for as long as the progress bar is filling.
  * In all, about fifty packets are sent.
  * (Measured during low-load testing.
  * The actual number of packets may be related to network load.)<br>
  * <br>
  * While firing, the player's movement is locked for the duration.
  * The weapon fire can be aborted at any time, returning control.
  * @param weapon_uid the laze pointer tool
  * @param player_pos the position of (the player holding the) laze pointer
  * @param lazed_pos position of the tip of the laze pointer's beam, or where it intersects something
  */
final case class WeaponLazeTargetPositionMessage(weapon_uid: PlanetSideGUID, player_pos: Vector3, lazed_pos: Vector3)
    extends PlanetSideGamePacket {
  type Packet = WeaponLazeTargetPositionMessage
  def opcode = GamePacketOpcode.WeaponLazeTargetPositionMessage
  def encode = WeaponLazeTargetPositionMessage.encode(this)
}

object WeaponLazeTargetPositionMessage extends Marshallable[WeaponLazeTargetPositionMessage] {
  implicit val codec: Codec[WeaponLazeTargetPositionMessage] = (
    ("weapon_uid" | PlanetSideGUID.codec) ::
      ("player_pos" | Vector3.codec_pos) ::
      ("lazed_pos" | Vector3.codec_pos)
  ).as[WeaponLazeTargetPositionMessage]
}
