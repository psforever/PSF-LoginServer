// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class PropertyOverrideMessage(list : List[PropertyOverrideMessage.GamePropertyScope])
  extends PlanetSideGamePacket {
  type Packet = PropertyOverrideMessage
  def opcode = GamePacketOpcode.PropertyOverrideMessage
  def encode = PropertyOverrideMessage.encode(this)
}

object GamePropertyTarget {
  final val game_properties : Int = 343

  def apply(target : Int) : PropertyOverrideMessage.GamePropertyTarget = {
    PropertyOverrideMessage.GamePropertyTarget(target, Nil)
  }

  def apply(target : Int, kv : (String, String)) : PropertyOverrideMessage.GamePropertyTarget = {
    PropertyOverrideMessage.GamePropertyTarget(target, PropertyOverrideMessage.GamePropertyValues(kv._1, kv._2) :: Nil)
  }

  def apply(target : Int, list : List[(String, String)]) : PropertyOverrideMessage.GamePropertyTarget = {
    PropertyOverrideMessage.GamePropertyTarget(target, list.map({
      case(key, value) =>
        PropertyOverrideMessage.GamePropertyValues(key, value)
    }))
  }
}

object GamePropertyScope {
  def apply(zone : Int, list : PropertyOverrideMessage.GamePropertyTarget) : PropertyOverrideMessage.GamePropertyScope = {
    PropertyOverrideMessage.GamePropertyScope(zone, list :: Nil)
  }

  def apply(zone : Int, list : List[PropertyOverrideMessage.GamePropertyTarget]) : PropertyOverrideMessage.GamePropertyScope = {
    PropertyOverrideMessage.GamePropertyScope(zone, list)
  }
}

object PropertyOverrideMessage extends Marshallable[PropertyOverrideMessage] {
  final case class GamePropertyValues(field1 : String, field2 : String)

  final case class GamePropertyTarget(target : Int, list : List[GamePropertyValues])

  final case class GamePropertyScope(zone : Int, list : List[GamePropertyTarget])

  def apply(list : PropertyOverrideMessage.GamePropertyScope) : PropertyOverrideMessage = {
    PropertyOverrideMessage(list :: Nil)
  }

  private def value_pair_aligned_codec(n : Int) : Codec[GamePropertyValues] = (
    ("field1" | PacketHelpers.encodedStringAligned(n)) ::
      ("field2" | PacketHelpers.encodedString)
    ).as[GamePropertyValues]

  private val value_pair_codec : Codec[GamePropertyValues] = (
    ("field1" | PacketHelpers.encodedString) ::
      ("field2" | PacketHelpers.encodedString)
  ).as[GamePropertyValues]

  private def game_property_target_codec(n : Int) : Codec[GamePropertyTarget] = (
    ("target" | uintL(11)) ::
      (uint16L >>:~ { len =>
        conditional(len > 0, value_pair_aligned_codec(n)) ::
          conditional(len > 1, PacketHelpers.listOfNSized((len - 1).toLong, value_pair_codec))
      })
    ).xmap[GamePropertyTarget] (
    {
      case target :: _ :: Some(first) :: None :: HNil =>
        GamePropertyTarget(target, first :: Nil)

      case target :: _ :: Some(first) :: Some(other) :: HNil =>
        GamePropertyTarget(target, first +: other)
    },
    {
      case GamePropertyTarget(target, list) =>
        val (first, other) = list match {
          case ((f : GamePropertyValues) +: (rest : List[GamePropertyValues])) => (Some(f), Some(rest))
          case (f : GamePropertyValues) +: Nil => (Some(f), None)
          case Nil => (None, None)
        }
        target :: list.length :: first :: other :: HNil
    }
  )

  private val game_property_scope_codec : Codec[GamePropertyScope] = (
    ("zone" | uint16L) ::
      (uintL(11) >>:~ { len =>
        conditional(len > 0, game_property_target_codec(2)) ::
          conditional(len > 1, PacketHelpers.listOfNSized((len - 1).toLong, game_property_target_codec(5)))
    })
    ).xmap[GamePropertyScope] (
    {
      case zone :: _ :: Some(first) :: None :: HNil =>
        GamePropertyScope(zone, first :: Nil)

      case zone :: _ :: Some(first) :: Some(other) :: HNil =>
        GamePropertyScope(zone, first +: other)
    },
    {
      case GamePropertyScope(zone, list) =>
        val (first, other) = list match {
          case ((f : GamePropertyTarget) +: (rest : List[GamePropertyTarget])) => (Some(f), Some(rest))
          case (f : GamePropertyTarget) +: Nil => (Some(f), None)
          case Nil => (None, None)
        }
        zone :: list.length :: first :: other :: HNil
    }
  )

  implicit val codec : Codec[PropertyOverrideMessage] =
    listOfN(uint16L, game_property_scope_codec).as[PropertyOverrideMessage]
}
