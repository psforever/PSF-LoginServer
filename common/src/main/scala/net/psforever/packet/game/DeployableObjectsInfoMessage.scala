// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.GamePacketOpcode.Type
import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, Vector3}
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._

/**
  * An `Enumeration` of the actions that can be performed upon a deployable item.
  */
object DeploymentAction extends Enumeration {
  type Type = Value

  val Dismiss, Build = Value

  implicit val codec: Codec[DeploymentAction.Value] =
    PacketHelpers.createEnumerationCodec(this, uint(1)) // no bool overload
}

/**
  * An `Enumeration` of the map element icons that can be displayed based on the type of deployable item.
  */
object DeployableIcon extends Enumeration {
  type Type = Value

  val Boomer, HEMine, MotionAlarmSensor, SpitfireTurret, RouterTelepad, DisruptorMine, ShadowTurret, CerebusTurret,
      TRAP, AegisShieldGenerator, FieldTurret, SensorDisruptor = Value

  implicit val codec: Codec[DeployableIcon.Value] = PacketHelpers.createEnumerationCodec(this, uint4L)
}

/**
  * The entry of a deployable item.
  * @param object_guid the deployable item
  * @param icon the map element depicting the item
  * @param pos the position of the deployable in the world (and on the map)
  * @param player_guid the player who is the owner
  */
final case class DeployableInfo(
    object_guid: PlanetSideGUID,
    icon: DeployableIcon.Value,
    pos: Vector3,
    player_guid: PlanetSideGUID
)

/**
  * Dispatched by the server to inform the client of a change in deployable items and that the map should be updated.<br>
  * <br>
  * When this packet defines a `Build` `action`, an icon of the deployable item is added to the avatar's map.
  * The actual object referenced does not have to actually exist on the client for the map element to appear.
  * The identity of the element is discerned from its icon rather than the actual object (if it exists).
  * When this packet defines a `Deconstruct` `action`, the icon of the deployable item is removed from the avatar's map.
  * (The map icon to be removed is located by searching for the matching UID.
  * The item does not need to exist to remove its icon.)<br>
  * <br>
  * All deployables have a map-icon-menu that allows for control of and some feedback about the item.
  * At the very least, the item can be dismissed.
  * The type of icon indicating the type of deployable item determines the map-icon-menu.
  * Normally, the icon of a random (but friendly) deployable is gray and the menu is unresponsive.
  * If the `player_guid` matches the client's avatar, the icon is yellow and that marks that the avatar owns this item.
  * The avatar is capable of accessing the item's map-icon-menu and manipulating the item from there.
  * If the deployable item actually doesn't exist, feedback is disabled, e.g., Aegis Shield Generators lack upgrade information.
  * @param action how the entries in the following `List` are affected
  * @param deployables a `List` of information regarding deployable items
  */
final case class DeployableObjectsInfoMessage(action: DeploymentAction.Value, deployables: List[DeployableInfo])
    extends PlanetSideGamePacket {
  type Packet = DeployableObjectsInfoMessage

  def opcode: Type = GamePacketOpcode.DeployableObjectsInfoMessage

  def encode: Attempt[BitVector] = DeployableObjectsInfoMessage.encode(this)
}

object DeployableObjectsInfoMessage extends Marshallable[DeployableObjectsInfoMessage] {

  /**
    * Overloaded constructor that accepts a single `DeployableInfo` entry (and turns it into a `List`).
    * @param action how the following entry is affected
    * @param info the singular entry of a deployable item
    * @return a `DeployableObjectsInfoMessage` object
    */
  def apply(action: DeploymentAction.Type, info: DeployableInfo): DeployableObjectsInfoMessage =
    new DeployableObjectsInfoMessage(action, info :: Nil)

  /**
    * `Codec` for `DeployableInfo` data.
    */
  private val info_codec: Codec[DeployableInfo] = (
    ("object_guid" | PlanetSideGUID.codec) ::
      ("icon" | DeployableIcon.codec) ::
      ("pos" | Vector3.codec_pos) ::
      ("player_guid" | PlanetSideGUID.codec)
  ).as[DeployableInfo]

  implicit val codec: Codec[DeployableObjectsInfoMessage] = (
    ("action" | DeploymentAction.codec) ::
      ("deployables" | PacketHelpers.listOfNAligned(uint32L, 0, info_codec))
  ).as[DeployableObjectsInfoMessage]
}
