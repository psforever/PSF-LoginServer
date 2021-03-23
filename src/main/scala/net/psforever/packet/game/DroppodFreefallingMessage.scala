// Copyright (c) 2020 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID, Vector3}
import scodec.Attempt.Successful
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Dispatched by the server to trigger a droppod's traditional behavior of plummeting from lower orbit like a rock and
  * slowing to a gentle land, breaking apart like flower petals to introduce a soldier to the battlefield.<br>
  * <br>
  * Only works on droppod-type vehicles.
  * Only works if a client avatar is mounted in the vehicle.
  * The furthest the vehicle will fall is determined by that avatar player's interaction with the ground.
  * The camera is maneuvered in three ways -
  * where it starts,
  * where it tracks the falling vehicle,
  * where it zooms in upon landing.
  * Only the "where it starts" portion of the camera is slightly manipulable.
  * @param guid the global unique identifier of the droppod
  * @param pos the position of the droppod
  * @param vel how quickly the droppod is moving
  * @param pos2 suggestion for positioning external viewpoint while observing the droppod descending;
  *             the most common offset from the model position was `Vector3(-20, 1.156f, -50)`
  * @param orientation1 na;
  *                     the y-component is usually 70.3125f
  * @param orientation2 na
  */
final case class DroppodFreefallingMessage(
    guid: PlanetSideGUID,
    pos: Vector3,
    vel: Vector3,
    pos2: Vector3,
    orientation1: Vector3,
    orientation2: Vector3
) extends PlanetSideGamePacket {
  type Packet = DroppodFreefallingMessage
  def opcode = GamePacketOpcode.DroppodFreefallingMessage
  def encode = DroppodFreefallingMessage.encode(this)
}

object DroppodFreefallingMessage extends Marshallable[DroppodFreefallingMessage] {
  private val rotation: Codec[Vector3] = (
    Angular.codec_roll ::
    Angular.codec_pitch ::
    Angular.codec_yaw()
    ).narrow[Vector3](
    {
      case u :: v :: w :: HNil => Successful(Vector3(u, v, w))
    },
    v => v.x :: v.y :: v.z :: HNil
  )

  implicit val codec: Codec[DroppodFreefallingMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
    ("pos" | Vector3.codec_float) ::
    ("vel" | Vector3.codec_float) ::
    ("pos2" | Vector3.codec_float) ::
    ("orientation1" | rotation) ::
    ("orientation2" | rotation)
  ).as[DroppodFreefallingMessage]
}
