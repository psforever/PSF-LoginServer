// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched from the client to request that an object be deployed.<br>
  * <br>
  * Information in the packet mainly reports about the surface on which the object will be coplanar when/if placed.
  * The server responds with a `ObjectDeployedMessage` packet with the results.
  * If successful, that is followed by an `ObjectCreateMessage` packet and a `DeployableObjectsInfoMessage` packet.
  * @param object_guid the object
  * @param unk1 na
  * @param pos the location where the object is to be deployed
  * @param roll the amount of roll that affects orientation
  * @param pitch the amount of pitch that affects orientation
  * @param yaw the amount of yaw that affects orientation
  * @param unk2 na
  */
final case class DeployObjectMessage(object_guid : PlanetSideGUID,
                                     unk1 : Long,
                                     pos : Vector3,
                                     roll : Int,
                                     pitch : Int,
                                     yaw : Int,
                                     unk2 : Long)
  extends PlanetSideGamePacket {
  type Packet = DeployObjectMessage
  def opcode = GamePacketOpcode.DeployObjectMessage
  def encode = DeployObjectMessage.encode(this)
}

object DeployObjectMessage extends Marshallable[DeployObjectMessage] {
  implicit val codec : Codec[DeployObjectMessage] = (
    ("object_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint32L) ::
      ("pos" | Vector3.codec_pos) ::
      ("roll" | uint8L) ::
      ("pitch" | uint8L) ::
      ("yaw" | uint8L) ::
      ("unk2" | uint32L)
    ).as[DeployObjectMessage]
}
