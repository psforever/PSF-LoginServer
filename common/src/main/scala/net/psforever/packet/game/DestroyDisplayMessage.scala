// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.PlanetSideEmpire
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
  * In the case of a suicide, the player attributed is the player who was killed (message format displays only the victim).<br>
  * <br>
  * The four bytes that follow each name seems to be important to the identification of the associated player.
  * The same value will be seen in every `DestroyDisplayMessage` that includes that player,
  * with respect to whether they are listed as the "killer" or as the "victim."
  * This holds true for every entry within the same login session, at least.
  * Blanking either of these values out does not change anything about the format of the event message.
  * If the two ids match, the packet will interpreted as the "suicide" format, even if the names do not match.
  * In the case of absentee kills where there is no killer listed, this field is zero'd.<br>
  * <br>
  * When marked as being in a vehicle or a turret, the player's name will be enclosed within square brackets.
  * The length of the player's name found at the start of the character string does not reflect
  * whether or not there will be square brackets (fortunately).
  * The color of the resulting icon is borrowed from the attributed killer's faction affiliation if it can be determined
  * and the type of icon is the same as an object id.
  * An unidentified method or a missing icon defaults to a skull and crossbones.
  * @param killer the name of the player who did the killing
  * @param killer_charId same as CharacterInfoMessage
  * @param killer_empire the empire affiliation of the killer
  * @param killer_in_vehicle true, if the killer was in a vehicle at the time of the kill; false, otherwise
  * @param unk na; but does not like being set to 0
  * @param method modifies the icon in the message, related to the way the victim was killed
  * @param victim the name of the player who was killed
  * @param victim_charId same as CharacterInfoMessage
  * @param victim_empire the empire affiliation of the victim
  * @param victim_in_vehicle true, if the victim was in a vehicle when he was killed; false, otherwise
  */
final case class DestroyDisplayMessage(
    killer: String,
    killer_charId: Long,
    killer_empire: PlanetSideEmpire.Value,
    killer_in_vehicle: Boolean,
    unk: Int,
    method: Int,
    victim: String,
    victim_charId: Long,
    victim_empire: PlanetSideEmpire.Value,
    victim_in_vehicle: Boolean
) extends PlanetSideGamePacket {
  type Packet = DestroyDisplayMessage
  def opcode = GamePacketOpcode.DestroyDisplayMessage
  def encode = DestroyDisplayMessage.encode(this)
}

object DestroyDisplayMessage extends Marshallable[DestroyDisplayMessage] {
  implicit val codec: Codec[DestroyDisplayMessage] = (
    ("killer" | PacketHelpers.encodedWideString) ::
      ("killer_charId" | ulongL(32)) ::
      ("killer_empire" | PlanetSideEmpire.codec) ::
      ("killer_in_vehicle" | bool) ::
      ("unk" | uint16L) ::
      ("method" | uint16L) ::
      ("victim" | PacketHelpers.encodedWideStringAligned(5)) ::
      ("victim_charId" | ulongL(32)) ::
      ("victim_empire" | PlanetSideEmpire.codec) ::
      ("victim_in_vehicle" | bool)
  ).as[DestroyDisplayMessage]
}
