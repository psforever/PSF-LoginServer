// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

sealed trait Trade {
  def value: Int
}

final case class NoTrade(value: Int) extends Trade {
  assert(value > 0 || value < 10, s"NoTrade has wrong code value - $value not in [0,a-f]")
}

final case class TradeOne(value: Int, unk1: PlanetSideGUID, unk2: PlanetSideGUID, unk3: PlanetSideGUID) extends Trade {
  assert(value == 1 || value == 2 || value == 3, s"TradeOne has wrong code value - $value not in [1,2,3]")
}

final case class TradeTwo(value: Int, unk1: PlanetSideGUID, unk2: PlanetSideGUID) extends Trade {
  assert(value == 4 || value == 5 || value == 7, s"TradeTwo has wrong code value - $value not in [4,5,7]")
}

final case class TradeThree(value: Int, unk: PlanetSideGUID) extends Trade {
  assert(value == 6 || value == 8, s"TradeThree has wrong code value - $value not in [6,8]")
}

final case class TradeFour(value: Int, unk: Int) extends Trade {
  assert(value == 9, s"TradeFour has wrong code value - $value not in [9]")
  assert(unk > -1 && unk < 16, s"TradeFour value is out of bounds - $unk not in [0-f]")
}

final case class TradeMessage(trade: Trade)
  extends PlanetSideGamePacket {
  type Packet = TradeMessage
  def opcode = GamePacketOpcode.TradeMessage
  def encode = TradeMessage.encode(this)
}

object NoTrade {
  def apply(): NoTrade = NoTrade(0)
}

object TradeOne {
  def apply(unk1: PlanetSideGUID, unk2: PlanetSideGUID, unk3: PlanetSideGUID): TradeOne = TradeOne(1, unk1, unk2, unk3)
}

object TradeTwo {
  def apply(unk1: PlanetSideGUID, unk2: PlanetSideGUID): TradeTwo = TradeTwo(4, unk1, unk2)
}

object TradeThree {
  def apply(unk: PlanetSideGUID): TradeThree = TradeThree(6, unk)
}

object TradeFour {
  def apply(unk: Int): TradeFour = TradeFour(6, unk)
}

object TradeMessage extends Marshallable[TradeMessage] {
  private def tradeOneCodec(value: Int): Codec[TradeOne] = (
    ("u1" | PlanetSideGUID.codec) ::
    ("u2" | PlanetSideGUID.codec) ::
    ("u3" | PlanetSideGUID.codec)
  ).xmap[TradeOne](
    {
      case a :: b :: c :: HNil => TradeOne(value, a, b, c)
    },
    {
      case TradeOne(_, a, b, c) => a :: b :: c :: HNil
    }
  )

  private def tradeTwoCodec(value: Int): Codec[TradeTwo] = (
    ("u1" | PlanetSideGUID.codec) ::
    ("u2" | PlanetSideGUID.codec)
  ).xmap[TradeTwo](
    {
      case a :: b :: HNil => TradeTwo(value, a, b)
    },
    {
      case TradeTwo(_, a, b) => a :: b :: HNil
    }
  )

  private def tradeThreeCodec(value: Int): Codec[TradeThree] = ("u1" | PlanetSideGUID.codec).xmap[TradeThree](
    a => TradeThree(value, a),
    {
      case TradeThree(_, a) => a
    }
  )

  private def tradeFourCodec(value: Int): Codec[TradeFour] = ("u1" | uint4).xmap[TradeFour](
    a => TradeFour(value, a),
    {
      case TradeFour(_, a) => a
    }
  )

  private def noTradeCodec(value: Int): Codec[NoTrade] = conditional(included = false, ignore(size = 1)).xmap[NoTrade](
    _ => NoTrade(value),
    {
      case NoTrade(_) => None
    }
  )

  private def selectTradeCodec(code: Int): Codec[Trade] = {
    (code match {
      case 1 | 2 | 3 => tradeOneCodec(code)
      case 4 | 5 | 7 => tradeTwoCodec(code)
      case 6 | 8     => tradeThreeCodec(code)
      case 9         => tradeFourCodec(code)
      case _         => noTradeCodec(code)
    }).asInstanceOf[Codec[Trade]]
  }

  implicit val codec: Codec[TradeMessage] = (
    uint4 >>:~ { code => ("trade" | selectTradeCodec(code)).hlist }
    ).xmap[TradeMessage](
    {
      case _ :: trade :: HNil => TradeMessage(trade)
    },
    {
      case TradeMessage(trade) => trade.value :: trade :: HNil
    }
  )
}
