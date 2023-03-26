// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{StatisticalCategory, StatisticalElement}
import scodec.Attempt.{Failure, Successful}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

import scala.annotation.switch

/**
 * na
 */
sealed abstract class Statistic(val code: Int)

/**
 * na
 */
sealed trait ComplexStatistic {
  def category: StatisticalCategory
  def unk: StatisticalElement
  def fields: List[Long]
}
/**
 * na
 */
sealed case class IntermediateStatistic(
                                         category: StatisticalCategory,
                                         unk: StatisticalElement,
                                         fields: List[Long]
                                       ) extends ComplexStatistic

/**
 *
 * @param category na
 * @param unk na
 * @param fields four pairs of values that add together to produce the first columns on the statistics spreadsheet;
 *             organized as TR, NC, VS, BO (PS)
 */
final case class CampaignStatistic(
                                category: StatisticalCategory,
                                unk: StatisticalElement,
                                fields: List[Long]
                              ) extends Statistic(code = 0) with ComplexStatistic

object CampaignStatistic {
  def apply(cat: StatisticalCategory, stat: StatisticalElement, tr: Long, nc: Long, vs: Long, bo: Long): CampaignStatistic = {
    CampaignStatistic(cat, stat, List(tr, 0, nc, 0, vs, 0, bo, 0))
  }
}

/**
 *
 * @param category na
 * @param unk na
 * @param fields four pairs of values that add together to produce the first column(s) on the statistics spreadsheet;
 *               organized as TR, NC, VS, BO (PS)
 */
final case class SessionStatistic(
                                  category: StatisticalCategory,
                                  unk: StatisticalElement,
                                  fields: List[Long]
                                ) extends Statistic(code = 1) with ComplexStatistic

object SessionStatistic {
  def apply(cat: StatisticalCategory, stat: StatisticalElement, tr: Long, nc: Long, vs: Long, bo: Long): SessionStatistic = {
    SessionStatistic(cat, stat, List(0, tr, 0, nc, 0, vs, 0, bo))
  }
}

/**
 *
 * @param deaths how badly you suck, quantitatively analyzed
 */
final case class DeathStatistic(deaths: Long) extends Statistic(code = 2)

/**
 * na
 * @param stats na
 */
final case class AvatarStatisticsMessage(stats: Statistic) extends PlanetSideGamePacket {
  type Packet = AvatarStatisticsMessage
  def opcode = GamePacketOpcode.AvatarStatisticsMessage
  def encode = AvatarStatisticsMessage.encode(this)
}

object AvatarStatisticsMessage extends Marshallable[AvatarStatisticsMessage] {
  /**
   * na
   */
  private val complexCodec: Codec[IntermediateStatistic] = (
    PacketHelpers.createIntEnumCodec(StatisticalCategory, uint(bits = 5)) ::
      PacketHelpers.createIntEnumCodec(StatisticalElement, uintL(bits = 11)) ::
      PacketHelpers.listOfNSized(size = 8, uint32L)
    ).exmap[IntermediateStatistic](
    {
      case a :: b :: c :: HNil =>
        Attempt.Successful(IntermediateStatistic(a, b, c))
    },
    {
      case IntermediateStatistic(a, b, c) =>
        if (c.length != 8) {
          Attempt.Failure(Err("list must have 8 entries"))
        } else {
          Attempt.Successful(a :: b :: c :: HNil)
        }
      case _ =>
        Attempt.Failure(Err("wrong type of statistic object or missing values (5-bit, 11-bit, 8 x 32-bit)"))
    }
  )

  /**
   * na
   */
  private val initCodec: Codec[Statistic] = complexCodec.exmap[Statistic](
    {
      case IntermediateStatistic(a, b, c) => Successful(CampaignStatistic(a, b, c))
    },
    {
      case CampaignStatistic(a, b, c) => Successful(IntermediateStatistic(a, b, c))
      case _ => Failure(Err("expected initializing statistic, but found something else"))
    }
  )
  /**
   * na
   */
  private val updateCodec: Codec[Statistic] = complexCodec.exmap[Statistic](
    {
      case IntermediateStatistic(a, b, c) => Successful(SessionStatistic(a, b, c))
    },
    {
      case SessionStatistic(a, b, c) => Successful(IntermediateStatistic(a, b, c))
      case _ => Failure(Err("expected updating statistic, but found something else"))
    }
  )
  /**
   * na
   */
  private val deathCodec: Codec[Statistic] = ulongL(bits = 32).hlist.exmap[Statistic](
    {
      case n :: HNil => Successful(DeathStatistic(n))
    },
    {
      case DeathStatistic(n) => Successful(n :: HNil)
      case _ => Failure(Err("wrong type of statistics object or missing value (32-bit)"))
    }
  )

  /**
   * na
   * @param n na
   * @return na
   */
  private def selectCodec(n: Int): Codec[Statistic]
  =
    (n: @switch) match {
      case 2 => deathCodec
      case 1 => updateCodec
      case _ => initCodec
    }

  implicit val codec: Codec[AvatarStatisticsMessage] = (
    uint(bits = 3) >>:~ { code =>
      ("stats" | selectCodec(code)).hlist
    }
    ).xmap[AvatarStatisticsMessage](
    {
      case _ :: stat :: HNil => AvatarStatisticsMessage(stat)
    },
    {
      case AvatarStatisticsMessage(stat) => stat.code :: stat :: HNil
    }
  )
}
