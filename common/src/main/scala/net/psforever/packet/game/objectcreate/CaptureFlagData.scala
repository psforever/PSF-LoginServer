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
  * @param faction the empire whose players may interact with this capture flag
  * @param unk1 na
  * @param unk2 na
  * @param unk3 na
  * @param unk4 na
  */
final case class CaptureFlagData(pos : PlacementData,
                                 faction : PlanetSideEmpire.Value,
                                 unk1 : Int,
                                 unk2 : Int,
                                 unk3 : Int,
                                 unk4 : Int
                                ) extends ConstructorData {
  override def bitsize : Long = 88L + pos.bitsize
}

object CaptureFlagData extends Marshallable[CaptureFlagData] {
  implicit val codec : Codec[CaptureFlagData] = (
    ("pos" | PlacementData.codec) ::
      ("faction" | PlanetSideEmpire.codec) ::
      bool ::
      uint4L ::
      uint16L ::
      ("unk1" | uint8L) ::
      uint8L ::
      ("unk2" | uint8L) ::
      uint8L ::
      ("unk3" | uint16L) :: //probably a PlanetSideGUID
      ("unk4" | uint8L) ::
      uint(9)
  ).exmap[CaptureFlagData] (
    {
      case pos :: fac :: false :: 4 :: 0 :: unk1 :: 0 :: unk2 :: 0 :: unk3 :: unk4 :: 0 :: HNil =>
        Attempt.Successful(CaptureFlagData(pos, fac, unk1, unk2, unk3, unk4))
      case _ =>
        Attempt.failure(Err("invalid capture flag data"))
    },
    {
      case CaptureFlagData(pos, fac, unk1, unk2, unk3, unk4) =>
        Attempt.successful(pos :: fac :: false :: 4 :: 0 :: unk1 :: 0 :: unk2 :: 0 :: unk3 :: unk4 :: 0 :: HNil)
    }
  )
}
