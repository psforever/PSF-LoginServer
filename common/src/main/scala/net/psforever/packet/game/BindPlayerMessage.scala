// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.{SpawnGroup, Vector3}
import scodec.Codec
import scodec.codecs._

/**
  * The purpose of the `BindPlayerMessage` packet.<br>
  * <br>
  * `Bind` and `Unbind` are generally manual actions performed by the player.
  * `Available` is applied to automatic Advanced Mobile Spawn points and other "Bound" points at the time of redeployment.
  * `Lost` and `Unavailable` remove the status of being bound and have slightly different connotations.
  * Each generates a different a events chat message if logging it turned on.
  */
object BindStatus extends Enumeration(1) {
  type Type = Value

  val Bind, Unbind, Lost, Available, Unavailable = Value

  implicit val codec = PacketHelpers.createEnumerationCodec(this, uint8)
}

/**
  * A packet dispatched to maintain a manually-set respawn location.<br>
  * <br>
  * The packet establishes the player's ability to spawn in an arbitrary location that is not a normal local option.
  * This process is called "binding one's matrix."
  * In addition to player establishing the binding, the packet updates as conditions of the respawn location changes.<br>
  * <br>
  * If `logging` is turned on, packets will display different messages depending on context.
  * The bind descriptor will be used to flavor the events chat message.
  * As long as the event is marked to be logged, when the packet is received, a message is displayed in the events window.
  * If the logged action is applicable, the matrixing sound effect will be played too.
  * Not displaying events is occasionally warranted for aesthetics.
  * The game will always note if this is your first time binding regardless of the state of this flag.<br>
  * <br>
  * Bind Descriptors:<br>
  * `&#64;amp_station`<br>
  * `&#64;ams`<br>
  * `&#64;comm_station` (interlink facility?)<br>
  * `&#64;comm_station_dsp` (dropship center?)<br>
  * `&#64;cryo_facility` (biolab?)<br>
  * `&#64;tech_plant`<br>
  * <br>
  * Exploration:<br>
  * Find other bind descriptors.
  * @param action the purpose of the packet
  * @param bind_desc a text description of the respawn binding point
  * @param display_icon show the selection icon on the redeployment map
  * @param logging true, to report on bind point change visible in the events window;
  *                false, to render spawn change silently;
  *                some first time notifications will always display regardless of this flag
  * @param spawn_group the kind of spawn request that will be made;
  *                    affects the type of icon displayed;
  *                    will coincide with the value of `unk2` in `SpawnRequestMessage` when the spawn option is selected
  * @param zone_number the number of the zone in which to display this spawn option;
  *                    if `zone_number` is not the current zone, and the action is positive,
  *                    a small map of the alternate zone with selectable spawn point will become visible
  * @param unk4 na
  * @param pos coordinates for any displayed deployment map icon;
  *            `x` and `y` determine the position
  */
final case class BindPlayerMessage(
    action: BindStatus.Value,
    bind_desc: String,
    display_icon: Boolean,
    logging: Boolean,
    spawn_group: SpawnGroup,
    zone_number: Long,
    unk4: Long,
    pos: Vector3
) extends PlanetSideGamePacket {
  type Packet = BindPlayerMessage
  def opcode = GamePacketOpcode.BindPlayerMessage
  def encode = BindPlayerMessage.encode(this)
}

object BindPlayerMessage extends Marshallable[BindPlayerMessage] {

  /**
    * A common variant of this packet.
    */
  val Standard = BindPlayerMessage(BindStatus.Unbind, "", false, false, SpawnGroup.BoundAMS, 0, 0, Vector3.Zero)

  private val spawnGroupCodec = PacketHelpers.createIntEnumCodec(SpawnGroup, uint4)

  implicit val codec: Codec[BindPlayerMessage] = (
    ("action" | BindStatus.codec) ::
      ("bind_desc" | PacketHelpers.encodedString) ::
      ("display_icon" | bool) ::
      ("logging" | bool) ::
      ("spawn_group" | spawnGroupCodec) ::
      ("zone_number" | uint32L) ::
      ("unk4" | uint32L) ::
      ("pos" | Vector3.codec_pos)
  ).as[BindPlayerMessage]
}
