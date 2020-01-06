// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the server to cause two associated objects to disentangle from one another.<br>
  * <br>
  * `ObjectDetachMessage` is the opposite of `ObjectAttachMessage`.
  * When detached, the resulting freed object will be placed at the given coordinates in the game world.
  * For detachment from some container objects, a default placement point may exist.
  * This usually matches the position where the original mounting occurred, or is relative to the current position of the container.
  * This mounting position overrides the input one, but other temporary side-effects may occur.
  * For example, if a player detaches from a vehicle with coordinates for "somewhere else,"
  * the camera will temporarily be moved to that location "somewhere else" for the duration of the animation
  * but it will soon regain the player who appeared where expected.<br>
  * <br>
  * An object that is already dropped is a special case where the parent (container) does not technically exist.
  * The parent also does not need to exist as the object will still be transported to the specified coordinates.
  * @param parent_guid the container/connector object
  * @param child_guid the contained/connected object
  * @param pos where the contained/connected object will be placed after it has detached
  * @param roll the amount of roll that affects orientation of the dropped item
  * @param pitch the amount of pitch that affects orientation of the dropped item
  * @param yaw the amount of yaw that affects orientation of the dropped item
  */
final case class ObjectDetachMessage(parent_guid : PlanetSideGUID,
                                     child_guid : PlanetSideGUID,
                                     pos : Vector3,
                                     roll : Float,
                                     pitch : Float,
                                     yaw : Float)
  extends PlanetSideGamePacket {
  type Packet = ObjectDetachMessage
  def opcode = GamePacketOpcode.ObjectDetachMessage
  def encode = ObjectDetachMessage.encode(this)
}

object ObjectDetachMessage extends Marshallable[ObjectDetachMessage] {
  def apply(parent_guid : PlanetSideGUID, child_guid : PlanetSideGUID, pos : Vector3, orient : Vector3) : ObjectDetachMessage = {
    ObjectDetachMessage(parent_guid, child_guid, pos, orient.x, orient.y, orient.z)
  }

  def apply(parent_guid : PlanetSideGUID, child_guid : PlanetSideGUID, pos : Vector3, orient_z : Float) : ObjectDetachMessage = {
    ObjectDetachMessage(parent_guid, child_guid, pos, 0, 0, orient_z)
  }

  implicit val codec : Codec[ObjectDetachMessage] = (
    ("parent_guid" | PlanetSideGUID.codec) ::
      ("child_guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("roll" | Angular.codec_roll) ::
      ("pitch" | Angular.codec_pitch) ::
      ("yaw" | Angular.codec_yaw())
    ).as[ObjectDetachMessage]
}
