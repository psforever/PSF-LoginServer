// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import net.psforever.types.Vector3
import scodec.Codec
import scodec.codecs._

/**
  * (Where the child object was before it was moved is not specified or important.)<br>
  * @param avatar_guid  the player.
  * @param unk1         dont know how call that. It's the "item" ID when use a rek to hack or a medkit to heal.
  * @param object_guid  can be : Door, Terminal, Avatar (medkit).
  * @param unk2         ???
  * @param unk3         ??? true when use a rek (false when door, medkit or open equip term)
  * @param unk4         ???
  * @param unk5         ???
  * @param unk6         ???
  * @param unk7         ??? 25 when door 223 when terminal
  * @param unk8         ??? 0 when door 1 when use rek (252 then equipment term)
  * @param itemType     object ID from game_objects.adb (ex 612 is an equipment terminal, for medkit we have 121 (avatar))
  */
final case class UseItemMessage(avatar_guid : PlanetSideGUID,
                                unk1 : Int,
                                object_guid : PlanetSideGUID,
                                unk2 : Long,
                                unk3 : Boolean,
                                unk4 : Vector3,
                                unk5 : Vector3,
                                unk6 : Int,
                                unk7 : Int,
                                unk8 : Int,
                                itemType : Long)
  extends PlanetSideGamePacket {
  type Packet = UseItemMessage
  def opcode = GamePacketOpcode.UseItemMessage
  def encode = UseItemMessage.encode(this)
}

object UseItemMessage extends Marshallable[UseItemMessage] {
  implicit val codec : Codec[UseItemMessage] = (
    ("avatar_guid" | PlanetSideGUID.codec) ::
      ("unk1" | uint16L) ::
      ("object_guid" | PlanetSideGUID.codec) ::
      ("unk2" | uint32L) ::
      ("unk3" | bool) ::
      ("unk4" | Vector3.codec_pos) ::
      ("unk5" | Vector3.codec_pos) ::
      ("unk6" | uint8L) ::
      ("unk7" | uint8L) ::
      ("unk8" | uint8L) ::
      ("itemType" | uint32L)
    ).as[UseItemMessage]
}
