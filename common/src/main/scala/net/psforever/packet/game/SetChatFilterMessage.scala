// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * An `Enumeration` of the valid chat channels.
  */
object ChatChannel extends Enumeration {
  type Type = Value

  val Unknown, Tells, Local, Squad, Outfit, Command, Platoon, Broadcast, SquadLeader = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint(7))
}

/**
  * Which comm. channels are allowed to display in the main chat window.
  * The server sends a `SetChatFilterMessage` and the client responds with the same during login.<br>
  * <br>
  * Nine channels exist.
  * Their values can be modified by radio buttons found under the current chat window's "Options" pane.
  * Each time the client updates the channel permissions, it sends this packet to the server nine times.
  * The packet starts with the previous channel filter states and then updates each channel sequentially.<br>
  * <br>
  * The `send_channel` and the `channel_filter` values are in the following order:<br>
  * Unknown, Tells, Local, Squad, Outfit, Command, Platoon, Broadcast, Squad Leader<br>
  * The first channel is unlisted.
  * @param send_channel automatically select the fully qualified channel to which the user sends messages
  * @param origin where this packet was dispatched;
  *               `true`, from the server; `false`, from the client
  * @param whitelist each channel permitted to post its messages;
  *                  when evaluated from a packet, always in original order
  */
final case class SetChatFilterMessage(
    send_channel: ChatChannel.Value,
    origin: Boolean,
    whitelist: List[ChatChannel.Value]
) extends PlanetSideGamePacket {
  type Packet = SetChatFilterMessage
  def opcode = GamePacketOpcode.SetChatFilterMessage
  def encode = SetChatFilterMessage.encode(this)
}

object SetChatFilterMessage extends Marshallable[SetChatFilterMessage] {

  /**
    * Transform a `List` of `Boolean` values into a `List` of `ChatChannel` values.
    * @param filters the boolean values representing ordered channel filters
    * @return the names of the channels permitted
    */
  private def stateArrayToChannelFilters(filters: List[Boolean]): List[ChatChannel.Value] = {
    (0 until 9)
      .filter(channel => { filters(channel) })
      .map(channel => ChatChannel(channel))
      .toList
  }

  /**
    * Transform a `List` of `ChatChannel` values into a `List` of `Boolean` values.
    * @param filters the names of the channels permitted
    * @return the boolean values representing ordered channel filters
    */
  private def channelFiltersToStateArray(filters: List[ChatChannel.Value]): List[Boolean] = {
    import scala.collection.mutable.ListBuffer
    val list = ListBuffer.fill(9)(false)
    filters.foreach(channel => { list(channel.id) = true })
    list.toList
  }

  implicit val codec: Codec[SetChatFilterMessage] = (
    ("send_channel" | ChatChannel.codec) ::
      ("origin" | bool) ::
      ("whitelist" | PacketHelpers.listOfNSized(9, bool))
  ).exmap[SetChatFilterMessage](
    {
      case a :: b :: c :: HNil =>
        Attempt.Successful(SetChatFilterMessage(a, b, stateArrayToChannelFilters(c)))
    },
    {
      case SetChatFilterMessage(a, b, c) =>
        Attempt.Successful(a :: b :: channelFiltersToStateArray(c) :: HNil)
    }
  )
}
