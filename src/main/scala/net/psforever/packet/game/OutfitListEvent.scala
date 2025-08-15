// Copyright (c) 2025 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.codecs._
import scodec.{Attempt, Codec}

// 98 5ec300000d01a020004000001 12056002e0053002e0053002e0055002e004400 845400680069006f009 85ee300001e2b 858000800000110041002e0027002e0041002e0027002e00 8448006900720075009 84003200005a540000060000011a0530065006300720065007400200043006800690065006600730085530069006c00610073009840a32000001953476fe0c00011c041007a0075007200650020005400770069006c006900670068007400874600720061006e0063006b006f009840c3200000d3a4c000c00000106030002e006f0085410074006c0061007300984183200011d9296000c0000011e0570061007200720069006f007200270073002000430072006500650064008653006500760061006b00690098442320001bf40e000080000013203100330033003700740068002000410072006d006f0072006500640020004400690076006900730069006f006e002d004b008548006f0073002d004b009844c320001b3d2c200060000012a03300330031007300740020004d0069006e006e00650073006f0074006100200054007200690062006500864d006100670069002d0045009846c3200009e206c00040000010c04100720065006100350031008942006c00610063

final case class OutfitListEvent(
    outfit_score: Long,
    unk1: Long,
    unk2: Long,
    unk3: Int,
    outfit_name: String,
    outfit_leader: String,
  ) extends PlanetSideGamePacket {
  type Packet = OutfitListEvent

  def opcode: Type = GamePacketOpcode.OutfitListEvent

  def encode: Attempt[BitVector] = OutfitListEvent.encode(this)
}
object OutfitListEvent extends Marshallable[OutfitListEvent] {
  implicit val codec: Codec[OutfitListEvent] = (
    ("outfit_score" | uint32) ::
      ("unk1" | uint32L) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint(3)) ::
      ("outfit_name" | PacketHelpers.encodedWideStringAligned(5)) ::
      ("outfit_leader" | PacketHelpers.encodedWideString)
    ).as[OutfitListEvent]
}
