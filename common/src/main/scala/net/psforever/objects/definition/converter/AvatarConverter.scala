// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{EquipmentSlot, ImplantSlot, Player}
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.objectcreate.{BasicCharacterData, CharacterAppearanceData, CharacterData, Cosmetics, DetailedCharacterData, DrawnSlot, ImplantEffects, ImplantEntry, InternalSlot, InventoryData, PlacementData, RibbonBars, UniformStyle}
import net.psforever.types.{GrenadeState, ImplantType}

import scala.annotation.tailrec
import scala.util.{Success, Try}

class AvatarConverter extends ObjectCreateConverter[Player]() {
  override def ConstructorData(obj : Player) : Try[CharacterData] = {
    val MaxArmor = obj.MaxArmor
    Success(
      CharacterData(
        MakeAppearanceData(obj),
        255 * obj.Health / obj.MaxHealth, //TODO not precise
        if(MaxArmor == 0) { 0 } else { 255 * obj.Armor / MaxArmor }, //TODO not precise
        DressBattleRank(obj),
        DressCommandRank(obj),
        recursiveMakeImplantEffects(obj.Implants.iterator),
        MakeCosmetics(obj.BEP),
        InventoryData(MakeHolsters(obj, BuildEquipment).sortBy(_.parentSlot)), //TODO is sorting necessary?
        GetDrawnSlot(obj)
      )
    )
    //TODO tidy this mess up
  }

  override def DetailedConstructorData(obj : Player) : Try[DetailedCharacterData] = {
    Success(
      DetailedCharacterData(
        MakeAppearanceData(obj),
        obj.BEP,
        obj.CEP,
        obj.MaxHealth,
        obj.Health,
        obj.Armor,
        obj.MaxStamina,
        obj.Stamina,
        obj.Certifications.toList.sortBy(_.id), //TODO is sorting necessary?
        MakeImplantEntries(obj),
        List.empty[String], //TODO fte list
        List.empty[String], //TODO tutorial list
        MakeCosmetics(obj.BEP),
        InventoryData((MakeHolsters(obj, BuildDetailedEquipment) ++ MakeFifthSlot(obj) ++ MakeInventory(obj)).sortBy(_.parentSlot)),
        GetDrawnSlot(obj)
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
      PlacementData(obj.Position, obj.Orientation, obj.Velocity),
      BasicCharacterData(obj.Name, obj.Faction, obj.Sex, obj.Head, obj.Voice),
      0,
      false,
      false,
      obj.ExoSuit,
      "",
      0,
      obj.isBackpack,
      obj.Orientation.y,
      obj.FacingYawUpper,
      true,
      GrenadeState.None,
      false,
      false,
      false,
      RibbonBars()
    )
  }

  /**
    * Select the appropriate `UniformStyle` design for a player's accumulated battle experience points.
    * At certain battle ranks, all exo-suits undergo some form of coloration change.
    * @param obj the `Player` game object
    * @return the resulting uniform upgrade level
    */
  private def DressBattleRank(obj : Player) : UniformStyle.Value = {
    val bep : Long = obj.BEP
    if(bep > 2583440) { //BR25+
      UniformStyle.ThirdUpgrade
    }
    else if(bep > 308989) { //BR14+
      UniformStyle.SecondUpgrade
    }
    else if(bep > 44999) { //BR7+
      UniformStyle.FirstUpgrade
    }
    else { //BR1+
      UniformStyle.Normal
    }
  }

  /**
    * Select the appropriate design for a player's accumulated command experience points.
    * Visual cues for command rank include armlets, anklets, and, finally, a backpack, awarded at different ranks.
    * @param obj the `Player` game object
    * @return the resulting uniform upgrade level
    */
  private def DressCommandRank(obj : Player) : Int = {
    val cep = obj.CEP
    if(cep > 599999) {
      5
    }
    else if(cep > 299999) {
      4
    }
    else if(cep > 149999) {
      3
    }
    else if(cep > 49999) {
      2
    }
    else if(cep > 9999) {
      1
    }
    else {
      0
    }
  }

  /**
    * Transform an `Array` of `Implant` objects into a `List` of `ImplantEntry` objects suitable as packet data.
    * @param obj the `Player` game object
    * @return the resulting implant `List`
    * @see `ImplantEntry` in `DetailedCharacterData`
    */
  private def MakeImplantEntries(obj : Player) : List[ImplantEntry] = {
    val numImplants : Int = DetailedCharacterData.numberOfImplantSlots(obj.BEP)
    val implants = obj.Implants
    (0 until numImplants).map(index => {
      val slot = implants(index)
      slot.Installed match {
        case Some(implant) =>
          if(slot.Initialized) {
            ImplantEntry(slot.Implant, None)
          }
          else {
            ImplantEntry(slot.Implant, Some(implant.Initialization.toInt))
          }
        case None =>
          ImplantEntry(ImplantType.None, None)
      }
    }).toList
  }

  /**
    * Find an active implant whose effect will be displayed on this player.
    * @param iter an `Iterator` of `ImplantSlot` objects
    * @return the effect of an active implant
    */
  @tailrec private def recursiveMakeImplantEffects(iter : Iterator[ImplantSlot]) : Option[ImplantEffects.Value] = {
    if(!iter.hasNext) {
      None
    }
    else {
      val slot = iter.next
      if(slot.Active) {
        slot.Implant match {
          case ImplantType.AdvancedRegen =>
            Some(ImplantEffects.RegenEffects)
          case ImplantType.DarklightVision =>
            Some(ImplantEffects.DarklightEffects)
          case ImplantType.PersonalShield =>
            Some(ImplantEffects.PersonalShieldEffects)
          case ImplantType.Surge =>
            Some(ImplantEffects.SurgeEffects)
          case _ =>
            recursiveMakeImplantEffects(iter)
        }
      }
      else {
        recursiveMakeImplantEffects(iter)
      }
    }
  }

  /**
    * Should this player be of battle rank 24 or higher, they will have a mandatory cosmetics object.
    * @param bep battle experience points
    * @see `Cosmetics`
    * @return the `Cosmetics` options
    */
  protected def MakeCosmetics(bep : Long) : Option[Cosmetics] =
    if(DetailedCharacterData.isBR24(bep)) {
      Some(Cosmetics(false, false, false, false, false))
    }
    else {
      None
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
          InternalSlot(equip.Definition.ObjectId, equip.GUID, item.start, equip.Definition.Packet.DetailedConstructorData(equip).get)
      }).toList
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
  private def MakeHolsters(obj : Player, builder : ((Int, Equipment) => InternalSlot)) : List[InternalSlot] = {
    recursiveMakeHolsters(obj.Holsters().iterator, builder)
  }

