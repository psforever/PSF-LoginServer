// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._

/**
  * Instructs client to play a weapon's dryfire sound when sent server to client.
  *
  * The particular sound played depends on the type of weapon that the guid is (likely from adb gamedata).
  * See also [[WeaponJammedMessage]]
  *
  * @param weapon_guid the weapon that is dry firing
  */
final case class WeaponDryFireMessage(weapon_guid : PlanetSideGUID)
  extends PlanetSideGamePacket {
  type Packet = WeaponDryFireMessage
  def opcode = GamePacketOpcode.WeaponDryFireMessage
  def encode = WeaponDryFireMessage.encode(this)
}

object WeaponDryFireMessage extends Marshallable[WeaponDryFireMessage] {
  implicit val codec : Codec[WeaponDryFireMessage] = ("weapon_guid" | PlanetSideGUID.codec).as[WeaponDryFireMessage]
}
