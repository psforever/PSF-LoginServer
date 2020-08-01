// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Dispatched by the server to alert the client about custom permissions in different zones.<br>
  * <br>
  * The primarily way this packet was used on Gemini Live was to restrict weapons per zone.
  * The Battle Island restrictions, for example, were enforced by these properties.<br>
  * <br>
  * Exploration:<br>
  * What else can you do with this packet?
  * @param list a `List` defining scopes for the targets of internal property changes
  */
final case class PropertyOverrideMessage(list: List[PropertyOverrideMessage.GamePropertyScope])
    extends PlanetSideGamePacket {
  type Packet = PropertyOverrideMessage
  def opcode = GamePacketOpcode.PropertyOverrideMessage
  def encode = PropertyOverrideMessage.encode(this)
}

object GamePropertyTarget {

  /**
    * A target value referring to general game properties.
    * In the context of this `GamePacket`, usually scoped to a "global" zone.
    */
  final val game_properties: Int = 343

  /**
    * Overloaded constructor for defining a target for a single paired key and value (String -> String).
    * @param target the target
    * @param kv the key-value pair
    * @return a `PropertyOverrideMessage.GamePropertyTarget` association object
    */
  def apply(target: Int, kv: (String, String)): PropertyOverrideMessage.GamePropertyTarget = {
    PropertyOverrideMessage.GamePropertyTarget(target, PropertyOverrideMessage.GameProperty(kv._1, kv._2) :: Nil)
  }

  /**
    * Overloaded constructor for defining a target for a list of paired key and value.
    * @param target the target
    * @param list a `List` of key-value pairs
    * @return a `PropertyOverrideMessage.GamePropertyTarget` association object
    */
  def apply(target: Int, list: List[(String, String)]): PropertyOverrideMessage.GamePropertyTarget = {
    PropertyOverrideMessage.GamePropertyTarget(
      target,
      list.map({
        case (key, value) =>
          PropertyOverrideMessage.GameProperty(key, value)
      })
    )
  }
}

object GamePropertyScope {

  /**
    * Overloaded constructor for defining a scope for the contained property.
    * @param zone a game zone where this property is valid
    * @param property a targeted key-value pair
    * @return a `PropertyOverrideMessage.GamePropertyScope` association object
    */
  def apply(
      zone: Int,
      property: PropertyOverrideMessage.GamePropertyTarget
  ): PropertyOverrideMessage.GamePropertyScope = {
    PropertyOverrideMessage.GamePropertyScope(zone, property :: Nil)
  }

  /**
    * Overloaded constructor for defining a scope for the contained properties.
    * @param zone a game zone where this property is valid
    * @param list a `List` of targeted key-value pairs
    * @return a `PropertyOverrideMessage.GamePropertyScope` association object
    */
  def apply(
      zone: Int,
      list: List[PropertyOverrideMessage.GamePropertyTarget]
  ): PropertyOverrideMessage.GamePropertyScope = {
    PropertyOverrideMessage.GamePropertyScope(zone, list)
  }
}

object PropertyOverrideMessage extends Marshallable[PropertyOverrideMessage] {

  /**
    * A wrapper class for the key-value pair.
    * Another class's overloading allows this to be parsed in a format `field1 -> field2` slightly more idiomatic to pairs.
    * @param field1 usually the "key;"
    *               occasionally, the only param
    * @param field2 the "value"
    */
  final case class GameProperty(field1: String, field2: String)

  /**
    * The association between a target and the properties that affect it.
    *
    * @param target what game object is affected by these properties
    * @param list   the properties
    * @see `ObjectClass`
    */
  final case class GamePropertyTarget(target: Int, list: Seq[GameProperty])

  /**
    * The association between a continent/zone and how game objects are affected differently in that region.
    *
    * @param zone the continent/zone number;
    *             0 refers to server-wide properties
    * @param list the target and its property changes
    */
  final case class GamePropertyScope(zone: Int, list: Seq[GamePropertyTarget])

