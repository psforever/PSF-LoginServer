// Copyright (c) 2017 PSForever
package net.psforever.objects.guid.actor

import akka.actor.{Actor, ActorContext, ActorRef, Props}
import net.psforever.objects.entity.IdentifiableEntity
import net.psforever.objects.guid.NumberPoolHub

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
  * The important relationship between this `Actor` and the `Map` of `NumberPoolActors` is an "gate."
  * A single `Map` is constructed and shared between multiple entry points to the UID system where requests are messaged.
  * Multiple entry points send messages to the same `NumberPool`.
  * That `NumberPool` deals with the messages one at a time and sends reply to each entry point that communicated with it.
  * This process is almost as fast as the process of the `NumberPool` selecting a number.
  * (At least, both should be fast.)
  * @param guid the `NumberPoolHub` that is partially manipulated by this `Actor`
  * @param poolActors a common mapping created from the `NumberPool`s in `guid`;
  *                   there is currently no check for this condition save for requests failing
  */
class UniqueNumberSystem(private val guid: NumberPoolHub, private val poolActors: Map[String, ActorRef]) extends Actor {

  /** Information about Register and Unregister requests that persists between messages to a specific `NumberPool`. */
  private val requestQueue: collection.mutable.LongMap[UniqueNumberSystem.GUIDRequest] =
    new collection.mutable.LongMap()

  /** The current value for the next request entry's index. */
  private var index: Long = Long.MinValue
  private[this] val log   = org.log4s.getLogger

  def receive: Receive = {
    case Register(obj, Some(pname), None, call) =>
      val callback = call.getOrElse(sender())
      if (obj.HasGUID) {
        AlreadyRegistered(obj, pname)
        callback ! Success(obj)
      } else {
        val id: Long = index
        index += 1
        requestQueue += id -> UniqueNumberSystem.GUIDRequest(obj, pname, callback)
        RegistrationProcess(pname, id)
      }

    //this message is automatically sent by NumberPoolActor
    case NumberPoolActor.GiveNumber(number, id) =>
      id match {
        case Some(nid: Long) =>
          RegistrationProcess(requestQueue.remove(nid), number, nid)
        case _ =>
          log.warn(s"received a number but there is no request to process it; returning number to pool")
          NoCallbackReturnNumber(number) //recovery?
        //no callback is possible
      }

    //this message is automatically sent by NumberPoolActor
    case NumberPoolActor.NoNumber(ex, id) =>
      id match {
        case Some(nid: Long) =>
          requestQueue.remove(nid) match {
            case Some(entry) =>
              entry.replyTo ! Failure(ex) //ONLY callback that is possible
            case None => ;
              log.warn(
                s"failed number request and no record of number request - $ex"
              ) //neither a successful request nor an entry of making the request
          }
        case None => ;
          log.warn(
            s"failed number request and no record of number request - $ex"
          ) //neither a successful request nor an entry of making the request
        case _ => ;
          log.warn(s"unrecognized request $id accompanying a failed number request - $ex")
      }

    case Unregister(obj, call) =>
      val callback = call.getOrElse(sender())
      try {
        val number = obj.GUID.guid
        guid.WhichPool(number) match {
          case Some(pname) =>
            val id: Long = index
            index += 1
            requestQueue += id -> UniqueNumberSystem.GUIDRequest(obj, pname, callback)
            UnregistrationProcess(pname, number, id)
          case None =>
            callback ! Failure(new Exception(s"the GUID of object $obj - $number - is not a part of this number pool"))
        }
      } catch {
        case _: Exception =>
          log.info(s"$obj is already unregistered")
          callback ! Success(obj)
      }

    //this message is automatically sent by NumberPoolActor
    case NumberPoolActor.ReturnNumberResult(number, None, id) =>
      id match {
        case Some(nid: Long) =>
          UnregistrationProcess(requestQueue.remove(nid), number, nid)
        case _ =>
          log.error(s"returned a number but there is no request to process it; recovering the number from pool")
          NoCallbackGetSpecificNumber(number) //recovery?
        //no callback is possible
      }

    //this message is automatically sent by NumberPoolActor
    case NumberPoolActor.ReturnNumberResult(number, Some(ex), id) => //if there is a problem when returning the number
      id match {
        case Some(nid: Long) =>
          requestQueue.remove(nid) match {
            case Some(entry) =>
              entry.replyTo ! Failure(new Exception(s"for ${entry.target} with number $number, ${ex.getMessage}"))
            case None => ;
              log.error(s"could not find original request $nid that caused error $ex, but pool was ${sender()}")
            //no callback is possible
          }
        case _ => ;
          log.error(s"could not find original request $id that caused error $ex, but pool was ${sender()}")
        //no callback is possible
      }

    case msg =>
      log.warn(s"unexpected message received - $msg")
  }

