// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.Codec
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
  * @param data common data fields and positional information
  * @param owning_facility_guid the GUID of the base that this LLU belongs to
  * @param target_facility_guid the GUID of the base that this LLU must be taken to
  * @param milliseconds_remaining the number of milliseconds left on the timer for this LLU - should match the CC timer
  */
final case class CaptureFlagData(
                                  data: CommonFieldDataWithPlacement,
                                  owning_facility_guid : Int,
                                  target_facility_guid : Int,
                                  milliseconds_remaining : Long
) extends ConstructorData {
  override def bitsize: Long = 65L + data.bitsize
}

object CaptureFlagData extends Marshallable[CaptureFlagData] {
  implicit val codec: Codec[CaptureFlagData] = (
    ("data" | CommonFieldDataWithPlacement.codec) ::
      ("owning_facility_guid" | uint16L) ::
      ("target_facility_guid" | uint16L) ::
      ("milliseconds_remaining" | uint32L) ::
      ignore(size = 1)
  ).xmap[CaptureFlagData](
    {
      case data :: owningBaseGuid :: targetBaseGuid :: milliseconds_remaining :: _ :: HNil =>
        CaptureFlagData(data, owningBaseGuid, targetBaseGuid, milliseconds_remaining)
    },
    {
      case CaptureFlagData(data, owningBaseGuid, targetBaseGuid, milliseconds_remaining) =>
        data :: owningBaseGuid :: targetBaseGuid :: milliseconds_remaining :: () :: HNil
    }
  )
}
