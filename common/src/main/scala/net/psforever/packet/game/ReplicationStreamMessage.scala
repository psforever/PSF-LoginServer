// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

final case class SquadHeader(unk1 : Int,
                             unk2 : Int,
                             squad_guid : Int,
                             unk3 : Boolean,
                             unk4 : Boolean,
                             leader : String,
                             name : String,
                             continent_guid : PlanetSideGUID,
                             unk5 : Int,
                             size : Int,
                             capacity : Int = 10)

final case class SquadListing(index : Int = 255,
                              listing : Option[SquadHeader] = None,
                              na : Option[Unit] = None)

final case class ReplicationStreamMessage(unk : Int,
                                          entries : Vector[SquadListing] = Vector.empty)
  extends PlanetSideGamePacket {
  type Packet = ReplicationStreamMessage
  def opcode = GamePacketOpcode.ReplicationStreamMessage
  def encode = ReplicationStreamMessage.encode(this)
}

object SquadHeader extends Marshallable[SquadHeader] {
  implicit val codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) ::
      ("unk2" | uintL(3)) ::
      ("squad_guid" | uintL(12)) ::
      ("unk3" | bool) ::
      ("unk4" | bool) ::
      ("leader" | PacketHelpers.encodedWideString) ::
      ("name" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      ("unk5" | uint16L) ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).as[SquadHeader]

  implicit val alt_codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) ::
      ("unk2" | uintL(3)) ::
      ("squad_guid" | uintL(12)) ::
      ("unk3" | bool) ::
      ("unk4" | bool) ::
      ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
      ("name" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      ("unk5" | uint16L) ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).as[SquadHeader]
}

object SquadListing extends Marshallable[SquadListing] {
  implicit val codec : Codec[SquadListing] = (
    ("index" | uint8L) >>:~ { index =>
      conditional(index < 255,
        newcodecs.binary_choice(index == 0,
          "listing" | SquadHeader.codec,
          "listing" | SquadHeader.alt_codec)
      ) >>:~ { listing =>
        conditional(listing.isEmpty, choice(ignore(1), ignore(0))).hlist
      }
    }).as[SquadListing]
}

object ReplicationStreamMessage extends Marshallable[ReplicationStreamMessage] {
  implicit val codec : Codec[ReplicationStreamMessage] = (
    ("unk" | uintL(7)) ::
      ("entries" | vector(SquadListing.codec))
    ).as[ReplicationStreamMessage]
}
