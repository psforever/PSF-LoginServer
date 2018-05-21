// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

import scala.annotation.tailrec

/**
  * A `Codec` for the actions that each layer of the diagram performs.
  * `Style`, `Vertex`, `Action5`, `DrawString`, and `Action7` have additional `DiagramStroke` input data.
  */
object DiagramActionCode extends Enumeration {
  type Type = Value

  val Action0,
      Style,
      Vertex,
      Action3,
      Action4,
      Action5,
      DrawString,
      Action7,
      Action8,
      Action9,
      ActionA,
      ActionB,
      ActionC, //clear?
      ActionD, //opposite of clear?
      StartDrawing,
      StopDrawing
      = Value //TODO replace all these with descriptive words

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint4L)
}

/**
  * A common ancestor of all the different "strokes" used to keep track of the data.
  */
sealed trait DiagramStroke

/**
  * Set style properties for the line segemnt(s) to be drawn.
  * Color and thickness can not vary within a given line and will only apply to the subsequent line segments.
  * Attempting to list a change in between coordinate points will invalidate that segment.
  * @param thickness the line width in pixels;
  *                  0.0f - 16.0f;
  *                  3.0f is about normal and 0.0f is smaller than the map grid lines
  * @param color the color of the line;
  *              0 is gray (default);
  *              1 is red;
  *              2 is green;
  *              3 is blue
  */
final case class Style(thickness : Float,
                       color : Int) extends DiagramStroke

/**
  * Indicate coordinates on the tactical map.
  * Any adjacent sets of coordinates will be connected with a line segment.
  * @param x the x-coordinate of this point
  * @param y the y-coordinate of this point
  */
final case class Vertex(x : Float,
                        y : Float) extends DiagramStroke

/**
  * na
  * @param x the x-coordinate of this point
  * @param y the y-coordinate of this point
  * @param unk na;
  *            1024.0f - 0.0f
  */
final case class StrokeFive(x : Float,
                            y : Float,
                            unk : Float) extends DiagramStroke

/**
  * Draw a string message on the tactical map.
  * String messages have their own color designation and will not inherit line properties.
  * @param x the x-coordinate marking the bottom center of this message's text
  * @param y the y-coordinate marking the bottom center of this message's text
  * @param color the color of the message;
  *              0 is gray (default);
  *              1 is red;
  *              2 is green;
  *              3 is blue
  * @param channel the available "slots" in which to display messages on the map;
  *                a maximum of 16 channels/messages (0-15) are available per player;
  *                no two messages may inhabit the same channel
  * @param message the text to display
  */
final case class DrawString(x : Float,
                            y : Float,
                            color : Int,
                            channel : Int,
                            message : String) extends DiagramStroke

/**
  * na
  * @param unk na
  */
final case class StrokeSeven(unk : Int) extends DiagramStroke

/**
  * A particular instruction in the rendering of this battleplan's diagram entry.
  * @param action the behavior of this stroke;
  *               a hint to the kind of stroke data stored, if at all, and how to use it or incorporate prior data
  * @param stroke the data;
  *               defaults to `None`
  */
final case class BattleDiagramAction(action : DiagramActionCode.Value,
                                     stroke : Option[DiagramStroke] = None)

