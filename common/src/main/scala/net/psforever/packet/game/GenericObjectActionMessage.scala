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
  *             24 - deconstructs player
  *             28 - start imprinting process (progress bar + character animation)
  *             32 - finish imprinting?
  *             36 - cloak
  *             40 - uncloak
  *             82 - hit flinch?
  *             138 - time till item can be used ?????
  *             44, 45, 46, 47 - Deploy capital base shield pole with animation and broadcasts "The capitol force dome at X has been activated"
  *             48, 49, 50, 51 - Stow capital base shield pole with animation and broadcasts "The capitol force dome at X has been deactivated"
  *             52, 53, 54, 55 - Deploy capital base shield pole (instantly, unless still in the middle of the stow animation)
  *             60, 61, 62, 63 - Displays "This facility's generator is under attack!"
  *             64, 65, 66, 67 - Displays "Generator has Overloaded! Evacuate Generator Room Immediately!"
  *             68, 69, 70, 71 - Displays "This facility's generator is back on line"
  *             88, 89, 90, 91 - ???? (Has been seen on vehicle pad objects)
  *             92, 93, 94, 95 - Plays vehicle pad animation moving downwards
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
