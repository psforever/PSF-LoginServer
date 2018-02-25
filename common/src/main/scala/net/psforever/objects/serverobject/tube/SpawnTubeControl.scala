// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.tube

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `SpawnTube`.
  * @param tube the `SpawnTube` object being governed
  */
class SpawnTubeControl(tube : SpawnTube) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = tube

  def receive : Receive = checkBehavior.orElse { case _ =>; }

  override def toString : String = tube.Definition.Name
}