// Copyright (c) 2017 PSForever
package net.psforever.objects

import net.psforever.types.{PlanetSideGUID, ValidPlanetSideGUID}

object Default {
  //'g'lobal 'u'nique 'id'entifier
  final val GUID0: PlanetSideGUID = ValidPlanetSideGUID(0)

  //cancellable
  import akka.actor.Cancellable
  protected class InternalCancellable extends Cancellable {
    override def cancel(): Boolean = true

    override def isCancelled: Boolean = true
  }
  private val cancellable: Cancellable = new InternalCancellable

  /**
    * Used to initialize the value of a re-usable `Cancellable` object.
    * By convention, it always acts like it has been cancelled before and can be cancelled.
    * Should be replaced with pertinent `Cancellable` logic through the initialization of an executor.
    */
  final def Cancellable: Cancellable = cancellable

  //actor
  import akka.actor.{Actor => AkkaActor, ActorRef, ActorSystem, DeadLetter, Props, typed => Typed}

  /**
    * An actor designed to wrap around `deadLetters` and redirect all normal messages to it.
    * This measure is more to "protect" `deadLetters` than anything else.
    * Even if it is stopped, it still fulfills exactly the same purpose!
    * The original target to which the actor is assigned will not be implicitly accredited.
    */
  private class DefaultActor extends AkkaActor {
    def receive: Receive = {
      case msg => context.system.deadLetters ! DeadLetter(msg, sender(), self)
    }
  }
  @volatile private var defaultRef: ActorRef = ActorRef.noSender
  @volatile private var defaultRefSystem: ActorSystem = _
  private val defaultActors = scala.collection.mutable.Map.empty[ActorSystem, ActorRef]

  /**
    * Instigate the default actor.
    * A new default actor is created whenever the requested `ActorSystem` is not the one
    * that owns the cached reference. Tests create and shut down one `ActorSystem` per
    * suite, so without this check later suites would keep sending to a dead actor.
    * Each system keeps its own actor, so rebinding to a system that already has one
    * does not try to create a second actor with the same name.
    * @param sys the actor universe under which this default actor will exist
    * @return the new default actor
    */
  def apply(sys: ActorSystem): ActorRef = synchronized {
    if (defaultRef == ActorRef.noSender || (defaultRefSystem ne sys)) {
      defaultRef = defaultActors.getOrElseUpdate(
        sys, {
          val ref = sys.actorOf(Props[DefaultActor](), name = "system-default-actor")
          sys.registerOnTermination {
            Default.synchronized {
              defaultActors.remove(sys)
            }
          }
          ref
        }
      )
      defaultRefSystem = sys
    }
    defaultRef
  }

  final def Actor: ActorRef = defaultRef

  object typed {
    import akka.actor.typed.scaladsl.adapter._
    private def defaultTypedRef: Typed.ActorRef[Any] = defaultRef.toTyped[Any]

    /**
     * A copy of the default actor
     * but promoted into a typed actor that accepts any kind of message.
     * Resolved on each access so it follows `Default.apply` re-binding.
     */
    final def Actor: Typed.ActorRef[Any] = defaultTypedRef
  }
}
