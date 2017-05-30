// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.actor

import akka.pattern.ask
import akka.util.Timeout
import akka.actor.{Actor, ActorRef, Props}

import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.NumberPoolHub
import net.psforever.objects.guid.pool.NumberPool

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * An incoming message for retrieving a specific `NumberPoolAccessorActor`.
  * @param name the name of the accessor's `NumberPool`
  */
final case class RequestPoolActor(name : String)

/**
  * An outgoing message for giving a specific `NumberPoolAccessorActor`.
  * @param name the name of the accessor's `NumberPool`, for reference
  * @param actor the accessor
  */
final case class DeliverPoolActor(name : String, actor : ActorRef)

/**
  * An `Actor` that wraps around the management system for `NumberPools`.<br>
  * <br>
  * By just instantiating, this object builds and stores a `NumberPoolAccessorActor` for each `NumberPool` known to the `hub`.
  * Additional `NumberPool`s created by the `hub` need to be paired with a created accessor manually.
  * Each accessor is the primary entry point to a registration process for the specific `NumberPool` it represents.
  * The `hub` `Actor` itself distribute any registration task it receives out to an applicable accessor of which it is aware.
  * It will attempt to revoke registration on its own, without relying on the functionality from any accessor.<br>
  * <br>
  * In the same way that `NumberPoolHub` is a tool for keeping track of `NumberPool` objects,
  * its `Actor` is a tool for keeping track of accessors created from `NumberPool` objects.
  * It is very, however, for handling unspecific revoke tasks.
  * @param hub the central `NumberPool` management object for an embedded `NumberSource` object
  */
class NumberPoolHubActor(private val hub : NumberPoolHub) extends Actor {
  private val actorHash : mutable.HashMap[String, ActorRef] = mutable.HashMap[String, ActorRef]()
  hub.Pools.foreach({ case(name, pool) => CreatePoolActor(name, pool) })
  implicit val timeout = Timeout(50 milliseconds)
  private[this] val log = org.log4s.getLogger

  def receive : Receive = {
    case RequestPoolActor(name) =>
      sender ! (GetPoolActor(name) match {
        case Success(poolActor) =>
          DeliverPoolActor(name, poolActor)
        case Failure(ex) =>
          Failure(ex)
      })

    case Register(obj, name, None, callback) =>
      HubRegister(obj, name, callback)

    case Register(obj, name, Some(number), callback) =>
      HubRegister(obj, name, number, callback)

    //common
    case IsRegistered(Some(obj), None) =>
      sender ! hub.isRegistered(obj)

    case IsRegistered(None, Some(number)) =>
      sender ! hub.isRegistered(number)

    case Unregister(obj, callback) =>
      Unregister(obj, if(callback.isEmpty) { sender } else { callback.get })

    case msg =>
      log.warn(s"unexpected message received - ${msg.toString}")
  }

  /**
    * From a name, find an existing `NumberPoolAccessorActor`.
    * @param name the accessor's name
    * @return the accessor that was requested
    */
  private def GetPoolActor(name : String) : Try[ActorRef] = {
    actorHash.get(name) match {
      case Some(actor) =>
        Success(actor)
      case _ =>
        Failure(new Exception(s"number pool $name not defined"))
    }
  }

  /**
    * Create a new `NumberPoolAccessorActor` and add it to the local collection of accessors.
    * @param name the accessor's name
    * @param pool the underlying `NumberPool`
    */
  private def CreatePoolActor(name : String, pool : NumberPool) : Unit = {
    actorHash.get(name) match {
      case None =>
        actorHash += name -> context.actorOf(Props(classOf[NumberPoolAccessorActor], hub, pool), s"${name}Actor")
      case Some(_) =>
      //TODO complain?
    }
  }

