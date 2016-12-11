// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.newcodecs.newcodecs
import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class PlayerStateMessage(guid : PlanetSideGUID,
                                    pos : Vector3,
                                    unk1 : Int,
                                    unk2 : Int,
                                    unk3 : Int,
                                    unk4 : Int,
                                    y : Boolean,
                                    unk5 : Boolean = false,
                                    unk6 : Boolean = false,
                                    unk7 : Boolean = false,
                                    unk8 : Boolean = false)
  extends PlanetSideGamePacket {
  type Packet = TimeOfDayMessage
  def opcode = GamePacketOpcode.PlayerStateMessage
  def encode = PlayerStateMessage.encode(this)
}

object PlayerStateMessage extends Marshallable[PlayerStateMessage] {
  type fourBoolPattern = Boolean :: Boolean :: Boolean :: Boolean :: HNil

  val booleanCodec : Codec[fourBoolPattern] = (
    bool ::
    bool ::
    bool ::
    bool
    ).as[fourBoolPattern]

  val defaultCodec : Codec[fourBoolPattern] = ignore(0).xmap[fourBoolPattern] (
    {
      case _ =>
        false :: false :: false :: false :: HNil
    },
    {
      case _ =>
        ()
    }
  ).as[fourBoolPattern]

  implicit val codec : Codec[PlayerStateMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("unk1" | uint8L) ::
      ("unk2" | uint8L) ::
      ("unk3" | uint8L) ::
      ("unk4" | uintL(10)) ::
      ("y" | bool >>:~ { test =>
        ignore(0) ::
          newcodecs.binary_choice(test, booleanCodec, defaultCodec)
      })
    ).as[PlayerStateMessage]
}
