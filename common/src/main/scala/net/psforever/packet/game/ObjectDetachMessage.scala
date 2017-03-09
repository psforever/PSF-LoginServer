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
  * Not all objects that have been attached ever need to be detached again, however.
  * When detached, the resulting freed object will be placed near the given coordinates.
  * For some container objects, most often static ones, a default placement point does exist.
  * This usually matches the position where the original mounting occurred, or is relative to the current position of the container.<br>
  * <br>
  * This packet is considered formal response to a `DismountVehicleMsg` packet.
  * @param parent_guid the container/connector object
  * @param child_guid the contained/connected object
  * @param pos (near) where the contained/connected object will be placed after it has detached
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  */
final case class ObjectDetachMessage(parent_guid : PlanetSideGUID,
                                     child_guid : PlanetSideGUID,
                                     pos : Vector3,
                                     unk1 : Int,
                                     unk2 : Int,
                                     unk3 : Int)
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
      ("unk1" | uint8L) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint8L)
    ).as[ObjectDetachMessage]
}
