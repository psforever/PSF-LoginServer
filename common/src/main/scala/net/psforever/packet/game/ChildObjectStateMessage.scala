// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched from a client when its user is controlling a secondary object whose state must be updated.<br>
  * <br>
  * When `ChildObjectStateMessage` is being sent to the server, it replaces `PlayerStateMessage`.
  * The packet frequently gets hidden in a `MultiPacket`, though it is not functionally essential to do that.<br>
  * <br>
  * Note the lack of position data.
  * The secondary object in question is updated in position through another means or is stationary.
  * The only concern is the direction the object is facing.
  * The angles are relative to the object's normal forward-facing and typically begin tracking at 0, 0 (forward-facing).
  * @param object_guid the object being manipulated (controlled)
  * @param pitch the amount of pitch that affects orientation from forward facing (0)
  * @param yaw the amount of yaw that affects orientation from forward-facing (0)
  */
final case class ChildObjectStateMessage(object_guid: PlanetSideGUID, pitch: Float, yaw: Float)
    extends PlanetSideGamePacket {
  type Packet = ChildObjectStateMessage
  def opcode = GamePacketOpcode.ChildObjectStateMessage
  def encode = ChildObjectStateMessage.encode(this)
}

object ChildObjectStateMessage extends Marshallable[ChildObjectStateMessage] {
  implicit val codec: Codec[ChildObjectStateMessage] = (
    ("object_guid" | PlanetSideGUID.codec) ::
      ("pitch" | Angular.codec_pitch) ::
      ("yaw" | Angular.codec_yaw(0f))
  ).as[ChildObjectStateMessage]
}