  /**
    * Overloaded constructor for defining a single region where object properties are to be changed.
    * @param list a list of regions, objects, and changed properties
    * @return a `PropertyOverrideMessage` object
    */
  def apply(list: PropertyOverrideMessage.GamePropertyScope): PropertyOverrideMessage = {
    PropertyOverrideMessage(list :: Nil)
  }

  /**
    * `Codec` for two strings containing a key-value pair, with the key being padded.
    * @param n the padding of the first `String`
    * @return a `GameProperty` object
    */
  private def value_pair_aligned_codec(n: Int): Codec[GameProperty] =
    (
      ("field1" | PacketHelpers.encodedStringAligned(n)) ::
        ("field2" | PacketHelpers.encodedString)
    ).as[GameProperty]

  /**
    * `Codec` for two strings containing a key-value pair.
    */
  private val value_pair_codec: Codec[GameProperty] = (
    ("field1" | PacketHelpers.encodedString) ::
      ("field2" | PacketHelpers.encodedString)
  ).as[GameProperty]

  /**
    * `Codec` for defining the target and switching between and concatenating different key-value pair `Codec`s.
    * @param n the padding of the first key in the first entry of the contents
    * @return a `GamePropertyTarget` object
    */
  private def game_property_target_codec(n: Int): Codec[GamePropertyTarget] =
    (
      ("target" | uintL(11)) ::
        (uint16L >>:~ { len =>
        conditional(len > 0, value_pair_aligned_codec(n)) ::
          conditional(len > 1, PacketHelpers.listOfNSized((len - 1).toLong, value_pair_codec))
      })
    ).xmap[GamePropertyTarget](
      {
        case target :: _ :: None :: None :: HNil => //unlikely
          GamePropertyTarget(target, Nil)

        case target :: _ :: Some(first) :: None :: HNil =>
          GamePropertyTarget(target, first :: Nil)

        case target :: _ :: Some(first) :: Some(other) :: HNil =>
          GamePropertyTarget(target, first +: other)
      },
      {
        case GamePropertyTarget(target, list) =>
          val (first, other) = list match {
            case ((f: GameProperty) +: (rest: List[GameProperty])) => (Some(f), Some(rest))
            case (f: GameProperty) +: Nil                          => (Some(f), None)
            case Nil                                               => (None, None) //unlikely
          }
          target :: list.length :: first :: other :: HNil
      }
    )

  /**
    * `Codec` for defining the scope and switching between and concatenating different alignments for the target `Codec`.<br>
    * <br>
    * For every first target entry of a scope, the leading property string will incur the displacement of two 11-bit fields.
    * That property will be byte-aligned by two bits.
    * For every subsequent scope, the leading property will only incur the displacement of a single 11-bit field.
    * These properties will be byte-aligned by five bits.
    */
  private val game_property_scope_codec: Codec[GamePropertyScope] = (
    ("zone" | uint16L) ::
      (uintL(11) >>:~ { len =>
      conditional(len > 0, game_property_target_codec(2)) ::
        conditional(len > 1, PacketHelpers.listOfNSized((len - 1).toLong, game_property_target_codec(5)))
    })
  ).xmap[GamePropertyScope](
    {
      case zone :: _ :: None :: None :: HNil => //unlikely
        GamePropertyScope(zone, Nil)

      case zone :: _ :: Some(first) :: None :: HNil =>
        GamePropertyScope(zone, first :: Nil)

      case zone :: _ :: Some(first) :: Some(other) :: HNil =>
        GamePropertyScope(zone, first +: other)
    },
    {
      case GamePropertyScope(zone, list) =>
        val (first, other) = list match {
          case (f: GamePropertyTarget) +: (rest: List[GamePropertyTarget]) => (Some(f), Some(rest))
          case (f: GamePropertyTarget) +: Nil                              => (Some(f), None)
          case Nil                                                         => (None, None) //unlikely
        }
        zone :: list.length :: first :: other :: HNil
    }
  )

  implicit val codec: Codec[PropertyOverrideMessage] =
    listOfN(uint16L, game_property_scope_codec).as[PropertyOverrideMessage]
}