/**
  * Share drawn images and words on the tactical map among a group of players.<br>
  * <br>
  * Each packet usually contains a small portion of an image, herein called a "diagram."
  * `BattleplanMessage` packets are accumulative towards a full diagram.
  * Moreover, rather than the `player_name`, each diagram is associated on a client by the `char_id` field.
  * Only squad leaders and platoon leaders can draw on the map and share with other players in their squad or platoon.<br>
  * <br>
  * To start drawing, a would-be artist must have all clients who will receive their diagrams acknowledge a `StartDrawing` action.
  * The `char_id` with this `StartDrawing` will associate all diagrams submitted with the same `char_id`'s portfolio.
  * Multiple portfolio definitions may exist on a client at a given time and each will manage their own diagrams.
  * When a given portfolio submits a `StopDrawing` action that is received, the previous diagrams associated with it will be cleared.
  * That `char_id` will no longer accept diagrams on that client.
  * Other portfolios will continue to accept diagrams as initialized.
  * When no portfolios are being accepted, the "Toggle -> Battleplan" button on that client's tactical map will be disabled.
  * When there is at least one portfolio accepted, the "Battleplan" button will be functional and can be toggled.<br>
  * <br>
  * To construct line segments, chain `StrokeTwo` diagrams in the given packet entry.
  * Each defined point will act like a successive vertex in a chain of segments.
  * Any non-vertex entry in between entries, e.g., a change of line color, will break the chain of line segments.
  * For example:<br>
  * RED-A-B-C will construct red lines segments A-B and B-C.<br>
  * RED-A-B-GREEN-C will only construct a red line segement A-B.<br>
  * RED-A-B-GREEN-C-D will construct a red line segement A-B and a green line segment C-D.<br>
  * (Default line color, if none is declared specifically, is gray.)<br>
  * <br>
  * To construct a message, define a point to act as the center baseline for the text.
  * The message will be written above and outwards from that point.
  * Messages do not carry properties over from line segments - they set their own color and do not have line thickness.
  * Any single portfolio may have only fifteen messages written to the tactical map at a time.
  * @param char_id na;
  *                same as in `CharacterInfoMessage`
  * @param player_name the player who contributed this battle plan
  * @param zone_id on which continent the battle plan will be overlaid;
  *                can identify as "no zone" 0 when performing instructions not specific to drawing
  * @param diagrams a list of the itemized actions that will construct this plan or are used to modify the plan
  */
final case class BattleplanMessage(char_id : Long,
                                   player_name : String,
                                   zone_id : Int,
                                   diagrams : List[BattleDiagramAction])
  extends PlanetSideGamePacket {
  type Packet = BattleplanMessage
  def opcode = GamePacketOpcode.BattleplanMessage
  def encode = BattleplanMessage.encode(this)
}

object BattleDiagramAction {
  /**
    * Create a `BattleDiagramAction` object containing `StrokeOne` data.
    * @param thickness the line width in pixels
    * @param color the color of the line
    * @return a `BattleDiagramAction` object
    */
  def style(thickness : Float, color : Int) : BattleDiagramAction =
    BattleDiagramAction(DiagramActionCode.Style, Some(Style(thickness, color)))

  /**
    * Create a `BattleDiagramAction` object containing `StrokeTwo` vertex data.
    * @param x the x-coordinate of this point
    * @param y the y-coordinate of this point
    * @return a `BattleDiagramAction` object
    */
  def vertex(x : Float, y : Float) : BattleDiagramAction =
    BattleDiagramAction(DiagramActionCode.Vertex, Some(Vertex(x, y)))

  /**
    * Create a `BattleDiagramAction` object containing `StrokeFive` data.
    * @param x the x-coordinate of this point
    * @param y the y-coordinate of this point
    * @param unk na
    * @return a `BattleDiagramAction` object
    */
  def stroke5(x : Float, y : Float, unk : Float) : BattleDiagramAction =
    BattleDiagramAction(DiagramActionCode.Action5, Some(StrokeFive(x, y, unk)))

  /**
    * Create a `BattleDiagramAction` object containing `StrokeSix` data.
    * @param x the x-coordinate marking the bottom center of this message's text
    * @param y the y-coordinate marking the bottom center of this message's text
    * @param color the color of the message
    * @param channel the available "slots" in which to display messages on the map
    * @param message the text to display
    */
  def drawString(x : Float, y : Float, color : Int, channel : Int, message : String) : BattleDiagramAction =
    BattleDiagramAction(DiagramActionCode.DrawString, Some(DrawString(x, y, color, channel, message)))

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
    * This hidden object is arranged like a linked list.
    * During the decoding process, it is converted into an accessible formal `List`.
    * During the encoding process, the `List` is transformed back into a linked list structure.
    * Scala's own linked list `Collection` is deprecated, without substitution, so this custom one shall be used.
    * @param diagram the contained object that maintains the data
    * @param next the next `BattleDiagramChain`, if any
    * @see scala.collection.mutable.LinkedList&#60;E&#62;
    * @see java.util.LinkedList&#60;E&#62;
    */
  private final case class BattleDiagramChain(diagram : BattleDiagramAction,
                                              next : Option[BattleDiagramChain])

