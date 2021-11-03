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
  * @param object_guid the target object<br/>
  * @param code the action code (0-63)<br/>
  *             6 - Deconstructs player<br/>
  *             7 - Start imprinting process (progress bar + character animation)<br/>
  *             8 - Finish imprinting?<br/>
  *             9 - Cloak<br/>
  *             10 - Uncloak<br/>
  *             11 - Deploy capital base shield pole with animation and broadcasts "The capitol force dome at X has been activated"<br/>
  *             12 - Stow capital base shield pole with animation and broadcasts "The capitol force dome at X has been deactivated"<br/>
  *             13 - Deploy capital base shield pole (instantly, unless still in the middle of the stow animation)<br/>
  *             14 - Changes capture console to say "Facility hacked by the [Faction] LLU has been spawned." when looked at<br/>
  *             15 - Displays "This facility's generator is under attack!"<br/>
  *             16 - Displays "Generator has Overloaded! Evacuate Generator Room Immediately!"<br/>
  *             17 - Displays "This facility's generator is back on line"<br/>
  *             19 - Cause mines to explode<br/>
  *             20 - Hit flinch? (orig, 82->80)<br/>
  *             21 - Reset build cooldown from using an ACE<br/>
  *             22 - ???? (Has been seen on vehicle pad objects, possibly some sort of reset flag after base faction flip / hack clear?)<br/>
  *             23 - Plays vehicle pad animation moving downwards<br/>
  *             24 - Makes the vehicle bounce slightly. Have seen this in packet captures after taking a vehicle through a warpgate<br/>
  *             25 - for observed driven BFR's, model resets animation following GOAM90?<br>
  *             27 - Activates the router internal telepad for linking<br/>
  *             28 - Activates the router internal telepad for linking<br/>
  *             29 - Activates the telepad deployable (also used on the router's internal telepad)<br/>
  *             30 - Activates the telepad deployable (also used on the router's internal telepad)<br/>
  *             31 - Animation during router teleportation (source)<br/>
  *             32 - Animation during router teleportation (destination)<br/>
  *             34 - Time until item can be used ?????<br/>
  *             38 - for BFR's, enable a disabled arm weapon<br>
  *             39 - for BFR's, disable an enabled arm weapon<br>
  *             44 - for BFR's, animates the energy shield<br>
  *             45 - for BFR's, energy shield dissipates<br>
  *             46 - for BFR's, causes an explosions on the machine's midsection<br>
  *             48 - for BFR's, Control Interface unstable messages<br>
  *             49 - for BFR's, Control Interface malfunction messages<br>
  *             50 - For aircraft - client shows "The bailing mechanism failed! To fix the mechanism, land and repair the vehicle!"<br/>
  *             53 - Put down an FDU<br/>
  *             56 - Sets vehicle or player to be black ops<br/>
  *             57 - Reverts player from black ops<br/>
  *             <br>
  *             What are these values?<br>
  *             90? - for observed driven BFR's, model pitches up slightly and stops idle animation<br>
  *             29? - ??? (response to GOAM55)<br>
  *             55? - ??? (client responds with GOAM29)
  * @see `GenericObjectActionEnum`
  */
final case class GenericObjectActionMessage(object_guid: PlanetSideGUID, code: Int) extends PlanetSideGamePacket {
  type Packet = GenericObjectActionMessage
  def opcode = GamePacketOpcode.GenericObjectActionMessage
  def encode = GenericObjectActionMessage.encode(this)
}

object GenericObjectActionMessage extends Marshallable[GenericObjectActionMessage] {
  def apply(object_guid: PlanetSideGUID, code: GenericObjectActionEnum.GenericObjectActionEnum): GenericObjectActionMessage = {
    GenericObjectActionMessage(object_guid, code.id)
  }

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

object GenericObjectActionEnum extends Enumeration {
  type GenericObjectActionEnum = Value

  /** <b>Effect:</b> Capture console displays "Facility hacked by the <Faction> LLU has been spawned." when looked at<br>
    * <b>Target</b>: CaptureTerminal
    */
  val FlagSpawned = Value(14)
}
