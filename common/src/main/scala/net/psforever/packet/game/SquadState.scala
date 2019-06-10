// Copyright (c) 2019 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class SquadStateInfo(char_id : Long,
                                unk2 : Int,
                                unk3 : Int,
                                pos : Vector3,
                                unk4 : Int,
                                unk5 : Int,
                                unk6 : Boolean,
                                unk7 : Int,
                                unk8 : Option[Int],
                                unk9 : Option[Boolean])

//7704001646c7a02810050a1a2bc97842280000
// SquadState(PlanetSideGUID(4),List(SquadStateInfo(1684830722,64,64,Vector3(3152.1562,3045.0781,35.515625),2,2,false,0,None,None)))
//770700342a28c028100420d60e9df8c2800000eab58a028100ce514b655ddc341400286d9130000021eb951539f4c4050800
// SquadState(PlanetSideGUID(7),List(SquadStateInfo(1117948930,64,64,Vector3(2822.125,3919.0234,43.546875),0,0,false,0,None,None), SquadStateInfo(3937765890,64,64,Vector3(2856.3984,3882.3516,53.859375),0,0,false,416,None,None), SquadStateInfo(2262373120,0,0,Vector3(2909.0547,3740.539,67.296875),0,1,false,132,None,None)))
//7704005dd9ccf01810132fdf9f9ef5c4a8000084de7a022c00e5898c8d5e4c429b004ed01d50181016395a4c364e08280001b901a070bd0140805308d59f90641f40000c001db11e280a00088d19e3a190f0ca6c0100
// SquadState(PlanetSideGUID(4),List(SquadStateInfo(3718041345,64,64,Vector3(3966.5938,6095.8047,75.359375),2,2,false,0,None,None), SquadStateInfo(2229172738,22,0,Vector3(3268.4453,3690.3906,66.296875),2,2,false,728,None,None), SquadStateInfo(3976320257,64,64,Vector3(3530.6875,4635.1484,128.875),2,2,false,0,Some(441),Some(true)), SquadStateInfo(1088518658,64,64,Vector3(3336.3203,4601.3438,60.78125),2,2,false,0,Some(896),Some(true)), SquadStateInfo(1816627714,64,0,Vector3(5027.0625,4931.7734,48.234375),2,2,false,728,None,None)))
final case class SquadState(guid : PlanetSideGUID,
                            info_list : List[SquadStateInfo])
  extends PlanetSideGamePacket {
  type Packet = SquadState
  def opcode = GamePacketOpcode.SquadState
  def encode = SquadState.encode(this)
}

object SquadStateInfo {
  def apply(unk1 : Long, unk2 : Int, unk3 : Int, pos : Vector3, unk4 : Int, unk5 : Int, unk6 : Boolean, unk7 : Int) : SquadStateInfo =
    SquadStateInfo(unk1, unk2, unk3, pos, unk4, unk5, unk6, unk7, None, None)

  def apply(unk1 : Long, unk2 : Int, unk3 : Int, pos : Vector3, unk4 : Int, unk5 : Int, unk6 : Boolean, unk7 : Int, unk8 : Int, unk9 : Boolean) : SquadStateInfo =
    SquadStateInfo(unk1, unk2, unk3, pos, unk4, unk5, unk6, unk7, Some(unk8), Some(unk9))
}

object SquadState extends Marshallable[SquadState] {
  private val info_codec : Codec[SquadStateInfo] = (
    ("char_id" | uint32L) ::
      ("unk2" | uint(7)) ::
      ("unk3" | uint(7)) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk4" | uint2) ::
      ("unk5" | uint2) ::
      ("unk6" | bool) ::
      ("unk7" | uint16L) ::
      (bool >>:~ { out =>
        conditional(out, "unk8" | uint16L) ::
          conditional(out, "unk9" | bool)
      })
    ).exmap[SquadStateInfo] (
    {
      case char_id :: u2 :: u3 :: pos :: u4 :: u5 :: u6 :: u7 :: _ :: u8 :: u9 :: HNil =>
        Attempt.Successful(SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, u8, u9))
    },
    {
      case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, Some(u8), Some(u9)) =>
        Attempt.Successful(char_id :: u2 :: u3 :: pos :: u4 :: u5 :: u6 :: u7 :: true :: Some(u8) :: Some(u9) :: HNil)
      case SquadStateInfo(char_id, u2, u3, pos, u4, u5, u6, u7, None, None) =>
        Attempt.Successful(char_id :: u2 :: u3 :: pos :: u4 :: u5 :: u6 :: u7 :: false :: None :: None :: HNil)
      case data @ (SquadStateInfo(_, _, _, _, _, _, _, _, Some(_), None) | SquadStateInfo(_, _, _, _, _, _, _, _, None, Some(_))) =>
        Attempt.Failure(Err(s"SquadStateInfo requires both unk8 and unk9 to be either defined or undefined at the same time - $data"))
    }
  )

  implicit val codec : Codec[SquadState] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("info_list" | listOfN(uint4, info_codec))
    ).as[SquadState]
}
