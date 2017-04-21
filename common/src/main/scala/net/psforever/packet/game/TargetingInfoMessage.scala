// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * na
  * @param target_guid the target
  * @param unk1 na
  * @param unk2 na
  */
final case class TargetInfo(target_guid : PlanetSideGUID,
                            unk1 : Float,
                            unk2 : Float = 0f)

/**
  * na
  * @param target_list a list of targets
  */
final case class TargetingInfoMessage(target_list : List[TargetInfo])
  extends PlanetSideGamePacket {
  type Packet = TargetingInfoMessage
  def opcode = GamePacketOpcode.TargetingInfoMessage
  def encode = TargetingInfoMessage.encode(this)
}

object TargetingInfoMessage extends Marshallable[TargetingInfoMessage] {
  private final val unit : Double = 0.0039215689 //common constant for 1/255

  private val info_codec : Codec[TargetInfo] = (
    ("target_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L)
  ).xmap[TargetInfo] (
    {
      case a :: b :: c :: HNil =>
        val bFloat : Float = (b.toDouble * unit).toFloat
        val cFloat : Float = (c.toDouble * unit).toFloat
        TargetInfo(a, bFloat, cFloat)
    },
    {
      case TargetInfo(a, bFloat, cFloat) =>
        val b : Int = (bFloat.toDouble * 255).toInt
        val c : Int = (cFloat.toDouble * 255).toInt
        a :: b :: c :: HNil
    }
  )

  implicit val codec : Codec[TargetingInfoMessage] = ("target_list" | listOfN(uint8L, info_codec)).as[TargetingInfoMessage]
}
