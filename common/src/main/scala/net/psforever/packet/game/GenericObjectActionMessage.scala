// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Dispatched by the server to enact an effect on some game object.
  * (Write more some other time.)
  * @param object_guid the target object
  * @param code the action code
  *             96, 97, 98, 99 - Makes the vehicle bounce slightly. Have seen this in packet captures after taking a vehicle through a warpgate
  *             200, 201, 202, 203 - For aircraft - client shows "THe bailing mechanism failed! To fix the mechanism, land and repair the vehicle!"
  *             224 - Sets vehicle or player to be black ops
  *             228 - Reverts player from black ops
  */
final case class GenericObjectActionMessage(object_guid : PlanetSideGUID,
                                            code : Int)
  extends PlanetSideGamePacket {
  type Packet = GenericObjectActionMessage
  def opcode = GamePacketOpcode.GenericObjectActionMessage
  def encode = GenericObjectActionMessage.encode(this)
}

object GenericObjectActionMessage extends Marshallable[GenericObjectActionMessage] {
  implicit val codec : Codec[GenericObjectActionMessage] = (
    ("object_guid" | PlanetSideGUID.codec) ::
      ("code" | uint8L) ::
      ("ex" | bits) //"code" may extract at odd sizes
    ).exmap[GenericObjectActionMessage] (
    {
      case guid :: code :: _ :: HNil =>
        Attempt.Successful(GenericObjectActionMessage(guid, code))
    },
    {
      case GenericObjectActionMessage(guid, code) =>
        Attempt.Successful(guid :: code :: BitVector.empty :: HNil)
    }
  )
}
