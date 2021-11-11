// Copyright (c) 2017-2021 PSForever
package net.psforever.packet.game.objectcreate

import net.psforever.packet.PacketHelpers
import net.psforever.types.PlanetSideGUID
import scodec.Attempt.Successful
import scodec.Codec
import scodec.codecs._
import shapeless.HNil //note: do not import shapeless.:: at top level; it messes up List's :: functionality

import scala.collection.mutable.ListBuffer

/*
Originally located in `VehicleData` and duplicated in `BattleFrameRoboticsData`, extracted to here and shared.
 */
object MountableInventory {
/**
  * A special method of handling mounted players within the same inventory space as normal `Equipment` can be encountered
  * before restoring normal inventory operations.
  * @see `custom_inventory_codec(Long)`
  * @see `InitialStreamLengthToSeatEntries(Boolean, VehicleFormat)`
  * @param hasVelocity the presence of a velocity field - `vel` - in the `PlacementData` object for this vehicle
  * @param format the subtype for this vehicle
  * @return a `Codec` that translates `InventoryData`
  */
  def custom_inventory_codec(hasVelocity: Boolean, format: VehicleFormat.Type): Codec[InventoryData] =
    custom_inventory_codec(InitialStreamLengthToSeatEntries(hasVelocity, format))

  /**
    * A special method of handling mounted players within the same inventory space as normal `Equipment` can be encountered
    * before restoring normal inventory operations.<br>
    * <br>
    * Due to variable-length fields within `PlayerData` extracted from the input,
    * the distance of the bit(stream) vector to the initial inventory entry is calculated
    * to produce the initial value for padding the `PlayerData` object's name field.
    * After player-related entries have been extracted and processed in isolation,
    * the remainder of the inventory must be handled as standard inventory
    * and finally both groups must be repackaged into a single standard `InventoryData` object.
    * Due to the unique value for the mounted players that must be updated for each entry processed,
    * the entries are temporarily formatted into a linked list before being put back into a normal `List`.<br>
    * <br>
    * 6 June 2018:<br>
    * Due to curious behavior in the vehicle mount access controls,
    * please only encode and decode the driver mount even though all seats are currently reachable.
    * @param length the distance in bits to the first inventory entry
    * @return a `Codec` that translates `InventoryData`
    */
  def custom_inventory_codec(length: Long): Codec[InventoryData] = {
    import shapeless.::
    (
      uint8 >>:~ { size =>
        uint2 ::
        (inventory_seat_codec(
          length,                                   //length of stream until current mount
          CumulativeSeatedPlayerNamePadding(length) //calculated offset of name field in next mount
        ) >>:~ { seats =>
          PacketHelpers.listOfNSized(size - countSeats(seats), InternalSlot.codec).hlist
         })
      }
      ).xmap[InventoryData](
      {
        case _ :: _ :: None :: inv :: HNil =>
          InventoryData(inv)

        case _ :: _ :: seats :: inv :: HNil =>
          InventoryData(unlinkSeats(seats) ++ inv)
      },
      {
        case InventoryData(inv) =>
          val (seats, slots) = inv.partition(entry => entry.objectClass == ObjectClass.avatar)
          inv.size :: 0 :: chainSeats(seats) :: slots :: HNil
      }
    )
  }

  import net.psforever.packet.game.objectcreate.{PlayerData => Player_Data}

  /**
    * Constructor that ignores the coordinate information
    * and performs a vehicle-unique calculation of the padding value.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are mounted.
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param inventory the player's inventory
    * @param drawn_slot the holster that is initially drawn
    * @param accumulative the input position for the stream up to which this entry;
    *                     used to calculate the padding value for the player's name in `CharacterAppearanceData`
    * @return a `PlayerData` object
    */
  def PlayerData(
                  basic_appearance: Int => CharacterAppearanceData,
                  character_data: (Boolean, Boolean) => CharacterData,
                  inventory: InventoryData,
                  drawn_slot: DrawnSlot.Type,
                  accumulative: Long
                ): Player_Data = {
    val appearance = basic_appearance(CumulativeSeatedPlayerNamePadding(accumulative))
    Player_Data(None, appearance, character_data(appearance.b.backpack, true), Some(inventory), drawn_slot)(false)
  }

