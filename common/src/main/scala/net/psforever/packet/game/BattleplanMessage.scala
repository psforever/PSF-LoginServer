// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A `Codec` for the actions that each layer of the diagram performs.
  * `Action1`, `Action2`, `Action5`, `Action6`, and `Action7` have additional `DiagramStroke` input data.
  */
object DiagramActionCode extends Enumeration {
  type Type = Value

  val Action0,
      Action1,
      Vertex,
      Action3,
      Action4,
      Action5,
      Action6,
      Action7,
      Action8,
      Action9,
      ActionA,
      ActionB,
      ActionC,
      ActionD,
      ActionE,
      ActionF
      = Value //TODO replace these with descriptive wording

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

/**
  * A common ancestor of all the different "strokes" used to keep track of the data.
  */
sealed trait DiagramStroke

/**
  * na
  * @param unk1 na
  * @param unk2 na
  */
final case class StrokeOne(unk1 : Float,
                           unk2 : Int) extends DiagramStroke

/**
  * Mark coordinates on the tactical map.
  * @param x the x-coordinate of this point
  * @param y the y-coordinate of this point
  */
final case class StrokeTwo(x : Float,
                           y : Float) extends DiagramStroke

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  */
final case class StrokeFive(unk1 : Float,
                            unk2 : Float,
                            unk3 : Float,
                            unk4 : Int) extends DiagramStroke

/**
  * na
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  * @param unk5 na
  */
final case class StrokeSix(unk1 : Float,
                           unk2 : Float,
                           unk3 : Int,
                           unk4 : Int,
                           unk5 : String) extends DiagramStroke

/**
  * na
  * @param unk na
  */
final case class StrokeSeven(unk : Int) extends DiagramStroke

/**
  * na
  * @param action the behavior of this stroke;
  *               a hint to the kind of stroke data stored, if at all, and how to use it or incorporate prior data
  * @param stroke the data
  */
final case class BattleDiagramAction(action : DiagramActionCode.Value,
                                     stroke : Option[DiagramStroke] = None)

/**
  * na
  * @param char_id na;
  *                same as in `CharacterInfoMessage`
  * @param player_name the player who contributed this battle plan
  * @param zone_id on which continent the battle plan will be overlaid
  * @param diagrams a list of the individual actions that compose this plan
  */
final case class BattleplanMessage(char_id : Long,
                                   player_name : String,
                                   zone_id : PlanetSideGUID,
                                   diagrams : List[BattleDiagramAction])
  extends PlanetSideGamePacket {
  type Packet = BattleplanMessage
  def opcode = GamePacketOpcode.BattleplanMessage
  def encode = BattleplanMessage.encode(this)
}

object BattleDiagramAction {
  /**
    * Create a `BattleDiagramAction` object containing `StrokeOne` data.
    * @param unk1 na
    * @param unk2 na
    * @return a `BattleDiagramAction` object
    */
  def stroke1(unk1 : Float, unk2 : Int) : BattleDiagramAction =
    BattleDiagramAction(DiagramActionCode.Action1, Some(StrokeOne(unk1, unk2)))

  /**
    * Create a `BattleDiagramAction` object containing `StrokeTwo` vertex data.
    * @param x the x-coordinate of this point
    * @param y the y-coordinate of this point
    * @return a `BattleDiagramAction` object
    */
  def vertex(x : Float, y : Float) : BattleDiagramAction =
    BattleDiagramAction(DiagramActionCode.Vertex, Some(StrokeTwo(x, y)))

  /**
    * Create a `BattleDiagramAction` object containing `StrokeFive` data.
    * @param unk1 na
    * @param unk2 na
    * @param unk3 na
    * @param unk4 na
    * @return a `BattleDiagramAction` object
    */
  def stroke5(unk1 : Float, unk2 : Float, unk3 : Float, unk4 : Int) : BattleDiagramAction =
    BattleDiagramAction(DiagramActionCode.Action5, Some(StrokeFive(unk1, unk2, unk3, unk4)))

  /**
    * Create a `BattleDiagramAction` object containing `StrokeSix` data.
    * @param unk1 na
    * @param unk2 na
    * @param unk3 na
    * @param unk4 na
    * @param unk5 na
    * @return a `BattleDiagramAction` object
    */
  def stroke6(unk1 : Float, unk2 : Float, unk3 : Int, unk4 : Int, unk5 : String) : BattleDiagramAction =
    BattleDiagramAction(DiagramActionCode.Action6, Some(StrokeSix(unk1, unk2, unk3, unk4, unk5)))

  /**
    * Create a `BattleDiagramAction` object containing `StrokeSeven` data.
    * @param unk na
    * @return a `BattleDiagramAction` object
    */
  def stroke7(unk : Int) : BattleDiagramAction =
    BattleDiagramAction(DiagramActionCode.Action7, Some(StrokeSeven(unk)))
}

object BattleplanMessage extends Marshallable[BattleplanMessage] {

