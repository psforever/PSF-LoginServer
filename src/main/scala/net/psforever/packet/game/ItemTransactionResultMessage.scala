// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, TransactionType}
import scodec.Codec
import scodec.codecs._

/**
  * Dispatch to the client in response to an `ItemRequestMessage`, roughly after the request has been fulfilled.
  * This TCP-like "after" behavior is typically supported by pushing this packet at the end of the `MultiPacket` that fulfills the request.
  * @param terminal_guid the terminal used
  * @param transaction_type the type of transaction
  * @param success whether the transaction was a success
  * @param error an error code, if applicable;
  *              no error by default
  */
final case class ItemTransactionResultMessage(
    terminal_guid: PlanetSideGUID,
    transaction_type: TransactionType.Value,
    success: Boolean,
    error: Int = 0
) extends PlanetSideGamePacket {
  type Packet = ItemTransactionResultMessage
  def opcode = GamePacketOpcode.ItemTransactionResultMessage
  def encode = ItemTransactionResultMessage.encode(this)
}

object ItemTransactionResultMessage extends Marshallable[ItemTransactionResultMessage] {
  implicit val codec: Codec[ItemTransactionResultMessage] = (
    ("terminal_guid" | PlanetSideGUID.codec) ::
      ("transaction_type" | TransactionType.codec) ::
      ("success" | bool) ::
      ("error" | uint8L)
  ).as[ItemTransactionResultMessage]
}
