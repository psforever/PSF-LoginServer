// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.mblocker

import akka.actor.{ActorContext, Props}
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.serverobject.hackable.Hackable
import net.psforever.objects.serverobject.structures.Amenity
import net.psforever.packet.game.TriggeredSound

class Locker extends Amenity with Hackable {
  def Definition : LockerDefinition = GlobalDefinitions.mb_locker
  HackSound = TriggeredSound.HackTerminal
  HackEffectDuration = Array(0, 30, 60, 90)
  HackDuration = Array(0, 10, 5, 3)
}

object Locker {
  /**
    * Overloaded constructor.
    * @return the `Locker` object
    */
  def apply() : Locker = {
    new Locker()
  }

  /**
    * Instantiate an configure a `Locker` object
    * @param id the unique id that will be assigned to this entity
    * @param context a context to allow the object to properly set up `ActorSystem` functionality;
    *                not necessary for this object, but required by signature
    * @return the `Locker` object
    */
  def Constructor(id : Int, context : ActorContext) : Locker = {
    val obj = Locker()
    obj.Actor = context.actorOf(Props(classOf[LockerControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }

  import net.psforever.types.Vector3
  def Constructor(pos: Vector3)(id : Int, context : ActorContext) : Locker = {
    val obj = Locker()
    obj.Position = pos
    obj.Actor = context.actorOf(Props(classOf[LockerControl], obj), s"${obj.Definition.Name}_$id")
    obj
  }
}
