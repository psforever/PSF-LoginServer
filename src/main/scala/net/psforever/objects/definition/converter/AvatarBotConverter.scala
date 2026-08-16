// Copyright (c) 2026 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Player
import net.psforever.objects.avatar.{AvatarBot, BattleRank}
import net.psforever.objects.equipment.{Equipment, EquipmentSlot}
import net.psforever.packet.objectcreate._
import net.psforever.types.{ExoSuitType, GrenadeState, PlanetSideEmpire, PlanetSideGUID}

import scala.annotation.tailrec
import scala.util.{Success, Try}

class AvatarBotConverter extends ObjectCreateConverter[AvatarBot]() {
  override def ConstructorData(obj: AvatarBot): Try[PlayerData] = {
    import AvatarBotConverter._
    Success(
      PlayerData(
        PlacementData(obj.Position, obj.Orientation, None),
        MakeAppearanceData(obj),
        MakeCharacterData(obj),
        MakeInventoryData(obj),
        GetDrawnSlot(obj)
      )
    )
  }

  override def DetailedConstructorData(obj: AvatarBot): Try[DetailedPlayerData] = {
    import AvatarBotConverter._
    Success(
      DetailedPlayerData.apply(
        PlacementData(obj.Position, obj.Orientation, None),
        MakeAppearanceData(obj),
        MakeDetailedCharacterData(obj),
        MakeDetailedInventoryData(obj),
        GetDrawnSlot(obj)
      )
    )
  }
}

object AvatarBotConverter {

  /**
    * Compose some data from a `AvatarBot` into a representation common to both `CharacterData` and `DetailedCharacterData`.
    * @param obj the `AvatarBot` game object
    * @return the resulting `CharacterAppearanceData`
    */
  def MakeAppearanceData(obj: AvatarBot): Int => CharacterAppearanceData = {
    val aa: Int => CharacterAppearanceA = CharacterAppearanceA(
      obj.basic,
      CommonFieldData(obj.Faction, bops = false, false, v1 = false, None, obj.Jammed, v5 = None, PlanetSideGUID(0)),
      obj.ExoSuit,
      0,
      0,
      0,
      0,
      0,
      0
    )
    val ab: (Boolean, Int) => CharacterAppearanceB = CharacterAppearanceB(
      0,
      "",
      outfit_logo = 0,
      unk1 = false,
      false,
      unk2 = false,
      unk3 = false,
      unk4 = false,
      facingPitch = obj.Orientation.y,
      facingYawUpper = obj.FacingYawUpper,
      false,
      GrenadeState.None,
      obj.Cloaked,
      unk5 = false,
      unk6 = false,
      charging_pose = false,
      unk7 = false,
      on_zipline = None
    )
    CharacterAppearanceData(aa, ab, obj.decoration.ribbonBars)
  }

  def MakeCharacterData(obj: AvatarBot): (Boolean, Boolean) => CharacterData = {
    val uniformStyle = obj.br.uniformStyle
    val cosmetics = if (BattleRank.showCosmetics(uniformStyle)) {
      obj.decoration.cosmetics
    } else {
      None
    }
    val MaxArmor = obj.MaxArmor
    val armor = if (MaxArmor == 0) {
      0
    } else {
      StatConverter.Health(obj.Armor, MaxArmor)
    }
    CharacterData(
      StatConverter.Health(obj.Health, obj.MaxHealth),
      armor,
      uniformStyle,
      0,
      obj.cr.value,
      obj.implants.flatten.filter(_.active).flatMap(_.definition.implantType.effect).toList,
      cosmetics
    )
  }

  def MakeDetailedCharacterData(obj: AvatarBot): Option[Int] => DetailedCharacterData = {
    val maxOpt: Option[Long] = if (obj.ExoSuit == ExoSuitType.MAX) {
      Some(0L)
    } else {
      None
    }
    val cosmetics = if (BattleRank.BR24.experience >= obj.bep) {
      obj.decoration.cosmetics
    } else {
      None
    }
    val ba: DetailedCharacterA = DetailedCharacterA(
      obj.bep,
      obj.cep,
      0L,
      0L,
      0L,
      obj.MaxHealth,
      obj.Health,
      unk4 = false,
      obj.Armor,
      0L,
      obj.maxStamina,
      obj.stamina,
      maxOpt,
      0,
      0,
      0L,
      List(0, 0, 0, 0, 0, 0),
      obj.certifications.toList.sortBy(_.value) //TODO is sorting necessary?
    )
    val bb: (Long, Option[Int]) => DetailedCharacterB = DetailedCharacterB(
      None,
      Nil,
      Nil,
      Nil,
      Nil,
      tutorials = List.empty[String], //TODO tutorial list
      0L,
      0L,
      0L,
      0L,
      0L,
      None, //Some(ImprintingProgress(0, 0)),
      Nil,
      Nil,
      unkC = false,
      cosmetics
    )
    pad_length: Option[Int] => DetailedCharacterData(ba, bb(0, pad_length))(pad_length)
  }

  def MakeInventoryData(obj: AvatarBot): InventoryData = {
    InventoryData(MakeHolsters(obj, BuildEquipment))
  }

  def MakeDetailedInventoryData(obj: AvatarBot): InventoryData = {
    InventoryData(
      MakeHolsters(obj, BuildDetailedEquipment) ++
       MakeFifthSlot(obj) ++
       MakeInventory(obj)
    )
  }

  /**
    * Given a player with an inventory, convert the contents of that inventory into converted-decoded packet data.
    * The inventory is not represented in a `0x17` `AvatarBot`, so the conversion is only valid for `0x18` avatars.
    * It will always be "`Detailed`".
    * @param obj the `AvatarBot` game object
    * @return a list of all items that were in the inventory in decoded packet form
    */
  private def MakeInventory(obj: AvatarBot): List[InternalSlot] = {
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
    * The fifth slot is only represented if the `AvatarBot` is an `0x18` type.
    * @param obj the `AvatarBot` game object
    * @param builder the function used to transform to the decoded packet form
    * @return a list of all items that were in the holsters in decoded packet form
    */
  def MakeHolsters(obj: AvatarBot, builder: (Int, Equipment) => InternalSlot): List[InternalSlot] = {
    recursiveMakeHolsters(obj.Holsters().iterator, builder)
  }

  /**
    * Given a player with equipment holsters, convert any content of the fifth holster slot into converted-decoded packet data.
    * The fifth holster is a curious divider between the standard holsters and the formal inventory.
    * This fifth slot is only ever represented if the `AvatarBot` is an `0x18` type.
    * @param obj the `AvatarBot` game object
    * @return a list of any item that was in the fifth holster in decoded packet form
    */
  private def MakeFifthSlot(obj: AvatarBot): List[InternalSlot] = {
    obj.Slot(slot = 5).Equipment match {
      case Some(equip) =>
        List(InternalSlot(
          equip.Definition.ObjectId,
          equip.GUID,
          5,
          DetailedLockerContainerData(
            CommonFieldData(PlanetSideEmpire.NEUTRAL, bops=false, alternate=false, v1=true, None, jammered=false, None, PlanetSideGUID(0)),
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
    * @param obj the `AvatarBot` game object
    * @return the holster's Enumeration value
    */
  def GetDrawnSlot(obj: AvatarBot): DrawnSlot.Value = {
    obj.DrawnSlot match {
      case Player.HandsDownSlot | Player.FreeHandSlot => DrawnSlot.None
      case n                                          => DrawnSlot(n)
    }
  }
}
