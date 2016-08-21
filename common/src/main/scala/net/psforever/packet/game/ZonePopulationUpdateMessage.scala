// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Report the raw numerical population for a zone (continent).<br>
  * <br>
  * Populations will be displayed in the Incentives window for that zone.
  * Populations will contribute to the Global Population window and the Incentives window for the server.
  * The Black OPs population does not show up in the Incentives window for the zone but will be indirectly represented in the other two windows.
  * This packet also shifts the flavor text for that zone.
  *
  * @param continent_guid identifies the zone (continent)
  * @param emblem the emblem on the zone in the Interstellar View:
  *               00 00 - zone shows as locked;
  *               9E 01 - the lock is not shown
  * @param unk10 unknown
  * @param unk11 unknown
  * @param tr_queue zone Incentive window text:
  *                 00 00 - "Empire Population Lock;"
  *                 8A 00 - normal queue;
  *                 F4 01 - similar to 8A 00, occasionally used for Sanctuaries and VR areas
  * @param unk20 unknown
  * @param unk21 unknown
  * @param tr_pop the TR population in this zone
  * @param nc_queue zone Incentive window text:
  *                 00 00 - "Empire Population Lock;"
  *                 8A 00 - normal queue;
  *                 F4 01 - similar to 8A 00, occasionally used for Sanctuaries and VR areas
  * @param unk30 unknown
  * @param unk31 unknown
  * @param nc_pop the NC population in this zone
  * @param vs_queue zone Incentive window text:
  *                 00 00 - "Empire Population Lock;"
  *                 8A 00 - normal queue;
  *                 F4 01 - similar to 8A 00, occasionally used for Sanctuaries and VR areas
  * @param unk40 unknown
  * @param unk41 unknown
  * @param vs_pop the NC population in this zone
  * @param bo_queue zone Incentive window text:
  *                 00 00 - "Empire Population Lock;"
  *                 8A 00 - normal queue;
  *                 F4 01 - similar to 8A 00, occasionally used for Sanctuaries and VR areas
  * @param unk50 unknown
  * @param unk51 unknown
  * @param bo_pop the Black OPs population in this zone
  */
final case class ZonePopulationUpdateMessage(continent_guid : PlanetSideGUID,
                                             emblem : Int,
                                             unk10 : Int,
                                             unk11 : Int,
                                             tr_queue : Int, // TR region
                                             unk20 : Int,
                                             unk21 : Int,
                                             tr_pop : Long,
                                             nc_queue : Int, // NC region
                                             unk30 : Int,
                                             unk31 : Int,
                                             nc_pop : Long,
                                             vs_queue : Int, // VS region
                                             unk40 : Int,
                                             unk41 : Int,
                                             vs_pop : Long,
                                             bo_queue : Int, // Black OPs region
                                             unk50 : Int,
                                             unk51 : Int,
                                             bo_pop : Long)
  extends PlanetSideGamePacket {
  type Packet = ZonePopulationUpdateMessage
  def opcode = GamePacketOpcode.ZonePopulationUpdateMessage
  def encode = ZonePopulationUpdateMessage.encode(this)
}

object ZonePopulationUpdateMessage extends Marshallable[ZonePopulationUpdateMessage] {
  implicit val codec : Codec[ZonePopulationUpdateMessage] = (
    ("continent_guid" | PlanetSideGUID.codec) :: ("emblem" | uint16L) ::
      ("unk10" | uint8L) :: ("unk11" | uint8L) ::
      ("tr_queue" | uint16L) :: ("unk21" | uint8L) :: ("unk22" | uint8L) :: ("tr_pop" | ulongL(32)) ::
      ("nc_queue" | uint16L) :: ("unk31" | uint8L) :: ("unk32" | uint8L) :: ("nc_pop" | ulongL(32)) ::
      ("vs_queue" | uint16L) :: ("unk41" | uint8L) :: ("unk42" | uint8L) :: ("vs_pop" | ulongL(32)) ::
      ("bo_queue" | uint16L) :: ("unk51" | uint8L) :: ("unk52" | uint8L) :: ("bo_pop" | ulongL(32))
    ).as[ZonePopulationUpdateMessage]
}
