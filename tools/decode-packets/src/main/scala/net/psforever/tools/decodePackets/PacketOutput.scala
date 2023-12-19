// Copyright (c) 2023 PSForever
package net.psforever.tools.decodePackets

trait PacketOutput {
  def header: Option[String]
  def text: String
}

final case class DecodedPacket(header: Option[String], text: String) extends PacketOutput

final case class DecodeError(header: Option[String], text: String) extends PacketOutput
