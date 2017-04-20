// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param object_guid the object being manipulated (controlled)
  * @param pitch the angle with respect to the sky and the ground towards which the object is directed;
  *              an 8-bit unsigned value;
  *              0 is perfectly level and forward-facing, wrapping around at 255;
  *              positive rotation is occurs by rotating downwards from forward-facing
  * @param yaw the angle with respect to the horizon towards which the object is directed;
  *              an 8-bit unsigned value;
  *              0 is forward-facing, wrapping around at 255;
  *              positive rotation is clockwise of forward-facing
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
