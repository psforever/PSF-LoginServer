// Copyright (c) 2017 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.avatar.scoring.Life
import net.psforever.objects.definition.{AvatarDefinition, ExoSuitDefinition}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player, Vehicle}
import net.psforever.types.{CharacterSex, ExoSuitType, PlanetSideEmpire, Vector3}

final case class UniquePlayer(
                               charId: Long,
                               name: String,
                               sex: CharacterSex,
                               faction: PlanetSideEmpire.Value
                             ) extends SourceUniqueness

object UniquePlayer {
  def apply(obj: Player): UniquePlayer = {
    UniquePlayer(obj.CharId, obj.Name, obj.Sex, obj.Faction)
  }
}

final case class PlayerSource(
                               Definition: AvatarDefinition,
                               ExoSuit: ExoSuitType.Value,
                               seatedIn: Option[(SourceEntry, Int)],
                               health: Int,
                               armor: Int,
                               Position: Vector3,
                               Orientation: Vector3,
                               Velocity: Option[Vector3],
                               crouching: Boolean,
                               jumping: Boolean,
                               Modifiers: ResistanceProfile,
                               bep: Long,
                               progress: Life,
                               unique: UniquePlayer
                             ) extends SourceWithHealthEntry {
  override def Name: String = unique.name
  override def Faction: PlanetSideEmpire.Value = unique.faction
  override def CharId: Long = unique.charId

  def Seated: Boolean = seatedIn.nonEmpty
  def Health: Int = health
  def Armor: Int = armor
  def total: Int = health + armor
}

object PlayerSource {
  def apply(p: Player): PlayerSource = {
    val exosuit = p.ExoSuit
    val faction = p.Faction
    val seatedEntity = mountableAndSeat(p)
    val avatar = p.avatar
    PlayerSource(
      p.Definition,
      exosuit,
      seatedEntity,
      p.Health,
      p.Armor,
      p.Position,
      p.Orientation,
      p.Velocity,
      p.Crouching,
      p.Jumping,
      ExoSuitDefinition.Select(exosuit, faction),
      avatar.bep,
      progress = avatar.scorecard.CurrentLife,
      UniquePlayer(p.CharId, p.Name, p.Sex, faction)
    )
  }

  def apply(name: String, faction: PlanetSideEmpire.Value, position: Vector3): PlayerSource = {
    this(UniquePlayer(0L, name, CharacterSex.Male, faction), position)
  }

  def apply(unique: UniquePlayer, position: Vector3): PlayerSource = {
    new PlayerSource(
      GlobalDefinitions.avatar,
      ExoSuitType.Standard,
      seatedIn = None,
      health = 100,
      armor = 0,
      position,
      Orientation = Vector3.Zero,
      Velocity = None,
      crouching = false,
      jumping = false,
      GlobalDefinitions.Standard,
      bep = 0L,
      progress = tokenLife,
      unique
    )
  }

  def mountableAndSeat(player: Player): Option[(SourceEntry, Int)] = {
    player.Zone.GUID(player.VehicleSeated) match {
      case Some(vehicle: Vehicle) =>
        Some((
          SourceEntry(vehicle),
          vehicle.PassengerInSeat(player).orElse {
            vehicle.PublishGatingManifest().orElse(vehicle.PreviousGatingManifest()).flatMap { manifest =>
              val playerName = player.Name
              manifest.passengers.find { _.name == playerName }.collect { _.mount }
            }
          }.getOrElse(0)
        ))
      case Some(thing: PlanetSideGameObject with Mountable with FactionAffinity) =>
        Some((SourceEntry(thing), thing.PassengerInSeat(player).getOrElse(0)))
      case _ =>
        None
    }
  }

  /**
   * Produce a mostly normal player source entity
   * but the `seatedIn` field is just a shallow copy of the mountable information.
   * Said "shallow copy" will not reflect that the player is an occupant of the mountable entity
   * even if this function is entirely for the purpose of establishing that the player is an occupant of the mountable entity.<br>
   * Don't think too much about it.
   * @param player player
   * @param source a `SourceEntry` for the aforementioned mountable entity
   * @param seatNumber the attributed seating index in which the player is mounted in `source`
   * @return a `PlayerSource` entity
   */
  def inSeat(player: Player, source: SourceEntry, seatNumber: Int): PlayerSource = {
    val exosuit = player.ExoSuit
    val avatar = player.avatar
    PlayerSource(
      player.Definition,
      exosuit,
      Some((source, seatNumber)),
      player.Health,
      player.Armor,
      player.Position,
      player.Orientation,
      player.Velocity,
      player.Crouching,
      player.Jumping,
      ExoSuitDefinition.Select(exosuit, player.Faction),
      avatar.bep,
      progress = avatar.scorecard.CurrentLife,
      UniquePlayer(player)
    )
  }

  /**
   * Produce a copy of a normal player source entity
   * but the `seatedIn` field is overrode to point at the specified vehicle and seat number.<br>
   * Don't think too much about it.
   * @param player `SourceEntry` for a player
   * @param source `SourceEntry` for the aforementioned mountable entity
   * @param seatNumber the attributed seating index in which the player is mounted in `source`
   * @return a `PlayerSource` entity
   */
  def inSeat(player: PlayerSource, source: SourceEntry, seatNumber: Int): PlayerSource = {
    player.copy(seatedIn = Some((source, seatNumber)))
  }

  /**
   * "Nobody is my name: Nobody they call me –
   * my mother and my father and all my other companions”
   * Thus I spoke but he immediately replied to me with a ruthless spirit:
   * “I shall kill Nobody last of all, after his companions,
   * the others first: this will be my guest-gift to you.”
   */
  final val Nobody = PlayerSource("Nobody", PlanetSideEmpire.NEUTRAL, Vector3.Zero)

  /**
   * Used to dummy the statistics value for shallow player source entities.
   */
  private val tokenLife: Life = Life()
}
