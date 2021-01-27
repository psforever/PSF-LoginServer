package net.psforever.objects.serverobject.llu

import net.psforever.objects.serverobject.structures.{Amenity, AmenityOwner, Building}
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.types.{PlanetSideEmpire, Vector3}

/**
  * This object represents the LLU that gets spawned at a LLU socket when a LLU control console is hacked
  */
class CaptureFlag(tDef: CaptureFlagDefinition) extends Amenity {
  def Definition : CaptureFlagDefinition = tDef

  private var target: Building = Building.NoBuilding
  private var faction: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var carrier: Option[Player] = None

  def Target: Building = target
  def Target_=(new_target: Building): Building = {
    target = new_target
    target
  }

  // Since a LLU belongs to a base, but needs to be picked up by the enemy faction we need to be able to override the faction that owns the LLU to the hacker faction
  override def Faction: PlanetSideEmpire.Value = faction
  override def Faction_=(new_faction: PlanetSideEmpire.Value): PlanetSideEmpire.Value = {
    faction = new_faction
    faction
  }

  // When the flag is carried by a player, the position returned should be that of the carrier not the flag
  override def Position: Vector3 = if (Carrier.nonEmpty) {
    carrier.get.Position
  } else {
    Entity.Position
  }

  def Carrier: Option[Player] = carrier
  def Carrier_=(new_carrier: Option[Player]) : Option[Player] = {
    carrier = new_carrier
    carrier
  }
}

object CaptureFlag {
  def apply(tDef: CaptureFlagDefinition): CaptureFlag = {
    new CaptureFlag(tDef)
  }

  def Constructor(pos: Vector3, ori: Vector3, target: Building, owner: AmenityOwner, faction: PlanetSideEmpire.Value) : CaptureFlag = {
    val obj = CaptureFlag(GlobalDefinitions.capture_flag)
    obj.Position = pos
    obj.Orientation = ori
    obj.Target = target
    obj.Owner = owner
    obj.Faction = faction

    obj
  }
}