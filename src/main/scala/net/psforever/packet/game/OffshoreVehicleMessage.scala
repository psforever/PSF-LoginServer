// Copyright (c) 2022 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched from the server to indicate that the player is traveling too far from the primary battlefield
  * and needs to return.
  * At first the intention is to warn.
  * After the warning, the intent is to dispose.<br>
  * Do not dispatch this packet if the player is not seated in a vehicle or else the client will crash.<br>
  * <br>
  * Messages follow:<br>
  * 1) `WARNING: Power Link is weakening.  Proceed back to the continent.`<br>
  * 2) `DANGER: Power Link is Dangerously Low. Turn back to the continent immediately!`<br>
  * 3) `Power Link lost.`<br>
  * Upon reception of #3, the player will lose control of their vehicle and it may explode depending on the vehicle.
  * The "Power Link" that is mentioned is a hand-wave.
  * @param player_guid na
  * @param vehicle_guid na
  * @param msg the number indexes of the message to be displayed by the client
  */
final case class OffshoreVehicleMessage(
                                         player_guid: PlanetSideGUID,
                                         vehicle_guid: PlanetSideGUID,
                                         msg: Int
                                       ) extends PlanetSideGamePacket {
  type Packet = OffshoreVehicleMessage
  def opcode = GamePacketOpcode.OffshoreVehicleMessage
  def encode = OffshoreVehicleMessage.encode(this)
}

object OffshoreVehicleMessage extends Marshallable[OffshoreVehicleMessage] {
  implicit val codec : Codec[OffshoreVehicleMessage] = (
    ("player_guid" | PlanetSideGUID.codec) ::
    ("vehicle_guid" | PlanetSideGUID.codec) ::
    ("msg" | uint2L)
  ).as[OffshoreVehicleMessage]
}
