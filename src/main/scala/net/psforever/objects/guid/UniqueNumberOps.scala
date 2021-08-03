// Copyright (c) 2017-2021 PSForever
package net.psforever.objects.guid

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.uns.{
  AlreadyRegisteredEntity,
  AlreadyUnregisteredEntity,
  NumberPoolActor,
  RegisteredEntity,
  UnregisteredEntity
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * Wrap around converted `NumberPool`s and synchronize a portion of the number registration process
  * as a part of the global unique identifier (GUID, UID) number system (UNS, "unique number system").
  * The ultimate goal is to manage a coherent group of unique identifiers for a given "region".
  * Both parts of the UID system sit atop the `Zone` for easy external access.
  * The plain part - the `NumberPoolHub` here - is used for low-priority requests
  * such as checking for existing associations.<br>
  * <br>
  * A four part process is used for object registration tasks.
  * First, the requested `NumberPool` is located among the list of known `NumberPool`s.
  * Second, an asynchronous request is sent to that pool to retrieve a number.
  * (Only any number.  Only a failing case allows for selection of a specific number.)
  * Third, the asynchronous request returns and the original information about the request is recovered.
  * Fourth, both sides of the contract are completed by the object being assigned the number and
  * the underlying "number source" is made to remember an association between the object and the number.
  * Short circuits and recoveries as available on all steps though reporting is split between logging and callbacks.
  * The process of removing the association between a number and object (unregistering) is a similar four part process.<br>
  * <br>
  * The important relationship between this `Actor` and the `Map` of `NumberPoolActors` is as a "gate."
  * A single `Map` is constructed and shared between multiple entry points to the UID system where requests are messaged.
  * Multiple entry points send messages to the same `NumberPool`.
  * That `NumberPool` deals with the messages one at a time and sends reply to each entry point that communicated with it.
  * This process is almost as fast as the process of the `NumberPool` selecting a number.
  * (At least, both should be fast.)
  * @param guid the supporting datatype for the unique number distribution
  * @param poolActors a mapping created from the `NumberPool`s, to achieve synchronized access
  */
class UniqueNumberOps(
                       private val guid: NumberPoolHub,
                       private val poolActors: Map[String, ActorRef]
                     ) {
  /** The timeout used by all number pool `ask` messaging */
  private implicit val timeout = UniqueNumberOps.timeout

  /**
    * The entry point for the entity GUID registration process.
    * A special check is made first to determine if the entity is already registered, and if so to where.
    * If the entity is not registered, then the process continues.
    * @param obj the entity to be assigned a GUID
    * @param poolName the pool from which the entity wants a GUID to be selected
    * @return the anticipation of this activity being completed
    */
  def Register(
                obj: IdentifiableEntity,
                poolName: String
              ): Future[Any] = {
    val result: Promise[Any] = Promise()
    if (obj.HasGUID) {
      alreadyRegisteredTo(obj, poolName) match {
        case Some(pname) =>
          result.success(AlreadyRegisteredEntity(RegisteredEntity(obj, pname, guid, obj.GUID.guid)))
        case None =>
          result.failure(new RegisteredToWrongPlaceException(obj, obj.GUID.guid))
      }
    } else {
      result.completeWith(registrationProcess(obj, poolName))
    }
    result.future
  }

  /**
    * The entry point for the entity GUID unregistration process.
    * A special check is made first to determine where, if at all, the entity is registered.
    * Obviously, if the entity is not registered somewhere within purview of this UNS, the process can not continue.
    * If the entity's registration number pool is found, then the process continues.
    * @param obj the entity to be unassigned its GUID
    * @return the anticipation of this activity being completed
    */
  def Unregister(obj: IdentifiableEntity): Future[Any] = {
    val result: Promise[Any] = Promise()
    if (obj.HasGUID) {
      val number = obj.GUID.guid
      guid.WhichPool(number) match {
        case Some(pname) =>
          result.completeWith(unregistrationProcess(obj, number, pname))
        case None =>
          result.failure(new RegisteredToWrongPlaceException(obj, number))
      }
    } else {
      UniqueNumberOps.log.warn(s"$obj is already unregistered")
      result.success(Future(AlreadyUnregisteredEntity(UnregisteredEntity(obj, "", guid, -1))))
    }
    result.future
  }

  /**
    * A step of the entity GUID registration process.
    * Pass control through to the next step.
    * @see `registrationProcess(IdentifiableEntity, NumberPoolHub, Map[String, ActorRef], String)`
    * @param obj the entity to be assigned a GUID
    * @param poolName the pool to which the object is trying to register
    * @return the anticipation of this activity being completed
    */
  private def registrationProcess(
                                   obj: IdentifiableEntity,
                                   poolName: String
                                 ): Future[Any] = {
    registrationProcess(obj, guid, poolActors, poolName)
  }

  /**
    * A step of the entity GUID registration process.
    * Send a message to the `NumberPool` to request a number.
    * If a number is received, continue with a successful registration process.
    * If no number is received, or some other issue occurs, attempt to recover from the error and report it.
    * This method is designed to be recursive as it is also utilized for the recovery attempt
    * and must pass all of the necessary information on to that next attempt.
    * @param obj the entity to be assigned a GUID
    * @param hub the supporting datatype for the unique number distribution
    * @param pools a mapping created from the `NumberPool`s, to achieve synchronized access
    * @param poolName the pool to which the object is trying to register
    * @return the anticipation of this activity being completed
    */
  private def registrationProcess(
                                   obj: IdentifiableEntity,
                                   hub: NumberPoolHub,
                                   pools: Map[String, ActorRef],
                                   poolName: String
                                 ): Future[Any] = {
    val promisingResult: Promise[Any] = Promise()
    pools.get(poolName) match {
      case Some(pool) =>
        //cache
        val localPromise = promisingResult
        val localTarget = obj
        val localUns = hub
        val localPools = pools
        val localPoolName = poolName
        val localPool = pool

        val result = ask(pool, NumberPoolActor.GetAnyNumber())(timeout)
        result.onComplete {
          case Success(NumberPoolActor.GiveNumber(number)) =>
            UniqueNumberOps.processRegisterResult(
              localPromise,
              localTarget,
              localUns,
              localPoolName,
              localPool,
              number
            )
          case Success(NumberPoolActor.NoNumber(ex)) =>
            registrationProcessRetry(localPromise, ex, localTarget, localUns, localPools, localPoolName)
          case msg =>
            UniqueNumberOps.log.warn(s"unexpected message during $localTarget's registration process - $msg")
        }
        result.recover {
          case ex: AskTimeoutException =>
            localPromise.failure(new RegisteringException(msg = s"did not register entity $localTarget in time", ex))
        }

      case None =>
        //do not log
        val ex = new Exception(s"can not find pool $poolName")
        registrationProcessRetry(promisingResult, ex, obj, guid, pools, poolName)
    }
    promisingResult.future
  }

  /**
    * na
    * @param promise the ongoing promise to be fulfilled for the future
    * @param exception an issue that has arrisen, forcing the retry attempt
    * @param obj the entity to be assigned a GUID
    * @param hub the supporting datatype for the unique number distribution
    * @param pools a mapping created from the `NumberPool`s, to achieve synchronized access
    * @param poolName the pool to which the object is trying to register
    */
  def registrationProcessRetry(
                                promise: Promise[Any],
                                exception: Throwable,
                                obj: IdentifiableEntity,
                                hub: NumberPoolHub,
                                pools: Map[String, ActorRef],
                                poolName: String
                              ): Unit = {
    if (poolName.equals("generic")) {
      promise.failure(new RegisteringException(msg = s"did not register entity $obj", exception))
    } else {
      org.log4s.getLogger("UniqueNumberOps").warn(s"${exception.getLocalizedMessage()} - $poolName")
      promise.completeWith(registrationProcess(obj, guid, pools, poolName = "generic"))
    }
  }

  /**
    * A step of the entity GUID unregistration process.
    * Pass control through to the next step.
    * @see `unregistrationProcess(IdentifiableEntity, NumberPoolHub, Map[String, ActorRef], Int, String)`
    * @param obj the entity to be unassigned its GUID
    * @param number the number that was previously drawn from the specified `NumberPool`
    * @param poolName the pool to which the number will try to be returned
    * @return the anticipation of this activity being completed
    */
  private def unregistrationProcess(
                                     obj: IdentifiableEntity,
                                     number: Int,
                                     poolName: String
                                   ): Future[Any] = {
    unregistrationProcess(obj, guid, poolActors, number, poolName)
  }

  /**
    * A step of the entity GUID unregistration process.
    * ...
    * @param obj the entity to be unassigned its GUID
    * @param hub the supporting datatype for the unique number distribution
    * @param pools a mapping created from the `NumberPool`s, to achieve synchronized access
    * @param number the number that was previously drawn from the specified `NumberPool`
    * @param poolName the pool to which the number will try to be returned
    * @return the anticipation of this activity being completed
    */
  private def unregistrationProcess(
                                     obj: IdentifiableEntity,
                                     hub: NumberPoolHub,
                                     pools: Map[String, ActorRef],
                                     number: Int,
                                     poolName: String
                                   ): Future[Any] = {
    val promisingResult: Promise[Any] = Promise()
    pools.get(poolName) match {
      case Some(pool) =>
        //cache
        val localPromise = promisingResult
        val localTarget = obj
        val localUns = hub
        val localPoolName = poolName
        val localPool = pool
        val localNumber = number

        val result = ask(pool, NumberPoolActor.ReturnNumber(number))
        result.onComplete {
          case Success(NumberPoolActor.ReturnNumberResult(_, None)) =>
            UniqueNumberOps.processUnregisterResult(
              localPromise,
              localTarget,
              localUns,
              localPoolName,
              localPool,
              localNumber
            )
          case Success(NumberPoolActor.ReturnNumberResult(_, Some(ex))) => //if there is a problem when returning the number
            localPromise.failure { new UnregisteringException(msg = s"could not unregister $localTarget with number $localNumber", ex) }
          case msg =>
            UniqueNumberOps.log.warn(s"unexpected message $msg during $localTarget's unregistration process")
        }
        result.recover {
          case ex: AskTimeoutException =>
            localPromise.failure { new UnregisteringException(msg = s"did not unregister entity $localTarget in time", ex) }
        }

      case None =>
        //do not log; use callback
        promisingResult.failure { new UnregisteringException(msg = s"can not find pool $poolName; $obj was not unregistered") }
    }
    promisingResult.future
  }

  /**
    * Generate a relevant logging message for an object that is trying to register to this UNS
    * but is actually already registered to this UNS.
    * Also take note if the entity is (probably) not registered to this UNS.
    * @param obj the object that was trying to register
    * @param poolName the pool to which the object was trying to register
    * @return the pool name to which the entity is registered, if it can be discovered
    */
  private def alreadyRegisteredTo(obj: IdentifiableEntity, poolName: String): Option[String] = {
    val (msg, determinedName) =
      guid.WhichPool(obj) match {
        case out @ Some(pname) =>
          if (poolName.equals(pname)) {
            (s"to pool $poolName", Some(poolName))
          } else {
            (s"but to different pool $pname", out)
          }
        case None =>
          ("but not to any pool known to this system", None)
      }
    UniqueNumberOps.log.warn(s"$obj already registered $msg")
    determinedName
  }
}

object UniqueNumberOps {
  private val log   = org.log4s.getLogger
  private implicit val timeout = Timeout(2.seconds)

  /**
    * Final step of the object registration process.
    * This step completes the registration by asking the `NumberPoolHub` to sort out its `NumberSource`.
    * @param promise the ongoing promise to be fulfilled for the future
    * @param obj the entity to be assigned a GUID
    * @param guid the supporting datatype for the unique number distribution
    * @param poolName the name of the pool to which the object is trying to register
    * @param pool the pool to which the object is trying to register
    * @param number the number that was drawn
    */
  private def processRegisterResult(
                                     promise: Promise[Any],
                                     obj: IdentifiableEntity,
                                     guid: NumberPoolHub,
                                     poolName: String,
                                     pool: ActorRef,
                                     number: Int
                                   ): Unit = {
    guid.latterPartRegister(obj, number) match {
      case Success(_) =>
        promise.success(RegisteredEntity(obj, poolName, guid, number))
      case Failure(ex) =>
        //do not log; use callback
        returnNumberNoCallback(number, pool) //recovery?
        promise.failure(ex)
    }
  }

  /**
    * A step of the object unregistration process.
    * This step completes revoking of the object's registration by consulting the `NumberSource`.
    * @param promise the ongoing promise to be fulfilled for the future
    * @param obj the entity to be unassigned its GUID
    * @param guid the supporting datatype for the unique number distribution
    * @param poolName the name of pool to which the number will try to be returned
    * @param pool the pool to which the number will try to be returned
    * @param number the number that was previously drawn from the specified `NumberPool`
    */
  private def processUnregisterResult(
                                       promise: Promise[Any],
                                       obj: IdentifiableEntity,
                                       guid: NumberPoolHub,
                                       poolName: String,
                                       pool: ActorRef,
                                       number: Int
                                     ): Unit = {
    guid.latterPartUnregister(number) match {
      case Some(_) =>
        obj.Invalidate()
        promise.success(UnregisteredEntity(obj, poolName, guid, number))
      case None =>
        //do not log
        requestSpecificNumberNoCallback(number, pool) //recovery?
        promise.failure(new UnregisteringException(msg = s"failed to unregister $obj from number $number; this may be a critical error"))
    }
  }

  /**
    * Access a specific `NumberPool` in a way that doesn't propagate a callback and reset one of its numbers.
    * The `ask` pattern catches any reply message and ensures nothing happens because of it.
    * @param number the number that was drawn from a `NumberPool`
    * @param pool the `NumberPool` from which the `number` was drawn
    */
  private def returnNumberNoCallback(number: Int, pool: ActorRef): Unit = {
    val result = ask(pool, NumberPoolActor.ReturnNumber(number))
    result.onComplete { _ => ; }
    result.recover { case _ => ; }
  }

  /**
    * Access a specific `NumberPool` in a way that doesn't propagate a callback and claim one of its numbers.
    * The `ask` pattern catches any reply message and ensures nothing happens because of it.
    * @param number the number to be drawn from a `NumberPool`
    * @param pool the `NumberPool` from which the `number` is to be drawn
    */
  private def requestSpecificNumberNoCallback(number: Int, pool: ActorRef): Unit = {
    val result = ask(pool, NumberPoolActor.GetSpecificNumber(number))
    result.onComplete { _ => ; }
    result.recover { case _ => ; }
  }
}

class RegisteringException(msg: String) extends Exception(msg) {
  def this(msg: String, cause: Throwable) = {
    this(msg)
    initCause(cause)
  }
}

class UnregisteringException(msg: String) extends Exception(msg) {
  def this(msg: String, cause: Throwable) = {
    this(msg)
    initCause(cause)
  }
}

/**
  * The entity was registered, but not to the target UNS.
  * Rookie mistake.
  * @param obj the entity to be assigned a GUID
  * @param number the name associated with this entity
  */
class RegisteredToWrongPlaceException(obj: IdentifiableEntity, number: Int)
  extends RuntimeException(s"$obj registered to number $number that is not part of a known or local number pool") {

  def this(obj: IdentifiableEntity, number: Int, cause: Throwable) = {
    this(obj, number)
    initCause(cause)
  }
}

/**
  * A class for spawning `Actor`s to manage the number pools and
  * create a number system operations class to access those pools within the context of registering and unregistering.
  * This `Actor` persists to maintain the number pool `Actor`s.
  * Note the `final` do-nothing `receive` method.
  * This `Actor` should do __nothing__ through message passing.
  * @see `UniqueNumberOps`
  * @param hub the number pool management class
  * @param poolActorConversionFunc the number pool management class
  */
class UniqueNumberSetup(
                         hub: NumberPoolHub,
                         poolActorConversionFunc: (ActorContext, NumberPoolHub) => Map[String, ActorRef]
                       ) extends Actor {
  init()

  final def receive: Receive = { case _ => ; }

  def init(): UniqueNumberOps = {
    new UniqueNumberOps(hub, poolActorConversionFunc(context, hub))
  }
}

object UniqueNumberSetup {

  /**
    * Transform `NumberPool`s into `NumberPoolActor`s and pair them with their name.
    * @param poolSource where the raw `NumberPools` are located
    * @param context used to create the `NumberPoolActor` instances
    * @return a `Map` of the pool names to the `ActorRef` created from the `NumberPool`
    */
  def AllocateNumberPoolActors(context: ActorContext, poolSource: NumberPoolHub): Map[String, ActorRef] = {
    poolSource.Pools
      .map { case (pname, pool) => (pname, context.actorOf(Props(classOf[NumberPoolActor], pool), pname)) }
      .toMap
  }
}