  /**
    * Constructor for `PlayerData` that ignores the coordinate information and the inventory
    * and performs a vehicle-unique calculation of the padding value.
    * It passes information between the three major divisions for the purposes of offset calculations.
    * This constructor should be used for players that are mounted.
    * @param basic_appearance a curried function for the common fields regarding the the character's appearance
    * @param character_data a curried function for the class-specific data that explains about the character
    * @param drawn_slot the holster that is initially drawn
    * @param accumulative the input position for the stream up to which this entry;
    *                     used to calculate the padding value for the player's name in `CharacterAppearanceData`
    * @return a `PlayerData` object
    */
  def PlayerData(
                  basic_appearance: Int => CharacterAppearanceData,
                  character_data: (Boolean, Boolean) => CharacterData,
                  drawn_slot: DrawnSlot.Type,
                  accumulative: Long
                ): Player_Data = {
    val appearance = basic_appearance(CumulativeSeatedPlayerNamePadding(accumulative))
    Player_Data.apply(None, appearance, character_data(appearance.b.backpack, true), None, drawn_slot)(false)
  }

  /**
    * Distance from the length field of a vehicle creation packet up until the start of the vehicle's inventory data.
    * The only field excluded belongs to the original opcode for the packet.
    * The parameters outline reasons why the length of the stream would be different
    * and are used to determine the exact difference value.<br>
    * <br>
    * Note:<br>
    * 198 includes the `ObjectCreateMessage` packet fields, without parent data,
    * the `VehicleData` fields,
    * and the first three fields of the `InternalSlot`.
    * @see `ObjectCreateMessage`
    * @param hasVelocity the presence of a velocity field - `vel` - in the `PlacementData` object for this vehicle
    * @param format the subtype for this vehicle
    * @return the length of the bitstream
    */
  def InitialStreamLengthToSeatEntries(hasVelocity: Boolean, format: VehicleFormat.Type): Long = {
    198 +
    (if (hasVelocity) 42 else 0) +
    (format match {
      case VehicleFormat.Utility           => 6
      case VehicleFormat.Variant           => 8
      case VehicleFormat.Battleframe       => 1
      case VehicleFormat.BattleframeFlight => 2
      case _                               => 0
    })
  }

  /**
    * Increment the distance to the next mounted player's `name` field with the length of the previous entry,
    * then calculate the new padding value for that next entry's `name` field.
    * @param base the original distance to the last entry
    * @param next the length of the last entry, if one was parsed
    * @return the padding value, 0-7 bits
    */
  def CumulativeSeatedPlayerNamePadding(base: Long, next: Option[StreamBitSize]): Int = {
    CumulativeSeatedPlayerNamePadding(base + (next match {
      case Some(o) => o.bitsize
      case None    => 0
    }))
  }

  /**
    * Calculate the padding value for the next mounted player character's name `String`.
    * Due to the depth of seated player characters, the `name` field can have a variable amount of padding
    * between the string size field and the first character.
    * Specifically, the padding value is the number of bits after the size field
    * that would cause the first character of the name to be aligned to the first bit of the next byte.
    * The 35 counts the object class, unique identifier, and slot fields of the enclosing `InternalSlot`.
    * The 23 counts all of the fields before the player's `name` field in `CharacterAppearanceData`.
    * @see `InternalSlot`<br>
    *       `CharacterAppearanceData.name`<br>
    *       `VehicleData.InitialStreamLengthToSeatEntries`
    * @param accumulative current entry stream offset (start of this player's entry)
    * @return the padding value, 0-7 bits
    */
  def CumulativeSeatedPlayerNamePadding(accumulative: Long): Int = {
    Player_Data.ByteAlignmentPadding(accumulative + 23 + 35)
  }

  /**
    * The format for the linked list of extracted mounted `PlayerData`.
    * @param seat data for this entry extracted via `PlayerData`
    * @param next the next entry
    */
  private case class InventorySeat(seat: Option[InternalSlot], next: Option[InventorySeat])

