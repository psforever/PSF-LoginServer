// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.actor

import akka.actor.ActorRef
import akka.pattern.{AskTimeoutException, ask}
import akka.util.Timeout
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.NumberPoolHub

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * An `Actor` that wraps around converted `NumberPool`s and synchronizes a portion of the number registration process.
  * The ultimate goal is to manage a coherent group of unique identifiers for a given "region" (`Zone`).
  * Both parts of the UID system sit atop the `Zone` for easy external access.
  * The plain part - the `NumberPoolHub` here - is used for low-priority requests such as checking for existing associations.
  * This `Actor` is the involved portion that paces registration and unregistration.<br>
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
  * @param guid the `NumberPoolHub` that is partially manipulated by this `Actor`
  * @param poolActors a common mapping created from the `NumberPool`s in `guid`;
  *                   there is currently no check for this condition save for requests failing
  */
class UniqueNumberOps(
                       guid: NumberPoolHub,
                       poolActors: Map[String, ActorRef]
                     ) {
  private implicit val timeout = Timeout(2.seconds)

  def Register(
                obj: IdentifiableEntity,
                poolName: String
              ): Future[Any] = {
    val result: Promise[Any] = Promise()
    if (obj.HasGUID) {
      alreadyRegisteredTo(obj, poolName) match {
        case Some(pname) =>
          result.success {
            AlreadyRegisteredEntity(RegisteredEntity(obj, pname, guid, obj.GUID.guid))
          }
        case None =>
          result.completeWith {
            UniqueNumberOps.callbackWhenRegisteredToWrongPlace(obj, obj.GUID.guid)
          }
      }
    } else {
      result.completeWith {
        RegistrationProcess(obj, poolName)
      }
    }
    result.future
  }

  def Unregister(obj: IdentifiableEntity): Future[Any] = {
    val result: Promise[Any] = Promise()
    try {
      val number = obj.GUID.guid
      guid.WhichPool(number) match {
        case Some(pname) =>
          result.completeWith {
            UnregistrationProcess(obj, number, pname)
          }
        case None =>
          result.completeWith {
            UniqueNumberOps.callbackWhenRegisteredToWrongPlace(obj, number)
          }
      }
    } catch {
      case _: Exception =>
        UniqueNumberOps.log.warn(s"$obj is already unregistered")
        result.completeWith {
          Future(obj)
        }
    }
    result.future
  }

  /**
    * A step of the object registration process.
    * @see `RegistrationProcess(IdentifiableEntity, NumberPoolHub, Map[String, ActorRef], String)`
    * @param poolName the pool to which the object is trying to register
    */
  private def RegistrationProcess(
                                   obj: IdentifiableEntity,
                                   poolName: String
                                 ): Future[Any] = {
    RegistrationProcess(obj, guid, poolActors, poolName)
  }

  /**
    * A step of the object registration process.
    * Send a message to the `NumberPool` to request a number back.
    * If a number is received, continue with a successful registration process.
    * If no number is received, or some other issue occurs, attempt to recover from the error and report it.
    * This method is designed to be recursive as it is also utilized for the recovery attempt
    * and must pass all of the necessary information on to that next attempt.
    * @param poolName the pool to which the object is trying to register
    */
  private def RegistrationProcess(
                                   obj: IdentifiableEntity,
                                   hub: NumberPoolHub,
                                   poolActors: Map[String, ActorRef],
                                   poolName: String
                                 ): Future[Any] = {
    val promisingResult: Promise[Any] = Promise()
    poolActors.get(poolName) match {
      case Some(pool) =>
        //cache
        val localTarget = obj
        val localUns = hub
        val localPools = poolActors
        val localPoolName = poolName
        val localPool = pool
        val successFunc: (IdentifiableEntity, NumberPoolHub, String, ActorRef, Int) => Future[Any] =
          UniqueNumberOps.processRegisterResult
        val retryFunc: (IdentifiableEntity, NumberPoolHub, Map[String, ActorRef], String) => Future[Any] =
          RegistrationProcess

        val result = ask(pool, NumberPoolActor.GetAnyNumber(None))(timeout)
        result.onComplete {
          case Success(NumberPoolActor.GiveNumber(number, _)) =>
            promisingResult.completeWith {
              successFunc(localTarget, localUns, localPoolName, localPool, number)
            }
          case Success(NumberPoolActor.NoNumber(ex, _)) =>
            if (poolName.equals("generic")) {
              promisingResult.completeWith {
                Future(Failure(ex))
              }
            } else {
              promisingResult.completeWith {
                retryFunc(localTarget, localUns, localPools, "generic")
              }
            }
          case msg =>
            UniqueNumberOps.log.warn(s"unexpected message $msg during $localTarget's registration process")
        }
        result.recover {
          case ex: AskTimeoutException =>
            promisingResult.completeWith {
              Future(Failure(new Exception(s"did not register entity $localTarget in time", ex)))
            }
        }

      case None =>
        //do not log
        promisingResult.completeWith {
          Future(Failure(new Exception(s"can not find pool $poolName; $obj was not registered")))
        }
    }
    promisingResult.future
  }

  /**
    * A step of the object unregistration process.
    * Send a message to the `NumberPool` to restore the availability of one of its numbers.
    * @param poolName the pool to which the number will try to be returned
    * @param number the number that was previously drawn from the specified `NumberPool`
    */
  private def UnregistrationProcess(
                                     obj: IdentifiableEntity,
                                     number: Int,
                                     poolName: String
                                   ): Future[Any] = {
    //val localEntry = request
    val promisingResult: Promise[Any] = Promise()
    poolActors.get(poolName) match {
      case Some(pool) =>
        val localTarget = obj
        val localUns = guid
        val localPoolName = poolName
        val localPool = pool
        val localNumber = number
        val successFunc: (IdentifiableEntity, NumberPoolHub, String, ActorRef, Int) => Future[Any] =
          UniqueNumberOps.processUnregisterResult

        val result = ask(pool, NumberPoolActor.ReturnNumber(number, None))
        result.onComplete {
          case Success(NumberPoolActor.ReturnNumberResult(_, None, _)) =>
            promisingResult.completeWith {
              successFunc(localTarget, localUns, localPoolName, localPool, localNumber)
            }
          case Success(NumberPoolActor.ReturnNumberResult(_, Some(ex), _)) => //if there is a problem when returning the number
            promisingResult.completeWith {
              Future(Failure(new Exception(s"could not unregister $localTarget with number $localNumber", ex)))
            }
          case msg =>
            UniqueNumberOps.log.warn(s"unexpected message $msg during $localTarget's unregistration process")
        }
        result.recover {
          case ex: AskTimeoutException =>
            promisingResult.completeWith {
              Future(Failure(new Exception(s"did not register entity $localTarget in time", ex)))
            }
        }

      case None =>
        //do not log; use callback
        promisingResult.completeWith {
          Future(Failure(new Exception(s"can not find pool $poolName; $obj was not unregistered")))
        }
    }
    promisingResult.future
  }

  /**
    * Generate a relevant logging message for an object that is trying to register is actually already registered.
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
    * A step of the object registration process.
    * This step completes the registration by asking the `NumberPoolHub` to sort out its `NumberSource`.
    * @param number the number to use
    */
  private def processRegisterResult(
                                     obj: IdentifiableEntity,
                                     guid: NumberPoolHub,
                                     poolName: String,
                                     pool: ActorRef,
                                     number: Int
                                   ): Future[Any] = {
    guid.latterPartRegister(obj, number) match {
      case Success(_) =>
        Future(RegisteredEntity(obj, poolName, guid, number))
      case Failure(ex) =>
        //do not log; use callback
        returnNumberNoCallback(number, pool) //recovery?
        Future(Failure(ex))
    }
  }

  /**
    * A step of the object unregistration process.
    * This step completes revoking of the object's registration by consulting the `NumberSource`.
    * @param number the number to use
    */
  private def processUnregisterResult(
                                       obj: IdentifiableEntity,
                                       guid: NumberPoolHub,
                                       poolName: String,
                                       pool: ActorRef,
                                       number: Int
                                     ): Future[Any] = {
    guid.latterPartUnregister(number) match {
      case Some(_) =>
        obj.Invalidate()
        Future(UnregisteredEntity(obj, poolName, guid, number))
      case None =>
        //do not log; use callback
        requestSpecificNumberNoCallback(number, pool) //recovery?
        Future(Failure(
          new Exception(s"failed to unregister $obj from number $number; this may be a critical error")
        ))
    }
  }

  /**
    * Access a specific `NumberPool` in a way that doesn't invoke a callback and reset one of its numbers.
    * To avoid fully processing the callback, an id of `Long.MinValue` is used to short circuit the routine.
    * @param number the number that was drawn from a `NumberPool`
    * @param pool the `NumberPool` from which the `number` was drawn
    */
  private def returnNumberNoCallback(number: Int, pool: ActorRef): Future[Any] = {
    val result = ask(pool, NumberPoolActor.ReturnNumber(number, Some(Long.MinValue)))
    result.onComplete { _ => ; }
    result.recover { case _ => ; }
    result
  }

  /**
    * Access a specific `NumberPool` in a way that doesn't invoke a callback and claim one of its numbers.
    * To avoid fully processing the callback, an id of `Long.MinValue` is used to short circuit the routine.
    * @param number the number to be drawn from a `NumberPool`
    * @param pool the `NumberPool` from which the `number` is to be drawn
    */
  private def requestSpecificNumberNoCallback(number: Int, pool: ActorRef): Future[Any] = {
    val result = ask(pool, NumberPoolActor.GetSpecificNumber(number, Some(Long.MinValue)))
    result.onComplete { _ => ; }
    result.recover { case _ => ; }
    result
  }

  private def callbackWhenRegisteredToWrongPlace(obj: IdentifiableEntity, number: Int): Future[Any] = {
    Future(Failure(
      new Exception(s"$obj registered to number $number that is not part of a known or local number pool")
    ))
  }
}
