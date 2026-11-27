// Copyright (c) 2026 PSForever
// SPDX-License-Identifier: AGPL-3.0-or-later

package net.psforever.packet.game.packets

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * Packet sent when a player uses an equipped item/tool or directly interacts with an entity in the game world.
  *
  * Dispatched on the client by `PlanetsideComm::SendUseItemMessage` (`0x00447E00`), serialized by
  * `UseItemMessage::Encode` (`0x0088B560`), and deserialized by `UseItemMessage::Decode` (`0x0088B710`).
  *
  * === Wire Layout (257 bits) ===
  *  - `opcode`: 8 bits (`0x10`)
  *  - `avatarGUID`: 16 bits (`PlanetSideGUID`, uint16L)
  *  - `itemUsedGUID`: 16 bits (`PlanetSideGUID`, uint16L)
  *  - `objectGUID`: 16 bits (`PlanetSideGUID`, uint16L)
  *  - `actionContext`: 32 bits (uint32L)
  *  - `weaponFireUse`: 1 bit (bool)
  *  - `rayStartPos`: 56 bits (`Vector3.codec_pos`, 20b X, 20b Y, 16b Z)
  *  - `rayHitPos`: 56 bits (`Vector3.codec_pos`, 20b X, 20b Y, 16b Z)
  *  - `pitch`: 8 bits (uint8L, quantized angle: client encodes as `int >> 7`, decodes as `<< 7`)
  *  - `yaw`: 8 bits (uint8L, quantized angle: client encodes as `int >> 7`, decodes as `<< 7`)
  *  - `roll`: 8 bits (uint8L, quantized angle: client encodes as `int >> 7`, decodes as `<< 7`)
  *  - `objectID`: 32 bits (uint32L, definition/class ID of target object)
  *
  * === Usage Types in `planetside.exe` ===
  *
  * '''1. T-REK Base Virus Upload / Purge (Main Terminal)'''
  *  - Initiated via `HUDUploadVirus` (`UploadVirusDialog`, `0x006EF770`) and `PlayerAvatar::UseTargetEntityWithItem` (`0x005766A0`).
  *  - `avatarGUID`: Player GUID
  *  - `itemUsedGUID`: T-REK GUID (`GlobalDefinitions.trek`)
  *  - `objectGUID`: Main Terminal GUID (`GlobalDefinitions.main_terminal`)
  *  - `actionContext`: Virus selection ID:
  *      - `0`: Unlock all doors (`@virus_unlock`)
  *      - `1`: Disable linked facility benefits / cavern modules (`@virus_module`)
  *      - `2`: Double NTU resource drain (`@virus_resource`)
  *      - `3`: Disable enemy radar / sensors (`@virus_radar`)
  *      - `4`: Access enemy equipment terminals (`@virus_equipment`)
  *      - `8`: Purge virus / No virus
  *  - `weaponFireUse`: `true`
  *  - `rayStartPos` / `rayHitPos`: Uninitialized stack memory (client passes NULL)
  *  - `pitch` / `yaw` / `roll`: Uninitialized stack memory (client passes NULL)
  *  - `objectID`: Definition ID of `main_terminal` (`ObjectClass.main_terminal`)
  *
  * '''2. Phalanx Turret Upgrades'''
  *  - Initiated when upgrading an unhacked facility turret with an upgrade kit.
  *  - `avatarGUID`: Player GUID
  *  - `itemUsedGUID`: Upgrade tool GUID
  *  - `objectGUID`: Turret GUID
  *  - `actionContext`: Upgrade combo selection:
  *      - `1`: `AVCombo` (Anti-Vehicle Turret)
  *      - `2`: `FlakCombo` (Anti-Air Turret)
  *  - `weaponFireUse`: `true`
  *  - `objectID`: Definition ID of target turret
  *
  * '''3. Active Tool & Weapon Fire Use (REK, Nano-Dispenser, Medical Applicator)'''
  *  - Initiated by triggering an equipped tool at an entity or player.
  *  - `avatarGUID`: Player GUID
  *  - `itemUsedGUID`: Tool GUID (REK, nano-dispenser, medical applicator)
  *  - `objectGUID`: Target object GUID (door, terminal, generator, vehicle, avatar)
  *  - `actionContext`: Context mode or `0xFFFFFFFFL` (`-1`)
  *  - `weaponFireUse`: `true`
  *  - `rayStartPos`: Raycast origin position
  *  - `rayHitPos`: Raycast intersection point on target object
  *  - `pitch` / `yaw` / `roll`: Player orientation angles
  *  - `objectID`: Target object definition ID
  *
  * '''4. Passive Environment & Object Interactions (Doors, Terminals, Lockers)'''
  *  - Initiated by direct proximity triggers or pressing the use key (`psc_useitem` / `0x0049D820`).
  *  - `avatarGUID`: Player GUID
  *  - `itemUsedGUID`: `PlanetSideGUID(0)` (no tool held/used)
  *  - `objectGUID`: Target entity GUID (door, order terminal, locker, vehicle spawn pad)
  *  - `actionContext`: `0xFFFFFFFFL` (`-1`)
  *  - `weaponFireUse`: `false`
  *  - `rayStartPos` / `rayHitPos`: Uninitialized stack memory (client passes NULL)
  *  - `objectID`: Definition ID of target
  *
  * '''5. Vehicle Mounting & Refueling Operations (ANT / Silo)'''
  *  - Initiated when mounting a vehicle seat or connecting an ANT to a resource silo (`0x004F01C0` / `0x00576B30`).
  *  - `avatarGUID`: Player GUID or Vehicle GUID
  *  - `itemUsedGUID`: `PlanetSideGUID(0)`
  *  - `objectGUID`: Vehicle or Silo GUID
  *  - `actionContext`: `0xFFFFFFFFL` (`-1`)
  *  - `weaponFireUse`: `false`
  *  - `objectID`: Definition ID of vehicle or silo
  *
  * @param avatarGUID     the player initiating the action (`User GUID`)
  * @param itemUsedGUID  the item or tool used, or 0 if passive interaction (`UsedItem GUID`)
  * @param objectGUID     the affected target entity (`Target GUID`)
  * @param actionContext  context parameter (`Old SlotIndex`): Virus ID (0-4), Turret Upgrade (1-2), or 0xFFFFFFFF
  * @param weaponFireUse `true` if activated via tool weapon-fire trigger; `false` if direct/passive interaction
  * @param rayStartPos   raytrace origin coordinates in world space (quantized float)
  * @param rayHitPos     raytrace intersection point on the target in world space (quantized float)
  * @param pitch           player facing pitch angle (quantized 8-bit)
  * @param yaw             player facing yaw angle (quantized 8-bit)
  * @param roll            player facing roll angle (quantized 8-bit)
  * @param objectID       the object class/definition ID of the target object (`Client Target ClassID`)
  */
