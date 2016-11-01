// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

//this packet is limited mainly by byte-size

//continents are stored in this packet as 32-bit numbers instead of 16-bit; after the normal 16 bits, two bytes can be ignored

final case class SquadInfo(leader : String,
                           task : String,
                           continent_guid : PlanetSideGUID,
                           size : Int,
                           capacity : Int)

final case class SquadHeader(unk1 : Int,
                             unk2 : Boolean,
                             squad_guid : PlanetSideGUID,
                             info : SquadInfo)

final case class SquadListing(index : Int = 255,
                              listing : Option[SquadHeader] = None,
                              na : Option[Unit] = None)

final case class ReplicationStreamMessage(action : Int,
                                          unk : Int,
                                          entries : Vector[SquadListing] = Vector.empty)
  extends PlanetSideGamePacket {
  type Packet = ReplicationStreamMessage
  def opcode = GamePacketOpcode.ReplicationStreamMessage
  def encode = ReplicationStreamMessage.encode(this)
}

object SquadInfo extends Marshallable[SquadInfo] {
  implicit val codec : Codec[SquadInfo] = (
    ("leader" | PacketHelpers.encodedWideString) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      ignore(16) ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).as[SquadInfo]

  implicit val alt_codec : Codec[SquadInfo] = (
    ("leader" | PacketHelpers.encodedWideStringAligned(7)) ::
      ("task" | PacketHelpers.encodedWideString) ::
      ("continent_guid" | PlanetSideGUID.codec) ::
      ignore(16) ::
      ("size" | uint4L) ::
      ("capacity" | uint4L)
    ).as[SquadInfo]
}

object SquadHeader extends Marshallable[SquadHeader] {
  implicit val codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) ::
      ("unk2" | bool) ::
      ("squad_guid" | PlanetSideGUID.codec) ::
      ("info" | SquadInfo.codec)
    ).as[SquadHeader]

  implicit val alt_codec : Codec[SquadHeader] = (
    ("unk1" | uint8L) ::
      ("unk2" | bool) ::
      ("squad_guid" | PlanetSideGUID.codec) ::
      ("info" | SquadInfo.alt_codec)
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
    ("action" | uintL(3)) ::
      ("unk" | uint4L) ::
      ("entries" | vector(SquadListing.codec))
    ).as[ReplicationStreamMessage]
}
