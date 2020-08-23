// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.Codec
import scodec.codecs._

/**
  * Dispatched by the server to update the value associated with an object in a specific container object.<br>
  * <br>
  * The object indicated by `object_guid` must be associated with the inventory (`container_guid`) at the time.
  * A common use for this packet is to update weapon data when gaining control over that weapon.
  * For example, before boarding any kind of turret for the first time, it's ammunition component will have exactly one shot.
  * This shot was established when the turret was first created.
  * This information would be displayed in the holster icon across the bottom of the GUI while it is mounted.
  * Furthermore, the mounted player will only fire the turret exactly one time.
  * This packet can provide the turret with its correct and current amount of ammunition before the player mounts it.
  * @param object_guid the object being affected
  * @param unk na;
  *            usually 0
  * @param container_guid the object in which `object_guid` is contained
  * @param value an amount with which to update `object_guid`
  */
final case class InventoryStateMessage(
    object_guid: PlanetSideGUID,
    unk: Int,
    container_guid: PlanetSideGUID,
    value: Long
) extends PlanetSideGamePacket {
  type Packet = InventoryStateMessage
  def opcode = GamePacketOpcode.InventoryStateMessage
  def encode = InventoryStateMessage.encode(this)
}

object InventoryStateMessage extends Marshallable[InventoryStateMessage] {

  /**
    * Overloaded constructor that ignores the unknown field.
    * @param object_guid the object being affected
    * @param container_guid the object in which `object_guid` is contained
    * @param value an amount with which to update `object_guid`
    * @return an `InventoryStateMessage` object
    */
  def apply(object_guid: PlanetSideGUID, container_guid: PlanetSideGUID, value: Long): InventoryStateMessage =
    InventoryStateMessage(object_guid, 0, container_guid, value)

  implicit val codec: Codec[InventoryStateMessage] = (
    ("object_guid" | PlanetSideGUID.codec) ::
      ("unk" | uintL(10)) ::
      ("container_guid" | PlanetSideGUID.codec) ::
      ("value" | uint32L)
  ).as[InventoryStateMessage]
}