  /**
    * An intermediary object intended to temporarily store `BattleDiagramAction` objects.<br>
    * <br>
    * This hidden object is arranged like a linked list;
    * but, later, it is converted into an accessible formal `List` during decoding;
    * likewise, during the encoding process, the `List` is transformed back into a linked list structure.
    * `Scala`'s own linked list `Collection` is deprecated, without substitution, so this custom one must be used.
    * @param diagram the contained object that maintains the data
    * @param next the next `BattleDiagramChain`, if any
    */
  private final case class BattleDiagramChain(diagram : BattleDiagramAction,
                                              next : Option[BattleDiagramChain])

  /**
    * Parse data into a `StrokeOne` object.
    */
  private val plan1_codec : Codec[StrokeOne] = ( //size: 8; pad: +0
    ("unk1" | newcodecs.q_float(0.0, 16.0, 5)) ::
      ("unk2" | uintL(3))
  ).as[StrokeOne]

  /**
    * Parse data into a `StrokeTwo` object.
    */
  private val plan2_codec : Codec[StrokeTwo] = ( //size: 22; pad: +2
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11))
    ).as[StrokeTwo]

  /**
    * Parse data into a `StrokeFive` object.
    */
  private val plan5_codec : Codec[StrokeFive] = ( //size: 44; pad: +4
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk3" | newcodecs.q_float(1024.0, 0.0, 11)) ::
      ("unk4" | uintL(11))
    ).as[StrokeFive]

  /**
    * Parse data into a `StrokeSix` object.
    * @param pad the current padding for the `String` entry
    */
  private def plan6_codec(pad : Int) : Codec[StrokeSix] = ( //size: 31 + string.length.field + string.length * 16 + padding; pad: value resets
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk3" | uintL(3)) ::
      ("unk4" | uintL(6)) ::
      ("unk5" | PacketHelpers.encodedWideStringAligned( (pad + 1) % 8 ))
    ).as[StrokeSix]

  /**
    * Parse data into a `StrokeSeven` object.
    */
  private val plan7_codec : Codec[StrokeSeven] = ("unk" | uintL(6)).as[StrokeSeven] // size: 6; pad: +2

  /**
    * Switch between different patterns to create a `BattleDiagramAction` for the following data.
    * @param plan a hint to help parse the following data
    * @param pad the current padding for any `String` entry stored within the parsed elements;
    *            when `plan == 6`, `plan6_codec` utilizes this value
    * @return a `BattleDiagramAction` object
    */
  private def diagram_codec(plan : DiagramActionCode.Value, pad : Int) : Codec[BattleDiagramAction] = (
    conditional(plan == DiagramActionCode.Action1, plan1_codec) ::
      conditional(plan == DiagramActionCode.Vertex, plan2_codec) ::
      conditional(plan == DiagramActionCode.Action5, plan5_codec) ::
      conditional(plan == DiagramActionCode.Action6, plan6_codec(pad)) ::
      conditional(plan == DiagramActionCode.Action7, plan7_codec)
    ).exmap[BattleDiagramAction] (
    {
      case Some(stroke) :: None :: None :: None :: None :: HNil =>
        Attempt.successful(BattleDiagramAction(plan, Some(stroke)))

      case None :: Some(stroke) :: None :: None :: None :: HNil =>
        Attempt.successful(BattleDiagramAction(plan, Some(stroke)))

      case None :: None :: Some(stroke) :: None :: None :: HNil =>
        Attempt.successful(BattleDiagramAction(plan, Some(stroke)))

      case None :: None :: None :: Some(stroke) :: None :: HNil =>
        Attempt.successful(BattleDiagramAction(plan, Some(stroke)))

      case None :: None :: None :: None :: Some(stroke) :: HNil =>
        Attempt.successful(BattleDiagramAction(plan, Some(stroke)))

      case None :: None :: None :: None :: None :: HNil =>
        Attempt.successful(BattleDiagramAction(plan, None))

      case _:: _ :: _ :: _ :: _ :: HNil =>
        Attempt.failure(Err(s"too many strokes for action $plan"))
    },
    {
      case BattleDiagramAction(DiagramActionCode.Action1, Some(stroke)) =>
        Attempt.successful(Some(stroke.asInstanceOf[StrokeOne]) :: None :: None :: None :: None :: HNil)

      case BattleDiagramAction(DiagramActionCode.Vertex, Some(stroke)) =>
        Attempt.successful(None :: Some(stroke.asInstanceOf[StrokeTwo]) :: None :: None :: None :: HNil)

      case BattleDiagramAction(DiagramActionCode.Action5, Some(stroke)) =>
        Attempt.successful(None :: None :: Some(stroke.asInstanceOf[StrokeFive]) :: None :: None :: HNil)

      case BattleDiagramAction(DiagramActionCode.Action6, Some(stroke)) =>
        Attempt.successful(None :: None :: None :: Some(stroke.asInstanceOf[StrokeSix]) :: None :: HNil)

      case BattleDiagramAction(DiagramActionCode.Action7, Some(stroke)) =>
        Attempt.successful(None :: None :: None :: None :: Some(stroke.asInstanceOf[StrokeSeven]) :: HNil)

      case BattleDiagramAction(_, None) =>
        Attempt.successful(None :: None :: None :: None :: None :: HNil)

      case BattleDiagramAction(n, _) =>
        Attempt.failure(Err(s"unhandled stroke action number $n"))
    }
  )

  /**
    * Parse what was originally an encoded `List` of elements as a linked list of elements.
    * Maintain a `String` padding value that applies an appropriate offset value.
    * @param remaining the number of elements remaining to parse
    * @param pad the current padding for any `String` entry stored within the parsed elements;
    *            different elements add different padding offset to this field on subsequent passes
    * @return a `Codec` for `BattleDiagramChain` segments
    */
  private def parse_diagrams_codec(remaining : Int, pad : Int = 0) : Codec[BattleDiagramChain] = (
    DiagramActionCode.codec >>:~ { plan =>
      ("diagram" | diagram_codec(plan, pad)) ::
        conditional(remaining > 1,
          "next" | parse_diagrams_codec(
            remaining - 1,
            pad + (if(plan == DiagramActionCode.Vertex || plan == DiagramActionCode.Action7) { 2 } else if(plan == DiagramActionCode.Action5) { 4 } else if(plan == DiagramActionCode.Action6) { -pad } else { 0 })
          )
        )
    }).exmap[BattleDiagramChain] (
    {
      case _ :: diagram :: next :: HNil =>
        Attempt.successful(BattleDiagramChain(diagram, next))
    },
    {
      case BattleDiagramChain(BattleDiagramAction(num, stroke), next) =>
        Attempt.successful(num :: BattleDiagramAction(num, stroke) :: next :: HNil)
    }
  )

  import scala.collection.mutable.ListBuffer
  /**
    * Transform a linked list of `BattleDiagramChain` into a `List` of `BattleDiagramAction` objects.
    * @param element the current link in a chain of `BattleDiagramChain` objects
    * @param list a `List` of extracted `BattleDiagrams`;
    *             technically, the output
    */
  private def rollDiagramLayers(element : BattleDiagramChain, list : ListBuffer[BattleDiagramAction]) : Unit = {
    list += element.diagram
    if(element.next.isDefined)
      rollDiagramLayers(element.next.get, list) //tail call optimization
  }

  /**
    * Transform a `List` of `BattleDiagramAction` objects into a linked list of `BattleDiagramChain` objects.
    * @param revIter a reverse `List` `Iterator` for a `List` of `BattleDiagrams`
    * @param layers the current head of a chain of `BattleDiagramChain` objects;
    *               technically, the output
    * @return a linked list of `BattleDiagramChain` objects
    */
  private def unrollDiagramLayers(revIter : Iterator[BattleDiagramAction], layers : Option[BattleDiagramChain] = None) : Option[BattleDiagramChain] = {
    if(!revIter.hasNext)
      return layers
    val elem : BattleDiagramAction = revIter.next
    unrollDiagramLayers(revIter, Some(BattleDiagramChain(elem, layers))) //tail call optimization
  }

  implicit val codec : Codec[BattleplanMessage] = (
    ("char_id" | uint32L) ::
      ("player_name" | PacketHelpers.encodedWideString) ::
      ("zone_id" | PlanetSideGUID.codec) ::
      (uint8L >>:~ { count =>
        conditional(count > 0, "diagrams" | parse_diagrams_codec(count)).hlist
      })
    ).exmap[BattleplanMessage] (
    {
      case char_id :: player :: zone_id :: _ :: diagramLayers :: HNil =>
        val list : ListBuffer[BattleDiagramAction] = new ListBuffer()
        if(diagramLayers.isDefined)
          rollDiagramLayers(diagramLayers.get, list)
        Attempt.successful(BattleplanMessage(char_id, player, zone_id, list.toList))
    },
    {
      case BattleplanMessage(char_id, player_name, zone_id, diagrams) =>
        val layersOpt : Option[BattleDiagramChain] = unrollDiagramLayers(diagrams.reverseIterator)
        Attempt.successful(char_id :: player_name :: zone_id :: diagrams.size :: layersOpt :: HNil)
    }
  )
}
