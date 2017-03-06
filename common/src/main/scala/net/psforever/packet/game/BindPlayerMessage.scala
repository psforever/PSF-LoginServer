// Copyright (c) 2016 PSForever.net to present
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * A packet dispatched to maintain a manually-set respawn location.<br>
  * <br>
  * The packet establishes the player's ability to spawn in an arbitrary location that is not a normal local option.
  * This process is called "binding."
  * In addition to player establishing the binding, the packet updates as conditions of the respawn location changes.<br>
  * <br>
  * If `logging` is turned on, packets will display different messages depending on context.
  * As long as the event is marked to be logged, when the packet is received, a message is displayed in the events window.
  * If the logged action is applicable, the matrixing sound effect will be played too.
  * Not displaying events is occasionally warranted for aesthetics.
  * The game will always note if this is your first time binding.<br>
  * <br>
  * One common occurrence of this packet is during zone transport.
  * Specifically, a packet is dispatched after unloading the current zone but before beginning loading in the new zone.
  * It is preceded by all of the `ObjectDeleteMessage` packets and itself precedes the `LoadMapMessage` packet.<br>
  * <br>
  * Actions:<br>
  * `1` - bound to respawn point<br>
  * `2` - general unbound / unbinding from respawn point<br>
  * `3` - respawn point lost<br>
  * `4` - bound spawn point became available<br>
  * `5` - bound spawn point became unavailable (different from 3)<br>
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
  * @param bindDesc a description of the respawn binding point
  * @param unk1 na; usually set `true` if there is more data in the packet ...
  * @param logging true, to report on bind point change visible in the events window;
  *                false, to render spawn change silent;
  *                a first time event notification will always show
  * @param unk2 na; if a value, it is usually 40 (hex`28`)
  * @param unk3 na
  * @param unk4 na
  * @param pos a position associated with the binding
  */
final case class BindPlayerMessage(action : Int,
                                   bindDesc : String,
                                   unk1 : Boolean,
                                   logging : Boolean,
                                   unk2 : Int,
                                   unk3 : Long,
                                   unk4 : Long,
                                   pos : Vector3)
  extends PlanetSideGamePacket {
  type Packet = BindPlayerMessage
  def opcode = GamePacketOpcode.BindPlayerMessage
  def encode = BindPlayerMessage.encode(this)
}

object BindPlayerMessage extends Marshallable[BindPlayerMessage] {
  /**
    * A common variant of this packet.
    * `16028004000000000000000000000000000000`
    */
  val STANDARD = BindPlayerMessage(2, "", false, false, 2, 0, 0, Vector3(0, 0, 0))

  //TODO: there are two ignore(1) in this packet; are they in a good position?
  implicit val codec : Codec[BindPlayerMessage] = (
    ("action" | uint8L) ::
      ("bindDesc" | PacketHelpers.encodedString) ::
      ("unk1" | bool) ::
      ("logging" | bool) ::
      ignore(1) ::
      ("unk2" | uint4L) ::
      ignore(1) ::
      ("unk3" | uint32L) ::
      ("unk4" | uint32L) ::
      ("pos" | Vector3.codec_pos)
    ).as[BindPlayerMessage]
}
