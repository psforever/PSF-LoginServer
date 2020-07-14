// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.actor

import akka.actor.ActorRef
import net.psforever.objects.entity.IdentifiableEntity

/**
  * A message for accepting object-number registration requests.<br>
  * <br>
  * The callback is actually an `ActorRef` to which a `RegisterSuccess` message or a `RegisterFailure` message is sent.
  * This is as opposed to what a "callback" is normally - a function.
  * @param obj the mandatory object
  * @param name the optional name of the number pool to which this object is registered
  * @param number the optional number pre-selected for registering this object
  * @param callback the optional custom callback for the messages from the success or failure conditions
  */
final case class Register(
    obj: IdentifiableEntity,
    name: Option[String],
    number: Option[Int],
    callback: Option[ActorRef]
)

object Register {

  /**
    * Overloaded constructor, accepting just the object.
    * @param obj the object to be registered
    * @return a `Register` object
    */
  def apply(obj: IdentifiableEntity): Register = {
    new Register(obj, None, None, None)
  }

  /**
    * Overloaded constructor, accepting the object and a callback.
    * @param obj the object to be registered
    * @param callback the custom callback for the messages from the success or failure conditions
    * @return a `Register` object
    */
  def apply(obj: IdentifiableEntity, callback: ActorRef): Register = {
    new Register(obj, None, None, Some(callback))
  }

  /**
    * Overloaded constructor, accepting an object and a pre-selected number.
    * @param obj the object to be registered
    * @param number the pre-selected number
    * @return a `Register` object
    */
  def apply(obj: IdentifiableEntity, number: Int): Register = {
    new Register(obj, None, Some(number), None)
  }

  /**
    * Overloaded constructor, accepting an object, a pre-selected number, and a callback.
    * @param obj the object to be registered
    * @param number the pre-selected number
    * @param callback the custom callback for the messages from the success or failure conditions
    * @return a `Register` object
    */
  def apply(obj: IdentifiableEntity, number: Int, callback: ActorRef): Register = {
    new Register(obj, None, Some(number), Some(callback))
  }

  /**
    * Overloaded constructor, accepting an object and a number pool.
    * @param obj the object to be registered
    * @param name the number pool name
    * @return a `Register` object
    */
  def apply(obj: IdentifiableEntity, name: String): Register = {
    new Register(obj, Some(name), None, None)
  }

  /**
    * Overloaded constructor, accepting an object, a number pool, and a callback.
    * @param obj the object to be registered
    * @param name the number pool name
    * @param callback the custom callback for the messages from the success or failure conditions
    * @return a `Register` object
    */
  def apply(obj: IdentifiableEntity, name: String, callback: ActorRef): Register = {
    new Register(obj, Some(name), None, Some(callback))
  }
}
