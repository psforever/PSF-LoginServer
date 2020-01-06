// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
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
  Cancelled,
  Ongoing,
  Finished,
  Unknown5,
  Hacked,
  HackCleared
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)
}

/**
  * Dispatched by the server to control the progress of hacking.
  * While "hacking" is typically performed against enemy targets,
  * some actions that involve ally on ally hacking can occur.
  * In this sense, hacking can be consider change progress.<br>
  * <br>
  * In general, the act of hacking is maintained by the server but the conclusion is managed by the client.
  * Hacking typically locks the player into a cancellable firing animation and works as all-or-nothing.
  * The progress bar window is displayed and updated each tick by the server; but, the client can cancel it on its own.
  * When hacking is complete as indicated by the appropriate `HackState`,
  * the client performs the intended action upon the target.
  * Facility amenities will temporarily ignore IFF requirements;
  * vehicles will permanently transfer control over to the hack-starter's empire;
  * facility turret weapons will temporarily convert to their anti-vehicle or anti-aircraft configurations;
  * facilities will be compromised and begin the long process of converting to the hack-starter's empire;
  * and, so forth.<br>
  * <br>
  * As mentioned, one of the unexpected uses of this message
  * will assist the conversion of allied facility turreted weapons to their upgraded armaments.
  * @param unk1 na;
  *             0 commonly;
  *             2 when performing (phalanx) upgrades;
  *             3 for building objects during login phase;
  *             hack type?
  *             possibly player hacking level 0-3?
  * @param target_guid the target of the hack
  * @param player_guid the player
  * @param progress the amount of progress visible;
  *                 visible range is 0 - 100
  * @param unk5 na;
  *             often a large number;
  *             doesn't seem to be `char_id`?
  * @param hack_state hack state
  * @param unk7 na;
  *             5 - boost pain field at matrixing terminal?
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
