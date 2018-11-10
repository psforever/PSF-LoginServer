// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.{EquipmentSlot, Player}
import net.psforever.objects.equipment.Equipment
import net.psforever.packet.game.objectcreate._
import net.psforever.types._

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

class CorpseConverter extends AvatarConverter {
  override def ConstructorData(obj : Player) : Try[PlayerData] =
    Failure(new Exception("CorpseConverter should not be used to generate CharacterData"))

  override def DetailedConstructorData(obj : Player) : Try[DetailedPlayerData] = {
    Success(
      DetailedPlayerData.apply(
        PlacementData(obj.Position, Vector3(0,0, obj.Orientation.z)),
        MakeAppearanceData(obj),
        MakeDetailedCharacterData(obj),
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
  private def MakeAppearanceData(obj : Player) : (Int)=>CharacterAppearanceData = {
    val aa : Int=>CharacterAppearanceA = CharacterAppearanceA(
      BasicCharacterData(obj.Name, obj.Faction, CharacterGender.Male, 0, CharacterVoice.Mute),
      black_ops = false,
      altModel = true,
      false,
      None,
      jammered = false,
      obj.ExoSuit,
      None,
      0,
      0,
      0L,
      0,
      0,
      0,
      0
    )
    val ab : (Boolean,Int)=>CharacterAppearanceB = CharacterAppearanceB(
      0L,
      outfit_name = "",
      outfit_logo = 0,
      false,
      backpack = true,
      false,
      false,
      false,
      facingPitch = 0,
      facingYawUpper = 0,
      lfs = false,
      GrenadeState.None,
      is_cloaking = false,
      false,
      false,
      charging_pose = false,
      false,
      on_zipline = None
    )
    CharacterAppearanceData(aa, ab, RibbonBars())
  }

  private def MakeDetailedCharacterData(obj : Player) : (Option[Int]=>DetailedCharacterData) = {
    val maxOpt : Option[Long] = if(obj.ExoSuit == ExoSuitType.MAX) { Some(0L) } else { None }
    val ba : DetailedCharacterA = DetailedCharacterA(
      bep = 0L,
      cep = 0L,
      0L, 0L, 0L,
      0, 0,
      false,
      0,
      0L,
      0, 0,
      maxOpt,
      0, 0, 0L,
      List(0, 0, 0, 0, 0, 0),
      certs = List.empty[CertificationType.Value]
    )
    val bb : (Long, Option[Int])=>DetailedCharacterB = DetailedCharacterB(
      None,
      implants = List.empty[ImplantEntry],
      Nil, Nil,
      firstTimeEvents = List.empty[String],
      tutorials = List.empty[String],
      0L, 0L, 0L, 0L, 0L,
      Some(DCDExtra2(0, 0)),
      Nil, Nil, false,
      cosmetics = None
    )
    (pad_length : Option[Int]) => DetailedCharacterData(ba, bb(0, pad_length))(pad_length)
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
      .map(item => {
          val equip : Equipment = item.obj
          BuildEquipment(item.start, equip)
      })
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
