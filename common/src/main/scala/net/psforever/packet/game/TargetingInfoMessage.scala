// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * An entry regarding a target's health and, if applicable, any secondary defensive option they possess, hitherto, "armor."
  * @param target_guid the target
  * @param health the amount of health the target has, as a percentage of a filled bar scaled between 0f and 1f inclusive
  * @param armor the amount of armor the target has, as a percentage of a filled bar scaled between 0f and 1f inclusive;
  *              defaults to 0f
  */
final case class TargetInfo(target_guid : PlanetSideGUID,
                            health : Float,
                            armor : Float = 0f)

/**
  * Dispatched by the server to update status information regarding the listed targets.<br>
  * <br>
  * This packet is often in response to a client-sent `TargetingImplantRequest` packet, when related to the implant's operation.
  * It can also arrive independent of a formal request and will operate even without the implant.
  * The enumerated targets report their status as two "progress bars" that can be empty (0f) or filled (1f).
  * When this packet is received, the client will actually update the current fields associated with those values for the target.
  * For example, for `0x17` player characters, the values are assigned to their health points and armor points respectively.
  * Allied player characters will have their "progress bar" visuals updated immediately;
  * the implant is still necessary to view enemy target progress bars, if they will be visible.<br>
  * <br>
  * This function can be used to update fields properly.
  * The value between 0 and 255 (0f to 1f) can be inserted directly into `ObjectCreateMessage` creations as it matches the scale.
  * The target will be killed or destroyed as expected when health is set to zero.
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
    * Overloaded constructor that takes `Integer` values rather than `Float` values.
    * @param target_guid the target
    * @param health the amount of health the target has
    * @param armor the amount of armor the target has
    * @return a `TargetInfo` object
    */
  def apply(target_guid : PlanetSideGUID, health : Int, armor : Int) : TargetInfo = {
    val health2 : Float = TargetingInfoMessage.rangedFloat(health)
    val armor2 : Float = TargetingInfoMessage.rangedFloat(armor)
    TargetInfo(target_guid, health2, armor2)
  }

  /**
    * Overloaded constructor that takes `Integer` values rather than `Float` values and only expects the first field.
    * @param target_guid the target
    * @param health the amount of health the target has
    * @return a `TargetInfo` object
    */
  def apply(target_guid : PlanetSideGUID, health : Int) : TargetInfo = {
    val health2 : Float = TargetingInfoMessage.rangedFloat(health)
    TargetInfo(target_guid, health2)
  }
}

object TargetingInfoMessage extends Marshallable[TargetingInfoMessage] {
  private final val unit : Double = 0.0039215689 //common constant for 1/255

  /**
    * Transform an unsigned `Integer` number into a scaled `Float`.
    * @param n an unsigned `Integer` number inclusive 0 and below 256
    * @return a scaled `Float` number inclusive to 0f to 1f
    */
  def rangedFloat(n : Int) : Float = {
    (
      (if(n <= 0) {
        0
      }
      else if(n >= 255) {
        255
      }
      else {
        n
      }).toDouble * unit
      ).toFloat
  }
  /**
    * Transform a scaled `Float` number into an unsigned `Integer`.
    * @param n `Float` number inclusive to 0f to 1f
    * @return a scaled unsigned `Integer` number inclusive 0 and below 256
    */
  def rangedInt(n : Float) : Int = {
    (
      (if(n <= 0f) {
        0f
      }
      else if(n >= 1.0f) {
        1.0f
      }
      else {
        n
      }).toDouble * 255
    ).toInt
  }

  private val info_codec : Codec[TargetInfo] = (
    ("target_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L)
  ).xmap[TargetInfo] (
    {
      case a :: b :: c :: HNil =>
        val b2 : Float = rangedFloat(b)
        val c2 : Float = rangedFloat(c)
        TargetInfo(a, b2, c2)
    },
    {
      case TargetInfo(a, b, c) =>
        val b2 : Int = rangedInt(b)
        val c2 : Int = rangedInt(c)
        a :: b2 :: c2 :: HNil
    }
  )

  implicit val codec : Codec[TargetingInfoMessage] = ("target_list" | listOfN(uint8L, info_codec)).as[TargetingInfoMessage]
}
