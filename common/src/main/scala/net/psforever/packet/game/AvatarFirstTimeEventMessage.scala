// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched to the server when the player encounters something for the very first time in their campaign.
  * For example, the first time the player rubs up against a game object with a yellow exclamation point.
  * For example, the first time the player draws a specific weapon.<br>
  * <br>
  * When the first time events (FTE's) are received, battle experience is awarded.
  * Text information about the object will be displayed.
  * A certain itemized checkbox under the "Training" tab that corresponds is marked off.
  * The latter list indicates all "encounter-able" game objects for which a FTE exists.
  * These effects only happen once per character/campaign.
  * (The Motion Sensor will occasionally erroneously display the information window on repeat encounters.
  * No additional experience is given, though.)<br>
  * <br>
  * FTE's are recorded in a great `List` of `String`s in the middle of player `ObjectCreateMessage` data.
  * Tutorial complete events are enqueued nearby.
  * @param avatar_guid the player
  * @param object_id the game object that triggers the event
  * @param unk na
  * @param event_name the string name of the event
  */
final case class AvatarFirstTimeEventMessage(avatar_guid : PlanetSideGUID,
                                             object_id : PlanetSideGUID,
                                             unk : Long,
                                             event_name : String)
  extends PlanetSideGamePacket {
  type Packet = AvatarFirstTimeEventMessage
  def opcode = GamePacketOpcode.AvatarFirstTimeEventMessage
  def encode = AvatarFirstTimeEventMessage.encode(this)
}

object AvatarFirstTimeEventMessage extends Marshallable[AvatarFirstTimeEventMessage] {
  implicit val codec : Codec[AvatarFirstTimeEventMessage] = (
     ("avatar_guid" | PlanetSideGUID.codec) ::
     ("object_id" | PlanetSideGUID.codec) ::
     ("unk" | uint32L ) ::
     ("event_name" | PacketHelpers.encodedString)
    ).as[AvatarFirstTimeEventMessage]
}
