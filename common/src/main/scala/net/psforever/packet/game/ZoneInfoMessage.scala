// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Change the "Empire Status" text in the Interstellar Map zone description for the specified zone.
  * The Empire Incentives window also displays the changed information.<br>
  * <br>
  * The `Long` value is applicable to the caverns.
  * It indicates how long the given cavern will remain locked until at least one of their geowarps will open during normal rotation.
  * As thus, when a cavern has its status as "inactive," this number will always be a non-zero.
  * On normal continents, this number is always zero, though a non-zero number will not have any effect anyway.
  * @param zone the zone id
  * @param empire_status `true` displays the queue availability for the avatar's empire;
  *                      `false` displays "Inactive"
  * @param lock_time how long until the continent naturally unlocks (in ms);
  *                  only applicable to caverns
  * @see `ZonePopulationUpdateMessage` for information on population and queues
  */
final case class ZoneInfoMessage(zone : Int,
                                 empire_status : Boolean,
                                 lock_time : Long)
  extends PlanetSideGamePacket {
  type Packet = ZoneInfoMessage
  def opcode = GamePacketOpcode.ZoneInfoMessage
  def encode = ZoneInfoMessage.encode(this)
}

object ZoneInfoMessage extends Marshallable[ZoneInfoMessage] {
  implicit val codec : Codec[ZoneInfoMessage] = (
    ("zone" | uint16L) ::
      ("empire_status" | bool) ::
      ("lock_time" | uint32L)
    ).as[ZoneInfoMessage]
}
