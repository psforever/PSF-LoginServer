// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import scodec.codecs._
import scodec.{Attempt, Codec, Err}
import shapeless.{::, HNil}

/**
  * A representation of the inventory portion of `ObjectCreateDetailedMessage` packet data that contains the items in the avatar's locker space.<br>
  * <br>
  * Although these items are technically always loaded and registered with globally unique identifiers for the current zone,
  * the actual container for them, in grid format, can only be accessed by interacting with locker objects in the game world.
  * Items are generally added and removed in the same way as with any other opened inventory.
  * Unlike other inventories, however, locker space is personal to an avatar and can not be accessed by other players.
  * @param unk na
  * @param contents the items in the inventory
  */
final case class DetailedLockerContainerData(unk : Int,
                                             contents : Option[List[InternalSlot]]
                                            ) extends ConstructorData {
  override def bitsize : Long = {
    val base : Long = 40L
    var invSize : Long = 0L //length of all items in inventory
    if(contents.isDefined) {
      invSize = InventoryData.BaseSize
      for(item <- contents.get) {
        invSize += item.bitsize
      }
    }
    base + invSize
  }
}

object DetailedLockerContainerData extends Marshallable[DetailedLockerContainerData] {
  /**
    * Overloaded constructor for creating `DetailedLockerContainerData` without a list of contents.
    * @param unk na
    * @return a `DetailedLockerContainerData` object
    */
  def apply(unk : Int) : DetailedLockerContainerData =
    new DetailedLockerContainerData(unk, None)

  /**
    * Overloaded constructor for creating `DetailedLockerContainerData` containing known items.
    * @param unk na
    * @param contents the items in the inventory
    * @return a `DetailedLockerContainerData` object
    */
  def apply(unk : Int, contents : List[InternalSlot]) : DetailedLockerContainerData =
    new DetailedLockerContainerData(unk, Some(contents))

  /**
    * Overloaded constructor for creating `DetailedLockerContainerData` while masking use of `InternalSlot`.
    * @param cls the code for the type of object being constructed
    * @param guid the GUID this object will be assigned
    * @param parentSlot a parent-defined slot identifier that explains where the child is to be attached to the parent
    * @param locker the `DetailedLockerContainerData`
    * @return an `InternalSlot` object that encapsulates `DetailedLockerContainerData`
    */
  def apply(cls : Int, guid : PlanetSideGUID, parentSlot : Int, locker : DetailedLockerContainerData) : InternalSlot =
    new InternalSlot(cls, guid, parentSlot, locker)

  implicit val codec : Codec[DetailedLockerContainerData] = (
    uint4L ::
      ("unk" | uint4L) :: // 8 - common - 4 - safe, 2 - stream misalignment, 1 - safe, 0 - common
      uint(15) ::
      uint16L :: //always 1
      optional(bool, InventoryData.codec_detailed)
    ).exmap[DetailedLockerContainerData] (
    {
      case 0xC :: unk :: 0 :: 1 :: None :: HNil =>
        Attempt.successful(DetailedLockerContainerData(unk, None))

      case 0xC :: unk :: 0 :: 1 :: Some(InventoryData(list)) :: HNil =>
        Attempt.successful(DetailedLockerContainerData(unk, Some(list)))
      case _ =>
        Attempt.failure(Err(s"invalid locker container data format"))
    },
    {
      case DetailedLockerContainerData(unk, None) =>
        Attempt.successful(0xC :: unk :: 0 :: 1 :: None :: HNil)

      case DetailedLockerContainerData(unk, Some(list)) =>
        Attempt.successful(0xC :: unk :: 0 :: 1 :: Some(InventoryData(list)) :: HNil)
    }
  )
}
