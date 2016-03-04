// Copyright (c) 2016 PSForever.net to present
package psforever.net
import scodec._
import scodec.bits._
import scodec.codecs._
import shapeless._

object WorldStatus extends Enumeration {
  type Type = Value
  val Up, Down, Locked, Full = Value
}

object ServerType extends Enumeration {
  type Type = Value
  val Development, Beta, Released = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8L)
}

object EmpireNeed extends Enumeration {
  type Type = Value
  val TR, NC, VS = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint2L)
}

final case class WorldInformation(name : String, status : WorldStatus.Value,
                             serverType : ServerType.Value, empireNeed : EmpireNeed.Value)

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

  implicit val codec : Codec[VNLWorldStatusMessage] = (
    ("welcome_message" | PacketHelpers.encodedWideString) ::
      ("worlds" | vectorOfN(uint8L, (
        // XXX: this needs to be limited to 0x20 bytes
        // XXX: this needs to be byte aligned, but not sure how to do this
        ("world_name" | PacketHelpers.encodedString) :: (
          ("status_and_type" | statusCodec) :+
          ("unknown" | constant(hex"01459e25403775")) :+
          ("empire_need" | EmpireNeed.codec)
        )
      ).as[WorldInformation]
      ))).as[VNLWorldStatusMessage]
}
