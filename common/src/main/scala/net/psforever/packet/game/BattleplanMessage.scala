// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

final case class SheetOne(unk1 : Float,
                          unk2 : Int)

final case class SheetTwo(unk1 : Float,
                          unk2 : Float)

final case class SheetFive(unk1 : Float,
                           unk2 : Float,
                           unk3 : Float,
                           unk4 : Int)

final case class SheetSix(unk1 : Float,
                          unk2 : Float,
                          unk3 : Int,
                          unk4 : Int,
                          unk5 : String)

final case class SheetSeven(unk : Int)

final case class BattleDiagram(sheet1 : Option[SheetOne] = None,
                               sheet2 : Option[SheetTwo] = None,
                               sheet3 : Option[SheetFive] = None,
                               sheet4 : Option[SheetSix] = None,
                               sheet5 : Option[SheetSeven] = None)

final case class BattleplanMessage(unk1 : Long,
                                   unk2 : String,
                                   unk3 : Int,
                                   unk4 : List[BattleDiagram])
  extends PlanetSideGamePacket {
  type Packet = BattleplanMessage
  def opcode = GamePacketOpcode.BattleplanMessage
  def encode = BattleplanMessage.encode(this)
}

object BattelplanDiagram {
  def sheet1(unk1 : Float, unk2 : Int) : BattleDiagram =
    BattleDiagram(Some(SheetOne(unk1, unk2)))

  def sheet2(unk1 : Float, unk2 : Float) : BattleDiagram =
    BattleDiagram(None, Some(SheetTwo(unk1, unk2)))

  def sheet3(unk1 : Float, unk2 : Float, unk3 : Float, unk4 : Int) : BattleDiagram =
    BattleDiagram(None, None, Some(SheetFive(unk1, unk2, unk3, unk4)))

  def sheet4(unk1 : Float, unk2 : Float, unk3 : Int, unk4 : Int, unk5 : String) : BattleDiagram =
    BattleDiagram(None, None, None, Some(SheetSix(unk1, unk2, unk3, unk4, unk5)))

  def sheet5(unk1 : Int) : BattleDiagram =
    BattleDiagram(None, None, None, None, Some(SheetSeven(unk1)))
}

object BattleplanMessage extends Marshallable[BattleplanMessage] {
  private final case class BattleDiagramLayer(diagram : BattleDiagram,
                                              next : Option[BattleDiagramLayer])

  private val plan1_codec : Codec[SheetOne] = ( //size: 8; pad: +0
    ("unk1" | newcodecs.q_float(0.0, 16.0, 5)) ::
      ("unk2" | uintL(3))
  ).as[SheetOne]

