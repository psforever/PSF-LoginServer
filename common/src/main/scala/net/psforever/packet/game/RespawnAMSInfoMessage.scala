// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class RespawnInfo(unk1 : List[Vector3],
                             unk2 : List[Boolean])

final case class RespawnAMSInfoMessage(unk1 : PlanetSideGUID,
                                       unk2 : Boolean,
                                       unk3 : Option[RespawnInfo])
  extends PlanetSideGamePacket {
  type Packet = RespawnAMSInfoMessage
  def opcode = GamePacketOpcode.RespawnAMSInfoMessage
  def encode = RespawnAMSInfoMessage.encode(this)
}

object RespawnAMSInfoMessage extends Marshallable[RespawnAMSInfoMessage] {
  def apply(u1 : PlanetSideGUID, u2 : Boolean) : RespawnAMSInfoMessage = {
    RespawnAMSInfoMessage(u1, u2, None)
  }

  def apply(u1 : PlanetSideGUID, u2 : Boolean, u3 : RespawnInfo) : RespawnAMSInfoMessage = {
    RespawnAMSInfoMessage(u1, u2, Some(u3))
  }

  private val info_codec : Codec[RespawnInfo] = (
    uint(6) >>:~ { size => //max 63
      ("unk1" | PacketHelpers.listOfNSized(size, Vector3.codec_pos)) ::
        ("unk2" | PacketHelpers.listOfNSized(size, bool))
    }).exmap[RespawnInfo] ({
      case _ :: a :: b :: HNil =>
        Attempt.Successful(RespawnInfo(a, b))
    },
    {
      case RespawnInfo(a, b) =>
        val alen = a.length
        if(alen != b.length) {
          Attempt.Failure(Err(s"respawn info lists must match in length - $alen vs ${b.length}"))
        }
        else if(alen > 63) {
          Attempt.Failure(Err(s"respawn info lists too long - $alen > 63"))
        }
        else {
          Attempt.Successful(alen :: a :: b :: HNil)
        }
    }
  )

  /*
  technically, the order of reading should be 16u + 1u + 7u which is byte-aligned
  the 7u, however, is divided into a subsequent 1u + 6u reading
  if that second 1u is true, the 6u doesn't matter and doesn't need to be read when not necessary
   */
  implicit val codec : Codec[RespawnAMSInfoMessage] = (
    ("unk1" | PlanetSideGUID.codec) ::
      ("unk2" | bool) ::
      (bool >>:~ { test =>
        conditional(!test, "unk3" | info_codec).hlist
      })
    ).xmap[RespawnAMSInfoMessage] (
    {
      case u1 :: u2 :: _ :: u3 :: HNil =>
        RespawnAMSInfoMessage(u1, u2, u3)
    },
    {
      case RespawnAMSInfoMessage(u1, u2, u3) =>
        u1 :: u2 :: u3.isDefined :: u3 :: HNil
    }
  )
}
