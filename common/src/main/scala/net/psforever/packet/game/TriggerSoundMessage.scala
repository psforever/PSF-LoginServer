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

  val SpawnInTube,  // 3dsounds - respawn_use
  HackTerminal,     // 3dsounds - equipment_rek_successful
  HackVehicle,      // 3dsounds - equipment_jack_successful
  HackDoor,         // 3dsounds - facility_door_hacked
  TREKSuccessful,   // patch5 - t_Rek_Successful (Note this file has the wrong bitrate set in the wav headers. It should be 44100 not 22050. Audio will sound HORRIBLE if you try to play it without fixing the headers)
  LockedOut,        // 3dsounds - facility_door_locked_feedback
  EMPPhase,         // patch1 - emp_phase
  LLUMaterialize,   // patch1 - LLU_Materialize
  LLUDeconstruct,   // patch1 - LLU_Deconstruct
  LLUInstall,       // patch1 - LLU_Install
  LLUPickup,        // patch1 - LLU_Pickup2
  LLUDrop,          // patch1 - LLU_Drop
  ModuleStabilized, // patch2 - module_stabilized2
  ModulePickup,     // patch2 - module_pickup
  ModuleDrop,       // patch2 - module_drop
  ModuleAlarmBeep,  // patch2 - module_alarm_beep
  ModuleExpiration, // patch2 - module_Expiration
  StasisDissipate,  // patch2 - statsis_dissipate
  StasisPickup,     // patch2 - stasis_pickup
  StasisEquip,      // patch2 - stasis_equip
  ModuleFFIntercept // patch2 - module_ff_intercept
  = Value

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
final case class TriggerSoundMessage(sound: TriggeredSound.Value, pos: Vector3, unk: Int, volume: Float)
    extends PlanetSideGamePacket {
  type Packet = TriggerSoundMessage
  def opcode = GamePacketOpcode.TriggerSoundMessage
  def encode = TriggerSoundMessage.encode(this)
}

object TriggerSoundMessage extends Marshallable[TriggerSoundMessage] {
  implicit val codec: Codec[TriggerSoundMessage] = (
    ("sound" | TriggeredSound.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk" | uintL(9)) ::
      ("volume" | uint8L)
  ).xmap[TriggerSoundMessage](
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
