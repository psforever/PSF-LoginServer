// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject

import akka.actor.ActorRef
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.serverobject.affinity.FactionAffinity
import net.psforever.objects.zones.ZoneAware

/**
  * An object layered on top of the standard game object class that maintains an internal `ActorRef`.
  * A measure of synchronization can be managed using this `Actor`.
  */
abstract class PlanetSideServerObject extends PlanetSideGameObject
  with FactionAffinity
  with ZoneAware {
  private var actor = ActorRef.noSender

  /**
    * Retrieve a reference to the internal `Actor`.
    * @return the internal `ActorRef`
    */
  def Actor : ActorRef = actor

  /**
    * Assign an `Actor` to act for this server object.
    * This reference is only set once, that is, as long as the internal `ActorRef` directs to `Actor.noSender` (`null`).
    * @param control the `Actor` whose functionality will govern this server object
    * @return the current internal `ActorRef`
    */
  def Actor_=(control : ActorRef) : ActorRef =  {
    if(actor == ActorRef.noSender || control == ActorRef.noSender) {
      actor = control
    }
    actor
  }
}
