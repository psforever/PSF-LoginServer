// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject

import akka.actor.ActorRef
import net.psforever.objects.{Default, PlanetSideGameObject}
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.zones.ZoneAware

/**
  * An object layered on top of the standard game object class that maintains an internal `ActorRef`.
  * A measure of synchronization can be managed using this `Actor` as a "controlling agent."
  */
abstract class PlanetSideServerObject extends PlanetSideGameObject with FactionAffinity with ZoneAware {
  private var actor: ActorRef              = ActorRef.noSender
  private var getActorFunc: () => ActorRef = PlanetSideServerObject.getDefaultActor

  /**
    * Retrieve a reference to the internal `Actor`.
    * @return the internal `ActorRef`
    */
  def Actor: ActorRef = getActorFunc()

  /**
    * Assign an `Actor` to act for this server object.
    * This reference is only set once, that is, as long as the internal `ActorRef` directs to `ActorRef.noSender` (`null`).
    * @param control the `Actor` whose functionality will govern this server object
    * @return the current internal `ActorRef`
    */
  def Actor_=(control: ActorRef): ActorRef = {
    if (control == Default.Actor) {
      ResetControl()
    } else {
      actor = control
      getActorFunc = PlanetSideServerObject.doGetLocalActor(this)
      actor
    }
  }

  def ResetControl(): ActorRef = {
    getActorFunc = PlanetSideServerObject.getDefaultActor
    val out = actor
    actor = ActorRef.noSender
    out
  }
}

object PlanetSideServerObject {

  /**
    * Before the internal control agency of a respective server object is ever set,
    * a default control agency will be produced.
    * @return the value pointed to by `Default.Actor`
    */
  private def getDefaultActor(): ActorRef = { Default.Actor }

  /**
    * Allow retrieving a reference to the internal `Actor`.
    * @see o the server object
    * @return the internal `ActorRef`
    */
  private def doGetLocalActor(o: PlanetSideServerObject)(): ActorRef = o.actor

  /**
    * `Actor` entities require unique names over the course of the lifetime of the `ActorSystem` object.
    * To produce this name, a composition of three strings separated by underscores is assembled.<br>
    * - the entity's object name;
    * uniqueness is very low, less than 50, but helps to identify the object<br>
    * - the entity's globally unique identifier number;
    * its uniqueness is much greater but still falls within less than 66535 possible integers;
    * useful for locating that entity;
    * an `Exception` can be thrown if this field was never set,
    * but that is a condition for which it is worth throwing the `Exception`<br>
    * - the current POSIX time in milliseconds;
    * results will remain reasonably unique;
    * useful for taking a rough estimation of how long the entity has existed
    * @throws `NoGUIDException` if the entity has never been registered to a unique identifier system
    * @param obj the entity for whom the `Actor` object will be created
    * @return the unique name
    */
  def UniqueActorName(obj: PlanetSideGameObject): String = {
    s"${obj.Definition.Name}_${obj.GUID.guid}_${System.currentTimeMillis}"
  }
}
