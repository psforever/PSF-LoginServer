// Copyright (c) 2022 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.Codec
import scodec.codecs._

object MemberAction extends Enumeration {
  type Type = Value

  val InitializeFriendList, AddFriend, RemoveFriend, UpdateFriend, InitializeIgnoreList, AddIgnoredPlayer,
  RemoveIgnoredPlayer = Value

  implicit val codec: Codec[MemberAction.Value] = PacketHelpers.createEnumerationCodec(this, uint(bits = 3))
}