// Copyright (c) 2017 PSForever
package net.psforever.packet.control

import net.psforever.packet.{ControlPacketOpcode, Marshallable, PlanetSideControlPacket}
import scodec.Codec
import scodec.codecs._

/**
  * The response packet dispatched by the server to a client's `ControlSync` packet.
  * As noted, it echoes most of the fields originating from within its companion packet except for `serverTick`.
  * @param timeDiff na;
  *                 echoes `ControlSync.timeDiff`
  * @param serverTick na
  * @param field1 na;
  *               echoes `ControlSync.field64A`
  * @param field2 na;
  *               echoes `ControlSync.field64B`
  * @param field3 na;
  *               echoes `ControlSync.field64B` (+/- 1)
  * @param field4 na;
  *               echoes `ControlSync.field64A`
  */
final case class ControlSyncResp(
    timeDiff: Int,
    serverTick: Long,
    field1: Long,
    field2: Long,
    field3: Long,
    field4: Long
) extends PlanetSideControlPacket {
  type Packet = ControlSyncResp
  def opcode = ControlPacketOpcode.ControlSyncResp
  def encode = ControlSyncResp.encode(this)
}

object ControlSyncResp extends Marshallable[ControlSyncResp] {
  implicit val codec: Codec[ControlSyncResp] = (
    ("time_diff" | uint16) ::
      ("server_tick" | uint32) ::
      ("field1" | int64) ::
      ("field2" | int64) ::
      ("field3" | int64) ::
      ("field4" | int64)
  ).as[ControlSyncResp]
}
