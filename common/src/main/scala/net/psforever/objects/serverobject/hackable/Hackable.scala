package net.psforever.objects.serverobject.hackable

import net.psforever.objects.Player
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3

trait Hackable {
  /** An entry that maintains a reference to the `Player`, and the player's GUID and location when the message was received. */
  private var hackedBy : Option[(Player, PlanetSideGUID, Vector3)] = None

  def HackedBy : Option[(Player, PlanetSideGUID, Vector3)] = hackedBy

  def HackedBy_=(agent : Player) : Option[(Player, PlanetSideGUID, Vector3)] = HackedBy_=(Some(agent))

  /**
    * Set the hack state of this object by recording important information about the player that caused it.
    * Set the hack state if there is no current hack state.
    * Override the hack state with a new hack state if the new user has different faction affiliation.
    * @param agent a `Player`, or no player
    * @return the player hack entry
    */
  def HackedBy_=(agent : Option[Player]) : Option[(Player, PlanetSideGUID, Vector3)] = {
    hackedBy match {
      case None =>
        //set the hack state if there is no current hack state
        if(agent.isDefined) {
          hackedBy = Some(agent.get, agent.get.GUID, agent.get.Position)
        }
      case Some(_) =>
        //clear the hack state
        if(agent.isEmpty) {
          hackedBy = None
        }
        //override the hack state with a new hack state if the new user has different faction affiliation
        else if(agent.get.Faction != hackedBy.get._1.Faction) {
          hackedBy = Some(agent.get, agent.get.GUID, agent.get.Position)
        }
    }
    HackedBy
  }
}