  /**
    * Look ahead at the next value to determine if it is an example of a player character
    * and would be processed as a `PlayerData` object.
    * Update the stream read position with each extraction.
    * Continue to process values so long as they represent player character data.
    * @param length the distance in bits to the current inventory entry
    * @param offset the padding value for this entry's player character's `name` field
    * @return a recursive `Codec` that translates subsequent `PlayerData` entries until exhausted
    */
  private def inventory_seat_codec(length: Long, offset: Int): Codec[Option[InventorySeat]] = {
    import shapeless.::
    (
      PacketHelpers.peek(uintL(11)) >>:~ { objClass =>
        conditional(objClass == ObjectClass.avatar, seat_codec(offset)) >>:~ { seat =>
          conditional(
            objClass == ObjectClass.avatar,
            inventory_seat_codec(
              { //length of stream until next mount
                length + (seat match {
                  case Some(o) => o.bitsize
                  case None    => 0
                })
              },
              CumulativeSeatedPlayerNamePadding(length, seat) //calculated offset of name field in next mount
            )
          ).hlist
        }
      }
      ).exmap[Option[InventorySeat]](
      {
        case _ :: None :: None :: HNil =>
          Successful(None)

        case _ :: slot :: Some(next) :: HNil =>
          Successful(Some(InventorySeat(slot, next)))
      },
      {
        case None =>
          Successful(0 :: None :: None :: HNil)

        case Some(InventorySeat(slot, None)) =>
          Successful(ObjectClass.avatar :: slot :: None :: HNil)

        case Some(InventorySeat(slot, next)) =>
          Successful(ObjectClass.avatar :: slot :: Some(next) :: HNil)
      }
    )
  }

  /**
    * Translate data the is verified to involve a player who is seated (mounted) to the parent object at a given slot.
    * The operation performed by this `Codec` is very similar to `InternalSlot.codec`.
    * @param pad the padding offset for the player's name;
    *            0-7 bits;
    *            this padding value must recalculate for each represented mount
    * @see `CharacterAppearanceData`<br>
    *       `VehicleData.InitialStreamLengthToSeatEntries`<br>
    *       `CumulativeSeatedPlayerNamePadding`
    * @return a `Codec` that translates `PlayerData`
    */
  private def seat_codec(pad: Int): Codec[InternalSlot] = {
    import shapeless.::
    (
      ("objectClass" | uintL(11)) ::
      ("guid" | PlanetSideGUID.codec) ::
      ("parentSlot" | PacketHelpers.encodedStringSize) ::
      ("obj" | Player_Data.codec(pad))
      ).xmap[InternalSlot](
      {
        case objectClass :: guid :: parentSlot :: obj :: HNil =>
          InternalSlot(objectClass, guid, parentSlot, obj)
      },
      {
        case InternalSlot(objectClass, guid, parentSlot, obj) =>
          objectClass :: guid :: parentSlot :: obj.asInstanceOf[PlayerData] :: HNil
      }
    )
  }

  /**
    * Count the number of entries in a linked list.
    * @param chain the head of the linked list
    * @return the number of entries
    */
  private def countSeats(chain: Option[InventorySeat]): Int = {
    chain match {
      case Some(_) =>
        var curr  = chain
        var count = 0
        do {
          val link = curr.get
          count += (if (link.seat.nonEmpty) { 1 }
          else { 0 })
          curr = link.next
        } while (curr.nonEmpty)
        count

      case None =>
        0
    }
  }

  /**
    * Transform a linked list of `InventorySlot` slot objects into a formal list of `InternalSlot` objects.
    * @param chain the head of the linked list
    * @return a proper list of the contents of the input linked list
    */
  private def unlinkSeats(chain: Option[InventorySeat]): List[InternalSlot] = {
    var curr = chain
    val out  = new ListBuffer[InternalSlot]
    while (curr.isDefined) {
      val link = curr.get
      link.seat match {
        case None =>
          curr = None
        case Some(seat) =>
          out += seat
          curr = link.next
      }
    }
    out.toList
  }

  /**
    * Transform a formal list of `InternalSlot` objects into a linked list of `InventorySlot` slot objects.
    * @param list a proper list of objects
    * @return a linked list composed of the contents of the input list
    */
  private def chainSeats(list: List[InternalSlot]): Option[InventorySeat] = {
    list match {
      case Nil =>
        None
      case x :: Nil =>
        Some(InventorySeat(Some(x), None))
      case _ :: _ =>
        var link = InventorySeat(Some(list.last), None) //build the chain in reverse order, starting with the last entry
        list.reverse
          .drop(1)
          .foreach { seat =>
            link = InventorySeat(Some(seat), Some(link))
          }
        Some(link)
    }
  }
}

