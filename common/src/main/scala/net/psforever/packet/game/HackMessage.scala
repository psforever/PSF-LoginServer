// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * An `Enumeration` of the various states and activities of the hacking process.
  * These values are closely tied to the condition of the hacking progress bar and/or the condition of the hacked object.<br>
  * <br>
  * `Start` initially displays the hacking progress bar.<br>
  * `Ongoing` is a neutral state that keeps the progress bar displayed while its value updates. (unconfirmed?)<br>
  * `Finished` disposes of the hacking progress bar.  It does not, by itself, mean the hack was successful.<br>
  * `Hacked` modifies the target of the hack.<br>
  * `HackCleared` modifies the target of the hack, opposite of `Hacked`.
  */
object HackState extends Enumeration {
  type Type = Value

  val
  Unknown0,
  Start,
  Unknown2,
  Ongoing,
  Finished,
  Unknown5,
  Hacked,
  HackCleared
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)
}

/**
  * Dispatched by the server to control the process of hacking.<br>
  * <br>
  * Part of the hacking process is regulated by the server while another part of it is automatically reset by the client.
  * The visibility, update, and closing of the hacking progress bar must be handled manually, for each tick.
  * When hacking is complete, using the appropriate `HackState` will cue the target to be affected by the hack.
  * Terminals and door IFF panels will temporarily expose their functionality;
  * the faction association of vehicles will be converted permanently;
  * a protracted process of a base conversion will be enacted; etc..
  * This transfer of faction association occurs to align the target with the faction of the hacking player (as indicated).
  * The client will select the faction without needing to be explicitly told
  * and will select the appropriate action to enact upon the target.
  * Upon the hack's completion, the target on the client will automatically revert back to its original state, if possible.
  * (It will still be necessary to alert this change from the server's perspective.)
  * @param unk1 na;
  *             hack type?
  * @param target_guid the target of the hack
  * @param player_guid the player
  * @param progress the amount of progress visible;
  *                 visible range is 0 - 100
  * @param unk5 na;
  *             often a large number;
  *             doesn't seem to be `char_id`?
  * @param hack_state hack state
  * @param unk7 na;
  *             usually, 8?
  */
final case class HackMessage(unk1 : Int,
                             target_guid : PlanetSideGUID,
                             player_guid : PlanetSideGUID,
                             progress : Int,
                             unk5 : Long,
                             hack_state : HackState.Value,
                             unk7 : Long)
  extends PlanetSideGamePacket {
  type Packet = HackMessage
  def opcode = GamePacketOpcode.HackMessage
  def encode = HackMessage.encode(this)
}

object HackMessage extends Marshallable[HackMessage] {
  implicit val codec : Codec[HackMessage] = (
    ("unk1" | uint2L) ::
      ("object_guid" | PlanetSideGUID.codec) ::
      ("player_guid" | PlanetSideGUID.codec) ::
      ("progress" | uint8L) ::
      ("unk5" | uint32L) ::
      ("hack_state" | HackState.codec) ::
      ("unk7" | uint32L)
  ).as[HackMessage]
}
