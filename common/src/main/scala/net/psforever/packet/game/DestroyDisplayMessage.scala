// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Display a message in the event window that informs of a player death.<br>
  * <br>
  * The message is composed of three parts:<br>
  * 1) killer information<br>
  * 2) method information<br>
  * 3) victim information<br>
  * In the case of a player kill, the player's name will be attributed directly.
  * In the case of an absentee kill, a description of the method of death will be attributed.
  * In the case of a suicide, the player attributed is the player who was killed (message format displays only the victim).
  * The victim's name is byte-aligned with a 5-bit buffer.<br>
  * <br>
  * The four bytes that follow each name seems to be important to the identification of the associated player.
  * The same value will be seen in every `DestroyDisplayMessage` that includes the player, with respect to whether they are listed as the "killer" or as the "victim."
  * This holds true for every entry within thie same login session, at least.
  * Blanking these values out does not change anything about the format of the event message.
  * In the case of absentee kills, for example, where there is no killer listed, this field has been zero'd (`00000000`).<br>
  * <br>
  * The faction affiliation is different from the normal way `PlanetSideEmpire` values are recorded.
  * The higher nibble will reflect the first part of the `PlanetSideEmpire` value - `0` for TR, `4` for NC `8` for TR, `C` for Neutral/BOPs.
  * An extra `20` will be added if the player is in a vehicle or turret at the time - `2` for TR, `6` for NC, `A` for VS, `E` for Neutral/BOPs.
  * When marked as being in a vehicle or turret, the player's name will be enclosed within square brackets.
  * The length of the player's name found at the start of the wide character string does not reflect whether or not there will be square brackets (fortunately).<br>
  * <br>
  * The two bytes in between the killer section and the victim section are the method of homicide or suicide.
  * The color of the resulting icon is borrowed from the attributed killer's faction affiliation if it can be determined.
  * An unidentified method defaults to a skull and crossbones icon.
  * The exact range of unique and valid icon values for this parameter is currently unknown.
  * It is also unknown what the two bytes preceding `method` specify, as changing them does nothing to the displayed message.
  * @param killer the name of the player who did the killing
  * @param killer_unk See above
  * @param killer_empire the empire affiliation of the killer:
  *                      0 - TR, 1 - NC, 2 - VS, 3 - Neutral/BOPs
  * @param killer_inVehicle true, if the killer was in a vehicle at the time of the kill; false, otherwise
  * @param unk na; but does not like being set to 0
  * @param method modifies the icon in the message, related to the way the victim was killed
  * @param victim the name of the player who was killed
  * @param victim_unk See above
  * @param victim_empire the empire affiliation of the victim:
  *                      0 - TR, 1 - NC, 2 - VS, 3 - Neutral/BOPs
  * @param victim_inVehicle true, if the victim was in a vehicle when he was killed; false, otherwise
  */
final case class DestroyDisplayMessage(killer : String,
                                       killer_unk : Long,
                                       killer_empire : Int,
                                       killer_inVehicle : Boolean,
                                       unk : PlanetSideGUID,
                                       method : PlanetSideGUID,
                                       victim : String,
                                       victim_unk : Long,
                                       victim_empire : Int,
                                       victim_inVehicle : Boolean
)
  extends PlanetSideGamePacket {
  type Packet = DestroyDisplayMessage
  def opcode = GamePacketOpcode.DestroyDisplayMessage
  def encode = DestroyDisplayMessage.encode(this)
}

object DestroyDisplayMessage extends Marshallable[DestroyDisplayMessage] {
  implicit val codec : Codec[DestroyDisplayMessage] = (
    ("killer" | PacketHelpers.encodedWideString) ::
      ("killer_unk" | ulongL(32)) ::
      ("killer_empire" | uintL(2)) ::
      ("killer_inVehicle" | bool) ::
      ("unk" | PlanetSideGUID.codec) ::
      ("method" | PlanetSideGUID.codec) ::
      ("victim" | PacketHelpers.encodedWideStringAligned(5)) ::
      ("victim_unk" | ulongL(32)) ::
      ("victim_empire" | uintL(2)) ::
      ("victim_inVehicle" | bool)
    ).as[DestroyDisplayMessage]
}
