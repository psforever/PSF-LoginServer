// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game.objectcreate

import net.psforever.packet.{Marshallable, PacketHelpers}
import net.psforever.types.Vector3
import scodec.{Attempt, Codec, Err}
import scodec.codecs._

case class CharacterData(pos : Vector3,
                         objYaw : Int,
                         faction : Int,
                         bops : Boolean,
                         name : String,
                         exosuit : Int,
                         sex : Int,
                         face1 : Int,
                         face2 : Int,
                         voice : Int,
                         unk1 : Int, //0x8080
                         unk2 : Int, //0xFFFF or 0x0
                         unk3 : Int, //2
                         viewPitch : Int,
                         viewYaw : Int,
                         ribbons : RibbonBars,
                         healthMax : Int,
                         health : Int,
                         armor : Int,
                         unk4 : Int, //1
                         unk5 : Int, //7
                         unk6 : Int, //7
                         staminaMax : Int,
                         stamina : Int,
                         unk7 : Int, // 28
                         unk8 : Int, //4
                         unk9 : Int, //44
                         unk10 : Int, //84
                         unk11 : Int, //104
                         unk12 : Int, //1900
                         firstTimeEvent_length : Long,
                         firstEntry : Option[String],
                         firstTimeEvent_list : List[String],
                         tutorial_list : List[String],
                         inventory : InventoryData
                        ) extends ConstructorData {
  override def bsize : Long = {
    //represents static fields
    val first : Long = 1194L //TODO due to changing understanding of the bit patterns in this data, this value will change
    //name
    val second : Long = CharacterData.stringBitSize(name, 16) + 4L //plus the padding
    //fte_list
    var third : Long = 32L
    if(firstEntry.isDefined) {
      third += CharacterData.stringBitSize(firstEntry.get) + 5L //plus the padding
      for(str <- firstTimeEvent_list) {
        third += CharacterData.stringBitSize(str)
      }
    }
    //tutorial list
    var fourth : Long = 32L
    for(str <- tutorial_list) {
      fourth += CharacterData.stringBitSize(str)
    }
    first + second + third + fourth + inventory.bsize
  }
}

object CharacterData extends Marshallable[CharacterData] {
  private def stringBitSize(str : String, width : Int = 8) : Long = {
    val strlen = str.length
    val lenSize = if(strlen > 127) 16L else 8L
    lenSize + strlen * width
  }

  implicit val codec : Codec[CharacterData] = (
    ("pos" | Vector3.codec_pos) ::
      ignore(16) ::
      ("objYaw" | uint8L) ::
      ignore(1) ::
      ("faction" | uintL(2)) ::
      ("bops" | bool) ::
      ignore(20) ::
      ("name" | PacketHelpers.encodedWideStringAligned(4)) ::
      ("exosuit" | uintL(3)) ::
      ignore(2) ::
      ("sex" | uintL(2)) ::
      ("face1" | uint4L) ::
      ("face2" | uint4L) ::
      ("voice" | uintL(3)) ::
      ignore(22) ::
      ("unk1" | uint16L) ::
      ignore(42) ::
      ("unk2" | uint16L) ::
      ignore(30) ::
      ("unk3" | uintL(4)) ::
      ignore(24) ::
      ("viewPitch" | uint8L) ::
      ("viewYaw" | uint8L) ::
      ignore(10) ::
      ("ribbons" | RibbonBars.codec) ::
      ignore(160) ::
      ("healthMax" | uint16L) ::
      ("health" | uint16L) ::
      ignore(1) ::
      ("armor" | uint16L) ::
      ignore(9) ::
      ("unk4" | uint8L) ::
      ignore(8) ::
      ("unk5" | uint4L) ::
      ("unk6" | uintL(3)) ::
      ("staminaMax" | uint16L) ::
      ("stamina" | uint16L) ::
      ignore(149) ::
      ("unk7" | uint16L) ::
      ("unk8" | uint8L) ::
      ("unk9" | uint8L) ::
      ("unk10" | uint8L) ::
      ("unk11" | uint8L) ::
      ("unk12" | uintL(12)) ::
      ignore(19) ::
      (("firstTimeEvent_length" | uint32L) >>:~ { len =>
        conditional(len > 0, "firstEntry" | PacketHelpers.encodedStringAligned(5)) ::
          ("firstTimeEvent_list" | PacketHelpers.listOfNSized(len - 1, PacketHelpers.encodedString)) ::
          ("tutorial_list" | PacketHelpers.listOfNAligned(uint32L, 0, PacketHelpers.encodedString)) ::
          ignore(207) ::
          ("inventory" | InventoryData.codec)
      })
    ).as[CharacterData]

  val genericCodec : Codec[ConstructorData.genericPattern] = codec.exmap[ConstructorData.genericPattern] (
    {
      case x =>
        Attempt.successful(Some(x.asInstanceOf[ConstructorData]))
    },
    {
      case Some(x) =>
        Attempt.successful(x.asInstanceOf[CharacterData])
      case _ =>
        Attempt.failure(Err(""))
    }
  )
}
