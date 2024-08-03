// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

sealed abstract class HackState1(val value: Int) extends IntEnumEntry

object HackState1 extends IntEnum[HackState1] {
  val values: IndexedSeq[HackState1] = findValues

  case object Unk0 extends HackState1(value = 0)
  case object Unk1 extends HackState1(value = 1)
  case object Unk2 extends HackState1(value = 2)
  case object Unk3 extends HackState1(value = 3)

  implicit val codec: Codec[HackState1] = PacketHelpers.createIntEnumCodec(this, uint2)
}

sealed abstract class HackState7(val value: Int) extends IntEnumEntry

object HackState7 extends IntEnum[HackState7] {
  val values: IndexedSeq[HackState7] = findValues

  case object Unk0 extends HackState7(value = 0)
  case object Unk1 extends HackState7(value = 1)
  case object Unk2 extends HackState7(value = 2)
  case object Unk3 extends HackState7(value = 3)
  case object Unk4 extends HackState7(value = 4)
  case object Unk5 extends HackState7(value = 5)
  case object Unk6 extends HackState7(value = 6)
  case object Unk7 extends HackState7(value = 7)
  case object Unk8 extends HackState7(value = 8)

  implicit val codec: Codec[HackState7] = (PacketHelpers.createIntEnumCodec(this, uint8) :: ignore(size = 24)).xmap[HackState7](
    {
      case a :: _ :: HNil => a
    },
    {
      a => a :: () :: HNil
    }
  )
}

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
sealed abstract class HackState(val value: Int) extends IntEnumEntry

object HackState extends IntEnum[HackState] {
  val values: IndexedSeq[HackState] = findValues

  case object Unknown0 extends HackState(value = 0)
  case object Start extends HackState(value = 1)
  case object Cancelled extends HackState(value = 2)
  case object Ongoing extends HackState(value = 3)
  case object Finished extends HackState(value = 4)
  case object Unknown5 extends HackState(value = 5)
  case object Hacked extends HackState(value = 6)
  case object HackCleared extends HackState(value = 7)

  implicit val codec: Codec[HackState] = PacketHelpers.createIntEnumCodec(this, uint8L)
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
 *             1 unknown;
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
 *             usually 8;
 *             values 3-7 noted for the Hacked state;
  *             5 - boost pain field at matrixing terminal?
  */
final case class HackMessage(
                              unk1: HackState1,
                              target_guid: PlanetSideGUID,
                              player_guid: PlanetSideGUID,
                              progress: Int,
                              unk5: Float,
                              hack_state: HackState,
                              unk7: HackState7
                            ) extends PlanetSideGamePacket {
  type Packet = HackMessage
  def opcode: Type = GamePacketOpcode.HackMessage
  def encode: Attempt[BitVector] = HackMessage.encode(this)
}

object HackMessage extends Marshallable[HackMessage] {
  def apply(
             unk1: HackState1,
             target_guid: PlanetSideGUID,
             player_guid: PlanetSideGUID,
             progress: Int,
             unk5: Int,
             hack_state: HackState,
             unk7: HackState7
           ): HackMessage = {
    new HackMessage(unk1, target_guid, player_guid, progress, unk5.toFloat, hack_state, unk7)
  }

  implicit val codec: Codec[HackMessage] = (
    ("unk1" | HackState1.codec) ::
      ("object_guid" | PlanetSideGUID.codec) ::
      ("player_guid" | PlanetSideGUID.codec) ::
      ("progress" | uint8L) ::
      ("unk5" | floatL) ::
      ("hack_state" | HackState.codec) ::
      ("unk7" | HackState7.codec)
  ).as[HackMessage]
}
