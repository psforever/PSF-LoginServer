// Copyright (c) 2017 PSForever
package net.psforever.types

import net.psforever.packet.PacketHelpers
import scodec.codecs.uint

/**
  * The voice used by the player character, from a selection of ten divided between five male voices and five female voices.
  * The first entry (0) is no voice.
  * While it is technically not valid to have a wrong-gendered voice,
  * unlisted sixth and seventh entries would give a male character a female voice;
  * a female character with either entry would become mute, however.
  * @see `CharacterGender`
  */
object CharacterVoice extends Enumeration {
  type Type = Value

  val Mute, Voice1, //grizzled, tough
  Voice2,           //greenhorn, clueless
  Voice3,           //roughneck, gruff
  Voice4,           //stalwart, smooth
  Voice5            //daredevil, calculating
  = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint(3))
}
