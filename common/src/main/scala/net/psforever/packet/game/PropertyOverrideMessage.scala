// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class PropertyOverrideMessage(list : List[PropertyOverrideMessage.GameProperty])
  extends PlanetSideGamePacket {
  type Packet = PropertyOverrideMessage
  def opcode = GamePacketOpcode.PropertyOverrideMessage
  def encode = PropertyOverrideMessage.encode(this)
}

object GamePropertyValues {
  def apply(field1 : String) : PropertyOverrideMessage.GamePropertyValues = {
    PropertyOverrideMessage.GamePropertyValues(field1, "")
  }

  def apply(field1 : String, field2 : String) : PropertyOverrideMessage.GamePropertyValues = {
    PropertyOverrideMessage.GamePropertyValues(field1, field2)
  }
}

object GamePropertyField {
  def apply(unk : Int) : PropertyOverrideMessage.GamePropertyField = {
    PropertyOverrideMessage.GamePropertyField(unk, Nil)
  }

  def apply(unk : Int, list : List[PropertyOverrideMessage.GamePropertyValues]) : PropertyOverrideMessage.GamePropertyField = {
    PropertyOverrideMessage.GamePropertyField(unk, list)
  }
}

object GameProperty {
  def apply(unk : Int) : PropertyOverrideMessage.GameProperty = {
    PropertyOverrideMessage.GameProperty(unk, Nil)
  }

  def apply(unk : Int, list : List[PropertyOverrideMessage.GamePropertyField]) : PropertyOverrideMessage.GameProperty = {
    PropertyOverrideMessage.GameProperty(unk, list)
  }
}

object PropertyOverrideMessage extends Marshallable[PropertyOverrideMessage] {
  final case class GamePropertyValues(field1 : String, field2 : String)

  final case class GamePropertyField(unk : Int, list : List[GamePropertyValues])

  final case class GameProperty(unk : Int, list : List[GamePropertyField])

  private def value_pair_aligned_codec(n : Int) : Codec[GamePropertyValues] = (
    ("field1" | PacketHelpers.encodedStringAligned(n)) ::
      ("field2" | PacketHelpers.encodedString)
    ).as[GamePropertyValues]

  private val value_pair_codec : Codec[GamePropertyValues] = (
    ("field1" | PacketHelpers.encodedString) ::
      ("field2" | PacketHelpers.encodedString)
  ).as[GamePropertyValues]

  private def game_subproperty_codec(n : Int) : Codec[GamePropertyField] = (
    ("unk" | uintL(11)) ::
      (("len" | uint16L) >>:~ { len =>
        conditional(len > 0, value_pair_aligned_codec(n)) ::
          conditional(len > 1, PacketHelpers.listOfNSized((len - 1).toLong, value_pair_codec))
      })
    ).xmap[GamePropertyField] (
    {
      case unk :: _ :: Some(first) :: None :: HNil =>
        GamePropertyField(unk, first :: Nil)

      case unk :: _ :: Some(first) :: Some(other) :: HNil =>
        GamePropertyField(unk, first +: other)
    },
    {
      case GamePropertyField(unk, list) =>
        val (first, other) = list match {
          case ((f : GamePropertyValues) +: (rest : List[GamePropertyValues])) => (Some(f), Some(rest))
          case (f : GamePropertyValues) +: Nil => (Some(f), None)
          case Nil => (None, None)
        }
        unk :: list.length :: first :: other :: HNil
    }
  )

  private val game_property_codec : Codec[GameProperty] = (
    ("unk" | uint16L) ::
      (("len" | uintL(11)) >>:~ { len =>
        conditional(len > 0, game_subproperty_codec(2)) ::
          conditional(len > 1, PacketHelpers.listOfNSized((len - 1).toLong, game_subproperty_codec(5)))
    })
    ).xmap[GameProperty] (
    {
      case unk :: _ :: Some(first) :: None :: HNil =>
        GameProperty(unk, first :: Nil)

      case unk :: _ :: Some(first) :: Some(other) :: HNil =>
        GameProperty(unk, first +: other)
    },
    {
      case GameProperty(unk, list) =>
        val (first, other) = list match {
          case ((f : GamePropertyField) +: (rest : List[GamePropertyField])) => (Some(f), Some(rest))
          case (f : GamePropertyField) +: Nil => (Some(f), None)
          case Nil => (None, None)
        }
        unk :: list.length :: first :: other :: HNil
    }
  )

  implicit val codec : Codec[PropertyOverrideMessage] =
    listOfN(uint16L, game_property_codec).as[PropertyOverrideMessage]
}
