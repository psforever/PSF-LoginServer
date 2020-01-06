// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Dispatched from the client to request that an object be deployed.<br>
  * <br>
  * Information in the packet mainly reports about the surface on which the object will be coplanar when/if placed.
  * The server responds with a `ObjectDeployedMessage` packet with the results.
  * If successful, that is followed by an `ObjectCreateMessage` packet and a `DeployableObjectsInfoMessage` packet.
  * @param object_guid the object
  * @param unk1 na
  * @param pos the location where the object is to be deployed
  * @param orient the angle of orientation
  * @param unk2 na
  */
final case class DeployObjectMessage(object_guid : PlanetSideGUID,
                                     unk1 : Long,
                                     pos : Vector3,
                                     orient : Vector3,
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
      (("roll" | Angular.codec_roll) ::
        ("pitch" | Angular.codec_pitch) ::
        ("yaw" | Angular.codec_yaw())
        ).xmap[Vector3] (
        {
          case x :: y :: z :: HNil =>
            Vector3(x, y, z)
        },
        {
          case Vector3(x, y, z) =>
            x :: y :: z :: HNil
        }
        ) ::
      ("unk2" | uint32L)
    ).as[DeployObjectMessage]
}
