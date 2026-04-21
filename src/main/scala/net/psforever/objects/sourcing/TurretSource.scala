// Copyright (c) 2023 PSForever
package net.psforever.objects.sourcing

import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.mount.Mountable
import net.psforever.objects.serverobject.turret.{FacilityTurret, WeaponTurret}
import net.psforever.objects.vital.VitalityDefinition
import net.psforever.objects.vital.resistance.ResistanceProfile
import net.psforever.objects.{PlanetSideGameObject, TurretDeployable}
import net.psforever.types.{PlanetSideEmpire, Vector3}

final case class TurretSource(
                               Definition: ObjectDefinition with VitalityDefinition,
                               Faction: PlanetSideEmpire.Value,
                               health: Int,
                               shields: Int,
                               Position: Vector3,
                               Orientation: Vector3,
                               occupants: List[SourceEntry],
                               unique: SourceUniqueness
                             ) extends SourceWithHealthEntry with SourceWithShieldsEntry with MountableEntry {
  def Name: String = SourceEntry.NameFormat(Definition.Descriptor)
  def Health: Int = health
  def Shields: Int = shields
  def Velocity: Option[Vector3] = None
  def Modifiers: ResistanceProfile = Definition.asInstanceOf[ResistanceProfile]

  def total: Int = health + shields
}

object TurretSource {
  def apply(obj: PlanetSideGameObject with WeaponTurret): TurretSource = {
    val position = obj.Position
    val identifer = obj match {
      case o: TurretDeployable =>
        UniqueDeployable(o)
      case o: FacilityTurret =>
        UniqueAmenity(o)
      case o =>
        throw new IllegalArgumentException(s"was given ${o.Actor.toString()} when only wanted to model turrets")
    }
    val turret = TurretSource(
      obj.Definition.asInstanceOf[ObjectDefinition with VitalityDefinition],
      obj.Faction,
      obj.Health,
      shields = 0, //TODO implement later
      position,
      obj.Orientation,
      Nil,
      identifer
    )
    turret.copy(occupants = obj match {
      case o: Mountable =>
        o.Seats
          .collect { case (num, seat) if seat.isOccupied => (num, seat.occupants.head) }
          .map { case (num, p) => PlayerSource.inSeat(p, turret, num) }.toList
      case _ =>
        Nil
    })
  }
}
