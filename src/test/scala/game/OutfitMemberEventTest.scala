// Copyright (c) 2025 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package game

import net.psforever.packet._
import net.psforever.packet.game.packets.{OutfitMemberEvent, OutfitMemberEventAction}
import net.psforever.packet.game.packets.OutfitMemberEventAction.Update
import org.specs2.mutable._
import scodec.bits._

class OutfitMemberEventTest extends Specification {

  //val unk0_ABC: ByteVector =  hex"90 3518 4000 1a4e 4100 2 180 450078007000650072007400 8483 07e0 119d bfe0 70" // 0x90048640001030c28022404c0061007a00650072003100390038003200f43a45e00b4c604010
  val Lazer  = hex"90 0 4864 0001 030c 2802 24 0 4c0061007a00650072003100390038003200          f43a 45e0 0b4c 6040 10"
  val Lazer2 = hex"90 0 4864 0001 030c 2802 24 0 4c0061007a00650072003100390038003200          e6dc 25a0 153e 6040 10"
  val OpolE  = hex"90 0 4864 0003 aad6 280a 14 0 4f0070006f006c004500                          c9a1 80e0 0d03 2040 10"
  val Billy  = hex"90 0 4864 0003 a41a 280a 20 0 620069006c006c007900320035003600              935f 6000 186a b040 50"
  val Virus  = hex"90 0 4864 0002 1b64 4c02 28 0 5600690072007500730047006900760065007200      2f89 0080 0000 0000 10"
  val PvtPa  = hex"90 0 4864 0000 1e69 e80a 2c 0 500076007400500061006e00630061006b0065007300  705e a080 0a85 e060 10"
  val Night  = hex"90 0 4864 0002 4cf0 3802 28 0 4e006900670068007400770069006e0067003100      b8fb 9a40 0da6 ec80 50"

  val unk1   = hex"90 5 40542002 3f61e808 0"

  "decode Lazer" in {
    PacketCoding.decodePacket(Lazer).require mustEqual OutfitMemberEvent(
      outfit_id = 6418,
      member_id = 705344,
      action = Update(
        member_name = "Lazer1982",
        rank = 7,
        points = 3134113,
        last_online = 156506,
        joined = true,
        invited_by = None
      )
    )
  }

  "encode Lazer" in {
    val msg = OutfitMemberEvent(
      outfit_id = 6418,
      member_id = 705344,
      action = Update(
        member_name = "Lazer1982",
        rank = 7,
        points = 3134113,
        last_online = 156506,
        joined = true,
        invited_by = None
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual Lazer
  }

  "decode Lazer with the client-ignored trailing bits of the container" in {
    // the old captured sub-packets carry 4 trailing container-alignment bits;
    // decodePacket tolerates unconsumed trailing bits exactly like the client decoder
    val padded = Lazer ++ hex"0f"
    PacketCoding.decodePacket(padded).require mustEqual OutfitMemberEvent(
      outfit_id = 6418,
      member_id = 705344,
      action = Update(
        member_name = "Lazer1982",
        rank = 7,
        points = 3134113,
        last_online = 156506,
        joined = true,
        invited_by = None
      )
    )
  }

  "decode OpolE" in {
    PacketCoding.decodePacket(OpolE).require mustEqual OutfitMemberEvent(
      outfit_id = 6418,
      member_id = 42644970,
      action = Update(
        member_name = "OpolE",
        rank = 6,
        points = 461901,
        last_online = 137576,
        joined = true,
        invited_by = None
      )
    )
  }

  "encode OpolE" in {
    val msg = OutfitMemberEvent(
      outfit_id = 6418,
      member_id = 42644970,
      action = Update(
        member_name = "OpolE",
        rank = 6,
        points = 461901,
        last_online = 137576,
        joined = true,
        invited_by = None
      )
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual OpolE
  }

  "an offline-style record with joined = false carries the extended value" in {
    val msg = OutfitMemberEvent(
      outfit_id = 6418,
      member_id = 705344,
      action = Update(
        member_name = "Lazer1982",
        rank = 7,
        points = 3134113,
        last_online = 156506,
        joined = false,
        invited_by = Some(0xc0ffeeL)
      )
    )
    val encoded = PacketCoding.encodePacket(msg).require.toByteVector
    // the wire: the same record, plus the flag cleared and a trailing 32-bit value
    PacketCoding.decodePacket(encoded).require mustEqual msg.copy(
      action = Update(
        member_name = "Lazer1982",
        rank = 7,
        points = 3134113,
        last_online = 156506,
        joined = false,
        invited_by = Some(0xc0ffeeL)
      )
    )
  }

  "decode Unk1 kicked" in {
    PacketCoding.decodePacket(unk1).require mustEqual OutfitMemberEvent(
      outfit_id = 529744,
      member_id = 41605263,
      OutfitMemberEventAction.Kicked()
    )
  }

  "encode Unk1 kicked" in {
    val msg = OutfitMemberEvent(
      outfit_id = 529744,
      member_id = 41605263,
      OutfitMemberEventAction.Kicked()
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk1
  }
}