  private val plan2_codec : Codec[SheetTwo] = ( //size: 22; pad: +2
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11))
    ).as[SheetTwo]

  private val plan5_codec : Codec[SheetFive] = ( //size: 44; pad: +4
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk3" | newcodecs.q_float(1024.0, 0.0, 11)) ::
      ("unk4" | uintL(11))
    ).as[SheetFive]

  private def plan6_codec(pad : Int) : Codec[SheetSix] = ( //size: 31 + string.length.field + string.length * 16 + padding; pad: value resets
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk3" | uintL(3)) ::
      ("unk4" | uintL(6)) ::
      ("unk5" | PacketHelpers.encodedWideStringAligned( (pad + 1) % 8 ))
    ).as[SheetSix]

  private val plan7_codec : Codec[SheetSeven] = ("unk" | uintL(6)).as[SheetSeven] // size: 6; pad: +2

  private def diagram_codec(plan : Int, pad : Int) : Codec[BattleDiagram] = (
    conditional(plan == 1, plan1_codec) ::
      conditional(plan == 2, plan2_codec) ::
      conditional(plan == 5, plan5_codec) ::
      conditional(plan == 6, plan6_codec(pad)) ::
      conditional(plan == 7, plan7_codec)
    ).exmap[BattleDiagram] (
    {
      case None :: None :: None :: None :: None :: HNil =>
        Attempt.failure(Err(s"unknown sheet number $plan"))

      case a :: b :: c :: d :: e :: HNil =>
        Attempt.successful(BattleDiagram(a, b, c, d, e))
    },
    {
      case BattleDiagram(Some(sheet), _, _, _, _) =>
        Attempt.successful(Some(sheet) :: None :: None :: None :: None :: HNil)

      case BattleDiagram(None, Some(sheet), _, _, _) =>
        Attempt.successful(None :: Some(sheet) :: None :: None :: None :: HNil)

      case BattleDiagram(None, None, Some(sheet), _, _) =>
        Attempt.successful(None :: None :: Some(sheet) :: None :: None :: HNil)

      case BattleDiagram(None, None, None, Some(sheet), _) =>
        Attempt.successful(None :: None :: None :: Some(sheet) :: None :: HNil)

      case BattleDiagram(None, None, None, None, Some(sheet)) =>
        Attempt.successful(None :: None :: None :: None :: Some(sheet) :: HNil)

      case BattleDiagram(None, None, None, None, None) =>
        Attempt.failure(Err("can not deal with blank sheet"))
    }
  )

  private def parse_diagrams_codec(remaining : Int, pad : Int = 0) : Codec[BattleDiagramLayer] = (
    uint4L >>:~ { plan =>
      ("diagram" | diagram_codec(plan, pad)) ::
        conditional(remaining > 1,
          "next" | parse_diagrams_codec(
            remaining - 1,
            pad + (if(plan == 2 || plan == 7) { 2 } else if(plan == 5) { 4 } else if(plan == 6) { -pad } else { 0 })
          )
        )
    }).exmap[BattleDiagramLayer] (
    {
      case _ :: diagram :: next :: HNil =>
        Attempt.successful(BattleDiagramLayer(diagram, next))
    },
    {
      case BattleDiagramLayer(BattleDiagram(Some(sheet), _, _, _, _), next) =>
        Attempt.successful(1 :: BattleDiagram(Some(sheet)) :: next :: HNil)

      case BattleDiagramLayer(BattleDiagram(None, Some(sheet), _, _, _), next) =>
        Attempt.successful(2 :: BattleDiagram(None, Some(sheet)) :: next :: HNil)

      case BattleDiagramLayer(BattleDiagram(None, None, Some(sheet), _, _), next) =>
        Attempt.successful(5 :: BattleDiagram(None, None, Some(sheet)) :: next :: HNil)

      case BattleDiagramLayer(BattleDiagram(None, None, None, Some(sheet), _), next) =>
        Attempt.successful(6 :: BattleDiagram(None, None, None, Some(sheet)) :: next :: HNil)

      case BattleDiagramLayer(BattleDiagram(None, None, None, None, Some(sheet)), next) =>
        Attempt.successful(7 :: BattleDiagram(None, None, None, None, Some(sheet)) :: next :: HNil)
    }
  )

  import scala.collection.mutable.ListBuffer
  private def rollDiagramLayers(element : BattleDiagramLayer, list : ListBuffer[BattleDiagram]) : Unit = {
    list += element.diagram
    if(element.next.isDefined)
      rollDiagramLayers(element.next.get, list)
  }

  private def unrollDiagramLayers(revIter : Iterator[BattleDiagram], layers : Option[BattleDiagramLayer] = None) : Option[BattleDiagramLayer] = {
    if(!revIter.hasNext)
      return layers
    val elem : BattleDiagram = revIter.next
    unrollDiagramLayers(revIter, Some(BattleDiagramLayer(elem, layers)))
  }

  implicit val codec : Codec[BattleplanMessage] = (
    ("unk1" | uint32L) ::
      ("unk2" | PacketHelpers.encodedWideString) ::
      ("unk3" | uint16L) ::
      (uint8L >>:~ { count =>
        conditional(count > 0, parse_diagrams_codec(count)).hlist
      })
    ).exmap[BattleplanMessage] (
    {
      case unk1 :: unk2 :: unk3 :: _ :: diagramLayers :: HNil =>
        val list : ListBuffer[BattleDiagram] = new ListBuffer()
        if(diagramLayers.isDefined)
          rollDiagramLayers(diagramLayers.get, list)
        Attempt.successful(BattleplanMessage(unk1, unk2, unk3, list.toList))
    },
    {
      case BattleplanMessage(unk1, unk2, unk3, diagrams) =>
        val layersOpt = unrollDiagramLayers(diagrams.reverseIterator)
        Attempt.successful(unk1 :: unk2 :: unk3 :: diagrams.size :: layersOpt :: HNil)
    }
  )
}