  /**
    * Parse data into a `Style` object.
    */
  private val plan1_codec : Codec[Style] = ( //size: 8 (12)
    ("thickness" | newcodecs.q_float(16.0, 0.0, 5)) ::
      ("color" | uintL(3))
  ).as[Style]

  /**
    * Parse data into a `Vertex` object.
    */
  private val plan2_codec : Codec[Vertex] = ( //size: 22 (26)
    ("x" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("y" | newcodecs.q_float(-4096.0, 12288.0, 11))
    ).as[Vertex]

  /**
    * Parse data into a `StrokeFive` object.
    */
  private val plan5_codec : Codec[StrokeFive] = ( //size: 33 (37)
    ("unk1" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk2" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("unk3" | newcodecs.q_float(1024.0, 0.0, 11))
    ).as[StrokeFive]

  /**
    * Parse data into a `DrawString` object.<br>
    * If we are on a byte boundary upon starting this entry, our message is padded by `5u` (always).
    * If we are not on a byte boundary, we must use our current offset and this size (`31u + 4u`) to calculate the padding value.
    * @param padOffset the current padding value for the `String` entry
    */
  private def plan6_codec(padOffset : Int) : Codec[DrawString] = ( //size: irrelevant, pad value resets
    ("x" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("y" | newcodecs.q_float(-4096.0, 12288.0, 11)) ::
      ("color" | uintL(3)) ::
      ("font_size" | uintL(6)) ::
      ("message" | PacketHelpers.encodedWideStringAligned( if(padOffset % 8 == 0) { 5 } else { 8 - (padOffset + 35) % 8 } ))
    ).as[DrawString]

  /**
    * Parse data into a `StrokeSeven` object.
    */
  private val plan7_codec : Codec[StrokeSeven] = ("unk" | uintL(6)).as[StrokeSeven] // size: 6 (10)

  /**
    * Switch between different patterns to create a `BattleDiagramAction` for the following data.
    * @param plan a hint to help parse the following data
    * @param pad the current padding for any `String` entry stored within the parsed elements;
    *            when `plan == 6`, `plan6_codec` utilizes this value
    * @return a `BattleDiagramAction` object
    */
  private def diagram_codec(plan : DiagramActionCode.Value, pad : Int) : Codec[BattleDiagramAction] = (
    conditional(plan == DiagramActionCode.Style, plan1_codec) ::
      conditional(plan == DiagramActionCode.Vertex, plan2_codec) ::
      conditional(plan == DiagramActionCode.Action5, plan5_codec) ::
      conditional(plan == DiagramActionCode.DrawString, plan6_codec(pad)) ::
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
      case BattleDiagramAction(DiagramActionCode.Style, Some(stroke)) =>
        Attempt.successful(Some(stroke.asInstanceOf[Style]) :: None :: None :: None :: None :: HNil)

      case BattleDiagramAction(DiagramActionCode.Vertex, Some(stroke)) =>
        Attempt.successful(None :: Some(stroke.asInstanceOf[Vertex]) :: None :: None :: None :: HNil)

      case BattleDiagramAction(DiagramActionCode.Action5, Some(stroke)) =>
        Attempt.successful(None :: None :: Some(stroke.asInstanceOf[StrokeFive]) :: None :: None :: HNil)

      case BattleDiagramAction(DiagramActionCode.DrawString, Some(stroke)) =>
        Attempt.successful(None :: None :: None :: Some(stroke.asInstanceOf[DrawString]) :: None :: HNil)

      case BattleDiagramAction(DiagramActionCode.Action7, Some(stroke)) =>
        Attempt.successful(None :: None :: None :: None :: Some(stroke.asInstanceOf[StrokeSeven]) :: HNil)

      case BattleDiagramAction(_, None) =>
        Attempt.successful(None :: None :: None :: None :: None :: HNil)

      case BattleDiagramAction(n, _) =>
        Attempt.failure(Err(s"unhandled stroke action number $n"))
    }
  )

  /**
    * Parse diagram instructions as a linked list.
    * Maintain a `String` padding value that applies an appropriate offset value regardless of where in the elements it is required.
    * @param remaining the number of elements remaining to parse
    * @param padOffset the current padding for any `String` entry stored within the parsed elements;
    *                  different elements add different padding offset to this field on subsequent passes
    * @return a `Codec` for `BattleDiagramChain` segments
    */
  private def parse_diagrams_codec(remaining : Int, padOffset : Int = 0) : Codec[BattleDiagramChain] = (
    DiagramActionCode.codec >>:~ { plan =>
      ("diagram" | diagram_codec(plan, padOffset)) ::
        conditional(remaining > 1,
          "next" | parse_diagrams_codec(
            remaining - 1,
            padOffset + (if(plan == DiagramActionCode.DrawString) { -padOffset } else if(plan == DiagramActionCode.Action5) { 37 } else if(plan == DiagramActionCode.Vertex) { 26 } else if(plan == DiagramActionCode.Style) { 12 } else if(plan == DiagramActionCode.Action7) { 10 } else { 4 })
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
  @tailrec private def rollDiagramLayers(element : Option[BattleDiagramChain], list : ListBuffer[BattleDiagramAction]) : Unit = {
    if(element.nonEmpty) {
      list += element.get.diagram
      rollDiagramLayers(element.get.next, list)
    }
  }

  /**
    * Transform a `List` of `BattleDiagramAction` objects into a linked list of `BattleDiagramChain` objects.
    * @param revIter a reverse `List` `Iterator` for a `List` of `BattleDiagrams`
    * @param layers the current head of a chain of `BattleDiagramChain` objects;
    *               defaults to `None`, so does not need to be defined during the initial pass;
    *               technically, the output
    * @return a linked list of `BattleDiagramChain` objects
    */
  @tailrec private def unrollDiagramLayers(revIter : Iterator[BattleDiagramAction], layers : Option[BattleDiagramChain] = None) : Option[BattleDiagramChain] = {
    if(!revIter.hasNext) {
      layers
    }
    else {
      val elem : BattleDiagramAction = revIter.next
      unrollDiagramLayers(revIter, Some(BattleDiagramChain(elem, layers)))
    }
  }

  implicit val codec : Codec[BattleplanMessage] = (
    ("char_id" | uint32L) ::
      ("player_name" | PacketHelpers.encodedWideString) ::
      ("zone_id" | uint16L) ::
      (uint8L >>:~ { count =>
        conditional(count > 0, "diagrams" | parse_diagrams_codec(count)).hlist
      })
    ).exmap[BattleplanMessage] (
    {
      case char_id :: player :: zone_id :: _ :: diagramLayers :: HNil =>
        val list : ListBuffer[BattleDiagramAction] = new ListBuffer()
        if(diagramLayers.isDefined)
          rollDiagramLayers(diagramLayers, list)
        Attempt.successful(BattleplanMessage(char_id, player, zone_id, list.toList))
    },
    {
      case BattleplanMessage(char_id, player_name, zone_id, diagrams) =>
        val layersOpt : Option[BattleDiagramChain] = unrollDiagramLayers(diagrams.reverseIterator)
        Attempt.successful(char_id :: player_name :: zone_id :: diagrams.size :: layersOpt :: HNil)
    }
  )
}
