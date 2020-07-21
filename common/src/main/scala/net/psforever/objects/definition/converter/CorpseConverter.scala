// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Player
import net.psforever.packet.game.objectcreate._
import net.psforever.types.{PlanetSideGUID, _}

import scala.util.{Failure, Success, Try}

class CorpseConverter extends AvatarConverter {
  override def ConstructorData(obj: Player): Try[PlayerData] =
    Failure(new Exception("CorpseConverter should not be used to generate CharacterData"))

  override def DetailedConstructorData(obj: Player): Try[DetailedPlayerData] = {
    Success(
      DetailedPlayerData.apply(
        PlacementData(obj.Position, Vector3(0, 0, obj.Orientation.z)),
        MakeAppearanceData(obj),
        MakeDetailedCharacterData(obj),
        InventoryData(),
        DrawnSlot.None
      )
    )
  }

  /**
    * Compose some data from a `Player` into a representation common to both `CharacterData` and `DetailedCharacterData`.
    * @param obj the `Player` game object
    * @return the resulting `CharacterAppearanceData`
    */
  private def MakeAppearanceData(obj: Player): Int => CharacterAppearanceData = {
    val aa: Int => CharacterAppearanceA = CharacterAppearanceA(
      BasicCharacterData(obj.Name, obj.Faction, obj.Sex, 0, CharacterVoice.Mute),
      CommonFieldData(
        obj.Faction,
        bops = false,
        alternate = false,
        false,
        None,
        false,
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
      false,
      backpack = false,
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

  private def MakeDetailedCharacterData(obj: Player): Option[Int] => DetailedCharacterData = {
    val maxOpt: Option[Long] = if (obj.ExoSuit == ExoSuitType.MAX) { Some(0L) }
    else { None }
    val ba: DetailedCharacterA = DetailedCharacterA(
      bep = 0L,
      cep = 0L,
      0L,
      0L,
      0L,
      0,
      0,
      false,
      0,
      0L,
      0,
      0,
      maxOpt,
      0,
      0,
      0L,
      List(0, 0, 0, 0, 0, 0),
      certs = List.empty[CertificationType.Value]
    )
    val bb: (Long, Option[Int]) => DetailedCharacterB = DetailedCharacterB(
      None,
      implants = List.empty[ImplantEntry],
      Nil,
      Nil,
      firstTimeEvents = List.empty[String],
      tutorials = List.empty[String],
      0L,
      0L,
      0L,
      0L,
      0L,
      Some(DCDExtra2(0, 0)),
      Nil,
      Nil,
      false,
      cosmetics = None
    )
    pad_length: Option[Int] => DetailedCharacterData(ba, bb(0, pad_length))(pad_length)
  }
}

object CorpseConverter {
  val converter = new CorpseConverter
}
