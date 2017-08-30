// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.actor

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.pool.NumberPool

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * An `Actor` that wraps around the `Actor` for a `NumberPool` and automates a portion of the number registration process.<br>
  * <br>
  * The `NumberPoolActor` that is created is used as the synchronized "gate" through which the number selection process occurs.
  * This `Actor` `ask`s the internal `Actor` and then waits on that `Future` to resolve.
  * For the registration process, once it resolves, a number for the accompanying object has been chosen.
  * The last part involves configuring the `NumberSource` of the hub so that it knows.
  * For the process of revoking registration, the number from the object is returned to the pool.
  * Like during the registration process, the `NumberSource` is then also updated.<br>
  * <br>
  * The object is always registered using the underlying governed `NumberPool`.
  * The object will not unregister if the object or its number are not recognized as members previously registered to the `NumberPool`.<br>
  * Whether or not an object or a specific number has been registered is always possible.
  * The scope encompasses the whole of the associated `NumberSource` as opposed to just this `NumberPool`.
  * @param hub the `NumberPoolHub` this `Actor` manipulates
  * @param pool the specific `NumberPool` this `Actor` maintains
  * @param poolActor a shared `Actor` that governs this `NumberPool`
  */
class NumberPoolAccessorActor(private val hub : NumberPoolHub, private val pool : NumberPool, private val poolActor : ActorRef) extends Actor {
  //the timeout is for when we ask the poolActor
  private implicit val timeout = Timeout(50 milliseconds)
  private[this] val log = org.log4s.getLogger

  private final case class GUIDRequest(obj : IdentifiableEntity, replyTo : ActorRef)
  private val requestQueue : collection.mutable.LongMap[GUIDRequest] = new collection.mutable.LongMap()
  private var index : Long = Long.MinValue

  def receive : Receive = {
    //register
    case Register(obj, _, None, call) =>
      try {
        obj.GUID //stop if object has a GUID; sometimes this happens
        log.warn(s"$obj already registered")
      }
      catch {
        case _ : Exception =>
          val id : Long = index
          index += 1
          requestQueue += id -> GUIDRequest(obj, call.getOrElse(sender()))
          poolActor ! NumberPoolActor.GetAnyNumber(Some(id))
      }

    case Register(obj, _, Some(number), call) =>
      try {
        obj.GUID //stop if object has a GUID; sometimes this happens
        log.warn(s"$obj already registered")
      }
      catch {
        case _ : Exception =>
          val id : Long = index
          index += 1
          requestQueue += id -> GUIDRequest(obj, call.getOrElse(sender()))
          poolActor ! NumberPoolActor.GetSpecificNumber(number, Some(id))
      }

    case NumberPoolActor.GiveNumber(number, id) =>
      id match {
        case Some(nid : Long) =>
          Register(nid, requestQueue.remove(nid), number)
        case _ =>
          pool.Return(number) //recovery?
          log.warn(s"received a number but there is no request to process it; returning number to pool")
      }

    case NumberPoolActor.NoNumber(ex, id) =>
      val req = id match {
        case Some(nid : Long) =>
          val req = requestQueue.remove(nid)
          if(req.isDefined) { s"$req" } else { s"a corresponding request $nid was not found;" }
        case _ =>
          "generic request;" //should be unreachable
      }
      log.warn(s"a number was not drawn from the pool; $req $ex")

    //unregister
    case Unregister(obj, call) =>
      val callback = call.getOrElse(sender())
      try {
        val number = obj.GUID.guid
        if(pool.Numbers.contains(number) && hub.WhichPool(obj).isDefined) {
          val id : Long = index
          index += 1
          requestQueue += id -> GUIDRequest(obj, callback)
          poolActor ! NumberPoolActor.ReturnNumber(number, Some(id))
        }
        else {
          callback ! Failure(new Exception(s"the GUID of object $obj - $number - is not a part of this number pool"))
        }
      }
      catch {
        case msg : Exception =>
          callback ! Failure(msg)
      }

    case NumberPoolActor.ReturnNumberResult(number, None, id) =>
      id match {
        case Some(nid : Long) =>
          Unregister(nid, requestQueue.remove(nid), number)
        case _ =>
          NumberPoolActor.GetSpecificNumber(pool, number) //recovery?
          log.error(s"returned a number but there is no request to process it; recovering the number from pool")
      }

    case NumberPoolActor.ReturnNumberResult(number, ex, id) =>
      val req = id match {
        case Some(nid : Long) =>
          val req = requestQueue.remove(nid)
          if(req.isDefined) { s"$req" } else { s"a corresponding request $nid was not found;" }
        case _ =>
          "generic request;" //should be unreachable
      }
      log.warn(s"a number $number was not returned to the pool; $req $ex")

    //common
    case IsRegistered(Some(obj), None) =>
      sender ! hub.isRegistered(obj)

    case IsRegistered(None, Some(number)) =>
      sender ! hub.isRegistered(number)

    case NumberPoolActor.ReturnNumber(number, _) =>
      sender ! (poolActor ? NumberPoolActor.ReturnNumber(number))

    case msg =>
      log.warn(s"unexpected message received - $msg")
  }

  /**
    * A step of the object registration process.
    * If there is a successful request object to be found, complete the registration request.
    * @param id the identifier of this request
    * @param request the request data
    * @param number the number that was drawn from the `NumberPool`
    */
  private def Register(id : Long, request : Option[GUIDRequest], number : Int) : Unit = {
    request match {
      case Some(GUIDRequest(obj, replyTo)) =>
        processRegisterResult(obj, number, replyTo)
      case None =>
        pool.Return(number) //recovery?
        log.warn(s"received a number but the request for it is missing; returning number to pool")
    }
  }

  /**
    * A step of the object registration process.
    * This step completes the registration by consulting the `NumberSource`.
    * @param obj the object
    * @param number the number to use
    * @param callback an optional callback `ActorRef`
    */
  private def processRegisterResult(obj : IdentifiableEntity, number : Int, callback : ActorRef) : Unit = {
    try {
      obj.GUID
      pool.Return(number) //recovery?
      callback ! Success(obj)
    }
    catch {
      case _ : Exception =>
        hub.latterPartRegister(obj, number) match {
          case Success(_) =>
            callback ! Success(obj)
          case Failure(ex) =>
            pool.Return(number) //recovery?
            callback ! Failure(ex)
        }
    }
  }

  /**
    * A step of the object un-registration process.
    * If there is a successful request object to be found, complete the registration request.
    * @param id the identifier of this request
    * @param request the request data
    * @param number the number that was drawn from the `NumberPool`
    */
  private def Unregister(id : Long, request : Option[GUIDRequest], number : Int) : Unit = {
    request match {
      case Some(GUIDRequest(obj, replyTo)) =>
        processUnregisterResult(obj, obj.GUID.guid, replyTo)
      case None =>
        NumberPoolActor.GetSpecificNumber(pool, number) //recovery?
        log.error(s"returned a number but the rest of the request is missing; recovering the number from pool")
    }
  }

  /**
    * A step of the object un-registration process.
    * This step completes revoking the object's registration by consulting the `NumberSource`.
    * @param obj the object
    * @param callback an optional callback `ActorRef`
    */
  private def processUnregisterResult(obj : IdentifiableEntity, number : Int, callback : ActorRef) : Unit = {
    hub.latterPartUnregister(number) match {
      case Some(_) =>
        obj.Invalidate()
        callback ! Success(obj)
      case None =>
        NumberPoolActor.GetSpecificNumber(pool, number) //recovery?
        callback ! Failure(new Exception(s"failed to unregister a number; this may be a critical error"))
    }
  }
}
