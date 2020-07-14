// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * (Where the child object was before it was moved is not specified or important.)
  * @see `Definition.ObjectId`<br>
  *       `TurretUpgrade`
  * @param avatar_guid     the player
  * @param item_used_guid  the item used;
  *                        e.g. a REK to hack or a medkit to heal.
  * @param object_guid     the object affected;
  *                        e.g., door when opened, terminal when accessed, avatar when medkit used
  * @param unk2            na;
  *                        when upgrading phalanx turrets, 1 for `AVCombo` and 2 for `FlakCombo`
  * @param unk3            using tools, e.g., a REK or nano-dispenser
  * @param unk4            seems to be related to T-REK viruses.
  *                        0 - unlock all doors
  *                        1 - disable linked benefits
  *                        2 - double ntu drain
  *                        3 - disable enemy radar
  *                        4 - access equipment terminals
  * @param unk6         na
  * @param unk7         na;
  *                     25 when door 223 when terminal
  * @param unk8         na;
  *                     0 when door 1 when use rek (252 then equipment term)
  * @param object_id    the object id `object_guid`'s object
  */

/*
    BETA CLIENT DEBUG INFO:
      User GUID
      UsedItem GUID
      Target GUID
      Old SlotIndex
      Weapon Fire Use
      RayTrace Start Position (3 fields - Vector3)
      RayTrace Intersection Position (3 fields - Vector3)
      Orientation (3 fields - Vector3)
      Client Target ClassID
 */
final case class UseItemMessage(
    avatar_guid: PlanetSideGUID,
    item_used_guid: PlanetSideGUID,
    object_guid: PlanetSideGUID,
    unk2: Long,
    unk3: Boolean,
    unk4: Vector3,
    unk5: Vector3,
    unk6: Int,
    unk7: Int,
    unk8: Int,
    object_id: Long
) extends PlanetSideGamePacket {
  type Packet = UseItemMessage
  def opcode = GamePacketOpcode.UseItemMessage
  def encode = UseItemMessage.encode(this)
}

object UseItemMessage extends Marshallable[UseItemMessage] {
  implicit val codec: Codec[UseItemMessage] = (
    ("avatar_guid" | PlanetSideGUID.codec) ::
      ("item_used_guid" | PlanetSideGUID.codec) ::
      ("object_guid" | PlanetSideGUID.codec) ::
      ("unk2" | uint32L) ::
      ("unk3" | bool) ::
      ("unk4" | Vector3.codec_pos) ::
      ("unk5" | Vector3.codec_pos) ::
      ("unk6" | uint8L) ::
      ("unk7" | uint8L) ::
      ("unk8" | uint8L) ::
      ("object_id" | uint32L)
  ).as[UseItemMessage]
}
