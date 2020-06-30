// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.affinity

import net.psforever.types.PlanetSideEmpire

/**
  * Keep track of the allegiance of the object in terms of its association to a `PlanetSideEmpire` value.
  */
trait FactionAffinity {
  def Faction: PlanetSideEmpire.Value

  def Faction_=(fac: PlanetSideEmpire.Value): PlanetSideEmpire.Value = Faction
}

object FactionAffinity {

  /**
    * Message that makes the server object transmit IFF feedback.
    * @see AssertFactionAffinity
    */
  final case class ConfirmFactionAffinity()

  /**
    * Message that makes the server object change allegiance to the specified faction value.
    * Transmit IFF feedback when done.
    * @param faction the allegiance to which to change
    */
  final case class ConvertFactionAffinity(faction: PlanetSideEmpire.Value)

  /**
    * Message that responds to an IFF feedback request.
    * Transmit IFF feedback when done.
    * @see ConfirmFactionAffinity
    * @param obj the governed object
    * @param faction the allegiance to which the object belongs
    */
  final case class AssertFactionAffinity(obj: FactionAffinity, faction: PlanetSideEmpire.Value)
}
