// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{EquipmentSlot, Player}
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.objectcreate.{BasicCharacterData, CharacterAppearanceData, CharacterData, DetailedCharacterData, DrawnSlot, InternalSlot, InventoryData, PlacementData, RibbonBars}
import net.psforever.types.{CharacterGender, GrenadeState, Vector3}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

class CorpseConverter extends AvatarConverter {
  override def ConstructorData(obj : Player) : Try[CharacterData] =
    Failure(new Exception("CorpseConverter should not be used to generate CharacterData"))

  override def DetailedConstructorData(obj : Player) : Try[DetailedCharacterData] = {
    Success(
      DetailedCharacterData(
        MakeAppearanceData(obj),
        0, 0, 0, 0, 0, 0, 0,
        Nil, Nil, Nil, Nil,
        None,
        InventoryData((MakeHolsters(obj) ++ MakeInventory(obj)).sortBy(_.parentSlot)),
        DrawnSlot.None
      )
    )
  }

  /**
    * Compose some data from a `Player` into a representation common to both `CharacterData` and `DetailedCharacterData`.
    * @param obj the `Player` game object
    * @return the resulting `CharacterAppearanceData`
    */
  private def MakeAppearanceData(obj : Player) : CharacterAppearanceData = {
    CharacterAppearanceData(
      PlacementData(obj.Position, Vector3(0,0, obj.Orientation.z)),
      BasicCharacterData(obj.Name, obj.Faction, CharacterGender.Male, 0, 0),
      0,
      false,
      false,
      obj.ExoSuit,
      "",
      0,
      true,
      obj.Orientation.y, //TODO is this important?
      0,
      true,
      GrenadeState.None,
      false,
      false,
      false,
      RibbonBars()
    )
  }

  /**
    * Given a player with an inventory, convert the contents of that inventory into converted-decoded packet data.
    * The inventory is not represented in a `0x17` `Player`, so the conversion is only valid for `0x18` avatars.
    * It will always be "`Detailed`".
    * @param obj the `Player` game object
    * @return a list of all items that were in the inventory in decoded packet form
    */
  private def MakeInventory(obj : Player) : List[InternalSlot] = {
    obj.Inventory.Items
      .map({
        case(_, item) =>
          val equip : Equipment = item.obj
          BuildEquipment(item.start, equip)
      }).toList
  }

  /**
    * Given a player with equipment holsters, convert the contents of those holsters into converted-decoded packet data.
    * The decoded packet form is determined by the function in the parameters as both `0x17` and `0x18` conversions are available,
    * with exception to the contents of the fifth slot.
    * The fifth slot is only represented if the `Player` is an `0x18` type.
    * @param obj the `Player` game object
    * @return a list of all items that were in the holsters in decoded packet form
    */
  private def MakeHolsters(obj : Player) : List[InternalSlot] = {
    recursiveMakeHolsters(obj.Holsters().iterator)
  }

  /**
    * Given some equipment holsters, convert the contents of those holsters into converted-decoded packet data.
    * @param iter an `Iterator` of `EquipmentSlot` objects that are a part of the player's holsters
    * @param list the current `List` of transformed data
    * @param index which holster is currently being explored
    * @return the `List` of inventory data created from the holsters
    */
  @tailrec private def recursiveMakeHolsters(iter : Iterator[EquipmentSlot], list : List[InternalSlot] = Nil, index : Int = 0) : List[InternalSlot] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val slot : EquipmentSlot = iter.next
      if(slot.Equipment.isDefined) {
        val equip : Equipment = slot.Equipment.get
        recursiveMakeHolsters(
          iter,
          list :+ BuildEquipment(index, equip),
          index + 1
        )
      }
      else {
        recursiveMakeHolsters(iter, list, index + 1)
      }
    }
  }

  /**
    * A builder method for turning an object into `0x17` decoded packet form.
    * @param index the position of the object
    * @param equip the game object
    * @return the game object in decoded packet form
    */
  private def BuildEquipment(index : Int, equip : Equipment) : InternalSlot = {
    InternalSlot(equip.Definition.ObjectId, equip.GUID, index, equip.Definition.Packet.DetailedConstructorData(equip).get)
  }
}

object CorpseConverter {
  val converter = new CorpseConverter
}
