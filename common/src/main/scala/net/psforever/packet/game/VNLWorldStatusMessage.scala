// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import java.net.{InetAddress, InetSocketAddress}

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec._
import scodec.bits._
import scodec.codecs._
import shapeless._

object WorldStatus extends Enumeration {
  type Type = Value
  val Up, Down, Locked, Full = Value
}

// this enumeration starts from one and is subtracted from before processing (0x005FF12A)
object ServerType extends Enumeration(1) {
  type Type = Value
  val Development, Beta, Released = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)
}

object PlanetSideEmpire extends Enumeration {
  type Type = Value
  val TR, NC, VS, NEUTRAL = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint2L)
}

final case class WorldConnectionInfo(address : InetSocketAddress)

final case class WorldInformation(name : String, status : WorldStatus.Value,
                                  serverType : ServerType.Value,
                                  connections : Vector[WorldConnectionInfo],
                                  empireNeed : PlanetSideEmpire.Value)

final case class VNLWorldStatusMessage(welcomeMessage : String, worlds : Vector[WorldInformation])
  extends PlanetSideGamePacket {
  type Packet = VNLWorldStatusMessage
  def opcode = GamePacketOpcode.VNLWorldStatusMessage
  def encode = VNLWorldStatusMessage.encode(this)
}

object VNLWorldStatusMessage extends Marshallable[VNLWorldStatusMessage] {
  type InStruct = WorldStatus.Value :: ServerType.Value :: HNil
  type OutStruct = Int :: ServerType.Value :: Int :: HNil

  implicit val statusCodec : Codec[InStruct] = {
    def from(a : InStruct) : OutStruct = a match {
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

    def to(a : OutStruct) : InStruct = a match {
      case status2 :: svrType :: status1 :: HNil =>
        if(status1 == 0) {
          if(status2 >= 5) {
            WorldStatus.Full :: svrType :: HNil
          } else {
            WorldStatus.Up :: svrType :: HNil
          }
        } else {
          if(status1 != 1)
            WorldStatus.Down :: svrType :: HNil
          else
            WorldStatus.Locked :: svrType :: HNil
        }
    }

    (("status2" | uint16L) ::
    ("server_type" | ServerType.codec) ::
    ("status1" | uint8L)).xmap(to, from)
  }

  implicit val connectionCodec : Codec[WorldConnectionInfo] = {

    type DecodeStruct = ByteVector :: Int :: HNil
    type EncodeStruct = InetSocketAddress :: HNil

    def decode(a : DecodeStruct) : EncodeStruct = a match {
      case ipBytes :: port :: HNil =>
        val addr = new InetSocketAddress(InetAddress.getByAddress(ipBytes.reverse.toArray), port)
        addr  :: HNil
    }

    def encode(a : EncodeStruct) : DecodeStruct = a match {
      case addr :: HNil =>
        val ip = addr.getAddress.getAddress
        val port = addr.getPort

        ByteVector(ip).reverse :: port :: HNil
    }

    (bytes(4) :: uint16L).xmap(decode, encode).as[WorldConnectionInfo]
  }

  implicit val codec : Codec[VNLWorldStatusMessage] = (
    ("welcome_message" | PacketHelpers.encodedWideString) ::
      ("worlds" | vectorOfN(uint8L, (
        // XXX: this needs to be limited to 0x20 bytes
        // XXX: this needs to be byte aligned, but not sure how to do this
        ("world_name" | PacketHelpers.encodedString) :: (
          ("status_and_type" | statusCodec) :+
          // TODO: limit the size of this vector to 11 as the client will fail on any more
          ("connections" | vectorOfN(uint8L, connectionCodec)) :+
          ("empire_need" | PlanetSideEmpire.codec)
        )
      ).as[WorldInformation]
      ))).as[VNLWorldStatusMessage]
}
