// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{Angular, PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param unk1 na;
  *             `true` to apply the effect usually
  * @param unk2 na
  */
final case class TriggeredEffect(unk1 : Boolean,
                                 unk2 : Long)

/**
  * Activate an effect that is not directly associated with an existing game object.
  * Without a game object from which to inherit position and orientation, those explicit parameters must be provided.
  * @param pos the position in the game world
  * @param orient the angle of orientation
  */
//TODO must find proper North-corrective angle for orientation
final case class TriggeredEffectLocation(pos : Vector3,
                                         orient : Vector3)

/**
  * Dispatched by the server to cause a client to display a special graphical effect.<br>
  * <br>
  * The effect being triggered can be based around a specific game object or replayed freely, absent of an anchoring object.
  * If object-based then the kinds of effects that can be activated are specific to the object.
  * If unbound, then a wider range of effects can be displayed.
  * Regardless, one category will rarely ever be activated under the same valid conditions of the other category.
  * For example, the effect "on" will only work on objects that accept "on" normally, like a deployed `motionalarmsensor`.
  * The effect "spawn_object_effect" can be applied anywhere in the environment;
  * but, it can not be activated in conjunction with an existing object.
  * @param object_guid an object that accepts the effect
  * @param effect the name of the effect
  * @param unk na;
  *            when activating an effect on an existing object
  * @param location an optional position where the effect will be displayed;
  *                 when activating an effect independently
  */
final case class TriggerEffectMessage(object_guid : PlanetSideGUID,
                                      effect : String,
                                      unk : Option[TriggeredEffect] = None,
                                      location : Option[TriggeredEffectLocation] = None
                                     ) extends PlanetSideGamePacket {
  type Packet = TriggerEffectMessage
  def opcode = GamePacketOpcode.TriggerEffectMessage
  def encode = TriggerEffectMessage.encode(this)
}

object TriggerEffectMessage extends Marshallable[TriggerEffectMessage] {
  def apply(object_guid : PlanetSideGUID, effect : String) : TriggerEffectMessage =
    TriggerEffectMessage(object_guid, effect, None, None)

  def apply(object_guid : PlanetSideGUID, effect : String, unk1 : Boolean, unk2 : Long) : TriggerEffectMessage =
    TriggerEffectMessage(object_guid, effect, Some(TriggeredEffect(unk1, unk2)), None)

  def apply(effect : String, position : Vector3, orientation : Vector3) : TriggerEffectMessage =
    TriggerEffectMessage(PlanetSideGUID(0), effect, None, Some(TriggeredEffectLocation(position, orientation)))

  /**
    * A `Codec` for `TriggeredEffect` data.
    */
  private val effect_codec : Codec[TriggeredEffect] = (
    ("unk1" | bool) ::
      ("unk2" | uint32L)
  ).as[TriggeredEffect]

  /**
    * A `Codec` for `TriggeredEffectLocation` data.
    */
  private val effect_location_codec : Codec[TriggeredEffectLocation] = (
    ("pos" | Vector3.codec_pos) ::
      (("roll" | Angular.codec_roll) ::
      ("pitch" | Angular.codec_pitch) ::
      ("yaw" | Angular.codec_yaw())).xmap[Vector3] (
        {
          case x :: y :: z :: HNil =>
            Vector3(x, y, z)
        },
        {
          case Vector3(x, y, z) =>
            x :: y :: z :: HNil
        }
      )
  ).as[TriggeredEffectLocation]

  implicit val codec : Codec[TriggerEffectMessage] = (
    ("object_guid" | PlanetSideGUID.codec) >>:~ { guid =>
      ("effect" | PacketHelpers.encodedString) ::
      optional(bool, "unk" | effect_codec) ::
      conditional(guid.guid == 0, "location" | effect_location_codec)
    }).as[TriggerEffectMessage]
}
