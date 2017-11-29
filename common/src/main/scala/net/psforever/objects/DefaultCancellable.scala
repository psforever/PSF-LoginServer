// Copyright (c) 2017 PSForever
package net.psforever.objects

/**
  * Used to initialize the value of a re-usable `Cancellable` object.
  * By convention, it always acts like it has been cancelled before and can be cancelled.
  * Should be replaced with pertinent `Cancellable` logic through the initialization of an executor.
  */
object DefaultCancellable {
  import akka.actor.Cancellable

  protected class InternalCancellable extends Cancellable {
    override def cancel : Boolean = true
    override def isCancelled : Boolean = true
  }

  final val obj : Cancellable = new InternalCancellable
}
