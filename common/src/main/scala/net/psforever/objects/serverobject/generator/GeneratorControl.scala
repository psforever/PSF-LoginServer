// Copyright (c) 2020 PSForever
package net.psforever.objects.serverobject.generator

import akka.actor.Actor
import net.psforever.objects.serverobject.affinity.{FactionAffinity, FactionAffinityBehavior}

class GeneratorControl(gen : Generator) extends Actor with FactionAffinityBehavior.Check {
  def FactionObject : FactionAffinity = gen

  def receive : Receive = checkBehavior.orElse {
    case _ => ;
  }
}
