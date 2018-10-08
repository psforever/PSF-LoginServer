// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{EquipmentSlot, Player}
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{CharacterVoice, GrenadeState, ImplantType}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
  * `CharacterSelectConverter` is a simplified `AvatarConverter`
  * that is tailored for appearance of the player character on the character selection screen.
  * Details that would not be apparent on that screen such as implants or certifications are ignored.
  */
class CharacterSelectConverter extends AvatarConverter {
  override def ConstructorData(obj : Player) : Try[PlayerData] = Failure(new Exception("CharacterSelectConverter should not be used to generate CharacterData"))

  override def DetailedConstructorData(obj : Player) : Try[DetailedPlayerData] = {
    Success(
      DetailedPlayerData.apply(
        PlacementData(0, 0, 0),
        MakeAppearanceData(obj),
        DetailedCharacterData(
          obj.BEP,
          obj.CEP,
          healthMax = 1,
          health = 1,
          armor = 0,
          staminaMax = 1,
          stamina = 1,
          certs = Nil,
          MakeImplantEntries(obj), //necessary for correct stream length
          firstTimeEvents = Nil,
          tutorials = Nil,
          AvatarConverter.MakeCosmetics(obj.BEP)
        ),
        InventoryData(recursiveMakeHolsters(obj.Holsters().iterator)),
        AvatarConverter.GetDrawnSlot(obj)
      )
    )
  }

  /**
    * Compose some data from a `Player` into a representation common to both `CharacterData` and `DetailedCharacterData`.
    * @param obj the `Player` game object
    * @see `AvatarConverter.MakeAppearanceData`
    * @return the resulting `CharacterAppearanceData`
    */
  private def MakeAppearanceData(obj : Player) : (Int)=>CharacterAppearanceData = {
    CharacterAppearanceData(
      BasicCharacterData(obj.Name, obj.Faction, obj.Sex, obj.Head, CharacterVoice.Mute),
      black_ops = false,
      jammered = false,
      obj.ExoSuit,
      outfit_name = "",
      outfit_logo = 0,
      backpack = false,
      facingPitch = 0,
      facingYawUpper = 0,
      lfs = true,
      GrenadeState.None,
      is_cloaking = false,
      charging_pose = false,
      on_zipline = None,
      RibbonBars()
    )
  }

  /**
    * Transform an `Array` of `Implant` objects into a `List` of `ImplantEntry` objects suitable as packet data.
    * @param obj the `Player` game object
    * @return the resulting implant `List`
    * @see `ImplantEntry` in `DetailedCharacterData`
    */
  private def MakeImplantEntries(obj : Player) : List[ImplantEntry] = {
    List.fill[ImplantEntry](DetailedCharacterData.numberOfImplantSlots(obj.BEP))(ImplantEntry(ImplantType.None, None))
  }

  /**
    * Given some equipment holsters, convert the contents of those holsters into converted-decoded packet data.
    * @param iter an `Iterator` of `EquipmentSlot` objects that are a part of the player's holsters
    * @param list the current `List` of transformed data
    * @param index which holster is currently being explored
    * @see `AvatarConverter.recursiveMakeHolsters`
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
          list :+ AvatarConverter.BuildDetailedEquipment(index, equip),
          index + 1
        )
      }
      else {
        recursiveMakeHolsters(iter, list, index + 1)
      }
    }
  }
}
