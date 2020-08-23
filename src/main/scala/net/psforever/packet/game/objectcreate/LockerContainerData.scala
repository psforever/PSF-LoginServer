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
  * @param inventory the items inside this locker
  */
final case class LockerContainerData(inventory: Option[InventoryData]) extends ConstructorData {
  override def bitsize: Long = {
    val base: Long = 105L
    (inventory match {
      case Some(inv) => inv.bitsize
      case None      => 0L
    }) + base //81u + 2u + 21u + 1u
  }
}

object LockerContainerData extends Marshallable[LockerContainerData] {
  def apply(): LockerContainerData = new LockerContainerData(None)

  def apply(inventory: InventoryData): LockerContainerData = new LockerContainerData(Some(inventory))

  def apply(inventory: List[InternalSlot]): LockerContainerData =
    new LockerContainerData(Some(InventoryData(inventory)))

  implicit val codec: Codec[LockerContainerData] = (
    uint32 :: uint32 :: uint(17) ::
      uint2L ::
      uint(21) ::
      ("inventory" | optional(bool, InventoryData.codec))
  ).exmap[LockerContainerData](
    {
      case 0 :: 0 :: 0 :: 3 :: 0 :: inventory :: HNil =>
        Attempt.successful(LockerContainerData(inventory))

      case data =>
        Attempt.failure(Err(s"invalid locker container data format - $data"))
    },
    {
      case LockerContainerData(inventory) =>
        Attempt.successful(0L :: 0L :: 0 :: 3 :: 0 :: inventory :: HNil)
    }
  )
}
