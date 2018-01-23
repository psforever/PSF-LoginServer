// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.mblocker

import akka.actor.ActorContext
import net.psforever.objects.GlobalDefinitions
import net.psforever.objects.definition.ObjectDefinition
import net.psforever.objects.serverobject.PlanetSideServerObject

class Locker extends PlanetSideServerObject {
  def Definition : ObjectDefinition = GlobalDefinitions.mb_locker
}

object Locker {
  /**
    * Overloaded constructor.
    * @return a `VehicleSpawnPad` object
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
    obj
  }
}
