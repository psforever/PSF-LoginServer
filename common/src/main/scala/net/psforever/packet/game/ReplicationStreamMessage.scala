// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class SquadHeader(leader : String,
                             name : String,
                             continent_guid : PlanetSideGUID,
                             unk : Int,
                             size : Int,
                             capacity : Int = 10)

final case class SquadListing(index : Int = 255,
                              listing : Option[SquadHeader] = None)

final case class ReplicationStreamMessage(unk : Int,
                                          entries : Vector[SquadListing])
  extends PlanetSideGamePacket {
  type Packet = ReplicationStreamMessage
  def opcode = GamePacketOpcode.ReplicationStreamMessage
  def encode = ReplicationStreamMessage.encode(this)
}

object SquadHeader extends Marshallable[SquadHeader] {
  implicit val codec : Codec[SquadHeader] = (
    ("leader" | PacketHelpers.encodedWideString) ::
      ("name" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      ("unk" | uint16L) ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).as[SquadHeader]
}

object SquadListing extends Marshallable[SquadListing] {
  implicit val codec : Codec[SquadListing] = (
    ("index" | uint8L) >>:~ { index =>
      conditional(index < 255, "listing" | SquadHeader.codec) :: ignore(0)
    }).as[SquadListing]
}

object ReplicationStreamMessage extends Marshallable[ReplicationStreamMessage] {
  implicit val codec : Codec[ReplicationStreamMessage] = (
    ("unk" | uintL(7)) ::
      ("entries" | vector(SquadListing.codec))
    ).as[ReplicationStreamMessage]
}
