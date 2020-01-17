// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when the player is interacting with a zip line.
  * Dispatched by the server to instruct the client to use the zip line.
  * Cavern teleportation rings also count as "zip lines" as far as the game is concerned, in that they use this packet.<br>
  * <br>
  * Action:<br>
  * `0 - Attach to a node`<br>
  * `1 - Arrived at destination`<br>
  * `2 - Forcibly detach from zip line in mid-transit`
  * @param player_guid the player
  * @param forwards true if the player is travelling in the direction of the light pulses
  * @param action how the player interacts with the zip line
  * @param path_id the path id that this zipline belongs to, from the relevant .zpl file
  * @param pos the coordinates of the point where the player is interacting with the zip line;
  *            "optional," in theory
  */
final case class ZipLineMessage(player_guid : PlanetSideGUID,
                                forwards : Boolean,
                                action : Int,
                                path_id : Long,
                                pos : Option[Vector3] = None)
  extends PlanetSideGamePacket {
  type Packet = ZipLineMessage
  def opcode = GamePacketOpcode.ZipLineMessage
  def encode = ZipLineMessage.encode(this)
}

object ZipLineMessage extends Marshallable[ZipLineMessage] {
  /**
    * Alternate constructor for `ZipLineMessage` that requirement for the last field.
    *
    * @param player_guid       the player
    * @param forwards          true if the player is travelling in the direction of the light pulses
    * @param action            how the player interacts with the zip line
    * @param path_id           the path id that this zipline belongs to, from the relevant .zpl file
    * @param pos               the coordinates of the point where the player is interacting with the zip line
    * @return a `ZipLineMessage` object
    */
  def apply(player_guid : PlanetSideGUID, forwards : Boolean, action : Int, path_id : Long, pos : Vector3) : ZipLineMessage = {
    ZipLineMessage(player_guid, forwards, action, path_id, Some(pos))
  }

  implicit val codec : Codec[ZipLineMessage] = (
    ("player_guid" | PlanetSideGUID.codec) >>:~ { player =>
      ("forwards" | bool) ::
        ("action" | uint2) ::
        ("id" | uint32L) ::
        conditional(player.guid > 0, Vector3.codec_float) // !(player.guid == 0)
    }
    ).as[ZipLineMessage]
}
