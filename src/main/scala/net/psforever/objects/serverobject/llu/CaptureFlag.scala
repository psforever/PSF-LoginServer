// Copyright (c) 2021 PSForever
package net.psforever.objects.serverobject.llu

import net.psforever.objects.serverobject.structures.{Amenity, AmenityOwner, Building}
import net.psforever.objects.{GlobalDefinitions, Player}
import net.psforever.types.{PlanetSideEmpire, Vector3}

/**
 * Represent a special entity that is carried by the player in certain circumstances.
 * The entity is not a piece of `Equipment` so it does not go into the holsters,
 * doe not into the player's inventory,
 * and is not carried in or manipulated by the player's hands.
 * The different game elements it simulates are:
 * a facility's lattice logic unit (LLU),
 * the cavern modules,
 * and the rabbit ball (special game mode).<br>
 * <br>
 * For the lattice logic unit, when a facility is set to generate an LLU upon hack,
 * and an adjacent facility on the lattice provides an accommodating faction connection,
 * the unit gets spawned at the LLU socket within the hacked facility.
 * The LLU socket actually doesn't do anything but keep track of the spawned flag and provide a location.
 * It associates with the faction of the hacker and, carried by other players of the same faction only,
 * must be brought to the control console of a designated facility that is owned by the faction of the hacking empire.
 * If the hack is cancelled through a resecure, the LLU despawns.
 * If the facility is counter-hacked, the active LLU despawns and a new LLU is spawned in the socket.
 * Other empires can not interact with the LLU while it is dropped on the ground and
 * vehicles will be warned and then deconstructed if they linges too long near a dropped LLU.
 * The LLU can not be submerged in water or it will despawn and the hack will cancel.
 */
class CaptureFlag(private val tDef: CaptureFlagDefinition) extends Amenity {
  def Definition : CaptureFlagDefinition = tDef

  private var target: Building = Building.NoBuilding
  private var owningFaction: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var faction: PlanetSideEmpire.Value = PlanetSideEmpire.NEUTRAL
  private var carrier: Option[Player] = None

  override def Owner_=(obj: AmenityOwner): AmenityOwner = {
    owningFaction = obj.Faction
    super.Owner_=(obj)
  }

  def OwningFaction: PlanetSideEmpire.Value = owningFaction

  def Target: Building = target
  def Target_=(newTarget: Building): Building = {
    target = newTarget
    target
  }

  /**
   * Since a LLU belongs to a base,
   * but needs to be picked up by the enemy faction,
   * override the faction that owns the LLU to display the hacker faction.
   */
  override def Faction: PlanetSideEmpire.Value = faction
  override def Faction_=(newFaction: PlanetSideEmpire.Value): PlanetSideEmpire.Value = {
    faction = newFaction
    faction
  }

  /**
   * When the flag is carried by a player, the position returned should be that of the carrier not the flag.
   * @return the position of the carrier, if there is a player carrying the flag, or the flag itself
   */
  override def Position: Vector3 = if (Carrier.nonEmpty) {
    carrier.get.Position
  } else {
    super.Position
  }

  def Carrier: Option[Player] = carrier
  def Carrier_=(newCarrier: Option[Player]) : Option[Player] = {
    carrier = newCarrier
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
