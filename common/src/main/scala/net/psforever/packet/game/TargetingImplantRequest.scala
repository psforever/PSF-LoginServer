// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * An entry regarding a specific target.
  * @param target_guid the target
  * @param unk na
  */
final case class TargetRequest(target_guid : PlanetSideGUID,
                               unk : Boolean)

/**
  * Dispatched by the client when the advanced targeting implant activates to collect status information from the server.<br>
  * <br>
  * This packet is answered by a `TargetingInfoMessage` with `List` entries of thed corresponding UIDs.
  * @param target_list a `List` of targets
  */
final case class TargetingImplantRequest(target_list : List[TargetRequest])
  extends PlanetSideGamePacket {
  type Packet = TargetingImplantRequest
  def opcode = GamePacketOpcode.TargetingImplantRequest
  def encode = TargetingImplantRequest.encode(this)
}

object TargetingImplantRequest extends Marshallable[TargetingImplantRequest] {
  private val request_codec : Codec[TargetRequest] = (
    ("target_guid" | PlanetSideGUID.codec) ::
      ("unk" | bool)
  ).as[TargetRequest]

  implicit val codec : Codec[TargetingImplantRequest] = ("target_list" | listOfN(intL(6), request_codec)).as[TargetingImplantRequest]
}
