// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the server to cause two associated objects to disentangle from one another.<br>
  * <br>
  * `ObjectDetachMessage` is the opposite to `ObjectAttachMessage`.
  * When detached, the resulting freed object will be placed at the given coordinates.
  * For some container objects, most often static ones, a default placement point does exist.
  * This usually matches the position where the original mounting occurred, or is relative to the current position of the container.
  * Using a position that is not the mounting one, in this case, counts as a temporary teleport of the character.
  * As soon as available, e.g., the end of an animation, the character will rw-appear at the mounting point.
  * The object may also have its orientation aspect changed.<br>
  * <br>
  * This packet is considered proper response to:<br>
  * - `DismountVehicleMsg`<br>
  * - `DropItemMessage`
  * @param parent_guid the container/connector object
  * @param child_guid the contained/connected object
  * @param pos where the contained/connected object will be placed after it has detached
  * @param roll the roll of the dropped item;
  *             every `0x1` is 2.813 degrees;
  *             every `0x10` is 45-degrees;
  *             it wraps at `0x0` == `0x80` == top facing up
  * @param pitch the pitch of the dropped item;
  *              every `0x1` is 2.813 degrees;
  *              every `0x10` is 45-degrees;
  *             it wraps at `0x0` == `0x80` == top facing up
  * @param yaw the yaw of the dropped item;
  *            every `0x1` is 2.813 degrees counter clockwise from East;
  *            every `0x10` is 45-degrees;
  *            it wraps at `0x0` == `0x80` == front facing East
  */
final case class ObjectDetachMessage(parent_guid : PlanetSideGUID,
                                     child_guid : PlanetSideGUID,
                                     pos : Vector3,
                                     roll : Int,
                                     pitch : Int,
                                     yaw : Int)
  extends PlanetSideGamePacket {
  type Packet = ObjectDetachMessage
  def opcode = GamePacketOpcode.ObjectDetachMessage
  def encode = ObjectDetachMessage.encode(this)
}

object ObjectDetachMessage extends Marshallable[ObjectDetachMessage] {
  implicit val codec : Codec[ObjectDetachMessage] = (
    ("parent_guid" | PlanetSideGUID.codec) ::
      ("child_guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("roll" | uint8L) ::
      ("pitch" | uint8L) ::
      ("yaw" | uint8L)
    ).as[ObjectDetachMessage]
}
