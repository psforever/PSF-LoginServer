// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.avatar.Certification
import net.psforever.objects.{Player, Tool}
import net.psforever.objects.equipment.EquipmentSlot
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideGUID, _}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
  * `CharacterSelectConverter` is a simplified `AvatarConverter`
  * that is tailored for appearance of the player character on the character selection screen.
  * Details that would not be apparent on that screen such as implants or certifications are ignored.
  */
class CharacterSelectConverter extends AvatarConverter {
  override def ConstructorData(obj: Player): Try[PlayerData] =
    Failure(new Exception("CharacterSelectConverter should not be used to generate CharacterData"))

  override def DetailedConstructorData(obj: Player): Try[DetailedPlayerData] = {
    Success(
      DetailedPlayerData.apply(
        PlacementData(0, 0, 0),
        MakeAppearanceData(obj),
        MakeDetailedCharacterData(obj),
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
  private def MakeAppearanceData(obj: Player): Int => CharacterAppearanceData = {
    val aa: Int => CharacterAppearanceA = CharacterAppearanceA(
      BasicCharacterData(obj.Name, obj.Faction, obj.Sex, obj.Head, CharacterVoice.Mute),
      CommonFieldData(
        obj.Faction,
        bops = false,
        alternate = false,
        v1 = false,
        None,
        jammered = false,
        None,
        v5 = None,
        PlanetSideGUID(0)
      ),
      obj.ExoSuit,
      0,
      0L,
      0,
      0,
      0,
      0
    )
    val ab: (Boolean, Int) => CharacterAppearanceB = CharacterAppearanceB(
      0L,
      outfit_name = "",
      outfit_logo = 0,
      unk1 = false,
      backpack = false,
      unk2 = false,
      unk3 = false,
      unk4 = false,
      facingPitch = 0,
      facingYawUpper = 0,
      lfs = false,
      GrenadeState.None,
      is_cloaking = obj.Cloaked,
      unk5 = false,
      unk6 = false,
      charging_pose = false,
      unk7 = false,
      on_zipline = None
    )
    CharacterAppearanceData(aa, ab, obj.avatar.ribbonBars)
  }

  private def MakeDetailedCharacterData(obj: Player): Option[Int] => DetailedCharacterData = {
    val maxOpt: Option[Long] = if (obj.ExoSuit == ExoSuitType.MAX) { Some(0L) }
    else { None }
    val ba: DetailedCharacterA = DetailedCharacterA(
      obj.avatar.bep,
      obj.avatar.cep,
      0L,
      0L,
      0L,
      1,
      1,
      unk4 = false,
      0,
      0L,
      1,
      1,
      maxOpt,
      0,
      0,
      0L,
      List(0, 0, 0, 0, 0, 0),
      certs = List.empty[Certification]
    )

    val bb: (Long, Option[Int]) => DetailedCharacterB = DetailedCharacterB(
      None,
      // necessary for correct stream length
      List.fill[ImplantEntry](obj.avatar.br.implantSlots)(ImplantEntry(ImplantType.None, None)),
      Nil,
      Nil,
      firstTimeEvents = List.empty[String],
      tutorials = List.empty[String],
      0L,
      0L,
      0L,
      0L,
      0L,
      Some(ImprintingProgress(0, 0)),
      Nil,
      Nil,
      unkC = false,
      obj.avatar.cosmetics
    )
    pad_length: Option[Int] => DetailedCharacterData(ba, bb(obj.avatar.bep, pad_length))(pad_length)
  }

  /**
    * Given some equipment holsters, convert the contents of those holsters into converted-decoded packet data.
    * @param iter an `Iterator` of `EquipmentSlot` objects that are a part of the player's holsters
    * @param list the current `List` of transformed data
    * @param index which holster is currently being explored
    * @see `AvatarConverter.recursiveMakeHolsters`
    * @return the `List` of inventory data created from the holsters
    */
  @tailrec private def recursiveMakeHolsters(
      iter: Iterator[EquipmentSlot],
      list: List[InternalSlot] = Nil,
      index: Int = 0
  ): List[InternalSlot] = {
    if (!iter.hasNext) {
      list
    } else {
      val slot: EquipmentSlot = iter.next()
      slot.Equipment match {
        case Some(equip: Tool) =>
          val jammed = equip.Jammed
          equip.Jammed = false
          val slot = AvatarConverter.BuildDetailedEquipment(index, equip)
          equip.Jammed = jammed
          recursiveMakeHolsters(
            iter,
            list :+ slot,
            index + 1
          )
        case Some(equip) =>
          recursiveMakeHolsters(
            iter,
            list :+ AvatarConverter.BuildDetailedEquipment(index, equip),
            index + 1
          )
        case _ =>
          recursiveMakeHolsters(iter, list, index + 1)
      }
//      if(slot.Equipment.isDefined) {
//
//        val equip : Equipment = slot.Equipment.get
//        recursiveMakeHolsters(
//          iter,
//          list :+ AvatarConverter.BuildDetailedEquipment(index, equip),
//          index + 1
//        )
//      }
//      else {
//        recursiveMakeHolsters(iter, list, index + 1)
//      }
    }
  }
}
