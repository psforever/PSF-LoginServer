// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.types.PlanetSideEmpire
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation of the capture flag portion of `ObjectCreateDetailedMessage` packet data.
  * This creates what is known as a lattice logic unit, or LLU.
  * It is originally spawned in the base object called the lattice link socket during certain base captures.<br>
  * <br>
  * Players can not directly interact with the capture flag.
  * Whenever an applicable player is nearby, that client will rapidly fire off `ItemUseMessage` packets to the server.
  * The capture flag will be picked-up by the player and stored in a special slot that is not part of their inventory.
  * A special dropping keybind has been prepared to relinquish the capture flag back to the game world.
  * @param pos the position + orientation + velocity of the LLU where it is dropped/spawned
  * @param faction the empire whose players may interact with this capture flag
  * @param owningBaseGuid The GUID of the base that this LLU belongs to
  * @param targetBaseGuid The GUID of the base that this LLU must be taken to
  * @param milliseconds_remaining The number of milliseconds left on the timer for this LLU - should match the CC timer
  */
final case class CaptureFlagData(
    pos: PlacementData,
    faction: PlanetSideEmpire.Value,
    owningBaseGuid : Int,
    targetBaseGuid : Int,
    milliseconds_remaining : Long
) extends ConstructorData {
  override def bitsize: Long = 88L + pos.bitsize
}

object CaptureFlagData extends Marshallable[CaptureFlagData] {
  implicit val codec: Codec[CaptureFlagData] = (
    ("pos" | PlacementData.codec) ::
      ("faction" | PlanetSideEmpire.codec) ::
      bool ::
      uint4L ::
      uint16L ::
      ("owningBaseGuid" | uint8L) ::
      uint8L ::
      ("targetBaseGuid" | uint8L) ::
      uint8L ::
      ("milliseconds_remaining" | uint32L) ::
      uint(bits = 1)
  ).exmap[CaptureFlagData](
    {
      case pos :: faction :: false :: 4 :: 0 :: owningBaseGuid :: 0 :: targetBaseGuid :: 0 :: milliseconds_remaining :: 0 :: HNil =>
        Attempt.Successful(CaptureFlagData(pos, faction, owningBaseGuid, targetBaseGuid, milliseconds_remaining))

      case data =>
        Attempt.failure(Err(s"invalid capture flag data format - $data"))
    },
    {
      case CaptureFlagData(pos, faction, owningBaseGuid, targetBaseGuid, milliseconds_remaining) =>
        Attempt.successful(pos :: faction :: false :: 4 :: 0 :: owningBaseGuid :: 0 :: targetBaseGuid :: 0 :: milliseconds_remaining :: 0 :: HNil)
    }
  )
}
