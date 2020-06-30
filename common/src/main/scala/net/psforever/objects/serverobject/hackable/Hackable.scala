package net.psforever.objects.serverobject.hackable

import net.psforever.objects.Player
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.serverobject.hackable.Hackable.HackInfo
import net.psforever.packet.game.TriggeredSound
import net.psforever.types.{PlanetSideEmpire, PlanetSideGUID, Vector3}

trait Hackable {
  _: FactionAffinity =>

  /** inportant information regarding the hack and how it was started */
  private var hackedBy: Option[HackInfo]          = None
  def HackedBy: Option[HackInfo]                  = hackedBy
  def HackedBy_=(agent: Player): Option[HackInfo] = HackedBy_=(Some(agent))

  /**
    * Set the hack state of this object by recording important information about the player who caused it.
    * Set the hack state if there is no current hack state.
    * Override the hack state with a new hack state if the new user has different faction affiliation.
    * @param agent a `Player`, or no player
    * @return the player hack entry
    */
  def HackedBy_=(agent: Option[Player]): Option[HackInfo] = {
    (hackedBy, agent) match {
      case (None, Some(actor)) =>
        hackedBy = Some(HackInfo(actor.Name, actor.GUID, actor.Faction, actor.Position, System.nanoTime, 0L))
      case (Some(info), Some(actor)) =>
        if (actor.Faction == this.Faction) {
          //hack cleared
          hackedBy = None
        } else if (actor.Faction != info.hackerFaction) {
          //override the hack state with a new hack state if the new user has different faction affiliation
          hackedBy = Some(HackInfo(actor.Name, actor.GUID, actor.Faction, actor.Position, System.nanoTime, 0L))
        }
      case (_, None) =>
        hackedBy = None
    }
    HackedBy
  }

  def HackedBy_=(hackInfo: HackInfo): Option[HackInfo] = {
    hackedBy = Some(hackInfo)
    HackedBy
  }

  /** The sound made when the object is hacked */
  private var hackSound: TriggeredSound.Value = TriggeredSound.HackDoor
  def HackSound: TriggeredSound.Value         = hackSound
  def HackSound_=(sound: TriggeredSound.Value): TriggeredSound.Value = {
    hackSound = sound
    hackSound
  }

  /** The duration in seconds a hack lasts for, based on the hacker's certification level */
  private var hackEffectDuration     = Array(0, 0, 0, 0)
  def HackEffectDuration: Array[Int] = hackEffectDuration
  def HackEffectDuration_=(arr: Array[Int]): Array[Int] = {
    hackEffectDuration = arr
    arr
  }

  /** How long it takes to hack the object in seconds, based on the hacker's certification level */
  private var hackDuration     = Array(0, 0, 0, 0)
  def HackDuration: Array[Int] = hackDuration
  def HackDuration_=(arr: Array[Int]): Array[Int] = {
    hackDuration = arr
    arr
  }

//  private var hackable : Option[Boolean] = None
//  def Hackable : Boolean = hackable.getOrElse(Definition.Hackable)
//
//  def Hackable_=(state : Boolean) : Boolean = Hackable_=(Some(state))
//
//  def Hackable_=(state : Option[Boolean]) : Boolean = {
//    hackable = state
//    Hackable
//  }
//
//  def Definition : HackableDefinition
}

object Hackable {
  final case class HackInfo(
      hackerName: String,
      hackerGUID: PlanetSideGUID,
      hackerFaction: PlanetSideEmpire.Value,
      hackerPos: Vector3,
      hackStartTime: Long,
      hackDuration: Long
  ) {
    def Duration(time: Long): HackInfo =
      HackInfo(hackerName, hackerGUID, hackerFaction, hackerPos, hackStartTime, time)
  }
}