  /**
    * A step of the object registration process.
    * Select a valid `NumberPoolAccessorActor` and pass a task onto it.
    * @param obj an object
    * @param name a potential accessor pool
    * @param callback an optional callback `ActorRef`
    */
  private def HubRegister(obj : IdentifiableEntity, name : Option[String], callback : Option[ActorRef]) : Unit = {
    val genericPool = actorHash("generic")
    val pool = if(name.isDefined) { actorHash.get(name.get).orElse(Some(genericPool)).get } else { genericPool }
    pool ! Register(obj, None, None, callback)
  }

  /**
    * A step of the object registration process.
    * Determine to which `NumberPool` the `number` belongs.
    * @param obj an object
    * @param name a potential accessor pool
    * @param number a potential number
    * @param callback an optional callback `ActorRef`
    */
  private def HubRegister(obj : IdentifiableEntity, name : Option[String], number : Int, callback : Option[ActorRef]) : Unit = {
    hub.WhichPool(number) match {
      case Some(poolname) =>
        HubRegister_GetActor(obj, name, poolname, number, callback)
      case None =>
        self ! Register(obj, name, None, callback)
    }
  }

  /**
    * A step of the object registration process.
    * Pass a task onto an accessor or, if the accessor can not be found, attempt to recover.
    * @param obj an object
    * @param name a potential accessor pool
    * @param poolname the suggested accessor pool
    * @param number a potential number
    * @param callback an optional callback `ActorRef`
    */
  private def HubRegister_GetActor(obj : IdentifiableEntity, name : Option[String], poolname : String, number : Int, callback : Option[ActorRef]) : Unit = {
    actorHash.get(poolname) match {
      case Some(pool) =>
        pool ! Register(obj, None, Some(number), callback)
      case None =>
        HubRegister_MissingActor(obj, name, poolname, number, callback)
    }
  }

  /**
    * A step of the object registration process.
    * If an accessor could not be found in the last step, attempt to create the accessor.
    * If the accessor can not be created, the `number` can not be used;
    * fall back on the original pool (`name`).
    * @param obj an object
    * @param name a potential accessor pool
    * @param poolname the suggested accessor pool
    * @param number a potential number
    * @param callback an optional callback `ActorRef`
    */
  private def HubRegister_MissingActor(obj : IdentifiableEntity, name : Option[String], poolname : String, number : Int, callback : Option[ActorRef]) : Unit = {
    hub.GetPool(poolname) match {
      case Some(pool) =>
        CreatePoolActor(poolname, pool)
        actorHash(poolname) ! Register(obj, None, Some(number), callback)
      case None =>
        log.error(s"matched number $number to pool $poolname, but could not find $poolname when asked")
        self ! Register(obj, name, None, callback)
    }
  }

  /**
    * A step of the object un-registration process.
    * This step locates the `NumberPool` to which this object is a member.
    * If found, it prepares a `Future` to resolve later regarding whether the `NumberPool` accepted the number.
    * @param obj the object
    * @param callback a callback `ActorRef`
    */
  private def Unregister(obj : IdentifiableEntity, callback : ActorRef) : Unit = {
    hub.WhichPool(obj) match {
      case Some(name) =>
        val objToUnregister = obj
        val poolName = name
        processUnregisterResult(objToUnregister, (actorHash(poolName) ? NumberPoolActor.ReturnNumber(objToUnregister.GUID.guid)).mapTo[Boolean], callback)
      case None =>
        callback ! UnregisterFailure(obj, new Exception("could not find pool object is member of"))
    }
  }

  /**
    * A step of the object un-registration process.
    * This step completes revoking the object's registration by consulting the `NumberSource`.
    * @param obj the object
    * @param result whether the number was returned in the last step
    * @param callback a callback `ActorRef`
    */
  private def processUnregisterResult(obj : IdentifiableEntity, result : Future[Boolean], callback : ActorRef) : Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    result.foreach {
      case true =>
        hub.latterPartUnregister(obj.GUID.guid)
        callback ! UnregisterSuccess(obj)
      case false =>
        callback ! UnregisterFailure(obj, new Exception("could not find object to remove"))
    }
  }
}
