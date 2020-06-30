// Copyright (c) 2017 PSForever
package net.psforever.packet.game

import net.psforever.packet.{GamePacketOpcode, Marshallable, PlanetSideGamePacket}
import net.psforever.types.PlanetSideGUID
import scodec.bits.BitVector
import scodec.{Attempt, Codec}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * Dispatched by the server to enact an effect on some game object.
  * (Write more some other time.)
  * @param object_guid the target object
  * @param code the action code (0-63)
  *             6 - Deconstructs player
  *             7 - Start imprinting process (progress bar + character animation)
  *             8 - Finish imprinting?
  *             9 - Cloak
  *             10 - Uncloak
  *             11 - Deploy capital base shield pole with animation and broadcasts "The capitol force dome at X has been activated"
  *             12 - Stow capital base shield pole with animation and broadcasts "The capitol force dome at X has been deactivated"
  *             13 - Deploy capital base shield pole (instantly, unless still in the middle of the stow animation)
  *             15 - Displays "This facility's generator is under attack!"
  *             16 - Displays "Generator has Overloaded! Evacuate Generator Room Immediately!"
  *             17 - Displays "This facility's generator is back on line"
  *             19 - Cause mines to explode
  *             20 - Hit flinch? (orig, 82->80)
  *             21 - Reset build cooldown from using an ACE
  *             22 - ???? (Has been seen on vehicle pad objects, possibly some sort of reset flag after base faction flip / hack clear?)
  *             23 - Plays vehicle pad animation moving downwards
  *             24 - Makes the vehicle bounce slightly. Have seen this in packet captures after taking a vehicle through a warpgate
  *             27 - Activates the router internal telepad for linking
  *             28 - Activates the router internal telepad for linking
  *             29 - Activates the telepad deployable (also used on the router's internal telepad)
  *             30 - Activates the telepad deployable (also used on the router's internal telepad)
  *             31 - Animation during router teleportation (source)
  *             32 - Animation during router teleportation (destination)
  *             34 - Time until item can be used ?????
  *             50 - For aircraft - client shows "The bailing mechanism failed! To fix the mechanism, land and repair the vehicle!"
  *             53 - Put down an FDU
  *             56 - Sets vehicle or player to be black ops
  *             57 - Reverts player from black ops
  */
final case class GenericObjectActionMessage(object_guid: PlanetSideGUID, code: Int) extends PlanetSideGamePacket {
  type Packet = GenericObjectActionMessage
  def opcode = GamePacketOpcode.GenericObjectActionMessage
  def encode = GenericObjectActionMessage.encode(this)
}

object GenericObjectActionMessage extends Marshallable[GenericObjectActionMessage] {
  implicit val codec: Codec[GenericObjectActionMessage] = (
    ("object_guid" | PlanetSideGUID.codec) ::
      ("code" | uint(bits = 6)) ::
      ("ex" | bits) //"code" may extract at odd sizes
  ).exmap[GenericObjectActionMessage](
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