final case class UseItemMessage(
      avatarGUID: PlanetSideGUID,
      itemUsedGUID: PlanetSideGUID,
      objectGUID: PlanetSideGUID,
      actionContext: Long,
      weaponFireUse: Boolean,
      rayStartPos: Vector3,
      rayHitPos: Vector3,
      pitch: Int,
      yaw: Int,
      roll: Int,
      objectID: Long
) extends PlanetSideGamePacket {
  type Packet = UseItemMessage
  def opcode = GamePacketOpcode.UseItemMessage
  def encode = UseItemMessage.encode(this)

}

object UseItemMessage extends Marshallable[UseItemMessage] {
  implicit val codec: Codec[UseItemMessage] = (
    ("avatarGUID" | PlanetSideGUID.codec) ::
      ("itemUsedGUID" | PlanetSideGUID.codec) ::
      ("objectGUID" | PlanetSideGUID.codec) ::
      ("actionContext" | uint32L) ::
      ("weaponFireUse" | bool) ::
      ("rayStartPos" | Vector3.codec_pos) ::
      ("rayHitPos" | Vector3.codec_pos) ::
      ("pitch" | uint8L) ::
      ("yaw" | uint8L) ::
      ("roll" | uint8L) ::
      ("objectID" | uint32L)
  ).as[UseItemMessage]
}
