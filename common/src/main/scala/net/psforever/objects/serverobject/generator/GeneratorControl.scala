// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.generator

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

/**
  * An `Actor` that handles messages being dispatched to a specific `Generator`.
  * @param gen the `Generator` object being governed
  */
class GeneratorControl(gen : Generator) extends Actor
  with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = gen

  def receive : Receive = checkBehavior.orElse {
    case _ => ;
  }
}
