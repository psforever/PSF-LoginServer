// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Change the "Empire Status" text in the Interstellar Map zone description for the specified zone.
  * The Empire Incentives window also displays the changed information.
  * @param zone the zone id
  * @param lock_status `true` displays "Locked;"
  *                    `false` displays the queue availability for the avatar's empire
  * @param unk na;
  *            usually `true`
  */
final case class ZoneLockInfoMessage(zone: Int, lock_status: Boolean, unk: Boolean) extends PlanetSideGamePacket {
  type Packet = ZoneLockInfoMessage
  def opcode = GamePacketOpcode.ZoneLockInfoMessage
  def encode = ZoneLockInfoMessage.encode(this)
}

object ZoneLockInfoMessage extends Marshallable[ZoneLockInfoMessage] {
  implicit val codec: Codec[ZoneLockInfoMessage] = (
    ("zone" | uint16L) ::
      ("lock_status" | bool) ::
      ("unk" | bool)
  ).as[ZoneLockInfoMessage]
}
