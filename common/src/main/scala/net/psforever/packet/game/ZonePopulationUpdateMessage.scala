// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Report the raw numerical population for a zone (continent).<br>
  * <br>
  * Populations are displayed as percentages of the three main empires against each other.
  * Populations specific to a zone will be displayed in the Incentives window for that zone.
  * Populations in all zones will contribute to the Global Population window and the Incentives window for the server.
  * The Black OPs population does not show up in the Incentives window for a zone but will be indirectly represented in the other two windows.
  * This packet also shifts the flavor text for that zone.<br>
  * <br>
  * The size of zone's queue is the final upper population limit for that zone.
  * Common values for the zone queue fields are 00 00 00 00 (locked) and 9E 01 00 00 (414 positions).
  * When a continent can not accept any players at all, a lock icon will appear over its view pane in the Interstellar View.
  * Setting the zone's queue to zero will also render this icon.<br>
  * <br>
  * The individual queue fields set the maximum empire occupancy for a zone that is represented in the zone Incentives text.
  * Common values for the empire queue fields are 00 00 00 00 (locked population), 8A 00 00 00 (138 positions), and FA 01 00 00 (500 positions).
  * Zone Incentives text, however, will never report more than a "100+" vacancy.
  * The actual limits are probably set based on server load.
  * The latter queue value is typical for VR area zones.<br>
  * <br>
  * The value of the zone queue trumps the sum of all individual empire queues.
  * Regardless of individual queues, once total zone population matches the zone queue size, all populations will lock.
  * For normal zones, if the individual queues are not set properly, whole empires can even be locked out of a zone for this reason.
  * In the worst case, other empires are allowed enough individual queue vacancy that they can occupy all the available slots.
  * Sanctuary zones possess strange queue values that are occasionally zero'd.
  * They do not have a lock icon and may not limit populations the same way as normal zones.
  *
  * @param continent_guid identifies the zone (continent)
  * @param zone_queue the maximum population of all three (four) empires that can join this zone
  * @param tr_queue the maximum number of TR players that can join this zone
  * @param tr_pop the current TR population in this zone
  * @param nc_queue the maximum number of NC players that can join this zone
  * @param nc_pop the current NC population in this zone
  * @param vs_queue the maximum number of VS players that can join this zone
  * @param vs_pop the VS population in this zone
  * @param bo_queue the maximum number of Black OPs players that can join this zone
  * @param bo_pop the current Black OPs population in this zone
  */
final case class ZonePopulationUpdateMessage(continent_guid : PlanetSideGUID,
                                             zone_queue : Long,
                                             tr_queue : Long,
                                             tr_pop : Long,
                                             nc_queue : Long,
                                             nc_pop : Long,
                                             vs_queue : Long,
                                             vs_pop : Long,
                                             bo_queue : Long,
                                             bo_pop : Long)
  extends PlanetSideGamePacket {
  type Packet = ZonePopulationUpdateMessage
  def opcode = GamePacketOpcode.ZonePopulationUpdateMessage
  def encode = ZonePopulationUpdateMessage.encode(this)
}

object ZonePopulationUpdateMessage extends Marshallable[ZonePopulationUpdateMessage] {
  implicit val codec : Codec[ZonePopulationUpdateMessage] = (
    ("continent_guid" | PlanetSideGUID.codec) ::
      ("zone_queue" | ulongL(32)) ::
      ("tr_queue" | ulongL(32)) :: ("tr_pop" | ulongL(32)) ::
      ("nc_queue" | ulongL(32)) :: ("nc_pop" | ulongL(32)) ::
      ("vs_queue" | ulongL(32)) :: ("vs_pop" | ulongL(32)) ::
      ("bo_queue" | ulongL(32)) :: ("bo_pop" | ulongL(32))
  ).as[ZonePopulationUpdateMessage]
}
