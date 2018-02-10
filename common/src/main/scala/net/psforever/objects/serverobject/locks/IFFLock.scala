// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.locks

import net.psforever.objects.Player
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.PlanetSideGUID
import net.psforever.types.Vector3

/**
  * A structure-owned server object that is a "door lock."<br>
  * <br>
  * The "door lock" exerts an "identify friend or foe" field that detects the faction affiliation of a target player.
  * It also indirectly inherits faction affiliation from the structure to which it is connected
  * or it can be "hacked" whereupon the person exploiting it leaves their "faction" as the aforementioned affiliated faction.
  * The `IFFLock` is ideally associated with a server map object - a `Door` - to which it acts as a gatekeeper.
  * @param idef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
  */
class IFFLock(private val idef : IFFLockDefinition) extends Amenity {
  /**
    * An entry that maintains a reference to the `Player`, and the player's GUID and location when the message was received.
    */
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

  def Definition : IFFLockDefinition = idef
}

object IFFLock {
  /**
    * Overloaded constructor.
    * @param idef the `ObjectDefinition` that constructs this object and maintains some of its immutable fields
    */
  def apply(idef : IFFLockDefinition) : IFFLock = {
    new IFFLock(idef)
  }

  import akka.actor.ActorContext
  /**
    * Instantiate an configure a `IFFLock` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality
    * @return the `IFFLock` object
    */
  def Constructor(id : Int, context : ActorContext) : IFFLock = {
    import akka.actor.Props
    import net.psforever.objects.GlobalDefinitions

    val obj = IFFLock(GlobalDefinitions.lock_external)
    obj.Actor = context.actorOf(Props(classOf[IFFLockControl], obj), s"${GlobalDefinitions.lock_external.Name}_$id")
    obj
  }
}
