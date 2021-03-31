// Copyright (c) 2021 PSForever
package net.psforever.types

import enumeratum.values.{IntEnum, IntEnumEntry}
import net.psforever.packet.PacketHelpers
import scodec.codecs.uint2L

/**
  * Values for two sexes, Male and Female, as required by `ObjectCreateMessage` parameters.
  * Some quaint language for log decoration is provided.
  * Blame the lack of gender dysphoria on the Terran Republic.
  */
sealed abstract class CharacterSex(
                                    val value: Int,
                                    val pronounSubject: String,
                                    val pronounObject: String,
                                    val possessive: String
                                  ) extends IntEnumEntry {
  def possessiveNoObject: String = possessive
}

/**
  * Values for two sexes, Male and Female.
  */
object CharacterSex extends IntEnum[CharacterSex] {
  val values = findValues

  case object Male extends CharacterSex(
    value = 1,
    pronounSubject = "he",
    pronounObject = "him",
    possessive = "his"
  )

  case object Female extends CharacterSex(
    value = 2,
    pronounSubject = "she",
    pronounObject = "her",
    possessive = "her"
  ) {
    override def possessiveNoObject: String = "hers"
  }

  implicit val codec = PacketHelpers.createIntEnumCodec(enum = this, uint2L)
}
