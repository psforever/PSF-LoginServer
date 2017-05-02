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

object TargetInfo {
  /**
    * Transform an unsigned number between 0 and 256 into a percentage of a 255 number.
    * @param n an unsigned `Integer` number
    * @return a scaled `Float` number
    */
  private def rangedFloat(n : Int) : Float = {
    (
      (if(n <= 0) {
        0
      }
      else if(n >= 255) {
        255
      }
      else {
        n
      }).toDouble * TargetingInfoMessage.unit
    ).toFloat
  }

  /**
    * Overloaded constructor that takes `Integer` values rather than `Float` values.
    * @param target_guid the target
    * @param unk1 na
    * @param unk2 na
    * @return a `TargetInfo` object
    */
  def apply(target_guid : PlanetSideGUID, unk1 : Int, unk2 : Int) : TargetInfo = {
    val unk1_2 : Float = rangedFloat(unk1)
    val unk2_2 : Float = rangedFloat(unk2)
    TargetInfo(target_guid, unk1_2, unk2_2)
  }

  /**
    * Overloaded constructor that takes `Integer` values rather than `Float` values and assumes the second `Integer` is zero.
    * @param target_guid the target
    * @param unk na
    * @return a `TargetInfo` object
    */
  def apply(target_guid : PlanetSideGUID, unk : Int) : TargetInfo = {
    val unk1_2 : Float = rangedFloat(unk)
    TargetInfo(target_guid, unk1_2, 0)
  }
}

object TargetingInfoMessage extends Marshallable[TargetingInfoMessage] {
  final val unit : Double = 0.0039215689 //common constant for 1/255

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
