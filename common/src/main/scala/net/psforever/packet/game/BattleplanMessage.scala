// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A common ancestor of all the different "sheets" used to keep track of the data.
  */
sealed trait DiagramSheet

/**
  * na
  * @param unk1 na
  * @param unk2 na
  */
final case class SheetOne(unk1 : Float,
                          unk2 : Int) extends DiagramSheet

/**
  * na
  * @param unk1 na
  * @param unk2 na
  */
final case class SheetTwo(unk1 : Float,
                          unk2 : Float) extends DiagramSheet

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  */
final case class SheetFive(unk1 : Float,
                           unk2 : Float,
                           unk3 : Float,
                           unk4 : Int) extends DiagramSheet

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param unk5 na
  */
final case class SheetSix(unk1 : Float,
                          unk2 : Float,
                          unk3 : Int,
                          unk4 : Int,
                          unk5 : String) extends DiagramSheet

/**
  * na
  * @param unk na
  */
final case class SheetSeven(unk : Int) extends DiagramSheet

/**
  * na
  * @param pageNum a hint to kind of data stored
  * @param sheet the data
  */
final case class BattleDiagram(pageNum : Int,
                               sheet : Option[DiagramSheet] = None)

/**
  * na
  * @param unk1 na
  * @param mastermind the player who contributed this battle plan
  * @param unk2 na
  * @param diagrams a list of the individual `BattleDiagram`s that compose this plan
  */
final case class BattleplanMessage(unk1 : Long,
                                   mastermind : String,
                                   unk2 : Int,
                                   diagrams : List[BattleDiagram])
  extends PlanetSideGamePacket {
  type Packet = BattleplanMessage
  def opcode = GamePacketOpcode.BattleplanMessage
  def encode = BattleplanMessage.encode(this)
}

object BattelplanDiagram {
  /**
    * Create a `BattleDiagram` object containing `SheetOne` data.
    * @param unk1 na
    * @param unk2 na
    * @return a `BattleDiagram` object
    */
  def sheet1(unk1 : Float, unk2 : Int) : BattleDiagram =
    BattleDiagram(1, Some(SheetOne(unk1, unk2)))

  /**
    * Create a `BattleDiagram` object containing `SheetTwo` data.
    * @param unk1 na
    * @param unk2 na
    * @return a `BattleDiagram` object
    */
  def sheet2(unk1 : Float, unk2 : Float) : BattleDiagram =
    BattleDiagram(2, Some(SheetTwo(unk1, unk2)))

  /**
    * Create a `BattleDiagram` object containing `SheetFive` data.
    * @param unk1 na
    * @param unk2 na
    * @param unk3 na
    * @param unk4 na
    * @return a `BattleDiagram` object
    */
  def sheet5(unk1 : Float, unk2 : Float, unk3 : Float, unk4 : Int) : BattleDiagram =
    BattleDiagram(5, Some(SheetFive(unk1, unk2, unk3, unk4)))

  /**
    * Create a `BattleDiagram` object containing `SheetSix` data.
    * @param unk1 na
    * @param unk2 na
    * @param unk3 na
    * @param unk4 na
    * @param unk5 na
    * @return a `BattleDiagram` object
    */
  def sheet6(unk1 : Float, unk2 : Float, unk3 : Int, unk4 : Int, unk5 : String) : BattleDiagram =
    BattleDiagram(6, Some(SheetSix(unk1, unk2, unk3, unk4, unk5)))

  /**
    * Create a `BattleDiagram` object containing `SheetSeven` data.
    * @param unk na
    * @return a `BattleDiagram` object
    */
  def sheet7(unk : Int) : BattleDiagram =
    BattleDiagram(7, Some(SheetSeven(unk)))
}

object BattleplanMessage extends Marshallable[BattleplanMessage] {

  /**
    * An intermediary object intended to temporarily store `BattleDiagram` objects.<br>
    * <br>
    * This hidden object is arranged like a linked list;
    * but, later, it is converted into an accessible formal `List` of `BattleDiagram` objects during decoding;
    * likewise, during the encoding process, the `List` is transformed back into a linked list structure.
    * `Scala`'s own linked list `Collection` is deprecated, without substitution, so this custom one must be used.
    * @param diagram the contained `BattleDiagram` with the sheet that maintains the data
    * @param next the next `BattleDiagramLayer`, if any, arranging into a linked list
    */
  private final case class BattleDiagramLayer(diagram : BattleDiagram,
                                              next : Option[BattleDiagramLayer])

  /**
    * Parse data into a `SheetOne` object.
    */
  private val plan1_codec : Codec[SheetOne] = ( //size: 8; pad: +0
    ("unk1" | newcodecs.q_float(0.0, 16.0, 5)) ::
      ("unk2" | uintL(3))
  ).as[SheetOne]

