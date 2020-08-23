// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched when the player wants to go to the training zones.
  * When a player enters the virtual reality hallways behind sanctuary spawn rooms and walks to the base of the ramp, he is presented with a prompt.
  * From the prompt, the player accepts either "Shooting Range" or "Vehicle Training Area."<br>
  * <br>
  * Both sets of training zones utilize the same map for their type - `map14` for shooting and `map15` for vehicles.
  * The factions are kept separate so there are actually six separated zones - two each - to accommodate the three factions.
  * There is no global map notation, i.e., `map##`, for going to a faction-instance training zone map.
  * The zone modifier is used in conjunction with the `LoadMapMessage` packet to determine the faction-instance of the training map.<br>
  * <br>
  * Players are sent to their respective empire's area by default.
  * A TR player utilizing the virtual reality hallway in the NC sanctuary and will still only be offered the TR virtual reality areas.
  * Black OPs do not have normal access to virtual reality areas.<br>
  * <br>
  * Zone:<br>
  * 17 - `11` - TR Shooting Range<br>
  * 18 - `12` - NC Shooting Range<br>
  * 19 - `13` - VS Shooting Range<br>
  * 20 - `14` - TR Vehicle Training Area<br>
  * 21 - `15` - NC Vehicle Training Area<br>
  * 22 - `16` - VS Vehicle Training Area
  * @param zone the virtual reality zone to send the player
  * @param unk na; always 0?
  */
final case class TrainingZoneMessage(zone: PlanetSideGUID, unk: Int = 0) extends PlanetSideGamePacket {
  type Packet = TrainingZoneMessage
  def opcode = GamePacketOpcode.TrainingZoneMessage
  def encode = TrainingZoneMessage.encode(this)
}

object TrainingZoneMessage extends Marshallable[TrainingZoneMessage] {
  implicit val codec: Codec[TrainingZoneMessage] = (
    ("zone" | PlanetSideGUID.codec) ::
      ("unk" | uint16L)
  ).as[TrainingZoneMessage]
}
