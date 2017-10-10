// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject

import akka.actor.ActorRef
import net.psforever.objects.PlanetSideGameObject

abstract class PlanetSideServerObject extends PlanetSideGameObject {
  private var actor = ActorRef.noSender

  def Actor : ActorRef = actor

  def Actor_=(control : ActorRef) : ActorRef =  {
    if(actor == ActorRef.noSender) {
      actor = control
    }
    actor
  }
}
