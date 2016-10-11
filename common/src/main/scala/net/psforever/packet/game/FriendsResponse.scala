// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class Friend(name : String = "",
                        online : Boolean = false)

final case class FriendsResponse(unk1 : Int,
                                 unk2 : Int,
                                 unk3 : Boolean,
                                 unk4 : Boolean,
                                 number_of_friends : Int,
                                 friend : Friend,
                                 friends : List[Friend] = Nil)
  extends PlanetSideGamePacket {
  type Packet = FriendsResponse
  def opcode = GamePacketOpcode.FriendsResponse
  def encode = FriendsReponse.encode(this)
}

object Friend extends Marshallable[Friend] {
  implicit val codec : Codec[Friend] = (
    ("name" | PacketHelpers.encodedWideStringAligned(3)) ::
      ("online" | bool)
    ).as[Friend]

  implicit val codec_list : Codec[Friend] = (
    ("name" | PacketHelpers.encodedWideStringAligned(7)) ::
      ("online" | bool)
    ).as[Friend]
}

object FriendsReponse extends Marshallable[FriendsResponse] {
  implicit val codec : Codec[FriendsResponse] = (
    ("unk1" | uintL(3)) ::
      ("unk2" | uintL(4)) ::
      ("unk3" | bool) ::
      ("unk4" | bool) ::
      (("number_of_friends" | uintL(4)) >>:~ { len =>
        ("friend" | Friend.codec) ::
        ("friends" | PacketHelpers.sizedList(len-1, Friend.codec_list))
      })
    ).as[FriendsResponse]
}
