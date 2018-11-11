package net.psforever.objects.serverobject.hackable

import net.psforever.objects.Player
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.packet.game.{PlanetSideGUID, TriggeredSound}
import net.psforever.types.Vector3

trait Hackable extends FactionAffinity {
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
        //clear the hack state if no agent is provided or the agent's faction matches the object faction
        if(agent.isEmpty || agent.get.Faction == this.Faction) {
          hackedBy = None
        }
        //override the hack state with a new hack state if the new user has different faction affiliation
        else if(agent.get.Faction != hackedBy.get._1.Faction) {
          hackedBy = Some(agent.get, agent.get.GUID, agent.get.Position)
        }
    }
    HackedBy
  }

  /** The sound made when the object is hacked */
  private var hackSound : TriggeredSound.Value = TriggeredSound.HackDoor
  def HackSound : TriggeredSound.Value = hackSound
  def HackSound_=(sound : TriggeredSound.Value) : TriggeredSound.Value = {
    hackSound = sound
    hackSound
  }

  /** The duration in seconds a hack lasts for, based on the hacker's certification level */
  private var hackEffectDuration = Array(0, 0, 0 , 0)
  def HackEffectDuration: Array[Int] = hackEffectDuration
  def HackEffectDuration_=(arr: Array[Int]) : Array[Int] = {
    hackEffectDuration = arr
    arr
  }

  /** How long it takes to hack the object in seconds, based on the hacker's certification level */
  private var hackDuration = Array(0, 0, 0, 0)
  def HackDuration: Array[Int] = hackDuration
  def HackDuration_=(arr: Array[Int]) : Array[Int] = {
    hackDuration = arr
    arr
  }
}
