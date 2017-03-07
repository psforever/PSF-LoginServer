// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Cloud data.<br>
  * <br>
  * The remaining fields should be divided between a "location" and a "velocity" as per debug output.
  * The values are probably paired.
  * The converted data, however, seems weird for the kind of information those fields would suggest.
  * @param id the id of the cloud;
  *           zero-indexed counter (probably)
  * @param unk1 na;
  *            the z-component is always `0.0f`
  * @param unk2 na;
  *            the z-component is always `0.0f`
  */
final case class CloudInfo(id : Int,
                           unk1 : Vector3,
                           unk2 : Vector3)

/**
  * Storm data.<br>
  * <br>
  * The remaining fields should be divided between an "intensity" and a "radius" as per debug output.
  * The converted data, however, seems weird for the kind of information those fields would suggest.
  * @param loc the location of the storm;
  *            the z-component is always `0.0f`
  * @param unk1 na
  * @param unk2 na
  */
final case class StormInfo(loc : Vector3,
                           unk1 : Int,
                           unk2 : Int)

/**
  * Dispatched by the server to update weather conditions.
  * On former live (Gemini), the server sent a new packet to connected clients once every ~60s.<br>
  * <br>
  * Information about the fields in this packet come from extracted debug information.
  * It is not necessarily "correct" but it is the best approximation for now.<br>
  * <br>
  * `
  * Message type:   %d (%s)\n        length: %d\n<br>
  * Number of Clouds : %d\n<br>
  * Cloud ID: %d\n<br>
  * \tCloud Location: %f %f\n<br>
  * \tCloud Velocity: %f %f\n<br>
  * Number of Storms : %d\n<br>
  * Storm:\n<br>
  * \tStorm Location: %f %f\n<br>
  * \tStorm Intensity: %d\n<br>
  * \tStorm Radius: %d\n<br>
  * `
  * @param clouds a list of cloud data;
  *               typically, just one entry
  * @param storms a list of storm data;
  *               typically, fluctuates between nine and eleven entries
  */
final case class WeatherMessage(clouds : List[CloudInfo],
                                storms : List[StormInfo])
  extends PlanetSideGamePacket {
  type Packet = WeatherMessage
  def opcode = GamePacketOpcode.WeatherMessage
  def encode = WeatherMessage.encode(this)
}

object WeatherMessage extends Marshallable[WeatherMessage] {
  /**
    * `Codec` for `CloudInfo` data.
    */
  private val cloudCodec : Codec[CloudInfo] = (
    ("id" | uint8L) ::
      ("unk1x" | floatL) ::
      ("unk1y" | floatL) ::
      ("unk2x" | floatL) ::
      ("unk2y" | floatL)
    ).xmap[CloudInfo] (
    {
      case id :: x1 :: y1 :: x2 :: y2 :: HNil =>
        CloudInfo(id, Vector3(x1, y1, 0.0f), Vector3(x2, y2, 0.0f))
    },
    {
      case CloudInfo(id, Vector3(x1, y1, _), Vector3(x2, y2, _)) =>
        id :: x1 :: y1 :: x2 :: y2 :: HNil
    }
  )

  /**
    * `Codec` for `StormInfo` data.
    */
  private val stormCodec : Codec[StormInfo] = (
    ("unkx" | floatL) ::
      ("unky" | floatL) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L)
    ).xmap[StormInfo] (
    {
      case x :: y :: u1 :: u2 :: HNil =>
        StormInfo(Vector3(x, y, 0.0f), u1, u2)
    },
    {
      case StormInfo(Vector3(x, y, _), u1, u2) =>
        x :: y :: u1 :: u2 :: HNil
    }
  )

  implicit val codec : Codec[WeatherMessage] = (
    ("clouds" | PacketHelpers.listOfNAligned(uint32L, 0, cloudCodec)) ::
      ("storms" | PacketHelpers.listOfNAligned(uint32L, 0, stormCodec))
    ).as[WeatherMessage]
}
