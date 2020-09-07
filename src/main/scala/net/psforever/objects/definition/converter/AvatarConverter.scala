// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Player
import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{ExoSuitType, GrenadeState, PlanetSideEmpire, PlanetSideGUID}

import scala.annotation.tailrec
import scala.util.{Success, Try}

class AvatarConverter extends ObjectCreateConverter[Player]() {
  override def ConstructorData(obj: Player): Try[PlayerData] = {
    import AvatarConverter._
    Success(
      if (obj.VehicleSeated.isEmpty) {
        PlayerData(
          PlacementData(obj.Position, obj.Orientation, obj.Velocity),
          MakeAppearanceData(obj),
          MakeCharacterData(obj),
          MakeInventoryData(obj),
          GetDrawnSlot(obj)
        )
      } else {
        PlayerData(
          MakeAppearanceData(obj),
          MakeCharacterData(obj),
          MakeInventoryData(obj),
          DrawnSlot.None
        )
      }
    )
  }

  override def DetailedConstructorData(obj: Player): Try[DetailedPlayerData] = {
    import AvatarConverter._
    Success(
      if (obj.VehicleSeated.isEmpty) {
        DetailedPlayerData.apply(
          PlacementData(obj.Position, obj.Orientation, obj.Velocity),
          MakeAppearanceData(obj),
          MakeDetailedCharacterData(obj),
          MakeDetailedInventoryData(obj),
          GetDrawnSlot(obj)
        )
      } else {
        DetailedPlayerData.apply(
          MakeAppearanceData(obj),
          MakeDetailedCharacterData(obj),
          MakeDetailedInventoryData(obj),
          GetDrawnSlot(obj)
        )
      }
    )
  }
}

object AvatarConverter {

  /**
    * Compose some data from a `Player` into a representation common to both `CharacterData` and `DetailedCharacterData`.
    * @param obj the `Player` game object
    * @return the resulting `CharacterAppearanceData`
    */
  def MakeAppearanceData(obj: Player): Int => CharacterAppearanceData = {
    val alt_model_flag: Boolean = obj.isBackpack
    val aa: Int => CharacterAppearanceA = CharacterAppearanceA(
      BasicCharacterData(obj.Name, obj.Faction, obj.Sex, obj.Head, obj.Voice),
      CommonFieldData(
        obj.Faction,
        bops = false,
        alt_model_flag,
        false,
        None,
        obj.Jammed,
        None,
        v5 = None,
        PlanetSideGUID(0)
      ),
      obj.ExoSuit,
      0,
      obj.CharId,
      0,
      0,
      0,
      0
    )
    val ab: (Boolean, Int) => CharacterAppearanceB = CharacterAppearanceB(
      0L,
      outfit_name = "",
      outfit_logo = 0,
      false,
      obj.isBackpack,
      false,
      false,
      false,
      facingPitch = obj.Orientation.y,
      facingYawUpper = obj.FacingYawUpper,
      obj.avatar.lookingForSquad,
      GrenadeState.None,
      obj.Cloaked,
      unk5 = false,
      unk6 = false,
      charging_pose = false,
      unk7 = false,
      on_zipline = None
    )
    CharacterAppearanceData(aa, ab, RibbonBars())
  }

  def MakeCharacterData(obj: Player): (Boolean, Boolean) => CharacterData = {
    val MaxArmor = obj.MaxArmor
    CharacterData(
      StatConverter.Health(obj.Health, obj.MaxHealth),
      if (MaxArmor == 0) {
        0
      } else {
        StatConverter.Health(obj.Armor, MaxArmor)
      },
      obj.avatar.br.uniformStyle,
      0,
      obj.avatar.cr.value,
      obj.avatar.implants.flatten.filter(_.active).flatMap(_.definition.implantType.effect).toList,
      obj.avatar.cosmetics
    )
  }

  def MakeDetailedCharacterData(obj: Player): Option[Int] => DetailedCharacterData = {
    val maxOpt: Option[Long] = if (obj.ExoSuit == ExoSuitType.MAX) { Some(0L) }
    else { None }
    val ba: DetailedCharacterA = DetailedCharacterA(
      obj.avatar.bep,
      obj.avatar.cep,
      0L,
      0L,
      0L,
      obj.MaxHealth,
      obj.Health,
      unk4 = false,
      obj.Armor,
      0L,
      obj.avatar.maxStamina,
      obj.avatar.stamina,
      maxOpt,
      0,
      0,
      0L,
      List(0, 0, 0, 0, 0, 0),
      obj.avatar.certifications.toList.sortBy(_.value) //TODO is sorting necessary?
    )
    val bb: (Long, Option[Int]) => DetailedCharacterB = DetailedCharacterB(
      None,
      obj.avatar.implants.flatten.map(_.toEntry).toList,
      Nil,
      Nil,
      obj.avatar.firstTimeEvents.toList,
      tutorials = List.empty[String], //TODO tutorial list
      0L,
      0L,
      0L,
      0L,
      0L,
      Some(DCDExtra2(0, 0)),
      Nil,
      Nil,
      false,
      obj.avatar.cosmetics
    )
    pad_length: Option[Int] => DetailedCharacterData(ba, bb(obj.avatar.bep, pad_length))(pad_length)
  }

