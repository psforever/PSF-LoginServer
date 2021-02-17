// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

final case class PadAndShuttlePair(
                                    pad: PlanetSideGUID,
                                    shuttle: PlanetSideGUID,
                                    unk3: Int
                                  )

final case class OrbitalShuttleTimeMsg(
                                        unk1: Int,
                                        unk2: Int,
                                        unk3: Int,
                                        time: Long, //in ms
                                        unk5: Long,
                                        unk6: Boolean,
                                        unk7: Long,
                                        pairs: List[PadAndShuttlePair]
                                      )
  extends PlanetSideGamePacket {
  type Packet = ObjectHeldMessage
  def opcode = GamePacketOpcode.OrbitalShuttleTimeMsg
  def encode = OrbitalShuttleTimeMsg.encode(this)
}

object OrbitalShuttleTimeMsg extends Marshallable[OrbitalShuttleTimeMsg] {
  val padShuttlePair_codec: Codec[PadAndShuttlePair] = (
    ("pad" | PlanetSideGUID.codec) ::
      ("shuttle" | PlanetSideGUID.codec) ::
      ("unk3" | uint(bits = 6))
    ).as[PadAndShuttlePair]

  implicit val codec: Codec[OrbitalShuttleTimeMsg] = (
    ("unk1" | uint(bits = 3)) ::
      ("unk2" | uint(bits = 3)) ::
      ("unk3" | uint(bits = 3)) ::
      ("time" | uint32L) ::
      ("unk5" | uint32L) ::
      ("unk6" | bool) ::
      ("unk7" | uint32L) ::
      ("pairs" | PacketHelpers.listOfNSized(size = 3, padShuttlePair_codec)) //always three?
    ).as[OrbitalShuttleTimeMsg]
}
