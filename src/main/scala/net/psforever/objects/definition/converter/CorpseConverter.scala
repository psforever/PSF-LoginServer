// Copyright (c) 2017 PSForever
package net.psforever.objects.definition.converter

import net.psforever.objects.Player
import net.psforever.objects.avatar.Certification
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
      BasicCharacterData(obj.Name, obj.Faction, CharacterSex.Male, 0, CharacterVoice.Mute),
      CommonFieldData(
        obj.Faction,
        bops = false,
        alternate = true,
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
      backpack = true,
      unk2 = false,
      unk3 = false,
      unk4 = false,
      facingPitch = 0,
      facingYawUpper = 0,
      lfs = false,
      GrenadeState.None,
      is_cloaking = false,
      unk5 = false,
      unk6 = false,
      charging_pose = false,
      unk7 = false,
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
      unk4 = false,
      0,
      0L,
      0,
      0,
      maxOpt,
      0,
      0,
      0L,
      List(0, 0, 0, 0, 0, 0),
      certs = List.empty[Certification]
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
      Some(ImprintingProgress(0, 0)),
      Nil,
      Nil,
      unkC = false,
      cosmetics = None
    )
    pad_length: Option[Int] => DetailedCharacterData(ba, bb(0, pad_length))(pad_length)
  }
}

object CorpseConverter {
  val converter = new CorpseConverter
}
