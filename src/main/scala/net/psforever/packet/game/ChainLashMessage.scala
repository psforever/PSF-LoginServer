// Copyright (c) 2020 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param lash_origin_target na
  * @param lash_origin_pos na
  * @param projectile_type na
  * @param targets na
  */
final case class ChainLashMessage(
                                   lash_origin_target: Option[PlanetSideGUID],
                                   lash_origin_pos: Option[Vector3],
                                   projectile_type : Int,
                                   targets: List[PlanetSideGUID]
                                 ) extends PlanetSideGamePacket {
  if(lash_origin_target.isEmpty != lash_origin_pos.nonEmpty) {
    assert(lash_origin_target.isEmpty, "one of these fields must be defined - unk1a, unk1b")
    assert(lash_origin_target.nonEmpty, "these fields can not be defined simultaneously - unk1a, unk1b")
  }
  type Packet = ChainLashMessage
  def opcode = GamePacketOpcode.ChainLashMessage
  def encode = ChainLashMessage.encode(this)
}

object ChainLashMessage extends Marshallable[ChainLashMessage] {
  def apply(lashOrigin : PlanetSideGUID, projectileType : Int, targets : List[PlanetSideGUID]) : ChainLashMessage =
    ChainLashMessage(Some(lashOrigin), None, projectileType, targets)

  def apply(lashOrigin : Vector3, projectileType : Int, targets : List[PlanetSideGUID]) : ChainLashMessage =
    ChainLashMessage(None, Some(lashOrigin), projectileType, targets)

  implicit val codec: Codec[ChainLashMessage] = (
    bool >>:~ { unk1 =>
      conditional(!unk1, codec = "lash_origin_target" | PlanetSideGUID.codec) ::
        conditional(unk1,codec = "lash_origin_pos" | Vector3.codec_pos) ::
        ("projectile_type" | uintL(bits = 11)) ::
        (uint32L >>:~ { len =>
          PacketHelpers.listOfNSized(len, codec = "targets" | PlanetSideGUID.codec).hlist
        })
    }
    ).xmap[ChainLashMessage] (
    {
      case _ :: target :: origin :: proj :: _ :: targets :: HNil =>
        ChainLashMessage(target, origin, proj, targets)
    },
    {
      case ChainLashMessage(target, origin, proj, targets) =>
        target.isEmpty :: target :: origin :: proj :: targets.length.toLong :: targets :: HNil
    }
  )
}
