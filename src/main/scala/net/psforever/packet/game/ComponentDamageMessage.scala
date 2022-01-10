// Copyright (c) 2021 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.codecs._
import scodec.Codec

/**
  * na
  * @param unk1 na;
  *             usually, 3 to 7
  * @param unk2 na
  * @param unk3 na;
  *             usually, `true`
  */
final case class ComponentDamageField(unk1: Long, unk2: Long, unk3: Boolean)

object ComponentDamageField {
  def apply(unk1: Long, unk2: Long): ComponentDamageField = ComponentDamageField(unk1, unk2, unk3 = false)
}

/**
  * na
  * @param guid the vehicle that owns this component
  * @param unk1 na;
  *             usually, 0 to 35
  * @param unk2 specific about the component damage;
  *             `None`, when damage issues are cleared
  */
final case class ComponentDamageMessage(guid: PlanetSideGUID, unk1: Long, unk2: Option[ComponentDamageField])
  extends PlanetSideGamePacket {
  type Packet = ComponentDamageMessage
  def opcode = GamePacketOpcode.ComponentDamageMessage
  def encode = ComponentDamageMessage.encode(this)
}

object ComponentDamageMessage extends Marshallable[ComponentDamageMessage] {
  def apply(guid: PlanetSideGUID, unk: Long): ComponentDamageMessage =
    ComponentDamageMessage(guid, unk, None)

  def apply(guid: PlanetSideGUID, unk1: Long, unk2: ComponentDamageField): ComponentDamageMessage =
    ComponentDamageMessage(guid, unk1, Some(unk2))

  private val componentDamageFieldCodec: Codec[ComponentDamageField] = (
    ("unk1" | uint32L) ::
    ("unk2" | uint32L) ::
    ("unk3" | bool)
  ).as[ComponentDamageField]

  implicit val codec: Codec[ComponentDamageMessage] = (
    ("guid" | PlanetSideGUID.codec) ::
    ("unk1" | uint32L) ::
    ("unk2" | optional(bool, componentDamageFieldCodec))
    ).as[ComponentDamageMessage]
}
