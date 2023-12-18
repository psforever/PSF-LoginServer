// Copyright (c) 2023 PSForever
package net.psforever.tools.decodePackets

trait PacketOutput {
  def header: String
  def text: String
}

final case class DecodedPacket(header: String, text: String) extends PacketOutput

final case class DecodeError(header: String, text: String) extends PacketOutput