  /**
    * A step of the object registration process.
    * Send a message to the `NumberPool` to request a number back.
    * @param poolName the pool to which the object is trying to register
    * @param id a potential identifier to associate this request
    */
  private def RegistrationProcess(poolName: String, id: Long): Unit = {
    poolActors.get(poolName) match {
      case Some(pool) =>
        pool ! NumberPoolActor.GetAnyNumber(Some(id))
      case None =>
        //do not log; use callback
        requestQueue.remove(id).get.replyTo ! Failure(
          new Exception(s"can not find pool $poolName; nothing was registered")
        )
    }
  }

  /**
    * A step of the object registration process.
    * If there is a successful request object to be found, continue the registration request.
    * @param request the original request data
    * @param number the number that was drawn from a `NumberPool`
    */
  private def RegistrationProcess(request: Option[UniqueNumberSystem.GUIDRequest], number: Int, id: Long): Unit = {
    request match {
      case Some(entry) =>
        processRegisterResult(entry, number)
      case None =>
        log.error(s"returned a number but the rest of the request is missing (id:$id)")
        if (id != Long.MinValue) { //check to ignore endless loop of error-catching
          log.warn("returning number to pool")
          NoCallbackReturnNumber(number) //recovery?
          //no callback is possible
        }
    }
  }

  /**
    * A step of the object registration process.
    * This step completes the registration by asking the `NumberPoolHub` to sort out its `NumberSource`.
    * @param entry the original request data
    * @param number the number to use
    */
  private def processRegisterResult(entry: UniqueNumberSystem.GUIDRequest, number: Int): Unit = {
    val obj = entry.target
    guid.latterPartRegister(obj, number) match {
      case Success(_) =>
        entry.replyTo ! Success(obj)
      case Failure(ex) =>
        //do not log; use callback
        NoCallbackReturnNumber(number, entry.targetPool) //recovery?
        entry.replyTo ! Failure(ex)
    }
  }

  /**
    * A step of the object unregistration process.
    * Send a message to the `NumberPool` to restore the availability of one of its numbers.
    * @param poolName the pool to which the number will try to be returned
    * @param number the number that was previously drawn from the specified `NumberPool`
    * @param id a potential identifier to associate this request
    */
  private def UnregistrationProcess(poolName: String, number: Int, id: Long): Unit = {
    poolActors.get(poolName) match {
      case Some(pool) =>
        pool ! NumberPoolActor.ReturnNumber(number, Some(id))
      case None =>
        //do not log; use callback
        requestQueue.remove(id).get.replyTo ! Failure(
          new Exception(s"can not find pool $poolName; nothing was de-registered")
        )
    }
  }

  /**
    * A step of the object unregistration process.
    * If there is a successful request object to be found, continue the registration request.
    * @param request the original request data
    * @param number the number that was drawn from the `NumberPool`
    */
  private def UnregistrationProcess(request: Option[UniqueNumberSystem.GUIDRequest], number: Int, id: Long): Unit = {
    request match {
      case Some(entry) =>
        processUnregisterResult(entry, number)
      case None =>
        log.error(s"returned a number but the rest of the request is missing (id:$id)")
        if (id != Long.MinValue) { //check to ignore endless loop of error-catching
          log.error("recovering the number from pool")
          NoCallbackGetSpecificNumber(number) //recovery?
          //no callback is possible
        }
    }
  }

