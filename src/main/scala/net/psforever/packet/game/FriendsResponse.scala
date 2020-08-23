// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

object FriendAction extends Enumeration {
  type Type = Value

  val InitializeFriendList, AddFriend, RemoveFriend, UpdateFriend, InitializeIgnoreList, AddIgnoredPlayer,
      RemoveIgnoredPlayer = Value

  implicit val codec: Codec[FriendAction.Value] = PacketHelpers.createEnumerationCodec(this, uint(3))
}

/**
  * An entry in the list of players known to and tracked by this player.
  * They're called "friends" even though they can be used for a list of ignored players as well.
  * @param name the name of the player
  * @param online the player's current state of activity; defaults to `false`, or offline
  */
final case class Friend(name: String, online: Boolean = false)

/**
  * Manage the lists of other players whose names are retained by the given player.<br>
  * <br>
  * Friends can be remembered and their current playing status can be reported.
  * Ignored players will have their comments stifled in the given player's chat window.
  * This does not handle outfit member lists.<br>
  * <br>
  * Actions:<br>
  * 0 - initialize friends list (no logging)<br>
  * 1 - add entry to friends list<br>
  * 2 - remove entry from friends list<br>
  * 3 - update status of player in friends list;
  *     if player is not listed, he is not added<br>
  * 4 - initialize ignored players list (no logging)<br>
  * 5 - add entry to ignored players list<br>
  * 6 - remove entry from ignored players list<br>
  * @param action the purpose of the entry(s) in this packet
  * @param unk1 na; always 0?
  * @param unk2 na; always `true`?
  * @param unk3 na; always `true`?
  * @param friends a list of `Friend`s
  */
final case class FriendsResponse(
    action: FriendAction.Value,
    unk1: Int,
    unk2: Boolean,
    unk3: Boolean,
    friends: List[Friend] = Nil
) extends PlanetSideGamePacket {
  type Packet = FriendsResponse

  def opcode: Type = GamePacketOpcode.FriendsResponse

  def encode: Attempt[BitVector] = FriendsResponse.encode(this)
}

object Friend extends Marshallable[Friend] {
  implicit val codec: Codec[Friend] = (
    ("name" | PacketHelpers.encodedWideStringAligned(3)) ::
      ("online" | bool)
  ).as[Friend]

  /**
    * This codec is used for the "`List` of other `Friends`."
    * Initial byte-alignment creates padding differences which requires a second `Codec`.
    */
  implicit val codec_list: Codec[Friend] = (
    ("name" | PacketHelpers.encodedWideStringAligned(7)) ::
      ("online" | bool)
  ).as[Friend]
}

object FriendsResponse extends Marshallable[FriendsResponse] {
  implicit val codec: Codec[FriendsResponse] = (
    ("action" | FriendAction.codec) ::
      ("unk1" | uint4L) ::
      ("unk2" | bool) ::
      ("unk3" | bool) ::
      (("number_of_friends" | uint4L) >>:~ { len =>
      conditional(len > 0, "friend" | Friend.codec) ::
        ("friends" | PacketHelpers.listOfNSized(len - 1, Friend.codec_list))
    })
  ).xmap[FriendsResponse](
    {
      case act :: u1 :: u2 :: u3 :: _ :: friend1 :: friends :: HNil =>
        val friendList: List[Friend] = if (friend1.isDefined) { friend1.get +: friends }
        else { friends }
        FriendsResponse(act, u1, u2, u3, friendList)
    },
    {
      case FriendsResponse(act, u1, u2, u3, friends) =>
        var friend1: Option[Friend]  = None
        var friendList: List[Friend] = Nil
        if (friends.nonEmpty) {
          friend1 = Some(friends.head)
          friendList = friends.drop(1)
        }
        act :: u1 :: u2 :: u3 :: friends.size :: friend1 :: friendList :: HNil
    }
  )
}
