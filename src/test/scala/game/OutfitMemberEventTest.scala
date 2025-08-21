// Copyright (c) 2025 PSForever
package game

import net.psforever.packet._

import net.psforever.packet.game._
import org.specs2.mutable._
import scodec.bits._

class OutfitMemberEventTest extends Specification {

  //val unk0_ABC: ByteVector =  hex"90 3518 4000 1a4e 4100 2 180 450078007000650072007400 8483 07e0 119d bfe0 70" // 0x90048640001030c28022404c0061007a00650072003100390038003200f43a45e00b4c604010
  val unk0_ABC_Lazer =  hex"90 0 4864 0001 030c 2802 24 0 4c0061007a00650072003100390038003200          f43a 45e0 0b4c 6040 10"

  val OpolE =           hex"90 0 4864 0003 aad6 280a 14 0 4f0070006f006c004500                          c9a1 80e0 0d03 2040 10"
  val Billy =           hex"90 0 4864 0003 a41a 280a 20 0 620069006c006c007900320035003600              935f 6000 186a b040 50"
  val Lazer =           hex"90 0 4864 0001 030c 2802 24 0 4c0061007a00650072003100390038003200          e6dc 25a0 153e 6040 10"
  val Virus =           hex"90 0 4864 0002 1b64 4c02 28 0 5600690072007500730047006900760065007200      2f89 0080 0000 0000 10"
  val PvtPa =           hex"90 0 4864 0000 1e69 e80a 2c 0 500076007400500061006e00630061006b0065007300  705e a080 0a85 e060 10"
  val Night =           hex"90 0 4864 0002 4cf0 3802 28 0 4e006900670068007400770069006e0067003100      b8fb 9a40 0da6 ec80 50"

  val Unk0 = hex"90 5 40542002 3f61e808 0"

  "decode Unk0 ABC" in {
    PacketCoding.decodePacket(unk0_ABC_Lazer).require match {
      case OutfitMemberEvent(action, outfit_id, member_id, member_name, rank, points, last_login, action2, padding) =>
        action       mustEqual 0
        outfit_id   mustEqual 6418L
        member_id   mustEqual 705344
        member_name mustEqual "Lazer1982"
        rank        mustEqual 7
        points      mustEqual 3134113
        last_login  mustEqual 156506
        action2     mustEqual 1
        padding     mustEqual ByteVector(0x0)
      case _ =>
        ko
    }
  }

  "encode Unk0 ABC" in {
    val msg = OutfitMemberEvent(
      action = 0,
      outfit_id = 6418L,
      member_id = 705344,
      member_name = "Lazer1982",
      rank = 7,
      points = 3134113,
      last_login = 156506,
      action2 = 1,
      ByteVector.empty
    )
    val pkt = PacketCoding.encodePacket(msg).require.toByteVector

    pkt mustEqual unk0_ABC_Lazer
  }
}