  /**
    * A step of the object unregistration process.
    * This step completes revoking of the object's registration by consulting the `NumberSource`.
    * @param entry the original request data
    * @param number the number to use
    */
  private def processUnregisterResult(entry: UniqueNumberSystem.GUIDRequest, number: Int): Unit = {
    val obj = entry.target
    guid.latterPartUnregister(number) match {
      case Some(_) =>
        obj.Invalidate()
        entry.replyTo ! Success(obj)
      case None =>
        //do not log; use callback
        NoCallbackGetSpecificNumber(number, entry.targetPool) //recovery?
        entry.replyTo ! Failure(new Exception(s"failed to unregister a number; this may be a critical error"))
    }
  }

  /**
    * Generate a relevant logging message for an object that is trying to register is actually already registered.
    * @param obj the object that was trying to register
    * @param poolName the pool to which the object was trying to register
    */
  private def AlreadyRegistered(obj: IdentifiableEntity, poolName: String): Unit = {
    val msg =
      guid.WhichPool(obj) match {
        case Some(pname) =>
          if (poolName.equals(pname)) {
            s"to pool $poolName"
          } else {
            s"but to different pool $pname"
          }
        case None =>
          "but not to any pool known to this system"
      }
    log.warn(s"$obj already registered $msg")
  }

  /**
    * Access a specific `NumberPool` in a way that doesn't invoke a callback and reset one of its numbers.
    * @param number the number that was drawn from a `NumberPool`
    */
  private def NoCallbackReturnNumber(number: Int): Unit = {
    guid.WhichPool(number) match {
      case Some(pname) =>
        NoCallbackReturnNumber(number, pname)
      case None =>
        log.error(s"critical: tried to return number $number but could not find containing pool")
    }
  }

  /**
    * Access a specific `NumberPool` in a way that doesn't invoke a callback and reset one of its numbers.
    * To avoid fully processing the callback, an id of `Long.MinValue` is used to short circuit the routine.
    * @param number the number that was drawn from a `NumberPool`
    * @param poolName the `NumberPool` from which the `number` was drawn
    * @see `UniqueNumberSystem.UnregistrationProcess(Option[GUIDRequest], Int, Int)`
    */
  private def NoCallbackReturnNumber(number: Int, poolName: String): Unit = {
    poolActors(poolName) ! NumberPoolActor.ReturnNumber(number, Some(Long.MinValue))
  }

  /**
    * Access a specific `NumberPool` in a way that doesn't invoke a callback and claim one of its numbers.
    * @param number the number to be drawn from a `NumberPool`
    */
  private def NoCallbackGetSpecificNumber(number: Int): Unit = {
    guid.WhichPool(number) match {
      case Some(pname) =>
        NoCallbackGetSpecificNumber(number, pname)
      case None =>
        log.error(s"critical: tried to re-register number $number but could not find containing pool")
    }
  }

  /**
    * Access a specific `NumberPool` in a way that doesn't invoke a callback and claim one of its numbers.
    * To avoid fully processing the callback, an id of `Long.MinValue` is used to short circuit the routine.
    * @param number the number to be drawn from a `NumberPool`
    * @param poolName the `NumberPool` from which the `number` is to be drawn
    * @see `UniqueNumberSystem.RegistrationProcess(Option[GUIDRequest], Int, Int)`
    */
  private def NoCallbackGetSpecificNumber(number: Int, poolName: String): Unit = {
    poolActors(poolName) ! NumberPoolActor.GetSpecificNumber(number, Some(Long.MinValue))
  }
}

object UniqueNumberSystem {

  /**
    * Persistent record of the important information between the time fo request and the time of reply.
    * @param target the object
    * @param targetPool the name of the `NumberPool` being used
    * @param replyTo the callback `ActorRef`
    */
  private final case class GUIDRequest(target: IdentifiableEntity, targetPool: String, replyTo: ActorRef)

  /**
    * Transform `NumberPool`s into `NumberPoolActor`s and pair them with their name.
    * @param poolSource where the raw `NumberPools` are located
    * @param context used to create the `NumberPoolActor` instances
    * @return a `Map` of the pool names to the `ActorRef` created from the `NumberPool`
    */
  def AllocateNumberPoolActors(poolSource: NumberPoolHub)(implicit context: ActorContext): Map[String, ActorRef] = {
    poolSource.Pools
      .map({
        case ((pname, pool)) =>
          pname -> context.actorOf(Props(classOf[NumberPoolActor], pool), pname)
      })
      .toMap
  }
}
