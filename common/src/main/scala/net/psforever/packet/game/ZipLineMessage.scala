// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

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
  * @param origin_side whether this corresponds with the "entry" or the "exit" of the zip line, as per the direction of the light pulse visuals
  * @param action how the player interacts with the zip line
  * @param guid a number that is consistent to a terminus
  * @param x the x-coordinate of the point where the player is interacting with the zip line
  * @param y the y-coordinate of the point where the player is interacting with the zip line
  * @param z the z-coordinate of the point where the player is interacting with the zip line
  */
final case class ZipLineMessage(player_guid : PlanetSideGUID,
                                origin_side : Boolean,
                                action : Int,
                                guid : Long,
                                x : Float,
                                y : Float,
                                z : Float)
  extends PlanetSideGamePacket {
  type Packet = ZipLineMessage
  def opcode = GamePacketOpcode.ZipLineMessage
  def encode = ZipLineMessage.encode(this)
}

object ZipLineMessage extends Marshallable[ZipLineMessage] {
  type threeFloatsPattern = Float :: Float :: Float :: HNil

  /**
    * A `Codec` for when three `Float` values are to be read or written.
    */
  val threeFloatValues : Codec[threeFloatsPattern] = (
    ("x" | floatL) ::
      ("y" | floatL) ::
      ("z" | floatL)
    ).as[threeFloatsPattern]

  /**
    * A `Codec` for when there are no extra `Float` values present.
    */
  val noFloatValues : Codec[threeFloatsPattern] = ignore(0).xmap[threeFloatsPattern] (
    {
      case () =>
        0f :: 0f :: 0f :: HNil
    },
    {
      case _ =>
        ()
    }
  )

  implicit val codec : Codec[ZipLineMessage] = (
    ("player_guid" | PlanetSideGUID.codec) >>:~ { player =>
      ("origin_side" | bool) ::
        ("action" | uint2) ::
        ("id" | uint32L) ::
        newcodecs.binary_choice(player.guid > 0, threeFloatValues, noFloatValues) // !(player.guid == 0)
    }
    ).as[ZipLineMessage]
}
