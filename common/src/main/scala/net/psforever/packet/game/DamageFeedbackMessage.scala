// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class DamageFeedbackMessage(unk1 : Int,
                                       unk2 : Boolean,
                                       unk2a : Option[PlanetSideGUID],
                                       unk2b : Option[String],
                                       unk2c : Option[Int],
                                       unk3 : Boolean,
                                       unk3a : Option[PlanetSideGUID],
                                       unk3b : Option[String],
                                       unk3c : Option[Int],
                                       unk3d : Option[Int],
                                       unk4 : Int,
                                       unk5 : Long,
                                       unk6 : Int)
  extends PlanetSideGamePacket {
  assert(
    {
      val unk2aEmpty = unk2a.isEmpty
      val unk2bEmpty = unk2b.isEmpty
      val unk2cEmpty = unk2c.isEmpty
      if(unk2a.nonEmpty) unk2bEmpty && unk2cEmpty
      else if(unk2b.nonEmpty) unk2 && unk2aEmpty && unk2cEmpty
      else unk2aEmpty && !unk2 && unk2bEmpty && unk2c.nonEmpty
    }
  )
  assert(
    {
      val unk3aEmpty = unk3a.isEmpty
      val unk3bEmpty = unk3b.isEmpty
      val unk3cEmpty = unk3c.isEmpty
      if(unk3a.nonEmpty) unk3bEmpty && unk3cEmpty
      else if(unk3b.nonEmpty) unk3 && unk3aEmpty && unk3cEmpty
      else unk3aEmpty && !unk3 && unk3bEmpty && unk3c.nonEmpty
    }
  )
  assert(unk3a.isEmpty == unk3d.nonEmpty)

  type Packet = DamageFeedbackMessage
  def opcode = GamePacketOpcode.DamageFeedbackMessage
  def encode = DamageFeedbackMessage.encode(this)
}

object DamageFeedbackMessage extends Marshallable[DamageFeedbackMessage] {
  implicit val codec : Codec[DamageFeedbackMessage] = (
    ("unk1" | uint4) ::
      (bool >>:~ { u2 =>
        bool >>:~ { u3 =>
          ("unk2a" | conditional(u2, PlanetSideGUID.codec)) ::
            (("unk2b" | conditional(!u2 && u3, PacketHelpers.encodedWideStringAligned(6))) >>:~ { u2b =>
              ("unk2c" | conditional(!(u2 && u3), uintL(11))) ::
                (bool >>:~ { u5 =>
                  bool >>:~ { u6 =>
                  ("unk3a" | conditional(u5, PlanetSideGUID.codec)) ::
                    ("unk3b" | conditional(!u5 && u6, PacketHelpers.encodedWideStringAligned( if(u2b.nonEmpty) 3 else 1 ))) ::
                    ("unk3c" | conditional(!(u5 && u6), uintL(11))) ::
                    ("unk3d" | conditional(!u5, uint2)) ::
                    ("unk4" | uint(3)) ::
                    ("unk5" | uint32L) ::
                    ("unk6" | uint2)
                }
              })
            })
        }
      })
    ).xmap[DamageFeedbackMessage] (
    {
      case u1 :: _ :: u2 :: u2a :: u2b :: u2c :: _ :: u3 :: u3a :: u3b :: u3c :: u3d :: u4 :: u5 :: u6 :: HNil =>
        DamageFeedbackMessage(u1, u2, u2a, u2b, u2c, u3, u3a, u3b, u3c, u3d, u4, u5, u6)
    },
    {
      case DamageFeedbackMessage(u1, u2, u2a, u2b, u2c, u3, u3a, u3b, u3c, u3d, u4, u5, u6) =>
        u1 :: u2a.nonEmpty :: u2 :: u2a :: u2b :: u2c :: u3a.nonEmpty :: u3 :: u3a :: u3b :: u3c :: u3d :: u4 :: u5 :: u6 :: HNil
    }
  )
}
