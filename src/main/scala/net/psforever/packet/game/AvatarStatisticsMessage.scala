// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

import scala.annotation.switch

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  */
final case class Statistics(unk1: Option[Int], unk2: Option[Int], unk3: List[Long])

/**
  * na
  * @param unk na
  * @param stats na
  */
final case class AvatarStatisticsMessage(unk: Int, stats: Statistics) extends PlanetSideGamePacket {
  type Packet = AvatarStatisticsMessage
  def opcode = GamePacketOpcode.AvatarStatisticsMessage
  def encode = AvatarStatisticsMessage.encode(this)
}

object AvatarStatisticsMessage extends Marshallable[AvatarStatisticsMessage] {

  /**
    * na
    */
  private val longCodec: Codec[Statistics] = ulong(32).hlist.exmap(
    {
      case n :: HNil =>
        Attempt.Successful(Statistics(None, None, List(n)))
    },
    {
      case Statistics(_, _, Nil) =>
        Attempt.Failure(Err("missing value (32-bit)"))

      case Statistics(_, _, n) =>
        Attempt.Successful(n.head :: HNil)
    }
  )

  /**
    * na
    */
  private val complexCodec: Codec[Statistics] = (
    uint(5) ::
      uintL(11) ::
      PacketHelpers.listOfNSized(8, uint32L)
  ).exmap[Statistics](
    {
      case a :: b :: c :: HNil =>
        Attempt.Successful(Statistics(Some(a), Some(b), c))
    },
    {
      case Statistics(None, _, _) =>
        Attempt.Failure(Err("missing value (5-bit)"))

      case Statistics(_, None, _) =>
        Attempt.Failure(Err("missing value (11-bit)"))

      case Statistics(a, b, c) =>
        if (c.length != 8) {
          Attempt.Failure(Err("list must have 8 entries"))
        } else {
          Attempt.Successful(a.get :: b.get :: c :: HNil)
        }
    }
  )

  /**
    * na
    * @param n na
    * @return na
    */
  private def selectCodec(n: Int): Codec[Statistics] =
    (n: @switch) match {
      case 2 | 3 =>
        longCodec
      case _ =>
        complexCodec
    }

  implicit val codec: Codec[AvatarStatisticsMessage] = (("unk" | uint(3)) >>:~ { unk =>
    ("stats" | selectCodec(unk)).hlist
  }).as[AvatarStatisticsMessage]
}

object Statistics {
  def apply(unk: Long): Statistics =
    Statistics(None, None, List(unk))

  def apply(unk1: Int, unk2: Int, unk3: List[Long]): Statistics =
    Statistics(Some(unk1), Some(unk2), unk3)
}
