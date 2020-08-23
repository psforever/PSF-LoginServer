// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the client when driving a vehicle in the sphere of influence of an allied base
  * that is an amp station facility or that possesses the lattice-connected benefit of an amp station.
  * The vehicle that is being driven will not have perfect fully-charged shields at the time.
  * @param vehicle_guid the vehicle whose shield is being charged
  */
final case class FacilityBenefitShieldChargeRequestMessage(vehicle_guid: PlanetSideGUID) extends PlanetSideGamePacket {
  type Packet = FacilityBenefitShieldChargeRequestMessage
  def opcode = GamePacketOpcode.FacilityBenefitShieldChargeRequestMessage
  def encode = FacilityBenefitShieldChargeRequestMessage.encode(this)
}

object FacilityBenefitShieldChargeRequestMessage extends Marshallable[FacilityBenefitShieldChargeRequestMessage] {
  implicit val codec: Codec[FacilityBenefitShieldChargeRequestMessage] =
    ("vehicle_guid" | PlanetSideGUID.codec).as[FacilityBenefitShieldChargeRequestMessage]
}
