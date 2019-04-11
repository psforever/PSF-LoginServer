// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.SpawnGroup
import scodec.Codec
import scodec.codecs._

/**
  * na
  * @param unk1 when defined, na;
  *             non-zero when selecting the sanctuary option from a non-sanctuary continent deployment map
  * @param spawn_type the type of spawn point destination
  * @param unk3 na
  * @param unk4 na
  * @param zone_number when defined, the continent number
  */
final case class SpawnRequestMessage(unk1 : Int,
                                     spawn_type : SpawnGroup.Value,
                                     unk3 : Int,
                                     unk4 : Int,
                                     zone_number : Int)
  extends PlanetSideGamePacket {
  type Packet = SpawnRequestMessage
  def opcode = GamePacketOpcode.SpawnRequestMessage
  def encode = SpawnRequestMessage.encode(this)
}

object SpawnRequestMessage extends Marshallable[SpawnRequestMessage] {
  private val spawnGroupCodec = PacketHelpers.createLongEnumerationCodec(SpawnGroup, uint32L)

  implicit val codec : Codec[SpawnRequestMessage] = (
    ("unk1" | uint16L) ::
      ("spawn_type" | spawnGroupCodec) ::
      ("unk3" | uint16L) ::
      ("unk4" | uint16L) ::
      ("zone_number" | uintL(10))
    ).as[SpawnRequestMessage]
}
