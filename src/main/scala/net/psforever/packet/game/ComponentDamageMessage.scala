// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{PlanetSideGUID, SubsystemComponent}
import scodec.codecs._
import scodec.Codec

/**
  * The status of the component's changing condition,
  * including the level of alert the player experiences when the change occurs.
  * @param alarm_level the klaxon sound effect associated with this damage
  * @param damage the amount of damage (encoded ...)
  * @param unk na;
  *            usually, `true`;
  *            known `false` states during shield generator offline and destruction conditions
  */
final case class ComponentDamageField(alarm_level: Long, damage: Long, unk: Boolean)

object ComponentDamageField {
  def apply(alarmLevel: Long, dam: Long): ComponentDamageField = ComponentDamageField(alarmLevel, dam, unk = true)
}

/**
  * Vehicles have aspects that are neither registered -
  * do not necessarily represented unique entities of the vehicle -
  * and are not statistical behaviors derived from the same level as the game files -
  * modify vehicle stats but are not vehicle stats themselves.
  * When these "components of the vehicle" are affected, however,
  * such as when the vehicle has been jammed or when it has sustained damage,
  * changes to the handling of the vehicle will occur through the said statistical mechanics.
  * @see `VehicleSubsystem`
  * @see `VehicleSubsystemEntity`
  * @param guid the entity that owns this component, usually a vehicle
  * @param component the subsystem, or part of the subsystem, being affected
  * @param status specific about the component damage;
  *               `None`, when damage issues are cleared
  */
final case class ComponentDamageMessage(
                                         guid: PlanetSideGUID,
                                         component: SubsystemComponent,
                                         status: Option[ComponentDamageField]
                                       ) extends PlanetSideGamePacket {
  type Packet = ComponentDamageMessage
  def opcode = GamePacketOpcode.ComponentDamageMessage
  def encode = ComponentDamageMessage.encode(this)
}

object ComponentDamageMessage extends Marshallable[ComponentDamageMessage] {
  /**
    * Overloaded constructor where the component's current state is be cleared.
    * @param guid the entity that owns this component, usually a vehicle
    * @param component the subsystem, or part of the subsystem, being affected
    * @return a `ComponentDamageMessage` packet
    */
  def apply(guid: PlanetSideGUID, component: SubsystemComponent): ComponentDamageMessage =
    ComponentDamageMessage(guid, component, None)

  /**
    * Overloaded constructor where the component's current state is always defined.
    * @param guid the entity that owns this component, usually a vehicle
    * @param component the subsystem, or part of the subsystem, being affected
    * @param status specific about the component damage
    * @return a `ComponentDamageMessage` packet
    */
  def apply(guid: PlanetSideGUID, component: SubsystemComponent, status: ComponentDamageField): ComponentDamageMessage =
    ComponentDamageMessage(guid, component, Some(status))

  private val subsystemComponentCodec = PacketHelpers.createLongIntEnumCodec(SubsystemComponent, uint32L)

  private val componentDamageFieldCodec: Codec[ComponentDamageField] = (
    ("unk1" | uint32L) ::
    ("unk2" | uint32L) ::
    ("unk3" | bool)
  ).as[ComponentDamageField]

  implicit val codec: Codec[ComponentDamageMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
    ("component" | subsystemComponentCodec) ::
    ("status" | optional(bool, componentDamageFieldCodec))
    ).as[ComponentDamageMessage]
}
