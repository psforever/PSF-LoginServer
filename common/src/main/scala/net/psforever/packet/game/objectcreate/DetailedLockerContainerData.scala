// Copyright (c) 2017 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.Marshallable
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.PlanetSideEmpire
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
  * @param data na
  * @param inventory the items in this inventory
  */
final case class DetailedLockerContainerData(data : CommonFieldData,
                                             inventory : Option[InventoryData]
                                            ) extends ConstructorData {
  override def bitsize : Long = {
    val base : Long = 40L
    val invSize : Long = if(inventory.isDefined) { inventory.get.bitsize } else { 0L }
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
    new DetailedLockerContainerData(CommonFieldData(PlanetSideEmpire.NEUTRAL, unk), None)

  /**
    * Overloaded constructor for creating `DetailedLockerContainerData` containing known items.
    * @param unk na
    * @param inventory the items in the inventory
    * @return a `DetailedLockerContainerData` object
    */
  def apply(unk : Int, inventory : List[InternalSlot]) : DetailedLockerContainerData =
    new DetailedLockerContainerData(CommonFieldData(PlanetSideEmpire.NEUTRAL, unk), Some(InventoryData(inventory)))

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
    ("data" | CommonFieldData.codec) ::
      uint16L :: //always 1
      optional(bool, InventoryData.codec_detailed)
    ).exmap[DetailedLockerContainerData] (
    {
      case data :: 1 :: None :: HNil =>
        Attempt.successful(DetailedLockerContainerData(data, None))

      case data :: 1 :: Some(inv) :: HNil =>
        Attempt.successful(DetailedLockerContainerData(data, Some(inv)))

      case data =>
        Attempt.failure(Err(s"invalid detailed locker container data format - $data"))
    },
    {
      case DetailedLockerContainerData(data, None) =>
        Attempt.successful(data :: 1 :: None :: HNil)

      case DetailedLockerContainerData(data, Some(inv)) =>
        Attempt.successful(data :: 1 :: Some(inv) :: HNil)
    }
  )
}
