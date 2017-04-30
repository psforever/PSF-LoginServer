// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import scodec.{Attempt, Codec, Err}
import scodec.codecs._
import shapeless.{::, HNil}

/**
  * A representation for a game object that can contain items.<br>
  * <br>
  * For whatever reason, these "lockers" are typically placed at the origin coordinates.
  * @param inventory the items inside his locker
  */
final case class LockerContainerData(inventory : InventoryData) extends ConstructorData {
  override def bitsize : Long =  105L + inventory.bitsize //81u + 2u + 21u + 1u
}

object LockerContainerData extends Marshallable[LockerContainerData] {
  implicit val codec : Codec[LockerContainerData] = (
    uint32 :: uint32 :: uint(17) :: //can substitute with PlacementData, if ever necessary
      uint2L ::
      uint(21) ::
      bool ::
      InventoryData.codec
    ).exmap[LockerContainerData] (
    {
      case 0 :: 0 :: 0 :: 3 :: 0 :: true :: inv :: HNil  =>
        Attempt.successful(LockerContainerData(inv))
      case _ =>
        Attempt.failure(Err("invalid locker container format"))
    },
    {
      case LockerContainerData(inv) =>
        Attempt.successful(0L :: 0L :: 0 :: 3 :: 0 :: true :: inv :: HNil)
    }
  )
}
