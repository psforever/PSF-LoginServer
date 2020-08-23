// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client periodically (approximately once every ten seconds).
  * @param timeDiff the exact number of milliseconds since the last `ControlSync` packet
  * @param unk na
  * @param field1 na
  * @param field2 na
  * @param field3 na
  * @param field4 na
  * @param field64A na;
  *                 increments by 41 per packet
  * @param field64B na;
  *                 increments by 21 per packet
  */
final case class ControlSync(
    timeDiff: Int,
    unk: Long,
    field1: Long,
    field2: Long,
    field3: Long,
    field4: Long,
    field64A: Long,
    field64B: Long
) extends PlanetSideControlPacket {
  type Packet = ControlSync
  def opcode = ControlPacketOpcode.ControlSync
  def encode = ControlSync.encode(this)
}

object ControlSync extends Marshallable[ControlSync] {
  implicit val codec: Codec[ControlSync] = (
    ("time_diff" | uint16) ::
      ("unk" | uint32) ::
      ("field1" | uint32) ::
      ("field2" | uint32) ::
      ("field3" | uint32) ::
      ("field4" | uint32) ::
      ("field64A" | int64) ::
      ("field64B" | int64)
  ).as[ControlSync]
}
