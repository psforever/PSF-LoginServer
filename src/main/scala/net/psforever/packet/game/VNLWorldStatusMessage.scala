// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import java.net.{InetAddress, InetSocketAddress}

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideEmpire
import net.psforever.newcodecs.newcodecs._
import scodec._
import scodec.bits._
import scodec.codecs._
import shapeless._
import enumeratum.values.{IntEnum, IntEnumEntry}

object WorldStatus extends Enumeration {
  type Type = Value
  val Up, Down, Locked, Full = Value
}

sealed abstract class ServerType(val value: Int, val name: String) extends IntEnumEntry

object ServerType extends IntEnum[ServerType] {
  case object Development    extends ServerType(1, "development")
  case object Beta           extends ServerType(2, "beta")
  case object Released       extends ServerType(3, "released")
  case object ReleasedGemini extends ServerType(4, "released_gemini")

  val values: IndexedSeq[ServerType]    = findValues
  implicit val codec: Codec[ServerType] = PacketHelpers.createIntEnumCodec(this, uint8L)
}

// This MUST be an IP address. The client DOES NOT do name resolution properly
final case class WorldConnectionInfo(address: InetSocketAddress)

final case class WorldInformation(
    name: String,
    status: WorldStatus.Value,
    serverType: ServerType,
    connections: Vector[WorldConnectionInfo],
    empireNeed: PlanetSideEmpire.Value
)

final case class VNLWorldStatusMessage(welcomeMessage: String, worlds: Vector[WorldInformation])
    extends PlanetSideGamePacket {
  type Packet = VNLWorldStatusMessage
  def opcode = GamePacketOpcode.VNLWorldStatusMessage
  def encode = VNLWorldStatusMessage.encode(this)
}

object VNLWorldStatusMessage extends Marshallable[VNLWorldStatusMessage] {
  type InStruct  = WorldStatus.Value :: ServerType :: HNil
  type OutStruct = Int :: ServerType :: Int :: HNil

  implicit val statusCodec: Codec[InStruct] = {
    def from(a: InStruct): OutStruct =
      a match {
        case status :: svrType :: HNil =>
          status match {
            case WorldStatus.Down =>
              0 :: svrType :: 2 :: HNil
            case WorldStatus.Locked =>
              0 :: svrType :: 1 :: HNil
            case WorldStatus.Up =>
              1 :: svrType :: 0 :: HNil
            case WorldStatus.Full =>
              5 :: svrType :: 0 :: HNil
          }
      }

    def to(a: OutStruct): InStruct =
      a match {
        case status2 :: svrType :: status1 :: HNil =>
          if (status1 == 0) {
            if (status2 >= 5) {
              WorldStatus.Full :: svrType :: HNil
            } else {
              WorldStatus.Up :: svrType :: HNil
            }
          } else {
            if (status1 != 1)
              WorldStatus.Down :: svrType :: HNil
            else
              WorldStatus.Locked :: svrType :: HNil
          }
      }

    (("status2" | uint16L) ::
      ("server_type" | ServerType.codec) ::
      ("status1" | uint8L)).xmap(to, from)
  }

  implicit val connectionCodec: Codec[WorldConnectionInfo] = {

    type DecodeStruct = ByteVector :: Int :: HNil
    type EncodeStruct = InetSocketAddress :: HNil

    def decode(a: DecodeStruct): EncodeStruct =
      a match {
        case ipBytes :: port :: HNil =>
          val addr = new InetSocketAddress(InetAddress.getByAddress(ipBytes.reverse.toArray), port)
          addr :: HNil
      }

    def encode(a: EncodeStruct): DecodeStruct =
      a match {
        case addr :: HNil =>
          val ip   = addr.getAddress.getAddress
          val port = addr.getPort

          ByteVector(ip).reverse :: port :: HNil
      }

    (bytes(4) :: uint16L).xmap(decode, encode).as[WorldConnectionInfo]
  }

  implicit val world_codec: Codec[WorldInformation] = (("world_name" | PacketHelpers.encodedString) :: (
    ("status_and_type" | statusCodec) :+
      // TODO: limit the size of this vector to 11 as the client will fail on any more
      ("connections" | vectorOfN(uint8L, connectionCodec))
      :+
        ("empire_need" | PlanetSideEmpire.codec)
  )).as[WorldInformation]

  implicit val world_codec_aligned: Codec[WorldInformation] =
    (("world_name" | PacketHelpers.encodedStringAligned(6)) :: (
      ("status_and_type" | statusCodec) :+
        // TODO: limit the size of this vector to 11 as the client will fail on any more
        ("connections" | vectorOfN(uint8L, connectionCodec))
        :+
          ("empire_need" | PlanetSideEmpire.codec)
    )).as[WorldInformation]

  implicit val codec: Codec[VNLWorldStatusMessage] = (
    ("welcome_message" | PacketHelpers.encodedWideString) ::
      ("worlds" | prefixedVectorOfN(uint8L, world_codec, world_codec_aligned))
  ).as[VNLWorldStatusMessage]
}