  /**
    * Given a player with equipment holsters, convert any content of the fifth holster slot into converted-decoded packet data.
    * The fifth holster is a curious divider between the standard holsters and the formal inventory.
    * This fifth slot is only ever represented if the `Player` is an `0x18` type.
    * @param obj the `Player` game object
    * @return a list of any item that was in the fifth holster in decoded packet form
    */
  private def MakeFifthSlot(obj : Player) : List[InternalSlot] = {
    obj.Slot(5).Equipment match {
      case Some(equip) =>
        BuildDetailedEquipment(5, equip) :: Nil
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
  private def BuildEquipment(index : Int, equip : Equipment) : InternalSlot = {
    InternalSlot(equip.Definition.ObjectId, equip.GUID, index, equip.Definition.Packet.ConstructorData(equip).get)
  }

  /**
    * A builder method for turning an object into `0x18` decoded packet form.
    * @param index the position of the object
    * @param equip the game object
    * @return the game object in decoded packet form
    */
  protected def BuildDetailedEquipment(index : Int, equip : Equipment) : InternalSlot = {
    InternalSlot(equip.Definition.ObjectId, equip.GUID, index, equip.Definition.Packet.DetailedConstructorData(equip).get)
  }

  /**
    * Given some equipment holsters, convert the contents of those holsters into converted-decoded packet data.
    * @param iter an `Iterator` of `EquipmentSlot` objects that are a part of the player's holsters
    * @param builder the function used to transform to the decoded packet form
    * @param list the current `List` of transformed data
    * @param index which holster is currently being explored
    * @return the `List` of inventory data created from the holsters
    */
  @tailrec private def recursiveMakeHolsters(iter : Iterator[EquipmentSlot], builder : ((Int, Equipment) => InternalSlot), list : List[InternalSlot] = Nil, index : Int = 0) : List[InternalSlot] = {
    if(!iter.hasNext) {
      list
    }
    else {
      val slot : EquipmentSlot = iter.next
      if(slot.Equipment.isDefined) {
        val equip : Equipment = slot.Equipment.get
        recursiveMakeHolsters(
          iter,
          builder,
          list :+ builder(index, equip),
          index + 1
        )
      }
      else {
        recursiveMakeHolsters(iter, builder, list, index + 1)
      }
    }
  }

  /**
    * Resolve which holster the player has drawn, if any.
    * @param obj the `Player` game object
    * @return the holster's Enumeration value
    */
  protected def GetDrawnSlot(obj : Player) : DrawnSlot.Value = {
    try { DrawnSlot(obj.DrawnSlot) } catch { case _ : Exception => DrawnSlot.None }
  }
}