  def MakeInventoryData(obj: Player): InventoryData = {
    InventoryData(MakeHolsters(obj, BuildEquipment).sortBy(_.parentSlot))
  }

  def MakeDetailedInventoryData(obj: Player): InventoryData = {
    InventoryData(
      (MakeHolsters(obj, BuildDetailedEquipment) ++
       MakeFifthSlot(obj) ++
       MakeInventory(obj)).sortBy(_.parentSlot)
    )
  }

  /**
    * Given a player with an inventory, convert the contents of that inventory into converted-decoded packet data.
    * The inventory is not represented in a `0x17` `Player`, so the conversion is only valid for `0x18` avatars.
    * It will always be "`Detailed`".
    * @param obj the `Player` game object
    * @return a list of all items that were in the inventory in decoded packet form
    */
  private def MakeInventory(obj: Player): List[InternalSlot] = {
    obj.Inventory.Items
      .map(item => {
        val equip: Equipment = item.obj
        InternalSlot(
          equip.Definition.ObjectId,
          equip.GUID,
          item.start,
          equip.Definition.Packet.DetailedConstructorData(equip).get
        )
      })
  }

  /**
    * Given a player with equipment holsters, convert the contents of those holsters into converted-decoded packet data.
    * The decoded packet form is determined by the function in the parameters as both `0x17` and `0x18` conversions are available,
    * with exception to the contents of the fifth slot.
    * The fifth slot is only represented if the `Player` is an `0x18` type.
    * @param obj the `Player` game object
    * @param builder the function used to transform to the decoded packet form
    * @return a list of all items that were in the holsters in decoded packet form
    */
  private def MakeHolsters(obj: Player, builder: (Int, Equipment) => InternalSlot): List[InternalSlot] = {
    recursiveMakeHolsters(obj.Holsters().iterator, builder)
  }

  /**
    * Given a player with equipment holsters, convert any content of the fifth holster slot into converted-decoded packet data.
    * The fifth holster is a curious divider between the standard holsters and the formal inventory.
    * This fifth slot is only ever represented if the `Player` is an `0x18` type.
    * @param obj the `Player` game object
    * @return a list of any item that was in the fifth holster in decoded packet form
    */
  private def MakeFifthSlot(obj: Player): List[InternalSlot] = {
    obj.Slot(5).Equipment match {
      case Some(equip) =>
        //List(BuildDetailedEquipment(5, equip))
        List(InternalSlot(
          equip.Definition.ObjectId,
          equip.GUID,
          5,
          DetailedLockerContainerData(
            CommonFieldData(PlanetSideEmpire.NEUTRAL, false, false, true, None, false, None, None, PlanetSideGUID(0)),
            None
          )
        ))
      case _ =>
        Nil
    }
  }

  /**
    * A builder method for turning an object into `0x17` decoded packet form.
    * @param index the position of the object
    * @param equip the game object
    * @return the game object in decoded packet form
    */
  private def BuildEquipment(index: Int, equip: Equipment): InternalSlot = {
    InternalSlot(equip.Definition.ObjectId, equip.GUID, index, equip.Definition.Packet.ConstructorData(equip).get)
  }

  /**
    * A builder method for turning an object into `0x18` decoded packet form.
    * @param index the position of the object
    * @param equip the game object
    * @return the game object in decoded packet form
    */
  def BuildDetailedEquipment(index: Int, equip: Equipment): InternalSlot = {
    InternalSlot(
      equip.Definition.ObjectId,
      equip.GUID,
      index,
      equip.Definition.Packet.DetailedConstructorData(equip).get
    )
  }

  /**
    * Given some equipment holsters, convert the contents of those holsters into converted-decoded packet data.
    * @param iter an `Iterator` of `EquipmentSlot` objects that are a part of the player's holsters
    * @param builder the function used to transform to the decoded packet form
    * @param list the current `List` of transformed data
    * @param index which holster is currently being explored
    * @return the `List` of inventory data created from the holsters
    */
  @tailrec private def recursiveMakeHolsters(
      iter: Iterator[EquipmentSlot],
      builder: (Int, Equipment) => InternalSlot,
      list: List[InternalSlot] = Nil,
      index: Int = 0
  ): List[InternalSlot] = {
    if (!iter.hasNext) {
      list
    } else {
      val slot: EquipmentSlot = iter.next()
      if (slot.Equipment.isDefined) {
        val equip: Equipment = slot.Equipment.get
        recursiveMakeHolsters(
          iter,
          builder,
          list :+ builder(index, equip),
          index + 1
        )
      } else {
        recursiveMakeHolsters(iter, builder, list, index + 1)
      }
    }
  }

  /**
    * Resolve which holster the player has drawn, if any.
    * @param obj the `Player` game object
    * @return the holster's Enumeration value
    */
  def GetDrawnSlot(obj: Player): DrawnSlot.Value = {
    obj.DrawnSlot match {
      case Player.HandsDownSlot | Player.FreeHandSlot => DrawnSlot.None
      case n                                          => DrawnSlot(n)
    }
  }
}
