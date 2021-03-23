// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{HartSequence, PlanetSideGUID}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Paired globally unique identifier numbers,
  * the first one being the pad (`obbasemesh`) of a HART shuttle building,
  * the second being the shuttle itself.
  * @param pad the HART shuttle pad
  * @param shuttle the HART orbital shuttle
  * @param unk a control code;
  *            has indeterminate purpose regardless of the phase expressed in the greater packet;
  *            frequently `20` but also frequently varies
  */
final case class PadAndShuttlePair(pad: PlanetSideGUID, shuttle: PlanetSideGUID, unk: Int)

/**
  * Control the animation state transitions of the high altitude rapid transport (HART) orbital shuttle building
  * and the accompanying orbital shuttle model.<br>
  * <br>
  * The animation sequence is controlled primarily by the first field and
  * goes through a strict cycle of boarding, lift shuttle, takeoff, land, lower shuttle.
  * All HART facilities (amenity `obbasemesh`) in a given zone are controlled by this packet.
  * Multiple systems are controlled by a single field during a given animation,
  * e.g., the boarding gantries are retracted or extended during the same part where the shuttle is raised or lowered.
  * Certain neutral animation states - `State0`, `State5`, and `State7` - all behave the same way
  * though denote different points in the sequence.
  * Animation subsequence states are coordinated by the second field,
  * though the specific purpose of the subsequence isn't always obvious,
  * and the field isn't always necessary to achieve the result of the primary sequence.<br>
  * <br>
  * The total time of the system is bound between two states:
  * whether the shuttle has left or whether it is boarding.
  * When separated ("has left"),
  * the shuttle will be lifted out of the bay to atop the building and will fly off into the horizon,
  * remaining despawned until it returns to view, perches atop the building again, and is lowered into the bay.
  * When boarding,
  * the shuttle is fixed in the bay and is accepting passengers via one of the boarding hallways.
  * Upon boarding the shuttle, the time until takeoff ("has left") is displayed to all waiting passengers
  * in the form of a progress bar.
  * This progress bar is fixed to a full time of 60 seconds (60000 milliseconds) in the client and
  * will start at fractions of completion for boarding times under 60 seconds.<br>
  * <br>
  * Pairs of globally unique identifiers for the shuttle facility and the shuttle
  * link the time fields to their function.
  * All facilities and shuttles in a given zone are paired and enumerated for a single packet.
  * If the HART facility identifier is missing or incorrect,
  * the absent facility will continue to undergo correct animation state transition,
  * but the door timer will not animate correctly and constantly display the time 10:37 and
  * the door lights will be neither locked closed (red) or openable (green).
  * If the shuttle identifier is missing or incorrect,
  * the absent shuttle will continue to undergo partially correct animation state transitions,
  * cycling between visible and invisible atop the HART facility,
  * and the aforementioned progress bars visible by shuttle passengers will not display during the boarding phase
  * if the shuttle is made available for boarding.
  * @param model_state a control code that affects the over-all state of the HART system
  * @param unk0 na
  * @param arrival_time the time for the orbital shuttle to return during instances when the shuttle is away;
  *                     displayed on a related time near the shuttle boarding entryways;
  *                     in milliseconds
  * @param boarding_time the time for the orbital shuttle to depart during instances when the shuttle is boarding;
  *                      frequently `8000L` when not in use;
  *                      in milliseconds
  * @param other_time time field used for a variety of things;
  *                   in most uses, the amount of time that has passed since the start of the event,
  *                   so usually `0` (at start of event);
  *                   with respects to `model_state` and `unk3`:
  *                   full departure time when `5`-`3` (variant of `7`-`3`);
  *                   occasionally, full departure time when `0`-`0`
  *                   in milliseconds
  * @param pairs a list of entries that pair
  *              a paired facility pad unique identifier and shuttle unique identifier
  *              with a control code
  */
final case class OrbitalShuttleTimeMsg(
                                        model_state: HartSequence,
                                        unk0: Int,
                                        arrival_time: Long,
                                        boarding_time: Long,
                                        other_time: Long,
                                        pairs: List[PadAndShuttlePair]
                                      )
  extends PlanetSideGamePacket {
  type Packet = OrbitalShuttleTimeMsg
  def opcode = GamePacketOpcode.OrbitalShuttleTimeMsg
  def encode = OrbitalShuttleTimeMsg.encode(this)
}

object OrbitalShuttleTimeMsg extends Marshallable[OrbitalShuttleTimeMsg] {
  private val uint3: Codec[Int] = uint(bits = 3)

  private val hartSequenceCodec: Codec[HartSequence] = PacketHelpers.createIntEnumCodec(HartSequence, uint3)

  private val padShuttlePair_codec: Codec[PadAndShuttlePair] = (
    ("pad" | PlanetSideGUID.codec) ::
      ("shuttle" | PlanetSideGUID.codec) ::
      ("unk" | uint(bits = 6))
    ).as[PadAndShuttlePair]

  implicit val codec: Codec[OrbitalShuttleTimeMsg] = (
    uint3 >>:~ { size =>
      ("model_state" | hartSequenceCodec) ::
      ("unk0" | uint3) ::
      ("arrival_time" | uint32L) ::
      ("boarding_time" | uint32L) ::
      bool ::
      ("other_time" | uint32L) ::
      ("pairs" | PacketHelpers.listOfNSized(size, padShuttlePair_codec))
    }
    ).xmap[OrbitalShuttleTimeMsg](
    {
      case _ :: model :: u0 :: arrival :: boarding :: _ :: other :: pairs :: HNil =>
        OrbitalShuttleTimeMsg(model, u0, arrival, boarding, other, pairs)
    },
    {
      case OrbitalShuttleTimeMsg(model, u0, arrival, boarding, other, pairs) =>
        pairs.length :: model :: u0 :: arrival :: boarding :: true :: other :: pairs :: HNil
    }
  )
}
