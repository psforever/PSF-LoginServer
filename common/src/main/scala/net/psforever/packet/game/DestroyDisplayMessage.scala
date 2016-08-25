// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import akka.util.ByteString
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Display a message in the event window that informs of a player death.<br>
  * <br>
  * The message is composed of three parts:<br>
  * 1) killer - who or what is attributed the death<br>
  * 2) method - an icon of the implement of death<br>
  * 3) victim - the player that was killed<br>
  * In the case of a player kill, the player's name will be attributed directly.
  * In the case of an absent kill, a description of the method will be attributed.
  * In the case of a suicide, the player attributed is the player who was killed; but, the player is only shown as the victim.<br>
  * <br>
  * The faction affiliation and whether or not the individual is/was in a vehicle are combined into one byte.
  * After normal byte values for faction - see PlanetSideEmpire - 20 (32) is added depending on whether the player is vehicle-occupying at the time.
  * When marked like this, the player gets square brackets around their name.
  * An extra 0F is added for the killer's affiliation as convention, but it is not absolutely necessary.
  * For example, for a vehicle-bound NC player, 40 + F + 20 = 6F (64 + 15 + 32 = 111).
  * This formula works for all factions, even Black OPs.
  * The extra 0F is not generally applied to the victim's affiliation but adding 20 still applies square brackets to the name.
  * (The name length parameters do not need to take the brackets into account.)<br>
  * <br>
  * The method of homicide or suicide is passed as a 20-bit value, or two bytes and the high order nibble of a third.
  * The color of the icon is borrowed from the killer's faction affiliation if it can be determined.
  * An unidentified method parameter defaults into a skull and crossbones icon.
  * The color of the victim's name is often borrowed from the killer's faction affiliation in the case of slightly malformed, but still accepted, packets.<br>
  * <br>
  * The length of the victim's name is a normal byte flush against the 20-bit method portion.
  * If all bytes are paired properly, this would divide it between the low order nibble of the third method byte and the high order nibble of the next byte.
  * This leaves the high order nibble (0-F) between the victim's name's length and the victim's double wide character name unused.
  * Unlike with the killer, the victim's name is reported at twice its string length, reflecting the double-wide characters.<br>
  * <br>
  * Exploration:<br>
  * Following each player name are four bytes that, within the same login session, should match up to the player.
  * The association of these bytes to the player is valid regardless of whether that player is marked herein as a "killer" or as a "victim."
  * It seems likely that one pair of bytes is the player GUID (PlanetSideGUID).
  * What do the other bytes represent? and which pair is which?<br>
  * <br>
  * Examples:<br>
  * 1Shot1KiIl-VS (Lasher) BlueJay-NC<br>
  * `81 8A 3100530068006F00740031004B00690049006C00 46591000 8F 20 15A03 0E 0 42006C00750065004A0061007900             F4FC0500 40`<br>
  * Devinator-NC (Jackhammer) 1Shot1KiIl-VS<br>
  * `81 89 44006500760069006E00610074006F007200     E110D501 4F 20 19405 14 0 3100530068006F00740031004B00690049006C00 46591000 80`<br>
  *
  * @param killer the wide character name of the player who did the killing
  * @param unk1 see Exploration above
  * @param unk2 see Exploration above
  * @param killer_empire_mode the empire affiliation of the killer and whether they are a pedestrian
  * @param buffer20 a buffering value separating the killer and victim sides of the data; typically, 20
  * @param method how the victim was killed (what icon to display)
  * @param victim_name_length the length of the victim's name
  * @param buffer0 a buffering value; typically, 0
  * @param victim the wide character name of the player who was killed (note: not preceded by the length and can not be processed if prepended)
  * @param unk3 see Exploration above
  * @param unk4 see Exploration above
  * @param victim_empire_mode the empire affiliation of the victim
  */
//sendRawResponse(hex"81 89 44006500760069006E00610074006F007200 E110 D501 4F 20 19405 14 0 3100530068006F00740031004B00690049006C00 4659 1000 80") // Devinator-NC (Jackhammer) 1Shot1KiIl-VS
final case class DestroyDisplayMessage(killer : String,
                                       unk1 : Int,
                                       unk2 : Int,
                                       killer_empire_mode : Int,
                                       buffer20 : Int,
                                       method : Int,
                                       victim_name_length : Int,
                                       buffer0 : Int,
                                       victim : String,
                                       unk3 : Int,
                                       unk4 : Int,
                                       victim_empire_mode : Int)
  extends PlanetSideGamePacket {
  type Packet = DestroyDisplayMessage
  def opcode = GamePacketOpcode.DestroyDisplayMessage
  def encode = DestroyDisplayMessage.encode(this)
  ByteString
}

object DestroyDisplayMessage extends Marshallable[DestroyDisplayMessage] {
  implicit val codec : Codec[DestroyDisplayMessage] = (
    ("killer" | PacketHelpers.encodedWideString) ::
      ("unk1" | uint16L) ::
      ("unk2" | uint16L) ::
      ("killer_empire_mode" | uint8L) ::
      ("buffer20" | uint8L) ::
      ("method" | uintL(20)) ::
      ("victim_name_length" | uint8L) ::
      ("buffer0" | uintL(4)) ::
      (("victim" | PacketHelpers.encodedWideString).) ::
      ("unk3" | uint16L) ::
      ("unk4" | uint16L) ::
      ("victim_empire_mode" | uint8L)
    ).as[DestroyDisplayMessage]
}
