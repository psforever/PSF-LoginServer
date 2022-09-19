// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.MemberAction
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

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
  * This does not handle outfit member lists.
  * @param action the purpose of the entry(s) in this packet
  * @param unk1 na;
  *             always 0?
  * @param first_entry this is the first packet for this action
  * @param last_entry this is the last packet for this action
  * @param friends a list of `Friend`s
  */
final case class FriendsResponse(
                                  action: MemberAction.Value,
                                  unk1: Int,
                                  first_entry: Boolean,
                                  last_entry: Boolean,
                                  friends: List[Friend]
) extends PlanetSideGamePacket {
  type Packet = FriendsResponse

  def opcode: Type = GamePacketOpcode.FriendsResponse

  def encode: Attempt[BitVector] = FriendsResponse.encode(this)
}

object Friend extends Marshallable[Friend] {
  implicit val codec: Codec[Friend] = (
    ("name" | PacketHelpers.encodedWideStringAligned(adjustment = 3)) ::
      ("online" | bool)
  ).as[Friend]

  /**
    * This codec is used for the "`List` of other `Friends`."
    * Initial byte-alignment creates padding differences which requires a second `Codec`.
    */
  implicit val codec_list: Codec[Friend] = (
    ("name" | PacketHelpers.encodedWideStringAligned(adjustment = 7)) ::
      ("online" | bool)
  ).as[Friend]
}

object FriendsResponse extends Marshallable[FriendsResponse] {
  def apply(action: MemberAction.Value, friend: Friend): FriendsResponse = {
    FriendsResponse(action, unk1=0, first_entry=true, last_entry=true, List(friend))
  }

  /**
    * Take a list of members and construct appropriate packets by which they can be dispatched to the client.
    * Attention needs to be paid to the number of entries in a single packet,
    * and where the produced packets begin and end.
    * @param action the purpose of the entry(s) in this packet
    * @param friends a list of `Friend`s
    * @return a list of `FriendResponse` packets
    */
  def packetSequence(action: MemberAction.Value, friends: List[Friend]): List[FriendsResponse] = {
    val lists = friends.grouped(15)
    val size = lists.size
    if (size <= 1) {
      List(FriendsResponse(action, unk1=0, first_entry=true, last_entry=true, friends))
    } else {
      val size1 = size - 1
      val first = lists.take(1)
      val rest = lists.slice(1, size1)
      val last = lists.drop(size1)
      List(FriendsResponse(action, unk1=0, first_entry=true, last_entry=false, first.next())) ++
        rest.map { FriendsResponse(action, unk1=0, first_entry=false, last_entry=false, _)} ++
        List(FriendsResponse(action, unk1=0, first_entry=false, last_entry=true, last.next()))
    }
  }

  implicit val codec: Codec[FriendsResponse] = (
    ("action" | MemberAction.codec) ::
      ("unk1" | uint4L) ::
      ("first_entry" | bool) ::
      ("last_entry" | bool) ::
      (("number_of_friends" | uint4L) >>:~ { len =>
      conditional(len > 0, codec = "friend" | Friend.codec) ::
        ("friends" | PacketHelpers.listOfNSized(len - 1, Friend.codec_list))
    })
  ).xmap[FriendsResponse](
    {
      case act :: u1 :: first :: last :: _ :: friend1 :: friends :: HNil =>
        val friendList: List[Friend] = if (friend1.isDefined) { friend1.get +: friends }
        else { friends }
        FriendsResponse(act, u1, first, last, friendList)
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
