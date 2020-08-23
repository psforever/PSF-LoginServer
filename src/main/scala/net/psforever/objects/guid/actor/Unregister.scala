// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.actor

import akka.actor.ActorRef
import net.psforever.objects.entity.IdentifiableEntity

/**
  * A message for accepting object-number unregistration requests.
  * When given to a number pool (`NumberPoolAccessorActor`), that `Actor` assumes itself to have the object.
  * When given to a hub object (`NumberPoolHubActor`), it will attempt to determine which pool currently has the object.<br>
  * <br>
  * The callback is actually an `ActorRef` to which a `RegisterSuccess` message or a `RegisterFailure` message is sent.
  * This is as opposed to what a "callback" is normally - a function.
  * @param obj the mandatory object
  * @param callback the optional custom callback for the messages from the success or failure conditions
  */
final case class Unregister(obj: IdentifiableEntity, callback: Option[ActorRef] = None)

object Unregister {
  def apply(obj: IdentifiableEntity, callback: ActorRef): Unregister = {
    Unregister(obj, Some(callback))
  }
}
