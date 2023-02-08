// Copyright (c) 2017 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.definition.{AvatarDefinition, ExoSuitDefinition}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.{GlobalDefinitions, PlanetSideGameObject, Player}
import net.psforever.types.{CharacterSex, ExoSuitType, PlanetSideEmpire, Vector3}

final case class UniquePlayer(
                               charId: Long,
                               name: String,
                               sex: CharacterSex,
                               faction: PlanetSideEmpire.Value
                             ) extends SourceUniqueness

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
                               kills: Seq[Any],
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
      p.avatar.bep,
      kills = Nil,
      UniquePlayer(p.CharId, p.Name, p.Sex, faction)
    )
  }

  def apply(name: String, faction: PlanetSideEmpire.Value, position: Vector3): PlayerSource = {
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
      kills = Nil,
      UniquePlayer(0L, name, CharacterSex.Male, faction)
    )
  }

  def mountableAndSeat(player: Player): Option[(SourceEntry, Int)] = {
    player.Zone.GUID(player.VehicleSeated) match {
      case Some(thing: PlanetSideGameObject with Mountable with FactionAffinity) =>
        Some((SourceEntry(thing), thing.PassengerInSeat(player).get))
      case _ =>
        None
    }
  }

  /**
   * Nobody is my name: Nobody they call me –
   * my mother and my father and all my other companions”
   * Thus I spoke but he immediately replied to me with a ruthless spirit:
   * “I shall kill Nobody last of all, after his companions,
   * the others first: this will be my guest-gift to you.”
   */
  final val Nobody = PlayerSource("Nobody", PlanetSideEmpire.NEUTRAL, Vector3.Zero)
}
