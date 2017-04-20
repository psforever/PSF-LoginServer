// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
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
  * @param pitch the angle with respect to the sky and the ground towards which the object is directed;
  *              an 8-bit unsigned value;
  *              0 is perfectly level and forward-facing and mapped to 255;
  *              positive rotation is downwards from forward-facing;
  *              between 7 (down) and 231 (up), for 32 angles
  * @param yaw the angle with respect to the horizon towards which the object is directed;
  *            an 8-bit unsigned value;
  *            0 is forward-facing, wrapping around at 127;
  *            positive rotation is counter-clockwise of forward-facing;
  *            full rotation
  */
final case class ChildObjectStateMessage(object_guid : PlanetSideGUID,
                                         pitch : Int,
                                         yaw : Int)
  extends PlanetSideGamePacket {
  type Packet = ChildObjectStateMessage
  def opcode = GamePacketOpcode.ChildObjectStateMessage
  def encode = ChildObjectStateMessage.encode(this)
}

object ChildObjectStateMessage extends Marshallable[ChildObjectStateMessage] {
  implicit val codec : Codec[ChildObjectStateMessage] = (
    ("object_guid" | PlanetSideGUID.codec) ::
      ("pitch" | uint8L) ::
      ("yaw" | uint8L)
    ).as[ChildObjectStateMessage]
}
