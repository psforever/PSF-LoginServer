// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PacketHelpers, PlanetSideGamePacket}
import scodec.Codec
import scodec.codecs._
import shapeless.{::, HNil}

final case class DeployObjectMessage(guid : PlanetSideGUID,
                                     str : String,
                                     unk1 : Long,
                                     unk2 : Long,
                                     unk3 : Long)
  extends PlanetSideGamePacket {
  type Packet = DeployObjectMessage
  def opcode = GamePacketOpcode.ObjectDeployedMessage
  def encode = DeployObjectMessage.encode(this)
}

object DeployObjectMessage extends Marshallable[DeployObjectMessage] {
  implicit val codec : Codec[DeployObjectMessage] = (
    ("object_guid" | PlanetSideGUID.codec) ::
      ("str" | PacketHelpers.encodedString) ::
      ("unk1" | uint32L) ::
      ("unk2" | uint32L) ::
      ("unk3" | uint32L)
    ).xmap[DeployObjectMessage] (
    {
      case guid :: str :: u1 :: u2 :: u3 :: HNil =>
        DeployObjectMessage(guid, str, u1, u2, u3)
    },
    {
      case DeployObjectMessage(guid, str, u1, u2, u3) =>
        //truncate string length to 100 characters; raise no warnings
        val limitedStr : String =  if(str.length() > 100) { str.substring(0,100) } else { str }
        guid :: limitedStr :: u1 :: u2 :: u3 :: HNil
    }
  )
}