  /**
    * Parse data into a `SheetTwo` object.
    */
  private val plan2_codec : Codec[SheetTwo] = ( //size: 22; pad: +2
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11))
    ).as[SheetTwo]

  /**
    * Parse data into a `SheetFive` object.
    */
  private val plan5_codec : Codec[SheetFive] = ( //size: 44; pad: +4
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk3" | newcodecs.q_float(1024.0, 0.0, 11)) ::
      ("unk4" | uintL(11))
    ).as[SheetFive]

  /**
    * Parse data into a `SheetSix` object.
    * @param pad the current padding for the `String` entry
    */
  private def plan6_codec(pad : Int) : Codec[SheetSix] = ( //size: 31 + string.length.field + string.length * 16 + padding; pad: value resets
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk3" | uintL(3)) ::
      ("unk4" | uintL(6)) ::
      ("unk5" | PacketHelpers.encodedWideStringAligned( (pad + 1) % 8 ))
    ).as[SheetSix]

  /**
    * Parse data into a `SheetSeven` object.
    */
  private val plan7_codec : Codec[SheetSeven] = ("unk" | uintL(6)).as[SheetSeven] // size: 6; pad: +2

  /**
    * Switch between different patterns to create a `BattleDiagram` for the following data.
    * @param plan a hint to help parse the following data
    * @param pad the current padding for any `String` entry stored within the parsed elements;
    *            when `plan == 6`, `plan6_codec` utilizes this value
    * @return a `BattleDiagram` object
    */
  private def diagram_codec(plan : Int, pad : Int) : Codec[BattleDiagram] = (
    conditional(plan == 1, plan1_codec) ::
      conditional(plan == 2, plan2_codec) ::
      conditional(plan == 5, plan5_codec) ::
      conditional(plan == 6, plan6_codec(pad)) ::
      conditional(plan == 7, plan7_codec)
    ).exmap[BattleDiagram] (
    {
      case Some(sheet) :: None :: None :: None :: None :: HNil =>
        Attempt.successful(BattleDiagram(plan, Some(sheet)))

      case None :: Some(sheet) :: None :: None :: None :: HNil =>
        Attempt.successful(BattleDiagram(plan, Some(sheet)))

      case None :: None :: Some(sheet) :: None :: None :: HNil =>
        Attempt.successful(BattleDiagram(plan, Some(sheet)))

      case None :: None :: None :: Some(sheet) :: None :: HNil =>
        Attempt.successful(BattleDiagram(plan, Some(sheet)))

      case None :: None :: None :: None :: Some(sheet) :: HNil =>
        Attempt.successful(BattleDiagram(plan, Some(sheet)))

      case None :: None :: None :: None :: None :: HNil =>
        Attempt.successful(BattleDiagram(plan, None))

      case _:: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err(s"too many sheets at once for $plan"))
    },
    {
      case BattleDiagram(1, Some(sheet)) =>
        Attempt.successful(Some(sheet.asInstanceOf[SheetOne]) :: None :: None :: None :: None :: HNil)

      case BattleDiagram(2, Some(sheet)) =>
        Attempt.successful(None :: Some(sheet.asInstanceOf[SheetTwo]) :: None :: None :: None :: HNil)

      case BattleDiagram(5, Some(sheet)) =>
        Attempt.successful(None :: None :: Some(sheet.asInstanceOf[SheetFive]) :: None :: None :: HNil)

      case BattleDiagram(6, Some(sheet)) =>
        Attempt.successful(None :: None :: None :: Some(sheet.asInstanceOf[SheetSix]) :: None :: HNil)

      case BattleDiagram(7, Some(sheet)) =>
        Attempt.successful(None :: None :: None :: None :: Some(sheet.asInstanceOf[SheetSeven]) :: HNil)

      case BattleDiagram(_, None) =>
        Attempt.successful(None :: None :: None :: None :: None :: HNil)

      case BattleDiagram(n, _) =>
        Attempt.failure(Err(s"unhandled sheet number $n"))
    }
  )

  /**
    * Parse what was originally an encoded `List` of elements as a linked list of elements.
    * Maintain a `String` padding value that applies an appropriate offset value.
    * @param remaining the number of elements remaining to parse
    * @param pad the current padding for any `String` entry stored within the parsed elements;
    *            different elements add different padding offset to this field on subsequent passes
    * @return a `Codec` for `BattleDiagramLayer` objects
    */
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
      case BattleDiagramLayer(BattleDiagram(num, sheet), next) =>
        Attempt.successful(num :: BattleDiagram(num, sheet) :: next :: HNil)
    }
  )

  import scala.collection.mutable.ListBuffer
  /**
    * Transform a linked list of `BattleDiagramLayer` into a `List` of `BattleDiagram` objects.
    * @param element the current link in a chain of `BattleDiagramLayer` objects
    * @param list a `List` of extracted `BattleDiagrams`;
    *             technically, the output
    */
  private def rollDiagramLayers(element : BattleDiagramLayer, list : ListBuffer[BattleDiagram]) : Unit = {
    list += element.diagram
    if(element.next.isDefined)
      rollDiagramLayers(element.next.get, list) //tail call optimization
  }

  /**
    * Transform a `List` of `BattleDiagram` objects into a linked list of `BattleDiagramLayer` objects.
    * @param revIter a reverse `List` `Iterator` for a `List` of `BattleDiagrams`
    * @param layers the current head of a chain of `BattleDiagramLayer` objects;
    *               technically, the output
    * @return a linked list of `BattleDiagramLayer` objects
    */
  private def unrollDiagramLayers(revIter : Iterator[BattleDiagram], layers : Option[BattleDiagramLayer] = None) : Option[BattleDiagramLayer] = {
    if(!revIter.hasNext)
      return layers
    val elem : BattleDiagram = revIter.next
    unrollDiagramLayers(revIter, Some(BattleDiagramLayer(elem, layers))) //tail call optimization
  }

  implicit val codec : Codec[BattleplanMessage] = (
    ("unk1" | uint32L) ::
      ("mastermind" | PacketHelpers.encodedWideString) ::
      ("unk2" | uint16L) ::
      (uint8L >>:~ { count =>
        conditional(count > 0, "diagrams" | parse_diagrams_codec(count)).hlist
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
