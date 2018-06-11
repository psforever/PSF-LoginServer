// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * An `Enumeration` of the sounds triggered by this packet.
  * Twenty-one possible sounds are available for playback.
  */
object TriggeredSound extends Enumeration {
  type Type = Value

  val
  SpawnInTube,
  HackTerminal,
  HackVehicle,
  HackDoor,
  Unknown4,
  LockedOut,
  Unknown6,
  Unknown7,
  Unknown8,
  Unknown9,
  Unknown10,
  Unknown11,
  Unknown12,
  Unknown13,
  Unknown14,
  Unknown15,
  Unknown16,
  Unknown17,
  Unknown18,
  Unknown19,
  Unknown20 = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uintL(5))
}

/**
  * Dispatched by the server to cause a sound to be played at a certain location in the world.
  * @param sound the kind of sound
  * @param pos the location where the sound gets played
  * @param unk na;
  *            may be radius
  * @param volume the volume of the sound at the origin (0.0f - 1.0f)
  */
final case class TriggerSoundMessage(sound : TriggeredSound.Value,
                                     pos : Vector3,
                                     unk : Int,
                                     volume : Float)
  extends PlanetSideGamePacket {
  type Packet = TriggerSoundMessage
  def opcode = GamePacketOpcode.TriggerSoundMessage
  def encode = TriggerSoundMessage.encode(this)
}

object TriggerSoundMessage extends Marshallable[TriggerSoundMessage] {
  implicit val codec : Codec[TriggerSoundMessage] = (
    ("sound" | TriggeredSound.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk" | uintL(9)) ::
      ("volume" | uint8L)
    ).xmap[TriggerSoundMessage] (
      {
        case a :: b :: c :: d :: HNil =>
          TriggerSoundMessage(a, b, c, d.toFloat * 0.0039215689f)
      },
      {
        case TriggerSoundMessage(a, b, c, d) =>
          a :: b :: c :: (d * 255f).toInt :: HNil
      }
    )
}
