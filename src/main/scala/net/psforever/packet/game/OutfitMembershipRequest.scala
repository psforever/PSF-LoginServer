// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

final case class OutfitMembershipRequest(
    request_type: OutfitMembershipRequest.RequestType.Type,
    avatar_guid: PlanetSideGUID,
    unk1: Int,
    unk2: String,
    unk3: Int,
    unk4: Boolean,
    outfit_name: String
) extends PlanetSideGamePacket {
  type Packet = OutfitMembershipRequest

  def opcode = GamePacketOpcode.OutfitMembershipRequest

  def encode = OutfitMembershipRequest.encode(this)
}

object OutfitMembershipRequest extends Marshallable[OutfitMembershipRequest] {

  object RequestType extends Enumeration {
    type Type = Value

    val Create = Value(0x0)
    val Form   = Value(0x1)
    val Accept = Value(0x3)
    val Reject = Value(0x4)
    val Cancel = Value(0x5)

    implicit val codec: Codec[Type] = PacketHelpers.createEnumerationCodec(this, uintL(3))
  }

  implicit val codec: Codec[OutfitMembershipRequest] = (
    ("request_type" | RequestType.codec) ::
      ("avatar_guid" | PlanetSideGUID.codec) :: // as in DB avatar table
      ("unk1" | uint16L) :: // avatar2_guid / invited player ?
      ("unk2" | PacketHelpers.encodedWideString) :: // could be string
      ("unk3" | uint4L) ::
      ("unk4" | bool) ::
      ("outfit_name" | PacketHelpers.encodedWideString)
  ).as[OutfitMembershipRequest]
}

/*

/outfitcreate

0 0200 000 1000 83 410042004300 -- /outfitcreate ABC -- from AA       - TR BR 24 CR 0
0 0200 000 1000 83 410042004300 -- /outfitcreate ABC -- from AA       - TR BR 24 CR 0
0 1000 000 1000 83 410042004300 -- /outfitcreate ABC -- from TTEESSTT - TR BR 24 CR 0

0 0a00 000 1000 83 310032003300 -- /outfitcreate 123 -- from BBBB - TR BR 1 CR 0
0 0a00 000 1000 83 310032003300
0 0a00 000 1000 83 310032003300
0 0a00 000 1000 83 310032003300
0 0a00 000 1000 83 410042004300 -- ABC

0 0400 000 1000 83 580059005a00 -- /outfitcreate XYZ -- from BB   - VS BR 24 CR 0

0 1000 000 1000 84 3200320032003200 -- /outfitcreate 2222 -- from TTEESSTT   - TR BR 24 CR 0

/outfitform

20 2000 00 1000 83 610062006300 -- /outfitform abc -- from AA       - TR BR 24 CR 0
21 0000 00 1000 81 3100         -- /outfitform 1   -- from TTEESSTT - TR BR 24 CR 0

/outfitinvite

3 // guess

/outfitkick

4 // guess

/outfitaccept

60 2000 00 1000 -- from AA  - TR BR 24 CR 0
60 4000 00 1000 -- from BB  - VS BR 24 CR 0

/outfitreject

80 2000 00 1000 -- from AA  - TR BR 24 CR 0
80 4000 00 1000 -- from BB  - VS BR 24 CR 0
80 6000 00 1000 -- from BBB - NC BR 1  CR 0

/outfitcancel

a0 2000 00 0000 0000 1000 -- from AA  - TR BR 24 CR 0
a0 4000 00 0000 0000 1000 -- from BB  - VS BR 24 CR 0
a0 6000 00 0000 0000 1000 -- from BBB - NC BR 1  CR 0

a0 2000 00 0000 0000 1060 610064006200 -- /outfitcancel abc      -- from AA - TR BR 24 CR 0
a0 2000 00 0000 0000 1080 3100320033003400 -- /outfitcancel 1234 -- from AA - TR BR 24 CR 0

 */

