// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched from the client to indicate the player wishes to use an orbital droppod
  * to rapidly deploy into a zone at a pre-approved position.<br>
  * <br>
  * Follows after an instance of "player stasis" where they are permitted to make this sort of selection
  * by referencing a zone from the interstellar deployment map.
  * This is the conclusion of utilizing the high altitude rapid transport (HART) system
  * though does not need to be limited only to prior use of the orbital shuttle.
  * @see `PlayerStasisMessage`
  * @param info information related to this droppod event
  * @param unk na;
  *            consistently 3
  */
final case class DroppodLaunchRequestMessage(
                                              info: DroppodLaunchInfo,
                                              unk: Int
                                            ) extends PlanetSideGamePacket {
  type Packet = DroppodLaunchRequestMessage
  def opcode = GamePacketOpcode.DroppodLaunchRequestMessage
  def encode = DroppodLaunchRequestMessage.encode(this)
}

object DroppodLaunchRequestMessage extends Marshallable[DroppodLaunchRequestMessage] {
  /**
    * Overloaded constructor that ignores the last field.
    * Existing fields match `DroppodLaunchInfo`.
    * @param guid the player using the droppod
    * @param zoneNumber the zone to which the player desires transportation
    * @param pos where in the zone (relative to the ground) the player will be placed
    * @return a `DroppodLaunchRequestMessage` packet
    */
  def apply(guid: PlanetSideGUID, zoneNumber: Int, pos: Vector3): DroppodLaunchRequestMessage =
    DroppodLaunchRequestMessage(DroppodLaunchInfo(guid, zoneNumber, pos), 3)

  implicit val codec: Codec[DroppodLaunchRequestMessage] = (
    DroppodLaunchInfo.codec ::
    ("unk" | uint2)
    ).as[DroppodLaunchRequestMessage]
}
