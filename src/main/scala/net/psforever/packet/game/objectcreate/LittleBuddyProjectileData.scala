// Copyright (c) 2022 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

final case class LittleBuddyProjectileData(
                                            data: CommonFieldDataWithPlacement,
                                            u2: Int,
                                            u4: Boolean
                                          ) extends ConstructorData {
  assert(data.pos.vel.nonEmpty, "oicw little buddy object always requires velocity to be defined")
  /**
    * The length of the little buddy data is functionally `32u`
    * after all other fields are accounted for
    * but the packet decode demands an additional bit be accounted for.
    * @return the number of bits necessary to measure an object of this class
    */
  override def bitsize: Long = 33L + data.bitsize
}

object LittleBuddyProjectileData extends Marshallable[LittleBuddyProjectileData] {
  implicit val codec: Codec[LittleBuddyProjectileData] = (
    ("data" | CommonFieldDataWithPlacement.codec) ::
      ("unk2" | uint24L) ::
      uint(bits = 7) ::
      ("unk4" | bool)
    ).exmap[LittleBuddyProjectileData](
    {
      case data :: u2 :: 31 :: u4 :: HNil =>
        Attempt.successful(LittleBuddyProjectileData(data, u2, u4))
    },
    {
      case LittleBuddyProjectileData(data, u2, u4) =>
        Attempt.successful(data :: u2 :: 31 :: u4 :: HNil)
    }
  )
}
