// Copyright (c) 2017 PSForever
package net.psforever.objects.serverobject.builders

import akka.actor.ActorContext
import net.psforever.objects.PlanetSideGameObject
import net.psforever.objects.guid.NumberPoolHub

/**
  * Wrapper `Trait` designed to be extended to implement custom object instantiation logic at the `ZoneMap` level.
  * @see `Zone.Init`
  */
trait ServerObjectBuilder[A <: PlanetSideGameObject] {
  /**
    * Instantiate and configure the given server object
    * (at a later time compared to the construction of the builder class).<br>
    * <br>
    * Externally, it expects a `context` to properly integrate within an `ActorSystem`
    * and is provided with a source for globally unique identifiers to integrate into the `Zone`.
    * Neither is required of the `return` type, however.
    * @param context a context to allow the object to properly set up `ActorSystem` functionality;
    *                defaults to `null`
    * @param guid the local globally unique identifier system to complete the process of object introduction;
    *             defaults to `null`
    * @return the object that was created and integrated into the `Zone`
    */
  def Build(implicit context : ActorContext = null, guid : NumberPoolHub = null) : A
}
