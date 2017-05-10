// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Instructs client to play the weapon jammed sound when sent server to client.
  *
  * Appears to have the same functionality and sound regardless of guid, as long as the client has that item in inventory.
  * See also [[WeaponDryFireMessage]]
  *
  * @param weapon_guid the weapon that is jammed
  */
final case class WeaponJammedMessage(weapon_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = WeaponJammedMessage
  def opcode = GamePacketOpcode.WeaponJammedMessage
  def encode = WeaponJammedMessage.encode(this)
}

object WeaponJammedMessage extends Marshallable[WeaponJammedMessage] {
  implicit val codec : Codec[WeaponJammedMessage] = ("weapon_guid" | PlanetSideGUID.codec).as[WeaponJammedMessage]
}
