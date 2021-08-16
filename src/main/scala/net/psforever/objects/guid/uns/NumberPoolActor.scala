// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.uns

import akka.actor.Actor
import net.psforever.objects.guid.pool.NumberPool
import net.psforever.objects.guid.selector.{NumberSelector, SpecificSelector}

import scala.util.{Failure, Success, Try}

/**
  * An `Actor` that wraps around a `NumberPool` and regulates access to it.<br>
  * <br>
  * Wrapping around the pool like this forces a FIFO order to requests for numbers from the pool.
  * This synchronization only lasts as long as this `Actor` is the only one for the given pool.
  * In the distribution of globally unique identifiers, this is extremely important.
  * `NumberPool`s are used as the primary determination of whether a number is available at any given moment.
  * The categorization of the pool is also important, though for a contextually-sensitive reason.
  * @param pool the `NumberPool` being manipulated
  */
class NumberPoolActor(pool: NumberPool) extends Actor {
  private[this] val log = org.log4s.getLogger

  def receive: Receive = {
    case NumberPoolActor.GetAnyNumber() =>
      sender() ! (pool.Get() match {
        case Success(value) =>
          NumberPoolActor.GiveNumber(value)
        case Failure(ex) =>
          NumberPoolActor.NoNumber(ex)
      })

    case NumberPoolActor.GetSpecificNumber(number) =>
      sender() ! (NumberPoolActor.GetSpecificNumber(pool, number) match {
        case Success(value) =>
          NumberPoolActor.GiveNumber(value)
        case Failure(ex) => ;
          NumberPoolActor.NoNumber(ex)
      })

    case NumberPoolActor.ReturnNumber(number) =>
      val result = pool.Return(number)
      val ex: Option[Throwable] = if (!result) { Some(new Exception("number was not returned")) }
      else { None }
      sender() ! NumberPoolActor.ReturnNumberResult(number, ex)

    case msg =>
      log.warn(s"Received an unexpected message - ${msg.toString}")
  }
}

object NumberPoolActor {

  /**
    * A message to invoke the current `NumberSelector`'s functionality.
    */
  final case class GetAnyNumber()

  /**
    * A message to invoke a `SpecificSelector` to acquire the specific `number`, if it is available in this pool.
    * @param number the pre-selected number
    */
  final case class GetSpecificNumber(number: Int)

  /**
    * A message to distribute the `number` that was drawn.
    * @param number the pre-selected number
    */
  final case class GiveNumber(number: Int)

  final case class NoNumber(ex: Throwable)

  /**
    * A message to invoke the `returnNumber` functionality of the current `NumberSelector`.
    * @param number the number
    */
  final case class ReturnNumber(number: Int)

  final case class ReturnNumberResult(number: Int, ex: Option[Throwable])

  /**
    * Use the `SpecificSelector` on this pool to extract a specific object from the pool, if it is included and available.
    * Getting a specific number involves creating the appropriate selector and swapping with the current selector.
    * This process may involve re-formatting the underlying number pool array once for each selector.
    * @see `NumberSelector.Format`
    * @param pool the `NumberPool` to draw from
    * @param number the number requested
    * @return the number requested, or an error
    */
  def GetSpecificNumber(pool: NumberPool, number: Int): Try[Int] = {
    val original: NumberSelector   = pool.Selector
    val specific: SpecificSelector = new SpecificSelector
    pool.Selector = specific
    specific.SelectionIndex = pool.Numbers.indexOf(number)
    val out: Try[Int] = pool.Get()
    pool.Selector = original
    out
  }
}
