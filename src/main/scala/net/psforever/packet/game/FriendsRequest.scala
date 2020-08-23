// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Manage the lists of other players whose names are retained by the given player.<br>
  * <br>
  * Players can be remembered by their names and added to a list of remembered names - the "friends list."
  * They can also be dropped from the list.
  * A list of "ignored" player names can also be retained.
  * Ignored players will have their comments stifled in the given player's chat window.
  * No name will be appended or removed from any list until the response to this packet is received.<br>
  * <br>
  * Actions that involve the "remove" functionality will locate the entered name in the local list before dispatching this packet.
  * A complaint will be logged to the event window if the name is not found.<br>
  * <br>
  * Actions:<br>
  * 0 - display friends list<br>
  * 1 - add player to friends list<br>
  * 2 - remove player from friends list<br>
  * 4 - display ignored player list<br>
  * 5 - add player to ignored player list<br>
  * 6 - remove player from ignored player list<br>
  * <br>
  * Exploration:<br>
  * Are action = 3 and action = 7 supposed to do anything?
  * @param action the purpose of this packet
  * @param friend the player name that was entered;
  *               blank in certain situations
  */
final case class FriendsRequest(action: Int, friend: String) extends PlanetSideGamePacket {
  type Packet = FriendsRequest
  def opcode = GamePacketOpcode.FriendsRequest
  def encode = FriendsRequest.encode(this)
}

object FriendsRequest extends Marshallable[FriendsRequest] {
  implicit val codec: Codec[FriendsRequest] = (
    ("action" | uintL(3)) ::
      ("friend" | PacketHelpers.encodedWideStringAligned(5))
  ).as[FriendsRequest]
}
