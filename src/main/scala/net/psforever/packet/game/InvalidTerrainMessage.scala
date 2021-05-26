// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Attempt.Successful
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * An `Enumeration` of the two conditions of the terrain - safe and unsafe.
  */
object TerrainCondition extends Enumeration {
  type Type = Value
  val Safe, Unsafe = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(enum = this, uint(bits = 1))
}

/**
  * na
  * @param player_guid the player who is driving the vehicle
  * @param vehicle_guid the vehicle
  * @param proximity_alert whether this position is invalid;
  *                        `true`, for the nearby terrain being unsuitable;
  *                        `false`, when the vehicle has been moved back to a safe distance (place)
  * @param pos the vehicle's current position in the game world
  */
final case class InvalidTerrainMessage(
                                        player_guid: PlanetSideGUID,
                                        vehicle_guid: PlanetSideGUID,
                                        proximity_alert: TerrainCondition.Value,
                                        pos: Vector3
                                      ) extends PlanetSideGamePacket {
  type Packet = InvalidTerrainMessage
  def opcode = GamePacketOpcode.InvalidTerrainMessage
  def encode = InvalidTerrainMessage.encode(this)
}

object InvalidTerrainMessage extends Marshallable[InvalidTerrainMessage] {

  implicit val codec: Codec[InvalidTerrainMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
    ("vehicle_guid" | PlanetSideGUID.codec) ::
    ("proximity_alert" | TerrainCondition.codec) ::
    ("pos" | floatL :: floatL :: floatL).narrow[Vector3](
      {
        case x :: y :: z :: HNil => Successful(Vector3(x, y, z))
      },
      v => v.x :: v.y :: v.z :: HNil
    )).as[InvalidTerrainMessage]
}
